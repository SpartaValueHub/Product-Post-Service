package com.sparta.product_post_service.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sparta.product_post_service.application.port.in.BakePopularSearchTermsUseCase;
import com.sparta.product_post_service.application.port.out.LoadSearchTermRankingPort;
import com.sparta.product_post_service.application.port.out.SavePopularSearchTermsPort;
import com.sparta.product_post_service.application.port.out.SearchBakeLockPort;
import com.sparta.product_post_service.config.SearchProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 검색어 카운터 TopN을 추천 서빙 스냅샷으로 베이크
@Slf4j
@Service
@RequiredArgsConstructor
public class PopularSearchBakeService implements BakePopularSearchTermsUseCase {

	// 카운터 TopN 조회
	private final LoadSearchTermRankingPort loadSearchTermRankingPort;
	// 서빙 스냅샷 저장
	private final SavePopularSearchTermsPort savePopularSearchTermsPort;
	// 다중 인스턴스 락
	private final SearchBakeLockPort searchBakeLockPort;
	// 검색 정책
	private final SearchProperties searchProperties;

	@Override
	public void bake() {
		if (!searchBakeLockPort.tryLock()) {
			log.debug("추천 검색어 베이크 스킵 (다른 인스턴스가 실행 중)");
			return;
		}
		try {
			List<String> topTerms = loadSearchTermRankingPort.loadTopTerms(
					searchProperties.popularLimit(),
					searchProperties.bakeMinScore()
			);
			savePopularSearchTermsPort.savePopular(topTerms);
			log.info("추천 검색어 베이크 완료 size={}", topTerms.size());
		} catch (RuntimeException ex) {
			log.warn("추천 검색어 베이크 실패", ex);
		} finally {
			searchBakeLockPort.unlock();
		}
	}
}
