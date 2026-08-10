package com.sparta.listing_service.adaptor.out.mysql.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.listing_service.adaptor.out.mysql.entity.ListingImageEntity;

// 판매글 이미지 JPA Repository
public interface ListingImageJpaRepository extends JpaRepository<ListingImageEntity, Long> {

	// 판매글 PK로 이미지 목록 조회 (노출 순서 오름차순)
	List<ListingImageEntity> findByListingIdOrderBySortOrderAscListingImageIdAsc(Long listingId);

	// 이미지 UUID로 조회
	Optional<ListingImageEntity> findByListingImageUuid(String listingImageUuid);
}
