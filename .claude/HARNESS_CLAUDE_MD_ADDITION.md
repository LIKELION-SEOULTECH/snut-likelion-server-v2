# CLAUDE.md에 추가할 하네스 포인터 섹션

아래 내용을 CLAUDE.md 파일 맨 끝에 추가하세요.

---

## 하네스: SNUT LikeLion Dev

**목표:** 신기능 개발 요청을 받아 feature-analyst → domain-implementer → test-writer + code-reviewer 팀이 컨벤션에 맞게 구현·테스트·리뷰까지 완료한다.

**트리거:** 기능 추가, API 개발, 도메인 구현, 버그 수정, 테스트 작성, 코드 리뷰 요청 시 `snut-dev-orchestrator` 스킬을 사용하라. 단순 질문·설명은 직접 답변 가능.

**에이전트 팀:**
| 에이전트 | 역할 | 정의 파일 |
|----------|------|---------|
| feature-analyst | 요구사항 분석 + 구현 계획서 작성 | `.claude/agents/feature-analyst.md` |
| domain-implementer | 계층별 코드 구현 | `.claude/agents/domain-implementer.md` |
| test-writer | 서비스 단위 테스트(Mockito) 작성 | `.claude/agents/test-writer.md` |
| code-reviewer | 컨벤션 준수 + 보안/성능 리뷰 | `.claude/agents/code-reviewer.md` |

**참조 스킬:**
| 스킬 | 내용 |
|------|------|
| `snut-dev-orchestrator` | 팀 조율 오케스트레이터 |
| `snut-feature-dev` | 프로젝트 고유 구현 컨벤션 |
| `snut-test-writing` | 프로젝트 고유 테스트 컨벤션 |
| `springboot-patterns` | 범용 Spring Boot 패턴 (ECC) |
| `jpa-patterns` | JPA/QueryDSL 패턴 (ECC) |
| `springboot-security` | Spring Security 패턴 (ECC) |

**변경 이력:**
| 날짜 | 변경 내용 | 대상 | 사유 |
|------|----------|------|------|
| 2026-05-28 | 하네스 초기 구성 | 전체 | 전문 에이전트 팀 체계 구축 |
