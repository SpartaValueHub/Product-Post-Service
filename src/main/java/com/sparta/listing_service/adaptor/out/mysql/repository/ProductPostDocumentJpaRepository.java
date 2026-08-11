package com.sparta.listing_service.adaptor.out.mysql.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.listing_service.adaptor.out.mysql.entity.ProductPostDocumentEntity;

// 판매글 서류 JPA Repository
public interface ProductPostDocumentJpaRepository extends JpaRepository<ProductPostDocumentEntity, Long> {

	// 판매글 PK로 서류 목록 조회
	List<ProductPostDocumentEntity> findByProductPostIdOrderByProductPostDocumentIdAsc(Long productPostId);

	// 서류 UUID로 조회
	Optional<ProductPostDocumentEntity> findByProductPostDocumentUuid(String productPostDocumentUuid);
}
