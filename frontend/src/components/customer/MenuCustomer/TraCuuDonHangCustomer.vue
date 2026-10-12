<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { traCuuDatBan } from "@/api/customer/tracuu/traCuuApi";
import {
  connectWebSocketTraCuu,
  disconnectWebSocket,
  subscribeMaDatBan,
} from "@/utils/websocket";

const formatDateTime = (isoString) => {
  if (!isoString) return "--";
  const date = new Date(isoString);
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const year = date.getFullYear();
  return `${hours}:${minutes} - ${day}/${month}/${year}`;
};

const searchCode = ref("");
const searchResult = ref(null);
const hasSearched = ref(false);
const isLoading = ref(false);

const getHoaDonStatusKey = (status) => {
  if (status === null || status === undefined) return "none";
  const s = String(status).toUpperCase();
  if (s === "11" || s.includes("NO_XAU") || s.includes("NỢ XẤU")) return "bad_debt";
  if (s === "10" || s.includes("TREO_NO") || s.includes("TREO NỢ")) return "debt";
  if (s === "-1" || s.includes("DA_HUY") || s.includes("ĐÃ HỦY")) return "cancelled";
  if (s === "8" || s.includes("DA_THANH_TOAN") || s.includes("ĐÃ THANH TOÁN"))
    return "fully_paid";
  if (s === "7" || s.includes("CHO_THANH_TOAN") || s.includes("CHỜ THANH TOÁN"))
    return "payment";
  if (s === "9" || s.includes("HOAN_THANH") || s.includes("HOÀN THÀNH")) return "completed";
  return "confirmed";
};

const hoaDonIconMap = {
  bad_debt: "bi bi-exclamation-triangle-fill",
  debt: "bi bi-exclamation-octagon-fill",
  cancelled: "bi bi-x-circle-fill",
  fully_paid: "bi bi-check-circle-fill",
  payment: "bi bi-credit-card",
  completed: "bi bi-award-fill",
  confirmed: "bi bi-hourglass-split",
  none: "bi bi-question-circle",
};

const getHoaDonStatusIcon = (status) =>
  hoaDonIconMap[getHoaDonStatusKey(status)] || "bi bi-question-circle";

const getStatusKey = (status) => {
  if (status === null || status === undefined) return "pending";
  const s = String(status).toUpperCase();
  if (s === "11" || s.includes("NO_XAU") || s.includes("NỢ XẤU")) return "bad_debt";
  if (s === "10" || s.includes("TREO_NO") || s.includes("TREO NỢ")) return "debt";
  if (s.includes("HOAN_THANH") || s.includes("HOÀN THÀNH")) return "completed";
  if (
    s.includes("CHO_THANH_TOAN") ||
    s.includes("CHỜ THANH TOÁN") ||
    s.includes("DA_THANH_TOAN") ||
    s.includes("ĐÃ THANH TOÁN")
  )
    return "payment";
  if (
    s.includes("DANG_PHUC_VU") ||
    s.includes("ĐANG PHỤC VỤ") ||
    s.includes("KHACH_DEN") ||
    s.includes("KHÁCH ĐẾN")
  )
    return "dining";
  if (
    s.includes("DA_HUY") ||
    s.includes("HỦY") ||
    s.includes("HET_HAN") ||
    s.includes("HẾT HẠN") ||
    s.includes("HOAN_COC") ||
    s.includes("HOÀN CỌC")
  )
    return "cancelled";
  if (
    s.includes("CHO_XAC_NHAN") ||
    s.includes("CHỜ XÁC NHẬN") ||
    s.includes("CHUA_COC") ||
    s.includes("CHƯA CỌC") ||
    s.includes("VUA_TAO") ||
    s.includes("VỪA TẠO")
  )
    return "pending";
  return "confirmed";
};

const getDepositStatusKey = (status) => {
  if (status === null || status === undefined) return "unpaid";
  const s = String(status).toUpperCase();
  if (
    s.includes("CHUA_COC") ||
    s.includes("CHƯA CỌC") ||
    s.includes("KHONG_CAN_COC") ||
    s.includes("KHÔNG CẦN CỌC")
  )
    return "unpaid";
  if (s.includes("HOAN_THANH") || s.includes("HOÀN THÀNH")) return "fully_paid";
  if (
    s.includes("HUY_COC") ||
    s.includes("HỦY CỌC") ||
    s.includes("CHO_HOAN_COC") ||
    s.includes("CHỜ HOÀN CỌC")
  )
    return "cancelled";
  return "paid"; // Gom: DA_COC
};

const statusIconMap = {
  confirmed: "bi bi-check2-circle",
  pending: "bi bi-hourglass-split",
  dining: "bi bi-cup-hot-fill",
  completed: "bi bi-check-circle-fill",
  cancelled: "bi bi-x-circle-fill",
  payment: "bi bi-credit-card",
  bad_debt: "bi bi-exclamation-triangle-fill",
  debt: "bi bi-exclamation-octagon-fill",
};

const depositIconMap = {
  unpaid: "bi bi-exclamation-circle-fill",
  paid: "bi bi-cash-coin",
  fully_paid: "bi bi-check-circle-fill",
  cancelled: "bi bi-x-circle-fill",
};

const getStatusIcon = (status) =>
  statusIconMap[getStatusKey(status)] || "bi bi-question-circle";
const getDepositStatusIcon = (status) =>
  depositIconMap[getDepositStatusKey(status)] || "bi bi-question-circle";
