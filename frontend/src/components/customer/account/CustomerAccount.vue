<script setup>
import { getThongTin } from "@/api/customer/info/thongTinCaNhanApi";
import { lichSuDonHang } from "@/api/customer/info/thongTinCaNhanApi";
import { updateThongTin } from "@/api/customer/info/thongTinCaNhanApi";
import { timKiemDonHang } from "@/api/customer/info/thongTinCaNhanApi";
import { doiMatKhau } from "@/api/customer/info/thongTinCaNhanApi";
import { ref, onMounted, watch } from "vue";
import { useUtils } from "@/utils/helper";
import { computed } from "vue";
import { useRouter } from "vue-router";
// --- LOGIC GỐC VÀ DỮ LIỆU BỔ SUNG ---
const router = useRouter();
const activeTab = ref("info");
const selectedOrder = ref(null);
const { notify, nprogress } = useUtils();
/////////////////// TÌM KIẾM ////////////////////
const keyword = ref("");
const maHoaDon = ref("");
const isSearching = computed(() => maHoaDon.value?.trim()?.length > 0);
const timKiemHoaDon = async () => {
  try {
    currentPage.value = 1;

    const params = {
      maHoaDon: maHoaDon.value?.trim() || null,
    };

    const res = await timKiemDonHang(params);

    const data = res.data?.data ?? res.data ?? [];

    danhSachDonHang.value = Array.isArray(data) ? data : [];
  } catch (error) {
    console.log(error);
    danhSachDonHang.value = [];
  }
};
/////////////////// PHÂN TRANG /////////////////
const currentPage = ref(1);
const pageSize = 3;

////////////////// LỊCH SỬ CHI TIẾT ///////////////////
const goChiTiet = (order) => {
  router.push(`/lich-su/detail/${order.idHoaDon}`);
};

/////////////////// THÔNG TIN CÁ NHÂN /////////////////

const customer = ref({});
const customerForm = ref({});

const loadThongTin = async () => {
  try {
    const res = await getThongTin();
    console.log(res.data);
    ``;
    customer.value = res.data;
    customerForm.value = { ...res.data };
  } catch (error) {
    console.error("Lỗi load thông tin", error);
  }
};

const saveThongTin = async () => {
  if (!(await notify.confirm("Bạn có muốn cập nhật thông tin?"))) return;
  nprogress.start();
  try {
    const payload = {
      tenKhachHang: customer.value.tenKhachHang,
      soDienThoai: customer.value.soDienThoai,
      gioiTinh: customer.value.gioiTinh,
      ngaySinh: customer.value.ngaySinh,

      diaChi: {
        tinhThanh: customer.value.defaultAddress?.tinhThanh || "",

        xaPhuong: customer.value.defaultAddress?.xaPhuong || "",

        diaChiChiTiet: customer.value.defaultAddress?.diaChiChiTiet || "",
      },
    };

    const res = await updateThongTin(payload);

    customer.value = res.data;

    notify.success("Cập nhật thành công");
  } catch (error) {
    console.error(error);
    notify.error("Cập nhật thất bại");
  } finally {
    nprogress.done();
  }
};
///////////////////////////////// ĐỊA CHỈ //////////////////
const showAddressModal = ref(false);

const provinces = ref([]);
const wards = ref([]);

const selectedProvinceCode = ref(null);

const loadProvinces = async () => {
  try {
    const res = await fetch("https://provinces.open-api.vn/api/v2/p/");
    provinces.value = await res.json();
  } catch (e) {
    console.error("Lỗi load tỉnh", e);
  }
};

const onChangeProvince = async () => {
  if (!selectedProvinceCode.value) return;

  try {
    const res = await fetch(
      `https://provinces.open-api.vn/api/v2/p/${selectedProvinceCode.value}?depth=2`,
    );
    const data = await res.json();

    wards.value = data.wards || [];

    // reset xã khi đổi tỉnh
    customerForm.value.defaultAddress.xaPhuong = "";
  } catch (e) {
    console.error("Lỗi load xã", e);
  }
};

const onSelectProvince = () => {
  const p = provinces.value.find((x) => x.code === selectedProvinceCode.value);

  customerForm.value.defaultAddress.tinhThanh = p?.name || "";
};

const onSelectWard = (wardName) => {
  customerForm.value.defaultAddress.xaPhuong = wardName;
};

const openAddressModal = () => {
  showAddressModal.value = true;
};

const closeAddressModal = () => {
  showAddressModal.value = false;
};

const formatDiaChi = (dc) => {
  if (!dc) return "";
  return [dc.diaChiChiTiet, dc.xaPhuong, dc.tinhThanh]
    .filter(Boolean)
    .join(", ");
};
///////////////////////////// LỊCH SỬ ĐƠN HÀNG ///////////////////
const selectedStatus = ref("all");
const danhSachDonHang = ref([]);

const loadDonHang = async () => {
  try {
    const res = await lichSuDonHang();
    danhSachDonHang.value = res.data;
  } catch (error) {
    console.log("Lỗi load đơn hàng", error);
  }
};

