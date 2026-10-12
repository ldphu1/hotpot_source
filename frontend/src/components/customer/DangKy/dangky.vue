<script setup>
import { ref, reactive, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import { getProvinces, getProvinceDetail } from "@/api/diachi/diaChiApi";
import { useUtils } from "@/utils/helper";
import { registerCustomer } from "@/api/customer/register/registerApi";
const { notify, nprogress } = useUtils();
const router = useRouter();
const loading = ref(false);
const provinces = ref([]);
const wards = ref([]);

const form = reactive({
  fullname: "",
  phone: "",
  email: "",
  birthday: "",
  gender: "Nam",
  password: "",
  confirmPassword: "",
  province: "",
  ward: "",
  street: "",
});

const errors = ref({
  fullname: "",
  phone: "",
  email: "",
  birthday: "",
  province: "",
  ward: "",
  password: "",
  confirmPassword: "",
});

const clearErrors = () => {
  Object.keys(errors.value).forEach((k) => (errors.value[k] = ""));
};

const validateForm = () => {
  clearErrors();

  let valid = true;

  if (!form.fullname.trim()) {
    errors.value.fullname = "Vui lòng nhập họ tên";
    valid = false;
  }

  if (!form.phone.trim()) {
    errors.value.phone = "Vui lòng nhập số điện thoại";
    valid = false;
  } else if (!/^(0[3|5|7|8|9])[0-9]{8}$/.test(form.phone.trim())) {
    errors.value.phone = "Số điện thoại không hợp lệ";
    valid = false;
  }

  if (!form.email.trim()) {
    errors.value.email = "Vui lòng nhập email";
    valid = false;
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.value.email = "Email không hợp lệ";
    valid = false;
  }

  if (!form.birthday) {
    errors.value.birthday = "Vui lòng chọn ngày sinh";
    valid = false;
  }

  if (!form.province) {
    errors.value.province = "Vui lòng chọn tỉnh/thành";
    valid = false;
  }

  if (!form.ward) {
    errors.value.ward = "Vui lòng chọn phường/xã";
    valid = false;
  }

  if (!form.password) {
    errors.value.password = "Vui lòng nhập mật khẩu";
    valid = false;
  } else if (form.password.length < 6) {
    errors.value.password = "Mật khẩu phải có ít nhất 6 ký tự";
    valid = false;
  }

  if (!form.confirmPassword) {
    errors.value.confirmPassword = "Vui lòng xác nhận mật khẩu";
    valid = false;
  } else if (form.password !== form.confirmPassword) {
    errors.value.confirmPassword = "Mật khẩu xác nhận không khớp";
    valid = false;
  }

  return valid;
};

onMounted(async () => {
  try {
    const res = await fetch("https://provinces.open-api.vn/api/v2/p/");
    provinces.value = await res.json();
  } catch (e) {
    console.error("Lỗi load tỉnh", e);
  }
});
const closeRegister = () => {
  router.push("/");
};
watch(
  () => form.province,
  async (newVal) => {
    wards.value = [];
    form.ward = "";
    if (!newVal) return;

    try {
      const selected = provinces.value.find((p) => p.name === newVal);
      if (!selected) return;

      const res = await fetch(
        `https://provinces.open-api.vn/api/v2/p/${selected.code}?depth=2`,
      );
      const data = await res.json();
      wards.value = data.wards || [];
    } catch (e) {
      console.error("Lỗi load xã", e);
    }
  },
);
const dangKyKhachHang = async () => {
  if (!validateForm()) return;

  if (form.password !== form.confirmPassword) {
    notify.error("Mật khẩu xác nhận không khớp!");
    return;
  }

  if (!(await notify.confirm("Bạn muốn đăng ký tài khoản này?"))) return;

  nprogress.start();

  try {
    const payload = {
      hoTen: form.fullname,
      soDienThoai: form.phone.replace(/\s/g, ""),
      email: form.email,
      ngaySinh: form.birthday,
      gioiTinh: form.gender === "Nam",
      matKhau: form.password,
      diaChiList: [
        {
          tinhThanh: form.province,
          xaPhuong: form.ward,
          diaChiChiTiet: form.street,
          macDinh: true,
        },
      ],
    };

    const formData = new FormData();

    formData.append(
      "data",
      new Blob([JSON.stringify(payload)], {
        type: "application/json",
      }),
    );

    const res = await registerCustomer(formData);

    notify.success(res.data?.message || "Đăng ký thành công!");

    router.push("/dang-nhap");
  } catch (e) {
    console.error("REGISTER ERROR:", e);

    notify.error(
      e.response?.data?.message ||
        e.response?.data ||
        e.message ||
        "Lỗi hệ thống",
    );
  } finally {
    nprogress.done();
  }
};
const goLogin = () => {
  router.push("/dang-nhap");
};
</script>
<template>
  <div class="luxury-register-wrapper">
    <div class="floating-particles">
      <div v-for="n in 5" :key="n" :class="'particle p-' + n"></div>
    </div>

    <div class="container d-flex min-vh-100 py-4 py-sm-5">
      <div class="register-card-oriental" style="margin: auto">
        <div class="corner-gold tl"></div>
        <div class="corner-gold tr"></div>
        <div class="corner-gold bl"></div>
        <div class="corner-gold br"></div>
        <div class="close-btn" @click="closeRegister">✕</div>
        <div class="text-center mb-5">
          <h2 class="premium-header-title">ĐĂNG KÝ</h2>
          <div class="luxury-divider">
            <span class="divider-dot"></span>
          </div>
          <p class="subtitle-gold">FOUR SPICE HOTPOT EXPERIENCE</p>
        </div>

        <form @submit.prevent="dangKyKhachHang" class="premium-form-luxury">
          <div class="row g-5">
            <div class="col-md-6 border-gold-divider">
              <h5 class="luxury-section-title">
                <i class="bi bi-person-badge"></i> Thông tin cá nhân
              </h5>

              <div class="luxury-input-group">
                <label>Họ và tên *</label>
                <div class="luxury-input-box">
                  <input
                    v-model="form.fullname"
                    type="text"
                    placeholder="Ví dụ: Lãnh Minh Hiếu"
                    @input="errors.fullname = ''"
                  />

                  <transition name="shake">
                    <p v-if="errors.fullname" class="error-hint">
                      <i class="fas fa-exclamation-circle"></i>
                      {{ errors.fullname }}
                    </p>
                  </transition>

                  <div class="input-focus-line"></div>
                </div>
              </div>

              <div class="row">
                <div class="col-sm-7">
                  <div class="luxury-input-group">
                    <label>Số điện thoại *</label>
                    <div class="luxury-input-box">
                      <input
                        v-model="form.phone"
                        type="tel"
                        placeholder="0332..."
                        @input="errors.phone = ''"
                      />

                      <transition name="shake">
                        <p v-if="errors.phone" class="error-hint">
                          <i class="fas fa-exclamation-circle"></i>
                          {{ errors.phone }}
                        </p>
                      </transition>

                      <div class="input-focus-line"></div>
                    </div>
                  </div>
                </div>
                <div class="col-sm-5">
                  <div class="luxury-input-group">
                    <label>Giới tính *</label>
                    <div class="luxury-gender-box">
                      <label class="gender-btn">
                        <input type="radio" value="Nam" v-model="form.gender" />
                        <span>NAM</span>
                      </label>
                      <label class="gender-btn">
                        <input type="radio" value="Nữ" v-model="form.gender" />
                        <span>NỮ</span>
                      </label>
                    </div>
                  </div>
                </div>
              </div>

              <div class="luxury-input-group">
                <label>Email *</label>
                <div class="luxury-input-box">
                  <input
                    v-model="form.email"
                    type="email"
                    placeholder="...@gmail.com"
                    @input="errors.email = ''"
                  />

                  <transition name="shake">
                    <p v-if="errors.email" class="error-hint">
                      <i class="fas fa-exclamation-circle"></i>
                      {{ errors.email }}
                    </p>
                  </transition>

                  <div class="input-focus-line"></div>
                </div>
              </div>

              <div class="luxury-input-group">
                <label>Ngày sinh *</label>
                <div class="luxury-input-box">
                  <input
                    v-model="form.birthday"
                    type="date"
                    @input="errors.birthday = ''"
                  />

                  <transition name="shake">
                    <p v-if="errors.birthday" class="error-hint">
                      <i class="fas fa-exclamation-circle"></i>
                      {{ errors.birthday }}
                    </p>
                  </transition>

                  <div class="input-focus-line"></div>
                </div>
              </div>
            </div>

            <div class="col-md-6">
              <h5 class="luxury-section-title">
                <i class="bi bi-geo-alt"></i> Địa chỉ
              </h5>

              <div class="row g-3 mb-4">
                <div class="col-md-6">
                  <div class="luxury-select-box">
                    <select v-model="form.province">
                      <option value="">Chọn Tỉnh/Thành</option>
                      <option
                        v-for="p in provinces"
                        :key="p.code"
                        :value="p.name"
                      >
                        {{ p.name }}
                      </option>
                    </select>
                  </div>
                  <transition name="shake">
                    <p v-if="errors.province" class="error-hint">
                      <i class="fas fa-exclamation-circle"></i>
                      {{ errors.province }}
                    </p>
                  </transition>
                </div>
                <div class="col-md-6">
                  <div class="luxury-select-box">
                    <select v-model="form.ward" :disabled="!form.province">
                      <option value="">Chọn Phường/Xã</option>
                      <option v-for="w in wards" :key="w.code" :value="w.name">
                        {{ w.name }}
                      </option>
                    </select>
                  </div>
                  <transition name="shake">
                    <p v-if="errors.ward" class="error-hint">
                      <i class="fas fa-exclamation-circle"></i>
                      {{ errors.ward }}
                    </p>
                  </transition>
                </div>
              </div>

              <div class="luxury-input-group mb-5">
                <label>Địa chỉ chi tiết</label>
                <div class="luxury-input-box">
                  <input
                    v-model="form.street"
                    type="text"
                    placeholder="Số nhà, tên đường..."
                  />
                  <div class="input-focus-line"></div>
                </div>
              </div>

              <h5 class="luxury-section-title mt-4">
                <i class="bi bi-shield-lock"></i> Thiết lập mật khẩu
              </h5>

              <div class="luxury-input-group">
                <label>Mật khẩu</label>
                <div class="luxury-input-box">
                  <input
                    v-model="form.password"
                    type="password"
                    placeholder="••••••••"
                    @input="errors.password = ''"
                  />

                  <transition name="shake">
                    <p v-if="errors.password" class="error-hint">
                      <i class="fas fa-exclamation-circle"></i>
                      {{ errors.password }}
                    </p>
                  </transition>

                  <div class="input-focus-line"></div>
                </div>
              </div>

              <div class="luxury-input-group">
                <label>Xác nhận mật mã</label>
                <div class="luxury-input-box">
                  <input
                    v-model="form.confirmPassword"
                    type="password"
                    placeholder="••••••••"
                    @input="errors.confirmPassword = ''"
                  />

                  <transition name="shake">
                    <p v-if="errors.confirmPassword" class="error-hint">
                      <i class="fas fa-exclamation-circle"></i>
                      {{ errors.confirmPassword }}
                    </p>
                  </transition>

                  <div class="input-focus-line"></div>
                </div>
              </div>
            </div>
          </div>

          <div class="footer-actions-luxury mt-5">
            <button class="btn-luxury-submit" type="submit" :disabled="loading">
              <span v-if="!loading">
                XÁC NHẬN <i class="bi bi-arrow-right"></i
              ></span>
              <span v-else
                ><div class="spinner-luxury"></div>
                ĐANG XỬ LÝ...</span
              >
              <div class="btn-light-sweep"></div>
            </button>

            <p class="login-redirect">
              Quý khách đã có tài khoản?
              <span @click="goLogin">Đăng nhập tại đây</span>
            </p>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Playfair+Display:wght@700;900&family=Plus+Jakarta+Sans:wght@300;400;600;800&display=swap");

.btn-luxury-submit {
  position: relative;
  overflow: hidden; /* QUAN TRỌNG */
}

.btn-light-sweep {
  position: absolute;
  top: 0;
  left: -120%;
  width: 60%;
  height: 100%;
  background: linear-gradient(
    120deg,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.5) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  transform: skewX(-25deg);
  transition: left 0.6s ease;
  z-index: 2;
  pointer-events: none;
}

.btn-luxury-submit:hover .btn-light-sweep {
  left: 120%;
}
.luxury-register-wrapper {
  background: #f0e9e9;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  background-image: linear-gradient(
      135deg,
      rgba(255, 255, 255, 0.9),
      rgba(255, 255, 255, 0.8)
    ),
    url("@/assets/images/background-form.jpg");
  font-family: var(--font-family, sans-serif);
}

