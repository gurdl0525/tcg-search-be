package com.tcgsearch.domain.card.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

/**
 * 카드의 공격 속성을 저장하는 JPA Entity
 *
 * Slash, Strike 같은 공식 카드 속성 값을 참조 데이터로 관리합니다.
 *
 * @author gurdl0525
 * @since 20-06-2026
 */
@Entity
@Table(name = "attributes")
class CardAttribute(
    @Column(nullable = false, unique = true)
    var name: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null
}
