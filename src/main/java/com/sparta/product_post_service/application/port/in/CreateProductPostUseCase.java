package com.sparta.product_post_service.application.port.in;

import com.sparta.product_post_service.application.port.in.dto.CreateProductPostCommand;
import com.sparta.product_post_service.application.port.in.dto.ProductPostSummaryDto;

// 판매글 등록 UseCase
public interface CreateProductPostUseCase {

	// 판매글 등록 (memberUuid는 Gateway 헤더에서 전달)
	ProductPostSummaryDto create(String memberUuid, CreateProductPostCommand command);
}
