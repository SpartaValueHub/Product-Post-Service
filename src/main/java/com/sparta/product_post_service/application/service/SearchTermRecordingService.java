package com.sparta.product_post_service.application.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.sparta.product_post_service.application.port.out.RecordSearchCooccurrencePort;
import com.sparta.product_post_service.application.port.out.RecordSearchTermPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 검색어 카운터·동시검색 비동기 기록 (검색 응답을 기다리지 않음)
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchTermRecordingService {

	// 검색어 인기 카운터 Port
	private final RecordSearchTermPort recordSearchTermPort;
	// 동시검색 Port
	private final RecordSearchCooccurrencePort recordSearchCooccurrencePort;

	// 정규화된 검색어·세션을 비동기로 기록. 실패해도 호출측에 전파하지 않음
	@Async
	public void recordAsync(String normalizedTerm, String sessionKey) {
		if (normalizedTerm == null || normalizedTerm.isBlank()) {
			return;
		}
		try {
			recordSearchTermPort.record(normalizedTerm);
			if (sessionKey != null && !sessionKey.isBlank()) {
				recordSearchCooccurrencePort.recordTransition(sessionKey, normalizedTerm);
			}
		} catch (RuntimeException ex) {
			log.warn("비동기 검색어 기록 실패 term={}", normalizedTerm, ex);
		}
	}
}
