CREATE TABLE gio_hang (
                          id INT IDENTITY(1,1) PRIMARY KEY,
                          id_khach_hang BIGINT NULL,
                          session_id VARCHAR(100) NULL,
                          thoi_gian_tao DATETIME2 DEFAULT GETDATE(),
                          trang_thai INT DEFAULT 0,

                          CONSTRAINT FK_gio_hang_khach_hang
                              FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id)
);