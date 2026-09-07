# [PLAN-123] 학번 로그인 전환과 학적 상태 보강

> 이슈: #123
> 브랜치: feat/123-student-id-login

## 목표
로그인 식별자를 이메일에서 학번으로 바꾸고 학번에 유일 제약을 건다. 함께 학적 상태에 실측된 `유예`를 더한다.

## 사전 확인 (코드 대조 결과)

- `auth.md`가 "학번은 회원 간 중복될 수 있다. 한 학번으로 여러 계정을 만드는 것을 막지 않는다"를 명시하고 있다. **이번 작업은 이 정책을 뒤집는 것**이므로 문서를 함께 고친다
- `saveUniqueEmail`이 미리 검사한 뒤 `DataIntegrityViolationException`도 잡는 2단 구조다. 학번 제약이 생기면 이 catch가 어느 제약을 어겼는지 구분하지 못한다
- **`MemberFixture.createMember()`가 학번을 `20240001`로 고정한다.** 한 테스트에서 회원을 둘 이상 저장하는 곳이 여럿 있어(예: `RegistrationServiceTest`) 유일 제약을 걸면 그대로 깨진다. 이메일이 시퀀스로 유일해진 것과 같은 처리가 학번에도 필요하다
- 회원가입은 학번을 영문자와 숫자 20자 이하로 검증한다. 로그인은 값을 찾기만 하므로 같은 규칙을 다시 걸지 않는다
- 배포 DB에 학번이 겹치는 행이 있으면 마이그레이션이 실패한다. **이 확인은 서버 환경에서만 가능하다**

## 영향 범위

### 신규 파일
- `src/main/java/uss/code/auth/dto/response/StudentIdAvailabilityResponse.java` — 학번 사용 가능 여부
- `src/main/resources/database/migration/V1_15__add_unique_student_id_to_members.sql` — 학번 유일 제약

### 수정 파일
- `src/main/java/uss/code/auth/dto/request/LoginRequest.java` — 이메일을 학번으로 교체
- `src/main/java/uss/code/auth/service/AuthService.java` — 학번으로 로그인, 학번 중복 검사, 학번 사용 가능 여부 조회
- `src/main/java/uss/code/auth/controller/AuthController.java`, `AuthControllerDocs.java` — 학번 사용 가능 여부 엔드포인트 추가, 로그인 설명 갱신
- `src/main/java/uss/code/member/repository/MemberRepository.java` — 학번 조회와 존재 검사
- `src/main/java/uss/code/member/domain/AcademicStatus.java` — `유예` 추가
- `src/main/java/uss/code/global/exception/domain/ExceptionCode.java` — `MEM-005` 추가
- `src/main/java/uss/code/global/http/WhitelistEndpoint.java` — 학번 사용 가능 여부를 인증 예외 경로에 추가

### 정책 문서
- `.claude/spec/service-policy/auth.md` — 로그인 식별자, 학번 중복 금지, 인증 예외 경로
- `.claude/spec/service-policy/member.md` — 학적 상태 값

### 테스트
- `src/test/java/uss/code/member/fixture/MemberFixture.java` — 학번을 시퀀스로 유일하게
- `src/test/java/uss/code/auth/service/AuthServiceTest.java` — 로그인과 중복 검사 재작성
- `src/test/java/uss/code/auth/dto/request/LoginRequestTest.java` — 이메일 형식 검증을 학번 검증으로 교체

## 구현 계획

### 1. Flyway
`V1_15__add_unique_student_id_to_members.sql`
```sql
-- 로그인 식별자가 이메일에서 학번으로 바뀐다. 학번으로 회원을 하나로 특정할 수 있어야 한다.
ALTER TABLE members
    ADD CONSTRAINT uk_student_id UNIQUE (student_id);
```
이 문장은 이미 겹치는 학번이 있으면 실패한다. 배포 전에 중복 행을 확인하고 정리해야 한다.

### 2. Domain
`AcademicStatus`에 `DEFERMENT("유예")` 추가. 학교 화면 실측값만 넣고 `졸업유예`, `수료` 같은 추정 값은 넣지 않는다.

`Member`에 유일 제약을 함께 선언한다.
```java
@Table(
        name = "members",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"email"}),
                @UniqueConstraint(columnNames = {"student_id"})
        }
)
```

### 3. Repository
- `Optional<Member> findByStudentId(final String studentId)`
- `boolean existsByStudentId(final String studentId)`

### 4. DTO
`LoginRequest`
```java
public record LoginRequest(
        @Schema(description = "학번", example = "202012345")
        @NotBlank(message = "학번이 비어있습니다.")
        String studentId,

        @Schema(description = "비밀번호", example = "password1234")
        @NotBlank(message = "비밀번호가 비어있습니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
        String password
) {}
```
학번 형식 검증은 회원가입에만 둔다. 로그인은 값을 찾기만 하고, 형식이 틀리면 회원이 없는 것과 결과가 같다.

