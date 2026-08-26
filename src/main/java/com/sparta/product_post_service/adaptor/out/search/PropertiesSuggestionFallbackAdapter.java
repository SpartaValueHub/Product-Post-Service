package com.sparta.product_post_service.adaptor.out.search;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.sparta.product_post_service.application.port.out.LoadFallbackSuggestionTermsPort;
import com.sparta.product_post_service.application.support.SearchTermNormalizer;
import com.sparta.product_post_service.config.SearchProperties;

// YAML popular-seed·related 기반 자동완성 fallback 후보
@Component
public class PropertiesSuggestionFallbackAdapter implements LoadFallbackSuggestionTermsPort {

	// 정규화된 후보 (시드 → 연관 키 → 연관 값 순)
	private final List<String> candidates;

	public PropertiesSuggestionFallbackAdapter(SearchProperties searchProperties) {
		int maxLength = searchProperties.keywordMaxLength();
		Set<String> unique = new LinkedHashSet<>();
		for (String raw : searchProperties.popularSeed()) {
			String term = SearchTermNormalizer.normalize(raw, maxLength);
			if (term != null) {
				unique.add(term);
			}
		}
		for (Map.Entry<String, List<String>> entry : searchProperties.related().entrySet()) {
			String key = SearchTermNormalizer.normalize(entry.getKey(), maxLength);
			if (key != null) {
				unique.add(key);
			}
			if (entry.getValue() == null) {
				continue;
			}
			for (String raw : entry.getValue()) {
				String term = SearchTermNormalizer.normalize(raw, maxLength);
				if (term != null) {
					unique.add(term);
				}
			}
		}
		this.candidates = List.copyOf(unique);
	}

	@Override
	public List<String> loadCandidates() {
		return candidates;
	}
}
