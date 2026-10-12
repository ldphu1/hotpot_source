<script setup>
import { reactive, watch, computed, onMounted, ref, onBeforeUnmount } from "vue";
import flatpickr from "flatpickr";
import "flatpickr/dist/flatpickr.min.css";
import { Vietnamese } from "flatpickr/dist/l10n/vn.js";

const props = defineProps({
  modelValue: Object,
  isEdit: {
    type: Boolean,
    default: false,
  },
});
const emit = defineEmits(["update:modelValue", "valid"]);

/* FORM */
const form = reactive({
  tenDot: "",
  giaTriGiam: 0,
  ngayBatDau: "",
  ngayKetThuc: "",
  moTa: "",
});

/* ERROR */
const errors = reactive({
  tenDot: "",
  giaTriGiam: "",
  ngay: "",
});

/* TOUCHED FIELDS */
const touched = reactive({
  tenDot: false,
  giaTriGiam: false,
  ngay: false,
});

/* PICKER REFS & INSTANCES */
const startPickerRef = ref(null);
const endPickerRef = ref(null);
let startPicker = null;
let endPicker = null;

/* SYNC FROM PROPS */
watch(
  () => props.modelValue,
  (val) => {
    Object.assign(form, val || {});
    // Ensure defaults for loaiGiam and giamToiDa
    if (form.loaiGiam !== 0) form.loaiGiam = 0;
    if (form.giamToiDa !== 0) form.giamToiDa = 0;

    // Update flatpickr dates if instances are active
    if (startPicker && form.ngayBatDau) {
      startPicker.setDate(form.ngayBatDau);
    }
    if (endPicker && form.ngayKetThuc) {
      endPicker.setDate(form.ngayKetThuc);
    }
  },
  { immediate: true, deep: true },
);

/* VALIDATE */
function validate() {
  errors.tenDot = "";
  errors.giaTriGiam = "";
  errors.ngay = "";

  if (!form.tenDot?.trim()) {
    errors.tenDot = "Tên đợt không được để trống";
  } else if (form.tenDot.trim().length > 100) {
    errors.tenDot = "Tên đợt không được vượt quá 100 ký tự";
  }

  if (form.giaTriGiam === "" || form.giaTriGiam == null) {
    errors.giaTriGiam = "Giá trị giảm không được để trống";
  } else {
    const valueNum = Number(form.giaTriGiam);

    if (isNaN(valueNum) || valueNum <= 0) {
      errors.giaTriGiam = "Mức giảm phải lớn hơn 0%";
    } else if (valueNum > 100) {
      errors.giaTriGiam = "Mức giảm không được vượt quá 100%";
    }
  }

  if (!form.ngayBatDau || !form.ngayKetThuc) {
    errors.ngay = "Vui lòng chọn đầy đủ thời gian";
  } else {
    const start = new Date(form.ngayBatDau.replace(" ", "T"));
    const end = new Date(form.ngayKetThuc.replace(" ", "T"));
    const now = new Date();

    if (!props.isEdit && start.getTime() < now.getTime() - 5 * 60 * 1000) {
      errors.ngay = "Thời gian bắt đầu không được nhỏ hơn hiện tại";
    } else if (end <= start) {
      errors.ngay = "Thời gian kết thúc phải sau thời gian bắt đầu";
    }
  }
}

/* VALID */
const isValid = computed(() => {
  const valueNum = Number(form.giaTriGiam);
  return (
    !errors.tenDot &&
    !errors.giaTriGiam &&
    !errors.ngay &&
    form.tenDot?.trim() &&
    valueNum > 0 &&
    valueNum <= 100 &&
    form.ngayBatDau &&
    form.ngayKetThuc
  );
});

/* WATCH */
watch(
  form,
  (val) => {
    validate();
    emit("update:modelValue", { ...val });
    emit("valid", isValid.value);
  },
  { deep: true },
);

