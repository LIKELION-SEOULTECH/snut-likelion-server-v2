package com.snut_likelion.admin.file.controller;

import com.snut_likelion.admin.file.dto.request.IssuePresignedUrlRequest;
import com.snut_likelion.admin.file.dto.response.PresignedUrlResponse;
import com.snut_likelion.admin.file.service.AdminPresignedUrlCommandService;
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
public class AdminPresignedUrlController {

    private final AdminPresignedUrlCommandService adminPresignedUrlCommandService;

    @Operation(
            summary = "STEP 1 - Presigned URL 발급",
            description = """
                    S3에 파일을 직접 업로드하기 위한 Presigned PUT URL을 발급합니다.

                    **전체 파일 업로드 순서**
                    1. `POST /api/v1/admin/files/presigned-url` — Presigned URL 발급 **(현재)**
                    2. 응답의 `uploadUrl` 로 파일을 **직접 PUT 업로드** (서버를 거치지 않음)
                       - PUT 요청 시 응답의 `headers` 값을 헤더에 포함해야 합니다 (주로 Content-Type)
                       - URL 유효시간: **10분** (만료 시 재발급 필요)
                    3. `POST /api/v1/admin/files/upload-complete` — 업로드 완료 등록
                    4. 도메인 API (공지 생성/수정 등)에 `storedFileName` 포함하여 호출

                    **uploadCategory 값**
                    - `BLOG` / `PROJECT` / `NOTICE` / `MEMBER`

                    **fileStorageType 값** (생략 시 IMAGE)
                    - `IMAGE` — 이미지 파일 (images/{category}/... 경로)
                    - `FILE` — 문서·파일 (files/{category}/... 경로)

                    **허용 contentType**
                    - 이미지: `image/png`, `image/jpeg`, `image/webp`
                    - 문서: `application/pdf`, `application/zip`, `text/plain`, `application/vnd.openxmlformats-officedocument.wordprocessingml.document` (docx), `...spreadsheetml.sheet` (xlsx), `...presentationml.presentation` (pptx)
                    """
    )
    @PostMapping("/presigned-url")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PresignedUrlResponse> issuePresignedUrl(@Valid @RequestBody IssuePresignedUrlRequest request) {
        PresignedUrlResponse response = adminPresignedUrlCommandService.issueImageUploadUrl(request);
        return ApiResponse.success(response);
    }
}