const isDebtPending = computed(() => {
  if (!searchResult.value) return false;
  return (
    searchResult.value.status === "completed" &&
    searchResult.value.hoaDonStatus === "debt"
  );
});
const isBadDebt = computed(() => {
  if (!searchResult.value) return false;
  return (
    searchResult.value.hoaDonStatus === "bad_debt" ||
    searchResult.value.status === "bad_debt"
  );
});
const handleSearch = async () => {
  const codeClean = searchCode.value.trim().toUpperCase();
  if (!codeClean) return;

  isLoading.value = true;
  searchResult.value = null;
  hasSearched.value = false;

  try {
    const response = await traCuuDatBan(codeClean);
    const data = response.data;

    if (data) {
      searchResult.value = {
        code: data.maDatBan,
        status: getStatusKey(data.trangThai),
        statusText: data.trangThai || "Không xác định",
        depositStatus: getDepositStatusKey(data.trangThaiCoc),
        depositStatusText: data.trangThaiCoc || "Không có",
        hoaDonStatus: getHoaDonStatusKey(data.hoaDon?.trangThai),
        hoaDonStatusText: data.hoaDon?.trangThai || null,
        createdAt: formatDateTime(data.ngayTao),
        bookingTime: formatDateTime(data.thoiGianDat),
        ngayCapNhat: formatDateTime(data.ngayCapNhat),
        guests: data.soNguoiToiDa || 0,
        depositAmount: data.tienCoc || 0,
        depositMethod: "Chuyển khoản",
        note: data.ghiChu || "Không có ghi chú.",
        customer: {
          name: data.khachHang?.tenKhachHang || "Khách vãng lai",
          phone: data.khachHang?.soDienThoai || "Không có",
          email: data.khachHang?.email || "Không có",
        },
        table: {
          name:
            data.banAns?.length > 0
              ? data.banAns.map((b) => b.tenBanAn).join(", ")
              : "Chưa xếp bàn",
          zone: data.banAns?.length > 0 ? data.banAns[0].tenKhuVuc : "Chưa xếp",
          capacity:
            data.banAns?.length > 0
              ? `Chứa tối đa ${data.banAns.reduce(
                  (sum, b) => sum + (b.sucChuaToiDa || 0),
                  0,
                )} người`
              : "--",
        },
        items:
          data.hoaDon?.monAns?.map((m) => ({
            name: m.tenMon,
            hinhAnh: m.hinhAnh,
            qty: m.soLuong,
            price: m.donGia,
            tongTienMon: m.tongTienMon,
            tenDanhMuc: m.tenDanhMuc,
          })) || [],
        tongTienMon: data.hoaDon?.tongTienMon ?? 0,
        soTienDuocGiam: data.hoaDon?.soTienDuocGiam ?? 0,
        voucher: data.hoaDon?.phieuGiamGia
          ? {
              code: data.hoaDon.phieuGiamGia.maGiamGia,
              name: data.hoaDon.phieuGiamGia.tenGiamGia,
              kieuGiamGia: data.hoaDon.phieuGiamGia.kieuGiamGia,
              giaTriGiam: data.hoaDon.phieuGiamGia.giaTriGiam,
              giamToiDa: data.hoaDon.phieuGiamGia.giamToiDa,
            }
          : null,
        discountAmount: data.hoaDon?.soTienDuocGiam ?? 0,
        totalBill: data.hoaDon?.tongTienMon || 0,
        tienConLai: data.hoaDon?.tienConLai ?? 0,
        giamTrucTiep: data.hoaDon?.giamTrucTiep ?? 0,
        diemTichLuySuDung: data.hoaDon?.diemTichLuySuDung ?? 0,
        tongTienDaThanhToan: data.hoaDon?.tongTienDaThanhToan ?? 0,
        tongTien: data.hoaDon?.tongTien ?? 0,
      };
      dishPage.value = 1;
      subscribeMaDatBan(data.maDatBan);
    } else {
      searchResult.value = null;
    }
  } catch (error) {
    console.log(error.response?.status);
    console.log(error.response?.data);
    console.error(error);
  } finally {
    isLoading.value = false;
    hasSearched.value = true;
  }
};

// Phân trang danh sách món ăn
const dishPage = ref(1);
const dishPageSize = 5;

const pagedItems = computed(() => {
  if (!searchResult.value) return [];
  const start = (dishPage.value - 1) * dishPageSize;
  return searchResult.value.items.slice(start, start + dishPageSize);
});

const dishTotalPages = computed(() => {
  if (!searchResult.value) return 0;
  return Math.ceil(searchResult.value.items.length / dishPageSize);
});

const dishPageList = computed(() => {
  const total = dishTotalPages.value;
  if (total <= 5) return Array.from({ length: total }, (_, i) => i + 1);
  const pages = [1, 2, 3];
  if (total > 4) pages.push("…");
  pages.push(total);
  return pages;
});

const getImageUrl = (img) => {
  if (!img) return "https://via.placeholder.com/50";

  return `http://localhost:8080/uploads/${img}`;
};

const remainingPayment = computed(() => {
  if (!searchResult.value) return 0;
  return searchResult.value.tienConLai || 0;
});

