---
description: Kotlin Controller 레이어 작성 패턴
paths:
  - "src/main/kotlin/**/controller/**/*.kt"
  - "kotlin-reference/**/controller/**/*.kt"
---

# Kotlin Controller Convention

Java 컨벤션(`../java/controller.md`)의 기본 경로, `{Controller}Docs` 구현, 비즈니스 로직 금지, `@Auth` 주입, `@ParamValidation`/`@EnumValidation`, `ResponseEntity` 반환 규칙은 그대로 따른다.
문서 내용은 `.claude/spec/api-docs-convention.md`를 따른다.

- `@RestController` 클래스의 주 생성자로 Service를 주입하고 `{Controller}Docs`를 구현하라
- Docs 인터페이스의 함수는 `override fun`으로 구현하라
- 인증된 회원 식별자는 `@Auth memberId: Long`으로 받아라
- body 없는 응답의 타입은 `ResponseEntity<Void>`로 선언하라

```kotlin
@RestController
@RequestMapping("/api/v1/carts")
class CartController(
    private val cartService: CartService,
) : CartControllerDocs {

    @GetMapping
    override fun getCartedCourse(@Auth memberId: Long): ResponseEntity<CoursesResponse> =
        ResponseEntity.ok(cartService.getCartedCourse(memberId))

    @DeleteMapping("/{courseId}")
    override fun deleteCartedCourse(
        @Auth memberId: Long,
        @PathVariable("courseId") courseId: Long,
    ): ResponseEntity<Void> {
        cartService.deleteCartedCourse(memberId, courseId)
        return ResponseEntity.noContent().build()
    }
}
```

## Docs 인터페이스

- 배열 인자는 `[...]`로, 중첩 어노테이션은 `@` 없이 작성하라
- 클래스 참조는 `ErrorResponse::class`로 작성하라
- 어노테이션 인자는 컴파일 타임 상수여야 한다. 긴 설명은 `+`로 이어 붙이고 `trimIndent()` 같은 함수 호출을 쓰지 마라

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
@PostMapping("/{courseId}")
fun addCart(
    @Auth memberId: Long,
    @PathVariable("courseId") courseId: Long,
): ResponseEntity<Void>
```
