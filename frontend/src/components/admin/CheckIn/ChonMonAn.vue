<script>
import { reactive } from "vue";
const chonMonDrafts = reactive({});
</script>

<script setup>
import { computed, ref, onMounted, watch } from "vue";
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";

import { useUtils } from "@/utils/helper";
import { getAllNuocLau } from "@/api/admin/nuoclau/nuocLauApi";
import { getAllDanhMuc } from "@/api/admin/danhmuc/danhMucApi";

import {
  themNhieuMonVaoHoaDon,
  exportPdfGoiMon,
  searchMonLe,
  searchSetLauCheckIn,
} from "@/api/admin/checkin/checkinapi";
import CacheService from "@/services/CacheService";

const { notify, nprogress } = useUtils();

const props = defineProps({
  show: { type: Boolean, default: false },
  ban: { type: Object, default: () => ({}) },
  danhSachMon: { type: Array, default: () => [] },
});

const emit = defineEmits(["close", "added", "xem-don-hang"]);

const tuKhoa = ref("");
const tabDangChon = ref("MON");
const tabDaChonRight = ref("SET");
const hienThiXacNhan = ref(false);
const dangXuLy = ref(false);
const hasProductChanged = ref(false);
const productChangedMessage = ref("");
const coMonNgungKinhDoanh = computed(() => {
  return danhSachDaChon.value.some((item) => item.trangThaiXoa);
});

const nuocLauDaChon = ref("");
const danhMucDaChon = ref("");
const sortGia = ref("");

const danhSachMonHienThi = ref([]);
const danhSachNuocLau = ref([]);
const danhSachDanhMuc = ref([]);

const layDanhSachTuResponse = (res) => {
  if (Array.isArray(res?.data)) return res.data;
  if (Array.isArray(res?.data?.data)) return res.data.data;
  return [];
};

const layIdDanhMuc = (item) =>
  item?.idDanhMuc ?? item?.danhMuc?.id ?? item?.danhMucId ?? "";

const layIdNuocLau = (item) =>
  item?.idNuocLau ?? item?.nuocLau?.id ?? item?.nuocLauId ?? "";

const cungId = (a, b) => String(a ?? "") === String(b ?? "");

const coGiaTriLoc = (value) =>
  value !== null && value !== undefined && String(value).trim() !== "";

const taoParamsLoc = () => {
  const params = {};
  const keyword = tuKhoa.value?.trim();

  if (keyword) {
    params.keyword = keyword;
  }

  if (tabDangChon.value === "SET") {
    if (coGiaTriLoc(nuocLauDaChon.value)) {
      params.idNuocLau = nuocLauDaChon.value;
    }
  } else if (coGiaTriLoc(danhMucDaChon.value)) {
    params.idDanhMuc = danhMucDaChon.value;
  }

  return params;
};

const mapSoLuong = computed(() => {
  const id = props.ban?.idHoaDon || "unknown";
  if (!chonMonDrafts[id]) {
    chonMonDrafts[id] = { soLuong: {}, thongTin: {} };
  }
  return chonMonDrafts[id].soLuong;
});

const mapThongTinMon = computed(() => {
  const id = props.ban?.idHoaDon || "unknown";
  if (!chonMonDrafts[id]) {
    chonMonDrafts[id] = { soLuong: {}, thongTin: {} };
  }
  return chonMonDrafts[id].thongTin;
});

const thongTinBan = computed(() => {
  const listTenBan = Array.isArray(props.ban?.listTenBan)
    ? props.ban.listTenBan
    : [];

  return {
    tenBan:
      listTenBan.length > 0
        ? listTenBan.join(", ")
        : props.ban?.tenBanAn || props.ban?.tenBan || "Chưa xác định",

    khuVuc:
      props.ban?.tenKhuVuc || props.ban?.khuVuc?.tenKhuVuc || "Chưa rõ khu vực",

    maHoaDon: props.ban?.maHoaDon || "Chưa có",

    khachHang: props.ban?.tenKhachHang || "Khách vãng lai",

    soNguoi:
      props.ban?.soNguoiDangDung ||
      props.ban?.soNguoi ||
      props.ban?.soNguoiToiDa ||
      props.ban?.soNguoiDat ||
      0,
  };
});

const ganSoLuongCu = (dsMoi) => {
  return dsMoi.map((item) => {
    const laSet =
      item.laSet === true ||
      item.loai === "SET" ||
      item.loaiSanPham === "SET" ||
      item.tenSet;

    const id = item.id ?? item.idSanPham;

    const mapped = {
      id,
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
        (laSet ? "Set lẩu" : "Món lẻ"),
      loaiSet:
        item.tenNuocLau || item.nuocLau?.tenNuocLau || item.tenLoaiSet || "",
      donVi: laSet ? "Set" : "Phần",
      giaGoc: item.gia ?? item.donGia ?? item.giaBan ?? 0,
      giaSauGiam:
        item.giaSauGiam ?? item.gia ?? item.donGia ?? item.giaBan ?? 0,
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
      soLuongChon: mapSoLuong.value[id] || 0,
      raw: item,
    };

    mapThongTinMon.value[mapped.id] = mapped;
    return mapped;
  });
};

const loadNuocLau = async () => {
  try {
    const res = await getAllNuocLau();
    danhSachNuocLau.value = (layDanhSachTuResponse(res) || []).filter((nl) => !nl.trangThaiXoa);
  } catch (e) {
    notify.error("Không tải được danh sách nước lẩu");
  }
};

const loadDanhMuc = async () => {
  try {
    const cached = CacheService.get("DANH_MUC_CACHE");
    if (cached) {
      danhSachDanhMuc.value = cached;
      return;
    }
    const res = await getAllDanhMuc();
    const activeList = (layDanhSachTuResponse(res) || []).filter(
      (dm) => !dm.trangThaiXoa,
    );
    danhSachDanhMuc.value = activeList;
    CacheService.set("DANH_MUC_CACHE", activeList, 10);
  } catch (e) {
    console.error("Lỗi load danh mục:", e);
    notify.error("Không tải được danh mục món");
  }
};

const loadDanhSachMon = async () => {
  try {
    nprogress.start();

    let data = [];

    if (tabDangChon.value === "SET") {
      const res = await searchSetLauCheckIn(taoParamsLoc());
      data = layDanhSachTuResponse(res);

      if (coGiaTriLoc(nuocLauDaChon.value)) {
        data = data.filter((x) => cungId(layIdNuocLau(x), nuocLauDaChon.value));
      }
    } else {
      const res = await searchMonLe(taoParamsLoc());
      data = layDanhSachTuResponse(res);

      if (coGiaTriLoc(danhMucDaChon.value)) {
        data = data.filter((x) => cungId(layIdDanhMuc(x), danhMucDaChon.value));
      }
    }

    if (sortGia.value) {
      data = [...data].sort((a, b) => {
        const giaA = Number(a.giaSauGiam ?? a.gia ?? a.donGia ?? a.giaBan ?? 0);
        const giaB = Number(b.giaSauGiam ?? b.gia ?? b.donGia ?? b.giaBan ?? 0);

        if (sortGia.value === "asc") return giaA - giaB;
        if (sortGia.value === "desc") return giaB - giaA;
        return 0;
      });
    }

    danhSachMonHienThi.value = ganSoLuongCu(data);
  } catch (e) {
    console.error("Lỗi load thực đơn:", e);
    notify.error("Không tải được danh sách món");
    danhSachMonHienThi.value = [];
  } finally {
    nprogress.done();
  }
};

