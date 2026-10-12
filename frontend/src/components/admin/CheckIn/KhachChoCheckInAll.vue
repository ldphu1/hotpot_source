<script setup>
import { ref, computed, watch } from "vue";
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";
import { getAlldatbanByNgay1 } from "@/api/admin/datban/datBanApi";
import { getDanhSachBan } from "@/api/admin/checkin/checkinapi";
import { getAllkhuvuc } from "@/api/admin/khuvuc/khuVucApi";

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["close"]);

const loading = ref(false);

const danhSachKhach = ref([]);
const danhSachBan = ref([]);
const danhSachKhuVuc = ref([]);

const keyword = ref("");
const idKhuVuc = ref("");
const idBan = ref("");
const trangThaiLoc = ref("ALL");

const today = new Date().toISOString().slice(0, 10);
const ngayFrom = ref(today);
const ngayTo = ref(today);

const errorNgayFrom = ref("");
const errorNgayTo = ref("");

const trangThaiOptions = [
  { value: "ALL", label: "Tất cả" },
  { value: "CHUA_CHECKIN", label: "Chưa check in" },
  { value: "DA_CHECKIN", label: "Đã check in" },
  { value: "HOAN_THANH", label: "Hoàn thành" },
  { value: "DA_HUY", label: "Đã hủy" },
  { value: "HET_HAN", label: "Hết hạn" },
];

function formatDateTime(value) {
  if (!value) return "--";
  return new Date(value).toLocaleString("vi-VN");
}

function formatTime(value) {
  if (!value) return "--";
  return new Date(value).toLocaleTimeString("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
  });
}

function getIdBan(ban) {
  return ban?.idBanAn || ban?.idBan || ban?.id;
}

function getTenBan(ban) {
  return ban?.tenBanAn || ban?.tenBan || ban?.maBanAn || "--";
}

function getIdKhuVuc(ban) {
  return ban?.khuVucId || ban?.idKhuVuc || ban?.khuVuc?.id;
}

function getTenKhuVucByBan(ban) {
  return (
    ban?.tenKhuVuc ||
    ban?.khuVuc?.tenKhuVuc ||
    `Tầng ${getIdKhuVuc(ban) || "--"}`
  );
}

function getBanById(id) {
  return danhSachBan.value.find((ban) => Number(getIdBan(ban)) === Number(id));
}

function hienThiBan(item) {
  if (!Array.isArray(item?.listIdBan) || item.listIdBan.length === 0) {
    if (item?.tenBanAn || item?.tenBan) {
      return `Bàn ${item.tenBanAn || item.tenBan}`;
    }

    return "Đợi xếp bàn";
  }

  const result = item.listIdBan
    .map((id) => {
      const ban = getBanById(id);
      if (!ban) return null;

      return `${getTenKhuVucByBan(ban)} - Bàn ${getTenBan(ban)}`;
    })
    .filter(Boolean);

  return result.length ? result.join(", ") : "Đợi xếp bàn";
}

function daHetHan(item) {
  return item?.trangThai === "HET_HAN";
}

function daHuy(item) {
  return item?.trangThai === "DA_HUY";
}

function daHoanThanh(item) {
  return item?.trangThai === "HOAN_THANH";
}

function daCheckIn(item) {
  return (
    !!item?.idHoaDon ||
    item?.trangThai === "DA_CHECKIN" ||
    item?.trangThai === "HOAN_THANH"
  );
}

function getTrangThaiText(item) {
  if (daHuy(item)) return "Đã hủy";
  if (daHetHan(item)) return "Hết hạn";
  if (daHoanThanh(item)) return "Hoàn thành";
  if (daCheckIn(item)) return "Đã check in";
  return "Chưa check in";
}

function getTrangThaiClass(item) {
  if (daHuy(item)) return "status-cancel";
  if (daHetHan(item)) return "status-expired";
  if (daHoanThanh(item)) return "status-done";
  if (daCheckIn(item)) return "status-checkin";
  return "status-wait";
}

function matchKhuVuc(item) {
  if (!idKhuVuc.value) return true;

  return (item?.listIdBan || []).some((id) => {
    const ban = getBanById(id);
    return Number(getIdKhuVuc(ban)) === Number(idKhuVuc.value);
  });
}

