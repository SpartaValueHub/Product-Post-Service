package com.sparta.product_post_service.adaptor.out.mysql.entity;

import java.time.Instant;

import com.sparta.product_post_service.domain.model.DocumentType;

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

// product_post_document 테이블 매핑 Entity
@Entity
@Table(name = "product_post_document")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductPostDocumentEntity {

	// DB PK
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_post_document_id")
	private Long productPostDocumentId;

	// 소속 판매글 PK
	@Column(name = "product_post_id", nullable = false)
	private Long productPostId;

	// 외부 공개용 서류 UUID
	@Column(name = "product_post_document_uuid", nullable = false, unique = true, length = 36)
	private String productPostDocumentUuid;

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
	public static ProductPostDocumentEntity create(
			Long productPostId,
			String productPostDocumentUuid,
			DocumentType documentType,
			String imageUrl,
			Instant createdAt
	) {
		return ProductPostDocumentEntity.builder()
				.productPostId(productPostId)
				.productPostDocumentUuid(productPostDocumentUuid)
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
