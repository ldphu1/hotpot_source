<script setup>
import { reactive, ref, nextTick, onMounted, onUnmounted, computed, Teleport } from "vue";
import { useRouter, onBeforeRouteLeave } from "vue-router";
import axios from "axios";
import iconBack from "@/assets/icons/prev.png";
import {
  getAllChucVu,
  getAllGioiTinh,
  addNhanVien,
} from "@/api/admin/nhanVien/nhanVienApi";
import { useNhanVienValidation } from "@/composables/useNhanVienValidation";
import { useAddressValidation } from "@/composables/useAddressValidation";
import { Html5Qrcode } from "html5-qrcode";
import { useUtils } from "@/utils/helper";

const { notify } = useUtils();

/* ================= STATE ================= */
const router = useRouter();
const selectedFile = ref(null);
const gioiTinhList = ref([]);
const chucVuList = ref([]);
const isSubmitting = ref(false);
const isLoadingXa = ref(false);
const isSubmittingOrCanceling = ref(false);
/* IMAGE */
const imagePreview = ref(null);
const onSelectImage = (e) => {
  const file = e.target.files[0];
  if (!file) return;
  selectedFile.value = file;
  if (file.size > 5 * 1024 * 1024) {
    notify.error("Ảnh tối đa 5MB");
    return;
  }

  imagePreview.value = URL.createObjectURL(file);
};

/* FORM */
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
});

/* ================= VALIDATION ================= */
const { errors, validateForm } = useNhanVienValidation(form);
const { addressErrors, validateAddress } = useAddressValidation(form);

/* ================= ADDRESS ================= */
const tinhList = ref([]);
const xaList = ref([]);
const selectedTinh = ref("");

const tinhKeyword = ref("");
const xaKeyword = ref("");

const showTinhDropdown = ref(false);
const showXaDropdown = ref(false);

/* LOAD */
const loadTinh = async () => {
  const res = await axios.get("https://34tinhthanh.com/api/provinces");
  tinhList.value = res.data.map((t) => ({
    code: t.province_code,
    name: t.name || "",
  }));
};

const loadXaByTinh = async (code) => {
  if (!code) return;

  try {
    isLoadingXa.value = true;

    const res = await axios.get(
      `https://34tinhthanh.com/api/wards?province_code=${code}`,
    );

    console.log("XA API:", res.data);

    const raw = res.data?.data || res.data?.wards || res.data || [];

    xaList.value = raw.map((x) => ({
      code: x.ward_code || x.id,
      name: x.ward_name || x.full_name || x.name || "Không rõ",
    }));
  } catch (err) {
    console.error("Load xã lỗi:", err);
    xaList.value = [];
  } finally {
    isLoadingXa.value = false;
  }
};

// /* WATCH */
// watch(selectedTinh, async (code) => {
//   xaList.value = [];
//   xaKeyword.value = "";
//   form.xaPhuong = "";

//   if (code) {
//     await loadXaByTinh(code);
//   }
// });

/* FILTER (FIX LỖI Ở ĐÂY) */
const filteredTinhList = computed(() =>
  tinhList.value.filter((t) =>
    (t.name || "")
      .toLowerCase()
      .includes((tinhKeyword.value || "").toLowerCase()),
  ),
);

const filteredXaList = computed(() =>
  xaList.value.filter((x) =>
    (x.name || "")
      .toLowerCase()
      .includes((xaKeyword.value || "").toLowerCase()),
  ),
);

/* SELECT */
const selectTinh = async (tinh) => {
  selectedTinh.value = tinh.code;
  tinhKeyword.value = tinh.name;
  form.tinhThanh = tinh.name;

  xaList.value = [];
  xaKeyword.value = "";
  form.xaPhuong = "";

  showTinhDropdown.value = false;

  await loadXaByTinh(tinh.code);
};
const selectXa = (xa) => {
  xaKeyword.value = xa.name;
  form.xaPhuong = xa.name;
  showXaDropdown.value = false;
};