function matchBan(item) {
  if (!idBan.value) return true;

  return (item?.listIdBan || []).some(
    (id) => Number(id) === Number(idBan.value),
  );
}

function matchTrangThai(item) {
  if (trangThaiLoc.value === "ALL") return true;

  if (trangThaiLoc.value === "CHUA_CHECKIN") {
    return !daCheckIn(item) && !daHuy(item) && !daHetHan(item);
  }

  if (trangThaiLoc.value === "DA_CHECKIN") {
    return (
      daCheckIn(item) && !daHuy(item) && !daHoanThanh(item) && !daHetHan(item)
    );
  }

  if (trangThaiLoc.value === "HOAN_THANH") {
    return daHoanThanh(item);
  }

  if (trangThaiLoc.value === "DA_HUY") {
    return daHuy(item);
  }

  if (trangThaiLoc.value === "HET_HAN") {
    return daHetHan(item);
  }

  return true;
}

const danhSachBanTheoKhuVuc = computed(() => {
  if (!idKhuVuc.value) return danhSachBan.value;

  return danhSachBan.value.filter(
    (ban) => Number(getIdKhuVuc(ban)) === Number(idKhuVuc.value),
  );
});

const danhSachLoc = computed(() => {
  const kw = keyword.value.trim().replace(/\s+/g, " ").toLowerCase();

  return danhSachKhach.value.filter((item) => {
    const matchKeyword =
      !kw ||
      String(item?.maDatBan || "")
        .trim()
        .replace(/\s+/g, " ")
        .toLowerCase()
        .includes(kw) ||
      String(item?.tenKhachHang || "")
        .trim()
        .replace(/\s+/g, " ")
        .toLowerCase()
        .includes(kw) ||
      String(item?.soDienThoai || "")
        .trim()
        .replace(/\s+/g, " ")
        .toLowerCase()
        .includes(kw);

    return (
      matchKeyword &&
      matchKhuVuc(item) &&
      matchBan(item) &&
      matchTrangThai(item)
    );
  });
});
/* ===== PAGINATION ===== */
const currentPage = ref(1);
const pageSize = ref(5);

const totalElements = computed(() => danhSachLoc.value.length);
const totalPages = computed(
  () => Math.ceil(totalElements.value / pageSize.value) || 1,
);

const danhSachHienThi = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return danhSachLoc.value.slice(start, end);
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

watch(
  [keyword, idKhuVuc, idBan, trangThaiLoc, ngayFrom, ngayTo, pageSize],
  () => {
    currentPage.value = 1;
  },
);

const thongKe = computed(() => {
  const all = danhSachKhach.value;

  return {
    tong: all.length,
    chuaCheckIn: all.filter(
      (item) => !daCheckIn(item) && !daHuy(item) && !daHetHan(item),
    ).length,
    daCheckIn: all.filter(
      (item) =>
        daCheckIn(item) &&
        !daHuy(item) &&
        !daHoanThanh(item) &&
        !daHetHan(item),
    ).length,
    hoanThanh: all.filter((item) => daHoanThanh(item)).length, // Thêm đếm Hoàn thành
    daHuy: all.filter((item) => daHuy(item)).length,
    daHetHan: all.filter((item) => daHetHan(item)).length,
  };
});

function addDays(date, days) {
  const d = new Date(date);
  d.setDate(d.getDate() + days);
  return d;
}

async function loadKhachTheoKhoangNgay() {
  try {
    errorNgayFrom.value = "";
    errorNgayTo.value = "";

    loading.value = true;

    const start = new Date(ngayFrom.value);
    const end = new Date(ngayTo.value);

    if (!ngayFrom.value) {
      errorNgayFrom.value = "Vui lòng chọn từ ngày";
      return;
    }
    if (!ngayTo.value) {
      errorNgayTo.value = "Vui lòng chọn đến ngày";
      return;
    }

    if (start > end) {
      errorNgayFrom.value = "Từ ngày không được lớn hơn đến ngày";
      errorNgayTo.value = "Từ ngày không được lớn hơn đến ngày";
      return;
    }

    const result = [];
    let current = new Date(start);

    const res = await getAlldatbanByNgay1({
      ngayFrom: start.toISOString().slice(0, 10),
      ngayTo: end.toISOString().slice(0, 10),
    });
    const data = Array.isArray(res.data) ? res.data : [];
    result.push(...data);

    current = addDays(current, 1);

    const map = new Map();

    result.forEach((item) => {
      const key = item?.id || item?.maDatBan || JSON.stringify(item);
      map.set(key, item);
    });

    danhSachKhach.value = Array.from(map.values());
  } catch (error) {
    console.error("Lỗi load khách chờ check in:", error);
    danhSachKhach.value = [];
  } finally {
    loading.value = false;
  }
}

