package com.sparta.listing_service.adaptor.in.web.mapper;

import java.util.List;

import com.sparta.listing_service.adaptor.in.web.vo.CreateProductPostDocumentRequestVo;
import com.sparta.listing_service.adaptor.in.web.vo.CreateProductPostImageRequestVo;
import com.sparta.listing_service.adaptor.in.web.vo.CreateProductPostRequestVo;
import com.sparta.listing_service.adaptor.in.web.vo.ProductPostDocumentResponseVo;
import com.sparta.listing_service.adaptor.in.web.vo.ProductPostImageResponseVo;
import com.sparta.listing_service.adaptor.in.web.vo.ProductPostSummaryResponseVo;
import com.sparta.listing_service.application.port.in.dto.CreateProductPostCommand;
import com.sparta.listing_service.application.port.in.dto.CreateProductPostDocumentCommand;
import com.sparta.listing_service.application.port.in.dto.CreateProductPostImageCommand;
import com.sparta.listing_service.application.port.in.dto.ProductPostDocumentSummaryDto;
import com.sparta.listing_service.application.port.in.dto.ProductPostImageSummaryDto;
import com.sparta.listing_service.application.port.in.dto.ProductPostSummaryDto;

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
				.sortOrder(request.getSortOrder())
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
