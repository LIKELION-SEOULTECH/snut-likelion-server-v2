package com.snut_likelion.admin.file.service;

import com.snut_likelion.admin.file.dto.request.IssuePresignedUrlRequest;
import com.snut_likelion.admin.file.dto.response.PresignedUrlResponse;
import com.snut_likelion.domain.file.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Admin Presigned URL 발급 서비스
 *
 * 실제 로직은 FileUploadService에 위임한다.
 * 이 클래스는 Admin 전용 DTO 변환만 담당한다.
 */
@Service
@RequiredArgsConstructor
public class AdminPresignedUrlCommandService {

    private final FileUploadService fileUploadService;

    public PresignedUrlResponse issueImageUploadUrl(IssuePresignedUrlRequest req) {
        FileUploadService.PresignedIssueResult result = fileUploadService.issuePresignedUrl(
                req.getUploadCategory(),
                req.getOriginalFileName(),
                req.getContentType(),
                req.getContentLength()
        );

        return PresignedUrlResponse.builder()
                .uploadUrl(result.uploadUrl())
                .headers(result.headers())
                .storedFileName(result.storedFileName())
                .fileUrl(result.fileUrl())
                .expiresInSeconds(result.expiresInSeconds())
                .build();
    }
}