async function loadInit() {
  try {
    loading.value = true;

    const [resBan, resKhuVuc] = await Promise.all([
      getDanhSachBan(),
      getAllkhuvuc(),
    ]);

    danhSachBan.value = Array.isArray(resBan.data) ? resBan.data : [];
    danhSachKhuVuc.value = Array.isArray(resKhuVuc.data) ? resKhuVuc.data : [];

    await loadKhachTheoKhoangNgay();
  } catch (error) {
    console.error("Lỗi load dữ liệu khách chờ:", error);
  } finally {
    loading.value = false;
  }
}

function resetBoLoc() {
  keyword.value = "";
  idKhuVuc.value = "";
  idBan.value = "";
  trangThaiLoc.value = "ALL";
  ngayFrom.value = today;
  ngayTo.value = today;
  loadKhachTheoKhoangNgay();
}

watch(
  () => props.show,
  (val) => {
    if (val) {
      loadInit();
    }
  },
  { immediate: true },
);

watch(idKhuVuc, () => {
  idBan.value = "";
});
</script>

<template>
  <Teleport to="body">
    <div v-if="show" class="overlay-khach-cho-all" @click.self="emit('close')">
      <div class="khach-cho-all-modal">
        <div class="modal-header-all">
          <div>
            <h2>Toàn bộ khách chờ check in</h2>
          </div>

          <button class="btn-close-all" @click="emit('close')">×</button>
        </div>

        <div class="stat-row">
          <div
            class="stat-card"
            :class="{ 'active-filter': trangThaiLoc === 'ALL' }"
            @click="trangThaiLoc = 'ALL'"
          >
            <span>Tổng phiếu</span>
            <b>{{ thongKe.tong }}</b>
          </div>

          <div
            class="stat-card wait"
            :class="{ 'active-filter': trangThaiLoc === 'CHUA_CHECKIN' }"
            @click="trangThaiLoc = 'CHUA_CHECKIN'"
          >
            <span>Chưa check in</span>
            <b>{{ thongKe.chuaCheckIn }}</b>
          </div>

          <div
            class="stat-card checkin"
            :class="{ 'active-filter': trangThaiLoc === 'DA_CHECKIN' }"
            @click="trangThaiLoc = 'DA_CHECKIN'"
          >
            <span>Đã check in</span>
            <b>{{ thongKe.daCheckIn }}</b>
          </div>

          <div
            class="stat-card done"
            :class="{ 'active-filter': trangThaiLoc === 'HOAN_THANH' }"
            @click="trangThaiLoc = 'HOAN_THANH'"
          >
            <span>Hoàn thành</span>
            <b>{{ thongKe.hoanThanh }}</b>
          </div>

          <div
            class="stat-card cancel"
            :class="{ 'active-filter': trangThaiLoc === 'DA_HUY' }"
            @click="trangThaiLoc = 'DA_HUY'"
          >
            <span>Đã hủy</span>
            <b>{{ thongKe.daHuy }}</b>
          </div>

          <div
            class="stat-card expired"
            :class="{ 'active-filter': trangThaiLoc === 'HET_HAN' }"
            @click="trangThaiLoc = 'HET_HAN'"
          >
            <span>Hết hạn</span>
            <b>{{ thongKe.daHetHan }}</b>
          </div>
        </div>

        <div class="filter-box">
          <div class="filter-item filter-keyword">
            <label>
              <i class="ri-search-line"></i>
              Tìm kiếm
            </label>
            <input
              v-model="keyword"
              placeholder="Tìm theo mã, tên khách hoặc SĐT..."
            />
          </div>

          <div class="filter-item">
            <label>
              <i class="ri-stack-line"></i>
              Tầng / khu vực
            </label>
            <v-select
              v-model="idKhuVuc"
              :options="danhSachKhuVuc"
              label="tenKhuVuc"
              :reduce="(item) => item.id"
              placeholder="Tất cả tầng"
              :clearable="false"
              class="modern-select"
            >
              <template #no-options="{ search, searching }">
                <template v-if="searching">
                  Không có khu vực "<b>{{ search }}</b
                  >"
                </template>
                <em v-else style="opacity: 0.5">Nhập tên khu vực để tìm...</em>
              </template>
            </v-select>
          </div>

          <div class="filter-item">
            <label>
              <i class="ri-armchair-line"></i>
              Bàn
            </label>
            <v-select
              v-model="idBan"
              :options="danhSachBanTheoKhuVuc"
              label="tenBanAn"
              :reduce="(item) => getIdBan(item)"
              placeholder="Tất cả bàn"
              :clearable="false"
              class="modern-select"
            >
              <template #option="option">
                {{ getTenKhuVucByBan(option) }} - Bàn {{ getTenBan(option) }}
              </template>

              <template #selected-option="option">
                {{ getTenKhuVucByBan(option) }} - Bàn {{ getTenBan(option) }}
              </template>

              <template #no-options="{ search, searching }">
                <template v-if="searching">
                  Không có bàn "<b>{{ search }}</b
                  >"
                </template>
                <em v-else style="opacity: 0.5">Nhập tên bàn để tìm...</em>
              </template>
            </v-select>
          </div>

          <div class="filter-item">
            <label>
              <i class="ri-checkbox-circle-line"></i>
              Trạng thái
            </label>
            <v-select
              v-model="trangThaiLoc"
              :options="trangThaiOptions"
              label="label"
              :reduce="(item) => item.value"
              placeholder="Tất cả trạng thái"
              :clearable="false"
              class="modern-select"
            >
              <template #no-options="{ search, searching }">
                <template v-if="searching">
                  Không có trạng thái "<b>{{ search }}</b
                  >"
                </template>
                <em v-else style="opacity: 0.5">Nhập trạng thái để tìm...</em>
              </template>
            </v-select>
          </div>

          <div class="filter-item">
            <label>
              <i class="ri-calendar-line"></i>
              Từ ngày
            </label>
            <input
              type="date"
              v-model="ngayFrom"
              @change="errorNgayFrom = ''"
            />
            <span class="error-text" v-if="errorNgayFrom">{{
              errorNgayFrom
            }}</span>
          </div>

          <div class="filter-item">
            <label>
              <i class="ri-calendar-check-line"></i>
              Đến ngày
            </label>
            <input type="date" v-model="ngayTo" @change="errorNgayTo = ''" />
            <span class="error-text" v-if="errorNgayTo">{{ errorNgayTo }}</span>
          </div>

          <div class="filter-actions">
            <button class="btn-primary" @click="loadKhachTheoKhoangNgay">
              <div class="btn-light-sweep"></div>
              <i class="ri-filter-3-line"></i>
              Lọc
            </button>

            <button class="btn-cancel" @click="resetBoLoc">
              <i class="ri-refresh-line"></i>
              Reset
            </button>
          </div>
        </div>

        <div class="result-header">
          <div>
            Hiển thị
            <b>{{ danhSachLoc.length }}</b>
            /
            <b>{{ danhSachKhach.length }}</b>
            phiếu
          </div>
        </div>

        <div class="table-box">
          <div v-if="loading" class="empty-state">Đang tải dữ liệu...</div>

          <div v-else-if="danhSachLoc.length === 0" class="empty-state">
            Không có khách chờ phù hợp
          </div>

          <table v-else>
            <thead>
              <tr>
                <th>Mã đặt bàn</th>
                <th>Khách hàng</th>
                <th>SĐT</th>
                <th>Thời gian đến</th>
                <th>Tầng / bàn</th>
                <th>Số người</th>
                <th>Trạng thái</th>
                <th>Ghi chú</th>
              </tr>
            </thead>

            <tbody>
              <tr v-for="item in danhSachHienThi" :key="item.id">
                <td>
                  <b class="ma-dat-ban">{{ item.maDatBan || "--" }}</b>
                </td>

                <td>
                  <div class="khach-cell">
                    <b>{{ item.tenKhachHang || "Khách vãng lai" }}</b>
                    <span v-if="item.hinhThucDat !== undefined">
                      {{
                        item.hinhThucDat === 2 ? "Đặt tại quầy" : "Đặt online"
                      }}
                    </span>
                  </div>
                </td>

                <td>
                  <i class="ri-phone-line phone-icon"></i>
                  {{ item.soDienThoai || "--" }}
                </td>

                <td>
                  <div class="time-cell">
                    <b>{{ formatTime(item.thoiGianDat) }}</b>
                    <span>{{ formatDateTime(item.thoiGianDat) }}</span>
                  </div>
                </td>

                <td>{{ hienThiBan(item) }}</td>

                <td>
                  {{
                    item.soNguoiToiDa || item.soNguoi || item.soKhach || "--"
                  }}
                </td>

                <td>
                  <span class="status-minimal" :class="getTrangThaiClass(item)">
                    {{ getTrangThaiText(item) }}
                  </span>
                </td>

                <td>
                  {{ item.ghiChu || item.ghiChuDatBan || "--" }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="d-flex justify-content-between align-items-center mt-3">
          <div class="d-flex align-items-center gap-2">
            <span style="white-space: nowrap">Hiển thị</span>
            <select
              class="form-select form-select-sm w-auto"
              v-model="pageSize"
            >
              <option :value="5">5 dòng</option>
              <option :value="10">10 dòng</option>
              <option :value="20">20 dòng</option>
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

          <div class="text-muted" style="font-size: 14px">
            Hiển thị {{ danhSachHienThi.length }} / {{ totalElements }} phiếu
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.error-text {
  color: #dc2626;
  font-size: 12px;
  margin-top: 4px;
  display: block;
  font-weight: 500;
}

.overlay-khach-cho-all,
.khach-cho-all-modal,
.modal-header-all,
.stat-row,
.stat-card,
.filter-box,
.filter-item,
.filter-actions,
.table-box,
.result-header,
button,
input,
select,
table,
div,
span,
p,
h2 {
  font-family: var(--font-family, "Inter", sans-serif);
  box-sizing: border-box;
}

.overlay-khach-cho-all {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.58);
  backdrop-filter: blur(6px);
  z-index: 1000000;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.khach-cho-all-modal {
  width: min(1280px, 96vw);
  max-height: 92vh;
  overflow-y: auto;
  overflow-x: hidden;
  background: #fff;
  border-radius: 7px;
  padding: 20px;
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.28);
  color: #0f172a;
}

