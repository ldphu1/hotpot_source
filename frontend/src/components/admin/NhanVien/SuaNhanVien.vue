<script setup>
import {
  reactive,
  ref,
  onMounted,
  onUnmounted,
  watch,
  computed,
  nextTick,
} from "vue";
import { useRouter, useRoute, onBeforeRouteLeave } from "vue-router";
import axios from "axios";
import {
  getAllChucVu,
  getAllGioiTinh,
  getNhanVienById,
  updateNhanVien,
} from "@/api/admin/nhanVien/nhanVienApi";
import { useNhanVienValidation } from "@/composables/useNhanVienValidation";
import { useAddressValidation } from "@/composables/useAddressValidation";
import flatpickr from "flatpickr";
import "flatpickr/dist/flatpickr.min.css";
import { Vietnamese } from "flatpickr/dist/l10n/vn.js";
import { useUtils } from "@/utils/helper";
import iconBack from "@/assets/icons/prev.png";

const { notify, nprogress } = useUtils();

/* ================= ROUTER ================= */
const router = useRouter();
const route = useRoute();
const idNhanVien = route.params.id;
const isSubmittingOrCanceling = ref(false);
const initialDataSnapshot = ref("");

/* ================= STATE ================= */
const gioiTinhList = ref([]);
const chucVuList = ref([]);

const form = reactive({
  tenNhanVien: "",
  ngaySinh: "",
  gioiTinh: null,
  soDienThoai: "",
  email: "",
  chucVu: "",
  tinhThanh: "",
  xaPhuong: "",
  diaChiChiTiet: "",
  anhDaiDien: null,
});

/* ================= VALIDATION ================= */
const { errors: errorsNhanVien, validateForm } = useNhanVienValidation(form);
const { addressErrors, validateAddress } = useAddressValidation(form);

/* ================= UTILS ================= */
const removeVietnameseTones = (str = "") =>
  str
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/g, "d")
    .replace(/Đ/g, "D")
    .toLowerCase();

/* ================= IMAGE ================= */
const imagePreview = ref(null);

const onSelectImage = (e) => {
  const file = e.target.files[0];
  if (!file) return;

  if (file.size > 5 * 1024 * 1024) {
    notify.error("Ảnh vượt quá 5MB");
    return;
  }

  form.anhDaiDien = file;
  imagePreview.value = URL.createObjectURL(file);
};

watch(imagePreview, (val, old) => {
  if (old) URL.revokeObjectURL(old);
});

/* ================= ADDRESS ================= */
const tinhList = ref([]);
const xaList = ref([]);
const selectedTinh = ref("");

const tinhKeyword = ref("");
const xaKeyword = ref("");

const showTinhDropdown = ref(false);
const showXaDropdown = ref(false);

// load tỉnh
const loadTinh = async () => {
  const res = await axios.get("https://34tinhthanh.com/api/provinces");
  tinhList.value = res.data.map((t) => ({
    code: t.province_code,
    name: t.name,
  }));
};

// chống race condition
let currentTinhRequest = 0;
const loadXaByTinh = async (tinhCode) => {
  if (!tinhCode) return;

  const requestId = ++currentTinhRequest;

  try {
    const res = await axios.get(
      `https://34tinhthanh.com/api/wards?province_code=${tinhCode}`,
    );

    if (requestId !== currentTinhRequest) return;

    xaList.value = res.data.map((x) => ({
      code: x.ward_code,
      name: x.ward_name || x.full_name || x.name || "Không có tên",
    }));
  } catch (err) {
    console.log("LOAD XA ERROR:", err);
    xaList.value = [];
  }
};

// watch tỉnh (fix double call)
watch(selectedTinh, async (code, old) => {
  if (!code || code === old) return;
  await loadXaByTinh(code);
});

// nhập tay tỉnh
watch(tinhKeyword, (val) => {
  if (!val) {
    selectedTinh.value = "";
    form.tinhThanh = "";
    xaList.value = [];
    xaKeyword.value = "";
    form.xaPhuong = "";
    return;
  }

  const match = tinhList.value.find(
    (t) => removeVietnameseTones(t.name) === removeVietnameseTones(val),
  );

  if (match) {
    selectedTinh.value = match.code;
    form.tinhThanh = match.name;
  }
});

