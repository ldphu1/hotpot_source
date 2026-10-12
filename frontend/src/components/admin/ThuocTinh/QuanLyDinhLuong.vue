<script setup>
import { reactive, ref, watch, computed, onMounted, onUnmounted } from "vue";
import {
  addDonViTongHop,
  chuyenTrangThai,
  detaiDonVi,
  getAllDonVi,
  searchDonVi,
  updateDonVi,
} from "@/api/admin/thuoctinh/donViApi";
import { useRoute, useRouter } from "vue-router";
import { connectWebSocket, disconnectWebSocket } from "@/utils/websocket";

/* ===== ICON ===== */
import iconAdd from "@/assets/icons/add.png";
import iconExcel from "@/assets/icons/excel.png";
import iconSearch from "@/assets/icons/search.png";
import iconEdit from "@/assets/icons/edit.png";
import iconView from "@/assets/icons/view.png";
import iconRefresh from "@/assets/icons/refresh.png";
import iconDanhSach from "@/assets/icons/danh-sach.png";

import { addDanhMuc, getAllDanhMuc } from "@/api/admin/danhmuc/danhMucApi";
import { useUtils } from "@/utils/helper";

const { notify, nprogress } = useUtils();
const route = useRoute();
const router = useRouter();
const isEdit = ref(false);
const editingId = ref(null);
const selectedDanhMuc = ref(null);

// THÊM NHANH DANH MỤC
const showAddDanhMucModal = ref(false);
const formDanhMuc = reactive({
  tenDanhMuc: "",
  idDonVi: [],
});

const errorsDanhMuc = reactive({
  tenDanhMuc: "",
  idDonVi: "",
});

const donViId = ref(null);

const activeDonViList = computed(() => {
  return danhSachDonVi.value.filter((dv) => !dv.trangThaiXoa);
});

const isDonViDisabled = (id) => {
  const dv = danhSachDonVi.value.find((d) => d.id === id);
  return !dv || Boolean(dv.trangThaiXoa);
};

const getDonViName = (id) => {
  const dv = danhSachDonVi.value.find((d) => d.id === id);
  return dv ? dv.tenDonVi : `Đơn vị #${id}`;
};

const coDonViNgungHoatDongDanhMuc = computed(() => {
  if (!formDanhMuc.idDonVi || formDanhMuc.idDonVi.length === 0) return false;
  return formDanhMuc.idDonVi.some((id) => isDonViDisabled(id));
});

const getDonViDisabledNamesDanhMuc = computed(() => {
  if (!formDanhMuc.idDonVi || formDanhMuc.idDonVi.length === 0) return [];
  return formDanhMuc.idDonVi
    .map((id) => danhSachDonVi.value.find((d) => d.id === id))
    .filter((dv) => !dv || dv.trangThaiXoa)
    .map((dv) => dv?.tenDonVi || `ID ${dv?.id || "N/A"}`);
});

const openAddDanhMucModal = () => {
  formDanhMuc.tenDanhMuc = "";
  formDanhMuc.idDonVi = [];
  errorsDanhMuc.tenDanhMuc = "";
  errorsDanhMuc.idDonVi = "";
  showAddDanhMucModal.value = true;
};

const closeAddDanhMucModal = () => {
  showAddDanhMucModal.value = false;
  errorsDanhMuc.tenDanhMuc = "";
  errorsDanhMuc.idDonVi = "";
};

const toggleDonVi = (id) => {
  if (!id) return;
  const index = formDanhMuc.idDonVi.indexOf(id);
  if (index === -1) {
    formDanhMuc.idDonVi.push(id);
  } else {
    formDanhMuc.idDonVi.splice(index, 1);
  }
  donViId.value = null;
};

