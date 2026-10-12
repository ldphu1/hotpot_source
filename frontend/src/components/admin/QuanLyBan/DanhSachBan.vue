<script setup>
import { ref, computed, onMounted, watch } from "vue";
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";

/* ===== ICON ===== */
import iconAdd from "@/assets/icons/add.png";
import iconEdit from "@/assets/icons/edit.png";
import iconChange from "@/assets/icons/sync.png";
import iconDanhSach from "@/assets/icons/danh-sach.png";
import iconExcel from "@/assets/icons/excel.png";
import {
  getAllquanlyban,
  getBanPhanTrang,
  xoaBan,
  doiTrangThaiBan,
  themBanAn,
  timKiem,
  exportBanAn,
  importExcelBanAn,
  downloadTemplateExcel,
} from "@/api/admin/quanlyban/quanlybanApi.js";
import { getAllkhuvuc } from "@/api/admin/khuvuc/khuVucApi";
import ThemBan from "./ThemBan.vue";
import SuaBanAn from "./SuaBanDaTao.vue";
import { useUtils } from "@/utils/helper";
import { hasPermission } from "@/utils/permission.js";
import router from "@/router/index.js";
const { notify } = useUtils();

/* ===== FILTER ===== */
const keyword = ref("");
const trangThai = ref("");
const tang = ref("");

/* ===== PAGINATION ===== */
const currentPage = ref(1);
const pageSize = ref(5);
const totalElements = ref(0);
const totalPages = ref(1);

// thêm
const showThemBan = ref(false);
const themBanRef = ref(null);
const suaBanRef = ref(null);
const moThemBan = () => {
  if (!hasPermission("THEM_BAN")) {
    router.push("/403");
    return;
  }
  showThemBan.value = true;
};
/* ===== cbo bàn ===== */
const danhSachKhuVuc = ref([]);

//load lại trang
const reloadKey = ref(0);

/* ===== DATA ===== */
const danhSach = ref([]); // list danh sach quanlyban

// thay đổi trạng thái
const trangThaiTam = ref(null);
const showDoiTrangThai = ref(false);
const banDangChon = ref(null);

// chọn bàn để xóa
const danhSachIdDaChon = ref([]);

//Trạng thái
const mapTrangThai = {
  0: { text: "Không sử dụng", class: "khong_su_dung" },
  1: { text: "Trống", class: "trong" },
  2: { text: "Đã đặt", class: "da_dat" },
  3: { text: "Đang sử dụng", class: "co_khach" },
  4: { text: "Đang dọn", class: "dang_don" },
};

const danhSachTrangThaiFilterOptions = [
  { label: "Tất cả", value: "" },
  { label: "Không sử dụng", value: 0 },
  { label: "Trống", value: 1 },
  { label: "Đã đặt", value: 2 },
  { label: "Đang sử dụng", value: 3 },
  { label: "Đang dọn", value: 4 },
];

const moDoiTrangThai = async (ban) => {
  if (!hasPermission("SUA_BAN")) {
    router.push("/403");
    return;
  }
  if (ban.trangThai === 2 || ban.trangThai === 3) {
    notify.warning("Bàn đã đặt hoặc đang sử dụng, không thể đổi trạng thái!");
    return;
  }

  let trangThaiMoi = 1;
  let tenTrangThaiMoi = "Trống";

  if (ban.trangThai === 1) {
    trangThaiMoi = 0;
    tenTrangThaiMoi = "Không sử dụng";
  } else if (ban.trangThai === 0 || ban.trangThai === 4) {
    trangThaiMoi = 1;
    tenTrangThaiMoi = "Trống";
  }

  if (
    await notify.confirm(
      `Bạn có chắc chắn muốn đổi bàn ${ban.tenBanAn} sang trạng thái "${tenTrangThaiMoi}" không?`,
    )
  ) {
    try {
      await doiTrangThaiBan(ban.id, trangThaiMoi);
      notify.success("Đổi trạng thái thành công!");
      await loadDanhSach();
    } catch (e) {
      const errorMessage =
        typeof e.response?.data === "string"
          ? e.response.data
          : e.response?.data?.message;
      notify.error(errorMessage || "Đổi trạng thái thất bại");
    }
  }
};

