package com.tcgsearch.domain.auth.repository

import com.tcgsearch.domain.auth.entity.RefreshToken
import com.tcgsearch.global.annotation.RequiredTransactional
import org.springframework.data.repository.Repository
import java.util.*

@org.springframework.stereotype.Repository
interface RefreshTokenRepository : Repository<RefreshToken, UUID>, CustomRefreshTokenRepository {

    fun existsByTokenHash(tokenHash: String): Boolean

    fun findByTokenHash(tokenHash: String): RefreshToken?

    @RequiredTransactional
    fun save(token: RefreshToken): RefreshToken
}