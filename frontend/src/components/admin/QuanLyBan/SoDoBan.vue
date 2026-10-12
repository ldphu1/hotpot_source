<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from "vue"; // Thay đường dẫn thực tế của bạn vào đây
import { Modal } from "bootstrap";
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";
import SuaBanDaTao from "./SuaBanDaTao.vue";
import {
  getAllquanlyban,
  getBanChiTiet,
  themBanAn,
  xoaBan,
  capNhatViTriBan,
  importExcelBanAn,
  exportBanAn,
  downloadTemplateExcel,
} from "@/api/admin/quanlyban/quanlybanApi.js";
import {
  getAllkhuvuc,
  xoaKhuVuc,
  themKhuVuc,
} from "@/api/admin/khuvuc/khuVucApi";
import ThemBan from "./ThemBan.vue";
import { getDanhSachBan } from "@/api/admin/checkin/checkinapi.js";

// Icon
import iconWifi from "@/assets/icons/wifi.png";
import iconAdd from "@/assets/icons/add.png";
import iconDanhSach from "@/assets/icons/danh-sach.png";
import iconExcel from "@/assets/icons/excel.png";
import { useUtils } from "@/utils/helper";
import { hasPermission } from "@/utils/permission.js";
import router from "@/router/index.js";
import { connectWebSocket, disconnectWebSocket } from "@/utils/websocket";

const { notify, formatCurrency } = useUtils();
// 1. Khai báo hằng số lên đầu
const SO_COT = 6;

const danhSachBan = ref([]);
const danhSachTang = ref([]);
const tangDangChon = ref(null);

const isTangNgungHoatDong = computed(() => {
  const tang = danhSachTang.value.find((t) => t.id === tangDangChon.value);
  return tang ? tang.trangThai === 0 : false;
});

const viTriBan = ref({});
const slotDangChon = ref(null);
const trangHienTai = ref(1);
const soDongMoiTrang = ref(5);

const currentTime = ref(new Date());
let timerInterval;

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

const loadDanhSachBanVaHoaDon = async () => {
  const resBan = await getAllquanlyban();
  let checkInData = [];
  try {
    const resCheckIn = await getDanhSachBan();
    checkInData = resCheckIn.data || [];
  } catch (e) {
    console.error("Lỗi load data checkin", e);
  }

  return resBan.data.map((ban) => {
    const banCheckIn = checkInData.find(
      (b) => Number(b.idBanAn || b.idBan) === Number(ban.id),
    );
    if (banCheckIn) {
      return {
        ...ban,
        dsHoaDon: banCheckIn.dsHoaDon
          ? sapXepHoaDonTheoThoiGianMo(banCheckIn.dsHoaDon)
          : [],
        maHoaDon: banCheckIn.maHoaDon,
        tongTien: banCheckIn.tongTien,
        tienConLai: banCheckIn.tienConLai,
        coNhieuHoaDon: banCheckIn.coNhieuHoaDon,
        soHoaDonDangHoatDong: banCheckIn.soHoaDonDangHoatDong,
        thoiGianCheckIn: banCheckIn.thoiGianCheckIn,
        thoiGianMo: banCheckIn.thoiGianMo,
        thoiGianDat: banCheckIn.thoiGianDat,
      };
    }
    return ban;
  });
};

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

const goFirst = () => (trangHienTai.value = 1);
const goPrev = () => {
  if (trangHienTai.value > 1) trangHienTai.value--;
};
const goNext = () => {
  if (trangHienTai.value < tongTrang.value) trangHienTai.value++;
};
const goLast = () => (trangHienTai.value = tongTrang.value);

const showThemBan = ref(false);
const themBanRef = ref(null);
const reloadKey = ref(0);
/* ===== cbo bàn ===== */
const danhSachKhuVuc = ref([]);

// sửa bàn
const showSuaBan = ref(false);
const banDangSua = ref(null);
const suaBanRef = ref(null);

const errorsTang = ref({
  tenKhuVuc: "",
  viTri: "",
});

// tầng
const showChiTietTang = ref(false);
const showModalThemTang = ref(false);
const showModalSuaTang = ref(false);
const tangDangSua = ref(null);
const formTang = ref({
  tenKhuVuc: "",
  viTri: "",
  moTa: "",
  trangThai: 1,
});

// Bootstrap Modals Refs
const modalThemTangRef = ref(null);
let modalThemTangInstance = null;

const modalSuaTangRef = ref(null);
let modalSuaTangInstance = null;

const modalChiTietTangRef = ref(null);
let modalChiTietTangInstance = null;

onMounted(async () => {
  timerInterval = setInterval(() => {
    currentTime.value = new Date();
  }, 1000);

  // Init Modals
  modalThemTangInstance = new Modal(modalThemTangRef.value);
  modalSuaTangInstance = new Modal(modalSuaTangRef.value);
  modalChiTietTangInstance = new Modal(modalChiTietTangRef.value);

  modalThemTangRef.value.addEventListener("hidden.bs.modal", () => {
    showModalThemTang.value = false;
  });
  modalSuaTangRef.value.addEventListener("hidden.bs.modal", () => {
    showModalSuaTang.value = false;
  });
  modalChiTietTangRef.value.addEventListener("hidden.bs.modal", () => {
    showChiTietTang.value = false;
  });
});

//  nhận dữ liệu thêm bàn từ ThemBan
const handleThemBan = async (banMoi) => {
  const isDuplicate = danhSachBan.value.some(
    (b) =>
      b.khuVuc?.id === banMoi.khuVuc.id &&
      b.tenBanAn.trim().toLowerCase() === banMoi.tenBanAn.trim().toLowerCase(),
  );
  if (isDuplicate) {
    themBanRef.value?.setErrors({
      ten_ban_an: "Tên bàn đã tồn tại trong khu vực này",
    });
    return;
  }

  try {
    await themBanAn(banMoi);

    notify.success("Thêm bàn thành công!");

    showThemBan.value = false;
    reloadKey.value++;

    danhSachBan.value = await loadDanhSachBanVaHoaDon();

    khoiTaoSlot();
  } catch (e) {
    const errorMessage =
      typeof e.response?.data === "string"
        ? e.response.data
        : e.response?.data?.message;
    notify.error(errorMessage || "Khu vực này đã đủ 18 bàn");
    throw e;
  }
};

const handleWsMessage = async (data) => {
  try {
    danhSachBan.value = await loadDanhSachBanVaHoaDon();
    khoiTaoSlot();
  } catch (e) {
    console.error("Lỗi reload WebSocket", e);
  }
};

onMounted(async () => {
  try {
    // 1. Load khu vực
    const resKhuVuc = await getAllkhuvuc();
    danhSachKhuVuc.value = resKhuVuc.data;
    danhSachTang.value = resKhuVuc.data;

    // 2. Load bàn & Đồng bộ LocalStorage
    danhSachBan.value = await loadDanhSachBanVaHoaDon();

    // 3. Kích hoạt tầng đầu tiên
    if (danhSachTang.value.length > 0) {
      tangDangChon.value = danhSachTang.value[0].id;
      khoiTaoSlot();
    }
  } catch (error) {
    notify.error("Lỗi tải dữ liệu ban đầu");
  }

  // Realtime
  connectWebSocket(null, handleWsMessage);
});

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval);
  if (modalThemTangInstance) modalThemTangInstance.dispose();
  if (modalSuaTangInstance) modalSuaTangInstance.dispose();
  if (modalChiTietTangInstance) modalChiTietTangInstance.dispose();

  disconnectWebSocket(handleWsMessage);
});

