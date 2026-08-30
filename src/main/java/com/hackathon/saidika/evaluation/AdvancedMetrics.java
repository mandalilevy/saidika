package com.hackathon.saidika.evaluation;

import java.util.List;

/**
 * Agent-only metrics not applicable to the baseline: fallback-classifier usage, and information-retrieval
 * / provenance / verification success, measured only over scenarios where a provider was actually matched
 * (research is only attempted once a provider has been selected).
 */
public final class AdvancedMetrics {

    private final int totalScenarios;
    private final int fallbackClassifierUsedCount;
    private final int researchAttemptedCount;
    private final int researchEligibleCount;
    private final int verifiedCount;
    private final int publicInfoFoundCount;
    private final int informationUnavailableCount;

    public AdvancedMetrics(List<AdvancedScenarioResult> results) {
        this.totalScenarios = results.size();
        int fallback = 0;
        int attempted = 0;
        int eligible = 0;
        int verified = 0;
        int publicFound = 0;
        int unavailable = 0;

        for (AdvancedScenarioResult r : results) {
            if (r.usedFallbackClassifier()) {
                fallback++;
            }
            if (r.actualOutcome() == ExpectedOutcome.MATCHED) {
                eligible++;
                if (r.researchAttempted()) {
                    attempted++;
                }
                if (r.verificationStatus() != null) {
                    switch (r.verificationStatus()) {
                        case VERIFIED -> verified++;
                        case PUBLIC_INFORMATION_FOUND -> publicFound++;
                        case INFORMATION_UNAVAILABLE -> unavailable++;
                    }
                }
            }
        }

        this.fallbackClassifierUsedCount = fallback;
        this.researchAttemptedCount = attempted;
        this.researchEligibleCount = eligible;
        this.verifiedCount = verified;
        this.publicInfoFoundCount = publicFound;
        this.informationUnavailableCount = unavailable;
    }

    public int getTotalScenarios() {
        return totalScenarios;
    }

    public int getFallbackClassifierUsedCount() {
        return fallbackClassifierUsedCount;
    }

    public double getFallbackUsageRate() {
        return totalScenarios == 0 ? 0.0 : 100.0 * fallbackClassifierUsedCount / totalScenarios;
    }

    public int getResearchAttemptedCount() {
        return researchAttemptedCount;
    }

    public int getResearchEligibleCount() {
        return researchEligibleCount;
    }

    public double getResearchAttemptRate() {
        return researchEligibleCount == 0 ? 0.0 : 100.0 * researchAttemptedCount / researchEligibleCount;
    }

    public int getVerifiedCount() {
        return verifiedCount;
    }

    public int getPublicInfoFoundCount() {
        return publicInfoFoundCount;
    }

    public int getInformationUnavailableCount() {
        return informationUnavailableCount;
    }

    public double getProvenanceRetentionRate() {
        int withProvenance = verifiedCount + publicInfoFoundCount;
        return researchEligibleCount == 0 ? 0.0 : 100.0 * withProvenance / researchEligibleCount;
    }

    public double getVerificationSuccessRate() {
        return researchEligibleCount == 0 ? 0.0 : 100.0 * verifiedCount / researchEligibleCount;
    }
}
