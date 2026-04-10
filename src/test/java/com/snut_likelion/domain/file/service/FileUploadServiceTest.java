package com.snut_likelion.domain.file.service;

import com.snut_likelion.domain.file.dto.FileStorageType;
import com.snut_likelion.domain.file.dto.PresignedIssueResult;
import com.snut_likelion.domain.file.dto.UploadCategory;
import com.snut_likelion.domain.file.entity.UploadedFile;
import com.snut_likelion.domain.file.repository.UploadedFileRepository;
import com.snut_likelion.global.error.exception.BadRequestException;
import com.snut_likelion.global.error.exception.ExistingResourceException;
import com.snut_likelion.global.provider.FileProvider;
import com.snut_likelion.global.provider.PresignedUrlProvider;
import com.snut_likelion.global.provider.UploadedFileVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class FileUploadServiceTest {

    @Mock private PresignedUrlProvider presignedUrlProvider;
    @Mock private FileProvider fileProvider;
    @Mock private UploadedFileVerifier uploadedFileVerifier;
    @Mock private UploadedFileRepository uploadedFileRepository;

    @InjectMocks
    private FileUploadService fileUploadService;

    // ── issuePresignedUrl ────────────────────────────────────────────────────

    @Test
    void issuePresignedUrl_returnsResultWithCorrectKeyFormat() {
        // Given
        PresignedUrlProvider.PresignedPutUrl putUrl =
                new PresignedUrlProvider.PresignedPutUrl("https://s3.upload.url", Map.of("Content-Type", "image/png"));
        when(presignedUrlProvider.issuePutUrl(anyString(), eq("image/png"), any())).thenReturn(putUrl);
        when(fileProvider.buildImageUrl(anyString())).thenReturn("https://cdn/images/blogs/uuid-photo.png");

        // When
        PresignedIssueResult result = fileUploadService.issuePresignedUrl(
                UploadCategory.BLOG, "photo.png", "image/png", 1024L
        );

        // Then
        assertThat(result.uploadUrl()).isEqualTo("https://s3.upload.url");
        assertThat(result.storedFileName()).startsWith("images/blogs/");
        assertThat(result.storedFileName()).endsWith(".png");
        assertThat(result.fileUrl()).isEqualTo("https://cdn/images/blogs/uuid-photo.png");
        assertThat(result.expiresInSeconds()).isEqualTo(600L);
    }

    @Test
    void issuePresignedUrl_jpeg_generatesJpgExtension() {
        // Given
        PresignedUrlProvider.PresignedPutUrl putUrl =
                new PresignedUrlProvider.PresignedPutUrl("https://s3.upload.url", Map.of());
        when(presignedUrlProvider.issuePutUrl(anyString(), eq("image/jpeg"), any())).thenReturn(putUrl);
        when(fileProvider.buildImageUrl(anyString())).thenReturn("https://cdn/any");

        // When
        PresignedIssueResult result = fileUploadService.issuePresignedUrl(
                UploadCategory.PROJECT, "image.jpg", "image/jpeg", 2048L
        );

        // Then
        assertThat(result.storedFileName()).startsWith("images/projects/");
        assertThat(result.storedFileName()).endsWith(".jpg");
    }

    // ── completeUpload ───────────────────────────────────────────────────────

    @Test
    void completeUpload_success_savesAndReturns() {
        // Given
        String key = "images/blogs/uuid-photo.png";
        UploadedFile saved = UploadedFile.builder()
                .uploadCategory(UploadCategory.BLOG)
                .storedFileName(key)
                .originalFileName("photo.png")
                .contentType("image/png")
                .contentLength(1024L)
                .build();

        when(uploadedFileRepository.existsByStoredFileName(key)).thenReturn(false);
        when(uploadedFileVerifier.exists(key)).thenReturn(true);
        when(uploadedFileRepository.save(any())).thenReturn(saved);

        // When
        UploadedFile result = fileUploadService.completeUpload(
                UploadCategory.BLOG, FileStorageType.IMAGE, key, "photo.png", "image/png", 1024L
        );

        // Then
        assertThat(result.getStoredFileName()).isEqualTo(key);
        verify(uploadedFileRepository).save(any());
    }

    @Test
    void completeUpload_alreadyRegistered_throwsExistingResource() {
        // Given
        String key = "images/blogs/uuid-photo.png";
        when(uploadedFileRepository.existsByStoredFileName(key)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> fileUploadService.completeUpload(
                UploadCategory.BLOG, FileStorageType.IMAGE, key, "photo.png", "image/png", 1024L
        )).isInstanceOf(ExistingResourceException.class);
    }

    @Test
    void completeUpload_fileNotUploadedToS3_throwsBadRequest() {
        // Given
        String key = "images/blogs/uuid-photo.png";
        when(uploadedFileRepository.existsByStoredFileName(key)).thenReturn(false);
        when(uploadedFileVerifier.exists(key)).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> fileUploadService.completeUpload(
                UploadCategory.BLOG, FileStorageType.IMAGE, key, "photo.png", "image/png", 1024L
        )).isInstanceOf(BadRequestException.class);
    }

    // ── validateStoredFileNames ──────────────────────────────────────────────

    @Test
    void validateStoredFileNames_validKeys_passes() {
        // Given
        List<String> keys = List.of("images/blogs/uuid-photo.png");
        when(uploadedFileRepository.existsByStoredFileName("images/blogs/uuid-photo.png")).thenReturn(true);

        // When / Then
        assertDoesNotThrow(() -> fileUploadService.validateStoredFileNames(keys, UploadCategory.BLOG));
    }

    @Test
    void validateStoredFileNames_emptyList_passes() {
        assertDoesNotThrow(() -> fileUploadService.validateStoredFileNames(List.of(), UploadCategory.BLOG));
    }

    @Test
    void validateStoredFileNames_wrongCategoryPrefix_throwsBadRequest() {
        // "images/projects/..." 키를 BLOG 카테고리로 검증 → 실패
        List<String> keys = List.of("images/projects/uuid-photo.png");

        assertThatThrownBy(() -> fileUploadService.validateStoredFileNames(keys, UploadCategory.BLOG))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void validateStoredFileNames_pathTraversal_throwsBadRequest() {
        List<String> keys = List.of("images/blogs/../etc/passwd");

        assertThatThrownBy(() -> fileUploadService.validateStoredFileNames(keys, UploadCategory.BLOG))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void validateStoredFileNames_notRegisteredInDb_throwsBadRequest() {
        // Given
        String key = "images/blogs/uuid-photo.png";
        when(uploadedFileRepository.existsByStoredFileName(key)).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> fileUploadService.validateStoredFileNames(List.of(key), UploadCategory.BLOG))
                .isInstanceOf(BadRequestException.class);
    }

    // ── issuePresignedUrl (FileStorageType 오버로드) ──────────────────────────

    @Test
    void issuePresignedUrl_withFileStorageType_generatesFilesRootPath() {
        // Given
        PresignedUrlProvider.PresignedPutUrl putUrl =
                new PresignedUrlProvider.PresignedPutUrl("https://s3.upload.url", Map.of("Content-Type", "application/pdf"));
        when(presignedUrlProvider.issuePutUrl(anyString(), eq("application/pdf"), any())).thenReturn(putUrl);
        when(fileProvider.buildImageUrl(anyString())).thenReturn("https://cdn/files/notices/uuid-report.pdf");

        // When
        PresignedIssueResult result = fileUploadService.issuePresignedUrl(
                UploadCategory.NOTICE, FileStorageType.FILE, "report.pdf", "application/pdf", 204800L
        );

        // Then
        assertThat(result.storedFileName()).startsWith("files/notices/");
        assertThat(result.storedFileName()).endsWith(".pdf");
    }

    @Test
    void issuePresignedUrl_defaultOverload_usesImageStorageType() {
        // Given
        PresignedUrlProvider.PresignedPutUrl putUrl =
                new PresignedUrlProvider.PresignedPutUrl("https://s3.upload.url", Map.of());
        when(presignedUrlProvider.issuePutUrl(anyString(), eq("image/webp"), any())).thenReturn(putUrl);
        when(fileProvider.buildImageUrl(anyString())).thenReturn("https://cdn/images/members/uuid.webp");

        // When
        PresignedIssueResult result = fileUploadService.issuePresignedUrl(
                UploadCategory.MEMBER, "avatar.webp", "image/webp", 512L
        );

        // Then — 기본값은 images/ 경로
        assertThat(result.storedFileName()).startsWith("images/members/");
        assertThat(result.storedFileName()).endsWith(".webp");
    }

    // ── validateStoredFileNames (FileStorageType 오버로드) ────────────────────

    @Test
    void validateStoredFileNames_withFileStorageType_acceptsFilesPrefix() {
        // Given
        String key = "files/notices/uuid-report.pdf";
        when(uploadedFileRepository.existsByStoredFileName(key)).thenReturn(true);

        // When / Then
        assertDoesNotThrow(() ->
                fileUploadService.validateStoredFileNames(List.of(key), UploadCategory.NOTICE, FileStorageType.FILE));
    }

    @Test
    void validateStoredFileNames_withFileStorageType_rejectsImagesPrefix() {
        // "images/..." 경로는 FileStorageType.FILE 검증에서 실패해야 함
        String key = "images/notices/uuid-photo.png";

        assertThatThrownBy(() ->
                fileUploadService.validateStoredFileNames(List.of(key), UploadCategory.NOTICE, FileStorageType.FILE))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void validateStoredFileNames_withImageStorageType_rejectsFilesPrefix() {
        // "files/..." 경로는 FileStorageType.IMAGE 검증에서 실패해야 함
        String key = "files/notices/uuid-report.pdf";

        assertThatThrownBy(() ->
                fileUploadService.validateStoredFileNames(List.of(key), UploadCategory.NOTICE, FileStorageType.IMAGE))
                .isInstanceOf(BadRequestException.class);
    }

    // ── findUploadedFiles ────────────────────────────────────────────────────

    @Test
    void findUploadedFiles_returnsMatchingEntities() {
        // Given
        List<String> keys = List.of("images/notices/uuid1.png", "files/notices/uuid2.pdf");
        List<UploadedFile> files = List.of(
                UploadedFile.builder().uploadCategory(UploadCategory.NOTICE)
                        .storedFileName("images/notices/uuid1.png").originalFileName("banner.png")
                        .contentType("image/png").contentLength(1024L).build(),
                UploadedFile.builder().uploadCategory(UploadCategory.NOTICE)
                        .storedFileName("files/notices/uuid2.pdf").originalFileName("report.pdf")
                        .contentType("application/pdf").contentLength(204800L).build()
        );
        when(uploadedFileRepository.findAllByStoredFileNameIn(keys)).thenReturn(files);

        // When
        List<UploadedFile> result = fileUploadService.findUploadedFiles(keys);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(UploadedFile::getOriginalFileName)
                .containsExactlyInAnyOrder("banner.png", "report.pdf");
    }

    @Test
    void findUploadedFiles_emptyInput_returnsEmptyList() {
        // When
        List<UploadedFile> result = fileUploadService.findUploadedFiles(List.of());

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(uploadedFileRepository);
    }

    @Test
    void findUploadedFiles_nullInput_returnsEmptyList() {
        // When
        List<UploadedFile> result = fileUploadService.findUploadedFiles(null);

        // Then
        assertThat(result).isEmpty();
    }

    // ── deleteFile / buildFileUrl ────────────────────────────────────────────

    @Test
    void deleteFile_delegatesToFileProvider() {
        fileUploadService.deleteFile("images/blogs/uuid-photo.png");
        verify(fileProvider).deleteFile("images/blogs/uuid-photo.png");
    }

    @Test
    void buildFileUrl_delegatesToFileProvider() {
        // Given
        when(fileProvider.buildImageUrl("images/blogs/uuid-photo.png"))
                .thenReturn("https://cdn/images/blogs/uuid-photo.png");

        // When
        String url = fileUploadService.buildFileUrl("images/blogs/uuid-photo.png");

        // Then
        assertThat(url).isEqualTo("https://cdn/images/blogs/uuid-photo.png");
    }

    @Test
    void buildFileDownloadUrl_delegatesToFileProvider() {
        // Given
        when(fileProvider.buildFileDownloadUrl("files/notices/uuid-report.pdf"))
                .thenReturn("https://cdn/files/notices/uuid-report.pdf");

        // When
        String url = fileUploadService.buildFileDownloadUrl("files/notices/uuid-report.pdf");

        // Then
        assertThat(url).isEqualTo("https://cdn/files/notices/uuid-report.pdf");
    }

    @Test
    void issuePresignedUrl_zip_generatesZipExtension() {
        PresignedUrlProvider.PresignedPutUrl putUrl =
                new PresignedUrlProvider.PresignedPutUrl("https://s3.upload.url", Map.of());
        when(presignedUrlProvider.issuePutUrl(anyString(), eq("application/zip"), any())).thenReturn(putUrl);
        when(fileProvider.buildImageUrl(anyString())).thenReturn("https://cdn/any");

        PresignedIssueResult result = fileUploadService.issuePresignedUrl(
                UploadCategory.NOTICE, FileStorageType.FILE, "archive.zip", "application/zip", 1024L
        );
        assertThat(result.storedFileName()).endsWith(".zip");
    }

    @Test
    void issuePresignedUrl_unknownContentType_generatesBinExtension() {
        PresignedUrlProvider.PresignedPutUrl putUrl =
                new PresignedUrlProvider.PresignedPutUrl("https://s3.upload.url", Map.of());
        when(presignedUrlProvider.issuePutUrl(anyString(), eq("application/octet-stream"), any())).thenReturn(putUrl);
        when(fileProvider.buildImageUrl(anyString())).thenReturn("https://cdn/any");

        PresignedIssueResult result = fileUploadService.issuePresignedUrl(
                UploadCategory.BLOG, FileStorageType.IMAGE, "file.bin", "application/octet-stream", 1024L
        );
        assertThat(result.storedFileName()).endsWith(".bin");
    }

    @Test
    void issuePresignedUrl_completeUpload_invalidKeyRule_throws() {
        // A key not starting with expected prefix
        assertThatThrownBy(() -> fileUploadService.completeUpload(
                UploadCategory.BLOG, FileStorageType.IMAGE,
                "images/projects/uuid-photo.png", "photo.png", "image/png", 1024L
        )).isInstanceOf(BadRequestException.class);
    }
}