.khach-cho-all-modal::-webkit-scrollbar {
  width: 8px;
}

.khach-cho-all-modal::-webkit-scrollbar-track {
  background: transparent;
}

.khach-cho-all-modal::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 999px;
}

.modal-header-all {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eef2f7;
}

.modal-header-all h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
}

.modal-header-all p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
}

.btn-close-all {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 7px;
  background: #f1f5f9;
  color: #334155;
  font-size: 24px;
  font-weight: 800;
  cursor: pointer;
  transition: all 0.25s ease;
}

.btn-close-all:hover {
  background: #fee2e2;
  color: #dc2626;
  transform: rotate(90deg);
}

.stat-row {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin: 16px 0;
}

.stat-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 7px;
  padding: 14px;
  transition: all 0.25s ease;
  cursor: pointer;
}

.stat-card.active-filter {
  transform: translateY(-3px);
  box-shadow: 0 10px 22px rgba(15, 23, 42, 0.12);
  border-color: #94a3b8;
}

.stat-card.wait.active-filter {
  border-color: #f59e0b;
  box-shadow: 0 10px 22px rgba(245, 158, 11, 0.15);
}

.stat-card.checkin.active-filter {
  border-color: #22c55e;
  box-shadow: 0 10px 22px rgba(34, 197, 94, 0.15);
}

