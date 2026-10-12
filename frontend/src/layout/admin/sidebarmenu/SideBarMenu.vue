<script setup>
import { ref, watch, onMounted, onUnmounted } from "vue";
import { useRoute } from "vue-router";

// PHÂN QUYÊN
const role = localStorage.getItem("role");

const props = defineProps({
  collapsed: Boolean,
});

watch(
  () => props.collapsed,
  (val) => {
    console.log("collapsed =", val);
  },
  { immediate: true },
);

/* ===== ICON MENU (GIỮ NGUYÊN) ===== */
import datBan from "@/assets/icons/datban.png";
import donHang from "@/assets/icons/donhang.png";
import checkIn from "@/assets/icons/checkin.png";
import quanLyBan from "@/assets/icons/quanlyban.png";
import nhanVien from "@/assets/icons/nhanvien.png";
import khachHang from "@/assets/icons/khachhang.png";
import danhMucIcon from "@/assets/icons/danhmuc.png";
import khuyenMai from "@/assets/icons/khuyenmai.png";
import dotGiamGia from "@/assets/icons/dotgiamgia.png";
import nhanTin from "@/assets/icons/nhantin.png";
import phanHoi from "@/assets/icons/phanhoi.png";
import thamSoDong from "@/assets/icons/thamso.png";
import baoCao from "@/assets/icons/baocao.png";
import lauIcon from "@/assets/icons/lau.png";
import iconThuocTinh from "@/assets/icons/scale.png";
import iconLichLamViec from "@/assets/icons/calendar.png";
/* ===== ICON PHỤ (GIỮ NGUYÊN) ===== */
import iconRight from "@/assets/icons/arrow-right.png";
import iconDown from "@/assets/icons/arrow-down.png";
import iconDot from "@/assets/icons/tron.png";
import iconCheck from "@/assets/icons/check.png";

const route = useRoute();

const hasUnreadChat = ref(false);
const hasUnreadDatBan = ref(false);

const handleNewChat = () => {
  hasUnreadChat.value = true;
};
const handleReadChat = () => {
  hasUnreadChat.value = false;
};

const handleNewDatBan = () => {
  hasUnreadDatBan.value = true;
};
const handleReadDatBan = () => {
  hasUnreadDatBan.value = false;
};

onMounted(() => {
  window.addEventListener('new-chat-message', handleNewChat);
  window.addEventListener('read-chat-message', handleReadChat);
  window.addEventListener('new-datban-message', handleNewDatBan);
  window.addEventListener('read-datban-message', handleReadDatBan);
});

onUnmounted(() => {
  window.removeEventListener('new-chat-message', handleNewChat);
  window.removeEventListener('read-chat-message', handleReadChat);
  window.removeEventListener('new-datban-message', handleNewDatBan);
  window.removeEventListener('read-datban-message', handleReadDatBan);
});

watch(
  () => route.path,
  (newPath) => {
    if (newPath === '/admin/nhan-tin') {
      hasUnreadChat.value = false;
    }
    if (newPath === '/admin/dat-ban') {
      hasUnreadDatBan.value = false;
    }
  },
  { immediate: true }
);

const handleMenuClick = (item) => {
  if (item.path === '/admin/nhan-tin') {
    hasUnreadChat.value = false;
    window.dispatchEvent(new CustomEvent('read-chat-message'));
  }
  if (item.path === '/admin/dat-ban') {
    hasUnreadDatBan.value = false;
    window.dispatchEvent(new CustomEvent('read-datban-message'));
  }
  if (route.path === item.path) {
    window.dispatchEvent(new CustomEvent("reload-page", { detail: item.path }));
  }
};

const toggleSubmenu = (name) => {
  openMenus.value[name] = !openMenus.value[name];
};

