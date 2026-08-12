package com.sparta.product_post_service.adaptor.in.web.vo;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

// 판매글 목록 페이지 응답 VO
@Getter
@Builder
public class ProductPostCardPageResponseVo {

	// 현재 페이지 카드 목록
	private final List<ProductPostCardResponseVo> content;
	// 1-based 현재 페이지
	private final int page;
	// 페이지 크기
	private final int size;
	// 전체 건수
	private final long totalElements;
	// 전체 페이지 수
	private final int totalPages;
}
