package com.hackathon.saidika.web;

import com.hackathon.saidika.agent.AgentAssistanceResultTestSupport;
import com.hackathon.saidika.agent.RoadsideAssistanceAgent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Web-layer tests with {@link RoadsideAssistanceAgent} mocked, so they need no Ollama and no network,
 * keeping ".\mvnw.cmd test" deterministic while still covering error handling at the controller boundary.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoadsideAssistanceAgent agent;

    @Test
    void emptyRequestTextIsRejectedByValidationBeforeReachingTheAgent() throws Exception {
        mockMvc.perform(post("/agent/assist")
                        .contentType("application/x-www-form-urlencoded")
                        .param("requestText", "")
                        .param("latitude", "-1.28")
                        .param("longitude", "36.81"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void missingCoordinatesProduceAFriendlyError() throws Exception {
        mockMvc.perform(post("/agent/assist")
                        .contentType("application/x-www-form-urlencoded")
                        .param("requestText", "My car needs to be transported to a garage.")
                        .param("latitude", "")
                        .param("longitude", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void validRequestRendersAgentResultView() throws Exception {
        when(agent.assist(any())).thenReturn(AgentAssistanceResultTestSupport.unresolved("ambiguous"));

        mockMvc.perform(post("/agent/assist")
                        .contentType("application/x-www-form-urlencoded")
                        .param("requestText", "The vehicle stopped suddenly.")
                        .param("latitude", "-1.28")
                        .param("longitude", "36.81"))
                .andExpect(status().isOk())
                .andExpect(view().name("agent-result"))
                .andExpect(model().attributeExists("agentResult"));
    }
}
