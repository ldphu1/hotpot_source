<script setup>
/////////////////////// THU VIEN //////////////////
import {
  ref,
  onMounted,
  computed,
  defineProps,
  defineEmits,
  nextTick,
  onBeforeUnmount,
  watch,
} from "vue";
import { useUtils } from "@/utils/helper";
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";
import { connectWebSocket, disconnectWebSocket } from "@/utils/websocket";
/////////////////// API ?????????????????????
import { getAllBanAn, tuChoiXepBan } from "@/api/admin/datban/datBanApi";
import { getAllkhuvuc } from "@/api/admin/khuvuc/khuVucApi";
import { getAllBanChoDat } from "@/api/admin/datban/datBanApi";
import { xepBanChoKhach } from "@/api/admin/datban/datBanApi";
import { getAllThamSoDong } from "@/api/admin/thamsodong/thamSoDongApi";
import { filterBanAn } from "@/api/admin/quanlyban/quanlybanApi";
//////////////////// HAM ??????????????????????
const { notify, nprogress } = useUtils();
const danhSachBan = ref([]);
const danhSachTang = ref([]);
const danhSachSoCho = ref([]);
const banDangChon = ref([]);
const isLoading = ref(false);
const daBiXepONoiKhac = ref(false);
const moBanDuPhong = ref(false); // Trạng thái mở / khóa bàn dự phòng

// DIALOG CẢNH BÁO TRÙNG LỊCH
const showWarnTrungLich = ref(false);
const banDangCanhBao = ref(null);
const selectTenKhuVuc = ref(null);
const selectSoCho = ref(null);
const selectTrangThai = ref(null);

//////////////////////// THAM SỐ HỆ THỐNG //////////////////
const thamSoDong = ref([]);

const loadThamSo = async () => {
  try {
    const res = await getAllThamSoDong();
    thamSoDong.value = res.data;
  } catch (error) {
    console.log(error);
  }
};

/////////////// TIM KIEM ///////////////
const searchBan = async () => {
  try {
    nprogress.start();

    const params = {
      tenKhuVuc: selectTenKhuVuc.value,
      soChoToiDa: selectSoCho.value,
      trangThai: selectTrangThai.value,
    };

    Object.keys(params).forEach((key) => {
      if (
        params[key] === null ||
        params[key] === "" ||
        params[key] === undefined
      ) {
        delete params[key];
      }
    });

    const res = await filterBanAn(params, props.bookingData?.ngayGio);
    danhSachBan.value = res.data.data || res.data.content || res.data || [];
  } catch (e) {
    console.log(e);
    notify.error("Lỗi tìm kiếm bàn");
  } finally {
    nprogress.done();
  }
};

///////////////////////////////////////

const props = defineProps(["bookingData"]);
const emit = defineEmits(["close"]);

const getTenTrangThai = (trangThai) => {
  const map = {
    1: "Trống",
    2: "Đã đặt",
    3: "Có khách",
    4: "Đang dọn",
  };
  return map[trangThai] || "Không xác định";
};

const closePopup = () => emit("close");

const getClassTrangThai = (table) => {
  if (!table.coTheDat) return "trung-lich";

  switch (table.trangThai) {
    case 1:
      return "trong";

    case 2:
      return "da_dat";

    case 3:
      return "co_khach";

    case 4:
      return "dang_don";

    default:
      return "trong";
  }
};

/////////////////// VALIDATE CHỌN BÀN /////////////

const tongChoNgoi = computed(() => {
  return banDangChon.value.reduce((sum, b) => sum + b.sucChuaToiDa, 0);
});

const duCho = computed(() => {
  return tongChoNgoi.value >= (props.bookingData?.soNguoi || 0);
});

const trangThaiSucChua = computed(() => {
  const soNguoi = props.bookingData?.soNguoi || 0;
  const tong = tongChoNgoi.value;
  const max = soNguoi + soGheDuToiDa.value;

  if (tong === 0) return "empty";
  if (tong < soNguoi) return "thieu";

  if (tong > max) {
    const conTotHon = conLuaChonTotHon();
    return conTotHon || (!conTotHon && soNguoi === 1) ? "vuot" : "ok";
  }

  return "ok";
});

const soGheDuToiDa = computed(() =>
  Number(
    thamSoDong.value.find((i) => i.maThamSo === "SO_GHE_DU_TOI_DA")?.giaTri ||
      0,
  ),
);

// Kiểm tra còn bàn đơn lẻ nào (chưa chọn, còn khả dụng) dư <= ngưỡng không
const coBanDonPhuHop = computed(() => {
  const soNguoi = props.bookingData?.soNguoi || 0;
  return danhSachBan.value.some((t) => {
    const daChon = banDangChon.value.some((b) => b.id === t.id);
    if (daChon || t.coTheDat === false) return false;
    return (
      t.sucChuaToiDa >= soNguoi &&
      t.sucChuaToiDa - soNguoi <= soGheDuToiDa.value
    );
  });
});

const coToHopBanPhuHop = () => {
  const soNguoi = props.bookingData?.soNguoi || 0;
  const banKhaDung = danhSachBan.value
    .filter(
      (t) =>
        t.coTheDat !== false && !banDangChon.value.some((b) => b.id === t.id),
    )
    .sort((a, b) => a.sucChuaToiDa - b.sucChuaToiDa);

  const timToHop = (index, soLuongDaChon, tongHienTai) => {
    if (tongHienTai >= soNguoi) {
      return tongHienTai - soNguoi <= soGheDuToiDa.value;
    }
    if (index >= banKhaDung.length || soLuongDaChon >= 3) return false;

    if (
      timToHop(
        index + 1,
        soLuongDaChon + 1,
        tongHienTai + banKhaDung[index].sucChuaToiDa,
      )
    )
      return true;

    return timToHop(index + 1, soLuongDaChon, tongHienTai);
  };

  return timToHop(0, 0, 0);
};

const conLuaChonTotHon = () => coBanDonPhuHop.value || coToHopBanPhuHop();

// Kiểm tra hình thức đặt là Online hay Offline
//   1 (Online)     → "Trực tuyến"
//   2 (Tại quầy)   → "Tại quầy"

const isOnlineBooking = computed(() => {
  const ht = props.bookingData?.hinhThucDat;
  if (typeof ht === "string") {
    return ht === "Trực tuyến" || ht.toLowerCase().includes("online");
  }
  return ht === 1;
});

// Kiểm tra xem còn bàn nào cùng loại (ONLINE/OFFLINE) có thể chọn được không
const coBanKhaDungTheoLoai = (loai) => {
  return danhSachBan.value.some((table) => {
    if (table.loaiBan !== loai) return false;
    if (table.coTheDat === false) return false;

    // Bàn đó phải chưa được chọn
    const daChon = banDangChon.value.some((b) => b.id === table.id);
    if (daChon) return false;

    // Bàn dự phòng phải được mở thì mới tính là khả dụng
    if (table.loaiBan === "DU_PHONG" && !moBanDuPhong.value) return false;

    // Kiểm tra sức chứa
    const soNguoi = props.bookingData?.soNguoi || 0;
    const max = soNguoi + soGheDuToiDa.value;
    const tongSau = tongChoNgoi.value + table.sucChuaToiDa;
    if (tongSau > max) {
      const conTotHon = conLuaChonTotHon();
      if (conTotHon || (!conTotHon && soNguoi === 1)) {
        return false;
      }
    }
    return true;
  });
};

