package com.sparta.listing_service.adaptor.out.mysql.entity;

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

// listing_image 테이블 매핑 Entity
@Entity
@Table(name = "listing_image")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ListingImageEntity {

	// DB PK
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "listing_image_id")
	private Long listingImageId;

	// 소속 판매글 PK
	@Column(name = "listing_id", nullable = false)
	private Long listingId;

	// 외부 공개용 이미지 UUID
	@Column(name = "listing_image_uuid", nullable = false, unique = true, length = 36)
	private String listingImageUuid;

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
	public static ListingImageEntity create(
			Long listingId,
			String listingImageUuid,
			String imageUrl,
			int sortOrder,
			Instant createdAt
	) {
		return ListingImageEntity.builder()
				.listingId(listingId)
				.listingImageUuid(listingImageUuid)
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