// tìm kiếm
const timKiemBan = async () => {
  currentPage.value = 1;
  await loadDanhSach();
};

// Tự động đưa về trang 1 khi thay đổi trạng thái hoặc tầng
watch([trangThai, tang, pageSize], () => {
  if (currentPage.value !== 1) {
    currentPage.value = 1; // Điều này sẽ kích hoạt watcher của currentPage
  } else {
    loadDanhSach();
  }
});

const handleExportExcel = async () => {
  try {
    const response = await exportBanAn(
      keyword.value,
      trangThai.value,
      tang.value,
    );
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", "Danh_Sach_Ban_An.xlsx");
    document.body.appendChild(link);
    link.click();
    link.remove();
    notify.success("Xuất Excel thành công!");
  } catch (error) {
    console.error("Lỗi xuất Excel:", error);
    notify.error("Lỗi xuất Excel!");
  }
};

const handleDownloadTemplate = async () => {
  try {
    const response = await downloadTemplateExcel();
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", "Mau_Them_Ban.xlsx");
    document.body.appendChild(link);
    link.click();
    link.remove();
    notify.success("Tải mẫu Excel thành công!");
  } catch (error) {
    console.error("Lỗi tải mẫu Excel:", error);
    notify.error("Lỗi tải mẫu Excel!");
  }
};

watch(currentPage, () => {
  loadDanhSach();
});

const danhSachKhuVucOptions = computed(() => [
  { tenKhuVuc: "Tất cả", id: "" },
  ...danhSachKhuVuc.value,
]);

/* ===== EXPORT / IMPORT EXCEL ===== */
const excelInputRef = ref(null);

const uploadExcel = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  const formData = new FormData();
  formData.append("file", file);

  try {
    const response = await importExcelBanAn(formData);
    notify.success("Import thành công " + response.data.soLuong + " bàn!");
    await loadDanhSach();
  } catch (error) {
    console.error("Lỗi import", error);
    const msg = error.response?.data;
    notify.error(typeof msg === "string" ? msg : "Lỗi import file Excel");
  } finally {
    if (excelInputRef.value) {
      excelInputRef.value.value = null;
    }
  }
};

/* ===== PAGINATION ===== */

const danhSachHienThi = computed(() => {
  return danhSach.value;
});

const chonTatCa = computed({
  get() {
    const danhSachHopLe = danhSachHienThi.value.filter(
      (item) => item.trangThai !== 2 && item.trangThai !== 3,
    );

    return (
      danhSachHopLe.length > 0 &&
      danhSachHopLe.every((item) => danhSachIdDaChon.value.includes(item.id))
    );
  },
  set(value) {
    if (value) {
      getBanPhanTrang(
        keyword.value,
        trangThai.value !== "" ? trangThai.value : null,
        tang.value !== "" ? tang.value : null,
        1,
        1000000, // Fetch all matching items
      ).then((res) => {
        const tatCaBan = res.data.content;
        const tatCaBanHopLe = tatCaBan.filter(
          (item) => item.trangThai !== 2 && item.trangThai !== 3,
        );
        const newIds = tatCaBanHopLe.map((item) => item.id);
        const uniqueIds = Array.from(
          new Set([...danhSachIdDaChon.value, ...newIds]),
        );
        danhSachIdDaChon.value = uniqueIds;
      });
    } else {
      danhSachIdDaChon.value = [];
    }
  },
});

const goFirst = () => (currentPage.value = 1);
const goPrev = () => currentPage.value > 1 && currentPage.value--;
const goNext = () =>
  currentPage.value < totalPages.value && currentPage.value++;
const goLast = () => (currentPage.value = totalPages.value);

