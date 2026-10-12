<script setup>
import { ref } from "vue";
import { computed } from "vue";
import { onMounted } from "vue";
import { watch } from "vue";
import { onBeforeUnmount } from "vue";
import { onActivated } from "vue";
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";
import { getAlldatbanByNgay1 } from "@/api/admin/datban/datBanApi";
import { getAllkhuvuc } from "@/api/admin/khuvuc/khuVucApi";
import { getDanhSachBan } from "@/api/admin/checkin/checkinapi";
import { getChiTietCheckIn } from "@/api/admin/checkin/checkinapi";
import { doiTrangThaiCheckIn } from "@/api/admin/checkin/checkinapi";
import { getHoaDonDangDungTheoBan } from "@/api/admin/checkin/checkinapi";
import { getChiTietCheckInTheoHoaDon } from "@/api/admin/checkin/checkinapi";
import { taoHoaDonMoiTrenBan } from "@/api/admin/checkin/checkinapi";
import { xacThucMaDatBan } from "@/api/admin/checkin/checkinapi";
import { exportPdfGoiMon } from "@/api/admin/checkin/checkinapi";
import { getThamSoInt } from "@/api/admin/thamsodong/thamSoDongApi";
import { getAllKhachHang } from "@/api/admin/khachhang/khachhangApi";
import { timKiemKhachHang } from "@/api/admin/khachhang/khachhangApi";
import { getAllKhachChoBan } from "@/api/admin/khachchoban/khachChoBanApi.js";
import { dangKyChoBan } from "@/api/admin/khachchoban/khachChoBanApi.js";
import { huyKhachChoBan } from "@/api/admin/khachchoban/khachChoBanApi.js";
import { xepBanKhachCho } from "@/api/admin/khachchoban/khachChoBanApi.js";
import iconWifi from "@/assets/icons/wifi.png";
import iconBan from "@/assets/icons/ban.png";
import ChonBanCheckIn from "./ChonBanCheckIn.vue";
import CheckInChiTiet from "./CheckInChiTiet.vue";
import KhachChoCheckInAll from "./KhachChoCheckInAll.vue";
import { useRouter, useRoute } from "vue-router";
import { useUtils } from "@/utils/helper";
import { connectWebSocket, disconnectWebSocket } from "@/utils/websocket";
import flatpickr from "flatpickr";
import { Vietnamese } from "flatpickr/dist/l10n/vn.js";
import "flatpickr/dist/flatpickr.min.css";
import { nextTick } from "vue";
const router = useRouter();
const route = useRoute();
const { notify, formatCurrency } = useUtils();

/////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////

/* ================== MODAL CHỌN KHÁCH CHỜ SẴN (ĐẶT BÀN NHANH) - FAKE DATA ================== */
/* ================== MODAL CHỌN KHÁCH CHỜ SẴN (ĐẶT BÀN NHANH) ================== */
const showChonKhachChoDatNhanh = ref(false);
const khachChoDatNhanhKeyword = ref("");
const khachChoDatNhanhTrangThai = ref(null);
const khachChoDaChonDatNhanh = ref(null);

const dsKhachChoDatNhanhLoc = computed(() => {
  const keyword = khachChoDatNhanhKeyword.value
    .trim()
    .replace(/\s+/g, " ")
    .toLowerCase();

  return danhSachKhachChoBanThat.value.filter((item) => {
    // Chỉ cho chọn những khách đang ở trạng thái "Đang chờ"
    if (item.trangThai !== "DANG_CHO") return false;

    const ten = (tenHienThiKhachChoBan(item) || "").trim().toLowerCase();
    const sdt = (item.soDienThoai || "").trim();

    const matchKeyword = ten.includes(keyword) || sdt.includes(keyword);

    let matchTrangThai = true;
    if (khachChoDatNhanhTrangThai.value !== null) {
      matchTrangThai = item.trangThai === khachChoDatNhanhTrangThai.value;
    }

    return matchKeyword && matchTrangThai;
  });
});

const moModalChonKhachChoDatNhanh = async () => {
  khachChoDatNhanhKeyword.value = "";
  khachChoDatNhanhTrangThai.value = null;
  showChonKhachChoDatNhanh.value = true;
  trangKhachChoDatNhanh.value = 1;
  await loadDanhSachKhachChoBan();
};

const dongModalChonKhachChoDatNhanh = () => {
  showChonKhachChoDatNhanh.value = false;
};

const chonKhachChoDatNhanh = (item) => {
  khachChoDaChonDatNhanh.value = item;
  soKhachDatNhanh.value = item.soLuongKhach || soKhachDatNhanh.value;
  showChonKhachChoDatNhanh.value = false;
};

const boChonKhachChoDatNhanh = () => {
  khachChoDaChonDatNhanh.value = null;
};

const trangKhachChoDatNhanh = ref(1);
const pageSizeKhachChoDatNhanh = 5;

const dsKhachChoDatNhanhPhanTrang = computed(() => {
  const start = (trangKhachChoDatNhanh.value - 1) * pageSizeKhachChoDatNhanh;
  return dsKhachChoDatNhanhLoc.value.slice(
    start,
    start + pageSizeKhachChoDatNhanh,
  );
});

const tongSoTrangKhachChoDatNhanh = computed(
  () =>
    Math.ceil(dsKhachChoDatNhanhLoc.value.length / pageSizeKhachChoDatNhanh) ||
    1,
);

const pageListKhachChoDatNhanh = computed(() => {
  const total = tongSoTrangKhachChoDatNhanh.value;
  if (total <= 5) return Array.from({ length: total }, (_, i) => i + 1);
  const pages = [1, 2, 3];
  if (total > 4) pages.push("…");
  pages.push(total);
  return pages;
});

// Reset về trang 1 khi đổi filter/tìm kiếm
watch([khachChoDatNhanhKeyword, khachChoDatNhanhTrangThai], () => {
  trangKhachChoDatNhanh.value = 1;
});

/////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////

/* ================== DATA THẬT CHO MODAL KHÁCH CHỜ BÀN ================== */
const danhSachKhachChoBanThat = ref([]);
const loadingKhachChoBan = ref(false);

const loadDanhSachKhachChoBan = async () => {
  loadingKhachChoBan.value = true;
  try {
    const res = await getAllKhachChoBan();
    danhSachKhachChoBanThat.value = Array.isArray(res.data) ? res.data : [];
  } catch (e) {
    console.error("Lỗi load danh sách khách chờ bàn", e);
    notify.error("Không tải được danh sách khách chờ bàn");
  } finally {
    loadingKhachChoBan.value = false;
  }
};

///////////////////////////// XÁC NHẬN ĐÃ XẾP BÀN /////////////////
const loadingXepBanKhachCho = ref(false);

const xuLyXepBanKhachCho = async (item) => {
  if (item.trangThai !== "DANG_CHO") return;

  const isConfirm = await notify.confirm(
    `Xác nhận đã xếp bàn cho "${tenHienThiKhachChoBan(item)}" ?`,
  );
  if (!isConfirm) return;

  try {
    loadingXepBanKhachCho.value = true;
    await xepBanKhachCho(item.id);
    notify.success("Xác nhận thành công");
    await loadDanhSachKhachChoBan();
  } catch (e) {
    notify.error(e?.response?.data?.message || "Xếp bàn thất bại");
  } finally {
    loadingXepBanKhachCho.value = false;
  }
};

//////////////////////// HỦY KHÁCH CHỜ //////////////////////////
const loadingHuyKhachCho = ref(false);

const xuLyHuyKhachCho = async (item) => {
  if (item.trangThai !== "DANG_CHO") return;

  const isConfirm = await notify.confirm(
    `Bạn có muốn hủy khách chờ "${tenHienThiKhachChoBan(item)}" không?`,
  );
  if (!isConfirm) return;

  try {
    loadingHuyKhachCho.value = true;
    await huyKhachChoBan(item.id);
    notify.success("Hủy khách chờ thành công");
    await loadDanhSachKhachChoBan();
  } catch (e) {
    notify.error(e?.response?.data?.message || "Hủy khách chờ thất bại");
  } finally {
    loadingHuyKhachCho.value = false;
  }
};

/* ================== MODAL KHÁCH CHỜ BÀN (DẠNG BẢNG) ================== */
const showKhachChoBan = ref(false);
const khachChoBanKeyword = ref("");
const khachChoBanTrangThai = ref(null);

const tenHienThiKhachChoBan = (item) => {
  return item.idKhachHang ? item.tenKhachHang : item.tenKhach;
};

const getTrangThaiKhachChoBanUI = (trangThai) => {
  switch (trangThai) {
    case "DANG_CHO":
      return { text: "Đang chờ", class: "status-pending-dm" };
    case "DA_XEP_BAN":
      return { text: "Đã xếp bàn", class: "status-confirmed-dm" };
    case "HOAN_THANH":
      return { text: "Hoàn thành", class: "status-success" };
    case "DA_HUY":
      return { text: "Đã hủy", class: "status-error" };
    default:
      return { text: trangThai, class: "status-default-dm" };
  }
};

const trangThaiOptionsKhachCho = [
  { label: "Tất cả trạng thái", value: null },
  { label: "Đang chờ", value: "DANG_CHO" },
  { label: "Đã xếp bàn", value: "DA_XEP_BAN" },
  { label: "Hoàn thành", value: "HOAN_THANH" },
  { label: "Đã hủy", value: "DA_HUY" },
];

const soKhachDangCho = computed(() => {
  return danhSachKhachChoBanThat.value.filter(
    (item) => item.trangThai === "DANG_CHO",
  ).length;
});

const danhSachKhachChoBanLoc = computed(() => {
  const keyword = khachChoBanKeyword.value
    .trim()
    .replace(/\s+/g, " ")
    .toLowerCase();

  return danhSachKhachChoBanThat.value.filter((item) => {
    const ten = tenHienThiKhachChoBan(item) || "";
    const tenLower = ten.trim().replace(/\s+/g, " ").toLowerCase();
    const sdt = (item.soDienThoai || "").trim().replace(/\s+/g, " ");

    const matchKeyword = tenLower.includes(keyword) || sdt.includes(keyword);

    let matchTrangThai = true;
    const filterVal = khachChoBanTrangThai.value;
    if (filterVal !== null && filterVal !== undefined) {
      matchTrangThai = item.trangThai === filterVal;
    }

    return matchKeyword && matchTrangThai;
  });
});

const moModalKhachChoBan = async () => {
  showKhachChoBan.value = true;
  trangKhachChoBan.value = 1;
  await loadDanhSachKhachChoBan();
};

function dongModalKhachChoBan() {
  showKhachChoBan.value = false;
  khachChoBanKeyword.value = "";
  khachChoBanTrangThai.value = null;
}

/* ================== MODAL THÊM KHÁCH CHỜ BÀN (MỚI) ================== */
const formatLocalDateTime = (date) => {
  if (!date) return null;
  const pad = (n) => n.toString().padStart(2, "0");
  const year = date.getFullYear();
  const month = pad(date.getMonth() + 1);
  const day = pad(date.getDate());
  const hours = pad(date.getHours());
  const minutes = pad(date.getMinutes());
  return `${year}-${month}-${day}T${hours}:${minutes}:00`;
};
const thoiGianDatChoRef = ref(null);
let thoiGianDatChoPicker = null;
const showAddKhachCho = ref(false);
const loadingAddKhachCho = ref(false);
const errorAddKhachCho = ref("");

const formKhachCho = ref({
  tenKhachHang: "",
  soDienThoai: "",
  soLuongKhach: 0,
  thoiGianDangKy: "",
});

const moModalThemKhachCho = () => {
  formKhachCho.value = {
    tenKhachHang: "",
    soDienThoai: "",
    soLuongKhach: 0,
    thoiGianDangKy: "",
  };
  errorAddKhachCho.value = "";
  showKhachChoBan.value = false;
  showAddKhachCho.value = true;

  nextTick(() => {
    if (thoiGianDatChoPicker) {
      thoiGianDatChoPicker.destroy();
      thoiGianDatChoPicker = null;
    }
    thoiGianDatChoPicker = flatpickr(thoiGianDatChoRef.value, {
      enableTime: true,
      dateFormat: "d/m/Y H:i",
      time_24hr: true,
      minuteIncrement: 5,
      locale: Vietnamese,
      minDate: new Date(),
      maxDate: new Date().setHours(23, 59, 59, 999),
      onChange: (selectedDates) => {
        formKhachCho.value.thoiGianDangKy = selectedDates[0] || "";
        errorAddKhachCho.value = "";
      },
    });
  });
};

const xacNhanThemKhachCho = async () => {
  errorAddKhachCho.value = "";

  const thieuThongTin =
    !formKhachCho.value.tenKhachHang.trim() ||
    !formKhachCho.value.soDienThoai.trim() ||
    !formKhachCho.value.soLuongKhach ||
    formKhachCho.value.soLuongKhach <= 0;

  if (thieuThongTin) {
    errorAddKhachCho.value = "Vui lòng nhập đầy đủ thông tin khách hợp lệ";
    return;
  }

  if (!formKhachCho.value.thoiGianDangKy) {
    errorAddKhachCho.value = "Vui lòng chọn giờ đăng ký";
    return;
  }

  const isConfirm = await notify.confirm("Bạn có muốn thêm khách chờ không?");
  if (!isConfirm) return;

  try {
    loadingAddKhachCho.value = true;

    await dangKyChoBan({
      tenKhach: formKhachCho.value.tenKhachHang.trim(),
      soDienThoai: formKhachCho.value.soDienThoai.trim(),
      soLuongKhach: Number(formKhachCho.value.soLuongKhach),
      thoiGianDangKy: formatLocalDateTime(formKhachCho.value.thoiGianDangKy),
    });

    notify.success("Thêm khách chờ thành công");
    dongModalThemKhachCho();
    await loadKhachCho();
    await loadDanhSachKhachChoBan();
    showKhachChoBan.value = true;
  } catch (e) {
    errorAddKhachCho.value =
      e?.response?.data?.message || "Thêm khách chờ thất bại";
  } finally {
    loadingAddKhachCho.value = false;
  }
};
const dongModalThemKhachCho = () => {
  showAddKhachCho.value = false;
  errorAddKhachCho.value = "";
  if (thoiGianDatChoPicker) {
    thoiGianDatChoPicker.destroy();
    thoiGianDatChoPicker = null;
  }
};

const chonKhachHang = () => {
  // TODO: nối với modal/API tìm khách hàng có sẵn để tự điền tên + SĐT
  notify.info("Chức năng chọn khách hàng đang được phát triển");
};

/* ================== MODAL CHỌN KHÁCH HÀNG (cho form thêm khách chờ) ================== */
const hienModalChonKhachHang = ref(false);
const dangTaiKhachHang = ref(false);
const dsKhachHangGoc = ref([]);
const dsKhachHangHienThi = ref([]);
const tuKhoaTimKhachHang = ref("");
const trangKhachHang = ref(1);
const pageSizeKhachHang = ref(5);

const tongSoTrangKhachHang = computed(
  () =>
    Math.ceil(dsKhachHangHienThi.value.length / pageSizeKhachHang.value) || 1,
);

const pageListKhachHang = computed(() => {
  const total = tongSoTrangKhachHang.value;
  if (total <= 5) return Array.from({ length: total }, (_, i) => i + 1);
  return [1, 2, 3, "…", total];
});

const dsKhachHangPhanTrang = computed(() => {
  const start = (trangKhachHang.value - 1) * pageSizeKhachHang.value;
  return dsKhachHangHienThi.value.slice(start, start + pageSizeKhachHang.value);
});

const taiDanhSachKhachHang = async () => {
  dangTaiKhachHang.value = true;
  try {
    const res = await getAllKhachHang();
    dsKhachHangGoc.value = Array.isArray(res.data) ? res.data : [];
    dsKhachHangHienThi.value = dsKhachHangGoc.value;
  } catch (e) {
    notify.error("Không tải được danh sách khách hàng");
  } finally {
    dangTaiKhachHang.value = false;
  }
};

let timeoutTimKiemKh = null;
watch(tuKhoaTimKhachHang, (val) => {
  clearTimeout(timeoutTimKiemKh);
  timeoutTimKiemKh = setTimeout(async () => {
    trangKhachHang.value = 1;
    const kw = val.trim();
    if (!kw) {
      dsKhachHangHienThi.value = dsKhachHangGoc.value;
      return;
    }
    dangTaiKhachHang.value = true;
    try {
      const res = await timKiemKhachHang(kw);
      dsKhachHangHienThi.value = Array.isArray(res.data) ? res.data : [];
    } catch (e) {
      notify.error("Tìm kiếm khách hàng thất bại");
    } finally {
      dangTaiKhachHang.value = false;
    }
  }, 350);
});

const moModalChonKhachHang = async () => {
  hienModalChonKhachHang.value = true;
  tuKhoaTimKhachHang.value = "";
  trangKhachHang.value = 1;
  await taiDanhSachKhachHang();
};

const dongModalChonKhachHang = () => {
  hienModalChonKhachHang.value = false;
};

const chonKhachHangNay = (kh) => {
  formKhachCho.value.tenKhachHang = kh.tenKhachHang || kh.hoTen || "";
  formKhachCho.value.soDienThoai = kh.soDienThoai || "";
  errorAddKhachCho.value = "";
  hienModalChonKhachHang.value = false;
};

/* ================== TAB & TẦNG ================== */
const tabDangChon = ref(1);
const danhSachTang = ref([]);
const tangDangChon = ref(null);

/* ================== STATE ================== */
const danhSachBan = ref([]);
const showCheckIn = ref(false);
const banDangChon = ref(null);
const showChonHoaDon = ref(false);
const dsHoaDonTheoBan = ref([]);
const banCoNhieuHoaDon = ref(null);
const datBanNhanh = ref(false);
const moBanDuPhong = ref(false);
const loadingCheckIn = ref(false);
const soKhachMacDinhDatNhanh = ref(2);

const dsBanDaChonNhanh = ref([]);
const soKhachDatNhanh = ref(2);
const soGheDuToiDaDatNhanh = ref(2);

const showChonBanCheckIn = ref(false);
const itemCheckInDoiBan = ref(null);

const showConfirmDoiBan = ref(false);
const confirmDoiBanItem = ref(null);

const thoiGianGiuBan = ref(15);
const thoiGianDonBan = ref(15);
const thoiGianSuDungBanMacDinh = ref(120);
const datTruocToiThieu = ref(60);

const showKhachChoAll = ref(false);

const showTaoHoaDonMoi = ref(false);
const soKhachHoaDonMoi = ref(1);
const loadingTaoHoaDon = ref(false);

const showNhapMaDatBan = ref(false);
const maDatBanInput = ref("");
const itemDangCheckInKhachCho = ref(null);

const errorSoKhachHoaDonMoi = ref("");
const errorMaDatBan = ref("");

const danhSachTrangThai = [
  { value: 1, label: "Trống" },
  { value: 2, label: "Đã đặt" },
  { value: 3, label: "Đang sử dụng" },
  { value: 4, label: "Đang dọn" },
];

const laBanLon = (ban) => ban && ban.sucChuaToiDa >= 12;

const tongSucChuaBanDaChon = computed(() => {
  return dsBanDaChonNhanh.value.reduce(
    (sum, ban) => sum + getSoGheTrong(ban),
    0,
  );
});

const getSoGheTrong = (ban) => {
  const tt = ban.trangThaiHienThi ?? ban.trangThaiBan ?? ban.trangThai;

  // Bàn đang dọn thì không còn ghế trống
  if (Number(tt) === 4) return 0;

  // Nếu là bàn Trống (1) hoặc Đã đặt (2) thì khách chưa ngồi ăn -> Số ghế trống = Sức chứa
  if (Number(tt) === 1 || Number(tt) === 2) {
    return Number(ban.sucChuaToiDa || 0);
  }
  // Nếu là bàn Đang sử dụng (3) thì mới tính toán thực tế
  const sucChua = Number(ban?.sucChuaToiDa || 0);
  const soNguoiDangNgoi = Number(ban?.soNguoiDangDung || ban?.soKhach || 0);
  return Math.max(sucChua - soNguoiDangNgoi, 0);
};

const getSoGheDaDung = (ban) => {
  const sucChua = Number(ban?.sucChuaToiDa || 0);
  const tt = ban?.trangThaiHienThi ?? ban?.trangThaiBan ?? ban?.trangThai;

  if (Number(tt) === 1 || Number(tt) === 2) return 0;

  const soNguoiDangNgoi = Number(ban?.soNguoiDangDung || ban?.soKhach || 0);
  return Math.min(Math.max(soNguoiDangNgoi, 0), sucChua);
};

const conGheTrong = (ban) => getSoGheTrong(ban) > 0;

