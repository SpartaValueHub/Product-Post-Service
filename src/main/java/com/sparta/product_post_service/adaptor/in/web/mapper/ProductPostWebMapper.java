package com.sparta.product_post_service.adaptor.in.web.mapper;

import java.util.List;

import com.sparta.product_post_service.adaptor.in.web.vo.ChangeProductPostTradeStatusRequestVo;
import com.sparta.product_post_service.adaptor.in.web.vo.ChangeProductPostVisibilityRequestVo;
import com.sparta.product_post_service.adaptor.in.web.vo.CreateProductPostDocumentRequestVo;
import com.sparta.product_post_service.adaptor.in.web.vo.CreateProductPostImageRequestVo;
import com.sparta.product_post_service.adaptor.in.web.vo.CreateProductPostRequestVo;
import com.sparta.product_post_service.adaptor.in.web.vo.ProductPostCardPageResponseVo;
import com.sparta.product_post_service.adaptor.in.web.vo.ProductPostCardResponseVo;
import com.sparta.product_post_service.adaptor.in.web.vo.ProductPostDocumentResponseVo;
import com.sparta.product_post_service.adaptor.in.web.vo.ProductPostImageResponseVo;
import com.sparta.product_post_service.adaptor.in.web.vo.ProductPostSummaryResponseVo;
import com.sparta.product_post_service.adaptor.in.web.vo.UpdateProductPostRequestVo;
import com.sparta.product_post_service.application.port.in.dto.ChangeProductPostTradeStatusCommand;
import com.sparta.product_post_service.application.port.in.dto.ChangeProductPostVisibilityCommand;
import com.sparta.product_post_service.application.port.in.dto.CreateProductPostCommand;
import com.sparta.product_post_service.application.port.in.dto.CreateProductPostDocumentCommand;
import com.sparta.product_post_service.application.port.in.dto.CreateProductPostImageCommand;
import com.sparta.product_post_service.application.port.in.dto.ListProductPostsQuery;
import com.sparta.product_post_service.application.port.in.dto.ProductPostCardDto;
import com.sparta.product_post_service.application.port.in.dto.ProductPostCardPageDto;
import com.sparta.product_post_service.application.port.in.dto.ProductPostDocumentSummaryDto;
import com.sparta.product_post_service.application.port.in.dto.ProductPostImageSummaryDto;
import com.sparta.product_post_service.application.port.in.dto.ProductPostSummaryDto;
import com.sparta.product_post_service.application.port.in.dto.UpdateProductPostCommand;

// ProductPost Web VO ↔ Application DTO 변환 (UUID·시간 생성 금지)
public final class ProductPostWebMapper {

	private ProductPostWebMapper() {
	}

	// 등록 요청 VO → Command
	public static CreateProductPostCommand toCreateCommand(CreateProductPostRequestVo request) {
		List<CreateProductPostImageCommand> images = request.getImages().stream()
				.map(ProductPostWebMapper::toImageCommand)
				.toList();
		List<CreateProductPostDocumentCommand> documents = request.getDocuments() == null
				? List.of()
				: request.getDocuments().stream()
						.map(ProductPostWebMapper::toDocumentCommand)
						.toList();

		return CreateProductPostCommand.builder()
				.categoryUuid(request.getCategoryUuid())
				.productPostName(request.getProductPostName())
				.conditionGrade(request.getConditionGrade())
				.price(request.getPrice())
				.description(request.getDescription())
				.latitude(request.getLatitude())
				.longitude(request.getLongitude())
				.placeName(request.getPlaceName())
				.images(images)
				.documents(documents)
				.build();
	}

	// 거래 상태 변경 요청 VO → Command
	public static ChangeProductPostTradeStatusCommand toChangeTradeStatusCommand(
			ChangeProductPostTradeStatusRequestVo request
	) {
		return ChangeProductPostTradeStatusCommand.builder()
				.tradeStatus(request.getTradeStatus())
				.build();
	}

	// 노출 상태 변경 요청 VO → Command
	public static ChangeProductPostVisibilityCommand toChangeVisibilityCommand(
			ChangeProductPostVisibilityRequestVo request
	) {
		return ChangeProductPostVisibilityCommand.builder()
				.productPostStatus(request.getProductPostStatus())
				.build();
	}

