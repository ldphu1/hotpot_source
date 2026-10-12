<script setup>
import { onMounted, onUnmounted, reactive, ref, watch, computed } from "vue";
import flatpickr from "flatpickr";
import "flatpickr/dist/flatpickr.min.css";
import { Vietnamese } from "flatpickr/dist/l10n/vn.js";
import { useRouter, useRoute, onBeforeRouteLeave } from "vue-router";
import { getAllKhachHang } from "@/api/admin/khachhang/khachhangApi";
import {
  updatePhieuGiamGia,
  getDetailPhieuGiamGia,
} from "@/api/admin/phieugiamgia/phieuGiamGiaApi";
import { useUtils } from "@/utils/helper";
import iconBack from "@/assets/icons/prev.png";
const router = useRouter();
const route = useRoute();
const { notify, nprogress } = useUtils();
const isLoading = ref(false);
const isSubmittingOrCanceling = ref(false);
const initialDataSnapshot = ref("");
const id = route.params.id;

/* ================= DATA ================= */

const danhSachKhachHang = ref([]);

const isAllSelected = computed(() => {
  return (
    danhSachKhachHang.value.length > 0 &&
    formPhieu.danhSachKhachHangIds.length === danhSachKhachHang.value.length
  );
});

const toggleSelectAll = (event) => {
  if (event.target.checked) {
    formPhieu.danhSachKhachHangIds = danhSachKhachHang.value.map((kh) =>
      Number(kh.id),
    );
  } else {
    formPhieu.danhSachKhachHangIds = [];
  }
};

const formPhieu = reactive({
  tenGiamGia: "",
  soLuong: null,
  kieuGiamGia: 1,
  phuongThucGiamGia: 1,
  giaTriGiam: null,
  dieuKienSuDung: null,
  giamToiDa: null,
  ngayBatDau: "",
  ngayKetThuc: "",
  danhSachKhachHangIds: [],
});

/* ================= TODAY ================= */

const todayDate = new Date();
todayDate.setHours(0, 0, 0, 0);
const today = todayDate.toISOString().split("T")[0];

const formatDate = (date) => {
  if (!date) return "-";
  return new Date(date).toLocaleDateString("vi-VN");
};

/* ===== FORMAT MONEY HELPERS ===== */
const displayGiaTriGiam = ref("");
const displayDieuKien = ref("");
const displayGiamToiDa = ref("");

const onGiaTriGiamInput = (e) => {
  const raw = e.target.value.replace(/\./g, "").replace(/[^\d]/g, "");
  formPhieu.giaTriGiam = raw ? Number(raw) : null;
  displayGiaTriGiam.value = raw ? Number(raw).toLocaleString("vi-VN") : "";
  errors.giaTriGiam = "";
};

const onDieuKienInput = (e) => {
  const raw = e.target.value.replace(/\./g, "").replace(/[^\d]/g, "");
  formPhieu.dieuKienSuDung = raw ? Number(raw) : null;
  displayDieuKien.value = raw ? Number(raw).toLocaleString("vi-VN") : "";
  errors.dieuKienSuDung = "";
};

const onGiamToiDaInput = (e) => {
  const raw = e.target.value.replace(/\./g, "").replace(/[^\d]/g, "");
  formPhieu.giamToiDa = raw ? Number(raw) : null;
  displayGiamToiDa.value = raw ? Number(raw).toLocaleString("vi-VN") : "";
  errors.giamToiDa = "";
};

/* ================= LOAD DATA ================= */

const handleBeforeUnload = (e) => {
  if (isSubmittingOrCanceling.value || !hasUnsavedChanges.value) return;
  e.preventDefault();
  e.returnValue = "";
};

const startPickerRef = ref(null);
const endPickerRef = ref(null);
let startPicker = null;
let endPicker = null;

