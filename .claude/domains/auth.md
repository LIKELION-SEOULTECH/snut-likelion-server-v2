# 도메인: Auth (인증)

## 개요

JWT Access + Refresh Token 이중 구조 인증.
비밀번호 재설정은 이메일 인증 코드 입력 방식.

---

## JWT 토큰 전략

| 토큰 | 만료 | 저장 위치 |
|------|------|----------|
| Access Token | 짧음 (수십 분) | 쿠키 |
| Refresh Token | 길음 (수일~수주) | 쿠키 + DB (`RefreshToken` 엔티티) |

- `JwtProvider`: 토큰 발급·파싱 (JJWT 0.12.3)
- `JwtService`: 토큰 생성 프로세스, 갱신, 로그아웃, 쿠키 설정
- 환경별 쿠키 설정: `activeProfile=dev` → Secure 속성 없음, `prod` → Secure=true

---

## 인증 주체

```java
// 컨트롤러에서 현재 사용자 주입
@AuthenticationPrincipal SnutLikeLionUser user

// 사용 가능한 필드
user.getId()          // Long
user.getEmail()       // String
user.getGeneration()  // int (현재 기수)
user.getRole()        // Role enum
```

---

## 비밀번호 재설정 플로우

```
[1] POST /api/v1/auth/password/find?email={email}
    → CertificationToken 생성 (UUID 6자리 코드, 10분 유효)
    → Gmail SMTP 비동기 발송 (코드를 메일 본문에 직접 노출)

[2] PATCH /api/v1/auth/password/reset
    Body: { email, code, newPassword, newPasswordConfirm }
    → CertificationToken 조회 (email 기준)
    → 코드 일치 + 만료 시간 검증
    → BCrypt 해시로 비밀번호 변경
    → CertificationToken 삭제
```

---

## 주요 예외

| 상황 | 예외 |
|------|------|
| 토큰 형식 오류 | `UnauthorizedException(AuthErrorCode.INVALID_TOKEN)` |
| 토큰 만료 | `UnauthorizedException(AuthErrorCode.EXPIRED_TOKEN)` |
| 리프레시 토큰 없음 | `NotFoundException(AuthErrorCode.NOT_FOUND_REFRESH_TOKEN)` |
| 인증 코드 불일치·만료 | `BadRequestException(AuthErrorCode.INVALID_CERTIFICATION_TOKEN)` |
| 이미 존재하는 이메일 | `ExistingResourceException(UserErrorCode.ALREADY_EXISTS)` |

---

## 관련 패키지

```
domain/auth/
  entity/   CertificationToken, RefreshToken
  service/  AuthService, AuthMailService
  repository/ RefreshTokenRepository, CertificationTokenRepository

global/auth/
  jwt/      JwtProvider, JwtService, JwtAuthenticationFilter
  model/    SnutLikeLionUser, UserInfo
  dto/      TokenDto

infra/mail/
  MailSender (interface)
  GmailSender (구현체 — Gmail SMTP, @Async)
```
