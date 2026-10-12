<script setup>
import { computed, ref, onMounted, onUnmounted, watch } from "vue";
import { huyMonKhoiHoaDon } from "@/api/admin/checkin/checkinapi";
import iconDonHang from "@/assets/icons/clipboard.png";
import { useUtils } from "@/utils/helper";
import IconDDM from "@/assets/icons/danh_sach_mon.png";
import ChonMonAn from "./ChonMonAn.vue";
import { getThamSoInt } from "@/api/admin/thamsodong/thamSoDongApi.js";

const showChonMon = ref(false);
const activeTabMon = ref("cho");
const expandedCategoriesCho = ref({});
const expandedCategoriesDaLen = ref({});
const expandedCategoriesHuy = ref({});

const { notify } = useUtils();

/////////////////// HỦY MÓN ////////////////////
const xuLyHuyMon = async (mon) => {
  if (!mon?.id) {
    notify.error("Không tìm thấy món cần hủy");
    return;
  }

  const idHoaDon = props.hoaDon?.idHoaDon || props.hoaDon?.id;

  if (!idHoaDon) {
    notify.error("Không tìm thấy hóa đơn");
    return;
  }

  const xacNhan = await notify.confirm(
    `Bạn có chắc chắn muốn hủy món "${mon.tenSanPham}" không?`,
  );

  if (!xacNhan) {
    return;
  }

  try {
    const response = await huyMonKhoiHoaDon(
      idHoaDon,
      mon.id,
      mon.tenBanGoi || tenBan.value,
      props.hoaDon?.idBanAn || null,
    );

    emit("update-hoa-don", response.data);

    notify.success("Hủy món thành công");
  } catch (e) {
    notify.error(
      "Lỗi khi hủy món: " + (e.response?.data?.message || e.message),
    );
  }
};

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  hoaDon: {
    type: Object,
    default: () => ({}),
  },
  dsMon: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits([
  "close",
  "back",
  "thanh-toan",
  "ap-dung-uu-dai",
  "xoa-mon",
  "da-len-mon",
  "da-len-tat-ca",
  "them-mon",
  "update-hoa-don",
]);

const thoiGianSuDungBanMacDinh = ref(120);

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

const formatDateTime = (value) => {
  if (!value) return "--";
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleString("vi-VN");
};

const getTrangThaiText = (status) => {
  if (status === 1) return "Trống";
  if (status === 2) return "Đã đặt";
  if (status === 3) return "Đang phục vụ";
  if (status === 4) return "Đang dọn";
  return "Không xác định";
};

const hienThiViTriBan = computed(() => {
  const kv = props.hoaDon?.tenKhuVuc || props.hoaDon?.khuVuc?.tenKhuVuc || "";

  if (props.hoaDon?.listTenBan?.length) {
    return `${kv} -- Bàn ${props.hoaDon.listTenBan.join(", ")}`;
  }

  if (props.hoaDon?.listIdBan?.length) {
    return `${kv} -- Bàn ${props.hoaDon.listIdBan.join(", ")}`;
  }

  const tenBanDon =
    props.hoaDon?.tenBanAn || props.hoaDon?.tenBan || props.hoaDon?.maBanAn;
  if (tenBanDon) {
    return `${kv} -- Bàn ${tenBanDon}`;
  }

  return kv || "Chưa xác định";
});

const tenBan = computed(() => {
  return (
    props.hoaDon?.tenBanAn ||
    props.hoaDon?.tenBan ||
    props.hoaDon?.maBanAn ||
    ""
  );
});

const laBanGhep = computed(() => {
  return (
    Array.isArray(props.hoaDon?.listIdBan) && props.hoaDon.listIdBan.length > 1
  );
});

const maHoaDon = computed(() => {
  return props.hoaDon?.maHoaDon || "--";
});

const maDatBan = computed(() => props.hoaDon?.maDatBan || "--");
const tenKhach = computed(() => props.hoaDon?.tenKhachHang || "Khách vãng lai");
const soDienThoai = computed(() => props.hoaDon?.soDienThoai || "--");
const khuVuc = computed(
  () => props.hoaDon?.tenKhuVuc || props.hoaDon?.khuVuc?.tenKhuVuc || "--",
);
const sucChua = computed(
  () =>
    props.hoaDon?.sucChuaToiDa ||
    props.hoaDon?.soNguoi ||
    props.hoaDon?.ghe ||
    "--",
);
const soNguoiDangDung = computed(() => props.hoaDon?.soNguoiDangDung || 0);
const soKhachDat = computed(
  () => props.hoaDon?.soNguoiDat || props.hoaDon?.soNguoiToiDa || "--",
);

const trangThaiBan = computed(
  () => props.hoaDon?.trangThaiBan ?? props.hoaDon?.trangThai,
);

