<script setup>
import { ref } from "vue";
import LichLamViec from "./LichLamViec.vue";
import CaLam from "./CaLam.vue";
import iconCalendar from "@/assets/icons/calendar.png";

const activeTab = ref("lich-lam-viec");
</script>

<template>
  <div class="quan-ly-lich-lam-viec d-flex flex-column h-100 fade-in">
    <!-- TOP HEADER -->
    <div class="box mb-3">
      <div class="box-header">
        <h2 class="main-title">
          <span class="icon-wrapper">
            <img :src="iconCalendar" alt="Lịch làm việc" />
          </span>
          Quản Lý Lịch Làm Việc
        </h2>
        <p class="sub-title">Quản lý ca làm việc và phân công nhân viên</p>
      </div>

      <!-- TABS -->
      <ul class="nav nav-tabs custom-tabs mt-3 px-3">
        <li class="nav-item">
          <a
            class="nav-link cursor-pointer"
            :class="{ active: activeTab === 'lich-lam-viec' }"
            @click="activeTab = 'lich-lam-viec'"
          >
            <i class="ri-calendar-event-line me-1"></i> Lịch làm việc
          </a>
        </li>
        <li class="nav-item">
          <a
            class="nav-link cursor-pointer"
            :class="{ active: activeTab === 'ca-lam' }"
            @click="activeTab = 'ca-lam'"
          >
            <i class="ri-time-line me-1"></i> Ca làm
          </a>
        </li>
      </ul>
    </div>

    <!-- TAB CONTENT -->
    <div class="flex-grow-1 overflow-hidden d-flex flex-column">
      <LichLamViec v-if="activeTab === 'lich-lam-viec'" />
      <CaLam v-if="activeTab === 'ca-lam'" />
    </div>
  </div>
</template>

<style scoped>
.btn {
  border-radius: 16px !important;
}
@import url("https://cdn.jsdelivr.net/npm/remixicon@4.2.0/fonts/remixicon.css");

.quan-ly-lich-lam-viec {
}

.box {
  background: #fff;
  border-radius: 8px;
  padding: 0;
  margin-bottom: 20px;
  border: 1px solid #eee;
  overflow: hidden;
}

.box-header {
  padding: 20px 24px 0 24px;
}

.main-title {
  display: flex;
  align-items: center;
  font-size: 30px;
  font-weight: 600;
  gap: 12px;
  letter-spacing: 1px;
  color: #1a1a1a;
  margin: 0;
}

.icon-wrapper img {
  width: 32px;
  height: 32px;
  object-fit: contain;
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

.sub-title {
  color: #666;
  margin-top: 4px;
  font-size: 16px;
}

/* Custom Tabs Styling */
.custom-tabs {
  border-bottom: 1px solid #e2e8f0;
  margin-top: 10px;
}

.custom-tabs .nav-item {
  margin-bottom: -1px;
}

.custom-tabs .nav-link {
  color: #64748b;
  border: none;
  border-bottom: 3px solid transparent;
  padding: 12px 20px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.custom-tabs .nav-link:hover {
  color: #0f172a;
  background-color: #f8fafc;
}

.custom-tabs .nav-link.active {
  color: #dc3545;
  border-bottom: 3px solid #dc3545;
  background-color: transparent;
}

.cursor-pointer {
  cursor: pointer;
}

.fade-in {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
