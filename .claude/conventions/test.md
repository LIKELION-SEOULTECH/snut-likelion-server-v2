# 테스트 컨벤션 (Test Conventions)

## 원칙

- **서비스 레이어**: 외부 의존성(DB, S3, AI 서버)을 Mockito로 격리, 비즈니스 로직만 검증
- **레포지토리 레이어**: `@DataJpaTest`로 슬라이스 테스트 (실제 DB 쿼리 검증)
- **외부 HTTP 통신**: WireMock 통합 테스트로 검증 (OpenFeign 클라이언트)
- **목표 커버리지**: 서비스 레이어 JaCoCo instruction coverage 90% 이상

---

## 서비스 단위 테스트 (Mockito)

### 기본 구조

```java
@ExtendWith(MockitoExtension.class)
class BlogCommandServiceTest {

    @Mock BlogRepository blogRepository;
    @Mock UserRepository userRepository;

    @InjectMocks BlogCommandService blogCommandService;

    // 공통 픽스처
    User user;
    Blog blog;

    @BeforeEach
    void setup() {
        user = User.builder().id(1L).email("test@test.com").username("tester").build();
        blog = Blog.builder().title("제목").author(user).build();
        // BaseEntity의 id는 ReflectionTestUtils로 주입
        ReflectionTestUtils.setField(blog, "id", 1L);
    }

    @Test
    void createPost_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        blogCommandService.createPost(1L, new CreateBlogPostRequest("제목", "OFFICIAL"));

        verify(blogRepository).save(any(Blog.class));
    }

    @Test
    void createPost_userNotFound_throwsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blogCommandService.createPost(99L, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(UserErrorCode.NOT_FOUND.getMessage());
    }
}
```

### 핵심 규칙

**Mock 선언**
- `@Mock`: 의존성 목 선언
- `@InjectMocks`: 테스트 대상 서비스 (생성자 주입 방식으로 Mock 주입됨)
- `@Spy`: 실제 구현을 부분적으로 덮어쓸 때 사용

**Stubbing 규칙**
- `when(...).thenReturn(...)`: 일반 반환값 설정
- `doThrow(...).when(mock).method(...)`: void 메서드에 예외 설정
- `doNothing().when(mock).method(...)`: void 메서드에만 사용 (non-void에 사용 시 MockitoException 발생)
- 실제로 호출되지 않는 stubbing 금지 → `UnnecessaryStubbingException` 발생 (strict mode)

**ReflectionTestUtils 활용**
```java
// BaseEntity의 id (protected 필드) 설정
ReflectionTestUtils.setField(entity, "id", 1L);

// @Value 필드 설정 (JwtService 등)
ReflectionTestUtils.setField(service, "activeProfile", "dev");
ReflectionTestUtils.setField(service, "currentGeneration", 14);

// private 메서드 직접 호출 (필요한 경우만)
ReflectionTestUtils.invokeMethod(object, "privateMethodName", arg1, arg2);
```

**Verification**
```java
verify(repository).save(any(Blog.class));           // 호출 여부 검증
verify(repository, never()).delete(any());          // 미호출 검증
verify(repository, times(2)).findById(anyLong());   // 호출 횟수 검증
```

---

## 예외 검증 패턴

```java
// 예외 타입 + 메시지 동시 검증
assertThatThrownBy(() -> service.someMethod(invalidArg))
        .isInstanceOf(NotFoundException.class)
        .hasMessage(UserErrorCode.NOT_FOUND.getMessage());

// 예외 미발생 검증
assertThatCode(() -> service.someMethod(validArg))
        .doesNotThrowAnyException();
```

---

## `@Value` 필드가 있는 서비스 테스트

`@ExtendWith(MockitoExtension.class)`는 Spring 컨텍스트를 로드하지 않으므로 `@Value` 필드가 주입되지 않는다.
`ReflectionTestUtils.setField()`로 직접 주입한다.

```java
@BeforeEach
void setup() {
    // JwtService의 @Value 필드
    ReflectionTestUtils.setField(jwtService, "activeProfile", "dev");
    ReflectionTestUtils.setField(jwtService, "currentGeneration", 14);
}
```

---

## 레포지토리 슬라이스 테스트 (`@DataJpaTest`)

```java
@DataJpaTest
@Import(QueryDslConfig.class)   // QueryDSL JPAQueryFactory 빈 등록
class BlogQueryRepositoryTest {

    @Autowired BlogQueryRepository blogQueryRepository;
    @Autowired TestEntityManager em;

    @Test
    void getBlogList_filterByCategory_returnsFiltered() {
        // given
        User user = em.persist(User.builder().email("t@t.com").build());
        em.persist(Blog.builder().title("공지").category("OFFICIAL").author(user).build());
        em.persist(Blog.builder().title("기술글").category("UNOFFICIAL").author(user).build());
        em.flush();

        // when
        Page<BlogListResponse> result = blogQueryRepository
                .getBlogList("OFFICIAL", null, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("공지");
    }
}
```

---

## WireMock 통합 테스트 (OpenFeign)

```java
@SpringBootTest
@AutoConfigureWireMock(port = 0)
class AiIntegrationTest {

    @Autowired AiQueryService aiQueryService;

    @Test
    void chat_aiServerDown_returnsFallback() {
        stubFor(post(urlEqualTo("/chat"))
                .willReturn(aResponse().withStatus(500)));

        String result = aiQueryService.chat("안녕");

        assertThat(result).isEqualTo("현재 챗봇 서비스를 이용할 수 없습니다.");
    }
}
```

---

## MockHttpServletResponse (쿠키/헤더 검증)

```java
@Test
void setCookie_devProfile_setsTwoCookies() {
    MockHttpServletResponse response = new MockHttpServletResponse();
    jwtService.setCookie(tokenDto, response);

    assertThat(response.getHeaders("Set-Cookie")).hasSize(2);
}
```

---

## 테스트 네이밍 컨벤션

```
{메서드명}_{시나리오}_{기대결과}
```

예시:
```
createPost_success
createPost_userNotFound_throwsNotFoundException
getPost_validId_returnsDetail
logout_validToken_deletesRefreshTokens
validate_expiredToken_throwsUnauthorized
```

---

## 테스트 파일 위치

```
src/test/java/com/snut_likelion/
├── domain/
│   ├── auth/service/AuthServiceTest.java
│   ├── blog/service/BlogCommandServiceTest.java
│   ├── user/service/MemberQueryServiceTest.java
│   ├── project/service/ProjectCommandServiceTest.java
│   ├── project/service/ProjectRetrospectionServiceTest.java
│   └── file/service/FileUploadServiceTest.java
├── admin/
│   ├── blog/service/AdminBlogServiceTest.java
│   ├── notice/service/AdminNoticeServiceTest.java
│   ├── project/service/AdminProjectServiceTest.java
│   └── member/service/AdminMemberServiceTest.java
├── global/
│   └── auth/jwt/JwtServiceTest.java
└── infra/
    └── ai/AiIntegrationTest.java
```

---

## 알려진 커버리지 한계

| 서비스 | 커버리지 | 원인 |
|--------|---------|------|
| `ProjectCommandService` | ~53% | `connectRetrospections`, `upsertRetrospections`, `createRetrospection`이 public 메서드에서 호출되지 않는 dead code — 단위 테스트로 커버 불가 |
| `JwtService` | ~85% | `SecurityException` 분기, 일부 쿠키 속성 세부 검증 누락 |
| `FileUploadService` | ~89% | `docx`/`xlsx`/`pptx`/`txt`/`svg` ContentType switch case 미커버 |