const gioCheckIn = computed(() => {
  return (
    props.hoaDon?.thoiGianCheckIn ||
    props.hoaDon?.thoiGianMo ||
    props.hoaDon?.thoiGianDat ||
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
  if (Number(trangThaiBan.value) !== 3) return null;
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

const ngayTaoHoaDon = computed(() => {
  return props.hoaDon?.thoiGianTaoHoaDon || props.hoaDon?.thoiGianTao || null;
});

const dsMonCho = computed(() => {
  return props.dsMon.filter((m) => (m.trangThai ?? 0) === 0);
});

const dsMonDaLen = computed(() => {
  return props.dsMon.filter((m) => m.trangThai === 1);
});
const dsMonHuy = computed(() => {
  return props.dsMon.filter((m) => m.trangThai === 2);
});

const columnGridStyle = computed(() => {
  const hasHuy = dsMonHuy.value.length > 0;
  return {
    display: "grid",
    gridTemplateColumns: hasHuy ? "1fr 1fr 1fr" : "1fr 1fr",
    gap: "20px",
  };
});

const searchMonKeyword = ref("");

const isPriority = (m) => {
  const name = (m.tenSanPham || m.tenMon || "").toLowerCase();
  const cat = (m.tenDanhMuc || "").toLowerCase();
  return (
    name.includes("sale") ||
    cat.includes("lẻ") ||
    cat.includes("lẩu") ||
    cat.includes("set")
  );
};

const dsMonChoGrouped = computed(() => {
  const grouped = {};
  let list = dsMonCho.value;
  if (searchMonKeyword.value) {
    const kw = searchMonKeyword.value.toLowerCase().trim();
    list = list.filter((m) =>
      (m.tenSanPham || m.tenMon || "").toLowerCase().includes(kw),
    );
  }
  list
    .sort((a, b) => (isPriority(b) ? 1 : 0) - (isPriority(a) ? 1 : 0))
    .forEach((mon) => {
      const dm = mon.tenDanhMuc || "Khác";
      if (!grouped[dm]) grouped[dm] = [];
      grouped[dm].push(mon);
    });
  return grouped;
});

const dsMonChoFiltered = computed(() => {
  let count = 0;
  Object.values(dsMonChoGrouped.value).forEach(
    (list) => (count += list.length),
  );
  return { length: count };
});

const dsMonDaLenGrouped = computed(() => {
  const grouped = {};
  let list = dsMonDaLen.value;
  if (searchMonKeyword.value) {
    const kw = searchMonKeyword.value.toLowerCase().trim();
    list = list.filter((m) =>
      (m.tenSanPham || m.tenMon || "").toLowerCase().includes(kw),
    );
  }
  list
    .sort((a, b) => (isPriority(b) ? 1 : 0) - (isPriority(a) ? 1 : 0))
    .forEach((mon) => {
      const dm = mon.tenDanhMuc || "Khác";
      if (!grouped[dm]) grouped[dm] = [];
      grouped[dm].push(mon);
    });
  return grouped;
});

const dsMonDaLenFiltered = computed(() => {
  let count = 0;
  Object.values(dsMonDaLenGrouped.value).forEach(
    (list) => (count += list.length),
  );
  return { length: count };
});

const dsMonHuyGrouped = computed(() => {
  const grouped = {};
  let list = dsMonHuy.value;
  if (searchMonKeyword.value) {
    const kw = searchMonKeyword.value.toLowerCase().trim();
    list = list.filter((m) =>
      (m.tenSanPham || m.tenMon || "").toLowerCase().includes(kw),
    );
  }
  list
    .sort((a, b) => (isPriority(b) ? 1 : 0) - (isPriority(a) ? 1 : 0))
    .forEach((mon) => {
      const dm = mon.tenDanhMuc || "Khác";
      if (!grouped[dm]) grouped[dm] = [];
      grouped[dm].push(mon);
    });
  return grouped;
});

const dsMonHuyFiltered = computed(() => {
  let count = 0;
  Object.values(dsMonHuyGrouped.value).forEach(
    (list) => (count += list.length),
  );
  return { length: count };
});

const tienCoc = computed(() => toMoney(props.hoaDon?.tienCoc || 0));
const tongTienBackend = computed(() => toMoney(props.hoaDon?.tongTien || 0));

const tongTienMonInt = computed(() => {
  // Tính cả món đang chờ (trangThai 0) và món đã lên (trangThai 1),
  // chỉ loại trừ món đã hủy (trangThai 2) — khớp với cách backend tính tongTienMon
  return props.dsMon
    .filter((m) => (m.trangThai ?? 0) !== 2)
    .reduce((sum, mon) => {
      if (mon.thanhTien != null) return sum + Number(mon.thanhTien);

      const soLuong = Number(mon.soLuong || mon.soLuongChon || 0);
      const donGia = Number(mon.donGia || mon.gia || 0);
      return sum + soLuong * donGia;
    }, 0);
});

const tongTienMon = computed(() => toMoney(tongTienMonInt.value));

const daLenHetMon = computed(() => {
  return props.dsMon.length > 0 && dsMonCho.value.length === 0;
});

const layAnhMon = (img) => {
  if (!img) return null;

  if (typeof img === "string") {
    if (img.startsWith("http")) return img;
    return `http://localhost:8080/uploads/${img}`;
  }

  return null;
};

function onImgError(event) {
  event.target.src = "/default-food.png";
}

function xuLyDaLenMon(mon) {
  if (mon.trangThai === 1) return;
  emit("da-len-mon", { ...mon, tenBanGoi: mon.tenBanGoi || tenBan.value });
}

function xuLyDaLenTatCa() {
  if (!dsMonCho.value.length) return;
  emit("da-len-tat-ca", tenBan.value);
}

const diDenThanhToan = () => {
  if (!daLenHetMon.value) {
    notify.error("Phải lên hết món mới được thanh toán");
    return;
  }
  if (!props.hoaDon?.idHoaDon && !props.hoaDon?.id) {
    notify.error("Không tìm thấy hóa đơn để thanh toán");
    return;
  }
  emit("thanh-toan", {});
};

function xuLyThemMon() {
  emit("them-mon", tenBan.value);
}

const loadThamSo = async () => {
  try {
    const res = await getThamSoInt("THOI_GIAN_SU_DUNG_BAN_MAC_DINH");
    if (res?.data) thoiGianSuDungBanMacDinh.value = Number(res.data);
  } catch (e) {}
};

onMounted(() => {
  loadThamSo();
});
</script>

<template>
  <div v-if="show" class="xem-don-overlay" @click.self="emit('close')">
    <div class="xem-don-modal">
      <div class="modal-header">
        <h2 style="display: flex; align-items: center; gap: 12px; margin: 0">
          <span class="icon-wrapper">
            <img :src="iconDonHang" alt="Order Icon" />
          </span>
          Xem đơn hàng - Bàn {{ tenBan }}
        </h2>
        <button class="btn btn-icon btn-close" @click="emit('close')">✕</button>
      </div>

      <div class="modal-body split-layout">
        <!-- CỘT TRÁI: THÔNG TIN HÓA ĐƠN VÀ THANH TOÁN (Tỷ lệ 3) -->
        <div class="left-col">
          <div class="info-grid full-info">
            <div class="info-item">
              <div class="label">
                <i class="ri-receipt-line me-1"></i> Mã hóa đơn
              </div>
              <div class="value">#{{ maHoaDon }}</div>
            </div>

            <div class="info-item" v-if="gioCheckIn">
              <div class="label"><i class="ri-time-line me-1"></i> Giờ vào</div>
              <div class="value">{{ formatDateTime(gioCheckIn) }}</div>
            </div>

            <div class="info-item" v-if="gioRaDuKien">
              <div class="label">
                <i class="ri-calendar-check-line me-1"></i> Dự kiến xong
              </div>
              <div class="value">{{ formatDateTime(gioRaDuKien) }}</div>
            </div>

            <div
              class="info-item dang-su-dung-text"
              v-if="thoiGianSuDungBanText"
            >
              <div class="label">
                <i class="ri-timer-flash-line"></i> Thời gian đang sử dụng
              </div>
              <div class="value" style="color: #d16969">
                {{ thoiGianSuDungBanText }}
              </div>
            </div>

            <div class="info-item">
              <div class="label">
                <i class="ri-checkbox-circle-line me-1"></i> Trạng thái
              </div>
              <div class="value" style="display: flex; align-items: flex-start">
                <span
                  class="badge-status"
                  :class="
                    Number(trangThaiBan) === 3
                      ? 'status-serving'
                      : 'status-confirmed'
                  "
                >
                  {{
                    Number(trangThaiBan) === 3 ? "ĐANG PHỤC VỤ" : "ĐÃ ĐẶT CHỖ"
                  }}
                </span>
              </div>
            </div>

            <div class="info-item" v-if="tenKhach">
              <div class="label">
                <i class="ri-user-line me-1"></i> Khách hàng
              </div>
              <div class="value">{{ tenKhach }}</div>
            </div>

            <div class="info-item" v-if="soDienThoai">
              <div class="label">
                <i class="ri-phone-line me-1"></i> Số điện thoại
              </div>
              <div class="value">{{ soDienThoai }}</div>
            </div>

            <div class="info-item">
              <div class="label">
                <i class="ri-map-pin-2-line me-1"></i> Vị trí bàn
              </div>
              <div class="value vi-tri-ban">
                {{ hienThiViTriBan }}
                <span v-if="laBanGhep" class="badge-ban-ghep">
                  <i class="ri-links-line"></i>
                  Bàn ghép
                </span>
              </div>
            </div>

            <div class="info-item">
              <div class="label">
                <i class="ri-group-fill me-1"></i> Số người ngồi trong bàn
              </div>
              <div class="value">
                {{ soNguoiDangDung }} người / {{ sucChua }} chỗ
              </div>
            </div>
          </div>
        </div>

        <div class="right-col">
          <!-- CỘT PHẢI: MÓN ĂN VÀ PHIẾU GIẢM GIÁ (Tỷ lệ 2) -->
          <div class="danh-sach-mon-section">
            <div class="section-head">
              <div class="section-title">
                <img :src="IconDDM" alt="Danh sách món " /> Danh sách món
              </div>

              <div class="head-actions-group">
                <button
                  class="btn-primary btn-len-tat-ca"
                  @click="xuLyThemMon"
                >
                  <div class="btn-light-sweep"></div>
                  <i class="ri-add-line me-1"></i>
                  <span>Thêm món</span>
                </button>

                <button
                  v-if="dsMonCho.length > 0"
                  class="btn-primary btn-len-tat-ca"
                  @click="xuLyDaLenTatCa"
                >
                  <div class="btn-light-sweep"></div>
                  <i class="ri-check-double-fill me-1"></i>
                  <span>Lên tất cả món</span>
                </button>
              </div>
            </div>

            <!-- Tìm kiếm món -->
            <div class="search-mon-container" v-if="dsMon.length > 0">
              <i class="ri-search-line"></i>
              <input
                type="text"
                v-model="searchMonKeyword"
                placeholder="Tìm món đã chọn..."
                class="search-mon-input"
              />
            </div>

            <div class="two-column-lists" :style="columnGridStyle">
              <!-- CỘT 1: ĐANG CHỜ PHỤC VỤ -->
              <div
                class="col-cho"
                style="
                  border: 1px solid #fecaca;
                  border-radius: 8px;
                  padding: 16px;
                "
              >
                <div
                  class="col-title"
                  style="
                    font-weight: 700;
                    color: #dc2626;
                    margin-bottom: 12px;
                    padding: 10px;
                    background: #fef2f2;
                    border-radius: 6px;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                  "
                >
                  <span>Đang chờ phục vụ</span>
                  <span class="col-count-badge" style="background: #dc2626">{{
                    dsMonChoFiltered.length
                  }}</span>
                </div>
                <div class="list-mon-scrollable">
                  <div
                    v-if="dsMonChoFiltered.length"
                    class="category-group-container"
                  >
                    <div
                      v-for="(monList, dm) in dsMonChoGrouped"
                      :key="dm"
                      class="selected-category-group"
                    >
                      <div
                        class="category-header"
                        @click="
                          expandedCategoriesCho[dm] =
                            expandedCategoriesCho[dm] === false ? true : false
                        "
                        style="
                          cursor: pointer;
                          display: flex;
                          justify-content: space-between;
                          align-items: center;
                        "
                      >
                        <span class="category-header-content">
                          <span>{{ dm }}</span>
                          <span class="category-count-badge">{{
                            monList.length
                          }}</span>
                        </span>
                        <i
                          class="ri-arrow-down-s-line"
                          :style="{
                            transform:
                              expandedCategoriesCho[dm] !== false
                                ? 'rotate(180deg)'
                                : 'rotate(0deg)',
                            transition: 'transform 0.3s',
                          }"
                        ></i>
                      </div>
                      <div
                        class="ds-mon"
                        v-show="expandedCategoriesCho[dm] !== false"
                      >
                        <div
                          v-for="(mon, index) in monList"
                          :key="`cho-${mon.id || index}`"
                          class="mon-card"
                        >
                          <div class="mon-left">
                            <div class="mon-icon">
                              <img
                                :src="layAnhMon(mon.urlAnh || mon.hinhAnh)"
                                alt="ảnh món"
                                @error="onImgError"
                              />
                            </div>

                            <div class="mon-content">
                              <div class="ten-mon">
                                {{ mon.tenSanPham || mon.tenMon }}
                              </div>

                              <div class="so-luong">
                                SL: {{ mon.soLuong || mon.soLuongChon || 1 }}
                                <span class="don-gia-inline">
                                  |
                                  <template
                                    v-if="
                                      Number(mon.giaSauGiam || 0) > 0 &&
                                      Number(mon.giaSauGiam || 0) <
                                        Number(mon.giaGoc || 0)
                                    "
                                  >
                                    <span class="gia-goc">{{
                                      formatTien(mon.giaGoc)
                                    }}</span>
                                    <span class="gia-giam">{{
                                      formatTien(mon.giaSauGiam)
                                    }}</span>
                                  </template>
                                  <template v-else>
                                    {{ formatTien(mon.donGia || mon.gia || 0) }}
                                  </template>
                                </span>
                              </div>
                            </div>
                          </div>

                          <div class="mon-right">
                            <button
                              class="btn btn-primary btn-da-len-mon"
                              :disabled="mon.trangThai === 1"
                              @click="xuLyDaLenMon(mon)"
                              title="Lên món"
                              style="
                                width: 34px;
                                height: 34px;
                                padding: 0;
                                display: flex;
                                align-items: center;
                                justify-content: center;
                                border-radius: 7px;
                                flex-shrink: 0;
                              "
                            >
                              <div class="btn-light-sweep"></div>
                              <i
                                class="ri-check-double-fill"
                                style="font-size: 17px"
                              ></i>
                            </button>
                            <button
                              class="btn"
                              title="Hủy món"
                              @click="xuLyHuyMon(mon)"
                              style="
                                width: 34px;
                                height: 34px;
                                padding: 0;
                                display: flex;
                                align-items: center;
                                justify-content: center;
                                border-radius: 7px;
                                flex-shrink: 0;
                                background: #fef2f2;
                                border: 1px solid #fecaca;
                                color: #dc2626;
                              "
                            >
                              <i
                                class="ri-close-line"
                                style="font-size: 16px"
                              ></i>
                            </button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div
                    v-else-if="daLenHetMon && !searchMonKeyword"
                    class="empty-mon done-box"
                  >
                    <i class="ri-check-line"></i> Bàn này đã lên đủ món!
                  </div>
                  <div v-else class="empty-mon">
                    Không tìm thấy món chờ phục vụ
                  </div>
                </div>
              </div>

              <!-- CỘT 2: ĐÃ PHỤC VỤ -->
              <div
                class="col-dalen"
                style="
                  border: 1px solid #bbf7d0;
                  border-radius: 8px;
                  padding: 16px;
                "
              >
                <div
                  class="col-title"
                  style="
                    font-weight: 700;
                    color: #16a34a;
                    margin-bottom: 12px;
                    padding: 10px;
                    background: #f0fdf4;
                    border-radius: 6px;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                  "
                >
                  <span>Đã phục vụ</span>
                  <span class="col-count-badge" style="background: #16a34a">{{
                    dsMonDaLenFiltered.length
                  }}</span>
                </div>
                <div class="list-mon-scrollable">
                  <div
                    v-if="dsMonDaLenFiltered.length"
                    class="category-group-container da-phuc-vu-list"
                  >
                    <div
                      v-for="(monList, dm) in dsMonDaLenGrouped"
                      :key="dm"
                      class="selected-category-group"
                    >
                      <div
                        class="category-header"
                        @click="
                          expandedCategoriesDaLen[dm] =
                            expandedCategoriesDaLen[dm] === false ? true : false
                        "
                        style="
                          cursor: pointer;
                          display: flex;
                          justify-content: space-between;
                          align-items: center;
                        "
                      >
                        <span class="category-header-content">
                          <span>{{ dm }}</span>
                          <span class="category-count-badge">{{
                            monList.length
                          }}</span>
                        </span>
                        <i
                          class="ri-arrow-down-s-line"
                          :style="{
                            transform:
                              expandedCategoriesDaLen[dm] !== false
                                ? 'rotate(180deg)'
                                : 'rotate(0deg)',
                            transition: 'transform 0.3s',
                          }"
                        ></i>
                      </div>
                      <div
                        class="ds-mon"
                        v-show="expandedCategoriesDaLen[dm] !== false"
                      >
                        <div
                          v-for="(mon, index) in monList"
                          :key="`done-${mon.id || index}`"
                          class="mon-card mon-card-done"
                        >
                          <div class="mon-left">
                            <div class="mon-icon done has-image">
                              <img
                                :src="layAnhMon(mon.urlAnh || mon.hinhAnh)"
                                alt="ảnh món"
                                @error="onImgError"
                              />
                            </div>

                            <div class="mon-content">
                              <div class="ten-mon da-len-text">
                                {{ mon.tenSanPham || mon.tenMon }}
                              </div>

                              <div class="so-luong da-len-sub">
                                SL: {{ mon.soLuong || mon.soLuongChon || 1 }}
                                <span class="don-gia-inline">
                                  |
                                  <template
                                    v-if="
                                      Number(mon.giaSauGiam || 0) > 0 &&
                                      Number(mon.giaSauGiam || 0) <
                                        Number(mon.giaGoc || 0)
                                    "
                                  >
                                    <span class="gia-goc">{{
                                      formatTien(mon.giaGoc)
                                    }}</span>
                                    <span class="gia-giam">{{
                                      formatTien(mon.giaSauGiam)
                                    }}</span>
                                  </template>
                                  <template v-else>
                                    {{ formatTien(mon.donGia || mon.gia || 0) }}
                                  </template>
                                </span>
                              </div>
                            </div>
                          </div>

                          <div class="mon-right done-right">
                            <span class="done-label">Hoàn tất</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div v-else class="empty-mon">
                    Không có món nào đã phục vụ
                  </div>
                </div>
              </div>

              <!-- CỘT 3: MÓN ĐÃ HỦY -->
              <div
                v-if="dsMonHuy.length > 0"
                class="col-huy"
                style="
                  border: 1px solid #cbd5e1;
                  border-radius: 8px;
                  padding: 16px;
                  background: #f8fafc;
                "
              >
                <div
                  class="col-title"
                  style="
                    font-weight: 700;
                    color: #64748b;
                    margin-bottom: 12px;
                    padding: 10px;
                    background: #f1f5f9;
                    border-radius: 6px;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                  "
                >
                  <span>Đã hủy</span>
                  <span class="col-count-badge" style="background: #64748b">{{
                    dsMonHuyFiltered.length
                  }}</span>
                </div>
                <div class="list-mon-scrollable">
                  <div
                    v-if="dsMonHuyFiltered.length"
                    class="category-group-container"
                  >
                    <div
                      v-for="(monList, dm) in dsMonHuyGrouped"
                      :key="dm"
                      class="selected-category-group"
                    >
                      <div
                        class="category-header"
                        @click="
                          expandedCategoriesHuy[dm] =
                            expandedCategoriesHuy[dm] === false ? true : false
                        "
                        style="
                          cursor: pointer;
                          display: flex;
                          justify-content: space-between;
                          align-items: center;
                        "
                      >
                        <span class="category-header-content">
                          <span>{{ dm }}</span>
                          <span class="category-count-badge">{{
                            monList.length
                          }}</span>
                        </span>
                        <i
                          class="ri-arrow-down-s-line"
                          :style="{
                            transform:
                              expandedCategoriesHuy[dm] !== false
                                ? 'rotate(180deg)'
                                : 'rotate(0deg)',
                            transition: 'transform 0.3s',
                          }"
                        ></i>
                      </div>
                      <div
                        class="ds-mon"
                        v-show="expandedCategoriesHuy[dm] !== false"
                      >
                        <div
                          v-for="(mon, index) in monList"
                          :key="`huy-${mon.id || index}`"
                          class="mon-card mon-card-done"
                          style="opacity: 0.7; border: 1px dashed #cbd5e1"
                        >
                          <div class="mon-left">
                            <div
                              class="mon-icon done has-image"
                              style="filter: grayscale(1)"
                            >
                              <img
                                :src="layAnhMon(mon.urlAnh || mon.hinhAnh)"
                                alt="ảnh món"
                                @error="onImgError"
                              />
                            </div>

                            <div class="mon-content">
                              <div
                                class="ten-mon da-len-text"
                                style="
                                  color: #64748b;
                                  text-decoration: line-through;
                                "
                              >
                                {{ mon.tenSanPham || mon.tenMon }}
                              </div>

                              <div
                                class="so-luong da-len-sub"
                                style="color: #94a3b8"
                              >
                                SL: {{ mon.soLuong || mon.soLuongChon || 1 }}
                                <span class="don-gia-inline">
                                  |
                                  <template
                                    v-if="
                                      Number(mon.giaSauGiam || 0) > 0 &&
                                      Number(mon.giaSauGiam || 0) <
                                        Number(mon.giaGoc || 0)
                                    "
                                  >
                                    <span class="gia-goc">{{
                                      formatTien(mon.giaGoc)
                                    }}</span>
                                    <span class="gia-giam">{{
                                      formatTien(mon.giaSauGiam)
                                    }}</span>
                                  </template>
                                  <template v-else>
                                    {{ formatTien(mon.donGia || mon.gia || 0) }}
                                  </template>
                                </span>
                              </div>
                            </div>
                          </div>

                          <div class="mon-right done-right">
                            <span class="huy-label">Đã hủy</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div v-else class="empty-mon">Không có món nào đã hủy</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <ChonMonAn
          v-if="showChonMon"
          :show="showChonMon"
          :ban="props.hoaDon"
          @close="showChonMon = false"
          @added="$emit('update-hoa-don')"
          @xem-don-hang="showChonMon = false"
        />
      </div>

      <!-- FOOTER Ở DƯỚI CÙNG -->
      <div class="modal-footer">
        <div class="footer-note">
          <i
            class="ri-information-line"
            style="font-size: 16px; color: #dc2626"
          ></i>
          <span style="color: #dc2626; font-size: 14px; font-weight: 600">
            * Hãy đảm bảo các món ăn đã được phục vụ đầy đủ trước khi xác nhận.
          </span>
        </div>
        <div class="footer-actions">
          <button
            class="btn-cancel btn-back"
            style="
              display: flex;
              align-items: center;
              justify-content: center;
              margin: 0;
              min-height: 48px;
              border-radius: 7px;
              font-weight: 600;
              padding: 0 24px;
            "
            @click="emit('back')"
          >
            Quay lại
          </button>

          <button
            class="btn-thanh-toan"
            :class="{ 'btn-primary': daLenHetMon }"
            style="
              display: flex;
              align-items: center;
              justify-content: center;
              gap: 8px;
              margin: 0;
              min-height: 48px;
              border-radius: 7px;
              font-weight: 600;
              font-size: 15px;
              position: relative;
              overflow: hidden;
              padding: 0 32px;
            "
            :style="
              !daLenHetMon
                ? {
                    backgroundColor: '#9e9e9e',
                    borderColor: '#9e9e9e',
                    color: '#fff',
                    cursor: 'pointer',
                  }
                : {}
            "
            @click="diDenThanhToan"
            :disabled="isAutoApplying || loadingApDung"
          >
            <i
              class="ri-loader-4-line spin"
              v-if="isAutoApplying || loadingApDung"
            ></i>
            <template v-else>
              <i class="ri-check-double-line"></i>
              <span>Xác nhận</span>
            </template>
            <div class="btn-light-sweep"></div>
          </button>
        </div>
      </div>
    </div>
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

