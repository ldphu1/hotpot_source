<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useUtils } from "@/utils/helper";
import { loginCustomer } from "@/services/authService";
import { forgotPassword } from "@/services/authService";
import { verifyOtp } from "@/services/authService";
import { resetPassword } from "@/services/authService";
const router = useRouter();
const { notify, nprogress } = useUtils();

const username = ref("");
const password = ref("");
const rememberMe = ref(false);
const isLoading = ref(false);
const goHome = () => {
  router.push("/");
};

// ===== FORGOT PASSWORD =====
const step = ref(0); // 0: đóng | 1: email | 2: otp | 3: mk mới
const fpEmail = ref("");
const fpOtp = ref("");
const fpNewPassword = ref("");
const fpConfirmPassword = ref("");
const fpLoading = ref(false);
const showNewPassword = ref(false);
const showConfirmPassword = ref(false);

const openForgotPassword = () => {
  fpEmail.value = "";
  fpOtp.value = "";
  fpNewPassword.value = "";
  fpConfirmPassword.value = "";
  step.value = 1;
};
const closeForgotPassword = () => {
  step.value = 0;
};

const handleSendOtp = async () => {
  if (!fpEmail.value.trim()) return notify.error("Vui lòng nhập email");
  try {
    fpLoading.value = true;
    await forgotPassword({ email: fpEmail.value.trim() });
    notify.success("Mã OTP đã được gửi đến email của bạn");
    step.value = 2;
  } catch (err) {
    notify.error(err.response?.data?.message || "Gửi OTP thất bại");
  } finally {
    fpLoading.value = false;
  }
};

const handleVerifyOtp = async () => {
  if (!fpOtp.value.trim()) return notify.error("Vui lòng nhập mã OTP");
  try {
    fpLoading.value = true;
    await verifyOtp({ email: fpEmail.value.trim(), otp: fpOtp.value.trim() });
    notify.success("OTP hợp lệ");
    step.value = 3;
  } catch (err) {
    notify.error(
      err.response?.data?.message || "OTP không hợp lệ hoặc đã hết hạn",
    );
  } finally {
    fpLoading.value = false;
  }
};

const handleResetPassword = async () => {
  if (!fpNewPassword.value || !fpConfirmPassword.value)
    return notify.error("Vui lòng nhập đầy đủ mật khẩu");
  if (fpNewPassword.value.length < 6)
    return notify.error("Mật khẩu phải có ít nhất 6 ký tự");
  if (fpNewPassword.value !== fpConfirmPassword.value)
    return notify.error("Mật khẩu xác nhận không khớp");
  try {
    fpLoading.value = true;
    await resetPassword({
      email: fpEmail.value.trim(),
      otp: fpOtp.value.trim(),
      newPassword: fpNewPassword.value,
    });
    notify.success("Đặt lại mật khẩu thành công! Vui lòng đăng nhập lại");
    step.value = 0;
  } catch (err) {
    notify.error(err.response?.data?.message || "Đặt lại mật khẩu thất bại");
  } finally {
    fpLoading.value = false;
  }
};

const errors = ref({
  username: "",
  password: "",
});

const clearErrors = () => {
  Object.keys(errors.value).forEach((k) => (errors.value[k] = ""));
};