const formatDateTime = (value) => {
  if (!value) return "";

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;

  return date.toLocaleString("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
};

const formatMsgCheckIn = (msg) => {
  const regex = /Giờ đặt bàn:\s*([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2})/;
  const match = msg.match(regex);

  if (!match) return msg;

  return msg.replace(match[0], `Giờ đặt bàn: ${formatDateTime(match[1])}`);
};

const getTrangThaiHoaDonText = (status) => {
  const map = {
    0: "Vừa tạo",
    1: "Chờ cọc",
    2: "Đã cọc",
    3: "Đã xác nhận",
    4: "Chờ xác nhận",
    5: "Khách đến",
    6: "Đang phục vụ",
    8: "Đã thanh toán",
    9: "Hoàn thành",
    "-1": "Đã hủy",
  };
  return map[status] || "Không xác định";
};

const laBanGhep = (ban) => {
  return (
    Array.isArray(ban?.listIdBan) && ban.listIdBan.length > 1 && ban.idHoaDon
  );
};

const nowTick = ref(Date.now());

const getMocThoiGianHoaDon = (hoaDon) => {
  const value =
    hoaDon?.thoiGianTao ||
    hoaDon?.ngayTao ||
    hoaDon?.createdAt ||
    hoaDon?.thoiGianCheckIn ||
    hoaDon?.thoiGianMo ||
    hoaDon?.thoiGianDat;
  const time = value ? new Date(value).getTime() : Number.POSITIVE_INFINITY;
  return Number.isNaN(time) ? Number.POSITIVE_INFINITY : time;
};

const sapXepHoaDonTheoThoiGianMo = (dsHoaDon) => {
  if (!Array.isArray(dsHoaDon)) return [];
  return [...dsHoaDon].sort((a, b) => {
    const diff = getMocThoiGianHoaDon(a) - getMocThoiGianHoaDon(b);
    if (diff !== 0) return diff;
    return (
      Number(a?.idHoaDon || a?.id || 0) - Number(b?.idHoaDon || b?.id || 0)
    );
  });
};

/* ================== LOAD DATA ================== */
async function loadBan() {
  try {
    const res = await getDanhSachBan();
    danhSachBan.value = (res.data || []).map((ban) => ({
      ...ban,
      dsHoaDon: sapXepHoaDonTheoThoiGianMo(ban.dsHoaDon),
    }));
  } catch (e) {
    console.error("Lỗi load danh sách bàn check in", e);
  }
}

async function loadKhuVuc() {
  try {
    const res = await getAllkhuvuc();
    const data = res.data || [];

    danhSachTang.value = data
      .filter((kv) => Number(kv.trangThai) === 1)
      .map((kv) => ({
        id: kv.id,
        ten: kv.tenKhuVuc,
      }));

    if (danhSachTang.value.length > 0 && !tangDangChon.value) {
      tangDangChon.value = danhSachTang.value[0].id;
    }
  } catch (e) {
    console.error("Lỗi load khu vực", e);
  }
}

const isSameDate = (dateStr, selectedDateStr) => {
  if (!dateStr || !selectedDateStr) return false;

  const ngayDat = String(dateStr).split("T")[0];
  return ngayDat === selectedDateStr;
};

const dsIdBanDaDatTheoNgay = computed(() => {
  const result = new Set();

  danhSachKhachCho.value.forEach((item) => {
    if (!isSameDate(item.thoiGianDat, selectedDate.value)) return;

    (item.listIdBan || []).forEach((idBan) => {
      result.add(Number(idBan));
    });
  });

  return result;
});

const getTrangThaiHienThi = (ban) => {
  if (ban.trangThaiBan !== undefined && ban.trangThaiBan !== null) {
    return Number(ban.trangThaiBan);
  }
  return 1;
};

function capNhatTrangThaiBanTrenGrid(idBanAn, trangThaiMoi) {
  danhSachBan.value = danhSachBan.value.map((item) => {
    const itemId = item.idBanAn || item.idBan;
    if (Number(itemId) !== Number(idBanAn)) return item;

    return {
      ...item,
      trangThaiBan: trangThaiMoi,
      trangThai: trangThaiMoi,
    };
  });
}

/* ================== GRID LOGIC ================== */
const danhSachBanTheoTang = computed(() => {
  if (!tangDangChon.value) return [];
  let list = danhSachBan.value
    .filter(
      (b) =>
        Number(b.khuVucId || b.khuVuc?.id) === Number(tangDangChon.value) &&
        getTrangThaiHienThi(b) !== 0,
    )
    .map((b) => ({
      ...b,
      trangThaiHienThi: getTrangThaiHienThi(b),
    }));
  return list;
});

const SO_COT = 6;
const TONG_SLOT = ref(36);
const slots = computed(() =>
  Array.from({ length: TONG_SLOT.value }, (_, i) => ({ id: i + 1, index: i })),
);
const viTriBan = ref({});

const isSlotHopLe = (index, loaiBan) => {
  const col = index % SO_COT;
  if (loaiBan === "OFFLINE") return col === 0 || col === 1;
  if (loaiBan === "ONLINE") return col === 2 || col === 3;
  if (loaiBan === "DU_PHONG") return col === 4 || col === 5;
  return false;
};

const khoiTaoSlot = () => {
  const map = {};
  const banCuaTang = [...danhSachBanTheoTang.value];

  // Phân nhóm bàn theo loại bàn
  const banOffline = banCuaTang.filter((b) => b.loaiBan === "OFFLINE");
  const banOnline = banCuaTang.filter((b) => b.loaiBan === "ONLINE");
  const banDuPhong = banCuaTang.filter((b) => b.loaiBan === "DU_PHONG");

  const xepNhomBan = (danhSachNhom, loaiBan) => {
    const danhSachSapXep = [...danhSachNhom].sort((a, b) =>
      laBanLon(a) ? -1 : 1,
    );

    // Bước 1: Ưu tiên đặt các bàn ĐÃ CÓ vị trí hợp lệ
    danhSachSapXep.forEach((b) => {
      if (
        b.viTriIndex === undefined &&
        b.viTriBan !== null &&
        b.viTriBan !== undefined
      ) {
        b.viTriIndex = b.viTriBan;
      }
      if (b.viTriIndex !== undefined) {
        let canPlace = isSlotHopLe(b.viTriIndex, loaiBan);

        if (canPlace) {
          if (laBanLon(b)) {
            const slot1 = b.viTriIndex;
            const slot2 = b.viTriIndex + SO_COT;
            const slot3 = b.viTriIndex + SO_COT * 2;

            if (
              map[slot1] ||
              map[slot2] ||
              map[slot3] ||
              slot3 >= TONG_SLOT.value ||
              !isSlotHopLe(slot2, loaiBan) ||
              !isSlotHopLe(slot3, loaiBan)
            ) {
              canPlace = false;
            }
          } else {
            if (map[b.viTriIndex]) {
              canPlace = false;
            }
          }
        }

        if (canPlace) {
          map[b.viTriIndex] = b;
          if (laBanLon(b)) {
            map[b.viTriIndex + SO_COT] =
              "BLOCKED_" + (b.idBanAn || b.idBan || b.id);
            map[b.viTriIndex + SO_COT * 2] =
              "BLOCKED_" + (b.idBanAn || b.idBan || b.id);
          }
        } else {
          b.viTriIndex = undefined;
        }
      }
    });

    // Bước 2: Xếp tự động các bàn chưa có vị trí
    const banChuaCoViTri = danhSachSapXep.filter(
      (b) => b.viTriIndex === undefined,
    );
    banChuaCoViTri.sort((a, b) => (laBanLon(a) ? 1 : -1));

    let currentSlot = 0;
    banChuaCoViTri.forEach((b) => {
      while (map[currentSlot] || !isSlotHopLe(currentSlot, loaiBan)) {
        currentSlot++;
      }

      if (laBanLon(b)) {
        while (
          map[currentSlot] ||
          !isSlotHopLe(currentSlot, loaiBan) ||
          map[currentSlot + SO_COT] ||
          !isSlotHopLe(currentSlot + SO_COT, loaiBan) ||
          map[currentSlot + SO_COT * 2] ||
          !isSlotHopLe(currentSlot + SO_COT * 2, loaiBan)
        ) {
          currentSlot++;
        }
      }

      map[currentSlot] = b;
      b.viTriIndex = currentSlot;

      if (laBanLon(b)) {
        map[currentSlot + SO_COT] = "BLOCKED_" + (b.idBanAn || b.idBan || b.id);
        map[currentSlot + SO_COT * 2] =
          "BLOCKED_" + (b.idBanAn || b.idBan || b.id);
      }
      currentSlot++;
    });
  };

  xepNhomBan(banOffline, "OFFLINE");
  xepNhomBan(banOnline, "ONLINE");
  xepNhomBan(banDuPhong, "DU_PHONG");

  let maxSlot = 18;
  Object.keys(map).forEach((key) => {
    const idx = parseInt(key);
    const item = map[idx];
    let endIdx = idx;
    if (item && typeof item === "object" && laBanLon(item)) {
      endIdx = idx + SO_COT * 2;
    }
    if (endIdx >= maxSlot) {
      maxSlot = endIdx + 1;
    }
  });
  maxSlot = Math.ceil(maxSlot / SO_COT) * SO_COT;
  TONG_SLOT.value = maxSlot;

  viTriBan.value = map;
};

const gridRenderItems = computed(() => {
  return slots.value.map((slot) => {
    return {
      id: slot.id,
      ban:
        viTriBan.value[slot.index] &&
        typeof viTriBan.value[slot.index] === "object"
          ? viTriBan.value[slot.index]
          : null,
      blocked: typeof viTriBan.value[slot.index] === "string",
    };
  });
});

watch(
  danhSachBanTheoTang,
  () => {
    khoiTaoSlot();
  },
  { deep: true, immediate: true },
);

const coBanOfflineKhaDung = () => {
  return danhSachBanTheoTang.value.some((b) => {
    if (b.loaiBan !== "OFFLINE") return false;
    if (Number(b.trangThaiHienThi) !== 1) return false;
    if (datBanNhanh.value) {
      return laBanHopLeDatNhanh(b);
    }
    return true;
  });
};

/* ================== ACTIONS ================== */
/* ================== ACTIONS ================== */
async function clickBan(ban) {
  if (ban.loaiBan === "DU_PHONG" && !moBanDuPhong.value) {
    notify.error(
      "Bàn dự phòng đang bị khóa. Vui lòng nhấn 'Mở bàn dự phòng' để sử dụng!",
    );
    return;
  }

  const tt = ban.trangThaiHienThi ?? ban.trangThaiBan ?? ban.trangThai;

  if (ban.loaiBan === "ONLINE") {
    const idBan = Number(ban.idBanAn || ban.idBan);
    const daChon = dsBanDaChonNhanh.value.some(
      (b) => Number(b.idBanAn || b.idBan) === idBan,
    );
    if (!daChon && Number(tt) === 1 && coBanOfflineKhaDung()) {
      notify.warning(
        "Ưu tiên chọn bàn OFFLINE (chỉ dùng bàn ONLINE khi hết bàn OFFLINE khả dụng)!",
      );
      return;
    }
  }

  if (Number(tt) === 4) {
    notify.warning("Bàn đang dọn dẹp, không được đặt!");
    return;
  }

  if (datBanNhanh.value && laBanTrongKhungGioDat(ban)) {
    notify.error(
      "Bàn này đang trong thời gian được đặt trước. Không thể check-in nhanh!",
    );
    return;
  }

  if (datBanNhanh.value && !laBanHopLeDatNhanh(ban)) {
    notify.warning(
      "Bàn này không hợp lệ để đặt nhanh (số lượng khách đã đủ hoặc bàn không phù hợp sức chứa)!",
    );
    return;
  }

  // 1. Nếu bàn đang màu Đỏ (Đang dùng) -> Xử lý theo chế độ
  if (Number(tt) === 3) {
    // FIX: Nếu đang check-in nhanh -> cho phép thêm bàn đang dùng vào danh sách
    if (datBanNhanh.value) {
      // Nhưng chỉ cho phép nếu là khách vãng lai (không phải VIP)
      const idBan = Number(ban.idBanAn || ban.idBan);
      const daChon = dsBanDaChonNhanh.value.some(
        (b) => Number(b.idBanAn || b.idBan) === idBan,
      );

      if (daChon) {
        dsBanDaChonNhanh.value = dsBanDaChonNhanh.value.filter(
          (b) => Number(b.idBanAn || b.idBan) !== idBan,
        );
        notify.info("Bỏ chọn bàn " + ban.tenBanAn);
      } else {
        dsBanDaChonNhanh.value.push(ban);
        notify.info("Thêm bàn " + ban.tenBanAn);
      }
      return;
    }

    // Nếu không phải check-in nhanh -> mở chi tiết bàn bình thường
    moCheckIn(ban);
    return;
  }

  // THÊM MỚI: Chặn bàn Đã đặt (Xanh dương) nếu chưa tới giờ, TRỪ khi bàn
  // đang ở trạng thái hợp lệ để check-in sớm (xử lý trong chuaToiGio())
  if (Number(tt) === 2 && ban.idDatBan && !datBanNhanh.value) {
    const khachCho = danhSachKhachCho.value.find(
      (k) =>
        Number(k.id) === Number(ban.idDatBan) ||
        Number(k.idDatBan) === Number(ban.idDatBan),
    );

    if (khachCho) {
      if (chuaToiGio(khachCho)) {
        notify.error(
          "Chưa tới giờ check-in cho khách VIP đặt trước. Bàn đang được giữ, hệ thống khóa mở bàn!",
        );
        return;
      }
      // Nếu đã tới giờ (hoặc đủ điều kiện check-in sớm), tự động mở thẳng
      // check-in cho đúng khách đó
      xuLyCheckInKhachCho(khachCho);
      return;
    }
    
    // Nếu không tìm thấy khách chờ tương ứng (do khách đặt cho ngày tương lai
    // và danh sách khách chờ hiện tại đang lọc theo ngày hôm nay)
    // -> Block cứng, không cho phép mở thành khách vãng lai
    notify.error(
      "Bàn đang được giữ cho phiếu đặt của ngày khác. Hệ thống khóa mở bàn!"
    );
    return;
  }

  // 2. Chốt chặn thời gian dựa trên giờ đặt (DÀNH CHO KHÁCH VÃNG LAI TẠI BÀN TRỐNG CÓ LỊCH TƯƠNG LAI XA)
  if (ban.thoiGianDat && Number(tt) !== 2) {
    const now = new Date();
    const gioDat = new Date(ban.thoiGianDat);
    const diffMinutes = (gioDat - now) / (1000 * 60);

    // Dưới datTruocToiThieu: CHẶN CỨNG
    if (diffMinutes <= datTruocToiThieu.value && diffMinutes >= -120) {
      notify.error(
        `Bàn này có lịch đặt lúc ${formatDateTime(gioDat)}. Vui lòng đổi bàn!`,
      );
      return;
    }
    // Lớn hơn datTruocToiThieu: CẢNH BÁO HỎI Ý KIẾN TRƯỚC KHI MỞ BÀN VÃNG LAI
    else if (diffMinutes > datTruocToiThieu.value) {
      const isConfirm = await notify.confirm(
        `Bàn đang có khách đặt lúc ${formatDateTime(
          gioDat,
        )}. Bạn có chắc chắn muốn mở bàn cho KHÁCH VÃNG LAI?`,
      );
      if (!isConfirm) return;
    }
  }

  // 3. Logic chọn bàn đặt nhanh
  if (datBanNhanh.value) {
    const idBan = Number(ban.idBanAn || ban.idBan);
    const daChon = dsBanDaChonNhanh.value.some(
      (b) => Number(b.idBanAn || b.idBan) === idBan,
    );

    if (daChon) {
      dsBanDaChonNhanh.value = dsBanDaChonNhanh.value.filter(
        (b) => Number(b.idBanAn || b.idBan) !== idBan,
      );
    } else {
      dsBanDaChonNhanh.value.push(ban);
    }
  } else {
    // Chặn mở bàn trống khi chưa bật Đặt bàn nhanh
    if (Number(tt) === 1) {
      notify.warning("Vui lòng bật 'Đặt bàn nhanh' để chọn bàn trống!");
      return;
    }
    // 4. Mở modal mở bàn bình thường
    moCheckIn(ban);
  }
}

async function xacNhanCheckIn(ban) {
  try {
    loadingCheckIn.value = true;

    if (!ban?.idBanAn && !ban?.idBan) {
      notify.error("Thiếu id bàn ăn");
      return;
    }

    const payload = {
      idBanAn: ban.idBanAn || ban.idBan,
      listIdBanAn: (ban.listIdBanAn || ban.listIdBan || []).map(Number),
      idDatBan: datBanNhanh.value ? null : ban.idDatBan || null,
      idNhanVien: 1,
      soKhach: ban.soKhachCheckIn || 1,
      ghiChu: ban.ghiChuDatBan || "Đặt nhanh tại quầy",
      isQuickCheckIn: datBanNhanh.value,
      idKhachChoBan: ban.idKhachChoBan || null,
    };

    const res = await doiTrangThaiCheckIn(payload);
    const data = res?.data || {};

    if (!data.idHoaDon) {
      notify.error("Check-in thất bại");
      return;
    }
    const chiTietRes = await getChiTietCheckInTheoHoaDon(
      data.idBanAn,
      data.idHoaDon,
    );
    const duLieuMoi = chiTietRes?.data || {};

    const resHoaDon = await getHoaDonDangDungTheoBan(data.idBanAn);
    let dsHoaDon = Array.isArray(resHoaDon.data) ? [...resHoaDon.data] : [];
    dsHoaDon = sapXepHoaDonTheoThoiGianMo(
      dsHoaDon.filter((hd) => !!hd.thoiGianMo),
    );

    // 4. Cập nhật dữ liệu hiển thị lên Modal[cite: 5]
    banDangChon.value = {
      ...duLieuMoi,
      trangThaiBan: 3, // Ép về trạng thái Đang sử dụng
      dsHoaDon: dsHoaDon,
    };

    // 5. Cập nhật lại Grid bàn và danh sách khách chờ[cite: 5]
    await loadBan();
    await loadKhachCho();

    // 6. Reset các biến đặt nhanh và mở Modal[cite: 5]
    dsBanDaChonNhanh.value = [];
    soKhachDatNhanh.value = 1;
    datBanNhanh.value = false;
    showCheckIn.value = true;
    khachChoDaChonDatNhanh.value = null;

    console.log(
      ">>>> [DEBUG] Check-in thành công. Dữ liệu gán vào Modal:",
      banDangChon.value,
    );
  } catch (error) {
    console.error(">>>> [DEBUG] Lỗi xác nhận check-in:", error);
    const rawMsg =
      error?.response?.data?.message || error?.message || "Check-in thất bại";

    if (rawMsg.includes("VA_CHAM_KHACH_DAT")) {
      const banThucTe = danhSachBan.value.find(
        (b) =>
          Number(b.idBanAn || b.idBan) === Number(ban.idBanAn || ban.idBan),
      );
      confirmDoiBanItem.value = { item: ban, banThucTe: banThucTe || ban };
      showCheckIn.value = false;
      showConfirmDoiBan.value = true;
      return;
    }

    notify.error(formatMsgCheckIn(rawMsg.replace("VA_CHAM_KHACH_DAT: ", "")));
  } finally {
    loadingCheckIn.value = false;
  }
}
const laBanTrongKhungGioDat = (ban) => {
  if (!ban || !ban.thoiGianDat) return false;

  // Bàn đang phục vụ (DANG_SU_DUNG = 3): luôn cho phép ghép thêm khách nếu còn ghế trống
  // Không áp dụng window check vì đây là khách đang ngồi thật sự, không phải lịch đặt tương lai
  const tt = Number(ban.trangThaiHienThi ?? ban.trangThaiBan ?? ban.trangThai);
  if (tt === 3) return false;

  const now = new Date();
  const gioDat = new Date(ban.thoiGianDat);
  const suDungMs = (thoiGianSuDungBanMacDinh.value || 120) * 60000;
  const donBanMs = (thoiGianDonBan.value || 15) * 60000;
  // Window: [thoiGianDat - THOI_GIAN_SU_DUNG, thoiGianDat + THOI_GIAN_SU_DUNG + THOI_GIAN_DON_BAN]
  const startWindow = new Date(gioDat.getTime() - suDungMs);
  const endWindow = new Date(gioDat.getTime() + suDungMs + donBanMs);

  return now >= startWindow && now <= endWindow;
};
const laBanHopLeDeCheckInSom = (item) => {
  const listIdBan = item?.listIdBan?.length
    ? item.listIdBan
    : item?.idBanAn
    ? [item.idBanAn]
    : item?.listIdBanAn?.length
    ? item.listIdBanAn
    : [];

  if (!listIdBan.length) return false;

  return listIdBan.every((idBan) => {
    const ban = danhSachBan.value.find(
      (b) => Number(b.idBanAn || b.idBan) === Number(idBan),
    );
    if (!ban) return false;

    const tt = ban.trangThaiHienThi ?? ban.trangThaiBan ?? ban.trangThai;
    // Chỉ cho check-in sớm nếu bàn đang Trống (1) hoặc Đã đặt (2, chưa có ai ngồi)
    return Number(tt) === 1 || Number(tt) === 2;
  });
};

const chuaToiGio = (item) => {
  if (!item?.thoiGianDat) return false;

  const now = new Date();
  const gioDat = new Date(item.thoiGianDat);

  // Cho phép check-in sớm dựa vào tham số hệ thống (datTruocToiThieu tính bằng phút)
  const gioDuocPhepCheckIn = new Date(
    gioDat.getTime() - (datTruocToiThieu.value || 0) * 60000,
  );

  // Chỉ cho phép check-in sớm nếu đặt bàn là trong ngày hôm nay
  const today = new Date();
  if (
    today.getFullYear() !== gioDat.getFullYear() ||
    today.getMonth() !== gioDat.getMonth() ||
    today.getDate() !== gioDat.getDate()
  ) {
    return true;
  }

  if (now >= gioDuocPhepCheckIn) return false;

  // Chưa tới mốc giờ giữ bàn, nhưng nếu bàn đang ở trạng thái hợp lệ
  // (Trống/Đã đặt, chưa bị chiếm) thì vẫn cho check-in sớm
  if (laBanHopLeDeCheckInSom(item)) return false;

  return true;
};

const hetThoiGianGiuBan = (item) => {
  if (!item?.thoiGianDat) return false;

  if (daCheckIn(item) || daHuyDatBan(item)) return false;

  if (item.trangThai === "HET_HAN") return true;

  const now = new Date(nowTick.value);
  const gioDat = new Date(item.thoiGianDat);

  if (Number.isNaN(gioDat.getTime())) return false;

  if (now < gioDat) return false;

  let hanGiuBan;

  if (item?.hanGiuBan) {
    hanGiuBan = new Date(item.hanGiuBan);
  } else {
    hanGiuBan = new Date(gioDat);
    hanGiuBan.setMinutes(hanGiuBan.getMinutes() + thoiGianGiuBan.value);
  }

  return now >= hanGiuBan;
};

// cái này quan trọng lắm này
const tinhPhuongAnBanHopLe = (soKhach) => {
  const dsBanTrong = danhSachBanTheoTang.value
    .filter((b) => Number(b.trangThaiHienThi) === 1)
    .map((b) => ({
      ...b,
      id: Number(b.idBanAn || b.idBan),
      sucChua: Number(b.sucChuaToiDa || 0),
    }))
    .filter((b) => b.sucChua > 0);

  const ketQua = [];

  const backtrack = (start, combo) => {
    const tong = combo.reduce((sum, b) => sum + b.sucChua, 0);

    if (tong >= soKhach) {
      ketQua.push({
        ids: combo.map((b) => b.id),
        tong,
        du: tong - soKhach,
        soBan: combo.length,
      });
      return;
    }

    for (let i = start; i < dsBanTrong.length; i++) {
      backtrack(i + 1, [...combo, dsBanTrong[i]]);
    }
  };

  backtrack(0, []);

  if (!ketQua.length) return [];

  const duNhoNhat = Math.min(...ketQua.map((p) => p.du));

  return ketQua.filter((p) => p.du === duNhoNhat);
};

const conPhuongAnDatNhanhPhuHop = (soKhach) => {
  const soGheDuToiDa = Number(soGheDuToiDaDatNhanh.value || 0);
  const dsBanKhaDung = danhSachBanTheoTang.value
    .filter((b) => conGheTrong(b))
    .map((b) => ({
      id: Number(b.idBanAn || b.idBan),
      sucChua: getSoGheTrong(b),
    }))
    .filter((b) => b.id && b.sucChua > 0)
    .sort((a, b) => a.sucChua - b.sucChua);

  const timToHop = (start, soBanDaChon, tongSucChua) => {
    if (tongSucChua >= soKhach) {
      return tongSucChua - soKhach <= soGheDuToiDa;
    }
    if (start >= dsBanKhaDung.length || soBanDaChon >= 3) {
      return false;
    }

    for (let i = start; i < dsBanKhaDung.length; i++) {
      if (
        timToHop(i + 1, soBanDaChon + 1, tongSucChua + dsBanKhaDung[i].sucChua)
      ) {
        return true;
      }
    }

    return false;
  };

  return timToHop(0, 0, 0);
};

const laBanHopLeDatNhanh = (ban) => {
  if (laBanTrongKhungGioDat(ban)) return false;

  const soKhach = Number(soKhachDatNhanh.value || 0);
  if (soKhach <= 0) return false;

  const idBan = Number(ban.idBanAn || ban.idBan);

  const daChon = dsBanDaChonNhanh.value.some(
    (b) => Number(b.idBanAn || b.idBan) === idBan,
  );

  if (daChon) return true;
  if (!conGheTrong(ban)) return false;

  const sucChuaBanMoi = getSoGheTrong(ban);
  const tongDaChon = tongSucChuaBanDaChon.value;
  const tongSau = tongDaChon + sucChuaBanMoi;

  if (tongDaChon >= soKhach) return false;

  if (tongSau >= soKhach) {
    const soGheDu = tongSau - soKhach;
    const soGheDuToiDa = Number(soGheDuToiDaDatNhanh.value || 0);

    if (soGheDu > soGheDuToiDa) {
      // Đếm số bàn còn ghế trống (chưa được chọn)
      const soBanConTrong = danhSachBanTheoTang.value.filter((b) => {
        const bId = Number(b.idBanAn || b.idBan);
        const daChonRoi = dsBanDaChonNhanh.value.some(
          (sel) => Number(sel.idBanAn || sel.idBan) === bId,
        );
        return !daChonRoi && conGheTrong(b);
      }).length;

      // Chỉ bỏ qua SO_GHE_DU_TOI_DA khi còn đúng 1 bàn duy nhất
      if (soBanConTrong > 1) {
        return false;
      }
    }
  }

  return true;
};
const goiYBan = computed(() => {
  const soKhach = Number(soKhachDatNhanh.value || 0);

  if (soKhach === 0) return "";

  if (soKhach <= 4) return "Chọn 1 bàn phù hợp (không cần ghép bàn)";
  if (soKhach === 5) return "Nên chọn bàn 6 (tránh ghép 2 bàn 4)";
  if (soKhach === 6) return "Chọn 1 bàn 6";
  if (soKhach === 7) return "Chọn bàn 8 hoặc ghép 2 bàn 4";
  if (soKhach === 8) return "Chọn 1 bàn 8";
  if (soKhach === 9) return "Chọn bàn 2 bàn 6 hoặc 1 bàn 6 và 1 bàn 4";
  if (soKhach === 10) return "Chọn bàn 2 bàn 6 hoặc 1 bàn 6 và 1 bàn 4";
  if (soKhach === 11) return "Chọn bàn 2 bàn 6 ";
  if (soKhach === 12) return "Chọn bàn 2 bàn 6 hoặc 1 bàn 6 và 1 bàn 8";
  if (soKhach === 13) return "Chọn 1 bàn 6 và 1 bàn 8 hoặc 2 bàn 8";
  if (soKhach === 14) return "Chọn 2 bàn 8 hoặc 1 bàn 6 và 1 bàn 8";
  if (soKhach === 15) return "Chọn 2 bàn 8 hoặc bàn 16";
  if (soKhach === 16) return "Chọn bàn 16 hoặc ghép 2 bàn 8";

  return "Hệ thống sẽ tự gợi ý phương án tối ưu";
});

async function dongModalVaResetBan() {
  const idBanAnDangDong =
    banDangChon.value?.idBanAn || banDangChon.value?.idBan;

  if (
    banDangChon.value?.idBanAn &&
    Number(banDangChon.value?.trangThaiBan) === 1
  ) {
    capNhatTrangThaiBanTrenGrid(banDangChon.value.idBanAn, 1);
  }

  showCheckIn.value = false;
  banDangChon.value = null;

  await loadBan();

  if (banCoNhieuHoaDon.value && idBanAnDangDong) {
    const res = await getHoaDonDangDungTheoBan(idBanAnDangDong);
    const ds = Array.isArray(res.data) ? res.data : [];
    dsHoaDonTheoBan.value = sapXepHoaDonTheoThoiGianMo(
      ds.filter((hd) => !!hd.thoiGianMo),
    );
  }
}

const moCheckIn = async (item) => {
  console.log("item bàn/khách chờ =", item);

  try {
    // 1. RESET trạng thái modal trước khi xử lý
    showChonHoaDon.value = false;
    dsHoaDonTheoBan.value = [];
    banCoNhieuHoaDon.value = null;

    const idBanAn = item.idBanAn || item.idBan;
    if (!idBanAn) {
      notify.error("Không tìm thấy id bàn");
      return;
    }

    const trangThai =
      item.trangThaiHienThi ?? item.trangThaiBan ?? item.trangThai;

    // 3. XỬ LÝ BÀN ĐANG SỬ DỤNG (Màu đỏ - Trạng thái 3)
    if (Number(trangThai) === 3 && !item.isFromKhachCho) {
      const resHoaDon = await getHoaDonDangDungTheoBan(idBanAn);

      let dsHoaDonRaw = Array.isArray(resHoaDon.data)
        ? [...resHoaDon.data]
        : [];

      // Lọc bỏ những phiếu đặt bàn tương lai (chưa check-in, thoiGianMo là null)
      let dsHoaDon = sapXepHoaDonTheoThoiGianMo(
        dsHoaDonRaw.filter((hd) => !!hd.thoiGianMo),
      );

      // FIX: Nếu không tìm được hóa đơn nào có thoiGianMo (backend chưa set),
      // thử lấy hóa đơn theo trạng thái đang hoạt động (5=Khách đến, 6=Đang phục vụ)
      if (dsHoaDon.length === 0 && dsHoaDonRaw.length > 0) {
        dsHoaDon = sapXepHoaDonTheoThoiGianMo(
          dsHoaDonRaw.filter((hd) =>
            [5, 6].includes(Number(hd.trangThaiHoaDon ?? hd.trangThai)),
          ),
        );
        // Nếu vẫn không tìm thấy, lấy tất cả hóa đơn có (không phải tương lai)
        if (dsHoaDon.length === 0) {
          dsHoaDon = sapXepHoaDonTheoThoiGianMo(
            dsHoaDonRaw.filter(
              (hd) =>
                ![0, 1, 2, 3, 4].includes(
                  Number(hd.trangThaiHoaDon ?? hd.trangThai),
                ),
            ),
          );
        }
        // Cuối cùng nếu vẫn rỗng, lấy tất cả
        if (dsHoaDon.length === 0) {
          dsHoaDon = sapXepHoaDonTheoThoiGianMo(dsHoaDonRaw);
        }
      }

      if (dsHoaDon.length >= 1) {
        banCoNhieuHoaDon.value = item;

        // Nếu đang checkin nhanh mà bàn đã có hóa đơn, mở popup tạo hóa đơn mới
        if (datBanNhanh.value) {
          showTaoHoaDonMoi.value = true;
          return;
        }

        // Nếu chỉ có 1 hóa đơn -> Mở thẳng chi tiết
        if (dsHoaDon.length === 1) {
          await chonHoaDonCheckIn(dsHoaDon[0]);
          return;
        }

        // Nếu có nhiều hóa đơn -> Hiện thẳng CheckInChiTiet dạng tab
        banDangChon.value = {
          ...item,
          ...dsHoaDon[0],
          dsHoaDon: dsHoaDon,
        };
        // 7. MỞ MODAL
        showCheckIn.value = true;
        return;
      }
    }

    // 4. LẤY CHI TIẾT VÀ HIỂN THỊ MODAL CHECK-IN
    const res = await getChiTietCheckIn(idBanAn);
    const data = res?.data || {};

    // Nếu item có idDatBan (Bấm từ danh sách khách chờ)
    if (item.idDatBan) {
      banDangChon.value = {
        ...data,
        ...item,
        idBanAn: data.idBan || data.idBanAn || idBanAn,
        trangThaiBan: 2,
        dsMon: [],
        tongTien: 0,
      };
    } else {
      // Nếu là khách vãng lai hoặc thao tác bình thường
      banDangChon.value = {
        ...item,
        ...data,
        idDatBan:
          Number(trangThai) === 2 ? null : data.idDatBan ?? item.idDatBan,
        maDatBan:
          Number(trangThai) === 2 ? null : data.maDatBan ?? item.maDatBan,
        tenBanAn: data.tenBan || data.tenBanAn || item.tenBanAn,
        trangThaiBan: data.trangThaiBan ?? item.trangThaiBan ?? item.trangThai,

        idHoaDon: data.idHoaDon || null,
        maHoaDon: data.maHoaDon || null,
        trangThaiHoaDon: data.trangThaiHoaDon || null,

        tongTienMon: Number(data.tongTienMon || 0),
        tongTien: Number(data.tongTien || 0),
        dsMon: [],
      };
    }

    // Cập nhật lại grid nếu bàn vô tình nhảy về trạng thái trống
    if (Number(banDangChon.value.trangThaiBan) === 1) {
      capNhatTrangThaiBanTrenGrid(idBanAn, 1);
    }

    showCheckIn.value = true; // Mở modal chi tiết bàn[cite: 4]
  } catch (e) {
    console.error("Lỗi lấy chi tiết check in", e);
    notify.error(e?.response?.data?.message || "Không tải được chi tiết bàn");
  }
};

async function chonHoaDonCheckIn(hoaDon) {
  try {
    const idBanAn =
      hoaDon.idBanAn ||
      banCoNhieuHoaDon.value?.idBanAn ||
      banCoNhieuHoaDon.value?.idBan;

    if (!idBanAn || !hoaDon.idHoaDon) {
      notify.error("Thiếu thông tin bàn hoặc hóa đơn");
      return;
    }

    const res = await getChiTietCheckInTheoHoaDon(idBanAn, hoaDon.idHoaDon);
    const data = res?.data || {};

    banDangChon.value = {
      ...banCoNhieuHoaDon.value,
      ...data,
      idBanAn: data.idBanAn || data.idBan || idBanAn,
      maBanAn: data.maBanAn || data.maBan || banCoNhieuHoaDon.value?.maBanAn,
      tenBanAn:
        data.tenBanAn || data.tenBan || banCoNhieuHoaDon.value?.tenBanAn,
      trangThaiBan: data.trangThaiBan ?? 3,

      idHoaDon: data.idHoaDon || hoaDon.idHoaDon,
      maHoaDon: data.maHoaDon || hoaDon.maHoaDon,
      trangThaiHoaDon: data.trangThaiHoaDon || hoaDon.trangThaiHoaDon,
    };

    showChonHoaDon.value = false;
    showCheckIn.value = true;
  } catch (e) {
    console.error("Lỗi chọn hóa đơn check in", e);
    notify.error(e?.response?.data?.message || "Không mở được hóa đơn");
  }
}

const xuLyTaoHoaDonMoi = async () => {
  try {
    loadingTaoHoaDon.value = true;

    const idBan =
      banCoNhieuHoaDon.value.idBanAn || banCoNhieuHoaDon.value.idBan;

    const sucChua = banCoNhieuHoaDon.value?.sucChuaToiDa || 0;

    const soGheTrong = getSoGheTrong(banCoNhieuHoaDon.value);

    errorSoKhachHoaDonMoi.value = "";

    if (soKhachHoaDonMoi.value > soGheTrong) {
      errorSoKhachHoaDonMoi.value = `Bàn này chỉ còn ${soGheTrong} ghế trống`;
      return;
    }

    await taoHoaDonMoiTrenBan({
      idBanAn: idBan,
      soKhach: soKhachHoaDonMoi.value,
      idNhanVien: 1,
      ghiChu: "Khách mới ngồi chung bàn, tách hóa đơn",
    });

    showTaoHoaDonMoi.value = false;
    soKhachHoaDonMoi.value = 1;

    // reload lại danh sách hóa đơn
    await loadBan();
    await reloadDanhSachHoaDonTheoBan();

    const banSauReload = danhSachBan.value.find(
      (b) =>
        Number(b.idBanAn || b.idBan) ===
        Number(banCoNhieuHoaDon.value.idBanAn || banCoNhieuHoaDon.value.idBan),
    );

    if (banSauReload && !conGheTrong(banSauReload)) {
      showChonHoaDon.value = false;
      showTaoHoaDonMoi.value = false;
      banCoNhieuHoaDon.value = null;
      dsHoaDonTheoBan.value = [];
      notify.success("Tạo hóa đơn mới thành công. Bàn đã full chỗ.");
      return;
    }

    notify.success("Tạo hóa đơn mới thành công");
  } catch (e) {
    errorSoKhachHoaDonMoi.value =
      e.response?.data?.message || "Lỗi tạo hóa đơn";
  } finally {
    loadingTaoHoaDon.value = false;
  }
};

async function reloadDanhSachHoaDonTheoBan() {
  try {
    const idBanAn =
      banCoNhieuHoaDon.value?.idBanAn || banCoNhieuHoaDon.value?.idBan;

    if (!idBanAn) return;

    const res = await getHoaDonDangDungTheoBan(idBanAn);
    let rawList = Array.isArray(res.data) ? res.data : [];
    dsHoaDonTheoBan.value = sapXepHoaDonTheoThoiGianMo(
      rawList.filter((hd) => !!hd.thoiGianMo),
    );
  } catch (e) {
    console.error("Lỗi reload danh sách hóa đơn theo bàn", e);
  }
}

const getClassTrangThai = (trangThai) => {
  const map = {
    1: "trong",
    2: "da_dat",
    3: "co_khach",
    4: "dang_don",
  };
  return map[trangThai] || "";
};

const getTrangThaiLabel = (value) => {
  return danhSachTrangThai.find((t) => t.value === value)?.label || "";
};

/* ================== STATE BỔ SUNG ================== */
const danhSachKhachCho = ref([]);
const searchKeyword = ref("");
const selectedDate = ref(new Date().toISOString().substr(0, 10));
const timeFilter = ref("15p");

const statusFilter = ref("ALL");

/* ================== LOAD DATA ĐẶT BÀN ================== */
async function loadKhachCho() {
  try {
    const res = await getAlldatbanByNgay1({
      ngayFrom: selectedDate.value,
      ngayTo: selectedDate.value,
    });
    danhSachKhachCho.value = res.data || [];
  } catch (e) {
    console.error("Lỗi load khách chờ", e);
  }
}

const isInSelectedTimeRange = (thoiGianDat) => {
  if (!thoiGianDat) return false;

  const bookingTime = new Date(thoiGianDat);
  if (Number.isNaN(bookingTime.getTime())) return false;

  const ngayDat = bookingTime.toISOString().slice(0, 10);
  if (ngayDat !== selectedDate.value) return false;

  if (timeFilter.value === "het-ngay") return true;

  const now = new Date();
  const nowDate = now.toISOString().slice(0, 10);

  // nếu không phải hôm nay thì chỉ lọc theo ngày
  if (selectedDate.value !== nowDate) {
    return true;
  }

  let soPhut = 0;
  switch (timeFilter.value) {
    case "15p":
      soPhut = 15;
      break;
    case "30p":
      soPhut = 30;
      break;
    case "1h":
      soPhut = 60;
      break;
    case "3h":
      soPhut = 180;
      break;
    default:
      return true;
  }

  // Lấy mốc giới hạn: hiện tại trừ đi thời gian giữ bàn (để hiện khách đến muộn)
  const mocBatDau = new Date(now.getTime() - thoiGianGiuBan.value * 60000);
  const mocKetThuc = new Date(now.getTime() + soPhut * 60000);

  // Hiển thị nếu giờ đặt nằm trong khoảng (Quá 15p -> Sắp tới X phút)
  return bookingTime >= mocBatDau && bookingTime <= mocKetThuc;
};

const daHuyDatBan = (item) => {
  return item?.trangThai === "DA_HUY";
};

const textNutCheckIn = (item) => {
  if (daHuyDatBan(item)) return "Đã hủy";
  if (hetThoiGianGiuBan(item)) return "Hết thời gian giữ bàn";
  if (daCheckIn(item)) return "Đã check in";
  if (chuaToiGio(item)) return "Chưa tới giờ";
  if (item.trangThai !== "DA_XAC_NHAN") return "Chưa xác nhận đặt bàn";
  return "Check-in";
};

/* ================== LOGIC LỌC ================== */
const danhSachKhachLoc = computed(() => {
  const keyword = searchKeyword.value.trim().replace(/\s+/g, " ").toLowerCase();

  return danhSachKhachCho.value.filter((item) => {
    // Chỉ hiển thị các phiếu chưa check-in
    if (daCheckIn(item)) return false;

    const tenKhachHang = (item.tenKhachHang || "")
      .trim()
      .replace(/\s+/g, " ")
      .toLowerCase();
    const soDienThoai = (item.soDienThoai || "").trim().replace(/\s+/g, " ");
    const maDatBan = (item.maDatBan || "")
      .trim()
      .replace(/\s+/g, " ")
      .toLowerCase();

    const matchKeyword =
      tenKhachHang.includes(keyword) ||
      soDienThoai.includes(keyword) ||
      maDatBan.includes(keyword);

    const matchTime = isInSelectedTimeRange(item.thoiGianDat);

    //  LỌC TRẠNG THÁI
    let matchStatus = true;
    if (statusFilter.value !== "ALL") {
      if (statusFilter.value === "DA_HUY_HET_HAN") {
        matchStatus =
          item.trangThai === "DA_HUY" || item.trangThai === "HET_HAN";
      } else {
        matchStatus = item.trangThai === statusFilter.value;
      }
    }

    return matchKeyword && matchTime && matchStatus;
  });
});
async function xuLyXacNhanMon(dsMon) {
  if (!banDangChon.value) return;

  const currentIdHoaDon = banDangChon.value.idHoaDon;

  banDangChon.value = {
    ...banDangChon.value,
    dsMon: Array.isArray(dsMon) ? dsMon : [],
  };

  try {
    const idBanAn = banDangChon.value.idBanAn || banDangChon.value.idBan;
    if (!idBanAn || !currentIdHoaDon) return;

    const res = await getChiTietCheckInTheoHoaDon(idBanAn, currentIdHoaDon);

    if (banDangChon.value?.idHoaDon !== currentIdHoaDon) return;

    const data = res?.data || {};

    banDangChon.value = {
      ...banDangChon.value,
      ...data,
      idBanAn: data.idBanAn || data.idBan || idBanAn,
      idHoaDon: data.idHoaDon || currentIdHoaDon,
      dsMon: Array.isArray(dsMon) ? dsMon : [],
    };

    await reloadDanhSachHoaDonTheoBan();
  } catch (e) {
    console.error("Lỗi reload tiền sau khi thêm món", e);
  }
}

const daCheckIn = (item) => {
  return (
    !!item.idHoaDon ||
    item.trangThai === "DANG_PHUC_VU" ||
    item.trangThai === "HOAN_THANH"
  );
};

const hienThiBanTheoDatBan = (item) => {
  if (!item.listIdBan?.length) return "Đợi xếp bàn";

  const dsBan = item.listIdBan
    .map((id) => {
      const ban = danhSachBan.value.find(
        (b) => Number(b.idBanAn || b.idBan) === Number(id),
      );
      if (!ban) return null;

      return `Bàn ${ban.tenBanAn}`;
    })
    .filter(Boolean);

  return dsBan.length ? dsBan.join(", ") : "Đợi xếp bàn";
};

function xuLyCheckInKhachCho(item) {
  if (hetThoiGianGiuBan(item)) {
    notify.error("Đặt bàn đã hết thời gian giữ bàn, không thể check in");
    return;
  }

  if (chuaToiGio(item)) {
    notify.error("Chưa tới giờ đặt bàn, không thể check in");
    return;
  }

  if (item.trangThai !== "DA_XAC_NHAN") {
    notify.error("Chỉ có thể check-in khi đặt bàn ở trạng thái Đã xác nhận");
    return;
  }

  itemDangCheckInKhachCho.value = item;
  maDatBanInput.value = "";
  showNhapMaDatBan.value = true;
}

async function xacNhanMaDatBan() {
  errorMaDatBan.value = "";
  if (!maDatBanInput.value.trim()) {
    errorMaDatBan.value = "Vui lòng nhập mã xác nhận đặt bàn";
    return;
  }

  const item = itemDangCheckInKhachCho.value;
  const idBanAn = item?.idBanAn || item?.listIdBan?.[0];
  if (!idBanAn) {
    notify.error("Phiếu đặt này chưa được xếp bàn");
    return;
  }

  try {
    // Gọi API xác thực mã (thay vì xác thực và check-in gộp)
    const res = await xacThucMaDatBan(item.id, maDatBanInput.value);

    if (res.data) {
      // Mã đúng -> Đóng modal nhập mã
      showNhapMaDatBan.value = false;
      notify.success("Xác thực mã thành công, đang tiến hành xếp bàn...");

      // Cấu trúc lại item để tương thích với hàm xacNhanCheckIn
      const checkInItem = {
        ...item,
        idDatBan: item.idDatBan || item.id,
        idBanAn: idBanAn,
        listIdBan: item.listIdBan,
        listIdBanAn: item.listIdBan,
        soKhachCheckIn: item.soNguoiToiDa || item.soKhach || 1,
      };

      // Nhảy luôn vào checkin, bỏ qua giao diện xác nhận (confirm)
      await xacNhanCheckIn(checkInItem);
      await loadDanhSachKhachChoBan();
    } else {
      errorMaDatBan.value = "Mã đặt bàn không chính xác";
    }
  } catch (e) {
    errorMaDatBan.value =
      e?.response?.data?.message ||
      "Mã đặt bàn không chính xác hoặc lỗi hệ thống";
  }
}

function tienHanhCheckInKhachCho(item) {
  console.log("item khách chờ =", item);
  const idBanAn = item?.idBanAn || item?.listIdBan?.[0];
  if (!idBanAn) {
    notify.error("Phiếu đặt này chưa được xếp bàn");
    return;
  }

  const banThucTe = danhSachBan.value.find(
    (b) => Number(b.idBanAn || b.idBan) === Number(idBanAn),
  );

  if (banThucTe && banThucTe.trangThaiHienThi === 3) {
    confirmDoiBanItem.value = { item, banThucTe };
    showConfirmDoiBan.value = true;
    return;
  }

  if (banThucTe && banThucTe.trangThaiHienThi === 4) {
    notify.error(
      "Bàn đang dọn dẹp, vui lòng chờ nhân viên dọn xong hoặc đổi bàn khác!",
    );
    return;
  }

  moCheckIn({
    isFromKhachCho: true,
    idDatBan: item.id,
    idBanAn: item.idBanAn || item.listIdBan?.[0],
    maBanAn: item.maBanAn,
    tenBanAn: item.tenBanAn || item.tenBan,
    khuVucId: item.khuVucId,
    tenKhuVuc: item.tenKhuVuc,
    trangThaiBan: 2,
    maDatBan: item.maDatBan,
    tenKhachHang: item.tenKhachHang,
    soDienThoai: item.soDienThoai,
    thoiGianDat: item.thoiGianDat,
    soNguoiToiDa: item.soNguoiToiDa,
    ghiChuDatBan: item.ghiChu,
    idHoaDon: null,
    maHoaDon: null,
    trangThaiHoaDon: null,
  });
}

function xacNhanDoiBanKhachCho() {
  if (!confirmDoiBanItem.value) return;
  const { item, banThucTe } = confirmDoiBanItem.value;
  showConfirmDoiBan.value = false;

  itemCheckInDoiBan.value = {
    ...item,
    id: item.idDatBan || item.id,
    soNguoiDangDung:
      item.soNguoiToiDa || item.soNguoiDat || item.soKhachCheckIn || 1,
    idBanAn: banThucTe.idBanAn || banThucTe.idBan,
    tenBanAn: banThucTe.tenBanAn,
    khuVuc: {
      id: banThucTe.khuVucId || banThucTe.khuVuc?.id,
      tenKhuVuc: banThucTe.tenKhuVuc || banThucTe.khuVuc?.tenKhuVuc,
    },
  };
  showChonBanCheckIn.value = true;
}

async function xuLyXacNhanDoiBanCheckIn(payload) {
  showChonBanCheckIn.value = false;

  const item = itemCheckInDoiBan.value;
  const banMoi = danhSachBan.value.find(
    (b) => Number(b.idBanAn || b.idBan) === Number(payload.idBanMoi),
  );

  moCheckIn({
    isFromKhachCho: true,
    idDatBan: item.idDatBan || item.id,
    idBanAn: payload.idBanMoi,
    listIdBanAn: payload.listIdBanMoi,
    maBanAn: banMoi?.maBanAn,
    tenBanAn: banMoi?.tenBanAn,
    khuVucId: banMoi?.khuVucId || banMoi?.khuVuc?.id,
    tenKhuVuc: banMoi?.tenKhuVuc || banMoi?.khuVuc?.tenKhuVuc,
    trangThaiBan: 2,
    maDatBan: item.maDatBan,
    tenKhachHang: item.tenKhachHang,
    soDienThoai: item.soDienThoai,
    thoiGianDat: item.thoiGianDat,
    soNguoiToiDa:
      (item.soNguoiToiDa || item.soNguoiDat || item.soKhachCheckIn || 1) +
      (payload.soKhachThem || 0),
    ghiChuDatBan: item.ghiChu || item.ghiChuDatBan,
    idHoaDon: null,
    maHoaDon: null,
    trangThaiHoaDon: null,
  });
}

async function xuLyCapNhatHoaDon(hoaDonMoi) {
  if (!banDangChon.value) return;

  let dsHoaDonMoi = banDangChon.value.dsHoaDon;
  if (dsHoaDonMoi && hoaDonMoi.idHoaDon) {
    dsHoaDonMoi = dsHoaDonMoi.map((hd) => {
      if (Number(hd.idHoaDon) === Number(hoaDonMoi.idHoaDon)) {
        return {
          ...hd,
          ...hoaDonMoi,
        };
      }
      return hd;
    });
  }

  banDangChon.value = {
    ...banDangChon.value,
    ...hoaDonMoi,
    dsHoaDon: dsHoaDonMoi,
  };

  if (hoaDonMoi?.refreshGrid) {
    await loadBan();
    return;
  }

  danhSachBan.value = danhSachBan.value.map((item) => {
    if (Number(item.idBanAn) !== Number(banDangChon.value.idBanAn)) return item;

    return {
      ...item,
      ...hoaDonMoi,
    };
  });
}

async function loadThamSo() {
  try {
    const res = await getThamSoInt("THOI_GIAN_GIU_BAN");
    thoiGianGiuBan.value = Number(res.data || 15);
  } catch (e) {
    console.error("Lỗi load tham số", e);
  }

  try {
    const res2 = await getThamSoInt("THOI_GIAN_SU_DUNG_BAN_MAC_DINH");
    thoiGianSuDungBanMacDinh.value = Number(res2.data || 120);
  } catch (e) {
    console.error("Lỗi load tham số THOI_GIAN_SU_DUNG_BAN_MAC_DINH", e);
  }

  try {
    const res3 = await getThamSoInt("DAT_TRUOC_TOI_THIEU");
    datTruocToiThieu.value = Number(res3.data || 60);
  } catch (e) {
    console.error("Lỗi load tham số DAT_TRUOC_TOI_THIEU", e);
  }

  try {
    const res4 = await getThamSoInt("SO_GHE_DU_TOI_DA");
    soGheDuToiDaDatNhanh.value = Number(res4.data || 1);
    soKhachMacDinhDatNhanh.value = Number(res4.data || 2);
    soKhachDatNhanh.value = soKhachMacDinhDatNhanh.value;
  } catch (e) {
    console.error("Lỗi load tham số SO_GHE_DU_TOI_DA", e);
  }
  try {
    const res5 = await getThamSoInt("THOI_GIAN_DON_BAN");
    thoiGianDonBan.value = Number(res5.data || 15);
  } catch (e) {
    console.error("Lỗi load tham số THOI_GIAN_DON_BAN", e);
  }
}

const getConLaiGiuBan = (item) => {
  if (!item?.thoiGianDat || daCheckIn(item) || daHuyDatBan(item)) return "";
  if (item.trangThai === "HET_HAN") return "Hết thời gian giữ bàn";

  const now = new Date(nowTick.value);
  const gioDat = new Date(item.thoiGianDat);

  // 1. NẾU CHƯA TỚI GIỜ: Đếm ngược tới giờ khách đến (Nếu còn dưới 60p)
  if (now < gioDat) {
    const diffMs = gioDat - now;
    const totalSec = Math.floor(diffMs / 1000);
    const mm = Math.floor(totalSec / 60);
    const ss = totalSec % 60;
    if (mm < 60) return `Khách đến sau ${mm}:${String(ss).padStart(2, "0")}`;
    return "Chưa tới giờ đặt";
  }

  // 2. NẾU ĐÃ QUÁ GIỜ: Đếm ngược thời gian giữ bàn (15 phút)
  let hanGiuBan = item?.hanGiuBan
    ? new Date(item.hanGiuBan)
    : new Date(gioDat.getTime() + thoiGianGiuBan.value * 60000);
  const diffMs = hanGiuBan - now;

  if (diffMs <= 0) return "Hết thời gian giữ bàn";

  const totalSec = Math.floor(diffMs / 1000);
  return `Giữ bàn còn ${Math.floor(totalSec / 60)}:${String(
    totalSec % 60,
  ).padStart(2, "0")}`;
};

const getTrangThaiDatBanUI = (trangThai) => {
  switch (trangThai) {
    case "DA_XAC_NHAN":
      return {
        text: "Đã xác nhận",
        class: "status-confirmed",
      };
    case "CHO_XAC_NHAN":
      return {
        text: "Chờ xác nhận",
        class: "status-pending",
      };
    case "DANG_PHUC_VU":
      return {
        text: "Đã check in",
        class: "status-checkin",
      };
    case "DA_HUY":
      return {
        text: "Đã hủy",
        class: "status-cancel",
      };
    case "HOAN_THANH":
      return {
        text: "Hoàn thành",
        class: "status-success",
      };

    case "HET_HAN":
      return {
        text: "Hết hạn giữ bàn",
        class: "status-cancel",
      };
    default:
      return {
        text: trangThai,
        class: "status-default",
      };
  }
};

function diTrangThanhToan(payload) {
  showCheckIn.value = false;
  banDangChon.value = null;
  banCoNhieuHoaDon.value = null;
  dsHoaDonTheoBan.value = [];
  showChonHoaDon.value = false;

  router.push({
    path: `/admin/thanh-toan/${payload.idHoaDon}`,
    query: {
      idBanAn: payload.idBanAn,
      maHoaDon: payload.maHoaDon,
      tenBanAn: payload.tenBanAn,
      diemTichLuySuDung: payload.diemTichLuySuDung || 0,
      giamTrucTiep: payload.giamTrucTiep || 0,
      lyDoGiamTrucTiep: payload.lyDoGiamTrucTiep || "",
      maGiamGia: payload.maGiamGia || "",
      soTienDuocGiam: payload.soTienDuocGiam || 0,
      tienSauGiam: payload.tienSauGiam || 0,
    },
  });
}

function dongPopupChonHoaDon() {
  showChonHoaDon.value = false;
  dsHoaDonTheoBan.value = [];
  banCoNhieuHoaDon.value = null;
}

watch(datBanNhanh, (value) => {
  if (!value) {
    dsBanDaChonNhanh.value = [];
    soKhachDatNhanh.value = soKhachMacDinhDatNhanh.value;
  }
});

watch(soKhachDatNhanh, (value) => {
  if (!value || value <= 0) {
    dsBanDaChonNhanh.value = [];
  }
});

const checkInChiTietRef = ref(null);

// Thêm 1 biến để check xem có đang mở modal nào không
const dangMoModal = computed(
  () =>
    showCheckIn.value ||
    showChonHoaDon.value ||
    showKhachChoAll.value ||
    showKhachChoBan.value,
);
watch(dangMoModal, (newVal) => {
  if (newVal) {
    document.body.style.overflow = "hidden"; // khóa form
  } else {
    document.body.style.overflow = ""; // mở khóa
  }
});

const handleWebSocketMessage = async (msg) => {
  if (!msg) return;

  if (msg.type === "MON_DA_CHON_UPDATE") {
    if (showCheckIn.value && checkInChiTietRef.value) {
      checkInChiTietRef.value.loadMonDaChon();
    }
    return;
  }

  if (msg.type === "CHI_TIET_HOA_DON") {
    if (showCheckIn.value && checkInChiTietRef.value) {
      checkInChiTietRef.value.capNhatHoaDonTrucTiep(msg);
    }
    return;
  }

  if (msg.type === "TOAST_HOA_DON") {
    if (showCheckIn.value && checkInChiTietRef.value) {
      checkInChiTietRef.value.hienThiToast(msg);
    }
    return;
  }

  if (msg.type === "IN_PDF") {
    try {
      const payload = msg;
      if (payload && payload.idHoaDon) {
        const res = await exportPdfGoiMon(
          payload.idHoaDon,
          payload.chiTietIds || [],
        );
        const url = window.URL.createObjectURL(
          new Blob([res.data], { type: "application/pdf" }),
        );
        window.open(url, "_blank");
      }
    } catch (e) {
      console.error("Lỗi tự động tải PDF", e);
    }
    return;
  }

  await loadBan();
  await loadKhachCho();

  if (showCheckIn.value && banDangChon.value) {
    const idBan = banDangChon.value.idBanAn || banDangChon.value.idBan;
    const banCapNhat = danhSachBan.value.find(
      (b) => Number(b.idBanAn || b.idBan) === Number(idBan),
    );
    if (banCapNhat) {
      banDangChon.value.trangThaiBan = banCapNhat.trangThaiBan;
      banDangChon.value.trangThai = banCapNhat.trangThaiBan;

      const capNhatAnToan = { ...banCapNhat };
      Object.keys(capNhatAnToan).forEach((key) => {
        if (capNhatAnToan[key] == null) delete capNhatAnToan[key];
      });

      delete capNhatAnToan.idHoaDon;
      delete capNhatAnToan.maHoaDon;
      delete capNhatAnToan.trangThaiHoaDon;
      delete capNhatAnToan.idDatBan;
      delete capNhatAnToan.maDatBan;
      delete capNhatAnToan.thoiGianDat;
      delete capNhatAnToan.soNguoiToiDa;
      delete capNhatAnToan.trangThaiDatBan;
      delete capNhatAnToan.ghiChuDatBan;
      delete capNhatAnToan.tenKhachHang;
      delete capNhatAnToan.soDienThoai;
      delete capNhatAnToan.hinhThucDat;
      delete capNhatAnToan.listIdBan;
      delete capNhatAnToan.listTenBan;
      delete capNhatAnToan.soNguoiDangDung;
      delete capNhatAnToan.soGheTrong;

      banDangChon.value = { ...banDangChon.value, ...capNhatAnToan };
    }
  }
};

const handleReloadPage = (e) => {
  if (e.detail === "/admin/check-in") {
    loadBan();
    loadKhachCho();
    showCheckIn.value = false;
    banDangChon.value = null;
    showChonHoaDon.value = false;
    banCoNhieuHoaDon.value = null;
    dsHoaDonTheoBan.value = [];
    showKhachChoAll.value = false;
  }
};

/* ================== INIT ================== */
// ==== THỜI GIAN ĐANG SỬ DỤNG BÀN ====
const currentTime = ref(new Date());
let timerInterval;

const getThoiGianSuDungText = (ban) => {
  const trangThai =
    ban?.trangThaiHienThi ?? ban?.trangThaiBan ?? ban?.trangThai;
  if (Number(trangThai) !== 3) return null;

  const hoaDonDauTien =
    Array.isArray(ban.dsHoaDon) && ban.dsHoaDon.length > 0
      ? sapXepHoaDonTheoThoiGianMo(ban.dsHoaDon)[0]
      : null;
  const thoiGianMo =
    hoaDonDauTien?.thoiGianCheckIn ||
    hoaDonDauTien?.thoiGianMo ||
    hoaDonDauTien?.thoiGianDat ||
    ban.thoiGianCheckIn ||
    ban.thoiGianMo ||
    ban.thoiGianDat;
  if (!thoiGianMo) return null;

  const start = new Date(thoiGianMo).getTime();
  if (Number.isNaN(start)) return null;

  const diff = currentTime.value.getTime() - start;
  if (diff < 0) return "0p 0s";

  const totalSeconds = Math.floor(diff / 1000);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;

  if (hours > 0) return `${hours} giờ ${minutes} phút ${seconds} giây`;
  return `${minutes} phút ${seconds} giây`;
};
// ===================================

onMounted(async () => {
  window.addEventListener("reload-page", handleReloadPage);
  timerInterval = setInterval(() => {
    currentTime.value = new Date();
  }, 1000);
  await loadKhuVuc();
  await Promise.all([loadBan(), loadKhachCho(), loadThamSo()]);

  if (route.query.openBanAn) {
    const banId = Number(route.query.openBanAn);
    const banMatch = danhSachBan.value.find(
      (b) => Number(b.idBanAn || b.idBan) === banId,
    );
    if (banMatch) {
      await moCheckIn(banMatch);
    }
  }
  // Khởi chạy WebSocket Realtime
  connectWebSocket(null, handleWebSocketMessage);
});

onBeforeUnmount(() => {
  if (timerInterval) clearInterval(timerInterval);
  window.removeEventListener("reload-page", handleReloadPage);

  disconnectWebSocket(handleWebSocketMessage);
  if (thoiGianDatChoPicker) {
    thoiGianDatChoPicker.destroy();
    thoiGianDatChoPicker = null;
  }
});

onActivated(async () => {
  await loadBan();
  await loadKhachCho();

  if (route.query.openBanAn) {
    const banId = Number(route.query.openBanAn);
    const banMatch = danhSachBan.value.find(
      (b) => Number(b.idBanAn || b.idBan) === banId,
    );
    if (banMatch) {
      await moCheckIn(banMatch);
    }
  }
});

/* ================== PHÂN TRANG KHÁCH CHỜ BÀN ================== */
const trangKhachChoBan = ref(1);
const pageSizeKhachChoBan = 5;

const danhSachKhachChoBanPhanTrang = computed(() => {
  const start = (trangKhachChoBan.value - 1) * pageSizeKhachChoBan;
  return danhSachKhachChoBanLoc.value.slice(start, start + pageSizeKhachChoBan);
});

const tongSoTrangKhachChoBan = computed(
  () =>
    Math.ceil(danhSachKhachChoBanLoc.value.length / pageSizeKhachChoBan) || 1,
);

const pageListKhachChoBan = computed(() => {
  const total = tongSoTrangKhachChoBan.value;
  if (total <= 5) return Array.from({ length: total }, (_, i) => i + 1);
  const pages = [1, 2, 3];
  if (total > 4) pages.push("…");
  pages.push(total);
  return pages;
});

// Reset về trang 1 khi đổi filter/tìm kiếm hoặc mở lại modal
watch([khachChoBanKeyword, khachChoBanTrangThai], () => {
  trangKhachChoBan.value = 1;
});
</script>

<template>
  <div class="check-in-container">
    <div v-show="!showCheckIn">
      <div class="box mb-3">
        <div class="box-header">
          <h2 class="main-title">
            <span class="icon-wrapper">
              <img :src="iconBan" alt="Check In" />
            </span>
            Check In
          </h2>
          <p>Quản lý bàn và sơ đồ bàn theo khu vực</p>
        </div>
      </div>

      <div v-if="tabDangChon === 1" class="noi-dung">
        <div>
          <div class="khu-tang box-white">
            <div class="list-tang">
              <div
                v-for="t in danhSachTang"
                :key="t.id"
                class="the-tang"
                :class="{ active: Number(tangDangChon) === Number(t.id) }"
                @click="tangDangChon = t.id"
              >
                {{ t.ten }}
              </div>
            </div>

            <div class="dat-nhanh">
              <span>Đặt bàn nhanh</span>
              <div
                class="switch"
                :class="{ active: datBanNhanh }"
                @click="datBanNhanh = !datBanNhanh"
              >
                <div class="dot"></div>
              </div>
            </div>
          </div>

          <!-- CHÚ THÍCH + NÚT MỞ BÀN DỰ PHÒNG -->
          <div class="khu-chu-thich">
            <div><span class="dot do"></span> Có khách</div>
            <div><span class="dot xanh-la"></span> Trống</div>
            <div><span class="dot vang"></span> Đang dọn</div>
            <div><span class="dot xanh-duong"></span> Đã đặt</div>

            <!-- NÚT MỚI: Khách chờ bàn -->
            <div class="btn-khach-cho-ban-wrap">
              <button class="btn-khach-cho-ban" @click="moModalKhachChoBan">
                <i class="ri-user-follow-line"></i>
                Khách chờ bàn
              </button>
              <span v-if="soKhachDangCho > 0" class="badge-khach-cho">{{
                soKhachDangCho
              }}</span>
            </div>
            <!-- Nút mở bàn dự phòng -->
            <button
              class="btn-mo-du-phong"
              :class="{ active: moBanDuPhong }"
              @click="moBanDuPhong = !moBanDuPhong"
            >
              <i
                :class="
                  moBanDuPhong ? 'fa-solid fa-lock-open' : 'fa-solid fa-lock'
                "
              ></i>
              {{ moBanDuPhong ? "Khóa bàn dự phòng" : "Mở bàn dự phòng" }}
            </button>
          </div>

          <div class="khu-so-do">
            <div class="grid-ban">
              <template
                v-for="item in gridRenderItems"
                :key="'slot-' + item.id"
              >
                <div
                  v-if="item.ban"
                  :class="[
                    'table-card',
                    item.ban.loaiBan === 'DU_PHONG' && !moBanDuPhong
                      ? 'du-phong'
                      : getClassTrangThai(item.ban.trangThaiHienThi),
                    {
                      'large-vertical': laBanLon(item.ban),
                      'ban-ghep': laBanGhep(item.ban),
                      selected: dsBanDaChonNhanh.some(
                        (b) =>
                          Number(b.idBanAn || b.idBan) ===
                          Number(item.ban.idBanAn || item.ban.idBan),
                      ),
                      'not-available':
                        (item.ban.loaiBan === 'DU_PHONG' && !moBanDuPhong) ||
                        (item.ban.loaiBan === 'ONLINE' &&
                          item.ban.trangThaiHienThi === 1 &&
                          coBanOfflineKhaDung()) ||
                        (datBanNhanh && !laBanHopLeDatNhanh(item.ban)) ||
                        (!datBanNhanh && item.ban.trangThaiHienThi === 1),
                    },
                  ]"
                  @click="clickBan(item.ban)"
                >
                  <img
                    v-if="item.ban.hinhThucDat === 1"
                    :src="iconWifi"
                    class="wifi-ban"
                  />

                  <!-- Overlay khóa bàn dự phòng -->
                  <div
                    v-if="item.ban.loaiBan === 'DU_PHONG' && !moBanDuPhong"
                    class="du-phong-khoa-overlay"
                  >
                    <i class="fa-solid fa-lock"></i>
                    <span>DỰ PHÒNG</span>
                  </div>

                  <!-- Overlay cảnh báo ưu tiên offline -->
                  <div
                    v-else-if="
                      item.ban.loaiBan === 'ONLINE' &&
                      item.ban.trangThaiHienThi === 1 &&
                      coBanOfflineKhaDung()
                    "
                    class="invalid-overlay"
                  >
                    <span>ƯU TIÊN ONLINE</span>
                  </div>

                  <div v-if="laBanGhep(item.ban)" class="icon-ban-ghep">
                    <i class="ri-links-line"></i>
                  </div>

                  <div
                    v-if="
                      dsBanDaChonNhanh.some(
                        (b) =>
                          Number(b.idBanAn || b.idBan) ===
                          Number(item.ban.idBanAn || item.ban.idBan),
                      )
                    "
                    class="corner-check"
                  >
                    <i class="ri-checkbox-circle-fill"></i>
                  </div>

                  <div class="table-header">
                    <span class="table-code">{{ item.ban.tenBanAn }}</span>
                    <span
                      class="table-status-text"
                      :style="{
                        color:
                          item.ban.trangThaiHienThi === 1
                            ? '#4caf50'
                            : item.ban.trangThaiHienThi === 2
                            ? '#42a5f5'
                            : item.ban.trangThaiHienThi === 3
                            ? '#f44336'
                            : item.ban.trangThaiHienThi === 4
                            ? '#ff9800'
                            : '#9e9e9e',
                      }"
                    >
                      {{ getTrangThaiLabel(item.ban.trangThaiHienThi) }}
                    </span>
                  </div>

                  <div
                    v-if="getThoiGianSuDungText(item.ban)"
                    class="table-timer-text"
                    style="
                      text-align: right;
                      padding-right: 6px;
                      margin-top: -2px;
                      margin-bottom: -10px;
                      z-index: 1;
                    "
                  >
                    {{ getThoiGianSuDungText(item.ban) }}
                  </div>

                  <div class="table-body">
                    {{ getSoGheDaDung(item.ban) }}/{{ item.ban.sucChuaToiDa }}
                    chỗ
                  </div>

                  <div class="table-footer">
                    <div
                      class="table-invoice-text"
                      style="display: flex; flex-direction: column; width: 100%"
                    >
                      <template v-if="item.ban.trangThaiHienThi === 3">
                        <!-- Trường hợp có nhiều hóa đơn -->
                        <template
                          v-if="
                            (item.ban.coNhieuHoaDon ||
                              item.ban.soHoaDonDangHoatDong > 1) &&
                            item.ban.dsHoaDon &&
                            item.ban.dsHoaDon.length > 0
                          "
                        >
                          <div
                            v-for="hd in sapXepHoaDonTheoThoiGianMo(
                              item.ban.dsHoaDon,
                            )"
                            :key="hd.idHoaDon"
                            style="
                              display: flex;
                              justify-content: space-between;
                              width: 100%;
                              margin-top: 2px;
                            "
                          >
                            <span>{{ hd.maHoaDon }}</span>
                            <span style="font-weight: 600"
                              >{{
                                formatCurrency(
                                  hd.tienConLai ?? hd.tongTien ?? 0,
                                )
                              }}đ</span
                            >
                          </div>
                        </template>
                        <!-- Trường hợp chỉ có 1 hóa đơn -->
                        <template v-else>
                          <div
                            style="
                              display: flex;
                              justify-content: space-between;
                              width: 100%;
                            "
                          >
                            <span>
                              {{ item.ban.maHoaDon || 1 }}
                            </span>
                            <span style="font-weight: 600">
                              {{
                                formatCurrency(
                                  item.ban.tienConLai ?? item.ban.tongTien ?? 0,
                                )
                              }}đ
                            </span>
                          </div>
                        </template>
                      </template>
                    </div>
                  </div>
                </div>

                <div
                  v-else-if="!item.blocked"
                  class="slot-empty"
                  style="min-height: 120px"
                ></div>
              </template>
            </div>
          </div>
        </div>

        <div class="box-white khach-cho">
          <template v-if="datBanNhanh">
            <div class="header-khach-cho">
              <h6>Đặt bàn nhanh</h6>
              <button
                class="btn-chon-khach-cho-san"
                @click="moModalChonKhachChoDatNhanh"
              >
                <i class="ri-user-follow-line"></i>
                Chọn khách chờ sẵn
              </button>
            </div>

            <div class="dat-nhanh-panel">
              <div>Đã chọn {{ dsBanDaChonNhanh.length }} bàn</div>

              <div>Tổng sức chứa: {{ tongSucChuaBanDaChon }} khách</div>

              <input
                v-model.number="soKhachDatNhanh"
                type="number"
                min="1"
                class="input"
                placeholder="Nhập số khách"
              />

              <!-- Box thông tin khách chờ đã chọn -->
              <div v-if="khachChoDaChonDatNhanh" class="khach-cho-da-chon-box">
                <div class="khach-cho-da-chon-header">
                  <span
                    ><i class="ri-user-follow-fill"></i> Khách chờ đã chọn</span
                  >
                  <button
                    class="btn-bo-chon-khach-cho"
                    @click="boChonKhachChoDatNhanh"
                  >
                    <i class="ri-close-line"></i>
                  </button>
                </div>
                <div class="khach-cho-da-chon-row">
                  <span class="lbl"
                    ><i class="ri-user-line"></i> Tên khách hàng</span
                  >
                  <b>{{ tenHienThiKhachChoBan(khachChoDaChonDatNhanh) }}</b>
                </div>
                <div class="khach-cho-da-chon-row">
                  <span class="lbl"
                    ><i class="ri-phone-line"></i> Số điện thoại</span
                  >
                  <b>{{ khachChoDaChonDatNhanh.soDienThoai }}</b>
                </div>
                <div class="khach-cho-da-chon-row">
                  <span class="lbl"
                    ><i class="ri-time-line"></i> Giờ đăng ký</span
                  >
                  <b>{{
                    formatDateTime(khachChoDaChonDatNhanh.thoiGianDangKy)
                  }}</b>
                </div>
              </div>

              <div
                v-if="soKhachDatNhanh > tongSucChuaBanDaChon"
                class="canh-bao-suc-chua"
              >
                Số khách vượt quá tổng sức chứa bàn đã chọn
              </div>

              <button
                class="btn-primary w-100 mt-2"
                :disabled="
                  dsBanDaChonNhanh.length === 0 ||
                  soKhachDatNhanh <= 0 ||
                  soKhachDatNhanh > tongSucChuaBanDaChon ||
                  loadingCheckIn
                "
                @click="
                  xacNhanCheckIn({
                    ...dsBanDaChonNhanh[0],
                    listIdBanAn: dsBanDaChonNhanh.map(
                      (b) => b.idBanAn || b.idBan,
                    ),
                    soKhachCheckIn: soKhachDatNhanh,
                    ghiChuDatBan: 'Đặt bàn trực tiếp tại quầy',
                    idDatBan: null, // <--- ÉP VỀ NULL ĐỂ KHÔNG BỊ LẤY PHIẾU CỦA VIP
                    maDatBan: null,
                    idKhachChoBan: khachChoDaChonDatNhanh?.id || null,
                  })
                "
              >
                <div class="btn-light-sweep"></div>
                Xác nhận đặt nhanh
              </button>
              <div v-if="soKhachDatNhanh > 0" class="note-dat-nhanh">
                Nên:
                {{ goiYBan }}
              </div>
            </div>
          </template>

          <template v-else>
            <div class="header-khach-cho">
              <h6>Khách chờ check in</h6>

              <button class="btn-xem-all" @click="showKhachChoAll = true">
                <i class="ri-list-check"></i>
                Xem tất cả
              </button>
            </div>

            <div class="filter-group">
              <div class="filter-label">
                <i class="ri-search-line search-icon"></i> TÌM KIẾM
              </div>
              <input
                v-model="searchKeyword"
                class="input"
                placeholder="Tìm theo mã, tên hoặc SĐT..."
              />
            </div>

            <div class="filter-group">
              <div class="filter-label">
                <i class="ri-time-line history-icon"></i> HIỂN THỊ PHIẾU ĐẶT
                TRONG:
              </div>
              <div class="time-filter-segment">
                <button
                  :class="{ active: timeFilter === '15p' }"
                  @click="timeFilter = '15p'"
                >
                  <span class="tf-value">15</span
                  ><span class="tf-unit">phút</span>
                </button>
                <button
                  :class="{ active: timeFilter === '30p' }"
                  @click="timeFilter = '30p'"
                >
                  <span class="tf-value">30</span
                  ><span class="tf-unit">phút</span>
                </button>
                <button
                  :class="{ active: timeFilter === '1h' }"
                  @click="timeFilter = '1h'"
                >
                  <span class="tf-value">1</span
                  ><span class="tf-unit">giờ</span>
                </button>
                <button
                  :class="{ active: timeFilter === '3h' }"
                  @click="timeFilter = '3h'"
                >
                  <span class="tf-value">3</span
                  ><span class="tf-unit">giờ</span>
                </button>
                <button
                  :class="{ active: timeFilter === 'het-ngay' }"
                  @click="timeFilter = 'het-ngay'"
                >
                  <i class="ri-calendar-check-line tf-icon"></i>
                  <span class="tf-unit">Hết ngày</span>
                </button>
              </div>
            </div>

            <div class="filter-row">
              <div class="filter-group">
                <div class="filter-label">
                  <i class="ri-calendar-line date-icon"></i> NGÀY ĐẾN
                </div>
                <input
                  type="date"
                  v-model="selectedDate"
                  class="input"
                  @change="loadKhachCho"
                />
              </div>

              <div class="filter-group">
                <div class="filter-label">
                  <i class="ri-filter-3-line filter-icon"></i> TRẠNG THÁI
                </div>
                <select v-model="statusFilter" class="input">
                  <option value="ALL">Tất cả trạng thái</option>
                  <option value="CHO_XAC_NHAN">Chờ xác nhận</option>
                  <option value="DA_XAC_NHAN">Chưa tới giờ</option>
                  <option value="DANG_PHUC_VU">Đã check in</option>
                  <option value="HOAN_THANH">Hoàn thành</option>
                  <option value="DA_HUY_HET_HAN">Đã hủy / Hết hạn</option>
                </select>
              </div>
            </div>

            <div class="list-khach">
              <div
                v-if="danhSachKhachLoc.length === 0"
                style="text-align: center; color: #999; padding: 20px"
              >
                Không có khách đặt bàn phù hợp
              </div>

              <div
                class="card-khach"
                v-for="item in danhSachKhachLoc"
                :key="item.id"
              >
                <div class="top">
                  <b>{{ item.tenKhachHang }}</b>
                  <span class="ma-ban">
                    {{ hienThiBanTheoDatBan(item) }}
                  </span>
                </div>

                <div class="info">
                  <div>
                    <i class="ri-phone-line" style="color: #a52a2a"></i>
                    {{ item.soDienThoai }}
                  </div>
                  <div>
                    <i class="ri-time-line"></i>
                    {{
                      new Date(item.thoiGianDat).toLocaleTimeString("vi-VN", {
                        hour: "2-digit",
                        minute: "2-digit",
                      })
                    }}
                  </div>
                  <div v-if="item.tenKhuVuc">
                    <i class="ri-map-pin-line" style="color: #ea4335"></i>
                    {{ item.tenKhuVuc }}
                  </div>
                </div>

                <button
                  class="btn-primary w-100 mt-2"
                  :disabled="
                    item.trangThai !== 'DA_XAC_NHAN' ||
                    daCheckIn(item) ||
                    daHuyDatBan(item) ||
                    chuaToiGio(item) ||
                    hetThoiGianGiuBan(item)
                  "
                  :class="{
                    'da-check in': daCheckIn(item),
                    'da-huy': daHuyDatBan(item) || hetThoiGianGiuBan(item),
                  }"
                  @click="xuLyCheckInKhachCho(item)"
                >
                  <div class="btn-light-sweep"></div>
                  {{ textNutCheckIn(item) }}
                </button>

                <div
                  v-if="!daCheckIn(item) && !daHuyDatBan(item)"
                  class="thoi-gian-giu-ban"
                  :class="{
                    'sap-het':
                      getConLaiGiuBan(item).includes('Còn 00:') ||
                      getConLaiGiuBan(item).includes('Còn 01:'),
                  }"
                >
                  {{ getConLaiGiuBan(item) }}
                </div>

                <div
                  class="countdown"
                  :class="getTrangThaiDatBanUI(item.trangThai).class"
                >
                  {{
                    hetThoiGianGiuBan(item)
                      ? "Đã hủy"
                      : getTrangThaiDatBanUI(item.trangThai).text
                  }}
                </div>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>

    <Teleport to="body">
      <div
        v-if="showTaoHoaDonMoi"
        class="overlay-tao-hd"
        @click.self="showTaoHoaDonMoi = false"
      >
        <div class="popup-tao-hd">
          <h4>Thêm khách vào bàn</h4>

          <label
            >Nhập số lượng khách (Tối đa:
            {{ getSoGheTrong(banCoNhieuHoaDon) }})</label
          >
          <input
            type="number"
            v-model="soKhachHoaDonMoi"
            min="1"
            :class="{ 'input-invalid': errorSoKhachHoaDonMoi }"
            @input="errorSoKhachHoaDonMoi = ''"
          />
          <transition name="shake">
            <p v-if="errorSoKhachHoaDonMoi" class="error-hint">
              <i class="bi bi-exclamation-circle"></i>
              {{ errorSoKhachHoaDonMoi }}
            </p>
          </transition>

          <div class="btn-group-tao-hd">
            <button
              class="btn-primary"
              :disabled="loadingTaoHoaDon || soKhachHoaDonMoi <= 0"
              @click="xuLyTaoHoaDonMoi"
            >
              <div class="btn-light-sweep"></div>
              Xác nhận
            </button>

            <button class="btn-cancel" @click="showTaoHoaDonMoi = false">
              Hủy
            </button>
          </div>
        </div>
      </div>

      <!-- Modal nhập mã đặt bàn & check in -->
      <div
        v-if="showNhapMaDatBan"
        class="overlay-tao-hd"
        @click.self="showNhapMaDatBan = false"
      >
        <div class="popup-tao-hd popup-xac-thuc-checkin" style="width: 450px">
          <h4
            style="
              text-align: center;
              border-bottom: 1px dashed #e2e8f0;
              padding-bottom: 12px;
              margin-bottom: 15px;
            "
          >
            XÁC THỰC MÃ ĐẶT BÀN
          </h4>

          <div class="info-dat-ban-xac-thuc">
            <div class="info-row">
              <span class="lbl">Khách hàng:</span>
              <span class="val"
                ><b>{{ itemDangCheckInKhachCho?.tenKhachHang }}</b></span
              >
            </div>
            <div class="info-row">
              <span class="lbl">Số điện thoại:</span>
              <span class="val"
                ><b>{{ itemDangCheckInKhachCho?.soDienThoai }}</b></span
              >
            </div>
            <div class="info-row">
              <span class="lbl">Giờ đặt:</span>
              <span class="val">{{
                formatDateTime(itemDangCheckInKhachCho?.thoiGianDat)
              }}</span>
            </div>
            <div class="info-row">
              <span class="lbl">Số lượng:</span>
              <span class="val"
                >{{ itemDangCheckInKhachCho?.soNguoiToiDa || 1 }} người</span
              >
            </div>
            <div class="info-row">
              <span class="lbl">Vị trí xếp:</span>
              <span class="val">{{
                hienThiBanTheoDatBan(itemDangCheckInKhachCho)
              }}</span>
            </div>
          </div>

          <input
            type="text"
            v-model="maDatBanInput"
            class="input input-ma-xac-thuc"
            :class="{ 'input-invalid': errorMaDatBan }"
            placeholder="Nhập mã xác nhận đặt bàn..."
            @keyup.enter="xacNhanMaDatBan"
            @input="errorMaDatBan = ''"
            style="text-align: center; font-weight: bold; letter-spacing: 1px"
          />
          <transition name="shake">
            <p v-if="errorMaDatBan" class="error-hint mt-1 mb-2">
              <i class="bi bi-exclamation-circle"></i>
              {{ errorMaDatBan }}
            </p>
          </transition>

          <div
            class="btn-group-tao-hd"
            style="margin-top: 20px; justify-content: space-between"
          >
            <button
              class="btn-primary"
              @click="xacNhanMaDatBan"
              style="width: 65%"
            >
              <div class="btn-light-sweep"></div>
              Xác nhận check in
            </button>
            <button
              class="btn-cancel"
              @click="showNhapMaDatBan = false"
              style="width: 30%"
            >
              Hủy
            </button>
          </div>
        </div>
      </div>

      <!-- Modal Xác nhận đổi bàn khi check-in va chạm khách -->
      <div
        v-if="showConfirmDoiBan"
        class="overlay-chon-hoa-don"
        @click.self="showConfirmDoiBan = false"
      >
        <div
          class="modal-chon-hoa-don"
          style="width: 1000px; text-align: center; padding: 30px 20px"
        >
          <div
            style="
              font-size: 54px;
              color: #f59e0b;
              margin-bottom: 10px;
              line-height: 1;
            "
          >
            <i class="ri-error-warning-fill"></i>
          </div>
          <h3
            style="
              margin: 0 0 12px;
              font-size: 20px;
              font-weight: 800;
              color: var(--color-text);
            "
          >
            Bàn đang có người ngồi!
          </h3>
          <p
            style="
              color: #64748b;
              margin: 0 0 24px;
              font-size: 15px;
              line-height: 1.5;
            "
          >
            Bàn khách đặt hiện tại vẫn đang có khách sử dụng. Bạn có muốn đổi
            sang bàn trống khác cho khách đặt trước này không?
          </p>
          <div style="display: flex; gap: 12px; justify-content: center">
            <button
              class="btn-cancel"
              style="
                flex: 1;
                padding: 12px;
                border-radius: 7px;
                font-size: 15px;
              "
              @click="showConfirmDoiBan = false"
            >
              Hủy bỏ
            </button>
            <button
              class="btn-primary"
              style="
                flex: 1;
                padding: 12px;
                border-radius: 7px;
                font-size: 15px;
              "
              @click="xacNhanDoiBanKhachCho"
            >
              <div class="btn-light-sweep"></div>
              Có, đổi bàn
            </button>
          </div>
        </div>
      </div>

      <!-- Modal Danh sách khách chờ bàn (dạng bảng) -->
      <div
        v-if="showKhachChoBan"
        class="overlay-chon-hoa-don"
        @click.self="dongModalKhachChoBan"
      >
        <div class="modal-chon-hoa-don modal-khach-cho-ban">
          <!-- Header -->
          <div class="modal-hd-header khcb-header">
            <div class="khcb-title-group">
              <div class="khcb-icon-wrap">
                <i class="ri-user-follow-line"></i>
              </div>
              <div>
                <h3 class="khcb-title">Danh sách khách chờ bàn</h3>
                <p class="khcb-subtitle">
                  Quản lý khách đặt bàn – xếp bàn – check-in
                </p>
              </div>
            </div>
            <button class="khcb-close-btn" @click="dongModalKhachChoBan">
              <i class="ri-close-line"></i>
            </button>
          </div>

          <!-- Bộ lọc -->
          <div class="khcb-filter-box">
            <div class="khach-cho-ban-filter">
              <div class="filter-item-khcb">
                <label>Tìm kiếm</label>
                <input
                  v-model="khachChoBanKeyword"
                  class="input"
                  placeholder="Tìm theo tên hoặc số điện thoại..."
                />
              </div>
              <div class="filter-item-khcb">
                <label>Trạng thái</label>
                <v-select
                  class="modern-select"
                  v-model="khachChoBanTrangThai"
                  :options="trangThaiOptionsKhachCho"
                  :reduce="(opt) => opt.value"
                  label="label"
                  placeholder="Tất cả trạng thái"
                  :clearable="false"
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
          </div>

          <!-- BOX 2: Tiêu đề + nút + bảng -->
          <div class="khcb-table-box">
            <div class="khcb-table-header-row">
              <div class="khcb-table-title">
                <span class="khcb-table-title-icon">
                  <i class="ri-list-check-2"></i>
                </span>
                Danh sách khách chờ
                <span class="khcb-count-badge">{{
                  danhSachKhachChoBanLoc.length
                }}</span>
              </div>
              <button class="btn-them-khach-cho" @click="moModalThemKhachCho">
                <i class="ri-user-add-line"></i>
                Thêm khách chờ
              </button>
            </div>

            <div class="khach-cho-ban-table-wrap">
              <table class="khach-cho-ban-table">
                <thead>
                  <tr>
                    <th style="width: 50px">STT</th>
                    <th>Tên khách hàng</th>
                    <th>Số điện thoại</th>
                    <th style="text-align: center">Số khách</th>
                    <th>Giờ đăng ký</th>
                    <th style="text-align: center">Trạng thái</th>
                    <th style="text-align: center; width: 130px">Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="loadingKhachChoBan">
                    <td colspan="7" class="empty-row">
                      <div class="empty-state">
                        <i class="ri-loader-4-line"></i>
                        <span>Đang tải...</span>
                      </div>
                    </td>
                  </tr>
                  <tr v-else-if="danhSachKhachChoBanLoc.length === 0">
                    <td colspan="7" class="empty-row">
                      <div class="empty-state">
                        <i class="ri-user-search-line"></i>
                        <span>Không có khách chờ bàn phù hợp</span>
                      </div>
                    </td>
                  </tr>
                  <tr
                    v-for="(item, index) in danhSachKhachChoBanPhanTrang"
                    :key="item.id"
                  >
                    <td style="text-align: center; font-weight: 700">
                      {{
                        (trangKhachChoBan - 1) * pageSizeKhachChoBan + index + 1
                      }}
                    </td>
                    <td>
                      <div class="khcb-customer-name">
                        <b>{{ tenHienThiKhachChoBan(item) || "--" }}</b>
                      </div>
                    </td>
                    <td>
                      <span class="khcb-sdt">
                        {{ item.soDienThoai || "--" }}
                      </span>
                    </td>
                    <td style="text-align: center; font-weight: 600">
                      {{ item.soLuongKhach || "--" }}
                    </td>
                    <td>
                      <span class="khcb-time">
                        {{ formatDateTime(item.thoiGianDangKy) }}
                      </span>
                    </td>
                    <td style="text-align: center">
                      <span
                        class="status-text"
                        :class="getTrangThaiKhachChoBanUI(item.trangThai).class"
                      >
                        {{ getTrangThaiKhachChoBanUI(item.trangThai).text }}
                      </span>
                    </td>
                    <td>
                      <div class="khcb-action-cell">
                        <button
                          class="khcb-icon-btn khcb-icon-huy"
                          v-tooltip="'Hủy'"
                          :disabled="
                            item.trangThai !== 'DANG_CHO' || loadingHuyKhachCho
                          "
                          :class="{
                            'icon-active-huy': item.trangThai === 'DA_HUY',
                          }"
                          @click="xuLyHuyKhachCho(item)"
                        >
                          <i class="ri-close-circle-line"></i>
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div
            v-if="tongSoTrangKhachChoBan > 1"
            class="pagination pagination-sm mt-2"
          >
            <button
              :disabled="trangKhachChoBan === 1"
              @click="trangKhachChoBan--"
              class="pg-btn pg-arrow"
            >
              <i class="ri-arrow-left-s-line"></i>
            </button>
            <template v-for="p in pageListKhachChoBan" :key="p">
              <button v-if="p === '…'" class="pg-btn pg-ellipsis" disabled>
                ···
              </button>
              <button
                v-else
                class="pg-btn"
                :class="{ active: p === trangKhachChoBan }"
                @click="trangKhachChoBan = p"
              >
                {{ p }}
              </button>
            </template>
            <button
              :disabled="trangKhachChoBan === tongSoTrangKhachChoBan"
              @click="trangKhachChoBan++"
              class="pg-btn pg-arrow"
            >
              <i class="ri-arrow-right-s-line"></i>
            </button>
          </div>
        </div>
      </div>
      <!-- Modal Thêm khách chờ bàn -->
      <div
        v-if="showAddKhachCho"
        class="overlay-chon-hoa-don"
        @click.self="dongModalThemKhachCho"
      >
        <div class="modal-chon-hoa-don modal-them-khach-cho">
          <div class="modal-hd-header khcb-header">
            <div class="khcb-title-group">
              <div class="khcb-icon-wrap">
                <i class="ri-user-add-line"></i>
              </div>
              <div>
                <h3 class="khcb-title">Thêm khách chờ bàn</h3>
                <p class="khcb-subtitle">Nhập thông tin khách đặt bàn trước</p>
              </div>
            </div>
            <button class="khcb-close-btn" @click="dongModalThemKhachCho">
              <i class="ri-close-line"></i>
            </button>
          </div>

          <div class="form-them-khach-cho">
            <div class="form-item-tkc">
              <label>Tên khách hàng <span class="required">*</span></label>
              <div class="input-with-btn">
                <input
                  v-model="formKhachCho.tenKhachHang"
                  class="input"
                  placeholder="Nhập tên khách hàng..."
                  :class="{ 'input-invalid': errorAddKhachCho }"
                  @input="errorAddKhachCho = ''"
                />
                <button
                  class="btn-chon-khach-hang"
                  @click="moModalChonKhachHang"
                >
                  <i class="bi bi-person-lines-fill"></i> Chọn khách hàng
                </button>
              </div>
            </div>

            <div class="form-item-tkc">
              <label>Số điện thoại <span class="required">*</span></label>
              <input
                v-model="formKhachCho.soDienThoai"
                class="input"
                placeholder="Nhập số điện thoại..."
                :class="{ 'input-invalid': errorAddKhachCho }"
                @input="errorAddKhachCho = ''"
              />
            </div>
            <div class="form-item-tkc">
              <label>Số lượng khách <span class="required">*</span></label>
              <input
                v-model.number="formKhachCho.soLuongKhach"
                type="number"
                min="1"
                class="input"
                placeholder="Nhập số lượng khách..."
                :class="{ 'input-invalid': errorAddKhachCho }"
                @input="errorAddKhachCho = ''"
              />
            </div>
            <div class="form-item-tkc">
              <label>Giờ đăng ký <span class="required">*</span></label>
              <div class="datetime-wrapper">
                <input
                  ref="thoiGianDatChoRef"
                  class="datetime-input"
                  :class="{ 'input-error': errorAddKhachCho }"
                  placeholder="Chọn ngày & giờ"
                  readonly
                />
                <i class="bi bi-calendar-event datetime-icon"></i>
              </div>
            </div>

            <transition name="shake">
              <p v-if="errorAddKhachCho" class="error-hint">
                <i class="bi bi-exclamation-circle"></i>
                {{ errorAddKhachCho }}
              </p>
            </transition>
          </div>

          <div class="btn-group-tao-hd" style="margin-top: 20px">
            <button class="btn-cancel" @click="dongModalThemKhachCho">
              Hủy
            </button>
            <button
              class="btn-primary"
              :disabled="loadingAddKhachCho"
              @click="xacNhanThemKhachCho"
            >
              <div class="btn-light-sweep"></div>
              Xác nhận
            </button>
          </div>
        </div>
      </div>
      <!-- Modal Chọn Khách Hàng (dùng cho form thêm khách chờ) -->
      <div
        v-if="hienModalChonKhachHang"
        class="overlay-tao-hd"
        style="z-index: 100001"
        @click.self="dongModalChonKhachHang"
      >
        <div
          style="
            background: #fff;
            width: 1000px;
            max-width: 96vw;
            max-height: 90vh;
            border-radius: 14px;
            padding: 24px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
            display: flex;
            flex-direction: column;
            text-align: left;
          "
        >
          <div style="display: flex; align-items: center; margin-bottom: 16px">
            <h3
              style="
                margin: 0;
                font-size: 18px;
                font-weight: 700;
                color: #1e293b;
              "
            >
              <i
                class="bi bi-person-lines-fill"
                style="color: #f87171; margin-right: 8px"
              ></i>
              Chọn khách hàng
            </h3>
            <button
              @click="dongModalChonKhachHang"
              style="
                margin-left: auto;
                background: #f1f5f9;
                border: none;
                width: 32px;
                height: 32px;
                border-radius: 8px;
                cursor: pointer;
                font-size: 18px;
                color: #64748b;
              "
            >
              ×
            </button>
          </div>

          <input
            v-model="tuKhoaTimKhachHang"
            type="text"
            placeholder="Tìm theo tên, số điện thoại..."
            class="input"
            style="margin-bottom: 14px"
          />

          <div style="overflow-y: auto; flex: 1; min-height: 200px">
            <table
              style="width: 100%; border-collapse: collapse; font-size: 13.5px"
            >
              <thead>
                <tr style="background: #f8fafc; text-align: left">
                  <th
                    style="padding: 10px 8px; width: 44px; text-align: center"
                  >
                    STT
                  </th>
                  <th style="padding: 10px 8px">Mã</th>
                  <th style="padding: 10px 8px">Tên</th>
                  <th style="padding: 10px 8px">SĐT</th>
                  <th style="padding: 10px 8px">Email</th>
                  <th style="padding: 10px 8px">Ngày sinh</th>
                  <th
                    style="padding: 10px 12px; width: 90px; text-align: center"
                  >
                    Thao tác
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="dangTaiKhachHang">
                  <td
                    colspan="7"
                    style="text-align: center; padding: 24px; color: #94a3b8"
                  >
                    Đang tải...
                  </td>
                </tr>
                <tr v-else-if="!dsKhachHangPhanTrang.length">
                  <td
                    colspan="7"
                    style="text-align: center; padding: 24px; color: #94a3b8"
                  >
                    Không tìm thấy khách hàng
                  </td>
                </tr>
                <tr
                  v-for="(kh, index) in dsKhachHangPhanTrang"
                  :key="kh.id"
                  style="border-bottom: 1px solid #f1f5f9"
                >
                  <td
                    style="
                      padding: 10px 8px;
                      text-align: center;
                      color: #64748b;
                    "
                  >
                    {{ (trangKhachHang - 1) * pageSizeKhachHang + index + 1 }}
                  </td>
                  <td
                    style="padding: 10px 8px; font-weight: 600; color: #1e293b"
                  >
                    {{ kh.maKhachHang || kh.ma || "--" }}
                  </td>
                  <td
                    style="padding: 10px 8px; font-weight: 600; color: #1e293b"
                  >
                    {{ kh.tenKhachHang || kh.hoTen }}
                  </td>
                  <td style="padding: 10px 8px">
                    {{ kh.soDienThoai || "--" }}
                  </td>
                  <td style="padding: 10px 8px">{{ kh.email || "--" }}</td>
                  <td style="padding: 10px 8px">
                    {{
                      kh.ngaySinh
                        ? new Date(kh.ngaySinh).toLocaleDateString("vi-VN")
                        : "--"
                    }}
                  </td>
                  <td style="padding: 10px 8px; text-align: center">
                    <button
                      type="button"
                      @click="chonKhachHangNay(kh)"
                      style="
                        width: 30px;
                        height: 30px;
                        border-radius: 7px;
                        border: 1px solid #bbf7d0;
                        background: #f0fdf4;
                        color: #16a34a;
                        cursor: pointer;
                        font-size: 15px;
                      "
                      title="Chọn khách hàng này"
                    >
                      <i class="bi bi-plus-lg"></i>
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div
            class="pagination"
            style="margin-top: 14px"
            v-if="tongSoTrangKhachHang > 1"
          >
            <button
              :disabled="trangKhachHang === 1"
              @click="trangKhachHang--"
              class="pg-btn pg-arrow"
            >
              <i class="ri-arrow-left-s-line"></i>
            </button>
            <template v-for="p in pageListKhachHang" :key="p">
              <button v-if="p === '…'" class="pg-btn pg-ellipsis" disabled>
                ···
              </button>
              <button
                v-else
                class="pg-btn"
                :class="{ active: p === trangKhachHang }"
                @click="trangKhachHang = p"
              >
                {{ p }}
              </button>
            </template>
            <button
              :disabled="trangKhachHang === tongSoTrangKhachHang"
              @click="trangKhachHang++"
              class="pg-btn pg-arrow"
            >
              <i class="ri-arrow-right-s-line"></i>
            </button>
          </div>
        </div>
      </div>
      <!-- Modal Chọn khách chờ sẵn (Đặt bàn nhanh) -->
      <!-- Modal Chọn khách chờ sẵn (Đặt bàn nhanh) -->
      <div
        v-if="showChonKhachChoDatNhanh"
        class="overlay-chon-hoa-don"
        @click.self="dongModalChonKhachChoDatNhanh"
      >
        <div class="modal-chon-hoa-don modal-khach-cho-ban">
          <div class="modal-hd-header khcb-header">
            <div class="khcb-title-group">
              <div class="khcb-icon-wrap">
                <i class="ri-user-follow-line"></i>
              </div>
              <div>
                <h3 class="khcb-title">Chọn khách chờ sẵn</h3>
                <p class="khcb-subtitle">
                  Chọn khách đã đăng ký chờ để đặt bàn nhanh
                </p>
              </div>
            </div>
            <button
              class="khcb-close-btn"
              @click="dongModalChonKhachChoDatNhanh"
            >
              <i class="ri-close-line"></i>
            </button>
          </div>

          <!-- BOX 1: Bộ lọc -->
          <div class="khcb-filter-box">
            <div class="khach-cho-ban-filter">
              <div class="filter-item-khcb">
                <label>Tìm kiếm</label>
                <input
                  v-model="khachChoDatNhanhKeyword"
                  class="input"
                  placeholder="Tìm theo tên hoặc số điện thoại..."
                />
              </div>
              <div class="filter-item-khcb">
                <label>Trạng thái</label>
                <v-select
                  class="modern-select"
                  v-model="khachChoDatNhanhTrangThai"
                  :options="trangThaiOptionsKhachCho"
                  :reduce="(opt) => opt.value"
                  label="label"
                  placeholder="Tất cả trạng thái"
                  :clearable="false"
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
          </div>

          <!-- BOX 2: Tiêu đề + bảng -->
          <div class="khcb-table-box">
            <div class="khcb-table-header-row">
              <div class="khcb-table-title">
                <span class="khcb-table-title-icon">
                  <i class="ri-list-check-2"></i>
                </span>
                Danh sách khách chờ
                <span class="khcb-count-badge">{{
                  dsKhachChoDatNhanhLoc.length
                }}</span>
              </div>
            </div>

            <div class="khach-cho-ban-table-wrap">
              <table class="khach-cho-ban-table">
                <thead>
                  <tr>
                    <th style="width: 50px">STT</th>
                    <th>Tên khách hàng</th>
                    <th>Số điện thoại</th>
                    <th style="text-align: center">Số khách</th>
                    <th>Giờ đăng ký</th>
                    <th style="text-align: center">Trạng thái</th>
                    <th style="text-align: center; width: 130px">Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="loadingKhachChoBan">
                    <td colspan="7" class="empty-row">
                      <div class="empty-state">
                        <i class="ri-loader-4-line"></i>
                        <span>Đang tải...</span>
                      </div>
                    </td>
                  </tr>

                  <tr v-else-if="dsKhachChoDatNhanhLoc.length === 0">
                    <td colspan="7" class="empty-row">
                      <div class="empty-state">
                        <i class="ri-user-search-line"></i>
                        <span>Không có khách chờ phù hợp</span>
                      </div>
                    </td>
                  </tr>

                  <tr
                    v-for="(item, index) in dsKhachChoDatNhanhPhanTrang"
                    :key="item.id"
                  >
                    <td style="text-align: center; font-weight: 700">
                      {{
                        (trangKhachChoDatNhanh - 1) * pageSizeKhachChoDatNhanh +
                        index +
                        1
                      }}
                    </td>
                    <td>
                      <div class="khcb-customer-name">
                        <b>{{ tenHienThiKhachChoBan(item) }}</b>
                      </div>
                    </td>
                    <td>
                      <span class="khcb-sdt">{{ item.soDienThoai }}</span>
                    </td>
                    <td style="text-align: center; font-weight: 600">
                      {{ item.soLuongKhach }}
                    </td>
                    <td>
                      <span class="khcb-time">
                        {{ formatDateTime(item.thoiGianDangKy) }}
                      </span>
                    </td>
                    <td style="text-align: center">
                      <span
                        class="status-text"
                        :class="getTrangThaiKhachChoBanUI(item.trangThai).class"
                      >
                        {{ getTrangThaiKhachChoBanUI(item.trangThai).text }}
                      </span>
                    </td>
                    <td style="text-align: center">
                      <button
                        type="button"
                        class="btn-chon-plus"
                        :disabled="item.trangThai !== 'DANG_CHO'"
                        @click="chonKhachChoDatNhanh(item)"
                        title="Chọn khách này"
                      >
                        <i class="ri-add-line"></i>
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <!-- 👆 đóng khcb-table-box -->

          <!-- Phân trang: nằm NGOÀI box -->
          <div
            v-if="tongSoTrangKhachChoDatNhanh > 1"
            class="pagination pagination-sm mt-2"
          >
            <button
              :disabled="trangKhachChoDatNhanh === 1"
              @click="trangKhachChoDatNhanh--"
              class="pg-btn pg-arrow"
            >
              <i class="ri-arrow-left-s-line"></i>
            </button>
            <template v-for="p in pageListKhachChoDatNhanh" :key="p">
              <button v-if="p === '…'" class="pg-btn pg-ellipsis" disabled>
                ···
              </button>
              <button
                v-else
                class="pg-btn"
                :class="{ active: p === trangKhachChoDatNhanh }"
                @click="trangKhachChoDatNhanh = p"
              >
                {{ p }}
              </button>
            </template>
            <button
              :disabled="trangKhachChoDatNhanh === tongSoTrangKhachChoDatNhanh"
              @click="trangKhachChoDatNhanh++"
              class="pg-btn pg-arrow"
            >
              <i class="ri-arrow-right-s-line"></i>
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <CheckInChiTiet
      ref="checkInChiTietRef"
      v-if="showCheckIn && banDangChon"
      :ban="banDangChon"
      :ds-hoa-don="banDangChon.dsHoaDon"
      :loading="loadingCheckIn"
      @close="dongModalVaResetBan"
      @confirm="xacNhanCheckIn"
      @update-mon="xuLyXacNhanMon"
      @update-hoa-don="xuLyCapNhatHoaDon"
      @go-thanh-toan="diTrangThanhToan"
    />

    <KhachChoCheckInAll
      v-if="showKhachChoAll"
      :show="showKhachChoAll"
      @close="showKhachChoAll = false"
    />

    <ChonBanCheckIn
      v-if="showChonBanCheckIn"
      :show="showChonBanCheckIn"
      mode="doi-ban-checkin"
      :ban-hien-tai="itemCheckInDoiBan"
      @close="showChonBanCheckIn = false"
      @confirm="xuLyXacNhanDoiBanCheckIn"
    />
  </div>