.xem-don-overlay,
.xem-don-modal,
.modal-header,
.modal-body,
.thong-tin-box,
.danh-sach-mon-section,
.khach-box,
.khach-grid,
.info-grid,
.full-info,
.ds-mon,
.mon-card,
.voucher-list,
.voucher-card,
.modal-footer,
button,
span,
div,
h2,
h3,
p {
  font-family: var(--font-family, "Inter", sans-serif);
  box-sizing: border-box;
}

/* ===== OVERLAY ===== */
.xem-don-overlay {
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* ===== MODAL ===== */
.xem-don-modal {
  width: 100%;
  height: 100%;
  max-width: none;
  max-height: none;
  overflow-y: auto;
  overflow-x: hidden;
  background: transparent;
  box-shadow: none;
  color: var(--color-text);
  border: none;
  animation: modalScaleIn 0.25s ease;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.modal-header,
.modal-footer {
  background: #ffffff;
  border-radius: 7px;
  padding: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03);
  border: 1px solid #eee;
}

@keyframes modalScaleIn {
  from {
    opacity: 0;
    transform: translateY(8px) scale(0.985);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.xem-don-modal::-webkit-scrollbar,
.short-list::-webkit-scrollbar {
  width: 8px;
}

.xem-don-modal::-webkit-scrollbar-track,
.short-list::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 999px;
}

.xem-don-modal::-webkit-scrollbar-thumb,
.short-list::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 999px;
}

.xem-don-modal::-webkit-scrollbar-thumb:hover,
.short-list::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* ===== HEADER ===== */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.modal-header h2 {
  margin: 0;
  color: var(--color-text);
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.02em;
}

/* ===== BODY ===== */
.modal-body {
  padding: 0;
}

.split-layout {
  grid-template-rows: auto auto auto;
}

.left-col,
.right-col {
  display: contents;
}

/* ===== CARD BASE ===== */
.danh-sach-mon-section,
.info-grid {
  background: rgba(255, 255, 255, 0.96);
  border-radius: 7px;
  padding: 20px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03);
}

