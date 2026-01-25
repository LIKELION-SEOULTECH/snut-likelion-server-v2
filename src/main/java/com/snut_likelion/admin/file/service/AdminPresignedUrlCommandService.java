package com.snut_likelion.admin.file.service;

import com.snut_likelion.admin.file.dto.request.IssuePresignedUrlRequest;
import com.snut_likelion.admin.file.dto.response.PresignedUrlResponse;
import com.snut_likelion.global.provider.FileProvider;
import com.snut_likelion.global.provider.PresignedUrlProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminPresignedUrlCommandService {

    // Presigned URL 유효 시간 (10분)
    private static final Duration PRESIGNED_EXPIRES = Duration.ofMinutes(10);

    private final PresignedUrlProvider presignedUrlProvider;
    private final FileProvider fileProvider;

    public PresignedUrlResponse issueImageUploadUrl(IssuePresignedUrlRequest request) {
        String storedFileName = generateStoredFileName(
                request.getUploadCategory().getFolderName(),
                request.getOriginalFileName(),
                request.getContentType()
        );

        PresignedUrlProvider.PresignedPutUrl putUrl =
                presignedUrlProvider.issuePutUrl(storedFileName, request.getContentType(), PRESIGNED_EXPIRES);

        // 프로젝트 기존 방식: 환경별(Dev/Prod) 조회 URL은 FileProvider가 책임짐
        String fileUrl = fileProvider.buildImageUrl(storedFileName);

        return PresignedUrlResponse.builder()
                .uploadUrl(putUrl.url())
                .headers(putUrl.headers())
                .storedFileName(storedFileName)
                .fileUrl(fileUrl)
                .expiresInSeconds(PRESIGNED_EXPIRES.toSeconds())
                .build();
    }

    private String generateStoredFileName(String folderName, String originalFileName, String contentType) {
        String uuid = UUID.randomUUID().toString();
        String baseName = sanitizeBaseName(originalFileName);
        String ext = extensionFromContentType(contentType);

        // LocalPresignedController 보안조건: 반드시 images/ 로 시작 & '..' 없어야 함
        // baseName에는 점(.)을 허용하지 않아 '..' 생성 자체를 차단
        return String.format("images/%s/%s-%s.%s", folderName, uuid, baseName, ext);
    }

    private String sanitizeBaseName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            return "file";
        }

        // 1) 경로 분리자 제거 (마지막 구분자 이후만)
        String name = originalFileName;
        int lastSlash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (lastSlash >= 0) {
            name = name.substring(lastSlash + 1);
        }

        // 2) 확장자 제거 (점 포함)
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            name = name.substring(0, lastDot);
        }

        // 3) 허용 문자만 남김: 영문/숫자/언더바/하이픈
        name = name.replaceAll("[^a-zA-Z0-9_-]", "_");

        // 4) 길이 제한(너무 긴 파일명 방지)
        if (name.length() > 50) {
            name = name.substring(0, 50);
        }

        if (name.isBlank()) {
            return "file";
        }
        return name;
    }

    private String extensionFromContentType(String contentType) {
        if (contentType == null) {
            // DTO에서 이미 @NotBlank로 걸러지지만, 방어적으로 처리
            return "png";
        }

        String ct = contentType.toLowerCase(Locale.ROOT);
        return switch (ct) {
            case "image/png" -> "png";
            case "image/jpeg" -> "jpg";
            case "image/webp" -> "webp";
            default -> "png"; // DTO regex로 여기까지 잘 안 옴(방어 코드)
        };
    }
}
