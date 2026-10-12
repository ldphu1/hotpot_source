package com.fourspicehotpot.backend.common.enums;

public enum TrangThaiDatBan {
    CHO_XAC_NHAN("CHO_XAC_NHAN", "Chờ xác nhận"),
    DA_XAC_NHAN("DA_XAC_NHAN", "Đã xác nhận"),
    DA_COC("DA_COC", "Đã cọc"),
    HUY_COC("HUY_COC", "Hủy cọc"),
    DA_CHECKIN("DA_CHECKIN", "Đã check-in"),
    DANG_PHUC_VU("DANG_PHUC_VU", "Đang phục vụ"),
    HOAN_THANH("HOAN_THANH", "Hoàn thành"),
    DA_HUY("DA_HUY", "Đã hủy"),
    CHUA_COC("CHUA_COC", "Chưa cọc"),
    KHONG_CAN_COC("KHONG_CAN_COC", "Không cần cọc"),
    KHONG_DEN("KHONG_DEN", "Khách không đến"),
    HET_HAN("HET_HAN", "Hết hạn giữ bàn");

    private final String code;
    private final String label;

    TrangThaiDatBan(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}

