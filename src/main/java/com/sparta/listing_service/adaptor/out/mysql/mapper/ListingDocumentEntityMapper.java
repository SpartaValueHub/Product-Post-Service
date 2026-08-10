package com.sparta.listing_service.adaptor.out.mysql.mapper;

import com.sparta.listing_service.adaptor.out.mysql.entity.ListingDocumentEntity;
import com.sparta.listing_service.domain.model.ListingDocument;

// ListingDocument Entity <-> Domain 변환
public final class ListingDocumentEntityMapper {

	private ListingDocumentEntityMapper() {
	}

	// Entity를 Domain으로 복원
	public static ListingDocument toDomain(ListingDocumentEntity entity) {
		return ListingDocument.restore(
				entity.getListingDocumentId(),
				entity.getListingDocumentUuid(),
				entity.getListingId(),
				entity.getDocumentType(),
				entity.getImageUrl(),
				entity.getCreatedAt(),
				entity.getUpdatedAt(),
				entity.getDeletedAt()
		);
	}
}
