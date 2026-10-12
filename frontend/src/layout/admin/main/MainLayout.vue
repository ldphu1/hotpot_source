<template>
  <div class="admin-layout-root d-flex bg-light">
    <!-- Overlay backdrop khi drawer mở trên màn nhỏ -->
    <transition name="backdrop-fade">
      <div
        v-if="isDrawerOpen"
        class="sidebar-backdrop"
        @click="isDrawerOpen = false"
      ></div>
    </transition>

    <Sidebar :isDrawerOpen="isDrawerOpen" @close-drawer="isDrawerOpen = false" />

    <div class="admin-main-col d-flex flex-column">
      <Header :isInShift="isInShift" @toggle-drawer="isDrawerOpen = !isDrawerOpen" @ket-thuc-ca="showKetThucCaModal = true" />

      <main class="admin-main-content flex-grow-1 overflow-auto">
        <div class="route-content">
          <router-view v-slot="{ Component, route }">
            <component :is="Component" :key="route.fullPath" />
          </router-view>
        </div>
      </main>
    </div>

    <!-- Modal Xác nhận vào ca -->
    <XacNhanVaoCaModal 
      v-model="showVaoCaModal" 
      :giaoCaData="giaoCaData"
      :lichLamViecData="lichLamViecData"
      :isThuNgan="isThuNgan"
      @confirmed="handleVaoCaConfirmed"
    />

    <!-- Modal Kết thúc ca -->
    <KetThucCaModal 
      v-model="showKetThucCaModal" 
      :isThuNgan="isThuNgan"
      @confirmed="handleKetThucCaConfirmed"
    />
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from "vue";
import { useRoute, useRouter } from "vue-router";
import Sidebar from "../sidebar/Sidebar.vue";
import Header from "../header/Header.vue";
import XacNhanVaoCaModal from "@/components/admin/LichLamViec/XacNhanVaoCaModal.vue";
import KetThucCaModal from "@/components/admin/LichLamViec/KetThucCaModal.vue";
import { giaoCaApi } from "@/api/admin/lichlamviec/giaoCaApi";
import { logoutAdmin } from "@/services/authService";

const isDrawerOpen = ref(false);
const route = useRoute();
const router = useRouter();
const showVaoCaModal = ref(false);
const showKetThucCaModal = ref(false);
const giaoCaData = ref(null);
const lichLamViecData = ref(null);
const isInShift = ref(false);
const isThuNgan = ref(false);

// Tự động đóng drawer khi chuyển trang trên mobile
watch(
  () => route.path,
  () => {
    isDrawerOpen.value = false;
  }
);

// Tự động đóng drawer/backdrop khi phóng to màn hình về dạng desktop
const handleResize = () => {
  if (window.innerWidth > 900) {
    isDrawerOpen.value = false;
  }
};

const checkVaoCa = async () => {
  try {
    const res = await giaoCaApi.kiemTraVaoCa();
    const data = res.data;
    isThuNgan.value = !!data.isThuNgan;
    
    if (data.trangThai === "CAN_XAC_NHAN" || data.trangThai === "VAO_CA_MOI") {
      giaoCaData.value = data.giaoCa || {};
      if (giaoCaData.value) {
        giaoCaData.value.tongTienMatCaTruoc = data.tongTienMatCaTruoc;
        giaoCaData.value.tongChuyenKhoanCaTruoc = data.tongChuyenKhoanCaTruoc;
      }
      lichLamViecData.value = data.lichLamViec || null;
      showVaoCaModal.value = true;
      isInShift.value = false;
    } else if (data.trangThai === "DA_XAC_NHAN") {
      isInShift.value = true;
    } else {
      isInShift.value = false;
    }
  } catch (error) {
    console.error("Lỗi kiểm tra vào ca:", error);
  }
};

const handleVaoCaConfirmed = () => {
  showVaoCaModal.value = false;
  isInShift.value = true;
};

const handleKetThucCaConfirmed = async () => {
  showKetThucCaModal.value = false;
  isInShift.value = false;
  
  const refreshToken = localStorage.getItem("refreshToken");
  try {
    if (refreshToken) {
      await logoutAdmin(refreshToken);
    }
  } catch (e) {
    console.error("Lỗi đăng xuất:", e);
  }

  localStorage.removeItem("token");
  localStorage.removeItem("refreshToken");
  localStorage.removeItem("role");
  localStorage.removeItem("name");
  localStorage.removeItem("avatar");

  router.push("/");
};

onMounted(() => {
  window.addEventListener("resize", handleResize);
  checkVaoCa();
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", handleResize);
});
</script>

<style scoped>
.admin-layout-root {
  height: 100vh;
  overflow: hidden;
  position: relative;
}

.admin-main-col {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.admin-main-content {
  padding: 24px;
  flex: 1;
  min-height: 0;
}

/* Backdrop overlay */
.sidebar-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(2px);
  z-index: 1500;
}

.backdrop-fade-enter-active,
.backdrop-fade-leave-active {
  transition: opacity 0.3s ease;
}
.backdrop-fade-enter-from,
.backdrop-fade-leave-to {
  opacity: 0;
}

/* ===== RESPONSIVE ===== */
@media (max-width: 900px) {
  .admin-main-content {
    padding: 12px;
  }
}

@media (max-width: 600px) {
  .admin-main-content {
    padding: 8px;
  }
}
</style>
