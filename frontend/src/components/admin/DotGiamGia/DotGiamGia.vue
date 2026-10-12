<script setup>
import { onMounted, ref, computed, watch } from "vue";
import { useRouter } from "vue-router";
import { useUtils } from "@/utils/helper";
import { Ticket } from "lucide-vue-next";
/* ICON */
import iconAdd from "@/assets/icons/add.png";
import iconEdit from "@/assets/icons/edit.png";
import iconRefresh from "@/assets/icons/refresh.png";
import iconView from "@/assets/icons/eye.png";
import { Trash2 } from "lucide-vue-next";
import iconExcel from "@/assets/icons/excel.png";
import iconDanhSach from "@/assets/icons/danh-sach.png";
/* API */
import {
  filterDotGiamGia,
  toggleTrangThaiDotGiamGia,
  getDetailDotGiamGia,
} from "@/api/admin/dotgiamgia/dotGiamGiaApi";

import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";

const { notify, nprogress } = useUtils();
const router = useRouter();

/* DATA */
const danhSach = ref([]);

/* SELECTION */
const selectedIds = ref([]);
const isAllSelected = computed({
  get() {
    return (
      danhSachHienThi.value.length > 0 &&
      danhSachHienThi.value.every((item) => selectedIds.value.includes(item.id))
    );
  },
  set(value) {
    if (value) {
      danhSachHienThi.value.forEach((item) => {
        if (!selectedIds.value.includes(item.id)) {
          selectedIds.value.push(item.id);
        }
      });
    } else {
      const idsToRemove = danhSachHienThi.value.map((item) => item.id);
      selectedIds.value = selectedIds.value.filter(
        (id) => !idsToRemove.includes(id),
      );
    }
  },
});

/* MODAL */
const showModal = ref(false);
const selectedItem = ref(null);

/* FILTER */
const keyword = ref("");
const trangThai = ref(null);
const tuNgay = ref("");
const denNgay = ref("");
const minGiaTri = ref(0);
const maxGiaTri = ref(100);

const controlMin = () => {
  if (minGiaTri.value > maxGiaTri.value) {
    minGiaTri.value = maxGiaTri.value;
  }
};

const controlMax = () => {
  if (maxGiaTri.value < minGiaTri.value) {
    maxGiaTri.value = minGiaTri.value;
  }
};

const trackStyle = computed(() => {
  const left = minGiaTri.value;
  const right = 100 - maxGiaTri.value;
  return {
    left: `${left}%`,
    right: `${right}%`,
  };
});

/* PAGINATION */
const currentPage = ref(1);
const pageSize = ref(5);

const trangThaiText = (status) => {
  switch (status) {
    case 0:
      return "Chưa bắt đầu";

    case 1:
      return "Đang diễn ra";

    case 2:
      return "Đã kết thúc";

    case 3:
      return "Dừng hoạt động";

    default:
      return "Không xác định";
  }
};

const trangThaiClass = (status) => {
  switch (status) {
    case 0:
      return "status-upcoming";
    case 1:
      return "status-active";
    case 2:
      return "status-ended";
    case 3:
      return "status-stopped";
    default:
      return "";
  }
};
const autoTrangThai = (item) => {
  const now = new Date();
  const start = new Date(item.ngayBatDau);
  const end = new Date(item.ngayKetThuc);

  if (now < start) return 0;
  if (now > end) return 2;
  if (!item.active) return 3;
  return 1;
};

/* FETCH */
const fetchData = async () => {
  nprogress.start();
  try {
    const params = {};
    if (keyword.value?.trim()) params.keyword = keyword.value;
    if (trangThai.value !== null) params.trangThai = trangThai.value;

    const res = await filterDotGiamGia(params);

    danhSach.value = res.data
      .map((item) => ({
        ...item,
        trangThai: autoTrangThai(item),
        active: item.active,
      }))
      .sort((a, b) => b.id - a.id);
  } catch (e) {
    notify.error("Không tải được dữ liệu");
  } finally {
    nprogress.done();
  }
};

onMounted(fetchData);

/* WATCH FILTER */
watch([keyword, trangThai], () => {
  currentPage.value = 1;
  fetchData();
});

watch([tuNgay, denNgay, minGiaTri, maxGiaTri], () => {
  currentPage.value = 1;
});