const addDanhMucSubmit = async () => {
  let hasError = false;
  errorsDanhMuc.tenDanhMuc = "";
  errorsDanhMuc.idDonVi = "";

  if (!formDanhMuc.tenDanhMuc || formDanhMuc.tenDanhMuc.trim() === "") {
    errorsDanhMuc.tenDanhMuc = "Vui lòng nhập tên danh mục";
    hasError = true;
  }
  if (!formDanhMuc.idDonVi || formDanhMuc.idDonVi.length === 0) {
    errorsDanhMuc.idDonVi = "Vui lòng chọn ít nhất một đơn vị";
    hasError = true;
  }
  if (coDonViNgungHoatDongDanhMuc.value) {
    const names = getDonViDisabledNamesDanhMuc.value.join(", ");
    errorsDanhMuc.idDonVi = `Đơn vị [${names}] đã ngừng hoạt động, không thể áp dụng cho danh mục!`;
    notify.error(`Đơn vị [${names}] đã ngừng hoạt động, không thể thêm danh mục!`);
    return;
  }
  if (hasError) return;

  if (!(await notify.confirm("Bạn muốn thêm danh mục này?"))) return;
  nprogress.start();
  try {
    await addDanhMuc({
      tenDanhMuc: formDanhMuc.tenDanhMuc,
      idDonVi: formDanhMuc.idDonVi,
    });
    notify.success("Thêm danh mục thành công!");
    await loadDanhMuc();
    closeAddDanhMucModal();
  } catch (e) {
    notify.error(
      e.response?.data?.message || (typeof e.response?.data === "string" ? e.response.data : "Có lỗi xảy ra"),
    );
  } finally {
    nprogress.done();
  }
};

// Hiển thị danh sách danh mục
const id = route.params.id;
const danhSachDonVi = ref([]);
const danhSachDanhMuc = ref([]);

// Search & Select
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";
const keyword = ref("");
const trangThai = ref("");

const handleWsMessage = async (msg) => {
  if (msg?.type === "DON_VI_UPDATE") {
    if (keyword.value || trangThai.value !== "") {
      onFilter();
    } else {
      await loadDonVi();
    }
    if (showAddDanhMucModal.value) {
      const disabledNames = getDonViDisabledNamesDanhMuc.value;
      if (disabledNames.length > 0) {
        notify.confirm(
          `Đơn vị định lượng [${disabledNames.join(", ")}] đã bị ngừng hoạt động. Vui lòng kiểm tra lại thông tin trước khi xác nhận!`,
        );
      } else {
        notify.confirm(
          "Đơn vị định lượng có sự thay đổi hoặc đã được bật lại. Vui lòng kiểm tra lại thông tin trước khi xác nhận!",
        );
      }
    }
    if (showAddModal.value) {
      notify.confirm(
        "Dữ liệu đơn vị định lượng có sự thay đổi, vui lòng kiểm tra lại thông tin trước khi xác nhận!",
      );
    }
  } else if (msg?.type === "DANH_MUC_UPDATE") {
    await loadDanhMuc();
    if (showAddModal.value) {
      notify.confirm(
        "Danh mục áp dụng có sự thay đổi, vui lòng kiểm tra lại thông tin trước khi xác nhận!",
      );
    }
  }
};

const onFilter = async () => {
  try {
    nprogress.start();
    const res = await searchDonVi({
      keyword: keyword.value,
      trangThaiXoa:
        trangThai.value === "" ? null : trangThai.value == 1 ? true : false,
    });
    danhSachDonVi.value = res.data;
    currentPage.value = 1;
  } catch (e) {
    console.error("Lỗi search", e);
  } finally {
    nprogress.done();
  }
};

onMounted(() => {
  loadDonVi();
  loadDanhMuc();
  connectWebSocket(null, handleWsMessage);
});

onUnmounted(() => {
  disconnectWebSocket(handleWsMessage);
});

/* ===== FORM DATA ===== */
const errors = reactive({
  tenDonVi: "",
  danhSachDanhMucs: "",
  dinhLuongs: "",
});

const formDonVi = reactive({
  tenDonVi: "",
  danhSachDanhMucs: [],
  dinhLuongs: [],
});

const validateForm = () => {
  let hasError = false;
  errors.tenDonVi = "";
  errors.danhSachDanhMucs = "";
  errors.dinhLuongs = "";

  if (!formDonVi.tenDonVi || formDonVi.tenDonVi.trim() === "") {
    errors.tenDonVi = "Vui lòng nhập tên đơn vị";
    hasError = true;
  }
  if (!formDonVi.danhSachDanhMucs || formDonVi.danhSachDanhMucs.length === 0) {
    errors.danhSachDanhMucs = "Vui lòng chọn ít nhất một danh mục áp dụng";
    hasError = true;
  }
  if (!formDonVi.dinhLuongs || formDonVi.dinhLuongs.length === 0) {
    errors.dinhLuongs = "Vui lòng nhập ít nhất một định lượng";
    hasError = true;
  }
  return hasError;
};

