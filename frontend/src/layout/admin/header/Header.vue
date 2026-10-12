<script setup>
import {
  onMounted,
  onBeforeUnmount,
  ref,
  Teleport,
  reactive,
  computed,
} from "vue";
import { Client } from "@stomp/stompjs";
import { useUtils } from "@/utils/helper";
import { doiMatKhau } from "@/api/admin/nhanVien/nhanVienApi";
/* ===== PROPS & EMITS ===== */
const props = defineProps({
  isInShift: {
    type: Boolean,
    default: false,
  },
});
const emit = defineEmits(["toggle-drawer", "ket-thuc-ca"]);
import SockJS from "sockjs-client";
import router from "@/router";
import { logoutAdmin } from "@/services/authService";

//////////////// STATE //////////////////
const latestOrder = ref(null);
const showToast = ref(false);

const savedNotifs = localStorage.getItem("admin_notifications");
const notifications = ref(savedNotifs ? JSON.parse(savedNotifs) : []);

const saveNotifications = () => {
  localStorage.setItem(
    "admin_notifications",
    JSON.stringify(notifications.value),
  );
};

const unreadCount = computed(
  () => notifications.value.filter((n) => !n.read).length,
);

const showDropdown = ref(false);
const showUserMenu = ref(false);

let toastTimer = null;

//////////////// WEBSOCKET //////////////////
const stompClient = new Client({
  webSocketFactory: () => new SockJS("http://localhost:8080/ws"),
  reconnectDelay: 5000,
});

const currentUser = ref({
  hoTen: localStorage.getItem("name") || "",
  vaiTro: localStorage.getItem("role") === "ADMIN" ? "Admin" : "Nhân viên",
  avatar: localStorage.getItem("avatar") || null,
});

