package com.sparta.product_post_service.adaptor.out.mysql.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.product_post_service.adaptor.out.mysql.entity.ProductPostEntity;

// 판매글 JPA Repository (Application에서는 Adapter를 통해서만 사용)
public interface ProductPostJpaRepository extends JpaRepository<ProductPostEntity, Long> {

	// 판매글 UUID로 조회
	Optional<ProductPostEntity> findByProductPostUuid(String productPostUuid);
}
