package com.hackathon.saidika.agent;

import com.hackathon.saidika.agent.tool.ProviderLookupTool;
import com.hackathon.saidika.ai.LanguageModelGateway;
import com.hackathon.saidika.domain.Location;
import com.hackathon.saidika.domain.MatchResult;
import com.hackathon.saidika.domain.Provider;
import com.hackathon.saidika.domain.ServiceType;
import com.hackathon.saidika.research.ProviderInformationVerifier;
import com.hackathon.saidika.research.ProviderResearchGateway;
import com.hackathon.saidika.research.ProviderResearchResult;
import com.hackathon.saidika.research.ResearchStatus;
import com.hackathon.saidika.service.ServiceClassificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests (mocked LanguageModelGateway/tool/research/verifier, no Spring context, no Ollama, no
 * network) covering: structured-output success, deterministic fallback on LLM failure and on malformed
 * output, provider delegation, provenance retention and graceful error handling.
 */
class RoadsideAssistanceAgentTest {

    private LanguageModelGateway languageModelGateway;
    private ProviderLookupTool providerLookupTool;
    private ProviderResearchGateway researchGateway;
    private ProviderInformationVerifier verifier;
    private RoadsideAssistanceAgent agent;

    private final Provider provider = new Provider("Kilimani Mechanics", new Location(-1.2807, 36.7893),
            true, Set.of(ServiceType.TOWING));
    private final MatchResult matchResult = new MatchResult(ServiceType.TOWING, provider, 2.3, "rationale");

    @BeforeEach
    void setUp() {
        languageModelGateway = mock(LanguageModelGateway.class);
        providerLookupTool = mock(ProviderLookupTool.class);
        researchGateway = mock(ProviderResearchGateway.class);
        verifier = mock(ProviderInformationVerifier.class);
        agent = new RoadsideAssistanceAgent(languageModelGateway, new ServiceClassificationService(),
                providerLookupTool, researchGateway, verifier, 20000L);
    }

    @Test
    void successfulClassificationDelegatesToToolAndResearchesSelectedProvider() {
        when(languageModelGateway.complete(any())).thenReturn(
                "{\"service_type\": \"TOWING\", \"ambiguous\": false, \"explanation\": \"Needs a tow.\"}");
        when(providerLookupTool.findNearbyProviders(eq(ServiceType.TOWING), any(Location.class), eq(3)))
                .thenReturn(List.of(matchResult));
        ProviderResearchResult discovered = new ProviderResearchResult(null, "Some Road, Nairobi", null,
                "https://nominatim.openstreetmap.org/reverse", ResearchStatus.PUBLIC_INFORMATION_FOUND, "found");
        when(researchGateway.research(provider)).thenReturn(discovered);
        when(verifier.verify(provider, discovered)).thenReturn(discovered);

        AgentAssistanceResult result = agent.assist(new AgentRequest("My car needs to be transported to a garage.", -1.28, 36.81));

        assertThat(result.isRecognized()).isTrue();
        assertThat(result.getServiceType()).isEqualTo(ServiceType.TOWING);
        assertThat(result.isUsedFallbackClassifier()).isFalse();
        assertThat(result.isProviderFound()).isTrue();
        assertThat(result.getPrimaryProvider().getName()).isEqualTo("Kilimani Mechanics");
        assertThat(result.getPrimaryProvider().getAddress()).isEqualTo("Some Road, Nairobi");
        assertThat(result.getPrimaryProvider().getSourceUrl()).isEqualTo("https://nominatim.openstreetmap.org/reverse");
        assertThat(result.getPrimaryProvider().getVerificationStatus()).isEqualTo(ResearchStatus.PUBLIC_INFORMATION_FOUND);
    }

    @Test
    void modelUnavailableFallsBackToDeterministicClassifier() {
        when(languageModelGateway.complete(any())).thenThrow(new RuntimeException("connection refused"));
        when(providerLookupTool.findNearbyProviders(any(), any(), anyInt())).thenReturn(List.of(matchResult));
        when(researchGateway.research(any())).thenReturn(ProviderResearchResult.unavailable("n/a"));
        when(verifier.verify(any(), any())).thenReturn(ProviderResearchResult.unavailable("n/a"));

        AgentAssistanceResult result = agent.assist(new AgentRequest("My battery is dead and my car won't start.", -1.28, 36.81));

        assertThat(result.isRecognized()).isTrue();
        assertThat(result.getServiceType()).isEqualTo(ServiceType.JUMP_START);
        assertThat(result.isUsedFallbackClassifier()).isTrue();
    }