/* PAGINATION */
const danhSachFilter = computed(() => {
  let result = danhSach.value;

  if (trangThai.value !== null) {
    result = result.filter((i) => i.trangThai === trangThai.value);
  }

  if (tuNgay.value) {
    const filterStart = new Date(tuNgay.value);
    filterStart.setHours(0, 0, 0, 0);
    result = result.filter((i) => {
      if (!i.ngayBatDau) return false;
      const itemStart = new Date(i.ngayBatDau);
      return itemStart >= filterStart;
    });
  }

  if (denNgay.value) {
    const filterEnd = new Date(denNgay.value);
    filterEnd.setHours(23, 59, 59, 999);
    result = result.filter((i) => {
      if (!i.ngayKetThuc) return false;
      const itemEnd = new Date(i.ngayKetThuc);
      return itemEnd <= filterEnd;
    });
  }

  result = result.filter((i) => {
    const val = i.giaTriGiam || 0;
    return val >= minGiaTri.value && val <= maxGiaTri.value;
  });

  return result;
});

const totalItems = computed(() => danhSachFilter.value.length);

const totalPages = computed(() =>
  Math.max(1, Math.ceil(totalItems.value / pageSize.value)),
);

const danhSachHienThi = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return danhSachFilter.value.slice(start, start + pageSize.value);
});

/* NAV */
const goFirst = () => (currentPage.value = 1);
const goPrev = () => {
  if (currentPage.value > 1) currentPage.value--;
};
const goNext = () => {
  if (currentPage.value < totalPages.value) currentPage.value++;
};
const goLast = () => (currentPage.value = totalPages.value);

/* ACTION */
const suaDot = (item) => {
  if (item.trangThai === 2) return notify.error("Đợt đã kết thúc");
  router.push(`/admin/dot-giam-gia/update/${item.id}`);
};
const xemChiTiet = (item) => {
  router.push(`/admin/dot-giam-gia/detail/${item.id}`);
};

const toggleTrangThai = async (item) => {
  if (item.trangThai !== 1 && item.trangThai !== 3) {
    return notify.error("Chỉ được bật/tắt khi đang diễn ra hoặc dừng");
  }

  await toggleTrangThaiDotGiamGia(item.id);
  await fetchData();
  notify.success("Đổi trạng thái thành công");
};

/* HELPERS */
const formatMoney = (v) => Number(v).toLocaleString("vi-VN");

const tinhGiaSauGiam = (gia, dot) => {
  if (!dot || !dot.giaTriGiam) return gia;
  return gia - (gia * dot.giaTriGiam) / 100;
};

const formatDate = (d) => {
  if (!d) return "";
  const date = new Date(d);
  return (
    date.toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" }) +
    " " +
    date.toLocaleDateString("vi-VN")
  );
};

const goToAdd = () => {
  router.push("/admin/dot-giam-gia/add");
};

const reloadData = async () => {
  keyword.value = "";
  trangThai.value = null;
  tuNgay.value = "";
  denNgay.value = "";
  minGiaTri.value = 0;
  maxGiaTri.value = 100;
  currentPage.value = 1;
  selectedIds.value = [];
  await fetchData();
};
</script>

