<template>
  <div
    class="bg-white d-flex flex-column border-end sidebar"
    :class="{ collapsed: isCollapsed, 'drawer-open': isDrawerOpen }"
  >
    <!-- NÚT THU / PHÓNG (chỉ hiện trên desktop) -->
    <div class="toggle-btn desktop-only" @click="toggleSidebar">
      <img :src="isCollapsed ? arrowRight : arrowLeft" width="13" />
    </div>

    <!-- LOGO -->
    <!-- LOGO -->
    <div
      class="border-bottom sidebar-logo d-flex align-items-center justify-content-center"
    >
      <img :src="logo" alt="FOURSPICEHOTPOT" class="logo-img" />
    </div>

    <!-- MENU -->
    <div class="sidebar-menu">
      <!-- Truyền collapsed=false khi mở drawer trên mobile để hiện đầy đủ text -->
      <SideBarMenu :collapsed="isCollapsed && !isDrawerOpen" />
    </div>

    <!-- LOGOUT / KẾT THÚC CA -->
    <div class="border-top sidebar-logout">
      <div
        class="logout-item d-flex align-items-center gap-2"
        @click="dangXuat"
        v-tooltip="isCollapsed && !isDrawerOpen ? 'Đăng xuất' : ''"
      >
        <span v-if="!isCollapsed || isDrawerOpen">Đăng xuất</span>
        <img src="@/assets/icons/logout.png" width="18" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";
import logo from "@/assets/icons/logodatn.png";
import SideBarMenu from "../sidebarmenu/SideBarMenu.vue";
import arrowLeft from "@/assets/icons/thu-vao.png";
import arrowRight from "@/assets/icons/mo-ra.png";
import { logoutAdmin } from "@/services/authService";
/* ===== PROPS & EMITS ===== */
const props = defineProps({
  isDrawerOpen: {
    type: Boolean,
    default: false,
  },
});
defineEmits(["close-drawer"]);

/* ===== STATE ===== */
const isCollapsed = ref(false);
const router = useRouter();
const BREAKPOINT = 900;

/* ===== ACTIONS ===== */
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value;
};

const dangXuat = async () => {
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

  router.replace("/");
};

const handleResize = () => {
  if (window.innerWidth <= BREAKPOINT) {
    isCollapsed.value = false;
  }
};

/* ===== LIFECYCLE ===== */
onMounted(() => {
  window.addEventListener("resize", handleResize);
  handleResize();
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", handleResize);
});
</script>

<style scoped>
.sidebar {
  flex-shrink: 0;
  height: 100vh;
  width: 260px;
  transition: width 0.28s cubic-bezier(0.4, 0, 0.2, 1),
    transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 100;
}

.sidebar.collapsed {
  width: 85px;
}

/* ===== Toggle btn (desktop) ===== */
.toggle-btn {
  position: absolute;
  top: 30px;
  right: 12px;
  cursor: pointer;
  z-index: 100;
}

.toggle-btn img {
  opacity: 0.6;
  transition: 0.2s;
}
.toggle-btn:hover img {
  opacity: 1;
}

.desktop-only {
  display: flex;
}

/* ===== Menu ===== */
.sidebar-menu {
  flex: 1;
  overflow-y: auto;
  overflow-x: visible;
  scrollbar-width: none;
}

.sidebar-menu::-webkit-scrollbar {
  display: none;
}

/* Khi thu lại, không cần scroll → bỏ overflow để v-tooltip hoạt động */
.sidebar.collapsed .sidebar-menu {
  overflow-y: auto;
  overflow-x: hidden;
}

/* ===== Logout ===== */
.sidebar-logout {
  padding: 12px;
}

.logout-item {
  cursor: pointer;
  justify-content: center;
  font-weight: 600;
}

/* ===== Responsive: Drawer mode khi màn ≤ 900px ===== */
@media (max-width: 900px) {
  .sidebar {
    /* Ẩn sidebar ra ngoài màn hình */
    position: fixed;
    top: 0;
    left: 0;
    height: 100vh;
    width: 270px !important; /* Luôn full width khi drawer */
    transform: translateX(-100%);
    z-index: 2000;
    box-shadow: none;
  }

  /* Khi drawer mở: trượt vào */
  .sidebar.drawer-open {
    transform: translateX(0);
    box-shadow: 8px 0 32px rgba(0, 0, 0, 0.18);
  }

  /* Ẩn nút mũi tên */
  .desktop-only {
    display: none !important;
  }
}

.logout-item {
  cursor: pointer;
  justify-content: center;
  font-weight: 600;
  padding: 8px 12px;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.logout-item:hover {
  background-color: #fff0f0;
  color: #dc3545;
}
.logout-item:hover img {
  filter: invert(18%) sepia(90%) saturate(700%) hue-rotate(330deg);
}
.logo-img {
  height: 72px;
  width: auto;
  object-fit: contain;
  display: block;
  transition: height 0.28s cubic-bezier(0.4, 0, 0.2, 1);
}

/* ===== Logo ===== */
.sidebar-logo {
  height: 88px;
}

.sidebar.collapsed .logo-img {
  height: 32px;
}
</style>
