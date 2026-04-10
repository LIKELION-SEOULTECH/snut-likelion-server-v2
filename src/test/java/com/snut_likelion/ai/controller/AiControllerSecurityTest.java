package com.snut_likelion.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snut_likelion.ai.dto.res.AiChatResult;
import com.snut_likelion.ai.service.AiQueryService;
import com.snut_likelion.global.auth.jwt.JwtService;
import com.snut_likelion.global.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiController.class)
@Import(SecurityConfig.class)
class AiControllerSecurityTest {

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
    void chat_withoutAuthentication_returnsOk() throws Exception {
        // Given
        AiChatResult result = AiChatResult.of("answer", "intent-key", 0.9);
        when(aiQueryService.chat(anyString())).thenReturn(result);

        String requestBody = objectMapper.writeValueAsString(new TestTextRequest("질문"));

        // When & Then — 비로그인(토큰 없음)에서도 200 응답이어야 한다
        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"));
    }

    private record TestTextRequest(String text) {
    }
}