<template>
  <div class="pgg-page">
    <!-- HEADER -->
    <div class="box">
      <div class="box-header">
        <h2 class="main-title">
          <span class="icon-wrapper">
            <Ticket :size="26" stroke-width="2.2" />
          </span>
          Quản Lý Đợt Giảm Giá
        </h2>
        <p>Quản lý đợt giảm giá trong hệ thống</p>
      </div>
    </div>

    <!-- FILTER -->
    <div class="box filter-card">
      <div class="box-header">
        <h3>Bộ lọc tìm kiếm</h3>
      </div>

      <div class="filter-row">
        <!-- Tìm kiếm -->
        <div class="filter-item search-field">
          <label class="filter-label">Tìm kiếm</label>
          <input
            v-model="keyword"
            placeholder="Nhập mã hoặc tên để tìm..."
            class="filter-input"
          />
        </div>

        <!-- Trạng thái -->
        <div class="filter-item status-field">
          <label class="filter-label">Trạng thái</label>
          <v-select
            v-model="trangThai"
            :options="[
              { label: 'Tất cả', value: null },
              { label: 'Chưa bắt đầu', value: 0 },
              { label: 'Đang diễn ra', value: 1 },
              { label: 'Dừng hoạt động', value: 3 },
              { label: 'Đã kết thúc', value: 2 },
            ]"
            :reduce="(opt) => opt.value"
            :clearable="false"
            placeholder="Chọn trạng thái"
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

      <div class="filter-row second-row">
        <!-- Từ ngày -->
        <div class="filter-item date-field">
          <label class="filter-label">Từ ngày</label>
          <input type="date" v-model="tuNgay" class="filter-input date-input" />
        </div>

        <!-- Đến ngày -->
        <div class="filter-item date-field">
          <label class="filter-label">Đến ngày</label>
          <input
            type="date"
            v-model="denNgay"
            class="filter-input date-input"
          />
        </div>

        <!-- Giảm giá -->
        <div class="filter-item discount-field">
          <span class="discount-label">Giảm giá:</span>
          <div class="range-slider-container">
            <div class="range-slider">
              <div class="slider-track" :style="trackStyle"></div>
              <input
                type="range"
                min="0"
                max="100"
                v-model.number="minGiaTri"
                @input="controlMin"
              />
              <input
                type="range"
                min="0"
                max="100"
                v-model.number="maxGiaTri"
                @input="controlMax"
              />
            </div>
            <span class="range-value">{{ minGiaTri }}% - {{ maxGiaTri }}%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- TABLE -->
    <div class="box">
      <div class="box-header box-header-flex">
        <h3 class="table-title">
          <span class="icon-wrapper-small">
            <img :src="iconDanhSach" class="icon-title-small" />
          </span>
          Danh sách đợt giảm giá
        </h3>
        <div class="header-actions">
          <button class="add-btn" @click="goToAdd">
            <img :src="iconAdd" />
            <span>Thêm</span>
          </button>
          <!-- Excel -->

          <button class="refresh-btn" @click="reloadData">
            <img :src="iconRefresh" alt="" />
            <span>Làm mới</span>
          </button>
        </div>
      </div>

      <table class="pgg-table">
        <thead>
          <tr>
            <th style="width: 6%">STT</th>
            <th style="width: 10%">Mã</th>
            <th style="width: 20%; text-align: left">Tên đợt</th>
            <th style="width: 12%; text-align: left">Chi tiết giảm</th>
            <th style="width: 20%; text-align: left">Thời gian</th>
            <th style="width: 15%; text-align: left">Trạng thái</th>
            <th style="width: 13%">Thao tác</th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="(item, index) in danhSachHienThi" :key="item.id">
            <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>

            <td class="fw">{{ item.maDot }}</td>

            <td class="fw" style="text-align: left">{{ item.tenDot }}</td>

            <!-- FIX Ở ĐÂY -->
            <td style="text-align: left">
              <span class="discount-badge percent">
                {{ item.giaTriGiam }}%
              </span>
            </td>

            <td class="time-cell" style="text-align: left">
              <div class="time-range">
                <div class="time-start">
                  Bắt đầu: {{ formatDate(item.ngayBatDau) }}
                </div>
                <div class="time-end">
                  Kết thúc: {{ formatDate(item.ngayKetThuc) }}
                </div>
              </div>
            </td>

            <td style="text-align: left">
              <span class="status-text" :class="trangThaiClass(item.trangThai)">
                {{ trangThaiText(item.trangThai) }}
              </span>
            </td>
            <td class="text-center">
              <div class="action-cell">
                <button
                  class="btn-action-icon"
                  @click="xemChiTiet(item)"
                  v-tooltip="'Xem chi tiết'"
                >
                  <img :src="iconView" class="img-icon-action" />
                </button>

                <button
                  class="btn-action-icon"
                  @click="suaDot(item)"
                  :class="{ disabled: item.trangThai === 2 }"
                  v-tooltip="'Chỉnh sửa'"
                >
                  <img :src="iconEdit" class="img-icon-action" />
                </button>

                <!-- SWITCH -->
                <label
                  class="switch"
                  :class="{ disabled: item.trangThai === 2 }"
                  v-tooltip="'Đổi trạng thái'"
                >
                  <input
                    type="checkbox"
                    :checked="item.active"
                    :disabled="item.trangThai === 2"
                    @change="toggleTrangThai(item)"
                  />
                  <span class="slider"></span>
                </label>
              </div>
            </td>
          </tr>
          <tr v-if="danhSachHienThi.length === 0">
            <td colspan="8" class="empty-table">
              🔍 Không tìm thấy đợt giảm giá phù hợp
            </td>
          </tr>
        </tbody>
      </table>

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
                <span class="total-number">{{ totalPages }}</span>
              </div>
            </li>

            <li>
              <button
                class="nav-action-btn"
                :disabled="currentPage === totalPages"
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
                :disabled="currentPage === totalPages"
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
          Hiển thị {{ danhSachHienThi.length }} / {{ totalItems }} đợt giảm giá
        </div>
      </div>
    </div>

    <!-- MODAL VIEW CHI TIẾT -->
    <Teleport to="body">
      <div
        v-if="showModal"
        class="modal-overlay"
        @click.self="showModal = false"
      >
        <div class="modal-box" style="width: 800px; max-width: 95vw">
          <div class="modal-header">
            <h3>Chi tiết đợt giảm giá</h3>
            <span class="close" @click="showModal = false">&times;</span>
          </div>
          <div class="modal-body custom-scrollbar" v-if="selectedItem">
            <div class="grid">
              <div class="item">
                <label>Mã đợt</label>
                <span>{{ selectedItem.maDot }}</span>
              </div>
              <div class="item">
                <label>Tên đợt</label>
                <span>{{ selectedItem.tenDot }}</span>
              </div>
              <div class="item">
                <label>Thời gian bắt đầu</label>
                <span>{{ formatDate(selectedItem.ngayBatDau) }}</span>
              </div>
              <div class="item">
                <label>Thời gian kết thúc</label>
                <span>{{ formatDate(selectedItem.ngayKetThuc) }}</span>
              </div>
              <div class="item highlight">
                <label>Giá trị</label>
                <span>Giảm {{ selectedItem.giaTriGiam }}%</span>
              </div>

              <div class="item status">
                <label>Trạng thái</label>
                <span
                  class="badge"
                  :class="trangThaiClass(selectedItem.trangThai)"
                >
                  {{ trangThaiText(selectedItem.trangThai) }}
                </span>
              </div>
            </div>

            <div style="margin-top: 20px">
              <h4
                style="
                  margin-bottom: 12px;
                  font-size: 16px;
                  color: #333;
                  font-weight: 600;
                "
              >
                Sản phẩm áp dụng ({{ selectedItem.sanPhams?.length || 0 }})
              </h4>
              <div class="table-wrapper custom-scrollbar">
                <table class="pgg-table table-sanpham" style="margin: 0">
                  <thead style="position: sticky; top: 0; z-index: 1">
                    <tr>
                      <th width="60">STT</th>
                      <th width="110">Hình ảnh</th>
                      <th style="text-align: left">Tên sản phẩm</th>
                      <th width="120">Giá gốc</th>
                      <th width="120">Giá ưu đãi</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(sp, idx) in selectedItem.sanPhams" :key="sp.id">
                      <td>{{ idx + 1 }}</td>
                      <td>
                        <img
                          :src="
                            sp.hinhAnh
                              ? sp.hinhAnh.startsWith('http')
                                ? sp.hinhAnh
                                : `http://localhost:8080/uploads/${sp.hinhAnh}`
                              : '/no-image.png'
                          "
                          style="
                            width: 40px;
                            height: 40px;
                            border-radius: 6px;
                            object-fit: cover;
                            border: 1px solid #eee;
                          "
                          @error="(e) => (e.target.src = '/no-image.png')"
                        />
                      </td>
                      <td
                        style="text-align: left; font-weight: 600; color: #333"
                      >
                        {{ sp.tenSanPham }}
                      </td>
                      <td style="text-decoration: line-through; color: #999">
                        {{ formatMoney(sp.gia) }} đ
                      </td>
                      <td style="color: #d70018; font-weight: 700">
                        {{ formatMoney(tinhGiaSauGiam(sp.gia, selectedItem)) }}
                        đ
                      </td>
                    </tr>
                    <tr
                      v-if="
                        !selectedItem.sanPhams ||
                        selectedItem.sanPhams.length === 0
                      "
                    >
                      <td
                        colspan="5"
                        style="text-align: center; color: #888; padding: 30px 0"
                      >
                        <i
                          class="ri-inbox-2-line"
                          style="
                            font-size: 24px;
                            color: #ccc;
                            display: block;
                            margin-bottom: 8px;
                          "
                        ></i>
                        Không có sản phẩm nào
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
/* ===== PAGE ===== */
.pgg-page {
  font-family: var(--font-family);
}

