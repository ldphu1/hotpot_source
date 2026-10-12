<script setup>
import { reactive, ref, computed, onMounted, onUnmounted, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { getAllDanhMuc } from "@/api/admin/danhmuc/danhMucApi";
import { connectWebSocket, disconnectWebSocket } from "@/utils/websocket";

/* ===== ICON ===== */
import iconAdd from "@/assets/icons/add.png";
import iconExcel from "@/assets/icons/excel.png";
import iconEdit from "@/assets/icons/edit.png";
import iconFolder from "@/assets/icons/danh-muc.png";
import iconView from "@/assets/icons/view.png";
import iconRefresh from "@/assets/icons/refresh.png";
import iconDanhSach from "@/assets/icons/danh-sach.png";

import { useUtils } from "@/utils/helper";
import {
  chuyenTrangThai,
  deTailSanPham,
  exportSanPham,
  getAllSanPham,
  getSanPhamByDanhMuc,
  searchSanPham,
} from "@/api/admin/sanpham/sanPhamApi";
import { getAllDinhLuong } from "@/api/admin/thuoctinh/dinhLuongApi";

// SEARCH && SELECT 2
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";

const trangThai = ref("");
const keyword = ref("");
const sortGia = ref("");
const minPrice = ref("");
const maxPrice = ref("");

const { notify, nprogress } = useUtils();
const ask = (message) => window.confirm(message);
const router = useRouter();
const route = useRoute();
const isEdit = ref(false);
const editingId = ref(null);
const selectedDanhMucId = ref("");
const idDanhMuc = computed(() => route.params.idDanhMuc);

watch(
  () => idDanhMuc.value,
  () => {
    currentPage.value = 1;
    loadDanhSach();
  },
);

// Danh sách dữ liệu
const danhSach = ref([]);
const danhSachDanhMucCha = ref([]);
const danhSachDinhLuong = ref([]);

const handleWsMessage = (msg) => {
  if (msg?.type === "SAN_PHAM_UPDATE" || msg?.type === "DANH_MUC_UPDATE") {
    if (
      keyword.value ||
      trangThai.value !== "" ||
      selectedDanhMucId.value ||
      sortGia.value ||
      minPrice.value ||
      maxPrice.value
    ) {
      filterData();
    } else {
      loadDanhSach();
      loadDanhMucCha();
    }
  }
};

// HÀM GỌI
onMounted(() => {
  loadDanhSach();
  loadDanhMucCha();
  loadDinhLuong();
  connectWebSocket(null, handleWsMessage);
});

onUnmounted(() => {
  disconnectWebSocket(handleWsMessage);
});

// Chi tiết sản phẩm
const selectedItem = ref({
  id: null,
  idDanhMuc: null,
  maSanPham: "",
  tenSanPham: "",
  gia: null,
  idDinhLuong: null,
  idDonVi: null,
  giaTri: null,
  tenDonVi: "",
  moTa: "",
  hinhAnh: "",
  trangThaiXoa: null,
});

const xemChiTiet = async (item) => {
  nprogress.start();
  try {
    const res = await deTailSanPham(item.id);
    const data = res.data;

    selectedItem.value = {
      id: data.id,
      maSanPham: data.maSanPham,
      tenSanPham: data.tenSanPham,
      idDanhMuc: data.idDanhMuc,
      tenDanhMuc: data.tenDanhMuc,
      gia: data.gia,
      idDinhLuong: data.idDinhLuong,
      idDonVi: data.idDonVi,
      giaTri: data.giaTri,
      tenDonVi: data.tenDonVi,
      moTa: data.moTa,
      trangThaiXoa: data.trangThaiXoa,
      hinhAnh: data.hinhAnh,
    };
    showDetail.value = true;
  } catch (err) {
    console.error("Lỗi lấy chi tiết đồ uống", err);
    notify.error("Không thể tải chi tiết sản phẩm");
  } finally {
    nprogress.done();
  }
};

const handleExportSanPham = async () => {
  try {
    const params = {
      keyword: keyword.value || null,
      idDanhMuc:
        selectedDanhMucId.value === "" ? null : selectedDanhMucId.value,
      trangThaiXoa: trangThai.value === "" ? null : Number(trangThai.value),
      minPrice: minPrice.value || null,
      maxPrice: maxPrice.value || null,
      sortGia: sortGia.value || null,
    };

    const res = await exportSanPham(params);

    const url = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute(
      "download",
      `san_pham_${new Date().toISOString().slice(0, 10)}.xlsx`,
    );
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
    notify.success("Xuất excel thành công");
  } catch (e) {
    console.error("Lỗi export sản phẩm:", e);
  }
};

/* ===== MODAL CHI TIẾT ===== */
const showDetail = ref(false);

const closeDetail = () => {
  showDetail.value = false;
  selectedItem.value = {
    id: null,
    idDanhMuc: null,
    maSanPham: "",
    tenSanPham: "",
    gia: null,
    idDinhLuong: null,
    idDonVi: null,
    giaTri: null,
    tenDonVi: "",
    moTa: "",
    hinhAnh: "",
    trangThaiXoa: null,
  };
};

// Hàm load danh sách sản phẩm
const loadDanhSach = async () => {
  try {
    if (idDanhMuc.value) {
      const res = await getSanPhamByDanhMuc(idDanhMuc.value);
      danhSach.value = res.data;
    } else {
      const res = await getAllSanPham();
      danhSach.value = res.data;
    }
  } catch (error) {
    console.log("Không tìm thấy sản phẩm nào", error);
  }
};

const resetFilter = async () => {
  keyword.value = "";
  selectedDanhMucId.value = "";
  trangThai.value = "";
  sortGia.value = "";
  minPrice.value = "";
  maxPrice.value = "";

  currentPage.value = 1;

  await loadDanhSach();
};

// Chuyen sang trang sua san pham
const suaSanPham = (id) => {
  router.push(`/admin/san-pham/update/${id}`);
};

// Load danh mục cha
const loadDanhMucCha = async () => {
  try {
    const res = await getAllDanhMuc();
    danhSachDanhMucCha.value = res.data;
  } catch (error) {
    console.log("Không tìm thấy danh mục cha", error);
  }
};

// Lấy danh sách định lượng
const loadDinhLuong = async () => {
  try {
    const res = await getAllDinhLuong();
    danhSachDinhLuong.value = res.data;
  } catch (error) {
    console.log("Không tìm thấy định lượng", error);
  }
};

const danhSachDonVi = computed(() => {
  const map = new Map();
  danhSachDinhLuong.value.forEach((dl) => {
    if (!map.has(dl.idDonVi)) {
      map.set(dl.idDonVi, {
        idDonVi: dl.idDonVi,
        tenDonVi: dl.tenDonVi,
      });
    }
  });
  return Array.from(map.values());
});

const danhSachDinhLuongTheoDonVi = computed(() => {
  if (!selectedItem.value.idDonVi) return danhSachDinhLuong.value;
  return danhSachDinhLuong.value.filter(
    (dl) => dl.idDonVi === selectedItem.value.idDonVi,
  );
});

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

/* ===== MODAL Thêm danh mục chi tiết ===== */
const showModal = ref(false);

const formDanhMucChiTiet = reactive({
  id: null,
  tenDanhMucChiTiet: "",
  idDanhMucCha: "",
});

const addMonAn = () => {
  router.push("/admin/san-pham/save");
};

// /* ===== SUBMIT ===== */
const submitAdd = async () => {
  if (!(await notify.confirm("Bạn có muốn thêm danh mục chi tiết?"))) return;
  nprogress.start();
  try {
    const payload = {
      idDanhMuc: formDanhMucChiTiet.idDanhMucCha,
      tenDanhMucChiTiet: formDanhMucChiTiet.tenDanhMucChiTiet,
    };
    const res = await addDanhMucChiTiet(payload);
    notify.success(res.data.message || "Thêm danh mục chi tiết thành công");
    await loadDanhSach();
    closeModal();
    resetForm();
  } catch (error) {
    notify.error(error.response?.data?.message || "Có lỗi xảy ra");
  } finally {
    nprogress.done();
  }
};

const submitUpdate = async () => {
  if (!(await notify.confirm("Bạn có muốn cập nhật danh mục chi tiết?")))
    return;
  nprogress.start();
  try {
    const payload = {
      idDanhMuc: formDanhMucChiTiet.idDanhMucCha,
      tenDanhMucChiTiet: formDanhMucChiTiet.tenDanhMucChiTiet,
    };
    const res = await updateDanhMucChiTiet(editingId.value, payload);
    notify.success(res.data.message || "Thêm danh mục chi tiết thành công");
    await loadDanhSach();
    closeModal();
    resetForm();
  } catch (error) {
    notify.error(error.response?.data?.message || "Có lỗi xảy ra");
  } finally {
    nprogress.done();
  }
};

const updateTrangThai = async (id) => {
  if (!(await notify.confirm("Bạn có muốn chuyển trạng thái?"))) return;
  nprogress.start();
  try {
    await chuyenTrangThai(id);
    notify.success("Chuyển trạng thái thành công");
    await loadDanhSach();
  } catch (e) {
    notify.error(e.response?.data || "Có lỗi xảy ra");
  }
  nprogress.done();
};

const filterSanPham = async () => {
  try {
    const params = {
      keyword: keyword.value || null,
      idDanhMuc:
        selectedDanhMucId.value === "" ? null : selectedDanhMucId.value,
      trangThaiXoa: trangThai.value === "" ? null : Number(trangThai.value),
      minPrice: minPrice.value || null,
      maxPrice: maxPrice.value || null,
      sortGia: sortGia.value || null,
    };
    const res = await searchSanPham(params);
    danhSach.value = res.data;
    currentPage.value = 1;
  } catch (error) {
    console.error("Lỗi search sản phẩm:", error);
  }
};

const resetForm = () => {
  formDanhMucChiTiet.id = null;
  formDanhMucChiTiet.tenDanhMucChiTiet = "";
  formDanhMucChiTiet.idDanhMucCha = idDanhMuc.value;
  isEdit.value = false;
  editingId.value = null;
};

const closeModal = () => {
  showModal.value = false;
  resetForm();
};

watch(
  () => selectedItem.value.idDinhLuong,
  (newVal) => {
    if (!newVal) return;
    const dl = danhSachDinhLuong.value.find((item) => item.id === newVal);
    if (dl) {
      selectedItem.value.idDonVi = dl.idDonVi;
    }
  },
);

/* ===== PHÂN TRANG ===== */
const currentPage = ref(1);
const pageSize = ref(5);
const totalItems = computed(() => danhSach.value.length);
const totalPages = computed(
  () => Math.ceil(totalItems.value / pageSize.value) || 1,
);

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
  <div class="quan-ly-san-pham">
    <div class="box">
      <div class="box-header">
        <h2 class="main-title">
          <span class="icon-wrapper">
            <img src="@/assets/icons/monan.png" alt="Món ăn" />
          </span>
          Quản Lý Món Ăn
        </h2>
        <p>Quản lý các món ăn trong hệ thống</p>
      </div>
    </div>

    <div class="filter-box">
      <div class="filter-item">
        <label>Tìm sản phẩm</label>
        <input
          class="custom-input"
          placeholder="Tên sản phẩm..."
          v-model="keyword"
          @input="filterSanPham"
        />
      </div>

      <div class="filter-item">
        <label>Danh mục</label>
        <v-select
          class="modern-select"
          v-model="selectedDanhMucId"
          :options="[{ tenDanhMuc: 'Tất cả', id: '' }, ...danhSachDanhMucCha]"
          label="tenDanhMuc"
          :reduce="(dm) => dm.id"
          placeholder="Tất cả danh mục"
          :clearable="false"
          @update:modelValue="filterSanPham"
        >
          <template #no-options="{ search, searching }">
            <template v-if="searching"
              >Không tìm thấy "<b>{{ search }}</b
              >"</template
            >
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
            { label: 'Đang bán', value: 0 },
            { label: 'Ngừng bán', value: 1 },
          ]"
          :reduce="(opt) => opt.value"
          placeholder="Tất cả trạng thái"
          @update:modelValue="filterSanPham"
          :clearable="false"
        >
        </v-select>
      </div>

      <div class="filter-item">
        <label>Sắp xếp giá</label>
        <v-select
          class="modern-select"
          v-model="sortGia"
          :options="[
            { label: 'Mặc định', value: '' },
            { label: 'Giá tăng dần', value: 'asc' },
            { label: 'Giá giảm dần', value: 'desc' },
          ]"
          :reduce="(opt) => opt.value"
          placeholder="Sắp xếp"
          @update:modelValue="filterSanPham"
          :clearable="false"
        >
        </v-select>
      </div>

      <div class="filter-item">
        <label>Giá từ</label>
        <input
          class="custom-input"
          type="number"
          placeholder="0 đ"
          v-model="minPrice"
          @input="filterSanPham"
        />
      </div>

      <div class="filter-item">
        <label>Đến</label>
        <input
          class="custom-input"
          type="number"
          placeholder="Max đ"
          v-model="maxPrice"
          @input="filterSanPham"
        />
      </div>
    </div>

    <div class="box">
      <div class="box-header box-header-flex">
        <h3 class="table-title">
          <span class="icon-wrapper-small">
            <img :src="iconDanhSach" class="icon-title-small" />
          </span>
          Danh sách chi tiết
        </h3>

        <div class="header-actions">
          <button class="add-btn" @click="addMonAn()">
            <img :src="iconAdd" alt="" />
            <span>Thêm món ăn</span>
          </button>
          <button
            class="excel-btn"
            data-tooltip="Xuất Excel"
            @click="handleExportSanPham()"
          >
            <img :src="iconExcel" alt="" />
            <span>Xuất Excel</span>
          </button>
          <button class="refresh-btn" @click="resetFilter()">
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
              <th style="text-align: left">Danh mục</th>
              <th style="text-align: left">Mã sản phẩm</th>
              <th style="text-align: left">Tên sản phẩm</th>
              <th style="text-align: left">Giá bán</th>
              <th style="text-align: left">Định lượng</th>
              <th style="text-align: left">Đơn vị</th>
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
              <td style="text-align: left">
                <div class="dm-name">
                  <img :src="iconFolder" class="dm-icon" />
                  <span>{{ item.tenDanhMuc }}</span>
                </div>
              </td>
              <td class="text-bold" style="text-align: left">
                {{ item.maSanPham }}
              </td>
              <td style="text-align: left" class="text-bold">
                {{ item.tenSanPham }}
              </td>
              <td style="text-align: left" class="text-bold">
                {{ item.gia?.toLocaleString() }} đ
              </td>
              <td style="text-align: left" class="text-bold">
                {{ item.giaTri }}
              </td>
              <td style="text-align: left" class="text-bold">
                {{ item.tenDonVi }}
              </td>
              <td class="desc" style="text-align: left" :title="item.moTa">
                {{ item.moTa }}
              </td>
              <td>
                <span
                  class="status-text"
                  :class="item.trangThaiXoa ? 'status-error' : 'status-success'"
                >
                  {{ item.trangThaiXoa ? "Ngưng bán" : "Còn bán" }}
                </span>
              </td>
              <td class="text-center">
                <div class="action-cell">
                  <button
                    class="btn-action-icon"
                    :class="{ disabled: item.trangThaiXoa }"
                    @click="!item.trangThaiXoa && suaSanPham(item.id)"
                    v-tooltip="item.trangThaiXoa ? '' : 'Chỉnh sửa'"
                  >
                    <img :src="iconEdit" class="img-icon-action" />
                  </button>

                  <button
                    class="btn-action-icon"
                    :class="{ disabled: item.trangThaiXoa }"
                    @click="xemChiTiet(item)"
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
              <td colspan="11" class="empty-table">
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
          Hiển thị {{ danhSachHienThi.length }} / {{ totalItems }} bản ghi
        </div>
      </div>
    </div>

    <Transition name="fade">
      <Teleport to="body">
        <div v-if="showDetail" class="modal-overlay" @click.self="closeDetail">
          <div class="modal-box modal-wide">
            <div class="modal-header">
              <h3 class="modal-title-left">
                <div class="icon-box-wrapper-mini">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="22"
                    height="22"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="#d73737"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path
                      d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"
                    />
                  </svg>
                </div>
                <div>
                  Cập nhật thông tin món ăn
                  <span class="subtitle-mini"
                    >Chỉnh sửa chi tiết sản phẩm trên hệ thống</span
                  >
                </div>
              </h3>
              <button class="btn-icon-close" @click="closeDetail">×</button>
            </div>

            <div class="modal-body" v-if="selectedItem">
              <div class="detail-grid-layout">
                <div class="detail-aside">
                  <div class="avatar-wrapper-modern">
                    <img
                      v-if="selectedItem?.hinhAnh"
                      :src="`http://localhost:8080/uploads/${selectedItem.hinhAnh}`"
                      alt="Product"
                    />
                    <div v-else class="placeholder-img">Chưa có ảnh</div>
                  </div>
                  <div
                    class="status-badge-modern"
                    :class="
                      selectedItem.trangThaiXoa ? 'status-off' : 'status-on'
                    "
                  >
                    <span class="dot"></span>
                    {{
                      selectedItem.trangThaiXoa
                        ? "Ngưng bán"
                        : "Đang kinh doanh"
                    }}
                  </div>
                </div>

                <div class="detail-main-form">
                  <div class="form-row">
                    <div class="grid-item">
                      <label
                        >Tên món ăn <span class="text-danger">*</span></label
                      >
                      <input
                        v-model="selectedItem.tenSanPham"
                        class="custom-input"
                        placeholder="Nhập tên món ăn..."
                      />
                    </div>
                  </div>

                  <div class="form-row grid-2 mt-3">
                    <div class="grid-item">
                      <label>Danh mục</label>
                      <v-select
                        class="modern-select"
                        v-model="selectedItem.idDanhMuc"
                        :options="danhSachDanhMucCha"
                        label="tenDanhMuc"
                        :reduce="(dm) => dm.id"
                        placeholder="Chọn danh mục"
                      ></v-select>
                    </div>
                    <div class="grid-item">
                      <label>Giá bán (VNĐ)</label>
                      <input
                        type="number"
                        v-model="selectedItem.gia"
                        class="custom-input price-text"
                      />
                    </div>
                  </div>

                  <div class="form-row grid-2 mt-3">
                    <div class="grid-item">
                      <label>Đơn vị</label>
                      <v-select
                        class="modern-select"
                        v-model.number="selectedItem.idDonVi"
                        :options="danhSachDonVi"
                        label="tenDonVi"
                        :reduce="(dv) => dv.idDonVi"
                        placeholder="Chọn đơn vị"
                      ></v-select>
                    </div>
                    <div class="grid-item">
                      <label>Định lượng (Size)</label>
                      <v-select
                        class="modern-select"
                        v-model.number="selectedItem.idDinhLuong"
                        :options="danhSachDinhLuongTheoDonVi"
                        label="giaTri"
                        :reduce="(dl) => dl.id"
                        placeholder="Chọn định lượng"
                      ></v-select>
                    </div>
                  </div>

                  <div class="form-row mt-3">
                    <div class="grid-item">
                      <label>Mô tả đồ uống</label>
                      <textarea
                        v-model="selectedItem.moTa"
                        rows="3"
                        class="custom-input textarea"
                        placeholder="Mô tả ngắn gọn..."
                      ></textarea>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="modal-footer">
              <p class="footer-tip">
                * Lưu ý: Cập nhật thông tin sẽ thay đổi dữ liệu hiện tại
              </p>
              <div class="footer-actions">
                <button class="btn-cancel" @click="closeDetail">Hủy bỏ</button>
                <button
                  class="btn-primary"
                  @click="suaSanPham(selectedItem.id)"
                >
                  <i class="bi bi-check2-all"></i>
                  <div class="btn-light-sweep"></div>
                  Cập nhật thông tin
                </button>
              </div>
            </div>
          </div>
        </div>
      </Teleport>
    </Transition>

    <Transition name="fade">
      <Teleport to="body">
        <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
          <div class="modal-box" style="width: 500px">
            <div class="modal-header">
              <h3>
                {{
                  isEdit
                    ? "Cập nhật danh mục chi tiết"
                    : "Thêm danh mục chi tiết"
                }}
              </h3>
              <button class="btn-icon-close" @click="closeModal">×</button>
            </div>
            <div class="modal-body">
              <div class="grid-item mb-3">
                <label>Thuộc danh mục <span class="text-danger">*</span></label>
                <v-select
                  class="modern-select"
                  v-model="formDanhMucChiTiet.idDanhMucCha"
                  :options="danhSachDanhMucCha"
                  label="tenDanhMuc"
                  :reduce="(dm) => dm.id"
                  placeholder="Chọn danh mục"
                ></v-select>
              </div>
              <div class="grid-item">
                <label>Tên chi tiết <span class="text-danger">*</span></label>
                <input
                  v-model="formDanhMucChiTiet.tenDanhMucChiTiet"
                  class="custom-input"
                  placeholder="Nhập tên danh mục chi tiết..."
                />
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn-cancel" @click="closeModal">Hủy</button>
              <button
                class="btn-primary"
                @click="isEdit ? submitUpdate() : submitAdd()"
              >
                Xác nhận
              </button>
            </div>
          </div>
        </div>
      </Teleport>
    </Transition>
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
  min-width: 130px;
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
}
.custom-input:hover {
  border-color: #c8d0dc;
}
.custom-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12);
}
.custom-input.textarea {
  height: auto;
  padding: 12px;
  resize: vertical;
  min-height: 80px;
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

/* Ép cứng Cột 1 và 2 */
.table-main th:nth-child(1),
.table-main td:nth-child(1) {
  width: 40px;
  text-align: center;
  padding-right: 0;
}
.table-main th:nth-child(2),
.table-main td:nth-child(2) {
  width: 50px;
  text-align: center;
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
.text-danger {
  color: #d73737;
}

/* Cột Danh mục */
.dm-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #333;
}
.dm-icon {
  width: 18px;
  height: 18px;
  object-fit: contain;
}

/* MÔ TẢ (Giới hạn text) */
.desc {
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #666;
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

/* TRẠNG THÁI (PILL NỀN MỜ) */
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

/* ===== V-SELECT CUSTOM ĐỒNG BỘ ===== */
:deep(.v-select) {
  background: #fff;
  width: 100%;
}
:deep(.vs__dropdown-toggle) {
  border: 1px solid #e5e7eb !important;
  border-radius: 8px !important;
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
}
:deep(.vs__selected) {
  position: absolute;
  margin: 0 !important;
  padding-left: 12px !important;
  color: #374151;
}
:deep(.vs__dropdown-toggle:hover) {
  border-color: #c8d0dc !important;
}
:deep(.vs--open .vs__dropdown-toggle) {
  border-color: var(--color-primary) !important;
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12) !important;
}
:deep(.vs__search::placeholder) {
  color: #9ca3af !important;
}
:deep(.vs__dropdown-menu) {
  border: 1px solid #e5e7eb !important;
  border-radius: 8px !important;
  margin-top: 8px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1) !important;
  padding: 6px !important;
  z-index: 1000;
}
:deep(.vs__dropdown-option) {
  border-radius: 6px !important;
  padding: 8px 12px !important;
  margin-bottom: 2px !important;
}
:deep(.vs__dropdown-option--highlight) {
  background: #f3f4f6 !important;
  color: #d73737 !important;
}
:deep(.vs__dropdown-option--selected) {
  background: #f3f5f9 !important;
  color: #d73737 !important;
}
:deep(.vs__actions) {
  padding-right: 10px !important;
}
:deep(.vs__open-indicator),
:deep(.vs__clear) {
  fill: #9ca3af !important;
}

