package com.snut_likelion.domain.blog.controller;

import com.snut_likelion.domain.blog.dto.request.CreateBlogRequest;
import com.snut_likelion.domain.blog.dto.request.UpdateBlogRequest;
import com.snut_likelion.domain.blog.dto.response.BlogDetailResponse;
import com.snut_likelion.domain.blog.dto.response.BlogSummaryPageResponse;
import com.snut_likelion.domain.blog.entity.Category;
import com.snut_likelion.domain.blog.service.BlogCommandService;
import com.snut_likelion.domain.blog.service.BlogQueryService;
import com.snut_likelion.global.auth.model.SnutLikeLionUser;
import com.snut_likelion.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Blog", description = "블로그 게시글 관리 API")
@RestController
@RequestMapping("/api/v1/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final BlogCommandService commandService;
    private final BlogQueryService queryService;

    // 게시글(PUBLISHED) 목록
    @Operation(summary = "게시글 목록 조회", description = "카테고리별 게시글 목록을 페이징하여 조회합니다.")
    @GetMapping
    public ApiResponse<BlogSummaryPageResponse> getPostList(
            @RequestParam Category category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        return ApiResponse.success(queryService.getPostList(category, page, size, keyword));
    }

    // 단건 조회
    @Operation(summary = "게시글 상세 조회", description = "게시글 ID를 이용해 상세 내용을 조회합니다.")
    @GetMapping("/{blogId}")
    public ApiResponse<BlogDetailResponse> getPostDetail(@PathVariable("blogId") Long blogId) {
        return ApiResponse.success(queryService.getPostDetail(blogId));
    }

    // 게시글 작성(PUBLISHED)
    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다. submit 여부에 따라 임시저장/발행이 결정됩니다.")
    @PostMapping
    public ApiResponse<Long> createPost(
            @AuthenticationPrincipal SnutLikeLionUser user,
            @RequestParam(value = "submit") boolean submit,
            @RequestBody @Valid CreateBlogRequest req
    ) {
        Long id = commandService.createPost(req, user.getUserInfo(), submit);
        return ApiResponse.success(id);
    }

    // 게시글 수정
    @Operation(summary = "게시글 수정", description = "기존 게시글의 내용을 수정합니다.")
    @PatchMapping(value = "/{blogId}")
    public void updatePost(
            @AuthenticationPrincipal SnutLikeLionUser user,
            @PathVariable("blogId") Long blogId,
            @RequestParam(value = "submit") boolean submit,
            @RequestBody @Valid UpdateBlogRequest req
    ) {
        commandService.updatePost(blogId, req, user.getUserInfo(), submit);
    }

    // 게시글 삭제
    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @DeleteMapping("/{blogId}")
    public void deletePost(
            @AuthenticationPrincipal SnutLikeLionUser user,
            @PathVariable("blogId") Long blogId
    ) {
        commandService.deletePost(blogId, user.getUserInfo());
    }

    // 내가 쓴 글(PUBLISHED) 목록
    @Operation(summary = "내가 쓴 글 목록 조회", description = "로그인한 사용자가 작성한 게시글 목록을 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<BlogSummaryPageResponse> getMyPosts(
            @AuthenticationPrincipal SnutLikeLionUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(
                queryService.getMyPosts(user.getUserInfo(), page, size)
        );
    }

    // 내 임시저장 불러오기
    @Operation(summary = "내 임시저장 글 불러오기", description = "로그인한 사용자의 임시저장된 글을 불러옵니다.")
    @GetMapping(value = "/drafts/me")
    public ApiResponse<BlogDetailResponse> getMyDraft(
            @AuthenticationPrincipal SnutLikeLionUser user
    ) {
        return ApiResponse.success(
                queryService.loadDraft(user.getUserInfo()));
    }

    // 임시저장 버리기
    @Operation(summary = "임시저장 글 삭제", description = "임시저장된 글을 버립니다.")
    @DeleteMapping("/drafts/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void discardDraft(@AuthenticationPrincipal SnutLikeLionUser user) {
        commandService.discardDraft(user.getUserInfo());
    }
}