stompClient.onConnect = () => {
  stompClient.subscribe("/topic/hoadon", (msg) => {
    const order = JSON.parse(msg.body);
    window.dispatchEvent(new CustomEvent("new-datban-message"));

    let notifType = "DAT_BAN_MOI";
    let notifTitle = "Đặt bàn mới!";
    let notifMsg = `Có đơn đặt bàn mới từ: ${order.tenKhachHang || "Khách hàng"}${order.soDienThoai ? " - " + order.soDienThoai : ""}`;

    // Phân loại theo trạng thái hóa đơn / đặt bàn
    if (order.trangThai === -1) {
      notifType = "HUY_DAT_BAN";
      notifTitle = "Hóa đơn / Đặt bàn đã hủy!";
      notifMsg = `Đơn đặt bàn ${order.maDatBan ? '#' + order.maDatBan : (order.maHoaDon ? '#' + order.maHoaDon : '')} của khách ${order.tenKhachHang || 'Khách hàng'} đã bị hủy`;
    } else if (order.trangThai === 5) {
      notifType = "CANH_BAO_HOAN_COC";
      notifTitle = "Cảnh báo hoàn cọc!";
      notifMsg = `Đơn đặt bàn ${order.maDatBan ? '#' + order.maDatBan : (order.maHoaDon ? '#' + order.maHoaDon : '')} đã hủy và đang chờ hoàn cọc`;
    } else if (order.trangThai === 2) {
      notifType = "THANH_TOAN_COC";
      notifTitle = "Khách đã cọc tiền!";
      notifMsg = `Đơn đặt bàn ${order.maDatBan ? '#' + order.maDatBan : ''} từ ${order.tenKhachHang || 'Khách hàng'} đã cọc tiền thành công`;
    } else if (order.trangThai === 3) {
      notifType = "DA_XAC_NHAN";
      notifTitle = "Đã xác nhận đặt bàn!";
      notifMsg = `Đơn đặt bàn ${order.maDatBan ? '#' + order.maDatBan : ''} đã được xác nhận`;
    } else if (order.trangThai === 8 || order.trangThai === 9) {
      notifType = "DA_THANH_TOAN";
      notifTitle = "Thanh toán thành công!";
      notifMsg = `Hóa đơn ${order.maHoaDon ? '#' + order.maHoaDon : ''} đã thanh toán thành công`;
    } else if (order.trangThai === 0 || order.trangThai === 4 || order.trangThai === 1) {
      notifType = "DAT_BAN_MOI";
      notifTitle = "Đặt bàn mới!";
      notifMsg = `Có đơn đặt bàn mới từ: ${order.tenKhachHang || "Khách hàng"}${order.soDienThoai ? " - " + order.soDienThoai : ""}`;
    } else {
      // Bỏ qua các trạng thái phục vụ nội bộ không cần bắn toast header
      return;
    }

    latestOrder.value = {
      ...order,
      type: notifType,
      title: notifTitle,
      message: notifMsg,
    };

    showToast.value = true;

    notifications.value.unshift({
      id: Date.now(),
      message: notifMsg,
      time: new Date().toLocaleTimeString(),
      read: false,
      data: {
        ...order,
        type: notifType,
      },
    });
    saveNotifications();

    if (toastTimer) clearTimeout(toastTimer);
    toastTimer = setTimeout(() => {
      showToast.value = false;
    }, 5000);
  });
  stompClient.subscribe("/topic/thong-bao-admin", (msg) => {
    const data = JSON.parse(msg.body);
    window.dispatchEvent(new CustomEvent("new-datban-message"));

    latestOrder.value = data;
    showToast.value = true;

    notifications.value.unshift({
      id: Date.now(),
      message: data.message,
      time: new Date().toLocaleTimeString(),
      read: false,
      data: data,
    });
    saveNotifications();

    if (toastTimer) clearTimeout(toastTimer);
    toastTimer = setTimeout(() => {
      showToast.value = false;
    }, 5000);
  });
  stompClient.subscribe("/topic/thong-bao-dat-ban", (msg) => {
    const data = JSON.parse(msg.body);
    window.dispatchEvent(new CustomEvent("new-datban-message"));

    latestOrder.value = {
      type: "NHAC_XAC_NHAN",
      message: data.noiDung,
      idDatBan: data.idDatBan,
    };
    showToast.value = true;

    notifications.value.unshift({
      id: Date.now(),
      message: data.noiDung,
      time: new Date().toLocaleTimeString(),
      read: false,
      data: { type: "NHAC_XAC_NHAN", idDatBan: data.idDatBan },
    });
    saveNotifications();

    if (toastTimer) clearTimeout(toastTimer);
    toastTimer = setTimeout(() => {
      showToast.value = false;
    }, 5000);
  });

  stompClient.subscribe("/topic/chat", (msg) => {
    const data = JSON.parse(msg.body);
    window.dispatchEvent(new CustomEvent("new-chat-message"));

    latestOrder.value = {
      type: "TIN_NHAN",
      message: data.tenKhachHang
        ? `Khách ${data.tenKhachHang} nhắn tin`
        : "Khách hàng nhắn tin",
    };
    showToast.value = true;

    notifications.value.unshift({
      id: Date.now(),
      message: data.tenKhachHang
        ? `Khách ${data.tenKhachHang} nhắn tin`
        : "Khách hàng nhắn tin",
      time: new Date().toLocaleTimeString(),
      read: false,
      data: { type: "TIN_NHAN" },
    });
    saveNotifications();

    if (toastTimer) clearTimeout(toastTimer);
    toastTimer = setTimeout(() => {
      showToast.value = false;
    }, 5000);
  });
};

//////////////// ACTION //////////////////
const openNotifications = () => {
  showDropdown.value = !showDropdown.value;

  showToast.value = false;

  if (toastTimer) {
    clearTimeout(toastTimer);
    toastTimer = null;
  }
};

const readAllNotifications = () => {
  notifications.value.forEach((n) => (n.read = true));
  saveNotifications();
};

const handleClick = (item) => {
  item.read = true;
  saveNotifications();

  if (item.data?.type === "TIN_NHAN") {
    router.push("/admin/nhan-tin");
  } else {
    router.push("/admin/dat-ban");
  }

  showDropdown.value = false;
};

const handleToastClick = () => {
  showToast.value = false;
  if (toastTimer) {
    clearTimeout(toastTimer);
    toastTimer = null;
  }
  if (latestOrder.value?.type === "TIN_NHAN") {
    router.push("/admin/nhan-tin");
  } else {
    router.push("/admin/dat-ban");
  }
};

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value;
  if (showUserMenu.value) showDropdown.value = false;
};

const handleLogout = async () => {
  showUserMenu.value = false;

  const refreshToken = localStorage.getItem("refreshToken");
  try {
    if (refreshToken) {
      await logoutAdmin(refreshToken);
    }
  } catch (e) {
    // vẫn tiếp tục xóa local dù API lỗi
  }

  localStorage.removeItem("token");
  localStorage.removeItem("refreshToken");
  localStorage.removeItem("role");
  localStorage.removeItem("name");
  localStorage.removeItem("avatar");

  router.push("/");
};

const { notify } = useUtils();

// Trạng thái modal đổi mật khẩu
const showChangePasswordModal = ref(false);
const isSavingPassword = ref(false);

