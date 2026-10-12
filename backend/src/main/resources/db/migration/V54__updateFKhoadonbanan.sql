ALTER TABLE hoa_don_ban_an
    ADD CONSTRAINT fk_hdba_hoa_don
        FOREIGN KEY (id_hoa_don)
            REFERENCES hoa_don (id);

ALTER TABLE hoa_don_ban_an
    ADD CONSTRAINT fk_hdba_ban_an
        FOREIGN KEY (id_ban_an)
            REFERENCES ban_an (id);