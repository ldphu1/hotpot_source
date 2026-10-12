<script setup>
import { ref, onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
/* ===== ICON ===== */
import iconBack from "@/assets/icons/prev.png";
import { useUtils } from "@/utils/helper";
import { detailSetLau } from "@/api/admin/lau/setLauApi";

const route = useRoute();
const router = useRouter();
const { nprogress } = useUtils();

const dataDetail = ref(null);
const id = route.params.id;

const fetchDetail = async () => {
  nprogress.start();
  try {
    const res = await detailSetLau(id);
    dataDetail.value = res.data;
  } catch (err) {
    console.error("Lỗi lấy chi tiết:", err);
  } finally {
    nprogress.done();
  }
};
const giaPhuThuNuocLau = computed(() => {
  return Number(dataDetail.value?.giaPhuThu || 0);
});
onMounted(fetchDetail);

const getImgUrl = (path) =>
  path
    ? `http://localhost:8080/uploads/${path}`
    : "https://via.placeholder.com/300x300?text=No+Image";

const tongGiaThanhPhan = computed(() => {
  if (!dataDetail.value?.thanhPhans) return 0;
  return dataDetail.value.thanhPhans.reduce(
    (total, item) => total + item.gia * item.soLuong,
    0,
  );
});

const tongGiaGoiY = computed(() => {
  return tongGiaThanhPhan.value + giaPhuThuNuocLau.value;
});

const chuyenUpdateSetLau = (id) => {
  router.push(`/admin/set-lau/sua/${id}`);
};

// Phân trang thành phần chi tiết
const thanhPhanPage = ref(1);
const thanhPhanPageSize = 5;

const pagedThanhPhans = computed(() => {
  if (!dataDetail.value?.thanhPhans) return [];
  const start = (thanhPhanPage.value - 1) * thanhPhanPageSize;
  return dataDetail.value.thanhPhans.slice(start, start + thanhPhanPageSize);
});

const thanhPhanTotalPages = computed(() =>
  Math.ceil((dataDetail.value?.thanhPhans?.length || 0) / thanhPhanPageSize),
);

const thanhPhanPageList = computed(() => {
  const total = thanhPhanTotalPages.value;
  if (total <= 5) return Array.from({ length: total }, (_, i) => i + 1);
  const pages = [1, 2, 3];
  if (total > 4) pages.push("…");
  pages.push(total);
  return pages;
});
</script>

<template>
  <div class="lau-page" v-if="dataDetail">
    <div class="box">
      <div class="box-header box-header-flex">
        <div class="header-left">
          <div class="icon-wrapper">
            <i class="bi bi-layers-fill" style="color: #d32f2f"></i>
          </div>
          <div class="title-group">
            <h2 class="title">Chi tiết set lẩu: {{ dataDetail.tenSanPham }}</h2>
            <p class="subtitle-text">
              Mã sản phẩm: <strong>{{ dataDetail.maSanPham }}</strong>
            </p>
          </div>
        </div>
        <button class="icon-btn" data-tooltip="Quay lại" @click="router.back()">
          <img :src="iconBack" />
        </button>
      </div>
    </div>

    <div class="main-grid">
      <aside class="side-panel">
        <div class="box">
          <div class="box-header">
            <h3>Hình ảnh *</h3>
          </div>
          <div class="image-detail-zone">
            <img :src="getImgUrl(dataDetail.hinhAnh)" class="main-img" />
          </div>

          <div class="info-stack mt-4">
            <div class="info-row">
              <label>Loại lẩu:</label>
              <span class="pill hoatdong">{{ dataDetail.tenNuocLau }}</span>
            </div>
            <div class="info-row">
              <label>Giá bán set:</label>
              <span class="price-text"
                >{{ Number(dataDetail.gia).toLocaleString() }}đ</span
              >
            </div>
          </div>
        </div>
      </aside>
      <section class="main-panel">
        <div class="box detail-box">
          <div class="box-header">
            <h3>Thành phần chi tiết trong set</h3>
          </div>

          <!-- LIST -->
          <div class="selection-list-scroll">
            <div
              v-for="sp in pagedThanhPhans"
              :key="sp.id"
              class="selection-card-v2"
            >
              <div class="item-left-group">
                <img :src="getImgUrl(sp.hinhAnh)" class="selection-img-v2" />

                <div class="selection-info">
                  <p class="sel-name-v2">{{ sp.tenSanPham }}</p>

                  <span class="sel-sub-text">
                    Danh mục: {{ sp.tenDanhMuc }}
                  </span>
                </div>
              </div>

              <div class="item-mid-group">
                <div class="meta-box">
                  <span class="mini-label-v2">Định lượng</span>

                  <p class="meta-val-v2">{{ sp.giaTri }} {{ sp.tenDonVi }}</p>
                </div>

                <div class="meta-box">
                  <span class="mini-label-v2">Số lượng</span>

                  <p class="meta-val-v2 quantity-text">x{{ sp.soLuong }}</p>
                </div>
              </div>

              <div class="item-right-group">
                <div class="price-box-v2">
                  <span class="mini-label-v2 text-end">Đơn giá</span>

                  <p class="sel-price-v2">
                    {{ Number(sp.gia).toLocaleString() }}đ
                  </p>
                </div>
              </div>
            </div>
            <div v-if="thanhPhanTotalPages > 1" class="pagination mt-2">
              <button
                :disabled="thanhPhanPage === 1"
                @click="thanhPhanPage--"
                class="pg-btn pg-arrow"
              >
                <i class="ri-arrow-left-s-line"></i>
              </button>
              <template v-for="p in thanhPhanPageList" :key="p">
                <button v-if="p === '…'" class="pg-btn pg-ellipsis" disabled>
                  ···
                </button>
                <button
                  v-else
                  class="pg-btn"
                  :class="{ active: p === thanhPhanPage }"
                  @click="thanhPhanPage = p"
                >
                  {{ p }}
                </button>
              </template>
              <button
                :disabled="thanhPhanPage === thanhPhanTotalPages"
                @click="thanhPhanPage++"
                class="pg-btn pg-arrow"
              >
                <i class="ri-arrow-right-s-line"></i>
              </button>
            </div>
          </div>

          <!-- SUMMARY -->
          <div class="price-summary-wrapper">
            <div class="summary-box">
              <div class="summary-row">
                <span>Giá gốc nguyên liệu:</span>

                <span class="fw-bold">
                  {{ tongGiaThanhPhan.toLocaleString() }}đ
                </span>
              </div>

              <div class="summary-row">
                <span>Phụ thu nước lẩu:</span>

                <span class="fw-bold">
                  {{ giaPhuThuNuocLau.toLocaleString() }}đ
                </span>
              </div>

              <div class="summary-row total-row">
                <label>Giá bán thực tế:</label>

                <span class="final-price">
                  {{ Number(dataDetail.gia).toLocaleString() }}đ
                </span>
              </div>
            </div>
          </div>

          <!-- DESCRIPTION -->
          <div class="description-section">
            <label>Mô tả chi tiết</label>

            <div class="desc-content">
              {{
                dataDetail.moTa || "Không có mô tả chi tiết cho sản phẩm này."
              }}
            </div>
          </div>
        </div>
      </section>
    </div>

    <div class="box footer-sticky">
      <p class="text-muted">
        * Kiểm tra kỹ các thành phần trước khi thực hiện chỉnh sửa dữ liệu.
      </p>
      <div class="header-actions">
        <button class="btn-outline" @click="router.back()">Quay lại</button>
        <button class="btn-primary-custom" @click="chuyenUpdateSetLau(id)">
          <i class="bi bi-check2-all"></i>
          Chỉnh sửa set lẩu
          <div class="btn-light-sweep"></div>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* /////////// 2 NÚT //////////// */

.btn-light-sweep {
  position: absolute;
  top: 0;
  left: -150%;
  width: 40%;
  height: 100%;
  background: linear-gradient(
    to right,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.4) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  transform: skewX(-25deg);
  transition: 0s;
  z-index: 2;
  pointer-events: none;
}
.btn-primary-custom:hover .btn-light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
}