// Kiểm tra xem bàn có bị chặn do ưu tiên hình thức đặt hay không
const isBanBiChanTheoHinhThuc = (table) => {
  const daChon = banDangChon.value.some((b) => b.id === table.id);
  if (daChon) return false; // Cho phép bỏ chọn bàn đã chọn

  if (isOnlineBooking.value) {
    // Đặt Online: Chặn chọn bàn OFFLINE nếu vẫn còn bàn ONLINE khả dụng
    if (table.loaiBan === "OFFLINE" && coBanKhaDungTheoLoai("ONLINE")) {
      return true;
    }
  } else {
    // Đặt Offline (Tại quầy): Chặn chọn bàn ONLINE nếu vẫn còn bàn OFFLINE khả dụng
    if (table.loaiBan === "ONLINE" && coBanKhaDungTheoLoai("OFFLINE")) {
      return true;
    }
  }
  return false;
};

const isBanHopLe = (table) => {
  const daChon = banDangChon.value.some((b) => b.id === table.id);
  if (daChon) return true;

  if (table.loaiBan === "DU_PHONG" && !moBanDuPhong.value) {
    return false;
  }

  // Chặn theo ưu tiên loại hình đặt bàn
  if (isBanBiChanTheoHinhThuc(table)) {
    return false;
  }

  if (table.coTheDat === false) return false;

  const soNguoi = props.bookingData?.soNguoi || 0;
  const max = soNguoi + soGheDuToiDa.value;

  const tongSau = tongChoNgoi.value + table.sucChuaToiDa;

  if (tongSau > max) {
    const conTotHon = conLuaChonTotHon();
    if (conTotHon || (!conTotHon && soNguoi === 1)) {
      return false;
    }
  }

  return true;
};

const getLabelBan = (table) => {
  if (table.coTheDat === false) {
    return "TRÙNG LỊCH";
  }

  if (table.loaiBan === "DU_PHONG" && !moBanDuPhong.value) {
    return "ĐANG KHÓA";
  }

  // Nhãn hiển thị khi bị chặn do chế độ ưu tiên
  if (isBanBiChanTheoHinhThuc(table)) {
    return isOnlineBooking.value ? "ƯU TIÊN OFFLINE" : "ƯU TIÊN ONLINE";
  }

  const soNguoi = props.bookingData?.soNguoi || 0;
  const tongSau = tongChoNgoi.value + table.sucChuaToiDa;
  const max = soNguoi + soGheDuToiDa.value;

  if (tongSau > max) {
    const conTotHon = conLuaChonTotHon();
    return conTotHon || (!conTotHon && soNguoi === 1) ? "VƯỢT QUÁ SỐ GHẾ" : "";
  }

  return "";
};
const chonBan = (table) => {
  if (daBiXepONoiKhac.value) {
    notify.error("Phiếu đặt bàn đã được xử lý");
    return;
  }

  // Chặn chọn bàn dự phòng khi chưa mở
  if (table.loaiBan === "DU_PHONG" && !moBanDuPhong.value) {
    notify.error(
      "Bàn dự phòng đang bị khóa. Vui lòng nhấn 'Mở bàn dự phòng' để sử dụng!",
    );
    return;
  }

  // Chặn theo ưu tiên loại hình đặt bàn
  if (isBanBiChanTheoHinhThuc(table)) {
    const uuTienMsg = isOnlineBooking.value
      ? "Ưu tiên chọn bàn ONLINE (chỉ dùng bàn OFFLINE khi hết bàn ONLINE khả dụng)!"
      : "Ưu tiên chọn bàn OFFLINE (chỉ dùng bàn ONLINE khi hết bàn OFFLINE khả dụng)!";
    notify.error(uuTienMsg);
    return;
  }

  const index = banDangChon.value.findIndex((b) => b.id === table.id);

  if (index !== -1) {
    banDangChon.value.splice(index, 1);
    notify.success(`Đã bỏ chọn bàn: ${table.tenBanAn}`);
    return;
  }

  if (!table.coTheDat) {
    notify.error(`Bàn ${table.tenBanAn} đã có lịch đặt`);
    return;
  }

  if (!isBanHopLe(table)) {
    notify.error(`Bàn ${table.tenBanAn} vượt số ghế cho phép`);
    return;
  }

  // Bàn có thể đặt nhưng đang có trạng thái Đã đặt / Có khách => cảnh báo trùng lịch
  if (table.trangThai === 2 || table.trangThai === 3) {
    banDangCanhBao.value = table;
    showWarnTrungLich.value = true;
    return;
  }

  banDangChon.value.push(table);
  notify.success(`Đã chọn bàn: ${table.tenBanAn}`);
};

// Xác nhận chọn bàn dù có thể trùng lịch
const xacNhanChonDuCanhBao = () => {
  if (banDangCanhBao.value) {
    banDangChon.value.push(banDangCanhBao.value);
    notify.success(`Đã chọn bàn: ${banDangCanhBao.value.tenBanAn}`);
  }
  showWarnTrungLich.value = false;
  banDangCanhBao.value = null;
};

// Hủy bỏ cảnh báo
const huyBoCanhBao = () => {
  showWarnTrungLich.value = false;
  banDangCanhBao.value = null;
};

const xacNhanXepBan = async () => {
  if (daBiXepONoiKhac.value) {
    notify.error("Đơn này đã được xếp bàn ở nơi khác!");
    return;
  }
  if (!(await notify.confirm("Xác nhận xếp bàn & gửi mail cho khách?"))) return;

  if (trangThaiSucChua.value === "empty") {
    notify.error("Vui lòng chọn bàn!");
    return;
  }

  if (trangThaiSucChua.value === "thieu") {
    notify.error("Số chỗ chưa đủ cho khách!");
    return;
  }

  if (trangThaiSucChua.value === "vuot") {
    notify.error("Số chỗ vượt quá mức cho phép!");
    return;
  }

  isLoading.value = true;
  nprogress.start();

  await nextTick();
  const startTime = Date.now();

  const payload = {
    idDatBan: props.bookingData?.id,
    hinhThucDat: props.bookingData?.hinhThucDat,
    listIdBan: banDangChon.value.map((b) => b.id),
    monDatTruoc: [],
  };

  let successData = null;

  try {
    if (!props.bookingData?.id) {
      notify.error("Không có id đặt bàn!");
      return;
    }

    await xepBanChoKhach(payload);

    successData = {
      listIdBan: payload.listIdBan,
      listTenBan: banDangChon.value.map((b) => b.tenBanAn),
    };

    notify.success("Xếp bàn thành công");
  } catch (e) {
    notify.error(e.response?.data?.message || "Xếp bàn thất bại!");
  } finally {
    const elapsed = Date.now() - startTime;
    const minimumLoading = 3000;

    if (elapsed < minimumLoading) {
      await new Promise((r) => setTimeout(r, minimumLoading - elapsed));
    }

    isLoading.value = false;
    nprogress.done();

    if (successData) {
      emit("success", successData);
    }

    emit("close");
  }
};
//////////////// TỪ CHỐI XẾP BÀN ////////////////
const showModalTuChoi = ref(false);
const lyDoTuChoi = ref("");

