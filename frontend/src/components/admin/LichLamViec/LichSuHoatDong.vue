<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { giaoCaApi } from "@/api/admin/lichlamviec/giaoCaApi";
import iconDanhSach from "@/assets/icons/danh-sach.png";
import iconCalendar from "@/assets/icons/calendar.png";

const searchQuery = ref("");
const dateFrom = ref("");
const dateTo = ref("");

const histories = ref([]);

const getTrangThaiGiaoCaInfo = (item) => {
  const value = Number(item?.trangThai);
  const trangThaiGiaoCa = String(item?.trangThaiGiaoCa || "");

  if (value === 2 || value === 3 || trangThaiGiaoCa === "DA_KET_THUC" || item?.thoiGianKetThucCa) {
    return {
      text: "Đã kết ca",
      className: "status-completed",
    };
  }

  if (value === 0) {
    return {
      text: "Chưa xác nhận vào ca",
      className: "status-waiting",
    };
  }

  if (value === 1) {
    return {
      text: "Đã vào ca",
      className: "status-serving",
    };
  }

  return {
    text: "Không xác định",
    className: "status-created",
  };
};

const fetchData = async () => {
  try {
    const params = {};
    if (searchQuery.value) params.searchQuery = searchQuery.value;
    if (dateFrom.value) params.tuNgay = dateFrom.value;
    if (dateTo.value) params.denNgay = dateTo.value;

    const response = await giaoCaApi.timKiemLichSuHoatDong(params);
    histories.value = response.data.map((item, index) => {
      const parseDateTime = (dt) => {
        if (!dt) return null;
        if (Array.isArray(dt)) {
          return new Date(
            dt[0],
            dt[1] - 1,
            dt[2],
            dt[3] || 0,
            dt[4] || 0,
            dt[5] || 0,
          );
        }
        return new Date(dt);
      };

      const dateNhan = parseDateTime(item.thoiGianNhanCa);
      const dateKetThuc = parseDateTime(item.thoiGianKetThucCa);

      const formatTime = (d) =>
        d
          ? `${d.getHours().toString().padStart(2, "0")}:${d
              .getMinutes()
              .toString()
              .padStart(2, "0")} ${d.getDate().toString().padStart(2, "0")}/${(
              d.getMonth() + 1
            )
              .toString()
              .padStart(2, "0")}/${d.getFullYear()}`
          : "--:-- --/--/----";

      let formattedShiftTime = "";
      let shiftDateStr = "";
      if (item.ngay) {
        if (Array.isArray(item.ngay)) {
          formattedShiftTime = `${item.ngay[2]
            .toString()
            .padStart(2, "0")}/${item.ngay[1].toString().padStart(2, "0")}/${
            item.ngay[0]
          }`;
          shiftDateStr = `${item.ngay[0]}-${item.ngay[1]
            .toString()
            .padStart(2, "0")}-${item.ngay[2].toString().padStart(2, "0")}`;
        } else {
          formattedShiftTime = item.ngay.split("-").reverse().join("/");
          shiftDateStr = item.ngay;
        }
      }

      const statusInfo = getTrangThaiGiaoCaInfo(item);

      return {
        id: item.id,
        shiftDateStr,
        avatarText: item.tenNhanVien
          ? item.tenNhanVien.charAt(0).toUpperCase()
          : "NV",
        employeeName: item.tenNhanVien || "Không xác định",
        employeeCode: item.maNhanVien || "NV---",
        shiftName: item.tenCaLam || "Không xác định",
        shiftTime: formattedShiftTime,
        checkInTime: formatTime(dateNhan),
        checkInLateMinutes: 0,
        checkOutTime: formatTime(dateKetThuc),
        checkOutEarlyMinutes: 0,
        totalRevenue: new Intl.NumberFormat("vi-VN").format(
          item.tienBanGiaoDuKien || 0,
        ),
        cashRevenue: new Intl.NumberFormat("vi-VN").format(
          item.tongTienMat || 0,
        ),
        transferRevenue: new Intl.NumberFormat("vi-VN").format(
          item.tongChuyenKhoan || 0,
        ),
        status: statusInfo.text,
        statusClass: statusInfo.className,
        note: item.ghiChu || "Không có",
      };
    });
  } catch (error) {
    console.error("Lỗi khi tải lịch sử giao ca:", error);
  }
};

