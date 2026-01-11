package com.snut_likelion.domain.project.controller;

import com.snut_likelion.domain.project.dto.response.RetrospectionResponse;
import com.snut_likelion.domain.project.service.ProjectRetrospectionService;
import com.snut_likelion.global.auth.model.SnutLikeLionUser;
import com.snut_likelion.global.dto.ApiResponse;
// Swagger 관련 import
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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