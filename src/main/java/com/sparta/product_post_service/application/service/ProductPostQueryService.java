package com.sparta.product_post_service.application.service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.product_post_service.application.port.in.GetProductPostUseCase;
import com.sparta.product_post_service.application.port.in.ListProductPostsUseCase;
import com.sparta.product_post_service.application.port.in.dto.ListProductPostsQuery;
import com.sparta.product_post_service.application.port.in.dto.ProductPostCardDto;
import com.sparta.product_post_service.application.port.in.dto.ProductPostCardPageDto;
import com.sparta.product_post_service.application.port.in.dto.ProductPostDocumentSummaryDto;
import com.sparta.product_post_service.application.port.in.dto.ProductPostImageSummaryDto;
import com.sparta.product_post_service.application.port.in.dto.ProductPostSummaryDto;
import com.sparta.product_post_service.application.port.out.ProductPostLoadPort;
import com.sparta.product_post_service.application.port.out.dto.ProductPostCardPageProjection;
import com.sparta.product_post_service.application.port.out.dto.ProductPostListCriteria;
import com.sparta.product_post_service.domain.exception.ProductPostNotFoundException;
import com.sparta.product_post_service.domain.model.DocumentType;
import com.sparta.product_post_service.domain.model.ProductPost;
import com.sparta.product_post_service.domain.model.ProductPostStatus;

import lombok.RequiredArgsConstructor;