.info-grid {
  grid-column: 1 / -1;
  grid-row: 1;
}

.danh-sach-mon-section {
  display: flex;
  flex-direction: column;
  grid-column: 1 / -1;
  grid-row: 2;
}

.list-mon-scrollable {
  overflow-y: auto;
  padding-right: 4px;
}

.list-mon-scrollable::-webkit-scrollbar {
  width: 6px;
}
.list-mon-scrollable::-webkit-scrollbar-track {
  background: transparent;
}
.list-mon-scrollable::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 999px;
}
.list-mon-scrollable::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* ===== KHÁCH BOX ===== */
.khach-box {
  background: linear-gradient(
    135deg,
    rgba(230, 90, 90, 0.1),
    rgba(230, 90, 90, 0.04)
  );
  padding: 18px 20px;
  border-radius: 7px;
  border: 1px solid rgba(230, 90, 90, 0.18);
  box-shadow: 0 4px 15px rgba(230, 90, 90, 0.08);
  position: relative;
  overflow: hidden;
}

.khach-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px 32px;
}

.khach-item .label,
.info-item .label,
.dang-su-dung-text .label {
  color: var(--color-text-muted);
  font-size: 13px;
  margin-bottom: 4px;
  font-weight: 700;
}

.khach-item .value,
.info-item .value,
.dang-su-dung-text .value {
  color: var(--color-text);
  font-size: 15px;
  font-weight: 800;
  line-height: 1.45;
  word-break: break-word;
}