.header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-flex.no-border {
  border: none;
}

.main-title {
  display: flex;
  align-items: center;
  font-size: 30px;
  font-weight: bold;
  gap: 12px;
  letter-spacing: 1px;
  color: #1a1a1a;
}

.icon-wrapper img {
  width: 32px;
  height: 32px;
  object-fit: contain;
}

.icon-wrapper {
  width: 56px;
  height: 56px;
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

.icon-title {
  width: 24px;
  height: 24px;
  display: block;
}

/* ===== BOX ===== */
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

.box-header h2 {
  margin: 0;
}
.box-header h3 {
  margin: 0;
  font-weight: 700;
  font-size: 19 px;
}
.box-header p {
  margin: 10px 0 0;
  color: var(--color-text-muted);
}

/* ===== FILTER ===== */
.filter-box {
  display: grid;
  grid-template-columns: 2fr 1.5fr 1.5fr auto;
  gap: 12px;
  padding: 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 7px;
  margin-top: 14px;
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
  font-size: 13px;
  font-weight: 800;
  color: #64748b;
}

.filter-item input {
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

.filter-item input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12);
}

.filter-actions {
  display: flex;
  gap: 8px;
  align-self: end;
}

.btn-search,
.btn-reset {
  height: 40px;
  border: none;
  border-radius: 7px;
  padding: 0 14px;
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
  transition: all 0.25s ease;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-search {
  background: linear-gradient(135deg, var(--color-primary), #d16969);
  color: #fff;
  box-shadow: 0 10px 20px rgba(230, 90, 90, 0.16);
}

.btn-reset {
  background: #fff;
  color: #334155;
  border: 1px solid #dbe1ea;
}

.btn-search:hover,
.btn-reset:hover {
  transform: translateY(-2px);
}

/* ===== HEADER ACTION BUTTONS ===== */
.header-actions {
  display: flex;
  gap: 20px;
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

  background: #f0f2f7;
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
/* ===== TABLE ===== */
.pgg-table {
  width: 100%;
  border-collapse: collapse;
}

.pgg-table th {
  background: #e9f2fb;
  padding: 12px 16px;
  text-align: center;
  white-space: nowrap;
  line-height: 1.4;
}

.pgg-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
  text-align: center;
}

.fw {
  font-weight: 600;
}

/* ===== PILL ===== */
.pill {
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  color: #fff;

  display: inline-flex; /* dùng flex */
  align-items: center; /* căn dọc giữa */
  justify-content: center; /* căn ngang giữa */
  text-align: center; /* backup cho text */
  min-width: 60px; /* optional: pill đều nhau */
}
.congkhai {
  background: #d1fae5;
  color: #059669;
}

.canhan {
  background: #dbeafe;
  color: #2563eb;
}

.chuabatdau {
  background: #fef9c3;
  color: #ca8a04;
}

.hoatdong {
  background: var(--color-active);
}

.ketthuc {
  background: var(--color-inactive);
}

/* ===== DISCOUNT ===== */
.discount .sub {
  font-size: 12px;
  color: #666;
}

.discount-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 6px;
  font-weight: 400;
  margin-bottom: 4px;
}

.percent {
  background: #fee2e2;
  color: #dc2626;
}

.money {
  background: #dcfce7;
  color: #16a34a;
}

/* ===== SWITCH ===== */
.switch {
  position: relative;
  width: 40px;
  height: 20px;
  display: inline-block;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  inset: 0;
  background: #ccc;
  border-radius: 20px;
  transition: 0.25s ease;
  cursor: pointer;
}

.slider::before {
  content: "";
  position: absolute;
  width: 16px;
  height: 16px;
  left: 2px;
  bottom: 2px;
  background: #fff;
  border-radius: 50%;
  transition: 0.25s ease;
}

.switch input:checked + .slider {
  background: var(--color-switch-on);
}

.switch input:checked + .slider::before {
  transform: translateX(20px);
}

/* ===== MODAL OVERLAY ===== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
  backdrop-filter: blur(3px);
}

/* ===== MODAL BOX ===== */
.modal-box {
  background: #fff;
  border-radius: 7px;
  width: 650px;
  max-width: 90%;
  padding: 20px;
  animation: fadeIn 0.25s ease;

  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2);
  max-height: 90vh;
  height: fit-content;
  display: flex;
  flex-direction: column;
}

