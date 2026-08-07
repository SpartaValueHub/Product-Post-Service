package com.sparta.listing_service.adaptor.out.mysql.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.listing_service.adaptor.out.mysql.entity.ListingEntity;

// 판매글 JPA Repository (Application에서는 Adapter를 통해서만 사용)
public interface ListingJpaRepository extends JpaRepository<ListingEntity, Long> {

	// 판매글 UUID로 조회
	Optional<ListingEntity> findByListingUuid(String listingUuid);
}
