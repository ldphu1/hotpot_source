<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import flatpickr from "flatpickr";
import "flatpickr/dist/flatpickr.min.css";
import { Vietnamese } from "flatpickr/dist/l10n/vn.js";

import { useUtils } from "@/utils/helper";
import {
  getAllBan,
  getAllThamSoCustomer,
} from "@/api/customer/datbanonline/datBanOnlineApi";
import { kiemTraBanTrong } from "@/api/customer/datbanonline/datBanOnlineApi";
const router = useRouter();
const { notify, nprogress } = useUtils();
const getSavedForm = () => {
  const saved = localStorage.getItem("thong-tin-khach-hang");
  if (saved) {
    try {
      const parsed = JSON.parse(saved);
      return {
        tenKhachHang: parsed.tenKhachHang || "",
        soDienThoai: parsed.soDienThoai || "",
        email: parsed.email || "",
        thoiGianDat: parsed.thoiGianDat || "",
        soNguoiToiDa: parsed.soNguoiToiDa || "",
        ghiChu: parsed.ghiChu || "",
        monDatTruoc: parsed.monDatTruoc || [],
      };
    } catch (e) {
      console.error(e);
    }
  }
  return {
    tenKhachHang: "",
    soDienThoai: "",
    email: "",
    thoiGianDat: "",
    soNguoiToiDa: "",
    ghiChu: "",
    monDatTruoc: [],
  };
};
const formDatBan = ref(getSavedForm());
// ========================
// CHECK BÀN TRỐNG
// ========================
const dangKiemTraBan = ref(false);
const khongCoBanTrong = ref(false);
let debounceTimer = null;

const kiemTraBanKhiThayDoi = () => {
  clearTimeout(debounceTimer);
  debounceTimer = setTimeout(kiemTraBanTrongApi, 400);
};

const kiemTraBanTrongApi = async () => {
  const f = formDatBan.value;
  if (!f.soNguoiToiDa || f.soNguoiToiDa <= 0 || !f.thoiGianDat) {
    khongCoBanTrong.value = false;
    return;
  }

  dangKiemTraBan.value = true;
  try {
    const payload = {
      soNguoiToiDa: f.soNguoiToiDa,
      thoiGianDat: formatDateTime(f.thoiGianDat),
    };
    console.log("[kiemTraBanTrong] Request:", payload);

    const res = await kiemTraBanTrong(payload);

    console.log("[kiemTraBanTrong] Response:", res.data);

    khongCoBanTrong.value = !res.data.conBan;
  } catch (error) {
    console.error("Lỗi kiểm tra bàn trống:", error);
    khongCoBanTrong.value = false;
  } finally {
    dangKiemTraBan.value = false;
  }
};

watch(
  () => [formDatBan.value.soNguoiToiDa, formDatBan.value.thoiGianDat],
  kiemTraBanKhiThayDoi,
);

// ========================
// STATE
// ========================
const isChecking = ref(false);
const datetimeRef = ref(null);
const tongGheTrong = ref(0);
const danhSachThamSo = ref([]);
let dateTimePickerInstance = null;

const errors = ref({
  tenKhachHang: "",
  soDienThoai: "",
  email: "",
  soNguoiToiDa: "",
  thoiGianDat: "",
});

// ========================
// COMPUTED
// ========================
const soChoQuaTai = computed(
  () =>
    formDatBan.value.soNguoiToiDa > 0 &&
    formDatBan.value.soNguoiToiDa > tongGheTrong.value,
);

// ========================
// THAM SỐ ĐỘNG
// ========================
const loadThamSo = async () => {
  try {
    const res = await getAllThamSoCustomer();
    danhSachThamSo.value = res.data;
  } catch (error) {
    console.error("Lỗi load tham số:", error);
  }
};

const getThamSo = (ma) => {
  const item = danhSachThamSo.value.find((t) => t.maThamSo === ma);
  return item ? parseFloat(item.giaTri) : 0;
};

// ========================
// BÀN / SỨC CHỨA
// ========================
const loadBan = async () => {
  try {
    const res = await getAllBan();
    tongGheTrong.value = res.data
      .filter((b) => b.trangThai === 1)
      .reduce((sum, b) => sum + (b.sucChuaToiDa || 0), 0);
  } catch (error) {
    console.error("Lỗi load bàn:", error);
  }
};

