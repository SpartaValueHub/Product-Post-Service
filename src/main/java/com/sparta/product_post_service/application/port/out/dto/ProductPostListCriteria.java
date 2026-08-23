package com.sparta.product_post_service.application.port.out.dto;

import java.util.List;

import com.sparta.product_post_service.domain.model.DocumentType;

import lombok.Builder;
import lombok.Getter;

// 목록 조회 조건 (Application → Outbound, Pageable 미포함)
@Getter
@Builder
public class ProductPostListCriteria {

	// 리프 카테고리 UUID 목록 (비어 있으면 전체)
	private final List<String> categoryUuids;
	// 판매자(회원) UUID (null이면 미적용)
	private final String memberUuid;
	// 상품명 검색어 (null/blank면 미적용)
	private final String keyword;
	// 최소 가격 (null이면 미적용)
	private final Long minPrice;
	// 최대 가격 (null이면 미적용)
	private final Long maxPrice;
	// 상태 등급 목록 (비어 있으면 전체)
	private final List<String> conditionGrades;
	// 서류 종류 목록 (비어 있으면 미적용, OR 매칭)
	private final List<DocumentType> documentTypes;
	// 0-based 페이지 번호
	private final int page;
	// 페이지 크기
	private final int size;
}