.stat-card.cancel.active-filter {
  border-color: #ef4444;
  box-shadow: 0 10px 22px rgba(239, 68, 68, 0.15);
}

.stat-card.expired.active-filter {
  border-color: #f97316;
  box-shadow: 0 10px 22px rgba(249, 115, 22, 0.15);
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 22px rgba(15, 23, 42, 0.08);
}

.stat-card span {
  display: block;
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
}

.stat-card b {
  display: block;
  margin-top: 8px;
  font-size: 26px;
  font-weight: 900;
  color: #0f172a;
}

.stat-card.wait {
  background: #fffbeb;
  border-color: #fde68a;
}

.stat-card.checkin {
  background: #f0fdf4;
  border-color: #bbf7d0;
}

.stat-card.cancel {
  background: #fef2f2;
  border-color: #fecaca;
}

.stat-card.expired {
  background: #fff7ed;
  border-color: #fdba74;
}

.filter-box {
  display: grid;
  grid-template-columns: 2fr 1.2fr 1.4fr 1.2fr 1fr 1fr auto;
  gap: 12px;
  padding: 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 7px;
  margin-bottom: 14px;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-item label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 800;
  color: #64748b;
  text-transform: uppercase;
}

.filter-item input,
.filter-item select {
  width: 100%;
  height: 40px;
  border: 1px solid #dbe1ea;
  border-radius: 7px;
  padding: 0 12px;
  background: #fff;
  outline: none;
  color: #0f172a;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.22s ease;
}

