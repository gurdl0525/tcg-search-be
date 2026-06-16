package com.tcgsearch.global.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.tcgsearch.global.error.exception.BaseException
import com.tcgsearch.global.error.response.ErrorResponse
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.web.filter.OncePerRequestFilter

class ExceptionFilter(private val objectMapper: ObjectMapper): OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            filterChain.doFilter(request, response)
        } catch (e: BaseException) {
            response.let {
                it.status = e.errorCode.status.value()
                it.contentType = MediaType.APPLICATION_JSON_VALUE
                it.characterEncoding = Charsets.UTF_8.name()
            }
            objectMapper.writeValue(response.writer, ErrorResponse.of(e))
        }
    }

}