package com.snut_likelion.infra.ai.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiServerSummarizeResponse {

    private String summary;

    public AiServerSummarizeResponse(String summary) {
        this.summary = summary;
    }
}
