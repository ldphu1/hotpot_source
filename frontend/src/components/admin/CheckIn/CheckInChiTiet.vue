<script setup>
import {
  defineProps,
  defineEmits,
  ref,
  computed,
  onMounted,
  onUnmounted,
  watch,
} from "vue";
import { useRouter, useRoute } from "vue-router";
import {
  getMonDaChonTheoHoaDon,
  daLenMon as daLenMonApi,
  daLenTatCa as daLenTatCaApi,
  xoaMonKhoiHoaDon,
  apDungMaGiamGia,
  getChiTietCheckIn,
  getChiTietCheckInTheoHoaDon,
} from "@/api/admin/checkin/checkinapi";
import XemDonHangCheckIn from "./XemDonHangCheckIn.vue";
import ChonMonAn from "./ChonMonAn.vue";
import LichSuHoaDon from "./LichSuHoaDon.vue";
import ChonBanCheckIn from "./ChonBanCheckIn.vue";
import {
  getBanTrong,
  doiBan,
  ghepBan,
  tachBan,
  ghepHaiBanDangDung,
} from "@/api/admin/checkin/checkinapi";
import { getThamSoInt } from "@/api/admin/thamsodong/thamSoDongApi";
import iconBan from "@/assets/icons/ban.png";

import { useUtils } from "@/utils/helper";
const { notify } = useUtils();
const router = useRouter();
const route = useRoute();

const showQR = ref(false);

const banLocal = ref({});
const maGiamGiaDangCapNhat = ref(null);
const hoaDonDangCapNhat = ref(null);

const props = defineProps({
  ban: {
    type: Object,
    default: () => ({}),
  },
  thongTinDat: {
    type: Object,
    default: null,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  dsHoaDon: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits([
  "close",
  "confirm",
  "update-mon",
  "update-hoa-don",
  "go-thanh-toan",
]);

const soKhachCheckIn = ref(1);
const thoiGianSuDungBanMacDinh = ref(120);

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

const sortedDsHoaDon = computed(() => {
  if (!props.dsHoaDon || props.dsHoaDon.length === 0) return [];
  return [...props.dsHoaDon].sort((a, b) => {
    const diff = getMocThoiGianHoaDon(a) - getMocThoiGianHoaDon(b);
    if (diff !== 0) return diff;
    return (
      Number(a?.idHoaDon || a?.id || 0) - Number(b?.idHoaDon || b?.id || 0)
    );
  });
});

watch(
  () => props.ban,
  (newVal) => {
    if (!newVal) return;

    soKhachCheckIn.value =
      newVal?.soNguoiDat ||
      newVal?.soNguoiDangDung ||
      newVal?.soKhach ||
      newVal?.soNguoiToiDa ||
      newVal?.sucChuaToiDa ||
      1;
  },
  { immediate: true, deep: true },
);

function diDenThanhToan(payload) {
  if (!banLocal.value?.idHoaDon) {
    notify.error("Không tìm thấy hóa đơn để thanh toán");
    return;
  }

  emit("go-thanh-toan", {
    idHoaDon: banLocal.value.idHoaDon,
    idBanAn: banLocal.value.idBanAn,
    maHoaDon: banLocal.value.maHoaDon,
    tenBanAn: banLocal.value.tenBanAn,
    diemTichLuySuDung:
      payload?.diemTichLuySuDung || banLocal.value.diemTichLuySuDung || 0,
    giamTrucTiep:
      payload?.giamTrucTiep !== undefined
        ? payload.giamTrucTiep
        : banLocal.value.giamTrucTiep || 0,
    lyDoGiamTrucTiep:
      payload?.lyDoGiamTrucTiep !== undefined
        ? payload.lyDoGiamTrucTiep
        : banLocal.value.lyDoGiamTrucTiep || "",
    maGiamGia: payload?.maGiamGia || banLocal.value.maGiamGia || null,
    soTienDuocGiam:
      payload?.soTienDuocGiam !== undefined
        ? payload.soTienDuocGiam
        : banLocal.value.soTienDuocGiam || 0,
    tienSauGiam:
      payload?.tienSauGiam !== undefined
        ? payload.tienSauGiam
        : banLocal.value.tienSauGiam || 0,
  });
}

async function moUuDai(payload) {
  try {
    if (!payload?.idHoaDon) return;

    const res = await apDungMaGiamGia(payload.idHoaDon, {
      maGiamGia: payload.maGiamGia,
    });

    // lưu lại snapshot mới nhất từ backend sau khi áp mã
    hoaDonDangCapNhat.value = {
      maGiamGia:
        res?.data?.maGiamGia ||
        res?.data?.phieuGiamGia?.maGiamGia ||
        payload.maGiamGia,
      soTienDuocGiam: res?.data?.soTienDuocGiam,
      giamTrucTiep: res?.data?.giamTrucTiep ?? 0,
      tienSauGiam: res?.data?.tienSauGiam,
      tongTien: res?.data?.tongTien,
      tienConLai: res?.data?.tienConLai,
      phieuGiamGia: {
        ...(res?.data?.phieuGiamGia || {}),
        maGiamGia:
          res?.data?.maGiamGia ||
          res?.data?.phieuGiamGia?.maGiamGia ||
          payload.maGiamGia,
      },
    };

    // cập nhật local ngay
    banLocal.value = {
      ...banLocal.value,
      ...(res?.data || {}),
      ...hoaDonDangCapNhat.value,
    };

    await reloadThongTinHoaDon();
    await loadMonDaChon();

    emit("update-hoa-don", {
      idHoaDon: banLocal.value.idHoaDon || banLocal.value.id,
      maHoaDon: banLocal.value.maHoaDon,
      ...hoaDonDangCapNhat.value,
    });

    hoaDonDangCapNhat.value = null;
  } catch (error) {
    console.error("Lỗi áp dụng ưu đãi:", error);
    if (error?.response?.data?.message) {
      notify.error(error.response.data.message);
    } else {
      notify.error("Lỗi áp dụng ưu đãi");
    }
  }
}

function capNhatHoaDonTuPopup(payload) {
  hoaDonDangCapNhat.value = null;

  banLocal.value = {
    ...banLocal.value,
    ...payload,
  };

  emit("update-hoa-don", {
    idHoaDon: banLocal.value.idHoaDon || banLocal.value.id,
    maHoaDon: banLocal.value.maHoaDon,
    ...payload,
  });
}

const hienThiViTriBan = computed(() => {
  const khuVuc =
    banLocal.value?.tenKhuVuc || banLocal.value?.khuVuc?.tenKhuVuc || "";

  let tenBan = "";
  if (banLocal.value?.listTenBan?.length) {
    tenBan = `Bàn ${banLocal.value.listTenBan.join(", ")}`;
  } else if (banLocal.value?.listIdBan?.length) {
    tenBan = `Bàn ${banLocal.value.listIdBan.join(", ")}`;
  } else if (banLocal.value?.tenBanAn) {
    tenBan = `Bàn ${banLocal.value.tenBanAn}`;
  }

  if (tenBan && khuVuc) {
    return `${tenBan} - ${khuVuc}`;
  }
  return tenBan || khuVuc || "Chưa xác định";
});

const laBanGhep = computed(() => {
  return (
    Array.isArray(banLocal.value?.listIdBan) &&
    banLocal.value.listIdBan.length > 1
  );
});

async function xoaMon(mon) {
  try {
    if (!mon?.id) return;

    if (
      mon?.idHoaDon &&
      banLocal.value?.idHoaDon &&
      Number(mon.idHoaDon) !== Number(banLocal.value.idHoaDon)
    ) {
      return;
    }

    await xoaMonKhoiHoaDon(mon.id, mon.tenBanGoi);
    await loadMonDaChon();
    await reloadThongTinHoaDon();
  } catch (error) {
    console.error("Lỗi xóa món:", error);
  }
}

async function reloadThongTinHoaDon(idBanReload = null) {
  try {
    const idBanAn =
      idBanReload || banLocal.value?.idBanAn || banLocal.value?.idBan;

    if (!idBanAn) return;

    const idHoaDon = banLocal.value?.idHoaDon || banLocal.value?.id;

    if (!idHoaDon) return;
    const currentIdHoaDon = idHoaDon;

    const res = await getChiTietCheckInTheoHoaDon(idBanAn, currentIdHoaDon);

    if (banLocal.value?.idHoaDon !== currentIdHoaDon) return;

    const data = res?.data || {};

    console.log("RELOAD HOADON =", data);

    banLocal.value = {
      ...banLocal.value,
      ...data,
      idBanAn: data.idBanAn || data.idBan || banLocal.value?.idBanAn,
      tenBanAn: data.tenBanAn || data.tenBan || banLocal.value?.tenBanAn,
      maBanAn: data.maBanAn || data.maBan || banLocal.value?.maBanAn,
      khuVucId: data.khuVucId || data.idKhuVuc || banLocal.value?.khuVucId,
      tenKhuVuc: data.tenKhuVuc || banLocal.value?.tenKhuVuc,

      maGiamGia:
        hoaDonDangCapNhat.value?.maGiamGia !== undefined
          ? hoaDonDangCapNhat.value?.maGiamGia
          : data.maGiamGia || data.phieuGiamGia?.maGiamGia || null,

      soTienDuocGiam:
        hoaDonDangCapNhat.value?.soTienDuocGiam !== undefined
          ? hoaDonDangCapNhat.value?.soTienDuocGiam
          : data.soTienDuocGiam ?? 0,

      giamTrucTiep:
        hoaDonDangCapNhat.value?.giamTrucTiep !== undefined
          ? hoaDonDangCapNhat.value?.giamTrucTiep
          : data.giamTrucTiep ?? 0,

      tienSauGiam:
        hoaDonDangCapNhat.value?.tienSauGiam !== undefined
          ? hoaDonDangCapNhat.value?.tienSauGiam
          : data.tienSauGiam ?? 0,

      tongTien:
        hoaDonDangCapNhat.value?.tongTien !== undefined
          ? hoaDonDangCapNhat.value?.tongTien
          : data.tongTien ?? 0,

      tienConLai:
        hoaDonDangCapNhat.value?.tienConLai !== undefined
          ? hoaDonDangCapNhat.value?.tienConLai
          : data.tienConLai ?? 0,

      tienCoc: data.tienCoc ?? 0,

      phieuGiamGia:
        hoaDonDangCapNhat.value?.phieuGiamGia !== undefined
          ? hoaDonDangCapNhat.value?.phieuGiamGia
          : data.phieuGiamGia || null,
    };

    if (!data.idHoaDon) {
      banLocal.value = {
        ...banLocal.value,
        idHoaDon: null,
        maHoaDon: null,
        trangThaiHoaDon: null,
        dsMon: [],
        tienCoc: 0,
        tongTienMon: 0,
        tongTien: 0,
        soTienDuocGiam: 0,
        giamTrucTiep: 0,
        tienSauGiam: 0,
        maGiamGia: null,
        phieuGiamGia: null,
      };

      emit("update-mon", []);
    } else {
      emit("update-hoa-don", {
        ...banLocal.value,
      });
    }
  } catch (error) {
    console.error("Lỗi reload thông tin hóa đơn:", error);
  }
}

async function daLenMonLe(mon) {
  try {
    if (!mon?.id) return;

    if (
      mon?.idHoaDon &&
      banLocal.value?.idHoaDon &&
      Number(mon.idHoaDon) !== Number(banLocal.value.idHoaDon)
    ) {
      return;
    }

    await daLenMonApi(mon.id, mon.tenBanGoi);
    await loadMonDaChon();
    await reloadThongTinHoaDon();
  } catch (error) {
    console.error("Lỗi đã lên món:", error);
  }
}

async function daLenTatCaAll(tenBan) {
  try {
    if (!banLocal.value?.idHoaDon) return;

    await daLenTatCaApi(banLocal.value.idHoaDon, tenBan);
    await loadMonDaChon();
    await reloadThongTinHoaDon();
  } catch (error) {
    console.error("Lỗi đã lên tất cả:", error);
  }
}

const showChonMon = ref(false);
const showXemDonHang = ref(false);
const showLichSuHoaDon = ref(false);

const getTrangThaiText = (status) => {
  if (status === 1) return "Trống";
  if (status === 2) return "Đã đặt";
  if (status === 3) return "Đang phục vụ";
  if (status === 4) return "Đang dọn";
  return "Không xác định";
};

const formatDateTime = (value) => {
  if (!value) return "--";
  return new Date(value).toLocaleString("vi-VN");
};

const gioCheckIn = computed(() => {
  return (
    banLocal.value?.thoiGianCheckIn ||
    banLocal.value?.thoiGianMo ||
    banLocal.value?.thoiGianDat ||
    null
  );
});

const gioRaDuKien = computed(() => {
  if (!gioCheckIn.value) return null;
  const checkInTime = new Date(gioCheckIn.value);
  if (Number.isNaN(checkInTime.getTime())) return null;
  return new Date(
    checkInTime.getTime() + thoiGianSuDungBanMacDinh.value * 60000,
  );
});

// ==== THỜI GIAN ĐANG SỬ DỤNG BÀN ====
const currentTime = ref(new Date());

let timerInterval;

onMounted(() => {
  timerInterval = setInterval(() => {
    currentTime.value = new Date();
  }, 1000);
});

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval);
});

