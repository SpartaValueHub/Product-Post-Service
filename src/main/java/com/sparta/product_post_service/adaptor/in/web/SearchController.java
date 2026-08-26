package com.sparta.product_post_service.adaptor.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.product_post_service.adaptor.in.web.vo.SearchTermsResponseVo;
import com.sparta.product_post_service.application.port.in.GetPopularSearchTermsUseCase;
import com.sparta.product_post_service.application.port.in.GetRelatedSearchTermsUseCase;
import com.sparta.product_post_service.application.port.in.GetSearchSuggestionsUseCase;

import lombok.RequiredArgsConstructor;

// 헤더 검색어 API (추천·연관·자동완성) — 원본 판매글 미조회
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

	// 추천 검색어 UseCase
	private final GetPopularSearchTermsUseCase getPopularSearchTermsUseCase;
	// 연관 검색어 UseCase
	private final GetRelatedSearchTermsUseCase getRelatedSearchTermsUseCase;
	// 자동완성 UseCase
	private final GetSearchSuggestionsUseCase getSearchSuggestionsUseCase;

	// 추천(인기) 검색어
	@GetMapping("/popular")
	public SearchTermsResponseVo popular() {
		List<String> terms = getPopularSearchTermsUseCase.getPopular();
		return SearchTermsResponseVo.builder().terms(terms).build();
	}

	// 연관 검색어
	@GetMapping("/related")
	public SearchTermsResponseVo related(@RequestParam String q) {
		List<String> terms = getRelatedSearchTermsUseCase.getRelated(q);
		return SearchTermsResponseVo.builder().terms(terms).build();
	}

	// 자동완성 (타이핑 prefix)
	@GetMapping("/suggestions")
	public SearchTermsResponseVo suggestions(@RequestParam String q) {
		List<String> terms = getSearchSuggestionsUseCase.getSuggestions(q);
		return SearchTermsResponseVo.builder().terms(terms).build();
	}
}
