package com.snut_likelion.domain.project.controller;

import com.snut_likelion.domain.project.dto.request.CreateProjectRequest;
import com.snut_likelion.domain.project.dto.request.UpdateProjectRequest;
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

    @Operation(summary = "프로젝트 생성", description = "새로운 프로젝트를 생성합니다. (USER 권한 필요)")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Object> createProject(
            @ModelAttribute @Valid CreateProjectRequest req
    ) {
        projectCommandService.create(req);
        return ApiResponse.success("프로젝트 생성 성공");
    }

    @Operation(summary = "프로젝트 전체 조회", description = "기수(generation)나 카테고리별로 프로젝트 목록을 필터링하여 조회합니다.")
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

    @Operation(summary = "프로젝트 상세 조회", description = "프로젝트 ID를 통해 상세 정보를 조회합니다.")
    @GetMapping("/{projectId}")
    public ApiResponse<ProjectDetailResponse> getProject(
            @PathVariable("projectId") Long projectId
    ) {
        return ApiResponse.success(projectQueryService.getProjectById(projectId), "프로젝트 상세 조회 성공");
    }

    @Operation(summary = "프로젝트 수정", description = "자신의 프로젝트 정보를 수정합니다.")
    @PatchMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@authChecker.isMyProject(#loginUser.userInfo, #projectId)")
    public void modifyProject(
            @Parameter(hidden = true) @AuthenticationPrincipal SnutLikeLionUser loginUser,
            @PathVariable("projectId") Long projectId,
            @ModelAttribute("updateProjectRequest") @Valid UpdateProjectRequest req
    ) {
        projectCommandService.modify(projectId, req);
    }

    @Operation(summary = "프로젝트 삭제", description = "자신의 프로젝트를 삭제합니다.")
    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@authChecker.isMyProject(#loginUser.userInfo, #projectId)")
    public void deleteProject(
            @Parameter(hidden = true) @AuthenticationPrincipal SnutLikeLionUser loginUser,
            @PathVariable("projectId") Long projectId
    ) {
        projectCommandService.remove(projectId);
    }

    @Operation(summary = "프로젝트 이미지 삭제", description = "프로젝트에 포함된 특정 이미지를 삭제합니다.")
    @DeleteMapping("/{projectId}/images")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@authChecker.isMyProject(#loginUser.userInfo, #projectId)")
    public void deleteProjectImage(
            @Parameter(hidden = true) @AuthenticationPrincipal SnutLikeLionUser loginUser,
            @PathVariable("projectId") Long projectId,
            @RequestParam("imageUrl") String imageUrl
    ) {
        projectCommandService.removeImage(projectId, imageUrl);
    }

}