/* ===== TỔ CHỨC LẠI MẢNG MENU (GỘP CHUNG) ===== */
const menus = [
  {
    name: "Thống kê",
    path: "/admin/tong-quan",
    icon: baoCao,
    role: ["ADMIN"],
  },

  {
    name: "Quản lý đặt bàn",
    path: "/admin/dat-ban",
    icon: datBan,
    matchPaths: ["/admin/dat-ban"],
  },
  { name: "Check-in tại quầy", path: "/admin/check-in", icon: checkIn },
  {
    name: "Quản lý hóa đơn",
    icon: donHang,
    children: [
      {
        name: "Danh sách hóa đơn",
        path: "/admin/hoa-don",
        matchPaths: ["/admin/hoa-don"],
      },
      {
        name: "Hóa đơn treo",
        path: "/admin/hoa-don-treo",
        matchPaths: ["/admin/hoa-don-treo"],
      },
    ],
  },
  {
    name: "Quản lý bàn",
    path: "/admin/quan-ly-ban",
    icon: quanLyBan,
  },

  {
    name: "Quản lý set lẩu",
    icon: lauIcon,
    role: ["ADMIN"],
    children: [
      {
        name: "Set lẩu",
        path: "/admin/set-lau/hien-thi",
        matchPaths: ["/admin/set-lau"],
      },
      {
        name: "Nước lẩu",
        path: "/admin/thuc-don/nuoc-lau",
        matchPaths: ["/admin/thuc-don/nuoc-lau"],
      },
    ],
  },

  {
    name: "Thực đơn",
    icon: danhMucIcon,
    role: ["ADMIN"],
    children: [
      {
        name: "Danh mục",
        path: "/admin/danh-muc-cha",
        matchPaths: ["/admin/danh-muc-cha"],
      },
      {
        name: "Món ăn",
        path: "/admin/san-pham/hien-thi-all",
        matchPaths: ["/admin/san-pham"],
      },
    ],
  },

  {
    name: "Quản lý định lượng",
    role: ["ADMIN"],
    path: "/admin/dinh-luong/hien-thi",
    icon: iconThuocTinh,
  },

  {
    name: "Quản lý phiếu giảm giá",
    path: "/admin/phieu-giam-gia",
    icon: khuyenMai,
    role: ["ADMIN"],
    matchPaths: ["/admin/phieu-giam-gia"],
  },
  {
    name: "Quản lý đợt giảm giá",
    path: "/admin/dot-giam-gia",
    icon: dotGiamGia,
    role: ["ADMIN"],
  },

  { name: "Quản lý khách hàng", path: "/admin/khach-hang", icon: khachHang },

  {
    name: "Quản lý nhân viên",
    path: "/admin/nhan-vien",
    icon: nhanVien,
    role: ["ADMIN"],
  },
  {
    name: "Quản lý lịch làm việc",
    path: "/admin/lich-lam-viec",
    icon: iconLichLamViec,
    children: [
      {
        name: "Lịch làm việc",
        path: "/admin/lich-lam-viec",
        matchPaths: ["/admin/lich-lam-viec", "/admin/chi-tiet-lich-lam-viec"],
      },
      {
        name: "Lịch sử hoạt động",
        path: "/admin/giao-ca",
        matchPaths: ["/admin/giao-ca"],
      },
    ],
  },
  {
    name: "Quản lý tham số",
    path: "/admin/tham-so-dong",
    icon: thamSoDong,
    role: ["ADMIN"],
  },

  { name: "Nhắn tin", path: "/admin/nhan-tin", icon: nhanTin },
  { name: "Phản hồi", path: "/admin/phan-hoi", icon: phanHoi },
];

/* ===== QUẢN LÝ TRẠNG THÁI MỞ CỦA CÁC SUBMENU ===== */
const openMenus = ref({});

watch(
  () => [route.path, props.collapsed],
  () => {
    menus.forEach((menu) => {
      if (menu.children) {
        if (props.collapsed) {
          openMenus.value[menu.name] = false;
        } else {
          openMenus.value[menu.name] = menu.children.some((child) => {
            if (child.matchPaths) {
              return child.matchPaths.some((p) => route.path === p || route.path.startsWith(p + "/"));
            }
            return route.path === child.path || route.path.startsWith(child.path + "/");
          });
        }
      }
    });
  },
  { immediate: true },
);

const filteredMenus = menus
  .filter((item) => {
    if (!item.role) return true;
    return item.role.includes(role);
  })
  .map((item) => {
    if (!item.children) return item;

    return {
      ...item,
      children: item.children.filter((child) => {
        if (!child.role) return true;
        return child.role.includes(role);
      }),
    };
  });
const isActive = (child) => {
  if (child.matchPaths) {
    return child.matchPaths.some((p) => route.path === p || route.path.startsWith(p + "/"));
  }
  return route.path === child.path || route.path.startsWith(child.path + "/");
};

const isParentActive = (item) => {
  if (item.matchPaths) {
    return item.matchPaths.some((p) => route.path === p || route.path.startsWith(p + "/"));
  }

  if (!item.children) {
    return route.path === item.path || route.path.startsWith(item.path + "/");
  }

  return item.children.some((child) => isActive(child));
};
</script>