.badge-status {
  background-color: #f8e7e7;
  border: 1px solid #e7b5b5;
  padding: 8px 14px;
  border-radius: 7px;
  font-size: 12px;
  font-weight: 800;
  box-shadow: 0 8px 18px rgba(230, 90, 90, 0.2);
  letter-spacing: 0.03em;
  min-width: 110px;
  text-align: center;
}

.dang-su-dung-text {
  color: #0d6efd;
  font-weight: 700;
  font-size: 14px;
}

/* ===== INFO GRID ===== */
.info-grid,
.full-info {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px 28px;
  align-items: start;
  width: 100%;
}

.info-item .value {
  display: flex;
  align-items: center;
  gap: 8px;
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
  padding: 8px 14px;
  border-radius: 7px;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.03em;
  min-width: 110px;
  text-align: center;
}

/* ===== SECTION HEAD ===== */
.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.head-actions-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-title {
  font-weight: 800;
  color: var(--color-text);
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-title img {
  width: 20px;
  height: 20px;
  object-fit: contain;
}

/* ===== TRẠNG THÁI MÓN ===== */
.trang-thai-mon {
  font-weight: 800;
  margin-bottom: 12px;
  font-size: 14px;
  padding: 10px 12px;
  border-radius: 7px;
}

.trang-thai-mon.cho {
  color: #dc2626;
  background: rgba(230, 90, 90, 0.08);
  border: 1px solid rgba(230, 90, 90, 0.14);
}

.trang-thai-mon.da-len {
  color: #16a34a;
  background: rgba(34, 197, 94, 0.08);
  border: 1px solid rgba(34, 197, 94, 0.14);
}

/* ===== TÌM KIẾM MÓN ===== */
.search-mon-container {
  position: relative;
  margin-bottom: 6px;
}

.search-mon-container i {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
}

.search-mon-input {
  width: 100%;
  padding: 10px 14px 10px 38px;
  border: 1px solid #d1d5db;
  border-radius: 7px;
  font-size: 14px;
}

.search-mon-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(230, 90, 90, 0.12);
}

