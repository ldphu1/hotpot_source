CREATE TRIGGER trg_auto_ma_dinh_luong
    ON dinh_luong
    AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    -- Chỉ cập nhật những dòng vừa chèn mà mã đang null
UPDATE dinh_luong
SET ma_dinh_luong = 'DL' + LEFT(REPLACE(CONVERT(VARCHAR(36), NEWID()), '-', ''), 4)
FROM dinh_luong
    INNER JOIN inserted i ON dinh_luong.id = i.id
WHERE dinh_luong.ma_dinh_luong IS NULL;
END;