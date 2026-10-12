<script setup>
import { ref, watch } from "vue";
import { getLichSuHoaDon } from "@/api/admin/checkin/checkinapi";

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  ban: {
    type: Object,
    default: () => ({}),
  },
});

const emit = defineEmits(["close", "back"]);

const loading = ref(false);
const dsLichSu = ref([]);

const formatDateTime = (value) => {
  if (!value) return "--";
  return new Date(value).toLocaleString("vi-VN");
};

const getThoiGianText = (item) => {
  return formatDateTime(item?.thoiGian || item?.ngayTao || item?.ngayCapNhat);
};

const getTrangThaiInfo = (item) => {
  const trangThai = Number(item?.trangThai);

  if (trangThai === 0) {
    return {
      text: "Chưa xác nhận vào ca",
      type: "pending",
    };
  }

  if (trangThai === 1) {
    return {
      text: "Đã vào ca",
      type: "active",
    };
  }

  if (trangThai === 2) {
    return {
      text: "Đã kết thúc ca",
      type: "done",
    };
  }

  return null;
};

const getHanhDongInfo = (item) => {
  const hanhDong = item?.hanhDong || "";

  const map = {
    CHECK_IN: {
      title: "Khách nhận bàn",
      tag: "Khách nhận bàn",
      icon: "ri-user-check-line",
      type: "success",
    },
    GOI_MON: {
      title: "Gọi món",
      tag: "Gọi món",
      icon: "ri-restaurant-2-line",
      type: "food",
    },
    LEN_MON: {
      title: "Lên món",
      tag: "Lên món",
      icon: "ri-restaurant-line",
      type: "food",
    },
    THANH_TOAN: {
      title: "Thanh toán",
      tag: "Thanh toán",
      icon: "ri-bank-card-line",
      type: "success",
    },
    HUY_HOA_DON: {
      title: "Hủy hóa đơn",
      tag: "Hủy hóa đơn",
      icon: "ri-close-circle-line",
      type: "danger",
    },
    LEN_TAT_CA: {
      title: "Lên tất cả món",
      tag: "Lên tất cả",
      icon: "ri-check-double-line",
      type: "success",
    },
    GUI_MAIL: {
      title: "Gửi mail",
      tag: "Gửi mail",
      icon: "ri-mail-send-line",
      type: "default",
    },
    XAC_NHAN_DAT_BAN: {
      title: "Xác nhận đặt bàn",
      tag: "Xác nhận",
      icon: "ri-checkbox-circle-line",
      type: "success",
    },
    NHAN_TIEN_COC: {
      title: "Nhận tiền cọc",
      tag: "Nhận tiền cọc",
      icon: "ri-money-dollar-circle-line",
      type: "success",
    },
    TAO_HOA_DON: {
      title: "Tạo hóa đơn",
      tag: "Tạo hóa đơn",
      icon: "ri-receipt-line",
      type: "default",
    },
    AP_DUNG_MA_GIAM_GIA: {
      title: "Áp dụng mã giảm giá",
      tag: "Áp dụng mã giảm giá",
      icon: "ri-coupon-3-line",
      type: "default",
    },
    GIAM_GIA_TRUC_TIEP: {
      title: "Giảm giá trực tiếp",
      tag: "Giảm giá trực tiếp",
      icon: "ri-discount-percent-line",
      type: "default",
    },
    HOAN_TAT_THANH_TOAN: {
      title: "Hoàn tất thanh toán",
      tag: "Hoàn tất",
      icon: "ri-checkbox-circle-line",
      type: "success",
    },
    GHEP_CHO_NGOI: {
      title: "Ghép chỗ ngồi",
      tag: "Ghép chỗ",
      icon: "ri-group-line",
      type: "default",
    },
    CHUYEN_CHO_NGOI: {
      title: "Chuyển chỗ ngồi",
      tag: "Chuyển chỗ",
      icon: "ri-arrow-left-right-line",
      type: "default",
    },
    TACH_HOA_DON_KHOI_BAN_DUNG_CHUNG: {
      title: "Tách hóa đơn khỏi bàn dùng chung",
      tag: "Tách hóa đơn",
      icon: "ri-split-cells-horizontal",
      type: "default",
    },
    TACH_BAN: {
      title: "Tách bàn",
      tag: "Tách bàn",
      icon: "ri-split-cells-horizontal",
      type: "default",
    },
    DOI_BAN: {
      title: "Đổi bàn",
      tag: "Đổi bàn",
      icon: "ri-arrow-left-right-line",
      type: "default",
    },
    XEP_BAN: {
      title: "Xếp bàn",
      tag: "Xếp bàn",
      icon: "ri-layout-masonry-line",
      type: "default",
    },
    THEM_KHACH: {
      title: "Thêm khách",
      tag: "Thêm khách",
      icon: "ri-user-add-line",
      type: "default",
    },
    XOA_MON: {
      title: "Xóa món",
      tag: "Xóa món",
      icon: "ri-delete-bin-line",
      type: "default",
    },
  };

  const info = map[hanhDong];
  if (info) {
    return {
      title: info.title,
      tag: info.tag,
      note: item?.ghiChu || info.title,
      icon: info.icon,
      type: info.type,
    };
  }

  return {
    title: hanhDong || "Cập nhật hóa đơn",
    tag: hanhDong || "Khác",
    note: item?.ghiChu || "Không có ghi chú",
    icon: "ri-information-line",
    type: "default",
  };
};

