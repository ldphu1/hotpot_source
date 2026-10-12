<script setup>
////////////////// THU VIEN //////////////////////
import { ref, watch, computed, onMounted, onUnmounted } from "vue";
import { useRouter, onBeforeRouteLeave } from "vue-router";
import { useUtils } from "@/utils/helper";
import flatpickr from "flatpickr";
import { useValidateThongTinDatBan } from "@/composables/validateThongTinDatBan.js";
import "flatpickr/dist/flatpickr.min.css";
//////////////////// API ////////////////////////
import {
  boBan,
  huyDatBanDangTao,
  thongTinDatBan,
  xacNhanTaiQuay,
} from "@/api/admin/datban/datBanApi";
import { timKiemKhachHang } from "@/api/admin/khachhang/khachhangApi";
import { getAllSanPhamDotGiamGia } from "@/api/admin/sanpham/sanPhamApi";
import { Vietnamese } from "flatpickr/dist/l10n/vn.js";
import { getAllThamSoDong } from "@/api/admin/thamsodong/thamSoDongApi";
import ChonBanLucTaoPhieu from "./ChonBanLucTaoPhieu.vue";
import { getAllDanhMuc } from "@/api/admin/danhmuc/danhMucApi";
import { searchSanPham } from "@/api/admin/sanpham/sanPhamApi";
import { searchSetLau } from "@/api/admin/lau/setLauApi";
//////////////// ICON ////////////////////////
import iconBack from "@/assets/icons/prev.png";
import { getAllBanAn } from "@/api/admin/datban/datBanApi";

/////////////////////// VALIDATE ///////////////
const { validateThongTinDatBan } = useValidateThongTinDatBan();
const idDatBan = ref(null);
const isSubmittingOrCanceling = ref(false);

const errors = ref({
  tenKhachHang: "",
  soDienThoai: "",
  email: "",
  soNguoiToiDa: "",
  thoiGianDat: "",
});

const validateField = (field) => {
  // Reset field đó trước
  errors.value[field] = "";

  const f = {
    tenKhachHang: bookingData.value.tenKhach,
    soDienThoai: bookingData.value.sdt,
    email: bookingData.value.email,
    soNguoiToiDa: bookingData.value.soKhach,
    thoiGianDat: bookingData.value.thoiGian,
  };

  // Chỉ validate field được truyền vào
  const tempErrors = ref({
    tenKhachHang: "",
    soDienThoai: "",
    email: "",
    soNguoiToiDa: "",
    thoiGianDat: "",
  });

  validateThongTinDatBan({
    f,
    errors: tempErrors,
    soChoQuaTai,
    tongGheTrong,
    getThamSo: (key) => thamSo.value[key],
  });

  // Chỉ lấy lỗi của field đang validate
  errors.value[field] = tempErrors.value[field];
};

////////////// CAC FUNCTION
const datetimeRef = ref(null);
const birthdayRef = ref(null);
const router = useRouter();
const { notify, nprogress } = useUtils();
const isLoading = ref(false);
// state quản lý form search khách hàng
const searchCustomer = ref("");
const listKhach = ref([]);
const showDropdown = ref(false);
let timeout = null;
const isSelecting = ref(false);
const isModalOpen = ref(false);
const monDaChon = ref([]);
const monDaChonDraft = ref([]);

//////////////////// BỘ LỘC MODAL CHỌN MÓN ////////////////////
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";

const tenMonAn = ref("");
const selectDanhMuc = ref("");
const sortGia = ref("");
// Chọn món đặt bàn (Modal)
const showModal = ref(false);
const hasProductChanged = ref(false);
const productChangedMessage = ref("");

const coMonNgungKinhDoanh = computed(() => {
  return monDaChon.value.some((item) => item.trangThaiXoa);
});
const coMonNgungKinhDoanhDraft = computed(() => {
  return monDaChonDraft.value.some((item) => item.trangThaiXoa);
});
const tongTienDraft = computed(() => {
  return monDaChonDraft.value.reduce((sum, item) => {
    return sum + (item.giaSauGiam || item.gia) * item.soLuong;
  }, 0);
});

const xacNhanChonMonModal = () => {
  if (coMonNgungKinhDoanhDraft.value) {
    notify.error("Vui lòng loại bỏ các món ăn ngừng kinh doanh trước khi xác nhận!");
    return;
  }
  monDaChon.value = monDaChonDraft.value.map(item => ({ ...item }));
  showModal.value = false;
};
const listSetLau = ref([]);
const listMonLe = ref([]);
const keyword = ref("");
const selectedDanhMuc = ref("");

const thamSo = ref({});
const danhSachDanhMuc = ref([]);

const loadDanhMuc = async () => {
  try {
    const res = await getAllDanhMuc();
    danhSachDanhMuc.value = res.data;
  } catch (error) {
    console.log("Lỗi load danh mục", error);
  }
};

const loadSanPhamDiscount = async () => {
  const res = await getAllSanPhamDotGiamGia(false); // món lẻ
  listMonLe.value = res.data;
  return res;
};
const loadSetLauDiscount = async () => {
  const res = await getAllSanPhamDotGiamGia(true); // set lẩu
  listSetLau.value = res.data;
  return res;
};

const timKiemMonAn = async () => {
  currentPage.value = 1;
  try {
    if (activeTab.value === "set") {
      const params = {
        keyword: tenMonAn.value || null,
        idNuocLau: selectDanhMuc.value || null,
      };

      const res = await searchSetLau(params);
      listSetLau.value = res.data;
    } else {
      const params = {
        keyword: tenMonAn.value || null,
        idDanhMuc: selectDanhMuc.value || null,
        sortGia: sortGia.value || null,
      };

      const res = await searchSanPham(params);
      listMonLe.value = res.data;
    }
  } catch (error) {
    console.log("Lỗi tìm món ăn", error);
  }
};

///////// VALIDATE NÚT CHỌN BÀN
const canPickTable = computed(() => {
  return (
    bookingData.value.tenKhach &&
    bookingData.value.sdt &&
    bookingData.value.thoiGian &&
    bookingData.value.soKhach > 0
  );
});

const tongGheTrong = ref(0);

const danhSachAllBan = ref([]);

const loadBan = async () => {
  const res = await getAllBanAn();
  danhSachAllBan.value = res.data || [];

  tongGheTrong.value = res.data
    .filter((b) => b.trangThai === 1)
    .reduce((sum, b) => sum + (b.sucChuaToiDa || 0), 0);
};

const tongGheChon = computed(() => {
  if (!selectedBooking.value.listIdBan || selectedBooking.value.listIdBan.length === 0) {
    return 0;
  }
  return selectedBooking.value.listIdBan.reduce((sum, id) => {
    const ban = danhSachAllBan.value.find((b) => b.id === id);
    return sum + (ban ? (ban.sucChuaToiDa || 0) : 0);
  }, 0);
});

const soGheDuToiDa = computed(() => {
  return Number(thamSo.value.SO_GHE_DU_TOI_DA) || 0;
});

const laBanPhuHop = computed(() => {
  if (!selectedBooking.value.listIdBan || selectedBooking.value.listIdBan.length === 0) {
    return true;
  }
  const soKhach = bookingData.value.soKhach || 0;
  const tongGhe = tongGheChon.value;
  const maxGhe = soKhach + soGheDuToiDa.value;

  return tongGhe >= soKhach && tongGhe <= maxGhe;
});

const messageBanKhongPhuHop = computed(() => {
  if (laBanPhuHop.value) return "";
  const soKhach = bookingData.value.soKhach || 0;
  const tongGhe = tongGheChon.value;
  const maxGhe = soKhach + soGheDuToiDa.value;
  
  if (tongGhe < soKhach) {
    return `Bàn đã chọn chỉ có ${tongGhe} chỗ, không đủ cho ${soKhach} khách.`;
  }
  if (tongGhe > maxGhe) {
    return `Bàn đã chọn có ${tongGhe} chỗ, quá rộng cho ${soKhach} khách (tối đa dư ${soGheDuToiDa.value} ghế - tối đa ${maxGhe} chỗ).`;
  }
  return "";
});

const soChoQuaTai = computed(() => {
  return (
    bookingData.value.soKhach > 0 &&
    bookingData.value.soKhach > tongGheTrong.value
  );
});
//////// VALIDATE NÚT XÁC NHẬN ///
const canSubmit = computed(() => {
  return (
    bookingData.value.tenKhach &&
    bookingData.value.sdt &&
    bookingData.value.thoiGian &&
    bookingData.value.soKhach > 0 &&
    selectedBooking.value.listIdBan.length > 0 &&
    laBanPhuHop.value
  );
});

// MODAL CHỌN BÀN

const selectedBooking = ref({
  listIdBan: [],
  listTenBan: [],
});

const handleThongTinDatBan = async () => {
  const isValid = validateThongTinDatBan({
    f: {
      tenKhachHang: bookingData.value.tenKhach,
      soDienThoai: bookingData.value.sdt,
      email: bookingData.value.email,
      soNguoiToiDa: bookingData.value.soKhach,
      thoiGianDat: bookingData.value.thoiGian,
    },
    errors,
    soChoQuaTai,
    tongGheTrong,
    getThamSo: (key) => thamSo.value[key],
  });

  if (!isValid) return;

  try {
    nprogress.start();
    const payload = {
      soDienThoai: bookingData.value.sdt,
      tenKhachHang: bookingData.value.tenKhach,
      email: bookingData.value.email,
      ngaySinh: bookingData.value.ngaySinh,
      thoiGianDat: formatLocalDateTime(bookingData.value.thoiGian),
      soNguoiToiDa: bookingData.value.soKhach,
      ghiChu: bookingData.value.ghiChu,
    };

    const res = await thongTinDatBan(payload);
    idDatBan.value = res.data.id;

    // Mở popup chọn bàn
    isModalOpen.value = true;
  } catch (e) {
    const msg = e.response?.data?.message || "Có lỗi xảy ra";
    if (msg.includes("Số điện thoại") && msg.includes("email khác")) {
      errors.value.soDienThoai = msg;
    } else if (msg.includes("Email") && msg.includes("số điện thoại khác")) {
      errors.value.email = msg;
    } else if (msg.includes("thuộc 2 tài khoản")) {
      errors.value.soDienThoai = "Số điện thoại và email không khớp!";
      errors.value.email = "Số điện thoại và email không khớp!";
    } else {
      notify.error(msg);
    }
  } finally {
    nprogress.done();
  }
};
const removeBan = async (tenBan) => {
  const index = selectedBooking.value.listTenBan.indexOf(tenBan);
  if (index === -1) return;

  const idBan = selectedBooking.value.listIdBan[index];

  try {
    const res = await boBan(idDatBan.value, idBan);
    selectedBooking.value.listIdBan = res.data.listIdBan ?? [];
    selectedBooking.value.listTenBan = res.data.listTenBan ?? [];
    notify.success(`Đã bỏ bàn ${tenBan}`);
  } catch (e) {
    console.log(e);
    notify.error(e.response?.data?.message || "Không thể bỏ bàn");
  }
};
const openPickTable = () => {
  isModalOpen.value = true;
};

const handleSuccessXepBan = (data) => {
  isModalOpen.value = false;

  selectedBooking.value.listIdBan = data.listIdBan;
  selectedBooking.value.listTenBan = data.listTenBan;
};

/////////// FORTMAT THOI GIAN
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

import { connectWebSocket, disconnectWebSocket } from "@/utils/websocket";

let wsTimeout = null;

