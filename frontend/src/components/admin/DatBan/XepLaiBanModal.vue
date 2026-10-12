<script setup>
/////////////////////// THU VIEN //////////////////
import { ref, watch } from "vue";
import { onMounted } from "vue";
import { computed } from "vue";
import { defineProps } from "vue";
import { defineEmits } from "vue";
import { onBeforeUnmount } from "vue";
import { useUtils } from "@/utils/helper";
/////////////////// API ?????????????????????
import { getAllBanAn } from "@/api/admin/datban/datBanApi";
import { getAllBanChoDat } from "@/api/admin/datban/datBanApi";
import { getAllkhuvuc } from "@/api/admin/khuvuc/khuVucApi";
//////////////////// HAM ??????????????????????
const { notify, nprogress } = useUtils();
const danhSachBan = ref([]);
const danhSachTang = ref([]);
const tangDangChon = ref("");
const banDangChon = ref([]);
const isInitialized = ref(false);

//////////////////////////// BỔ SUNG /////////////////////
const moBanDuPhong = ref(false);
const selectSoCho = ref(null);
const selectTrangThai = ref(null);
const danhSachSoCho = ref([]);

// DIALOG CẢNH BÁO TRÙNG LỊCH
const showWarnTrungLich = ref(false);
const banDangCanhBao = ref(null);

const isOnlineBooking = computed(() => {
  const ht = props.bookingData?.hinhThucDat;
  if (typeof ht === "string") {
    return ht === "Trực tuyến" || ht.toLowerCase().includes("online");
  }
  return ht === 1;
});

const coBanKhaDungTheoLoai = (loai) => {
  return danhSachBan.value.some((table) => {
    if (table.loaiBan !== loai) return false;
    if (table.coTheDat === false && !isThuocPhieuNay(table.id)) return false;
    const daChon = banDangChon.value.some((b) => b.id === table.id);
    if (daChon) return false;
    if (table.loaiBan === "DU_PHONG" && !moBanDuPhong.value) return false;

    const soNguoi = props.bookingData?.soNguoi || 0;
    const max = soNguoi + soGheDuToiDa.value;
    const tongSau = tongChoNgoi.value + table.sucChuaToiDa;
    if (tongSau > max) return false;

    return true;
  });
};

const isBanBiChanTheoHinhThuc = (table) => {
  const daChon = banDangChon.value.some((b) => b.id === table.id);
  if (daChon) return false;
  if (isThuocPhieuNay(table.id)) return false; // bàn đã thuộc phiếu này thì không bị chặn

  if (isOnlineBooking.value) {
    if (table.loaiBan === "OFFLINE" && coBanKhaDungTheoLoai("ONLINE"))
      return true;
  } else {
    if (table.loaiBan === "ONLINE" && coBanKhaDungTheoLoai("OFFLINE"))
      return true;
  }
  return false;
};

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
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";
import { getAllThamSoDong } from "@/api/admin/thamsodong/thamSoDongApi";
import { connectWebSocket, disconnectWebSocket } from "@/utils/websocket";

const props = defineProps(["bookingData"]);
console.log("DANH SÁCH THÔNG TIN GỬI ", props.bookingData);
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

const isThuocPhieuNay = (tableId) => {
  const dsBanDaXep = props.bookingData?.dsBan || [];
  return dsBanDaXep.some((id) => String(id) === String(tableId));
};

const getClassTrangThai = (table) => {
  if (table.coTheDat === false && !isThuocPhieuNay(table.id))
    return "trung-lich";
  if (
    table.loaiBan === "DU_PHONG" &&
    !moBanDuPhong.value &&
    !isThuocPhieuNay(table.id)
  )
    return "du-phong";

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
  if (tong > max) return "vuot";
  return "ok";
});

const soGheDuToiDa = computed(() =>
  Number(
    thamSoDong.value.find((i) => i.maThamSo === "SO_GHE_DU_TOI_DA")?.giaTri ||
      0,
  ),
);

