package com.sparta.listing_service.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import lombok.Getter;

// 판매 등록글 도메인 (이미지·서류를 함께 다룸)
@Getter
public class Listing {

	// 상품명 최소·최대 길이
	private static final int NAME_MIN_LENGTH = 2;
	private static final int NAME_MAX_LENGTH = 100;
	// 상세 설명 최대 길이 (요구사항 상한에 맞춤)
	private static final int DESCRIPTION_MAX_LENGTH = 2000;
	// 장소명 최대 길이
	private static final int PLACE_NAME_MAX_LENGTH = 100;
	// 상품 사진 최소·최대 개수
	private static final int IMAGE_MIN_COUNT = 1;
	private static final int IMAGE_MAX_COUNT = 10;
	// 허용 상품 상태 등급
	private static final Set<String> ALLOWED_CONDITION_GRADES = Set.of("S", "A", "B", "C");

	// DB PK (신규면 null)
	private Long listingId;
	// 외부 공개용 판매글 UUID
	private final String listingUuid;
	// 판매자 회원 UUID
	private final String memberUuid;
	// 리프 카테고리 UUID
	private String categoryUuid;
	// 상품명
	private String listingName;
	// 상품 상태 등급 (S/A/B/C)
	private String conditionGrade;
	// 가격
	private long price;
	// 상세 설명 (에디터 본문)
	private String description;
	// 거래 상태
	private TradeStatus tradeStatus;
	// 노출 상태
	private ListingStatus listingStatus;
	// 가격 협상 가능 여부
	private boolean negotiable;
	// 위도
	private BigDecimal latitude;
	// 경도
	private BigDecimal longitude;
	// 거래 장소명
	private String placeName;
	// 마지막 끌올 시각
	private Instant bumpedAt;
	// 생성일시
	private final Instant createdAt;
	// 수정일시
	private Instant updatedAt;
	// 삭제일시
	private Instant deletedAt;
	// 상품 이미지 목록
	private final List<ListingImage> images = new ArrayList<>();
	// 서류 목록 (선택)
	private final List<ListingDocument> documents = new ArrayList<>();

	private Listing(
			Long listingId,
			String listingUuid,
			String memberUuid,
			String categoryUuid,
			String listingName,
			String conditionGrade,
			long price,
			String description,
			TradeStatus tradeStatus,
			ListingStatus listingStatus,
			boolean negotiable,
			BigDecimal latitude,
			BigDecimal longitude,
			String placeName,
			Instant bumpedAt,
			Instant createdAt,
			Instant updatedAt,
			Instant deletedAt,
			List<ListingImage> images,
			List<ListingDocument> documents
	) {
		this.listingId = listingId;
		this.listingUuid = listingUuid;
		this.memberUuid = memberUuid;
		this.categoryUuid = categoryUuid;
		this.listingName = listingName;
		this.conditionGrade = conditionGrade;
		this.price = price;
		this.description = description;
		this.tradeStatus = tradeStatus;
		this.listingStatus = listingStatus;
		this.negotiable = negotiable;
		this.latitude = latitude;
		this.longitude = longitude;
		this.placeName = placeName;
		this.bumpedAt = bumpedAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
		if (images != null) {
			this.images.addAll(images);
		}
		if (documents != null) {
			this.documents.addAll(documents);
		}
	}

	// 신규 판매글 생성 (등록 시 PUBLIC + SELLING)
	public static Listing create(
			String listingUuid,
			String memberUuid,
			String categoryUuid,
			String listingName,
			String conditionGrade,
			long price,
			String description,
			boolean negotiable,
			BigDecimal latitude,
			BigDecimal longitude,
			String placeName,
			List<ListingImage> images,
			List<ListingDocument> documents,
			Instant createdAt
	) {
		validateUuid(listingUuid, "판매글 UUID는 필수입니다.");
		validateUuid(memberUuid, "회원 UUID는 필수입니다.");
		validateUuid(categoryUuid, "카테고리 UUID는 필수입니다.");
		validateName(listingName);
		validateConditionGrade(conditionGrade);
		validatePrice(price);
		validateDescription(description);
		validatePlace(placeName, latitude, longitude);
		validateImages(images);
		Objects.requireNonNull(createdAt, "생성 시각은 필수입니다.");

		List<ListingDocument> safeDocuments = documents == null ? List.of() : List.copyOf(documents);

		return new Listing(
				null,
				listingUuid,
				memberUuid,
				categoryUuid.trim(),
				listingName.trim(),
				conditionGrade.trim().toUpperCase(),
				price,
				description.trim(),
				TradeStatus.SELLING,
				ListingStatus.PUBLIC,
				negotiable,
				latitude,
				longitude,
				placeName.trim(),
				null,
				createdAt,
				null,
				null,
				List.copyOf(images),
				safeDocuments
		);
	}

