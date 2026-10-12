INSERT INTO tham_so_he_thong (ma_tham_so, ten_tham_so, gia_tri, kieu_du_lieu, nhom)
VALUES (N'VAT_PERCENT', N'VAT ăn tại chỗ', N'0.1', N'DOUBLE', N'HOA_DON'),
       (N'VAT_DONG_GOI', N'VAT mang về', N'0.08', N'DOUBLE', N'HOA_DON'),
       (N'TIEN_TE', N'Đơn vị tiền', N'VND', N'STRING', N'HE_THONG'),
       (N'TI_LE_TICH_DIEM', N'Tỉ lệ tích điểm', N'0.05', N'DOUBLE', N'HOA_DON'),
       (N'HOTLINE', N'Số hotline', N'0988888888', N'STRING', N'HE_THONG'),

       (N'DAT_TRUOC_TOI_THIEU', N'Đặt trước tối thiểu', N'60', N'INT', N'DAT_BAN'),
       (N'DAT_TRUOC_TOI_DA_NGAY', N'Đặt trước tối đa ngày', N'7', N'INT', N'DAT_BAN'),
       (N'THOI_GIAN_GIU_BAN', N'Thời gian giữ bàn', N'15', N'INT', N'DAT_BAN'),
       (N'PHAN_TRAM_COC', N'Phần trăm cọc', N'0.2', N'DOUBLE', N'DAT_BAN'),
       (N'THOI_GIAN_HUY_HOAN_COC', N'Thời gian hủy hoàn cọc', N'120', N'INT', N'DAT_BAN'),
       (N'THOI_GIAN_CHO_THANH_TOAN_COC', N'Thời gian chờ thanh toán cọc', N'10', N'INT', N'DAT_BAN');