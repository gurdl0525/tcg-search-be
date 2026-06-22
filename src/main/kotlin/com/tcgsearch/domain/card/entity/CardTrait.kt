package com.tcgsearch.domain.card.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

/**
 * 카드 타입/소속 참조 값을 저장하는 JPA Entity
 *
 * Straw Hat Crew, Navy 같은 카드 타입 문자열을 검색 가능한 참조 데이터로 관리합니다.
 *
 * @author gurdl0525
 * @since 20-06-2026
 */
@Entity
@Table(name = "traits")
class CardTrait(
    @Column(nullable = false, unique = true)
    var name: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null
}
