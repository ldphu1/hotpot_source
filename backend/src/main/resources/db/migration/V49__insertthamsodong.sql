INSERT INTO tham_so_he_thong (ma_tham_so, ten_tham_so, gia_tri, kieu_du_lieu, nhom, mo_ta, trang_thai, ngay_tao,
                              ngay_cap_nhat)
VALUES ('SO_GHE_DU_TOI_DA', N'Số đủ ghế tối đa', '2', 'INT', 'DAT_BAN', N'--', 1, GETDATE(), GETDATE()),
       ('THOI_GIAN_SU_DUNG_BAN_MAC_DINH', N'Thời gian sử dụng bàn', '120', 'INT', 'DAT_BAN', N'Thời gian sử dụng bàn',
        1, GETDATE(), GETDATE()),
       ('MAX_LAU', N'Max lẩu', '3', 'INT', 'GIO_HANG', N'--', 1, GETDATE(), GETDATE()),
       ('MAX_DO_AN_KEM', N'Max đồ ăn kèm', '10', 'INT', 'GIO_HANG', N'--', 1, GETDATE(), GETDATE()),
       ('MAX_DO_UONG', N'Max đồ uống', '20', 'INT', 'GIO_HANG', N'--', 1, GETDATE(), GETDATE()),
       ('MAX_DO_TRANG_MIENG', N'Max đồ tráng miệng', '20', 'INT', 'GIO_HANG', N'--', 1, GETDATE(), GETDATE()),
       ('MAX_DO_NHUNG_LAU', N'Max đồ nhúng lẩu', '10', 'INT', 'GIO_HANG', N'--', 1, GETDATE(), GETDATE());