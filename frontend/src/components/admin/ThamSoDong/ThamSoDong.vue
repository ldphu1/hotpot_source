<script setup>
import { reactive, ref, computed, onMounted } from "vue";
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";

/* ===== ICON ===== */
import iconFolder from "@/assets/icons/danh-muc.png";
import iconEdit from "@/assets/icons/edit.png";
import iconAdd from "@/assets/icons/add.png";
import iconDanhSach from "@/assets/icons/danh-sach.png";
import iconRefresh from "@/assets/icons/refresh.png";

import { useUtils } from "@/utils/helper";
import {
  getAllThamSoDong,
  getDetailThamSo,
  createThamSo,
  updateThamSo,
} from "@/api/admin/thamsodong/thamSoDongApi";

const { notify, nprogress } = useUtils();

const keyword = ref("");
const trangThai = ref("");
const selectedMaThamSo = ref("");

const danhSach = ref([]);
const thamSoFilter = ref([]);

const showAddModal = ref(false);
const isEdit = ref(false);
const editingId = ref(null);

const currentPage = ref(1);
const pageSize = ref(5);

const formThamSo = reactive({
  maThamSo: "",
  tenThamSo: "",
  giaTri: "",
  kieuDuLieu: "",
  nhom: "",
  moTa: "",
  trangThai: true,
});

const errors = reactive({
  maThamSo: "",
  tenThamSo: "",
  giaTri: "",
  kieuDuLieu: "",
});

const validateThamSo = () => {
  errors.maThamSo = "";
  errors.tenThamSo = "";
  errors.giaTri = "";
  errors.kieuDuLieu = "";

  let valid = true;

  if (!formThamSo.maThamSo?.trim()) {
    errors.maThamSo = "Mã tham số không được để trống";
    valid = false;
  }
  if (!formThamSo.tenThamSo?.trim()) {
    errors.tenThamSo = "Tên tham số không được để trống";
    valid = false;
  }
  if (
    formThamSo.giaTri === "" ||
    formThamSo.giaTri === null ||
    formThamSo.giaTri === undefined
  ) {
    errors.giaTri = "Giá trị không được để trống";
    valid = false;
  }
  if (!formThamSo.kieuDuLieu) {
    errors.kieuDuLieu = "Vui lòng chọn kiểu dữ liệu";
    valid = false;
  }

  return valid;
};

onMounted(() => {
  loadThamSo();
});

const resetForm = () => {
  formThamSo.maThamSo = "";
  formThamSo.tenThamSo = "";
  formThamSo.giaTri = "";
  formThamSo.kieuDuLieu = "";
  formThamSo.nhom = "";
  formThamSo.moTa = "";
  formThamSo.trangThai = true;
  errors.maThamSo = "";
  errors.tenThamSo = "";
  errors.giaTri = "";
  errors.kieuDuLieu = "";
};

const loadThamSo = async () => {
  try {
    const res = await getAllThamSoDong();
    danhSach.value = Array.isArray(res.data) ? res.data : [];
    thamSoFilter.value = Array.isArray(res.data) ? res.data : [];
  } catch (error) {
    console.error("Không tải được tham số", error);
    notify.error("Không tải được danh sách tham số");
  }
};

const reloadThamSo = async () => {
  try {
    await loadThamSo();
    keyword.value = "";
    trangThai.value = "";
    selectedMaThamSo.value = "";
    currentPage.value = 1;
  } catch (e) {
    console.error(e);
  }
};

