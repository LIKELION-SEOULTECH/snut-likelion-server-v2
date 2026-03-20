package com.snut_likelion.admin.file.service;

import com.snut_likelion.admin.file.dto.request.CompleteFileUploadRequest;
import com.snut_likelion.admin.file.dto.response.CompleteFileUploadResponse;
import com.snut_likelion.domain.file.entity.UploadedFile;
import com.snut_likelion.domain.file.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin 파일 업로드 완료 서비스
 *
 * 실제 로직은 FileUploadService에 위임한다.
 * 이 클래스는 Admin 전용 DTO 변환만 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AdminFileUploadCommandService {

    private final FileUploadService fileUploadService;

    public CompleteFileUploadResponse completeUpload(CompleteFileUploadRequest req) {
        UploadedFile saved = fileUploadService.completeUpload(
                req.getUploadCategory(),
                req.getStoredFileName(),
                req.getOriginalFileName(),
                req.getContentType(),
                req.getContentLength()
        );

        String fileUrl = fileUploadService.buildFileUrl(saved.getStoredFileName());
        return CompleteFileUploadResponse.from(saved, fileUrl);
    }
}
