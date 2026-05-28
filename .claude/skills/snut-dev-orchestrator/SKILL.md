---
name: snut-dev-orchestrator
description: SNUT LikeLion 서버의 기능 개발 요청 시 전문 에이전트 팀을 조율하는 오케스트레이터. "새 기능 추가", "API 만들어줘", "도메인 구현", "테스트 작성해줘", "버그 수정", "코드 리뷰해줘", "엔드포인트 추가" 등 개발 작업 요청 시 반드시 이 스킬을 사용하라. 다시 실행, 재실행, 부분 수정, 이전 결과 업데이트도 포함. 단순 질문이나 설명 요청은 직접 답변 가능.
---

# SNUT LikeLion Dev Orchestrator

SNUT LikeLion 서버(Spring Boot 3.4.5 + Java 17) 기능 개발 워크플로우를 조율한다.

**실행 모드:** 서브 에이전트 파이프라인 + Phase 3 병렬
`feature-analyst → domain-implementer → [test-writer + code-reviewer 병렬]`

---

## Phase 0: 컨텍스트 확인

워크플로우 시작 전 기존 작업 상태를 확인한다.

```
_workspace/ 존재 여부 확인:
  - 미존재            → 초기 실행 (Phase 1부터)
  - 존재 + 부분 수정  → 부분 재실행 (해당 Phase만, 나머지 유지)
  - 존재 + 새 기능    → _workspace/를 _workspace_prev/로 이동 후 초기 실행
```

---

## Phase 1: 요구사항 분석

**실행 모드:** 서브 에이전트 (직렬)

```
Agent(
  subagent_type: "general-purpose",
  model: "opus",
  prompt: """
  당신은 SNUT LikeLion 서버의 feature-analyst 에이전트입니다.
  에이전트 정의: .claude/agents/feature-analyst.md

  다음 기능 요청을 분석하고 _workspace/01_analyst_plan.md를 생성하세요.

  기능 요청: {USER_REQUEST}

  반드시 먼저 읽을 파일:
  - CLAUDE.md (프로젝트 컨벤션 전체)
  - .claude/conventions/code.md (계층 규칙)
  - .claude/domains/*.md 중 관련 도메인 파일

  산출물: _workspace/01_analyst_plan.md
  """
)
```

**산출물 확인:** `_workspace/01_analyst_plan.md` 생성 여부 확인 후 Phase 2 진입.

---

## Phase 2: 구현

**실행 모드:** 서브 에이전트 (직렬)

```
Agent(
  subagent_type: "general-purpose",
  model: "opus",
  prompt: """
  당신은 SNUT LikeLion 서버의 domain-implementer 에이전트입니다.
  에이전트 정의: .claude/agents/domain-implementer.md

  _workspace/01_analyst_plan.md를 읽고 계획대로 구현하세요.

  필수 참조 스킬: snut-feature-dev (.claude/skills/snut-feature-dev/SKILL.md)

  구현 순서: Entity → Repository → Service → Controller → DTO → ErrorCode

  완료 후:
  1. ./gradlew build -x test 실행하여 컴파일 확인
  2. _workspace/02_implementation_summary.md 생성
  """
)
```

**산출물 확인:** `_workspace/02_implementation_summary.md` 생성 + 빌드 성공 여부 확인 후 Phase 3 진입.

---

## Phase 3: 테스트 + 리뷰 (병렬)

**실행 모드:** 서브 에이전트 2개 병렬 (run_in_background)

### test-writer

```
Agent(
  subagent_type: "general-purpose",
  model: "opus",
  run_in_background: true,
  prompt: """
  당신은 SNUT LikeLion 서버의 test-writer 에이전트입니다.
  에이전트 정의: .claude/agents/test-writer.md

  _workspace/02_implementation_summary.md를 읽고 대상 Service를 파악하세요.
  필수 참조 스킬: snut-test-writing (.claude/skills/snut-test-writing/SKILL.md)

  서비스 단위 테스트를 작성하고 ./gradlew test --tests "{TestClass}"로 통과 확인하세요.
  산출물: src/test/java/.../...Test.java + _workspace/03_test_summary.md
  """
)
```

