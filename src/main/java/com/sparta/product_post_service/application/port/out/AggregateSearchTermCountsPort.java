package com.sparta.product_post_service.application.port.out;

import java.util.List;
import java.util.Map;

// 시간 버킷 ZSET들을 SUM 집계해 검색어별 건수를 반환
public interface AggregateSearchTermCountsPort {

	// hourBucketKeys 점수 합산 후 상위 limit개 (limit<=0 이면 빈 맵)
	Map<String, Double> aggregateTopCounts(List<String> hourBucketKeys, int limit);
}
