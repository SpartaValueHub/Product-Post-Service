package com.sparta.product_post_service.adaptor.out.redis;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.sparta.product_post_service.application.port.out.LoadBakedRelatedSearchTermsPort;
import com.sparta.product_post_service.application.port.out.LoadSearchCooccurrencePort;
import com.sparta.product_post_service.application.port.out.RecordSearchCooccurrencePort;
import com.sparta.product_post_service.application.port.out.RelatedSearchBakeLockPort;
import com.sparta.product_post_service.application.port.out.SaveRelatedSearchTermsPort;
import com.sparta.product_post_service.config.SearchProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 동시검색 카운터·연관 서빙·락 Redis 어댑터
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRelatedSearchAdapter implements
		RecordSearchCooccurrencePort,
		LoadSearchCooccurrencePort,
		SaveRelatedSearchTermsPort,
		LoadBakedRelatedSearchTermsPort,
		RelatedSearchBakeLockPort {

	// Redis
	private final StringRedisTemplate stringRedisTemplate;
	// 검색 설정
	private final SearchProperties searchProperties;

	@Override
	public void recordTransition(String sessionKey, String currentTerm) {
		if (sessionKey == null || sessionKey.isBlank() || currentTerm == null || currentTerm.isBlank()) {
			return;
		}
		try {
			String lastKey = searchProperties.sessionLastKeyPrefix() + sessionKey;
			String previous = stringRedisTemplate.opsForValue().get(lastKey);
			if (previous != null && !previous.isBlank() && !previous.equals(currentTerm)) {
				String coocKey = searchProperties.cooccurrenceZsetKeyPrefix() + previous;
				stringRedisTemplate.opsForZSet().incrementScore(
						coocKey,
						currentTerm,
						searchProperties.cooccurrenceScoreIncrement()
				);
				stringRedisTemplate.opsForSet().add(searchProperties.cooccurrenceSourcesKey(), previous);
			}
			stringRedisTemplate.opsForValue().set(
					lastKey,
					currentTerm,
					Duration.ofMillis(searchProperties.sessionLastTtlMs())
			);
		} catch (RuntimeException ex) {
			log.warn("동시검색 기록 실패 sessionKey={} term={}", sessionKey, currentTerm, ex);
		}
	}

	@Override
	public Set<String> loadSourceTerms() {
		try {
			Set<String> members = stringRedisTemplate.opsForSet()
					.members(searchProperties.cooccurrenceSourcesKey());
			if (members == null || members.isEmpty()) {
				return Set.of();
			}
			return Collections.unmodifiableSet(new LinkedHashSet<>(members));
		} catch (RuntimeException ex) {
			log.warn("동시검색 source 조회 실패", ex);
			return Set.of();
		}
	}

	@Override
	public List<String> loadTopRelated(String fromTerm, int limit, double minScore) {
		if (fromTerm == null || fromTerm.isBlank() || limit <= 0) {
			return List.of();
		}
		try {
			Set<String> range = stringRedisTemplate.opsForZSet().reverseRangeByScore(
					searchProperties.cooccurrenceZsetKeyPrefix() + fromTerm,
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
			log.warn("동시검색 TopN 조회 실패 from={}", fromTerm, ex);
			return List.of();
		}
	}

	@Override
	public void saveRelated(String normalizedQuery, List<String> terms) {
		if (normalizedQuery == null || normalizedQuery.isBlank()) {
			return;
		}
		String servingKey = searchProperties.relatedServingKeyPrefix() + normalizedQuery;
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
			log.warn("연관 검색어 서빙 저장 실패 q={}", normalizedQuery, ex);
			try {
				stringRedisTemplate.delete(buildingKey);
			} catch (RuntimeException ignored) {
				// 다음 베이크에서 overwrite
			}
		}
	}

	@Override
	public List<String> loadRelated(String normalizedQuery, int limit) {
		if (normalizedQuery == null || normalizedQuery.isBlank() || limit <= 0) {
			return List.of();
		}
		try {
			List<String> range = stringRedisTemplate.opsForList().range(
					searchProperties.relatedServingKeyPrefix() + normalizedQuery,
					0,
					limit - 1L
			);
			if (range == null || range.isEmpty()) {
				return List.of();
			}
			return Collections.unmodifiableList(new ArrayList<>(range));
		} catch (RuntimeException ex) {
			log.warn("연관 검색어 서빙 조회 실패 q={}", normalizedQuery, ex);
			return List.of();
		}
	}

	@Override
	public boolean tryLock() {
		try {
			Boolean acquired = stringRedisTemplate.opsForValue().setIfAbsent(
					searchProperties.relatedBakeLockKey(),
					searchProperties.bakeLockValue(),
					Duration.ofMillis(searchProperties.bakeLockTtlMs())
			);
			return Boolean.TRUE.equals(acquired);
		} catch (RuntimeException ex) {
			log.warn("연관 검색어 베이크 락 획득 실패", ex);
			return false;
		}
	}

	@Override
	public void unlock() {
		try {
			stringRedisTemplate.delete(searchProperties.relatedBakeLockKey());
		} catch (RuntimeException ex) {
			log.warn("연관 검색어 베이크 락 해제 실패", ex);
		}
	}
}
