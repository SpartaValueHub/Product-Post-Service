package com.sparta.listing_service.adaptor.in.web.vo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.sparta.listing_service.domain.model.ListingStatus;
import com.sparta.listing_service.domain.model.TradeStatus;

import lombok.Builder;
import lombok.Getter;

// 판매글 요약 HTTP 응답 VO
@Getter
@Builder
public class ListingSummaryResponseVo {

	// 판매글 UUID
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
	// 이미지 목록
	private final List<ListingImageResponseVo> images;
	// 서류 목록
	private final List<ListingDocumentResponseVo> documents;
}
