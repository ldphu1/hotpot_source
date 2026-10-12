<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  huyDatBanCustomer,
  layLinkVnpayCoc,
} from "@/api/customer/info/thongTinCaNhanApi";
import { computed } from "vue";
import { lichSuDetail } from "@/api/customer/info/thongTinCaNhanApi";
import { previewHuyDatBan } from "@/api/customer/info/thongTinCaNhanApi";
import iconBack from "@/assets/icons/prev.png";
const route = useRoute();
const router = useRouter();
const detail = ref(null);
///////////////////// NÚT THANH TOÁN CỌC ////////////////
const isPaying = ref(false);

const handleThanhToanCoc = async () => {
  isPaying.value = true;
  try {
    const res = await layLinkVnpayCoc(detail.value.hoaDon.id);
    window.location.href = res.data;
  } catch (e) {
    notify.error(
      e.response?.data?.message || "Không thể tạo link thanh toán cọc",
    );
  } finally {
    isPaying.value = false;
  }
};
///////////////////// TRẠNG THÁI HÓA ĐƠN ////////////////
const mapStatusToStep = (status) => {
  switch (status) {
    case 0:
    case 1:
    case 2:
    case 4:
      return 0; // Vừa Tạo

    case 3:
      return 1; // Xác Nhận

    case 6:
      return 2; // Phục Vụ ( đang phục vụ)

    case 8:
      return 3; // Thanh Toán
    case 9:
      return 4; // Hoàn thành

    default:
      return -1; // Đã hủy
  }
};

const currentStep = computed(() => {
  if (!detail.value) return 0;
  return mapStatusToStep(detail.value.hoaDon.trangThai);
});

const isDebtPending = computed(() => {
  if (!detail.value?.hoaDon) return false;
  return detail.value.hoaDon.trangThai === 10;
});

const isBadDebt = computed(() => {
  if (!detail.value?.hoaDon) return false;
  return detail.value.hoaDon.trangThai === 11;
});

const remainingPayment = computed(() => {
  if (!detail.value?.hoaDon) return 0;
  return detail.value.hoaDon.tienConLai || 0;
});

///////////////////// HỦY ĐƠN HÀNG //////////////////
const modalHuy = ref(false);
const lyDoHuy = ref("");
const preview = ref(null);
const isLoading = ref(false);
import { useUtils } from "@/utils/helper";
import {
  connectWebSocketHoaDonDetail,
  disconnectWebSocket,
} from "@/utils/websocket";
const { notify, nprogress } = useUtils();

const openModalHuy = async () => {
  modalHuy.value = true;
  try {
    const res = await previewHuyDatBan(detail.value.hoaDon.idDatBan);
    preview.value = res.data;
  } catch (e) {
    console.error(e);
  }
};

const handleHuyDatBan = async () => {
  if (!lyDoHuy.value.trim()) {
    notify.error("Vui lòng nhập lý do hủy");
    return;
  }
  if (!(await notify.confirm("Bạn chắc chắn muốn hủy"))) return;
  isLoading.value = true;
  try {
    await huyDatBanCustomer(detail.value.hoaDon.idDatBan, {
      ghiChu: lyDoHuy.value,
    });
    modalHuy.value = false;
    lyDoHuy.value = "";
    await loadDetail();
  } catch (e) {
    notify.error(e.response?.data?.message || "Có lỗi xảy ra");
  } finally {
    isLoading.value = false;
  }
};

///////////////// GOM MÓN ĂN //////////////

// format
const formatCurrency = (money) => {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(money || 0);
};

const formatDateTime = (dateStr) => {
  if (!dateStr) return "";
  const d = new Date(dateStr);
  return d.toLocaleString("vi-VN");
};

const convertHanhDong = (action) => {
  switch (action) {
    case "TAO_HOA_DON":
      return "Tạo hóa đơn";
    case "NHAN_TIEN_COC":
      return "Nhận tiền cọc";
    case "XAC_NHAN_DAT_BAN":
      return "Xác nhận đặt bàn";
    case "DAT_BAN":
      return "Đặt bàn";
    case "XEP_BAN":
      return "Xếp bàn";
    case "GUI_MAIL":
      return "Gửi email";
    case "AUTO_HUY":
      return "Hủy phiếu đặt bàn";
    case "THANH_TOAN_COC":
      return "Thanh toán cọc";
    case "HOAN_COC":
      return "Hoàn cọc";
    case "HOAN_COC_THAT_BAI":
      return "Hoàn cọc thất bại";
    case "CHECK_IN":
      return "Check-in";
    case "GOI_MON":
      return "Gọi món";
    case "LEN_TAT_CA":
      return "Lên hết món";
    case "THANH_TOAN":
      return "Thanh toán";
    case "HOAN_TAT_THANH_TOAN":
      return "Hoàn tất thanh toán";
    case "HOAN_THANH":
      return "Hoàn thành";
    case "AP_DUNG_MA_GIAM_GIA":
      return "Giảm giá";
    case "THANH_TOAN_COC_THAT_BAI":
      return "Thanh toán cọc thất bại";
    case "TREO_BILL":
      return "Ghi nợ";
    case "NO_XAU":
      return "Nợ xấu";
    default:
      return action;
  }
};
const convertTrangThai = (stt) => {
  switch (stt) {
    case 0:
      return "Vừa tạo";
    case 1:
      return "Chờ thanh toán cọc";
    case 2:
      return "Đã cọc";
    case 3:
      return "Đã xác nhận";
    case 4:
      return "Chờ xác nhận";
    case 5:
      return "Chờ hoàn cọc";
    case 6:
      return "Đang phục vụ";
    case 7:
      return "Chờ thanh toán";
    case 8:
      return "Đã thanh toán";
    case 9:
      return "Hoàn thành";
    case 10:
      return "Treo nợ";
    case 11:
      return "Nợ xấu";
    default:
      return "Đã hủy";
  }
};

