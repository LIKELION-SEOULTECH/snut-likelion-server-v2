package com.snut_likelion.domain.ai.service;

import com.snut_likelion.domain.ai.dto.response.AiChatResult;
import com.snut_likelion.domain.ai.dto.response.AiSummarizeResult;
import com.snut_likelion.domain.ai.repository.AiChatRepository;
import com.snut_likelion.domain.ai.repository.AiSummaryRepository;
import com.snut_likelion.infra.ai.mapping.IntentAnswerResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiQueryService {

    private static final String FALLBACK_ANSWER = "\uC815\uD655\uD55C \uC815\uBCF4\uB97C \uCC3E\uAE30 \uC5B4\uB824\uC6CC\uC694. \uC778\uC2A4\uD0C0 DM\uC73C\uB85C \uBB38\uC758 \uC8FC\uC138\uC694 \uD83D\uDC49 instagram.com/likelion_st";

    private final AiChatRepository aiChatRepository;
    private final AiSummaryRepository aiSummaryRepository;
    private final IntentAnswerResolver intentAnswerResolver;

    public AiChatResult chat(String text) {
        AiChatRepository.ChatQueryResult chatResult = aiChatRepository.chat(text);

        String matchedQuestion = chatResult.matchedQuestion();
        String answer = intentAnswerResolver.findAnswer(matchedQuestion)
                .orElse(FALLBACK_ANSWER);

        return AiChatResult.builder()
                .answer(answer)
                .matchedQuestion(matchedQuestion)
                .score(chatResult.score())
                .build();
    }

    public AiSummarizeResult summarize(String text) {
        String summary = aiSummaryRepository.summarize(text);

        return AiSummarizeResult.builder()
                .summary(summary)
                .build();
    }
}