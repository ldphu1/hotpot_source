<script setup>
import { onMounted, ref } from "vue";
import SoDoBan from "./SoDoBan.vue";
import DanhSachBan from "./DanhSachBan.vue";
import { getAllkhuvuc } from "@/api/admin/khuvuc/khuVucApi";
import iconBan from "@/assets/icons/ban.png";

const tabDangChon = ref(1);
const tangDangChon = ref(null);
const reloadKey = ref(0);
const danhSachKhuVuc = ref([]);

onMounted(async () => {
  try {
    const res = await getAllkhuvuc();
    danhSachKhuVuc.value = res.data;
    console.log(res.data);
  } catch (e) {
    console.error("Lỗi load khu vực", e);
  }
});
</script>

<template>
  <div class="box">
    <div class="box-header">
      <h2 class="main-title">
        <span class="icon-wrapper">
          <img :src="iconBan" alt="Quản lý bàn" />
        </span>
        Quản Lý Bàn
      </h2>
      <p>Theo dõi sơ đồ bàn, trạng thái và danh sách bàn</p>
    </div>
  </div>

  <ul class="nav tab-underline mb-3">
    <li class="nav-item">
      <button
        class="nav-link"
        :class="{ active: tabDangChon === 1 }"
        @click="tabDangChon = 1"
      >
        <i class="bi bi-grid-3x3-gap me-1"></i>
        Sơ đồ bàn
      </button>
    </li>
    <li class="nav-item">
      <button
        class="nav-link"
        :class="{ active: tabDangChon === 2 }"
        @click="tabDangChon = 2"
      >
        <i class="bi bi-table me-1"></i>
        Danh sách bàn
      </button>
    </li>
  </ul>

  <div class="content-body">
    <SoDoBan
      v-if="tabDangChon === 1"
      :key="reloadKey"
    />
    <DanhSachBan v-if="tabDangChon === 2" />
  </div>
</template>

<style scoped>
/* ===== CỤC TRẮNG 1: CHỈ CHỨA HEADER ===== */
.box {
  background: #ffffff;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  padding: 18px 22px;
  margin-bottom: 20px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}
.box-header {
  margin-bottom: 0;
}

/* ===== HEADER FLEX ===== */
.header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-flex.no-border {
  border: none;
}
.header-section {
  width: 100%;
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
.icon-wrapper img {
  width: 28px;
  height: 28px;
  object-fit: contain;
}
.icon-title {
  width: 28px;
  height: 28px;
  object-fit: contain;
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
.sub-title {
  color: #666;
  margin-top: 4px;
  margin-left: 60px;
  font-size: 14px;
}
.box-header p {
  margin: 10px 0 0;
  color: var(--color-text-muted);
}

/* ===== TAB UNDERLINE ===== */
.tab-underline {
  border-bottom: 1px solid #dee2e6;
  margin-bottom: 16px !important;
  /* Reset lề cho tab bám sát viền trái */
  padding-left: 0;
  margin-left: 0;
}
.tab-underline .nav-link {
  border: none;
  background: none;
  color: #6c757d;
  font-weight: 500;
  padding: 10px 16px;
  position: relative;
}
.tab-underline .nav-link:hover {
  color: #d73737;
}
.tab-underline .nav-link.active {
  color: #d73737;
  font-weight: 600;
}
.tab-underline .nav-link.active::after {
  content: "";
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 3px;
  background-color: #d73737;
  border-radius: 3px 3px 0 0;
}

/* ===== CỤC TRẮNG 2: NỘI DUNG ===== */
.content-body {
  width: 100%;
  background: #ffffff;
  border-radius: 10px;
  border: 1px solid #eee;
  padding: 20px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
  box-sizing: border-box;

  /* Lệnh khóa form: Cố định giới hạn chiều cao, tự cuộn dọc bên trong */
  height: calc(100vh - 250px);
  overflow-y: auto;
}
</style>
