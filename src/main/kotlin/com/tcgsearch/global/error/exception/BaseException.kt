package com.tcgsearch.global.error.exception

import com.tcgsearch.global.error.ErrorCode

open class BaseException(val errorCode: ErrorCode): RuntimeException(errorCode.message) {
}