<script setup>
import { reactive, ref, computed, onMounted, onUnmounted } from "vue";
import {
  getAllDanhMuc,
  addDanhMuc,
  getDeTailDanhMuc,
  updateDanhMuc,
  deleteTrangThai,
  restoreDanhMuc,
  filterDanhMuc,
} from "@/api/admin/danhmuc/danhMucApi";
import { getAllDonVi } from "@/api/admin/thuoctinh/donViApi";
import { addDonViTongHop } from "@/api/admin/thuoctinh/donViApi";
import { useRouter } from "vue-router";
import { connectWebSocket, disconnectWebSocket } from "@/utils/websocket";
/* ===== ICON ===== */
import iconAdd from "@/assets/icons/add.png";
import iconEdit from "@/assets/icons/edit.png";
import iconFolder from "@/assets/icons/danh-muc.png";
import iconDanhSach from "@/assets/icons/danh-sach.png";
import iconRefresh from "@/assets/icons/refresh.png";
import iconView from "@/assets/icons/view.png";
import { useUtils } from "@/utils/helper";

// SEARCH && SELECT 2
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";

const keyword = ref("");
const trangThai = ref("");
const selectedDanhMucId = ref("");

const { notify, nprogress } = useUtils();
const router = useRouter();
const isEdit = ref(false);
const editingId = ref(null);

// Hiển thị danh sách danh mục
const danhSach = ref([]);
const danhMucFilter = ref([]);
const listDonVi = ref([]);
const soLuongMon = ref(0);
const donViId = ref(null);

const activeDonViList = computed(() => {
  return listDonVi.value.filter((dv) => !dv.trangThaiXoa);
});

const isDonViDisabled = (id) => {
  const dv = listDonVi.value.find((d) => d.id === id);
  return !dv || Boolean(dv.trangThaiXoa);
};

const getDonViName = (id) => {
  const dv = listDonVi.value.find((d) => d.id === id);
  return dv ? dv.tenDonVi : `Đơn vị #${id}`;
};

const coDonViNgungHoatDong = computed(() => {
  if (!formDanhMuc.idDonVi || formDanhMuc.idDonVi.length === 0) return false;
  return formDanhMuc.idDonVi.some((id) => isDonViDisabled(id));
});

const getDonViDisabledNames = computed(() => {
  if (!formDanhMuc.idDonVi || formDanhMuc.idDonVi.length === 0) return [];
  return formDanhMuc.idDonVi
    .map((id) => listDonVi.value.find((d) => d.id === id))
    .filter((dv) => !dv || dv.trangThaiXoa)
    .map((dv) => dv?.tenDonVi || `ID ${dv?.id || "N/A"}`);
});

