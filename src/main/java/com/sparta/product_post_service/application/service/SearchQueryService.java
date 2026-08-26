package com.sparta.product_post_service.application.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.sparta.product_post_service.application.port.in.GetPopularSearchTermsUseCase;
import com.sparta.product_post_service.application.port.in.GetRelatedSearchTermsUseCase;
import com.sparta.product_post_service.application.port.out.LoadPopularSearchTermsPort;
import com.sparta.product_post_service.application.port.out.LoadRelatedSearchTermsPort;
import com.sparta.product_post_service.application.support.SearchTermNormalizer;
import com.sparta.product_post_service.config.SearchProperties;

import lombok.RequiredArgsConstructor;

// 추천·연관 검색어 Application Service (원본 판매글 테이블 미조회)
@Service
@RequiredArgsConstructor
public class SearchQueryService implements GetPopularSearchTermsUseCase, GetRelatedSearchTermsUseCase {

	// 인기 검색어 Port
	private final LoadPopularSearchTermsPort loadPopularSearchTermsPort;
	// 연관 검색어 Port
	private final LoadRelatedSearchTermsPort loadRelatedSearchTermsPort;
	// 검색 정책·시드
	private final SearchProperties searchProperties;

	@Override
	public List<String> getPopular() {
		int limit = searchProperties.popularLimit();
		List<String> fromRedis = loadPopularSearchTermsPort.loadPopular(limit);
		if (!fromRedis.isEmpty()) {
			return cap(fromRedis, limit);
		}
		return cap(normalizeSeed(searchProperties.popularSeed()), limit);
	}

	@Override
	public List<String> getRelated(String query) {
		int limit = searchProperties.popularLimit();
		String normalized = SearchTermNormalizer.normalize(query, searchProperties.keywordMaxLength());
		if (normalized == null) {
			return List.of();
		}

		List<String> related = loadRelatedSearchTermsPort.loadRelated(normalized);
		List<String> result = new ArrayList<>();
		for (String term : related) {
			if (term == null || term.equals(normalized) || result.contains(term)) {
				continue;
			}
			result.add(term);
			if (result.size() >= limit) {
				return List.copyOf(result);
			}
		}

		if (result.isEmpty()) {
			for (String term : getPopular()) {
				if (term.equals(normalized) || result.contains(term)) {
					continue;
				}
				result.add(term);
				if (result.size() >= limit) {
					break;
				}
			}
		}
		return List.copyOf(result);
	}

	private List<String> normalizeSeed(List<String> seed) {
		Set<String> unique = new LinkedHashSet<>();
		int maxLength = searchProperties.keywordMaxLength();
		for (String raw : seed) {
			String term = SearchTermNormalizer.normalize(raw, maxLength);
			if (term != null) {
				unique.add(term);
			}
		}
		return List.copyOf(unique);
	}

	private static List<String> cap(List<String> terms, int limit) {
		if (terms.size() <= limit) {
			return List.copyOf(terms);
		}
		return List.copyOf(terms.subList(0, limit));
	}
}
