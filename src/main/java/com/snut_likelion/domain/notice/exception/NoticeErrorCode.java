package com.snut_likelion.domain.notice.exception;


import com.snut_likelion.global.error.BaseError;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NoticeErrorCode implements BaseError {
    NOT_FOUND("NOTICE_NOT_FOUND", "공지사항을 찾을 수 없습니다."),
    ATTACHMENT_NOT_FOUND("NOTICE_ATTACHMENT_NOT_FOUND", "첨부파일을 찾을 수 없습니다.");

    private final String code;
    private final String message;
}
