# 코드 컨벤션 (Code Conventions)

## 패키지 구조

```
com.snut_likelion/
├── global/          # 공통 관심사 (설정, 인증, 예외, 공통 DTO)
│   ├── auth/        # JWT 필터, SecurityConfig, SnutLikeLionUser
│   ├── config/      # Spring Bean 설정
│   ├── dto/         # ApiResponse<T> (공통 응답 래퍼)
│   ├── error/       # GlobalExceptionHandler, 예외 클래스, BaseError
│   └── support/     # BaseEntity (id, createdAt, updatedAt)
│
├── domain/          # 사용자 기능 도메인
│   ├── auth/
│   ├── recruitment/ # 지원서·모집공고
│   ├── user/
│   ├── project/
│   ├── blog/
│   ├── notice/
│   └── file/
│
├── admin/           # 관리자 전용 기능 (도메인별 분리)
│   ├── recruitment/
│   ├── member/
│   └── ...
│
└── infra/           # 외부 시스템 연동 (S3, 메일, AI 클라이언트)
```

### 도메인 내부 계층

```
{domain}/{feature}/
├── entity/
├── repository/
├── infra/           # QueryDSL 커스텀 레포지토리
├── service/
├── controller/
└── dto/
    ├── req/         # 요청 DTO
    └── res/         # 응답 DTO
```

---

## Entity

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "blogs")
public class Blog extends BaseEntity {   // ← 반드시 BaseEntity 상속

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlogTag> tags = new ArrayList<>();

    @Builder
    public Blog(String title, User author) {
        this.title = title;
        this.author = author;
    }

    // 비즈니스 메서드는 엔티티 안에 위치
    public void update(String title) {
        this.title = title;
    }
}
```

**규칙**
- `BaseEntity` 상속 → `id`, `createdAt`, `updatedAt` 자동 관리
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` — JPA용 기본 생성자
- 팩토리 생성자에 `@Builder` 사용
- `@OneToMany`: `cascade = ALL, orphanRemoval = true`, 컬렉션은 `new ArrayList<>()`로 초기화
- `@ManyToOne`: `fetch = LAZY` 필수
- 비즈니스 로직(상태 변경)은 엔티티 메서드로 캡슐화

---

## Repository

```java
public interface BlogRepository extends JpaRepository<Blog, Long> {

    // N+1 방지: JOIN FETCH 명시
    @Query("SELECT b FROM Blog b JOIN FETCH b.author WHERE b.id = :id")
    Optional<Blog> findWithAuthorById(@Param("id") Long id);
}
```

### QueryDSL 커스텀 레포지토리

```java
// infra/ 패키지에 위치
@Repository
@RequiredArgsConstructor
public class BlogQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<BlogListResponse> getBlogList(String category, String keyword, Pageable pageable) {
        BooleanExpression categoryFilter = category != null
                ? blog.category.eq(category) : null;
        BooleanExpression keywordFilter = keyword != null
                ? blog.title.containsIgnoreCase(keyword) : null;

        List<BlogListResponse> content = queryFactory
                .select(/* ... */)
                .from(blog)
                .where(categoryFilter, keywordFilter)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory.select(blog.count()).from(blog)
                .where(categoryFilter, keywordFilter).fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}
```

**규칙**
- 복잡한 동적 필터는 QueryDSL `BooleanExpression`으로 분리
- `null` 조건은 where절에서 자동으로 무시됨 — 별도 분기 불필요
- N+1 방지를 위해 `JOIN FETCH` / `fetchJoin()` 명시

---

## Service

```java
@Service
@RequiredArgsConstructor
public class BlogCommandService {

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createPost(Long userId, CreateBlogPostRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.NOT_FOUND));

        Blog blog = Blog.builder()
                .title(req.getTitle())
                .author(user)
                .build();

        blogRepository.save(blog);
    }

    @Transactional(readOnly = true)
    public BlogDetailResponse getPost(Long blogId) {
        Blog blog = blogRepository.findWithAuthorById(blogId)
                .orElseThrow(() -> new NotFoundException(BlogErrorCode.NOT_FOUND));
        return BlogDetailResponse.from(blog);
    }
}
```

**규칙**
- `@Service` + `@RequiredArgsConstructor` (생성자 주입)
- 데이터 변경: `@Transactional`
- 조회 전용: `@Transactional(readOnly = true)`
- 엔티티 조회 실패 시 즉시 예외 throw — `orElseThrow()` 사용
- 서비스 간 의존은 최소화; 다른 도메인 데이터가 필요하면 해당 레포지토리를 직접 주입

---

## Controller

```java
@RestController
@RequestMapping("/api/v1/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final BlogCommandService blogCommandService;
    private final BlogQueryService blogQueryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Object> createPost(
            @AuthenticationPrincipal SnutLikeLionUser user,
            @RequestBody @Valid CreateBlogPostRequest req
    ) {
        blogCommandService.createPost(user.getId(), req);
        return ApiResponse.success("게시글이 등록되었습니다.");
    }

    @GetMapping("/{blogId}")
    public ApiResponse<BlogDetailResponse> getPost(@PathVariable Long blogId) {
        return ApiResponse.success(blogQueryService.getPost(blogId));
    }
}
```

**관리자 전용 엔드포인트**