const submitDonVi = async () => {
  if (validateForm()) return;
  if (!(await notify.confirm("Bạn có muốn thêm thuộc tính?"))) return;
  nprogress.start();
  try {
    await addDonViTongHop({
      tenDonVi: formDonVi.tenDonVi,
      idDanhMucs: formDonVi.danhSachDanhMucs,
      dinhLuongs: formDonVi.dinhLuongs,
    });
    notify.success("Thêm thuộc tính thành công!");
    await realoadDonVi();
    closeModal();
  } catch (e) {
    notify.error(
      e.response?.data?.message || e.response?.data || "Có lỗi xảy ra",
    );
  } finally {
    nprogress.done();
  }
};

const updateDonViSubmmit = async () => {
  if (validateForm()) return;
  if (!(await notify.confirm("Bạn có muốn cập nhật thuộc tính?"))) return;
  nprogress.start();
  try {
    await updateDonVi(editingId.value, {
      tenDonVi: formDonVi.tenDonVi,
      idDanhMucs: formDonVi.danhSachDanhMucs,
      dinhLuongs: formDonVi.dinhLuongs,
    });
    notify.success("Cập nhật thuộc tính thành công!");
    await realoadDonVi();
    closeModal();
  } catch (error) {
    notify.error(
      error.response?.data?.message || error.response?.data || "Có lỗi xảy ra",
    );
  } finally {
    nprogress.done();
  }
};

const inputDinhLuong = ref("");
const addDinhLuongTag = () => {
  const value = parseFloat(inputDinhLuong.value);
  if (!isNaN(value) && !formDonVi.dinhLuongs.includes(value)) {
    formDonVi.dinhLuongs.push(value);
    inputDinhLuong.value = "";
    errors.dinhLuongs = "";
  }
};
const removeDinhLuongTag = (index) => {
  formDonVi.dinhLuongs.splice(index, 1);
};
const addDanhMucToUnit = (value) => {
  if (value && !formDonVi.danhSachDanhMucs.includes(value)) {
    formDonVi.danhSachDanhMucs.push(value);
    errors.danhSachDanhMucs = "";
  }
  selectedDanhMuc.value = null;
};
const removeDanhMucFromUnit = (index) => {
  formDonVi.danhSachDanhMucs.splice(index, 1);
};
const getTenDanhMucById = (id) => {
  const dm = danhSachDanhMuc.value.find((item) => item.id === id);
  return dm ? dm.tenDanhMuc : "N/A";
};

const loadDonVi = async () => {
  try {
    const res = await getAllDonVi();
    danhSachDonVi.value = res.data;
  } catch (error) {
    console.error("Không tìm thấy đơn vị", error);
  }
};

const updateTrangThai = async (id) => {
  if (!(await notify.confirm("Bạn có muốn chuyển trạng thái?"))) return;
  nprogress.start();
  try {
    await chuyenTrangThai(id);
    notify.success("Chuyển trạng thái thành công");
    await loadDonVi();
  } catch (e) {
    notify.error(e.response?.data || "Có lỗi xảy ra");
  }
  nprogress.done();
};

const loadDanhMuc = async () => {
  try {
    const res = await getAllDanhMuc();
    danhSachDanhMuc.value = res.data;
  } catch (error) {
    console.log("Không tìm thấy danh mục", error);
  }
};

const realoadDonVi = async () => {
  try {
    const res = await getAllDonVi();
    danhSachDonVi.value = res.data;
    currentPage.value = 1;
    keyword.value = "";
    trangThai.value = "";
  } catch (e) {
    console.error(e);
  }
};

/* ===== MODAL Them va sua danh muc ===== */
const showAddModal = ref(false);

const openAddModal = () => {
  isEdit.value = false;
  editingId.value = null;
  showAddModal.value = true;
};

const openEdit = async (id) => {
  isEdit.value = true;
  editingId.value = id;
  try {
    nprogress.start();
    const res = await detaiDonVi(id);
    const data = res.data;
    formDonVi.tenDonVi = data.tenDonVi;
    formDonVi.danhSachDanhMucs = danhSachDanhMuc.value
      .filter((dm) => data.danhMucs.includes(dm.tenDanhMuc))
      .map((dm) => dm.id);
    formDonVi.dinhLuongs = data.dinhLuongS.map((dl) => dl.giaTri);
    showAddModal.value = true;
  } catch (e) {
    notify.error("Không lấy được dữ liệu đơn vị");
  } finally {
    nprogress.done();
  }
};

