package com.sparta.listing_service.adaptor.out.mysql;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.sparta.listing_service.adaptor.out.mysql.entity.ProductPostDocumentEntity;
import com.sparta.listing_service.adaptor.out.mysql.entity.ProductPostEntity;
import com.sparta.listing_service.adaptor.out.mysql.entity.ProductPostImageEntity;
import com.sparta.listing_service.adaptor.out.mysql.mapper.ProductPostEntityMapper;
import com.sparta.listing_service.adaptor.out.mysql.repository.ProductPostDocumentJpaRepository;
import com.sparta.listing_service.adaptor.out.mysql.repository.ProductPostImageJpaRepository;
import com.sparta.listing_service.adaptor.out.mysql.repository.ProductPostJpaRepository;
import com.sparta.listing_service.application.port.out.ProductPostLoadPort;
import com.sparta.listing_service.domain.model.ProductPost;

import lombok.RequiredArgsConstructor;

// 판매글 조회 Adapter
@Component
@RequiredArgsConstructor
public class ProductPostLoadAdapter implements ProductPostLoadPort {

	// 판매글 JPA Repository
	private final ProductPostJpaRepository productPostJpaRepository;
	// 이미지 JPA Repository
	private final ProductPostImageJpaRepository productPostImageJpaRepository;
	// 서류 JPA Repository
	private final ProductPostDocumentJpaRepository productPostDocumentJpaRepository;

	// 판매글 UUID로 단건 조회
	@Override
	public Optional<ProductPost> findByUuid(String productPostUuid) {
		return productPostJpaRepository.findByProductPostUuid(productPostUuid)
				.map(this::toDomainWithChildren);
	}

	// 판매글 ID로 단건 조회
	@Override
	public Optional<ProductPost> findById(Long productPostId) {
		return productPostJpaRepository.findById(productPostId)
				.map(this::toDomainWithChildren);
	}

	// 부모 + 자식 Entity를 Domain으로 조립
	private ProductPost toDomainWithChildren(ProductPostEntity entity) {
		List<ProductPostImageEntity> images = productPostImageJpaRepository
				.findByProductPostIdOrderBySortOrderAscProductPostImageIdAsc(entity.getProductPostId());
		List<ProductPostDocumentEntity> documents = productPostDocumentJpaRepository
				.findByProductPostIdOrderByProductPostDocumentIdAsc(entity.getProductPostId());
		return ProductPostEntityMapper.toDomain(entity, images, documents);
	}
}
