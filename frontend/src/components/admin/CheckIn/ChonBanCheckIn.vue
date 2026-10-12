<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from "vue";
import { getAllBanAn } from "@/api/admin/datban/datBanApi";
import { getAllkhuvuc } from "@/api/admin/khuvuc/khuVucApi";
import { getDanhSachBan } from "@/api/admin/checkin/checkinapi";
import CacheService from "@/services/CacheService";
import { useUtils } from "@/utils/helper";
import iconWifi from "@/assets/icons/wifi.png";

const { notify, nprogress, formatCurrency } = useUtils();

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  mode: {
    type: String,
    default: "doi-ban",
  },
  banHienTai: {
    type: Object,
    default: () => ({}),
  },
});

const emit = defineEmits(["close", "confirm"]);

const danhSachBan = ref([]);
const danhSachTang = ref([]);
const tangDangChon = ref("");
const dsBanDaChon = ref([]);
const soKhachThem = ref(null);
const errorSoKhachThem = ref("");

// Variables for Step 2: Dish splitting
const stepTachBan = ref(1);
const dsMonHienThi = ref([]);
const banNhanSelected = ref(null);
const soLuongTachTheoBan = ref({}); // { idBan: { idMon: number } }
const danhSachBanMoi = ref([]);
const tachHoaDonSelected = ref(false);
const moBanDuPhong = ref(false);
const currentTime = ref(new Date());
let timerInterval;

// ==== PHÂN DANH MỤC & PHÂN TRANG ====
const danhMucDangChon = ref("Tất cả");
const currentPage = ref(1);
const pageSize = ref(5);

function getSoLuongDaChuyenTatCa(idMon) {
  let tong = 0;
  for (const ds of Object.values(soLuongTachTheoBan.value)) {
    tong += ds[idMon] || 0;
  }
  return tong;
}

const dsMonChuaPhanBo = computed(() => {
  return dsMonHienThi.value.filter(
    (mon) => mon.soLuong - getSoLuongDaChuyenTatCa(mon.id) > 0,
  );
});

const dsMonDaLenGrouped = computed(() => {
  const grouped = {};
  dsMonChuaPhanBo.value.forEach((mon, index) => {
    const dm = mon.tenDanhMuc || "Khác";
    if (!grouped[dm]) grouped[dm] = [];
    grouped[dm].push({ ...mon, globalIndex: index });
  });
  return grouped;
});

const danhSachDanhMuc = computed(() => {
  return ["Tất cả", ...Object.keys(dsMonDaLenGrouped.value)];
});

const dsMonHienThiLoc = computed(() => {
  if (danhMucDangChon.value === "Tất cả") {
    return Object.values(dsMonDaLenGrouped.value)
      .flat()
      .sort((a, b) => a.globalIndex - b.globalIndex);
  }
  return dsMonDaLenGrouped.value[danhMucDangChon.value] || [];
});

const totalElements = computed(() => dsMonHienThiLoc.value.length);
const totalPages = computed(
  () => Math.ceil(totalElements.value / pageSize.value) || 1,
);

const danhSachHienThiPhanTrang = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return dsMonHienThiLoc.value.slice(start, end);
});

const pageList = computed(() => {
  const current = currentPage.value;
  const total = totalPages.value;
  if (total <= 5) {
    return Array.from({ length: total }, (_, i) => i + 1);
  }
  if (current <= 3) {
    return [1, 2, 3, 4, "…", total];
  }
  if (current >= total - 2) {
    return [1, "…", total - 3, total - 2, total - 1, total];
  }
  return [1, "…", current - 1, current, current + 1, "…", total];
});

watch([danhMucDangChon, pageSize], () => {
  currentPage.value = 1;
});

watch(totalPages, (total) => {
  if (currentPage.value > total) {
    currentPage.value = total;
  }
});

const localMode = ref(props.mode);
watch(
  () => props.mode,
  (val) => {
    localMode.value = val;
  },
  { immediate: true },
);

const titleModal = computed(() => {
  if (localMode.value === "ghep-ban") return "Ghép thêm bàn trống";
  if (localMode.value === "ghep-ban-dang-dung") return "Gộp bàn đang dùng";
  if (localMode.value === "tach-ban") return "Tách bàn";
  if (localMode.value === "doi-ban-checkin")
    return "Đổi bàn cho khách đặt trước";
  return "Đổi bàn";
});

const textSidebarHeader = computed(() => {
  const tenBanHienTai =
    props.banHienTai?.tenBanAn ||
    props.banHienTai?.maBanAn ||
    props.banHienTai?.tenBan ||
    "...";
  const tenBanMoi =
    dsBanDaChon.value.length > 0
      ? dsBanDaChon.value
          .map((b) => b.tenBanAn || b.maBanAn || b.tenBan)
          .join(", ")
      : "...";

  if (localMode.value === "ghep-ban") {
    return `Bàn ${tenBanHienTai} ghép thêm bàn ${tenBanMoi}`;
  }
  if (localMode.value === "ghep-ban-dang-dung") {
    return `Bàn ${tenBanHienTai} gộp với bàn ${tenBanMoi}`;
  }
  if (localMode.value === "tach-ban") {
    return `Bàn ${tenBanHienTai} tách ra bàn ${tenBanMoi}`;
  }
  return `Bàn ${tenBanHienTai} đổi qua bàn ${tenBanMoi}`;
});

const textConfirm = computed(() => {
  if (localMode.value === "ghep-ban") return "Xác nhận";
  if (localMode.value === "ghep-ban-dang-dung") return "Xác nhận";
  if (localMode.value === "tach-ban") return "Xác nhận";
  if (localMode.value === "doi-ban-checkin") return "Xác nhận";
  return "Xác nhận";
});

const laGhepBanTrong = computed(() => localMode.value === "ghep-ban");

const thongTinBanHienTai = computed(() => {
  return {
    tenBanAn:
      props.banHienTai?.tenBanAn ||
      props.banHienTai?.maBanAn ||
      props.banHienTai?.tenBan ||
      "Chưa xác định",
    tenKhuVuc:
      props.banHienTai?.tenKhuVuc ||
      props.banHienTai?.khuVuc?.tenKhuVuc ||
      "Chưa xác định",
    maHoaDon: props.banHienTai?.maHoaDon || "Chưa có",
    maDatBan: props.banHienTai?.maDatBan || "Chưa có",
    tenKhachHang: props.banHienTai?.tenKhachHang || "Khách vãng lai",
    soNguoi:
      props.banHienTai?.soNguoiDangDung ||
      props.banHienTai?.soNguoi ||
      props.banHienTai?.soNguoiToiDa ||
      props.banHienTai?.soNguoiDat ||
      "--",
  };
});

const laBanCoNhieuHoaDon = computed(() => {
  return (
    props.banHienTai?.modeTachHoaDon === true ||
    props.banHienTai?.duocGhepTuBanDangDung === true ||
    props.banHienTai?.actionGanNhat === "ghep-ban-dang-dung" ||
    props.banHienTai?.soHoaDonDangHoatDong >= 2 ||
    props.banHienTai?.soHoaDonTrongBan >= 2 ||
    props.banHienTai?.coNhieuHoaDon === true ||
    props.banHienTai?.laBanDungChungNhieuHoaDon === true
  );
});

const moTaAction = computed(() => {
  if (localMode.value === "ghep-ban") {
    return "Nhập số khách thêm trước. Nếu bàn hiện tại còn đủ ghế thì không cho ghép bàn mới.";
  }

  if (localMode.value === "ghep-ban-dang-dung") {
    return "Chọn một bàn đang sử dụng khác để gộp vào hóa đơn hiện tại";
  }

  if (localMode.value === "tach-ban") {
    return laBanCoNhieuHoaDon.value
      ? "Chọn 1 bàn trống hoặc có khách để tách hóa đơn này ra khỏi bàn đang dùng chung"
      : "Chọn ít nhất 1 bàn trống hoặc đang dùng (còn chỗ) để chuyển khách";
  }

  return "Chọn 1 hoặc nhiều bàn trống. Tổng sức chứa phải đủ số người đang dùng và không được dư quá 3 chỗ";
});

const danhSachTrangThai = [
  { value: 1, label: "Trống" },
  { value: 2, label: "Đã đặt" },
  { value: 3, label: "Đang sử dụng" },
  { value: 4, label: "Đang dọn" },
];

const getTrangThaiLabel = (value) => {
  return danhSachTrangThai.find((t) => t.value === Number(value))?.label || "Không xác định";
};

const getTenTrangThai = (trangThai) => {
  return getTrangThaiLabel(trangThai);
};

const getTrangThaiHienThi = (ban) => {
  if (ban.trangThaiBan !== undefined && ban.trangThaiBan !== null) {
    return Number(ban.trangThaiBan);
  }
  return 1;
};

const laBanGhep = (ban) => {
  return (
    Array.isArray(ban?.listIdBan) && ban.listIdBan.length > 1 && ban.idHoaDon
  );
};

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

const coBanOfflineKhaDung = () => {
  return banDaLoc.value.some((b) => {
    if (b.loaiBan !== "OFFLINE") return false;
    if (Number(b.trangThaiHienThi ?? b.trangThaiBan ?? b.trangThai) !== 1) return false;
    return true;
  });
};

const getClassTrangThai = (trangThai) => {
  const map = {
    1: "trong",
    2: "da_dat",
    3: "co_khach",
    4: "dang_don",
  };
  return map[trangThai] || "trong";
};

const layAnhMon = (hinhAnh) => {
  if (!hinhAnh) return "/default-food.png";

  if (typeof hinhAnh === "string") {
    if (hinhAnh.startsWith("http")) return hinhAnh;
    const clean = hinhAnh.replace(/^\/+/, "");
    return `http://localhost:8080/uploads/${clean}`;
  }
  return "/default-food.png";
};

function onImgError(event) {
  event.target.src = "/default-food.png";
}

const getIdBan = (table) => {
  return table?.idBanAn || table?.idBan || table?.id;
};

const getSucChuaBan = (table) => {
  const idTable = Number(table?.idBanAn || table?.idBan || table?.id);
  const idBanCurrent = Number(
    props.banHienTai?.idBanAn || props.banHienTai?.idBan,
  );
  if (localMode.value === "ghep-ban" && idTable === idBanCurrent) {
    return soGheTrongBanHienTai.value;
  }

  const trangThai = Number(table.trangThaiBan ?? table.trangThai);

  // Bàn đang dọn
  if (trangThai === 4) return 0;

  // Bàn trống hoặc đã đặt (chưa ai ngồi)
  if (trangThai === 1 || trangThai === 2) {
    return Number(table.sucChuaToiDa || 0);
  }

  // Bàn đang có khách
  if (trangThai === 3) {
    if (table.soGheTrong !== undefined && table.soGheTrong !== null) {
      return Math.max(Number(table.soGheTrong), 0);
    }
    const sucChua = Number(table.sucChuaToiDa || 0);
    const dangNgoi = Number(table.soNguoiDangDung || table.soKhach || 0);
    return Math.max(sucChua - dangNgoi, 0);
  }

  return Number(table?.sucChuaToiDa || 0);
};

