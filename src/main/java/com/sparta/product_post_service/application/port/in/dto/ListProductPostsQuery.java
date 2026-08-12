package com.sparta.product_post_service.application.port.in.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

// 판매글 목록 조회 요청 (Inbound → Application)
@Getter
@Builder
public class ListProductPostsQuery {

	// 리프 카테고리 UUID 목록 (All/전체상품이면 비움 — FE가 하위 리프를 채움)
	private final List<String> categoryUuids;
	// 상품명 검색어
	private final String keyword;
	// 최소 가격
	private final Long minPrice;
	// 최대 가격
	private final Long maxPrice;
	// 상품 상태 등급 (S/A/B/C)
	private final List<String> conditionGrades;
	// 인증 서류 종류 (WARRANTY/RECEIPT/APPRAISAL) — 선택 중 하나라도 있으면 포함(OR)
	private final List<String> documentTypes;
	// 1-based 페이지 번호
	private final int page;
	// 페이지 크기
	private final int size;
}
