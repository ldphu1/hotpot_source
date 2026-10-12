<script setup>
import { ref, onMounted, computed, onUnmounted, onBeforeUnmount } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUtils } from "@/utils/helper";
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";

//////////////// API /////////////////////
import { datBanDetail } from "@/api/admin/datban/datBanApi";
import { huyDatBan } from "@/api/admin/datban/datBanApi";
import { previewTienCoc } from "@/api/admin/datban/datBanApi";
import { xacNhanHoanCoc } from "@/api/admin/datban/datBanApi";
import { xacNhanDatCocVaXacNhan } from "@/api/admin/datban/datBanApi";
import { getAllThamSoDong } from "@/api/admin/thamsodong/thamSoDongApi";
import { xepLaiBan } from "@/api/admin/datban/datBanApi";
import {
  getAllPhuongThucThanhToan,
  getQrChuyenKhoan,
} from "@/api/admin/thanhtoan/thanhToanApi";
import XepLaiBanModal from "./XepLaiBanModal.vue";
/* icons & assets */
import iconBack from "@/assets/icons/prev.png";
import ZaloPay from "@/assets/icons/ZaloPay.png";
import VNPAY from "@/assets/icons/VNPay.png";
import { connectWebSocket, disconnectWebSocket } from "@/utils/websocket";
const route = useRoute();
const router = useRouter();
const { notify, nprogress } = useUtils();
const phanTramCoc = ref(0);
const minKhachDoanLon = ref(0);
const tienCocMoiKhach = ref(0);
const idDatBan = route.params.id;
const isLoading = ref(false);
const lichSu = ref([]);
const showModalChonBan = ref(false);
// Tiền cọc
const showDepositModal = ref(false);
const depositInput = ref(0);
const trangThaiCoc = ref("CHUA_COC");
const hinhThucCoc = ref("");
const trangThaiDatBan = ref("");
const tienCoc = ref(0);
const idHoaDon = ref(null);
////////////////////////// CHỌN LẠI BÀN ///////////////////
const viewMonDaDat = ref({
  id: idDatBan || "---",
  maDatBan: "",
  tenKhachHang: "",
  soDienThoai: "",
  soNguoiToiDa: 0,
  thoiGianDat: "",
  hinhThucDat: "",
  trangThai: "",
  listMonAn: [],
  listBan: [],
  listIdBan: [],
  listTenBan: [],
});

const viewHoaDon = ref(null);

const bookingDataForTable = computed(() => {
  const ht = viewMonDaDat.value.hinhThucDat;
  let hinhThucVal = 2;
  if (ht === "Online" || ht === "Trực tuyến" || ht === 1) {
    hinhThucVal = 1;
  }
  return {
    tenKhachHang: viewMonDaDat.value.khachHang,
    soDienThoai: viewMonDaDat.value.sdt,
    soNguoi: viewMonDaDat.value.soNguoi,
    gioDen: viewMonDaDat.value.thoiGianDat,
    hinhThucDat: hinhThucVal,

    dsBan: viewMonDaDat.value.listIdBan,
    dsTenBan: viewMonDaDat.value.listTenBan,
  };
});

const openPickTable = () => {
  showModalChonBan.value = true;
};

const handleSuccessXepBan = async (data) => {
  try {
    await xepLaiBan({
      idDatBan: idDatBan,
      listIdBan: data.listIdBan,
    });

    notify.success("Xếp lại bàn thành công");
    showModalChonBan.value = false;

    await loadDetail();
  } catch (error) {
    notify.error(error.response?.data?.message || "Xếp bàn thất bại");
  }
};

///////////////////////////////////////////////

const danhSachHinhThucCoc = [
  { label: "Tiền mặt", value: "TIEN_MAT" },
  { label: "Chuyển khoản", value: "CHUYEN_KHOAN" },
];
const danhSachPhuongThuc = ref([]);
const idPhuongThucThanhToan = ref(null);
const hienModalQr = ref(false);
const qrCodeUrl = ref("");
const dangTaoQr = ref(false);
const errorPhuongThuc = ref("");

const openDepositModal = () => {
  depositInput.value = tienPhaiCoc.value;
  errorPhuongThuc.value = "";
  showDepositModal.value = true;
};

async function loadPhuongThuc() {
  try {
    const res = await getAllPhuongThucThanhToan();
    danhSachPhuongThuc.value = Array.isArray(res.data) ? res.data : [];

    const tienMat = danhSachPhuongThuc.value.find((p) => {
      const ten = String(p.tenPhuongThuc || "")
        .trim()
        .toLowerCase();
      return ten === "tiền mặt" || ten === "tien mat";
    });
    if (tienMat) idPhuongThucThanhToan.value = tienMat.id;
  } catch (e) {
    console.error("Lỗi load phương thức thanh toán:", e);
  }
}

const depositDisplay = computed({
  get() {
    if (!depositInput.value) return "";
    return depositInput.value.toLocaleString("vi-VN");
  },
  set(val) {
    const number = val.replace(/\D/g, "");
    depositInput.value = Number(number);
  },
});
//////////////////// XÁC NHẬN KHÔNG CỌC ///////////////
const xacNhanKhongCoc = async () => {
  const ok = await notify.confirm("Xác nhận đặt bàn?");
  if (!ok) return;
  if (
    !viewMonDaDat.value.listIdBan ||
    viewMonDaDat.value.listIdBan.length === 0
  ) {
    notify.error("Vui lòng chọn bàn trước khi xác nhận");
    return;
  }

  await callApiConfirm({
    tienCoc: null,
    hinhThucCoc: null,
  });
};

const callApiConfirm = async (payload) => {
  isLoading.value = true;
  nprogress.start();

  const startTime = Date.now();

  try {
    await xacNhanDatCocVaXacNhan(idDatBan, payload);
    notify.success("Xác nhận thành công & đã gửi mail!");
    showDepositModal.value = false;
    await loadDetail();
  } catch (error) {
    notify.error(error.response?.data?.message || "Thử lại sau");
  } finally {
    const elapsed = Date.now() - startTime;

    const minimumLoading = 3000;

    if (elapsed < minimumLoading) {
      await new Promise((resolve) =>
        setTimeout(resolve, minimumLoading - elapsed),
      );
    }

    isLoading.value = false;
    nprogress.done();
  }
};

/////////////////// XÁC NHẬN CÓ CỌC /////////////////
const xuLyXacNhanGiaoDich = async () => {
  errorPhuongThuc.value = "";
  if (depositInput.value <= 0) {
    notify.error("Vui lòng nhập số tiền cọc hợp lệ!");
    return;
  }
  if (!idPhuongThucThanhToan.value) {
    errorPhuongThuc.value = "Vui lòng chọn phương thức thanh toán!";
    return;
  }

  if (depositInput.value < tienPhaiCoc.value) {
    const ok = await notify.confirm(
      "Số tiền nhập vào ít hơn mức cọc quy định. vẫn tiếp tục?",
    );
    if (!ok) {
      return;
    }
  }

  const pt = danhSachPhuongThuc.value.find(
    (p) => Number(p.id) === Number(idPhuongThucThanhToan.value),
  );
  if (!pt) {
    notify.error("Không tìm thấy phương thức thanh toán");
    return;
  }

  const tenPt = String(pt.tenPhuongThuc || "")
    .trim()
    .toLowerCase();
  const laTienMat = tenPt.includes("tiền mặt") || tenPt.includes("tien mat");

  hinhThucCoc.value = laTienMat ? "TIEN_MAT" : "CHUYEN_KHOAN";

  if (laTienMat) {
    await xacNhanVaDatCoc();
  } else {
    dangTaoQr.value = true;
    nprogress.start();
    try {
      const res = await getQrChuyenKhoan(idHoaDon.value, depositInput.value);
      qrCodeUrl.value = res.data?.qrCodeUrl || "";
      if (qrCodeUrl.value) {
        hienModalQr.value = true;
      } else {
        notify.error("Không tạo được mã QR");
      }
    } catch (e) {
      notify.error("Tạo mã QR thất bại");
    } finally {
      dangTaoQr.value = false;
      nprogress.done();
    }
  }
};

const dongModalQr = () => {
  hienModalQr.value = false;
};

const xacNhanVaDatCoc = async () => {
  // confirm tổng
  const ok = await notify.confirm(
    "Xác nhận tiền cọc và thông tin phiếu đặt bàn ?",
  );
  if (!ok) {
    return;
  }
  isLoading.value = true;
  nprogress.start();
  const startTime = Date.now();
  try {
    await xacNhanDatCocVaXacNhan(idDatBan, {
      tienCoc: depositInput.value,
      hinhThucCoc: hinhThucCoc.value,
    });
    notify.success("Xác nhận thành công & đã gửi mail!");
    showDepositModal.value = false;
    hienModalQr.value = false;
    await loadDetail();
  } catch (error) {
    notify.error("Lỗi: " + (error.response?.data?.message || "thử lại sau"));
  } finally {
    const elapsed = Date.now() - startTime;

    const minimumLoading = 3000;

    if (elapsed < minimumLoading) {
      await new Promise((resolve) =>
        setTimeout(resolve, minimumLoading - elapsed),
      );
    }
    isLoading.value = false;
    nprogress.done();
  }
};

////////////////////// ĐẾM NGƯỢC HỦY ////////////////
const hanThanhToanCoc = ref(null);
const countdown = ref("");
let timer = null;

const startCountdown = () => {
  if (!hanThanhToanCoc.value) return;

  if (timer) clearInterval(timer);

  timer = setInterval(() => {
    const now = new Date().getTime();
    const target = new Date(hanThanhToanCoc.value).getTime();

    const diff = target - now;

    if (diff <= 0) {
      countdown.value = "Đã hết hạn - sẽ tự hủy";
      clearInterval(timer);
      return;
    }

    const minutes = Math.floor(diff / 1000 / 60);
    const seconds = Math.floor((diff / 1000) % 60);

    countdown.value = `${minutes.toString().padStart(2, "0")}:${seconds
      .toString()
      .padStart(2, "0")}`;
  }, 1000);
};

