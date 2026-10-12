<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getAllKhuyenMaiPublic } from "@/api/customer/khuyenmai/khuyenmaiApi";
import { useUtils } from "@/utils/helper";

// Assets
import bannerLau from "@/assets/images/banner-lau.png";
import banner1 from "@/assets/images/banner1.png";
import banner2 from "@/assets/images/banner2.png";
import banner3 from "@/assets/images/banner3.png";
import khuyenmai1 from "@/assets/images/khuyenmai1.png";
import khuyenmai2 from "@/assets/images/khuyenmai2.png";

const router = useRouter();
const { notify } = useUtils();

const loading = ref(true);
const danhSachPhieuGiamGia = ref([]);
const danhSachDotGiamGia = ref([]);
const copiedCode = ref("");

// State bộ lọc và phân trang đợt giảm giá
const selectedCampaignId = ref(null);
const selectedCategory = ref("ALL");
const currentPage = ref(1);
const itemsPerPage = 8; // Phân trang 8 sản phẩm 1 lần

// Danh sách ảnh mẫu cho voucher
const voucherImages = [khuyenmai1, khuyenmai2, banner1, banner2, banner3, bannerLau];

const getVoucherImage = (index) => {
  return voucherImages[index % voucherImages.length];
};

const getProductImage = (img) => {
  if (!img) return bannerLau;
  if (img.startsWith("http")) return img;
  return `http://localhost:8080/uploads/${img}`;
};

const formatCurrency = (val) => {
  if (val === null || val === undefined || val === "") return "0 ₫";
  return Number(val).toLocaleString("vi-VN") + " ₫";
};

const formatDate = (dateStr) => {
  if (!dateStr) return "Vô thời hạn";
  const d = new Date(dateStr);
  if (isNaN(d.getTime())) return dateStr;
  return d.toLocaleDateString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
};

const handleCopyCode = async (code) => {
  if (!code) return;
  try {
    await navigator.clipboard.writeText(code);
    copiedCode.value = code;
    notify.success(`Đã sao chép mã "${code}" vào bộ nhớ tạm!`);
    setTimeout(() => {
      if (copiedCode.value === code) copiedCode.value = "";
    }, 3000);
  } catch (err) {
    console.error("Lỗi sao chép mã:", err);
  }
};

const goToDatBan = () => {
  router.push("/dat-ban");
};

const goToThucDon = () => {
  router.push("/thuc-don");
};

// ==================== COMPUTED CHO ĐỢT GIẢM GIÁ ====================
// Đợt giảm giá đang chọn (mặc định là đợt đầu tiên)
const currentCampaign = computed(() => {
  if (!danhSachDotGiamGia.value || danhSachDotGiamGia.value.length === 0) return null;
  if (!selectedCampaignId.value) return danhSachDotGiamGia.value[0];
  return (
    danhSachDotGiamGia.value.find((d) => d.id === selectedCampaignId.value) ||
    danhSachDotGiamGia.value[0]
  );
});

// Danh sách danh mục có trong đợt giảm giá đang chọn
const categoriesInCampaign = computed(() => {
  if (!currentCampaign.value || !currentCampaign.value.danhSachSanPham) return [];
  const cats = new Set();
  currentCampaign.value.danhSachSanPham.forEach((sp) => {
    if (sp.tenDanhMuc && sp.tenDanhMuc.trim()) {
      cats.add(sp.tenDanhMuc.trim());
    }
  });
  return Array.from(cats);
});

// Danh sách sản phẩm sau khi lọc theo danh mục
const filteredProducts = computed(() => {
  if (!currentCampaign.value || !currentCampaign.value.danhSachSanPham) return [];
  if (selectedCategory.value === "ALL") {
    return currentCampaign.value.danhSachSanPham;
  }
  return currentCampaign.value.danhSachSanPham.filter(
    (sp) => (sp.tenDanhMuc || "").trim() === selectedCategory.value
  );
});

// Tổng số trang (mỗi trang 8 sản phẩm)
const totalPages = computed(() => {
  return Math.ceil(filteredProducts.value.length / itemsPerPage) || 1;
});

// Sản phẩm hiển thị trên trang hiện tại
const paginatedProducts = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  return filteredProducts.value.slice(start, start + itemsPerPage);
});

