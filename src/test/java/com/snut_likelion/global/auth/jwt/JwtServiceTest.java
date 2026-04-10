package com.snut_likelion.global.auth.jwt;

import com.snut_likelion.domain.auth.entity.RefreshToken;
import com.snut_likelion.domain.auth.exception.AuthErrorCode;
import com.snut_likelion.domain.auth.repository.RefreshTokenRepository;
import com.snut_likelion.domain.user.entity.User;
import com.snut_likelion.domain.user.exception.UserErrorCode;
import com.snut_likelion.domain.user.repository.UserRepository;
import com.snut_likelion.global.auth.dto.TokenDto;
import com.snut_likelion.global.auth.model.SnutLikeLionUser;
import com.snut_likelion.global.auth.model.UserInfo;
import com.snut_likelion.global.error.exception.NotFoundException;
import com.snut_likelion.global.error.exception.UnauthorizedException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock JwtProvider jwtProvider;
    @Mock UserRepository userRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    JwtService jwtService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(jwtService, "activeProfile", "dev");
        ReflectionTestUtils.setField(jwtService, "currentGeneration", 14);
    }

    // ── getTokenFromBearer ───────────────────────────────────────────────────

    @Test
    void getTokenFromBearer_validBearerToken_returnsToken() {
        String result = jwtService.getTokenFromBearer("Bearer mytoken123");
        assertThat(result).isEqualTo("mytoken123");
    }

    @Test
    void getTokenFromBearer_noBearerPrefix_returnsNull() {
        String result = jwtService.getTokenFromBearer("mytoken123");
        assertThat(result).isNull();
    }

    @Test
    void getTokenFromBearer_nullInput_returnsNull() {
        String result = jwtService.getTokenFromBearer(null);
        assertThat(result).isNull();
    }

    @Test
    void getTokenFromBearer_emptyInput_returnsNull() {
        String result = jwtService.getTokenFromBearer("");
        assertThat(result).isNull();
    }

    // ── validate ────────────────────────────────────────────────────────────

    @Test
    void validate_validToken_doesNotThrow() {
        // getPayload returns Claims (not void) — mock returns null by default (no exception = valid)
        jwtService.validate("validtoken");
        verify(jwtProvider).getPayload("validtoken");
    }

    @Test
    void validate_malformedToken_throwsUnauthorized() {
        doThrow(new MalformedJwtException("bad")).when(jwtProvider).getPayload("badtoken");

        assertThatThrownBy(() -> jwtService.validate("badtoken"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void validate_expiredToken_throwsUnauthorized() {
        doThrow(new ExpiredJwtException(null, null, "expired"))
                .when(jwtProvider).getPayload("expiredtoken");

        assertThatThrownBy(() -> jwtService.validate("expiredtoken"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void validate_unsupportedToken_throwsUnauthorized() {
        doThrow(new UnsupportedJwtException("unsupported"))
                .when(jwtProvider).getPayload("unsupportedtoken");

        assertThatThrownBy(() -> jwtService.validate("unsupportedtoken"))
                .isInstanceOf(UnauthorizedException.class);
    }

    // ── logout ───────────────────────────────────────────────────────────────

    @Test
    void logout_validToken_deletesRefreshTokens() {
        when(jwtProvider.getEmail("mytoken")).thenReturn("user@test.com");

        jwtService.logout("Bearer mytoken");

        verify(refreshTokenRepository).deleteAllByEmail("user@test.com");
    }

    // ── doTokenGenerationProcess ─────────────────────────────────────────────

    @Test
    void doTokenGenerationProcess_existingToken_updatesPayload() {
        User user = User.builder().id(1L).email("test@test.com").username("tester").build();
        UserInfo userInfo = UserInfo.from(user, 14);
        SnutLikeLionUser principal = SnutLikeLionUser.from(userInfo);

        TokenDto tokenDto = TokenDto.builder()
                .email("test@test.com")
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .build();

        RefreshToken existingToken = RefreshToken.builder()
                .payload("old-refresh-token")
                .email("test@test.com")
                .build();

        when(jwtProvider.createJwt(principal)).thenReturn(tokenDto);
        when(refreshTokenRepository.findByEmail("test@test.com")).thenReturn(Optional.of(existingToken));

        TokenDto result = jwtService.doTokenGenerationProcess(principal);

        assertThat(result.getAccessToken()).isEqualTo("new-access-token");
        assertThat(existingToken.getPayload()).isEqualTo("new-refresh-token");
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void doTokenGenerationProcess_noExistingToken_savesNewToken() {
        User user = User.builder().id(1L).email("test@test.com").username("tester").build();
        UserInfo userInfo = UserInfo.from(user, 14);
        SnutLikeLionUser principal = SnutLikeLionUser.from(userInfo);

        TokenDto tokenDto = TokenDto.builder()
                .email("test@test.com")
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        when(jwtProvider.createJwt(principal)).thenReturn(tokenDto);
        when(refreshTokenRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        TokenDto result = jwtService.doTokenGenerationProcess(principal);

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    // ── tokenRefresh ─────────────────────────────────────────────────────────

    @Test
    void tokenRefresh_notFoundRefreshToken_throws() {
        // getPayload does not throw (returns null by default) → token valid
        when(refreshTokenRepository.findByPayload("validtoken")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jwtService.tokenRefresh("Bearer validtoken"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(AuthErrorCode.NOT_FOUND_REFRESH_TOKEN.getMessage());
    }

    @Test
    void tokenRefresh_userNotFound_throws() {
        RefreshToken refreshToken = RefreshToken.builder()
                .payload("validtoken")
                .email("ghost@test.com")
                .build();

        // getPayload does not throw (returns null by default) → token valid
        when(refreshTokenRepository.findByPayload("validtoken")).thenReturn(Optional.of(refreshToken));
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jwtService.tokenRefresh("Bearer validtoken"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(UserErrorCode.NOT_FOUND.getMessage());
    }

    // ── setCookie ─────────────────────────────────────────────────────────────

    @Test
    void setCookie_devProfile_setsNonSecureCookies() {
        TokenDto tokenDto = TokenDto.builder()
                .email("test@test.com")
                .accessToken("access")
                .refreshToken("refresh")
                .build();

        when(jwtProvider.getAccessTokenExpiration()).thenReturn(3600000L);
        when(jwtProvider.getRefreshTokenExpiration()).thenReturn(86400000L);

        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtService.setCookie(tokenDto, response);

        assertThat(response.getHeaders("Set-Cookie")).hasSize(2);
    }
}