const getSoGheDaDung = (ban) => {
  const sucChua = Number(
    ban?.sucChuaToiDa || ban?.sucChua || ban?.soNguoiToiDa || 0,
  );
  const tt = Number(ban?.trangThaiBan ?? ban?.trangThai ?? 0);

  if (tt === 1 || tt === 2) return 0;

  const soNguoiDangNgoi = Number(
    ban?.soNguoiDangDung || ban?.soKhach || ban?.soNguoi || 0,
  );
  return Math.min(Math.max(soNguoiDangNgoi, 0), sucChua);
};

const soNguoiDangDungHienTai = computed(() => {
  if (
    props.banHienTai?.soNguoiDangDung !== undefined &&
    props.banHienTai?.soNguoiDangDung !== null
  ) {
    return Number(props.banHienTai.soNguoiDangDung);
  }
  if (
    props.banHienTai?.soKhach !== undefined &&
    props.banHienTai?.soKhach !== null
  ) {
    return Number(props.banHienTai.soKhach);
  }
  if (
    props.banHienTai?.soNguoi !== undefined &&
    props.banHienTai?.soNguoi !== null
  ) {
    return Number(props.banHienTai.soNguoi);
  }
  if (
    props.banHienTai?.sucChuaToiDa !== undefined &&
    props.banHienTai?.soGheTrong !== undefined
  ) {
    return Math.max(
      Number(props.banHienTai.sucChuaToiDa) -
        Number(props.banHienTai.soGheTrong),
      0,
    );
  }
  return Number(
    props.banHienTai?.soNguoiDat || props.banHienTai?.soNguoiToiDa || 0,
  );
});

const sucChuaHienTai = computed(() => {
  return Number(
    props.banHienTai?.sucChuaToiDa ||
      props.banHienTai?.sucChua ||
      props.banHienTai?.soGhe ||
      0,
  );
});

const soGheTrongBanHienTai = computed(() => {
  if (
    props.banHienTai?.soGheTrong !== undefined &&
    props.banHienTai?.soGheTrong !== null
  ) {
    return Math.max(Number(props.banHienTai.soGheTrong || 0), 0);
  }

  return Math.max(sucChuaHienTai.value - soNguoiDangDungHienTai.value, 0);
});

const soKhachHienTai = computed(() => {
  return soNguoiDangDungHienTai.value;
});

const soKhachCanThemBangBanMoi = computed(() => {
  const soKhach = Number(soKhachThem.value || 0);
  return Math.max(soKhach - soGheTrongBanHienTai.value, 0);
});

const soKhachCanThem = computed(() => {
  if (localMode.value === "ghep-ban") {
    return soKhachThem.value || 0;
  }
  // Thêm điều kiện cho mode tách bàn: nếu có nhập số thì lấy số đó làm mục tiêu
  if (localMode.value === "tach-ban" && soKhachThem.value > 0) {
    return Number(soKhachThem.value);
  }

  return soKhachHienTai.value + (Number(soKhachThem.value) || 0);
});

const tongSucChuaDaChon = computed(() => {
  return dsBanDaChon.value.reduce((sum, b) => sum + getSucChuaBan(b), 0);
});

const soChoDuSauKhiChon = computed(() => {
  return Math.max(tongSucChuaDaChon.value - soKhachCanThem.value, 0);
});

const loadBan = async () => {
  try {
    nprogress.start();

    const resBan = await getDanhSachBan();

    const dataBan =
      resBan?.data?.data || resBan?.data?.content || resBan?.data || [];

    const dsBan = Array.isArray(dataBan) ? dataBan : [];
    danhSachBan.value = dsBan
      .filter((ban) => Number(ban.trangThaiBan ?? ban.trangThai ?? 0) !== 0)
      .map((ban) => ({
        ...ban,
        id: ban.idBanAn || ban.idBan || ban.id,
        trangThai: Number(ban.trangThaiBan ?? ban.trangThai ?? 0),
        tenBanAn: ban.tenBanAn || ban.tenBan || ban.maBanAn || "--",
        khuVucId: ban.khuVucId || ban.idKhuVuc || ban.khuVuc?.id,
        tenKhuVuc: ban.tenKhuVuc || ban.khuVuc?.tenKhuVuc || "Chưa xác định",
        sucChuaToiDa: Number(ban.sucChuaToiDa || 0),
        soGheTrong: Number(ban.soGheTrong || 0),
      }));

    const resKhuVuc = await getAllkhuvuc();
    const dataKhuVucRaw =
      resKhuVuc?.data?.data ||
      resKhuVuc?.data?.content ||
      resKhuVuc?.data ||
      [];

    // Chỉ lấy khu vực có trạng thái 1 (đang hoạt động)
    danhSachTang.value = Array.isArray(dataKhuVucRaw)
      ? dataKhuVucRaw.filter((kv) => Number(kv.trangThai) === 1)
      : [];

    if (danhSachTang.value.length > 0 && !tangDangChon.value) {
      const currentFloorId =
        props.banHienTai?.khuVucId ||
        props.banHienTai?.idKhuVuc ||
        props.banHienTai?.khuVuc?.id;
      if (
        currentFloorId &&
        danhSachTang.value.some((t) => t.id === currentFloorId)
      ) {
        tangDangChon.value = currentFloorId;
      } else {
        tangDangChon.value = danhSachTang.value[0].id;
      }
    }
  } catch (e) {
    console.error(e);
    notify.error("Lỗi load dữ liệu bàn!");
  } finally {
    nprogress.done();
  }
};

onMounted(() => {
  timerInterval = setInterval(() => {
    currentTime.value = new Date();
  }, 1000);
  loadBan();
});

onBeforeUnmount(() => {
  if (timerInterval) clearInterval(timerInterval);
});

watch(
  () => props.show,
  (val) => {
    if (!val) return;

    dsBanDaChon.value = [];
    soKhachThem.value = null;

    if (danhSachTang.value.length > 0) {
      const currentFloorId =
        props.banHienTai?.khuVucId ||
        props.banHienTai?.idKhuVuc ||
        props.banHienTai?.khuVuc?.id;
      if (
        currentFloorId &&
        danhSachTang.value.some((t) => t.id === currentFloorId)
      ) {
        tangDangChon.value = currentFloorId;
      } else {
        tangDangChon.value = danhSachTang.value[0].id;
      }
    } else {
      tangDangChon.value = "";
    }

    loadBan();
  },
  { immediate: true },
);

watch(
  () => props.mode,
  () => {
    dsBanDaChon.value = [];
    soKhachThem.value = null;
    loadBan();
  },
);

watch(soKhachThem, () => {
  errorSoKhachThem.value = "";
  if (localMode.value === "ghep-ban") {
    dsBanDaChon.value = [];
    if (soKhachThem.value > 0 && soGheTrongBanHienTai.value > 0) {
      dsBanDaChon.value.push(props.banHienTai);
    }
  } else if (localMode.value === "doi-ban") {
    dsBanDaChon.value = [];
  }
});

function chuyenSangDoiBan() {
  localMode.value = "doi-ban";
  dsBanDaChon.value = [];
  notify.info(
    "Đã chuyển sang chế độ Đổi bàn. Vui lòng chọn bàn mới đủ sức chứa cho tất cả khách!",
  );
}

const banDaLoc = computed(() => {
  if (!tangDangChon.value) return [];
  let list = danhSachBan.value
    .filter(
      (b) =>
        Number(b.khuVucId || b.khuVuc?.id || b.idKhuVuc) ===
          Number(tangDangChon.value) && getTrangThaiHienThi(b) !== 0,
    )
    .map((b) => ({
      ...b,
      trangThaiHienThi: getTrangThaiHienThi(b),
      dsHoaDon: sapXepHoaDonTheoThoiGianMo(b.dsHoaDon),
    }));
  return list;
});

const laBanLon = (ban) => ban && Number(ban.sucChuaToiDa || 0) >= 12;

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
  const banCuaTang = [...banDaLoc.value];

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
  banDaLoc,
  () => {
    khoiTaoSlot();
  },
  { deep: true, immediate: true },
);

function laBanDangChon(table) {
  return dsBanDaChon.value.some(
    (item) => Number(getIdBan(item)) === Number(getIdBan(table)),
  );
}

function coTheChonBan(table) {
  if (laBanDangChon(table)) return true;

  if (table?.loaiBan === "DU_PHONG" && !moBanDuPhong.value) return false;

  const trangThai = Number(table.trangThaiBan ?? table.trangThai);
  if (
    table?.loaiBan === "ONLINE" &&
    trangThai === 1 &&
    coBanOfflineKhaDung()
  ) {
    return false;
  }

  const idTable = Number(table?.idBanAn || table?.idBan || table?.id);
  const idBanCurrent = Number(
    props.banHienTai?.idBanAn || props.banHienTai?.idBan,
  );

  // 1. Nếu là gộp bàn đang dùng
  if (localMode.value === "ghep-ban-dang-dung") {
    return trangThai === 3 && idTable !== idBanCurrent;
  }

  // 3. LOGIC KIỂM TRA SỐ KHÁCH CHO MODE GHÉP BÀN TRỐNG
  if (localMode.value === "ghep-ban") {
    if (idTable === idBanCurrent) {
      return soKhachThem.value > 0 && soGheTrongBanHienTai.value > 0;
    }

    if (trangThai !== 1) return false;

    // Chưa nhập số khách hoặc nhập <= 0
    if (!soKhachThem.value || soKhachThem.value <= 0) return false;

    // VALIDATE CHÍNH: Nếu số khách thêm <= số ghế trống hiện tại -> Bàn cũ vẫn đủ ngồi, KHÔNG cho chọn bàn mới
    if (soKhachThem.value <= soGheTrongBanHienTai.value) return false;
  } else if (localMode.value === "tach-ban") {
    if (idTable === idBanCurrent) return false;

    // Tách bàn được phép chọn bàn trống hoặc bàn đang sử dụng nhưng còn chỗ
    if (trangThai !== 1 && trangThai !== 3) return false;

    // Nếu bàn có khách, phải còn ghế trống
    if (trangThai === 3 && getSucChuaBan(table) <= 0) return false;
  } else {
    // 2. Các mode khác bắt buộc bàn phải TRỐNG
    if (trangThai !== 1) return false;
  }

  const sucChuaBanMoi = getSucChuaBan(table);
  const tongDaChon = tongSucChuaDaChon.value;

  let mucTieuKhach = soKhachCanThem.value;

  // Nếu đã chọn đủ số ghế rồi thì không cho chọn thêm bàn nữa
  if (tongDaChon >= mucTieuKhach && mucTieuKhach > 0) {
    return false;
  }

  // Không cho ghép dư quá 2 chỗ
  const tongSauKhiChon = tongDaChon + sucChuaBanMoi;
  if (mucTieuKhach > 0 && tongSauKhiChon >= mucTieuKhach) {
    if (tongSauKhiChon - mucTieuKhach > 2) {
      return false;
    }
  }

  return true;
}