const handleWsMessage = async (msg) => {
  if (msg?.type === "DANH_MUC_UPDATE" || msg?.type === "SAN_PHAM_UPDATE") {
    if (keyword.value || trangThai.value !== "" || selectedDanhMucId.value) {
      onFilter();
    } else {
      loadDanhMuc();
      loadDanhMucFilter();
    }
  }
  if (msg?.type === "DON_VI_UPDATE") {
    await loadDonVi();
    if (showAddModal.value) {
      const disabledNames = getDonViDisabledNames.value;
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
  }
};

onMounted(() => {
  loadDanhMuc();
  loadDanhMucFilter();
  loadDonVi();
  connectWebSocket(null, handleWsMessage);
});

onUnmounted(() => {
  disconnectWebSocket(handleWsMessage);
});

const loadDanhMuc = async () => {
  try {
    const res = await getAllDanhMuc();
    danhSach.value = res.data;
  } catch (error) {
    console.error("Không tìm thấy danh mục", error);
  }
};

const loadDonVi = async () => {
  const res = await getAllDonVi();
  listDonVi.value = res.data || [];
};

const loadDanhMucFilter = async () => {
  const res = await getAllDanhMuc();
  danhMucFilter.value = res.data;
};

// Lam moi danh mục
const reloadDanhMuc = async () => {
  keyword.value = "";
  trangThai.value = "";
  selectedDanhMucId.value = "";

  currentPage.value = 1;

  await loadDanhMuc();
};
/* ===== DỮ LIỆU ĐỊNH LƯỢNG ===== */
const formDanhMuc = reactive({
  tenDanhMuc: "",
  idDonVi: [],
});

const errors = reactive({
  tenDanhMuc: "",
  donVi: "",
  tenDonVi: "",
  danhSachDanhMucs: "",
  dinhLuongs: "",
});

/* Hàm thêm/xóa đơn vị khỏi danh sách chọn */
const toggleDonVi = (id) => {
  if (!id) return;
  const index = formDanhMuc.idDonVi.indexOf(id);
  if (index === -1) {
    formDanhMuc.idDonVi.push(id);
  } else {
    formDanhMuc.idDonVi.splice(index, 1);
  }
  errors.donVi = "";
  donViId.value = null; // reset select
};

/* ===== MODAL Thêm và sửa danh mục ===== */
const showAddModal = ref(false);

const openAddModal = () => {
  formDanhMuc.tenDanhMuc = "";
  formDanhMuc.idDonVi = [];
  errors.tenDanhMuc = "";
  errors.donVi = "";
  isEdit.value = false;
  editingId.value = null;
  showAddModal.value = true;
};

const closeModal = () => {
  showAddModal.value = false;
  errors.tenDanhMuc = "";
  errors.donVi = "";
  isEdit.value = false;
  editingId.value = null;
};

const openEditDanhMuc = async (id) => {
  try {
    const res = await getDeTailDanhMuc(id);
    formDanhMuc.tenDanhMuc = res.data.tenDanhMuc;
    formDanhMuc.idDonVi = res.data.idDonVi || [];
    editingId.value = id;
    isEdit.value = true;
    errors.tenDanhMuc = "";
    errors.donVi = "";
    showAddModal.value = true;
  } catch (error) {
    console.log("Không lấy được chi tiết danh mục", error);
  }
};

/* ===== SUBMIT ===== */
const addDanhMucSubmit = async () => {
  let hasError = false;
  errors.tenDanhMuc = "";
  errors.donVi = "";

  if (!formDanhMuc.tenDanhMuc || formDanhMuc.tenDanhMuc.trim() === "") {
    errors.tenDanhMuc = "Vui lòng nhập tên danh mục";
    hasError = true;
  }
  if (!formDanhMuc.idDonVi || formDanhMuc.idDonVi.length === 0) {
    errors.donVi = "Vui lòng chọn ít nhất 1 đơn vị";
    hasError = true;
  }
  if (coDonViNgungHoatDong.value) {
    const names = getDonViDisabledNames.value.join(", ");
    errors.donVi = `Đơn vị [${names}] đã ngừng hoạt động, không thể áp dụng cho danh mục!`;
    notify.error(`Đơn vị [${names}] đã ngừng hoạt động, không thể thêm danh mục!`);
    return;
  }
  if (hasError) return;

  if (!(await notify.confirm("Bạn muốn thêm danh mục này?"))) return;
  nprogress.start();
  try {
    const res = await addDanhMuc({
      tenDanhMuc: formDanhMuc.tenDanhMuc,
      idDonVi: formDanhMuc.idDonVi,
    });
    notify.success(res.data.message || "Thêm danh mục thành công!");
    await reloadDanhMuc();
    closeModal();
  } catch (e) {
    notify.error(
      e.response?.data?.message || (typeof e.response?.data === "string" ? e.response.data : "Có lỗi xảy ra"),
    );
  } finally {
    nprogress.done();
  }
};

const updateDanhMucSubmit = async () => {
  let hasError = false;
  errors.tenDanhMuc = "";
  errors.donVi = "";

  if (!formDanhMuc.tenDanhMuc || formDanhMuc.tenDanhMuc.trim() === "") {
    errors.tenDanhMuc = "Vui lòng nhập tên danh mục";
    hasError = true;
  }
  if (!formDanhMuc.idDonVi || formDanhMuc.idDonVi.length === 0) {
    errors.donVi = "Vui lòng chọn ít nhất 1 đơn vị";
    hasError = true;
  }
  if (coDonViNgungHoatDong.value) {
    const names = getDonViDisabledNames.value.join(", ");
    errors.donVi = `Đơn vị [${names}] đã ngừng hoạt động, không thể áp dụng cho danh mục!`;
    notify.error(`Đơn vị [${names}] đã ngừng hoạt động, không thể cập nhật danh mục!`);
    return;
  }
  if (hasError) return;

  if (!(await notify.confirm("Bạn có muốn cập nhật danh mục này không?")))
    return;
  nprogress.start();
  try {
    const res = await updateDanhMuc({
      id: editingId.value,
      tenDanhMuc: formDanhMuc.tenDanhMuc,
      idDonVi: formDanhMuc.idDonVi,
    });
    notify.success(res.data.message || "Chỉnh sửa danh mục thành công!");
    await reloadDanhMuc();
    closeModal();
  } catch (e) {
    notify.error(
      e.response?.data?.message || (typeof e.response?.data === "string" ? e.response.data : "Có lỗi xảy ra"),
    );
  } finally {
    nprogress.done();
  }
};

const chuyenTrangThai = async (dm) => {
  if (!(await notify.confirm("Bạn có muốn đổi trạng thái danh mục không?")))
    return;
  nprogress.start();
  try {
    if (dm.trangThaiXoa == 0) {
      await deleteTrangThai(dm.id);
    } else {
      await restoreDanhMuc(dm.id);
    }
    notify.success("Thay đổi trạng thái thành công");
    await loadDanhMuc();
  } catch (error) {
    notify.error(error.response?.data || "Có lỗi xảy ra");
    console.error(error);
  } finally {
    nprogress.done();
  }
};

const viewSanPhamByDanhMuc = (idDanhMuc) => {
  router.push({ path: `/admin/san-pham/by-danh-muc/${idDanhMuc}` });
};

const onFilter = async () => {
  try {
    const params = {
      keyword: keyword.value || null,
      idDanhMuc:
        selectedDanhMucId.value === "" ? null : selectedDanhMucId.value,
      trangThaiXoa: trangThai.value === "" ? null : trangThai.value,
    };
    const res = await filterDanhMuc(params);
    danhSach.value = res.data;
    currentPage.value = 1;
  } catch (e) {
    console.error("Lỗi filter", e);
  }
};

/* ===== MODAL ADD DON VI NHANH ===== */
const showAddDonViModal = ref(false);
const formDonVi = reactive({
  tenDonVi: "",
  dinhLuongs: [],
  danhSachDanhMucs: [],
});
const inputDinhLuong = ref("");

const openAddDonVi = () => {
  formDonVi.tenDonVi = "";
  formDonVi.dinhLuongs = [];
  formDonVi.danhSachDanhMucs = [];
  errors.tenDonVi = "";
  errors.danhSachDanhMucs = "";
  errors.dinhLuongs = "";
  showAddDonViModal.value = true;
};
const closeAddDonVi = () => {
  showAddDonViModal.value = false;
};
const addDinhLuongTag = () => {
  const value = parseFloat(inputDinhLuong.value);
  if (!isNaN(value) && !formDonVi.dinhLuongs.includes(value)) {
    formDonVi.dinhLuongs.push(value);
    errors.dinhLuongs = "";
    inputDinhLuong.value = "";
  }
};
const removeDinhLuongTag = (index) => {
  formDonVi.dinhLuongs.splice(index, 1);
};
const addDanhMucToUnit = (id) => {
  if (!id) return;
  if (!formDonVi.danhSachDanhMucs.includes(id)) {
    formDonVi.danhSachDanhMucs.push(id);
    errors.danhSachDanhMucs = "";
  }
};
const removeDanhMucFromUnit = (index) => {
  formDonVi.danhSachDanhMucs.splice(index, 1);
};
const getTenDanhMucById = (id) => {
  return danhSach.value.find((d) => d.id === id)?.tenDanhMuc;
};

const submitAddDonVi = async () => {
  let hasError = false;
  errors.tenDonVi = "";
  errors.danhSachDanhMucs = "";
  errors.dinhLuongs = "";

  if (!formDonVi.tenDonVi || formDonVi.tenDonVi.trim() === "") {
    errors.tenDonVi = "Vui lòng nhập tên đơn vị";
    hasError = true;
  }
  if (!formDonVi.danhSachDanhMucs || formDonVi.danhSachDanhMucs.length === 0) {
    errors.danhSachDanhMucs = "Vui lòng chọn ít nhất 1 danh mục";
    hasError = true;
  }
  if (!formDonVi.dinhLuongs || formDonVi.dinhLuongs.length === 0) {
    errors.dinhLuongs = "Vui lòng nhập ít nhất 1 định lượng";
    hasError = true;
  }
  if (hasError) return;

  if (!(await notify.confirm("Bạn muốn thêm đơn vị này?"))) return;
  nprogress.start();
  try {
    const res = await addDonViTongHop({
      tenDonVi: formDonVi.tenDonVi,
      dinhLuongs: formDonVi.dinhLuongs,
      idDanhMucs: formDonVi.danhSachDanhMucs,
    });
    notify.success("Thêm đơn vị thành công!");
    await loadDonVi();
    const newId = res.data.id;
    if (newId) toggleDonVi(newId);
    formDonVi.tenDonVi = "";
    formDonVi.dinhLuongs = [];
    formDonVi.danhSachDanhMucs = [];
    closeAddDonVi();
  } catch (e) {
    notify.error(e.response?.data?.message || "Có lỗi xảy ra");
  } finally {
    nprogress.done();
  }
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
      danhSachHienThi.value.every((dm) => selectedIds.value.includes(dm.id))
    );
  },
  set(val) {
    if (val) {
      danhSachHienThi.value.forEach((dm) => {
        if (!selectedIds.value.includes(dm.id)) {
          selectedIds.value.push(dm.id);
        }
      });
    } else {
      const idsToRemove = danhSachHienThi.value.map((dm) => dm.id);
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
  <div class="quan-ly-danh-muc">
    <div class="box">
      <div class="box-header">
        <h2 class="main-title">
          <span class="icon-wrapper">
            <img src="@/assets/icons/categories.png" alt="Danh mục" />
          </span>
          Quản Lý Danh Mục
        </h2>
        <p>Quản lý danh mục các loại món ăn</p>
      </div>
    </div>

    <div class="filter-box">
      <div class="filter-item">
        <label>Tìm kiếm</label>
        <input
          class="custom-input"
          placeholder="Tên danh mục..."
          v-model="keyword"
          @input="onFilter"
        />
      </div>

      <div class="filter-item">
        <label>Danh mục</label>
        <v-select
          class="modern-select"
          v-model="selectedDanhMucId"
          :options="[
            { label: 'Tất cả', value: '' },
            ...danhMucFilter.map((dm) => ({
              label: dm.tenDanhMuc,
              value: dm.id,
            })),
          ]"
          :reduce="(opt) => opt.value"
          placeholder="Tất cả danh mục"
          @update:modelValue="onFilter"
          :clearable="false"
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
            { label: 'Hoạt động', value: 0 },
            { label: 'Không hoạt động', value: 1 },
          ]"
          :reduce="(opt) => opt.value"
          placeholder="Tất cả trạng thái"
          @update:modelValue="onFilter"
          :clearable="false"
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
    </div>

    <div class="box">
      <div class="box-header box-header-flex">
        <h3 class="table-title">
          <span class="icon-wrapper-small">
            <img :src="iconDanhSach" class="icon-title-small" />
          </span>
          Danh sách danh mục
        </h3>

        <div class="header-actions">
          <button class="add-btn" @click="openAddModal">
            <img :src="iconAdd" alt="" />
            <span>Thêm danh mục</span>
          </button>

          <button class="refresh-btn" @click="reloadDanhMuc">
            <img :src="iconRefresh" alt="" />
            <span>Làm mới</span>
          </button>
        </div>
      </div>

      <div class="table-content">
        <table class="table-main">
          <thead>
            <tr>
              <th>STT</th>
              <th style="text-align: left">Mã danh mục</th>
              <th style="text-align: left">Tên danh mục</th>
              <th>Tổng số món ăn</th>
              <th>Trạng thái</th>
              <th style="text-align: center">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(dm, index) in danhSachHienThi" :key="dm.id">
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
              <td class="text-bold" style="text-align: left">
                {{ dm.maDanhMuc }}
              </td>
              <td>
                <div class="dm-name">
                  <img :src="iconFolder" class="dm-icon" />
                  <span>{{ dm.tenDanhMuc }}</span>
                </div>
              </td>
              <td>
                <span class="badge-count">{{ dm.soLuongMon }}</span>
              </td>
              <td>
                <span
                  class="status-text"
                  :class="dm.trangThaiXoa ? 'status-error' : 'status-success'"
                >
                  {{ dm.trangThaiXoa ? "Không hoạt động" : "Hoạt động" }}
                </span>
              </td>
              <td class="text-center">
                <div class="action-cell">
                  <button
                    class="btn-action-icon"
                    :class="{ disabled: dm.trangThaiXoa }"
                    @click="!dm.trangThaiXoa && openEditDanhMuc(dm.id)"
                    v-tooltip="dm.trangThaiXoa ? '' : 'Chỉnh sửa'"
                  >
                    <img :src="iconEdit" class="img-icon-action" />
                  </button>
                  <button
                    class="btn-action-icon"
                    :class="{ disabled: dm.trangThaiXoa }"
                    @click="!dm.trangThaiXoa && viewSanPhamByDanhMuc(dm.id)"
                    v-tooltip="'Xem chi tiết'"
                  >
                    <img :src="iconView" class="img-icon-action" />
                  </button>
                  <label class="switch" v-tooltip="'Đổi trạng thái'">
                    <input
                      type="checkbox"
                      :checked="!dm.trangThaiXoa"
                      @change="chuyenTrangThai(dm)"
                    />
                    <span class="slider"></span>
                  </label>
                </div>
              </td>
            </tr>
            <tr v-if="danhSachHienThi.length === 0">
              <td colspan="7" class="empty-table">
                🔍 Không tìm thấy danh mục phù hợp
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
          Hiển thị {{ danhSachHienThi.length }} / {{ totalItems }} danh mục
        </div>
      </div>
    </div>

    <Transition name="fade">
      <Teleport to="body">
        <div v-if="showAddModal" class="modal-overlay" @click.self="closeModal">
          <div class="modal-box modal-wide">
            <div class="modal-header">
              <h3>{{ isEdit ? "Cập nhật danh mục" : "Thêm danh mục mới" }}</h3>
              <button class="btn-icon-close" @click="closeModal">×</button>
            </div>

            <div class="modal-body">
              <div class="grid-form">
                <div class="grid-item">
                  <label>Tên danh mục <span class="text-danger">*</span></label>
                  <input
                    class="custom-input"
                    :class="{ 'input-invalid': errors.tenDanhMuc }"
                    v-model="formDanhMuc.tenDanhMuc"
                    @input="errors.tenDanhMuc = ''"
                    placeholder="Nhập tên danh mục..."
                  />
                  <transition name="shake">
                    <p v-if="errors.tenDanhMuc" class="error-hint">
                      <i class="bi bi-exclamation-circle"></i>
                      {{ errors.tenDanhMuc }}
                    </p>
                  </transition>
                </div>

                <div class="grid-item">
                  <label>Đơn vị tính <span class="text-danger">*</span></label>
                  <div class="donvi-row">
                    <v-select
                      append-to-body
                      class="flex-grow-1 modern-select"
                      :class="{ 'input-invalid-vs': errors.donVi }"
                      v-model="donViId"
                      :options="activeDonViList"
                      label="tenDonVi"
                      :reduce="(dv) => dv.id"
                      placeholder="-- Chọn đơn vị --"
                      @update:modelValue="toggleDonVi"
                    >
                      <template #no-options="{ search, searching }">
                        <template v-if="searching"
                          >Không tìm thấy "<b>{{ search }}</b
                          >"</template
                        >
                        <em v-else style="opacity: 0.5">Không có dữ liệu</em>
                      </template>
                    </v-select>
                    <div class="create-new-item" @click="openAddDonVi">
                      <span class="icon">+</span>
                      Tạo mới
                    </div>
                  </div>
                  <transition name="shake">
                    <p v-if="errors.donVi" class="error-hint mt-1">
                      <i class="bi bi-exclamation-circle"></i>
                      {{ errors.donVi }}
                    </p>
                  </transition>
                </div>
              </div>

              <div
                class="donvi-tags-container"
                v-if="formDanhMuc.idDonVi.length > 0"
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
                  <p v-if="coDonViNgungHoatDong" class="error-hint mt-2">
                    <i class="bi bi-exclamation-triangle-fill"></i>
                    Có đơn vị đã ngừng hoạt động trong danh sách đã chọn. Vui lòng xóa bỏ trước khi xác nhận.
                  </p>
                </transition>
              </div>
            </div>

            <div class="modal-footer">
              <button class="btn-cancel" @click="closeModal">Hủy bỏ</button>
              <button
                class="btn-primary"
                @click="isEdit ? updateDanhMucSubmit() : addDanhMucSubmit()"
              >
                <i class="bi bi-check2-all"></i>
                <div class="btn-light-sweep"></div>
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
          v-if="showAddDonViModal"
          class="modal-overlay"
          @click.self="closeAddDonVi"
        >
          <div class="modal-box modal-wide">
            <div class="modal-header">
              <h3>Thêm đơn vị mới</h3>
              <button class="btn-icon-close" @click="closeAddDonVi">×</button>
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
                    <p v-if="errors.tenDonVi" class="error-hint">
                      <i class="bi bi-exclamation-circle"></i>
                      {{ errors.tenDonVi }}
                    </p>
                  </transition>
                </div>

                <div class="grid-item">
                  <label
                    >Danh mục áp dụng <span class="text-danger">*</span></label
                  >
                  <div class="donvi-row">
                    <v-select
                      append-to-body
                      class="flex-grow-1 modern-select"
                      :class="{ 'input-invalid-vs': errors.danhSachDanhMucs }"
                      :options="danhSach"
                      label="tenDanhMuc"
                      :reduce="(dm) => dm.id"
                      placeholder="-- Chọn danh mục --"
                      @update:modelValue="addDanhMucToUnit"
                    >
                    </v-select>
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
                    <div class="donvi-tags mt-2">
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
                      @input="errors.dinhLuongs = ''"
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
                        class="tag-donvi"
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

            <div class="modal-footer">
              <button class="btn-cancel" @click="closeAddDonVi">Hủy bỏ</button>
              <button class="btn-primary" @click="submitAddDonVi">
                <i class="bi bi-check2-all"></i>
                <div class="btn-light-sweep"></div>
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
/* Chỉ cuộn nội dung phần bảng, cố định phân trang ở đáy */
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

