package com.fourspicehotpot.backend.common.enums;

public enum HinhThucCoc {

    TIEN_MAT("Tiền mặt"),
    CHUYEN_KHOAN("Chuyển khoản");

    private final String label;

    HinhThucCoc(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}