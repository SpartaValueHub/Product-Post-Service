package com.sparta.product_post_service.adaptor.out.search;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.sparta.product_post_service.application.port.out.LoadRelatedSearchTermsPort;
import com.sparta.product_post_service.application.support.SearchTermNormalizer;
import com.sparta.product_post_service.config.SearchProperties;

// YAML 사전 기반 연관 검색어 (키·값은 정규화 후 매칭)
@Component
public class PropertiesRelatedSearchTermAdapter implements LoadRelatedSearchTermsPort {

	// 정규화 키 → 연관어 목록
	private final Map<String, List<String>> relatedByNormalizedQuery;

	public PropertiesRelatedSearchTermAdapter(SearchProperties searchProperties) {
		int keywordMaxLength = searchProperties.keywordMaxLength();
		Map<String, List<String>> normalized = new LinkedHashMap<>();
		for (Map.Entry<String, List<String>> entry : searchProperties.related().entrySet()) {
			String key = SearchTermNormalizer.normalize(entry.getKey(), keywordMaxLength);
			if (key == null) {
				continue;
			}
			List<String> values = new ArrayList<>();
			if (entry.getValue() != null) {
				for (String raw : entry.getValue()) {
					String term = SearchTermNormalizer.normalize(raw, keywordMaxLength);
					if (term != null && !term.equals(key) && !values.contains(term)) {
						values.add(term);
					}
				}
			}
			normalized.put(key, List.copyOf(values));
		}
		this.relatedByNormalizedQuery = Map.copyOf(normalized);
	}

	@Override
	public List<String> loadRelated(String normalizedQuery) {
		if (normalizedQuery == null || normalizedQuery.isBlank()) {
			return List.of();
		}
		List<String> related = relatedByNormalizedQuery.get(normalizedQuery);
		return related == null ? List.of() : related;
	}
}