const currentPage = ref(1);
const pageSize = ref(6);

const totalPages = computed(() => {
  if (!danhSachMonHienThi.value.length) return 1;
  return Math.ceil(danhSachMonHienThi.value.length / pageSize.value);
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

watch([tuKhoa, tabDangChon, nuocLauDaChon, danhMucDaChon, sortGia], () => {
  currentPage.value = 1;
});

const danhSachDaLoc = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return danhSachMonHienThi.value.slice(start, end);
});

const danhSachDaChon = computed(() => {
  return Object.entries(mapSoLuong.value)
    .filter(([, soLuong]) => Number(soLuong) > 0)
    .map(([id, soLuong]) => {
      const mon = mapThongTinMon.value[id];
      if (!mon) return null;

      return {
        ...mon,
        soLuongChon: Number(soLuong),
      };
    })
    .filter(Boolean);
});

const tongSoMonDaChon = computed(() =>
  danhSachDaChon.value.reduce(
    (sum, item) => sum + Number(item.soLuongChon || 0),
    0,
  ),
);

const errorChonMon = ref("");

const danhSachDaChonTheoLoai = computed(() => {
  const dsSet = danhSachDaChon.value.filter((item) => item.loai === "SET");
  const dsMon = danhSachDaChon.value.filter((item) => item.loai !== "SET");

  const monGrouped = {};
  dsMon.forEach((item) => {
    const dm = item.danhMuc || "Khác";
    if (!monGrouped[dm]) {
      monGrouped[dm] = [];
    }
    monGrouped[dm].push(item);
  });

  return {
    SET: dsSet,
    MON: monGrouped,
  };
});

const tongTien = computed(() =>
  danhSachDaChon.value.reduce(
    (sum, item) => sum + Number(item.gia || 0) * Number(item.soLuongChon || 0),
    0,
  ),
);

function dinhDangTien(value) {
  return Number(value || 0).toLocaleString("vi-VN") + "đ";
}

function themMon(item) {
  errorChonMon.value = "";
  const id = item.id;
  const current = mapSoLuong.value[id] || 0;

  mapSoLuong.value[id] = current + 1;
  item.soLuongChon = mapSoLuong.value[id];

  mapThongTinMon.value[id] = {
    ...item,
    soLuongChon: mapSoLuong.value[id],
  };
}

function giamMon(item) {
  const id = item.id;
  const current = mapSoLuong.value[id] || 0;

  mapSoLuong.value[id] = current > 0 ? current - 1 : 0;
  item.soLuongChon = mapSoLuong.value[id];

  mapThongTinMon.value[id] = {
    ...item,
    soLuongChon: mapSoLuong.value[id],
  };
}

function xoaMon(item) {
  const id = item.id;
  mapSoLuong.value[id] = 0;
  item.soLuongChon = 0;

  if (mapThongTinMon.value[id]) {
    mapThongTinMon.value[id].soLuongChon = 0;
  }
}

watch(
  mapSoLuong,
  (newMap) => {
    if (danhSachMonHienThi.value && danhSachMonHienThi.value.length > 0) {
      danhSachMonHienThi.value.forEach((item) => {
        item.soLuongChon = newMap[item.id] || 0;
      });
    }
  },
  { deep: true, immediate: true },
);

function chuyenTab(tab) {
  if (tabDangChon.value === tab) return;

  tabDangChon.value = tab;
  tuKhoa.value = "";
  nuocLauDaChon.value = "";
  danhMucDaChon.value = "";
  sortGia.value = "";
  loadDanhSachMon();
}

function chonNhanhMon(item) {
  themMon(item);
}

async function moXacNhan() {
  errorChonMon.value = "";
  if (!danhSachDaChon.value.length) {
    errorChonMon.value = "Bạn chưa chọn món";
    return;
  }

  if (coMonNgungKinhDoanh.value) {
    errorChonMon.value =
      "Vui lòng loại bỏ các món ăn ngừng kinh doanh trước khi xác nhận";
    return;
  }

  // Thay thế modal cũ bằng notify.confirm
  const isConfirm = await notify.confirm(
    `Bạn đang chuẩn bị thêm ${tongSoMonDaChon.value} món ăn vào hóa đơn. Phiếu bếp sẽ được in ngay sau khi xác nhận. Bạn có chắc chắn gửi bếp?`,
  );

  if (!isConfirm) return;

  // Nếu bấm OK thì thực hiện logic gọi API
  await xacNhan();
}

function dongXacNhan() {
  if (dangXuLy.value) return;
  hienThiXacNhan.value = false;
}

async function xacNhan() {
  try {
    dangXuLy.value = true;
    const startTime = Date.now();

    if (!props.ban?.idHoaDon) {
      notify.error("Không tìm thấy hóa đơn");
      dangXuLy.value = false;
      return;
    }

    const ds = danhSachDaChon.value;
    if (!ds.length) {
      errorChonMon.value = "Bạn chưa chọn món";
      dangXuLy.value = false;
      return;
    }

    const idBanAn =
      props.ban?.idBanAn ||
      (Array.isArray(props.ban?.listIdBan) && props.ban.listIdBan.length > 0
        ? props.ban.listIdBan[0]
        : null);

    // GỌI BATCH API (1 request duy nhất cho nhiều món để chống Deadlock)
    const requestsData = ds.map((item) => ({
      idHoaDon: props.ban.idHoaDon,
      idSanPham: item.id,
      soLuong: item.soLuongChon,
      tenBan: props.ban?.tenBanAn || "",
      idBanAn: idBanAn,
    }));

    const results = await themNhieuMonVaoHoaDon(requestsData);

    // Xử lý xuất PDF ngay khi thêm món xong để trình duyệt không block download
    const idHoaDonPdf = props.ban.idHoaDon;
    const tenBanPdf = thongTinBan.value.tenBan;

    // API trả về trực tiếp danh sách HoaDonChiTietResponse
    const chiTietIds = (results.data || [])
      .map((item) => item.id)
      .filter((id) => id);

    if (chiTietIds.length > 0) {
      try {
        const pdfRes = await exportPdfGoiMon(idHoaDonPdf, chiTietIds);
        const url = window.URL.createObjectURL(
          new Blob([pdfRes.data], { type: "application/pdf" }),
        );
        const link = document.createElement("a");
        link.href = url;
        link.setAttribute(
          "download",
          `Phieu_Goi_Mon_Ban_${tenBanPdf}_HD${idHoaDonPdf}.pdf`,
        );
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      } catch (e) {
        console.error("Lỗi xuất PDF:", e);
      }
    }

    // Xóa draft
    if (idHoaDonPdf && chonMonDrafts[idHoaDonPdf]) {
      chonMonDrafts[idHoaDonPdf].soLuong = {};
      chonMonDrafts[idHoaDonPdf].thongTin = {};
    }

    // Đảm bảo loading đủ 3 giây cho mượt giao diện
    const elapsedTime = Date.now() - startTime;
    if (elapsedTime < 3000) {
      await new Promise((resolve) => setTimeout(resolve, 3000 - elapsedTime));
    }

    notify.success("Thêm món thành công");

    dangXuLy.value = false;
    hienThiXacNhan.value = false;
    emit("added");
    emit("xem-don-hang");
    dongModal();
  } catch (e) {
    console.error("Lỗi thêm món:", e);
    dangXuLy.value = false;
    notify.error(e?.response?.data?.message || "Thêm món thất bại");
  }
}

