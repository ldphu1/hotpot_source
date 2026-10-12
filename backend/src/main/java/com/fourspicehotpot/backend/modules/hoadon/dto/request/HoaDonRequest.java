package com.fourspicehotpot.backend.modules.hoadon.dto.request;

import com.fourspicehotpot.backend.common.enums.HinhThucCoc;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoaDonRequest {

    @Size(max = 50, message = "Mã hóa đơn không được vượt quá 50 ký tự")
    private String maHoaDon;

    @NotNull(message = "Loại hóa đơn không được để trống")
    private Integer loaiHoaDon;

    private LocalDateTime thoiGianThanhToan;

    @DecimalMin(value = "0", message = "Tiền cọc phải >= 0")
    private BigDecimal tienCoc;

    @DecimalMin(value = "0", message = "Tiền thuế phải >= 0")
    private BigDecimal tienThue;

    @Size(max = 100, message = "Tên người nhận tối đa 100 ký tự")
    private String tenNguoiNhan;

    @Pattern(regexp = "^(0[0-9]{9})$", message = "Số điện thoại không hợp lệ")
    private String soDienThoai;

    @Size(max = 255, message = "Địa chỉ giao tối đa 255 ký tự")
    private String diaChiGiao;

    @DecimalMin(value = "0", message = "Phí vận chuyển phải >= 0")
    private BigDecimal phiVanChuyen;

    @DecimalMin(value = "0", message = "Số tiền giảm phải >= 0")
    private BigDecimal soTienDuocGiam;

    @DecimalMin(value = "0", message = "Tổng tiền phải >= 0")
    private BigDecimal tongTien;

    @NotBlank(message = "Chưa chọn hình thức cọc")
    private HinhThucCoc hinhThucCoc;

    private Integer trangThai;
    private BigDecimal tienCocHoan;
    private BigDecimal tienCocBiMat;
    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String ghiChu;

    private Long nguoiTao;

    private LocalDateTime thoiGianMo;

    private LocalDateTime thoiGianDong;

    /* ===== RELATIONSHIP ID ===== */

    private Long idKhachHang;

    private Long idNhanVien;

    private Long idPhieuGiamGia;

    private Long idDatBan;

    private List<Long> listIdBan;

    // Bổ sung
    @DecimalMin(value = "0", message = "Tổng tiền món phải >= 0")
    private BigDecimal tongTienMon;

    @DecimalMin(value = "0", message = "VAT phải >= 0")
    private BigDecimal vatPercent;
}