</template>

<style scoped>
/* =========================================================
   BẢN CHUẨN ĐÃ DỌN SẠCH CODE RÁC VÀ CSS TRÙNG LẶP
========================================================= */

.noi-dung,
.box,
.box-white,
.card-khach,
.input,
button,
span,
p,
div,
h2,
h6,
label {
  font-family: var(--font-family, "Inter", sans-serif);
  box-sizing: border-box;
}
.header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-flex.no-border {
  border: none;
}
.header-section {
  width: 100%;
}
.title-group {
  display: flex;
  align-items: center;
  gap: 16px;
}

.icon-wrapper {
  width: 56px;
  height: 56px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 7px;
  background: linear-gradient(
    135deg,
    rgba(225, 29, 72, 0.08),
    rgba(225, 29, 72, 0.15)
  );
  border: 1px solid rgba(225, 29, 72, 0.1);
}

.icon-wrapper img,
.icon-wrapper-small img {
  width: 26px;
  height: 26px;
  object-fit: contain;
}
.icon-title {
  width: 28px;
  height: 28px;
  object-fit: contain;
}

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
.box-header p {
  margin: 10px 0 0;
  color: var(--color-text-muted);
}
.box {
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.96),
    rgba(255, 255, 255, 0.88)
  );
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 7px;
  padding: 18px 22px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
  backdrop-filter: blur(10px);
}
.box-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.noi-dung {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
  align-items: start;
}
.box-white {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 7px;
  padding: 16px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
}
.box-white h6 {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 16px;
  color: var(--color-text);
}

