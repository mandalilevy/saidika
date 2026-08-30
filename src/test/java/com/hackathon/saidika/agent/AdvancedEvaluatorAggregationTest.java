package com.hackathon.saidika.agent;

import com.hackathon.saidika.domain.ServiceType;
import com.hackathon.saidika.evaluation.AdvancedEvaluator;
import com.hackathon.saidika.evaluation.AdvancedMetrics;
import com.hackathon.saidika.evaluation.AdvancedScenarioResult;
import com.hackathon.saidika.evaluation.EvaluationScenario;
import com.hackathon.saidika.evaluation.EvaluationSummary;
import com.hackathon.saidika.evaluation.ExpectedOutcome;
import com.hackathon.saidika.evaluation.ScenarioCategory;
import com.hackathon.saidika.evaluation.Verdict;
import com.hackathon.saidika.research.ResearchStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Deterministic unit test for {@link AdvancedEvaluator}'s scoring/aggregation logic: the real
 * {@link RoadsideAssistanceAgent} is mocked so this test needs no Ollama, no network and no Spring
 * context, keeping ".\mvnw.cmd test" fully deterministic. The real agent is exercised separately by
 * {@code AdvancedEvaluationIT}. Lives in the agent package so it can build {@link AgentAssistanceResult}
 * instances via the package-private {@code success} factory, exactly as {@link RoadsideAssistanceAgent} does.
 */
class AdvancedEvaluatorAggregationTest {

    @Test
    void aggregatesAgentResultsIntoScoredScenariosAndMetrics() {
        RoadsideAssistanceAgent agent = mock(RoadsideAssistanceAgent.class);

        EvaluationScenario correctScenario = new EvaluationScenario("T1", ScenarioCategory.DIRECT_REQUEST,
                "My battery is dead.", -1.28, 36.81, ServiceType.JUMP_START,
                true, ExpectedOutcome.MATCHED, null, "rationale");
        EvaluationScenario wrongScenario = new EvaluationScenario("T2", ScenarioCategory.DIRECT_REQUEST,
                "Flat tyre.", -1.28, 36.81, ServiceType.TYRE_ASSISTANCE,
                true, ExpectedOutcome.MATCHED, null, "rationale");

        when(agent.assist(any()))
                .thenReturn(successResult(ServiceType.JUMP_START, "Westlands Battery Care"))
                .thenReturn(successResult(ServiceType.LOCKSMITH, "CBD Lock & Go"));

        AdvancedEvaluator evaluator = new AdvancedEvaluator(agent);
        List<AdvancedScenarioResult> results = evaluator.evaluate(List.of(correctScenario, wrongScenario));

        assertThat(results).hasSize(2);
        assertThat(results.get(0).overallVerdict()).isEqualTo(Verdict.CORRECT);
        assertThat(results.get(1).overallVerdict()).isEqualTo(Verdict.INCORRECT);
        assertThat(results.get(1).failureReason()).contains("Classification mismatch");

        EvaluationSummary summary = new EvaluationSummary(results.stream().map(AdvancedScenarioResult::toScenarioResult).toList());
        assertThat(summary.getEndToEndPass()).isEqualTo(1);
        assertThat(summary.getEndToEndFail()).isEqualTo(1);

        AdvancedMetrics metrics = new AdvancedMetrics(results);
        assertThat(metrics.getResearchEligibleCount()).isEqualTo(2);
        assertThat(metrics.getPublicInfoFoundCount()).isEqualTo(2);
    }

    private AgentAssistanceResult successResult(ServiceType serviceType, String providerName) {
        ResearchedProviderView view = new ResearchedProviderView(providerName, serviceType, 1.0, -1.28, 36.81, true,
                null, "Some Street, Nairobi", null, "https://nominatim.openstreetmap.org/reverse",
                ResearchStatus.PUBLIC_INFORMATION_FOUND, "found", true);
        return AgentAssistanceResult.success(serviceType, false, "explanation", false, view, List.of());
    }
}
