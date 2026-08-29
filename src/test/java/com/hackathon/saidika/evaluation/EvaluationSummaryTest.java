package com.hackathon.saidika.evaluation;

import com.hackathon.saidika.domain.ServiceType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Pure unit test for the evaluation harness's own scoring/aggregation logic (no Spring context, no baseline behavior). */
class EvaluationSummaryTest {

    @Test
    void aggregatesPassFailAndNotScoredCounts() {
        List<ScenarioResult> results = List.of(
                new ScenarioResult("X1", ScenarioCategory.DIRECT_REQUEST, "req1",
                        ServiceType.JUMP_START, ServiceType.JUMP_START, Verdict.CORRECT,
                        null, null, Verdict.NOT_APPLICABLE,
                        ExpectedOutcome.UNSUPPORTED, ExpectedOutcome.UNSUPPORTED, Verdict.CORRECT, null),
                new ScenarioResult("X2", ScenarioCategory.PROVIDER_MATCHING, "req2",
                        ServiceType.TOWING, ServiceType.TOWING, Verdict.CORRECT,
                        "Alpha", "Bravo", Verdict.INCORRECT,
                        ExpectedOutcome.MATCHED, ExpectedOutcome.MATCHED, Verdict.INCORRECT, "Provider mismatch"),
                new ScenarioResult("X3", ScenarioCategory.AMBIGUOUS, "req3",
                        null, null, Verdict.NOT_SCORED,
                        null, null, Verdict.NOT_APPLICABLE,
                        ExpectedOutcome.AMBIGUOUS, ExpectedOutcome.UNSUPPORTED, Verdict.NOT_SCORED, null)
        );

        EvaluationSummary summary = new EvaluationSummary(results);

        assertThat(summary.getTotalScenarios()).isEqualTo(3);
        assertThat(summary.getStrictlyScoredCount()).isEqualTo(2);
        assertThat(summary.getNotScoredCount()).isEqualTo(1);
        assertThat(summary.getEndToEndPass()).isEqualTo(1);
        assertThat(summary.getEndToEndFail()).isEqualTo(1);
        assertThat(summary.getEndToEndResolutionAccuracy()).isEqualTo(50.0);
        assertThat(summary.getProviderScored()).isEqualTo(1);
        assertThat(summary.getProviderCorrect()).isEqualTo(0);
        assertThat(summary.getProviderMatchingAccuracy()).isEqualTo(0.0);
        assertThat(summary.getFailures()).hasSize(1);
        assertThat(summary.getFailures().get(0).scenarioId()).isEqualTo("X2");
    }
}
