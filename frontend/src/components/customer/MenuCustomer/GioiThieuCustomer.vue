<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import bannerLau from "@/assets/images/banner-lau.png";
import banner1 from "@/assets/images/banner1.jpg";
import banner2 from "@/assets/images/banner1.png";
import banner3 from "@/assets/images/banner2.png";
import banner4 from "@/assets/images/banner3.png";
import bgLau from "@/assets/images/bg-lau.jpg";
import anhlau from "@/assets/images/anhlau.jpg";
import footerBg from "@/assets/images/footer-bg.jpg";
import khach1 from "@/assets/images/sontung.jpg";
import khach2 from "@/assets/images/huanhoahong.jpg";
import khach3 from "@/assets/images/leminho.jpg";

// ==================== SCROLL REVEAL ====================
const revealElements = ref([]);

const handleReveal = () => {
  revealElements.value.forEach((el) => {
    const rect = el.getBoundingClientRect();
    if (rect.top < window.innerHeight - 80) {
      el.classList.add("revealed");
    }
  });
};

// ==================== COUNTER ANIMATION ====================
const stats = ref([
  { value: 0, target: 8, suffix: "+", label: "Năm kinh nghiệm" },
  { value: 0, target: 50000, suffix: "+", label: "Khách hàng phục vụ" },
  { value: 0, target: 4, suffix: " vị", label: "Gia vị đặc trưng" },
  { value: 0, target: 98, suffix: "%", label: "Khách hàng hài lòng" },
]);

let counterStarted = false;
const animateCounters = () => {
  if (counterStarted) return;
  const statsSection = document.querySelector(".stats-section");
  if (!statsSection) return;
  const rect = statsSection.getBoundingClientRect();
  if (rect.top < window.innerHeight - 100) {
    counterStarted = true;
    stats.value.forEach((stat) => {
      const duration = 2000;
      const steps = 60;
      const increment = stat.target / steps;
      let current = 0;
      const timer = setInterval(() => {
        current += increment;
        if (current >= stat.target) {
          stat.value = stat.target;
          clearInterval(timer);
        } else {
          stat.value = Math.floor(current);
        }
      }, duration / steps);
    });
  }
};

// ==================== PARALLAX ====================
const heroParallax = ref(null);
const handleParallax = () => {
  if (heroParallax.value) {
    const scrolled = window.scrollY;
    heroParallax.value.style.transform = `translateY(${scrolled * 0.4}px)`;
  }
  handleReveal();
  animateCounters();
};

// ==================== GALLERY ====================
const galleryImages = [
  { src: bannerLau, alt: "Không gian nhà hàng", caption: "Không gian ấm cúng" },
  { src: banner2, alt: "Lẩu Thái đặc trưng", caption: "Lẩu Thái chua cay" },
  { src: banner3, alt: "Set lẩu cao cấp", caption: "Set lẩu premium" },
  { src: banner4, alt: "Đồ nhúng tươi ngon", caption: "Nguyên liệu tươi sạch" },
];

const activeGallery = ref(0);
const changeGallery = (index) => {
  activeGallery.value = index;
};

// ==================== TEAM ====================
const team = [
  {
    name: "Bếp trưởng Minh",
    role: "Executive Chef",
    exp: "15 năm kinh nghiệm",
    desc: "Chuyên gia ẩm thực Thái Lan, từng tu nghiệp tại Bangkok",
    icon: "bi bi-person-badge",
  },
  {
    name: "Quản lý Hương",
    role: "Restaurant Manager",
    exp: "10 năm kinh nghiệm",
    desc: "Đảm bảo mỗi trải nghiệm của khách đều hoàn hảo",
    icon: "bi bi-people",
  },
  {
    name: "Sommelier Tuấn",
    role: "Beverage Expert",
    exp: "8 năm kinh nghiệm",
    desc: "Tư vấn đồ uống kết hợp hoàn hảo với lẩu Thái",
    icon: "bi bi-cup-hot",
  },
];

// ==================== TESTIMONIALS ====================

const testimonials = [
  {
    name: "Sơn Tùng M-TP",
    role: "Ca sĩ nổi tiếng",
    img: khach1,
    comment:
      "Lẩu Thái ở đây chuẩn vị nhất Hà Nội, mình đã ăn rất nhiều lần và lần nào cũng đầy hài lòng!",
    stars: 5,
  },
  {
    name: "Huấn Hoa Hồng",
    role: "Tiktoker triệu view",
    img: khach2,
    comment:
      "Không gian đẹp, phục vụ nhiệt tình, đặc biệt nước lẩu đậm đà chua cay rất đúng vị Thái Lan!",
    stars: 5,
  },
  {
    name: "Lê Minh Hồ",
    role: "Food Blogger",
    img: khach3,
    comment:
      "Four Spice là điểm đến không thể bỏ qua cho những ai mê ẩm thực Thái. Cực kỳ recommend!",
    stars: 5,
  },
];

const activeTestimonial = ref(0);
let testimonialInterval;

const nextTestimonial = () => {
  activeTestimonial.value = (activeTestimonial.value + 1) % testimonials.length;
};

