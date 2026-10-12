<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { lichLamViecApi } from "@/api/admin/lichlamviec/lichLamViecApi";
import { caLamApi } from "@/api/admin/lichlamviec/caLamApi";

import iconBack from "@/assets/icons/prev.png";
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";
import iconCalendar from "@/assets/icons/calendar.png";

const router = useRouter();

// --- Logic ---
const allSchedules = ref([]);
const timeSlots = ref([]);
const searchNhanVien = ref("");
const filterTuNgay = ref("");
const filterDenNgay = ref("");
const filterCaLam = ref("");
const filterTrangThai = ref("");

const appliedSearchNhanVien = ref("");
const appliedFilterTuNgay = ref("");
const appliedFilterDenNgay = ref("");
const appliedFilterCaLam = ref("");
const appliedFilterTrangThai = ref("");

const currentPageSchedules = ref(1);
const itemsPerPageSchedules = ref(10);
const isLoading = ref(false);

const danhSachTrangThaiFilterOptions = [
  { label: "Tất cả trạng thái", value: "" },
  { label: "Đã hủy", value: 0 },
  { label: "Chưa tới", value: 1 },
  { label: "Sắp tới", value: 2 },
  { label: "Trong ca", value: 3 },
  { label: "Đã xong", value: 4 },
];

const danhSachCaLamFilterOptions = computed(() => {
  return [
    { label: "Tất cả ca làm", value: "" },
    ...timeSlots.value.map((s) => ({ label: s.name, value: s.name })),
  ];
});

const getTrangThaiText = (status) => {
  if (status === 0) return "Đã hủy";
  if (status === 1) return "Chưa tới";
  if (status === 2) return "Sắp tới";
  if (status === 3) return "Trong ca";
  if (status === 4) return "Đã xong";
  return "Chưa xác định";
};

const fetchCaLam = async () => {
  try {
    const caRes = await caLamApi.layDanhSach();
    if (caRes && caRes.data) {
      timeSlots.value = caRes.data.map((ca) => ({
        id: ca.id,
        name: ca.tenCaLam,
      }));
    }
  } catch (error) {
    console.error("Lỗi lấy ca làm:", error);
  }
};

const fetchSchedules = async () => {
  isLoading.value = true;
  try {
    const params = {};
    if (appliedFilterTuNgay.value) params.tuNgay = appliedFilterTuNgay.value;
    if (appliedFilterDenNgay.value) params.denNgay = appliedFilterDenNgay.value;
    if (appliedSearchNhanVien.value)
      params.nhanVien = appliedSearchNhanVien.value;
    if (appliedFilterCaLam.value) params.caLam = appliedFilterCaLam.value;
    if (appliedFilterTrangThai.value !== "")
      params.trangThai = parseInt(appliedFilterTrangThai.value);

    const lichRes = await lichLamViecApi.layDanhSach(params);
    if (lichRes && lichRes.data) {
      allSchedules.value = lichRes.data
        .map((lich) => {
          let dateFormatted = "";
          let y, m, d;
          if (lich.ngay) {
            if (Array.isArray(lich.ngay)) {
              y = lich.ngay[0];
              m = lich.ngay[1];
              d = lich.ngay[2];
              dateFormatted = `${d < 10 ? "0" + d : d}/${
                m < 10 ? "0" + m : m
              }/${y}`;
            } else {
              const parts = lich.ngay.split("-");
              if (parts.length >= 3) {
                y = parseInt(parts[0]);
                m = parseInt(parts[1]);
                d = parseInt(parts[2]);
                dateFormatted = `${d < 10 ? "0" + d : d}/${
                  m < 10 ? "0" + m : m
                }/${y}`;
              }
            }
          }
          return {
            id: lich.id,
            date: dateFormatted,
            empName: lich.tenNhanVien || "Không tên",
            trangThai: lich.trangThai,
            gioBatDau: lich.gioBatDau ? lich.gioBatDau.substring(0, 5) : "",
            gioKetThuc: lich.gioKetThuc ? lich.gioKetThuc.substring(0, 5) : "",
            tenCaLam: lich.tenCaLam,
            ghiChu: lich.ghiChu,
            _rawDate: new Date(y, m - 1, d),
          };
        })
        .sort((a, b) => b._rawDate - a._rawDate);
    }
  } catch (error) {
    console.error("Lỗi lấy lịch làm việc:", error);
  } finally {
    isLoading.value = false;
  }
};

const applyFilter = async () => {
  appliedSearchNhanVien.value = searchNhanVien.value;
  appliedFilterTuNgay.value = filterTuNgay.value;
  appliedFilterDenNgay.value = filterDenNgay.value;
  appliedFilterCaLam.value = filterCaLam.value;
  appliedFilterTrangThai.value = filterTrangThai.value;
  currentPageSchedules.value = 1;
  await fetchSchedules();
};