const getStatusIcon = (stt) => {
  switch (stt) {
    case 0:
      return "bi bi-journal-plus";
    case 1:
      return "bi bi-hourglass-split";
    case 2:
      return "bi bi-cash-coin";
    case 3:
      return "bi bi-check-circle-fill";
    case 4:
      return "bi bi-hourglass-top";
    case 5:
      return "bi bi-arrow-counterclockwise";
    case 6:
      return "bi bi-cup-hot-fill";
    case 7:
      return "bi bi-receipt";
    case 8:
      return "bi bi-credit-card-fill";
    case 9:
      return "bi bi-trophy-fill";
    case 10:
      return "bi bi-exclamation-octagon-fill";
    case 11:
      return "bi bi-exclamation-triangle-fill";
    default:
      return "bi bi-x-circle-fill";
  }
};

const getStatusClass = (stt) => {
  switch (stt) {
    case 0:
      return "status-new";
    case 1:
    case 4:
    case 5:
    case 7:
    case 10:
      return "status-pending";
    case 11:
      return "status-bad-debt";
    case 2:
    case 3:
    case 8:
      return "status-success";
    case 6:
      return "status-active";
    case 9:
      return "status-done";
    default:
      return "status-cancel";
  }
};

// Phân trang món ăn (gộp tất cả nhóm, phân trang theo flat list)
const monAnPage = ref(1);
const monAnPageSize = 5;

const flatItems = computed(() => {
  if (!detail.value) return [];
  return detail.value.items.map((item) => ({
    ...item,
    groupLabel: item.tenDanhMuc === "Lẩu" ? "Món chính" : "Món phụ",
  }));
});

const pagedItems = computed(() => {
  const start = (monAnPage.value - 1) * monAnPageSize;
  return flatItems.value.slice(start, start + monAnPageSize);
});

const monAnTotalPages = computed(() =>
  Math.ceil(flatItems.value.length / monAnPageSize),
);

const monAnPageList = computed(() => {
  const total = monAnTotalPages.value;
  if (total <= 5) return Array.from({ length: total }, (_, i) => i + 1);
  const pages = [1, 2, 3];
  if (total > 4) pages.push("…");
  pages.push(total);
  return pages;
});

const loadDetail = async () => {
  const res = await lichSuDetail(route.params.id);
  detail.value = res.data;
};
const getImageUrl = (img) => {
  if (!img) return "https://via.placeholder.com/50";

  return `http://localhost:8080/uploads/${img}`;
};

////////////////////////////// REALTIME /////////////////////////
const handleSocketMessage = (data) => {
  if (data.type !== "HOA_DON_DETAIL") return;
  if (!detail.value || String(data.hoaDon?.id) !== String(route.params.id))
    return;
  detail.value = data;
};

onMounted(() => {
  loadDetail().then(() => {
    connectWebSocketHoaDonDetail(route.params.id, handleSocketMessage);
  });
});

onUnmounted(() => {
  disconnectWebSocket(handleSocketMessage);
});

const goBack = () => router.back();
</script>

