package com.hackathon.saidika.evaluation;

import com.hackathon.saidika.domain.ServiceType;
import com.hackathon.saidika.research.ResearchStatus;

/** Recorded outcome of running one {@link EvaluationScenario} through the REAL advanced agent. */
public record AdvancedScenarioResult(
        String scenarioId,
        ScenarioCategory category,
        String requestText,
        ServiceType expectedServiceType,
        ServiceType actualServiceType,
        Verdict classificationVerdict,
        String expectedProviderName,
        String actualProviderName,
        Verdict providerVerdict,
        ExpectedOutcome expectedOutcome,
        ExpectedOutcome actualOutcome,
        Verdict overallVerdict,
        boolean usedFallbackClassifier,
        boolean researchAttempted,
        ResearchStatus verificationStatus,
        String failureReason
) {

    /** Adapts this result into the baseline's {@link ScenarioResult} shape so the two systems are comparable. */
    public ScenarioResult toScenarioResult() {
        return new ScenarioResult(scenarioId, category, requestText, expectedServiceType, actualServiceType,
                classificationVerdict, expectedProviderName, actualProviderName, providerVerdict,
                expectedOutcome, actualOutcome, overallVerdict, failureReason);
    }
}