// ========================
// AUTO FILL KHI ĐÃ LOGIN
// ========================
const autoFillCustomerInfo = () => {
  const token = localStorage.getItem("token");
  if (!token) return;
  formDatBan.value.tenKhachHang = localStorage.getItem("name") || "";
  formDatBan.value.email = localStorage.getItem("email") || "";
  formDatBan.value.soDienThoai = localStorage.getItem("soDienThoai") || "";
};

// ========================
// FLATPICKR
// ========================
const getRoundedNow = () => {
  const now = new Date();
  now.setMinutes(Math.ceil(now.getMinutes() / 5) * 5, 0, 0);
  return now;
};

const initDateTimePicker = () => {
  if (dateTimePickerInstance) dateTimePickerInstance.destroy();

  dateTimePickerInstance = flatpickr(datetimeRef.value, {
    enableTime: true,
    dateFormat: "d/m/Y H:i",
    time_24hr: true,
    minuteIncrement: 5,
    locale: Vietnamese,
    minDate: "today",
    onChange: (selectedDates) => {
      if (selectedDates.length > 0) {
        formDatBan.value.thoiGianDat = selectedDates[0];
      }
    },
  });

  if (!formDatBan.value.thoiGianDat) {
    // Set mặc định = bây giờ
    const defaultTime = getRoundedNow();
    formDatBan.value.thoiGianDat = defaultTime;
    dateTimePickerInstance.setDate(defaultTime, true);
  } else {
    // Sử dụng thời gian đã lưu
    dateTimePickerInstance.setDate(formDatBan.value.thoiGianDat, true);
  }
};

// ========================
// INTERSECTION OBSERVER
// ========================
const initRevealAnimation = () => {
  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) entry.target.classList.add("active");
      });
    },
    { threshold: 0.1 },
  );
  document.querySelectorAll(".reveal").forEach((el) => observer.observe(el));
};

// ========================
// VALIDATE
// ========================
const clearErrors = () => {
  Object.keys(errors.value).forEach((k) => (errors.value[k] = ""));
};

const validateForm = () => {
  clearErrors();
  const f = formDatBan.value;
  let isValid = true;

  // Họ tên
  if (!f.tenKhachHang?.trim()) {
    errors.value.tenKhachHang = "Vui lòng nhập họ tên";
    isValid = false;
  }

  // SĐT
  const phoneRegex = /^(0[35789]|\+84[35789])[0-9]{8}$/;
  if (!f.soDienThoai?.trim()) {
    errors.value.soDienThoai = "Vui lòng nhập số điện thoại";
    isValid = false;
  } else if (!phoneRegex.test(f.soDienThoai)) {
    errors.value.soDienThoai = "Số điện thoại không hợp lệ";
    isValid = false;
  }

  // Email
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!f.email?.trim()) {
    errors.value.email = "Vui lòng nhập email";
    isValid = false;
  } else if (!emailRegex.test(f.email)) {
    errors.value.email = "Email không hợp lệ";
    isValid = false;
  }

  // Số khách
  if (!f.soNguoiToiDa || f.soNguoiToiDa <= 0) {
    errors.value.soNguoiToiDa = "Chưa nhập số lượng khách";
    isValid = false;
  } else if (soChoQuaTai.value) {
    errors.value.soNguoiToiDa = `Vượt quá ${tongGheTrong.value} chỗ`;
    isValid = false;
  } else if (khongCoBanTrong.value) {
    errors.value.soNguoiToiDa = "Không còn bàn trống phù hợp với thời gian này";
    isValid = false;
  }

  // Thời gian
  if (!f.thoiGianDat) {
    errors.value.thoiGianDat = "Vui lòng chọn thời gian";
    isValid = false;
  } else {
    const now = new Date();
    const timeDat = new Date(f.thoiGianDat);
    const minMinutes = getThamSo("DAT_TRUOC_TOI_THIEU");
    const maxDays = getThamSo("DAT_TRUOC_TOI_DA_NGAY");

    if (
      minMinutes > 0 &&
      timeDat < new Date(now.getTime() + minMinutes * 60000)
    ) {
      errors.value.thoiGianDat = `Phải đặt trước ít nhất ${minMinutes} phút`;
      isValid = false;
    } else if (
      maxDays > 0 &&
      timeDat > new Date(now.getTime() + maxDays * 24 * 60 * 60000)
    ) {
      errors.value.thoiGianDat = `Chỉ được đặt trước tối đa ${maxDays} ngày`;
      isValid = false;
    }
  }

  return isValid;
};

