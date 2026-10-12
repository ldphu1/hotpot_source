package com.fourspicehotpot.backend.common.enums;

import java.util.Arrays;

public enum TrangThaiCoc {

    CHUA_COC(0, "Chưa cọc"),
    DA_COC(1, "Đã cọc"),
    KHONG_CAN_COC(2, "Không cần cọc");

    private final int code;
    private final String label;

    TrangThaiCoc(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static TrangThaiCoc fromCode(int code) {
        return Arrays.stream(values())
                .filter(e -> e.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid code: " + code));
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return this.label;
    }
}