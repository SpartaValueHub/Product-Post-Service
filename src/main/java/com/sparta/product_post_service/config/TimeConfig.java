package com.sparta.product_post_service.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 시간 생성 위치 고정 (테스트에서 Clock 교체 가능)
@Configuration
public class TimeConfig {

	// UTC 기준 시스템 시계
	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}
}
