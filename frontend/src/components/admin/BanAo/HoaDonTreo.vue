<script setup>
import { ref, computed, onMounted } from "vue";
import { useUtils } from "@/utils/helper";
import { getAllBanAo } from "@/api/admin/banao/banAoApi";

/* ICON */
import iconHoaDon from "@/assets/icons/don-hang.png";
import iconDanhSach from "@/assets/icons/danh-sach.png";
import iconEye from "@/assets/icons/eye.png";
import { useRouter } from "vue-router";

const { notify, nprogress } = useUtils();
const router = useRouter();

/* ===== DATA THẬT ===== */
const danhSachBanAo = ref([]);

const loadDanhSach = async () => {
  nprogress.start();
  try {
    const res = await getAllBanAo();
    danhSachBanAo.value = res.data || [];
  } catch (e) {
    console.error("Lỗi tải danh sách bill treo:", e);
    notify.error("Không tải được danh sách bill treo");
  } finally {
    nprogress.done();
  }
};

onMounted(() => {
  loadDanhSach();
});

const keyword = ref("");
const selectTrangThai = ref("");
const ngayStart = ref("");
const ngayEnd = ref("");
const pageSize = ref(5);
const currentPage = ref(1);

const formatMoney = (money) => {
  if (!money) return "0 đ";
  return money.toLocaleString("vi-VN") + " đ";
};
const formatDate = (date) => {
  if (!date) return "";
  return new Date(date).toLocaleString("vi-VN");
};

const convertTrangThai = (status) => {
  switch (status) {
    case "CHUA_THANH_TOAN":
      return "Đang treo nợ";
    case "DA_THANH_TOAN":
      return "Hoàn thành";
    case "NO_XAU":
      return "Nợ xấu";
    default:
      return "Không xác định";
  }
};
const getStatusClass = (status) => {
  switch (status) {
    case "CHUA_THANH_TOAN":
      return "status-treo";
    case "DA_THANH_TOAN":
      return "status-wait-payment";
    case "NO_XAU":
      return "status-expired";
    default:
      return "status-other";
  }
};

const SO_GIO_CANH_BAO = 24;
const laTreoQuaLau = (item) => {
  if (item.trangThaiThanhToan !== "CHUA_THANH_TOAN") return false;
  const gioTreo =
    (Date.now() - new Date(item.thoiGianTao).getTime()) / (1000 * 60 * 60);
  return gioTreo > SO_GIO_CANH_BAO;
};

const danhSachSauLoc = computed(() => {
  let data = danhSachBanAo.value;

  if (keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase();
    data = data.filter(
      (item) =>
        (item.maBanAo || "").toLowerCase().includes(kw) ||
        (item.maHoaDon || "").toLowerCase().includes(kw) ||
        (item.tenKhachHang || "").toLowerCase().includes(kw) ||
        (item.soDienThoai || "").includes(kw),
    );
  }
  if (selectTrangThai.value !== "") {
    data = data.filter(
      (item) => item.trangThaiThanhToan === selectTrangThai.value,
    );
  }
  if (ngayStart.value) {
    const start = new Date(ngayStart.value);
    data = data.filter((item) => new Date(item.thoiGianTao) >= start);
  }
  if (ngayEnd.value) {
    const end = new Date(ngayEnd.value);
    end.setHours(23, 59, 59);
    data = data.filter((item) => new Date(item.thoiGianTao) <= end);
  }
  return data;
});

const totalItems = computed(() => danhSachSauLoc.value.length);
const totalPages = computed(
  () => Math.ceil(totalItems.value / pageSize.value) || 1,
);
const danhSachHienThi = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return danhSachSauLoc.value.slice(start, start + pageSize.value);
});

const goFirst = () => (currentPage.value = 1);
const goPrev = () => {
  if (currentPage.value > 1) currentPage.value--;
};
const goNext = () => {
  if (currentPage.value < totalPages.value) currentPage.value++;
};
const goLast = () => (currentPage.value = totalPages.value);

const refreshData = () => {
  keyword.value = "";
  selectTrangThai.value = "";
  ngayStart.value = "";
  ngayEnd.value = "";
  currentPage.value = 1;
  loadDanhSach();
};

const goToDetail = (item) => {
  router.push(`/admin/hoa-don/detail/${item.idHoaDon}`);
};

const thanhToan = (item) => {
  router.push(`/admin/hoa-don-treo/thanh-toan/${item.id}`);
};
</script>

