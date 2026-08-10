package com.sparta.listing_service.adaptor.in.web.mapper;

import java.util.List;

import com.sparta.listing_service.adaptor.in.web.vo.CreateListingDocumentRequestVo;
import com.sparta.listing_service.adaptor.in.web.vo.CreateListingImageRequestVo;
import com.sparta.listing_service.adaptor.in.web.vo.CreateListingRequestVo;
import com.sparta.listing_service.adaptor.in.web.vo.ListingDocumentResponseVo;
import com.sparta.listing_service.adaptor.in.web.vo.ListingImageResponseVo;
import com.sparta.listing_service.adaptor.in.web.vo.ListingSummaryResponseVo;
import com.sparta.listing_service.application.port.in.dto.CreateListingCommand;
import com.sparta.listing_service.application.port.in.dto.CreateListingDocumentCommand;
import com.sparta.listing_service.application.port.in.dto.CreateListingImageCommand;
import com.sparta.listing_service.application.port.in.dto.ListingDocumentSummaryDto;
import com.sparta.listing_service.application.port.in.dto.ListingImageSummaryDto;
import com.sparta.listing_service.application.port.in.dto.ListingSummaryDto;

// Listing Web VO ↔ Application DTO 변환 (UUID·시간 생성 금지)
public final class ListingWebMapper {

	private ListingWebMapper() {
	}

	// 등록 요청 VO → Command
	public static CreateListingCommand toCreateCommand(CreateListingRequestVo request) {
		List<CreateListingImageCommand> images = request.getImages().stream()
				.map(ListingWebMapper::toImageCommand)
				.toList();
		List<CreateListingDocumentCommand> documents = request.getDocuments() == null
				? List.of()
				: request.getDocuments().stream()
						.map(ListingWebMapper::toDocumentCommand)
						.toList();

		return CreateListingCommand.builder()
				.categoryUuid(request.getCategoryUuid())
				.listingName(request.getListingName())
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
	public static ListingSummaryResponseVo toSummaryResponse(ListingSummaryDto dto) {
		List<ListingImageResponseVo> images = dto.getImages().stream()
				.map(ListingWebMapper::toImageResponse)
				.toList();
		List<ListingDocumentResponseVo> documents = dto.getDocuments().stream()
				.map(ListingWebMapper::toDocumentResponse)
				.toList();

		return ListingSummaryResponseVo.builder()
				.listingUuid(dto.getListingUuid())
				.memberUuid(dto.getMemberUuid())
				.categoryUuid(dto.getCategoryUuid())
				.listingName(dto.getListingName())
				.conditionGrade(dto.getConditionGrade())
				.price(dto.getPrice())
				.description(dto.getDescription())
				.tradeStatus(dto.getTradeStatus())
				.listingStatus(dto.getListingStatus())
				.latitude(dto.getLatitude())
				.longitude(dto.getLongitude())
				.placeName(dto.getPlaceName())
				.createdAt(dto.getCreatedAt())
				.images(images)
				.documents(documents)
				.build();
	}

	private static CreateListingImageCommand toImageCommand(CreateListingImageRequestVo request) {
		return CreateListingImageCommand.builder()
				.imageUrl(request.getImageUrl())
				.sortOrder(request.getSortOrder())
				.build();
	}

	private static CreateListingDocumentCommand toDocumentCommand(CreateListingDocumentRequestVo request) {
		return CreateListingDocumentCommand.builder()
				.documentType(request.getDocumentType())
				.imageUrl(request.getImageUrl())
				.build();
	}

	private static ListingImageResponseVo toImageResponse(ListingImageSummaryDto dto) {
		return ListingImageResponseVo.builder()
				.listingImageUuid(dto.getListingImageUuid())
				.imageUrl(dto.getImageUrl())
				.sortOrder(dto.getSortOrder())
				.build();
	}

	private static ListingDocumentResponseVo toDocumentResponse(ListingDocumentSummaryDto dto) {
		return ListingDocumentResponseVo.builder()
				.listingDocumentUuid(dto.getListingDocumentUuid())
				.documentType(dto.getDocumentType())
				.imageUrl(dto.getImageUrl())
				.build();
	}
}