const showOldPassword = ref(false);
const showNewPassword = ref(false);
const showConfirmPassword = ref(false);

const passwordForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const openChangePasswordModal = () => {
  showUserMenu.value = false;
  // Reset form
  passwordForm.oldPassword = "";
  passwordForm.newPassword = "";
  passwordForm.confirmPassword = "";
  showOldPassword.value = false;
  showNewPassword.value = false;
  showConfirmPassword.value = false;
  showChangePasswordModal.value = true;
};

const closeChangePasswordModal = () => {
  showChangePasswordModal.value = false;
};

const submitChangePassword = async () => {
  const oldPw = passwordForm.oldPassword.trim();
  const newPw = passwordForm.newPassword.trim();
  const confirmPw = passwordForm.confirmPassword.trim();

  if (!oldPw || !newPw || !confirmPw) {
    notify.error("Vui lòng điền đầy đủ các thông tin mật khẩu");
    return;
  }

  if (newPw.length < 6) {
    notify.error("Mật khẩu mới phải có độ dài từ 6 ký tự trở lên");
    return;
  }

  if (newPw === oldPw) {
    notify.error("Mật khẩu mới không được trùng với mật khẩu hiện tại");
    return;
  }

  if (newPw !== confirmPw) {
    notify.error("Xác nhận mật khẩu mới không trùng khớp");
    return;
  }

  const ok = await notify.confirm("Xác nhận đổi mật khẩu?");
  if (!ok) return;

  isSavingPassword.value = true;
  try {
    await doiMatKhau({
      matKhauCu: oldPw,
      matKhauMoi: newPw,
      xacNhanMatKhau: confirmPw,
    });
    notify.success("Đổi mật khẩu thành công!");
    showChangePasswordModal.value = false;
  } catch (err) {
    const msg = err.response?.data?.message || "Có lỗi xảy ra khi đổi mật khẩu";
    notify.error(msg);
  } finally {
    isSavingPassword.value = false;
  }
};

const handleClickOutside = (e) => {
  if (!e.target.closest(".notification-bell-wrapper")) {
    showDropdown.value = false;
  }
  if (!e.target.closest(".user-profile-wrapper")) {
    showUserMenu.value = false;
  }
};

//////////////// THEME //////////////////
const THEME_KEY = "theme";

const applyTheme = (theme) => {
  document.body.classList.toggle("dark-mode", theme === "dark");
};

const toggleTheme = () => {
  const isDark = document.body.classList.contains("dark-mode");
  const newTheme = isDark ? "light" : "dark";

  applyTheme(newTheme);
  localStorage.setItem(THEME_KEY, newTheme);
};

//////////////// LIFECYCLE //////////////////

onMounted(() => {
  // websocket
  stompClient.activate();

  // theme
  const savedTheme = localStorage.getItem(THEME_KEY) || "light";
  applyTheme(savedTheme);

  // click outside
  document.addEventListener("click", handleClickOutside);
});

onBeforeUnmount(() => {
  if (stompClient && stompClient.active) {
    stompClient.deactivate();
  }

  if (toastTimer) clearTimeout(toastTimer);

  document.removeEventListener("click", handleClickOutside);
});
</script>

