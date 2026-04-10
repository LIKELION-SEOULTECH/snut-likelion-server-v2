package com.snut_likelion.domain.auth.service;

import com.snut_likelion.domain.auth.dto.ResetPasswordRequest;
import com.snut_likelion.domain.auth.entity.CertificationToken;
import com.snut_likelion.domain.auth.exception.AuthErrorCode;
import com.snut_likelion.domain.auth.repository.CertificationTokenRepository;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.domain.user.exception.UserErrorCode;
import com.snut_likelion.domain.user.repository.UserRepository;
import com.snut_likelion.global.error.exception.BadRequestException;
import com.snut_likelion.global.error.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    CertificationTokenRepository certificationTokenRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    AuthService authService;

    String email = "test@example.com";
    String code = "ABC123";
    User user;
    CertificationToken validToken;

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(1L)
                .email(email)
                .username("tester")
                .build();

        validToken = CertificationToken.builder()
                .id(1L)
                .email(email)
                .code(code)
                .expiredAt(LocalDateTime.now().plusMinutes(10))
                .build();
    }

    @Test
    void resetPassword_success() {
        // given
        ResetPasswordRequest req = ResetPasswordRequest.builder()
                .email(email)
                .code(code)
                .newPassword("newPwd123!")
                .newPasswordConfirm("newPwd123!")
                .build();

        when(certificationTokenRepository.findByEmail(email)).thenReturn(Optional.of(validToken));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPwd123!")).thenReturn("encoded_newPwd123!");

        // when
        authService.resetPassword(req);

        // then
        verify(certificationTokenRepository).delete(validToken);
        assertThat(user.getPassword()).isEqualTo("encoded_newPwd123!");
    }

    @Test
    void resetPassword_invalidCode_throws() {
        // given
        ResetPasswordRequest req = ResetPasswordRequest.builder()
                .email(email)
                .code("WRONG1")
                .newPassword("newPwd123!")
                .newPasswordConfirm("newPwd123!")
                .build();

        when(certificationTokenRepository.findByEmail(email)).thenReturn(Optional.of(validToken));

        // when / then
        assertThatThrownBy(() -> authService.resetPassword(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(AuthErrorCode.INVALID_CERTIFICATION_TOKEN.getMessage());

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void resetPassword_expiredToken_throws() {
        // given
        CertificationToken expiredToken = CertificationToken.builder()
                .id(2L)
                .email(email)
                .code(code)
                .expiredAt(LocalDateTime.now().minusMinutes(1))
                .build();

        ResetPasswordRequest req = ResetPasswordRequest.builder()
                .email(email)
                .code(code)
                .newPassword("newPwd123!")
                .newPasswordConfirm("newPwd123!")
                .build();

        when(certificationTokenRepository.findByEmail(email)).thenReturn(Optional.of(expiredToken));

        // when / then
        assertThatThrownBy(() -> authService.resetPassword(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(AuthErrorCode.INVALID_CERTIFICATION_TOKEN.getMessage());

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void resetPassword_noToken_throws() {
        // given
        ResetPasswordRequest req = ResetPasswordRequest.builder()
                .email(email)
                .code(code)
                .newPassword("newPwd123!")
                .newPasswordConfirm("newPwd123!")
                .build();

        when(certificationTokenRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> authService.resetPassword(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(AuthErrorCode.INVALID_CERTIFICATION_TOKEN.getMessage());
    }

    @Test
    void resetPassword_userNotFound_throws() {
        // given
        ResetPasswordRequest req = ResetPasswordRequest.builder()
                .email(email)
                .code(code)
                .newPassword("newPwd123!")
                .newPasswordConfirm("newPwd123!")
                .build();

        when(certificationTokenRepository.findByEmail(email)).thenReturn(Optional.of(validToken));
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> authService.resetPassword(req))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(UserErrorCode.NOT_FOUND.getMessage());

        verify(certificationTokenRepository).delete(validToken);
    }

    @Test
    void certifyCode_success() {
        // given
        when(certificationTokenRepository.findByEmail(email)).thenReturn(Optional.of(validToken));

        // when
        authService.certifyCode(email, code);

        // then
        verify(certificationTokenRepository).delete(validToken);
    }

    @Test
    void certifyCode_invalidCode_throws() {
        // given
        when(certificationTokenRepository.findByEmail(email)).thenReturn(Optional.of(validToken));

        // when / then
        assertThatThrownBy(() -> authService.certifyCode(email, "WRONG1"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(AuthErrorCode.INVALID_CERTIFICATION_TOKEN.getMessage());

        verify(certificationTokenRepository, never()).delete(any());
    }

    @Test
    void certifyCode_expiredToken_throws() {
        // given
        CertificationToken expiredToken = CertificationToken.builder()
                .id(2L)
                .email(email)
                .code(code)
                .expiredAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(certificationTokenRepository.findByEmail(email)).thenReturn(Optional.of(expiredToken));

        // when / then
        assertThatThrownBy(() -> authService.certifyCode(email, code))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(AuthErrorCode.INVALID_CERTIFICATION_TOKEN.getMessage());

        verify(certificationTokenRepository, never()).delete(any());
    }
}
