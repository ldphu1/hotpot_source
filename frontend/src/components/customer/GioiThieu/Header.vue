<script setup>
import { ref, onMounted } from "vue";
import router from "@/router";
import logo from "@/assets/icons/logodatn.png";
import { logoutCustomer } from "@/services/authService";
//import main from "@/assets/icons/main.png";
const token = ref(null);
const name = ref("");

onMounted(() => {
  token.value = localStorage.getItem("token");
  name.value = localStorage.getItem("name");
});

const isCollapseOpen = ref(false);
const showRoleModal = ref(false);

const logout = async () => {
  const refreshToken = localStorage.getItem("refreshToken");

  try {
    if (refreshToken) {
      await logoutCustomer(refreshToken);
    }
  } catch (e) {
    // dù lỗi vẫn tiếp tục xóa local, không chặn user
  }

  localStorage.removeItem("token");
  localStorage.removeItem("refreshToken");
  localStorage.removeItem("name");
  localStorage.removeItem("email");
  localStorage.removeItem("soDienThoai");
  localStorage.removeItem("avatar");
  localStorage.removeItem("role");

  localStorage.removeItem("chatSessionId");
  localStorage.removeItem("idCuocTroChuyenHienTai");
  localStorage.removeItem("thong-tin-khach-hang");
  window.dispatchEvent(new Event("auth-changed"));

  token.value = null;
  name.value = "";

  router.replace("/");
};

const goLogin = () => {
  router.push("/dang-nhap");
};
</script>

<template>
  <nav
    class="navbar navbar-expand-lg navbar-dark bg-danger fixed-top custom-nav"
  >
    <div class="container">
      <router-link
        to="/"
        class="navbar-brand brand-logo d-flex align-items-center luxury-header-brand"
      >
        <div class="logo-container-luxury">
          <img :src="logo" alt="logo" class="brand-img" />
          <div class="frame-border"></div>
        </div>

        <span class="brand-name-luxury">FSH</span>
      </router-link>
      <button
        class="navbar-toggler"
        type="button"
        @click="isCollapseOpen = !isCollapseOpen"
      >
        <span class="navbar-toggler-icon"></span>
      </button>

      <div
        class="navbar-collapse d-flex"
        :class="{ show: isCollapseOpen }"
        id="navbarContent"
      >
        <ul class="navbar-nav mx-auto">
          <li class="nav-item">
            <router-link
              to="/"
              class="nav-link"
              exact-active-class="router-link-active"
            >
              TRANG CHỦ
              <div class="nav-light-sweep"></div>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/gioi-thieu" class="nav-link">
              GIỚI THIỆU
              <div class="nav-light-sweep"></div>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/thuc-don" class="nav-link">
              THỰC ĐƠN
              <div class="nav-light-sweep"></div>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/khuyen-mai" class="nav-link">
              KHUYẾN MẠI
              <div class="nav-light-sweep"></div>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/tin-tuc" class="nav-link">
              TIN TỨC
              <div class="nav-light-sweep"></div>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/thong-tin-dat-ban" class="nav-link">
              ĐẶT BÀN NGAY
              <div class="nav-light-sweep"></div>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/quy-dinh" class="nav-link">
              QUY ĐỊNH
              <div class="nav-light-sweep"></div>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/tra-cuu" class="nav-link">
              TRA CỨU
              <div class="nav-light-sweep"></div>
            </router-link>
          </li>
        </ul>

        <div class="d-flex align-items-center gap-3 mt-3 mt-lg-0">
          <template v-if="!token">
            <div class="auth-buttons d-flex gap-2">
              <button class="btn btn-login" @click="goLogin()">
                Đăng nhập
              </button>
              <router-link to="/dang-ky" class="btn btn-register"
                >Đăng ký</router-link
              >
            </div>
          </template>
          <div v-else class="user-box" @click="router.push('/tai-khoan')">
            <i class="bi bi-person-circle user-icon"></i>
            <span class="user-name">{{ name }}</span>

            <button class="logout-btn" @click.stop="logout">
              <i class="bi bi-box-arrow-right"></i>
            </button>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<style scoped>
