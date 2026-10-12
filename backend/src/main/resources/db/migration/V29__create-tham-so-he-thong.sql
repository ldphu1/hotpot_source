CREATE TABLE tham_so_he_thong (
                                  id BIGINT IDENTITY(1,1) PRIMARY KEY,
                                  ma_tham_so NVARCHAR(100) NOT NULL UNIQUE,
                                  ten_tham_so NVARCHAR(255),
                                  gia_tri NVARCHAR(255) NOT NULL,
                                  kieu_du_lieu NVARCHAR(50) NOT NULL,
                                  nhom NVARCHAR(100),
                                  mo_ta NVARCHAR(MAX),
                                  trang_thai BIT DEFAULT 1,
                                  ngay_tao DATETIME DEFAULT GETDATE(),
                                  ngay_cap_nhat DATETIME DEFAULT GETDATE()
);