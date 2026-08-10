-- ERD 반영: listing.negotiable 제거 (이미 컬럼이 있는 DB만 실행)
-- listing_db 선택 후 실행하세요.

USE listing_db;

-- 컬럼이 있을 때만 삭제 (MySQL 8+)
SET @col_exists := (
	SELECT COUNT(*)
	FROM information_schema.COLUMNS
	WHERE TABLE_SCHEMA = DATABASE()
	  AND TABLE_NAME = 'listing'
	  AND COLUMN_NAME = 'negotiable'
);

SET @ddl := IF(
	@col_exists > 0,
	'ALTER TABLE listing DROP COLUMN negotiable',
	'SELECT ''negotiable column already absent'' AS info'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
