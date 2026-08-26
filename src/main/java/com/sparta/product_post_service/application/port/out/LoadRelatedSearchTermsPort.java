package com.sparta.product_post_service.application.port.out;

import java.util.List;

// 연관 검색어 조회 (사전·캐시)
public interface LoadRelatedSearchTermsPort {

	// 정규화된 쿼리에 대한 연관어. 없으면 빈 리스트
	List<String> loadRelated(String normalizedQuery);
}