const thoiGianSuDungBanText = computed(() => {
  if ((banLocal.value?.trangThaiBan ?? banLocal.value?.trangThai) !== 3)
    return null;
  if (!gioCheckIn.value) return null;

  const start = new Date(gioCheckIn.value).getTime();
  if (Number.isNaN(start)) return null;

  const diff = currentTime.value.getTime() - start;
  if (diff < 0) return "Chưa bắt đầu";

  const totalSeconds = Math.floor(diff / 1000);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;

  if (hours > 0) return `${hours} giờ ${minutes} phút ${seconds} giây`;
  return `${minutes} phút ${seconds} giây`;
});
// ===================================

async function moChonMon() {
  console.log("ban truyền sang chọn món =", banLocal.value);
  if (
    (banLocal.value?.trangThaiBan ?? banLocal.value?.trangThai) !== 3 ||
    !banLocal.value?.idBanAn ||
    !banLocal.value?.idHoaDon
  ) {
    console.log("Không thể chọn món, thiếu dữ liệu:", banLocal.value);
    return;
  }

  await loadMonDaChon();
  showChonMon.value = true;
}

async function moXemDonHang() {
  showChonMon.value = false;
  await loadMonDaChon();
  showXemDonHang.value = true;
}

async function xuLyThemMonTuXemDonHang() {
  showXemDonHang.value = false;
  await moChonMon();
}

const toMoney = (value) => {
  const num = Number(value || 0);
  return Number(num.toFixed(2));
};

const formatTien = (value) => {
  const num = Number(value || 0);
  return (
    new Intl.NumberFormat("vi-VN", {
      minimumFractionDigits: num % 1 !== 0 ? 2 : 0,
      maximumFractionDigits: 2,
    }).format(num) + "đ"
  );
};

const tinhTongTien = () => {
  if (!banLocal.value?.dsMon?.length) return 0;

  const tong = banLocal.value.dsMon.reduce((tong, mon) => {
    const gia = Number(mon.gia || mon.donGia || 0);
    const soLuong = Number(mon.soLuongChon || mon.soLuong || 1);
    return tong + gia * soLuong;
  }, 0);

  return toMoney(tong);
};

async function loadMonDaChon() {
  try {
    const currentIdHoaDon = banLocal.value?.idHoaDon;
    if (!currentIdHoaDon) return;

    const res = await getMonDaChonTheoHoaDon(currentIdHoaDon);
    if (banLocal.value?.idHoaDon !== currentIdHoaDon) return;

    const dsMon = Array.isArray(res.data) ? res.data : [];

    banLocal.value = {
      ...banLocal.value,
      dsMon,
    };

    emit("update-mon", dsMon);
  } catch (e) {
    console.error("Lỗi load món đã chọn:", e);

    banLocal.value = {
      ...banLocal.value,
      dsMon: [],
    };

    emit("update-mon", []);
  }
}

