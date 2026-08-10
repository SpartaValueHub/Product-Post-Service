package com.sparta.listing_service.adaptor.out.mysql.mapper;

import com.sparta.listing_service.adaptor.out.mysql.entity.ListingImageEntity;
import com.sparta.listing_service.domain.model.ListingImage;

// ListingImage Entity <-> Domain 변환
public final class ListingImageEntityMapper {

	private ListingImageEntityMapper() {
	}

	// Entity를 Domain으로 복원
	public static ListingImage toDomain(ListingImageEntity entity) {
		return ListingImage.restore(
				entity.getListingImageId(),
				entity.getListingImageUuid(),
				entity.getListingId(),
				entity.getImageUrl(),
				entity.getSortOrder(),
				entity.getCreatedAt(),
				entity.getUpdatedAt(),
				entity.getDeletedAt()
		);
	}
}
