package com.snut_likelion.domain.recruitment.service;

import com.snut_likelion.domain.recruitment.entity.RecruitmentSubscription;
import com.snut_likelion.domain.recruitment.entity.SubscriptionType;
import com.snut_likelion.domain.recruitment.exception.RecruitmentErrorCode;
import com.snut_likelion.domain.recruitment.infra.RecruitmentSubscriptionRepository;
import com.snut_likelion.global.error.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    RecruitmentSubscriptionRepository subscriptionRepository;

    @InjectMocks
    SubscriptionService subscriptionService;

    // ───────────────────────────────────────────────
    // 정상 케이스
    // ───────────────────────────────────────────────

    @Test
    void register_success() {
        // given
        String email = "user@seoultech.ac.kr";
        SubscriptionType type = SubscriptionType.MEMBER;
        int generation = 14;

        when(subscriptionRepository.existsByEmailAndSubscriptionTypeAndGeneration(email, type, generation))
                .thenReturn(false);

        // when
        subscriptionService.register(email, type, generation);

        // then
        ArgumentCaptor<RecruitmentSubscription> captor = ArgumentCaptor.forClass(RecruitmentSubscription.class);
        verify(subscriptionRepository).save(captor.capture());
        RecruitmentSubscription saved = captor.getValue();

        assertAll(
                () -> assertThat(saved.getEmail()).isEqualTo(email),
                () -> assertThat(saved.getSubscriptionType()).isEqualTo(type),
                () -> assertThat(saved.getGeneration()).isEqualTo(generation)
        );
    }

    @Test
    void register_manager_success() {
        // given
        String email = "manager@seoultech.ac.kr";
        SubscriptionType type = SubscriptionType.MANAGER;
        int generation = 14;

        when(subscriptionRepository.existsByEmailAndSubscriptionTypeAndGeneration(email, type, generation))
                .thenReturn(false);

        // when
        subscriptionService.register(email, type, generation);

        // then
        ArgumentCaptor<RecruitmentSubscription> captor = ArgumentCaptor.forClass(RecruitmentSubscription.class);
        verify(subscriptionRepository).save(captor.capture());
        RecruitmentSubscription saved = captor.getValue();

        assertAll(
                () -> assertThat(saved.getSubscriptionType()).isEqualTo(SubscriptionType.MANAGER),
                () -> assertThat(saved.getGeneration()).isEqualTo(14)
        );
    }

    // ───────────────────────────────────────────────
    // 중복 등록
    // ───────────────────────────────────────────────

    @Test
    void register_duplicate_sameCombination_throws() {
        // given: 동일한 이메일+타입+기수 조합 중복 등록 시도
        String email = "user@seoultech.ac.kr";
        SubscriptionType type = SubscriptionType.MEMBER;
        int generation = 14;

        when(subscriptionRepository.existsByEmailAndSubscriptionTypeAndGeneration(email, type, generation))
                .thenReturn(true);

        // when / then
        assertThatThrownBy(() -> subscriptionService.register(email, type, generation))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(RecruitmentErrorCode.ALREADY_ENROLLED_EMAIL.getMessage());

        verify(subscriptionRepository, never()).save(any());
    }

    // ───────────────────────────────────────────────
    // 파티션: 기수가 다르면 동일 이메일+타입이어도 별도 등록 가능
    // ───────────────────────────────────────────────

    @Test
    void register_sameEmailAndType_differentGeneration_success() {
        // given: 13기에는 이미 구독했지만 14기는 새로 구독하는 경우
        String email = "user@seoultech.ac.kr";
        SubscriptionType type = SubscriptionType.MEMBER;

        when(subscriptionRepository.existsByEmailAndSubscriptionTypeAndGeneration(email, type, 14))
                .thenReturn(false);

        // when
        subscriptionService.register(email, type, 14);

        // then: 14기 구독이 정상 저장됨
        ArgumentCaptor<RecruitmentSubscription> captor = ArgumentCaptor.forClass(RecruitmentSubscription.class);
        verify(subscriptionRepository).save(captor.capture());
        assertThat(captor.getValue().getGeneration()).isEqualTo(14);
    }

    // ───────────────────────────────────────────────
    // 파티션: 타입이 다르면 동일 이메일+기수이어도 별도 등록 가능
    // ───────────────────────────────────────────────

    @Test
    void register_sameEmailAndGeneration_differentType_success() {
        // given: 같은 기수지만 MEMBER와 MANAGER를 각각 구독하는 경우
        String email = "user@seoultech.ac.kr";
        int generation = 14;

        when(subscriptionRepository.existsByEmailAndSubscriptionTypeAndGeneration(email, SubscriptionType.MANAGER, generation))
                .thenReturn(false);

        // when
        subscriptionService.register(email, SubscriptionType.MANAGER, generation);

        // then
        ArgumentCaptor<RecruitmentSubscription> captor = ArgumentCaptor.forClass(RecruitmentSubscription.class);
        verify(subscriptionRepository).save(captor.capture());
        assertAll(
                () -> assertThat(captor.getValue().getSubscriptionType()).isEqualTo(SubscriptionType.MANAGER),
                () -> assertThat(captor.getValue().getGeneration()).isEqualTo(14)
        );
    }
}
