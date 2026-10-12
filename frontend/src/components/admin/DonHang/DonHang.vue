<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from "vue";
import {
  exportHoaDon,
  getAllhoadon,
  search,
} from "@/api/admin/hoadon/hoaDonApi";
import { useRouter } from "vue-router";
import { inHoaDonThanhToan } from "@/api/admin/thanhtoan/thanhToanApi";
import { useUtils } from "@/utils/helper";
import { scanQrHoaDon } from "@/api/admin/hoadon/hoaDonApi";
import jsQR from "jsqr";
const router = useRouter();
const { notify, nprogress } = useUtils();

/* ICON */
import iconEye from "@/assets/icons/eye.png";
import iconExcel from "@/assets/icons/excel.png";
import iconHoaDon from "@/assets/icons/don-hang.png";
import iconDanhSach from "@/assets/icons/danh-sach.png";

// SELECT 2
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";
import { getAllkhuvuc } from "@/api/admin/khuvuc/khuVucApi";
import { connectWebSocket, disconnectWebSocket } from "@/utils/websocket";

/* ===== FIX BUGS: KHAI BÁO CÁC BIẾN MODAL BỊ THIẾU ===== */
const isModalOpen = ref(false);
const activeTab = ref("thong-tin");
const selectedOrder = ref({});
const closeModal = () => {
  isModalOpen.value = false;
};

/* ===== QR MODAL ===== */
///////////////// QUÉT QR ///////////////
const qrCanvasRef = ref(null);
let scanRafId = null;
const isProcessingQr = ref(false);
const showQRModal = ref(false);
const qrVideoRef = ref(null);
const qrStream = ref(null);

const openQRModal = async () => {
  showQRModal.value = true;
  isProcessingQr.value = false;
  await startCamera();
  startScanLoop();
};
const closeQRModal = () => {
  showQRModal.value = false;
  stopScanLoop();
  stopCamera();
};

const startScanLoop = () => {
  const tick = () => {
    if (!showQRModal.value) return;

    const video = qrVideoRef.value;
    const canvas = qrCanvasRef.value;

    if (video && canvas && video.readyState === video.HAVE_ENOUGH_DATA) {
      canvas.width = video.videoWidth;
      canvas.height = video.videoHeight;
      const ctx = canvas.getContext("2d");
      ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

      const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
      const code = jsQR(imageData.data, imageData.width, imageData.height);

      if (code && code.data && !isProcessingQr.value) {
        handleQrDetected(code.data);
        return; // dừng loop, chờ xử lý xong
      }
    }
    scanRafId = requestAnimationFrame(tick);
  };
  scanRafId = requestAnimationFrame(tick);
};

const stopScanLoop = () => {
  if (scanRafId) {
    cancelAnimationFrame(scanRafId);
    scanRafId = null;
  }
};

const handleQrDetected = async (qrData) => {
  isProcessingQr.value = true;
  try {
    const res = await scanQrHoaDon(qrData);
    const id = res?.data?.id;

    if (!id) {
      notify.error("Không tìm thấy hóa đơn từ mã QR này");
      isProcessingQr.value = false;
      startScanLoop();
      return;
    }

    closeQRModal();
    router.push(`/admin/hoa-don/detail/${id}`);
  } catch (error) {
    console.error("Lỗi quét QR hóa đơn:", error);
    notify.error(
      error?.response?.data?.message ||
        "Mã QR không hợp lệ hoặc không tìm thấy hóa đơn",
    );
    isProcessingQr.value = false;
    startScanLoop();
  }
};

const startCamera = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: "environment" },
    });
    qrStream.value = stream;
    if (qrVideoRef.value) {
      qrVideoRef.value.srcObject = stream;
      qrVideoRef.value.play();
    }
  } catch (err) {
    console.error("Không thể mở camera:", err);
    notify.error(
      "Không thể truy cập camera. Vui lòng kiểm tra quyền truy cập.",
    );
  }
};

const stopCamera = () => {
  if (qrStream.value) {
    qrStream.value.getTracks().forEach((track) => track.stop());
    qrStream.value = null;
  }
  if (qrVideoRef.value) {
    qrVideoRef.value.srcObject = null;
  }
};

watch(qrVideoRef, (el) => {
  if (el && qrStream.value) {
    el.srcObject = qrStream.value;
    el.play();
  }
});

/* STATE */
const danhSachDonHang = ref([]);
const danhSachKhuVuc = ref([]);
const tatCaDonHang = ref([]);

const loadKhuVuc = async () => {
  try {
    const res = await getAllkhuvuc();
    danhSachKhuVuc.value = res.data;
  } catch (error) {
    console.log("Lỗi", error);
  }
};

/* FILTER */
const keyword = ref("");
const selectTrangThai = ref("");
const selectTenKhuVuc = ref("");
const selectloaiHoaDon = ref("");
const ngayStart = ref("");
const ngayEnd = ref("");

