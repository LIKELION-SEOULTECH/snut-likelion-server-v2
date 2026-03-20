package com.snut_likelion.domain.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiSummarizeRequest {

    @NotBlank(message = "요약할 내용을 입력해주세요.")
    private String text;
}