package com.hackathon.saidika.ai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Manual connectivity check against a real local Ollama instance running the configured model
 * (default: qwen3:4b). Deliberately named *IT* rather than *Test* so Surefire's default
 * inclusion pattern ("**Test", "**Tests", "**TestCase") skips it during ".\mvnw.cmd test".
 *
 * Run explicitly (with Ollama running) via:
 *   .\mvnw.cmd test -Dtest=OllamaLiveConnectivityIT
 */
@SpringBootTest
class OllamaLiveConnectivityIT {

    @Autowired
    private LanguageModelGateway languageModelGateway;

    @Test
    void completesAPromptAgainstTheRunningOllamaInstance() {
        String response = languageModelGateway.complete("Reply with the single word: OK");

        assertThat(response).isNotBlank();
    }
}