const statuses = [
  { label: "Tất cả", value: "all" },
  { label: "Chờ xác nhận", value: "processing" },
  { label: "Đang phục vụ", value: "serving" },
  { label: "Hoàn thành", value: "done" },
  { label: "Đã hủy", value: "cancel" },
];

const matchStatus = (o) => {
  switch (selectedStatus.value) {
    case "processing":
      return [0, 1, 2, 3, 4].includes(o.trangThai);

    case "serving":
      return [6, 7].includes(o.trangThai);

    case "done":
      return [8, 9].includes(o.trangThai);

    case "cancel":
      return [-1, 5].includes(o.trangThai);

    default:
      return true;
  }
};

const filteredOrders = computed(() => {
  return danhSachDonHang.value.filter((o) => {
    const statusOk = matchStatus(o);

    const matchKeyword =
      !keyword.value ||
      o.maHoaDon?.toLowerCase().includes(keyword.value.toLowerCase());

    return statusOk && matchKeyword;
  });
});

const totalPages = computed(() =>
  Math.ceil(filteredOrders.value.length / pageSize),
);
const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return filteredOrders.value.slice(start, start + pageSize);
});

watch([selectedStatus, keyword], () => {
  currentPage.value = 1;
});

const cancelOrder = (id) => {
  if (confirm(`bạn có chắc muốn hủy đơn hàng ${id}?`)) {
    orders.value = orders.value.filter((o) => o.id !== id);
    selectedOrder.value = null;
  }
};

///////////////////////////////////////

const convertTrangThai = (stt) => {
  switch (stt) {
    case 0:
      return "Vừa tạo";
    case 1:
      return "Chờ thanh toán cọc";
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
      return "Đã hủy";
    default:
      return "Không xác định";
  }
};

const formatCurrency = (money) => {
  if (!money && money !== 0) return "0 ₫";

  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(money);
};

