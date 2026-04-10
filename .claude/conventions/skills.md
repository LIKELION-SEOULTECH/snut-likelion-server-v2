# 기술 스택 (Skills)

## 언어 / 프레임워크

| 항목 | 버전 |
|------|------|
| Java | 17 |
| Spring Boot | 3.4.5 |
| Gradle | 8.x |
| Spring Data JPA | 3.x |
| QueryDSL | 5.0.0 (jakarta) |
| Spring Security | 6.x |

## 주요 라이브러리

| 라이브러리 | 용도 |
|-----------|------|
| JJWT 0.12.3 | JWT 발급 / 검증 |
| AWS SDK S3 2.25.21 | Presigned URL 직접 업로드 |
| Gmail SMTP | 비동기 메일 발송 |
| OpenFeign | AI 챗봇·요약 서버 연동 |
| Lombok | 보일러플레이트 제거 (`@Getter`, `@Builder`, etc.) |
| springdoc-openapi 2.8.5 | Swagger UI |
| WireMock | 외부 HTTP 통합 테스트 |

## 인프라 / DevOps

- **운영**: GCE + Cloud SQL (MySQL)
- **컨테이너**: Docker (Spring Boot 서버 / AI 서버 분리)
- **CI/CD**: GitHub Actions
- **파일 스토리지**: AWS S3 (Presigned URL)

## 금지 / 주의 사항

- Java 21 전용 기능 사용 금지 (Virtual Threads, Record Patterns 등)
- `spring.jpa.open-in-view=false` → 서비스 계층 밖 Lazy 로딩 불가, 반드시 `JOIN FETCH` 사용
- prod 환경 `ddl-auto=none` → 스키마 변경은 수동 SQL 마이그레이션으로 처리
- 시크릿(DB 비밀번호, JWT 시크릿, API 키)은 환경 변수로만 주입, `application.yml` 하드코딩 금지

## 테스트 스택

| 라이브러리 | 용도 |
|-----------|------|
| JUnit 5 | 테스트 프레임워크 |
| Mockito | 단위 테스트 Mock (서비스 레이어) |
| Spring Boot Test | 통합 테스트 컨텍스트 |
| `@DataJpaTest` | 레포지토리 계층 슬라이스 테스트 |
| WireMock | OpenFeign 외부 HTTP 통합 테스트 |
| MockHttpServletResponse | 쿠키/응답 헤더 검증 |

## QueryDSL 설정

- Q클래스 생성 위치: `src/main/generated/`
- Entity 변경 후 반드시 `./gradlew compileJava` 실행
- `BooleanExpression` 단위로 동적 필터 모듈화 (파트·기수·상태 복합 조건)
