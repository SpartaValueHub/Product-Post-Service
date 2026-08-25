package com.sparta.product_post_service.application.port.in.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.sparta.product_post_service.domain.model.ProductPostStatus;
import com.sparta.product_post_service.domain.model.TradeStatus;

import lombok.Builder;
import lombok.Getter;

// 판매글 요약 응답 DTO (Application → Inbound)
@Getter
@Builder
public class ProductPostSummaryDto {

	// 외부 공개용 판매글 UUID
	private final String productPostUuid;
	// 판매자 회원 UUID
	private final String memberUuid;
	// 카테고리 UUID
	private final String categoryUuid;
	// 상품명
	private final String productPostName;
	// 상품 상태 등급
	private final String conditionGrade;
	// 가격
	private final long price;
	// 상세 설명
	private final String description;
	// 거래 상태
	private final TradeStatus tradeStatus;
	// 노출 상태
	private final ProductPostStatus productPostStatus;
	// 위도
	private final BigDecimal latitude;
	// 경도
	private final BigDecimal longitude;
	// 거래 희망 동(읍면동, 없으면 null)
	private final String regionDong;
	// 거래 희망 구(시군구, 없으면 null)
	private final String regionGu;
	// 거래 장소명
	private final String placeName;
	// 마지막 끌올 시각
	private final Instant bumpedAt;
	// 생성 시각
	private final Instant createdAt;
	// 이미지 요약 목록
	private final List<ProductPostImageSummaryDto> images;
	// 서류 요약 목록
	private final List<ProductPostDocumentSummaryDto> documents;
}
