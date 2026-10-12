
CREATE TRIGGER trg_auto_ma_thanh_toan
    ON thanh_toan
    AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

UPDATE tt
SET ma_thanh_toan =
    'TT-' +
    RIGHT(
    CAST(ABS(CHECKSUM(NEWID())) AS VARCHAR(20)) +
    CAST(ABS(CHECKSUM(NEWID())) AS VARCHAR(20)),
    10
    )
FROM thanh_toan tt
    INNER JOIN inserted i ON tt.id = i.id
WHERE i.ma_thanh_toan IS NULL;
END;

