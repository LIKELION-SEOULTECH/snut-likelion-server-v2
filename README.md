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
* **External API**: OpenFeign (AI 서버 연동 — 챗봇 · 요약)
* **DevOps**: Docker, GitHub Actions (CI/CD)

<br/>

## 3. 핵심 도메인 및 기능 (Core Features)

| 도메인 | 주요 기능 설명 | 관련 기술 |
| :--- | :--- | :--- |
| **Recruitment** | 기수별 동적 지원서 생성, 지원 상태 관리, 합격 발표 자동화 | Scheduler |
| **Member** | 운영진/사자 권한 분리, 파트별 멤버 프로필 및 포트폴리오 관리 | Role-based Security |
| **Project** | 프로젝트 아카이빙, 카테고리별 검색 및 상세 조회 | QueryDSL |
| **Notice & Blog** | 공지사항 및 기술 블로그 게시판, AI 기반 공지 자동 요약 | OpenFeign (AI 서버) |
| **AI** | 챗봇 intent 매핑 응답, 텍스트 요약 API | OpenFeign, Apache POI |
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

<br/>

## 6. AI 챗봇 연동 상세 설계

### 패키지 구조

AI 연동 기능은 JPA Entity·DB를 가지는 일반 비즈니스 도메인(`domain/`)과 성격이 다르기 때문에 최상위 독립 패키지 `ai/`로 분리합니다.

```
com.snut_likelion/
├── ai/                          ← AI 기능 독립 패키지 (Entity 없는 외부 연동 기능)
│   ├── controller/              AiController — POST /api/v1/ai/chat, /summarize
│   ├── dto/
│   │   ├── request/             AiChatRequest, AiSummarizeRequest  (@NotBlank + @NoArgsConstructor PROTECTED)
│   │   └── response/            AiChatResult, AiSummarizeResult    (static of() 팩토리)
│   ├── exception/               AiErrorCode, AiException (HTTP 503)
│   ├── repository/              AiChatRepository, AiSummaryRepository, IntentAnswerPort  (포트 인터페이스)
│   └── service/                 AiQueryService
│
└── infra/
    └── ai/                      ← AI 서버 연동 구현체
        ├── AiServerChatRequest/Response       (Feign 요청·응답 DTO — flat)
        ├── AiServerSummarizeRequest/Response  (Feign 요청·응답 DTO — flat)
        ├── NoticeSummaryApiClientImpl         (공지 자동 요약용 — 폴백 처리, Feign 기반)
        ├── client/
        │   ├── ChatFeignClient                POST /chat
        │   └── SummaryFeignClient             POST /summarize
        ├── intent/
        │   └── IntentAnswerResolver           엑셀(intent-answer.xlsx) 로딩·캐싱, IntentAnswerPort 구현
        └── repository/
            ├── AiChatRepositoryImpl           AiChatRepository 구현 (폴백 전략)
            └── AiSummaryRepositoryImpl        AiSummaryRepository 구현 (예외 전파 전략)
```

### 레이어 의존 방향 (Port-Adapter 패턴)

```
ai/service/AiQueryService
    │  depends on (interface only)
    ├─ ai/repository/AiChatRepository  ←── infra/ai/repository/AiChatRepositoryImpl
    ├─ ai/repository/AiSummaryRepository ←── infra/ai/repository/AiSummaryRepositoryImpl
    └─ ai/repository/IntentAnswerPort  ←── infra/ai/intent/IntentAnswerResolver
```

`ai/` 패키지는 `infra/`를 절대 직접 의존하지 않습니다. 모든 외부 연동은 포트 인터페이스를 통해 주입됩니다.

### 챗봇 요청 흐름 (POST /api/v1/ai/chat)

```
클라이언트
  │  { "text": "멋사 지원 방법 알려줘" }
  ▼
AiController.chat()
  ▼
AiQueryService.chat(text)
  ├─ AiChatRepository.chat(text)
  │    └─ ChatFeignClient → POST {ai-server.url}/chat
  │         응답: { matched_question, score }
  │         오류 시: ChatQueryResult(null, null) 반환 (폴백)
  │
  ├─ IntentAnswerPort.findAnswer(matchedQuestion)
  │    └─ IntentAnswerResolver → intent-answer.xlsx 인메모리 맵 조회
  │
  ├─ 매핑 성공 → 엑셀 answer 반환
  └─ 매핑 실패·null → "정확한 정보를 찾기 어려워요..." 폴백 문구
  ▼
ApiResponse<AiChatResult> { answer, matchedQuestion, score }
```

### 에러 처리 전략 비교

