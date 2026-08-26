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
		// LIST 스냅샷 교체용 임시 접미사
		String snapshotBuildingSuffix,
		// TopN 베이크 주기 (ms)
		long bakeIntervalMs,
		// 앱 기동 후 첫 베이크까지 대기 (ms)
		long bakeInitialDelayMs,
		// 추천 TopN에 넣을 최소 ZSET 점수
		double bakeMinScore,
		// 검색어 카운터 1회 증가분
		double termScoreIncrement,
		// 다중 인스턴스 추천 베이크 분산 락 키
		String bakeLockKey,
		// 분산 락 TTL (ms)
		long bakeLockTtlMs,
		// 분산 락 저장 값
		String bakeLockValue,
		// 동시검색 ZSET 키 prefix (뒤에 정규화 from 검색어)
		String cooccurrenceZsetKeyPrefix,
		// 동시검색이 발생한 from 검색어 SET
		String cooccurrenceSourcesKey,
		// 동시검색 1회 증가분
		double cooccurrenceScoreIncrement,
		// 연관 서빙 LIST 키 prefix (뒤에 정규화 q)
		String relatedServingKeyPrefix,
		// 연관 TopN 최소 동시검색 점수
		double relatedBakeMinScore,
		// 연관 베이크 분산 락 키
		String relatedBakeLockKey,
		// 세션 직전 검색어 키 prefix
		String sessionLastKeyPrefix,
		// 로그인 회원 세션 키 prefix
		String sessionMemberPrefix,
		// 익명(헤더) 세션 키 prefix
		String sessionAnonymousPrefix,
		// 세션 직전 검색어 TTL (ms)
		long sessionLastTtlMs,
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
		if (snapshotBuildingSuffix == null || snapshotBuildingSuffix.isBlank()) {
			snapshotBuildingSuffix = ":building";
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
		if (termScoreIncrement <= 0) {
			termScoreIncrement = 1.0d;
		}
		if (bakeLockKey == null || bakeLockKey.isBlank()) {
			bakeLockKey = "search:popular:bake-lock";
		}
		if (bakeLockTtlMs <= 0) {
			bakeLockTtlMs = 300_000L;
		}
		if (bakeLockValue == null || bakeLockValue.isBlank()) {
			bakeLockValue = "1";
		}
		if (cooccurrenceZsetKeyPrefix == null || cooccurrenceZsetKeyPrefix.isBlank()) {
			cooccurrenceZsetKeyPrefix = "search:cooc:z:";
		}
		if (cooccurrenceSourcesKey == null || cooccurrenceSourcesKey.isBlank()) {
			cooccurrenceSourcesKey = "search:cooc:sources";
		}
		if (cooccurrenceScoreIncrement <= 0) {
			cooccurrenceScoreIncrement = 1.0d;
		}
		if (relatedServingKeyPrefix == null || relatedServingKeyPrefix.isBlank()) {
			relatedServingKeyPrefix = "search:related:";
		}
		if (relatedBakeMinScore < 0) {
			relatedBakeMinScore = 2.0d;
		}
		if (relatedBakeLockKey == null || relatedBakeLockKey.isBlank()) {
			relatedBakeLockKey = "search:related:bake-lock";
		}
		if (sessionLastKeyPrefix == null || sessionLastKeyPrefix.isBlank()) {
			sessionLastKeyPrefix = "search:session:last:";
		}
		if (sessionMemberPrefix == null || sessionMemberPrefix.isBlank()) {
			sessionMemberPrefix = "m:";
		}
		if (sessionAnonymousPrefix == null || sessionAnonymousPrefix.isBlank()) {
			sessionAnonymousPrefix = "s:";
		}
		if (sessionLastTtlMs <= 0) {
			sessionLastTtlMs = 1_800_000L;
		}
		popularSeed = popularSeed == null ? List.of() : List.copyOf(popularSeed);
		related = related == null ? Map.of() : Map.copyOf(related);
	}
}
