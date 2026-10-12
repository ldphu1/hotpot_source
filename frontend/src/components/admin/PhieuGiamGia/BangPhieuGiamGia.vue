<script setup>
import { onMounted, ref, computed, watch } from "vue";
import { useRouter } from "vue-router";
import {
  deleteTrangThai,
  exportPhieuGiamGia,
  filterPhieuGiamGia,
  restorePhieuGiamGia,
} from "@/api/admin/phieugiamgia/phieuGiamGiaApi";
import { useUtils } from "@/utils/helper";

/* ICON */
import iconAdd from "@/assets/icons/add.png";
import iconExcel from "@/assets/icons/excel.png";
import iconSearch from "@/assets/icons/search.png";
import iconEdit from "@/assets/icons/edit.png";
import iconRefresh from "@/assets/icons/refresh.png";
import { TicketPercent } from "lucide-vue-next";
import { Ticket } from "lucide-vue-next";
import iconDanhSach from "@/assets/icons/danh-sach.png";
const { notify, nprogress } = useUtils();
const router = useRouter();
// SELECT 2
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";
/* DATA */
const danhSach = ref([]);

const currentPage = ref(1);
const pageSize = ref(5);
const totalItems = ref(0);

/* FILTER */
const keyword = ref("");
const phuongThucGiamGia = ref(null);
const kieuGiamGia = ref(null);
const trangThai = ref(null);

const isHetHan = (item) => {
  const now = new Date();
  return now > new Date(item.ngayKetThuc);
};

/* FETCH DATA */
const loadData = async () => {
  nprogress.start();

  try {
    const res = await filterPhieuGiamGia({
      keyword: keyword.value,
      phuongThucGiamGia: phuongThucGiamGia.value,
      kieuGiamGia: kieuGiamGia.value,
      trangThai: trangThai.value,
      page: currentPage.value - 1,
      size: pageSize.value,
    });

    danhSach.value = res.data.content;
    totalItems.value = res.data.totalElements;
  } catch (error) {
    notify.error("Không tải được dữ liệu");
  } finally {
    nprogress.done();
  }
};

onMounted(loadData);

/* SEARCH */
const searchData = async () => {
  currentPage.value = 1;
  await loadData();
};

const handleExportPhieuGiamGia = async () => {
  try {
    const res = await exportPhieuGiamGia({
      keyword: keyword.value || null,
      phuongThucGiamGia: phuongThucGiamGia.value,
      kieuGiamGia: kieuGiamGia.value,
      trangThai: trangThai.value,
    });

    const url = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute(
      "download",
      `phieu_giam_gia_${new Date().toISOString().slice(0, 10)}.xlsx`,
    );
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
    notify.success("Xuất excel thành công");
  } catch (e) {
    notify.error("Không thể xuất Excel");
    console.error("Lỗi export phiếu giảm giá:", e);
  }
};

const danhSachPhuongThucGiamGia = [
  { label: "Tất cả", value: null },
  { label: "Giảm %", value: 1 },
  { label: "Giảm tiền", value: 2 },
];

const danhSachKieuGiamGia = [
  { label: "Tất cả", value: null },
  { label: "Công khai", value: 1 },
  { label: "Cá nhân", value: 2 },
];

const danhSachTrangThai = [
  { label: "Tất cả", value: null },
  { label: "Chưa bắt đầu", value: 0 },
  { label: "Đang diễn ra", value: 1 },
  { label: "Đã kết thúc", value: 2 },
  { label: "Tạm ngừng", value: 3 },
];

/* RELOAD */
const reloadData = async () => {
  keyword.value = "";
  phuongThucGiamGia.value = null;
  kieuGiamGia.value = null;
  trangThai.value = null;

  currentPage.value = 1;

  await loadData();

  notify.success("Đã làm mới dữ liệu");
};

/* NAVIGATION */
const addPhieuGiamGia = () => {
  router.push("/admin/phieu-giam-gia/save");
};

