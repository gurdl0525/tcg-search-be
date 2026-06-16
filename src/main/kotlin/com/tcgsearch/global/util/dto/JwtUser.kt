package com.tcgsearch.global.util.dto

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class JwtUser (
    private val userId: String,
    private val role: String?,
): UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> = listOf(SimpleGrantedAuthority(role ?: "none"))

    override fun getPassword() = null

    override fun getUsername() = userId
}