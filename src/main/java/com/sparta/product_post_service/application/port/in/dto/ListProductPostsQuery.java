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
	// 판매자(회원) UUID — 해당 회원의 글만 (프로필 모달용, null/blank면 미적용)
	private final String memberUuid;
	// 검색자 회원 UUID (Gateway X-Member-Uuid, 동시검색 세션용 — 판매자 memberUuid와 별개)
	private final String searcherMemberUuid;
	// FE 검색 세션 ID (X-Search-Session-Id, 비로그인 동시검색용)
	private final String searchSessionId;
	// 거래 상태 필터 (SELLING|RESERVED|SOLD_OUT, null/blank면 세 상태 전체)
	private final String tradeStatus;
	// 상품명 검색어
	private final String keyword;
	// 최소 가격
	private final Long minPrice;
	// 최대 가격
	private final Long maxPrice;
	// 상품 상태 등급 (S/A/B/C)
	private final List<String> conditionGrades;
	// 인증 서류 종류 (WARRANTY/RECEIPT/APPRAISAL/OTHER) — 선택 중 하나라도 있으면 포함(OR)
	private final List<String> documentTypes;
	// 1-based 페이지 번호
	private final int page;
	// 페이지 크기
	private final int size;
}
