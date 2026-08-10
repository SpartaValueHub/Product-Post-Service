package com.sparta.listing_service.adaptor.out.mysql.entity;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// bump_quota 테이블 매핑 Entity (회원+날짜 UNIQUE)
@Entity
@Table(
		name = "bump_quota",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_member_quota_date",
				columnNames = {"member_uuid", "quota_date"}
		)
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BumpQuotaEntity {

	// DB PK
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "bump_quota_id")
	private Long bumpQuotaId;

	// 회원 UUID
	@Column(name = "member_uuid", nullable = false, length = 36)
	private String memberUuid;

	// 기준일 (KST 날짜)
	@Column(name = "quota_date", nullable = false)
	private LocalDate quotaDate;

	// 그날 사용한 끌올 횟수
	@Column(name = "used_count", nullable = false)
	private int usedCount;

	// 수정일시
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	// 신규 저장용 Entity 생성
	public static BumpQuotaEntity create(
			String memberUuid,
			LocalDate quotaDate,
			int usedCount,
			Instant updatedAt
	) {
		return BumpQuotaEntity.builder()
				.memberUuid(memberUuid)
				.quotaDate(quotaDate)
				.usedCount(usedCount)
				.updatedAt(updatedAt)
				.build();
	}

	// 사용 횟수·수정시각 반영
	public void update(int usedCount, Instant updatedAt) {
		this.usedCount = usedCount;
		this.updatedAt = updatedAt;
	}
}
