package com.sparta.product_post_service.application.port.in;

// 동시검색 카운터 → 연관 서빙 스냅샷 베이크
public interface BakeRelatedSearchTermsUseCase {

	void bake();
}
