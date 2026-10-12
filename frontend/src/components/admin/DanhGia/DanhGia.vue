<script setup>
import { ref, computed, onMounted } from "vue";
import {
  getAllDanhGia,
  getDetailDanhGia,
} from "@/api/admin/danhgia/danhGiaApi";

const danhSach = ref([]);
const selectedItem = ref(null);
const tuKhoa = ref("");

const danhSachFilter = computed(() => {
  if (!tuKhoa.value.trim()) return danhSach.value;
  const kw = tuKhoa.value.trim().toLowerCase();
  return danhSach.value.filter(
    (x) =>
      (x.maHoaDon || "").toLowerCase().includes(kw) ||
      (x.tenKhachHang || "").toLowerCase().includes(kw),
  );
});

const LOAI_HOA_DON_MAP = {
  1: "Tại quầy",
  2: "Đặt online",
  3: "Giao hàng",
};
const formatLoaiHoaDon = (val) => {
  if (val == null) return "--";
  return LOAI_HOA_DON_MAP[val] || `Loại ${val}`;
};

const formatDate = (val) => {
  if (!val) return "--";
  return new Date(val).toLocaleDateString("vi-VN");
};
const formatCurrency = (amount) => {
  if (amount == null) return "--";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(amount);
};

onMounted(async () => {
  try {
    const res = await getAllDanhGia();
    danhSach.value = res.data || [];
    if (danhSach.value.length > 0) selectedItem.value = danhSach.value[0];
  } catch (e) {
    console.error("Lỗi load đánh giá:", e);
  }
});
</script>