/* INIT */
onMounted(() => {
  // Initialize start date picker
  startPicker = flatpickr(startPickerRef.value, {
    enableTime: true,
    dateFormat: "Y-m-d H:i",
    time_24hr: true,
    locale: Vietnamese,
    onChange: (selectedDates, dateStr) => {
      form.ngayBatDau = dateStr;
      touched.ngay = true;
    },
  });

  // Initialize end date picker
  endPicker = flatpickr(endPickerRef.value, {
    enableTime: true,
    dateFormat: "Y-m-d H:i",
    time_24hr: true,
    locale: Vietnamese,
    onChange: (selectedDates, dateStr) => {
      form.ngayKetThuc = dateStr;
      touched.ngay = true;
    },
  });

  // Prepopulate values if edit mode has loaded values
  if (form.ngayBatDau) {
    startPicker.setDate(form.ngayBatDau);
  }
  if (form.ngayKetThuc) {
    endPicker.setDate(form.ngayKetThuc);
  }

  validate();
  emit("valid", isValid.value);
});

onBeforeUnmount(() => {
  if (startPicker) startPicker.destroy();
  if (endPicker) endPicker.destroy();
});

const showErrors = () => {
  touched.tenDot = true;
  touched.giaTriGiam = true;
  touched.ngay = true;
};

defineExpose({ showErrors });
</script>

<template>
  <div class="luxury-form-card">
    <h3 class="card-title">
      <span class="icon-indicator">
        <i class="ri-coupon-3-line"></i>
      </span>
      Thông tin đợt giảm giá
    </h3>

    <div class="form-grid">
      <!-- Tên đợt giảm giá -->
      <div class="form-group full-width">
        <label class="form-label">
          Tên chương trình khuyến mãi <span class="batbuoc">*</span>
        </label>
        <div class="input-wrapper">
          <i class="ri-edit-box-line prefix-icon"></i>
          <input
            v-model="form.tenDot"
            type="text"
            placeholder="Ví dụ: Đón hè rực rỡ, Siêu tiệc buffet..."
            :class="{ 'input-invalid': touched.tenDot && errors.tenDot }"
            @input="touched.tenDot = true"
            @blur="touched.tenDot = true"
          />
        </div>
        <transition name="shake">
          <p class="error-hint" v-if="touched.tenDot && errors.tenDot">
            <i class="bi bi-exclamation-circle"></i> {{ errors.tenDot }}
          </p>
        </transition>
      </div>

      <!-- Giá trị giảm -->
      <div class="form-group full-width">
        <label class="form-label">
          Mức giảm (%) <span class="batbuoc">*</span>
        </label>
        <div class="input-wrapper group-suffix">
          <i class="ri-percent-line prefix-icon"></i>
          <input
            type="number"
            min="1"
            max="100"
            v-model.number="form.giaTriGiam"
            placeholder="Nhập mức giảm từ 1 đến 100..."
            :class="{ 'input-invalid': touched.giaTriGiam && errors.giaTriGiam }"
            @input="touched.giaTriGiam = true"
            @blur="touched.giaTriGiam = true"
          />
          <span class="suffix-tag">%</span>
        </div>
        <transition name="shake">
          <p class="error-hint" v-if="touched.giaTriGiam && errors.giaTriGiam">
            <i class="bi bi-exclamation-circle"></i> {{ errors.giaTriGiam }}
          </p>
        </transition>
      </div>

      <!-- Thời gian bắt đầu & kết thúc -->
      <div class="row-dates full-width">
        <div class="form-group">
          <label class="form-label">
            Thời gian bắt đầu <span class="batbuoc">*</span>
          </label>
          <div class="input-wrapper">
            <i class="ri-play-circle-line prefix-icon"></i>
            <input
              ref="startPickerRef"
              type="text"
              placeholder="Chọn ngày & giờ bắt đầu"
              :class="{ 'input-invalid': touched.ngay && errors.ngay }"
              style="padding-right: 40px;"
              @blur="touched.ngay = true"
            />
            <i class="bi bi-calendar-event datetime-icon" style="position: absolute; right: 14px; color: #9ca3af; pointer-events: none; font-size: 16px;"></i>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">
            Thời gian kết thúc <span class="batbuoc">*</span>
          </label>
          <div class="input-wrapper">
            <i class="ri-stop-circle-line prefix-icon"></i>
            <input
              ref="endPickerRef"
              type="text"
              placeholder="Chọn ngày & giờ kết thúc"
              :class="{ 'input-invalid': touched.ngay && errors.ngay }"
              style="padding-right: 40px;"
              @blur="touched.ngay = true"
            />
            <i class="bi bi-calendar-event datetime-icon" style="position: absolute; right: 14px; color: #9ca3af; pointer-events: none; font-size: 16px;"></i>
          </div>
        </div>
      </div>

      <!-- Mô tả chi tiết -->
      <div class="form-group full-width">
        <label class="form-label">Mô tả chương trình</label>
        <textarea
          v-model="form.moTa"
          rows="4"
          placeholder="Nhập ghi chú hoặc thông tin chi tiết về đợt giảm giá này..."
        ></textarea>
      </div>
    </div>

  <!-- Thông báo lỗi chung -->
    <transition name="shake">
      <p class="error-hint" style="margin-top: 15px;" v-if="touched.ngay && errors.ngay">
        <i class="bi bi-exclamation-circle"></i> {{ errors.ngay }}
      </p>
    </transition>
  </div>
