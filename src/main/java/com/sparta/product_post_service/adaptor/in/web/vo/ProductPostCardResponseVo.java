package com.sparta.product_post_service.adaptor.in.web.vo;

import java.time.Instant;

import com.sparta.product_post_service.domain.model.TradeStatus;

import lombok.Builder;
import lombok.Getter;

// 판매글 목록 카드 응답 VO
@Getter
@Builder
public class ProductPostCardResponseVo {

	// 외부 공개용 판매글 UUID
	private final String productPostUuid;
	// 상품명
	private final String productPostName;
	// 가격
	private final long price;
	// 거래 상태 (카드 좌측 상단 뱃지)
	private final TradeStatus tradeStatus;
	// 목록 기준 시각 (FE 상대 시간)
	private final Instant listedAt;
	// 대표 이미지 URL
	private final String thumbnailUrl;
	// 거래 희망 동(읍면동, 없으면 null → FE는 구·장소명 순으로 fallback)
	private final String regionDong;
	// 거래 희망 구(시군구, 없으면 null)
	private final String regionGu;
	// 거래 희망 장소명
	private final String placeName;
}
