package com.sparta.product_post_service.application.port.out;

import java.util.List;

// 베이크된 연관 검색어 서빙 조회
public interface LoadBakedRelatedSearchTermsPort {

	List<String> loadRelated(String normalizedQuery, int limit);
}
