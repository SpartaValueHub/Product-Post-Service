package com.sparta.product_post_service.application.port.out.dto;

import java.time.Instant;

import com.sparta.product_post_service.domain.model.TradeStatus;

import lombok.Builder;
import lombok.Getter;

// 목록 카드 Projection (Outbound → Application)
@Getter
@Builder
public class ProductPostCardProjection {

	// 외부 공개용 판매글 UUID
	private final String productPostUuid;
	// 상품명
	private final String productPostName;
	// 가격
	private final long price;
	// 거래 상태 (카드 뱃지)
	private final TradeStatus tradeStatus;
	// 목록 정렬·상대시간 기준 시각 (bumpedAt 없으면 createdAt)
	private final Instant listedAt;
	// 대표 이미지 URL (없으면 null)
	private final String thumbnailUrl;
	// 거래 희망 동(읍면동, 없으면 null)
	private final String regionDong;
	// 거래 희망 구(시군구, 없으면 null)
	private final String regionGu;
	// 거래 희망 장소명
	private final String placeName;
}
