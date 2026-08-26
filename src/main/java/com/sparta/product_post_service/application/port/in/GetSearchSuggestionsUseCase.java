package com.sparta.product_post_service.application.port.in;

import java.util.List;

// 검색 자동완성(prefix) 조회
public interface GetSearchSuggestionsUseCase {

	List<String> getSuggestions(String query);
}