async function chonBan(table) {
  if (table?.loaiBan === "DU_PHONG" && !moBanDuPhong.value) {
    notify.error(
      "Bàn dự phòng đang bị khóa. Vui lòng nhấn 'Mở bàn dự phòng' để sử dụng!",
    );
    return;
  }

  const tt = Number(table.trangThaiBan ?? table.trangThai);
  if (
    table?.loaiBan === "ONLINE" &&
    tt === 1 &&
    coBanOfflineKhaDung()
  ) {
    notify.warning(
      "Ưu tiên chọn bàn OFFLINE (chỉ dùng bàn ONLINE khi hết bàn OFFLINE khả dụng)!",
    );
    return;
  }

  const idTable = getIdBan(table);
  if (!idTable) return;
  const idBanCurrent = Number(
    props.banHienTai?.idBanAn || props.banHienTai?.idBan,
  );

  // 1. KIỂM TRA CHO MODE GHÉP BÀN TRỐNG
  if (localMode.value === "ghep-ban") {
    if (!soKhachThem.value || soKhachThem.value <= 0) {
      errorSoKhachThem.value =
        "Vui lòng nhập số khách thêm trước khi chọn bàn!";
      return;
    }
    if (
      soKhachThem.value <= soGheTrongBanHienTai.value &&
      Number(idTable) !== idBanCurrent
    ) {
      notify.error(
        `Yêu cầu ghép vào bàn đang dùng vì bàn hiện tại vẫn đủ chỗ cho ${soKhachThem.value} người!`,
      );
      return;
    }
  }

  // 2. NẾU CLICK VÀO BÀN ĐÃ CHỌN RỒI -> HỦY CHỌN (XÓA KHỎI MẢNG)
  const index = dsBanDaChon.value.findIndex(
    (item) => Number(getIdBan(item)) === Number(idTable),
  );
  if (index >= 0) {
    const idBanCurrent = Number(
      props.banHienTai?.idBanAn || props.banHienTai?.idBan,
    );
    if (localMode.value === "ghep-ban" && Number(idTable) === idBanCurrent) {
      notify.warning(
        "Bàn hiện tại đang còn chỗ nên sẽ được ưu tiên ghép, không thể bỏ chọn!",
      );
      return;
    }
    dsBanDaChon.value.splice(index, 1);
    return;
  }

  // 3. KIỂM TRA TRẠNG THÁI BÀN (TRỐNG HAY CÓ KHÁCH)
  const trangThai = Number(table.trangThaiBan ?? table.trangThai);
  if (localMode.value === "ghep-ban-dang-dung") {
    if (trangThai !== 3) {
      notify.error("Chỉ được chọn bàn đang sử dụng để gộp!");
      return;
    }
  } else if (
    localMode.value === "ghep-ban" &&
    Number(idTable) === idBanCurrent
  ) {
    // Bypass check empty cho bàn hiện tại
  } else if (localMode.value === "tach-ban") {
    if (Number(idTable) === idBanCurrent) {
      notify.warning("Không thể chọn bàn đang tách!");
      return;
    }
    if (trangThai !== 1 && trangThai !== 3) {
      notify.error(
        "Chỉ được chọn bàn trống hoặc bàn đang sử dụng nhưng còn chỗ!",
      );
      return;
    }
    if (trangThai === 3 && getSucChuaBan(table) <= 0) {
      notify.error("Bàn này đã hết chỗ trống!");
      return;
    }
  } else {
    if (trangThai !== 1) {
      notify.error("Bàn này không còn trống!");
      return;
    }
  }

  // 4. KIỂM TRA SỨC CHỨA (CHẶN CHỌN DƯ QUÁ 2 CHỖ)
  if (localMode.value !== "ghep-ban-dang-dung") {
    let mucTieuKhach = soKhachCanThem.value;

    if (tongSucChuaDaChon.value >= mucTieuKhach && mucTieuKhach > 0) {
      notify.error("Số lượng bàn đã chọn đã đủ sức chứa, không cần chọn thêm!");
      return;
    }

    const sucChuaBanMoi = getSucChuaBan(table);
    const tongSauKhiChon = tongSucChuaDaChon.value + sucChuaBanMoi;

    if (mucTieuKhach > 0 && tongSauKhiChon >= mucTieuKhach) {
      const soChoDu = tongSauKhiChon - mucTieuKhach;
      if (soChoDu > 3) {
        notify.error(
          `Chọn bàn này sẽ dư ${soChoDu} chỗ (vượt mức cho phép 3 chỗ). Vui lòng chọn bàn nhỏ hơn!`,
        );
        return;
      }
    }
  } else {
    if (dsBanDaChon.value.length >= 1) {
      notify.error("Chỉ được chọn 1 bàn để gộp!");
      return;
    }

    const getSoKhachDangNgoiTable = (tbl) => {
      if (tbl?.soNguoiDangDung !== undefined && tbl?.soNguoiDangDung !== null) {
        return Number(tbl.soNguoiDangDung);
      }
      if (tbl?.soKhach !== undefined && tbl?.soKhach !== null) {
        return Number(tbl.soKhach);
      }
      if (tbl?.soNguoi !== undefined && tbl?.soNguoi !== null) {
        return Number(tbl.soNguoi);
      }
      if (tbl?.sucChuaToiDa !== undefined && tbl?.soGheTrong !== undefined) {
        return Math.max(Number(tbl.sucChuaToiDa) - Number(tbl.soGheTrong), 0);
      }
      return Number(tbl?.soNguoiDat || tbl?.soNguoiToiDa || 0);
    };

    const soKhachBanChinh = soKhachHienTai.value;
    const sucChuaBanChinh = sucChuaHienTai.value;
    const soKhachBanGhep = getSoKhachDangNgoiTable(table);

    if (soKhachBanChinh + soKhachBanGhep > sucChuaBanChinh) {
      notify.error(
        `Bàn hiện tại không đủ chỗ! (Đang có ${soKhachBanChinh}, ghép thêm ${soKhachBanGhep} sẽ vượt sức chứa ${sucChuaBanChinh})`,
      );
      return;
    }
  }

  // 5. ĐẨY BÀN VÀO DANH SÁCH BÊN PHẢI (CHỖ NÀY LÀ QUAN TRỌNG NHẤT)
  if (localMode.value === "tach-ban") {
    // LOGIC MỚI: Tự động điền số khách tối đa có thể chuyển
    const soKhachConLai = soKhachBanGoc.value - tongKhachChuyen.value;
    if (soKhachConLai <= 0) {
      notify.error("Đã xếp đủ khách, không thể chọn thêm bàn!");
      return;
    }

    const sucChuaBanMoi = getSucChuaBan(table);
    const soKhachUuTien = Math.min(soKhachConLai, sucChuaBanMoi);

    dsBanDaChon.value.push({ ...table, soKhachChuyen: soKhachUuTien });

    if (dsBanDaChon.value.length === 2) {
      tachHoaDonSelected.value = await notify.confirm(
        "Bạn có muốn tách hóa đơn cho các bàn mới không?",
      );
    }
  } else {
    // Các mode khác (Đổi bàn, Ghép bàn) thì đẩy data bình thường
    dsBanDaChon.value.push(table);
  }
}

function xoaBanDaChon(ban) {
  const idBanCurrent = Number(
    props.banHienTai?.idBanAn || props.banHienTai?.idBan,
  );
  if (
    localMode.value === "ghep-ban" &&
    Number(getIdBan(ban)) === idBanCurrent
  ) {
    notify.warning(
      "Bàn hiện tại đang còn chỗ nên sẽ được ưu tiên ghép, không thể bỏ chọn!",
    );
    return;
  }
  dsBanDaChon.value = dsBanDaChon.value.filter(
    (item) => Number(getIdBan(item)) !== Number(getIdBan(ban)),
  );
  if (localMode.value === "tach-ban" && dsBanDaChon.value.length < 2) {
    tachHoaDonSelected.value = false;
  }
}

function closePopup() {
  emit("close");
}

async function xacNhanChonBan() {
  // --- 1. XỬ LÝ ĐỔI BÀN ---
  if (localMode.value === "doi-ban" || localMode.value === "doi-ban-checkin") {
    if (!dsBanDaChon.value.length) {
      notify.error("Vui lòng chọn ít nhất 1 bàn muốn đổi!");
      return;
    }

    const soKhach = soKhachHienTai.value + (Number(soKhachThem.value) || 0);
    const tongSucChua = tongSucChuaDaChon.value;

    if (tongSucChua < soKhach) {
      notify.error(
        `Bàn mới không đủ chỗ. Cần ${soKhach} chỗ, hiện có ${tongSucChua} chỗ`,
      );
      return;
    }

    if (tongSucChua - soKhach > 3) {
      notify.error(
        `Không được đổi bàn dư quá 3 chỗ. Hiện đang dư ${
          tongSucChua - soKhach
        } chỗ`,
      );
      return;
    }

    emit("confirm", {
      mode: localMode.value,
      idBanMoi: getIdBan(dsBanDaChon.value[0]),
      listIdBanMoi: dsBanDaChon.value.map((b) => getIdBan(b)),
      dsBanDaChon: dsBanDaChon.value,
      soKhachThem: Number(soKhachThem.value) || 0,
    });
    return;
  }

  // --- 2. XỬ LÝ GHÉP THÊM BÀN TRỐNG ---
  if (localMode.value === "ghep-ban") {
    const soKhach = Number(soKhachThem.value || 0);
    if (soKhach <= 0) {
      errorSoKhachThem.value = "Vui lòng nhập số khách thêm hợp lệ!";
      return;
    }

    if (!dsBanDaChon.value.length) {
      notify.error("Vui lòng chọn ít nhất 1 bàn để ghép!");
      return;
    }

    if (tongSucChuaDaChon.value < soKhach) {
      notify.error(
        `Bàn đã chọn không đủ chỗ cho ${soKhach} khách thêm. Vui lòng thao tác Đổi bàn hoặc chọn thêm bàn trống để ghép!`,
      );
      return;
    }

    emit("confirm", {
      mode: "ghep-ban",
      soKhachThem: soKhach,
      listIdBanMuonGhep: dsBanDaChon.value.map((item) => getIdBan(item)),
    });
    return;
  }

  // --- 3. XỬ LÝ TÁCH BÀN ---
  if (localMode.value === "tach-ban") {
    const dangTachHoaDon = laBanCoNhieuHoaDon.value;

    if (dangTachHoaDon) {
      if (dsBanDaChon.value.length !== 1) {
        notify.error("Vui lòng chọn đúng 1 bàn để tách hóa đơn");
        return;
      }
      emit("confirm", {
        mode: "tach-ban",
        laBanCoNhieuHoaDon: true,
        idHoaDon: props.banHienTai?.idHoaDon,
        idBanGoc: getIdBan(props.banHienTai),
        danhSachBanMoi: [
          {
            idBan: getIdBan(dsBanDaChon.value[0]),
            soKhach: soKhachHienTai.value,
          },
        ],
      });
    } else {
      if (dsBanDaChon.value.length === 0) {
        notify.error("Vui lòng chọn ít nhất 1 bàn mới để tách khách");
        return;
      }

      // Validate từng bàn
      let hasError = false;
      for (const ban of dsBanDaChon.value) {
        ban.error = "";
        if (!ban.soKhachChuyen || ban.soKhachChuyen <= 0) {
          ban.error = `Vui lòng nhập số khách cho bàn ${ban.tenBanAn}`;
          hasError = true;
        } else if (ban.soKhachChuyen > getSucChuaBan(ban)) {
          ban.error = `Bàn ${ban.tenBanAn} chỉ còn ${getSucChuaBan(
            ban,
          )} chỗ trống!`;
          hasError = true;
        }
      }
      if (hasError) return;

      const tongKhachDinhChuyen = dsBanDaChon.value.reduce(
        (sum, b) => sum + Number(b.soKhachChuyen),
        0,
      );
      const tongSucChuaBanMoi = dsBanDaChon.value.reduce(
        (sum, b) => sum + Number(b.sucChuaToiDa),
        0,
      );

      if (tongKhachDinhChuyen > soKhachBanGoc.value) {
        notify.error(
          `Tổng khách chuyển (${tongKhachDinhChuyen}) vượt quá số khách gốc (${soKhachBanGoc.value})`,
        );
        return;
      }

      // Validate trống quá 2 ghế
      const soGheTrongDuKien = tongSucChuaBanMoi - tongKhachDinhChuyen;
      if (soGheTrongDuKien > 2) {
        notify.error(
          `Tổng bàn mới (${tongSucChuaBanMoi} chỗ) quá rộng so với ${tongKhachDinhChuyen} khách. (Dư ${soGheTrongDuKien} chỗ, tối đa cho dư 2)`,
        );
        return;
      }

      const payloadBan = {
        mode: "tach-ban",
        laBanCoNhieuHoaDon: false,
        idHoaDon: props.banHienTai?.idHoaDon,
        idBanGoc: getIdBan(props.banHienTai),
        tachHoaDon: tachHoaDonSelected.value,
        danhSachBanMoi: dsBanDaChon.value.map((b) => ({
          idBan: getIdBan(b),
          tenBan: b.tenBanAn || b.tenBan,
          soKhach: Number(b.soKhachChuyen),
        })),
      };

      const dsHopLe = (props.banHienTai?.dsMon || []).filter(
        (m) => (m.trangThai === 1 || m.trangThai === 0) && m.soLuong > 0,
      );

      if (dsHopLe.length > 0) {
        danhSachBanMoi.value = payloadBan.danhSachBanMoi;
        banNhanSelected.value = danhSachBanMoi.value[0]?.idBan;
        danhSachBanMoi.value.forEach((b) => {
          if (!soLuongTachTheoBan.value[b.idBan]) {
            soLuongTachTheoBan.value[b.idBan] = {};
          }
        });
        dsMonHienThi.value = dsHopLe.map((m) => ({ ...m }));
        stepTachBan.value = 2;
        return;
      }

      emit("confirm", payloadBan);
    }
    return;
  }

  // --- 4. GỘP BÀN ---
  if (localMode.value === "ghep-ban-dang-dung") {
    if (!dsBanDaChon.value.length) {
      notify.error("Vui lòng chọn bàn đang sử dụng cần gộp");
      return;
    }
    emit("confirm", {
      mode: "ghep-ban-dang-dung",
      idHoaDonChinh: props.banHienTai?.idHoaDon,
      idHoaDonGhep: dsBanDaChon.value[0].idHoaDon,
      idBanChinh: getIdBan(props.banHienTai),
      idBanGhep: getIdBan(dsBanDaChon.value[0]),
    });
  }
}

