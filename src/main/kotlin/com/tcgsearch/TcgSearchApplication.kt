package com.tcgsearch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * TCG Search 백엔드 애플리케이션의 Spring Boot 진입점
 *
 * 카드 도감, 컬렉션, 덱 빌더, 외부 판매 링크 연동 기능을 제공하기 위한
 * API 서버의 루트 애플리케이션 컨텍스트를 구성합니다.
 *
 * @author gurdl0525
 * @since 16-06-2026
 */
@SpringBootApplication
class TcgSearchApplication

/**
 * TCG Search 백엔드 애플리케이션을 실행합니다.
 *
 * @param args 애플리케이션 실행 인자
 */
fun main(args: Array<String>) {
	runApplication<TcgSearchApplication>(*args)
}
