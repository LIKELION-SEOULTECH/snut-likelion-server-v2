package com.snut_likelion.ai.service;

import com.snut_likelion.ai.dto.response.AiChatResult;
import com.snut_likelion.ai.dto.response.AiSummarizeResult;
import com.snut_likelion.ai.repository.AiChatRepository;
import com.snut_likelion.ai.repository.AiSummaryRepository;
import com.snut_likelion.ai.repository.IntentAnswerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiQueryService {

    private static final String FALLBACK_ANSWER = "정확한 정보를 찾기 어려워요. 인스타 DM으로 문의 주세요 👉 instagram.com/likelion_st";

    private final AiChatRepository aiChatRepository;
    private final AiSummaryRepository aiSummaryRepository;
    private final IntentAnswerPort intentAnswerPort;

    public AiChatResult chat(String text) {
        AiChatRepository.ChatQueryResult chatResult = aiChatRepository.chat(text);

        String matchedQuestion = chatResult.matchedQuestion();
        String answer = intentAnswerPort.findAnswer(matchedQuestion)
                .orElse(FALLBACK_ANSWER);

        return AiChatResult.of(answer, matchedQuestion, chatResult.score());
    }

    public AiSummarizeResult summarize(String text) {
        String summary = aiSummaryRepository.summarize(text);
        return AiSummarizeResult.of(summary);
    }
}
