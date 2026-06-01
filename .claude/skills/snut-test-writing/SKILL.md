---
name: snut-test-writing
description: SNUT LikeLion 서버의 서비스 레이어 단위 테스트를 Mockito로 작성할 때 반드시 사용하라. @ExtendWith(MockitoExtension), ReflectionTestUtils로 BaseEntity id 주입, @Value 필드 주입, 예외 검증, 테스트 네이밍 컨벤션 등 이 프로젝트 고유 테스트 패턴을 담고 있다. 테스트 작성, 커버리지 향상, 버그 수정 후 테스트 추가, 기존 테스트 수정 시 이 스킬을 사용하라.
---

# SNUT LikeLion Test Writing

서비스 레이어 Mockito 단위 테스트 작성 가이드.
상세 예시는 `.claude/conventions/test.md`를 참조하라.

---

## 테스트 체크리스트

- [ ] `@ExtendWith(MockitoExtension.class)` 사용 — Spring Context 로드 금지
- [ ] `@Mock` / `@InjectMocks` / `@Spy` 적절히 적용
- [ ] `@BeforeEach`에 공통 픽스처 (User, Entity 생성)
- [ ] `ReflectionTestUtils.setField(entity, "id", 1L)` — BaseEntity id 주입
- [ ] `@Value` 필드: `ReflectionTestUtils.setField(service, "fieldName", value)`
- [ ] 성공 케이스 + 실패(예외) 케이스 양쪽 작성
- [ ] 테스트 네이밍: `{메서드명}_{시나리오}_{기대결과}`
- [ ] `./gradlew test --tests "{TestClassName}"` 실행하여 통과 확인

---

## 기본 구조

```java
@ExtendWith(MockitoExtension.class)
class {ServiceName}Test {

    @Mock {Dependency}Repository {dependency}Repository;
    @Mock UserRepository userRepository;

    @InjectMocks {ServiceName} {serviceName};

    // 공통 픽스처
    User user;
    {Entity} {entity};

    @BeforeEach
    void setup() {
        user = User.builder()
                .email("test@seoultech.ac.kr")
                .username("tester")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);   // BaseEntity id 주입 필수

        {entity} = {Entity}.builder()
                // 필수 필드 설정
                .build();
        ReflectionTestUtils.setField({entity}, "id", 1L);
    }
```

---

## 성공 케이스 패턴

```java
    @Test
    void {methodName}_success() {
        // given
        when({dependency}Repository.findById(1L))
                .thenReturn(Optional.of({entity}));

        // when
        {serviceName}.{methodName}(1L, validRequest);

        // then
        verify({dependency}Repository).save(any({Entity}.class));
    }
```

---

## 실패(예외) 케이스 패턴

```java
    @Test
    void {methodName}_{entity}NotFound_throwsNotFoundException() {
        // given
        when({dependency}Repository.findById(99L))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> {serviceName}.{methodName}(99L, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessage({Domain}ErrorCode.NOT_FOUND.getMessage());
    }

    @Test
    void {methodName}_alreadyExists_throwsExistingResourceException() {
        // given
        when({dependency}Repository.existsBy...()).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> {serviceName}.{methodName}(req))
                .isInstanceOf(ExistingResourceException.class);
    }
```

---

## @Value 필드가 있는 서비스 테스트

Spring Context 없이 `@Value` 필드를 직접 주입한다.

```java
    @BeforeEach
    void setup() {
        // JwtService, 기수 설정 등 @Value 필드
        ReflectionTestUtils.setField(service, "activeProfile", "dev");
        ReflectionTestUtils.setField(service, "currentGeneration", 14);
        ReflectionTestUtils.setField(service, "accessExpiration", 3600000L);
    }
```

---

## Verification 패턴

```java
verify(repository).save(any({Entity}.class));           // 호출 여부
verify(repository, never()).delete(any());              // 미호출 확인
verify(repository, times(2)).findById(anyLong());       // 호출 횟수
```

---

## 예외 검증 패턴

```java
// 예외 타입 + 메시지 동시 검증
assertThatThrownBy(() -> service.method(arg))
        .isInstanceOf(NotFoundException.class)
        .hasMessage(ErrorCode.NOT_FOUND.getMessage());

// 예외 미발생 검증
assertThatCode(() -> service.method(validArg))
        .doesNotThrowAnyException();

// void 메서드에 예외 설정
doThrow(new RuntimeException("S3 오류")).when(mockProvider).upload(any());
```

---

## Stubbing 주의사항

```java
// GOOD: 실제로 호출되는 stubbing만 선언
when(repository.findById(1L)).thenReturn(Optional.of(entity));

// BAD: 실제로 호출되지 않는 stubbing → UnnecessaryStubbingException 발생
when(repository.findAll()).thenReturn(List.of(entity));  // 테스트에서 미사용 시 제거

// GOOD: void 메서드 예외 설정
doThrow(new SomeException()).when(mock).voidMethod();

// BAD: non-void 메서드에 doNothing 사용 → MockitoException
doNothing().when(mock).nonVoidMethod();  // ← 잘못됨
```

---

## 테스트 파일 위치 규칙

```
src/test/java/com/snut_likelion/
├── domain/{domain}/service/{ServiceName}Test.java
│   예) domain/blog/service/BlogCommandServiceTest.java
├── admin/{domain}/service/Admin{ServiceName}Test.java
│   예) admin/notice/service/AdminNoticeServiceTest.java
├── global/auth/jwt/JwtServiceTest.java
└── infra/{module}/{Name}IntegrationTest.java  ← WireMock 통합 테스트
```

---

## 커버리지 목표 및 한계

- **목표**: 서비스 레이어 JaCoCo instruction coverage 90% 이상
- 커버리지 확인: `./gradlew test jacocoTestReport` → `build/reports/jacoco/`
- **알려진 한계**: dead code, private 메서드, `SecurityException` 분기는 커버 불가 — `_workspace/03_test_summary.md`에 명시
