package com.sparta.product_post_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

// 판매글 등록용 이미지 Command (순서는 Application이 배열 인덱스로 부여)
@Getter
@Builder
public class CreateProductPostImageCommand {

	// 이미지 URL
	private final String imageUrl;
}
