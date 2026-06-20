# ONE PIECE CARD GAME 공식 사이트 크롤링 데이터 분석

작성일: 2026-06-19

## 요약

ONE PIECE CARD GAME 공식 사이트에서 수집 가능한 데이터는 크게 다섯 소스로 나뉜다.

1. `CARD LIST`: 카드 단위의 정규 데이터. 도감 검색의 1차 소스.
2. `PRODUCTS`: 상품/세트 단위 메타데이터. 출시일, MSRP, 수록 타입, 레어도 용어 보강.
3. `RULE / PLAY GUIDE`: 카드 필드 정의와 덱 구성 규칙. DB enum/검증 규칙의 근거.
4. `RECOMMENDED DECKS`: 공식 추천 덱/전략 문서. 덱 빌더의 샘플 데이터나 추천 콘텐츠 후보.
5. `NEWS / TOPICS`: 에라타, 금지/제한, 금지 페어. 운영성 데이터의 소스.

현재 앱 목적이 `도감 + 컬렉션 + 덱 빌더 + 외부 판매 링크 연결`이라면, 크롤러의 핵심은
`CARD LIST`를 중심으로 `card_identity`와 `card_printing`을 분리해서 저장하는 것이다.
같은 카드 번호가 일반판, 패러렐/Alt-Art, TR, SP 등으로 여러 번 나타날 수 있기 때문이다.

## 분석 대상

| 소스 | URL 예시 | 주요 역할 |
| --- | --- | --- |
| 카드 리스트 | `https://en.onepiece-cardgame.com/cardlist/?series=569116` | 카드 번호, 이름, 레어도, 타입, 효과 등 카드별 원천 데이터 |
| 플레이 가이드 | `https://en.onepiece-cardgame.com/play-guide/` | 카드 종류, 덱 구성, 게임 규칙 요약 |
| 룰 매뉴얼 PDF | `https://en.onepiece-cardgame.com/pdf/rule_manual.pdf?20230623=` | 카드 정보 필드 정의와 덱 규칙 |
| 상품 상세 | `https://en.onepiece-cardgame.com/products/op16.html` | 세트명, 출시일, MSRP, 수록 타입/레어도 |
| 추천 덱 | `https://en.onepiece-cardgame.com/feature/deck/?tags=OP-16` | 공식 추천 덱 목록, 날짜, 리더 색상, 덱 콘셉트 |
| 금지/제한 공지 | `https://en.onepiece-cardgame.com/news/restriction.html` | 금지 카드, 제한 카드, 금지 페어, 적용일 |
| 에라타 공지 | `https://en.onepiece-cardgame.com/news/notice-op16.html` | 카드번호별 수정 전/후 효과 텍스트 |

## 소스별 크롤링 가능 데이터

### 1. CARD LIST

가장 중요한 1차 소스다. 카드 리스트 페이지는 카드가 반복 블록으로 렌더링되어 있고,
각 카드 블록에서 아래 데이터를 추출할 수 있다.