const validateForm = () => {
  clearErrors();
  let valid = true;

  if (!username.value?.trim()) {
    errors.value.username = "Vui lòng nhập email";
    valid = false;
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  if (username.value && !emailRegex.test(username.value)) {
    errors.value.username = "Email không hợp lệ";
    valid = false;
  }

  if (!password.value?.trim()) {
    errors.value.password = "Vui lòng nhập mật khẩu";
    valid = false;
  }

  return valid;
};

const handleLogin = async () => {
  if (!validateForm()) return;

  try {
    isLoading.value = true;
    nprogress.start();

    const res = await loginCustomer({
      email: username.value,
      matKhau: password.value,
    });

    const data = res.data;

    localStorage.setItem("token", data.token);
    localStorage.setItem("refreshToken", data.refreshToken);
    localStorage.setItem("name", data.hoTen);
    localStorage.setItem("email", data.email);
    localStorage.setItem("soDienThoai", data.soDienThoai);
    localStorage.setItem("avatar", data.avatar);
    localStorage.setItem("role", "CUSTOMER");

    localStorage.removeItem("chatSessionId");
    localStorage.removeItem("idCuocTroChuyenHienTai");
    localStorage.removeItem("thong-tin-khach-hang"); // ✅ thêm dòng này

    window.dispatchEvent(new Event("auth-changed"));

    notify.success("Đăng nhập thành công");
    router.push("/");
  } catch (err) {
    notify.error(err.response?.data?.message || "Đăng nhập thất bại");
  } finally {
    isLoading.value = false;
    nprogress.done();
  }
};

const loginWithGoogle = () => {
  window.location.href = "http://localhost:8080/oauth2/authorization/google";
};

const goToRegister = () => {
  router.push("/dang-ky");
};
</script>

<template>
  <div class="login-luxury-wrapper">
    <div class="overlay-dark"></div>

    <div class="container d-flex min-vh-100 py-4 py-sm-5">
      <div class="login-card-royal" style="margin: auto">
        <div class="close-btn" @click="goHome">✕</div>
        <div class="text-center mb-5">
          <img
            src="@/assets/images/logodatn.png"
            class="main-logo-luxury mb-3"
          />
          <h2 class="title-gold">FourSpiceHotpot</h2>
          <p class="subtitle-white">TRẢI NGHIỆM ẨM THỰC</p>
        </div>

        <form @submit.prevent="handleLogin">
          <div class="input-royal mb-4">
            <label>Email <span class="required">*</span></label>

            <div class="input-box">
              <i class="bi bi-person-fill"></i>

              <input
                v-model="username"
                type="text"
                placeholder="Nhập thông tin tài khoản"
                @input="errors.username = ''"
              />

              <div class="input-line"></div>
            </div>

            <transition name="shake">
              <p v-if="errors.username" class="error-hint">
                <i class="fas fa-exclamation-circle"></i>
                {{ errors.username }}
              </p>
            </transition>
          </div>
          <div class="input-royal mb-4">
            <label>Mật khẩu <span class="required">*</span></label>

            <div class="input-box">
              <i class="bi bi-shield-lock-fill"></i>

              <input
                v-model="password"
                type="password"
                placeholder="Nhập mật khẩu"
                @input="errors.password = ''"
              />

              <div class="input-line"></div>
            </div>

            <transition name="shake">
              <p v-if="errors.password" class="error-hint">
                <i class="fas fa-exclamation-circle"></i>
                {{ errors.password }}
              </p>
            </transition>
          </div>

          <div class="d-flex justify-content-between mb-4 small">
            <label>
              <input type="checkbox" v-model="rememberMe" />
              Ghi nhớ
            </label>
            <span class="forgot-gold" @click="openForgotPassword"
              >Quên mật khẩu?</span
            >
          </div>

          <button class="btn-royal-submit w-100" :disabled="isLoading">
            <span v-if="!isLoading">XÁC NHẬN</span>
            <div
              v-else
              class="spinner-border spinner-border-sm text-light"
            ></div>
            <div class="btn-light-sweep"></div>
          </button>
        </form>

        <div class="divider-royal my-4">
          <span>HOẶC</span>
        </div>
        <button class="btn-google-luxury w-100" @click="loginWithGoogle">
          <svg width="20" height="20" viewBox="0 0 48 48" class="me-2">
            <path
              fill="#EA4335"
              d="M24 9.5c3.54 0 6.6 1.22 9.06 3.22l6.78-6.78C35.82 2.36 30.36 0 24 0 14.64 0 6.64 5.38 2.68 13.22l7.88 6.12C12.48 13.18 17.78 9.5 24 9.5z"
            />
            <path
              fill="#4285F4"
              d="M46.5 24.5c0-1.54-.14-3.02-.4-4.44H24v8.4h12.66c-.54 2.9-2.18 5.36-4.66 7.02l7.24 5.62c4.24-3.9 6.26-9.64 6.26-16.6z"
            />
            <path
              fill="#FBBC05"
              d="M10.56 28.34c-.48-1.42-.76-2.94-.76-4.5s.28-3.08.76-4.5l-7.88-6.12C.96 16.2 0 19.98 0 23.84s.96 7.64 2.68 10.62l7.88-6.12z"
            />
            <path
              fill="#34A853"
              d="M24 48c6.36 0 11.82-2.1 15.76-5.68l-7.24-5.62c-2.02 1.36-4.6 2.16-8.52 2.16-6.22 0-11.52-3.68-13.44-8.84l-7.88 6.12C6.64 42.62 14.64 48 24 48z"
            />
          </svg>
          Đăng nhập bằng Google
        </button>

        <p class="text-center mt-4">
          Chưa có tài khoản?
          <span class="action-link-gold" @click="goToRegister">Đăng ký</span>
        </p>
      </div>
    </div>
    <!-- ===== MODAL QUÊN MẬT KHẨU ===== -->
    <Transition name="fade">
      <div
        v-if="step > 0"
        class="modal-overlay"
        @click.self="closeForgotPassword"
      >
        <div class="modal-card">
          <!-- STEP 1: Email -->
          <div v-if="step === 1">
            <div class="modal-header-custom">
              <div class="modal-icon-wrap">
                <i class="ri-mail-send-line"></i>
              </div>
              <h5>Quên mật khẩu</h5>
              <p>Nhập email để nhận mã OTP xác thực</p>
            </div>
            <div class="modal-input-group">
              <i class="ri-mail-line"></i>
              <input
                v-model="fpEmail"
                type="email"
                placeholder="Nhập địa chỉ email"
                @keyup.enter="handleSendOtp"
              />
            </div>
            <div class="modal-actions">
              <button class="btn-modal-cancel" @click="closeForgotPassword">
                <i></i> Hủy
              </button>
              <button
                class="btn-modal-submit"
                :disabled="fpLoading"
                @click="handleSendOtp"
              >
                <span v-if="!fpLoading"
                  ><i class="ri-send-plane-fill me-1"></i> Gửi OTP</span
                >
                <div
                  v-else
                  class="spinner-border spinner-border-sm text-light"
                ></div>
              </button>
            </div>
          </div>

          <!-- STEP 2: OTP -->
          <div v-else-if="step === 2">
            <div class="modal-header-custom">
              <div class="modal-icon-wrap">
                <i class="ri-shield-keyhole-line"></i>
              </div>
              <h5>Nhập mã OTP</h5>
              <p>
                Mã OTP đã được gửi đến <strong>{{ fpEmail }}</strong>
              </p>
            </div>
            <div class="modal-input-group">
              <i class="ri-key-2-line"></i>
              <input
                v-model="fpOtp"
                type="text"
                maxlength="6"
                placeholder="Nhập mã 6 số"
                @keyup.enter="handleVerifyOtp"
              />
            </div>
            <p class="resend-text">
              Không nhận được?
              <span class="resend-link" @click="handleSendOtp">Gửi lại</span>
            </p>
            <div class="modal-actions">
              <button class="btn-modal-cancel" @click="step = 1">
                <i></i> Quay lại
              </button>
              <button
                class="btn-modal-submit"
                :disabled="fpLoading"
                @click="handleVerifyOtp"
              >
                <span v-if="!fpLoading"
                  ><i class="ri-checkbox-circle-line me-1"></i> Xác nhận</span
                >
                <div
                  v-else
                  class="spinner-border spinner-border-sm text-light"
                ></div>
              </button>
            </div>
          </div>

          <!-- STEP 3: Mật khẩu mới -->
          <div v-else-if="step === 3">
            <div class="modal-header-custom">
              <div class="modal-icon-wrap">
                <i class="ri-lock-password-line"></i>
              </div>
              <h5>Đặt lại mật khẩu</h5>
              <p>Nhập mật khẩu mới cho tài khoản của bạn</p>
            </div>
            <div class="modal-input-group mb-3">
              <i class="ri-lock-line"></i>
              <input
                v-model="fpNewPassword"
                :type="showNewPassword ? 'text' : 'password'"
                placeholder="Mật khẩu mới (ít nhất 6 ký tự)"
              />
              <i
                class="toggle-eye"
                :class="showNewPassword ? 'ri-eye-off-line' : 'ri-eye-line'"
                @click="showNewPassword = !showNewPassword"
              ></i>
            </div>
            <div class="modal-input-group">
              <i class="ri-lock-2-line"></i>
              <input
                v-model="fpConfirmPassword"
                :type="showConfirmPassword ? 'text' : 'password'"
                placeholder="Xác nhận mật khẩu mới"
                @keyup.enter="handleResetPassword"
              />
              <i
                class="toggle-eye"
                :class="showConfirmPassword ? 'ri-eye-off-line' : 'ri-eye-line'"
                @click="showConfirmPassword = !showConfirmPassword"
              ></i>
            </div>
            <div class="modal-actions">
              <button class="btn-modal-cancel" @click="step = 2">
                <i></i> Quay lại
              </button>
              <button
                class="btn-modal-submit"
                :disabled="fpLoading"
                @click="handleResetPassword"
              >
                <span v-if="!fpLoading"
                  ><i class="ri-save-line me-1"></i> Xác nhận</span
                >
                <div
                  v-else
                  class="spinner-border spinner-border-sm text-light"
                ></div>
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700;800&display=swap");
.btn-google-luxury {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
/* ===== WRAPPER ===== */
.login-luxury-wrapper {
  position: relative;
  min-height: 100vh;
  font-family: var(--font-family, sans-serif);
  background: url("@/assets/images/banner1.jpg") center center no-repeat;
  background-size: cover;
}

.overlay-dark {
  position: absolute;
  inset: 0;
  background: radial-gradient(
    circle,
    rgba(0, 0, 0, 0.3) 0%,
    rgba(0, 0, 0, 0.85) 100%
  );
  z-index: 0;
}

/* ===== CARD ===== */
.login-card-royal {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 500px;
  padding: 45px 40px;
  border-radius: 8px;

  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);

  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.5);

  overflow: hidden;
}