const formatDateTime = (dateStr) => {
  if (!dateStr) return "";

  const date = new Date(dateStr);

  const datePart = new Intl.DateTimeFormat("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  }).format(date);

  const timePart = new Intl.DateTimeFormat("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);

  return `${datePart} ${timePart}`;
};
///////////////////////////// ĐỔI MẬT KHẨU /////////////////

const showPasswordModal = ref(false);
const isSubmittingPassword = ref(false);

const passwordForm = ref({
  matKhauCu: "",
  matKhauMoi: "",
  xacNhanMatKhauMoi: "",
});

const passwordErrors = ref({});

const showOldPass = ref(false);
const showNewPass = ref(false);
const showConfirmPass = ref(false);

const openPasswordModal = () => {
  passwordForm.value = {
    matKhauCu: "",
    matKhauMoi: "",
    xacNhanMatKhauMoi: "",
  };
  passwordErrors.value = {};
  showPasswordModal.value = true;
};

const closePasswordModal = () => {
  showPasswordModal.value = false;
};

const validatePasswordForm = () => {
  const errors = {};

  if (!passwordForm.value.matKhauCu?.trim()) {
    errors.matKhauCu = "Mật khẩu cũ không được để trống";
  }

  if (!passwordForm.value.matKhauMoi?.trim()) {
    errors.matKhauMoi = "Mật khẩu mới không được để trống";
  } else if (passwordForm.value.matKhauMoi.length < 6) {
    errors.matKhauMoi = "Mật khẩu mới phải từ 6 ký tự trở lên";
  }

  if (!passwordForm.value.xacNhanMatKhauMoi?.trim()) {
    errors.xacNhanMatKhauMoi = "Vui lòng xác nhận mật khẩu mới";
  } else if (
    passwordForm.value.matKhauMoi !== passwordForm.value.xacNhanMatKhauMoi
  ) {
    errors.xacNhanMatKhauMoi = "Mật khẩu xác nhận không khớp";
  }

  passwordErrors.value = errors;
  return Object.keys(errors).length === 0;
};

const submitDoiMatKhau = async () => {
  if (!validatePasswordForm()) return;

  if (!(await notify.confirm("Bạn có muốn đổi mật khẩu?"))) return;

  isSubmittingPassword.value = true;
  nprogress.start();

  try {
    await doiMatKhau({
      email: customer.value.email,
      matKhauCu: passwordForm.value.matKhauCu,
      matKhauMoi: passwordForm.value.matKhauMoi,
      xacNhanMatKhauMoi: passwordForm.value.xacNhanMatKhauMoi,
    });

    notify.success("Đổi mật khẩu thành công");
    closePasswordModal();
  } catch (error) {
    const msg =
      error?.response?.data?.message ||
      error?.response?.data?.error ||
      "Đổi mật khẩu thất bại, vui lòng thử lại";
    notify.error(msg);
  } finally {
    isSubmittingPassword.value = false;
    nprogress.done();
  }
};
const switchTab = (tab) => {
  activeTab.value = tab;
  selectedOrder.value = null;
};

onMounted(() => {
  loadDonHang();
  loadProvinces();
  loadThongTin();
});
</script>

<template>
  <div class="profile-page-wrapper">
    <div class="container py-5">
      <div class="main-royal-card reveal-up">
        <aside class="sidebar-royal">
          <div class="user-brand-section">
            <div class="avatar-wrapper">
              <div class="avatar-glow"></div>
              <img
                src="https://api.dicebear.com/7.x/avataaars/svg?seed=Felix"
                alt="avatar"
                class="user-avatar-luxury"
              />
              <button class="mini-upload-btn-style">
                <i class="bi bi-camera-fill"></i>
              </button>
            </div>
            <div class="user-meta">
              <h3 class="user-name-gold">{{ customerForm.tenKhachHang }}</h3>
              <div class="membership-tag">
                <i class="bi bi-crown-fill me-1"></i>
                THÀNH VIÊN GOLD
              </div>
            </div>
          </div>
          <nav class="nav-royal-menu">
            <button
              :class="['royal-menu-item', { active: activeTab === 'info' }]"
              @click="switchTab('info')"
            >
              <i class="bi bi-person-circle"></i>
              <span>Hồ sơ cá nhân</span>
              <div class="active-indicator"></div>
            </button>

            <button
              :class="['royal-menu-item', { active: activeTab === 'orders' }]"
              @click="switchTab('orders')"
            >
              <i class="bi bi-moped-fill"></i>
              <span>Lịch sử đơn hàng</span>
              <div class="active-indicator"></div>
            </button>

            <div class="menu-separator"></div>

            <button class="royal-menu-item security" @click="openPasswordModal">
              <i class="bi bi-shield-lock-fill"></i>
              <span>Đổi mật khẩu</span>
            </button>
          </nav>
        </aside>

        <main class="viewport-royal">
          <transition name="fade-slide" mode="out-in">
            <div v-if="activeTab === 'info'" key="info" class="content-animate">
              <header class="royal-header">
                <h2 class="title-main">THÔNG TIN TÀI KHOẢN</h2>
                <div class="title-line"></div>
                <p class="subtitle-grey">
                  Quản lý và bảo mật thông tin cá nhân của mình
                </p>
              </header>

              <div class="grid-form-luxury">
                <div class="input-group-royal">
                  <label class="black-label-bold">Họ và tên</label>
                  <div class="input-field-wrapper">
                    <input
                      v-model="customer.tenKhachHang"
                      type="text"
                      placeholder="Nhập họ tên..."
                    />
                    <i class="bi bi-pencil-square"></i>
                  </div>
                </div>

                <div class="input-group-royal">
                  <label class="black-label-bold">Giới tính</label>
                  <div class="gender-grid-royal">
                    <label class="gender-card">
                      <input type="radio" name="gender" value="male" checked />
                      <div class="gender-box">NAM</div>
                    </label>
                    <label class="gender-card">
                      <input type="radio" name="gender" value="female" />
                      <div class="gender-box">NỮ</div>
                    </label>
                  </div>
                </div>

                <div class="input-group-royal">
                  <label class="black-label-bold">Số điện thoại</label>
                  <div class="input-field-wrapper">
                    <input type="tel" v-model="customer.soDienThoai" />
                    <i class="bi bi-phone"></i>
                  </div>
                </div>

                <div class="input-group-royal">
                  <label class="black-label-bold">Địa chỉ Email</label>
                  <div class="input-field-wrapper readonly">
                    <input type="email" v-model="customer.email" readonly />
                    <i class="bi bi-envelope-check"></i>
                  </div>
                </div>

                <div class="input-group-royal">
                  <label class="black-label-bold">Ngày sinh</label>
                  <div class="input-field-wrapper">
                    <input type="date" v-model="customer.ngaySinh" />
                  </div>
                </div>

                <div class="input-group-royal full-span">
                  <label class="black-label-bold">Địa chỉ</label>

                  <div
                    class="input-field-wrapper readonly address-edit-wrapper"
                  >
                    <input
                      type="text"
                      readonly
                      :value="formatDiaChi(customerForm.defaultAddress)"
                    />

                    <!-- nút edit -->
                    <button class="btn-edit-address" @click="openAddressModal">
                      <span>Chỉnh sửa</span>
                    </button>
                  </div>
                </div>
              </div>

              <div class="footer-actions">
                <a
                  href="#"
                  class="btn-minimal-action"
                  @click.prevent="saveThongTin"
                >
                  <i class="fas fa-save"></i>
                  <span class="text-label">XÁC NHẬN</span>
                  <span class="underline-animation"></span>
                </a>
              </div>
            </div>
            <div
              v-else-if="activeTab === 'orders'"
              key="orders"
              class="order-page"
            >
              <header class="order-header">
                <h2 class="title-main">LỊCH SỬ ĐẶT BÀN</h2>
                <div class="title-line"></div>
                <p class="subtitle-grey">
                  Quản lý lịch sử đặt bàn và theo dõi đơn hàng
                </p>
              </header>
              <div class="order-tabs">
                <div
                  v-for="st in statuses"
                  :key="st.value"
                  :class="['tab-item', { active: selectedStatus === st.value }]"
                  @click="selectedStatus = st.value"
                >
                  {{ st.label }}
                </div>
              </div>
              <!-- SEARCH -->
              <div class="search-box-wrapper">
                <i class="bi bi-search search-icon"></i>
                <input
                  v-model="keyword"
                  type="text"
                  placeholder="Tìm theo mã đơn hàng..."
                  class="form-control search-input"
                  @input="filteredOrders"
                />
              </div>

              <!-- LIST -->
              <div v-if="paginatedOrders.length" class="order-list">
                <div
                  v-for="order in paginatedOrders"
                  :key="order.id"
                  class="order-card"
                  @click="goChiTiet(order)"
                >
                  <!-- LEFT -->
                  <div class="order-info">
                    <div class="order-title">
                      Mã đơn hàng : {{ order.maHoaDon }}
                    </div>
                    <div class="order-date">
                      Thời gian đặt : {{ formatDateTime(order.thoiGianDat) }}
                    </div>

                    <div class="order-extra">
                      <span class="extra-item">
                        <i class="ri-user-3-line"></i>
                        {{ order.soNguoiToiDa }} người
                      </span>

                      <span
                        class="extra-item"
                        :class="{ 'no-table': !order.table }"
                      >
                        <i class="ri-restaurant-2-line"></i>
                        B{{ order.tenBanAn ? order.tenBanAn : "Chưa xếp bàn" }}
                        -
                        {{ order.tenKhuVuc }}
                      </span>
                    </div>
                  </div>

                  <!-- RIGHT -->
                  <div class="order-meta">
                    <div class="order-total">
                      Tổng tiền : {{ formatCurrency(order.tongTien) }}
                    </div>

                    <div class="order-deposit">
                      <i class="ri-wallet-3-line"></i>
                      Tiền cọc : {{ formatCurrency(order.tienCoc) }}
                    </div>
                    <span
                      :class="[
                        'order-status',
                        order.status === 'đang giao' ? 'shipping' : 'done',
                      ]"
                    >
                      {{ convertTrangThai(order.trangThai) }}
                    </span>
                  </div>
                </div>
              </div>
              <div v-else class="empty-state">
                <i class="ri-file-list-3-line"></i>
                Không có hóa đơn nào
              </div>
              <div class="pagination" v-if="totalPages > 1">
                <button
                  :disabled="currentPage === 1"
                  @click="currentPage--"
                  class="page-btn"
                >
                  ←
                </button>

                <span class="page-info">
                  Trang {{ currentPage }} / {{ totalPages }}
                </span>

                <button
                  :disabled="currentPage === totalPages"
                  @click="currentPage++"
                  class="page-btn"
                >
                  →
                </button>
              </div>
            </div>
          </transition>
        </main>
      </div>
    </div>
  </div>
  <div v-if="showAddressModal" class="modal-overlay">
    <div class="modal-content">
      <h3 class="modal-title">
        <i class="ri-map-pin-line"></i>
        Chọn địa chỉ
      </h3>

      <!-- Tỉnh -->
      <div class="form-group">
        <label><i class="ri-building-line"></i> Tỉnh / Thành phố</label>
        <select
          v-model="selectedProvinceCode"
          @change="
            onChangeProvince();
            onSelectProvince();
          "
        >
          <option disabled value="">Chọn tỉnh</option>
          <option v-for="p in provinces" :key="p.code" :value="p.code">
            {{ p.name }}
          </option>
        </select>
      </div>

      <!-- Xã -->
      <div class="form-group">
        <label><i class="ri-community-line"></i> Xã / Phường</label>
        <select @change="onSelectWard($event.target.value)">
          <option disabled value="">Chọn xã</option>
          <option v-for="w in wards" :key="w.code" :value="w.name">
            {{ w.name }}
          </option>
        </select>
      </div>

      <!-- Địa chỉ chi tiết -->
      <div class="form-group">
        <label><i class="ri-home-4-line"></i> Địa chỉ chi tiết</label>
        <input
          v-model="customerForm.defaultAddress.diaChiChiTiet"
          placeholder="Số nhà, tên đường..."
        />
      </div>

      <!-- Actions -->
      <div class="modal-actions">
        <button class="btn-cancel" @click="closeAddressModal">
          <i class="ri-close-line"></i> Hủy
        </button>
        <button
          class="btn-confirm"
          :disabled="
            !selectedProvinceCode || !customerForm.defaultAddress.diaChiChiTiet
          "
          @click="closeAddressModal"
        >
          <i class="ri-check-line"></i> Xác nhận
        </button>
      </div>
    </div>
  </div>
  <!-- MODAL ĐỔI MẬT KHẨU -->
  <div v-if="showPasswordModal" class="modal-overlay">
    <div class="modal-content">
      <h3 class="modal-title">
        <i class="ri-shield-keyhole-line"></i>
        Đổi mật khẩu
      </h3>

      <!-- Mật khẩu cũ -->
      <div class="form-group">
        <label><i class="ri-lock-line"></i> Mật khẩu cũ</label>
        <div class="password-input-wrapper">
          <input
            v-model="passwordForm.matKhauCu"
            @input="passwordErrors.matKhauCu = ''"
            :type="showOldPass ? 'text' : 'password'"
            placeholder="Nhập mật khẩu cũ"
            :class="{ 'input-invalid': passwordErrors.matKhauCu }"
          />
          <i
            :class="showOldPass ? 'ri-eye-off-line' : 'ri-eye-line'"
            @click="showOldPass = !showOldPass"
          ></i>
        </div>
        <transition name="shake">
          <p v-if="passwordErrors.matKhauCu" class="error-hint">
            <i class="ri-error-warning-line"></i>
            {{ passwordErrors.matKhauCu }}
          </p>
        </transition>
      </div>

      <!-- Mật khẩu mới -->
      <div class="form-group">
        <label><i class="ri-lock-password-line"></i> Mật khẩu mới</label>
        <div class="password-input-wrapper">
          <input
            v-model="passwordForm.matKhauMoi"
            @input="passwordErrors.matKhauMoi = ''"
            :type="showNewPass ? 'text' : 'password'"
            placeholder="Nhập mật khẩu mới (tối thiểu 6 ký tự)"
            :class="{ 'input-invalid': passwordErrors.matKhauMoi }"
          />
          <i
            :class="showNewPass ? 'ri-eye-off-line' : 'ri-eye-line'"
            @click="showNewPass = !showNewPass"
          ></i>
        </div>
        <transition name="shake">
          <p v-if="passwordErrors.matKhauMoi" class="error-hint">
            <i class="ri-error-warning-line"></i>
            {{ passwordErrors.matKhauMoi }}
          </p>
        </transition>
      </div>

      <!-- Xác nhận mật khẩu mới -->
      <div class="form-group">
        <label
          ><i class="ri-lock-password-line"></i> Xác nhận mật khẩu mới</label
        >
        <div class="password-input-wrapper">
          <input
            v-model="passwordForm.xacNhanMatKhauMoi"
            @input="passwordErrors.xacNhanMatKhauMoi = ''"
            :type="showConfirmPass ? 'text' : 'password'"
            placeholder="Nhập lại mật khẩu mới"
            :class="{ 'input-invalid': passwordErrors.xacNhanMatKhauMoi }"
          />
          <i
            :class="showConfirmPass ? 'ri-eye-off-line' : 'ri-eye-line'"
            @click="showConfirmPass = !showConfirmPass"
          ></i>
        </div>
        <transition name="shake">
          <p v-if="passwordErrors.xacNhanMatKhauMoi" class="error-hint">
            <i class="ri-error-warning-line"></i>
            {{ passwordErrors.xacNhanMatKhauMoi }}
          </p>
        </transition>
      </div>

      <!-- Actions -->
      <div class="modal-actions">
        <button class="btn-cancel" @click="closePasswordModal">
          <i class="ri-close-line"></i> Hủy
        </button>
        <button
          class="btn-confirm"
          :disabled="isSubmittingPassword"
          @click="submitDoiMatKhau"
        >
          <i class="ri-check-line"></i>
          {{ isSubmittingPassword ? "Đang xử lý..." : "Xác nhận" }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
  font-weight: 500;
  font-style: italic;
}

.empty-state i {
  font-size: 40px;
  display: block;
  margin-bottom: 10px;
  color: #ccc;
}
/* //////////////// PHÂN TRANG ////////////////// */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 20px;
}