<template>
  <div v-if="detail" class="container py-4">
    <!-- BOX 1: STATUS -->
    <div class="card order-detail-card p-4 mb-4 position-relative">
      <!-- HEADER -->
      <div class="order-header-top">
        <div class="status-text">
          <h4>
            Mã đơn hàng : {{ detail.hoaDon.maHoaDon }} -
            <span
              class="status-badge"
              :class="getStatusClass(detail.hoaDon.trangThai)"
            >
              <i :class="getStatusIcon(detail.hoaDon.trangThai)"></i>
              {{ convertTrangThai(detail.hoaDon.trangThai) }}
            </span>
          </h4>
        </div>
        <button class="icon-btn-back" @click="goBack">
          <img :src="iconBack" />
        </button>
      </div>
      <div v-if="detail.hoaDon.trangThai === -1" class="cancel-simple">
        <i class="bi bi-x-circle"></i>
        Đơn hàng đã bị hủy - không thể thao tác
      </div>

      <!-- Debt Notice -->
      <div
        v-else-if="isDebtPending"
        class="debt-notice p-3 rounded-3 text-center border-danger-subtle bg-danger-subtle text-danger mb-3"
        style="margin-top: 15px;"
      >
        <i class="bi bi-exclamation-octagon-fill me-2"></i>
        Đơn đặt bàn đã hoàn thành nhưng hóa đơn vẫn đang
        <strong>treo nợ</strong>. Số tiền còn lại cần thanh toán:
        <strong>{{ remainingPayment.toLocaleString() }}đ</strong>.
      </div>

      <!-- Bad Debt Notice -->
      <div
        v-else-if="isBadDebt"
        class="bad-debt-notice p-3 rounded-3 text-center mb-3"
        style="margin-top: 15px;"
      >
        <i class="bi bi-exclamation-triangle-fill me-2"></i>
        Hóa đơn đã bị chuyển sang trạng thái <strong>nợ xấu</strong> do chưa thanh toán khoản nợ quá hạn. Số tiền chưa thanh toán:
        <strong>{{ remainingPayment.toLocaleString() }}đ</strong>. Quý khách vui lòng liên hệ nhà hàng để được hỗ trợ và tất toán khoản nợ.
      </div>

      <!-- TIMELINE -->
      <div v-else-if="currentStep !== -1" class="stepper-wrapper py-3">
        <div class="step-bar">
          <div
            class="step-progress"
            :style="{
              width: currentStep === 0 ? '0%' : (currentStep / 4) * 100 + '%',
            }"
          ></div>
        </div>
        <div class="steps-container d-flex justify-content-between">
          <div class="step-item" :class="{ active: currentStep >= 0 }">
            <div class="step-number"><i class="bi bi-journal-check"></i></div>
            <div class="step-label">Vừa Tạo</div>
          </div>
          <div class="step-item" :class="{ active: currentStep >= 1 }">
            <div class="step-number"><i class="bi bi-wallet2"></i></div>
            <div class="step-label">Xác Nhận</div>
          </div>
          <div class="step-item" :class="{ active: currentStep >= 2 }">
            <div class="step-number"><i class="bi bi-shop-window"></i></div>
            <div class="step-label">Phục Vụ</div>
          </div>
          <div class="step-item" :class="{ active: currentStep >= 3 }">
            <div class="step-number"><i class="bi bi-credit-card"></i></div>
            <div class="step-label">Thanh Toán</div>
          </div>
          <div class="step-item" :class="{ active: currentStep >= 4 }">
            <div class="step-number"><i class="bi bi-award"></i></div>
            <div class="step-label">Hoàn Thành</div>
          </div>
        </div>
      </div>
    </div>

    <!-- THÔNG TIN CHUNG (BOX MỚI THÊM) -->
    <div class="row mb-4">
      <!-- Thông tin hóa đơn + Khách hàng -->
      <div class="col-md-6">
        <div class="card p-3 shadow-sm h-100">
          <div class="box-title">
            <h5>
              <i
                class="bi bi-person-lines-fill me-2"
                style="font-size: 20px; color: #000"
              ></i>
              Thông tin khách hàng & Hóa đơn
            </h5>
          </div>
          <div class="info-list mt-3">
            <div class="info-item">
              <span class="info-label"
                ><i class="bi bi-person-fill"></i> Khách hàng:</span
              >
              <span class="info-value">{{ detail.hoaDon.tenKhachHang }}</span>
            </div>
            <div class="info-item">
              <span class="info-label"
                ><i class="bi bi-telephone-fill"></i> Số điện thoại:</span
              >
              <span class="info-value">{{
                detail.hoaDon.soDienThoai || "Không có"
              }}</span>
            </div>
            <div class="info-item">
              <span class="info-label"
                ><i class="bi bi-receipt"></i> Loại đơn:</span
              >
              <span class="info-value">{{
                detail.hoaDon.loaiHoaDon === 1 ? "Tại quầy" : "Trực tuyến"
              }}</span>
            </div>
            <div class="info-item">
              <span class="info-label"
                ><i class="bi bi-chat-left-text-fill"></i> Ghi chú:</span
              >
              <span class="info-value">{{
                detail.hoaDon.ghiChu || "Không có"
              }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Thông tin bàn + Tầng -->
      <div class="col-md-6 mt-3 mt-md-0">
        <div class="card p-3 shadow-sm h-100">
          <div class="box-title">
            <h5>
              <i
                class="bi bi-grid-1x2-fill me-2"
                style="font-size: 20px; color: #000"
              ></i>
              Thông tin phục vụ
            </h5>
          </div>
          <div class="info-list mt-3">
            <div class="info-item">
              <span class="info-label"
                ><i class="bi bi-building"></i> Tầng:</span
              >
              <span class="info-value">{{
                detail.hoaDon.tenTang || "Chưa xếp"
              }}</span>
            </div>
            <div class="info-item">
              <span class="info-label"
                ><i class="bi bi-grid-3x3-gap-fill"></i> Bàn:</span
              >
              <span class="info-value">
                <template
                  v-if="
                    detail.hoaDon.listTenBan && detail.hoaDon.listTenBan.length
                  "
                >
                  {{ detail.hoaDon.listTenBan.join(", ") }}
                </template>
                <template v-else>Chưa xếp bàn</template>
              </span>
            </div>
            <div class="info-item">
              <span class="info-label"
                ><i class="bi bi-person-badge-fill"></i> Nhân viên:</span
              >
              <span class="info-value">{{
                detail.hoaDon.tenNhanVien || "Chưa có"
              }}</span>
            </div>
            <div class="info-item">
              <span class="info-label"
                ><i class="bi bi-calendar-check-fill"></i> Hình thức đặt:</span
              >
              <span class="info-value">{{
                detail.hoaDon.hinhThucDat === 1 ? "Trực tuyến" : "Tại quầy"
              }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- BOX 2 + 3 -->
    <div class="row mb-4">
      <!-- LỊCH SỬ -->
      <div class="col-md-6">
        <div class="card p-3 shadow-sm h-100">
          <div class="box-title border-bottom pb-2 mb-3">
            <h5>
              <i
                class="fa-solid fa-layer-group me-2"
                style="font-size: 20px; color: #000"
              ></i>
              Lịch sử hóa đơn
            </h5>
          </div>

          <div class="timeline">
            <div class="timeline-scroll">
              <div
                v-for="(ls, index) in detail.lichSu.slice(0, 10)"
                :key="ls.id"
                class="timeline-item"
              >
                <div class="timeline-left">
                  <div class="dot"></div>
                  <div
                    v-if="index !== detail.lichSu.length - 1"
                    class="line"
                  ></div>
                </div>

                <div class="timeline-content">
                  <div class="action">
                    {{ convertHanhDong(ls.hanhDong) }} : {{ ls.ghiChu }}
                  </div>
                  <div class="time">
                    {{ formatDateTime(ls.thoiGian) }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- MÓN ĂN -->
      <div class="col-md-6">
        <div class="receipt-card p-4 h-100 shadow-sm border">
          <!-- Header -->
          <div class="receipt-header mb-4">
            <div class="receipt-icon">
              <i class="bi bi-receipt"></i>
            </div>
            <div>
              <p class="receipt-sub">Thông tin phục vụ</p>
              <h5 class="receipt-title">Thực đơn</h5>
            </div>
          </div>

          <div class="dish-list">
            <div
              v-for="item in pagedItems"
              :key="item.id"
              class="dish-item d-flex align-items-center py-2 gap-2"
            >
              <img
                :src="getImageUrl(item.hinhAnh)"
                class="food-img flex-shrink-0"
              />

              <div class="flex-grow-1 text-start">
                <div
                  class="dish-title fw-bold text-dark"
                  style="font-size: 0.9rem"
                >
                  {{ item.tenSanPham }}
                </div>
                <span class="dish-category-badge">{{ item.groupLabel }}</span>
              </div>

              <div
                class="dish-qty text-center text-muted fw-semibold"
                style="min-width: 40px; font-size: 0.85rem"
              >
                x{{ item.soLuong }}
              </div>

              <div
                class="dish-price fw-bold text-end"
                style="font-size: 0.95rem; min-width: 80px"
              >
                {{ formatCurrency(item.thanhTien) }}
              </div>
            </div>
          </div>

          <div v-if="monAnTotalPages > 1" class="pagination mt-3">
            <button
              :disabled="monAnPage === 1"
              @click="monAnPage--"
              class="pg-btn pg-arrow"
            >
              <i class="bi bi-chevron-left"></i>
            </button>
            <template v-for="p in monAnPageList" :key="p">
              <button v-if="p === '…'" class="pg-btn pg-ellipsis" disabled>
                ···
              </button>
              <button
                v-else
                class="pg-btn"
                :class="{ active: p === monAnPage }"
                @click="monAnPage = p"
              >
                {{ p }}
              </button>
            </template>
            <button
              :disabled="monAnPage === monAnTotalPages"
              @click="monAnPage++"
              class="pg-btn pg-arrow"
            >
              <i class="bi bi-chevron-right"></i>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- BOX 4: THANH TOÁN -->
    <div class="bill-calc-box mt-4 shadow-sm border">
      <!-- Tổng tiền món -->
      <div class="bill-row">
        <span class="bill-label">Tổng cộng hóa đơn</span>
        <span class="bill-val">{{
          formatCurrency(detail.hoaDon.tongTienMon)
        }}</span>
      </div>

      <!-- Giảm giá voucher -->
      <div
        v-if="detail.hoaDon.soTienDuocGiam > 0"
        class="bill-row bill-row--discount"
      >
        <span class="bill-label">
          Giảm giá
          <span v-if="detail.phieuGiamGia" class="voucher-tag">
            <i class="bi bi-ticket-perforated-fill me-1"></i>
            {{ detail.phieuGiamGia.ma }}
          </span>
        </span>
        <span class="bill-val bill-val--green">
          - {{ formatCurrency(detail.hoaDon.soTienDuocGiam) }}
        </span>
      </div>

      <!-- Giảm trực tiếp -->
      <div
        v-if="detail.hoaDon.giamTrucTiep > 0"
        class="bill-row bill-row--discount"
      >
        <span class="bill-label">Giảm trực tiếp</span>
        <span class="bill-val bill-val--green">
          - {{ formatCurrency(detail.hoaDon.giamTrucTiep) }}
        </span>
      </div>

      <!-- Điểm tích lũy sử dụng -->
      <div
        v-if="detail.hoaDon.diemTichLuySuDung > 0"
        class="bill-row bill-row--discount"
      >
        <span class="bill-label">Điểm tích lũy sử dụng</span>
        <span class="bill-val bill-val--green">
          - {{ formatCurrency(detail.hoaDon.diemTichLuySuDung) }}
        </span>
      </div>

      <!-- Tiền đã cọc -->
      <div
        v-if="detail.hoaDon.trangThai !== 1 && detail.hoaDon.tienCoc > 0"
        class="bill-row bill-row--deposit"
      >
        <span class="bill-label">Tiền đã cọc</span>
        <span class="bill-val bill-val--green">
          - {{ formatCurrency(detail.hoaDon.tienCoc) }}
        </span>
      </div>

      <!-- Tiền đã thanh toán -->
      <div v-if="detail.hoaDon.tongTienDaThanhToan > 0" class="bill-row">
        <span class="bill-label">Tiền đã thanh toán</span>
        <span class="bill-val bill-val--green">
          {{ formatCurrency(detail.hoaDon.tongTienDaThanhToan) }}
        </span>
      </div>

      <!-- Còn lại -->
      <div class="bill-row bill-row--total">
        <span class="bill-label-bold">Tiền còn lại phải thanh toán</span>
        <span class="remaining-value">
          {{ formatCurrency(detail.hoaDon.tienConLai ?? (detail.hoaDon.tongTien - (detail.hoaDon.tongTienDaThanhToan || 0))) }}
        </span>
      </div>
      <div class="bill-actions mt-3">
        <button
          v-if="detail.hoaDon.trangThai === 1"
          class="btn-pay-deposit"
          :disabled="isPaying"
          @click="handleThanhToanCoc"
        >
          <i class="bi bi-credit-card-2-front-fill"></i>
          {{ isPaying ? "Đang xử lý..." : "Thanh toán cọc" }}
        </button>
        <button
          class="btn-cancel"
          :disabled="
            detail.hoaDon.trangThai >= 6 ||
            detail.hoaDon.trangThai === -1 ||
            detail.hoaDon.trangThai === 5
          "
          @click="openModalHuy"
        >
          <i class="ri-close-circle-line"></i>
          Hủy hóa đơn
        </button>
      </div>

      <!-- Dòng nhắc nhở khi không thể hủy -->
      <div
        v-if="
          detail.hoaDon.trangThai >= 6 ||
          detail.hoaDon.trangThai === -1 ||
          detail.hoaDon.trangThai === 5
        "
        class="text-danger text-center mt-2 fw-semibold"
        style="font-size: 0.85rem"
      >
        Trạng thái hóa đơn hiện tại không thể hủy - vui lòng liên hệ 0332654198
      </div>
    </div>
  </div>
  <transition name="fade">
    <div v-if="modalHuy" class="modal-overlay" @click.self="modalHuy = false">
      <div class="modal-cancel">
        <div class="modal-cancel-header">
          <div class="modal-cancel-icon">
            <i class="ri-error-warning-fill"></i>
          </div>
          <div>
            <h3>Hủy đặt bàn</h3>
            <p>Thao tác này không thể hoàn tác sau khi xác nhận</p>
          </div>
          <button class="modal-close" @click="modalHuy = false">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>

        <div class="modal-cancel-body">
          <div v-if="preview" class="refund-summary">
            <div class="refund-row">
              <span
                ><i class="ri-money-dollar-circle-line me-2"></i>Số tiền sẽ được
                hoàn</span
              >
              <span class="refund-positive"
                >+ {{ formatCurrency(preview.tienCocHoan) }}</span
              >
            </div>
            <div class="refund-row">
              <span><i class="ri-subtract-line me-2"></i>Số tiền sẽ mất</span>
              <span class="refund-negative"
                >- {{ formatCurrency(preview.tienCocBiMat) }}</span
              >
            </div>
            <div class="refund-note">
              <i class="ri-information-line"></i>
              Nhân viên sẽ liên hệ và hoàn tiền cọc cho bạn sau khi xác nhận
              hủy.
            </div>
          </div>

          <div class="cancel-reason">
            <label>Lý do hủy <span style="color: #ef4444">*</span></label>
            <textarea
              v-model="lyDoHuy"
              placeholder="Nhập lý do hủy..."
              rows="3"
            ></textarea>
          </div>
        </div>

        <div class="modal-cancel-footer">
          <button class="btn-modal-secondary" @click="modalHuy = false">
            Đóng
          </button>
          <button
            class="btn-modal-danger"
            @click="handleHuyDatBan"
            :disabled="isLoading"
          >
            <i class="bi bi-x-circle me-1"></i>
            {{ isLoading ? "Đang xử lý..." : "Xác nhận hủy" }}
          </button>
        </div>
      </div>
    </div>
  </transition>
</template>

<style scoped>
.timeline-scroll {
  max-height: 420px; /* chỉnh tùy UI */
  overflow-y: auto;
  padding-right: 8px;
}

/* optional: scrollbar đẹp hơn */
.timeline-scroll::-webkit-scrollbar {
  width: 6px;
}

.timeline-scroll::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 10px;
}

.timeline-scroll::-webkit-scrollbar-thumb:hover {
  background: #bbb;
}
.food-group {
  margin-bottom: 18px;
}

.category-title {
  font-size: 14px;
  font-weight: 700;
  color: #111827;
  margin: 10px 0;
  display: flex;
  align-items: center;
}

.category-title i {
  color: #ef4444;
  font-size: 14px;
}

/* list container */
.food-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* item card */
.food-item {
  display: flex;
  align-items: center;
  gap: 12px;

  padding: 5px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #f3f4f6;

  transition: 0.2s;
}

.food-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06);
}