// ========================
// FORMAT
// ========================
const formatDateTime = (date) => {
  if (!date) return null;
  const d = new Date(date);
  const yyyy = d.getFullYear();
  const MM = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  const HH = String(d.getHours()).padStart(2, "0");
  const mm = String(d.getMinutes()).padStart(2, "0");
  return `${yyyy}-${MM}-${dd}T${HH}:${mm}:00`;
};

// ========================
// SUBMIT
// ========================
const handleKiemTraDatBan = async () => {
  if (isChecking.value) return;
  if (!validateForm()) return;

  isChecking.value = true;
  try {
    const payload = {
      ...formDatBan.value,
      thoiGianDat: formatDateTime(formDatBan.value.thoiGianDat),
    };

    localStorage.setItem("thong-tin-khach-hang", JSON.stringify(payload));
    router.push({ path: "/dat-ban", state: { form: payload } });
  } catch (e) {
    console.error("Lỗi kiểm tra đặt bàn:", e);
    isChecking.value = false;
  }
};

const handleAuthChanged = () => {
  autoFillCustomerInfo();
};

// ========================
// MOUNTED
// ========================
onMounted(async () => {
  initDateTimePicker(); // 1. Flatpickr + set giờ mặc định
  autoFillCustomerInfo(); // 2. Điền thông tin nếu đã login
  initRevealAnimation(); // 3. Animation scroll
  await Promise.all([loadThamSo(), loadBan()]); // 4. Load tham số + bàn (chờ xong trước khi cho submit)
  window.addEventListener("auth-changed", handleAuthChanged);
});

onUnmounted(() => {
  window.removeEventListener("auth-changed", handleAuthChanged);
});
</script>

