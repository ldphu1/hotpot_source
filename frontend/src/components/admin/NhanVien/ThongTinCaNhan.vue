<script setup>
import { reactive, ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { profile, updateNhanVien } from "@/api/admin/nhanVien/nhanVienApi";
import iconBack from "@/assets/icons/prev.png";
import { useUtils } from "@/utils/helper";

const { notify, nprogress } = useUtils();
const router = useRouter();

/* ============ STATE ============ */
const isLoading = ref(true);
const isSubmitting = ref(false);
const maNhanVien = ref("");
const nhanVienId = ref(null);

const form = reactive({
  tenNhanVien: "",
  ngaySinh: "",
  soDienThoai: "",
  email: "",
  gioiTinh: null,
  chucVu: null,
  diaChiChiTiet: "",
  xaPhuong: "",
  tinhThanh: "",
  canCuocCongDan: "",
  anhNhanVien: null,
});

const errors = reactive({
  tenNhanVien: "",
  ngaySinh: "",
  soDienThoai: "",
  email: "",
  tinhThanh: "",
  xaPhuong: "",
  diaChiChiTiet: "",
});

const getImageUrl = (img) => {
  if (!img) return null;
  return `http://localhost:8080/uploads/${img}`;
};

const imagePreview = computed(() => {
  if (form.anhNhanVien instanceof File) {
    return URL.createObjectURL(form.anhNhanVien);
  }
  if (typeof form.anhNhanVien === "string" && form.anhNhanVien) {
    return getImageUrl(form.anhNhanVien);
  }
  return null;
});
/* ============ LABEL HELPERS ============ */
const chucVuLabel = computed(() => {
  const map = { 1: "Quản lý", 0: "Nhân viên", 2: "Thu ngân" };
  return map[form.chucVu] || `Chức vụ ${form.chucVu}`;
});

/* ============ LOAD PROFILE ============ */
const loadProfile = async () => {
  try {
    isLoading.value = true;
    const res = await profile();
    const nv = res.data;

    nhanVienId.value = nv.id;
    maNhanVien.value = nv.maNhanVien || "";
    form.tenNhanVien = nv.tenNhanVien || "";
    form.ngaySinh = nv.ngaySinh?.slice(0, 10) || "";
    form.soDienThoai = nv.soDienThoai || "";
    form.email = nv.email || "";
    form.gioiTinh = nv.gioiTinh;
    form.chucVu = nv.chucVu;
    form.diaChiChiTiet = nv.diaChiChiTiet || "";
    form.xaPhuong = nv.xaPhuong || "";
    form.tinhThanh = nv.tinhThanh || "";
    form.canCuocCongDan = nv.canCuocCongDan || "";
    form.anhNhanVien = nv.anhNhanVien || null;

    provinceSearch.value = form.tinhThanh || "";
    wardSearch.value = form.xaPhuong || "";

    if (form.tinhThanh) {
      const p = provinces.value.find(
        (x) => x.name.toLowerCase() === form.tinhThanh.toLowerCase(),
      );
      if (p) {
        selectedProvinceCode.value = p.code;
        await loadWards(p.code);
      }
    }
  } catch (err) {
    notify.error("Không thể tải thông tin cá nhân");
  } finally {
    isLoading.value = false;
  }
};

/* ============ COMBOBOX ĐỊA CHỈ V2 ============ */
const provinces = ref([]);
const wards = ref([]);
const showProvinceDropdown = ref(false);
const showWardDropdown = ref(false);
const provinceSearch = ref("");
const wardSearch = ref("");
const selectedProvinceCode = ref(null);

const hideProvinceDropdown = () => {
  setTimeout(() => {
    showProvinceDropdown.value = false;
    const current = provinces.value.find((p) => p.name === form.tinhThanh);
    provinceSearch.value = current ? current.name : form.tinhThanh;
  }, 200);
};

const hideWardDropdown = () => {
  setTimeout(() => {
    showWardDropdown.value = false;
    const current = wards.value.find((w) => w.name === form.xaPhuong);
    wardSearch.value = current ? current.name : form.xaPhuong;
  }, 200);
};

const filteredProvinces = computed(() => {
  const q = provinceSearch.value.trim().toLowerCase();
  if (!q) return provinces.value;
  return provinces.value.filter((p) => p.name.toLowerCase().includes(q));
});

const filteredWards = computed(() => {
  const q = wardSearch.value.trim().toLowerCase();
  if (!q) return wards.value;
  return wards.value.filter((w) => w.name.toLowerCase().includes(q));
});

const loadProvinces = async () => {
  try {
    const res = await fetch("https://provinces.open-api.vn/api/v2/p/");
    provinces.value = await res.json();
  } catch (e) {
    console.error("Lỗi tải danh sách tỉnh/thành", e);
  }
};

const loadWards = async (provinceCode) => {
  try {
    const res = await fetch(
      `https://provinces.open-api.vn/api/v2/p/${provinceCode}?depth=2`,
    );
    const data = await res.json();
    wards.value = data.wards || [];
  } catch (e) {
    console.error("Lỗi tải danh sách phường/xã", e);
  }
};

const selectProvince = async (p) => {
  provinceSearch.value = p.name;
  form.tinhThanh = p.name;
  selectedProvinceCode.value = p.code;
  showProvinceDropdown.value = false;

  // Reset xã
  wardSearch.value = "";
  form.xaPhuong = "";
  wards.value = [];

  await loadWards(p.code);
};

const selectWard = (w) => {
  wardSearch.value = w.name;
  form.xaPhuong = w.name;
  showWardDropdown.value = false;
};

/* ============ IMAGE ============ */
const fileInputRef = ref(null);

const triggerFileInput = () => {
  fileInputRef.value?.click();
};

const onSelectImage = (e) => {
  const file = e.target.files[0];
  if (!file) return;
  if (file.size > 5 * 1024 * 1024) {
    notify.error("Ảnh tối đa 5MB");
    return;
  }
  form.anhNhanVien = file;
};

/* ============ SUBMIT ============ */
const submit = async () => {
  let hasError = false;
  Object.keys(errors).forEach((key) => (errors[key] = ""));

  if (!form.tenNhanVien?.trim()) {
    errors.tenNhanVien = "Tên nhân viên không được để trống";
    hasError = true;
  } else if (!/^[A-Za-zÀ-ỹ]+( [A-Za-zÀ-ỹ]+)*$/.test(form.tenNhanVien.trim())) {
    errors.tenNhanVien = "Tên nhân viên không hợp lệ";
    hasError = true;
  }

  if (!form.ngaySinh) {
    errors.ngaySinh = "Ngày sinh không được để trống";
    hasError = true;
  } else if (new Date(form.ngaySinh) >= new Date()) {
    errors.ngaySinh = "Ngày sinh phải nhỏ hơn ngày hiện tại";
    hasError = true;
  }

  if (!form.soDienThoai?.trim()) {
    errors.soDienThoai = "SĐT không được để trống";
    hasError = true;
  } else if (!/^0[35789][0-9]{8}$/.test(form.soDienThoai.trim())) {
    errors.soDienThoai = "SĐT không hợp lệ";
    hasError = true;
  }

  if (!form.email?.trim()) {
    errors.email = "Email không được để trống";
    hasError = true;
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) {
    errors.email = "Email không đúng định dạng";
    hasError = true;
  }

  if (!form.tinhThanh?.trim()) {
    errors.tinhThanh = "Vui lòng chọn tỉnh/thành";
    hasError = true;
  }

  if (!form.xaPhuong?.trim()) {
    errors.xaPhuong = "Vui lòng chọn xã/phường";
    hasError = true;
  }

  if (!form.diaChiChiTiet?.trim()) {
    errors.diaChiChiTiet = "Vui lòng nhập địa chỉ chi tiết";
    hasError = true;
  }

  if (hasError) {
    notify.error("Vui lòng kiểm tra lại thông tin");
    return;
  }

  const ok = await notify.confirm("Xác nhận lưu thay đổi?");
  if (!ok) return;

  isSubmitting.value = true;
  nprogress.start();

  try {
    const fd = new FormData();
    const dto = {
      tenNhanVien: form.tenNhanVien.trim(),
      soDienThoai: form.soDienThoai.trim(),
      email: form.email.trim(),
      diaChiChiTiet: form.diaChiChiTiet?.trim() || "",
      ngaySinh: form.ngaySinh,
      gioiTinh: form.gioiTinh,
      chucVu: form.chucVu,
      tinhThanh: form.tinhThanh?.trim() || "",
      xaPhuong: form.xaPhuong?.trim() || "",
    };

    fd.append(
      "data",
      new Blob([JSON.stringify(dto)], { type: "application/json" }),
    );

    if (form.anhNhanVien instanceof File) {
      fd.append("anhNhanVien", form.anhNhanVien);
    }

    const id = nhanVienId.value;
    const res = await updateNhanVien(id, fd);

    if (res?.data?.anhNhanVien) {
      form.anhNhanVien = res.data.anhNhanVien;
    }

    notify.success("Cập nhật thông tin thành công!");
  } catch (err) {
    const msg = err.response?.data?.message || "Có lỗi xảy ra";
    notify.error(msg);
  } finally {
    isSubmitting.value = false;
    nprogress.done();
  }
};

/* ============ LIFECYCLE ============ */
onMounted(async () => {
  await loadProvinces();
  await loadProfile();
});
</script>

<template>
  <div class="profile-page">
    <!-- Header box giống ThemNhanVien -->
    <div class="box">
      <div class="box-header-flex">
        <div class="box-header">
          <h2 class="main-title">
            <span class="icon-wrapper">
              <i
                class="bi bi-person-circle"
                style="color: rgb(225, 29, 72); font-size: 24px"
              ></i>
            </span>
            Thông Tin Cá Nhân
          </h2>
          <p class="subtitle">Xem và cập nhật thông tin hồ sơ của bạn</p>
        </div>

        <button class="icon-btn-back" @click="router.back()">
          <img :src="iconBack" />
        </button>
      </div>
    </div>

    <!-- Loading skeleton -->
    <div v-if="isLoading" class="skeleton-wrapper">
      <div class="skeleton-avatar"></div>
      <div class="skeleton-lines">
        <div class="skeleton-line" v-for="i in 6" :key="i"></div>
      </div>
    </div>

    <!-- Content -->
    <div v-else class="profile-content">
      <!-- LEFT: Avatar + badge -->
      <div class="profile-left">
        <div class="avatar-card">
          <!-- Avatar ring -->
          <div class="avatar-ring">
            <img
              v-if="imagePreview"
              :src="imagePreview"
              class="avatar-img"
              alt="avatar"
            />
            <div v-else class="avatar-placeholder">
              <i class="bi bi-person-fill"></i>
            </div>

            <!-- Camera overlay -->
            <div class="avatar-overlay" @click="triggerFileInput">
              <i class="bi bi-camera-fill"></i>
              <span>Đổi ảnh</span>
            </div>
          </div>

          <input
            ref="fileInputRef"
            type="file"
            accept="image/*"
            class="hidden-input"
            @change="onSelectImage"
          />

          <!-- Tên và chức vụ -->
          <div class="avatar-name">{{ form.tenNhanVien || "—" }}</div>
          <div class="avatar-role">
            <span class="role-badge">{{ chucVuLabel }}</span>
          </div>
          <div class="avatar-id">{{ maNhanVien }}</div>

          <!-- Nút thay đổi ảnh -->
          <button class="btn-change-photo" @click="triggerFileInput">
            <i class="bi bi-camera"></i>
            Thay đổi ảnh
          </button>
        </div>
      </div>

      <!-- RIGHT: form chỉnh sửa -->
      <div class="profile-right">
        <!-- Section: Thông tin cơ bản -->
        <div class="info-section">
          <div class="section-header">
            <i class="bi bi-person-vcard section-icon"></i>
            <span>Thông tin cơ bản</span>
          </div>

          <div class="form-grid">
            <!-- Họ tên -->
            <div class="form-group">
              <label class="form-label">
                <i class="bi bi-person"></i> Họ và tên
                <span class="required">*</span>
              </label>
              <input
                v-model="form.tenNhanVien"
                @input="errors.tenNhanVien = ''"
                class="form-input"
                :class="{ 'input-invalid': errors.tenNhanVien }"
                placeholder="Nhập họ và tên"
              />
              <transition name="shake">
                <p v-if="errors.tenNhanVien" class="error-hint">
                  <i class="bi bi-exclamation-circle"></i>
                  {{ errors.tenNhanVien }}
                </p>
              </transition>
            </div>

            <!-- Mã NV (readonly) -->
            <div class="form-group">
              <label class="form-label">
                <i class="bi bi-badge-id"></i> Mã nhân viên
              </label>
              <input :value="maNhanVien" class="form-input readonly" readonly />
            </div>

            <!-- Ngày sinh -->
            <div class="form-group">
              <label class="form-label">
                <i class="bi bi-calendar3"></i> Ngày sinh
                <span class="required">*</span>
              </label>
              <input 
                v-model="form.ngaySinh" 
                @input="errors.ngaySinh = ''"
                type="date" 
                class="form-input" 
                :class="{ 'input-invalid': errors.ngaySinh }"
              />
              <transition name="shake">
                <p v-if="errors.ngaySinh" class="error-hint">
                  <i class="bi bi-exclamation-circle"></i>
                  {{ errors.ngaySinh }}
                </p>
              </transition>
            </div>

            <!-- Giới tính -->
            <div class="form-group">
              <label class="form-label">
                <i class="bi bi-gender-ambiguous"></i> Giới tính
                <span class="required">*</span>
              </label>
              <div class="gender-row">
                <label
                  class="gender-option"
                  :class="{ active: form.gioiTinh === true }"
                >
                  <input type="radio" v-model="form.gioiTinh" :value="true" />
                  <i class="bi bi-gender-male"></i> Nam
                </label>
                <label
                  class="gender-option"
                  :class="{ active: form.gioiTinh === false }"
                >
                  <input type="radio" v-model="form.gioiTinh" :value="false" />
                  <i class="bi bi-gender-female"></i> Nữ
                </label>
              </div>
            </div>

            <!-- CCCD -->
            <div class="form-group">
              <label class="form-label">
                <i class="bi bi-card-text"></i> CCCD
              </label>
              <input
                :value="form.canCuocCongDan"
                class="form-input readonly"
                readonly
              />
            </div>

            <!-- Chức vụ -->
            <div class="form-group">
              <label class="form-label">
                <i class="bi bi-briefcase"></i> Chức vụ
              </label>
              <input
                :value="chucVuLabel"
                class="form-input readonly"
                readonly
              />
            </div>
          </div>
        </div>

        <!-- Section: Liên hệ -->
        <div class="info-section">
          <div class="section-header">
            <i class="bi bi-telephone section-icon"></i>
            <span>Thông tin liên hệ</span>
          </div>

          <div class="form-grid">
            <!-- Email -->
            <div class="form-group">
              <label class="form-label">
                <i class="bi bi-envelope"></i> Email
                <span class="required">*</span>
              </label>
              <input
                v-model="form.email"
                @input="errors.email = ''"
                class="form-input"
                :class="{ 'input-invalid': errors.email }"
                placeholder="email@example.com"
                type="email"
              />
              <transition name="shake">
                <p v-if="errors.email" class="error-hint">
                  <i class="bi bi-exclamation-circle"></i>
                  {{ errors.email }}
                </p>
              </transition>
            </div>

            <!-- Số điện thoại -->
            <div class="form-group">
              <label class="form-label">
                <i class="bi bi-phone"></i> Số điện thoại
                <span class="required">*</span>
              </label>
              <input
                v-model="form.soDienThoai"
                @input="errors.soDienThoai = ''"
                class="form-input"
                :class="{ 'input-invalid': errors.soDienThoai }"
                placeholder="VD: 0987..."
              />
              <transition name="shake">
                <p v-if="errors.soDienThoai" class="error-hint">
                  <i class="bi bi-exclamation-circle"></i>
                  {{ errors.soDienThoai }}
                </p>
              </transition>
            </div>
          </div>
        </div>

        <!-- Section: Địa chỉ -->
        <div class="info-section">
          <div class="section-header">
            <i class="bi bi-geo-alt section-icon"></i>
            <span>Địa chỉ</span>
          </div>

          <div class="form-grid">
            <div class="form-group combobox-group">
              <label class="form-label">
                <i class="bi bi-map"></i> Tỉnh / Thành phố
                <span class="required">*</span>
              </label>
              <div class="combobox-wrapper">
                <input
                  v-model="provinceSearch"
                  class="form-input"
                  :class="{ 'input-invalid': errors.tinhThanh }"
                  placeholder="Tìm và chọn tỉnh/thành phố..."
                  @focus="showProvinceDropdown = true; errors.tinhThanh = ''"
                  @blur="hideProvinceDropdown"
                />
                <i class="bi bi-chevron-down combobox-arrow"></i>
                <ul v-if="showProvinceDropdown" class="combobox-dropdown">
                  <li
                    v-for="p in filteredProvinces"
                    :key="p.code"
                    :class="{ selected: form.tinhThanh === p.name }"
                    @mousedown="selectProvince(p)"
                  >
                    {{ p.name }}
                  </li>
                  <li v-if="filteredProvinces.length === 0" class="no-options">
                    Không tìm thấy tỉnh/thành
                  </li>
                </ul>
              </div>
              <transition name="shake">
                <p v-if="errors.tinhThanh" class="error-hint">
                  <i class="bi bi-exclamation-circle"></i>
                  {{ errors.tinhThanh }}
                </p>
              </transition>
            </div>

            <div class="form-group combobox-group">
              <label class="form-label">
                <i class="bi bi-signpost"></i> Phường / Xã
                <span class="required">*</span>
              </label>
              <div class="combobox-wrapper">
                <input
                  v-model="wardSearch"
                  class="form-input"
                  :class="{ 'input-invalid': errors.xaPhuong }"
                  placeholder="Tìm và chọn phường/xã..."
                  @focus="showWardDropdown = true; errors.xaPhuong = ''"
                  @blur="hideWardDropdown"
                  :disabled="!selectedProvinceCode"
                />
                <i class="bi bi-chevron-down combobox-arrow"></i>
                <ul v-if="showWardDropdown" class="combobox-dropdown">
                  <li
                    v-for="w in filteredWards"
                    :key="w.code"
                    :class="{ selected: form.xaPhuong === w.name }"
                    @mousedown="selectWard(w)"
                  >
                    {{ w.name }}
                  </li>
                  <li v-if="filteredWards.length === 0" class="no-options">
                    Không tìm thấy phường/xã
                  </li>
                </ul>
              </div>
              <transition name="shake">
                <p v-if="errors.xaPhuong" class="error-hint">
                  <i class="bi bi-exclamation-circle"></i>
                  {{ errors.xaPhuong }}
                </p>
              </transition>
            </div>

            <div class="form-group form-group--full">
              <label class="form-label">
                <i class="bi bi-house"></i> Địa chỉ chi tiết
                <span class="required">*</span>
              </label>
              <input
                v-model="form.diaChiChiTiet"
                @input="errors.diaChiChiTiet = ''"
                class="form-input"
                :class="{ 'input-invalid': errors.diaChiChiTiet }"
                placeholder="Số nhà, đường, ngõ..."
              />
              <transition name="shake">
                <p v-if="errors.diaChiChiTiet" class="error-hint">
                  <i class="bi bi-exclamation-circle"></i>
                  {{ errors.diaChiChiTiet }}
                </p>
              </transition>
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="actions">
          <button class="btn-cancel" @click="router.back()">Hủy bỏ</button>
          <button class="btn-submit" :disabled="isSubmitting" @click="submit">
            <i class="bi bi-check2-all"></i>
            <div class="btn-light-sweep"></div>
            Xác nhận
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ========== FONT ========== */
.profile-page {
  background: #f7f8fa;
  padding: 10px;
  font-family: "Inter", system-ui, sans-serif;
  min-height: 100vh;
}

/* ========== HEADER BOX (giống ThemNhanVien) ========== */
.box {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
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

.main-title {
  display: flex;
  align-items: center;
  font-size: 26px;
  font-weight: 700;
  gap: 12px;
  letter-spacing: 0.5px;
  margin-bottom: 4px;
  color: #1a1a1a;
}

.icon-wrapper {
  width: 52px;
  height: 52px;
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

.subtitle {
  color: #6b7280;
  font-size: 13.5px;
  margin: 0;
}

.icon-btn-back {
  margin-left: auto;
  flex-shrink: 0;
  width: 44px;
  height: 44px;
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
  width: 20px;
  height: 20px;
}

/* ========== SKELETON ========== */
.skeleton-wrapper {
  display: flex;
  gap: 24px;
  animation: shimmer 1.5s infinite;
}

.skeleton-avatar {
  width: 280px;
  height: 400px;
  background: linear-gradient(90deg, #e8e8e8 25%, #f5f5f5 50%, #e8e8e8 75%);
  background-size: 400% 100%;
  border-radius: 16px;
  flex-shrink: 0;
  animation: skeleton-wave 1.5s ease-in-out infinite;
}

.skeleton-lines {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-top: 20px;
}

.skeleton-line {
  height: 48px;
  border-radius: 10px;
  background: linear-gradient(90deg, #e8e8e8 25%, #f5f5f5 50%, #e8e8e8 75%);
  background-size: 400% 100%;
  animation: skeleton-wave 1.5s ease-in-out infinite;
}

@keyframes skeleton-wave {
  0% {
    background-position: 100% 0;
  }
  100% {
    background-position: -100% 0;
  }
}

/* ========== MAIN LAYOUT ========== */
.profile-content {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 20px;
  align-items: start;
}

/* ========== LEFT CARD ========== */
.avatar-card {
  background: #fff;
  border-radius: 16px;
  padding: 28px 20px;
  border: 1px solid #eee;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  position: sticky;
  top: 20px;
}

/* Avatar ring with gradient border */
.avatar-ring {
  position: relative;
  width: 140px;
  height: 140px;
  border-radius: 50%;
  padding: 3px;
  background: linear-gradient(135deg, #e11d48, #fb923c, #e11d48);
  cursor: pointer;
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #fff;
  display: block;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: #f3f4f6;
  border: 3px solid #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 52px;
  color: #d1d5db;
}

.avatar-overlay {
  position: absolute;
  inset: 3px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: white;
  font-size: 13px;
  font-weight: 500;
  opacity: 0;
  transition: opacity 0.2s ease;
  cursor: pointer;
}

.avatar-ring:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay i {
  font-size: 22px;
}

.hidden-input {
  display: none;
}

.avatar-name {
  font-size: 17px;
  font-weight: 700;
  color: #1a1a1a;
  text-align: center;
  line-height: 1.3;
}

.avatar-role {
  margin-top: -4px;
}

.role-badge {
  display: inline-block;
  padding: 3px 12px;
  border-radius: 20px;
  background: linear-gradient(
    135deg,
    rgba(225, 29, 72, 0.1),
    rgba(225, 29, 72, 0.18)
  );
  color: #e11d48;
  font-size: 12px;
  font-weight: 600;
  border: 1px solid rgba(225, 29, 72, 0.15);
}

.avatar-id {
  font-size: 12px;
  color: #9ca3af;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.btn-change-photo {
  width: 100%;
  padding: 10px;
  border-radius: 10px;
  border: 1.5px dashed #e11d48;
  background: rgba(225, 29, 72, 0.04);
  color: #e11d48;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s;
  margin-top: 4px;
}

.btn-change-photo:hover {
  background: rgba(225, 29, 72, 0.1);
  border-style: solid;
  transform: translateY(-1px);
}

/* ========== RIGHT PANEL ========== */
.profile-right {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* Section card */
.info-section {
  background: #fff;
  border-radius: 14px;
  padding: 20px 24px;
  border: 1px solid #eee;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 18px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f3f4f6;
  letter-spacing: 0.3px;
}

.section-icon {
  font-size: 16px;
  color: #e11d48;
}

/* Form grid */
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group--full {
  grid-column: 1 / -1;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  display: flex;
  align-items: center;
  gap: 6px;
}

.form-label i {
  font-size: 13px;
  color: #9ca3af;
}

.required {
  color: #e11d48;
  font-weight: 700;
}

.form-input {
  width: 100%;
  height: 42px;
  padding: 0 12px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  font-size: 14px;
  color: #374151;
  background: #fff;
  transition: all 0.2s;
  outline: none;
  box-sizing: border-box;
  font-family: inherit;
}

.form-input:hover {
  border-color: #c8d0dc;
}

.form-input:focus {
  border-color: #e11d48;
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12);
}

.form-input.readonly {
  background: #f9fafb;
  color: #6b7280;
  cursor: not-allowed;
  border-color: #e5e7eb;
}

/* Gender radio */
.gender-row {
  display: flex;
  gap: 10px;
}

.gender-option {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 14px;
  border-radius: 10px;
  border: 1.5px solid #e5e7eb;
  cursor: pointer;
  font-size: 13.5px;
  font-weight: 500;
  color: #6b7280;
  transition: all 0.18s;
  user-select: none;
}

.gender-option input[type="radio"] {
  display: none;
  width: auto;
  padding: 0;
  border: none;
}

.gender-option.active {
  border-color: #e11d48;
  background: rgba(225, 29, 72, 0.06);
  color: #e11d48;
  font-weight: 600;
}

.gender-option:hover:not(.active) {
  border-color: #d1d5db;
  background: #f9fafb;
}

/* Actions */
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 4px;
}

.btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 11px 22px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.2s;
  font-family: inherit;
}

/* --- BUTTONS --- */
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
.btn-submit:hover .btn-light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
}
.btn-submit:hover .btn-light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
}
.btn-submit {
  position: relative;
  overflow: hidden;
  background: #d32f2f;
  color: white;
  border: none;
  padding: 12px 28px;
  border-radius: 7px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 4px 12px rgba(211, 47, 47, 0.2);
}
.btn-cancel {
  background: #dbdddf;
  border: none;
  padding: 12px 28px;
  border-radius: 7px;
  cursor: pointer;
  font-weight: 600;
  color: #64748b;
}

.btn-save {
  background: linear-gradient(135deg, #e11d48, #be123c);
  color: white;
  box-shadow: 0 4px 14px rgba(225, 29, 72, 0.3);
}

.btn-save:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(225, 29, 72, 0.4);
}

.btn-save:disabled {
  opacity: 0.65;
  cursor: not-allowed;
  transform: none;
}

/* Spin */
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.spin {
  display: inline-block;
  animation: spin 0.8s linear infinite;
}

/* ========== RESPONSIVE ========== */
@media (max-width: 900px) {
  .profile-content {
    grid-template-columns: 1fr;
  }

  .avatar-card {
    position: static;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .profile-page {
    padding: 12px;
  }

  .main-title {
    font-size: 20px;
  }

  .icon-wrapper {
    width: 40px;
    height: 40px;
  }
}

/* ========== COMBOBOX STYLE (ĐỒNG BỘ VỚI DONHANG) ========== */
.combobox-group {
  position: relative;
}

.combobox-wrapper {
  position: relative;
}

/* Đồng bộ input hover và focus */
.combobox-wrapper .form-input {
  transition: all 0.2s ease;
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px 38px 10px 13px; /* Dành chỗ cho arrow */
  height: 42px;
}

.combobox-wrapper .form-input:hover {
  border-color: #c8d0dc;
}

.combobox-wrapper .form-input:focus {
  border-color: #e11d48;
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12);
}

.combobox-arrow {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
  pointer-events: none;
  font-size: 14px;
  transition: transform 0.2s ease, color 0.2s ease;
}

/* Khi dropdown đang mở */
.combobox-wrapper:focus-within .combobox-arrow {
  transform: translateY(-50%) rotate(180deg);
  color: #e11d48;
}

/* Dropdown menu bo tròn độc lập, shadow nhẹ mượt giống v-select của DonHang */
.combobox-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 1000;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  margin-top: 8px;
  max-height: 220px;
  overflow-y: auto;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1),
    0 8px 10px -6px rgba(0, 0, 0, 0.05);
  list-style: none;
  padding: 6px;
}