// ==================== VALUES ====================
const values = [
  {
    icon: "bi bi-fire",
    title: "Gia vị đích thực",
    desc: "Gia vị nhập khẩu trực tiếp từ Thái Lan, đảm bảo hương vị chuẩn xác nhất",
    color: "#e63a3f",
  },
  {
    icon: "bi bi-leaf",
    title: "Nguyên liệu tươi sạch",
    desc: "100% rau củ, thịt, hải sản được chọn lọc kỹ càng, tươi sạch mỗi ngày",
    color: "#27ae60",
  },
  {
    icon: "bi bi-stars",
    title: "Không gian đẳng cấp",
    desc: "Thiết kế sang trọng mang phong cách Thái Lan, tạo cảm giác đi du lịch ngay tại Hà Nội",
    color: "#d4af37",
  },
  {
    icon: "bi bi-heart",
    title: "Tâm huyết phục vụ",
    desc: "Đội ngũ phục vụ chuyên nghiệp, tận tâm đặt trải nghiệm khách hàng lên hàng đầu",
    color: "#e63a3f",
  },
];

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

  testimonialInterval = setInterval(nextTestimonial, 4000);

  // Particle effect
  createParticles();
});

onUnmounted(() => {
  window.removeEventListener("scroll", handleParallax);
  clearInterval(testimonialInterval);
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
</script>

<template>
  <main class="gioi-thieu-page">
    <!-- ====================== HERO SECTION ====================== -->
    <section class="hero-gt position-relative overflow-hidden">
      <div class="hero-bg-parallax" ref="heroParallax">
        <img :src="bannerLau" alt="Four Spice Hotpot" class="hero-img-bg" />
      </div>
      <div class="hero-gt-overlay"></div>
      <div class="particles-container"></div>

      <div class="hero-gt-content container">
        <div class="hero-badge reveal-on-scroll">
          <span class="badge-dot"></span>
          <span>Nhà hàng lẩu Thái số 1 Hà Nội</span>
          <span class="badge-dot"></span>
        </div>
        <h1 class="hero-gt-title reveal-on-scroll">
          <span class="title-line-1">FOUR SPICE</span>
          <span class="title-line-2">HOTPOT</span>
        </h1>
        <p class="hero-gt-sub reveal-on-scroll">
          Hành trình khám phá tinh hoa ẩm thực Thái Lan<br />
          qua từng nồi lẩu chua cay đậm đà
        </p>
        <div class="hero-divider reveal-on-scroll">
          <span class="divider-line"></span>
          <span class="divider-icon">✦</span>
          <span class="divider-line"></span>
        </div>
      </div>
    </section>

    <!-- ====================== INTRO STORY SECTION ====================== -->
    <section class="section-story py-5 reveal-on-scroll">
      <div class="container">
        <div class="row align-items-center g-5">
          <!-- Gallery Stack -->
          <div class="col-lg-6 order-2 order-lg-1">
            <div class="gallery-stack-gt">
              <div class="gallery-main-img">
                <img
                  :src="galleryImages[activeGallery].src"
                  :alt="galleryImages[activeGallery].alt"
                  class="gallery-img-main"
                />
                <div class="gallery-caption">
                  {{ galleryImages[activeGallery].caption }}
                </div>
                <div class="gallery-gold-border"></div>
              </div>
              <div class="gallery-thumbnails">
                <div
                  v-for="(img, idx) in galleryImages"
                  :key="idx"
                  class="gallery-thumb"
                  :class="{ active: idx === activeGallery }"
                  @click="changeGallery(idx)"
                >
                  <img :src="img.src" :alt="img.alt" />
                  <div class="thumb-overlay"></div>
                </div>
              </div>
              <!-- Floating badge -->
              <div class="floating-badge">
                <div class="badge-inner">
                  <span class="badge-num">8</span>
                  <span class="badge-text">Năm<br />Trải nghiệm</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Story Content -->
          <div class="col-lg-6 order-1 order-lg-2 ps-lg-5">
            <div class="story-content">
              <div class="section-eyebrow">
                <span class="eyebrow-line"></span>
                <span>CÂU CHUYỆN CỦA CHÚNG TÔI</span>
                <span class="eyebrow-line"></span>
              </div>
              <h2 class="story-title">
                Từ niềm đam mê<br />
                <span class="story-highlight">đến đỉnh cao ẩm thực</span>
              </h2>
              <p class="story-text">
                <strong>Four Spice Hotpot</strong> được thành lập với triết lý
                đơn giản: mang đến trải nghiệm lẩu Thái chính thống nhất, ngon
                nhất ngay giữa lòng Hà Nội. Chúng tôi tin rằng mỗi bữa ăn là một
                hành trình khám phá.
              </p>
              <p class="story-text">
                Với bí quyết gia truyền được học hỏi trực tiếp từ các đầu bếp
                lão luyện tại Bangkok, Thái Lan, mỗi nồi lẩu của chúng tôi là sự
                kết hợp hoàn hảo giữa <em>4 vị gia vị đặc trưng</em>: chua – cay
                – mặn – ngọt.
              </p>

              <div class="story-features">
                <div
                  class="story-feature-item"
                  v-for="(v, i) in values.slice(0, 3)"
                  :key="i"
                >
                  <div class="sf-icon" :style="{ color: v.color }">
                    <i :class="v.icon"></i>
                  </div>
                  <span>{{ v.title }}</span>
                </div>
              </div>

              <div class="established-badge">
                <span class="est-year">Est. 2016</span>
                <span class="est-sep">|</span>
                <span class="est-text"
                  >Tự hào phục vụ hơn 50.000 khách hàng</span
                >
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ====================== STATS SECTION ====================== -->
    <section class="stats-section py-5">
      <div class="stats-bg">
        <img :src="bgLau" alt="" class="stats-bg-img" />
        <div class="stats-overlay"></div>
      </div>
      <div class="container position-relative">
        <div class="row g-4 text-center">
          <div
            class="col-6 col-md-3 reveal-on-scroll"
            v-for="(stat, i) in stats"
            :key="i"
          >
            <div class="stat-card">
              <div class="stat-number">
                {{ stat.value.toLocaleString() }}{{ stat.suffix }}
              </div>
              <div class="stat-label">{{ stat.label }}</div>
              <div class="stat-line"></div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ====================== VALUES SECTION ====================== -->
    <section class="section-values py-5 bg-white">
      <div class="container">
        <div class="text-center mb-5 reveal-on-scroll">
          <div class="section-eyebrow">
            <span class="eyebrow-line"></span>
            <span>GIÁ TRỊ CỐT LÕI</span>
            <span class="eyebrow-line"></span>
          </div>
          <h2 class="values-title">
            Điều làm nên <span class="text-highlight">Four Spice</span>
          </h2>
        </div>

        <div class="row g-4">
          <div
            class="col-md-6 col-lg-3 reveal-on-scroll"
            v-for="(val, idx) in values"
            :key="idx"
          >
            <div class="value-card feature-card-royal">
              <div class="card-shine"></div>
              <div class="card-shine second"></div>
              <div class="royal-border"></div>

              <div class="value-icon-box" :style="{ '--val-color': val.color }">
                <i :class="val.icon"></i>
              </div>
              <h5 class="value-title">{{ val.title }}</h5>
              <p class="value-desc">{{ val.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ====================== ALBUM / GALLERY NEW ====================== -->
    <section class="section-album py-5">
      <div class="container">
        <div class="text-center mb-5 reveal-on-scroll">
          <div class="section-eyebrow">
            <span class="eyebrow-line"></span>
            <span>THƯ VIỆN HÌNH ẢNH</span>
            <span class="eyebrow-line"></span>
          </div>
          <h2 class="values-title">Góc nhìn <span class="text-highlight">đẳng cấp</span></h2>
        </div>

        <div class="row g-4 album-grid">
          <div class="col-6 col-md-4 reveal-on-scroll">
            <div class="album-img-wrap"><img :src="bannerLau" alt="Lẩu" class="img-fluid" /></div>
          </div>
          <div class="col-6 col-md-4 reveal-on-scroll">
            <div class="album-img-wrap"><img :src="banner1" alt="Không gian" class="img-fluid" /></div>
          </div>
          <div class="col-6 col-md-4 reveal-on-scroll">
            <div class="album-img-wrap"><img :src="banner2" alt="Lẩu Thái" class="img-fluid" /></div>
          </div>
          <div class="col-6 col-md-4 reveal-on-scroll">
            <div class="album-img-wrap"><img :src="banner3" alt="Lẩu Thái" class="img-fluid" /></div>
          </div>
          <div class="col-6 col-md-4 reveal-on-scroll">
            <div class="album-img-wrap"><img :src="banner4" alt="Lẩu Thái" class="img-fluid" /></div>
          </div>
          <div class="col-6 col-md-4 reveal-on-scroll">
            <div class="album-img-wrap"><img :src="anhlau" alt="Lẩu Thái" class="img-fluid" /></div>
          </div>
        </div>
      </div>
    </section>

    <!-- ====================== ATMOSPHERE SECTION ====================== -->
    <section class="section-atmosphere py-5">
      <div class="container">
        <div class="text-center mb-5 reveal-on-scroll">
          <div class="section-eyebrow">
            <span class="eyebrow-line"></span>
            <span>KHÔNG GIAN & ĐỊA ĐIỂM</span>
            <span class="eyebrow-line"></span>
          </div>
          <h2 class="values-title">
            Không gian <span class="text-highlight">đẳng cấp</span>
          </h2>
          <p class="atm-subtitle">
            Trải nghiệm bữa ăn trong không gian sang trọng mang phong cách Thái
            Lan đích thực
          </p>
        </div>

        <!-- Mosaic Gallery -->
        <div class="mosaic-gallery reveal-on-scroll">
          <div class="mosaic-main">
            <img :src="banner2" alt="Không gian chính" class="mosaic-img" />
            <div class="mosaic-overlay">
              <div class="mosaic-text">Không gian chính</div>
            </div>
          </div>
          <div class="mosaic-side">
            <div class="mosaic-item">
              <img :src="banner3" alt="Khu vực VIP" class="mosaic-img" />
              <div class="mosaic-overlay">
                <div class="mosaic-text">Khu vực VIP</div>
              </div>
            </div>
            <div class="mosaic-item">
              <img :src="banner4" alt="Bàn đặc biệt" class="mosaic-img" />
              <div class="mosaic-overlay">
                <div class="mosaic-text">Bàn ngoài trời</div>
              </div>
            </div>
            <div class="mosaic-item">
              <img :src="anhlau" alt="Món lẩu" class="mosaic-img" />
              <div class="mosaic-overlay">
                <div class="mosaic-text">Lẩu đặc trưng</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Location Info -->
        <div class="location-info reveal-on-scroll">
          <div class="row g-4 mt-4">
            <div class="col-md-4">
              <div class="location-card">
                <div class="location-icon">
                  <i class="bi bi-geo-alt-fill"></i>
                </div>
                <h6>Địa chỉ</h6>
                <p>123 Đường Ẩm Thực, Quận Hoàn Kiếm, Hà Nội</p>
              </div>
            </div>
            <div class="col-md-4">
              <div class="location-card">
                <div class="location-icon">
                  <i class="bi bi-clock-fill"></i>
                </div>
                <h6>Giờ mở cửa</h6>
                <p>Thứ 2 – Chủ nhật<br />10:00 – 22:00</p>
              </div>
            </div>
            <div class="col-md-4">
              <div class="location-card">
                <div class="location-icon">
                  <i class="bi bi-telephone-fill"></i>
                </div>
                <h6>Liên hệ đặt bàn</h6>
                <p>
                  Hotline: <strong>1900 xxxx</strong><br />Email:
                  fourspice@email.com
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ====================== TEAM SECTION ====================== -->
    <section class="section-team py-5">
      <div class="team-bg-texture"></div>
      <div class="container position-relative">
        <div class="text-center mb-5 reveal-on-scroll">
          <div class="section-eyebrow">
            <span class="eyebrow-line"></span>
            <span>ĐỘI NGŨ CỦA CHÚNG TÔI</span>
            <span class="eyebrow-line"></span>
          </div>
          <h2 class="values-title">
            Những con người <span class="text-highlight">tạo nên kỳ diệu</span>
          </h2>
        </div>

        <div class="row g-4 justify-content-center">
          <div
            class="col-md-4 reveal-on-scroll"
            v-for="(member, idx) in team"
            :key="idx"
          >
            <div class="team-card">
              <div class="team-icon-wrapper">
                <i :class="member.icon"></i>
              </div>
              <div class="team-glow"></div>
              <div class="team-royal-border"></div>
              <h5 class="team-name">{{ member.name }}</h5>
              <span class="team-role">{{ member.role }}</span>
              <div class="team-exp">
                <i class="bi bi-award me-1"></i>{{ member.exp }}
              </div>
              <p class="team-desc">{{ member.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ====================== TESTIMONIALS ====================== -->
    <section class="section-testimonials py-5">
      <div class="testimonials-bg">
        <img :src="footerBg" alt="" class="testimonials-bg-img" />
        <div class="testimonials-overlay"></div>
      </div>
      <div class="container position-relative">
        <div class="text-center mb-5 reveal-on-scroll">
          <div class="section-eyebrow eyebrow-light">
            <span class="eyebrow-line light"></span>
            <span>CẢM NHẬN KHÁCH HÀNG</span>
            <span class="eyebrow-line light"></span>
          </div>
          <h2 class="values-title text-white">
            Khách hàng
            <span class="text-highlight-gold">nói gì về chúng tôi</span>
          </h2>
        </div>

        <!-- Testimonials Slider -->
        <div class="testimonials-slider">
          <div
            class="testimonial-track"
            :style="{ transform: `translateX(-${activeTestimonial * 100}%)` }"
          >
            <div
              class="testimonial-slide"
              v-for="(t, idx) in testimonials"
              :key="idx"
            >
              <div class="testimonial-card">
                <div class="testimonial-quote-icon">"</div>
                <p class="testimonial-comment">{{ t.comment }}</p>
                <div class="testimonial-stars">
                  <i class="bi bi-star-fill" v-for="s in t.stars" :key="s"></i>
                </div>
                <div class="testimonial-author">
                  <img :src="t.img" :alt="t.name" class="author-avatar" />
                  <div>
                    <div class="author-name">{{ t.name }}</div>
                    <div class="author-role">{{ t.role }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Dots -->
          <div class="testimonial-dots">
            <span
              v-for="(t, idx) in testimonials"
              :key="idx"
              class="t-dot"
              :class="{ active: idx === activeTestimonial }"
              @click="activeTestimonial = idx"
            ></span>
          </div>

          <!-- Navigation -->
          <button
            class="testimonial-nav prev"
            @click="
              activeTestimonial =
                (activeTestimonial - 1 + testimonials.length) %
                testimonials.length
            "
          >
            <i class="bi bi-chevron-left"></i>
          </button>
          <button
            class="testimonial-nav next"
            @click="
              activeTestimonial = (activeTestimonial + 1) % testimonials.length
            "
          >
            <i class="bi bi-chevron-right"></i>
          </button>
        </div>
      </div>
    </section>

    <!-- ====================== PROMISE SECTION ====================== -->
    <section class="section-promise py-5">
      <div class="container">
        <div class="promise-box reveal-on-scroll">
          <div class="promise-gold-border"></div>
          <div class="promise-inner">
            <div class="promise-ornament top-left">✦</div>
            <div class="promise-ornament top-right">✦</div>
            <div class="promise-ornament bottom-left">✦</div>
            <div class="promise-ornament bottom-right">✦</div>

            <div class="section-eyebrow">
              <span class="eyebrow-line"></span>
              <span>CAM KẾT CỦA CHÚNG TÔI</span>
              <span class="eyebrow-line"></span>
            </div>
            <h2 class="promise-title">
              Mỗi bữa ăn là một<br />
              <span class="text-highlight">trải nghiệm đáng nhớ</span>
            </h2>
            <p class="promise-text">
              Tại Four Spice Hotpot, chúng tôi không chỉ phục vụ bữa ăn — chúng
              tôi tạo ra những khoảnh khắc ký ức đáng trân trọng bên gia đình và
              bạn bè. Mỗi ngày, chúng tôi cam kết mang đến chất lượng tốt nhất,
              dịch vụ chân thành nhất.
            </p>
            <div class="promise-icons">
              <div class="pi-item">
                <i class="bi bi-shield-check"></i>
                <span>An toàn vệ sinh</span>
              </div>
              <div class="pi-item">
                <i class="bi bi-award"></i>
                <span>Chất lượng đảm bảo</span>
              </div>
              <div class="pi-item">
                <i class="bi bi-emoji-smile"></i>
                <span>Trải nghiệm hoàn hảo</span>
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
.gioi-thieu-page {
  font-family: "Inter", sans-serif;
  overflow-x: hidden;
  background: #fff;
}

/* =================== HERO SECTION =================== */
.hero-gt {
  height: 100vh;
  min-height: 600px;
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
    rgba(10, 2, 2, 0.75) 0%,
    rgba(100, 15, 15, 0.6) 50%,
    rgba(0, 0, 0, 0.8) 100%
  );
  z-index: 1;
}

/* Particles */
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
  animation: fadeDown 1s ease forwards;
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
  animation: fadeUp 1s ease 0.2s both;
}

.title-line-1 {
  display: block;
  font-family: "Times New Roman", Times, serif;
  font-size: clamp(3rem, 8vw, 7rem);
  font-weight: 900;
  color: #ffffff;
  letter-spacing: 10px;
  text-shadow: 0 0 40px rgba(228, 50, 50, 0.4);
}

.title-line-2 {
  display: block;
  font-family: "Times New Roman", Times, serif;
  font-size: clamp(1.5rem, 4vw, 3rem);
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
  animation: fadeUp 1s ease 0.4s both;
  font-weight: 300;
  letter-spacing: 0.5px;
}

.hero-divider {
  display: flex;
  align-items: center;
  gap: 15px;
  justify-content: center;
  margin-bottom: 50px;
  animation: fadeUp 1s ease 0.6s both;
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

/* Scroll hint */
.hero-scroll-hint {
  position: absolute;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 0.7rem;
  letter-spacing: 2px;
  text-transform: uppercase;
  animation: fadeUp 1s ease 1s both;
}

.scroll-mouse {
  width: 22px;
  height: 36px;
  border: 1.5px solid rgba(255, 255, 255, 0.4);
  border-radius: 11px;
  display: flex;
  justify-content: center;
  padding-top: 6px;
}

.scroll-wheel {
  width: 3px;
  height: 6px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 2px;
  animation: scrollWheel 2s infinite ease;
}

@keyframes scrollWheel {
  0% {
    transform: translateY(0);
    opacity: 1;
  }
  100% {
    transform: translateY(10px);
    opacity: 0;
  }
}

/* =================== PARTICLES =================== */
:deep(.particle) {
  position: absolute;
  bottom: -10px;
  border-radius: 50%;
  background: radial-gradient(
    circle,
    rgba(212, 175, 55, 0.8),
    rgba(228, 50, 50, 0.4)
  );
  animation: particleFloat linear infinite;
  pointer-events: none;
}

@keyframes particleFloat {
  0% {
    transform: translateY(0) scale(1);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 0.5;
  }
  100% {
    transform: translateY(-100vh) scale(0);
    opacity: 0;
  }
}

/* =================== REVEAL ANIMATIONS =================== */
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

/* =================== ALBUM SECTION =================== */
.section-album {
  background: #fafafa;
}

.album-img-wrap {
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
  border: 1px solid rgba(212, 175, 55, 0.3);
  height: 250px;
  position: relative;
}

.album-img-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
}

.album-img-wrap:hover img {
  transform: scale(1.1);
}

/* =================== SECTION EYEBROW =================== */
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

.eyebrow-line.light {
  background: linear-gradient(90deg, transparent, rgba(212, 175, 55, 0.8));
}

.eyebrow-light {
  color: #d4af37;
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

/* =================== STORY SECTION =================== */
.section-story {
  background: #fff;
}

/* Gallery Stack */
.gallery-stack-gt {
  position: relative;
}

.gallery-main-img {
  position: relative;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.15);
}

.gallery-img-main {
  width: 100%;
  height: 450px;
  object-fit: cover;
  transition: transform 0.8s cubic-bezier(0.23, 1, 0.32, 1);
}

.gallery-main-img:hover .gallery-img-main {
  transform: scale(1.05);
}

.gallery-caption {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
  color: #d4af37;
  padding: 20px 15px 15px;
  font-size: 0.85rem;
  font-weight: 700;
  letter-spacing: 2px;
  text-transform: uppercase;
}

.gallery-gold-border {
  position: absolute;
  top: 8px;
  left: 8px;
  right: 8px;
  bottom: 8px;
  border: 1px solid rgba(212, 175, 55, 0.3);
  pointer-events: none;
  z-index: 2;
}

.gallery-thumbnails {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.gallery-thumb {
  flex: 1;
  height: 80px;
  border-radius: 3px;
  overflow: hidden;
  cursor: pointer;
  position: relative;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.gallery-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.gallery-thumb:hover img {
  transform: scale(1.1);
}

.gallery-thumb.active {
  border-color: #d4af37;
  box-shadow: 0 0 12px rgba(212, 175, 55, 0.4);
}

.thumb-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.2);
  transition: 0.3s;
}

.gallery-thumb.active .thumb-overlay {
  background: transparent;
}

/* Floating Badge */
.floating-badge {
  position: absolute;
  top: -20px;
  right: -20px;
  width: 90px;
  height: 90px;
  background: linear-gradient(135deg, #e63a3f, #a31d21);
  border: 3px double #d4af37;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 25px rgba(163, 29, 33, 0.4);
  animation: rotateBadge 10s linear infinite;
  z-index: 5;
}

.badge-inner {
  text-align: center;
  color: #fff;
}

.badge-num {
  display: block;
  font-family: "Playfair Display", serif;
  font-size: 1.8rem;
  font-weight: 900;
  line-height: 1;
}

.badge-text {
  font-size: 0.5rem;
  letter-spacing: 1px;
  text-transform: uppercase;
  line-height: 1.3;
  color: #d4af37;
}

@keyframes rotateBadge {
  0% {
    box-shadow: 0 8px 25px rgba(163, 29, 33, 0.4), 0 0 0 rgba(212, 175, 55, 0);
  }
  50% {
    box-shadow: 0 8px 25px rgba(163, 29, 33, 0.6),
      0 0 20px rgba(212, 175, 55, 0.3);
  }
  100% {
    box-shadow: 0 8px 25px rgba(163, 29, 33, 0.4), 0 0 0 rgba(212, 175, 55, 0);
  }
}

/* Story Content */
.story-title {
  font-family: "Playfair Display", serif;
  font-size: clamp(1.8rem, 3vw, 2.8rem);
  font-weight: 800;
  color: #1a1a1a;
  line-height: 1.25;
  margin-bottom: 20px;
}

.values-title {
  font-family: "Playfair Display", serif;
  font-size: clamp(1.6rem, 3vw, 2.5rem);
  font-weight: 800;
  color: #1a1a1a;
  margin-bottom: 10px;
}

.story-text {
  color: #555;
  line-height: 1.9;
  margin-bottom: 15px;
  font-size: 0.95rem;
}

.story-features {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin: 25px 0;
}

.story-feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fafafa;
  border: 1px solid #f0e0e0;
  padding: 8px 16px;
  font-size: 0.85rem;
  font-weight: 600;
  color: #333;
  border-radius: 2px;
  transition: 0.3s;
}

.story-feature-item:hover {
  border-color: #e63a3f;
  background: rgba(230, 58, 63, 0.04);
  transform: translateY(-2px);
}

.sf-icon {
  font-size: 1.1rem;
}

.established-badge {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  background: linear-gradient(135deg, #f9f2f2, #fff3e0);
  border: 1px solid rgba(212, 175, 55, 0.3);
  border-left: 3px solid #e63a3f;
  padding: 12px 20px;
  font-size: 0.85rem;
  color: #555;
}

.est-year {
  font-family: "Playfair Display", serif;
  font-weight: 700;
  color: #a31d21;
  font-size: 1rem;
}

.est-sep {
  color: #d4af37;
}

/* =================== STATS SECTION =================== */
.stats-section {
  position: relative;
  overflow: hidden;
}

.stats-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.stats-bg-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.stats-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(163, 29, 33, 0.92),
    rgba(80, 5, 5, 0.95)
  );
}

.stat-card {
  position: relative;
  color: #fff;
  padding: 30px 20px;
  transition: 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-number {
  font-family: "Playfair Display", serif;
  font-size: clamp(2rem, 4vw, 3.5rem);
  font-weight: 900;
  color: #d4af37;
  line-height: 1;
  text-shadow: 0 0 20px rgba(212, 175, 55, 0.3);
  transition: text-shadow 0.3s;
}

.stat-card:hover .stat-number {
  text-shadow: 0 0 30px rgba(212, 175, 55, 0.6);
}

.stat-label {
  font-size: 0.78rem;
  letter-spacing: 2px;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 8px;
  font-weight: 600;
}

.stat-line {
  width: 30px;
  height: 2px;
  background: #d4af37;
  margin: 12px auto 0;
  transition: width 0.3s;
}

.stat-card:hover .stat-line {
  width: 60px;
}

/* =================== VALUES SECTION =================== */
.section-values {
  background: #fff;
}

/* Reuse existing royal card styles */
.feature-card-royal {
  position: relative;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(247, 36, 36, 0.2);
  padding: 35px 25px;
  border-radius: 4px;
  text-align: center;
  overflow: hidden;
  transition: all 0.5s cubic-bezier(0.23, 1, 0.32, 1);
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  z-index: 1;
}

.royal-border {
  position: absolute;
  top: 5px;
  left: 5px;
  right: 5px;
  bottom: 5px;
  border: 1px solid rgba(212, 175, 55, 0.3);
  pointer-events: none;
  z-index: 2;
  transition: all 0.5s ease;
}

.royal-border::after {
  content: "";
  position: absolute;
  top: 3px;
  left: 3px;
  right: 3px;
  bottom: 3px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.card-shine {
  position: absolute;
  top: 0;
  left: -150%;
  width: 70px;
  height: 100%;
  background: linear-gradient(
    120deg,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 1) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  transform: skewX(-25deg);
  transition: 0.6s ease;
  z-index: 3;
}

.card-shine.second {
  width: 120px;
  background: linear-gradient(
    120deg,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.6) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  transition: 1s ease;
}

.feature-card-royal:hover .card-shine {
  left: 150%;
}
.feature-card-royal:hover .card-shine.second {
  left: 170%;
}

.feature-card-royal:hover {
  transform: translateY(-10px);
  background: rgba(212, 175, 55, 0.06);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15), 0 0 20px rgba(212, 175, 55, 0.15);
  border-color: #ef6b6b;
}

.feature-card-royal:hover .royal-border {
  border-color: #d4af37;
  top: 8px;
  left: 8px;
  right: 8px;
  bottom: 8px;
}

.value-icon-box {
  font-size: 2.5rem;
  color: var(--val-color, #e63a3f);
  margin-bottom: 15px;
  transition: all 0.5s ease;
  filter: drop-shadow(0 0 5px rgba(212, 175, 55, 0.2));
}

.feature-card-royal:hover .value-icon-box {
  transform: scale(1.2) rotate(-5deg);
  filter: drop-shadow(0 0 12px currentColor);
}

.value-title {
  font-family: "Playfair Display", serif;
  font-size: 1.1rem;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 10px;
}

.value-desc {
  font-size: 0.85rem;
  color: #666;
  line-height: 1.7;
  margin: 0;
}

/* =================== ATMOSPHERE SECTION =================== */
.section-atmosphere {
  background: #f9f5f5;
}

.atm-subtitle {
  color: #777;
  font-size: 0.95rem;
  max-width: 550px;
  margin: 0 auto;
  line-height: 1.7;
}

/* Mosaic Gallery */
.mosaic-gallery {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 10px;
  height: 500px;
  border-radius: 4px;
  overflow: hidden;
}

.mosaic-main {
  position: relative;
  overflow: hidden;
}

.mosaic-side {
  display: grid;
  grid-template-rows: 1fr 1fr 1fr;
  gap: 10px;
}

.mosaic-item {
  position: relative;
  overflow: hidden;
}

.mosaic-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.23, 1, 0.32, 1);
}

.mosaic-main:hover .mosaic-img,
.mosaic-item:hover .mosaic-img {
  transform: scale(1.08);
}

.mosaic-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(transparent 50%, rgba(0, 0, 0, 0.6));
  display: flex;
  align-items: flex-end;
  padding: 15px;
  opacity: 0;
  transition: 0.4s;
}

.mosaic-main:hover .mosaic-overlay,
.mosaic-item:hover .mosaic-overlay {
  opacity: 1;
}

.mosaic-text {
  color: #d4af37;
  font-size: 0.8rem;
  font-weight: 700;
  letter-spacing: 2px;
  text-transform: uppercase;
}

/* Location Info */
.location-card {
  background: #fff;
  border: 1px solid #f0e0e0;
  border-top: 3px solid #e63a3f;
  padding: 25px 20px;
  text-align: center;
  transition: 0.3s;
}

.location-card:hover {
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  transform: translateY(-5px);
}

.location-icon {
  font-size: 2rem;
  color: #e63a3f;
  margin-bottom: 12px;
}

.location-card h6 {
  font-family: "Playfair Display", serif;
  font-weight: 700;
  color: #1a1a1a;
  font-size: 1rem;
  margin-bottom: 8px;
}

.location-card p {
  color: #666;
  font-size: 0.88rem;
  line-height: 1.6;
  margin: 0;
}

/* =================== TEAM SECTION =================== */
.section-team {
  background: #fff;
  position: relative;
  overflow: hidden;
}

.team-bg-texture {
  position: absolute;
  inset: 0;
  background: radial-gradient(
      ellipse at 20% 50%,
      rgba(230, 58, 63, 0.04) 0%,
      transparent 60%
    ),
    radial-gradient(
      ellipse at 80% 50%,
      rgba(212, 175, 55, 0.04) 0%,
      transparent 60%
    );
  pointer-events: none;
}

.team-card {
  position: relative;
  background: #fff;
  border: 1px solid rgba(230, 58, 63, 0.15);
  padding: 40px 25px 30px;
  text-align: center;
  overflow: hidden;
  transition: all 0.5s cubic-bezier(0.23, 1, 0.32, 1);
}

.team-card:hover {
  transform: translateY(-12px);
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.12), 0 0 25px rgba(212, 175, 55, 0.1);
  border-color: #d4af37;
}

.team-royal-border {
  position: absolute;
  top: 8px;
  left: 8px;
  right: 8px;
  bottom: 8px;
  border: 1px solid rgba(212, 175, 55, 0.2);
  pointer-events: none;
  transition: 0.5s;
}

.team-card:hover .team-royal-border {
  border-color: rgba(212, 175, 55, 0.5);
  top: 12px;
  left: 12px;
  right: 12px;
  bottom: 12px;
}

.team-icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e63a3f, #a31d21);
  border: 3px double #d4af37;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  color: #fff;
  margin: 0 auto 20px;
  box-shadow: 0 8px 20px rgba(163, 29, 33, 0.3);
  transition: 0.5s;
}

.team-card:hover .team-icon-wrapper {
  transform: scale(1.1) rotate(-10deg);
  box-shadow: 0 12px 30px rgba(163, 29, 33, 0.4),
    0 0 15px rgba(212, 175, 55, 0.3);
}

.team-glow {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 150px;
  height: 150px;
  background: radial-gradient(circle, rgba(230, 58, 63, 0.08), transparent 70%);
  transition: 0.5s;
  pointer-events: none;
}

.team-card:hover .team-glow {
  width: 250px;
  height: 250px;
  background: radial-gradient(circle, rgba(212, 175, 55, 0.1), transparent 70%);
}

.team-name {
  font-family: "Playfair Display", serif;
  font-size: 1.2rem;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 5px;
}

.team-role {
  display: inline-block;
  font-size: 0.72rem;
  letter-spacing: 2px;
  text-transform: uppercase;
  color: #e63a3f;
  font-weight: 700;
  margin-bottom: 12px;
}

.team-exp {
  display: inline-flex;
  align-items: center;
  background: rgba(212, 175, 55, 0.1);
  border: 1px solid rgba(212, 175, 55, 0.3);
  padding: 4px 14px;
  font-size: 0.75rem;
  color: #a31d21;
  font-weight: 600;
  letter-spacing: 1px;
  margin-bottom: 15px;
}

.team-desc {
  font-size: 0.85rem;
  color: #666;
  line-height: 1.7;
  margin: 0;
}

/* =================== TESTIMONIALS =================== */
.section-testimonials {
  position: relative;
  overflow: hidden;
}

.testimonials-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.testimonials-bg-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.testimonials-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(5, 5, 20, 0.92),
    rgba(80, 5, 5, 0.9)
  );
}