/* ===== SCROLL MÓN ===== */
.list-mon-scrollable {
  height: 350px;
  overflow-y: auto;
  padding-right: 8px;
}

.list-mon-scrollable::-webkit-scrollbar {
  width: 6px;
}
.list-mon-scrollable::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 999px;
}

/* ===== CATEGORY MÓN ===== */
.category-group-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 12px;
}

.selected-category-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.category-header {
  font-size: 14px;
  font-weight: 800;
  color: #1e293b;
  padding: 6px 10px;
  background: #f1f5f9;
  border-left: 3px solid var(--color-primary);
  border-radius: 4px;
}

/* ===== DANH SÁCH MÓN ===== */
.ds-mon {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
}

.mon-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 7px;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  transition: var(--transition);
}

.mon-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 22px rgba(15, 23, 42, 0.06);
  border-color: rgba(230, 90, 90, 0.16);
}

.mon-card-done {
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  padding: 12px 14px;
  box-shadow: none;
}

.mon-left {
  display: flex;
  gap: 14px;
  align-items: center;
  flex: 1;
  margin: 0;
}

.icon-wrapper {
  width: 48px;
  height: 48px;
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

.icon-wrapper img {
  width: 24px;
  height: 24px;
  object-fit: contain;
}

.mon-icon {
  width: 58px;
  height: 58px;
  border-radius: 7px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f1f5f9;
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.mon-icon img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.25s ease;
}

.mon-card:hover .mon-icon img {
  transform: scale(1.06);
}

.mon-icon.done {
  width: 58px;
  height: 58px;
  border-radius: 7px;
  background: #f1f5f9;
  border: 2px solid #bbf7d0;
  position: relative;
  overflow: hidden;
}

.mon-content {
  flex: 1;
  min-width: 0;
}

.ten-mon {
  font-size: 16px;
  font-weight: 800;
  color: var(--color-text);
  margin-bottom: 6px;
  line-height: 1.4;
}

.da-len-text {
  text-decoration: line-through;
  color: #64748b;
}

.so-luong {
  color: #475569;
  margin-bottom: 4px;
  font-size: 14px;
  font-weight: 600;
}

.da-len-sub,
.don-gia-inline {
  color: #64748b;
}

.mon-right {
  display: flex;
  flex-direction: row;
  justify-content: flex-end;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.done-right {
  justify-content: center;
}

.done-label {
  color: #16a34a;
  font-size: 13px;
  font-weight: 800;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(34, 197, 94, 0.08);
  border: 1px solid rgba(34, 197, 94, 0.14);
}

.huy-label {
  color: #dc2626;
  font-size: 13px;
  font-weight: 800;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(220, 38, 38, 0.08);
  border: 1px solid rgba(220, 38, 38, 0.14);
}

.empty-mon {
  background: #f8fafc;
  border-radius: 7px;
  padding: 18px;
  text-align: center;
  color: #64748b;
  font-size: 14px;
  font-weight: 600;
  border: 1px dashed #e2e8f0;
}

.done-box {
  border: 1px solid #dcfce7;
  color: #166534;
  background: #f0fdf4;
}

.badge-ban-goi {
  display: inline-block;
  background-color: #d16969;
  color: #ffffff;
  font-size: 11px;
  font-weight: 800;
  padding: 2px 6px;
  border-radius: 4px;
  margin-left: 6px;
  vertical-align: middle;
}
:deep(body.dark-mode) .badge-ban-goi {
  background-color: #713f12;
  color: #fef08a;
}

.line-sep {
  height: 1px;
  background: #e5e7eb;
  margin: 24px 0 16px;
}

.note-warning {
  margin-top: 12px;
  padding: 12px 14px;
  border-radius: 7px;
  background: #fff7ed;
  border: 1px solid #fed7aa;
  color: #c2410c;
  font-size: 13px;
  font-weight: 700;
}

.voucher-search-box {
  margin-bottom: 10px;
}

.search-input-wrapper {
  position: relative;
}

.search-input-wrapper i {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
}

.voucher-search-input {
  width: 100%;
  padding: 11px 12px;
  border: 1px solid #d1d5db;
  border-radius: 7px;
  font-size: 14px;
  color: #1e293b;
  font-weight: 500;
  font-family: var(--font-family, "Inter", sans-serif);
  transition: var(--transition);
}

.voucher-search-input.has-icon {
  padding-left: 38px;
}

.voucher-search-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12);
}

