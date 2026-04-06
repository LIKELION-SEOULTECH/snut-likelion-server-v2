package com.snut_likelion.domain.notice.dto.req;


import com.snut_likelion.domain.notice.entity.Notice;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CreateNoticeRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private Boolean pinned = false;  // 기본값 false

    /** 이미지 첨부 storedFileName 목록 (upload-complete 완료 후 받은 S3 key) */
    private List<String> imageStoredFileNames = new ArrayList<>();

    /** 일반 파일 첨부 storedFileName 목록 (upload-complete 완료 후 받은 S3 key) */
    private List<String> fileStoredFileNames = new ArrayList<>();

    public Notice toEntity() {
        return Notice.builder()
                .title(title)
                .content(content)
                .pinned(pinned)
                .build();
    }
}