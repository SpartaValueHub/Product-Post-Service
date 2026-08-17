package com.sparta.product_post_service.adaptor.out.mysql.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sparta.product_post_service.adaptor.out.mysql.entity.ProductPostEntity;
import com.sparta.product_post_service.domain.model.DocumentType;
import com.sparta.product_post_service.domain.model.ProductPostStatus;
import com.sparta.product_post_service.domain.model.TradeStatus;

// 판매글 JPA Repository (Application에서는 Adapter를 통해서만 사용)
public interface ProductPostJpaRepository extends JpaRepository<ProductPostEntity, Long> {

	// 판매글 UUID로 조회
	Optional<ProductPostEntity> findByProductPostUuid(String productPostUuid);

	// FO 목록 검색 (PUBLIC + 판매중·예약·완료, 끌올 반영 최신순)
	@Query("""
			SELECT p FROM ProductPostEntity p
			WHERE p.productPostStatus = :productPostStatus
			  AND p.deletedAt IS NULL
			  AND p.tradeStatus IN :tradeStatuses
			  AND (:hasCategories = false OR p.categoryUuid IN :categoryUuids)
			  AND (:keyword IS NULL OR p.productPostName LIKE CONCAT('%', :keyword, '%'))
			  AND (:minPrice IS NULL OR p.price >= :minPrice)
			  AND (:maxPrice IS NULL OR p.price <= :maxPrice)
			  AND (:hasGrades = false OR p.conditionGrade IN :conditionGrades)
			  AND (:hasDocumentTypes = false OR EXISTS (
			        SELECT 1 FROM ProductPostDocumentEntity d
			        WHERE d.productPostId = p.productPostId
			          AND d.deletedAt IS NULL
			          AND d.documentType IN :documentTypes
			  ))
			ORDER BY CASE WHEN p.bumpedAt IS NULL THEN p.createdAt ELSE p.bumpedAt END DESC
			""")
	Page<ProductPostEntity> searchForList(
			@Param("productPostStatus") ProductPostStatus productPostStatus,
			@Param("tradeStatuses") Collection<TradeStatus> tradeStatuses,
			@Param("hasCategories") boolean hasCategories,
			@Param("categoryUuids") Collection<String> categoryUuids,
			@Param("keyword") String keyword,
			@Param("minPrice") Long minPrice,
			@Param("maxPrice") Long maxPrice,
			@Param("hasGrades") boolean hasGrades,
			@Param("conditionGrades") Collection<String> conditionGrades,
			@Param("hasDocumentTypes") boolean hasDocumentTypes,
			@Param("documentTypes") Collection<DocumentType> documentTypes,
			Pageable pageable
	);
}
