<script setup>
import { ref, watch, onMounted, onUnmounted } from "vue";
import { Modal } from "bootstrap";
import { useUtils } from "@/utils/helper";
import iconAdd from "@/assets/icons/add.png";
import { themKhuVuc } from "@/api/admin/khuvuc/khuVucApi";
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";

const { notify } = useUtils();

/* PROPS */
const props = defineProps({
  danhSachKhuVuc: {
    type: Array,
    default: () => [],
  },
  tangDangChon: {
    type: [Number, String],
    default: null,
  },
});

/* EMITS */
const emit = defineEmits(["close", "submit", "reload-khu-vuc"]);

/* FORM STATE */
const form = ref({
  id_khu_vuc: null,
  ten_ban_an: "",
  suc_chua_toi_da: 4,
  trang_thai: 1,
  loai_ban: "OFFLINE",
});

const errors = ref({
  id_khu_vuc: "",
  ten_ban_an: "",
});

/* AUTO CHỌN KHU VỰC */
watch(
  () => props.tangDangChon,
  (newVal) => {
    if (newVal) {
      form.value.id_khu_vuc = Number(newVal);
    }
  },
  { immediate: true },
);

const showThemTang = ref(false);

const formKhuVuc = ref({
  tenKhuVuc: "",
  viTri: "",
  moTa: "",
  trangThai: 1,
});

const errorsKhuVuc = ref({
  tenKhuVuc: "",
  viTri: "",
});

const submitKhuVuc = async () => {
  errorsKhuVuc.value.tenKhuVuc = "";
  errorsKhuVuc.value.viTri = "";
  let hasError = false;

  if (!formKhuVuc.value.tenKhuVuc) {
    errorsKhuVuc.value.tenKhuVuc = "Vui lòng nhập tên khu vực";
    hasError = true;
  }
  if (!formKhuVuc.value.viTri) {
    errorsKhuVuc.value.viTri = "Vui lòng nhập vị trí khu vực";
    hasError = true;
  }
  if (hasError) return;

  const isDuplicate = props.danhSachKhuVuc.some(
    (t) =>
      t.tenKhuVuc.trim().toLowerCase() ===
      formKhuVuc.value.tenKhuVuc.trim().toLowerCase(),
  );
  if (isDuplicate) {
    errorsKhuVuc.value.tenKhuVuc = "Tên khu vực đã tồn tại";
    return;
  }

  try {
    const res = await themKhuVuc(formKhuVuc.value);

    notify.success("Thêm khu vực thành công!");

    showThemTang.value = false;

    // báo cha reload và truyền id mới lên
    emit("reload-khu-vuc", res.data.id);

    // reset form
    formKhuVuc.value = {
      tenKhuVuc: "",
      viTri: "",
      moTa: "",
      trangThai: 1,
    };
  } catch (e) {
    const errorMessage =
      typeof e.response?.data === "string"
        ? e.response.data
        : e.response?.data?.message;
    notify.error(errorMessage || "Thêm khu vực thất bại");
  }
};

// Hàm set error từ component cha
const setErrors = (errObj) => {
  Object.assign(errors.value, errObj);
};
defineExpose({ setErrors });

/* SUBMIT (ĐÃ MAP ĐÚNG ENTITY) */
const submitForm = async () => {
  errors.value.id_khu_vuc = "";
  errors.value.ten_ban_an = "";
  let hasError = false;

  if (!form.value.id_khu_vuc) {
    errors.value.id_khu_vuc = "Vui lòng chọn khu vực";
    hasError = true;
  }

  if (
    form.value.ten_ban_an === "" ||
    form.value.ten_ban_an === null ||
    form.value.ten_ban_an === undefined
  ) {
    errors.value.ten_ban_an = "Vui lòng nhập tên bàn";
    hasError = true;
  }

  if (hasError) return;

  const payload = {
    tenBanAn: String(form.value.ten_ban_an).trim(),
    sucChuaToiDa: form.value.suc_chua_toi_da,
    trangThai: form.value.trang_thai,
    loaiBan: form.value.loai_ban || "OFFLINE",
    khuVuc: {
      id: form.value.id_khu_vuc,
    },
  };

  emit("submit", payload); // cha sẽ xử lý API và đóng modal khi thành công
};

/* MODAL CONTROL */
const modalThemBanRef = ref(null);
let modalInstance = null;

const modalThemKhuVucRef = ref(null);
let modalKhuVucInstance = null;

onMounted(() => {
  modalInstance = new Modal(modalThemBanRef.value);
  modalInstance.show();

  modalThemBanRef.value.addEventListener("hidden.bs.modal", () => {
    emit("close");
  });
});

onUnmounted(() => {
  if (modalInstance) {
    modalInstance.dispose();
  }
  if (modalKhuVucInstance) {
    modalKhuVucInstance.dispose();
  }
});

