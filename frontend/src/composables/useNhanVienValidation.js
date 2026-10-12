import { reactive } from "vue";

/* ================= REGEX ================= */
const NAME_REGEX = /^[A-Za-zÀ-ỹ]+( [A-Za-zÀ-ỹ]+)*$/;
const PHONE_REGEX = /^(03|05|07|08|09)[0-9]{8}$/;
const EMAIL_REGEX = /^[a-zA-Z0-9]+([._%+-]?[a-zA-Z0-9]+)*@(gmail|email)\.com$/;

export function useNhanVienValidation(form) {
  const errors = reactive({
    tenNhanVien: "",
    ngaySinh: "",
    gioiTinh: "",
    soDienThoai: "",
    email: "",
    chucVu: "",
  });

  const validateForm = () => {
    let valid = true;

    // reset lỗi
    Object.keys(errors).forEach((k) => (errors[k] = ""));

    /* ===== HỌ TÊN ===== */
    const tenNhanVien = form.tenNhanVien?.trim();

    if (!tenNhanVien) {
      errors.tenNhanVien = "Họ tên không được để trống";
      valid = false;
    } else if (tenNhanVien.length < 2 || tenNhanVien.length > 100) {
      errors.tenNhanVien = "Họ tên phải từ 2 đến 100 ký tự";
      valid = false;
    } else if (!NAME_REGEX.test(tenNhanVien)) {
      errors.tenNhanVien =
        "Họ tên chỉ được chứa chữ cái và 1 khoảng trắng giữa các từ";
      valid = false;
    }

    /* ===== NGÀY SINH ===== */
    if (!form.ngaySinh) {
      errors.ngaySinh = "Ngày sinh không được để trống";
      valid = false;
    } else {
      const [year, month, day] = form.ngaySinh.split("-").map(Number);
      const birth = new Date(year, month - 1, day);
      const today = new Date();

      // reset giờ để tránh lệch timezone
      birth.setHours(0, 0, 0, 0);
      today.setHours(0, 0, 0, 0);

      // check ngày tương lai
      if (birth > today) {
        errors.ngaySinh = "Ngày sinh không được lớn hơn ngày hiện tại";
        valid = false;
      } else {
        let age = today.getFullYear() - birth.getFullYear();

        const monthDiff = today.getMonth() - birth.getMonth();

        if (
          monthDiff < 0 ||
          (monthDiff === 0 && today.getDate() < birth.getDate())
        ) {
          age--;
        }

        if (age < 18 || age > 65) {
          errors.ngaySinh = "Tuổi phải từ 18 đến 65";
          valid = false;
        }
      }
    }

    /* ===== GIỚI TÍNH ===== */
    if (form.gioiTinh == null) {
      errors.gioiTinh = "Vui lòng chọn giới tính";
      valid = false;
    }

    /* ===== SỐ ĐIỆN THOẠI ===== */
    const phone = form.soDienThoai?.trim();

    if (!phone) {
      errors.soDienThoai = "Số điện thoại không được để trống";
      valid = false;
    } else if (!PHONE_REGEX.test(phone)) {
      errors.soDienThoai =
        "Số điện thoại phải gồm 10 số và bắt đầu bằng 03, 05, 07, 08 hoặc 09";
      valid = false;
    }

    /* ===== EMAIL ===== */
    const email = form.email?.trim();

    if (!email) {
      errors.email = "Email không được để trống";
      valid = false;
    } else if (!EMAIL_REGEX.test(email)) {
      errors.email = "Email phải là gmail.com hoặc email.com";
      valid = false;
    }

    /* ===== CHỨC VỤ ===== */
    if (
      form.chucVu === null ||
      form.chucVu === undefined ||
      form.chucVu === ""
    ) {
      errors.chucVu = "Vui lòng chọn chức vụ";
      valid = false;
    }

    return valid;
  };

  return { errors, validateForm };
}