/* BLUR */
const hideTinh = () => setTimeout(() => (showTinhDropdown.value = false), 200);
const hideXa = () =>
  setTimeout(() => {
    showXaDropdown.value = false;
  }, 200);

/* ================= QR ================= */

const showQrModal = ref(false);
let html5QrCode = null;

const scanCCCD = async () => {
  showQrModal.value = true;
  await nextTick();

  html5QrCode = new Html5Qrcode("qr-reader");

  await html5QrCode.start(
    { facingMode: "environment" },
    { fps: 10 },
    async (decodedText) => {
      const parts = decodedText.split("|");

      form.tenNhanVien = parts[2]?.trim() || "";

      const dob = (parts[3] || "").replace(/\D/g, "");

      if (dob.length === 8) {
        const day = dob.slice(0, 2);
        const month = dob.slice(2, 4);
        const year = dob.slice(4, 8);

        form.ngaySinh = `${year}-${month}-${day}`;
      }

      form.gioiTinh = parts[4]?.trim() === "Nam";

      // 👉 TẮT CAMERA NGAY 1 LẦN DUY NHẤT
      try {
        await html5QrCode.stop();
        await html5QrCode.clear();
      } catch (e) {}

      html5QrCode = null;
      showQrModal.value = false;
    },
  );
};

const closeQr = async () => {
  if (html5QrCode) {
    try {
      await html5QrCode.stop();
    } catch (e) {}

    try {
      await html5QrCode.clear();
    } catch (e) {}

    html5QrCode = null;
  }

  showQrModal.value = false;
};

/* INIT */
const handleBeforeUnload = (e) => {
  if (isSubmittingOrCanceling.value || !hasUnsavedChanges.value) return;
  e.preventDefault();
  e.returnValue = "";
};

onMounted(async () => {
  await loadTinh();
  gioiTinhList.value = await getAllGioiTinh();
  console.log(gioiTinhList.value);
  chucVuList.value = await getAllChucVu();
  window.addEventListener("beforeunload", handleBeforeUnload);
});

onUnmounted(() => {
  window.removeEventListener("beforeunload", handleBeforeUnload);
});

