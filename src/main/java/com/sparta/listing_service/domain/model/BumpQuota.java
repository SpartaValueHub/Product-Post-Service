package com.sparta.listing_service.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

import lombok.Getter;

// 회원 끌올 일일 횟수 도메인 (회원+날짜당 행 1개)
@Getter
public class BumpQuota {

	// DB PK (신규면 null)
	private Long bumpQuotaId;
	// 회원 UUID
	private final String memberUuid;
	// 기준일 (KST 날짜)
	private final LocalDate quotaDate;
	// 그날 사용한 끌올 횟수
	private int usedCount;
	// 수정일시
	private Instant updatedAt;

	private BumpQuota(
			Long bumpQuotaId,
			String memberUuid,
			LocalDate quotaDate,
			int usedCount,
			Instant updatedAt
	) {
		this.bumpQuotaId = bumpQuotaId;
		this.memberUuid = memberUuid;
		this.quotaDate = quotaDate;
		this.usedCount = usedCount;
		this.updatedAt = updatedAt;
	}

	// 신규 일일 쿼터 생성 (첫 끌올)
	public static BumpQuota create(String memberUuid, LocalDate quotaDate, Instant updatedAt) {
		validateMemberUuid(memberUuid);
		Objects.requireNonNull(quotaDate, "기준일은 필수입니다.");
		Objects.requireNonNull(updatedAt, "수정 시각은 필수입니다.");

		return new BumpQuota(null, memberUuid.trim(), quotaDate, 0, updatedAt);
	}

	// 저장소 조회 값으로 복원
	public static BumpQuota restore(
			Long bumpQuotaId,
			String memberUuid,
			LocalDate quotaDate,
			int usedCount,
			Instant updatedAt
	) {
		Objects.requireNonNull(bumpQuotaId, "쿼터 ID는 필수입니다.");
		validateMemberUuid(memberUuid);
		Objects.requireNonNull(quotaDate, "기준일은 필수입니다.");
		if (usedCount < 0) {
			throw new IllegalArgumentException("사용 횟수는 0 이상이어야 합니다.");
		}
		Objects.requireNonNull(updatedAt, "수정 시각은 필수입니다.");

		return new BumpQuota(bumpQuotaId, memberUuid, quotaDate, usedCount, updatedAt);
	}

	// 사용 횟수 1 증가 (한도 초과 시 예외)
	public void increaseUsage(int dailyLimit, Instant updatedAt) {
		if (dailyLimit < 1) {
			throw new IllegalArgumentException("일일 한도는 1 이상이어야 합니다.");
		}
		Objects.requireNonNull(updatedAt, "수정 시각은 필수입니다.");
		if (usedCount >= dailyLimit) {
			throw new IllegalArgumentException("일일 끌올 한도를 초과했습니다.");
		}
		this.usedCount++;
		this.updatedAt = updatedAt;
	}

	private static void validateMemberUuid(String memberUuid) {
		if (memberUuid == null || memberUuid.isBlank()) {
			throw new IllegalArgumentException("회원 UUID는 필수입니다.");
		}
	}
}