const formatGiaTri = (ts) => {
  if (ts.giaTri == null || ts.giaTri === "") return "--";

  switch (ts.maThamSo) {
    case "VAT_PERCENT":
    case "VAT_DONG_GOI":
    case "TI_LE_TICH_DIEM":
    case "PHAN_TRAM_COC":
      return ts.giaTri + " %";
    case "DAT_TRUOC_TOI_THIEU":
    case "THOI_GIAN_SU_DUNG_BAN_MAC_DINH":
    case "THOI_GIAN_GIU_BAN":
    case "THOI_GIAN_HUY_HOAN_COC":
    case "THOI_GIAN_CHO_THANH_TOAN_COC":
      return ts.giaTri + " phút";
    case "DAT_TRUOC_TOI_DA_NGAY":
      return ts.giaTri + " ngày";
    case "SO_GHE_DU_TOI_DA":
      return ts.giaTri + " ghế";
    default:
      return ts.giaTri;
  }
};

const openAddModal = () => {
  resetForm();
  isEdit.value = false;
  editingId.value = null;
  showAddModal.value = true;
};

const closeModal = () => {
  showAddModal.value = false;
  isEdit.value = false;
  editingId.value = null;
  resetForm();
};

const openEditThamSo = async (id) => {
  nprogress.start();
  try {
    const res = await getDetailThamSo(id);
    const data = res.data;

    formThamSo.maThamSo = data.maThamSo || "";
    formThamSo.tenThamSo = data.tenThamSo || "";
    formThamSo.giaTri = data.giaTri || "";
    formThamSo.kieuDuLieu = data.kieuDuLieu || "";
    formThamSo.nhom = data.nhom || "";
    formThamSo.moTa = data.moTa || "";
    formThamSo.trangThai = data.trangThai ?? true;

    editingId.value = id;
    isEdit.value = true;
    showAddModal.value = true;
  } catch (error) {
    console.error("Không lấy được chi tiết tham số", error);
    notify.error("Không lấy được chi tiết tham số");
  } finally {
    nprogress.done();
  }
};

const addThamSoSubmit = async () => {
  if (!validateThamSo()) return;
  if (!(await notify.confirm("Bạn có chắc muốn thêm tham số này không?")))
    return;

  nprogress.start();
  try {
    await createThamSo({
      maThamSo: formThamSo.maThamSo,
      tenThamSo: formThamSo.tenThamSo,
      giaTri: formThamSo.giaTri,
      kieuDuLieu: formThamSo.kieuDuLieu,
      nhom: formThamSo.nhom,
      moTa: formThamSo.moTa,
      trangThai: formThamSo.trangThai,
    });

    notify.success("Thêm tham số thành công!");
    await reloadThamSo();
    closeModal();
  } catch (e) {
    const errorMessage =
      typeof e.response?.data === "string"
        ? e.response.data
        : e.response?.data?.message;
    notify.error(errorMessage || "Có lỗi xảy ra khi thêm tham số");
  } finally {
    nprogress.done();
  }
};

const updateThamSoSubmit = async () => {
  if (!validateThamSo()) return;
  if (!(await notify.confirm("Bạn có chắc muốn cập nhật tham số này không?")))
    return;

  nprogress.start();
  try {
    await updateThamSo(editingId.value, {
      maThamSo: formThamSo.maThamSo,
      tenThamSo: formThamSo.tenThamSo,
      giaTri: formThamSo.giaTri,
      kieuDuLieu: formThamSo.kieuDuLieu,
      nhom: formThamSo.nhom,
      moTa: formThamSo.moTa,
      trangThai: formThamSo.trangThai,
    });

    notify.success("Cập nhật tham số thành công!");
    await reloadThamSo();
    closeModal();
  } catch (e) {
    const errorMessage =
      typeof e.response?.data === "string"
        ? e.response.data
        : e.response?.data?.message;
    notify.error(errorMessage || "Có lỗi xảy ra khi cập nhật tham số");
  } finally {
    nprogress.done();
  }
};