/* ===== KHU TẦNG ===== */
.khu-tang {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
}
.list-tang {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.the-tang {
  border: 1px solid rgba(0, 0, 0, 0.08);
  padding: 10px 16px;
  border-radius: 7px;
  cursor: pointer;
  transition: var(--transition);
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  background: #fff;
}
.the-tang:hover {
  transform: translateY(-2px);
  background: #f8f9fa;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.06);
}
.the-tang.active {
  background: rgba(230, 90, 90, 0.12);
  border-color: rgba(230, 90, 90, 0.35);
  color: var(--color-primary);
  font-weight: 700;
}
.dat-nhanh {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
}
.switch {
  width: 46px;
  height: 24px;
  background: var(--color-switch-off);
  border-radius: 999px;
  position: relative;
  cursor: pointer;
  transition: var(--transition);
}
.switch .dot {
  width: 20px;
  height: 20px;
  background: var(--color-white);
  border-radius: 999px;
  position: absolute;
  top: 2px;
  left: 2px;
  transition: var(--transition);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}
.switch.active {
  background: var(--color-switch-on);
  box-shadow: 0 0 0 4px rgba(215, 55, 55, 0.14);
}
.switch.active .dot {
  left: 24px;
}

/* ===== CHÚ THÍCH ===== */
.khu-chu-thich {
  display: flex;
  gap: 18px;
  margin: 14px 0 16px 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-muted);
}
.dot {
  width: 10px;
  height: 10px;
  border-radius: 7px;
  display: inline-block;
  margin-right: 4px;
}
.xanh-la {
  background: #4caf50;
}
.do {
  background: #f44336;
}
.vang {
  background: #ffca28;
  color: #000;
}
.xanh-duong {
  background: #42a5f5;
}
.wifi-icon {
  width: 16px;
  margin-right: 4px;
  vertical-align: text-bottom;
}

