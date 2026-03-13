package com.snut_likelion.domain.ai.service;

import com.snut_likelion.domain.ai.dto.response.AiChatResult;
import com.snut_likelion.domain.ai.dto.response.AiSummarizeResult;
import com.snut_likelion.domain.ai.exception.AiErrorCode;
import com.snut_likelion.domain.ai.exception.AiException;
import com.snut_likelion.domain.ai.repository.AiChatRepository;
import com.snut_likelion.domain.ai.repository.AiSummaryRepository;
import com.snut_likelion.infra.ai.mapping.IntentAnswerResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiQueryServiceTest {

    @Mock
    private AiChatRepository aiChatRepository;

    @Mock
    private AiSummaryRepository aiSummaryRepository;

    @Mock
    private IntentAnswerResolver intentAnswerResolver;

    @InjectMocks
    private AiQueryService aiQueryService;

    @Test
    void chat_whenChatSuccessAndIntentMapped_returnsMappedAnswer() {
        // Given
        String text = "question";
        String matchedQuestion = "intent-key";
        Double score = 0.91;

        when(aiChatRepository.chat(text)).thenReturn(new AiChatRepository.ChatQueryResult(matchedQuestion, score));
        when(intentAnswerResolver.findAnswer(matchedQuestion)).thenReturn(Optional.of("mapped answer"));

        // When
        AiChatResult result = aiQueryService.chat(text);

        // Then
        assertThat(result.getAnswer()).isEqualTo("mapped answer");
        assertThat(result.getMatchedQuestion()).isEqualTo(matchedQuestion);
        assertThat(result.getScore()).isEqualTo(score);
    }

    @Test
    void chat_whenMatchedQuestionIsNull_returnsFallbackAnswer() {
        // Given
        String text = "question";

        when(aiChatRepository.chat(text)).thenReturn(new AiChatRepository.ChatQueryResult(null, 0.2));
        when(intentAnswerResolver.findAnswer(null)).thenReturn(Optional.empty());

        // When
        AiChatResult result = aiQueryService.chat(text);

        // Then
        assertThat(result.getAnswer()).contains("instagram.com/likelion_st");
        assertThat(result.getMatchedQuestion()).isNull();
        assertThat(result.getScore()).isEqualTo(0.2);
    }

    @Test
    void chat_whenIntentMappingMissing_returnsFallbackAnswer() {
        // Given
        String text = "question";
        String matchedQuestion = "unknown-intent";

        when(aiChatRepository.chat(text)).thenReturn(new AiChatRepository.ChatQueryResult(matchedQuestion, 0.7));
        when(intentAnswerResolver.findAnswer(matchedQuestion)).thenReturn(Optional.empty());

        // When
        AiChatResult result = aiQueryService.chat(text);

        // Then
        assertThat(result.getAnswer()).contains("instagram.com/likelion_st");
        assertThat(result.getMatchedQuestion()).isEqualTo(matchedQuestion);
        assertThat(result.getScore()).isEqualTo(0.7);
    }

    @Test
    void summarize_whenSuccess_returnsSummary() {
        // Given
        String text = "source text";
        when(aiSummaryRepository.summarize(text)).thenReturn("summary text");

        // When
        AiSummarizeResult result = aiQueryService.summarize(text);

        // Then
        assertThat(result.getSummary()).isEqualTo("summary text");
    }

    @Test
    void summarize_whenRepositoryThrowsAiException_propagatesException() {
        // Given
        String text = "source text";
        when(aiSummaryRepository.summarize(text)).thenThrow(new AiException(AiErrorCode.AI_SUMMARIZE_CALL_FAILED));

        // When & Then
        assertThatThrownBy(() -> aiQueryService.summarize(text))
                .isInstanceOf(AiException.class)
                .hasMessage(AiErrorCode.AI_SUMMARIZE_CALL_FAILED.getMessage());
    }
}