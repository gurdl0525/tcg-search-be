package com.tcgsearch.global.filter

import com.tcgsearch.global.util.TokenProvider
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtFilter(
    private val tokenProvider: TokenProvider
): OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        tokenProvider.resolveToken(request)
            ?.let { token -> tokenProvider.parseAccessToken(token) }
            ?.apply {
                SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(
                    this,
                    null,
                    this.authorities
                )
            }
    }

}