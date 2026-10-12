package com.fourspicehotpot.backend.modules.khachhang.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class KhachHangRequest {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ tên phải từ 2 đến 100 ký tự")
    @Pattern(
            regexp = "^[A-Za-zÀ-ỹ]+( [A-Za-zÀ-ỹ]+)*$",
            message = "Họ tên chỉ chứa chữ cái và 1 khoảng trắng giữa các từ"
    )
    private String hoTen;

    @NotNull(message = "Ngày sinh không được để trống")
    @PastOrPresent(message = "Ngày sinh không được lớn hơn ngày hiện tại")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate ngaySinh;

    @NotNull(message = "Giới tính không được để trống")
    private Boolean gioiTinh;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(
            regexp = "^(03|05|07|08|09)[0-9]{8}$",
            message = "Số điện thoại không hợp lệ"
    )
    private String soDienThoai;
    private String matKhau;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotEmpty(message = "Phải có ít nhất 1 địa chỉ")
    @Valid
    private List<DiaChiKhachHangRequest> diaChiList;
}

