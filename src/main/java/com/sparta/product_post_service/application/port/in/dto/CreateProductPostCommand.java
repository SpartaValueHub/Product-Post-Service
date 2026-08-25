package com.sparta.product_post_service.application.port.in.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

// 판매글 등록 요청 Command (Application 계약)
@Getter
@Builder
public class CreateProductPostCommand {

	// 리프 카테고리 UUID
	private final String categoryUuid;
	// 상품명
	private final String productPostName;
	// 상품 상태 등급 (S/A/B/C)
	private final String conditionGrade;
	// 가격
	private final long price;
	// 상세 설명
	private final String description;
	// 위도
	private final BigDecimal latitude;
	// 경도
	private final BigDecimal longitude;
	// 거래 장소명
	private final String placeName;
	// 거래 희망 동(읍면동, optional)
	private final String regionDong;
	// 거래 희망 구(시군구, optional)
	private final String regionGu;
	// 상품 이미지 목록
	private final List<CreateProductPostImageCommand> images;
	// 서류 목록 (선택)
	private final List<CreateProductPostDocumentCommand> documents;
}
