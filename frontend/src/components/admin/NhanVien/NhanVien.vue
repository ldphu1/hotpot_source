<script setup>
import { onMounted, ref, watch, computed } from "vue";
import { useRouter } from "vue-router";
import { useUtils } from "@/utils/helper";

/* ===== ICON ===== */
import iconAdd from "@/assets/icons/add.png";
import iconExcel from "@/assets/icons/excel.png";
import iconEdit from "@/assets/icons/edit.png";
import iconNhanVien from "@/assets/icons/nhanvien.png";
import iconDanhSach from "@/assets/icons/danh-sach.png";
import iconRefresh from "@/assets/icons/refresh.png";

import {
  getAllNhanVien,
  doiTrangThaiNhanVien,
  searchNhanVien,
  importNhanVienExcel,
  downloadNhanVienTemplate,
  exportNhanVienExcel,
} from "@/api/admin/nhanVien/nhanVienApi";

const router = useRouter();
const { notify, nprogress } = useUtils();

const currentRole = localStorage.getItem("role");

const laCungQuanLy = (nv) => {
  return currentRole === "ADMIN" && nv.chucVu == 1;
};

/* ===== STATE & DATA ===== */
const danhSach = ref([]);
const fileInput = ref(null);
const selectedIds = ref([]);
const filter = ref({
  ten: "",
  contact: "",
  trangThai: "",
});

/* ===== FETCH DATA ===== */
onMounted(async () => {
  try {
    const res = await getAllNhanVien();
    danhSach.value = res.data.sort((a, b) => b.id - a.id);
  } catch (error) {
    console.error("Không tìm thấy nhân viên", error);
  }
});

const reloadNhanVien = async () => {
  try {
    const res = await getAllNhanVien();
    danhSach.value = res.data.sort((a, b) => b.id - a.id);
    filter.value = { ten: "", contact: "", trangThai: "" };
    currentPage.value = 1;
    selectedIds.value = [];
  } catch (e) {
    console.error(e);
  }
};

const handleSearch = async () => {
  try {
    const res = await searchNhanVien({
      ten: filter.value.ten || null,
      contact: filter.value.contact || null,
      trangThai: filter.value.trangThai === "" ? null : filter.value.trangThai,
    });
    danhSach.value = res.data;
    currentPage.value = 1;
  } catch (e) {
    notify.error("Tìm kiếm thất bại");
  }
};

watch(
  filter,
  () => {
    handleSearch();
  },
  { deep: true },
);

/* ===== EXPORT / IMPORT EXCEL ===== */
const exportSelected = async () => {
  if (selectedIds.value.length === 0) {
    notify.error("Chưa chọn nhân viên nào");
    return;
  }
  try {
    nprogress.start();
    const res = await exportNhanVienExcel(selectedIds.value);
    const url = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement("a");
    link.href = url;
    link.download = "nhan-vien.xlsx";
    link.click();
    selectedIds.value = [];
    notify.success("Xuất Excel thành công");
  } catch (err) {
    notify.error("Xuất Excel thất bại");
  } finally {
    nprogress.done();
  }
};

const downloadTemplate = async () => {
  try {
    const res = await downloadNhanVienTemplate();
    const url = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement("a");
    link.href = url;
    link.download = "nhan-vien-template.xlsx";
    link.click();
  } catch (err) {
    notify.error("Tải file mẫu thất bại");
  }
};

const handleFile = async (e) => {
  const file = e.target.files[0];
  if (!file) return;

  if (!file.name.endsWith(".xlsx")) {
    notify.error("Chỉ chấp nhận file Excel (.xlsx)");
    return;
  }

  const ok = await notify.confirm("Xác nhận import file này?");
  if (!ok) return;

  try {
    nprogress.start();
    const res = await importNhanVienExcel(file);
    if (!res.data.success) {
      notify.error("Import thất bại");
      if (res.data.errors?.length) console.table(res.data.errors);
      return;
    }
    notify.success("Import thành công");
    reloadNhanVien();
  } catch (err) {
    notify.error(err.response?.data || "Import thất bại");
  } finally {
    e.target.value = "";
    nprogress.done();
  }
};

/* ===== ACTIONS ===== */
const goToAddNhanVien = () => {
  router.push("/admin/nhan-vien/save");
};
const goEdit = (id) => {
  router.push(`/admin/nhan-vien/sua/${id}`);
};

