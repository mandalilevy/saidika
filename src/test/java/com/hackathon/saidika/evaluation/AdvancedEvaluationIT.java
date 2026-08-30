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
 * Executes the SAME frozen scenario dataset used by BaselineEvaluationTest against the REAL advanced
 * agent (real Ollama/Qwen3 classification, real public web research via Nominatim). Deliberately named
 * *IT* (not *Test*) so Surefire's default inclusion pattern skips it during ".\mvnw.cmd test": it
 * requires a running local Ollama instance with qwen3:4b and internet access, so it cannot be part of
 * the deterministic normal test suite.
 *
 * Run explicitly via: .\mvnw.cmd test -Dtest=AdvancedEvaluationIT
 */
@SpringBootTest
class AdvancedEvaluationIT {

    @Autowired
    private AdvancedEvaluator evaluator;

    @Test
    void runAdvancedEvaluation() throws IOException {
        List<EvaluationScenario> scenarios = EvaluationDataset.scenarios();
        assertThat(scenarios).hasSize(24);

        List<AdvancedScenarioResult> results = evaluator.evaluate(scenarios);
        assertThat(results).hasSameSizeAs(scenarios);

        EvaluationSummary summary = new EvaluationSummary(results.stream().map(AdvancedScenarioResult::toScenarioResult).toList());
        AdvancedMetrics metrics = new AdvancedMetrics(results);

        String humanReadableReport = AdvancedEvaluationReportFormatter.toHumanReadableReport(summary, metrics);
        String jsonReport = AdvancedEvaluationReportFormatter.toJson(summary, metrics);

        Path outputDir = Path.of("target", "evaluation");
        Files.createDirectories(outputDir);
        Files.writeString(outputDir.resolve("advanced-evaluation-report.txt"), humanReadableReport);
        Files.writeString(outputDir.resolve("advanced-evaluation-report.json"), jsonReport);

        Path evaluationDir = Path.of("evaluation", "advanced");
        Files.createDirectories(evaluationDir);
        Files.writeString(evaluationDir.resolve("advanced-evaluation-report.txt"), humanReadableReport);
        Files.writeString(evaluationDir.resolve("advanced-evaluation-report.json"), jsonReport);
    }
}
