package com.tcgsearch.domain.deck.controller

import com.tcgsearch.domain.deck.service.DeckSummaryService
import com.tcgsearch.global.annotation.WebAdapter
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

/**
 * 내 덱 요약 API 요청을 처리합니다.
 *
 * 덱 탭 첫 화면에 필요한 덱 수와 최근 덱 정보를 반환합니다.
 *
 * @author gurdl0525
 * @since 01-07-2026
 */
@Tag(name = "Deck API")
@WebAdapter
@RequestMapping("/api/me/decks")
class DeckSummaryController(
    private val service: DeckSummaryService,
) {
    @GetMapping("/summary")
    fun summary(authentication: JwtAuthenticationToken) =
        service.summary(UUID.fromString(authentication.name))
}
