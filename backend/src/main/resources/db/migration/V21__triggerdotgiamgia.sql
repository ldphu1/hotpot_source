CREATE TRIGGER trg_gen_ma_dot_giam_gia
    ON dot_giam_gia
    AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

UPDATE dgg
SET ma_dot = 'DGG' + RIGHT('000' + CAST(dgg.id AS VARCHAR(10)), 3)
FROM dot_giam_gia dgg
    INNER JOIN inserted i ON dgg.id = i.id;
END;