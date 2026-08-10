package com.sparta.listing_service.application.port.in;

import com.sparta.listing_service.application.port.in.dto.CreateListingCommand;
import com.sparta.listing_service.application.port.in.dto.ListingSummaryDto;

// 판매글 등록 UseCase
public interface CreateListingUseCase {

	// 판매글 등록 (memberUuid는 Gateway 헤더에서 전달)
	ListingSummaryDto create(String memberUuid, CreateListingCommand command);
}
