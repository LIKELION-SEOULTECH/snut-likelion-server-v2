package com.snut_likelion.domain.auth.service;

import com.snut_likelion.domain.auth.dto.ChangePasswordRequest;
import com.snut_likelion.domain.auth.dto.RegisterReq;
import com.snut_likelion.domain.auth.dto.ResetPasswordRequest;
import com.snut_likelion.domain.auth.entity.CertificationToken;
import com.snut_likelion.domain.auth.exception.AuthErrorCode;
import com.snut_likelion.domain.auth.repository.CertificationTokenRepository;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.domain.user.exception.UserErrorCode;
import com.snut_likelion.domain.user.repository.UserRepository;
import com.snut_likelion.global.error.exception.BadRequestException;
import com.snut_likelion.global.error.exception.ExistingResourceException;
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

    @Test
    void register_success() {
        // given
        RegisterReq req = RegisterReq.builder()
                .email("new@example.com")
                .username("newuser")
                .password("pass123!")
                .confirmPassword("pass123!")
                .phoneNumber("01012345678")
                .isEmailVerified(true)
                .build();

        when(userRepository.existsByEmailOrUsernameOrPhoneNumber(any(), any(), any())).thenReturn(false);
        when(passwordEncoder.encode("pass123!")).thenReturn("encoded_pass123!");

        // when
        authService.register(req);

        // then
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_existingUser_throws() {
        // given
        RegisterReq req = RegisterReq.builder()
                .email(email)
                .username("tester")
                .password("pass123!")
                .confirmPassword("pass123!")
                .phoneNumber("01012345678")
                .isEmailVerified(true)
                .build();

        when(userRepository.existsByEmailOrUsernameOrPhoneNumber(any(), any(), any())).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(ExistingResourceException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_success() {
        // given
        ChangePasswordRequest req = ChangePasswordRequest.builder()
                .oldPassword("oldPwd123!")
                .newPassword("newPwd456!")
                .confirmPassword("newPwd456!")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPwd456!")).thenReturn("encoded_newPwd456!");

        // when
        authService.changePassword(1L, req);

        // then
        assertThat(user.getPassword()).isEqualTo("encoded_newPwd456!");
    }

    @Test
    void changePassword_userNotFound_throws() {
        // given
        ChangePasswordRequest req = ChangePasswordRequest.builder()
                .oldPassword("oldPwd123!")
                .newPassword("newPwd456!")
                .confirmPassword("newPwd456!")
                .build();

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> authService.changePassword(99L, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(UserErrorCode.NOT_FOUND.getMessage());
    }
}