<template>
  <div class="quan-ly-hoa-don-treo">
    <!-- HEADER -->
    <div class="box">
      <div class="box-header">
        <h2 class="main-title">
          <span class="icon-wrapper">
            <img :src="iconHoaDon" alt="Hóa đơn treo" />
          </span>
          Hóa Đơn Treo Tạm Thời
        </h2>
        <p>Quản lý danh sách hóa đơn đang được treo tạm thời trong hệ thống</p>
      </div>
    </div>

    <!-- BO LOC -->
    <div class="filter-box">
      <div class="filter-item">
        <label>Tìm kiếm</label>
        <input
          placeholder="Mã hóa đơn, tên, số điện thoại..."
          type="text"
          v-model="keyword"
          @input="currentPage = 1"
          class="custom-input"
        />
      </div>
      <div class="filter-item">
        <label>Từ ngày</label>
        <input
          type="date"
          v-model="ngayStart"
          class="custom-input"
          @change="currentPage = 1"
        />
      </div>
      <div class="filter-item">
        <label>Đến ngày</label>
        <input
          type="date"
          v-model="ngayEnd"
          class="custom-input"
          @change="currentPage = 1"
        />
      </div>
    </div>

    <!-- BANG DANH SACH -->
    <div class="box">
      <div class="box-header box-header-flex">
        <h3 class="table-title">
          <span class="icon-wrapper-small">
            <img :src="iconDanhSach" class="icon-title-small" />
          </span>
          Danh sách hóa đơn treo
        </h3>
        <div class="header-actions">
          <button class="refresh-btn" @click="refreshData()">
            <img src="@/assets/icons/refresh.png" alt="" />
            <span>Làm mới</span>
          </button>
        </div>
      </div>

      <!-- TABLE -->
      <div class="table-content">
        <table class="table-main">
          <thead>
            <tr>
              <th>STT</th>
              <th style="text-align: left">Mã đơn treo</th>
              <th style="text-align: left">Bàn ăn</th>
              <th style="text-align: left">Tên khách</th>
              <th style="text-align: left">Số điện thoại</th>
              <th style="text-align: right">Đã thanh toán</th>
              <th style="text-align: right">Còn lại</th>
              <th style="text-align: left">Thời gian treo</th>
              <th>Trạng thái</th>
              <th style="text-align: center">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in danhSachHienThi" :key="item.id">
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
              <td style="text-align: left">
                <span class="ma-hoa-don">{{ item.maBanAo }}</span>
              </td>
              <td style="text-align: left">
                <span class="table-tag"
                  >Bàn {{ item.tenBanGoc || "--" }} - {{ item.tenKhuVuc }}</span
                >
              </td>
              <td class="text-bold" style="text-align: left">
                {{ item.tenKhachHang || "Khách lẻ" }}
              </td>
              <td style="text-align: left">{{ item.soDienThoai || "--" }}</td>
              <td class="text-bold" style="text-align: right">
                {{ formatMoney(item.tongTienDaThanhToan) }}
              </td>
              <td class="text-bold" style="text-align: right">
                {{ formatMoney(item.tienConLai) }}
              </td>
              <td style="text-align: left">
                {{ formatDate(item.thoiGianTao) }}
                <span
                  v-if="laTreoQuaLau(item)"
                  class="canh-bao-treo-lau"
                  v-tooltip="'Treo hơn 24h'"
                  >⚠️</span
                >
              </td>
              <td>
                <span
                  class="status-text"
                  :class="getStatusClass(item.trangThaiThanhToan)"
                >
                  {{ convertTrangThai(item.trangThaiThanhToan) }}
                </span>
              </td>
              <td class="text-center">
                <button
                  class="btn-action-icon btn-detail"
                  @click="goToDetail(item)"
                  v-tooltip="'Xem chi tiết'"
                >
                  <img :src="iconEye" class="img-icon-action" />
                </button>
                <button
                  v-if="item.trangThaiThanhToan === 'CHUA_THANH_TOAN' || item.trangThaiThanhToan === 'NO_XAU'"
                  class="btn-action-icon btn-payment"
                  @click="thanhToan(item)"
                  v-tooltip="'Thanh toán'"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="14"
                    height="14"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <rect x="1" y="4" width="22" height="16" rx="2" ry="2" />
                    <line x1="1" y1="10" x2="23" y2="10" />
                  </svg>
                </button>
              </td>
            </tr>
            <tr v-if="danhSachHienThi.length === 0">
              <td colspan="10" class="empty-table">
                Không tìm thấy hóa đơn treo phù hợp
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- PAGINATION -->
      <div class="pagination-footer">
        <div class="pagination-left">
          <span>Hiển thị</span>
          <select
            class="form-select form-select-sm w-auto"
            v-model="pageSize"
            @change="currentPage = 1"
          >
            <option :value="5">5 dòng</option>
            <option :value="10">10 dòng</option>
            <option :value="20">20 dòng</option>
          </select>
        </div>
        <nav class="pagination-container">
          <ul class="pagination-premium">
            <li>
              <button
                class="nav-action-btn"
                :disabled="currentPage === 1"
                @click="goFirst"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <path d="m11 17-5-5 5-5" />
                  <path d="m18 17-5-5 5-5" />
                </svg>
              </button>
            </li>
            <li>
              <button
                class="nav-action-btn"
                :disabled="currentPage === 1"
                @click="goPrev"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                >
                  <path d="m15 18-6-6 6-6" />
                </svg>
              </button>
            </li>
            <li>
              <div class="page-indicator-badge">
                <span class="current-number">{{ currentPage }}</span>
                <span class="divider">/</span>
                <span class="total-number">{{ totalPages }}</span>
              </div>
            </li>
            <li>
              <button
                class="nav-action-btn"
                :disabled="currentPage === totalPages"
                @click="goNext"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <path d="m9 18 6-6-6-6" />
                </svg>
              </button>
            </li>
            <li>
              <button
                class="nav-action-btn"
                :disabled="currentPage === totalPages"
                @click="goLast"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <path d="m6 17 5-5-5-5" />
                  <path d="m13 17 5-5-5-5" />
                </svg>
              </button>
            </li>
          </ul>
        </nav>
        <div class="pagination-right">
          Hiển thị {{ danhSachHienThi.length }} / {{ totalItems }} hóa đơn
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.box {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
  border: 1px solid #eee;
}
.box-header {
  margin-bottom: 16px;
}
.box-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
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
.icon-wrapper img {
  width: 28px;
  height: 28px;
  object-fit: contain;
}
.box-header p {
  margin: 10px 0 0;
  color: var(--color-text-muted);
}
.table-title {
  display: flex;
  align-items: center;
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}
.icon-wrapper-small {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background-color: #f0eaea;
  border-radius: 8px;
  margin-right: 10px;
}
.icon-title-small {
  width: 18px;
  height: 18px;
}
.filter-box {
  background: white;
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 24px;
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 12px;
  border: 1px solid #eee;
}
.filter-item {
  flex: 1;
  min-width: 150px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.filter-item label {
  font-size: 14px;
  font-weight: 600;
  color: #666;
}
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
.custom-input:hover {
  border-color: #c8d0dc;
}
.custom-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12);
}
.header-actions {
  display: flex;
  gap: 10px;
}
.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8px;

  padding: 9px 14px;

  border: none;
  border-radius: 12px;

  background: #f3f4f6;
  color: #ca2525;

  font-size: 13px;
  font-weight: 600;

  cursor: pointer;
  transition: all 0.25s ease;
}

