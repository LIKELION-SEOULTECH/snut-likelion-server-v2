package com.snut_likelion.domain.user.controller;

import com.snut_likelion.domain.user.dto.response.SayingResponse;
import com.snut_likelion.domain.user.service.SayingService;
import com.snut_likelion.global.dto.ApiResponse;
// Swagger 관련 import
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Saying", description = "명언 조회 API")
@RestController
@RequestMapping("/api/v1/sayings")
@RequiredArgsConstructor
public class SayingController {

    private final SayingService sayingService;

    @Operation(summary = "명언 리스트 조회", description = "서비스에서 제공하는 명언 리스트를 전체 조회합니다.")
    @GetMapping
    public ApiResponse<List<SayingResponse>> getSayings() {
        return ApiResponse.success(
                sayingService.getSayings(),
                "명언 리스트 조회 성공"
        );
    }
}