package com.tcgsearch.domain.card.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

/**
 * 카드 일러스트레이터를 저장하는 JPA Entity
 *
 * 필터 시트의 일러스트레이터 자동완성과 카드 상세 표시에서 사용합니다.
 *
 * @author gurdl0525
 * @since 01-07-2026
 */
@Entity
@Table(name = "illustrators")
class Illustrator(
    @Column(nullable = false, unique = true)
    var name: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
}
