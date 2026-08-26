package com.sparta.product_post_service.application.port.out;

import java.util.List;

// 자동완성 prefix 조회 (서빙 사전)
public interface LoadSuggestionTermsPort {

	// normalizedPrefix 로 시작하는 검색어 (사전순, 최대 limit)
	List<String> loadByPrefix(String normalizedPrefix, int limit);
}
