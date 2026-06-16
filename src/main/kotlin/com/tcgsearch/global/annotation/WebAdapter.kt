package com.tcgsearch.global.annotation

import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RestController

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Validated
@MustBeDocumented
@RestController
annotation class WebAdapter()
