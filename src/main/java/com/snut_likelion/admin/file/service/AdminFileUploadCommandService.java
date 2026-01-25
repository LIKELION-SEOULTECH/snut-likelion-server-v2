package com.snut_likelion.admin.file.service;

import com.snut_likelion.admin.file.dto.request.CompleteFileUploadRequest;
import com.snut_likelion.admin.file.dto.response.CompleteFileUploadResponse;
import com.snut_likelion.domain.file.entity.UploadedFile;
import com.snut_likelion.domain.file.repository.UploadedFileRepository;
import com.snut_likelion.global.error.exception.BadRequestException;
import com.snut_likelion.global.error.exception.ExistingResourceException;
import com.snut_likelion.global.provider.FileProvider;
import com.snut_likelion.global.provider.UploadedFileVerifier;
import com.snut_likelion.infra.file.FileErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminFileUploadCommandService {

    private final UploadedFileRepository uploadedFileRepository;
    private final UploadedFileVerifier uploadedFileVerifier;
    private final FileProvider fileProvider;

    public CompleteFileUploadResponse completeUpload(CompleteFileUploadRequest request) {

        validateKeyRule(request);

        // 중복 등록 방지
        uploadedFileRepository.findByStoredFileName(request.getStoredFileName())
                .ifPresent(it -> { throw new ExistingResourceException(FileErrorCode.FILE_UPLOAD_FAILED, "이미 등록된 파일입니다."); });

        // 실제 업로드 존재 확인 (Dev: 로컬 파일, Prod: S3 HEAD)
        boolean exists = uploadedFileVerifier.exists(request.getStoredFileName());
        if (!exists) {
            throw new BadRequestException(FileErrorCode.FILE_NOT_FOUND, "업로드가 완료되지 않았거나 파일이 존재하지 않습니다.");
        }

        UploadedFile saved = uploadedFileRepository.save(request.toEntity());
        String fileUrl = fileProvider.buildImageUrl(saved.getStoredFileName());

        return CompleteFileUploadResponse.from(saved, fileUrl);
    }

    private void validateKeyRule(CompleteFileUploadRequest request) {
        String folder = request.getUploadCategory().getFolderName();
        String expectedPrefix = "images/" + folder + "/";

        String key = request.getStoredFileName();

        if (key == null || key.isBlank()) {
            throw new BadRequestException(FileErrorCode.BAD_FILE_URL, "storedFileName이 비어있습니다.");
        }
        if (!key.startsWith(expectedPrefix)) {
            throw new BadRequestException(FileErrorCode.BAD_FILE_URL,
                    "storedFileName 경로 규칙이 올바르지 않습니다. expectedPrefix=" + expectedPrefix);
        }
        if (key.contains("..")) {
            throw new BadRequestException(FileErrorCode.BAD_FILE_URL, "storedFileName에 허용되지 않는 문자열(\"..\")이 포함되어 있습니다.");
        }
    }
}
