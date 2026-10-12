package com.fourspicehotpot.backend.modules.setlau.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SanPhamResponse {

    private Long id;
    private String maSanPham;
    private String tenSanPham;
    private BigDecimal gia;
    private BigDecimal giaSauGiam;
    private Integer phanTramGiam;
    private BigDecimal soTienGiam;
    private String moTa;
    private Boolean trangThaiXoa;
    private Integer idDanhMuc;
    private String tenDanhMuc;
    private Long idDonVi;
    private Long idNuocLau;
    private String tenNuocLau;
    private BigDecimal giaPhuThu;
    private Integer idDinhLuong;
    private BigDecimal giaTri;
    private String tenDonVi;
    private String hinhAnh;
    private Boolean laSet;
    private List<ThanhPhanResponse> thanhPhans;

    public SanPhamResponse(Long id, String tenSanPham, BigDecimal gia) {
        this.id = id;
        this.tenSanPham = tenSanPham;
        this.gia = gia;
    }

    public SanPhamResponse(Long id, String tenSanPham, BigDecimal gia, Boolean laSet) {
        this.id = id;
        this.tenSanPham = tenSanPham;
        this.gia = gia;
        this.laSet = laSet;
    }


}