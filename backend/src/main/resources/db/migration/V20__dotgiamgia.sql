CREATE TABLE dot_giam_gia (
                              id BIGINT PRIMARY KEY IDENTITY(1,1),
                              ma_dot VARCHAR(50),
                              ten_dot NVARCHAR(255),
                              loai_giam INT, -- 0: %, 1: tiền
                              gia_tri_giam DECIMAL(10,2),
                              ngay_bat_dau DATETIME,
                              ngay_ket_thuc DATETIME,
                              trang_thai INT, -- 0: chưa diễn ra, 1: đang diễn ra, 2: hết hạn
                              created_at DATETIME DEFAULT GETDATE()
);
CREATE TABLE dot_giam_gia_san_pham (
                                       id BIGINT PRIMARY KEY IDENTITY(1,1),
                                       dot_giam_gia_id BIGINT,
                                       san_pham_id BIGINT,
                                       FOREIGN KEY (dot_giam_gia_id) REFERENCES dot_giam_gia(id),
                                       FOREIGN KEY (san_pham_id) REFERENCES san_pham(id)
);
