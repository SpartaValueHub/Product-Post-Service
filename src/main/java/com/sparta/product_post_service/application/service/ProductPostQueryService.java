package com.sparta.product_post_service.application.service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.sparta.product_post_service.application.support.ProductPostListGeoFilterResolver;
import com.sparta.product_post_service.application.support.SearchSessionKeyResolver;
import com.sparta.product_post_service.application.support.SearchTermNormalizer;
import com.sparta.product_post_service.config.SearchProperties;
import com.sparta.product_post_service.application.port.out.dto.ProductPostListGeoFilter;
import com.sparta.product_post_service.domain.exception.ProductPostNotFoundException;
import com.sparta.product_post_service.domain.model.DocumentType;
import com.sparta.product_post_service.domain.model.ProductPost;
import com.sparta.product_post_service.domain.model.ProductPostStatus;
import com.sparta.product_post_service.domain.model.TradeStatus;

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
	// 검색어 비동기 기록
	private final SearchTermRecordingService searchTermRecordingService;
	// 동시검색 세션 키
	private final SearchSessionKeyResolver searchSessionKeyResolver;
	// 검색 정책 (키워드 길이 등)
	private final SearchProperties searchProperties;
	// 목록 반경 필터 해석
	private final ProductPostListGeoFilterResolver productPostListGeoFilterResolver;

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

	// FO 목록 조회 (PUBLIC + 거래상태 필터, 끌올 반영 최신순)
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
		List<TradeStatus> tradeStatuses = normalizeTradeStatuses(query.getTradeStatus());
		String memberUuid = blankToNull(query.getMemberUuid());
		String keyword = SearchTermNormalizer.normalize(
				query.getKeyword(),
				searchProperties.keywordMaxLength()
		);
		String sessionKey = searchSessionKeyResolver.resolve(
				query.getSearcherMemberUuid(),
				query.getSearchSessionId()
		);

		// ngram 최소 길이 미만은 FULLTEXT 매칭 불가 — DB 조회 없이 빈 목록 (카운터는 기록)
		if (keyword != null && keyword.length() < searchProperties.fulltextMinKeywordLength()) {
			searchTermRecordingService.recordAsync(keyword, sessionKey);
			return emptyCardPage(page, size);
		}

		ProductPostListGeoFilter geoFilter = productPostListGeoFilterResolver.resolve(
				memberUuid,
				query.getCenterLatitude(),
				query.getCenterLongitude(),
				query.getRadiusKm()
		);
		if (geoFilter.isMissingCenterCoordinates()) {
			return emptyCardPage(page, size);
		}

		ProductPostCardPageProjection projection = productPostLoadPort.findCards(
				ProductPostListCriteria.builder()
						.productPostStatus(ProductPostStatus.PUBLIC)
						.categoryUuids(categoryUuids)
						.memberUuid(memberUuid)
						.tradeStatuses(tradeStatuses)
						.keyword(keyword)
						.minPrice(minPrice)
						.maxPrice(maxPrice)
						.conditionGrades(conditionGrades)
						.documentTypes(documentTypes)
						.geoFilter(geoFilter)
						.page(page - 1)
						.size(size)
						.build()
		);

		if (keyword != null) {
			searchTermRecordingService.recordAsync(keyword, sessionKey);
		}

		List<ProductPostCardDto> content = projection.getContent().stream()
				.map(card -> ProductPostCardDto.builder()
						.productPostUuid(card.getProductPostUuid())
						.productPostName(card.getProductPostName())
						.price(card.getPrice())
						.tradeStatus(card.getTradeStatus())
						.listedAt(card.getListedAt())
						.thumbnailUrl(card.getThumbnailUrl())
						.regionDong(card.getRegionDong())
						.regionGu(card.getRegionGu())
						.placeName(card.getPlaceName())
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

	// FULLTEXT 최소 길이 미달 시 빈 페이지
	private ProductPostCardPageDto emptyCardPage(int page, int size) {
		return ProductPostCardPageDto.builder()
				.content(List.of())
				.page(page)
				.size(size)
				.totalElements(0L)
				.totalPages(0)
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
				.regionDong(productPost.getRegionDong())
				.regionGu(productPost.getRegionGu())
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
								"서류 종류는 " + DocumentType.allowedNamesCsv() + " 중 하나여야 합니다."
						);
					}
				})
				.distinct()
				.toList();
	}

	// 거래 상태 정규화·검증 (미전달 시 목록 노출 가능 상태 전체)
	private List<TradeStatus> normalizeTradeStatuses(String tradeStatus) {
		if (tradeStatus == null || tradeStatus.isBlank()) {
			return listVisibleTradeStatuses();
		}
		return List.of(parseListVisibleTradeStatus(tradeStatus));
	}

	// Domain 플래그 기준으로 목록 노출 상태 수집
	private List<TradeStatus> listVisibleTradeStatuses() {
		return Arrays.stream(TradeStatus.values())
				.filter(TradeStatus::isListVisible)
				.toList();
	}

	// 목록 필터로 허용되는 거래 상태만 파싱
	private TradeStatus parseListVisibleTradeStatus(String tradeStatus) {
		String normalized = tradeStatus.trim().toUpperCase(Locale.ROOT);
		TradeStatus parsed;
		try {
			parsed = TradeStatus.valueOf(normalized);
		} catch (IllegalArgumentException ex) {
			throw invalidTradeStatusArgument();
		}
		if (!parsed.isListVisible()) {
			throw invalidTradeStatusArgument();
		}
		return parsed;
	}

	// 허용 값은 Domain 노출 플래그에서 조합
	private IllegalArgumentException invalidTradeStatusArgument() {
		String allowed = listVisibleTradeStatuses().stream()
				.map(Enum::name)
				.collect(Collectors.joining(", "));
		return new IllegalArgumentException("tradeStatus는 " + allowed + " 중 하나여야 합니다.");
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

	// blank 문자열은 필터 미적용
	private String blankToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
