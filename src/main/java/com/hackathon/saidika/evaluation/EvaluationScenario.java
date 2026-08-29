package com.hackathon.saidika.evaluation;

import com.hackathon.saidika.domain.ServiceType;

/**
 * A single fixed roadside-assistance evaluation scenario.
 * Coordinates are deterministic (never derived from browser geolocation) so the
 * evaluation harness produces identical results on every run.
 */
public record EvaluationScenario(
        String id,
        ScenarioCategory category,
        String requestText,
        double latitude,
        double longitude,
        ServiceType expectedServiceType,
        boolean strictlyScored,
        ExpectedOutcome expectedOutcome,
        String expectedProviderName,
        String rationale
) {
}