function dongModal() {
  hasProductChanged.value = false;
  productChangedMessage.value = "";
  emit("close");
}

const layAnhMon = (img) => {
  if (!img) return null;

  if (typeof img === "string") {
    if (img.startsWith("http")) return img;
    return `http://localhost:8080/uploads/${img}`;
  }

  return null;
};

function onImgError(event) {
  event.target.style.display = "none";
}

import { connectWebSocket, disconnectWebSocket } from "@/utils/websocket";
import { onUnmounted } from "vue";

let wsTimeout = null;

const capNhatTrangThaiDaChon = async () => {
  const selectedItems = danhSachDaChon.value;
  if (!selectedItems.length) return;

  try {
    const [resSet, resMon] = await Promise.all([
      searchSetLauCheckIn({}),
      searchMonLe({}),
    ]);

    const activeSets = layDanhSachTuResponse(resSet);
    const activeMons = layDanhSachTuResponse(resMon);
    const allActive = [...activeSets, ...activeMons];

    const activeMap = new Map();
    allActive.forEach((p) => {
      activeMap.set(String(p.id ?? p.idSanPham), p);
    });

    selectedItems.forEach((item) => {
      const activeItem = activeMap.get(String(item.id));
      if (activeItem) {
        const giaGoc =
          activeItem.gia ?? activeItem.donGia ?? activeItem.giaBan ?? 0;
        const giaSauGiam =
          activeItem.giaSauGiam ??
          activeItem.gia ??
          activeItem.donGia ??
          activeItem.giaBan ??
          0;
        const soTienGiam = activeItem.soTienGiam ?? 0;
        const phanTramGiam = activeItem.phanTramGiam ?? 0;
        const tenMon =
          activeItem.tenSanPham ||
          activeItem.tenMon ||
          activeItem.tenSet ||
          activeItem.ten ||
          activeItem.name ||
          item.tenMon;
        const hinhAnh =
          activeItem.hinhAnh ||
          activeItem.urlAnh ||
          activeItem.anh ||
          activeItem.image ||
          activeItem.imageUrl ||
          item.hinhAnh;

        item.trangThaiXoa = false;
        item.giaGoc = giaGoc;
        item.giaSauGiam = giaSauGiam;
        item.gia = giaSauGiam;
        item.soTienGiam = soTienGiam;
        item.phanTramGiam = phanTramGiam;
        item.tenMon = tenMon;
        item.hinhAnh = hinhAnh;
        item.raw = activeItem;

        if (mapThongTinMon.value[item.id]) {
          mapThongTinMon.value[item.id] = {
            ...mapThongTinMon.value[item.id],
            trangThaiXoa: false,
            giaGoc,
            giaSauGiam,
            gia: giaSauGiam,
            soTienGiam,
            phanTramGiam,
            tenMon,
            hinhAnh,
            raw: activeItem,
          };
        }
      } else {
        item.trangThaiXoa = true;
        if (mapThongTinMon.value[item.id]) {
          mapThongTinMon.value[item.id].trangThaiXoa = true;
        }
      }
    });
  } catch (err) {
    console.error("Lỗi đồng bộ trạng thái món đã chọn:", err);
  }
};

const handleWsMessage = (msg) => {
  console.log(">>> [ChonMonAn WS] Received message:", msg);
  if (
    msg?.type === "SAN_PHAM_UPDATE" ||
    msg?.type === "DOT_GIAM_GIA_UPDATE" ||
    msg?.type === "DANH_MUC_UPDATE"
  ) {
    console.log(">>> [ChonMonAn WS] Matching type, props.show =", props.show);
    if (props.show) {
      if (wsTimeout) clearTimeout(wsTimeout);
      wsTimeout = setTimeout(async () => {
        CacheService.remove("DANH_MUC_CACHE");
        await Promise.all([loadNuocLau(), loadDanhMuc(), loadDanhSachMon()]);
        await capNhatTrangThaiDaChon();
        console.log(">>> [ChonMonAn WS] Reloaded.");
        hasProductChanged.value = true;
        if (msg.type === "DOT_GIAM_GIA_UPDATE") {
          productChangedMessage.value =
            "Đợt giảm giá sản phẩm vừa thay đổi hãy kiểm tra giá sản phẩm trước khi xác nhận";
        } else {
          productChangedMessage.value =
            "Món ăn có sự thay đổi vui lòng kiểm tra thông tin trước khi xác nhận";
        }
        console.log(
          ">>> [ChonMonAn WS] Triggering confirm dialog:",
          productChangedMessage.value,
        );
        notify.confirm(productChangedMessage.value);
      }, 150);
    }
  }
};

watch(
  () => props.show,
  async (val) => {
    if (val) {
      hasProductChanged.value = false;
      productChangedMessage.value = "";
      await loadDanhSachMon();
      await capNhatTrangThaiDaChon();
    }
  },
);

onMounted(async () => {
  await Promise.all([loadNuocLau(), loadDanhMuc()]);
  await loadDanhSachMon();
  connectWebSocket(null, handleWsMessage);
});

