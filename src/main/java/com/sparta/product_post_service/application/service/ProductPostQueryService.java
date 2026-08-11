package com.sparta.product_post_service.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.product_post_service.application.port.in.GetProductPostUseCase;
import com.sparta.product_post_service.application.port.in.dto.ProductPostDocumentSummaryDto;
import com.sparta.product_post_service.application.port.in.dto.ProductPostImageSummaryDto;
import com.sparta.product_post_service.application.port.in.dto.ProductPostSummaryDto;
import com.sparta.product_post_service.application.port.out.ProductPostLoadPort;
import com.sparta.product_post_service.domain.exception.ProductPostNotFoundException;
import com.sparta.product_post_service.domain.model.ProductPost;
import com.sparta.product_post_service.domain.model.ProductPostStatus;

import lombok.RequiredArgsConstructor;

// 판매글 조회 Application Service
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductPostQueryService implements GetProductPostUseCase {

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
}
