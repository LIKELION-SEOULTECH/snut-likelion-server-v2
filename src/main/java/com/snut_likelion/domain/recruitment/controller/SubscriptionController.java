package com.snut_likelion.domain.recruitment.controller;

import com.snut_likelion.domain.recruitment.entity.SubscriptionType;
import com.snut_likelion.domain.recruitment.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Subscription", description = "모집 알림 구독 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(
            summary = "모집 알림 구독 등록",
            description = """
                    비회원이 특정 기수 모집 시작 알림을 이메일로 받기 위해 구독을 등록합니다.

                    - `type=MEMBER`: 아기사자 모집 알림
                    - `type=MANAGER`: 운영진 모집 알림
                    - `generation`: 알림을 받을 모집 기수 (예: 14기 모집이면 14)

                    모집 공고 `openDate`가 되면 해당 기수·타입에 맞게 등록된 이메일로 알림 메일이 발송됩니다.
                    알림 발송 후 구독 정보는 자동으로 삭제됩니다.

                    같은 이메일·타입·기수 조합으로 중복 등록 시 400 에러가 반환됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "구독 등록 성공"),
            @ApiResponse(responseCode = "400", description = "이미 등록된 이메일 (ALREADY_ENROLLED_EMAIL)"),
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void subscribeMember(
            @Parameter(description = "알림을 받을 이메일 주소", example = "example@seoultech.ac.kr")
            @RequestParam("email") String email,
            @Parameter(description = "구독할 모집 타입 (`MEMBER`: 아기사자, `MANAGER`: 운영진)", example = "MEMBER")
            @RequestParam("type") SubscriptionType type,
            @Parameter(description = "알림을 받을 모집 기수", example = "14")
            @RequestParam("generation") int generation
    ) {
        subscriptionService.register(email, type, generation);
    }
}