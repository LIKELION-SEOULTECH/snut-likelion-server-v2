package com.snut_likelion.ai.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiChatRequest {

    @NotBlank(message = "질문을 입력해주세요.")
    private String text;
}
