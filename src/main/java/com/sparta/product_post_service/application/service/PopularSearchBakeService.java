package com.sparta.product_post_service.application.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.sparta.product_post_service.application.port.in.BakePopularSearchTermsUseCase;
import com.sparta.product_post_service.application.port.out.AggregateSearchTermCountsPort;
import com.sparta.product_post_service.application.port.out.SavePopularSearchTermsPort;
import com.sparta.product_post_service.application.port.out.SearchBakeLockPort;
import com.sparta.product_post_service.application.support.PopularSearchScoreCalculator;
import com.sparta.product_post_service.application.support.SearchHourBucketSupport;
import com.sparta.product_post_service.config.SearchProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 시간 버킷 가중 점수로 추천 TopN을 서빙 스냅샷에 베이크
@Slf4j
@Service
@RequiredArgsConstructor
public class PopularSearchBakeService implements BakePopularSearchTermsUseCase {

	// 시간 버킷 집계
	private final AggregateSearchTermCountsPort aggregateSearchTermCountsPort;
	// 서빙 스냅샷 저장
	private final SavePopularSearchTermsPort savePopularSearchTermsPort;
	// 다중 인스턴스 락
	private final SearchBakeLockPort searchBakeLockPort;
	// 버킷 키
	private final SearchHourBucketSupport searchHourBucketSupport;
	// 가중 점수
	private final PopularSearchScoreCalculator popularSearchScoreCalculator;
	// 검색 정책
	private final SearchProperties searchProperties;

	@Override
	public void bake() {
		if (!searchBakeLockPort.tryLock()) {
			log.debug("추천 검색어 베이크 스킵 (다른 인스턴스가 실행 중)");
			return;
		}
		try {
			int candidateLimit = searchProperties.bakeCandidateLimit();
			List<String> hours24 = searchHourBucketSupport.recentHourBucketKeys(
					searchProperties.recentWindowHours24h()
			);
			List<String> hours7d = searchHourBucketSupport.recentHourBucketKeys(
					searchProperties.recentWindowHours7d()
			);
			// 윈도우별 상위만 가져와 메모리·Redis 부하 상한
			Map<String, Double> counts24h = aggregateSearchTermCountsPort.aggregateTopCounts(
					hours24,
					candidateLimit
			);
			Map<String, Double> counts7d = aggregateSearchTermCountsPort.aggregateTopCounts(
					hours7d,
					candidateLimit
			);

			Set<String> candidates = new HashSet<>();
			candidates.addAll(counts24h.keySet());
			candidates.addAll(counts7d.keySet());

			double minScore = searchProperties.bakeMinScore();
			List<ScoredTerm> scored = new ArrayList<>();
			for (String term : candidates) {
				if (term == null || term.isBlank()) {
					continue;
				}
				double count24 = counts24h.getOrDefault(term, 0.0d);
				// 24h ⊆ 7d — 7d top 밖에 있어도 하한은 24h 건수
				double count7 = Math.max(count24, counts7d.getOrDefault(term, 0.0d));
				double score = popularSearchScoreCalculator.weightedScore(count24, count7);
				if (score < minScore) {
					continue;
				}
				scored.add(new ScoredTerm(term, score));
			}

			scored.sort(Comparator.comparingDouble(ScoredTerm::score).reversed()
					.thenComparing(ScoredTerm::term));
			int limit = searchProperties.popularLimit();
			List<String> topTerms = scored.stream()
					.limit(limit)
					.map(ScoredTerm::term)
					.toList();

			savePopularSearchTermsPort.savePopular(topTerms);
			log.info("추천 검색어 가중 베이크 완료 size={}", topTerms.size());
		} catch (RuntimeException ex) {
			log.warn("추천 검색어 베이크 실패", ex);
		} finally {
			searchBakeLockPort.unlock();
		}
	}

	// 정렬용 검색어·점수
	private record ScoredTerm(String term, double score) {
	}
}