	// 저장소 조회 값으로 복원
	public static Listing restore(
			Long listingId,
			String listingUuid,
			String memberUuid,
			String categoryUuid,
			String listingName,
			String conditionGrade,
			long price,
			String description,
			TradeStatus tradeStatus,
			ListingStatus listingStatus,
			boolean negotiable,
			BigDecimal latitude,
			BigDecimal longitude,
			String placeName,
			Instant bumpedAt,
			Instant createdAt,
			Instant updatedAt,
			Instant deletedAt,
			List<ListingImage> images,
			List<ListingDocument> documents
	) {
		Objects.requireNonNull(listingId, "판매글 ID는 필수입니다.");
		validateUuid(listingUuid, "판매글 UUID는 필수입니다.");
		validateUuid(memberUuid, "회원 UUID는 필수입니다.");
		validateUuid(categoryUuid, "카테고리 UUID는 필수입니다.");
		Objects.requireNonNull(tradeStatus, "거래 상태는 필수입니다.");
		Objects.requireNonNull(listingStatus, "노출 상태는 필수입니다.");
		Objects.requireNonNull(createdAt, "생성 시각은 필수입니다.");

		return new Listing(
				listingId,
				listingUuid,
				memberUuid,
				categoryUuid,
				listingName,
				conditionGrade,
				price,
				description,
				tradeStatus,
				listingStatus,
				negotiable,
				latitude,
				longitude,
				placeName,
				bumpedAt,
				createdAt,
				updatedAt,
				deletedAt,
				images,
				documents
		);
	}

	// 거래 상태 변경
	public void changeTradeStatus(TradeStatus tradeStatus) {
		Objects.requireNonNull(tradeStatus, "거래 상태는 필수입니다.");
		assertNotDeleted();
		this.tradeStatus = tradeStatus;
	}

	// 숨김 처리
	public void hide() {
		assertNotDeleted();
		this.listingStatus = ListingStatus.HIDDEN;
	}

	// 공개 처리
	public void publish() {
		assertNotDeleted();
		this.listingStatus = ListingStatus.PUBLIC;
	}

	// 소프트 삭제
	public void softDelete(Instant deletedAt) {
		Objects.requireNonNull(deletedAt, "삭제 시각은 필수입니다.");
		this.listingStatus = ListingStatus.DELETED;
		this.deletedAt = deletedAt;
	}

	// 끌올 시각 반영 (쿨다운·한도 판정은 Application에서)
	public void markBumped(Instant bumpedAt) {
		Objects.requireNonNull(bumpedAt, "끌올 시각은 필수입니다.");
		assertNotDeleted();
		this.bumpedAt = bumpedAt;
	}

	// 활성 이미지만
	public List<ListingImage> activeImages() {
		return images.stream().filter(ListingImage::isActive).toList();
	}

	// 활성 서류만
	public List<ListingDocument> activeDocuments() {
		return documents.stream().filter(ListingDocument::isActive).toList();
	}

	private void assertNotDeleted() {
		if (listingStatus == ListingStatus.DELETED || deletedAt != null) {
			throw new IllegalArgumentException("삭제된 판매글은 변경할 수 없습니다.");
		}
	}

	private static void validateUuid(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(message);
		}
	}

	private static void validateName(String listingName) {
		if (listingName == null || listingName.isBlank()) {
			throw new IllegalArgumentException("상품명은 필수입니다.");
		}
		String trimmed = listingName.trim();
		if (trimmed.length() < NAME_MIN_LENGTH || trimmed.length() > NAME_MAX_LENGTH) {
			throw new IllegalArgumentException("상품명은 2~100자여야 합니다.");
		}
	}

	private static void validateConditionGrade(String conditionGrade) {
		if (conditionGrade == null || conditionGrade.isBlank()) {
			throw new IllegalArgumentException("상품 상태 등급은 필수입니다.");
		}
		if (!ALLOWED_CONDITION_GRADES.contains(conditionGrade.trim().toUpperCase())) {
			throw new IllegalArgumentException("상품 상태 등급은 S, A, B, C 중 하나여야 합니다.");
		}
	}

	private static void validatePrice(long price) {
		if (price <= 0) {
			throw new IllegalArgumentException("가격은 0보다 커야 합니다.");
		}
	}

	private static void validateDescription(String description) {
		if (description == null || description.isBlank()) {
			throw new IllegalArgumentException("상세 설명은 필수입니다.");
		}
		if (description.trim().length() > DESCRIPTION_MAX_LENGTH) {
			throw new IllegalArgumentException("상세 설명은 최대 2000자까지 가능합니다.");
		}
	}

	private static void validatePlace(String placeName, BigDecimal latitude, BigDecimal longitude) {
		if (placeName == null || placeName.isBlank()) {
			throw new IllegalArgumentException("거래 장소명은 필수입니다.");
		}
		if (placeName.trim().length() > PLACE_NAME_MAX_LENGTH) {
			throw new IllegalArgumentException("거래 장소명은 최대 100자까지 가능합니다.");
		}
		Objects.requireNonNull(latitude, "위도는 필수입니다.");
		Objects.requireNonNull(longitude, "경도는 필수입니다.");
	}

	private static void validateImages(List<ListingImage> images) {
		if (images == null || images.isEmpty()) {
			throw new IllegalArgumentException("상품 사진은 최소 1장 필요합니다.");
		}
		if (images.size() > IMAGE_MAX_COUNT) {
			throw new IllegalArgumentException("상품 사진은 최대 10장까지 가능합니다.");
		}
	}
}
