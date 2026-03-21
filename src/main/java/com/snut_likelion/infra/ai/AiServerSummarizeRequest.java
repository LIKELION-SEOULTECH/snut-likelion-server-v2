package com.snut_likelion.infra.ai;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiServerSummarizeRequest {

    private String text;

    public AiServerSummarizeRequest(String text) {
        this.text = text;
    }
}