watch(showModalThemTang, (val) => {
  if (val) modalThemTangInstance?.show();
  else modalThemTangInstance?.hide();
});

watch(showModalSuaTang, (val) => {
  if (val) modalSuaTangInstance?.show();
  else modalSuaTangInstance?.hide();
});

watch(showChiTietTang, (val) => {
  if (val) modalChiTietTangInstance?.show();
  else modalChiTietTangInstance?.hide();
});

// 2. Các hàm bổ trợ
const getTenTrangThai = (trangThai) => {
  const map = {
    0: "Ngừng hoạt động",
    1: "Trống",
    2: "Đã đặt",
    3: "Đang sử dụng",
    4: "Đang dọn",
  };
  return map[trangThai] || "Không xác định";
};

const excelInputRef = ref(null);

const uploadExcel = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  const formData = new FormData();
  formData.append("file", file);

  try {
    const response = await importExcelBanAn(formData);
    notify.success("Import thành công " + response.data.soLuong + " bàn!");

    const resBan = await getAllquanlyban();
    danhSachBan.value = resBan.data;
    khoiTaoSlot();
  } catch (error) {
    console.error("Lỗi import", error);
    const msg = error.response?.data;
    notify.error(typeof msg === "string" ? msg : "Lỗi import file Excel");
  } finally {
    if (excelInputRef.value) {
      excelInputRef.value.value = null;
    }
  }
};

const handleExportExcel = async () => {
  try {
    const response = await exportBanAn("", null, null); // Sơ đồ bàn không lọc từ khóa nên truyền null
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", "Danh_Sach_Ban_An.xlsx");
    document.body.appendChild(link);
    link.click();
    link.remove();
    notify.success("Xuất Excel thành công!");
  } catch (error) {
    console.error("Lỗi xuất Excel:", error);
    notify.error("Lỗi xuất Excel!");
  }
};

const handleDownloadTemplate = async () => {
  try {
    const response = await downloadTemplateExcel();
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", "Mau_Them_Ban.xlsx");
    document.body.appendChild(link);
    link.click();
    link.remove();
    notify.success("Tải mẫu Excel thành công!");
  } catch (error) {
    console.error("Lỗi tải mẫu Excel:", error);
    notify.error("Lỗi tải mẫu Excel!");
  }
};

const moThemBan = () => {
  if (!hasPermission("THEM_BAN")) {
    router.push("/403");
    return;
  }
  showThemBan.value = true;
};

const moKhuVuc = () => {
  if (!hasPermission("THEM_KHU_VUC")) {
    router.push("/403");
    return;
  }
  showChiTietTang.value = true;
};

// Hàm map class CSS theo trạng thái
const getClassTrangThai = (trangThai) => {
  const map = {
    0: "ngung_su_dung",
    1: "trong",
    2: "da_dat",
    3: "co_khach",
    4: "dang_don",
  };
  return map[trangThai] || "";
};

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
    // Ưu tiên xếp bàn lớn lên trước
    const danhSachSapXep = [...danhSachNhom].sort((a, b) => (laBanLon(a) ? -1 : 1));

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
            map[b.viTriIndex + SO_COT] = "BLOCKED_" + b.id;
            map[b.viTriIndex + SO_COT * 2] = "BLOCKED_" + b.id;
          }
        } else {
          b.viTriIndex = undefined;
        }
      }
    });

    // Bước 2: Xếp tự động các bàn chưa có vị trí
    const banChuaCoViTri = danhSachSapXep.filter((b) => b.viTriIndex === undefined);
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
        map[currentSlot + SO_COT] = "BLOCKED_" + b.id;
        map[currentSlot + SO_COT * 2] = "BLOCKED_" + b.id;
      }
      currentSlot++;
    });
  };

  xepNhomBan(banOffline, "OFFLINE");
  xepNhomBan(banOnline, "ONLINE");
  xepNhomBan(banDuPhong, "DU_PHONG");

  let maxSlot = 18; // Tối thiểu 3 hàng (18 slot) để giao diện cân đối
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

// 3. Computed
const danhSachBanTheoTang = computed(() =>
  danhSachBan.value.filter((b) => b.khuVuc?.id === tangDangChon.value),
);

const danhSachBanPhanTrang = computed(() => {
  const start = (trangHienTai.value - 1) * soDongMoiTrang.value;
  return danhSachBanTheoTang.value.slice(start, start + soDongMoiTrang.value);
});

const tongTrang = computed(() =>
  Math.ceil(danhSachBanTheoTang.value.length / soDongMoiTrang.value),
);

const TONG_SLOT = ref(36);

const slots = computed(() =>
  Array.from({ length: TONG_SLOT.value }, (_, i) => ({ id: i + 1, index: i })),
);

const tongBan = computed(() => danhSachBanTheoTang.value.length);

const soBanTrong = computed(
  () => danhSachBanTheoTang.value.filter((b) => b.trangThai === 1).length,
);

const soBanDaDat = computed(
  () => danhSachBanTheoTang.value.filter((b) => b.trangThai === 2).length,
);

const soBanCoKhach = computed(
  () => danhSachBanTheoTang.value.filter((b) => b.trangThai === 3).length,
);

const soBanDangDon = computed(
  () => danhSachBanTheoTang.value.filter((b) => b.trangThai === 4).length,
);

const soBanNgungHoatDong = computed(
  () => danhSachBanTheoTang.value.filter((b) => b.trangThai === 0).length,
);

// 4. Watchers & Lifecycle
watch(tangDangChon, () => {
  trangHienTai.value = 1;
  khoiTaoSlot(); // Khởi tạo lại slot khi đổi tầng
});

// Thêm hàm kiểm tra bàn lớn - chỉ dựa vào sức chứa thực tế
const laBanLon = (ban) => ban && ban.sucChuaToiDa >= 12;

const chuyenBan = (from, to) => {
  const banFrom = viTriBan.value[from];
  const banTo = viTriBan.value[to];

  if (!banFrom || typeof banFrom === "string") return;

  // Kiểm tra xem vị trí đích có thuộc khu vực hợp lệ của bàn đó không
  if (!isSlotHopLe(to, banFrom.loaiBan)) {
    notify.error("Chỉ được phép di chuyển bàn trong cùng phân khu!");
    return;
  }

  // Nếu có bàn đích, cũng phải kiểm tra xem bàn đích có hợp lệ với vị trí cũ không
  if (banTo && typeof banTo === "object" && !isSlotHopLe(from, banTo.loaiBan)) {
    notify.error("Chỉ được phép di chuyển bàn trong cùng phân khu!");
    return;
  }

  // Cập nhật index
  banFrom.viTriIndex = to;
  if (banTo && typeof banTo === "object") {
    banTo.viTriIndex = from;
  }

  khoiTaoSlot();

  const payload = [];
  payload.push({ id: banFrom.id, viTriBan: banFrom.viTriIndex });
  if (banTo && typeof banTo === "object") {
    payload.push({ id: banTo.id, viTriBan: banTo.viTriIndex });
  }

  capNhatViTriBan(payload).catch((e) => {
    console.error("Lỗi cập nhật vị trí", e);
    const errorMessage =
      typeof e.response?.data === "string"
        ? e.response.data
        : e.response?.data?.message;
    notify.error(errorMessage || "Lỗi cập nhật vị trí lên server");
  });
};

