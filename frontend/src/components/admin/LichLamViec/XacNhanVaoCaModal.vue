<template>
  <div v-if="visible">
    <div class="modal-backdrop fade show"></div>
    <div 
      class="modal fade show d-block" 
      tabindex="-1"
      style="background: rgba(0,0,0,0.5);"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header bg-danger text-white d-flex justify-content-between align-items-center">
            <h5 class="modal-title fw-bold m-0">
              <i class="bi bi-box-arrow-in-right me-2"></i> Xác Nhận Vào Ca
            </h5>
            <div class="badge bg-light text-danger fs-6 d-flex align-items-center">
              <i class="bi bi-clock me-1"></i> {{ formatCurrentTime(currentTime) }}
            </div>
          </div>
          <div class="modal-body p-4">
            <!-- THÔNG TIN CA LÀM VIỆC -->
            <div v-if="lichLamViecData" class="mb-4 p-3 bg-light rounded-3 border shadow-sm">
              <h6 class="fw-bold border-bottom pb-2 mb-3 text-danger">
                <i class="bi bi-calendar-event me-1"></i> Thông tin ca làm việc
              </h6>
              <div class="row g-3">
                <div class="col-12 col-sm-6">
                  <div class="text-muted small mb-1">Nhân viên:</div>
                  <div class="fw-bold">{{ lichLamViecData.tenNhanVien }}</div>
                </div>
                <div class="col-12 col-sm-6">
                  <div class="text-muted small mb-1">Ca làm:</div>
                  <div class="fw-bold">{{ lichLamViecData.tenCaLam }} ({{ formatOnlyDate(lichLamViecData.ngay) }})</div>
                </div>
                <div class="col-12 col-sm-6">
                  <div class="text-muted small mb-1">Giờ bắt đầu:</div>
                  <div class="fw-bold text-success">{{ formatTime(lichLamViecData.gioBatDau) }}</div>
                </div>
                <div class="col-12 col-sm-6">
                  <div class="text-muted small mb-1">Giờ kết thúc:</div>
                  <div class="fw-bold text-danger">{{ formatTime(lichLamViecData.gioKetThuc) }}</div>
                </div>
              </div>
            </div>

            <template v-if="isThuNgan">
              <div v-if="giaoCa" class="mb-4">
                <div class="alert alert-info d-flex align-items-center mb-3 border-0 shadow-sm">
                  <i class="bi bi-info-circle-fill fs-4 me-3"></i>
                  <div>
                    <strong class="d-block mb-1">Thông tin bàn giao từ ca trước</strong>
                    <span class="text-muted small">Vui lòng kiểm tra lại thông tin bàn giao trước khi nhận ca.</span>
                  </div>
                </div>
                <div class="p-3 bg-light rounded-3 border">
                  <!-- Người giao ca -->
                  <div class="d-flex justify-content-between mb-2 pb-2 border-bottom">
                    <span class="text-muted"><i class="bi bi-person-fill me-1"></i>Người giao ca:</span>
                    <span class="fw-bold text-danger">{{ giaoCa.tenNhanVienGiaoCa || giaoCa.maNhanVienGiaoCa || '— Chưa có —' }}</span>
                  </div>
                  <!-- Thời gian kết thúc ca trước -->
                  <div class="d-flex justify-content-between mb-2 pb-2 border-bottom">
                    <span class="text-muted"><i class="bi bi-clock-history me-1"></i>Ca trước kết thúc lúc:</span>
                    <span class="fw-bold">{{ formatDateTime(giaoCa.thoiGianKetThucCa) || '—' }}</span>
                  </div>
                  <!-- Tiền bàn giao dự kiến (số tiền để lại két) -->
                  <div class="d-flex justify-content-between mb-2 pb-2 border-bottom">
                    <span class="text-muted"><i class="bi bi-cash-coin me-1"></i>Tiền để lại két (dự kiến):</span>
                    <span class="fw-bold text-danger fs-5">{{ formatCurrency(giaoCa.tienBanGiaoDuKien) }}</span>
                  </div>
                  <!-- Tổng tiền mặt thu được ca trước -->
                  <div v-if="giaoCa.tongTienMatCaTruoc != null" class="d-flex justify-content-between mb-2 pb-2 border-bottom">
                    <span class="text-muted"><i class="bi bi-wallet2 me-1"></i>Tổng tiền mặt ca trước:</span>
                    <span class="fw-bold text-success">{{ formatCurrency(giaoCa.tongTienMatCaTruoc) }}</span>
                  </div>
                  <!-- Tổng chuyển khoản ca trước -->
                  <div v-if="giaoCa.tongChuyenKhoanCaTruoc != null" class="d-flex justify-content-between">
                    <span class="text-muted"><i class="bi bi-bank me-1"></i>Tổng chuyển khoản / Online ca trước:</span>
                    <span class="fw-bold text-info">{{ formatCurrency(giaoCa.tongChuyenKhoanCaTruoc) }}</span>
                  </div>
                </div>
                <!-- Ghi chú -->
                <div v-if="giaoCa.ghiChu" class="mt-3 p-3 bg-warning bg-opacity-10 rounded-3 border border-warning">
                  <strong class="text-warning-emphasis"><i class="bi bi-journal-text me-1"></i>Ghi chú bàn giao:</strong>
                  <span class="text-dark ms-1">{{ giaoCa.ghiChu }}</span>
                </div>
              </div>
              
              <div v-else class="mb-4">
                <div class="alert alert-success d-flex align-items-center mb-0 border-0 shadow-sm">
                  <i class="bi bi-check-circle-fill fs-4 me-3"></i>
                  <div>
                    <strong class="d-block mb-1">Vào ca làm việc mới</strong>
                    <span class="text-muted small">Chào mừng bạn đến với ca làm việc. Vui lòng nhập số tiền mặt ban đầu tại quầy.</span>
                  </div>
                </div>
              </div>

              <div class="form-group mb-0">
                <label class="form-label fw-bold text-dark mb-2">Nhập số tiền mặt thực tế tại quầy (Tiền đầu ca) <span class="text-danger">*</span></label>
                <div class="input-group input-group-lg shadow-sm">
                  <span class="input-group-text bg-white text-secondary"><i class="bi bi-cash-stack"></i></span>
                  <input 
                    type="text" 
                    class="form-control border-start-0 ps-0" 
                    :value="formatMoneyInput(tongTienMat)" 
                    inputmode="numeric"
                    @input="handleTongTienMatInput"
                    placeholder="Nhập số tiền hiện có trong két..."
                  >
                  <span class="input-group-text bg-white fw-bold">đ</span>
                </div>
              </div>
            </template>
            <template v-else>
              <div class="alert alert-success d-flex align-items-center mb-0 border-0 shadow-sm">
                <i class="bi bi-check-circle-fill fs-4 me-3"></i>
                <div>
                  <strong class="d-block mb-1">Xác nhận vào ca làm việc</strong>
                  <span class="text-muted small">Chúc bạn một ca làm việc vui vẻ và hiệu quả!</span>
                </div>
              </div>
            </template>
          </div>
          <div class="modal-footer bg-light">
            <button type="button" class="btn btn-danger px-4 py-2 fw-bold" :disabled="loading" @click="handleConfirm">
              <span v-if="loading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
              <i v-else class="bi bi-check2-circle me-1"></i>
              Xác nhận
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue';
import { giaoCaApi } from '@/api/admin/lichlamviec/giaoCaApi';
import { useUtils } from '@/utils/helper';

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  giaoCaData: {
    type: Object,
    default: null
  },
  lichLamViecData: {
    type: Object,
    default: null
  },
  isThuNgan: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:modelValue', 'confirmed']);
