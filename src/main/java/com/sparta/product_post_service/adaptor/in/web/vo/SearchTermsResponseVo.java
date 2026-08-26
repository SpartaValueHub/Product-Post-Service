package com.sparta.product_post_service.adaptor.in.web.vo;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

// 검색어 목록 HTTP 응답
@Getter
@Builder
public class SearchTermsResponseVo {

	// 검색어 목록
	private final List<String> terms;
}
