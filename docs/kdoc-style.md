# KDoc Style

This project follows the KDoc style used in MaeumGaGym_Backend, adjusted for
TCG Search.

## Class And Interface Comments

Use a short Korean summary first, then add details only when the type owns
meaningful behavior or a project boundary.

```kotlin
/**
 * 카드 도감 조회 유스케이스
 *
 * 카드 식별자와 인쇄본 정보를 기준으로 검색 결과를 구성합니다.
 *
 * @author gurdl0525
 * @since 16-06-2026
 */
interface SearchCardsUseCase
```

## Function Comments

Document public or boundary-facing functions when the name alone does not
explain parameters, returned values, or failure cases.

```kotlin
/**
 * 카드 번호와 이름을 기준으로 외부 판매처 검색 URL을 생성합니다.
 *
 * @param cardNumber 카드 번호
 * @param cardName 카드 이름
 * @return 외부 판매처 검색 URL
 */
fun createSearchUrl(cardNumber: String, cardName: String): String
```

## Tags

- `@param` explains non-obvious parameters.
- `@return` explains returned values when the type is not enough.
- `@throws` documents domain-level failure cases.
- `@see` links related ports, implementations, or value objects.
- `@author` uses the GitHub handle.
- `@since` uses `DD-MM-YYYY`, matching the referenced project.

## Avoid

- Do not restate obvious Kotlin syntax.
- Do not write KDoc for every private helper.
- Do not describe implementation details that can change without affecting
  callers.
