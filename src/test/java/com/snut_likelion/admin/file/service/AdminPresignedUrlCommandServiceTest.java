package com.snut_likelion.admin.file.service;

import com.snut_likelion.admin.file.dto.request.IssuePresignedUrlRequest;
import com.snut_likelion.admin.file.dto.response.PresignedUrlResponse;
import com.snut_likelion.domain.file.dto.PresignedIssueResult;
import com.snut_likelion.domain.file.dto.UploadCategory;
import com.snut_likelion.domain.file.service.FileUploadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPresignedUrlCommandServiceTest {

    @Mock private FileUploadService fileUploadService;

    @InjectMocks
    private AdminPresignedUrlCommandService service;

    @Test
    void issueImageUploadUrl_delegatesAndMapsToResponse() {
        // Given
        IssuePresignedUrlRequest req = mock(IssuePresignedUrlRequest.class);
        when(req.getUploadCategory()).thenReturn(UploadCategory.BLOG);
        when(req.getOriginalFileName()).thenReturn("photo.png");
        when(req.getContentType()).thenReturn("image/png");
        when(req.getContentLength()).thenReturn(1024L);

        PresignedIssueResult result = new PresignedIssueResult(
                "https://s3.upload.url",
                Map.of("Content-Type", "image/png"),
                "images/blogs/uuid-photo.png",
                "https://cdn/images/blogs/uuid-photo.png",
                600L
        );
        when(fileUploadService.issuePresignedUrl(
                UploadCategory.BLOG, "photo.png", "image/png", 1024L
        )).thenReturn(result);

        // When
        PresignedUrlResponse response = service.issueImageUploadUrl(req);

        // Then
        assertThat(response.getUploadUrl()).isEqualTo("https://s3.upload.url");
        assertThat(response.getStoredFileName()).isEqualTo("images/blogs/uuid-photo.png");
        assertThat(response.getFileUrl()).isEqualTo("https://cdn/images/blogs/uuid-photo.png");
        assertThat(response.getExpiresInSeconds()).isEqualTo(600L);
    }
}