//  nhận dữ liệu thêm bàn từ ThemBan
const handleThemBan = async (payload) => {
  try {
    const resAll = await getAllquanlyban();
    const isDuplicate = resAll.data.some(
      (b) =>
        b.khuVuc?.id === payload.khuVuc.id &&
        b.tenBanAn.trim().toLowerCase() ===
          payload.tenBanAn.trim().toLowerCase(),
    );
    if (isDuplicate) {
      themBanRef.value?.setErrors({
        ten_ban_an: "Tên bàn đã tồn tại trong khu vực này",
      });
      return;
    }

    await themBanAn(payload);

    notify.success("Thêm bàn thành công!");

    showThemBan.value = false; // DÒNG QUAN TRỌNG NHẤT

    await loadDanhSach(); // reload table
  } catch (e) {
    const errorMessage =
      typeof e.response?.data === "string"
        ? e.response.data
        : e.response?.data?.message;
    notify.error(errorMessage || "Thêm bàn thất bại");
  }
};

onMounted(async () => {
  try {
    const res = await getAllkhuvuc();
    danhSachKhuVuc.value = res.data;
  } catch (e) {
    console.error("Lỗi load khu vực", e);
  }
});

// Mở sửa bàn
const showSuaBan = ref(false);
const banDangSua = ref(null);

const moSuaBan = (ban) => {
  if (!hasPermission("SUA_BAN")) {
    router.push("/403");
    return;
  }
  banDangSua.value = { ...ban }; // clone tránh sửa trực tiếp
  showSuaBan.value = true;
};

const loadDanhSach = async () => {
  try {
    const res = await getBanPhanTrang(
      keyword.value,
      trangThai.value !== "" ? trangThai.value : null,
      tang.value !== "" ? tang.value : null,
      currentPage.value,
      pageSize.value,
    );
    danhSach.value = res.data.content;
    totalElements.value = res.data.totalElements;
    totalPages.value = res.data.totalPages || 1;
  } catch (error) {
    console.error("lỗi rồi", error);
  }
};

onMounted(loadDanhSach);

const lamMoi = () => {
  keyword.value = "";
  trangThai.value = "";
  tang.value = "";
  currentPage.value = 1;
  danhSachIdDaChon.value = [];
  loadDanhSach();
};

const xoaBanHandler = async (id) => {
  try {
    await xoaBan(id);

    notify.success("Xóa bàn thành công!");

    showSuaBan.value = false;
    await loadDanhSach();
  } catch (e) {
    const errorMessage =
      typeof e.response?.data === "string"
        ? e.response.data
        : e.response?.data?.message;
    notify.error(errorMessage || "Xóa bàn thất bại");
  }
};

const capNhatBan = async (ban) => {
  try {
    const resAll = await getAllquanlyban();
    const isDuplicate = resAll.data.some(
      (b) =>
        b.id !== ban.id &&
        b.khuVuc?.id === ban.khuVuc.id &&
        b.tenBanAn.trim().toLowerCase() === ban.tenBanAn.trim().toLowerCase(),
    );
    if (isDuplicate) {
      suaBanRef.value?.setErrors({
        tenBanAn: "Tên bàn đã tồn tại trong khu vực này",
      });
      return;
    }

    await themBanAn(ban);

    notify.success("Cập nhật bàn thành công!");

    showSuaBan.value = false;
    await loadDanhSach();
  } catch (e) {
    const errorMessage =
      typeof e.response?.data === "string"
        ? e.response.data
        : e.response?.data?.message;
    notify.error(errorMessage || "Cập nhật bàn thất bại");
  }
};

// xóa nhiều bàn
const xoaNhieuBan = async () => {
  if (!hasPermission("XOA_BAN")) {
    router.push("/403");
    return;
  }
  if (danhSachIdDaChon.value.length === 0) {
    notify.warning("Vui lòng chọn bàn cần xóa!");
    return;
  }

  // dùng notify.confirm thay cho confirm()
  if (
    !(await notify.confirm(
      `Bạn có chắc chắn muốn xóa ${danhSachIdDaChon.value.length} bàn không?`,
    ))
  ) {
    return;
  }

  try {
    for (const id of danhSachIdDaChon.value) {
      await xoaBan(id);
    }

    notify.success("Xóa bàn thành công!");
    danhSachIdDaChon.value = [];
    await loadDanhSach();
  } catch (e) {
    const errorMessage =
      typeof e.response?.data === "string"
        ? e.response.data
        : e.response?.data?.message;
    notify.error(errorMessage || "Xóa bàn thất bại!");
  }
};
</script>

