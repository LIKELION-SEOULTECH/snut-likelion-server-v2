package com.snut_likelion.infra.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiServerChatResponse {

    private String response;

    @JsonProperty("matched_question")
    private String matchedQuestion;

    private Double score;

    public AiServerChatResponse(String response, String matchedQuestion, Double score) {
        this.response = response;
        this.matchedQuestion = matchedQuestion;
        this.score = score;
    }
}
