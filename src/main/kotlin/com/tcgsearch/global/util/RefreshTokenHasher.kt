package com.tcgsearch.global.util

import org.springframework.stereotype.Component
import java.security.MessageDigest

@Component
class RefreshTokenHasher {
    fun hash(rawToken: String): String {
        return MessageDigest
            .getInstance("SHA-256")
            .digest(rawToken.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}