const moModalTuChoi = () => {
  if (daBiXepONoiKhac.value) {
    notify.error("Đơn này đã được xử lý ở nơi khác!");
    return;
  }
  if (!props.bookingData?.id) {
    notify.error("Không có id đặt bàn!");
    return;
  }
  lyDoTuChoi.value = "Không còn bàn trống ở khung giờ đã đặt";
  showModalTuChoi.value = true;
};

const dongModalTuChoi = () => {
  showModalTuChoi.value = false;
  lyDoTuChoi.value = "";
};

const xacNhanTuChoiBan = async () => {
  if (!lyDoTuChoi.value || !lyDoTuChoi.value.trim()) {
    notify.error("Vui lòng nhập lý do từ chối!");
    return;
  }

  if (
    !(await notify.confirm(
      "Xác nhận từ chối xếp bàn & gửi mail thông báo cho khách?",
    ))
  )
    return;

  showModalTuChoi.value = false;
  isLoading.value = true;
  nprogress.start();

  await nextTick();
  const startTime = Date.now();

  try {
    await tuChoiXepBan(props.bookingData.id, lyDoTuChoi.value.trim());
    notify.success("Đã từ chối xếp bàn");
  } catch (e) {
    notify.error(e.response?.data?.message || "Từ chối xếp bàn thất bại!");
  } finally {
    const elapsed = Date.now() - startTime;
    const minimumLoading = 3000;

    if (elapsed < minimumLoading) {
      await new Promise((r) => setTimeout(r, minimumLoading - elapsed));
    }

    isLoading.value = false;
    nprogress.done();
    lyDoTuChoi.value = "";
    emit("close");
  }
};

// Load bàn
const loadBan = async () => {
  try {
    nprogress.start();

    const thoiGianDat = props.bookingData?.ngayGio;

    const resKhuVuc = await getAllkhuvuc();
    const dataKhuVuc = resKhuVuc.data.data || resKhuVuc.data || [];

    danhSachTang.value = dataKhuVuc;

    const defaultTang = danhSachTang.value[0]?.id || null;
    selectTenKhuVuc.value = defaultTang;

    selectSoCho.value = null;
    selectTrangThai.value = null;

    const res = await filterBanAn(
      {
        tenKhuVuc: defaultTang,
        soChoToiDa: null,
        trangThai: null,
      },
      thoiGianDat,
    );

    const dataBan = res.data.data || res.data || [];

    danhSachBan.value = dataBan;

    danhSachSoCho.value = [
      ...new Map(
        dataBan.map((b) => [
          b.sucChuaToiDa,
          {
            label: `${b.sucChuaToiDa} chỗ`,
            value: b.sucChuaToiDa,
          },
        ]),
      ).values(),
    ];

    danhSachBan.value = res.data.data || res.data || [];
  } catch (e) {
    console.error(e);
    notify.error("Lỗi load dữ liệu bàn!");
  } finally {
    nprogress.done();
  }
};
const formatDateTime = (isoString) => {
  if (!isoString) return "---";

  const date = new Date(isoString);

  const pad = (n) => n.toString().padStart(2, "0");

  const day = pad(date.getDate());
  const month = pad(date.getMonth() + 1);
  const year = date.getFullYear();

  const hours = pad(date.getHours());
  const minutes = pad(date.getMinutes());

  return `${day}/${month}/${year} ${hours}:${minutes}`;
};

const handleWsMessage = (data) => {
  if (!data?.maDatBan) return;

  loadBan();

  if (
    props.bookingData?.id &&
    Number(data.id) === Number(props.bookingData.id) &&
    data.listIdBan?.length > 0
  ) {
    daBiXepONoiKhac.value = true;
  }
};

// Cấu hình phân khu bàn theo sơ đồ bàn
const SO_COT = 6;
const TONG_SLOT = ref(36);
const viTriBan = ref({});

const slots = computed(() =>
  Array.from({ length: TONG_SLOT.value }, (_, i) => ({ id: i + 1, index: i })),
);

const isSlotHopLe = (index, loaiBan) => {
  const col = index % SO_COT;
  if (loaiBan === "OFFLINE") return col === 0 || col === 1;
  if (loaiBan === "ONLINE") return col === 2 || col === 3;
  if (loaiBan === "DU_PHONG") return col === 4 || col === 5;
  return false;
};

const laBanLon = (table) => table && table.sucChuaToiDa >= 12;