const hasUnsavedChanges = computed(() => {
  return (
    form.tenNhanVien ||
    form.ngaySinh ||
    form.gioiTinh !== null ||
    form.soDienThoai ||
    form.email ||
    form.chucVu ||
    form.tinhThanh ||
    form.xaPhuong ||
    form.diaChiChiTiet ||
    selectedFile.value !== null
  );
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

/* SUBMIT */
const submit = async () => {
  if (!validateForm() || !validateAddress()) return;

  const formData = new FormData();

  // ✅ PHẢI CÓ "data"
  formData.append(
    "data",
    new Blob([JSON.stringify(form)], {
      type: "application/json",
    }),
  );

  // ✅ file
  if (selectedFile.value) {
    formData.append("anh", selectedFile.value);
  }

  try {
    isSubmitting.value = true;

    await addNhanVien(formData);

    notify.success("Thêm nhân viên thành công");
    isSubmittingOrCanceling.value = true;
    router.push("/admin/nhan-vien");
  } catch (err) {
    console.error(err);

    const msg = err.response?.data?.message || "Có lỗi xảy ra";

    if (msg.includes("Email")) {
      errorsNhanVien.email = msg;
    } else if (msg.includes("SĐT") || msg.includes("Số điện thoại")) {
      errorsNhanVien.soDienThoai = msg;
    } else {
      notify.error(msg);
    }
  } finally {
    isSubmitting.value = false;
  }
};

const cancel = () => {
  window.history.back();
};
</script>

<template>
  <div class="page">
    <div class="box">
      <div class="box-header-flex">
        <div class="box-header">
          <h2 class="main-title">
            <span class="icon-wrapper">
              <i
                class="bi bi-person-badge"
                style="color: rgb(225, 29, 72); font-size: 24px"
              ></i>
            </span>
            Thêm Nhân Viên Mới
          </h2>
          <p>Thiết lập hồ sơ nhân sự và thông tin liên hệ của nhân viên</p>
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
        <!-- cccd quét -->
        <div class="full qr-box">
          <div class="qr-content">
            <img src="@/assets/icons/cccdqr.png" class="qr-icon" />

            <div class="qr-info">
              <h4>Quét CCCD tự động</h4>
              <p>Tự động lấy họ tên từ mã QR trên CCCD</p>
            </div>

            <button type="button" class="scan-btn" @click="scanCCCD">
              Quét ngay
            </button>
          </div>
        </div>

        <div>
          <label class="form-label">
            Họ và tên<span class="text-required">*</span>
          </label>
          <input
            v-model="form.tenNhanVien"
            placeholder="Nhập họ tên đầy đủ"
            :class="{ 'input-invalid': errors.tenNhanVien }"
            @input="errors.tenNhanVien = ''"
          />
          <transition name="shake">
            <p class="error-hint" v-if="errors.tenNhanVien">
              <i class="bi bi-exclamation-circle"></i>
              {{ errors.tenNhanVien }}
            </p>
          </transition>
        </div>

        <div>
          <label class="form-label">
            Ngày sinh<span class="text-required">*</span>
          </label>
          <input
            v-model="form.ngaySinh"
            type="date"
            placeholder="dd/MM/yyyy"
            :class="{ 'input-invalid': errors.ngaySinh }"
            @change="errors.ngaySinh = ''"
          />
          <transition name="shake">
            <p class="error-hint" v-if="errors.ngaySinh">
              <i class="bi bi-exclamation-circle"></i>
              {{ errors.ngaySinh }}
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
                @change="errors.gioiTinh = ''"
              />
              {{ gt.label }}
            </label>
          </div>
          <transition name="shake">
            <p class="error-hint" v-if="errors.gioiTinh">
              <i class="bi bi-exclamation-circle"></i>
              {{ errors.gioiTinh }}
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
            :class="{ 'input-invalid': errors.soDienThoai }"
            @input="errors.soDienThoai = ''"
          />
          <transition name="shake">
            <p class="error-hint" v-if="errors.soDienThoai">
              <i class="bi bi-exclamation-circle"></i>
              {{ errors.soDienThoai }}
            </p>
          </transition>
        </div>

        <div>
          <label class="form-label">
            Email<span class="text-required">*</span>
          </label>
          <input
            v-model="form.email"
            placeholder="example@gmail.com"
            :class="{ 'input-invalid': errors.email }"
            @input="errors.email = ''"
          />
          <transition name="shake">
            <p class="error-hint" v-if="errors.email">
              <i class="bi bi-exclamation-circle"></i>
              {{ errors.email }}
            </p>
          </transition>
        </div>

        <div>
          <label class="form-label">
            Chức vụ<span class="text-required">*</span>
          </label>
          <select
            v-model="form.chucVu"
            :class="{ 'input-invalid': errors.chucVu }"
            @change="errors.chucVu = ''"
          >
            <option value="" disabled>Chọn chức vụ</option>

            <option v-for="cv in chucVuList" :key="cv.value" :value="cv.value">
              {{ cv.label }}
            </option>
          </select>
          <transition name="shake">
            <p class="error-hint" v-if="errors.chucVu">
              <i class="bi bi-exclamation-circle"></i>
              {{ errors.chucVu }}
            </p>
          </transition>
        </div>

        <div class="col-3 address-title">
          <img src="@/assets/icons/diachiic.png" /> ĐỊA CHỈ LIÊN HỆ
        </div>

        <!-- TỈNH -->
        <div class="autocomplete" tabindex="0" @blur="hideTinh">
          <label>Tỉnh / Thành phố </label><span class="text-required">*</span>
          <input
            v-model="tinhKeyword"
            placeholder="Nhập tỉnh..."
            :class="{ 'input-invalid': addressErrors.tinhThanh }"
            @focus="showTinhDropdown = true"
            @input="addressErrors.tinhThanh = ''"
          />

          <ul v-if="showTinhDropdown" class="dropdown">
            <li
              v-for="t in filteredTinhList"
              :key="t.code"
              @mousedown.prevent="selectTinh(t)"
            >
              {{ t.name }}
            </li>
          </ul>
          <transition name="shake">
            <p class="error-hint" v-if="addressErrors.tinhThanh">
              <i class="bi bi-exclamation-circle"></i>
              {{ addressErrors.tinhThanh }}
            </p>
          </transition>
        </div>

        <!-- XÃ -->
        <div class="autocomplete" tabindex="0" @blur="hideXa">
          <label>Phường / Xã </label><span class="text-required">*</span>
          <input
            v-model="xaKeyword"
            placeholder="Nhập xã..."
            :disabled="!selectedTinh"
            :class="{ 'input-invalid': addressErrors.xaPhuong }"
            @focus="selectedTinh && (showXaDropdown = true)"
            @input="addressErrors.xaPhuong = ''"
          />

          <ul v-if="showXaDropdown && selectedTinh" class="dropdown">
            <li v-if="isLoadingXa" class="empty">Đang tải...</li>

            <li
              v-for="x in filteredXaList"
              :key="x.code"
              @mousedown.prevent="selectXa(x)"
            >
              {{ x.name }}
            </li>

            <li
              v-if="!isLoadingXa && filteredXaList.length === 0"
              class="empty"
            >
              Không có dữ liệu
            </li>
          </ul>
          <transition name="shake">
            <p class="error-hint" v-if="addressErrors.xaPhuong">
              <i class="bi bi-exclamation-circle"></i>
              {{ addressErrors.xaPhuong }}
            </p>
          </transition>
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
    <!-- QR MODAL -->
    <teleport to="body">
      <div v-if="showQrModal" class="qr-overlay">
        <div class="qr-modal">
          <h3>Quét QR CCCD</h3>
          <div id="qr-reader" style="width: 300px; height: 300px"></div>

          <button
            type="button"
            class="btn-cancel"
            @click="closeQr"
            style="margin-top: 16px"
          >
            Đóng
          </button>
        </div>
      </div>
    </teleport>
  </div>
</template>

<style scoped>
.page {
  background: #f7f8fa;
  padding: 7px;
  font-family: system-ui;
}

h2 {
  margin-bottom: 16px;
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
  color: black;
}

.dropdown li {
  padding: 10px;
  cursor: pointer;
  color: #111; /* 🔥 FIX HIỂN THỊ CHỮ */
}

.dropdown li:hover {
  background: #f3f4f6;
}

.dropdown .empty {
  text-align: center;
  color: #999;
}
.error {
  color: #dc3545;
  font-size: 13px;
}
.qr-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.qr-modal {
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  text-align: center;
}
/* ===== QR CARD ===== */

.qr-box {
  background: linear-gradient(135deg, #fff8f8, #fff);
  border: 1px solid #f0d6d6;
  border-radius: 14px;
  padding: 18px;
}

.qr-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.qr-icon {
  width: 60px;
  height: 60px;
}

.qr-info {
  flex: 1;
  margin-left: 16px;
}

.qr-info h4 {
  margin: 0;
  font-size: 18px;
  color: #222;
}

.qr-info p {
  margin-top: 4px;
  color: #666;
  font-size: 14px;
}

.scan-btn {
  background: var(--color-primary, #e63946);
  color: white;
  border: none;
  border-radius: 10px;
  padding: 12px 20px;
  cursor: pointer;
  font-weight: 600;
  transition: 0.2s;
}

.scan-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(187, 101, 101, 0.25);
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
  align-items: center; /* căn giữa dọc */
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
  background-color: #e2e6ea;
  transform: translateX(-2px);
}

.icon-btn-back img {
  width: 20px;
  height: 20px;
  object-fit: contain;
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
</style>
