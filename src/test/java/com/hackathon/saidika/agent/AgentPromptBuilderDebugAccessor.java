package com.hackathon.saidika.agent;

/** Test-only accessor to the package-private {@link AgentPromptBuilder} for diagnostics. */
final class AgentPromptBuilderDebugAccessor {
    private AgentPromptBuilderDebugAccessor() {
    }

    static String buildPrompt(String requestText) {
        return AgentPromptBuilder.buildClassificationPrompt(requestText);
    }
}
