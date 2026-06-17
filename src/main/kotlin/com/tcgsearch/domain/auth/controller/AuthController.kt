package com.tcgsearch.domain.auth.controller

import com.tcgsearch.domain.auth.dto.request.LoginRequest
import com.tcgsearch.domain.auth.dto.request.RefreshTokenRequest
import com.tcgsearch.domain.auth.dto.request.SignUpRequest
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

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signUp(@Valid @RequestBody request: SignUpRequest): TokenResponse = service.signUp(request)

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): TokenResponse = service.login(request)

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): TokenResponse =
        service.rotateRefreshToken(request.refreshToken!!)

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(@Valid @RequestBody request: RefreshTokenRequest) {
        service.revokeRefreshToken(request.refreshToken!!)
    }
}
