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
		// 검색어 점수 ZSET 키
		String termsZsetKey,
		// Redis 비어 있을 때 추천 시드
		List<String> popularSeed,
		// 정규화 전 원문 키 → 연관 검색어 (서비스에서 정규화 키로 조회)
		Map<String, List<String>> related
) {
	public SearchProperties {
		if (popularLimit <= 0) {
			popularLimit = 10;
		}
		if (keywordMaxLength <= 0) {
			keywordMaxLength = 50;
		}
		if (termsZsetKey == null || termsZsetKey.isBlank()) {
			termsZsetKey = "search:terms:z";
		}
		popularSeed = popularSeed == null ? List.of() : List.copyOf(popularSeed);
		related = related == null ? Map.of() : Map.copyOf(related);
	}
}
