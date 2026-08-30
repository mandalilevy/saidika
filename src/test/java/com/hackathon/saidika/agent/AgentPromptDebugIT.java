package com.hackathon.saidika.agent;

import com.hackathon.saidika.ai.LanguageModelGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Temporary diagnostic: prints the raw Qwen3 completion for the real classification prompt so the raw
 * output format can be inspected. Named *IT* so it is excluded from ".\mvnw.cmd test".
 */
@SpringBootTest
class AgentPromptDebugIT {

    @Autowired
    private LanguageModelGateway gateway;

    @Test
    void printRawCompletionForTowingPrompt() {
        String prompt = AgentPromptBuilder.buildClassificationPrompt("My car needs to be transported to a garage.");
        String raw = gateway.complete(prompt);
        System.out.println("=====PROMPT=====");
        System.out.println(prompt);
        System.out.println("=====RAW OUTPUT=====");
        System.out.println(raw);
        System.out.println("=====END=====");
    }
}
