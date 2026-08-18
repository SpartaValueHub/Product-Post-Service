package com.sparta.product_post_service.application.port.in;

// 판매글 Soft Delete Input Port
public interface DeleteProductPostUseCase {

	// 판매글 Soft Delete (판매자 본인, 미삭제 글만)
	void delete(String memberUuid, String productPostUuid);
}
