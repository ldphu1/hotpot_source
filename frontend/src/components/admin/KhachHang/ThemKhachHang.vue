<script setup>
import { reactive, ref, onMounted, computed, onUnmounted } from "vue";
import { useRouter, onBeforeRouteLeave } from "vue-router";
import axios from "axios";
import iconBack from "@/assets/icons/prev.png";
import { useKhachHangValidation } from "@/composables/useKhachHangValidation";
import { useAddressValidation } from "@/composables/useAddressValidation";

import { useUtils } from "@/utils/helper";
import axiosClient from "@/services/axiosClient";
const { notify, nprogress } = useUtils();

const submitting = ref(false);
const isSubmittingOrCanceling = ref(false);

/* ===== API ĐỊA CHỈ ===== */
const provinces = ref([]);
const wards = ref([]);

const selectedProvinceCode = ref("");
const wardKeyword = ref("");
const showWardDropdown = ref(false);
const filteredWards = computed(() => {
  if (!wardKeyword.value) return wards.value;
  return wards.value.filter((w) =>
    w.name.toLowerCase().includes(wardKeyword.value.toLowerCase()),
  );
});
const selectWard = (w) => {
  wardKeyword.value = w.name;
  addressForm.xaPhuong = w.name;
  // clear lỗi
  addressErrors.xaPhuong = "";
  showWardDropdown.value = false;
};

onMounted(async () => {
  try {
    const res = await axios.get("https://provinces.open-api.vn/api/v2/p");
    provinces.value = res.data;
    console.log("PROVINCES:", provinces.value);
  } catch (e) {
    console.error(e);
  }
});

const onProvinceChange = async () => {
  wards.value = [];

  addressForm.xaPhuong = "";
  wardKeyword.value = "";
  addressErrors.xaPhuong = "";

  const province = provinces.value.find(
    (p) => String(p.code) === selectedProvinceCode.value,
  );

  addressForm.tinhThanh = province?.name || "";

  try {
    const res = await axios.get(
      `https://provinces.open-api.vn/api/p/${selectedProvinceCode.value}?depth=3`,
    );

    console.log("DATA:", res.data);

    const allWards = [];

    // flatten wards
    res.data.districts?.forEach((district) => {
      district.wards?.forEach((ward) => {
        allWards.push({
          code: ward.code,
          name: ward.name,
        });
      });
    });

    wards.value = allWards;

    console.log("WARDS:", wards.value);
  } catch (e) {
    console.error("LOAD WARDS ERROR:", e);
  }
};
const provinceKeyword = ref("");
const showProvinceDropdown = ref(false);

const filteredProvinces = computed(() => {
  if (!provinceKeyword.value) return provinces.value;
  return provinces.value.filter((p) =>
    p.name.toLowerCase().includes(provinceKeyword.value.toLowerCase()),
  );
});

const selectProvince = (p) => {
  provinceKeyword.value = p.name;
  selectedProvinceCode.value = String(p.code);
  addressForm.tinhThanh = p.name;
  // clear lỗi khi chọn lại
  addressErrors.tinhThanh = "";
  addressErrors.xaPhuong = "";
  showProvinceDropdown.value = false;
  onProvinceChange();
};

const provinceBox = ref(null);
const wardBox = ref(null);

const handleClickOutside = (e) => {
  if (provinceBox.value && !provinceBox.value.contains(e.target)) {
    showProvinceDropdown.value = false;
  }

  if (wardBox.value && !wardBox.value.contains(e.target)) {
    showWardDropdown.value = false;
  }
};

const handleBeforeUnload = (e) => {
  if (isSubmittingOrCanceling.value || !hasUnsavedChanges.value) return;
  e.preventDefault();
  e.returnValue = "";
};

onMounted(() => {
  document.addEventListener("click", handleClickOutside);
  window.addEventListener("beforeunload", handleBeforeUnload);
});

onUnmounted(() => {
  document.removeEventListener("click", handleClickOutside);
  window.removeEventListener("beforeunload", handleBeforeUnload);
});

