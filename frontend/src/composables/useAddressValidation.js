import { reactive } from "vue";

const addressRegex = /^[A-Za-zÀ-ỹà-ỹ0-9\s\/\.,-]+$/;

export function useAddressValidation(addressForm) {
  const addressErrors = reactive({
    tinhThanh: "",
   
    xaPhuong: "",
    diaChiChiTiet: "",
  });

  const normalize = (val) => {
    return val?.trim().replace(/\s+/g, " ");
  };

  const validateAddress = () => {
    let valid = true;

    // reset lỗi
    Object.keys(addressErrors).forEach(k => addressErrors[k] = "");

    // ===== TỈNH =====
    if (!addressForm.tinhThanh) {
      addressErrors.tinhThanh = "Vui lòng chọn tỉnh / thành phố";
      valid = false;
    }


    // ===== XÃ =====
    if (!addressForm.xaPhuong) {
      addressErrors.xaPhuong = "Vui lòng chọn phường / xã";
      valid = false;
    }

    // ===== ĐỊA CHỈ CHI TIẾT =====
    const dc = normalize(addressForm.diaChiChiTiet);

    if (!dc) {
      addressErrors.diaChiChiTiet = "Địa chỉ chi tiết không được để trống";
      valid = false;
    } 
    else if (dc.length < 5 || dc.length > 255) {
      addressErrors.diaChiChiTiet = "Địa chỉ phải từ 5 đến 255 ký tự";
      valid = false;
    } 
    else if (!addressRegex.test(dc)) {
      addressErrors.diaChiChiTiet =
        "Địa chỉ chỉ được chứa chữ, số và / , . -";
      valid = false;
    } 
    else {
      // chuẩn hóa lại form
      addressForm.diaChiChiTiet = dc;
    }

    return valid;
  };

  return { addressErrors, validateAddress };
}