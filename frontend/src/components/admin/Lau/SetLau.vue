<script setup>
import { onMounted, ref, computed } from "vue";
import { useRouter } from "vue-router";
import { chuyenTrangThai, getAllSetLau } from "@/api/admin/lau/setLauApi";
import { getAllNuocLau } from "@/api/admin/nuoclau/nuocLauApi";
import { searchSetLau } from "@/api/admin/lau/setLauApi";
import { useUtils } from "@/utils/helper";
import { exportSetLau } from "@/api/admin/lau/setLauApi";
const { notify, nprogress } = useUtils();

/* ===== ICON ===== */
import iconAdd from "@/assets/icons/add.png";
import iconExcel from "@/assets/icons/excel.png";
import iconSearch from "@/assets/icons/search.png";
import iconEdit from "@/assets/icons/edit.png";
import iconRefresh from "@/assets/icons/refresh.png";
import iconView from "@/assets/icons/view.png";
import iconDanhSach from "@/assets/icons/danh-sach.png";

const router = useRouter();
const danhSach = ref([]);
const danhSachNuocLau = ref([]);

// SELECT 2
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";
import { getAllThamSoCustomer } from "@/api/customer/datbanonline/datBanOnlineApi";
import { hasPermission } from "@/utils/permission";

//SEARCH
const keyword = ref("");
const idNuocLau = ref("");
const trangThai = ref("");
const minPrice = ref("");
const maxPrice = ref("");

const searchData = async () => {
  try {
    const params = {};

    if (keyword.value) params.keyword = keyword.value;
    if (idNuocLau.value) params.idNuocLau = idNuocLau.value;

    if (trangThai.value !== "") {
      params.trangThaiXoa = trangThai.value === "1";
    }

    if (minPrice.value) params.minPrice = minPrice.value;
    if (maxPrice.value) params.maxPrice = maxPrice.value;

    // Nếu không có filter nào → load toàn bộ
    if (Object.keys(params).length === 0) {
      await loadData();
      return;
    }

    const res = await searchSetLau(params);

    danhSach.value = res.data;
    currentPage.value = 1;
  } catch (error) {
    console.error("Search lỗi", error);
  }
};

/* ===== FETCH DATA ===== */
const loadData = async () => {
  try {
    const res = await getAllSetLau();
    danhSach.value = res.data;
  } catch (error) {
    console.error("Khong tim thay set lau", error);
  }
};
const loadNuocLau = async () => {
  try {
    const res = await getAllNuocLau();
    danhSachNuocLau.value = (res.data || []).filter((nl) => !nl.trangThaiXoa);
  } catch (error) {
    console.log("Không tìm thấy nước lẩu", error);
  }
};

const handleExportSetLau = async () => {
  try {
    const params = {};

    if (keyword.value) params.keyword = keyword.value;
    if (idNuocLau.value) params.idNuocLau = idNuocLau.value;
    if (trangThai.value !== "") {
      params.trangThaiXoa = trangThai.value === "1";
    }
    if (minPrice.value) params.minPrice = minPrice.value;
    if (maxPrice.value) params.maxPrice = maxPrice.value;

    const res = await exportSetLau(params);

    const url = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute(
      "download",
      `set_lau_${new Date().toISOString().slice(0, 10)}.xlsx`,
    );
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
    notify.success("Xuất excel thành công");
  } catch (e) {
    console.error("Lỗi export set lẩu:", e);
  }
};

onMounted(() => {
  loadData();
  loadNuocLau();
});

const reloadData = async () => {
  keyword.value = "";
  idNuocLau.value = "";
  trangThai.value = "";
  minPrice.value = "";
  maxPrice.value = "";

  currentPage.value = 1;
  selectedIds.value = [];

  await loadData();
};

/* ===== NAVIGATION ===== */
const updateTrangThai = async (id) => {
  if (!(await notify.confirm("Bạn có muốn chuyển trạng thái?"))) return;
  nprogress.start();
  try {
    await chuyenTrangThai(id);
    notify.success("Chuyển trạng thái thành công");
    await loadData();
  } catch (e) {
    notify.error(e.response?.data || "Có lỗi xảy ra");
  }
  nprogress.done();
};

