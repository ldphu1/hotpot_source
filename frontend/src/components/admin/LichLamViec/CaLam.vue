<script setup>
import { ref, onMounted, computed, watch, nextTick } from "vue";
import { caLamApi } from "@/api/admin/lichlamviec/caLamApi";
import { useUtils } from "@/utils/helper";
import iconAdd from "@/assets/icons/add.png";
import iconEdit from "@/assets/icons/edit.png";
import iconChange from "@/assets/icons/sync.png";
import iconDanhSach from "@/assets/icons/danh-sach.png";
import iconView from "@/assets/icons/view.png";
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";
import flatpickr from "flatpickr";
import "flatpickr/dist/flatpickr.min.css";

const caLamList = ref([]);
const showAddCaLamModal = ref(false);
const newCaLam = ref({
  tenCaLam: "",
  thoiGianBatDau: "",
  thoiGianKetThuc: "",
  trangThai: 1,
});

const showEditCaLamModal = ref(false);
const caLamDangSua = ref({});

const initTimePicker = (selector, defaultTime, onChangeCallback) => {
  flatpickr(selector, {
    enableTime: true,
    noCalendar: true,
    dateFormat: "H:i",
    time_24hr: true,
    defaultDate: defaultTime,
    onChange: (selectedDates, dateStr) => {
      onChangeCallback(dateStr);
    },
  });
};

watch(showAddCaLamModal, (val) => {
  if (val) {
    nextTick(() => {
      initTimePicker(
        ".time-picker-add-start",
        newCaLam.value.thoiGianBatDau,
        (val) => (newCaLam.value.thoiGianBatDau = val),
      );
      initTimePicker(
        ".time-picker-add-end",
        newCaLam.value.thoiGianKetThuc,
        (val) => (newCaLam.value.thoiGianKetThuc = val),
      );
    });
  }
});

watch(showEditCaLamModal, (val) => {
  if (val) {
    nextTick(() => {
      initTimePicker(
        ".time-picker-edit-start",
        caLamDangSua.value.thoiGianBatDau,
        (val) => (caLamDangSua.value.thoiGianBatDau = val),
      );
      initTimePicker(
        ".time-picker-edit-end",
        caLamDangSua.value.thoiGianKetThuc,
        (val) => (caLamDangSua.value.thoiGianKetThuc = val),
      );
    });
  }
});

const { notify } = useUtils();
const isQuanLy = computed(() => localStorage.getItem("role") === "ADMIN");

const fetchData = async () => {
  try {
    const res = await caLamApi.layDanhSach();
    if (res.data) {
      caLamList.value = res.data;
    }
  } catch (error) {
    console.error("Lỗi khi lấy danh sách ca làm:", error);
  }
};

const handleAddCaLam = async () => {
  if (!isQuanLy.value) {
    notify.error(
      "Chỉ nhân viên có chức vụ Quản lý (Chức vụ 1) mới được thực hiện thao tác này",
    );
    return;
  }
  if (
    !newCaLam.value.tenCaLam ||
    !newCaLam.value.thoiGianBatDau ||
    !newCaLam.value.thoiGianKetThuc
  ) {
    notify.error("Vui lòng điền đầy đủ thông tin ca làm!");
    return;
  }
  try {
    const payload = { ...newCaLam.value };
    if (payload.thoiGianBatDau && payload.thoiGianBatDau.length === 5)
      payload.thoiGianBatDau += ":00";
    if (payload.thoiGianKetThuc && payload.thoiGianKetThuc.length === 5)
      payload.thoiGianKetThuc += ":00";

    const res = await caLamApi.them(payload);
    if (res.data) {
      notify.success("Thêm ca làm thành công!");
      showAddCaLamModal.value = false;
      newCaLam.value = {
        tenCaLam: "",
        thoiGianBatDau: "",
        thoiGianKetThuc: "",
        trangThai: 1,
      };
      fetchData();
    }
  } catch (error) {
    console.error("Lỗi khi thêm ca làm:", error);
    notify.error(
      error.response?.data?.message ||
        error.response?.data ||
        "Thêm ca làm thất bại",
    );
  }
};

const openEditModal = (ca, event) => {
  if (event) {
    event.currentTarget.dispatchEvent(new Event("mouseleave"));
  }
  caLamDangSua.value = { ...ca };
  showEditCaLamModal.value = true;
};

