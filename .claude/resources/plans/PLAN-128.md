# [PLAN-128] refactor: 캐시와 API 문서 직렬화의 record 의존 제거

> 이슈: #128
> 브랜치: refactor/128-record-serialization

## 목표
Kotlin 모듈이 없는 Jackson 매퍼(Redis 직렬화기의 자체 매퍼, springdoc의 Jackson 2 매퍼)가 `isEnglish` 같은 is-Boolean 프로퍼티를 record라서 우연히 맞게 읽고 있다.
두 매퍼가 Kotlin 프로퍼티 이름을 그대로 쓰게 한 뒤 남은 `@JvmRecord` 3개를 지운다.

## 영향 범위
### 신규 파일
- `src/test/kotlin/uss/code/global/config/RedisCacheConfigTest.kt` — 캐시 직렬화 왕복 테스트 (승인됨)
- `src/test/kotlin/uss/code/global/config/SwaggerConfigTest.kt` — API 문서의 `CourseResponse` 프로퍼티 이름 테스트 (결정 3)

### 수정 파일
- `src/main/kotlin/uss/code/global/config/RedisCacheConfig.kt` — 캐시 직렬화기가 Spring이 만든 매퍼를 쓰게 한다
- `src/main/kotlin/uss/code/course/dto/internal/CachedCourseDto.kt` — `@JvmRecord` 삭제
- `src/main/kotlin/uss/code/course/dto/internal/CachedCoursesDto.kt` — `@JvmRecord` 삭제
- `src/main/kotlin/uss/code/course/dto/response/CourseResponse.kt` — `@JvmRecord` 삭제
- `build.gradle` — Jackson 2용 `jackson-module-kotlin` 추가 (결정 2)

서비스 정책 변화 없음 (직렬화 형식과 API 응답이 그대로다).

## 구현 계획

1. **Config**: `RedisCacheConfig`
   - 주 생성자에 `private val objectMapper: ObjectMapper`(`tools.jackson.databind.ObjectMapper`)를 받는다. Boot가 만든 `JsonMapper` 빈이 주입된다. `FilterChainConfig`, `JwtExceptionFilter`와 같은 타입이다
   - `majorCoursesCacheCustomizer()`, `generalEducationCoursesCacheCustomizer()`의 직렬화기를 `JacksonJsonRedisSerializer(objectMapper, CachedCoursesDto::class.java)`로 바꾼다
2. **DTO**: `CachedCourseDto`, `CachedCoursesDto`, `CourseResponse`의 `@JvmRecord`를 지운다
3. **API 문서**: `build.gradle`의 `// API Specification` 묶음에 `implementation 'com.fasterxml.jackson.module:jackson-module-kotlin'`을 추가한다
   - 버전은 Boot BOM이 관리한다(2.20.1, 이미 있는 Jackson 2 `jackson-databind` 2.20.1과 같다)
   - springdoc의 `SpringDocJacksonKotlinModuleConfiguration`이 켜져 swagger-core의 Jackson 2 매퍼에 `KotlinModule`을 등록한다
   - 부수 효과: 응답 스키마 23개에 `required` 배열이 생긴다(non-null 프로퍼티). Request 스키마의 `required`는 그대로다
4. **테스트**: `RedisCacheConfigTest` (`@IntegrationTest`)
   - 생성자로 `List<RedisCacheManagerBuilderCustomizer>`와 `CourseRepository`를 받는다
   - `RedisCacheManager.builder()`에 커스터마이저를 적용하고 `getCacheConfigurationFor(캐시 이름).valueSerializationPair`를 꺼낸다. 직렬화기를 테스트에서 따로 만들지 않고 `RedisCacheConfig`가 등록한 것을 그대로 쓴다
   - 표본: 저장한 `Course`로 `CachedCourseDto.from(...)`을 만들어 `CachedCoursesDto.of(...)`로 묶는다. `isEnglish`와 `isNight` 값이 서로 달라야 키와 값의 대응이 보인다
   - 시나리오
     - `MAJOR_COURSES`, `GENERAL_EDUCATION_COURSES` 캐시에서 직렬화한 값을 되읽으면 원래 값과 같다
     - 직렬화한 JSON의 강의 항목 키에 `isEnglish`, `isNight`가 있고 `english`, `night`는 없다
5. **테스트**: `SwaggerConfigTest` (`@IntegrationTest`)
   - `WebApplicationContext`로 `MockMvcBuilders.webAppContextSetup(...)`을 만들어 `GET /v3/api-docs`를 호출한다(spring-test만 쓴다. `build.gradle` 무변경)
   - `components.schemas.CourseResponse.properties`에 `isEnglish`, `isNight`, `isClosed`가 있다

## 결정 필요 (Decisions needed)
- [x] 1. Redis 매퍼 — **`RedisCacheConfig` 생성자로 Spring 매퍼 주입** / 직렬화기 전용 매퍼에 `KotlinModule` 등록
  - 근거: 스크래치에서 캐시 JSON이 변경 전과 바이트 단위로 같고 왕복된다. 생성자 주입으로 인한 BeanPostProcessor 경고가 없다
- [x] 2. API 문서 — **Jackson 2용 `jackson-module-kotlin` 추가** / `CourseResponse` 프로퍼티 3개에 `@get:JsonProperty`
  - 근거: 이름과 순서가 그대로다. 앞으로 추가될 is 프로퍼티도 맞게 나온다. `@get:JsonProperty`는 세 프로퍼티 순서를 `isClosed, isNight, isEnglish`로 뒤집는다
  - 받아들인 차이: 응답 스키마 23개의 `required`. 마지막 Java 커밋(`0d71376`)의 문서에도 없던 것으로, 실제 응답과 맞는다
- [x] 3. API 문서 테스트 — **추가** / 추가하지 않음
  - 근거: 추가한 의존성은 코드가 참조하지 않아 지워도 컴파일과 기존 테스트가 통과한다

## 검증
- 전체 테스트: `./gradlew test`를 백그라운드로, Docker를 켠 상태에서. 기준 304개 + 새 테스트, 실패 0
- 스크래치 비교: 스크래치 복사본의 임시 테스트(`SerializationDumpTest`, `@SpringBootTest(RANDOM_PORT)`)로 `dto` 패키지 전 클래스의 MVC JSON과 왕복, 두 캐시 직렬화기의 JSON과 왕복, `/v3/api-docs`를 덤프해 변경 전(`origin/dev`)과 diff한다. 임시 테스트는 커밋하지 않는다
  - 기대 차이: record 여부 3건 + 응답 스키마 `required` 23건
- 회귀 감지: 스크래치에서 `RedisCacheConfig`를 자체 매퍼로 되돌리면 `RedisCacheConfigTest`가, `build.gradle`의 의존성을 빼면 `SwaggerConfigTest`가 실패하는지 본다

## Deviation Log
