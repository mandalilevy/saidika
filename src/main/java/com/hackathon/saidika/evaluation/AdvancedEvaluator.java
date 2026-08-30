package com.hackathon.saidika.evaluation;

import com.hackathon.saidika.agent.AgentAssistanceResult;
import com.hackathon.saidika.agent.AgentRequest;
import com.hackathon.saidika.agent.RoadsideAssistanceAgent;
import com.hackathon.saidika.domain.ServiceType;
import com.hackathon.saidika.research.ResearchStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs the frozen {@link EvaluationDataset} scenarios through the REAL advanced agent
 * ({@link RoadsideAssistanceAgent}: Qwen3 classification, tool-based provider lookup, public web research
 * and verification). Contains no classification, matching or research business logic of its own; it only
 * adapts {@link AgentAssistanceResult} into the same {@link Verdict}/outcome shape the baseline evaluator
 * uses, so the two systems can be scored the same way and fairly compared.
 */
@Service
public class AdvancedEvaluator {

    private final RoadsideAssistanceAgent agent;

    public AdvancedEvaluator(RoadsideAssistanceAgent agent) {
        this.agent = agent;
    }

    public List<AdvancedScenarioResult> evaluate(List<EvaluationScenario> scenarios) {
        List<AdvancedScenarioResult> results = new ArrayList<>();
        for (EvaluationScenario scenario : scenarios) {
            results.add(evaluateScenario(scenario));
        }
        return results;
    }

    private AdvancedScenarioResult evaluateScenario(EvaluationScenario scenario) {
        AgentAssistanceResult agentResult = agent.assist(
                new AgentRequest(scenario.requestText(), scenario.latitude(), scenario.longitude()));

        ServiceType actualServiceType = agentResult.isRecognized() ? agentResult.getServiceType() : null;

        String actualProviderName = null;
        ExpectedOutcome actualOutcome;
        boolean researchAttempted = false;
        ResearchStatus verificationStatus = null;

        if (!agentResult.isRecognized()) {
            actualOutcome = ExpectedOutcome.UNSUPPORTED;
        } else if (!agentResult.isProviderFound()) {
            actualOutcome = ExpectedOutcome.NO_PROVIDER;
        } else {
            actualProviderName = agentResult.getPrimaryProvider().getName();
            actualOutcome = ExpectedOutcome.MATCHED;
            researchAttempted = agentResult.getPrimaryProvider().isResearchAttempted();
            verificationStatus = agentResult.getPrimaryProvider().getVerificationStatus();
        }

        Verdict classificationVerdict = classificationVerdict(scenario, agentResult.isRecognized(), actualServiceType);
        Verdict providerVerdict = providerVerdict(scenario, classificationVerdict, actualProviderName, actualOutcome);
        Verdict overallVerdict = overallVerdict(scenario, classificationVerdict, providerVerdict, actualOutcome);
        String failureReason = overallVerdict == Verdict.INCORRECT
                ? buildFailureReason(scenario, classificationVerdict, actualServiceType, providerVerdict, actualProviderName, actualOutcome)
                : null;

        return new AdvancedScenarioResult(
                scenario.id(), scenario.category(), scenario.requestText(),
                scenario.expectedServiceType(), actualServiceType, classificationVerdict,
                scenario.expectedProviderName(), actualProviderName, providerVerdict,
                scenario.expectedOutcome(), actualOutcome, overallVerdict,
                agentResult.isUsedFallbackClassifier(), researchAttempted, verificationStatus, failureReason);
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

    private Verdict providerVerdict(EvaluationScenario scenario, Verdict classificationVerdict,
                                     String actualProviderName, ExpectedOutcome actualOutcome) {
        if (scenario.expectedProviderName() == null) {
            return Verdict.NOT_APPLICABLE;
        }
        if (classificationVerdict != Verdict.CORRECT) {
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
                    ? "unrecognized" : String.valueOf(scenario.expectedServiceType());
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