onMounted(async () => {
  window.addEventListener("beforeunload", handleBeforeUnload);
  nprogress.start();

  startPicker = flatpickr(startPickerRef.value, {
    enableTime: false,
    dateFormat: "Y-m-d",
    locale: Vietnamese,
    onChange: (selectedDates, dateStr) => {
      formPhieu.ngayBatDau = dateStr;
      errors.ngayBatDau = "";
      if (endPicker) {
        endPicker.set("minDate", dateStr);
      }
    },
  });

  endPicker = flatpickr(endPickerRef.value, {
    enableTime: false,
    dateFormat: "Y-m-d",
    locale: Vietnamese,
    onChange: (selectedDates, dateStr) => {
      formPhieu.ngayKetThuc = dateStr;
      errors.ngayKetThuc = "";
    },
  });

  try {
    const khRes = await getAllKhachHang();
    danhSachKhachHang.value = khRes.data || [];

    const detailRes = await getDetailPhieuGiamGia(id);
    const data = detailRes.data;

    formPhieu.tenGiamGia = data.tenGiamGia;
    formPhieu.soLuong = data.soLuong;
    formPhieu.kieuGiamGia = Number(data.kieuGiamGia);
    formPhieu.phuongThucGiamGia = Number(data.phuongThucGiamGia);
    formPhieu.giaTriGiam = data.giaTriGiam;
    formPhieu.dieuKienSuDung = data.dieuKienSuDung;
    formPhieu.giamToiDa = data.giamToiDa;

    // Pre-fill display refs
    if (formPhieu.phuongThucGiamGia === 2 && data.giaTriGiam) {
      displayGiaTriGiam.value = Number(data.giaTriGiam).toLocaleString("vi-VN");
    }
    if (data.dieuKienSuDung) displayDieuKien.value = Number(data.dieuKienSuDung).toLocaleString("vi-VN");
    if (data.giamToiDa) displayGiamToiDa.value = Number(data.giamToiDa).toLocaleString("vi-VN");

    formPhieu.ngayBatDau = data.ngayBatDau?.slice(0, 10);
    formPhieu.ngayKetThuc = data.ngayKetThuc?.slice(0, 10);

    if (formPhieu.ngayBatDau && startPicker) {
      startPicker.setDate(formPhieu.ngayBatDau);
    }
    if (formPhieu.ngayKetThuc && endPicker) {
      endPicker.setDate(formPhieu.ngayKetThuc);
      endPicker.set("minDate", formPhieu.ngayBatDau);
    }

    formPhieu.danhSachKhachHangIds = (data.danhSachKhachHangIds || []).map(
      (id) => Number(id),
    );

    initialDataSnapshot.value = JSON.stringify({
      tenGiamGia: formPhieu.tenGiamGia || "",
      soLuong: formPhieu.soLuong,
      kieuGiamGia: formPhieu.kieuGiamGia,
      phuongThucGiamGia: formPhieu.phuongThucGiamGia,
      giaTriGiam: formPhieu.giaTriGiam,
      dieuKienSuDung: formPhieu.dieuKienSuDung,
      giamToiDa: formPhieu.giamToiDa,
      ngayBatDau: formPhieu.ngayBatDau || "",
      ngayKetThuc: formPhieu.ngayKetThuc || "",
      danhSachKhachHangIds: [...formPhieu.danhSachKhachHangIds].sort((a, b) => a - b),
    });
  } catch (e) {
    notify.error("Không tải được dữ liệu phiếu giảm giá");
    router.back();
  } finally {
    nprogress.done();
  }
});

onUnmounted(() => {
  window.removeEventListener("beforeunload", handleBeforeUnload);
  if (startPicker) startPicker.destroy();
  if (endPicker) endPicker.destroy();
});

const hasUnsavedChanges = computed(() => {
  if (!initialDataSnapshot.value) return false;

  const currentSnapshot = JSON.stringify({
    tenGiamGia: formPhieu.tenGiamGia || "",
    soLuong: formPhieu.soLuong,
    kieuGiamGia: formPhieu.kieuGiamGia,
    phuongThucGiamGia: formPhieu.phuongThucGiamGia,
    giaTriGiam: formPhieu.giaTriGiam,
    dieuKienSuDung: formPhieu.dieuKienSuDung,
    giamToiDa: formPhieu.giamToiDa,
    ngayBatDau: formPhieu.ngayBatDau || "",
    ngayKetThuc: formPhieu.ngayKetThuc || "",
    danhSachKhachHangIds: [...formPhieu.danhSachKhachHangIds].sort((a, b) => a - b),
  });

  return currentSnapshot !== initialDataSnapshot.value;
});