<template>
  <ul
    class="nav flex-column py-4 gap-1 flex-grow-1"
    :class="collapsed ? 'px-1' : 'px-3'"
  >
    <li v-for="item in filteredMenus" :key="item.name">
      <router-link
        v-if="!item.children"
        :to="item.path"
        class="nav-link d-flex align-items-center gap-3 py-2 rounded-3 menu-item"
        :class="{
          'menu-active': isParentActive(item),
          'justify-content-center px-2': collapsed,
          'px-3': !collapsed,
        }"
        @click="handleMenuClick(item)"
      >
        <img :src="item.icon" width="18" height="18" />
        <span v-if="!collapsed">{{ item.name }}</span>

        <span v-if="item.name === 'Nhắn tin' && hasUnreadChat" class="chat-red-dot"></span>
        <span v-if="item.name === 'Quản lý đặt bàn' && hasUnreadDatBan" class="chat-red-dot"></span>

        <span v-if="collapsed" class="tt">
          {{ item.name }}
        </span>
      </router-link>

      <div v-else class="submenu-container">
        <div
          class="nav-link d-flex align-items-center py-2 rounded-3 menu-item"
          :class="{
            'menu-active': isParentActive(item),
            'justify-content-center px-2': collapsed,
            'justify-content-between px-3': !collapsed,
          }"
          @click="toggleSubmenu(item.name)"
        >
          <div class="d-flex align-items-center gap-3">
            <img :src="item.icon" width="18" height="18" />
            <span v-if="!collapsed">{{ item.name }}</span>

            <span v-if="collapsed" class="tt">
              {{ item.name }}
            </span>
          </div>
          <img
            v-if="!collapsed"
            :src="openMenus[item.name] ? iconDown : iconRight"
            width="14"
            height="14"
          />
        </div>

        <ul
          v-show="openMenus[item.name]"
          class="submenu"
          :class="collapsed ? 'collapsed-modal py-2' : 'ps-4 mt-1'"
        >
          <li v-for="child in item.children" :key="child.path">
            <router-link
              :to="child.path"
              class="submenu-item"
              :class="{ 'submenu-active': isActive(child) }"
            >
              <img
                :src="isActive(child) ? iconCheck : iconDot"
                width="12"
                height="12"
              />
              <span>{{ child.name }}</span>
            </router-link>
          </li>
        </ul>
      </div>
    </li>
  </ul>
</template>

<style scoped>
/* ===== DOT ===== */
.chat-red-dot {
  width: 8px;
  height: 8px;
  background-color: #ed0909;
  border-radius: 50%;
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  animation: pulse-dot 1.5s infinite;
}

@keyframes pulse-dot {
  0% {
    transform: translateY(-50%) scale(1);
    opacity: 1;
  }
  50% {
    transform: translateY(-50%) scale(1.4);
    opacity: 0.6;
  }
  100% {
    transform: translateY(-50%) scale(1);
    opacity: 1;
  }
}

/* ===== STYLE CŨ CỦA BẠN GIỮ NGUYÊN ===== */
.menu-item {
  font-family: var(--font-family);
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-muted);
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.15s ease;
  text-decoration: none;
  position: relative;
  overflow: visible;
}

.menu-item:hover {
  background-color: rgb(245, 240, 240);
}

.menu-active {
  background-color: #f2f4f9 !important;
  color: #ff0000 !important;
  font-weight: 600;
}

/* ===== SUBMENU STYLE ===== */
.submenu-container {
  position: relative;
}

.collapsed-modal {
  position: fixed;
  left: 80px;
  margin-top: -30px;
  background-color: white;
  min-width: 180px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  border-radius: 8px;
  z-index: 9999;
  border: 1px solid #f0f0f0;
  padding: 8px;
}

.submenu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  font-family: var(--font-family);
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text);
  text-decoration: none;
  cursor: pointer;
  transition: background 0.15s ease;
}

.submenu-item:hover {
  background-color: rgb(245, 240, 240);
}

/* Thêm class active cho submenu */
.submenu-active {
  color: #ff0000 !important;
  font-weight: 500;
}

ul {
  list-style: none;
  padding-left: 0;
  margin: 0;
}

.submenu {
  overflow: hidden;
}

/* COLLAPSED MODE */
:deep(.collapsed) .menu-item {
  justify-content: center;
  padding-left: 10px !important;
  padding-right: 10px !important;
}

/* ===== RESPONSIVE ===== */
@media (max-width: 900px) {
  .submenu-item {
    padding: 6px 8px;
  }
}
.nav {
  overflow: visible !important;
}

/* ===== TOOLTIP SPAN CỨNG ===== */

.menu-item {
  position: relative;
}

.tt {
  display: none;
}

.menu-item {
  position: relative;
}

.menu-item:hover .tt {
  display: block;

  position: fixed;

  left: 95px;
  transform: translateY(-50%);

  background: #c56060;
  color: white;

  padding: 6px 12px;
  border-radius: 8px;

  white-space: nowrap;
  z-index: 99999;
}
</style>

<!-- Tooltip style không scoped để render được qua Teleport -->
<style>
.sidebar-tooltip {
  position: fixed;
  background: linear-gradient(135deg, #c56060, #b04040);
  color: #fff;
  padding: 7px 13px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  pointer-events: none;
  white-space: nowrap;
  z-index: 9999;
  box-shadow: 0 6px 20px rgba(197, 96, 96, 0.45);
  animation: tooltipFadeIn 0.12s ease;
}

@keyframes tooltipFadeIn {
  from {
    opacity: 0;
    transform: translateY(4px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
</style>
