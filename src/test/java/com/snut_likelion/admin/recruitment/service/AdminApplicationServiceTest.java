package com.snut_likelion.admin.recruitment.service;

import com.snut_likelion.admin.recruitment.dto.req.ApplicationListStatus;
import com.snut_likelion.admin.recruitment.dto.req.ChangeApplicationStatusParameter;
import com.snut_likelion.admin.recruitment.dto.req.ChangeApplicationStatusRequest;
import com.snut_likelion.admin.recruitment.dto.res.ApplicationPageResponse;
import com.snut_likelion.admin.recruitment.infra.ApplicationQueryRepository;
import com.snut_likelion.domain.recruitment.entity.*;
import com.snut_likelion.domain.recruitment.infra.ApplicationRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import com.snut_likelion.domain.user.entity.LionInfo;
import com.snut_likelion.domain.user.entity.Part;
import com.snut_likelion.domain.user.entity.Role;
import com.snut_likelion.domain.user.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;

import com.snut_likelion.domain.recruitment.dto.res.ApplicationDetailsResponse;
import com.snut_likelion.global.error.exception.NotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminApplicationServiceTest {

    @Mock
    ApplicationRepository applicationRepository;

    @Mock
    ApplicationQueryRepository applicationQueryRepository;

    @Mock
    NotificationService notificationService;

    @InjectMocks
    AdminApplicationService service;

    Long recId = 1L, userId = 1L, appId = 1L;
    Recruitment recruitment;
    User user;

    @BeforeEach
    void setup() {
        TransactionSynchronizationManager.initSynchronization();
        recruitment = Recruitment.builder()
                .id(recId)
                .generation(13)
                .recruitmentType(RecruitmentType.MEMBER)
                .openDate(LocalDateTime.of(2025, 6, 22, 0, 0))
                .closeDate(LocalDateTime.of(2025, 6, 29, 23, 59))
                .build();
        user = User.builder()
                .id(userId)
                .username("tester")
                .email("test@example.com")
                .build();
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clearSynchronization();
    }

    @Test
    void updateApplicationStatus_success_and_notify_PAPER_PASS() {
        // given
        Application app1 = Application.builder()
                .id(1L)
                .status(ApplicationStatus.SUBMITTED)
                .part(Part.AI)
                .departmentType(null)
                .build();

        Application app2 = Application.builder()
                .id(2L)
                .status(ApplicationStatus.SUBMITTED)
                .part(Part.BACKEND)
                .departmentType(null)
                .build();

        app1.setUser(user);
        User user2 = User.builder().id(2L).build();
        app2.setUser(user2);
        when(applicationRepository.findAllByStatus(eq(ApplicationStatus.SUBMITTED), anyLong()))
                .thenReturn(List.of(app1, app2));

        // when
        service.updateApplicationStatus(
                ChangeApplicationStatusParameter.PAPER_PASS,
                new ChangeApplicationStatusRequest(recId, List.of(1L))
        );

        // simulate transaction commit
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);

        // then
        assertAll(
                () -> assertThat(app1.getStatus()).isEqualTo(ApplicationStatus.PAPER_PASS),
                () -> verify(notificationService).sendNotification(
                        eq("test@example.com"), eq("tester"),
                        eq(ApplicationStatus.PAPER_PASS),
                        eq(RecruitmentType.MEMBER.getDescription()),
                        eq(Part.AI.getDescription())),
                () -> assertThat(app2.getStatus()).isEqualTo(ApplicationStatus.FAILED),
                () -> verify(notificationService).sendNotification(
                        eq(user2.getEmail()), eq(user2.getUsername()),
                        eq(ApplicationStatus.FAILED),
                        eq(RecruitmentType.MEMBER.getDescription()),
                        eq(Part.BACKEND.getDescription()))
        );
    }

    @Test
    void updateApplicationStatus_success_and_notify_FINAL_PASS() {
        // given
        Application app = Application.builder()
                .id(1L)
                .status(ApplicationStatus.PAPER_PASS)
                .part(Part.AI)
                .departmentType(DepartmentType.OPERATION)
                .build();
        app.setUser(user);
        app.setRecruitment(recruitment);

        Application app2 = Application.builder()
                .id(2L)
                .status(ApplicationStatus.PAPER_PASS)
                .part(Part.BACKEND)
                .departmentType(DepartmentType.OPERATION)
                .build();
        User user2 = User.builder().id(2L).build();
        app2.setUser(user2);
        app2.setRecruitment(recruitment);

        when(applicationRepository.findAllByStatus(eq(ApplicationStatus.PAPER_PASS), anyLong()))
                .thenReturn(List.of(app, app2));

        // when
        service.updateApplicationStatus(
                ChangeApplicationStatusParameter.FINAL_PASS,
                new ChangeApplicationStatusRequest(recId, List.of(1L))
        );

        // simulate transaction commit
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);

        // then
        LionInfo lionInfo = user.getLionInfos().get(0);
        assertAll(
                () -> assertThat(app.getStatus()).isEqualTo(ApplicationStatus.FINAL_PASS),
                () -> assertThat(user.getLionInfos()).hasSize(1),
                () -> assertThat(lionInfo.getRole()).isEqualTo(Role.ROLE_MANAGER),
                () -> assertThat(lionInfo.getPart()).isEqualTo(Part.AI),
                () -> assertThat(lionInfo.getDepartmentType()).isEqualTo(DepartmentType.OPERATION),
                () -> verify(notificationService).sendNotification(
                        eq("test@example.com"), eq("tester"),
                        eq(ApplicationStatus.FINAL_PASS),
                        eq(RecruitmentType.MANAGER.getDescription()),
                        eq(Part.AI.getDescription())),
                () -> assertThat(app2.getStatus()).isEqualTo(ApplicationStatus.FAILED),
                () -> verify(notificationService).sendNotification(
                        eq(user2.getEmail()), eq(user2.getUsername()),
                        eq(ApplicationStatus.FAILED),
                        eq(RecruitmentType.MANAGER.getDescription()),
                        eq(Part.BACKEND.getDescription()))
        );
    }

    @Test
    void getApplicationDetails_성공_기수와_무관하게_조회() {
        // given
        Application app = Application.builder()
                .id(appId)
                .status(ApplicationStatus.SUBMITTED)
                .part(Part.BACKEND)
                .departmentType(null)
                .build();
        app.setUser(user);

        when(applicationRepository.findWithDetailsById(appId))
                .thenReturn(Optional.of(app));

        // when
        ApplicationDetailsResponse response = service.getApplicationDetails(appId);

        // then
        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.getPart()).isEqualTo(Part.BACKEND.name())
        );
    }

    @Test
    void getApplicationDetails_존재하지_않는_id_예외발생() {
        // given
        when(applicationRepository.findWithDetailsById(appId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.getApplicationDetails(appId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getApplicationsByRecruitmentId_returnsPassCounts() {
        // given
        ApplicationPageResponse.ApplicationListResponse appResponse =
                ApplicationPageResponse.ApplicationListResponse.builder()
                        .id(1L)
                        .username("tester")
                        .part(Part.BACKEND)
                        .departmentType(null)
                        .status(ApplicationStatus.SUBMITTED)
                        .submittedAt(LocalDateTime.now())
                        .build();

        PageImpl<ApplicationPageResponse.ApplicationListResponse> pageResult =
                new PageImpl<>(List.of(appResponse), PageRequest.of(0, 8), 1);

        when(applicationQueryRepository.getApplicationList(eq(recId), any(), any(), any()))
                .thenReturn(pageResult);
        when(applicationQueryRepository.countByRecruitmentIdAndStatus(recId, ApplicationStatus.PAPER_PASS))
                .thenReturn(15L);
        when(applicationQueryRepository.countByRecruitmentIdAndStatus(recId, ApplicationStatus.FINAL_PASS))
                .thenReturn(10L);

        // when
        ApplicationPageResponse response = service.getApplicationsByRecruitmentId(
                recId, null, 0, ApplicationListStatus.SUBMITTED);

        // then
        assertAll(
                () -> assertThat(response.getPaperPassCount()).isEqualTo(15L),
                () -> assertThat(response.getFinalPassCount()).isEqualTo(10L),
                () -> assertThat(response.getContent()).hasSize(1),
                () -> assertThat(response.getTotalElements()).isEqualTo(1)
        );
    }
}