onBeforeRouteLeave(async (to, from) => {
  if (isSubmittingOrCanceling.value) {
    return true;
  }

  if (!hasUnsavedChanges.value) {
    return true;
  }

  const answer = await notify.confirm(
    "Bạn có thay đổi chưa lưu. Bạn có chắc chắn muốn rời đi và hủy bỏ các thay đổi này?"
  );

  return answer;
});

/* ================= UPDATE ================= */

const errors = reactive({
  tenGiamGia: "",
  giaTriGiam: "",
  giamToiDa: "",
  dieuKienSuDung: "",
  soLuong: "",
  ngayBatDau: "",
  ngayKetThuc: "",
  khachHang: "",
});

const capNhatPhieu = async () => {
  let hasError = false;
  Object.keys(errors).forEach((key) => (errors[key] = ""));

  if (!formPhieu.tenGiamGia?.trim()) {
    errors.tenGiamGia = "Tên phiếu không được để trống";
    hasError = true;
  }

  if (formPhieu.giaTriGiam == null || formPhieu.giaTriGiam === "") {
    errors.giaTriGiam = "Vui lòng nhập giá trị giảm";
    hasError = true;
  } else if (formPhieu.giaTriGiam <= 0) {
    errors.giaTriGiam = "Giá trị giảm phải lớn hơn 0";
    hasError = true;
  }

  if (formPhieu.phuongThucGiamGia === 1) {
    if (!formPhieu.giamToiDa) {
      errors.giamToiDa = "Giảm theo % phải nhập giảm tối đa";
      hasError = true;
    }

    if (formPhieu.giaTriGiam > 100) {
      errors.giaTriGiam = "Giảm theo % không được lớn hơn 100%";
      hasError = true;
    }
  }

  if (!formPhieu.dieuKienSuDung || formPhieu.dieuKienSuDung <= 0) {
    errors.dieuKienSuDung = "Đơn tối thiểu phải lớn hơn 0";
    hasError = true;
  }

  if (formPhieu.phuongThucGiamGia === 2) {
    if (formPhieu.giaTriGiam > formPhieu.dieuKienSuDung) {
      errors.giaTriGiam = "Giảm tiền không được lớn hơn đơn tối thiểu";
      hasError = true;
    }
    formPhieu.giamToiDa = null;
  }

  if (formPhieu.kieuGiamGia === 1 && !formPhieu.soLuong) {
    errors.soLuong = "Vui lòng nhập số lượng";
    hasError = true;
  }

  if (!formPhieu.ngayBatDau) {
    errors.ngayBatDau = "Vui lòng chọn ngày bắt đầu";
    hasError = true;
  }
  if (!formPhieu.ngayKetThuc) {
    errors.ngayKetThuc = "Vui lòng chọn ngày kết thúc";
    hasError = true;
  }

  if (formPhieu.ngayBatDau && formPhieu.ngayKetThuc) {
    const startDate = new Date(formPhieu.ngayBatDau);
    const endDate = new Date(formPhieu.ngayKetThuc);
    const maxDate = new Date(startDate);
    maxDate.setMonth(maxDate.getMonth() + 6);

    if (endDate <= startDate) {
      errors.ngayKetThuc = "Ngày kết thúc phải lớn hơn ngày bắt đầu";
      hasError = true;
    }

    if (endDate > maxDate) {
      errors.ngayKetThuc = "Thời gian voucher tối đa 6 tháng";
      hasError = true;
    }
  }

  if (
    formPhieu.kieuGiamGia === 2 &&
    formPhieu.danhSachKhachHangIds.length === 0
  ) {
    errors.khachHang = "Vui lòng chọn ít nhất 1 khách hàng";
    hasError = true;
  }

  if (hasError) return;

  const confirm = await notify.confirm(
    "Bạn có muốn cập nhật phiếu giảm giá này không?",
  );
  if (!confirm) return;

  isLoading.value = true;
  nprogress.start();
  const startTime = Date.now();
  try {
    const payload = {
      ...formPhieu,
      giamToiDa: formPhieu.phuongThucGiamGia === 1 ? formPhieu.giamToiDa : null,
      soLuong:
        formPhieu.kieuGiamGia === 2
          ? formPhieu.danhSachKhachHangIds.length
          : formPhieu.soLuong,
    };

    const res = await updatePhieuGiamGia(id, payload);

    notify.success(res.data?.message || "Cập nhật thành công");
  } catch (e) {
    notify.error(
      e.response?.data?.message || e.response?.data || "Có lỗi xảy ra",
    );
  } finally {
    const elapsed = Date.now() - startTime;

    const minimumLoading = 3000;

    if (elapsed < minimumLoading) {
      await new Promise((resolve) =>
        setTimeout(resolve, minimumLoading - elapsed),
      );
    }

    isLoading.value = false;
    nprogress.done();

    isSubmittingOrCanceling.value = true;
    router.push("/admin/phieu-giam-gia");
  }
};