////////////////////////////////// REALTIIME //////////////////////////////
const handleSocketMessage = (data) => {
  if (data.type !== "DAT_BAN_TRA_CUU") return;
  if (!searchResult.value || data.maDatBan !== searchResult.value.code) return;

  if (data.hoaDon?.tienConLai != null) {
    searchResult.value.tienConLai = data.hoaDon.tienConLai;
  }
  if (data.hoaDon?.tongTienMon != null) {
    searchResult.value.totalBill = data.hoaDon.tongTienMon;
  }
  if (data.hoaDon?.tongTien != null) {
    searchResult.value.tongTien = data.hoaDon.tongTien;
  }
  if (data.hoaDon?.giamTrucTiep != null) {
    searchResult.value.giamTrucTiep = data.hoaDon.giamTrucTiep;
  }
  if (data.hoaDon?.diemTichLuySuDung != null) {
    searchResult.value.diemTichLuySuDung = data.hoaDon.diemTichLuySuDung;
  }
  if (data.hoaDon?.tongTienDaThanhToan != null) {
    searchResult.value.tongTienDaThanhToan = data.hoaDon.tongTienDaThanhToan;
  }
  searchResult.value.status = getStatusKey(data.trangThai);
  searchResult.value.statusText =
    data.trangThai || searchResult.value.statusText;
  searchResult.value.depositStatus = getDepositStatusKey(data.trangThaiCoc);
  searchResult.value.depositStatusText =
    data.trangThaiCoc || searchResult.value.depositStatusText;
  if (data.hoaDon?.trangThai != null) {
    searchResult.value.hoaDonStatus = getHoaDonStatusKey(data.hoaDon.trangThai);
    searchResult.value.hoaDonStatusText = data.hoaDon.trangThai;
  }
  if (data.listTenBan?.length) {
    searchResult.value.table.name = data.listTenBan.join(", ");
  }
  if (data.tenKhuVuc) {
    searchResult.value.table.zone = data.tenKhuVuc;
  }
  if (data.tienCoc != null) {
    searchResult.value.depositAmount = data.tienCoc;
  }
  if (data.ghiChu != null) {
    searchResult.value.note = data.ghiChu;
  }
};

onMounted(() => {
  connectWebSocketTraCuu(null, handleSocketMessage);
});

onUnmounted(() => {
  disconnectWebSocket(handleSocketMessage);
});
</script>

