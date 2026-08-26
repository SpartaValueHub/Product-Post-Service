package com.sparta.product_post_service.application.port.in;

import java.util.List;

// 추천(인기) 검색어 조회
public interface GetPopularSearchTermsUseCase {

	List<String> getPopular();
}
