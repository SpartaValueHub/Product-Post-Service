package com.sparta.listing_service.domain.model;

import java.time.Instant;
import java.util.Objects;

import lombok.Getter;

// 판매글 상품 이미지 도메인
@Getter
public class ProductPostImage {

	// 이미지 URL 최대 길이
	private static final int IMAGE_URL_MAX_LENGTH = 500;

	// DB PK (신규면 null)
	private Long productPostImageId;
	// 외부 공개용 이미지 UUID
	private final String productPostImageUuid;
	// 소속 판매글 PK (신규 저장 전이면 null)
	private Long productPostId;
	// 이미지 경로
	private String imageUrl;
	// 노출 순서 (작을수록 앞, 최소값이 대표/썸네일)
	private int sortOrder;
	// 생성일시
	private final Instant createdAt;
	// 수정일시
	private Instant updatedAt;
	// 삭제일시 (소프트 삭제)
	private Instant deletedAt;

	private ProductPostImage(
			Long productPostImageId,
			String productPostImageUuid,
			Long productPostId,
			String imageUrl,
			int sortOrder,
			Instant createdAt,
			Instant updatedAt,
			Instant deletedAt
	) {
		this.productPostImageId = productPostImageId;
		this.productPostImageUuid = productPostImageUuid;
		this.productPostId = productPostId;
		this.imageUrl = imageUrl;
		this.sortOrder = sortOrder;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
	}

	// 신규 이미지 생성
	public static ProductPostImage create(
			String productPostImageUuid,
			String imageUrl,
			int sortOrder,
			Instant createdAt
	) {
		validateUuid(productPostImageUuid);
		validateImageUrl(imageUrl);
		validateSortOrder(sortOrder);
		Objects.requireNonNull(createdAt, "생성 시각은 필수입니다.");

		return new ProductPostImage(
				null,
				productPostImageUuid,
				null,
				imageUrl.trim(),
				sortOrder,
				createdAt,
				null,
				null
		);
	}

	// 저장소 조회 값으로 복원
	public static ProductPostImage restore(
			Long productPostImageId,
			String productPostImageUuid,
			Long productPostId,
			String imageUrl,
			int sortOrder,
			Instant createdAt,
			Instant updatedAt,
			Instant deletedAt
	) {
		Objects.requireNonNull(productPostImageId, "이미지 ID는 필수입니다.");
		validateUuid(productPostImageUuid);
		Objects.requireNonNull(productPostId, "판매글 ID는 필수입니다.");
		validateImageUrl(imageUrl);
		validateSortOrder(sortOrder);
		Objects.requireNonNull(createdAt, "생성 시각은 필수입니다.");

		return new ProductPostImage(
				productPostImageId,
				productPostImageUuid,
				productPostId,
				imageUrl,
				sortOrder,
				createdAt,
				updatedAt,
				deletedAt
		);
	}

	// 노출 순서 변경
	public void changeSortOrder(int sortOrder) {
		validateSortOrder(sortOrder);
		this.sortOrder = sortOrder;
	}

	// 소프트 삭제
	public void softDelete(Instant deletedAt) {
		Objects.requireNonNull(deletedAt, "삭제 시각은 필수입니다.");
		this.deletedAt = deletedAt;
	}

	// 삭제되지 않았는지
	public boolean isActive() {
		return deletedAt == null;
	}

	private static void validateUuid(String productPostImageUuid) {
		if (productPostImageUuid == null || productPostImageUuid.isBlank()) {
			throw new IllegalArgumentException("이미지 UUID는 필수입니다.");
		}
	}

	private static void validateImageUrl(String imageUrl) {
		if (imageUrl == null || imageUrl.isBlank()) {
			throw new IllegalArgumentException("이미지 경로는 필수입니다.");
		}
		if (imageUrl.trim().length() > IMAGE_URL_MAX_LENGTH) {
			throw new IllegalArgumentException("이미지 경로는 최대 500자까지 가능합니다.");
		}
	}

	private static void validateSortOrder(int sortOrder) {
		if (sortOrder < 1) {
			throw new IllegalArgumentException("이미지 노출 순서는 1 이상이어야 합니다.");
		}
	}
}
