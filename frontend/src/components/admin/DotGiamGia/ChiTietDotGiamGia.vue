<script setup>
import { ref, computed, watch, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUtils } from "@/utils/helper";
import iconBack from "@/assets/icons/prev.png";
import { getDetailDotGiamGia } from "@/api/admin/dotgiamgia/dotGiamGiaApi";

const route = useRoute();
const router = useRouter();
const { notify } = useUtils();

const loading = ref(false);
const detail = ref({
  maDot: "",
  tenDot: "",
  giaTriGiam: 0,
  ngayBatDau: "",
  ngayKetThuc: "",
  moTa: "",
  sanPhams: []
});

/* STATE CHO LỌC VÀ PHÂN TRANG SẢN PHẨM */
const filterType = ref("all"); // 'all', 'set', 'mon'
const searchKeyword = ref("");
const currentPage = ref(1);
const pageSize = ref(5);

watch([filterType, searchKeyword, pageSize], () => {
  currentPage.value = 1;
});

const loadData = async () => {
  const currentId = route.params.id;
  if (!currentId) {
    notify.error("Không tìm thấy ID");
    return;
  }

  try {
    loading.value = true;
    const res = await getDetailDotGiamGia(currentId);
    detail.value = res.data || {};
  } catch (err) {
    console.error(err);
    notify.error("Không tải được dữ liệu chi tiết");
  } finally {
    loading.value = false;
  }
};

onMounted(loadData);

const formatMoney = (v) => Number(v).toLocaleString("vi-VN");

const tinhGiaSauGiam = (gia, phanTram) => {
  if (!phanTram) return gia;
  return gia - (gia * phanTram) / 100;
};

const formatDateForDisplay = (d) => {
  if (!d) return "";
  const date = new Date(d);
  return (
    date.toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" }) +
    " " +
    date.toLocaleDateString("vi-VN")
  );
};

const formatDateForInput = (d) => {
  if (!d) return "";
  return d.slice(0, 16);
};

/* LOGIC PHÂN LOẠI + PHÂN TRANG */
const filteredProducts = computed(() => {
  let list = detail.value.sanPhams || [];

  // Phân loại
  if (filterType.value === "set") {
    list = list.filter((p) => p.laSet);
  } else if (filterType.value === "mon") {
    list = list.filter((p) => !p.laSet);
  }

  // Tìm kiếm
  if (searchKeyword.value.trim() !== "") {
    const kw = searchKeyword.value.trim().toLowerCase();
    list = list.filter((p) =>
      (p.tenSanPham || "").toLowerCase().includes(kw)
    );
  }

  return list;
});

const paginatedProducts = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return filteredProducts.value.slice(start, start + pageSize.value);
});

const totalPages = computed(() => {
  return Math.ceil(filteredProducts.value.length / pageSize.value) || 1;
});

const pageList = computed(() => {
  const total = totalPages.value;
  if (total <= 5) return Array.from({ length: total }, (_, i) => i + 1);
  const pages = [1, 2, 3];
  if (total > 4) pages.push("…");
  pages.push(total);
  return pages;
});

const goBack = () => router.back();
</script>

