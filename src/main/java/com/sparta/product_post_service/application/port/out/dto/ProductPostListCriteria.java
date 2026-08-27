package com.sparta.product_post_service.application.port.out.dto;

import java.util.List;

import com.sparta.product_post_service.domain.model.DocumentType;
import com.sparta.product_post_service.application.port.out.dto.ProductPostListGeoFilter;
import com.sparta.product_post_service.domain.model.ProductPostStatus;
import com.sparta.product_post_service.domain.model.TradeStatus;

import lombok.Builder;
import lombok.Getter;

// 목록 조회 조건 (Application → Outbound, Pageable 미포함)
@Getter
@Builder
public class ProductPostListCriteria {

	// 게시 상태 (Application이 목록 정책에 맞게 지정)
	private final ProductPostStatus productPostStatus;
	// 리프 카테고리 UUID 목록 (비어 있으면 전체)
	private final List<String> categoryUuids;
	// 판매자(회원) UUID (null이면 미적용)
	private final String memberUuid;
	// 거래 상태 IN 조건 (Application에서 미필터 시 노출 가능 상태 전체로 채움)
	private final List<TradeStatus> tradeStatuses;
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
	// 반경 필터 (Application이 memberUuid·좌표 정책으로 해석)
	private final ProductPostListGeoFilter geoFilter;
	// 0-based 페이지 번호
	private final int page;
	// 페이지 크기
	private final int size;
}
