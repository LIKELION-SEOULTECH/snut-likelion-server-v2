package com.snut_likelion.domain.recruitment.controller;

import com.snut_likelion.domain.recruitment.dto.response.RecruitmentResponse;
import com.snut_likelion.domain.recruitment.entity.RecruitmentType;
import com.snut_likelion.domain.recruitment.service.RecruitmentService;
import com.snut_likelion.global.dto.ApiResponse;
// Swagger 관련 import
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Recruitment", description = "모집 공고 및 질문 관리 API")
@RestController
@RequestMapping("/api/v1/recruitments")
@RequiredArgsConstructor
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    @Operation(summary = "최근 모집 공고 조회", description = "특정 타입(아기사자/운영진 등)의 가장 최근 모집 정보를 조회합니다.")
    @GetMapping
    public ApiResponse<RecruitmentResponse> getRecentRecruitmentInfo(
            @Parameter(description = "모집 구분 (예: LION, MANAGER)")
            @RequestParam("recruitmentType") RecruitmentType recruitmentType
    ) {
        return ApiResponse.success(
                recruitmentService.getRecentRecruitmentInfo(recruitmentType),
                "최근 모집 공고 조회 성공"
        );
    }

}