<script setup>
import { ref } from "vue";
import { computed } from "vue";
import { watch } from "vue";
import { onMounted } from "vue";
import { onBeforeUnmount } from "vue";
import { nextTick } from "vue";
import ChonBanDanhSach from "./ChonBanDanhSach.vue";
import { useUtils } from "@/utils/helper";
import { exportDatBan } from "@/api/admin/datban/datBanApi";
import { getAlldatban } from "@/api/admin/datban/datBanApi";
import { tuChoiXepBan } from "@/api/admin/datban/datBanApi";
import { searchDatBan } from "@/api/admin/datban/datBanApi";
import { getAllkhuvuc } from "@/api/admin/khuvuc/khuVucApi";
import { connectWebSocket, disconnectWebSocket } from "@/utils/websocket";
// Import các thư viện
import iconAdd from "@/assets/icons/add.png";
import iconExcel from "@/assets/icons/excel.png";
import iconDanhSach from "@/assets/icons/danh-sach.png";
import router from "@/router";
import vSelect from "vue-select";
import iconEdit from "@/assets/icons/edit.png";
import "vue-select/dist/vue-select.css";

// BỘ LỌC //
let isReloading = false;
const soDienThoai = ref("");
const thoiGianDat = ref("");
const selectTrangThai = ref("");
const selectTrangThaiCoc = ref("");
const selectTenKhuVuc = ref("");
const danhSachKhuVuc = ref([]);
const tatCaDatBan = ref([]);
const { notify } = useUtils();

const coTheSua = (item) => {
  return ["Chờ xác nhận", "Chờ cọc", "Đã xác nhận"].includes(item.trangThai);
};

const goEditDatBan = (id) => {
  router.push(`/admin/dat-ban/edit/${id}`);
};

//////////////////////////// TỪ CHỐI /////////////////////////
const isModalTuChoiOpen = ref(false);
const datBanDangTuChoi = ref(null);
const lyDoTuChoi = ref("");
const isSubmittingTuChoi = ref(false);
const isLoading = ref(false);

const coTheTuChoi = (item) => {
  return (
    (!item.listIdBan || item.listIdBan.length === 0) &&
    !isHuyOrHetHan(item.trangThai)
  );
};

const openTuChoi = (item) => {
  if (!coTheTuChoi(item)) return;
  datBanDangTuChoi.value = item;
  lyDoTuChoi.value = "Không còn bàn trống ở khung giờ đã đặt";
  isModalTuChoiOpen.value = true;
};

const dongModalTuChoi = () => {
  isModalTuChoiOpen.value = false;
  datBanDangTuChoi.value = null;
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

  isModalTuChoiOpen.value = false;
  isSubmittingTuChoi.value = true;
  isLoading.value = true;

  await nextTick();
  const startTime = Date.now();

  try {
    await tuChoiXepBan(datBanDangTuChoi.value.id, lyDoTuChoi.value.trim());
    notify.success("Đã từ chối xếp bàn");
    await loadDatBan();
  } catch (e) {
    notify.error(e.response?.data?.message || "Từ chối xếp bàn thất bại!");
  } finally {
    const elapsed = Date.now() - startTime;
    const minimumLoading = 3000;

    if (elapsed < minimumLoading) {
      await new Promise((r) => setTimeout(r, minimumLoading - elapsed));
    }

    isSubmittingTuChoi.value = false;
    isLoading.value = false;
    datBanDangTuChoi.value = null;
    lyDoTuChoi.value = "";
  }
};

const loadKhuVuc = async () => {
  try {
    const res = await getAllkhuvuc();
    danhSachKhuVuc.value = res.data;
  } catch (error) {
    console.log("Lỗi load khu vực", error);
  }
};

const isHuyOrHetHan = (trangThai) => {
  return (
    trangThai === "Đã hủy" || trangThai === "Hết hạn" || trangThai === "Từ chối"
  );
};

const TRANG_THAI = [
  { label: "Chờ xác nhận", value: "CHO_XAC_NHAN" },
  { label: "Đã xác nhận", value: "DA_XAC_NHAN" },
  { label: "Không cần cọc", value: "KHONG_CAN_COC" },
  { label: "Chưa cọc", value: "CHUA_COC" },
  { label: "Đã cọc", value: "DA_COC" },
  { label: "Đang phục vụ", value: "DANG_PHUC_VU" },
  { label: "Hoàn thành", value: "HOAN_THANH" },
  { label: "Đã hủy", value: "DA_HUY" },
  { label: "Hết hạn", value: "HET_HAN" },
  { label: "Từ chối", value: "TU_CHOI" },
  { label: "Chờ cọc", value: "CHO_COC" },
];