onUnmounted(() => {
  if (wsTimeout) clearTimeout(wsTimeout);
  disconnectWebSocket(handleWsMessage);
});
</script>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="show" class="modal-overlay" @click.self="dongModal">
        <div class="chon-mon-modal">
          <div class="modal-header">
            <div class="header-left">
              <h2>Chọn món cho bàn {{ thongTinBan.tenBan }}</h2>

              <div class="ban-info-grid">
                <div class="ban-info-card">
                  <span>Khu vực</span>
                  <strong>{{ thongTinBan.khuVuc }}</strong>
                </div>

                <div class="ban-info-card">
                  <span>Hóa đơn</span>
                  <strong>{{ thongTinBan.maHoaDon }}</strong>
                </div>

                <div class="ban-info-card">
                  <span>Khách hàng</span>
                  <strong>{{ thongTinBan.khachHang }}</strong>
                </div>

                <div class="ban-info-card">
                  <span>Số người</span>
                  <strong>{{ thongTinBan.soNguoi }} người</strong>
                </div>
              </div>
            </div>

            <button class="btn-close" @click="dongModal">✕</button>
          </div>

          <div class="content-wrap">
            <div class="left-side">
              <p class="tip">
                <i class="ri-information-fill"></i>
                Bấm vào món để chọn nhanh. Có thể lọc món theo từ khóa, danh mục
                và giá.
              </p>

              <div class="tab-row">
                <button
                  class="tab-btn"
                  :class="{ active: tabDangChon === 'MON' }"
                  @click="chuyenTab('MON')"
                >
                  Món lẻ
                </button>

                <button
                  class="tab-btn"
                  :class="{ active: tabDangChon === 'SET' }"
                  @click="chuyenTab('SET')"
                >
                  Set lẩu
                </button>
              </div>

              <div class="modal-filter-bar">
                <div class="filter-group">
                  <div class="filter-item">
                    <label>Tìm sản phẩm</label>
                    <input
                      v-model="tuKhoa"
                      type="text"
                      placeholder="Tên sản phẩm..."
                      @input="loadDanhSachMon"
                    />
                  </div>

                  <div class="filter-item">
                    <label>Danh mục</label>

                    <v-select
                      v-if="tabDangChon === 'SET'"
                      class="modern-select"
                      v-model="nuocLauDaChon"
                      :options="[
                        { tenNuocLau: 'Tất cả danh mục', id: '' },
                        ...danhSachNuocLau,
                      ]"
                      label="tenNuocLau"
                      :reduce="(item) => item.id"
                      placeholder="Tất cả danh mục"
                      :clearable="false"
                      :searchable="true"
                      @update:modelValue="loadDanhSachMon"
                    >
                      <template #no-options="{ search, searching }">
                        <template v-if="searching">
                          Không tìm thấy "<b>{{ search }}</b
                          >"
                        </template>
                        <em v-else style="opacity: 0.5">Không có dữ liệu</em>
                      </template>
                    </v-select>

                    <v-select
                      v-else
                      class="modern-select"
                      v-model="danhMucDaChon"
                      :options="[
                        { tenDanhMuc: 'Tất cả danh mục', id: '' },
                        ...danhSachDanhMuc,
                      ]"
                      label="tenDanhMuc"
                      :reduce="(item) => item.id"
                      placeholder="Tất cả danh mục"
                      :clearable="false"
                      :searchable="true"
                      @update:modelValue="loadDanhSachMon"
                    >
                      <template #no-options="{ search, searching }">
                        <template v-if="searching">
                          Không tìm thấy "<b>{{ search }}</b
                          >"
                        </template>
                        <em v-else style="opacity: 0.5">Không có dữ liệu</em>
                      </template>
                    </v-select>
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
                      :reduce="(opt) => opt.value"
                      placeholder="Mặc định"
                      :clearable="false"
                      :searchable="false"
                      @update:modelValue="loadDanhSachMon"
                    >
                      <template #no-options="{ search, searching }">
                        <template v-if="searching">
                          Không tìm thấy "<b>{{ search }}</b
                          >"
                        </template>
                        <em v-else style="opacity: 0.5">Không có dữ liệu</em>
                      </template>
                    </v-select>
                  </div>
                </div>
              </div>

              <div class="left-panel">
                <div class="panel-title">
                  {{
                    tabDangChon === "SET"
                      ? "Danh sách set lẩu"
                      : "Danh sách món lẻ"
                  }}
                </div>

                <div v-if="danhSachDaLoc.length === 0" class="empty-state">
                  Không có món phù hợp
                </div>

                <div v-else class="mon-grid">
                  <div
                    v-for="item in danhSachDaLoc"
                    :key="item.id"
                    class="mon-card mon-card-row"
                    @click="chonNhanhMon(item)"
                  >
                    <div class="mon-card-left">
                      <div class="mon-thumb-wrap">
                        <img
                          v-if="layAnhMon(item.hinhAnh)"
                          :src="layAnhMon(item.hinhAnh)"
                          alt="Ảnh món"
                          class="mon-thumb"
                          @error="onImgError"
                        />

                        <div v-else class="mon-thumb placeholder">
                          {{ item.loai === "SET" ? "SET" : "MÓN" }}
                        </div>
                      </div>

                      <div class="mon-text">
                        <div class="mon-ten">
                          {{ item.tenMon || "Chưa có tên món" }}
                        </div>

                        <div class="mon-meta-line">
                          <template
                            v-if="
                              Number(item.giaSauGiam || 0) <
                              Number(item.giaGoc || 0)
                            "
                          >
                            <span class="gia-goc">
                              {{ dinhDangTien(item.giaGoc) }}
                            </span>
                            <span class="gia-giam">
                              {{ dinhDangTien(item.giaSauGiam) }}
                            </span>
                            <span v-if="item.soTienGiam > 0" class="badge-giam">
                              -{{ dinhDangTien(item.soTienGiam) }}
                            </span>
                          </template>

                          <template v-else>
                            <span class="gia-thuong">
                              {{ dinhDangTien(item.gia) }}
                            </span>
                          </template>
                        </div>

                        <div class="mon-meta">
                          <span
                            v-if="item.loai === 'MON' && item.danhMuc"
                            class="tag danhmuc"
                          >
                            {{ item.danhMuc }}
                          </span>

                          <span
                            v-if="item.loai === 'SET' && item.loaiSet"
                            class="tag nuoclau"
                          >
                            {{ item.loaiSet }}
                          </span>
                        </div>
                      </div>
                    </div>

                    <div class="mon-card-right" @click.stop>
                      <div
                        v-if="item.soLuongChon > 0"
                        class="badge-so-luong-inline"
                      >
                        {{ item.soLuongChon }}
                      </div>

                      <div class="mon-action inline-action">
                        <button class="btn-sl" @click="giamMon(item)">−</button>
                        <span class="sl">{{ item.soLuongChon || 0 }}</span>
                        <button class="btn-sl" @click="themMon(item)">
                          ＋
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
                <div v-if="danhSachDaLoc.length > 0" class="pagination">
                  <button
                    :disabled="currentPage === 1"
                    @click="currentPage--"
                    class="pg-btn pg-arrow"
                  >
                    <i class="ri-arrow-left-s-line"></i>
                  </button>

                  <template v-for="p in pageList" :key="p">
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
            </div>

            <div class="right-panel">
              <div class="panel-title">Món đã chọn</div>

              <div v-if="danhSachDaChon.length === 0" class="empty-state">
                Chưa có món nào được chọn
              </div>

              <div v-else class="selected-list-container">
                <div class="right-tabs">
                  <button
                    class="right-tab-btn"
                    :class="{ active: tabDaChonRight === 'SET' }"
                    @click="tabDaChonRight = 'SET'"
                  >
                    Set lẩu ({{ danhSachDaChonTheoLoai.SET.length }})
                  </button>
                  <button
                    class="right-tab-btn"
                    :class="{ active: tabDaChonRight === 'MON' }"
                    @click="tabDaChonRight = 'MON'"
                  >
                    Món lẻ ({{
                      Object.values(danhSachDaChonTheoLoai.MON).reduce(
                        (a, b) => a + b.length,
                        0,
                      )
                    }})
                  </button>
                </div>

                <div class="selected-list">
                  <!-- SET LAU -->
                  <div
                    v-if="tabDaChonRight === 'SET'"
                    class="selected-category"
                  >
                    <div
                      v-if="danhSachDaChonTheoLoai.SET.length === 0"
                      class="empty-state-sm"
                    >
                      Chưa có set lẩu nào
                    </div>
                    <div
                      v-else
                      v-for="item in danhSachDaChonTheoLoai.SET"
                      :key="`chon-${item.id}`"
                      class="selected-item"
                      :class="{ 'ngung-kinh-doanh': item.trangThaiXoa }"
                    >
                      <div class="selected-left">
                        <div class="selected-thumb-wrap">
                          <img
                            v-if="layAnhMon(item.hinhAnh)"
                            :src="layAnhMon(item.hinhAnh)"
                            alt="Ảnh món"
                            class="selected-thumb"
                            @error="onImgError"
                          />
                          <div v-else class="selected-thumb placeholder">
                            IMG
                          </div>
                        </div>

                        <div class="selected-main">
                          <div class="selected-ten">{{ item.tenMon }}</div>
                          <div class="selected-meta">
                            {{ item.soLuongChon }} x
                            {{ dinhDangTien(item.gia) }}
                            <span
                              v-if="item.soTienGiam > 0"
                              class="badge-giam"
                              style="
                                margin-left: 6px;
                                padding: 1px 5px;
                                font-size: 10px;
                              "
                            >
                              -{{ dinhDangTien(item.soTienGiam) }}
                            </span>
                          </div>
                          <div
                            v-if="item.trangThaiXoa"
                            class="status-ngung-kinh-doanh-text text-danger fw-bold"
                            style="font-size: 11px; margin-top: 4px"
                          >
                            <i class="ri-error-warning-fill"></i> Món ăn ngừng
                            kinh doanh
                          </div>
                        </div>
                      </div>

                      <div class="selected-right">
                        <div class="selected-total">
                          {{ dinhDangTien(item.soLuongChon * item.gia) }}
                        </div>

                        <div class="selected-action">
                          <button class="btn-sl sm" @click="giamMon(item)">
                            −
                          </button>
                          <span class="sl sm-text">{{ item.soLuongChon }}</span>
                          <button
                            class="btn-sl sm"
                            @click="themMon(item)"
                            :disabled="item.trangThaiXoa"
                          >
                            ＋
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- MON LE -->
                  <div
                    v-if="tabDaChonRight === 'MON'"
                    class="selected-category-container"
                  >
                    <div
                      v-if="
                        Object.keys(danhSachDaChonTheoLoai.MON).length === 0
                      "
                      class="empty-state-sm"
                    >
                      Chưa có món lẻ nào
                    </div>

                    <template v-else>
                      <div
                        v-for="(items, dm) in danhSachDaChonTheoLoai.MON"
                        :key="dm"
                        class="selected-category-group"
                      >
                        <div class="category-header">{{ dm }}</div>
                        <div class="selected-category">
                          <div
                            v-for="item in items"
                            :key="`chon-${item.id}`"
                            class="selected-item"
                            :class="{ 'ngung-kinh-doanh': item.trangThaiXoa }"
                          >
                            <div class="selected-left">
                              <div class="selected-thumb-wrap">
                                <img
                                  v-if="layAnhMon(item.hinhAnh)"
                                  :src="layAnhMon(item.hinhAnh)"
                                  alt="Ảnh món"
                                  class="selected-thumb"
                                  @error="onImgError"
                                />
                                <div v-else class="selected-thumb placeholder">
                                  IMG
                                </div>
                              </div>

                              <div class="selected-main">
                                <div class="selected-ten">
                                  {{ item.tenMon }}
                                </div>
                                <div class="selected-meta">
                                  {{ item.soLuongChon }} x
                                  {{ dinhDangTien(item.gia) }}
                                  <span
                                    v-if="item.soTienGiam > 0"
                                    class="badge-giam"
                                    style="
                                      margin-left: 6px;
                                      padding: 1px 5px;
                                      font-size: 10px;
                                    "
                                  >
                                    -{{ dinhDangTien(item.soTienGiam) }}
                                  </span>
                                </div>
                                <div
                                  v-if="item.trangThaiXoa"
                                  class="status-ngung-kinh-doanh-text text-danger fw-bold"
                                  style="font-size: 11px; margin-top: 4px"
                                >
                                  <i class="ri-error-warning-fill"></i> Món ăn
                                  ngừng kinh doanh
                                </div>
                              </div>
                            </div>

                            <div class="selected-right">
                              <div class="selected-total">
                                {{ dinhDangTien(item.soLuongChon * item.gia) }}
                              </div>

                              <div class="selected-action">
                                <button
                                  class="btn-sl sm"
                                  @click="giamMon(item)"
                                >
                                  −
                                </button>
                                <span class="sl sm-text">{{
                                  item.soLuongChon
                                }}</span>
                                <button
                                  class="btn-sl sm"
                                  @click="themMon(item)"
                                  :disabled="item.trangThaiXoa"
                                >
                                  ＋
                                </button>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </template>
                  </div>
                </div>
              </div>

              <div class="tong-ket">
                <div class="tong-row">
                  <span>Tổng số lượng</span>
                  <strong>{{ tongSoMonDaChon }}</strong>
                </div>

                <div class="tong-row tong-tien">
                  <span>Tổng tiền</span>
                  <strong>{{ dinhDangTien(tongTien) }}</strong>
                </div>
              </div>

              <div class="footer-action">
                <div
                  v-if="hasProductChanged"
                  class="alert-product-changed fw-bold mb-3"
                >
                  <i class="ri-error-warning-fill"></i>
                  <span>{{
                    productChangedMessage ||
                    "Món ăn có sự thay đổi vui lòng kiểm tra thông tin trước khi xác nhận"
                  }}</span>
                </div>
                <div
                  v-if="errorChonMon"
                  class="error-text"
                  style="text-align: right; margin-bottom: 8px"
                >
                  {{ errorChonMon }}
                </div>
                <div style="display: flex; gap: 10px; width: 100%">
                  <button class="btn-cancel" @click="dongModal">
                    <i class="ri-close-line"></i> Đóng
                  </button>
                  <button
                    class="btn-primary"
                    @click="moXacNhan"
                    :disabled="coMonNgungKinhDoanh"
                    :class="{ disabled: coMonNgungKinhDoanh }"
                  >
                    <div class="btn-light-sweep"></div>
                    <i class="ri-check-double-line"></i> Xác nhận
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <Transition name="fade">
      <div v-if="dangXuLy" class="admin-luxury-overlay">
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
    </Transition>
  </Teleport>
