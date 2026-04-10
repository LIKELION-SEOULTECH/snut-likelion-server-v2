# SNUT LikeLion Server v2 — Claude 컨벤션 인덱스

이 폴더는 Claude와의 협업 시 참조할 코드 컨벤션 및 도메인 문서 모음입니다.

## 컨벤션 문서

| 문서 | 내용 |
|------|------|
| [skills.md](conventions/skills.md) | 기술 스택, 버전, 금지 사항, 테스트 스택 |
| [code.md](conventions/code.md) | 패키지 구조, Entity/Repository/Service/Controller/DTO 작성 규칙 |
| [test.md](conventions/test.md) | Mockito 단위 테스트, @DataJpaTest, WireMock 작성 규칙 |
| [workflow.md](conventions/workflow.md) | 브랜치 전략, 커밋 컨벤션, 개발 순서, 로컬 설정 |

## 도메인 문서

| 문서 | 내용 |
|------|------|
| [domains/auth.md](domains/auth.md) | JWT 인증, 비밀번호 재설정 (인증 코드 방식) |
| [domains/recruitment.md](domains/recruitment.md) | 모집 공고, 지원서, 알림 구독 (기수별 분리) |
| [domains/file.md](domains/file.md) | S3 Presigned URL 4단계 업로드 플로우 |

## 빠른 참조

- **새 API 추가**: `code.md` → Controller / Service / DTO 섹션
- **테스트 작성**: `test.md` → 서비스 단위 테스트 섹션
- **커밋·PR**: `workflow.md` → 커밋 메시지 컨벤션
- **환경 변수 추가**: `CLAUDE.md` (루트) → 필수 환경 변수 목록
