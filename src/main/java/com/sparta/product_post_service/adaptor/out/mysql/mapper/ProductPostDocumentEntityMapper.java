package com.sparta.product_post_service.adaptor.out.mysql.mapper;

import com.sparta.product_post_service.adaptor.out.mysql.entity.ProductPostDocumentEntity;
import com.sparta.product_post_service.domain.model.ProductPostDocument;

// ProductPostDocument Entity <-> Domain 변환
public final class ProductPostDocumentEntityMapper {

	private ProductPostDocumentEntityMapper() {
	}

	// Entity를 Domain으로 복원
	public static ProductPostDocument toDomain(ProductPostDocumentEntity entity) {
		return ProductPostDocument.restore(
				entity.getProductPostDocumentId(),
				entity.getProductPostDocumentUuid(),
				entity.getProductPostId(),
				entity.getDocumentType(),
				entity.getImageUrl(),
				entity.getCreatedAt(),
				entity.getUpdatedAt(),
				entity.getDeletedAt()
		);
	}
}