.filter-item input:focus,
.filter-item select:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12);
}

.filter-actions {
  display: flex;
  gap: 8px;
  align-self: end;
}

.btn-primary,
.btn-cancel {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 42px;
  padding: 0 16px;
  border-radius: 7px;
  font-weight: 600;
  font-size: 14px;
  transition: 0.2s;
  text-transform: none;
  cursor: pointer;
  border: none;
  white-space: nowrap;
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

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #64748b;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 10px;
}

.result-header b {
  color: #0f172a;
}

.table-box {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 7px;
  overflow-x: auto;
  overflow-y: auto;
  height: 420px;
  display: flex;
  flex-direction: column;
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 1050px;
}

thead th {
  text-align: left;
  background: #f8fafc;
  color: #475569;
  font-size: 13px;
  font-weight: 900;
  padding: 13px 12px;
  border-bottom: 1px solid #e2e8f0;
  white-space: nowrap;
}

tbody td {
  padding: 13px 12px;
  border-bottom: 1px solid #eef2f7;
  color: #1e293b;
  font-size: 14px;
  font-weight: 500;
  vertical-align: top;
}

tbody tr:hover td {
  background: #fff7f7;
}

.ma-dat-ban {
  color: var(--color-primary);
}

.khach-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.khach-cell b {
  color: #0f172a;
}

.khach-cell span {
  color: #64748b;
  font-size: 12px;
}

.time-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.time-cell b {
  color: #0f172a;
}

.time-cell span {
  color: #64748b;
  font-size: 12px;
}

.phone-icon {
  color: #a52a2a;
  margin-right: 4px;
}

.status {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 900;
  white-space: nowrap;
}

.status-wait {
  background: rgba(251, 146, 60, 0.12);
  color: #fb923c;
}
.status-checkin {
  background: rgba(139, 92, 246, 0.12);
  color: #8b5cf6;
}
.status-cancel {
  background: rgba(239, 68, 68, 0.12);
  color: #ef4444;
}
.status-done {
  background: rgba(5, 150, 105, 0.12);
  color: #059669;
}
.status-expired {
  background: rgba(107, 114, 128, 0.12);
  color: #6b7280;
}

