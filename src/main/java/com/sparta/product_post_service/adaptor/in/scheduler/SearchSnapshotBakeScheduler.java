package com.sparta.product_post_service.adaptor.in.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sparta.product_post_service.application.port.in.BakePopularSearchTermsUseCase;
import com.sparta.product_post_service.application.port.in.BakeRelatedSearchTermsUseCase;

import lombok.RequiredArgsConstructor;

// 추천·연관 검색어 스냅샷 주기 베이크 트리거 (Inbound → UseCase만 호출)
@Component
@RequiredArgsConstructor
public class SearchSnapshotBakeScheduler {

	// 추천 TopN 베이크
	private final BakePopularSearchTermsUseCase bakePopularSearchTermsUseCase;
	// 연관 동시검색 베이크
	private final BakeRelatedSearchTermsUseCase bakeRelatedSearchTermsUseCase;

	// 주기·초기 지연은 YAML (product-post.search.bake-*-ms)
	@Scheduled(
			fixedDelayString = "${product-post.search.bake-interval-ms}",
			initialDelayString = "${product-post.search.bake-initial-delay-ms}"
	)
	public void bakeSearchSnapshots() {
		bakePopularSearchTermsUseCase.bake();
		bakeRelatedSearchTermsUseCase.bake();
	}
}
