---
description: Kotlin Service 레이어 작성 패턴
paths:
  - "src/main/kotlin/**/service/**/*.kt"
  - "kotlin-reference/**/service/**/*.kt"
---

# Kotlin Service Convention

Java 컨벤션(`../java/service.md`)의 단일 도메인 책임, 다른 도메인은 Repository로 접근, Query/Command 네이밍 규칙은 그대로 따른다.

- `@Service` 클래스의 주 생성자로 의존성을 주입하라 (`@RequiredArgsConstructor` 대체)
- 조회 함수에는 `@Transactional(readOnly = true)`를, 변경 함수에는 `@Transactional`을 붙여라
- 반환값이 없는 함수는 반환 타입을 생략하라
- 조회 결과가 없을 때의 예외는 `?: throw RestApiException(XXX)`로 처리하라

```kotlin
@Service
class CartService(
    private val cartRepository: CartRepository,
    private val courseRepository: CourseRepository,
    private val memberRepository: MemberRepository,
) {
    @Transactional
    fun deleteCartedCourse(
        memberId: Long,
        courseId: Long,
    ) {
        val cart = cartRepository.findByMemberIdAndCourseId(memberId, courseId)
            ?: throw RestApiException(CARTED_COURSE_NOT_FOUND)

        decreaseCartCount(courseId)

        cartRepository.delete(cart)
    }
}
```
