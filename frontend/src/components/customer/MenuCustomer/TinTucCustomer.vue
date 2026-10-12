<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import bannerLau from "@/assets/images/banner-lau.png";
import banner1 from "@/assets/images/banner1.jpg";
import banner2 from "@/assets/images/banner1.png";
import banner3 from "@/assets/images/banner2.png";
import banner4 from "@/assets/images/banner3.png";
import khuyenmai1 from "@/assets/images/khuyenmai1.png";
import khuyenmai2 from "@/assets/images/khuyenmai2.png";

// ==================== SCROLL REVEAL (4 CORNERS) ====================
const revealElements = ref([]);

const handleReveal = () => {
  revealElements.value.forEach((el) => {
    const rect = el.getBoundingClientRect();
    if (rect.top < window.innerHeight - 80) {
      el.classList.add("revealed");
    }
  });
};

// ==================== PARALLAX ====================
const heroParallax = ref(null);
const handleParallax = () => {
  if (heroParallax.value) {
    const scrolled = window.scrollY;
    heroParallax.value.style.transform = `translateY(${scrolled * 0.4}px)`;
  }
  handleReveal();
};

// ==================== LIFECYCLE ====================
onMounted(() => {
  revealElements.value = Array.from(
    document.querySelectorAll(".reveal-on-scroll"),
  );
  
  const dirs = ['tl', 'tr', 'bl', 'br'];
  revealElements.value.forEach(el => {
    if (!el.className.includes('reveal-t') && !el.className.includes('reveal-b')) {
      const dir = dirs[Math.floor(Math.random() * dirs.length)];
      el.classList.add('reveal-' + dir);
    }
  });

  window.addEventListener("scroll", handleParallax);
  handleReveal();

  createParticles();
});

onUnmounted(() => {
  window.removeEventListener("scroll", handleParallax);
});

// ==================== PARTICLES ====================
const createParticles = () => {
  const container = document.querySelector(".particles-container");
  if (!container) return;
  for (let i = 0; i < 20; i++) {
    const p = document.createElement("div");
    p.className = "particle";
    p.style.cssText = `
      left: ${Math.random() * 100}%;
      animation-delay: ${Math.random() * 5}s;
      animation-duration: ${4 + Math.random() * 6}s;
      width: ${4 + Math.random() * 6}px;
      height: ${4 + Math.random() * 6}px;
      opacity: ${0.3 + Math.random() * 0.5};
    `;
    container.appendChild(p);
  }
};

// ==================== DUMMY DATA ====================
const featuredNews = {
  id: 1,
  title: "Ra Mắt Set Lẩu Thái Thượng Hạng Dành Riêng Cho Mùa Lễ Hội",
  excerpt: "Khám phá hương vị lẩu Thái chuẩn vị hoàng gia với các nguyên liệu cao cấp nhất được nhập khẩu trực tiếp, mang đến trải nghiệm không thể quên.",
  image: banner1,
  date: "20/11/2026",
  category: "Tin Nổi Bật"
};

const subFeaturedNews = [
  {
    id: 2,
    title: "Không Gian Mới - Trải Nghiệm Mới Tại Four Spice",
    date: "15/11/2026",
    image: banner2,
    category: "Sự Kiện"
  },
  {
    id: 3,
    title: "Bí Quyết Tạo Nên Nồi Lẩu 4 Vị Độc Quyền",
    date: "10/11/2026",
    image: banner3,
    category: "Khám Phá"
  }
];

const latestNews = [
  {
    id: 4,
    title: "Top 5 Món Nhúng Lẩu Không Thể Bỏ Qua Tại Nhà Hàng",
    excerpt: "Thịt bò Wagyu, hải sản tươi sống và nhiều nguyên liệu khác đang chờ bạn thưởng thức.",
    date: "05/11/2026",
    image: banner4,
    category: "Ẩm Thực"
  },
  {
    id: 5,
    title: "Chương Trình Đón Giáng Sinh: Tặng Tráng Miệng Băng Giá",
    excerpt: "Hòa chung không khí lễ hội, Four Spice dành tặng hàng ngàn phần quà đặc biệt.",
    date: "01/11/2026",
    image: khuyenmai1,
    category: "Khuyến Mãi"
  },
  {
    id: 6,
    title: "Tri Ân Khách Hàng - Tích Điểm Đổi Quà Cuối Năm",
    excerpt: "Chương trình membership mới với nhiều ưu đãi hấp dẫn dành cho khách hàng thân thiết.",
    date: "25/10/2026",
    image: khuyenmai2,
    category: "Sự Kiện"
  }
];
</script>

