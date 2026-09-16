---
description: Kotlin DTO(Request/Response) 클래스 작성 패턴
paths:
  - "src/main/kotlin/**/dto/**/*.kt"
  - "kotlin-reference/**/dto/**/*.kt"
---

# Kotlin DTO Convention

Java 컨벤션(`../java/dto.md`)의 대상 이름 반복 금지, 같은 값 같은 이름, 목록 응답 `{단수형}Responses` 규칙은 그대로 따른다.
패키지는 `request/`, `response/`로 나누고, Java의 `common/`은 Kotlin에서 `internal/`로 바꾼다.

- DTO는 `data class`로 선언하고 프로퍼티는 모두 `val`로 선언하라 (Java record 대응)
- 생성자는 public으로 둔다. data class에 `private constructor`를 쓰면 `copy()`가 생성자를 우회하는 통로가 되어 컴파일러가 지적한다

## 프로퍼티 사이 빈 줄

- `request/`, `response/`는 프로퍼티 사이를 빈 줄로 구분하라. 프로퍼티마다 `@Schema`와 validation 어노테이션이 붙어서 붙여 쓰면 경계가 보이지 않는다 (Java 규칙 계승)
- `internal/`은 어노테이션이 붙지 않으니 빈 줄 없이 붙여 써라

## 프로퍼티 어노테이션

- 주 생성자 프로퍼티에 붙이는 `@Schema`와 validation 어노테이션은 `@field:` 대상을 명시하라 (명시하지 않으면 생성자 파라미터에만 붙어 검증이 무시될 수 있다)
- `@Schema` 속성이 2개 이상이면 속성당 한 줄로 작성하라 (Java 규칙 계승)

## Request

- `@Schema` + validation 어노테이션을 포함하라 (Java 규칙 계승)
- 프로퍼티는 nullable로 선언하라. non-null로 두면 필드가 빠진 요청이 validation 전에 역직렬화에서 실패해, `@NotBlank`의 메시지 대신 일반 `INVALID_REQUEST_PARAMETER` 응답이 나간다 (Java record와 응답이 달라진다)
- 검증을 통과한 필드를 꺼낼 때에 한해 `!!`를 허용한다

```kotlin
data class LoginRequest(
    @field:Schema(
        description = "학번",
        example = "202012345"
    )
    @field:NotBlank(message = "학번이 비어있습니다.")
    val studentId: String?,

    @field:Schema(
        description = "비밀번호",
        example = "password1234"
    )
    @field:NotBlank(message = "비밀번호가 비어있습니다.")
    @field:Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
    val password: String?,
)
```

## Response

- 프로퍼티는 실제 값에 맞춰 non-null로 선언하라
- validation 어노테이션을 붙이지 마라 (Java 규칙 계승)
- 생성은 `companion object`의 `of()` / `from()`으로 하고, 그 안에서 이름 붙인 인자로 생성자를 호출하라

```kotlin
data class CoursesResponse(
    val courseResponses: List<CourseResponse>,
) {
    companion object {
        fun of(courseResponses: List<CourseResponse>): CoursesResponse =
            CoursesResponse(courseResponses = courseResponses)
    }
}
```

## internal

Java의 `{domain}/dto/common/`을 대체한다. 여러 조회에서 공유하거나 Repository projection·캐시로 쓰는, 클라이언트에 그대로 나가지 않는 DTO를 둔다.

- 클래스 이름은 `~Dto`로 끝내라 (`CachedCourseDto`, `CourseCapacityDto`). `request/`, `response/`의 DTO에는 붙이지 마라
- JPQL 생성자 표현식(`SELECT new ...`)으로 받는 DTO는 생성자 파라미터의 타입과 순서를 select 절과 맞춰라. 클래스를 옮기면 쿼리 문자열의 패키지·클래스 이름도 함께 고쳐라
- Java에 남은 `dto/common/` 파일은 이름을 바꾸지 마라. Kotlin으로 옮기는 단위에서 `internal/`로 옮기고 `~Dto`를 붙인다
