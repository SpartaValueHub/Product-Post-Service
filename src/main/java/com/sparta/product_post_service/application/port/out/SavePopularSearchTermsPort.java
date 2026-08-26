package com.sparta.product_post_service.application.port.out;

import java.util.List;

// 추천 검색어 서빙 스냅샷 저장
public interface SavePopularSearchTermsPort {

	// 기존 스냅샷을 새 TopN으로 교체 (빈 리스트면 서빙 키 삭제 → API seed fallback)
	void savePopular(List<String> terms);
}
