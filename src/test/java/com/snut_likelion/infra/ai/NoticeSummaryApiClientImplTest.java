package com.snut_likelion.infra.ai;

import com.snut_likelion.infra.ai.client.SummaryFeignClient;
import feign.FeignException;
import feign.Request;
import feign.Response;
import feign.RetryableException;
import feign.codec.DecodeException;
import feign.codec.EncodeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticeSummaryApiClientImplTest {

    private static final String FALLBACK_SUMMARY = "요약이 제공되지 않았습니다.";

    @Mock
    private SummaryFeignClient summaryFeignClient;

    @InjectMocks
    private NoticeSummaryApiClientImpl noticeSummaryApiClient;

    // ── 정상 케이스 ──────────────────────────────────────────────────────────

    @Test
    void summarize_whenValidResponse_returnsSummary() {
        // Given
        when(summaryFeignClient.summarize(any(AiServerSummarizeRequest.class)))
                .thenReturn(new AiServerSummarizeResponse("공지 요약 결과입니다."));

        // When
        String result = noticeSummaryApiClient.summarize("공지 내용");

        // Then
        assertThat(result).isEqualTo("공지 요약 결과입니다.");
    }

    // ── 폴백 케이스 (절대 예외를 던지지 않아야 함) ────────────────────────────

    @Test
    void summarize_whenResponseIsNull_returnsFallback() {
        // Given
        when(summaryFeignClient.summarize(any(AiServerSummarizeRequest.class)))
                .thenReturn(null);

        // When
        String result = noticeSummaryApiClient.summarize("공지 내용");

        // Then
        assertThat(result).isEqualTo(FALLBACK_SUMMARY);
    }

    @Test
    void summarize_whenSummaryFieldIsNull_returnsFallback() {
        // Given
        when(summaryFeignClient.summarize(any(AiServerSummarizeRequest.class)))
                .thenReturn(new AiServerSummarizeResponse(null));

        // When
        String result = noticeSummaryApiClient.summarize("공지 내용");

        // Then
        assertThat(result).isEqualTo(FALLBACK_SUMMARY);
    }

    @Test
    void summarize_whenRetryableExceptionOccurs_returnsFallbackWithoutThrowing() {
        // Given
        when(summaryFeignClient.summarize(any(AiServerSummarizeRequest.class)))
                .thenThrow(new RetryableException(
                        503, "timeout", Request.HttpMethod.POST,
                        new RuntimeException("connect timeout"), (Long) null, request()
                ));

        // When & Then
        assertThatCode(() -> {
            String result = noticeSummaryApiClient.summarize("공지 내용");
            assertThat(result).isEqualTo(FALLBACK_SUMMARY);
        }).doesNotThrowAnyException();
    }

    @Test
    void summarize_whenFeignExceptionWith500_returnsFallbackWithoutThrowing() {
        // Given
        FeignException feignException = FeignException.errorStatus("summarize", response(500));
        when(summaryFeignClient.summarize(any(AiServerSummarizeRequest.class)))
                .thenThrow(feignException);

        // When & Then
        assertThatCode(() -> {
            String result = noticeSummaryApiClient.summarize("공지 내용");
            assertThat(result).isEqualTo(FALLBACK_SUMMARY);
        }).doesNotThrowAnyException();
    }

    @Test
    void summarize_whenFeignExceptionWith404_returnsFallbackWithoutThrowing() {
        // Given
        FeignException feignException = FeignException.errorStatus("summarize", response(404));
        when(summaryFeignClient.summarize(any(AiServerSummarizeRequest.class)))
                .thenThrow(feignException);

        // When & Then
        assertThatCode(() -> {
            String result = noticeSummaryApiClient.summarize("공지 내용");
            assertThat(result).isEqualTo(FALLBACK_SUMMARY);
        }).doesNotThrowAnyException();
    }

    @Test
    void summarize_whenDecodeExceptionOccurs_returnsFallbackWithoutThrowing() {
        // Given
        DecodeException decodeException = new DecodeException(
                500, "decode failed", request(), new RuntimeException("json parse failed")
        );
        when(summaryFeignClient.summarize(any(AiServerSummarizeRequest.class)))
                .thenThrow(decodeException);

        // When & Then
        assertThatCode(() -> {
            String result = noticeSummaryApiClient.summarize("공지 내용");
            assertThat(result).isEqualTo(FALLBACK_SUMMARY);
        }).doesNotThrowAnyException();
    }

    @Test
    void summarize_whenEncodeExceptionOccurs_returnsFallbackWithoutThrowing() {
        // Given
        EncodeException encodeException = new EncodeException(
                "encode failed", new RuntimeException("json write failed")
        );
        when(summaryFeignClient.summarize(any(AiServerSummarizeRequest.class)))
                .thenThrow(encodeException);

        // When & Then
        assertThatCode(() -> {
            String result = noticeSummaryApiClient.summarize("공지 내용");
            assertThat(result).isEqualTo(FALLBACK_SUMMARY);
        }).doesNotThrowAnyException();
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private Request request() {
        return Request.create(
                Request.HttpMethod.POST, "http://localhost:8081/summarize",
                Map.of(), new byte[0], StandardCharsets.UTF_8
        );
    }

    private Response response(int status) {
        return Response.builder()
                .status(status).reason("error")
                .request(request()).headers(Map.of())
                .build();
    }
}