/* TRẠNG THÁI */
const chuyenTrangThai = async (item) => {
  if (isHetHan(item)) {
    notify.error("Phiếu đã kết thúc không thể thay đổi trạng thái");
    return;
  }

  const isRestore = item.trangThaiXoa;

  const confirm = await notify.confirm(
    isRestore
      ? "Bạn có muốn khôi phục phiếu giảm giá này?"
      : "Bạn có muốn ngừng hoạt động phiếu giảm giá này?",
  );

  if (!confirm) return;

  nprogress.start();

  try {
    if (isRestore) {
      await restorePhieuGiamGia(item.id);
      notify.success("Đã khôi phục phiếu giảm giá");
    } else {
      await deleteTrangThai(item.id);
      notify.success("Đã ngừng hoạt động phiếu giảm giá");
    }

    await loadData();
  } catch (error) {
    notify.error("Không thể cập nhật trạng thái");
  } finally {
    nprogress.done();
  }
};

/* SỬA */
const suaPhieu = (item) => {
  if (item.trangThaiXoa) {
    notify.error("Phiếu đang ngừng hoạt động");
    return;
  }

  if (item.trangThai === 2) {
    // ← sửa
    notify.error("Phiếu đã hết hạn");
    return;
  }

  router.push(`/admin/phieu-giam-gia/sua/${item.id}`);
};

/* PAGINATION */
const totalPages = computed(() =>
  Math.max(1, Math.ceil(totalItems.value / pageSize.value)),
);

const danhSachHienThi = computed(() => danhSach.value);

const selectedIds = ref([]);
const isAllSelected = computed({
  get() {
    return (
      danhSachHienThi.value.length > 0 &&
      danhSachHienThi.value.every((item) => selectedIds.value.includes(item.id))
    );
  },
  set(val) {
    if (val) {
      danhSachHienThi.value.forEach((item) => {
        if (!selectedIds.value.includes(item.id)) {
          selectedIds.value.push(item.id);
        }
      });
    } else {
      const idsToRemove = danhSachHienThi.value.map((item) => item.id);
      selectedIds.value = selectedIds.value.filter(
        (id) => !idsToRemove.includes(id),
      );
    }
  },
});

const goFirst = () => {
  currentPage.value = 1;
  loadData();
};

const goPrev = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
    loadData();
  }
};

const goNext = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
    loadData();
  }
};

const goLast = () => {
  currentPage.value = totalPages.value;
  loadData();
};

watch(pageSize, () => {
  currentPage.value = 1;
  loadData();
});

/* HELPER */
const formatMoney = (v) => Number(v).toLocaleString("vi-VN");

const formatDate = (d) => new Date(d).toLocaleDateString("vi-VN");

const getTrangThaiClass = (status) => {
  switch (status) {
    case 0:
      return "status-upcoming";
    case 1:
      return "status-active";
    case 2:
      return "status-ended";
    case 3:
      return "status-paused";
    default:
      return "";
  }
};
const getTrangThaiText = (status) => {
  switch (status) {
    case 0:
      return "Chưa bắt đầu";
    case 1:
      return "Đang diễn ra";
    case 2:
      return "Đã kết thúc";
    case 3:
      return "Tạm ngừng";
    default:
      return "Không xác định";
  }
};
</script>