const capNhatTrangThaiDaChon = async () => {
  if (!monDaChon.value.length && !monDaChonDraft.value.length) return;

  try {
    const [resSet, resMon] = await Promise.all([
      getAllSanPhamDotGiamGia(true),
      getAllSanPhamDotGiamGia(false),
    ]);

    const activeSets = resSet.data || [];
    const activeMons = resMon.data || [];
    const allActive = [...activeSets, ...activeMons];

    const activeMap = new Map();
    allActive.forEach((p) => {
      activeMap.set(String(p.id ?? p.idSanPham), p);
    });

    const updateItem = (item) => {
      const activeItem = activeMap.get(String(item.id ?? item.idSanPham));
      if (activeItem) {
        item.trangThaiXoa = false;
        item.gia = activeItem.gia ?? activeItem.donGia ?? activeItem.giaBan ?? item.gia;
        item.giaSauGiam = activeItem.giaSauGiam ?? item.gia;
        item.soTienGiam = activeItem.soTienGiam ?? 0;
        item.phanTramGiam = activeItem.phanTramGiam ?? 0;
        item.tenSanPham = activeItem.tenSanPham || activeItem.tenMon || activeItem.tenSet || item.tenSanPham;
        item.hinhAnh = activeItem.hinhAnh || item.hinhAnh;
      } else {
        item.trangThaiXoa = true;
      }
    };

    monDaChon.value.forEach(updateItem);
    monDaChonDraft.value.forEach(updateItem);
  } catch (err) {
    console.error("Lỗi đồng bộ trạng thái món đã chọn:", err);
  }
};

const handleWsMessage = (msg) => {
  console.log(">>> [ThemPhieuDatBan WS] Received message:", msg);
  if (msg?.type === "SAN_PHAM_UPDATE" || msg?.type === "DOT_GIAM_GIA_UPDATE") {
    console.log(">>> [ThemPhieuDatBan WS] Matching type");
    if (wsTimeout) clearTimeout(wsTimeout);
    wsTimeout = setTimeout(async () => {
      if (showModal.value) {
        await Promise.all([loadSanPhamDiscount(), loadSetLauDiscount()]);
      }
      await capNhatTrangThaiDaChon();
      console.log(">>> [ThemPhieuDatBan WS] Synced.");
      
      if (showModal.value || monDaChon.value.length > 0) {
        hasProductChanged.value = true;
        if (msg.type === "DOT_GIAM_GIA_UPDATE") {
          productChangedMessage.value = "Đợt giảm giá sản phẩm vừa thay đổi hãy kiểm tra giá sản phẩm trước khi xác nhận";
        } else {
          productChangedMessage.value = "Món ăn có sự thay đổi vui lòng kiểm tra thông tin trước khi xác nhận";
        }
        console.log(">>> [ThemPhieuDatBan WS] Triggering confirm dialog:", productChangedMessage.value);
        notify.confirm(productChangedMessage.value);
      }
    }, 150);
  }
};

const handleBeforeUnload = (e) => {
  if (isSubmittingOrCanceling.value || !hasUnsavedChanges.value) return;
  e.preventDefault();
  e.returnValue = "";
};

onMounted(async () => {
  loadDanhMuc();
  loadBan();
  await loadThamSo();
  initDatePicker();
  connectWebSocket(null, handleWsMessage);
  window.addEventListener("beforeunload", handleBeforeUnload);
});

onUnmounted(() => {
  if (wsTimeout) clearTimeout(wsTimeout);
  disconnectWebSocket(handleWsMessage);
  window.removeEventListener("beforeunload", handleBeforeUnload);
});

const hasUnsavedChanges = computed(() => {
  return (
    bookingData.value.tenKhach ||
    bookingData.value.sdt ||
    bookingData.value.email ||
    bookingData.value.ghiChu ||
    bookingData.value.soKhach > 0 ||
    monDaChon.value.length > 0 ||
    idDatBan.value !== null
  );
});

onBeforeRouteLeave(async (to, from) => {
  if (isSubmittingOrCanceling.value) {
    return true;
  }

  if (!hasUnsavedChanges.value) {
    return true;
  }

  const answer = await notify.confirm(
    "Bạn đang trong quá trình đặt bàn. Rời khỏi trang này sẽ hủy toàn bộ thông tin đã nhập và bàn đã chọn. Bạn có chắc chắn muốn rời đi?"
  );

  if (!answer) {
    return false;
  }

  if (idDatBan.value) {
    try {
      await huyDatBanDangTao(idDatBan.value);
    } catch (error) {
      console.error("Lỗi hủy đặt bàn đang tạo khi rời trang:", error);
    }
  }

  return true;
});

watch(thamSo, (val) => {
  if (val && Object.keys(val).length > 0) {
    initDateTimePicker();
  }
});

watch(showModal, async (val) => {
  if (val) {
    hasProductChanged.value = false;
    productChangedMessage.value = "";
    monDaChonDraft.value = monDaChon.value.map(item => ({ ...item }));
    await Promise.all([loadSanPhamDiscount(), loadSetLauDiscount()]);
    await capNhatTrangThaiDaChon();
  }
});

const openModal = async () => {
  showModal.value = true;
  monDaChonDraft.value = monDaChon.value.map(item => ({ ...item }));

  try {
    await Promise.all([loadSanPhamDiscount(), loadSetLauDiscount()]);

    console.log("SET LẨU", listSetLau.value);
    console.log("MÓN LẺ", listMonLe.value);
  } catch (e) {
    console.log("LỖI TẢI MÓN", e);
  }
};

const loadThamSo = async () => {
  try {
    const res = await getAllThamSoDong();
    const obj = {};
    if (Array.isArray(res.data)) {
      res.data.forEach((item) => {
        obj[item.maThamSo] = item.giaTri;
      });
    }
    thamSo.value = obj;
  } catch (e) {
    notify.error("Không tải được tham số hệ thống");
  }
};

let birthdayPicker = null;

const initDatePicker = () => {
  birthdayPicker = flatpickr(birthdayRef.value, {
    dateFormat: "d/m/Y",
    maxDate: "today",
    locale: Vietnamese,
    onChange: (selectedDates) => {
      if (selectedDates.length) {
        const date = selectedDates[0];

        bookingData.value.ngaySinh = date.toISOString().split("T")[0];
      } else {
        bookingData.value.ngaySinh = "";
      }
    },
  });
};

const formattedTime = computed(() => {
  if (!bookingData.value.thoiGian) return "—";

  const date = new Date(bookingData.value.thoiGian);

  return date.toLocaleString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
});

const initDateTimePicker = () => {
  const min = thamSo.value.DAT_TRUOC_TOI_THIEU || 30;
  const max = thamSo.value.DAT_TRUOC_TOI_DA_NGAY || 7;

  flatpickr(datetimeRef.value, {
    enableTime: true,
    dateFormat: "d/m/Y H:i",
    time_24hr: true,
    minuteIncrement: 5,
    locale: Vietnamese,

    minDate: new Date(Date.now() + min * 60000),
    maxDate: new Date(Date.now() + max * 86400000),
    onChange: (selectedDates) => {
      bookingData.value.thoiGian = selectedDates[0];
      validateField("thoiGianDat");
    },
  });
};

const themMon = (item, isDraft = false) => {
  const list = isDraft ? monDaChonDraft.value : monDaChon.value;
  const found = list.find((i) => i.id === item.id);

  if (found) {
    found.soLuong++;
  } else {
    list.push({
      ...item,
      soLuong: 1,
    });
  }
};

const tongTien = computed(() => {
  return monDaChon.value.reduce((sum, item) => {
    return sum + (item.giaSauGiam || item.gia) * item.soLuong;
  }, 0);
});

const tienPhaiCoc = computed(() => {
  const percent = Number(thamSo.value.PHAN_TRAM_COC) || 0;
  const minKhachDoanLon = Number(thamSo.value.SO_KHACH_BAT_BUOC_COC_DOAN_LON) || 0;
  const tienCocMoiKhach = Number(thamSo.value.TIEN_COC_MOI_KHACH_DOAN_LON) || 0;
  const soKhach = Number(bookingData.value.soKhach) || 0;

  let rawVal = 0;
  if (tongTien.value > 0) {
    rawVal += (tongTien.value * percent) / 100;
  }
  if (soKhach > minKhachDoanLon) {
    rawVal += soKhach * tienCocMoiKhach;
  }
  return Math.round(rawVal * 100) / 100;
});
const chooseMon = (item) => {
  themMon({
    ...item,
    laSetLau: false,
  }, true);
};
const chooseSet = (item) => {
  themMon({
    ...item,
    laSetLau: true,
  }, true);
};

const removeSet = (itemOrId) => {
  const targetId = typeof itemOrId === "object" ? itemOrId?.id : itemOrId;
  monDaChon.value = monDaChon.value.filter((i) => i.id !== targetId);
};

const removeMon = (itemOrId) => {
  const targetId = typeof itemOrId === "object" ? itemOrId?.id : itemOrId;
  monDaChon.value = monDaChon.value.filter((i) => i.id !== targetId);
};
const tangSoLuong = (item) => {
  item.soLuong++;
};

const giamSoLuong = (item, isDraft = false) => {
  if (item.soLuong > 1) {
    item.soLuong--;
  } else {
    if (isDraft) {
      monDaChonDraft.value = monDaChonDraft.value.filter((i) => i.id !== item.id);
    } else {
      monDaChon.value = monDaChon.value.filter((i) => i.id !== item.id);
    }
  }
};

const selectedSetLau = computed(() => {
  return monDaChon.value.filter((i) => i.laSetLau);
});

const selectedMonLe = computed(() => {
  return monDaChon.value.filter((i) => !i.laSetLau);
});

watch(searchCustomer, (val) => {
  if (isSelecting.value) {
    isSelecting.value = false;
    return;
  }

  clearTimeout(timeout);
  if (!val || val.trim().length < 2) {
    listKhach.value = [];
    showDropdown.value = false;
    return;
  }
  timeout = setTimeout(async () => {
    try {
      const res = await timKiemKhachHang(val);
      listKhach.value = res.data;
      showDropdown.value = true;
    } catch (e) {
      showDropdown.value = false;
    }
  }, 300);
});

const selectKhach = (khach) => {
  isSelecting.value = true;
  bookingData.value.tenKhach = khach.tenKhachHang;
  bookingData.value.sdt = khach.soDienThoai;
  bookingData.value.email = khach.email;

  // Xóa các lỗi validate cũ của các trường này khi chọn khách hàng có sẵn
  errors.value.tenKhachHang = "";
  errors.value.soDienThoai = "";
  errors.value.email = "";

  if (khach.ngaySinh) {
    let dateObj;
    if (khach.ngaySinh.includes("/")) {
      const parts = khach.ngaySinh.split("/");
      dateObj = new Date(+parts[2], parts[1] - 1, +parts[0]);
    } else {
      dateObj = new Date(khach.ngaySinh);
    }

    if (!isNaN(dateObj.getTime())) {
      bookingData.value.ngaySinh = dateObj.toISOString().split("T")[0];

      if (birthdayPicker) birthdayPicker.setDate(dateObj, true);
    } else {
      bookingData.value.ngaySinh = "";
      if (birthdayPicker) birthdayPicker.clear();
    }
  } else {
    bookingData.value.ngaySinh = "";
    if (birthdayPicker) birthdayPicker.clear();
  }

  searchCustomer.value = `${khach.tenKhachHang} - ${khach.soDienThoai}`;
  showDropdown.value = false;
};

const bookingDataForTable = computed(() => ({
  tenKhachHang: bookingData.value.tenKhach,
  soDienThoai: bookingData.value.sdt,
  soNguoi: bookingData.value.soKhach,
  gioDen: bookingData.value.thoiGian,
  hinhThucDat: 2,
  idDatBan: idDatBan.value,
  dsBan: selectedBooking.value.listIdBan,
  dsTenBan: selectedBooking.value.listTenBan,
}));

// Data Form Đặt Bàn
const bookingData = ref({
  tenKhach: "",
  sdt: "",
  email: "",
  ngaySinh: "",
  soKhach: 0,
  thoiGian: "",
  ghiChu: "",
});

const goBack = () => {
  router.push("/admin/dat-ban");
};