// đổi vị trí
const handleSlotClick = async (index) => {
  const item = viTriBan.value[index];

  // TRƯỜNG HỢP 1: Chưa chọn bàn nào
  if (slotDangChon.value === null) {
    if (item && typeof item === "object") {
      // Chỉ cho chọn bàn khi trạng thái là Trống (1) hoặc Không sử dụng (0)
      const trangThai = item.trangThai;
      if (trangThai !== 0 && trangThai !== 1) {
        const tenTrangThai =
          {
            2: "đã được đặt trước",
            3: "đang có khách",
            4: "đang được dọn",
          }[trangThai] || "không ở trạng thái cho phép";
        notify.error(
          `Bàn "${item.tenBanAn}" đang ${tenTrangThai}, không thể thay đổi vị trí!`,
        );
        return;
      }
      slotDangChon.value = index;
    }
  }
  // TRƯỜNG HỢP 2: Đã chọn 1 bàn trước đó
  else {
    if (slotDangChon.value === index) {
      slotDangChon.value = null;
    } else {
      const banDangChuyen = viTriBan.value[slotDangChon.value];
      const banDich = viTriBan.value[index];

      if (typeof banDich === "string" && banDich.startsWith("BLOCKED")) {
        const blockedId = banDich.split("_")[1];
        if (blockedId != banDangChuyen.id) {
          notify.error("Vị trí này đang thuộc về một bàn lớn");
          slotDangChon.value = null;
          return;
        }
      }

      // Nếu ô đích đang có bàn, kiểm tra trạng thái bàn đích
      if (banDich && typeof banDich === "object") {
        const trangThaiDich = banDich.trangThai;
        if (trangThaiDich !== 0 && trangThaiDich !== 1) {
          const tenTrangThai =
            {
              2: "đã được đặt trước",
              3: "đang có khách",
              4: "đang được dọn",
            }[trangThaiDich] || "không ở trạng thái cho phép";
          notify.error(
            `Bàn "${banDich.tenBanAn}" đang ${tenTrangThai}, không thể đổi vị trí!`,
          );
          slotDangChon.value = null;
          return;
        }
      }

      // LUÔN hỏi xác nhận
      const isSwap = banDich && typeof banDich === "object";
      const ok = await notify.confirm(
        isSwap
          ? `Bạn có chắc muốn đổi vị trí ${banDangChuyen.tenBanAn} với ${banDich.tenBanAn}?`
          : `Bạn có chắc muốn chuyển ${banDangChuyen.tenBanAn} sang vị trí mới?`,
      );

      if (!ok) {
        slotDangChon.value = null;
        return;
      }

      // Thực hiện chuyển
      chuyenBan(slotDangChon.value, index);
      slotDangChon.value = null;

      notify.success("Đổi vị trí thành công!");
    }
  }
};

//tính
const getTongBan = (khuVucId) => {
  return danhSachBan.value.filter(
    (b) => b.khuVuc?.id === khuVucId && b.trangThai !== 0,
  ).length;
};

const getSoBanTrong = (khuVucId) => {
  return danhSachBan.value.filter(
    (b) => b.khuVuc?.id === khuVucId && b.trangThai === 1, // 1 = Trống
  ).length;
};

//sửa
const openSuaBan = async (ban) => {
  if (!hasPermission("SUA_BAN")) {
    router.push("/403");
    return;
  }
  try {
    const res = await getBanChiTiet(ban.id); // gọi backend
    banDangSua.value = res.data; // data CHUẨN
    showSuaBan.value = true; // mở modal

    reloadKey.value++;
  } catch (err) {
    if (err.response?.status === 404) {
      notify.error("Không tìm thấy bàn");
    } else {
      notify.error("Lỗi server");
    }
  }
};

const capNhatBan = async (ban) => {
  const isDuplicate = danhSachBan.value.some(
    (b) =>
      b.id !== ban.id &&
      b.khuVuc?.id === ban.khuVuc.id &&
      b.tenBanAn.trim().toLowerCase() === ban.tenBanAn.trim().toLowerCase(),
  );
  if (isDuplicate) {
    suaBanRef.value?.setErrors({
      tenBanAn: "Tên bàn đã tồn tại trong khu vực này",
    });
    return;
  }

  try {
    await themBanAn(ban);

    notify.success("Cập nhật bàn thành công!");

    showSuaBan.value = false;

    danhSachBan.value = await loadDanhSachBanVaHoaDon();

    khoiTaoSlot();
  } catch (e) {
    const errorMessage =
      typeof e.response?.data === "string"
        ? e.response.data
        : e.response?.data?.message;
    notify.error(errorMessage || "Cập nhật bàn thất bại");
    console.error(e);
  }
};

//xóa
const xoaBanHandler = async (banId) => {
  try {
    await xoaBan(banId);

    notify.success("Xóa bàn thành công!");

    // Cập nhật trạng thái thành 0 thay vì xóa khỏi danh sách
    const banTimThay = danhSachBan.value.find((b) => b.id === banId);
    if (banTimThay) {
      banTimThay.trangThai = 0;
    }

    slotDangChon.value = null;
    showSuaBan.value = false;

    khoiTaoSlot();
  } catch (e) {
    const errorMessage =
      typeof e.response?.data === "string"
        ? e.response.data
        : e.response?.data?.message;
    notify.error(errorMessage || "Xóa bàn thất bại");
    console.error(e);
  }
};

// chọn tầng mới
const loadKhuVuc = async () => {
  try {
    const res = await getAllkhuvuc();

    danhSachKhuVuc.value = res.data;
    danhSachTang.value = res.data; // thêm dòng này
  } catch (e) {
    notify.error("Không load được khu vực");
  }
};

const handleReloadKhuVuc = async (newId) => {
  const res = await getAllkhuvuc();

  danhSachKhuVuc.value = res.data;
  danhSachTang.value = res.data;

  tangDangChon.value = newId;
};

//thêm tầng
const themTang = () => {
  formTang.value = {
    tenKhuVuc: "",
    viTri: "",
    moTa: "",
    trangThai: 1,
  };

  showModalThemTang.value = true;
};

const submitThemTang = async () => {
  errorsTang.value.tenKhuVuc = "";
  errorsTang.value.viTri = "";
  let hasError = false;

  if (!formTang.value.tenKhuVuc) {
    errorsTang.value.tenKhuVuc = "Vui lòng nhập tên tầng";
    hasError = true;
  }
  if (!formTang.value.viTri) {
    errorsTang.value.viTri = "Vui lòng nhập vị trí tầng";
    hasError = true;
  }

  if (hasError) return;

  const isDuplicate = danhSachTang.value.some(
    (t) =>
      t.tenKhuVuc.trim().toLowerCase() ===
      formTang.value.tenKhuVuc.trim().toLowerCase(),
  );
  if (isDuplicate) {
    errorsTang.value.tenKhuVuc = "Tên khu vực đã tồn tại";
    return;
  }

  try {
    const res = await themKhuVuc(formTang.value);

    notify.success("Thêm tầng thành công");

    showModalThemTang.value = false;

    formTang.value = {
      tenKhuVuc: "",
      viTri: "",
      moTa: "",
      trangThai: 1,
    };

    await loadKhuVuc();

    tangDangChon.value = res.data.id;
  } catch (e) {
    const errorMessage =
      typeof e.response?.data === "string"
        ? e.response.data
        : e.response?.data?.message;
    notify.error(errorMessage || "Thêm tầng thất bại");
  }
};

// sửa tầng
const suaTang = (tang) => {
  tangDangSua.value = tang;

  formTang.value = {
    ...tang,
  };

  showModalSuaTang.value = true;
};