async function loadLichSu() {
  try {
    if (props.ban?.idHoaDon == null) {
      dsLichSu.value = [];
      return;
    }

    loading.value = true;
    const res = await getLichSuHoaDon(props.ban.idHoaDon);
    dsLichSu.value = Array.isArray(res.data) ? res.data : [];
  } catch (error) {
    console.error("Lỗi load lịch sử hóa đơn:", error);
    dsLichSu.value = [];
  } finally {
    loading.value = false;
  }
}

watch(
  () => props.show,
  (val) => {
    if (val) loadLichSu();
  },
  { immediate: true },
);

watch(
  () => props.ban?.idHoaDon,
  (val) => {
    if (props.show && val) loadLichSu();
  },
  { immediate: true },
);
</script>

<template>
  <Teleport to="body">
    <div v-if="show" class="modal-overlay" @click.self="emit('close')">
      <div class="modal-box premium-modal-content" style="width: 920px; max-width: 96vw; max-height: 92vh; overflow-y: auto;">
        <div class="modal-header premium-modal-header">
          <h5 class="title-main fw-bold mb-0" style="font-size: 20px;">
            Lịch sử đơn - {{ ban?.tenBanAn || ban?.maBanAn || "..." }}
          </h5>
          <button class="btn-close" @click="emit('close')"></button>
        </div>

        <div class="modal-body premium-modal-body content" style="padding-top: 20px;">
          <div class="header-box">
            <h3 class="title-sub">
              ↩ Lịch sử đơn hàng - {{ ban?.tenBanAn || ban?.maBanAn || "..." }}
            </h3>
            <div class="order-code">
              Mã hóa đơn: #{{ ban?.idHoaDon || "--" }}
            </div>
          </div>

          <div class="top-action">
            <button class="btn btn-back" @click="emit('back')">
              <i class="ri-arrow-left-line me-2"></i>
              Quay lại thông tin bàn
            </button>
          </div>

          <div v-if="loading" class="empty-state">
            Đang tải lịch sử hóa đơn...
          </div>

          <div v-else-if="!dsLichSu.length" class="empty-state">
            Chưa có lịch sử hóa đơn
          </div>

          <div v-else class="timeline">
            <div
              v-for="(item, index) in dsLichSu"
              :key="item.id || index"
              class="timeline-item"
            >
              <div class="timeline-left">
                <div class="timeline-icon" :class="getHanhDongInfo(item).type">
                  <i :class="getHanhDongInfo(item).icon"></i>
                </div>
                <div
                  v-if="index !== dsLichSu.length - 1"
                  class="timeline-line"
                ></div>
              </div>

              <div class="timeline-card">
                <div class="timeline-card-top">
                  <div class="timeline-title">
                    {{ getHanhDongInfo(item).title }}
                  </div>

                  <div class="timeline-badges">
                    <div
                      v-if="getTrangThaiInfo(item)"
                      class="timeline-status"
                      :class="getTrangThaiInfo(item).type"
                    >
                      {{ getTrangThaiInfo(item).text }}
                    </div>
                  </div>
                </div>

                <div class="timeline-meta">
                  <span
                    ><i class="ri-time-line"></i>
                    {{ getThoiGianText(item) }}</span
                  >
                  <span>
                    <i class="ri-user-line"></i>
                    {{
                      item?.tenNhanVien ||
                      item?.nguoiTao ||
                      ban?.tenNhanVien ||
                      "Nhân viên"
                    }}
                  </span>
                </div>

                <div class="timeline-note">
                  {{ getHanhDongInfo(item).note }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
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

.premium-modal-content {
  background: #ffffff;
  border-radius: 16px;
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
  font-size: 20px;
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

.title-main {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
  text-transform: none;
}

.divider {
  height: 1px;
  background: #eef2f7;
}

.content {
  padding: 22px 24px 28px;
}

.header-box {
  margin-bottom: 18px;
}

.title-sub {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.order-code {
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
}

.top-action {
  margin: 18px 0 24px;
}

.empty-state {
  padding: 30px;
  text-align: center;
  color: #94a3b8;
  background: #f8fafc;
  border: 1px dashed #e2e8f0;
  border-radius: 7px;
  font-size: 14px;
  font-weight: 500;
}

.timeline {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.timeline-item {
  display: flex;
  gap: 16px;
  padding: 6px;
  border-radius: 7px;
  transition: all 0.25s ease;
}

.timeline-item:hover {
  background: #f8fafc;
  transform: translateY(-2px);
}

.timeline-item:hover .timeline-card {
  border-color: #fecaca;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.08);
  background: #fffdfd;
}

.timeline-item:hover .timeline-icon {
  transform: scale(1.08);
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.12);
}

.timeline-item:hover .timeline-title {
  color: #b91c1c;
}

.timeline-left {
  width: 34px;
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.timeline-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  color: white;
  font-size: 12px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.25s ease;
}

.timeline-icon.success,
.timeline-badge.success {
  background: #22c55e;
}

.timeline-icon.food,
.timeline-badge.food {
  background: #3b82f6;
}

.timeline-icon.danger,
.timeline-badge.danger {
  background: #ef4444;
}

.timeline-icon.default,
.timeline-badge.default {
  background: #94a3b8;
}

.timeline-line {
  width: 2px;
  flex: 1;
  background: #e5e7eb;
  margin-top: 4px;
}

.timeline-card {
  flex: 1;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 7px;
  padding: 14px;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.05);
  transition: all 0.25s ease;
}

.timeline-card-top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.timeline-title {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  transition: color 0.25s ease;
}

.timeline-badge {
  border-radius: 8px;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
  color: #fff;
  transition: all 0.25s ease;
}

.timeline-badges {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}

.timeline-status {
  border-radius: 8px;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
  border: 1px solid transparent;
}

.timeline-status.pending {
  background: rgba(251, 146, 60, 0.12);
  color: #fb923c;
}

.timeline-status.active {
  background: rgba(59, 130, 246, 0.12);
  color: #3b82f6;
}

.timeline-status.done {
  background: rgba(5, 150, 105, 0.12);
  color: #059669;
}

.timeline-meta {
  display: flex;
  gap: 18px;
  flex-wrap: wrap;
  margin-top: 6px;
  color: #64748b;
  font-size: 13px;
  font-weight: 500;
}

.timeline-note {
  margin-top: 12px;
  background: #f8fafc;
  border-radius: 7px;
  padding: 10px 12px;
  color: #475569;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.25s ease;
}

.timeline-item:hover .timeline-note {
  background: #fff7f7;
  color: #334155;
}

/* BUTTON SYSTEM */
.btn {
  border: none;
  outline: none;
  cursor: pointer;
  transition: transform 0.22s ease, box-shadow 0.22s ease,
    background-color 0.22s ease, color 0.22s ease, border-color 0.22s ease;
  font-family: var(--font-family, "Inter", sans-serif);
}

.btn:hover {
  transform: translateY(-2px);
}

.btn:active {
  transform: scale(0.97);
}

.btn-close {
  background: #f3f4f6;
  font-size: 18px;
  color: #475569;
  width: 40px;
  height: 40px;
  border-radius: 7px;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.06);
}

.btn-close:hover {
  background: #e2e8f0;
  color: #0f172a;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.12);
}

.btn-back {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e2e8f0;
  background: #fff;
  border-radius: 7px;
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.05);
}

.btn-back:hover {
  background: linear-gradient(135deg, #f8fafc, #eef2f7);
  border-color: #cbd5e1;
  color: #0f172a;
  box-shadow: 0 10px 20px rgba(15, 23, 42, 0.08);
}

@media (max-width: 768px) {
  .timeline-card-top {
    flex-direction: column;
    align-items: flex-start;
  }

  .timeline-badge {
    align-self: flex-start;
  }

  .timeline-badges {
    justify-content: flex-start;
  }
}
</style>