| 필드 | 예시 | 수집 안정성 | 비고 |
| --- | --- | --- | --- |
| 카드 번호 | `OP16-001` | 높음 | 같은 번호의 여러 프린팅이 존재할 수 있음 |
| 레어도 코드 | `L`, `C`, `UC`, `R`, `SR`, `SEC`, `TR`, `SP CARD` | 높음 | `SP CARD`처럼 공백 포함 코드 정규화 필요 |
| 카드 카테고리 | `LEADER`, `CHARACTER`, `EVENT`, `STAGE` | 높음 | DON!! 카드는 상품 상세/이미지 쪽 보강 필요 |
| 카드명 | `Portgas.D.Ace` | 높음 | 언어/지역별 표기 차이 가능 |
| Life | `5` | 높음 | Leader 전용 |
| Cost | `1`, `8`, `-` | 높음 | Character/Event/Stage 중심, `-`는 null 처리 |
| Attribute | `Slash`, `Strike`, `Ranged`, `Special`, `Wisdom`, `-` | 높음 | Event/Stage는 대개 `-` |
| Power | `5000`, `10000`, `-` | 높음 | Leader/Character 중심 |
| Counter | `1000`, `2000`, `-` | 높음 | Character 중심 |
| Color | `Red`, `Black/Yellow`, `Multicolor` | 높음 | `/` 기준 복수 색상 분해 필요 |
| Block icon | `1`, `2`, `3`, `4`, `5`, `X` | 높음 | 공식 필터에도 존재 |
| Type | `Whitebeard Pirates`, `Navy`, `The Four Emperors/Red-Haired Pirates` | 높음 | `/` 기준 복수 타입 분해 필요 |
| Effect | `[On Play] ...`, `[Blocker]` | 높음 | 줄바꿈과 키워드 토큰 보존 필요 |
| Trigger | `[Trigger] Draw 2 cards.` | 높음 | 없는 카드는 null |
| Card Set(s) | `-THE TIME OF BATTLE- [OP-16]` | 높음 | 재록 카드는 복수 세트 가능성 고려 |
| 카드 이미지 | 이미지 URL, alt text | 중간 | 공식 이미지 재사용은 권리 검토 필요 |

카드 리스트의 필터 영역에서도 아래 기준값을 얻을 수 있다.

| 필터 | 값 |
| --- | --- |
| Color | Red, Green, Blue, Purple, Black, Yellow, Multicolor |
| Card type | Leader, Character, Stage, Event |
| Illustration Type | Comic, Animation, Original Illustrations, Other |
| Block icon | 1, 2, 3, 4, 5, X |
| Recording/Product | OP, ST, EB, PRB, Promotion card, Other Product Card 등 |

#### CARD LIST 정규화 포인트

- 카드 번호는 공식 표기 그대로 저장한다. 예: 카드 번호 `OP16-001`, 상품 코드 `OP-16`은 다른 개념이다.
- `Color`와 `Type`은 단일 문자열로 수집되지만, 검색/덱 검증을 위해 다대다 테이블로 분리하는 편이 좋다.
- `-`는 값 없음으로 정규화한다.
- `Effect`와 `Trigger`는 원문 문자열을 보존하고, 키워드 검색용으로 별도 토큰화 필드를 추가한다.
- 같은 카드 번호가 여러 레어도/이미지로 반복될 수 있으므로 카드 번호 단위 identity와 이미지/레어도 단위 printing을 분리한다.

### 2. PRODUCTS

상품 상세 페이지는 카드 리스트만으로 부족한 세트 메타데이터를 보강한다.

| 필드 | 예시 | 수집 안정성 | 비고 |
| --- | --- | --- | --- |
| 상품 코드 | `OP-16` | 높음 | URL/제목에서 추출 |
| 상품명 | `BOOSTER PACK -THE TIME OF BATTLE- [OP-16]` | 높음 | 세트 표시명 |
| 상품 종류 | Booster, Starter Deck, Extra Booster, Premium Booster 등 | 높음 | URL/제목/본문에서 추출 |
| 출시일 | `June 12, 2026` | 높음 | 지역별 차이 가능 |
| MSRP | `USD $4.99 per pack` | 중간 | 지역/통화 차이 가능 |
| 수록 수 | `126+1 types in total` | 높음 | 실제 카드 수 검증에 사용 |
| 레어도 설명 | Commons, Uncommons, Rares, Super Rares, Secret Rares, Leaders, Specials, Treasure Rare, DON!! Card | 높음 | rarity seed 데이터 근거 |
| 추천 덱 링크 | OP-16 추천 덱 목록 | 높음 | 공식 콘텐츠 연결 |
| 토너먼트 적법 시점 | 출시 후 7일 등 | 중간 | 지역/이벤트 조건 주의 |

상품 페이지는 카드별 상세 데이터보다 세트 단위 보강에 적합하다.
특히 `Specials`, `Treasure Rare`, `DON!! Card` 같은 레어도/수록 타입 용어는
상품 상세에서 더 명확하게 나온다.