/* ===== PHÂN TRANG VÀ TÌM KIẾM ===== */
const currentPage = ref(1);
const pageSize = ref(5);

const filteredHistories = computed(() => {
  return histories.value;
});

const totalItems = computed(() => filteredHistories.value.length);
const totalPages = computed(
  () => Math.ceil(totalItems.value / pageSize.value) || 1,
);

const danhSachHienThi = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return filteredHistories.value.slice(start, start + pageSize.value);
});

watch([searchQuery, dateFrom, dateTo], () => {
  currentPage.value = 1;
  fetchData();
});

const goFirst = () => (currentPage.value = 1);
const goPrev = () => {
  if (currentPage.value > 1) currentPage.value--;
};
const goNext = () => {
  if (currentPage.value < totalPages.value) currentPage.value++;
};
const goLast = () => (currentPage.value = totalPages.value);

onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="lich-su-hoat-dong fade-in d-flex flex-column h-100">
    <!-- TOP HEADER -->
    <div class="top-box mb-3">
      <div class="top-box-header">
        <h2 class="main-title">
          <span class="icon-wrapper">
            <img :src="iconCalendar" alt="Lịch sử hoạt động" />
          </span>
          Lịch Sử Hoạt Động
        </h2>
        <p class="sub-title">
          Xem lại lịch sử bàn giao ca làm việc của nhân viên
        </p>
      </div>
    </div>

    <div class="filter-box">
      <div class="filter-item">
        <label>Tìm kiếm chung</label>
        <input
          type="text"
          class="custom-input"
          placeholder="Nhập tên nhân viên, tên ca..."
          v-model="searchQuery"
        />
      </div>
      <div class="filter-item">
        <label>Từ ngày</label>
        <div class="input-with-icon">
          <input type="date" class="custom-input" v-model="dateFrom" />
        </div>
      </div>
      <div class="filter-item">
        <label>Đến ngày</label>
        <div class="input-with-icon">
          <input type="date" class="custom-input" v-model="dateTo" />
        </div>
      </div>
    </div>

    <!-- Table Section -->
    <div class="box">
      <div class="box-header box-header-flex">
        <h3 class="table-title">
          <span class="icon-wrapper-small">
            <img :src="iconDanhSach" class="icon-title-small" />
          </span>
          Danh sách hoạt động
        </h3>
        <div class="header-actions">
          <button class="refresh-btn" @click="fetchData">
            <img src="@/assets/icons/refresh.png" alt="" />
            <span>Làm mới</span>
          </button>
        </div>
      </div>
      <div class="table-content table-responsive">
        <table class="table-main">
          <thead>
            <tr>
              <th>STT</th>
              <th style="text-align: left">Nhân viên / Ca làm việc</th>
              <th>Vào ca</th>
              <th>Ra ca</th>
              <th>Tiền mặt thu</th>
              <th>Chuyển khoản</th>
              <th>Tiền bàn giao</th>
              <th>Trạng thái</th>
              <th>Ghi chú</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in danhSachHienThi" :key="item.id">
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
              <td style="text-align: left">
                <div class="d-flex align-items-center">
                  <div class="avatar-circle fw-bold me-3">
                    {{ item.avatarText }}
                  </div>
                  <div>
                    <div class="fw-bold text-dark mb-1">
                      {{ item.employeeName }}
                      <span class="text-muted fw-normal"
                        >({{ item.employeeCode }})</span
                      >
                    </div>
                    <div class="text-muted fs-13">
                      {{ item.shiftName }} ({{ item.shiftTime }})
                    </div>
                  </div>
                </div>
              </td>
              <td>
                <div
                  class="d-flex align-items-center justify-content-center mb-1"
                >
                  <span class="fw-semibold text-dark fs-14">{{
                    item.checkInTime
                  }}</span>
                </div>
                <span
                  class="badge tag-late rounded-1 fw-normal"
                  v-if="item.checkInLateMinutes"
                >
                  Đi muộn {{ item.checkInLateMinutes }} phút
                </span>
              </td>
              <td>
                <div
                  class="d-flex align-items-center justify-content-center mb-1"
                >
                  <span class="fw-semibold text-dark fs-14">{{
                    item.checkOutTime
                  }}</span>
                </div>
                <span
                  class="badge tag-early rounded-1 fw-normal"
                  v-if="item.checkOutEarlyMinutes"
                >
                  Ra sớm {{ item.checkOutEarlyMinutes }} phút
                </span>
              </td>
              <td>
                <span class="fw-bold text-success fs-14">{{ item.cashRevenue }} đ</span>
              </td>
              <td>
                <span class="fw-bold text-info fs-14">{{ item.transferRevenue }} đ</span>
              </td>
              <td>
                <span class="fw-bold text-danger fs-14">{{ item.totalRevenue }} đ</span>
              </td>
              <td>
                <span
                  class="status-text"
                  :class="item.statusClass"
                >
                  {{ item.status }}
                </span>
              </td>
              <td
                class="text-muted fst-italic fs-14"
                style="white-space: normal"
              >
                {{ item.note }}
              </td>
            </tr>
            <tr v-if="danhSachHienThi.length === 0">
              <td
                colspan="9"
                class="empty-table text-center text-muted fst-italic py-4"
              >
                🔍 Không tìm thấy hoạt động nào phù hợp
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- PHÂN TRANG -->
      <div class="d-flex justify-content-between align-items-center mt-3">
        <div class="d-flex align-items-center gap-2">
          <span class="text-nowrap">Hiển thị</span>
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
                @click.prevent="goFirst"
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
                  <path d="m11 17-5-5 5-5" />
                  <path d="m18 17-5-5 5-5" />
                </svg>
              </button>
            </li>
            <li>
              <button
                class="nav-action-btn"
                :disabled="currentPage === 1"
                @click.prevent="goPrev"
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
                @click.prevent="goNext"
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
                  <path d="m9 18 6-6-6-6" />
                </svg>
              </button>
            </li>
            <li>
              <button
                class="nav-action-btn"
                :disabled="currentPage === totalPages"
                @click.prevent="goLast"
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

        <div class="text-muted">
          Hiển thị {{ danhSachHienThi.length }} / {{ totalItems }} bản ghi
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url("https://cdn.jsdelivr.net/npm/remixicon@4.2.0/fonts/remixicon.css");

