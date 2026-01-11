package com.snut_likelion.admin.recruitment.controller;

import com.snut_likelion.admin.recruitment.dto.request.ApplicationListStatus;
import com.snut_likelion.admin.recruitment.dto.request.ChangeApplicationStatusParameter;
import com.snut_likelion.admin.recruitment.dto.request.ChangeApplicationStatusRequest;
import com.snut_likelion.admin.recruitment.dto.response.ApplicationPageResponse;
import com.snut_likelion.admin.recruitment.service.AdminApplicationService;
import com.snut_likelion.domain.recruitment.dto.response.ApplicationDetailsResponse;
import com.snut_likelion.domain.recruitment.dto.response.ApplicationResponse;
import com.snut_likelion.domain.user.entity.Part;
import com.snut_likelion.global.dto.ApiResponse;
// Swagger 관련 import
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Application", description = "관리자용 지원서 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ROLE_MANAGER')")
public class AdminApplicationController {

    private final AdminApplicationService adminApplicationService;

    @Operation(summary = "모집 공고별 지원서 목록 조회", description = "특정 모집 공고에 제출된 지원서 목록을 페이징하여 조회합니다. 파트 및 심사 상태별 필터링이 가능합니다.")
    @GetMapping("/recruitments/{recId}/applications")
    public ApiResponse<ApplicationPageResponse> getApplicationsByRecruitmentId(
            @Parameter(description = "모집 공고 ID(recId)") @PathVariable("recId") Long recId,
            @RequestParam("page") int page,
            @Parameter(description = "지원 파트 필터") @RequestParam(value = "part", required = false) Part part,
            @Parameter(description = "목록 표시 상태 필터") @RequestParam(value = "status", defaultValue = "SUBMITTED") ApplicationListStatus status
    ) {
        return ApiResponse.success(
                adminApplicationService.getApplicationsByRecruitmentId(recId, part, page, status),
                "지원서 조회 성공"
        );
    }

    @Operation(summary = "지원서 상세 조회", description = "관리자 권한으로 특정 지원서의 전체 내용을 상세 조회합니다.")
    @GetMapping("/applications/{appId}")
    public ApiResponse<ApplicationDetailsResponse> getApplicationDetails(
            @Parameter(description = "지원서 ID(appId)") @PathVariable("appId") Long appId
    ) {
        return ApiResponse.success(
                adminApplicationService.getApplicationDetails(appId),
                "지원서 상세 조회 성공"
        );
    }

    @Operation(summary = "지원서 상태 변경", description = "지원서의 합격/불합격 등 심사 상태를 변경합니다.")
    @PatchMapping("/applications/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateApplicationStatus(
            @Parameter(description = "변경할 상태 구분") @RequestParam(value = "status") ChangeApplicationStatusParameter status,
            @RequestBody ChangeApplicationStatusRequest req
    ) {
        adminApplicationService.updateApplicationStatus(status, req);
    }

}