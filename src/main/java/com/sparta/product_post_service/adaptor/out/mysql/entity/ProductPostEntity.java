package com.sparta.product_post_service.adaptor.out.mysql.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.sparta.product_post_service.domain.model.ProductPostStatus;
import com.sparta.product_post_service.domain.model.TradeStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// product_post 테이블 매핑 Entity
@Entity
@Table(
		name = "product_post",
		indexes = {
				// 마이페이지·프로필: member + tradeStatus + 공개/미삭제 조건 후 listed 정렬
				@Index(
						name = "idx_pp_member_trade_list",
						columnList = "member_uuid, trade_status, product_post_status, deleted_at, bumped_at, created_at"
				),
				// 홈 피드: tradeStatus만으로 필터할 때
				@Index(
						name = "idx_pp_trade_public_list",
						columnList = "trade_status, product_post_status, deleted_at, bumped_at, created_at"
				)
		}
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductPostEntity {

	// DB PK
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_post_id")
	private Long productPostId;

	// 외부 공개용 판매글 UUID
	@Column(name = "product_post_uuid", nullable = false, unique = true, length = 36)
	private String productPostUuid;

	// 판매자 회원 UUID
	@Column(name = "member_uuid", nullable = false, length = 36)
	private String memberUuid;

	// 리프 카테고리 UUID
	@Column(name = "category_uuid", nullable = false, length = 36)
	private String categoryUuid;

	// 상품명
	@Column(name = "product_post_name", nullable = false, length = 100)
	private String productPostName;

	// 상품 상태 등급
	@Column(name = "condition_grade", nullable = false, length = 100)
	private String conditionGrade;

	// 가격 (최소가는 product-post.policy.min-price 설정으로 검증)
	@Column(name = "price", nullable = false)
	private long price;

	// 상세 설명
	@Column(name = "description", nullable = false, columnDefinition = "TEXT")
	private String description;

	// 거래 상태
	@Enumerated(EnumType.STRING)
	@Column(name = "trade_status", nullable = false, length = 20)
	private TradeStatus tradeStatus;

	// 노출 상태
	@Enumerated(EnumType.STRING)
	@Column(name = "product_post_status", nullable = false, length = 20)
	private ProductPostStatus productPostStatus;

	// 위도
	@Column(name = "latitude", nullable = false, precision = 10, scale = 7)
	private BigDecimal latitude;

	// 경도
	@Column(name = "longitude", nullable = false, precision = 10, scale = 7)
	private BigDecimal longitude;

	// 거래 장소명
	@Column(name = "place_name", nullable = false, length = 100)
	private String placeName;

	// 거래 희망 동(읍면동, 목록 카드용, 없으면 null)
	@Column(name = "region_dong", length = 50)
	private String regionDong;

	// 거래 희망 구(시군구, 목록 카드용, 없으면 null)
	@Column(name = "region_gu", length = 50)
	private String regionGu;

	// 마지막 끌올 시각
	@Column(name = "bumped_at")
	private Instant bumpedAt;

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
	public static ProductPostEntity create(
			String productPostUuid,
			String memberUuid,
			String categoryUuid,
			String productPostName,
			String conditionGrade,
			long price,
			String description,
			TradeStatus tradeStatus,
			ProductPostStatus productPostStatus,
			BigDecimal latitude,
			BigDecimal longitude,
			String placeName,
			String regionDong,
			String regionGu,
			Instant createdAt
	) {
		return ProductPostEntity.builder()
				.productPostUuid(productPostUuid)
				.memberUuid(memberUuid)
				.categoryUuid(categoryUuid)
				.productPostName(productPostName)
				.conditionGrade(conditionGrade)
				.price(price)
				.description(description)
				.tradeStatus(tradeStatus)
				.productPostStatus(productPostStatus)
				.latitude(latitude)
				.longitude(longitude)
				.placeName(placeName)
				.regionDong(regionDong)
				.regionGu(regionGu)
				.bumpedAt(null)
				.createdAt(createdAt)
				.updatedAt(null)
				.deletedAt(null)
				.build();
	}

	// 도메인 변경분을 Entity에 반영
	public void update(
			String categoryUuid,
			String productPostName,
			String conditionGrade,
			long price,
			String description,
			TradeStatus tradeStatus,
			ProductPostStatus productPostStatus,
			BigDecimal latitude,
			BigDecimal longitude,
			String placeName,
			String regionDong,
			String regionGu,
			Instant bumpedAt,
			Instant updatedAt,
			Instant deletedAt
	) {
		this.categoryUuid = categoryUuid;
		this.productPostName = productPostName;
		this.conditionGrade = conditionGrade;
		this.price = price;
		this.description = description;
		this.tradeStatus = tradeStatus;
		this.productPostStatus = productPostStatus;
		this.latitude = latitude;
		this.longitude = longitude;
		this.placeName = placeName;
		this.regionDong = regionDong;
		this.regionGu = regionGu;
		this.bumpedAt = bumpedAt;
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
	}
}