// Chuyển đợt giảm giá
const selectCampaign = (dotId) => {
  selectedCampaignId.value = dotId;
  selectedCategory.value = "ALL";
  currentPage.value = 1;
};

// Chọn danh mục lọc
const selectCategory = (cat) => {
  selectedCategory.value = cat;
  currentPage.value = 1;
};

// Đổi trang
const setPage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
    const el = document.getElementById("campaign-products-section");
    if (el) {
      el.scrollIntoView({ behavior: "smooth", block: "start" });
    }
  }
};

const loadData = async () => {
  loading.value = true;
  try {
    const res = await getAllKhuyenMaiPublic();
    const data = res.data;
    if (data) {
      danhSachPhieuGiamGia.value = data.danhSachPhieuGiamGia || [];
      danhSachDotGiamGia.value = data.danhSachDotGiamGia || [];
      if (danhSachDotGiamGia.value.length > 0) {
        selectedCampaignId.value = danhSachDotGiamGia.value[0].id;
      }
    }
  } catch (err) {
    console.error("Lỗi khi tải dữ liệu khuyến mãi:", err);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadData();
});
</script>

<template>
  <main class="promo-page">
    <!-- ====================== HERO BANNER ====================== -->
    <section class="hero-section position-relative overflow-hidden">
      <div class="hero-bg-wrapper">
        <img :src="khuyenmai1" alt="Khuyến mãi FourSpice Hotpot" class="hero-bg-img" />
      </div>
      <div class="hero-overlay"></div>

      <div class="hero-content container text-center">
        <div class="hero-badge">
          <span class="badge-sparkle">✦</span>
          <span>ƯU ĐÃI ĐẶC QUYỀN</span>
          <span class="badge-sparkle">✦</span>
        </div>
        <h1 class="hero-title">
          <span class="hero-title-main">KHUYẾN MÃI & VOUCHER</span>
          <span class="hero-title-sub">FOURSPICE HOTPOT</span>
        </h1>
        <p class="hero-desc">
          Tận hưởng không gian ẩm thực lẩu Thái thượng hạng cùng vô vàn mã giảm giá hấp dẫn
          và các chương trình ưu đãi sản phẩm diễn ra mỗi ngày.
        </p>
        <div class="hero-actions">
          <button class="btn-hero-primary" @click="goToDatBan">
            <i class="bi bi-calendar-check me-2"></i>Đặt bàn nhận ưu đãi
          </button>
          <button class="btn-hero-secondary" @click="goToThucDon">
            <i class="bi bi-book-half me-2"></i>Khám phá thực đơn
          </button>
        </div>
      </div>
    </section>

    <!-- LOADING STATE -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-danger" style="width: 3rem; height: 3rem;" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
      <p class="mt-3 text-muted fw-semibold">Đang cập nhật các chương trình ưu đãi mới nhất...</p>
    </div>

    <!-- MAIN CONTENT -->
    <div v-else class="promo-content-container">

      <!-- ========================================================= -->
      <!-- MỤC 1: MÃ GIẢM GIÁ (VOUCHERS) - ĐẦU TIÊN TRÊN CÙNG       -->
      <!-- ========================================================= -->
      <section class="section-vouchers py-5">
        <div class="container">
          <!-- Section Header -->
          <div class="section-header text-center mb-5">
            <div class="section-subtitle">
              <span class="line"></span>
              <span>MÃ GIẢM GIÁ ĐỘC QUYỀN</span>
              <span class="line"></span>
            </div>
            <h2 class="section-title">
              Săn Voucher <span class="highlight-text">Cực Hấp Dẫn</span>
            </h2>
            <p class="section-desc">
              Nhập mã khi đặt bàn trực tuyến hoặc xuất trình cho nhân viên thu ngân khi thanh toán để được giảm giá trực tiếp.
            </p>
          </div>

          <!-- Voucher Grid -->
          <div v-if="danhSachPhieuGiamGia && danhSachPhieuGiamGia.length > 0" class="row g-4 justify-content-center">
            <div
              v-for="(voucher, idx) in danhSachPhieuGiamGia"
              :key="voucher.id || idx"
              class="col-md-6 col-lg-4"
            >
              <div class="voucher-card" :class="{ 'is-private': voucher.laPhieuCaNhan || voucher.kieuGiamGia === 2 }">
                <!-- Tag riêng tư nếu có -->
                <div v-if="voucher.laPhieuCaNhan || voucher.kieuGiamGia === 2" class="voucher-private-tag">
                  <i class="bi bi-gem me-1"></i>Dành riêng cho bạn
                </div>

                <!-- Voucher Image Header -->
                <div class="voucher-img-box">
                  <img :src="getVoucherImage(idx)" :alt="voucher.tenGiamGia" class="voucher-img" />
                  <div class="voucher-img-overlay"></div>
                  
                  <!-- Discount Ribbon -->
                  <div class="voucher-discount-pill">
                    <template v-if="voucher.phuongThucGiamGia === 1">
                      Giảm {{ voucher.giaTriGiam }}%
                    </template>
                    <template v-else>
                      Giảm {{ formatCurrency(voucher.giaTriGiam) }}
                    </template>
                  </div>
                </div>

                <!-- Voucher Body -->
                <div class="voucher-body">
                  <h3 class="voucher-name" :title="voucher.tenGiamGia">
                    {{ voucher.tenGiamGia || 'Phiếu giảm giá đặc biệt' }}
                  </h3>

                  <!-- Details info -->
                  <div class="voucher-details">
                    <div class="detail-row">
                      <span class="detail-label">
                        <i class="bi bi-cart-check me-1 text-danger"></i>Đơn tối thiểu:
                      </span>
                      <span class="detail-val">{{ formatCurrency(voucher.dieuKienSuDung || 0) }}</span>
                    </div>

                    <div v-if="voucher.phuongThucGiamGia === 1 && voucher.giamToiDa > 0" class="detail-row">
                      <span class="detail-label">
                        <i class="bi bi-shield-check me-1 text-danger"></i>Giảm tối đa:
                      </span>
                      <span class="detail-val">{{ formatCurrency(voucher.giamToiDa) }}</span>
                    </div>

                    <div class="detail-row">
                      <span class="detail-label">
                        <i class="bi bi-calendar-event me-1 text-danger"></i>Ngày kết thúc:
                      </span>
                      <span class="detail-val text-muted">{{ formatDate(voucher.ngayKetThuc) }}</span>
                    </div>
                  </div>

                  <!-- Code Copy Box -->
                  <div class="voucher-code-wrapper">
                    <div class="code-display">
                      <span class="code-prefix">MÃ:</span>
                      <span class="code-text">{{ voucher.maGiamGia }}</span>
                    </div>
                    <button
                      class="btn-copy-code"
                      :class="{ 'copied': copiedCode === voucher.maGiamGia }"
                      @click="handleCopyCode(voucher.maGiamGia)"
                    >
                      <i :class="copiedCode === voucher.maGiamGia ? 'bi bi-check2-circle' : 'bi bi-copy'"></i>
                      <span>{{ copiedCode === voucher.maGiamGia ? 'Đã sao chép' : 'Sao chép' }}</span>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Empty Vouchers -->
          <div v-else class="empty-box text-center py-4 bg-white rounded-3 shadow-sm border">
            <i class="bi bi-ticket-perforated fs-1 text-muted"></i>
            <p class="mt-2 text-muted mb-0">Hiện tại chưa có mã giảm giá nào đang áp dụng.</p>
          </div>
        </div>
      </section>

      <!-- ========================================================= -->
      <!-- MỤC 2: ĐỢT GIẢM GIÁ (CAMPAIGNS) - BÊN DƯỚI                -->
      <!-- ========================================================= -->
      <section id="campaign-products-section" class="section-campaigns py-5 bg-light-section">
        <div class="container">
          <!-- Section Header -->
          <div class="section-header text-center mb-4">
            <div class="section-subtitle">
              <span class="line"></span>
              <span>SỰ KIỆN ƯU ĐÃI THỰC ĐƠN</span>
              <span class="line"></span>
            </div>
            <h2 class="section-title">
              Đợt Giảm Giá <span class="highlight-text">Đang Diễn Ra</span>
            </h2>
            <p class="section-desc">
              Thưởng thức các set lẩu và món nhúng hảo hạng với mức giá ưu đãi cực hấp dẫn trong các chương trình khuyến mãi theo đợt.
            </p>
          </div>

          <!-- Tabs chọn Đợt Giảm Giá (khi có nhiều đợt) -->
          <div v-if="danhSachDotGiamGia && danhSachDotGiamGia.length > 1" class="campaign-tabs-wrapper mb-4 d-flex justify-content-center flex-wrap gap-2 gap-md-3">
            <button
              v-for="dot in danhSachDotGiamGia"
              :key="dot.id"
              class="campaign-tab-btn"
              :class="{ active: currentCampaign?.id === dot.id }"
              @click="selectCampaign(dot.id)"
            >
              <i class="bi bi-tags-fill me-2"></i>
              <span>{{ dot.tenDot }}</span>
              <span class="badge-tab-discount">-{{ dot.giaTriGiam }}%</span>
            </button>
          </div>

          <!-- Khối Đợt Giảm Giá Hiện Tại -->
          <div v-if="currentCampaign" class="campaign-block shadow-sm">
            <!-- Campaign Block Header -->
            <div class="campaign-header">
              <div class="campaign-header-left">
                <div class="campaign-status-badge">
                  <span class="pulse-dot"></span>
                  <span>ĐANG ÁP DỤNG</span>
                </div>
                <h3 class="campaign-name">{{ currentCampaign.tenDot || 'Chương trình giảm giá đặc biệt' }}</h3>
                <div class="campaign-timing">
                  <span>
                    <i class="bi bi-clock-history me-1"></i>Hạn áp dụng đến:
                    <strong>{{ formatDate(currentCampaign.ngayKetThuc) }}</strong>
                  </span>
                </div>
              </div>

              <div class="campaign-header-right">
                <div class="campaign-discount-tag">
                  <span class="tag-title">ƯU ĐÃI TỚI</span>
                  <span class="tag-percent">-{{ currentCampaign.giaTriGiam }}%</span>
                </div>
              </div>
            </div>

            <!-- BỘ LỌC THEO DANH MỤC SẢN PHẨM -->
            <div class="campaign-filters-bar px-4 pt-4 pb-2">
              <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 border-bottom pb-3">
                <div class="filter-heading d-flex align-items-center gap-2">
                  <i class="bi bi-funnel-fill text-danger"></i>
                  <span class="fw-bold text-dark">Lọc theo danh mục:</span>
                </div>

                <div class="category-filters-wrapper d-flex flex-wrap gap-2">
                  <button
                    class="category-filter-btn"
                    :class="{ active: selectedCategory === 'ALL' }"
                    @click="selectCategory('ALL')"
                  >
                    <i class="bi bi-grid-fill me-1"></i> Tất cả ({{ currentCampaign.danhSachSanPham ? currentCampaign.danhSachSanPham.length : 0 }})
                  </button>
                  <button
                    v-for="cat in categoriesInCampaign"
                    :key="cat"
                    class="category-filter-btn"
                    :class="{ active: selectedCategory === cat }"
                    @click="selectCategory(cat)"
                  >
                    {{ cat }}
                  </button>
                </div>
              </div>
            </div>

            <!-- Product Grid in Campaign (8 items per page) -->
            <div class="campaign-products-body p-4">
              <div v-if="paginatedProducts && paginatedProducts.length > 0" class="row g-3 g-md-4">
                <div
                  v-for="sp in paginatedProducts"
                  :key="sp.sanPhamId"
                  class="col-6 col-md-4 col-lg-3"
                >
                  <div class="product-promo-card">
                    <!-- Image container -->
                    <div class="product-img-box">
                      <img :src="getProductImage(sp.hinhAnh)" :alt="sp.tenSanPham" class="product-img" />
                      
                      <!-- Discount Badge -->
                      <div class="product-badge-sale">
                        -{{ currentCampaign.giaTriGiam }}%
                      </div>
                    </div>

                    <!-- Product Info -->
                    <div class="product-info-box">
                      <div v-if="sp.tenDanhMuc" class="product-category">
                        {{ sp.tenDanhMuc }}
                      </div>
                      <h4 class="product-title" :title="sp.tenSanPham">
                        {{ sp.tenSanPham }}
                      </h4>

                      <!-- Price box -->
                      <div class="product-price-row">
                        <div class="price-current">
                          {{ formatCurrency(sp.giaSauGiam) }}
                        </div>
                        <div class="price-old">
                          {{ formatCurrency(sp.giaGoc) }}
                        </div>
                      </div>

                      <!-- Expiry row -->
                      <div class="product-expiry-row">
                        <i class="bi bi-calendar-check me-1 text-danger"></i>
                        <span>HSD: {{ formatDate(currentCampaign.ngayKetThuc) }}</span>
                      </div>

                      <button class="btn-order-product" @click="goToDatBan">
                        <i class="bi bi-fire me-1"></i>Đặt bàn ngay
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Empty Products in Category -->
              <div v-else class="text-center py-5 text-muted">
                <i class="bi bi-inbox fs-2 text-muted"></i>
                <p class="mt-2 mb-0">Không có sản phẩm nào thuộc danh mục này trong đợt giảm giá.</p>
              </div>

              <!-- PHÂN TRANG 8 SẢN PHẨM / TRANG (Nút phân trang chuẩn Trang Chủ / Thực Đơn) -->
              <div class="d-flex justify-content-center mt-5" v-if="totalPages > 1">
                <nav>
                  <ul class="pagination-custom d-flex align-items-center gap-2 list-unstyled mb-0">
                    <li
                      class="page-item-custom"
                      :class="{ disabled: currentPage === 1 }"
                    >
                      <button
                        class="page-link-custom"
                        @click="setPage(currentPage - 1)"
                        :disabled="currentPage === 1"
                        aria-label="Previous Page"
                      >
                        <i class="bi bi-chevron-left"></i>
                      </button>
                    </li>

                    <li
                      v-for="page in totalPages"
                      :key="page"
                      class="page-item-custom"
                      :class="{ active: page === currentPage }"
                    >
                      <button
                        class="page-link-custom"
                        @click="setPage(page)"
                      >
                        {{ page }}
                      </button>
                    </li>

                    <li
                      class="page-item-custom"
                      :class="{ disabled: currentPage === totalPages }"
                    >
                      <button
                        class="page-link-custom"
                        @click="setPage(currentPage + 1)"
                        :disabled="currentPage === totalPages"
                        aria-label="Next Page"
                      >
                        <i class="bi bi-chevron-right"></i>
                      </button>
                    </li>
                  </ul>
                </nav>
              </div>

            </div>
          </div>

          <!-- Empty Campaigns -->
          <div v-else class="empty-box text-center py-4 bg-white rounded-3 shadow-sm border">
            <i class="bi bi-calendar-x fs-1 text-muted"></i>
            <p class="mt-2 text-muted mb-0">Hiện tại chưa có đợt giảm giá nào đang diễn ra.</p>
          </div>
        </div>
      </section>

      <!-- EMPTY STATE ALL -->
      <section
        v-if="(!danhSachPhieuGiamGia || danhSachPhieuGiamGia.length === 0) && (!danhSachDotGiamGia || danhSachDotGiamGia.length === 0)"
        class="py-5 text-center"
      >
        <div class="container py-5">
          <i class="bi bi-inbox fs-1 text-muted mb-3"></i>
          <h3 class="fw-bold text-dark">Hiện chưa có chương trình khuyến mãi nào</h3>
          <p class="text-muted">Quý khách vui lòng quay lại sau để cập nhật những ưu đãi mới nhất từ FourSpice Hotpot.</p>
          <button class="btn-hero-primary mt-3" @click="goToDatBan">
            <i class="bi bi-calendar-check me-2"></i>Đặt bàn trải nghiệm ngay
          </button>
        </div>
      </section>

    </div>
  </main>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,600;0,700;0,800;0,900;1,400&family=Inter:wght@300;400;500;600;700;800&display=swap");

