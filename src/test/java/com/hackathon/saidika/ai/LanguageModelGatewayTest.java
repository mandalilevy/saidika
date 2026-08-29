package com.hackathon.saidika.ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Pure unit test: the underlying {@link ChatModel} is mocked, so no Ollama instance is required. */
class LanguageModelGatewayTest {

    @Test
    void delegatesPromptToChatModelAndReturnsGeneratedText() {
        ChatModel chatModel = mock(ChatModel.class);
        when(chatModel.getOptions()).thenReturn(ChatOptions.builder().build());
        Generation generation = new Generation(new AssistantMessage("mocked response"));
        ChatResponse response = new ChatResponse(List.of(generation), ChatResponseMetadata.builder().build());
        when(chatModel.call(any(Prompt.class))).thenReturn(response);

        LanguageModelGateway gateway = new SpringAiLanguageModelGateway(chatModel);

        assertThat(gateway.complete("hello")).isEqualTo("mocked response");
    }
}