.luxury-register-wrapper::before {
  content: "";
  position: absolute;
  width: 600px;
  height: 600px;
  background: radial-gradient(
    circle,
    rgba(163, 29, 33, 0.15) 0%,
    transparent 70%
  );
  top: -10%;
  right: -5%;
}

.register-card-oriental {
  background: #fff;
  padding: 60px;
  border-radius: 4px;
  border: 3px double #db1818; /* Viền kép Gold */
  width: 100%;
  max-width: 1000px;
  position: relative;
  box-shadow: 0 40px 100px rgba(0, 0, 0, 0.5);
}

.corner-gold {
  position: absolute;
  width: 20px;
  height: 20px;
  border: 3px solid #eb3636;
}
.corner-gold.tl {
  top: 15px;
  left: 15px;
  border-right: none;
  border-bottom: none;
}
.corner-gold.tr {
  top: 15px;
  right: 15px;
  border-left: none;
  border-bottom: none;
}
.corner-gold.bl {
  bottom: 15px;
  left: 15px;
  border-right: none;
  border-top: none;
}
.corner-gold.br {
  bottom: 15px;
  right: 15px;
  border-left: none;
  border-top: none;
}

.close-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;

  font-size: 18px;
  font-weight: 900;
  color: #999;
  cursor: pointer;

  border-radius: 50%;
  transition: 0.2s;
  z-index: 10;
}