.promo-page {
  font-family: "Inter", sans-serif;
  background-color: #fdfdfd;
  color: #1f2937;
  min-height: 100vh;
}

/* ==================== HERO SECTION ==================== */
.hero-section {
  position: relative;
  height: 52vh;
  min-height: 380px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-bg-wrapper {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.hero-bg-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: brightness(0.65);
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(140, 0, 0, 0.75) 0%,
    rgba(20, 2, 2, 0.85) 100%
  );
  z-index: 1;
}

.hero-content {
  position: relative;
  z-index: 2;
  color: #fff;
  max-width: 860px;
  padding: 40px 15px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(212, 175, 55, 0.2);
  border: 1px solid rgba(212, 175, 55, 0.6);
  padding: 6px 18px;
  border-radius: 30px;
  font-size: 0.8rem;
  font-weight: 700;
  letter-spacing: 2px;
  text-transform: uppercase;
  color: #f5d470;
  margin-bottom: 20px;
}

.badge-sparkle {
  color: #f5d470;
}

.hero-title {
  margin-bottom: 16px;
  line-height: 1.15;
}

.hero-title-main {
  display: block;
  font-family: "Playfair Display", serif;
  font-size: clamp(2rem, 5vw, 3.8rem);
  font-weight: 900;
  color: #ffffff;
  letter-spacing: 2px;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.6);
}