const filterHoaDon = async () => {
  try {
    const hasTrangThai =
      selectTrangThai.value !== "" &&
      selectTrangThai.value !== null &&
      selectTrangThai.value !== undefined;
    const hasLoaiHoaDon =
      selectloaiHoaDon.value !== "" &&
      selectloaiHoaDon.value !== null &&
      selectloaiHoaDon.value !== undefined;

    const params = {
      keyword: keyword.value || null,
      trangThai: hasTrangThai ? selectTrangThai.value : null,
      tenKhuVuc: selectTenKhuVuc.value || null,
      loaiHoaDon: hasLoaiHoaDon ? selectloaiHoaDon.value : null,
      ngayBatDau: ngayStart.value || null,
      ngayKetThuc: ngayEnd.value || null,
    };
    const isEmpty =
      !keyword.value &&
      !hasTrangThai &&
      !selectTenKhuVuc.value &&
      !hasLoaiHoaDon &&
      !ngayStart.value &&
      !ngayEnd.value;

    if (isEmpty) {
      await loadHoaDon();
      return;
    }

    const [res, resForCount] = await Promise.all([
      search(params),
      search({ ...params, trangThai: null }),
    ]);

    const dataForCount =
      resForCount?.data?.data ||
      resForCount?.data?.content ||
      resForCount?.data ||
      [];
    tatCaDonHang.value = dataForCount;

    const data = res?.data?.data || res?.data?.content || res?.data || [];
    danhSachDonHang.value = data.map((item) => {
      return {
        id: item.id,
        maHoaDon: item.maHoaDon,

        khachHang: item.tenKhachHang || "Khách lẻ",
        sdt: item.soDienThoai || "Chưa có",

        ban: item.listTenBan || [],
        tenKhuVucList: item.listTenKhuVuc || [],
        tang: item.tenKhuVuc || "—",

        loaiHoaDon: convertLoaiHoaDon(item.loaiHoaDon),

        ngayGio: formatDate(item.thoiGianTao),

        tongTien: formatMoney(item.tongTien),
        tienCoc: formatMoney(item.tienCoc),

        trangThai: convertTrangThai(item.trangThai),
        rawTrangThai: item.trangThai,

        items: [],
        lichSu: [],
      };
    });
    currentPage.value = 1;
  } catch (error) {
    console.log("Lỗi filter", error);
  }
};

/* PAGINATION */
const pageSize = ref(5);
const currentPage = ref(1);

const goToDetail = (id) => {
  router.push(`/admin/hoa-don/detail/${id}`);
};

const inHoaDon = async (idHoaDon) => {
  try {
    nprogress.start();
    const res = await inHoaDonThanhToan(idHoaDon);
    const blob = new Blob([res.data ? res.data : res], {
      type: "application/pdf",
    });
    const url = window.URL.createObjectURL(blob);
    window.open(url, "_blank");
  } catch (error) {
    console.error("Lỗi in hóa đơn:", error);
    notify.error("Không thể in hóa đơn");
  } finally {
    nprogress.done();
  }
};

const convertLoaiHoaDon = (type) => {
  const t = Number(type);
  switch (t) {
    case 1:
      return "Tại quầy";
    case 2:
      return "Trực tuyến";
    default:
      return "Không xác định";
  }
};

const getLoaiHoaDonClass = (type) => {
  if (type === "Trực tuyến") return "type-online";
  if (type === "Tại quầy") return "type-offline";
  return "type-other";
};

/* LOAD DATA */
const loadHoaDon = async () => {
  try {
    const res = await getAllhoadon();

    tatCaDonHang.value = res.data || [];

    danhSachDonHang.value = res.data.map((item) => ({
      id: item.id,
      maHoaDon: item.maHoaDon,

      khachHang: item.tenKhachHang || "Khách lẻ",
      sdt: item.soDienThoai || "Chưa có",

      ban: item.listTenBan || [],
      tenKhuVucList: item.listTenKhuVuc || [],
      tang: item.tenKhuVuc || "—",

      loaiHoaDon: convertLoaiHoaDon(item.loaiHoaDon),

      ngayGio: formatDate(item.thoiGianTao),

      tongTien: formatMoney(item.tongTien),
      tongTienDaThanhToan: formatMoney(item.tongTienDaThanhToan),
      tienCoc: formatMoney(item.tienCoc),

      trangThai: convertTrangThai(item.trangThai),
      rawTrangThai: item.trangThai,

      items: [],
      lichSu: [],
    }));
  } catch (error) {
    console.error("Lỗi load hóa đơn:", error);
  }
};

const refreshData = async () => {
  keyword.value = "";
  selectTrangThai.value = "";
  selectTenKhuVuc.value = "";
  selectloaiHoaDon.value = "";
  ngayStart.value = "";
  ngayEnd.value = "";

  currentPage.value = 1;

  await loadHoaDon();
};

/* FORMAT DATE & MONEY */
const formatDate = (date) => {
  if (!date) return "";
  return new Date(date).toLocaleString("vi-VN");
};

const formatMoney = (money) => {
  if (!money) return "0 đ";
  return money.toLocaleString("vi-VN") + " đ";
};

/* CONVERT STATUS */
const convertTrangThai = (status) => {
  switch (status) {
    case 0:
      return "Vừa tạo";
    case 1:
      return "Chờ khách cọc";
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

const getStatusClass = (status) => {
  switch (status) {
    case "Vừa tạo":
      return "status-created";
    case "Chờ khách cọc":
      return "status-wait-deposit";
    case "Đã cọc":
      return "status-deposited";
    case "Đã xác nhận":
      return "status-confirmed";
    case "Chờ xác nhận":
      return "status-waiting";
    case "Chờ hoàn cọc":
      return "status-arrived";
    case "Đang phục vụ":
      return "status-serving";
    case "Chờ thanh toán":
      return "status-wait-payment";
    case "Đã thanh toán":
      return "status-paid";
    case "Hoàn thành":
      return "status-completed";

    default:
      return "status-cancelled";
  }
};

/* COMPUTED */
const totalItems = computed(() => danhSachDonHang.value.length);
const totalPages = computed(
  () => Math.ceil(totalItems.value / pageSize.value) || 1,
);

const danhSachHienThi = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return danhSachDonHang.value.slice(start, end);
});