// Lấy số khách hiện tại của bàn gốc để validate
const soKhachBanGoc = computed(() => {
  return soNguoiDangDungHienTai.value;
});

// Tính tổng số khách đang định chuyển sang các bàn
const tongKhachChuyen = computed(() => {
  if (localMode.value !== "tach-ban") return 0;
  return dsBanDaChon.value.reduce(
    (sum, b) => sum + (Number(b.soKhachChuyen) || 0),
    0,
  );
});

// --- DISH SPLITTING LOGIC (STEP 2) ---
function getSoLuongChuyen(idMon) {
  if (
    !banNhanSelected.value ||
    !soLuongTachTheoBan.value[banNhanSelected.value]
  )
    return 0;
  return soLuongTachTheoBan.value[banNhanSelected.value][idMon] || 0;
}

function getSoLuongDaChuyenCacBanKhac(idMon, idBanHienTai) {
  let tong = 0;
  for (const [idBan, ds] of Object.entries(soLuongTachTheoBan.value)) {
    if (Number(idBan) !== Number(idBanHienTai)) {
      tong += ds[idMon] || 0;
    }
  }
  return tong;
}

function getSoLuongKhaDung(mon) {
  return (
    mon.soLuong - getSoLuongDaChuyenCacBanKhac(mon.id, banNhanSelected.value)
  );
}

function tangSoLuong(mon) {
  if (!banNhanSelected.value) {
    notify.warning("Vui lòng chọn bàn nhận món trước!");
    return;
  }
  const max = getSoLuongKhaDung(mon);
  const hienTai = getSoLuongChuyen(mon.id);
  if (hienTai < max) {
    soLuongTachTheoBan.value[banNhanSelected.value][mon.id] = hienTai + 1;
  }
}

function giamSoLuong(mon) {
  if (!banNhanSelected.value) return;
  const hienTai = getSoLuongChuyen(mon.id);
  if (hienTai > 0) {
    soLuongTachTheoBan.value[banNhanSelected.value][mon.id] = hienTai - 1;
  }
}

function updateSoLuongChuyen(mon, event) {
  if (!banNhanSelected.value) {
    event.target.value = 0;
    return;
  }
  let val = parseInt(event.target.value) || 0;
  const max = getSoLuongKhaDung(mon);
  if (val > max) val = max;
  if (val < 0) val = 0;
  soLuongTachTheoBan.value[banNhanSelected.value][mon.id] = val;
  event.target.value = val;
}

function chonBanNhan(idBan) {
  banNhanSelected.value = idBan;
}

function getMonDaChonTheoBan(idBan) {
  const dsTheoBan = soLuongTachTheoBan.value[idBan] || {};
  return dsMonHienThi.value
    .map((mon) => ({
      ...mon,
      soLuongDaChon: dsTheoBan[mon.id] || 0,
    }))
    .filter((mon) => mon.soLuongDaChon > 0);
}

function getTongMonDaChonTheoBan(idBan) {
  return getMonDaChonTheoBan(idBan).reduce(
    (tong, mon) => tong + mon.soLuongDaChon,
    0,
  );
}

function xoaMonDaChon(idBan, idMon) {
  if (!soLuongTachTheoBan.value[idBan]) return;
  soLuongTachTheoBan.value[idBan][idMon] = 0;
}

function chuyenTatCa() {
  if (!banNhanSelected.value) {
    notify.warning("Vui lòng chọn bàn nhận món trước!");
    return;
  }
  dsMonHienThi.value.forEach((m) => {
    const max = getSoLuongKhaDung(m);
    if (max > 0) {
      soLuongTachTheoBan.value[banNhanSelected.value][m.id] = max;
    }
  });
}

function xacNhanTachMon() {
  const danhSachMonTach = [];

  for (const [idBan, ds] of Object.entries(soLuongTachTheoBan.value)) {
    for (const [idMon, soLuong] of Object.entries(ds)) {
      if (soLuong > 0) {
        danhSachMonTach.push({
          idHoaDonChiTiet: Number(idMon),
          soLuongTach: soLuong,
          idBanNhan: Number(idBan),
        });
      }
    }
  }

  const payloadBan = {
    mode: "tach-ban",
    laBanCoNhieuHoaDon: false,
    idHoaDon: props.banHienTai?.idHoaDon,
    idBanGoc: getIdBan(props.banHienTai),
    danhSachBanMoi: danhSachBanMoi.value,
    danhSachMonTach: danhSachMonTach,
    tachHoaDon: tachHoaDonSelected.value,
  };

  emit("confirm", payloadBan);
}
</script>

