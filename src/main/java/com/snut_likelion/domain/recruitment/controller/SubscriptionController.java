package com.snut_likelion.domain.recruitment.controller;

import com.snut_likelion.domain.recruitment.entity.SubscriptionType;
import com.snut_likelion.domain.recruitment.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Subscription", description = "알림 구독 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "알림 구독 등록", description = "특정 이메일에 대해 모집 알림 등의 구독 정보를 등록합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void subscribeMember(
            @Parameter(description = "구독할 이메일 주소")
            @RequestParam("email") String email,
            @Parameter(description = "구독 타입 (예: RECRUITMENT)")
            @RequestParam("type") SubscriptionType type
    ) {
        subscriptionService.register(email, type);
    }
}