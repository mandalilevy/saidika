package com.hackathon.saidika.evaluation;

import com.hackathon.saidika.domain.ClassificationResult;
import com.hackathon.saidika.domain.Location;
import com.hackathon.saidika.domain.MatchResult;
import com.hackathon.saidika.domain.ServiceType;
import com.hackathon.saidika.service.ProviderMatchingService;
import com.hackathon.saidika.service.ServiceClassificationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Runs fixed {@link EvaluationScenario}s through the real, unmodified baseline classification and
 * provider-matching services (the same beans used by AssistanceController) and records the results.
 * This class contains no classification or matching business logic of its own.
 */
@Service
public class BaselineEvaluator {

    private final ServiceClassificationService classificationService;
    private final ProviderMatchingService providerMatchingService;

    public BaselineEvaluator(ServiceClassificationService classificationService,
                              ProviderMatchingService providerMatchingService) {
        this.classificationService = classificationService;
        this.providerMatchingService = providerMatchingService;
    }

    public List<ScenarioResult> evaluate(List<EvaluationScenario> scenarios) {
        return scenarios.stream().map(this::evaluateScenario).toList();
    }

    private ScenarioResult evaluateScenario(EvaluationScenario scenario) {
        ClassificationResult classification = classificationService.classify(scenario.requestText());
        ServiceType actualServiceType = classification.isRecognized() ? classification.getServiceType() : null;

        String actualProviderName = null;
        ExpectedOutcome actualOutcome;

        if (!classification.isRecognized()) {
            actualOutcome = ExpectedOutcome.UNSUPPORTED;
        } else {
            Location location = new Location(scenario.latitude(), scenario.longitude());
            Optional<MatchResult> match = providerMatchingService.findBestProvider(actualServiceType, location);
            if (match.isPresent()) {
                actualProviderName = match.get().getProvider().getName();
                actualOutcome = ExpectedOutcome.MATCHED;
            } else {
                actualOutcome = ExpectedOutcome.NO_PROVIDER;
            }
        }

        Verdict classificationVerdict = classificationVerdict(scenario, classification.isRecognized(), actualServiceType);
        Verdict providerVerdict = providerVerdict(scenario, classificationVerdict, actualProviderName, actualOutcome);
        Verdict overallVerdict = overallVerdict(scenario, classificationVerdict, providerVerdict, actualOutcome);
        String failureReason = overallVerdict == Verdict.INCORRECT
                ? buildFailureReason(scenario, classificationVerdict, actualServiceType, providerVerdict, actualProviderName, actualOutcome)
                : null;

        return new ScenarioResult(
                scenario.id(), scenario.category(), scenario.requestText(),
                scenario.expectedServiceType(), actualServiceType, classificationVerdict,
                scenario.expectedProviderName(), actualProviderName, providerVerdict,
                scenario.expectedOutcome(), actualOutcome, overallVerdict, failureReason);
    }

    private Verdict classificationVerdict(EvaluationScenario scenario, boolean recognized, ServiceType actualServiceType) {
        if (!scenario.strictlyScored()) {
            return Verdict.NOT_SCORED;
        }
        if (scenario.expectedOutcome() == ExpectedOutcome.UNSUPPORTED) {
            return !recognized ? Verdict.CORRECT : Verdict.INCORRECT;
        }
        boolean correct = recognized && actualServiceType == scenario.expectedServiceType();
        return correct ? Verdict.CORRECT : Verdict.INCORRECT;
    }

    // Only strictly scored when the scenario names a specific expected provider (the PROVIDER_MATCHING
    // category); other MATCHED/NO_PROVIDER scenarios are still checked, but only at the outcome level.
    private Verdict providerVerdict(EvaluationScenario scenario, Verdict classificationVerdict,
                                     String actualProviderName, ExpectedOutcome actualOutcome) {
        if (scenario.expectedProviderName() == null) {
            return Verdict.NOT_APPLICABLE;
        }
        if (classificationVerdict != Verdict.CORRECT) {
            // Provider stage was fed a misclassified (or unrecognized) service; not a fair provider-matching comparison.
            return Verdict.NOT_APPLICABLE;
        }
        boolean correct = actualOutcome == ExpectedOutcome.MATCHED
                && scenario.expectedProviderName().equals(actualProviderName);
        return correct ? Verdict.CORRECT : Verdict.INCORRECT;
    }

    private Verdict overallVerdict(EvaluationScenario scenario, Verdict classificationVerdict, Verdict providerVerdict,
                                    ExpectedOutcome actualOutcome) {
        if (!scenario.strictlyScored()) {
            return Verdict.NOT_SCORED;
        }
        boolean classificationOk = classificationVerdict == Verdict.CORRECT;
        boolean outcomeOk = actualOutcome == scenario.expectedOutcome();
        boolean providerOk = providerVerdict == Verdict.CORRECT || providerVerdict == Verdict.NOT_APPLICABLE;
        return (classificationOk && outcomeOk && providerOk) ? Verdict.CORRECT : Verdict.INCORRECT;
    }

    private String buildFailureReason(EvaluationScenario scenario, Verdict classificationVerdict, ServiceType actualServiceType,
                                       Verdict providerVerdict, String actualProviderName, ExpectedOutcome actualOutcome) {
        if (classificationVerdict == Verdict.INCORRECT) {
            String expected = scenario.expectedOutcome() == ExpectedOutcome.UNSUPPORTED
                    ? "unrecognized"
                    : String.valueOf(scenario.expectedServiceType());
            String actual = actualServiceType == null ? "unrecognized" : actualServiceType.name();
            return "Classification mismatch: expected " + expected + " but got " + actual + ".";
        }
        if (providerVerdict == Verdict.INCORRECT) {
            return "Provider mismatch: expected '" + scenario.expectedProviderName()
                    + "' but got '" + (actualProviderName == null ? "no provider" : actualProviderName)
                    + "' (actual outcome: " + actualOutcome + ").";
        }
        if (actualOutcome != scenario.expectedOutcome()) {
            return "Outcome mismatch: expected " + scenario.expectedOutcome() + " but got " + actualOutcome
                    + (actualProviderName != null ? " (matched provider: " + actualProviderName + ")" : "") + ".";
        }
        return "Scenario did not meet expected outcome.";
    }
}