const mapTienCacTab = ref({});

async function loadTienCacTab() {
  // Đã gỡ bỏ logic tính tiền cục bộ để sử dụng giá trị tienConLai từ DB (giống với CheckIn.vue)
}

watch(
  () => props.dsHoaDon,
  () => {
    // loadTienCacTab();
  },
  { deep: true, immediate: true },
);

watch(
  () => banLocal.value?.idHoaDon,
  () => {
    // loadTienCacTab();
  },
);

const dsMonCho = computed(() => {
  return (banLocal.value?.dsMon || []).filter((m) => (m.trangThai ?? 0) === 0);
});

const dsMonDaLen = computed(() => {
  return (banLocal.value?.dsMon || []).filter((m) => m.trangThai === 1);
});

const tienCoc = computed(() => {
  return toMoney(banLocal.value?.tienCoc || 0);
});

const tongTienMon = computed(() => {
  const tong = dsMonDaLen.value.reduce((sum, mon) => {
    if (mon.thanhTien != null) return sum + Number(mon.thanhTien);

    const soLuong = Number(mon.soLuong || mon.soLuongChon || 0);
    const donGia = Number(mon.donGia || mon.gia || 0);
    return sum + soLuong * donGia;
  }, 0);

  return toMoney(tong);
});

const soTienGiam = computed(() => {
  return toMoney(banLocal.value?.soTienDuocGiam || 0);
});

const giamTrucTiep = computed(() => {
  return toMoney(banLocal.value?.giamTrucTiep || 0);
});

const tienSauGiam = computed(() => {
  return toMoney(
    Math.max(tongTienMon.value - soTienGiam.value - giamTrucTiep.value, 0),
  );
});

const tongThanhToanHienThi = computed(() => {
  return toMoney(Math.max(tienSauGiam.value - tienCoc.value, 0));
});

const tongThanhToanDuKien = computed(() => {
  const sauGiam = Math.max(
    tongTienMon.value - soTienGiam.value - giamTrucTiep.value,
    0,
  );
  return toMoney(Math.max(sauGiam - tienCoc.value, 0));
});

const maGiamGiaDangApDung = computed(() => {
  return (
    banLocal.value?.maGiamGia || banLocal.value?.phieuGiamGia?.maGiamGia || null
  );
});

const showChonBan = ref(false);
const modeChonBan = ref("doi-ban");
const dsBanTrong = ref([]);

async function hoanTatStateTachBan(payload) {
  const idBanMoi = payload.danhSachBanMoi?.[0]?.idBan;
  const idBanReload = idBanMoi || banLocal.value.idBanAn;

  banLocal.value = {
    ...banLocal.value,
    idBanAn: idBanReload,
    trangThaiBan: 3,
  };

  await reloadThongTinHoaDon(idBanReload);
  await loadMonDaChon();

  banLocal.value = {
    ...banLocal.value,
    modeTachHoaDon: false,
    duocGhepTuBanDangDung: false,
    actionGanNhat: "tach-ban",
  };

  emit("update-hoa-don", {
    ...banLocal.value,
    idBanAn: idBanReload,
    refreshGrid: true,
    action: payload.laBanCoNhieuHoaDon ? "tach-hoa-don" : "tach-ban",
  });
  showChonBan.value = false;
}

async function moDoiBan() {
  try {
    const res = await getBanTrong();
    dsBanTrong.value = Array.isArray(res.data) ? res.data : [];
    modeChonBan.value = "doi-ban";
    showChonBan.value = true;
  } catch (error) {
    console.error("Lỗi load bàn trống:", error);
    notify.error("Không tải được danh sách bàn");
  }
}

async function moGhepBan() {
  try {
    const res = await getBanTrong();
    dsBanTrong.value = Array.isArray(res.data) ? res.data : [];
    modeChonBan.value = "ghep-ban";
    showChonBan.value = true;
  } catch (error) {
    console.error("Lỗi load bàn trống:", error);
    notify.error("Không tải được danh sách bàn");
  }
}

async function moTachBan() {
  try {
    const res = await getBanTrong();
    dsBanTrong.value = Array.isArray(res.data) ? res.data : [];

    modeChonBan.value = "tach-ban";

    banLocal.value = {
      ...banLocal.value,
      modeTachHoaDon:
        banLocal.value?.modeTachHoaDon === true ||
        banLocal.value?.duocGhepTuBanDangDung === true ||
        banLocal.value?.actionGanNhat === "ghep-ban-dang-dung",
    };

    showChonBan.value = true;
  } catch (error) {
    console.error("Lỗi load bàn trống:", error);
    notify.error("Không tải được danh sách bàn");
  }
}

async function moGhepBanDangDung() {
  try {
    modeChonBan.value = "ghep-ban-dang-dung";
    showChonBan.value = true;
  } catch (error) {
    console.error("Lỗi mở gộp bàn đang dùng:", error);
    notify.error("Không mở được chức năng gộp bàn đang dùng");
  }
}

const coNhieuBan = computed(() => {
  return (
    Array.isArray(banLocal.value?.listIdBan) &&
    banLocal.value.listIdBan.length > 1
  );
});

const laBanCoNhieuHoaDon = computed(() => {
  return (
    banLocal.value?.modeTachHoaDon === true ||
    banLocal.value?.duocGhepTuBanDangDung === true ||
    banLocal.value?.actionGanNhat === "ghep-ban-dang-dung" ||
    banLocal.value?.soHoaDonDangHoatDong >= 2 ||
    banLocal.value?.soHoaDonTrongBan >= 2 ||
    banLocal.value?.coNhieuHoaDon === true ||
    banLocal.value?.laBanDungChungNhieuHoaDon === true
  );
});

async function xuLyXacNhanChonBan(payload) {
  try {
    if (!banLocal.value?.idHoaDon || !banLocal.value?.idBanAn) {
      notify.error("Thiếu thông tin hóa đơn hoặc bàn hiện tại");
      return;
    }

    if (payload.mode === "doi-ban") {
      const listIdBanMoi = Array.isArray(payload.listIdBanMoi)
        ? payload.listIdBanMoi.map(Number)
        : [Number(payload.idBanMoi)];

      const idBanReload = listIdBanMoi[0];

      await doiBan({
        idHoaDon: banLocal.value.idHoaDon,
        idBanHienTai: banLocal.value.idBanAn,
        idBanMoi: idBanReload,
        listIdBanMoi,
        soKhachThem: payload.soKhachThem,
      });

      banLocal.value = {
        ...banLocal.value,
        idBanAn: idBanReload,
        trangThaiBan: 3,
        listIdBan: listIdBanMoi,
        listTenBan: payload.dsBanDaChon?.map((b) => b.tenBanAn) || [],
      };

      await reloadThongTinHoaDon(idBanReload);
      await loadMonDaChon();

      emit("update-hoa-don", {
        ...banLocal.value,
        idBanAn: idBanReload,
        idHoaDon: banLocal.value.idHoaDon,
        listIdBan: listIdBanMoi,
        listTenBan: payload.dsBanDaChon?.map((b) => b.tenBanAn) || [],
        refreshGrid: true,
        action: "doi-ban",
      });
    }

    if (payload.mode === "ghep-ban") {
      await ghepBan({
        idHoaDon: banLocal.value.idHoaDon,
        idBanHienTai: banLocal.value.idBanAn,
        listIdBanMuonGhep: payload.listIdBanMuonGhep,
        soKhachThem: payload.soKhachThem,
      });

      await reloadThongTinHoaDon(banLocal.value.idBanAn);

      emit("update-hoa-don", {
        ...banLocal.value,
        refreshGrid: true,
        action: "ghep-ban",
      });
    }

    if (payload.mode === "tach-ban") {
      let tachHoaDon = payload.tachHoaDon || false;
      let tachMonDaGoi =
        Array.isArray(payload.danhSachMonTach) &&
        payload.danhSachMonTach.length > 0;

      await tachBan({
        idHoaDon: payload.idHoaDon || banLocal.value.idHoaDon,
        idBanGoc: payload.idBanGoc || banLocal.value.idBanAn,
        danhSachBanMoi: payload.danhSachBanMoi,
        danhSachMonTach: payload.danhSachMonTach || [],
        tachHoaDon,
        tachMonDaGoi,
      });

      await hoanTatStateTachBan(payload);
    }

    if (payload.mode === "ghep-ban-dang-dung") {
      await ghepHaiBanDangDung({
        idHoaDonChinh: payload.idHoaDonChinh,
        idHoaDonGhep: payload.idHoaDonGhep,
        idBanChinh: payload.idBanChinh,
        idBanGhep: payload.idBanGhep,
      });

      await reloadThongTinHoaDon(payload.idBanChinh);
      await loadMonDaChon();

      banLocal.value = {
        ...banLocal.value,
        idBanAn: payload.idBanChinh,
        modeTachHoaDon: true,
        duocGhepTuBanDangDung: true,
        actionGanNhat: "ghep-ban-dang-dung",
      };

      emit("update-hoa-don", {
        ...banLocal.value,
        idBanAn: payload.idBanChinh,
        modeTachHoaDon: true,
        duocGhepTuBanDangDung: true,
        actionGanNhat: "ghep-ban-dang-dung",
        refreshGrid: true,
        action: "ghep-ban-dang-dung",
      });
    }

    showChonBan.value = false;
  } catch (error) {
    console.error(error);
    notify.error(error?.response?.data?.message || "Thao tác thất bại");
  }
}