### 3. RULE / PLAY GUIDE / RULE MANUAL

룰 문서는 크롤링 대상이라기보다 DB 제약과 덱 빌더 검증 규칙의 기준이다.

| 데이터 | 내용 | 활용 |
| --- | --- | --- |
| 카드 종류 | Leader, Character, Event, Stage, DON!! | enum/검증 규칙 |
| 필요 카드 | Leader 1장, 50장 덱, DON!! 10장 | 덱 빌더 검증 |
| 메인 덱 구성 | Character/Event/Stage | 덱 빌더 검증 |
| 색상 제한 | 리더 카드 색상에 포함된 카드만 덱에 포함 가능 | 덱 빌더 검증 |
| 같은 카드 번호 제한 | 같은 카드 번호 최대 4장 | identity 기준 수량 검증 |
| Leader 필드 | Life, Power, Effect, Type 등 | 카드 타입별 nullable 정책 |
| Character 필드 | Cost, Power, Counter, Attribute, Trigger 등 | 카드 타입별 nullable 정책 |
| Event/Stage 필드 | Cost, Effect, Type, Trigger 등 | 카드 타입별 nullable 정책 |
| DON!! 규칙 | 비용 지불, Leader/Character에 부여, 턴 중 +1000 | DON!! 카드 처리 기준 |

여기서 중요한 점은 덱 제한이 프린팅이 아니라 `카드 번호` 기준이라는 점이다.
따라서 컬렉션은 printing 단위로 세지만, 덱 빌더의 4장 제한은 identity/card number 기준으로 잡아야 한다.

### 4. RECOMMENDED DECKS

추천 덱 페이지는 공식 덱 콘텐츠를 보강 데이터로 사용할 수 있다.

| 필드 | 예시 | 수집 안정성 | 비고 |
| --- | --- | --- | --- |
| 게시일 | `June 12, 2026` | 높음 | 목록 페이지에서 추출 가능 |
| 리더 색상 | `(Red) Portgas.D.Ace`, `(Black/Yellow) Marshall.D.Teach` | 높음 | 제목 문자열 파싱 |
| 덱명/리더명 | `Portgas.D.Ace` | 높음 | 추천 덱 제목 |
| 덱 설명 | 덱 콘셉트 문장 | 중간 | 자연어 콘텐츠 |
| 주요 카드 참조 | `OP16-001`, `OP16-011` 등 | 중간 | 본문/이미지 alt text에서 추출 가능 |
| 전략 구간 | Early Game, Mid Game, End Game | 중간 | 구조는 있으나 페이지별 차이 가능 |

추천 덱은 사용자 덱 빌더의 초기 샘플이나 콘텐츠 기능에 좋지만,
카드 마스터 데이터의 원천으로 쓰기에는 카드 리스트보다 신뢰도가 낮다.
덱 수량 리스트가 항상 구조화되어 보장된다고 보기 어렵기 때문에,
초기에는 추천 덱 아티클과 참조 카드 정도만 수집하는 것이 안전하다.

### 5. NEWS / TOPICS

운영성 데이터는 공지 페이지에서 별도 수집해야 한다.

| 데이터 | 예시 | 활용 |
| --- | --- | --- |
| 금지 카드 | `OP06-047 Charlotte Pudding` 등 | 덱 빌더 경고/차단 |
| 제한 카드 | 현재 없음 같은 상태도 데이터화 | 덱 빌더 검증 |
| 금지 페어 | `OP07-115` + `EB04-058` | 조합 검증 |
| 적용일 | `effective from April 10, 2026` | 포맷/시점별 검증 |
| Alt-Art 포함 여부 | parallel/alt-art 포함 문구 | printing이 아닌 identity 기준 제재 근거 |
| 에라타 카드번호 | `OP16-081`, `OP15-023` | 카드 텍스트 correction 관리 |
| 수정 전/후 텍스트 | Before/After | 도감에 에라타 배지 표시 |