/* image */
.food-img-wrapper {
  width: 50px;
  height: 50px;
  flex-shrink: 0;
}

.food-img {
  width: 100%;
  height: 100%;
  border-radius: 10px;
  object-fit: cover;
}

/* info */
.food-info {
  margin-left: 20px;
  flex: 1;
  min-width: 0;
}

.food-info .name {
  font-weight: 600;
  font-size: 14px;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.food-info .qty {
  font-size: 12px;
  color: #6b7280;
}

/* price */
.price {
  font-weight: 700;
  font-size: 14px;
  color: #ef4444;
  white-space: nowrap;
}

/* /////////////////////////// */

.bill-actions {
  display: flex;
  gap: 12px;
  width: 100%;
}

.btn-pay-deposit {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  border: none;
  border-radius: 10px;
  background-color: #d32f2f;
  color: #ffffff;
  font-weight: 700;
  font-size: 14px;
  transition: all 0.2s ease;
  cursor: pointer;
  height: 46px;
}

.btn-pay-deposit i {
  font-size: 18px;
  line-height: 1;
}

.btn-pay-deposit:hover:not(:disabled) {
  background-color: #b71c1c;
  transform: translateY(-1px);
}

.btn-pay-deposit:active:not(:disabled) {
  transform: translateY(1px);
}

.btn-pay-deposit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background-color: #cbd5e1;
  color: #64748b;
}