/* ===== HEADER ===== */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  border-bottom: 1px solid #f1f1f1;
  padding-bottom: 10px;
  flex-shrink: 0;
}

.modal-header h3 {
  margin: 0;
  color: #e11d48;
  font-weight: 700;
}

.close {
  font-size: 22px;
  cursor: pointer;
  transition: 0.2s;
}

.close:hover {
  color: #e11d48;
  transform: rotate(90deg);
}

.modal-body {
  overflow-y: auto;
  padding-right: 5px;
}

/* ===== BODY GRID ===== */
.modal-body .grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px 24px;
  border: 1px solid #e2e8f0;
  border-radius: 7px;
  padding: 20px 24px;
  background: #fcfcfc;
}

/* ===== ITEM ===== */
.modal-body .item {
  display: flex;
  flex-direction: column;
}

.modal-body .item label {
  font-size: 15px;
  color: #64748b;
  margin-bottom: 4px;
}

.modal-body .item span {
  font-weight: 700;
  color: #1e293b;
  font-size: 16px;
}

/* ===== HIGHLIGHT ===== */
.modal-body .highlight {
}

.modal-body .highlight label {
  color: #e11d48;
}

.modal-body .highlight span {
  color: #e11d48;
  font-size: 18px;
  font-weight: 700;
}