/* PAGINATION */
const goFirst = () => (currentPage.value = 1);
const goPrev = () => {
  if (currentPage.value > 1) currentPage.value--;
};
const goNext = () => {
  if (currentPage.value < totalPages.value) currentPage.value++;
};
const goLast = () => {
  currentPage.value = totalPages.value;
};

const handleExportHoaDon = async () => {
  try {
    const res = await exportHoaDon({
      soDienThoai: keyword.value || null,
      tenKhachHang: keyword.value || null,
      trangThai: selectTrangThai.value !== "" ? selectTrangThai.value : null,
      loaiHoaDon: selectloaiHoaDon.value !== "" ? selectloaiHoaDon.value : null,
      tenKhuVuc: selectTenKhuVuc.value || null,
      ngayBatDau: ngayStart.value || null,
      ngayKetThuc: ngayEnd.value || null,
    });

    const blob = new Blob([res.data], {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    });

    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute(
      "download",
      `hoa_don_${new Date().toISOString().slice(0, 10)}.xlsx`,
    );
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
    notify.success("Xuất excel thành công");
  } catch (e) {
    console.error("Lỗi export hóa đơn:", e);
    notify.error("Xuất Excel thất bại");
  }
};
/* FILTER STATUS */
const danhSachLoaiHoaDon = [
  { label: "Tất cả", value: "" },
  { label: "Trực tuyến", value: 2 },
  { label: "Tại quầy", value: 1 },
];

const getStatusCount = (statusValue) => {
  if (statusValue === "") return tatCaDonHang.value.length;
  return tatCaDonHang.value.filter((item) => item.trangThai === statusValue)
    .length;
};

const danhSachTrangThai = [
  { label: "Tất cả", value: "" },
  { label: "Vừa tạo", value: 0 },
  { label: "Chờ xác nhận", value: 4 },
  { label: "Đã xác nhận", value: 3 },
  { label: "Đang phục vụ", value: 6 },
  { label: "Chờ thanh toán", value: 7 },
  { label: "Đã thanh toán", value: 8 },
  { label: "Hoàn thành", value: 9 },
  { label: "Đã hủy", value: -1 },
];

const handleWsMessage = (data) => {
  filterHoaDon();
};

/* MOUNT */
onMounted(() => {
  loadKhuVuc();
  loadHoaDon();

  connectWebSocket(null, handleWsMessage);
});

onBeforeUnmount(() => {
  disconnectWebSocket(handleWsMessage);
});
</script>