const getStatusClass = (status) => {
  switch (status) {
    case "Chờ xác nhận":
      return "status-waiting";

    case "Đã xác nhận":
      return "status-confirmed";

    case "Không cần cọc":
      return "status-no-deposit";

    case "Chưa cọc":
      return "status-not-deposit";

    case "Đã cọc":
      return "status-deposited";

    case "Đang phục vụ":
      return "status-serving";

    case "Chờ hoàn cọc":
      return "status-not-deposit";
    case "Chờ cọc":
      return "status-not-deposit";

    case "Hoàn thành":
      return "status-completed";

    case "Đã hủy":
      return "status-cancelled";

    case "Từ chối":
      return "status-cancelled";

    case "Hết hạn":
      return "status-cancelled";

    default:
      return "";
  }
};
const getTrangThaiCocClass = (trangThaiCoc) => {
  switch (trangThaiCoc) {
    case "Đã cọc":
      return "deposit-success";
    case "Đã hoàn cọc":
      return "deposit-success";

    case "Chưa cọc":
      return "deposit-pending";

    case "Mất cọc":
      return "deposit-cancel";

    case "Chờ hoàn cọc":
      return "deposit-pending";

    case "Hủy cọc":
      return "deposit-cancel";

    case "Không cần cọc":
      return "deposit-none";

    default:
      return "";
  }
};

const danhSachTrangThai = [{ label: "Tất cả", value: "" }, ...TRANG_THAI];

const getStatusCount = (statusValue) => {
  if (statusValue === "") {
    return tatCaDatBan.value.length;
  }
  return tatCaDatBan.value.filter((item) => item.trangThai === statusValue)
    .length;
};

const TRANG_THAI_COC = [
  { label: "Chưa cọc", value: "CHUA_COC" },
  { label: "Đã cọc", value: "DA_COC" },
  { label: "Chờ hoàn cọc", value: "CHO_HOAN_COC" },
  { label: "Không cần cọc", value: "KHONG_CAN_COC" },
  { label: "Mất cọc", value: "MAT_COC" },
];

const filterDatBan = async () => {
  try {
    const params = {
      soDienThoai: soDienThoai.value || null,
      tenKhuVuc: selectTenKhuVuc.value || null,
      trangThai: selectTrangThai.value || null,
      trangThaiCoc: selectTrangThaiCoc.value || null,
      thoiGianDat: thoiGianDat.value || null,
    };

    const isEmpty =
      !soDienThoai.value &&
      !selectTenKhuVuc.value &&
      !selectTrangThai.value &&
      !selectTrangThaiCoc.value &&
      !thoiGianDat.value;

    if (isEmpty) {
      await loadDatBan();
      return;
    }

    const [res, resForCount] = await Promise.all([
      searchDatBan(params),
      searchDatBan({ ...params, trangThai: null }),
    ]);

    const dataForCount =
      resForCount?.data?.data ||
      resForCount?.data?.content ||
      resForCount?.data ||
      [];
    tatCaDatBan.value = dataForCount;

    const data = res?.data?.data || res?.data?.content || res?.data || [];
    danhSachGoc.value = data.map((item) => {
      return {
        id: item.id,

        listIdBan: item.listIdBan ?? [],
        listTenBan: item.listTenBan ?? [],
        listTenKhuVuc: item.listTenKhuVuc ?? [],
        maKhachHang: item.maKhachHang ?? "",
        khachHang: item.tenKhachHang ?? "",
        sdt: item.soDienThoai ?? "",

        ban: (item.listTenBan || []).join(", "),
        tang: item.tenKhuVuc ?? "",

        ngayGio: item.thoiGianDat,
        ngayGioDisplay: formatDate(item.thoiGianDat),
        soNguoi: item.soNguoiToiDa ?? 0,

        ghiChu: item.ghiChu ?? "",

        trangThaiCoc: convertTrangThaiCoc(item.trangThaiCoc),
        hinhThucDat: convertHinhThuc(item.hinhThucDat),
        trangThai: convertTrangThai(item.trangThai),
      };
    });
    currentPage.value = 1;
  } catch (error) {
    console.log("Lỗi filter", error);
  }
};

const handleRefresh = async () => {
  soDienThoai.value = "";
  selectTrangThai.value = "";
  selectTrangThaiCoc.value = "";
  selectTenKhuVuc.value = "";
  thoiGianDat.value = "";

  currentPage.value = 1;

  await loadDatBan();

  notify.success("Đã làm mới dữ liệu");
};

const handleExport = async () => {
  try {
    const res = await exportDatBan({
      soDienThoai: soDienThoai.value || null,
      trangThai: selectTrangThai.value || null,
      trangThaiCoc: selectTrangThaiCoc.value || null,
      thoiGianDat: thoiGianDat.value || null,
      tenKhuVuc: selectTenKhuVuc.value || null,
    });

    const url = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute(
      "download",
      `dat_ban_${new Date().toISOString().slice(0, 10)}.xlsx`,
    );
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
    notify.success("Xuất excel thành công");
  } catch (e) {
    console.error("Lỗi export đặt bàn:", e);
  }
};