const submitSuaTang = async () => {
  errorsTang.value.tenKhuVuc = "";
  errorsTang.value.viTri = "";
  let hasError = false;

  if (!formTang.value.tenKhuVuc) {
    errorsTang.value.tenKhuVuc = "Vui lòng nhập tên tầng";
    hasError = true;
  }
  if (!formTang.value.viTri) {
    errorsTang.value.viTri = "Vui lòng nhập vị trí tầng";
    hasError = true;
  }

  if (hasError) return;

  const isDuplicate = danhSachTang.value.some(
    (t) =>
      t.id !== tangDangSua.value.id &&
      t.tenKhuVuc.trim().toLowerCase() ===
        formTang.value.tenKhuVuc.trim().toLowerCase(),
  );
  if (isDuplicate) {
    errorsTang.value.tenKhuVuc = "Tên khu vực đã tồn tại";
    return;
  }

  try {
    const data = {
      ...formTang.value,
      id: tangDangSua.value.id,
    };

    await themKhuVuc(data);

    notify.success("Cập nhật tầng thành công");

    showModalSuaTang.value = false;

    await loadKhuVuc();
  } catch (e) {
    notify.error("Cập nhật tầng thất bại");
  }
};

//đổi trạng thái tầng
const doiTrangThaiTang = async (tang) => {
  if (tang.trangThai === 1) {
    const soBanHoatDong = getTongBan(tang.id);
    if (soBanHoatDong > 0) {
      notify.error("Tầng đang có bàn hoạt động, không thể ngừng hoạt động!");
      return;
    }
  }

  if (
    !(await notify.confirm(
      `Bạn có chắc muốn đổi trạng thái tầng ${tang.tenKhuVuc}?`,
    ))
  )
    return;

  try {
    const data = {
      ...tang,
      trangThai: tang.trangThai === 1 ? 0 : 1,
    };
    await themKhuVuc(data);
    notify.success("Đổi trạng thái thành công!");
    await loadKhuVuc();
  } catch (e) {
    const errorMessage =
      typeof e.response?.data === "string"
        ? e.response.data
        : e.response?.data?.message;
    notify.error(errorMessage || "Đổi trạng thái thất bại!");
  }
};
</script>

