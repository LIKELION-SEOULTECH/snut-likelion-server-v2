package com.snut_likelion.infra.file;

import com.snut_likelion.global.error.BaseError;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileErrorCode implements BaseError {

    BAD_FILE_URL("BAD_REQUEST", "잘못된 파일 URL입니다."),
    FILE_NOT_FOUND("NOT_FOUND", "파일을 찾을 수 없습니다"),
    INVALID_IMAGE_URL("BAD_REQUEST", "이미지 URL이 유효하지 않습니다."),
    S3_SERVICE_ERROR("SERVICE_UNAVAILABLE", "S3 서비스가 응답 불가 상태입니다."),
    FILE_UPLOAD_FAILED("INTERNAL_SERVER_ERROR", "파일 업로드에 실패했습니다."),
    IO_ERROR("INTERNAL_SERVER_ERROR", "파일 처리 실패"),
    INVALID_FILE_KEY("BAD_REQUEST", "잘못된 파일 Key(storedFileName)입니다.");

    private final String code;
    private final String message;
}