const resetFilter = async () => {
  searchNhanVien.value = "";
  filterTuNgay.value = "";
  filterDenNgay.value = "";
  filterCaLam.value = "";
  filterTrangThai.value = "";
  appliedSearchNhanVien.value = "";
  appliedFilterTuNgay.value = "";
  appliedFilterDenNgay.value = "";
  appliedFilterCaLam.value = "";
  appliedFilterTrangThai.value = "";
  currentPageSchedules.value = 1;
  await fetchSchedules();
};

const totalPagesSchedules = computed(
  () => Math.ceil(allSchedules.value.length / itemsPerPageSchedules.value) || 1,
);

const paginatedSchedules = computed(() => {
  const start = (currentPageSchedules.value - 1) * itemsPerPageSchedules.value;
  return allSchedules.value.slice(start, start + itemsPerPageSchedules.value);
});

const goFirst = () => {
  currentPageSchedules.value = 1;
};
const goPrev = () => {
  if (currentPageSchedules.value > 1) currentPageSchedules.value--;
};
const goNext = () => {
  if (currentPageSchedules.value < totalPagesSchedules.value)
    currentPageSchedules.value++;
};
const goLast = () => {
  currentPageSchedules.value = totalPagesSchedules.value;
};

const goBack = () => {
  router.push("/admin/lich-lam-viec");
};

onMounted(() => {
  fetchCaLam();
  fetchSchedules();
});
</script>

