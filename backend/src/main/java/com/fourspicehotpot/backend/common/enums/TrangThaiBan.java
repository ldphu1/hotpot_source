package com.fourspicehotpot.backend.common.enums;

public enum TrangThaiBan {
    KHONG_SU_DUNG(0),
    TRONG(1),
    DA_DAT(2),
    DANG_SU_DUNG(3),
    DANG_DON(4);

    private final int value;

    TrangThaiBan(int value) {
        this.value = value;
    }

    public static TrangThaiBan fromValue(int value) {
        for (TrangThaiBan t : TrangThaiBan.values()) {
            if (t.value == value) {
                return t;
            }
        }
        throw new IllegalArgumentException("Trạng thái không hợp lệ: " + value);
    }

    public int getValue() {
        return value;
    }
}