const { notify } = useUtils();

const visible = ref(props.modelValue);
const giaoCa = ref(null);
const tongTienMat = ref(0);
const loading = ref(false);
const currentTime = ref(new Date());
let timer = null;

onMounted(() => {
  timer = setInterval(() => {
    currentTime.value = new Date();
  }, 1000);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});

watch(() => props.modelValue, (val) => {
  visible.value = val;
});

watch(() => props.giaoCaData, (val) => {
  giaoCa.value = val;
  if (val && val.tienBanGiaoDuKien) {
    tongTienMat.value = val.tienBanGiaoDuKien;
  } else {
    tongTienMat.value = 0;
  }
}, { immediate: true });

const formatCurrency = (value) => {
  return `${formatMoneyInput(value)} đ`;
};

const parseMoney = (value) => {
  if (value === null || value === undefined || value === '') return 0;
  return Number(value.toString().replace(/\D/g, '')) || 0;
};

const formatMoneyInput = (value) => {
  return parseMoney(value).toLocaleString('vi-VN');
};

const handleTongTienMatInput = (event) => {
  tongTienMat.value = parseMoney(event.target.value);
};

const formatDateTime = (value) => {
  if (!value) return '';
  const date = new Date(value);
  return date.toLocaleString('vi-VN', {
    hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric'
  });
};

const formatTime = (time) => {
  if (!time) return '';
  if (Array.isArray(time)) {
    const hour = time[0].toString().padStart(2, '0');
    const min = time[1].toString().padStart(2, '0');
    return `${hour}:${min}`;
  }
  if (typeof time === 'string') {
    return time.substring(0, 5); // "08:00:00" -> "08:00"
  }
  return time;
};

const formatOnlyDate = (date) => {
  if (!date) return '';
  if (Array.isArray(date)) {
    const year = date[0];
    const month = date[1].toString().padStart(2, '0');
    const day = date[2].toString().padStart(2, '0');
    return `${day}/${month}/${year}`;
  }
  if (typeof date === 'string' && date.includes('-')) {
    const parts = date.split('-');
    return `${parts[2]}/${parts[1]}/${parts[0]}`; // YYYY-MM-DD -> DD/MM/YYYY
  }
  return date;
};

const formatCurrentTime = (date) => {
  if (!date) return '';
  return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
};

const handleConfirm = async () => {
  if (tongTienMat.value === null || tongTienMat.value === undefined || tongTienMat.value < 0) {
    notify.warning('Vui lòng nhập số tiền đầu ca hợp lệ!');
    return;
  }

  try {
    loading.value = true;
    await giaoCaApi.xacNhanVaoCa(tongTienMat.value);
    notify.success('Xác nhận vào ca thành công!');
    visible.value = false;
    emit('update:modelValue', false);
    emit('confirmed');
  } catch (error) {
    console.error('Lỗi xác nhận vào ca:', error);
    notify.error(error.response?.data?.message || 'Có lỗi xảy ra khi xác nhận vào ca');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.modal-backdrop {
  opacity: 0.5;
}
.modal-content {
  border-radius: 12px;
  overflow: hidden;
}
.form-control:focus {
  box-shadow: none;
  border-color: #dee2e6;
}
</style>