const khoiTaoSlot = () => {
  const map = {};
  const banCuaTang = [...danhSachBan.value];

  // Phân nhóm bàn theo loại bàn
  const banOffline = banCuaTang.filter((b) => b.loaiBan === "OFFLINE");
  const banOnline = banCuaTang.filter((b) => b.loaiBan === "ONLINE");
  const banDuPhong = banCuaTang.filter((b) => b.loaiBan === "DU_PHONG");

  const xepNhomBan = (danhSachNhom, loaiBan) => {
    // Ưu tiên xếp bàn lớn lên trước
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
            map[b.viTriIndex + SO_COT] = "BLOCKED_" + b.id;
            map[b.viTriIndex + SO_COT * 2] = "BLOCKED_" + b.id;
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

// Theo dõi danh sách bàn thay đổi để khởi tạo slot
watch(
  danhSachBan,
  () => {
    khoiTaoSlot();
  },
  { deep: true, immediate: true },
);

onMounted(() => {
  loadThamSo();
  loadBan();

  connectWebSocket(null, handleWsMessage);
});

onBeforeUnmount(() => {
  disconnectWebSocket(handleWsMessage, null);
});
</script>

<template>
  <Teleport to="body">
    <div class="modal-overlay" @click.self="closePopup">
      <div class="modal-container expanded">
        <h1 class="modal-title">
          <i
            class="fa-solid fa-chair"
            style="color: #8b1e1e; margin-right: 10px"
          ></i>
          Chọn bàn cho khách
        </h1>
        <div class="info-section-luxury">
          <div class="info-grid">
            <div class="info-card">
              <div class="card-icon"><i class="fa-solid fa-user-tie"></i></div>
              <div class="card-body">
                <label>Khách hàng</label>
                <div class="value-text">
                  {{ bookingData?.khachHang || "Khách lẻ" }}
                </div>
              </div>
            </div>

            <div class="info-card">
              <div class="card-icon">
                <i class="fa-solid fa-phone-volume"></i>
              </div>
              <div class="card-body">
                <label>SĐT Liên hệ</label>
                <div class="value-text">
                  {{ bookingData?.sdt || "---" }}
                </div>
              </div>
            </div>

            <div class="info-card">
              <div class="card-icon"><i class="fa-solid fa-users"></i></div>
              <div class="card-body">
                <label>Số lượng khách</label>
                <div class="value-text highlight">
                  {{ bookingData?.soNguoi || 0 }} Người
                </div>
              </div>
            </div>

            <div class="info-card">
              <div class="card-icon">
                <i class="fa-solid fa-calendar-check"></i>
              </div>
              <div class="card-body">
                <label>Thời gian đến</label>
                <div class="value-text">
                  {{ formatDateTime(bookingData?.ngayGio) }}
                </div>
              </div>
            </div>

            <div class="info-card">
              <div class="card-icon">
                <i class="fa-solid fa-circle-nodes"></i>
              </div>
              <div class="card-body">
                <label>Hình thức đặt</label>
                <div
                  class="value-text status-tag"
                  :class="{ online: bookingData?.hinhThucDat !== 0 }"
                >
                  {{ bookingData?.hinhThucDat }}
                </div>
              </div>
            </div>
          </div>
        </div>
        <div
          v-if="daBiXepONoiKhac"
          class="alert-status-card"
          style="margin: 0 25px 15px"
        >
          <div class="alert-icon">
            <i class="ti ti-calendar-x"></i>
          </div>
          <div class="alert-content">
            <span class="alert-title">Phiếu đặt bàn đã được xử lý</span>
            <span class="alert-desc">Vui lòng đóng popup</span>
          </div>
        </div>
        <div class="modal-main-layout">
          <div class="selection-section">
            <div class="filter-bar">
              <!-- Tầng -->
              <div class="filter-item">
                Tầng
                <v-select
                  v-model="selectTenKhuVuc"
                  :options="danhSachTang"
                  label="tenKhuVuc"
                  :reduce="(t) => t.id"
                  :clearable="true"
                  @update:modelValue="searchBan"
                />
              </div>

              <!-- Số chỗ -->
              <div class="filter-item">
                Số chỗ
                <v-select
                  class="modern-select"
                  v-model="selectSoCho"
                  :options="danhSachSoCho"
                  placeholder="Chọn số chỗ"
                  label="label"
                  :reduce="(t) => t.value"
                  :clearable="true"
                  @update:modelValue="searchBan"
                />
              </div>

              <!-- Trạng thái -->
              <div class="filter-item">
                Trạng thái
                <v-select
                  v-model="selectTrangThai"
                  :options="[
                    { label: 'Trống', value: 1 },
                    { label: 'Đã đặt', value: 2 },
                    { label: 'Có khách', value: 3 },
                    { label: 'Đang dọn', value: 4 },
                  ]"
                  label="label"
                  placeholder="Chọn trạng thái"
                  :reduce="(t) => t.value"
                  :clearable="true"
                  @update:modelValue="searchBan"
                />
              </div>

              <!-- Nút mở bàn dự phòng -->
              <div class="filter-du-phong">
                <button
                  class="btn-mo-du-phong"
                  :class="{ active: moBanDuPhong }"
                  @click="moBanDuPhong = !moBanDuPhong"
                >
                  <i
                    :class="
                      moBanDuPhong
                        ? 'fa-solid fa-lock-open'
                        : 'fa-solid fa-lock'
                    "
                  ></i>
                  {{ moBanDuPhong ? "Khóa bàn dự phòng" : "Mở bàn dự phòng" }}
                </button>
              </div>
            </div>

            <div class="map-container">
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
                >
                  <div
                    v-if="
                      viTriBan[slot.index] &&
                      typeof viTriBan[slot.index] === 'object'
                    "
                    class="table-card"
                    style="height: 100%; width: 100%"
                    :class="[
                      viTriBan[slot.index].loaiBan === 'DU_PHONG' &&
                      !moBanDuPhong
                        ? 'du-phong'
                        : getClassTrangThai(viTriBan[slot.index]),
                      { 'large-vertical': laBanLon(viTriBan[slot.index]) },
                      {
                        selected: banDangChon.some(
                          (b) => b.id === viTriBan[slot.index].id,
                        ),
                      },
                      {
                        'not-available':
                          viTriBan[slot.index].coTheDat === false ||
                          !isBanHopLe(viTriBan[slot.index]) ||
                          (viTriBan[slot.index].loaiBan === 'DU_PHONG' &&
                            !moBanDuPhong),
                      },
                      {
                        'trung-lich': viTriBan[slot.index].coTheDat === false,
                      },
                    ]"
                    @click="chonBan(viTriBan[slot.index])"
                  >
                    <div
                      v-if="
                        viTriBan[slot.index].loaiBan === 'DU_PHONG' &&
                        !moBanDuPhong
                      "
                      class="du-phong-khoa-overlay"
                    >
                      <i class="fa-solid fa-lock"></i>
                      <span>DỰ PHÒNG</span>
                    </div>

                    <div
                      v-else-if="viTriBan[slot.index].coTheDat === false"
                      class="trung-lich-overlay"
                    >
                      TRÙNG LỊCH
                    </div>

                    <div
                      v-else-if="isBanBiChanTheoHinhThuc(viTriBan[slot.index])"
                      class="invalid-overlay"
                    >
                      <span>{{ getLabelBan(viTriBan[slot.index]) }}</span>
                    </div>

                    <div
                      v-else-if="
                        viTriBan[slot.index].trangThai === 2 ||
                        viTriBan[slot.index].trangThai === 3
                      "
                      class="co-the-dat-overlay"
                    >
                      CÓ THỂ ĐẶT
                    </div>

                    <div
                      v-else-if="
                        viTriBan[slot.index].trangThai === 1 &&
                        !isBanHopLe(viTriBan[slot.index])
                      "
                      class="invalid-overlay"
                    >
                      <span>{{ getLabelBan(viTriBan[slot.index]) }}</span>
                    </div>

                    <div
                      v-if="
                        banDangChon.some(
                          (b) => b.id === viTriBan[slot.index].id,
                        )
                      "
                      class="corner-check"
                    >
                      <i class="fa-solid fa-circle-check"></i>
                    </div>

                    <div class="table-header">
                      <span class="table-code">
                        {{ viTriBan[slot.index].tenBanAn }}
                      </span>
                    </div>
                    <div class="table-body">
                      {{ viTriBan[slot.index].sucChuaToiDa }} chỗ
                    </div>
                    <div class="table-status-text">
                      {{ getTenTrangThai(viTriBan[slot.index].trangThai) }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="selected-sidebar">
            <div class="sidebar-header">
              Bàn đã chọn ({{ banDangChon.length }})
            </div>

            <div class="selected-list">
              <div v-if="banDangChon.length === 0" class="empty-state">
                <p>Chưa có bàn nào</p>
              </div>

              <div
                v-for="item in banDangChon"
                :key="item.id"
                class="selected-tag-item"
              >
                <div class="tag-info">
                  <span class="tag-name">Bàn {{ item.tenBanAn }}</span>
                  <span class="tag-area">{{ item.khuVuc?.tenKhuVuc }}</span>
                </div>
                <button class="remove-tag" @click="chonBan(item)">×</button>
              </div>
            </div>

            <div class="sidebar-footer">
              <div class="total-seats">
                Tổng chỗ:
                <b>{{ tongChoNgoi }}</b>
              </div>
              <div
                v-if="trangThaiSucChua === 'thieu'"
                class="alert-status-card"
              >
                <div class="alert-icon">
                  <i class="fa-solid fa-circle-exclamation"></i>
                </div>
                <div class="alert-content">
                  <span class="alert-title">Chưa đủ chỗ ngồi</span>
                  <span class="alert-desc">
                    Cần chọn thêm bàn cho {{ bookingData?.soNguoi }} khách
                  </span>
                </div>
              </div>

              <div v-if="trangThaiSucChua === 'vuot'" class="alert-status-card">
                <div class="alert-icon">
                  <i class="fa-solid fa-triangle-exclamation"></i>
                </div>
                <div class="alert-content">
                  <span class="alert-title">Dư quá số ghế</span>
                  <span class="alert-desc"> Vượt quá số ghế cho phép </span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <div class="footer-actions">
            <button class="btn-modern btn-cancel" @click="closePopup">
              <i class="bi bi-x-circle"></i>
              Hủy bỏ
            </button>
            <button
              class="btn-modern btn-reject"
              :disabled="daBiXepONoiKhac"
              :class="{ 'is-disabled': daBiXepONoiKhac }"
              @click="moModalTuChoi"
            >
              <i class="fa-solid fa-ban"></i>
              Từ chối
            </button>
            <button
              class="btn-modern btn-confirm"
              :class="{
                'is-disabled':
                  !(duCho && banDangChon.length > 0) || daBiXepONoiKhac,
              }"
              :disabled="!(duCho && banDangChon.length > 0) || daBiXepONoiKhac"
              @click="
                duCho &&
                  banDangChon.length > 0 &&
                  !daBiXepONoiKhac &&
                  xacNhanXepBan()
              "
            >
              <template v-if="duCho && banDangChon.length > 0">
                <i class="bi bi-check2-all"></i>
                Xác nhận
              </template>

              <template v-else>
                <i class="fa-solid fa-fingerprint"></i>
                Chưa đủ điều kiện
              </template>
            </button>
          </div>
        </div>
      </div>

      <!-- DIALOG CẢNH BÁO TRÙNG LỊCH -->
      <div
        v-if="showWarnTrungLich"
        class="modal-overlay blur-overlay"
        style="z-index: 4100"
        @click.self="huyBoCanhBao"
      >
        <div class="modal-warn-container">
          <div class="modal-warn-header">
            <div class="warn-icon-wrapper">
              <i class="fa-solid fa-triangle-exclamation"></i>
            </div>
            <div class="warn-header-text">
              <h3 class="modal-warn-title">Cảnh báo có thể trùng lịch</h3>
              <span class="modal-warn-subtitle"
                >Bàn <b>{{ banDangCanhBao?.tenBanAn }}</b></span
              >
            </div>
          </div>

          <div class="modal-warn-body">
            <div class="warn-message-box">
              <i class="fa-solid fa-circle-exclamation warn-msg-icon"></i>
              <p class="warn-message-text">
                Việc tạo thêm phiếu lúc
                <b>{{ formatDateTime(props.bookingData?.ngayGio) }}</b> có thể
                bị trùng lịch.
              </p>
            </div>
            <p class="warn-sub-desc">
              Bàn này hiện đang có lịch trong khung giờ gần đó. Bạn có chắc chắn
              muốn tiếp tục?
            </p>
          </div>

          <div class="modal-warn-footer">
            <button class="btn-modern btn-cancel" @click="huyBoCanhBao">
              <i class="bi bi-x-circle"></i>
              Hủy bỏ
            </button>
            <button
              class="btn-modern btn-warn-confirm"
              @click="xacNhanChonDuCanhBao"
            >
              <i class="bi bi-check2-all"></i>
              Xác nhận
            </button>
          </div>
        </div>
      </div>

      <div
        v-if="showModalTuChoi"
        class="modal-overlay blur-overlay"
        style="z-index: 4000"
        @click.self="dongModalTuChoi"
      >
        <div class="modal-reject-container">
          <div class="modal-reject-header">
            <div class="reject-icon-wrapper">
              <i class="fa-solid fa-triangle-exclamation"></i>
            </div>
            <div class="reject-header-text">
              <h3 class="modal-reject-title">Từ chối xếp bàn</h3>
              <span class="modal-reject-subtitle"
                >Khách hàng:
                <b>{{ bookingData?.khachHang || "Khách lẻ" }}</b></span
              >
            </div>
          </div>

          <div class="modal-reject-body">
            <p class="modal-reject-desc">
              Vui lòng cung cấp lý do từ chối xếp bàn. Hệ thống sẽ tự động gửi
              email thông báo kèm theo lý do này tới khách hàng.
            </p>

            <div class="textarea-wrapper">
              <label>Lý do từ chối <span class="text-danger">*</span></label>
              <textarea
                v-model="lyDoTuChoi"
                class="modal-reject-textarea"
                rows="4"
                placeholder="VD: Không còn bàn trống ở khung giờ đã đặt..."
              ></textarea>
            </div>
          </div>

          <div class="modal-reject-footer">
            <button class="btn-modern btn-cancel" @click="dongModalTuChoi">
              Hủy bỏ
            </button>
            <button class="btn-modern btn-danger" @click="xacNhanTuChoiBan">
              <i class="bi bi-check2-all"></i>
              Xác nhận
            </button>
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
    </div>
  </Teleport>
