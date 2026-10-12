ALTER TABLE khach_hang
    ADD diem_tich_luy DECIMAL(18,2) CONSTRAINT df_kh_diem DEFAULT 0;

ALTER TABLE khach_hang
    ADD tong_chi_tieu DECIMAL(18,2) CONSTRAINT df_kh_tong DEFAULT 0;

ALTER TABLE khach_hang
    ADD hang NVARCHAR(50);