const closeModal = () => {
  if (modalInstance) {
    modalInstance.hide();
  }
};

watch(showThemTang, (val) => {
  if (val) {
    if (!modalKhuVucInstance) {
      modalKhuVucInstance = new Modal(modalThemKhuVucRef.value);
      modalThemKhuVucRef.value.addEventListener("hidden.bs.modal", () => {
        showThemTang.value = false;
      });
    }
    modalKhuVucInstance.show();
  } else {
    if (modalKhuVucInstance) {
      modalKhuVucInstance.hide();
    }
  }
});
</script>

<template>
  <Teleport to="body">
    <!-- MODAL THÊM BÀN -->
    <div class="modal fade" tabindex="-1" ref="modalThemBanRef">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content premium-modal-content">
          <div class="modal-header premium-modal-header">
            <h5 class="fw-bold mb-0">Thêm bàn mới</h5>
            <button
              type="button"
              class="btn-close"
              @click="closeModal"
            ></button>
          </div>
          <div class="modal-body premium-modal-body">
            <!-- KHU VỰC -->
            <div class="mb-3">
              <label class="form-label">Khu vực </label>
              <div class="d-flex gap-2 align-items-center">
                <div class="w-100" style="flex: 1">
                  <v-select
                    class="modern-select"
                    :class="{ 'input-invalid': errors.id_khu_vuc }"
                    v-model.number="form.id_khu_vuc"
                    :options="danhSachKhuVuc"
                    :reduce="(kv) => kv.id"
                    label="tenKhuVuc"
                    placeholder="-- Chọn khu vực --"
                    @update:modelValue="errors.id_khu_vuc = ''"
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
                <button
                  class="icon-btn primary"
                  data-tooltip="Thêm tầng"
                  @click="showThemTang = true"
                >
                  <img :src="iconAdd" alt="Thêm tầng" />
                </button>
              </div>
              <transition name="shake">
                <p v-if="errors.id_khu_vuc" class="error-hint mt-1 mb-0">
                  <i class="bi bi-exclamation-circle"></i>
                  {{ errors.id_khu_vuc }}
                </p>
              </transition>
            </div>

            <!-- TÊN BÀN -->
            <div class="mb-3">
              <label class="form-label">Tên bàn</label>
              <input
                v-model="form.ten_ban_an"
                type="text"
                class="custom-input"
                :class="{ 'input-invalid': errors.ten_ban_an }"
                placeholder="VD: 01 hoặc VIP 1"
                @input="errors.ten_ban_an = ''"
              />
              <transition name="shake">
                <p v-if="errors.ten_ban_an" class="error-hint mt-1 mb-0">
                  <i class="bi bi-exclamation-circle"></i>
                  {{ errors.ten_ban_an }}
                </p>
              </transition>
            </div>

            <!-- SỨC CHỨA -->
            <div class="mb-3">
              <label class="form-label">Sức chứa tối đa</label>
              <v-select
                class="modern-select"
                v-model.number="form.suc_chua_toi_da"
                :options="[
                  { label: '2 chỗ', value: 2 },
                  { label: '4 chỗ', value: 4 },
                  { label: '6 chỗ', value: 6 },
                  { label: '8 chỗ', value: 8 },
                  { label: '16 chỗ', value: 16 },
                ]"
                :reduce="(option) => option.value"
                label="label"
                placeholder="-- Chọn sức chứa --"
              >
                <template #no-options="{ search, searching }">
                  <template v-if="searching"
                    >Không tìm thấy "<b>{{ search }}</b
                    >"</template
                  >
                  <em v-else style="opacity: 0.5">Không có dữ liệu</em>
                </template>
              </v-select>
              <small class="text-muted">
                ≥ 12 chỗ sẽ được xem là bàn lớn
              </small>
            </div>

            <!-- LOẠI BÀN -->
            <div class="mb-3">
              <label class="form-label">Loại bàn</label>
              <v-select
                class="modern-select"
                v-model="form.loai_ban"
                :options="[
                  { label: 'Bàn Offline (Tại chỗ)', value: 'OFFLINE' },
                  { label: 'Bàn Online (Đặt trước)', value: 'ONLINE' },
                  { label: 'Bàn Dự Phòng', value: 'DU_PHONG' },
                ]"
                :reduce="(option) => option.value"
                label="label"
                placeholder="-- Chọn loại bàn --"
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

            <!-- TRẠNG THÁI -->
            <div class="mb-3">
              <label class="form-label">Trạng thái</label>
              <v-select
                class="modern-select"
                v-model.number="form.trang_thai"
                :options="[
                  { label: 'Trống', value: 1 },
                  { label: 'Ngừng hoạt động', value: 0 },
                ]"
                :reduce="(option) => option.value"
                label="label"
                placeholder="-- Chọn trạng thái --"
              >
              </v-select>
            </div>
          </div>
          <div class="modal-footer premium-modal-footer">
            <button type="button" class="btn-cancel" @click="closeModal">
              Hủy bỏ
            </button>
            <button type="button" class="btn-primary" @click="submitForm">
              <i class="ri-check-double-line me-1"></i>
              <div class="btn-light-sweep"></div>
              Xác nhận
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- MODAL THÊM KHU VỰC -->
    <div class="modal fade" tabindex="-1" ref="modalThemKhuVucRef">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content premium-modal-content">
          <div class="modal-header premium-modal-header">
            <h5 class="fw-bold mb-0">
              <i class="ri-add-line me-1"></i> Thêm khu vực
            </h5>
            <button
              type="button"
              class="btn-close"
              @click="showThemTang = false"
            ></button>
          </div>
          <div class="modal-body premium-modal-body">
            <div class="mb-3">
              <label class="form-label">Tên khu vực</label>
              <input
                v-model="formKhuVuc.tenKhuVuc"
                class="custom-input"
                :class="{ 'input-invalid': errorsKhuVuc.tenKhuVuc }"
                placeholder="VD: Tầng 01"
                @input="errorsKhuVuc.tenKhuVuc = ''"
              />
              <transition name="shake">
                <p v-if="errorsKhuVuc.tenKhuVuc" class="error-hint mt-1 mb-0">
                  <i class="bi bi-exclamation-circle"></i>
                  {{ errorsKhuVuc.tenKhuVuc }}
                </p>
              </transition>
            </div>
            <div class="mb-3">
              <label class="form-label">Vị trí</label>
              <input
                v-model="formKhuVuc.viTri"
                class="custom-input"
                :class="{ 'input-invalid': errorsKhuVuc.viTri }"
                placeholder="VD: Tầng 01"
                @input="errorsKhuVuc.viTri = ''"
              />
              <transition name="shake">
                <p v-if="errorsKhuVuc.viTri" class="error-hint mt-1 mb-0">
                  <i class="bi bi-exclamation-circle"></i>
                  {{ errorsKhuVuc.viTri }}
                </p>
              </transition>
            </div>
            <div class="mb-3">
              <label class="form-label">Mô tả</label>
              <textarea
                v-model="formKhuVuc.moTa"
                class="custom-input"
              ></textarea>
            </div>
          </div>
          <div class="modal-footer premium-modal-footer">
            <button
              type="button"
              class="btn-cancel"
              @click="showThemTang = false"
            >
              Hủy bỏ
            </button>
            <button type="button" class="btn-primary" @click="submitKhuVuc">
              <i class="ri-check-double-line me-1"></i>
              <div class="btn-light-sweep"></div>
              Xác nhận
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
/* OVERLAY */
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

