package com.snut_likelion.domain.user.controller;

import com.snut_likelion.domain.user.dto.request.UpdateProfileRequest;
import com.snut_likelion.domain.user.dto.response.LionInfoDetailsResponse;
import com.snut_likelion.domain.user.dto.response.MemberDetailResponse;
import com.snut_likelion.domain.user.dto.response.MemberResponse;
import com.snut_likelion.domain.user.dto.response.MemberSearchResponse;
import com.snut_likelion.domain.user.service.MemberCommandService;
import com.snut_likelion.domain.user.service.MemberQueryService;
import com.snut_likelion.global.auth.model.SnutLikeLionUser;
import com.snut_likelion.global.dto.ApiResponse;
// Swagger 관련 import
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Member", description = "회원 프로필 및 멤버 관리 API")
@RequestMapping("/api/v1/members")
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberQueryService memberQueryService;
    private final MemberCommandService memberCommandService;

    @Operation(summary = "멤버 목록 조회", description = "기수 및 운영진 여부를 기준으로 멤버 리스트를 조회합니다.")
    @GetMapping
    public ApiResponse<List<MemberResponse>> getMembersByQuery(
            @RequestParam(name = "generation") int generation,
            @RequestParam(name = "isManager") boolean isManager
    ) {
        return ApiResponse.success(
                memberQueryService.getMembersByQuery(generation, isManager),
                "멤버 리스트 조회 성공"
        );
    }

    @Operation(summary = "멤버 검색", description = "키워드를 통해 멤버를 검색합니다.")
    @GetMapping("/search")
    public ApiResponse<List<MemberSearchResponse>> searchMembers(
            @RequestParam(name = "keyword") String keyword
    ) {
        return ApiResponse.success(
                memberQueryService.searchMembers(keyword),
                "멤버 검색 성공"
        );
    }

    @Operation(summary = "내 정보 상세 조회", description = "로그인한 사용자의 상세 프로필 정보를 조회합니다.")
    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ApiResponse<MemberDetailResponse> getMyDetails(
            @Parameter(hidden = true) @AuthenticationPrincipal SnutLikeLionUser loginUser
    ) {
        return ApiResponse.success(
                memberQueryService.getMyDetails(loginUser.getId()),
                "멤버 상세 정보 조회 성공"
        );
    }

    @Operation(summary = "멤버 상세 조회", description = "멤버 ID를 통해 특정 회원의 상세 프로필 정보를 조회합니다.")
    @GetMapping("/{memberId}")
    public ApiResponse<MemberDetailResponse> getMemberDetails(
            @Parameter(description = "조회할 멤버 ID") @PathVariable("memberId") Long memberId
    ) {
        return ApiResponse.success(memberQueryService.getMemberDetailsById(memberId), "멤버 상세 정보 조회 성공");
    }

    @Operation(summary = "멤버 사자 활동 정보 조회", description = "특정 멤버의 특정 기수 활동 정보(사자 정보)를 조회합니다.")
    @GetMapping("/{memberId}/lion-info")
    public ApiResponse<LionInfoDetailsResponse> getMemberLionInfo(
            @Parameter(description = "멤버 ID") @PathVariable("memberId") Long memberId,
            @Parameter(description = "조회할 활동 기수") @RequestParam(name = "generation") int generation
    ) {
        return ApiResponse.success(
                memberQueryService.getMemberLionInfoByIdAndGeneration(memberId, generation),
                String.format("UserId: %d | %d기 활동 정보 조회 성공", memberId, generation)
        );
    }

    @Operation(summary = "프로필 수정", description = "회원의 프로필 정보를 수정합니다. (본인 권한 필요)")
    @PatchMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_USER')")
    public void updateProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal SnutLikeLionUser loginUser,
            @Parameter(description = "수정할 멤버 ID") @PathVariable("memberId") Long memberId,
            @ModelAttribute("updateProfileRequest") UpdateProfileRequest req
    ) {
        memberCommandService.updateProfile(loginUser.getUserInfo(), memberId, req);
    }

    @Operation(summary = "회원 탈퇴", description = "서비스에서 탈퇴합니다.")
    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdrawMember(
            @Parameter(hidden = true) @AuthenticationPrincipal SnutLikeLionUser loginUser,
            @Parameter(description = "탈퇴할 멤버 ID") @PathVariable("memberId") Long memberId
    ) {
        memberCommandService.withdrawMember(loginUser.getUserInfo(), memberId);
    }
}