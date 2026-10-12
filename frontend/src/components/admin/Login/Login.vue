<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useUtils } from "@/utils/helper";
import { loginAdmin } from "@/services/authService";
import { forgotPasswordNhanVien } from "@/api/admin/nhanVien/nhanVienApi";
import { verifyOtpNhanVien } from "@/api/admin/nhanVien/nhanVienApi";
import { resetPasswordNhanVien } from "@/api/admin/nhanVien/nhanVienApi";
import { connectWebSocketForceLogout } from "@/utils/websocket";

// --- GIỮ NGUYÊN LOGIC CỦA ÔNG ANH ---
const router = useRouter();
const { notify, nprogress } = useUtils();
const username = ref("");
const password = ref("");
const rememberMe = ref(false);
const isLoading = ref(false);

const usernameError = ref("");
const passwordError = ref("");

const validateUsername = () => {
  if (!username.value?.trim()) {
    usernameError.value = "Vui lòng nhập email";
    return false;
  }
  usernameError.value = "";
  return true;
};

const validatePassword = () => {
  if (!password.value?.trim()) {
    passwordError.value = "Vui lòng nhập mật khẩu";
    return false;
  }

  passwordError.value = "";
  return true;
};

const handleLogin = async () => {
  const isUsernameValid = validateUsername();
  const isPasswordValid = validatePassword();

  if (!isUsernameValid || !isPasswordValid) {
    return;
  }
  try {
    isLoading.value = true;
    nprogress.start();

    const res = await loginAdmin({
      email: username.value,
      matKhau: password.value,
    });

    const data = res.data;

    localStorage.setItem("token", data.token);
    localStorage.setItem("refreshToken", data.refreshToken);
    localStorage.setItem("name", data.name);
    localStorage.setItem("avatar", data.avatar);
    localStorage.setItem("role", data.role);
    localStorage.setItem("permissions", JSON.stringify(data.permissions || []));
    localStorage.setItem("idNhanVien", data.id);

    if (data.role !== "CUSTOMER" && data.id) {
      connectWebSocketForceLogout(data.id, () => {
        localStorage.removeItem("token");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("idNhanVien");
        localStorage.removeItem("role");
        localStorage.removeItem("name");
        localStorage.removeItem("avatar");
        localStorage.removeItem("permissions");
        notify.error("Tài khoản của bạn đã bị khóa hoặc chuyển sang trạng thái nghỉ việc!");
        router.push("/admin/login");
      });
    }

    notify.success("Đăng nhập admin thành công");

    if (data.role === "NHAN_VIEN") {
      router.push("/admin/dat-ban");
    } else {
      router.push("/admin/tong-quan");
    }
  } catch (err) {
    notify.error(err.response?.data?.message || "Đăng nhập thất bại");
  } finally {
    isLoading.value = false;
    nprogress.done();
  }
};

const goHome = () => {
  router.push("/");
};

//////////////////// QUÊN MK ///////////////////

const step = ref(0);
const fpSoDienThoai = ref("");
const fpEmail = ref("");
const fpOtp = ref("");
const fpNewPassword = ref("");
const fpConfirmPassword = ref("");
const fpLoading = ref(false);
const showNewPassword = ref(false);
const showConfirmPassword = ref(false);

const openForgotPassword = () => {
  fpSoDienThoai.value = "";
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
  if (!fpSoDienThoai.value.trim())
    return notify.error("Vui lòng nhập số điện thoại");
  if (!fpEmail.value.trim()) return notify.error("Vui lòng nhập email");
  try {
    fpLoading.value = true;
    await forgotPasswordNhanVien({
      soDienThoai: fpSoDienThoai.value.trim(),
      email: fpEmail.value.trim(),
    });
    notify.success("Mã OTP đã được gửi đến email của bạn");
    step.value = 2;
  } catch (err) {
    debugger;
    notify.error(
      err.response?.data?.message || "Tài khoản hoặc email không đúng",
    );
    debugger;
  } finally {
    fpLoading.value = false;
  }
};