</template>

<style>
@keyframes spin {
  100% {
    transform: rotate(360deg);
  }
}
</style>

<style scoped>
.modal-overlay,
.chon-mon-modal,
.modal-header,
.tab-row,
.modal-filter-bar,
.content-wrap,
.left-side,
.left-panel,
.right-panel,
.panel-title,
.mon-grid,
.mon-card,
.selected-list,
.selected-item,
.tong-ket,
.footer-action,
button,
input,
label,
span,
div,
h2,
p {
  font-family: var(--font-family, "Inter", sans-serif);
  box-sizing: border-box;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99999;
  padding: 20px;
  overflow-y: auto;
}

.chon-mon-modal {
  width: min(1600px, 98vw);
  height: min(94vh, 900px);
  background: #fff;
  border-radius: 7px;
  overflow: hidden;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.18);
  display: flex;
  flex-direction: column;
  color: var(--color-text);
  border: 1px solid rgba(0, 0, 0, 0.06);
  animation: modalScaleIn 0.25s ease;
}

@keyframes modalScaleIn {
  from {
    opacity: 0;
    transform: translateY(8px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  padding: 20px 24px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.header-left {
  flex: 1;
  min-width: 0;
}

.modal-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  color: var(--color-text);
  letter-spacing: -0.02em;
}

.ban-info-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(4, minmax(120px, 1fr));
  gap: 10px;
}

.ban-info-card {
  background: #fff7ed;
  border: 1px solid #fed7aa;
  border-radius: 7px;
  padding: 10px 12px;
  min-width: 0;
  transition: all 0.25s ease;
}

.ban-info-card:hover {
  transform: translateY(-2px);
  background: #ffedd5;
  border-color: #fdba74;
  box-shadow: 0 10px 20px rgba(249, 115, 22, 0.12);
}

.ban-info-card span {
  display: block;
  font-size: 11px;
  font-weight: 800;
  color: #9a3412;
  margin-bottom: 4px;
  text-transform: uppercase;
}

.ban-info-card strong {
  display: block;
  font-size: 13px;
  font-weight: 800;
  color: #0f172a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.btn-close {
  width: 42px;
  height: 42px;
  border: none;
  border-radius: 7px;
  background: #f3f4f6;
  font-size: 18px;
  font-weight: 700;
  color: #475569;
  cursor: pointer;
  transition: all 0.25s ease;
  flex-shrink: 0;
}

.btn-close:hover {
  background: rgba(230, 90, 90, 0.12);
  color: var(--color-primary);
  transform: rotate(90deg) scale(1.04);
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.12);
}