<template>
  <main class="lookup-page py-5">
    <div class="container">
      <!-- Header -->
      <header class="text-center mb-5 reveal active">
        <span class="sub-title">tra cứu thông tin</span>
        <h2 class="section-title">Kiểm Tra Đơn Đặt Bàn</h2>
        <div class="luxury-line"></div>
        <p class="section-desc max-w-2xl mx-auto text-muted">
          Nhập mã đặt bàn của Quý khách (được gửi qua email hoặc SMS sau khi đặt
          bàn) để theo dõi trạng thái, vị trí bàn phục vụ và danh sách món ăn đã
          đặt trước.
        </p>
      </header>

      <!-- Search Box Card -->
      <div class="row justify-content-center mb-5">
        <div class="col-lg-7">
          <div
            class="luxury-card search-card p-4 p-md-5 bg-white text-center shadow-sm"
          >
            <h5 class="form-section-label mb-4" style="font-size: 1.1rem">
              Nhập mã đặt bàn
            </h5>

            <div
              class="d-flex flex-column flex-md-row gap-3 justify-content-center mb-4"
            >
              <div class="search-input-group flex-grow-1">
                <i class="bi bi-qr-code search-input-icon"></i>
                <input
                  v-model="searchCode"
                  type="text"
                  class="luxury-input-field"
                  placeholder="Ví dụ: DB-1234"
                  @keyup.enter="handleSearch"
                />
              </div>

              <button
                class="btn-search-luxury"
                :disabled="isLoading || !searchCode.trim()"
                @click="handleSearch"
              >
                <span v-if="!isLoading">Tìm</span>
                <span v-else class="d-flex align-items-center gap-2">
                  <span
                    class="spinner-border spinner-border-sm"
                    role="status"
                    aria-hidden="true"
                  ></span>
                  Đang tìm...
                </span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="isLoading" class="text-center py-5">
        <div class="sliding-puzzle-loader mx-auto mb-3">
          <div class="puzzle-piece piece-1"></div>
          <div class="puzzle-piece piece-2"></div>
          <div class="puzzle-piece piece-3"></div>
        </div>
        <p class="text-muted mt-4">
          Đang kết nối hệ thống và truy xuất dữ liệu...
        </p>
      </div>

      <!-- Result Not Found -->
      <div
        v-if="!isLoading && hasSearched && !searchResult"
        class="row justify-content-center"
      >
        <div class="col-lg-7">
          <div
            class="no-results-card p-5 text-center bg-white rounded-4 border shadow-sm"
          >
            <div class="warning-icon-wrapper mb-3">
              <i class="bi bi-exclamation-triangle-fill"></i>
            </div>
            <h5 class="fw-bold">
              Không tìm thấy mã đặt bàn "{{ searchCode.toUpperCase().trim() }}"
            </h5>
            <p class="text-muted max-w-md mx-auto mt-2">
              Quý khách vui lòng kiểm tra kỹ lại mã đặt bàn. Đảm bảo mã viết
              đúng định dạng gồm ký tự kèm số phía sau.
            </p>
            <div class="alert alert-info mt-4 text-start small">
              <strong>Hướng dẫn:</strong>
              <ul class="mb-0 ps-3 mt-1">
                <li>
                  Kiểm tra lại hộp thư Email (hoặc thư rác) lúc tạo đặt bàn
                  thành công.
                </li>
                <li>
                  Đảm bảo không copy khoảng trắng dư thừa ở đầu hoặc cuối mã.
                </li>
                <li>
                  Nếu cần hỗ trợ gấp, vui lòng liên hệ hotline
                  <strong>0332 654 198</strong>.
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <!-- Result Found -->
      <div
        v-if="!isLoading && hasSearched && searchResult"
        class="result-details-section"
      >
        <!-- Status Stepper -->
        <div
          class="luxury-card stepper-card p-4 bg-white border mb-4 shadow-sm"
        >
          <div
            class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 pb-3 border-bottom mb-4"
          >
            <div>
              <h5 class="fw-bold mb-0">
                <span class="text-gold">Mã đơn hàng: </span>
                <span class="text-danger">{{ searchResult.code }}</span>
              </h5>
            </div>
            <div class="d-flex gap-2">
              <span class="badge-status" :class="searchResult.status">
                <i :class="getStatusIcon(searchResult.status)"></i>
                {{ searchResult.statusText }}
              </span>
              <span class="badge-status" :class="searchResult.depositStatus">
                <i
                  :class="getDepositStatusIcon(searchResult.depositStatus)"
                ></i>
                {{ searchResult.depositStatusText }}
              </span>
            </div>
          </div>

          <!-- Stepper Graphic (Only if not cancelled and not debt/bad debt) -->
          <div
            v-if="searchResult.status !== 'cancelled' && !isDebtPending && !isBadDebt"
            class="stepper-wrapper py-3"
          >
            <div class="step-bar">
              <div
                class="step-progress"
                :style="{
                  width:
                    searchResult.status === 'pending'
                      ? '0%'
                      : searchResult.status === 'confirmed'
                      ? '25%'
                      : searchResult.status === 'dining'
                      ? '50%'
                      : searchResult.status === 'payment'
                      ? '75%'
                      : '100%',
                }"
              ></div>
            </div>
            <div class="steps-container d-flex justify-content-between">
              <div class="step-item" :class="{ active: true }">
                <div class="step-number">
                  <i class="bi bi-journal-check"></i>
                </div>
                <div class="step-label">Vừa Tạo</div>
                <div class="step-time">{{ searchResult.createdAt }}</div>
              </div>
              <div
                class="step-item"
                :class="{
                  active:
                    searchResult.status === 'confirmed' ||
                    searchResult.status === 'dining' ||
                    searchResult.status === 'payment' ||
                    searchResult.status === 'completed',
                }"
              >
                <div class="step-number"><i class="bi bi-wallet2"></i></div>
                <div class="step-label">Xác Nhận</div>
                <div
                  class="step-time"
                  v-if="
                    searchResult.status === 'confirmed' ||
                    searchResult.status === 'dining' ||
                    searchResult.status === 'payment' ||
                    searchResult.status === 'completed'
                  "
                >
                  Xác nhận lịch
                </div>
              </div>
              <div
                class="step-item"
                :class="{
                  active:
                    searchResult.status === 'dining' ||
                    searchResult.status === 'payment' ||
                    searchResult.status === 'completed',
                }"
              >
                <div class="step-number"><i class="bi bi-shop-window"></i></div>
                <div class="step-label">Phục Vụ</div>
                <div
                  class="step-time"
                  v-if="
                    searchResult.status === 'dining' ||
                    searchResult.status === 'payment' ||
                    searchResult.status === 'completed'
                  "
                >
                  {{ searchResult.bookingTime.split(" - ")[0] }}
                </div>
              </div>
              <div
                class="step-item"
                :class="{
                  active:
                    searchResult.status === 'payment' ||
                    searchResult.status === 'completed',
                }"
              >
                <div class="step-number"><i class="bi bi-credit-card"></i></div>
                <div class="step-label">Thanh Toán</div>
                <div
                  class="step-time"
                  v-if="
                    searchResult.status === 'payment' ||
                    searchResult.status === 'completed'
                  "
                >
                  Thanh toán HĐ
                </div>
              </div>
              <div
                class="step-item"
                :class="{ active: searchResult.status === 'completed' }"
              >
                <div class="step-number"><i class="bi bi-award"></i></div>
                <div class="step-label">Hoàn Thành</div>
                <div
                  class="step-time"
                  v-if="searchResult.status === 'completed'"
                >
                  Hoàn tất hóa đơn
                </div>
              </div>
            </div>
          </div>
          <!-- Debt Notice -->
          <div
            v-else-if="isDebtPending"
            class="debt-notice p-3 rounded-3 text-center border-danger-subtle bg-danger-subtle text-danger"
          >
            <i class="bi bi-exclamation-octagon-fill me-2"></i>
            Đơn đặt bàn đã hoàn thành nhưng hóa đơn vẫn đang
            <strong>treo nợ</strong>. Số tiền còn lại cần thanh toán:
            <strong>{{ remainingPayment.toLocaleString() }}đ</strong>.
          </div>
          <!-- Bad Debt Notice -->
          <div
            v-else-if="isBadDebt"
            class="bad-debt-notice p-3 rounded-3 text-center"
          >
            <i class="bi bi-exclamation-triangle-fill me-2"></i>
            Hóa đơn đã bị chuyển sang trạng thái <strong>nợ xấu</strong> do chưa thanh toán khoản nợ quá hạn. Số tiền chưa thanh toán:
            <strong>{{ remainingPayment.toLocaleString() }}đ</strong>. Quý khách vui lòng liên hệ nhà hàng (Hotline: <strong>0332 654 198</strong>) để được hỗ trợ và tất toán khoản nợ.
          </div>
          <!-- Cancelled Notice -->
          <div
            v-else
            class="cancelled-notice p-3 rounded-3 text-center border-danger-subtle bg-danger-subtle text-danger"
          >
            <i class="bi bi-x-circle-fill me-2"></i>
            Đơn hàng đặt bàn này đã bị hủy bỏ vào lúc
            <strong>{{ searchResult.ngayCapNhat }}</strong
            >. Vui lòng liên hệ hỗ trợ hoặc đặt bàn mới.
          </div>
        </div>

        <div class="row g-4">
          <!-- Left Column: Info Panels -->
          <div class="col-lg-7">
            <div class="d-flex flex-column gap-4">
              <!-- Customer Info Card -->
              <div class="luxury-card info-card p-4 bg-white border shadow-sm">
                <div class="card-title-luxury border-bottom pb-2 mb-3">
                  <h5 class="form-section-label mb-0" style="font-size: 1rem">
                    <i class="bi bi-person-fill text-gold me-2"></i>Thông Tin
                    Khách Hàng
                  </h5>
                </div>
                <div class="row g-3">
                  <div class="col-md-6">
                    <label class="info-label">
                      <i class="bi bi-person me-1 text-gold"></i>
                      Họ tên
                    </label>
                    <div class="info-value">
                      {{ searchResult.customer.name }}
                    </div>
                  </div>

                  <div class="col-md-6">
                    <label class="info-label">
                      <i class="bi bi-telephone me-1 text-gold"></i>
                      Số điện thoại
                    </label>
                    <div class="info-value">
                      {{ searchResult.customer.phone }}
                    </div>
                  </div>

                  <div class="col-12">
                    <label class="info-label">
                      <i class="bi bi-envelope me-1 text-gold"></i>
                      Email
                    </label>
                    <div class="info-value">
                      {{ searchResult.customer.email }}
                    </div>
                  </div>
                </div>
              </div>

              <!-- Booking details Card -->
              <div class="luxury-card info-card p-4 bg-white border shadow-sm">
                <div class="card-title-luxury border-bottom pb-2 mb-3">
                  <h5 class="form-section-label mb-0" style="font-size: 1rem">
                    <i class="bi bi-calendar-event-fill text-gold me-2"></i>Chi
                    Tiết Lịch Hẹn
                  </h5>
                </div>
                <div class="row g-3">
                  <div class="col-md-6">
                    <label class="info-label">
                      <i class="bi bi-clock-history me-1 text-gold"></i>
                      Thời gian phục vụ
                    </label>
                    <div class="info-value text-danger font-bold">
                      {{ searchResult.bookingTime }}
                    </div>
                  </div>

                  <div class="col-md-6">
                    <label class="info-label">
                      <i class="bi bi-people-fill me-1 text-gold"></i>
                      Số lượng khách đặt
                    </label>
                    <div class="info-value">
                      {{ searchResult.guests }} người
                    </div>
                  </div>

                  <div class="col-12">
                    <label class="info-label">
                      <i class="bi bi-chat-left-text-fill me-1 text-gold"></i>
                      Ghi chú yêu cầu đặc biệt
                    </label>
                    <div class="info-value note-box">
                      {{ searchResult.note || "Không có ghi chú đặc biệt." }}
                    </div>
                  </div>
                </div>
              </div>

              <!-- Table Assignment Card -->
              <div class="luxury-card info-card p-4 bg-white border shadow-sm">
                <div class="card-title-luxury border-bottom pb-2 mb-3">
                  <h5 class="form-section-label mb-0" style="font-size: 1rem">
                    <i class="bi bi-geo-alt-fill text-gold me-2"></i>Vị Trí Bàn
                    Phục Vụ
                  </h5>
                </div>
                <div
                  v-if="
                    searchResult.status !== 'cancelled' &&
                    searchResult.status !== 'pending'
                  "
                  class="row g-3"
                >
                  <div class="col-md-5 text-center border-end-md">
                    <div class="table-name-badge my-2">
                      {{ searchResult.table.name }}
                    </div>
                  </div>
                  <div
                    class="col-md-7 d-flex flex-column justify-content-center"
                  >
                    <div class="mb-2">
                      <div class="info-label">
                        <i class="bi bi-geo-alt-fill me-1 text-gold"></i>
                        Khu vực
                      </div>
                      <div class="info-value-small">
                        {{ searchResult.table.zone }}
                      </div>
                    </div>

                    <div>
                      <div class="info-label">
                        <i class="bi bi-people-fill me-1 text-gold"></i>
                        Sức chứa bàn
                      </div>
                      <div class="info-value-small">
                        {{ searchResult.table.capacity }}
                      </div>
                    </div>
                  </div>
                </div>
                <div v-else class="text-muted text-center py-3 italic">
                  <i class="bi bi-info-circle me-1"></i>
                  Vị trí bàn cụ thể sẽ được xếp khi nhân viên xác nhận phiếu đặt
                  bàn
                </div>
              </div>
            </div>
          </div>

          <!-- Right Column: Pre-order Bill Receipt -->
          <div class="col-lg-5">
            <div class="receipt-card p-4 sticky-top" style="top: 100px">
              <!-- Header -->
              <div class="receipt-header mb-4">
                <div class="receipt-icon">
                  <i class="bi bi-receipt"></i>
                </div>
                <div>
                  <p class="receipt-sub">Phục vụ bàn ăn</p>
                  <h5 class="receipt-title">Thực đơn đã đặt</h5>
                </div>
              </div>

              <!-- Dish List -->
              <div class="dish-list mb-3">
                <div
                  v-for="(item, idx) in pagedItems"
                  :key="idx"
                  class="dish-item d-flex align-items-center py-2 gap-2"
                >
                  <!-- Ảnh -->
                  <img
                    :src="getImageUrl(item.hinhAnh)"
                    class="food-img flex-shrink-0"
                  />

                  <!-- Tên + Danh mục -->
                  <div class="flex-grow-1 text-start">
                    <div
                      class="dish-title fw-bold text-dark"
                      style="font-size: 0.9rem"
                    >
                      {{ item.name }}
                    </div>
                    <span class="dish-category-badge">{{
                      item.tenDanhMuc
                    }}</span>
                  </div>

                  <!-- Số lượng -->
                  <div
                    class="dish-qty text-center text-muted fw-semibold"
                    style="min-width: 40px; font-size: 0.85rem"
                  >
                    x{{ item.qty }}
                  </div>

                  <!-- Thành tiền -->
                  <div
                    class="dish-price fw-bold text-end"
                    style="font-size: 0.95rem; min-width: 80px"
                  >
                    {{
                      (
                        item.thanhTien ?? item.price * item.qty
                      ).toLocaleString()
                    }}đ
                  </div>
                </div>
              </div>
              <div v-if="dishTotalPages > 1" class="pagination mb-3">
                <button
                  :disabled="dishPage === 1"
                  @click="dishPage--"
                  class="pg-btn pg-arrow"
                >
                  <i class="bi bi-chevron-left"></i>
                </button>
                <template v-for="p in dishPageList" :key="p">
                  <button v-if="p === '…'" class="pg-btn pg-ellipsis" disabled>
                    ···
                  </button>
                  <button
                    v-else
                    class="pg-btn"
                    :class="{ active: p === dishPage }"
                    @click="dishPage = p"
                  >
                    {{ p }}
                  </button>
                </template>
                <button
                  :disabled="dishPage === dishTotalPages"
                  @click="dishPage++"
                  class="pg-btn pg-arrow"
                >
                  <i class="bi bi-chevron-right"></i>
                </button>
              </div>
              <!-- Invoice Calculation -->
              <div class="bill-calc-box mb-4">
                <!-- Tổng tiền món -->
                <div class="bill-row">
                  <span class="bill-label">Tổng cộng hóa đơn</span>
                  <span class="bill-val"
                    >{{ searchResult.totalBill.toLocaleString() }}đ</span
                  >
                </div>

                <!-- Giảm giá voucher -->
                <div
                  v-if="searchResult.discountAmount > 0"
                  class="bill-row bill-row--discount"
                >
                  <span class="bill-label">
                    Giảm giá
                    <span v-if="searchResult.voucher" class="voucher-tag">
                      <i class="bi bi-ticket-perforated-fill me-1"></i>
                      {{ searchResult.voucher.code }}
                      <span class="voucher-dot">·</span>
                      <span
                        v-if="
                          searchResult.voucher.kieuGiamGia === 1 ||
                          searchResult.voucher.kieuGiamGia === 'PHAN_TRAM'
                        "
                      >
                        {{ searchResult.voucher.giaTriGiam }}%
                      </span>
                      <span v-else>
                        {{
                          Number(
                            searchResult.voucher.giaTriGiam,
                          ).toLocaleString()
                        }}đ
                      </span>
                    </span>
                  </span>
                  <span class="bill-val bill-val--green"
                    >- {{ searchResult.discountAmount.toLocaleString() }}đ</span
                  >
                </div>

                <!-- Giảm trực tiếp -->
                <div
                  v-if="searchResult.giamTrucTiep > 0"
                  class="bill-row bill-row--discount"
                >
                  <span class="bill-label">Giảm trực tiếp</span>
                  <span class="bill-val bill-val--green"
                    >- {{ searchResult.giamTrucTiep.toLocaleString() }}đ</span
                  >
                </div>

                <!-- Điểm tích lũy sử dụng -->
                <div
                  v-if="searchResult.diemTichLuySuDung > 0"
                  class="bill-row bill-row--discount"
                >
                  <span class="bill-label">Điểm tích lũy sử dụng</span>
                  <span class="bill-val bill-val--green"
                    >-
                    {{ searchResult.diemTichLuySuDung.toLocaleString() }}đ</span
                  >
                </div>

                <!-- Tiền đã cọc -->
                <div
                  v-if="
                    searchResult.depositStatus !== 'unpaid' &&
                    searchResult.depositAmount > 0
                  "
                  class="bill-row bill-row--deposit"
                >
                  <span class="bill-label">Tiền đã cọc</span>
                  <span class="bill-val bill-val--green"
                    >- {{ searchResult.depositAmount.toLocaleString() }}đ</span
                  >
                </div>

                <!-- Tiền đã thanh toán -->
                <div
                  v-if="searchResult.tongTienDaThanhToan > 0"
                  class="bill-row"
                >
                  <span class="bill-label">Tiền đã thanh toán</span>
                  <span class="bill-val bill-val--green"
                    >{{
                      searchResult.tongTienDaThanhToan.toLocaleString()
                    }}đ</span
                  >
                </div>
                <!-- Còn lại phải thanh toán -->
                <div
                  v-if="searchResult.tienConLai > 0"
                  class="bill-row bill-row--remaining"
                >
                  <span class="bill-label">Còn lại phải thanh toán</span>
                  <span class="bill-val bill-val--red"
                    >{{ searchResult.tienConLai.toLocaleString() }}đ</span
                  >
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.lookup-page {
  background-color: #fbfbfb;
  font-family: var(--font-family, "Inter", sans-serif);
  color: #1a1a1a;
  min-height: 100vh;
}

