package com.sparta.listing_service.adaptor.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.listing_service.adaptor.in.web.mapper.ListingWebMapper;
import com.sparta.listing_service.adaptor.in.web.vo.CreateListingRequestVo;
import com.sparta.listing_service.adaptor.in.web.vo.ListingSummaryResponseVo;
import com.sparta.listing_service.application.port.in.CreateListingUseCase;
import com.sparta.listing_service.application.port.in.dto.ListingSummaryDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 판매글 API Controller (홀 직원: 주문만 받고 주방장에게 전달)
@RestController
@RequestMapping("/api/v1/listings")
@RequiredArgsConstructor
public class ListingController {

	// 판매글 등록 UseCase
	private final CreateListingUseCase createListingUseCase;

	// 판매글 등록 (FO)
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ListingSummaryResponseVo create(
			@RequestHeader(value = InternalAuthHeaders.MEMBER_UUID, required = false) String memberUuid,
			@Valid @RequestBody CreateListingRequestVo request
	) {
		ListingSummaryDto created = createListingUseCase.create(
				memberUuid,
				ListingWebMapper.toCreateCommand(request)
		);
		return ListingWebMapper.toSummaryResponse(created);
	}
}
