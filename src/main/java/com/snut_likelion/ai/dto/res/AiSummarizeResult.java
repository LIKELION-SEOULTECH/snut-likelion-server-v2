package com.snut_likelion.ai.dto.res;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiSummarizeResult {

    private String summary;

    private AiSummarizeResult(String summary) {
        this.summary = summary;
    }

    public static AiSummarizeResult of(String summary) {
        return new AiSummarizeResult(summary);
    }
}