const chuyenAddSetlau = () => {
  router.push("/admin/set-lau/view-save");
};

const viewDetail = (id) => {
  router.push(`/admin/set-lau/detail/${id}`);
};

const chuyenUpdateSetLau = (id) => {
  router.push(`/admin/set-lau/sua/${id}`);
};

/* ===== PHÂN TRANG ===== */
const currentPage = ref(1);
const pageSize = ref(5);
const totalItems = computed(() => danhSach.value.length);
const totalPages = computed(
  () => Math.ceil(totalItems.value / pageSize.value) || 1,
);
const danhSachHienThi = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return danhSach.value.slice(start, start + pageSize.value);
});

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

const goFirst = () => (currentPage.value = 1);
const goPrev = () => {
  if (currentPage.value > 1) currentPage.value--;
};
const goNext = () => {
  if (currentPage.value < totalPages.value) currentPage.value++;
};
const goLast = () => (currentPage.value = totalPages.value);
</script>

<template>
  <div class="quan-ly-set-lau">
    <div class="box">
      <div class="box-header">
        <h2 class="main-title">
          <span class="icon-wrapper">
            <img src="@/assets/icons/setlau.png" alt="Set lẩu" />
          </span>
          Quản Lý Set Lẩu
        </h2>
        <p>Thực đơn và các món ăn trong hệ thống</p>
      </div>
    </div>

    <div class="filter-box">
      <div class="filter-item">
        <label>Tìm kiếm</label>
        <input
          class="custom-input"
          v-model="keyword"
          placeholder="Tên món, mô tả..."
          @input="searchData"
        />
      </div>

      <div class="filter-item">
        <label>Loại lẩu</label>
        <v-select
          class="modern-select"
          v-model="idNuocLau"
          :options="danhSachNuocLau"
          label="tenNuocLau"
          :reduce="(item) => item.id"
          placeholder="Tất cả loại lẩu"
          @update:modelValue="searchData"
        >
          <template #no-options="{ search, searching }">
            <template v-if="searching">
              Không tìm thấy "<b>{{ search }}</b
              >"
            </template>
            <em v-else style="opacity: 0.5">Không có dữ liệu</em>
          </template>
        </v-select>
      </div>

      <div class="filter-item">
        <label>Trạng thái</label>
        <v-select
          class="modern-select"
          v-model="trangThai"
          :options="[
            { label: 'Tất cả', value: '' },
            { label: 'Hoạt động', value: '0' },
            { label: 'Ngừng bán', value: '1' },
          ]"
          :reduce="(opt) => opt.value"
          placeholder="Tất cả trạng thái"
          @update:modelValue="searchData"
          :clearable="false"
        >
        </v-select>
      </div>

      <div class="filter-item">
        <label>Giá từ</label>
        <input
          class="custom-input"
          type="number"
          placeholder="Nhập giá tối thiểu..."
          v-model="minPrice"
          @change="searchData"
        />
      </div>

      <div class="filter-item">
        <label>Đến</label>
        <input
          class="custom-input"
          type="number"
          placeholder="Nhập giá tối đa..."
          v-model="maxPrice"
          @change="searchData"
        />
      </div>
    </div>

    <div class="box">
      <div class="box-header box-header-flex">
        <h3 class="table-title">
          <span class="icon-wrapper-small">
            <img :src="iconDanhSach" class="icon-title-small" />
          </span>
          Danh sách món ăn
        </h3>

        <div class="header-actions">
          <button
            class="add-btn"
            @click="chuyenAddSetlau"
            v-if="hasPermission('THEM_SAN_PHAM')"
          >
            <img :src="iconAdd" alt="" />
            <span>Thêm món</span>
          </button>
          <button
            class="excel-btn"
            data-tooltip="Xuất Excel"
            @click="handleExportSetLau()"
          >
            <img :src="iconExcel" alt="" />
            <span>Xuất Excel</span>
          </button>
          <button class="refresh-btn" @click="reloadData">
            <img :src="iconRefresh" alt="" />
            <span>Làm mới</span>
          </button>
        </div>
      </div>

      <div class="table-content">
        <table class="table-main">
          <thead>
            <tr>
              <th>
                <input
                  type="checkbox"
                  class="custom-checkbox"
                  v-model="isAllSelected"
                />
              </th>
              <th style="width: 50px">STT</th>
              <th>Mã set lẩu</th>
              <th style="text-align: left">Tên set lẩu</th>
              <th style="text-align: left">Giá set lẩu</th>
              <th style="text-align: left">Mô tả</th>
              <th>Trạng thái</th>
              <th style="text-align: center">Thao tác</th>
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
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
              <td>
                {{ item.maSanPham }}
              </td>
              <td style="text-align: left">
                <span class="text-bold">{{ item.tenSanPham }}</span>
              </td>
              <td style="text-align: left" class="text-bold">
                {{ item.gia?.toLocaleString() }} đ
              </td>
              <td class="desc" style="text-align: left" :title="item.moTa">
                {{ item.moTa }}
              </td>
              <td>
                <span
                  class="status-text"
                  :class="item.trangThaiXoa ? 'status-error' : 'status-success'"
                >
                  {{ item.trangThaiXoa ? "Ngưng bán" : "Hoạt động" }}
                </span>
              </td>
              <td class="text-center">
                <div class="action-cell">
                  <button
                    class="btn-action-icon"
                    :class="{ disabled: item.trangThaiXoa }"
                    @click="!item.trangThaiXoa && chuyenUpdateSetLau(item.id)"
                    v-tooltip="item.trangThaiXoa ? '' : 'Chỉnh sửa'"
                  >
                    <img :src="iconEdit" class="img-icon-action" />
                  </button>

                  <button
                    class="btn-action-icon"
                    :class="{ disabled: item.trangThaiXoa }"
                    @click="viewDetail(item.id)"
                    v-tooltip="'Xem chi tiết'"
                  >
                    <img :src="iconView" class="img-icon-action" />
                  </button>

                  <label class="switch" v-tooltip="'Đổi trạng thái'">
                    <input
                      type="checkbox"
                      :checked="!item.trangThaiXoa"
                      @change="updateTrangThai(item.id)"
                    />
                    <span class="slider"></span>
                  </label>
                </div>
              </td>
            </tr>
            <tr v-if="danhSachHienThi.length === 0">
              <td colspan="8" class="empty-table">
                🔍 Không tìm thấy sản phẩm phù hợp
              </td>
            </tr>
          </tbody>
        </table>
      </div>

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
                :disabled="currentPage === 1"
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
                :disabled="currentPage === totalPages"
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

        <div class="text-muted">
          Hiển thị {{ danhSachHienThi.length }} / {{ totalItems }} sản phẩm
        </div>
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

