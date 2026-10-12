<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import { useUtils } from "@/utils/helper";
import {
  doiTrangThaiKhachHang,
  getAllKhachHang,
  exportSelectedKhachHang,
} from "@/api/admin/khachhang/khachhangApi";
import { getKhachHangDetail } from "@/api/admin/khachhang/khachhangApi";
import DiaChiKhachHangModal from "./DiaChiKhachHangModal.vue";

/* ===== ICON ===== */
import iconAdd from "@/assets/icons/add.png";
import iconEdit from "@/assets/icons/edit.png";
import iconExcel from "@/assets/icons/excel.png";
import iconRefresh from "@/assets/icons/refresh.png";
import iconDanhSach from "@/assets/icons/danh-sach.png";
import iconEye from "@/assets/icons/eye.png";
import { hasPermission } from "@/utils/permission.js";

const { notify, nprogress } = useUtils();
const router = useRouter();

// ===== EXPORT EXCEL =====
const exportSelected = async () => {
  if (selectedIds.value.length === 0) {
    notify.error("Vui lòng chọn ít nhất 1 khách hàng để xuất Excel");
    return;
  }

  try {
    nprogress.start();
    const res = await exportSelectedKhachHang(selectedIds.value);
    const blob = new Blob([res.data], {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    });

    const url = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "khach-hang.xlsx";
    a.click();
    window.URL.revokeObjectURL(url);

    nprogress.done();
    notify.success("Xuất Excel thành công");
  } catch (e) {
    notify.error("Xuất Excel thất bại");
  }
};

// ===== STATE & DATA =====
const selectedIds = ref([]);

const danhSachGoc = ref([]);
const danhSach = ref([]);
const filters = ref({
  keyword: "",
  contact: "",
  ngaySinh: "",
  trangThai: "",
});

const searchKhachHang = () => {
  const k = filters.value;
  const kw = (k.keyword || "").trim().replace(/\s+/g, " ").toLowerCase();
  const contact = (k.contact || "").trim().replace(/\s+/g, " ");

  danhSach.value = danhSachGoc.value.filter((kh) => {
    const tenKhachHang = (kh.tenKhachHang || "")
      .trim()
      .replace(/\s+/g, " ")
      .toLowerCase();
    const email = (kh.email || "").trim().replace(/\s+/g, " ");
    const soDienThoai = (kh.soDienThoai || "").trim().replace(/\s+/g, " ");

    return (
      (!kw || tenKhachHang.includes(kw)) &&
      (!contact || email.includes(contact) || soDienThoai.includes(contact)) &&
      (!k.ngaySinh || kh.ngaySinh?.slice(0, 10) === k.ngaySinh) &&
      (k.trangThai === "" || kh.trangThai == k.trangThai)
    );
  });
};

watch(
  filters,
  () => {
    currentPage.value = 1;
    searchKhachHang();
  },
  { deep: true },
);

onMounted(async () => {
  try {
    const res = await getAllKhachHang();
    danhSach.value = res.data.sort((a, b) => b.id - a.id);
    danhSachGoc.value = [...danhSach.value];
  } catch (error) {
    console.error("Không tìm thấy khách hàng", error);
  }
});

const reloadKhachHang = async () => {
  try {
    const res = await getAllKhachHang();
    danhSach.value = res.data.sort((a, b) => b.id - a.id);
    danhSachGoc.value = [...danhSach.value];

    filters.value = { keyword: "", contact: "", ngaySinh: "", trangThai: "" };
    currentPage.value = 1;
    selectedIds.value = [];
  } catch (e) {
    console.error(e);
  }
};

// ===== NAVIGATION & ACTIONS =====
const goToEditKhachHang = (id) => {
  router.push(`/admin/khach-hang/sua/${id}`);
};

const goToAddKhachHang = () => {
  router.push("/admin/khach-hang/save");
};
const goToDetailKhachHang = (id) => {
  router.push(`/admin/khach-hang/${id}`);
};
const toggleTrangThai = async (kh) => {
  if (!hasPermission("SUA_KHACH_HANG")) {
    router.push("/403");
    return;
  }
  const isActive = kh.trangThai === 1;
  const ok = await notify.confirm(
    isActive
      ? "Bạn có chắc muốn ngừng hoạt động khách hàng này?"
      : "Bạn có chắc muốn kích hoạt khách hàng này?",
  );
  if (!ok) return;

  try {
    nprogress.start();
    await doiTrangThaiKhachHang(kh.id);
    kh.trangThai = isActive ? 0 : 1;
    notify.success(
      isActive ? "Đã ngừng hoạt động khách hàng" : "Đã kích hoạt khách hàng",
    );
  } catch (e) {
    notify.error("Đổi trạng thái thất bại");
  } finally {
    nprogress.done();
  }
};