const isBanHopLe = (table) => {
  const daChon = banDangChon.value.some((b) => b.id === table.id);
  if (daChon) return true;

  if (
    table.loaiBan === "DU_PHONG" &&
    !moBanDuPhong.value &&
    !isThuocPhieuNay(table.id)
  )
    return false;

  if (isBanBiChanTheoHinhThuc(table)) return false;

  if (table.coTheDat === false && !isThuocPhieuNay(table.id)) return false;

  const soNguoi = props.bookingData?.soNguoi || 0;
  const max = soNguoi + soGheDuToiDa.value;
  const tongSauKhiThem = tongChoNgoi.value + table.sucChuaToiDa;

  if (tongSauKhiThem > max) return false;

  return true;
};

const getLabelBan = (table) => {
  if (table.coTheDat === false && !isThuocPhieuNay(table.id)) {
    return "TRÙNG LỊCH";
  }

  if (
    table.loaiBan === "DU_PHONG" &&
    !moBanDuPhong.value &&
    !isThuocPhieuNay(table.id)
  ) {
    return "ĐANG KHÓA";
  }

  if (isBanBiChanTheoHinhThuc(table)) {
    return isOnlineBooking.value ? "ƯU TIÊN OFFLINE" : "ƯU TIÊN ONLINE";
  }

  const soNguoi = props.bookingData?.soNguoi || 0;
  const tongSau = tongChoNgoi.value + table.sucChuaToiDa;
  const max = soNguoi + soGheDuToiDa.value;

  if (tongSau > max) return "VƯỢT QUÁ SỐ GHẾ";

  return "";
};

