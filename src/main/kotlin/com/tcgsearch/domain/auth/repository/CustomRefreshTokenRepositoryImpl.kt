package com.tcgsearch.domain.auth.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.tcgsearch.domain.auth.entity.QRefreshToken
import com.tcgsearch.global.annotation.RequiredTransactional
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class CustomRefreshTokenRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val entityManager: EntityManager,
): CustomRefreshTokenRepository {

    @RequiredTransactional
    override fun revokeFamily(tokenFamilyId: UUID, revokedAt: Instant): Boolean {
        val refreshToken = QRefreshToken.refreshToken

        entityManager.flush()

        val affectedRows = queryFactory
            .update(refreshToken)
            .set(refreshToken.revokedAt, revokedAt)
            .set(refreshToken.updatedAt, revokedAt)
            .where(refreshToken.tokenFamilyId.eq(tokenFamilyId)
                .and(refreshToken.revokedAt.isNull))
            .execute()

        entityManager.clear()

        return affectedRows > 0L
    }
}