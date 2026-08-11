package com.sparta.listing_service.adaptor.in.web.vo;

import lombok.Builder;
import lombok.Getter;

// 판매글 이미지 HTTP 응답 VO
@Getter
@Builder
public class ProductPostImageResponseVo {

	// 이미지 UUID
	private final String productPostImageUuid;
	// 이미지 URL
	private final String imageUrl;
	// 노출 순서
	private final int sortOrder;
}
