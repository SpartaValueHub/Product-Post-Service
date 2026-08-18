package com.sparta.product_post_service.application.port.in;

import com.sparta.product_post_service.application.port.in.dto.ProductPostSummaryDto;

// 판매글 끌올 Input Port
public interface BumpProductPostUseCase {

	// 끌올 (판매자 본인, SELLING + PUBLIC, 쿨다운·일일 한도 적용)
	ProductPostSummaryDto bump(String memberUuid, String productPostUuid);
}