<template>
  <div class="trang-so-do-ban">
    <div class="box-white box-header-tang">
      <div class="khu-tang">
        <div
          v-for="t in danhSachTang"
          :key="t.id"
          class="the-tang"
          :class="{ active: t.id === tangDangChon }"
          @click="tangDangChon = t.id"
        >
          <b
            :style="t.trangThai === 0 ? 'text-decoration: line-through' : ''"
            >{{ t.tenKhuVuc }}</b
          >
          <div>{{ getSoBanTrong(t.id) }} / {{ getTongBan(t.id) }} bàn</div>
        </div>
      </div>

      <div class="header-actions">
        <button class="excel-btn" @click="handleExportExcel">
          <img :src="iconExcel" alt="" />
          <span>Xuất Excel</span>
        </button>

        <input
          type="file"
          ref="excelInputRef"
          accept=".xlsx, .xls"
          class="d-none"
          @change="uploadExcel"
        />
        <button
          class="import-btn"
          @click="excelInputRef.click()"
          :disabled="isTangNgungHoatDong"
        >
          <img src="@/assets/icons/nhapexx.png" alt="" />
          <span>Thêm từ Excel</span>
        </button>

        <button class="excel-btn" @click="handleDownloadTemplate">
          <img :src="iconExcel" alt="" />
          <span>Tải mẫu</span>
        </button>

        <button
          class="icon-btn primary"
          @click="moThemBan"
          :disabled="isTangNgungHoatDong"
        >
          <img :src="iconAdd" />
          <span>Thêm bàn</span>
        </button>

        <button class="icon-btn primary" @click="moKhuVuc">
          <i class="bi bi-geo-alt"></i>
          <span>Khu vực</span>
        </button>
      </div>
    </div>

    <div class="box-white border">
      <div class="row g-3 text-center">
        <div class="col">
          <div class="border rounded-3 p-3 h-100 stat-card">
            <div class="text-muted small">Tổng bàn</div>
            <div class="fs-4 fw-bold">{{ tongBan }}</div>
          </div>
        </div>

        <div class="col">
          <div class="border rounded-3 p-3 h-100 stat-card">
            <div
              class="d-flex justify-content-center align-items-center gap-2 text-muted small"
            >
              <span class="dot bg-success"></span> Trống
            </div>
            <div class="fs-4 fw-bold text-success">{{ soBanTrong }}</div>
          </div>
        </div>

        <div class="col">
          <div class="border rounded-3 p-3 h-100 stat-card">
            <div
              class="d-flex justify-content-center align-items-center gap-2 text-muted small"
            >
              <span class="dot bg-danger"></span> Đang sử dụng
            </div>
            <div class="fs-4 fw-bold text-danger">{{ soBanCoKhach }}</div>
          </div>
        </div>

        <div class="col">
          <div class="border rounded-3 p-3 h-100 stat-card">
            <div
              class="d-flex justify-content-center align-items-center gap-2 text-muted small"
            >
              <span class="dot bg-warning"></span> Đang dọn
            </div>
            <div class="fs-4 fw-bold text-warning">{{ soBanDangDon }}</div>
          </div>
        </div>

        <div class="col">
          <div class="border rounded-3 p-3 h-100 stat-card">
            <div
              class="d-flex justify-content-center align-items-center gap-2 text-muted small"
            >
              <span class="dot bg-primary"></span> Đã đặt
            </div>
            <div class="fs-4 fw-bold text-primary">{{ soBanDaDat }}</div>
          </div>
        </div>

        <div class="col">
          <div class="border rounded-3 p-3 h-100 stat-card">
            <div
              class="d-flex justify-content-center align-items-center gap-2 text-muted small"
            >
              <span class="dot bg-secondary"></span> Ngừng hoạt động
            </div>
            <div class="fs-4 fw-bold text-secondary">
              {{ soBanNgungHoatDong }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="khu-chu-thich">
      <div><span class="dot do"></span> Đang sử dụng</div>
      <div><span class="dot xanh-la"></span> Trống</div>
      <div><span class="dot vang"></span> Đang dọn</div>
      <div><span class="dot xanh-duong"></span> Đã đặt</div>
      <div>
        <span class="dot" style="background: #9e9e9e"></span> Ngừng hoạt động
      </div>
      <div><img :src="iconWifi" class="wifi-icon" /> Đặt online</div>
    </div>

    <div class="noi-dung">
      <div class="khu-so-do">
        <!-- Tiêu đề phân khu sơ đồ bàn -->
        <div class="so-do-header">
          <div class="header-offline">BÀN OFFLINE</div>
          <div class="header-online">BÀN ONLINE</div>
          <div class="header-duphong">BÀN DỰ PHÒNG</div>
        </div>

        <div class="grid-ban">
          <div
            v-for="slot in slots"
            :key="slot.id"
            class="slot"
            :class="{
              'slot-active': slotDangChon === slot.index,
              'ban-lon':
                viTriBan[slot.index] &&
                typeof viTriBan[slot.index] === 'object' &&
                laBanLon(viTriBan[slot.index]),
            }"
            v-show="
              !(
                typeof viTriBan[slot.index] === 'string' &&
                viTriBan[slot.index].startsWith('BLOCKED_')
              )
            "
            @click="handleSlotClick(slot.index)"
          >
            <div
              v-if="
                viTriBan[slot.index] && typeof viTriBan[slot.index] === 'object'
              "
              class="o-ban"
              style="height: 100%; width: 100%"
              :class="[
                viTriBan[slot.index].loaiBan === 'DU_PHONG'
                  ? 'ban_du_phong'
                  : getClassTrangThai(viTriBan[slot.index].trangThai)
              ]"
            >
              <!-- Top row: Tên bàn & Trạng thái -->
              <div class="table-header">
                <span
                  class="table-code"
                  :style="
                    viTriBan[slot.index].trangThai === 0
                      ? 'text-decoration: line-through'
                      : ''
                  "
                >
                  {{ viTriBan[slot.index].tenBanAn }}
                </span>
                <span
                  class="table-status-text"
                  :style="{
                    color:
                      viTriBan[slot.index].trangThai === 1
                        ? '#4caf50'
                        : viTriBan[slot.index].trangThai === 2
                        ? '#42a5f5'
                        : viTriBan[slot.index].trangThai === 3
                        ? '#f44336'
                        : viTriBan[slot.index].trangThai === 4
                        ? '#ff9800'
                        : '#9e9e9e',
                  }"
                >
                  {{
                    viTriBan[slot.index].trangThai === 0
                      ? "Ngừng hoạt động"
                      : viTriBan[slot.index].trangThai === 1
                      ? "Trống"
                      : viTriBan[slot.index].trangThai === 2
                      ? "Đã đặt"
                      : viTriBan[slot.index].trangThai === 3
                      ? "Đang sử dụng"
                      : "Đang dọn"
                  }}
                </span>
              </div>

              <div
                v-if="getThoiGianSuDungText(viTriBan[slot.index])"
                class="table-timer-text"
                style="
                  text-align: right;
                  padding-right: 6px;
                  margin-top: -2px;
                  margin-bottom: -10px;
                  z-index: 1;
                "
              >
                {{ getThoiGianSuDungText(viTriBan[slot.index]) }}
              </div>

              <!-- Middle row: Sức chứa -->
              <div class="table-body">
                {{ viTriBan[slot.index].sucChuaToiDa }} chỗ
              </div>

              <div class="table-footer">
                <div
                  class="table-invoice-text"
                  style="display: flex; flex-direction: column; width: 100%"
                >
                  <template v-if="viTriBan[slot.index].trangThai === 3">
                    <!-- Trường hợp có nhiều hóa đơn -->
                    <template
                      v-if="
                        (viTriBan[slot.index].coNhieuHoaDon ||
                          viTriBan[slot.index].soHoaDonDangHoatDong > 1) &&
                        viTriBan[slot.index].dsHoaDon &&
                        viTriBan[slot.index].dsHoaDon.length > 0
                      "
                    >
                      <div
                        v-for="hd in sapXepHoaDonTheoThoiGianMo(
                          viTriBan[slot.index].dsHoaDon,
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
                            formatCurrency(hd.tienConLai ?? hd.tongTien ?? 0)
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
                          {{ viTriBan[slot.index].maHoaDon || 1 }}
                        </span>
                        <span style="font-weight: 600">
                          {{
                            formatCurrency(
                              viTriBan[slot.index].tienConLai ??
                                viTriBan[slot.index].tongTien ??
                                0,
                            )
                          }}đ
                        </span>
                      </div>
                    </template>
                  </template>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="box-white">
        <div
          class="d-flex justify-content-between align-items-center mb-4 pb-3 border-bottom"
        >
          <h6 class="fw-bold m-0 fs-5 d-flex align-items-center hover-dance">
            <img
              :src="iconDanhSach"
              style="width: 22px; height: 22px; margin-right: 8px"
              class="dance-icon"
            />
            Danh sách bàn
          </h6>
          <span
            class="badge px-3 py-2"
            style="
              border-radius: 4px;
              background-color: #ffe4e6;
              color: #d73737;
            "
          >
            {{ tongBan }} bàn
          </span>
        </div>

        <table class="table align-middle table-hover custom-hover-table">
          <thead class="text-muted small text-nowrap">
            <tr>
              <th>TÊN BÀN</th>
              <th>GHẾ</th>
              <th>TRẠNG THÁI</th>
              <th>ĐẶT ONLINE</th>
              <th class="text-end">THAO TÁC</th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="b in danhSachBanPhanTrang" :key="b.id">
              <td
                class="fw-semibold"
                :style="
                  b.trangThai === 0 ? 'text-decoration: line-through' : ''
                "
              >
                {{ b.tenBanAn }}
              </td>

              <td>{{ b.sucChuaToiDa }}</td>

              <td>
                <span
                  class="badge px-3 py-1"
                  style="border-radius: 4px"
                  :class="{
                    'bg-secondary text-white': b.trangThai === 0,
                    'bg-success-subtle text-success': b.trangThai === 1,
                    'bg-primary-subtle text-primary': b.trangThai === 2,
                    'bg-danger-subtle text-danger': b.trangThai === 3,
                    'bg-warning-subtle text-warning': b.trangThai === 4,
                  }"
                >
                  {{ getTenTrangThai(b.trangThai) }}
                </span>
              </td>

              <td>
                <span v-if="b.online" class="text-success fw-medium">
                  <i class="bi bi-check-circle-fill me-1"></i>Cho phép
                </span>
                <span v-else class="text-muted">Cho phép </span>
              </td>

              <td class="text-end">
                <button
                  class="btn btn-sm fw-medium d-inline-flex align-items-center justify-content-center gap-1"
                  @click="openSuaBan(b)"
                >
                  <i class="ri-edit-line"></i>
                  Sửa
                </button>
              </td>
            </tr>
          </tbody>
        </table>

        <nav class="pagination-container">
          <ul class="pagination-premium">
            <li>
              <button
                class="nav-action-btn"
                :disabled="trangHienTai === 1"
                @click.prevent="goFirst"
                aria-label="First page"
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
                  <path d="m11 17-5-5 5-5" />
                  <path d="m18 17-5-5 5-5" />
                </svg>
              </button>
            </li>
            <li>
              <button
                class="nav-action-btn"
                :disabled="trangHienTai === 1"
                @click.prevent="goPrev"
                aria-label="Previous page"
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
                  <path d="m15 18-6-6 6-6" />
                </svg>
              </button>
            </li>
            <li>
              <div class="page-indicator-badge">
                <span class="current-number">{{ trangHienTai }}</span>
                <span class="divider">/</span>
                <span class="total-number">{{ tongTrang }}</span>
              </div>
            </li>
            <li>
              <button
                class="nav-action-btn"
                :disabled="trangHienTai === tongTrang"
                @click.prevent="goNext"
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
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <path d="m9 18 6-6-6-6" />
                </svg>
              </button>
            </li>
            <li>
              <button
                class="nav-action-btn"
                :disabled="trangHienTai === tongTrang"
                @click.prevent="goLast"
                aria-label="Last page"
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
      </div>
    </div>
  </div>

  <Teleport to="body">
    <!-- DANH SÁCH TẦNG / KHU VỰC -->
    <div class="modal fade" tabindex="-1" ref="modalChiTietTangRef">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content premium-modal-content">
          <div class="modal-header premium-modal-header">
            <h5 class="fw-bold mb-0">
              <i class="ri-list-check me-1"></i> Danh sách khu vực
            </h5>
            <div class="d-flex gap-2 align-items-center">
              <button
                class="btn-primary py-2 px-3"
                style="font-size: 14px"
                @click="themTang"
              >
                <i class="ri-add-line me-1"></i> Thêm tầng
                <div class="btn-light-sweep"></div>
              </button>
              <button
                type="button"
                class="btn-close"
                @click="showChiTietTang = false"
              ></button>
            </div>
          </div>
          <div class="modal-body premium-modal-body">
            <table class="table-tang">
              <thead>
                <tr>
                  <th>STT</th>
                  <th>Khu vực</th>
                  <th>Số bàn</th>
                  <th>Vị trí</th>
                  <th>Mô tả</th>
                  <th>Trạng thái</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(t, index) in danhSachTang" :key="t.id">
                  <td>{{ index + 1 }}</td>
                  <td>{{ t.tenKhuVuc }}</td>
                  <td>{{ getTongBan(t.id) }}</td>
                  <td>{{ t.viTri }}</td>
                  <td>{{ t.moTa }}</td>
                  <td>
                    <span
                      class="pill-status"
                      :class="t.trangThai === 1 ? 'active' : 'inactive'"
                    >
                      {{ t.trangThai === 1 ? "Hoạt động" : "Ngừng hoạt động" }}
                    </span>
                  </td>
                  <td>
                    <div
                      class="tang-action d-flex flex-row justify-content-center gap-2"
                    >
                      <button
                        class="btn-action-icon"
                        @click="suaTang(t)"
                        title="Sửa tầng"
                      >
                        <i class="ri-edit-line"></i>
                      </button>
                      <button
                        class="btn-action-icon"
                        @click="doiTrangThaiTang(t)"
                        title="Đổi trạng thái"
                      >
                        <i class="ri-loop-left-line"></i>
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- MODAL THÊM TẦNG -->
    <div class="modal fade" tabindex="-1" ref="modalThemTangRef">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content premium-modal-content">
          <div class="modal-header premium-modal-header">
            <h5 class="fw-bold mb-0">
              <i class="ri-add-line me-1"></i> Thêm tầng
            </h5>
            <button
              type="button"
              class="btn-close"
              @click="showModalThemTang = false"
            ></button>
          </div>

          <div class="modal-body premium-modal-body">
            <div class="mb-3">
              <label class="form-label">Khu vực</label>
              <input
                v-model="formTang.tenKhuVuc"
                class="custom-input"
                :class="{ 'input-invalid': errorsTang.tenKhuVuc }"
                @input="errorsTang.tenKhuVuc = ''"
              />
              <transition name="shake">
                <p v-if="errorsTang.tenKhuVuc" class="error-hint mt-2 mb-0">
                  <i class="bi bi-exclamation-circle"></i>
                  {{ errorsTang.tenKhuVuc }}
                </p>
              </transition>
            </div>
            <div class="mb-3">
              <label class="form-label">Vị trí</label>
              <input
                v-model="formTang.viTri"
                class="custom-input"
                :class="{ 'input-invalid': errorsTang.viTri }"
                @input="errorsTang.viTri = ''"
              />
              <transition name="shake">
                <p v-if="errorsTang.viTri" class="error-hint mt-2 mb-0">
                  <i class="bi bi-exclamation-circle"></i>
                  {{ errorsTang.viTri }}
                </p>
              </transition>
            </div>
            <div class="mb-3">
              <label class="form-label">Mô tả</label>
              <input v-model="formTang.moTa" class="custom-input" />
            </div>
          </div>

          <div class="modal-footer premium-modal-footer">
            <button
              type="button"
              class="btn-cancel"
              @click="showModalThemTang = false"
            >
              Hủy bỏ
            </button>
            <button type="button" class="btn-primary" @click="submitThemTang">
              <i class="ri-check-double-line me-1"></i>
              <div class="btn-light-sweep"></div>
              Xác nhận
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- MODAL SỬA TẦNG -->
    <div class="modal fade" tabindex="-1" ref="modalSuaTangRef">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content premium-modal-content">
          <div class="modal-header premium-modal-header">
            <h5 class="fw-bold mb-0">
              <i class="ri-edit-line me-1"></i> Sửa tầng
            </h5>
            <button
              type="button"
              class="btn-close"
              @click="showModalSuaTang = false"
            ></button>
          </div>

          <div class="modal-body premium-modal-body">
            <div class="mb-3">
              <label class="form-label">Khu vực</label>
              <input
                v-model="formTang.tenKhuVuc"
                class="custom-input"
                :class="{ 'input-invalid': errorsTang.tenKhuVuc }"
                @input="errorsTang.tenKhuVuc = ''"
              />
              <transition name="shake">
                <p v-if="errorsTang.tenKhuVuc" class="error-hint mt-2 mb-0">
                  <i class="bi bi-exclamation-circle"></i>
                  {{ errorsTang.tenKhuVuc }}
                </p>
              </transition>
            </div>
            <div class="mb-3">
              <label class="form-label">Vị trí</label>
              <input
                v-model="formTang.viTri"
                class="custom-input"
                :class="{ 'input-invalid': errorsTang.viTri }"
                @input="errorsTang.viTri = ''"
              />
              <transition name="shake">
                <p v-if="errorsTang.viTri" class="error-hint mt-2 mb-0">
                  <i class="bi bi-exclamation-circle"></i>
                  {{ errorsTang.viTri }}
                </p>
              </transition>
            </div>
            <div class="mb-3">
              <label class="form-label">Mô tả</label>
              <input v-model="formTang.moTa" class="custom-input" />
            </div>
            <div class="mb-3">
              <label class="form-label">Trạng thái</label>
              <v-select
                v-model="formTang.trangThai"
                :options="[
                  { label: 'Hoạt động', value: 1 },
                  { label: 'Ngừng hoạt động', value: 0 },
                ]"
                :reduce="(option) => option.value"
                label="label"
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
          </div>

          <div class="modal-footer premium-modal-footer">
            <button
              type="button"
              class="btn-cancel"
              @click="showModalSuaTang = false"
            >
              Hủy bỏ
            </button>
            <button type="button" class="btn-primary" @click="submitSuaTang">
              <i class="ri-check-double-line me-1"></i>
              <div class="btn-light-sweep"></div>
              Xác nhận
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>

  <ThemBan
    ref="themBanRef"
    v-if="showThemBan"
    :key="reloadKey"
    :danhSachKhuVuc="danhSachKhuVuc"
    :tangDangChon="tangDangChon"
    @close="showThemBan = false"
    @submit="handleThemBan"
    @reload-khu-vuc="handleReloadKhuVuc"
  />

  <SuaBanDaTao
    ref="suaBanRef"
    :show="showSuaBan"
    :key="reloadKey"
    :ban="banDangSua"
    :danhSachTang="danhSachTang"
    @close="showSuaBan = false"
    @save="capNhatBan"
    @deleted="xoaBanHandler"
  />
