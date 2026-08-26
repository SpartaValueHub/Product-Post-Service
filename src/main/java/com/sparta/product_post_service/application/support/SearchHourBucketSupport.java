package com.sparta.product_post_service.application.support;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sparta.product_post_service.config.SearchProperties;

import lombok.RequiredArgsConstructor;

// 검색어 시간 버킷 키 생성 (점수 집계·기록용)
@Component
@RequiredArgsConstructor
public class SearchHourBucketSupport {

	// yyyyMMddHH
	private static final DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHH");

	// 검색 설정
	private final SearchProperties searchProperties;
	// 시각 (테스트 교체 가능)
	private final Clock clock;

	// 현재 시각 기준 hour 버킷 Redis 키
	public String currentHourBucketKey() {
		return bucketKeyAt(ZonedDateTime.now(clock.withZone(zoneId())));
	}

	// now 포함 최근 hours 개의 버킷 키 (최신 → 과거)
	public List<String> recentHourBucketKeys(int hours) {
		if (hours <= 0) {
			return List.of();
		}
		ZonedDateTime cursor = ZonedDateTime.now(clock.withZone(zoneId())).withMinute(0).withSecond(0).withNano(0);
		List<String> keys = new ArrayList<>(hours);
		for (int i = 0; i < hours; i++) {
			keys.add(bucketKeyAt(cursor.minusHours(i)));
		}
		return List.copyOf(keys);
	}

	private String bucketKeyAt(ZonedDateTime hour) {
		return searchProperties.termsHourlyZsetKeyPrefix() + HOUR_FORMAT.format(hour);
	}

	private ZoneId zoneId() {
		return ZoneId.of(searchProperties.scoreZoneId());
	}
}