/* ===== TABLE WRAPPER ===== */
.table-wrapper {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #eee;
  border-radius: 8px;
  height: fit-content;
}

/* ===== CUSTOM SCROLLBAR ===== */
.custom-scrollbar::-webkit-scrollbar {
  width: 5px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: #f1f1f1;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 7px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* ===== STATUS ===== */
.status .badge {
  display: inline-block;
  padding: 6px 14px;
  border-radius: 7px;
  font-size: 14px;
  font-weight: 600;
  width: fit-content;
}

.status .badge.chuabatdau {
  background: #fef9c3;
  color: #ca8a04;
}

.status .badge.hoatdong {
  background: #dcfce7;
  color: #16a34a;
}

.status .badge.ketthuc {
  background: #fee2e2;
  color: #dc2626;
}

/* ===== ANIMATION ===== */
@keyframes fadeIn {
  from {
    transform: translateY(20px) scale(0.95);
    opacity: 0;
  }
  to {
    transform: translateY(0) scale(1);
    opacity: 1;
  }
}

/* ===== VUE SELECT CUSTOM (Đã đồng bộ với KhachChoCheckInAll) ===== */
.modern-select {
  width: 100%;
  background: #fff;
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
  cursor: text !important;
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
  cursor: text !important;
}

/* Ẩn hiện chữ cực mượt khi click */
:deep(.vs--single.vs--selected:not(.vs--open) .vs__search) {
  opacity: 0 !important;
}

:deep(.vs--single.vs--open .vs__selected) {
  opacity: 0 !important; /* Ẩn hẳn chữ cũ khi mở dropdown */
}

:deep(.vs--single.vs--open .vs__search) {
  opacity: 1 !important; /* Hiện ô gõ chữ */
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

/* Hiệu ứng focus phát sáng màu primary */
:deep(.vs--open .vs__dropdown-toggle) {
  border-color: var(--color-primary) !important;
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12) !important;
}

/* Menu Dropdown */
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

/* Màu khi hover y hệt file KhachCho */
:deep(.vs__dropdown-option--highlight) {
  background: #fff7f7 !important;
  color: var(--color-primary) !important;
}
/* lm mờ nút sửa vs switch */
.disabled {
  opacity: 0.4;
  pointer-events: none;
  cursor: not-allowed;
}

/* ===== NEW FILTER SYSTEM ===== */
.filter-card {
  padding: 20px !important;
  border-radius: 12px;
  border: 1px solid #eee;
  background: #fff;
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  gap: 20px;
  align-items: center;
  width: 100%;
}

.second-row {
  margin-top: 4px;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.search-field {
  flex: 3;
}

.status-field {
  flex: 1;
}

.date-field {
  flex: 1;
}

.discount-field {
  flex: 1.2;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12px;
  margin-top: 22px; /* Align with date fields which have top labels */
}

.action-field {
  display: flex;
  align-items: flex-end;
  margin-top: 22px; /* Align with inputs */
}

.filter-label {
  font-size: 13px;
  color: #6c757d;
  margin-bottom: 2px;
  display: block;
  font-weight: 500;
}

.filter-input {
  width: 100%;
  height: 40px;
  border: 1px solid #dbe1ea;
  border-radius: 7px;
  padding: 0 12px;
  background: #fff;
  outline: none;
  color: #0f172a;
  font-size: 14px;
  font-weight: 400;
  transition: all 0.22s ease;
}
.filter-input::placeholder {
  font-weight: 400;
  color: #9ca3af;
}

.filter-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12);
}