const showMenuDoiTach = ref(false);
const showMenuGopBan = ref(false);

function toggleMenuDoiTach() {
  showMenuDoiTach.value = !showMenuDoiTach.value;
  showMenuGopBan.value = false;
}

function toggleMenuGopBan() {
  showMenuGopBan.value = !showMenuGopBan.value;
  showMenuDoiTach.value = false;
}

async function loadThamSo() {
  try {
    const res = await getThamSoInt("THOI_GIAN_SU_DUNG_BAN_MAC_DINH");
    thoiGianSuDungBanMacDinh.value = Number(res.data || 120);
  } catch (e) {
    console.error("Lỗi load tham số THOI_GIAN_SU_DUNG_BAN_MAC_DINH", e);
  }
}

onMounted(() => {
  loadMonDaChon();
  loadThamSo();

  if (route.query.openXemDonHang === "1") {
    showXemDonHang.value = true;
  }
  if (route.query.openChonMon === "1") {
    showChonMon.value = true;
  }
});

watch(
  () => banLocal.value?.idHoaDon,
  async (newVal, oldVal) => {
    if (!newVal) {
      banLocal.value = {
        ...banLocal.value,
        dsMon: [],
      };
      emit("update-mon", []);
      return;
    }

    if (newVal !== oldVal) {
      banLocal.value = {
        ...banLocal.value,
        dsMon: [],
      };
      emit("update-mon", []);
    }

    await loadMonDaChon();
    await reloadThongTinHoaDon();
    isSwitchingTab.value = false;
  },
  { immediate: true },
);

watch(
  () => props.ban,
  (newVal) => {
    if (!newVal) return;

    const cungHoaDon =
      String(newVal?.idHoaDon || "") === String(banLocal.value?.idHoaDon || "");

    if (cungHoaDon) {
      const capNhatBan = { ...newVal };
      delete capNhatBan.idHoaDon;
      delete capNhatBan.maHoaDon;
      delete capNhatBan.trangThaiHoaDon;
      delete capNhatBan.tongTien;
      delete capNhatBan.tongTienMon;
      delete capNhatBan.tienCoc;
      delete capNhatBan.tienConLai;
      delete capNhatBan.tienSauGiam;
      delete capNhatBan.soTienDuocGiam;
      delete capNhatBan.giamTrucTiep;
      delete capNhatBan.maGiamGia;
      delete capNhatBan.phieuGiamGia;
      delete capNhatBan.dsMon;
      delete capNhatBan.idDatBan;
      delete capNhatBan.maDatBan;
      delete capNhatBan.thoiGianDat;
      delete capNhatBan.soNguoiToiDa;
      delete capNhatBan.trangThaiDatBan;
      delete capNhatBan.ghiChuDatBan;
      delete capNhatBan.tenKhachHang;
      delete capNhatBan.soDienThoai;
      delete capNhatBan.hinhThucDat;
      delete capNhatBan.soNguoiDangDung;
      delete capNhatBan.soGheTrong;
      delete capNhatBan.giamGiaTrucTiep;
      delete capNhatBan.maGiamGiaDangApDung;

      banLocal.value = {
        ...banLocal.value,
        ...capNhatBan,
      };

      if (hoaDonDangCapNhat.value) {
        banLocal.value = {
          ...banLocal.value,
          ...hoaDonDangCapNhat.value,
        };
      }
      return;
    }

    banLocal.value = {
      ...(newVal || {}),
    };
  },
  { immediate: true, deep: true },
);

async function xuLyXacNhanMon() {
  await loadMonDaChon();
  showChonMon.value = false;
}
function moLichSuHoaDon() {
  if (!banLocal.value?.idHoaDon) {
    console.log("Không tìm thấy hóa đơn để xem lịch sử");
    return;
  }

  showLichSuHoaDon.value = true;
}

function moQrDatMon() {
  if (
    (banLocal.value?.trangThaiBan ?? banLocal.value?.trangThai) !== 3 ||
    !banLocal.value?.idHoaDon ||
    !banLocal.value?.idBanAn
  ) {
    notify.error("Không tìm thấy thông tin bàn hoặc hóa đơn để tạo QR");
    return;
  }

  showQR.value = true;
}

const linkDatMon = computed(() => {
  const params = new URLSearchParams({
    idHoaDon: banLocal.value.idHoaDon || "",
    maHoaDon: banLocal.value.maHoaDon || "",
    idBanAn: banLocal.value.idBanAn || "",
    tenBanAn: banLocal.value.tenBanAn || "",
    tenKhuVuc: banLocal.value.tenKhuVuc || "",
  });

  return `${window.location.origin}/qr-chon-mon?${params.toString()}`;
});

const qrImageUrl = computed(() => {
  return `https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${encodeURIComponent(
    linkDatMon.value,
  )}`;
});

async function copyQRLink() {
  await navigator.clipboard.writeText(linkDatMon.value);
  notify.success("Đã copy link");
}

function xuLyButtonHoanTat() {
  if (tinhTongTien() === 0) {
    notify.error("Chưa chọn món, hãy chọn món trước khi hoàn tất");
    moXemDonHang();
    return;
  }

  if (dsMonCho.value.length > 0) {
    notify.error("Phải lên hết món mới được hoàn tất");
    moXemDonHang();
    return;
  }

  diDenThanhToan();
}

const isSwitchingTab = ref(false);

function switchTab(hd) {
  isSwitchingTab.value = true;

  const capNhatBan = { ...banLocal.value };
  delete capNhatBan.tongTien;
  delete capNhatBan.tongTienMon;
  delete capNhatBan.tienCoc;
  delete capNhatBan.tienConLai;
  delete capNhatBan.tienSauGiam;
  delete capNhatBan.soTienDuocGiam;
  delete capNhatBan.giamTrucTiep;
  delete capNhatBan.maGiamGia;
  delete capNhatBan.phieuGiamGia;

  banLocal.value = {
    ...capNhatBan,
    ...hd,
    idHoaDon: hd.idHoaDon,
  };
  emit("update-hoa-don", { idHoaDon: hd.idHoaDon, maHoaDon: hd.maHoaDon });
}

