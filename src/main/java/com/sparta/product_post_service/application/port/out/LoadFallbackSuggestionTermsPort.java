package com.sparta.product_post_service.application.port.out;

import java.util.List;

// Redis 장애·빈 사전 시 자동완성 fallback 후보 (시드·YAML 사전)
public interface LoadFallbackSuggestionTermsPort {

	List<String> loadCandidates();
}
