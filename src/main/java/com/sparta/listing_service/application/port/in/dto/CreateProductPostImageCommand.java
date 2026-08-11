package com.sparta.listing_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

// 판매글 등록용 이미지 Command
@Getter
@Builder
public class CreateProductPostImageCommand {

	// 이미지 URL
	private final String imageUrl;
	// 노출 순서 (1 이상, 최소값이 대표)
	private final int sortOrder;
}
