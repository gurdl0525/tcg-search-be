package com.tcgsearch.domain.user.repository

import com.tcgsearch.domain.user.entity.User
import org.springframework.data.repository.Repository
import java.util.UUID

@org.springframework.stereotype.Repository
interface UserRepository : Repository<User, UUID> {

    fun existsById(id: UUID): Boolean
}