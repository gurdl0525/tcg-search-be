package com.tcgsearch.domain.card.service

import com.tcgsearch.domain.card.dto.response.CardSearchPageResponse

/**
 * 카드 프린팅 검색 유스케이스를 제공합니다.
 *
 * @author gurdl0525
 * @since 25-06-2026
 */
interface CardSearchService {
    fun search(query: CardSearchQuery): CardSearchPageResponse
}
