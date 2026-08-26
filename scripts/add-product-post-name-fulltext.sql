-- 판매글 제목 keyword 검색용 FULLTEXT(ngram)
-- Hibernate ddl-auto=update 는 WITH PARSER ngram FULLTEXT를 만들지 않음. prod/local 수동 적용.
-- MySQL ngram_token_size 기본 2 → 검색어 2글자 미만은 앱에서 빈 결과 처리.

-- 이미 있으면 스킵 (수동 재실행 대비)
SET @ft_exists := (
	SELECT COUNT(1)
	FROM information_schema.STATISTICS
	WHERE TABLE_SCHEMA = DATABASE()
	  AND TABLE_NAME = 'product_post'
	  AND INDEX_NAME = 'ft_pp_name_ngram'
);

SET @sql := IF(
	@ft_exists = 0,
	'ALTER TABLE product_post ADD FULLTEXT INDEX ft_pp_name_ngram (product_post_name) WITH PARSER ngram',
	'SELECT ''ft_pp_name_ngram already exists'' AS info'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