.close-btn:hover {
  background: rgba(0, 0, 0, 0.08);
  color: #d92d2d;
  transform: scale(1.1);
}

.premium-header-title {
  font-family: "Playfair Display", serif;
  font-weight: 700;
  font-size: 2.5rem;
  color: #ad282d;
  letter-spacing: 4px;
}

.luxury-divider {
  width: 100px;
  height: 1px;
  background: #d4af37;
  margin: 15px auto;
  position: relative;
}
.divider-dot {
  width: 6px;
  height: 6px;
  background: #a31d21;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) rotate(45deg);
}

.subtitle-gold {
  color: #f83b3b;
  font-weight: 800;
  letter-spacing: 3px;
  font-size: 0.8rem;
}

.luxury-section-title {
  font-family: var(--font-family, sans-serif);
  color: #df262c;
  font-weight: 900;
  margin-bottom: 2rem;
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  gap: 10px;
}

.luxury-input-group {
  margin-bottom: 25px;
}

.luxury-input-group label {
  font-weight: 700;
  font-size: 0.75rem;
  color: #888;
  text-transform: uppercase;
  margin-bottom: 8px;
  display: block;
}

.luxury-input-box {
  position: relative;
}

.luxury-input-box input {
  width: 100%;
  padding: 14px 14px;
  border: none;
  border-bottom: 1px solid #eee;
  background: transparent;
  font-weight: 600;
  color: #333;
  transition: 0.3s;
}

