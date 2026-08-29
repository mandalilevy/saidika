package com.hackathon.saidika.evaluation;

import com.hackathon.saidika.domain.ServiceType;

/** Recorded outcome of running one {@link EvaluationScenario} through the live baseline services. */
public record ScenarioResult(
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
        String failureReason
) {
}
