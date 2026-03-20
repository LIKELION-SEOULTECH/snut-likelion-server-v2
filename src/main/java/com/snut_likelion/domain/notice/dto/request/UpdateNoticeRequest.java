package com.snut_likelion.domain.notice.dto.request;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class UpdateNoticeRequest {
    private String title;
    private String content;
    private Boolean pinned;

    /** 새로 추가할 이미지 storedFileName 목록 */
    private List<String> imageStoredFileNames = new ArrayList<>();

    /** 새로 추가할 일반 파일 storedFileName 목록 */
    private List<String> fileStoredFileNames = new ArrayList<>();

    /** 삭제할 첨부파일 ID 목록 */
    private List<Long> attachmentIdsToDelete = new ArrayList<>();
}
