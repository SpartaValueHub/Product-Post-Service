package com.sparta.product_post_service.application.port.in;

// 검색어 카운터 → 추천 TopN 서빙 스냅샷 베이크
public interface BakePopularSearchTermsUseCase {

	// 분산 락 하에 TopN을 서빙 키에 저장. 락 실패 시 no-op
	void bake();
}
