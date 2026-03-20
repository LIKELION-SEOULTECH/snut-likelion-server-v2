package com.snut_likelion.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snut_likelion.ai.dto.res.AiChatResult;
import com.snut_likelion.ai.dto.res.AiSummarizeResult;
import com.snut_likelion.ai.service.AiQueryService;
import com.snut_likelion.global.auth.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiController.class)
@AutoConfigureMockMvc(addFilters = false)
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiQueryService aiQueryService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @Test
    void chat_whenValidRequest_returnsSuccessResponse() throws Exception {
        // Given
        AiChatResult result = AiChatResult.of("mapped answer", "intent-key", 0.95);
        when(aiQueryService.chat(anyString())).thenReturn(result);

        String requestBody = objectMapper.writeValueAsString(new TestTextRequest("question"));

        // When & Then
        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.answer").value("mapped answer"))
                .andExpect(jsonPath("$.data.matchedQuestion").value("intent-key"))
                .andExpect(jsonPath("$.data.score").value(0.95));
    }

    @Test
    void summarize_whenValidRequest_returnsSuccessResponse() throws Exception {
        // Given
        AiSummarizeResult result = AiSummarizeResult.of("summary text");
        when(aiQueryService.summarize(anyString())).thenReturn(result);

        String requestBody = objectMapper.writeValueAsString(new TestTextRequest("long text"));

        // When & Then
        mockMvc.perform(post("/api/v1/ai/summarize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.summary").value("summary text"));
    }

    @Test
    void chat_whenTextIsBlank_returnsBadRequest() throws Exception {
        // Given
        String requestBody = objectMapper.writeValueAsString(new TestTextRequest(""));

        // When & Then
        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void summarize_whenTextIsBlank_returnsBadRequest() throws Exception {
        // Given
        String requestBody = objectMapper.writeValueAsString(new TestTextRequest(""));

        // When & Then
        mockMvc.perform(post("/api/v1/ai/summarize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    private record TestTextRequest(String text) {
    }
}
