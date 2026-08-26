package com.sparta.product_post_service.config;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

// 헤더 검색(추천·연관·키워드) 정책
@ConfigurationProperties(prefix = "product-post.search")
public record SearchProperties(
		// 추천·연관 최대 개수
		int popularLimit,
		// 검색어 최대 길이 (정규화 후 truncate)
		int keywordMaxLength,
		// FULLTEXT(ngram) 최소 검색어 길이 (미만이면 목록 빈 결과)
		int fulltextMinKeywordLength,
		// 검색어 점수 ZSET 키 (기록용)
		String termsZsetKey,
		// 추천 검색어 서빙 LIST 키 (배치 스냅샷)
		String popularServingKey,
		// TopN 베이크 주기 (ms)
		long bakeIntervalMs,
		// 앱 기동 후 첫 베이크까지 대기 (ms)
		long bakeInitialDelayMs,
		// TopN에 넣을 최소 ZSET 점수
		double bakeMinScore,
		// 다중 인스턴스 베이크 분산 락 키
		String bakeLockKey,
		// 분산 락 TTL (ms)
		long bakeLockTtlMs,
		// Redis 서빙 비어 있을 때 추천 시드
		List<String> popularSeed,
		// 정규화 전 원문 키 → 연관 검색어 (서비스에서 정규화 키로 조회)
		Map<String, List<String>> related
) {
	public SearchProperties {
		if (popularLimit <= 0) {
			popularLimit = 5;
		}
		if (keywordMaxLength <= 0) {
			keywordMaxLength = 50;
		}
		if (fulltextMinKeywordLength <= 0) {
			fulltextMinKeywordLength = 2;
		}
		if (termsZsetKey == null || termsZsetKey.isBlank()) {
			termsZsetKey = "search:terms:z";
		}
		if (popularServingKey == null || popularServingKey.isBlank()) {
			popularServingKey = "search:popular";
		}
		if (bakeIntervalMs <= 0) {
			bakeIntervalMs = 3_600_000L;
		}
		if (bakeInitialDelayMs < 0) {
			bakeInitialDelayMs = 60_000L;
		}
		if (bakeMinScore < 0) {
			bakeMinScore = 1.0d;
		}
		if (bakeLockKey == null || bakeLockKey.isBlank()) {
			bakeLockKey = "search:popular:bake-lock";
		}
		if (bakeLockTtlMs <= 0) {
			bakeLockTtlMs = 300_000L;
		}
		popularSeed = popularSeed == null ? List.of() : List.copyOf(popularSeed);
		related = related == null ? Map.of() : Map.copyOf(related);
	}
}