<template>
  <div class="quan-ly-dat-ban">
    <div class="filter-box">
      <div class="filter-item">
        <label>Tìm kiếm bàn</label>
        <input
          class="custom-input"
          type="text"
          placeholder="Mã bàn, tên bàn, sức chứa..."
          v-model="keyword"
          @keyup.enter="timKiemBan"
        />
      </div>

      <div class="filter-item">
        <label>Trạng thái</label>
        <v-select
          class="modern-select"
          v-model="trangThai"
          :options="danhSachTrangThaiFilterOptions"
          label="label"
          :reduce="(opt) => opt.value"
          placeholder="Tất cả trạng thái"
          :clearable="false"
        >
          <template #no-options>Không tìm thấy</template>
        </v-select>
      </div>

      <div class="filter-item">
        <label>Tầng</label>
        <v-select
          class="modern-select"
          v-model="tang"
          :options="danhSachKhuVucOptions"
          label="tenKhuVuc"
          :reduce="(kv) => kv.id"
          placeholder="Tất cả khu vực"
          :clearable="false"
        >
          <template #no-options>Không tìm thấy</template>
        </v-select>
      </div>
    </div>

    <div class="box">
      <div class="box-header box-header-flex">
        <h3 class="table-title">
          <span class="icon-wrapper-small">
            <img
              v-if="iconDanhSach"
              :src="iconDanhSach"
              class="icon-title-small"
            />
            <svg
              v-else
              xmlns="http://www.w3.org/2000/svg"
              w
              width="18"
              height="18"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <path d="M3 6h18" />
              <path d="M3 12h18" />
              <path d="M3 18h18" />
            </svg>
          </span>
          Danh sách bàn
        </h3>

        <div class="header-actions">
          <input
            type="file"
            ref="excelInputRef"
            accept=".xlsx, .xls"
            style="display: none"
            @change="uploadExcel"
          />
          <button class="import-btn" @click="excelInputRef.click()">
            <img src="@/assets/icons/nhapexx.png" alt="" />
            <span>Thêm từ Excel</span>
          </button>

          <button class="excel-btn" @click="handleDownloadTemplate">
            <img :src="iconExcel" alt="" />
            <span>Tải mẫu</span>
          </button>

          <button class="excel-btn" @click="handleExportExcel">
            <img :src="iconExcel" alt="" />
            <span>Xuất Excel</span>
          </button>

          <button class="add-btn" @click="moThemBan">
            <img :src="iconAdd" alt="" />
            <span>Thêm bàn</span>
          </button>

          <button class="refresh-btn" @click="lamMoi">
            <img src="@/assets/icons/refresh.png" alt="" />
            <span>Làm mới</span>
          </button>
        </div>
      </div>

      <div class="table-content">
        <table class="table-main">
          <thead>
            <tr>
              <th style="width: 50px">
                <input
                  type="checkbox"
                  v-model="chonTatCa"
                  class="custom-checkbox"
                />
              </th>
              <th style="width: 50px">STT</th>
              <th>Mã bàn</th>
              <th>Tên bàn</th>
              <th>Tầng</th>
              <th>Sức chứa</th>
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
                  v-model="danhSachIdDaChon"
                  :disabled="item.trangThai === 2 || item.trangThai === 3"
                />
              </td>
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
              <td class="text-bold">{{ item.maBanAn }}</td>
              <td>
                {{ "Bàn " + item.tenBanAn }}
              </td>
              <td>{{ item.khuVuc.tenKhuVuc }}</td>
              <td>{{ item.sucChuaToiDa }} người</td>
              <td>
                <span
                  class="status-text"
                  :class="mapTrangThai[item.trangThai]?.class"
                >
                  {{ mapTrangThai[item.trangThai]?.text }}
                </span>
              </td>
              <td class="text-center">
                <div class="action-cell">
                  <button
                    class="btn-action-icon"
                    @click="moSuaBan(item)"
                    v-tooltip="'Sửa bàn'"
                  >
                    <img :src="iconEdit" class="img-icon-action" />
                  </button>
                  <label class="switch" v-tooltip="'Đổi trạng thái'">
                    <input
                      type="checkbox"
                      :checked="item.trangThai !== 0"
                      :disabled="item.trangThai === 2 || item.trangThai === 3"
                      @click.prevent="moDoiTrangThai(item)"
                    />
                    <span class="slider"></span>
                  </label>
                </div>
              </td>
            </tr>
            <tr v-if="danhSachHienThi.length === 0">
              <td colspan="8" class="empty-table">
                Không tìm thấy bàn phù hợp
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="d-flex justify-content-between align-items-center mt-3">
        <div class="d-flex align-items-center gap-2">
          <span>Hiển thị</span>
          <select class="form-select form-select-sm w-auto" v-model="pageSize">
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
          Hiển thị {{ danhSachHienThi.length }} / {{ totalElements }} bàn
        </div>
      </div>
    </div>
  </div>

  <ThemBan
    ref="themBanRef"
    v-if="showThemBan"
    :key="reloadKey"
    :danhSachKhuVuc="danhSachKhuVuc"
    @close="showThemBan = false"
    @submit="handleThemBan"
  />

  <SuaBanAn
    ref="suaBanRef"
    :show="showSuaBan"
    :key="reloadKey"
    :ban="banDangSua"
    :danhSachTang="danhSachKhuVuc"
    @close="showSuaBan = false"
    @save="capNhatBan"
    @deleted="xoaBanHandler"
  />
