# 프로젝트 공통 규칙

모든 에이전트가 작업 시작 전에 숙지해야 할 SNUT LikeLion 서버 공통 규칙.
상세 내용은 각 참조 파일에서 확인한다.

---

## 기술 제약사항

| 제약 | 내용 |
|------|------|
| Java 버전 | Java 17 — Java 21 전용 기능(Virtual Threads, Record Patterns) 사용 금지 |
| JPA Lazy Loading | `spring.jpa.open-in-view=false` — 서비스 계층 밖 Lazy 로딩 불가, 반드시 `JOIN FETCH` 사용 |
| DDL 자동화 | `prod` 환경 `ddl-auto=none` — Entity 변경 시 수동 SQL 마이그레이션 필요 |
| 시크릿 관리 | 비밀번호·API 키·JWT 시크릿 하드코딩 금지 — 환경 변수로만 주입 |
| QueryDSL | Entity 변경 후 Q클래스 재생성 필수: `./gradlew compileJava` |

---

## 빌드 명령어

```bash
# 컴파일 확인 (테스트 스킵)
./gradlew build -x test

# QueryDSL Q클래스 재생성 (Entity 변경 후)
./gradlew compileJava

# 특정 테스트 클래스만 실행
./gradlew test --tests "com.snut_likelion.{패키지}.{TestClassName}"

# 전체 테스트
./gradlew test

# JaCoCo 커버리지 리포트
./gradlew test jacocoTestReport
```

---

## 패키지 루트

```
소스:  src/main/java/com/snut_likelion/
테스트: src/test/java/com/snut_likelion/
Q클래스: src/main/generated/
```

---

## 커밋 메시지 컨벤션

규칙 전문: `.claude/conventions/workflow.md`

```
{type}(#{issue}): {요약}
```

| 에이전트 | 사용 type |
|---------|---------|
| domain-implementer | `feat` / `fix` / `refactor` |
| test-writer | `test` |

- 이슈 번호(`#{issue}`)는 `_workspace/01_analyst_plan.md`에서 확인하거나, 없으면 생략
- 요약은 한국어로 구체적으로 작성

---

## 불명확한 요구사항 처리 원칙

**중단하지 않는다.** 불명확한 부분은 가정을 명시하고 작업을 계속 진행한다.

```
## 가정 사항
- {불명확한 항목}: {선택한 가정} (이유: {근거})
```

가정이 틀렸을 경우 오케스트레이터가 수정 요청을 보낸다.

---

## 참조 파일 목록

| 파일 | 내용 |
|------|------|
| `CLAUDE.md` | 프로젝트 전체 컨벤션 |
| `.claude/conventions/code.md` | Entity/Repo/Service/Controller/DTO 규칙 |
| `.claude/conventions/test.md` | Mockito 단위 테스트 규칙 |
| `.claude/conventions/workflow.md` | 브랜치 전략, 커밋 컨벤션, 개발 순서 |
| `.claude/domains/auth.md` | JWT 인증, 비밀번호 재설정 |
| `.claude/domains/recruitment.md` | 모집 공고, 지원서, 알림 구독 |
| `.claude/domains/file.md` | S3 Presigned URL 업로드 플로우 |
| `.claude/agents/shared/workspace-protocol.md` | _workspace/ 파일 구조 및 소유권 |
