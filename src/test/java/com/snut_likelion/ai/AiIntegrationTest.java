package com.snut_likelion.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
@ActiveProfiles({"integration-test", "dev"})
@TestPropertySource(properties = {
        "ai-server.url=http://localhost:${wiremock.server.port}"
})
class AiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void resetWireMock() {
        WireMock.reset();
    }

    // ── /chat 정상 케이스 ──────────────────────────────────────────────────

    @Test
    @WithMockUser
    void chat_whenAiReturnsNullMatchedQuestion_returnsFallbackAnswer() throws Exception {
        stubFor(WireMock.post(urlEqualTo("/chat"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"response":"모르겠어요","matched_question":null,"score":0.1}
                                """)));

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TextRequest("아무 말"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.answer").value(containsString("instagram.com/likelion_st")));
    }

    @Test
    @WithMockUser
    void chat_whenAiReturnsUnknownIntent_returnsFallbackAnswer() throws Exception {
        stubFor(WireMock.post(urlEqualTo("/chat"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"response":"raw","matched_question":"unknown-intent-xyz","score":0.55}
                                """)));

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TextRequest("알 수 없는 질문"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.answer").value(containsString("instagram.com/likelion_st")));
    }

    @Test
    @WithMockUser
    void chat_whenAiServerReturns500_returnsFallbackAnswer() throws Exception {
        stubFor(WireMock.post(urlEqualTo("/chat"))
                .willReturn(aResponse().withStatus(500)));

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TextRequest("질문"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.answer").value(containsString("instagram.com/likelion_st")));
    }

    @Test
    @WithMockUser
    void chat_whenAiServerReturns503_returnsFallbackAnswer() throws Exception {
        stubFor(WireMock.post(urlEqualTo("/chat"))
                .willReturn(aResponse().withStatus(503)));

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TextRequest("질문"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.answer").value(containsString("instagram.com/likelion_st")));
    }

    @Test
    @WithMockUser
    void chat_whenAiReturnsEmptyBody_returnsFallbackAnswer() throws Exception {
        stubFor(WireMock.post(urlEqualTo("/chat"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TextRequest("질문"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.answer").value(containsString("instagram.com/likelion_st")));
    }

    // ── /chat 유효성 검사 ──────────────────────────────────────────────────

    @Test
    @WithMockUser
    void chat_whenTextIsBlank_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TextRequest(""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    @WithMockUser
    void chat_whenTextIsNull_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    // ── /summarize 정상 케이스 ─────────────────────────────────────────────

    @Test
    @WithMockUser
    void summarize_whenAiReturnsValidSummary_returnsSummary() throws Exception {
        stubFor(WireMock.post(urlEqualTo("/summarize"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"summary":"이것은 요약된 내용입니다."}
                                """)));

        mockMvc.perform(post("/api/v1/ai/summarize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TextRequest("긴 공지사항 내용..."))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.summary").value("이것은 요약된 내용입니다."));
    }

    // ── /summarize 오류 케이스 (503 반환) ─────────────────────────────────

    @Test
    @WithMockUser
    void summarize_whenAiServerReturns500_returns503WithCallFailedCode() throws Exception {
        stubFor(WireMock.post(urlEqualTo("/summarize"))
                .willReturn(aResponse().withStatus(500)));

        mockMvc.perform(post("/api/v1/ai/summarize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TextRequest("공지 내용"))))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("AI_SUMMARIZE_CALL_FAILED"));
    }

    @Test
    @WithMockUser
    void summarize_whenAiServerReturnsNullSummary_returns503WithEmptyResponseCode() throws Exception {
        stubFor(WireMock.post(urlEqualTo("/summarize"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));

        mockMvc.perform(post("/api/v1/ai/summarize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TextRequest("공지 내용"))))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("AI_SUMMARIZE_EMPTY_RESPONSE"));
    }

    @Test
    @WithMockUser
    void summarize_whenAiServerReturns503_returns503WithCallFailedCode() throws Exception {
        stubFor(WireMock.post(urlEqualTo("/summarize"))
                .willReturn(aResponse().withStatus(503)));

        mockMvc.perform(post("/api/v1/ai/summarize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TextRequest("공지 내용"))))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("AI_SUMMARIZE_CALL_FAILED"));
    }

    // ── /summarize 유효성 검사 ─────────────────────────────────────────────

    @Test
    @WithMockUser
    void summarize_whenTextIsBlank_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/ai/summarize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TextRequest(""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    // ── 인증 검사 ──────────────────────────────────────────────────────────

    @Test
    void chat_whenNotAuthenticated_returns200() throws Exception {
        stubFor(WireMock.post(urlEqualTo("/chat"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"matched_question\": null, \"score\": null}")));

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TextRequest("질문"))))
                .andExpect(status().isOk());
    }

    @Test
    void summarize_whenNotAuthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/ai/summarize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TextRequest("내용"))))
                .andExpect(status().isUnauthorized());
    }

    private record TextRequest(String text) {}
}
