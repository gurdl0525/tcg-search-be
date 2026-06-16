package com.tcgsearch.global.annotation

import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Target(allowedTargets = [AnnotationTarget.FUNCTION, AnnotationTarget.CLASS])
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Transactional(propagation = Propagation.MANDATORY)
annotation class RequiredTransactional {}