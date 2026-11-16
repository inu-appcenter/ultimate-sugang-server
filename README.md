# ultimate-sugang-server

인천대학교 수강신청 시뮬레이터 [ultimate-sugang-client](https://github.com/xunssoie/ultimate-sugang-client)의 **백엔드 서버**.
강의 조회, 장바구니, 수강신청을 실제 수강신청 당일처럼 많은 요청이 몰리는 상황에서도 연습할 수 있도록 제공합니다.

<br>

## 기술 스택

<img src="https://skillicons.dev/icons?i=kotlin,spring,mysql,redis,docker,githubactions&perline=6" alt="Kotlin, Spring Boot, MySQL, Redis, Docker, GitHub Actions" />

JPA · Flyway · JWT · FULLTEXT(ngram) 검색 · springdoc-openapi · Testcontainers · Prometheus

<br>

## 시작하기

JDK 17과 Docker가 필요합니다.

```bash
docker compose -f docker/docker-compose-local.yml up -d     # MySQL(3307), Redis(6379)
./gradlew bootRun --args='--spring.profiles.active=perf'    # 로컬 MySQL, Redis에 붙는 프로필
```

Swagger UI: http://localhost:8080/swagger-ui/index.html

```bash
./gradlew build
./gradlew test       # H2 기반, Redis 필요. MySQL 통합 테스트는 Docker가 있을 때만 실행
```

<br>

## 구조

```
src/main/kotlin/uss/code/
├── auth/             로그인, 회원가입, JWT 발급과 검증
├── member/           회원 정보, 학과 변경
├── course/           강의 조회, 검색, 캐시
├── cart/             장바구니
├── registration/     수강신청, 취소
├── admin/            관리자용 강의, 학기 관리
└── global/           설정, 예외, 필터, 인터셉터

src/main/resources/database/
├── migration/        Flyway 스키마 마이그레이션
└── seed/             강의, 학기 시드 데이터

docker/               로컬, 운영 compose와 로그 수집 설정
.claude/              Claude Code 작업 하네스(규칙, 스펙, 스킬)
```