// 판매글 조회 Application Service
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductPostQueryService implements GetProductPostUseCase, ListProductPostsUseCase {

	// 기본·최대 페이지 크기
	private static final int DEFAULT_PAGE_SIZE = 20;
	private static final int MAX_PAGE_SIZE = 50;
	// 허용 상태 등급
	private static final Set<String> ALLOWED_CONDITION_GRADES = Set.of("S", "A", "B", "C");

	// 판매글 조회 Port
	private final ProductPostLoadPort productPostLoadPort;

	// 공개 판매글 상세 조회 (HIDDEN·DELETED는 미존재와 동일 처리)
	@Override
	public ProductPostSummaryDto getByUuid(String productPostUuid) {
		if (productPostUuid == null || productPostUuid.isBlank()) {
			throw new ProductPostNotFoundException("판매글을 찾을 수 없습니다.");
		}

		ProductPost productPost = productPostLoadPort.findByUuid(productPostUuid.trim())
				.filter(this::isPubliclyVisible)
				.orElseThrow(() -> new ProductPostNotFoundException("판매글을 찾을 수 없습니다."));

		return toSummary(productPost);
	}

	// FO 목록 조회 (PUBLIC + SELLING|RESERVED|SOLD_OUT, 끌올 반영 최신순)
	@Override
	public ProductPostCardPageDto list(ListProductPostsQuery query) {
		int page = normalizePage(query.getPage());
		int size = normalizeSize(query.getSize());
		Long minPrice = query.getMinPrice();
		Long maxPrice = query.getMaxPrice();
		validatePriceRange(minPrice, maxPrice);
		List<String> conditionGrades = normalizeConditionGrades(query.getConditionGrades());
		List<DocumentType> documentTypes = normalizeDocumentTypes(query.getDocumentTypes());
		List<String> categoryUuids = normalizeUuids(query.getCategoryUuids());
		String keyword = query.getKeyword() == null ? null : query.getKeyword().trim();

		ProductPostCardPageProjection projection = productPostLoadPort.findCards(
				ProductPostListCriteria.builder()
						.categoryUuids(categoryUuids)
						.keyword(keyword)
						.minPrice(minPrice)
						.maxPrice(maxPrice)
						.conditionGrades(conditionGrades)
						.documentTypes(documentTypes)
						.page(page - 1)
						.size(size)
						.build()
		);

		List<ProductPostCardDto> content = projection.getContent().stream()
				.map(card -> ProductPostCardDto.builder()
						.productPostUuid(card.getProductPostUuid())
						.productPostName(card.getProductPostName())
						.price(card.getPrice())
						.tradeStatus(card.getTradeStatus())
						.listedAt(card.getListedAt())
						.thumbnailUrl(card.getThumbnailUrl())
						.build())
				.toList();

		long totalElements = projection.getTotalElements();
		int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

		return ProductPostCardPageDto.builder()
				.content(content)
				.page(page)
				.size(size)
				.totalElements(totalElements)
				.totalPages(totalPages)
				.build();
	}

	// 일반 사용자에게 노출 가능한 상태인지
	private boolean isPubliclyVisible(ProductPost productPost) {
		return productPost.getProductPostStatus() == ProductPostStatus.PUBLIC
				&& productPost.getDeletedAt() == null;
	}

	// Domain → 요약 DTO (활성 이미지·서류만, sortOrder 오름차순 유지)
	private ProductPostSummaryDto toSummary(ProductPost productPost) {
		List<ProductPostImageSummaryDto> images = productPost.activeImages().stream()
				.map(image -> ProductPostImageSummaryDto.builder()
						.productPostImageUuid(image.getProductPostImageUuid())
						.imageUrl(image.getImageUrl())
						.sortOrder(image.getSortOrder())
						.build())
				.toList();
		List<ProductPostDocumentSummaryDto> documents = productPost.activeDocuments().stream()
				.map(document -> ProductPostDocumentSummaryDto.builder()
						.productPostDocumentUuid(document.getProductPostDocumentUuid())
						.documentType(document.getDocumentType())
						.imageUrl(document.getImageUrl())
						.build())
				.toList();

		return ProductPostSummaryDto.builder()
				.productPostUuid(productPost.getProductPostUuid())
				.memberUuid(productPost.getMemberUuid())
				.categoryUuid(productPost.getCategoryUuid())
				.productPostName(productPost.getProductPostName())
				.conditionGrade(productPost.getConditionGrade())
				.price(productPost.getPrice())
				.description(productPost.getDescription())
				.tradeStatus(productPost.getTradeStatus())
				.productPostStatus(productPost.getProductPostStatus())
				.latitude(productPost.getLatitude())
				.longitude(productPost.getLongitude())
				.placeName(productPost.getPlaceName())
				.createdAt(productPost.getCreatedAt())
				.images(images)
				.documents(documents)
				.build();
	}

	// 1-based page 정규화
	private int normalizePage(int page) {
		if (page < 1) {
			throw new IllegalArgumentException("page는 1 이상이어야 합니다.");
		}
		return page;
	}

	// size 정규화 (미지정·0 이하는 기본값)
	private int normalizeSize(int size) {
		if (size <= 0) {
			return DEFAULT_PAGE_SIZE;
		}
		if (size > MAX_PAGE_SIZE) {
			throw new IllegalArgumentException("size는 최대 " + MAX_PAGE_SIZE + "까지 가능합니다.");
		}
		return size;
	}

	// 가격 범위 검증
	private void validatePriceRange(Long minPrice, Long maxPrice) {
		if (minPrice != null && minPrice < 0) {
			throw new IllegalArgumentException("minPrice는 0 이상이어야 합니다.");
		}
		if (maxPrice != null && maxPrice < 0) {
			throw new IllegalArgumentException("maxPrice는 0 이상이어야 합니다.");
		}
		if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
			throw new IllegalArgumentException("minPrice는 maxPrice보다 클 수 없습니다.");
		}
	}

	// 상태 등급 정규화·검증
	private List<String> normalizeConditionGrades(List<String> conditionGrades) {
		if (conditionGrades == null || conditionGrades.isEmpty()) {
			return List.of();
		}
		return conditionGrades.stream()
				.filter(grade -> grade != null && !grade.isBlank())
				.map(grade -> grade.trim().toUpperCase(Locale.ROOT))
				.peek(grade -> {
					if (!ALLOWED_CONDITION_GRADES.contains(grade)) {
						throw new IllegalArgumentException("상품 상태 등급은 S, A, B, C 중 하나여야 합니다.");
					}
				})
				.distinct()
				.toList();
	}

	// 서류 종류 정규화·검증
	private List<DocumentType> normalizeDocumentTypes(List<String> documentTypes) {
		if (documentTypes == null || documentTypes.isEmpty()) {
			return List.of();
		}
		return documentTypes.stream()
				.filter(type -> type != null && !type.isBlank())
				.map(type -> type.trim().toUpperCase(Locale.ROOT))
				.map(type -> {
					try {
						return DocumentType.valueOf(type);
					} catch (IllegalArgumentException ex) {
						throw new IllegalArgumentException(
								"서류 종류는 WARRANTY, RECEIPT, APPRAISAL 중 하나여야 합니다."
						);
					}
				})
				.distinct()
				.toList();
	}

	// UUID 목록 정규화
	private List<String> normalizeUuids(List<String> uuids) {
		if (uuids == null || uuids.isEmpty()) {
			return List.of();
		}
		return uuids.stream()
				.filter(uuid -> uuid != null && !uuid.isBlank())
				.map(String::trim)
				.distinct()
				.toList();
	}
}