function capNhatHoaDonTrucTiep(payload) {
  if (!payload) return;
  if (
    hoaDonDangCapNhat.value &&
    (Number(payload.id) === Number(hoaDonDangCapNhat.value.idHoaDon) ||
      Number(payload.id) === Number(hoaDonDangCapNhat.value.id))
  ) {
    hoaDonDangCapNhat.value.tongTien = payload.tongTien;
    hoaDonDangCapNhat.value.tienSauGiam = payload.tienSauGiam;
    hoaDonDangCapNhat.value.soTienDuocGiam = payload.soTienDuocGiam;
    hoaDonDangCapNhat.value.giamTrucTiep = payload.giamTrucTiep;
    hoaDonDangCapNhat.value.tienConLai = payload.tienConLai;
    hoaDonDangCapNhat.value.phieuGiamGia = payload.idPhieuGiamGia
      ? {
          id: payload.idPhieuGiamGia,
          maGiamGia: payload.maGiamGia,
          tenGiamGia: payload.tenGiamGia,
          giaTriGiam: payload.giaTriGiam,
        }
      : null;
    hoaDonDangCapNhat.value.maGiamGia = payload.maGiamGia || null;
    hoaDonDangCapNhat.value.tenGiamGia = payload.tenGiamGia || null;
  }

  if (
    banLocal.value &&
    (Number(payload.id) === Number(banLocal.value.idHoaDon) ||
      Number(payload.id) === Number(banLocal.value.id))
  ) {
    banLocal.value.tongTien = payload.tongTien;
    banLocal.value.tienSauGiam = payload.tienSauGiam;
    banLocal.value.soTienDuocGiam = payload.soTienDuocGiam;
    banLocal.value.giamTrucTiep = payload.giamTrucTiep;
    banLocal.value.tienConLai = payload.tienConLai;
    banLocal.value.phieuGiamGia = payload.idPhieuGiamGia
      ? {
          id: payload.idPhieuGiamGia,
          maGiamGia: payload.maGiamGia,
          tenGiamGia: payload.tenGiamGia,
          giaTriGiam: payload.giaTriGiam,
        }
      : null;
    banLocal.value.maGiamGia = payload.maGiamGia || null;
    banLocal.value.tenGiamGia = payload.tenGiamGia || null;
    if (payload.diemTichLuySuDung !== undefined) {
      banLocal.value.diemTichLuySuDung = payload.diemTichLuySuDung;
    }
  }
}
function hienThiToast(payload) {
  if (!payload) return;
  const currentId =
    hoaDonDangCapNhat.value?.idHoaDon ||
    hoaDonDangCapNhat.value?.id ||
    banLocal.value?.idHoaDon ||
    banLocal.value?.id;
  if (currentId && Number(payload.idHoaDon) === Number(currentId)) {
    if (payload.toastType === "success") {
      notify.success(payload.message);
    } else if (payload.toastType === "warning") {
      notify.warning(payload.message);
    }
  }
}

defineExpose({
  loadMonDaChon,
  capNhatHoaDonTrucTiep,
  reloadThongTinHoaDon,
  hienThiToast,
});
</script>

