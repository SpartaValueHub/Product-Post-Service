package com.sparta.product_post_service.application.port.out;

import java.util.List;

// 인기 검색어 조회 (Redis TopN 등)
public interface LoadPopularSearchTermsPort {

	// 점수 내림차순 limit개. 장애·빈 결과면 빈 리스트
	List<String> loadPopular(int limit);
}
