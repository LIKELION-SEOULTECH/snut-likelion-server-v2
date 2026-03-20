package com.snut_likelion.admin.recruitment.service;

import com.snut_likelion.domain.recruitment.entity.ApplicationStatus;
import com.snut_likelion.domain.recruitment.entity.Recruitment;
import com.snut_likelion.domain.recruitment.entity.RecruitmentType;
import com.snut_likelion.domain.recruitment.exception.ApplicationErrorCode;
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
    public void sendNotification(String email, String name, ApplicationStatus status,
                                 String recruitmentTypeDesc, String partDesc) {
        switch (status) {
            case PAPER_PASS -> mailSender.sendInterviewScheduledMail(email, name, recruitmentTypeDesc);
            case FINAL_PASS -> mailSender.sendAcceptedMail(email, name, recruitmentTypeDesc, partDesc);
            case FAILED -> mailSender.sendRejectedMail(email, name, recruitmentTypeDesc, partDesc);
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