</template>

<style scoped>
.trang-so-do-ban,
.noi-dung,
.khu-so-do,
.grid-ban,
.box-white,
.slot,
.o-ban,
table,
button,
span,
p,
div,
h5,
h6,
label {
  box-sizing: border-box;
}

button {
  border: none;
}
.btn-cancel {
  background: #f1f5f9;
  color: #334155;
  border: 1px solid #e2e8f0;
  padding: 10px 24px;
  border-radius: 7px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}
.btn-cancel:hover {
  background: #e2e8f0;
  transform: translateY(-2px);
}

.btn-primary {
  position: relative;
  overflow: hidden;
  background: #d73737;
  color: #fff;
  border: none;
  padding: 10px 24px;
  border-radius: 7px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}
.btn-primary:hover {
  background: #b92b2b;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(230, 90, 90, 0.3);
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
.trang-so-do-ban {
  background: #fff;
}

.custom-hover-table tbody tr {
  transition: background-color 0.2s ease;
  cursor: pointer;
}
.custom-hover-table tbody tr:hover {
  background-color: #f8fafc !important;
}

.stat-card {
  transition: all 0.3s ease;
  cursor: pointer;
}
.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  border-color: #cbd5e1 !important;
}
/* Nút icon trên Header Tầng */
.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 42px;
  padding: 0 16px;
  border: none;
  border-radius: 7px;
  background: #f1f5f9;
  color: #475569;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.2s;
}

