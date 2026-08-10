package com.sparta.listing_service.adaptor.out.mysql;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.sparta.listing_service.adaptor.out.mysql.entity.ListingDocumentEntity;
import com.sparta.listing_service.adaptor.out.mysql.entity.ListingEntity;
import com.sparta.listing_service.adaptor.out.mysql.entity.ListingImageEntity;
import com.sparta.listing_service.adaptor.out.mysql.mapper.ListingEntityMapper;
import com.sparta.listing_service.adaptor.out.mysql.repository.ListingDocumentJpaRepository;
import com.sparta.listing_service.adaptor.out.mysql.repository.ListingImageJpaRepository;
import com.sparta.listing_service.adaptor.out.mysql.repository.ListingJpaRepository;
import com.sparta.listing_service.application.port.out.ListingLoadPort;
import com.sparta.listing_service.domain.model.Listing;

import lombok.RequiredArgsConstructor;

// 판매글 조회 Adapter
@Component
@RequiredArgsConstructor
public class ListingLoadAdapter implements ListingLoadPort {

	// 판매글 JPA Repository
	private final ListingJpaRepository listingJpaRepository;
	// 이미지 JPA Repository
	private final ListingImageJpaRepository listingImageJpaRepository;
	// 서류 JPA Repository
	private final ListingDocumentJpaRepository listingDocumentJpaRepository;

	// 판매글 UUID로 단건 조회
	@Override
	public Optional<Listing> findByUuid(String listingUuid) {
		return listingJpaRepository.findByListingUuid(listingUuid)
				.map(this::toDomainWithChildren);
	}

	// 판매글 ID로 단건 조회
	@Override
	public Optional<Listing> findById(Long listingId) {
		return listingJpaRepository.findById(listingId)
				.map(this::toDomainWithChildren);
	}

	// 부모 + 자식 Entity를 Domain으로 조립
	private Listing toDomainWithChildren(ListingEntity entity) {
		List<ListingImageEntity> images = listingImageJpaRepository
				.findByListingIdOrderBySortOrderAscListingImageIdAsc(entity.getListingId());
		List<ListingDocumentEntity> documents = listingDocumentJpaRepository
				.findByListingIdOrderByListingDocumentIdAsc(entity.getListingId());
		return ListingEntityMapper.toDomain(entity, images, documents);
	}
}
