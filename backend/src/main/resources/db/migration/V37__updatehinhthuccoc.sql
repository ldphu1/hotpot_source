ALTER TABLE hoa_don
    ADD CONSTRAINT CK_hoa_don_hinh_thuc_coc
        CHECK (hinh_thuc_coc IN (N'TIEN_MAT', N'CHUYEN_KHOAN'));