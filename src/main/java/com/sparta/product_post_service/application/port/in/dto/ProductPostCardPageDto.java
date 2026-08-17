package com.sparta.product_post_service.application.port.in.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

// 판매글 목록 페이지 응답 DTO (Application → Inbound)
@Getter
@Builder
public class ProductPostCardPageDto {

	// 현재 페이지 카드 목록
	private final List<ProductPostCardDto> content;
	// 1-based 현재 페이지
	private final int page;
	// 페이지 크기
	private final int size;
	// 전체 건수
	private final long totalElements;
	// 전체 페이지 수
	private final int totalPages;
}
