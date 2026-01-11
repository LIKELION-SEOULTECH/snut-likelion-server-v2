package com.snut_likelion.domain.recruitment.controller;

import com.snut_likelion.domain.recruitment.dto.request.CreateApplicationRequest;
import com.snut_likelion.domain.recruitment.dto.request.UpdateApplicationRequest;
import com.snut_likelion.domain.recruitment.dto.response.ApplicationDetailsResponse;
import com.snut_likelion.domain.recruitment.service.ApplicationCommandService;
import com.snut_likelion.domain.recruitment.service.ApplicationQueryService;
import com.snut_likelion.global.auth.model.SnutLikeLionUser;
import com.snut_likelion.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Application", description = "지원서 제출 및 관리 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationCommandService applicationCommandService;
    private final ApplicationQueryService applicationQueryService;

    @Operation(summary = "내 지원서 목록 조회", description = "로그인한 사용자가 작성한 모든 지원서 목록을 조회합니다.")
    @GetMapping("/applications/me")
    public ApiResponse<List<ApplicationDetailsResponse>> getMyApplication(
            @Parameter(hidden = true) @AuthenticationPrincipal SnutLikeLionUser loginUser
    ) {
        return ApiResponse.success(
                applicationQueryService.getMyApplication(loginUser.getId()),
                "내 지원서 조회 성공"
        );
    }

    @Operation(summary = "지원서 작성", description = "특정 모집 공고에 대한 지원서를 작성합니다. submit 값에 따라 임시저장/최종제출이 결정됩니다.")
    @PostMapping("/recruitments/{recId}/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Object> createApplication(
            @Parameter(description = "모집 공고 ID(recId)") @PathVariable("recId") Long recId,
            @Parameter(hidden = true) @AuthenticationPrincipal SnutLikeLionUser loginUser,
            @Parameter(description = "true: 최종제출, false: 임시저장") @RequestParam(value = "submit", required = true) boolean submit,
            @ModelAttribute("createApplicationRequest") @Valid CreateApplicationRequest req
    ) {
        applicationCommandService.createApplication(recId, loginUser.getId(), submit, req);
        return ApiResponse.success("지원서 작성 성공");
    }

    @Operation(summary = "지원서 수정", description = "기존에 작성한 지원서 내용을 수정합니다. 최종 제출 이후에는 수정이 불가능할 수 있습니다.")
    @PutMapping("/applications/{appId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateApplication(
            @Parameter(description = "지원서 ID(appId)") @PathVariable("appId") Long appId,
            @Parameter(hidden = true) @AuthenticationPrincipal SnutLikeLionUser loginUser,
            @Parameter(description = "true: 최종제출, false: 임시저장") @RequestParam(value = "submit", required = true) boolean submit,
            @ModelAttribute("updateApplicationRequest") @Valid UpdateApplicationRequest req
    ) {
        applicationCommandService.updateApplication(appId, loginUser.getUserInfo(), submit, req);
    }

    @Operation(summary = "지원서 삭제", description = "작성 중인 지원서를 삭제합니다.")
    @DeleteMapping("/applications/{appId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteApplication(
            @Parameter(description = "지원서 ID(appId)") @PathVariable("appId") Long appId,
            @Parameter(hidden = true) @AuthenticationPrincipal SnutLikeLionUser loginUser
    ) {
        applicationCommandService.deleteApplication(appId, loginUser.getUserInfo());
    }

}