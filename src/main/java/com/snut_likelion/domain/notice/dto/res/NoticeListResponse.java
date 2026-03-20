package com.snut_likelion.domain.notice.dto.res;

import com.snut_likelion.domain.notice.entity.Notice;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NoticeListResponse {

    private Long noticeId;
    private String title;
    private Boolean pinned;
    private LocalDateTime createdAt;  // 이걸 다시 추가해야 하나?
    private LocalDateTime updatedAt;

    public static NoticeListResponse from(Notice notice) {

        return NoticeListResponse.builder()
                .noticeId(notice.getId())
                .title(notice.getTitle())
                .pinned(notice.getPinned())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }
}