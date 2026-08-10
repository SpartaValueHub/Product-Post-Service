package com.sparta.listing_service.application.port.out;

import java.time.LocalDate;
import java.util.Optional;

import com.sparta.listing_service.domain.model.BumpQuota;

// 끌올 일일 횟수 조회 Output Port
public interface BumpQuotaLoadPort {

	// 회원 UUID + 기준일로 조회
	Optional<BumpQuota> findByMemberUuidAndQuotaDate(String memberUuid, LocalDate quotaDate);
}
