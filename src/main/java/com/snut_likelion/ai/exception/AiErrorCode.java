package com.snut_likelion.ai.exception;

import com.snut_likelion.global.error.BaseError;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AiErrorCode implements BaseError {
    AI_SUMMARIZE_CALL_FAILED("AI_SUMMARIZE_CALL_FAILED", "AI 요약 서버 호출에 실패했습니다."),
    AI_SUMMARIZE_EMPTY_RESPONSE("AI_SUMMARIZE_EMPTY_RESPONSE", "AI 요약 응답이 비어 있습니다.");

    private final String code;
    private final String message;
}