<template>
  <div class="pgg-page">
    <!-- HEADER -->
    <div class="box">
      <div class="box-header">
        <h2 class="main-title">
          <span class="icon-wrapper">
            <TicketPercent :size="26" stroke-width="2.2" />
          </span>
          Quản Lý Phiếu Giảm Giá
        </h2>
        <p>Quản lý danh sách phiếu giảm giá trong hệ thống</p>
      </div>
    </div>

    <!-- FILTER -->
    <div class="box">
      <div class="box-header">
        <h3>Bộ lọc tìm kiếm</h3>
      </div>

      <div class="filter-box">
        <div class="filter-item">
          <label>Tìm kiếm chung</label>
          <input
            v-model="keyword"
            placeholder="Tìm theo mã hoặc tên..."
            @input="searchData"
            class="custom-input"
          />
        </div>

        <div class="filter-item">
          <label>Kiểu giảm giá</label>
          <v-select
            v-model="phuongThucGiamGia"
            :options="danhSachPhuongThucGiamGia"
            label="label"
            :reduce="(item) => item.value"
            placeholder="Tất cả"
            :clearable="false"
            class="modern-select"
            @update:modelValue="searchData"
          >
            <template #no-options="{ search, searching }">
              <template v-if="searching">
                Không tìm thấy "<b>{{ search }}</b
                >"
              </template>
              <em v-else style="opacity: 0.5"> Nhập để tìm... </em>
            </template>
          </v-select>
        </div>

        <div class="filter-item">
          <label>Đối tượng</label>
          <v-select
            v-model="kieuGiamGia"
            :options="danhSachKieuGiamGia"
            label="label"
            :reduce="(item) => item.value"
            placeholder="Tất cả"
            :clearable="false"
            class="modern-select"
            @update:modelValue="searchData"
          >
            <template #no-options="{ search, searching }">
              <template v-if="searching">
                Không tìm thấy "<b>{{ search }}</b
                >"
              </template>
              <em v-else style="opacity: 0.5"> Nhập để tìm... </em>
            </template>
          </v-select>
        </div>

        <div class="filter-item">
          <label>Trạng thái hoạt động</label>
          <v-select
            v-model="trangThai"
            :options="danhSachTrangThai"
            label="label"
            :reduce="(item) => item.value"
            placeholder="Tất cả"
            :clearable="false"
            class="modern-select"
            @update:modelValue="searchData"
          >
            <template #no-options="{ search, searching }">
              <template v-if="searching">
                Không tìm thấy "<b>{{ search }}</b
                >"
              </template>
              <em v-else style="opacity: 0.5"> Nhập để tìm... </em>
            </template>
          </v-select>
        </div>
      </div>
    </div>

    <!-- TABLE -->
    <div class="box">
      <div class="box-header box-header-flex">
        <h3 class="table-title">
          <span class="icon-wrapper-small">
            <img :src="iconDanhSach" class="icon-title-small" />
          </span>
          Danh sách phiếu giảm giá
        </h3>

        <div class="header-actions">
          <button class="add-btn" @click="addPhieuGiamGia">
            <img :src="iconAdd" />
            <span>Thêm</span>
          </button>
          <!-- Excel -->
          <button class="excel-btn" @click="handleExportPhieuGiamGia()">
            <img :src="iconExcel" alt="" />
            <span>Xuất Excel</span>
          </button>

          <button class="refresh-btn" @click="reloadData">
            <img :src="iconRefresh" alt="" />
            <span>Làm mới</span>
          </button>
        </div>
      </div>

      <table class="pgg-table">
        <thead>
          <tr>
            <th>
              <input
                type="checkbox"
                class="custom-checkbox"
                v-model="isAllSelected"
              />
            </th>
            <th>STT</th>
            <th>Mã</th>
            <th>Tên</th>
            <th>Đối tượng</th>
            <th>Số lượng</th>
            <th>Chi tiết ưu đãi</th>
            <th>Thời gian áp dụng</th>
            <th>Trạng thái</th>
            <th>Thao tác</th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="(item, index) in danhSachHienThi" :key="item.id">
            <td>
              <input
                type="checkbox"
                class="custom-checkbox"
                :value="item.id"
                v-model="selectedIds"
              />
            </td>

            <td>
              {{ (currentPage - 1) * pageSize + index + 1 }}
            </td>

            <td class="fw">{{ item.maGiamGia }}</td>

            <td class="fw">{{ item.tenGiamGia }}</td>

            <td>
              <span
                class="pill"
                :class="item.kieuGiamGia === 1 ? 'congkhai' : 'canhan'"
              >
                {{ item.kieuGiamGia === 1 ? "Công khai" : "Cá nhân" }}
              </span>
            </td>

            <td>{{ item.soLuong }}</td>

            <td>
              <div class="discount">
                <span
                  class="discount-badge"
                  :class="item.phuongThucGiamGia === 1 ? 'percent' : 'money'"
                >
                  {{
                    item.phuongThucGiamGia === 1
                      ? `Giảm ${item.giaTriGiam}%`
                      : `Giảm ${formatMoney(item.giaTriGiam)} đ`
                  }}
                </span>

                <div class="sub">
                  Tối đa:
                  {{ formatMoney(item.giamToiDa || 0) }} đ
                </div>

                <div class="sub">
                  Đơn tối thiểu:
                  {{ formatMoney(item.dieuKienSuDung || 0) }} đ
                </div>
              </div>
            </td>

            <td class="time-cell">
              <div class="time-range">
                <div class="time-start">
                  Bắt đầu: {{ formatDate(item.ngayBatDau) }}
                </div>
                <div class="time-end">
                  Kết thúc: {{ formatDate(item.ngayKetThuc) }}
                </div>
              </div>
            </td>
            <td>
              <span
                class="status-text"
                :class="getTrangThaiClass(item.trangThai)"
              >
                {{ getTrangThaiText(item.trangThai) }}
              </span>
            </td>

            <td>
              <div class="actions">
                <img
                  :src="iconEdit"
                  class="icon-action"
                  v-tooltip="'Chỉnh sửa'"
                  :style="{
                    opacity:
                      item.trangThaiXoa || item.trangThai === 2 ? 0.4 : 1,
                    cursor:
                      item.trangThaiXoa || item.trangThai === 2
                        ? 'not-allowed'
                        : 'pointer',
                  }"
                  @click="suaPhieu(item)"
                />

                <label class="switch" v-tooltip="'Đổi trạng thái'">
                  <input
                    type="checkbox"
                    :checked="!item.trangThaiXoa"
                    :disabled="isHetHan(item)"
                    @change="chuyenTrangThai(item)"
                  />
                  <span class="slider"></span>
                </label>
              </div>
            </td>
          </tr>
          <tr v-if="danhSachHienThi.length === 0">
            <td colspan="10" class="empty-table">
              🔍 Không tìm thấy phiếu giảm giá phù hợp
            </td>
          </tr>
        </tbody>
      </table>

      <!-- PAGINATION -->
      <div class="d-flex justify-content-between align-items-center mt-3">
        <div class="d-flex align-items-center gap-2">
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
                :disabled="currentPage === 1"
                @click="goPrev"
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
                aria-label="Next page"
              >
                <!-- Icon một mũi tên phải -->
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
                @click="goLast"
                aria-label="Last page"
              >
                <!-- Icon hai mũi tên phải thanh mảnh -->
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
          Hiển thị {{ danhSachHienThi.length }} / {{ totalItems }} phiếu giảm
          giá
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.status-text {
  display: inline-flex;
  align-items: center;
  justify-content: center;

  min-width: 120px;

  padding: 6px;
  border-radius: 4px;

  font-size: 13px;
  font-weight: 500;

  white-space: nowrap;
}
.status-upcoming {
  background: #fef3c7;
  color: #d97706;
}