const handleEditCaLam = async () => {
  if (
    !caLamDangSua.value.tenCaLam ||
    !caLamDangSua.value.thoiGianBatDau ||
    !caLamDangSua.value.thoiGianKetThuc
  ) {
    notify.error("Vui lòng điền đầy đủ thông tin ca làm!");
    return;
  }
  try {
    const payload = { ...caLamDangSua.value };
    if (payload.thoiGianBatDau && payload.thoiGianBatDau.length === 5)
      payload.thoiGianBatDau += ":00";
    if (payload.thoiGianKetThuc && payload.thoiGianKetThuc.length === 5)
      payload.thoiGianKetThuc += ":00";

    const res = await caLamApi.sua(payload.id, payload);
    if (res.data) {
      notify.success("Cập nhật ca làm thành công!");
      showEditCaLamModal.value = false;
      fetchData();
    }
  } catch (error) {
    console.error("Lỗi khi cập nhật ca làm:", error);
    notify.error(
      error.response?.data?.message ||
        error.response?.data ||
        "Cập nhật ca làm thất bại",
    );
  }
};

const doiTrangThai = async (ca, event) => {
  if (event) {
    event.currentTarget.dispatchEvent(new Event("mouseleave"));
  }
  const isInactive = ca.trangThai === 0;
  const ok = await notify.confirm(
    isInactive
      ? "Kích hoạt lại ca làm này?"
      : "Chuyển ca làm này sang ngừng hoạt động?",
  );
  if (!ok) return;

  try {
    const updatedCaLam = { ...ca, trangThai: isInactive ? 1 : 0 };
    await caLamApi.sua(ca.id, updatedCaLam);
    notify.success("Đổi trạng thái thành công!");
    fetchData();
  } catch (error) {
    console.error("Lỗi khi đổi trạng thái:", error);
    notify.error("Đổi trạng thái thất bại");
  }
};

/* ===== PHÂN TRANG ===== */
const currentPage = ref(1);
const pageSize = ref(5);
const totalItems = computed(() => caLamList.value.length);
const totalPages = computed(
  () => Math.ceil(totalItems.value / pageSize.value) || 1,
);

const danhSachHienThi = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return caLamList.value.slice(start, start + pageSize.value);
});

const goFirst = () => (currentPage.value = 1);
const goPrev = () => {
  if (currentPage.value > 1) currentPage.value--;
};
const goNext = () => {
  if (currentPage.value < totalPages.value) currentPage.value++;
};
const goLast = () => (currentPage.value = totalPages.value);

onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="ca-lam-container fade-in d-flex flex-column h-100">
    <div
      class="box flex-grow-1 d-flex flex-column mb-0 mt-3 shadow-sm rounded-3"
      style="overflow: visible !important"
    >
      <div
        class="box-header box-header-flex p-0 pb-3 border-bottom d-flex justify-content-between align-items-center"
      >
        <h3
          class="table-title m-0 fw-bold text-dark d-flex align-items-center gap-2"
        >
          <span class="icon-wrapper-small">
            <img :src="iconDanhSach" class="icon-title-small" />
          </span>
          Danh sách ca làm
        </h3>
        <div class="header-actions">
          <button v-if="isQuanLy" class="add-btn" @click="showAddCaLamModal = true">
            <img :src="iconAdd" alt="" />
            <span>Thêm ca làm</span>
          </button>
        </div>
      </div>
      <div
        class="table-content table-container flex-grow-1 p-0 mt-3 overflow-auto custom-scrollbar"
      >
        <table class="table-main">
          <thead>
            <tr>
              <th width="20">STT</th>
              <th width="80">Tên Ca Làm</th>
              <th width="80">Giờ Bắt Đầu</th>
              <th width="80">Giờ Kết Thúc</th>
              <th width="100">Ngày tạo</th>
              <th width="80">Trạng thái</th>
              <th width="50" class="text-center">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="danhSachHienThi.length === 0">
              <td colspan="7" class="empty-table text-center py-4">
                Không có ca làm nào
              </td>
            </tr>
            <tr v-for="(ca, index) in danhSachHienThi" :key="ca.id">
              <td class="text-center fw-bold">
                {{ (currentPage - 1) * pageSize + index + 1 }}
              </td>
              <td class="text-center fw-semibold text-dark">
                {{ ca.tenCaLam }}
              </td>
              <td>
                <span class="badge bg-light border text-dark fs-6 px-3 py-2">
                  <i class="ri-time-line me-1 text-secondary"></i>
                  {{
                    ca.thoiGianBatDau
                      ? ca.thoiGianBatDau.substring(0, 5)
                      : "--:--"
                  }}
                </span>
              </td>
              <td>
                <span class="badge bg-light border text-dark fs-6 px-3 py-2">
                  <i class="ri-time-line me-1 text-secondary"></i>
                  {{
                    ca.thoiGianKetThuc
                      ? ca.thoiGianKetThuc.substring(0, 5)
                      : "--:--"
                  }}
                </span>
              </td>
              <td class="text-muted">
                {{
                  ca.ngayTao
                    ? new Date(ca.ngayTao).toLocaleDateString("vi-VN") +
                      " " +
                      new Date(ca.ngayTao).toLocaleTimeString("vi-VN", {
                        hour: "2-digit",
                        minute: "2-digit",
                      })
                    : "---"
                }}
              </td>
              <td>
                <span
                  class="status-text"
                  :class="
                    ca.trangThai === 1 ? 'status-success' : 'status-error'
                  "
                >
                  {{
                    ca.trangThai === 1 ? "Đang hoạt động" : "Ngừng hoạt động"
                  }}
                </span>
              </td>
              <td class="text-center">
                <div class="action-cell d-flex justify-content-center gap-2">
                  <button
                    class="btn-action-icon border-0 d-flex align-items-center justify-content-center"
                    @click="openEditModal(ca, $event)"
                    v-tooltip="'Xem chi tiết'"
                  >
                    <img :src="iconView" class="img-icon-action" />
                  </button>
                  <button
                    v-if="isQuanLy && ca.trangThai === 1"
                    class="btn-action-icon border-0 d-flex align-items-center justify-content-center"
                    @click="doiTrangThai(ca, $event)"
                    v-tooltip="'Đổi trạng thái'"
                  >
                    <img :src="iconChange" class="img-icon-action" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div
        class="d-flex justify-content-between align-items-center mt-3 p-0 pt-3"
      >
        <div class="d-flex align-items-center gap-2">
          <span class="text-nowrap">Hiển thị</span>
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
          Hiển thị {{ danhSachHienThi.length }} / {{ totalItems }} ca làm
        </div>
      </div>
    </div>

    <!-- ADD CA LÀM MODAL -->
    <Transition name="fade">
      <Teleport to="body">
        <div
          v-if="showAddCaLamModal"
          class="modal-overlay"
          @click.self="showAddCaLamModal = false"
        >
          <div class="modal-box premium-modal-content" style="width: 500px">
            <div class="modal-header premium-modal-header">
              <h5 class="fw-bold mb-0">
                <i class="ri-add-line me-1"></i> Thêm Ca Làm
              </h5>
              <button type="button" class="btn-close-modal" @click="showAddCaLamModal = false">✕</button>
            </div>
            <div class="modal-body premium-modal-body">
              <div class="mb-3">
                <label class="form-label fw-bold"
                  >Tên ca làm <span class="text-danger">*</span></label
                >
                <input
                  type="text"
                  class="form-control"
                  v-model="newCaLam.tenCaLam"
                  placeholder="VD: Ca Sáng, Ca Chiều..."
                />
              </div>
              <div class="row">
                <div class="col-6 mb-3">
                  <label class="form-label fw-bold"
                    >Giờ bắt đầu <span class="text-danger">*</span></label
                  >
                  <div class="position-relative">
                    <input
                      type="text"
                      class="form-control time-picker-add-start bg-white"
                      v-model="newCaLam.thoiGianBatDau"
                      placeholder="--:--"
                    />
                    <i
                      class="ri-time-line position-absolute top-50 end-0 translate-middle-y me-2 text-muted"
                    ></i>
                  </div>
                </div>
                <div class="col-6 mb-3">
                  <label class="form-label fw-bold"
                    >Giờ kết thúc <span class="text-danger">*</span></label
                  >
                  <div class="position-relative">
                    <input
                      type="text"
                      class="form-control time-picker-add-end bg-white"
                      v-model="newCaLam.thoiGianKetThuc"
                      placeholder="--:--"
                    />
                    <i
                      class="ri-time-line position-absolute top-50 end-0 translate-middle-y me-2 text-muted"
                    ></i>
                  </div>
                </div>
              </div>
            </div>
            <div class="modal-footer premium-modal-footer">
              <button type="button" class="btn-cancel" @click="showAddCaLamModal = false">
                Hủy bỏ
              </button>
              <button type="button" class="btn-primary" @click="handleAddCaLam">
                <i class="ri-check-double-line me-1"></i>
                <div class="btn-light-sweep"></div>
                Xác nhận
              </button>
            </div>
          </div>
        </div>
      </Teleport>
    </Transition>

    <!-- EDIT CA LÀM MODAL -->
    <Transition name="fade">
      <Teleport to="body">
        <div
          v-if="showEditCaLamModal"
          class="modal-overlay"
          @click.self="showEditCaLamModal = false"
        >
          <div class="modal-box premium-modal-content" style="width: 500px">
            <div class="modal-header premium-modal-header">
              <h5 class="fw-bold mb-0">
                <i class="ri-edit-line me-1"></i> Chi Tiết Ca Làm
              </h5>
              <button type="button" class="btn-close-modal" @click="showEditCaLamModal = false">✕</button>
            </div>
            <div class="modal-body premium-modal-body">
              <div class="mb-3">
                <label class="form-label fw-bold">Tên ca làm</label>
                <input
                  type="text"
                  class="form-control"
                  v-model="caLamDangSua.tenCaLam"
                  disabled
                />
              </div>
              <div class="row">
                <div class="col-6 mb-3">
                  <label class="form-label fw-bold">Giờ bắt đầu</label>
                  <div class="position-relative">
                    <input
                      type="text"
                      class="form-control bg-light"
                      v-model="caLamDangSua.thoiGianBatDau"
                      disabled
                    />
                    <i
                      class="ri-time-line position-absolute top-50 end-0 translate-middle-y me-2 text-muted"
                    ></i>
                  </div>
                </div>
                <div class="col-6 mb-3">
                  <label class="form-label fw-bold">Giờ kết thúc</label>
                  <div class="position-relative">
                    <input
                      type="text"
                      class="form-control bg-light"
                      v-model="caLamDangSua.thoiGianKetThuc"
                      disabled
                    />
                    <i
                      class="ri-time-line position-absolute top-50 end-0 translate-middle-y me-2 text-muted"
                    ></i>
                  </div>
                </div>
              </div>
              <div class="mb-3">
                <label class="form-label fw-bold">Trạng thái</label>
                <input
                  type="text"
                  class="form-control"
                  :value="
                    caLamDangSua.trangThai === 1
                      ? 'Đang hoạt động'
                      : 'Ngừng hoạt động'
                  "
                  disabled
                />
              </div>
              <div class="row">
                <div class="col-6 mb-3">
                  <label class="form-label fw-bold">Ngày tạo</label>
                  <input
                    type="text"
                    class="form-control"
                    :value="
                      caLamDangSua.ngayTao
                        ? new Date(caLamDangSua.ngayTao).toLocaleString('vi-VN')
                        : '---'
                    "
                    disabled
                  />
                </div>
                <div class="col-6 mb-3">
                  <label class="form-label fw-bold">Ngày kết thúc</label>
                  <input
                    type="text"
                    class="form-control"
                    :value="
                      caLamDangSua.ngayCapNhat
                        ? new Date(caLamDangSua.ngayCapNhat).toLocaleString(
                            'vi-VN',
                          )
                        : '---'
                    "
                    disabled
                  />
                </div>
              </div>
            </div>
            <div class="modal-footer premium-modal-footer">
              <button type="button" class="btn-cancel" @click="showEditCaLamModal = false">
                Đóng
              </button>
              <button type="button" class="btn-primary" @click="handleEditCaLam">
                <i class="ri-check-double-line me-1"></i>
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
@import url("https://cdn.jsdelivr.net/npm/remixicon@4.2.0/fonts/remixicon.css");

