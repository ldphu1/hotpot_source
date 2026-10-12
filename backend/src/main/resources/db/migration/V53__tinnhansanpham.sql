
CREATE TABLE tin_nhan_san_pham (
                                   id BIGINT IDENTITY(1,1) PRIMARY KEY,
                                   id_tin_nhan BIGINT NOT NULL,
                                   id_san_pham BIGINT NOT NULL,
                                   CONSTRAINT fk_tnsp_tin_nhan FOREIGN KEY (id_tin_nhan) REFERENCES tin_nhan(id),
                                   CONSTRAINT fk_tnsp_san_pham FOREIGN KEY (id_san_pham) REFERENCES san_pham(id)
);