const handleSave = async () => {
  if (coMonNgungKinhDoanh.value) {
    notify.error("Vui lòng loại bỏ các món ăn ngừng kinh doanh trước khi tạo đặt bàn!");
    return;
  }

  if (!canSubmit.value) {
    notify.error("Vui lòng nhập đủ thông tin và chọn bàn");
    return;
  }

  if (!(await notify.confirm("Bạn muốn tạo đặt bàn này?"))) return;

  isLoading.value = true;
  nprogress.start();

  const startTime = Date.now();

  try {
    const monDatTruoc = monDaChon.value.map((i) => ({
      idSanPham: i.id,
      soLuong: i.soLuong,
    }));

    await xacNhanTaiQuay(idDatBan.value, { monDatTruoc });

    notify.success("Tạo đặt bàn thành công!");

    if (monDatTruoc.length === 0) {
      const elapsed = Date.now() - startTime;
      const minimumLoading = 3000;
      if (elapsed < minimumLoading) {
        await new Promise((resolve) =>
          setTimeout(resolve, minimumLoading - elapsed),
        );
      }
    }

    isSubmittingOrCanceling.value = true;
    router.push("/admin/dat-ban");
  } catch (e) {
    notify.error(e.response?.data?.message || "Có lỗi xảy ra khi tạo đặt bàn");
  } finally {
    isLoading.value = false;
    nprogress.done();
  }
};

const huyPhieuDatBan = async () => {
  if (!(await notify.confirm("Hủy quá trình tạo phiếu này?"))) return;
  try {
    if (idDatBan.value) {
      await huyDatBanDangTao(idDatBan.value);
    }
    notify.success("Hủy thành công");
    isSubmittingOrCanceling.value = true;
    goBack();
  } catch (error) {
    console.log(error);
  }
};

////////////////// PhÂN TRANG MÓN ĂN ///////////////////
const activeTab = ref("set");
const currentPage = ref(1);
const pageSize = 6; // số món mỗi trang

const pagedList = computed(() => {
  const list = activeTab.value === "set" ? listSetLau.value : listMonLe.value;
  const start = (currentPage.value - 1) * pageSize;
  return list.slice(start, start + pageSize);
});

const pageList = computed(() => {
  const total = totalPages.value;
  if (total <= 5) return Array.from({ length: total }, (_, i) => i + 1);

  const pages = [1, 2, 3];
  if (total > 4) pages.push("…");
  pages.push(total);
  return pages;
});

const totalPages = computed(() => {
  const list = activeTab.value === "set" ? listSetLau.value : listMonLe.value;
  return Math.ceil(list.length / pageSize);
});

// reset trang khi đổi tab hoặc tìm kiếm
watch(activeTab, () => {
  currentPage.value = 1;
});
watch([tenMonAn, selectDanhMuc, sortGia], () => {
  currentPage.value = 1;
});

/////////  PHÂN TRANG MÓN ĐÃ CHỌN
const currentPageSelected = ref(1);
const pageSizeSelected = 4;

const pagedMonDaChon = computed(() => {
  const start = (currentPageSelected.value - 1) * pageSizeSelected;
  return monDaChonDraft.value.slice(start, start + pageSizeSelected);
});

const totalPagesSelected = computed(() => {
  return Math.ceil(monDaChonDraft.value.length / pageSizeSelected);
});

const pageListSelected = computed(() => {
  const total = totalPagesSelected.value;
  if (total <= 5) return Array.from({ length: total }, (_, i) => i + 1);
  return [1, 2, 3, "…", total];
});

const currentPageSetLau = ref(1);
const currentPageMonLe = ref(1);
const pageSizeSidebar = 3;

const pagedSetLau = computed(() => {
  const start = (currentPageSetLau.value - 1) * pageSizeSidebar;
  return selectedSetLau.value.slice(start, start + pageSizeSidebar);
});

const pagedMonLe = computed(() => {
  const start = (currentPageMonLe.value - 1) * pageSizeSidebar;
  return selectedMonLe.value.slice(start, start + pageSizeSidebar);
});

const totalPagesSetLau = computed(() =>
  Math.ceil(selectedSetLau.value.length / pageSizeSidebar),
);
const totalPagesMonLe = computed(() =>
  Math.ceil(selectedMonLe.value.length / pageSizeSidebar),
);

const pageListSetLau = computed(() => {
  const total = totalPagesSetLau.value;
  if (total <= 5) return Array.from({ length: total }, (_, i) => i + 1);
  return [1, 2, 3, "…", total];
});

const pageListMonLe = computed(() => {
  const total = totalPagesMonLe.value;
  if (total <= 5) return Array.from({ length: total }, (_, i) => i + 1);
  return [1, 2, 3, "…", total];
});

const handleBlur = () => {
  setTimeout(() => {
    showDropdown.value = false;
  }, 200);
};

const getImgUrl = (path) => {
  return path
    ? `http://localhost:8080/uploads/${path}`
    : "https://via.placeholder.com/300x300?text=No+Image";
};
</script>