/* ===== KHU SƠ ĐỒ ===== */
.khu-so-do {
  min-height: calc(100vh - 260px);
  padding: 18px;
  border-radius: 7px;
  border: 1px dashed rgba(0, 0, 0, 0.12);
  background: radial-gradient(
    circle at 1px 1px,
    rgba(0, 0, 0, 0.08) 1px,
    transparent 0
  );
  background-size: 30px 30px;
  overflow: hidden;
}
.grid-ban {
  display: grid;
  gap: 18px;
  justify-content: start;
  grid-template-columns: repeat(6, minmax(120px, 1fr));
  grid-template-rows: repeat(3, 120px);
  grid-auto-rows: 120px;
}

.slot {
  width: 100%;
  height: 120px;
  position: relative;
}

.slot-empty {
  border: 1px dashed #cfd8dc;
  border-radius: 7px;
  position: relative;
  height: 100%;
}

.ban-lon {
  grid-row: span 3;
  height: calc(120px * 3 + 18px * 2) !important;
  z-index: 10;
}

.table-card.du-phong {
  background: #9ca3af !important;
  color: #ffffff !important;
}

.table-card.du-phong .table-header {
  background: #f3f4f6 !important;
}

.table-card.du-phong .table-code {
  color: #374151 !important;
}

.table-card.du-phong .table-body {
  color: #ffffff !important;
}