.user-box {
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(255, 255, 255, 0.15);
  padding: 6px 10px 6px 12px;
  border-radius: 999px;
  backdrop-filter: blur(10px);
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-box:hover {
  background: rgba(255, 255, 255, 0.25);
}

/* icon user */
.user-icon {
  font-size: 22px;
  color: white;
}

/* tên */
.user-name {
  color: white;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}

/* nút logout dạng icon */
.logout-btn {
  border: none;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background: #fff;
  color: #dc3545;
  transform: scale(1.1);
}
/* //////////////////////////////// MENU ////////////////// */
.nav-link {
  position: relative;
  display: inline-flex !important;
  flex-direction: column;
  align-items: center;
  padding: 10px 20px !important;
  color: #fff !important;
  font-weight: 700;
  letter-spacing: 1px;
  overflow: hidden;
  transition: all 0.3s ease;
}
.navbar .nav-link {
  font-size: 14px !important;
}
.nav-link::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: #d4af37;
  transform: translateX(-50%);
  transition: width 0.4s cubic-bezier(0.23, 1, 0.32, 1);
  box-shadow: 0 0 8px rgba(212, 175, 55, 0.6);
}

.nav-link::before {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 1px; /* Mảnh hơn thanh gold */
  background: #fff;
  transform: translateX(-50%);
  z-index: 2;
  opacity: 0.8;
  transition: width 0.6s cubic-bezier(0.23, 1, 0.32, 1);
}

.nav-link:hover {
  color: #fff !important;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
}

.nav-link:hover::after {
  width: 80%;
}

.nav-link:hover::before {
  width: 50%;
}

.nav-light-sweep {
  position: absolute;
  top: 0;
  left: -150%;
  width: 50px;
  height: 100%;
  background: linear-gradient(
    to right,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.4) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  transform: skewX(-25deg);
  pointer-events: none;
}

.nav-link:hover .nav-light-sweep {
  left: 150%;
  transition: 0.7s ease-in-out;
}

.router-link-active {
  color: #ffffff !important;
}

.router-link-active::after {
  width: 80%;
}

.router-link-active::before {
  width: 50%;
}
/* //////////////////////////////// MENU ////////////////// */
/* ///////////////////// LOGO ////////////// */
.luxury-header-brand {
  text-decoration: none;
  gap: 15px;
}

