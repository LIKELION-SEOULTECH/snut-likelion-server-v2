package com.snut_likelion.admin.blog.controller;

import com.snut_likelion.admin.blog.dto.response.BlogPageResponse;
import com.snut_likelion.admin.blog.service.AdminBlogService;
import com.snut_likelion.domain.blog.dto.request.CreateBlogRequest;
import com.snut_likelion.domain.blog.dto.request.UpdateBlogRequest;
import com.snut_likelion.global.auth.model.SnutLikeLionUser;
import com.snut_likelion.global.dto.ApiResponse;
// Swagger 관련 import
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

@Tag(name = "Admin Blog", description = "관리자용 블로그 관리 API")
@RestController
@RequestMapping("/api/v1/admin/blogs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_MANAGER')")
public class AdminBlogController {

    private final AdminBlogService blogService;

    @Operation(summary = "관리자 블로그 목록 조회", description = "카테고리 및 키워드 검색을 포함한 블로그 전체 목록을 관리자 권한으로 조회합니다.")
    @GetMapping
    public ApiResponse<BlogPageResponse> getBlogList(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return ApiResponse.success(
                blogService.getBlogList(category, page, keyword),
                "블로그 리스트 조회 성공"
        );
    }

    @Operation(summary = "관리자 블로그 업로드", description = "관리자 권한으로 새로운 블로그 포스트를 작성합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Object> createBlog(
            @Parameter(hidden = true) @AuthenticationPrincipal SnutLikeLionUser user,
            @RequestBody @Valid CreateBlogRequest req
    ) {
        blogService.create(req, user.getUserInfo());
        return ApiResponse.success("블로그 업로드 성공");
    }

    @Operation(summary = "관리자 블로그 수정", description = "관리자 권한으로 특정 블로그 포스트를 수정합니다.")
    @PatchMapping("/{blogId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void modifyBlog(
            @Parameter(hidden = true) @AuthenticationPrincipal SnutLikeLionUser user,
            @Parameter(description = "수정할 블로그 ID") @PathVariable Long blogId,
            @RequestBody @Valid UpdateBlogRequest req
    ) {
        blogService.modify(blogId, req, user.getUserInfo());
    }

    @Operation(summary = "관리자 블로그 단건 삭제", description = "관리자 권한으로 특정 블로그 포스트를 삭제합니다.")
    @DeleteMapping("/{blogId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBlog(
            @Parameter(hidden = true) @AuthenticationPrincipal SnutLikeLionUser user,
            @Parameter(description = "삭제할 블로그 ID") @PathVariable Long blogId
    ) {
        blogService.delete(blogId, user.getUserInfo());
    }

    @Operation(summary = "관리자 블로그 다중 삭제", description = "선택한 여러 개의 블로그 포스트를 한 번에 삭제합니다.")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBlogs(
            @Parameter(hidden = true) @AuthenticationPrincipal SnutLikeLionUser user,
            @Parameter(description = "삭제할 블로그 ID 리스트 (예: 1,2,3)") @RequestParam("ids") List<Long> ids
    ) {
        blogService.deleteBlogs(ids, user.getUserInfo());
    }
}