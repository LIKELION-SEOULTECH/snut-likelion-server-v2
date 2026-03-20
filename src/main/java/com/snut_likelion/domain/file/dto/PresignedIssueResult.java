package com.snut_likelion.domain.file.dto;

import java.util.Map;

/**
 * Presigned URL 발급 결과 DTO
 * FileUploadService.issuePresignedUrl() 반환값
 */
public record PresignedIssueResult(
        String uploadUrl,
        Map<String, String> headers,
        String storedFileName,
        String fileUrl,
        long expiresInSeconds
) {}
