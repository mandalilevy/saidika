package com.hackathon.saidika.evaluation;

import java.util.List;
import java.util.Locale;

/** Formats an advanced-evaluation run as a human-readable report and as a hand-written JSON document. */
public final class AdvancedEvaluationReportFormatter {

    private AdvancedEvaluationReportFormatter() {
    }

    public static String toHumanReadableReport(EvaluationSummary summary, AdvancedMetrics metrics) {
        StringBuilder sb = new StringBuilder();
        sb.append("Advanced (Agent) Evaluation\n");
        sb.append("---------------------------\n");
        sb.append("Total scenarios: ").append(summary.getTotalScenarios()).append('\n');
        sb.append(String.format(Locale.ROOT, "Strict classification accuracy: %.1f%% (%d/%d scored)%n",
                summary.getStrictClassificationAccuracy(), summary.getClassificationCorrect(), summary.getClassificationScored()));
        sb.append(String.format(Locale.ROOT, "Provider matching accuracy: %.1f%% (%d/%d scored)%n",
                summary.getProviderMatchingAccuracy(), summary.getProviderCorrect(), summary.getProviderScored()));
        sb.append(String.format(Locale.ROOT, "End-to-end resolution accuracy: %.1f%% (%d/%d strictly scored)%n",
                summary.getEndToEndResolutionAccuracy(), summary.getEndToEndPass(), summary.getStrictlyScoredCount()));
        sb.append("Passes: ").append(summary.getEndToEndPass())
                .append(", Failures: ").append(summary.getEndToEndFail())
                .append(", Ambiguous/not-strictly-scored: ").append(summary.getNotScoredCount()).append('\n');
        sb.append('\n');

        sb.append("Category results:\n");
        for (ScenarioCategory category : ScenarioCategory.values()) {
            EvaluationSummary.CategoryStats stats = summary.getCategoryStats().get(category);
            if (stats == null) {
                continue;
            }
            sb.append("- ").append(category.getDisplayName()).append(": ")
                    .append(stats.pass()).append(" pass / ")
                    .append(stats.fail()).append(" fail");
            if (stats.notScored() > 0) {
                sb.append(" / ").append(stats.notScored()).append(" not scored");
            }
            sb.append(" (of ").append(stats.total()).append(")\n");
        }
        sb.append('\n');

        sb.append("Agent-specific metrics:\n");
        sb.append(String.format(Locale.ROOT, "- Deterministic fallback classifier used: %d/%d scenarios (%.1f%%)%n",
                metrics.getFallbackClassifierUsedCount(), metrics.getTotalScenarios(), metrics.getFallbackUsageRate()));
        sb.append(String.format(Locale.ROOT, "- Provider research attempted: %d/%d matched scenarios (%.1f%%)%n",
                metrics.getResearchAttemptedCount(), metrics.getResearchEligibleCount(), metrics.getResearchAttemptRate()));
        sb.append(String.format(Locale.ROOT, "- Provenance retained (verified or public info found): %.1f%%%n",
                metrics.getProvenanceRetentionRate()));
        sb.append(String.format(Locale.ROOT, "- Verification success rate: %.1f%% (%d verified / %d public info found / %d unavailable)%n",
                metrics.getVerificationSuccessRate(), metrics.getVerifiedCount(), metrics.getPublicInfoFoundCount(), metrics.getInformationUnavailableCount()));
        sb.append('\n');

        List<ScenarioResult> failures = summary.getFailures();
        sb.append("Most important failures:\n");
        if (failures.isEmpty()) {
            sb.append("(none)\n");
        } else {
            int rank = 1;
            for (ScenarioResult failure : failures) {
                sb.append(rank++).append(". [").append(failure.scenarioId()).append("] ")
                        .append(failure.requestText()).append(" -> ").append(failure.failureReason()).append('\n');
            }
        }
        return sb.toString();
    }

    public static String toJson(EvaluationSummary summary, AdvancedMetrics metrics) {
        String baseJson = EvaluationReportFormatter.toJson(summary).stripTrailing();
        String withoutClosingBrace = baseJson.substring(0, baseJson.length() - 1);
        StringBuilder sb = new StringBuilder(withoutClosingBrace);
        sb.append(",\n  \"advancedMetrics\": {\n");
        sb.append("    \"fallbackClassifierUsedCount\": ").append(metrics.getFallbackClassifierUsedCount()).append(",\n");
        sb.append("    \"fallbackUsageRatePercent\": ").append(round1(metrics.getFallbackUsageRate())).append(",\n");
        sb.append("    \"researchAttemptedCount\": ").append(metrics.getResearchAttemptedCount()).append(",\n");
        sb.append("    \"researchEligibleCount\": ").append(metrics.getResearchEligibleCount()).append(",\n");
        sb.append("    \"provenanceRetentionRatePercent\": ").append(round1(metrics.getProvenanceRetentionRate())).append(",\n");
        sb.append("    \"verificationSuccessRatePercent\": ").append(round1(metrics.getVerificationSuccessRate())).append(",\n");
        sb.append("    \"verifiedCount\": ").append(metrics.getVerifiedCount()).append(",\n");
        sb.append("    \"publicInfoFoundCount\": ").append(metrics.getPublicInfoFoundCount()).append(",\n");
        sb.append("    \"informationUnavailableCount\": ").append(metrics.getInformationUnavailableCount()).append("\n");
        sb.append("  }\n}\n");
        return sb.toString();
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
