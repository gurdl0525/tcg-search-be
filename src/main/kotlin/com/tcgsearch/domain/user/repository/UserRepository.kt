package com.tcgsearch.domain.user.repository

import com.tcgsearch.domain.user.entity.User
import org.springframework.data.repository.Repository
import java.util.UUID

@org.springframework.stereotype.Repository
interface UserRepository : Repository<User, UUID> {

    fun existsById(id: UUID): Boolean

    fun existsByEmail(email: String): Boolean

    fun existsByAuthProviderAndProviderSubject(authProvider: String, providerSubject: String): Boolean

    fun findById(id: UUID): User?

    fun findByAuthProviderAndProviderSubject(authProvider: String, providerSubject: String): User?

    fun save(user: User): User
}
