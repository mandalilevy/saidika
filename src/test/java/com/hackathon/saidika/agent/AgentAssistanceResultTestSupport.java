package com.hackathon.saidika.agent;

/** Test-only helper exposing package-private {@link AgentAssistanceResult} factories to other test packages. */
public final class AgentAssistanceResultTestSupport {

    private AgentAssistanceResultTestSupport() {
    }

    public static AgentAssistanceResult unresolved(String explanation) {
        return AgentAssistanceResult.unresolved(explanation, false);
    }
}