/* Header & Titles */
.sub-title {
  text-transform: uppercase;
  letter-spacing: 6px;
  font-size: 0.7rem;
  color: #c5a059;
  font-weight: 700;
  display: block;
}

.section-title {
  font-family: "Playfair Display", serif;
  font-size: 2.5rem;
  font-weight: 700;
  color: #e65a5a;
}

.luxury-line {
  width: 40px;
  height: 2px;
  background: #c5a059;
  margin: 20px auto;
}

.section-desc {
  max-width: 650px;
  font-size: 0.95rem;
  line-height: 1.6;
}

/* Card General */
.luxury-card {
  background: #ffffff;
  border: 1px solid #f0f0f0;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.02);
  border-radius: 7px;
}

.form-section-label {
  font-family: var(--font-family, "Inter", sans-serif);
  font-weight: 700;
  letter-spacing: 1px;
}

/* Input Styles */
.search-input-group {
  position: relative;
  min-width: 250px;
}

.search-input-icon {
  position: absolute;
  left: 18px;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
  font-size: 1.2rem;
}

.luxury-input-field {
  width: 100%;
  padding: 14px 16px 14px 48px;
  font-size: 1rem;
  font-weight: 700;
  color: #111;
  letter-spacing: 1px;
  text-transform: uppercase;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  background: #f9fafb;
  transition: all 0.25s ease;
}