.lich-su-hoat-dong {
  font-family: var(--font-family, "Inter", sans-serif);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.fade-in {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* FILTER BOX */
.filter-box {
  background: rgb(255, 255, 255);
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 24px;
  display: flex;
  align-items: flex-end;
  gap: 12px;
}
.filter-item {
  flex: 1;
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
  width: 100%;
  height: 42px;
  box-sizing: border-box;
  padding: 0 12px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  font-size: 14px;
  outline: none;
  transition: all 0.22s ease;
}
.custom-input:hover {
  border-color: #c8d0dc;
}
.custom-input:focus {
  border-color: var(--color-primary, #e65a5a);
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12);
}
.input-with-icon {
  position: relative;
}

/* TOP BOX HEADER */
.top-box {
  background: #fff;
  border-radius: 8px;
  padding: 0;
  margin-bottom: 20px;
  border: 1px solid #eee;
  overflow: hidden;
}
.top-box-header {
  padding: 20px 24px 20px 24px;
}
.main-title {
  display: flex;
  align-items: center;
  font-size: 30px;
  font-weight: 600;
  gap: 12px;
  color: #1a1a1a;
  margin: 0 0 4px 0;
}
.icon-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background-color: #fce4e4;
  border-radius: 12px;
}
.icon-wrapper img {
  width: 24px;
  height: 24px;
}
.sub-title {
  font-size: 14px;
  color: #666;
}

/* BOX & HEADER */
.box {
  background: #fff;
  border-radius: var(--radius-md, 12px);
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
.header-actions {
  display: flex;
  gap: 12px;
}
.table-title {
  display: flex;
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
  display: block;
}
.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 14px;
  border: none;
  border-radius: 7px;
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

/* TABLE MAIN */
.table-main {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  font-weight: 600;
}
.table-main th,
.table-main td {
  vertical-align: middle;
  text-align: center;
  padding: 12px 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.table-main th {
  background: #e9f2fb;
  font-size: 14px;
  color: #333;
  font-weight: 700;
  font-size: 15px;
}
.table-main td {
  border-bottom: 1px solid #f0f0f0;
  font-size: 13px;
}
.table-main th:nth-child(1),
.table-main td:nth-child(1) {
  width: 50px;
}
.table-main th:nth-child(2),
.table-main td:nth-child(2) {
  width: 220px;
  text-align: left;
}
.table-main th:nth-child(3),
.table-main td:nth-child(3) {
  width: 140px;
}
.table-main th:nth-child(4),
.table-main td:nth-child(4) {
  width: 140px;
}
.table-main th:nth-child(5),
.table-main td:nth-child(5) {
  width: 130px;
}
.table-main th:nth-child(6),
.table-main td:nth-child(6) {
  width: 130px;
}
.table-main th:nth-child(7),
.table-main td:nth-child(7) {
  width: 130px;
}
.table-main th:nth-child(8),
.table-main td:nth-child(8) {
  width: 120px;
}
.table-main th:nth-child(9),
.table-main td:nth-child(9) {
  width: 150px;
}

.fs-12 {
  font-size: 12px;
}
.fs-13 {
  font-size: 13px;
}
.fs-14 {
  font-size: 14px;
}

.avatar-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #e8f5e9;
  color: #2e7d32;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

/* Tags */
.tag-late {
  background-color: #ffebee;
  color: #d32f2f;
  padding: 4px 8px;
  font-size: 12px;
}

.tag-early {
  background-color: #fff3e0;
  color: #e65100;
  padding: 4px 8px;
  font-size: 12px;
}

/* TRẠNG THÁI STATUS (GIỐNG DONHANG.VUE) */
.status-text {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 100px;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}
.status-created {
  background: rgba(107, 114, 128, 0.12);
  color: #6b7280;
}
.status-waiting {
  background: rgba(251, 146, 60, 0.12);
  color: #fb923c;
}
.status-serving {
  background: rgba(139, 92, 246, 0.12);
  color: #8b5cf6;
}
.status-completed {
  background: rgba(5, 150, 105, 0.12);
  color: #059669;
}
.status-cancelled {
  background: rgba(239, 68, 68, 0.12);
  color: #ef4444;
}

/* PHÂN TRANG */
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
  -webkit-backdrop-filter: blur(12px);
  padding: 6px;
  border-radius: 10px;
  border: 1px solid rgba(241, 245, 249, 0.8);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02),
    0 10px 15px -3px rgba(15, 23, 42, 0.05);
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
  outline: none;
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

.nav-action-btn svg {
  transition: transform 0.2s ease;
}

.nav-action-btn:hover:not(:disabled) svg {
  transform: stroke-width(2.5);
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
  color: #ffffff;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  box-shadow: 0 4px 12px rgba(174, 193, 237, 0.15);
  user-select: none;
}

.current-number {
  font-weight: 600;
  font-size: 0.875rem;
  color: #f31e1e;
}

.divider {
  font-size: 0.75rem;
  color: #f31111;
  margin: 0 2px;
}

.total-number {
  font-weight: 500;
  font-size: 0.875rem;
  color: #94a3b8;
}

@media (max-width: 1024px) {
  .filter-box {
    flex-wrap: wrap !important;
  }

  .filter-item {
    flex: 1 1 calc(50% - 12px) !important;
    min-width: 140px !important;
  }

  .box-header-flex {
    flex-wrap: wrap !important;
    gap: 10px !important;
  }

  .header-actions {
    flex-wrap: wrap !important;
    gap: 8px !important;
  }
}
</style>
