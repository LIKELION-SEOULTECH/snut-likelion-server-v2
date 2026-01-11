package com.snut_likelion.admin.recruitment.controller;

import com.snut_likelion.admin.recruitment.dto.request.UpdateQuestionsRequest;
import com.snut_likelion.admin.recruitment.service.AdminQuestionService;
import com.snut_likelion.domain.recruitment.dto.response.QuestionResponse;
import com.snut_likelion.global.dto.ApiResponse;
// Swagger 관련 import
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Question", description = "관리자용 모집 질문 관리 API")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_MANAGER')")
public class AdminQuestionController {

    private final AdminQuestionService adminQuestionService;

    @Operation(summary = "관리자 모집 질문 조회", description = "특정 모집 공고에 등록된 전체 질문 목록을 조회합니다. (MANAGER 권한 필요)")
    @GetMapping("/recruitments/{recId}/questions")
    public ApiResponse<List<QuestionResponse>> getQuestions(
            @Parameter(description = "모집 공고 ID(recId)") @PathVariable("recId") Long recId
    ) {
        return ApiResponse.success(
                adminQuestionService.getAllQuestions(recId),
                "질문 조회 성공"
        );
    }

    @Operation(summary = "관리자 모집 질문 수정/생성", description = "특정 모집 공고의 질문들을 일괄 수정하거나 새롭게 생성합니다.")
    @PutMapping("/recruitments/{recId}/questions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Object> updateQuestions(
            @Parameter(description = "모집 공고 ID(recId)") @PathVariable("recId") Long recId,
            @RequestBody @Valid List<UpdateQuestionsRequest> req
    ) {
        adminQuestionService.updateQuestions(recId, req);
        return ApiResponse.success("질문 생성 성공");
    }

}