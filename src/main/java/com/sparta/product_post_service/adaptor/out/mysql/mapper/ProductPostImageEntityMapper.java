package com.sparta.product_post_service.adaptor.out.mysql.mapper;

import com.sparta.product_post_service.adaptor.out.mysql.entity.ProductPostImageEntity;
import com.sparta.product_post_service.domain.model.ProductPostImage;

// ProductPostImage Entity <-> Domain 변환
public final class ProductPostImageEntityMapper {

	private ProductPostImageEntityMapper() {
	}

	// Entity를 Domain으로 복원
	public static ProductPostImage toDomain(ProductPostImageEntity entity) {
		return ProductPostImage.restore(
				entity.getProductPostImageId(),
				entity.getProductPostImageUuid(),
				entity.getProductPostId(),
				entity.getImageUrl(),
				entity.getSortOrder(),
				entity.getCreatedAt(),
				entity.getUpdatedAt(),
				entity.getDeletedAt()
		);
	}
}
