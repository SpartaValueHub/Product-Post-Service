package com.sparta.listing_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

// 판매글 이미지 요약 DTO
@Getter
@Builder
public class ListingImageSummaryDto {

	// 이미지 UUID
	private final String listingImageUuid;
	// 이미지 URL
	private final String imageUrl;
	// 노출 순서
	private final int sortOrder;
}