<template>
  <div class="chi-tiet-wrapper">
    <div v-if="!showXemDonHang && !showChonMon" class="page-container">
      <div class="modal">
        <div class="modal-header">
          <div
            class="header-left"
            style="
              display: flex;
              flex-direction: column;
              gap: 4px;
              align-items: flex-start;
            "
          >
            <div style="display: flex; align-items: center; gap: 24px">
              <h2
                class="main-title"
                style="
                  margin: 0;
                  display: flex;
                  align-items: center;
                  gap: 10px;
                  font-size: 22px;
                  font-weight: 800;
                  color: var(--color-text);
                "
              >
                <span class="icon-wrapper">
                  <img :src="iconBan" alt="Table Icon" />
                </span>
                Thông tin bàn chi tiết
              </h2>

              <div
                class="tabs-container"
                v-if="sortedDsHoaDon && sortedDsHoaDon.length > 1"
              >
                <button
                  v-for="(hd, index) in sortedDsHoaDon"
                  :key="hd.idHoaDon"
                  class="tab-btn"
                  :class="{
                    active: Number(banLocal?.idHoaDon) === Number(hd.idHoaDon),
                  }"
                  @click="switchTab(hd)"
                >
                  <i :class="Number(banLocal?.idHoaDon) === Number(hd.idHoaDon) ? 'ri-checkbox-circle-fill text-green' : 'ri-receipt-line'"></i>
                  {{ hd.maHoaDon || "Hóa đơn " + (index + 1) }}
                  <span class="tab-money">
                    {{
                      formatTien(
                        Number(banLocal?.idHoaDon) === Number(hd.idHoaDon)
                          ? banLocal?.tienConLai ?? banLocal?.tongTien ?? 0
                          : hd.tienConLai ?? hd.tongTien ?? 0,
                      )
                    }}
                  </span>
                </button>
              </div>
            </div>
            <p
              class="subtitle"
              style="
                margin: 0;
                font-size: 14px;
                color: var(--color-text-muted);
                font-weight: 500;
              "
            >
              Thông tin chi tiết Bàn
              {{ banLocal?.tenBanAn || banLocal?.maBanAn || "..." }} -
              {{ banLocal?.tenKhuVuc || "" }}
            </p>
          </div>

          <span class="close-btn" @click="emit('close')">✕</span>
        </div>

        <div class="modal-body-split">
          <div class="info-cards-container">
            <div
              class="khach-box"
              v-if="
                (banLocal?.trangThaiBan ?? banLocal?.trangThai) === 2 ||
                (banLocal?.trangThaiBan ?? banLocal?.trangThai) === 3
              "
            >
              <div class="item">
                <b><i class="ri-qr-code-line text-blue"></i> Mã phiếu</b>
                <span>{{ banLocal?.maDatBan || "Chưa có mã" }}</span>
              </div>

              <div class="item">
                <b><i class="ri-user-star-line text-orange"></i> Khách hàng</b>
                <span>{{ banLocal?.tenKhachHang || "Khách vãng lai" }}</span>
              </div>

              <div class="item">
                <b><i class="ri-phone-line text-green"></i> Số điện thoại</b>
                <span>{{ banLocal?.soDienThoai || "N/A" }}</span>
              </div>

              <div class="item">
                <b><i class="ri-store-2-line text-cyan"></i> Hình thức đặt</b>
                <span>{{
                  banLocal?.hinhThucDat === 2
                    ? "Tại quầy"
                    : banLocal?.hinhThucDat === 1
                    ? "Online"
                    : "Khác"
                }}</span>
              </div>

              <div class="item">
                <b><i class="ri-time-line text-purple"></i> Giờ vào</b>
                <span>
                  {{ gioCheckIn ? formatDateTime(gioCheckIn) : "..." }}
                </span>
              </div>

              <div
                class="item"
                v-if="
                  (banLocal?.trangThaiBan ?? banLocal?.trangThai) === 3 &&
                  gioRaDuKien
                "
              >
                <b><i class="ri-timer-line text-red"></i> Dự kiến xong</b>
                <span>{{ formatDateTime(gioRaDuKien) }}</span>
              </div>

              <div class="item" v-if="thoiGianSuDungBanText">
                <b
                  ><i class="ri-flashlight-line text-yellow"></i> Đang sử
                  dụng</b
                >
                <span style="color: #d16969; font-weight: 600">
                  {{ thoiGianSuDungBanText }}
                </span>
              </div>

              <div class="item">
                <b><i class="ri-shield-check-line text-teal"></i> Trạng thái</b>
                <span
                  class="status-text"
                  :class="
                    (banLocal?.trangThaiBan ?? banLocal?.trangThai) === 3
                      ? 'status-serving'
                      : 'status-confirmed'
                  "
                >
                  {{
                    getTrangThaiText(
                      banLocal?.trangThaiBan ?? banLocal?.trangThai,
                    )
                  }}
                </span>
              </div>
            </div>

            <div class="section">
              <h4>
                <i class="ri-table-2 text-primary-color"></i> THÔNG TIN BÀN
              </h4>
              <div class="info-grid">
                <div class="info-row">
                  <span><i class="ri-group-line text-blue"></i> Sức chứa:</span>
                  <b
                    >{{
                      banLocal?.soNguoi ||
                      banLocal?.ghe ||
                      banLocal?.sucChuaToiDa
                    }}
                    người</b
                  >
                </div>

                <div
                  class="info-row"
                  v-if="(banLocal?.trangThaiBan ?? banLocal?.trangThai) === 3"
                >
                  <span
                    ><i class="ri-user-follow-line text-orange"></i> Khách đang
                    dùng:</span
                  >
                  <b>{{ banLocal?.soNguoiDat || 1 }} người</b>
                </div>

                <div
                  class="info-row"
                  v-if="(banLocal?.trangThaiBan ?? banLocal?.trangThai) !== 3"
                >
                  <span
                    ><i class="ri-user-add-line text-green"></i> Số khách:</span
                  >
                  <input
                    v-model.number="soKhachCheckIn"
                    type="number"
                    min="1"
                    class="input-so-khach"
                    placeholder="Nhập số khách"
                  />
                </div>

                <div class="info-row">
                  <span><i class="ri-map-pin-line text-red"></i> Vị trí:</span>
                  <b class="vi-tri-ban">
                    {{ hienThiViTriBan }}

                    <span v-if="laBanGhep" class="badge-ban-ghep">
                      <i class="ri-links-line"></i>
                      Bàn ghép
                    </span>
                  </b>
                </div>

                <div class="info-row">
                  <span><i class="ri-money-dollar-circle-line text-purple"></i> Tổng tiền món:</span>
                  <b style="color: #e65a5a;">{{ formatTien(banLocal?.tongTienMon ?? banLocal?.tongTien ?? 0) }}</b>
                </div>
              </div>
            </div>
          </div>

          <div class="actions-grid">
            <button
              class="btn-opt"
              :disabled="
                ![5, 6].includes(banLocal?.trangThaiHoaDon) ||
                !banLocal?.idHoaDon
              "
              @click="moChonMon"
            >
              <i class="ri-restaurant-line"></i> Thêm món
            </button>

            <button
              class="btn-opt"
              :disabled="
                ![5, 6].includes(banLocal?.trangThaiHoaDon) ||
                !banLocal?.idBanAn
              "
              @click="moQrDatMon"
            >
              <i class="ri-qr-code-line"></i> QR đặt món
            </button>

            <button
              class="btn-opt"
              :disabled="![5, 6].includes(banLocal?.trangThaiHoaDon)"
              @click="moXemDonHang"
            >
              <i class="ri-file-list-3-line"></i> Xem đơn hàng
            </button>

            <button
              class="btn-opt"
              :disabled="!banLocal?.idHoaDon"
              @click="moLichSuHoaDon"
            >
              <i class="ri-history-line"></i> Lịch sử hóa đơn
            </button>

            <div class="action-dropdown">
              <button
                class="btn-opt"
                :disabled="
                  ![5, 6].includes(banLocal?.trangThaiHoaDon) ||
                  !banLocal?.idHoaDon
                "
                @click="toggleMenuDoiTach"
              >
                <i class="ri-arrow-left-right-line"></i> Đổi / tách bàn
              </button>

              <div v-if="showMenuDoiTach" class="dropdown-actions">
                <button @click="moDoiBan">
                  <i class="ri-arrow-left-right-line"></i>Đổi bàn
                </button>

                <button @click="moTachBan">
                  <i class="ri-split-cells-horizontal"></i>
                  {{ laBanCoNhieuHoaDon ? "Tách hóa đơn" : "Tách bàn" }}
                </button>
              </div>
            </div>

            <div class="action-dropdown">
              <button
                class="btn-opt"
                :disabled="
                  ![5, 6].includes(banLocal?.trangThaiHoaDon) ||
                  !banLocal?.idHoaDon
                "
                @click="toggleMenuGopBan"
              >
                <i class="ri-layout-masonry-line"></i> Gộp bàn
              </button>

              <div v-if="showMenuGopBan" class="dropdown-actions">
                <button @click="moGhepBan">Gộp bàn trống</button>
                <button @click="moGhepBanDangDung">Gộp bàn đang dùng</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <Teleport to="body">
      <div v-if="showQR" class="qr-overlay" @click.self="showQR = false">
        <div class="qr-modal">
          <h2>QR đặt món</h2>

          <img :src="qrImageUrl" class="qr-img" />

          <div class="qr-link">{{ linkDatMon }}</div>

          <div class="qr-actions">
            <button class="btn-primary" @click="copyQRLink">
              <div class="btn-light-sweep"></div>
              Copy link
            </button>
            <button class="btn-cancel" @click="showQR = false">
              <i class="ri-close-line"></i> Đóng
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <XemDonHangCheckIn
      :show="showXemDonHang"
      :hoaDon="banLocal"
      :dsMon="banLocal?.dsMon || []"
      @close="showXemDonHang = false"
      @back="showXemDonHang = false"
      @thanh-toan="diDenThanhToan"
      @ap-dung-uu-dai="moUuDai"
      @xoa-mon="xoaMon"
      @da-len-mon="daLenMonLe"
      @da-len-tat-ca="daLenTatCaAll"
      @them-mon="xuLyThemMonTuXemDonHang"
      @update-hoa-don="capNhatHoaDonTuPopup"
    />
    <ChonMonAn
      v-if="showChonMon"
      :show="showChonMon"
      :ban="banLocal"
      @close="showChonMon = false"
      @added="xuLyXacNhanMon"
      @xem-don-hang="moXemDonHang"
    />
    <LichSuHoaDon
      :show="showLichSuHoaDon"
      :ban="banLocal"
      @close="showLichSuHoaDon = false"
      @back="showLichSuHoaDon = false"
    />
    <ChonBanCheckIn
      v-if="showChonBan"
      :show="showChonBan"
      :mode="modeChonBan"
      :ban-hien-tai="{
        ...banLocal,
        modeTachHoaDon: laBanCoNhieuHoaDon,
      }"
      :ds-ban-trong="dsBanTrong"
      @close="showChonBan = false"
      @confirm="xuLyXacNhanChonBan"
    />
    <TachMonCheckIn
      v-if="showTachMonModal"
      :show="showTachMonModal"
      :ds-mon="banLocal?.dsMon"
      :danh-sach-ban-moi="payloadTachBanPending?.danhSachBanMoi || []"
      @close="showTachMonModal = false"
      @confirm="thucHienTachBanSauKhiChonMon"
    />
  </div>
</template>

<style scoped>
.overlay,
.modal,
.modal-header,
.khach-box,
.section,
.info-grid,
.actions-grid,
.checkout-box,
.thanh-toan-box,
.modal-footer,
.qr-overlay,
.qr-modal,
.qr-actions,
button,
span,
div,
h3,
h4 {
  font-family: var(--font-family, "Inter", sans-serif);
  box-sizing: border-box;
}

/* ===== TABS ===== */
.tabs-container {
  display: flex;
  gap: 10px;
  overflow-x: auto;
}
.tab-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  background: #f8fafc;
  color: var(--color-text-muted);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}
.tab-btn:hover {
  background: #f1f5f9;
  color: var(--color-text);
}
.tab-btn.active {
  background: rgba(230, 90, 90, 0.1);
  border-color: rgba(230, 90, 90, 0.3);
  color: #e65a5a;
}
.tab-money {
  font-size: 13px;
  background: rgba(0, 0, 0, 0.05);
  padding: 2px 6px;
  border-radius: 4px;
}
.tab-btn.active .tab-money {
  background: rgba(230, 90, 90, 0.15);
  color: #e65a5a;
}

/* ===== OVERLAY ===== */
.page-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  width: 100%;
  animation: fadeOverlay 0.25s ease;
}