const cancel = () => router.back();

/* ================= WATCH ================= */

watch(
  () => formPhieu.danhSachKhachHangIds,
  (ids) => {
    if (formPhieu.kieuGiamGia === 2) {
      formPhieu.soLuong = ids.length;
    }
  },
  { deep: true },
);

watch(
  () => formPhieu.kieuGiamGia,
  (val) => {
    if (val === 2) {
      formPhieu.soLuong = formPhieu.danhSachKhachHangIds.length || 0;
    } else {
      formPhieu.danhSachKhachHangIds = [];
    }
  },
);

watch(
  () => formPhieu.phuongThucGiamGia,
  (val) => {
    if (val === 2) {
      formPhieu.giamToiDa = null;
    }
  },
);

///////////////////// PHÂN TRANG RIÊNG TƯ /////////////////
// Phân trang danh sách khách hàng
const khachHangPage = ref(1);
const khachHangPageSize = 5;

const pagedKhachHang = computed(() => {
  const start = (khachHangPage.value - 1) * khachHangPageSize;
  return danhSachKhachHang.value.slice(start, start + khachHangPageSize);
});

const khachHangTotalPages = computed(() =>
  Math.ceil(danhSachKhachHang.value.length / khachHangPageSize),
);

const khachHangPageList = computed(() => {
  const total = khachHangTotalPages.value;
  if (total <= 5) return Array.from({ length: total }, (_, i) => i + 1);
  const pages = [1, 2, 3];
  if (total > 4) pages.push("…");
  pages.push(total);
  return pages;
});
</script>