</template>

<style scoped>
/* ===== DESIGN SYSTEM & COMPONENT CONTAINER ===== */
.luxury-form-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 28px;
  height: 100%;
  border: 1px solid #f0f0f0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
  font-family: var(--font-family);
  transition: all 0.3s ease;
}

.luxury-form-card:hover {
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.05);
}

.card-title {
  font-size: 19px;
  font-weight: 600;
  color: #1f2937;
  margin-top: 0;
  margin-bottom: 26px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.icon-indicator {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background-color: rgba(230, 90, 90, 0.08);
  color: var(--color-primary, #e65a5a);
  border-radius: 8px;
  font-size: 18px;
}

/* ===== GRID LAYOUT ===== */
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.full-width {
  grid-column: span 2;
}

.row-dates {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

/* ===== LABELS & ASTRIES ===== */
.form-label {
  display: block;
  font-size: 14px;
  color: #374151;
  margin-bottom: 8px;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.batbuoc {
  color: var(--color-primary, #e65a5a);
  margin-left: 2px;
}

/* ===== INPUTS & TEXTAREAS ===== */
.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.prefix-icon {
  position: absolute;
  left: 14px;
  color: #9ca3af;
  font-size: 18px;
  pointer-events: none;
}

.input-wrapper input {
  width: 100%;
  padding: 12px 14px 12px 42px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background-color: #fafafa;
  font-size: 14px;
  color: #1f2937;
  transition: all 0.25s ease;
  font-family: var(--font-family);
}

.input-wrapper.group-suffix input {
  padding-right: 48px;
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
}

.suffix-tag {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  border-left: none;
  border-top-right-radius: 10px;
  border-bottom-right-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #4b5563;
  height: 46px; /* Matches input height including padding and borders */
}

/* Textarea styling */
textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background-color: #fafafa;
  font-size: 14px;
  color: #1f2937;
  transition: all 0.25s ease;
  font-family: var(--font-family);
  resize: vertical;
}

/* Focus and Hover Effects */
.input-wrapper input:hover,
textarea:hover {
  border-color: #d1d5db;
}

.input-wrapper input:focus,
textarea:focus {
  outline: none;
  background-color: #ffffff;
  border-color: var(--color-primary, #e65a5a);
  box-shadow: 0 0 0 3px rgba(230, 90, 90, 0.12);
}

/* Chrome/Safari autofill styling */
.input-wrapper input:-webkit-autofill {
  -webkit-box-shadow: 0 0 0px 1000px #fafafa inset;
  transition: background-color 5000s ease-in-out 0s;
}

/* ===== DATE PICKER ICON STYLE ===== */
input[type="datetime-local"]::-webkit-calendar-picker-indicator {
  cursor: pointer;
  filter: opacity(0.5);
  transition: filter 0.2s;
}

input[type="datetime-local"]::-webkit-calendar-picker-indicator:hover {
  filter: opacity(0.8);
}

/* ===== ERROR HANDLING & SUMMARIES ===== */
.error-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #e63946;
  font-size: 0.85rem;
  margin-top: 6px;
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