<template>
  <header
    class="px-4 d-flex align-items-center justify-content-between header-main"
  >
    <!-- Hamburger: chỉ hiện khi màn ≤ 900px -->
    <button
      class="hamburger-btn"
      @click="emit('toggle-drawer')"
      aria-label="Mở menu"
    >
      <svg
        width="22"
        height="22"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2.2"
        stroke-linecap="round"
      >
        <line x1="3" y1="6" x2="21" y2="6" />
        <line x1="3" y1="12" x2="21" y2="12" />
        <line x1="3" y1="18" x2="21" y2="18" />
      </svg>
    </button>

    <!-- Right -->
    <div class="d-flex align-items-center gap-4 ms-auto">
      <div
        class="notification-bell-wrapper"
        :class="{ 'has-notif': unreadCount > 0 }"
        @click="openNotifications"
        title="Thông báo"
      >
        <!-- Bell SVG icon -->
        <svg
          class="bell-icon"
          viewBox="0 0 24 24"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
          <path
            d="M13.73 21a2 2 0 0 1-3.46 0"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
        <!-- Badge -->
        <span v-if="unreadCount > 0" class="notif-badge">
          {{ unreadCount > 9 ? "9+" : unreadCount }}
        </span>
      </div>

      <!-- Popup confirm -->
      <Teleport to="body">
        <transition name="ultra-lux">
          <div v-if="showDropdown" class="notification-dropdown">
            <div
              class="dropdown-header d-flex justify-content-between align-items-center"
            >
              <div class="header-content">
                <span>Thông báo</span>
                <i class="ri-fire-fill fire-icon-lux"></i>
              </div>
              <span
                v-if="unreadCount > 0"
                class="read-all-btn"
                @click.stop="readAllNotifications"
                >Đọc tất cả</span
              >
            </div>
            <div class="header-line"></div>
            <div v-if="notifications.length === 0" class="empty">
              Không có thông báo
            </div>
            <div
              v-for="item in notifications"
              :key="item.id"
              class="notification-item"
              @click="handleClick(item)"
              :class="{ unread: !item.read }"
            >
              <div class="d-flex justify-content-between align-items-center">
                <div>
                  <div class="message">{{ item.message }}</div>
                  <div class="time">{{ item.time }}</div>
                </div>
                <span v-if="!item.read" class="unread-dot"></span>
              </div>
            </div>
          </div>
        </transition>

        <transition name="toast-slide">
          <div
            class="toast-monolith"
            v-if="showToast"
            @click="handleToastClick"
            style="cursor: pointer"
          >
            <!-- Toast header: label + X -->
            <div class="toast-header">
              <span class="toast-label">Thông báo mới</span>
              <div class="toast-header-right">
                <span class="toast-time">Vừa xong</span>
                <div class="toast-close-hint" @click.stop="handleToastClick">
                  <i class="bi bi-x"></i>
                </div>
              </div>
            </div>

            <!-- Toast body: bell + title + desc -->
            <div class="toast-body">
              <!-- Icon -->
              <div class="toast-icon-wrap">
                <i class="bi bi-bell-fill toast-bell-icon"></i>
              </div>

              <!-- Content -->
              <div class="toast-content">
                <div class="toast-title">
                  {{
                    latestOrder?.title ||
                    (latestOrder?.type === "HUY_DAT_BAN"
                      ? "Khách hủy đặt bàn!"
                      : latestOrder?.type === "CANH_BAO_HOAN_COC"
                      ? "Cảnh báo hoàn cọc!"
                      : latestOrder?.type === "THANH_TOAN_COC"
                      ? "Khách đã cọc tiền!"
                      : latestOrder?.type === "NHAC_XAC_NHAN"
                      ? "Nhắc xác nhận đặt bàn!"
                      : latestOrder?.type === "TIN_NHAN"
                      ? "Tin nhắn mới!"
                      : latestOrder?.type === "DA_XAC_NHAN"
                      ? "Đã xác nhận đặt bàn!"
                      : latestOrder?.type === "DA_THANH_TOAN"
                      ? "Thanh toán thành công!"
                      : "Đặt bàn mới!")
                  }}
                </div>
                <div class="toast-desc">
                  {{ latestOrder?.message || "Có đơn đặt bàn từ khách hàng" }}
                </div>
              </div>
            </div>

            <!-- Progress bar -->
            <div class="toast-progress"></div>
          </div>
        </transition>
      </Teleport>

      <!-- User -->
      <div class="user-profile-wrapper">
        <div
          class="user-profile"
          title="Thông tin tài khoản"
          @click.stop="toggleUserMenu"
        >
          <div class="text-end lh-sm">
            <div class="user-name">{{ currentUser.hoTen }}</div>
            <div class="user-role">{{ currentUser.vaiTro }}</div>
          </div>

          <div class="user-avatar">
            <img src="@/assets/icons/taikhoan.png" width="20" height="20" />
          </div>
        </div>

        <!-- User Menu Dropdown -->
        <Teleport to="body">
          <transition name="user-menu">
            <div v-if="showUserMenu" class="user-menu-dropdown">
              <div class="user-menu-header">
                <div class="user-menu-avatar">
                  <img
                    src="@/assets/icons/taikhoan.png"
                    width="22"
                    height="22"
                  />
                </div>
                <div>
                  <div class="user-menu-name">{{ currentUser.hoTen }}</div>
                  <div class="user-menu-role">{{ currentUser.vaiTro }}</div>
                </div>
              </div>
              <div class="user-menu-divider"></div>
              <button
                class="user-menu-item"
                @click="
                  showUserMenu = false;
                  $router.push('/admin/nhan-vien/me');
                "
              >
                <i class="ri-user-line user-menu-icon"></i>
                <span>Thông tin cá nhân</span>
              </button>
              <button class="user-menu-item" @click="openChangePasswordModal">
                <i class="ri-lock-password-line user-menu-icon"></i>
                <span>Đổi mật khẩu</span>
              </button>
              <button
                v-if="isInShift"
                class="user-menu-item text-danger"
                @click="
                  $emit('ket-thuc-ca');
                  showUserMenu = false;
                "
              >
                <i class="bi bi-box-arrow-right user-menu-icon text-danger"></i>
                <span>Kết thúc ca làm</span>
              </button>
              <div class="user-menu-divider"></div>
              <button
                class="user-menu-item user-menu-item--danger"
                @click="handleLogout"
              >
                <i class="ri-logout-box-r-line user-menu-icon"></i>
                <span>Đăng xuất</span>
              </button>
            </div>
          </transition>
        </Teleport>

        <!-- Modal đổi mật khẩu -->
        <Teleport to="body">
          <transition name="modal-fade">
            <div
              v-if="showChangePasswordModal"
              class="pw-modal-overlay"
              @click.self="closeChangePasswordModal"
            >
              <div class="pw-modal-content">
                <div class="pw-modal-header">
                  <h3>Đổi mật khẩu</h3>
                  <button
                    class="pw-close-btn"
                    @click="closeChangePasswordModal"
                  >
                    &times;
                  </button>
                </div>

                <div class="pw-modal-body">
                  <div class="pw-form-group">
                    <label>Mật khẩu hiện tại</label>
                    <div class="pw-input-wrapper">
                      <input
                        :type="showOldPassword ? 'text' : 'password'"
                        v-model="passwordForm.oldPassword"
                        placeholder="Nhập mật khẩu hiện tại"
                        class="pw-input"
                      />
                      <button
                        class="pw-eye-btn"
                        @click="showOldPassword = !showOldPassword"
                      >
                        <i
                          :class="
                            showOldPassword ? 'bi bi-eye-slash' : 'bi bi-eye'
                          "
                        ></i>
                      </button>
                    </div>
                  </div>

                  <div class="pw-form-group">
                    <label>Mật khẩu mới</label>
                    <div class="pw-input-wrapper">
                      <input
                        :type="showNewPassword ? 'text' : 'password'"
                        v-model="passwordForm.newPassword"
                        placeholder="Nhập mật khẩu mới"
                        class="pw-input"
                      />
                      <button
                        class="pw-eye-btn"
                        @click="showNewPassword = !showNewPassword"
                      >
                        <i
                          :class="
                            showNewPassword ? 'bi bi-eye-slash' : 'bi bi-eye'
                          "
                        ></i>
                      </button>
                    </div>
                  </div>

                  <div class="pw-form-group">
                    <label>Xác nhận mật khẩu mới</label>
                    <div class="pw-input-wrapper">
                      <input
                        :type="showConfirmPassword ? 'text' : 'password'"
                        v-model="passwordForm.confirmPassword"
                        placeholder="Nhập lại mật khẩu mới"
                        class="pw-input"
                      />
                      <button
                        class="pw-eye-btn"
                        @click="showConfirmPassword = !showConfirmPassword"
                      >
                        <i
                          :class="
                            showConfirmPassword
                              ? 'bi bi-eye-slash'
                              : 'bi bi-eye'
                          "
                        ></i>
                      </button>
                    </div>
                  </div>
                </div>

                <div class="pw-modal-footer">
                  <button
                    class="pw-btn pw-btn-cancel"
                    @click="closeChangePasswordModal"
                  >
                    Hủy bỏ
                  </button>
                  <button
                    class="btn-submit"
                    :disabled="isSavingPassword"
                    @click="submitChangePassword"
                  >
                    <i class="bi bi-check2-all"></i>
                    <div class="btn-light-sweep"></div>
                    Xác nhận
                  </button>
                </div>
              </div>
            </div>
          </transition>
        </Teleport>
      </div>
    </div>
  </header>