<template>
  <div class="h-100 d-flex flex-column fade-in gap-3">
    <!-- HEADER -->
    <div class="box m-0 shadow-sm">
      <div class="box-header d-flex justify-content-between align-items-center">
        <div>
          <h2 class="main-title">
            <span class="icon-wrapper">
              <img :src="iconCalendar" alt="Lịch làm việc" />
            </span>
            Chi Tiết Lịch Làm Việc
          </h2>
          <p class="sub-title">
            Xem chi tiết toàn bộ lịch và trạng thái ca làm
          </p>
        </div>
        <button class="icon-btn" @click="goBack" data-tooltip="Quay lại">
          <img :src="iconBack" />
        </button>
      </div>
    </div>

    <!-- Filter Section -->
    <div
      class="filter-box m-0 shadow-sm border-0 d-flex flex-wrap align-items-end gap-3 p-3 bg-white rounded"
    >
      <div class="d-flex flex-column" style="flex: 2; min-width: 180px;">
        <label class="fw-bold mb-1 text-muted small">Nhân viên</label>
        <input
          class="custom-input"
          placeholder="Tìm tên nhân viên..."
          v-model="searchNhanVien"
        />
      </div>
      <div class="d-flex flex-column" style="flex: 1.5; min-width: 140px;">
        <label class="fw-bold mb-1 text-muted small">Từ ngày</label>
        <input type="date" class="custom-input" v-model="filterTuNgay" />
      </div>
      <div class="d-flex flex-column" style="flex: 1.5; min-width: 140px;">
        <label class="fw-bold mb-1 text-muted small">Đến ngày</label>
        <input type="date" class="custom-input" v-model="filterDenNgay" />
      </div>
      <div class="d-flex flex-column" style="flex: 1.5; min-width: 140px;">
        <label class="fw-bold mb-1 text-muted small">Ca làm</label>
        <v-select
          class="modern-select"
          v-model="filterCaLam"
          :options="danhSachCaLamFilterOptions"
          label="label"
          :reduce="(opt) => opt.value"
          placeholder="Tất cả ca làm"
          :clearable="false"
        >
          <template #no-options>Không tìm thấy</template>
        </v-select>
      </div>
      <div class="d-flex flex-column" style="flex: 1.5; min-width: 140px;">
        <label class="fw-bold mb-1 text-muted small">Trạng thái</label>
        <v-select
          class="modern-select"
          v-model="filterTrangThai"
          :options="danhSachTrangThaiFilterOptions"
          label="label"
          :reduce="(opt) => opt.value"
          placeholder="Tất cả trạng thái"
          :clearable="false"
        >
          <template #no-options>Không tìm thấy</template>
        </v-select>
      </div>
      <div class="d-flex gap-2" style="flex: 0 0 auto;">
        <button class="btn-primary" style="height: 42px" @click="applyFilter">
          <i class="ri-filter-3-line"></i> Lọc
        </button>
        <button class="btn-cancel" style="height: 42px; background: white" @click="resetFilter">
          <i class="ri-refresh-line"></i> Reset
        </button>
      </div>
    </div>

    <!-- Table Section -->
    <div
      class="bg-white rounded shadow-sm border overflow-hidden flex-grow-1 d-flex flex-column"
    >
      <div
        v-if="isLoading"
        class="d-flex justify-content-center align-items-center flex-grow-1"
      >
        <div class="spinner-border text-danger" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
      </div>
      <div v-else class="table-responsive flex-grow-1 overflow-auto">
        <table class="table-main">
          <thead>
            <tr>
              <th style="text-align: left">Nhân viên</th>
              <th>Ngày</th>
              <th>Ca làm</th>
              <th>Giờ làm</th>
              <th>Trạng thái</th>
              <th>Ghi chú</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="sched in paginatedSchedules" :key="sched.id">
              <td style="text-align: left">
                <div class="d-flex align-items-center gap-2">
                  <div
                    class="avatar bg-danger text-white rounded-circle d-flex align-items-center justify-content-center fw-bold shadow-sm"
                    style="width: 32px; height: 32px"
                  >
                    {{ sched.empName.charAt(0) }}
                  </div>
                  <span class="fw-bold">{{ sched.empName }}</span>
                </div>
              </td>
              <td class="fw-semibold text-dark">{{ sched.date }}</td>
              <td>{{ sched.tenCaLam || "Không rõ" }}</td>
              <td>
                <span class="badge bg-light border text-dark px-2 py-1">
                  <i class="ri-time-line text-secondary me-1"></i>
                  {{ sched.gioBatDau || "--:--" }} -
                  {{ sched.gioKetThuc || "--:--" }}
                </span>
              </td>
              <td>
                <span
                  class="status-text"
                  :class="{
                    'status-cancelled': sched.trangThai === 0,
                    'status-created': sched.trangThai === 1,
                    'status-waiting': sched.trangThai === 2,
                    'status-serving': sched.trangThai === 3,
                    'status-completed': sched.trangThai === 4,
                  }"
                >
                  {{ getTrangThaiText(sched.trangThai) }}
                </span>
              </td>
              <td class="text-muted small fst-italic">
                {{ sched.ghiChu || "Không có" }}
              </td>
            </tr>
            <tr v-if="paginatedSchedules.length === 0">
              <td colspan="6" class="text-center py-5 text-muted">
                <i class="ri-inbox-line fs-1 d-block mb-2"></i>
                Không có dữ liệu lịch làm việc nào
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div
        v-if="!isLoading"
        class="d-flex justify-content-between align-items-center p-3 border-top bg-light"
      >
        <div class="d-flex align-items-center gap-2">
          <span>Hiển thị</span>
          <select
            class="form-select form-select-sm w-auto"
            v-model="itemsPerPageSchedules"
            @change="currentPageSchedules = 1"
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
                :disabled="currentPageSchedules === 1"
                @click.prevent="goFirst"
                aria-label="First page"
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
                :disabled="currentPageSchedules === 1"
                @click.prevent="goPrev"
                aria-label="Previous page"
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
                <span class="current-number">{{ currentPageSchedules }}</span>
                <span class="divider">/</span>
                <span class="total-number">{{ totalPagesSchedules }}</span>
              </div>
            </li>
            <li>
              <button
                class="nav-action-btn"
                :disabled="currentPageSchedules === totalPagesSchedules"
                @click.prevent="goNext"
                aria-label="Next page"
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
                :disabled="currentPageSchedules === totalPagesSchedules"
                @click.prevent="goLast"
                aria-label="Last page"
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

        <div class="text-muted small">
          Hiển thị
          {{
            paginatedSchedules.length > 0
              ? (currentPageSchedules - 1) * itemsPerPageSchedules + 1
              : 0
          }}
          -
          {{
            Math.min(
              currentPageSchedules * itemsPerPageSchedules,
              allSchedules.length,
            )
          }}
          trong số {{ allSchedules.length }} bản ghi
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.box {
  background: #fff;
  border-radius: 8px;
  padding: 0;
  border: 1px solid #eee;
  overflow: hidden;
}

