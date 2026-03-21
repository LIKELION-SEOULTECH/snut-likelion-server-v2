package com.snut_likelion.domain.blog.dto.res;

import com.snut_likelion.domain.blog.entity.BlogPost;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogSummaryPageResponse {
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private List<BlogSummaryResponse> content;

    @Builder
    public BlogSummaryPageResponse(int page, int size, long totalElements, int totalPages, List<BlogSummaryResponse> content) {
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.content = content;
    }

    /**
     * @param keyToUrl thumbnailKey(S3 key) → URL 변환 함수
     *                 ex) fileUploadService::buildFileUrl
     */
    public static BlogSummaryPageResponse from(Page<BlogPost> page, Function<String, String> keyToUrl) {
        return BlogSummaryPageResponse.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .content(page.getContent().stream()
                        .map(post -> BlogSummaryResponse.from(post, keyToUrl))
                        .toList())
                .build();
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class BlogSummaryResponse {

        private Long postId;
        private String title;
        private LocalDateTime updatedAt;
        private String thumbnailUrl;

        @Builder
        public BlogSummaryResponse(Long postId, String title, LocalDateTime updatedAt, String thumbnailUrl) {
            this.postId = postId;
            this.title = title;
            this.updatedAt = updatedAt;
            this.thumbnailUrl = thumbnailUrl;
        }

        /**
         * @param keyToUrl thumbnailKey → URL 변환 함수
         */
        public static BlogSummaryResponse from(BlogPost blogPost, Function<String, String> keyToUrl) {
            String thumbnailUrl = blogPost.getThumbnailKey() != null
                    ? keyToUrl.apply(blogPost.getThumbnailKey())
                    : null;

            return BlogSummaryResponse.builder()
                    .postId(blogPost.getId())
                    .title(blogPost.getTitle())
                    .updatedAt(blogPost.getUpdatedAt())
                    .thumbnailUrl(thumbnailUrl)
                    .build();
        }
    }
}
