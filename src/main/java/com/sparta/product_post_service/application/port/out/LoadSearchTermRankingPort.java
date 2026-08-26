package com.sparta.product_post_service.application.port.out;

import java.util.List;

// 검색어 카운터 ZSET에서 점수 내림차순 TopN 조회 (베이크용)
public interface LoadSearchTermRankingPort {

	// minScore 이상만, 최대 limit개
	List<String> loadTopTerms(int limit, double minScore);
}