const chonBan = (table) => {
  const index = banDangChon.value.findIndex((b) => b.id === table.id);

  if (index !== -1) {
    banDangChon.value.splice(index, 1);
    notify.success(`Đã bỏ chọn bàn: ${table.tenBanAn}`);
    return;
  }

  if (
    table.loaiBan === "DU_PHONG" &&
    !moBanDuPhong.value &&
    !isThuocPhieuNay(table.id)
  ) {
    notify.error(
      "Bàn dự phòng đang bị khóa. Vui lòng nhấn 'Mở bàn dự phòng' để sử dụng!",
    );
    return;
  }

  if (isBanBiChanTheoHinhThuc(table)) {
    const msg = isOnlineBooking.value
      ? "Ưu tiên chọn bàn ONLINE (chỉ dùng bàn OFFLINE khi hết bàn ONLINE khả dụng)!"
      : "Ưu tiên chọn bàn OFFLINE (chỉ dùng bàn ONLINE khi hết bàn OFFLINE khả dụng)!";
    notify.error(msg);
    return;
  }

  if (!table.coTheDat && !isThuocPhieuNay(table.id)) {
    notify.error(`Bàn ${table.tenBanAn} đã có lịch đặt`);
    return;
  }

  if (!isBanHopLe(table)) {
    notify.error(`Bàn ${table.tenBanAn} vượt số ghế cho phép`);
    return;
  }

  // Bàn có thể đặt nhưng đang có trạng thái Đã đặt / Có khách và không thuộc phiếu này => cảnh báo trùng lịch
  if ((table.trangThai === 2 || table.trangThai === 3) && !isThuocPhieuNay(table.id)) {
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

  const isConfirm = await notify.confirm("Xác nhận xếp bàn cho khách?");
  if (!isConfirm) return;

  nprogress.start();

  try {
    const payload = {
      hinhThucDat: props.bookingData?.hinhThucDat,
      listIdBan: banDangChon.value.map((b) => b.id),
      monDatTruoc: [],
    };

    notify.success("Xếp bàn thành công");

    emit("success", {
      listIdBan: payload.listIdBan,
      listTenBan: banDangChon.value.map((b) => b.tenBanAn),
    });

    emit("close");
  } catch (e) {
    console.error(" ERROR:", e);
    console.error(" RESPONSE ERROR:", e.response);
    notify.error(e.response?.data?.message || "Xếp bàn thất bại!");
  } finally {
    nprogress.done();
  }
};

// Load bàn
const loadBan = async () => {
  try {
    nprogress.start();

    // load bàn
    let resBan;

    const thoiGianDat = props.bookingData?.gioDen;

    if (thoiGianDat) {
      resBan = await getAllBanChoDat(thoiGianDat);
    } else {
      resBan = await getAllBanAn();
    }
    const dataBan =
      resBan.data.data || resBan.data.content || resBan.data || [];

    danhSachBan.value = dataBan;

    const dsBanDaXep = props.bookingData?.dsBan || [];

    if (!isInitialized.value) {
      if (dsBanDaXep.length > 0) {
        banDangChon.value = danhSachBan.value.filter((ban) => {
          return dsBanDaXep.some((b) => String(b) === String(ban.id));
        });
      }
      isInitialized.value = true;
    } else {
      const currentSelectedIds = banDangChon.value.map((b) => String(b.id));
      banDangChon.value = danhSachBan.value.filter((ban) =>
        currentSelectedIds.includes(String(ban.id)),
      );
    }

    // load khu vực
    const resKhuVuc = await getAllkhuvuc();
    const dataKhuVuc =
      resKhuVuc.data.data || resKhuVuc.data.content || resKhuVuc.data || [];
    danhSachTang.value = dataKhuVuc;

    // chọn tầng đầu
    if (danhSachTang.value.length > 0 && !tangDangChon.value) {
      tangDangChon.value = danhSachTang.value[0].id;
    }
    danhSachSoCho.value = [
      ...new Map(
        dataBan.map((b) => [
          b.sucChuaToiDa,
          { label: `${b.sucChuaToiDa} chỗ`, value: b.sucChuaToiDa },
        ]),
      ).values(),
    ];
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
  loadBan();
};

onMounted(() => {
  loadThamSo();
  loadBan();

  // Realtime
  connectWebSocket(null, handleWsMessage);
});

onBeforeUnmount(() => {
  disconnectWebSocket(handleWsMessage);
});
const banDaLoc = computed(() => {
  return danhSachBan.value.filter((ban) => {
    if (tangDangChon.value && ban.khuVuc?.id !== tangDangChon.value)
      return false;
    if (selectSoCho.value && ban.sucChuaToiDa !== selectSoCho.value)
      return false;
    if (selectTrangThai.value && ban.trangThai !== selectTrangThai.value)
      return false;
    return true;
  });
});

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
  const banTheoTang = banDaLoc.value;

  const banOffline = banTheoTang.filter((b) => b.loaiBan === "OFFLINE");
  const banOnline = banTheoTang.filter((b) => b.loaiBan === "ONLINE");
  const banDuPhong = banTheoTang.filter((b) => b.loaiBan === "DU_PHONG");

  const xepNhomBan = (nhom, loaiBan) => {
    const sapXep = [...nhom].sort((a, b) => (laBanLon(a) ? -1 : 1));
    let currentSlot = 0;

    sapXep.forEach((b) => {
      while (map[currentSlot] || !isSlotHopLe(currentSlot, loaiBan))
        currentSlot++;

      if (laBanLon(b)) {
        while (
          map[currentSlot] ||
          !isSlotHopLe(currentSlot, loaiBan) ||
          map[currentSlot + SO_COT] ||
          !isSlotHopLe(currentSlot + SO_COT, loaiBan) ||
          map[currentSlot + SO_COT * 2] ||
          !isSlotHopLe(currentSlot + SO_COT * 2, loaiBan)
        )
          currentSlot++;
      }

      map[currentSlot] = b;
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

  let maxSlot = 18;
  Object.keys(map).forEach((key) => {
    const idx = parseInt(key);
    const item = map[idx];
    let endIdx = idx;
    if (item && typeof item === "object" && laBanLon(item))
      endIdx = idx + SO_COT * 2;
    if (endIdx >= maxSlot) maxSlot = endIdx + 1;
  });
  maxSlot = Math.ceil(maxSlot / SO_COT) * SO_COT;
  TONG_SLOT.value = maxSlot;

  viTriBan.value = map;
};

watch(banDaLoc, () => khoiTaoSlot(), { deep: true, immediate: true });
</script>

<template>
  <div class="modal-overlay" @click.self="closePopup">
    <div class="modal-container expanded">
      <h1 class="modal-title">
        <i class="fa-solid fa-chair" style="color: red; margin-right: 10px"></i>
        Chọn bàn cho khách
      </h1>
      <div class="info-section-luxury">
        <div class="info-grid">
          <div class="info-card">
            <div class="card-icon"><i class="fa-solid fa-user-tie"></i></div>
            <div class="card-body">
              <label>Khách hàng</label>
              <div class="value-text">
                {{ bookingData?.tenKhachHang || "Khách lẻ" }}
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
                {{ bookingData?.soDienThoai || "---" }}
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
                {{ formatDateTime(bookingData?.gioDen) }}
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
                :class="{ online: bookingData?.hinhThucDat === 1 }"
              >
                {{ bookingData?.hinhThucDat === 2 ? "Tại quầy" : "Online" }}
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="modal-main-layout">
        <div class="selection-section">
          <div class="filter-bar">
            <div class="filter-item">
              Tầng
              <v-select
                v-model="tangDangChon"
                :options="danhSachTang"
                label="tenKhuVuc"
                :reduce="(t) => t.id"
                :clearable="false"
              />
            </div>

            <div class="filter-item">
              Số chỗ
              <v-select
                v-model="selectSoCho"
                :options="danhSachSoCho"
                label="label"
                :reduce="(t) => t.value"
                placeholder="Chọn số chỗ"
                :clearable="true"
              />
            </div>

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
                :reduce="(t) => t.value"
                placeholder="Chọn trạng thái"
                :clearable="true"
              />
            </div>

            <div class="filter-du-phong">
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
          </div>

          <div class="map-container">
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
                    !moBanDuPhong &&
                    !isThuocPhieuNay(viTriBan[slot.index].id)
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
                        (viTriBan[slot.index].coTheDat === false &&
                          !isThuocPhieuNay(viTriBan[slot.index].id)) ||
                        !isBanHopLe(viTriBan[slot.index]),
                    },
                  ]"
                  @click="chonBan(viTriBan[slot.index])"
                >
                  <div
                    v-if="
                      viTriBan[slot.index].loaiBan === 'DU_PHONG' &&
                      !moBanDuPhong &&
                      !isThuocPhieuNay(viTriBan[slot.index].id)
                    "
                    class="du-phong-khoa-overlay"
                  >
                    <i class="fa-solid fa-lock"></i><span>DỰ PHÒNG</span>
                  </div>

                  <div
                    v-else-if="
                      viTriBan[slot.index].coTheDat === false &&
                      !isThuocPhieuNay(viTriBan[slot.index].id)
                    "
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
                      banDangChon.some((b) => b.id === viTriBan[slot.index].id)
                    "
                    class="corner-check"
                  >
                    <i class="fa-solid fa-circle-check"></i>
                  </div>

                  <div class="table-header">
                    <span class="table-code">{{
                      viTriBan[slot.index].tenBanAn
                    }}</span>
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
            <div v-if="!duCho" class="alert-status-card">
              <div class="alert-icon">
                <i class="fa-solid fa-circle-exclamation"></i>
              </div>
              <div class="alert-content">
                <span class="alert-title">Chưa đủ chỗ ngồi</span>
                <span class="alert-desc"
                  >Cần chọn thêm bàn cho {{ bookingData?.soNguoi }} khách</span
                >
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
            class="btn-modern btn-confirm"
            :class="{ 'is-disabled': !(duCho && banDangChon.length > 0) }"
            :disabled="!(duCho && banDangChon.length > 0)"
            @click="duCho && banDangChon.length > 0 && xacNhanXepBan()"
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
              <b>{{ formatDateTime(props.bookingData?.gioDen) }}</b> có thể
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
  </div>
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

/* MESSAGE CHƯA ĐỦ CHỖ */
.alert-status-card {
  margin-top: 15px;
  padding: 12px 15px;
  background: #fff5f5;
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
  background: #d82a2a;
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

/* FOOTER */
.modal-footer {
  display: flex !important;
  justify-content: flex-end !important;
  padding: 20px 30px;
  background: #ffffff;
  border-top: 1px solid #f1f5f9;
  width: 100%;
  box-sizing: border-box;
}

.footer-actions {
  display: flex;
  gap: 15px;
}

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

.btn-confirm:disabled {
  background: #ffb6b6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* OVERLAY NHÃN (TRÙNG LỊCH PHỤ / ƯU TIÊN / VƯỢT SỐ GHẾ) */
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
  background: #fff;
  display: flex;
  flex-direction: column;
  border-left: 1px solid #eee;
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

.selected-tag-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: #fff5f5;
  border: 1px solid rgba(139, 30, 30, 0.1);
  border-radius: 10px;
  animation: slideIn 0.2s ease-out;
}

.tag-info {
  flex: 1;
  display: flex;
  flex-direction: column;
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

/* Tinh chỉnh map */
.map-container {
  background: #eef1f4;
  padding: 35px;
  flex: 1;
  border-radius: 10px;
  border: 1px solid #dcdfe6;
  background-image: radial-gradient(#d1d1d1 0.8px, transparent 0.8px);
  background-size: 15px 15px;
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

.not-available {
  opacity: 0.5;
  filter: grayscale(0.6);
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

/* SELECT CHỌN BÀN */
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
  display: flex;
  flex-direction: column;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.table-card:not(.not-available):hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
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
  font-weight: 800;
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

/* Nút Mở bàn dự phòng */
.filter-du-phong {
  display: flex;
  align-items: center;
  margin-left: auto;
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

/* Tiêu đề phân khu sơ đồ bàn */
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

/* Grid phân vùng bàn theo slot */
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
  grid-row: span 3;
  height: calc(120px * 3 + 16px * 2) !important;
  z-index: 10;
}

.ban-lon .table-header {
  justify-content: center;
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

/* Màu trạng thái bàn */
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

/* ===== SELECT 2 && TIM KIEM ===== */
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

:deep(.vs--open .vs__dropdown-toggle) {
  border-color: #3b6cff !important;
  box-shadow: 0 0 0 3px rgba(59, 108, 255, 0.1) !important;
}

:deep(.vs__dropdown-menu) {
  border: 1px solid #e5e7eb !important;
  border-radius: 10px !important;
  margin-top: 8px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1) !important;
  padding: 6px !important;
  z-index: 1000;
}

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

/* //////////// BỔ SUNG //////// */
.invalid-seat {
  opacity: 0.45;
  filter: grayscale(0.7);
}

.invalid-seat:hover {
  transform: none !important;
  box-shadow: none !important;
}

/* ===== RESPONSIVE ===== */
@media (max-width: 900px) {
  .modal-container {
    width: 96vw !important;
    max-height: 96vh !important;
    display: flex;
    flex-direction: column;
  }

  .modal-container.expanded {
    width: 98vw !important;
    height: 96vh !important;
  }

  .modal-main-layout {
    flex-direction: column !important;
    overflow-y: auto !important;
  }

  .selected-sidebar {
    width: 100% !important;
    border-left: none !important;
    border-top: 1px solid #eee !important;
  }

  .map-container {
    padding: 15px !important;
    overflow-x: auto !important;
    display: block !important;
  }

  .filter-bar {
    flex-direction: column !important;
    align-items: stretch !important;
    gap: 10px !important;
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

  .info-grid {
    gap: 15px !important;
  }

  .modal-footer {
    flex-wrap: wrap !important;
    gap: 10px !important;
    padding: 15px !important;
  }

  .btn-modern {
    padding: 10px 20px !important;
    font-size: 12px !important;
  }
}

@media (max-width: 480px) {
  .info-grid {
    grid-template-columns: 1fr 1fr !important;
    gap: 10px !important;
  }

  .value-text {
    font-size: 12px !important;
  }

  .card-body label {
    font-size: 14px !important;
  }

  .grid-ban {
    grid-template-columns: repeat(2, 1fr) !important;
  }
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