<template>
  <main class="news-page">
    <!-- ====================== HERO SECTION ====================== -->
    <section class="hero-gt position-relative overflow-hidden">
      <div class="hero-bg-parallax" ref="heroParallax">
        <img :src="bannerLau" alt="Tin tức Four Spice" class="hero-img-bg" />
      </div>
      <div class="hero-gt-overlay"></div>
      <div class="particles-container"></div>

      <div class="hero-gt-content container">
        <div class="hero-badge reveal-tl">
          <span class="badge-dot"></span>
          <span>Bản Tin & Sự Kiện</span>
          <span class="badge-dot"></span>
        </div>
        <h1 class="hero-gt-title reveal-tr">
          <span class="title-line-1">TIN TỨC</span>
          <span class="title-line-2">MỚI NHẤT</span>
        </h1>
        <p class="hero-gt-sub reveal-bl">
          Cập nhật những thông tin, sự kiện và khuyến mãi hấp dẫn nhất<br />
          từ hệ thống nhà hàng Four Spice Hotpot
        </p>
        <div class="hero-divider reveal-br">
          <span class="divider-line"></span>
          <span class="divider-icon">✦</span>
          <span class="divider-line"></span>
        </div>
      </div>
    </section>

    <!-- ====================== FEATURED NEWS ====================== -->
    <section class="section-featured py-5">
      <div class="container mt-4">
        <div class="row g-4">
          <!-- Main Feature -->
          <div class="col-lg-7 reveal-on-scroll">
            <div class="news-card main-feature feature-card-royal">
              <div class="card-shine"></div>
              <div class="royal-border"></div>
              
              <div class="news-img-wrapper">
                <img :src="featuredNews.image" :alt="featuredNews.title" />
                <div class="news-category">{{ featuredNews.category }}</div>
              </div>
              
              <div class="news-content">
                <div class="news-date">
                  <i class="bi bi-calendar-event me-2"></i>{{ featuredNews.date }}
                </div>
                <h3 class="news-title">{{ featuredNews.title }}</h3>
                <p class="news-excerpt">{{ featuredNews.excerpt }}</p>
                <button class="luxury-read-btn">
                  <span>ĐỌC TIẾP</span>
                  <i class="bi bi-arrow-right"></i>
                </button>
              </div>
            </div>
          </div>

          <!-- Sub Features -->
          <div class="col-lg-5 d-flex flex-column gap-4">
            <div class="news-card sub-feature feature-card-royal reveal-on-scroll" v-for="item in subFeaturedNews" :key="item.id">
              <div class="royal-border"></div>
              <div class="row g-0 h-100">
                <div class="col-5">
                  <div class="news-img-wrapper h-100">
                    <img :src="item.image" :alt="item.title" class="h-100 object-fit-cover" />
                    <div class="news-category small">{{ item.category }}</div>
                  </div>
                </div>
                <div class="col-7">
                  <div class="news-content">
                    <div class="news-date">
                      <i class="bi bi-calendar-event me-2"></i>{{ item.date }}
                    </div>
                    <h4 class="news-title small">{{ item.title }}</h4>
                    <button class="luxury-read-btn mt-3">
                      <span>ĐỌC TIẾP</span>
                      <i class="bi bi-arrow-right"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ====================== LATEST NEWS GRID ====================== -->
    <section class="section-grid py-5 bg-light-pattern">
      <div class="container">
        <div class="text-center mb-5 reveal-on-scroll">
          <div class="section-eyebrow">
            <span class="eyebrow-line"></span>
            <span>TẤT CẢ BÀI VIẾT</span>
            <span class="eyebrow-line"></span>
          </div>
          <h2 class="values-title">Khám Phá <span class="text-highlight">Thêm</span></h2>
        </div>

        <div class="row g-4">
          <div class="col-md-6 col-lg-4 reveal-on-scroll" v-for="item in latestNews" :key="item.id">
            <div class="news-card grid-card feature-card-royal">
              <div class="card-shine"></div>
              <div class="royal-border"></div>
              
              <div class="news-img-wrapper">
                <img :src="item.image" :alt="item.title" />
                <div class="news-category">{{ item.category }}</div>
              </div>
              
              <div class="news-content">
                <div class="news-date">
                  <i class="bi bi-calendar-event me-2"></i>{{ item.date }}
                </div>
                <h4 class="news-title">{{ item.title }}</h4>
                <p class="news-excerpt">{{ item.excerpt }}</p>
                <div class="text-center mt-4">
                  <button class="luxury-read-btn w-100 justify-content-center">
                    <span>CHI TIẾT</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Pagination -->
        <div class="d-flex justify-content-center mt-5 reveal-on-scroll">
          <ul class="pagination-custom d-flex gap-2 m-0 p-0">
            <li class="page-item-custom disabled">
              <button class="page-link-custom"><i class="bi bi-chevron-left"></i></button>
            </li>
            <li class="page-item-custom active">
              <button class="page-link-custom">1</button>
            </li>
            <li class="page-item-custom">
              <button class="page-link-custom">2</button>
            </li>
            <li class="page-item-custom">
              <button class="page-link-custom">3</button>
            </li>
            <li class="page-item-custom">
              <button class="page-link-custom"><i class="bi bi-chevron-right"></i></button>
            </li>
          </ul>
        </div>
      </div>
    </section>

    <!-- ====================== NEWSLETTER ====================== -->
    <section class="section-newsletter py-5">
      <div class="newsletter-bg"></div>
      <div class="container position-relative">
        <div class="row justify-content-center">
          <div class="col-lg-8 text-center reveal-on-scroll">
            <div class="newsletter-box feature-card-royal">
              <div class="royal-border"></div>
              <i class="bi bi-envelope-paper newsletter-icon"></i>
              <h2 class="values-title text-white">Đăng Ký Nhận <span class="text-highlight-gold">Tin Tức</span></h2>
              <p class="text-white-50 mb-4">Để lại email của bạn để không bỏ lỡ bất kỳ chương trình khuyến mãi và sự kiện đặc biệt nào từ Four Spice.</p>
              
              <div class="newsletter-form">
                <input type="email" class="luxury-input" placeholder="Nhập địa chỉ email của bạn..." />
                <button class="luxury-submit-btn">XÁC NHẬN</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </main>