.du-phong-khoa-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: rgba(0, 0, 0, 0.45);
  color: white;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.5px;
  border-radius: 8px;
  z-index: 20;
  gap: 6px;
}

.du-phong-khoa-overlay i {
  font-size: 16px;
}

.invalid-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 6px;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 15;
  pointer-events: none;
}
.invalid-overlay span {
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.5px;
  text-align: center;
  padding: 0 6px;
  white-space: nowrap;
}

.btn-mo-du-phong {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px solid #dcdfe6;
  background: #ffffff;
  color: #606266;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-left: auto;
}

.btn-mo-du-phong:hover {
  background: #f5f7fa;
  border-color: #c0c4cc;
  color: #303133;
}

.btn-mo-du-phong.active {
  background: #fffbeb;
  border-color: #f59e0b;
  color: #d97706;
  box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.15);
}

.btn-mo-du-phong.active:hover {
  background: #fef3c7;
}

/* ===== 1. GIAO DIỆN BÀN CHUẨN MẪU (FLAT) ===== */
.table-card {
  position: relative;
  border: 1.5px solid #e2e8f0;
  background: #ffffff;
  border-radius: 7px;
  padding: 8px;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  height: 100%;
  container-type: inline-size;
}
.table-card:not(.not-available):hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}
.table-header {
  background: #f8fafc;
  border-radius: 6px;
  padding: 2px 4px;
  margin-bottom: 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.table-code {
  color: #1e293b;
  font-size: clamp(9px, 10cqw, 10px);
  letter-spacing: 0.5px;
  font-weight: 800;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 35%;
}
.table-body {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 14px;
  font-weight: 800;
  color: #ffffff;
  margin: 4px 0 10px;
}
.table-invoice-text {
  font-size: clamp(6.5px, 6cqw, 7.5px);
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.table-footer {
  margin-top: auto;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 2px;
}
.table-status-text {
  font-size: 9px;
  font-weight: 600;
  text-transform: uppercase;
  opacity: 0.9;
  color: rgba(255, 255, 255, 0.9);
}
.table-timer-text {
  font-size: 8px;
  font-weight: 600;
  text-transform: uppercase;
  opacity: 0.9;
  color: rgba(255, 255, 255, 0.9);
}

.d-block {
  display: block;
}
.text-muted {
  color: #64748b;
}
.mt-1 {
  margin-top: 4px;
}

.table-card.trong {
  background: #4caf50;
}
.table-card.da_dat {
  background: #42a5f5;
}
.table-card.co_khach {
  background: #f44336;
}
.table-card.dang_don {
  background: #ffca28;
  color: #000;
}
.table-card.dang_don .table-body,
.table-card.dang_don .table-status-text,
.table-card.dang_don .table-invoice-text,
.table-card.dang_don .table-timer-text {
  color: rgba(0, 0, 0, 0.7);
}

.table-card.selected {
  border: 2px solid #8b1e1e !important;
  transform: translateY(-5px) scale(1.02);
  background: #ffffff !important;
  box-shadow: 0 10px 20px rgba(139, 30, 30, 0.15);
  z-index: 10;
}
.table-card.selected .table-body,
.table-card.selected .table-status-text,
.table-card.selected .table-invoice-text,
.table-card.selected .table-timer-text {
  color: #8b1e1e !important;
}

.corner-check {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #ffffff;
  color: #8b1e1e;
  font-size: 20px;
  line-height: 1;
  border-radius: 50%;
  z-index: 11;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  animation: popCheck 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
@keyframes popCheck {
  0% {
    transform: scale(0);
  }
  70% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

.not-available {
  cursor: not-allowed !important;
  pointer-events: all;
}

.not-available::after {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: 7px;
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: grayscale(80%);
  z-index: 4;
  pointer-events: none;
}

/* ===== 2. CSS ICONS ===== */
.wifi-ban {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 16px;
  opacity: 0.9;
}
.table-card.ban-ghep {
  border: 2px dashed #f97316;
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.18),
    0 10px 22px rgba(249, 115, 22, 0.25);
}
.icon-ban-ghep {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 24px;
  height: 24px;
  border-radius: 999px;
  background: #f97316;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  z-index: 5;
  box-shadow: 0 4px 10px rgba(249, 115, 22, 0.35);
}
.icon-warning-booking {
  position: absolute;
  top: 6px;
  left: 6px;
  background: #dc2626;
  color: #fff;
  width: 24px;
  height: 24px;
  border-radius: 7px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  box-shadow: 0 4px 10px rgba(220, 38, 38, 0.3);
  z-index: 5;
}
.large-vertical {
  grid-row: span 3;
  height: 100%;
  z-index: 5;
}

/* ===== KHÁCH CHỜ ===== */
.khach-cho {
  height: calc(100vh - 180px);
  overflow-y: auto;
}
.filter-group {
  margin-bottom: 14px;
}
.filter-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 700;
  color: var(--color-text-muted);
  margin-bottom: 6px;
}
.filter-label i {
  transition: var(--transition);
}
.filter-group:hover .filter-label i {
  color: var(--color-primary);
  transform: scale(1.06);
}
.input {
  width: 100%;
  padding: 10px 12px;
  border-radius: var(--radius-md);
  border: 1px solid #dee2e6;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
  background: #fff;
  transition: var(--transition);
  outline: none;
}
.input::placeholder {
  color: var(--color-text-muted);
}
.input:hover {
  border-color: #cfcfcf;
}
.input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12);
}
.time-filter {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.time-filter button {
  flex: 1;
  min-width: 58px;
  padding: 8px;
  border-radius: 7px;
  border: none;
  background: #e9ecef;
  color: var(--color-text);
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  transition: var(--transition);
}
.time-filter button:hover {
  background: #dee2e6;
  transform: translateY(-1px);
}
.time-filter button.active {
  background: linear-gradient(135deg, var(--color-primary), #d16969);
  color: var(--color-white);
  font-weight: 700;
  box-shadow: 0 10px 20px rgba(230, 90, 90, 0.2);
}

/* ===== CARD KHÁCH ===== */
.list-khach {
  margin-top: 10px;
}
.card-khach {
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 7px;
  padding: 14px;
  margin-bottom: 12px;
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.98),
    rgba(255, 255, 255, 0.9)
  );
  position: relative;
  transition: all 0.25s ease;
  overflow: hidden;
}
.card-khach::before {
  content: "";
  position: absolute;
  inset: 0 auto auto 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, var(--color-primary), #f97316);
}
.card-khach:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
}
.card-khach .top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}
.card-khach .top b {
  font-size: 15px;
  font-weight: 800;
  color: var(--color-text);
}