.btn {
  border-radius: 7px !important;
}

/* ===== BUTTON MODERN (Cancel + Primary) ===== */
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

/* Nút Đóng / Hủy (Cancel) */
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

/* Nút Lưu / Cập nhật (Primary) */
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

/* Hiệu ứng khi Click */
.btn-cancel:active,
.btn-primary:active {
  transform: translateY(1px) scale(0.97);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

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

.ca-lam-container {
  height: 100%;
}

.table-main {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 7px;
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
  font-size: 16px;
  border-bottom: none;
}
.table-main td {
  border-bottom: 1px solid #f0f0f0;
  font-size: 15px;
}

/* Modal styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
}
.modal-box {
  background-color: #fff;
  border-radius: 7px;
  overflow: hidden;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1),
    0 10px 10px -5px rgba(0, 0, 0, 0.04);
  transform: translateY(0);
  transition: all 0.3s ease;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
}
.btn-close-modal {
  background: #f3f4f6;
  font-size: 18px;
  color: #475569;
  width: 40px;
  height: 40px;
  border-radius: 7px;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.06);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.25s ease;
  padding: 0;
  opacity: 1;
}
.btn-close-modal:hover {
  background: rgba(230, 90, 90, 0.12);
  color: #d73737;
  box-shadow: 0 8px 18px rgba(230, 90, 90, 0.15);
  transform: translateY(-2px);
}
.modal-body {
  padding: 24px;
  max-height: 70vh;
  overflow-y: auto;
}
.modal-footer {
  padding: 16px 24px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.add-btn {
  background: #eef2ff;
  color: #d73737;
  border: none;
  padding: 9px 14px;
  border-radius: 7px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  transition: all 0.2s ease;
  cursor: pointer;
}
.add-btn img {
  width: 16px;
  height: 16px;
  object-fit: contain;
}
.add-btn:hover {
  background: #dfe7ff;
  transform: translateY(-1px);
}

.btn-action-icon {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50px !important;
  border: none;
  background: #f2f5f7;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-action-icon:hover {
  background: #eef2ff !important;
  transform: translateY(-1px);
}

.img-icon-action {
  width: 17px;
  height: 17px;
  object-fit: contain;
}

/* ===== PHÂN TRANG ===== */
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
  border-radius: 7px;
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
  display: block;
}
.table-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
  display: flex;
  align-items: center;
}

