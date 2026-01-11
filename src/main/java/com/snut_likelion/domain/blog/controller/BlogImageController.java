package com.snut_likelion.domain.blog.controller;

import com.snut_likelion.domain.blog.dto.response.UploadBlogImageResponse;
import com.snut_likelion.domain.blog.service.BlogImageCommandService;
import com.snut_likelion.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Blog Image", description = "블로그 이미지 관리 API")
@RestController
@RequestMapping("/api/v1/blogs/images")
@RequiredArgsConstructor
public class BlogImageController {

    private final BlogImageCommandService imgCmdService;

    @Operation(summary = "블로그 이미지 업로드", description = "여러 장의 이미지를 업로드하고 S3 URL 리스트를 반환합니다. (USER, MANAGER 권한 필요)")
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MANAGER')")
    public ApiResponse<UploadBlogImageResponse> uploadImages(
            @RequestPart("files") @NotNull List<MultipartFile> files) {

        List<String> urls = imgCmdService.upload(files);
        return ApiResponse.success(UploadBlogImageResponse.from(urls));
    }
}