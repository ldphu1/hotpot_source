package com.fourspicehotpot.backend.modules.phieugiamgia.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThemPhieuGiamGiaRequest {

    private String maGiamGia;

    @NotBlank(message = "Tên giảm giá không được để trống")
    @Size(max = 255)
    private String tenGiamGia;

    @NotNull
    @Min(1)
    private Integer soLuong;

    @NotNull
    private Integer kieuGiamGia;

    @NotNull
    private Integer phuongThucGiamGia;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal dieuKienSuDung;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal giaTriGiam;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal giamToiDa;

    @NotNull
    private LocalDate ngayBatDau;

    @NotNull
    private LocalDate ngayKetThuc;

    private List<Long> danhSachKhachHangIds;

    @AssertTrue(message = "Ngày kết thúc phải sau ngày bắt đầu")
    public boolean isNgayHopLe() {
        if (ngayBatDau == null || ngayKetThuc == null) return true;
        return ngayKetThuc.isAfter(ngayBatDau);
    }

    @AssertTrue(message = "Giảm % phải ≤ 100")
    public boolean isGiaTriPhanTramHopLe() {
        if (phuongThucGiamGia != null && phuongThucGiamGia == 1 && giaTriGiam != null) {
            return giaTriGiam.compareTo(BigDecimal.valueOf(100)) <= 0;
        }
        return true;
    }

    @AssertTrue(message = "Giảm tiền không được lớn hơn đơn tối thiểu")
    public boolean isGiaTriTienHopLe() {
        if (phuongThucGiamGia != null && phuongThucGiamGia == 2
                && giaTriGiam != null && dieuKienSuDung != null) {
            return giaTriGiam.compareTo(dieuKienSuDung) <= 0;
        }
        return true;
    }

    @AssertTrue(message = "Voucher riêng tư phải có khách hàng")
    public boolean isKhachHangHopLe() {
        if (kieuGiamGia != null && kieuGiamGia == 2) {
            return danhSachKhachHangIds != null && !danhSachKhachHangIds.isEmpty();
        }
        return true;
    }

    @AssertTrue(message = "Thời gian voucher tối đa 6 tháng")
    public boolean isThoiGianHopLe() {
        if (ngayBatDau == null || ngayKetThuc == null) return true;
        return ngayBatDau.plusMonths(6).isAfter(ngayKetThuc);
    }
}