</template>
<style scoped>
/* =====================================================
   HEADER
===================================================== */
.header-main {
  height: 88px;
  background: #ffffff !important;
  border-bottom: 1px solid #f1f1f4;
  box-shadow: 0 1px 0 0 rgba(0, 0, 0, 0.05);
}

/* =====================================================
   NOTIFICATION BELL
===================================================== */
.notification-bell-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  cursor: pointer;
  color: #6b7280;
  transition: background 0.2s ease, color 0.2s ease, transform 0.18s ease;
}

.notification-bell-wrapper:hover {
  background: #f3f4f6;
  color: #111827;
  transform: scale(1.05);
}

.notification-bell-wrapper:active {
  transform: scale(0.95);
}

/* Shake animation khi có thông báo */
.notification-bell-wrapper.has-notif .bell-icon {
  animation: bell-shake 0.5s ease 0.1s;
}

@keyframes bell-shake {
  0% {
    transform: rotate(0deg);
  }
  15% {
    transform: rotate(14deg);
  }
  30% {
    transform: rotate(-10deg);
  }
  45% {
    transform: rotate(8deg);
  }
  60% {
    transform: rotate(-5deg);
  }
  75% {
    transform: rotate(3deg);
  }
  100% {
    transform: rotate(0deg);
  }
}