<template>
  <div class="page">
    <div class="box">
      <div class="box-header-flex">
        <div class="box-header">
          <h2 class="main-title">
            <span class="icon-wrapper">
              <i
                class="bi bi-gift"
                style="color: rgb(225, 29, 72); font-size: 24px"
              ></i>
            </span>
            Cập Nhật Phiếu Giảm Giá
          </h2>
          <p>Cập nhật thông tin và đối tượng áp dụng phiếu giảm giá</p>
        </div>

        <button class="icon-btn-back" @click="cancel">
          <img :src="iconBack" />
        </button>
      </div>
    </div>

    <div class="layout">
      <div class="panel">
        <div class="form-grid">
          <div class="field full">
            <label><b>Tên phiếu</b> <label style="color: red">*</label></label>
            <input
              v-model="formPhieu.tenGiamGia"
              placeholder="VD: Khuyến mãi ngày lễ..."
              :class="{ 'input-invalid': errors.tenGiamGia }"
              @input="errors.tenGiamGia = ''"
            />
            <transition name="shake">
              <p v-if="errors.tenGiamGia" class="error-hint">
                <i class="bi bi-exclamation-circle"></i>
                {{ errors.tenGiamGia }}
              </p>
            </transition>
          </div>

          <div class="field">
            <label
              ><b>Phương thức giảm </b>
              <label style="color: red">*</label></label
            >
            <div class="radio">
              <label>
                <input
                  type="radio"
                  name="phuongThucGiamGia"
                  :value="1"
                  v-model.number="formPhieu.phuongThucGiamGia"
                />
                Giảm %
              </label>
              <label>
                <input
                  type="radio"
                  name="phuongThucGiamGia"
                  :value="2"
                  v-model.number="formPhieu.phuongThucGiamGia"
                />
                Giảm tiền
              </label>
            </div>
          </div>

          <div class="field">
            <label><b>Đối tượng áp dụng</b></label>
            <div class="radio">
              <label>
                <input
                  type="radio"
                  name="kieuGiamGia"
                  :value="1"
                  v-model.number="formPhieu.kieuGiamGia"
                />
                Công khai
              </label>
              <label>
                <input
                  type="radio"
                  name="kieuGiamGia"
                  :value="2"
                  v-model.number="formPhieu.kieuGiamGia"
                />
                Riêng tư
              </label>
            </div>
          </div>

          <div class="field">
            <label><b>Giá trị giảm</b> <label style="color: red">*</label></label>
            <div class="input-money-wrap">
              <input
                type="text"
                inputmode="numeric"
                :value="formPhieu.phuongThucGiamGia === 2 ? displayGiaTriGiam : formPhieu.giaTriGiam ?? ''"
                @input="formPhieu.phuongThucGiamGia === 2 ? onGiaTriGiamInput($event) : (formPhieu.giaTriGiam = $event.target.value === '' ? null : Number($event.target.value), errors.giaTriGiam = '')"
                :placeholder="formPhieu.phuongThucGiamGia === 1 ? 'Nhập % giảm (VD: 10)' : 'Nhập số tiền giảm...'"
                :class="{ 'input-invalid': errors.giaTriGiam }"
              />
              <span class="money-unit">{{ formPhieu.phuongThucGiamGia === 1 ? '%' : 'đ' }}</span>
            </div>
            <transition name="shake">
              <p v-if="errors.giaTriGiam" class="error-hint">
                <i class="bi bi-exclamation-circle"></i>
                {{ errors.giaTriGiam }}
              </p>
            </transition>
          </div>

          <div class="field">
            <label><b>Đơn tối thiểu</b> <label style="color: red">*</label></label>
            <div class="input-money-wrap">
              <input
                type="text"
                inputmode="numeric"
                :value="displayDieuKien"
                @input="onDieuKienInput"
                placeholder="Nhập hóa đơn tối thiểu..."
                :class="{ 'input-invalid': errors.dieuKienSuDung }"
              />
              <span class="money-unit">đ</span>
            </div>
            <transition name="shake">
              <p v-if="errors.dieuKienSuDung" class="error-hint">
                <i class="bi bi-exclamation-circle"></i>
                {{ errors.dieuKienSuDung }}
              </p>
            </transition>
          </div>

          <div class="field">
            <label
              ><b>Ngày bắt đầu</b> <label style="color: red">*</label></label
            >
            <div style="position: relative; display: flex; align-items: center;">
              <input
                ref="startPickerRef"
                type="text"
                placeholder="Chọn ngày bắt đầu"
                :class="{ 'input-invalid': errors.ngayBatDau }"
                style="padding-right: 40px; width: 100%;"
              />
              <i class="bi bi-calendar-event" style="position: absolute; right: 14px; color: #9ca3af; pointer-events: none; font-size: 16px;"></i>
            </div>
            <transition name="shake">
              <p v-if="errors.ngayBatDau" class="error-hint">
                <i class="bi bi-exclamation-circle"></i>
                {{ errors.ngayBatDau }}
              </p>
            </transition>
          </div>

          <div class="field">
            <label
              ><b>Ngày kết thúc</b> <label style="color: red">*</label></label
            >
            <div style="position: relative; display: flex; align-items: center;">
              <input
                ref="endPickerRef"
                type="text"
                placeholder="Chọn ngày kết thúc"
                :class="{ 'input-invalid': errors.ngayKetThuc }"
                style="padding-right: 40px; width: 100%;"
              />
              <i class="bi bi-calendar-event" style="position: absolute; right: 14px; color: #9ca3af; pointer-events: none; font-size: 16px;"></i>
            </div>
            <transition name="shake">
              <p v-if="errors.ngayKetThuc" class="error-hint">
                <i class="bi bi-exclamation-circle"></i>
                {{ errors.ngayKetThuc }}
              </p>
            </transition>
          </div>

          <div class="field">
            <label><b>Giảm tối đa</b> <label style="color: red">*</label></label>
            <div class="input-money-wrap">
              <input
                type="text"
                inputmode="numeric"
                :value="displayGiamToiDa"
                @input="onGiamToiDaInput"
                :disabled="formPhieu.phuongThucGiamGia === 2"
                placeholder="Nhập số tiền giảm tối đa..."
                :class="{ 'input-invalid': errors.giamToiDa }"
              />
              <span class="money-unit">đ</span>
            </div>
            <transition name="shake">
              <p v-if="errors.giamToiDa" class="error-hint">
                <i class="bi bi-exclamation-circle"></i>
                {{ errors.giamToiDa }}
              </p>
            </transition>
          </div>

          <div class="field">
            <label><b>Số lượng</b> <label style="color: red">*</label></label>

            <input
              type="number"
              v-model="formPhieu.soLuong"
              :disabled="formPhieu.kieuGiamGia === 2"
              placeholder="Nhập số lượng phiếu..."
            />
          </div>
        </div>
      </div>

      <!-- KHÁCH HÀNG -->

      <div v-if="formPhieu.kieuGiamGia === 2" class="panel customer-panel">
        <h4 class="mb-2">Chọn khách hàng</h4>

        <div class="customer-toolbar">
          <input placeholder="Tìm tên, mã..." />

          <select>
            <option>Tháng này</option>
            <option>Tất cả</option>
          </select>
        </div>

        <div class="table-wrapper">
          <table class="table">
            <thead>
              <tr>
                <th>
                  <input
                    type="checkbox"
                    @change="toggleSelectAll($event)"
                    :checked="isAllSelected"
                  />
                </th>
                <th>STT</th>
                <th>Mã KH</th>
                <th>Tên KH</th>
                <th>Ngày sinh</th>
                <th>Số điện thoại</th>
                <th>Số lần mua</th>
                <th>Ngày mua cuối</th>
                <th>Email</th>
              </tr>
            </thead>

            <tbody>
              <tr v-for="(kh, index) in pagedKhachHang" :key="kh.id">
                <td>
                  <input
                    type="checkbox"
                    :value="kh.id"
                    v-model="formPhieu.danhSachKhachHangIds"
                  />
                </td>
                <td>
                  {{ (khachHangPage - 1) * khachHangPageSize + index + 1 }}
                </td>
                <td>{{ kh.maKhachHang }}</td>

                <td>{{ kh.tenKhachHang }}</td>

                <td>{{ formatDate(kh.ngaySinh) }}</td>

                <td>{{ kh.soDienThoai }}</td>

                <!-- số lần mua -->
                <td>{{ kh.soLanMua ?? 0 }}</td>

                <!-- ngày mua cuối -->
                <td>{{ formatDate(kh.ngayMuaCuoi) }}</td>

                <td>{{ kh.email }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-if="khachHangTotalPages > 1" class="pagination mt-2">
          <button
            :disabled="khachHangPage === 1"
            @click="khachHangPage--"
            class="pg-btn pg-arrow"
          >
            <i class="bi bi-chevron-left"></i>
          </button>
          <template v-for="p in khachHangPageList" :key="p">
            <button v-if="p === '…'" class="pg-btn pg-ellipsis" disabled>
              ···
            </button>
            <button
              v-else
              class="pg-btn"
              :class="{ active: p === khachHangPage }"
              @click="khachHangPage = p"
            >
              {{ p }}
            </button>
          </template>
          <button
            :disabled="khachHangPage === khachHangTotalPages"
            @click="khachHangPage++"
            class="pg-btn pg-arrow"
          >
            <i class="bi bi-chevron-right"></i>
          </button>
        </div>
        <div class="selected-box">
          <h5>Danh sách đã chọn</h5>
          <transition name="shake">
            <p v-if="errors.khachHang" class="error-hint mb-2">
              <i class="bi bi-exclamation-circle"></i>
              {{ errors.khachHang }}
            </p>
          </transition>

          <div class="tags">
            <span
              v-for="kh in danhSachKhachHang.filter((k) =>
                formPhieu.danhSachKhachHangIds.includes(Number(k.id)),
              )"
              :key="kh.id"
              class="tag"
              @click="
                formPhieu.danhSachKhachHangIds =
                  formPhieu.danhSachKhachHangIds.filter(
                    (id) => id !== Number(kh.id),
                  )
              "
            >
              {{ kh.tenKhachHang }}
              <i class="bi bi-x-circle-fill tag-close"></i>
            </span>

            <span
              v-if="formPhieu.danhSachKhachHangIds.length === 0"
              class="empty"
            >
              Chưa chọn khách hàng nào
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="actions">
      <button class="btn-cancel" @click="cancel">Hủy</button>
      <button class="btn-primary" @click="capNhatPhieu">
        <i class="bi bi-check2-all"></i>
        <div class="btn-light-sweep"></div>
        Xác nhận
      </button>
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
</template>

