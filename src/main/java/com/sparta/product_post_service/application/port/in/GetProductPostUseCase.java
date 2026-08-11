package com.sparta.product_post_service.application.port.in;

import com.sparta.product_post_service.application.port.in.dto.ProductPostSummaryDto;

// 판매글 상세 조회 UseCase
public interface GetProductPostUseCase {

	// 판매글 UUID로 공개 상세를 조회한다
	ProductPostSummaryDto getByUuid(String productPostUuid);
}