| API | 오류 발생 시 | 이유 |
| :--- | :--- | :--- |
| `/chat` | 폴백 문구 반환 (HTTP 200) | 챗봇은 항상 응답해야 하는 UX 요구사항 |
| `/summarize` | `AiException` throw (HTTP 503) | 요약 기능은 명시적 실패 처리가 필요 |
| 공지 자동 요약 | 폴백 문구 저장, 예외 무시 | 요약 실패가 공지 등록을 막으면 안 됨 |

### intent-answer.xlsx 매핑 캐시

```
애플리케이션 시작 시 (@PostConstruct) 1회 로딩
  resources/ai/intent-answer.xlsx
  ├── 헤더: intent | answer
  ├── 중복 intent / 빈 값 검증 → IllegalStateException
  └── Map<String, String> (LinkedHashMap → Map.copyOf, 불변)
```

### 공지 자동 요약과의 관계

| 구분 | 포트 인터페이스 | 구현체 | 오류 전략 |
| :--- | :--- | :--- | :--- |
| AI 요약 API | `ai/repository/AiSummaryRepository` | `AiSummaryRepositoryImpl` | 예외 throw |
| 공지 자동 요약 | `domain/notice/repository/SummaryApiClient` | `NoticeSummaryApiClientImpl` | 폴백 반환 |

두 포트는 역할이 다르므로 분리 유지합니다. 단, 구현체는 모두 `infra/ai/`에 통합되어 동일한 `SummaryFeignClient`를 사용합니다.

<br/>

## 7. 테스트 전략

### 핵심 원칙

이 프로젝트의 테스트는 **"내가 책임지는 계층만 테스트한다"** 는 원칙을 중심으로 설계됩니다.

> 서비스 레이어 테스트는 DB가 잘 동작하는지 확인하는 게 아니라,
> **내 비즈니스 로직이 올바른지** 확인하는 것입니다.
> 외부 의존성(DB, S3, AI 서버 등)은 모두 Mockito로 격리하고,
> 실제 HTTP 통신이 필요한 경우에만 WireMock 통합 테스트로 검증합니다.

### 사용 기술 한눈에 보기

| 도구 | 용도 |
| :--- | :--- |
| **Mockito** (`@ExtendWith(MockitoExtension.class)`) | Spring 컨텍스트 없이 순수 Java로 단위 테스트. Repository·Provider 등 외부 의존성을 가짜 객체(Mock)로 대체 |
| **AssertJ** (`assertThat`, `assertThatThrownBy`) | 가독성 높은 검증 문법. 예외 타입·메시지까지 체이닝으로 검증 |
| **Spring WebMvcTest** (`@WebMvcTest`) | Controller 레이어만 띄워 HTTP 요청/응답·유효성 검사를 빠르게 검증 |
| **WireMock** (`@AutoConfigureWireMock`) | 실제 HTTP 서버를 로컬에 띄워 외부 API(AI 서버) 동작을 시뮬레이션. 실제 네트워크 없이 E2E 흐름 검증 |
| **Apache POI + `@TempDir`** | 테스트용 `.xlsx` 파일을 코드로 생성해 Excel 로딩 로직을 검증. 테스트 종료 후 JUnit이 파일 자동 정리 |
| **`@SpringBootTest`** | 전체 Spring 컨텍스트를 H2 + WireMock 환경에서 구동. 실제 운영 흐름과 동일한 경로로 검증 |

---

### 단위 테스트 — Mockito 격리 패턴

모든 Service·Repository 단위 테스트는 동일한 구조를 따릅니다.

```java
@ExtendWith(MockitoExtension.class)   // Spring 없이 Mockito만 사용
class SomeServiceTest {

    @Mock SomeRepository repo;         // 실제 DB 연결 없는 가짜 객체
    @InjectMocks SomeService service;  // @Mock 객체를 자동 주입받는 테스트 대상

    @Test
    void 성공_케이스() {
        // Given — Mock의 반환값 지정
        when(repo.findById(1L)).thenReturn(Optional.of(someEntity));

        // When — 테스트 대상 실행
        SomeResult result = service.doSomething(1L);

        // Then — 결과 검증
        assertThat(result.getValue()).isEqualTo("expected");
        verify(repo).save(any());      // 특정 메서드가 호출됐는지 검증
    }

    @Test
    void 예외_케이스() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        // 예외 타입과 메시지를 한 번에 검증
        assertThatThrownBy(() -> service.doSomething(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ErrorCode.NOT_FOUND.getMessage());
    }
}
```