.btn-cancel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  border: none;
  border-radius: 10px;
  background-color: #fee2e2;
  color: #dc2626;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.2s ease;
  cursor: pointer;
  height: 46px;
}

.btn-cancel i {
  font-size: 18px;
  line-height: 1;
}

.btn-cancel:hover:not(:disabled) {
  background-color: #fecaca;
  transform: translateY(-1px);
}

.btn-cancel:active:not(:disabled) {
  transform: translateY(1px);
}

.btn-cancel:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  background-color: #f1f5f9;
  color: #94a3b8;
}

.box-title h5 {
  font-size: 20px;
}
/* //////////// LỊCH SỬ //////////// */
.timeline {
  position: relative;
  margin-top: 10px;
  font-family: "Inter", -apple-system, BlinkMacSystemFont, sans-serif;
}

.timeline-item {
  display: flex;
  gap: 16px;
  position: relative;
  overflow: hidden;
}

.timeline-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 14px;
}

.dot {
  width: 12px;
  height: 12px;
  background: #eb2525;
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px #fb3434;
  border-radius: 50%;
  z-index: 1;
  transition: transform 0.2s ease;
}

.timeline-item:hover .dot {
  transform: scale(1.2);
}

.line {
  width: 2px;
  flex: 1;
  background: #e5e7eb;
  margin-top: 4px;
  margin-bottom: 4px;
  border-radius: 2px;
}

