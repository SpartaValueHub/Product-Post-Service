package com.sparta.listing_service.application.port.out;

import java.util.Optional;

import com.sparta.listing_service.domain.model.Listing;

// 판매글 조회 Output Port
public interface ListingLoadPort {

	// 판매글 UUID로 단건 조회 (이미지·서류 포함)
	Optional<Listing> findByUuid(String listingUuid);

	// 판매글 ID로 단건 조회 (이미지·서류 포함)
	Optional<Listing> findById(Long listingId);
}
