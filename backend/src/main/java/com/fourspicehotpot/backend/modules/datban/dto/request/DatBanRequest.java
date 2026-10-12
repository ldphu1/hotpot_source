package com.fourspicehotpot.backend.modules.datban.dto.request;

import com.fourspicehotpot.backend.common.enums.HinhThucCoc;
import com.fourspicehotpot.backend.modules.thucdon.dto.request.CustomerMonDatTruocRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DatBanRequest {

    List<CustomerMonDatTruocRequest> monDatTruoc;
    private Integer id;
    @NotBlank(message = "Mã đặt bàn không được để trống")
    private String maDatBan;
    @NotNull(message = "Khách hàng không được để trống")
    private Long idKhachHang;
    @NotBlank(message = "Email không được để trống")
    private String email;
    private LocalDate ngaySinh;

    @NotBlank(message = "Tên khách không được để trống")
    private String tenKhachHang;
    @NotBlank(message = "Số điện thoại không được để trống")
    private String soDienThoai;
    // Chọn nhiều bàn 1 lúc
    private List<Integer> listIdBan;

    @NotNull(message = "Số người tối đa không được để trống")
    private Integer soNguoiToiDa;
    private String ghiChu;
    @NotNull(message = "Hình thức đặt không được để trống")
    private Integer hinhThucDat;
    private String trangThai;

    private HinhThucCoc hinhThucCoc;
    private BigDecimal tienCoc;

    @NotNull(message = "Thời gian đặt không được để trống")
    private LocalDateTime thoiGianDat;
    private LocalDateTime thoiGianKetThucDuKien;
}