/* ===== MODAL DETAIL CHUẨN ===== */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999999;
}
.modal-box {
  background: white;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.2);
}
.modal-wide {
  width: 900px;
  max-width: 95%;
}

.modal-header {
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.modal-title-left {
  display: flex;
  align-items: center;
  gap: 14px;
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}
.icon-box-wrapper-mini {
  background: #fff1f1;
  width: 45px;
  height: 45px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
}
.subtitle-mini {
  display: block;
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
  margin-top: 4px;
}
.btn-icon-close {
  background: none;
  border: none;
  font-size: 28px;
  color: #94a3b8;
  cursor: pointer;
  transition: 0.2s;
}
.btn-icon-close:hover {
  color: #d73737;
}

.modal-body {
  padding: 24px;
  max-height: 70vh;
  overflow-y: auto;
  background: #fcfcfc;
}
.detail-grid-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 30px;
}

/* Cột Ảnh */
.avatar-wrapper-modern {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  background: #fff;
}
.avatar-wrapper-modern img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.placeholder-img {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  color: #94a3b8;
  font-size: 14px;
}
.status-badge-modern {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 13px;
}
.status-on {
  background: #f0fdf4;
  color: #16a34a;
  border: 1px solid #bbf7d0;
}
.status-off {
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
}

/* Form Field */
.detail-main-form {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}
.form-row {
  margin-bottom: 20px;
}
.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
.grid-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.grid-item label {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
}
.price-text {
  color: #d73737;
  font-weight: 700;
}
.mt-3 {
  margin-top: 1.5rem;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #e2e8f0;
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.footer-tip {
  font-size: 13px;
  color: #94a3b8;
  font-style: italic;
  margin: 0;
}
.footer-actions {
  display: flex;
  gap: 12px;
}

.btn-cancel {
  padding: 10px 24px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  cursor: pointer;
  transition: 0.2s;
}
.btn-cancel:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}
.btn-primary {
  position: relative;
  overflow: hidden;
  background: #d73737;
  color: #fff;
  border: none;
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}
.btn-primary:hover {
  background: #b92b2b;
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
.btn-primary:hover .btn-light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
