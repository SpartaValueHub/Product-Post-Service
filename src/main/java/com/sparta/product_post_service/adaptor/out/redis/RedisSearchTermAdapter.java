package com.sparta.product_post_service.adaptor.out.redis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.sparta.product_post_service.application.port.out.LoadPopularSearchTermsPort;
import com.sparta.product_post_service.application.port.out.RecordSearchTermPort;
import com.sparta.product_post_service.config.SearchProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 검색어 카운터·인기 TopN Redis 어댑터 (장애 시 no-op / 빈 리스트)
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSearchTermAdapter implements RecordSearchTermPort, LoadPopularSearchTermsPort {

	// Redis
	private final StringRedisTemplate stringRedisTemplate;
	// 검색 설정 (ZSET 키 등)
	private final SearchProperties searchProperties;

	@Override
	public void record(String normalizedTerm) {
		if (normalizedTerm == null || normalizedTerm.isBlank()) {
			return;
		}
		try {
			stringRedisTemplate.opsForZSet()
					.incrementScore(searchProperties.termsZsetKey(), normalizedTerm, 1.0d);
		} catch (RuntimeException ex) {
			log.warn("검색어 카운터 기록 실패 term={}", normalizedTerm, ex);
		}
	}

	@Override
	public List<String> loadPopular(int limit) {
		if (limit <= 0) {
			return List.of();
		}
		try {
			Set<String> range = stringRedisTemplate.opsForZSet()
					.reverseRange(searchProperties.termsZsetKey(), 0, limit - 1L);
			if (range == null || range.isEmpty()) {
				return List.of();
			}
			return Collections.unmodifiableList(new ArrayList<>(range));
		} catch (RuntimeException ex) {
			log.warn("인기 검색어 조회 실패", ex);
			return List.of();
		}
	}
}
