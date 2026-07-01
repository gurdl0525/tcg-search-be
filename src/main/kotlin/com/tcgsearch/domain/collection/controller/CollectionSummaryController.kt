package com.tcgsearch.domain.collection.controller

import com.tcgsearch.domain.collection.service.CollectionSummaryService
import com.tcgsearch.global.annotation.WebAdapter
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

/**
 * 내 컬렉션 요약 API 요청을 처리합니다.
 *
 * Bearer JWT의 사용자 식별자를 기준으로 컬렉션 탭에 필요한 최소 집계 정보를 반환합니다.
 *
 * @author gurdl0525
 * @since 01-07-2026
 */
@Tag(name = "Collection API")
@WebAdapter
@RequestMapping("/api/me/collections")
class CollectionSummaryController(
    private val service: CollectionSummaryService,
) {
    @GetMapping("/summary")
    fun summary(authentication: JwtAuthenticationToken) =
        service.summary(UUID.fromString(authentication.name))
}