<template>
  <div v-if="show" class="modal-overlay">
    <div class="modal-title">
      <h1>{{ titleModal }}</h1>
      <button class="btn-close-modal" @click="closePopup">✕</button>
    </div>

    <div class="info-section">
      <div class="info-top">
        <div class="info-block current-table">
          <div class="info-label">
            <i class="ri-table-line"></i>
            Bàn đang dùng
          </div>
          <div class="info-value">
            {{ thongTinBanHienTai.tenKhuVuc }} -
            {{ thongTinBanHienTai.tenBanAn }}
          </div>
        </div>

        <div class="info-block">
          <div class="info-label">
            <i class="ri-receipt-line"></i>
            Mã hóa đơn
          </div>
          <div class="info-value">{{ thongTinBanHienTai.maHoaDon }}</div>
        </div>

        <div class="info-block">
          <div class="info-label">
            <i class="ri-calendar-check-line"></i>
            Mã đặt bàn
          </div>
          <div class="info-value">{{ thongTinBanHienTai.maDatBan }}</div>
        </div>

        <div class="info-block">
          <div class="info-label">
            <i class="ri-group-line"></i>
            Số người hiện tại
          </div>
          <div class="info-value">{{ thongTinBanHienTai.soNguoi }}</div>
        </div>

        <div class="info-block">
          <div class="info-label">
            <i class="ri-armchair-line"></i>
            Ghế hiện tại trống
          </div>
          <div class="info-value">{{ soGheTrongBanHienTai }}</div>
        </div>
      </div>

      <div
        v-if="
          localMode === 'ghep-ban' ||
          localMode === 'tach-ban' ||
          localMode === 'doi-ban'
        "
        class="so-khach-them-box"
      >
        <div
          v-if="localMode === 'ghep-ban' || localMode === 'doi-ban'"
          class="input-group"
        >
          <label> <i class="ri-user-add-line"></i> Số khách thêm </label>
          <input
            v-model.number="soKhachThem"
            type="number"
            min="0"
            step="1"
            class="input-so-khach-them"
            :class="{ 'input-invalid': errorSoKhachThem }"
            :placeholder="
              localMode === 'ghep-ban'
                ? 'Bắt buộc nhập số khách thêm'
                : 'Khách thêm (nếu có)'
            "
          />
        </div>
        <transition name="shake">
          <p
            v-if="errorSoKhachThem"
            class="error-hint"
            style="margin-left: 10px; margin-bottom: 10px"
          >
            <i class="bi bi-exclamation-circle"></i> {{ errorSoKhachThem }}
          </p>
        </transition>

        <div
          class="chon-note"
          :style="{ width: localMode === 'tach-ban' ? '100%' : 'auto' }"
        >
          <template v-if="localMode === 'ghep-ban'">
            Bàn hiện tại còn <b>{{ soGheTrongBanHienTai }}</b> ghế.
            <span v-if="soKhachCanThemBangBanMoi > 0">
              Cần chọn thêm <b>{{ soKhachCanThemBangBanMoi }}</b> chỗ từ bàn
              khác.<br />
            </span>
            <span v-else><br /></span>
            Đã chọn <b>{{ tongSucChuaDaChon }}</b> chỗ.
            <span v-if="tongSucChuaDaChon >= soKhachThem">
              Dư <b>{{ soChoDuSauKhiChon }}</b> chỗ.
            </span>

            <div
              v-if="Number(soKhachThem) > soGheTrongBanHienTai"
              class="alert-doi-ban"
            >
              <div class="alert-content">
                <i class="ri-error-warning-line icon-warning"></i>
                <span
                  >Bàn hiện tại không đủ chỗ. Thay vì ghép thêm bàn mới, bạn có
                  thể chuyển tất cả khách sang 1 bàn lớn hơn!</span
                >
              </div>
              <button
                class="btn-modern btn-chuyen-ban"
                @click="chuyenSangDoiBan"
              >
                <i class="ri-exchange-line"></i> Đổi sang bàn khác
              </button>
            </div>
          </template>

          <template v-else-if="localMode === 'doi-ban'">
            <div v-if="Number(soKhachThem) > 0">
              Chuyển <b>{{ soNguoiDangDungHienTai }}</b> khách cũ +
              <b>{{ soKhachThem }}</b> khách mới.<br />
              Tổng:
              <b style="color: #b91c1c; font-size: 16px">{{
                soNguoiDangDungHienTai + Number(soKhachThem)
              }}</b>
              khách.
            </div>
            <div v-else>
              Chuyển <b>{{ soNguoiDangDungHienTai }}</b> khách sang bàn mới.
            </div>
          </template>

          <template v-else-if="localMode === 'tach-ban' && !laBanCoNhieuHoaDon">
            <div style="font-size: 15px; margin-bottom: 4px">
              <i class="ri-user-search-line"></i>
              Đang tách:
              <b style="color: #b91c1c; font-size: 18px">{{
                tongKhachChuyen
              }}</b>
              / {{ soKhachBanGoc }} khách
            </div>
            <span
              >Hãy nhập số lượng khách muốn chuyển vào từng bàn ở danh sách bên
              phải.</span
            >
          </template>
        </div>
      </div>

      <div class="info-note">
        {{ moTaAction }}
      </div>
    </div>

    <template v-if="stepTachBan === 1">
      <div class="modal-main-layout">
        <div class="selection-section">
          <div class="khu-tang">
            <div class="list-tang">
              <div
                v-for="tang in danhSachTang"
                :key="tang.id"
                class="the-tang"
                :class="{ active: Number(tangDangChon) === Number(tang.id) }"
                @click="tangDangChon = tang.id"
              >
                {{ tang.tenKhuVuc }}
              </div>
            </div>
          </div>

          <div class="khu-chu-thich">
            <div><span class="dot do"></span> Có khách</div>
            <div><span class="dot xanh-la"></span> Trống</div>
            <div><span class="dot vang"></span> Đang dọn</div>
            <div><span class="dot xanh-duong"></span> Đã đặt</div>
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

          <div class="khu-so-do custom-scrollbar">
            <div v-if="banDaLoc.length === 0" class="empty-state">
              Không có bàn ở tầng này
            </div>

            <div v-else class="grid-ban">
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
                      : getClassTrangThai(
                          Number(
                            item.ban.trangThaiHienThi ??
                              item.ban.trangThaiBan ??
                              item.ban.trangThai,
                          ),
                        ),
                    {
                      'large-vertical': laBanLon(item.ban),
                      'ban-ghep': laBanGhep(item.ban),
                      selected: laBanDangChon(item.ban),
                      'not-available':
                        !coTheChonBan(item.ban) ||
                        (item.ban.loaiBan === 'DU_PHONG' && !moBanDuPhong) ||
                        (item.ban.loaiBan === 'ONLINE' &&
                          Number(
                            item.ban.trangThaiHienThi ??
                              item.ban.trangThaiBan ??
                              item.ban.trangThai,
                          ) === 1 &&
                          coBanOfflineKhaDung()),
                    },
                  ]"
                  @click="chonBan(item.ban)"
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
                      Number(
                        item.ban.trangThaiHienThi ??
                          item.ban.trangThaiBan ??
                          item.ban.trangThai,
                      ) === 1 &&
                      coBanOfflineKhaDung()
                    "
                    class="invalid-overlay"
                  >
                    <span>ƯU TIÊN ONLINE</span>
                  </div>

                  <div v-if="laBanGhep(item.ban)" class="icon-ban-ghep">
                    <i class="ri-links-line"></i>
                  </div>

                  <div v-if="laBanDangChon(item.ban)" class="corner-check">
                    <i class="ri-checkbox-circle-fill"></i>
                  </div>

                  <div class="table-header">
                    <span class="table-code">{{ item.ban.tenBanAn }}</span>
                    <span
                      class="table-status-text"
                      :style="{
                        color:
                          Number(
                            item.ban.trangThaiHienThi ??
                              item.ban.trangThaiBan ??
                              item.ban.trangThai,
                          ) === 1
                            ? '#4caf50'
                            : Number(
                                item.ban.trangThaiHienThi ??
                                  item.ban.trangThaiBan ??
                                  item.ban.trangThai,
                              ) === 2
                            ? '#42a5f5'
                            : Number(
                                item.ban.trangThaiHienThi ??
                                  item.ban.trangThaiBan ??
                                  item.ban.trangThai,
                              ) === 3
                            ? '#f44336'
                            : Number(
                                item.ban.trangThaiHienThi ??
                                  item.ban.trangThaiBan ??
                                  item.ban.trangThai,
                              ) === 4
                            ? '#ff9800'
                            : '#9e9e9e',
                      }"
                    >
                      {{
                        getTrangThaiLabel(
                          Number(
                            item.ban.trangThaiHienThi ??
                              item.ban.trangThaiBan ??
                              item.ban.trangThai,
                          ),
                        )
                      }}
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
                    {{ getSoGheDaDung(item.ban) }}/{{
                      item.ban.sucChuaToiDa ||
                      item.ban.sucChua ||
                      item.ban.soNguoiToiDa ||
                      0
                    }}
                    chỗ
                  </div>

                  <div class="table-footer">
                    <div
                      class="table-invoice-text"
                      style="display: flex; flex-direction: column; width: 100%"
                    >
                      <template
                        v-if="
                          Number(
                            item.ban.trangThaiHienThi ??
                              item.ban.trangThaiBan ??
                              item.ban.trangThai,
                          ) === 3
                        "
                      >
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
                              {{
                                item.ban.maHoaDon ||
                                item.ban.dsHoaDon?.[0]?.maHoaDon ||
                                ""
                              }}
                            </span>
                            <span style="font-weight: 600">
                              {{
                                formatCurrency(
                                  item.ban.tienConLai ??
                                    item.ban.tongTien ??
                                    item.ban.dsHoaDon?.[0]?.tongTien ??
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
                <div
                  v-else-if="!item.blocked"
                  :key="'slot-empty-' + item.id"
                  class="slot-empty"
                  style="min-height: 120px"
                ></div>
              </template>
            </div>
          </div>
        </div>

        <div class="selected-sidebar">
          <div class="sidebar-header">
            {{ textSidebarHeader }}
          </div>

          <div
            v-if="localMode === 'ghep-ban' || localMode === 'doi-ban'"
            class="side-summary"
          >
            <template v-if="localMode === 'ghep-ban'">
              <div>
                Số khách thêm: <b>{{ soKhachThem || 0 }}</b>
              </div>
              <div>
                Ghế trống hiện tại: <b>{{ soGheTrongBanHienTai }}</b>
              </div>
              <div>
                Cần bàn mới: <b>{{ soKhachCanThemBangBanMoi }}</b> chỗ
              </div>
              <div>
                Đã chọn: <b>{{ tongSucChuaDaChon }}</b> chỗ
              </div>
              <div>
                Dư: <b>{{ soChoDuSauKhiChon }}</b> chỗ
              </div>
            </template>
            <template v-else-if="localMode === 'doi-ban'">
              <div>
                Tổng khách lúc sau:
                <b>{{ soNguoiDangDungHienTai + (Number(soKhachThem) || 0) }}</b>
              </div>
              <div>
                Bàn mới đã chọn: <b>{{ tongSucChuaDaChon }}</b> chỗ
              </div>
              <div
                v-if="
                  tongSucChuaDaChon >=
                  soNguoiDangDungHienTai + (Number(soKhachThem) || 0)
                "
              >
                Dư:
                <b>{{
                  tongSucChuaDaChon -
                  (soNguoiDangDungHienTai + (Number(soKhachThem) || 0))
                }}</b>
                chỗ
              </div>
            </template>
          </div>

          <div class="selected-list">
            <div v-if="dsBanDaChon.length === 0" class="empty-state">
              <p>Chưa có bàn nào</p>
            </div>

            <div
              v-for="item in dsBanDaChon"
              :key="getIdBan(item)"
              class="selected-tag-item"
              :style="{
                flexDirection:
                  localMode === 'tach-ban' && !laBanCoNhieuHoaDon
                    ? 'column'
                    : 'row',
                alignItems: 'stretch',
              }"
            >
              <div
                style="
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  width: 100%;
                "
              >
                <div class="tag-info">
                  <span class="tag-name">Bàn {{ item.tenBanAn }}</span>
                  <span class="tag-area">
                    {{
                      item.tenKhuVuc ||
                      item.khuVuc?.tenKhuVuc ||
                      "Chưa xác định"
                    }}
                    - Còn {{ getSucChuaBan(item) }} ghế
                  </span>
                </div>
                <button class="remove-tag" @click="xoaBanDaChon(item)">
                  ×
                </button>
              </div>

              <div
                v-if="localMode === 'tach-ban' && !laBanCoNhieuHoaDon"
                class="guest-split-input"
              >
                <label>Chuyển sang:</label>
                <div class="guest-split-control">
                  <input
                    type="number"
                    v-model.number="item.soKhachChuyen"
                    min="1"
                    :max="getSucChuaBan(item)"
                    :class="{ 'input-invalid': item.error }"
                    @input="item.error = ''"
                  />
                  <span>khách</span>
                </div>
                <transition name="shake">
                  <p v-if="item.error" class="error-hint mt-2 mb-0">
                    <i class="bi bi-exclamation-circle"></i> {{ item.error }}
                  </p>
                </transition>
              </div>
            </div>
          </div>

          <div class="sidebar-footer">
            <button class="btn-cancel" @click="closePopup">
              <i class="ri-close-line"></i> Hủy bỏ
            </button>

            <button
              class="btn-primary"
              :disabled="
                (localMode === 'ghep-ban' &&
                  (!soKhachThem ||
                    Number(soKhachThem) <= 0 ||
                    dsBanDaChon.length === 0 ||
                    tongSucChuaDaChon < Number(soKhachThem))) ||
                (localMode === 'tach-ban' &&
                  !laBanCoNhieuHoaDon &&
                  (dsBanDaChon.length === 0 ||
                    tongKhachChuyen <= 0 ||
                    tongKhachChuyen > soKhachBanGoc)) ||
                ((localMode === 'doi-ban' || localMode === 'doi-ban-checkin') &&
                  dsBanDaChon.length === 0)
              "
              @click="xacNhanChonBan"
            >
              <div class="btn-light-sweep"></div>
              <i class="ri-check-line"></i>
              {{
                localMode === "tach-ban" &&
                (props.banHienTai?.dsMon || []).filter(
                  (m) =>
                    (m.trangThai === 1 || m.trangThai === 0) && m.soLuong > 0,
                ).length > 0
                  ? "Xác nhận tách bàn"
                  : textConfirm
              }}
            </button>
          </div>
        </div>
      </div>
    </template>

    <!-- BƯỚC 2: CHIA MÓN CHO CÁC BÀN MỚI (DÀNH CHO TÁCH BÀN) -->
    <template v-else-if="stepTachBan === 2">
      <div class="modal-main-layout tach-mon-view">
        <aside class="tach-mon-sidebar">
          <div class="tach-mon-sidebar-header">
            <div class="tach-mon-sidebar-title">Bàn nhận món</div>
            <div class="tach-mon-sidebar-subtitle">
              Chọn bàn để phân bổ món ăn
            </div>
          </div>

          <div v-if="danhSachBanMoi.length > 0" class="tach-mon-table-list">
            <button
              v-for="ban in danhSachBanMoi"
              :key="ban.idBan"
              type="button"
              class="table-option"
              :class="{ active: banNhanSelected === ban.idBan }"
              @click="chonBanNhan(ban.idBan)"
            >
              <span class="table-option-top">
                <span class="table-option-title">Bàn {{ ban.tenBan }}</span>
                <span
                  v-if="getTongMonDaChonTheoBan(ban.idBan) > 0"
                  class="table-option-badge"
                >
                  {{ getTongMonDaChonTheoBan(ban.idBan) }} món
                </span>
              </span>
              <span class="table-option-meta">{{ ban.soKhach }} khách</span>
              <span
                v-if="
                  banNhanSelected === ban.idBan &&
                  getMonDaChonTheoBan(ban.idBan).length > 0
                "
                class="selected-dish-list"
              >
                <span
                  v-for="monDaChon in getMonDaChonTheoBan(ban.idBan)"
                  :key="monDaChon.id"
                  class="selected-dish-item"
                >
                  <span class="selected-dish-name">
                    {{ monDaChon.tenSanPham || monDaChon.tenMonAn }}
                  </span>
                  <span class="selected-dish-actions">
                    <span class="selected-dish-qty">
                      x{{ monDaChon.soLuongDaChon }}
                    </span>
                    <button
                      type="button"
                      class="selected-dish-remove"
                      title="Xóa món khỏi bàn này"
                      @click.stop="xoaMonDaChon(ban.idBan, monDaChon.id)"
                    >
                      <i class="ri-close-line"></i>
                    </button>
                  </span>
                </span>
              </span>
              <span
                v-else-if="banNhanSelected === ban.idBan"
                class="selected-dish-empty"
              >
                Chưa chọn món
              </span>
            </button>
          </div>

          <div v-else class="empty-state">Không có bàn mới để chuyển món</div>
        </aside>

        <div class="tach-mon-main">
          <div class="tach-mon-main-header">
            <div>
              <div class="tach-mon-main-title">Danh sách món cần tách</div>
              <div class="tach-mon-main-subtitle">
                Chọn số lượng món cho bàn được chọn
              </div>
            </div>
            <button
              class="tach-mon-action"
              :disabled="!banNhanSelected || dsMonChuaPhanBo.length === 0"
              @click="chuyenTatCa"
            >
              <i class="ri-share-forward-line"></i>
              <span>Chuyển tất cả</span>
            </button>
          </div>

          <div class="category-tabs" v-if="dsMonChuaPhanBo.length > 0">
            <button
              v-for="dm in danhSachDanhMuc"
              :key="dm"
              class="tab-btn"
              :class="{ active: danhMucDangChon === dm }"
              @click="danhMucDangChon = dm"
            >
              {{ dm }} ({{
                dm === "Tất cả"
                  ? dsMonChuaPhanBo.length
                  : (dsMonDaLenGrouped &&
                      dsMonDaLenGrouped[dm] &&
                      dsMonDaLenGrouped[dm].length) ||
                    0
              }})
            </button>
          </div>

          <div class="table-container">
            <table class="mon-table">
              <colgroup>
                <col class="col-mon-name" />
                <col class="col-mon-price" />
                <col class="col-mon-qty" />
                <col class="col-mon-transfer" />
              </colgroup>
              <thead>
                <tr>
                  <th>Tên món</th>
                  <th>Đơn giá</th>
                  <th>SL khả dụng</th>
                  <th>SL chuyển</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="mon in danhSachHienThiPhanTrang" :key="mon.id">
                  <td>
                    <div class="mon-info">
                      <img
                        :src="
                          layAnhMon(
                            mon.urlAnh ||
                              mon.hinhAnh ||
                              mon.anh ||
                              mon.hinhAnhSanPham,
                          )
                        "
                        alt="Ảnh món"
                        @error="onImgError"
                      />
                      <span>{{ mon.tenSanPham || mon.tenMonAn }}</span>
                    </div>
                  </td>
                  <td>{{ formatCurrency(mon.donGia) }}đ</td>
                  <td>{{ getSoLuongKhaDung(mon) }} / {{ mon.soLuong }}</td>
                  <td>
                    <div class="qty-control">
                      <button
                        @click="giamSoLuong(mon)"
                        class="qty-btn"
                        :disabled="getSoLuongChuyen(mon.id) <= 0"
                      >
                        <i class="ri-subtract-line"></i>
                      </button>
                      <input
                        type="number"
                        :value="getSoLuongChuyen(mon.id)"
                        @input="(e) => updateSoLuongChuyen(mon, e)"
                        min="0"
                        :max="getSoLuongKhaDung(mon)"
                      />
                      <button
                        @click="tangSoLuong(mon)"
                        class="qty-btn"
                        :disabled="
                          getSoLuongKhaDung(mon) <= getSoLuongChuyen(mon.id)
                        "
                      >
                        <i class="ri-add-line"></i>
                      </button>
                    </div>
                  </td>
                </tr>
                <tr v-if="dsMonChuaPhanBo.length === 0">
                  <td colspan="4" class="empty-table-row">
                    Tất cả món đã được chọn
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-if="dsMonChuaPhanBo.length > 0" class="tach-mon-toolbar">
            <div class="page-size-group">
              <span>Hiển thị</span>
              <select class="form-select" v-model="pageSize">
                <option :value="5">5 món</option>
                <option :value="10">10 món</option>
                <option :value="20">20 món</option>
              </select>
            </div>

            <div class="pagination">
              <button
                :disabled="currentPage === 1"
                @click="currentPage--"
                class="pg-btn pg-arrow"
              >
                <i class="ri-arrow-left-s-line"></i>
              </button>

              <template v-for="p in pageList">
                <button
                  v-if="p === '…'"
                  :key="'ellipsis-' + p"
                  class="pg-btn pg-ellipsis"
                  disabled
                >
                  ···
                </button>
                <button
                  v-else
                  :key="'page-' + p"
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

            <div class="page-summary">
              Hiển thị {{ danhSachHienThiPhanTrang.length }} /
              {{ totalElements }} món
            </div>
          </div>

          <div class="tach-mon-footer">
            <button class="btn-cancel" @click="stepTachBan = 1">
              <i class="ri-arrow-left-line"></i> Quay lại
            </button>
            <button class="btn-primary" @click="xacNhanTachMon">
              <div class="btn-light-sweep"></div>
              Xác nhận tách bàn
            </button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.error-text {
  color: #dc2626;
  font-size: 13px;
  margin-top: 6px;
  display: block;
  font-weight: 500;
}

.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  min-height: 100%;
  background: #f3f4f6;
  display: flex;
  flex-direction: column;
  z-index: 99999;
  font-family: var(--font-family, "Inter", sans-serif);
  box-sizing: border-box;
}

.modal-overlay * {
  box-sizing: border-box;
}

.modal-container {
  background: #f8fafc;
  width: 96vw;
  max-width: 1600px;
  height: 92vh;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
  animation: modalScaleIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.modal-title {
  padding: 18px 22px;
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.96),
    rgba(255, 255, 255, 0.88)
  );
  border-radius: 7px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.06);
  margin-bottom: 16px;
  color: var(--color-text);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
  backdrop-filter: blur(10px);
}

