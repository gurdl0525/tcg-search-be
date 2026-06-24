package com.tcgsearch.domain.card.repository

import com.tcgsearch.domain.card.entity.CardPrinting
import org.springframework.data.repository.Repository
import java.util.UUID

/**
 * 카드 프린팅 영속성 접근을 제공합니다.
 *
 * 프린팅 단위 검색은 [CustomCardPrintingRepository] 구현으로 위임합니다.
 *
 * @author gurdl0525
 * @since 25-06-2026
 */
@org.springframework.stereotype.Repository
interface CardPrintingRepository : Repository<CardPrinting, UUID>, CustomCardPrintingRepository