const chuyenTrangThai = async (ts) => {
  const action = ts.trangThai ? "tắt" : "bật";
  if (
    !(await notify.confirm(
      `Bạn có chắc muốn ${action} tham số "${ts.tenThamSo}" không?`,
    ))
  ) {
    return;
  }

  nprogress.start();
  try {
    await updateThamSo(ts.id, {
      maThamSo: ts.maThamSo,
      tenThamSo: ts.tenThamSo,
      giaTri: ts.giaTri,
      kieuDuLieu: ts.kieuDuLieu,
      nhom: ts.nhom,
      moTa: ts.moTa,
      trangThai: !ts.trangThai,
    });

    notify.success(`Đã ${action} tham số "${ts.tenThamSo}" thành công!`);
    await reloadThamSo();
  } catch (error) {
    const errorMessage =
      typeof error.response?.data === "string"
        ? error.response.data
        : error.response?.data?.message;
    notify.error(errorMessage || "Có lỗi xảy ra khi đổi trạng thái");
    console.error(error);
  } finally {
    nprogress.done();
  }
};

const danhSachLoc = computed(() => {
  return danhSach.value.filter((ts) => {
    const kw = keyword.value.trim().toLowerCase();

    const matchKeyword =
      !kw ||
      String(ts.maThamSo || "")
        .toLowerCase()
        .includes(kw) ||
      String(ts.tenThamSo || "")
        .toLowerCase()
        .includes(kw) ||
      String(ts.nhom || "")
        .toLowerCase()
        .includes(kw);

    const matchMa =
      selectedMaThamSo.value === "" ||
      String(ts.maThamSo) === String(selectedMaThamSo.value);

    const matchTrangThai =
      trangThai.value === ""
        ? true
        : String(ts.trangThai) === String(trangThai.value);

    return matchKeyword && matchMa && matchTrangThai;
  });
});

const onFilter = () => {
  currentPage.value = 1;
};

const totalItems = computed(() => danhSachLoc.value.length);
const totalPages = computed(() =>
  Math.max(1, Math.ceil(totalItems.value / pageSize.value)),
);