// nhập tay xã
watch(xaKeyword, (val) => {
  if (!val) {
    form.xaPhuong = "";
    return;
  }

  const match = xaList.value.find(
    (x) => removeVietnameseTones(x.name) === removeVietnameseTones(val),
  );

  if (match) {
    form.xaPhuong = match.name;
  }
});

const filteredTinhList = computed(() => {
  const key = removeVietnameseTones(tinhKeyword.value.trim());
  if (!key) return tinhList.value;
  return tinhList.value.filter((t) =>
    removeVietnameseTones(t.name).includes(key),
  );
});

const filteredXaList = computed(() => {
  const key = removeVietnameseTones(xaKeyword.value.trim());
  if (!key) return xaList.value;
  return xaList.value.filter((x) =>
    removeVietnameseTones(x.name).includes(key),
  );
});

const selectTinh = async (tinh) => {
  selectedTinh.value = tinh.code;
  tinhKeyword.value = tinh.name;
  form.tinhThanh = tinh.name;

  xaKeyword.value = "";
  form.xaPhuong = "";

  showTinhDropdown.value = false;

  await loadXaByTinh(tinh.code); // 🔥 FIX QUAN TRỌNG
  showXaDropdown.value = true; // mở sau khi có data
};

const selectXa = (xa) => {
  form.xaPhuong = xa.name;
  xaKeyword.value = xa.name;
  showXaDropdown.value = false;
};

/* ================= LOAD NHÂN VIÊN ================= */
const loadNhanVien = async () => {
  const res = await getNhanVienById(idNhanVien);
  const nv = res.data;

  form.tenNhanVien = nv.tenNhanVien;
  form.email = nv.email;
  form.soDienThoai = nv.soDienThoai;

  form.gioiTinh = nv.gioiTinh;
  form.chucVu = nv.chucVu;
  form.ngaySinh = nv.ngaySinh?.slice(0, 10) || "";

  form.tinhThanh = nv.tinhThanh?.trim();
  form.xaPhuong = nv.xaPhuong?.trim();
  form.diaChiChiTiet = nv.diaChiChiTiet;

  const tinh = tinhList.value.find((t) =>
    removeVietnameseTones(t.name).includes(
      removeVietnameseTones(form.tinhThanh),
    ),
  );

  if (tinh) {
    selectedTinh.value = tinh.code;
    tinhKeyword.value = tinh.name;

    // 🔥 FIX CHÍNH
    await loadXaByTinh(tinh.code);

    const xa = xaList.value.find((x) =>
      removeVietnameseTones(x.name).includes(
        removeVietnameseTones(form.xaPhuong),
      ),
    );

    if (xa) {
      xaKeyword.value = xa.name;
      form.xaPhuong = xa.name;
    }
  }

  if (nv.anhNhanVien) {
    imagePreview.value = `http://localhost:8080/uploads/${nv.anhNhanVien}`;
  }

  initialDataSnapshot.value = JSON.stringify({
    tenNhanVien: form.tenNhanVien || "",
    ngaySinh: form.ngaySinh || "",
    gioiTinh: form.gioiTinh,
    soDienThoai: form.soDienThoai || "",
    email: form.email || "",
    chucVu: form.chucVu || "",
    tinhThanh: form.tinhThanh || "",
    xaPhuong: form.xaPhuong || "",
    diaChiChiTiet: form.diaChiChiTiet || "",
    anhDaiDien: "",
  });
};

/* ================= DATE ================= */
const ngaySinhRef = ref(null);
let fpNgaySinh = null;

