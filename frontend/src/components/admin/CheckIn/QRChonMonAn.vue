<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRoute } from "vue-router";

import { useUtils } from "@/utils/helper";
import {
  themNhieuMonVaoHoaDon,
} from "@/api/admin/checkin/checkinapi";
import { getAllSanPhamDotGiamGia } from "@/api/customer/thucdon/thucDonCustomerApi";

const { notify, nprogress } = useUtils();
const route = useRoute();

const thongTinQr = ref({
  idHoaDon: "",
  maHoaDon: "",
  idBanAn: "",
  tenBanAn: "",
  tenKhuVuc: "",
});

const tabDangChon = ref("SET");
const tuKhoa = ref("");
const danhMucDaChon = ref("");
const sapXepGia = ref("mac_dinh");

const danhSachMonHienThi = ref([]);
const mapSoLuong = ref({});
const mapThongTinMon = ref({});

const tongSoMonDaChon = computed(() =>
  Object.values(mapSoLuong.value).reduce(
    (sum, soLuong) => sum + Number(soLuong || 0),
    0,
  ),
);

const tongTien = computed(() =>
  Object.entries(mapSoLuong.value).reduce((sum, [id, soLuong]) => {
    const mon = mapThongTinMon.value[id];
    if (!mon) return sum;
    return sum + Number(mon.gia || 0) * Number(soLuong || 0);
  }, 0),
);

const danhSachDaChon = computed(() => {
  return Object.entries(mapSoLuong.value)
    .filter(([, soLuong]) => Number(soLuong) > 0)
    .map(([id, soLuong]) => ({
      ...mapThongTinMon.value[id],
      soLuongChon: Number(soLuong),
    }))
    .filter(Boolean);
});

const danhSachDanhMuc = computed(() => {
  const map = new Map();

  danhSachMonHienThi.value.forEach((item) => {
    const ten =
      item.danhMuc ||
      item.raw?.tenDanhMuc ||
      item.raw?.danhMuc?.tenDanhMuc ||
      (item.loai === "SET" ? "Set lẩu" : "Món phụ / Đồ uống");

    if (ten) {
      map.set(ten, ten);
    }
  });

  return ["", ...Array.from(map.values())];
});

function formatTien(value) {
  return Number(value || 0).toLocaleString("vi-VN") + "đ";
}

function layAnhMon(hinhAnh) {
  if (!hinhAnh) return null;

  if (typeof hinhAnh === "string") {
    if (hinhAnh.startsWith("http")) return hinhAnh;
    return `http://localhost:8080/uploads/${hinhAnh}`;
  }

  return null;
}

function ganSoLuongCu(dsMoi) {
  return dsMoi.map((item) => {
    const laSet =
      item.laSet === true ||
      item.loai === "SET" ||
      item.loaiSanPham === "SET" ||
      item.tenSet;

    const id = item.id ?? item.idSanPham;

    const mapped = {
      id: id,
      tenMon:
        item.tenSanPham ||
        item.tenMon ||
        item.tenSet ||
        item.ten ||
        item.name ||
        "Chưa có tên",
      loai: laSet ? "SET" : "MON",
      danhMuc:
        item.tenDanhMuc ||
        item.danhMuc?.tenDanhMuc ||
        (laSet ? "Set lẩu" : "Món phụ / Đồ uống"),
      loaiSet:
        item.tenNuocLau || item.nuocLau?.tenNuocLau || item.tenLoaiSet || "",
      giaGoc: item.gia ?? item.donGia ?? item.giaBan ?? 0,
      giaSauGiam: item.giaSauGiam ?? item.gia ?? item.donGia ?? item.giaBan ?? 0,
      soTienGiam: item.soTienGiam ?? 0,
      phanTramGiam: item.phanTramGiam ?? 0,
      gia: item.giaSauGiam ?? item.gia ?? item.donGia ?? item.giaBan ?? 0,
      hinhAnh:
        item.hinhAnh ||
        item.urlAnh ||
        item.anh ||
        item.image ||
        item.imageUrl ||
        "",
      raw: item,
      soLuongChon: mapSoLuong.value[id] || 0,
    };

    mapThongTinMon.value[mapped.id] = mapped;
    return mapped;
  });
}