<template>
  <main class="booking-page py-5">
    <div class="container">
      <header class="text-center mb-5 reveal active">
        <span class="sub-title">exclusive dining</span>
        <h2 class="section-title">Đặt Bàn Thượng Hạng</h2>
        <div class="luxury-line"></div>
      </header>

      <div class="row g-5 justify-content-center">
        <div class="col-lg-8 reveal">
          <div class="luxury-card p-4 p-md-5">
            <h5 class="form-section-label">thông tin đặt bàn</h5>
            <div class="booking-grid mb-5">
              <div class="form-group full-width">
                <label class="label-black">
                  Họ tên <span class="required">*</span>
                </label>
                <input
                  v-model="formDatBan.tenKhachHang"
                  type="text"
                  placeholder="Nhập họ tên..."
                  class="luxury-input"
                  @input="errors.tenKhachHang = ''"
                />
                <transition name="shake">
                  <p v-if="errors.tenKhachHang" class="error-hint">
                    <i class="fas fa-exclamation-circle"></i>
                    {{ errors.tenKhachHang }}
                  </p>
                </transition>
              </div>
              <div class="form-group">
                <label class="label-black">
                  Số điện thoại <span class="required">*</span>
                </label>
                <input
                  v-model="formDatBan.soDienThoai"
                  type="tel"
                  placeholder="090 • • • • • • •"
                  class="luxury-input"
                  @input="errors.soDienThoai = ''"
                />
                <transition name="shake">
                  <p v-if="errors.soDienThoai" class="error-hint">
                    <i class="fas fa-exclamation-circle"></i>
                    {{ errors.soDienThoai }}
                  </p>
                </transition>
              </div>
              <div class="form-group">
                <label class="label-black">
                  Địa chỉ email <span class="required">*</span>
                </label>
                <input
                  v-model="formDatBan.email"
                  type="email"
                  placeholder="khachhang@luxury.com"
                  class="luxury-input"
                  @input="errors.email = ''"
                />
                <transition name="shake">
                  <p v-if="errors.email" class="error-hint">
                    <i class="fas fa-exclamation-circle"></i>
                    {{ errors.email }}
                  </p>
                </transition>
              </div>
            </div>

            <h5 class="form-section-label">Chi tiết buổi tiệc</h5>
            <div class="booking-grid mb-5">
              <div class="form-group">
                <label class="label-black">
                  Số lượng khách<span class="required">*</span>
                </label>
                <input
                  v-model.number="formDatBan.soNguoiToiDa"
                  type="number"
                  value="1"
                  min="1"
                  class="luxury-input"
                  @input="errors.soNguoiToiDa = ''"
                />
                <transition name="shake">
                  <p v-if="soChoQuaTai" class="error-hint">
                    <i class="fas fa-exclamation-circle"></i>
                    Vượt quá sức chứa hiện tại ({{ tongGheTrong }}
                    chỗ trống)
                  </p>
                </transition>
                <transition name="shake">
                  <p v-if="errors.soNguoiToiDa" class="error-hint">
                    <i class="fas fa-exclamation-circle"></i>
                    {{ errors.soNguoiToiDa }}
                  </p>
                </transition>
                <transition name="shake">
                  <p v-if="khongCoBanTrong" class="error-hint">
                    <i class="fas fa-exclamation-circle"></i>
                    Không còn bàn trống phù hợp với
                    {{ formDatBan.soNguoiToiDa }} khách vào thời điểm này
                  </p>
                </transition>
                <p
                  v-if="dangKiemTraBan"
                  class="text-muted"
                  style="font-size: 0.8rem"
                >
                  Đang kiểm tra bàn trống...
                </p>
              </div>

              <div class="form-group">
                <label class="label-black">
                  Thời gian phục vụ <span class="required">*</span>
                </label>
                <input
                  ref="datetimeRef"
                  type="text"
                  placeholder="Chọn ngày & giờ"
                  class="luxury-input"
                  :class="{ 'input-invalid': errors.thoiGianDat }"
                  @input="errors.thoiGianDat = ''"
                />
                <transition name="shake">
                  <p v-if="errors.thoiGianDat" class="error-hint">
                    <i class="fas fa-exclamation-circle"></i>
                    {{ errors.thoiGianDat }}
                  </p>
                </transition>
              </div>
              <div class="form-group full-width">
                <label>Ghi chú đặc biệt</label>
                <textarea
                  v-model="formDatBan.ghiChu"
                  rows="2"
                  placeholder="Ví dụ: bàn gần cửa sổ, dị ứng thực phẩm..."
                  class="luxury-input"
                ></textarea>
              </div>
            </div>
            <div class="text-center mt-4">
              <button
                class="btn-check-booking-luxury"
                :class="{ 'is-loading': isChecking }"
                :disabled="isChecking || !validateForm"
                @click="handleKiemTraDatBan"
              >
                <span class="btn-content" v-if="!isChecking"
                  >KIỂM TRA ĐẶT BÀN</span
                >
                <span class="btn-loading" v-else>
                  <span class="spinner-luxury"></span>
                  ĐANG KIỂM TRA...
                </span>

                <div class="light-sweep"></div>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
/* /////////////// VALIDATE//////////////// */
.error-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #e63946;
  font-size: 0.85rem;
  margin-top: 4px;
  padding: 6px 12px;
  background-color: #fff1f2;
  border-radius: 6px;

  animation: slideDown 0.3s ease-out;
}

/* @keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
.shake-enter-active {
  animation: shake 0.4s cubic-bezier(0.36, 0.07, 0.19, 0.97) both;
}

@keyframes shake {
  10%,
  90% {
    transform: translate3d(-1px, 0, 0);
  }
  20%,
  80% {
    transform: translate3d(2px, 0, 0);
  }
  30%,
  50%,
  70% {
    transform: translate3d(-3px, 0, 0);
  }
  40%,
  60% {
    transform: translate3d(3px, 0, 0);
  }
} */

