package com.fourspicehotpot.backend.common.enums;

public enum TrangThaiHoaDon {
    VUA_TAO(0, "Vừa tạo"),
    CHO_COC(1, "Chờ thanh toán cọc"),
    DA_COC(2, "Đã cọc"),
    DA_XAC_NHAN(3, "Đã xác nhận"),
    CHO_XAC_NHAN(4, "Chờ xác nhận"),
    KHACH_DEN(5, "Khách đến"),
    DANG_PHUC_VU(6, "Đang phục vụ"),
    CHO_THANH_TOAN(7, "Chờ thanh toán"),
    DA_THANH_TOAN(8, "Đã thanh toán"),
    HOAN_THANH(9, "Hoàn thành"),
    DA_HUY(-1, "Đã hủy");

    private final int code;
    private final String label;

    TrangThaiHoaDon(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static TrangThaiHoaDon fromCode(int code) {
        for (TrangThaiHoaDon t : values()) {
            if (t.code == code) {
                return t;
            }
        }
        throw new IllegalArgumentException("Trạng thái không hợp lệ: " + code);
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}