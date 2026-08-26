package com.sparta.product_post_service.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.sparta.product_post_service.application.port.in.BakeRelatedSearchTermsUseCase;
import com.sparta.product_post_service.application.port.out.LoadSearchCooccurrencePort;
import com.sparta.product_post_service.application.port.out.RelatedSearchBakeLockPort;
import com.sparta.product_post_service.application.port.out.SaveRelatedSearchTermsPort;
import com.sparta.product_post_service.config.SearchProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 동시검색 카운터 → 연관 서빙 스냅샷 베이크
@Slf4j
@Service
@RequiredArgsConstructor
public class RelatedSearchBakeService implements BakeRelatedSearchTermsUseCase {

	// 동시검색 조회
	private final LoadSearchCooccurrencePort loadSearchCooccurrencePort;
	// 연관 서빙 저장
	private final SaveRelatedSearchTermsPort saveRelatedSearchTermsPort;
	// 분산 락
	private final RelatedSearchBakeLockPort relatedSearchBakeLockPort;
	// 검색 정책
	private final SearchProperties searchProperties;

	@Override
	public void bake() {
		if (!relatedSearchBakeLockPort.tryLock()) {
			log.debug("연관 검색어 베이크 스킵 (다른 인스턴스가 실행 중)");
			return;
		}
		try {
			Set<String> sources = loadSearchCooccurrencePort.loadSourceTerms();
			int limit = searchProperties.popularLimit();
			double minScore = searchProperties.relatedBakeMinScore();
			int baked = 0;
			for (String fromTerm : sources) {
				if (fromTerm == null || fromTerm.isBlank()) {
					continue;
				}
				List<String> top = loadSearchCooccurrencePort.loadTopRelated(fromTerm, limit + 1, minScore);
				List<String> related = new ArrayList<>();
				for (String term : top) {
					if (term == null || term.equals(fromTerm) || related.contains(term)) {
						continue;
					}
					related.add(term);
					if (related.size() >= limit) {
						break;
					}
				}
				saveRelatedSearchTermsPort.saveRelated(fromTerm, related);
				baked++;
			}
			log.info("연관 검색어 베이크 완료 sources={}", baked);
		} catch (RuntimeException ex) {
			log.warn("연관 검색어 베이크 실패", ex);
		} finally {
			relatedSearchBakeLockPort.unlock();
		}
	}
}