</template>

<style scoped>
/* ================== LAYOUT & COMPONENTS CHUNG ================== */
.box {
  background: #fff;
  border-radius: 7px;
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

/* ===== FILTER BOX ===== */
.filter-box {
  background: white;
  padding: 16px 20px;
  border-radius: 7px;
  margin-bottom: 24px;
  display: flex;
  align-items: flex-end;
  gap: 12px;
  border: 1px solid #eee;
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
  height: 42px;
  border: 1px solid #e5e7eb;
  border-radius: 7px;
  padding: 0 12px;
  font-size: 14px;
  outline: none;
  transition: all 0.2s;
}
.custom-input:hover {
  border-color: #c8d0dc;
}
.custom-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12);
}
.btn.search {
  background-color: #00d1f3;
  color: white;
  border: none;
  border-radius: 7px;
  height: 42px;
  padding: 0 24px;
  font-weight: 700;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;
}
.btn.search:hover {
  background-color: #00b8d4;
}

/* ===== SELECT 2 ĐỒNG BỘ ===== */
:deep(.v-select) {
  background: #fff;
  width: 100%;
  --vs-dropdown-option--active-bg: #f3f4f6;
  --vs-dropdown-option--active-color: #eb6464;
  --vs-border-color: #e5e7eb;
  --vs-border-radius: 7px;
  --vs-dropdown-box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1);
}
:deep(.vs__dropdown-toggle) {
  border: 1px solid #e5e7eb !important;
  border-radius: 7px !important;
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
  border-radius: 7px !important;
  margin-top: 8px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1) !important;
  padding: 6px !important;
  z-index: 1000;
}
:deep(.vs__dropdown-option) {
  border-radius: 7px !important;
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

/* ===== TABLE TITLE & ACTIONS ===== */
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
  border-radius: 7px;
  margin-right: 10px;
}
.icon-title-small {
  width: 18px;
  height: 18px;
}
.header-actions {
  display: flex;
  gap: 10px;
}