<template>
  <div class="page" v-if="!loading">
    <div class="box">
      <div class="box-header-flex">
        <div class="box-header">
          <h2 class="main-title">
            <span class="icon-wrapper">
              <i
                class="bi bi-tags"
                style="color: rgb(225, 29, 72); font-size: 24px"
              ></i>
            </span>
            Chi tiết đợt khuyến mãi
          </h2>
          <p class="sub-title">Xem thông tin chi tiết đợt khuyến mãi và danh sách món ăn áp dụng</p>
        </div>

        <button class="icon-btn-back" @click="goBack">
          <img :src="iconBack" />
        </button>
      </div>
    </div>

    <!-- Form thông tin đợt giảm giá (Read-only) -->
    <div class="form-top-section">
      <div class="luxury-form-card">
        <h3 class="card-title">
          <span class="icon-indicator">
            <i class="bi bi-info-circle"></i>
          </span>
          Thông tin đợt giảm giá
        </h3>

        <div class="form-grid">
          <!-- Mã đợt -->
          <div class="form-group">
            <label class="form-label">Mã đợt giảm giá</label>
            <div class="input-wrapper disabled">
              <i class="bi bi-qr-code prefix-icon"></i>
              <input :value="detail.maDot" type="text" disabled />
            </div>
          </div>

          <!-- Tên đợt giảm giá -->
          <div class="form-group">
            <label class="form-label">Tên chương trình khuyến mãi</label>
            <div class="input-wrapper disabled">
              <i class="bi bi-card-text prefix-icon"></i>
              <input :value="detail.tenDot" type="text" disabled />
            </div>
          </div>

          <!-- Giá trị giảm -->
          <div class="form-group">
            <label class="form-label">Mức giảm (%)</label>
            <div class="input-wrapper group-suffix disabled">
              <i class="bi bi-percent prefix-icon"></i>
              <input :value="detail.giaTriGiam" type="text" disabled />
              <span class="suffix-tag">%</span>
            </div>
          </div>

          <!-- Thời gian bắt đầu -->
          <div class="form-group">
            <label class="form-label">Thời gian bắt đầu</label>
            <div class="input-wrapper disabled">
              <i class="bi bi-calendar-event prefix-icon"></i>
              <input :value="formatDateForDisplay(detail.ngayBatDau)" type="text" disabled />
            </div>
          </div>

          <!-- Thời gian kết thúc -->
          <div class="form-group">
            <label class="form-label">Thời gian kết thúc</label>
            <div class="input-wrapper disabled">
              <i class="bi bi-calendar-check prefix-icon"></i>
              <input :value="formatDateForDisplay(detail.ngayKetThuc)" type="text" disabled />
            </div>
          </div>

          <!-- Mô tả chi tiết -->
          <div class="form-group full-width">
            <label class="form-label">Mô tả chương trình</label>
            <textarea :value="detail.moTa" rows="3" disabled></textarea>
          </div>
        </div>
      </div>
    </div>

    <!-- Danh sách sản phẩm áp dụng -->
    <div class="mt-3">
      <div class="luxury-form-card">
        <h3 class="card-title">
          <span class="icon-indicator">
            <i class="bi bi-list-stars"></i>
          </span>
          Sản phẩm áp dụng ({{ detail.sanPhams?.length || 0 }})
        </h3>
        
        <!-- BỘ LỌC PHÂN LOẠI & TÌM KIẾM -->
        <div class="product-filter-bar mb-3">
          <div class="filter-tabs">
            <button 
              class="filter-tab-btn" 
              :class="{ active: filterType === 'all' }" 
              @click="filterType = 'all'"
            >
              Tất cả ({{ detail.sanPhams?.length || 0 }})
            </button>
            <button 
              class="filter-tab-btn" 
              :class="{ active: filterType === 'set' }" 
              @click="filterType = 'set'"
            >
              Set lẩu ({{ detail.sanPhams?.filter(p => p.laSet).length || 0 }})
            </button>
            <button 
              class="filter-tab-btn" 
              :class="{ active: filterType === 'mon' }" 
              @click="filterType = 'mon'"
            >
              Món lẻ ({{ detail.sanPhams?.filter(p => !p.laSet).length || 0 }})
            </button>
          </div>
          <div class="filter-search">
            <div class="search-input-wrapper">
              <i class="bi bi-search search-icon"></i>
              <input 
                v-model="searchKeyword" 
                type="text" 
                placeholder="Tìm kiếm sản phẩm..." 
                class="search-input"
              />
            </div>
          </div>
        </div>
        
        <div class="table-wrapper">
          <table class="pgg-table">
            <thead>
              <tr>
                <th width="80">STT</th>
                <th width="120">Hình ảnh</th>
                <th style="text-align: left">Tên sản phẩm</th>
                <th width="150">Giá gốc</th>
                <th width="150">Giá ưu đãi</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(sp, idx) in paginatedProducts" :key="sp.id">
                <td>{{ (currentPage - 1) * pageSize + idx + 1 }}</td>
                <td>
                  <img
                    :src="
                      sp.hinhAnh
                        ? sp.hinhAnh.startsWith('http')
                          ? sp.hinhAnh
                          : `http://localhost:8080/uploads/${sp.hinhAnh}`
                        : '/no-image.png'
                    "
                    class="product-img"
                    @error="(e) => (e.target.src = '/no-image.png')"
                  />
                </td>
                <td style="text-align: left; font-weight: 600; color: #333">
                  {{ sp.tenSanPham }}
                </td>
                <td style="text-decoration: line-through; color: #999">
                  {{ formatMoney(sp.gia) }} đ
                </td>
                <td style="color: #d70018; font-weight: 700">
                  {{ formatMoney(tinhGiaSauGiam(sp.gia, detail.giaTriGiam)) }} đ
                </td>
              </tr>
              <tr v-if="filteredProducts.length === 0">
                <td colspan="5" class="empty-row">
                  Không có sản phẩm nào phù hợp bộ lọc
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- PHÂN TRANG -->
        <div class="pagination-bar mt-3" v-if="filteredProducts.length > 0">
          <div class="d-flex align-items-center gap-2">
            <span>Hiển thị</span>
            <select
              class="form-select-sm limit-select"
              v-model="pageSize"
            >
              <option :value="5">5 dòng</option>
              <option :value="10">10 dòng</option>
              <option :value="20">20 dòng</option>
            </select>
          </div>

          <div class="pagination">
            <button
              :disabled="currentPage === 1"
              @click="currentPage--"
              class="pg-btn pg-arrow"
            >
              <i class="ri-arrow-left-s-line"></i>
            </button>
            <template v-for="p in pageList" :key="p">
              <button v-if="p === '…'" class="pg-btn pg-ellipsis" disabled>
                ···
              </button>
              <button
                v-else
                class="pg-btn"
                :class="{ active: p === currentPage }"
                @click="currentPage = p"
              >
                {{ p }}
              </button>
            </template>
            <button
              :disabled="currentPage === totalPages"
              @click="currentPage++"
              class="pg-btn pg-arrow"
            >
              <i class="ri-arrow-right-s-line"></i>
            </button>
          </div>

          <span class="total-count text-muted">
            Hiển thị {{ paginatedProducts.length }} / {{ filteredProducts.length }} sản phẩm
          </span>
        </div>
      </div>
    </div>
  </div>
  <div v-else class="loading-state">
    <div class="spinner"></div>
    <p>Đang tải dữ liệu...</p>
  </div>
