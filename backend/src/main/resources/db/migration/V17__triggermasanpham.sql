CREATE TRIGGER trg_auto_ma_san_pham
    ON san_pham
    AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

UPDATE sp
SET ma_san_pham =
        'SP-' +
        SUBSTRING(chars, ABS(CHECKSUM(NEWID())) % 36 + 1, 1) +
        SUBSTRING(chars, ABS(CHECKSUM(NEWID())) % 36 + 1, 1) +
        SUBSTRING(chars, ABS(CHECKSUM(NEWID())) % 36 + 1, 1) +
        SUBSTRING(chars, ABS(CHECKSUM(NEWID())) % 36 + 1, 1)
    FROM san_pham sp
    JOIN inserted i ON sp.id = i.id
    CROSS JOIN (
    SELECT 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789' AS chars
    ) c
WHERE sp.ma_san_pham IS NULL;
END;
