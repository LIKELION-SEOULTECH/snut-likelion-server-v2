package com.snut_likelion.admin.file.controller;

import com.snut_likelion.admin.file.dto.request.CompleteFileUploadRequest;
import com.snut_likelion.admin.file.dto.response.CompleteFileUploadResponse;
import com.snut_likelion.admin.file.service.AdminFileUploadCommandService;
import com.snut_likelion.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin File", description = "관리자 파일 업로드 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/files")
@PreAuthorize("hasRole('ROLE_MANAGER')")
public class AdminFileUploadController {

    private final AdminFileUploadCommandService adminFileUploadCommandService;

    @Operation(summary = "업로드 완료 처리", description = "Presigned 업로드 완료 후 파일 메타데이터를 DB에 저장합니다.")
    @PostMapping("/upload-complete")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<CompleteFileUploadResponse> completeUpload(@Valid @RequestBody CompleteFileUploadRequest request) {
        CompleteFileUploadResponse response = adminFileUploadCommandService.completeUpload(request);
        return ApiResponse.success(response);
    }
}