onUnmounted(() => {
  if (timer) clearInterval(timer);
});

/////////////////// HỦY ĐẶT BÀN //////////////
const modalHuyDatBan = ref(false);
const lyDoHuy = ref("");
const preview = ref(null);

const openModalHuy = async () => {
  modalHuyDatBan.value = true;
  try {
    const res = await previewTienCoc(idDatBan);
    preview.value = res.data;
  } catch (e) {
    notify.error("Không lấy được thông tin hoàn cọc");
  }
};

const handleHuyDatBan = async () => {
  if (!lyDoHuy.value.trim()) {
    notify.error("Vui lòng nhập lý do hủy");
    return;
  }
  if (!(await notify.confirm("Bạn có muốn hủy phiếu đặt bàn này?"))) return;
  isLoading.value = true;
  nprogress.start();
  const startTime = Date.now();
  try {
    await huyDatBan(idDatBan, { ghiChu: lyDoHuy.value });
    notify.success("Hủy phiếu đặt bàn thành công");
    modalHuyDatBan.value = false;
    lyDoHuy.value = "";
    await loadDetail();
  } catch (e) {
    notify.error(
      e.response?.data?.message || e.response?.data || "Có lỗi xảy ra",
    );
  } finally {
    const elapsed = Date.now() - startTime;
    const minimumLoading = 3000;
    if (elapsed < minimumLoading) {
      await new Promise((resolve) =>
        setTimeout(resolve, minimumLoading - elapsed),
      );
    }
    isLoading.value = false;
    nprogress.done();
  }
};

const handleXacNhanHoanCoc = async () => {
  if (!(await notify.confirm("Xác nhận đã hoàn tiền cọc cho khách?"))) return;
  nprogress.start();
  try {
    await xacNhanHoanCoc(idDatBan);
    notify.success("Xác nhận hoàn cọc thành công");
    await loadDetail();
  } catch (e) {
    notify.error(e.response?.data?.message || "Có lỗi xảy ra");
  } finally {
    nprogress.done();
  }
};

////////////////////////////////////

// Load tham số hệ thống
const loadThamSo = async () => {
  try {
    const res = await getAllThamSoDong();
    const config = res.data.find((item) => item.maThamSo === "PHAN_TRAM_COC");
    if (config) {
      phanTramCoc.value = parseFloat(config.giaTri);
    }
    const configMinKhach = res.data.find(
      (item) => item.maThamSo === "SO_KHACH_BAT_BUOC_COC_DOAN_LON",
    );
    if (configMinKhach) {
      minKhachDoanLon.value = parseFloat(configMinKhach.giaTri);
    }
    const configTienMoiKhach = res.data.find(
      (item) => item.maThamSo === "TIEN_COC_MOI_KHACH_DOAN_LON",
    );
    if (configTienMoiKhach) {
      tienCocMoiKhach.value = parseFloat(configTienMoiKhach.giaTri);
    }
  } catch (error) {
    console.error("lỗi lấy tham số cọc:", error);
  }
};

const tienPhaiCoc = computed(() => {
  const soKhach = Number(viewMonDaDat.value.soNguoi) || 0;
  let rawVal = 0;
  if (totalFoodPrice.value > 0) {
    rawVal += (totalFoodPrice.value * phanTramCoc.value) / 100;
  }
  if (soKhach > minKhachDoanLon.value) {
    rawVal += soKhach * tienCocMoiKhach.value;
  }
  return Math.round(rawVal * 100) / 100;
});
const totalFoodPrice = computed(() => {
  return viewMonDaDat.value.listMonAn
    .filter((mon) => mon.trangThai !== 2)
    .reduce((sum, mon) => sum + mon.gia * mon.soLuong, 0);
});

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
    case "HOAN_THANH":
      return "Hoàn thành";
    case "THANH_TOAN":
      return "Thanh toán";
    case "HOAN_TAT_THANH_TOAN":
      return "Thanh toán thành công";
    case "HUY_HOA_DON":
      return "Hủy đơn";
    case "LEN_MON":
      return "Lên món";
    case "AP_DUNG_MA_GIAM_GIA":
      return "Mã giảm giá";
    case "XAC_NHAN":
      return "Xác nhận";
    case "CHO_HOAN_COC":
      return "Chờ hoàn cọc";
    case "XEP_LAI_BAN":
      return "Xếp lại bàn";
    case "GIAM_GIA_TRUC_TIEP":
      return "Giảm trực tiếp";
    case "GHEP_CHO_NGOI":
      return "Ghép bàn";
    case "TREO_BILL":
      return "Ghi nợ";
    case "CAP_NHAT_PHIEU":
      return "Cập nhật phiếu";
    case "NO_XAU":
      return "Nợ xấu";
    case "THANH_TOAN_COC_SAU_HUY":
      return "Thanh toán cọc sau hủy";
    default:
      return action;
  }
};

const getTimelineClass = (action) => {
  if (action.includes("Tạo")) return "success";
  if (action.includes("Nhận tiền cọc")) return "primary";
  if (action.includes("Xác nhận")) return "primary";
  if (action.includes("Xếp bàn")) return "success";
  if (action.includes("Hủy phiếu đặt bàn")) return "success";
  if (action.includes("Hoàn cọc")) return "success";
  if (action.includes("Hoàn cọc thất bại")) return "success";
  if (action.includes("Gửi email")) return "success";
  if (action.includes("Thanh toán cọc")) return "success";
  if (action.includes("Đặt bàn")) return "success";
  if (action.includes("Check-in")) return "success";
  if (action.includes("Gọi món")) return "success";
  if (action.includes("Lên hết món")) return "success";
  if (action.includes("Hoàn thành")) return "success";
  if (action.includes("Thanh toán")) return "success";
  if (action.includes("Thanh toán thành công")) return "success";
  if (action.includes("Hủy đơn")) return "success";
  if (action.includes("Lên món")) return "success";
  if (action.includes("Mã giảm giá")) return "success";
  if (action.includes("Xác nhận")) return "success";
  if (action.includes("Chờ hoàn cọc")) return "success";
  if (action.includes("Xếp lại bàn")) return "success";
  if (action.includes("Giảm trực tiếp")) return "success";
  if (action.includes("Ghép bàn")) return "success";
  if (action.includes("Ghi nợ")) return "success";
  if (action.includes("Cập nhật phiếu")) return "success";
  if (action.includes("Nợ xấu")) return "success";
  if (action.includes("Thanh toán cọc sau hủy")) return "success";
  return "";
};

const statusBadgeClass = {
  0: "status-pending",
  1: "status-done",
  2: "status-cancelled",
};
const statusBadgeText = {
  0: "Chưa lên món",
  1: "Đã lên món",
  2: "Hủy món",
};
const loadDetail = async () => {
  try {
    const res = await datBanDetail(idDatBan);
    const data = res.data;
    trangThaiCoc.value = data.trangThaiCoc;
    hanThanhToanCoc.value = data.hanThanhToanCoc;
    trangThaiDatBan.value = data.trangThai;
    tienCoc.value = data.tienCoc || 0;
    startCountdown();

    lichSu.value = (data.lichSuHoaDons || []).map((log) => ({
      tenNhanVien: log.tenNhanVien,
      hanhDong: convertHanhDong(log.hanhDong),
      ghiChu: log.ghiChu,
      thoiGian: log.thoiGian,
    }));

    viewMonDaDat.value = {
      id: data.id,
      maDatBan: data.maDatBan,
      khachHang: data.tenKhachHang,
      sdt: data.soDienThoai,
      soNguoi: data.soNguoiToiDa,

      thoiGianDat: data.thoiGianDat,
      ngayGio: formatDateTime(data.thoiGianDat),
      hinhThucDat: mapHinhThuc(data.hinhThucDat),
      trangThai: mapTrangThai(data.trangThai),

      tenKhuVuc: data.tenKhuVuc,
      listIdBan: data.listIdBan,
      listTenBan: data.listTenBan,
      listMonAn: (data.chiTietHoaDons || []).map((ct) => ({
        tenMon: ct.tenSanPham,
        tenDanhMuc: ct.tenDanhMuc,
        soLuong: ct.soLuong,
        gia: ct.donGia,
        hinhAnh: ct.hinhAnh,
        trangThai: ct.trangThai,
      })),
    };
    idHoaDon.value = data.hoaDon?.id || null;
    viewHoaDon.value = data.hoaDon
      ? {
          maGiamGia: data.hoaDon.maGiamGia,
          tenGiamGia: data.hoaDon.tenGiamGia,
          giaTriGiam: data.hoaDon.giaTriGiam,
          tongTienDaThanhToan: data.hoaDon.tongTienDaThanhToan,
          diemTichLuySuDung: data.hoaDon.diemTichLuySuDung,
          giamTrucTiep: data.hoaDon.giamTrucTiep,
          soTienDuocGiam: data.hoaDon.soTienDuocGiam,
          tongTienMon: data.hoaDon.tongTienMon,
        }
      : null;
  } catch (error) {
    notify.error("không thể tải thông tin");
  }
};

const formatDateTime = (iso) => {
  if (!iso) return "";
  const d = new Date(iso);
  return `${d.getHours().toString().padStart(2, "0")}:${d
    .getMinutes()
    .toString()
    .padStart(2, "0")} - ${d.toLocaleDateString("vi-VN")}`;
};