### code-reviewer

```
Agent(
  subagent_type: "general-purpose",
  model: "opus",
  run_in_background: true,
  prompt: """
  당신은 SNUT LikeLion 서버의 code-reviewer 에이전트입니다.
  에이전트 정의: .claude/agents/code-reviewer.md

  _workspace/02_implementation_summary.md를 읽고 구현된 파일들을 검토하세요.
  체크리스트: .claude/agents/code-reviewer.md 참조

  산출물: _workspace/04_review_report.md
  """
)
```

---

## Phase 4: 결과 종합 및 보고

1. `_workspace/04_review_report.md`에서 심각 이슈 확인
   - 심각 이슈 존재 시 → domain-implementer에게 수정 요청 (Phase 2 재실행)
2. 테스트 통과 여부 확인 (`_workspace/03_test_summary.md`)
   - 실패 시 → test-writer에게 수정 요청
3. 최종 결과 사용자에게 보고:
   ```
   ## 구현 완료 보고

   ### 새 파일
   | 경로 | 역할 |

   ### 새 API 엔드포인트
   | HTTP | 경로 | 설명 |

   ### 테스트
   - 작성된 테스트: N개 (성공 M개 / 실패 케이스 K개)
   - 실행 결과: 전체 통과 / 실패 목록

   ### 리뷰 이슈
   - 심각: 없음 / 목록
   - 중요: 목록
   - 경미: 목록

   ### 다음 단계
   - QueryDSL 재생성 필요 여부
   - 수동 SQL 마이그레이션 필요 여부
   ```

---

## 에러 핸들링

| 상황 | 처리 |
|------|------|
| Phase 1: 계획서 미생성 | feature-analyst 재호출 (가정 명시하고 진행) |
| Phase 2: 빌드 실패 | domain-implementer에게 오류 메시지 전달 + 1회 재시도 |
| Phase 3: 테스트 실패 | test-writer에게 실패 로그 전달 + 수정 요청 |
| Phase 3: 심각 리뷰 이슈 | domain-implementer 수정 → code-reviewer 재리뷰 |
| 요구사항 불명확 | 가정을 명시하고 계속 진행 (중단하지 않음) |

---

## 데이터 전달 프로토콜

| 경로 | 생성자 | 소비자 |
|------|--------|--------|
| `_workspace/01_analyst_plan.md` | feature-analyst | domain-implementer |
| `_workspace/02_implementation_summary.md` | domain-implementer | test-writer, code-reviewer |
| `_workspace/03_test_summary.md` | test-writer | 오케스트레이터 |
| `_workspace/04_review_report.md` | code-reviewer | 오케스트레이터 |

`_workspace/`는 작업 후 보존한다 (사후 감사·재실행 기준점).

---

## 테스트 시나리오

### 정상 흐름: 신규 기능
1. 입력: "공지사항에 댓글 기능 추가해줘"
2. Phase 0: `_workspace/` 없음 → 초기 실행
3. Phase 1: `NoticeComment` 엔티티, 서비스, API 계획서 생성
4. Phase 2: Entity/Repo/Service/Controller/DTO + ErrorCode 구현, 빌드 통과
5. Phase 3: `NoticeCommentServiceTest` 작성 + 코드 리뷰 병렬 실행
6. Phase 4: 결과 보고

### 에러 흐름: 부분 수정
1. 입력: "방금 구현한 댓글 기능에서 삭제 API만 수정해줘"
2. Phase 0: `_workspace/` 존재 + 부분 수정 → `02_implementation_summary.md` 참조
3. 삭제 Service + Controller만 수정
4. test-writer가 해당 테스트만 업데이트
