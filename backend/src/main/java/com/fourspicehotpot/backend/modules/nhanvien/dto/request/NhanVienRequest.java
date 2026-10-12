package com.fourspicehotpot.backend.modules.nhanvien.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class NhanVienRequest {

    @NotBlank(message = "Tên nhân viên không được để trống")
    @Size(min = 2, max = 100)
    @Pattern(
            regexp = "^[A-Za-zÀ-ỹ]+( [A-Za-zÀ-ỹ]+)*$",
            message = "Tên nhân viên không hợp lệ"
    )
    private String tenNhanVien;

    @NotNull(message = "Ngày sinh không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Past(message = "Ngày sinh phải nhỏ hơn ngày hiện tại")
    private LocalDate ngaySinh;

    @NotNull(message = "Vui lòng chọn giới tính")
    private Boolean gioiTinh;

    @NotBlank(message = "SĐT không được để trống")
    @Pattern(
            regexp = "^0[35789][0-9]{8}$",
            message = "SĐT không hợp lệ"
    )
    private String soDienThoai;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotNull(message = "Vui lòng chọn chức vụ")
    private Integer chucVu;

    @NotBlank(message = "Vui lòng chọn tỉnh/thành")
    private String tinhThanh;

    @NotBlank(message = "Vui lòng chọn xã/phường")
    private String xaPhuong;

    @NotBlank(message = "Vui lòng nhập địa chỉ chi tiết")
    private String diaChiChiTiet;


}
