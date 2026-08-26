package com.sparta.product_post_service.adaptor.out.redis;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import com.sparta.product_post_service.application.port.out.AggregateSearchTermCountsPort;
import com.sparta.product_post_service.application.port.out.LoadPopularSearchTermsPort;
import com.sparta.product_post_service.application.port.out.RecordSearchTermPort;
import com.sparta.product_post_service.application.port.out.SavePopularSearchTermsPort;
import com.sparta.product_post_service.application.port.out.SearchBakeLockPort;
import com.sparta.product_post_service.application.support.SearchHourBucketSupport;
import com.sparta.product_post_service.config.SearchProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 검색어 시간 버킷·추천 서빙·베이크 락 Redis 어댑터 (장애 시 no-op / 빈 결과)
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSearchTermAdapter implements
		RecordSearchTermPort,
		LoadPopularSearchTermsPort,
		AggregateSearchTermCountsPort,
		SavePopularSearchTermsPort,
		SearchBakeLockPort {

	// Redis
	private final StringRedisTemplate stringRedisTemplate;
	// 검색 설정
	private final SearchProperties searchProperties;
	// 시간 버킷 키
	private final SearchHourBucketSupport searchHourBucketSupport;

	@Override
	public void record(String normalizedTerm) {
		if (normalizedTerm == null || normalizedTerm.isBlank()) {
			return;
		}
		try {
			String hourKey = searchHourBucketSupport.currentHourBucketKey();
			stringRedisTemplate.opsForZSet().incrementScore(
					hourKey,
					normalizedTerm,
					searchProperties.termScoreIncrement()
			);
			stringRedisTemplate.expire(hourKey, Duration.ofMillis(searchProperties.termsHourlyTtlMs()));
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
	public Map<String, Double> aggregateTopCounts(List<String> hourBucketKeys, int limit) {
		if (hourBucketKeys == null || hourBucketKeys.isEmpty() || limit <= 0) {
			return Map.of();
		}
		String tempKey = searchProperties.termsAggregateTempKeyPrefix() + UUID.randomUUID();
		try {
			List<String> existingKeys = hourBucketKeys.stream()
					.filter(key -> Boolean.TRUE.equals(stringRedisTemplate.hasKey(key)))
					.toList();
			if (existingKeys.isEmpty()) {
				return Map.of();
			}
			String sourceKey;
			if (existingKeys.size() == 1) {
				sourceKey = existingKeys.get(0);
			} else {
				stringRedisTemplate.opsForZSet().unionAndStore(
						existingKeys.get(0),
						existingKeys.subList(1, existingKeys.size()),
						tempKey
				);
				sourceKey = tempKey;
			}
			return topScores(sourceKey, limit);
		} catch (RuntimeException ex) {
			log.warn("검색어 시간 버킷 집계 실패", ex);
			return Map.of();
		} finally {
			try {
				stringRedisTemplate.delete(tempKey);
			} catch (RuntimeException ignored) {
				// 임시 키 정리 실패는 TTL 없음 — 다음 호출 UUID로 분리
			}
		}
	}

	private Map<String, Double> topScores(String zsetKey, int limit) {
		Set<ZSetOperations.TypedTuple<String>> tuples = stringRedisTemplate.opsForZSet()
				.reverseRangeWithScores(zsetKey, 0, limit - 1L);
		if (tuples == null || tuples.isEmpty()) {
			return Map.of();
		}
		Map<String, Double> counts = new HashMap<>();
		for (ZSetOperations.TypedTuple<String> tuple : tuples) {
			if (tuple.getValue() == null || tuple.getScore() == null) {
				continue;
			}
			counts.put(tuple.getValue(), tuple.getScore());
		}
		return Collections.unmodifiableMap(counts);
	}

	@Override
	public void savePopular(List<String> terms) {
		String servingKey = searchProperties.popularServingKey();
		String buildingKey = servingKey + searchProperties.snapshotBuildingSuffix();
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
				// 다음 베이크에서 overwrite
			}
		}
	}

	@Override
	public boolean tryLock() {
		try {
			Boolean acquired = stringRedisTemplate.opsForValue().setIfAbsent(
					searchProperties.bakeLockKey(),
					searchProperties.bakeLockValue(),
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