.icon-btn.primary {
  background: #eef2ff;
  color: #d73737;
}

.icon-btn img {
  width: 18px !important;
  height: 18px !important;
  object-fit: contain;
  transition: all 0.3s ease;
}

.icon-btn i {
  font-size: 16px;
  transition: all 0.3s ease;
}

.icon-btn:hover {
  background: #e2e8f0;
  transform: translateY(-2px);
}

.icon-btn.primary:hover {
  background: #dfe7ff;
}

.excel-btn,
.import-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border: none;
  border-radius: 7px;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.2s;
}

.excel-btn img,
.import-btn img {
  width: 18px !important;
  height: 18px !important;
  object-fit: contain;
  transition: all 0.3s ease;
}

.excel-btn {
  background: #d4f0dd;
  color: #d73737;
}

.excel-btn:hover {
  background: #dcfce7;
  transform: translateY(-2px);
}

.import-btn {
  background: #e0f2fe;
  color: #d73737;
}

.import-btn:hover:not(:disabled) {
  background: #bae6fd;
  transform: translateY(-2px);
}

.import-btn:disabled,
.icon-btn.primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
}

@keyframes wiggle {
  0% {
    transform: rotate(0deg) scale(1);
  }
  25% {
    transform: rotate(-15deg) scale(1.2);
  }
  50% {
    transform: rotate(15deg) scale(1.2);
  }
  75% {
    transform: rotate(-5deg) scale(1.2);
  }
  100% {
    transform: rotate(0deg) scale(1.2);
  }
}

.icon-btn:hover img,
.icon-btn:hover i,
.hover-dance:hover .dance-icon {
  animation: wiggle 0.5s ease-in-out forwards;
}

.icon-btn:hover {
  background: #e2e8f0;
  transform: translateY(-4px);
}
.icon-btn.primary:hover {
  background: #dfe7ff;
}

/* BOX TRẮNG */
.box-white {
  background: #fff;
  padding: 14px;
  margin-top: 14px;

  border-radius: 7px; /* bo đủ 4 góc */
}

.box-header-tang {
  display: flex;
  align-items: center;
  justify-content: space-between;

  border-radius: 7px 7px 0 0; /* chỉ 2 góc trên */
}

