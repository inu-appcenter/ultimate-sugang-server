---
description: Kotlin 소스 코드를 작성하거나 수정할 때 공통으로 적용되는 컨벤션
paths:
  - "src/main/kotlin/**/*.kt"
  - "kotlin-reference/**/*.kt"
---

# Kotlin Common Convention

Java 컨벤션(`../java/common.md`)을 계승하고, Kotlin 공식 코딩 컨벤션과 겹치는 부분은 이 문서를 따른다.
여기 적지 않은 규칙(레이어 흐름, 에러 코드 체계와 메시지 어투, 네이밍, 필드명 반복 금지, enum 타입명 접두사, 주석)은 Java 컨벤션을 그대로 따른다.

## 소스 위치

- Kotlin 소스는 `src/main/kotlin/uss/code/` 아래에 두고, 패키지 구조는 `project-structure.md`와 같게 유지하라
- 파일 하나에 최상위 클래스 하나를 두고, 파일명은 클래스명과 같게 하라

## 불변과 null

- 기본은 `val`이다. 값이 바뀌어야 할 때만 `var`를 써라 (Java의 `final` 변수는 `val`, `final` 파라미터는 Kotlin 기본 동작이다)
- 타입은 non-null로 선언하고, null이 의미 있는 상태일 때만 `?`를 붙여라
- `!!`를 쓰지 마라. null을 예외로 바꿀 때는 `?: throw`를 쓴다
  - 예외: Bean Validation을 통과한 Request DTO 필드 (`dto.md`)
- Java API가 돌려주는 값(플랫폼 타입)은 받는 변수나 반환 타입에 nullability를 명시해 확정하라

## 예외 처리

- `throw RestApiException(XXX)` 패턴을 사용하라
- `ExceptionCode` 항목은 개별 import로 식별자만 노출하라. Kotlin에는 `import static`이 없고 enum 항목을 바로 import한다
  (`import uss.code.global.exception.domain.ExceptionCode.MEMBER_NOT_FOUND`)
- 와일드카드 import(`*`)를 쓰지 마라
- 비즈니스 검증에 `require()`/`check()`를 쓰지 마라. 이들이 던지는 `IllegalArgumentException`/`IllegalStateException`은 `GlobalExceptionHandler`에서 500으로 떨어진다

```kotlin
val member = memberRepository.findByIdOrNull(memberId)
    ?: throw RestApiException(MEMBER_NOT_FOUND)
```

## 객체 생성

- 도메인 객체(Entity)는 `private constructor` + `companion object`의 팩토리 함수(`create()` / `of()` / `from()`)로만 생성하라
- Response DTO도 팩토리 함수로 생성하라. 단 data class라 생성자는 public으로 둔다 (`dto.md`)
- Lombok `@Builder`의 가독성은 이름 붙인 인자로 대신한다. 인자가 2개 이상인 생성자·팩토리 호출은 이름 붙인 인자로 한 줄에 하나씩 작성하라

```kotlin
fun create(
    member: Member,
    course: Course,
): Cart {
    return Cart(
        member = member,
        course = course,
    )
}
```

## Lombok 대체

Kotlin 코드에서 Lombok을 쓰지 마라.

| Lombok | Kotlin |
|---|---|
| `@Getter` | 프로퍼티 (`val` / `var`) |
| `@RequiredArgsConstructor` | 주 생성자 |
| `@NoArgsConstructor` (Entity) | `kotlin-jpa` 플러그인 |
| `@Builder` | 이름 붙인 인자 |
| `@UtilityClass` | `object` |
| `@Log4j2` | 아래 로깅 규칙 |

## 상수

- 매직넘버·매직스트링은 상수로 선언하라 (Java 규칙 계승)
- 상수는 `companion object` 안에 `private const val`로 선언하라. `object`면 그 본문에 둔다
  - 파일 최상단(클래스 선언 밖)에 두지 마라. 파일을 열었을 때 클래스 선언이 먼저 보여야 한다
  - enum이면 companion 안에서 항목을 이름만으로 참조할 수 있다
