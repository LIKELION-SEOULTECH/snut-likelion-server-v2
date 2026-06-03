package com.snut_likelion.domain.project.controller;

import com.snut_likelion.domain.project.dto.req.CreateRetrospectionRequest;
import com.snut_likelion.domain.project.dto.res.RetrospectionResponse;
import com.snut_likelion.domain.project.service.ProjectRetrospectionService;
import com.snut_likelion.global.auth.model.SnutLikeLionUser;
import com.snut_likelion.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Project Retrospection", description = "프로젝트 회고 관리 API")
@RestController
@RequestMapping("/api/v1/projects/{projectId}/retrospections")
@RequiredArgsConstructor
public class ProjectRetrospectionController {

    private final ProjectRetrospectionService projectRetrospectionService;

    @Operation(summary = "프로젝트 회고 작성",
            description = "특정 프로젝트에 회고를 작성합니다. 회고 작성 시 프로젝트 참여자로 자동 등록됩니다. " +
                    "일반 사용자는 본인(memberId=본인) 회고만 작성할 수 있으며, 매니저는 타 멤버의 회고도 등록할 수 있습니다.")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER') and @authChecker.isMe(#loginUser.userInfo, #request.memberId)")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RetrospectionResponse> createRetrospection(
            @Parameter(hidden = true) @AuthenticationPrincipal SnutLikeLionUser loginUser,
            @Parameter(description = "프로젝트 ID") @PathVariable("projectId") Long projectId,
            @Valid @RequestBody CreateRetrospectionRequest request
    ) {
        return ApiResponse.success(
                projectRetrospectionService.create(projectId, request.getMemberId(), request),
                "프로젝트 회고 작성 성공"
        );
    }

    @Operation(summary = "프로젝트 회고 목록 조회", description = "특정 프로젝트에 작성된 모든 회고 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<List<RetrospectionResponse>> getProjectRetrospections(
            @Parameter(description = "프로젝트 ID") @PathVariable("projectId") Long projectId
    ) {
        return ApiResponse.success(
                projectRetrospectionService.getAllByProjectId(projectId),
                "프로젝트 회고 목록 조회 성공"
        );
    }

    @Operation(summary = "프로젝트 회고 삭제", description = "특정 프로젝트의 회고를 삭제합니다. (본인 프로젝트 여부 확인)")
    @DeleteMapping("/{retrospectionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@authChecker.isMyProject(#loginUser.userInfo, #projectId)")
    public void deleteProjectRetrospection(
            @Parameter(hidden = true) @AuthenticationPrincipal SnutLikeLionUser loginUser,
            @Parameter(description = "프로젝트 ID") @PathVariable("projectId") Long projectId,
            @Parameter(description = "회고 ID") @PathVariable("retrospectionId") Long retrospectionId
    ) {
        projectRetrospectionService.remove(projectId, retrospectionId);
    }
}