.modal-title h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: var(--color-text);
}

.btn-close-modal {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #64748b;
  line-height: 1;
  padding: 4px 8px;
  border-radius: 6px;
  transition: all 0.2s;
}

.btn-close-modal:hover {
  background: #fee2e2;
  color: #b91c1c;
}

.info-section {
  padding: 16px 24px;
  background: #ffffff;
  border-radius: 10px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03);
  border: 1px solid #eee;
  flex-shrink: 0;
  margin-bottom: 16px;
}

.info-top {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.info-block {
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 7px;
  padding: 12px;
}

.current-table {
  background: #fff5f5;
  border: 1px solid #fee2e2;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #64748b;
  margin-bottom: 6px;
  font-weight: 700;
}

.info-value {
  font-weight: 800;
  font-size: 14px;
  color: #0f172a;
  line-height: 1.4;
}

.so-khach-them-box {
  margin-top: 14px;
  display: flex;
  align-items: end;
  gap: 14px;
  padding: 12px;
  border-radius: 7px;
  background: #fff7ed;
  border: 1px solid #fed7aa;
}

.input-group {
  width: 220px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.input-group label {
  font-size: 13px;
  font-weight: 800;
  color: #9a3412;
  display: flex;
  align-items: center;
  gap: 6px;
}

.input-so-khach-them {
  width: 100%;
  height: 40px;
  border: 1px solid #dbe1ea;
  border-radius: 7px;
  padding: 0 12px;
  font-size: 14px;
  font-weight: 700;
  outline: none;
  background: #fff;
}

.input-so-khach-them:focus {
  border-color: #f97316;
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.15);
}

.chon-note {
  font-size: 13px;
  color: #9a3412;
  font-weight: 600;
  padding-bottom: 9px;
}

.info-note {
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: 7px;
  background: #fff7ed;
  color: #9a3412;
  font-size: 13px;
  font-weight: 600;
  border: 1px solid #fed7aa;
}

.modal-main-layout {
  display: flex;
  align-items: stretch;
  background: transparent;
  flex: 1;
  gap: 16px;
  min-height: 0;
}

.selection-section {
  flex: 2.5;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.selected-sidebar {
  flex: 1;
  background: #ffffff;
  border-radius: 10px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03);
  border: 1px solid #eee;
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 200px);
}

.sidebar-header {
  padding: 16px 20px;
  font-weight: 800;
  font-size: 14px;
  color: var(--color-primary);
  border-bottom: 1px solid #f0f0f0;
  background: #fff;
  border-radius: 10px 10px 0 0;
}

.side-summary {
  margin: 12px;
  padding: 10px 12px;
  border-radius: 7px;
  background: #fff7ed;
  color: #9a3412;
  font-size: 13px;
  font-weight: 600;
  border: 1px solid #fed7aa;
  display: grid;
  gap: 6px;
}

.selected-list {
  flex: 1;
  min-height: 300px;
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.selected-tag-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  background: #fff5f5;
  border: 1px solid rgba(230, 90, 90, 0.12);
  border-radius: 7px;
}

.tag-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.tag-name {
  font-weight: 800;
  font-size: 14px;
  color: var(--color-primary);
}

.tag-area {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
}

.remove-tag {
  background: none;
  border: none;
  color: #ccc;
  font-size: 18px;
  cursor: pointer;
  padding: 0 5px;
  transition: all 0.2s ease;
}