.tip {
  font-size: 13px;
  color: var(--color-text-muted);
  margin: 0;
  font-weight: 500;
  background: rgba(230, 90, 90, 0.06);
  border: 1px solid rgba(230, 90, 90, 0.1);
  border-radius: 7px;
  padding: 10px 12px;
}

.tab-row {
  display: flex;
  gap: 10px;
  padding: 0;
}

.tab-btn {
  min-width: 128px;
  height: 42px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: #fff;
  color: var(--color-text);
  border-radius: 7px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.25s ease;
}

.tab-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 18px rgba(15, 23, 42, 0.07);
}

.tab-btn.active {
  background: linear-gradient(135deg, var(--color-primary), #d16969);
  color: white;
  border-color: transparent;
  box-shadow: 0 10px 20px rgba(230, 90, 90, 0.22);
}

.content-wrap {
  padding: 16px 24px 24px;
  display: grid;
  grid-template-columns: 7fr 3fr;
  gap: 18px;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.left-side {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  gap: 16px;
}

.modal-filter-bar {
  padding: 0;
  border: none;
  background: transparent;
  overflow: visible;
  position: relative;
  z-index: 20;
}

.filter-group {
  display: grid;
  grid-template-columns: 1.4fr 0.95fr 0.95fr;
  gap: 14px;
  align-items: end;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
  position: relative;
}

.filter-item label {
  font-size: 13px;
  color: var(--color-text-muted);
  font-weight: 700;
}

.filter-item input {
  width: 100%;
  height: 44px;
  padding: 0 14px;
  border-radius: 7px;
  border: 1px solid #dcdfe4;
  outline: none;
  font-size: 14px;
  background: #fff;
  color: var(--color-text);
  transition: all 0.25s ease;
}

.filter-item input:hover,
:deep(.modern-select .vs__dropdown-toggle:hover) {
  border-color: rgba(230, 90, 90, 0.45) !important;
  box-shadow: 0 8px 18px rgba(230, 90, 90, 0.08);
  transform: translateY(-1px);
}

.filter-item input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12);
}

:deep(.modern-select) {
  width: 100%;
  position: relative;
  background: #fff;
}

:deep(.modern-select .vs__dropdown-toggle) {
  min-height: 44px;
  height: 44px;
  border-radius: 7px;
  border: 1px solid #dcdfe4 !important;
  padding: 0;
  box-shadow: none !important;
  background: #fff;
  display: flex;
  align-items: center;
  transition: all 0.25s ease;
  overflow: visible !important;
}

:deep(.modern-select .vs__selected-options) {
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  padding: 0 4px 0 14px;
  margin: 0;
  height: 100%;
}

:deep(.modern-select .vs__selected) {
  margin: 0 !important;
  padding: 0 !important;
  font-size: 14px;
  color: #64748b !important; /* Màu xám */
  font-weight: 500;
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  border: none !important;
}

:deep(.modern-select .vs__search),
:deep(.modern-select .vs__search:focus) {
  width: auto !important;
  min-width: 4px !important;
  height: 22px !important;
  flex: 1 1 4px !important;
  padding: 0 !important;
  margin: 0 !important;
  opacity: 1 !important;
  position: static !important;
  pointer-events: auto !important;
  color: var(--color-text) !important;
  font-size: 14px !important;
  line-height: 22px !important;
  border: none !important;
  box-shadow: none !important;
}

:deep(.modern-select .vs__actions) {
  padding: 0 12px 0 8px;
  display: flex;
  align-items: center;
}

:deep(.modern-select .vs__open-indicator) {
  fill: #9ca3af;
  transform: scale(0.75);
}

:deep(.modern-select.vs--open) {
  z-index: 1000006 !important;
}

:deep(.modern-select.vs--open .vs__dropdown-toggle) {
  border-color: var(--color-primary) !important;
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12) !important;
}

