package com.sparta.product_post_service.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.product_post_service.application.exception.ForbiddenException;
import com.sparta.product_post_service.application.exception.UnauthorizedException;
import com.sparta.product_post_service.application.port.in.ChangeProductPostVisibilityUseCase;
import com.sparta.product_post_service.application.port.in.CreateProductPostUseCase;
import com.sparta.product_post_service.application.port.in.DeleteProductPostUseCase;
import com.sparta.product_post_service.application.port.in.UpdateProductPostUseCase;
import com.sparta.product_post_service.application.port.in.dto.ChangeProductPostVisibilityCommand;
import com.sparta.product_post_service.application.port.in.dto.CreateProductPostCommand;
import com.sparta.product_post_service.application.port.in.dto.CreateProductPostDocumentCommand;
import com.sparta.product_post_service.application.port.in.dto.CreateProductPostImageCommand;
import com.sparta.product_post_service.application.port.in.dto.ProductPostDocumentSummaryDto;
import com.sparta.product_post_service.application.port.in.dto.ProductPostImageSummaryDto;
import com.sparta.product_post_service.application.port.in.dto.ProductPostSummaryDto;
import com.sparta.product_post_service.application.port.in.dto.UpdateProductPostCommand;
import com.sparta.product_post_service.application.port.out.ProductPostLoadPort;
import com.sparta.product_post_service.application.port.out.ProductPostSavePort;
import com.sparta.product_post_service.config.ProductPostPolicyProperties;
import com.sparta.product_post_service.domain.exception.ProductPostNotFoundException;
import com.sparta.product_post_service.domain.model.ProductPost;
import com.sparta.product_post_service.domain.model.ProductPostDocument;
import com.sparta.product_post_service.domain.model.ProductPostImage;
import com.sparta.product_post_service.domain.model.ProductPostStatus;

import lombok.RequiredArgsConstructor;

