package com.snut_likelion.admin.project.controller;

import com.snut_likelion.admin.project.dto.req.AdminCreateRetrospectionRequest;
import com.snut_likelion.admin.project.dto.res.ProjectPageResponse;
import com.snut_likelion.admin.project.service.AdminProjectService;
import com.snut_likelion.domain.project.dto.req.CreateProjectPresignedRequest;
import com.snut_likelion.domain.project.dto.req.UpdateProjectPresignedRequest;
import com.snut_likelion.domain.project.dto.res.RetrospectionResponse;
import com.snut_likelion.global.dto.ApiResponse;
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

    @Operation(summary = "관리자 프로젝트 목록 조회")
    @GetMapping
    public ApiResponse<ProjectPageResponse> getProjectList(
            @RequestParam(value = "generation", required = false) Integer generation,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return ApiResponse.success(
                adminProjectService.getProjectList(generation, page, keyword),
                "프로젝트 리스트 조회 성공"
        );
    }

    @Operation(summary = "관리자 프로젝트 다중 삭제")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProjects(@RequestParam("ids") List<Long> ids) {
        adminProjectService.deleteProjects(ids);
    }

    @Operation(summary = "관리자 프로젝트 생성 (Presigned URL 기반)",
            description = "Presigned URL로 이미지 업로드 완료 후 storedFileName을 전달하여 프로젝트를 생성합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> createProject(@Valid @RequestBody CreateProjectPresignedRequest req) {
        Long projectId = adminProjectService.create(req);
        return ApiResponse.success(projectId, "프로젝트 생성 성공");
    }

    @Operation(summary = "관리자 프로젝트 수정 (Presigned URL 기반)")
    @PatchMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void modifyProject(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody UpdateProjectPresignedRequest req
    ) {
        adminProjectService.modify(projectId, req);
    }

    @Operation(summary = "관리자 프로젝트 이미지 삭제",
            description = "storedFileName(S3 key)으로 프로젝트 이미지를 삭제합니다.")
    @DeleteMapping("/{projectId}/images")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProjectImage(
            @PathVariable("projectId") Long projectId,
            @RequestParam("imageStoredFileName") String imageStoredFileName
    ) {
        adminProjectService.removeImage(projectId, imageStoredFileName);
    }

    @Operation(summary = "관리자 프로젝트 회고 목록 조회")
    @GetMapping("/{projectId}/retrospections")
    public ApiResponse<List<RetrospectionResponse>> getProjectRetrospections(
            @PathVariable("projectId") Long projectId
    ) {
        return ApiResponse.success(
                adminProjectService.getAllRetrospectionsByProjectId(projectId),
                "프로젝트 회고 목록 조회 성공"
        );
    }

    @Operation(summary = "관리자 프로젝트 회고 생성",
            description = "memberId를 지정하여 특정 멤버의 회고를 관리자가 직접 등록합니다.")
    @PostMapping("/{projectId}/retrospections")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RetrospectionResponse> createProjectRetrospection(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody AdminCreateRetrospectionRequest req
    ) {
        return ApiResponse.success(
                adminProjectService.createRetrospection(projectId, req),
                "프로젝트 회고 등록 성공"
        );
    }

    @Operation(summary = "관리자 프로젝트 회고 삭제")
    @DeleteMapping("/{projectId}/retrospections/{retrospectionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProjectRetrospection(
            @PathVariable("projectId") Long projectId,
            @PathVariable("retrospectionId") Long retrospectionId
    ) {
        adminProjectService.removeRetrospection(projectId, retrospectionId);
    }
}
