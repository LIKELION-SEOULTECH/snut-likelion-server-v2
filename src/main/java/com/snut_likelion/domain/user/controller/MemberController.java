package com.snut_likelion.domain.user.controller;

import com.snut_likelion.domain.user.dto.req.UpdateProfileRequest;
import com.snut_likelion.domain.user.dto.res.LionInfoDetailsResponse;
import com.snut_likelion.domain.user.dto.res.MemberDetailResponse;
import com.snut_likelion.domain.user.dto.res.MemberResponse;
import com.snut_likelion.domain.user.dto.res.MemberSearchResponse;
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

    @Operation(
            summary = "프로필 수정",
            description = """
                    회원의 프로필 정보를 수정합니다. (본인 권한 필요)

                    모든 필드는 선택사항입니다. 수정하고 싶은 필드만 포함하여 요청하세요.

                    ---

                    **프로필 이미지 변경 시 사전 업로드 필요 (3단계)**

                    이미지를 변경하려면 먼저 아래 순서로 파일을 S3에 업로드한 뒤,
                    응답으로 받은 `storedFileName` 을 `profileImage` 필드에 담아 요청해야 합니다.

                    1. `POST /api/v1/admin/files/presigned-url`
                       ```json
                       {
                         "originalFileName": "profile.png",
                         "contentType": "image/png",
                         "contentLength": 102400,
                         "uploadCategory": "MEMBER",
                         "fileStorageType": "IMAGE"
                       }
                       ```
                       → 응답: `uploadUrl`, `storedFileName` ("images/members/uuid-profile.png"), `headers`

                    2. 응답의 `uploadUrl` 로 파일을 **직접 PUT 업로드**
                       - 응답의 `headers` 값을 요청 헤더에 포함 (Content-Type 등)
                       - URL 유효시간: **10분**

                    3. `POST /api/v1/admin/files/upload-complete`
                       ```json
                       {
                         "storedFileName": "images/members/uuid-profile.png",
                         "originalFileName": "profile.png",
                         "contentType": "image/png",
                         "contentLength": 102400,
                         "uploadCategory": "MEMBER"
                       }
                       ```

                    이후 아래 요청 바디의 `profileImage` 에 `storedFileName` 값을 넣어 호출하세요.
                    이미지 변경이 없을 경우 `profileImage` 필드를 생략하거나 null로 전달하세요.

                    ---

                    **portfolioLinks.name 허용 값:** GITHUB, NOTION, BEHANCE, BLOG, INSTAGRAM, OTHER
                    """
    )
    @PatchMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_USER')")
    public void updateProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal SnutLikeLionUser loginUser,
            @Parameter(description = "수정할 멤버 ID") @PathVariable("memberId") Long memberId,
            @RequestBody UpdateProfileRequest req
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