.luxury-input-box input:focus {
  outline: none;
}

.input-focus-line {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: #d4af37;
  transition: 0.4s ease;
}
.error-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #dc2626;
  font-size: 13px;
  margin-top: 8px;
  padding: 6px 12px;
  background-color: #fff1f2;
  border-radius: 6px;
  animation: slideDown 0.3s ease-out;
}

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
.luxury-input-box input:focus + .input-focus-line {
  width: 100%;
}

/* Select Box */
.luxury-select-box select {
  width: 100%;
  padding: 12px 16px;
  border: 1.5px solid #eee;
  border-radius: 8px;
  font-weight: 600;
  color: #555;
  background: #fdfdfd;
  transition: 0.2s;
}

.luxury-select-box select:focus {
  outline: none;
  border-color: #a31d21;
}
/* Gender Button */
.luxury-gender-box {
  display: flex;
  gap: 5px;
}
.gender-btn {
  flex: 1;
  cursor: pointer;
}
.gender-btn input {
  display: none;
}
.gender-btn span {
  display: block;
  padding: 10px;
  border: 1px solid #eee;
  text-align: center;
  font-weight: 800;
  font-size: 0.8rem;
  transition: 0.3s;
}
.gender-btn input:checked + span {
  background: #a31d21;
  color: #fff;
  border-color: #a31d21;
}

/* Footer Button */
.btn-luxury-submit {
  width: 100%;
  background: #bd272c;
  color: #fff;
  padding: 20px;
  border: none;
  border-radius: 4px;
  font-weight: 900;
  letter-spacing: 2px;
  font-size: 1.1rem;
  transition: 0.4s;
  box-shadow: 0 10px 20px rgba(163, 29, 33, 0.2);
}

.btn-luxury-submit:hover:not(:disabled) {
  background: #d01f25;
  transform: translateY(-3px);
  box-shadow: 0 15px 30px rgba(163, 29, 33, 0.4);
}

.login-redirect {
  text-align: center;
  margin-top: 20px;
  font-weight: 700;
  font-size: 0.9rem;
  color: #666;
}
.login-redirect span {
  color: #a31d21;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: 0.3s;
}
.login-redirect span:hover {
  border-color: #a31d21;
}

/* Spinner */
.spinner-luxury {
  width: 20px;
  height: 20px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  display: inline-block;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.border-gold-divider {
  border-right: 1px dashed #d4af37;
}

@media (max-width: 768px) {
  .register-card-oriental {
    padding: 30px 20px;
  }
  .border-gold-divider {
    border-right: none;
    border-bottom: 1px dashed #d4af37;
    padding-bottom: 30px;
    margin-bottom: 30px;
  }
}
</style>
