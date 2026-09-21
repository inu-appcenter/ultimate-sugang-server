---
description: Kotlin Entity(domain) 클래스와 enum 작성 패턴
paths:
  - "src/main/kotlin/**/domain/**/*.kt"
  - "kotlin-reference/**/domain/**/*.kt"
---

# Kotlin Domain (Entity) Convention

Java 컨벤션(`../java/domain.md`)의 DB 매핑 규칙(`@Table`, `@Column(name = "snake_case")`, 예약어 회피, `@Enumerated(EnumType.STRING)`, `IDENTITY`, `LAZY`)은 그대로 따른다.
시각 필드를 각 Entity에서 직접 관리하고 검증 로직을 Entity 내부 private 함수로 두는 규칙도 같다.

## Entity 클래스

- 일반 `class`로 선언하라. `data class`를 쓰지 마라 (자동 생성되는 `equals`/`hashCode`/`toString`이 지연 로딩 연관관계까지 건드린다)
- JPA용 기본 생성자는 `kotlin-jpa` 플러그인이, 프록시용 `open`은 `allOpen` 설정이 만든다. 코드에 직접 쓰지 마라
- 생성자는 `private constructor`로 감추고 `companion object`의 `create()`로만 생성하라
  - `create()`는 값을 개별 파라미터로 받는다. 파라미터가 많아도 파라미터 객체로 묶지 않는다 (호출은 이름 붙인 인자라 위치가 섞이지 않는다. SonarQube `kotlin:S107`은 이 이유로 두었다)
- 주 생성자 파라미터는 프로퍼티(`val`/`var`)로 선언하지 말고 값만 받아라. 영속 필드는 클래스 본문에 선언한다 (생성자 프로퍼티에는 `protected set`을 지정할 수 없다)

## 영속 필드

- 영속 필드는 `var` + `protected set`으로 선언하라
  - `val`은 final 필드가 되는데, JPA 명세는 영속 필드에 final을 허용하지 않는다
  - `allOpen`으로 프로퍼티가 open이 되므로 `private set`은 컴파일되지 않는다
- 값 변경은 Entity의 도메인 함수로만 하라 (Java의 "Getter만 노출" 계승)
- `@Column(nullable = false)`면 non-null 타입, nullable 컬럼이면 `?` 타입으로 맞춰라
- 식별자는 `var id: Long = 0L`로 선언하라. 0이면 저장 전 상태로 판정된다
- 컬렉션 연관관계는 `MutableList`로 선언하고 `mutableListOf()`로 초기화하라

```kotlin
@Entity
@Table(name = "carts")
class Cart private constructor(
    member: Member,
    course: Course,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "member_id")
    var member: Member = member
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "course_id")
    var course: Course = course
        protected set

    @Column(nullable = false, name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now()
        protected set

    companion object {
        fun create(
            member: Member,
            course: Course,
        ): Cart {
            return Cart(
                member = member,
                course = course,
            )
        }
    }
}
```

## enum

- 값은 `enum class` 주 생성자의 `val` 프로퍼티로 선언하라
- `name` 프로퍼티는 선언할 수 없다 (`Enum.name`과 충돌한다). 표시 이름은 `displayName`으로 선언하라
- `values()` 대신 `entries`를 써라
- 조회 함수는 `companion object`에 둔다. 없을 수 있는 조회는 `Optional` 대신 nullable을 반환하고, 반드시 있어야 하는 조회는 `?: throw`로 작성하라

```kotlin
enum class CourseTerm(
    val code: String,
    val displayName: String,
) {
    FIRST("10", "1학기"),
    SECOND("20", "2학기");

    companion object {
        fun tryFromCode(code: String): CourseTerm? {
            return entries.firstOrNull { it.code == code }
        }

        fun fromCode(code: String): CourseTerm {
            return tryFromCode(code) ?: throw RestApiException(INVALID_ENUM_TYPE)
        }
    }
}
```

## 값 객체

- 여러 값을 묶어 전달하는 도메인 값 객체(Java record)는 `data class`로 선언하라
