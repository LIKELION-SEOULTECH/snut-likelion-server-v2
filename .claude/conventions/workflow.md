# 개발 워크플로우 (Workflow)

## 브랜치 전략

```
main          ← 운영 배포 브랜치 (직접 push 금지)
  └── {issue-number}-{feature-slug}   ← 기능 브랜치
        예) 71-find-pw
            68-alert
            48-hotfix
```

- 브랜치명은 GitHub 이슈 번호를 prefix로 사용한다
- `main`에는 반드시 PR을 통해 머지한다

## 커밋 메시지 컨벤션

```
{type}(#{issue}): {summary}
```

### type 목록

| type | 용도 |
|------|------|
| `feat` | 새 기능 추가 |
| `fix` | 버그 수정 |
| `test` | 테스트 추가·수정 |
| `refactor` | 기능 변경 없는 코드 개선 |
| `docs` | 문서 수정 (README 등) |
| `chore` | 빌드·설정 변경 |

### 예시

```
feat(#71): 비밀번호 재설정 메일을 링크 방식에서 인증 코드 방식으로 변경
test(#71): 비밀번호 재설정 인증 코드 검증 테스트 추가
fix(#48): 공지사항 일반 파일 업로드 완료 시 key 검증 오류 수정
test(#68): 기수별 모집 알림 구독 분리 검증 테스트 추가
```

## 기능 개발 순서

1. GitHub 이슈 생성 → 이슈 번호 확인
2. `main`에서 브랜치 생성: `git checkout -b {issue-number}-{slug}`
3. Entity 변경 시 `./gradlew compileJava`로 Q클래스 재생성
4. 계층 구현 순서: Entity → Repository → Service → Controller → DTO
5. 서비스 로직 구현 후 단위 테스트 작성 (Mockito)
6. `./gradlew test`로 전체 테스트 통과 확인
7. PR 생성 → 리뷰 → `main` 머지

## PR 작성 가이드

- 제목: `feat(#{issue}): {요약}` 형식
- 본문에 변경 파일 목록, 주요 변경 내용, 테스트 항목 포함
- 스크린샷 또는 API 응답 예시 첨부 권장 (새 엔드포인트 추가 시)

## 로컬 개발 체크리스트

```bash
# 빌드 확인
./gradlew build -x test

# 테스트 실행
./gradlew test

# JaCoCo 커버리지 리포트 생성
./gradlew test jacocoTestReport
# 리포트 위치: build/reports/jacoco/test/jacocoTestReport.xml

# QueryDSL Q클래스 재생성
./gradlew compileJava

# 로컬 실행 (dev 프로필)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## 환경 변수 관리

- `.env` 파일은 Git 커밋 금지 (`.gitignore` 등록 필수)
- 새 환경 변수 추가 시 `CLAUDE.md` 필수 환경 변수 목록 업데이트
- 로컬: `.env` 파일 또는 IntelliJ Run/Debug Configurations에서 직접 설정