.short-list {
  max-height: 360px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.short-card {
  padding: 12px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 7px;
  cursor: pointer;
  transition: var(--transition);
  background: #fff;
}

/* ===== NEW VOUCHER CARD STYLE ===== */
.voucher-card-new {
  position: relative;
  display: flex;
  flex-shrink: 0;
  align-items: flex-start;
  gap: 12px;
  background-color: #fffcf8;
  border: 1px solid #fed7aa;
  border-bottom: 2px solid #f97316;
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  overflow: hidden;
}

.voucher-card-new:hover {
  background-color: #fff7ed;
  border-color: #fdba74;
}

.voucher-card-new.active {
  border-color: #f97316;
  background-color: #fff7ed;
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.15);
}

.voucher-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #f97316;
  padding-top: 2px;
}

.voucher-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.voucher-title {
  font-size: 14.5px;
  font-weight: 600;
  color: #c2410c;
  margin-bottom: 2px;
}

.voucher-code {
  font-weight: 700;
}

.voucher-info-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #6b7280;
}

.voucher-amount {
  color: #ea580c;
  font-weight: 700;
  font-size: 14px;
}

.text-muted {
  color: #6b7280;
}

.text-date {
  color: #9ca3af;
  font-size: 12px;
  margin-top: 2px;
}

.voucher-badge-applied {
  position: absolute;
  top: 0;
  right: 0;
  background-color: #f97316;
  color: white;
  font-size: 11px;
  font-weight: bold;
  padding: 3px 8px;
  border-bottom-left-radius: 6px;
  z-index: 1;
}

/* ===== FOOTER ===== */
.modal-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  position: sticky;
  bottom: 0;
  z-index: 5;
}

.footer-note {
  display: flex;
  align-items: center;
  gap: 8px;
}

.footer-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

/* ===== IMG ===== */
img {
  width: 18px;
  height: 18px;
  object-fit: contain;
}

/* ================= BUTTON SYSTEM ================= */
.btn {
  border-radius: 7px;
  font-weight: 700;
  font-family: var(--font-family, "Inter", sans-serif);
  cursor: pointer;
  transition: transform 0.22s ease, box-shadow 0.22s ease,
    background-color 0.22s ease, color 0.22s ease, border-color 0.22s ease,
    opacity 0.22s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  outline: none;
}

.btn:hover:not(:disabled) {
  transform: translateY(-2px);
}

.btn:active:not(:disabled) {
  transform: translateY(0) scale(0.97);
}

.btn:focus-visible {
  box-shadow: 0 0 0 3px rgba(230, 90, 90, 0.14);
}

