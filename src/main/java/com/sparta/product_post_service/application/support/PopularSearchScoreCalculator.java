package com.sparta.product_post_service.application.support;

import org.springframework.stereotype.Component;

import com.sparta.product_post_service.config.SearchProperties;

import lombok.RequiredArgsConstructor;

// 추천 검색어 가중 점수 계산
@Component
@RequiredArgsConstructor
public class PopularSearchScoreCalculator {

	// 가중치 설정
	private final SearchProperties searchProperties;

	// score = count24h * w24 + max(0, count7d - count24h) * w7Remainder
	public double weightedScore(double count24h, double count7d) {
		double recent = Math.max(0.0d, count24h);
		double olderIn7d = Math.max(0.0d, count7d - recent);
		return recent * searchProperties.weightRecent24h()
				+ olderIn7d * searchProperties.weightRecent7dRemainder();
	}
}
