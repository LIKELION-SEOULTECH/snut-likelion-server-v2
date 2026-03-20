package com.snut_likelion.domain.blog.entity;

import com.snut_likelion.global.common.support.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "blog_image")
public class BlogImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id")
    private BlogPost post;

    // S3 key 저장 (URL 아님)
    // 조회 시 FileUploadService.buildFileUrl()로 URL 변환한다.
    @Column(nullable = false, length = 500)
    private String storedFileName;

    @Builder
    public BlogImage(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public static List<BlogImage> listOf(List<String> storedFileNames) {
        if (storedFileNames == null || storedFileNames.isEmpty()) return List.of();
        return storedFileNames.stream()
                .map(key -> BlogImage.builder().storedFileName(key).build())
                .toList();
    }

    public void setPost(BlogPost post) { this.post = post; }
}