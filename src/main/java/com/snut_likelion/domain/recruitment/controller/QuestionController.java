package com.snut_likelion.domain.recruitment.controller;

import com.snut_likelion.domain.recruitment.dto.response.QuestionResponse;
import com.snut_likelion.domain.recruitment.entity.DepartmentType;
import com.snut_likelion.domain.recruitment.service.QuestionService;
import com.snut_likelion.domain.user.entity.Part;
import com.snut_likelion.global.dto.ApiResponse;
// Swagger 관련 import
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Recruitment", description = "모집 공고 및 질문 관리 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @Operation(summary = "모집 질문 조회", description = "특정 모집 공고의 질문 목록을 조회합니다. 파트(Part)나 학과 타입(Department)별로 필터링이 가능합니다.")
    @GetMapping("/recruitments/{recId}/questions")
    public ApiResponse<List<QuestionResponse>> getQuestions(
            @Parameter(description = "모집 공고 ID(recId)") @PathVariable("recId") Long recId,
            @Parameter(description = "모집 파트 (예: PM, DESIGN, FRONTEND, BACKEND)") @RequestParam(value = "part", required = false) Part part,
            @Parameter(description = "학과 구분 (일반/디자인 등)") @RequestParam(value = "department", required = false) DepartmentType department
    ) {
        return ApiResponse.success(questionService.getQuestions(recId, part, department), "질문 조회 성공");
    }

}