.luxury-input-field:focus {
  outline: none;
  background: #ffffff;
  border-color: #c5a059;
  box-shadow: 0 0 0 4px rgba(197, 160, 89, 0.1);
}

.luxury-input-field::placeholder {
  text-transform: none;
  font-weight: normal;
  letter-spacing: 0px;
  color: #b8b8b8;
}

.btn-search-luxury {
  background: linear-gradient(135deg, #ce494d 0%, #ec2229 100%);
  color: #ffffff;
  font-weight: 700;
  letter-spacing: 1.5px;
  padding: 14px 30px;
  border-radius: 7px;
  border: 1px solid #d4af37;
  transition: all 0.3s ease;
  cursor: pointer;
}

.btn-search-luxury:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(236, 34, 41, 0.3);
  border-color: #ffffff;
}

.btn-search-luxury:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-search-luxury {
  position: relative;
  overflow: hidden;
  isolation: isolate;
}

.btn-search-luxury::after {
  content: "";
  position: absolute;
  top: 0;
  left: -150%;
  width: 50%;
  height: 100%;

  background: linear-gradient(
    100deg,
    transparent 20%,
    rgba(255, 255, 255, 0.35) 50%,
    transparent 80%
  );

  transform: skewX(-20deg);
  animation: shine-sweep 2.8s ease-in-out infinite;
  pointer-events: none;
  z-index: 1;
}