</template>

<style scoped>
/* /////////////////// CÓ THỂ ĐẶT //////////////// */
.co-the-dat-overlay {
  position: absolute;

  top: 40%;
  left: 50%;

  transform: translate(-50%, -50%);

  background: rgba(194, 192, 192, 0.96);

  color: #ffffff;

  padding: 6px 15px;

  border-radius: 9px;

  font-size: 12px;
  font-weight: 800;

  letter-spacing: 0.6px;
  text-transform: uppercase;

  white-space: nowrap;

  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(37, 99, 235, 0.15);

  z-index: 10;

  pointer-events: none;
}
/* ////////////// TRÙNG LỊCH /////////////// */
.trung-lich-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(0, 0, 0, 0.35);
  color: white;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1px;

  border-radius: 8px;
  z-index: 20;
}
/* MESSGAE CHUA DU CHO */
.alert-status-card {
  margin-top: 15px;
  padding: 12px 15px;
  background: #fff5f5; /* Nền đỏ cực nhẹ */
  border: 1px solid rgba(139, 30, 30, 0.1);
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 12px;
  animation: slideInUp 0.3s ease;
}

.alert-icon {
  width: 32px;
  height: 32px;
  background: #d82a2a; /* Màu đỏ đô thương hiệu */
  color: white;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
  box-shadow: 0 4px 8px rgba(139, 30, 30, 0.2);
}

.alert-content {
  display: flex;
  flex-direction: column;
}

.alert-title {
  font-size: 13px;
  font-weight: 700;
  color: #8b1e1e;
  line-height: 1.2;
}

.alert-desc {
  font-size: 11px;
  color: #a1a1a1;
  margin-top: 2px;
}

