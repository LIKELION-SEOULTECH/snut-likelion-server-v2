package com.snut_likelion.domain.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AiSummarizeRequest {

    @NotBlank
    private String text;
}