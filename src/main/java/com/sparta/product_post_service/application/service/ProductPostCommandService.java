package com.sparta.product_post_service.application.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.product_post_service.application.exception.ForbiddenException;
import com.sparta.product_post_service.application.exception.UnauthorizedException;
import com.sparta.product_post_service.application.port.in.BumpProductPostUseCase;
import com.sparta.product_post_service.application.port.in.ChangeProductPostTradeStatusUseCase;
import com.sparta.product_post_service.application.port.in.ChangeProductPostVisibilityUseCase;
import com.sparta.product_post_service.application.port.in.CreateProductPostUseCase;
import com.sparta.product_post_service.application.port.in.DeleteProductPostUseCase;
import com.sparta.product_post_service.application.port.in.UpdateProductPostUseCase;
import com.sparta.product_post_service.application.port.in.dto.ChangeProductPostTradeStatusCommand;
import com.sparta.product_post_service.application.port.in.dto.ChangeProductPostVisibilityCommand;
import com.sparta.product_post_service.application.port.in.dto.CreateProductPostCommand;
import com.sparta.product_post_service.application.port.in.dto.CreateProductPostDocumentCommand;
import com.sparta.product_post_service.application.port.in.dto.CreateProductPostImageCommand;
import com.sparta.product_post_service.application.port.in.dto.ProductPostDocumentSummaryDto;
import com.sparta.product_post_service.application.port.in.dto.ProductPostImageSummaryDto;
import com.sparta.product_post_service.application.port.in.dto.ProductPostSummaryDto;
import com.sparta.product_post_service.application.port.in.dto.UpdateProductPostCommand;
import com.sparta.product_post_service.application.port.out.BumpQuotaLoadPort;
import com.sparta.product_post_service.application.port.out.BumpQuotaSavePort;
import com.sparta.product_post_service.application.port.out.ProductPostLoadPort;
import com.sparta.product_post_service.application.port.out.ProductPostSavePort;
import com.sparta.product_post_service.config.ProductPostPolicyProperties;
import com.sparta.product_post_service.domain.exception.ProductPostNotFoundException;
import com.sparta.product_post_service.domain.model.BumpQuota;
import com.sparta.product_post_service.domain.model.ProductPost;
import com.sparta.product_post_service.domain.model.ProductPostDocument;
import com.sparta.product_post_service.domain.model.ProductPostImage;
import com.sparta.product_post_service.domain.model.ProductPostStatus;
import com.sparta.product_post_service.domain.model.TradeStatus;

import lombok.RequiredArgsConstructor;

