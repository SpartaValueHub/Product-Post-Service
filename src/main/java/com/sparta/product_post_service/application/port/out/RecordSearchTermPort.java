package com.sparta.product_post_service.application.port.out;

// 검색어 카운터 기록 (비동기·실패 무시 가능)
public interface RecordSearchTermPort {

	// 정규화된 검색어 점수 +1
	void record(String normalizedTerm);
}
