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
          <div class="modal-header bg-danger text-white">
            <h5 class="modal-title fw-bold">
              <i class="bi bi-box-arrow-right me-2"></i> Kết Thúc Ca Làm Việc
            </h5>
            <button type="button" class="btn-close btn-close-white" @click="closeModal"></button>
          </div>
          <div class="modal-body p-4">
            <div class="alert alert-warning d-flex align-items-center mb-4 border-0 shadow-sm">
              <i class="bi bi-exclamation-triangle-fill fs-4 me-3"></i>
              <div>
                <strong class="d-block mb-1">Xác nhận kết thúc ca</strong>
                <span class="text-muted small">Bạn đang yêu cầu kết thúc ca làm việc hiện tại. Vui lòng đếm lại số tiền mặt trong két để bàn giao cho ca sau.</span>
              </div>
            </div>

              <!-- THÔNG KÊ CA LÀM VIỆC HIỆN TẠI -->
              <div class="mb-4 p-3 bg-light rounded-3 border shadow-sm" v-if="thongKe">
                <h6 class="fw-bold border-bottom pb-2 mb-3 text-primary">
                  <i class="bi bi-calculator me-1"></i> Thống kê trong ca
                </h6>
                <div class="d-flex justify-content-between mb-2 pb-2 border-bottom">
                  <span class="text-muted"><i class="bi bi-cash me-1"></i>Tiền mặt đã thu:</span>
                  <span class="fw-bold text-success">{{ formatCurrency(thongKe.tongTienMat) }}</span>
                </div>
                <div class="d-flex justify-content-between mb-2 pb-2 border-bottom">
                  <span class="text-muted"><i class="bi bi-bank me-1"></i>Chuyển khoản / Online đã thu:</span>
                  <span class="fw-bold text-info">{{ formatCurrency(thongKe.tongChuyenKhoan) }}</span>
                </div>
                <div class="d-flex justify-content-between">
                  <span class="text-muted"><i class="bi bi-wallet-fill me-1"></i>Két dự kiến (Tiền đầu ca + Tiền mặt):</span>
                  <span class="fw-bold text-danger fs-5">{{ formatCurrency(thongKe.tienDuKienBanGiao) }}</span>
                </div>
              </div>

              <div class="form-group mb-4">
                <label class="form-label fw-bold text-dark mb-2">Số tiền bàn giao (để lại két) <span class="text-danger">*</span></label>
                <div class="input-group input-group-lg shadow-sm">
                  <span class="input-group-text bg-white text-secondary"><i class="bi bi-cash-stack"></i></span>
                  <input 
                    type="text" 
                    class="form-control border-start-0 ps-0" 
                    :value="formatMoneyInput(tienBanGiao)" 
                    inputmode="numeric"
                    @input="handleTienBanGiaoInput"
                    placeholder="Nhập số tiền bàn giao..."
                  >
                  <span class="input-group-text bg-white fw-bold">đ</span>
                </div>
                <div class="form-text mt-2 text-danger" v-if="tienBanGiao < 0">Số tiền không được âm.</div>
              </div>

              <div class="form-group mb-4">
                <label class="form-label fw-bold text-dark mb-2">Người nhận ca tiếp theo (Tùy chọn)</label>
                <select class="form-select shadow-sm" v-model="nhanVienNhanCaTiepTheoId" style="height: 48px;">
                  <option :value="null">-- Không chọn --</option>
                  <option v-for="nv in danhSachNhanCaTiepTheo" :key="nv.idNhanVien" :value="nv.idNhanVien">
                    {{ nv.tenNhanVien }} - {{ nv.tenCaLam }} ({{ formatDate(nv.ngay) }} | {{ formatTime(nv.gioBatDau) }} - {{ formatTime(nv.gioKetThuc) }})
                  </option>
                </select>
                <div class="form-text mt-1 text-muted">Chọn nhân viên sẽ nhận bàn giao ca này từ bạn.</div>
              </div>

            <div class="form-group mb-0">
              <label class="form-label fw-bold text-dark mb-2">Ghi chú bàn giao (Tùy chọn)</label>
              <textarea 
                class="form-control shadow-sm" 
                v-model="ghiChu" 
                rows="3" 
                placeholder="Ví dụ: Đã kiểm kho, thiếu 2 chai nước suối..."
              ></textarea>
            </div>
          </div>
          <div class="modal-footer bg-light">
            <button type="button" class="btn btn-secondary px-4 py-2" @click="closeModal" :disabled="loading">
              Hủy bỏ
            </button>
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
import { ref, watch } from 'vue';
import { giaoCaApi } from '@/api/admin/lichlamviec/giaoCaApi';
import { useUtils } from '@/utils/helper';

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  isThuNgan: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:modelValue', 'confirmed']);
const { notify } = useUtils();