금지/제한과 에라타는 카드 리스트와 갱신 주기가 다르다.
크롤링 배치도 카드 리스트와 분리하는 것이 좋다.

## 요청 필드 기준 수집 가능성 비교

| 요청/관심 요소 | 공식 사이트 수집 가능성 | 권장 저장 위치 | 비고 |
| --- | --- | --- | --- |
| 이름 | 가능 | `card_identity`, `card_printing` 언어별 | 공식 카드 리스트 |
| 카드 번호 | 가능 | `card_identity.card_number` | 덱 룰 기준 |
| 코스트 | 가능 | `card_identity.cost` | 타입별 nullable |
| 효과 | 가능 | `card_identity.effect_text` | 원문 보존 |
| Trigger | 가능 | `card_identity.trigger_text` | 없는 경우 null |
| 종류 | 가능 | `card_identity.card_category` | Leader/Character/Event/Stage/DON!! |
| 색깔 | 가능 | `card_identity_colors` | 복수 색상 지원 |
| Attribute | 가능 | `card_identity.attribute` | Slash/Strike 등 |
| Power | 가능 | `card_identity.power` | Leader/Character 중심 |
| Counter | 가능 | `card_identity.counter` | Character 중심 |
| Life | 가능 | `card_identity.life` | Leader 전용 |
| Type/소속 | 가능 | `card_identity_types` | `/` 분해 |
| 레어도 | 가능 | `card_printing.rarity_code` | R/TR/SP 등 printing 단위 |
| SEC 여부 | 가능 | `rarity_code = SEC` 또는 파생 flag | boolean만 저장하지 말 것 |
| SP 여부 | 가능 | `rarity_code = SP_CARD` 또는 variant | 명칭 정규화 필요 |
| TR 여부 | 가능 | `rarity_code = TR` 또는 variant | Treasure Rare |
| 프로모션 여부 | 가능 | `product.kind = PROMOTION` 또는 card number prefix | 세트/상품 기준 보강 |
| 패러렐/Alt-Art 여부 | 부분 가능 | `card_printing.variant_kind` | 공식 카드 리스트는 이미지/중복으로 추론해야 하는 경우 있음 |
| 일러스트 타입 | 가능 | `card_printing.illustration_type` | 공식 필터 기준 |
| 블록 아이콘 | 가능 | `card_identity.block_icon` 또는 printing | 카드 텍스트 영역에 표시 |
| 테두리 색 | 낮음 | `card_printing.border_treatment` | 공식 텍스트 필드 아님. 이미지 분석/수동 보강 필요 |
| 일반/실버/골드 처리 | 낮음 | `card_printing.finish_treatment` | 공식 카드 리스트 텍스트만으로는 불안정 |
| 포일/텍스처 | 낮음 | `card_printing.finish_treatment` | 실물/이미지 기반 보강 필요 |
| 판매 링크 | 공식에서 불가 | `external_card_links` | SNKR DUNK 등 외부 소스/수동 링크 |
| 시세 | 공식에서 불가 | 별도 market price 테이블 | 외부 마켓 데이터 필요 |

## DB 관점 권장 모델링

공식 사이트 데이터를 안정적으로 받으려면 아래처럼 분리하는 것이 좋다.

### card_identity

카드 번호 기준의 게임상 동일 카드다.

- `card_number`
- `name`
- `card_category`
- `life`
- `cost`
- `attribute`
- `power`
- `counter`
- `effect_text`
- `trigger_text`
- `block_icon`
- `search_vector`

### card_identity_colors

복수 색상 지원용 조인 테이블이다.

- `card_identity_id`
- `color_code`

### card_identity_types

소속/타입 검색용 조인 테이블이다.

- `card_identity_id`
- `type_name`

### card_printing

같은 카드 번호의 수집/판매/이미지 단위다.

- `card_identity_id`
- `product_id`
- `rarity_code`
- `variant_kind`
- `illustration_type`
- `image_url`
- `is_reprint`
- `source_url`
- `source_scraped_at`

### products

세트/상품 단위다.