.btn:disabled,
.btn.disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.btn-cancel {
  padding: 10px 24px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  font-size: 14px;
  font-weight: 600;
  color: #334155;
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
  background: #d73737 !important;
  color: #fff !important;
  border: none !important;
  padding: 10px 24px;
  border-radius: 7px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.btn-primary:hover,
.btn-primary:focus,
.btn-primary:active {
  background: #b92b2b !important;
  color: #fff !important;
  border: none !important;
  outline: none !important;
  box-shadow: 0 4px 10px rgba(230, 90, 90, 0.3) !important;
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

.btn-outline-danger {
  border: 1px solid #d73737;
  background: #fff;
  color: #d73737;
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}
.btn-outline-danger:hover {
  background: #d73737;
  color: #fff;
}

.btn-ghost:hover:not(:disabled) {
  background: #f8fafc;
  border-color: #cbd5e1;
  color: #0f172a;
}

.btn-text-danger {
  border: none;
  background: transparent;
  color: #dc2626;
  box-shadow: none;
  padding: 0;
}

.btn-text-danger:hover:not(:disabled) {
  color: #b91c1c;
  transform: scale(1.04);
}

/* ================= SPECIFIC BUTTONS ================= */
.btn-close,
.btn-close-popup {
  background: #f3f4f6;
  color: #475569;
  width: 40px;
  height: 40px;
  border: none;
  font-size: 18px;
  border-radius: 7px;
  font-weight: 700;
  padding: 0;
}

.btn-close-popup {
  width: 38px;
  height: 38px;
}

.btn-close:hover:not(:disabled),
.btn-close-popup:hover:not(:disabled) {
  background: rgba(230, 90, 90, 0.12);
  color: var(--color-primary);
}

.btn-len-tat-ca {
  padding: 10px 14px;
  font-size: 14px;
}

.btn-da-len-mon {
  padding: 12px 14px;
  font-size: 14px;
}

.btn-xoa {
  font-size: 14px;
}

.btn-back {
  padding: 14px 10px;
  font-size: 14px;
}

.btn-thanh-toan {
  padding: 14px;
  font-size: 18px;
  font-weight: 800;
}

.btn-icon {
  box-shadow: none;
}
.gia-goc {
  text-decoration: line-through;
  color: #9ca3af;
  margin-right: 4px;
  font-size: 12px;
}

.gia-giam {
  color: var(--color-primary);
  font-weight: 700;
}

.vi-tri-ban {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
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

/* ===== DARK MODE ===== */
:deep(body.dark-mode) .xem-don-overlay {
  background: rgba(0, 0, 0, 0.68);
}

:deep(body.dark-mode) .xem-don-modal {
  background: transparent;
  border: none;
  box-shadow: none;
}

:deep(body.dark-mode) .danh-sach-mon-section,
:deep(body.dark-mode) .mon-card,
:deep(body.dark-mode) .short-card,
:deep(body.dark-mode) .info-grid,
:deep(body.dark-mode) .full-info,
:deep(body.dark-mode) .modal-header,
:deep(body.dark-mode) .category-header,
:deep(body.dark-mode) .modal-footer {
  background: #181818;
  border: 1px solid #2a2a2a;
  color: #e5e5e5;
  box-shadow: none;
}

:deep(body.dark-mode) .modal-header h2,
:deep(body.dark-mode) .category-header,
:deep(body.dark-mode) .section-title,
:deep(body.dark-mode) .ten-mon,
:deep(body.dark-mode) .khach-item .value,
:deep(body.dark-mode) .info-item .value,
:deep(body.dark-mode) .voucher-row-1 strong,
:deep(body.dark-mode) .voucher-row-2 {
  color: #fff;
}

:deep(body.dark-mode) .khach-box,
:deep(body.dark-mode) .empty-mon {
  background: #1f1f1f;
  border-color: #2a2a2a;
}

:deep(body.dark-mode) .khach-item .label,
:deep(body.dark-mode) .info-item .label,
:deep(body.dark-mode) .so-luong,
:deep(body.dark-mode) .da-len-sub,
:deep(body.dark-mode) .don-gia-inline,
:deep(body.dark-mode) .voucher-row-3,
:deep(body.dark-mode) .done-label {
  color: #bdbdbd;
}

:deep(body.dark-mode) .status-chip.status-blue {
  background: rgba(59, 130, 246, 0.18);
  color: #93c5fd;
}

:deep(body.dark-mode) .status-chip.status-red {
  background: rgba(230, 90, 90, 0.18);
  color: #ffb0b0;
}

:deep(body.dark-mode) .trang-thai-mon.cho {
  background: rgba(230, 90, 90, 0.12);
  border-color: rgba(230, 90, 90, 0.18);
  color: #ffb0b0;
}

:deep(body.dark-mode) .trang-thai-mon.da-len {
  background: rgba(34, 197, 94, 0.12);
  border-color: rgba(34, 197, 94, 0.18);
  color: #86efac;
}

:deep(body.dark-mode) .line-sep {
  background: #2a2a2a;
}

:deep(body.dark-mode) .btn-close,
:deep(body.dark-mode) .btn-close-popup,
:deep(body.dark-mode) .btn-ghost {
  background: #222;
  color: #e5e5e5;
  border-color: #2a2a2a;
}

:deep(body.dark-mode) .btn-close:hover:not(:disabled),
:deep(body.dark-mode) .btn-close-popup:hover:not(:disabled) {
  background: rgba(230, 90, 90, 0.18);
  color: #fff;
}

:deep(body.dark-mode) .btn-outline {
  background: #1f1f1f;
  border-color: rgba(230, 90, 90, 0.34);
  color: #ffb0b0;
}

:deep(body.dark-mode) .btn-outline:hover:not(:disabled) {
  color: #fff;
}

:deep(body.dark-mode) .voucher-search-input {
  background: #1f1f1f;
  border-color: #333;
  color: #fff;
}

:deep(body.dark-mode) .voucher-search-input::placeholder {
  color: #8a8a8a;
}

:deep(body.dark-mode) .short-card:hover {
  background: #202020;
}

:deep(body.dark-mode) .short-card.active {
  background: rgba(230, 90, 90, 0.12);
}

:deep(body.dark-mode) .short-card.applied {
  background: rgba(34, 197, 94, 0.12);
  border-color: rgba(34, 197, 94, 0.4);
}

:deep(body.dark-mode) .xem-don-modal::-webkit-scrollbar-track,
:deep(body.dark-mode) .short-list::-webkit-scrollbar-track {
  background: #1f1f1f;
}

:deep(body.dark-mode) .xem-don-modal::-webkit-scrollbar-thumb,
:deep(body.dark-mode) .short-list::-webkit-scrollbar-thumb {
  background: #444;
}

/* ===== RESPONSIVE ===== */
@media (max-width: 900px) {
  .khach-grid,
  .info-grid,
  .full-info {
    grid-template-columns: 1fr;
  }

  .split-layout {
    grid-template-columns: 1fr;
  }

  .modal-footer {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }

  .footer-note {
    justify-content: center;
    text-align: center;
  }

  .footer-actions {
    justify-content: stretch;
  }

  .footer-actions button {
    flex: 1;
  }
}

@media (max-width: 768px) {
  .xem-don-overlay {
    padding: 12px;
  }

  .xem-don-modal {
    border-radius: 7px;
    max-height: 96vh;
  }

  .modal-header,
  .modal-body {
    padding-left: 14px;
    padding-right: 14px;
  }

  .khach-box {
    padding-right: 18px;
    min-height: auto;
  }

  .badge-status {
    position: static;
    display: inline-flex;
    margin-top: 12px;
    width: fit-content;
  }

  .mon-card {
    flex-direction: column;
    align-items: stretch;
  }

  .mon-right {
    align-items: stretch;
  }
}
/* ===== DISCOUNT FORM ===== */
.discount-type-tabs {
  display: flex;
  background: #f1f5f9;
  border-radius: 8px;
  padding: 4px;
  margin-top: 16px;
  gap: 4px;
}
.discount-tab {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  color: #64748b;
  transition: all 0.2s ease;
  user-select: none;
}
.discount-tab:hover {
  color: var(--color-primary);
}
.discount-tab.active {
  background: #fff;
  color: var(--color-primary);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.quick-btn-list {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.quick-btn {
  border: 1px solid var(--color-border);
  background: var(--color-white);
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text);
  transition: all 0.2s ease;
  cursor: pointer;
}
.quick-btn:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}
.quick-btn.btn-max {
  border: 1px solid var(--color-primary);
  background: var(--color-primary-light, #eff6ff);
  color: var(--color-primary);
}
.quick-btn.btn-max:hover {
  background: #dbeafe;
}

/* MON TABS STYLES */
.mon-tabs-container {
  display: flex;
  background: #f8fafc;
  padding: 4px;
  border-radius: 8px;
  margin: 8px 0 12px 0;
  gap: 4px;
}
.mon-tab-item {
  flex: 1;
  text-align: center;
  padding: 8px 12px;
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s ease;
}
.mon-tab-item.active {
  background: white;
  color: #ef4444;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}
.mon-tab-item:hover:not(.active) {
  background: #f1f5f9;
  color: #334155;
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

  60% {
    transform: translate3d(4px, 0, 0);
  }
}
.diem-tich-luy-card {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 200px;
}
.diem-tich-luy-input-v2 {
  flex: 1;
  min-width: 0;
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  padding: 9px 12px;
  font-size: 13.5px;
  font-weight: 700;
  text-align: right;
  color: #0f172a;
  background: #f8fafc;
  transition: all 0.2s;
}
.diem-tich-luy-input-v2:focus {
  outline: none;
  border-color: #d73737;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(215, 55, 55, 0.1);
}
.diem-tich-luy-input-v2:disabled {
  color: #94a3b8;
  cursor: not-allowed;
}
.diem-tich-luy-btn-v2 {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: none;
  background: linear-gradient(135deg, #d73737, #b92b2b);
  color: #fff;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 3px 8px rgba(215, 55, 55, 0.25);
}
.diem-tich-luy-btn-v2:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 5px 12px rgba(215, 55, 55, 0.35);
}
.diem-tich-luy-btn-v2:disabled {
  background: #d1d5db;
  box-shadow: none;
  cursor: not-allowed;
}
/* ===== BADGE SỐ LƯỢNG Ở TIÊU ĐỀ CỘT MÓN ===== */
.col-count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 24px;
  height: 22px;
  padding: 0 7px;
  border-radius: 999px;
  color: #fff;
  font-size: 12px;
  font-weight: 800;
  line-height: 1;
}
/* ===== BADGE SỐ LƯỢNG TRONG CATEGORY-HEADER ===== */
.category-header-content {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.category-count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 19px;
  padding: 0 6px;
  border-radius: 999px;
  background: var(--color-primary, #d73737);
  color: #fff;
  font-size: 11px;
  font-weight: 800;
  line-height: 1;
}
</style>