.timeline-item:last-child .line {
  display: none;
}

.timeline-content {
  flex: 1;
  padding-bottom: 14px;
}

.action {
  display: block;
  font-weight: 600;
  font-size: 15px;
  color: #1f2937;
  line-height: 1.4;
  margin-bottom: 2px;
}

.time {
  font-size: 13px;
  color: #6b7280;
  display: flex;
  align-items: center;
}
/* /////////////////////////// TRẠNG THÁI /////////////// */
.order-detail-card {
  border-radius: 16px;
  background: #fff;
  border: 1px solid #eee;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.06);
  transition: 0.3s;
}

.order-detail-card:hover {
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.1);
}

.order-header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30px;
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 6px;

  border: none;
  background: #e6e2e2;
  color: #333;

  padding: 12px 18px;
  border-radius: 20px;

  font-size: 13px;
  font-weight: 700;

  cursor: pointer;
  transition: 0.25s;
}

.btn-back:hover {
  background: #d5cccc;
  color: #fff;
  transform: translateX(-2px);
}

/* STATUS TEXT */
.status-text h4 {
  font-weight: 700;
  font-size: 18px;
  margin: 0;
  color: #222;
}

/* STEP */
/* Timeline Stepper */
.stepper-wrapper {
  position: relative;
  margin-top: 20px;
}

