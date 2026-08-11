package com.sparta.product_post_service.adaptor.out.mysql.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// product_post_image 테이블 매핑 Entity
@Entity
@Table(name = "product_post_image")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductPostImageEntity {

	// DB PK
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_post_image_id")
	private Long productPostImageId;

	// 소속 판매글 PK
	@Column(name = "product_post_id", nullable = false)
	private Long productPostId;

	// 외부 공개용 이미지 UUID
	@Column(name = "product_post_image_uuid", nullable = false, unique = true, length = 36)
	private String productPostImageUuid;

	// 이미지 경로
	@Column(name = "image_url", nullable = false, length = 500)
	private String imageUrl;

	// 노출 순서 (최소값이 대표/썸네일)
	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

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
	public static ProductPostImageEntity create(
			Long productPostId,
			String productPostImageUuid,
			String imageUrl,
			int sortOrder,
			Instant createdAt
	) {
		return ProductPostImageEntity.builder()
				.productPostId(productPostId)
				.productPostImageUuid(productPostImageUuid)
				.imageUrl(imageUrl)
				.sortOrder(sortOrder)
				.createdAt(createdAt)
				.updatedAt(null)
				.deletedAt(null)
				.build();
	}

	// 도메인 변경분을 Entity에 반영
	public void update(String imageUrl, int sortOrder, Instant updatedAt, Instant deletedAt) {
		this.imageUrl = imageUrl;
		this.sortOrder = sortOrder;
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
	}
}
