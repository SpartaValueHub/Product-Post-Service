package com.sparta.product_post_service.adaptor.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.product_post_service.adaptor.in.web.mapper.ProductPostWebMapper;
import com.sparta.product_post_service.adaptor.in.web.vo.CreateProductPostRequestVo;
import com.sparta.product_post_service.adaptor.in.web.vo.ProductPostSummaryResponseVo;
import com.sparta.product_post_service.application.port.in.CreateProductPostUseCase;
import com.sparta.product_post_service.application.port.in.GetProductPostUseCase;
import com.sparta.product_post_service.application.port.in.dto.ProductPostSummaryDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 판매글 API Controller (홀 직원: 주문만 받고 주방장에게 전달)
@RestController
@RequestMapping("/api/v1/product-posts")
@RequiredArgsConstructor
public class ProductPostController {

	// 판매글 등록 UseCase
	private final CreateProductPostUseCase createProductPostUseCase;
	// 판매글 상세 조회 UseCase
	private final GetProductPostUseCase getProductPostUseCase;

	// 판매글 등록 (FO)
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ProductPostSummaryResponseVo create(
			@RequestHeader(value = InternalAuthHeaders.MEMBER_UUID, required = false) String memberUuid,
			@Valid @RequestBody CreateProductPostRequestVo request
	) {
		ProductPostSummaryDto created = createProductPostUseCase.create(
				memberUuid,
				ProductPostWebMapper.toCreateCommand(request)
		);
		return ProductPostWebMapper.toSummaryResponse(created);
	}

	// 판매글 상세 조회 (FO, 공개글만)
	@GetMapping("/{productPostUuid}")
	public ProductPostSummaryResponseVo getDetail(@PathVariable String productPostUuid) {
		ProductPostSummaryDto detail = getProductPostUseCase.getByUuid(productPostUuid);
		return ProductPostWebMapper.toSummaryResponse(detail);
	}
}
