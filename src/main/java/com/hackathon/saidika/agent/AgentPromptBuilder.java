package com.hackathon.saidika.agent;

import com.hackathon.saidika.domain.ServiceType;

import java.util.Arrays;
import java.util.stream.Collectors;

/** Builds the strict-JSON classification prompt sent to Qwen3 via {@code LanguageModelGateway}. */
final class AgentPromptBuilder {

    private AgentPromptBuilder() {
    }

    static String buildClassificationPrompt(String requestText) {
        String validTypes = Arrays.stream(ServiceType.values()).map(Enum::name).collect(Collectors.joining(", "));
        String sanitizedRequest = requestText.replace("\"", "'").replace("\n", " ").trim();

        return """
                You are Saidika, a roadside-assistance triage agent. Read the driver's message below and \
                decide which single roadside assistance service applies.

                Valid service types: %s.

                Rules:
                - Consider the driver's stated priority: if they say one issue is bigger/more urgent/the \
                real problem, prefer that one over an issue merely mentioned first or in passing.
                - If genuinely no single service is clearly indicated, or the request is outside roadside \
                vehicle assistance, set "service_type" to "UNKNOWN" and "ambiguous" to true.
                - You are only classifying the request here: never invent providers, phone numbers, \
                addresses or websites, and never claim any verification.
                - Do not show your reasoning steps or any chain-of-thought. Respond with ONLY one JSON \
                object and nothing else: no markdown fences, no prose before or after it.

                Respond with exactly this JSON shape:
                {"service_type": "<one of: %s, or UNKNOWN>", "ambiguous": <true or false>, "explanation": "<one concise sentence>"}

                Driver's message: "%s"
                """.formatted(validTypes, validTypes, sanitizedRequest);
    }
}
