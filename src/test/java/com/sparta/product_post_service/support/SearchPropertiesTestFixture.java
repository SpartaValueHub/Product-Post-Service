package com.sparta.product_post_service.support;

import java.util.List;
import java.util.Map;

import com.sparta.product_post_service.config.SearchProperties;

// 테스트용 SearchProperties 기본값
public final class SearchPropertiesTestFixture {

	private SearchPropertiesTestFixture() {
	}

	public static SearchProperties minimal() {
		return withPopularSeedAndRelated(List.of("롤렉스"), Map.of());
	}

	public static SearchProperties withPopularSeedAndRelated(
			List<String> popularSeed,
			Map<String, List<String>> related
	) {
		return new SearchProperties(
				5,
				50,
				2,
				"search:terms:z",
				"search:terms:h:",
				691_200_000L,
				"search:popular",
				":building",
				3_600_000L,
				60_000L,
				1.0d,
				500,
				1.0d,
				3.0d,
				1.0d,
				24,
				168,
				"Asia/Seoul",
				"search:terms:agg:",
				"search:popular:bake-lock",
				300_000L,
				"1",
				"search:cooc:z:",
				"search:cooc:sources",
				1.0d,
				"search:related:",
				2.0d,
				"search:related:bake-lock",
				"search:session:last:",
				"m:",
				"s:",
				1_800_000L,
				2,
				"search:suggest:dict",
				2000,
				"search:suggest:bake-lock",
				popularSeed,
				related
		);
	}
}