<template>
  <div class="page-container">
    <header class="box-header-card">
      <div class="box-header-flex">
        <div class="box-header">
          <h2 class="main-title">
            <span class="icon-wrapper">
              <i class="bi bi-calendar-check"></i>
            </span>
            Tạo phiếu đặt bàn
          </h2>
          <p>Thiết lập thông tin khách hàng và dịch vụ đặt bàn</p>
        </div>

        <button class="icon-btn-back" @click="huyPhieuDatBan(idDatBan)">
          <img :src="iconBack" />
        </button>
      </div>
    </header>

    <div class="content-layout">
      <div class="main-form-flow">
        <div class="card form-card">
          <div class="card-header">
            <div class="step-indicator">1</div>
            <h3>Thông tin khách hàng</h3>
          </div>

          <div class="search-section">
            <label class="field-label">Tìm kiếm khách hàng nhanh</label>
            <div class="search-wrapper">
              <input
                v-model="searchCustomer"
                type="text"
                placeholder="Nhập tên hoặc số điện thoại..."
                class="custom-input search-input"
                @focus="showDropdown = true"
                @blur="handleBlur"
              />
              <i class="bi bi-search search-icon"></i>

              <div v-if="showDropdown" class="dropdown-list">
                <template v-if="listKhach.length">
                  <div
                    v-for="item in listKhach"
                    :key="item.id"
                    class="dropdown-item"
                    @mousedown.prevent="selectKhach(item)"
                  >
                    <div class="item-info">
                      <span class="item-name">{{ item.tenKhachHang }}</span>
                      <span class="item-phone">{{ item.soDienThoai }}</span>
                    </div>
                    <span class="choose-text">Chọn</span>
                  </div>
                </template>
                <div v-else class="dropdown-item empty">
                  Không có khách hàng
                </div>
              </div>
            </div>
          </div>

          <div class="divider"><span>hoặc nhập mới</span></div>

          <div class="form-grid">
            <div class="form-group">
              <label class="field-label require">Tên khách hàng</label>
              <input
                v-model="bookingData.tenKhach"
                class="custom-input"
                placeholder="Ví dụ: Lãnh Minh Hiếu"
                @blur="validateField('tenKhachHang')"
                @input="validateField('tenKhachHang')"
              />
              <transition name="shake">
                <p v-if="errors.tenKhachHang" class="error-hint">
                  <i class="fas fa-exclamation-circle"></i>
                  {{ errors.tenKhachHang }}
                </p>
              </transition>
            </div>
            <div class="form-group">
              <label class="field-label require">Số điện thoại</label>
              <input
                v-model="bookingData.sdt"
                class="custom-input"
                placeholder="Ví dụ: 0912345678"
                @blur="validateField('soDienThoai')"
                @input="validateField('soDienThoai')"
              />
              <transition name="shake">
                <p v-if="errors.soDienThoai" class="error-hint">
                  <i class="fas fa-exclamation-circle"></i>
                  {{ errors.soDienThoai }}
                </p>
              </transition>
            </div>
            <div class="form-group">
              <label class="field-label require">Email</label>
              <input
                v-model="bookingData.email"
                type="email"
                class="custom-input"
                placeholder="khachhang@email.com"
                @blur="validateField('email')"
                @input="validateField('email')"
              />
              <transition name="shake">
                <p v-if="errors.email" class="error-hint">
                  <i class="fas fa-exclamation-circle"></i>
                  {{ errors.email }}
                </p>
              </transition>
            </div>
            <div class="form-group">
              <label class="field-label require">Ngày sinh</label>

              <div class="date-wrapper">
                <input
                  ref="birthdayRef"
                  class="date-input"
                  placeholder="Chọn ngày sinh"
                />
                <i class="bi bi-calendar-heart date-icon"></i>
              </div>
            </div>
          </div>
        </div>

        <div class="card form-card">
          <div class="card-header">
            <div class="step-indicator">2</div>
            <h3>Chi tiết đặt bàn</h3>
          </div>
          <div class="form-grid">
            <div class="form-group">
              <label class="field-label require">Số lượng khách</label>
              <input
                v-model="bookingData.soKhach"
                type="number"
                min="0"
                class="custom-input"
                placeholder=""
                @blur="validateField('soNguoiToiDa')"
                @input="validateField('soNguoiToiDa')"
              />
              <transition name="shake">
                <p v-if="errors.soNguoiToiDa" class="error-hint">
                  <i class="fas fa-exclamation-circle"></i>
                  {{ errors.soNguoiToiDa }}
                </p>
              </transition>
              <p v-if="soChoQuaTai" class="error-hint">
                Số khách vượt quá sức chứa hiện tại ({{ tongGheTrong }}
                chỗ trống)
              </p>
            </div>
            <div class="form-group">
              <label class="field-label require">Thời gian đến</label>

              <div class="datetime-wrapper">
                <input
                  ref="datetimeRef"
                  class="datetime-input"
                  :class="{ 'input-error': errors.thoiGianDat }"
                  placeholder="Chọn ngày & giờ"
                />
                <transition name="shake">
                  <p v-if="errors.thoiGianDat" class="error-hint">
                    <i class="fas fa-exclamation-circle"></i>
                    {{ errors.thoiGianDat }}
                  </p>
                </transition>
                <i class="bi bi-calendar-event datetime-icon"></i>
              </div>
            </div>
          </div>
          <div class="form-group full-width" style="margin-top: 15px">
            <label class="field-label">Ghi chú đặc biệt</label>
            <textarea
              v-model="bookingData.ghiChu"
              class="custom-input textarea"
              placeholder="Ví dụ: Bàn gần cửa sổ, tổ chức sinh nhật, dị ứng hải sản..."
              rows="3"
            ></textarea>
          </div>
          <div class="table-selector-wrapper">
            <!-- Chưa nhập đủ -->
            <p v-if="!canPickTable" class="alert-line-luxury">
              <i class="ri-error-warning-fill alert-icon-pulse"></i>
              Vui lòng nhập đủ thông tin để chọn bàn
            </p>

            <!-- Đã nhập đủ, CHƯA xác nhận (chưa có idDatBan) -->
            <div
              v-else-if="!soChoQuaTai && !idDatBan"
              class="confirm-pick-table-box"
            >
              <div class="confirm-content">
                <i class="ri-information-line icon-info"></i>
                <span>Vui lòng xác nhận thông tin để chọn bàn:</span>
              </div>
              <button
                class="btn btn-primary confirm-btn-sweep"
                @click="handleThongTinDatBan()"
              >
                <i class="bi bi-check2-circle"></i> Xác nhận
              </button>
            </div>

            <!-- Đã xác nhận (có idDatBan) nhưng CHƯA chọn bàn -->
            <!-- Đã xác nhận (có idDatBan) nhưng CHƯA chọn bàn -->
            <div
              v-else-if="
                !soChoQuaTai &&
                idDatBan &&
                selectedBooking.listTenBan.length === 0
              "
              class="confirm-pick-table-box"
            >
              <div class="confirm-content">
                <i class="ri-information-line icon-info"></i>
                <span>Vui lòng chọn bàn cho khách:</span>
              </div>

              <span
                class="table-tag-modern add-table-tag"
                @click="openPickTable"
              >
                <i class="bi bi-plus-circle"></i>
                Chọn bàn
                <div class="btn-light-sweep"></div>
              </span>
            </div>

            <!-- Đã chọn bàn -->
            <div
              v-else-if="!soChoQuaTai && selectedBooking.listTenBan.length > 0"
              class="selected-tables-container"
            >
              <div v-if="!laBanPhuHop" class="alert-line-warning" style="margin-top: 0; margin-bottom: 12px;">
                <i class="ri-error-warning-fill alert-icon-pulse"></i>
                <span>{{ messageBanKhongPhuHop }}</span>
              </div>
              <div class="container-label">Bàn đã giữ chỗ:</div>
              <div class="badge-group-modern">
                <span
                  v-for="tenBan in selectedBooking.listTenBan"
                  :key="tenBan"
                  class="table-tag-modern"
                  :class="{ 'table-tag-invalid': !laBanPhuHop }"
                >
                  <i v-if="laBanPhuHop" class="bi bi-check2-circle"></i>
                  <i v-else class="bi bi-exclamation-triangle-fill"></i>
                  Bàn {{ tenBan }}
                  <i
                    class="bi bi-x-lg remove-tag"
                    @click="removeBan(tenBan)"
                  ></i>
                </span>

                <span
                  class="table-tag-modern add-table-tag"
                  @click="openPickTable"
                >
                  <i class="bi bi-plus-circle"></i>
                  Chọn bàn
                  <div class="btn-light-sweep"></div>
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="sidebar-sticky">
        <div class="card summary-card-modern">
          <div class="summary-header-clean">
            <i class="fas fa-clipboard-list header-icon-main"></i>
            <div class="header-text">
              <h3>Tổng quan đặt bàn</h3>
            </div>
          </div>

          <div class="summary-block">
            <div class="block-title">
              <i class="fas fa-user-tag"></i> Khách hàng
            </div>

            <div class="info-row">
              <span><i class="fas fa-user"></i> Tên</span>
              <b class="text-truncate">{{ bookingData.tenKhach || "—" }}</b>
            </div>

            <div class="info-row">
              <span><i class="fas fa-phone-alt"></i> SĐT</span>
              <b>{{ bookingData.sdt || "—" }}</b>
            </div>

            <div class="info-row">
              <span><i class="fas fa-users"></i> Số khách</span>
              <b>{{ bookingData.soKhach || "—" }} </b>
            </div>

            <div class="info-row">
              <span><i class="fas fa-calendar-alt"></i> Thời gian</span>
              <b>{{ formattedTime }}</b>
            </div>
          </div>
          <div class="summary-block">
            <div class="block-title" style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
              <span><i class="ri-shopping-basket-2-line"></i> Món đã chọn</span>
              <button
                v-if="selectedSetLau.length || selectedMonLe.length"
                class="btn btn-outline"
                style="padding: 4px 10px !important; font-size: 12px !important; border-radius: 6px !important; display: inline-flex; align-items: center; gap: 4px;"
                @click="openModal"
              >
                <i class="ri-edit-line"></i> Chỉnh sửa
              </button>
            </div>

            <!-- MÓN CHÍNH -->
            <div class="dish-group">
              <div class="group-title">Món chính</div>
              <div v-if="selectedSetLau.length" class="dish-list">
                <div
                  v-for="(item, index) in pagedSetLau"
                  :key="item.id"
                  class="dish-item-modern"
                  :class="{ 'ngung-kinh-doanh': item.trangThaiXoa }"
                >
                  <img :src="getImgUrl(item.hinhAnh)" class="dish-img-mini" />
                  <div class="dish-detail">
                    <span class="name">{{ item.tenSanPham }}</span>
                    <div v-if="item.trangThaiXoa" class="status-ngung-kinh-doanh-text text-danger fw-bold" style="font-size: 11px; margin-top: 2px;"><i class="ri-error-warning-fill"></i> Món ăn ngừng kinh doanh</div>
                    <div class="dish-bottom-row">
                      <span class="price"
                        >{{
                          (
                            (item.giaSauGiam || item.gia) * item.soLuong
                          ).toLocaleString()
                        }}đ</span
                      >
                      <div class="quantity-inline">
                        <button @click.stop="giamSoLuong(item)">
                          <i class="bi bi-dash"></i>
                        </button>
                        <span>{{ item.soLuong }}</span>
                        <button @click.stop="tangSoLuong(item)" :disabled="item.trangThaiXoa">
                          <i class="bi bi-plus"></i>
                        </button>
                      </div>
                    </div>
                  </div>
                  <button class="remove-icon-btn" @click="removeSet(item.id)">
                    ×
                  </button>
                </div>

                <!-- PHÂN TRANG MÓN CHÍNH -->
                <div
                  v-if="totalPagesSetLau > 1"
                  class="pagination pagination-sm"
                >
                  <button
                    :disabled="currentPageSetLau === 1"
                    @click="currentPageSetLau--"
                    class="pg-btn pg-arrow"
                  >
                    <i class="ri-arrow-left-s-line"></i>
                  </button>
                  <template v-for="p in pageListSetLau" :key="p">
                    <button
                      v-if="p === '…'"
                      class="pg-btn pg-ellipsis"
                      disabled
                    >
                      ···
                    </button>
                    <button
                      v-else
                      class="pg-btn"
                      :class="{ active: p === currentPageSetLau }"
                      @click="currentPageSetLau = p"
                    >
                      {{ p }}
                    </button>
                  </template>
                  <button
                    :disabled="currentPageSetLau === totalPagesSetLau"
                    @click="currentPageSetLau++"
                    class="pg-btn pg-arrow"
                  >
                    <i class="ri-arrow-right-s-line"></i>
                  </button>
                </div>
              </div>
              <p v-else class="empty-hint">Chưa có món chính nào được chọn</p>
            </div>

            <!-- MÓN PHỤ -->
            <div class="dish-group">
              <div class="group-title">Món phụ</div>
              <div v-if="selectedMonLe.length" class="dish-list">
                <div
                  v-for="(item, index) in pagedMonLe"
                  :key="item.id"
                  class="dish-item-modern"
                  :class="{ 'ngung-kinh-doanh': item.trangThaiXoa }"
                >
                  <img :src="getImgUrl(item.hinhAnh)" class="dish-img-mini" />
                  <div class="dish-detail">
                    <span class="name">{{ item.tenSanPham }}</span>
                    <div v-if="item.trangThaiXoa" class="status-ngung-kinh-doanh-text text-danger fw-bold" style="font-size: 11px; margin-top: 2px;"><i class="ri-error-warning-fill"></i> Món ăn ngừng kinh doanh</div>
                    <div class="dish-bottom-row">
                      <span class="price"
                        >{{
                          (
                            (item.giaSauGiam || item.gia) * item.soLuong
                          ).toLocaleString()
                        }}đ</span
                      >
                      <div class="quantity-inline">
                        <button @click.stop="giamSoLuong(item)">
                          <i class="bi bi-dash"></i>
                        </button>
                        <span>{{ item.soLuong }}</span>
                        <button @click.stop="tangSoLuong(item)" :disabled="item.trangThaiXoa">
                          <i class="bi bi-plus"></i>
                        </button>
                      </div>
                    </div>
                  </div>
                  <button class="remove-icon-btn" @click="removeMon(item.id)">
                    ×
                  </button>
                </div>

                <!-- PHÂN TRANG MÓN PHỤ -->
                <div
                  v-if="totalPagesMonLe > 1"
                  class="pagination pagination-sm"
                >
                  <button
                    :disabled="currentPageMonLe === 1"
                    @click="currentPageMonLe--"
                    class="pg-btn pg-arrow"
                  >
                    <i class="ri-arrow-left-s-line"></i>
                  </button>
                  <template v-for="p in pageListMonLe" :key="p">
                    <button
                      v-if="p === '…'"
                      class="pg-btn pg-ellipsis"
                      disabled
                    >
                      ···
                    </button>
                    <button
                      v-else
                      class="pg-btn"
                      :class="{ active: p === currentPageMonLe }"
                      @click="currentPageMonLe = p"
                    >
                      {{ p }}
                    </button>
                  </template>
                  <button
                    :disabled="currentPageMonLe === totalPagesMonLe"
                    @click="currentPageMonLe++"
                    class="pg-btn pg-arrow"
                  >
                    <i class="ri-arrow-right-s-line"></i>
                  </button>
                </div>
              </div>
              <p v-else class="empty-hint">Chưa có món phụ nào được chọn</p>
            </div>

            <div
              v-if="!selectedSetLau.length && !selectedMonLe.length"
              class="empty-pick-minimal"
              @click="openModal"
            >
              <div class="luxury-add-btn">
                <i class="ri-play-list-add-line"></i>
                <span class="btn-text">CHỌN MÓN</span>
                <div class="shine-line"></div>
              </div>
            </div>
          </div>

          <div class="payment-summary-box">
            <!-- WARNING BANNER OUTSIDE MODAL -->
            <div v-if="hasProductChanged" class="alert-product-changed fw-bold">
              <i class="ri-error-warning-fill"></i>
              <span>{{ productChangedMessage || "Món ăn có sự thay đổi vui lòng kiểm tra thông tin trước khi xác nhận" }}</span>
            </div>

            <div class="total-row-modern">
              <span><i class="fas fa-calculator"></i> Tổng tiền</span>
              <b class="total-price-text">{{ tongTien.toLocaleString() }}đ</b>
            </div>
            <!-- Dòng tiền cọc -->
            <div class="total-row-modern">
              <span><i class="fas fa-hand-holding-usd"></i> Tiền cần cọc</span>
              <b class="total-price-text">{{ tienPhaiCoc.toLocaleString() }}đ</b>
            </div>
          </div>
        </div>
        <div class="fixed-bottom-actions">
          <div class="action-container">
            <button
              class="btn btn-outline btn-lg"
              @click="huyPhieuDatBan(idDatBan)"
            >
              <i class="bi bi-x-circle"></i> Hủy bỏ
            </button>

            <button
              class="btn btn-primary btn-lg"
              :disabled="!canSubmit || coMonNgungKinhDoanh"
              :class="{ 'btn-disabled': !canSubmit || coMonNgungKinhDoanh }"
              @click="(canSubmit && !coMonNgungKinhDoanh) && handleSave()"
            >
              <template v-if="coMonNgungKinhDoanh">
                <i class="bi bi-slash-circle"></i> Có món ngừng bán
              </template>
              <template v-else-if="canSubmit">
                <i class="bi bi-check2-all"></i> Xác nhận
              </template>
              <template v-else>
                <i class="fa-solid fa-fingerprint"></i> Thiếu thông tin
              </template>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ////////// CHỌN MÓN TRƯỚC ////////// -->
    <Teleport to="body">
      <div
        v-if="showModal"
        class="modal-overlay"
        @click.self="showModal = false"
      >
        <div class="modal modern-modal big-modal">
          <!-- HEADER -->
          <div class="modal-header-bar">
            <h3>
              <i class="ri-restaurant-2-fill"></i>
              Chọn thực đơn đặt trước
            </h3>
            <button class="close-modal" @click="showModal = false">×</button>
          </div>

          <!-- TAB -->
          <div class="tabs-header">
            <button
              :class="{ active: activeTab === 'set' }"
              @click="activeTab = 'set'"
            >
              <i class="ri-fire-fill me-1"></i>
              Set lẩu
            </button>

            <button
              :class="{ active: activeTab === 'mon' }"
              @click="activeTab = 'mon'"
            >
              <i class="ri-restaurant-2-line me-1"></i>
              Món phụ
            </button>
          </div>

          <!-- FILTER -->
          <div class="modal-filter-bar">
            <div class="filter-group">
              <div class="filter-item">
                <label>Tìm sản phẩm</label>
                <input
                  placeholder="Tên sản phẩm..."
                  v-model="tenMonAn"
                  @input="timKiemMonAn"
                />
              </div>

              <div class="filter-item">
                <label>Danh mục</label>

                <v-select
                  class="modern-select"
                  v-model="selectDanhMuc"
                  :options="danhSachDanhMuc"
                  label="tenDanhMuc"
                  :reduce="(dm) => dm.id"
                  placeholder="Tất cả danh mục"
                  :clearable="false"
                  @update:modelValue="timKiemMonAn"
                />
              </div>

              <div class="filter-item">
                <label>Sắp xếp giá</label>

                <v-select
                  class="modern-select"
                  v-model="sortGia"
                  :options="[
                    { label: 'Mặc định', value: '' },
                    { label: 'Giá tăng dần', value: 'asc' },
                    { label: 'Giá giảm dần', value: 'desc' },
                  ]"
                  label="label"
                  :clearable="false"
                  :reduce="(opt) => opt.value"
                  placeholder="Mặc định"
                  @update:modelValue="timKiemMonAn"
                />
              </div>
            </div>
          </div>

          <!-- CONTENT -->
          <div class="modal-main-layout">
            <!-- LEFT -->
            <div class="modal-body-content">
              <div class="product-grid">
                <div
                  v-for="item in pagedList"
                  :key="item.id"
                  class="product-item-card"
                  @click="
                    activeTab === 'set' ? chooseSet(item) : chooseMon(item)
                  "
                >
                  <div class="product-img-box">
                    <img :src="getImgUrl(item.hinhAnh)" />
                    <div class="hover-add">
                      <span>
                        <i class="ri-add-circle-fill"></i>
                        Chọn món
                      </span>
                    </div>
                    <div class="btn-light-sweep"></div>
                  </div>

                  <div class="product-info">
                    <b class="product-name">{{ item.tenSanPham }}</b>

                    <div class="product-price">
                      <template
                        v-if="item.giaSauGiam && item.giaSauGiam < item.gia"
                      >
                        <span class="old-price">
                          {{ item.gia.toLocaleString() }}đ
                        </span>

                        <span class="new-price">
                          {{ item.giaSauGiam.toLocaleString() }}đ
                        </span>
                      </template>

                      <template v-else>
                        <span class="new-price">
                          {{ (item.giaSauGiam || item.gia).toLocaleString() }}đ
                        </span>
                      </template>
                    </div>
                  </div>
                </div>
              </div>
              <div class="pagination">
                <button
                  :disabled="currentPage === 1"
                  @click="currentPage--"
                  class="pg-btn pg-arrow"
                >
                  <i class="ri-arrow-left-s-line"></i>
                </button>

                <template v-for="p in pageList" :key="p">
                  <button v-if="p === '…'" class="pg-btn pg-ellipsis" disabled>
                    ···
                  </button>
                  <button
                    v-else
                    class="pg-btn"
                    :class="{ active: p === currentPage }"
                    @click="currentPage = p"
                  >
                    {{ p }}
                  </button>
                </template>

                <button
                  :disabled="currentPage === totalPages"
                  @click="currentPage++"
                  class="pg-btn pg-arrow"
                >
                  <i class="ri-arrow-right-s-line"></i>
                </button>
              </div>
            </div>

            <!-- RIGHT -->
            <div class="selected-sidebar">
              <div class="selected-header">
                <h4>
                  <i class="ri-bowl-line" style="font-size: 15px"></i> Món đã
                  chọn
                </h4>
              </div>

              <!-- LIST -->
              <div class="selected-list">
                <div v-if="monDaChonDraft.length === 0" class="empty-summary">
                  <i class="bi bi-egg-fried"></i>
                  <p>Chưa có món nào được gọi</p>
                </div>
                <div
                  v-for="(item, index) in pagedMonDaChon"
                  :key="index"
                  class="selected-item"
                  :class="{ 'ngung-kinh-doanh': item.trangThaiXoa }"
                >
                  <img :src="getImgUrl(item.hinhAnh)" />
                  <div class="selected-info">
                    <b>{{ item.tenSanPham }}</b>
                    
                    <div v-if="item.trangThaiXoa" class="status-ngung-kinh-doanh-text text-danger fw-bold" style="font-size: 11px; margin-top: 4px;">
                      <i class="ri-error-warning-fill"></i> Món ăn ngừng kinh doanh
                    </div>

                    <div class="price-row">
                      <span style="color: #e53935; font-weight: 600">
                        {{
                          (
                            (item.giaSauGiam || item.gia) * item.soLuong
                          ).toLocaleString()
                        }}đ
                      </span>

                      <div class="quantity-box">
                        <button @click.stop="giamSoLuong(item, true)">-</button>
                        <span>{{ item.soLuong }}</span>
                        <button @click.stop="tangSoLuong(item)" :disabled="item.trangThaiXoa">+</button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- PHÂN TRANG - nằm ngoài selected-list -->
              <div
                v-if="totalPagesSelected > 1"
                class="pagination pagination-sm"
              >
                <button
                  :disabled="currentPageSelected === 1"
                  @click="currentPageSelected--"
                  class="pg-btn pg-arrow"
                >
                  <i class="ri-arrow-left-s-line"></i>
                </button>
                <template v-for="p in pageListSelected" :key="p">
                  <button v-if="p === '…'" class="pg-btn pg-ellipsis" disabled>
                    ···
                  </button>
                  <button
                    v-else
                    class="pg-btn"
                    :class="{ active: p === currentPageSelected }"
                    @click="currentPageSelected = p"
                  >
                    {{ p }}
                  </button>
                </template>
                <button
                  :disabled="currentPageSelected === totalPagesSelected"
                  @click="currentPageSelected++"
                  class="pg-btn pg-arrow"
                >
                  <i class="ri-arrow-right-s-line"></i>
                </button>
              </div>

              <!-- FOOTER WRAPPER -->
              <div class="selected-footer">
                <!-- WARNING BANNER -->
                <div v-if="hasProductChanged" class="alert-product-changed fw-bold" style="margin-top: 0; margin-bottom: 12px;">
                  <i class="ri-error-warning-fill"></i>
                  <span>{{ productChangedMessage || "Món ăn có sự thay đổi vui lòng kiểm tra thông tin trước khi xác nhận" }}</span>
                </div>

                <!-- TOTAL -->
                <div class="selected-total-row">
                  <span>Tổng tiền</span>
                  <b>{{ tongTienDraft.toLocaleString() }}đ</b>
                </div>

                <!-- BUTTONS -->
                <div class="modal-buttons-row">
                  <button class="btn btn-modal-cancel" @click="showModal = false">
                    <i class="bi bi-x-circle"></i> Hủy
                  </button>
                  <button
                    class="btn btn-modal-confirm"
                    :disabled="coMonNgungKinhDoanhDraft"
                    :class="{ 'btn-disabled': coMonNgungKinhDoanhDraft }"
                    @click="xacNhanChonMonModal"
                  >
                    <i class="ri-check-double-line"></i> Xác nhận
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
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

  <ChonBanLucTaoPhieu
    v-if="isModalOpen"
    :bookingData="bookingDataForTable"
    @close="isModalOpen = false"
    @success="handleSuccessXepBan"
  />