/* Nút Action Thêm / Excel / Xóa / Refesh đồng bộ */
.add-btn,
.import-btn,
.excel-btn,
.delete-btn,
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
.import-btn img,
.excel-btn img,
.delete-btn img,
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

.import-btn {
  background: #e0f2fe;
  color: #d73737;
}
.import-btn:hover {
  background: #bae6fd;
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

.delete-btn {
  background: #fee2e2;
  color: #d73737;
}
.delete-btn:hover {
  background: #fecaca;
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

/* ===== TABLE CHÍNH ===== */
.table-main {
  width: 100%;
  border-collapse: collapse;
  table-layout: auto;
  background: white;
  border-radius: 7px;
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

/* Action Icons trong Table (Sửa, Sync) */
.action-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
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
.img-icon-action {
  width: 17px;
  height: 17px;
  object-fit: contain;
}

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
  background: #fee2e2;
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
input:disabled + .slider {
  cursor: not-allowed;
  opacity: 0.5;
}

/* ===== STATUS BÀN (Tương tự hệ màu chuẩn) ===== */
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

/* ===== STATUS BÀN TRONG BẢNG (Nền mờ 12%, Chữ nét) ===== */
.khong_su_dung {
  background: rgba(107, 114, 128, 0.12);
  color: #6b7280;
}
.trong {
  background: rgba(34, 197, 94, 0.12);
  color: #22c55e;
} /* Xanh lá */
.da_dat {
  background: rgba(59, 130, 246, 0.12);
  color: #3b82f6;
} /* Xanh nước biển */
.co_khach {
  background: rgba(239, 68, 68, 0.12);
  color: #ef4444;
} /* Đỏ */
.dang_don {
  background: rgba(245, 158, 11, 0.12);
  color: #f59e0b;
} /* Vàng (Cam vàng) */

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
  border-radius: 7px;
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
  border-radius: 7px;
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

/* ================== MODAL ĐỔI TRẠNG THÁI (Giữ form cũ, tinh chỉnh màu) ================== */
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.box-trang-thai {
  background: #fff;
  width: max-content;
  min-width: 700px;
  max-width: 90vw;
  padding: 30px 40px;
  border-radius: 7px;
}
.step-wrapper {
  margin-top: 20px;
  position: relative;
}
.radio-group {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 10px;
  position: relative;
}
.radio-group::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 30px;
  right: 30px;
  height: 2px;
  background: #e5e7eb;
  transform: translateY(-50%);
  z-index: 0;
}
.radio-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-weight: 500;
  position: relative;
  z-index: 1;
  background: #fff;
  padding: 4px 6px;
}
.radio-item input {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #d73737;
}
.radio-label {
  padding: 6px 16px;
  border-radius: 7px;
  font-size: 14px;
  white-space: nowrap;
}

/* ===== STATUS BÀN TRONG MODAL ĐỔI TRẠNG THÁI (Radio) ===== */
.radio-label.khong_su_dung {
  background: rgba(107, 114, 128, 0.15);
  color: #6b7280;
}
.radio-label.trong {
  background: rgba(34, 197, 94, 0.15);
  color: #22c55e;
}
.radio-label.da_dat {
  background: rgba(59, 130, 246, 0.15);
  color: #3b82f6;
}
.radio-label.co_khach {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}
.radio-label.dang_don {
  background: rgba(245, 158, 11, 0.15);
  color: #f59e0b;
}

.confirm-box {
  margin-top: 30px;
  text-align: center;
}
.btn-cancel {
  background: #f1f5f9;
  color: #334155;
  border: 1px solid #e2e8f0;
  padding: 10px 24px;
  border-radius: 7px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}
.btn-cancel:hover {
  background: #e2e8f0;
  transform: translateY(-2px);
}

.btn-primary {
  position: relative;
  overflow: hidden;
  background: #d73737;
  color: #fff;
  border: none;
  padding: 10px 24px;
  border-radius: 7px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}
.btn-primary:hover {
  background: #b92b2b;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(230, 90, 90, 0.3);
}
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
.btn-primary:hover .btn-light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
}
</style>