.page-btn {
  border: 1px solid #ddd;
  background: #fff;
  padding: 6px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: 0.2s;
}

.page-btn:hover:not(:disabled) {
  border-color: #d32f2f;
  color: #d32f2f;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info {
  font-size: 13px;
  font-weight: 600;
}
/* //////////////////////////////// LỊCH SỬ ĐƠN HÀNG //////////////// */
/* PAGE */
.order-tabs {
  display: flex;
  gap: 30px;
  border-bottom: 2px solid #eee;
  margin-bottom: 20px;
}

.tab-item {
  position: relative;
  padding: 10px 0;
  font-size: 14px;
  font-weight: 700;
  color: #777;
  cursor: pointer;
  transition: 0.2s;
}

.tab-item:hover {
  color: #d32f2f;
}

.tab-item::after {
  content: "";
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 3px;
  background: #d32f2f;
  transition: 0.3s;
}

.tab-item.active {
  color: #d32f2f;
}

.tab-item.active::after {
  width: 100%;
}
.order-card {
  display: flex;
  justify-content: space-between;
  align-items: center;

  padding: 18px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #eee;

  cursor: pointer;
  transition: 0.25s;
}

.order-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
}

/* LEFT */
.order-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-title {
  font-weight: 800;
  font-size: 15px;
}