</template>

<style scoped>
/* =================== IMPORT FONTS =================== */
@import url("https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,700;0,800;0,900;1,400&family=Inter:wght@300;400;500;600;700&display=swap");

/* =================== BASE =================== */
.news-page {
  font-family: "Inter", sans-serif;
  overflow-x: hidden;
  background: #fff;
}

/* =================== HERO SECTION =================== */
.hero-gt {
  height: 60vh;
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.hero-bg-parallax {
  position: absolute;
  inset: -20%;
  z-index: 0;
}

.hero-img-bg {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero-gt-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    170deg,
    rgba(10, 2, 2, 0.8) 0%,
    rgba(100, 15, 15, 0.7) 50%,
    rgba(0, 0, 0, 0.85) 100%
  );
  z-index: 1;
}

.particles-container {
  position: absolute;
  inset: 0;
  z-index: 2;
  pointer-events: none;
  overflow: hidden;
}

.hero-gt-content {
  position: relative;
  z-index: 3;
  text-align: center;
  color: #fff;
  padding-top: 50px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  background: rgba(212, 175, 55, 0.15);
  border: 1px solid rgba(212, 175, 55, 0.4);
  padding: 8px 20px;
  border-radius: 2px;
  font-size: 0.78rem;
  letter-spacing: 3px;
  text-transform: uppercase;
  color: #d4af37;
  margin-bottom: 25px;
}

.badge-dot {
  width: 5px;
  height: 5px;
  background: #d4af37;
  border-radius: 50%;
}

.hero-gt-title {
  margin-bottom: 20px;
  line-height: 1;
}

.title-line-1 {
  display: block;
  font-family: "Times New Roman", Times, serif;
  font-size: clamp(2.5rem, 6vw, 5rem);
  font-weight: 900;
  color: #ffffff;
  letter-spacing: 10px;
  text-shadow: 0 0 40px rgba(228, 50, 50, 0.4);
}

.title-line-2 {
  display: block;
  font-family: "Times New Roman", Times, serif;
  font-size: clamp(1.2rem, 3vw, 2.5rem);
  font-weight: 400;
  color: #d4af37;
  letter-spacing: 20px;
  text-transform: uppercase;
}

.hero-gt-sub {
  font-family: "Times New Roman", Times, serif;
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.8;
  margin-bottom: 30px;
  font-weight: 300;
  letter-spacing: 0.5px;
}

