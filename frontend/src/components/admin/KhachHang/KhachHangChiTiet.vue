<script setup>
import { reactive, ref, onMounted, computed } from "vue";
import { onUnmounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";
import { useRoute } from "vue-router";
import { useKhachHangValidation } from "@/composables/useKhachHangValidation";
import { useAddressValidation } from "@/composables/useAddressValidation";
import { useUtils } from "@/utils/helper";
import axiosClient from "@/services/axiosClient";
const { notify, nprogress } = useUtils();
import iconBack from "@/assets/icons/prev.png";
const route = useRoute();
const activeTab = ref("diachi");
const hoaDonList = ref([]);

const getTrangThaiText = (tt) => {
  switch (tt) {
    case 0:
      return "Vừa tạo";
    case 1:
      return "Chờ khách cọc";
    case 2:
      return "Đã cọc";
    case 3:
      return "Đã xác nhận";
    case 4:
      return "Chờ xác nhận";
    case 5:
      return "Chờ hoàn cọc";
    case 6:
      return "Đang phục vụ";
    case 7:
      return "Chờ thanh toán";
    case 8:
      return "Đã thanh toán";
    case 9:
      return "Hoàn thành";
    case 10:
      return "Treo nợ";
    case 11:
      return "Nợ xấu";
    case -1:
    default:
      return "Đã hủy";
  }
};

const getBadgeClass = (tt) => {
  switch (tt) {
    case 0:
      return "status-badge status-created";
    case 1:
      return "status-badge status-wait-deposit";
    case 2:
      return "status-badge status-deposited";
    case 3:
      return "status-badge status-confirmed";
    case 4:
      return "status-badge status-waiting";
    case 5:
      return "status-badge status-refund";
    case 6:
      return "status-badge status-serving";
    case 7:
      return "status-badge status-wait-payment";
    case 8:
      return "status-badge status-paid";
    case 9:
      return "status-badge status-completed";
    case 10:
      return "status-badge status-debt";
    case 11:
      return "status-badge status-bad-debt";
    case -1:
    default:
      return "status-badge status-cancelled";
  }
};
/* ================= ROUTER ================= */
const router = useRouter();

const khachHangId = route.params.id;
const loadKhachHang = async () => {
  try {
    const res = await axiosClient.get(
      `http://localhost:8080/api/admin/khach-hang/${khachHangId}`,
    );

    const data = res.data;

    // ===== FORM CHÍNH =====
    form.hoTen = data.tenKhachHang;
    form.ngaySinh = data.ngaySinh ? data.ngaySinh.slice(0, 10) : "";

    form.gioiTinh =
      data.gioiTinh === true ||
      data.gioiTinh === 1 ||
      String(data.gioiTinh).toLowerCase() === "true" ||
      String(data.gioiTinh) === "1";
    form.soDienThoai = data.soDienThoai;
    form.email = data.email;

    // ===== THÔNG TIN HẠNG THÀNH VIÊN =====
    form.tongChiTieu = data.tongChiTieu;
    form.diemTichLuy = data.diemTichLuy;
    form.hangThanhVien = data.hangThanhVien;

    // ===== ẢNH =====
    if (data.anhDaiDien) {
      imagePreview.value = `http://localhost:8080/uploads/${data.anhDaiDien}`;
    }

    // ===== ĐỊA CHỈ =====
    addresses.value = (data.diaChiList || [])
      .filter(Boolean)
      .map((d) => ({ ...d }));

    // ===== LỊCH SỬ HÓA ĐƠN =====
    hoaDonList.value = data.hoaDonList || [];

    // ===== SET TỈNH + LOAD PHƯỜNG CHO ĐỊA CHỈ MẶC ĐỊNH =====
    if (addresses.value.length > 0) {
      const addr = addresses.value.find((a) => a.macDinh) || addresses.value[0];

      const province = provinces.value.find((p) => p.name === addr.tinhThanh);
      if (province) {
        selectedProvinceCode.value = String(province.code);
        provinceKeyword.value = province.name;
        addressForm.tinhThanh = province.name;
        await onProvinceChange(true); // giữ phường
        wardKeyword.value = addr.xaPhuong;
        addressForm.xaPhuong = addr.xaPhuong;
      }
    }

    if (addresses.value.length > 0 && !addresses.value.some((a) => a.macDinh)) {
      addresses.value[0].macDinh = true;
    }
  } catch (e) {
    console.error(e);
    alert("Không tải được dữ liệu khách hàng");
    router.back();
  }
};

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
  showWardDropdown.value = false;
};

