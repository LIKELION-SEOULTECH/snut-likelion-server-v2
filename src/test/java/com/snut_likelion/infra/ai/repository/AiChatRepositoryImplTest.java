package com.snut_likelion.infra.ai.repository;

import com.snut_likelion.domain.ai.repository.AiChatRepository;
import com.snut_likelion.infra.ai.client.ChatFeignClient;
import com.snut_likelion.infra.ai.dto.request.AiServerChatRequest;
import com.snut_likelion.infra.ai.dto.response.AiServerChatResponse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiChatRepositoryImplTest {

    @Mock
    private ChatFeignClient chatFeignClient;

    @InjectMocks
    private AiChatRepositoryImpl aiChatRepository;

    @Test
    void chat_whenResponseIsValid_returnsMatchedQuestionAndScore() {
        // Given
        when(chatFeignClient.chat(any(AiServerChatRequest.class)))
                .thenReturn(new AiServerChatResponse("any", "intent-key", 0.88));

        // When
        AiChatRepository.ChatQueryResult result = aiChatRepository.chat("question");

        // Then
        assertThat(result.matchedQuestion()).isEqualTo("intent-key");
        assertThat(result.score()).isEqualTo(0.88);
    }

    @Test
    void chat_whenResponseIsNull_returnsNullResultForFallback() {
        // Given
        when(chatFeignClient.chat(any(AiServerChatRequest.class))).thenReturn(null);

        // When
        AiChatRepository.ChatQueryResult result = aiChatRepository.chat("question");

        // Then
        assertThat(result.matchedQuestion()).isNull();
        assertThat(result.score()).isNull();
    }

    @Test
    void chat_whenRetryableExceptionOccurs_returnsNullResultForFallback() {
        // Given
        when(chatFeignClient.chat(any(AiServerChatRequest.class)))
                .thenThrow(new RetryableException(
                        503,
                        "timeout",
                        Request.HttpMethod.POST,
                        new RuntimeException("connect timeout"),
                        (Long) null,
                        request()
                ));

        // When
        AiChatRepository.ChatQueryResult result = aiChatRepository.chat("question");

        // Then
        assertThat(result.matchedQuestion()).isNull();
        assertThat(result.score()).isNull();
    }

    @Test
    void chat_whenFeignExceptionOccurs_returnsNullResultForFallback() {
        // Given
        FeignException feignException = FeignException.errorStatus("chat", response(500));
        when(chatFeignClient.chat(any(AiServerChatRequest.class))).thenThrow(feignException);

        // When
        AiChatRepository.ChatQueryResult result = aiChatRepository.chat("question");

        // Then
        assertThat(result.matchedQuestion()).isNull();
        assertThat(result.score()).isNull();
    }

    @Test
    void chat_whenDecodeExceptionOccurs_returnsNullResultForFallback() {
        // Given
        DecodeException decodeException = new DecodeException(
                500,
                "decode failed",
                request(),
                new RuntimeException("json parse failed")
        );
        when(chatFeignClient.chat(any(AiServerChatRequest.class))).thenThrow(decodeException);

        // When
        AiChatRepository.ChatQueryResult result = aiChatRepository.chat("question");

        // Then
        assertThat(result.matchedQuestion()).isNull();
        assertThat(result.score()).isNull();
    }

    @Test
    void chat_whenEncodeExceptionOccurs_returnsNullResultForFallback() {
        // Given
        EncodeException encodeException = new EncodeException(
                "encode failed",
                new RuntimeException("json write failed")
        );
        when(chatFeignClient.chat(any(AiServerChatRequest.class))).thenThrow(encodeException);

        // When
        AiChatRepository.ChatQueryResult result = aiChatRepository.chat("question");

        // Then
        assertThat(result.matchedQuestion()).isNull();
        assertThat(result.score()).isNull();
    }

    private Request request() {
        return Request.create(
                Request.HttpMethod.POST,
                "http://localhost:8081/chat",
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