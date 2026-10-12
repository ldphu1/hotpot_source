
CREATE TRIGGER trg_auto_ma_don_vi
    ON don_vi
    AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    -- Cập nhật lại những dòng vừa chèn mà chưa có mã
UPDATE don_vi
SET ma_don_vi = 'DV' + LEFT(REPLACE(CONVERT(VARCHAR(36), NEWID()), '-', ''), 4)
FROM don_vi
    INNER JOIN inserted i ON don_vi.id = i.id
WHERE don_vi.ma_don_vi IS NULL;
END;




