package com.snut_likelion.infra.ai.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiServerChatRequest {

    private String text;

    public AiServerChatRequest(String text) {
        this.text = text;
    }
}