</template>

<style scoped>
.page {
  font-family: var(--font-family);
}

.box {
  background: #fff;
  border-radius: 10px;
  padding: 20px 24px;
  margin-bottom: 20px;
  border: 1px solid #eee;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}

.box-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.main-title {
  display: flex;
  align-items: center;
  font-size: 28px;
  font-weight: 600;
  gap: 12px;
  letter-spacing: 1px;
  margin-bottom: 5px;
  color: #1a1a1a;
}

.icon-wrapper {
  width: 56px;
  height: 56px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 9px;
  background: linear-gradient(
    135deg,
    rgba(225, 29, 72, 0.08),
    rgba(225, 29, 72, 0.15)
  );
  border: 1px solid rgba(225, 29, 72, 0.1);
}

.sub-title {
  font-size: 14px;
  color: #888;
  margin: 0;
}

.icon-btn-back {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background-color: #f4f6f8;
  border: 1px solid #e9e9e9;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.icon-btn-back:hover {
  background-color: #e2e6ea;
  transform: translateX(-2px);
}

.icon-btn-back img {
  width: 20px;
  height: 20px;
  object-fit: contain;
}

/* Luxury Form Card style matching AddDotGiamGia */
.luxury-form-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 650;
  color: #1e293b;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 1.5px dashed #e2e8f0;
}

