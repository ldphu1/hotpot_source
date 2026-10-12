<script setup>
import { ref } from "vue";
import DanhSach from "./DanhSach.vue";
import LichDat from "./LichDat.vue";

/* ICON */

import iconDanhSach from "@/assets/icons/danh-sach.png";
import iconCalendar from "@/assets/icons/calendar.png";
import iconDatBan from "@/assets/icons/qldatban.png";

/* TAB */
const currentTab = ref("danh-sach");
</script>

<template>
  <div class="dat-ban-page">
    <!-- BOX HEADER -->
    <div class="box-header">
      <!-- HEADER -->
      <div class="header-flex no-border">
        <div class="header-section">
          <div class="title-group">
            <div class="icon-wrapper">
              <img :src="iconDatBan" alt="Đặt bàn" class="icon-title" />
            </div>

            <div>
              <h2 class="main-title">Quản Lý Đặt Bàn</h2>
              <p class="sub-title">
                Theo dõi lịch đặt bàn, xác nhận khách và tạo đơn nhanh chóng
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- TAB -->
      <div class="tab-container">
        <div class="tab-wrapper">
          <div
            class="tab-item"
            :class="{ active: currentTab === 'danh-sach' }"
            @click="currentTab = 'danh-sach'"
          >
            <img :src="iconDanhSach" class="tab-icon" />
            Danh sách
          </div>

          <div
            class="tab-item"
            :class="{ active: currentTab === 'lich-dat' }"
            @click="currentTab = 'lich-dat'"
          >
            <img :src="iconCalendar" class="tab-icon" />
            Lịch đặt
          </div>
        </div>
      </div>
    </div>

    <!-- CONTENT -->
    <div class="content-body">
      <keep-alive>
        <component :is="currentTab === 'danh-sach' ? DanhSach : LichDat" />
      </keep-alive>
    </div>
  </div>
</template>

<style scoped>
.header-flex.no-border {
  border: none;
}

.icon-title {
  width: 24px;
  height: 24px;
  margin-right: 8px;
  vertical-align: middle;
}
.tab-container {
  width: 100%;
  border-bottom: 1px solid #f0f0f0;
  margin-top: 10px;
}
.box-header {
  background: white;
  border-radius: 10px;
  border: 1px solid #eee;
  padding: 18px 22px;
  margin-bottom: 20px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}
/* ===== PAGE WRAPPER (FIX LỆCH Ở ĐÂY) ===== */
.dat-ban-page {
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
  padding: 0;
  box-sizing: border-box;
  font-family: var(--font-family);
}

/* ===== HEADER ===== */
.header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.header-section {
  margin-bottom: 24px;
  padding-bottom: 18px;
  border-bottom: 1px solid #f1f3f5;
}

.title-group {
  display: flex;
  align-items: center;
  gap: 16px;
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

.icon-title {
  width: 28px;
  height: 28px;
  object-fit: contain;
}

.main-title {
  margin: 0;

  font-size: 32px;
  font-weight: 500;
  line-height: 1.2;
  letter-spacing: 1px;

  color: #611c1c;
}

.sub-title {
  margin-top: 6px;

  font-size: 14px;
  line-height: 1.6;
  color: #6b7280;
}
/* ===== BUTTON ===== */
.header-actions {
  display: flex;
  gap: 12px;
}

.btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border-radius: var(--radius-md);
  border: none;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
}

.btn-primary {
  background: var(--color-primary);
  color: #000;
}

.btn-excel {
  background: #7dddf0;
  color: #000;
}

.btn-icon {
  width: 18px;
  height: 18px;
}

/* ===== TAB ===== */
.tab-container {
  width: 100%;
  border-bottom: 2px solid #f0f0f0;
  margin-bottom: 20px;
}

.tab-wrapper {
  display: flex;
  gap: 32px;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 2px;
  font-size: 14px;
  font-weight: 600;
  color: #888;
  cursor: pointer;
  position: relative;
  box-sizing: border-box;
}

.tab-item.active {
  color: var(--color-primary);
}

/* Gạch chân tab */
.tab-item.active::after {
  content: "";
  position: absolute;
  left: 0;
  bottom: -2px;
  width: 100%;
  height: 2px;
  background: var(--color-primary);
}

.tab-icon {
  width: 18px;
  opacity: 0.6;
}

.tab-item.active .tab-icon {
  opacity: 1;
}

/* ===== CONTENT ===== */
.content-body {
  width: 100%;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

/* ===== RESPONSIVE ===== */
@media (max-width: 768px) {
  .dat-ban-page {
    height: auto !important;
  }

  .box-header {
    padding: 14px 14px !important;
  }

  .title-group {
    gap: 10px !important;
  }

  .icon-wrapper {
    width: 44px !important;
    height: 44px !important;
    flex-shrink: 0 !important;
  }

  .main-title {
    font-size: 20px !important;
    letter-spacing: 0.5px !important;
  }

  .sub-title {
    font-size: 12px !important;
    line-height: 1.4 !important;
  }

  .tab-wrapper {
    gap: 16px !important;
    overflow-x: auto !important;
    flex-wrap: nowrap !important;
  }

  .tab-item {
    font-size: 13px !important;
    white-space: nowrap !important;
    padding: 8px 2px !important;
  }
}

@media (max-width: 480px) {
  .box-header {
    padding: 10px !important;
  }

  .icon-wrapper {
    width: 36px !important;
    height: 36px !important;
  }

  .main-title {
    font-size: 16px !important;
  }

  .sub-title {
    display: none !important;
  }
}
</style>
