package com.fourspicehotpot.backend.common.enums;

public enum LoaiThanhToan {

    TIEN_COC(1, "Tiền cọc"),
    THANH_TOAN_MON(2, "Thanh toán"),
    HOAN_COC(3, "Hoàn cọc"),
    MAT_COC(4, "Mất cọc");

    private final int value;
    private final String label;

    LoaiThanhToan(int value, String label) {
        this.value = value;
        this.label = label;
    }


    public static LoaiThanhToan fromValue(int value) {
        for (LoaiThanhToan loai : LoaiThanhToan.values()) {
            if (loai.value == value) {
                return loai;
            }
        }
        throw new IllegalArgumentException("Invalid LoaiThanhToan: " + value);
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }
}