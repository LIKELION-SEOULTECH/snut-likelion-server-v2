---
name: snut-feature-dev
description: SNUT LikeLion 서버(Spring Boot 3.4.5, Java 17)에서 새 도메인 기능을 구현할 때 반드시 사용하라. Entity/Repository/Service/Controller/DTO 구현, ApiResponse 래핑, QueryDSL 동적 필터, 관리자 API, 예외 처리 등 이 프로젝트 고유 컨벤션을 담고 있다. 기능 추가, API 개발, 도메인 구현, 버그 수정 시 이 스킬을 사용하라. snut-feature-dev를 다시 실행하거나 이전 구현을 업데이트할 때도 동일하게 사용하라.
---

# SNUT LikeLion Feature Development

이 스킬은 SNUT LikeLion 서버 고유 컨벤션을 담는다.
범용 Spring Boot 패턴은 `springboot-patterns` 스킬을 추가로 참조하라.
보안 관련 구현은 `springboot-security` 스킬을 추가로 참조하라.
JPA/QueryDSL 최적화는 `jpa-patterns` 스킬을 추가로 참조하라.

---

## 구현 시작 전 필수 확인

1. `.claude/conventions/code.md` — 패키지 구조 + 계층별 규칙
2. 관련 도메인 파일 — `.claude/domains/auth.md`, `recruitment.md`, `file.md`
3. 기존 유사 도메인 코드 — `src/main/java/com/snut_likelion/{domain}/`

---

## 구현 체크리스트

- [ ] Entity (BaseEntity 상속, @Builder, 비즈니스 메서드 캡슐화)
- [ ] Repository (JOIN FETCH로 N+1 방지)
- [ ] QueryDSL infra/ (동적 필터 조건이 있을 때)
- [ ] Service (@Transactional / @Transactional(readOnly=true))
- [ ] Controller (ApiResponse<T> 래핑, @Valid)
- [ ] DTO (dto/req/ + dto/res/, static from())
- [ ] ErrorCode enum (도메인 패키지 내)
- [ ] `./gradlew build -x test` 컴파일 확인

---

## Entity 패턴

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "{table_name}s")
public class {Name} extends BaseEntity {    // BaseEntity 상속 필수

    @Column(nullable = false)
    private String field;

    @ManyToOne(fetch = FetchType.LAZY)      // EAGER 금지
    @JoinColumn(name = "{parent}_id")
    private Parent parent;

    @OneToMany(mappedBy = "{field}", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Child> children = new ArrayList<>();   // ArrayList 초기화 필수

    @Builder
    public {Name}(String field, Parent parent) {
        this.field = field;
        this.parent = parent;
    }

    public void update(String newField) {   // 비즈니스 메서드는 엔티티 안에
        this.field = newField;
    }
}
```

**Entity 규칙 요약**
- `BaseEntity` 상속 → `id`, `createdAt`, `updatedAt` 자동 관리
- `@NoArgsConstructor(access = PROTECTED)` — JPA 전용 기본 생성자
- `@Builder` — 팩토리 생성자에만 적용
- `@ManyToOne` — `fetch = LAZY` 필수
- `@OneToMany` — `cascade = ALL, orphanRemoval = true`, `new ArrayList<>()` 초기화

---

## Repository 패턴

```java
public interface {Name}Repository extends JpaRepository<{Name}, Long> {

    // N+1 방지: 연관 엔티티가 필요한 쿼리는 JOIN FETCH 명시
    @Query("SELECT n FROM {Name} n JOIN FETCH n.parent WHERE n.id = :id")
    Optional<{Name}> findWithParentById(@Param("id") Long id);
}
```

### QueryDSL 커스텀 레포지토리 (infra/ 패키지)

복합 동적 필터(파트·기수·상태 등)가 필요할 때만 추가한다.

```java
@Repository
@RequiredArgsConstructor
public class {Name}QueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<{Name}Response> getList(SomeFilter filter, Pageable pageable) {
        BooleanExpression filterExpr = filter != null
                ? q{Name}.field.eq(filter) : null;
        // null 조건은 where절에서 자동으로 무시됨 — 별도 분기 불필요

        List<{Name}Response> content = queryFactory
                .select(/* ... */)
                .from(q{Name})
                .where(filterExpr)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory.select(q{Name}.count())
                .from(q{Name}).where(filterExpr).fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}
```

---

## Service 패턴

```java
@Service
@RequiredArgsConstructor
public class {Name}CommandService {

    private final {Name}Repository {name}Repository;
    private final UserRepository userRepository;

    @Transactional                          // 데이터 변경 시 필수
    public void create(Long userId, Create{Name}Request req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException({Domain}ErrorCode.USER_NOT_FOUND));

        {Name} entity = {Name}.builder()
                .field(req.getField())
                .author(user)
                .build();

