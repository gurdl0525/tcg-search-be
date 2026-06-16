package com.tcgsearch.domain.auth.repository

import com.tcgsearch.domain.auth.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID>, CustomRefreshTokenRepository {

    fun existsByTokenHash(tokenHash: String): Boolean

    fun findByTokenHash(tokenHash: String): RefreshToken?
}