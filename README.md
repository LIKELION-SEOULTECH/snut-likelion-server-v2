# 🦁 SNUT LikeLion 통합 관리 시스템 v2
> **서울과학기술대학교 멋쟁이사자처럼의 운영 효율화 및 데이터 통합 관리를 위한 백엔드 서비스**
>
> 기존의 파편화된 운영 도구들을 하나로 통합하여, 14기 운영진의 업무 리소스를 절감하고 지원자들에게 안정적인 경험을 제공하는 프로덕트입니다.

<br/>

## 1. 프로젝트 개요
* **개발 기간**: 2025.10 ~ 현재 운영 중
* **주요 목적**: 신입 회원 모집 프로세스 자동화, 멤버 활동 이력 관리, 공지 및 콘텐츠 허브 구축
* **운영 성과**: 2026년 14기 신입 회원 모집 시 실제 도입하여 안정적으로 운영

<br/>

## 2. 기술 스택 (Tech Stack)
* **Language**: Java 17
* **Framework**: Spring Boot 3.4.5
* **Build Tool**: Gradle
* **RDBMS**: MySQL (Production), H2 (Local/Test), Redis
* **ORM**: Spring Data JPA / QueryDSL 5.0
* **Security**: Spring Security & **JJWT** (Access/Refresh Token 전략)
* **Storage**: **AWS S3** (Presigned URL을 통한 이미지 업로드 최적화)
* **External API**: WebClient (AI 서버 연동 및 챗봇 구현)
* **DevOps**: Docker, GitHub Actions (CI/CD)

<br/>

## 3. 핵심 도메인 및 기능 (Core Features)

| 도메인 | 주요 기능 설명 | 관련 기술 |
| :--- | :--- | :--- |
| **Recruitment** | 기수별 동적 지원서 생성, 지원 상태 관리, 합격 발표 자동화 | Scheduler |
| **Member** | 운영진/사자 권한 분리, 파트별 멤버 프로필 및 포트폴리오 관리 | Role-based Security |
| **Project** | 프로젝트 아카이빙, 카테고리별 검색 및 상세 조회 | QueryDSL |
| **Notice & Blog** | 공지사항 및 기술 블로그 게시판, AI 기반 공지 요약 기능 | External AI API |
| **Auth** | JWT 기반 소셜 및 일반 로그인, 비밀번호 재설정 기능 | Mail Sender |

## 4. Troubleshooting

- QueryDSL 기반의 동적 필터링 시스템 구축
  * **문제**: 지원서 및 멤버 조회 시 필터링 조건(파트, 기수, 상태 등)이 복잡해짐에 따라 기존 JPA 만으로는 코드 가독성과 유지보수성이 저하됨.
  * **해결**: QueryDSL을 도입하여 `BooleanExpression` 단위로 조건을 모듈화. 이를 통해 복잡한 검색 쿼리를 자바 코드로 안전하게 관리하고 성능을 최적화

- 정교한 JWT 보안 전략 수립
  * **문제**: Access Token만 사용할 경우 탈취 리스크가 크고, 만료 시마다 사용자가 재로그인해야 하는 불편함이 발생함.
  * **해결**: Access/Refresh Token 체계를 도입하고, 서버 측에서 토큰 만료 및 유효성을 엄격히 검증하는 필터 계층을 설계하여 보안성과 사용자 경험을 동시에 확보

- 이미지 처리 성능 및 비용 최적화
  * **문제**: 서버를 거쳐 S3에 이미지를 업로드할 경우 서버 리소스 소모와 트래픽 비용이 증가함.
  * **해결**: S3 Presigned URL 방식을 도입하여 클라이언트가 저장소로 직접 업로드하게 함으로써 서버 부하를 제거하고 처리 속도를 개선