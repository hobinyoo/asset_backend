ALTER TABLE asset DROP CONSTRAINT asset_type_check;

UPDATE asset SET type = 'HOUSING'    WHERE type = 'FIXED';
UPDATE asset SET type = 'SAVINGS'    WHERE type = 'REGULAR';
UPDATE asset SET type = 'INVESTMENT' WHERE type = 'VARIABLE';

ALTER TABLE debt DROP CONSTRAINT debt_type_check;

UPDATE debt SET type = 'HOUSING'    WHERE type = 'FIXED';
UPDATE debt SET type = 'SAVINGS'    WHERE type = 'REGULAR';
UPDATE debt SET type = 'INVESTMENT' WHERE type = 'VARIABLE';