.status-active {
  background: #e5f7ff;
  color: #27a8e8;
}

.status-ended {
  background: #fee2e2;
  color: #dc2626;
}

.status-paused {
  background: #f3f4f6;
  color: #6b7280;
}
/* ===== PAGE ===== */
.pgg-page {
  font-family: var(--font-family);
}

/* ===== BOX ===== */

.table-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.table-title i {
  font-size: 22px;
  color: #dc2626;
}
.box-header {
  margin-bottom: 16px;
}

.box-header h3 {
  font-size: 18px;
}

.box {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 16px;
  margin-bottom: 20px;
  border: 1px solid #eee;
}
.icon-wrapper-small {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background-color: #ece4e4;
  border-radius: 8px;
  margin-right: 10px;
}
.icon-title-small {
  width: 18px;
  height: 18px;
  display: block;
}

.box-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.box-header h2 {
  margin: 0;
}
.box-header h3 {
  margin: 0;
  font-weight: 700;
  font-size: 19 px;
}

.box-header p {
  margin: 10px 0 0;
  color: var(--color-text-muted);
}

/* ===== FILTER ===== */
.filter-box {
  display: flex;
  gap: 15px;
  align-items: flex-end;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 100%;
}
.filter-item label {
  font-weight: 500;
  color: rgb(114, 112, 112);
}
.custom-input,
.custom-select {
  height: 42px;
  border: 1px solid #ffdada;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 14px;
  outline: none;
}
.filter-item input,
.filter-item select {
  width: 100%;
  padding: 10px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  transition: all 0.22s ease;
  outline: none;
  font-size: 14px;
}
.filter-item input:hover,
.filter-item select:hover {
  border-color: #c8d0dc;
}
.filter-item input:focus,
.filter-item select:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12);
}

