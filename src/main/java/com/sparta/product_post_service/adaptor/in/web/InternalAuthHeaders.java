package com.sparta.product_post_service.adaptor.in.web;

// Gateway가 JWT 검증 후 내려주는 내부 헤더 이름 (Gateway InternalAuthHeaderWebFilter와 동일)
public final class InternalAuthHeaders {

	// 판매자(회원) UUID / 로그인 검색자 UUID
	public static final String MEMBER_UUID = "X-Member-Uuid";
	// 역할 (필요 시 사용)
	public static final String ROLE = "X-Role";
	// FE 발급 검색 세션 ID (동시검색용, 비로그인 포함)
	public static final String SEARCH_SESSION_ID = "X-Search-Session-Id";

	private InternalAuthHeaders() {
	}
}
