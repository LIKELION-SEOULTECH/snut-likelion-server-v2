package com.snut_likelion.ai.controller;

import com.snut_likelion.ai.dto.request.AiChatRequest;
import com.snut_likelion.ai.dto.request.AiSummarizeRequest;
import com.snut_likelion.ai.dto.response.AiChatResult;
import com.snut_likelion.ai.dto.response.AiSummarizeResult;
import com.snut_likelion.ai.service.AiQueryService;
import com.snut_likelion.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI", description = "AI 조회 API")
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiQueryService aiQueryService;

    @Operation(summary = "AI 챗봇", description = "AI 챗봇을 호출하고 매핑된 답변을 반환합니다.")
    @PostMapping("/chat")
    public ApiResponse<AiChatResult> chat(@Valid @RequestBody AiChatRequest request) {
        return ApiResponse.success(aiQueryService.chat(request.getText()));
    }

    @Operation(summary = "AI 요약", description = "텍스트를 AI 서버에 전달하여 요약 결과를 반환합니다.")
    @PostMapping("/summarize")
    public ApiResponse<AiSummarizeResult> summarize(@Valid @RequestBody AiSummarizeRequest request) {
        return ApiResponse.success(aiQueryService.summarize(request.getText()));
    }
}