</template>

<style scoped>
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

/* ===============VALIDATE========= */
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

.input-error {
  border-color: #dc2626 !important;
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.1) !important;
}

.shake-enter-active {
  animation: shakeIn 0.4s ease;
}
.shake-leave-active {
  transition: opacity 0.2s ease;
}
.shake-leave-to {
  opacity: 0;
}

@keyframes shakeIn {
  0% {
    transform: translateX(0);
    opacity: 0;
  }
  20% {
    transform: translateX(-6px);
    opacity: 1;
  }
  40% {
    transform: translateX(6px);
  }
  60% {
    transform: translateX(-4px);
  }
  80% {
    transform: translateX(4px);
  }
  100% {
    transform: translateX(0);
  }
}

/* ================================== */

.empty-table-state {
  width: 100%;
  text-align: center;
  color: #aaa;
  font-style: italic;
  padding: 10px 0;
}

.add-table-tag {
  cursor: pointer;
  border: 1px dashed #ccc;
}
/* //////////////// CHỌN MÓN //////////// */
.empty-hint {
  font-size: 14px;
  text-align: center;
  font-style: italic;
  color: #9ca3af;
  margin-top: 8px;
  opacity: 0.9;
}
/* wrapper */
.empty-pick-minimal {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

/* button */
.luxury-add-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 10px;

  padding: 10px 18px;
  border-radius: 999px;

  background: linear-gradient(135deg, #ffffff, #fff7f7);
  border: 1px solid rgba(139, 30, 30, 0.15);

  font-size: 13.5px;
  font-weight: 600;
  letter-spacing: 0.6px;
  color: #8b1e1e;

  cursor: pointer;
  overflow: hidden;

  transition: all 0.25s ease;
}

/* hover */
.luxury-add-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(139, 30, 30, 0.12);
  border-color: #8b1e1e;
}

/* icon */
.luxury-add-btn i {
  font-size: 16px;
  transition: transform 0.25s ease;
}

/* icon hover nhẹ */
.luxury-add-btn:hover i {
  transform: rotate(90deg) scale(1.1);
}

/* text */
.btn-text {
  white-space: nowrap;
}

/* shine effect */
.shine-line {
  position: absolute;
  top: 0;
  left: -75%;
  width: 50%;
  height: 100%;

  background: linear-gradient(
    120deg,
    transparent,
    rgba(255, 255, 255, 0.6),
    transparent
  );

  transform: skewX(-20deg);
}

.luxury-add-btn:hover .shine-line {
  animation: shineMove 0.8s ease;
}

@keyframes shineMove {
  0% {
    left: -75%;
  }
  100% {
    left: 130%;
  }
}