/* ================= INIT ================= */
onMounted(async () => {
  await loadTinh();

  gioiTinhList.value = await getAllGioiTinh();
  chucVuList.value = await getAllChucVu();

  await loadNhanVien();

  await nextTick();

  if (ngaySinhRef.value) {
    fpNgaySinh = flatpickr(ngaySinhRef.value, {
      locale: Vietnamese,
      dateFormat: "d/m/Y",
      defaultDate: form.ngaySinh
        ? form.ngaySinh.split("-").reverse().join("/")
        : null,
      onChange: (dates) => {
        if (dates.length) {
          form.ngaySinh = flatpickr.formatDate(dates[0], "Y-m-d");
        }
      },
    });
  }

  document.addEventListener("click", handleClickOutside);
  window.addEventListener("beforeunload", handleBeforeUnload);
});

onUnmounted(() => {
  document.removeEventListener("click", handleClickOutside);
  window.removeEventListener("beforeunload", handleBeforeUnload);
  if (imagePreview.value) URL.revokeObjectURL(imagePreview.value);
  if (fpNgaySinh) fpNgaySinh.destroy();
});

const handleBeforeUnload = (e) => {
  if (isSubmittingOrCanceling.value || !hasUnsavedChanges.value) return;
  e.preventDefault();
  e.returnValue = "";
};

