INSERT INTO dot_giam_gia
(ten_dot, loai_giam, gia_tri_giam, ngay_bat_dau, ngay_ket_thuc, created_at, is_deleted, giam_toi_da)
VALUES
    (N'Giảm giá tháng 3', 0, 20, '2026-03-01', '2026-03-30', GETDATE(), 0, 50000),
    (N'Sale cuối tuần', 1, 30000, '2026-03-10', '2026-03-25', GETDATE(), 0, 0),
    (N'Flash sale tháng 4', 0, 10, '2026-04-01', '2026-04-30', GETDATE(), 0, 20000);
INSERT INTO dot_giam_gia_san_pham
(dot_giam_gia_id, san_pham_id)
VALUES
    (1, 1),
    (1, 2),
    (2, 2),
    (2, 3),
    (3, 1),
    (3, 4);