- `const`가 불가능한 타입(`DateTimeFormatter`, 컬렉션)은 `private val`로 선언하라
- `companion object` 안에서는 상수와 로거를 먼저, 함수를 뒤에 둔다
- 이름은 `SCREAMING_SNAKE_CASE`로 작성하라

```kotlin
@Service
class CartService(
    private val cartRepository: CartRepository,
) {
    private fun validateCartLimit(carts: List<Cart>) {
        if (carts.size >= MAX_CART_COUNT) {
            throw RestApiException(CARTED_COURSE_LIMIT_EXCEEDED)
        }
    }

    companion object {
        private const val MAX_CART_COUNT = 10
    }
}
```

## 포맷팅

- 들여쓰기는 4칸, 그 밖의 형식은 Kotlin 공식 스타일을 따른다
- 파라미터가 2개 이상인 함수·생성자 선언은 파라미터마다 줄바꿈하고, 마지막 파라미터 뒤에 trailing comma를 붙여라 (Java 규칙 계승 + Kotlin 공식 권장)
  - Controller와 Docs의 함수는 파라미터가 1개여도 줄바꿈한다 (`controller.md`)
- 의존성 주입 파라미터는 계층별로 묶고, 그룹 사이를 빈 줄로 나눠라
  - 같은 계층(Repository끼리, Service끼리)은 한 그룹이다. 도메인이 달라도 나누지 않는다. 그룹 안에서는 자기 도메인을 먼저 쓴다
  - 계층에 속하지 않는 컴포넌트(`JwtProvider`, 인코더, 리졸버, 로더 등)는 따로 한 그룹으로 묶는다
  - 가장 가까운 아래 계층 그룹을 맨 위에 둔다. Service는 Repository 그룹, Controller는 Service 그룹이 먼저다

```kotlin
@Service
class AdminAuthService(
    private val adminRepository: AdminRepository,

    private val jwtProvider: JwtProvider,
    private val passwordEncoder: AdminPasswordEncoder,
)
```
- 함수 본문에서 논리 단계나 처리 대상 도메인이 바뀌면 빈 줄로 구분하라 (Java 규칙 계승)
- 함수 본문은 한 줄이어도 블록(`{ }`)으로 쓰고 `return`으로 반환하라. `=` 표현식 본문을 쓰지 마라. 커스텀 getter도 같다 (`get() { return ... }`)
  - Kotlin 공식 스타일은 한 줄 본문에 `=`를 권하지만, 이 프로젝트는 두 형태가 섞이지 않게 블록으로 통일한다
- 클래스 멤버 순서는 프로퍼티 → `init` 블록 → 부 생성자 → 함수 → `companion object`다 (Kotlin 공식 순서)
- 문자열 연결 대신 문자열 템플릿(`"$value"`, `"${course.code}"`)을 써라

## 함수와 컬렉션

- Java Stream 대신 Kotlin 컬렉션 함수(`map`, `filter`, `any`, `associateBy`, `groupBy`)를 써라
- 외부에 노출하는 컬렉션은 읽기 전용 타입(`List`, `Map`)으로 선언하라
- 분기가 3개 이상이면 `if-else` 체인 대신 `when`을 써라
- 범위 함수(`let`, `apply`, `also`, `run`, `with`)는 null 처리(`?.let`)와 객체 초기화에만 쓰고, 중첩하지 마라
- 상태 없는 유틸리티는 `object`로 선언하라

## 로깅

- 로거는 `companion object`에 Log4j2 API로 선언하라 (Java의 `@Log4j2`와 같은 API)

```kotlin
companion object {
    private val log = LogManager.getLogger(GlobalExceptionHandler::class.java)
}
```

## 스프링 컴포넌트

- 의존성은 주 생성자의 `private val` 파라미터로 주입하라. `lateinit var` 필드 주입을 쓰지 마라
- 스프링 빈 클래스는 `kotlin-spring` 플러그인이 `open`으로 만든다. 직접 `open`을 붙이지 마라
- `@Value`의 `$`는 문자열 템플릿과 겹치므로 `\$`로 이스케이프하라 (`@Value("\${security.jwt.secret-key}")`)
- 설정 묶음은 `@ConfigurationProperties` + data class로 선언하라