//Load bàn
const handleSuccessXepBan = async () => {
  isModalOpen.value = false;

  await loadDatBan();
};

///////////////////// BIẾN ĐIỀU KHIÊN MODAL
const isModalOpen = ref(false);
const selectedBooking = ref(null);

const formatDate = (date) => {
  if (!date) return "";
  return new Date(date).toLocaleString("vi-VN");
};

//////////// DỮ LIỆU ĐẶT BÀN /////////////////

const danhSachGoc = ref([]);
const loadDatBan = async () => {
  isReloading = true;

  try {
    const res = await getAlldatban();

    const data = res?.data?.data || res?.data?.content || res?.data || [];

    tatCaDatBan.value = data;

    danhSachGoc.value = data.map((item) => {
      return {
        id: item.id,

        listIdBan: item.listIdBan ?? [],
        listTenBan: item.listTenBan ?? [],
        listTenKhuVuc: item.listTenKhuVuc ?? [],
        maKhachHang: item.maKhachHang ?? "",
        khachHang: item.tenKhachHang ?? "",
        sdt: item.soDienThoai ?? "",

        ban: (item.listTenBan || []).join(", "),
        tang: item.tenKhuVuc ?? "",

        ngayGio: item.thoiGianDat,
        ngayGioDisplay: formatDate(item.thoiGianDat),
        soNguoi: item.soNguoiToiDa ?? 0,

        ghiChu: item.ghiChu ?? "",

        trangThaiCoc: convertTrangThaiCoc(item.trangThaiCoc),
        hinhThucDat: convertHinhThuc(item.hinhThucDat),
        trangThai: convertTrangThai(item.trangThai),
      };
    });
  } catch (error) {
    console.error("Lỗi load đặt bàn:", error);
    danhSachGoc.value = [];
  }
};

const convertHinhThuc = (type) => {
  if (type === 2) return "Tại quầy";
  if (type === 1) return "Trực tuyến";
  return "";
};

const convertTrangThaiCoc = (trangthaicoc) => {
  if (trangthaicoc === "DA_COC") return "Đã cọc";
  if (trangthaicoc === "CHUA_COC") return "Chưa cọc";
  if (trangthaicoc === "HUY_COC") return "Hủy cọc";
  if (trangthaicoc === "KHONG_CAN_COC") return "Không cần cọc";
  if (trangthaicoc === "MAT_COC") return "Mất cọc";

  return trangthaicoc;
};

const convertTrangThai = (status) => {
  if (status === "DA_XAC_NHAN") return "Đã xác nhận";
  if (status === "CHO_XAC_NHAN") return "Chờ xác nhận";
  if (status === "HOAN_THANH") return "Hoàn thành";
  if (status === "HET_HAN") return "Hết hạn";
  if (status === "DANG_PHUC_VU") return "Đang phục vụ";
  if (status === "DA_HUY") return "Đã hủy";
  if (status === "CHO_HOAN_COC") return "Chờ hoàn cọc";
  if (status === "TU_CHOI") return "Từ chối";
  if (status === "CHO_COC") return "Chờ cọc";

  return status;
};
// 2. LOGIC PHÂN TRANG
const pageSize = ref(5);
const currentPage = ref(1);

const tongSoTrang = computed(
  () => Math.ceil(danhSachGoc.value.length / pageSize.value) || 1,
);
const danhSachHienThi = computed(() => {
  const batDau = (currentPage.value - 1) * pageSize.value;
  return danhSachGoc.value.slice(batDau, batDau + pageSize.value);
});

// 3. CÁC HÀM XỬ LÝ
const goFirst = () => {
  currentPage.value = 1;
};
const goLast = () => {
  currentPage.value = tongSoTrang.value;
};
const goPrev = () => {
  if (currentPage.value > 1) currentPage.value--;
};
const goNext = () => {
  if (currentPage.value < tongSoTrang.value) currentPage.value++;
};

const totalItems = computed(() => danhSachGoc.value.length);

const openPickTable = (item) => {
  selectedBooking.value = item;
  isModalOpen.value = true;
};

const getBookingTypeClass = (type) => {
  if (type === "Trực tuyến") return "type-online";
  if (type === "Tại quầy") return "type-offline";
  return "type-other";
};

watch(pageSize, () => {
  currentPage.value = 1;
});

const handleWsMessage = (data) => {
  filterDatBan();
};

onMounted(() => {
  loadKhuVuc();
  loadDatBan();

  connectWebSocket(null, handleWsMessage);
});

onBeforeUnmount(() => {
  disconnectWebSocket(handleWsMessage);
});

const goDatBanDetail = (id) => {
  router.push(`/admin/dat-ban/detail/${id}`);
};