:deep(.modern-select .vs__dropdown-menu) {
  border: 1px solid #e5e7eb !important;
  border-radius: 7px !important;
  margin-top: 8px;
  box-shadow: 0 14px 28px rgba(0, 0, 0, 0.08) !important;
  padding: 6px !important;
  overflow-y: auto !important;
  overflow-x: hidden !important;
  max-height: 220px !important;
  z-index: 1000007 !important;
  background: #fff !important;
}

:deep(.modern-select .vs__dropdown-option) {
  border-radius: 7px;
  padding: 10px 12px;
  font-size: 14px;
  color: var(--color-text);
}

:deep(.modern-select .vs__dropdown-option--highlight) {
  background: rgba(230, 90, 90, 0.08) !important;
  color: var(--color-primary) !important;
}

:deep(.modern-select .vs__dropdown-option--selected) {
  background: rgba(230, 90, 90, 0.1) !important;
  color: var(--color-primary) !important;
}

.left-panel,
.right-panel {
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 7px;
  background: rgba(255, 255, 255, 0.96);
  min-height: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
  transition: all 0.25s ease;
}

.left-panel:hover,
.right-panel:hover {
  border-color: rgba(230, 90, 90, 0.2);
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.08);
}

.right-panel {
  width: 100%;
  min-width: 0;
  max-width: none;
}

.panel-title {
  padding: 14px 16px;
  font-size: 16px;
  font-weight: 800;
  color: var(--color-text);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  transition: all 0.25s ease;
}

.panel-title:hover {
  color: var(--color-primary);
  background: rgba(230, 90, 90, 0.04);
}

.right-ban-summary {
  padding: 12px 14px;
  display: grid;
  gap: 8px;
  background: #fff7ed;
  border-bottom: 1px solid #fed7aa;
  transition: all 0.25s ease;
}

.right-ban-summary:hover {
  background: #ffedd5;
}

.right-ban-summary div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.right-ban-summary span {
  font-size: 12px;
  font-weight: 800;
  color: #9a3412;
}

.right-ban-summary strong {
  font-size: 13px;
  font-weight: 800;
  color: #0f172a;
  text-align: right;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mon-grid {
  padding: 14px;
  display: grid;
  grid-template-columns: repeat(2, minmax(280px, 1fr));
  gap: 14px;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
}

.mon-card {
  position: relative;
  border: 1px solid #e5e7eb;
  border-radius: 7px;
  overflow: hidden;
  background: #fff;
  cursor: pointer;
  transition: all 0.25s ease;
}

.mon-card::after {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: 7px;
  border: 2px solid transparent;
  pointer-events: none;
  transition: all 0.25s ease;
}

.mon-card:hover::after {
  border-color: rgba(230, 90, 90, 0.25);
}

.mon-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 16px 28px rgba(15, 23, 42, 0.08);
  border-color: rgba(230, 90, 90, 0.18);
}

.mon-card-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  min-height: 114px;
  padding: 14px 16px;
}

.mon-card-left {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
  flex: 1;
}

.mon-thumb-wrap {
  width: 84px;
  height: 84px;
  border-radius: 7px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f3f4f6;
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.25s ease;
}

.mon-card:hover .mon-thumb-wrap {
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.12);
}

.mon-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.25s ease;
}

.mon-card:hover .mon-thumb {
  transform: scale(1.06);
}

.mon-thumb.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8fafc, #eef2f7);
  color: #94a3b8;
  font-size: 12px;
  font-weight: 800;
}

.mon-text {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.mon-ten {
  font-size: 17px;
  font-weight: 800;
  color: var(--color-text);
  line-height: 1.35;
}

.mon-meta-line {
  font-size: 13px;
  color: var(--color-text-muted);
  line-height: 1.4;
  font-weight: 500;
}

.mon-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 6px;
}

.tag {
  padding: 2px 7px;
  border-radius: 999px;
  font-size: 10px;
  font-weight: 600;
  transition: all 0.25s ease;
}

.tag:hover {
  transform: translateY(-1px);
  filter: brightness(0.98);
}

.tag.loai {
  background: rgba(230, 90, 90, 0.12);
  color: var(--color-primary);
}

.tag.danhmuc {
  background: #ecfeff;
  color: #0e7490;
}

.tag.nuoclau {
  background: #ede9fe;
  color: #5b21b6;
}

.mon-card-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.badge-so-luong-inline {
  min-width: 30px;
  height: 30px;
  padding: 0 8px;
  border-radius: 999px;
  background: linear-gradient(135deg, var(--color-primary), #d16969);
  color: #fff;
  font-size: 13px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 16px rgba(230, 90, 90, 0.18);
  transition: all 0.25s ease;
}

.mon-card:hover .badge-so-luong-inline {
  transform: scale(1.08);
}

.inline-action {
  margin: 0;
  padding: 0;
  border-top: none;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-sl {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 7px;
  background: #f3f4f6;
  cursor: pointer;
  font-size: 18px;
  font-weight: 800;
  color: #334155;
  transition: all 0.25s ease;
}

.btn-sl:hover {
  background: linear-gradient(135deg, var(--color-primary), #d16969);
  color: white;
  transform: scale(1.06);
  box-shadow: 0 10px 18px rgba(230, 90, 90, 0.18);
}

.btn-sl:active {
  transform: scale(0.94);
}

.sl {
  min-width: 28px;
  text-align: center;
  font-weight: 800;
  font-size: 15px;
  color: var(--color-text);
}

.selected-list-container {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.right-tabs {
  display: flex;
  gap: 4px;
  padding: 8px 12px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  background: #f8fafc;
}

.right-tab-btn {
  flex: 1;
  padding: 8px 4px;
  font-size: 12px;
  font-weight: 700;
  color: #64748b;
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.right-tab-btn:hover {
  color: #334155;
  background: rgba(0, 0, 0, 0.02);
}

.right-tab-btn.active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
  background: #fff;
}

.empty-state-sm {
  padding: 24px;
  text-align: center;
  font-size: 13px;
  color: #94a3b8;
  font-weight: 500;
  background: rgba(248, 250, 252, 0.5);
  border-radius: 7px;
  border: 1px dashed #cbd5e1;
  margin: 12px;
}

.selected-list {
  padding: 4px 12px 12px;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
}

.selected-category {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 8px;
}

.selected-category-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.selected-category-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-header {
  font-size: 14px;
  font-weight: 800;
  color: #1e293b;
  padding: 6px 10px;
  background: #f1f5f9;
  border-left: 3px solid var(--color-primary);
  border-radius: 4px;
  margin-bottom: 2px;
}

.selected-item {
  position: relative;
  border: 1px solid #e5e7eb;
  border-radius: 7px;
  padding: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  background: #fff;
  transition: all 0.25s ease;
}

.selected-item:hover {
  transform: translateY(-2px);
  border-color: rgba(230, 90, 90, 0.18);
  box-shadow: 0 12px 20px rgba(15, 23, 42, 0.06);
}

.selected-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  flex: 1;
}

.selected-thumb-wrap {
  width: 72px;
  height: 72px;
  border-radius: 7px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f3f4f6;
  border: 1px solid #eef2f7;
}

.selected-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.selected-thumb.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 11px;
  font-weight: 700;
}

.selected-main {
  min-width: 0;
  flex: 1;
}

.selected-ten {
  font-weight: 800;
  color: var(--color-text);
  line-height: 1.3;
  font-size: 16px;
  word-break: break-word;
}

.selected-meta {
  margin-top: 4px;
  color: var(--color-text-muted);
  font-size: 14px;
  font-weight: 500;
}

.selected-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: center;
  gap: 6px;
  flex-shrink: 0;
  min-width: 78px;
}

.selected-total {
  font-weight: 800;
  color: var(--color-primary);
  white-space: nowrap;
  font-size: 16px;
  text-align: right;
}

.selected-action {
  display: flex;
  align-items: center;
  gap: 4px;
}

.sm-text {
  min-width: 20px;
  text-align: center;
  font-weight: 800;
  font-size: 14px;
}

.btn-sl.sm {
  width: 28px;
  height: 28px;
  font-size: 15px;
  border-radius: 7px;
}

.tong-ket {
  margin-top: auto;
  padding: 12px 14px;
  border-top: 1px solid #eef2f7;
  background: linear-gradient(
    135deg,
    rgba(230, 90, 90, 0.06),
    rgba(230, 90, 90, 0.03)
  );
  transition: all 0.25s ease;
}

.tong-ket:hover {
  background: linear-gradient(
    135deg,
    rgba(230, 90, 90, 0.1),
    rgba(230, 90, 90, 0.05)
  );
}

.tong-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  color: #334155;
  font-size: 13px;
  font-weight: 600;
}

