<template>
  <footer class="footer-bg text-white">
    <div class="overlay"></div>

    <div class="container position-relative py-5 text-center">
      <!-- LOGO -->
      <img
        src="@/assets/icons/logodatn.png"
        alt="logo"
        class="mb-3 footer-logo"
      />

      <!-- CONTACT -->
      <div class="mb-3">
        <span class="fw-bold fs-5">0332654198</span>
        <span class="mx-2">|</span>
        <span>fourspicehotpot.vn</span>
      </div>

      <!-- MENU -->
      <ul class="footer-menu mb-4">
        <li>TRANG CHỦ</li>
        <li>GIỚI THIỆU</li>
        <li>KHUYẾN MẠI</li>
        <li>THỰC ĐƠN</li>
        <li>TIN TỨC</li>
        <li>TUYỂN DỤNG</li>
      </ul>

      <!-- SOCIAL -->
      <div class="social mb-4">
        <span class="icon">f</span>
        <span class="icon">◎</span>
        <span class="icon">♫</span>
      </div>

      <hr class="border-light opacity-25" />

      <!-- INFO -->
      <div class="footer-info text-start mx-auto">
        <p>
          Website lẩu thái công ty TNHH 4 thành viên thành lập<br />
          Trụ sở chính: B10, Dự án tổ hợp công trình hỗn hợp số 53 Triều Khúc,
          Thanh Xuân, Hà Nội<br />
          Giấy phép ĐKKD: 0108704192 cấp ngày 25/01/2026<br />
          📞 Phone: 0332654198<br />
          ✉ Contact Email: hieulm24@gmail.com
        </p>
        <p class="mt-3">© 2026 FourSpiceHotpot's Group. All Rights Reserved.</p>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer-bg {
  position: relative;
  background: url("@/assets/images/footer-bg.jpg") center / cover no-repeat;
}

.overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.75);
}

/* LOGO */
.footer-logo {
  width: 140px;
}
.footer-logo {
  width: auto;
  max-width: 220px;
  height: auto;
  display: block;
  margin: 0 auto;
}

/* MENU */
.footer-menu {
  list-style: none;
  padding: 0;
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 20px;
  font-size: 14px;
}

.footer-menu li {
  cursor: pointer;
  transition: 0.3s;
}

.footer-menu li:hover {
  color: #e53935;
}

/* SOCIAL */
.social {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.icon {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: #777;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

/* INFO */
.footer-info {
  max-width: 900px;
  font-size: 14px;
  line-height: 1.7;
}
.footer-logo {
  max-width: 400px;
  width: 100%;
  height: auto;
}

/* CUSTOM ADDITIONAL RESPONSIVE & ZOOM OVERRIDES */
@media (max-width: 768px) {
  .footer-menu {
    gap: 10px 15px !important;
    font-size: 12px !important;
  }
  .footer-info {
    text-align: center !important;
    font-size: 12px !important;
    padding: 0 10px;
  }
  .footer-logo {
    max-width: 150px !important;
  }
}
</style>