.hero-title-sub {
  display: block;
  font-family: "Playfair Display", serif;
  font-size: clamp(1rem, 2.5vw, 1.8rem);
  font-weight: 700;
  color: #f5d470;
  letter-spacing: 6px;
  text-transform: uppercase;
}

.hero-desc {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.6;
  margin-bottom: 25px;
  max-width: 700px;
  margin-left: auto;
  margin-right: auto;
}

.hero-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  flex-wrap: wrap;
}

.btn-hero-primary {
  background: linear-gradient(135deg, #d32f2f, #991b1b);
  color: #fff;
  font-weight: 700;
  font-size: 0.9rem;
  padding: 12px 28px;
  border-radius: 8px;
  border: 1px solid #f87171;
  transition: all 0.3s ease;
  box-shadow: 0 6px 20px rgba(220, 38, 38, 0.4);
}

.btn-hero-primary:hover {
  background: linear-gradient(135deg, #ef4444, #b91c1c);
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(220, 38, 38, 0.6);
}

.btn-hero-secondary {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(8px);
  color: #ffffff;
  font-weight: 700;
  font-size: 0.9rem;
  padding: 12px 28px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  transition: all 0.3s ease;
}

.btn-hero-secondary:hover {
  background: rgba(255, 255, 255, 0.3);
  color: #fff;
  transform: translateY(-2px);
}

/* ==================== SECTION HEADERS ==================== */
.section-header .section-subtitle {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  font-size: 0.8rem;
  font-weight: 800;
  letter-spacing: 3px;
  color: #b91c1c;
  text-transform: uppercase;
  margin-bottom: 8px;
}

.section-header .line {
  width: 40px;
  height: 2px;
  background: #b91c1c;
}

.section-header .section-title {
  font-family: "Playfair Display", serif;
  font-size: clamp(1.8rem, 3.5vw, 2.6rem);
  font-weight: 800;
  color: #111827;
  margin-bottom: 12px;
}

.section-header .highlight-text {
  color: #b91c1c;
}

.section-header .section-desc {
  max-width: 680px;
  margin: 0 auto;
  color: #6b7280;
  font-size: 0.95rem;
  line-height: 1.6;
}

.bg-light-section {
  background-color: #f8fafc;
}

/* ==================== MỤC 1: VOUCHER CARDS ==================== */
.section-vouchers {
  background: #ffffff;
}

.voucher-card {
  background: #ffffff;
  border: 1px solid #f1f5f9;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  transition: all 0.35s cubic-bezier(0.2, 0.8, 0.2, 1);
  display: flex;
  flex-direction: column;
  position: relative;
  height: 100%;
}

.voucher-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 40px rgba(185, 28, 28, 0.12);
  border-color: #fecaca;
}

.voucher-card.is-private {
  border: 2px solid #f59e0b;
  box-shadow: 0 10px 30px rgba(245, 158, 11, 0.1);
}

.voucher-private-tag {
  position: absolute;
  top: 14px;
  right: 14px;
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: #fff;
  font-size: 0.72rem;
  font-weight: 800;
  padding: 4px 10px;
  border-radius: 20px;
  letter-spacing: 0.5px;
  z-index: 5;
  box-shadow: 0 4px 10px rgba(217, 119, 6, 0.4);
}

.voucher-img-box {
  position: relative;
  height: 160px;
  overflow: hidden;
}

.voucher-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.voucher-card:hover .voucher-img {
  transform: scale(1.08);
}

.voucher-img-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0,0,0,0.1) 0%, rgba(0,0,0,0.6) 100%);
}