<style scoped>
/* ===== MONEY INPUT WRAPPER ===== */
.input-money-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.input-money-wrap input {
  width: 100%;
  padding-right: 32px;
}

.money-unit {
  position: absolute;
  right: 10px;
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  pointer-events: none;
  user-select: none;
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
.page {
  font-family: var(--font-family);
  min-height: 85vh;
}
.box {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
  border: 1px solid #eee;
}
.main-title {
  display: flex;
  align-items: center;
  font-size: 28px;
  font-weight: 600;
  gap: 12px;
  letter-spacing: 1px;
  margin-bottom: 5px;
  color: #1a1a1a;
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
.icon-btn-back {
  margin-left: auto;
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background-color: #f4f6f8;
  border: 1px solid #e9e9e9;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.icon-btn-back:hover {
  background-color: #e2e6ea;
  transform: translateX(-2px);
}

.icon-btn-back img {
  width: 20px;
  height: 20px;
  object-fit: contain;
}
.box-header {
  margin-bottom: 0;
}
.box-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center; /* căn giữa dọc */
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.layout {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
  margin: 20px 0;
}

.panel {
  background: #fff;
  border-radius: 12px;
  padding: 30px;
  border: 1px solid #eee;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  column-gap: 30px;
  row-gap: 40px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.field.full {
  grid-column: span 2;
}

.field input:not([type="radio"]):not([type="checkbox"]),
.field select {
  width: 100%;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  transition: all 0.22s ease;
  outline: none;
  font-size: 14px;
  box-sizing: border-box;
}

.field input:not([type="radio"]):not([type="checkbox"]):hover,
.field select:hover {
  border-color: #c8d0dc;
}

.field input:not([type="radio"]):not([type="checkbox"]):focus,
.field select:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12);
}

.radio {
  display: flex;
  gap: 24px;
  padding-top: 5px;
}

/* tổng thể panel */
.customer-panel {
  background: #ffffff;
  padding: 24px;
  border-radius: 7px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
}

/* tiêu đề phong cách hoàng gia */
.customer-panel h4 {
  letter-spacing: 1px;
  color: #1a1a1a;
  text-transform: uppercase;
  font-size: 1.1rem;
  border-left: 4px solid #f13e3e;
  padding-left: 15px;
}

/* thanh công cụ thanh thoát */
.customer-toolbar {
  margin-bottom: 10px;
  display: flex;
  gap: 12px; /* khoảng cách giữa các item */
  align-items: center;
  margin-bottom: 16px;
}

.customer-toolbar input,
.customer-toolbar select {
  height: 40px;
}
.customer-toolbar input {
  flex: 1; /* chiếm hết chiều ngang */
  min-width: 200px;
}

.customer-toolbar select {
  width: 150px;
}

.customer-toolbar input,
.customer-toolbar select {
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  padding: 10px 16px;
  font-size: 14px;
  transition: all 0.3s ease;
  background-color: #fafafa;
}

.customer-toolbar input:focus {
  outline: none;
  border-color: #f33636;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(212, 175, 55, 0.1);
}

/* table wrapper ẩn thanh cuộn thô cứng */
.table-wrapper {
  max-height: 450px;
  overflow-y: auto;
  border: none; /* bỏ viền bao ngoài */
}

/* thiết kế bảng luxury */
.table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0 8px; /* tạo khoảng cách giữa các hàng */
}