const mapTrangThai = (stt) => {
  switch (stt) {
    case "CHO_XAC_NHAN":
      return "Chờ xác nhận";

    case "DA_XAC_NHAN":
      return "Đã xác nhận";

    case "DA_COC":
      return "Đã cọc";

    case "HUY_COC":
      return "Hủy cọc";

    case "DA_CHECKIN":
      return "Đã check-in";

    case "DANG_PHUC_VU":
      return "Đang phục vụ";

    case "HOAN_THANH":
      return "Hoàn thành";

    case "DA_HUY":
      return "Đã hủy";

    case "CHUA_COC":
      return "Chưa cọc";

    case "KHONG_CAN_COC":
      return "Không cần cọc";

    case "KHONG_DEN":
      return "Khách không đến";

    case "HET_HAN":
      return "Hết hạn giữ bàn";

    case "CHO_HOAN_COC":
      return "Chờ hoàn cọc";

    default:
      return stt || "Không xác định";
  }
};

const mapHinhThuc = (ht) => {
  return ht === 1 ? "Online" : "Tại quầy";
};

const getStatusClass = (status) => {
  switch (status) {
    case "Chờ xác nhận":
      return "stt-confirmed";
    case "Chờ hoàn cọc":
      return "stt-confirmed";
    case "Đã xác nhận":
      return "stt-confirmed";
    case "Đang phục vụ":
      return "stt-serving";
    case "Đã hủy":
      return "stt-confirmed";
    default:
      return "stt-confirmed";
  }
};

const goBack = () => router.back();

//////////// PhAN TRANG MÓN ĐÃ CHỌN /////////
const currentPageMonDaDat = ref(1);
const pageSizeMonDaDat = 5;

const pagedMonDaDat = computed(() => {
  const start = (currentPageMonDaDat.value - 1) * pageSizeMonDaDat;
  return viewMonDaDat.value.listMonAn.slice(start, start + pageSizeMonDaDat);
});

const totalPagesMonDaDat = computed(() =>
  Math.ceil((viewMonDaDat.value.listMonAn?.length || 0) / pageSizeMonDaDat),
);

const pageListMonDaDat = computed(() => {
  const total = totalPagesMonDaDat.value;
  if (total <= 5) return Array.from({ length: total }, (_, i) => i + 1);
  return [1, 2, 3, "…", total];
});

const handleWsMessage = (data) => {
  loadDetail();
};

onMounted(() => {
  loadDetail();
  loadThamSo();
  loadPhuongThuc();

  connectWebSocket(idDatBan, handleWsMessage);
});

onBeforeUnmount(() => {
  disconnectWebSocket(handleWsMessage);
});
const getImageUrl = (img) => {
  if (!img) return "https://via.placeholder.com/50";

  return `http://localhost:8080/uploads/${img}`;
};
</script>

