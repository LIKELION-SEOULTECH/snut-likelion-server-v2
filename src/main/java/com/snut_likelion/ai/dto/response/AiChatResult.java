package com.snut_likelion.ai.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiChatResult {

    private String answer;
    private String matchedQuestion;
    private Double score;

    private AiChatResult(String answer, String matchedQuestion, Double score) {
        this.answer = answer;
        this.matchedQuestion = matchedQuestion;
        this.score = score;
    }

    public static AiChatResult of(String answer, String matchedQuestion, Double score) {
        return new AiChatResult(answer, matchedQuestion, score);
    }
}
