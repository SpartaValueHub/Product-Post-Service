package com.sparta.product_post_service.application.port.out;

import java.util.List;
import java.util.Set;

// 동시검색 카운터에서 베이크 대상·TopN 조회
public interface LoadSearchCooccurrencePort {

	// 동시검색이 발생한 from 검색어 집합
	Set<String> loadSourceTerms();

	// fromTerm 기준 연관 TopN (minScore 이상, 자기 자신 제외는 Application에서 처리 가능)
	List<String> loadTopRelated(String fromTerm, int limit, double minScore);
}