/* ///////////// NÚT ĐẶT BÀN //////////////////// */
.btn-check-booking-luxury {
  position: relative;
  background: linear-gradient(135deg, #ce494d 0%, #ec2229 100%);
  color: #fff;
  border: 1px solid #d4af37;
  padding: 16px 45px;
  font-family: "Playfair Display", serif;
  font-weight: 800;
  font-size: 0.9rem;
  letter-spacing: 3px;
  text-transform: uppercase;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.23, 1, 0.32, 1);
  box-shadow: 0 10px 20px rgba(163, 29, 33, 0.2);
  cursor: pointer;
}

.btn-check-booking-luxury:hover {
  transform: translateY(-3px) scale(1.02);
  box-shadow: 0 15px 30px rgba(163, 29, 33, 0.4);
  border-color: #fff;
}

.btn-check-booking-luxury:hover .light-sweep {
  left: 150%;
}
.light-sweep {
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  transition: 0.5s;
}

.btn-check-booking-luxury.is-loading {
  background: #dd3137;
  padding-left: 55px;
  cursor: not-allowed;
  opacity: 0.9;
}

.btn-loading {
  display: flex;
  align-items: center;
  gap: 12px;
}

.spinner-luxury {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(246, 79, 79, 0.3);
  border-top-color: #ffc505;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* //////////////// HẾT ///////////////// */
.payment-expand {
  margin-top: 12px;
  padding: 12px;
  border: 1px solid #eee;
  border-radius: 12px;
  background: #fafafa;
  animation: slideDown 0.25s ease;
}

.payment-warning {
  font-size: 13px;
  color: #ec6969;
  margin-bottom: 10px;
  font-weight: 600;
}

.payment-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.payment-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border: 1.5px solid #ddd;
  border-radius: 10px;
  cursor: pointer;
  transition: 0.25s;
  background: white;
}

.payment-item img {
  width: 36px;
  height: 36px;
}

.payment-item.active {
  border-color: #ef5757;
  background: #fadbdb;
}

/* momo riêng */
.payment-item.momo.active {
  border-color: #d82d8b;
  background: #ffe6f2;
}

/* animation */
@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
.disabled {
  opacity: 0.4;
  pointer-events: none;
  filter: grayscale(40%);
}
.receipt-inner {
  display: flex;
  flex-direction: column;
}

.empty-receipt {
  flex: 1;
}
.deposit-row {
  font-size: 14px;
  color: #c0392b;
  font-weight: 600;
}

.deposit-amount {
  color: #e74c3c;
  font-weight: 800;
}
.receipt-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.receipt-food-img {
  width: 45px;
  height: 45px;
  overflow: hidden;
  border-radius: 6px;
}

.receipt-food-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.deposit-warning {
  font-size: 13px;
  color: #c0392b;
  margin-bottom: 12px;
}
.menu-filter-box {
  position: sticky;
  top: 0;
  background: white;
  z-index: 5;
  padding-bottom: 10px;
}

.menu-food-scroll {
  max-height: 420px;
  overflow-y: auto;
  padding-right: 6px;
}

.menu-food-scroll::-webkit-scrollbar {
  width: 6px;
}

.menu-food-scroll::-webkit-scrollbar-thumb {
  background: #c5a059;
  border-radius: 10px;
}

/* scroll đẹp hơn */
.menu-selection::-webkit-scrollbar {
  width: 6px;
}
.menu-selection {
  padding: 20px;
  border: 1px solid red;
}

.menu-food-scroll {
  max-height: 420px;
  overflow-y: auto;
}

.menu-selection::-webkit-scrollbar-thumb {
  background: #c5a059;
  border-radius: 10px;
}
.luxury-input[type="date"]::-webkit-datetime-edit {
  color: transparent;
}

.luxury-input[type="date"]:focus::-webkit-datetime-edit,
.luxury-input[type="date"]:valid::-webkit-datetime-edit {
  color: #1a1a1a;
}
.btn-open-menu {
  background: transparent;
  border: 1.5px solid #c5a059;
  color: #af0911;
  padding: 10px 28px;
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 2px;
  transition: 0.25s;
}

.btn-open-menu:hover {
  background: #e46e6e;
  color: #111;
}