    @Test
    void malformedModelOutputFallsBackToDeterministicClassifier() {
        when(languageModelGateway.complete(any())).thenReturn("not valid json at all");
        when(providerLookupTool.findNearbyProviders(any(), any(), anyInt())).thenReturn(List.of(matchResult));
        when(researchGateway.research(any())).thenReturn(ProviderResearchResult.unavailable("n/a"));
        when(verifier.verify(any(), any())).thenReturn(ProviderResearchResult.unavailable("n/a"));

        AgentAssistanceResult result = agent.assist(new AgentRequest("I have a flat tyre and need help.", -1.28, 36.81));

        assertThat(result.getServiceType()).isEqualTo(ServiceType.TYRE_ASSISTANCE);
        assertThat(result.isUsedFallbackClassifier()).isTrue();
    }

    @Test
    void ambiguousModelOutputProducesUnresolvedResultWithoutCallingProviderTool() {
        when(languageModelGateway.complete(any())).thenReturn(
                "{\"service_type\": \"UNKNOWN\", \"ambiguous\": true, \"explanation\": \"Not clear.\"}");

        AgentAssistanceResult result = agent.assist(new AgentRequest("The vehicle stopped suddenly.", -1.28, 36.81));

        assertThat(result.isRecognized()).isFalse();
        assertThat(result.isProviderFound()).isFalse();
        assertThat(result.getErrorMessage()).isNotBlank();
        verify(providerLookupTool, never()).findNearbyProviders(any(), any(), anyInt());
    }

    @Test
    void noEligibleProviderProducesNoProviderResult() {
        when(languageModelGateway.complete(any())).thenReturn(
                "{\"service_type\": \"LOCKSMITH\", \"ambiguous\": false, \"explanation\": \"Locked out.\"}");
        when(providerLookupTool.findNearbyProviders(any(), any(), anyInt())).thenReturn(List.of());

        AgentAssistanceResult result = agent.assist(new AgentRequest("I'm locked out of my car.", -1.55, 36.45));

        assertThat(result.isRecognized()).isTrue();
        assertThat(result.isProviderFound()).isFalse();
        assertThat(result.getErrorMessage()).contains("No eligible provider");
    }

    @Test
    void blankRequestTextIsRejectedWithoutCallingModel() {
        AgentAssistanceResult result = agent.assist(new AgentRequest("   ", -1.28, 36.81));

        assertThat(result.isRecognized()).isFalse();
        assertThat(result.getErrorMessage()).isNotBlank();
        verify(languageModelGateway, never()).complete(any());
    }

    @Test
    void invalidCoordinatesAreRejectedGracefully() {
        AgentAssistanceResult result = agent.assist(new AgentRequest("My battery is dead.", 999.0, 36.81));

        assertThat(result.isRecognized()).isFalse();
        assertThat(result.getErrorMessage()).contains("Latitude");
    }

    @Test
    void slowModelCallTimesOutAndFallsBackInsteadOfHangingIndefinitely() {
        RoadsideAssistanceAgent shortTimeoutAgent = new RoadsideAssistanceAgent(languageModelGateway,
                new ServiceClassificationService(), providerLookupTool, researchGateway, verifier, 200L);
        when(languageModelGateway.complete(any())).thenAnswer(invocation -> {
            Thread.sleep(2000);
            return "{\"service_type\": \"TOWING\", \"ambiguous\": false, \"explanation\": \"too slow\"}";
        });
        when(providerLookupTool.findNearbyProviders(any(), any(), anyInt())).thenReturn(List.of(matchResult));
        when(researchGateway.research(any())).thenReturn(ProviderResearchResult.unavailable("n/a"));
        when(verifier.verify(any(), any())).thenReturn(ProviderResearchResult.unavailable("n/a"));

        long start = System.currentTimeMillis();
        AgentAssistanceResult result = shortTimeoutAgent.assist(
                new AgentRequest("My battery is dead and my car won't start.", -1.28, 36.81));
        long elapsedMs = System.currentTimeMillis() - start;

        assertThat(elapsedMs).isLessThan(1500);
        assertThat(result.isRecognized()).isTrue();
        assertThat(result.getServiceType()).isEqualTo(ServiceType.JUMP_START);
        assertThat(result.isUsedFallbackClassifier()).isTrue();
    }

    @Test
    void researchGatewayFailureIsHandledGracefullyAndMarkedNotAttempted() {
        when(languageModelGateway.complete(any())).thenReturn(
                "{\"service_type\": \"TOWING\", \"ambiguous\": false, \"explanation\": \"Needs a tow.\"}");
        when(providerLookupTool.findNearbyProviders(any(), any(), anyInt())).thenReturn(List.of(matchResult));
        when(researchGateway.research(any())).thenThrow(new RuntimeException("network down"));
        when(verifier.verify(any(), any())).thenAnswer(invocation -> invocation.getArgument(1));

        AgentAssistanceResult result = agent.assist(new AgentRequest("My car needs to be transported to a garage.", -1.28, 36.81));

        assertThat(result.getPrimaryProvider().isResearchAttempted()).isFalse();
        assertThat(result.getPrimaryProvider().getVerificationStatus()).isEqualTo(ResearchStatus.INFORMATION_UNAVAILABLE);
    }
}
