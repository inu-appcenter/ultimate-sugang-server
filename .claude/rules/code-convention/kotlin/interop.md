---
description: Java와 Kotlin이 공존하는 마이그레이션 기간에만 적용하는 상호운용 규칙. 전환 완료 후 이 파일과 해당 코드를 제거한다
paths:
  - "src/main/kotlin/**/*.kt"
  - "kotlin-reference/**/*.kt"
---

# Kotlin ↔ Java 전환 기간 규칙

아직 Java로 남은 코드가 Kotlin으로 옮긴 코드를 호출하는 동안에만 쓰는 규칙이다.
여기 적힌 장치는 호출부가 모두 Kotlin이 되면 제거한다. 최종 형태의 기준은 다른 Kotlin 컨벤션 문서다.

`src/main`의 Java 코드는 모두 옮겨졌다. 지금 남은 Java 호출부는 테스트 코드(`src/test/java`)다.
장치는 테스트를 Kotlin으로 옮긴 뒤 제거한다.

| 상황 | 전환 기간 처리 | 호출부가 Kotlin이 된 뒤 |
|---|---|---|
| Java가 `companion object`·`object` 함수를 `Type.method()`로 호출한다 | 함수에 `@JvmStatic` | 제거 |
| Java가 record 접근자(`request.term()`)로 DTO 필드를 읽는다 | 클래스에 `@JvmRecord` | 제거 |
| Java가 반환값을 `Optional`로 다룬다 (`.orElseThrow(...)`) | 반환 타입 `Optional<T>` 유지 | `T?`로 변경 |
| 이름이 바뀐 프로퍼티(enum `name` → `displayName`)를 Java가 `getName()`으로 호출한다 | `@get:JvmName("getName")` | 제거 |
| Java가 null을 넘길 수 있다 (외부 API 응답 값 등) | 파라미터 타입 `T?` | 실제 호출부에 맞춰 재검토 |

- Kotlin 전환으로 컴파일이 깨지는 Java 호출부(예: Lombok 빌더 호출, `long`이 된 식별자에 `.equals()` 호출)는 같은 작업 단위에서 최소한으로 수정하라
  - Java 테스트 픽스처의 `new Entity()`는 `BeanUtils.instantiateClass(Entity.class)`로 바꾼다. `kotlin-jpa`가 만드는 기본 생성자는 소스에서 부를 수 없고 리플렉션으로만 부를 수 있다. 테스트를 Kotlin으로 옮길 때 다시 정한다
