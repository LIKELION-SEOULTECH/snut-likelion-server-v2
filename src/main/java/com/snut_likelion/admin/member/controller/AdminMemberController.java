package com.snut_likelion.admin.member.controller;

import com.snut_likelion.admin.member.dto.request.UpdateMemberRequest;
import com.snut_likelion.admin.member.dto.response.MemberPageResponse;
import com.snut_likelion.admin.member.service.AdminMemberService;
import com.snut_likelion.domain.user.entity.Part;
import com.snut_likelion.domain.user.entity.Role;
import com.snut_likelion.global.dto.ApiResponse;
// Swagger 관련 import
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Member", description = "관리자용 멤버 관리 API")
@RestController
@RequestMapping("/api/v1/admin/members")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_MANAGER')")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    @Operation(summary = "관리자 멤버 목록 조회", description = "기수, 파트, 역할별 필터링을 포함하여 전체 멤버 목록을 조회합니다. (MANAGER 권한 필요)")
    @GetMapping
    public ApiResponse<MemberPageResponse> getMemberList(
            @Parameter(description = "조회할 기수") @RequestParam(value = "generation", required = false) Integer generation,
            @Parameter(description = "활동 파트 (PM, DESIGN, FRONTEND, BACKEND)") @RequestParam(value = "part", required = false) Part part,
            @Parameter(description = "권한 역할 (ROLE_USER, ROLE_MANAGER 등)") @RequestParam(value = "role", required = false) Role role,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "검색 키워드 (이름 등)") @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return ApiResponse.success(
                adminMemberService.getMemberList(generation, part, role, page, keyword),
                "멤버 리스트 조회 성공"
        );
    }

    @Operation(summary = "관리자 멤버 정보 수정", description = "특정 멤버의 기수별 활동 정보를 수정합니다.")
    @PutMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMember(
            @Parameter(description = "멤버 ID") @PathVariable Long memberId,
            @Parameter(description = "수정할 활동 정보의 기수") @RequestParam("generation") int generation,
            @RequestBody UpdateMemberRequest req
    ) {
        adminMemberService.updateMember(memberId, generation, req);
    }

    @Operation(summary = "관리자 멤버 활동 정보 삭제", description = "특정 멤버의 특정 기수 활동 정보(사자 정보)를 삭제합니다.")
    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLionInfo(
            @Parameter(description = "멤버 ID") @PathVariable("memberId") Long memberId,
            @Parameter(description = "삭제할 활동 정보의 기수") @RequestParam("generation") int generation
    ) {
        adminMemberService.deleteMember(memberId, generation);
    }

}