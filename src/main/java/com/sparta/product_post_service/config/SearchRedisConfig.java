package com.sparta.product_post_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

// 검색 Redis·비동기 기록·SearchProperties 활성화
@Configuration
@EnableAsync
@EnableConfigurationProperties(SearchProperties.class)
public class SearchRedisConfig {
}