.order-date {
  font-size: 13px;
  color: #888;
}

/* RIGHT */
.order-meta {
  text-align: right;
}

.order-total {
  font-weight: 800;
  margin-bottom: 5px;
}

/* STATUS */
.order-status {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 20px;
  font-weight: 700;
}

.order-status.shipping {
  background: #fff3cd;
  color: #856404;
}

.order-status.done {
  background: #d4edda;
  color: #155724;
}
.order-page {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

/* HEADER */
.order-header h2 {
  font-size: 35px;
  font-weight: 800;
}

.order-header p {
  color: #777;
  font-size: 14px;
}

/* LIST */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

/* CARD */
.order-card {
  display: flex;
  align-items: center;
  justify-content: space-between;

  padding: 18px 22px;
  border-radius: 12px;

  background: #fff;
  border: 1px solid #eee;

  cursor: pointer;
  transition: all 0.25s ease;
}

.order-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
}

/* LEFT */
.order-left {
  display: flex;
  flex-direction: column;
}

.order-code {
  font-weight: 800;
  color: #d32f2f;
}

.order-date {
  font-size: 13px;
  color: #999;
}

/* STATUS */
.order-status {
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
}

.order-status.shipping {
  background: #fff3cd;
  color: #856404;
}

.order-status.done {
  background: #d4edda;
  color: #155724;
}