async function loadDanhSachMon() {
  try {
    nprogress?.start?.();
    let allData = [];

    if (tabDangChon.value === "SET") {
      const res = await getAllSanPhamDotGiamGia(true);
      allData = Array.isArray(res?.data) ? res.data : [];
      if (allData.length === 0 && Array.isArray(res?.data?.data)) {
         allData = res.data.data;
      }
    } else {
      const res = await getAllSanPhamDotGiamGia(false);
      allData = Array.isArray(res?.data) ? res.data : [];
      if (allData.length === 0 && Array.isArray(res?.data?.data)) {
         allData = res.data.data;
      }
    }

    danhSachMonHienThi.value = ganSoLuongCu(allData);
  } catch (error) {
    console.error("Lỗi load danh sách món:", error);
    notify.error("Không tải được danh sách món");
    danhSachMonHienThi.value = [];
  } finally {
    nprogress?.done?.();
  }
}

const danhSachDaLoc = computed(() => {
  let ds = [...danhSachMonHienThi.value];

  if (tuKhoa.value?.trim()) {
    const kw = tuKhoa.value.trim().toLowerCase();
    ds = ds.filter((item) => (item.tenMon || "").toLowerCase().includes(kw));
  }

  if (danhMucDaChon.value) {
    ds = ds.filter((item) => (item.danhMuc || "") === danhMucDaChon.value);
  }

  if (sapXepGia.value === "gia_tang") {
    ds.sort((a, b) => Number(a.gia || 0) - Number(b.gia || 0));
  } else if (sapXepGia.value === "gia_giam") {
    ds.sort((a, b) => Number(b.gia || 0) - Number(a.gia || 0));
  }

  return ds;
});

function chuyenTab(tab) {
  if (tabDangChon.value === tab) return;
  tabDangChon.value = tab;
  tuKhoa.value = "";
  danhMucDaChon.value = "";
  sapXepGia.value = "mac_dinh";
  loadDanhSachMon();
}

function themMon(item) {
  const id = item.id;
  const current = mapSoLuong.value[id] || 0;
  mapSoLuong.value[id] = current + 1;
}

function giamMon(item) {
  const id = item.id;
  const current = mapSoLuong.value[id] || 0;
  mapSoLuong.value[id] = current > 0 ? current - 1 : 0;
}

function chonNhanhMon(item) {
  themMon(item);
}

async function xacNhanThemMon() {
  try {
    if (!thongTinQr.value.idHoaDon) {
      notify.error("Không tìm thấy hóa đơn");
      return;
    }

    if (!danhSachDaChon.value.length) {
      notify.error("Bạn chưa chọn món");
      return;
    }

    const requestsData = danhSachDaChon.value.map((item) => ({
      idHoaDon: thongTinQr.value.idHoaDon,
      idSanPham: item.id,
      soLuong: item.soLuongChon,
      tenBan: thongTinQr.value.tenBanAn,
      idBanAn: thongTinQr.value.idBanAn,
    }));

    await themNhieuMonVaoHoaDon(requestsData);

    notify.success("Đặt món thành công");
    mapSoLuong.value = {};
    await loadDanhSachMon();
  } catch (error) {
    console.error("Lỗi thêm món từ QR:", error);
    notify.error(error?.response?.data?.message || "Đặt món thất bại");
  }
}

function docThongTinTuUrl() {
  thongTinQr.value = {
    idHoaDon: route.query.idHoaDon || "",
    maHoaDon: route.query.maHoaDon || "",
    idBanAn: route.query.idBanAn || "",
    tenBanAn: route.query.tenBanAn || "",
    tenKhuVuc: route.query.tenKhuVuc || "",
  };
}

watch(
  () => route.query,
  () => {
    docThongTinTuUrl();
  },
  { deep: true, immediate: true },
);

watch(tabDangChon, () => {
  loadDanhSachMon();
});

onMounted(async () => {
  docThongTinTuUrl();
  await loadDanhSachMon();
});
</script>