const goAddPage = () => {
  router.push("/admin/dat-ban/save");
};
</script>

<template>
  <div class="quan-ly-dat-ban">
    <div class="filter-box">
      <div class="filter-item">
        <label>Tìm kiếm</label>
        <input
          type="text"
          placeholder="Nhập số điện thoại"
          v-model="soDienThoai"
          @input="filterDatBan"
          class="custom-input"
        />
      </div>
      <div class="filter-item">
        <label>Tầng</label>
        <v-select
          class="modern-select"
          v-model="selectTenKhuVuc"
          :options="danhSachKhuVuc"
          label="tenKhuVuc"
          :reduce="(kv) => kv.tenKhuVuc"
          placeholder="Tất cả khu vực"
          :clearable="false"
          @update:modelValue="filterDatBan"
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
        <label>Trạng thái cọc</label>
        <v-select
          class="modern-select"
          v-model="selectTrangThaiCoc"
          :options="TRANG_THAI_COC"
          :reduce="(item) => item.value"
          placeholder="Tất cả trạng thái"
          :clearable="false"
          @update:modelValue="filterDatBan"
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
      <!-- Trạng thái được chuyển thành thanh tab nằm ngang ở phần danh sách bên dưới -->
      <div class="filter-item">
        <label>Ngày</label>
        <div class="input-with-icon">
          <input
            type="date"
            v-model="thoiGianDat"
            @change="filterDatBan"
            class="custom-input"
          />
        </div>
      </div>
    </div>

    <div class="box">
      <!-- HEADER TABLE -->
      <div class="box-header box-header-flex">
        <h3 class="table-title">
          <span class="icon-wrapper-small">
            <img :src="iconDanhSach" class="icon-title-small" />
          </span>
          Danh sách đặt bàn
        </h3>

        <div class="header-actions">
          <!-- Thêm -->
          <button class="add-btn" @click="goAddPage">
            <img :src="iconAdd" alt="" />
            <span>Thêm</span>
          </button>
          <!-- Excel -->
          <button class="excel-btn" @click="handleExport()">
            <img :src="iconExcel" alt="" />
            <span>Xuất Excel</span>
          </button>

          <!-- Reload -->
          <button class="refresh-btn" @click="handleRefresh()">
            <img src="@/assets/icons/refresh.png" alt="" />
            <span>Làm mới</span>
          </button>
        </div>
      </div>
      <!-- Thanh bộ lọc trạng thái đặt bàn nằm ngang -->
      <div class="status-filter-tabs">
        <button
          v-for="status in danhSachTrangThai"
          :key="status.value"
          class="status-tab-btn"
          :class="{ active: selectTrangThai === status.value }"
          @click="
            selectTrangThai = status.value;
            filterDatBan();
          "
        >
          {{ status.label }}
          <span class="status-count-badge">
            {{ getStatusCount(status.value) }}
          </span>
        </button>
      </div>

      <div class="table-content">
        <table class="table-main">
          <thead>
            <tr>
              <th>STT</th>
              <th>Khách hàng</th>
              <th>SĐT</th>
              <th>Vị trí</th>
              <th>Ngày giờ</th>
              <th>Số khách</th>
              <th>Hình thức</th>

              <th>Trạng thái cọc</th>
              <th>Trạng thái</th>
              <th style="text-align: center">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in danhSachHienThi" :key="item.id">
              <td>{{ index + 1 }}</td>
              <td class="text-bold">{{ item.khachHang }}</td>
              <td>{{ item.sdt || "Không xác định" }}</td>
              <td>
                <div class="cell-table-wrap">
                  <template v-if="item.listIdBan?.length > 0">
                    <div class="badge-group">
                      <span
                        v-for="(tenBan, idx) in item.listTenBan"
                        :key="tenBan"
                        class="table-tag"
                      >
                        B{{ tenBan }} -
                        {{ item.listTenKhuVuc?.[idx] ?? item.tang }}
                      </span>
                    </div>
                  </template>

                  <template v-else>
                    <div
                      class="btn-select-table"
                      :class="{ disabled: isHuyOrHetHan(item.trangThai) }"
                      @click="
                        !isHuyOrHetHan(item.trangThai) && openPickTable(item)
                      "
                      v-tooltip="
                        item.trangThai === 'Từ chối'
                          ? 'Phiếu đã bị từ chối'
                          : isHuyOrHetHan(item.trangThai)
                          ? 'Phiếu đã bị hủy'
                          : ''
                      "
                    >
                      <span class="plus-icon">+</span>
                      <span class="text">Chọn bàn</span>
                    </div>
                  </template>
                </div>
              </td>
              <td>{{ item.ngayGioDisplay }}</td>
              <td>{{ item.soNguoi }}</td>
              <td>
                <span
                  :class="[
                    'booking-type',
                    getBookingTypeClass(item.hinhThucDat),
                  ]"
                >
                  {{ item.hinhThucDat }}
                </span>
              </td>

              <td>
                <span
                  class="status-text"
                  :class="getTrangThaiCocClass(item.trangThaiCoc)"
                >
                  {{ convertTrangThaiCoc(item.trangThaiCoc) }}
                </span>
              </td>

              <td>
                <span
                  class="status-text"
                  :class="getStatusClass(item.trangThai)"
                >
                  {{ item.trangThai }}
                </span>
              </td>
              <td class="text-center">
                <div class="actions">
                  <button
                    class="btn-action-icon"
                    @click="goDatBanDetail(item.id)"
                    v-tooltip="'Xem chi tiết'"
                  >
                    <img
                      src="@/assets/icons/view.png"
                      class="img-icon-action"
                    />
                  </button>

                  <button
                    v-if="coTheSua(item)"
                    class="btn-action-icon"
                    @click="goEditDatBan(item.id)"
                    v-tooltip="'Cập nhật phiếu'"
                  >
                    <img :src="iconEdit" class="img-icon-action" />
                  </button>

                  <button
                    class="btn-action-icon btn-reject-icon"
                    :class="{ disabled: !coTheTuChoi(item) }"
                    :disabled="!coTheTuChoi(item)"
                    @click="openTuChoi(item)"
                    v-tooltip="
                      coTheTuChoi(item)
                        ? 'Từ chối xếp bàn'
                        : 'Chỉ áp dụng cho phiếu chưa xếp bàn'
                    "
                  >
                    <i class="fa-solid fa-ban"></i>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="danhSachHienThi.length === 0">
              <td colspan="10" class="empty-table">
                🔍 Không tìm thấy phiếu đặt bàn phù hợp
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <!-- PAGINATION -->
      <div class="d-flex justify-content-between align-items-center mt-3">
        <div class="d-flex align-items-center gap-2">
          <span>Hiển thị</span>
          <select
            class="form-select form-select-sm w-auto"
            v-model="pageSize"
            @change="currentPage = 1"
          >
            <option :value="5">5 dòng</option>
            <option :value="10">10 dòng</option>
            <option :value="20">20 dòng</option>
          </select>
        </div>

        <nav class="pagination-container">
          <ul class="pagination-premium">
            <li>
              <button
                class="nav-action-btn"
                :disabled="currentPage === 1"
                @click="goFirst"
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
                :disabled="currentPage === 1"
                @click="goPrev"
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
                <span class="current-number">{{ currentPage }}</span>
                <span class="divider">/</span>
                <span class="total-number">{{ tongSoTrang }}</span>
              </div>
            </li>

            <li>
              <button
                class="nav-action-btn"
                :disabled="currentPage === tongSoTrang"
                @click="goNext"
                aria-label="Next page"
              >
                <!-- Icon một mũi tên phải -->
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
                :disabled="currentPage === tongSoTrang"
                @click="goLast"
                aria-label="Last page"
              >
                <!-- Icon hai mũi tên phải thanh mảnh -->
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
        <div class="text-muted">
          Hiển thị {{ danhSachHienThi.length }} / {{ totalItems }} phiếu đặt bàn
        </div>
      </div>
    </div>

    <ChonBanDanhSach
      v-if="isModalOpen"
      :bookingData="selectedBooking"
      @close="isModalOpen = false"
      @success="handleSuccessXepBan"
    />
    <Teleport to="body">
      <div
        v-if="isModalTuChoiOpen"
        class="modal-overlay-reject blur-overlay"
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
                <b>{{ datBanDangTuChoi?.khachHang || "Khách lẻ" }}</b></span
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
            <button
              class="btn-modern btn-danger"
              :disabled="isSubmittingTuChoi"
              @click="xacNhanTuChoiBan"
            >
              <i v-if="!isSubmittingTuChoi" class="bi bi-check2-all"></i>
              {{ isSubmittingTuChoi ? "Đang xử lý..." : "Xác nhận" }}
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
    </Teleport>
  </div>