// 판매글 쓰기 Application Service
@Service
@RequiredArgsConstructor
public class ProductPostCommandService implements CreateProductPostUseCase, UpdateProductPostUseCase,
		DeleteProductPostUseCase, ChangeProductPostVisibilityUseCase {

	// 판매글 저장 Port
	private final ProductPostSavePort productPostSavePort;
	// 판매글 조회 Port (수정 시 기존 데이터 로드)
	private final ProductPostLoadPort productPostLoadPort;
	// 생성 시각용 시계 (테스트 교체 가능)
	private final Clock clock;
	// 판매글 정책 (최소가 등)
	private final ProductPostPolicyProperties productPostPolicyProperties;

	// 판매글 등록
	@Override
	@Transactional
	public ProductPostSummaryDto create(String memberUuid, CreateProductPostCommand command) {
		requireMemberUuid(memberUuid);

		Instant createdAt = Instant.now(clock);
		List<ProductPostImage> images = toImages(command.getImages(), createdAt);
		List<ProductPostDocument> documents = toDocuments(command.getDocuments(), createdAt);

		ProductPost productPost = ProductPost.create(
				newUuid(),
				memberUuid.trim(),
				command.getCategoryUuid(),
				command.getProductPostName(),
				command.getConditionGrade(),
				command.getPrice(),
				command.getDescription(),
				command.getLatitude(),
				command.getLongitude(),
				command.getPlaceName(),
				images,
				documents,
				productPostPolicyProperties.minPrice(),
				createdAt
		);

		ProductPost saved = productPostSavePort.save(productPost);
		return toSummary(saved);
	}

	// 판매글 수정 (본인·SELLING·미삭제만)
	@Override
	@Transactional
	public ProductPostSummaryDto update(String memberUuid, String productPostUuid, UpdateProductPostCommand command) {
		requireMemberUuid(memberUuid);

		if (productPostUuid == null || productPostUuid.isBlank()) {
			throw new ProductPostNotFoundException("판매글을 찾을 수 없습니다.");
		}

		ProductPost existing = productPostLoadPort.findByUuid(productPostUuid.trim())
				.filter(this::isUpdatable)
				.orElseThrow(() -> new ProductPostNotFoundException("판매글을 찾을 수 없습니다."));

		if (!existing.getMemberUuid().equals(memberUuid.trim())) {
			throw new ForbiddenException("판매글을 수정할 권한이 없습니다.");
		}

		Instant updatedAt = Instant.now(clock);
		List<ProductPostImage> images = toImages(command.getImages(), updatedAt);
		List<ProductPostDocument> documents = toDocuments(command.getDocuments(), updatedAt);

		existing.updateContent(
				command.getCategoryUuid(),
				command.getProductPostName(),
				command.getConditionGrade(),
				command.getPrice(),
				command.getDescription(),
				command.getLatitude(),
				command.getLongitude(),
				command.getPlaceName(),
				images,
				documents,
				productPostPolicyProperties.minPrice(),
				updatedAt
		);

		ProductPost saved = productPostSavePort.update(existing);
		return toSummary(saved);
	}

	// 판매글 Soft Delete (본인·미삭제, 거래상태 무관)
	@Override
	@Transactional
	public void delete(String memberUuid, String productPostUuid) {
		ProductPost existing = loadOwnedMutablePost(memberUuid, productPostUuid, "판매글을 삭제할 권한이 없습니다.");
		existing.softDelete(Instant.now(clock));
		productPostSavePort.update(existing);
	}

	// 판매글 노출 상태 변경 (본인·미삭제, HIDDEN|PUBLIC)
	@Override
	@Transactional
	public ProductPostSummaryDto changeVisibility(
			String memberUuid,
			String productPostUuid,
			ChangeProductPostVisibilityCommand command
	) {
		requireVisibilityTarget(command.getProductPostStatus());

		ProductPost existing = loadOwnedMutablePost(
				memberUuid,
				productPostUuid,
				"판매글 노출 상태를 변경할 권한이 없습니다."
		);

		if (command.getProductPostStatus() == ProductPostStatus.HIDDEN) {
			existing.hide();
		} else {
			existing.publish();
		}

		ProductPost saved = productPostSavePort.update(existing);
		return toSummary(saved);
	}

	// 본인 소유·미삭제 판매글 로드
	private ProductPost loadOwnedMutablePost(String memberUuid, String productPostUuid, String forbiddenMessage) {
		requireMemberUuid(memberUuid);

		if (productPostUuid == null || productPostUuid.isBlank()) {
			throw new ProductPostNotFoundException("판매글을 찾을 수 없습니다.");
		}

		ProductPost existing = productPostLoadPort.findByUuid(productPostUuid.trim())
				.filter(this::isUpdatable)
				.orElseThrow(() -> new ProductPostNotFoundException("판매글을 찾을 수 없습니다."));

		if (!existing.getMemberUuid().equals(memberUuid.trim())) {
			throw new ForbiddenException(forbiddenMessage);
		}

		return existing;
	}

	// visibility PATCH 허용 값 (DELETED는 DELETE API 전용)
	private void requireVisibilityTarget(ProductPostStatus productPostStatus) {
		if (productPostStatus == null) {
			throw new IllegalArgumentException("노출 상태는 필수입니다.");
		}
		if (productPostStatus != ProductPostStatus.HIDDEN && productPostStatus != ProductPostStatus.PUBLIC) {
			throw new IllegalArgumentException("노출 상태는 HIDDEN 또는 PUBLIC만 지정할 수 있습니다.");
		}
	}

	// 수정 대상 조회 가능 여부 (삭제·DELETED 제외, HIDDEN 허용)
	private boolean isUpdatable(ProductPost productPost) {
		return productPost.getDeletedAt() == null
				&& productPost.getProductPostStatus() != ProductPostStatus.DELETED;
	}

	// Gateway 헤더 누락 시 인증 실패로 처리
	private void requireMemberUuid(String memberUuid) {
		if (memberUuid == null || memberUuid.isBlank()) {
			throw new UnauthorizedException("판매자 정보가 없습니다.");
		}
	}

	// 이미지 Command → Domain (배열 인덱스+1 이 sort_order, 0번이 대표/썸네일)
	private List<ProductPostImage> toImages(List<CreateProductPostImageCommand> images, Instant createdAt) {
		if (images == null) {
			return List.of();
		}
		List<ProductPostImage> result = new ArrayList<>(images.size());
		for (int i = 0; i < images.size(); i++) {
			CreateProductPostImageCommand image = images.get(i);
			result.add(ProductPostImage.create(
					newUuid(),
					image.getImageUrl(),
					i + 1,
					createdAt
			));
		}
		return List.copyOf(result);
	}

	// 서류 Command → Domain
	private List<ProductPostDocument> toDocuments(List<CreateProductPostDocumentCommand> documents, Instant createdAt) {
		if (documents == null || documents.isEmpty()) {
			return List.of();
		}
		return documents.stream()
				.map(document -> ProductPostDocument.create(
						newUuid(),
						document.getDocumentType(),
						document.getImageUrl(),
						createdAt
				))
				.toList();
	}

	// Domain → 요약 DTO (활성 이미지·서류만)
	private ProductPostSummaryDto toSummary(ProductPost listing) {
		List<ProductPostImageSummaryDto> images = listing.activeImages().stream()
				.map(image -> ProductPostImageSummaryDto.builder()
						.productPostImageUuid(image.getProductPostImageUuid())
						.imageUrl(image.getImageUrl())
						.sortOrder(image.getSortOrder())
						.build())
				.toList();
		List<ProductPostDocumentSummaryDto> documents = listing.activeDocuments().stream()
				.map(document -> ProductPostDocumentSummaryDto.builder()
						.productPostDocumentUuid(document.getProductPostDocumentUuid())
						.documentType(document.getDocumentType())
						.imageUrl(document.getImageUrl())
						.build())
				.toList();

		return ProductPostSummaryDto.builder()
				.productPostUuid(listing.getProductPostUuid())
				.memberUuid(listing.getMemberUuid())
				.categoryUuid(listing.getCategoryUuid())
				.productPostName(listing.getProductPostName())
				.conditionGrade(listing.getConditionGrade())
				.price(listing.getPrice())
				.description(listing.getDescription())
				.tradeStatus(listing.getTradeStatus())
				.productPostStatus(listing.getProductPostStatus())
				.latitude(listing.getLatitude())
				.longitude(listing.getLongitude())
				.placeName(listing.getPlaceName())
				.createdAt(listing.getCreatedAt())
				.images(images)
				.documents(documents)
				.build();
	}

	// 업무 UUID 생성 (Application 책임)
	private String newUuid() {
		return UUID.randomUUID().toString();
	}
}
