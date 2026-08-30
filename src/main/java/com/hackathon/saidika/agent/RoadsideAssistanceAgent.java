package com.hackathon.saidika.agent;

import com.hackathon.saidika.agent.tool.ProviderLookupTool;
import com.hackathon.saidika.ai.LanguageModelGateway;
import com.hackathon.saidika.domain.ClassificationResult;
import com.hackathon.saidika.domain.Location;
import com.hackathon.saidika.domain.MatchResult;
import com.hackathon.saidika.domain.Provider;
import com.hackathon.saidika.research.ProviderInformationVerifier;
import com.hackathon.saidika.research.ProviderResearchGateway;
import com.hackathon.saidika.research.ProviderResearchResult;
import com.hackathon.saidika.service.ServiceClassificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Agent orchestrator: Qwen3 (via {@link LanguageModelGateway}) interprets the driver's request and
 * decides the service type; every geographic/provider fact stays authoritative in deterministic Java
 * ({@link ProviderLookupTool} -&gt; {@code ProviderMatchingService}). Falls back to the deterministic
 * baseline classifier whenever the model is unavailable or returns unusable output, so the app degrades
 * safely without the LLM.
 */
@Service
public class RoadsideAssistanceAgent {

    private static final int MAX_ALTERNATE_PROVIDERS = 2;

    private final LanguageModelGateway languageModelGateway;
    private final ServiceClassificationService fallbackClassifier;
    private final ProviderLookupTool providerLookupTool;
    private final ProviderResearchGateway researchGateway;
    private final ProviderInformationVerifier verifier;
    private final AgentClassificationParser parser = new AgentClassificationParser();
    private final long classificationTimeoutMs;

    public RoadsideAssistanceAgent(LanguageModelGateway languageModelGateway,
                                    ServiceClassificationService fallbackClassifier,
                                    ProviderLookupTool providerLookupTool,
                                    ProviderResearchGateway researchGateway,
                                    ProviderInformationVerifier verifier,
                                    @Value("${saidika.agent.classification-timeout-ms:20000}") long classificationTimeoutMs) {
        this.languageModelGateway = languageModelGateway;
        this.fallbackClassifier = fallbackClassifier;
        this.providerLookupTool = providerLookupTool;
        this.researchGateway = researchGateway;
        this.verifier = verifier;
        this.classificationTimeoutMs = classificationTimeoutMs;
    }

    public AgentAssistanceResult assist(AgentRequest request) {
        if (request.requestText() == null || request.requestText().isBlank()) {
            return AgentAssistanceResult.invalidInput("Please describe the roadside problem.");
        }

        Location location;
        try {
            location = new Location(request.latitude(), request.longitude());
        } catch (IllegalArgumentException ex) {
            return AgentAssistanceResult.invalidInput(ex.getMessage());
        }

        AgentClassification classification = classify(request.requestText());
        if (classification.serviceType() == null) {
            return AgentAssistanceResult.unresolved(classification.explanation(), classification.usedFallbackClassifier());
        }

        List<MatchResult> eligible = providerLookupTool.findNearbyProviders(
                classification.serviceType(), location, MAX_ALTERNATE_PROVIDERS + 1);

        if (eligible.isEmpty()) {
            return AgentAssistanceResult.noProvider(classification.serviceType(), classification.ambiguous(),
                    classification.explanation(), classification.usedFallbackClassifier());
        }

        MatchResult primary = eligible.get(0);
        ResearchedProviderView primaryView = researchAndVerify(primary);
        List<ProviderCandidate> alternates = eligible.stream().skip(1).map(ProviderCandidate::from).toList();

        return AgentAssistanceResult.success(classification.serviceType(), classification.ambiguous(),
                classification.explanation(), classification.usedFallbackClassifier(), primaryView, alternates);
    }

    private AgentClassification classify(String requestText) {
        try {
            String prompt = AgentPromptBuilder.buildClassificationPrompt(requestText);
            // Bounded regardless of how long Ollama/qwen3 takes (or hangs): the request thread is never
            // blocked past this timeout, so the user can never be stuck indefinitely on the loading screen.
            String raw = CompletableFuture.supplyAsync(() -> languageModelGateway.complete(prompt))
                    .get(classificationTimeoutMs, TimeUnit.MILLISECONDS);
            Optional<AgentClassification> parsed = parser.parse(raw);
            if (parsed.isPresent()) {
                return parsed.get();
            }
        } catch (Exception ex) {
            // Ollama/model unavailable, timed out, or failed; fall through to the deterministic baseline.
        }
        ClassificationResult fallback = fallbackClassifier.classify(requestText);
        if (!fallback.isRecognized()) {
            return new AgentClassification(null, true, fallback.getReason(), true);
        }
        return new AgentClassification(fallback.getServiceType(), false,
                "Deterministic fallback classifier used: " + fallback.getReason(), true);
    }

    private ResearchedProviderView researchAndVerify(MatchResult match) {
        Provider provider = match.getProvider();
        ProviderResearchResult raw;
        boolean researchAttempted;
        try {
            raw = researchGateway.research(provider);
            researchAttempted = true;
        } catch (Exception ex) {
            raw = ProviderResearchResult.unavailable("Provider research failed (" + ex.getClass().getSimpleName() + ").");
            researchAttempted = false;
        }
        ProviderResearchResult verified = verifier.verify(provider, raw);
        return ResearchedProviderView.of(match, verified, researchAttempted);
    }
}
