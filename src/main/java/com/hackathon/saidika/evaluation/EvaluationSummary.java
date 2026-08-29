package com.hackathon.saidika.evaluation;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Aggregated metrics computed from a list of {@link ScenarioResult}s. Contains no scoring business logic of its own. */
public final class EvaluationSummary {

    /** Per-category pass/fail/not-scored counts. */
    public record CategoryStats(ScenarioCategory category, int total, int pass, int fail, int notScored) {
    }

    private final int totalScenarios;
    private final int strictlyScoredCount;
    private final int notScoredCount;

    private final int classificationScored;
    private final int classificationCorrect;

    private final int providerScored;
    private final int providerCorrect;

    private final int endToEndPass;
    private final int endToEndFail;

    private final Map<ScenarioCategory, CategoryStats> categoryStats;
    private final List<ScenarioResult> results;

    public EvaluationSummary(List<ScenarioResult> results) {
        this.results = results;
        this.totalScenarios = results.size();

        int strictlyScored = 0;
        int notScored = 0;
        int classificationScoredCount = 0;
        int classificationCorrectCount = 0;
        int providerScoredCount = 0;
        int providerCorrectCount = 0;
        int pass = 0;
        int fail = 0;

        Map<ScenarioCategory, int[]> categoryCounts = new EnumMap<>(ScenarioCategory.class); // [total, pass, fail, notScored]

        for (ScenarioResult result : results) {
            int[] counts = categoryCounts.computeIfAbsent(result.category(), c -> new int[4]);
            counts[0]++;

            if (result.overallVerdict() == Verdict.NOT_SCORED) {
                notScored++;
                counts[3]++;
            } else {
                strictlyScored++;
                if (result.overallVerdict() == Verdict.CORRECT) {
                    pass++;
                    counts[1]++;
                } else {
                    fail++;
                    counts[2]++;
                }
            }

            if (result.classificationVerdict() != Verdict.NOT_SCORED) {
                classificationScoredCount++;
                if (result.classificationVerdict() == Verdict.CORRECT) {
                    classificationCorrectCount++;
                }
            }

            if (result.providerVerdict() == Verdict.CORRECT || result.providerVerdict() == Verdict.INCORRECT) {
                providerScoredCount++;
                if (result.providerVerdict() == Verdict.CORRECT) {
                    providerCorrectCount++;
                }
            }
        }

        this.strictlyScoredCount = strictlyScored;
        this.notScoredCount = notScored;
        this.classificationScored = classificationScoredCount;
        this.classificationCorrect = classificationCorrectCount;
        this.providerScored = providerScoredCount;
        this.providerCorrect = providerCorrectCount;
        this.endToEndPass = pass;
        this.endToEndFail = fail;

        Map<ScenarioCategory, CategoryStats> stats = new EnumMap<>(ScenarioCategory.class);
        for (Map.Entry<ScenarioCategory, int[]> entry : categoryCounts.entrySet()) {
            int[] c = entry.getValue();
            stats.put(entry.getKey(), new CategoryStats(entry.getKey(), c[0], c[1], c[2], c[3]));
        }
        this.categoryStats = stats;
    }

    public int getTotalScenarios() {
        return totalScenarios;
    }

    public int getStrictlyScoredCount() {
        return strictlyScoredCount;
    }

    public int getNotScoredCount() {
        return notScoredCount;
    }

    public int getEndToEndPass() {
        return endToEndPass;
    }

    public int getEndToEndFail() {
        return endToEndFail;
    }

    public double getStrictClassificationAccuracy() {
        return classificationScored == 0 ? 0.0 : (100.0 * classificationCorrect / classificationScored);
    }

    public double getProviderMatchingAccuracy() {
        return providerScored == 0 ? 0.0 : (100.0 * providerCorrect / providerScored);
    }

    public double getEndToEndResolutionAccuracy() {
        return strictlyScoredCount == 0 ? 0.0 : (100.0 * endToEndPass / strictlyScoredCount);
    }

    public int getClassificationScored() {
        return classificationScored;
    }

    public int getClassificationCorrect() {
        return classificationCorrect;
    }

    public int getProviderScored() {
        return providerScored;
    }

    public int getProviderCorrect() {
        return providerCorrect;
    }

    public Map<ScenarioCategory, CategoryStats> getCategoryStats() {
        return categoryStats;
    }

    public List<ScenarioResult> getResults() {
        return results;
    }

    public List<ScenarioResult> getFailures() {
        return results.stream().filter(r -> r.overallVerdict() == Verdict.INCORRECT).toList();
    }
}