.logo-box {
  position: relative;
  padding: 5px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-main {
  height: 65px;
  width: auto;
  z-index: 2;
  filter: drop-shadow(0 0 10px rgba(255, 255, 255, 0.4));
  transition: all 0.5s ease;
}

.logo-frame {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border: 1px solid rgba(212, 175, 55, 0.5); /* viền vàng ngoài */
  z-index: 1;
}

.logo-frame::before {
  content: "";
  position: absolute;
  top: 3px;
  left: 3px;
  right: 3px;
  bottom: 3px;
  border: 1px solid #fff; /* viền trắng trong */
  opacity: 0.8;
}

.brand-name-luxury {
  font-family: "playfair display", serif;
  font-weight: 900;
  font-size: 28px;
  letter-spacing: 3px;
  color: #fff;
  text-shadow: 0 0 5px #fff, 0 0 10px #d4af37, 0 0 20px #d4af37,
    2px 2px 2px rgba(0, 0, 0, 0.8);
  transition: all 0.4s ease;
}

.luxury-header-brand:hover .logo-main {
  transform: scale(1.1) rotate(5deg);
}

.luxury-header-brand:hover .brand-name-luxury {
  color: #d4af37;
  text-shadow: 0 0 10px #fff, 0 0 20px #d4af37, 0 0 40px #d4af37;
  letter-spacing: 5px;
}

.luxury-header-brand::after {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(
    to right,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.3) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  transform: skewX(-25deg);
  transition: 0.8s;
}

.luxury-header-brand:hover::after {
  left: 150%;
}

.logo-container-luxury {
  position: relative;
  width: 75px;
  height: 75px;
  background: radial-gradient(circle at 35% 35%, #ffffff 0%, #f2f2f2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
  margin-right: 15px;
  z-index: 1;
  transition: all 0.4s ease;
  overflow: hidden;
}

.brand-img {
  height: 70px;
  width: auto;
  z-index: 3;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.frame-border {
  position: absolute;
  top: 4px;
  left: 4px;
  right: 4px;
  bottom: 4px;
  border: 1.5px solid #d4af37;
  border-radius: 50%;
  z-index: 2;
}

.frame-border::after {
  content: "";
  position: absolute;
  top: 2px;
  left: 2px;
  right: 2px;
  bottom: 2px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 50%;
}

.brand-name-luxury {
  font-size: 2.5rem;
  font-weight: 900;
  color: #fff;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.8), 0 0 20px #d4af37;
  letter-spacing: 2px;
  transition: all 0.3s ease;
}

.luxury-header-brand:hover .logo-container-luxury {
  transform: scale(1.1);
  box-shadow: 0 0 20px rgba(212, 175, 55, 0.5);
}

.luxury-header-brand:hover .brand-name-luxury {
  color: #d4af37;
  text-shadow: 0 0 15px #fff;
  letter-spacing: 4px;
}

/* ///////////////////////////////////// */

.role-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(59, 58, 58, 0.85);
  backdrop-filter: blur(8px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.role-modal {
  background: #ffffff;
  padding: 40px 30px;
  border-radius: 2px;
  width: 380px;
  text-align: center;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  border: 1px solid rgba(0, 0, 0, 0.05);
  animation: slideUpFade 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.modal-header-luxury h5 {
  font-weight: 300;
  letter-spacing: 3px;
  font-size: 1.1rem;
  margin-bottom: 8px;
  color: #1a1a1a;
}

.modal-header-luxury p {
  font-size: 0.85rem;
  color: #888;
  margin-bottom: 30px;
}

.role-options {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

/* Nút bấm thiết kế tối giản */
.btn-luxury {
  position: relative;
  padding: 15px;
  border: none;
  font-size: 0.9rem;
  letter-spacing: 1px;
  transition: all 0.3s ease;
  cursor: pointer;
  text-transform: uppercase;
}

.btn-luxury.primary {
  background: #dd3636;
  color: white;
}

.btn-luxury.outline {
  background: transparent;
  border: 1px solid #e0e0e0;
  color: #1a1a1a;
}

.btn-luxury:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(231, 93, 93, 0.1);
}

.btn-luxury.primary:hover {
  background: #c33d3d;
}

.btn-luxury.outline:hover {
  border-color: #1a1a1a;
}

.btn-close-minimal {
  margin-top: 25px;
  background: none;
  border: none;
  color: #999;
  font-size: 0.8rem;
  text-decoration: underline;
  text-underline-offset: 4px;
  cursor: pointer;
}

@keyframes slideUpFade {
  from {
    transform: translateY(30px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
/* ----------------------------- */
.brand-logo {
  font-family: "Poppins", sans-serif;
  font-weight: 600;
  letter-spacing: 1px;
  transition: all 0.3s ease;
}

.brand-logo:hover {
  letter-spacing: 2px;
  transform: scale(1.05);
}

.custom-nav {
  backdrop-filter: blur(15px) saturate(180%);
  -webkit-backdrop-filter: blur(15px) saturate(180%);
  background-color: rgba(220, 53, 69, 0.8) !important; /* đỏ trong suốt */
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

@media (max-width: 991.98px) {
  /* hiệu ứng menu đổ xuống như một tấm kính bay */
  .navbar-collapse {
    background: linear-gradient(
      145deg,
      rgba(220, 53, 69, 0.95),
      rgba(165, 29, 42, 0.98)
    );
    border-radius: 24px; /* bo góc cực lớn */
    margin: 15px;
    padding: 25px;
    border: 1px solid rgba(255, 255, 255, 0.2);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  }

  /* các mục menu sắp xếp thoáng đãng */
  .nav-item {
    text-align: center;
    margin-bottom: 10px;
  }

  .nav-link {
    font-size: 1rem !important;
    letter-spacing: 1.5px;
    text-transform: uppercase;
    padding: 12px !important;
    border-radius: 12px;
    transition: all 0.3s ease;
  }

  .nav-link:active {
    background: rgba(255, 255, 255, 0.1);
    transform: scale(0.95);
  }

  .nav-link.router-link-active::after {
    display: none;
  }

  .nav-link.router-link-active {
    background: rgba(255, 255, 255, 0.2);
    color: #fff !important;
  }

  /* khu vực user/nút bấm trên mobile */
  .d-flex.align-items-center.gap-3 {
    flex-direction: column;
    width: 100%;
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
  }

  .auth-buttons {
    width: 100%;
    flex-direction: column;
  }

  .btn-login,
  .btn-register {
    width: 100%;
    padding: 14px !important;
    font-size: 1rem;
  }

  /* user info sau khi login trên mobile */
  .user-info {
    flex-direction: row !important;
    background: rgba(0, 0, 0, 0.2);
    padding: 10px 20px;
    border-radius: 50px;
    width: fit-content;
  }

  .user-name {
    font-size: 14px;
    margin: 0 15px;
  }

  .logout-btn {
    background: #fff;
    color: #dc3545;
    padding: 5px 12px;
    border-radius: 20px;
    font-weight: 700;
  }

  /* nút hamburger (nếu bạn dùng nút mặc định của bootstrap) */
  .navbar-toggler {
    border: none !important;
    padding: 0;
  }

  .navbar-toggler:focus {
    box-shadow: none;
  }
}

.brand-logo {
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  transition: transform 0.3s ease;
}

.brand-logo:active {
  transform: scale(0.9);
}
.logout-btn {
  background: none;
  border: none;
  color: white;
  font-size: 12px;
  cursor: pointer;
}

.logout-btn:hover {
  text-decoration: underline;
}
.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: white;
  cursor: pointer;
}

.user-icon {
  font-size: 26px;
}

.user-name {
  font-size: 12px;
  font-weight: 600;
}
/* Tổng thể Navbar */
.custom-nav {
  height: 80px;
  background-color: rgba(220, 53, 69, 0.95) !important;
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.brand-logo {
  font-size: 1.4rem;
  letter-spacing: -0.5px;
}

/* Nút bấm hiện đại */
.contact-link {
  text-decoration: none;
  font-weight: 600;
  font-size: 0.95rem;
}

.btn {
  padding: 8px 20px;
  border-radius: 50px; /* Bo tròn hoàn toàn kiểu 2026 */
  font-size: 0.9rem;
  font-weight: 600;
  transition: all 0.3s transform;
}

.btn:active {
  transform: scale(0.95);
}

.btn-login {
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.4);
  background: transparent;
}

.btn-login:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: white;
  color: white;
}

.btn-register {
  background: white;
  color: #dc3545; /* Màu đỏ trùng tông nền */
  border: none;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.btn-register:hover {
  background: #f8f9fa;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  color: #c82333;
}

/* CUSTOM RESPONSIVE CSS - ADDED TO PREVENT CONFLICTS AND FIX SCREEN LAYOUTS */
@media (max-width: 991.98px) {
  /* Fix Bootstrap collapse not hiding when d-flex is present */
  .navbar-collapse:not(.show) {
    display: none !important;
  }

  .navbar-collapse {
    flex-direction: column !important;
    align-items: stretch !important;
  }

  .navbar-nav {
    width: 100% !important;
    flex-direction: column !important;
    text-align: center !important;
  }

  .custom-nav {
    height: auto !important;
    min-height: 70px;
    padding: 10px 0;
  }

  .logo-container-luxury {
    width: 50px !important;
    height: 50px !important;
    margin-right: 10px !important;
  }

  .brand-img {
    height: 45px !important;
  }

  .brand-name-luxury {
    font-size: 1.6rem !important;
    letter-spacing: 1.5px !important;
  }

  .user-box {
    padding: 6px 12px !important;
    width: fit-content;
    margin: 10px auto 0 auto;
  }
}

@media (max-width: 576px) {
  .custom-nav {
    min-height: 65px;
  }

  .logo-container-luxury {
    width: 45px !important;
    height: 45px !important;
  }

  .brand-img {
    height: 40px !important;
  }

  .brand-name-luxury {
    font-size: 1.4rem !important;
  }
}
</style>