/* ================== KHÓA FORM LAYOUT ================== */
/* Chỉ cuộn nội dung bảng, cố định phân trang */
.table-content {
  flex: 1;
  overflow-y: visible;
  margin-bottom: 12px;
}

/* ======== HEADER =======*/
.header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-flex.no-border {
  border: none;
}
.header-section {
  width: 100%;
}
.title-group {
  display: flex;
  align-items: center;
  gap: 16px;
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
.icon-title {
  width: 28px;
  height: 28px;
  object-fit: contain;
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
.sub-title {
  color: #666;
  margin-top: 4px;
  margin-left: 60px;
  font-size: 14px;
}
.box-header p {
  margin: 10px 0 0;
  color: var(--color-text-muted);
}

.table-title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: #333;
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

/* ===== FILTER BOX CHUẨN ===== */
.filter-box {
  background: white;
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 24px;
  display: flex;
  flex-wrap: wrap; /* Cho phép rớt dòng nếu hẹp */
  align-items: flex-end;
  gap: 12px;
  border: 1px solid #eee;
}
.filter-item {
  flex: 1;
  min-width: 150px; /* Giữ layout không bị bóp méo */
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

/* ===== HEADER ACTIONS ĐỒNG BỘ ===== */
.header-actions {
  display: flex;
  gap: 10px;
}
.add-btn,
.excel-btn,
.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 14px;
  border: none;
  border-radius: 7px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}
.add-btn img,
.excel-btn img,
.refresh-btn img {
  width: 16px;
  height: 16px;
  object-fit: contain;
}
.add-btn {
  background: #eef2ff;
  color: #d73737;
}
.add-btn:hover {
  background: #dfe7ff;
  transform: translateY(-1px);
}

.excel-btn {
  background: #d4f0dd;
  color: #d73737;
}
.excel-btn:hover {
  background: #dcfce7;
  transform: translateY(-1px);
}

.refresh-btn {
  background: #f3f4f6;
  color: #d73737;
}
.refresh-btn:hover {
  background: #e5e7eb;
  transform: translateY(-1px);
}
.refresh-btn:hover img {
  transform: rotate(180deg);
  transition: transform 0.4s ease;
}

/* ===== BẢNG CHÍNH ĐỒNG BỘ ===== */
.table-main {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  font-weight: 500;
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
  font-size: 17px;
  border-bottom: none;
}
.table-main td {
  border-bottom: 1px solid #f0f0f0;
  font-size: 15px;
}
.empty-table {
  text-align: center;
  color: #6b7280;
  padding: 20px;
  font-style: italic;
}
.text-bold {
  font-weight: 600;
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

/* MÔ TẢ (Giới hạn text) */
.desc {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #666;
}

/* TRẠNG THÁI (PILL) */
.status-text {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 100px;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
}
.status-success {
  background: rgba(34, 197, 94, 0.12);
  color: #22c55e;
}
.status-error {
  background: rgba(239, 68, 68, 0.12);
  color: #ef4444;
}

/* ACTION ICONS & SWITCH TRONG TABLE */
.action-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}
.btn-action-icon {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 50px;
  background: #f2f5f7;
  cursor: pointer;
  transition: all 0.2s ease;
}
.btn-action-icon:hover {
  background: #eef2ff;
  transform: translateY(-1px);
}
.btn-action-icon.disabled {
  opacity: 0.4;
  cursor: not-allowed;
  pointer-events: none;
}
.img-icon-action {
  width: 17px;
  height: 17px;
  object-fit: contain;
}

/* Nút Switch */
.switch {
  position: relative;
  width: 38px;
  height: 20px;
  display: inline-block;
  margin-top: 2px;
}
.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}
.slider {
  position: absolute;
  inset: 0;
  background: #fee2e2; /* Nền đỏ nhạt khi tắt (tùy chọn) */
  border-radius: 34px;
  transition: 0.3s;
  cursor: pointer;
}

.slider:before {
  position: absolute;
  content: "";
  height: 16px;
  width: 16px;
  left: 2px;
  bottom: 2px;
  background: white;
  transition: 0.3s;
  border-radius: 50%;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}
input:checked + .slider {
  background: #d73737;
}
input:checked + .slider:before {
  transform: translateX(18px);
}

/* ===== PHÂN TRANG (PAGINATION PREMIUM) ===== */
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
.page-indicator-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  height: 36px;
  padding: 0 16px;
  background: #f3efef;
  border-radius: 7px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
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

/* ===== SELECT 2 ĐỒNG BỘ ===== */
:deep(.v-select) {
  background: #fff;
  width: 100%;
  --vs-dropdown-option--active-bg: #f3f4f6;
  --vs-dropdown-option--active-color: #eb6464;
  --vs-border-color: #e5e7eb;
  --vs-border-radius: 10px;
  --vs-dropdown-box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1);
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
:deep(.vs__selected) {
  position: absolute;
  margin: 0 !important;
  padding-left: 12px !important;
  color: #374151;
  border: none !important;
}
:deep(.vs__search::placeholder) {
  color: #9ca3af !important;
}
:deep(.vs__dropdown-toggle:hover) {
  border-color: #c8d0dc !important;
}
:deep(.vs--open .vs__dropdown-toggle) {
  border-color: var(--color-primary) !important;
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12) !important;
}
:deep(.vs__dropdown-menu) {
  border: 1px solid #e5e7eb !important;
  border-radius: 10px !important;
  margin-top: 8px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1) !important;
  padding: 6px !important;
  z-index: 1000;
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
</style>