`StudentIdAvailabilityResponse(boolean available)` — `EmailAvailabilityResponse`와 같은 형태

### 5. Service
- `AuthService.login` — `findByStudentId`로 회원을 찾는다. 나머지는 그대로
- `AuthService.checkStudentIdAvailability(final String studentId)` — `!existsByStudentId(studentId)`
- `saveUniqueEmail`을 `saveUniqueMember`로 바꾼다
  1. `existsByStudentId`면 `STUDENT_ID_ALREADY_EXISTS`
  2. `existsByEmail`이면 `EMAIL_ALREADY_EXISTS`
  3. `saveAndFlush` 후 `DataIntegrityViolationException`이면 어긴 제약 이름으로 갈라 던진다
  - 학번을 먼저 검사한다. 로그인 식별자가 학번이 되면서 사용자가 먼저 부딪히는 벽이 학번이기 때문이다
  - 미리 검사해도 두 요청이 같은 순간에 들어오면 제약이 먼저 걸린다. 그때 어느 쪽이 겹쳤는지는 제약 이름으로만 알 수 있으므로, `ConstraintViolationException.getConstraintName()`을 읽어 `uk_student_id`면 학번, 그 밖은 이메일로 본다. 이름을 읽지 못하면 이메일로 본다

### 6. Controller
`GET /api/v1/auth/student-id-availability?student-id=` 추가. `@ParamValidation(maxLength = 20)`을 붙이고 `EmailAvailabilityResponse` 쪽과 같은 형태로 둔다.
`WhitelistEndpoint`에 `new EndPoint("/api/v1/auth/student-id-availability", HttpMethod.GET)` 추가.

### 7. 전역
`ExceptionCode`에 `STUDENT_ID_ALREADY_EXISTS(CONFLICT, "MEM-005", "이미 사용 중인 학번이에요.")` 추가. `EMAIL_ALREADY_EXISTS`가 `CONFLICT`이므로 같은 상태를 쓴다.

### 8. Fixture
`MemberFixture`에 학번 시퀀스를 둔다. 이메일이 `EMAIL_SEQUENCE`로 유일해진 것과 같은 방식이다.
```java
private static final AtomicLong STUDENT_ID_SEQUENCE = new AtomicLong();

public static Member createMember() {
    return createMember(nextStudentId(), "홍길동", ...);
}
```
학번을 직접 넘기는 오버로드는 그대로 둔다. 학번이 중요한 테스트는 값을 지정해 쓴다.

## 결정 필요 (Decisions needed)
- [x] 학번 형식 검증은 회원가입에만 둔다 — 로그인은 조회일 뿐이고, 형식이 틀리면 회원이 없는 것과 결과가 같다
- [x] 회원가입에서 학번을 이메일보다 먼저 검사한다 — 로그인 식별자가 학번이 되면서 사용자가 먼저 부딪히는 벽이 학번이다
- [x] 이메일 유일 제약은 유지한다 — 회원가입에 계속 쓰이고, 로그인이 학번 하나로만 이뤄지므로 식별자가 둘이어도 충돌하지 않는다

## 검증
- `AuthServiceTest` — 학번으로 로그인 성공, 없는 학번이면 `MEM-001`, 비밀번호 불일치면 `MEM-002`, 학번이 겹치면 가입 실패(`MEM-005`), 이메일이 겹치면 `MEM-003`, 학번과 이메일이 모두 겹치면 학번 사유가 먼저, 학번 사용 가능 여부 조회
- `LoginRequestTest` — 학번이 비면 검증 실패, 이메일 형식 검증이 사라졌는지
- `MemberServiceTest` — 학적 상태 `유예`가 프로필에 그대로 내려가는지
- 유일 제약이 실제로 걸리는지는 `AuthServiceTest`의 중복 가입 시나리오가 대신한다
- 전체: `.claude/CLAUDE.md`의 macOS 전체 실행 명령

## Deviation Log
- `AuthServiceTest`: `createSignUpRequest(String, String)`이 이미 (단과대학, 학과)라 이메일과 학번을 받는 헬퍼를 `createSignUpRequestOf`로 따로 뒀다 — 이유: 같은 시그니처로 오버로드하면 컴파일은 되고 뜻만 뒤바뀌어, 나중에 읽는 사람이 어느 쪽인지 알 수 없다
- `AuthServiceTest.이미_사용_중인_이메일이면_예외를_반환한다`: 학번을 다른 값으로 바꿔 다시 가입하도록 고쳤다 — 이유: 같은 요청을 두 번 보내면 이제 학번이 먼저 걸려 이메일 사유가 검증되지 않는다
- `MemberFixture.nextStudentId()`를 공개 메서드로 뒀다 — 이유: 학적 상태처럼 다른 필드를 지정해 회원을 만드는 테스트도 학번은 유일해야 한다
