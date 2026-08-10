package com.sparta.listing_service.adaptor.out.mysql.mapper;

import com.sparta.listing_service.adaptor.out.mysql.entity.BumpQuotaEntity;
import com.sparta.listing_service.domain.model.BumpQuota;

// BumpQuota Entity <-> Domain 변환
public final class BumpQuotaEntityMapper {

	private BumpQuotaEntityMapper() {
	}

	// Entity를 Domain으로 복원
	public static BumpQuota toDomain(BumpQuotaEntity entity) {
		return BumpQuota.restore(
				entity.getBumpQuotaId(),
				entity.getMemberUuid(),
				entity.getQuotaDate(),
				entity.getUsedCount(),
				entity.getUpdatedAt()
		);
	}
}
