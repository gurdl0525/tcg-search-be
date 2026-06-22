package com.tcgsearch.domain.card.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

/**
 * 카드 레어도 참조 값을 저장하는 JPA Entity
 *
 * 일반판, SEC, 프로모션처럼 인쇄본 단위에서 사용하는 레어도 코드를 관리합니다.
 *
 * @author gurdl0525
 * @since 20-06-2026
 */
@Entity
@Table(name = "rarities")
class CardRarity(
    @Column(nullable = false, unique = true)
    var code: String,

    @Column(nullable = false)
    var name: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null
}