- `code`
- `name`
- `product_kind`
- `release_date`
- `msrp_text`
- `contents_text`
- `region`
- `source_url`

### card_errata

에라타 공지 단위다.

- `card_number`
- `notice_date`
- `before_text`
- `after_text`
- `applies_to_alt_art`
- `source_url`

### card_legality_rules

금지/제한/금지 페어 단위다.

- `rule_type`: `BANNED`, `RESTRICTED`, `BANNED_PAIR`
- `effective_date`
- `card_number`
- `paired_card_number`
- `applies_to_parallel`
- `source_url`

## 크롤러 구현 우선순위

### 1단계: 카드 마스터

1. 상품 페이지에서 `VIEW THE CARDS` 링크를 수집한다.
2. 카드 리스트 `series` URL을 순회한다.
3. 카드 블록을 파싱한다.
4. `card_number + normalized game text` 기준으로 identity를 upsert한다.
5. `card_number + product + rarity + image_url` 기준으로 printing을 upsert한다.

### 2단계: 상품/세트 메타데이터

1. `products` 페이지를 순회한다.
2. 상품 코드, 상품명, 출시일, 수록 수, 레어도 설명을 저장한다.
3. 카드 리스트의 `Card Set(s)`와 product code를 연결한다.

### 3단계: 운영 데이터

1. 금지/제한 공지를 별도 크롤링한다.
2. 에라타 공지를 별도 크롤링한다.
3. 덱 빌더 검증은 현재 날짜 기준 active rule만 적용하되, rule history는 유지한다.

### 4단계: 수집/판매 보강

1. 공식 이미지/텍스트만으로 구분 어려운 `border_treatment`, `finish_treatment`, `manga`, `parallel`은 수동 큐레이션 필드로 둔다.
2. SNKR DUNK 링크는 공식 데이터에 없으므로 `card_number + name + rarity/variant` 기반 검색 URL을 생성한다.
3. 수동 링크가 있으면 수동 링크를 우선하고, 없으면 검색 URL을 fallback으로 제공한다.

## 주의사항

1. 공식 사이트 하단에는 이미지/텍스트/데이터 무단 복제 금지 문구가 있다. 서비스에서 공식 이미지와 전체 카드 텍스트를 그대로 제공하려면 권리 검토가 필요하다.
2. 영어 사이트의 일부 페이지는 AI 번역 고지 문구를 포함한다. 확정 데이터는 원문 영어 기준으로 저장하고, 한국어 표기는 별도 소스 검토가 필요하다.
3. 카드 리스트의 `Hide reprint cards` 옵션은 같은 카드의 재록/중복 표시가 존재한다는 신호다. 크롤러는 중복을 무조건 제거하면 안 된다.
4. 테두리 색, 포일, 금박/은박, 텍스처 같은 실물 수집 요소는 공식 텍스트 필드로 안정적으로 제공되지 않는다. 이미지 분석 또는 수동 관리 대상으로 남겨야 한다.
5. 금지/제한은 `printing`이 아니라 카드 번호 단위로 적용된다. 공지에서도 parallel/Alt-Art 포함 여부가 언급된다.

## 결론

공식 사이트만으로도 도감의 핵심 데이터는 충분히 만들 수 있다.
다만 컬렉션/판매 링크까지 고려하면 `카드 번호 단위 identity`와 `실물 인쇄본 단위 printing`을 반드시 분리해야 한다.

공식 사이트에서 안정적으로 크롤링 가능한 것은 카드명, 카드번호, 카드 종류, 색상, 코스트, 파워, 카운터, 라이프, 속성, 타입, 효과, 트리거, 레어도, 블록, 세트, 출시일, 금지/제한/에라타다.

반대로 테두리 색, 실버/골드 처리, 포일 질감, 정확한 패러렐 세부 분류, 실거래 링크/가격은 공식 사이트만으로는 부족하다.
이 영역은 수동 큐레이션 또는 외부 마켓 데이터와 결합하는 방향이 맞다.
