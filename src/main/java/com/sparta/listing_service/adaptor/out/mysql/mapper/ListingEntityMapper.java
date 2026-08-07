package com.sparta.listing_service.adaptor.out.mysql.mapper;

import java.util.List;

import com.sparta.listing_service.adaptor.out.mysql.entity.ListingDocumentEntity;
import com.sparta.listing_service.adaptor.out.mysql.entity.ListingEntity;
import com.sparta.listing_service.adaptor.out.mysql.entity.ListingImageEntity;
import com.sparta.listing_service.domain.model.Listing;
import com.sparta.listing_service.domain.model.ListingDocument;
import com.sparta.listing_service.domain.model.ListingImage;

// Listing Entity <-> Domain 변환
public final class ListingEntityMapper {

	private ListingEntityMapper() {
	}

	// Entity + 자식 Entity를 Domain으로 복원
	public static Listing toDomain(
			ListingEntity entity,
			List<ListingImageEntity> imageEntities,
			List<ListingDocumentEntity> documentEntities
	) {
		List<ListingImage> images = imageEntities.stream()
				.map(ListingImageEntityMapper::toDomain)
				.toList();
		List<ListingDocument> documents = documentEntities.stream()
				.map(ListingDocumentEntityMapper::toDomain)
				.toList();

		return Listing.restore(
				entity.getListingId(),
				entity.getListingUuid(),
				entity.getMemberUuid(),
				entity.getCategoryUuid(),
				entity.getListingName(),
				entity.getConditionGrade(),
				entity.getPrice(),
				entity.getDescription(),
				entity.getTradeStatus(),
				entity.getListingStatus(),
				entity.isNegotiable(),
				entity.getLatitude(),
				entity.getLongitude(),
				entity.getPlaceName(),
				entity.getBumpedAt(),
				entity.getCreatedAt(),
				entity.getUpdatedAt(),
				entity.getDeletedAt(),
				images,
				documents
		);
	}
}
