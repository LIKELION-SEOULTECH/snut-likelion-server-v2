package com.snut_likelion.infra.ai.repository;

import com.snut_likelion.ai.exception.AiErrorCode;
import com.snut_likelion.ai.exception.AiException;
import com.snut_likelion.infra.ai.AiServerSummarizeRequest;
import com.snut_likelion.infra.ai.AiServerSummarizeResponse;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiSummaryRepositoryImplTest {

    @Mock
    private SummaryFeignClient summaryFeignClient;

    @InjectMocks
    private AiSummaryRepositoryImpl aiSummaryRepository;

    @Test
    void summarize_whenResponseIsValid_returnsSummary() {
        // Given
        when(summaryFeignClient.summarize(any(AiServerSummarizeRequest.class)))
                .thenReturn(new AiServerSummarizeResponse("summary text"));

        // When
        String summary = aiSummaryRepository.summarize("source text");

        // Then
        assertThat(summary).isEqualTo("summary text");
    }

    @Test
    void summarize_whenResponseIsNull_throwsAiExceptionWithEmptyResponseCode() {
        // Given
        when(summaryFeignClient.summarize(any(AiServerSummarizeRequest.class))).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> aiSummaryRepository.summarize("source text"))
                .isInstanceOfSatisfying(AiException.class, ex -> {
                    assertThat(ex.getError()).isEqualTo(AiErrorCode.AI_SUMMARIZE_EMPTY_RESPONSE);
                    assertThat(ex.getMessage()).isEqualTo(AiErrorCode.AI_SUMMARIZE_EMPTY_RESPONSE.getMessage());
                });
    }

    @Test
    void summarize_whenSummaryFieldIsNull_throwsAiExceptionWithEmptyResponseCode() {
        // Given
        when(summaryFeignClient.summarize(any(AiServerSummarizeRequest.class)))
                .thenReturn(new AiServerSummarizeResponse(null));

        // When & Then
        assertThatThrownBy(() -> aiSummaryRepository.summarize("source text"))
                .isInstanceOfSatisfying(AiException.class, ex -> {
                    assertThat(ex.getError()).isEqualTo(AiErrorCode.AI_SUMMARIZE_EMPTY_RESPONSE);
                    assertThat(ex.getMessage()).isEqualTo(AiErrorCode.AI_SUMMARIZE_EMPTY_RESPONSE.getMessage());
                });
    }

    @Test
    void summarize_whenRetryableExceptionOccurs_throwsAiExceptionWithCallFailedCode() {
        // Given
        when(summaryFeignClient.summarize(any(AiServerSummarizeRequest.class)))
                .thenThrow(new RetryableException(
                        503,
                        "timeout",
                        Request.HttpMethod.POST,
                        new RuntimeException("connect timeout"),
                        (Long) null,
                        request()
                ));

        // When & Then
        assertThatThrownBy(() -> aiSummaryRepository.summarize("source text"))
                .isInstanceOfSatisfying(AiException.class, ex -> {
                    assertThat(ex.getError()).isEqualTo(AiErrorCode.AI_SUMMARIZE_CALL_FAILED);
                    assertThat(ex.getMessage()).isEqualTo(AiErrorCode.AI_SUMMARIZE_CALL_FAILED.getMessage());
                });
    }

    @Test
    void summarize_whenFeignExceptionOccurs_throwsAiExceptionWithCallFailedCode() {
        // Given
        FeignException feignException = FeignException.errorStatus("summarize", response(500));
        when(summaryFeignClient.summarize(any(AiServerSummarizeRequest.class))).thenThrow(feignException);

        // When & Then
        assertThatThrownBy(() -> aiSummaryRepository.summarize("source text"))
                .isInstanceOfSatisfying(AiException.class, ex -> {
                    assertThat(ex.getError()).isEqualTo(AiErrorCode.AI_SUMMARIZE_CALL_FAILED);
                    assertThat(ex.getMessage()).isEqualTo(AiErrorCode.AI_SUMMARIZE_CALL_FAILED.getMessage());
                });
    }

    @Test
    void summarize_whenDecodeExceptionOccurs_throwsAiExceptionWithCallFailedCode() {
        // Given
        DecodeException decodeException = new DecodeException(
                500,
                "decode failed",
                request(),
                new RuntimeException("json parse failed")
        );
        when(summaryFeignClient.summarize(any(AiServerSummarizeRequest.class))).thenThrow(decodeException);

        // When & Then
        assertThatThrownBy(() -> aiSummaryRepository.summarize("source text"))
                .isInstanceOfSatisfying(AiException.class, ex -> {
                    assertThat(ex.getError()).isEqualTo(AiErrorCode.AI_SUMMARIZE_CALL_FAILED);
                    assertThat(ex.getMessage()).isEqualTo(AiErrorCode.AI_SUMMARIZE_CALL_FAILED.getMessage());
                });
    }

    @Test
    void summarize_whenEncodeExceptionOccurs_throwsAiExceptionWithCallFailedCode() {
        // Given
        EncodeException encodeException = new EncodeException(
                "encode failed",
                new RuntimeException("json write failed")
        );
        when(summaryFeignClient.summarize(any(AiServerSummarizeRequest.class))).thenThrow(encodeException);

        // When & Then
        assertThatThrownBy(() -> aiSummaryRepository.summarize("source text"))
                .isInstanceOfSatisfying(AiException.class, ex -> {
                    assertThat(ex.getError()).isEqualTo(AiErrorCode.AI_SUMMARIZE_CALL_FAILED);
                    assertThat(ex.getMessage()).isEqualTo(AiErrorCode.AI_SUMMARIZE_CALL_FAILED.getMessage());
                });
    }

    private Request request() {
        return Request.create(
                Request.HttpMethod.POST,
                "http://localhost:8081/summarize",
                Map.of(),
                new byte[0],
                StandardCharsets.UTF_8
        );
    }

    private Response response(int status) {
        return Response.builder()
                .status(status)
                .reason("error")
                .request(request())
                .headers(Map.of())
                .build();
    }
}
