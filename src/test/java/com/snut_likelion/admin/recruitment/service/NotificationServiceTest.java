package com.snut_likelion.admin.recruitment.service;

import com.snut_likelion.domain.recruitment.entity.ApplicationStatus;
import com.snut_likelion.domain.recruitment.entity.Recruitment;
import com.snut_likelion.domain.recruitment.entity.RecruitmentType;
import com.snut_likelion.domain.recruitment.exception.ApplicationErrorCode;
import com.snut_likelion.global.error.exception.BadRequestException;
import com.snut_likelion.global.provider.MailSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    MailSender mailSender;

    @InjectMocks
    NotificationService notificationService;

    @Test
    void sendNotification_paperPass() {
        notificationService.sendNotification("a@a.com", "kim", ApplicationStatus.PAPER_PASS, "아기사자", "백엔드");
        verify(mailSender).sendInterviewScheduledMail("a@a.com", "kim", "아기사자");
    }

    @Test
    void sendNotification_finalPass() {
        notificationService.sendNotification("a@a.com", "kim", ApplicationStatus.FINAL_PASS, "아기사자", "백엔드");
        verify(mailSender).sendAcceptedMail("a@a.com", "kim", "아기사자", "백엔드");
    }

    @Test
    void sendNotification_failed() {
        notificationService.sendNotification("a@a.com", "kim", ApplicationStatus.FAILED, "아기사자", "백엔드");
        verify(mailSender).sendRejectedMail("a@a.com", "kim", "아기사자", "백엔드");
    }

    @Test
    void sendNotification_invalidStatus_throws() {
        assertThatThrownBy(() ->
                notificationService.sendNotification("a@a.com", "kim", ApplicationStatus.DRAFT, "아기사자", "백엔드"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ApplicationErrorCode.INVALID_STATUS_CHANGE.getMessage());
    }

    @Test
    void sendRecruitmentStartNotice_success() {
        Recruitment rec = Recruitment.builder()
                .generation(14)
                .recruitmentType(RecruitmentType.MEMBER)
                .openDate(LocalDateTime.of(2026, 3, 1, 0, 0))
                .closeDate(LocalDateTime.of(2026, 3, 7, 23, 59))
                .build();

        notificationService.sendRecruitmentStartNotice("a@a.com", rec);

        verify(mailSender).sendRecruitmentStartNotification(
                "a@a.com", 14, "아기사자", "member",
                rec.getOpenDate(), rec.getCloseDate());
    }
}