/* TRẠNG THÁI (PILL NỀN MỜ) */
.status-text {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 125px;
  padding: 4px 12px;
  border-radius: 7px;
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
</style>

<style>
/* ===== SELECT 2 ĐỒNG BỘ (GLOBAL FOR APPEND-TO-BODY) ===== */
.v-select,
.modern-select {
  background: #fff;
  width: 100%;
  --vs-dropdown-option--active-bg: #f3f4f6;
  --vs-dropdown-option--active-color: #eb6464;
  --vs-border-color: #e5e7eb;
  --vs-border-radius: 10px;
  --vs-dropdown-box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1);
}
.modern-select .vs__dropdown-toggle {
  border: 1px solid #e5e7eb !important;
  border-radius: 10px !important;
  padding: 0 !important;
  height: 42px;
  display: flex;
  align-items: center;
  transition: all 0.2s ease;
  box-shadow: none !important;
}
.modern-select .vs__search,
.modern-select .vs__search:focus {
  border: none !important;
  margin: 0 !important;
  padding: 0 12px !important;
  background: transparent !important;
  box-shadow: none !important;
}
.modern-select .vs__selected {
  position: absolute;
  margin: 0 !important;
  padding-left: 12px !important;
  color: #374151;
  border: none !important;
}
.modern-select .vs__search::placeholder {
  color: #9ca3af !important;
}
.modern-select .vs__dropdown-toggle:hover {
  border-color: #c8d0dc !important;
}
.modern-select.vs--open .vs__dropdown-toggle {
  border-color: #dc3545 !important;
  box-shadow: 0 0 0 4px rgba(220, 53, 69, 0.12) !important;
}
.vs__dropdown-menu {
  border: 1px solid #e5e7eb !important;
  border-radius: 10px !important;
  margin-top: 8px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1) !important;
  padding: 6px !important;
  z-index: 10000 !important;
}
.vs__dropdown-option {
  border-radius: 8px !important;
  padding: 8px 12px !important;
  margin-bottom: 2px !important;
  color: #4b5563;
}
.vs__dropdown-option--highlight {
  background: #f3f4f6 !important;
  color: #eb6464 !important;
}
.vs__dropdown-option--selected {
  background: #f3f5f9 !important;
  color: #fc0101 !important;
}
.modern-select .vs__actions {
  padding-right: 10px !important;
}
.modern-select .vs__open-indicator {
  fill: #9ca3af !important;
  transform: scale(0.7);
}
.modern-select .vs__clear {
  fill: #9ca3af !important;
}

/* CUSTOM SMALL FLATPICKR TIME */
.flatpickr-calendar.noCalendar {
  width: 140px !important;
  min-width: 140px !important;
  max-width: 140px !important;
  padding: 0 !important;
}
.flatpickr-time {
  height: 40px !important;
  max-height: 40px !important;
  line-height: 40px !important;
}
.flatpickr-time .numInputWrapper {
  height: 40px !important;
}
.flatpickr-time input {
  font-size: 16px !important;
  height: 40px !important;
}
</style>
