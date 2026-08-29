package com.hackathon.saidika.evaluation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Executes the frozen baseline evaluation dataset against the real classification and
 * provider-matching services and writes a human-readable report plus a JSON result file.
 *
 * This is a measurement harness, not a correctness test of the baseline itself: individual
 * scenario failures are expected to surface genuine baseline weaknesses and must not fail the build.
 */
@SpringBootTest
class BaselineEvaluationTest {

    @Autowired
    private BaselineEvaluator evaluator;

    @Test
    void runBaselineEvaluation() throws IOException {
        List<EvaluationScenario> scenarios = EvaluationDataset.scenarios();
        assertThat(scenarios).hasSize(24);

        List<ScenarioResult> results = evaluator.evaluate(scenarios);
        assertThat(results).hasSameSizeAs(scenarios);

        EvaluationSummary summary = new EvaluationSummary(results);

        String humanReadableReport = EvaluationReportFormatter.toHumanReadableReport(summary);
        String jsonReport = EvaluationReportFormatter.toJson(summary);

        Path outputDir = Path.of("target", "evaluation");
        Files.createDirectories(outputDir);
        Files.writeString(outputDir.resolve("baseline-evaluation-report.txt"), humanReadableReport);
        Files.writeString(outputDir.resolve("baseline-evaluation-report.json"), jsonReport);

        System.out.println(humanReadableReport);
    }
}
