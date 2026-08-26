package com.sparta.product_post_service.adaptor.out.mysql.repository;

import java.util.Collection;
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

	// FO 목록 (keyword 없음 — JPQL, 인덱스 필터 경로)
	@Query("""
			SELECT p FROM ProductPostEntity p
			WHERE p.productPostStatus = :productPostStatus
			  AND p.deletedAt IS NULL
			  AND p.tradeStatus IN :tradeStatuses
			  AND (:hasCategories = false OR p.categoryUuid IN :categoryUuids)
			  AND (:memberUuid IS NULL OR p.memberUuid = :memberUuid)
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
			@Param("memberUuid") String memberUuid,
			@Param("minPrice") Long minPrice,
			@Param("maxPrice") Long maxPrice,
			@Param("hasGrades") boolean hasGrades,
			@Param("conditionGrades") Collection<String> conditionGrades,
			@Param("hasDocumentTypes") boolean hasDocumentTypes,
			@Param("documentTypes") Collection<DocumentType> documentTypes,
			Pageable pageable
	);

	// FO 목록 + 제목 FULLTEXT(ngram). ft_pp_name_ngram 인덱스 필요 (scripts/add-product-post-name-fulltext.sql)
	// tradeStatuses·documentTypes 는 native IN 바인딩용 enum name 문자열
	@Query(
			value = """
					SELECT * FROM product_post p
					WHERE p.product_post_status = :productPostStatus
					  AND p.deleted_at IS NULL
					  AND p.trade_status IN (:tradeStatuses)
					  AND (:hasCategories = false OR p.category_uuid IN (:categoryUuids))
					  AND (:memberUuid IS NULL OR p.member_uuid = :memberUuid)
					  AND MATCH(p.product_post_name) AGAINST (:keyword IN NATURAL LANGUAGE MODE)
					  AND (:minPrice IS NULL OR p.price >= :minPrice)
					  AND (:maxPrice IS NULL OR p.price <= :maxPrice)
					  AND (:hasGrades = false OR p.condition_grade IN (:conditionGrades))
					  AND (:hasDocumentTypes = false OR EXISTS (
					        SELECT 1 FROM product_post_document d
					        WHERE d.product_post_id = p.product_post_id
					          AND d.deleted_at IS NULL
					          AND d.document_type IN (:documentTypes)
					  ))
					ORDER BY CASE WHEN p.bumped_at IS NULL THEN p.created_at ELSE p.bumped_at END DESC
					""",
			countQuery = """
					SELECT COUNT(*) FROM product_post p
					WHERE p.product_post_status = :productPostStatus
					  AND p.deleted_at IS NULL
					  AND p.trade_status IN (:tradeStatuses)
					  AND (:hasCategories = false OR p.category_uuid IN (:categoryUuids))
					  AND (:memberUuid IS NULL OR p.member_uuid = :memberUuid)
					  AND MATCH(p.product_post_name) AGAINST (:keyword IN NATURAL LANGUAGE MODE)
					  AND (:minPrice IS NULL OR p.price >= :minPrice)
					  AND (:maxPrice IS NULL OR p.price <= :maxPrice)
					  AND (:hasGrades = false OR p.condition_grade IN (:conditionGrades))
					  AND (:hasDocumentTypes = false OR EXISTS (
					        SELECT 1 FROM product_post_document d
					        WHERE d.product_post_id = p.product_post_id
					          AND d.deleted_at IS NULL
					          AND d.document_type IN (:documentTypes)
					  ))
					""",
			nativeQuery = true
	)
	Page<ProductPostEntity> searchForListByKeyword(
			@Param("productPostStatus") String productPostStatus,
			@Param("tradeStatuses") Collection<String> tradeStatuses,
			@Param("hasCategories") boolean hasCategories,
			@Param("categoryUuids") Collection<String> categoryUuids,
			@Param("memberUuid") String memberUuid,
			@Param("keyword") String keyword,
			@Param("minPrice") Long minPrice,
			@Param("maxPrice") Long maxPrice,
			@Param("hasGrades") boolean hasGrades,
			@Param("conditionGrades") Collection<String> conditionGrades,
			@Param("hasDocumentTypes") boolean hasDocumentTypes,
			@Param("documentTypes") Collection<String> documentTypes,
			Pageable pageable
	);
}
