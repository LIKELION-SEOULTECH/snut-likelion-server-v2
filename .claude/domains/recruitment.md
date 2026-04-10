# 도메인: Recruitment (모집·지원서)

## 개요

기수별 신입 회원 모집 프로세스를 관리한다.
- 모집 공고(`Recruitment`) 생성 및 관리
- 동적 지원서 폼(`Question`) 구성
- 지원자 `Application` 및 답변(`Answer`) 저장
- 합격 발표 자동화 (Scheduler)
- 기수별 모집 알림 구독(`RecruitmentSubscription`)

---

## 주요 엔티티

### Recruitment

```
id | generation | title | status | startAt | endAt | ...
```

- `status`: `OPEN` / `CLOSED` / `ANNOUNCED`
- 기수(`generation`) 단위로 생성됨

### Application

```
id | recruitment_id | user_id | part | status | isPersonalInfoConsent | ...
```

- `status`: `PENDING` / `PASSED` / `FAILED`
- 1인 1지원 제약 (`APPLICATION_ALREADY_EXISTS` 예외)

### Question / Answer

- `Question`: 모집 공고별 동적 질문 (`type`: `TEXT` / `SELECT` / `MULTI_SELECT`)
- `Answer`: 지원자의 질문별 답변 (JSON 형태 저장 가능)

### RecruitmentSubscription

- 이메일 기반 알림 구독
- `generation` 필드로 기수별 구독 분리 (68-alert 브랜치에서 추가됨)
- 기존 구독자에게 다른 기수 알림 발송 차단

---

## 주요 비즈니스 규칙

1. **중복 지원 방지**: 같은 `(user, recruitment)` 조합으로 지원 시 `ExistingResourceException`
2. **모집 기간 검증**: `startAt` ~ `endAt` 범위 밖 지원 시 `BadRequestException`
3. **파트 필수**: `part` null → Bean Validation `@NotNull`
4. **개인정보 동의 필수**: `isPersonalInfoConsent` → `@NotNull`
5. **합격 발표**: Scheduler가 `endAt` 이후 `status`를 `ANNOUNCED`로 변경, 합격/불합격 메일 발송

---

## 쿼리 패턴

복합 필터(파트·기수·상태) 조회는 QueryDSL로 처리:

```java
// infra/AdminRecruitmentQueryRepository.java
BooleanExpression partFilter = part != null ? application.part.eq(part) : null;
BooleanExpression statusFilter = status != null ? application.status.eq(status) : null;
```

---

## 알림 구독 플로우

```
[1] POST /api/v1/recruitments/subscribe?email=...&generation=14
    → RecruitmentSubscription 저장 (generation 포함)

[2] 모집 공고 오픈 시 Scheduler 또는 관리자 트리거
    → 해당 generation 구독자에게만 메일 발송
```

---

## 관련 패키지

```
domain/recruitment/
  entity/   Recruitment, Application, Question, Answer, RecruitmentSubscription
  service/  ApplicationCommandService, RecruitmentQueryService, NotificationService
  infra/    AdminRecruitmentQueryRepository

admin/recruitment/
  service/  AdminRecruitmentService, AdminApplicationService
```
