import { reactive } from "vue";

const NAME_REGEX = /^[\p{L}]+(?:\s[\p{L}]+)*$/u;
const PHONE_REGEX = /^(0[35789])[0-9]{8}$/;
const EMAIL_REGEX = /^[a-zA-Z0-9]+([._%+-]?[a-zA-Z0-9]+)*@(gmail|email)\.com$/;

export function useKhachHangValidation(form) {
  const errors = reactive({
    hoTen: "",
    ngaySinh: "",
    soDienThoai: "",
    email: "",
  });

  const validateForm = () => {
    let valid = true;

    // Reset lỗi
    Object.keys(errors).forEach((key) => {
      errors[key] = "";
    });

    /* ===== HỌ TÊN ===== */
    const hoTen = form.hoTen?.trim() || "";

    if (!hoTen) {
      errors.hoTen = "Họ tên không được để trống";
      valid = false;
    } else if (form.hoTen !== hoTen) {
      errors.hoTen = "Không được có khoảng trắng đầu hoặc cuối";
      valid = false;
    } else if (hoTen.length < 2 || hoTen.length > 100) {
      errors.hoTen = "Họ tên phải từ 2 đến 100 ký tự";
      valid = false;
    } else if (!NAME_REGEX.test(hoTen)) {
      errors.hoTen = "Họ tên chỉ được chứa chữ cái và khoảng trắng";
      valid = false;
    }

    /* ===== NGÀY SINH ===== */
    if (!form.ngaySinh) {
      errors.ngaySinh = "Ngày sinh không được để trống";
      valid = false;
    } else {
      // Fix lỗi lệch ngày do UTC
      const [year, month, day] = form.ngaySinh.split("-");
      const birth = new Date(Number(year), Number(month) - 1, Number(day));

      const today = new Date();

      // Đưa về đầu ngày để tránh sai lệch giờ
      today.setHours(0, 0, 0, 0);
      birth.setHours(0, 0, 0, 0);

      if (birth > today) {
        errors.ngaySinh = "Ngày sinh không được lớn hơn ngày hiện tại";
        valid = false;
      } else {
        let age = today.getFullYear() - birth.getFullYear();

        const hasBirthdayPassed =
          today.getMonth() > birth.getMonth() ||
          (today.getMonth() === birth.getMonth() &&
            today.getDate() >= birth.getDate());

        if (!hasBirthdayPassed) {
          age--;
        }

        if (age < 13 || age > 85) {
          errors.ngaySinh = "Tuổi phải từ 13 đến 85";
          valid = false;
        }
      }
    }

    /* ===== SỐ ĐIỆN THOẠI ===== */
    const soDienThoai = form.soDienThoai?.trim() || "";

    if (!soDienThoai) {
      errors.soDienThoai = "Số điện thoại không được để trống";
      valid = false;
    } else if (!PHONE_REGEX.test(soDienThoai)) {
      errors.soDienThoai =
        "Số điện thoại phải gồm 10 số và bắt đầu bằng 03, 05, 07, 08 hoặc 09";
      valid = false;
    }

    /* ===== EMAIL ===== */
    const email = form.email?.trim() || "";

    if (!email) {
      errors.email = "Email không được để trống";
      valid = false;
    } else if (!EMAIL_REGEX.test(email)) {
      errors.email = "Email phải là gmail.com hoặc email.com";
      valid = false;
    }

    return valid;
  };

  return {
    errors,
    validateForm,
  };
}