.empty-state {
  padding: 40px;
  text-align: center;
  color: #94a3b8;
  font-size: 15px;
  font-weight: 700;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modern-select {
  width: 100%;
  background: #fff;
}

.status-minimal {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  font-size: 13px;
  font-weight: 700;
  border-radius: 7px;
  white-space: nowrap;
}

:deep(.vs__dropdown-toggle) {
  min-height: 40px !important;
  height: 40px !important;
  border: 1px solid #dbe1ea !important;
  border-radius: 7px !important;
  padding: 0 8px 0 12px !important;
  background: #fff !important;
  display: flex !important;
  align-items: center !important;
  box-shadow: none !important;
}

:deep(.vs__selected-options) {
  position: relative !important;
  flex: 1 !important;
  min-width: 0 !important;
  height: 40px !important;
  padding: 0 !important;
  display: flex !important;
  align-items: center !important;
  overflow: hidden !important;
}

:deep(.vs__selected) {
  position: absolute !important;
  left: 0 !important;
  top: 50% !important;
  transform: translateY(-50%) !important;
  margin: 0 !important;
  padding: 0 !important;
  border: none !important;
  color: #0f172a !important;
  font-size: 14px !important;
  font-weight: 400 !important;
  line-height: 1 !important;
  max-width: calc(100% - 8px) !important;
  white-space: nowrap !important;
  overflow: hidden !important;
  text-overflow: ellipsis !important;
  pointer-events: none !important;
}

:deep(.vs__search),
:deep(.vs__search:focus) {
  width: 100% !important;
  height: 38px !important;
  line-height: 38px !important;
  margin: 0 !important;
  padding: 0 !important;
  position: relative !important;
  z-index: 2 !important;
  border: none !important;
  background: transparent !important;
  box-shadow: none !important;
  font-size: 14px !important;
  font-weight: 600 !important;
  color: #0f172a !important;
}

:deep(.vs--single.vs--selected:not(.vs--open) .vs__search) {
  opacity: 0 !important;
}

:deep(.vs--single.vs--open .vs__selected) {
  opacity: 0 !important;
}

:deep(.vs--single.vs--open .vs__search) {
  opacity: 1 !important;
}

:deep(.vs__search::placeholder) {
  color: #9ca3af !important;
  font-weight: 500 !important;
}

:deep(.vs__actions) {
  height: 40px !important;
  padding: 0 0 0 6px !important;
  display: flex !important;
  align-items: center !important;
}

:deep(.vs__open-indicator) {
  fill: #64748b !important;
  transform: scale(0.75);
}

:deep(.vs__clear) {
  display: none !important;
}

:deep(.vs--open .vs__dropdown-toggle) {
  border-color: var(--color-primary) !important;
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12) !important;
}

:deep(.vs__dropdown-menu) {
  border: 1px solid #e5e7eb !important;
  border-radius: 7px !important;
  margin-top: 8px !important;
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.14) !important;
  padding: 6px !important;
  z-index: 1000002 !important;
}

:deep(.vs__dropdown-option) {
  border-radius: 7px !important;
  padding: 9px 12px !important;
  font-size: 14px !important;
  font-weight: 600 !important;
  color: #334155 !important;
}

:deep(.vs__dropdown-option--highlight) {
  background: #fff7f7 !important;
  color: var(--color-primary) !important;
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
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
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

@media (max-width: 1300px) {
  .filter-box {
    grid-template-columns: repeat(3, 1fr);
  }

  .filter-actions {
    grid-column: span 3;
  }
}

@media (max-width: 900px) {
  .filter-box {
    grid-template-columns: 1fr;
  }

  .filter-actions {
    grid-column: auto;
  }

  .stat-row {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 640px) {
  .stat-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .overlay-khach-cho-all {
    padding: 10px;
  }

  .khach-cho-all-modal {
    padding: 14px;
    border-radius: 7px;
  }

  .modal-header-all {
    align-items: flex-start;
  }

  .modal-header-all h2 {
    font-size: 20px;
  }

  .filter-actions {
    flex-direction: column;
  }
}
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
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

:deep(body.dark-mode) .pg-btn {
  background-color: #2d3748;
  border-color: #4a5568;
  color: #e2e8f0;
}

:deep(body.dark-mode) .pg-btn:hover:not(:disabled):not(.active) {
  background-color: #4a5568;
  border-color: #ff4d4f;
  color: #ff4d4f;
}

:deep(body.dark-mode) .pg-btn:disabled {
  background-color: #1a202c;
  border-color: #2d3748;
  color: #718096;
}

:deep(body.dark-mode) .pg-btn.active {
  background-color: #ff4d4f;
  color: #ffffff;
  border-color: #ff4d4f;
}
</style>