const visible = ref(props.modelValue);
const tienBanGiao = ref(0);
const ghiChu = ref('');
const loading = ref(false);
const nhanVienNhanCaTiepTheoId = ref(null);
const danhSachNhanCaTiepTheo = ref([]);
const thongKe = ref(null);

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

const handleTienBanGiaoInput = (event) => {
  tienBanGiao.value = parseMoney(event.target.value);
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

const formatDate = (date) => {
  if (!date) return '';
  if (Array.isArray(date)) {
    const year = date[0];
    const month = date[1].toString().padStart(2, '0');
    const day = date[2].toString().padStart(2, '0');
    return `${day}/${month}/${year}`;
  }
  if (typeof date === 'string') {
    const parts = date.split('-');
    if (parts.length === 3) {
      return `${parts[2]}/${parts[1]}/${parts[0]}`;
    }
  }
  return date;
};

const laNhanVienChucVu2 = (nhanVien) => {
  const chucVu = nhanVien?.chucVu ?? nhanVien?.viTri ?? nhanVien?.nhanVien?.chucVu;
  return Number(chucVu) === 0;
};

const loadDanhSachNhanCaTiepTheo = async () => {
  try {
    const response = await giaoCaApi.danhSachNhanCaTiepTheo();
    const danhSach = Array.isArray(response.data) ? response.data : [];
    danhSachNhanCaTiepTheo.value = danhSach.filter(laNhanVienChucVu2);
    if (!danhSachNhanCaTiepTheo.value.some((nv) => nv.idNhanVien === nhanVienNhanCaTiepTheoId.value)) {
      nhanVienNhanCaTiepTheoId.value = null;
    }
  } catch (error) {
    console.error('Lỗi load danh sách nhận ca tiếp theo:', error);
  }
};

const loadThongKeHienTai = async () => {
  try {
    const response = await giaoCaApi.thongKeHienTai();
    thongKe.value = response.data;
    if (response.data && response.data.tienDuKienBanGiao != null) {
      tienBanGiao.value = response.data.tienDuKienBanGiao;
    }
  } catch (error) {
    console.error('Lỗi load thống kê hiện tại:', error);
  }
};

watch(() => props.modelValue, (val) => {
  visible.value = val;
  if (val) {
    // Reset form when modal opens
    tienBanGiao.value = 0;
    ghiChu.value = '';
    nhanVienNhanCaTiepTheoId.value = null;
    loadDanhSachNhanCaTiepTheo();
    loadThongKeHienTai();
  }
});

const closeModal = () => {
  if (loading.value) return;
  visible.value = false;
  emit('update:modelValue', false);
};

const handleConfirm = async () => {
  if (tienBanGiao.value === null || tienBanGiao.value === undefined || tienBanGiao.value < 0) {
    notify.warning('Vui lòng nhập số tiền bàn giao hợp lệ!');
    return;
  }

  try {
    loading.value = true;
    await giaoCaApi.ketThucCa(tienBanGiao.value, ghiChu.value, nhanVienNhanCaTiepTheoId.value);
    notify.success('Kết thúc ca thành công!');
    closeModal();
    emit('confirmed');
  } catch (error) {
    console.error('Lỗi kết thúc ca:', error);
    notify.error(error.response?.data?.message || 'Có lỗi xảy ra khi kết thúc ca');
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
