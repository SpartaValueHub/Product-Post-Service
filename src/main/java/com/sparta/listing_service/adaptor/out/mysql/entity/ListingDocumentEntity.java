package com.sparta.listing_service.adaptor.out.mysql.entity;

import java.time.Instant;

import com.sparta.listing_service.domain.model.DocumentType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// listing_document 테이블 매핑 Entity
@Entity
@Table(name = "listing_document")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ListingDocumentEntity {

	// DB PK
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "listing_document_id")
	private Long listingDocumentId;

	// 소속 판매글 PK
	@Column(name = "listing_id", nullable = false)
	private Long listingId;

	// 외부 공개용 서류 UUID
	@Column(name = "listing_document_uuid", nullable = false, unique = true, length = 36)
	private String listingDocumentUuid;

	// 서류 종류
	@Enumerated(EnumType.STRING)
	@Column(name = "document_type", nullable = false, length = 20)
	private DocumentType documentType;

	// 서류 이미지 경로
	@Column(name = "image_url", nullable = false, length = 500)
	private String imageUrl;

	// 생성일시
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	// 수정일시
	@Column(name = "updated_at")
	private Instant updatedAt;

	// 삭제일시
	@Column(name = "deleted_at")
	private Instant deletedAt;

	// 신규 저장용 Entity 생성
	public static ListingDocumentEntity create(
			Long listingId,
			String listingDocumentUuid,
			DocumentType documentType,
			String imageUrl,
			Instant createdAt
	) {
		return ListingDocumentEntity.builder()
				.listingId(listingId)
				.listingDocumentUuid(listingDocumentUuid)
				.documentType(documentType)
				.imageUrl(imageUrl)
				.createdAt(createdAt)
				.updatedAt(null)
				.deletedAt(null)
				.build();
	}

	// 도메인 변경분을 Entity에 반영
	public void update(DocumentType documentType, String imageUrl, Instant updatedAt, Instant deletedAt) {
		this.documentType = documentType;
		this.imageUrl = imageUrl;
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
	}
}
