package com.snut_likelion.domain.ai.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiSummarizeResult {

    private String summary;

    @Builder
    public AiSummarizeResult(String summary) {
        this.summary = summary;
    }
}