/* GIỮ LẠI CLASS MA-BAN CHO KHÁCH CHỜ */
.ma-ban {
  order: 1;
  background: rgba(255, 255, 255, 0.96);
  color: var(--color-text);
  border-radius: 7px;
  font-weight: 800;
  font-size: 12px;
  padding: 6px 10px;
  display: inline-block;
  margin-bottom: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}
.card-khach .top .ma-ban {
  margin-bottom: 0;
  font-size: 12px;
  padding: 6px 10px;
  border-radius: 7px;
  background: #f8f9fa;
  box-shadow: none;
}

.card-khach .info {
  font-size: 13px;
  color: var(--color-text-muted);
  margin-top: 8px;
  font-weight: 500;
  display: grid;
  gap: 6px;
}

/* ===== BUTTON ===== */
.btn-primary,
.btn-cancel {
  flex: 1;
  padding: 10px 24px;
  border-radius: 7px;
  font-weight: 600;
  font-size: 14px;
  transition: 0.2s;
  text-transform: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  cursor: pointer;
}

.btn-primary {
  position: relative;
  overflow: hidden;
  background: #d73737;
  color: #fff;
  border: none;
}

.btn-primary:hover {
  background: #b92b2b;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(230, 90, 90, 0.3);
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.btn-cancel {
  background: #f1f5f9;
  color: #334155;
  border: 1px solid #e2e8f0;
}

.btn-cancel:hover {
  background: #e2e8f0;
  transform: translateY(-2px);
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

.btn-primary.da-check in,
.btn-primary:disabled {
  background: #d1d5db;
  color: #555;
  cursor: not-allowed;
  box-shadow: none;
}
.btn-primary.da-huy {
  background: #fee2e2;
  color: #dc2626;
  cursor: not-allowed;
  box-shadow: none;
}

.w-100 {
  width: 100%;
}
.mt-2 {
  margin-top: 10px;
}

/* ===== GIỮ BÀN / STATUS ===== */
.thoi-gian-giu-ban {
  font-weight: 600;
  margin-top: 8px;
  padding: 8px 10px;
  border-radius: 7px;
  background: rgba(0, 0, 0, 0.04);
  color: var(--color-text);
  text-align: center;
}
.thoi-gian-giu-ban.sap-het {
  color: var(--color-inactive);
  background: rgba(255, 0, 0, 0.08);
  font-weight: 800;
}
.countdown {
  position: absolute;
  right: 12px;
  bottom: 12px;
  padding: 5px 10px;
  border-radius: 7px;
  font-weight: 700;
  font-size: 12px;
  border: 1px solid transparent;
}
.status-confirmed {
  background: rgba(59, 130, 246, 0.12);
  color: #3b82f6;
  border-color: transparent;
}
.status-pending {
  background: rgba(251, 146, 60, 0.12);
  color: #fb923c;
  border-color: transparent;
}
.status-checkin {
  background: rgba(139, 92, 246, 0.12);
  color: #8b5cf6;
  border-color: transparent;
}
.status-cancel {
  background: rgba(239, 68, 68, 0.12);
  color: #ef4444;
  border-color: transparent;
}
.status-default {
  background: rgba(107, 114, 128, 0.12);
  color: #6b7280;
  border-color: transparent;
}
.status-success {
  background: rgba(5, 150, 105, 0.12);
  color: #059669;
  border-color: transparent;
  box-shadow: none;
}

/* ===== OVERLAY & MODAL ===== */
.overlay-chon-hoa-don {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(6px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}
.modal-chon-hoa-don {
  width: 1200px;
  max-width: 96vw;
  min-height: 550px;
  max-height: 92vh;
  overflow-y: auto;
  background: #fff;
  border-radius: 7px;
  padding: 30px;
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.28);
}
.modal-hd-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.modal-hd-header h3 {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: var(--color-text);
}
.modal-hd-header button {
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 7px;
  background: #f1f5f9;
  font-size: 22px;
  cursor: pointer;
}
.modal-hd-desc {
  margin: 8px 0 18px;
  color: #64748b;
  font-size: 14px;
}
.hoa-don-card {
  display: grid;
  grid-template-columns: 1fr auto auto;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 7px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #fff;
}
.hoa-don-card:hover {
  border-color: var(--color-primary);
  background: #fff5f5;
  transform: translateY(-2px);
  box-shadow: 0 10px 22px rgba(230, 90, 90, 0.14);
}
.hd-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.hd-info b {
  color: var(--color-primary);
  font-size: 15px;
}
.hd-info span {
  font-weight: 700;
  color: #0f172a;
}
.hd-info small {
  color: #64748b;
}
.hd-money {
  font-weight: 800;
  color: #111827;
  white-space: nowrap;
}
.btn-chon-hd {
  border: none;
  border-radius: 7px;
  padding: 8px 14px;
  background: linear-gradient(135deg, var(--color-primary), #d16969);
  color: #fff;
  font-weight: 700;
  cursor: pointer;
}

.header-khach-cho {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.header-khach-cho h6 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

.btn-xem-all {
  background: #d73737;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s ease;
}

.btn-xem-all:hover {
  background: #b92b2b;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(215, 55, 55, 0.2);
}

.dat-nhanh-panel {
  margin-top: 14px;
  padding: 14px;
  border-radius: 7px;
  background: #fff;
  border: 1px solid #dc2626;
  display: grid;
  gap: 10px;
  font-weight: 700;
}
.canh-bao-suc-chua {
  color: #dc2626;
  font-size: 13px;
  font-weight: 700;
}
.note-dat-nhanh {
  margin-top: 10px;
  padding: 10px 12px;
  border-radius: 7px;
  background: #fff;
  border: 1px solid #dc2626;
  color: #000000;
  font-size: 13px;
  line-height: 1.5;
  font-weight: 600;
}

.tao-hd-moi {
  margin: 10px 0;
}

.overlay-tao-hd {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.55);
  backdrop-filter: blur(4px);
  z-index: 100000;
  display: flex;
  align-items: center;
  justify-content: center;
}
.popup-tao-hd {
  width: 300px;
  background: #fff;
  border-radius: 7px;
  padding: 18px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.25);
  text-align: center;
}
.popup-tao-hd h4 {
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 800;
}
.btn-group-tao-hd {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}
.btn-group-tao-hd .btn-primary,
.btn-group-tao-hd .btn-cancel {
  flex: 1;
  padding: 10px;
  border-radius: 7px;
  border: none;
}

.filter-row {
  display: flex;
  gap: 12px;
  margin-bottom: 14px;
}
.filter-row .filter-group {
  flex: 1;
  margin-bottom: 0;
}

.table-card.co_khach:not(.not-available) {
  animation: smoothPulse 2s infinite ease-in-out;
  border: 1.5px solid #f44336;
}

@keyframes smoothPulse {
  0% {
    box-shadow: 0 0 0 0 rgba(244, 67, 54, 0.1);
  }
  50% {
    box-shadow: 0 0 0 7px rgba(244, 67, 54, 0.1);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(244, 67, 54, 0.6);
  }
}

.text-ghep-ban {
  display: flex;
  align-items: center;
  color: #ea580c !important;
  font-weight: 600;
}

.badge-ghep-mini {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: #f97316;
  color: #fff;
  padding: 3px 6px;
  border-radius: 6px;
  font-size: 10px;
  font-weight: 800;
  margin-right: 6px;
  box-shadow: 0 2px 6px rgba(249, 115, 22, 0.25);
}

:deep(body.dark-mode) .text-ghep-ban {
  color: #fb923c !important;
}

/* ===== DARK MODE ===== */
:deep(body.dark-mode) .box,
:deep(body.dark-mode) .box-white,
:deep(body.dark-mode) .card-khach {
  background: #181818;
  border: 1px solid #2a2a2a;
  box-shadow: none;
}
:deep(body.dark-mode) .title,
:deep(body.dark-mode) .box-white h6,
:deep(body.dark-mode) .card-khach .top b,
:deep(body.dark-mode) .dat-nhanh,
:deep(body.dark-mode) .the-tang,
:deep(body.dark-mode) .input,
:deep(body.dark-mode) .thoi-gian-giu-ban {
  color: #e5e5e5;
}
:deep(body.dark-mode) .box-header p,
:deep(body.dark-mode) .filter-label,
:deep(body.dark-mode) .card-khach .info {
  color: #bdbdbd;
}
:deep(body.dark-mode) .input {
  background: #1f1f1f;
  border: 1px solid #333;
  color: #fff;
}
:deep(body.dark-mode) .input::placeholder {
  color: #8a8a8a;
}
:deep(body.dark-mode) .the-tang {
  background: #1a1a1a;
  border-color: #2a2a2a;
}
:deep(body.dark-mode) .the-tang:hover {
  background: #222;
}
:deep(body.dark-mode) .the-tang.active {
  background: rgba(230, 90, 90, 0.18);
  border-color: rgba(230, 90, 90, 0.35);
  color: #fff;
}
:deep(body.dark-mode) .khu-so-do {
  border-color: #2a2a2a;
  background: radial-gradient(
      circle at 1px 1px,
      rgba(255, 255, 255, 0.05) 1px,
      transparent 0
    ),
    #121212;
  background-size: 30px 30px;
}
:deep(body.dark-mode) .ma-ban {
  background: #232323;
  color: #fff;
}
:deep(body.dark-mode) .card-khach .top .ma-ban {
  background: #232323;
  color: #fff;
}
:deep(body.dark-mode) .time-filter button {
  background: #232323;
  color: #d6d6d6;
}
:deep(body.dark-mode) .time-filter button:hover {
  background: #2a2a2a;
}
:deep(body.dark-mode) .time-filter button.active {
  background: linear-gradient(135deg, var(--color-primary), #d16969);
  color: #fff;
}
:deep(body.dark-mode) .thoi-gian-giu-ban {
  background: #202020;
}
:deep(body.dark-mode) .countdown {
  box-shadow: none;
}

:deep(body.dark-mode) .not-available::after {
  background: rgba(0, 0, 0, 0.55);
}

/* ===== RESPONSIVE ===== */
@media (max-width: 1400px) {
}
@media (max-width: 1200px) {
  .noi-dung {
    grid-template-columns: 1fr;
  }
  .khach-cho {
    height: auto;
    max-height: 650px;
  }
}
@media (max-width: 768px) {
  .title {
    font-size: 24px;
  }
  .box,
  .box-white {
    padding: 14px;
  }
  .khu-tang {
    flex-direction: column;
    align-items: stretch;
  }
  .dat-nhanh {
    justify-content: space-between;
  }
  .time-filter {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
  }
  .card-khach .top {
    flex-direction: column;
    align-items: flex-start;
  }
  .countdown {
    position: static;
    display: inline-flex;
    margin-top: 10px;
  }
}
@media (max-width: 480px) {
  .time-filter {
    grid-template-columns: repeat(2, 1fr);
  }
}
.info-dat-ban-xac-thuc {
  background: #f8fafc;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}
.info-dat-ban-xac-thuc .info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}
.info-dat-ban-xac-thuc .info-row:last-child {
  margin-bottom: 0;
}
.info-dat-ban-xac-thuc .info-row .lbl {
  color: #64748b;
}
.info-dat-ban-xac-thuc .info-row .val {
  color: #0f172a;
  font-weight: 500;
}
.input-ma-xac-thuc {
  padding: 12px !important;
  font-size: 16px !important;
  border: 2px solid #e2e8f0 !important;
  border-radius: 8px !important;
  margin-bottom: 5px !important;
}
.input-ma-xac-thuc:focus {
  border-color: #f59e0b !important;
  box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.2) !important;
}

.error-text {
  color: #dc2626;
  font-size: 13px;
  margin-top: 6px;
  display: block;
  font-weight: 500;
}
/* --- ERROR VALIDATION --- */
.error-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #e63946;
  font-size: 0.85rem;
  margin-top: 4px;
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

/* ===== NÚT KHÁCH CHỜ BÀN (NGOÀI MÀN HÌNH) ===== */
.btn-khach-cho-ban-wrap {
  position: relative;
  display: inline-flex;
  margin-left: auto;
}
.btn-khach-cho-ban {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  border-radius: 8px;
  border: 1px solid #dcdfe6;
  background: #ffffff;
  color: #606266;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}
.btn-khach-cho-ban:hover {
  background: #f5f7fa;
  border-color: #c0c4cc;
  color: #303133;
}
.badge-khach-cho {
  position: absolute;
  top: -8px;
  right: -8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 5px;
  border-radius: 999px;
  background: #d73737;
  color: #fff;
  font-size: 11px;
  font-weight: 800;
  line-height: 1;
  border: 2px solid #fff;
  box-shadow: 0 2px 6px rgba(215, 55, 55, 0.4);
  animation: pulseBadge 2s infinite;
  pointer-events: none;
}
@keyframes pulseBadge {
  0%,
  100% {
    box-shadow: 0 2px 6px rgba(215, 55, 55, 0.4);
  }
  50% {
    box-shadow: 0 2px 6px rgba(215, 55, 55, 0.4),
      0 0 0 5px rgba(215, 55, 55, 0.15);
  }
}

/* bỏ margin-left auto ở nút mở bàn dự phòng vì nút khách chờ bàn đã đảm nhiệm */
.btn-mo-du-phong {
  margin-left: 0 !important;
}

/* ===== MODAL KHÁCH CHỜ BÀN (TO VÀ DÀI) ===== */
.modal-khach-cho-ban {
  width: 1300px;
  max-width: 97vw;
  min-height: 82vh;
  max-height: 95vh;
}

/* Header modal mới */
.khcb-header {
  border-bottom: 1px solid #e8edf3;
  padding-bottom: 16px;
  margin-bottom: 0;
}
.khcb-title-group {
  display: flex;
  align-items: center;
  gap: 14px;
}
.khcb-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(
    135deg,
    rgba(215, 55, 55, 0.1),
    rgba(215, 55, 55, 0.18)
  );
  border: 1px solid rgba(215, 55, 55, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #d73737;
  flex-shrink: 0;
}
.khcb-title {
  margin: 0 0 4px;
  font-size: 20px;
  font-weight: 800;
  color: #1a1a2e;
  letter-spacing: 0.3px;
}
.khcb-subtitle {
  margin: 0;
  font-size: 13px;
  color: #64748b;
}
.khcb-close-btn {
  width: 36px;
  height: 36px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #f8fafc;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  color: #64748b;
}
.khcb-close-btn:hover {
  background: #fee2e2;
  border-color: #fca5a5;
  color: #dc2626;
}

/* Bộ lọc mới */
.khach-cho-ban-filter {
  display: flex;
  gap: 16px;
  margin: 18px 0 0;
}
.filter-item-khcb {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.filter-item-khcb label {
  font-size: 12px;
  font-weight: 700;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* Tiêu đề bảng + nút thêm */
.khcb-table-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 18px 0 10px;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}
.khcb-table-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 800;
  color: #1e293b;
}
.khcb-table-title-icon {
  width: 28px;
  height: 28px;
  background: rgba(215, 55, 55, 0.1);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #d73737;
  font-size: 14px;
}
.khcb-count-badge {
  background: #d73737;
  color: #fff;
  font-size: 11px;
  font-weight: 800;
  padding: 2px 7px;
  border-radius: 999px;
  min-width: 22px;
  text-align: center;
}
.btn-them-khach-cho {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  background: linear-gradient(135deg, #d73737, #b92b2b);
  color: #fff;
  border: none;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  overflow: hidden;
}
.btn-them-khach-cho:hover {
  background: linear-gradient(135deg, #b92b2b, #9b2222);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(215, 55, 55, 0.3);
}

/* Bảng mới */
.khach-cho-ban-table-wrap {
  max-height: calc(95vh - 380px);
  overflow-y: auto;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
}
.khach-cho-ban-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13.5px;
}
.khach-cho-ban-table thead th {
  position: sticky;
  top: 0;
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  color: #475569;
  font-weight: 700;
  text-align: left;
  padding: 12px 16px;
  border-bottom: 2px solid #e2e8f0;
  z-index: 1;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.4px;
}
.khach-cho-ban-table tbody td {
  padding: 12px 16px;
  border-bottom: 1px solid #f1f5f9;
  color: #1e293b;
  vertical-align: middle;
}
.khach-cho-ban-table tbody tr:hover {
  background: #fdf8f8;
}

/* Ô khách hàng */
.khcb-customer-name {
  display: flex;
  align-items: center;
  gap: 10px;
}
.khcb-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #d73737, #f97316);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 800;
  flex-shrink: 0;
}
.khcb-sdt {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #374151;
  font-weight: 500;
}
.khcb-sdt i {
  color: #d73737;
}
.khcb-time {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #374151;
  font-weight: 500;
}
.khcb-time i {
  color: #3b82f6;
}
.khcb-ban-info {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #374151;
  font-weight: 600;
}
.khcb-ban-info i {
  color: #059669;
}

/* Badge trạng thái theo style DanhMuc */
.status-text {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 110px;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}
.status-success {
  background: rgba(34, 197, 94, 0.12) !important;
  color: #22c55e !important;
}
.status-error {
  background: rgba(239, 68, 68, 0.12) !important;
  color: #ef4444 !important;
}
.status-confirmed-dm {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 110px;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 700;
  background: rgba(59, 130, 246, 0.12);
  color: #3b82f6;
}
.status-pending-dm {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 110px;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 700;
  background: rgba(245, 158, 11, 0.12);
  color: #d97706;
}
.status-default-dm {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 110px;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 700;
  background: rgba(107, 114, 128, 0.12);
  color: #6b7280;
}

/* 3 icon thao tác */
.khcb-action-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.khcb-icon-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  color: #94a3b8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s;
}
.khcb-icon-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
  transform: none !important;
  box-shadow: none !important;
}
.khcb-icon-btn:not(:disabled):hover {
  transform: translateY(-2px);
}
/* Hủy */
.icon-active-huy {
  background: #fee2e2;
  border-color: #fca5a5;
  color: #dc2626 !important;
}
/* Đang chờ */
.icon-active-cho {
  background: #fef9c3;
  border-color: #fde047;
  color: #ca8a04 !important;
}
.khcb-icon-cho.icon-active-cho:not(:disabled):hover {
  background: #fef08a;
}
/* Đã xếp bàn */
.icon-active-xep {
  background: #dcfce7;
  border-color: #86efac;
  color: #16a34a !important;
}
.khcb-icon-xep-ban:not(:disabled):hover {
  background: #dcfce7;
  border-color: #86efac;
  color: #16a34a;
  box-shadow: 0 4px 10px rgba(22, 163, 74, 0.2);
}
.khcb-icon-huy:not(:disabled):not(.icon-active-huy):hover {
  background: #fee2e2;
  border-color: #fca5a5;
  color: #dc2626;
}

/* Empty state */
.empty-row {
  text-align: center;
  color: #999;
  padding: 40px !important;
}
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #94a3b8;
}
.empty-state i {
  font-size: 32px;
  opacity: 0.5;
}
.empty-state span {
  font-size: 14px;
  font-weight: 500;
}

