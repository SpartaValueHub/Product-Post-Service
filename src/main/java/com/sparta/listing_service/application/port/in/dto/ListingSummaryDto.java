package com.sparta.listing_service.application.port.in.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.sparta.listing_service.domain.model.ListingStatus;
import com.sparta.listing_service.domain.model.TradeStatus;

import lombok.Builder;
import lombok.Getter;

// 판매글 요약 응답 DTO (Application → Inbound)
@Getter
@Builder
public class ListingSummaryDto {

	// 외부 공개용 판매글 UUID
	private final String listingUuid;
	// 판매자 회원 UUID
	private final String memberUuid;
	// 카테고리 UUID
	private final String categoryUuid;
	// 상품명
	private final String listingName;
	// 상품 상태 등급
	private final String conditionGrade;
	// 가격
	private final long price;
	// 상세 설명
	private final String description;
	// 거래 상태
	private final TradeStatus tradeStatus;
	// 노출 상태
	private final ListingStatus listingStatus;
	// 위도
	private final BigDecimal latitude;
	// 경도
	private final BigDecimal longitude;
	// 거래 장소명
	private final String placeName;
	// 생성 시각
	private final Instant createdAt;
	// 이미지 요약 목록
	private final List<ListingImageSummaryDto> images;
	// 서류 요약 목록
	private final List<ListingDocumentSummaryDto> documents;
}