/* ===== HEADER ===== */
.main-logo-luxury {
  width: 140px;
  display: block;
  margin: 0 auto;
}

.title-gold {
  font-weight: 800;
  font-size: 35px;
  letter-spacing: 2px;
  color: #d33535;
  margin: 0;
}

.subtitle-white {
  color: #c22424;
  font-weight: 700;
  letter-spacing: 4px;
  font-size: 0.8rem;
  margin-top: 5px;
}

/* ===== INPUT ===== */
.input-royal label {
  display: block;
  font-size: 0.7rem;
  font-weight: 800;
  color: #333;
  margin-bottom: 8px;
  text-transform: uppercase;
}

.input-box {
  position: relative;
}

.input-box i {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #b54646;
  font-size: 1.1rem;
}

.input-box input {
  width: 100%;
  padding: 14px 14px 14px 45px;
  border: none;
  border-bottom: 1px solid #eee;
  background: transparent;
  font-weight: 600;
  transition: 0.3s;
}

.input-box input:focus {
  outline: none;
}

/* line effect */
.input-line {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: #d4af37;
  transition: 0.4s;
}

.input-box input:focus + .input-line {
  width: 100%;
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
}
/* ===== CHECKBOX & LINK ===== */
.forgot-gold {
  color: #d03333;
  font-weight: 700;
  cursor: pointer;
}