const danhSachHienThi = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return danhSachLoc.value.slice(start, start + pageSize.value);
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
  <div class="quan-ly-tham-so">
    <div class="box">
      <div class="box-header">
        <h2 class="main-title">
          <span class="icon-wrapper">
            <img src="@/assets/icons/qlthamso.png" alt="Tham số" />
          </span>
          Quản Lý Tham Số
        </h2>
        <p>Cấu hình các tham số động cho toàn hệ thống</p>
      </div>
    </div>

    <div class="filter-box">
      <div class="filter-item">
        <label>Tìm kiếm</label>
        <input
          class="custom-input"
          placeholder="Mã, tên, nhóm tham số..."
          v-model="keyword"
          @input="onFilter"
        />
      </div>

      <div class="filter-item">
        <label>Tham số</label>
        <v-select
          class="modern-select"
          v-model="selectedMaThamSo"
          :options="[
            { label: 'Tất cả', value: '' },
            ...thamSoFilter.map((ts) => ({
              label: `${ts.maThamSo} - ${ts.tenThamSo}`,
              value: ts.maThamSo,
            })),
          ]"
          :reduce="(opt) => opt.value"
          placeholder="Chọn tham số"
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
            { label: 'Hoạt động', value: 'true' },
            { label: 'Không hoạt động', value: 'false' },
          ]"
          :reduce="(opt) => opt.value"
          placeholder="Tất cả trạng thái"
          @update:modelValue="onFilter"
          :clearable="false"
        >
        </v-select>
      </div>
    </div>

    <div class="box">
      <div class="box-header box-header-flex">
        <h3 class="table-title">
          <span class="icon-wrapper-small">
            <img :src="iconDanhSach" class="icon-title-small" />
          </span>
          Danh sách tham số
        </h3>

        <div class="header-actions">
          <button
            class="refresh-btn"
            @click="reloadThamSo"
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
              <th>STT</th>
              <th style="text-align: left">Mã tham số</th>
              <th style="text-align: left">Tên tham số</th>
              <th style="text-align: right">Giá trị</th>
              <th>Kiểu dữ liệu</th>
              <th>Nhóm</th>
              <th style="text-align: left">Mô tả</th>
              <th>Trạng thái</th>
              <th style="text-align: center">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(ts, index) in danhSachHienThi" :key="ts.id">
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
              <td style="text-align: left" class="text-bold">
                {{ ts.maThamSo }}
              </td>
              <td style="text-align: left">
                <div class="dm-name">
                  <img :src="iconFolder" class="dm-icon" />
                  <span class="text-bold">{{ ts.tenThamSo }}</span>
                </div>
              </td>
              <td style="text-align: right" class="text-bold">
                {{ formatGiaTri(ts) }}
              </td>
              <td>
                <span class="type-badge">{{ ts.kieuDuLieu || "--" }}</span>
              </td>
              <td>{{ ts.nhom || "--" }}</td>
              <td class="desc" style="text-align: left" :title="ts.moTa">
                {{ ts.moTa || "--" }}
              </td>
              <td>
                <span
                  class="status-text"
                  :class="ts.trangThai ? 'status-success' : 'status-error'"
                >
                  {{ ts.trangThai ? "Hoạt động" : "Không hoạt động" }}
                </span>
              </td>
              <td class="text-center">
                <div class="action-cell">
                  <button
                    class="btn-action-icon"
                    @click="openEditThamSo(ts.id)"
                    v-tooltip="'Chỉnh sửa'"
                  >
                    <img :src="iconEdit" class="img-icon-action" />
                  </button>
                  <label class="switch" v-tooltip="'Đổi trạng thái'">
                    <input
                      type="checkbox"
                      :checked="ts.trangThai"
                      @change="chuyenTrangThai(ts)"
                    />
                    <span class="slider"></span>
                  </label>
                </div>
              </td>
            </tr>
            <tr v-if="danhSachHienThi.length === 0">
              <td colspan="9" class="empty-table">
                🔍 Không tìm thấy tham số phù hợp
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
          Hiển thị {{ danhSachHienThi.length }} / {{ totalItems }} tham số
        </div>
      </div>
    </div>

    <Transition name="fade">
      <Teleport to="body">
        <div v-if="showAddModal" class="modal-overlay" @click.self="closeModal">
          <div class="modal-box modal-wide">
            <div class="modal-header">
              <h3>{{ isEdit ? "Cập nhật tham số" : "Thêm tham số mới" }}</h3>
              <button class="btn-icon-close" @click="closeModal">×</button>
            </div>

            <div class="modal-body">
              <div class="grid-form">
                <div class="grid-item">
                  <label>Mã tham số <span class="text-danger">*</span></label>
                  <input
                    class="custom-input"
                    :class="{ 'input-invalid': errors.maThamSo }"
                    v-model="formThamSo.maThamSo"
                    placeholder="Ví dụ: VAT_PERCENT"
                    :disabled="isEdit"
                    @input="errors.maThamSo = ''"
                  />
                  <transition name="shake">
                    <p v-if="errors.maThamSo" class="error-hint">
                      <i class="bi bi-exclamation-circle"></i>
                      {{ errors.maThamSo }}
                    </p>
                  </transition>
                </div>

                <div class="grid-item">
                  <label>Tên tham số <span class="text-danger">*</span></label>
                  <input
                    class="custom-input"
                    :class="{ 'input-invalid': errors.tenThamSo }"
                    v-model="formThamSo.tenThamSo"
                    placeholder="Nhập tên tham số..."
                    @input="errors.tenThamSo = ''"
                  />
                  <transition name="shake">
                    <p v-if="errors.tenThamSo" class="error-hint">
                      <i class="bi bi-exclamation-circle"></i>
                      {{ errors.tenThamSo }}
                    </p>
                  </transition>
                </div>

                <div class="grid-item">
                  <label>Giá trị <span class="text-danger">*</span></label>
                  <input
                    class="custom-input"
                    :class="{ 'input-invalid': errors.giaTri }"
                    v-model="formThamSo.giaTri"
                    placeholder="Ví dụ: 10 hoặc 0.2"
                    @input="errors.giaTri = ''"
                  />
                  <transition name="shake">
                    <p v-if="errors.giaTri" class="error-hint">
                      <i class="bi bi-exclamation-circle"></i>
                      {{ errors.giaTri }}
                    </p>
                  </transition>
                </div>

                <div class="grid-item">
                  <label>Kiểu dữ liệu <span class="text-danger">*</span></label>
                  <v-select
                    class="modern-select"
                    :class="{ 'select-invalid': errors.kieuDuLieu }"
                    v-model="formThamSo.kieuDuLieu"
                    :options="['STRING', 'INT', 'DOUBLE', 'BOOLEAN']"
                    placeholder="Chọn kiểu dữ liệu"
                    :clearable="false"
                    @update:modelValue="errors.kieuDuLieu = ''"
                  />
                  <transition name="shake">
                    <p v-if="errors.kieuDuLieu" class="error-hint">
                      <i class="bi bi-exclamation-circle"></i>
                      {{ errors.kieuDuLieu }}
                    </p>
                  </transition>
                </div>

                <div class="grid-item">
                  <label>Nhóm</label>
                  <input
                    class="custom-input"
                    v-model="formThamSo.nhom"
                    placeholder="Ví dụ: THANH_TOAN, DAT_BAN..."
                  />
                </div>

                <div class="grid-item">
                  <label>Trạng thái</label>
                  <v-select
                    class="modern-select"
                    v-model="formThamSo.trangThai"
                    :options="[
                      { label: 'Hoạt động', value: true },
                      { label: 'Không hoạt động', value: false },
                    ]"
                    :reduce="(opt) => opt.value"
                    label="label"
                    placeholder="Chọn trạng thái"
                    :clearable="false"
                  />
                </div>

                <div class="grid-item full-col">
                  <label>Mô tả chi tiết</label>
                  <textarea
                    class="custom-input textarea"
                    v-model="formThamSo.moTa"
                    rows="3"
                    placeholder="Mô tả ý nghĩa của tham số..."
                  ></textarea>
                </div>
              </div>
            </div>

            <div class="modal-footer">
              <button class="btn-cancel" @click="closeModal">Hủy bỏ</button>
              <button
                class="btn-primary"
                @click="isEdit ? updateThamSoSubmit() : addThamSoSubmit()"
              >
                <i class="ri-check-double-line" style="margin-right: 4px"></i>
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
.custom-input.textarea {
  height: auto;
  padding: 12px;
  resize: vertical;
}

