<template>
  <div class="route-content">
    <RouterView />
  </div>

  <!-- Global Modal & Toast Container -->
  <GlobalModalContainer />

  <!-- Global Loading Overlay -->
  <transition name="fade">
    <div v-if="isLoading" class="global-loader-overlay">
      <div class="loader-content">
        <div class="sliding-puzzle">
          <div class="puzzle-piece piece-1"></div>
          <div class="puzzle-piece piece-2"></div>
          <div class="puzzle-piece piece-3"></div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { RouterView, useRoute } from "vue-router";
import { ref, onMounted, onUnmounted, watch } from "vue";
import router from "@/router";
import { useUtils } from "./utils/helper";
import { connectWebSocketForceLogout, disconnectWebSocket } from "./utils/websocket";
import GlobalModalContainer from "@/components/common/GlobalModalContainer.vue";

const isLoading = ref(true);
const route = useRoute();
const { notify } = useUtils();

const handleForceLogout = () => {
  localStorage.removeItem("token");
  localStorage.removeItem("refreshToken");
  localStorage.removeItem("idNhanVien");
  localStorage.removeItem("role");
  localStorage.removeItem("name");
  localStorage.removeItem("avatar");
  localStorage.removeItem("permissions");
  notify.error("Tài khoản của bạn đã bị khóa hoặc chuyển sang trạng thái nghỉ việc!");
  router.push("/admin/login");
};

const checkAndConnectForceLogout = () => {
  const role = localStorage.getItem("role");
  const idNhanVien = localStorage.getItem("idNhanVien");
  if (role && role !== "CUSTOMER" && idNhanVien) {
    connectWebSocketForceLogout(idNhanVien, handleForceLogout);
  }
};

onMounted(() => {
  router.isReady().then(() => {
    setTimeout(() => {
      isLoading.value = false;
    }, 600);
  });

  checkAndConnectForceLogout();
});

watch(
  () => route.path,
  () => {
    checkAndConnectForceLogout();
  }
);

onUnmounted(() => {
  disconnectWebSocket(null, null, handleForceLogout);
});
</script>

<style scoped>
/* Loader fade transition */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
