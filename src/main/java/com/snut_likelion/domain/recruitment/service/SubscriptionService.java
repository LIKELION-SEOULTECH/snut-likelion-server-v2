package com.snut_likelion.domain.recruitment.service;

import com.snut_likelion.domain.recruitment.entity.RecruitmentSubscription;
import com.snut_likelion.domain.recruitment.entity.SubscriptionType;
import com.snut_likelion.domain.recruitment.exception.RecruitmentErrorCode;
import com.snut_likelion.domain.recruitment.infra.RecruitmentSubscriptionRepository;
import com.snut_likelion.global.error.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final RecruitmentSubscriptionRepository recruitmentSubscriptionRepository;

    @Transactional
    public void register(String email, SubscriptionType subscriptionType, int generation) {
        if (recruitmentSubscriptionRepository.existsByEmailAndSubscriptionTypeAndGeneration(email, subscriptionType, generation)) {
            throw new BadRequestException(RecruitmentErrorCode.ALREADY_ENROLLED_EMAIL);
        }

        recruitmentSubscriptionRepository.save(RecruitmentSubscription.of(email, subscriptionType, generation));
    }
}
