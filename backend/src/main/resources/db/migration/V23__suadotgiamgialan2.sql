ALTER TABLE dot_giam_gia
DROP COLUMN trang_thai;
ALTER TABLE dot_giam_gia
    ADD giam_toi_da DECIMAL(10,2) NULL;
ALTER TABLE dot_giam_gia
ALTER COLUMN loai_giam INT NOT NULL;

ALTER TABLE dot_giam_gia
ALTER COLUMN gia_tri_giam DECIMAL(10,2) NOT NULL;

ALTER TABLE dot_giam_gia
ALTER COLUMN ngay_bat_dau DATETIME NOT NULL;

ALTER TABLE dot_giam_gia
ALTER COLUMN ngay_ket_thuc DATETIME NOT NULL;
ALTER TABLE dot_giam_gia
    ADD CONSTRAINT chk_giam_percent
        CHECK (
            loai_giam != 0
    OR (gia_tri_giam >= 0 AND gia_tri_giam <= 100)
    );
ALTER TABLE dot_giam_gia
    ADD CONSTRAINT chk_ngay
        CHECK (ngay_ket_thuc >= ngay_bat_dau);
CREATE INDEX idx_time
    ON dot_giam_gia(ngay_bat_dau, ngay_ket_thuc);

CREATE INDEX idx_sp
    ON dot_giam_gia_san_pham(san_pham_id);
ALTER TABLE dot_giam_gia_san_pham
    ADD CONSTRAINT uq_dgg_sp UNIQUE (dot_giam_gia_id, san_pham_id);