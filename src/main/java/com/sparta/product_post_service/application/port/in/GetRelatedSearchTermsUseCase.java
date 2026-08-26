package com.sparta.product_post_service.application.port.in;

import java.util.List;

// 연관 검색어 조회
public interface GetRelatedSearchTermsUseCase {

	List<String> getRelated(String query);
}
