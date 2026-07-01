package com.tcgsearch.domain.card.repository

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * 카드 프린팅 검색용 QueryDSL repository contract입니다.
 *
 * 필터와 정렬 조건을 받아 카드 식별자가 아닌 프린팅 row 기준으로 결과를 반환합니다.
 *
 * @author gurdl0525
 * @since 25-06-2026
 */
interface CustomCardPrintingRepository {
    fun search(condition: CardPrintingSearchCondition): CardPrintingSearchResult

    fun findDetailByPrintingId(
        printingId: UUID,
        languageCode: String?,
    ): CardPrintingSearchRow?

    fun findRelatedPrintings(printingId: UUID): List<CardPrintingSearchRow>

    fun findMarketplaceLinks(printingId: UUID): List<MarketplaceLinkRow>
}

data class CardPrintingSearchCondition(
    val page: Int,
    val size: Int,
    val searchWord: String?,
    val sortBy: String,
    val sort: String,
    val cardTypes: Set<String>,
    val colors: Set<String>,
    val rarities: Set<String>,
    val cardSets: Set<String>,
    val traits: Set<String>,
    val attributes: Set<String>,
    val isParallel: Boolean?,
    val languageCode: String?,
    val regionCode: String?,
    val illustrationTypes: Set<String>,
    val foilTreatments: Set<String>,
    val blockNo: Int?,
    val detailTags: Set<String> = emptySet(),
    val characterIds: Set<UUID> = emptySet(),
    val illustratorIds: Set<UUID> = emptySet(),
)

data class CardPrintingSearchResult(
    val rows: List<CardPrintingSearchRow>,
    val totalElements: Long,
)

data class CardPrintingSearchRow(
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
    val colors: List<CardColorSearchRow>,
    val traits: List<CardTraitSearchRow>,
    val cardSet: CardSetSearchRow,
    val rarity: CardRaritySearchRow?,
    val languageCode: String,
    val regionCode: String?,
    val variantName: String?,
    val isParallel: Boolean,
    val foilTreatment: String?,
    val illustrationType: String?,
    val imageUrl: String?,
    val sourceUrl: String?,
    val detailTags: List<String>,
    val releaseDate: LocalDate?,
    val illustrator: IllustratorSearchRow?,
)

data class CardColorSearchRow(
    val code: String,
    val name: String,
)

data class CardTraitSearchRow(
    val name: String,
)

data class CardSetSearchRow(
    val code: String,
    val name: String,
)

data class CardRaritySearchRow(
    val code: String,
    val name: String,
)

data class IllustratorSearchRow(
    val id: UUID,
    val name: String,
)

data class MarketplaceLinkRow(
    val provider: String,
    val label: String,
    val url: String,
    val updatedAt: Instant,
)