/* Option items */
.combobox-dropdown li {
  padding: 8px 12px;
  font-size: 13.5px;
  color: #4b5563;
  cursor: pointer;
  border-radius: 8px;
  margin-bottom: 2px;
  transition: all 0.15s ease;
}

/* Hover/Highlight giống v-select của DonHang */
.combobox-dropdown li:hover {
  background: #f3f4f6;
  color: #eb6464;
  padding-left: 12px;
}

/* Trạng thái đã chọn (selected) giống v-select của DonHang */
.combobox-dropdown li.selected {
  background: #f3f5f9;
  color: #fc0101;
  font-weight: 600;
}

.combobox-dropdown .no-options {
  color: #9ca3af;
  text-align: center;
  padding: 12px;
  font-style: italic;
  cursor: default;
}

.combobox-dropdown .no-options:hover {
  background: transparent;
  color: #9ca3af;
}

/* Scrollbar styling for dropdown */
.combobox-dropdown::-webkit-scrollbar {
  width: 6px;
}

.combobox-dropdown::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.combobox-dropdown::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}

.combobox-dropdown::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}

/* ========== VALIDATION ========== */
.input-invalid {
  border-color: #e63946 !important;
  box-shadow: 0 0 0 4px rgba(230, 57, 70, 0.1) !important;
}

.error-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #e63946;
  font-size: 12.5px;
  margin-top: 4px;
  padding: 5px 10px;
  background-color: #fff1f2;
  border-radius: 6px;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-4px); }
  to   { opacity: 1; transform: translateY(0); }
}

.shake-enter-active {
  animation: shake 0.4s cubic-bezier(0.36, 0.07, 0.19, 0.97) both;
}

@keyframes shake {
  10%, 90% { transform: translate3d(-1px, 0, 0); }
  20%, 80% { transform: translate3d(2px, 0, 0); }
  30%, 50%, 70% { transform: translate3d(-4px, 0, 0); }
  40%, 60% { transform: translate3d(4px, 0, 0); }
}
</style>