```java
@RestController
@RequestMapping("/api/v1/admin/blogs")
@PreAuthorize("hasRole('ROLE_MANAGER')")
@RequiredArgsConstructor
public class AdminBlogController { ... }
```

**규칙**
- `@RestController` + `@RequestMapping("/api/v1/...")`
- 현재 사용자: `@AuthenticationPrincipal SnutLikeLionUser user`
- 요청 검증: `@RequestBody @Valid` 필수
- 응답: 반드시 `ApiResponse<T>`로 래핑
- 관리자 엔드포인트: `@PreAuthorize("hasRole('ROLE_MANAGER')")`

---

## DTO

### 요청 DTO (`dto/req/`)

```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateBlogPostRequest {

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    @NotNull(message = "카테고리를 선택해주세요.")
    private String category;

    // 선택 필드 — 검증 어노테이션 없이 선언
    private String content;
}
```

### 응답 DTO (`dto/res/`)

```java
@Getter
@Builder
public class BlogDetailResponse {

    private Long id;
    private String title;
    private String authorName;

    // 엔티티에서 변환: static from() 팩토리 메서드
    public static BlogDetailResponse from(Blog blog) {
        return BlogDetailResponse.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .authorName(blog.getAuthor().getUsername())
                .build();
    }

    // 엔티티 없이 필드로 직접 생성: static of() 사용
    public static BlogDetailResponse of(Long id, String title) {
        return BlogDetailResponse.builder()
                .id(id).title(title).build();
    }
}
```

**규칙**
- 패키지명: `dto/req/` / `dto/res/` (`request`/`response` 전체 단어 금지)
- Request: `@Getter` + `@NoArgsConstructor(access = PROTECTED)`, Bean Validation 어노테이션
- Response: `@Getter` + `@Builder`, `static from(Entity)` 팩토리 메서드
- 엔티티를 Controller/Service 바깥으로 직접 노출 금지

---

## 예외 처리

### 예외 클래스 선택

| 클래스 | HTTP | 용도 |
|--------|------|------|
| `BadRequestException` | 400 | 잘못된 요청 파라미터·상태 |
| `UnauthorizedException` | 401 | 인증 필요 |
| `ForbiddenException` | 403 | 권한 없음 |
| `NotFoundException` | 404 | 리소스 없음 |
| `ExistingResourceException` | 409 | 이미 존재하는 리소스 |
| `NotAcceptableException` | 406 | 허용되지 않는 요청 |
| `InternalServerException` | 500 | 서버 내부 오류 |

### 도메인별 에러 코드 정의

```java
public enum BlogErrorCode implements BaseError {
    NOT_FOUND("BLOG_NOT_FOUND", "게시글을 찾을 수 없습니다."),
    ALREADY_PUBLISHED("BLOG_ALREADY_PUBLISHED", "이미 발행된 게시글입니다.");

    private final String code;
    private final String message;

    BlogErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
```

### 예외 throw 패턴

```java
// 리소스 없음
Blog blog = blogRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(BlogErrorCode.NOT_FOUND));

// 중복 리소스
if (blogRepository.existsByTitle(title)) {
    throw new ExistingResourceException(BlogErrorCode.ALREADY_EXISTS);
}

// 비즈니스 규칙 위반
if (blog.isPublished()) {
    throw new BadRequestException(BlogErrorCode.ALREADY_PUBLISHED);
}
```

---

## 공통 응답 포맷 (ApiResponse)

```json
// 성공 (데이터 있음)
{ "code": "OK", "data": { ... } }

// 성공 (메시지만)
{ "code": "OK", "message": "처리 완료" }

// 실패
{ "code": "NOT_FOUND", "message": "게시글을 찾을 수 없습니다." }
```

```java
ApiResponse.success(data)
ApiResponse.success("처리 완료")
ApiResponse.success(data, "처리 완료")
ApiResponse.fail(BlogErrorCode.NOT_FOUND)
ApiResponse.fail(BlogErrorCode.NOT_FOUND, "상세 메시지")
```

---

## S3 Presigned URL 업로드 패턴

```
[1] 클라이언트 → 서버: Presigned URL 발급 요청 (파일명, ContentType)
[2] 서버 → 클라이언트: S3 key + Presigned PUT URL 반환
[3] 클라이언트 → S3: Presigned URL로 파일 직접 업로드
[4] 클라이언트 → 서버: 업로드 완료 신고 (key 포함)
    → 서버: S3 실존 확인 + 메타데이터 저장
```

- S3 key 형식: `{storageRoot}/{category}/{UUID}-{originalName}.{ext}`
- DB에는 URL이 아닌 key만 저장 (CDN 변경 시 마이그레이션 불필요)
- `FileStorageType` enum으로 이미지(`images/`) / 파일(`files/`) 경로 분리

---

## JWT 인증 패턴

- Access Token + Refresh Token 이중 구조
- 현재 사용자: `@AuthenticationPrincipal SnutLikeLionUser user`
- `user.getId()` / `user.getEmail()` / `user.getGeneration()` 사용
- 쿠키에 토큰 설정: `JwtService.setCookie(tokenDto, response)`

---

## 비동기 메일 발송

```java
// MailSender 인터페이스를 통해 추상화
// 실제 구현: GmailSender (infra/mail/)
// 비동기: @Async 적용됨 → 메일 발송 실패가 비즈니스 로직을 차단하지 않음
mailSender.sendPasswordResetCode(email, code);
```