<template>
  <div class="quan-ly-don-hang">
    <div class="box">
      <div class="box-header">
        <h2 class="main-title">
          <span class="icon-wrapper">
            <img :src="iconHoaDon" alt="Đặt bàn" />
          </span>
          Quản Lý Hóa Đơn
        </h2>
        <p>Quản lý danh sách hóa đơn trong hệ thống</p>
      </div>
    </div>

    <div class="filter-box" @click.stop>
      <div class="filter-item">
        <label>Tìm kiếm</label>
        <input
          placeholder="Tên, số điện thoại..."
          type="text"
          v-model="keyword"
          @input="filterHoaDon"
          class="custom-input"
        />
      </div>

      <div class="filter-item">
        <label>Khu vực</label>
        <v-select
          v-model="selectTenKhuVuc"
          :options="danhSachKhuVuc"
          label="tenKhuVuc"
          :reduce="(item) => item.tenKhuVuc"
          placeholder="Tất cả khu vực"
          @update:modelValue="filterHoaDon"
          :clearable="false"
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

      <div class="filter-item">
        <label>Loại hóa đơn</label>
        <v-select
          v-model="selectloaiHoaDon"
          :options="danhSachLoaiHoaDon"
          label="label"
          :reduce="(item) => item.value"
          placeholder="Tất cả"
          @update:modelValue="filterHoaDon"
          :clearable="false"
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

      <div class="filter-item">
        <label>Từ ngày</label>
        <div class="input-with-icon">
          <input
            type="date"
            placeholder="dd / MM / yyyy"
            v-model="ngayStart"
            class="custom-input"
            @change="filterHoaDon"
          />
        </div>
      </div>
      <div class="filter-item">
        <label>Đến ngày</label>
        <div class="input-with-icon">
          <input
            type="date"
            placeholder="dd / MM / yyyy"
            v-model="ngayEnd"
            class="custom-input"
            @change="filterHoaDon"
          />
        </div>
      </div>
    </div>

    <div class="box">
      <div class="box-header box-header-flex">
        <h3 class="table-title">
          <span class="icon-wrapper-small">
            <img :src="iconDanhSach" class="icon-title-small" />
          </span>
          Danh sách hóa đơn
        </h3>

        <div class="header-actions">
          <button class="qr-scan-btn" @click="openQRModal()">
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
              <rect x="3" y="3" width="7" height="7" />
              <rect x="14" y="3" width="7" height="7" />
              <rect x="3" y="14" width="7" height="7" />
              <path d="M14 14h3v3h-3zM17 17h3v3h-3zM14 17v3M17 14h3" />
            </svg>
            <span>Quét QR</span>
          </button>
          <button class="excel-btn" @click="handleExportHoaDon()">
            <img :src="iconExcel" alt="" />
            <span>Xuất Excel</span>
          </button>
          <button class="refresh-btn" @click="refreshData()">
            <img src="@/assets/icons/refresh.png" alt="" />

            <span>Làm mới</span>
          </button>
        </div>
      </div>

      <div class="status-filter-tabs">
        <button
          v-for="status in danhSachTrangThai"
          :key="status.value"
          class="status-tab-btn"
          :class="{ active: selectTrangThai === status.value }"
          @click="
            selectTrangThai = status.value;
            filterHoaDon();
          "
        >
          {{ status.label }}
          <span class="status-count-badge">
            {{ getStatusCount(status.value) }}
          </span>
        </button>
      </div>

      <div class="table-content">
        <table class="table-main">
          <thead>
            <tr>
              <th>STT</th>
              <th style="text-align: left">Khách hàng</th>
              <th>Điện thoại</th>
              <th>Vị trí</th>
              <th>Loại hóa đơn</th>
              <th>Ngày giờ</th>
              <th style="text-align: right">Tổng tiền</th>
              <th style="text-align: right">Tiền cọc</th>
              <th>Trạng thái</th>
              <th style="text-align: center">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(order, index) in danhSachHienThi" :key="order.id">
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
              <td class="text-bold" style="text-align: left">
                {{ order.khachHang }}
              </td>
              <td class="text-bold">{{ order.sdt }}</td>
              <td>
                <div class="cell-table-wrap">
                  <div class="badge-group column">
                    <div
                      v-for="(ban, i) in order.ban"
                      :key="i"
                      class="table-tag"
                    >
                      B{{ ban }} - {{ order.tenKhuVucList[i] ?? order.tang }}
                    </div>
                    <div v-if="!order.ban.length" class="text-placeholder">
                      Chưa có bàn
                    </div>
                  </div>
                </div>
              </td>
              <td>
                <span
                  :class="[
                    'booking-type',
                    getLoaiHoaDonClass(order.loaiHoaDon),
                  ]"
                >
                  {{ order.loaiHoaDon }}
                </span>
              </td>
              <td class="text-bold">{{ order.ngayGio }}</td>
              <td class="text-bold">
                {{
                  [8, 9].includes(order.rawTrangThai)
                    ? order.tongTienDaThanhToan
                    : order.tongTien
                }}
              </td>
              <td class="text-bold">{{ order.tienCoc }}</td>
              <td>
                <span
                  class="status-text"
                  :class="getStatusClass(order.trangThai)"
                >
                  {{ order.trangThai }}
                </span>
              </td>
              <td class="text-center">
                <button
                  class="btn-action-icon"
                  @click="goToDetail(order.id)"
                  v-tooltip="'Xem chi tiết'"
                >
                  <img
                    :src="iconEye"
                    class="img-icon-action"
                    title="Xem chi tiết"
                  />
                </button>

                <button
                  class="btn-action-icon"
                  @click="inHoaDon(order.id)"
                  v-tooltip="'In hóa đơn'"
                >
                  <i
                    class="bi bi-printer img-icon-action"
                    title="Xem chi tiết"
                  ></i>
                </button>
              </td>
            </tr>
            <tr v-if="danhSachHienThi.length === 0">
              <td colspan="10" class="empty-table">
                🔍 Không tìm thấy hóa đơn phù hợp
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- PAGINATION -->
      <div class="pagination-footer">
        <div class="pagination-left">
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
                @click="goFirst"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
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
                @click="goPrev"
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
                @click="goNext"
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
                >
                  <path d="m9 18 6-6-6-6" />
                </svg>
              </button>
            </li>
            <li>
              <button
                class="nav-action-btn"
                :disabled="currentPage === totalPages"
                @click="goLast"
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

        <div class="pagination-right">
          Hiển thị {{ danhSachHienThi.length }} / {{ totalItems }} hóa đơn
        </div>
      </div>
    </div>
  </div>

  <!-- QR SCAN MODAL -->
  <div v-if="showQRModal" class="qr-modal-overlay" @click.self="closeQRModal">
    <div class="qr-modal">
      <!-- Header -->
      <div class="qr-modal-header">
        <div class="qr-modal-title">
          <div class="qr-modal-icon">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="18"
              height="18"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <rect x="3" y="3" width="7" height="7" />
              <rect x="14" y="3" width="7" height="7" />
              <rect x="3" y="14" width="7" height="7" />
              <path d="M14 14h3v3h-3zM17 17h3v3h-3zM14 17v3M17 14h3" />
            </svg>
          </div>
          Đưa QR hóa đơn vào
        </div>
        <button class="qr-close-btn" @click="closeQRModal">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="18"
            height="18"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <line x1="18" y1="6" x2="6" y2="18" />
            <line x1="6" y1="6" x2="18" y2="18" />
          </svg>
        </button>
      </div>

      <!-- Subtitle -->
      <p class="qr-subtitle">Đặt mã QR hóa đơn vào khung để quét tự động</p>

      <!-- Camera viewport -->
      <div class="qr-viewport">
        <video
          ref="qrVideoRef"
          class="qr-video"
          autoplay
          muted
          playsinline
        ></video>
        <canvas ref="qrCanvasRef" style="display: none"></canvas>
        <div class="qr-corner qr-corner--tl"></div>
        <div class="qr-corner qr-corner--tr"></div>
        <div class="qr-corner qr-corner--bl"></div>
        <div class="qr-corner qr-corner--br"></div>
        <div class="qr-scan-line"></div>
      </div>

      <!-- Tip -->
      <div class="qr-tip">
        <div class="qr-tip-dot"></div>
        <span>Giữ mã QR ổn định trong khung hình để quét nhanh hơn</span>
      </div>
    </div>
  </div>

  <div v-if="isModalOpen" class="modal-overlay" @click.self="closeModal">
    <div class="modal-content">
      <div class="modal-header">
        <h3>
          Chi tiết hóa đơn:
          <span class="primary-text">#{{ selectedOrder.id }}</span>
        </h3>
        <button class="close-btn" @click="closeModal">&times;</button>
      </div>
      <div class="detail-tabs">
        <button
          class="tab-item"
          :class="{ active: activeTab === 'thong-tin' }"
          @click="activeTab = 'thong-tin'"
        >
          Thông tin
        </button>
        <button
          class="tab-item"
          :class="{ active: activeTab === 'lich-su' }"
          @click="activeTab = 'lich-su'"
        >
          Lịch sử thanh toán
        </button>
      </div>
      <div class="detail-grid">
        <div class="detail-info-column">
          <div class="info-row">
            <span class="info-label">Mã hóa đơn:</span>
            <span class="info-value text-bold">{{ selectedOrder.id }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">Thời gian:</span>
            <span class="info-value">{{ selectedOrder.ngayGio }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">Khách hàng:</span>
            <span class="info-value text-bold">{{
              selectedOrder.khachHang
            }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">Bàn:</span>
            <span class="info-value">{{ selectedOrder.ban }}</span>
          </div>
          <div class="info-row border-none">
            <span class="info-label">Tầng:</span>
            <span class="info-value">{{ selectedOrder.tang }}</span>
          </div>
        </div>
        <div class="detail-action-column">
          <div class="payment-summary">
            <div class="info-row">
              <span class="info-label">Tổng tiền:</span>
              <span class="info-value text-bold">{{
                selectedOrder.tongTien
              }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">Khách đã trả:</span>
              <span class="info-value">0 đ</span>
            </div>
            <div class="info-row border-none">
              <span class="info-label">Trạng thái:</span>
              <span class="text-bold status-pending-text">{{
                selectedOrder.trangThai
              }}</span>
            </div>
          </div>
          <div class="action-list mt-3">
            <button class="btn-v btn-primary">Xác nhận</button>
            <button class="btn-v btn-gray">In hóa đơn</button>
            <button class="btn-v btn-red" @click="closeModal">Đóng</button>
          </div>
        </div>
      </div>
    </div>
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
.icon-wrapper {
  width: 56px;
  height: 56px;

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

.icon-wrapper img {
  width: 28px;
  height: 28px;
  object-fit: contain;
}
.box-header p {
  margin: 10px 0 0;
  color: var(--color-text-muted);
}

.icon-title {
  width: 24px;
  height: 24px;
}
.table-title {
  display: flex;
  align-items: center;
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
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
}
.custom-input:hover {
  border-color: #c8d0dc;
}
.custom-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12);
}

/* ===== HEADER ACTIONS ĐỒNG BỘ ===== */
.header-actions {
  display: flex;
  gap: 10px;
}
.excel-btn,
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
.excel-btn {
  background: #d4f0dd;
  color: #16a34a;
}
.input-with-icon {
  position: relative;
  width: 100%;
}
.excel-btn:hover {
  background: #dcfce7;
  transform: translateY(-1px);
}
.refresh-btn {
  background: #f3f4f6;
  color: #374151;
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
  font-size: 14px;
  border-bottom: none;
}
.table-main td {
  border-bottom: 1px solid #f0f0f0;
  font-size: 15px;
}

/* Cố định cột */
.table-main th:nth-child(1),
.table-main td:nth-child(1) {
  width: 50px;
  text-align: center;
}
.table-main th:nth-child(2),
.table-main td:nth-child(2) {
  width: 140px;
  text-align: left;
}
.table-main th:nth-child(3),
.table-main td:nth-child(3) {
  width: 100px;
  text-align: center;
}
.table-main th:nth-child(4),
.table-main td:nth-child(4) {
  width: 130px;
  text-align: center;
}
.table-main th:nth-child(5),
.table-main td:nth-child(5) {
  width: 100px;
  text-align: center;
}
.table-main th:nth-child(6),
.table-main td:nth-child(6) {
  width: 140px;
  text-align: center;
}
.table-main th:nth-child(7),
.table-main td:nth-child(7) {
  width: 100px;
  text-align: right;
}
.table-main th:nth-child(8),
.table-main td:nth-child(8) {
  width: 100px;
  text-align: right;
}
.table-main th:nth-child(9),
.table-main td:nth-child(9) {
  width: 155px;
  text-align: center;
}
.table-main th:nth-child(10),
.table-main td:nth-child(10) {
  width: 90px;
  text-align: center;
}

/* STATUS BADGE */
.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 700;
  color: #fff;
  display: inline-block;
}
.status-active {
  background: var(--color-active, #28a745);
}
.status-pending {
  background: var(--color-pending, #ffc107);
  color: #000;
}

/* MODAL & DETAIL GRID */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(2px);
}
.modal-content {
  background: white;
  width: 95%;
  max-width: 1200px;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  animation: fadeIn 0.3s ease;
}
.modal-header {
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}
.primary-text {
  color: var(--color-primary, #ffb6b6);
}

.detail-tabs {
  display: flex;
  background: #fafafa;
  border-bottom: 1px solid #eee;
}
.tab-item {
  padding: 12px 25px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 13px;
  color: var(--color-text-muted);
}
.tab-item.active {
  color: var(--color-text, #000);
  font-weight: 700;
  border-bottom: 3px solid var(--color-primary, #ffb6b6);
}

.detail-grid {
  display: grid;
  grid-template-columns: 240px 1fr 220px;
  gap: 20px;
  padding: 20px;
}
.detail-info-column {
  border-right: 1px solid #f0f0f0;
  padding-right: 15px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px dashed #eee;
  font-size: 13px;
}
.border-none {
  border: none !important;
}
.status-pending-text {
  color: #ff4d4f;
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.btn-v {
  border: none;
  padding: 10px;
  border-radius: var(--radius-sm, 4px);
  color: #fff;
  font-weight: 700;
  cursor: pointer;
}
.btn-primary {
  background: var(--color-primary, #ffb6b6);
  color: #000;
}
.btn-gray {
  background: #eee;
  color: #333;
}
.btn-red {
  background: var(--color-inactive, #ff4d4f);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-15px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  gap: 10px;
}

.page-size {
  height: 32px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  padding: 0 8px;
  background: #fff;
  font-size: 14px;
}

/* ===== PAGINATION WRAPPER ===== */
.pagination {
  background: #f8f4f5;
  border-radius: 10px;
  padding: 6px;
  gap: 4px;
}

/* ===== PAGE ITEM ===== */
.page-item .page-link {
  min-width: 30px;
  height: 30px;
  padding: 0;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  cursor: pointer;
}

/* ===== ICON ===== */
.page-link img {
  width: 16px;
  height: 16px;
}

/* ===== ACTIVE PAGE ===== */
.page-item.active .page-link {
  background: #faf5f5;
  color: #d01414;
  font-weight: 600;
  box-shadow: 0 4px 10px rgba(59, 108, 255, 0.35);
}

/* ===== HOVER ===== */
.page-item:not(.active) .page-link:hover {
  background: #d24431;
}

/* ===== SELECT 2 && TIM KIEM ===== */
/* V-SELECT STYLE */
.modern-select {
  width: 100%;
}

.modern-select .vs__dropdown-toggle {
  height: 36px;
  border-radius: 8px;
  border: 1px solid #f3b6b6;
  padding: 0 8px;
  display: flex;
  align-items: center;
}

.modern-select .vs__selected {
  font-size: 14px;
}

.modern-select .vs__search {
  font-size: 14px;
}

.modern-select .vs__actions {
  padding-right: 6px;
}

.modern-select .vs__dropdown-menu {
  border-radius: 8px;
  border: 1px solid #eee;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.1);
}
:deep(.v-select) {
  background: #fff;
  width: 100%;
}

:deep(.vs__dropdown-toggle) {
  border: 1px solid #e5e7eb !important;
  border-radius: 10px !important;
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

/* Xử lý text đã chọn */
:deep(.vs__selected) {
  position: absolute;
  margin: 0 !important;
  padding-left: 12px !important;
  color: #374151;
  border: none !important;
}

/* Placeholder */
:deep(.vs__search::placeholder) {
  color: #9ca3af !important;
}

:deep(.vs__dropdown-toggle:hover) {
  border-color: #c8d0dc !important;
}

:deep(.vs--open .vs__dropdown-toggle) {
  border-color: var(--color-primary) !important;
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12) !important;
}

/* Menu đổ xuống */
:deep(.vs__dropdown-menu) {
  border: 1px solid #e5e7eb !important;
  border-radius: 10px !important;
  margin-top: 8px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1) !important;
  padding: 6px !important;
  z-index: 1000;
}

/* Các Option bên trong */
:deep(.vs__dropdown-option) {
  border-radius: 8px !important;
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

/* Icon mũi tên và nút xóa */
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

.disabled {
  opacity: 0.4;
  pointer-events: none;
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
.text-danger {
  color: #d01414;
}
.text-primary {
  color: #00d1f3;
}

/* Tag Bàn */
.cell-table-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
}
.badge-group {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.badge-group.column {
  flex-direction: column;
  align-items: center;
}
.table-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  background: #eff6ff;
  color: #fd0606;
  border-radius: 4px;
  font-size: 12.5px;
  font-weight: 500;
}
.text-placeholder {
  color: #94a3b8;
  font-size: 13px;
  font-style: italic;
}

/* Loại hóa đơn */
.booking-type {
  width: 90px;
  padding: 4px 0;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block;
  text-align: center;
}
.type-other {
  color: #ff9800;
  border: 1px solid rgba(255, 152, 0, 0.25);
  background: rgba(255, 152, 0, 0.08);
}
.type-online {
  color: #0d6efd;
  border: 1px solid rgba(13, 110, 253, 0.25);
  background: rgba(13, 110, 253, 0.08);
}
.type-offline {
  color: #6c757d;
  border: 1px solid rgba(108, 117, 125, 0.25);
  background: rgba(108, 117, 125, 0.08);
}

/* BỘ LỌC TRẠNG THÁI OUTLINE PILLS */
.status-filter-tabs {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 20px;
  overflow-x: auto;
  scrollbar-width: none;
  padding: 4px 0;
}
.status-filter-tabs::-webkit-scrollbar {
  display: none;
}

.status-tab-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding: 8px 16px;
  font-size: 13.5px;
  font-weight: 600;
  color: #64748b;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s ease;
}
.status-tab-btn:hover {
  color: #1e293b;
  border-color: #cbd5e1;
  background: #f8fafc;
}
.status-tab-btn.active {
  color: #d01414;
  border-color: #d01414;
  background: #fdf2f2;
  font-weight: 700;
}

.status-count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  font-size: 11px;
  font-weight: 700;
  line-height: 1;
  border-radius: 999px;
  margin-left: 6px;
  background: #f1f5f9;
  color: #64748b;
  transition: all 0.2s ease;
}
.status-tab-btn:hover .status-count-badge {
  background: #cbd5e1;
  color: #0f172a;
}
.status-tab-btn.active .status-count-badge {
  background: #d01414;
  color: #ffffff;
}

/* Trạng thái */
.status-text {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 135px;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}
.status-created {
  background: rgba(107, 114, 128, 0.12);
  color: #6b7280;
}
.status-wait-deposit {
  background: rgba(245, 158, 11, 0.12);
  color: #f59e0b;
}
.status-deposited {
  background: rgba(34, 197, 94, 0.12);
  color: #22c55e;
}
.status-confirmed {
  background: rgba(59, 130, 246, 0.12);
  color: #3b82f6;
}
.status-waiting {
  background: rgba(251, 146, 60, 0.12);
  color: #fb923c;
}
.status-arrived {
  background: rgba(14, 165, 233, 0.12);
  color: #0ea5e9;
}
.status-serving {
  background: rgba(139, 92, 246, 0.12);
  color: #8b5cf6;
}
.status-wait-payment {
  background: rgba(234, 88, 12, 0.12);
  color: #ea580c;
}
.status-paid {
  background: rgba(16, 185, 129, 0.12);
  color: #10b981;
}
.status-completed {
  background: rgba(5, 150, 105, 0.12);
  color: #059669;
}
.status-cancelled {
  background: rgba(239, 68, 68, 0.12);
  color: #ef4444;
}

/* ACTION ICONS TRONG TABLE */
.action-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.btn-action-icon {
  width: 28px;
  height: 28px;
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

/* FIX NHỎ ICON NHƯ ĐÃ CHỐT */
.img-icon-action {
  width: 14px !important;
  height: 14px !important;
  object-fit: contain;
}
.excel-btn img,
.refresh-btn img {
  width: 14px !important;
  height: 14px !important;
  object-fit: contain;
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
:deep(.vs__dropdown-toggle:hover) {
  border-color: #c8d0dc !important;
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
:deep(.vs__search::placeholder) {
  color: #9ca3af !important;
}
:deep(.vs--open .vs__dropdown-toggle) {
  border-color: var(--color-primary) !important;
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12) !important;
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
:deep(.vs__actions) {
  padding-right: 10px !important;
}
:deep(.vs__open-indicator),
:deep(.vs__clear) {
  fill: #9ca3af !important;
}

/* ===== MODAL DỰ PHÒNG ===== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.modal-content {
  background: white;
  width: 900px;
  max-width: 95%;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  animation: fadeIn 0.3s ease;
}
.modal-header {
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
}
.primary-text {
  color: #d73737;
}
.detail-tabs {
  display: flex;
  background: #fafafa;
  border-bottom: 1px solid #eee;
}
.tab-item {
  padding: 12px 25px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 13.5px;
  color: #666;
}
.tab-item.active {
  color: #1a1a1a;
  font-weight: 700;
  border-bottom: 3px solid #d73737;
}
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
  padding: 20px;
}
.detail-info-column {
  border-right: 1px solid #f0f0f0;
  padding-right: 15px;
}
.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px dashed #eee;
  font-size: 14px;
}
.border-none {
  border: none !important;
}
.status-pending-text {
  color: #f59e0b;
}
.action-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.btn-v {
  border: none;
  padding: 10px;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  font-size: 13.5px;
}
.btn-primary {
  background: #d73737;
  color: #fff;
}
.btn-gray {
  background: #e5e7eb;
  color: #374151;
}
.btn-red {
  background: #fee2e2;
  color: #ef4444;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-15px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ///////////////////////// PHÂN TRANG ///////////// */
.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 12px;
}
.pagination-footer {
  display: grid;
  grid-template-columns: 220px 1fr 220px;
  align-items: center;
  margin-top: 16px;
}

.pagination-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin: 0;
  padding: 0;
}

.pagination-right {
  text-align: right;
  color: #6b7280;
  font-size: 14px;
}

.pagination-premium {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  padding: 6px;
  border-radius: 10px;
  border: 1px solid rgba(241, 245, 249, 0.8);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02),
    0 10px 15px -3px rgba(15, 23, 42, 0.05);
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
  outline: none;
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

.page-indicator-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  height: 36px;
  padding: 0 16px;
  background: #f3efef;
  border-radius: 7px;
  color: #ffffff;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  box-shadow: 0 4px 12px rgba(174, 193, 237, 0.15);
  user-select: none;
}

.current-number {
  font-weight: 600;
  font-size: 0.875rem;
  color: #f31e1e;
}

.divider {
  font-size: 0.75rem;
  color: #e00808;
  margin: 0 2px;
}

.total-number {
  font-weight: 500;
  font-size: 0.875rem;
  color: #94a3b8;
}

.nav-action-btn:disabled {
  opacity: 0.25;
  cursor: not-allowed;
  color: #94a3b8;
}
.nav-action-btn svg {
  transition: transform 0.2s ease;
}

.nav-action-btn:hover:not(:disabled) svg {
  transform: stroke-width(2.5);
}

/* //////////// NÚT HÀNH ĐỘNG //////////// */

.excel-btn {
  display: flex;
  align-items: center;
  gap: 10px;

  padding: 12px 18px;

  border: none;
  border-radius: 7px;

  background: #d4f0dd;
  color: #16a34a;

  font-size: 14px;
  font-weight: 600;

  cursor: pointer;
  transition: all 0.25s ease;
}

.excel-btn img {
  width: 18px;
  height: 18px;
  object-fit: contain;
}

.excel-btn:hover {
  background: #dcfce7;
  transform: translateY(-1px);
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8px;

  padding: 9px 14px;

  border: none;
  border-radius: 12px;

  background: #f3f4f6;
  color: #cb1d1d;

  font-size: 13px;
  font-weight: 600;

  cursor: pointer;
  transition: all 0.25s ease;
}

.refresh-btn img {
  width: 16px;
  height: 16px;
  object-fit: contain;
}

.refresh-btn:hover {
  background: #e5e7eb;
  transform: translateY(-1px);
}

.refresh-btn:hover img {
  transform: rotate(180deg);
  transition: transform 0.4s ease;
}
.add-btn,
.excel-btn,
.refresh-btn {
  padding: 9px 14px;
  gap: 8px;
  font-size: 13px;
  border-radius: 7px;
}

.add-btn img,
.excel-btn img,
.refresh-btn img {
  width: 16px;
  height: 16px;
}

/* //////////// ICON THAO TÁC //////////// */

.btn-action-icon {
  background: #f2f5f7;
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  vertical-align: middle;
  border: none;
  border-radius: 50px;

  cursor: pointer;
  transition: all 0.2s ease;
}

.img-icon-action {
  width: 17px;
  height: 17px;
  display: block;
}

.printer-icon {
  font-size: 17px;
  line-height: 1;
  display: block;
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
.text-center {
  text-align: center;
}

/* ===== QR SCAN BUTTON ===== */
.qr-scan-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 9px 14px;
  font-size: 13px;
  border-radius: 7px;
  border: none;
  cursor: pointer;
  font-weight: 600;
  background: #fce8e8;
  color: #c0392b;
  transition: all 0.25s ease;
}
.qr-scan-btn:hover {
  background: #fad5d5;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(192, 57, 43, 0.18);
}
.qr-scan-btn svg {
  flex-shrink: 0;
}

/* ===== QR MODAL OVERLAY ===== */
.qr-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(2px);
  animation: fadeIn 0.2s ease;
}

.qr-modal {
  background: #fff;
  border-radius: 12px;
  padding: 24px 24px 20px;
  width: 380px;
  max-width: 95vw;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  animation: slideUp 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}
@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.qr-modal-header {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}
.qr-modal-title {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #1a1a1a;
  font-size: 16px;
  font-weight: 700;
}
.qr-modal-icon {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  background: #fce8e8;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0392b;
  flex-shrink: 0;
}
.qr-close-btn {
  background: #f3f4f6;
  border: none;
  cursor: pointer;
  color: #6b7280;
  border-radius: 8px;
  padding: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.qr-close-btn:hover {
  background: #e5e7eb;
  color: #111;
}

.qr-subtitle {
  color: #6b7280;
  font-size: 13px;
  text-align: center;
  width: 100%;
  margin-top: -4px;
}

.qr-viewport {
  position: relative;
  width: 280px;
  height: 280px;
  border-radius: 12px;
  overflow: hidden;
  background: #000;
  box-shadow: 0 0 0 3px rgba(192, 57, 43, 0.25), 0 4px 20px rgba(0, 0, 0, 0.15);
}
.qr-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

/* corner brackets */
.qr-corner {
  position: absolute;
  width: 36px;
  height: 36px;
  pointer-events: none;
}
.qr-corner--tl {
  top: 10px;
  left: 10px;
  border-top: 3px solid #e65a5a;
  border-left: 3px solid #e65a5a;
  border-radius: 4px 0 0 0;
}
.qr-corner--tr {
  top: 10px;
  right: 10px;
  border-top: 3px solid #e65a5a;
  border-right: 3px solid #e65a5a;
  border-radius: 0 4px 0 0;
}
.qr-corner--bl {
  bottom: 10px;
  left: 10px;
  border-bottom: 3px solid #e65a5a;
  border-left: 3px solid #e65a5a;
  border-radius: 0 0 0 4px;
}
.qr-corner--br {
  bottom: 10px;
  right: 10px;
  border-bottom: 3px solid #e65a5a;
  border-right: 3px solid #e65a5a;
  border-radius: 0 0 4px 0;
}

/* scan line */
.qr-scan-line {
  position: absolute;
  left: 10px;
  right: 10px;
  height: 2px;
  background: linear-gradient(
    90deg,
    transparent,
    #e65a5a,
    #ff8a80,
    #e65a5a,
    transparent
  );
  border-radius: 2px;
  box-shadow: 0 0 10px rgba(230, 90, 90, 0.8);
  animation: scanLine 2s ease-in-out infinite;
}
@keyframes scanLine {
  0% {
    top: 20px;
    opacity: 1;
  }
  50% {
    top: 248px;
    opacity: 1;
  }
  100% {
    top: 20px;
    opacity: 1;
  }
}

.qr-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6b7280;
  font-size: 12.5px;
  background: #fdf2f2;
  border: 1px solid #fad5d5;
  border-radius: 8px;
  padding: 10px 14px;
  width: 100%;
}
.qr-tip-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #e65a5a;
  flex-shrink: 0;
  animation: pulse 1.5s ease-in-out infinite;
}
@keyframes pulse {
  0%,
  100% {
    box-shadow: 0 0 0 0 rgba(230, 90, 90, 0.6);
  }
  50% {
    box-shadow: 0 0 0 6px rgba(230, 90, 90, 0);
  }
}
</style>
