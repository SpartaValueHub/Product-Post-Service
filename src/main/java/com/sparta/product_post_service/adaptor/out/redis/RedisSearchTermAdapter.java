package com.sparta.product_post_service.adaptor.out.redis;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.sparta.product_post_service.application.port.out.LoadPopularSearchTermsPort;
import com.sparta.product_post_service.application.port.out.LoadSearchTermRankingPort;
import com.sparta.product_post_service.application.port.out.RecordSearchTermPort;
import com.sparta.product_post_service.application.port.out.SavePopularSearchTermsPort;
import com.sparta.product_post_service.application.port.out.SearchBakeLockPort;
import com.sparta.product_post_service.config.SearchProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 검색어 카운터·추천 서빙·베이크 락 Redis 어댑터 (장애 시 no-op / 빈 리스트)
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSearchTermAdapter implements
		RecordSearchTermPort,
		LoadPopularSearchTermsPort,
		LoadSearchTermRankingPort,
		SavePopularSearchTermsPort,
		SearchBakeLockPort {

	// Redis
	private final StringRedisTemplate stringRedisTemplate;
	// 검색 설정 (키·점수·락 TTL)
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
			List<String> range = stringRedisTemplate.opsForList()
					.range(searchProperties.popularServingKey(), 0, limit - 1L);
			if (range == null || range.isEmpty()) {
				return List.of();
			}
			return Collections.unmodifiableList(new ArrayList<>(range));
		} catch (RuntimeException ex) {
			log.warn("인기 검색어 서빙 조회 실패", ex);
			return List.of();
		}
	}

	@Override
	public List<String> loadTopTerms(int limit, double minScore) {
		if (limit <= 0) {
			return List.of();
		}
		try {
			Set<String> range = stringRedisTemplate.opsForZSet().reverseRangeByScore(
					searchProperties.termsZsetKey(),
					minScore,
					Double.POSITIVE_INFINITY,
					0,
					limit
			);
			if (range == null || range.isEmpty()) {
				return List.of();
			}
			return Collections.unmodifiableList(new ArrayList<>(range));
		} catch (RuntimeException ex) {
			log.warn("검색어 랭킹 TopN 조회 실패", ex);
			return List.of();
		}
	}

	@Override
	public void savePopular(List<String> terms) {
		String servingKey = searchProperties.popularServingKey();
		String buildingKey = servingKey + ":building";
		try {
			stringRedisTemplate.delete(buildingKey);
			if (terms == null || terms.isEmpty()) {
				stringRedisTemplate.delete(servingKey);
				return;
			}
			stringRedisTemplate.opsForList().rightPushAll(buildingKey, terms.toArray(String[]::new));
			stringRedisTemplate.rename(buildingKey, servingKey);
		} catch (RuntimeException ex) {
			log.warn("추천 검색어 서빙 저장 실패", ex);
			try {
				stringRedisTemplate.delete(buildingKey);
			} catch (RuntimeException ignored) {
				// building 키 정리 실패는 무시 (TTL 없는 임시 키 — 다음 베이크에서 overwrite)
			}
		}
	}

	@Override
	public boolean tryLock() {
		try {
			Boolean acquired = stringRedisTemplate.opsForValue().setIfAbsent(
					searchProperties.bakeLockKey(),
					"1",
					Duration.ofMillis(searchProperties.bakeLockTtlMs())
			);
			return Boolean.TRUE.equals(acquired);
		} catch (RuntimeException ex) {
			log.warn("추천 검색어 베이크 락 획득 실패", ex);
			return false;
		}
	}

	@Override
	public void unlock() {
		try {
			stringRedisTemplate.delete(searchProperties.bakeLockKey());
		} catch (RuntimeException ex) {
			log.warn("추천 검색어 베이크 락 해제 실패", ex);
		}
	}
}
