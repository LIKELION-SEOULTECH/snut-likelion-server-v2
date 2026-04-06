package com.snut_likelion.domain.blog.dto.res;

import com.snut_likelion.domain.blog.entity.BlogImage;
import com.snut_likelion.domain.blog.entity.BlogPost;
import com.snut_likelion.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

// 블로그 단건 조회 DTO
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogDetailResponse {

    private Long postId;
    private String title;
    private String contentHtml;
    private List<String> imageUrls;  // 응답은 항상 URL (key 아님)
    private LocalDateTime updatedAt;
    private String authorName;
    private List<String> taggedMemberNames;
    private String category;

    @Builder
    public BlogDetailResponse(Long postId, String title, String contentHtml,
                              List<String> imageUrls, LocalDateTime updatedAt,
                              String authorName, List<String> taggedMemberNames, String category) {
        this.postId = postId;
        this.title = title;
        this.contentHtml = contentHtml;
        this.imageUrls = imageUrls;
        this.updatedAt = updatedAt;
        this.authorName = authorName;
        this.taggedMemberNames = taggedMemberNames;
        this.category = category;
    }

    /**
     * @param keyToUrl storedFileName(key) → URL 변환 함수
     *                 ex) fileUploadService::buildFileUrl
     */
    public static BlogDetailResponse from(BlogPost post, Function<String, String> keyToUrl) {
        return BlogDetailResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .contentHtml(post.getContent())
                .imageUrls(post.getImages().stream()
                        .map(BlogImage::getStoredFileName)
                        .map(keyToUrl)           // key → URL 변환
                        .toList())
                .updatedAt(post.getUpdatedAt())
                .authorName(post.getAuthor().getUsername())
                .taggedMemberNames(post.getTaggedMembers().stream()
                        .map(User::getUsername)
                        .toList())
                .category(post.getCategory().name())
                .build();
    }
}