.luxury-add-btn:active {
  transform: scale(0.96);
}
/* //////////////////// nÚT CHỘN VÍ TRÍ BÀN */
.layout-selector {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

.btn-spot-picker {
  display: inline-flex;
  align-items: center;
  gap: 10px;

  padding: 12px 18px;
  border-radius: 5px;

  background: rgb(213, 30, 30);
  border: 1px solid rgba(255, 255, 255, 0.08);

  color: #f5f5f5;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.3px;

  cursor: pointer;

  box-shadow: 0 10px 25px rgba(69, 68, 68, 0.35),
    inset 0 1px 0 rgba(245, 240, 240, 0.05);

  transition: all 0.25s ease;
  position: relative;
  overflow: hidden;
}

.btn-spot-picker i {
  font-size: 18px;
  color: #f8f7f7; /* vàng gold nhẹ kiểu nhà hàng */
  transition: transform 0.3s ease;
}

.btn-spot-picker:hover {
  transform: translateY(-2px);
  background: rgb(218, 37, 37);
  border-color: rgba(212, 175, 55, 0.35);
}

.btn-spot-picker:hover i {
  transform: scale(1.15) rotate(-5deg);
}

.btn-spot-picker:active {
  transform: translateY(0px) scale(0.98);
}

.btn-spot-picker::before {
  content: "";
  position: absolute;
  top: 0;
  left: -120%;
  width: 60%;
  height: 100%;
  background: linear-gradient(
    120deg,
    transparent,
    rgba(255, 255, 255, 0.08),
    transparent
  );
  transform: skewX(-20deg);
  transition: 0.6s;
}

.btn-spot-picker:hover::before {
  left: 120%;
}

/* /////////////// CHƯA CHỌN ĐỦ////////////// */

.alert-line-luxury {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: center;
  margin-top: 12px;
  padding: 12px 14px;

  border-radius: 5px;

  background: linear-gradient(
    135deg,
    rgba(255, 60, 60, 0.12),
    rgba(255, 140, 0, 0.08)
  );
  border: 1px solid rgba(255, 80, 80, 0.35);

  color: #ff4d4d;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.2px;

  box-shadow: 0 10px 25px rgba(150, 149, 149, 0.25),
    inset 0 0 0 1px rgba(243, 158, 158, 0.08);

  animation: shakeFade 0.35s ease-out;
}

.alert-icon-pulse {
  font-size: 22px;
  color: #ff3b3b;

  animation: warningPulse 0.9s infinite;
}

/* hiệu ứng xuất hiện có “warning feel” */
@keyframes shakeFade {
  0% {
    opacity: 0;
    transform: translateY(-6px);
  }
  40% {
    transform: translateX(-3px);
  }
  60% {
    transform: translateX(3px);
  }
  100% {
    opacity: 1;
    transform: translateX(0);
  }
}
/* /////////////////////////////////////////// */
.modal-filter-bar {
  padding: 16px 24px;
  border-bottom: 1px solid #eee;
}

/* DÀN NGANG CHUẨN */
.filter-group {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  width: 100%;
  gap: 16px;
}
/* SEARCH */
.search-box {
  position: relative;
}

.search-box input {
  width: 100%;
  padding: 10px 40px 10px 14px;
  border-radius: 10px;
  border: 1px solid #ddd;
  transition: all 0.25s;
  font-size: 14px;
}

.search-box input:focus {
  border-color: #8b1e1e;
  box-shadow: 0 0 0 2px rgba(139, 30, 30, 0.1);
  outline: none;
}

.icon-search {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #aaa;
}

/* SELECT */
.custom-select {
  width: 100%;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #ddd;
  background: white;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.25s;
}

.custom-select:hover {
  border-color: #8b1e1e;
}

.btn.btn-primary.btn-lg {
  position: relative;
  overflow: hidden;
}
.custom-select:focus {
  outline: none;
  border-color: #8b1e1e;
  box-shadow: 0 0 0 2px rgba(139, 30, 30, 0.1);
}

.btn-outline:hover {
  background: #f8fafc;
  color: #1e293b;
  border-color: #cbd5e1;
}

.btn-primary:hover:not(.btn-disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(139, 30, 30, 0.3);
  filter: brightness(1.1);
}

.btn.btn-primary.btn-lg:hover {
  transform: translateY(-2px);
  background-color: rgb(211, 42, 42);
  border-color: rgba(212, 175, 55, 0.45);

  box-shadow: 0 14px 30px rgba(0, 0, 0, 0.45),
    0 0 0 1px rgba(212, 175, 55, 0.15);
}

.btn.btn-primary.btn-lg::before {
  content: "";
  position: absolute;
  top: 0;
  left: -120%;
  width: 60%;
  height: 100%;

  background: linear-gradient(
    120deg,
    transparent,
    rgba(255, 255, 255, 0.12),
    transparent
  );

  transform: skewX(-20deg);
  transition: 0.6s;
}
.btn.btn-primary.btn-lg:hover::before {
  left: 120%;
}

.btn-disabled {
  background: #f3f4f6 !important;
  color: #9ca3af !important;
  cursor: not-allowed !important;
  box-shadow: none !important;
  transform: none !important;
  border: 1px solid #e5e7eb !important;
}

.btn-lg i {
  font-size: 18px;
}

.table-selector-wrapper {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.icon-circle {
  width: 38px;
  height: 38px;
  background: #8b1e1e;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.text-group {
  display: flex;
  flex-direction: column;
  text-align: left;
}

.main-text {
  font-size: 15px;
  font-weight: 700;
  color: #8b1e1e;
}

.hint-text {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}

.btn-outline {
  background: #ffffff;
  border: 1px solid #d1d5db;
  color: #374151;
}

.btn-outline:hover {
  background: #f1f5f9;
}
.action-container {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #eee;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.03);
}
.action-container button {
  flex: 1;
}

.btn-primary.disabled {
  background: #e5e7eb;
  color: #9ca3af;
  box-shadow: none;
}
.summary-actions {
  margin-top: 16px;
  padding: 16px 18px;
  border-top: 1px solid #f1f5f9;

  display: flex;
  flex-direction: column;
  gap: 10px;
}

.summary-actions .btn {
  width: 100%;
}

.summary-actions .btn-primary {
  justify-content: center;
}

.summary-actions .error-hint {
  font-size: 12px;
  color: #dc2626;
  text-align: center;
}
.error-hint {
  color: #dc2626;
  font-size: 13px;
  margin-top: 8px;
}
.btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
}
/* Tổng thể card */
.summary-card-modern {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #edf2f7;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  overflow: hidden;
}

/* Header xịn hơn */
.summary-header-clean {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: linear-gradient(to right, #fdf2f2, #ffffff);
  border-bottom: 1px solid #f1f5f9;
}

.header-icon-main {
  font-size: 20px;
  color: #d72e2e;
}

.header-text h3 {
  margin: 0;
  font-size: 19px;
  font-weight: 700;
  color: #1a202c;
}

/* Icon trong dòng thông tin */
.info-row i,
.block-title i,
.group-title i,
.total-row-modern i,
.deposit-row-modern i {
  width: 18px;
  margin-right: 8px;
  color: #d72e2e;
  text-align: center;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
}

.info-row span {
  color: #718096;
  display: flex;
  align-items: center;
}

/* Danh sách món ăn */
/* container bọc món ăn */
.dish-item-modern {
  position: relative; /* để nút xóa căn theo món này */
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  background: #ffffff;
  border: 1px solid #f1f5f9;
  border-radius: 12px;
  margin-bottom: 8px;
  transition: all 0.2s ease;
}

.remove-icon-btn {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 22px;
  height: 22px;
  background: #ef4444;
  color: white;
  border: 2px solid #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  cursor: pointer;
  opacity: 0;
  transform: scale(0.8);
  transition: all 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  box-shadow: 0 2px 6px rgba(239, 68, 68, 0.3);
  z-index: 10;
}

.dish-item-modern:hover .remove-icon-btn {
  opacity: 1;
  transform: scale(1);
}

.remove-icon-btn:hover {
  background: #dc2626;
  transform: scale(1.1) !important;
}

.dish-item-modern:hover {
  border-color: #fca5a5;
  background: #fff8f8;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
}
.dish-img-mini {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  object-fit: cover;
}

.dish-detail {
  flex: 1;
}

.dish-detail .name {
  display: block;
  font-size: 13px;
  font-weight: 600;
}

.dish-detail .price {
  font-size: 12px;
  color: #e53e3e;
  font-weight: 700;
}

.payment-summary-box {
  background: #fcfcfc;
  padding: 20px;
  border-top: 1px solid #f1f5f9;
}

.total-row-modern {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.total-price-text {
  font-size: 18px;
  color: #e63333;
  font-weight: 800;
}

.deposit-row-modern {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.deposit-input-wrapper-modern {
  position: relative;
  width: 120px;
}

.deposit-input-wrapper-modern input {
  width: 100%;
  padding: 6px 24px 6px 8px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  text-align: right;
  font-weight: 700;
}

.deposit-input-wrapper-modern .unit {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  color: #a0aec0;
}

.table-selector-wrapper {
  margin-top: 20px;
}

.table-btn {
  border: 1px dashed #d1d5db !important;
  background: #fdfdfd !important;
}

.table-btn:hover {
  border-style: solid;
  border-color: #8b1e1e !important;
  background: #fff5f5 !important;
}

.selected-tables-container {
  margin-top: 15px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 7px;
  border: 1px solid #edf2f7;
}

.container-label {
  font-size: 12px;
  font-weight: 600;
  color: #000000;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.badge-group-modern {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.confirm-pick-table-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px 20px;
  margin-top: 15px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.02);
}

.confirm-pick-table-box .confirm-content {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #334155;
  font-size: 14.5px;
  font-weight: 500;
}

.confirm-pick-table-box .icon-info {
  font-size: 20px;
  color: #3b82f6;
}

.confirm-pick-table-box .btn-primary {
  position: relative;
  overflow: hidden;
  padding: 8px 20px;
  font-size: 14px;
  border-radius: 6px;
  background-color: #df2b2b;
  border-color: #df2b2b;
  color: #fff;
  transition: all 0.3s ease;
}

.confirm-pick-table-box .btn-primary:hover {
  transform: translateY(-2px);
  background-color: rgb(211, 42, 42);
  border-color: rgba(212, 175, 55, 0.45);
  box-shadow: 0 6px 15px rgba(139, 30, 30, 0.3);
  filter: brightness(1.1);
}

.confirm-pick-table-box .btn-primary::before {
  content: "";
  position: absolute;
  top: 0;
  left: -120%;
  width: 60%;
  height: 100%;
  background: linear-gradient(
    120deg,
    transparent,
    rgba(255, 255, 255, 0.25),
    transparent
  );
  transform: skewX(-20deg);
  transition: 0.6s ease;
}

.confirm-pick-table-box .btn-primary:hover::before {
  left: 120%;
}

.table-tag-modern {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  background: linear-gradient(135deg, #fdf2f2, #ffffff);
  color: #b91c1c;
  border: 1px solid #fca5a5;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(220, 38, 38, 0.05);
  transition: all 0.25s ease;
  overflow: hidden;
}

.table-tag-modern i {
  font-size: 15px;
}

.table-tag-modern .remove-tag {
  cursor: pointer;
  padding: 2px;
  border-radius: 50%;
  transition: background 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.table-tag-modern:hover .remove-tag:hover {
  background: rgba(255, 255, 255, 0.2);
}

.table-tag-modern:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(220, 38, 38, 0.15);
  background: #b91c1c;
  color: #ffffff;
  border-color: #b91c1c;
}

.add-table-tag {
  cursor: pointer;
  border: 1px dashed #ef4444;
  background: #fff5f5;
  color: #ef4444;
}

.add-table-tag:hover {
  background: #ef4444;
  color: #ffffff;
  border-style: solid;
}

.icon-circle.shadow-sm {
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  background: #fff;
  color: #8b1e1e;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 18px;
}
.date-wrapper {
  position: relative;
}

.date-input {
  width: 100%;
  height: 44px;
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  padding: 0 40px 0 14px;
  font-size: 14px;
  transition: all 0.25s ease;
  background: #fff;
}

.date-input:focus {
  border-color: #ec4899;
  box-shadow: 0 0 0 3px rgba(236, 72, 153, 0.2);
  outline: none;
}

.date-icon {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #6b7280;
}
.datetime-wrapper {
  position: relative;
}

.datetime-input {
  width: 100%;
  height: 44px;
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  padding: 0 40px 0 14px;
  font-size: 14px;
  background: #fff;
  transition: all 0.25s ease;
}

.datetime-input:focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.2);
  outline: none;
}

.datetime-icon {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 18px;
  color: #6b7280;
}
/* ===== BOX ===== */

.summary-header-clean h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
}

.summary-header-clean span {
  font-size: 12px;
  color: #777;
}

/* ===== BLOCK ===== */
.summary-block {
  padding: 16px 18px;
  border-bottom: 1px solid #f3f4f6;
}

.block-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #555;
}

/* ===== INFO ===== */
.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 13px;
}

.info-row span {
  color: #888;
}

.info-row b {
  font-weight: 600;
}

/* ===== DISH ===== */
.dish-group {
  margin-bottom: 12px;
}

.group-title {
  font-size: 14px;
  color: #675757;
  margin-bottom: 6px;
}

.dish-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.close-modal {
  width: 36px;
  height: 36px;
  border-radius: 10px;

  border: 1px solid #eee;
  background: #f8fafc;

  font-size: 20px;
  font-weight: 500;
  color: #555;

  display: flex;
  align-items: center;
  justify-content: center;

  cursor: pointer;
  transition: all 0.2s;
}

