package com.sparta.product_post_service.adaptor.out.mysql;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.sparta.product_post_service.adaptor.out.mysql.entity.ProductPostDocumentEntity;
import com.sparta.product_post_service.adaptor.out.mysql.entity.ProductPostEntity;
import com.sparta.product_post_service.adaptor.out.mysql.entity.ProductPostImageEntity;
import com.sparta.product_post_service.adaptor.out.mysql.mapper.ProductPostEntityMapper;
import com.sparta.product_post_service.adaptor.out.mysql.repository.ProductPostDocumentJpaRepository;
import com.sparta.product_post_service.adaptor.out.mysql.repository.ProductPostImageJpaRepository;
import com.sparta.product_post_service.adaptor.out.mysql.repository.ProductPostJpaRepository;
import com.sparta.product_post_service.application.port.out.ProductPostLoadPort;
import com.sparta.product_post_service.application.port.out.dto.ProductPostCardPageProjection;
import com.sparta.product_post_service.application.port.out.dto.ProductPostCardProjection;
import com.sparta.product_post_service.application.port.out.dto.ProductPostListCriteria;
import com.sparta.product_post_service.domain.model.DocumentType;
import com.sparta.product_post_service.domain.model.ProductPost;
import com.sparta.product_post_service.domain.model.ProductPostStatus;
import com.sparta.product_post_service.domain.model.TradeStatus;

import lombok.RequiredArgsConstructor;

// 판매글 조회 Adapter
@Component
@RequiredArgsConstructor
public class ProductPostLoadAdapter implements ProductPostLoadPort {

	// 목록에 노출하는 거래 상태 (숨김·삭제 제외, B 정책)
	private static final List<TradeStatus> LIST_TRADE_STATUSES = List.of(
			TradeStatus.SELLING,
			TradeStatus.RESERVED,
			TradeStatus.SOLD_OUT
	);

	// IN 절 placeholder (hasXxx=false 일 때 빈 IN 방지)
	private static final List<String> UNUSED_STRINGS = List.of("__unused__");
	// 서류 IN 절 placeholder
	private static final List<DocumentType> UNUSED_DOCUMENT_TYPES = List.of(DocumentType.WARRANTY);

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

	// 목록 카드 페이지 조회
	@Override
	public ProductPostCardPageProjection findCards(ProductPostListCriteria criteria) {
		boolean hasCategories = criteria.getCategoryUuids() != null && !criteria.getCategoryUuids().isEmpty();
		boolean hasGrades = criteria.getConditionGrades() != null && !criteria.getConditionGrades().isEmpty();
		boolean hasDocumentTypes = criteria.getDocumentTypes() != null && !criteria.getDocumentTypes().isEmpty();
		String memberUuid = blankToNull(criteria.getMemberUuid());
		String keyword = blankToNull(criteria.getKeyword());

		Page<ProductPostEntity> page = productPostJpaRepository.searchForList(
				ProductPostStatus.PUBLIC,
				LIST_TRADE_STATUSES,
				hasCategories,
				hasCategories ? criteria.getCategoryUuids() : UNUSED_STRINGS,
				memberUuid,
				keyword,
				criteria.getMinPrice(),
				criteria.getMaxPrice(),
				hasGrades,
				hasGrades ? criteria.getConditionGrades() : UNUSED_STRINGS,
				hasDocumentTypes,
				hasDocumentTypes ? criteria.getDocumentTypes() : UNUSED_DOCUMENT_TYPES,
				PageRequest.of(criteria.getPage(), criteria.getSize())
		);

		Map<Long, String> thumbnails = loadThumbnailUrls(page.getContent());
		List<ProductPostCardProjection> content = page.getContent().stream()
				.map(entity -> toCardProjection(entity, thumbnails.get(entity.getProductPostId())))
				.toList();

		return ProductPostCardPageProjection.builder()
				.content(content)
				.totalElements(page.getTotalElements())
				.page(criteria.getPage())
				.size(criteria.getSize())
				.build();
	}

	// 부모 + 자식 Entity를 Domain으로 조립
	private ProductPost toDomainWithChildren(ProductPostEntity entity) {
		List<ProductPostImageEntity> images = productPostImageJpaRepository
				.findByProductPostIdOrderBySortOrderAscProductPostImageIdAsc(entity.getProductPostId());
		List<ProductPostDocumentEntity> documents = productPostDocumentJpaRepository
				.findByProductPostIdOrderByProductPostDocumentIdAsc(entity.getProductPostId());
		return ProductPostEntityMapper.toDomain(entity, images, documents);
	}

	// 판매글별 대표 이미지 URL 일괄 조회
	private Map<Long, String> loadThumbnailUrls(List<ProductPostEntity> entities) {
		if (entities.isEmpty()) {
			return Map.of();
		}
		List<Long> ids = entities.stream().map(ProductPostEntity::getProductPostId).toList();
		List<ProductPostImageEntity> images = productPostImageJpaRepository
				.findByProductPostIdInAndDeletedAtIsNullOrderByProductPostIdAscSortOrderAscProductPostImageIdAsc(ids);

		Map<Long, String> thumbnails = new HashMap<>();
		for (ProductPostImageEntity image : images) {
			thumbnails.putIfAbsent(image.getProductPostId(), image.getImageUrl());
		}
		return thumbnails;
	}

	// Entity → 카드 Projection
	private ProductPostCardProjection toCardProjection(ProductPostEntity entity, String thumbnailUrl) {
		Instant listedAt = entity.getBumpedAt() != null ? entity.getBumpedAt() : entity.getCreatedAt();
		return ProductPostCardProjection.builder()
				.productPostUuid(entity.getProductPostUuid())
				.productPostName(entity.getProductPostName())
				.price(entity.getPrice())
				.tradeStatus(entity.getTradeStatus())
				.listedAt(listedAt)
				.thumbnailUrl(thumbnailUrl)
				.build();
	}

	// blank 검색어는 조건 미적용
	private String blankToNull(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return null;
		}
		return keyword.trim();
	}
}
