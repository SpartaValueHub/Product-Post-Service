package com.sparta.product_post_service.application.support;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

// 자동완성 prefix 매칭 (정규화된 후보 목록)
@Component
public class SuggestionPrefixMatcher {

	// prefix로 시작하는 후보만 수집 (입력 순서 유지, 중복 제거)
	public List<String> matchPrefix(List<String> candidates, String normalizedPrefix, int limit) {
		if (candidates == null || candidates.isEmpty()
				|| normalizedPrefix == null || normalizedPrefix.isBlank()
				|| limit <= 0) {
			return List.of();
		}
		List<String> matched = new ArrayList<>();
		for (String term : candidates) {
			if (term == null || term.isBlank()) {
				continue;
			}
			if (!term.startsWith(normalizedPrefix)) {
				continue;
			}
			if (matched.contains(term)) {
				continue;
			}
			matched.add(term);
			if (matched.size() >= limit) {
				break;
			}
		}
		return List.copyOf(matched);
	}
}