.remove-tag:hover {
  transform: scale(1.2);
  color: #b91c1c;
}

.khu-tang {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
  background: #ffffff;
  padding: 14px 18px;
  border-radius: 10px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03);
  border: 1px solid #eee;
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
  overflow-y: auto;
  overflow-x: hidden;
}

.empty-state {
  text-align: center;
  color: #94a3b8;
  padding: 40px 0;
  font-size: 14px;
  font-weight: 500;
}

.grid-ban {
  display: grid;
  gap: 18px;
  justify-content: start;
  grid-auto-flow: dense;
  grid-template-columns: repeat(5, minmax(120px, 1fr)) minmax(140px, 1.2fr);
  grid-template-rows: repeat(3, 120px);
  grid-auto-rows: 120px;
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
}

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
.disabled-table {
  cursor: not-allowed !important;
  pointer-events: all;
}
.disabled-table::after {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: 7px;
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: grayscale(80%);
  z-index: 4;
  pointer-events: none;
}

.sidebar-footer {
  display: flex;
  justify-content: flex-end;
  gap: 14px;
  padding: 16px 20px;
  border-top: 1px solid #e5e7eb;
  background: white;
  margin-top: auto;
  border-radius: 0 0 10px 10px;
  position: sticky;
  bottom: 0;
  z-index: 10;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.05);
}

.tach-mon-view {
  background: white !important;
  border-radius: 10px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03);
}

.tach-mon-footer {
  padding: 16px 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  position: sticky;
  bottom: 0;
  background: white;
  z-index: 10;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.05);
}

.btn-primary,
.btn-cancel {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 12px 28px;
  border-radius: 7px;
  font-weight: 600;
  font-size: 14px;
  transition: 0.2s;
  text-transform: none;
  cursor: pointer;
  border: none;
}

.btn-primary {
  position: relative;
  overflow: hidden;
  background: #d73737;
  color: #fff;
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

/* ===== BẮT ĐẦU CSS HOVER CHO CÁC Ô THÔNG TIN TRÊN CÙNG ===== */
.info-block {
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 7px;
  padding: 12px;
  transition: all 0.25s ease; /* Thêm transition để hiệu ứng mượt */
  cursor: default;
}

/* Hiệu ứng khi hover vào ô thông tin thường */
.info-block:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.06);
  border-color: #cbd5e1;
}

/* Thêm hiệu ứng nhún nhẹ + đổi màu cho các icon bên trong khi hover */
.info-block:hover .info-label i {
  color: var(--color-primary);
  transform: scale(1.1);
  transition: all 0.2s ease;
}

.current-table {
  background: #fff5f5;
  border: 1px solid #fee2e2;
}

/* Hiệu ứng riêng cho ô "Bàn đang dùng" (Màu đỏ nhạt) */
.current-table:hover {
  border-color: rgba(230, 90, 90, 0.35);
  box-shadow: 0 8px 20px rgba(230, 90, 90, 0.12);
}

.guest-split-input {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed rgba(230, 90, 90, 0.2);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.guest-split-input label {
  font-size: 13px;
  font-weight: 700;
  color: #475569;
}

.guest-split-control {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #fff;
  border: 1px solid #cbd5e1;
  border-radius: 7px;
  padding: 2px 8px;
}

.guest-split-control input {
  width: 40px;
  text-align: center;
  border: none;
  outline: none;
  font-size: 14px;
  font-weight: 800;
  color: #b91c1c;
  background: transparent;
}

.guest-split-control span {
  font-size: 12px;
  color: #64748b;
  font-weight: 600;
}

.guest-split-control:focus-within {
  border-color: #b91c1c;
  box-shadow: 0 0 0 2px rgba(185, 28, 28, 0.1);
}

.alert-doi-ban {
  margin-top: 14px;
  padding: 12px;
  background: #fff0f0;
  border: 1px dashed #fecaca;
  border-radius: 7px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  animation: fadeIn 0.3s ease;
}

.alert-content {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  color: #b91c1c;
  font-size: 13px;
  line-height: 1.4;
}

.icon-warning {
  font-size: 16px;
  margin-top: -2px;
}

.large-vertical {
  grid-row: span 3;
  height: 100%;
  z-index: 5;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1400px) {
  .grid-ban {
    grid-template-columns: repeat(4, minmax(120px, 1fr)) minmax(140px, 1.2fr);
    grid-template-rows: repeat(4, 120px);
    grid-auto-rows: 120px;
  }
  .large-vertical {
    grid-row: span 4;
  }
}

@media (max-width: 1200px) {
  .grid-ban {
    grid-template-columns: repeat(4, minmax(120px, 1fr));
    grid-template-rows: auto;
  }
  .large-vertical {
    grid-column: auto;
    grid-row: auto;
    min-height: 140px;
  }
}

@media (max-width: 900px) {
  .info-top {
    grid-template-columns: repeat(2, 1fr);
  }

  .modal-main-layout {
    flex-direction: column;
  }

  .selected-sidebar {
    width: 100%;
    min-width: 100%;
  }

  .grid-ban {
    grid-template-columns: repeat(3, minmax(120px, 1fr));
    grid-template-rows: auto;
  }

  .large-vertical {
    grid-column: auto;
    grid-row: auto;
  }

  .tach-mon-main-header {
    flex-direction: column;
    align-items: stretch;
  }

  .tach-mon-action {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .grid-ban {
    grid-template-columns: repeat(2, minmax(120px, 1fr));
    gap: 14px;
  }
}

@media (max-width: 480px) {
  .grid-ban {
    grid-template-columns: 1fr;
  }
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
/* ==== STEP 2 SPLIT LAYOUT ==== */
.tach-mon-view {
  display: flex;
  align-items: stretch;
  gap: 0;
  padding: 0;
  flex: 1 0 auto;
  min-height: fit-content;
  background: #fff;
}
.tach-mon-sidebar {
  flex: 0 0 300px;
  max-width: 300px;
  background: #f8fafc;
  border-right: 1px solid #e5e7eb;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 0;
  align-self: flex-start;
  max-height: calc(100vh - 180px);
  overflow: auto;
}
.tach-mon-sidebar-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.tach-mon-sidebar-title {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
}
.tach-mon-sidebar-subtitle {
  font-size: 13px;
  color: #64748b;
}
.tach-mon-table-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.table-option {
  width: 100%;
  text-align: left;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #334155;
  padding: 12px 14px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.table-option:hover {
  border-color: #fca5a5;
  transform: translateY(-1px);
  box-shadow: 0 8px 16px rgba(15, 23, 42, 0.06);
}
.table-option.active {
  border-color: #d73737;
  background: #fff5f5;
  color: #b91c1c;
  box-shadow: 0 8px 16px rgba(215, 55, 55, 0.12);
}
.table-option-title {
  font-size: 14px;
  font-weight: 700;
}
.table-option-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.table-option-badge {
  flex: 0 0 auto;
  border-radius: 999px;
  background: #fee2e2;
  color: #d73737;
  font-size: 11px;
  font-weight: 800;
  padding: 3px 8px;
}
.table-option-meta {
  font-size: 12px;
  color: inherit;
  opacity: 0.8;
}
.selected-dish-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed rgba(215, 55, 55, 0.28);
}
.selected-dish-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 12px;
  color: #334155;
}
.selected-dish-name {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.selected-dish-qty {
  flex: 0 0 auto;
  color: #b91c1c;
  font-weight: 800;
}
.selected-dish-actions {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.selected-dish-remove {
  width: 22px;
  height: 22px;
  border: 1px solid #fecaca;
  border-radius: 999px;
  background: #fff;
  color: #d73737;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}
.selected-dish-remove:hover {
  background: #fee2e2;
  border-color: #fca5a5;
}
.selected-dish-empty {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed rgba(100, 116, 139, 0.25);
  font-size: 12px;
  color: #94a3b8;
}
.tach-mon-action {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 38px;
  padding: 0 14px;
  border: 1px solid #fecaca;
  border-radius: 7px;
  background: #fff5f5;
  color: #d73737;
  font-size: 13px;
  font-weight: 800;
  white-space: nowrap;
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease, color 0.18s ease,
    transform 0.18s ease, box-shadow 0.18s ease;
}
.tach-mon-action:hover:not(:disabled) {
  background: #d73737;
  border-color: #d73737;
  color: #fff;
  transform: translateY(-1px);
  box-shadow: 0 8px 16px rgba(215, 55, 55, 0.18);
}
.tach-mon-action:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: none;
}
.tach-mon-action:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}
.tach-mon-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  padding: 24px;
  background: #fff;
}
.tach-mon-main-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}
.tach-mon-main-title {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
}
.tach-mon-main-subtitle {
  font-size: 13px;
  color: #64748b;
}
.table-container {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: auto;
  min-height: 400px;
}
.mon-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}
.col-mon-name {
  width: 46%;
}
.col-mon-price {
  width: 18%;
}
.col-mon-qty {
  width: 18%;
}
.col-mon-transfer {
  width: 18%;
}
.mon-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #444;
  text-align: left;
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  z-index: 1;
}
.mon-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
  vertical-align: middle;
}
.mon-table th:nth-child(n + 2),
.mon-table td:nth-child(n + 2) {
  text-align: center;
}
.mon-info {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}
.mon-info img {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  object-fit: cover;
  flex: 0 0 auto;
}
.mon-info span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.qty-control {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}
.qty-control .qty-btn {
  width: 34px;
  height: 32px;
  border: 0;
  background: #f8fafc;
  cursor: pointer;
  color: #334155;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: background 0.18s ease, color 0.18s ease, transform 0.18s ease;
}
.qty-control .qty-btn:hover:not(:disabled) {
  background: #fee2e2;
  color: #d73737;
}
.qty-control .qty-btn:active:not(:disabled) {
  transform: scale(0.94);
}
.qty-control .qty-btn:disabled {
  color: #cbd5e1;
  background: #f8fafc;
  cursor: not-allowed;
}
.qty-control input {
  width: 44px;
  height: 32px;
  text-align: center;
  border: 0;
  border-left: 1px solid #e2e8f0;
  border-right: 1px solid #e2e8f0;
  outline: none;
  color: #0f172a;
  font-weight: 700;
  background: #fff;
}
.qty-control input:focus {
  background: #fff7f7;
}
.qty-control input::-webkit-outer-spin-button,
.qty-control input::-webkit-inner-spin-button {
  margin: 0;
  appearance: none;
}
.empty-table-row {
  text-align: center;
  color: #888;
  padding: 24px;
}
.tach-mon-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  gap: 12px;
  flex-wrap: wrap;
}
.page-size-group {
  display: flex;
  align-items: center;
  gap: 8px;
}
.page-size-group span {
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  white-space: nowrap;
}
.page-size-group .form-select {
  height: 36px;
  border-radius: 7px;
  border: 1px solid #e2e8f0;
  padding: 0 10px;
  font-size: 14px;
  outline: none;
  background: #fff;
}
.page-summary {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}
.btn-secondary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 16px;
  border: 1px solid #d32f2f;
  background: white;
  color: #d32f2f;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s ease;
}
.btn-secondary:hover:not(:disabled) {
  background: #fff5f5;
  transform: translateY(-1px);
}
.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* ==== STEP 2 SPLIT LAYOUT ==== */
.tach-mon-view {
  display: flex;
  align-items: stretch;
  gap: 0;
  padding: 0;
  flex: 1 0 auto;
  min-height: fit-content;
  background: #fff;
}
.tach-mon-sidebar {
  flex: 0 0 300px;
  max-width: 300px;
  background: #f8fafc;
  border-right: 1px solid #e5e7eb;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 0;
  align-self: flex-start;
  max-height: calc(100vh - 180px);
  overflow: auto;
}
.tach-mon-sidebar-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.tach-mon-sidebar-title {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
}
.tach-mon-sidebar-subtitle {
  font-size: 13px;
  color: #64748b;
}
.tach-mon-table-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.table-option {
  width: 100%;
  text-align: left;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #334155;
  padding: 12px 14px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.table-option:hover {
  border-color: #fca5a5;
  transform: translateY(-1px);
  box-shadow: 0 8px 16px rgba(15, 23, 42, 0.06);
}
.table-option.active {
  border-color: #d73737;
  background: #fff5f5;
  color: #b91c1c;
  box-shadow: 0 8px 16px rgba(215, 55, 55, 0.12);
}
.table-option-title {
  font-size: 14px;
  font-weight: 700;
}
.table-option-meta {
  font-size: 12px;
  color: inherit;
  opacity: 0.8;
}
.tach-mon-action {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 38px;
  padding: 0 14px;
  border: 1px solid #fecaca;
  border-radius: 7px;
  background: #fff5f5;
  color: #d73737;
  font-size: 13px;
  font-weight: 800;
  white-space: nowrap;
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease, color 0.18s ease,
    transform 0.18s ease, box-shadow 0.18s ease;
}
.tach-mon-action:hover:not(:disabled) {
  background: #d73737;
  border-color: #d73737;
  color: #fff;
  transform: translateY(-1px);
  box-shadow: 0 8px 16px rgba(215, 55, 55, 0.18);
}
.tach-mon-action:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: none;
}
.tach-mon-action:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}
.tach-mon-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  padding: 24px;
  background: #fff;
}
.tach-mon-main-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}
.tach-mon-main-title {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
}
.tach-mon-main-subtitle {
  font-size: 13px;
  color: #64748b;
}
.table-container {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: auto;
  min-height: 400px;
}
.mon-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}
.mon-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #444;
  text-align: left;
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  z-index: 1;
}
.mon-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
  vertical-align: middle;
}
.mon-table th:nth-child(n + 2),
.mon-table td:nth-child(n + 2) {
  text-align: center;
}
.mon-info {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}
.mon-info img {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  object-fit: cover;
  flex: 0 0 auto;
}
.mon-info span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.qty-control {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}
.qty-control .qty-btn {
  width: 34px;
  height: 32px;
  border: 0;
  background: #f8fafc;
  cursor: pointer;
  color: #334155;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: background 0.18s ease, color 0.18s ease, transform 0.18s ease;
}
.qty-control .qty-btn:hover:not(:disabled) {
  background: #fee2e2;
  color: #d73737;
}
.qty-control .qty-btn:active:not(:disabled) {
  transform: scale(0.94);
}
.qty-control .qty-btn:disabled {
  color: #cbd5e1;
  background: #f8fafc;
  cursor: not-allowed;
}
.qty-control input {
  width: 44px;
  height: 32px;
  text-align: center;
  border: 0;
  border-left: 1px solid #e2e8f0;
  border-right: 1px solid #e2e8f0;
  outline: none;
  color: #0f172a;
  font-weight: 700;
  background: #fff;
}
.qty-control input:focus {
  background: #fff7f7;
}
.qty-control input::-webkit-outer-spin-button,
.qty-control input::-webkit-inner-spin-button {
  margin: 0;
  appearance: none;
}
.empty-table-row {
  text-align: center;
  color: #888;
  padding: 24px;
}
.tach-mon-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  gap: 12px;
  flex-wrap: wrap;
}
.page-size-group {
  display: flex;
  align-items: center;
  gap: 8px;
}
.page-size-group span {
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  white-space: nowrap;
}
.page-size-group .form-select {
  height: 36px;
  border-radius: 7px;
  border: 1px solid #e2e8f0;
  padding: 0 10px;
  font-size: 14px;
  outline: none;
  background: #fff;
}
.page-summary {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}
.btn-secondary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 16px;
  border: 1px solid #d32f2f;
  background: white;
  color: #d32f2f;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s ease;
}
.btn-secondary:hover:not(:disabled) {
  background: #fff5f5;
  transform: translateY(-1px);
}
.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* ==== CATEGORY TABS ==== */
.category-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  overflow-x: auto;
  padding-bottom: 4px;
}
.category-tabs::-webkit-scrollbar {
  height: 4px;
}
.category-tabs::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 999px;
}
.tab-btn {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  color: #64748b;
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  max-width: 240px; /* prevent overly long pills */
  overflow: hidden;
  text-overflow: ellipsis;
}
.tab-btn:hover {
  background: #f1f5f9;
  color: #334155;
}
.tab-btn.active {
  background: #fee2e2;
  color: #d73737;
  border-color: #fca5a5;
}

