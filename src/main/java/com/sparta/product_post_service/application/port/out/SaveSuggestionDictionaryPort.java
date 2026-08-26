package com.sparta.product_post_service.application.port.out;

import java.util.List;

// 자동완성 사전 스냅샷 저장
public interface SaveSuggestionDictionaryPort {

	void saveDictionary(List<String> terms);
}