.btn-search-luxury:disabled::after {
  animation-play-state: paused;
  opacity: 0.4;
}

@keyframes shine-sweep {
  0% {
    left: -150%;
  }
  50% {
    left: 150%;
  }
  100% {
    left: 150%;
  }
}

/* Example Badge Codes */
.badge-example {
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  color: #4b5563;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.badge-example:hover {
  background: #e65a5a;
  color: #ffffff;
  border-color: #e65a5a;
  transform: scale(1.05);
}

/* Status Badges */
.badge-status {
  padding: 6px 14px;
  border-radius: 100px;
  font-size: 0.8rem;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.badge-status.confirmed {
  background-color: #ecfdf5;
  color: #059669;
  border: 1px solid #a7f3d0;
}
.badge-status.pending {
  background-color: #fffbeb;
  color: #d97706;
  border: 1px solid #fde68a;
}
.badge-status.dining {
  background-color: #eff6ff;
  color: #2563eb;
  border: 1px solid #bfdbfe;
}
.badge-status.completed {
  background-color: #f0fdf4;
  color: #16a34a;
  border: 1px solid #bbf7d0;
}
.badge-status.cancelled {
  background-color: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.badge-status.paid {
  background-color: #e0f2fe;
  color: #0369a1;
  border: 1px solid #bae6fd;
}
.badge-status.unpaid {
  background-color: #fff1f2;
  color: #be123c;
  border: 1px solid #fecdd3;
}
.badge-status.fully_paid {
  background-color: #f0fdf4;
  color: #15803d;
  border: 1px solid #bbf7d0;
}

.badge-status.payment {
  background-color: #fef3c7;
  color: #b45309;
  border: 1px solid #fde68a;
}

.badge-status {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.badge-status i {
  font-size: 0.85em;
}

/* Timeline Stepper */
.stepper-wrapper {
  position: relative;
}

.step-bar {
  position: absolute;
  top: 40px;
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

.step-time {
  font-size: 0.7rem;
  color: #9ca3af;
  margin-top: 4px;
}

/* Info Cards */
.info-card {
  border-color: #f0f0f0;
}

.info-label {
  font-size: 15px;
  color: #9ca3af;
  letter-spacing: 0.5px;
  font-weight: 600;
  margin-bottom: 4px;
}

.info-value {
  font-size: 0.95rem;
  font-weight: 700;
  color: #1f2937;
}

.info-value-small {
  font-size: 0.9rem;
  font-weight: 700;
  color: #111;
}

.note-box {
  background: #fdfaf3;
  border: 1px solid #f9eed2;
  padding: 12px;
  border-radius: 8px;
  color: #854d0e;
  font-weight: 500;
  line-height: 1.5;
}

.table-name-badge {
  display: inline-block;
  background: radial-gradient(circle, #fcf8f2 0%, #f6ebd6 100%);
  border: 2px solid #c5a059;
  color: #92400e;
  font-weight: 800;
  font-size: 1.3rem;
  padding: 14px 28px;
  border-radius: 12px;
  letter-spacing: 0.5px;
}

/* Receipt Card */
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
  color: #ee4545;
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

/* Bill calculations */
.dish-item {
  gap: 12px;
  padding-left: 10px;
  padding-right: 10px;
  border-bottom: 1px dashed #f0f0f0;
}

.dish-item:last-child {
  border-bottom: none;
}
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  user-select: none;
}
.pg-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 30px;
  height: 30px;
  padding: 0 6px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #fff;
  color: #374151;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}
.pg-btn:hover:not(:disabled):not(.active) {
  border-color: #e65a5a;
  color: #e65a5a;
  background: #fff1f1;
}
.pg-btn.active {
  border: 1.5px solid #e65a5a;
  color: #e65a5a;
  font-weight: 700;
}
.pg-btn:disabled {
  border-color: #f0f0f0;
  background: #f9fafb;
  color: #d1d5db;
  cursor: not-allowed;
}
.pg-ellipsis {
  border: none;
  background: transparent;
  cursor: default;
  color: #aaa;
  min-width: 18px;
}
.pg-arrow i {
  font-size: 14px;
}

.dish-category-badge {
  display: inline-block;
  margin-top: 3px;
  padding: 3px 10px;
  font-size: 0.72rem;
  font-weight: 600;
  color: #f53232;
  background: #e9f6f7;
  border: 1px solid #d8edf1;
  border-radius: 5px;
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

.remaining-value {
  font-size: 1.4rem;
  font-weight: 800;
  color: #dc2626;
}

/* Bill Calculation Box */
.bill-calc-box {
  background: #f9fafb;
  border: 1px solid #f0f0f0;
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

/* Voucher tag */
.voucher-tag {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  background: #f4e4e4;
  border: 1px solid #fe4545;
  color: #ef2424;
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

.btn-danger-luxury {
  background: linear-gradient(135deg, #ce494d 0%, #ec2229 100%);
  color: #ffffff;
  font-weight: 700;
  font-size: 0.85rem;
  letter-spacing: 1px;
  padding: 14px;
  border-radius: 10px;
  border: 1px solid #d4af37;
  transition: all 0.25s ease;
}

.btn-danger-luxury:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(236, 34, 41, 0.2);
}

.btn-outline-dark-luxury {
  background: transparent;
  color: #1a1a1a;
  border: 1.5px solid #1a1a1a;
  font-weight: 700;
  font-size: 0.85rem;
  letter-spacing: 1px;
  padding: 12px;
  border-radius: 10px;
  transition: all 0.25s ease;
}

.btn-outline-dark-luxury:hover {
  background: #1a1a1a;
  color: #ffffff;
}

/* Watermark */
.card-watermark {
  position: absolute;
  right: -20px;
  bottom: -20px;
  font-size: 10rem;
  opacity: 0.02;
  color: #000;
  pointer-events: none;
  line-height: 1;
}

/* 2D Sliding Puzzle Blocks Loader */
.sliding-puzzle-loader {
  width: 44px;
  height: 44px;
  position: relative;
}

.sliding-puzzle-loader .puzzle-piece {
  width: 20px;
  height: 20px;
  position: absolute;
  border-radius: 4px;
  box-shadow: 0 4px 10px rgba(230, 90, 90, 0.15);
  animation-duration: 2s;
  animation-iteration-count: infinite;
  animation-timing-function: cubic-bezier(0.25, 1, 0.5, 1);
}

.sliding-puzzle-loader .piece-1 {
  top: 0;
  left: 0;
  background-color: #e65a5a;
  animation-name: slidePiece1;
}

.sliding-puzzle-loader .piece-2 {
  top: 0;
  left: 24px;
  background-color: #f87171;
  animation-name: slidePiece2;
}

.sliding-puzzle-loader .piece-3 {
  top: 24px;
  left: 0;
  background-color: #ff8a8a;
  animation-name: slidePiece3;
}

.no-results-card {
  background: #ffffff;
}
.warning-icon-wrapper {
  width: 88px;
  height: 88px;
  margin: 0 auto;

  display: flex;
  align-items: center;
  justify-content: center;

  border-radius: 50%;
  background: linear-gradient(135deg, #fff8e1, #f4c8c8);
  box-shadow: 0 8px 24px rgba(244, 58, 58, 0.25);
}

.warning-icon-wrapper i {
  font-size: 2.2rem;
  color: #ef3a3a;

  filter: drop-shadow(0 2px 4px rgba(245, 158, 11, 0.3));
}
/* Grid helper for border-end */
@media (min-width: 768px) {
  .border-end-md {
    border-right: 1px solid #e5e7eb;
  }
}

@keyframes slidePiece1 {
  0%,
  100% {
    top: 0;
    left: 0;
  }
  25% {
    top: 0;
    left: 24px;
  }
  50% {
    top: 24px;
    left: 24px;
  }
  75% {
    top: 24px;
    left: 0;
  }
}

@keyframes slidePiece2 {
  0%,
  100% {
    top: 0;
    left: 24px;
  }
  25% {
    top: 24px;
    left: 24px;
  }
  50% {
    top: 24px;
    left: 0;
  }
  75% {
    top: 0;
    left: 0;
  }
}

@keyframes slidePiece3 {
  0%,
  100% {
    top: 24px;
    left: 0;
  }
  25% {
    top: 0;
    left: 0;
  }
  50% {
    top: 0;
    left: 24px;
  }
  75% {
    top: 24px;
    left: 24px;
  }
}

/* Animations */
.reveal {
  opacity: 0;
  transform: translateY(20px);
  transition: 0.8s cubic-bezier(0.2, 1, 0.3, 1);
}

.reveal.active {
  opacity: 1;
  transform: translateY(0);
}

.badge-status.bad_debt {
  background-color: #fef2f2;
  color: #dc2626;
  border: 1px solid #f87171;
  animation: pulse-debt 1.8s ease-in-out infinite;
}

.badge-status.debt {
  background-color: #7f1d1d;
  color: #ffffff;
  border: 1px solid #7f1d1d;
  animation: pulse-debt 1.8s ease-in-out infinite;
}

@keyframes pulse-debt {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.75;
  }
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

.bill-val--red {
  color: #dc2626;
  font-weight: 700;
}

.bill-row--remaining {
  border-top: 1px dashed #e5e7eb;
  padding-top: 10px;
  margin-top: 2px;
}
</style>