/* TẦNG */
.khu-tang {
  display: flex;
  gap: 12px;
}
.the-tang {
  border: 1px solid #ddd;
  padding: 10px 14px;
  border-radius: 7px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.the-tang:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
  transform: translateY(-4px);
}
.the-tang.active {
  background: rgba(225, 29, 72, 0.08);
  border-color: #d16969;
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

/* CHÚ THÍCH */
.khu-chu-thich {
  display: flex;
  gap: 18px;
  margin: 14px 0;
  font-size: 13px;
}
.dot {
  width: 10px;
  height: 10px;
  border-radius: 7px;
  display: inline-block;
}
.wifi-icon {
  width: 14px !important; /* Giới hạn icon wifi ở chú thích */
  height: 14px !important;
  display: inline-block;
  object-fit: contain;
}

/* NỘI DUNG */
.noi-dung {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 480px;
  gap: 16px;
  align-items: start;
}

/* SƠ ĐỒ */
.khu-so-do {
  min-width: 0;
  padding: 18px;
  border-radius: 7px;
  border: 1px dashed rgba(0, 0, 0, 0.12);
  background-color: #f5f6fa;
  background-image: radial-gradient(
    circle at 1px 1px,
    rgba(0, 0, 0, 0.08) 1px,
    transparent 0
  );
  background-size: 30px 30px;
  min-height: 480px;
  overflow: hidden;
}

.so-do-header {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
  text-align: center;
  font-weight: 800;
  font-size: 13px;
}
.header-offline {
  grid-column: span 2;
  color: #2563eb;
  border-bottom: 3px solid #3b82f6;
  padding-bottom: 6px;
}
.header-online {
  grid-column: span 2;
  color: #16a34a;
  border-bottom: 3px solid #10b981;
  padding-bottom: 6px;
}
.header-duphong {
  grid-column: span 2;
  color: #4b5563;
  border-bottom: 3px solid #9ca3af;
  padding-bottom: 6px;
}

.grid-ban {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  grid-template-rows: repeat(3, 120px);
  grid-auto-rows: 120px;
  grid-auto-flow: dense;
  gap: 16px;
  justify-content: start;
  position: relative;
}
.grid-ban::before {
  content: "";
  position: absolute;
  top: 0;
  bottom: 0;
  left: calc(100% / 3 - 2.67px);
  width: 1px;
  border-left: 1px dashed #cbd5e1;
  pointer-events: none;
  z-index: 5;
}
.grid-ban::after {
  content: "";
  position: absolute;
  top: 0;
  bottom: 0;
  left: calc(100% * 2 / 3 + 2.67px);
  width: 1px;
  border-left: 1px dashed #cbd5e1;
  pointer-events: none;
  z-index: 5;
}

.noi-dung > .box-white {
  min-width: 0;
}

.o-ban {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 7px;
  color: #fff;
  transition: all 0.3s ease;
  cursor: grab;
  padding: 4px;
  display: flex;
  flex-direction: column;
  container-type: inline-size;
  overflow: hidden;
}

.o-ban:active {
  cursor: grabbing;
  transform: scale(0.95);
}

.table-header {
  background: #f8fafc;
  border-radius: 4px;
  padding: 2px 4px;
  margin-bottom: 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.table-code {
  color: #1e293b;
  font-size: min(10px, 8cqw);
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
  font-size: min(14px, 12cqw);
  font-weight: 800;
  color: #ffffff;
  margin: 2px 0 4px;
}
.table-invoice-text {
  font-size: min(7.5px, 6cqw);
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
  font-size: min(8px, 7cqw);
  font-weight: 600;
  text-transform: uppercase;
  opacity: 0.9;
  color: rgba(255, 255, 255, 0.9);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 65%;
  text-align: right;
}
.table-timer-text {
  font-size: min(7.5px, 6cqw);
  font-weight: 600;
  text-transform: uppercase;
  opacity: 0.9;
  color: rgba(255, 255, 255, 0.9);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Highlight slot đang được chọn để di chuyển */
.slot-active {
  border: 2px solid #2196f3 !important;
  background: rgba(33, 150, 243, 0.1);
  z-index: 20;
}

.o-ban.ngung_su_dung {
  background: #9e9e9e;
  color: #fff;
}
.o-ban.ban_du_phong {
  background: #9ca3af !important;
  color: #fff !important;
}
.o-ban.trong {
  background: #4caf50;
}
.o-ban.co_khach {
  background: #f44336;
}
.o-ban.dang_don {
  background: #ffca28;
  color: #000;
}
.o-ban.da_dat {
  background: #42a5f5;
}

.ten-ban {
  background: #fff;
  color: #000;
  padding: 4px 10px;
  border-radius: 4px;
  font-weight: bold;
  display: inline-block;
}
.ghe {
  font-size: 13px;
}

.wifi-ban {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 16px !important; /* Giới hạn icon wifi trên sơ đồ bàn */
  height: 16px !important;
  object-fit: contain;
}

/* TABLE */
table {
  width: 100%;
  border-collapse: collapse;
}
th,
td {
  padding: 8px;
  font-size: 13px;
  border-bottom: 1px solid #eee;
}

.slot {
  width: 100%;
  height: 120px;
  border: 1px dashed #cfd8dc;
  border-radius: 7px;
  position: relative;
  transition: border 0.2s;
}

.ban-lon {
  grid-row: span 3; /* Chiếm 3 hàng */
  height: calc(
    120px * 3 + 16px * 2
  ) !important; /* Cao bằng 3 ô + 2 khoảng gap */
  z-index: 10;
}

/* Đảm bảo ten-ban nằm đẹp trong bàn lớn */
.ban-lon .ten-ban {
  margin-bottom: 10px;
  transform: scale(1.1);
}
.btn {
  background: #d73737;
  color: white;
}
.btn:hover {
  background: #b92b2b;
  color: white;
}
.btn-next:hover {
  background: #f1f5f9;
  color: #1e293b;
}

/* Tầng */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.35);

  display: flex;
  justify-content: center;
  align-items: center;

  z-index: 9999;
}

.modal-box {
  width: 1000px; /* to ra */
  max-width: 95vw; /* responsive */
  background: white;
  border-radius: 7px;
  padding: 22px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.thong-ke-tang {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

@keyframes modalFade {
  from {
    transform: translateY(-20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* HEADER */
.tang-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.tang-header h5 {
  margin: 0;
  font-weight: 600;
}

.tang-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* BUTTON ADD */
.btn-add {
  background: #d73737;
  color: white;
  border: none;
  padding: 6px 14px;
  border-radius: 7px;
  font-size: 14px;
}
.btn-add:hover {
  background: #b92b2b;
}

/* TABLE */
.table-tang {
  width: 100%;
  border-collapse: collapse;
}

.table-tang th {
  text-align: left;
  font-size: 14px;
  padding: 12px 10px;
  border-bottom: 1px solid #eee;
  white-space: nowrap;
}

.table-tang th:nth-child(6),
.table-tang td:nth-child(6) {
  min-width: 112px;
}

.table-tang td {
  padding: 12px 10px;
  font-size: 14px;
  border-bottom: 1px solid #f2f2f2;
}

/* STATUS */
.pill-status {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  border-radius: 7px;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.2;
  white-space: nowrap;
}

.pill-status.active {
  background: #e8f5e9;
  color: #2e7d32;
}

.pill-status.inactive {
  background: #eeeeee;
  color: #555;
}

/* ACTION */
.tang-action {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

/* ACTION */
.tang-action {
  display: flex;
  align-items: center;
}

.btn-action-icon {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 50px;
  background: #f2f5f7;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 16px;
}
.btn-action-icon:hover {
  background: #eef2ff;
  color: #d73737;
  transform: translateY(-1px);
}

/* ===== SELECT 2 ĐỒNG BỘ ===== */
:deep(.v-select) {
  background: #fff;
  width: 100%;
  --vs-dropdown-option--active-bg: #f3f4f6;
  --vs-dropdown-option--active-color: #eb6464;
  --vs-border-color: #e5e7eb;
  --vs-border-radius: 7px;
  --vs-dropdown-box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1);
}
:deep(.vs__dropdown-toggle) {
  border: 1px solid #e5e7eb !important;
  border-radius: 7px !important;
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
:deep(.vs__selected) {
  position: absolute;
  margin: 0 !important;
  padding-left: 12px !important;
  color: #374151;
  border: none !important;
}
:deep(.vs__search::placeholder) {
  color: #9ca3af !important;
}
:deep(.vs__dropdown-toggle:hover) {
  border-color: #c8d0dc !important;
}
:deep(.vs--open .vs__dropdown-toggle) {
  border-color: var(--color-primary, #e65a5a) !important;
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12) !important;
}
:deep(.vs__dropdown-menu) {
  border: 1px solid #e5e7eb !important;
  border-radius: 7px !important;
  margin-top: 8px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1) !important;
  padding: 6px !important;
  z-index: 1000;
}
:deep(.vs__dropdown-option) {
  border-radius: 7px !important;
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
  border-radius: 7px;
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
  border-radius: 7px;
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

/* ================== PREMIUM MODAL ================== */
.premium-modal-content {
  background: #ffffff;
  border-radius: 7px;
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
  font-size: 25px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* OVERRIDE BOOTSTRAP BTN-CLOSE */
.premium-modal-header .btn-close {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 7px;
  background-color: #f1f5f9;
  background-image: none !important;
  color: #64748b;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 0;
  box-shadow: none;
  opacity: 1;
}
.premium-modal-header .btn-close::before {
  content: "✕";
}
.premium-modal-header .btn-close:hover {
  background-color: #fee2e2;
  color: #d73737;
}

.premium-modal-body {
  padding: 24px;
}

.premium-modal-footer {
  padding: 16px 24px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* CUSTOM INPUT ĐỒNG BỘ */
.custom-input {
  width: 100%;
  height: 42px;
  box-sizing: border-box;
  padding: 0 12px;
  border: 1px solid #e5e7eb;
  border-radius: 7px;
  font-size: 14px;
  outline: none;
  transition: all 0.22s ease;
}
.custom-input:hover {
  border-color: #c8d0dc;
}
.custom-input:focus {
  border-color: var(--color-primary, #e65a5a);
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12);
}

.form-label {
  font-weight: 600;
  font-size: 14px;
  color: #4b5563;
  margin-bottom: 6px;
  display: block;
}
/* ERROR CSS */
.error-hint {
  color: #dc3545;
  font-size: 13px;
  margin-top: 4px;
}
.input-invalid {
  border-color: #dc3545 !important;
  box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.1) !important;
}
.shake-enter-active {
  animation: shake 0.5s;
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

@media (max-width: 1400px) {
  .grid-ban {
    grid-template-columns: repeat(5, minmax(0, 1fr));
    grid-template-rows: repeat(4, 120px);
  }

  .slot.ban-lon {
    grid-row: span 4;
    height: calc(120px * 4 + 16px * 3) !important;
  }
}

@media (max-width: 1200px) {
  .noi-dung {
    grid-template-columns: 1fr;
  }

  .grid-ban {
    grid-template-columns: repeat(4, minmax(120px, 1fr));
    grid-template-rows: auto;
  }

  .slot.ban-lon {
    grid-row: auto;
    height: 140px !important;
  }
}

@media (max-width: 768px) {
  .box-header-tang,
  .tang-header,
  .premium-modal-header {
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
  }

  .header-actions,
  .tang-actions,
  .khu-tang,
  .khu-chu-thich {
    flex-wrap: wrap;
  }

  .khu-so-do {
    padding: 12px;
    min-height: 360px;
    overflow-x: auto;
  }

  .grid-ban {
    grid-template-columns: repeat(2, minmax(130px, 1fr));
    grid-auto-rows: 112px;
    gap: 12px;
    min-width: 300px;
  }

  .slot.ban-lon {
    grid-row: span 2;
    height: calc(112px * 2 + 12px) !important;
  }

  .premium-modal-body {
    padding: 16px;
    overflow-x: auto;
  }

  .table-tang {
    min-width: 680px;
  }

  .premium-modal-header h5 {
    font-size: 20px;
  }

  .pagination-premium {
    flex-wrap: wrap;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .grid-ban {
    grid-template-columns: 1fr;
  }

  .icon-btn,
  .excel-btn,
  .import-btn,
  .btn-primary {
    width: 100%;
    justify-content: center;
  }
}
</style>