.step-bar {
  position: absolute;
  top: 36px;
  left: 10%;
  right: 10%;
  height: 4px;
  background: #e5e7eb;
  z-index: 1;
}

.step-progress {
  height: 100%;
  background: #ec2229;
  transition: width 0.5s ease;
}

.steps-container {
  position: relative;
  z-index: 2;
  width: 100%;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 20%;
}

.step-number {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #ffffff;
  border: 3px solid #e5e7eb;
  color: #9ca3af;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  transition: all 0.3s ease;
}

.step-item.active .step-number {
  border-color: #ec2229;
  background: #ec2229;
  color: #ffffff;
  box-shadow: 0 0 15px rgba(236, 34, 41, 0.4);
}

.step-label {
  font-size: 0.85rem;
  font-weight: 700;
  margin-top: 10px;
  color: #4b5563;
}

.step-item.active .step-label {
  color: #111;
}
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 14px;
  margin-left: 6px;

  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.2px;

  border-radius: 20px;
  border: 1px solid transparent;

  background: #f5f5f5;
  color: #555;

  transition: all 0.2s ease;
}

.status-badge i {
  font-size: 13px;
}

.status-new {
  background: #e3f2fd;
  border-color: #bbdefb;
  color: #1976d2;
}

.status-pending {
  background: #fff3e0;
  border-color: #ffe0b2;
  color: #ef6c00;
}

.status-success {
  background: #e8f5e9;
  border-color: #c8e6c9;
  color: #2e7d32;
}

.status-active {
  background: #e0f7fa;
  border-color: #b2ebf2;
  color: #00838f;
}

.status-done {
  background: #f8f5fd;
  border-color: #e9c4c4;
  color: #ec4628;
}

.status-cancel {
  background: #ebfeff;
  border-color: #ffcdd2;
  color: #c62828;
}
.cancel-simple {
  margin-top: 12px;

  display: flex;
  align-items: center;
  gap: 6px;

  font-size: 13px;
  font-weight: 600;

  color: #d32f2f;
}

.icon-btn-back {
  margin-left: auto;
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background-color: #f4f6f8;
  border: 1px solid #e9e9e9;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.icon-btn-back:hover {
  background-color: #e2e6ea;
  transform: translateX(-2px);
}

.icon-btn-back img {
  width: 20px;
  height: 20px;
  object-fit: contain;
}
/* ////////////////////////////////////// */

.status-text {
  font-weight: 700;
  color: #d32f2f;
}

.status {
  font-weight: 700;
  color: #d32f2f;
}

.timeline-item {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.dot {
  width: 10px;
  height: 10px;
  background: red;
  border-radius: 50%;
  margin-top: 5px;
}

/* CSS Của Món Ăn */
.receipt-card {
  background: #fff;
  border-radius: 16px;
}

.receipt-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding-bottom: 16px;
  border-bottom: 2px dashed #f0f0f0;
}

.receipt-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #fff1f2, #ffe4e6);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.3rem;
  color: #e65a5a;
  flex-shrink: 0;
}

.receipt-sub {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 2px;
  color: #ee2d2d;
  margin: 0 0 2px 0;
}