/* /////////////////// */
.selection-card-v2 {
  display: grid;
  grid-template-columns: 1.2fr 1fr 1fr;
  align-items: center;
  padding: 12px 20px;
  background: white;
  border: 1px solid #d32f2f; /* Viền đỏ mảnh */
  border-radius: 10px;
  margin-bottom: 12px;
  transition: 0.2s;
}

.selection-card-v2:hover {
  background: #fffcfc;
  box-shadow: 0 4px 12px rgba(211, 47, 47, 0.08);
}

.item-left-group {
  display: flex;
  align-items: center;
  gap: 15px;
}
.selection-img-v2 {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  object-fit: cover;
}

.sel-name-v2 {
  font-weight: 800;
  font-size: 17px;
  margin: 0;
  color: #0f172a;
}
.sel-sub-text {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

/* Sửa lại cụm giữa để chứa được 2 thông tin */
.item-mid-group {
  display: grid;
  grid-template-columns: 120px 80px;
  gap: 20px;
}
.mini-label-v2 {
  display: block;
  font-weight: 700;
  font-size: 11px;
  color: #94a3b8;
  margin-bottom: 2px;
}

.meta-val-v2 {
  font-weight: 800;
  font-size: 15px;
  color: #1e293b;
  margin: 0;
}

.item-right-group {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 20px;
}
.sel-price-v2 {
  font-weight: 900;
  color: #d32f2f;
  font-size: 18px;
  margin: 0;
}

.lau-page {
  font-family: var(--font-family, "Inter", sans-serif);
  padding: 5px;
  background: #f4f6f8;
  min-height: 100vh;
}

.box {
  background: #fff;
  border-radius: 7px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #eee;
}

.box-header {
  font-weight: 600;
  margin-bottom: 5px;
}

.box-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 7px;
  background: #f3e8e8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}
.title {
  margin: 0;
  font-size: 25px;
  font-weight: 700;
  color: #111;
  display: block; /* bỏ flex cũ */
}
.subtitle-text {
  margin: 4px 0 0;
  font-size: 15px;
  color: #6b7280;
}
.box-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.icon-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  border: none;
  background: #f4f6f8;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.25s ease;
}
.icon-btn img {
  width: 18px;
  height: 18px;
}
.icon-btn:hover {
  background: #e9eef3;
  transform: translateY(-1px);
}

