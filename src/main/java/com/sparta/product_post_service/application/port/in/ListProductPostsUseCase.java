package com.sparta.product_post_service.application.port.in;

import com.sparta.product_post_service.application.port.in.dto.ListProductPostsQuery;
import com.sparta.product_post_service.application.port.in.dto.ProductPostCardPageDto;

// 판매글 목록 조회 UseCase
public interface ListProductPostsUseCase {

	// 필터·페이징 조건으로 공개 판매글 목록을 조회한다
	ProductPostCardPageDto list(ListProductPostsQuery query);
}
