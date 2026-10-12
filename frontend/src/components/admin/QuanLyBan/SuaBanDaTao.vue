<script setup>
import { ref, watch, onMounted, onUnmounted } from "vue";
import { Modal } from "bootstrap";
import { useUtils } from "@/utils/helper";
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";

const { notify } = useUtils();

/* ================= PROPS & EMITS ================= */
const props = defineProps({
  show: Boolean,
  ban: Object,
  danhSachTang: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(["close", "save", "deleted"]);

/* ================= FORM ================= */
const form = ref({
  id: null,
  maBanAn: "",
  tenBanAn: "",
  sucChuaToiDa: 1,
  trangThai: 1,
  loaiBan: "OFFLINE",
  khuVuc: {
    id: null,
  },
});

const errors = ref({
  idKhuVuc: "",
  tenBanAn: "",
});

/* ================= WATCH ================= */
watch(
  () => props.ban,
  (ban) => {
    if (!ban) return;

    form.value = {
      id: ban.id,
      maBanAn: ban.maBanAn,
      tenBanAn: ban.tenBanAn,
      sucChuaToiDa: ban.sucChuaToiDa,
      trangThai: ban.trangThai,
      loaiBan: ban.loaiBan || "OFFLINE",
      khuVuc: {
        id: ban.khuVuc?.id || null,
      },
    };
  },
  { immediate: true },
);

/* ================= ACTIONS ================= */
// Hàm set error từ component cha
const setErrors = (errObj) => {
  Object.assign(errors.value, errObj);
};
defineExpose({ setErrors });

const dong = () => emit("close");

const luu = async () => {
  errors.value.idKhuVuc = "";
  errors.value.tenBanAn = "";
  let hasError = false;

  if (!form.value.khuVuc.id) {
    errors.value.idKhuVuc = "Vui lòng chọn tầng";
    hasError = true;
  }
  if (!form.value.tenBanAn) {
    errors.value.tenBanAn = "Vui lòng nhập tên bàn";
    hasError = true;
  }

  if (hasError) return;

  emit("save", { ...form.value });
};

const xoaBanHandler = async () => {
  const dangNgung = form.value.trangThai === 0;
  const msg = dangNgung
    ? "Bạn có chắc muốn kích hoạt lại bàn này không?"
    : "Bạn có chắc muốn đổi trạng thái bàn này sang 'Không sử dụng' không?";

  if (!(await notify.confirm(msg))) return;

  // Đổi trạng thái: nếu đang ngừng (0) -> trống (1), ngược lại -> ngừng (0)
  const updatedForm = { ...form.value, trangThai: dangNgung ? 1 : 0 };
  emit("save", updatedForm);
  dong();
};

/* ================= MODAL ================= */
const modalSuaBanRef = ref(null);
let modalInstance = null;

onMounted(() => {
  modalInstance = new Modal(modalSuaBanRef.value);
  if (props.show) {
    modalInstance.show();
  }

  modalSuaBanRef.value.addEventListener("hidden.bs.modal", () => {
    emit("close");
  });
});

watch(
  () => props.show,
  (val) => {
    if (val) {
      modalInstance?.show();
    } else {
      modalInstance?.hide();
    }
  },
);

onUnmounted(() => {
  if (modalInstance) {
    modalInstance.dispose();
  }
});
</script>

<template>
  <Teleport to="body">
    <!-- MODAL SỬA BÀN -->
    <div class="modal fade" tabindex="-1" ref="modalSuaBanRef">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content premium-modal-content">
          <div class="modal-header premium-modal-header">
            <h5 class="fw-bold mb-0">Cập nhật thông tin bàn</h5>
            <button type="button" class="btn-close" @click="dong"></button>
          </div>
          <div class="modal-body premium-modal-body">
            <!-- KHU VỰC -->
            <div class="mb-3">
              <label class="form-label">Tầng</label>
              <v-select
                v-model.number="form.khuVuc.id"
                :options="danhSachTang"
                :reduce="(tang) => tang.id"
                label="tenKhuVuc"
                placeholder="-- Chọn tầng --"
                class="modern-select"
                :class="{ 'input-invalid': errors.idKhuVuc }"
                @update:modelValue="errors.idKhuVuc = ''"
              >
                <template #no-options="{ search, searching }">
                  <template v-if="searching"
                    >Không tìm thấy "<b>{{ search }}</b
                    >"</template
                  >
                  <em v-else style="opacity: 0.5">Không có dữ liệu</em>
                </template>
              </v-select>
              <transition name="shake">
                <p v-if="errors.idKhuVuc" class="error-hint mt-1 mb-0">
                  <i class="bi bi-exclamation-circle"></i> {{ errors.idKhuVuc }}
                </p>
              </transition>
            </div>

            <!-- MÃ BÀN -->
            <div class="mb-3">
              <label class="form-label">Mã bàn</label>
              <input v-model="form.maBanAn" class="custom-input" readonly />
            </div>

            <!-- TÊN BÀN -->
            <div class="mb-3">
              <label class="form-label">Tên bàn</label>
              <input
                v-model="form.tenBanAn"
                class="custom-input"
                :class="{ 'input-invalid': errors.tenBanAn }"
                @input="errors.tenBanAn = ''"
              />
              <transition name="shake">
                <p v-if="errors.tenBanAn" class="error-hint mt-1 mb-0">
                  <i class="bi bi-exclamation-circle"></i> {{ errors.tenBanAn }}
                </p>
              </transition>
            </div>

            <!-- SỨC CHỨA -->
            <div class="mb-3">
              <label class="form-label">Sức chứa tối đa</label>
              <v-select
                v-model.number="form.sucChuaToiDa"
                :options="[
                  { label: '2 chỗ', value: 2 },
                  { label: '4 chỗ', value: 4 },
                  { label: '6 chỗ', value: 6 },
                  { label: '8 chỗ', value: 8 },
                  { label: '16 chỗ', value: 16 },
                ]"
                :reduce="(option) => option.value"
                label="label"
                class="modern-select"
              >
                <template #no-options="{ search, searching }">
                  <template v-if="searching"
                    >Không tìm thấy "<b>{{ search }}</b
                    >"</template
                  >
                  <em v-else style="opacity: 0.5">Không có dữ liệu</em>
                </template>
              </v-select>
              <small class="text-muted">≥ 12 chỗ được xem là bàn lớn</small>
            </div>

            <!-- LOẠI BÀN -->
            <div class="mb-3">
              <label class="form-label">Loại bàn</label>
              <v-select
                v-model="form.loaiBan"
                :options="[
                  { label: 'Bàn Offline (Tại chỗ)', value: 'OFFLINE' },
                  { label: 'Bàn Online (Đặt trước)', value: 'ONLINE' },
                  { label: 'Bàn Dự Phòng', value: 'DU_PHONG' },
                ]"
                :reduce="(option) => option.value"
                label="label"
                class="modern-select"
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
                v-model="form.trangThai"
                :options="[
                  { label: 'Ngừng hoạt động', value: 0 },
                  { label: 'Trống', value: 1 },
                  { label: 'Đã đặt', value: 2 },
                  { label: 'Đang sử dụng', value: 3 },
                  { label: 'Đang dọn', value: 4 },
                ]"
                :reduce="(option) => option.value"
                label="label"
                class="modern-select"
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
          <div class="modal-footer premium-modal-footer">
            <button
              type="button"
              :class="form.trangThai === 0 ? 'btn-activate' : 'btn-cancel'"
              @click="xoaBanHandler"
            >
              <i
                :class="
                  form.trangThai === 0
                    ? 'ri-checkbox-circle-line'
                    : 'ri-forbid-line'
                "
              ></i>
              {{ form.trangThai === 0 ? "Hoạt động" : "Ngừng hoạt động" }}
            </button>
            <button type="button" class="btn-primary" @click="luu">
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
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
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
  border-radius: 6px;
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

.btn-activate {
  background: #dcfce7;
  color: #166534;
  border: 1px solid #bbf7d0;
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}
.btn-activate:hover {
  background: #bbf7d0;
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