.box-header {
  padding: 20px 24px;
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

.icon-wrapper img {
  width: 32px;
  height: 32px;
  object-fit: contain;
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
  color: #666;
  margin-top: 4px;
  font-size: 16px;
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
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 10px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background-color: transparent;
}

.btn {
  border-radius: 7px !important;
}

/* nút icon giống bên Set Lẩu */
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
  position: relative;
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

/* tooltip */
.icon-btn::after {
  content: attr(data-tooltip);
  position: absolute;
  bottom: -35px;
  left: 50%;
  transform: translateX(-50%);
  white-space: nowrap;
  background: #333;
  color: #fff;
  padding: 5px 10px;
  border-radius: 6px;
  font-size: 12px;
  opacity: 0;
  transition: 0.3s;
  pointer-events: none;
}

.icon-btn:hover::after {
  opacity: 1;
}

/* PAGINATION PREMIUM */
.pagination-container {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 10px 0;
}
.pagination-premium {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #ffffff;
  border-radius: 12px;
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
  border-radius: 7px;
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

/* V-SELECT MODERN */
:deep(.v-select),
:deep(.modern-select) {
  background: #fff;
  width: 100%;
  --vs-dropdown-option--active-bg: #f3f4f6;
  --vs-dropdown-option--active-color: #eb6464;
  --vs-border-color: #e5e7eb;
  --vs-border-radius: 10px;
  --vs-dropdown-box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1);
}
:deep(.modern-select .vs__dropdown-toggle) {
  border: 1px solid #e5e7eb !important;
  border-radius: 10px !important;
  padding: 0 !important;
  height: 42px;
  display: flex;
  align-items: center;
  transition: all 0.2s ease;
  box-shadow: none !important;
}
:deep(.modern-select .vs__search),
:deep(.modern-select .vs__search:focus) {
  border: none !important;
  margin: 0 !important;
  padding: 0 12px !important;
  background: transparent !important;
  box-shadow: none !important;
}
:deep(.modern-select .vs__selected) {
  position: absolute;
  margin: 0 !important;
  padding-left: 12px !important;
  color: #374151;
  border: none !important;
}
:deep(.modern-select .vs__search::placeholder) {
  color: #9ca3af !important;
}
:deep(.modern-select .vs__dropdown-toggle:hover) {
  border-color: #c8d0dc !important;
}
:deep(.modern-select.vs--open .vs__dropdown-toggle) {
  border-color: #dc3545 !important;
  box-shadow: 0 0 0 4px rgba(220, 53, 69, 0.12) !important;
}
:deep(.vs__dropdown-menu) {
  border: 1px solid #e5e7eb !important;
  border-radius: 10px !important;
  margin-top: 8px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1) !important;
  padding: 6px !important;
  z-index: 10000 !important;
}
:deep(.vs__dropdown-option) {
  border-radius: 8px !important;
  padding: 8px 12px !important;
  margin-bottom: 2px !important;
  color: #4b5563;
}
:deep(.vs__dropdown-option--highlight) {
  background: #f3f4f6 !important;
  color: #eb6464 !important;
}
:deep(.vs__dropdown-option--selected) {
  background: #f3f5f9 !important;
  color: #fc0101 !important;
}
:deep(.modern-select .vs__actions) {
  padding-right: 10px !important;
}
:deep(.modern-select .vs__open-indicator) {
  fill: #9ca3af !important;
  transform: scale(0.7);
}
:deep(.modern-select .vs__clear) {
  fill: #9ca3af !important;
}

.custom-input {
  height: 42px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 0 12px;
  font-size: 14px;
  outline: none;
  transition: all 0.2s ease;
  width: 100%;
  font-family: inherit;
  background-color: #fff;
  box-shadow: none;
}
.custom-input:hover {
  border-color: #c8d0dc;
}
.custom-input:focus {
  border-color: #dc3545;
  box-shadow: 0 0 0 4px rgba(220, 53, 69, 0.12);
}

/* ===== BUTTON MODERN (Cancel + Primary) ===== */
.btn-cancel {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: transparent;
  color: #64748b;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}
.btn-cancel:hover {
  background: #f8fafc;
  color: #334155;
  border-color: #94a3b8;
  transform: translateY(-2px);
}

.btn-primary {
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 24px;
  background: #d73737;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(215, 55, 55, 0.2);
}
.btn-primary:hover {
  background: #b92b2b;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(215, 55, 55, 0.3);
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

/* TABLE MAIN */
.table-main {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  background: white;
  overflow: hidden;
}
.table-main th,
.table-main td {
  vertical-align: middle;
  text-align: center;
  padding: 16px 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.table-main th {
  background: #f8fafc;
  color: #1e293b;
  font-weight: 700;
  font-size: 14px;
  border-bottom: 2px solid #e2e8f0;
}
.table-main td {
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}
.table-main th:nth-child(1),
.table-main td:nth-child(1) {
  width: 25%;
  text-align: left;
}
.table-main th:nth-child(2),
.table-main td:nth-child(2) {
  width: 15%;
  text-align: center;
}
.table-main th:nth-child(3),
.table-main td:nth-child(3) {
  width: 15%;
}
.table-main th:nth-child(4),
.table-main td:nth-child(4) {
  width: 15%;
}
.table-main th:nth-child(5),
.table-main td:nth-child(5) {
  width: 15%;
  text-align: center !important;
}
.table-main th:nth-child(6),
.table-main td:nth-child(6) {
  width: 15%;
  text-align: center !important;
}
</style>
