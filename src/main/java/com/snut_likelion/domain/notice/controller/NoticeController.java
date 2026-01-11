package com.snut_likelion.domain.notice.controller;

import com.snut_likelion.domain.notice.dto.request.CreateNoticeRequest;
import com.snut_likelion.domain.notice.dto.response.NoticeDetailResponse;
import com.snut_likelion.domain.notice.dto.response.NoticePageResponse;
import com.snut_likelion.domain.notice.dto.request.UpdateNoticeRequest;
import com.snut_likelion.domain.notice.service.NoticeService;
import com.snut_likelion.global.dto.ApiResponse;
// Swagger 관련 import
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notice", description = "공지사항 관리 API")
@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "공지사항 생성", description = "새로운 공지사항을 작성합니다. (MANAGER 권한 필요)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ApiResponse<Long> createNotice(
            @Valid @RequestBody CreateNoticeRequest request) {
        return ApiResponse.success(noticeService.createNotice(request));
    }

    @Operation(summary = "공지사항 수정", description = "기존 공지사항의 내용을 수정합니다. (MANAGER 권한 필요)")
    @PatchMapping("/{noticeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public void updateNotice(
            @PathVariable Long noticeId,
            @Valid @RequestBody UpdateNoticeRequest request) {
        noticeService.updateNotice(noticeId, request);
    }

    @Operation(summary = "공지사항 삭제", description = "공지사항을 삭제합니다. (MANAGER 권한 필요)")
    @DeleteMapping("/{noticeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public void deleteNotice(@PathVariable Long noticeId) {
        noticeService.deleteNotice(noticeId);
    }

    @Operation(summary = "공지사항 목록 조회", description = "공지사항 목록을 페이징하여 조회합니다. 키워드 검색이 가능합니다.")
    @GetMapping
    public ApiResponse<NoticePageResponse> getNoticePage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        return ApiResponse.success(noticeService.getNoticePage(page, size, keyword));
    }

    @Operation(summary = "공지사항 상세 조회", description = "공지사항 ID를 통해 상세 내용을 조회합니다.")
    @GetMapping("/{noticeId}")
    public ApiResponse<NoticeDetailResponse> getNotice(@PathVariable Long noticeId) {
        return ApiResponse.success(noticeService.getNoticeDetail(noticeId));
    }

}