package com.snut_likelion.admin.recruitment.service;

import com.snut_likelion.domain.recruitment.entity.Application;
import com.snut_likelion.domain.recruitment.entity.ApplicationStatus;
import com.snut_likelion.domain.recruitment.entity.Recruitment;
import com.snut_likelion.domain.recruitment.entity.RecruitmentType;
import com.snut_likelion.domain.recruitment.exception.ApplicationErrorCode;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.global.error.exception.BadRequestException;
import com.snut_likelion.global.provider.MailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final MailSender mailSender;

    @Async
    public void sendNotification(User user, ApplicationStatus status, Application application) {
        String email = user.getEmail();
        String name = user.getUsername();

        RecruitmentType recruitmentType = RecruitmentType.MEMBER;
        if (application.getDepartmentType() != null) {
            recruitmentType = RecruitmentType.MANAGER;
        }

        switch (status) {
            case PAPER_PASS -> mailSender.sendInterviewScheduledMail(email, name, recruitmentType.getDescription());
            case FINAL_PASS -> mailSender.sendAcceptedMail(
                    email, name, recruitmentType.getDescription(), application.getPart().getDescription());
            case FAILED -> mailSender.sendRejectedMail(
                    email, name, recruitmentType.getDescription(), application.getPart().getDescription());
            default -> throw new BadRequestException(ApplicationErrorCode.INVALID_STATUS_CHANGE);
        }
    }

    @Async
    public void sendRecruitmentStartNotice(String email, Recruitment rec) {
        int generation = rec.getGeneration();
        RecruitmentType recruitmentType = rec.getRecruitmentType();
        LocalDateTime openDate = rec.getOpenDate();
        LocalDateTime closeDate = rec.getCloseDate();

        mailSender.sendRecruitmentStartNotification(email, generation, recruitmentType.getDescription(), recruitmentType.name().toLowerCase(), openDate, closeDate);
    }
}