/* FORM */
.them-ban {
  width: 420px;
  max-width: 95%;
  animation: zoomIn 0.25s ease;
}

/* ERROR CSS */
.error-hint {
  color: #dc3545;
  font-size: 13px;
  margin-top: 4px;
}
.input-invalid {
  border-color: #dc3545 !important;
  box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.1) !important;
}
.shake-enter-active {
  animation: shake 0.5s;
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

/* FIX ICON GIGANTIC SIZE */
.icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  padding: 6px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: #f8fafc;
  cursor: pointer;
}
.icon-btn img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

/* FORM */
.them-ban {
  width: 420px;
  max-width: 95%;
  animation: zoomIn 0.25s ease;
}

/* ================== PREMIUM MODAL ================== */
.premium-modal-content {
  background: #ffffff;
  border-radius: 7px;
  border: none;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  animation: modalFadeIn 0.3s ease-out;
}

@keyframes modalFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.premium-modal-header {
  background: #f8fafc;
  padding: 16px 24px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.premium-modal-header h5 {
  font-size: 25px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.premium-modal-body {
  padding: 24px;
}

.premium-modal-footer {
  padding: 16px 24px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* CUSTOM INPUT ĐỒNG BỘ */
.custom-input {
  width: 100%;
  height: 42px;
  box-sizing: border-box;
  padding: 0 12px;
  border: 1px solid #e5e7eb;
  border-radius: 7px;
  font-size: 14px;
  outline: none;
  transition: all 0.22s ease;
}
.custom-input:hover {
  border-color: #c8d0dc;
}
.custom-input:focus {
  border-color: var(--color-primary, #e65a5a);
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12);
}
.custom-input:read-only {
  background: #f3f4f6;
  color: #9ca3af;
  cursor: not-allowed;
}

.form-label {
  font-weight: 600;
  font-size: 14px;
  color: #4b5563;
  margin-bottom: 6px;
  display: block;
}

.btn {
  padding: 8px 16px;
  border-radius: 7px;
  font-weight: 500;
  transition: all 0.2s ease;
  cursor: pointer;
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
  border-color: var(--color-primary, #e65a5a) !important;
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
</style>
