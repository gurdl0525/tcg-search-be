# TCG Search Kopring Code Conventions

이 문서는 TCG Search 백엔드에서 Kotlin + Spring Boot 코드를 작성할 때의 기준이다.
새 코드는 Java 관성보다 Kotlin 언어 기능과 Spring의 Kotlin 지원을 우선한다.

## Priority

- Kotlin-first style을 우선한다. `runApplication<TcgSearchApplication>(*args)`, primary constructor injection, null-safety, `data class` DTO, expression body, named argument를 자연스럽게 사용한다.
- Java식 패턴은 Spring/JPA 제약이 있을 때만 사용한다. Field injection, static utility class, JavaBean setter 중심 DTO, `Optional`, 불필요한 builder 패턴은 피한다.
- 현재 패키지 구조를 유지한다. 기능 코드는 `domain.<feature>`, 공통 인프라는 `global.*`에 둔다.
- Flyway migration을 DB contract로 보고 JPA는 `ddl-auto: validate` 기준으로 맞춘다.
- 인증/보안 코드는 native app + Bearer JWT + refresh token rotation 전제를 유지한다.

## Package Layout

현재 코드베이스의 기본 구조는 다음과 같다.

```text
com.tcgsearch
├── domain
│   └── <feature>
│       ├── controller
│       ├── dto
│       │   ├── request
│       │   └── response
│       ├── entity
│       ├── repository
│       └── service
└── global
    ├── annotation
    ├── property
    ├── querydsl
    ├── security
    └── util
```

- `controller`는 HTTP adapter 역할만 한다. 인증 주체 추출, validation, status mapping 외의 도메인 흐름은 service로 넘긴다.
- `service`는 transaction boundary와 유스케이스 흐름을 소유한다.
- `repository`는 persistence 접근만 담당한다. 복잡한 조회는 custom repository 또는 QueryDSL 구현으로 분리한다.
- `entity`는 JPA 제약 때문에 `data class`로 만들지 않는다.
- `dto.request`와 `dto.response`는 `data class`를 기본값으로 사용한다.
- `global.annotation`의 stereotype annotation은 중복 Spring annotation을 감추는 용도로만 사용한다.
- `global.property`는 `@ConfigurationProperties` + immutable `data class` + `val`을 기본값으로 사용한다.

## Kotlin Style

- 들여쓰기는 4 spaces를 사용하고 탭을 쓰지 않는다.
- 한 파일에는 같은 책임의 선언만 둔다. 단일 class/interface 파일명은 타입명과 맞춘다.
- 간단한 단일 표현식 함수는 expression body를 사용한다.
- multi-line constructor/function call에는 trailing comma를 사용한다.
- nullable은 타입으로 표현하고, `!!`는 Spring/JPA 바인딩 경계처럼 검증이 선행된 경우를 제외하면 피한다.
- `takeIf`, `also`, `apply`, `let`, `run`은 흐름이 즉시 읽힐 때만 사용한다. 예외 처리나 transaction 흐름이 감춰지면 명시적인 `if`/`when`을 우선한다.
- collection 처리에는 Kotlin 표준 library를 우선한다. Java Stream API는 기존 Java API와 직접 맞물릴 때만 사용한다.
- `companion object`는 상수, factory, logger처럼 타입에 붙어야 하는 값에만 둔다.
- public modifier는 생략한다. `private`, `internal`은 의도를 드러낼 때 명시한다.

## Spring Style

- dependency는 primary constructor로 주입한다. `lateinit var` field injection은 테스트 fixture처럼 Spring이 직접 주입해야 하는 경우로 제한한다.
- `@ConfigurationProperties`는 mutable bean 대신 immutable `data class`로 작성한다.
- Spring Security는 `SecurityFilterChain` bean과 Kotlin-friendly lambda DSL을 사용한다. `WebSecurityConfigurerAdapter`식 패턴은 쓰지 않는다.
- Controller method는 request DTO를 받고 response DTO를 반환한다. Entity를 직접 반환하지 않는다.
- Validation annotation은 request DTO의 property에 `@field:` target을 명시한다.
- Transaction은 service layer에 둔다. read query는 `@Transactional(readOnly = true)`, write/rotation 흐름은 필요한 isolation을 명시한다.
- custom annotation은 의미가 선명할 때만 만든다. 단순 alias가 아니라 팀 규칙을 고정하는 annotation이어야 한다.

