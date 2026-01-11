package com.snut_likelion.admin.notice.controller;

import com.snut_likelion.admin.notice.dto.response.NoticePageResponse;
import com.snut_likelion.admin.notice.service.AdminNoticeService;
import com.snut_likelion.domain.notice.dto.request.CreateNoticeRequest;
import com.snut_likelion.domain.notice.dto.request.UpdateNoticeRequest;
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

@Tag(name = "Admin Notice", description = "관리자용 공지사항 관리 API")
@RestController
@RequestMapping("/api/v1/admin/notices")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_MANAGER')")
public class AdminNoticeController {

    private final AdminNoticeService adminNoticeService;

    @Operation(summary = "관리자 공지사항 목록 조회", description = "관리자 권한으로 전체 공지사항 목록을 조회합니다. 키워드 검색이 가능합니다.")
    @GetMapping
    public ApiResponse<NoticePageResponse> getNoticeList(
            @RequestParam(value = "page",  defaultValue = "0")  int page,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return ApiResponse.success(
                adminNoticeService.getNoticeList(page, keyword),
                "공지 리스트 조회 성공"
        );
    }

    @Operation(summary = "관리자 공지사항 생성", description = "새로운 공지사항을 생성합니다. (MANAGER 권한 필요)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Object> createNotice(
            @Valid @RequestBody CreateNoticeRequest req
    ) {
        adminNoticeService.create(req);
        return ApiResponse.success("공지 생성 성공");
    }

    @Operation(summary = "관리자 공지사항 수정", description = "기존 공지사항의 내용을 수정합니다.")
    @PatchMapping("/{noticeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void modifyNotice(
            @Parameter(description = "수정할 공지사항 ID") @PathVariable Long noticeId,
            @Valid @RequestBody UpdateNoticeRequest req
    ) {
        adminNoticeService.modify(noticeId, req);
    }

    @Operation(summary = "공지사항 핀 고정 토글", description = "공지사항의 상단 고정(Pin) 상태를 활성화하거나 해제합니다.")
    @PatchMapping("/{noticeId}/pin")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void togglePin(@Parameter(description = "토글할 공지사항 ID") @PathVariable Long noticeId) {
        adminNoticeService.togglePin(noticeId);
    }

    @Operation(summary = "관리자 공지사항 단건 삭제", description = "특정 공지사항을 삭제합니다.")
    @DeleteMapping("/{noticeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotice(@Parameter(description = "삭제할 공지사항 ID") @PathVariable Long noticeId) {
        adminNoticeService.remove(noticeId);
    }

    @Operation(summary = "관리자 공지사항 다중 삭제", description = "여러 개의 공지사항을 한 번에 삭제합니다.")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotices(
            @Parameter(description = "삭제할 공지사항 ID 리스트 (예: 1,2,3)") @RequestParam("ids") List<Long> ids
    ) {
        adminNoticeService.removeNotices(ids);
    }
}