.close-modal:hover {
  background: #fee2e2;
  border-color: #fca5a5;
  color: #dc2626;
}
.done-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(139, 30, 30, 0.2);
}

.loading-overlay {
  position: fixed;
  inset: 0;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(5px);
  display: flex;
  justify-content: center;
  align-items: center;
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
  gap: 8px;
  margin-bottom: 25px;
}

.dot-loader span {
  width: 10px;
  height: 10px;
  background-color: #e83737;
  border-radius: 50%;
  display: inline-block;
  opacity: 0.3;
  animation: dotPulse 1.4s infinite ease-in-out both;
}

.dot-loader span:nth-child(1) {
  animation-delay: -0.32s;
}

.dot-loader span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes dotPulse {
  0%,
  80%,
  100% {
    transform: scale(0);
    opacity: 0.3;
  }
  40% {
    transform: scale(1);
    opacity: 1; /* Sáng lên và phóng to */
  }
}

/* Animations khác */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideInUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
/* --------------------- */
.box-header-card {
  background: var(--card-bg);
  border-radius: 7px;
  padding: 20px 24px;
  margin-bottom: 20px;

  border: 1px solid var(--border-color);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
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
}

.icon-btn-back img {
  width: 18px;
}

/* ==========================================================
   BIẾN MÀU SẮC & FONT (GIỮ NGUYÊN TỪ FILE CŨ)
   ========================================================== */
:global(body) {
  margin: 0;
  font-family: "Inter", -apple-system, BlinkMacSystemFont, sans-serif;
  -webkit-font-smoothing: antialiased;
}

.page-container {
  --primary-color: #8b1e1e; /* Màu đỏ đô của bạn */
  --primary-hover: #a12525;
  --bg-color: #f4f6f8; /* Nền xám nhạt hiện đại hơn */
  --text-main: #1a1f23;
  --text-sub: #637381;
  --border-color: #e1e8ed;
  --card-bg: #ffffff;

  width: 100%;

  background-color: var(--bg-color);
  min-height: 100vh;
  box-sizing: border-box;
}

/* ==========================================================
   GLOBAL COMPONENTS (INPUT, BUTTON, CARD)
   ========================================================== */

.box {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
  border: 1px solid #eee;
}

.icon-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  border: none;
  background: #f4f6f8;
  cursor: pointer;
}
.card {
  background: var(--card-bg);
  padding: 20px;
  border-radius: 4px; /* Bo góc hiện đại */
  border: 1px solid var(--border-color);
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05); /* Bóng đổ nhẹ */
}

.custom-input {
  width: 100%;
  height: 42px;
  box-sizing: border-box;
  padding: 0 12px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  font-size: 14px;
  outline: none;
  transition: all 0.22s ease;
  color: var(--text-main);
}
.custom-input:hover {
  border-color: #c8d0dc;
}
.custom-input:focus {
  border-color: var(--color-primary, #e65a5a);
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12);
}

.textarea {
  resize: vertical;
  line-height: 1.5;
  height: auto;
  min-height: 100px;
  padding: 12px;
}

.field-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-main);
  margin-bottom: 6px;
}
.field-label.require::after {
  content: " *";
  color: var(--primary-color);
}

.btn {
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-lg {
  padding: 12px 28px;
  font-size: 15px;
}

.btn-primary {
  background: rgb(213, 30, 30);
  color: white;
}

.btn-outline {
  background: white;
  border-color: var(--border-color);
  color: var(--text-main);
}
.btn-outline:hover {
  background: #f9fafb;
  border-color: #c4cdd5;
}

.divider {
  display: flex;
  align-items: center;
  text-align: center;
  margin: 20px 0;
  color: rgb(100, 86, 86);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 1px;
}
.divider::before,
.divider::after {
  content: "";
  flex: 1;
  border-bottom: 1px dashed var(--border-color);
}
.divider span {
  padding: 0 10px;
}

.content-layout {
  display: grid;
  grid-template-columns: minmax(0, 2fr) 380px; /* Cột phải cố định chiều rộng */
  gap: 24px;
  align-items: start;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid var(--border-color);
}

.step-indicator {
  background: #f7e9e9;
  color: rgb(232, 46, 46);
  font-weight: 700;
  font-size: 15px;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-main);
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

/* Search Khách hàng nhanh */
.search-section {
  margin-bottom: 10px;
}
.search-wrapper {
  position: relative;
}

.search-input {
  padding-left: 40px;
  height: 42px;
}

.search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-sub);
  font-size: 16px;
}

.dropdown-list {
  position: absolute;
  top: calc(100% + 5px);
  left: 0;
  width: 100%;
  background: white;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  z-index: 100;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  max-height: 250px;
  overflow-y: auto;
}

.dropdown-item {
  padding: 12px 16px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f4f6f8;
}

.dropdown-item:last-child {
  border-bottom: none;
}

.dropdown-item:hover {
  background: #fdf2f2;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.item-name {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-main);
}

.item-phone {
  font-size: 12px;
  color: var(--text-sub);
}

.dropdown-item:hover .choose-text {
  opacity: 1;
}

.dropdown-item.empty {
  text-align: center;
  color: #9ca3af;
  font-style: italic;
  justify-content: center;
  cursor: default;
}
/* NÚT CHỌN BÀN */
.choose-table-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.icon-circle {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #8b1e1e;
  color: white;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.choose-text {
  color: #8b1e1e;
  font-weight: 600;
  font-size: 14px;
}

.choose-table-btn:hover .icon-circle {
  background: #a62b2b;
}

.choose-text {
  font-size: 12px;
  color: var(--primary-color);
  font-weight: 600;
  opacity: 0;
  transition: opacity 0.2s;
}
.icon-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: white;
  border: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
  font-size: 20px;
  font-weight: bold;
  transition: all 0.2s;
}

.text-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
  text-align: left;
}

.main-text {
  font-weight: 700;
  font-size: 15px;
  color: var(--primary-color);
}

.hint-text {
  font-size: 13px;
  color: var(--text-sub);
}

.action-spacer {
  height: 20px;
}

/* ==========================================================
   CỘT PHẢI: TÓM TẮT & MÓN CHỌN (STICKY & ĐẸP)
   ========================================================== */

.sidebar-sticky {
  position: sticky;
  top: 20px;
  z-index: 10;
}

/* Phần món ăn đã chọn trong tóm tắt */
.section-title {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 700;
  color: var(--text-main);
}

/* Thanh toán */
.payment-section {
  background: #fafbfc;
  border-bottom: none;
}

.total-price {
  font-size: 20px;
  font-weight: 800;
  color: var(--primary-color);
}

.deposit-input-wrapper {
  position: relative;
  width: 140px;
}
.deposit-input {
  text-align: right;
  padding-right: 30px;
  font-weight: 700;
  color: var(--text-main);
  height: 38px;
}
.deposit-input-wrapper .currency {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 13px;
  color: var(--text-sub);
  font-weight: 600;
}

/* ==========================================================
   2. FIXED BOTTOM ACTIONS (NÚT XUỐNG GÓC PHẢI)
   ========================================================== */
.fixed-bottom-actions {
  position: static;
  margin-top: 20px;
  display: block;
}
.action-container {
  display: flex;
  gap: 12px;
  max-width: 100%;
}
.action-container .btn {
  flex: 1;
  white-space: nowrap;
  font-size: 14px !important;
  padding: 10px 16px !important;
}

/* ===== SELECT 2 && TIM KIEM ===== */
/* Ghi đè triệt để các viền mặc định của thư viện */
:deep(.v-select) {
  background: #fff;
  width: 100%;
}

/* Khung chính: Xóa border mặc định, dùng border của mình */
:deep(.vs__dropdown-toggle) {
  border: 1px solid #e5e7eb !important; /* Màu viền giống ô Trạng thái */
  border-radius: 10px !important;
  padding: 0 !important;
  height: 42px; /* Chiều cao khớp hoàn toàn với input/select thường */
  display: flex;
  align-items: center;
  transition: all 0.2s ease;
  box-shadow: none !important;
}

/* Xóa viền đỏ/xanh khi focus mà thư viện tự thêm vào search input */
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

/* Hiệu ứng khi mở menu (Focus) */
:deep(.vs--open .vs__dropdown-toggle) {
  border-color: #3b6cff !important;
  box-shadow: 0 0 0 3px rgba(59, 108, 255, 0.1) !important;
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
  color: #3b6cff !important;
}

:deep(.vs__dropdown-option--selected) {
  background: #e8efff !important;
  color: #3b6cff !important;
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

.custom-select {
  height: 42px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  padding: 0 12px;
  font-size: 14px;
  background: #fff;
  cursor: pointer;
}

.custom-select:focus {
  border-color: #8b1e1e;
  outline: none;
}

.search-box input {
  padding-right: 35px;
}

.search-icon-inside {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #aaa;
}

/* //////////////////// CHỌN MÓN /////////////// */

/* // TÁB */
/* ================= TABS ================= */

.tabs-header {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;

  width: fit-content;

  margin: 18px auto 14px;

  padding: 6px;

  background: #f8fafc;
  border: 1px solid #e2e8f0;

  border-radius: 18px;
}
.tabs-header button {
  width: 180px;

  height: 48px;

  border: none;
  border-radius: 14px;

  background: transparent;

  display: flex;
  justify-content: center;
  align-items: center;

  font-size: 14px;
  font-weight: 600;

  color: #64748b;

  cursor: pointer;

  transition: all 0.22s ease;

  position: relative;
}
/* hover */
.tabs-header button:hover {
  background: rgba(255, 255, 255, 0.85);

  color: #0f172a;
}

/* active */
.tabs-header button.active {
  background: linear-gradient(135deg, #991b1b, #dc2626);

  color: white;

  box-shadow: 0 10px 20px rgba(220, 38, 38, 0.18), 0 2px 4px rgba(0, 0, 0, 0.06);

  transform: translateY(-1px);
}

/* active glow */
.tabs-header button.active::after {
  content: "";

  position: absolute;

  inset: 0;

  border-radius: inherit;

  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.25);

  pointer-events: none;
}

.big-modal {
  width: 92vw;
  max-width: 1500px;
  height: 92vh;
  border-radius: 18px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-main-layout {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.modal-body-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.selected-header {
  padding: 18px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 15px;
  font-weight: 600;
}

.selected-item {
  display: flex;
  gap: 12px;
  align-items: center;
  background: white;
  border-radius: 12px;
  padding: 10px;
  margin-bottom: 12px;
}

.selected-item img {
  width: 58px;
  height: 58px;
  object-fit: cover;
  border-radius: 10px;
}

.selected-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.selected-info .price-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.quantity-box {
  display: flex;
  align-items: center;
  gap: 8px;
}

.quantity-box button {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 6px;
  background: #f1f5f9;
  cursor: pointer;
  font-size: 14px;
  font-weight: 700;
  transition: 0.2s;
}

.quantity-box button:hover {
  background: #e2e8f0;
}
.remove-btn {
  border: none;
  background: #ffebee;
  color: #e53935;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.55);
  backdrop-filter: blur(6px);
  z-index: 9999;
}

/* QUAN TRỌNG: center bằng transform */
.modern-modal {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);

  width: min(1000px, 92vw);
  height: min(85vh, 800px);

  background: #fff;
  border-radius: 20px;

  display: flex;
  flex-direction: column;
  overflow: hidden;

  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

@keyframes modalZoom {
  from {
    transform: scale(0.96) translateY(10px);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.advanced-filter {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  border-bottom: 1px solid #eee;
}

.filter-item {
  flex: 1;
  position: relative;
}
.filter-item input,
.filter-item select {
  width: 100%;
  height: 42px;
  padding: 0 14px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  box-sizing: border-box;
  display: block;
}

.modal-filter-bar label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 6px;
  display: block;
}
.modal-header-bar,
.modal-filter-bar {
  flex-shrink: 0;
}
.modal-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;

  padding: 18px 24px;
  border-bottom: 1px solid #eee;

  font-weight: 600;
  font-size: 16px;
}

/* Grid sản phẩm có ảnh */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.product-item-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;

  border: 1px solid #eee;

  display: flex;
  flex-direction: column;

  transition: all 0.25s ease;
}

.product-item-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
  border-color: #8b1e1e;
}

.product-img-box {
  width: 100%;
  aspect-ratio: 1 / 1; /* QUAN TRỌNG */
  position: relative;
  background: #f1f5f9;
  overflow: hidden;
}

.product-img-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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
.product-img-box:hover .btn-light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
}
.product-img-box {
  position: relative;
  overflow: hidden;
}
.table-tag-modern:hover .btn-light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
}
.table-tag-modern {
  position: relative;
  overflow: hidden;
}

