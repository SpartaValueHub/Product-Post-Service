package com.sparta.product_post_service.application.port.out.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

// 목록 페이지 Projection (Outbound → Application, Spring Page 타입 미사용)
@Getter
@Builder
public class ProductPostCardPageProjection {

	// 현재 페이지 카드 목록
	private final List<ProductPostCardProjection> content;
	// 전체 건수
	private final long totalElements;
	// 요청 페이지 (0-based, Adapter 내부 페이징 기준)
	private final int page;
	// 페이지 크기
	private final int size;
}
