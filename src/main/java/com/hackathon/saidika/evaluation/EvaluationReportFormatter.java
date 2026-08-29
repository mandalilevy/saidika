package com.hackathon.saidika.evaluation;

import java.util.List;
import java.util.Locale;

/** Formats an {@link EvaluationSummary} as a human-readable report and as a hand-written JSON document. */
public final class EvaluationReportFormatter {

    private EvaluationReportFormatter() {
    }

    public static String toHumanReadableReport(EvaluationSummary summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("Baseline Evaluation\n");
        sb.append("-------------------\n");
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

    public static String toJson(EvaluationSummary summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"totalScenarios\": ").append(summary.getTotalScenarios()).append(",\n");
        sb.append("  \"strictClassificationAccuracy\": ").append(round1(summary.getStrictClassificationAccuracy())).append(",\n");
        sb.append("  \"providerMatchingAccuracy\": ").append(round1(summary.getProviderMatchingAccuracy())).append(",\n");
        sb.append("  \"endToEndResolutionAccuracy\": ").append(round1(summary.getEndToEndResolutionAccuracy())).append(",\n");
        sb.append("  \"passes\": ").append(summary.getEndToEndPass()).append(",\n");
        sb.append("  \"failures\": ").append(summary.getEndToEndFail()).append(",\n");
        sb.append("  \"notStrictlyScored\": ").append(summary.getNotScoredCount()).append(",\n");
        sb.append("  \"categories\": [\n");
        List<ScenarioCategory> categories = List.of(ScenarioCategory.values());
        for (int i = 0; i < categories.size(); i++) {
            ScenarioCategory category = categories.get(i);
            EvaluationSummary.CategoryStats stats = summary.getCategoryStats().get(category);
            if (stats == null) {
                continue;
            }
            sb.append("    {\"category\": \"").append(category.name()).append("\", ")
                    .append("\"total\": ").append(stats.total()).append(", ")
                    .append("\"pass\": ").append(stats.pass()).append(", ")
                    .append("\"fail\": ").append(stats.fail()).append(", ")
                    .append("\"notScored\": ").append(stats.notScored()).append("}")
                    .append(i < categories.size() - 1 ? ",\n" : "\n");
        }
        sb.append("  ],\n");
        sb.append("  \"scenarios\": [\n");
        List<ScenarioResult> results = summary.getResults();
        for (int i = 0; i < results.size(); i++) {
            sb.append("    ").append(scenarioToJson(results.get(i)));
            sb.append(i < results.size() - 1 ? ",\n" : "\n");
        }
        sb.append("  ]\n");
        sb.append("}\n");
        return sb.toString();
    }

    private static String scenarioToJson(ScenarioResult r) {
        return "{" +
                "\"scenarioId\": " + jsonString(r.scenarioId()) + ", " +
                "\"category\": " + jsonString(r.category().name()) + ", " +
                "\"requestText\": " + jsonString(r.requestText()) + ", " +
                "\"expectedServiceType\": " + jsonString(r.expectedServiceType() == null ? null : r.expectedServiceType().name()) + ", " +
                "\"actualServiceType\": " + jsonString(r.actualServiceType() == null ? null : r.actualServiceType().name()) + ", " +
                "\"classificationVerdict\": " + jsonString(r.classificationVerdict().name()) + ", " +
                "\"expectedProviderName\": " + jsonString(r.expectedProviderName()) + ", " +
                "\"actualProviderName\": " + jsonString(r.actualProviderName()) + ", " +
                "\"providerVerdict\": " + jsonString(r.providerVerdict().name()) + ", " +
                "\"expectedOutcome\": " + jsonString(r.expectedOutcome().name()) + ", " +
                "\"actualOutcome\": " + jsonString(r.actualOutcome() == null ? null : r.actualOutcome().name()) + ", " +
                "\"overallVerdict\": " + jsonString(r.overallVerdict().name()) + ", " +
                "\"failureReason\": " + jsonString(r.failureReason()) +
                "}";
    }

    private static String jsonString(String value) {
        if (value == null) {
            return "null";
        }
        String escaped = value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
        return "\"" + escaped + "\"";
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
