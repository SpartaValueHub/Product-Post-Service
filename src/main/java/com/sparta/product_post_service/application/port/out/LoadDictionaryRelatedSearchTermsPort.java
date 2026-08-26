package com.sparta.product_post_service.application.port.out;

import java.util.List;

// YAML 사전 기반 연관 검색어
public interface LoadDictionaryRelatedSearchTermsPort {

	List<String> loadRelated(String normalizedQuery);
}