.table thead th {
  background-color: transparent;
  color: #181818;
  font-weight: 700;
  font-size: 13px;
  letter-spacing: 1.5px;
  padding: 12px 15px;
  border-bottom: 1px solid #f0f0f0;
}

.table tbody tr {
  background-color: #fff;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.table tbody td {
  padding: 16px 15px;
  font-size: 14px;
  color: #444;
  border-bottom: 1px solid #f8f8f8; /* viền kẻ mờ */
}

.table tbody tr td:first-child {
  border-radius: 8px 0 0 8px;
}
.table tbody tr td:last-child {
  border-radius: 0 8px 8px 0;
}

.table tbody tr:hover {
  background: #09d9e0 !important;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
  cursor: pointer;
}

/* checkbox tùy chỉnh */
input[type="checkbox"] {
  accent-color: #716666;
  width: 16px;
  height: 16px;
}

/* khu vực đã chọn */
.selected-box {
  margin-top: 20px;
  padding: 20px;
  background: #fdfbf7;
  border-radius: 12px;
}

.selected-box h5 {
  font-size: 13px;
  color: #996515;
  margin-bottom: 12px;
  font-weight: 600;
}

/* tag phong cách quý tộc hiện đại */
.tag {
  background: #fff0f0;
  color: var(--color-primary, #e63946);
  border: 1px solid rgba(230, 57, 70, 0.2);
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(230, 57, 70, 0.05);
}

.tag-close {
  font-size: 15px;
  color: rgba(230, 57, 70, 0.5);
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag:hover {
  background: var(--color-primary, #e63946);
  color: #fff;
  cursor: pointer;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(230, 57, 70, 0.2);
}

.tag:hover .tag-close {
  color: #fff;
}

.empty {
  font-size: 13px;
  color: #888;
  font-style: italic;
}

.btn {
  padding: 10px 20px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
}

.btn-cancel {
  background: #f1f5f9;
  color: #334155;
  border: 1px solid #e2e8f0;
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}
.btn-cancel:hover {
  background: #e2e8f0;
}

.btn-primary {
  position: relative;
  overflow: hidden;
  background: #d73737;
  color: #fff;
  border: none;
  padding: 10px 24px;
  border-radius: 7px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}
.btn-primary:hover {
  background: #b92b2b;
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
.btn-primary:hover .btn-light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
}
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 40px;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
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
  border-radius: 6px;
  background: #fff;
  color: #333;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}
.pg-btn:hover:not(:disabled):not(.active) {
  border-color: #d73737;
  color: #d73737;
  background: #fff1f0;
}
.pg-btn.active {
  border: 1.5px solid #d73737;
  color: #d73737;
  font-weight: 600;
}
.pg-btn:disabled {
  border-color: #f0f0f0;
  background: #f5f5f5;
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
.pg-arrow i {
  font-size: 16px;
}

/* ===== VALIDATION ===== */
.error-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #e63946;
  font-size: 0.85rem;
  margin-top: 4px;
  padding: 6px 12px;
  background-color: #fff1f2;
  border-radius: 6px;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.input-invalid {
  border-color: #e63946 !important;
  box-shadow: 0 0 0 4px rgba(230, 57, 70, 0.1) !important;
}

.shake-enter-active {
  animation: shake 0.4s cubic-bezier(0.36, 0.07, 0.19, 0.97) both;
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
</style>