<template>
  <div class="qr-page">
    <div class="qr-layout">
      <div class="left-area">
        <div class="page-header">
          <div>
            <h1>Chọn món</h1>
            <p class="sub">
              Bàn:
              <strong>{{ thongTinQr.tenBanAn || "Chưa có bàn" }}</strong>
              <span v-if="thongTinQr.tenKhuVuc">
                - {{ thongTinQr.tenKhuVuc }}
              </span>
            </p>
            <p class="sub">
              Mã hóa đơn:
              <strong>{{
                thongTinQr.maHoaDon || thongTinQr.idHoaDon || "--"
              }}</strong>
            </p>
          </div>
        </div>

        <div class="tab-wrap">
          <button
            class="tab-btn"
            :class="{ active: tabDangChon === 'SET' }"
            @click="chuyenTab('SET')"
          >
            Set lẩu
          </button>
          <button
            class="tab-btn"
            :class="{ active: tabDangChon === 'MON' }"
            @click="chuyenTab('MON')"
          >
            Món phụ / Đồ uống
          </button>
        </div>

        <div class="toolbar">
          <div class="field">
            <label>Tìm sản phẩm</label>
            <input
              v-model="tuKhoa"
              type="text"
              placeholder="Tên sản phẩm..."
              @input="loadDanhSachMon"
            />
          </div>

          <div class="field">
            <label>Danh mục</label>
            <select v-model="danhMucDaChon">
              <option value="">Tất cả danh mục</option>
              <option
                v-for="dm in danhSachDanhMuc.filter(Boolean)"
                :key="dm"
                :value="dm"
              >
                {{ dm }}
              </option>
            </select>
          </div>

          <div class="field">
            <label>Sắp xếp giá</label>
            <select v-model="sapXepGia">
              <option value="mac_dinh">Mặc định</option>
              <option value="gia_tang">Giá tăng dần</option>
              <option value="gia_giam">Giá giảm dần</option>
            </select>
          </div>
        </div>

        <div class="menu-grid">
          <div
            v-for="item in danhSachDaLoc"
            :key="item.id"
            class="menu-card"
            @click="chonNhanhMon(item)"
          >
            <div class="thumb-wrap">
              <img
                v-if="layAnhMon(item.hinhAnh)"
                :src="layAnhMon(item.hinhAnh)"
                alt="Ảnh món"
                class="thumb"
              />
              <div v-else class="thumb placeholder">IMG</div>
            </div>

            <div class="menu-body">
              <div class="menu-name">{{ item.tenMon }}</div>
              
              <div class="mon-meta-line" style="display: flex; align-items: center; justify-content: center; gap: 8px; flex-wrap: wrap; margin-bottom: 12px;">
                <template v-if="Number(item.giaSauGiam || 0) < Number(item.giaGoc || 0)">
                  <span class="gia-goc" style="text-decoration: line-through; color: #9ca3af; font-size: 14px;">
                    {{ formatTien(item.giaGoc) }}
                  </span>
                  <span class="gia-giam" style="color: #991b1b; font-weight: 700; font-size: 16px;">
                    {{ formatTien(item.giaSauGiam) }}
                  </span>
                  <span v-if="item.soTienGiam > 0" class="badge-giam" style="background: #fee2e2; color: #dc2626; font-size: 12px; font-weight: 700; padding: 2px 6px; border-radius: 4px;">
                    -{{ formatTien(item.soTienGiam) }}
                  </span>
                </template>
                <template v-else>
                  <span class="gia-thuong" style="color: #991b1b; font-weight: 700; font-size: 16px;">
                    {{ formatTien(item.gia) }}
                  </span>
                </template>
              </div>

              <div class="counter" @click.stop>
                <button class="btn-count" @click="giamMon(item)">-</button>
                <span>{{ mapSoLuong[item.id] || 0 }}</span>
                <button class="btn-count" @click="themMon(item)">+</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="right-area">
        <div class="summary-title">Món đã chọn</div>

        <div v-if="danhSachDaChon.length === 0" class="empty">
          Chưa chọn món nào
        </div>

        <div v-else class="selected-list">
          <div
            v-for="item in danhSachDaChon"
            :key="item.id"
            class="selected-item"
          >
            <div class="selected-name">{{ item.tenMon }}</div>
            <div class="selected-meta">
              {{ item.soLuongChon }} x {{ formatTien(item.gia) }}
            </div>
            <div class="selected-total">
              {{ formatTien(item.soLuongChon * item.gia) }}
            </div>
          </div>
        </div>

        <div class="summary-footer">
          <div class="sum-row">
            <span>Đã chọn:</span>
            <strong>{{ tongSoMonDaChon }} món</strong>
          </div>
          <div class="sum-row total">
            <span>Tổng tiền:</span>
            <strong>{{ formatTien(tongTien) }}</strong>
          </div>

          <button class="btn btn-confirm-full" @click="xacNhanThemMon">
            Xác nhận đặt món
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.qr-page {
  min-height: 100vh;
  background: #f5f6f8;
  padding: 20px;
  font-family: Inter, sans-serif;
  color: #2c2c2c;
}