## Persistence

- JPA Entity는 `class`로 작성하고, 생성자에는 필수 domain field만 둔다.
- Entity ID는 nullable `var id: UUID? = null` 형태를 유지한다.
- DB schema 변경은 Flyway migration을 먼저 작성한다.
- Repository method명 query는 단순 조회에만 쓴다. 조건이 늘어나면 QueryDSL custom repository로 이동한다.
- refresh token 원문은 저장하지 않는다. hash만 저장하고 token family 기반 rotation/revoke를 유지한다.
- DB time column은 `Instant`를 사용한다.

## API And Security

- 외부 JSON은 snake_case를 유지하되 Kotlin property는 camelCase를 사용한다.
- 인증은 stateless Bearer JWT를 기준으로 한다. 서버 session에 의존하지 않는다.
- Refresh token은 native app secure storage를 전제로 하며 서버에는 hash만 저장한다.
- CSRF는 cookie/session 기반 인증이 아니므로 API security chain에서 disable한다.
- CORS origin/method/header는 `app.security.cors.*` configuration으로만 바꾼다.
- Swagger/OpenAPI와 Actuator public endpoint는 security 설정에서 명시적으로 permit한다.

## KDoc

KDoc은 `docs/kdoc-style.md`를 따른다.

- public boundary class/interface/controller/service/entity/custom annotation에는 KDoc을 작성한다.
- private helper에는 기본적으로 KDoc을 쓰지 않는다. 이름만으로 의도가 부족한 복잡한 계산이나 security-sensitive helper만 예외로 한다.
- 첫 문장은 한국어 요약으로 작성한다.
- `@param`, `@return`은 타입을 반복하지 말고 caller가 알아야 할 의미를 적는다.
- `@throws`는 domain/security 실패처럼 호출자가 의사결정해야 하는 예외에만 작성한다.
- `@author gurdl0525`, `@since DD-MM-YYYY` 형식을 유지한다.

좋은 예시는 다음과 같다.

```kotlin
/**
 * 리프레시 토큰을 회전하고 새 토큰 쌍을 발급합니다.
 *
 * 기존 토큰이 이미 회전되었거나 만료된 경우 같은 token family를 폐기합니다.
 *
 * @param rawRefreshToken 클라이언트가 보낸 원문 refresh token
 * @return 새 access token과 refresh token
 * @throws org.springframework.web.server.ResponseStatusException 토큰을 사용할 수 없는 경우
 */
fun rotateRefreshToken(rawRefreshToken: String): TokenResponse
```

피해야 할 예시는 다음과 같다.

```kotlin
/**
 * 리프레쉬 토큰 생성
 * @return [String]
 */
private fun generateRefreshToken(): String
```

## Tests

- API/security 변경은 `MockMvc` + `springSecurity()`로 검증한다.
- persistence/Flyway 변경은 Testcontainers PostgreSQL로 검증한다.
- test method명은 Kotlin backtick style을 사용해 행위를 문장으로 표현한다.
- 새 migration은 대표 테이블/컬럼 smoke test를 같이 갱신한다.
- 완료 전 기본 검증은 `./gradlew test`다. 문서/skill만 바꾼 경우에는 skill validation과 파일 검토로 충분하다.

## References

- Kotlin coding conventions: https://kotlinlang.org/docs/coding-conventions.html
- KDoc syntax: https://kotlinlang.org/docs/kotlin-doc.html
- Spring Framework Kotlin support: https://docs.spring.io/spring-framework/reference/languages/kotlin.html
- Spring Boot Kotlin support: https://docs.spring.io/spring-boot/reference/features/kotlin.html
