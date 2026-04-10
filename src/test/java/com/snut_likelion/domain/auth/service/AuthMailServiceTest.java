package com.snut_likelion.domain.auth.service;

import com.snut_likelion.domain.auth.entity.CertificationToken;
import com.snut_likelion.domain.auth.repository.CertificationTokenRepository;
import com.snut_likelion.domain.user.exception.UserErrorCode;
import com.snut_likelion.domain.user.repository.UserRepository;
import com.snut_likelion.global.error.exception.NotFoundException;
import com.snut_likelion.global.provider.MailSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthMailServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    CertificationTokenRepository certificationTokenRepository;

    @Mock
    MailSender mailSender;

    @InjectMocks
    AuthMailService authMailService;

    @Test
    void sendCertifyEmail_savesTokenAndSendsMail() {
        String email = "test@example.com";

        authMailService.sendCertifyEmail(email);

        ArgumentCaptor<CertificationToken> captor = ArgumentCaptor.forClass(CertificationToken.class);
        verify(certificationTokenRepository).save(captor.capture());
        CertificationToken saved = captor.getValue();

        assertThat(saved.getEmail()).isEqualTo(email);
        assertThat(saved.getCode()).hasSize(6);
        assertThat(saved.getExpiredAt()).isNotNull();

        verify(mailSender).sendVerificationCode(eq(email), anyString());
    }

    @Test
    void sendFindPasswordEmail_success() {
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        authMailService.sendFindPasswordEmail(email);

        ArgumentCaptor<CertificationToken> captor = ArgumentCaptor.forClass(CertificationToken.class);
        verify(certificationTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getCode()).hasSize(6);

        verify(mailSender).sendPasswordResetCode(eq(email), anyString());
    }

    @Test
    void sendFindPasswordEmail_userNotFound_throws() {
        String email = "none@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        assertThatThrownBy(() -> authMailService.sendFindPasswordEmail(email))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(UserErrorCode.NOT_FOUND.getMessage());

        verify(certificationTokenRepository, never()).save(any());
        verify(mailSender, never()).sendPasswordResetCode(anyString(), anyString());
    }
}
