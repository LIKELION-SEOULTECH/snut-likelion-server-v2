package com.snut_likelion.admin.file.controller;

import com.snut_likelion.admin.file.dto.IssuePresignedUrlRequest;
import com.snut_likelion.admin.file.dto.PresignedUrlResponse;
import com.snut_likelion.admin.file.service.AdminPresignedUrlCommandService;
import com.snut_likelion.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin File", description = "관리자 파일 업로드 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/files")
@PreAuthorize("hasRole('ROLE_MANAGER')")  // 관리자만 사용 가능
public class AdminPresignedUrlController {

    private final AdminPresignedUrlCommandService adminPresignedUrlCommandService;

    @Operation(summary = "Presigned URL 발급", description = "\"S3(또는 로컬)에 이미지를 직접 업로드(PUT)하기 위한 URL을 발급받습니다.")
    @PostMapping("/presigned-url")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PresignedUrlResponse> issuePresignedUrl(@Valid @RequestBody IssuePresignedUrlRequest request) {
        PresignedUrlResponse response = adminPresignedUrlCommandService.issueImageUploadUrl(request);
        return ApiResponse.success(response);
    }
}
