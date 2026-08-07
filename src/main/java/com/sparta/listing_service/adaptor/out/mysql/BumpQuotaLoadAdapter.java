package com.sparta.listing_service.adaptor.out.mysql;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.sparta.listing_service.adaptor.out.mysql.mapper.BumpQuotaEntityMapper;
import com.sparta.listing_service.adaptor.out.mysql.repository.BumpQuotaJpaRepository;
import com.sparta.listing_service.application.port.out.BumpQuotaLoadPort;
import com.sparta.listing_service.domain.model.BumpQuota;

import lombok.RequiredArgsConstructor;

// 끌올 일일 횟수 조회 Adapter
@Component
@RequiredArgsConstructor
public class BumpQuotaLoadAdapter implements BumpQuotaLoadPort {

	// 끌올 일일 횟수 JPA Repository
	private final BumpQuotaJpaRepository bumpQuotaJpaRepository;

	// 회원 UUID + 기준일로 조회
	@Override
	public Optional<BumpQuota> findByMemberUuidAndQuotaDate(String memberUuid, LocalDate quotaDate) {
		return bumpQuotaJpaRepository.findByMemberUuidAndQuotaDate(memberUuid, quotaDate)
				.map(BumpQuotaEntityMapper::toDomain);
	}
}
