package com.snut_likelion.admin.project.controller;

import com.snut_likelion.admin.project.dto.response.ProjectPageResponse;
import com.snut_likelion.admin.project.service.AdminProjectService;
import com.snut_likelion.domain.project.dto.request.CreateProjectRequest;
import com.snut_likelion.domain.project.dto.request.UpdateProjectRequest;
import com.snut_likelion.domain.project.dto.response.RetrospectionResponse;
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

@Tag(name = "Admin Project", description = "관리자용 프로젝트 관리 API")
@RestController
@RequestMapping("/api/v1/admin/projects")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_MANAGER')")
public class AdminProjectController {

    private final AdminProjectService adminProjectService;

    @Operation(summary = "관리자 프로젝트 목록 조회", description = "기수 필터링 및 키워드 검색을 포함한 전체 프로젝트 목록을 조회합니다. (MANAGER 권한 필요)")
    @GetMapping
    public ApiResponse<ProjectPageResponse> getProjectList(
            @Parameter(description = "조회할 기수") @RequestParam(value = "generation", required = false) Integer generation,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "검색 키워드 (프로젝트 명 등)") @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return ApiResponse.success(
                adminProjectService.getProjectList(generation, page, keyword),
                "프로젝트 리스트 조회 성공"
        );
    }

    @Operation(summary = "관리자 프로젝트 다중 삭제", description = "여러 개의 프로젝트를 한 번에 삭제합니다.")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProjects(
            @Parameter(description = "삭제할 프로젝트 ID 리스트 (예: 1,2,3)") @RequestParam("ids") List<Long> ids
    ) {
        adminProjectService.deleteProjects(ids);
    }

    @Operation(summary = "관리자 프로젝트 생성", description = "관리자 권한으로 새로운 프로젝트를 생성합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Object> createProject(
            @ModelAttribute @Valid CreateProjectRequest req
    ) {
        adminProjectService.create(req);
        return ApiResponse.success("프로젝트 생성 성공");
    }

    @Operation(summary = "관리자 프로젝트 수정", description = "특정 프로젝트의 정보를 관리자 권한으로 수정합니다.")
    @PatchMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void modifyProject(
            @Parameter(description = "수정할 프로젝트 ID") @PathVariable("projectId") Long projectId,
            @ModelAttribute("updateProjectRequest") @Valid UpdateProjectRequest req
    ) {
        adminProjectService.modify(projectId, req);
    }

    @Operation(summary = "관리자 프로젝트 이미지 삭제", description = "프로젝트에 등록된 특정 이미지를 삭제합니다.")
    @DeleteMapping("/{projectId}/images")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProjectImage(
            @Parameter(description = "프로젝트 ID") @PathVariable("projectId") Long projectId,
            @Parameter(description = "삭제할 이미지의 S3 URL") @RequestParam("imageUrl") String imageUrl
    ) {
        adminProjectService.removeImage(projectId, imageUrl);
    }

    @Operation(summary = "관리자 프로젝트 회고 목록 조회", description = "특정 프로젝트의 모든 회고 목록을 관리자 권한으로 조회합니다.")
    @GetMapping("/{projectId}/retrospections")
    public ApiResponse<List<RetrospectionResponse>> getProjectRetrospections(
            @Parameter(description = "프로젝트 ID") @PathVariable("projectId") Long projectId
    ) {
        return ApiResponse.success(
                adminProjectService.getAllRetrospectionsByProjectId(projectId),
                "프로젝트 회고 목록 조회 성공"
        );
    }

    @Operation(summary = "관리자 프로젝트 회고 삭제", description = "관리자 권한으로 특정 프로젝트의 회고를 삭제합니다.")
    @DeleteMapping("/{projectId}/retrospections/{retrospectionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProjectRetrospection(
            @Parameter(description = "프로젝트 ID") @PathVariable("projectId") Long projectId,
            @Parameter(description = "삭제할 회고 ID") @PathVariable("retrospectionId") Long retrospectionId
    ) {
        adminProjectService.removeRetrospection(projectId, retrospectionId);
    }
}