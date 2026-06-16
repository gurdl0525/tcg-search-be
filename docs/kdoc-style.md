# KDoc Style

TCG Search는 MaeumGaGym_Backend의 문서화 톤을 참고하되, Kotlin/Spring
백엔드에서 실제 호출자가 알아야 할 정보만 KDoc으로 남긴다.

## Scope

KDoc을 작성하는 대상은 다음으로 제한한다.

- public controller, service, repository interface, entity, custom annotation
- 외부 설정을 표현하는 `@ConfigurationProperties` class
- security, token, transaction처럼 실패 조건이 중요한 public method
- 이름만으로 의도가 드러나지 않는 domain rule

private helper에는 기본적으로 KDoc을 작성하지 않는다. 단, 보안 로직이나
계산 규칙이 외부 규칙을 반영할 때만 짧게 남긴다.

## Class And Interface Comments

짧은 한국어 요약을 첫 문단에 둔다. 타입이 의미 있는 경계나 정책을 소유할 때만
두 번째 문단에 상세 설명을 추가한다.

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

public 또는 boundary-facing function 중 parameter 의미, 반환값, 실패 조건이
이름만으로 충분히 드러나지 않는 경우에 작성한다.

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

- `@param`은 non-obvious parameter의 의미를 설명한다. 타입 이름을 반복하지 않는다.
- `@return`은 반환 타입만으로 부족한 의미를 설명한다.
- `@throws`는 domain/security 실패처럼 호출자가 처리 전략을 알아야 하는 경우에만 쓴다.
- `@see`는 관련 port, implementation, value object를 연결할 때 쓴다.
- `@author`는 GitHub handle을 사용한다.
- `@since`는 참고 프로젝트와 맞춰 `DD-MM-YYYY`를 사용한다.

## Tone

- 첫 문장은 동사형 한국어 문장으로 끝낸다.
- "합니다" 톤을 유지한다.
- "리프레시", "액세스", "토큰"처럼 한글 표기를 프로젝트 안에서 일관되게 쓴다.
- Markdown link는 KDoc symbol link인 `[TokenResponse]`처럼 작성한다.
- 구현 세부사항보다 caller contract를 설명한다.

## Avoid

- obvious Kotlin syntax를 반복하지 않는다.
- private helper마다 KDoc을 쓰지 않는다.
- caller contract와 무관한 구현 세부사항을 설명하지 않는다.
- `@param rawRefreshToken [String]`, `@return [Boolean]`처럼 타입만 반복하지 않는다.
- 임시 의문 주석이나 `// ?`를 남기지 않는다.