/* Animation cho chuyên nghiệp */
@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* HET MESSAGE */
/* BUTTON HUY BO & XÁC NHAN */
/* CONTAINER FOOTER - Đảm bảo sang phải tuyệt đối */
.modal-footer {
  display: flex !important;
  justify-content: flex-end !important; /* Đẩy cụm nút sang phải */
  padding: 20px 30px;
  background: #ffffff;
  border-top: 1px solid #f1f5f9;
  width: 100%; /* Ép rộng hết cỡ modal */
  box-sizing: border-box;
}

.footer-actions {
  display: flex;
  gap: 15px; /* Khoảng cách giữa 2 nút */
}

/* NÚT MODERN - Giữ đúng style sang trọng của bạn */
.btn-modern.btn-confirm.is-disabled {
  opacity: 0.5;
  filter: grayscale(0.5);
  cursor: not-allowed;
  transform: none !important;
  box-shadow: none !important;
}
.btn-modern.btn-confirm:not(.is-disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 25px rgba(0, 0, 0, 0.35);
}
.btn-modern {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 12px 32px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  border: none;
}

/* Nút Hủy bỏ */
.btn-cancel {
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.btn-cancel:hover {
  background: #f1f5f9;
  transform: translateY(-1px);
}

.btn-confirm {
  background: #d63f3f !important;
  color: #ffffff !important;
  box-shadow: 0 4px 12px rgba(139, 30, 30, 0.2);
}

.btn-confirm:hover {
  background: #dd2e2e !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(139, 30, 30, 0.3);
}

.btn-modern:active {
  transform: scale(0.96);
}

.btn-modern.btn-confirm {
  position: relative;
  overflow: hidden;
}
.btn-modern.btn-confirm::before {
  content: "";
  position: absolute;
  top: 0;
  left: -120%;
  width: 60%;
  height: 100%;

  background: linear-gradient(
    120deg,
    transparent,
    rgba(255, 255, 255, 0.15),
    transparent
  );

  transform: skewX(-20deg);
  transition: 0.6s ease;
  pointer-events: none;
}
.btn-modern.btn-confirm:hover::before {
  left: 120%;
}

/* HẾT */
.invalid-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 6px;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 6;
}

.invalid-overlay span {
  transform: translateY(-8px);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  text-align: center;
  padding: 0 5px;
}
/* Layout chính */
.modal-container.expanded {
  width: 96vw;
  max-width: 1400px;
  height: 92vh;
}
.modal-main-layout {
  display: flex;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}
.selection-section {
  flex: 1;
  padding: 20px;
  border-right: 1px solid #eee;
  overflow-y: auto;
}

/* Sidebar bên phải */
.selected-sidebar {
  width: 280px;
  background: #fdfdfd;
  display: flex;
  flex-direction: column;
  border-left: 1px solid #eee;
}

.sidebar-header {
  padding: 15px;
  font-weight: 800;
  font-size: 14px;
  color: #e95757;
  border-bottom: 1px solid #f0f0f0;
}

.selected-list {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
  gap: 8px;
  display: flex;
  flex-direction: column;
}

.selected-tag-item {
  display: flex;
  align-items: center;
  padding: 10px;
  background: white;
  border: 1px solid #ffebeb;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.03);
}

.tag-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.tag-name {
  font-weight: 700;
  color: #333;
}

.tag-area {
  font-size: 11px;
  color: #999;
}

.selected-overlay {
  position: absolute;
  inset: 0;
  background: rgba(233, 87, 87, 0.9); /* Đỏ FourSpice mờ */
  z-index: 10;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  animation: zoomIn 0.2s ease-out;
}

.check-icon {
  font-size: 26px;
  line-height: 1;
  font-weight: bold;
}

.check-text {
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.5px;
}

@keyframes zoomIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

