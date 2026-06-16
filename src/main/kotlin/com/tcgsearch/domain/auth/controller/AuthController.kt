package com.tcgsearch.domain.auth.controller

import com.tcgsearch.domain.auth.dto.request.RefreshTokenRequest
import com.tcgsearch.domain.auth.dto.response.TokenResponse
import com.tcgsearch.domain.auth.service.AuthService
import com.tcgsearch.global.annotation.WebAdapter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus

@Tag(name = "Auth API")
@WebAdapter
@RequestMapping("/api/auth")
class AuthController(private val service: AuthService) {

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): TokenResponse {
        return service.rotateRefreshToken(request.refreshToken!!)
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(@Valid @RequestBody request: RefreshTokenRequest) {
        service.revokeRefreshToken(request.refreshToken!!)
    }
}