/* RIGHT */
.order-right {
  font-weight: 800;
  font-size: 16px;
}

/* DETAIL */
.order-detail {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

/* BACK */
.back-btn {
  border: none;
  background: none;
  color: #d32f2f;
  font-weight: 700;
  cursor: pointer;
}

/* CARD DETAIL */
.detail-card {
  background: #fff;
  padding: 25px;
  border-radius: 14px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.08);
}

/* TOP */
.detail-top {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.detail-top h3 {
  font-weight: 800;
}

/* ITEMS */
.detail-items {
  display: flex;
  flex-direction: column;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px dashed #eee;
}

.item-name {
  font-weight: 600;
}

.item-qty {
  font-size: 12px;
  color: #d32f2f;
  font-weight: 700;
}

.item-price {
  font-weight: 700;
}

/* BOTTOM */
.detail-bottom {
  margin-top: 20px;
}

.total {
  display: flex;
  justify-content: space-between;
  font-size: 18px;
  font-weight: 800;
}

.price {
  color: #d32f2f;
}

/* BUTTON */
.cancel-btn {
  margin-top: 15px;
  width: 100%;
  padding: 14px;

  border: none;
  border-radius: 8px;

  background: #d32f2f;
  color: #fff;
  font-weight: 700;

  cursor: pointer;
  transition: 0.2s;
}

.cancel-btn:hover {
  background: #b71c1c;
}

.order-extra {
  display: flex;
  gap: 14px;
  margin-top: 6px;
}
.extra-item {
  display: flex;
  align-items: center;
  gap: 5px;

  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.extra-item i {
  font-size: 14px;
  color: #d32f2f;
}
.order-deposit {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 5px;

  font-size: 12px;
  color: #d32f2f;
  font-weight: 700;
}

.order-deposit i {
  font-size: 14px;
}
.no-table {
  color: #999;
}

.no-table i {
  color: #999;
}

/* Style cho thanh tìm kiếm đẳng cấp */
.search-box-wrapper {
  position: relative;
  min-width: 250px;
}

.search-icon {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #a31d21;
  font-size: 0.9rem;
}
.search-input:focus {
  border-color: #d32f2f !important;
  box-shadow: none !important;
  outline: none;
}

.search-input {
  padding-left: 40px;
  padding-right: 20px;
  height: 40px;
  border-radius: 30px;
  border: 2px solid #eee;
  font-size: 0.9rem;
  font-weight: 600;
  transition: all 0.3s ease;
  background: #fff;
}

/* //////////////// INPUT ĐỊA CHỈ /////////// */
.address-edit-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.address-edit-wrapper input {
  flex: 1;
  margin-right: 30px;
}

.btn-edit-address {
  border: none;
  background: transparent;
  color: #e53935;
  font-weight: 700;
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  margin-left: 20px;
}

.btn-edit-address:hover {
  opacity: 0.7;
}

/* Overlay */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.25);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
  animation: fadeIn 0.25s ease;
}

/* Modal */
.modal-content {
  width: 420px;
  background: #ffffff;
  color: #111827;
  padding: 24px;
  border-radius: 18px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.12);
  animation: slideUp 0.3s ease;
}

/* Title */
.modal-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 25px;
  font-weight: 600;
  margin-bottom: 18px;
  color: #111827;
}

/* Form */
.form-group {
  margin-bottom: 14px;
}

/* Label */
.form-group label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  margin-bottom: 6px;
  color: #6b7280;
}

/* Input + Select */
.form-group select,
.form-group input {
  width: 100%;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  background: #f9fafb;
  color: #111827;
  outline: none;
  font-size: 14px;
  transition: all 0.2s;
}

/* Hover */
.form-group select:hover,
.form-group input:hover {
  border-color: #d1d5db;
}

/* Focus */
.form-group select:focus,
.form-group input:focus {
  background: #ffffff;
  border-color: #f44e4e;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
}

.password-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
}

.password-input-wrapper input {
  width: 100%;
  padding-right: 40px !important;
}

.password-input-wrapper i {
  position: absolute;
  right: 12px;
  cursor: pointer;
  color: #6b7280;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  user-select: none;
}

.password-input-wrapper i:hover {
  color: #111827;
}

.input-invalid {
  border-color: #ef4444 !important;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.15) !important;
}

.error-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #ef4444;
  font-size: 12.5px;
  margin-top: 6px;
  padding: 6px 12px;
  background-color: #fef2f2;
  border-radius: 6px;
  text-align: left;
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

