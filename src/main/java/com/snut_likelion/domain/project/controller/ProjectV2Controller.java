package com.snut_likelion.domain.project.controller;

import com.snut_likelion.domain.project.service.ProjectCommandService;
import com.snut_likelion.global.auth.model.SnutLikeLionUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Project V2", description = "프로젝트(작품) 관리 API v2")
@RestController
@RequestMapping("/api/v2/projects")
@RequiredArgsConstructor
public class ProjectV2Controller {

    private final ProjectCommandService projectCommandService;

    @Operation(summary = "프로젝트 이미지 삭제(v2)", description = "storedFileName(key)로 프로젝트 이미지를 삭제합니다.")
    @DeleteMapping("/{projectId}/images")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@authChecker.isMyProject(#loginUser.userInfo, #projectId)")
    public void deleteProjectImageV2(
            @Parameter(hidden = true) @AuthenticationPrincipal SnutLikeLionUser loginUser,
            @PathVariable("projectId") Long projectId,
            @RequestParam("imageStoredFileName") String imageStoredFileName
    ) {
        projectCommandService.removeImageByKey(projectId, imageStoredFileName);
    }
}
