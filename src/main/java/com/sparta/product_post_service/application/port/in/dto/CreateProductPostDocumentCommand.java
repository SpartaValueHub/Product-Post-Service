package com.sparta.product_post_service.application.port.in.dto;

import com.sparta.product_post_service.domain.model.DocumentType;

import lombok.Builder;
import lombok.Getter;

// 판매글 등록용 서류 Command
@Getter
@Builder
public class CreateProductPostDocumentCommand {

	// 서류 종류
	private final DocumentType documentType;
	// 서류 이미지 URL
	private final String imageUrl;
}
