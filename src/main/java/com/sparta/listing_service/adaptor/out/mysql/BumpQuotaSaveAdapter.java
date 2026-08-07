package com.sparta.listing_service.adaptor.out.mysql;

import org.springframework.stereotype.Component;

import com.sparta.listing_service.adaptor.out.mysql.entity.BumpQuotaEntity;
import com.sparta.listing_service.adaptor.out.mysql.mapper.BumpQuotaEntityMapper;
import com.sparta.listing_service.adaptor.out.mysql.repository.BumpQuotaJpaRepository;
import com.sparta.listing_service.application.port.out.BumpQuotaSavePort;
import com.sparta.listing_service.domain.model.BumpQuota;

import lombok.RequiredArgsConstructor;

// 끌올 일일 횟수 저장 Adapter
@Component
@RequiredArgsConstructor
public class BumpQuotaSaveAdapter implements BumpQuotaSavePort {

	// 끌올 일일 횟수 JPA Repository
	private final BumpQuotaJpaRepository bumpQuotaJpaRepository;

	// 신규 일일 쿼터 저장
	@Override
	public BumpQuota save(BumpQuota bumpQuota) {
		BumpQuotaEntity saved = bumpQuotaJpaRepository.save(BumpQuotaEntity.create(
				bumpQuota.getMemberUuid(),
				bumpQuota.getQuotaDate(),
				bumpQuota.getUsedCount(),
				bumpQuota.getUpdatedAt()
		));
		return BumpQuotaEntityMapper.toDomain(saved);
	}

	// 기존 일일 쿼터 변경 저장
	@Override
	public BumpQuota update(BumpQuota bumpQuota) {
		BumpQuotaEntity entity = bumpQuotaJpaRepository.findById(bumpQuota.getBumpQuotaId())
				.orElseThrow(() -> new IllegalArgumentException("수정할 끌올 쿼터를 찾을 수 없습니다."));
		entity.update(bumpQuota.getUsedCount(), bumpQuota.getUpdatedAt());
		return BumpQuotaEntityMapper.toDomain(entity);
	}
}