</template>

<style scoped>
.btn-action-icon {
  width: 32px;
  height: 32px;

  display: inline-flex;
  align-items: center;
  justify-content: center;

  border: none;
  border-radius: 50px;

  background: #f2f5f7;

  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-action-icon:hover {
  background: #eef2ff;
  transform: translateY(-1px);
}

.img-icon-action {
  width: 17px;
  height: 17px;
  object-fit: contain;
}
.text-center {
  text-align: center;
}
.table-main {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed; /* cột đều, gọn */
  background: white;
  border-radius: 8px;
  overflow: hidden;
  font-weight: 600;
}

.table-main th,
.table-main td {
  vertical-align: middle;
  text-align: center;
  padding: 12px 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.table-main th {
  background: #e9f2fb;
  font-size: 14px;
  color: #333;
  font-weight: 700;
  font-size: 15px;
}

.table-main td {
  border-bottom: 1px solid #f0f0f0;
  font-size: 13px;
}
/* Cố định cột của bảng đặt bàn */
.table-main th:nth-child(1),
.table-main td:nth-child(1) {
  width: 50px;
  text-align: center;
}

.table-main th:nth-child(2),
.table-main td:nth-child(2) {
  width: 130px;
  text-align: left;
}

.table-main th:nth-child(3),
.table-main td:nth-child(3) {
  width: 100px;
  text-align: center;
}

.table-main th:nth-child(4),
.table-main td:nth-child(4) {
  width: 130px;
  text-align: center;
}

.table-main th:nth-child(5),
.table-main td:nth-child(5) {
  width: 140px;
  text-align: center;
}

.table-main th:nth-child(6),
.table-main td:nth-child(6) {
  width: 90px;
  text-align: center;
}

.table-main th:nth-child(7),
.table-main td:nth-child(7) {
  width: 110px;
  text-align: center;
}

.table-main th:nth-child(8),
.table-main td:nth-child(8) {
  width: 130px;
  text-align: center;
}

.table-main th:nth-child(9),
.table-main td:nth-child(9) {
  width: 130px;
  text-align: center;
}

.table-main th:nth-child(10),
.table-main td:nth-child(10) {
  width: 130px;
  text-align: center !important;
  overflow: visible !important;
  text-overflow: clip !important;
}
.status-text {
  display: inline-flex;
  align-items: center;
  justify-content: center;

  min-width: 110px;

  padding: 4px 12px;
  border-radius: 4px;

  font-size: 13px;
  font-weight: 500;

  white-space: nowrap;
}

/* Đã cọc */
.deposit-success {
  background: rgba(34, 197, 94, 0.1);
  color: #22c55e;
}

/* Chưa cọc */
.deposit-pending {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

/* Hủy cọc */
.deposit-cancel {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

/* Không cần cọc */
.deposit-none {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}
/* Chờ xác nhận */
.status-waiting {
  background: rgba(245, 158, 11, 0.12);
  color: #f59e0b;
}

/* Đã xác nhận */
.status-confirmed {
  background: rgba(59, 130, 246, 0.12);
  color: #3b82f6;
}

/* Không cần cọc */
.status-no-deposit {
  background: rgba(14, 165, 233, 0.12);
  color: #0ea5e9;
}

/* Chưa cọc */
.status-not-deposit {
  background: rgba(251, 146, 60, 0.12);
  color: #fb923c;
}

/* Đã cọc */
.status-deposited {
  background: rgba(34, 197, 94, 0.12);
  color: #22c55e;
}

/* Đang phục vụ */
.status-serving {
  background: rgba(139, 92, 246, 0.12);
  color: #8b5cf6;
}

/* Hoàn thành */
.status-completed {
  background: rgba(16, 185, 129, 0.12);
  color: #10b981;
}

/* Đã hủy */
.status-cancelled {
  background: rgba(239, 68, 68, 0.12);
  color: #ef4444;
}

.actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
}

.icon-action {
  width: 18px;
  height: 18px;
  cursor: pointer;
  display: inline-block !important;
}
.cell-table-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 35px;
}

.badge-group {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: center;
}

.table-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;

  padding: 4px 10px;
  min-height: 26px;

  background: #eff6ff;
  color: #fd0606;

  border-radius: 4px;
  border: none;

  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
}

.floor-text {
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
}
/* Duplicate style cell-table-wrap removed */

/* Nút chọn bàn khi chưa có dữ liệu */
.btn-select-table {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #c93030;
  font-weight: 600;
  font-size: 14px;
  transition: opacity 0.2s;
}

.btn-select-table:hover {
  opacity: 0.8;
}
.btn-select-table.disabled {
  cursor: not-allowed;
  opacity: 0.5;
  color: #94a3b8;
  pointer-events: auto; /* vẫn cho hover để hiện tooltip */
}

.btn-select-table.disabled .plus-icon {
  background: #94a3b8;
}

.btn-select-table.disabled:hover {
  opacity: 0.5;
}
.plus-icon {
  background: #d92c2c;
  color: #fff;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  line-height: 1;
}

.text-placeholder {
  color: #cbd5e1;
}

.box {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 16px;
  margin-bottom: 20px;
  border: 1px solid #eee;
}

.box-header {
  margin-bottom: 16px;
}

.box-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.booking-type {
  width: 90px;
  padding: 4px 0;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block;
  text-align: center;
  box-sizing: border-box;
}

.type-other {
  color: #ff9800;
  border: 1px solid rgba(255, 152, 0, 0.25);
  background: rgba(255, 152, 0, 0.08);
}

/* Online */
.type-online {
  color: #0d6efd;
  border: 1px solid rgba(13, 110, 253, 0.25);
  background: rgba(13, 110, 253, 0.08);
}

/* Offline */
.type-offline {
  color: #6c757d;
  border: 1px solid rgba(108, 117, 125, 0.25);
  background: rgba(108, 117, 125, 0.08);
}
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
/* RESET & BIẾN */
/* .quan-ly-dat-ban {
  padding: 24px;
  font-family: var(--font-family);
} */
.main-title {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
}
.sub-title {
  font-size: 13px;
  color: #888;
  margin-bottom: 20px;
}

/* FILTER BOX (Hàng ngang + Viền xanh Cyan) */
.filter-box {
  background: rgb(255, 255, 255);
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 24px;
  display: flex;
  align-items: flex-end;
  gap: 12px;
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
}
.custom-input:hover {
  border-color: #c8d0dc;
}
.custom-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12);
}
.input-with-icon {
  position: relative;
}
.calendar-icon-blue {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
}

