package com.sparta.product_post_service.adaptor.in.web.vo;

import com.sparta.product_post_service.domain.model.DocumentType;

import lombok.Builder;
import lombok.Getter;

// 판매글 서류 HTTP 응답 VO
@Getter
@Builder
public class ProductPostDocumentResponseVo {

	// 서류 UUID
	private final String productPostDocumentUuid;
	// 서류 종류
	private final DocumentType documentType;
	// 서류 이미지 URL
	private final String imageUrl;
}
