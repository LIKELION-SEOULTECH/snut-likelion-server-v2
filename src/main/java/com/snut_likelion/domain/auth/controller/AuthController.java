package com.snut_likelion.domain.auth.controller;

import com.snut_likelion.domain.auth.dto.ChangePasswordRequest;
import com.snut_likelion.domain.auth.dto.RegisterReq;
import com.snut_likelion.domain.auth.dto.ResetPasswordRequest;
import com.snut_likelion.domain.auth.service.AuthMailService;
import com.snut_likelion.domain.auth.service.AuthService;
import com.snut_likelion.global.auth.dto.TokenDto;
import com.snut_likelion.global.auth.jwt.JwtService;
import com.snut_likelion.global.auth.model.SnutLikeLionUser;
import com.snut_likelion.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.snut_likelion.global.auth.jwt.JwtProvider.REFRESH_TOKEN_HEADER;

@Tag(name = "Auth", description = "인증 및 계정 관리 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final AuthMailService authMailService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Object> register(@RequestBody @Valid RegisterReq req) {
        authService.register(req);
        return ApiResponse.success("회원가입 성공");
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token을 이용해 새로운 Access Token을 발급합니다.")
    @GetMapping("/refresh")
    public TokenDto refresh(
            HttpServletResponse response,
            @RequestHeader(value = REFRESH_TOKEN_HEADER) String bearerRefreshToken
    ) {
        TokenDto tokenDto = jwtService.tokenRefresh(bearerRefreshToken);
        jwtService.setCookie(tokenDto, response);
        return tokenDto;
    }

    @Operation(summary = "인증 메일 발송", description = "입력한 이메일로 가입 인증 코드를 전송합니다.")
    @PostMapping("/email/send")
    public ApiResponse<Object> sendCertifyEmail(
            @RequestParam("email") String email
    ) {
        authMailService.sendCertifyEmail(email);
        return ApiResponse.success("인증 코드 전송 완료");
    }

    @Operation(summary = "이메일 인증 확인", description = "이메일로 받은 코드가 일치하는지 확인합니다.")
    @PostMapping("/email/certify")
    public ApiResponse<Object> certifyCode(
            @RequestParam("email") String email,
            @RequestParam("code") String code
    ) {
        authService.certifyCode(email, code);
        return ApiResponse.success("이메일 인증 성공");
    }

    @Operation(summary = "비밀번호 찾기 메일 발송", description = "비밀번호 재설정을 위한 메일을 발송합니다.")
    @PostMapping("/password/find")
    public ApiResponse<Object> sendPasswordEmail(
            @RequestParam("email") String email
    ) {
        authMailService.sendFindPasswordEmail(email);
        return ApiResponse.success("비밀번호 찾기 메일 전송 완료");
    }

    @Operation(summary = "비밀번호 재설정", description = "인증 후 비밀번호를 새로운 값으로 초기화합니다.")
    @PatchMapping("/password/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(
            @RequestBody @Valid ResetPasswordRequest req
    ) {
        authService.resetPassword(req);
    }

    @Operation(summary = "비밀번호 변경", description = "로그인 상태에서 기존 비밀번호를 변경합니다.")
    @PatchMapping("/password/change")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal SnutLikeLionUser loginUser,
            @RequestBody @Valid ChangePasswordRequest req
    ) {
        authService.changePassword(loginUser.getId(), req);
    }
}