.bell-icon {
  width: 20px;
  height: 20px;
  display: block;
  flex-shrink: 0;
}

/* Badge số thông báo */
.notif-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: #ef4444;
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  line-height: 16px;
  border-radius: 8px;
  border: 1.5px solid #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  letter-spacing: 0;
  box-shadow: 0 1px 4px rgba(239, 68, 68, 0.4);
}

/* =====================================================
   NOTIFICATION DROPDOWN
===================================================== */
.notification-dropdown {
  position: fixed;
  top: 74px;
  right: 20px;
  width: 360px;
  max-height: 480px;
  z-index: 9999999 !important;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.07),
    0 20px 40px -8px rgba(0, 0, 0, 0.12);
  overflow-y: auto;
  overflow-x: hidden;
}

.notification-dropdown::-webkit-scrollbar {
  width: 4px;
}

.notification-dropdown::-webkit-scrollbar-track {
  background: transparent;
}

.notification-dropdown::-webkit-scrollbar-thumb {
  background: #e5e7eb;
  border-radius: 4px;
}

.notification-dropdown::-webkit-scrollbar-thumb:hover {
  background: #d1d5db;
}

/* Header dropdown */
.dropdown-header {
  padding: 16px 18px 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dropdown-header .header-content span {
  font-size: 15px;
  font-weight: 700;
  color: #111827;
  letter-spacing: -0.2px;
}

.fire-icon-lux {
  font-size: 16px;
  background: linear-gradient(180deg, #fbbf24 0%, #ef4444 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: pulse-fire 2.5s ease-in-out infinite;
}

@keyframes pulse-fire {
  0%,
  100% {
    opacity: 0.85;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.08);
  }
}

.header-line {
  height: 1px;
  background: #f3f4f6;
  margin: 0;
}

.read-all-btn {
  font-size: 12px;
  color: #6b7280;
  cursor: pointer;
  font-weight: 500;
  padding: 4px 10px;
  border-radius: 6px;
  transition: all 0.2s ease;
  border: 1px solid #e5e7eb;
  background: transparent;
}

.read-all-btn:hover {
  background: #f3f4f6;
  color: #374151;
  border-color: #d1d5db;
}

/* Notification items */
.notification-item {
  padding: 12px 18px;
  cursor: pointer;
  border-bottom: 1px solid #f9fafb;
  transition: background 0.15s ease;
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-item:hover {
  background: #f9fafb;
}

.notification-item.unread {
  background: #fef2f2;
}

.notification-item.unread:hover {
  background: #fee2e2;
}

.message {
  font-size: 13px;
  font-weight: 500;
  color: #111827;
  line-height: 1.45;
}

.notification-item.unread .message {
  color: #111827;
  font-weight: 600;
}

.time {
  font-size: 11.5px;
  color: #9ca3af;
  margin-top: 3px;
  font-weight: 400;
}

.unread-dot {
  width: 7px;
  height: 7px;
  background: #ef4444;
  border-radius: 50%;
  flex-shrink: 0;
  margin-left: 8px;
}

.empty {
  padding: 32px 16px;
  text-align: center;
  color: #9ca3af;
  font-size: 13.5px;
}
/* ========== TOAST MONOLITH ========== */
.toast-monolith {
  position: fixed;
  top: 80px;
  right: 24px;
  z-index: 999999;
  width: 340px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.07),
    0 16px 32px -4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.toast-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px 6px;
  border-bottom: 1px solid #f3f4f6;
}

.toast-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toast-body {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px 16px;
  position: relative;
  z-index: 2;
}

/* Icon bell */
.toast-icon-wrap {
  position: relative;
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toast-bell-icon {
  color: #ef4444;
  font-size: 17px;
}

/* Content */
.toast-content {
  flex: 1;
  min-width: 0;
}

.toast-top-row {
  display: none;
}

.toast-label {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.8px;
  text-transform: uppercase;
  color: #ef4444;
}

.toast-time {
  font-size: 11px;
  color: #9ca3af;
  font-weight: 400;
}

.toast-title {
  font-size: 14px;
  font-weight: 700;
  color: #111827;
  letter-spacing: -0.1px;
  margin-bottom: 2px;
}

.toast-desc {
  font-size: 12px;
  color: #6b7280;
  font-weight: 400;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Close hint */
.toast-close-hint {
  width: 26px;
  height: 26px;
  border-radius: 6px;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
  font-size: 16px;
  flex-shrink: 0;
  transition: all 0.15s;
  cursor: pointer;
}

.toast-close-hint:hover {
  background: #fee2e2;
  color: #ef4444;
}

/* Progress bar đếm ngược */
.toast-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 3px;
  width: 100%;
  background: linear-gradient(90deg, #e53935, #ff5722, #ff8a65);
  border-radius: 0 0 18px 18px;
  animation: countdownBar 5s linear forwards;
  transform-origin: left;
}

@keyframes countdownBar {
  0% {
    width: 100%;
    opacity: 1;
  }
  85% {
    opacity: 1;
  }
  100% {
    width: 0%;
    opacity: 0;
  }
}

/* === Transition: ultra-lux (dropdown) === */
.ultra-lux-enter-active {
  transition: all 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.ultra-lux-leave-active {
  transition: all 0.3s cubic-bezier(0.55, 0, 1, 0.45);
}
.ultra-lux-enter-from {
  transform: translateY(-12px) scale(0.95);
  opacity: 0;
}
.ultra-lux-leave-to {
  transform: translateY(-8px) scale(0.96);
  opacity: 0;
}

/* === Transition: toast-slide (toast popup) === */
.toast-slide-enter-active {
  transition: all 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.toast-slide-leave-active {
  transition: all 0.35s cubic-bezier(0.55, 0, 1, 0.45);
}
.toast-slide-enter-from {
  transform: translateX(100%) scale(0.9);
  opacity: 0;
}
.toast-slide-leave-to {
  transform: translateX(110%) scale(0.95);
  opacity: 0;
}

/* ===== RESPONSIVE ===== */
/* ===== HAMBURGER BUTTON ===== */
.hamburger-btn {
  display: none; /* Ẩn trên desktop */
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 10px;
  background: #f1f5f9;
  color: #475569;
  cursor: pointer;
  flex-shrink: 0;
  transition: background 0.2s, color 0.2s, transform 0.15s;
}

.hamburger-btn:hover {
  background: #fee2e2;
  color: #dc2626;
  transform: scale(1.05);
}

.hamburger-btn:active {
  transform: scale(0.95);
}

/* ===== RESPONSIVE ===== */
@media (max-width: 900px) {
  /* Hiện nút hamburger */
  .hamburger-btn {
    display: flex;
  }

  .notification-dropdown {
    width: 320px;
    right: 10px;
  }

  .toast-monolith {
    width: 300px;
    right: 10px;
  }
}

@media (max-width: 600px) {
  .notification-dropdown {
    width: calc(100vw - 24px) !important;
    right: 12px !important;
    left: 12px !important;
    top: 56px !important;
  }

  .toast-monolith {
    width: calc(100vw - 24px) !important;
    right: 12px !important;
    top: 64px !important;
  }

  .header-main {
    min-height: 74px !important; /* trước: 60px, tăng nhẹ theo tỉ lệ mobile */
    padding-left: 12px !important;
    padding-right: 12px !important;
  }
}

/* ===== USER PROFILE HOVER ===== */
.user-profile-wrapper {
  position: relative;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 10px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.22s ease, box-shadow 0.22s ease, transform 0.18s ease;
  border: 1px solid transparent;
}

.user-profile:hover {
  background: rgba(239, 68, 68, 0.06);
  border-color: rgba(239, 68, 68, 0.15);
  box-shadow: 0 2px 12px rgba(239, 68, 68, 0.08);
  transform: translateY(-1px);
}

.user-profile:active {
  transform: translateY(0) scale(0.98);
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  transition: color 0.22s ease;
  white-space: nowrap;
}

.user-profile:hover .user-name {
  color: #dc2626;
}

.user-role {
  font-size: 11.5px;
  color: #9ca3af;
  text-align: center;
  font-weight: 500;
  letter-spacing: 0.3px;
  transition: color 0.22s ease;
}

.user-profile:hover .user-role {
  color: #ef4444;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background 0.22s ease, transform 0.22s ease, box-shadow 0.22s ease;
  border: 2px solid transparent;
}

.user-profile:hover .user-avatar {
  background: #fee2e2;
  border-color: rgba(239, 68, 68, 0.3);
  transform: scale(1.08);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.2);
}

/* ===== USER MENU DROPDOWN ===== */
.user-menu-dropdown {
  position: fixed;
  top: 78px;
  right: 20px;
  width: 220px;
  z-index: 9999999;
  background: rgba(255, 255, 255, 0.97);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 14px;
  border: 1px solid rgba(239, 68, 68, 0.1);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12), 0 2px 8px rgba(239, 68, 68, 0.08);
  overflow: hidden;
  padding: 6px 0;
}

.user-menu-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px 10px;
}

.user-menu-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #fee2e2;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid rgba(239, 68, 68, 0.2);
  flex-shrink: 0;
}

.user-menu-name {
  font-size: 13px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.3;
}

.user-menu-role {
  font-size: 11px;
  color: #9ca3af;
  font-weight: 500;
}

.user-menu-divider {
  height: 1px;
  background: #f1f1f1;
  margin: 4px 0;
}

.user-menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 10px 16px;
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 13.5px;
  font-weight: 500;
  color: #374151;
  transition: background 0.15s ease, color 0.15s ease, padding-left 0.15s ease;
  text-align: left;
}

