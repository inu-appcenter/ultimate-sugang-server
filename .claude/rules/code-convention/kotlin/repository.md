---
description: Kotlin Repository 레이어 작성 패턴
paths:
  - "src/main/kotlin/**/repository/**/*.kt"
  - "kotlin-reference/**/repository/**/*.kt"
---

# Kotlin Repository Convention

Java 컨벤션(`../java/repository.md`)의 위치, `@Query` 직접 작성(JPQL 기본, DB 종속 쿼리만 `nativeQuery = true`), Entity 반환과 projection 규칙은 그대로 따른다.

- `interface XxxRepository : JpaRepository<Xxx, Long>`으로 선언하라
- 쿼리는 raw string(`"""`)으로 작성하고 `trimIndent()`를 붙이지 마라 (어노테이션 인자는 컴파일 타임 상수여야 한다)
- 파라미터는 `@Param("...")`으로 바인딩하라. Kotlin 파라미터는 기본이 불변이라 `final`에 해당하는 표기는 없다
- 단건 조회는 `Optional<T>` 대신 nullable(`T?`)을 반환하라
- 기본 `findById`는 확장 함수 `findByIdOrNull`(`org.springframework.data.repository.findByIdOrNull`)로 호출하라
- 반환값이 없는 수정 쿼리는 반환 타입을 생략하라
- projection으로 받는 DTO는 `{domain}/dto/internal/`에 `~Dto` 이름으로 둔다 (`dto.md`)

```kotlin
interface CartRepository : JpaRepository<Cart, Long> {
    @Query("""
        SELECT c
        FROM Cart c
        WHERE c.member.id = :memberId
          AND c.course.id = :courseId
    """)
    fun findByMemberIdAndCourseId(
        @Param("memberId") memberId: Long,
        @Param("courseId") courseId: Long,
    ): Cart?
}
```
