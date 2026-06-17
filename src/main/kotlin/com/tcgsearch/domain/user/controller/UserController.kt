package com.tcgsearch.domain.user.controller

import com.tcgsearch.domain.user.service.UserService
import com.tcgsearch.global.error.ErrorCode
import com.tcgsearch.global.annotation.WebAdapter
import com.tcgsearch.global.error.exception.BaseException
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.util.UUID


/**
 * 사용자 API 요청을 처리합니다.
 *
 * JWT 인증 주체를 현재 사용자 조회 유스케이스로 전달합니다.
 *
 * @author gurdl0525
 * @since 16-06-2026
 */
@Tag(name = "User API")
@WebAdapter
@RequestMapping("/api/users")
class UserController(
    private val service: UserService,
) {
    @GetMapping("/me")
    fun me(authentication: JwtAuthenticationToken) =
        service.getCurrentUser(authentication.userId())

    private fun JwtAuthenticationToken.userId(): UUID =
        runCatching { UUID.fromString(name) }
            .getOrElse { throw BaseException(ErrorCode.INVALID_ACCESS_TOKEN) }
}
