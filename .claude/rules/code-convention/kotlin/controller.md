---
description: Kotlin Controller 레이어 작성 패턴
paths:
  - "src/main/kotlin/**/controller/**/*.kt"
  - "kotlin-reference/**/controller/**/*.kt"
---

# Kotlin Controller Convention

Java 컨벤션(`../java/controller.md`)의 기본 경로, `{Controller}Docs` 구현, 비즈니스 로직 금지, `@Auth` 주입, `@ParamValidation`/`@EnumValidation` 규칙은 그대로 따른다.
`ResponseEntity` 반환 형식은 Java와 다르게 아래 규칙을 따른다.
문서 내용은 `.claude/spec/api-docs-convention.md`를 따른다.

- `@RestController` 클래스의 주 생성자로 Service를 주입하고 `{Controller}Docs`를 구현하라
- Docs 인터페이스의 함수는 `override fun`으로 구현하라
- 인증된 회원 식별자는 `@Auth memberId: Long`으로 받아라
- `required = false`인 `@RequestParam`, `@RequestHeader`는 nullable 타입으로 받아라

## 요청 이름

- URL에 드러나는 이름(경로, 경로 변수, 쿼리 파라미터, 헤더)은 케밥 케이스로 써라 (`/re-issue`, `{course-id}`, `area-code`, `access-token`)
- 경로 변수와 쿼리 파라미터는 Kotlin 파라미터 이름(카멜 케이스)과 다르므로 이름을 명시하라 (`@PathVariable("course-id") courseId: Long`)

## 함수 형식

- 파라미터는 개수와 상관없이 한 줄에 하나씩 쓰고, 마지막 파라미터 뒤에 trailing comma를 붙여라. 파라미터가 없으면 `()`로 쓴다
- 파라미터 하나에 붙는 어노테이션은 그 파라미터와 같은 줄에 써라 (`@ParamValidation(maxLength = 40) @RequestParam("department") department: String,`)
- 본문은 블록(`{ }`)으로 쓰고, 서비스 호출 결과를 `val response`에 담아 바로 다음 줄에서 반환하라. 두 줄 사이에 빈 줄을 넣지 않는다
- body가 없으면 서비스를 호출한 다음 줄에서 반환하라. 반환 타입은 `ResponseEntity<Void>`다

## 응답

- 응답은 항상 `ResponseEntity.status(상태).body(response)`로 만들어라. body가 없으면 `.status(상태).build()`다
  - `ok()`, `noContent()` 같은 단축 함수를 쓰지 마라. 상태 코드가 모든 함수에서 같은 자리에 보이게 한다
- 상태는 `HttpStatus` 항목을 하나씩 import해서 써라 (`import org.springframework.http.HttpStatus.OK`). 와일드카드 import는 쓰지 않는다

```kotlin
@RestController
@RequestMapping("/api/v1/carts")
class CartController(
    private val cartService: CartService,
) : CartControllerDocs {

    @GetMapping
    override fun getCartedCourse(
        @Auth memberId: Long,
    ): ResponseEntity<CoursesResponse> {
        val response = cartService.getCartedCourse(memberId)
        return ResponseEntity.status(OK).body(response)
    }

    @DeleteMapping("/{course-id}")
    override fun deleteCartedCourse(
        @Auth memberId: Long,
        @PathVariable("course-id") courseId: Long,
    ): ResponseEntity<Void> {
        cartService.deleteCartedCourse(memberId, courseId)
        return ResponseEntity.status(NO_CONTENT).build()
    }
}
```

## Docs 인터페이스

- 배열 인자는 `[...]`로, 중첩 어노테이션은 `@` 없이 작성하라
- 클래스 참조는 `ErrorResponse::class`로 작성하라
- 어노테이션 인자는 컴파일 타임 상수여야 한다. 긴 설명은 `+`로 이어 붙이고 `trimIndent()` 같은 함수 호출을 쓰지 마라
- 함수 선언은 컨트롤러와 같은 형식으로 써라 (파라미터마다 줄바꿈, 파라미터 어노테이션은 한 줄)

```kotlin
@Operation(
    summary = "장바구니 추가",
    description = "특정 과목을 장바구니에 추가합니다.<br>" +
        "🔐 <strong>Jwt 필요</strong><br>"
)
@ApiResponses(
    ApiResponse(responseCode = "200", description = "✅ 장바구니 추가 성공"),
    ApiResponse(
        responseCode = "404",
        description = "🚨 사용자 조회 실패",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = [
                    ExampleObject(
                        name = "사용자 조회 실패",
                        value = "{\"code\" : \"MEM-001\", \"message\" : \"사용자를 찾을 수 없어요.\"}"
                    )
                ],
                schema = Schema(implementation = ErrorResponse::class)
            )
        ]
    )
)
@PostMapping("/{course-id}")
fun addCart(
    @Auth memberId: Long,
    @PathVariable("course-id") courseId: Long,
): ResponseEntity<Void>
```
