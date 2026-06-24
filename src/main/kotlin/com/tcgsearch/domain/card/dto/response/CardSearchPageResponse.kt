package com.tcgsearch.domain.card.dto.response

/**
 * 카드 검색 페이지 응답입니다.
 *
 * total 값은 필터링된 프린팅 row 수를 기준으로 계산합니다.
 *
 * @author gurdl0525
 * @since 25-06-2026
 */
data class CardSearchPageResponse(
    val content: List<CardSearchItemResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)