/* BUTTON SEARCH CYAN */
.btn.search {
  background-color: #00d1f3;
  border: none;
  border-radius: 8px;
  height: 38px;
  padding: 0 20px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  white-space: nowrap;
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

:deep(.vs__dropdown-toggle:hover) {
  border-color: #c8d0dc !important;
}

:deep(.vs--open .vs__dropdown-toggle) {
  border-color: var(--color-primary) !important;
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12) !important;
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
  color: #eb6464 !important;
}

:deep(.vs__dropdown-option--selected) {
  background: #f3f5f9 !important;
  color: #fc0101 !important;
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
.empty-table {
  text-align: center;
  color: #6b7280;
  padding: 20px;
  font-style: italic;
}

/* ===== BỘ LỌC TRẠNG THÁI OUTLINE PILLS (CÓ VIỀN TỪNG Ô) ===== */
.status-filter-tabs {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  overflow-x: auto;
  scrollbar-width: none; /* Ẩn scrollbar */
  padding: 4px 0;
}

.status-filter-tabs::-webkit-scrollbar {
  display: none;
}

.status-tab-btn {
  display: inline-flex;
  align-items: center;
  padding: 8px 16px;
  font-size: 13.5px;
  font-weight: 600;
  color: #64748b;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 999px; /* Dạng viên thuốc tròn trịa */
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s ease;
}

.status-tab-btn:hover {
  color: #1e293b;
  border-color: #cbd5e1;
  background: #f8fafc;
}

.status-tab-btn.active {
  color: #d01414;
  border-color: #d01414;
  background: #fdf2f2;
  font-weight: 700;
}

.status-count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  font-size: 11px;
  font-weight: 700;
  line-height: 1;
  border-radius: 999px; /* Huy hiệu cũng bo tròn */
  margin-left: 6px;
  background: #f1f5f9;
  color: #64748b;
  transition: all 0.2s ease;
}

