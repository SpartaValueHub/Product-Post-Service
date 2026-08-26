package com.sparta.product_post_service.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sparta.product_post_service.application.port.in.GetSearchSuggestionsUseCase;
import com.sparta.product_post_service.application.port.out.LoadFallbackSuggestionTermsPort;
import com.sparta.product_post_service.application.port.out.LoadSuggestionTermsPort;
import com.sparta.product_post_service.application.support.SearchTermNormalizer;
import com.sparta.product_post_service.application.support.SuggestionPrefixMatcher;
import com.sparta.product_post_service.config.SearchProperties;

import lombok.RequiredArgsConstructor;

// 검색 자동완성 Application Service (판매글 DB 미조회)
@Service
@RequiredArgsConstructor
public class SearchSuggestionQueryService implements GetSearchSuggestionsUseCase {

	// Redis 사전 prefix
	private final LoadSuggestionTermsPort loadSuggestionTermsPort;
	// YAML 시드·연관 fallback
	private final LoadFallbackSuggestionTermsPort loadFallbackSuggestionTermsPort;
	// prefix 매칭
	private final SuggestionPrefixMatcher suggestionPrefixMatcher;
	// 검색 정책
	private final SearchProperties searchProperties;

	@Override
	public List<String> getSuggestions(String query) {
		String normalized = SearchTermNormalizer.normalize(query, searchProperties.keywordMaxLength());
		if (normalized == null) {
			return List.of();
		}
		int minLength = searchProperties.suggestionsMinLength();
		if (normalized.length() < minLength) {
			return List.of();
		}
		int limit = searchProperties.popularLimit();
		List<String> fromRedis = loadSuggestionTermsPort.loadByPrefix(normalized, limit);
		if (!fromRedis.isEmpty()) {
			return fromRedis;
		}
		return suggestionPrefixMatcher.matchPrefix(
				loadFallbackSuggestionTermsPort.loadCandidates(),
				normalized,
				limit
		);
	}
}
