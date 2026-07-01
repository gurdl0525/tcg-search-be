package com.tcgsearch.domain.card.dto.response

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class IosCardSearchPageResponse(
    val content: List<IosCardSearchItemResponse>,
    val page: Int,
    val size: Int,
    val totalCount: Long,
    val hasNext: Boolean,
    val nextPage: Int?,
)

data class IosCardSearchItemResponse(
    val printingId: UUID,
    val cardIdentityId: UUID,
    val cardNo: String,
    val name: String,
    val cardType: String,
    val rarity: String?,
    val language: String,
    val colors: List<String>,
    val traits: List<String>,
    val pack: IosCardPackSummaryResponse,
    val variant: IosCardVariantResponse,
    val imageUrl: String?,
)

data class IosCardPackSummaryResponse(
    val code: String,
    val name: String,
    val releaseDate: LocalDate? = null,
)

data class IosCardVariantResponse(
    val isParallel: Boolean,
    val detailTags: List<String>,
    val displayName: String?,
)

data class IosCardDetailResponse(
    val printingId: UUID,
    val cardIdentityId: UUID,
    val cardNo: String,
    val name: String,
    val cardType: String,
    val rarity: String?,
    val language: String,
    val colors: List<String>,
    val traits: List<String>,
    val cost: Short?,
    val life: Short?,
    val power: Int?,
    val counter: Int?,
    val attribute: String?,
    val effectText: String?,
    val triggerText: String?,
    val pack: IosCardPackSummaryResponse,
    val variant: IosCardVariantResponse,
    val illustrator: IosIllustratorSummaryResponse?,
    val imageUrl: String?,
    val sourceUrl: String?,
)

data class IosIllustratorSummaryResponse(
    val id: UUID,
    val name: String,
)

data class IosRelatedPrintingResponse(
    val printingId: UUID,
    val language: String,
    val variant: IosCardVariantResponse,
    val imageUrl: String?,
)

data class IosListResponse<T>(
    val content: List<T>,
)

data class IosOptionResponse(
    val code: String,
    val name: String,
)

data class IosFilterOptionsResponse(
    val languages: List<IosOptionResponse>,
    val cardTypes: List<IosOptionResponse>,
    val rarities: List<IosOptionResponse>,
    val detailTags: List<IosOptionResponse>,
    val defaultSort: String,
)

data class IosPackOptionResponse(
    val code: String,
    val name: String,
    val releaseDate: LocalDate?,
)

data class IosCharacterOptionResponse(
    val id: UUID,
    val name: String,
    val aliases: List<String>,
    val cardCount: Long,
)

data class IosIllustratorOptionResponse(
    val id: UUID,
    val name: String,
    val cardCount: Long,
)

data class IosTrendingCardResponse(
    val rank: Int,
    val searchCount: Long,
    val card: IosCardSearchItemResponse,
)

data class IosMarketplaceLinkResponse(
    val provider: String,
    val label: String,
    val url: String,
    val updatedAt: Instant,
)