@keyframes fadeOverlay {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* ===== MODAL ===== */
.modal {
  position: relative;
  width: 100%;
  min-height: calc(100vh - 120px);
  background: transparent;
  padding: 0;
  box-shadow: none;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  color: var(--color-text);
  border: none;
  animation: scaleInModal 0.28s ease;
  overflow-y: auto;
  overflow-x: hidden;
}

.modal-header,
.khach-box,
.section,
.actions-grid,
.checkout-box,
.modal-footer {
  background: #ffffff;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03);
  border: 1px solid #eee;
}

@keyframes scaleInModal {
  from {
    opacity: 0;
    transform: translateY(10px) scale(0.97);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal > * {
  flex-shrink: 0;
}

.checkout-box {
  margin-top: 12px;
}

.actions-grid {
  display: grid;
}
.modal::-webkit-scrollbar {
  width: 8px;
}
.modal::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 999px;
}
.modal::-webkit-scrollbar-thumb {
  background: rgba(108, 117, 125, 0.3);
  border-radius: 999px;
}
.modal::-webkit-scrollbar-thumb:hover {
  background: var(--color-primary);
}

.modal-body-split {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-cards-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
}

/* ===== HEADER ===== */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.modal-header h3 {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: var(--color-text);

  display: flex;
  align-items: center;
  gap: 10px;

  text-transform: none;
  letter-spacing: -0.02em;
}

.close-btn {
  width: 34px;
  height: 34px;
  border-radius: 7px;
  display: inline-flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  font-size: 18px;
  color: var(--color-text-muted);
  font-weight: 700;
  transition: var(--transition);
  background: rgba(0, 0, 0, 0.04);
}

.close-btn:hover {
  background: rgba(230, 90, 90, 0.12);
  color: var(--color-primary);
  transform: rotate(90deg);
}

/* ===== KHÁCH BOX ===== */
.khach-box {
  background-color: #fdfdfd;
  border: 1px solid #f5ebeb;
  position: relative;
  overflow: hidden;
  border-radius: 10px;
  padding: 20px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px 30px;
  align-items: start;
}

.khach-box .item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
  margin-bottom: 0 !important;
  line-height: 1.4 !important;
}

.khach-box .item b {
  font-size: 13px !important;
  font-weight: 500 !important;
  letter-spacing: 0.02em;
  color: #7f8c8d;
  display: flex;
  align-items: center;
  gap: 6px;
}

.khach-box .item b i {
  color: var(--color-primary, #e65a5a);
  font-size: 14px;
}

.khach-box .item span {
  font-size: 15px !important;
  font-weight: 700 !important;
  color: #2c3e50;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.badge-status {
  display: inline-flex;
  align-items: center;
  justify-content: center;

  background-color: rgb(233, 215, 215);
  color: #000000;

  padding: 9px 14px;
  border-radius: 999px;

  font-size: 15px;
  font-weight: 800;

  letter-spacing: 0.05em;

  white-space: nowrap;

  box-shadow: 0 8px 18px rgba(230, 90, 90, 0.2);
}

@media (max-width: 768px) {
  .khach-box {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 520px) {
  .khach-box {
    grid-template-columns: 1fr;
  }

  .badge-status {
    position: static;
    width: fit-content;
  }
}

/* ===== SECTION ===== */

.section h4,
.section-title {
  margin: 0 0 12px;
  font-size: 13px;
  font-weight: 800;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

/* ===== INFO GRID ===== */
.info-grid {
  background-color: #fdfdfd;
  border: 1px solid #f5ebeb;
  border-radius: 10px;
  padding: 20px;

  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px 30px;
  align-items: start;

  position: relative;
  overflow: hidden;
}

.info-grid .info-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
  border: none;
}

.info-grid .info-row span:first-child {
  font-size: 13px !important;
  font-weight: 500 !important;
  letter-spacing: 0.02em;
  color: #7f8c8d;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 6px;
}

.info-grid .info-row span:first-child i {
  font-size: 14px;
}

.info-grid .info-row b,
.info-grid .info-row .badge-red,
.info-grid .info-row .badge-blue {
  font-size: 15px !important;
  font-weight: 700 !important;
  color: #2c3e50;

  white-space: normal;
  word-break: break-word;

  margin-left: 0;
}
@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 520px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}
.badge-red,
.badge-blue {
  display: inline-block;
  width: fit-content;
  padding: 5px 10px;
  border-radius: 999px;
  font-weight: 800;
  font-size: 12px;
}

.status-text {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
  width: fit-content;
}

.status-serving {
  background: rgba(139, 92, 246, 0.12) !important;
  color: #8b5cf6 !important;
  border: none !important;
}

.status-confirmed {
  background: rgba(59, 130, 246, 0.12) !important;
  color: #3b82f6 !important;
  border: none !important;
}

.badge-status {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 9px 14px;
  border-radius: 999px;
  font-size: 15px;
  font-weight: 800;
  letter-spacing: 0.05em;
  white-space: nowrap;
  box-shadow: none;
}

/* ===== ACTIONS ===== */
.actions-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  margin: 16px 0;
}

.btn-opt {
  width: 100%;
  min-height: 52px;

  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
@media (max-width: 768px) {
  .actions-grid {
    grid-template-columns: 1fr;
  }
}

.btn-opt {
  padding: 12px 14px;
  border-radius: 7px;
  border: 1px solid rgba(230, 90, 90, 0.35);
  background: #fff;
  color: var(--color-primary);
  cursor: pointer;
  font-size: var(--fs-button, 14px);
  font-weight: 700;
  text-transform: none;
  transition: all 0.25s ease;
  box-shadow: 0 4px 12px rgba(230, 90, 90, 0.08);
}

.btn-opt:not(:disabled):hover {
  background: linear-gradient(135deg, var(--color-primary), #d16969);
  color: #fff;
  border-color: transparent;
  transform: translateY(-2px);
  box-shadow: 0 12px 22px rgba(230, 90, 90, 0.22);
}

.btn-opt:not(:disabled):active {
  transform: translateY(0) scale(0.98);
}

.btn-opt:focus-visible {
  outline: none;
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.14);
}

.btn-opt:disabled {
  border: 1px solid rgba(0, 0, 0, 0.06);
  background: #e9ecef;
  color: #9ca3af;
  cursor: not-allowed;
  box-shadow: none;
}

/* ===== CHECKOUT ===== */
.checkout-box {
}

.thanh-toan-box {
  max-width: 100%;
  background: linear-gradient(
    135deg,
    rgba(230, 90, 90, 0.08),
    rgba(230, 90, 90, 0.03)
  );
  border: 1px solid rgba(230, 90, 90, 0.16);
  border-radius: 7px;
  padding: 18px;
}

.thanh-toan-title {
  font-weight: 800;
  color: var(--color-primary);
  margin-bottom: 16px;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.row-tien {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  color: var(--color-text);
  font-size: 14px;
  font-weight: 500;
  border-bottom: 1px dashed rgba(0, 0, 0, 0.08);
}

.row-tien:last-of-type {
  border-bottom: none;
}

.row-tien span:first-child {
  color: var(--color-text-muted);
  font-weight: 600;
}

.row-tien span:last-child {
  color: var(--color-text);
  font-weight: 700;
}

.tong-cong {
  margin-top: 8px;
  padding-top: 16px;
  border-top: 1px solid rgba(230, 90, 90, 0.16);
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 800;
  color: var(--color-text);
}

.tong-cong strong {
  color: var(--color-primary);
  font-size: 24px;
  font-weight: 800;
}

/* ===== FOOTER ===== */
.modal-footer {
  display: flex;
  gap: 12px;
  align-items: stretch;
}

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

/* ===== ALERT / EMPTY ===== */
.alert-info {
  background: rgba(230, 90, 90, 0.1);
  color: var(--color-primary);
  padding: 12px;
  border-radius: 7px;
  font-size: 13px;
  font-weight: 600;
  text-align: center;
  border: 1px solid rgba(230, 90, 90, 0.16);
  margin-bottom: 12px;
}

.btn-hoan-tat {
  width: 100%;
  background: #888;
  color: white;
  padding: 12px;
  border-radius: 7px;
  border: none;
  font-weight: 700;
  font-size: 14px;
}

.mt-3 {
  margin-top: 15px;
}

.checkout-title {
  font-size: 28px;
  font-weight: 800;
  color: var(--color-text);
  margin-bottom: 14px;
}

.ds-mon {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mon-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 10px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.mon-left {
  flex: 1;
}

.ten-mon {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text);
}

.mon-phu {
  margin-top: 4px;
  font-size: 13px;
  color: var(--color-text-muted);
  font-weight: 500;
}

.dot {
  margin: 0 6px;
}

.mon-right {
  font-size: 15px;
  font-weight: 800;
  color: var(--color-primary);
  white-space: nowrap;
}

.tong-tien {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 2px solid rgba(0, 0, 0, 0.08);
  font-size: 17px;
  font-weight: 700;
  color: var(--color-text);
}

.empty-cart {
  background: rgba(230, 90, 90, 0.08);
  border: 1px solid rgba(230, 90, 90, 0.16);
  color: var(--color-primary);
  padding: 14px;
  border-radius: 7px;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
}

/* ===== QR ===== */
.qr-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(5px);
  z-index: 999999;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.qr-modal {
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.98),
    rgba(255, 255, 255, 0.93)
  );
  padding: 28px;
  border-radius: 7px;
  text-align: center;
  width: 360px;
  max-width: 100%;
  box-shadow: 0 24px 50px rgba(15, 23, 42, 0.2);
  border: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.qr-modal h2 {
  margin: 0 0 12px;
  color: var(--color-text);
  font-size: 22px;
  font-weight: 800;
}

.qr-img {
  width: 260px;
  height: 260px;
  display: block;
  margin: 15px auto;
  border-radius: 7px;
  background: #fff;
  padding: 10px;
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
}

.qr-link {
  font-size: 12px;
  word-break: break-all;
  width: 100%;
  margin-bottom: 14px;
  color: var(--color-text-muted);
  line-height: 1.5;
  background: rgba(0, 0, 0, 0.03);
  border-radius: 7px;
  padding: 10px;
}

.qr-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
  width: 100%;
}

/* qr-actions buttons now use btn-primary and btn-cancel */

.vi-tri-ban {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;

  justify-content: flex-start;
  text-align: left;
}

.badge-ban-ghep {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 9px;
  border-radius: 999px;
  background: #f97316;
  color: #fff;
  font-size: 11px;
  font-weight: 800;
  white-space: nowrap;
  box-shadow: 0 4px 10px rgba(249, 115, 22, 0.25);
}

.input-so-khach {
  width: 120px;
  padding: 8px 10px;
  border: 1px solid rgba(230, 90, 90, 0.3);
  border-radius: 7px;
  outline: none;
  text-align: right;
  font-weight: 700;
  color: var(--color-text);
}

.input-so-khach:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(230, 90, 90, 0.12);
}