/* Slider */
.testimonials-slider {
  position: relative;
  max-width: 700px;
  margin: 0 auto;
  overflow: hidden;
}

.testimonial-track {
  display: flex;
  transition: transform 0.6s cubic-bezier(0.23, 1, 0.32, 1);
}

.testimonial-slide {
  min-width: 100%;
  padding: 0 10px;
}

.testimonial-card {
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(15px);
  border: 1px solid rgba(212, 175, 55, 0.25);
  padding: 40px;
  text-align: center;
  position: relative;
}

.testimonial-quote-icon {
  font-size: 5rem;
  line-height: 0.5;
  color: rgba(212, 175, 55, 0.3);
  font-family: "Playfair Display", serif;
  font-weight: 900;
  margin-bottom: 15px;
}

.testimonial-comment {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.85;
  font-style: italic;
  margin-bottom: 20px;
}

.testimonial-stars {
  color: #d4af37;
  font-size: 1rem;
  letter-spacing: 3px;
  margin-bottom: 20px;
}

.testimonial-author {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
}

.author-avatar {
  width: 55px;
  height: 55px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #d4af37;
  padding: 2px;
}

.author-name {
  font-family: "Playfair Display", serif;
  font-weight: 700;
  color: #fff;
  text-align: left;
  font-size: 1rem;
}

