package com.tcgsearch.domain.card.service

import java.util.UUID

/**
 * 카드 검색 조건을 서비스 계층으로 전달합니다.
 *
 * Controller validation을 통과한 page, sort, filter 값을 프린팅 검색 기준으로 보관합니다.
 *
 * @author gurdl0525
 * @since 25-06-2026
 */
data class CardSearchQuery(
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
    val language: String = languageCode ?: "jp",
    val detailTags: Set<String> = emptySet(),
    val packCodes: Set<String> = cardSets,
    val characterIds: Set<UUID> = emptySet(),
    val illustratorIds: Set<UUID> = emptySet(),
)
