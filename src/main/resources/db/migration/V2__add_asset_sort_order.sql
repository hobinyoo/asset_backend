-- asset sort_order 추가
ALTER TABLE asset ADD COLUMN sort_order INTEGER;

UPDATE asset
SET sort_order = sub.row_num
    FROM (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS row_num
    FROM asset
) sub
WHERE asset.id = sub.id;

ALTER TABLE asset ALTER COLUMN sort_order SET NOT NULL;