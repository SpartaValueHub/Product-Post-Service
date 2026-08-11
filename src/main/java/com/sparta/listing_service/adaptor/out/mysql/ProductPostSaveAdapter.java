package com.sparta.listing_service.adaptor.out.mysql;

import java.util.List;

import org.springframework.stereotype.Component;

import com.sparta.listing_service.adaptor.out.mysql.entity.ProductPostDocumentEntity;
import com.sparta.listing_service.adaptor.out.mysql.entity.ProductPostEntity;
import com.sparta.listing_service.adaptor.out.mysql.entity.ProductPostImageEntity;
import com.sparta.listing_service.adaptor.out.mysql.mapper.ProductPostEntityMapper;
import com.sparta.listing_service.adaptor.out.mysql.repository.ProductPostDocumentJpaRepository;
import com.sparta.listing_service.adaptor.out.mysql.repository.ProductPostImageJpaRepository;
import com.sparta.listing_service.adaptor.out.mysql.repository.ProductPostJpaRepository;
import com.sparta.listing_service.application.port.out.ProductPostSavePort;
import com.sparta.listing_service.domain.model.ProductPost;
import com.sparta.listing_service.domain.model.ProductPostDocument;
import com.sparta.listing_service.domain.model.ProductPostImage;

import lombok.RequiredArgsConstructor;

// 판매글 저장 Adapter (부모 저장 후 product_post_id로 자식 저장)
@Component
@RequiredArgsConstructor
public class ProductPostSaveAdapter implements ProductPostSavePort {

	// 판매글 JPA Repository
	private final ProductPostJpaRepository productPostJpaRepository;
	// 이미지 JPA Repository
	private final ProductPostImageJpaRepository productPostImageJpaRepository;
	// 서류 JPA Repository
	private final ProductPostDocumentJpaRepository productPostDocumentJpaRepository;

	// 판매글 신규 저장
	@Override
	public ProductPost save(ProductPost listing) {
		ProductPostEntity saved = productPostJpaRepository.save(ProductPostEntity.create(
				listing.getProductPostUuid(),
				listing.getMemberUuid(),
				listing.getCategoryUuid(),
				listing.getProductPostName(),
				listing.getConditionGrade(),
				listing.getPrice(),
				listing.getDescription(),
				listing.getTradeStatus(),
				listing.getProductPostStatus(),
				listing.getLatitude(),
				listing.getLongitude(),
				listing.getPlaceName(),
				listing.getCreatedAt()
		));

		Long productPostId = saved.getProductPostId();
		List<ProductPostImageEntity> imageEntities = listing.getImages().stream()
				.map(image -> productPostImageJpaRepository.save(toNewImageEntity(productPostId, image)))
				.toList();
		List<ProductPostDocumentEntity> documentEntities = listing.getDocuments().stream()
				.map(document -> productPostDocumentJpaRepository.save(toNewDocumentEntity(productPostId, document)))
				.toList();

		return ProductPostEntityMapper.toDomain(saved, imageEntities, documentEntities);
	}

	// 기존 판매글 변경 저장
	@Override
	public ProductPost update(ProductPost listing) {
		ProductPostEntity entity = productPostJpaRepository.findById(listing.getProductPostId())
				.orElseThrow(() -> new IllegalArgumentException("수정할 판매글을 찾을 수 없습니다."));

		entity.update(
				listing.getCategoryUuid(),
				listing.getProductPostName(),
				listing.getConditionGrade(),
				listing.getPrice(),
				listing.getDescription(),
				listing.getTradeStatus(),
				listing.getProductPostStatus(),
				listing.getLatitude(),
				listing.getLongitude(),
				listing.getPlaceName(),
				listing.getBumpedAt(),
				listing.getUpdatedAt(),
				listing.getDeletedAt()
		);

		Long productPostId = entity.getProductPostId();
		List<ProductPostImageEntity> imageEntities = listing.getImages().stream()
				.map(image -> saveOrUpdateImage(productPostId, image))
				.toList();
		List<ProductPostDocumentEntity> documentEntities = listing.getDocuments().stream()
				.map(document -> saveOrUpdateDocument(productPostId, document))
				.toList();

		return ProductPostEntityMapper.toDomain(entity, imageEntities, documentEntities);
	}

	// 신규 이미지 Entity 생성
	private ProductPostImageEntity toNewImageEntity(Long productPostId, ProductPostImage image) {
		return ProductPostImageEntity.create(
				productPostId,
				image.getProductPostImageUuid(),
				image.getImageUrl(),
				image.getSortOrder(),
				image.getCreatedAt()
		);
	}

	// 신규 서류 Entity 생성
	private ProductPostDocumentEntity toNewDocumentEntity(Long productPostId, ProductPostDocument document) {
		return ProductPostDocumentEntity.create(
				productPostId,
				document.getProductPostDocumentUuid(),
				document.getDocumentType(),
				document.getImageUrl(),
				document.getCreatedAt()
		);
	}

	// 이미지 PK 있으면 수정, 없으면 신규
	private ProductPostImageEntity saveOrUpdateImage(Long productPostId, ProductPostImage image) {
		if (image.getProductPostImageId() == null) {
			return productPostImageJpaRepository.save(toNewImageEntity(productPostId, image));
		}

		ProductPostImageEntity entity = productPostImageJpaRepository.findById(image.getProductPostImageId())
				.orElseThrow(() -> new IllegalArgumentException("수정할 이미지를 찾을 수 없습니다."));
		entity.update(image.getImageUrl(), image.getSortOrder(), image.getUpdatedAt(), image.getDeletedAt());
		return entity;
	}

	// 서류 PK 있으면 수정, 없으면 신규
	private ProductPostDocumentEntity saveOrUpdateDocument(Long productPostId, ProductPostDocument document) {
		if (document.getProductPostDocumentId() == null) {
			return productPostDocumentJpaRepository.save(toNewDocumentEntity(productPostId, document));
		}

		ProductPostDocumentEntity entity = productPostDocumentJpaRepository.findById(document.getProductPostDocumentId())
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