.shake-enter-active {
  animation: shake 0.4s cubic-bezier(0.36, 0.07, 0.19, 0.97) both;
}

@keyframes shake {
  10%, 90% {
    transform: translate3d(-1px, 0, 0);
  }
  20%, 80% {
    transform: translate3d(2px, 0, 0);
  }
  30%, 50%, 70% {
    transform: translate3d(-4px, 0, 0);
  }
  40%, 60% {
    transform: translate3d(4px, 0, 0);
  }
}

/* Actions */
.modal-actions {
  display: flex;
  gap: 10px;
  margin-top: 16px;
}

/* Buttons */
.modal-actions button {
  flex: 1;
  padding: 10px;
  border-radius: 7px;
  border: none;
  cursor: pointer;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: 0.2s;
}

/* Cancel */
.btn-cancel {
  background: #f3f4f6;
  color: #111827;
}

.btn-cancel:hover {
  background: #e5e7eb;
}

/* Confirm */
.btn-confirm {
  background: #ef3d3d;
  color: #fff;
}

.btn-confirm:hover {
  background: #eb2525;
}

/* Disabled */
.btn-confirm:disabled {
  background: #e8ebed;
  cursor: not-allowed;
}

/* Animation */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(25px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* //////////////////////////////////// */
.btn-minimal-action {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 10px 15px;
  color: #322424;
  text-decoration: none;
  font-weight: 700;
  font-size: 14px;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
}

.btn-minimal-action i {
  font-size: 18px;
  color: #e41e1e;
  transition: transform 0.3s ease;
}

.underline-animation {
  position: absolute;
  bottom: 5px;
  left: 15px;
  width: 0;
  height: 2px;
  background-color: #f93030;
  transition: width 0.3s ease;
}

.btn-minimal-action:hover {
  color: #eb3939;
}

.btn-minimal-action:hover i {
  transform: scale(1.1);
  color: #e12929;
}

.btn-minimal-action:hover .underline-animation {
  width: calc(100% - 30px);
}
.profile-page-wrapper {
  min-height: 100vh;
  background: #f4f4f4;
  font-family: var(--font-family);
  background-image: radial-gradient(
      at 0% 0%,
      rgba(212, 175, 55, 0.05) 0px,
      transparent 50%
    ),
    radial-gradient(at 100% 100%, rgba(139, 30, 30, 0.05) 0px, transparent 50%);
}

.main-royal-card {
  display: grid;
  grid-template-columns: 320px 1fr;
  background: #ffffff;
  border-radius: 4px;
  box-shadow: 0 40px 100px rgba(0, 0, 0, 0.08);
  min-height: 750px;
  overflow: hidden;
  border: 1px solid #eee;
}

/* SIDEBAR */
.sidebar-royal {
  background: #fafafa;
  border-right: 1px solid #f0f0f0;
  padding: 50px 30px;
}

.user-brand-section {
  text-align: center;
  margin-bottom: 50px;
}

.avatar-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto 20px;
}

.user-avatar-luxury {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border: 6px solid #fff;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  background: #fff;
}

.mini-upload-btn-style {
  position: absolute;
  bottom: 5px;
  right: 5px;
  width: 38px;
  height: 38px;
  background: #000;
  color: #fff;
  border: none;
  border-radius: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: 0.3s;
}

.user-name-gold {
  font-weight: 800;
  font-size: 1.2rem;
  color: #1a1a1a;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 8px;
}