.hero-divider {
  display: flex;
  align-items: center;
  gap: 15px;
  justify-content: center;
}

.divider-line {
  width: 60px;
  height: 1px;
  background: linear-gradient(90deg, transparent, #d4af37, transparent);
}

.divider-icon {
  color: #d4af37;
  font-size: 1.2rem;
}

/* =================== PARTICLES =================== */
:deep(.particle) {
  position: absolute;
  bottom: -10px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(212, 175, 55, 0.8), rgba(228, 50, 50, 0.4));
  animation: particleFloat linear infinite;
  pointer-events: none;
}

@keyframes particleFloat {
  0% { transform: translateY(0) scale(1); opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 0.5; }
  100% { transform: translateY(-100vh) scale(0); opacity: 0; }
}

/* =================== 4-CORNER REVEAL =================== */
.reveal-on-scroll {
  opacity: 0;
  transition: all 1s cubic-bezier(0.23, 1, 0.32, 1);
}

.reveal-on-scroll.reveal-tl { transform: translate(-80px, -80px); }
.reveal-on-scroll.reveal-tr { transform: translate(80px, -80px); }
.reveal-on-scroll.reveal-bl { transform: translate(-80px, 80px); }
.reveal-on-scroll.reveal-br { transform: translate(80px, 80px); }

.reveal-on-scroll.revealed {
  opacity: 1;
  transform: translate(0, 0);
}

/* =================== TEXT HIGHLIGHTS =================== */
.section-eyebrow {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: center;
  font-size: 0.72rem;
  letter-spacing: 4px;
  text-transform: uppercase;
  color: #e63a3f;
  font-weight: 700;
  margin-bottom: 15px;
}

