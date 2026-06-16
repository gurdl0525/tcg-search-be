package com.tcgsearch

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<TcgSearchApplication>().with(TestcontainersConfiguration::class).run(*args)
}