const handleVerifyOtp = async () => {
  if (!fpOtp.value.trim()) return notify.error("Vui lòng nhập mã OTP");
  try {
    fpLoading.value = true;
    await verifyOtpNhanVien({
      email: fpEmail.value.trim(),
      otp: fpOtp.value.trim(),
    });
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
    await resetPasswordNhanVien({
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

// -----------------------------------
</script>

<template>
  <div class="login-luxury-wrapper">
    <div class="overlay-dark"></div>

    <div class="container d-flex min-vh-100 py-4 py-sm-5">
      <div class="login-card-royal reveal-up" style="margin: auto">
        <div class="close-btn" @click="goHome">✕</div>
        <div class="card-shine-sweep"></div>

        <div class="text-center mb-5">
          <div class="logo-container mb-3">
            <img
              src="@/assets/images/logodatn.png"
              alt="Logo"
              class="main-logo-luxury"
            />
          </div>
          <h2 class="title-gold">Hệ Thống Quản Trị</h2>
          <p class="subtitle-white">FOUR SPICE HOTPOT</p>
        </div>

        <form @submit.prevent="handleLogin" class="form-container">
          <div class="input-royal mb-4">
            <label class="label-black">
              Tên đăng nhập <span class="required">*</span>
            </label>

            <div class="input-box" :class="{ 'input-error': usernameError }">
              <i class="bi bi-person-fill"></i>

              <input
                v-model.trim="username"
                type="text"
                placeholder="Nhập tài khoản"
                @blur="validateUsername"
              />

              <div class="input-line"></div>
            </div>
            <transition name="shake">
              <p v-if="usernameError" class="error-hint">
                <i class="fas fa-exclamation-circle"></i>
                {{ usernameError }}
              </p>
            </transition>
          </div>

          <div class="input-royal mb-4">
            <label class="label-black">
              Mật khẩu <span class="required">*</span>
            </label>

            <div class="input-box" :class="{ 'input-error': passwordError }">
              <i class="bi bi-shield-lock-fill"></i>

              <input
                v-model.trim="password"
                type="password"
                placeholder="••••••••"
                @blur="validatePassword"
              />

              <div class="input-line"></div>
            </div>

            <transition name="shake">
              <p v-if="passwordError" class="error-hint">
                <i class="fas fa-exclamation-circle"></i>
                {{ passwordError }}
              </p>
            </transition>
          </div>

          <div
            class="d-flex justify-content-between align-items-center mb-5 px-1"
          >
            <div class="checkbox-royal">
              <input type="checkbox" id="remember" v-model="rememberMe" />
              <label for="remember" class="ms-2">Ghi nhớ phiên làm việc</label>
            </div>
            <a class="forgot-gold" @click.prevent="openForgotPassword" href="#"
              >Quên mật khẩu?</a
            >
          </div>

          <button
            class="btn-royal-submit w-100"
            type="submit"
            :disabled="isLoading"
          >
            <span v-if="!isLoading" class="btn-text">XÁC THỰC TRUY CẬP</span>
            <div
              v-else
              class="spinner-border spinner-border-sm text-light"
            ></div>
            <div class="btn-light-sweep"></div>
          </button>
        </form>

        <div class="text-center mt-5">
          <p class="security-note">
            <i class="bi bi-incognito me-2"></i>
            Hệ thống giám sát bảo mật 24/7
          </p>
        </div>
      </div>
    </div>
    <Transition name="fade">
      <div
        v-if="step > 0"
        class="modal-overlay"
        @click.self="closeForgotPassword"
      >
        <div class="modal-card">
          <!-- STEP 1: Tài khoản + Email -->
          <div v-if="step === 1">
            <div class="modal-header-custom">
              <div class="modal-icon-wrap">
                <i class="ri-shield-user-line"></i>
              </div>
              <h5>Quên mật khẩu</h5>
              <p>Nhập tài khoản và email để nhận mã OTP</p>
            </div>
            <div class="modal-input-group mb-3">
              <i class="ri-user-line"></i>
              <input
                v-model="fpSoDienThoai"
                type="text"
                placeholder="Số điện thoại"
                @keyup.enter="handleSendOtp"
              />
            </div>
            <div class="modal-input-group">
              <i class="ri-mail-line"></i>
              <input
                v-model="fpEmail"
                type="email"
                placeholder="Địa chỉ email"
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
              <p>
                Nhập mật khẩu mới cho tài khoản
                <strong>{{ fpEmail }}</strong>
              </p>
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

.login-luxury-wrapper {
  position: relative;
  min-height: 100vh;
  background: url("@/assets/images/banner1.jpg") center center no-repeat;
  background-size: cover;
  font-family: "Montserrat", sans-serif;
}

.overlay-dark {
  position: absolute;
  inset: 0;
  background: radial-gradient(
    circle,
    rgba(0, 0, 0, 0.4) 0%,
    rgba(0, 0, 0, 0.85) 100%
  );
  z-index: 0;
}

/* CARD ĐĂNG NHẬP HỆ HOÀNG GIA */
.login-card-royal {
  background: rgba(255, 255, 255, 0.9); /* Nền trắng sứ mờ */
  backdrop-filter: blur(15px);
  width: 100%;
  max-width: 550px;
  padding: 60px;
  border-radius: 4px; /* Bo góc nhẹ cực sang */
  border: 1px solid #d4af37; /* Viền Gold mỏng */
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.5);
  position: relative;
  overflow: hidden;
  z-index: 1;
}

/* TIÊU ĐỀ */
.title-gold {
  color: #cf3a3a;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 2px;
  margin: 0;
  font-size: 1.8rem;
}

.subtitle-white {
  color: #dc2e2e;
  font-weight: 700;
  letter-spacing: 5px;
  font-size: 0.9rem;
  margin-top: 5px;
}

/* INPUT STYLE CỦA ÔNG ANH */
.label-black {
  display: block;
  font-weight: 700;
  color: #000;
  text-transform: uppercase;
  font-size: 0.75rem;
  margin-bottom: 10px;
  letter-spacing: 1px;
}

.input-box {
  position: relative;
  background: #f9f9f9;
  border-radius: 4px;
}

.input-box i {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #e03434;
  font-size: 1.2rem;
}

.input-box input {
  width: 100%;
  padding: 15px 15px 15px 50px;
  border: 1px solid #eee;
  background: transparent;
  font-weight: 600;
  transition: 0.3s;
}

.input-line {
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: #d4af37;
  transform: translateX(-50%);
  transition: 0.4s ease;
}

.input-box input:focus + .input-line {
  width: 100%;
}

.input-box input:focus {
  outline: none;
  background: #fff;
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
/* CHECKBOX & FORGOT */
.checkbox-royal label {
  font-weight: 600;
  color: #444;
  font-size: 0.9rem;
  cursor: pointer;
}

.forgot-gold {
  color: #8b1e1e;
  text-decoration: none;
  font-weight: 700;
  font-size: 0.9rem;
}

/* NÚT BẤM ÁNH SÁNG CHẠY NGANG */
.btn-royal-submit {
  background: #d32828;
  color: #fff;
  border: none;
  padding: 18px;
  font-weight: 800;
  letter-spacing: 2px;
  position: relative;
  overflow: hidden;
  transition: 0.4s;
  box-shadow: 0 10px 20px rgba(139, 30, 30, 0.3);
}

.btn-royal-submit:hover {
  background: #d52c2c;
  transform: translateY(-3px);
  box-shadow: 0 15px 30px rgba(139, 30, 30, 0.4);
}

.btn-light-sweep {
  position: absolute;
  top: 0;
  left: -150%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    to right,
    transparent,
    rgba(255, 255, 255, 0.3),
    transparent
  );
  transform: skewX(-25deg);
  transition: 0.6s ease-in-out;
}

.btn-royal-submit:hover .btn-light-sweep {
  left: 150%;
}

/* LOGO */
.main-logo-luxury {
  width: 180px;
  filter: drop-shadow(0 0 10px rgba(0, 0, 0, 0.1));
}

.security-note {
  color: #999;
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 1px;
}

.logo-container {
  display: flex;
  justify-content: center;
}

.main-logo-luxury {
  width: 180px;
  display: block;
}

/* ===== RESPONSIVE ===== */
@media (max-width: 576px) {
  .login-card-royal {
    padding: 30px 16px;
  }

  .title-gold {
    font-size: 1.4rem;
  }

  .main-logo-luxury {
    width: 110px;
  }
}

.close-btn {
  position: absolute;
  top: 12px;
  right: 14px;
  width: 36px;
  height: 36px;

  display: flex;
  align-items: center;
  justify-content: center;

  font-size: 18px;
  font-weight: 900;
  color: #888;

  border-radius: 50%;
  cursor: pointer;
  transition: 0.2s;
  z-index: 10;
}

.close-btn:hover {
  background: rgba(0, 0, 0, 0.08);
  color: #d32828;
  transform: scale(1.1);
}

.required {
  color: #d32828;
  font-weight: 900;
}

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
  color: #d32828;
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
  border-color: #d32828;
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
  color: #d32828;
}
.resend-text {
  font-size: 0.82rem;
  color: #888;
  text-align: center;
  margin-bottom: 20px;
}
.resend-link {
  color: #d32828;
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
  border-color: #d32828;
  color: #d32828;
}
.btn-modal-submit {
  flex: 2;
  padding: 11px;
  background: #d32828;
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