/* ==== PAGINATION ==== */
.pagination {
  display: flex;
  align-items: center;
  gap: 6px;
}
.pg-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e2e8f0;
  color: #64748b;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.pg-btn:hover:not(:disabled):not(.active) {
  background: #f8fafc;
  border-color: #cbd5e1;
  color: #1e293b;
}
.pg-btn.active {
  background: #d73737;
  border-color: #d73737;
  color: #fff;
}
.pg-btn:disabled {
  background: #f8fafc;
  color: #cbd5e1;
  cursor: not-allowed;
}
.pg-ellipsis {
  background: transparent !important;
  border-color: transparent !important;
  color: #64748b !important;
}

/* ===== RESPONSIVE ===== */
@media (max-width: 1400px) {
  .grid-ban {
    grid-template-columns: repeat(4, minmax(120px, 1fr)) minmax(140px, 1.2fr);
    grid-template-rows: repeat(4, 120px);
    grid-auto-rows: 120px;
  }
  .large-vertical {
    grid-row: span 4;
  }
}
@media (max-width: 1200px) {
  .modal-main-layout {
    flex-direction: column;
  }
  .selected-sidebar {
    width: 100%;
    min-width: 100%;
  }
  .grid-ban {
    grid-template-columns: repeat(4, minmax(120px, 1fr));
    grid-template-rows: auto;
  }
  .large-vertical {
    grid-column: auto;
    grid-row: auto;
    min-height: 140px;
  }
}
@media (max-width: 900px) {
  .info-top {
    grid-template-columns: repeat(2, 1fr);
  }
  .grid-ban {
    grid-template-columns: repeat(3, minmax(95px, 1fr));
    grid-template-rows: auto;
  }
  .large-vertical {
    grid-column: auto;
    grid-row: auto;
  }
  .tach-mon-main-header {
    flex-direction: column;
    align-items: stretch;
  }
  .tach-mon-action {
    width: 100%;
  }
}
@media (max-width: 480px) {
  .grid-ban {
    grid-template-columns: 1fr;
  }
}

.qty-control input {
  width: 44px;
  height: 32px;
  text-align: center;
  border: 0;
  border-left: 1px solid #e2e8f0;
  border-right: 1px solid #e2e8f0;
  outline: none;
  color: #0f172a;
  font-weight: 700;
  background: #fff;
}
.qty-control input:focus {
  background: #fff7f7;
}
.qty-control input::-webkit-outer-spin-button,
.qty-control input::-webkit-inner-spin-button {
  margin: 0;
  appearance: none;
}
.empty-table-row {
  text-align: center;
  color: #888;
  padding: 24px;
}
.tach-mon-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  gap: 12px;
  flex-wrap: wrap;
}
.page-size-group {
  display: flex;
  align-items: center;
  gap: 8px;
}
.page-size-group span {
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  white-space: nowrap;
}
.page-size-group .form-select {
  height: 36px;
  border-radius: 7px;
  border: 1px solid #e2e8f0;
  padding: 0 10px;
  font-size: 14px;
  outline: none;
  background: #fff;
}
.page-summary {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}
.btn-secondary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 16px;
  border: 1px solid #d32f2f;
  background: white;
  color: #d32f2f;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s ease;
}
.btn-secondary:hover:not(:disabled) {
  background: #fff5f5;
  transform: translateY(-1px);
}
.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* ==== CATEGORY TABS ==== */
.category-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  overflow-x: auto;
  padding-bottom: 4px;
}
.category-tabs::-webkit-scrollbar {
  height: 4px;
}
.category-tabs::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 999px;
}
.tab-btn {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  color: #64748b;
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  max-width: 240px; /* prevent overly long pills */
  overflow: hidden;
  text-overflow: ellipsis;
}
.tab-btn:hover {
  background: #f1f5f9;
  color: #334155;
}
.tab-btn.active {
  background: #fee2e2;
  color: #d73737;
  border-color: #fca5a5;
}

/* ==== PAGINATION ==== */
.pagination {
  display: flex;
  align-items: center;
  gap: 6px;
}
.pg-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e2e8f0;
  color: #64748b;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.pg-btn:hover:not(:disabled):not(.active) {
  background: #f8fafc;
  border-color: #cbd5e1;
  color: #1e293b;
}
.pg-btn.active {
  background: #d73737;
  border-color: #d73737;
  color: #fff;
}
.pg-btn:disabled {
  background: #f8fafc;
  color: #cbd5e1;
  cursor: not-allowed;
}
.pg-ellipsis {
  background: transparent !important;
  border-color: transparent !important;
  color: #64748b !important;
}

/* ===== RESPONSIVE ===== */
@media (max-width: 1400px) {
  .grid-ban {
    grid-template-columns: repeat(4, minmax(120px, 1fr)) minmax(140px, 1.2fr);
    grid-template-rows: repeat(4, 120px);
    grid-auto-rows: 120px;
  }
  .large-vertical {
    grid-row: span 4;
  }
}
@media (max-width: 1200px) {
  .modal-main-layout {
    flex-direction: column;
  }
  .selected-sidebar {
    width: 100%;
    min-width: 100%;
  }
  .grid-ban {
    grid-template-columns: repeat(4, minmax(120px, 1fr));
    grid-template-rows: auto;
  }
  .large-vertical {
    grid-column: auto;
    grid-row: auto;
    min-height: 140px;
  }
}
@media (max-width: 900px) {
  .info-top {
    grid-template-columns: repeat(2, 1fr);
  }
  .grid-ban {
    grid-template-columns: repeat(3, minmax(95px, 1fr));
    grid-template-rows: auto;
  }
  .large-vertical {
    grid-column: auto;
    grid-row: auto;
  }
  .tach-mon-main-header {
    flex-direction: column;
    align-items: stretch;
  }
  .tach-mon-action {
    width: 100%;
  }
}
@media (max-width: 480px) {
  .grid-ban {
    grid-template-columns: 1fr;
  }
}

/* Scrollbar mỏng, đẹp như màn CheckIn */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}
</style>
