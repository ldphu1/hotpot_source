ALTER TABLE lich_su_hoa_don
    ADD CONSTRAINT fk_lshd_nhan_vien
        FOREIGN KEY (id_nhan_vien)
            REFERENCES nhan_vien (id);