const hasUnsavedChanges = computed(() => {
  if (!initialDataSnapshot.value) return false;

  const currentSnapshot = JSON.stringify({
    tenNhanVien: form.tenNhanVien || "",
    ngaySinh: form.ngaySinh || "",
    gioiTinh: form.gioiTinh,
    soDienThoai: form.soDienThoai || "",
    email: form.email || "",
    chucVu: form.chucVu || "",
    tinhThanh: form.tinhThanh || "",
    xaPhuong: form.xaPhuong || "",
    diaChiChiTiet: form.diaChiChiTiet || "",
    anhDaiDien: form.anhDaiDien instanceof File ? form.anhDaiDien.name : "",
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

/* ================= CLICK OUTSIDE ================= */
const tinhBoxRef = ref(null);
const xaBoxRef = ref(null);

const handleClickOutside = (e) => {
  if (tinhBoxRef.value && !tinhBoxRef.value.contains(e.target)) {
    showTinhDropdown.value = false;
  }
  if (xaBoxRef.value && !xaBoxRef.value.contains(e.target)) {
    showXaDropdown.value = false;
  }
};

/* ================= SUBMIT ================= */
const isSubmitting = ref(false);

const submit = async () => {
  if (!validateForm() || !validateAddress()) return;

  const ok = await notify.confirm("Xác nhận cập nhật?");
  if (!ok) return;

  isSubmitting.value = true;
  nprogress.start();

  const fd = new FormData();

  const dto = {
    tenNhanVien: form.tenNhanVien.trim(),
    soDienThoai: form.soDienThoai.trim(),
    email: form.email.trim(),
    diaChiChiTiet: form.diaChiChiTiet.trim(),
    ngaySinh: form.ngaySinh,
    gioiTinh: form.gioiTinh,
    chucVu: form.chucVu,
    tinhThanh: form.tinhThanh.trim(),
    xaPhuong: form.xaPhuong.trim(),
  };

  fd.append(
    "data",
    new Blob([JSON.stringify(dto)], { type: "application/json" }),
  );

  if (form.anhDaiDien) {
    fd.append("anhNhanVien", form.anhDaiDien);
  }

  try {
    await updateNhanVien(idNhanVien, fd);
    notify.success("Cập nhật thành công");
    isSubmittingOrCanceling.value = true;
    router.push("/admin/nhan-vien");
  } catch (err) {
    const msg = err.response?.data?.message || "Có lỗi xảy ra";

    if (msg.includes("Email")) errorsNhanVien.email = msg;
    else if (msg.includes("SĐT")) errorsNhanVien.soDienThoai = msg;
    else notify.error(msg);
  } finally {
    isSubmitting.value = false;
    nprogress.done();
  }
};

const cancel = () => router.back();
</script>

<template>
  <div class="page">
    <div class="box">
      <div class="box-header-flex">
        <div class="box-header">
          <h2 class="main-title">
            <span class="icon-wrapper">
              <i
                class="bi bi-person-gear"
                style="color: rgb(225, 29, 72); font-size: 24px"
              ></i>
            </span>
            Cập Nhật Nhân Viên
          </h2>
          <p>Chỉnh sửa thông tin hồ sơ nhân sự và quyền truy cập</p>
        </div>

        <button class="icon-btn-back" @click="cancel">
          <img :src="iconBack" />
        </button>
      </div>
    </div>

    <!-- CARD -->
    <div class="card">
      <!-- AVATAR -->
      <div class="avatar-box">
        <img v-if="imagePreview" :src="imagePreview" class="avatar" />

        <div v-else class="avatar placeholder">
          <img src="@/assets/icons/iconusers.jpg" class="avatar-icon" />
        </div>

        <input
          type="file"
          id="upload"
          class="hidden"
          accept="image/*"
          @change="onSelectImage"
        />
        <label for="upload" class="upload-btn">
          Chọn ảnh<br /><small>Max 5MB</small>
        </label>
      </div>

      <!-- FORM -->
      <div class="form">
        <div>
          <label class="form-label">
            Họ và tên<span class="text-required">*</span>
          </label>
          <input
            v-model="form.tenNhanVien"
            placeholder="Nhập họ tên đầy đủ"
            :class="{ 'input-invalid': errorsNhanVien.tenNhanVien }"
            @input="errorsNhanVien.tenNhanVien = ''"
          />
          <transition name="shake">
            <p class="error-hint" v-if="errorsNhanVien.tenNhanVien">
              <i class="bi bi-exclamation-circle"></i>
              {{ errorsNhanVien.tenNhanVien }}
            </p>
          </transition>
        </div>

        <div>
          <label class="form-label">
            Ngày sinh<span class="text-required">*</span>
          </label>
          <input
            ref="ngaySinhRef"
            placeholder="dd/mm/yyyy"
            :class="{ 'input-invalid': errorsNhanVien.ngaySinh }"
            @change="errorsNhanVien.ngaySinh = ''"
          />
          <transition name="shake">
            <p class="error-hint" v-if="errorsNhanVien.ngaySinh">
              <i class="bi bi-exclamation-circle"></i>
              {{ errorsNhanVien.ngaySinh }}
            </p>
          </transition>
        </div>

        <div>
          <label class="form-label">
            Giới tính<span class="text-required">*</span>
          </label>
          <div class="radio">
            <label v-for="gt in gioiTinhList" :key="gt.value">
              <input
                type="radio"
                v-model="form.gioiTinh"
                :value="gt.value"
                @change="errorsNhanVien.gioiTinh = ''"
              />
              {{ gt.label }}
            </label>
          </div>
          <transition name="shake">
            <p class="error-hint" v-if="errorsNhanVien.gioiTinh">
              <i class="bi bi-exclamation-circle"></i>
              {{ errorsNhanVien.gioiTinh }}
            </p>
          </transition>
        </div>

        <div>
          <label class="form-label">
            Số điện thoại<span class="text-required">*</span>
          </label>
          <input
            v-model="form.soDienThoai"
            placeholder="VD: 0987..."
            :class="{ 'input-invalid': errorsNhanVien.soDienThoai }"
            @input="errorsNhanVien.soDienThoai = ''"
          />
          <transition name="shake">
            <p class="error-hint" v-if="errorsNhanVien.soDienThoai">
              <i class="bi bi-exclamation-circle"></i>
              {{ errorsNhanVien.soDienThoai }}
            </p>
          </transition>
        </div>

        <div>
          <label class="form-label">
            Email<span class="text-required">*</span>
          </label>
          <input
            v-model="form.email"
            placeholder="example@email.com"
            :class="{ 'input-invalid': errorsNhanVien.email }"
            @input="errorsNhanVien.email = ''"
          />
          <transition name="shake">
            <p class="error-hint" v-if="errorsNhanVien.email">
              <i class="bi bi-exclamation-circle"></i>
              {{ errorsNhanVien.email }}
            </p>
          </transition>
        </div>

        <div>
          <label class="form-label">
            Chức vụ<span class="text-required">*</span>
          </label>
          <select
            v-model="form.chucVu"
            :class="{ 'input-invalid': errorsNhanVien.chucVu }"
            @change="errorsNhanVien.chucVu = ''"
          >
            <option value="" disabled>Chọn chức vụ</option>

            <option v-for="cv in chucVuList" :key="cv.value" :value="cv.value">
              {{ cv.label }}
            </option>
          </select>
          <transition name="shake">
            <p class="error-hint" v-if="errorsNhanVien.chucVu">
              <i class="bi bi-exclamation-circle"></i>
              {{ errorsNhanVien.chucVu }}
            </p>
          </transition>
        </div>

        <div class="col-3 address-title">
          <img src="@/assets/icons/diachiic.png" alt="icon" /> ĐỊA CHỈ LIÊN HỆ
        </div>

        <div
          class="autocomplete"
          tabindex="0"
          @blur="() => setTimeout(() => (showTinhDropdown = false), 150)"
          ref="tinhBoxRef"
        >
          <label class="form-label">
            Tỉnh / Thành phố<span class="text-required">*</span>
          </label>

          <input
            v-model="tinhKeyword"
            placeholder="Nhập tỉnh / thành phố..."
            :class="{ 'input-invalid': addressErrors.tinhThanh }"
            @focus="showTinhDropdown = true"
            @input="
              showTinhDropdown = true;
              addressErrors.tinhThanh = '';
            "
          />
          <transition name="shake">
            <p class="error-hint" v-if="addressErrors.tinhThanh">
              <i class="bi bi-exclamation-circle"></i>
              {{ addressErrors.tinhThanh }}
            </p>
          </transition>
          <ul v-if="showTinhDropdown" class="dropdown">
            <li
              v-for="t in filteredTinhList"
              :key="t.code"
              @click="selectTinh(t)"
            >
              {{ t.name }}
            </li>
            <li
              v-if="!filteredTinhList || filteredTinhList.length === 0"
              class="empty"
            >
              Không tìm thấy
            </li>
          </ul>
        </div>

        <div
          class="autocomplete"
          tabindex="0"
          @blur="() => setTimeout(() => (showXaDropdown = false), 150)"
          ref="xaBoxRef"
        >
          <label class="form-label">
            Phường / Xã<span class="text-required">*</span>
          </label>

          <input
            v-model="xaKeyword"
            placeholder="Nhập phường / xã..."
            :disabled="
              !selectedTinh || !Array.isArray(xaList) || xaList.length === 0
            "
            :class="{ 'input-invalid': addressErrors.xaPhuong }"
            @focus="selectedTinh && (showXaDropdown = true)"
            @input="
              selectedTinh && (showXaDropdown = true);
              addressErrors.xaPhuong = '';
            "
          />
          <transition name="shake">
            <p class="error-hint" v-if="addressErrors.xaPhuong">
              <i class="bi bi-exclamation-circle"></i>
              {{ addressErrors.xaPhuong }}
            </p>
          </transition>
          <ul v-if="showXaDropdown && selectedTinh" class="dropdown">
            <li v-for="x in filteredXaList" :key="x.code" @click="selectXa(x)">
              {{ x.name || "Không có dữ liệu" }}
            </li>
            <li
              v-if="!filteredXaList || filteredXaList.length === 0"
              class="empty"
            >
              Không tìm thấy
            </li>
          </ul>
        </div>

        <div class="full">
          <label class="form-label">
            Địa chỉ chi tiết<span class="text-required">*</span>
          </label>
          <input
            v-model="form.diaChiChiTiet"
            placeholder="Số nhà, đường..."
            :class="{ 'input-invalid': addressErrors.diaChiChiTiet }"
            @input="addressErrors.diaChiChiTiet = ''"
          />
          <transition name="shake">
            <p class="error-hint" v-if="addressErrors.diaChiChiTiet">
              <i class="bi bi-exclamation-circle"></i>
              {{ addressErrors.diaChiChiTiet }}
            </p>
          </transition>
        </div>
      </div>
    </div>

    <!-- ACTION -->
    <div class="actions">
      <button type="button" class="btn-cancel" @click="cancel">Hủy bỏ</button>
      <button
        type="button"
        class="btn-primary"
        :disabled="isSubmitting"
        @click="submit"
      >
        <div class="btn-light-sweep"></div>
        <i class="bi bi-check2-all"></i>
        Xác nhận
      </button>
    </div>
  </div>
</template>

<style scoped>
.page {
  background: #f7f8fa;
  padding: 7px;
  font-family: system-ui;
}

/* ///////////////////// HEADER ADD ////////// */
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

.box {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
  border: 1px solid #eee;
}

.box-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.box-header {
  margin-bottom: 0;
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
  background: #fee2e2;
  border-color: rgba(225, 29, 72, 0.2);
  transform: scale(1.05);
}

.icon-btn-back img {
  width: 24px;
  height: 24px;
}

.address-title img {
  width: 24px;
  height: 24px;
}
/* CARD */
.card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 24px;
  border: 1px solid #e5e7eb;
}

/* AVATAR */
.avatar-box {
  text-align: center;
}
.avatar {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  object-fit: cover;
}
.placeholder {
  background: #f1f1f1;
  border: 2px dashed #ccc;
  display: flex;
  align-items: center;
  justify-content: center;
}
.avatar-icon {
  width: 72px;
  height: 72px;
  opacity: 0.4;
}

.upload-btn {
  display: block;
  margin-top: 10px;
  font-size: 20px;
  color: #666;
  cursor: pointer;
}

.hidden {
  display: none;
}

/* dấu * bắt buộc */
.text-required {
  color: #dc3545; /* đỏ bootstrap */
  font-weight: 600;
  margin-left: 2px;
}

/* FORM */
.form {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.col-3 {
  grid-column: span 3;
}
.full {
  grid-column: 1 / -1;
}
.input-with-btn {
  position: relative;
}

.input-with-btn input {
  padding-right: 44px;
}

.icon-btn {
  position: absolute;
  right: 6px;
  top: 50%;
  transform: translateY(-50%);
  border: none;
  background: transparent;
  cursor: pointer;
}

.icon-btn img {
  width: 24px;
  height: 24px;
}

label {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
  display: inline-block;
}

input:not([type="radio"]):not([type="checkbox"]):not([type="file"]),
select {
  width: 100%;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  transition: all 0.22s ease;
  outline: none;
  font-size: 14px;
  box-sizing: border-box;
  font-family: inherit;
  color: #374151;
}

input:not([type="radio"]):not([type="checkbox"]):not([type="file"]):hover,
select:hover {
  border-color: #c8d0dc;
}

input:not([type="radio"]):not([type="checkbox"]):not([type="file"]):focus,
select:focus {
  border-color: var(--color-primary, #e63946);
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12);
}

select {
  cursor: pointer;
}
.radio {
  display: flex;
  gap: 16px;
}

.radio label {
  display: flex;
  align-items: center; /* canh chữ ngang tâm với radio */
  gap: 6px; /* khoảng cách giữa nút radio và chữ */
  cursor: pointer;
}
.address-title {
  font-weight: 600;
  margin-top: 10px;
}

/* ACTION */
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}
.btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.2s ease;
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
  transform: translateY(-2px);
}

.btn-activate {
  background: #dcfce7;
  color: #166534;
  border: 1px solid #bbf7d0;
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}
.btn-activate:hover {
  background: #bbf7d0;
  transform: translateY(-2px);
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
  gap: 8px;
}
.btn-primary:hover {
  background: #b92b2b;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(230, 90, 90, 0.3);
}

/* Hiệu ứng quét sáng (light-sweep) của nút Primary */
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

/* ===== VALIDATION CSS ===== */
.error-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #e63946;
  font-size: 0.85rem;
  margin-top: 6px;
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

/* combobox dia chi */
.autocomplete {
  position: relative;
}

.dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  max-height: 220px;
  overflow-y: auto;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  z-index: 50;
  margin-top: 4px;
}

.dropdown li {
  padding: 10px;
  cursor: pointer;
}

.dropdown li:hover {
  background: #f3f4f6;
}

.dropdown .empty {
  text-align: center;
  color: #999;
}
</style>