/* ===== BUTTON ===== */
.btn.search {
  background: var(--color-primary);
  color: #fff;
  height: 42px;
  padding: 0 20px;
  border-radius: 10px;
  cursor: pointer;
  white-space: nowrap;
}

/* ===== HEADER ACTION BUTTONS ===== */
.header-actions {
  display: flex;
  gap: 20px;
}

/* ===== TABLE ===== */
.pgg-table {
  width: 100%;
  border-collapse: collapse;
}

.pgg-table th {
  background: #e9f2fb;
  padding: 12px;
  text-align: left;
}

.pgg-table td {
  padding: 12px;
  border-bottom: 1px solid #eee;
}

.fw {
  font-weight: 600;
}

/* ===== PILL ===== */
.pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;

  width: 95px;

  padding: 6px 0;

  border-radius: 9px;
  font-size: 12px;
  font-weight: 600;

  box-sizing: border-box;
}

.congkhai {
  background: #d1fae5;
  color: #059669;
}

.canhan {
  background: #dbeafe;
  color: #2563eb;
}

/* ===== DISCOUNT ===== */
.discount .sub {
  font-size: 12px;
  color: #666;
}

.discount-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 6px;
  font-weight: 600;
  margin-bottom: 4px;
}

.percent {
  background: #fee2e2;
  color: #dc2626;
}

.money {
  background: #dcfce7;
  color: #16a34a;
}

/* ===== ACTIONS ===== */
.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-action {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

/* ===== SWITCH ===== */
.switch {
  position: relative;
  width: 40px;
  height: 20px;
  display: inline-block;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  inset: 0;
  background: #ccc;
  border-radius: 20px;
  transition: 0.25s ease;
  cursor: pointer;
}

.slider::before {
  content: "";
  position: absolute;
  width: 16px;
  height: 16px;
  left: 2px;
  bottom: 2px;
  background: #fff;
  border-radius: 50%;
  transition: 0.25s ease;
}

.switch input:checked + .slider {
  background: var(--color-switch-on);
}

.switch input:checked + .slider::before {
  transform: translateX(20px);
}

/* //////////////// HEADER /////////////// */
.main-title {
  display: flex;
  align-items: center;
  font-size: 30px;
  font-weight: bold;
  gap: 12px;
  letter-spacing: 1px;
  color: #1a1a1a;
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

/* ===== SELECT 2 && TIM KIEM ===== */
/* V-SELECT STYLE */
.modern-select {
  width: 100%;
}

.modern-select .vs__dropdown-toggle {
  height: 36px;
  border-radius: 8px;
  border: 1px solid #f3b6b6;
  padding: 0 8px;
  display: flex;
  align-items: center;
}

.modern-select .vs__selected {
  font-size: 14px;
}

.modern-select .vs__search {
  font-size: 14px;
}

.modern-select .vs__actions {
  padding-right: 6px;
}

.modern-select .vs__dropdown-menu {
  border-radius: 8px;
  border: 1px solid #eee;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.1);
}
:deep(.v-select) {
  background: #fff;
  width: 100%;
}

:deep(.vs__dropdown-toggle) {
  border: 1px solid #e5e7eb !important;
  border-radius: 10px !important;
  padding: 0 !important;
  height: 42px;
  display: flex;
  align-items: center;
  transition: all 0.2s ease;
  box-shadow: none !important;
}

:deep(.vs__search),
:deep(.vs__search:focus) {
  border: none !important;
  margin: 0 !important;
  padding: 0 12px !important;
  background: transparent !important;
  box-shadow: none !important;
}

/* Xử lý text đã chọn */
:deep(.vs__selected) {
  position: absolute;
  margin: 0 !important;
  padding-left: 12px !important;
  color: #374151;
  border: none !important;
}

/* Placeholder */
:deep(.vs__search::placeholder) {
  color: #9ca3af !important;
}

:deep(.vs--open .vs__dropdown-toggle) {
  border-color: var(--color-primary) !important;
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12) !important;
}

:deep(.vs__dropdown-toggle:hover) {
  border-color: #c8d0dc !important;
}

/* Menu đổ xuống */
:deep(.vs__dropdown-menu) {
  border: 1px solid #e5e7eb !important;
  border-radius: 10px !important;
  margin-top: 8px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1) !important;
  padding: 6px !important;
  z-index: 1000;
}

