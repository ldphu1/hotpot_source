
CREATE TABLE cuoc_tro_chuyen
(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang BIGINT NOT NULL,
    id_nhan_vien_xu_ly BIGINT NULL,
    ten_khach NVARCHAR(255),
    loai VARCHAR(50),
    trang_thai VARCHAR(50),
    thoi_gian_tao DATETIME DEFAULT GETDATE(),
    thoi_gian_cap_nhat DATETIME DEFAULT GETDATE(),
    thoi_gian_dong DATETIME NULL,

    CONSTRAINT fk_ctc_khach_hang
        FOREIGN KEY (id_khach_hang)
            REFERENCES khach_hang(id),

    CONSTRAINT fk_ctc_nhan_vien
        FOREIGN KEY (id_nhan_vien_xu_ly)
            REFERENCES nhan_vien(id)
);

CREATE TABLE tin_nhan
(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    id_cuoc_tro_chuyen BIGINT NOT NULL,
    id_nguoi_gui BIGINT NULL,
    loai_tin_nhan VARCHAR(50),
    noi_dung NVARCHAR(MAX),
    da_doc BIT DEFAULT 0,
    thoi_gian DATETIME DEFAULT GETDATE(),

    CONSTRAINT fk_tin_nhan_ctc
        FOREIGN KEY (id_cuoc_tro_chuyen)
            REFERENCES cuoc_tro_chuyen(id)
            ON DELETE CASCADE
);

CREATE TABLE tep_dinh_kem
(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    id_tin_nhan BIGINT NOT NULL,
    duong_dan NVARCHAR(MAX) NOT NULL,

    CONSTRAINT fk_tdk_tin_nhan
        FOREIGN KEY (id_tin_nhan)
            REFERENCES tin_nhan(id)
            ON DELETE CASCADE
);

CREATE TABLE lich_su_phan_cong
(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    id_cuoc_tro_chuyen BIGINT NOT NULL,
    id_nhan_vien BIGINT NOT NULL,
    thoi_gian DATETIME DEFAULT GETDATE(),

    CONSTRAINT fk_lspc_ctc
        FOREIGN KEY (id_cuoc_tro_chuyen)
            REFERENCES cuoc_tro_chuyen(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_lspc_nhan_vien
        FOREIGN KEY (id_nhan_vien)
            REFERENCES nhan_vien(id)
);

CREATE INDEX idx_ctc_khach_hang
ON cuoc_tro_chuyen(id_khach_hang);

CREATE INDEX idx_ctc_nhan_vien
ON cuoc_tro_chuyen(id_nhan_vien_xu_ly);

CREATE INDEX idx_tin_nhan_ctc
ON tin_nhan(id_cuoc_tro_chuyen);

CREATE INDEX idx_tdk_tin_nhan
ON tep_dinh_kem(id_tin_nhan);

CREATE INDEX idx_lspc_ctc
ON lich_su_phan_cong(id_cuoc_tro_chuyen);

CREATE INDEX idx_lspc_nhan_vien
ON lich_su_phan_cong(id_nhan_vien);

