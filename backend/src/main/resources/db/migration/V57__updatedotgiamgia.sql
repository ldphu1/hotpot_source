ALTER TABLE dot_giam_gia
DROP CONSTRAINT chk_giam_percent;

ALTER TABLE dot_giam_gia
DROP COLUMN loai_giam;

UPDATE dbo.dot_giam_gia
SET gia_tri_giam = 10
WHERE gia_tri_giam < 0 OR gia_tri_giam > 100;

ALTER TABLE dbo.dot_giam_gia
    ADD CONSTRAINT chk_percent_range
        CHECK (gia_tri_giam BETWEEN 0 AND 100);

ALTER TABLE dot_giam_gia
DROP COLUMN giam_toi_da;