.author-role {
  font-size: 0.75rem;
  color: #d4af37;
  letter-spacing: 1px;
  text-align: left;
}

/* Dots */
.testimonial-dots {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 25px;
}

.t-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  cursor: pointer;
  transition: 0.3s;
}

.t-dot.active {
  background: #d4af37;
  width: 24px;
  border-radius: 4px;
}

/* Nav Buttons */
.testimonial-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(212, 175, 55, 0.15);
  border: 1px solid rgba(212, 175, 55, 0.3);
  color: #d4af37;
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: 0.3s;
  font-size: 1rem;
}

.testimonial-nav:hover {
  background: #d4af37;
  color: #1a1a1a;
}

.testimonial-nav.prev {
  left: -55px;
}
.testimonial-nav.next {
  right: -55px;
}

/* =================== PROMISE SECTION =================== */
.section-promise {
  background: linear-gradient(135deg, #fff5f5, #fff8ec);
}

.promise-box {
  position: relative;
  padding: 3px;
  background: linear-gradient(135deg, #d4af37, #e63a3f, #d4af37);
}

.promise-gold-border {
  position: absolute;
  inset: 8px;
  border: 1px solid rgba(212, 175, 55, 0.3);
  pointer-events: none;
  z-index: 1;
}

.promise-inner {
  background: #fff;
  padding: 60px 50px;
  text-align: center;
  position: relative;
}

.promise-ornament {
  position: absolute;
  font-size: 1.2rem;
  color: #d4af37;
}

.promise-ornament.top-left {
  top: 15px;
  left: 20px;
}
.promise-ornament.top-right {
  top: 15px;
  right: 20px;
}
.promise-ornament.bottom-left {
  bottom: 15px;
  left: 20px;
}
.promise-ornament.bottom-right {
  bottom: 15px;
  right: 20px;
}

.promise-title {
  font-family: "Playfair Display", serif;
  font-size: clamp(1.6rem, 3vw, 2.5rem);
  font-weight: 800;
  color: #1a1a1a;
  margin-bottom: 20px;
  line-height: 1.3;
}

.promise-text {
  color: #666;
  font-size: 0.95rem;
  line-height: 1.9;
  max-width: 600px;
  margin: 0 auto 35px;
}

.promise-icons {
  display: flex;
  justify-content: center;
  gap: 40px;
  flex-wrap: wrap;
}

.pi-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: #333;
}

.pi-item i {
  font-size: 2rem;
  color: #e63a3f;
  transition: 0.3s;
}

.pi-item:hover i {
  color: #d4af37;
  transform: scale(1.2) rotate(-10deg);
}

.pi-item span {
  font-size: 0.78rem;
  font-weight: 700;
  letter-spacing: 1.5px;
  text-transform: uppercase;
  color: #555;
}

/* =================== KEYFRAMES =================== */
@keyframes fadeDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* =================== RESPONSIVE =================== */
@media (max-width: 768px) {
  .mosaic-gallery {
    grid-template-columns: 1fr;
    height: auto;
  }
  .mosaic-main {
    height: 250px;
  }
  .mosaic-side {
    grid-template-rows: none;
    grid-template-columns: 1fr 1fr 1fr;
    height: 100px;
  }
  .testimonial-nav {
    display: none;
  }
  .promise-inner {
    padding: 40px 25px;
  }
  .floating-badge {
    top: -10px;
    right: -10px;
    width: 70px;
    height: 70px;
  }
  .badge-num {
    font-size: 1.4rem;
  }
  .gallery-img-main {
    height: 280px;
  }
}
</style>