.user-menu-item:hover {
  background: rgba(239, 68, 68, 0.05);
  color: #dc2626;
  padding-left: 20px;
}

.user-menu-item--danger {
  color: #dc2626;
}

.user-menu-item--danger:hover {
  background: rgba(239, 68, 68, 0.08);
  color: #b91c1c;
  padding-left: 20px;
}

.user-menu-icon {
  font-size: 16px;
  flex-shrink: 0;
  width: 18px;
  text-align: center;
}

/* Transition user menu */
.user-menu-enter-active {
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.user-menu-leave-active {
  transition: all 0.18s cubic-bezier(0.55, 0, 1, 0.45);
}
.user-menu-enter-from {
  transform: translateY(-8px) scale(0.95);
  opacity: 0;
}
.user-menu-leave-to {
  transform: translateY(-6px) scale(0.97);
  opacity: 0;
}

/* ========== MODAL ĐỔI MẬT KHẨU ========== */
.pw-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99999;
}

.pw-modal-content {
  background: #ffffff;
  width: 90%;
  max-width: 400px;
  border-radius: 10px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1),
    0 10px 10px -5px rgba(0, 0, 0, 0.04);
  overflow: hidden;
  border: 1px solid rgba(239, 68, 68, 0.1);
  display: flex;
  flex-direction: column;
}