// 판매글 쓰기 Application Service
@Service
@RequiredArgsConstructor
public class ProductPostCommandService implements CreateProductPostUseCase, UpdateProductPostUseCase,
		DeleteProductPostUseCase, ChangeProductPostVisibilityUseCase, ChangeProductPostTradeStatusUseCase,
		BumpProductPostUseCase {

	// 판매글 저장 Port
	private final ProductPostSavePort productPostSavePort;
	// 판매글 조회 Port (수정 시 기존 데이터 로드)
	private final ProductPostLoadPort productPostLoadPort;
	// pending 미디어 승격
	private final PromotePendingMediaService promotePendingMediaService;
	// 끌올 일일 횟수 조회 Port
	private final BumpQuotaLoadPort bumpQuotaLoadPort;
	// 끌올 일일 횟수 저장 Port
	private final BumpQuotaSavePort bumpQuotaSavePort;
	// 생성 시각용 시계 (테스트 교체 가능)
	private final Clock clock;
	// 판매글 정책 (최소가, 끌올 한도·쿨다운 등)
	private final ProductPostPolicyProperties productPostPolicyProperties;

	// 일일 한도 기준 시간대 (KST)
	private static final ZoneId QUOTA_ZONE = ZoneId.of("Asia/Seoul");

	// 판매글 등록
	@Override
	@Transactional
	public ProductPostSummaryDto create(String memberUuid, CreateProductPostCommand command) {
		requireMemberUuid(memberUuid);

		Instant createdAt = Instant.now(clock);
		PromotedMedia promoted = promoteMedia(memberUuid.trim(), command.getImages(), command.getDocuments());
		List<ProductPostImage> images = toImages(promoted.imageUrls(), createdAt);
		List<ProductPostDocument> documents = toDocuments(command.getDocuments(), promoted.documentUrls(), createdAt);

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
				command.getRegionDong(),
				command.getRegionGu(),
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
		List<String> previousMediaUrls = collectMediaUrls(existing);
		PromotedMedia promoted = promoteMedia(memberUuid.trim(), command.getImages(), command.getDocuments());
		List<ProductPostImage> images = toImages(promoted.imageUrls(), updatedAt);
		List<ProductPostDocument> documents = toDocuments(command.getDocuments(), promoted.documentUrls(), updatedAt);

		existing.updateContent(
				command.getCategoryUuid(),
				command.getProductPostName(),
				command.getConditionGrade(),
				command.getPrice(),
				command.getDescription(),
				command.getLatitude(),
				command.getLongitude(),
				command.getPlaceName(),
				command.getRegionDong(),
				command.getRegionGu(),
				images,
				documents,
				productPostPolicyProperties.minPrice(),
				updatedAt
		);

		ProductPost saved = productPostSavePort.update(existing);
		deleteRemovedConfirmedMedia(memberUuid.trim(), previousMediaUrls, collectMediaUrls(saved));
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

	// 판매글 거래 상태 변경 (본인·미삭제, Domain 전이 규칙)
	@Override
	@Transactional
	public ProductPostSummaryDto changeTradeStatus(
			String memberUuid,
			String productPostUuid,
			ChangeProductPostTradeStatusCommand command
	) {
		requireTradeStatusTarget(command.getTradeStatus());

		ProductPost existing = loadOwnedMutablePost(
				memberUuid,
				productPostUuid,
				"판매글 거래 상태를 변경할 권한이 없습니다."
		);

		existing.transitionTradeStatus(command.getTradeStatus());

		ProductPost saved = productPostSavePort.update(existing);
		return toSummary(saved);
	}

	// 판매글 끌올 (본인·SELLING·PUBLIC, 쿨다운·일일 한도 적용)
	@Override
	@Transactional
	public ProductPostSummaryDto bump(String memberUuid, String productPostUuid) {
		ProductPost existing = loadOwnedMutablePost(
				memberUuid,
				productPostUuid,
				"판매글을 끌올할 권한이 없습니다."
		);

		requireBumpableStatus(existing);

		Instant now = Instant.now(clock);
		requireCooldownElapsed(existing, now);

		LocalDate today = now.atZone(QUOTA_ZONE).toLocalDate();
		BumpQuota quota = bumpQuotaLoadPort
				.findByMemberUuidAndQuotaDate(memberUuid.trim(), today)
				.orElseGet(() -> BumpQuota.create(memberUuid.trim(), today, now));

		quota.increaseUsage(productPostPolicyProperties.bumpDailyLimit(), now);

		existing.markBumped(now);

		productPostSavePort.update(existing);

		if (quota.getBumpQuotaId() == null) {
			bumpQuotaSavePort.save(quota);
		} else {
			bumpQuotaSavePort.update(quota);
		}

		return toSummary(existing);
	}

	// 끌올 가능 상태 확인 (SELLING + PUBLIC)
	private void requireBumpableStatus(ProductPost post) {
		if (post.getTradeStatus() != TradeStatus.SELLING) {
			throw new IllegalArgumentException("판매중 상태에서만 끌올할 수 있습니다.");
		}
		if (post.getProductPostStatus() != ProductPostStatus.PUBLIC) {
			throw new IllegalArgumentException("공개 상태에서만 끌올할 수 있습니다.");
		}
	}

	// 동일 상품 쿨다운 확인
	private void requireCooldownElapsed(ProductPost post, Instant now) {
		if (post.getBumpedAt() == null) {
			return;
		}
		Duration elapsed = Duration.between(post.getBumpedAt(), now);
		long cooldownHours = productPostPolicyProperties.bumpCooldownHours();
		if (elapsed.toHours() < cooldownHours) {
			long remainMinutes = Duration.ofHours(cooldownHours).minus(elapsed).toMinutes();
			throw new IllegalArgumentException(
					"끌올 쿨다운 중입니다. " + remainMinutes + "분 후 다시 시도해주세요."
			);
		}
	}

	// trade-status PATCH 허용 값
	private void requireTradeStatusTarget(TradeStatus tradeStatus) {
		if (tradeStatus == null) {
			throw new IllegalArgumentException("거래 상태는 필수입니다.");
		}
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

	// 이미지·서류 URL을 한 번에 승격한다. 일부 실패 시 전부 실패.
	private PromotedMedia promoteMedia(
			String memberUuid,
			List<CreateProductPostImageCommand> images,
			List<CreateProductPostDocumentCommand> documents
	) {
		List<String> requested = new ArrayList<>();
		int imageCount = images == null ? 0 : images.size();
		if (images != null) {
			for (CreateProductPostImageCommand image : images) {
				requested.add(image.getImageUrl());
			}
		}
		if (documents != null) {
			for (CreateProductPostDocumentCommand document : documents) {
				requested.add(document.getImageUrl());
			}
		}
		List<String> persisted = promotePendingMediaService.persistAll(memberUuid, requested);
		List<String> imageUrls = persisted.subList(0, imageCount);
		List<String> documentUrls = persisted.subList(imageCount, persisted.size());
		return new PromotedMedia(imageUrls, documentUrls);
	}

	// 승격된 이미지 URL → Domain (배열 인덱스+1 이 sort_order)
	private List<ProductPostImage> toImages(List<String> imageUrls, Instant createdAt) {
		if (imageUrls == null || imageUrls.isEmpty()) {
			return List.of();
		}
		List<ProductPostImage> result = new ArrayList<>(imageUrls.size());
		for (int i = 0; i < imageUrls.size(); i++) {
			result.add(ProductPostImage.create(
					newUuid(),
					imageUrls.get(i),
					i + 1,
					createdAt
			));
		}
		return List.copyOf(result);
	}

	// 승격된 서류 URL → Domain
	private List<ProductPostDocument> toDocuments(
			List<CreateProductPostDocumentCommand> documents,
			List<String> imageUrls,
			Instant createdAt
	) {
		if (documents == null || documents.isEmpty()) {
			return List.of();
		}
		List<ProductPostDocument> result = new ArrayList<>(documents.size());
		for (int i = 0; i < documents.size(); i++) {
			CreateProductPostDocumentCommand document = documents.get(i);
			result.add(ProductPostDocument.create(
					newUuid(),
					document.getDocumentType(),
					imageUrls.get(i),
					createdAt
			));
		}
		return List.copyOf(result);
	}

	// 승격 결과 (이미지 URL / 서류 URL)
	private record PromotedMedia(
			// 승격된 이미지 publicUrl
			List<String> imageUrls,
			// 승격된 서류 publicUrl
			List<String> documentUrls
	) {
	}

	// 활성 이미지·서류 publicUrl 목록
	private List<String> collectMediaUrls(ProductPost productPost) {
		List<String> urls = new ArrayList<>();
		productPost.activeImages().forEach(image -> urls.add(image.getImageUrl()));
		productPost.activeDocuments().forEach(document -> urls.add(document.getImageUrl()));
		return urls;
	}

	// 수정 후 빠진 정식 객체를 저장소에서 제거한다.
	private void deleteRemovedConfirmedMedia(String memberUuid, List<String> previousUrls, List<String> nextUrls) {
		for (String previousUrl : previousUrls) {
			if (!nextUrls.contains(previousUrl)) {
				promotePendingMediaService.deleteConfirmedIfOwned(memberUuid, previousUrl);
			}
		}
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
				.regionDong(listing.getRegionDong())
				.regionGu(listing.getRegionGu())
				.placeName(listing.getPlaceName())
				.bumpedAt(listing.getBumpedAt())
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
