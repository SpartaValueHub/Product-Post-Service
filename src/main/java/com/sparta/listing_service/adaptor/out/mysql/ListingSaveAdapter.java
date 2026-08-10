package com.sparta.listing_service.adaptor.out.mysql;

import java.util.List;

import org.springframework.stereotype.Component;

import com.sparta.listing_service.adaptor.out.mysql.entity.ListingDocumentEntity;
import com.sparta.listing_service.adaptor.out.mysql.entity.ListingEntity;
import com.sparta.listing_service.adaptor.out.mysql.entity.ListingImageEntity;
import com.sparta.listing_service.adaptor.out.mysql.mapper.ListingEntityMapper;
import com.sparta.listing_service.adaptor.out.mysql.repository.ListingDocumentJpaRepository;
import com.sparta.listing_service.adaptor.out.mysql.repository.ListingImageJpaRepository;
import com.sparta.listing_service.adaptor.out.mysql.repository.ListingJpaRepository;
import com.sparta.listing_service.application.port.out.ListingSavePort;
import com.sparta.listing_service.domain.model.Listing;
import com.sparta.listing_service.domain.model.ListingDocument;
import com.sparta.listing_service.domain.model.ListingImage;

import lombok.RequiredArgsConstructor;

// 판매글 저장 Adapter (부모 저장 후 listing_id로 자식 저장)
@Component
@RequiredArgsConstructor
public class ListingSaveAdapter implements ListingSavePort {

	// 판매글 JPA Repository
	private final ListingJpaRepository listingJpaRepository;
	// 이미지 JPA Repository
	private final ListingImageJpaRepository listingImageJpaRepository;
	// 서류 JPA Repository
	private final ListingDocumentJpaRepository listingDocumentJpaRepository;

	// 판매글 신규 저장
	@Override
	public Listing save(Listing listing) {
		ListingEntity saved = listingJpaRepository.save(ListingEntity.create(
				listing.getListingUuid(),
				listing.getMemberUuid(),
				listing.getCategoryUuid(),
				listing.getListingName(),
				listing.getConditionGrade(),
				listing.getPrice(),
				listing.getDescription(),
				listing.getTradeStatus(),
				listing.getListingStatus(),
				listing.getLatitude(),
				listing.getLongitude(),
				listing.getPlaceName(),
				listing.getCreatedAt()
		));

		Long listingId = saved.getListingId();
		List<ListingImageEntity> imageEntities = listing.getImages().stream()
				.map(image -> listingImageJpaRepository.save(toNewImageEntity(listingId, image)))
				.toList();
		List<ListingDocumentEntity> documentEntities = listing.getDocuments().stream()
				.map(document -> listingDocumentJpaRepository.save(toNewDocumentEntity(listingId, document)))
				.toList();

		return ListingEntityMapper.toDomain(saved, imageEntities, documentEntities);
	}

	// 기존 판매글 변경 저장
	@Override
	public Listing update(Listing listing) {
		ListingEntity entity = listingJpaRepository.findById(listing.getListingId())
				.orElseThrow(() -> new IllegalArgumentException("수정할 판매글을 찾을 수 없습니다."));

		entity.update(
				listing.getCategoryUuid(),
				listing.getListingName(),
				listing.getConditionGrade(),
				listing.getPrice(),
				listing.getDescription(),
				listing.getTradeStatus(),
				listing.getListingStatus(),
				listing.getLatitude(),
				listing.getLongitude(),
				listing.getPlaceName(),
				listing.getBumpedAt(),
				listing.getUpdatedAt(),
				listing.getDeletedAt()
		);

		Long listingId = entity.getListingId();
		List<ListingImageEntity> imageEntities = listing.getImages().stream()
				.map(image -> saveOrUpdateImage(listingId, image))
				.toList();
		List<ListingDocumentEntity> documentEntities = listing.getDocuments().stream()
				.map(document -> saveOrUpdateDocument(listingId, document))
				.toList();

		return ListingEntityMapper.toDomain(entity, imageEntities, documentEntities);
	}

	// 신규 이미지 Entity 생성
	private ListingImageEntity toNewImageEntity(Long listingId, ListingImage image) {
		return ListingImageEntity.create(
				listingId,
				image.getListingImageUuid(),
				image.getImageUrl(),
				image.getSortOrder(),
				image.getCreatedAt()
		);
	}

	// 신규 서류 Entity 생성
	private ListingDocumentEntity toNewDocumentEntity(Long listingId, ListingDocument document) {
		return ListingDocumentEntity.create(
				listingId,
				document.getListingDocumentUuid(),
				document.getDocumentType(),
				document.getImageUrl(),
				document.getCreatedAt()
		);
	}

	// 이미지 PK 있으면 수정, 없으면 신규
	private ListingImageEntity saveOrUpdateImage(Long listingId, ListingImage image) {
		if (image.getListingImageId() == null) {
			return listingImageJpaRepository.save(toNewImageEntity(listingId, image));
		}

		ListingImageEntity entity = listingImageJpaRepository.findById(image.getListingImageId())
				.orElseThrow(() -> new IllegalArgumentException("수정할 이미지를 찾을 수 없습니다."));
		entity.update(image.getImageUrl(), image.getSortOrder(), image.getUpdatedAt(), image.getDeletedAt());
		return entity;
	}

	// 서류 PK 있으면 수정, 없으면 신규
	private ListingDocumentEntity saveOrUpdateDocument(Long listingId, ListingDocument document) {
		if (document.getListingDocumentId() == null) {
			return listingDocumentJpaRepository.save(toNewDocumentEntity(listingId, document));
		}

		ListingDocumentEntity entity = listingDocumentJpaRepository.findById(document.getListingDocumentId())
				.orElseThrow(() -> new IllegalArgumentException("수정할 서류를 찾을 수 없습니다."));
		entity.update(
				document.getDocumentType(),
				document.getImageUrl(),
				document.getUpdatedAt(),
				document.getDeletedAt()
		);
		return entity;
	}
}
