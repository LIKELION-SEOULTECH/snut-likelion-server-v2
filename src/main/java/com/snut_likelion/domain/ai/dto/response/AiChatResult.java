package com.snut_likelion.domain.ai.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiChatResult {

    private String answer;
    private String matchedQuestion;
    private Double score;

    @Builder
    public AiChatResult(String answer, String matchedQuestion, Double score) {
        this.answer = answer;
        this.matchedQuestion = matchedQuestion;
        this.score = score;
    }
}