.table-badge-trangthai {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.btn-thao-tac-nho {
  padding: 6px 12px;
  font-size: 12px;
  width: auto;
}

/* Vue-select trong modal */
.khach-cho-ban-filter :deep(.vs__dropdown-toggle) {
  border: 1px solid #dee2e6;
  border-radius: var(--radius-md, 8px);
  padding: 6px 10px;
  background: #fff;
  min-height: 42px;
}
.khach-cho-ban-filter :deep(.vs__search) {
  font-size: 14px;
  color: #1e293b;
}

/* Dark mode */
:deep(body.dark-mode) .khach-cho-ban-table thead th {
  background: #1f1f1f;
  color: #ccc;
  border-color: #2a2a2a;
}
:deep(body.dark-mode) .khach-cho-ban-table tbody td {
  color: #e5e5e5;
  border-color: #2a2a2a;
}
:deep(body.dark-mode) .khach-cho-ban-table-wrap {
  border-color: #2a2a2a;
}
:deep(body.dark-mode) .btn-khach-cho-ban {
  background: #1a1a1a;
  border-color: #2a2a2a;
  color: #ccc;
}
:deep(body.dark-mode) .khcb-table-header-row {
  background: #181818;
  border-color: #2a2a2a;
}
:deep(body.dark-mode) .khcb-table-title {
  color: #e5e5e5;
}

.modal-them-khach-cho {
  width: 560px;
  max-width: 92vw;
  min-height: auto;
}

.form-them-khach-cho {
  display: grid;
  gap: 16px;
  margin-top: 18px;
}

.form-item-tkc label {
  display: block;
  font-size: 13px;
  font-weight: 700;
  color: #475569;
  margin-bottom: 6px;
}

.form-item-tkc .required {
  color: #dc2626;
}

.input-with-btn {
  display: flex;
  gap: 8px;
}
.input-with-btn .input {
  flex: 1;
}

.btn-chon-khach-hang {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border-radius: 8px;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  color: #2563eb;
  font-size: 12.5px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;
}
.btn-chon-khach-hang:hover {
  background: #dbeafe;
  border-color: #93c5fd;
}
/* ===== PHÂN TRANG (dùng cho modal chọn khách hàng) ===== */
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

:deep(body.dark-mode) .pg-btn {
  background-color: #2d3748;
  border-color: #4a5568;
  color: #e2e8f0;
}

:deep(body.dark-mode) .pg-btn:hover:not(:disabled):not(.active) {
  background-color: #4a5568;
  border-color: #ff4d4f;
  color: #ff4d4f;
}

:deep(body.dark-mode) .pg-btn:disabled {
  background-color: #1a202c;
  border-color: #2d3748;
  color: #718096;
}

:deep(body.dark-mode) .pg-btn.active {
  background-color: #ff4d4f;
  color: #ffffff;
  border-color: #ff4d4f;
}
.datetime-wrapper {
  position: relative;
}

.datetime-input {
  width: 100%;
  height: 42px;
  border-radius: 8px;
  border: 1px solid #cbd5e1;
  padding: 0 40px 0 12px;
  font-size: 14px;
  background: #fff;
  transition: all 0.22s ease;
  color: #334155;
  cursor: pointer;
}

.datetime-input:hover {
  border-color: #94a3b8;
}

.datetime-input:focus {
  border-color: #ea580c;
  box-shadow: 0 0 0 4px rgba(234, 88, 12, 0.1);
  outline: none;
}

.datetime-icon {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 17px;
  color: #64748b;
  pointer-events: none;
}

.datetime-input.input-error {
  border-color: #dc2626 !important;
  box-shadow: 0 0 0 4px rgba(220, 38, 38, 0.1) !important;
}

.mt-2 {
  margin-top: 10px;
}

.pagination-sm {
  flex-shrink: 0;
  padding: 8px 0;
}

.pagination-sm .pg-btn {
  min-width: 28px;
  height: 28px;
  font-size: 12px;
  padding: 0 6px;
}

/* ===== NÚT CHỌN KHÁCH CHỜ SẴN Ở BOX ĐẶT BÀN NHANH ===== */
.btn-chon-khach-cho-san {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 6px;
  border: 1px solid #dcdfe6;
  background: #ffffff;
  color: #606266;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}
.btn-chon-khach-cho-san:hover {
  background: #f5f7fa;
  border-color: #c0c4cc;
  color: #303133;
}

/* ===== BOX THÔNG TIN KHÁCH CHỜ ĐÃ CHỌN ===== */
.khach-cho-da-chon-box {
  margin-top: 10px;
  padding: 12px 14px;
  border-radius: 7px;
  background: #f0fdf4;
  border: 1px solid #86efac;
  display: grid;
  gap: 6px;
}
.khach-cho-da-chon-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  font-weight: 700;
  color: #15803d;
  margin-bottom: 4px;
}
.btn-bo-chon-khach-cho {
  width: 22px;
  height: 22px;
  border: none;
  border-radius: 6px;
  background: rgba(21, 128, 61, 0.12);
  color: #15803d;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  transition: all 0.2s;
}
.btn-bo-chon-khach-cho:hover {
  background: rgba(21, 128, 61, 0.22);
}
.khach-cho-da-chon-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}
.khach-cho-da-chon-row .lbl {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: #166534;
  font-weight: 500;
}
.khach-cho-da-chon-row b {
  color: #14532d;
  font-weight: 700;
}

/* ===== NÚT + TRONG BẢNG CHỌN KHÁCH CHỜ ===== */
.btn-chon-plus {
  width: 30px;
  height: 30px;
  border-radius: 7px;
  border: 1px solid #bbf7d0;
  background: #f0fdf4;
  color: #16a34a;
  cursor: pointer;
  font-size: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.btn-chon-plus:hover:not(:disabled) {
  background: #dcfce7;
  transform: translateY(-1px);
}
.btn-chon-plus:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}
/* Box trắng bọc bộ lọc */
.khcb-filter-box {
  margin-top: 18px;
  padding: 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
}
.khcb-filter-box .khach-cho-ban-filter {
  margin: 0; /* bỏ margin cũ vì đã có padding của box cha */
}

:deep(body.dark-mode) .khcb-filter-box {
  background: #181818;
  border-color: #2a2a2a;
}
.khcb-filter-box {
  margin-top: 18px;
  padding: 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
}
.khcb-filter-box .khach-cho-ban-filter {
  margin: 0;
}

.khcb-table-box {
  margin-top: 14px;
  padding: 16px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
}
.khcb-table-box .khcb-table-header-row {
  margin: 0 0 10px;
}

:deep(body.dark-mode) .khcb-filter-box,
:deep(body.dark-mode) .khcb-table-box {
  background: #181818;
  border-color: #2a2a2a;
}
.time-filter-segment {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 6px;
  padding: 4px;
  background: #f1f5f9;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}

.time-filter-segment button {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1px;
  padding: 8px 4px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.time-filter-segment button:hover:not(.active) {
  background: rgba(255, 255, 255, 0.7);
  color: #334155;
}

.time-filter-segment .tf-value {
  font-size: 15px;
  font-weight: 800;
  line-height: 1.1;
}

.time-filter-segment .tf-unit {
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.3px;
  opacity: 0.85;
}

.time-filter-segment .tf-icon {
  font-size: 15px;
  margin-bottom: 2px;
}

.time-filter-segment button.active {
  background: linear-gradient(135deg, var(--color-primary), #d16969);
  color: #fff;
  box-shadow: 0 4px 12px rgba(230, 90, 90, 0.3);
  transform: translateY(-1px);
}

.time-filter-segment button.active .tf-unit {
  opacity: 1;
}

/* Dark mode */
:deep(body.dark-mode) .time-filter-segment {
  background: #1a1a1a;
  border-color: #2a2a2a;
}
:deep(body.dark-mode) .time-filter-segment button {
  color: #a3a3a3;
}
:deep(body.dark-mode) .time-filter-segment button:hover:not(.active) {
  background: rgba(255, 255, 255, 0.06);
  color: #e5e5e5;
}
:deep(body.dark-mode) .time-filter-segment button.active {
  background: linear-gradient(135deg, var(--color-primary), #d16969);
  color: #fff;
}

@media (max-width: 480px) {
  .time-filter-segment {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
