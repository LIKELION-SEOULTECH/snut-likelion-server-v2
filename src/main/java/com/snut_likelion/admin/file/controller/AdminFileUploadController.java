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

@Tag(name = "Admin File", description = "관리자 파일 업로드 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/files")
@PreAuthorize("hasRole('ROLE_MANAGER')")
public class AdminFileUploadController {

    private final AdminFileUploadCommandService adminFileUploadCommandService;

    @Operation(
            summary = "STEP 3 - 업로드 완료 등록",
            description = """
                    S3 PUT 업로드 완료 후 파일 메타데이터를 서버에 등록합니다.

                    **호출 시점**
                    - STEP 2(S3 직접 PUT 업로드) 성공 후 반드시 호출해야 합니다.
                    - 이 단계를 완료하지 않으면 도메인 API에서 `storedFileName` 검증이 실패합니다.

                    **요청값 출처**
                    - `storedFileName` → STEP 1 발급 응답의 `storedFileName` 그대로 사용
                    - `originalFileName` → 사용자가 업로드한 원본 파일명
                    - `contentType` / `contentLength` → 실제 업로드한 파일 정보

                    **응답값 활용**
                    - 응답의 `storedFileName` 을 도메인 API(공지 생성 등) 요청 바디에 포함하여 호출
                    """
    )
    @PostMapping("/upload-complete")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<CompleteFileUploadResponse> completeUpload(@Valid @RequestBody CompleteFileUploadRequest request) {
        CompleteFileUploadResponse response = adminFileUploadCommandService.completeUpload(request);
        return ApiResponse.success(response);
    }
}
