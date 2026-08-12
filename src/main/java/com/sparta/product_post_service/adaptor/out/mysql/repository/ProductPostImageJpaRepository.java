package com.sparta.product_post_service.adaptor.out.mysql.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.product_post_service.adaptor.out.mysql.entity.ProductPostImageEntity;

// 판매글 이미지 JPA Repository
public interface ProductPostImageJpaRepository extends JpaRepository<ProductPostImageEntity, Long> {

	// 판매글 PK로 이미지 목록 조회 (노출 순서 오름차순)
	List<ProductPostImageEntity> findByProductPostIdOrderBySortOrderAscProductPostImageIdAsc(Long productPostId);

	// 여러 판매글의 활성 이미지 (대표 썸네일 선정용)
	List<ProductPostImageEntity> findByProductPostIdInAndDeletedAtIsNullOrderByProductPostIdAscSortOrderAscProductPostImageIdAsc(
			Collection<Long> productPostIds
	);

	// 이미지 UUID로 조회
	Optional<ProductPostImageEntity> findByProductPostImageUuid(String productPostImageUuid);
}