.title img {
  width: 24px;
}

.main-grid {
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 20px;
}

.image-detail-zone {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
}

.main-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

label {
  font-weight: 700;
  color: #333;
  display: block;
  margin-bottom: 5px;
  font-size: 13px;
  text-transform: none; /* Không in hoa */
}

.info-stack {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px dashed #eee;
}

/* Đồng bộ màu giá tiền với màu Button Primary */
.price-text,
.text-primary,
.final-price {
  color: var(--color-primary, #3b6cff) !important;
}

.price-text {
  font-weight: 800;
  font-size: 18px;
}

.ingredient-item {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  padding: 15px;
  background: #f9fafb;
  border-radius: 10px;
  margin-bottom: 10px;
  border: 1px solid #f1f5f9;
  align-items: center;
}

.item-name {
  font-weight: 700;
  margin: 0;
  color: #111;
}

.item-sub {
  font-size: 12px;
  color: #666;
}

.mini-label {
  font-size: 10px;
  font-weight: 700;
  color: #999;
  text-transform: none; /* Không in hoa */
}

.val-bold {
  font-weight: 700;
  margin: 0;
}

.price-summary-wrapper {
  display: flex;
  justify-content: flex-end;
}

.summary-box {
  background: #f8fbff;
  border: 1px solid #e0e7ff;
  padding: 20px;
  border-radius: 12px;
  width: 350px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.total-row {
  border-top: 1px solid #e0e7ff;
  padding-top: 12px;
  margin-top: 10px;
}

.final-price {
  font-size: 20px;
  font-weight: 800;
}

.desc-content {
  background: #fcfcfc;
  border: 1px solid #eee;
  padding: 15px;
  border-radius: 8px;
  line-height: 1.6;
  color: #444;
}

.icon-btn {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  border: none;
  background: #f4f6f8;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.footer-sticky {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.btn-outline {
  padding: 10px 25px;
  border-radius: 7px;
  border: 1px solid #ddd;
  background: #fff;
  cursor: pointer;
  font-weight: 600;
  margin-right: 5px;
}

.btn-primary-custom {
  position: relative;
  overflow: hidden;
  padding: 10px 25px;
  border-radius: 7px;
  border: none;
  background: var(--color-primary, #3b6cff);
  color: white;
  cursor: pointer;
  font-weight: 600;
}

.pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;

  padding: 7px 14px;

  border-radius: 999px;

  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.3px;

  backdrop-filter: blur(10px);

  border: 1px solid transparent;

  transition: 0.2s ease;
}

.hoatdong {
  background: linear-gradient(
    135deg,
    rgba(211, 47, 47, 0.12),
    rgba(211, 47, 47, 0.2)
  );

  color: #b42318;

  border-color: rgba(211, 47, 47, 0.18);

  box-shadow: 0 4px 10px rgba(211, 47, 47, 0.08);
}

.hoatdong:hover {
  transform: translateY(-1px);

  box-shadow: 0 6px 16px rgba(211, 47, 47, 0.14);
}

.detail-box {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.selection-list-scroll {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quantity-text {
  color: #d32f2f;
}

.price-summary-wrapper {
  display: flex;
  justify-content: flex-end;
}

.description-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
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
  min-width: 36px;
  height: 36px;
  padding: 0 6px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  background: #fff;
  color: #333;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}
.pg-btn:hover:not(:disabled):not(.active) {
  border-color: #d32f2f;
  color: #d32f2f;
  background: #fff1f0;
}
.pg-btn.active {
  border: 1.5px solid #d32f2f;
  color: #d32f2f;
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
  min-width: 24px;
}
.pg-arrow i {
  font-size: 18px;
}
</style>