/* ===== BUTTON ===== */
.btn-royal-submit {
  position: relative;
  overflow: hidden;

  background: #d92d2d;
  color: #fff;
  border: none;

  padding: 16px;
  font-weight: 800;
  letter-spacing: 2px;

  transition: 0.3s;
  box-shadow: 0 10px 20px rgba(139, 30, 30, 0.3);
}

.btn-royal-submit:hover:not(:disabled) {
  background: #e42c2c;
  transform: translateY(-2px);
  box-shadow: 0 15px 30px rgba(139, 30, 30, 0.4);
}

/* ===== LIGHT SWEEP (FIX MƯỢT) ===== */
.btn-light-sweep {
  position: absolute;
  top: 0;
  left: 0;
  width: 60%;
  height: 100%;
  background: linear-gradient(
    120deg,
    transparent,
    rgba(255, 255, 255, 0.4),
    transparent
  );
  transform: translateX(-150%) skewX(-25deg);
  transition: transform 0.6s ease;
  pointer-events: none;
}

.btn-royal-submit:hover .btn-light-sweep {
  transform: translateX(150%) skewX(-25deg);
}

/* ===== DIVIDER ===== */
.divider-royal {
  position: relative;
  text-align: center;
}

.divider-royal::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #eee;
}

.divider-royal span {
  padding: 0 12px;
  font-size: 0.7rem;
  font-weight: 800;
  color: #eb1919;
  letter-spacing: 2px;
  position: relative;
}

