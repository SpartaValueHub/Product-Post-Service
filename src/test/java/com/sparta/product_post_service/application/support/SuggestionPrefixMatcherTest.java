package com.sparta.product_post_service.application.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class SuggestionPrefixMatcherTest {

	private final SuggestionPrefixMatcher matcher = new SuggestionPrefixMatcher();

	@Test
	void matchPrefix_keepsOrderAndRespectsLimit() {
		List<String> matched = matcher.matchPrefix(
				List.of("롤렉스", "샤넬백", "롤렉스 서브마리너", "오메가"),
				"롤",
				2
		);

		assertThat(matched).containsExactly("롤렉스", "롤렉스 서브마리너");
	}
}
