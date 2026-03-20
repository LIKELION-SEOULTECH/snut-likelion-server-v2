package com.snut_likelion.domain.blog.dto.request;

import com.snut_likelion.domain.blog.entity.Category;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class UpdateBlogRequest {

    private String title;
    private String contentHtml;
    private Category category;

    /**
     * 새로 추가할 이미지의 storedFileName 목록
     * null이면 이미지 변경 없음, 빈 리스트면 전체 삭제
     */
    private List<String> newImageStoredFileNames;
    private List<Long> taggedMemberIds;

    @Builder
    public UpdateBlogRequest(String title, String contentHtml, Category category,
                             List<String> newImageStoredFileNames, List<Long> taggedMemberIds) {
        this.title = title;
        this.contentHtml = contentHtml;
        this.category = category;
        this.newImageStoredFileNames = newImageStoredFileNames;
        this.taggedMemberIds = taggedMemberIds;
    }
}
