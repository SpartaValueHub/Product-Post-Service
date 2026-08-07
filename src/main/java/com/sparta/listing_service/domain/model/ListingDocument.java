package com.sparta.listing_service.domain.model;

import java.time.Instant;
import java.util.Objects;

import lombok.Getter;

// 판매글 서류(보증서·영수증·감정서) 도메인
@Getter
public class ListingDocument {

	// 서류 이미지 URL 최대 길이
	private static final int IMAGE_URL_MAX_LENGTH = 500;

	// DB PK (신규면 null)
	private Long listingDocumentId;
	// 외부 공개용 서류 UUID
	private final String listingDocumentUuid;
	// 소속 판매글 PK (신규 저장 전이면 null)
	private Long listingId;
	// 서류 종류
	private DocumentType documentType;
	// 서류 이미지 경로
	private String imageUrl;
	// 생성일시
	private final Instant createdAt;
	// 수정일시
	private Instant updatedAt;
	// 삭제일시 (소프트 삭제)
	private Instant deletedAt;

	private ListingDocument(
			Long listingDocumentId,
			String listingDocumentUuid,
			Long listingId,
			DocumentType documentType,
			String imageUrl,
			Instant createdAt,
			Instant updatedAt,
			Instant deletedAt
	) {
		this.listingDocumentId = listingDocumentId;
		this.listingDocumentUuid = listingDocumentUuid;
		this.listingId = listingId;
		this.documentType = documentType;
		this.imageUrl = imageUrl;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
	}

	// 신규 서류 생성
	public static ListingDocument create(
			String listingDocumentUuid,
			DocumentType documentType,
			String imageUrl,
			Instant createdAt
	) {
		validateUuid(listingDocumentUuid);
		Objects.requireNonNull(documentType, "서류 종류는 필수입니다.");
		validateImageUrl(imageUrl);
		Objects.requireNonNull(createdAt, "생성 시각은 필수입니다.");

		return new ListingDocument(
				null,
				listingDocumentUuid,
				null,
				documentType,
				imageUrl.trim(),
				createdAt,
				null,
				null
		);
	}

	// 저장소 조회 값으로 복원
	public static ListingDocument restore(
			Long listingDocumentId,
			String listingDocumentUuid,
			Long listingId,
			DocumentType documentType,
			String imageUrl,
			Instant createdAt,
			Instant updatedAt,
			Instant deletedAt
	) {
		Objects.requireNonNull(listingDocumentId, "서류 ID는 필수입니다.");
		validateUuid(listingDocumentUuid);
		Objects.requireNonNull(listingId, "판매글 ID는 필수입니다.");
		Objects.requireNonNull(documentType, "서류 종류는 필수입니다.");
		validateImageUrl(imageUrl);
		Objects.requireNonNull(createdAt, "생성 시각은 필수입니다.");

		return new ListingDocument(
				listingDocumentId,
				listingDocumentUuid,
				listingId,
				documentType,
				imageUrl,
				createdAt,
				updatedAt,
				deletedAt
		);
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

	private static void validateUuid(String listingDocumentUuid) {
		if (listingDocumentUuid == null || listingDocumentUuid.isBlank()) {
			throw new IllegalArgumentException("서류 UUID는 필수입니다.");
		}
	}

	private static void validateImageUrl(String imageUrl) {
		if (imageUrl == null || imageUrl.isBlank()) {
			throw new IllegalArgumentException("서류 이미지 경로는 필수입니다.");
		}
		if (imageUrl.trim().length() > IMAGE_URL_MAX_LENGTH) {
			throw new IllegalArgumentException("서류 이미지 경로는 최대 500자까지 가능합니다.");
		}
	}
}
