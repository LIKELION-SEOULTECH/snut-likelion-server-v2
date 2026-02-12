package com.snut_likelion.global.provider;

import com.snut_likelion.domain.recruitment.entity.RecruitmentType;

import java.time.LocalDateTime;

public interface MailSender {

    void sendVerificationCode(String toEmail, String code);

    void sendChangePasswordLinkMail(String toEmail, String code);

    void sendInterviewScheduledMail(String toEmail, String username, String recruitmentType);

    void sendAcceptedMail(String toEmail, String username, String recruitmentType, String part);

    void sendRejectedMail(String toEmail, String username, String recruitmentType, String part);

    void sendRecruitmentStartNotification(String toEmail, int generation, String recruitmentType, String recruitmentPath, LocalDateTime openDate, LocalDateTime closeDate);
}
