package com.snut_likelion.domain.blog.dto.res;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class UploadBlogImageResponse {

    // 업로드된 이미지 URL 목록
    private final List<String> urls;

    public static UploadBlogImageResponse from(List<String> urls) {
        return new UploadBlogImageResponse(urls);
    }
}