const doiTrangThai = async (nv) => {
  const isInactive = nv.trangThaiXoa;
  const ok = await notify.confirm(
    isInactive
      ? "Kích hoạt lại nhân viên này?"
      : "Chuyển nhân viên này sang nghỉ?",
  );
  if (!ok) return;

  try {
    nprogress.start();
    await doiTrangThaiNhanVien(nv.id);
    nv.trangThaiXoa = !nv.trangThaiXoa;
    notify.success(
      isInactive
        ? "Đã kích hoạt lại nhân viên"
        : "Đã chuyển nhân viên sang nghỉ",
    );
  } catch (e) {
    notify.error("Đổi trạng thái thất bại");
  } finally {
    nprogress.done();
  }
};

// Tách tại dấu phẩy thứ 2: line1 = “chi tiết, phường”, line2 = “tỉnh”
const splitDiaChi = (fullStr) => {
  if (!fullStr || fullStr === "—") return { line1: fullStr, line2: "" };
  const first = fullStr.indexOf(",");
  if (first === -1) return { line1: fullStr, line2: "" };
  const second = fullStr.indexOf(",", first + 1);
  if (second === -1) return { line1: fullStr, line2: "" };
  return {
    line1: fullStr.slice(0, second).trim(),
    line2: fullStr.slice(second + 1).trim(),
  };
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

const isAllSelected = computed({
  get() {
    return (
      danhSachHienThi.value.length > 0 &&
      danhSachHienThi.value.every((nv) => selectedIds.value.includes(nv.id))
    );
  },
  set(val) {
    if (val) {
      danhSachHienThi.value.forEach((nv) => {
        if (!selectedIds.value.includes(nv.id)) {
          selectedIds.value.push(nv.id);
        }
      });
    } else {
      const idsToRemove = danhSachHienThi.value.map((nv) => nv.id);
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
  <div class="quan-ly-nhan-vien">
    <div class="box">
      <div class="box-header">
        <h2 class="main-title">
          <span class="icon-wrapper">
            <img :src="iconNhanVien" alt="Nhân viên" />
          </span>
          Quản Lý Nhân Viên
        </h2>
        <p>Quản lý danh sách nhân viên và phân quyền hệ thống</p>
      </div>
    </div>

    <div class="box">
      <div class="filter-box">
        <div class="filter-item">
          <label>Họ và tên</label>
          <input
            class="custom-input"
            v-model="filter.ten"
            placeholder="Nhập tên nhân viên..."
          />
        </div>

        <div class="filter-item">
          <label>Số điện thoại / Email</label>
          <input
            class="custom-input"
            v-model="filter.contact"
            placeholder="Nhập SĐT hoặc email..."
          />
        </div>

        <div class="filter-item">
          <label>Trạng thái</label>
          <select class="custom-input" v-model="filter.trangThai">
            <option value="">Tất cả trạng thái</option>
            <option :value="false">Đang hoạt động</option>
            <option :value="true">Ngừng hoạt động</option>
          </select>
        </div>
      </div>
    </div>

    <div class="box">
      <div class="box-header box-header-flex">
        <h3 class="table-title">
          <span class="icon-wrapper-small">
            <img :src="iconDanhSach" class="icon-title-small" />
          </span>
          Danh sách nhân viên
        </h3>

        <div class="header-actions">
          <button class="add-btn" @click="goToAddNhanVien">
            <img :src="iconAdd" alt="" />
            <span>Thêm nhân viên</span>
          </button>

          <button
            class="download-btn"
            @click="downloadTemplate"
            data-tooltip="Tải file mẫu"
          >
            <img src="@/assets/icons/dowload.png" alt="" />
            <span>Tải mẫu</span>
          </button>

          <input
            type="file"
            ref="fileInput"
            accept=".xlsx"
            style="display: none"
            @change="handleFile"
          />

          <button
            class="import-btn"
            @click="fileInput.click()"
            data-tooltip="Nhập Excel"
          >
            <img src="@/assets/icons/nhapexx.png" alt="" />
            <span>Nhập Excel</span>
          </button>

          <button
            class="excel-btn"
            @click="exportSelected"
            data-tooltip="Xuất Excel"
          >
            <img :src="iconExcel" alt="" />
            <span>Xuất Excel</span>
          </button>

          <button
            class="refresh-btn"
            @click="reloadNhanVien"
            data-tooltip="Làm mới"
          >
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
              <th>STT</th>
              <th style="text-align: left">Nhân viên</th>
              <th>Chức vụ</th>
              <th>Giới tính</th>
              <th style="text-align: left">Liên hệ</th>
              <th style="text-align: left">Địa chỉ</th>
              <th>Trạng thái</th>
              <th style="text-align: center">Thao tác</th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="(nv, index) in danhSachHienThi" :key="nv.id">
              <td>
                <input
                  type="checkbox"
                  class="custom-checkbox"
                  :value="nv.id"
                  v-model="selectedIds"
                />
              </td>
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>

              <td style="text-align: left">
                <div class="user-cell">
                  <img
                    v-if="nv.anhNhanVien"
                    :src="`http://localhost:8080/uploads/${nv.anhNhanVien}`"
                    class="avatar-img"
                    alt="avatar"
                  />
                  <div v-else class="avatar-placeholder">
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="20"
                      height="20"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    >
                      <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
                      <circle cx="12" cy="7" r="4" />
                    </svg>
                  </div>
                  <span class="text-bold">{{ nv.tenNhanVien }}</span>
                </div>
              </td>

              <td>
                <span
                  class="role-badge"
                  :class="nv.chucVu == 1 ? 'role-admin' : 'role-staff'"
                >
                  {{
                    nv.chucVu == 1
                      ? "Quản lý"
                      : nv.chucVu == 2
                      ? "Thu ngân"
                      : "Nhân viên"
                  }}
                </span>
              </td>

              <td>{{ nv.gioiTinh ? "Nam" : "Nữ" }}</td>

              <td style="text-align: left">
                <div class="contact-info">
                  <span class="contact-line" v-if="nv.email" :title="nv.email">
                    <i class="bi bi-envelope-fill text-primary me-1"></i>
                    <span class="contact-text">{{ nv.email }}</span>
                  </span>
                  <span class="contact-line" v-if="nv.soDienThoai">
                    <i class="bi bi-telephone-fill text-danger me-1"></i>
                    <span class="contact-text">{{ nv.soDienThoai }}</span>
                  </span>
                </div>
              </td>

              <td>
                <div
                  class="address-cell"
                  :title="`${nv.diaChiChiTiet}, ${nv.xaPhuong}, ${nv.tinhThanh}`"
                >
                  <span class="addr-line1">{{
                    splitDiaChi(
                      `${nv.diaChiChiTiet}, ${nv.xaPhuong}, ${nv.tinhThanh}`,
                    ).line1
                  }}</span>
                  <span
                    class="addr-line2"
                    v-if="
                      splitDiaChi(
                        `${nv.diaChiChiTiet}, ${nv.xaPhuong}, ${nv.tinhThanh}`,
                      ).line2
                    "
                  >
                    {{
                      splitDiaChi(
                        `${nv.diaChiChiTiet}, ${nv.xaPhuong}, ${nv.tinhThanh}`,
                      ).line2
                    }}
                  </span>
                </div>
              </td>

              <td>
                <span
                  class="status-text"
                  :class="!nv.trangThaiXoa ? 'status-success' : 'status-error'"
                >
                  {{ !nv.trangThaiXoa ? "Đang hoạt động" : "Ngừng hoạt động" }}
                </span>
              </td>

              <td class="text-center">
                <div class="action-cell">
                  <button
                    class="btn-action-icon"
                    @click="goEdit(nv.id)"
                    v-tooltip="'Chỉnh sửa'"
                  >
                    <img :src="iconEdit" class="img-icon-action" />
                  </button>

                  <label
                    class="switch"
                    :class="{ 'switch-disabled': laCungQuanLy(nv) }"
                    v-tooltip="
                      laCungQuanLy(nv)
                        ? 'Không thể đổi trạng thái quản lý khác'
                        : 'Đổi trạng thái'
                    "
                  >
                    <input
                      type="checkbox"
                      :checked="!nv.trangThaiXoa"
                      :disabled="laCungQuanLy(nv)"
                      @change="doiTrangThai(nv)"
                    />
                    <span class="slider"></span>
                  </label>
                </div>
              </td>
            </tr>
            <tr v-if="danhSachHienThi.length === 0">
              <td colspan="9" class="empty-table">
                🔍 Không tìm thấy nhân viên phù hợp
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
          Hiển thị {{ danhSachHienThi.length }} / {{ totalItems }} nhân viên
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

/* ===== HEADER ===== */

/* Chỉ cuộn nội dung bảng, giữ cố định phân trang */
.table-content {
  flex: 1;
  overflow-y: visible;
  margin-bottom: 12px;
}

/* ================== HEADER FLEX ĐỒNG BỘ ================== */
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

/* Font chữ to 32px, đậm 500 chuẩn form */
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
  padding: 0 0 10px 0;
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 12px;
}
.filter-item {
  flex: 1;
  min-width: 200px;
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
  font-family: inherit;
  color: #374151;
}
.custom-input:hover {
  border-color: #c8d0dc;
}
.custom-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12);
}
select.custom-input {
  cursor: pointer;
}

/* ===== HEADER ACTIONS ĐỒNG BỘ ===== */
.header-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.add-btn,
.excel-btn,
.refresh-btn,
.download-btn,
.import-btn {
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
.refresh-btn img,
.download-btn img,
.import-btn img {
  width: 16px;
  height: 16px;
  object-fit: contain;
}

/* Nút chuyên biệt */
.add-btn {
  background: #eef2ff;
  color: #d73737;
}
.add-btn:hover {
  background: #dfe7ff;
  transform: translateY(-1px);
}

.download-btn {
  background: #fdf2f8;
  color: #d73737;
}
.download-btn:hover {
  background: #fce7f3;
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

.refresh-btn {
  background: #f3f4f6;
  color: #d73737;
}
.refresh-btn:hover {
  background: #e5e7eb;
  transform: translateY(-1px);
}

/* ===== BẢNG CHÍNH ĐỒNG BỘ ===== */
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
  font-size: 18px;
  border-bottom: none;
}
.table-main td {
  border-bottom: 1px solid #f0f0f0;
  font-size: 16px;
}

/* Ép cứng độ rộng các cột */
.table-main th:nth-child(1),
.table-main td:nth-child(1) {
  width: 40px;
  padding-right: 0;
}
.table-main th:nth-child(2),
.table-main td:nth-child(2) {
  width: 50px;
}
.table-main th:nth-child(3),
.table-main td:nth-child(3) {
  width: 170px;
} /* Nhân viên */
.table-main th:nth-child(4),
.table-main td:nth-child(4) {
  width: 100px;
} /* Chức vụ */
.table-main th:nth-child(5),
.table-main td:nth-child(5) {
  width: 80px;
} /* Giới tính */
.table-main th:nth-child(6),
.table-main td:nth-child(6) {
  width: 230px;
  overflow: hidden;
} /* Liên hệ */
.table-main th:nth-child(7),
.table-main td:nth-child(7) {
  width: 250px;
  white-space: normal;
  text-align: left;
} /* Địa chỉ */
.table-main th:nth-child(8),
.table-main td:nth-child(8) {
  width: 140px;
} /* Trạng thái */
.table-main th:nth-child(9),
.table-main td:nth-child(9) {
  width: 100px;
} /* Thao tác */

.empty-table {
  text-align: center;
  color: #6b7280;
  padding: 20px;
  font-style: italic;
}
.text-bold {
  font-weight: 600;
}

/* Avatar + Tên */
.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.avatar-img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #e5e7eb;
}
.avatar-placeholder {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f1f5f9;
  color: #94a3b8;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Badge Chức vụ */
.role-badge {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
}
.role-admin {
  background: #e0e7ff;
  color: #4338ca;
}
.role-staff {
  background: #f3f4f6;
  color: #4b5563;
}

/* Liên hệ (Email + SĐT) */
.contact-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.contact-line {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #4b5563;
  max-width: 260px;
}
.contact-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.me-1 {
  margin-right: 6px;
}
.text-primary {
  color: #0d6efd !important;
}
.text-danger {
  color: #dc3545 !important;
}

/* Địa chỉ (2 dòng: đường/phường + tỉnh) */
.address-cell {
  display: flex;
  flex-direction: column;
  gap: 3px;
  text-align: left;
}
.addr-line1 {
  display: block;
  color: #374151;
  font-size: 15px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}
.addr-line2 {
  display: block;
  color: #9ca3af;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

/* CHECKBOX CHUẨN ĐỎ */
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

/* TRẠNG THÁI (PILL NỀN MỜ) */
.status-text {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 125px;
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
  gap: 10px;
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
.switch-disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.switch-disabled .slider {
  cursor: not-allowed;
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
</style>
