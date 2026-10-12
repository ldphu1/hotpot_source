CREATE TABLE dat_ban_ban_an
(
    id_dat_ban BIGINT NOT NULL,
    id_ban_an  BIGINT NOT NULL,

    CONSTRAINT pk_dat_ban_ban_an PRIMARY KEY (id_dat_ban, id_ban_an),

    CONSTRAINT fk_datban FOREIGN KEY (id_dat_ban)
        REFERENCES dat_ban (id),

    CONSTRAINT fk_banan FOREIGN KEY (id_ban_an)
        REFERENCES ban_an (id)
);