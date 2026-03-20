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

- S3 Presigned URL 기반 파일 업로드 아키텍처 설계
  * **문제**: 서버를 경유하여 S3에 파일을 업로드하면 서버 메모리·트래픽 비용이 증가하고, 대용량 파일 처리 시 타임아웃 위험이 있음.
  * **해결**: 클라이언트가 서버를 거치지 않고 S3에 직접 업로드하는 Presigned URL 방식을 도입하여 서버 부하를 제거하고 처리 속도를 개선
  * **적용 범위**: Blog 이미지, Project 이미지, Member 프로필 이미지, Notice 이미지·파일 첨부 (4개 도메인 전체)

<br/>

## 5. S3 Presigned URL 업로드 상세 설계

### 배경 및 문제 정의

초기에는 클라이언트가 이미지를 서버에 전송하면 서버가 S3에 재업로드하는 방식을 사용했습니다.
이 구조는 다음과 같은 문제를 야기합니다.

- 서버가 파일 스트림을 메모리에 올려 처리하므로, 동시 업로드가 많을수록 GC 부담 및 OOM 위험 증가
- 서버 → S3 구간의 추가 네트워크 홉으로 인한 업로드 지연
- Spring의 `multipart.max-file-size` 등 설정 한계로 대용량 파일 처리 어려움

### 해결: 4단계 Presigned URL 업로드 플로우

```
[STEP 1] 클라이언트 → 서버: URL 발급 요청
         POST /api/v1/admin/files/presigned-url
         { originalFileName, contentType, contentLength, uploadCategory, fileStorageType }

[STEP 2] 서버 → 클라이언트: Presigned PUT URL 응답
         서버가 UUID 기반 S3 key를 직접 생성 (경로 규칙 강제)
         응답: { uploadUrl, storedFileName, fileUrl, expiresInSeconds }

[STEP 3] 클라이언트 → S3: Presigned URL로 파일 직접 PUT 업로드
         서버를 완전히 우회하여 S3에 직접 업로드

[STEP 4] 클라이언트 → 서버: 업로드 완료 메타데이터 등록
         POST /api/v1/admin/files/upload-complete
         서버가 S3 파일 실존 여부를 확인 후 UploadedFile 테이블에 메타데이터 저장

[STEP 5] 도메인 생성/수정 API 호출 시 storedFileName(S3 key) 전달
         서버가 UploadedFile 테이블에서 등록 여부를 재검증 후 엔티티에 저장
```

### 핵심 설계 결정

**① 서버가 S3 key를 직접 생성**

클라이언트가 key를 자유롭게 지정하면 경로 조작(Path Traversal) 및 타 도메인 폴더 침범이 가능합니다.
서버가 `{storageRoot}/{category}/{UUID}-{sanitizedName}.{ext}` 형식으로 key를 생성하여 경로 규칙을 강제합니다.

```
images/notices/a1b2c3d4-banner.png   ← IMAGE 타입
files/notices/e5f6g7h8-report.pdf    ← FILE 타입
```

**② storedFileName을 엔티티에 저장하고, URL은 조회 시 동적으로 생성**

URL을 DB에 저장하면 CDN 도메인 변경, S3 버킷 이전 등 인프라 변경 시 대규모 데이터 마이그레이션이 필요합니다.
S3 key(storedFileName)만 저장하고, 조회 시점에 `FileUploadService.buildFileUrl(key)`로 URL을 동적 생성합니다.

```java
// 엔티티 저장 시: key만 저장
user.changeProfileImage("images/members/uuid-profile.png");

// 조회 응답 시: URL로 변환
String url = fileUploadService.buildFileUrl(storedFileName);
```

**③ 4단계 검증으로 미완료 업로드 차단**

Presigned URL을 발급받고도 S3에 실제로 업로드하지 않은 key가 도메인 엔티티에 저장되는 것을 방지하기 위해,
도메인 저장 시 `UploadedFile` 테이블 등록 여부를 재검증합니다.

```
발급(STEP 2) → S3 업로드(STEP 3) → 완료 등록(STEP 4) → 도메인 저장(STEP 5)
                                     ↑                      ↑
                              S3 실존 확인            UploadedFile 등록 확인
```

**④ FileStorageType으로 이미지·파일 경로를 구분**

공지사항 도메인에서 이미지와 일반 파일(PDF, ZIP, DOCX 등)을 모두 지원하기 위해
`FileStorageType` enum(IMAGE → `images/`, FILE → `files/`)을 도입하여 저장 경로와 검증 규칙을 분리했습니다.

```java
// 이미지: images/notices/uuid-banner.png
fileUploadService.validateStoredFileNames(imageKeys, UploadCategory.NOTICE, FileStorageType.IMAGE);

// 파일: files/notices/uuid-report.pdf
fileUploadService.validateStoredFileNames(fileKeys, UploadCategory.NOTICE, FileStorageType.FILE);
```

### 적용 도메인 현황

| 도메인 | 저장 유형 | S3 경로 |
| :--- | :--- | :--- |
| Blog | 이미지 | `images/blogs/{uuid}-{name}.{ext}` |
| Project | 이미지 | `images/projects/{uuid}-{name}.{ext}` |
| Member 프로필 | 이미지 | `images/members/{uuid}-{name}.{ext}` |
| Notice 이미지 | 이미지 | `images/notices/{uuid}-{name}.{ext}` |
| Notice 파일 첨부 | 문서·파일 | `files/notices/{uuid}-{name}.{ext}` |