const getDiaChiMacDinh = (kh) => {
  if (!kh.diaChiList || kh.diaChiList.length === 0) return "—";
  let dc = kh.diaChiList.find((d) => d.macDinh);
  if (!dc) dc = kh.diaChiList[0];
  return `${dc.diaChiChiTiet}, ${dc.xaPhuong}, ${dc.tinhThanh}`;
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

// format đ/mm/yyyy
const formatDateVN = (dateStr) => {
  if (!dateStr || !dateStr.includes("-")) return "—";
  const [y, m, d] = dateStr.split("-");
  return `${d}/${m}/${y}`;
};

/* ===== PHÂN TRANG ===== */
const currentPage = ref(1);
const pageSize = ref(5);
const totalItems = computed(() => danhSach.value.length);
const totalPages = computed(
  () => Math.ceil(totalItems.value / pageSize.value) || 1,
);

const danhSachHienThi = computed(() => {
  const sorted = [...danhSach.value].sort((a, b) => b.id - a.id);
  const start = (currentPage.value - 1) * pageSize.value;
  return sorted.slice(start, start + pageSize.value);
});

const isAllSelected = computed({
  get() {
    return (
      danhSachHienThi.value.length > 0 &&
      danhSachHienThi.value.every((k) => selectedIds.value.includes(k.id))
    );
  },
  set(val) {
    if (val) {
      danhSachHienThi.value.forEach((k) => {
        if (!selectedIds.value.includes(k.id)) {
          selectedIds.value.push(k.id);
        }
      });
    } else {
      const idsToRemove = danhSachHienThi.value.map((k) => k.id);
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

/* ===== MODAL ĐỊA CHỈ ===== */
const selectedKhachHang = ref(null);
const showDiaChiModal = ref(false);
const selectedKhId = ref(null);

const openDiaChiModal = (kh) => {
  selectedKhId.value = kh.id;
  selectedKhachHang.value = kh;
  showDiaChiModal.value = true;
};
const closeDiaChiModal = () => {
  showDiaChiModal.value = false;
};
</script>

<template>
  <div class="quan-ly-khach-hang">
    <div class="box">
      <div class="box-header">
        <h2 class="main-title">
          <span class="icon-wrapper">
            <img src="@/assets/icons/khachhang.png" alt="Khách hàng" />
          </span>
          Quản Lý Khách Hàng
        </h2>
        <p>Quản lý thông tin tài khoản và địa chỉ nhận hàng</p>
      </div>
    </div>

    <div class="filter-box">
      <div class="filter-item">
        <label>Họ và tên</label>
        <input
          class="custom-input"
          placeholder="Nhập tên khách hàng..."
          v-model="filters.keyword"
        />
      </div>

      <div class="filter-item">
        <label>Số điện thoại / Email</label>
        <input
          class="custom-input"
          placeholder="Nhập SĐT hoặc email..."
          v-model="filters.contact"
        />
      </div>

      <div class="filter-item">
        <label>Ngày sinh</label>
        <input class="custom-input" type="date" v-model="filters.ngaySinh" />
      </div>

      <div class="filter-item">
        <label>Trạng thái</label>
        <select class="custom-input" v-model="filters.trangThai">
          <option value="">Tất cả trạng thái</option>
          <option value="1">Đang hoạt động</option>
          <option value="0">Ngừng hoạt động</option>
        </select>
      </div>
    </div>

    <div class="box">
      <div class="box-header box-header-flex">
        <h3 class="table-title">
          <span class="icon-wrapper-small">
            <img :src="iconDanhSach" class="icon-title-small" />
          </span>
          Danh sách khách hàng
        </h3>

        <div class="header-actions">
          <button class="add-btn" @click="goToAddKhachHang">
            <img :src="iconAdd" alt="" />
            <span>Thêm khách hàng</span>
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
            @click="reloadKhachHang"
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
              <th style="text-align: left">Khách hàng</th>
              <th style="text-align: left">Giới tính</th>
              <th style="text-align: left">Ngày sinh</th>
              <th style="text-align: left">Liên hệ</th>
              <th>Địa chỉ</th>
              <th>Trạng thái</th>
              <th style="text-align: center">Thao tác</th>
            </tr>
          </thead>

          <tbody>
            <tr
              v-for="(kh, index) in danhSachHienThi"
              :key="kh.id"
              :class="{ 'row-no-xau': kh.coNoXau }"
              v-tooltip="kh.coNoXau ? 'Khách có nợ xấu' : null"
            >
              <td>
                <input
                  type="checkbox"
                  class="custom-checkbox"
                  :value="kh.id"
                  v-model="selectedIds"
                />
              </td>
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>

              <td style="text-align: left">
                <div class="user-cell">
                  <img
                    :src="
                      kh.anhDaiDien
                        ? `http://localhost:8080${kh.anhDaiDien}`
                        : '/default-avatar.png'
                    "
                    class="avatar-img"
                    alt="avatar"
                  />
                  <span class="text-bold">{{ kh.tenKhachHang }}</span>
                </div>
              </td>

              <td style="text-align: left">
                {{
                  kh.gioiTinh === null || kh.gioiTinh === undefined
                    ? "—"
                    : kh.gioiTinh
                    ? "Nam"
                    : "Nữ"
                }}
              </td>
              <td style="text-align: left">{{ formatDateVN(kh.ngaySinh) }}</td>

              <td style="text-align: left">
                <div class="contact-info">
                  <span class="contact-line" v-if="kh.email" :title="kh.email">
                    <i class="bi bi-envelope-fill text-primary me-1"></i>
                    <span class="contact-text">{{ kh.email }}</span>
                  </span>
                  <span class="contact-line" v-if="kh.soDienThoai">
                    <i class="bi bi-telephone-fill text-danger me-1"></i>
                    <span class="contact-text">{{ kh.soDienThoai }}</span>
                  </span>
                </div>
              </td>

              <td>
                <div class="address-cell" :title="getDiaChiMacDinh(kh)">
                  <span class="addr-line1">{{
                    splitDiaChi(getDiaChiMacDinh(kh)).line1
                  }}</span>
                  <span
                    class="addr-line2"
                    v-if="splitDiaChi(getDiaChiMacDinh(kh)).line2"
                  >
                    {{ splitDiaChi(getDiaChiMacDinh(kh)).line2 }}
                  </span>
                </div>
              </td>

              <td>
                <span
                  class="status-text"
                  :class="
                    kh.trangThai === 1 ? 'status-success' : 'status-error'
                  "
                >
                  {{
                    kh.trangThai === 1 ? "Đang hoạt động" : "Ngừng hoạt động"
                  }}
                </span>
              </td>

              <td class="text-center">
                <div class="action-cell">
                  <button
                    class="btn-action-icon"
                    @click="goToEditKhachHang(kh.id)"
                    v-tooltip="'Chỉnh sửa'"
                  >
                    <img :src="iconEdit" class="img-icon-action" />
                  </button>
                  <button
                    class="btn-action-icon"
                    @click="goToDetailKhachHang(kh.id)"
                    v-tooltip="'Chi tiết'"
                  >
                    <img :src="iconEye" class="img-icon-action" />
                  </button>
                  <button
                    class="btn-action-icon"
                    @click="openDiaChiModal(kh)"
                    v-tooltip="'Quản lý địa chỉ'"
                  >
                    <img
                      src="@/assets/icons/diachi.png"
                      class="img-icon-action"
                    />
                  </button>

                  <label class="switch" v-tooltip="'Đổi trạng thái'">
                    <input
                      type="checkbox"
                      :checked="kh.trangThai === 1"
                      @click.prevent="toggleTrangThai(kh)"
                    />
                    <span class="slider"></span>
                  </label>
                </div>
              </td>
            </tr>
            <tr v-if="danhSachHienThi.length === 0">
              <td colspan="9" class="empty-table">
                🔍 Không tìm thấy khách hàng phù hợp
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
          Hiển thị {{ danhSachHienThi.length }} / {{ totalItems }} khách hàng
        </div>
      </div>
    </div>

    <DiaChiKhachHangModal
      :khId="selectedKhId"
      :tenKhachHang="selectedKhachHang?.tenKhachHang"
      :isOpen="showDiaChiModal"
      @close="closeDiaChiModal"
    />
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
  font-size: 17px;
  border-bottom: none;
}
.table-main td {
  border-bottom: 1px solid #f0f0f0;
  font-size: 15px;
}

/* Ép cứng độ rộng các cột cơ bản */
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
  width: 180px;
} /* Khách hàng */
.table-main th:nth-child(4),
.table-main td:nth-child(4) {
  width: 80px;
} /* Giới tính */
.table-main th:nth-child(5),
.table-main td:nth-child(5) {
  width: 100px;
} /* Ngày sinh */
.table-main th:nth-child(6),
.table-main td:nth-child(6) {
  width: 240px;
  overflow: hidden;
} /* Liên hệ */
.table-main th:nth-child(7),
.table-main td:nth-child(7) {
  width: 220px;
  white-space: normal;
  text-align: left;
} /* Địa chỉ */
.table-main th:nth-child(8),
.table-main td:nth-child(8) {
  width: 130px;
} /* Trạng thái */
.table-main th:nth-child(9),
.table-main td:nth-child(9) {
  width: 180px;
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

/* Khách hàng (Avatar + Tên) */
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

/* Địa chỉ (2 dòng: đường/phường + tỉnh) */
.table-main td:nth-child(7) {
  white-space: normal;
  text-align: left;
}
.address-cell {
  display: flex;
  flex-direction: column;
  gap: 3px;
  text-align: left;
}
.addr-line1 {
  display: block;
  color: #374151;
  font-size: 14px;
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
  min-width: 120px;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 13px;
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
.row-no-xau td {
  font-weight: 700;
  color: #dc2626;
}

.row-no-xau .text-bold {
  color: #dc2626;
}

.row-no-xau:hover td {
  background-color: #fef2f2;
}
</style>
