package com.tcgsearch.domain.card.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

/**
 * 카드 색상 참조 값을 저장하는 JPA Entity
 *
 * 단색과 다색 카드 검색을 위해 공식 색상 코드를 관리합니다.
 *
 * @author gurdl0525
 * @since 20-06-2026
 */
@Entity
@Table(name = "colors")
class CardColor(
    @Column(nullable = false, unique = true)
    var code: String,

    @Column(nullable = false)
    var name: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null
}
