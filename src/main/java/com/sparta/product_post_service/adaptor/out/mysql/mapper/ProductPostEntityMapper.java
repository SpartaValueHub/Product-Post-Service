package com.sparta.product_post_service.adaptor.out.mysql.mapper;

import java.util.List;

import com.sparta.product_post_service.adaptor.out.mysql.entity.ProductPostDocumentEntity;
import com.sparta.product_post_service.adaptor.out.mysql.entity.ProductPostEntity;
import com.sparta.product_post_service.adaptor.out.mysql.entity.ProductPostImageEntity;
import com.sparta.product_post_service.domain.model.ProductPost;
import com.sparta.product_post_service.domain.model.ProductPostDocument;
import com.sparta.product_post_service.domain.model.ProductPostImage;

// ProductPost Entity <-> Domain 변환
public final class ProductPostEntityMapper {

	private ProductPostEntityMapper() {
	}

	// Entity + 자식 Entity를 Domain으로 복원
	public static ProductPost toDomain(
			ProductPostEntity entity,
			List<ProductPostImageEntity> imageEntities,
			List<ProductPostDocumentEntity> documentEntities
	) {
		List<ProductPostImage> images = imageEntities.stream()
				.map(ProductPostImageEntityMapper::toDomain)
				.toList();
		List<ProductPostDocument> documents = documentEntities.stream()
				.map(ProductPostDocumentEntityMapper::toDomain)
				.toList();

		return ProductPost.restore(
				entity.getProductPostId(),
				entity.getProductPostUuid(),
				entity.getMemberUuid(),
				entity.getCategoryUuid(),
				entity.getProductPostName(),
				entity.getConditionGrade(),
				entity.getPrice(),
				entity.getDescription(),
				entity.getTradeStatus(),
				entity.getProductPostStatus(),
				entity.getLatitude(),
				entity.getLongitude(),
				entity.getPlaceName(),
				entity.getRegionDong(),
				entity.getRegionGu(),
				entity.getBumpedAt(),
				entity.getCreatedAt(),
				entity.getUpdatedAt(),
				entity.getDeletedAt(),
				images,
				documents
		);
	}
}