.qr-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 20px;
  align-items: start;
  width: 100%;
  max-width: 1440px;
  margin: 0 auto;
}

.left-area,
.right-area {
  background: #fff;
  border-radius: 7px;
  border: 1px solid #ececec;
  overflow: hidden;
}

.page-header {
  padding: 24px 24px 0 24px;
}

.page-header h1 {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 700;
}

.sub {
  margin: 4px 0;
  color: #6b7280;
  font-size: 14px;
}

.tab-wrap {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin: 20px 24px 0;
  background: #eef1f5;
  border-radius: 999px;
  padding: 10px;
}

.tab-btn {
  height: 42px;
  border: none;
  border-radius: 999px;
  background: transparent;
  font-weight: 700;
  cursor: pointer;
  color: #64748b;
}

.tab-btn.active {
  background: #b91c1c;
  color: #fff;
}

.toolbar {
  display: grid;
  grid-template-columns: 1.4fr 1fr 1fr;
  gap: 16px;
  padding: 20px 24px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field label {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

.field input,
.field select {
  height: 44px;
  border: 1px solid #dcdfe4;
  border-radius: 7px;
  padding: 0 14px;
  outline: none;
  font-size: 14px;
  background: #fff;
}

.menu-grid {
  padding: 0 24px 24px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
  max-height: calc(100vh - 220px);
  overflow-y: auto;
}

.menu-card {
  border: 1px solid #e5e7eb;
  border-radius: 7px;
  overflow: hidden;
  background: #fff;
  cursor: pointer;
  transition: 0.2s ease;
}

.menu-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.06);
}

.thumb-wrap {
  height: 220px;
  background: #eef1f5;
}

.thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb.placeholder {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  color: #94a3b8;
  font-weight: 700;
}

.menu-body {
  padding: 14px;
  text-align: center;
}

.menu-name {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}

.menu-price {
  color: #991b1b;
  font-weight: 700;
  margin-bottom: 12px;
}

.counter {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.btn-count {
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 7px;
  background: #f1f5f9;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.right-area {
  position: sticky;
  top: 20px;
}

.summary-title {
  padding: 20px;
  font-size: 18px;
  font-weight: 700;
  border-bottom: 1px solid #ececec;
}

.empty {
  padding: 24px 20px;
  color: #94a3b8;
}

.selected-list {
  padding: 16px 20px;
  max-height: 460px;
  overflow-y: auto;
}

.selected-item {
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
}

.selected-name {
  font-weight: 600;
  margin-bottom: 4px;
}

.selected-meta {
  color: #64748b;
  font-size: 13px;
}

.selected-total {
  margin-top: 6px;
  color: #991b1b;
  font-weight: 700;
}

.summary-footer {
  border-top: 1px solid #ececec;
  padding: 16px 20px 20px;
}

.sum-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.sum-row.total {
  font-size: 18px;
  font-weight: 700;
}

.btn {
  height: 42px;
  border-radius: 7px;
  border: none;
  padding: 0 16px;
  cursor: pointer;
  font-weight: 700;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn-confirm-full {
  background: #b91c1c;
  color: white;
  width: 100%;
  margin-top: 14px;
}

@media (max-width: 1200px) {
  .menu-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 992px) {
  .qr-layout {
    grid-template-columns: 1fr;
  }

  .toolbar {
    grid-template-columns: 1fr;
  }

  .menu-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .right-area {
    position: static;
  }
}

@media (max-width: 640px) {
  .qr-page {
    padding: 14px;
  }

  .page-header {
    text-align: center;
  }

  .tab-wrap {
    margin-left: auto;
    margin-right: auto;
  }

  .menu-grid {
    grid-template-columns: 1fr;
  }
}
</style>
