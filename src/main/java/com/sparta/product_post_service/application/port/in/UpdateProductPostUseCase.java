package com.sparta.product_post_service.application.port.in;

import com.sparta.product_post_service.application.port.in.dto.ProductPostSummaryDto;
import com.sparta.product_post_service.application.port.in.dto.UpdateProductPostCommand;

// 판매글 수정 Input Port
public interface UpdateProductPostUseCase {

	// 판매글 수정 (판매자 본인, SELLING 상태만)
	ProductPostSummaryDto update(String memberUuid, String productPostUuid, UpdateProductPostCommand command);
}
