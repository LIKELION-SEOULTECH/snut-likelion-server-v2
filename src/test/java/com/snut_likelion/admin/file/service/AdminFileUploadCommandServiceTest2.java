package com.snut_likelion.admin.file.service;

import com.snut_likelion.admin.file.dto.req.CompleteFileUploadRequest;
import com.snut_likelion.admin.file.dto.res.CompleteFileUploadResponse;
import com.snut_likelion.domain.file.dto.FileStorageType;
import com.snut_likelion.domain.file.dto.UploadCategory;
import com.snut_likelion.domain.file.entity.UploadedFile;
import com.snut_likelion.domain.file.service.FileUploadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminFileUploadCommandServiceTest2 {

    @Mock
    FileUploadService fileUploadService;

    @InjectMocks
    AdminFileUploadCommandService adminFileUploadCommandService;

    @Test
    void completeUpload_success() throws Exception {
        java.lang.reflect.Constructor<CompleteFileUploadRequest> ctor =
                CompleteFileUploadRequest.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        CompleteFileUploadRequest req = ctor.newInstance();
        ReflectionTestUtils.setField(req, "uploadCategory", UploadCategory.BLOG);
        ReflectionTestUtils.setField(req, "fileStorageType", FileStorageType.IMAGE);
        ReflectionTestUtils.setField(req, "storedFileName", "images/blogs/uuid-test.png");
        ReflectionTestUtils.setField(req, "originalFileName", "test.png");
        ReflectionTestUtils.setField(req, "contentType", "image/png");
        ReflectionTestUtils.setField(req, "contentLength", 1024L);

        UploadedFile saved = UploadedFile.builder()
                .uploadCategory(UploadCategory.BLOG)
                .storedFileName("images/blogs/uuid-test.png")
                .originalFileName("test.png")
                .contentType("image/png")
                .contentLength(1024L)
                .build();

        when(fileUploadService.completeUpload(any(), any(), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(saved);
        when(fileUploadService.buildFileUrl("images/blogs/uuid-test.png"))
                .thenReturn("https://cdn.example.com/images/blogs/uuid-test.png");

        CompleteFileUploadResponse result = adminFileUploadCommandService.completeUpload(req);

        assertThat(result).isNotNull();
        assertThat(result.getFileUrl()).isEqualTo("https://cdn.example.com/images/blogs/uuid-test.png");
    }
}
