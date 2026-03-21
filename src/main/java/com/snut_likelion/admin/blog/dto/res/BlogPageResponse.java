package com.snut_likelion.admin.blog.dto.res;

import com.snut_likelion.domain.blog.entity.Category;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogPageResponse {

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private List<BlogListResponse> content;

    @Builder
    private BlogPageResponse(
            int page, int size, long totalElements, int totalPages,
            List<BlogListResponse> content
    ) {
        this.page          = page;
        this.size          = size;
        this.totalElements = totalElements;
        this.totalPages    = totalPages;
        this.content       = content;
    }

    public static BlogPageResponse from(Page<BlogListResponse> page) {
        return BlogPageResponse.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .content(page.getContent())
                .build();
    }

    @Getter
    @NoArgsConstructor
    public static class BlogListResponse {

        private Long id;
        private String title;
        private String category;
        private String author;
        private LocalDateTime createdAt;

        @Builder
        public BlogListResponse(
                Long id, String title, Category category,
                String author, LocalDateTime createdAt
        ) {
            this.id        = id;
            this.title     = title;
            this.category  = category.name();
            this.author    = author;
            this.createdAt = createdAt;
        }
    }
}