.status-tab-btn:hover .status-count-badge {
  background: #cbd5e1;
  color: #0f172a;
}

.status-tab-btn.active .status-count-badge {
  background: #d01414;
  color: #ffffff;
}

.table-title {
  display: flex;
  align-items: center;
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}

.icon-wrapper-small {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background-color: #f0eaea;
  border-radius: 8px;
  margin-right: 10px;
}

.icon-title-small {
  width: 18px;
  height: 18px;
  display: block;
}

/* //////////////// ACTION  */
.add-btn {
  display: flex;
  align-items: center;
  gap: 10px;

  padding: 12px 20px;
  border: none;
  border-radius: 7px;

  background: #eef2ff;
  color: #d73737;

  font-size: 15px;
  font-weight: 600;

  cursor: pointer;
  transition: all 0.25s ease;
}

.add-btn img {
  width: 18px;
  height: 18px;
  object-fit: contain;
}

.add-btn:hover {
  background: #dfe7ff;
  transform: translateY(-1px);
}

.add-btn:active {
  transform: scale(0.98);
}

.excel-btn {
  display: flex;
  align-items: center;
  gap: 10px;

  padding: 12px 18px;

  border: none;
  border-radius: 7px;

  background: #d4f0dd;
  color: #16a34a;

  font-size: 14px;
  font-weight: 600;

  cursor: pointer;
  transition: all 0.25s ease;
}

.excel-btn img {
  width: 18px;
  height: 18px;
  object-fit: contain;
}

.excel-btn:hover {
  background: #dcfce7;
  transform: translateY(-1px);
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8px;

  padding: 9px 14px;

  border: none;
  border-radius: 12px;

  background: #f3f4f6;
  color: #ca2525;

  font-size: 13px;
  font-weight: 600;

  cursor: pointer;
  transition: all 0.25s ease;
}

.refresh-btn img {
  width: 16px;
  height: 16px;
  object-fit: contain;
}

