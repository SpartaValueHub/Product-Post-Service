package com.sparta.product_post_service.application.port.out;

import java.util.List;

// 연관 검색어 서빙 스냅샷 저장
public interface SaveRelatedSearchTermsPort {

	// normalizedQuery 의 연관 LIST를 교체 (빈 리스트면 키 삭제)
	void saveRelated(String normalizedQuery, List<String> terms);
}
