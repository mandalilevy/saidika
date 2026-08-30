package com.hackathon.saidika.agent;

import com.hackathon.saidika.domain.ServiceType;

import java.util.List;

/** Structured, final output of {@link RoadsideAssistanceAgent#assist}. Rendered directly by Thymeleaf. */
public class AgentAssistanceResult {

    private final boolean recognized;
    private final boolean providerFound;
    private final ServiceType serviceType;
    private final boolean ambiguous;
    private final String explanation;
    private final boolean usedFallbackClassifier;
    private final ResearchedProviderView primaryProvider;
    private final List<ProviderCandidate> alternateProviders;
    private final String errorMessage;

    private AgentAssistanceResult(boolean recognized, boolean providerFound, ServiceType serviceType, boolean ambiguous,
                                   String explanation, boolean usedFallbackClassifier, ResearchedProviderView primaryProvider,
                                   List<ProviderCandidate> alternateProviders, String errorMessage) {
        this.recognized = recognized;
        this.providerFound = providerFound;
        this.serviceType = serviceType;
        this.ambiguous = ambiguous;
        this.explanation = explanation;
        this.usedFallbackClassifier = usedFallbackClassifier;
        this.primaryProvider = primaryProvider;
        this.alternateProviders = alternateProviders;
        this.errorMessage = errorMessage;
    }

    static AgentAssistanceResult invalidInput(String message) {
        return new AgentAssistanceResult(false, false, null, false, null, false, null, List.of(), message);
    }

    static AgentAssistanceResult unresolved(String explanation, boolean usedFallback) {
        return new AgentAssistanceResult(false, false, null, true, explanation, usedFallback, null, List.of(),
                "Saidika could not confidently identify the roadside service you need. " + explanation);
    }

    static AgentAssistanceResult noProvider(ServiceType serviceType, boolean ambiguous, String explanation, boolean usedFallback) {
        return new AgentAssistanceResult(true, false, serviceType, ambiguous, explanation, usedFallback, null, List.of(),
                "No eligible provider is available for this service right now.");
    }

    static AgentAssistanceResult success(ServiceType serviceType, boolean ambiguous, String explanation, boolean usedFallback,
                                          ResearchedProviderView primaryProvider, List<ProviderCandidate> alternateProviders) {
        return new AgentAssistanceResult(true, true, serviceType, ambiguous, explanation, usedFallback, primaryProvider,
                alternateProviders, null);
    }

    public boolean isRecognized() {
        return recognized;
    }

    public boolean isProviderFound() {
        return providerFound;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public boolean isAmbiguous() {
        return ambiguous;
    }

    public String getExplanation() {
        return explanation;
    }

    public boolean isUsedFallbackClassifier() {
        return usedFallbackClassifier;
    }

    public ResearchedProviderView getPrimaryProvider() {
        return primaryProvider;
    }

    public List<ProviderCandidate> getAlternateProviders() {
        return alternateProviders;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
