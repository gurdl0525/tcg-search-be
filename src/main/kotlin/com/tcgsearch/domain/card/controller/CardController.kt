package com.tcgsearch.domain.card.controller

import com.tcgsearch.domain.card.service.CardSearchQuery
import com.tcgsearch.domain.card.service.CardSearchService
import com.tcgsearch.global.annotation.WebAdapter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

private const val SORT_BY_PATTERN =
    "card_no|name|card_type|cost|life|power|counter|block_no|rarity|card_set|variant_name|is_parallel|created_at"
private const val SORT_PATTERN = "(?i)ASC|DESC"

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

        @Size(max = 20, message = "must be at most 20 characters.")
        @RequestParam(name = "language_code", required = false) languageCode: String?,

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
            languageCode = languageCode?.trim()?.takeIf { it.isNotEmpty() },
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
}