.refresh-btn:hover {
  background: #e5e7eb;
  transform: translateY(-1px);
}

.refresh-btn:hover img {
  transform: rotate(180deg);
  transition: transform 0.4s ease;
}
.add-btn,
.excel-btn,
.refresh-btn {
  padding: 9px 14px;
  gap: 8px;
  font-size: 13px;
  border-radius: 7px;
}

.add-btn img,
.excel-btn img,
.refresh-btn img {
  width: 16px;
  height: 16px;
}

/* ///////////////////////// PHÂN TRANG ///////////// */
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
  -webkit-backdrop-filter: blur(12px);
  padding: 6px;
  border-radius: 10px;
  border: 1px solid rgba(241, 245, 249, 0.8);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02),
    0 10px 15px -3px rgba(15, 23, 42, 0.05);
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
  border-radius: 12px;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  outline: none;
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

.page-indicator-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  height: 36px;
  padding: 0 16px;
  background: #f3efef;
  border-radius: 7px;
  color: #ffffff;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  box-shadow: 0 4px 12px rgba(174, 193, 237, 0.15);
  user-select: none;
}

.current-number {
  font-weight: 600;
  font-size: 0.875rem;
  color: #f31e1e;
}

.divider {
  font-size: 0.75rem;
  color: #f31111;
  margin: 0 2px;
}

.total-number {
  font-weight: 500;
  font-size: 0.875rem;
  color: #94a3b8;
}

.nav-action-btn:disabled {
  opacity: 0.25;
  cursor: not-allowed;
  color: #94a3b8;
}
.nav-action-btn svg {
  transition: transform 0.2s ease;
}

.nav-action-btn:hover:not(:disabled) svg {
  transform: stroke-width(2.5);
}

/* ===== RESPONSIVE ===== */
@media (max-width: 1024px) {
  .filter-box {
    flex-wrap: wrap !important;
  }

  .filter-item {
    flex: 1 1 calc(50% - 12px) !important;
    min-width: 140px !important;
  }

  .box-header-flex {
    flex-wrap: wrap !important;
    gap: 10px !important;
  }

  .header-actions {
    flex-wrap: wrap !important;
    gap: 8px !important;
  }
}

@media (max-width: 768px) {
  .filter-box {
    flex-direction: column !important;
    align-items: stretch !important;
    padding: 12px !important;
    gap: 10px !important;
  }

  .filter-item {
    flex: 1 1 100% !important;
    width: 100% !important;
  }

  .box-header-flex {
    flex-direction: column !important;
    align-items: flex-start !important;
    gap: 10px !important;
  }

  .table-title {
    font-size: 17px !important;
  }

  .header-actions {
    width: 100% !important;
    justify-content: flex-start !important;
    gap: 8px !important;
  }

  .add-btn,
  .excel-btn,
  .refresh-btn {
    padding: 8px 12px !important;
    font-size: 12px !important;
    gap: 6px !important;
  }

  .add-btn img,
  .excel-btn img,
  .refresh-btn img {
    width: 14px !important;
    height: 14px !important;
  }

  .status-filter-tabs {
    gap: 6px !important;
  }

  .status-tab-btn {
    padding: 6px 10px !important;
    font-size: 12px !important;
  }

  .table-content {
    display: block !important;
    overflow-x: auto !important;
    -webkit-overflow-scrolling: touch !important;
  }

  .table-main {
    table-layout: auto !important;
    min-width: 750px !important;
  }

  .d-flex.justify-content-between {
    flex-direction: column !important;
    align-items: flex-start !important;
    gap: 10px !important;
  }

  .status-text {
    min-width: 80px !important;
    font-size: 11px !important;
    padding: 3px 8px !important;
  }
}

@media (max-width: 480px) {
  .filter-box {
    padding: 10px !important;
  }

  .table-title {
    font-size: 15px !important;
  }

  .header-actions {
    gap: 6px !important;
  }

  .add-btn span,
  .excel-btn span,
  .refresh-btn span {
    display: none !important;
  }

  .add-btn,
  .excel-btn,
  .refresh-btn {
    padding: 8px !important;
  }
}

.btn-reject-icon {
  color: #8b1e1e;
}

.btn-reject-icon i {
  font-size: 15px;
}

.btn-reject-icon:hover:not(.disabled) {
  background: #fdf2f2;
}

.btn-reject-icon.disabled {
  opacity: 0.4;
  filter: grayscale(0.6);
  cursor: not-allowed !important;
  pointer-events: none;
}

.modal-overlay-reject {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 4000;
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

.btn-modern {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-modern.btn-cancel {
  background: #f1f5f9;
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.btn-modern.btn-cancel:hover {
  background: #e2e8f0;
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

.btn-modern.btn-danger:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
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
  border-top-color: #f03232;
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
</style>
