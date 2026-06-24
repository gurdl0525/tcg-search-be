package com.tcgsearch.domain.card.dto.response

import java.util.UUID

/**
 * 카드 검색 결과의 프린팅 단위 응답입니다.
 *
 * 같은 카드 번호라도 이미지, 세트, 레어도, 패러렐 여부가 다르면 별도 항목으로 반환합니다.
 *
 * @author gurdl0525
 * @since 25-06-2026
 */
data class CardSearchItemResponse(
    val printingId: UUID,
    val cardIdentityId: UUID,
    val cardNo: String,
    val name: String,
    val cardType: String,
    val cost: Short?,
    val life: Short?,
    val power: Int?,
    val counter: Int?,
    val attribute: String?,
    val effectText: String?,
    val triggerText: String?,
    val blockNo: Int?,
    val colors: List<CardColorSummaryResponse>,
    val traits: List<CardTraitSummaryResponse>,
    val cardSet: CardSetSummaryResponse,
    val rarity: CardRaritySummaryResponse?,
    val languageCode: String,
    val regionCode: String?,
    val variantName: String?,
    val isParallel: Boolean,
    val foilTreatment: String?,
    val illustrationType: String?,
    val imageUrl: String?,
    val sourceUrl: String?,
)

data class CardColorSummaryResponse(
    val code: String,
    val name: String,
)

data class CardTraitSummaryResponse(
    val name: String,
)

data class CardSetSummaryResponse(
    val code: String,
    val name: String,
)

data class CardRaritySummaryResponse(
    val code: String,
    val name: String,
)