        {name}Repository.save(entity);
    }

    @Transactional(readOnly = true)         // 조회 전용 시 필수
    public {Name}Response get(Long id) {
        {Name} entity = {name}Repository.findById(id)
                .orElseThrow(() -> new NotFoundException({Domain}ErrorCode.NOT_FOUND));
        return {Name}Response.from(entity);
    }
}
```

---

## Controller 패턴

```java
@RestController
@RequestMapping("/api/v1/{resources}")
@RequiredArgsConstructor
public class {Name}Controller {

    private final {Name}CommandService {name}CommandService;
    private final {Name}QueryService {name}QueryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Object> create(
            @AuthenticationPrincipal SnutLikeLionUser user,
            @RequestBody @Valid Create{Name}Request req
    ) {
        {name}CommandService.create(user.getId(), req);
        return ApiResponse.success("{name}이 생성되었습니다.");
    }

    @GetMapping("/{id}")
    public ApiResponse<{Name}Response> get(@PathVariable Long id) {
        return ApiResponse.success({name}QueryService.get(id));
    }
}
```

### 관리자 전용 Controller

```java
@RestController
@RequestMapping("/api/v1/admin/{resources}")
@PreAuthorize("hasRole('ROLE_MANAGER')")        // 관리자 엔드포인트 필수
@RequiredArgsConstructor
public class Admin{Name}Controller {
    // ...
}
```

---

## DTO 패턴

### 요청 DTO (dto/req/)

```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Create{Name}Request {

    @NotNull(message = "필드를 입력해주세요.")
    private String requiredField;

    // 선택 필드 — 검증 어노테이션 없이 선언
    private String optionalField;
}
```

### 응답 DTO (dto/res/)

```java
@Getter
@Builder
public class {Name}Response {

    private Long id;
    private String field;
    private String authorName;

    // 엔티티로부터 변환: static from() 팩토리 메서드
    public static {Name}Response from({Name} entity) {
        return {Name}Response.builder()
                .id(entity.getId())
                .field(entity.getField())
                .authorName(entity.getAuthor().getUsername())
                .build();
    }

    // 엔티티 없이 필드로 직접 생성
    public static {Name}Response of(Long id, String field) {
        return {Name}Response.builder().id(id).field(field).build();
    }
}
```

**DTO 규칙 요약**
- 패키지명: `dto/req/` / `dto/res/` — `request`/`response` 전체 단어 금지
- Request: `@Getter` + `@NoArgsConstructor(access = PROTECTED)` + Bean Validation
- Response: `@Getter` + `@Builder` + `static from(Entity)` 팩토리
- 엔티티를 Controller/Service 바깥에 직접 노출 금지

---

## ErrorCode 패턴

```java
public enum {Domain}ErrorCode implements BaseError {
    NOT_FOUND("{DOMAIN}_NOT_FOUND", "{도메인}을 찾을 수 없습니다."),
    ALREADY_EXISTS("{DOMAIN}_ALREADY_EXISTS", "이미 존재하는 {도메인}입니다.");

    private final String code;
    private final String message;

    {Domain}ErrorCode(String code, String message) {
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
.orElseThrow(() -> new NotFoundException({Domain}ErrorCode.NOT_FOUND))

// 중복 리소스
if (repository.existsBy...()) {
    throw new ExistingResourceException({Domain}ErrorCode.ALREADY_EXISTS);
}

// 비즈니스 규칙 위반
throw new BadRequestException({Domain}ErrorCode.INVALID_STATE);

// 권한 없음
throw new ForbiddenException({Domain}ErrorCode.NO_PERMISSION);
```

---

## 도메인별 특이사항

상세 규칙이 필요하면 해당 파일을 읽어라:

| 도메인 | 참조 파일 | 핵심 내용 |
|--------|----------|---------|
| 인증/JWT | `.claude/domains/auth.md` | 토큰 전략, 비밀번호 재설정, SnutLikeLionUser |
| 모집·지원서 | `.claude/domains/recruitment.md` | 중복 지원 방지, 모집 기간 검증, 기수별 구독 |
| 파일 업로드 | `.claude/domains/file.md` | S3 Presigned URL 4단계 플로우, key 형식 |

---

## 중요 제약사항

- `spring.jpa.open-in-view=false` → 서비스 계층 밖 Lazy 로딩 불가, JOIN FETCH 사용
- `prod` 환경 `ddl-auto=none` → Entity 변경 시 수동 SQL 마이그레이션 필요
- Java 21 전용 기능(Virtual Threads, Record Patterns) 사용 금지
- QueryDSL Q클래스 재생성: Entity 변경 후 `./gradlew compileJava` 실행 필요
- 모든 시크릿은 환경 변수로 주입, `application.yml` 하드코딩 금지