.voucher-discount-pill {
  position: absolute;
  bottom: 12px;
  left: 14px;
  background: linear-gradient(135deg, #dc2626, #991b1b);
  color: #ffffff;
  font-family: "Playfair Display", serif;
  font-weight: 800;
  font-size: 1.15rem;
  padding: 6px 14px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.voucher-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  flex: 1;
  justify-content: space-between;
}

.voucher-name {
  font-family: "Playfair Display", serif;
  font-size: 1.2rem;
  font-weight: 700;
  color: #111827;
  margin-bottom: 12px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 48px;
}

.voucher-details {
  background: #fafafa;
  border-radius: 10px;
  padding: 12px;
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  border: 1px dashed #e5e7eb;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.85rem;
}

.detail-label {
  color: #6b7280;
  font-weight: 500;
}

.detail-val {
  font-weight: 700;
  color: #111827;
}

.voucher-code-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff1f2;
  border: 1.5px dashed #f87171;
  border-radius: 10px;
  padding: 8px 12px;
}

.code-display {
  display: flex;
  align-items: center;
  gap: 6px;
}

.code-prefix {
  font-size: 0.72rem;
  font-weight: 800;
  color: #991b1b;
}

.code-text {
  font-family: monospace;
  font-size: 1.05rem;
  font-weight: 800;
  color: #b91c1c;
  letter-spacing: 1px;
}