.booking-page {
  background-color: #fbfbfb;
  font-family: var(--font-family);
  color: #1a1a1a;
  min-height: 100vh;
}
/* Typography & Headers */
.sub-title {
  text-transform: uppercase;
  letter-spacing: 6px;
  font-size: 0.7rem;
  color: #c5a059;
  font-weight: 700;
  display: block;
  margin-bottom: 10px;
}
.section-title {
  font-family: "Playfair Display", serif;
  font-size: 2.5rem;
  font-weight: 700;
  color: #e86565;
}
.luxury-line {
  width: 40px;
  height: 2px;
  background: #c5a059;
  margin: 20px auto;
}

/* Card Styles */
.luxury-card {
  background: #ffffff;
  border: 2px solid #f0f0f0;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.02);
}
.form-section-label {
  font-family: "Playfair Display", serif;
  font-size: 1.1rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 25px;
  color: #1a1a1a;
  border-left: 3px solid #c5a059;
  padding-left: 15px;
}

/* Grid & Inputs */
.booking-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}
.full-width {
  grid-column: span 2;
}

.form-group label {
  display: block;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.5px;
  color: #1a1a1a;
  margin-bottom: 6px;
}
.luxury-input {
  width: 100%;
  border: none;
  border-bottom: 2px solid #e0e0e0;
  padding: 10px 0;
  background: transparent;
  font-size: 0.95rem;
  transition: 0.3s;
  border-radius: 0;
}
.luxury-input:focus {
  outline: none;
  border-color: #c5a059;
}
.luxury-input::placeholder {
  color: #b8b8b8;
  font-size: 0.85rem;
}

/* Food Gallery */
/* food list horizontal */
.food-row-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.food-row-bottom {
  margin-top: auto;
  display: flex;
  justify-content: flex-end;
}
.food-gallery {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* mỗi món */

.food-row {
  display: flex;
  gap: 18px;
  padding: 16px;
  border: 1px solid #ee8080;
  transition: 0.3s;
  background: white;
  border-radius: 6px;
}

.food-row:hover {
  border-color: #c5a059;
  transform: translateY(-2px);
}

/* ảnh món */

.food-img-box {
  width: 80px;
  height: 80px;
  flex-shrink: 0;
  overflow: hidden;
}

.food-img-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* info */

.food-row-info {
  flex: 1;
}

.food-row-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.food-name {
  font-size: 18px;
  font-weight: 1000;
  text-transform: uppercase;
  margin: 0;
  color: #111;
}
.price-tag {
  font-weight: 800;
  font-size: 1rem;
  color: #c5a059;
}

.food-desc {
  font-size: 0.85rem;
  color: #666;
  font-weight: 500;
  margin: 6px 0 10px;
}
/* nút thêm */

.btn-add-dish {
  background: transparent;
  border: 1px solid #c5a059;
  color: #ef3838;
  padding: 5px 14px;
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  transition: 0.25s;
}

.btn-add-dish:hover {
  background: #f47676;
  color: #111;
}

/* Receipt Card (Right Sidebar) */
.receipt-card {
  background: #ffffff;
  border: 2px solid #f0f0f0;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  color: #1a1a1a;
  top: 30px;
}
.text-gold {
  color: #c5a059;
  font-size: 0.6rem;
  letter-spacing: 2px;
  text-transform: uppercase;
}
.receipt-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #f0f0f0;
}
.item-title {
  display: block;
  font-size: 0.9rem;
  font-weight: 800;
  text-transform: uppercase;
  color: #111;
  letter-spacing: 0.5px;
}
.item-subtotal {
  font-size: 0.85rem;
  font-weight: 600;
  color: #777;
}
.item-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.btn-qty-luxury {
  background: white;
  border: 1px solid #ddd;
  color: #333;
  width: 28px;
  height: 28px;
  font-weight: 700;
  border-radius: 4px;
}
.qty-num {
  font-weight: 700;
  min-width: 20px;
  text-align: center;
}
.btn-qty-luxury:hover {
  border-color: #c5a059;
  color: #c5a059;
}
.total-amount {
  font-size: 1.8rem;
  font-weight: 800;
  color: #f14949;
}
.btn-confirm-luxury {
  background: #f36161;
  color: #111;
  border: none;
  padding: 18px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 3px;
  font-size: 0.8rem;
  transition: 0.3s;
}
.btn-confirm-luxury:hover {
  background: #d6b575;
  letter-spacing: 4px;
}