<template>
  <div class="quan-ly-danh-gia">
    <div class="box" style="margin-bottom: 20px">
      <div class="box-header">
        <h2 class="main-title">
          <span class="icon-wrapper">
            <i
              class="bi bi-star-fill"
              style="font-size: 24px; color: #e11d48"
            ></i>
          </span>
          Đánh Giá Của Khách Hàng
        </h2>
        <p>Quản lý và xem chi tiết phản hồi từ khách hàng</p>
      </div>
    </div>

    <div class="danh-gia-layout">
      <!-- Sidebar / Invoice List -->
      <div class="box left-panel">
        <div class="box-header-flex" style="margin-bottom: 15px">
          <h3 class="table-title" style="font-size: 18px">Phản hồi hóa đơn</h3>
          <button class="refresh-btn p-2" style="padding: 6px 10px">
            <i class="bi bi-funnel"></i>
          </button>
        </div>

        <div class="search-box mb-3">
          <div class="input-with-icon relative">
            <i
              class="bi bi-search absolute"
              style="
                left: 10px;
                top: 50%;
                transform: translateY(-50%);
                color: #999;
              "
            ></i>
            <input
              v-model="tuKhoa"
              type="text"
              placeholder="Tìm mã hóa đơn"
              class="custom-input"
              style="padding-left: 35px"
            />
          </div>
        </div>

        <div class="feedback-list custom-scrollbar">
          <div
            v-if="danhSachFilter.length === 0"
            style="
              text-align: center;
              color: #888;
              font-size: 14px;
              padding: 20px 0;
            "
          >
            Không có đánh giá nào
          </div>
          <div
            v-for="item in danhSachFilter"
            :key="item.id"
            @click="selectedItem = item"
            class="feedback-item"
            :class="{ active: selectedItem?.id === item.id }"
          >
            <div
              style="
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 6px;
              "
            >
              <div class="text-bold" style="font-size: 15px; color: #1a1a1a">
                {{ item.maHoaDon || "--" }}
              </div>
              <div style="font-size: 13px; color: #666">
                {{ item.tenKhachHang || "Khách vãng lai" }}
              </div>
            </div>

            <div
              style="
                display: flex;
                justify-content: space-between;
                align-items: center;
              "
            >
              <div class="stars-list">
                <i
                  v-for="n in 5"
                  :key="n"
                  class="bi"
                  :class="
                    n <= item.soSao
                      ? 'bi-star-fill text-warning'
                      : 'bi-star text-muted'
                  "
                  style="font-size: 14px; margin-right: 2px"
                ></i>
              </div>
              <div style="font-size: 12px; color: #aaa">
                {{ formatDate(item.ngayDanhGia) }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Main Content / Details -->
      <div class="box right-panel">
        <div
          class="box-header"
          style="padding-bottom: 15px; border-bottom: 1px solid #eee"
        >
          <h3 class="table-title" style="font-size: 18px">Chi tiết phản hồi</h3>
        </div>

        <div v-if="selectedItem" class="detail-content custom-scrollbar">
          <!-- Top Info Section -->
          <!-- Top Info Section -->
          <div class="top-info-section">
            <!-- Thông tin khách hàng -->
            <div class="info-column customer-col">
              <div style="color: #666; font-size: 13px; margin-bottom: 24px">
                THÔNG TIN KHÁCH HÀNG
              </div>
              <div
                class="text-bold"
                style="font-size: 18px; margin-bottom: 20px"
              >
                {{ selectedItem.tenKhachHang }}
              </div>
              <div style="color: #555; font-size: 15px">
                <i
                  class="bi bi-telephone-fill"
                  style="margin-right: 5px; font-size: 12px; color: #888"
                ></i>
                {{ selectedItem.soDienThoai ?? "Không xác định" }}
              </div>
            </div>

            <div class="divider-vertical"></div>

            <!-- Thông tin hóa đơn -->
            <div class="info-column invoice-col">
              <div style="color: #666; font-size: 13px; margin-bottom: 8px">
                THÔNG TIN HÓA ĐƠN
              </div>
              <div
                class="text-bold"
                style="font-size: 18px; color: #333; margin-bottom: 8px"
              >
                {{ selectedItem.maHoaDon }}
              </div>

              <div class="info-grid-3">
                <div class="info-cell">
                  <span class="info-label">
                    <i
                      class="ri-file-list-3-line"
                      style="font-size: 14px; color: #aaa"
                    ></i>
                    Loại hóa đơn:
                  </span>
                  <span class="text-bold">{{
                    formatLoaiHoaDon(selectedItem.loaiHoaDon)
                  }}</span>
                </div>

                <div class="info-cell">
                  <span class="info-label">
                    <i
                      class="ri-user-star-line"
                      style="font-size: 14px; color: #aaa"
                    ></i>
                    Nhân viên xử lý:
                  </span>
                  <span class="text-bold">{{
                    selectedItem.tenNhanVien || "--"
                  }}</span>
                </div>

                <div class="info-cell" style="grid-row: span 2">
                  <span class="info-label">
                    <i
                      class="ri-map-pin-line"
                      style="font-size: 14px; color: #aaa"
                    ></i>
                    Vị trí phục vụ:
                  </span>
                  <span
                    class="text-bold"
                    style="display: flex; flex-wrap: wrap; gap: 6px"
                  >
                    <template v-if="selectedItem.tenBanAn?.length">
                      <span
                        v-for="(ten, i) in selectedItem.tenBanAn"
                        :key="i"
                        class="ban-tag"
                        style="white-space: nowrap"
                      >
                        Bàn {{ ten }} - {{ selectedItem.khuVuc?.[i] ?? "--" }}
                      </span>
                    </template>
                    <span v-else style="color: #aaa">--</span>
                  </span>
                </div>

                <div class="info-cell">
                  <span class="info-label">
                    <i
                      class="ri-calendar-line"
                      style="font-size: 14px; color: #aaa"
                    ></i>
                    Ngày thanh toán:
                  </span>
                  <span class="text-bold">{{
                    formatDate(selectedItem.thoiGianThanhToan)
                  }}</span>
                </div>

                <div class="info-cell">
                  <span class="info-label">
                    <i
                      class="ri-money-dollar-circle-line"
                      style="font-size: 14px; color: #aaa"
                    ></i>
                    Tổng tiền thanh toán:
                  </span>
                  <span class="text-bold" style="color: #e11d48">{{
                    formatCurrency(selectedItem.tongTienDaThanhToan)
                  }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Rating Overview -->
          <div class="rating-overview-box">
            <h3 style="font-size: 16px; font-weight: 600; margin-bottom: 10px">
              Tổng Quan Đánh Giá
            </h3>
            <div class="big-stars">
              <i
                v-for="n in 5"
                :key="n"
                class="bi"
                :class="
                  n <= selectedItem.soSao
                    ? 'bi-star-fill text-warning'
                    : 'bi-star text-muted'
                "
              ></i>
            </div>
            <div style="font-weight: 500; color: #555">
              Đánh giá chung: {{ selectedItem.soSao }} sao
            </div>
          </div>

          <!-- Detailed Feedback -->
          <div class="detailed-feedback">
            <h3 style="font-size: 16px; font-weight: 600; margin-bottom: 15px">
              Đánh giá chi tiết của khách hàng
            </h3>
            <div class="feedback-card">
              <div class="feedback-header">
                <div class="feedback-user">
                  <span class="text-bold">{{ selectedItem.tenKhachHang }}</span>
                </div>
                <div class="feedback-time">
                  {{ formatDate(selectedItem.ngayDanhGia) }}
                </div>
              </div>
              <p class="feedback-text">
                {{ selectedItem.noiDung }}
              </p>
            </div>
          </div>
        </div>

        <div v-else class="empty-state">
          Vui lòng chọn một đánh giá để xem chi tiết
        </div>

        <div class="panel-footer">*Chế độ xem chỉ sổ: Không thể phản hồi</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ================== LAYOUT & COMPONENTS CHUNG ================== */
.box {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #eee;
}
.box-header {
  margin-bottom: 10px;
}
.box-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* ===== HEADER ===== */
.main-title {
  display: flex;
  align-items: center;
  font-size: 30px;
  font-weight: 600;
  gap: 12px;
  letter-spacing: 1px;
  color: #1a1a1a;
  margin: 0;
}
.icon-wrapper {
  width: 56px;
  height: 56px;
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
.box-header p {
  margin: 10px 0 0;
  color: #666;
  font-size: 14px;
}

.table-title {
  display: flex;
  align-items: center;
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}

/* ===== INPUTS & BUTTONS ===== */
.custom-input {
  height: 42px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 14px;
  outline: none;
  transition: all 0.2s;
  width: 100%;
}
.custom-input:focus {
  border-color: #e11d48;
  box-shadow: 0 0 0 4px rgba(225, 29, 72, 0.1);
}

.refresh-btn {
  background: #f3f4f6;
  color: #374151;
  border: none;
  border-radius: 7px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.refresh-btn:hover {
  background: #e5e7eb;
}
.text-bold {
  font-weight: 600;
}

/* ================== DANH GIA SPECIFIC LAYOUT ================== */
.danh-gia-layout {
  display: flex;
  gap: 20px;
  height: calc(100vh - 180px);
  min-height: 500px;
}

/* --- Left Panel --- */
.left-panel {
  width: 320px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  padding: 15px;
}
.feedback-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-right: 5px;
}
.feedback-item {
  padding: 12px 15px;
  border: 1px solid #eee;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}
.feedback-item:hover {
  border-color: #ddd;
  background: #fafafa;
}
.feedback-item.active {
  border-color: #e11d48;
  background: #fff0f2;
}

/* --- Right Panel --- */
.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
}
.right-panel .box-header {
  padding: 15px 20px;
  margin: 0;
}
.detail-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}
.panel-footer {
  padding: 12px 20px;
  border-top: 1px solid #eee;
  font-size: 13px;
  color: #888;
  background: #fafafa;
  border-radius: 0 0 8px 8px;
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #888;
  font-size: 15px;
}

/* --- Details Styling --- */
.top-info-section {
  display: flex;
  align-items: stretch;
  min-height: 120px;
  padding-bottom: 25px;
  margin-bottom: 25px;
  border-bottom: 1px solid #eee;
}
.info-column {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}
.customer-col {
  flex: 3;
  align-items: center;
  text-align: center;
}
.invoice-col {
  flex: 7;
  align-items: flex-start;
  text-align: left;
}
.divider-vertical {
  width: 1px;
  align-self: stretch;
  background: #eee;
  margin: 0 24px;
  flex-shrink: 0;
}

.ban-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: #fff0f2;
  border: 1px solid #fecdd3;
  color: #e11d48;
  border-radius: 20px;
  padding: 2px 10px;
  font-size: 13px;
  font-weight: 500;
  margin-right: 5px;
}
.info-grid {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 8px 16px;
  font-size: 14px;
  align-items: center;
}

.rating-overview-box {
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  margin-bottom: 30px;
  background: #fafafa;
}
.big-stars {
  margin-bottom: 8px;
}
.big-stars i {
  font-size: 32px;
  margin: 0 5px;
}

.info-grid-3 {
  display: grid;
  grid-template-columns: 1fr 1fr 1.2fr;
  gap: 16px 20px;
  width: 100%;
}
.info-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 14px;
}
.info-label {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #666;
}

.feedback-card {
  background: #f4f6f8;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #ebeef2;
}
.feedback-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}
.feedback-user {
  display: flex;
  align-items: center;
  gap: 10px;
}
.small-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}
.feedback-time {
  font-size: 12px;
  color: #888;
}
.feedback-text {
  font-size: 14px;
  color: #444;
  line-height: 1.6;
  margin: 0;
}

/* Scrollbar */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #ddd;
  border-radius: 10px;
}
.custom-scrollbar:hover::-webkit-scrollbar-thumb {
  background-color: #ccc;
}

.text-warning {
  color: #f59e0b !important;
}
.text-muted {
  color: #9ca3af !important;
}
.text-dark {
  color: #374151 !important;
}
.relative {
  position: relative;
}
.absolute {
  position: absolute;
}
</style>
