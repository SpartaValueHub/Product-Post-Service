package com.sparta.product_post_service.application.port.in.dto;

import java.time.Instant;

import com.sparta.product_post_service.domain.model.TradeStatus;

import lombok.Builder;
import lombok.Getter;

// 판매글 목록 카드 응답 DTO (Application → Inbound)
@Getter
@Builder
public class ProductPostCardDto {

	// 외부 공개용 판매글 UUID
	private final String productPostUuid;
	// 상품명
	private final String productPostName;
	// 가격
	private final long price;
	// 거래 상태 (예약중·판매완료 뱃지)
	private final TradeStatus tradeStatus;
	// 목록 기준 시각 (상대 시간 표시용)
	private final Instant listedAt;
	// 대표 이미지 URL
	private final String thumbnailUrl;
}