const closeModal = () => {
  showAddModal.value = false;
  isEdit.value = false;
  editingId.value = null;
  formDonVi.tenDonVi = "";
  formDonVi.danhSachDanhMucs = [];
  formDonVi.dinhLuongs = [];
  errors.tenDonVi = "";
  errors.danhSachDanhMucs = "";
  errors.dinhLuongs = "";
};

/* ===== BIẾN ĐIỀU KHIỂN XỔ DÒNG ===== */
const expandedRows = ref([]);
const viewSanPhamByDanhMuc = (id) => {
  const index = expandedRows.value.indexOf(id);
  if (index > -1) {
    expandedRows.value.splice(index, 1);
  } else {
    expandedRows.value.push(id);
  }
};

/* ===== PHÂN TRANG ===== */
const currentPage = ref(1);
const pageSize = ref(5);
const totalItems = computed(() => danhSachDonVi.value.length);
const totalPages = computed(
  () => Math.ceil(totalItems.value / pageSize.value) || 1,
);
const danhSachHienThi = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return danhSachDonVi.value.slice(start, start + pageSize.value);
});

const selectedIds = ref([]);
const isAllSelected = computed({
  get() {
    return (
      danhSachHienThi.value.length > 0 &&
      danhSachHienThi.value.every((dv) => selectedIds.value.includes(dv.id))
    );
  },
  set(val) {
    if (val) {
      danhSachHienThi.value.forEach((dv) => {
        if (!selectedIds.value.includes(dv.id)) {
          selectedIds.value.push(dv.id);
        }
      });
    } else {
      const idsToRemove = danhSachHienThi.value.map((dv) => dv.id);
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
  <div class="quan-ly-dinh-luong">
    <div class="box">
      <div class="box-header">
        <h2 class="main-title">
          <span class="icon-wrapper">
            <img src="@/assets/icons/dinhluong.png" alt="Định lượng" />
          </span>
          Quản Lý Định Lượng
        </h2>
        <p>Quản lý đơn vị tính và các định lượng món ăn</p>
      </div>
    </div>

    <div class="filter-box">
      <div class="filter-item">
        <label>Tìm kiếm</label>
        <input
          class="custom-input"
          placeholder="Tên đơn vị..."
          v-model="keyword"
          @input="onFilter"
        />
      </div>

      <div class="filter-item">
        <label>Trạng thái</label>
        <v-select
          class="modern-select"
          v-model="trangThai"
          :options="[
            { label: 'Tất cả', value: '' },
            { label: 'Hoạt động', value: 0 },
            { label: 'Không hoạt động', value: 1 },
          ]"
          label="label"
          :reduce="(opt) => opt.value"
          :clearable="false"
          placeholder="Tất cả trạng thái"
          @update:modelValue="onFilter"
        />
      </div>
    </div>

    <div class="box">
      <div class="box-header box-header-flex">
        <h3 class="table-title">
          <span class="icon-wrapper-small">
            <img :src="iconDanhSach" class="icon-title-small" />
          </span>
          Danh sách đơn vị
        </h3>

        <div class="header-actions">
          <button class="add-btn" @click="openAddModal">
            <img :src="iconAdd" alt="" />
            <span>Thêm đơn vị</span>
          </button>

          <button class="refresh-btn" @click="realoadDonVi">
            <img :src="iconRefresh" alt="" />
            <span>Làm mới</span>
          </button>
        </div>
      </div>

      <div class="table-content">
        <table class="table-main">
          <thead>
            <tr>
              <th style="text-align: left">STT</th>
              <th style="text-align: left">Tên đơn vị</th>
              <th>Số lượng định lượng</th>
              <th>Trạng thái</th>
              <th style="text-align: center">Thao tác</th>
            </tr>
          </thead>

          <tbody>
            <tr v-if="danhSachHienThi.length === 0">
              <td colspan="6" class="empty-table">
                🔍 Không tìm thấy đơn vị phù hợp
              </td>
            </tr>
            <template v-for="(dv, index) in danhSachHienThi" :key="dv.id">
              <tr>
                <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
                <td class="text-bold" style="text-align: left">
                  {{ dv.tenDonVi }}
                </td>
                <td>
                  <span class="badge-count">{{ dv.soLuongDinhLuong }}</span>
                </td>
                <td>
                  <span
                    class="status-text"
                    :class="dv.trangThaiXoa ? 'status-error' : 'status-success'"
                  >
                    {{ dv.trangThaiXoa ? "Không hoạt động" : "Hoạt động" }}
                  </span>
                </td>
                <td class="text-center">
                  <div class="action-cell">
                    <button
                      class="btn-action-icon"
                      :class="{ disabled: dv.trangThaiXoa }"
                      @click="!dv.trangThaiXoa && openEdit(dv.id)"
                      v-tooltip="dv.trangThaiXoa ? '' : 'Chỉnh sửa'"
                    >
                      <img :src="iconEdit" class="img-icon-action" />
                    </button>

                    <button
                      class="btn-action-icon"
                      :class="{
                        'active-view': expandedRows.includes(dv.id),
                        disabled: dv.trangThaiXoa,
                      }"
                      @click="!dv.trangThaiXoa && viewSanPhamByDanhMuc(dv.id)"
                      v-tooltip="'Xem danh mục áp dụng'"
                    >
                      <img :src="iconView" class="img-icon-action" />
                    </button>

                    <label class="switch" v-tooltip="'Đổi trạng thái'">
                      <input
                        type="checkbox"
                        :checked="!dv.trangThaiXoa"
                        @change="updateTrangThai(dv.id)"
                      />
                      <span class="slider"></span>
                    </label>
                  </div>
                </td>
              </tr>

              <tr v-if="expandedRows.includes(dv.id)" class="row-detail">
                <td colspan="6" class="p-0">
                  <div class="detail-container">
                    <div class="applied-categories">
                      <div class="detail-title">
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
                          class="mr-1"
                        >
                          <path
                            d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1 0-5H20"
                          />
                        </svg>
                        DANH MỤC ÁP DỤNG
                      </div>
                      <div class="category-tags">
                        <span
                          v-for="(tenDM, i) in dv.danhMucs"
                          :key="i"
                          class="tag-item"
                        >
                          {{ tenDM }}
                        </span>
                        <span
                          v-if="!dv.danhMucs || dv.danhMucs.length === 0"
                          class="text-placeholder"
                        >
                          Chưa có danh mục nào.
                        </span>
                      </div>
                    </div>

                    <div class="detail-divider"></div>

                    <div class="dinhluong-section">
                      <div class="detail-title">
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
                          class="mr-1"
                        >
                          <line x1="12" y1="5" x2="12" y2="19" />
                          <line x1="5" y1="12" x2="19" y2="12" />
                        </svg>
                        DANH SÁCH ĐỊNH LƯỢNG ({{ dv.tenDonVi }})
                      </div>
                      <div class="dinhluong-grid">
                        <div
                          v-for="dl in dv.dinhLuongS"
                          :key="dl.id"
                          class="dl-card"
                        >
                          <span class="dl-value">{{ dl.giaTri }}</span>
                        </div>
                        <span
                          v-if="!dv.dinhLuongS || dv.dinhLuongS.length === 0"
                          class="text-placeholder"
                        >
                          Chưa có định lượng nào.
                        </span>
                      </div>
                    </div>
                  </div>
                </td>
              </tr>
            </template>
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
          Hiển thị {{ danhSachHienThi.length }} / {{ totalItems }} đơn vị
        </div>
      </div>
    </div>

    <Transition name="fade">
      <Teleport to="body">
        <div v-if="showAddModal" class="modal-overlay" @click.self="closeModal">
          <div class="modal-box modal-wide">
            <div class="modal-header">
              <h3>{{ isEdit ? "Cập nhật đơn vị" : "Thêm đơn vị mới" }}</h3>
              <button class="btn-icon-close" @click="closeModal">×</button>
            </div>

            <div class="modal-body">
              <div class="grid-form">
                <div class="grid-item">
                  <label>Tên đơn vị <span class="text-danger">*</span></label>
                  <input
                    class="custom-input"
                    :class="{ 'input-invalid': errors.tenDonVi }"
                    v-model="formDonVi.tenDonVi"
                    @input="errors.tenDonVi = ''"
                    placeholder="Ví dụ: Gram, Ml, Lon..."
                  />
                  <transition name="shake">
                    <p v-if="errors.tenDonVi" class="error-hint mt-1">
                      <i class="bi bi-exclamation-circle"></i>
                      {{ errors.tenDonVi }}
                    </p>
                  </transition>
                </div>

                <div class="grid-item">
                  <div class="label-row">
                    <label
                      >Danh mục áp dụng
                      <span class="text-danger">*</span></label
                    >
                    <div class="create-new-item" @click="openAddDanhMucModal">
                      <span class="icon">+</span> Thêm danh mục
                    </div>
                  </div>
                  <div class="donvi-row">
                    <v-select
                      append-to-body
                      class="flex-grow-1 modern-select"
                      :class="{ 'input-invalid-vs': errors.danhSachDanhMucs }"
                      v-model="selectedDanhMuc"
                      :options="
                        danhSachDanhMuc.filter(
                          (dm) => !formDonVi.danhSachDanhMucs.includes(dm.id),
                        )
                      "
                      label="tenDanhMuc"
                      :reduce="(dm) => dm.id"
                      placeholder="-- Chọn danh mục để thêm --"
                      @update:modelValue="addDanhMucToUnit"
                    ></v-select>
                  </div>
                  <transition name="shake">
                    <p v-if="errors.danhSachDanhMucs" class="error-hint mt-1">
                      <i class="bi bi-exclamation-circle"></i>
                      {{ errors.danhSachDanhMucs }}
                    </p>
                  </transition>

                  <div
                    class="donvi-tags-container"
                    v-if="formDonVi.danhSachDanhMucs.length > 0"
                  >
                    <label class="tag-title">Danh mục đã chọn:</label>
                    <div class="donvi-tags">
                      <span
                        v-for="(idDm, index) in formDonVi.danhSachDanhMucs"
                        :key="index"
                        class="tag-donvi"
                      >
                        {{ getTenDanhMucById(idDm) }}
                        <span
                          class="remove"
                          @click="removeDanhMucFromUnit(index)"
                          >×</span
                        >
                      </span>
                    </div>
                  </div>
                </div>

                <div class="grid-item" style="grid-column: span 2">
                  <label
                    >Nhập các định lượng
                    <span class="text-danger">*</span></label
                  >
                  <div class="donvi-row">
                    <input
                      class="custom-input flex-grow-1"
                      :class="{ 'input-invalid': errors.dinhLuongs }"
                      type="number"
                      v-model="inputDinhLuong"
                      placeholder="Nhập số và nhấn Enter hoặc nút +"
                      @keyup.enter="addDinhLuongTag"
                    />
                    <button class="btn-add-dv" @click="addDinhLuongTag">
                      +
                    </button>
                  </div>
                  <transition name="shake">
                    <p v-if="errors.dinhLuongs" class="error-hint mt-1">
                      <i class="bi bi-exclamation-circle"></i>
                      {{ errors.dinhLuongs }}
                    </p>
                  </transition>

                  <div
                    class="donvi-tags-container"
                    v-if="formDonVi.dinhLuongs.length > 0"
                  >
                    <label class="tag-title">Định lượng đã nhập:</label>
                    <div class="donvi-tags">
                      <span
                        v-for="(dl, index) in formDonVi.dinhLuongs"
                        :key="index"
                        class="tag-donvi outline"
                      >
                        {{ dl }}
                        <span class="remove" @click="removeDinhLuongTag(index)"
                          >×</span
                        >
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="modal-footer bg-white border-top">
              <button class="btn-cancel" @click="closeModal">Hủy bỏ</button>
              <button
                class="btn-primary"
                @click="isEdit ? updateDonViSubmmit() : submitDonVi()"
              >
                <i class="ri-check-double-line" style="margin-right: 4px"></i>
                Xác nhận
              </button>
            </div>
          </div>
        </div>
      </Teleport>
    </Transition>

    <Transition name="fade">
      <Teleport to="body">
        <div
          v-if="showAddDanhMucModal"
          class="modal-overlay"
          @click.self="closeAddDanhMucModal"
        >
          <div class="modal-box" style="width: 500px">
            <div class="modal-header">
              <h3>Thêm danh mục mới</h3>
              <button class="btn-icon-close" @click="closeAddDanhMucModal">
                ×
              </button>
            </div>

            <div class="modal-body">
              <div class="grid-item mb-3">
                <label>Tên danh mục <span class="text-danger">*</span></label>
                <input
                  class="custom-input"
                  :class="{ 'input-invalid': errorsDanhMuc.tenDanhMuc }"
                  v-model="formDanhMuc.tenDanhMuc"
                  @input="errorsDanhMuc.tenDanhMuc = ''"
                  placeholder="Nhập tên danh mục..."
                />
                <transition name="shake">
                  <p v-if="errorsDanhMuc.tenDanhMuc" class="error-hint mt-1">
                    <i class="bi bi-exclamation-circle"></i>
                    {{ errorsDanhMuc.tenDanhMuc }}
                  </p>
                </transition>
              </div>

              <div class="grid-item">
                <label>Đơn vị <span class="text-danger">*</span></label>
                <v-select
                  append-to-body
                  class="modern-select"
                  :class="{ 'input-invalid-vs': errorsDanhMuc.idDonVi }"
                  v-model="donViId"
                  :options="activeDonViList"
                  label="tenDonVi"
                  :reduce="(dv) => dv.id"
                  placeholder="-- Chọn đơn vị --"
                  @update:modelValue="
                    (val) => {
                      toggleDonVi(val);
                      errorsDanhMuc.idDonVi = '';
                    }
                  "
                />
                <transition name="shake">
                  <p v-if="errorsDanhMuc.idDonVi" class="error-hint mt-1">
                    <i class="bi bi-exclamation-circle"></i>
                    {{ errorsDanhMuc.idDonVi }}
                  </p>
                </transition>
              </div>

              <div
                v-if="formDanhMuc.idDonVi.length > 0"
                class="donvi-tags-container mt-3"
              >
                <label class="tag-title">Đơn vị đã chọn:</label>
                <div class="donvi-tags">
                  <span
                    v-for="id in formDanhMuc.idDonVi"
                    :key="id"
                    class="tag-donvi"
                    :class="{ 'tag-donvi-disabled': isDonViDisabled(id) }"
                  >
                    {{ getDonViName(id) }}
                    <span v-if="isDonViDisabled(id)" class="tag-disabled-badge">(Đã tắt)</span>
                    <span class="remove" @click="toggleDonVi(id)">×</span>
                  </span>
                </div>
                <transition name="shake">
                  <p v-if="coDonViNgungHoatDongDanhMuc" class="error-hint mt-2">
                    <i class="bi bi-exclamation-triangle-fill"></i>
                    Có đơn vị đã ngừng hoạt động trong danh sách đã chọn. Vui lòng xóa bỏ trước khi xác nhận.
                  </p>
                </transition>
              </div>
            </div>

            <div class="modal-footer">
              <button class="btn-cancel" @click="closeAddDanhMucModal">
                Hủy bỏ
              </button>
              <button class="btn-primary" @click="addDanhMucSubmit">
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
  min-width: 150px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-width: 300px;
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
  padding: 12px 18px;
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

/* Ép cứng Checkbox và STT */
.table-main th:nth-child(1),
.table-main td:nth-child(1) {
  width: 60px;
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
.badge-count {
  background: #f1f5f9;
  padding: 4px 10px;
  border-radius: 12px;
  font-weight: 700;
  color: #475569;
  font-size: 12px;
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

/* TRẠNG THÁI (PILL) */
.status-text {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 110px;
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
.btn-action-icon.active-view {
  background: #d73737;
  filter: none;
}
.btn-action-icon.active-view img {
  filter: brightness(0) invert(1);
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

/* ===== PHẦN XỔ DÒNG (EXPANDED ROW) ===== */
.row-detail {
  background-color: #fdfdfd;
}
.p-0 {
  padding: 0 !important;
}
.detail-container {
  padding: 16px 24px;
  border-left: 3px solid #d73737;
  text-align: left;
  animation: slideDown 0.25s ease-out;
}
@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.detail-title {
  display: flex;
  align-items: center;
  font-weight: 700;
  font-size: 13px;
  margin-bottom: 12px;
  color: #333;
}
.mr-1 {
  margin-right: 6px;
}

.category-tags,
.dinhluong-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.tag-item {
  background: #fef2f2;
  color: #d73737;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  border: 1px solid #fecaca;
}
.dl-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  padding: 6px 16px;
  border-radius: 6px;
  font-weight: 600;
  color: #333;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
}
.detail-divider {
  height: 1px;
  background: #e2e8f0;
  border: none;
  margin: 16px 0;
}
.text-placeholder {
  color: #94a3b8;
  font-size: 13px;
  font-style: italic;
}

/* ===== PHÂN TRANG PREMIUM ===== */
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

/* ===== V-SELECT CUSTOM ===== */
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
/* SỬA */
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

/* ===== MODAL & FORM ===== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-box {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  width: 900px;
  max-width: 95vw;
  display: flex;
  flex-direction: column;
}
.modal-header {
  padding: 18px 24px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.modal-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
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
}
/* ===== MODAL FOOTER & BUTTONS ĐỒNG BỘ ===== */
.modal-footer {
  padding: 16px 24px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  background-color: #f8fafc;
  border-top: 1px solid #e2e8f0;
}

.btn-cancel,
.btn-primary {
  position: relative;
  overflow: hidden;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border-radius: 6px !important;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Nút Đóng / Hủy */
.btn-cancel {
  background-color: #ffffff;
  color: #4b5563;
  border: 1px solid #d1d5db;
  padding: 10px 20px;
}

.btn-cancel:hover {
  background-color: #f8fafc;
  border-color: #cbd5e1;
  color: #0f172a;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

/* Nút Lưu / Cập nhật */
.btn-primary {
  background-color: #d73737;
  color: #ffffff;
  border: 1px solid #d73737;
  padding: 10px 20px;
}

.btn-primary::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.3),
    transparent
  );
  transition: none;
}

.btn-primary:hover::before {
  left: 100%;
  transition: all 0.6s ease-in-out;
}

.btn-primary:hover {
  background-color: #b92b2b;
  border-color: #b92b2b;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(215, 55, 55, 0.35);
}

.btn-primary:hover i {
  transform: scale(1.15);
  transition: transform 0.2s ease;
}

.btn-cancel:active,
.btn-primary:active {
  transform: translateY(1px) scale(0.97);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
/* Grid Form */
.grid-form {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
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
  min-height: 30px;
  display: flex;
  align-items: center;
}
.label-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 30px;
}
.donvi-row {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 42px;
}
.flex-grow-1 {
  flex: 1;
}

.create-new-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #d73737;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  background: #fdf2f2;
  transition: 0.2s;
}
.create-new-item:hover {
  background: #fee2e2;
}
.create-new-item .icon {
  font-size: 14px;
  font-weight: bold;
}

.btn-add-dv {
  width: 42px;
  height: 42px;
  background: #d73737;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 20px;
  font-weight: bold;
  transition: 0.2s;
}
.btn-add-dv:hover {
  background: #b92b2b;
}

.donvi-tags-container {
  margin-top: 15px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px dashed #e2e8f0;
}
.tag-title {
  font-weight: 600;
  color: #475569;
  font-size: 13px;
  margin-bottom: 8px;
  display: block;
}
.donvi-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-donvi {
  background: #fef2f2;
  color: #d73737;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  border: 1px solid #fecaca;
  display: flex;
  align-items: center;
  gap: 8px;
}
.tag-donvi-disabled {
  background: #f1f5f9 !important;
  color: #64748b !important;
  border-color: #cbd5e1 !important;
}
.tag-disabled-badge {
  font-size: 11px;
  color: #ef4444;
  font-weight: 700;
}
.tag-donvi.outline {
  background: #fff;
  border-style: dashed;
}
.tag-donvi .remove {
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
  transition: 0.2s;
}
.tag-donvi .remove:hover {
  color: #b92b2b;
  transform: scale(1.1);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* --- ERROR VALIDATION --- */
.error-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #e63946;
  font-size: 0.85rem;
  margin-top: 4px;
  padding: 6px 12px;
  background-color: #fff1f2;
  border-radius: 6px;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.input-invalid {
  border-color: #e63946 !important;
  box-shadow: 0 0 0 4px rgba(230, 57, 70, 0.1) !important;
}

.input-invalid-vs :deep(.vs__dropdown-toggle) {
  border-color: #e63946 !important;
  box-shadow: 0 0 0 4px rgba(230, 57, 70, 0.1) !important;
}

.shake-enter-active {
  animation: shake 0.4s cubic-bezier(0.36, 0.07, 0.19, 0.97) both;
}

@keyframes shake {
  10%,
  90% {
    transform: translate3d(-1px, 0, 0);
  }
  20%,
  80% {
    transform: translate3d(2px, 0, 0);
  }
  30%,
  50%,
  70% {
    transform: translate3d(-4px, 0, 0);
  }
  40%,
  60% {
    transform: translate3d(4px, 0, 0);
  }
}
</style>
<style>
/* append-to-body dropdown - không scoped */
.vs__dropdown-menu {
  border: 1px solid #e5e7eb !important;
  border-radius: 8px !important;
  margin-top: 8px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1) !important;
  padding: 6px !important;
  z-index: 9999 !important;
}
.vs__dropdown-option {
  border-radius: 6px !important;
  padding: 8px 12px !important;
  margin-bottom: 2px !important;
  color: #4b5563;
}
.vs__dropdown-option--highlight {
  background: #f3f4f6 !important;
  color: #d73737 !important;
}
.vs__dropdown-option--selected {
  background: #f3f5f9 !important;
  color: #d73737 !important;
}
</style>