/* Minimal controls */
.minimal-search {
  border: none;
  border-bottom: 1px solid rgb(232, 16, 16);
  font-size: 1rem;
  padding: 10px 6px;
  background: transparent;
  min-width: 180px;
}

.minimal-select {
  border: none;
  background: transparent;
  cursor: pointer;
  appearance: none;
  padding-right: 20px;
  min-width: 160px;
}

.minimal-search:focus,
.minimal-select:focus {
  outline: none;
}
/* Animations */
.reveal {
  opacity: 0;
  transform: translateY(20px);
  transition: 0.8s cubic-bezier(0.2, 1, 0.3, 1);
}
.reveal.active {
  opacity: 1;
  transform: translateY(0);
}

@media (max-width: 768px) {
  .booking-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  .section-title {
    font-size: 1.8rem;
  }
}

.v-select {
  position: relative;
  z-index: 1000;
}

.vs__dropdown-menu {
  z-index: 2000;
}

/* ===== SELECT 2 && TIM KIEM ===== */
/* Ghi đè triệt để các viền mặc định của thư viện */
:deep(.v-select) {
  background: #fff;
}

/* Khung chính: Xóa border mặc định, dùng border của mình */
:deep(.vs__dropdown-toggle) {
  border: none !important;
  border-bottom: 1px solid #e74c3c !important;
  border-radius: 0 !important;
  height: 42px;
  padding: 0;
  display: flex;
  align-items: center;
  box-shadow: none !important;
}
/* Xóa viền đỏ/xanh khi focus mà thư viện tự thêm vào search input */
:deep(.vs__search),
:deep(.vs__selected) {
  padding-left: 6px !important;
  font-size: 16px;
}
:deep(.vs__search),
:deep(.vs__search:focus) {
  border: none !important;
  margin: 0 !important;
  padding: 0 6px !important;
  height: 40px;
}

/* Xử lý text đã chọn */
:deep(.vs__selected) {
  margin: 0 !important;
  padding-left: 12px !important;
  color: #374151;
  border: none !important;
}

/* Placeholder */
:deep(.vs__search::placeholder) {
  color: #9ca3af !important;
}

/* Hiệu ứng khi mở menu (Focus) */
:deep(.vs--open .vs__dropdown-toggle) {
  border-color: #c5a059 !important;
  box-shadow: none !important;
}

/* Menu đổ xuống */
:deep(.vs__dropdown-menu) {
  border: 1px solid #e5e7eb !important;
  border-radius: 10px !important;
  margin-top: 8px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1) !important;
  padding: 6px !important;
  z-index: 1000;
}

/* Các Option bên trong */
:deep(.vs__dropdown-option) {
  border-radius: 8px !important;
  padding: 8px 12px !important;
  margin-bottom: 2px !important;
  color: #4b5563;
}

:deep(.vs__dropdown-option--highlight) {
  background: #f3f4f6 !important;
  color: #3b6cff !important;
}

:deep(.vs__dropdown-option--selected) {
  background: #e8efff !important;
  color: #3b6cff !important;
}

/* Icon mũi tên và nút xóa */
:deep(.vs__actions) {
  padding-right: 10px !important;
}

:deep(.vs__open-indicator) {
  fill: #9ca3af !important;
  transform: scale(0.7);
}

:deep(.vs__clear) {
  display: none !important;
}

.disabled {
  opacity: 0.4;
  pointer-events: none;
}
.empty-table {
  text-align: center;
  color: #6b7280;
  padding: 20px;
  font-style: italic;
}

.menu-filter-row input {
  width: 220px;
}

.menu-filter-row .v-select {
  width: 200px;
}
.menu-filter-row {
  display: flex;
  align-items: center;
  gap: 20px;
}

.menu-filter-row .minimal-search {
  width: 220px;
}