.refresh-btn img {
  width: 16px;
  height: 16px;
  object-fit: contain;
}

.refresh-btn:hover {
  background: #e5e7eb;
  transform: translateY(-1px);
}

.refresh-btn:hover img {
  transform: rotate(180deg);
  transition: transform 0.4s ease;
}
.add-btn,
.excel-btn,
.refresh-btn {
  padding: 9px 14px;
  gap: 8px;
  font-size: 13px;
  border-radius: 7px;
}

.add-btn img,
.excel-btn img,
.refresh-btn img {
  width: 16px;
  height: 16px;
}
.status-filter-tabs {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  overflow-x: auto;
  scrollbar-width: none;
  padding: 4px 0;
}
.status-filter-tabs::-webkit-scrollbar {
  display: none;
}
.status-tab-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding: 8px 16px;
  font-size: 13.5px;
  font-weight: 600;
  color: #64748b;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s ease;
}
.status-tab-btn:hover {
  color: #1e293b;
  border-color: #cbd5e1;
  background: #f8fafc;
}
.status-tab-btn.active {
  color: #d01414;
  border-color: #d01414;
  background: #fdf2f2;
  font-weight: 700;
}
.status-count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  font-size: 11px;
  font-weight: 700;
  line-height: 1;
  border-radius: 999px;
  margin-left: 6px;
  background: #f1f5f9;
  color: #64748b;
  transition: all 0.2s ease;
}
.status-tab-btn:hover .status-count-badge {
  background: #cbd5e1;
  color: #0f172a;
}
.status-tab-btn.active .status-count-badge {
  background: #d01414;
  color: #ffffff;
}
.table-main {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  font-weight: 500;
  table-layout: fixed;
}
.table-main th,
.table-main td {
  vertical-align: middle;
  text-align: center;
  padding: 12px 10px;
  white-space: nowrap;
}
.table-main th {
  background: #e9f2fb;
  color: #333;
  font-weight: 700;
  font-size: 14px;
  border-bottom: none;
}
.table-main td {
  border-bottom: 1px solid #f0f0f0;
  font-size: 15px;
}
.table-main th:nth-child(1),
.table-main td:nth-child(1) {
  width: 50px;
  text-align: center;
}
.table-main th:nth-child(2),
.table-main td:nth-child(2) {
  width: 140px;
  text-align: left;
}
.table-main th:nth-child(3),
.table-main td:nth-child(3) {
  width: 140px;
  text-align: left;
}
.table-main th:nth-child(4),
.table-main td:nth-child(4) {
  width: 160px;
  text-align: left;
}
.table-main th:nth-child(5),
.table-main td:nth-child(5) {
  width: 130px;
  text-align: left;
}
.table-main th:nth-child(6),
.table-main td:nth-child(6) {
  width: 120px;
  text-align: right;
}
.table-main th:nth-child(7),
.table-main td:nth-child(7) {
  width: 120px;
  text-align: right;
}
.table-main th:nth-child(8),
.table-main td:nth-child(8) {
  width: 165px;
  text-align: left;
}
.table-main th:nth-child(9),
.table-main td:nth-child(9) {
  width: 145px;
  text-align: center;
}
.table-main th:nth-child(10),
.table-main td:nth-child(10) {
  width: 100px;
  text-align: center;
}
.ma-hoa-don {
  font-weight: 600;
  color: #d73737;
  font-size: 13.5px;
}
.status-text {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 130px;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}
.status-treo {
  background: rgba(139, 92, 246, 0.12);
  color: #8b5cf6;
}
.status-wait-payment {
  background: rgba(234, 88, 12, 0.12);
  color: #ea580c;
}
.status-expired {
  background: rgba(239, 68, 68, 0.12);
  color: #ef4444;
}
.status-other {
  background: rgba(107, 114, 128, 0.12);
  color: #6b7280;
}
.btn-action-icon {
  width: 30px;
  height: 30px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 50px;
  background: #f2f5f7;
  cursor: pointer;
  transition: all 0.2s ease;
  margin: 0 3px;
}
.btn-action-icon:hover {
  transform: translateY(-1px);
}
.btn-detail:hover {
  background: #eff6ff;
}
.btn-payment {
  color: #059669;
}
.btn-payment:hover {
  background: #d1fae5;
  color: #059669;
}
.img-icon-action {
  width: 14px !important;
  height: 14px !important;
  object-fit: contain;
}
.text-bold {
  font-weight: 600;
}
.text-center {
  text-align: center;
}
.empty-table {
  text-align: center;
  color: #6b7280;
  padding: 20px;
  font-style: italic;
}
.pagination-footer {
  display: grid;
  grid-template-columns: 220px 1fr 220px;
  align-items: center;
  padding: 12px 4px;
  margin-top: 8px;
}
.pagination-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #555;
}
.pagination-right {
  text-align: right;
  font-size: 13px;
  color: #6b7280;
}
.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 12px;
}
.pagination-premium {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(12px);
  padding: 6px;
  border-radius: 10px;
  border: 1px solid rgba(241, 245, 249, 0.8);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02);
  list-style: none;
  margin: 0;
}
.nav-action-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid transparent;
  border-radius: 12px;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}
.nav-action-btn:hover:not(:disabled) {
  background: #f8fafc;
  color: #0f172a;
  border-color: #e2e8f0;
}
.nav-action-btn:active:not(:disabled) {
  transform: scale(0.96);
  background: #f1f5f9;
}
.nav-action-btn:disabled {
  opacity: 0.25;
  cursor: not-allowed;
  color: #94a3b8;
}
.page-indicator-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  height: 36px;
  padding: 0 16px;
  background: #f3efef;
  border-radius: 7px;
  user-select: none;
}
.current-number {
  font-weight: 600;
  font-size: 0.875rem;
  color: #d73737;
}
.divider {
  font-size: 0.75rem;
  color: #d73737;
  margin: 0 2px;
}
.total-number {
  font-weight: 500;
  font-size: 0.875rem;
  color: #94a3b8;
}
.table-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  background: #eff6ff;
  color: #fd0606;
  border-radius: 4px;
  font-size: 12.5px;
  font-weight: 500;
}
.canh-bao-treo-lau {
  margin-left: 4px;
  font-size: 13px;
}
</style>