.receipt-title {
  font-size: 15px;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

/* Dish list */
.dish-list {
  border-radius: 10px;
  overflow: hidden;
  background: #fafafa;
  border: 1px solid #f3f4f6;
}

.dish-item {
  gap: 12px;
  padding-left: 10px;
  padding-right: 10px;
  border-bottom: 1px dashed #f0f0f0;
}

.dish-item:last-child {
  border-bottom: none;
}

.dish-category-badge {
  display: inline-block;
  margin-top: 3px;
  padding: 2px 10px;
  font-size: 13px;
  font-weight: 600;
  color: #ed1616;
  background: #e7fbfc;
  border: 1px solid #f5eeee;
  border-radius: 7px;
  letter-spacing: 0.3px;
}

.food-img {
  width: 45px;
  height: 45px;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid #eee;
  flex-shrink: 0;
}

/* CSS của Thanh Toán */
.remaining-value {
  font-size: 1.4rem;
  font-weight: 800;
  color: #dc2626;
}

.bill-calc-box {
  background: #f9fafb;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.bill-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
}

.bill-label {
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.bill-val {
  font-weight: 600;
  color: #111827;
}

.bill-val--green {
  color: #16a34a;
  font-weight: 600;
}

.bill-row--total {
  border-top: 1px dashed #e5e7eb;
  padding-top: 10px;
  margin-top: 2px;
}

.bill-label-bold {
  font-weight: 700;
  color: #111827;
  font-size: 0.9rem;
}

.voucher-tag {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  background: #f5e0e0;
  border: 1px solid #fcd34d;
  color: #ed2929;
  font-size: 0.72rem;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 20px;
  letter-spacing: 0.3px;
}

.voucher-dot {
  color: #b45309;
  font-weight: 900;
  font-size: 0.9rem;
  margin: 0 1px;
}
button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* //////////////////// HỦY HÓA ĐƠN ///////////// */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-cancel {
  background: #fff;
  width: 100%;
  max-width: 460px;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
}

.modal-cancel-header {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 24px;
  border-bottom: 1px solid #f3f4f6;
}

.modal-cancel-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: #fef2f2;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #ef4444;
  flex-shrink: 0;
}

.modal-cancel-header h3 {
  margin: 0 0 4px;
  font-size: 17px;
  font-weight: 700;
  color: #111;
}

.modal-cancel-header p {
  margin: 0;
  font-size: 13px;
  color: #9ca3af;
}

.modal-close {
  margin-left: auto;
  background: #f3f4f6;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #6b7280;
  flex-shrink: 0;
}

.modal-close:hover {
  background: #e5e7eb;
}

.modal-cancel-body {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.refund-summary {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
}

.refund-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  font-size: 14px;
  color: #374151;
  border-bottom: 1px solid #e5e7eb;
}

.refund-positive {
  font-weight: 700;
  color: #16a34a;
}
.refund-negative {
  font-weight: 700;
  color: #ef4444;
}

.refund-note {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px 16px;
  font-size: 13px;
  color: #92400e;
  background: #fefce8;
}

.refund-note i {
  color: #f59e0b;
  flex-shrink: 0;
  margin-top: 2px;
}

.cancel-reason label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.cancel-reason textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  font-size: 14px;
  resize: none;
  font-family: inherit;
  box-sizing: border-box;
}

.cancel-reason textarea:focus {
  outline: none;
  border-color: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}

.modal-cancel-footer {
  display: flex;
  gap: 10px;
  padding: 16px 24px;
  background: #f9fafb;
  border-top: 1px solid #f3f4f6;
}

.btn-modal-secondary {
  flex: 1;
  padding: 11px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  cursor: pointer;
}

.btn-modal-danger {
  flex: 2;
  padding: 11px;
  border: none;
  border-radius: 8px;
  background: #ef4444;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-modal-danger:hover:not(:disabled) {
  background: #dc2626;
}
.btn-modal-danger:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.info-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.info-label .bi {
  color: #f79999;
  font-size: 0.95rem;
}
.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px dashed #eee;
  padding-bottom: 8px;
}

.info-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.info-label {
  color: #6b7280;
  font-size: 14px;
  font-weight: 500;
}

.info-value {
  color: #111827;
  font-size: 14px;
  font-weight: 600;
  text-align: right;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  user-select: none;
}
.pg-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
  padding: 0 6px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  background: #fff;
  color: #333;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}
.pg-btn:hover:not(:disabled):not(.active) {
  border-color: #d32f2f;
  color: #d32f2f;
  background: #fff1f0;
}
.pg-btn.active {
  border: 1.5px solid #d32f2f;
  color: #d32f2f;
  font-weight: 600;
}
.pg-btn:disabled {
  border-color: #f0f0f0;
  background: #f5f5f5;
  color: #bfbfbf;
  cursor: not-allowed;
}
.pg-ellipsis {
  border: none;
  background: transparent;
  cursor: default;
  color: #aaa;
  min-width: 20px;
}
.pg-arrow i {
  font-size: 14px;
}
.status-bad-debt {
  background: #fef2f2;
  border-color: #fca5a5;
  color: #dc2626;
}
.debt-notice {
  background: #fff7ed;
  border-color: #fdba74 !important;
  color: #c2410c !important;
}
.bad-debt-notice {
  background: #fef2f2;
  border: 1px solid #f87171 !important;
  color: #b91c1c !important;
}
</style>
