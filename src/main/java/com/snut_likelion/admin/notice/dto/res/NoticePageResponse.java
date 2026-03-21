package com.snut_likelion.admin.notice.dto.res;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticePageResponse {

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private List<NoticeListResponse> content;

    @Builder
    private NoticePageResponse(
            int page, int size, long totalElements, int totalPages,
            List<NoticeListResponse> content
    ) {
        this.page          = page;
        this.size          = size;
        this.totalElements = totalElements;
        this.totalPages    = totalPages;
        this.content       = content;
    }

    // Page<Entity> -> DTO 로 변환
    public static NoticePageResponse from(Page<NoticeListResponse> page) {
        return NoticePageResponse.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .content(page.getContent())
                .build();
    }

    @Getter
    @NoArgsConstructor
    public static class NoticeListResponse {

        private Long noticeId;
        private String title;
        private boolean pinned;
        private LocalDateTime updatedAt;

        @Builder
        public NoticeListResponse(
                Long noticeId, String title, boolean pinned,
                LocalDateTime updatedAt
        ) {
            this.noticeId  = noticeId;
            this.title     = title;
            this.pinned    = pinned;
            this.updatedAt = updatedAt;
        }
    }
}