	// 수정 요청 VO → Command
	public static UpdateProductPostCommand toUpdateCommand(UpdateProductPostRequestVo request) {
		List<CreateProductPostImageCommand> images = request.getImages().stream()
				.map(ProductPostWebMapper::toImageCommand)
				.toList();
		List<CreateProductPostDocumentCommand> documents = request.getDocuments() == null
				? List.of()
				: request.getDocuments().stream()
						.map(ProductPostWebMapper::toDocumentCommand)
						.toList();

		return UpdateProductPostCommand.builder()
				.categoryUuid(request.getCategoryUuid())
				.productPostName(request.getProductPostName())
				.conditionGrade(request.getConditionGrade())
				.price(request.getPrice())
				.description(request.getDescription())
				.latitude(request.getLatitude())
				.longitude(request.getLongitude())
				.placeName(request.getPlaceName())
				.images(images)
				.documents(documents)
				.build();
	}

	// 목록 쿼리 파라미터 → Application Query
	public static ListProductPostsQuery toListQuery(
			List<String> categoryUuids,
			String keyword,
			Long minPrice,
			Long maxPrice,
			List<String> conditionGrades,
			List<String> documentTypes,
			int page,
			int size
	) {
		return ListProductPostsQuery.builder()
				.categoryUuids(categoryUuids)
				.keyword(keyword)
				.minPrice(minPrice)
				.maxPrice(maxPrice)
				.conditionGrades(conditionGrades)
				.documentTypes(documentTypes)
				.page(page)
				.size(size)
				.build();
	}

	// 목록 페이지 DTO → 응답 VO
	public static ProductPostCardPageResponseVo toCardPageResponse(ProductPostCardPageDto dto) {
		List<ProductPostCardResponseVo> content = dto.getContent().stream()
				.map(ProductPostWebMapper::toCardResponse)
				.toList();
		return ProductPostCardPageResponseVo.builder()
				.content(content)
				.page(dto.getPage())
				.size(dto.getSize())
				.totalElements(dto.getTotalElements())
				.totalPages(dto.getTotalPages())
				.build();
	}

	// 목록 카드 DTO → 응답 VO
	public static ProductPostCardResponseVo toCardResponse(ProductPostCardDto dto) {
		return ProductPostCardResponseVo.builder()
				.productPostUuid(dto.getProductPostUuid())
				.productPostName(dto.getProductPostName())
				.price(dto.getPrice())
				.tradeStatus(dto.getTradeStatus())
				.listedAt(dto.getListedAt())
				.thumbnailUrl(dto.getThumbnailUrl())
				.build();
	}

	// 요약 DTO → 응답 VO
	public static ProductPostSummaryResponseVo toSummaryResponse(ProductPostSummaryDto dto) {
		List<ProductPostImageResponseVo> images = dto.getImages().stream()
				.map(ProductPostWebMapper::toImageResponse)
				.toList();
		List<ProductPostDocumentResponseVo> documents = dto.getDocuments().stream()
				.map(ProductPostWebMapper::toDocumentResponse)
				.toList();

		return ProductPostSummaryResponseVo.builder()
				.productPostUuid(dto.getProductPostUuid())
				.memberUuid(dto.getMemberUuid())
				.categoryUuid(dto.getCategoryUuid())
				.productPostName(dto.getProductPostName())
				.conditionGrade(dto.getConditionGrade())
				.price(dto.getPrice())
				.description(dto.getDescription())
				.tradeStatus(dto.getTradeStatus())
				.productPostStatus(dto.getProductPostStatus())
				.latitude(dto.getLatitude())
				.longitude(dto.getLongitude())
				.placeName(dto.getPlaceName())
				.createdAt(dto.getCreatedAt())
				.images(images)
				.documents(documents)
				.build();
	}

	private static CreateProductPostImageCommand toImageCommand(CreateProductPostImageRequestVo request) {
		return CreateProductPostImageCommand.builder()
				.imageUrl(request.getImageUrl())
				.build();
	}

	private static CreateProductPostDocumentCommand toDocumentCommand(CreateProductPostDocumentRequestVo request) {
		return CreateProductPostDocumentCommand.builder()
				.documentType(request.getDocumentType())
				.imageUrl(request.getImageUrl())
				.build();
	}

	private static ProductPostImageResponseVo toImageResponse(ProductPostImageSummaryDto dto) {
		return ProductPostImageResponseVo.builder()
				.productPostImageUuid(dto.getProductPostImageUuid())
				.imageUrl(dto.getImageUrl())
				.sortOrder(dto.getSortOrder())
				.build();
	}

	private static ProductPostDocumentResponseVo toDocumentResponse(ProductPostDocumentSummaryDto dto) {
		return ProductPostDocumentResponseVo.builder()
				.productPostDocumentUuid(dto.getProductPostDocumentUuid())
				.documentType(dto.getDocumentType())
				.imageUrl(dto.getImageUrl())
				.build();
	}
}
