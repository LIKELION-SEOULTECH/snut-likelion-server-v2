package com.snut_likelion.domain.project.controller;

import com.snut_likelion.domain.project.dto.request.CreateProjectPresignedRequest;
import com.snut_likelion.domain.project.dto.request.UpdateProjectPresignedRequest;
import com.snut_likelion.domain.project.dto.response.ProjectDetailResponse;
import com.snut_likelion.domain.project.dto.response.ProjectResponse;
import com.snut_likelion.domain.project.entity.ProjectCategory;
import com.snut_likelion.domain.project.service.ProjectCommandService;
import com.snut_likelion.domain.project.service.ProjectQueryService;
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

@Tag(name = "Project", description = "프로젝트(작품) 관리 API")
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectCommandService projectCommandService;
    private final ProjectQueryService projectQueryService;

    @Operation(summary = "프로젝트 전체 조회")
    @GetMapping
    public ApiResponse<List<ProjectResponse>> getAllProjects(
            @RequestParam(required = false) Integer generation,
            @RequestParam(required = false) ProjectCategory category
    ) {
        return ApiResponse.success(
                projectQueryService.getAllProjects(generation, category),
                "프로젝트 전체 조회 성공"
        );
    }

    @Operation(summary = "프로젝트 상세 조회")
    @GetMapping("/{projectId}")
    public ApiResponse<ProjectDetailResponse> getProject(
            @PathVariable("projectId") Long projectId
    ) {
        return ApiResponse.success(
                projectQueryService.getProjectById(projectId),
                "프로젝트 상세 조회 성공"
        );
    }

    @Operation(summary = "프로젝트 생성 (Presigned URL 기반)",
            description = "Presigned URL로 이미지 업로드 완료 후 storedFileName을 전달하여 프로젝트를 생성합니다.")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Object> createProject(
            @Parameter(hidden = true) @AuthenticationPrincipal SnutLikeLionUser loginUser,
            @Valid @RequestBody CreateProjectPresignedRequest request
    ) {
        Long id = projectCommandService.createPresigned(request);
        return ApiResponse.success("프로젝트가 등록되었습니다. projectId=" + id);
    }

    @Operation(summary = "프로젝트 수정 (Presigned URL 기반)")
    @PatchMapping("/{projectId}")
    @PreAuthorize("@authChecker.isMyProject(#loginUser.userInfo, #projectId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void modifyProject(
            @Parameter(hidden = true) @AuthenticationPrincipal SnutLikeLionUser loginUser,
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectPresignedRequest request
    ) {
        projectCommandService.modifyPresigned(projectId, request);
    }

    @Operation(summary = "프로젝트 삭제")
    @DeleteMapping("/{projectId}")
    @PreAuthorize("@authChecker.isMyProject(#loginUser.userInfo, #projectId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(
            @Parameter(hidden = true) @AuthenticationPrincipal SnutLikeLionUser loginUser,
            @PathVariable("projectId") Long projectId
    ) {
        projectCommandService.remove(projectId);
    }

    @Operation(summary = "프로젝트 이미지 삭제",
            description = "storedFileName(S3 key)으로 프로젝트 이미지를 삭제합니다.")
    @DeleteMapping("/{projectId}/images")
    @PreAuthorize("@authChecker.isMyProject(#loginUser.userInfo, #projectId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProjectImage(
            @Parameter(hidden = true) @AuthenticationPrincipal SnutLikeLionUser loginUser,
            @PathVariable("projectId") Long projectId,
            @RequestParam("imageStoredFileName") String imageStoredFileName
    ) {
        projectCommandService.removeImageByKey(projectId, imageStoredFileName);
    }
}