.tong-row strong {
  color: var(--color-text);
  font-weight: 800;
}

.tong-row.tong-tien {
  margin-bottom: 0;
  font-size: 15px;
}

.tong-row.tong-tien strong {
  color: var(--color-primary);
  font-size: 18px;
}

.footer-action {
  padding: 12px 14px 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.btn-primary,
.btn-cancel {
  flex: 1;
  padding: 10px 16px;
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
  white-space: nowrap;
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

.empty-state {
  padding: 30px 16px;
  text-align: center;
  color: var(--color-text-muted);
  font-size: 14px;
  font-weight: 500;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.mon-grid::-webkit-scrollbar,
.selected-list::-webkit-scrollbar {
  width: 8px;
}

.mon-grid::-webkit-scrollbar-track,
.selected-list::-webkit-scrollbar-track {
  background: #f3f4f6;
  border-radius: 999px;
}

.mon-grid::-webkit-scrollbar-thumb,
.selected-list::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 999px;
}

.mon-grid::-webkit-scrollbar-thumb:hover,
.selected-list::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}

.gia-goc {
  color: #9ca3af;
  text-decoration: line-through;
  font-size: 11px;
  margin-right: 6px;
}

.gia-giam,
.gia-thuong {
  color: var(--color-primary);
  font-weight: 800;
  margin-right: 6px;
}

.badge-giam {
  display: inline-flex;
  align-items: center;
  padding: 2px 7px;
  border-radius: 999px;
  background: rgba(230, 90, 90, 0.12);
  color: var(--color-primary);
  font-size: 11px;
  font-weight: 800;
}

/* /////////////////// PHÂN TRANG ///////// */

.pagination {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  padding-bottom: 16px;
  gap: 8px;
  margin-top: 16px;
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
  border-radius: 7px;
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

@media (max-width: 992px) {
  .content-wrap {
    display: flex;
    flex-direction: column;
    overflow-y: auto;
  }

  .left-side,
  .left-panel,
  .right-panel,
  .mon-grid {
    min-height: auto;
    height: auto;
    flex: none;
    overflow: visible;
  }

  .right-panel {
    width: 100%;
    min-width: 100%;
    max-width: 100%;
    margin-top: 24px;
  }

  .filter-group {
    grid-template-columns: 1fr;
  }

  .ban-info-grid {
    grid-template-columns: repeat(2, minmax(120px, 1fr));
  }
}

@media (max-width: 768px) {
  .mon-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .modal-overlay {
    padding: 12px;
  }

  .chon-mon-modal {
    width: 100%;
    height: auto;
    max-height: 96vh;
    border-radius: 7px;
  }

  .modal-header,
  .tab-row,
  .content-wrap {
    padding-left: 16px;
    padding-right: 16px;
  }

  .modal-header {
    flex-direction: column;
  }

  .btn-close {
    position: absolute;
    top: 14px;
    right: 14px;
  }

  .content-wrap {
    padding-bottom: 16px;
  }

  .modal-header h2 {
    font-size: 20px;
    padding-right: 48px;
  }

  .ban-info-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .tab-row {
    flex-wrap: wrap;
  }

  .tab-btn {
    flex: 1;
  }

  .footer-action {
    flex-direction: column;
  }

  .selected-item,
  .mon-card-row {
    flex-direction: column;
    align-items: stretch;
  }

  .mon-card-right,
  .selected-right {
    align-items: flex-start;
  }
}

.modern-confirm-modal {
  background: #ffffff;
  width: 420px;
  max-width: 92vw;
  border-radius: 7px;
  padding: 32px 24px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1),
    0 10px 10px -5px rgba(0, 0, 0, 0.04);
  text-align: center;
  position: relative;
  overflow: hidden;
}

.confirm-icon-wrap {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.icon-circle {
  width: 64px;
  height: 64px;
  background: rgba(211, 47, 47, 0.08);
  color: #d32f2f;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
}

.confirm-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 12px 0;
}

.confirm-desc {
  font-size: 15px;
  color: #4b5563;
  margin: 0 0 28px 0;
  line-height: 1.5;
}

.confirm-desc strong {
  color: #d32f2f;
  font-weight: 600;
}

.confirm-actions {
  display: flex;
  gap: 12px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.modern-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(211, 47, 47, 0.1);
  border-left-color: #d32f2f;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

.loading-state p {
  color: #6b7280;
  font-size: 15px;
  margin: 0;
}

.error-text {
  color: #dc2626;
  font-size: 13px;
  margin-top: 6px;
  display: block;
  font-weight: 500;
}

.alert-product-changed {
  background: rgba(220, 38, 38, 0.05);
  border: 1px solid rgba(220, 38, 38, 0.15);
  border-radius: 8px;
  padding: 12px 14px;
  color: #b91c1c;
  font-size: 13px;
  line-height: 1.45;
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

/* /////////////////// MODAL LOADING /////////////// */
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

.selected-item.ngung-kinh-doanh {
  opacity: 0.5;
  filter: grayscale(1);
}

.btn-sl:disabled {
  background: #e5e7eb !important;
  color: #9ca3af !important;
  cursor: not-allowed;
  pointer-events: none;
  transform: none !important;
  box-shadow: none !important;
}
</style>
