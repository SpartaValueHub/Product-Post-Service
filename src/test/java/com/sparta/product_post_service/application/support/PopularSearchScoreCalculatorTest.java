package com.sparta.product_post_service.application.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sparta.product_post_service.support.SearchPropertiesTestFixture;

class PopularSearchScoreCalculatorTest {

	private PopularSearchScoreCalculator calculator;

	@BeforeEach
	void setUp() {
		calculator = new PopularSearchScoreCalculator(SearchPropertiesTestFixture.minimal());
	}

	@Test
	void weightedScore_applies24hAndRemainderWeights() {
		// 24h=10, 7d=15 → 10*3 + 5*1 = 35
		assertThat(calculator.weightedScore(10.0d, 15.0d)).isEqualTo(35.0d);
	}

	@Test
	void weightedScore_when7dLessThan24h_usesZeroRemainder() {
		assertThat(calculator.weightedScore(10.0d, 8.0d)).isEqualTo(30.0d);
	}
}
