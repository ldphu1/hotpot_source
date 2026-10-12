export function useValidateThongTinDatBan() {
  const validateThongTinDatBan = ({
    f,
    errors,
    soChoQuaTai,
    tongGheTrong,
    getThamSo,
  }) => {
    let isValid = true;

    /////////////////////// RESET ERROR ///////////////////////
    errors.value = {
      tenKhachHang: "",
      soDienThoai: "",
      email: "",
      soNguoiToiDa: "",
      thoiGianDat: "",
    };

    /////////////////////// TÊN KHÁCH HÀNG ///////////////////////
    if (!f.tenKhachHang?.trim()) {
      errors.value.tenKhachHang = "Vui lòng nhập họ tên";
      isValid = false;
    } else if (f.tenKhachHang.trim().length < 2) {
      errors.value.tenKhachHang = "Họ tên tối thiểu 2 ký tự";
      isValid = false;
    } else if (f.tenKhachHang.trim().length > 50) {
      errors.value.tenKhachHang = "Họ tên tối đa 50 ký tự";
      isValid = false;
    }

    /////////////////////// SỐ ĐIỆN THOẠI ///////////////////////
    const phoneRegex = /^(0|\+84)[0-9]{9}$/;

    if (!f.soDienThoai?.trim()) {
      errors.value.soDienThoai = "Vui lòng nhập số điện thoại";
      isValid = false;
    } else if (f.soDienThoai.trim().length > 11) {
      errors.value.soDienThoai = "Số điện thoại quá dài";
      isValid = false;
    } else if (!phoneRegex.test(f.soDienThoai)) {
      errors.value.soDienThoai = "Số điện thoại không hợp lệ";
      isValid = false;
    }

    /////////////////////// EMAIL ///////////////////////
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!f.email?.trim()) {
      errors.value.email = "Vui lòng nhập email";
      isValid = false;
    } else if (f.email.trim().length > 100) {
      errors.value.email = "Email tối đa 100 ký tự";
      isValid = false;
    } else if (!emailRegex.test(f.email)) {
      errors.value.email = "Email không hợp lệ";
      isValid = false;
    }

    /////////////////////// SỐ NGƯỜI ///////////////////////
    if (!f.soNguoiToiDa || f.soNguoiToiDa <= 0) {
      errors.value.soNguoiToiDa = "Chưa nhập số lượng khách";
      isValid = false;
    } else if (f.soNguoiToiDa > 100) {
      errors.value.soNguoiToiDa = "Số lượng khách quá lớn";
      isValid = false;
    } else if (soChoQuaTai.value) {
      errors.value.soNguoiToiDa = `Vượt quá ${tongGheTrong.value} chỗ`;
      isValid = false;
    }

    /////////////////////// THỜI GIAN ĐẶT ///////////////////////
    if (!f.thoiGianDat) {
      errors.value.thoiGianDat = "Vui lòng chọn thời gian";
      isValid = false;
    } else {
      const now = new Date();
      const timeDat = new Date(f.thoiGianDat);

      const minMinutes = getThamSo("DAT_TRUOC_TOI_THIEU");
      const maxDays = getThamSo("DAT_TRUOC_TOI_DA_NGAY");

      const minTime = new Date(now.getTime() + minMinutes * 60000);

      if (timeDat < minTime) {
        errors.value.thoiGianDat = `Phải đặt trước ít nhất ${minMinutes} phút`;

        isValid = false;
      }

      const maxTime = new Date(now.getTime() + maxDays * 24 * 60 * 60000);

      if (timeDat > maxTime) {
        errors.value.thoiGianDat = `Chỉ được đặt trước tối đa ${maxDays} ngày`;

        isValid = false;
      }
    }

    return isValid;
  };

  return {
    validateThongTinDatBan,
  };
}
