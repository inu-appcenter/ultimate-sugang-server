# Kotlin 마이그레이션 TODO

- 이슈: #126 `refactor: 서버 코드 Kotlin 전환`
- 브랜치: `refactor/126-kotlin-migration`
- 컨벤션: `.claude/rules/code-convention/kotlin/` (전환 기간 규칙은 `interop.md`)
- 새 세션은 이 파일을 먼저 읽고 "현재 상태"부터 이어간다. 작업을 끝낼 때마다 이 파일을 갱신한다

## 현재 상태

- `src/main`은 모두 Kotlin이다 (2026-09-18). 강의 동기화 코드는 #127로 지웠고 Lombok도 걷어냈다
- 테스트(`src/test/java`)는 아직 Java다
- **다음: 테스트 코드 Kotlin 전환.** 새 컨텍스트에서 시작한다
- 여기까지 126 브랜치에 모두 커밋했다. 푸시는 하지 않았다
  - `.claude/skills/implement/SKILL.md`의 한 줄 수정은 마이그레이션과 무관하다. 커밋에서 뺀다

## 역할

- **테스트 전환은 전적으로 클로드가 한다** (2026-09-18 사용자 결정)
  - main 코드에서 쓴 따라치기 방식(참고 파일을 만들고 사용자가 따라 친다)을 쓰지 않는다. `kotlin-reference/`도 만들지 않는다
  - 컨벤션, 구조 변경, 테스트 추가·삭제처럼 사용자가 정할 것은 묻고 진행한다
- 사용자가 Kotlin 문법을 물으면 답한 내용을 루트 `NOTE.md`(gitignore)의 해당 주제에 추가한다
- 커밋, 푸시는 사용자가 요청할 때만 한다

## 남은 작업

### 1. 테스트 코드 Kotlin 전환

- [ ] Kotlin 테스트 컨벤션을 정한다. `.claude/spec/test-convention.md`는 Java 기준이다 (파일명 `{Class}Test.java`, `final` 지역 변수 등)
- [ ] 단위와 순서를 정해 사용자에게 보고한다
- [ ] 옮긴다. 옮긴 뒤 원래 테스트와 같은 것을 검증하는지 확인한다 (테스트 개수, 이름, 단언)
- 규모: 34개 파일, 7476줄
  - 공용: `global/infra`의 `IntegrationTest`, `MySqlIntegrationTest`, `MySqlContainerConfig`, 픽스처 7개(`admin` 2, `course` 2, `cart`, `member`, `registration`)
  - 테스트: `course/domain` 10개, `course` 서비스 2개와 DTO·infra 각 1개, `admin/service` 3개, `auth` 3개, `cart`, `member`, `registration` 서비스 각 1개, `UssServerApplicationTests`
  - Kotlin 테스트 위치는 `src/test/kotlin/uss/code/` 아래 같은 패키지 경로다 (main과 같은 규칙)

### 2. 전환용 장치 제거 (1 뒤에)

- [ ] `interop.md`의 장치를 지운다: `@JvmStatic` 62개, `@JvmRecord` 34개, `@get:JvmName("getName")` 12개, `Optional`을 돌려주는 enum `tryFromCode` 4개(`CourseCollege`, `CourseDepartment`, `CourseDay`, `CourseArea`). 그다음 `interop.md`를 지운다
  - 2026-09-18 스크래치 확인: 장치를 모두 빼도 main은 컴파일된다. 깨지는 곳은 Java 테스트뿐이었다 (21개 파일 374곳: record 접근자 약 200, static 호출 약 110, 메서드 참조 56, `getName()` 12)
  - **지울 때 덤프로 확인한다.** 테스트는 캐시를 끄고 돌아서(`spring.cache.type: none`) 아래를 잡지 못한다
    - `@JvmRecord`를 빼면 `CachedCoursesDto`의 Redis 역직렬화가 깨질 수 있다. `RedisCacheConfig`의 `JacksonJsonRedisSerializer(CachedCoursesDto::class.java)`는 Spring MVC의 매퍼가 아니라 자체 매퍼를 쓴다
    - JSON 키 `isEnglish`, `isNight`, `isClosed`(`CourseResponse`, `CachedCourseDto`)가 유지되는지 확인한다. 지금은 record로 다뤄져 유지된다