.icon-indicator {
  color: #d70018;
  font-size: 20px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.full-width {
  grid-column: span 2;
}

.form-label {
  font-size: 14px;
  font-weight: 550;
  color: #475569;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.prefix-icon {
  position: absolute;
  left: 14px;
  color: #94a3b8;
  font-size: 16px;
}

.input-wrapper input,
textarea {
  width: 100%;
  padding: 10px 14px 10px 40px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 14px;
  color: #334155;
  background-color: #f8fafc;
  transition: all 0.2s ease;
}

textarea {
  padding-left: 14px;
}

.input-wrapper.disabled input,
textarea:disabled {
  background-color: #f1f5f9;
  color: #64748b;
  border-color: #e2e8f0;
  cursor: not-allowed;
}

.group-suffix input {
  padding-right: 45px;
}

.suffix-tag {
  position: absolute;
  right: 14px;
  color: #64748b;
  font-weight: 600;
  font-size: 14px;
}

/* Table Style */
.table-wrapper {
  overflow-x: auto;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.pgg-table {
  width: 100%;
  border-collapse: collapse;
}

.pgg-table th {
  background: #f8fafc;
  color: #475569;
  font-weight: 600;
  font-size: 14px;
  padding: 12px 16px;
  border-bottom: 1px solid #e2e8f0;
}

.pgg-table td {
  padding: 14px 16px;
  border-bottom: 1px solid #e2e8f0;
  font-size: 14px;
  text-align: center;
  color: #334155;
}

.pgg-table tbody tr:hover {
  background-color: #f8fafc;
}

.product-img {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #e2e8f0;
}

.empty-row {
  padding: 40px 0 !important;
  color: #94a3b8;
  font-style: italic;
}

.mt-3 {
  margin-top: 1rem;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: #64748b;
}

.spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #d70018;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Filter bar & search styling */
.product-filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
  background: #f8fafc;
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.filter-tabs {
  display: flex;
  gap: 8px;
}

.filter-tab-btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 13.5px;
  font-weight: 550;
  border: 1px solid #cbd5e1;
  background: #fff;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-tab-btn:hover {
  background: #f1f5f9;
  color: #1e293b;
}

.filter-tab-btn.active {
  background: #d70018;
  color: #fff;
  border-color: #d70018;
  box-shadow: 0 2px 4px rgba(215, 0, 24, 0.15);
}

.filter-search {
  flex-shrink: 0;
  width: 250px;
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 12px;
  color: #94a3b8;
  font-size: 14px;
}

.search-input {
  width: 100%;
  padding: 8px 12px 8px 36px !important;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 13.5px;
  background: #fff !important;
  color: #334155;
}

/* Pagination bar styling */
.pagination-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
  padding-top: 15px;
  border-top: 1px solid #e2e8f0;
  font-size: 13.5px;
}

.limit-select {
  padding: 4px 8px;
  border-radius: 6px;
  border: 1px solid #cbd5e1;
  background: #fff;
  font-size: 13px;
  color: #334155;
  cursor: pointer;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  user-select: none;
}
.pg-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
  padding: 0 6px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  background: #fff;
  color: #333;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}
.pg-btn:hover:not(:disabled):not(.active) {
  border-color: #d70018;
  color: #d70018;
  background: #fff1f0;
}
.pg-btn.active {
  border: 1.5px solid #d70018;
  color: #d70018;
  font-weight: 600;
}
.pg-btn:disabled {
  border-color: #f0f0f0;
  background: #f5f5f5;
  color: #bfbfbf;
  cursor: not-allowed;
}
.pg-ellipsis {
  border: none;
  background: transparent;
  cursor: default;
  color: #aaa;
  min-width: 20px;
}
.pg-arrow i {
  font-size: 16px;
}

.total-count {
  font-size: 13px;
}

.mb-3 {
  margin-bottom: 1rem;
}
</style>
