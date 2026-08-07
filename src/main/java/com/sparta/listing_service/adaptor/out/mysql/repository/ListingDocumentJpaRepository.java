package com.sparta.listing_service.adaptor.out.mysql.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.listing_service.adaptor.out.mysql.entity.ListingDocumentEntity;

// 판매글 서류 JPA Repository
public interface ListingDocumentJpaRepository extends JpaRepository<ListingDocumentEntity, Long> {

	// 판매글 PK로 서류 목록 조회
	List<ListingDocumentEntity> findByListingIdOrderByListingDocumentIdAsc(Long listingId);

	// 서류 UUID로 조회
	Optional<ListingDocumentEntity> findByListingDocumentUuid(String listingDocumentUuid);
}