/* Tinh chỉnh map */
.map-container {
  background: #f0f2f5;
  background-image: radial-gradient(#d1d1d1 0.5px, transparent 0.5px);
  background-size: 10px 10px;
}
.modal-main-layout {
  display: flex;
  flex: 1;
  min-height: 0;
}

.selection-section {
  flex: 1;
  border-right: 1px solid #eee;
  overflow-y: auto;
}

/* Sidebar bên phải */
.selected-sidebar {
  width: 280px;
  background: #fff;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 15px;
  font-weight: 700;
  font-size: 14px;
  color: #8b1e1e;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.selected-list {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* Style cho từng Tag bàn trong sidebar */
.selected-tag-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: #fff5f5;
  border: 1px solid rgba(139, 30, 30, 0.1);
  border-radius: 10px;
  animation: slideIn 0.2s ease-out;
}

.tag-name {
  font-weight: 700;
  font-size: 14px;
  color: #8b1e1e;
  margin-right: 8px;
}

.tag-area {
  font-size: 11px;
  color: #888;
  flex: 1;
}

.remove-tag {
  background: none;
  border: none;
  color: #ccc;
  font-size: 18px;
  cursor: pointer;
  padding: 0 5px;
}

.remove-tag:hover {
  color: #ff4d4f;
}

/* Lớp phủ ĐÃ CHỌN trên bàn */
.selected-overlay {
  position: absolute;
  inset: 0;
  background: rgba(139, 30, 30, 0.85); /* Màu đỏ đô thương hiệu mờ */
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 5;
  border-radius: 6px;
  color: white;
  animation: zoomIn 0.2s ease;
}

.selected-overlay i {
  font-size: 20px;
  margin-bottom: 2px;
}

.selected-overlay span {
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 1px;
}

/* Footer Sidebar */
.sidebar-footer {
  padding: 15px;
  background: #fafafa;
  border-top: 1px solid #f0f0f0;
}

.total-seats {
  font-size: 13px;
  color: #555;
}

/* Animation */
@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes zoomIn {
  from {
    transform: scale(0.8);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.empty-state {
  text-align: center;
  margin-top: 50px;
  color: #ccc;
}

.empty-state i {
  font-size: 40px;
}

.empty-state p {
  font-size: 12px;
  margin-top: 10px;
}
/* Hiệu ứng rung khi click vào bàn không khả dụng */
.table-card.not-available:active {
  animation: shake 0.2s ease-in-out;
}

@keyframes shake {
  0%,
  100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-4px);
  }
  75% {
    transform: translateX(4px);
  }
}

/* Giữ nguyên logic làm mờ */
.not-available {
  opacity: 0.5;
  filter: grayscale(0.6);
  cursor: not-allowed !important;
}
/* Làm mờ bàn */
/* Hiệu ứng làm mờ và chặn click cho bàn không trống */
.not-available {
  opacity: 0.5;
  filter: grayscale(0.4);
  cursor: not-allowed !important;
}

.table-status-text {
  position: absolute;
  bottom: 4px;
  right: 6px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  opacity: 0.9;
  color: rgba(255, 255, 255, 0.9);
}

.table-card.dang_don .table-status-text {
  color: rgba(0, 0, 0, 0.7);
}

/* SELECT CHON BAN */
/* 1. Hiệu ứng cho bàn khi được chọn */
.table-card.selected {
  border: 2px solid #8b1e1e !important;
  transform: translateY(-5px) scale(1.02);
  background: #ffffff !important;
  box-shadow: 0 10px 20px rgba(139, 30, 30, 0.15);
  z-index: 10;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.table-card.selected .table-body {
  color: #8b1e1e !important;
}
.table-card.selected .table-status-text {
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

.table-card {
  position: relative;
  border: 1.5px solid #e2e8f0;
  background: #ffffff;
  border-radius: 12px;
  padding: 12px;
  transition: all 0.2s ease;
}

.table-header {
  background: #f8fafc;
  border-radius: 6px;
  padding: 4px 8px;
  margin-bottom: 8px;
}

.table-code {
  color: #1e293b;
  font-size: 11px;
  letter-spacing: 0.5px;
}

.not-available {
  opacity: 0.4;
  filter: grayscale(0.8);
  cursor: not-allowed !important;
  pointer-events: all;
}
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3000;
  font-family: "Segoe UI", sans-serif;
}

.modal-container {
  background: #f8f9fa;
  width: 920px;
  border-radius: 12px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-title {
  font-size: 24px;
  font-weight: 700;
  padding: 15px 25px;
  background: white;
  border-bottom: 1px solid #eee;
  margin: 0;
}

/* Info Styling */

.info-section-luxury {
  padding: 20px 30px;
  background: #ffffff;
  border-bottom: 1px solid #f1f5f9;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 25px;
}

.info-card {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-icon {
  width: 38px;
  height: 38px;
  background: #f8fafc;
  color: #d53535;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  border: 1px solid #eef2f6;
  transition: all 0.3s ease;
}

.info-card:hover .card-icon {
  background: #e43232;
  color: #fff;
  transform: rotate(-10deg);
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

/* Label đen in đậm theo yêu cầu của bạn */
.card-body label {
  font-size: 16px;
  color: #313030;
  font-weight: 600;
  letter-spacing: 0.8px;
}

.value-text {
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
  white-space: nowrap;
}

.value-text.highlight {
  color: #8b1e1e;
  font-size: 16px;
}

.status-tag {
  color: #64748b;
}

.status-tag.online {
  color: #3b82f6;
}
/* Filter & Legend */
.selection-section {
  padding: 15px 25px;
  background: #fdfdfd;
  border-top: 1px solid #ccc;
}
.filter-bar {
  display: flex;
  align-items: flex-end;
  gap: 15px;
  margin-bottom: 15px;
  font-weight: 600;
  font-size: 13px;
}
.pink-select {
  border: 1px solid #ffb6c1;
  border-radius: 8px;
  padding: 2px 8px;
  outline: none;
}

/* MAP GRID */
.map-container {
  background: #eef1f4;
  padding: 35px;
  flex: 1;
  border-radius: 10px;
  border: 1px solid #dcdfe6;
  background-image: radial-gradient(#d1d1d1 0.8px, transparent 0.8px);
  background-size: 15px 15px;
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

/* Nút Mở bàn dự phòng */
.filter-du-phong {
  display: flex;
  align-items: center;
  margin-left: auto; /* Đẩy nút sang bên phải của filter bar */
}

.btn-mo-du-phong {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 42px;
  padding: 0 16px;
  border-radius: 10px;
  border: 1px solid #dcdfe6;
  background: #ffffff;
  color: #606266;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
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

/* Lớp phủ Khóa bàn dự phòng */
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

/* Kiểu màu cho bàn dự phòng */
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

.table-card.du-phong .table-status-text {
  color: #e5e7eb !important;
}

.table-card {
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  padding: 5px;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.table-card:not(.not-available):hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}
/* Giữ nguyên logic chặn click cho bàn không trống */
.not-available {
  opacity: 0.5;
  filter: grayscale(0.6);

  /* Thêm !important để ghi đè cursor: pointer ở trên */
  cursor: not-allowed !important;
}
.table-header {
  background: white;
  border-radius: 5px;
  padding: 2px 8px;
  height: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.table-code {
  font-weight: 800;
  font-size: 15px;
  color: #333;
}

.table-body {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  font-weight: 700;
  font-size: 15px;
  margin: 10px 0;
}

/* Large Table */
.ban-lon .table-header {
  justify-content: center;
}

/* Footer */
.modal-footer {
  display: flex;
  justify-content: center;
  gap: 30px;
  padding: 20px;
  border-top: 1px solid #eee;
  background: white;
}
.btn-cancel {
  background: #f0f0f0;
  border: none;
  padding: 10px 45px;
  border-radius: 6px;
  cursor: pointer;
  color: #666;
  font-weight: 600;
  transition: all 0.3s ease; /* Tạo hiệu ứng chuyển cảnh mượt */
}

.btn-cancel:hover {
  background: #e0e0e0;
  color: #333;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.btn-cancel:active {
  transform: scale(0.95); /* Nhấn xuống thì co lại tí */
}

.btn-confirm {
  background: #e95757;
  border: none;
  padding: 10px 45px;
  border-radius: 6px;
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(233, 87, 87, 0.3);
}

.btn-confirm:hover {
  background: #f14646; /* Sáng hơn một chút khi hover */
  box-shadow: 0 6px 15px rgba(233, 87, 87, 0.4);
  transform: translateY(-2px); /* Bay nhẹ lên trên */
}

.btn-confirm:active {
  transform: translateY(0) scale(0.95); /* Trở về vị trí cũ khi nhấn */
}

/* Thêm hiệu ứng cho nút khi bị disabled (nếu cần) */
.btn-confirm:disabled {
  background: #ffb6b6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}
.table-card.trong {
  background: #4caf50;
}

.table-card.co_khach {
  background: #f44336;
}

.table-card.dang_don {
  background: #ffca28;
  color: #000;
}

.table-card.da_dat {
  background: #42a5f5;
}

/* 
 TIM KIEM VA BO LOC */
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
.filter-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.filter-item label {
  font-size: 14px;
  font-weight: 600;
  color: #666;
}
.custom-input,
.custom-select {
  height: 38px;
  border: 1px solid #ffdada;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 14px;
  outline: none;
}
.input-with-icon {
  position: relative;
}

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

/* ===== RESPONSIVE ===== */
@media (max-width: 900px) {
  .modal-container.expanded {
    width: 98vw !important;
    height: 95vh !important;
    max-width: 100vw !important;
  }

  .modal-main-layout {
    flex-direction: column !important;
    overflow-y: auto !important;
  }

  .selection-section {
    border-right: none !important;
    border-bottom: 1px solid #eee !important;
    overflow-y: visible !important;
  }

  .selected-sidebar {
    width: 100% !important;
    border-left: none !important;
    border-top: 1px solid #eee !important;
    max-height: 260px !important;
    overflow-y: auto !important;
  }

  .info-grid {
    grid-template-columns: repeat(auto-fit, minmax(120px, 1fr)) !important;
    gap: 12px !important;
  }

  .info-section-luxury {
    padding: 14px 16px !important;
  }

  .filter-bar {
    flex-direction: column !important;
    align-items: stretch !important;
    gap: 8px !important;
  }

  .filter-item {
    width: 100% !important;
  }

  .so-do-header {
    display: none !important;
  }
  .grid-ban::before,
  .grid-ban::after {
    display: none !important;
  }
  .grid-ban {
    grid-template-columns: repeat(3, 1fr) !important;
    grid-template-rows: auto !important;
  }
  .slot.ban-lon {
    grid-row: auto !important;
    height: 140px !important;
  }

  .modal-footer {
    padding: 12px 16px !important;
  }
}

@media (max-width: 600px) {
  .modal-container.expanded {
    width: 100vw !important;
    height: 100vh !important;
    border-radius: 0 !important;
  }

  .modal-title {
    font-size: 18px !important;
    padding: 12px 16px !important;
  }

  .info-grid {
    grid-template-columns: 1fr 1fr !important;
    gap: 10px !important;
  }

  .card-body label {
    font-size: 13px !important;
  }

  .value-text {
    font-size: 13px !important;
  }

  .grid-ban {
    grid-template-columns: repeat(2, 1fr) !important;
  }

  .map-container {
    padding: 16px !important;
  }

  .footer-actions {
    flex-direction: column !important;
    width: 100% !important;
    gap: 8px !important;
  }

  .btn-modern {
    width: 100% !important;
    justify-content: center !important;
  }
}

/* ///////////////// TỪ CHỐI //////////////// */
.btn-reject {
  background: #ffffff !important;
  color: #8b1e1e !important;
  border: 1.5px solid #8b1e1e !important;
}

.btn-reject:hover {
  background: #fff5f5 !important;
  transform: translateY(-1px);
}

.btn-reject.is-disabled {
  opacity: 0.5;
  filter: grayscale(0.5);
  cursor: not-allowed;
  transform: none !important;
}

.blur-overlay {
  backdrop-filter: blur(4px);
  background: rgba(0, 0, 0, 0.4) !important;
}

.modal-reject-container {
  background: #ffffff;
  width: 480px;
  max-width: 92vw;
  border-radius: 16px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  animation: slideDown 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-reject-header {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 24px 28px 20px;
  background: linear-gradient(135deg, #fff5f5 0%, #ffffff 100%);
  border-bottom: 1px solid #f3e8e8;
}

.reject-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #fee2e2;
  color: #dc2626;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.15);
}

.reject-header-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.modal-reject-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  letter-spacing: -0.3px;
}

.modal-reject-subtitle {
  font-size: 13px;
  color: #64748b;
}

.modal-reject-subtitle b {
  color: #0f172a;
  font-weight: 600;
}

.modal-reject-body {
  padding: 24px 28px;
}

.modal-reject-desc {
  font-size: 14px;
  color: #475569;
  margin: 0 0 20px 0;
  line-height: 1.6;
}

.textarea-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.textarea-wrapper label {
  font-size: 13px;
  font-weight: 600;
  color: #334155;
}

.text-danger {
  color: #dc2626;
}

.modal-reject-textarea {
  width: 100%;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  padding: 14px;
  font-size: 14px;
  font-family: inherit;
  color: #1e293b;
  resize: vertical;
  outline: none;
  transition: all 0.2s ease;
  background: #f8fafc;
  box-sizing: border-box;
}

.modal-reject-textarea:focus {
  border-color: #dc2626;
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(220, 38, 38, 0.1);
}

.modal-reject-textarea::placeholder {
  color: #94a3b8;
}

.modal-reject-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 28px;
  background: #f8fafc;
  border-top: 1px solid #f1f5f9;
}

.btn-modern.btn-danger {
  background: #dc2626 !important;
  color: white !important;
  border: none !important;
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.2) !important;
  position: relative;
  overflow: hidden;
}

.btn-modern.btn-danger::after {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(
    to right,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.3) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  transform: skewX(-25deg);
  animation: dangerLightSweep 3s infinite;
}

@keyframes dangerLightSweep {
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

.btn-modern.btn-danger:hover {
  background: #b91c1c !important;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(220, 38, 38, 0.3) !important;
}

/* =========== DIALOG CẢNH BÁO TRÙNG LỊCH =========== */
.modal-warn-container {
  background: #ffffff;
  width: 480px;
  max-width: 92vw;
  border-radius: 7px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  border: 1px solid #ffe8cc;
  overflow: hidden;
  animation: slideDown 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.modal-warn-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #fffaf0 0%, #ffffff 100%);
  border-bottom: 1px solid #fcf5ec;
}

.warn-icon-wrapper {
  width: 42px;
  height: 42px;
  border-radius: 7px;
  background: #fff3e0;
  color: #f57c00;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
  box-shadow: 0 3px 8px rgba(245, 124, 0, 0.15);
}

.warn-header-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.modal-warn-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #1a1a1a;
  letter-spacing: -0.2px;
}

.modal-warn-subtitle {
  font-size: 12px;
  color: #64748b;
}

.modal-warn-subtitle b {
  color: #0f172a;
  font-weight: 600;
}

.modal-warn-body {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* HỘP MESSAGE CẢNH BÁO MÀU CAM NHẸ */
.warn-message-box {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  background: #fffcf5;
  border: 1px solid #ffe0b2;
  border-radius: 7px;
  padding: 12px 16px;
}

.warn-msg-icon {
  font-size: 16px;
  color: #ef6c00;
  margin-top: 2px;
  flex-shrink: 0;
}

.warn-message-text {
  margin: 0;
  font-size: 13.5px;
  color: #e65100;
  line-height: 1.5;
  font-weight: 500;
}

.warn-message-text b {
  color: #b71c1c;
  font-weight: 700;
}

.warn-sub-desc {
  margin: 0;
  font-size: 13px;
  color: #475569;
  line-height: 1.6;
}

.modal-warn-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 24px;
  background: #fafafb;
  border-top: 1px solid #f1f3f5;
}

/* NÚT MODAL */
.modal-warn-footer .btn-modern {
  border-radius: 7px !important;
  font-size: 13px;
  font-weight: 600;
  padding: 8px 16px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s ease;
}

.modal-warn-footer .btn-cancel {
  background: #f1f3f5 !important;
  color: #495057 !important;
  border: 1px solid #dee2e6 !important;
}

.modal-warn-footer .btn-cancel:hover {
  background: #e9ecef !important;
  color: #212529 !important;
}

.modal-warn-footer .btn-warn-confirm {
  background: #dc2626 !important;
  color: white !important;
  border: none !important;
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.2) !important;
  position: relative;
  overflow: hidden;
}

.modal-warn-footer .btn-warn-confirm::before {
  content: "";
  position: absolute;
  top: 0;
  left: -120%;
  width: 60%;
  height: 100%;
  background: linear-gradient(
    120deg,
    transparent,
    rgba(255, 255, 255, 0.15),
    transparent
  );
  transform: skewX(-20deg);
  transition: 0.6s ease;
  pointer-events: none;
}

.modal-warn-footer .btn-warn-confirm:hover::before {
  left: 120%;
}

.modal-warn-footer .btn-warn-confirm:hover {
  background: #b91c1c !important;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(220, 38, 38, 0.3) !important;
}
</style>
