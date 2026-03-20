package com.snut_likelion.domain.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AiChatRequest {

    @NotBlank(message = "질문을 입력해주세요.")
    private String text;
}