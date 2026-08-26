package com.sparta.product_post_service.adaptor.out.redis;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.sparta.product_post_service.application.port.out.LoadSuggestionTermsPort;
import com.sparta.product_post_service.application.port.out.SaveSuggestionDictionaryPort;
import com.sparta.product_post_service.application.port.out.SuggestionDictionaryBakeLockPort;
import com.sparta.product_post_service.config.SearchProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 자동완성 사전 Redis 어댑터 (lex ZSET, 장애 시 빈 결과 / no-op)
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSuggestionDictionaryAdapter implements
		LoadSuggestionTermsPort,
		SaveSuggestionDictionaryPort,
		SuggestionDictionaryBakeLockPort {

	// Redis
	private final StringRedisTemplate stringRedisTemplate;
	// 검색 설정
	private final SearchProperties searchProperties;

	@Override
	public List<String> loadByPrefix(String normalizedPrefix, int limit) {
		if (normalizedPrefix == null || normalizedPrefix.isBlank() || limit <= 0) {
			return List.of();
		}
		try {
			String key = searchProperties.suggestionsDictKey();
			// prefix ~ prefix+\uffff (UTF-8 prefix 범위)
			Range<String> lexRange = Range.closed(normalizedPrefix, normalizedPrefix + "\uffff");
			Set<String> range = stringRedisTemplate.opsForZSet().rangeByLex(
					key,
					lexRange,
					Limit.limit().count(limit)
			);
			if (range == null || range.isEmpty()) {
				return List.of();
			}
			return Collections.unmodifiableList(new ArrayList<>(range));
		} catch (RuntimeException ex) {
			log.warn("자동완성 사전 조회 실패 prefix={}", normalizedPrefix, ex);
			return List.of();
		}
	}

	@Override
	public void saveDictionary(List<String> terms) {
		String servingKey = searchProperties.suggestionsDictKey();
		String buildingKey = servingKey + searchProperties.snapshotBuildingSuffix();
		try {
			stringRedisTemplate.delete(buildingKey);
			if (terms == null || terms.isEmpty()) {
				stringRedisTemplate.delete(servingKey);
				return;
			}
			for (String term : terms) {
				if (term == null || term.isBlank()) {
					continue;
				}
				// score=0 — ZRANGEBYLEX 전용
				stringRedisTemplate.opsForZSet().add(buildingKey, term, 0.0d);
			}
			stringRedisTemplate.rename(buildingKey, servingKey);
		} catch (RuntimeException ex) {
			log.warn("자동완성 사전 저장 실패", ex);
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
					searchProperties.suggestionsBakeLockKey(),
					searchProperties.bakeLockValue(),
					Duration.ofMillis(searchProperties.bakeLockTtlMs())
			);
			return Boolean.TRUE.equals(acquired);
		} catch (RuntimeException ex) {
			log.warn("자동완성 사전 베이크 락 획득 실패", ex);
			return false;
		}
	}

	@Override
	public void unlock() {
		try {
			stringRedisTemplate.delete(searchProperties.suggestionsBakeLockKey());
		} catch (RuntimeException ex) {
			log.warn("자동완성 사전 베이크 락 해제 실패", ex);
		}
	}
}
