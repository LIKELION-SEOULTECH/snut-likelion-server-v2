package com.snut_likelion.domain.recruitment.schedule;

import com.snut_likelion.admin.recruitment.service.NotificationService;
import com.snut_likelion.domain.recruitment.entity.Recruitment;
import com.snut_likelion.domain.recruitment.entity.RecruitmentSubscription;
import com.snut_likelion.domain.recruitment.entity.RecruitmentType;
import com.snut_likelion.domain.recruitment.entity.SubscriptionType;
import com.snut_likelion.domain.recruitment.infra.RecruitmentRepository;
import com.snut_likelion.domain.recruitment.infra.RecruitmentSubscriptionRepository;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecruitmentNotifySchedulerTest {

    @Mock
    RecruitmentRepository recruitmentRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    NotificationService notificationService;
    @Mock
    RecruitmentSubscriptionRepository subscriptionRepository;

    @InjectMocks
    RecruitmentNotifyScheduler scheduler;

    // ───────────────────────────────────────────────
    // 기본 발송 흐름
    // ───────────────────────────────────────────────

    @Test
    void sendNotifications_noTarget_skip() {
        // given: 발송 대상 모집 없음
        when(recruitmentRepository.findAllByOpenDateLessThanEqualAndStartNotifiedFalse(any()))
                .thenReturn(List.of());

        // when
        scheduler.sendRecruitmentStartNotifications();

        // then: 아무것도 호출되지 않음
        verifyNoInteractions(notificationService, subscriptionRepository, userRepository);
    }

    @Test
    void sendNotifications_sendsToAllUsersAndSubscribers() {
        // given
        Recruitment rec = recruitment(14, RecruitmentType.MEMBER);
        User user1 = user("a@test.com");
        User user2 = user("b@test.com");
        RecruitmentSubscription sub = subscription("sub@test.com", SubscriptionType.MEMBER, 14);

        when(recruitmentRepository.findAllByOpenDateLessThanEqualAndStartNotifiedFalse(any()))
                .thenReturn(List.of(rec));
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(subscriptionRepository.findAllBySubscriptionTypeAndGeneration(SubscriptionType.MEMBER, 14))
                .thenReturn(List.of(sub));

        // when
        scheduler.sendRecruitmentStartNotifications();

        // then: 유저 2명 + 구독자 1명 총 3건 발송
        verify(notificationService, times(3)).sendRecruitmentStartNotice(any(), eq(rec));
        verify(notificationService).sendRecruitmentStartNotice("a@test.com", rec);
        verify(notificationService).sendRecruitmentStartNotice("b@test.com", rec);
        verify(notificationService).sendRecruitmentStartNotice("sub@test.com", rec);
    }

    @Test
    void sendNotifications_userEmailEqualsSubscriberEmail_noDuplicate() {
        // given: 유저 이메일과 구독자 이메일이 동일한 경우 중복 제거
        Recruitment rec = recruitment(14, RecruitmentType.MEMBER);
        User user = user("same@test.com");
        RecruitmentSubscription sub = subscription("same@test.com", SubscriptionType.MEMBER, 14);

        when(recruitmentRepository.findAllByOpenDateLessThanEqualAndStartNotifiedFalse(any()))
                .thenReturn(List.of(rec));
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(subscriptionRepository.findAllBySubscriptionTypeAndGeneration(SubscriptionType.MEMBER, 14))
                .thenReturn(List.of(sub));

        // when
        scheduler.sendRecruitmentStartNotifications();

        // then: 중복 없이 1건만 발송
        verify(notificationService, times(1)).sendRecruitmentStartNotice(eq("same@test.com"), eq(rec));
    }

    // ───────────────────────────────────────────────
    // 파티션: 기수별 분리
    // ───────────────────────────────────────────────

    @Test
    void sendNotifications_onlySameGenerationSubscribersReceiveMail() {
        // given: 14기 모집 발생, 13기 구독자는 받으면 안 됨
        Recruitment rec14 = recruitment(14, RecruitmentType.MEMBER);

        when(recruitmentRepository.findAllByOpenDateLessThanEqualAndStartNotifiedFalse(any()))
                .thenReturn(List.of(rec14));
        when(userRepository.findAll()).thenReturn(List.of());
        // 14기 구독자만 반환 (13기 구독자는 이 쿼리에서 애초에 조회되지 않음)
        when(subscriptionRepository.findAllBySubscriptionTypeAndGeneration(SubscriptionType.MEMBER, 14))
                .thenReturn(List.of(subscription("gen14@test.com", SubscriptionType.MEMBER, 14)));

        // when
        scheduler.sendRecruitmentStartNotifications();

        // then: 14기 구독자에게만 발송
        verify(notificationService).sendRecruitmentStartNotice("gen14@test.com", rec14);
        verify(notificationService, never()).sendRecruitmentStartNotice(eq("gen13@test.com"), any());
        // 13기 구독자 조회 자체가 발생하지 않았음을 확인
        verify(subscriptionRepository, never())
                .findAllBySubscriptionTypeAndGeneration(SubscriptionType.MEMBER, 13);
    }

    @Test
    void sendNotifications_multipleRecruitments_eachUsesOwnGeneration() {
        // given: 14기 MEMBER 모집과 15기 MANAGER 모집이 동시에 발생
        Recruitment rec14Member = recruitment(14, RecruitmentType.MEMBER);
        Recruitment rec15Manager = recruitment(15, RecruitmentType.MANAGER);

        RecruitmentSubscription sub14 = subscription("m14@test.com", SubscriptionType.MEMBER, 14);
        RecruitmentSubscription sub15 = subscription("m15@test.com", SubscriptionType.MANAGER, 15);

        when(recruitmentRepository.findAllByOpenDateLessThanEqualAndStartNotifiedFalse(any()))
                .thenReturn(List.of(rec14Member, rec15Manager));
        when(userRepository.findAll()).thenReturn(List.of());
        when(subscriptionRepository.findAllBySubscriptionTypeAndGeneration(SubscriptionType.MEMBER, 14))
                .thenReturn(List.of(sub14));
        when(subscriptionRepository.findAllBySubscriptionTypeAndGeneration(SubscriptionType.MANAGER, 15))
                .thenReturn(List.of(sub15));

        // when
        scheduler.sendRecruitmentStartNotifications();

        // then: 각 기수·타입에 맞는 구독자에게만 발송
        verify(notificationService).sendRecruitmentStartNotice("m14@test.com", rec14Member);
        verify(notificationService).sendRecruitmentStartNotice("m15@test.com", rec15Manager);
        // 교차 발송 없음
        verify(notificationService, never()).sendRecruitmentStartNotice("m14@test.com", rec15Manager);
        verify(notificationService, never()).sendRecruitmentStartNotice("m15@test.com", rec14Member);
    }

    // ───────────────────────────────────────────────
    // 파티션: 모집 타입별 분리
    // ───────────────────────────────────────────────

    @Test
    void sendNotifications_memberRecruitment_queriesMemberSubscribersOnly() {
        // given: MEMBER 모집 → MEMBER 구독자 쿼리만 수행해야 함
        Recruitment rec = recruitment(14, RecruitmentType.MEMBER);

        when(recruitmentRepository.findAllByOpenDateLessThanEqualAndStartNotifiedFalse(any()))
                .thenReturn(List.of(rec));
        when(userRepository.findAll()).thenReturn(List.of());
        when(subscriptionRepository.findAllBySubscriptionTypeAndGeneration(SubscriptionType.MEMBER, 14))
                .thenReturn(List.of());

        // when
        scheduler.sendRecruitmentStartNotifications();

        // then: MEMBER 기수 구독자 쿼리 실행
        verify(subscriptionRepository).findAllBySubscriptionTypeAndGeneration(SubscriptionType.MEMBER, 14);
        // MANAGER 구독자 쿼리는 실행되지 않음
        verify(subscriptionRepository, never())
                .findAllBySubscriptionTypeAndGeneration(eq(SubscriptionType.MANAGER), anyInt());
    }

    @Test
    void sendNotifications_managerRecruitment_queriesManagerSubscribersOnly() {
        // given: MANAGER 모집 → MANAGER 구독자 쿼리만 수행해야 함
        Recruitment rec = recruitment(14, RecruitmentType.MANAGER);

        when(recruitmentRepository.findAllByOpenDateLessThanEqualAndStartNotifiedFalse(any()))
                .thenReturn(List.of(rec));
        when(userRepository.findAll()).thenReturn(List.of());
        when(subscriptionRepository.findAllBySubscriptionTypeAndGeneration(SubscriptionType.MANAGER, 14))
                .thenReturn(List.of());

        // when
        scheduler.sendRecruitmentStartNotifications();

        // then
        verify(subscriptionRepository).findAllBySubscriptionTypeAndGeneration(SubscriptionType.MANAGER, 14);
        verify(subscriptionRepository, never())
                .findAllBySubscriptionTypeAndGeneration(eq(SubscriptionType.MEMBER), anyInt());
    }

    // ───────────────────────────────────────────────
    // 사후 처리
    // ───────────────────────────────────────────────

    @Test
    void sendNotifications_afterSend_subscribersDeletedAndFlagSet() {
        // given
        Recruitment rec = recruitment(14, RecruitmentType.MEMBER);
        RecruitmentSubscription sub = subscription("sub@test.com", SubscriptionType.MEMBER, 14);

        when(recruitmentRepository.findAllByOpenDateLessThanEqualAndStartNotifiedFalse(any()))
                .thenReturn(List.of(rec));
        when(userRepository.findAll()).thenReturn(List.of());
        when(subscriptionRepository.findAllBySubscriptionTypeAndGeneration(SubscriptionType.MEMBER, 14))
                .thenReturn(List.of(sub));

        // when
        scheduler.sendRecruitmentStartNotifications();

        // then: 구독 레코드 삭제
        verify(subscriptionRepository).deleteAll(List.of(sub));
        // startNotified 플래그 설정
        assertThat(rec.isStartNotified()).isTrue();
    }

    @Test
    void sendNotifications_noSubscribersNoUsers_onlyFlagSet() {
        // given: 구독자도 유저도 없는 경우
        Recruitment rec = recruitment(14, RecruitmentType.MEMBER);

        when(recruitmentRepository.findAllByOpenDateLessThanEqualAndStartNotifiedFalse(any()))
                .thenReturn(List.of(rec));
        when(userRepository.findAll()).thenReturn(List.of());
        when(subscriptionRepository.findAllBySubscriptionTypeAndGeneration(SubscriptionType.MEMBER, 14))
                .thenReturn(List.of());

        // when
        scheduler.sendRecruitmentStartNotifications();

        // then: 발송은 없고 플래그만 설정
        verify(notificationService, never()).sendRecruitmentStartNotice(any(), any());
        assertThat(rec.isStartNotified()).isTrue();
    }

    // ───────────────────────────────────────────────
    // 헬퍼
    // ───────────────────────────────────────────────

    private Recruitment recruitment(int generation, RecruitmentType type) {
        return Recruitment.builder()
                .id((long) generation)
                .generation(generation)
                .recruitmentType(type)
                .openDate(LocalDateTime.now().minusMinutes(1))
                .closeDate(LocalDateTime.now().plusDays(7))
                .build();
    }

    private User user(String email) {
        return User.builder().email(email).build();
    }

    private RecruitmentSubscription subscription(String email, SubscriptionType type, int generation) {
        return RecruitmentSubscription.of(email, type, generation);
    }
}