.btn-copy-code {
  background: #b91c1c;
  color: #fff;
  border: none;
  font-size: 0.78rem;
  font-weight: 700;
  padding: 6px 14px;
  border-radius: 6px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-copy-code:hover {
  background: #991b1b;
  transform: scale(1.03);
}

.btn-copy-code.copied {
  background: #16a34a;
}

/* ==================== CAMPAIGN TABS ==================== */
.campaign-tabs-wrapper {
  padding: 5px 0;
}

.campaign-tab-btn {
  background: #ffffff;
  border: 1.5px solid #e2e8f0;
  color: #1e293b;
  padding: 10px 22px;
  border-radius: 12px;
  font-weight: 700;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: inline-flex;
  align-items: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
}

.campaign-tab-btn:hover {
  border-color: #cbd5e1;
  transform: translateY(-2px);
}

.campaign-tab-btn.active {
  background: linear-gradient(135deg, #8c0000, #b91c1c);
  color: #ffffff;
  border-color: #8c0000;
  box-shadow: 0 6px 20px rgba(140, 0, 0, 0.25);
}

.badge-tab-discount {
  margin-left: 8px;
  background: #fee2e2;
  color: #dc2626;
  font-size: 0.75rem;
  font-weight: 800;
  padding: 2px 8px;
  border-radius: 10px;
}

.campaign-tab-btn.active .badge-tab-discount {
  background: #ffffff;
  color: #8c0000;
}

/* ==================== MỤC 2: CAMPAIGNS ==================== */
.campaign-block {
  background: #ffffff;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  overflow: hidden;
  transition: all 0.3s ease;
}

.campaign-block:hover {
  border-color: #cbd5e1;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.06);
}

.campaign-header {
  background: linear-gradient(135deg, #8c0000, #b91c1c);
  color: #fff;
  padding: 24px 30px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
}

.campaign-status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.72rem;
  font-weight: 800;
  letter-spacing: 1px;
  margin-bottom: 8px;
}

.pulse-dot {
  width: 7px;
  height: 7px;
  background: #4ade80;
  border-radius: 50%;
  box-shadow: 0 0 8px #4ade80;
}

.campaign-name {
  font-family: "Playfair Display", serif;
  font-size: 1.6rem;
  font-weight: 800;
  color: #ffffff;
  margin: 0 0 6px;
}

.campaign-timing {
  font-size: 0.88rem;
  color: rgba(255, 255, 255, 0.85);
}

.campaign-discount-tag {
  background: rgba(255, 255, 255, 0.15);
  border: 2px solid rgba(255, 255, 255, 0.4);
  padding: 8px 18px;
  border-radius: 12px;
  text-align: center;
}

.campaign-discount-tag .tag-title {
  display: block;
  font-size: 0.68rem;
  font-weight: 800;
  letter-spacing: 1.5px;
  color: #fde047;
}

.campaign-discount-tag .tag-percent {
  display: block;
  font-family: "Playfair Display", serif;
  font-size: 1.5rem;
  font-weight: 900;
  color: #ffffff;
  line-height: 1;
}

/* ==================== BỘ LỌC THEO DANH MỤC ==================== */
.campaign-filters-bar {
  background: #ffffff;
}

.filter-heading {
  font-size: 0.92rem;
}

.category-filters-wrapper {
  display: flex;
  align-items: center;
}

.category-filter-btn {
  background: #f8fafc;
  border: 1.5px solid #e2e8f0;
  color: #475569;
  padding: 7px 18px;
  border-radius: 30px;
  font-weight: 600;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  display: inline-flex;
  align-items: center;
}

.category-filter-btn:hover {
  border-color: #fca5a5;
  color: #dc2626;
  background: #fff5f5;
  transform: translateY(-2px);
}

.category-filter-btn.active {
  background: linear-gradient(135deg, #b80000, #ff3c3c);
  color: #ffffff;
  border-color: transparent;
  box-shadow: 0 4px 14px rgba(184, 0, 0, 0.3);
  transform: translateY(-2px);
}

/* Products inside Campaign */
.product-promo-card {
  background: #ffffff;
  border: 1px solid #f1f5f9;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03);
  transition: all 0.3s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.product-promo-card:hover {
  transform: translateY(-4px);
  border-color: #fca5a5;
  box-shadow: 0 12px 24px rgba(239, 68, 68, 0.1);
}

.product-img-box {
  position: relative;
  height: 150px;
  overflow: hidden;
  background: #f8fafc;
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.product-promo-card:hover .product-img {
  transform: scale(1.08);
}

.product-badge-sale {
  position: absolute;
  top: 10px;
  left: 10px;
  background: linear-gradient(135deg, #dc2626, #991b1b);
  color: #fff;
  font-weight: 800;
  font-size: 0.8rem;
  padding: 3px 8px;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.25);
}

.product-info-box {
  padding: 14px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.product-category {
  font-size: 0.72rem;
  font-weight: 700;
  color: #9ca3af;
  text-transform: uppercase;
  margin-bottom: 4px;
}

.product-title {
  font-size: 0.95rem;
  font-weight: 700;
  color: #111827;
  margin: 0 0 10px;
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 38px;
}

.product-price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}

.price-current {
  font-size: 1.1rem;
  font-weight: 800;
  color: #dc2626;
}

.price-old {
  font-size: 0.82rem;
  color: #9ca3af;
  text-decoration: line-through;
}

.product-expiry-row {
  font-size: 0.75rem;
  color: #6b7280;
  margin-bottom: 12px;
}

.btn-order-product {
  margin-top: auto;
  width: 100%;
  background: #fee2e2;
  color: #dc2626;
  border: none;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 0.8rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-order-product:hover {
  background: #dc2626;
  color: #ffffff;
}

/* ==================== PHÂN TRANG (PAGINATION) ==================== */
.pagination-custom {
  padding: 8px 18px;
  background: #ffffff;
  border-radius: 50px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.06);
  border: 1px solid #f1f5f9;
}

.page-item-custom {
  list-style: none;
}

.page-link-custom {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #f1f5f9;
  background: #ffffff;
  color: #4b5563;
  border-radius: 50%;
  font-weight: 700;
  font-size: 0.92rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  outline: none;
}

.page-link-custom:hover:not(:disabled) {
  background: #fff5f5;
  color: #dc2626;
  border-color: #fca5a5;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.2);
}

.page-item-custom.active .page-link-custom {
  background: linear-gradient(135deg, #b80000, #ff3c3c);
  color: #ffffff;
  border: none;
  box-shadow: 0 6px 16px rgba(184, 0, 0, 0.35);
  transform: scale(1.08);
}

.page-item-custom.disabled .page-link-custom {
  opacity: 0.35;
  cursor: not-allowed;
  background: #f8fafc;
}

.page-link-custom i {
  font-size: 1rem;
}

@media (max-width: 768px) {
  .hero-section {
    height: auto;
    min-height: 320px;
  }
  .campaign-header {
    flex-direction: column;
    align-items: flex-start;
  }
  .page-link-custom {
    width: 36px;
    height: 36px;
    font-size: 0.85rem;
  }
}
</style>
