package com.snut_likelion.admin.recruitment.service;

import com.snut_likelion.admin.recruitment.dto.request.ApplicationListStatus;
import com.snut_likelion.admin.recruitment.dto.request.ChangeApplicationStatusParameter;
import com.snut_likelion.admin.recruitment.dto.request.ChangeApplicationStatusRequest;
import com.snut_likelion.admin.recruitment.dto.response.ApplicationPageResponse;
import com.snut_likelion.admin.recruitment.infra.ApplicationQueryRepository;
import com.snut_likelion.domain.recruitment.entity.*;
import com.snut_likelion.domain.recruitment.infra.ApplicationRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import com.snut_likelion.domain.user.entity.LionInfo;
import com.snut_likelion.domain.user.entity.Part;
import com.snut_likelion.domain.user.entity.Role;
import com.snut_likelion.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyInt;
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
        when(applicationRepository.findAllByStatus(eq(ApplicationStatus.SUBMITTED), anyInt()))
                .thenReturn(List.of(app1, app2));

        // when
        service.updateApplicationStatus(
                ChangeApplicationStatusParameter.PAPER_PASS,
                new ChangeApplicationStatusRequest(List.of(1L))
        );

        // then
        assertAll(
                () -> assertThat(app1.getStatus()).isEqualTo(ApplicationStatus.PAPER_PASS),
                () -> verify(notificationService).sendNotification(user, ApplicationStatus.PAPER_PASS, app1),
                () -> assertThat(app2.getStatus()).isEqualTo(ApplicationStatus.FAILED),
                () -> verify(notificationService).sendNotification(user2, ApplicationStatus.FAILED, app2)
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

        Application app2 = Application.builder()
                .id(2L)
                .status(ApplicationStatus.PAPER_PASS)
                .part(Part.BACKEND)
                .departmentType(DepartmentType.OPERATION)
                .build();
        User user2 = User.builder().id(2L).build();
        app2.setUser(user2);

        when(applicationRepository.findAllByStatus(eq(ApplicationStatus.PAPER_PASS), anyInt()))
                .thenReturn(List.of(app, app2));

        // when
        service.updateApplicationStatus(
                ChangeApplicationStatusParameter.FINAL_PASS,
                new ChangeApplicationStatusRequest(List.of(1L))
        );

        // then
        LionInfo lionInfo = user.getLionInfos().get(0);
        assertAll(
                () -> assertThat(app.getStatus()).isEqualTo(ApplicationStatus.FINAL_PASS),
                () -> assertThat(user.getLionInfos()).hasSize(1),
                () -> assertThat(lionInfo.getRole()).isEqualTo(Role.ROLE_MANAGER),
                () -> assertThat(lionInfo.getPart()).isEqualTo(Part.AI),
                () -> assertThat(lionInfo.getDepartmentType()).isEqualTo(DepartmentType.OPERATION),
                () -> verify(notificationService).sendNotification(user, ApplicationStatus.FINAL_PASS, app),
                () -> assertThat(app2.getStatus()).isEqualTo(ApplicationStatus.FAILED),
                () -> verify(notificationService).sendNotification(user2, ApplicationStatus.FAILED, app2)
        );
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