.product-info {
  padding: 12px;
  text-align: center;
}

.product-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;

  display: -webkit-box;
  -webkit-line-clamp: 2; /* max 2 dòng */
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-price {
  margin-top: 4px;
  font-weight: 700;
  color: #8b1e1e;
}

.new-price {
  color: #c0392b;
  font-weight: bold;
  font-size: 1.1rem;
}

.old-price {
  font-size: 0.85rem;
  color: #999;
  text-decoration: line-through;
}
.hover-add {
  position: absolute;
  inset: 0;

  background: linear-gradient(
    to top,
    rgba(15, 23, 42, 0.68),
    rgba(15, 23, 42, 0.18)
  );
  display: flex;
  align-items: center;
  justify-content: center;

  opacity: 0;
  transition: all 0.25s ease;
}

.product-item-card:hover .hover-add {
  opacity: 1;
}

/* button */
.hover-add span {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 18px;
  border-radius: 9px;
  background: rgba(207, 143, 143, 0.12);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  color: white;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.5px;
  transform: translateY(8px) scale(0.96);
  transition: all 0.25s ease;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.18);
}

/* hover animation */
.product-item-card:hover .hover-add span {
  transform: translateY(0) scale(1);
}
.hover-add span i {
  font-size: 18px;
}
.modal-body-content {
  flex: 1; /* QUAN TRỌNG */
  overflow-y: auto;
  padding: 16px 20px;
}

.modal-body-content::-webkit-scrollbar {
  width: 6px;
}

.modal-body-content::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 10px;
}

.modal-filter-bar {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  border-bottom: 1px solid var(--border-color);
}

.modal-filter-bar .search-box {
  position: relative;
  flex: 1;
}
.search-icon-inside {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #ccc;
}
.category-select {
  width: 200px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  padding-right: 30px;
}

.product-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-main);
  line-height: 1.4;
}

.product-price {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: var(--primary-color);
}

.add-btn-small {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 1px solid var(--border-color);
  background: white;
  color: var(--primary-color);
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}
.product-item-card:hover .add-btn-small {
  background: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

/* Responsive cơ bản */
@media (max-width: 1024px) {
  .content-layout {
    grid-template-columns: 1fr;
  }
  .sidebar-sticky {
    position: static;
    width: 100%;
  }
}

@media (max-width: 600px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
  .modal-filter-bar {
    flex-direction: column;
  }
  .category-select {
    width: 100%;
  }
  .fixed-bottom-actions {
    justify-content: center;
  }
  .action-container {
    width: 100%;
  }
  .action-container button {
    flex: 1;
  }
}

/* ================= FULLSCREEN MODAL ================= */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(4px);

  display: flex;
  justify-content: center;
  align-items: center;

  z-index: 99999;

  padding: 10px;
}

.modal.modern-modal.big-modal {
  width: 98vw !important;
  max-width: 1300px !important;

  height: 96vh !important;
  max-height: 96vh !important;

  border-radius: 10px;
  overflow: hidden;

  background: white;

  display: flex;
  flex-direction: column;

  margin: 0 !important;
}

.modal-main-layout {
  flex: 1;
  display: flex;
  overflow: hidden;
  min-height: 0;
}

.modal-body-content {
  flex: 1;
  overflow-y: auto;

  padding: 22px;
}

.product-grid {
  display: grid;

  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));

  gap: 18px;
}

.product-item-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;

  border: 1px solid #eee;

  transition: all 0.25s ease;
  cursor: pointer;
}

.product-item-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.08);
}

.product-img-box {
  height: 180px;
  overflow: hidden;
  position: relative;
}

.product-img-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.selected-header {
  padding: 20px;
  border-bottom: 1px solid #eee;

  display: flex;
  justify-content: space-between;
  align-items: center;
}

.selected-item {
  display: flex;
  align-items: center;
  gap: 12px;

  background: white;

  border-radius: 14px;

  padding: 10px;

  margin-bottom: 12px;

  border: 1px solid #f1f1f1;
}

.selected-item img {
  width: 65px;
  height: 65px;
  object-fit: cover;

  border-radius: 12px;
}

.selected-sidebar {
  width: 420px;
  min-width: 420px;

  display: flex;
  flex-direction: column;

  height: 100%;
  background: #fafafa;

  border-left: 1px solid #eee;

  overflow: hidden;
}
.selected-total {
  flex-shrink: 0;

  padding: 18px 20px;

  background: white;

  border-top: 1px solid #eee;

  display: flex;
  justify-content: space-between;
  align-items: center;

  font-size: 18px;
  font-weight: 700;

  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.04);
}

.selected-sidebar {
  display: flex;
  flex-direction: column;

  height: 100%;
  overflow: hidden;
}

.selected-list {
  flex: 1;
  overflow-y: auto;

  padding: 16px;

  min-height: 0;
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
.selected-total {
  flex-shrink: 0;

  padding: 18px;

  background: white;

  border-top: 1px solid #eee;

  display: flex;
  justify-content: space-between;
  align-items: center;

  font-size: 18px;
  font-weight: 700;
}

.quantity-control-modern {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
}

.quantity-control-modern button {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 6px;
  background: #f3f4f6;
  cursor: pointer;
  transition: 0.2s;
}

.quantity-control-modern button:hover {
  background: #e5e7eb;
}

.quantity-control-modern span {
  min-width: 18px;
  text-align: center;
  font-weight: 600;
}

.dish-bottom-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 4px;
}

.quantity-inline {
  display: flex;
  align-items: center;
  gap: 6px;
}

.quantity-inline button {
  width: 22px;
  height: 22px;
  border: none;
  border-radius: 6px;
  background: #f3f4f6;
  cursor: pointer;
  transition: 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quantity-inline button:hover {
  background: #e5e7eb;
}

.quantity-inline span {
  min-width: 16px;
  text-align: center;
  font-size: 13px;
  font-weight: 600;
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

/* ///////////////////// HEADER ADD ////////// */
.main-title {
  display: flex;
  align-items: center;
  font-size: 28px;
  font-weight: 600;
  gap: 12px;
  letter-spacing: 1px;
  margin-bottom: 5px;
  color: #1a1a1a;
}

.icon-wrapper {
  width: 56px;
  height: 56px;
  flex-shrink: 0;

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
.box-header {
  margin-bottom: 16px;
}

.box {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 16px;
  margin-bottom: 20px;
  border: 1px solid #eee;
}

.box-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center; /* căn giữa dọc */
}

.box-header {
  margin-bottom: 0;
}

.icon-btn-back {
  margin-left: auto;
  flex-shrink: 0;
}

/* ===== RESPONSIVE EXTRA ===== */
@media (max-width: 1024px) {
  .modal.modern-modal.big-modal {
    width: 96vw !important;
    height: 94vh !important;
    max-height: 94vh !important;
  }

  .modal-main-layout {
    flex-direction: column !important;
    overflow-y: auto !important;
  }

  .selected-sidebar {
    width: 100% !important;
    min-width: 100% !important;
    height: auto !important;
    border-left: none !important;
    border-top: 1px solid #eee !important;
  }

  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)) !important;
  }
}

@media (max-width: 768px) {
  .main-title {
    font-size: 20px !important;
    flex-wrap: wrap !important;
  }

  .icon-wrapper {
    width: 44px !important;
    height: 44px !important;
  }

  .box-header-flex {
    flex-direction: column !important;
    gap: 10px !important;
    align-items: flex-start !important;
  }

  .icon-btn-back {
    margin-left: 0 !important;
  }

  .summary-card-modern {
    margin-top: 20px !important;
  }

  .product-img-box {
    height: 140px !important;
  }
}

.selected-item.ngung-kinh-doanh {
  opacity: 0.5;
  filter: grayscale(1);
}

.quantity-box button:disabled {
  background: #e5e7eb !important;
  color: #9ca3af !important;
  cursor: not-allowed;
  pointer-events: none;
}

.alert-product-changed {
  background: rgba(220, 38, 38, 0.05);
  border: 1px solid rgba(220, 38, 38, 0.15);
  border-radius: 8px;
  padding: 12px 14px;
  color: #b91c1c;
  font-size: 13px;
  line-height: 1.45;
  margin-top: 12px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.04);
  text-align: left;
}
.alert-product-changed i {
  font-size: 18px;
  color: #dc2626;
  flex-shrink: 0;
}
.btn-modal-cancel {
  flex: 1;
  background: #ffffff !important;
  border: 1px solid #d1d5db !important;
  color: #374151 !important;
  padding: 10px 16px !important;
  font-size: 14px !important;
  border-radius: 8px !important;
  font-weight: 600 !important;
  white-space: nowrap !important;
  transition: all 0.2s !important;
}
.btn-modal-cancel:hover {
  background: #f9fafb !important;
  border-color: #9ca3af !important;
}

.btn-modal-confirm {
  flex: 1;
  background: rgb(213, 30, 30) !important;
  border: 1px solid rgb(213, 30, 30) !important;
  color: #ffffff !important;
  padding: 10px 16px !important;
  font-size: 14px !important;
  border-radius: 8px !important;
  font-weight: 600 !important;
  white-space: nowrap !important;
  transition: all 0.2s !important;
  position: relative !important;
  overflow: hidden !important;
}
.btn-modal-confirm:hover:not(:disabled) {
  background: rgb(211, 42, 42) !important;
  border-color: rgb(211, 42, 42) !important;
  transform: translateY(-2px) !important;
  box-shadow: 0 6px 15px rgba(213, 30, 30, 0.3) !important;
}
.btn-modal-confirm::before {
  content: "";
  position: absolute;
  top: 0;
  left: -120%;
  width: 60%;
  height: 100%;
  background: linear-gradient(
    120deg,
    transparent,
    rgba(255, 255, 255, 0.12),
    transparent
  );
  transform: skewX(-20deg);
  transition: 0.6s;
}
.btn-modal-confirm:hover:not(:disabled)::before {
  left: 120%;
}

.selected-footer {
  flex-shrink: 0;
  background: white;
  border-top: 1px solid #eee;
  padding: 18px 20px 20px 20px;
}

.selected-total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 14px;
}

.modal-buttons-row {
  display: flex;
  gap: 10px;
  width: 100%;
}

.table-tag-invalid {
  background: linear-gradient(135deg, #fffbeb, #ffffff) !important;
  color: #d97706 !important;
  border-color: #fcd34d !important;
  box-shadow: 0 0 8px rgba(217, 119, 6, 0.25) !important;
}

.table-tag-invalid:hover {
  background: #d97706 !important;
  color: #ffffff !important;
  border-color: #d97706 !important;
  box-shadow: 0 4px 8px rgba(217, 119, 6, 0.4) !important;
}

.alert-line-warning {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin-top: 12px;
  padding: 12px 14px;
  border-radius: 5px;
  background: linear-gradient(
    135deg,
    rgba(217, 119, 6, 0.12),
    rgba(245, 158, 11, 0.08)
  );
  border: 1px solid rgba(217, 119, 6, 0.35);
  color: #d97706;
  font-size: 13px;
  font-weight: 500;
  width: 100%;
}
</style>