.pw-modal-header {
  padding: 18px 24px;
  border-bottom: 1px solid #f3f4f6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pw-modal-header h3 {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}

.pw-close-btn {
  background: transparent;
  border: none;
  font-size: 26px;
  cursor: pointer;
  color: #9ca3af;
  line-height: 1;
  padding: 0;
  transition: color 0.15s ease;
}

.pw-close-btn:hover {
  color: #ef4444;
}

.pw-modal-body {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pw-form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pw-form-group label {
  font-size: 13.5px;
  font-weight: 600;
  color: #374151;
  text-align: left;
}

.pw-input-wrapper {
  position: relative;
  width: 100%;
}

.pw-input {
  width: 100%;
  padding: 10px 42px 10px 14px;
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  font-size: 14px;
  color: #1f2937;
  background: #ffffff;
  outline: none;
  transition: all 0.2s ease;
  box-sizing: border-box;
}

.pw-input:focus {
  border-color: #ef4444;
  box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.08);
}

.pw-eye-btn {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: transparent;
  border: none;
  color: #9ca3af;
  cursor: pointer;
  font-size: 16px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.15s ease;
}

.pw-eye-btn:hover {
  color: #ef4444;
}

.pw-modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #f3f4f6;
  background: #f9fafb;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.pw-btn {
  padding: 10px 18px;
  border-radius: 7px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  outline: none;
}

.pw-btn-cancel {
  background: #ffffff;
  color: #4b5563;
  border: 1px solid #d1d5db;
}

.pw-btn-cancel:hover {
  background: #f3f4f6;
  color: #1f2937;
}

.pw-btn-save {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.2);
}

.pw-btn-save:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(239, 68, 68, 0.3);
}

.pw-btn-save:disabled {
  opacity: 0.65;
  cursor: not-allowed;
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
/* Transition Modal */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.25s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-active .pw-modal-content {
  animation: pwModalZoomIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.modal-fade-leave-active .pw-modal-content {
  animation: pwModalZoomOut 0.2s ease-in;
}

@keyframes pwModalZoomIn {
  from {
    transform: scale(0.92);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes pwModalZoomOut {
  from {
    transform: scale(1);
    opacity: 1;
  }
  to {
    transform: scale(0.95);
    opacity: 0;
  }
}
</style>
