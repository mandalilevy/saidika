package com.hackathon.saidika.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

/**
 * {@link LanguageModelGateway} backed by Spring AI's provider-agnostic {@link ChatModel}.
 * The concrete model (currently Ollama/qwen3) is selected entirely through application
 * configuration; this class contains no Ollama-specific code.
 */
@Service
public class SpringAiLanguageModelGateway implements LanguageModelGateway {

    private final ChatClient chatClient;

    public SpringAiLanguageModelGateway(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    @Override
    public String complete(String prompt) {
        return chatClient.prompt(prompt).call().content();
    }
}
