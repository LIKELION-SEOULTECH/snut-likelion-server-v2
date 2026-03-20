package com.snut_likelion.domain.file.service;

import com.snut_likelion.domain.file.dto.PresignedIssueResult;
import com.snut_likelion.domain.file.dto.UploadCategory;
import com.snut_likelion.domain.file.entity.UploadedFile;
import com.snut_likelion.domain.file.repository.UploadedFileRepository;
import com.snut_likelion.global.error.exception.BadRequestException;
import com.snut_likelion.global.error.exception.ExistingResourceException;
import com.snut_likelion.global.provider.FileProvider;
import com.snut_likelion.global.provider.PresignedUrlProvider;
import com.snut_likelion.global.provider.UploadedFileVerifier;
import com.snut_likelion.infra.file.FileErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 파일 업로드 공통 서비스(BlogCommandService, ProjectCommandService 공통 사용)
 *
 * 역할:
 *   1. Presigned PUT URL 발급 + storedFileName(S3 key) 생성
 *   2. Upload-complete 처리 (S3 존재 확인 + UploadedFile DB 저장)
 *   3. 도메인 서비스에서 storedFileName 유효성 검증 재사용
 *   4. 파일 삭제 위임
 *   5. key → 접근 URL 변환
 */
@Service
@RequiredArgsConstructor
public class FileUploadService {

    // Presigned URL 유효 시간 (10분)
    private static final Duration PRESIGNED_EXPIRES = Duration.ofMinutes(10);

    private final PresignedUrlProvider presignedUrlProvider;
    private final FileProvider fileProvider;
    private final UploadedFileVerifier uploadedFileVerifier;
    private final UploadedFileRepository uploadedFileRepository;

    /**
     * S3 Presigned PUT URL과 storedFileName(S3 key)을 발급한다.
     *
     * @param category         업로드 카테고리 (BLOG, PROJECT, MEMBER, NOTICE)
     * @param originalFileName 클라이언트가 보낸 원본 파일명
     * @param contentType      MIME 타입 (image/png, image/jpeg, image/webp)
     * @param contentLength    파일 크기 (bytes)
     */
    public PresignedIssueResult issuePresignedUrl(
            UploadCategory category,
            String originalFileName,
            String contentType,
            long contentLength
    ) {
        // 서버가 S3 key를 직접 생성 — 형식: images/{category}/{uuid}-{sanitizedName}.{ext}
        String storedFileName = generateStoredFileName(
                category.getFolderName(), originalFileName, contentType
        );

        PresignedUrlProvider.PresignedPutUrl putUrl =
                presignedUrlProvider.issuePutUrl(storedFileName, contentType, PRESIGNED_EXPIRES);

        String fileUrl = fileProvider.buildImageUrl(storedFileName);

        return new PresignedIssueResult(
                putUrl.url(),
                putUrl.headers(),
                storedFileName,
                fileUrl,
                PRESIGNED_EXPIRES.toSeconds()
        );
    }

    /**
     * S3에 실제 파일이 올라갔는지 확인 후 UploadedFile 메타데이터를 DB에 저장한다.
     * 클라이언트가 presigned 요청에서 받은 storedFileName을 그대로 전달해야 한다.
     */
    @Transactional
    public UploadedFile completeUpload(
            UploadCategory category,
            String storedFileName,
            String originalFileName,
            String contentType,
            long contentLength
    ) {
        // key 규칙 검증 (카테고리 폴더 prefix, ".." 금지)
        validateKeyRule(storedFileName, category);

        // 중복 등록 방지
        if (uploadedFileRepository.existsByStoredFileName(storedFileName)) {
            throw new ExistingResourceException(
                    FileErrorCode.FILE_UPLOAD_FAILED, "이미 등록된 파일입니다."
            );
        }

        // S3(또는 로컬)에 실제 파일이 존재하는지 확인
        if (!uploadedFileVerifier.exists(storedFileName)) {
            throw new BadRequestException(
                    FileErrorCode.FILE_NOT_FOUND, "업로드가 완료되지 않았거나 파일이 존재하지 않습니다."
            );
        }

        return uploadedFileRepository.save(
                UploadedFile.builder()
                        .uploadCategory(category)
                        .storedFileName(storedFileName)
                        .originalFileName(originalFileName)
                        .contentType(contentType)
                        .contentLength(contentLength)
                        .build()
        );
    }

    /**
     * 도메인 서비스(BlogCommandService, ProjectCommandService 등)에서
     * imageStoredFileNames를 받아 엔티티에 저장하기 전에 호출한다.
     *
     * 검증 내용:
     *   1. key 형식: "images/{category}/" 로 시작, ".." 금지
     *   2. UploadedFile 테이블에 실제 존재 (STEP 3이 완료된 파일만 허용)
     */
    public void validateStoredFileNames(List<String> storedFileNames, UploadCategory category) {
        if (storedFileNames == null || storedFileNames.isEmpty()) return;

        storedFileNames.forEach(key -> {
            validateKeyRule(key, category);

            if (!uploadedFileRepository.existsByStoredFileName(key)) {
                throw new BadRequestException(
                        FileErrorCode.FILE_NOT_FOUND,
                        "등록되지 않은 파일입니다: " + key
                );
            }
        });
    }


    /**
     * S3(또는 로컬)에서 파일을 삭제한다.
     * storedFileName(S3 key)을 인자로 받는다.
     */
    public void deleteFile(String storedFileName) {
        fileProvider.deleteFile(storedFileName);
    }

    /**
     * storedFileName(S3 key) → 접근 가능한 URL로 변환한다.
     * 조회 응답 DTO에서 key를 URL로 바꿀 때 사용한다.
     * ex) "images/blogs/uuid-name.png" → "https://bucket.s3.../images/blogs/uuid-name.png"
     */
    public String buildFileUrl(String storedFileName) {
        return fileProvider.buildImageUrl(storedFileName);
    }


    // 내부 유틸
    private String generateStoredFileName(String folderName, String originalFileName, String contentType) {
        String uuid = UUID.randomUUID().toString();
        String baseName = sanitizeBaseName(originalFileName);
        String ext = extensionFromContentType(contentType);
        return String.format("images/%s/%s-%s.%s", folderName, uuid, baseName, ext);
    }

    private void validateKeyRule(String storedFileName, UploadCategory category) {
        String expectedPrefix = "images/" + category.getFolderName() + "/";
        if (storedFileName == null || storedFileName.isBlank()
                || !storedFileName.startsWith(expectedPrefix)
                || storedFileName.contains("..")) {
            throw new BadRequestException(FileErrorCode.INVALID_FILE_KEY,
                    "storedFileName 규칙 위반: " + storedFileName);
        }
    }

    private String sanitizeBaseName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) return "file";
        int lastSlash = Math.max(originalFileName.lastIndexOf('/'), originalFileName.lastIndexOf('\\'));
        String name = lastSlash >= 0 ? originalFileName.substring(lastSlash + 1) : originalFileName;
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) name = name.substring(0, lastDot);
        name = name.replaceAll("[^a-zA-Z0-9_-]", "_");
        if (name.length() > 50) name = name.substring(0, 50);
        return name.isBlank() ? "file" : name;
    }


    // contentType enum으로 리팩터링 예정!
    private String extensionFromContentType(String contentType) {
        if (contentType == null) return "png";
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/png"  -> "png";
            case "image/jpeg" -> "jpg";
            case "image/webp" -> "webp";
            default           -> "png";
        };
    }

}
