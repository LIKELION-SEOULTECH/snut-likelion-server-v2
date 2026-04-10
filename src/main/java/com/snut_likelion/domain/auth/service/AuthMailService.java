package com.snut_likelion.domain.auth.service;

import com.snut_likelion.domain.auth.entity.CertificationToken;
import com.snut_likelion.domain.auth.repository.CertificationTokenRepository;
import com.snut_likelion.domain.user.exception.UserErrorCode;
import com.snut_likelion.domain.user.repository.UserRepository;
import com.snut_likelion.global.error.exception.NotFoundException;
import com.snut_likelion.global.provider.MailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthMailService {

    private final UserRepository userRepository;
    private final CertificationTokenRepository certificationTokenRepository;
    private final MailSender mailSender;

    @Async
    @Transactional
    public void sendCertifyEmail(String email) {
        String code = this.generateCertificationCode(email);
        mailSender.sendVerificationCode(email, code);
    }

    @Async
    @Transactional
    public void sendFindPasswordEmail(String email) {
        boolean isExists = userRepository.existsByEmail(email);
        if (!isExists) throw new NotFoundException(UserErrorCode.NOT_FOUND);
        String code = this.generateCertificationCode(email);
        mailSender.sendPasswordResetCode(email, code);
    }

    private String generateCertificationCode(String email) {
        String code = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        CertificationToken certificationToken = CertificationToken.builder()
                .email(email)
                .code(code)
                .expiredAt(LocalDateTime.now().plusMinutes(10))
                .build();

        certificationTokenRepository.save(certificationToken);
        return code;
    }
}
