package com.tcgsearch.domain.user.controller

import com.tcgsearch.domain.user.service.UserService
import com.tcgsearch.global.annotation.WebAdapter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RequestMapping


@Tag(name = "User API")
@WebAdapter
@RequestMapping("/api/users")
class UserController(
    private val service: UserService,
) {
}