**`ArgumentCaptor`** — `save()`에 전달된 엔티티의 필드를 직접 꺼내 검증할 때 사용합니다.

```java
ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
verify(applicationRepository).save(captor.capture());
Application saved = captor.getValue();

assertThat(saved.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
assertThat(saved.getAnswers()).hasSize(4);
```

---

### 도메인별 테스트 상세

#### 지원서 (Application) — `ApplicationCommandServiceTest`

지원서 생성·수정의 핵심 비즈니스 규칙을 검증합니다.

```
검증 항목
├── 임시저장(DRAFT) vs 최종제출(SUBMITTED) 상태 분기
├── 파트별(BACKEND/FRONTEND/AI) 필수 질문 매칭 검증
├── 운영진 지원 시 부서(DEPARTMENT) 질문 추가 포함 여부
├── 제출 시 답변 세트가 필수 질문과 불일치하면 BadRequestException
├── 이미 SUBMITTED 상태인 지원서 수정 시도 시 예외 발생
└── 모집 공고·사용자 미존재 시 NotFoundException
```

특히 **`ArgumentCaptor`** 로 `save()` 직전의 엔티티 상태를 캡처해, 실제 DB에 저장되기 전에 비즈니스 로직이 올바르게 적용됐는지 확인합니다.

<br/>

#### AI 챗봇 — 3개 레이어 독립 검증

AI 기능은 계층별로 책임을 분리했기 때문에 테스트도 레이어별로 독립적으로 작성합니다.

**① `AiChatRepositoryImplTest` — Feign 클라이언트 예외 폴백 검증**

AI 서버 통신 실패 시 서비스 전체가 에러를 반환하지 않도록, 모든 Feign 예외를 `ChatQueryResult(null, null)` 로 변환하는지 검증합니다.

```java
@Test
void chat_whenFeignExceptionOccurs_returnsNullResultForFallback() {
    FeignException ex = FeignException.errorStatus("chat", response(500));
    when(chatFeignClient.chat(any())).thenThrow(ex);

    AiChatRepository.ChatQueryResult result = aiChatRepository.chat("question");

    // 예외를 던지지 않고 null 결과를 반환하는지 검증
    assertThat(result.matchedQuestion()).isNull();
    assertThat(result.score()).isNull();
}
```

검증하는 예외 유형: `FeignException` / `RetryableException` / `DecodeException` / `EncodeException` / `null 응답`

**② `AiQueryServiceTest` — 인텐트 매핑·폴백 로직 검증**

서비스 레이어는 Repository와 Port가 어떤 값을 반환하든 올바른 응답을 조립하는지 검증합니다.

```
시나리오
├── AI 응답 정상 + 인텐트 매핑 성공 → 엑셀 answer 반환
├── AI 응답에 matched_question이 null → 폴백 문구 반환
├── 매핑 테이블에 intent 키가 없음 → 폴백 문구 반환
└── summarize() — AI 예외 발생 시 AiException 전파 (fail-fast)
```

**③ `IntentAnswerResolverTest` — Excel 로딩 로직 검증**

`@TempDir` + Apache POI로 테스트용 `.xlsx` 파일을 직접 생성해 주입합니다.
Spring 컨텍스트 없이 Excel 파싱 로직 자체를 빠르게 검증할 수 있습니다.

```java
@Test
void load_whenDuplicateIntentExists_throwsIllegalStateException() throws IOException {
    // Apache POI로 중복 intent가 있는 xlsx 파일 생성 후 주입
    setResourcePath(new String[][]{
            {"intent", "answer"},
            {"faq-apply", "답변 1"},
            {"faq-apply", "답변 2"}   // ← 중복
    });

    assertThatThrownBy(() -> resolver.load())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Duplicate intent found: faq-apply");
}
```

검증하는 시나리오: 정상 조회 / null·공백 인텐트 / 공백 trim / 파일 없음 / 헤더 누락 / intent 중복 / 빈 셀 / 헤더만 있는 경우

<br/>

#### Presigned URL — `FileUploadServiceTest`

S3 Presigned URL 발급·완료·검증 흐름의 각 단계를 독립적으로 검증합니다.

```
검증 항목
├── [발급] S3 key가 {storageRoot}/{category}/{UUID}-{name}.{ext} 형식인지
├── [발급] FileStorageType.FILE 이면 files/ 경로, IMAGE 이면 images/ 경로 생성
├── [완료 등록] S3에 실제로 올라가지 않은 key → BadRequestException
├── [완료 등록] 이미 DB에 등록된 key → ExistingResourceException
├── [도메인 저장 전 검증] 잘못된 카테고리 prefix → BadRequestException
├── [도메인 저장 전 검증] Path Traversal 패턴(../) 포함 → BadRequestException
├── [도메인 저장 전 검증] DB에 미등록 key → BadRequestException
└── [도메인 저장 전 검증] FileStorageType.FILE / IMAGE 교차 검증
```