/* ===== HEADER ACTIONS ĐỒNG BỘ ===== */
.header-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.add-btn,
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
  table-layout: auto;
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
  font-size: 14px;
  border-bottom: none;
}
.table-main td {
  border-bottom: 1px solid #f0f0f0;
  font-size: 13px;
}
.table-main th:nth-child(1),
.table-main td:nth-child(1) {
  width: 50px;
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
.text-primary {
  color: #000000;
}
.text-danger {
  color: #d73737;
}

.type-badge {
  background: #f1f5f9;
  padding: 4px 10px;
  border-radius: 6px;
  font-weight: 600;
  color: #475569;
  font-size: 12px;
}
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
.desc {
  max-width: 250px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #666;
}

/* TRẠNG THÁI (PILL NỀN MỜ) */
.status-text {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 110px;
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
:deep(.vs--open .vs__dropdown-toggle) {
  border-color: #00d1f3 !important;
  box-shadow: 0 0 0 3px rgba(0, 209, 243, 0.1) !important;
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
  z-index: 999999;
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
/* Bố cục nhóm nút (Modal Footer) */
.modal-footer {
  display: flex;
  justify-content: flex-end; /* Căn lề phải */
  align-items: center;
  gap: 12px; /* Khoảng cách giữa 2 nút */
  padding: 16px 24px;
  background-color: #f8fafc; /* Nền xám nhạt cho footer */
  border-top: 1px solid #e2e8f0; /* Đường kẻ ngang phân cách */
}

/* Nút Đóng / Hủy bỏ (Cancel) */
.btn-cancel {
  background-color: #ffffff;
  color: #4b5563; /* Xám đậm */
  border: 1px solid #d1d5db; /* Viền xám nhạt */
  padding: 10px 20px;
  font-size: 15px;
  font-weight: 500;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-cancel:hover {
  background-color: #f3f4f6; /* Nền xám nhạt khi hover */
  color: #1f2937;
}

/* Nút Lưu thay đổi / Submit (Primary) */
.btn-primary {
  background-color: #d73737; /* Màu đỏ chủ đạo */
  color: #ffffff;
  border: 1px solid #d73737;
  padding: 10px 20px;
  font-size: 15px;
  font-weight: 500;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative; /* Dành cho hiệu ứng sweep (nếu dùng) */
  overflow: hidden;
}

.btn-primary:hover {
  background-color: #b92b2b; /* Đỏ sậm hơn khi hover */
  border-color: #b92b2b;
  box-shadow: 0 4px 6px -1px rgba(215, 55, 55, 0.2); /* Đổ bóng nhẹ */
}

/* Hiệu ứng khi bấm click (Active) */
.btn-cancel:active,
.btn-primary:active {
  transform: scale(0.97);
}

.btn-cancel,
.btn-primary {
  position: relative;
  overflow: hidden;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1); /* Hiệu ứng gia tốc mượt */
}

/* 1. HIỆU ỨNG NÚT LƯU / CẬP NHẬT (PRIMARY) */

/* Hiệu ứng tia sáng quét qua (Shimmer/Shine) khi hover */
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
  transition: all 0.6s ease-in-out; /* Tia sáng chạy từ trái qua phải */
}

/* Hiệu ứng khi Hover chuột vào nút Primary */
.btn-primary:hover {
  background-color: #b92b2b !important;
  border-color: #b92b2b !important;
  transform: translateY(-2px); /* Nhấc nhẹ nút lên tạo cảm giác nổi */
  box-shadow: 0 6px 16px rgba(215, 55, 55, 0.35); /* Đổ bóng phát sáng màu đỏ mịn */
}

/* Hiệu ứng nhúc nhích nhẹ icon bên trong nút khi hover */
.btn-primary:hover i {
  transform: scale(1.15);
  transition: transform 0.2s ease;
}

/* 2. HIỆU ỨNG NÚT ĐÓNG / HỦY (CANCEL) */

/* Hiệu ứng khi Hover chuột vào nút Cancel */
.btn-cancel:hover {
  background-color: #f8fafc !important;
  border-color: #cbd5e1 !important;
  color: #0f172a !important;
  transform: translateY(-2px); /* Nhấc nhẹ nút lên */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); /* Đổ bóng xám rất nhẹ */
}

/* 3. HIỆU ỨNG KHI CLICK CHUỘT (ACTIVE STATE) CHO CẢ 2 NÚT */

.btn-cancel:active,
.btn-primary:active {
  transform: translateY(1px) scale(0.97) !important; /* Ấn lún nút xuống và thu nhỏ nhẹ */
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1) !important; /* Giảm đổ bóng khi nhấn xuống */
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
.grid-item.full-col {
  grid-column: 1 / -1;
}
.grid-item label {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
/* ===== VALIDATION ERROR (giống ThemBan.vue) ===== */
.error-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #dc3545;
  font-size: 13px;
  margin-top: 6px;
  padding: 5px 10px;
  background-color: #fff5f5;
  border-radius: 6px;
}

.input-invalid {
  border-color: #dc3545 !important;
  box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.1) !important;
}

.select-invalid :deep(.vs__dropdown-toggle) {
  border-color: #dc3545 !important;
  box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.1) !important;
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
