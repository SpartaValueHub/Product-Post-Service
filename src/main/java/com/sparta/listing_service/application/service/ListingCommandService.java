package com.sparta.listing_service.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.listing_service.application.exception.UnauthorizedException;
import com.sparta.listing_service.application.port.in.CreateListingUseCase;
import com.sparta.listing_service.application.port.in.dto.CreateListingCommand;
import com.sparta.listing_service.application.port.in.dto.CreateListingDocumentCommand;
import com.sparta.listing_service.application.port.in.dto.CreateListingImageCommand;
import com.sparta.listing_service.application.port.in.dto.ListingDocumentSummaryDto;
import com.sparta.listing_service.application.port.in.dto.ListingImageSummaryDto;
import com.sparta.listing_service.application.port.in.dto.ListingSummaryDto;
import com.sparta.listing_service.application.port.out.ListingSavePort;
import com.sparta.listing_service.config.ListingPolicyProperties;
import com.sparta.listing_service.domain.model.Listing;
import com.sparta.listing_service.domain.model.ListingDocument;
import com.sparta.listing_service.domain.model.ListingImage;

import lombok.RequiredArgsConstructor;

// 판매글 쓰기 Application Service
@Service
@RequiredArgsConstructor
public class ListingCommandService implements CreateListingUseCase {

	// 판매글 저장 Port
	private final ListingSavePort listingSavePort;
	// 생성 시각용 시계 (테스트 교체 가능)
	private final Clock clock;
	// 판매글 정책 (최소가 등)
	private final ListingPolicyProperties listingPolicyProperties;

	// 판매글 등록
	@Override
	@Transactional
	public ListingSummaryDto create(String memberUuid, CreateListingCommand command) {
		requireMemberUuid(memberUuid);

		Instant createdAt = Instant.now(clock);
		List<ListingImage> images = toImages(command.getImages(), createdAt);
		List<ListingDocument> documents = toDocuments(command.getDocuments(), createdAt);

		Listing listing = Listing.create(
				newUuid(),
				memberUuid.trim(),
				command.getCategoryUuid(),
				command.getListingName(),
				command.getConditionGrade(),
				command.getPrice(),
				command.getDescription(),
				command.getLatitude(),
				command.getLongitude(),
				command.getPlaceName(),
				images,
				documents,
				listingPolicyProperties.minPrice(),
				createdAt
		);

		Listing saved = listingSavePort.save(listing);
		return toSummary(saved);
	}

	// Gateway 헤더 누락 시 인증 실패로 처리
	private void requireMemberUuid(String memberUuid) {
		if (memberUuid == null || memberUuid.isBlank()) {
			throw new UnauthorizedException("판매자 정보가 없습니다.");
		}
	}

	// 이미지 Command → Domain
	private List<ListingImage> toImages(List<CreateListingImageCommand> images, Instant createdAt) {
		if (images == null) {
			return List.of();
		}
		return images.stream()
				.map(image -> ListingImage.create(
						newUuid(),
						image.getImageUrl(),
						image.getSortOrder(),
						createdAt
				))
				.toList();
	}

	// 서류 Command → Domain
	private List<ListingDocument> toDocuments(List<CreateListingDocumentCommand> documents, Instant createdAt) {
		if (documents == null || documents.isEmpty()) {
			return List.of();
		}
		return documents.stream()
				.map(document -> ListingDocument.create(
						newUuid(),
						document.getDocumentType(),
						document.getImageUrl(),
						createdAt
				))
				.toList();
	}

	// Domain → 요약 DTO
	private ListingSummaryDto toSummary(Listing listing) {
		List<ListingImageSummaryDto> images = listing.getImages().stream()
				.map(image -> ListingImageSummaryDto.builder()
						.listingImageUuid(image.getListingImageUuid())
						.imageUrl(image.getImageUrl())
						.sortOrder(image.getSortOrder())
						.build())
				.toList();
		List<ListingDocumentSummaryDto> documents = listing.getDocuments().stream()
				.map(document -> ListingDocumentSummaryDto.builder()
						.listingDocumentUuid(document.getListingDocumentUuid())
						.documentType(document.getDocumentType())
						.imageUrl(document.getImageUrl())
						.build())
				.toList();

		return ListingSummaryDto.builder()
				.listingUuid(listing.getListingUuid())
				.memberUuid(listing.getMemberUuid())
				.categoryUuid(listing.getCategoryUuid())
				.listingName(listing.getListingName())
				.conditionGrade(listing.getConditionGrade())
				.price(listing.getPrice())
				.description(listing.getDescription())
				.tradeStatus(listing.getTradeStatus())
				.listingStatus(listing.getListingStatus())
				.latitude(listing.getLatitude())
				.longitude(listing.getLongitude())
				.placeName(listing.getPlaceName())
				.createdAt(listing.getCreatedAt())
				.images(images)
				.documents(documents)
				.build();
	}

	// 업무 UUID 생성 (Application 책임)
	private String newUuid() {
		return UUID.randomUUID().toString();
	}
}