- [ ] Java 흔적을 정리할지 사용자와 정한다: `code-convention/java/` 문서, `.claude/CLAUDE.md`의 Java 참조, `kotlin/common.md`의 Lombok 대응표, `build.gradle`의 `id 'java'`

### 3. (선택) `build.gradle` → `build.gradle.kts`

## 참고

- 테스트 실행은 `.claude/CLAUDE.md`의 "macOS에서 테스트 전체 실행" 명령을 백그라운드로 돌린다
  - 기준: 303개, 실패 0, 스킵 6. 스킵은 `CourseServiceSearchTest`(`@Testcontainers(disabledWithoutDocker = true)`)로, 로컬에 Docker가 없으면 스킵된다
  - 시그니처가 바뀌면 먼저 `./gradlew clean`을 돌린다. 증분 컴파일이 옛 시그니처로 컴파일된 테스트 클래스를 다시 만들지 않은 적이 있다
  - javac는 에러를 100개에서 끊는다. 전체를 보려면 `options.compilerArgs += ['-Xmaxerrs', '10000']`을 스크래치 복사본의 `build.gradle`에만 넣는다
- 엔티티 픽스처는 `BeanUtils.instantiateClass(Entity.class)` + `ReflectionTestUtils.setField`로 만든다. `kotlin-jpa`가 만드는 기본 생성자는 소스에서 부를 수 없다
  - Kotlin으로 옮길 때 팩토리(`Course.create` 등)로 바꿀지 정한다. `Course.create`는 main 호출부가 없다 (#127에서 개별 필드 25개로 바꿨다)
- 이전 단위의 검증 방식: 스크래치 복사본(`rsync`, `build`·`.gradle`·`.git` 제외)에서 컴파일, 전체 테스트, 변환 전후 동작 덤프 비교
- 레포가 iCloud로 동기화되는 `~/Desktop` 안에 있다. 브랜치 전환, `clean`, 대량 삭제 뒤에는 `이름 2`, `디렉터리 2` 충돌 사본이 생길 수 있다
  - `find . -path ./.git -prune -o \( -name '* 2' -o -name '* 2.*' \) -print`로 찾아 원본과 같은지 확인하고 지운다. 마이그레이션 사본은 Flyway 중복 버전으로 기동을 막는다
- 셸은 zsh다. `$var`는 단어로 나뉘지 않고 `$(...)`의 결과는 나뉜다. 경로 목록은 변수에 담지 말고 `for f in a/B c/D; do`처럼 직접 나열한다

## 결정 기록 (계속 유효한 것)

- Java `trim()`/`isBlank()`의 공백 기준을 유지한다: `trim { it <= ' ' }`, `all(Character::isWhitespace)` (2026-09-17). Kotlin의 `trim()`/`isBlank()`는 NBSP와 전각 공백도 공백으로 보고, Java `trim()`은 `' '` 이하 문자(제어 문자 포함)만 자른다
- IDE가 커밋된 Kotlin 파일 14개의 import를 와일드카드로 합친 변경은 그대로 둔다. 새 파일은 개별 import로 쓴다 (2026-09-17)
- 토큰 재발급 함수 이름은 `reissue`다. Swagger 경로(`/re-issue`)는 그대로다 (2026-09-17)
- 생성자 주입 파라미터는 계층별로 묶는다. 컨트롤러와 Docs의 파라미터는 개수와 상관없이 한 줄에 하나씩 쓴다. 다른 레이어는 2개 이상이면 줄바꿈한다 (2026-09-17)
- `Course` 생성자는 개별 필드 + `create`, 강의 상태는 `close`만 남긴다(`reopen`, `replaceSchedules` 삭제), 비동기 설정은 없다 (2026-09-18, #127)