.membership-tag {
  display: inline-flex;
  align-items: center;
  padding: 5px 15px;
  background: linear-gradient(135deg, #e46868 0%, #e54b4b 100%);
  color: white;
  font-size: 10px;
  font-weight: 900;
  letter-spacing: 2px;
  border-radius: 2px;
}

/* NAV MENU */
.nav-royal-menu {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.royal-menu-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 18px 20px;
  border: none;
  background: transparent;
  color: #888;
  font-weight: 700;
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  transition: 0.3s;
  cursor: pointer;
  text-align: left;
}

.royal-menu-item.active {
  color: #000;
  background: #fff;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.03);
}

.active-indicator {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 0;
  background: #d4af37;
  transition: 0.3s;
}

.royal-menu-item.active .active-indicator {
  height: 60%;
}

.security {
  color: #8b1e1e;
  margin-top: 10px;
}

.menu-separator {
  height: 1px;
  background: #eee;
  margin: 15px 0;
}

/* VIEWPORT */
.viewport-royal {
  padding: 60px 80px;
  background: #fff;
}

.royal-header {
  margin-bottom: 50px;
}

.title-main {
  font-weight: 800;
  font-size: 35px;
  letter-spacing: -0.5px;
  color: #1a1a1a;
  margin-bottom: 10px;
}

.title-line {
  width: 50px;
  height: 4px;
  background: #e72d2d;
  margin-bottom: 15px;
}

.subtitle-grey {
  color: #999;
  font-size: 0.9rem;
}

/* FORM GRID */
.grid-form-luxury {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

.full-span {
  grid-column: span 2;
}

.black-label-bold {
  display: block;
  font-weight: 1000;
  font-size: 0.75rem;
  color: #000;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 12px;
}

.input-field-wrapper {
  position: relative;
  border-bottom: 2px solid #eee;
  transition: 0.3s;
}

.input-field-wrapper input {
  width: 100%;
  padding: 12px 0;
  border: none;
  background: transparent;
  font-size: 1rem;
  font-weight: 600;
  outline: none;
}

.input-field-wrapper i {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  color: #ccc;
  font-size: 1.1rem;
}

.input-field-wrapper:focus-within {
  border-color: #d4af37;
}

.readonly input {
  color: #999;
  cursor: not-allowed;
}

/* GENDER SELECTION */
.gender-grid-royal {
  display: flex;
  gap: 15px;
}

.gender-card {
  flex: 1;
  cursor: pointer;
}

.gender-card input {
  display: none;
}

.gender-box {
  padding: 12px;
  border: 1px solid #eee;
  text-align: center;
  font-weight: 800;
  font-size: 0.75rem;
  letter-spacing: 1px;
  transition: 0.3s;
  color: #888;
}

.gender-card input:checked + .gender-box {
  border-color: #e94343;
  background: #d83737;
  color: #fff;
}

/* ORDER TABLE */
.table-container-royal {
  overflow-x: auto;
}

.royal-table.modern {
  border-collapse: separate;
  border-spacing: 0 12px;
}

.royal-table.modern tbody tr {
  background: #fff;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.05);
  transition: 0.25s;
}

.royal-table.modern tbody tr:hover {
  transform: translateY(-3px);
}

.royal-table.modern td {
  padding: 18px 15px;
}

.royal-table.modern td:first-child {
  border-top-left-radius: 10px;
  border-bottom-left-radius: 10px;
}

.royal-table.modern td:last-child {
  border-top-right-radius: 10px;
  border-bottom-right-radius: 10px;
}

.royal-table {
  width: 100%;
  border-collapse: collapse;
}

.royal-table th {
  padding: 15px 10px;
  border-bottom: 2px solid #eee;
  text-align: left;
}

.order-row {
  cursor: pointer;
  transition: 0.2s;
  border-bottom: 1px solid #f9f9f9;
}

.order-row:hover {
  background: #fafafa;
}

.order-row td {
  padding: 20px 10px;
  font-size: 0.9rem;
}

.order-id {
  font-weight: 700;
  color: #8b1e1e;
}

.status-pill {
  font-size: 11px;
  font-weight: 800;
  padding: 6px 14px;
  border-radius: 20px;
  letter-spacing: 0.5px;
}

.status-pill.shipping {
  background: #fff3cd;
  color: #856404;
}

.status-pill.done {
  background: #d4edda;
  color: #155724;
}

/* ORDER DETAIL VIEW */
.btn-back-link {
  background: none;
  border: none;
  color: #888;
  font-size: 0.8rem;
  font-weight: 700;
  text-transform: uppercase;
  margin-bottom: 25px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0;
}

.detail-card-flat {
  border-radius: 14px;
  padding: 30px;
  background: #fff;
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.08);
  border: none;
}
.detail-header {
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
}

.detail-title {
  font-size: 1.3rem;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.detail-title {
  font-weight: 800;
  font-size: 1.1rem;
  margin: 0;
}

.order-date-label {
  font-size: 0.85rem;
  color: #999;
}

.item-row {
  display: flex;
  justify-content: space-between;
  padding: 14px 0;
  border-bottom: 1px solid #f1f1f1;
}

.item-row:last-child {
  border-bottom: none;
}

.item-name {
  font-weight: 600;
  display: block;
}

.item-qty {
  font-size: 0.8rem;
  color: #8b1e1e;
  font-weight: 800;
}

.item-price {
  font-weight: 700;
}

.detail-footer {
  margin-top: 40px;
  padding-top: 25px;
  border-top: 2px solid #1a1a1a;
}

.total-row {
  display: flex;
  justify-content: space-between;
  font-weight: 800;
  font-size: 1.3rem;
  margin-bottom: 30px;
  color: #1a1a1a;
}

.btn-cancel-order {
  width: 100%;
  padding: 18px;
  background: transparent;
  border: 1px solid #8b1e1e;
  color: #8b1e1e;
  font-weight: 800;
  font-size: 0.75rem;
  letter-spacing: 2px;
  transition: 0.3s;
  cursor: pointer;
}
.btn-cancel-order {
  border-radius: 8px;
}

.btn-cancel-order:hover {
  transform: translateY(-2px);
}

/* ACTIONS */
.footer-actions {
  margin-top: 60px;
  display: flex;
  justify-content: flex-end;
}

.btn-save-royal {
  background: #1a1a1a;
  color: #fff;
  border: none;
  padding: 18px 50px;
  font-weight: 800;
  letter-spacing: 2px;
  font-size: 0.85rem;
  transition: 0.4s;
  cursor: pointer;
}

.btn-save-royal:hover {
  background: #8b1e1e;
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(139, 30, 30, 0.3);
}

.text-end {
  text-align: right;
}
.fw-bold {
  font-weight: 700;
}

/* ANIMATIONS */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
</style>