S3 SDK(`PresignedUrlProvider`)와 DB(`UploadedFileRepository`)는 모두 Mockito Mock으로 대체하여
**"서비스 로직이 올바른 조건으로 각 컴포넌트를 호출하는지"** 만 검증합니다.

```java
@Test
void validateStoredFileNames_pathTraversal_throwsBadRequest() {
    List<String> keys = List.of("images/blogs/../etc/passwd");  // Path Traversal 시도

    assertThatThrownBy(() -> fileUploadService.validateStoredFileNames(keys, UploadCategory.BLOG))
            .isInstanceOf(BadRequestException.class);
}
```

---

### 통합 테스트 — WireMock E2E 검증

`AiIntegrationTest` 는 전체 Spring 컨텍스트를 띄우고, 실제 AI 서버 대신 **WireMock** 이 HTTP 응답을 반환하도록 구성합니다.

```
[MockMvc] → [Controller] → [Service] → [Feign Client] → [WireMock(가짜 AI 서버)]
```

```java
@SpringBootTest              // 전체 Spring 컨텍스트 구동
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)  // 랜덤 포트로 가짜 HTTP 서버 실행
@ActiveProfiles({"integration-test", "dev"})
@TestPropertySource(properties = {
    "ai-server.url=http://localhost:${wiremock.server.port}"  // Feign이 WireMock을 바라보도록
})
class AiIntegrationTest {

    @Test
    @WithMockUser
    void chat_whenAiServerReturns500_returnsFallbackAnswer() throws Exception {
        // WireMock으로 AI 서버가 500을 반환하는 상황 시뮬레이션
        stubFor(WireMock.post(urlEqualTo("/chat"))
                .willReturn(aResponse().withStatus(500)));

        mockMvc.perform(post("/api/v1/ai/chat")...)
                .andExpect(status().isOk())  // 500이어도 클라이언트엔 200 반환
                .andExpect(jsonPath("$.data.answer").value(containsString("instagram.com/likelion_st")));
    }
}
```

통합 테스트 전용 설정(`application-integration-test.yml`)으로 운영 환경 설정을 완전히 격리합니다.

```yaml
# application-integration-test.yml
spring:
  config:
    import: ""          # 운영용 db.yml, aws.yml 등 임포트 억제
  datasource:
    url: jdbc:h2:mem:testdb  # H2 인메모리 DB 사용
```

**검증 시나리오 (14개)**

| 시나리오 | 기대 결과 |
| :--- | :--- |
| AI 서버 500 / 503 응답 | `/chat` → HTTP 200 + 폴백 문구 |
| matched_question이 null 또는 미매핑 | `/chat` → HTTP 200 + 폴백 문구 |
| 요약 성공 | `/summarize` → HTTP 200 + 요약 텍스트 |
| 요약 AI 서버 500 / null 응답 | `/summarize` → HTTP 503 + 에러코드 |
| text 필드 공백·null | 두 엔드포인트 모두 → HTTP 400 |
| 미인증 요청 | 두 엔드포인트 모두 → HTTP 401 |

---

### 테스트 커버리지 현황

| 테스트 클래스 | 종류 | 테스트 수 | 핵심 검증 |
| :--- | :--- | :---: | :--- |
| `ApplicationCommandServiceTest` | 단위 | 12 | 지원서 생성·수정·삭제 비즈니스 규칙 |
| `FileUploadServiceTest` | 단위 | 16 | Presigned URL 발급·완료·검증 전 단계 |
| `AiQueryServiceTest` | 단위 | 5 | 인텐트 매핑·폴백 조립 |
| `AiChatRepositoryImplTest` | 단위 | 6 | Feign 예외 유형별 폴백 |
| `IntentAnswerResolverTest` | 단위 | 13 | Excel 로딩 정상·이상 경로 |
| `NoticeSummaryApiClientImplTest` | 단위 | 8 | 공지 요약 Feign 예외 폴백 |
| `AiControllerTest` | 단위 (WebMvc) | 4 | 유효성 검사 + HTTP 응답 포맷 |
| `AiIntegrationTest` | 통합 (WireMock) | 14 | AI 기능 E2E 흐름 |
| 기타 (Blog, Notice, Project 등) | 단위 | 92 | 각 도메인 서비스 로직 |
| **합계** | | **170** | |