const onProvinceChange = async (keepWard = false) => {
  wards.value = [];

  if (!keepWard) {
    addressForm.xaPhuong = "";
    wardKeyword.value = "";
  }

  const province = provinces.value.find(
    (p) => String(p.code) === selectedProvinceCode.value,
  );
  addressForm.tinhThanh = province?.name || "";

  const url = `https://provinces.open-api.vn/api/v2/p/${selectedProvinceCode.value}?depth=3`;

  try {
    const res = await axiosClient.get(url);
    const allWards = [];

    if (Array.isArray(res.data.districts)) {
      res.data.districts.forEach((d) => {
        if (Array.isArray(d.wards)) {
          allWards.push(...d.wards);
        }
      });
    }

    wards.value = allWards;
  } catch (e) {
    console.error(e);
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

onMounted(async () => {
  // 1️⃣ load tỉnh TRƯỚC
  try {
    const res = await axiosClient.get("https://provinces.open-api.vn/api/v2/p");
    provinces.value = res.data;
  } catch (e) {
    console.error("Lỗi load tỉnh:", e);
  }

  // 2️⃣ rồi mới load khách hàng
  await loadKhachHang();

  // 3️⃣ click ngoài
  document.addEventListener("click", handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener("click", handleClickOutside);
});

/* ================= FORM ================= */
const form = reactive({
  hoTen: "",
  ngaySinh: "",
  gioiTinh: true,
  soDienThoai: "",
  email: "",
  anhDaiDien: null,
  tongChiTieu: 0,
  diemTichLuy: 0,
  hangThanhVien: "",
});

const validator = useKhachHangValidation(form);

const { errors, validateForm } = validator;

/* ================= IMAGE ================= */
const imagePreview = ref(null);

const onSelectImage = (e) => {
  const file = e.target.files[0];
  if (!file) return;

  if (!file.type.startsWith("image/")) {
    alert("File không phải ảnh");
    return;
  }

  if (file.size > 5 * 1024 * 1024) {
    alert("Ảnh tối đa 5MB");
    return;
  }

  form.anhDaiDien = file;
  if (imagePreview.value) {
    URL.revokeObjectURL(imagePreview.value);
  }
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
const addressMode = ref(null); // null | 'add' | 'edit'
const editingIndex = ref(null);

const addressForm = reactive({
  id: null,
  tinhThanh: "",
  xaPhuong: "",
  diaChiChiTiet: "",
  macDinh: false,
});
const { addressErrors, validateAddress } = useAddressValidation(addressForm);

const resetAddressForm = () => {
  Object.assign(addressForm, {
    id: null,
    tinhThanh: "",
    xaPhuong: "",
    diaChiChiTiet: "",
    macDinh: false,
  });

  addressMode.value = null;
  editingIndex.value = null;
};

const closeAddressForm = () => {
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
  addressMode.value = "add";
  provinceKeyword.value = "";
  wardKeyword.value = "";
  selectedProvinceCode.value = "";
  wards.value = [];
  showAddressForm.value = true;
};

/* ===== SAVE ADDRESS ===== */
const saveAddress = () => {
  if (!validateAddress()) return;

  const data = {
    id: addressForm.id,
    tinhThanh: addressForm.tinhThanh,
    xaPhuong: addressForm.xaPhuong,
    diaChiChiTiet: addressForm.diaChiChiTiet,
    macDinh: addressForm.macDinh,
  };

  // reset mặc định
  if (data.macDinh) {
    addresses.value.forEach((a) => (a.macDinh = false));
  }

  if (addressMode.value === "edit" && editingIndex.value > -1) {
    addresses.value.splice(editingIndex.value, 1, { ...data });
  } else {
    addresses.value.push({
      ...data,
      id: null,
    });
  }

  // đảm bảo có 1 địa chỉ mặc định
  if (addresses.value.length > 0 && !addresses.value.some((a) => a.macDinh)) {
    addresses.value[0].macDinh = true;
  }

  closeAddressForm();
};

/* ===== EDIT ===== */
const editAddress = async (addr, index) => {
  Object.assign(addressForm, { ...addr });

  editingIndex.value = index;
  addressMode.value = "edit";

  // tìm tỉnh
  const province = provinces.value.find((p) => p.name === addr.tinhThanh);

  if (province) {
    selectedProvinceCode.value = String(province.code);

    provinceKeyword.value = province.name;
    addressForm.tinhThanh = province.name;

    await onProvinceChange(true);
  }

  // set phường
  wardKeyword.value = addr.xaPhuong;
  addressForm.xaPhuong = addr.xaPhuong;

  showAddressForm.value = true;
  showWardDropdown.value = false;
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

  if (wasDefault && addresses.value.length > 0) {
    addresses.value[0].macDinh = true;
  }

  notify.success("Xóa địa chỉ thành công");
};

/* ================= ACTION ================= */
const submit = async () => {
  // ❌ validate form
  if (!validateForm()) {
    notify.error("Vui lòng kiểm tra lại thông tin khách hàng");
    return;
  }

  // ❌ chưa có địa chỉ
  if (addresses.value.length === 0) {
    notify.error("Vui lòng thêm ít nhất 1 địa chỉ");
    return;
  }

  // đảm bảo có địa chỉ mặc định
  if (!addresses.value.some((a) => a.macDinh)) {
    addresses.value[0].macDinh = true;
  }

  // 🔥 confirm trước khi cập nhật
  const ok = await notify.confirm("Bạn có chắc muốn cập nhật khách hàng?");
  if (!ok) return;

  const payload = {
    hoTen: form.hoTen.trim(),
    ngaySinh: form.ngaySinh,
    gioiTinh: form.gioiTinh,
    soDienThoai: form.soDienThoai.trim(),
    email: form.email.trim().toLowerCase(),
    diaChiList: addresses.value.map((a) => ({
      id: a.id,
      tinhThanh: a.tinhThanh,
      xaPhuong: a.xaPhuong,
      diaChiChiTiet: a.diaChiChiTiet,
      macDinh: a.macDinh,
    })),
  };

  const formData = new FormData();
  formData.append(
    "data",
    new Blob([JSON.stringify(payload)], { type: "application/json" }),
  );

  if (form.anhDaiDien) {
    formData.append("file", form.anhDaiDien);
  }

  try {
    nprogress.start();

    await axiosClient.post(
      `http://localhost:8080/api/admin/khach-hang/update/${khachHangId}`,
      formData,
    );

    notify.success("Cập nhật khách hàng thành công");
    router.back();
  } catch (e) {
    console.error(e);

    const res = e.response?.data;

    if (res?.errors) {
      Object.keys(res.errors).forEach((field) => {
        errors[field] = res.errors[field];
      });
      return;
    }

    notify.error(res?.message || "Cập nhật khách hàng thất bại");
  } finally {
    nprogress.done();
  }
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
            Chi Tiết Khách Hàng
          </h2>
          <p>Xem thông tin chi tiết và lịch sử đơn hàng của khách</p>
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
              <div class="mt-3 customer-info-simple">
                <div class="customer-tier">
                  Hạng: <span class="tier-name">{{ form.hangThanhVien || 'Thường' }}</span>
                </div>
                <div class="customer-stats">
                  <span>Tổng chi tiêu: <strong>{{ form.tongChiTieu?.toLocaleString('vi-VN') || 0 }} đ</strong></span>
                  <span class="mx-2 stat-divider">|</span>
                  <span>Điểm tích lũy: <strong>{{ form.diemTichLuy?.toLocaleString('vi-VN') || 0 }}</strong></span>
                </div>
              </div>
            </div>

            <!-- Form -->
            <div class="col-md-9">
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label">
                    Họ và tên <span class="text-required">*</span>
                  </label>
                  <input
                    v-model="form.hoTen"
                    class="form-control"
                    placeholder="Nhập họ tên đầy đủ"
                  />
                  <div v-if="errors.hoTen" class="text-danger small mt-1">
                    {{ errors.hoTen }}
                  </div>
                </div>

                <div class="col-md-6">
                  <label class="form-label">
                    Số điện thoại <span class="text-required">*</span>
                  </label>
                  <input
                    v-model="form.soDienThoai"
                    class="form-control"
                    placeholder="VD: 0987..."
                  />
                  <div v-if="errors.soDienThoai" class="text-danger small mt-1">
                    {{ errors.soDienThoai }}
                  </div>
                </div>

                <div class="col-md-6">
                  <label class="form-label">
                    Email <span class="text-required">*</span>
                  </label>

                  <input
                    v-model="form.email"
                    class="form-control"
                    placeholder="example@email.com"
                  />
                  <div v-if="errors.email" class="text-danger small mt-1">
                    {{ errors.email }}
                  </div>
                </div>

                <div class="col-md-6">
                  <label class="form-label">
                    Ngày sinh <span class="text-required">*</span>
                  </label>
                  <input
                    type="date"
                    v-model="form.ngaySinh"
                    class="form-control"
                  />
                  <div v-if="errors.ngaySinh" class="text-danger small mt-1">
                    {{ errors.ngaySinh }}
                  </div>
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
          <div class="custom-tabs mb-4">
            <button
              class="tab-btn"
              :class="{ active: activeTab === 'diachi' }"
              @click.prevent="activeTab = 'diachi'"
            >
              Địa chỉ
            </button>
            <button
              class="tab-btn"
              :class="{ active: activeTab === 'hoadon' }"
              @click.prevent="activeTab = 'hoadon'"
            >
              Lịch sử hóa đơn
            </button>
          </div>

          <!-- TAB ĐỊA CHỈ -->
          <div v-if="activeTab === 'diachi'">
            <div
              v-for="(addr, index) in addresses"
              :key="addr.id ? `addr-${addr.id}` : `addr-${index}`"
              class="border border-pink rounded p-3 mb-3"
            >
              <div class="d-flex align-items-center mb-2">
                <strong class="text-pink me-auto">
                  <i class="bi bi-geo-alt-fill me-1"></i>
                  Địa chỉ {{ index + 1 }}
                </strong>

                <span v-if="addr?.macDinh" class="badge bg-pink me-2"
                  >Mặc định</span
                >

                <button
                  class="btn btn-sm btn-outline-secondary me-1"
                  @click="editAddress(addr, index)"
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
                {{ addr?.diaChiChiTiet }}, {{ addr?.xaPhuong }},
                {{ addr?.tinhThanh }}
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
              <div class="d-flex align-items-center mb-3">
                <strong class="text-pink me-auto">
                  <i class="bi bi-geo-alt-fill me-1"></i>
                  {{
                    addressMode === "edit" ? "Sửa địa chỉ" : "Thêm địa chỉ mới"
                  }}
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
                    placeholder="Nhập tỉnh / thành phố..."
                    @focus="showProvinceDropdown = true"
                    @input="showProvinceDropdown = true"
                  />
                  <div
                    v-if="addressErrors.tinhThanh"
                    class="text-danger small mt-1"
                  >
                    {{ addressErrors.tinhThanh }}
                  </div>
                  <ul
                    v-if="showProvinceDropdown && provinces.length"
                    class="autocomplete-dropdown"
                  >
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
                    placeholder="Nhập phường / xã..."
                    :disabled="!selectedProvinceCode"
                    @focus="selectedProvinceCode && (showWardDropdown = true)"
                    @input="selectedProvinceCode && (showWardDropdown = true)"
                  />
                  <div
                    v-if="addressErrors.xaPhuong"
                    class="text-danger small mt-1"
                  >
                    {{ addressErrors.xaPhuong }}
                  </div>
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
                    placeholder="Số nhà, đường..."
                  />
                  <div
                    v-if="addressErrors.diaChiChiTiet"
                    class="text-danger small mt-1"
                  >
                    {{ addressErrors.diaChiChiTiet }}
                  </div>
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

                <span class="text-muted small">
                  (Địa chỉ khác sẽ bị thay thế)
                </span>
              </div>

              <!-- nut luu dia chi -->
              <div class="text-end mt-3">
                <button
                  type="button"
                  class="btn-primary"
                  style="padding: 8px 16px; font-size: 13px"
                  @click="saveAddress"
                >
                  <div class="btn-light-sweep"></div>
                  <i class="bi bi-check2-all"></i> Xác nhận
                </button>
              </div>
            </div>
          </div>
          <!-- END TAB ĐỊA CHỈ -->

          <!-- TAB HÓA ĐƠN -->
          <div v-if="activeTab === 'hoadon'">
            <div class="table-container">
              <table class="table-main">
                <thead>
                  <tr>
                    <th>STT</th>
                    <th>Mã hóa đơn</th>
                    <th>Loại hóa đơn</th>
                    <th>Tổng tiền</th>
                    <th>Thời gian thanh toán</th>
                    <th>Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(hd, idx) in hoaDonList" :key="idx">
                    <td>{{ idx + 1 }}</td>
                    <td class="text-start">{{ hd.maHoaDon }}</td>
                    <td>
                      <span
                        class="booking-type"
                        :class="
                          hd.loaiHoaDon === 1 ? 'type-online' : 'type-offline'
                        "
                      >
                        {{ hd.loaiHoaDon === 1 ? "Trực tuyến" : "Tại quầy" }}
                      </span>
                    </td>
                    <td class="text-danger fw-bold">
                      {{ hd.tongTien?.toLocaleString("vi-VN") }} đ
                    </td>
                    <td>
                      {{
                        hd.thoiGianThanhToan
                          ? new Date(hd.thoiGianThanhToan).toLocaleString(
                              "vi-VN",
                            )
                          : "—"
                      }}
                    </td>
                    <td>
                      <span :class="getBadgeClass(hd.trangThai)">
                        {{ getTrangThaiText(hd.trangThai) }}
                      </span>
                    </td>
                  </tr>
                  <tr v-if="hoaDonList.length === 0">
                    <td colspan="6" class="text-muted py-3">
                      Không có lịch sử hóa đơn
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <!-- ===== FIXED ACTION BAR (ONLY CONTENT) ===== -->
  <div class="fixed-action-bar">
    <div class="d-flex justify-content-end gap-2">
      <button type="button" class="btn-cancel" @click="cancel">Hủy bỏ</button>
      <button type="button" class="btn-primary px-4" @click="submit">
        <div class="btn-light-sweep"></div>
        <i class="bi bi-check2-all"></i> Cập nhật
      </button>
    </div>
  </div>
</template>

<style scoped>
/* ===== CUSTOMER INFO DESIGN (SIMPLE & MODERN) ===== */
.customer-info-simple {
  margin-top: 15px;
  padding: 8px 0;
  text-align: center;
}

.customer-tier {
  font-size: 18px;
  font-weight: 500;
  color: #475569;
  margin-bottom: 6px;
}

.customer-tier .tier-name {
  color: #e11d48;
  font-weight: 700;
  margin-left: 4px;
}

.customer-stats {
  font-size: 11px;
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 4px;
}

.customer-stats strong {
  color: #0f172a;
  font-weight: 600;
}

.customer-stats .stat-divider {
  color: #cbd5e1;
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
/* ===== WRAPPER ===== */
.content-wrapper {
  position: relative;
  height: 100vh;
  overflow: hidden;
}

/* Tabs */
.custom-tabs {
  display: flex;
  gap: 15px;
  border-bottom: 2px solid #eee;
}
.tab-btn {
  background: none;
  border: none;
  padding: 10px 15px;
  font-weight: 600;
  color: #666;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: all 0.2s;
}
.tab-btn:hover {
  color: #d73737;
}
.tab-btn.active {
  color: #d73737;
  border-bottom-color: #d73737;
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
  transition: all 0.2s ease;
}
.btn-pink:hover {
  background: #b03f3f;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(201, 76, 76, 0.3);
}

.btn-outline-pink {
  border: 1px solid #c94c4c;
  color: #c94c4c;
  transition: all 0.2s ease;
}
.btn-outline-pink:hover {
  background: #c94c4c;
  color: #fff;
  transform: translateY(-2px);
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
.autocomplete-box input {
  border-radius: 8px;
  height: 42px;
  font-size: 14px;
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
  z-index: 20;
  border: 1px solid #f1c1c1;
}

/* item */
.autocomplete-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.15s ease;
}

/* chấm tròn bên trái */
.autocomplete-item::before {
  content: "";
  width: 6px;
  height: 6px;
  background: #222;
  border-radius: 50%;
  flex-shrink: 0;
}

/* hover */
.autocomplete-item:hover {
  background: #fdecee;
}

/* không tìm thấy */
.autocomplete-dropdown .text-muted {
  padding: 10px 14px;
  font-size: 13px;
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
select.form-control,
select.form-select {
  cursor: pointer;
}
</style>

<style scoped>
/* ===== BẢNG HÓA ĐƠN ===== */
.table-container {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
}
.table-main {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  background: white;
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
  color: #333;
  font-weight: 700;
  font-size: 15px;
}
.table-main td {
  border-bottom: 1px solid #f0f0f0;
  font-size: 13px;
}

/* Cố định cột */
.table-main th:nth-child(1),
.table-main td:nth-child(1) {
  width: 50px;
}
.table-main th:nth-child(2),
.table-main td:nth-child(2) {
  width: 140px;
  text-align: left;
}
.table-main th:nth-child(3),
.table-main td:nth-child(3) {
  width: 120px;
}
.table-main th:nth-child(4),
.table-main td:nth-child(4) {
  width: 140px;
}
.table-main th:nth-child(5),
.table-main td:nth-child(5) {
  width: 180px;
}
.table-main th:nth-child(6),
.table-main td:nth-child(6) {
  width: 150px;
}

/* Booking Type (Loại hóa đơn) */
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
.type-online {
  color: #0d6efd;
  border: 1px solid rgba(13, 110, 253, 0.25);
  background: rgba(13, 110, 253, 0.08);
}
.type-offline {
  color: #6c757d;
  border: 1px solid rgba(108, 117, 125, 0.25);
  background: rgba(108, 117, 125, 0.08);
}

/* Status Badge */
.status-badge {
  display: inline-block;
  text-align: center;
  width: 125px;
  padding: 6px 0;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
  box-sizing: border-box;
}
.status-created {
  background: rgba(107, 114, 128, 0.12);
  color: #6b7280;
}
.status-wait-deposit {
  background: rgba(245, 158, 11, 0.12);
  color: #f59e0b;
}
.status-deposited {
  background: rgba(34, 197, 94, 0.12);
  color: #22c55e;
}
.status-confirmed {
  background: rgba(59, 130, 246, 0.12);
  color: #3b82f6;
}
.status-waiting {
  background: rgba(251, 146, 60, 0.12);
  color: #fb923c;
}
.status-refund {
  background: rgba(14, 165, 233, 0.12);
  color: #0ea5e9;
}
.status-serving {
  background: rgba(139, 92, 246, 0.12);
  color: #8b5cf6;
}
.status-wait-payment {
  background: rgba(234, 88, 12, 0.12);
  color: #ea580c;
}
.status-paid {
  background: rgba(16, 185, 129, 0.12);
  color: #10b981;
}
.status-completed {
  background: rgba(5, 150, 105, 0.12);
  color: #059669;
}
.status-debt {
  background: rgba(217, 119, 6, 0.12);
  color: #d97706;
}
.status-bad-debt {
  background: rgba(185, 28, 28, 0.12);
  color: #b91c1c;
}
.status-cancelled {
  background: rgba(239, 68, 68, 0.12);
  color: #ef4444;
}
</style>