.menu-filter-row .v-select {
  width: 200px;
}
/* ===== CHỈ FIX MOBILE ĐẲNG CẤP TẠI ĐÂY ===== */
@media (max-width: 768px) {
  /* tiêu đề co giãn mượt hơn */
  .section-title {
    font-size: 1.8rem !important;
    letter-spacing: -0.5px;
  }

  /* layout 1 cột sạch sẽ */
  .booking-grid {
    grid-template-columns: 1fr !important;
    gap: 15px !important;
  }

  .full-width {
    grid-column: span 1 !important;
  }

  /* card thông tin bo góc lớn */
  .luxury-card {
    padding: 20px !important;
    border-radius: 20px !important;
    border: none !important;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05) !important;
  }

  /* bộ lọc dồn hàng dọc chuyên nghiệp */
  .menu-filter-row {
    flex-direction: column !important;
    gap: 12px !important;
  }

  .menu-filter-row > *,
  .menu-filter-row .minimal-search,
  .menu-filter-row .v-select {
    width: 100% !important;
  }

  /* món ăn dạng card nổi */
  .food-row {
    flex-direction: row !important; /* giữ ảnh bên trái cho sang */
    align-items: flex-start !important;
    padding: 12px !important;
    border: 1px solid #f0f0f0 !important;
    border-radius: 15px !important;
  }

  .food-img-box {
    width: 70px !important;
    height: 70px !important;
    border-radius: 10px !important;
  }

  .food-name {
    font-size: 15px !important;
  }

  /* BIẾN GIỎ HÀNG THÀNH BOTTOM BAR ĐẲNG CẤP */
  .col-lg-4.reveal-right {
    position: fixed !important;
    bottom: 0 !important;
    left: 0 !important;
    right: 0 !important;
    width: 100% !important;
    z-index: 1000 !important;
    padding: 0 !important;
    margin: 0 !important;
  }

  .receipt-card {
    border-radius: 25px 25px 0 0 !important;
    box-shadow: 0 -10px 40px rgba(0, 0, 0, 0.15) !important;
    border: none !important;
    background: #fff !important;
  }

  .receipt-inner {
    padding: 15px 20px !important;
  }

  /* ẩn bớt list món cho gọn, chỉ hiện tổng tiền và nút */
  .selected-items-list {
    display: none; /* hoặc giới hạn max-height: 80px; overflow: auto; */
  }

  .receipt-head {
    margin-bottom: 10px !important;
  }

  .total-amount {
    font-size: 1.5rem !important;
  }

  /* nút xác nhận full chiều ngang, bo góc app-style */
  .btn-confirm-luxury {
    width: 100% !important;
    border-radius: 12px !important;
    padding: 14px !important;
    font-size: 0.9rem !important;
    margin-top: 5px !important;
    background: #1a1a1a !important; /* đen sang trọng */
    color: #c5a059 !important; /* chữ vàng đồng */
  }
  /* Căn chỉnh nút kiểm tra đặt bàn trên mobile */
  .btn-check-booking-luxury {
    width: 100% !important;
    padding: 14px 20px !important;
    font-size: 0.85rem !important;
    letter-spacing: 1.5px !important;
    border-radius: 12px !important;
  }
}

/* BAT DAU LAM */
.summary-actions .error-hint {
  font-size: 12px;
  color: #dc2626;
  text-align: center;
}
.error-hint {
  color: #dc2626;
  font-size: 13px;
  margin-top: 8px;
}

.required {
  color: #d32828;
  font-weight: 900;
}
.required {
  color: #dc2626 !important;
}

/* /////////////// QUY ĐỊNH NHÀ HÀNG //////////////////// */
.rules-card {
  background: #ffffff;
  border: 1px solid #f0f0f0;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.02);
  height: fit-content;
}
.rules-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.rule-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  font-size: 0.85rem;
  line-height: 1.4;
  color: #666;
}
.rule-icon {
  color: #c5a059;
  font-size: 1rem;
  margin-top: 2px;
  min-width: 16px;
  text-align: center;
}
.rule-item strong {
  color: #1a1a1a;
  display: block;
  margin-bottom: 2px;
  font-weight: 700;
  font-size: 0.9rem;
}
</style>