.eyebrow-line {
  flex: 1;
  max-width: 60px;
  height: 1px;
  background: linear-gradient(90deg, transparent, #e63a3f);
}

.values-title {
  font-family: "Playfair Display", serif;
  font-size: clamp(1.6rem, 3vw, 2.5rem);
  font-weight: 800;
  color: #1a1a1a;
  margin-bottom: 10px;
}

.text-highlight {
  background: linear-gradient(45deg, #a31d21, #e63a3f);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.text-highlight-gold {
  background: linear-gradient(45deg, #d4af37, #f5d470);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* =================== LUXURY CARD STYLES =================== */
.feature-card-royal {
  position: relative;
  background: #fff;
  border: 1px solid rgba(230, 58, 63, 0.15);
  border-radius: 4px;
  overflow: hidden;
  transition: all 0.5s cubic-bezier(0.23, 1, 0.32, 1);
}

.royal-border {
  position: absolute;
  top: 6px;
  left: 6px;
  right: 6px;
  bottom: 6px;
  border: 1px solid rgba(212, 175, 55, 0.2);
  pointer-events: none;
  z-index: 2;
  transition: all 0.5s ease;
}

.card-shine {
  position: absolute;
  top: 0;
  left: -150%;
  width: 70px;
  height: 100%;
  background: linear-gradient(120deg, rgba(255,255,255,0) 0%, rgba(255,255,255,0.6) 50%, rgba(255,255,255,0) 100%);
  transform: skewX(-25deg);
  transition: 0.6s ease;
  z-index: 3;
  pointer-events: none;
}

.feature-card-royal:hover .card-shine { left: 150%; }

.feature-card-royal:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1), 0 0 20px rgba(212, 175, 55, 0.1);
  border-color: #d4af37;
}

.feature-card-royal:hover .royal-border {
  border-color: rgba(212, 175, 55, 0.6);
  top: 10px;
  left: 10px;
  right: 10px;
  bottom: 10px;
}

/* =================== NEWS CARD SPECIFICS =================== */
.news-img-wrapper {
  position: relative;
  overflow: hidden;
}

.news-img-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
}

.feature-card-royal:hover .news-img-wrapper img {
  transform: scale(1.08);
}

.news-category {
  position: absolute;
  top: 15px;
  left: 15px;
  background: linear-gradient(135deg, #e63a3f, #a31d21);
  color: #fff;
  padding: 6px 15px;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
  border: 1px solid #d4af37;
  z-index: 2;
  box-shadow: 0 4px 10px rgba(0,0,0,0.3);
}

.news-category.small {
  padding: 4px 10px;
  font-size: 0.65rem;
  top: 10px;
  left: 10px;
}

.news-content {
  padding: 25px;
  position: relative;
  z-index: 4;
}

.sub-feature .news-content {
  padding: 15px 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 100%;
}

.news-date {
  font-size: 0.85rem;
  color: #a31d21;
  font-weight: 600;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.news-title {
  font-family: "Playfair Display", serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.4;
  margin-bottom: 15px;
  transition: color 0.3s;
}

.news-title.small {
  font-size: 1.1rem;
  margin-bottom: 10px;
}

.feature-card-royal:hover .news-title {
  color: #e63a3f;
}

.news-excerpt {
  color: #666;
  font-size: 0.95rem;
  line-height: 1.7;
  margin-bottom: 25px;
}

/* =================== BUTTONS =================== */
.luxury-read-btn {
  background: transparent;
  border: none;
  color: #1a1a1a;
  font-size: 0.85rem;
  font-weight: 700;
  letter-spacing: 2px;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 0;
  transition: all 0.3s ease;
  position: relative;
}

.luxury-read-btn i {
  color: #d4af37;
  font-size: 1.2rem;
  transition: transform 0.3s;
}

.luxury-read-btn::after {
  content: "";
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 0;
  height: 2px;
  background: #d4af37;
  transition: width 0.3s ease;
}

.luxury-read-btn:hover {
  color: #d4af37;
}

.luxury-read-btn:hover i {
  transform: translateX(5px);
}

.luxury-read-btn:hover::after {
  width: 100%;
}

/* =================== PAGINATION =================== */
.pagination-custom {
  list-style: none;
}

.page-link-custom {
  width: 45px;
  height: 45px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #eee;
  background: white;
  color: #555;
  border-radius: 50%;
  font-weight: 600;
  font-size: 0.95rem;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  outline: none;
}

.page-link-custom:hover:not(:disabled) {
  background: #fff5f5;
  color: #dc3545;
  border-color: #dc3545;
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(220, 53, 69, 0.2);
}

.page-item-custom.active .page-link-custom {
  background: linear-gradient(45deg, #a31d21, #e63a3f);
  color: white;
  border: none;
  box-shadow: 0 8px 20px rgba(163, 29, 33, 0.3);
  transform: scale(1.1);
}

.page-item-custom.disabled .page-link-custom {
  opacity: 0.4;
  cursor: not-allowed;
  background: #f8f9fa;
}

/* =================== NEWSLETTER =================== */
.section-newsletter {
  position: relative;
  overflow: hidden;
}

.newsletter-bg {
  position: absolute;
  inset: 0;
  background: url("@/assets/images/footer-bg.jpg") center/cover no-repeat;
}

.newsletter-bg::after {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(10, 2, 2, 0.95), rgba(80, 5, 5, 0.9));
}

.newsletter-box {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  padding: 50px;
  border: 1px solid rgba(212, 175, 55, 0.3);
}

.newsletter-icon {
  font-size: 3rem;
  color: #d4af37;
  margin-bottom: 20px;
  display: inline-block;
}

.newsletter-form {
  display: flex;
  gap: 15px;
  max-width: 500px;
  margin: 0 auto;
}

.luxury-input {
  flex: 1;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(212, 175, 55, 0.4);
  color: #fff;
  padding: 12px 20px;
  border-radius: 2px;
  outline: none;
  transition: 0.3s;
}

.luxury-input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.luxury-input:focus {
  border-color: #d4af37;
  background: rgba(255, 255, 255, 0.15);
  box-shadow: 0 0 15px rgba(212, 175, 55, 0.2);
}

.luxury-submit-btn {
  background: linear-gradient(135deg, #e63a3f, #a31d21);
  color: #fff;
  border: 1px solid #d4af37;
  padding: 0 30px;
  font-weight: 700;
  letter-spacing: 2px;
  border-radius: 2px;
  cursor: pointer;
  transition: 0.3s;
}

.luxury-submit-btn:hover {
  background: linear-gradient(135deg, #d4af37, #f5d470);
  color: #1a1a1a;
  border-color: #fff;
}

/* =================== RESPONSIVE =================== */
@media (max-width: 991px) {
  .sub-feature .news-content {
    padding: 15px;
  }
}

@media (max-width: 768px) {
  .newsletter-form {
    flex-direction: column;
  }
  .luxury-submit-btn {
    padding: 15px;
  }
  .news-img-wrapper {
    height: 250px;
  }
}
</style>
