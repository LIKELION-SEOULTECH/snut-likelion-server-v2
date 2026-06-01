# _workspace/ 공유 프로토콜

모든 에이전트가 파일을 통해 데이터를 주고받는 공유 작업 디렉토리 규칙.

## 파일 구조 및 소유권

| 파일 | 생성 에이전트 | 소비 에이전트 | 내용 |
|------|------------|------------|------|
| `_workspace/01_analyst_plan.md` | feature-analyst | domain-implementer | 구현 계획서 |
| `_workspace/02_implementation_summary.md` | domain-implementer | test-writer, code-reviewer (병렬) | 구현 완료 요약 |
| `_workspace/03_test_summary.md` | test-writer | 오케스트레이터 (Phase 4) | 테스트 결과 요약 |
| `_workspace/04_review_report.md` | code-reviewer | 오케스트레이터 (Phase 4) | 리뷰 보고서 |

## 원칙

- 자신이 **생성**하는 파일만 쓴다 — 다른 에이전트의 산출물을 덮어쓰지 않는다
- 자신이 **소비**하는 파일은 존재 여부를 먼저 확인하고, 없으면 오케스트레이터에 보고한다
- `_workspace/`는 작업 완료 후에도 보존한다 (재실행·감사 기준점)

## 재실행 시 네이밍 규칙

병렬로 여러 기능을 동시 구현하는 경우 파일명에 기능 식별자를 붙인다:

```
_workspace/01_analyst_plan_{feature}.md
_workspace/02_implementation_summary_{feature}.md
```

예시:
```
_workspace/01_analyst_plan_comment.md
_workspace/01_analyst_plan_like.md
```
