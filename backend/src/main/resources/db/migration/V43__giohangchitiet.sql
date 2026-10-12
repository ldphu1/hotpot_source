CREATE TABLE gio_hang_chi_tiet (
                                   id INT IDENTITY(1,1) PRIMARY KEY,
                                   id_gio_hang INT NOT NULL,
                                   id_san_pham BIGINT NOT NULL,
                                   so_luong INT NOT NULL ,
                                   gia DECIMAL(12,2) NOT NULL ,

                                   CONSTRAINT FK_gio_hang_ct_gio_hang
                                       FOREIGN KEY (id_gio_hang) REFERENCES gio_hang(id),

                                   CONSTRAINT FK_gio_hang_ct_san_pham
                                       FOREIGN KEY (id_san_pham) REFERENCES san_pham(id)
);