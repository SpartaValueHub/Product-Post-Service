package com.sparta.listing_service.application.port.out;

import com.sparta.listing_service.domain.model.BumpQuota;

// 끌올 일일 횟수 저장 Output Port
public interface BumpQuotaSavePort {

	// 신규 일일 쿼터 저장
	BumpQuota save(BumpQuota bumpQuota);

	// 기존 일일 쿼터 변경 저장
	BumpQuota update(BumpQuota bumpQuota);
}
