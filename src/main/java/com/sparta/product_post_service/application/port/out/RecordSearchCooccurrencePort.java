package com.sparta.product_post_service.application.port.out;

// 동시검색(A→B) 및 세션 직전 검색어 기록
public interface RecordSearchCooccurrencePort {

	// sessionKey 기준 직전 검색어와 currentTerm 쌍을 카운트하고 직전값을 갱신
	void recordTransition(String sessionKey, String currentTerm);
}