/* ===== GOOGLE BUTTON ===== */
.btn-google-luxury {
  background: #fff;
  border: 1px solid #eee;
  padding: 12px;
  font-weight: 700;
  transition: 0.3s;
}

.btn-google-luxury:hover {
  border-color: #d4af37;
  color: #000;
}

/* ===== REGISTER LINK ===== */
.action-link-gold {
  color: #8b1e1e;
  font-weight: 800;
  cursor: pointer;
}

.action-link-gold:hover {
  text-decoration: underline;
}

/* ===== RESPONSIVE ===== */
@media (max-width: 576px) {
  .login-card-royal {
    padding: 30px 16px;
  }

  .title-gold {
    font-size: 26px;
    letter-spacing: 1px;
  }

  .main-logo-luxury {
    width: 100px;
  }
}

.required {
  color: #e53935; /* đỏ */
  font-weight: 800;
}

.close-btn {
  position: absolute;
  top: 12px;
  right: 14px;
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

/* ===== MODAL ===== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}
.modal-card {
  background: #fff;
  border-radius: 12px;
  padding: 36px 32px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}
.modal-header-custom {
  text-align: center;
  margin-bottom: 24px;
}
.modal-icon-wrap {
  width: 64px;
  height: 64px;
  background: #fff5f5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 14px;
  font-size: 28px;
  color: #d92d2d;
  border: 2px solid #f5c6c6;
}
.modal-header-custom h5 {
  font-size: 1.2rem;
  font-weight: 800;
  color: #8c0000;
  margin-bottom: 6px;
}
.modal-header-custom p {
  font-size: 0.85rem;
  color: #666;
  margin: 0;
}
.modal-input-group {
  position: relative;
  margin-bottom: 16px;
}
.modal-input-group i:first-child {
  position: absolute;
  left: 13px;
  top: 50%;
  transform: translateY(-50%);
  color: #b54646;
  font-size: 1.1rem;
}
.modal-input-group input {
  width: 100%;
  padding: 12px 40px 12px 40px;
  border: 1.5px solid #eee;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 600;
  transition: 0.2s;
}
.modal-input-group input:focus {
  outline: none;
  border-color: #d92d2d;
}
.toggle-eye {
  position: absolute;
  right: 13px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
  font-size: 1.1rem;
  cursor: pointer;
}
.toggle-eye:hover {
  color: #d92d2d;
}
.resend-text {
  font-size: 0.82rem;
  color: #888;
  text-align: center;
  margin-bottom: 20px;
}
.resend-link {
  color: #d92d2d;
  font-weight: 700;
  cursor: pointer;
}
.resend-link:hover {
  text-decoration: underline;
}
.modal-actions {
  display: flex;
  gap: 10px;
  margin-top: 24px;
}
.btn-modal-cancel {
  flex: 1;
  padding: 11px;
  border: 1.5px solid #ddd;
  background: #fff;
  border-radius: 8px;
  font-weight: 700;
  color: #555;
  transition: 0.2s;
}
.btn-modal-cancel:hover {
  border-color: #d92d2d;
  color: #d92d2d;
}
.btn-modal-submit {
  flex: 2;
  padding: 11px;
  background: #d92d2d;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-weight: 800;
  letter-spacing: 0.5px;
  transition: 0.2s;
}
.btn-modal-submit:hover:not(:disabled) {
  background: #b71c1c;
  transform: translateY(-1px);
}
.btn-modal-submit:disabled {
  opacity: 0.7;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
