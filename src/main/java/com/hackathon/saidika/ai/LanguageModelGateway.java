package com.hackathon.saidika.ai;

/**
 * Minimal seam between Saidika and whichever language model provider is configured.
 * Future agent/tool-calling code should depend on this abstraction rather than on
 * Ollama- or Spring AI-specific types, so the provider can be swapped without
 * rewriting business logic.
 */
public interface LanguageModelGateway {

    String complete(String prompt);
}
