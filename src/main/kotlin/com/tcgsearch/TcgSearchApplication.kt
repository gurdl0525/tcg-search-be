package com.tcgsearch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TcgSearchApplication

fun main(args: Array<String>) {
	runApplication<TcgSearchApplication>(*args)
}
