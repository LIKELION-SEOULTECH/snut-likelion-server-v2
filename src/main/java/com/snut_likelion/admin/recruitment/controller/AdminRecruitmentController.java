package com.snut_likelion.admin.recruitment.controller;

import com.snut_likelion.admin.recruitment.dto.request.CreateRecruitmentRequest;
import com.snut_likelion.admin.recruitment.dto.request.UpdateRecruitmentRequest;
import com.snut_likelion.admin.recruitment.service.AdminRecruitmentService;
import com.snut_likelion.global.dto.ApiResponse;
// Swagger 관련 import 추가
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Recruitment", description = "관리자용 모집 공고 관리 API")
@RestController
@RequestMapping("/api/v1/admin/recruitments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_MANAGER')")
public class AdminRecruitmentController {

    private final AdminRecruitmentService adminRecruitmentService;

    @Operation(summary = "모집 공고 생성", description = "새로운 기수의 모집 공고(기간, 타입 등)를 생성합니다. (MANAGER 권한 필요)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Object> createRecruitment(
            @RequestBody @Valid CreateRecruitmentRequest req
    ) {
        adminRecruitmentService.createRecruitment(req);
        return ApiResponse.success("모집 공고 생성 완료");
    }

    @Operation(summary = "모집 공고 수정", description = "기존 모집 공고의 상세 정보(기간, 상태 등)를 수정합니다.")
    @PatchMapping("/{recId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRecruitment(
            @Parameter(description = "수정할 모집 공고 ID(recId)") @PathVariable("recId") Long recId,
            @RequestBody @Valid UpdateRecruitmentRequest req
    ) {
        adminRecruitmentService.updateRecruitment(recId, req);
    }

    @Operation(summary = "모집 공고 삭제", description = "특정 모집 공고를 시스템에서 삭제합니다.")
    @DeleteMapping("/{recId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeRecruitment(
            @Parameter(description = "삭제할 모집 공고 ID(recId)") @PathVariable("recId") Long recId
    ) {
        adminRecruitmentService.removeRecruitment(recId);
    }
}