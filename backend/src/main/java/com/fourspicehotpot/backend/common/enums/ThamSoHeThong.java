package com.fourspicehotpot.backend.common.enums;

public enum ThamSoHeThong {
    VAT_PERCENT("VAT_PERCENT"),
    VAT_DONG_GOI("VAT_DONG_GOI"),
    TIEN_TE("TIEN_TE"),
    TI_LE_TICH_DIEM("TI_LE_TICH_DIEM"),
    HOTLINE("HOTLINE"),
    DAT_TRUOC_TOI_THIEU("DAT_TRUOC_TOI_THIEU"),
    DAT_TRUOC_TOI_DA_NGAY("DAT_TRUOC_TOI_DA_NGAY"),
    THOI_GIAN_GIU_BAN("THOI_GIAN_GIU_BAN"),
    PHAN_TRAM_COC("PHAN_TRAM_COC"),
    THOI_GIAN_HUY_HOAN_COC("THOI_GIAN_HUY_HOAN_COC"),
    THOI_GIAN_CHO_THANH_TOAN_COC("THOI_GIAN_CHO_THANH_TOAN_COC"),
    TIEN_COC_MOI_NGUOI("TIEN_COC_MOI_NGUOI"),
    SO_GHE_DU_TOI_DA("SO_GHE_DU_TOI_DA"),
    THOI_GIAN_SU_DUNG_BAN_MAC_DINH("THOI_GIAN_SU_DUNG_BAN_MAC_DINH"),
    MAX_LAU("MAX_LAU"),
    MAX_DO_AN_KEM("MAX_DO_AN_KEM"),
    MAX_DO_UONG("MAX_DO_UONG"),
    MAX_DO_TRANG_MIENG("MAX_DO_TRANG_MIENG"),
    MAX_DO_NHUNG_LAU("MAX_DO_NHUNG_LAU"),
    THOI_GIAN_DON_BAN("THOI_GIAN_DON_BAN");

    private final String code;

    ThamSoHeThong(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
