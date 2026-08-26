package com.sparta.product_post_service.application.support;

import org.springframework.stereotype.Component;

import com.sparta.product_post_service.config.SearchProperties;

import lombok.RequiredArgsConstructor;

// 동시검색용 세션 키 조합 (회원 우선, 없으면 익명 세션 헤더)
@Component
@RequiredArgsConstructor
public class SearchSessionKeyResolver {

	// 검색 설정
	private final SearchProperties searchProperties;

	// 세션 키. 둘 다 없으면 null (동시검색 기록 생략)
	public String resolve(String searcherMemberUuid, String searchSessionId) {
		if (searcherMemberUuid != null && !searcherMemberUuid.isBlank()) {
			return searchProperties.sessionMemberPrefix() + searcherMemberUuid.trim();
		}
		if (searchSessionId != null && !searchSessionId.isBlank()) {
			return searchProperties.sessionAnonymousPrefix() + searchSessionId.trim();
		}
		return null;
	}
}