.action-dropdown-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
}

.action-dropdown {
  position: relative;
  width: 100%;
}

.action-dropdown > .btn-opt {
  width: 100%;
}

.dropdown-actions {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  z-index: 20;
  background: #fff;
  border: 1px solid rgba(230, 90, 90, 0.18);
  border-radius: 7px;
  padding: 8px;
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.14);
}

.dropdown-actions button {
  width: 100%;
  border: none;
  background: transparent;
  color: var(--color-text);
  padding: 10px 12px;
  border-radius: 7px;
  font-weight: 700;
  text-align: left;
}

.dropdown-actions button:hover {
  background: rgba(230, 90, 90, 0.08);
  color: var(--color-primary);
}

/* ===== DARK MODE ===== */
:deep(body.dark-mode) .page-container {
  background: transparent;
}
:deep(body.dark-mode) .qr-overlay {
  background: rgba(0, 0, 0, 0.72);
}

:deep(body.dark-mode) .modal {
  background: transparent;
  border: none;
  color: #e5e5e5;
  box-shadow: none;
}
:deep(body.dark-mode) .qr-modal {
  background: #181818;
  border: 1px solid #2a2a2a;
  color: #e5e5e5;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.45);
}

:deep(body.dark-mode) .modal-header,
:deep(body.dark-mode) .khach-box,
:deep(body.dark-mode) .section,
:deep(body.dark-mode) .actions-grid,
:deep(body.dark-mode) .checkout-box,
:deep(body.dark-mode) .modal-footer {
  background: #181818;
  border: 1px solid #2a2a2a;
}

:deep(body.dark-mode) .modal-header h3,
:deep(body.dark-mode) .qr-modal h2,
:deep(body.dark-mode) .thanh-toan-title,
:deep(body.dark-mode) .tong-cong,
:deep(body.dark-mode) .checkout-title,
:deep(body.dark-mode) .ten-mon,
:deep(body.dark-mode) .khach-box .item b {
  color: #fff;
}

:deep(body.dark-mode) .close-btn {
  background: #222;
  color: #bdbdbd;
}

:deep(body.dark-mode) .close-btn:hover {
  background: rgba(230, 90, 90, 0.18);
  color: #fff;
}

.btn-actions i {
  font-size: 14px;
}

.icon-wrapper {
  width: 48px;
  height: 48px;
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

.icon-wrapper img {
  width: 24px;
  height: 24px;
  object-fit: contain;
}

:deep(body.dark-mode) .khach-box,
:deep(body.dark-mode) .thanh-toan-box,
:deep(body.dark-mode) .alert-info,
:deep(body.dark-mode) .empty-cart {
  background: #1f1f1f;
  border: 1px solid #2a2a2a;
}

:deep(body.dark-mode) .khach-box .item,
:deep(body.dark-mode) .row-tien,
:deep(body.dark-mode) .info-grid .info-row b,
:deep(body.dark-mode) .mon-right,
:deep(body.dark-mode) .btn-opt,
:deep(body.dark-mode) .btn-cancel {
  color: #e5e5e5;
}

:deep(body.dark-mode) .section h4,
:deep(body.dark-mode) .section-title,
:deep(body.dark-mode) .info-grid .info-row span:first-child,
:deep(body.dark-mode) .row-tien span:first-child,
:deep(body.dark-mode) .mon-phu,
:deep(body.dark-mode) .qr-link {
  color: #bdbdbd;
}

:deep(body.dark-mode) .info-grid {
  background: #1f1f1f;
  border: 1px solid #2a2a2a;
}

:deep(body.dark-mode) .info-grid .info-row,
:deep(body.dark-mode) .mon-item,
:deep(body.dark-mode) .row-tien,
:deep(body.dark-mode) .modal-header {
  border-color: #2a2a2a;
}

:deep(body.dark-mode) .btn-opt {
  background: #202020;
  border-color: rgba(230, 90, 90, 0.28);
}

:deep(body.dark-mode) .btn-opt:not(:disabled):hover {
  color: #fff;
}

:deep(body.dark-mode) .btn-opt:disabled {
  background: #252525;
  color: #777;
  border-color: #2d2d2d;
}

:deep(body.dark-mode) .btn-cancel {
  background: #1f1f1f;
  border-color: rgba(230, 90, 90, 0.28);
}

:deep(body.dark-mode) .btn-cancel:hover {
  background: rgba(230, 90, 90, 0.12);
}

:deep(body.dark-mode) .qr-link {
  background: #202020;
}

:deep(body.dark-mode) .qr-img {
  background: #fff;
}

/* ===== NEW DISCOUNT STYLES ===== */
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

/* ===== RESPONSIVE ===== */
@media (max-width: 768px) {
  .page-container,
  .qr-overlay {
    padding: 12px;
  }

  .modal,
  .qr-modal {
    width: 100%;
    border-radius: 7px;
    padding: 18px;
  }

  .modal-header h3 {
    font-size: 18px;
  }

  .actions-grid {
    grid-template-columns: 1fr;
  }

  .modal-footer {
    flex-direction: column;
  }

  .badge-status {
    position: static;
    display: inline-flex;
    margin-top: 10px;
  }
  .info-grid .info-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .info-grid .info-row b,
  .info-grid .info-row .badge-red,
  .info-grid .info-row .badge-blue {
    margin-left: 0;
  }

  .qr-img {
    width: 220px;
    height: 220px;
  }

  .qr-actions {
    flex-direction: row;
  }
}

.text-blue {
  color: #3b82f6 !important;
}
.text-orange {
  color: #f97316 !important;
}
.text-green {
  color: #10b981 !important;
}
.text-cyan {
  color: #06b6d4 !important;
}
.text-purple {
  color: #8b5cf6 !important;
}
.text-red {
  color: #ef4444 !important;
}
.text-yellow {
  color: #eab308 !important;
}
.text-teal {
  color: #14b8a6 !important;
}
.text-primary-color {
  color: var(--color-primary, #e65a5a) !important;
}
</style>