/* Font chữ to 32px chuẩn form */
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

/* Cột Danh mục (Có icon) */
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
.badge-count {
  background: #f1f5f9;
  padding: 4px 10px;
  border-radius: 12px;
  font-weight: 700;
  color: #475569;
  font-size: 12px;
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
:deep(.vs__dropdown-toggle:hover) {
  border-color: #c8d0dc !important;
}
:deep(.vs--open .vs__dropdown-toggle) {
  border-color: var(--color-primary) !important;
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12) !important;
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
  z-index: 999;
}
.modal-box {
  background: #fff;
  border-radius: 7px;
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
  font-size: 25px;
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
.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
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

.btn-cancel {
  background: #f1f5f9;
  color: #334155;
  border: 1px solid #e2e8f0;
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}
.btn-cancel:hover {
  background: #e2e8f0;
}

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
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  padding: 0 10px;
  height: 42px;
  border-radius: 8px;
  background: #fdf2f2;
  transition: 0.2s;
}
.create-new-item:hover {
  background: #fee2e2;
}
.create-new-item .icon {
  font-size: 16px;
  font-weight: bold;
}

.btn-add-dv {
  width: 42px;
  height: 42px;
  background: #e4dada;
  color: rgb(231, 51, 51);
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 20px;
  font-weight: bold;
  transition: 0.2s;
}
.btn-add-dv:hover {
  background: #f64b4b;
  color: white;
}

.donvi-tags-container {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px dashed #e2e8f0;
}
.tag-title {
  font-weight: 600;
  color: #475569;
  font-size: 14px;
  margin-bottom: 12px;
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
