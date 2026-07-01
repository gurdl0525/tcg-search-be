package com.tcgsearch.domain.card.controller

import com.tcgsearch.domain.card.dto.request.CardSearchEventRequest
import com.tcgsearch.domain.card.service.CardSearchQuery
import com.tcgsearch.domain.card.service.CardSearchService
import com.tcgsearch.global.annotation.WebAdapter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus

private const val SORT_BY_PATTERN =
    "card_no|name|card_type|cost|life|power|counter|block_no|rarity|card_set|variant_name|is_parallel|created_at"
private const val SORT_PATTERN = "(?i)ASC|DESC"
private const val LANGUAGE_CODE_PATTERN = "jp|en|ko"

/**
 * 카드 검색 API 요청을 처리합니다.
 *
 * 검색 결과는 카드 식별자가 아니라 실제 프린팅 단위로 반환합니다.
 *
 * @author gurdl0525
 * @since 25-06-2026
 */
@Tag(name = "Card API")
@WebAdapter
@RequestMapping("/api/cards")
class CardController(
    private val service: CardSearchService,
) {

    @GetMapping("/search")
    fun searchForIos(
        @Min(0, message = "must be greater than or equal to 0.")
        @RequestParam(defaultValue = "0") page: Int,

        @Min(1, message = "must be greater than or equal to 1.")
        @Max(100, message = "must be less than or equal to 100.")
        @RequestParam(defaultValue = "20") size: Int,

        @Size(max = 100, message = "must be at most 100 characters.")
        @RequestParam(name = "query", required = false) query: String?,

        @Pattern(regexp = "all|ko|en|jp", message = "must be all, ko, en, or jp.")
        @RequestParam(name = "language", defaultValue = "all") language: String,

        @Size(max = 20, message = "must contain at most 20 values.")
        @RequestParam(name = "card_types", required = false) cardTypes: List<String>?,

        @Size(max = 20, message = "must contain at most 20 values.")
        @RequestParam(name = "rarities", required = false) rarities: List<String>?,

        @Size(max = 20, message = "must contain at most 20 values.")
        @RequestParam(name = "detail_tags", required = false) detailTags: List<String>?,

        @Size(max = 20, message = "must contain at most 20 values.")
        @RequestParam(name = "pack_codes", required = false) packCodes: List<String>?,

        @Size(max = 20, message = "must contain at most 20 values.")
        @RequestParam(name = "character_ids", required = false) characterIds: List<UUID>?,

        @Size(max = 20, message = "must contain at most 20 values.")
        @RequestParam(name = "illustrator_ids", required = false) illustratorIds: List<UUID>?,

        @Pattern(
            regexp = "card_no_desc|card_no_asc|name_asc|name_desc",
            message = "must be card_no_desc, card_no_asc, name_asc, or name_desc.",
        )
        @RequestParam(name = "sort", defaultValue = "card_no_desc") sort: String,
    ) = service.searchForIos(
        CardSearchQuery(
            page = page,
            size = size,
            searchWord = query?.trim()?.takeIf { it.isNotEmpty() },
            sortBy = sort.toSortBy(),
            sort = sort.toSortDirection(),
            cardTypes = cardTypes.toTokens(),
            colors = emptySet(),
            rarities = rarities.toTokens(),
            cardSets = packCodes.toTokens(),
            traits = emptySet(),
            attributes = emptySet(),
            isParallel = null,
            languageCode = language.takeUnless { it == ALL_LANGUAGE },
            regionCode = null,
            illustrationTypes = emptySet(),
            foilTreatments = emptySet(),
            blockNo = null,
            language = language,
            detailTags = detailTags.toTokens(),
            packCodes = packCodes.toTokens(),
            characterIds = characterIds.orEmpty().toSet(),
            illustratorIds = illustratorIds.orEmpty().toSet(),
        ),
    )

    @GetMapping("/filter-options")
    fun filterOptions(
        @Pattern(regexp = "all|ko|en|jp", message = "must be all, ko, en, or jp.")
        @RequestParam(defaultValue = "all") language: String,
    ) = service.filterOptions(language)

    @GetMapping("/packs")
    fun packs(
        @Pattern(regexp = "all|ko|en|jp", message = "must be all, ko, en, or jp.")
        @RequestParam(defaultValue = "all") language: String,

        @Size(max = 100, message = "must be at most 100 characters.")
        @RequestParam(required = false) query: String?,

        @Min(1, message = "must be greater than or equal to 1.")
        @Max(100, message = "must be less than or equal to 100.")
        @RequestParam(defaultValue = "50") limit: Int,
    ) = service.packs(language, query?.trim()?.takeIf { it.isNotEmpty() }, limit)

    @GetMapping("/characters")
    fun characters(
        @Pattern(regexp = "all|ko|en|jp", message = "must be all, ko, en, or jp.")
        @RequestParam(defaultValue = "all") language: String,

        @Size(max = 100, message = "must be at most 100 characters.")
        @RequestParam(required = false) query: String?,

        @Min(1, message = "must be greater than or equal to 1.")
        @Max(100, message = "must be less than or equal to 100.")
        @RequestParam(defaultValue = "20") limit: Int,
    ) = service.characters(language, query?.trim()?.takeIf { it.isNotEmpty() }, limit)

    @GetMapping("/illustrators")
    fun illustrators(
        @Size(max = 100, message = "must be at most 100 characters.")
        @RequestParam(required = false) query: String?,

        @Min(1, message = "must be greater than or equal to 1.")
        @Max(100, message = "must be less than or equal to 100.")
        @RequestParam(defaultValue = "20") limit: Int,
    ) = service.illustrators(query?.trim()?.takeIf { it.isNotEmpty() }, limit)

    @GetMapping("/trending")
    fun trending(
        @Pattern(regexp = "all|ko|en|jp", message = "must be all, ko, en, or jp.")
        @RequestParam(defaultValue = "all") language: String,

        @Min(1, message = "must be greater than or equal to 1.")
        @Max(20, message = "must be less than or equal to 20.")
        @RequestParam(defaultValue = "3") limit: Int,

        @Pattern(regexp = "day|week|month", message = "must be day, week, or month.")
        @RequestParam(defaultValue = "week") period: String,
    ) = service.trending(language, limit, period)

    @GetMapping("/{printingId}")
    fun detail(
        @PathVariable printingId: UUID,

        @Pattern(regexp = "ko|en|jp", message = "must be ko, en, or jp.")
        @RequestParam(name = "language", required = false) language: String?,
    ) = service.detail(printingId, language)

    @GetMapping("/{printingId}/related-printings")
    fun relatedPrintings(
        @PathVariable printingId: UUID,
    ) = service.relatedPrintings(printingId)

    @GetMapping("/{printingId}/marketplace-links")
    fun marketplaceLinks(
        @PathVariable printingId: UUID,
    ) = service.marketplaceLinks(printingId)

    @PostMapping("/search-events")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun recordSearchEvent(
        @Valid @RequestBody request: CardSearchEventRequest,
        authentication: Authentication?,
    ) = service.recordSearchEvent(
        request = request,
        userId = authentication?.name?.let { runCatching { UUID.fromString(it) }.getOrNull() },
    )

    @GetMapping
    fun search(
        @Min(0, message = "must be greater than or equal to 0.")
        @RequestParam(defaultValue = "0") page: Int,

        @Min(1, message = "must be greater than or equal to 1.")
        @Max(100, message = "must be less than or equal to 100.")
        @RequestParam(defaultValue = "20") size: Int,

        @Size(max = 100, message = "must be at most 100 characters.")
        @RequestParam(name = "search_word", required = false) searchWord: String?,

        @Pattern(regexp = SORT_BY_PATTERN, message = "must be a supported sort field.")
        @RequestParam(name = "sort_by", defaultValue = "card_no") sortBy: String,

        @Pattern(regexp = SORT_PATTERN, message = "must be ASC or DESC.")
        @RequestParam(name = "sort", defaultValue = "ASC") sort: String,

        @Size(max = 20, message = "must contain at most 20 values.")
        @RequestParam(name = "card_type", required = false) cardTypes: List<String>?,

        @Size(max = 20, message = "must contain at most 20 values.")
        @RequestParam(name = "color", required = false) colors: List<String>?,

        @Size(max = 20, message = "must contain at most 20 values.")
        @RequestParam(name = "rarity", required = false) rarities: List<String>?,

        @Size(max = 20, message = "must contain at most 20 values.")
        @RequestParam(name = "card_set", required = false) cardSets: List<String>?,

        @Size(max = 20, message = "must contain at most 20 values.")
        @RequestParam(name = "trait", required = false) traits: List<String>?,

        @Size(max = 20, message = "must contain at most 20 values.")
        @RequestParam(name = "attribute", required = false) attributes: List<String>?,
        @RequestParam(name = "is_parallel", required = false) isParallel: Boolean?,

        @Pattern(regexp = LANGUAGE_CODE_PATTERN, message = "must be jp, en, or ko.")
        @RequestParam(name = "language_code", defaultValue = "jp") languageCode: String,

        @Size(max = 20, message = "must be at most 20 characters.")
        @RequestParam(name = "region_code", required = false) regionCode: String?,

        @Size(max = 20, message = "must contain at most 20 values.")
        @RequestParam(name = "illustration_type", required = false) illustrationTypes: List<String>?,

        @Size(max = 20, message = "must contain at most 20 values.")
        @RequestParam(name = "foil_treatment", required = false) foilTreatments: List<String>?,

        @Min(0, message = "must be greater than or equal to 0.")
        @RequestParam(name = "block_no", required = false) blockNo: Int?,
    ) = service.search(
        CardSearchQuery(
            page = page,
            size = size,
            searchWord = searchWord?.trim()?.takeIf { it.isNotEmpty() },
            sortBy = sortBy,
            sort = sort.uppercase(),
            cardTypes = cardTypes.toTokens(),
            colors = colors.toTokens(),
            rarities = rarities.toTokens(),
            cardSets = cardSets.toTokens(),
            traits = traits.toTokens(),
            attributes = attributes.toTokens(),
            isParallel = isParallel,
            languageCode = languageCode.trim().takeIf { it.isNotEmpty() },
            regionCode = regionCode?.trim()?.takeIf { it.isNotEmpty() },
            illustrationTypes = illustrationTypes.toTokens(),
            foilTreatments = foilTreatments.toTokens(),
            blockNo = blockNo,
        ),
    )

    private fun List<String>?.toTokens(): Set<String> =
        orEmpty()
            .flatMap { it.split(",") }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()

    private fun String.toSortBy(): String =
        when (this) {
            "name_asc", "name_desc" -> "name"
            else -> "card_no"
        }

    private fun String.toSortDirection(): String =
        when {
            endsWith("_desc") -> "DESC"
            else -> "ASC"
        }

    private companion object {
        const val ALL_LANGUAGE = "all"
    }
}
