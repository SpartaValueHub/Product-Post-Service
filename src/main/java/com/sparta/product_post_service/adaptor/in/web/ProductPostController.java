package com.sparta.product_post_service.adaptor.in.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.product_post_service.adaptor.in.web.mapper.ProductPostWebMapper;
import com.sparta.product_post_service.adaptor.in.web.vo.ChangeProductPostTradeStatusRequestVo;
import com.sparta.product_post_service.adaptor.in.web.vo.ChangeProductPostVisibilityRequestVo;
import com.sparta.product_post_service.adaptor.in.web.vo.CreateProductPostRequestVo;
import com.sparta.product_post_service.adaptor.in.web.vo.ProductPostCardPageResponseVo;
import com.sparta.product_post_service.adaptor.in.web.vo.ProductPostSummaryResponseVo;
import com.sparta.product_post_service.adaptor.in.web.vo.UpdateProductPostRequestVo;
import com.sparta.product_post_service.application.port.in.BumpProductPostUseCase;
import com.sparta.product_post_service.application.port.in.ChangeProductPostTradeStatusUseCase;
import com.sparta.product_post_service.application.port.in.ChangeProductPostVisibilityUseCase;
import com.sparta.product_post_service.application.port.in.CreateProductPostUseCase;
import com.sparta.product_post_service.application.port.in.DeleteProductPostUseCase;
import com.sparta.product_post_service.application.port.in.GetProductPostUseCase;
import com.sparta.product_post_service.application.port.in.ListProductPostsUseCase;
import com.sparta.product_post_service.application.port.in.UpdateProductPostUseCase;
import com.sparta.product_post_service.application.port.in.dto.ProductPostCardPageDto;
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
	// 판매글 수정 UseCase
	private final UpdateProductPostUseCase updateProductPostUseCase;
	// 판매글 삭제 UseCase
	private final DeleteProductPostUseCase deleteProductPostUseCase;
	// 판매글 노출 상태 변경 UseCase
	private final ChangeProductPostVisibilityUseCase changeProductPostVisibilityUseCase;
	// 판매글 거래 상태 변경 UseCase
	private final ChangeProductPostTradeStatusUseCase changeProductPostTradeStatusUseCase;
	// 판매글 끌올 UseCase
	private final BumpProductPostUseCase bumpProductPostUseCase;
	// 판매글 상세 조회 UseCase
	private final GetProductPostUseCase getProductPostUseCase;
	// 판매글 목록 조회 UseCase
	private final ListProductPostsUseCase listProductPostsUseCase;

	// 판매글 목록 조회 (FO 홈·헤더, Auth 불필요)
	@GetMapping
	public ProductPostCardPageResponseVo list(
			@RequestParam(required = false) List<String> categoryUuids,
			@RequestParam(required = false) String memberUuid,
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Long minPrice,
			@RequestParam(required = false) Long maxPrice,
			@RequestParam(required = false) List<String> conditionGrades,
			@RequestParam(required = false) List<String> documentTypes,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "20") int size
	) {
		ProductPostCardPageDto result = listProductPostsUseCase.list(
				ProductPostWebMapper.toListQuery(
						categoryUuids,
						memberUuid,
						keyword,
						minPrice,
						maxPrice,
						conditionGrades,
						documentTypes,
						page,
						size
				)
		);
		return ProductPostWebMapper.toCardPageResponse(result);
	}

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

	// 판매글 수정 (FO, 판매자 본인·SELLING만)
	@PutMapping("/{productPostUuid}")
	public ProductPostSummaryResponseVo update(
			@RequestHeader(value = InternalAuthHeaders.MEMBER_UUID, required = false) String memberUuid,
			@PathVariable String productPostUuid,
			@Valid @RequestBody UpdateProductPostRequestVo request
	) {
		ProductPostSummaryDto updated = updateProductPostUseCase.update(
				memberUuid,
				productPostUuid,
				ProductPostWebMapper.toUpdateCommand(request)
		);
		return ProductPostWebMapper.toSummaryResponse(updated);
	}

	// 판매글 Soft Delete (FO, 판매자 본인)
	@DeleteMapping("/{productPostUuid}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(
			@RequestHeader(value = InternalAuthHeaders.MEMBER_UUID, required = false) String memberUuid,
			@PathVariable String productPostUuid
	) {
		deleteProductPostUseCase.delete(memberUuid, productPostUuid);
	}

	// 판매글 노출 상태 변경 (FO, 숨김·재공개)
	@PatchMapping("/{productPostUuid}/visibility")
	public ProductPostSummaryResponseVo changeVisibility(
			@RequestHeader(value = InternalAuthHeaders.MEMBER_UUID, required = false) String memberUuid,
			@PathVariable String productPostUuid,
			@Valid @RequestBody ChangeProductPostVisibilityRequestVo request
	) {
		ProductPostSummaryDto updated = changeProductPostVisibilityUseCase.changeVisibility(
				memberUuid,
				productPostUuid,
				ProductPostWebMapper.toChangeVisibilityCommand(request)
		);
		return ProductPostWebMapper.toSummaryResponse(updated);
	}

	// 판매글 거래 상태 변경 (FO, 판매자 본인·판매관리)
	@PatchMapping("/{productPostUuid}/trade-status")
	public ProductPostSummaryResponseVo changeTradeStatus(
			@RequestHeader(value = InternalAuthHeaders.MEMBER_UUID, required = false) String memberUuid,
			@PathVariable String productPostUuid,
			@Valid @RequestBody ChangeProductPostTradeStatusRequestVo request
	) {
		ProductPostSummaryDto updated = changeProductPostTradeStatusUseCase.changeTradeStatus(
				memberUuid,
				productPostUuid,
				ProductPostWebMapper.toChangeTradeStatusCommand(request)
		);
		return ProductPostWebMapper.toSummaryResponse(updated);
	}

	// 판매글 끌올 (FO, 판매자 본인·SELLING·PUBLIC)
	@PostMapping("/{productPostUuid}/bump")
	public ProductPostSummaryResponseVo bump(
			@RequestHeader(value = InternalAuthHeaders.MEMBER_UUID, required = false) String memberUuid,
			@PathVariable String productPostUuid
	) {
		ProductPostSummaryDto bumped = bumpProductPostUseCase.bump(memberUuid, productPostUuid);
		return ProductPostWebMapper.toSummaryResponse(bumped);
	}

	// 판매글 상세 조회 (FO, 공개글만)
	@GetMapping("/{productPostUuid}")
	public ProductPostSummaryResponseVo getDetail(@PathVariable String productPostUuid) {
		ProductPostSummaryDto detail = getProductPostUseCase.getByUuid(productPostUuid);
		return ProductPostWebMapper.toSummaryResponse(detail);
	}
}
