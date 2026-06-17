package com.tcgsearch.domain.user.service

import com.tcgsearch.domain.user.dto.response.CurrentUserResponse
import com.tcgsearch.domain.user.repository.UserRepository
import com.tcgsearch.global.error.ErrorCode
import com.tcgsearch.global.error.exception.BaseException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID


@Service
class UserServiceImpl(
    private val users: UserRepository,
): UserService {
    @Transactional(readOnly = true)
    override fun getCurrentUser(userId: UUID): CurrentUserResponse {
        val user = users.findById(userId) ?: throw BaseException(ErrorCode.USER_NOT_FOUND)

        return CurrentUserResponse(
            id = user.id ?: throw BaseException(ErrorCode.USER_NOT_FOUND),
            email = user.email,
            displayName = user.displayName,
            role = user.role,
            enabled = user.enabled,
        )
    }
}