/* Các Option bên trong */
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

/* Icon mũi tên và nút xóa */
:deep(.vs__actions) {
  padding-right: 10px !important;
}

:deep(.vs__open-indicator) {
  fill: #9ca3af !important;
  transform: scale(0.7);
}

:deep(.vs__clear) {
  fill: #9ca3af !important;
}

.disabled {
  opacity: 0.4;
  pointer-events: none;
}
.empty-table {
  text-align: center;
  color: #6b7280;
  padding: 20px;
  font-style: italic;
}

/* //////////////// ACTION  */
.add-btn {
  display: flex;
  align-items: center;
  gap: 10px;

  padding: 12px 20px;
  border: none;
  border-radius: 7px;

  background: #eef2ff;
  color: #d73737;

  font-size: 15px;
  font-weight: 600;

  cursor: pointer;
  transition: all 0.25s ease;
}

.add-btn img {
  width: 18px;
  height: 18px;
  object-fit: contain;
}

.add-btn:hover {
  background: #dfe7ff;
  transform: translateY(-1px);
}

.add-btn:active {
  transform: scale(0.98);
}

.excel-btn {
  display: flex;
  align-items: center;
  gap: 10px;

  padding: 12px 18px;

  border: none;
  border-radius: 7px;

  background: #d4f0dd;
  color: #16a34a;

  font-size: 14px;
  font-weight: 600;

  cursor: pointer;
  transition: all 0.25s ease;
}

.excel-btn img {
  width: 18px;
  height: 18px;
  object-fit: contain;
}

.excel-btn:hover {
  background: #dcfce7;
  transform: translateY(-1px);
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8px;

  padding: 9px 14px;

  border: none;
  border-radius: 12px;

  background: #f0f2f7;
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

/* ///////////////////////// PHÂN TRANG ///////////// */
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

/* /////////////// THỜI GIAN PGG */
.time-cell {
  font-size: 13px;
  line-height: 1.4;
}

.time-range {
  display: flex;
  flex-direction: column;
  gap: 4px;

  padding: 6px 10px;
  border-radius: 8px;
  background: #f9fafb;
  border: 1px solid #eef2f7;
}

.time-start {
  color: #059669;
  font-weight: 500;
}

.time-end {
  color: #dc2626;
  font-weight: 500;
}

/* CHECKBOX */
.custom-checkbox {
  width: 16px;
  height: 16px;
  cursor: pointer;
  accent-color: #d73737;
}
.custom-checkbox:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}
</style>