.discount-label {
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  white-space: nowrap;
}

.range-slider-container {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-grow: 1;
}

.range-slider {
  position: relative;
  flex-grow: 1;
  height: 4px;
  background: #e2e8f0;
  border-radius: 2px;
}

.slider-track {
  position: absolute;
  height: 100%;
  background: #850f16;
  border-radius: 2px;
}

.range-slider input[type="range"] {
  position: absolute;
  width: 100%;
  height: 4px;
  top: 0;
  left: 0;
  background: none;
  pointer-events: none;
  -webkit-appearance: none;
  appearance: none;
  margin: 0;
}

.range-slider input[type="range"]::-webkit-slider-thumb {
  height: 14px;
  width: 14px;
  border-radius: 50%;
  background: #850f16;
  cursor: pointer;
  pointer-events: auto;
  -webkit-appearance: none;
  border: none;
}

.range-slider input[type="range"]::-moz-range-thumb {
  height: 14px;
  width: 14px;
  border-radius: 50%;
  background: #850f16;
  cursor: pointer;
  pointer-events: auto;
  border: none;
}

.range-value {
  font-size: 14px;
  font-weight: 700;
  color: #850f16;
  white-space: nowrap;
}

.btn-clear-filter {
  height: 40px;
  padding: 0 20px;
  background: #850f16;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.btn-clear-filter:hover {
  background: #a12323;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(133, 15, 22, 0.2);
}

.table-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.table-title i {
  font-size: 22px;
  color: #dc2626;
}
.icon-wrapper-small {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background-color: #ece4e4;
  border-radius: 8px;
  margin-right: 3px;
}
.icon-title-small {
  width: 18px;
  height: 18px;
  display: block;
}
/* /////////////// THỜI GIAN PGG */
.time-cell {
  font-size: 13px;
  line-height: 1.4;
}

.time-range {
  display: inline-flex;
  flex-direction: column;
  gap: 4px;
  width: 210px; /* Cố định chiều rộng bằng nhau cho tất cả các dòng */
  padding: 6px 12px;
  border-radius: 8px;
  background: #f9fafb;
  border: 1px solid #eef2f7;
}

.time-start {
  color: #059669;
  font-weight: 500;
}

.time-end {
  color: #dc2626;
  font-weight: 500;
}

.status-text {
  display: inline-flex;
  align-items: center;
  justify-content: center;

  min-width: 120px;

  padding: 6px;
  border-radius: 4px;

  font-size: 13px;
  font-weight: 500;

  white-space: nowrap;
}

.status-upcoming {
  background: #fef3c7;
  color: #d97706;
}

.status-active {
  background: #e5f7ff;
  color: #27a8e8;
}

.status-ended {
  background: #fee2e2;
  color: #dc2626;
}

.status-stopped {
  background: #e5e7eb;
  color: #6b7280;
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

.empty-table {
  text-align: center;
  color: #6b7280;
  padding: 20px;
  font-style: italic;
}
.action-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
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

/* CHECKBOX */
.custom-checkbox {
  width: 16px;
  height: 16px;
  cursor: pointer;
  accent-color: #d73737;
}
.custom-checkbox:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}
</style>
