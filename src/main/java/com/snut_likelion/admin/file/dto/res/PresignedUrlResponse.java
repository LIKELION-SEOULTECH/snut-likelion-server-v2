package com.snut_likelion.admin.file.dto.res;

import com.snut_likelion.domain.file.service.FileUploadService;
import com.snut_likelion.domain.file.dto.PresignedIssueResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Schema(description = "Presigned URL 발급 응답")
public class PresignedUrlResponse {

    @Schema(description = "파일을 업로드할 Presigned URL (PUT 요청용)", example = "https://s3...")
    private String uploadUrl;

    @Schema(description = "업로드 시 함께 보내야 할 헤더 정보 (Content-Type 등)")
    private Map<String, String> headers;

    @Schema(description = "S3에 저장될 실제 키(경로 포함)", example = "images/blogs/uuid-file.png")
    private String storedFileName;

    @Schema(description = "업로드 완료 후 접근 가능한 이미지 조회 URL", example = "https://bucket.s3.region.amazonaws.com/images/...")
    private String fileUrl;

    @Schema(description = "URL 유효 시간 (초)", example = "600")
    private long expiresInSeconds;

    public static PresignedUrlResponse from(PresignedIssueResult result) {
        return PresignedUrlResponse.builder()
                .uploadUrl(result.uploadUrl())
                .headers(result.headers())
                .storedFileName(result.storedFileName())
                .fileUrl(result.fileUrl())
                .expiresInSeconds(result.expiresInSeconds())
                .build();
    }

    @Builder
    private PresignedUrlResponse(
            String uploadUrl,
            Map<String, String> headers,
            String storedFileName,
            String fileUrl,
            long expiresInSeconds
    ) {
        this.uploadUrl = uploadUrl;
        this.headers = headers;
        this.storedFileName = storedFileName;
        this.fileUrl = fileUrl;
        this.expiresInSeconds = expiresInSeconds;
    }
}
