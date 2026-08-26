package com.sparta.product_post_service.application.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.sparta.product_post_service.application.port.in.BakeSuggestionDictionaryUseCase;
import com.sparta.product_post_service.application.port.out.AggregateSearchTermCountsPort;
import com.sparta.product_post_service.application.port.out.LoadFallbackSuggestionTermsPort;
import com.sparta.product_post_service.application.port.out.SaveSuggestionDictionaryPort;
import com.sparta.product_post_service.application.port.out.SuggestionDictionaryBakeLockPort;
import com.sparta.product_post_service.application.support.SearchHourBucketSupport;
import com.sparta.product_post_service.config.SearchProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 최근 검색어·시드·YAML 사전으로 자동완성 Redis 사전을 베이크
@Slf4j
@Service
@RequiredArgsConstructor
public class SuggestionDictionaryBakeService implements BakeSuggestionDictionaryUseCase {

	// 시간 버킷 집계
	private final AggregateSearchTermCountsPort aggregateSearchTermCountsPort;
	// 시드·연관 후보
	private final LoadFallbackSuggestionTermsPort loadFallbackSuggestionTermsPort;
	// 사전 저장
	private final SaveSuggestionDictionaryPort saveSuggestionDictionaryPort;
	// 다중 인스턴스 락
	private final SuggestionDictionaryBakeLockPort suggestionDictionaryBakeLockPort;
	// 버킷 키
	private final SearchHourBucketSupport searchHourBucketSupport;
	// 검색 정책
	private final SearchProperties searchProperties;

	@Override
	public void bake() {
		if (!suggestionDictionaryBakeLockPort.tryLock()) {
			log.debug("자동완성 사전 베이크 스킵 (다른 인스턴스가 실행 중)");
			return;
		}
		try {
			int dictLimit = searchProperties.suggestionsDictLimit();
			List<String> hours7d = searchHourBucketSupport.recentHourBucketKeys(
					searchProperties.recentWindowHours7d()
			);
			Map<String, Double> recentTerms = aggregateSearchTermCountsPort.aggregateTopCounts(
					hours7d,
					dictLimit
			);

			Set<String> dictionary = new LinkedHashSet<>();
			// 최근 검색 상위 우선
			dictionary.addAll(recentTerms.keySet());
			dictionary.addAll(loadFallbackSuggestionTermsPort.loadCandidates());

			List<String> terms = new ArrayList<>(dictionary);
			if (terms.size() > dictLimit) {
				terms = new ArrayList<>(terms.subList(0, dictLimit));
			}
			saveSuggestionDictionaryPort.saveDictionary(terms);
			log.info("자동완성 사전 베이크 완료 size={}", terms.size());
		} catch (RuntimeException ex) {
			log.warn("자동완성 사전 베이크 실패", ex);
		} finally {
			suggestionDictionaryBakeLockPort.unlock();
		}
	}
}
