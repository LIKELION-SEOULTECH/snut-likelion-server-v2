package com.snut_likelion.domain.notice.dto.res;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class NoticePageResponse {

    private List<NoticeListResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static NoticePageResponse of(List<NoticeListResponse> content, Page<?> page) {

        return NoticePageResponse.builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
