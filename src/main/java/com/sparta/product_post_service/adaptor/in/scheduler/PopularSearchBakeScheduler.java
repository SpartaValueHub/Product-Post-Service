package com.sparta.product_post_service.adaptor.in.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sparta.product_post_service.application.port.in.BakePopularSearchTermsUseCase;

import lombok.RequiredArgsConstructor;

// 추천 검색어 TopN 주기 베이크 트리거 (Inbound → UseCase만 호출)
@Component
@RequiredArgsConstructor
public class PopularSearchBakeScheduler {

	// 베이크 UseCase
	private final BakePopularSearchTermsUseCase bakePopularSearchTermsUseCase;

	// 주기·초기 지연은 YAML (product-post.search.bake-*-ms)
	@Scheduled(
			fixedDelayString = "${product-post.search.bake-interval-ms}",
			initialDelayString = "${product-post.search.bake-initial-delay-ms}"
	)
	public void bakePopularSearchTerms() {
		bakePopularSearchTermsUseCase.bake();
	}
}
