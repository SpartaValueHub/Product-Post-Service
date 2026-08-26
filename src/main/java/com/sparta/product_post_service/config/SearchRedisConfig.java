package com.sparta.product_post_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

// 검색 Redis·비동기 기록·스케줄 베이크·SearchProperties 활성화
@Configuration
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(SearchProperties.class)
public class SearchRedisConfig {
}