const hasUnsavedChanges = computed(() => {
  return (
    form.hoTen ||
    form.ngaySinh ||
    form.soDienThoai ||
    form.email ||
    form.anhDaiDien !== null ||
    addresses.value.length > 0
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

/* ================= ROUTER ================= */
const router = useRouter();

/* ================= FORM ================= */
const form = reactive({
  hoTen: "",
  ngaySinh: "",
  gioiTinh: true,
  soDienThoai: "",
  email: "",
  anhDaiDien: null,
});
const { errors, validateForm } = useKhachHangValidation(form);

/* ================= IMAGE ================= */
const imagePreview = ref(null);

const onSelectImage = (e) => {
  const file = e.target.files[0];
  if (!file) return;
  e.target.value = "";

  if (!file.type.startsWith("image/")) {
    alert("File không phải ảnh");
    return;
  }

  if (file.size > 5 * 1024 * 1024) {
    alert("Ảnh tối đa 5MB");
    return;
  }

  form.anhDaiDien = file;
  imagePreview.value = URL.createObjectURL(file);
};
onUnmounted(() => {
  if (imagePreview.value) {
    URL.revokeObjectURL(imagePreview.value);
  }
});

/* ================= ADDRESS ================= */
const addresses = ref([]);
const showAddressForm = ref(false);
const editingIndex = ref(null);

const addressForm = reactive({
  tinhThanh: "",
  xaPhuong: "",
  diaChiChiTiet: "",
  macDinh: false,
});
const { addressErrors, validateAddress } = useAddressValidation(addressForm);

const resetAddressForm = () => {
  addressForm.tinhThanh = "";
  addressForm.xaPhuong = "";
  addressForm.diaChiChiTiet = "";
  addressForm.macDinh = false;
  editingIndex.value = null;

  addressErrors.tinhThanh = "";
  addressErrors.xaPhuong = "";
  addressErrors.diaChiChiTiet = "";
};

const closeAddressForm = async () => {
  if (editingIndex.value !== null) {
    const ok = await notify.confirm(
      "Bạn có chắc muốn hủy chỉnh sửa địa chỉ này?",
    );
    if (!ok) return;
  }

  resetAddressForm();
  provinceKeyword.value = "";
  wardKeyword.value = "";
  selectedProvinceCode.value = "";
  wards.value = [];
  showProvinceDropdown.value = false;
  showWardDropdown.value = false;
  showAddressForm.value = false;
};

const openAddressForm = () => {
  resetAddressForm();

  // clear lỗi địa chỉ
  addressErrors.tinhThanh = "";
  addressErrors.xaPhuong = "";
  addressErrors.diaChiChiTiet = "";

  provinceKeyword.value = "";
  wardKeyword.value = "";
  selectedProvinceCode.value = "";
  wards.value = [];

  showAddressForm.value = true;
};

/* ===== SAVE ADDRESS ===== */
const saveAddress = () => {
  if (!validateAddress()) return;

  if (addressForm.macDinh) {
    addresses.value.forEach((a) => (a.macDinh = false));
  }

  if (editingIndex.value !== null) {
    addresses.value[editingIndex.value] = {
      ...addresses.value[editingIndex.value],
      ...addressForm,
    };
  } else {
    //➡️ Địa chỉ đầu tiên sẽ tự động là mặc định.
    const newAddress = { ...addressForm };

    if (addresses.value.length === 0) {
      newAddress.macDinh = true;
    }

    addresses.value.push(newAddress);
  }

  resetAddressForm();
  showAddressForm.value = false;
};

/* ===== EDIT ===== */
const editAddress = async (index) => {
  // clear lỗi cũ
  addressErrors.tinhThanh = "";
  addressErrors.xaPhuong = "";
  addressErrors.diaChiChiTiet = "";

  Object.assign(addressForm, addresses.value[index]);
  editingIndex.value = index;

  const province = provinces.value.find(
    (p) => p.name === addressForm.tinhThanh,
  );

  if (province) {
    selectedProvinceCode.value = String(province.code);
    await onProvinceChange();
  }

  provinceKeyword.value = addressForm.tinhThanh;
  wardKeyword.value = addressForm.xaPhuong;

  showProvinceDropdown.value = false;
  showWardDropdown.value = false;

  showAddressForm.value = true;
};

/* ===== SET DEFAULT ===== */
const setDefault = (index) => {
  addresses.value.forEach((a, i) => {
    a.macDinh = i === index;
  });
};

/* ===== DELETE ===== */
const removeAddress = async (index) => {
  const ok = await notify.confirm("Bạn có chắc muốn xóa địa chỉ này?");
  if (!ok) return;

  const wasDefault = addresses.value[index].macDinh;

  addresses.value.splice(index, 1);

  if (
    wasDefault &&
    addresses.value.length > 0 &&
    !addresses.value.some((a) => a.macDinh)
  ) {
    addresses.value[0].macDinh = true;
  }
  notify.success("Đã xóa địa chỉ");
};

/* ================= ACTION ================= */
const doSubmit = async () => {
  if (submitting.value) return;

  // ❌ đang mở form địa chỉ thì cấm submit
  if (showAddressForm.value) {
    notify.error("Vui lòng lưu hoặc hủy form địa chỉ trước");
    return;
  }
  submitting.value = true;
  //➡️ Chỉ kiểm tra địa chỉ mặc định khi thực sự có địa chỉ.
  const defaultCount = addresses.value.filter((a) => a.macDinh).length;

  if (addresses.value.length > 0 && defaultCount !== 1) {
    notify.error("Phải có đúng 1 địa chỉ mặc định");
    return;
  }

  nprogress.start();

  try {
    // 1️⃣ chuẩn hóa payload
    const payload = {
      hoTen: form.hoTen.trim(),
      ngaySinh: form.ngaySinh,
      gioiTinh: form.gioiTinh,
      soDienThoai: form.soDienThoai.trim(),
      email: form.email.trim().toLowerCase(),
      diaChiList: addresses.value.map((a) => ({
        tinhThanh: a.tinhThanh,
        xaPhuong: a.xaPhuong,
        diaChiChiTiet: a.diaChiChiTiet,
        macDinh: !!a.macDinh,
      })),
    };

    // 2️⃣ FormData
    const formData = new FormData();
    formData.append(
      "data",
      new Blob([JSON.stringify(payload)], {
        type: "application/json",
      }),
    );

    if (form.anhDaiDien) {
      formData.append("file", form.anhDaiDien);
    }

    // 3️⃣ gọi API (⚠️ thêm headers)
    await axiosClient.post(
      "http://localhost:8080/api/admin/khach-hang/save",
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      },
    );

    notify.success("Thêm khách hàng thành công");

    setTimeout(() => {
      isSubmittingOrCanceling.value = true;
      router.back();
    }, 800);
  } catch (e) {
    const res = e.response?.data;
    const msg = String(res?.message || e.message || "Thêm khách hàng thất bại");
    const lowerMsg = msg.toLowerCase();

    if (res?.errors) {
      Object.keys(res.errors).forEach((field) => {
        errors[field] = res.errors[field];
      });
      return;
    }

    if (lowerMsg.includes("email")) {
      errors.email = msg;
      return;
    }

    if (
      lowerMsg.includes("sđt") ||
      lowerMsg.includes("số điện thoại") ||
      lowerMsg.includes("so dien thoai")
    ) {
      errors.soDienThoai = msg;
      return;
    }

    notify.error(msg);
  } finally {
    submitting.value = false;
    nprogress.done();
  }
};
// k bắt buộc thêm địa chỉ
const submit = async () => {
  if (!validateForm()) return;

  const ok = await notify.confirm("Bạn muốn thêm khách hàng này?");
  if (!ok) return;

  doSubmit();
};

const cancel = () => router.back();
</script>

<template>
  <!-- ===== CONTENT WRAPPER ===== -->
  <div class="content-wrapper">
    <!-- ===== HEADER / TITLE ===== -->
    <div class="box">
      <div class="box-header-flex">
        <div class="box-header">
          <h2 class="main-title">
            <span class="icon-wrapper">
              <i
                class="bi bi-person-plus"
                style="color: rgb(225, 29, 72); font-size: 24px"
              ></i>
            </span>
            Thêm Khách Hàng
          </h2>
          <p>
            Thiết lập thông tin tài khoản và địa chỉ giao hàng của khách hàng
          </p>
        </div>

        <button class="icon-btn-back" @click="cancel">
          <img :src="iconBack" />
        </button>
      </div>
    </div>

    <!-- ===== SCROLLABLE PAGE CONTENT ===== -->
    <div class="page-content">
      <!-- ================= FRAME 1 ================= -->
      <div class="card mb-4 shadow-sm">
        <div class="card-body">
          <h5 class="mb-4">Thông tin chung</h5>

          <div class="row">
            <!-- Avatar -->
            <div class="col-md-3 text-center">
              <div class="avatar-box mb-2">
                <img
                  v-if="imagePreview"
                  :src="imagePreview"
                  class="avatar-img"
                />
                <i v-else class="bi bi-person-fill avatar-icon"></i>
              </div>
              <label class="btn btn-outline-pink btn-sm">
                Chọn ảnh
                <input
                  type="file"
                  hidden
                  accept="image/png,image/jpeg"
                  @change="onSelectImage"
                />
              </label>
              <div class="text-muted small mt-1">Max 5MB</div>
            </div>

            <!-- Form -->
            <div class="col-md-9">
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label">
                    Họ và tên <span class="text-required">*</span>
                  </label>
                  <input
                    v-model.trim="form.hoTen"
                    class="form-control"
                    :class="{ 'input-invalid': errors.hoTen }"
                    placeholder="Nhập họ tên đầy đủ"
                    @input="errors.hoTen = ''"
                  />
                  <transition name="shake">
                    <p class="error-hint" v-if="errors.hoTen">
                      <i class="bi bi-exclamation-circle"></i>
                      {{ errors.hoTen }}
                    </p>
                  </transition>
                </div>

                <div class="col-md-6">
                  <label class="form-label">
                    Số điện thoại <span class="text-required">*</span>
                  </label>
                  <input
                    v-model.trim="form.soDienThoai"
                    class="form-control"
                    :class="{ 'input-invalid': errors.soDienThoai }"
                    placeholder="VD: 0987..."
                    @input="errors.soDienThoai = ''"
                  />
                  <transition name="shake">
                    <p class="error-hint" v-if="errors.soDienThoai">
                      <i class="bi bi-exclamation-circle"></i>
                      {{ errors.soDienThoai }}
                    </p>
                  </transition>
                </div>

                <div class="col-md-6">
                  <label class="form-label">
                    Email <span class="text-required">*</span>
                  </label>

                  <input
                    v-model.trim="form.email"
                    class="form-control"
                    :class="{ 'input-invalid': errors.email }"
                    placeholder="example@email.com"
                    @input="errors.email = ''"
                  />
                  <transition name="shake">
                    <p class="error-hint" v-if="errors.email">
                      <i class="bi bi-exclamation-circle"></i>
                      {{ errors.email }}
                    </p>
                  </transition>
                </div>

                <div class="col-md-6">
                  <label class="form-label">
                    Ngày sinh <span class="text-required">*</span>
                  </label>
                  <input
                    type="date"
                    v-model="form.ngaySinh"
                    class="form-control"
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

                <div class="col-md-12">
                  <label class="form-label">
                    Giới tính <span class="text-required">*</span>
                  </label>
                  <div class="d-flex gap-4 mt-1">
                    <label class="form-check">
                      <input
                        class="form-check-input"
                        type="radio"
                        :value="true"
                        v-model="form.gioiTinh"
                      />
                      Nam
                    </label>

                    <label class="form-check">
                      <input
                        class="form-check-input"
                        type="radio"
                        :value="false"
                        v-model="form.gioiTinh"
                      />
                      Nữ
                    </label>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ================= FRAME 2 ================= -->
      <div class="card shadow-sm">
        <div class="card-body">
          <h5 class="mb-3">Danh sách địa chỉ</h5>
          <div
            v-for="(addr, index) in addresses"
            :key="index"
            class="border border-pink rounded p-3 mb-3"
          >
            <div class="d-flex align-items-center mb-2">
              <strong class="text-pink me-auto">
                <i class="bi bi-geo-alt-fill me-1"></i>
                Địa chỉ {{ index + 1 }}
              </strong>

              <span v-if="addr.macDinh" class="badge bg-pink me-2"
                >Mặc định</span
              >

              <button
                class="btn btn-sm btn-outline-secondary me-1"
                @click="editAddress(index)"
              >
                <i class="bi bi-pencil"></i>
              </button>
              <button
                class="btn btn-sm btn-outline-danger"
                @click="removeAddress(index)"
              >
                <i class="bi bi-trash"></i>
              </button>
            </div>

            <div class="text-muted small">
              {{ addr.diaChiChiTiet }}, {{ addr.xaPhuong }},
              {{ addr.tinhThanh }}
            </div>

            <button
              v-if="!addr.macDinh"
              class="btn btn-link btn-sm p-0 text-pink"
              @click="setDefault(index)"
            >
              Đặt làm mặc định
            </button>
          </div>

          <div
            v-if="showAddressForm"
            class="border border-pink rounded p-3 mb-4 address-box position-relative"
          >
            <!-- 1️⃣ TIÊU ĐỀ (DÁN Ở ĐÂY) -->
            <div class="d-flex align-items-center mb-3">
              <strong class="text-pink me-auto">
                <i class="bi bi-geo-alt-fill me-1"></i>
                {{ editingIndex !== null ? "Sửa địa chỉ" : "Thêm địa chỉ mới" }}
              </strong>

              <!-- badge mặc định -->
              <span v-if="addressForm.macDinh" class="badge bg-pink me-2">
                Mặc định
              </span>

              <!-- nút X -->
              <button
                class="btn btn-sm btn-light"
                title="Hủy"
                @click="closeAddressForm"
              >
                <i class="bi bi-x-lg"></i>
              </button>
            </div>

            <div class="row g-3">
              <div
                class="col-md-6 position-relative autocomplete-box"
                ref="provinceBox"
              >
                <label class="form-label">
                  Tỉnh / Thành phố <span class="text-required">*</span>
                </label>

                <input
                  v-model="provinceKeyword"
                  class="form-control"
                  :class="{ 'input-invalid': addressErrors.tinhThanh }"
                  placeholder="Nhập tỉnh / thành phố..."
                  @focus="showProvinceDropdown = true"
                  @input="
                    showProvinceDropdown = true;
                    addressErrors.tinhThanh = '';
                  "
                />
                <transition name="shake">
                  <p class="error-hint" v-if="addressErrors.tinhThanh">
                    <i class="bi bi-exclamation-circle"></i>
                    {{ addressErrors.tinhThanh }}
                  </p>
                </transition>
                <ul v-if="showProvinceDropdown" class="autocomplete-dropdown">
                  <li
                    v-for="p in filteredProvinces"
                    :key="p.code"
                    class="autocomplete-item"
                    @mousedown.prevent
                    @click="selectProvince(p)"
                  >
                    {{ p.name }}
                  </li>

                  <li
                    v-if="filteredProvinces.length === 0"
                    class="list-group-item text-muted"
                  >
                    Không tìm thấy
                  </li>
                </ul>
              </div>

              <div
                class="col-md-6 position-relative autocomplete-box"
                ref="wardBox"
              >
                <label class="form-label">
                  Phường / Xã <span class="text-required">*</span>
                </label>

                <input
                  v-model="wardKeyword"
                  class="form-control"
                  :class="{ 'input-invalid': addressErrors.xaPhuong }"
                  placeholder="Nhập phường / xã..."
                  :disabled="!selectedProvinceCode"
                  @focus="selectedProvinceCode && (showWardDropdown = true)"
                  @input="
                    selectedProvinceCode && (showWardDropdown = true);
                    addressErrors.xaPhuong = '';
                  "
                />
                <transition name="shake">
                  <p class="error-hint" v-if="addressErrors.xaPhuong">
                    <i class="bi bi-exclamation-circle"></i>
                    {{ addressErrors.xaPhuong }}
                  </p>
                </transition>
                <ul
                  v-if="showWardDropdown && selectedProvinceCode"
                  class="autocomplete-dropdown"
                >
                  <li
                    v-for="w in filteredWards"
                    :key="w.code"
                    class="autocomplete-item"
                    @mousedown.prevent
                    @click="selectWard(w)"
                  >
                    {{ w.name }}
                  </li>

                  <li v-if="filteredWards.length === 0" class="text-muted">
                    Không tìm thấy
                  </li>
                </ul>
              </div>

              <div class="col-12">
                <label class="form-label">
                  Địa chỉ chi tiết <span class="text-required">*</span>
                </label>
                <input
                  v-model="addressForm.diaChiChiTiet"
                  class="form-control"
                  :class="{ 'input-invalid': addressErrors.diaChiChiTiet }"
                  placeholder="Số nhà, đường..."
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
            <hr class="my-3" />
            <!-- switch dat dia chi macc dinh -->
            <div class="d-flex align-items-center gap-3">
              <label class="form-label mb-0 fw-semibold">
                Đặt làm mặc định:
              </label>

              <div class="form-check form-switch">
                <input
                  class="form-check-input"
                  type="checkbox"
                  v-model="addressForm.macDinh"
                />
              </div>

              <span class="text-muted small fst-italic">
                * Khi bật, các địa chỉ khác sẽ bỏ mặc định
              </span>
            </div>

            <!-- nut luu dia chi -->
            <div class="text-end mt-3">
              <button class="btn btn-pink btn-sm" @click="saveAddress">
                Xác nhận
              </button>
            </div>
          </div>

          <!-- mở form dia chi -->
          <div class="text-center mb-4">
            <button
              v-if="!showAddressForm"
              class="btn btn-outline-pink"
              @click="openAddressForm"
            >
              + Thêm địa chỉ mới
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
  <!-- ===== FIXED ACTION BAR (ONLY CONTENT) ===== -->
  <div class="fixed-action-bar">
    <div class="d-flex justify-content-end gap-2">
      <button type="button" class="btn-cancel" @click="cancel">Hủy bỏ</button>
      <button
        type="button"
        class="btn-primary px-4"
        :disabled="submitting"
        @click="submit"
      >
        <div class="btn-light-sweep"></div>
        <i class="bi bi-check2-all"></i> Xác nhận
      </button>
    </div>
  </div>
</template>

<style scoped>
/* ===== WRAPPER ===== */
.content-wrapper {
  position: relative;
  height: 100vh;
  overflow: hidden;
}

/* ===== SCROLL AREA ===== */
.page-content {
  height: calc(100vh - 140px);
  overflow-y: auto;
  overflow-x: hidden;
  padding-bottom: 160px;
}
/* Dấu bắt buộc */
.text-required {
  color: #dc3545; /* đỏ bootstrap */
  font-weight: 600;
  margin-left: 2px;
}

/* ===== AVATAR ===== */
.avatar-box {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: #fdecee;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: auto;
  overflow: hidden;
}
.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.avatar-icon {
  font-size: 64px;
  color: #d48a8a;
}

/* ===== COLORS ===== */
.text-pink {
  color: #c94c4c;
}
.bg-pink {
  background: #c94c4c;
}
.border-pink {
  border-color: #e4a1a1 !important;
}

.btn-pink {
  background: #c94c4c;
  color: #fff;
}
.btn-pink:hover {
  background: #b03f3f;
}

.btn-outline-pink {
  border: 1px solid #c94c4c;
  color: #c94c4c;
}

/* ===== BUTTON ĐỒNG BỘ ===== */
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
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.btn-primary:hover:not(:disabled) {
  background: #b92b2b;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(230, 90, 90, 0.3);
}
.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Hiệu ứng quét sáng (light-sweep) */
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
.btn-primary:hover:not(:disabled) .btn-light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
}

/* ===== ADDRESS ===== */
.address-box {
  background: #fff;
}

/* ===== FIXED FOOTER ===== */
.fixed-action-bar {
  position: sticky;
  bottom: 0;
  background: #fff;
  border-top: 1px solid #f1c1c1;
  padding: 12px 24px;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
  z-index: 5;
}

/* ===== SWITCH HỒNG ===== */
.form-switch .form-check-input {
  width: 42px;
  height: 22px;
  cursor: pointer;
}

/* khi bật */
.form-switch .form-check-input:checked {
  background-color: #c94c4c;
  border-color: #c94c4c;
}

/* nút tròn */
.form-switch .form-check-input:focus {
  box-shadow: 0 0 0 0.15rem rgba(201, 76, 76, 0.3);
}

/* khi disabled (nếu có) */
.form-switch .form-check-input:disabled {
  opacity: 0.6;
}
/* ===== AUTOCOMPLETE ===== */
.autocomplete-box {
  position: relative;
  z-index: 50;
}

.autocomplete-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 4px;
  padding: 4px 0;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.12);
  max-height: 260px;
  overflow-y: auto;
  z-index: 9999;
  border: 1px solid #f1c1c1;
}

/* item */
.autocomplete-item {
  padding: 8px 16px;
  cursor: pointer;
  transition: background 0.2s;
  font-size: 14px;
}

.autocomplete-item:hover {
  background: #fdecee;
}

/* không tìm thấy */
.autocomplete-dropdown .text-muted {
  padding: 10px 14px;
  font-size: 13px;
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

/* ===== INPUT / SELECT HIỆU ỨNG ===== */
.form-control,
.form-select {
  height: 42px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 14px;
  outline: none;
  transition: all 0.2s;
  width: 100%;
  font-family: inherit;
  color: #374151;
  box-shadow: none;
}
.form-control:hover,
.form-select:hover {
  border-color: #c8d0dc;
}
.form-control:focus,
.form-select:focus {
  border-color: #e11d48;
  box-shadow: 0 0 0 4px rgba(225, 29, 72, 0.12);
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

select.form-control,
select.form-select {
  cursor: pointer;
}
</style>