<template>
  <div class="fsh-detail-container">
    <header class="box-header-card">
      <div class="box-header-flex">
        <div class="title-group">
          <div class="title-row">
            <div class="icon-box-wrapper">
              <i class="bi bi-calendar-check"></i>
            </div>
            <div class="title-text">
              <h2 class="title">Chi tiết phiếu đặt bàn</h2>
              <p class="subtitle-text">
                Mã định danh hệ thống:
                <span class="highlight-id">{{ viewMonDaDat.maDatBan }}</span>
              </p>
            </div>
          </div>

          <div
            v-if="trangThaiCoc === 'CHUA_COC' && trangThaiDatBan != 'DA_HUY'"
            class="timer-line"
          >
            <span>Vui lòng thanh toán cọc, phiếu hủy sau:</span>
            <span class="timer-value">
              {{ countdown }} <i class="fa-regular fa-clock-five"></i>
            </span>
          </div>
        </div>

        <button class="icon-btn-back" @click="goBack" title="quay lại">
          <img :src="iconBack" />
        </button>
      </div>
    </header>
    <div
      v-if="['DA_HUY', 'HET_HAN', 'KHONG_DEN'].includes(trangThaiDatBan)"
      class="cancel-banner mb-4"
    >
      <div class="cancel-banner-inner">
        <div class="cancel-banner-icon">
          <i class="bi bi-x-circle-fill"></i>
        </div>
        <div>
          <p class="cancel-banner-title">Phiếu đặt bàn đã bị hủy</p>
          <p class="cancel-banner-sub">
            Phiếu đặt này không còn hiệu lực và không thể tiếp tục xử lý
          </p>
        </div>
      </div>
    </div>
    <div class="row g-4 mb-4">
      <div class="col-md-4">
        <div class="info-glass-card">
          <div class="card-label">
            <i class="fa-solid fa-user-tie me-2"></i> khách hàng
          </div>
          <div class="card-value-group">
            <div class="info-row-detail">
              <span class="info-label"
                ><i class="ri-user-3-line"></i> Họ tên</span
              >
              <span class="info-value">{{ viewMonDaDat.khachHang }}</span>
            </div>
            <div class="info-row-detail">
              <span class="info-label"><i class="ri-phone-line"></i> SĐT</span>
              <span class="info-value">{{ viewMonDaDat.sdt }}</span>
            </div>
            <div class="info-row-detail">
              <span class="info-label"
                ><i class="ri-group-line"></i> Số khách</span
              >
              <span class="info-value">{{ viewMonDaDat.soNguoi }} khách</span>
            </div>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="info-glass-card">
          <div class="card-label">
            <i class="fa-solid fa-location-dot me-2"></i>bàn phục vụ
          </div>
          <div class="card-value-group">
            <template
              v-if="
                viewMonDaDat.listTenBan && viewMonDaDat.listTenBan.length > 0
              "
            >
              <div
                class="info-row-detail"
                style="align-items: flex-start; border-bottom: none"
              >
                <span class="info-label mt-1"
                  ><i class="ri-map-pin-line"></i> Vị trí</span
                >
                <div class="info-value">
                  <div
                    class="ban-list"
                    style="
                      justify-content: flex-end;
                      display: flex;
                      flex-wrap: wrap;
                      gap: 8px;
                    "
                  >
                    <span
                      v-for="ban in viewMonDaDat.listTenBan"
                      :key="ban"
                      class="ban-tag"
                    >
                      {{ "Bàn " + ban }} - {{ viewMonDaDat.tenKhuVuc }}
                    </span>
                  </div>
                </div>
              </div>
            </template>
            <div
              v-else
              class="d-flex flex-column align-items-center justify-content-center text-muted py-3"
            >
              <i
                class="fa-solid fa-chair mb-2"
                style="font-size: 24px; color: #c4c4c4"
              ></i>
              <span style="font-size: 14px; font-weight: 500"
                >Chưa xếp bàn</span
              >
            </div>
            <div
              class="mt-3 pt-3 border-top text-end"
              v-if="
                viewMonDaDat.listTenBan && viewMonDaDat.listTenBan.length > 0
              "
            >
              <button
                class="btn btn-sm btn-outline-danger"
                style="border-radius: 6px; font-weight: 500"
                @click="openPickTable"
              >
                <i class="ri-edit-line me-1"></i> Chọn lại bàn
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="info-glass-card">
          <div class="card-label">
            <i class="bi bi-calendar-check me-2"></i> chi tiết đặt
          </div>

          <div class="card-value-group detail-horizontal-wrapper">
            <div class="info-row-inline">
              <div class="info-icon-min">
                <i class="bi bi-clock"></i>
              </div>
              <div class="info-content">
                <span class="info-sub-label">thời gian</span>
                <p class="info-main-value">{{ viewMonDaDat.ngayGio }}</p>
              </div>
            </div>

            <div class="mini-divider"></div>

            <div class="info-row-inline">
              <div class="info-icon-min">
                <i class="bi bi-geo-alt"></i>
              </div>
              <div class="info-content">
                <span class="info-sub-label">hình thức</span>
                <p class="info-main-value">{{ viewMonDaDat.hinhThucDat }}</p>
              </div>
            </div>

            <div class="info-row-inline mt-3 pt-3 border-top">
              <div class="info-icon-min">
                <i class="bi bi-activity"></i>
              </div>
              <div class="info-content d-flex align-items-center">
                <span class="info-sub-label mb-0 me-2" style="font-weight: 700"
                  >Trạng thái :</span
                >
                <div
                  :class="[
                    'status-pill-v3',
                    getStatusClass(viewMonDaDat.trangThai),
                  ]"
                  style="display: inline-flex; margin: 0"
                >
                  <i
                    :class="
                      viewMonDaDat.trangThai === 'đã xác nhận'
                        ? 'bi bi-check-circle-fill'
                        : 'bi bi-patch-check-fill'
                    "
                  ></i>
                  {{ viewMonDaDat.trangThai }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="row g-4">
      <div class="col-lg-12">
        <div class="main-white-card">
          <div class="section-header-v2">
            <h3 class="title-bold">danh sách món ăn</h3>
            <span class="count-badge"
              >{{ viewMonDaDat.listMonAn.length }} món</span
            >
          </div>

          <div class="table-responsive mt-4">
            <table class="fsh-table-v2">
              <thead>
                <tr>
                  <th width="45%">Tên món ăn</th>
                  <th class="text-center">Số lượng</th>
                  <th class="text-end">Đơn giá</th>
                  <th class="text-end">Thành tiền</th>
                  <th class="text-center">Trạng thái</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(mon, idx) in pagedMonDaDat" :key="idx">
                  <td>
                    <div class="food-item">
                      <img :src="getImageUrl(mon.hinhAnh)" class="food-img" />
                      <div class="food-info">
                        <span class="food-name-v2">{{ mon.tenMon }}</span>
                        <span class="food-danh-muc">{{ mon.tenDanhMuc }}</span>
                      </div>
                    </div>
                  </td>
                  <td class="text-center">
                    <span class="qty-text">x{{ mon.soLuong }}</span>
                  </td>
                  <td class="text-end price-text">
                    {{ mon.gia.toLocaleString() }} đ
                  </td>
                  <td class="text-end total-text">
                    {{ (mon.gia * mon.soLuong).toLocaleString() }} đ
                  </td>
                  <td class="text-center">
                    <span
                      :class="['status-badge', statusBadgeClass[mon.trangThai]]"
                    >
                      {{ statusBadgeText[mon.trangThai] }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>

            <!-- PHÂN TRANG -->
            <div
              v-if="totalPagesMonDaDat > 1"
              class="pagination pagination-sm mt-2"
            >
              <button
                :disabled="currentPageMonDaDat === 1"
                @click="currentPageMonDaDat--"
                class="pg-btn pg-arrow"
              >
                <i class="ri-arrow-left-s-line"></i>
              </button>
              <template v-for="p in pageListMonDaDat" :key="p">
                <button v-if="p === '…'" class="pg-btn pg-ellipsis" disabled>
                  ···
                </button>
                <button
                  v-else
                  class="pg-btn"
                  :class="{ active: p === currentPageMonDaDat }"
                  @click="currentPageMonDaDat = p"
                >
                  {{ p }}
                </button>
              </template>
              <button
                :disabled="currentPageMonDaDat === totalPagesMonDaDat"
                @click="currentPageMonDaDat++"
                class="pg-btn pg-arrow"
              >
                <i class="ri-arrow-right-s-line"></i>
              </button>
            </div>
          </div>
          <div class="summary-box-v2 mt-4">
            <div v-if="!viewMonDaDat.listMonAn?.length" class="empty-summary">
              <i class="bi bi-egg-fried"></i>
              <p>Chưa có món nào được gọi</p>
            </div>

            <template v-else>
              <div class="summary-line">
                <span>Tổng cộng tạm tính</span>
                <span class="val">{{ totalFoodPrice.toLocaleString() }} đ</span>
              </div>
              <div
                class="summary-line"
                v-if="tienCoc > 0 && trangThaiCoc !== 'CHUA_COC'"
              >
                <span>Tiền đã cọc</span>
                <span class="val">- {{ tienCoc.toLocaleString() }} đ</span>
              </div>
              <div
                class="summary-line"
                v-if="viewHoaDon?.maGiamGia || viewHoaDon?.soTienDuocGiam > 0"
              >
                <div style="display: flex; align-items: center; gap: 8px">
                  Giảm giá
                  <span v-if="viewHoaDon?.maGiamGia" class="discount-badge">
                    {{ viewHoaDon.maGiamGia }} · {{ viewHoaDon.giaTriGiam }}%
                  </span>
                </div>
                <span class="val">
                  {{
                    viewHoaDon?.soTienDuocGiam > 0
                      ? "- " + viewHoaDon.soTienDuocGiam.toLocaleString() + " đ"
                      : ""
                  }}
                </span>
              </div>

              <div class="summary-line" v-if="viewHoaDon?.giamTrucTiep > 0">
                <span>Giảm trực tiếp</span>
                <span class="val"
                  >- {{ viewHoaDon.giamTrucTiep.toLocaleString() }} đ</span
                >
              </div>

              <div
                class="summary-line"
                v-if="viewHoaDon?.diemTichLuySuDung > 0"
              >
                <span>Điểm tích lũy sử dụng</span>
                <span class="val">{{ viewHoaDon.diemTichLuySuDung }}</span>
              </div>

              <div
                class="summary-total-final"
                v-if="viewHoaDon?.tongTienDaThanhToan > 0"
              >
                <div class="final-label">Tổng tiền đã thanh toán</div>
                <div class="final-value">
                  {{ viewHoaDon.tongTienDaThanhToan.toLocaleString() }} đ
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>

      <div class="col-lg-6">
        <div class="main-white-card h-100">
          <div class="section-header-v2 mb-4 d-flex align-items-center">
            <i
              class="fa-solid fa-layer-group me-2"
              style="font-size: 15px; color: #000"
            ></i>
            <h3 class="title-bold mb-0">lịch sử đơn hàng</h3>
          </div>
          <div class="fsh-timeline-v3">
            <div class="fsh-timeline-v3">
              <div class="timeline-scroll">
                <div
                  v-for="(log, i) in lichSu"
                  :key="i"
                  class="timeline-step-v3"
                  :class="getTimelineClass(log.hanhDong)"
                >
                  <div class="step-left">
                    <div class="dot-outer">
                      <div class="dot-inner"></div>
                    </div>

                    <div v-if="i !== lichSu.length - 1" class="step-line"></div>
                  </div>

                  <div class="step-right">
                    <span class="step-time-v3">
                      {{ formatDateTime(log.thoiGian) }} - {{ log.tenNhanVien }}
                    </span>

                    <p class="step-msg-v3">
                      <b>{{ log.hanhDong }}:</b> {{ log.ghiChu }}
                    </p>
                  </div>
                </div>
                <div v-if="lichSu.length === 0" class="text-center text-muted">
                  Chưa có lịch sử
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-lg-6">
        <div class="main-white-card h-100 d-flex flex-column">
          <div
            class="section-header-v2 mb-4 d-flex align-items-center"
            style="margin-left: -15px"
          >
            <i
              class="fa-solid fa-bolt-lightning me-3"
              style="font-size: 15px; color: #000"
            ></i>
            <h3 class="title-bold mb-0">thao tác nhanh</h3>
          </div>
          <div class="action-wrapper d-flex flex-column flex-grow-1">
            <!-- ĐÃ HỦY / HẾT HẠN / KHÔNG ĐẾN -->
            <div
              v-if="
                ['DA_HUY', 'HET_HAN', 'KHONG_DEN'].includes(trangThaiDatBan)
              "
              class="cancelled-notice"
            >
              <i class="ri-information-line"></i>
              <div>
                <p class="cancelled-notice-title">Phiếu đặt đã bị hủy</p>
                <p class="cancelled-notice-sub">
                  Mọi thao tác không còn hiệu lực.
                </p>
              </div>
            </div>

            <!-- CHỜ HOÀN CỌC -->
            <template v-else-if="trangThaiDatBan === 'CHO_HOAN_COC'">
              <div class="cancelled-notice mb-3">
                <i class="ri-information-line"></i>
                <div>
                  <p class="cancelled-notice-title">Đang chờ hoàn cọc</p>
                  <p class="cancelled-notice-sub">
                    Vui lòng xác nhận hoàn tiền cọc cho khách.
                  </p>
                </div>
              </div>
              <div
                class="mt-auto d-flex gap-3 pt-4 w-100"
                style="border-top: 1px dashed #eee"
              >
                <button
                  class="btn-hoan-coc-outline"
                  @click="handleXacNhanHoanCoc"
                >
                  <i class="ri-money-dollar-circle-line me-2"></i>
                  Xác nhận đã hoàn cọc
                </button>
              </div>
            </template>

            <template
              v-else-if="
                ['CHO_XAC_NHAN', 'CHUA_COC', 'DA_COC'].includes(trangThaiDatBan)
              "
            >
              <div
                class="cancelled-notice mb-3"
                style="
                  background: #eff6ff;
                  border-color: #bfdbfe;
                  color: #1d4ed8;
                "
              >
                <i class="bi bi-clock-fill" style="color: #3b82f6"></i>
                <div>
                  <p class="cancelled-notice-title" style="color: #1d4ed8">
                    Đang chờ xác nhận
                  </p>
                  <p class="cancelled-notice-sub" style="color: #1e40af">
                    Phiếu đặt bàn đang chờ được xử lý.
                  </p>
                </div>
              </div>
            </template>

            <!-- ĐÃ XÁC NHẬN -->
            <template v-else-if="trangThaiDatBan === 'DA_XAC_NHAN'">
              <div
                class="cancelled-notice mb-3"
                style="
                  background: #f0fdf4;
                  border-color: #86efac;
                  color: #15803d;
                "
              >
                <i class="bi bi-check-circle-fill" style="color: #16a34a"></i>
                <div>
                  <p class="cancelled-notice-title" style="color: #15803d">
                    Đã xác nhận
                  </p>
                  <p class="cancelled-notice-sub" style="color: #166534">
                    Đã gửi thông báo cho khách hàng.
                  </p>
                </div>
              </div>
            </template>

            <!-- ĐANG PHỤC VỤ -->
            <template v-else-if="trangThaiDatBan === 'DANG_PHUC_VU'">
              <div
                class="cancelled-notice mb-3"
                style="
                  background: #eff6ff;
                  border-color: #93c5fd;
                  color: #1d4ed8;
                "
              >
                <i class="ri-restaurant-line" style="color: #2563eb"></i>
                <div>
                  <p class="cancelled-notice-title" style="color: #1d4ed8">
                    Đang phục vụ
                  </p>
                  <p class="cancelled-notice-sub" style="color: #1e40af">
                    Đang phục vụ khách tại bàn.
                  </p>
                </div>
              </div>
            </template>

            <!-- HOÀN THÀNH -->
            <div
              v-else-if="trangThaiDatBan === 'HOAN_THANH'"
              class="cancelled-notice"
              style="background: #f0fdf4; border-color: #86efac; color: #15803d"
            >
              <i class="bi bi-check-circle-fill" style="color: #16a34a"></i>
              <div>
                <p class="cancelled-notice-title" style="color: #15803d">
                  Phiếu đặt đã hoàn thành
                </p>
                <p class="cancelled-notice-sub" style="color: #166534">
                  Phiếu đặt bàn đã kết thúc.
                </p>
              </div>
            </div>

            <div
              v-if="
                trangThaiCoc === 'CHUA_COC' ||
                trangThaiCoc === 'KHONG_CAN_COC' ||
                ['CHO_XAC_NHAN', 'DA_XAC_NHAN'].includes(trangThaiDatBan)
              "
              class="mt-auto d-flex gap-3 pt-4 w-100"
              style="border-top: 1px dashed #eee"
            >
              <button
                v-if="trangThaiCoc === 'CHUA_COC'"
                class="btn-action-main deposit-btn flex-fill"
                @click="openDepositModal"
              >
                <div class="btn-light-sweep"></div>
                <div class="btn-content-center">
                  <i class="fa-solid fa-hand-holding-dollar"></i>
                  <span>XÁC NHẬN ĐẶT CỌC</span>
                </div>
              </button>

              <button
                v-if="
                  trangThaiCoc === 'KHONG_CAN_COC' &&
                  trangThaiDatBan === 'CHO_XAC_NHAN'
                "
                class="btn-action-main confirm-only-btn flex-fill"
                @click="xacNhanKhongCoc"
              >
                <div class="btn-light-sweep"></div>
                <div class="btn-content-center">
                  <i class="fa-solid fa-paper-plane"></i>
                  <span>XÁC NHẬN</span>
                </div>
              </button>

              <button
                v-if="
                  ['CHO_XAC_NHAN', 'CHO_COC', 'DA_XAC_NHAN'].includes(
                    trangThaiDatBan,
                  )
                "
                class="btn-danger-outline flex-fill"
                @click="openModalHuy"
              >
                Hủy phiếu
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <div v-if="isLoading" class="admin-luxury-overlay">
    <div class="luxury-loader-card">
      <div class="inner-border"></div>

      <div class="loader-content">
        <div class="premium-ring-wrapper">
          <div class="ring-child"></div>
          <div class="ring-child"></div>
          <div class="logo-placeholder">FSH</div>
        </div>

        <h3 class="luxury-status">Đang xử lý hệ thống</h3>
        <div class="status-line"></div>
        <p class="luxury-note">Vui lòng không đóng trình duyệt lúc này.</p>
      </div>
    </div>
  </div>
  <div
    v-if="showDepositModal"
    class="fsh-modal-overlay"
    @click.self="showDepositModal = false"
  >
    <div class="fsh-modal-content">
      <button class="modal-close-btn" @click="showDepositModal = false">
        <i class="fa-solid fa-xmark"></i>
      </button>

      <div class="modal-header-modern">
        <div class="icon-badge-gold">
          <i class="fa-solid fa-coins"></i>
        </div>
        <h4 class="modal-title-bold">Xác Nhận Tiền Cọc</h4>
        <p class="modal-subtitle">
          Vui lòng nhập và kiểm tra thông tin tiền cọc của khách hàng
        </p>
      </div>

      <div class="suggestion-box mb-4">
        <div class="suggestion-info">
          <i class="fa-solid fa-circle-info info-icon"></i>
          <span class="label">Gợi ý từ hệ thống</span>
        </div>
        <span class="value">{{ tienPhaiCoc.toLocaleString() }}đ</span>
      </div>

      <div class="premium-input-group mb-4">
        <label class="currency-label">Số tiền cọc thực tế (VNĐ)</label>
        <div class="input-with-icon-v2">
          <i class="fa-solid fa-money-bill-wave input-field-icon"></i>
          <input
            type="text"
            v-model="depositDisplay"
            class="fsh-input-luxury"
            placeholder="0"
          />
        </div>
      </div>

      <div class="premium-input-group mb-4">
        <label class="currency-label">Hình thức đặt cọc</label>
        <div class="pay-methods pay-methods-horizontal">
          <label
            v-for="pt in danhSachPhuongThuc"
            :key="pt.id"
            class="radio-card radio-card-online"
            :class="{
              active: Number(idPhuongThucThanhToan) === Number(pt.id),
            }"
          >
            <input
              type="radio"
              :value="pt.id"
              v-model="idPhuongThucThanhToan"
              @change="errorPhuongThuc = ''"
              style="display: none"
            />
            <img
              v-if="String(pt.tenPhuongThuc).toLowerCase().includes('zalopay')"
              :src="ZaloPay"
              class="icon-pt"
            />
            <img
              v-else-if="
                String(pt.tenPhuongThuc).toLowerCase().includes('vnpay')
              "
              :src="VNPAY"
              class="icon-pt"
            />
            <i
              v-else-if="
                String(pt.tenPhuongThuc).toLowerCase().includes('tiền mặt') ||
                String(pt.tenPhuongThuc).toLowerCase().includes('tien mat')
              "
              class="bi bi-cash-stack"
              style="font-size: 18px; color: #10b981"
            ></i>
            <i
              v-else
              class="bi bi-bank"
              style="font-size: 18px; color: #3b82f6"
            ></i>
            <span>{{ pt.tenPhuongThuc }}</span>
          </label>
        </div>
        <p v-if="errorPhuongThuc" class="error-text error-phuong-thuc mb-0">
          <i class="bi bi-exclamation-circle"></i> {{ errorPhuongThuc }}
        </p>
      </div>

      <div class="modal-actions-grid-v2">
        <button class="btn-modal-cancel" @click="showDepositModal = false">
          Hủy bỏ
        </button>
        <button
          class="btn-modal-confirm"
          :disabled="dangTaoQr"
          @click="xuLyXacNhanGiaoDich"
        >
          <div class="btn-light-sweep"></div>
          <i v-if="!dangTaoQr" class="fa-solid fa-circle-check me-2"></i>
          {{ dangTaoQr ? "Đang tạo QR..." : "Xác nhận" }}
        </button>
      </div>
    </div>
  </div>

  <div v-if="hienModalQr" class="modal-qr-overlay" @click.self="dongModalQr">
    <div class="modal-qr-container">
      <div class="modal-qr-header">
        <span>Quét mã để thanh toán</span>
        <button class="btn-close-vnpay" @click="dongModalQr">×</button>
      </div>

      <div class="modal-qr-body">
        <div class="qr-info-row">
          <span>Mã phiếu đặt</span>
          <strong>{{ viewMonDaDat.maDatBan }}</strong>
        </div>
        <div class="qr-info-row">
          <span>Khách hàng</span>
          <strong>{{ viewMonDaDat.khachHang }}</strong>
        </div>
        <div class="qr-info-row qr-total">
          <span>Số tiền cọc</span>
          <strong>{{ depositDisplay }} đ</strong>
        </div>

        <div class="qr-image-wrapper">
          <img :src="qrCodeUrl" class="qr-image-large" alt="QR thanh toán" />
        </div>

        <p class="qr-note">
          Đưa điện thoại cho khách quét mã bằng app ngân hàng/ví điện tử. Sau
          khi khách chuyển khoản xong, bấm xác nhận bên dưới.
        </p>
      </div>

      <div class="modal-qr-footer">
        <button class="btn-cancel" @click="dongModalQr">Đóng</button>
        <button
          class="btn-primary"
          :disabled="isLoading"
          @click="xacNhanVaDatCoc"
        >
          <div class="btn-light-sweep"></div>
          {{ isLoading ? "Đang xử lý..." : "Xác nhận" }}
        </button>
      </div>
    </div>
  </div>

  <transition name="fade-scale">
    <div
      v-if="modalHuyDatBan"
      class="cancel-overlay"
      @click.self="modalHuyDatBan = false"
    >
      <div class="cancel-modal">
        <div class="cancel-modal-header">
          <div class="cancel-modal-icon">
            <i class="ri-spam-2-fill"></i>
          </div>
          <div>
            <h3>Hủy phiếu đặt bàn</h3>
            <p>Thao tác này không thể hoàn tác sau khi xác nhận</p>
          </div>
          <button class="cancel-modal-close" @click="modalHuyDatBan = false">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>

        <div class="cancel-modal-body">
          <div v-if="preview" class="refund-info">
            <div class="refund-info-row">
              <span
                ><i class="bi bi-arrow-return-left me-2"></i>Hoàn tiền cọc</span
              >
              <span class="refund-positive"
                >+ {{ preview.tienCocHoan?.toLocaleString() }} đ</span
              >
            </div>
            <div class="refund-info-row">
              <span
                ><i class="bi bi-dash-circle me-2"></i>Phí hủy (Mất cọc)</span
              >
              <span class="refund-negative"
                >- {{ preview.tienCocBiMat?.toLocaleString() }} đ</span
              >
            </div>
          </div>

          <div class="cancel-reason">
            <label>Lý do hủy <span class="required">*</span></label>
            <textarea
              v-model="lyDoHuy"
              placeholder="Nhập lý do hủy..."
              rows="4"
            ></textarea>
          </div>
        </div>

        <div class="cancel-modal-footer">
          <button class="cancel-btn-secondary" @click="modalHuyDatBan = false">
            Đóng
          </button>
          <button class="cancel-btn-confirm" @click="handleHuyDatBan">
            <i class="bi bi-check2-all"></i>
            <div class="btn-light-sweep"></div>
            Xác nhận
          </button>
        </div>
      </div>
    </div>
  </transition>

  <xep-lai-ban-modal
    v-if="showModalChonBan"
    :bookingData="bookingDataForTable"
    @close="showModalChonBan = false"
    @success="handleSuccessXepBan"
  />
</template>

<style scoped>
.info-row-detail {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px dashed #eee;
}
.info-row-detail:last-child {
  border-bottom: none;
}
.info-label {
  color: #666;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.info-value {
  font-weight: 600;
  color: #333;
  font-size: 15px;
}

.text-green {
  color: #16a34a !important;
}

.discount-badge {
  display: inline-block;
  background: #fef3f2;
  color: #d73737;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 99px;
  border: 1px solid #fecaca;
}

.timeline-scroll {
  max-height: 220px;
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
.empty-summary {
  text-align: center;
  padding: 20px 0;
  color: #999;
}

.empty-summary i {
  font-size: 26px;
  display: block;
  margin-bottom: 8px;
}
.confirm-only-btn {
  display: flex;
  justify-content: center;
  align-items: center;
}

.btn-content-center {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px; /* khoảng cách icon và chữ */
  width: 100%;
}

.confirm-only-btn {
  background: #db3333;
  color: #fff;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}
.timer-line {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #e53e3e;
  margin-top: 4px;
  padding-left: 0;
  white-space: nowrap;
}

.timer-value {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #ef4444;
  font-weight: 700;
  padding: 2px 8px;
  background: #fee2e2;
  border-radius: 6px;
  font-variant-numeric: tabular-nums;
}

.timer-value i {
  font-size: 12px;
  opacity: 0.8;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 0.8;
  }
  50% {
    transform: scale(1.1);
    opacity: 1;
  }
  100% {
    transform: scale(1);
    opacity: 0.8;
  }
}
/* CHI TIET DAT */

/* Container bọc nội dung chi tiết */
.detail-horizontal-wrapper {
  display: flex;
  flex-wrap: wrap; /* Cho phép xuống dòng nếu màn hình quá nhỏ */
  align-items: center;
  gap: 15px;
}

/* Từng khối thông tin dàn ngang */
.info-row-inline {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1; /* Để 2 khối chia đều không gian ngang */
  min-width: fit-content;
}

/* Icon nhỏ gọn lại để dàn ngang không bị thô */
.info-icon-min {
  width: 32px;
  height: 32px;
  background: #f8fafc;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  font-size: 14px;
  border: 1px solid #f1f5f9;
  flex-shrink: 0;
}

.info-sub-label {
  display: block;
  font-size: 10px;
  font-weight: 900;
  color: #d15454;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  line-height: 1;
  margin-bottom: 2px;
}

.info-main-value {
  font-size: 13px;
  font-weight: 800;
  color: #1e293b;
  margin: 0;
  white-space: nowrap;
}
.mini-divider {
  width: 1px;
  height: 25px;
  background: #e2e8f0;
}

.status-wrapper-inline {
  width: 100%;
  display: flex;
  justify-content: flex-start;
}

/* HET */
.box-header-card {
  background: #fff;
  border-radius: 6px;
  padding: 20px 24px;
  margin-bottom: 20px;

  border: 1px solid var(--border-color);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}

.badge-group {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.table-tag {
  background-color: #f1f5f9; /* Màu nền xám nhạt */
  color: #475569; /* Chữ xám đậm */
  border: 1px solid #e2e8f0;
  padding: 2px 10px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}
/* /////////////////// GỬI MAIL /////////////// */
.admin-luxury-overlay {
  position: fixed;
  inset: 0;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999999;
  animation: fadeIn 0.5s ease;
}

.loading-box {
  background: #ffffff;
  padding: 60px 40px;
  border-radius: 24px;
  text-align: center;
  width: 360px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  animation: slideInUp 0.4s ease-out;
}

.loading-title {
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
    Helvetica, Arial, sans-serif;
  font-size: 18px;
  font-weight: 400;
  color: #2c3e50;
  margin: 0 0 10px 0;
  letter-spacing: -0.01em;
}

.loading-sub {
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
    Helvetica, Arial, sans-serif;
  font-size: 14px;
  font-weight: 300;
  color: #7f8c8d;
  margin-bottom: 40px;
  line-height: 1.6;
}

.dot-loader {
  display: flex;
  justify-content: center;
  z-index: 99999;
}

.luxury-loader-card {
  position: relative;
  background: #ffffff;
  padding: 50px 80px;
  border-radius: 2px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f0f0;

  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.luxury-loader-card {
  position: relative;
  background: #ffffff;
  padding: 50px 80px;
  border-radius: 2px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f0f0;
  text-align: center;
}

.inner-border {
  position: absolute;
  inset: 10px;
  border: 1px solid #e5e5e5;
  pointer-events: none;
}

.premium-ring-wrapper {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto 25px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ring-child {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 1px solid transparent;
  border-top-color: #f03232; /* Vàng Champagne nhạt */
  border-radius: 50%;
  animation: luxury-spin 1.5s cubic-bezier(0.5, 0, 0.5, 1) infinite;
}

.ring-child:nth-child(2) {
  width: 80%;
  height: 80%;
  animation-delay: 0.2s;
  border-top-color: #e0e0e0;
}

.logo-placeholder {
  font-family: "Serif";
  font-size: 20px;
  color: #ef4545;
  font-weight: 300;
}

.luxury-status {
  font-family: "Inter", sans-serif;
  text-transform: uppercase;
  letter-spacing: 3px;
  font-size: 13px;
  color: #222;
  font-weight: 500;
  margin: 0;
}

.status-line {
  width: 40px;
  height: 1px;
  background: #c5a059;
  margin: 15px auto;
}

.luxury-note {
  font-size: 13px;
  color: #999;
  letter-spacing: 1px;
}

@keyframes luxury-spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
/* layout nền */
.fsh-detail-container {
  background-color: #f6f8fb;
  min-height: 100vh;
  font-family: "Inter", sans-serif;
}

.brand-icon-box {
  width: 45px;
  height: 45px;
  background: #fff5f5;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}
.brand-icon-box img {
  width: 25px;
}
.main-title {
  font-size: 18px;
  font-weight: 800;
  color: #1e293b;
  margin: 0;
}
.sub-title {
  font-size: 13px;
  color: #64748b;
  margin: 0;
}
.highlight-id {
  color: #a52a2a;
  font-weight: 700;
}

/* info cards 3 cột */
.info-glass-card {
  background: #fff;
  border: 1px solid #edf2f7;
  border-radius: 5px;
  padding: 10px;
  height: 100%;
  transition: transform 0.3s ease;
}
.info-glass-card:hover {
  transform: translateY(-5px);
}
.card-label {
  font-size: 15px;
  font-weight: 700;
  color: #a54f4f;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 15px;
}
.user-name,
.user-sub {
  font-size: 15px;
  font-weight: 500;
  color: #431818;
  margin-bottom: 8px;
}

.ban-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.ban-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: #fff5f5;
  color: #c0392b;
  border: 1px solid rgba(192, 57, 43, 0.2);
  padding: 6px 14px;
  border-radius: 999px;
  font-weight: 600;
  font-size: 13px;
  transition: all 0.2s;
}

.ban-tag:hover {
  background: #fde8e8;
  border-color: #c0392b;
}

/* DANH SACH MON AN */
.main-white-card {
  background: #fff;
  border-radius: 5px;
  padding: 30px;
  border: 1.5px solid #eee;
  box-shadow: 10px 10px 0px #00000005;
}

/* Header Section */
.section-header-v2 {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-bold {
  font-size: 18px;
  font-weight: 900;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin: 0;
  color: #000;
}

.count-badge {
  background: #d33535;
  color: #fff;
  font-size: 10px;
  font-weight: 900;
  padding: 2px 8px;
  border-radius: 4px;
  text-transform: uppercase;
}

/* Table Design - Sắc nét */
.fsh-table-v2 {
  width: 100%;
  border-collapse: collapse;
}

.fsh-table-v2 thead th {
  padding: 15px 10px;

  font-size: 14px;
  font-weight: 900;
  color: #453f3f;
  letter-spacing: 0.5px;
  border-radius: 5px;
}

.fsh-table-v2 tbody tr {
  border-bottom: 1px solid #f0f0f0;
}

.fsh-table-v2 tbody tr:hover {
  background: #fafafa;
}

.fsh-table-v2 td {
  padding: 13px 10px;
  vertical-align: middle;
}

.food-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.food-img-v2 {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #eee;
}

.food-name-v2 {
  font-size: 14px;
  font-weight: 800;
  color: #000;
}
.food-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.food-danh-muc {
  display: inline-block;
  width: fit-content;
  padding: 4px 10px;
  border-radius: 7px;
  background: #eff4fc;
  color: #806b6b;
  font-size: 12px;
  font-weight: 500;
}
.qty-text {
  font-weight: 900;
  color: #a52a2a;
  font-family: "Poppins", sans-serif;
}

.price-text {
  color: #666;
  font-weight: 600;
  font-size: 13px;
}

.total-text {
  font-weight: 900;
  color: #000;
  font-size: 15px;
}

/* Summary Box - Kiểu thẻ Luxury */
.summary-box-v2 {
  background: #f8f9fa;
  padding: 25px;
  border-radius: 8px;
}

.summary-line {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 700;
  color: #555555;
  padding: 10px;
}

.summary-line.red {
  color: #3b3535;
}

.summary-line .val {
  color: #000;
  font-weight: 900;
}

.summary-total-final {
  margin-top: 15px;
  padding-top: 35px;
  border-top: 1px solid #d44d4d;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.final-label {
  font-size: 20px;
  font-weight: 900;
  color: #000;
}

.final-value {
  font-size: 22px;
  font-weight: 900;
  color: #e83030;
  line-height: 1;
}

.summary-price {
  font-size: 26px;
  font-weight: 900;
  color: #a52a2a;
}

/* status badges */
.status-pill-v3 {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 8px;
  font-weight: 700;
  font-size: 12px;
}

.stt-confirmed {
  background: #e9fbfc;
  color: #d33232;
}
/* buttons */
.btn-fsh-primary {
  background: #a52a2a;
  color: white;
  border: none;
  padding: 14px;
  border-radius: 12px;
  font-weight: 700;
  transition: 0.3s;
}
.btn-fsh-primary:hover {
  background: #802020;
  transform: scale(1.02);
}
.btn-fsh-outline {
  background: #fff;
  border: 1px solid #e2e8f0;
  padding: 10px;
  border-radius: 10px;
  font-weight: 600;
  font-size: 13px;
}

/* LICH SU DAT BAN */
/* Container */
.fsh-timeline-v3 {
  padding: 10px 5px;
}

.timeline-step-v3 {
  display: flex;
  gap: 15px;
}

.step-left {
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* Hiệu ứng Dot 2 lớp */
.dot-outer {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border: 1px solid #e2e8f0;
  z-index: 2;
}

.dot-inner {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #cbd5e1;
}

/* Màu sắc cho từng trạng thái */
.success .dot-outer {
  border-color: #af2b2b;
  background: #ecfdf5;
}
.success .dot-inner {
  background: #ba3333;
}

.primary .dot-outer {
  border-color: #c72d2d9e;
  background: #eff6ff;
}
.primary .dot-inner {
  background: #d91d1d;
}

/* Đường kẻ trục dọc */
.step-line {
  width: 1px;
  flex-grow: 1;
  background: #354f69;
  margin: 4px 0;
}

/* Nội dung bên phải */
.step-right {
  padding-bottom: 25px;
}

.step-time-v3 {
  display: block;
  font-size: 11px;
  font-weight: 900; /* Black font cho thời gian */
  color: #000;
  margin-bottom: 4px;
  letter-spacing: 0.5px;
}

.step-msg-v3 {
  font-size: 13px;
  color: #475569;
  margin: 0;
  line-height: 1.5;
}

.step-msg-v3 b {
  font-weight: 800;
  color: #1e293b;
}

/* Trạng thái chờ */
.pending .dot-outer {
  border-style: dashed;
}
.pending .dot-inner {
  opacity: 0.3;
}

/* THAO TAC */

/* Nút chính (Deposit & Confirm) */
.btn-action-main {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px 24px;
  border-radius: 7px;
  border: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.btn-content {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 800;
  text-transform: uppercase;
  font-size: 14px;
  letter-spacing: 0.5px;
}

.arrow-icon {
  font-size: 12px;
  opacity: 0.5;
  transition: transform 0.3s ease;
}

/* Màu Đen cho xác nhận mail */
.confirm-btn {
  background: #be3333;
  color: #fff;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

/* Màu cho nút đặt cọc - đồng bộ với nút xác nhận */
.deposit-btn {
  background: #db3333;
  color: #fff;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.btn-action-main:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.btn-action-main:hover .arrow-icon {
  transform: translateX(5px);
  opacity: 1;
}

/* Nút phụ (Quay lại & Hủy đơn) */
.btn-secondary-outline,
.btn-danger-outline {
  padding: 12px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 700;
  text-transform: uppercase;
  transition: all 0.2s ease;
  border: 1.5px solid #eee;
  background: transparent;
}

.btn-secondary-outline {
  color: #64748b;
}

.btn-danger-outline {
  color: #ef4444;
  border-color: #fee2e2;
}

.btn-secondary-outline:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
  color: #000;
}

.btn-danger-outline:hover {
  background: #fef2f2;
  border-color: #ef4444;
}
/* HET */

.food-item .food-img {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 6px;
  margin-right: 10px;
}

/* MODAL TIEN COC RE-DESIGN */
.modal-close-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: #f1f5f9;
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 10;
}
.modal-close-btn:hover {
  background: #e2e8f0;
  color: #1e293b;
  transform: rotate(90deg);
}

.fsh-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(16px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.fsh-modal-content {
  background: #ffffff;
  width: 460px;
  padding: 40px 32px 32px;
  border-radius: 7px;
  border: 1px solid rgba(241, 245, 249, 0.8);
  box-shadow: 0 25px 50px -12px rgba(15, 23, 42, 0.08),
    0 0 80px rgba(165, 42, 42, 0.03);
  text-align: center;
  position: relative;
  animation: slideUp 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.modal-header-modern {
  margin-bottom: 24px;
}

.icon-badge-gold {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #a52a2a 0%, #c34343 100%);
  color: #fbbf24;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin: 0 auto 16px;
  box-shadow: 0 10px 25px rgba(165, 42, 42, 0.2);
}

.modal-title-bold {
  font-family: "Inter", -apple-system, sans-serif;
  font-weight: 700;
  font-size: 20px;
  color: #0f172a;
  margin-bottom: 6px;
  letter-spacing: -0.3px;
}

.modal-subtitle {
  font-size: 13.5px;
  color: #64748b;
  margin-bottom: 0;
  line-height: 1.5;
}

.suggestion-box {
  background: #fdf2f2;
  padding: 12px 16px;
  border-radius: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid #fee2e2;
  margin-bottom: 20px !important;
}

.suggestion-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.suggestion-info .info-icon {
  color: #a52a2a;
  font-size: 14px;
}

.suggestion-box .label {
  font-size: 13px;
  font-weight: 500;
  color: #7f1d1d;
}

.suggestion-box .value {
  font-size: 15px;
  font-weight: 700;
  color: #a52a2a;
}

.premium-input-group {
  text-align: left;
  margin-bottom: 20px !important;
}

.currency-label {
  font-size: 12px;
  font-weight: 600;
  color: #475569;
  text-transform: uppercase;
  margin-bottom: 8px;
  letter-spacing: 0.5px;
  display: block;
}

.input-with-icon-v2,
.select-with-icon-v2 {
  position: relative;
  display: flex;
  align-items: center;
  background: #f8fafc;
  border: 1.5px solid #e2e8f0;
  border-radius: 12px;
  padding: 0 16px;
  height: 48px;
  transition: all 0.2s ease;
  width: 100%;
}

.input-with-icon-v2:focus-within,
.select-with-icon-v2:focus-within {
  background: #ffffff;
  border-color: #a52a2a;
  box-shadow: 0 0 0 4px rgba(165, 42, 42, 0.1);
}

.input-field-icon {
  color: #94a3b8;
  font-size: 16px;
  margin-right: 12px;
  flex-shrink: 0;
}

.fsh-input-luxury {
  width: 100%;
  border: none !important;
  background: transparent !important;
  padding: 0;
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  outline: none !important;
  text-align: left;
}

.fsh-input-luxury::placeholder {
  color: #cbd5e1;
}

.modern-select {
  width: 100%;
}

/* Custom vue-select inside premium wrapper */
.modern-select :deep(.vs__dropdown-toggle) {
  border: none !important;
  background: transparent !important;
  padding: 0 !important;
  height: 100% !important;
  box-shadow: none !important;
}

.modern-select :deep(.vs__selected) {
  position: relative !important;
  margin: 0 !important;
  padding: 0 !important;
  color: #1e293b !important;
  font-size: 15px !important;
  font-weight: 600 !important;
  justify-content: flex-start !important;
  text-align: left !important;
}

.modern-select :deep(.vs__search),
.modern-select :deep(.vs__search:focus) {
  border: none !important;
  margin: 0 !important;
  padding: 0 !important;
  background: transparent !important;
}

.modern-select :deep(.vs__search::placeholder) {
  color: #cbd5e1 !important;
  font-size: 15px !important;
  font-weight: 500 !important;
  text-align: left !important;
}

.modern-select :deep(.vs__actions) {
  padding: 0 !important;
}

.modern-select :deep(.vs__open-indicator) {
  fill: #64748b !important;
  transform: scale(0.8);
  display: block !important;
}

/* Dropdown menu của select */
.modern-select :deep(.vs__dropdown-menu) {
  border: 1px solid #e2e8f0 !important;
  border-radius: 12px !important;
  padding: 6px !important;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08) !important;
  margin-top: 8px !important;
}

.modern-select :deep(.vs__dropdown-option) {
  padding: 10px 12px !important;
  border-radius: 8px !important;
  font-size: 14px !important;
  font-weight: 500 !important;
  color: #475569 !important;
}

.modern-select :deep(.vs__dropdown-option--highlight) {
  background: #fdf2f2 !important;
  color: #a52a2a !important;
}

.modern-select :deep(.vs__dropdown-option--selected) {
  background: #a52a2a !important;
  color: #ffffff !important;
}

/* Nút bấm action */
.modal-actions-grid-v2 {
  display: grid;
  grid-template-columns: 1fr 1.5fr;
  gap: 12px;
  margin-top: 28px;
}

.btn-modal-cancel {
  background: #f1f5f9;
  color: #475569;
  border: none;
  height: 48px;
  border-radius: 7px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-modal-cancel:hover {
  background: #e2e8f0;
  color: #1e293b;
}

.btn-modal-confirm {
  position: relative;
  overflow: hidden;
  background: #db3333;
  color: #ffffff;
  border: none;
  height: 48px;
  border-radius: 7px;
  font-weight: 600;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.btn-modal-confirm:hover {
  filter: brightness(1.08);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.btn-modal-confirm:active {
  transform: translateY(0);
}

/* ĐƠN HÀNG ĐÃ BỊ HỦY */
/* Thông báo khi phiếu đã bị hủy */
.cancelled-notice-box {
  padding: 24px 16px;
  background: #f8fafc; /* Nền xám nhạt tinh tế */
  border: 1px dashed #cbd5e1;
  border-radius: 12px;
  text-align: center;
}

.notice-content i {
  font-size: 28px;
  color: #94a3b8; /* Màu xám trung tính */
}

.notice-title {
  font-size: 15px;
  font-weight: 800;
  color: #475569;
  text-transform: uppercase;
  margin-bottom: 4px;
}

.notice-desc {
  font-size: 12px;
  color: #64748b;
  line-height: 1.5;
  margin: 0;
}

/* Tinh chỉnh lại nút secondary cho đồng bộ */
.btn-secondary-outline {
  background: #fff;
  border: 1.5px solid #e2e8f0;
  color: #475569;
  padding: 10px;
  border-radius: 8px;
  font-weight: 700;
  font-size: 13px;
  transition: all 0.2s;
}

.btn-secondary-outline:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.btn-danger-outline {
  border: 1.5px solid #fee2e2;
  color: #dc2626;
  background: #fff;
  padding: 10px;
  border-radius: 8px;
  font-weight: 700;
  font-size: 13px;
}
/* /////////////////// PHÂN TRANG ///////// */

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 24px;
  user-select: none;
}

.pg-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  height: 36px;
  padding: 0 6px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  background-color: #ffffff;
  color: #333333;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.pg-btn:hover:not(:disabled):not(.active) {
  border-color: #ff4d4f;
  color: #ff4d4f;
  background-color: #fff1f0;
}

.pg-btn.active {
  border: 1.5px solid #ff4d4f;
  color: #ff0000;
  font-weight: 600;
}

.pg-arrow i {
  font-size: 18px;
}

.pg-btn:disabled {
  border-color: #f0f0f0;
  background-color: #f5f5f5;
  color: #bfbfbf;
  cursor: not-allowed;
}

.pg-ellipsis {
  border: none;
  background: transparent;
  cursor: default;
  color: #aaa;
  min-width: 24px;
}

.selected-sidebar {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden; /* quan trọng */
}

.selected-list {
  flex: 1;
  overflow-y: auto; /* chỉ list scroll */
  padding: 16px;
  min-height: 0;
}

.pagination-sm {
  flex-shrink: 0; /* không bị ép nhỏ */
  padding: 8px 0;
  border-top: 1px solid #eee;
}

.pagination-sm .pg-btn {
  min-width: 28px;
  height: 28px;
  font-size: 12px;
  padding: 0 6px;
}

/* /////////////// HEADER ////////// */
.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.title-text h2,
.title {
  font-size: 27px;
  font-weight: 500;
  color: #1a1f23;
  margin: 0;
}

.highlight-id {
  color: #a52a2a;
  font-weight: 700;
}

.title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
}

.highlight-id {
  color: #a52a2a;
  font-weight: 700;
}

.timer-value {
  font-weight: 700;
}
.box-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.title-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.icon-box-wrapper {
  width: 55px;
  height: 55px;
  border-radius: 14px;
  background: #fdecec;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-box-wrapper i {
  font-size: 20px;
  color: #c0392b;
}

.subtitle-text {
  margin: 0;
  font-size: 14px;
  color: #637381;
  padding-right: 50px;
}

.highlight-id {
  color: #a52a2a;
  font-weight: 700;
}

.timer-value {
  font-weight: 700;
}

.icon-btn-back {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  border: none;
  background: #f4f6f8;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-btn-back img {
  width: 18px;
}

/* ////////////////// HỦY ĐẶT BÀN /////////// */
.cancel-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.cancel-modal {
  background: #fff;
  width: 100%;
  max-width: 460px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
  animation: slideUp 0.3s ease-out;
}

.cancel-modal-header {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 24px;
  border-bottom: 1px solid #f3f4f6;
}

.cancel-modal-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: #fef2f2;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 20px;
  color: #ef4444;
}
.cancel-modal-close {
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
  transition: all 0.2s;
  flex-shrink: 0;
}

.cancel-modal-header h3 {
  margin: 0 0 4px;
  font-size: 17px;
  font-weight: 700;
  color: #111;
}

.cancel-modal-header p {
  margin: 0;
  font-size: 13px;
  color: #9ca3af;
}

.cancel-modal-close:hover {
  background: #e5e7eb;
  color: #111;
}

.cancel-modal-body {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.refund-info {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
}

.refund-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  font-size: 14px;
  color: #374151;
}

.refund-info-row:not(:last-child) {
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

.cancel-reason label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.cancel-reason .required {
  color: #ef4444;
}

.cancel-reason textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  font-size: 14px;
  resize: none;
  font-family: inherit;
  transition: border-color 0.2s, box-shadow 0.2s;
  box-sizing: border-box;
}

.cancel-reason textarea:focus {
  outline: none;
  border-color: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}

.cancel-modal-footer {
  display: flex;
  gap: 10px;
  padding: 16px 24px;
  background: #f9fafb;
  border-top: 1px solid #f3f4f6;
}

.cancel-btn-secondary {
  flex: 1;
  padding: 11px;
  border: 1px solid #e5e7eb;
  border-radius: 7px;
  background: #fff;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-btn-secondary:hover {
  background: #f3f4f6;
}

.cancel-btn-confirm {
  position: relative;
  overflow: hidden;
  flex: 2;
  padding: 11px;
  border: none;
  border-radius: 7px;
  background: #f12b2b;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-btn-confirm:hover {
  background: #dc2626;
  transform: translateY(-1px);
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
.cancel-btn-confirm:hover .btn-light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
}
.cancel-btn-confirm:hover .btn-light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
}

.fade-scale-enter-active,
.fade-scale-leave-active {
  transition: all 0.3s ease;
}

.fade-scale-enter-from,
.fade-scale-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

.cancelled-notice {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  background: #fef9f0;
  border: 1px solid #fde68a;
  border-radius: 10px;
  color: #92400e;
}

.cancelled-notice i {
  font-size: 20px;
  color: #f59e0b;
  flex-shrink: 0;
  margin-top: 2px;
}

.cancelled-notice-title {
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 700;
}

.cancelled-notice-sub {
  margin: 0;
  font-size: 13px;
  color: #b45309;
}

.btn-hoan-coc-outline {
  background: transparent;
  border: 1.5px solid #16a34a;
  color: #16a34a;
  width: 100%;
  padding: 12px 20px;
  border-radius: 8px;
  font-weight: 700;
  font-size: 13.5px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.btn-hoan-coc-outline:hover {
  background: #f0fdf4;
  border-color: #15803d;
  color: #15803d;
  transform: translateY(-1px);
}

.cancel-banner {
  background: linear-gradient(135deg, #ffeaea, #ffd6d6);
  border: 1px solid #ff4d4f;
  border-radius: 10px;
  padding: 16px 20px;
}

.cancel-banner-inner {
  display: flex;
  align-items: center;
  gap: 14px;
}

.cancel-banner-icon {
  font-size: 28px;
  color: #ff4d4f;
  flex-shrink: 0;
}

.cancel-banner-title {
  margin: 0 0 4px;
  font-size: 16px;
  font-weight: 700;
  color: #b71c1c;
}

.cancel-banner-sub {
  margin: 0;
  font-size: 13px;
  color: #666;
}

/* ===== RESPONSIVE ===== */
@media (max-width: 992px) {
  .box-header-flex {
    flex-direction: column !important;
    gap: 15px !important;
    align-items: stretch !important;
  }

  .icon-btn-back {
    align-self: flex-start !important;
  }

  .title-group {
    width: 100% !important;
  }

  .timer-line {
    font-size: 13px !important;
  }
}

@media (max-width: 768px) {
  .fsh-detail-container {
    padding: 15px !important;
  }

  .info-glass-card {
    padding: 15px !important;
  }

  .food-item {
    flex-direction: column !important;
    align-items: flex-start !important;
    gap: 8px !important;
  }

  .food-img {
    width: 50px !important;
    height: 50px !important;
  }

  .summary-line {
    font-size: 13px !important;
  }

  .summary-total-final {
    flex-direction: column !important;
    align-items: flex-start !important;
    gap: 5px !important;
  }

  .cancel-modal {
    max-width: 90% !important;
    margin: 20px !important;
  }
}
/* Payment Methods UI */
.pay-methods-horizontal {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 6px;
  width: 100%;
}
.radio-card-online {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  padding: 8px 4px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  background: #fff;
  transition: all 0.2s;
  gap: 4px;
}
.radio-card-online:hover {
  border-color: #cbd5e1;
  background: #f8fafc;
}
.radio-card-online.active {
  border-color: #e65a5a;
  background: #fff1f2;
}
.radio-card-online input[type="radio"] {
  display: none;
}
.radio-card-online .icon-pt {
  width: 20px;
  height: 20px;
  object-fit: contain;
  display: block;
}
.radio-card-online span {
  font-size: 11px;
  font-weight: 600;
  line-height: 1.2;
  white-space: nowrap;
}

/* QR Modal UI */
.modal-qr-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(15, 23, 42, 0.7);
  backdrop-filter: blur(8px);
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-qr-container {
  background: #fff;
  width: 400px;
  border-radius: 16px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  overflow: hidden;
  animation: modalSlideUp 0.3s ease;
}

.modal-qr-header {
  background: #f8fafc;
  padding: 16px 24px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 700;
  font-size: 16px;
  color: #1e293b;
}

.modal-qr-body {
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.qr-info-row {
  width: 100%;
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
}

.qr-info-row span {
  color: #64748b;
}

.qr-info-row strong {
  color: #0f172a;
}

.qr-total {
  font-size: 16px;
  margin-top: 4px;
  padding-top: 12px;
  border-top: 1px dashed #e2e8f0;
}
.qr-total strong {
  color: #e11d48;
  font-size: 18px;
}

.qr-image-wrapper {
  margin: 20px 0;
  padding: 12px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.qr-image-large {
  width: 250px;
  height: 250px;
  object-fit: contain;
}

.qr-note {
  font-size: 13px;
  color: #64748b;
  text-align: center;
  line-height: 1.5;
  margin: 0;
}

.modal-qr-footer {
  padding: 16px 24px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
.btn-close-vnpay {
  background: none;
  border: none;
  font-size: 24px;
  color: #64748b;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

.btn-close-vnpay:hover {
  color: #ef4444;
}

.btn-cancel {
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel:hover {
  background: #f1f5f9;
  color: #1e293b;
}

.btn-primary {
  position: relative;
  overflow: hidden;
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  border: none;
  background: #db3333;
  color: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.btn-primary:hover {
  filter: brightness(1.08);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.btn-primary:disabled {
  background: #94a3b8;
  cursor: not-allowed;
  transform: none;
}

/* HIỆU ỨNG ÁNH SÁNG QUÉT */
.btn-light-sweep {
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(
    to right,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.4) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  transform: skewX(-25deg);
  animation: sweep 3s infinite;
  pointer-events: none;
}

@keyframes sweep {
  0% {
    left: -100%;
  }
  20% {
    left: 200%;
  }
  100% {
    left: 200%;
  }
}
.status-done,
.status-pending,
.status-cancelled {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 99px;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.5px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}
.status-done {
  background: rgba(235, 87, 87, 0.12);
  color: #b72d2d;
  border: 1px solid rgba(16, 185, 129, 0.2);
}
.status-pending {
  background: rgba(202, 55, 55, 0.12);
  color: #ff0000;
  border: 1px solid rgba(245, 158, 11, 0.2);
}
.status-cancelled {
  background: rgba(107, 114, 128, 0.12);
  color: #4b5563;
  border: 1px solid rgba(107, 114, 128, 0.2);
}
</style>
