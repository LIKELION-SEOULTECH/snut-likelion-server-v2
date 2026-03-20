package com.snut_likelion.domain.notice.dto.res;

import com.snut_likelion.domain.notice.entity.NoticeAttachment;
import lombok.Builder;
import lombok.Getter;

/**
 * 공지사항 파일 첨부 응답 DTO
 * 이미지·일반 파일 공통으로 사용하며, 타입은 attachmentType 필드로 구분한다.
 */
@Getter
@Builder
public class NoticeFileDto {

    private Long attachmentId;
    private String originalFileName;
    private String fileUrl;

    public static NoticeFileDto of(NoticeAttachment attachment, String fileUrl) {
        return NoticeFileDto.builder()
                .attachmentId(attachment.getId())
                .originalFileName(attachment.getOriginalFileName())
                .fileUrl(fileUrl)
                .build();
    }
}
