<script setup>
import { ref, onMounted, watch } from "vue";
import {
  getAllSanPham,
  getAllSanPhamDgg,
} from "@/api/admin/sanpham/sanPhamApi";
import { computed } from "vue";

const props = defineProps({ selected: Array });
const emit = defineEmits(["select"]);

const setLauList = ref([]);
const monAnList = ref([]);
const allSetLau = ref([]);
const allMonAn = ref([]);

const keywordSet = ref("");
const keywordMon = ref("");

const loading = ref(false);
const selectAllSet = ref(false);
const selectAllMon = ref(false);

let timeoutSet = null;
let timeoutMon = null;

/* LOAD DATA */
const loadData = async () => {
  loading.value = true;
  try {
    const res = await getAllSanPhamDgg();
    console.log(res.data);
    const data = res.data;

    allSetLau.value = data.filter((i) => i.laSet);
    allMonAn.value = data.filter((i) => !i.laSet);

    setLauList.value = allSetLau.value;
    monAnList.value = allMonAn.value;
  } catch (e) {
    console.log("Lỗi load", e);
  } finally {
    loading.value = false;
  }
};

/* SEARCH SET */
watch(keywordSet, (val) => {
  clearTimeout(timeoutSet);
  timeoutSet = setTimeout(() => {
    const kw = (val || "").trim().replace(/\s+/g, " ").toLowerCase();
    if (!kw) setLauList.value = allSetLau.value;
    else {
      setLauList.value = allSetLau.value.filter((i) =>
        (i.tenSanPham || "")
          .trim()
          .replace(/\s+/g, " ")
          .toLowerCase()
          .includes(kw),
      );
    }
  }, 300);
});

/* SEARCH MÓN */
watch(keywordMon, (val) => {
  clearTimeout(timeoutMon);
  timeoutMon = setTimeout(() => {
    const kw = (val || "").trim().replace(/\s+/g, " ").toLowerCase();
    if (!kw) monAnList.value = allMonAn.value;
    else {
      monAnList.value = allMonAn.value.filter((i) =>
        (i.tenSanPham || "")
          .trim()
          .replace(/\s+/g, " ")
          .toLowerCase()
          .includes(kw),
      );
    }
  }, 300);
});
/* INIT */
onMounted(loadData);

/* CHECK */
const isChecked = (item) => {
  return props.selected.some((p) => p.id === item.id);
};

/* TOGGLE */
const toggle = (item) => {
  emit("select", item);
};

/* SELECT ALL SET */
const toggleAllSet = () => {
  selectAllSet.value = !selectAllSet.value;

  setLauList.value.forEach((item) => {
    if (selectAllSet.value && !isChecked(item)) emit("select", item);
    if (!selectAllSet.value && isChecked(item)) emit("select", item);
  });
};

/* SELECT ALL MÓN */
const toggleAllMon = () => {
  selectAllMon.value = !selectAllMon.value;

  monAnList.value.forEach((item) => {
    if (selectAllMon.value && !isChecked(item)) emit("select", item);
    if (!selectAllMon.value && isChecked(item)) emit("select", item);
  });
};

/* AUTO CHECK */
watch(setLauList, () => {
  selectAllSet.value =
    setLauList.value.length > 0 && setLauList.value.every((i) => isChecked(i));
});

watch(monAnList, () => {
  selectAllMon.value =
    monAnList.value.length > 0 && monAnList.value.every((i) => isChecked(i));
});

/* FORMAT */
const formatPrice = (price) =>
  new Intl.NumberFormat("vi-VN").format(price) + " đ";

/* IMAGE */
const getImage = (img) => {
  if (!img) return "/no-image.png";
  if (img.startsWith("http")) return img;
  return `http://localhost:8080/uploads/${img}`;
};

// Phân trang set lẩu
const setPage = ref(1);
const setPageSize = 4;

const pagedSetLau = computed(() => {
  const start = (setPage.value - 1) * setPageSize;
  return setLauList.value.slice(start, start + setPageSize);
});

const setTotalPages = computed(() =>
  Math.ceil(setLauList.value.length / setPageSize),
);

const setPageList = computed(() => {
  const total = setTotalPages.value;
  if (total <= 5) return Array.from({ length: total }, (_, i) => i + 1);
  const pages = [1, 2, 3];
  if (total > 4) pages.push("…");
  pages.push(total);
  return pages;
});

// Phân trang món lẻ
const monPage = ref(1);
const monPageSize = 4;

const pagedMonAn = computed(() => {
  const start = (monPage.value - 1) * monPageSize;
  return monAnList.value.slice(start, start + monPageSize);
});

const monTotalPages = computed(() =>
  Math.ceil(monAnList.value.length / monPageSize),
);

const monPageList = computed(() => {
  const total = monTotalPages.value;
  if (total <= 5) return Array.from({ length: total }, (_, i) => i + 1);
  const pages = [1, 2, 3];
  if (total > 4) pages.push("…");
  pages.push(total);
  return pages;
});

// Reset trang khi search hoặc khi danh sách lọc thay đổi
watch(keywordSet, () => {
  setPage.value = 1;
});
watch(keywordMon, () => {
  monPage.value = 1;
});
</script>

<template>
  <div class="box">
    <h3 class="box-title">
      <i class="ri-checkbox-multiple-line" style="margin-right: 8px"></i>
      Sản phẩm áp dụng
    </h3>

    <div class="section">
      <div class="section-header">
        <span class="category-name">Set lẩu</span>
        <label class="select-all-label">
          <input
            type="checkbox"
            :checked="selectAllSet"
            @change="toggleAllSet"
          />
          Chọn tất cả
        </label>
      </div>

      <div class="search-wrapper">
        <i class="ri-search-line search-icon"></i>
        <input
          v-model="keywordSet"
          placeholder="Tìm kiếm set lẩu..."
          class="search-input"
        />
      </div>

      <div class="list custom-scrollbar">
        <div
          v-for="item in pagedSetLau"
          :key="item.id"
          class="item-row"
          :class="{ active: isChecked(item) }"
          @click="toggle(item)"
        >
          <div class="item-info">
            <input
              type="checkbox"
              class="standard-checkbox"
              :checked="isChecked(item)"
              @change.stop="toggle(item)"
            />
            <div class="img-container">
              <img :src="getImage(item.hinhAnh)" class="img" />
            </div>
            <span class="product-name">{{ item.tenSanPham }}</span>
          </div>

          <div class="price-tag">{{ formatPrice(item.gia) }}</div>
        </div>
      </div>
      <div v-if="setTotalPages > 1" class="pagination mt-2">
        <button
          :disabled="setPage === 1"
          @click="setPage--"
          class="pg-btn pg-arrow"
        >
          <i class="ri-arrow-left-s-line"></i>
        </button>
        <template v-for="p in setPageList" :key="p">
          <button v-if="p === '…'" class="pg-btn pg-ellipsis" disabled>
            ···
          </button>
          <button
            v-else
            class="pg-btn"
            :class="{ active: p === setPage }"
            @click="setPage = p"
          >
            {{ p }}
          </button>
        </template>
        <button
          :disabled="setPage === setTotalPages"
          @click="setPage++"
          class="pg-btn pg-arrow"
        >
          <i class="ri-arrow-right-s-line"></i>
        </button>
      </div>
    </div>

    <div class="section">
      <div class="section-header">
        <span class="category-name">Món ăn lẻ đi kèm</span>
        <label class="select-all-label">
          <input
            type="checkbox"
            :checked="selectAllMon"
            @change="toggleAllMon"
          />
          Chọn tất cả
        </label>
      </div>

      <div class="search-wrapper">
        <i class="ri-search-line search-icon"></i>
        <input
          v-model="keywordMon"
          placeholder="Tìm kiếm món ăn lẻ..."
          class="search-input"
        />
      </div>

      <div class="list custom-scrollbar">
        <div
          v-for="item in pagedMonAn"
          :key="item.id"
          class="item-row"
          :class="{ active: isChecked(item) }"
          @click="toggle(item)"
        >
          <div class="item-info">
            <input
              type="checkbox"
              class="standard-checkbox"
              :checked="isChecked(item)"
              @change.stop="toggle(item)"
            />
            <div class="img-container">
              <img :src="getImage(item.hinhAnh)" class="img" />
            </div>
            <span class="product-name">{{ item.tenSanPham }}</span>
          </div>

          <div class="price-tag">{{ formatPrice(item.gia) }}</div>
        </div>
      </div>
      <div v-if="monTotalPages > 1" class="pagination mt-2">
        <button
          :disabled="monPage === 1"
          @click="monPage--"
          class="pg-btn pg-arrow"
        >
          <i class="ri-arrow-left-s-line"></i>
        </button>
        <template v-for="p in monPageList" :key="p">
          <button v-if="p === '…'" class="pg-btn pg-ellipsis" disabled>
            ···
          </button>
          <button
            v-else
            class="pg-btn"
            :class="{ active: p === monPage }"
            @click="monPage = p"
          >
            {{ p }}
          </button>
        </template>
        <button
          :disabled="monPage === monTotalPages"
          @click="monPage++"
          class="pg-btn pg-arrow"
        >
          <i class="ri-arrow-right-s-line"></i>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ===== SỬ DỤNG FONT MẶC ĐỊNH CỦA DỰ ÁN GIỐNG DATBAN.VUE ===== */
.box {
  background: #fff;
  border-radius: 7px;
  padding: 24px;
  height: 100%;
  border: 1px solid #eee;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
  font-family: var(--font-family);
}

.box-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin-top: 0;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
}

.section {
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.category-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.select-all-label {
  font-size: 13px;
  color: #d70018; /* Xanh tiêu chuẩn */
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}

/* ===== THANH TÌM KIẾM (Đồng bộ với input bên form) ===== */
.search-wrapper {
  position: relative;
  margin-bottom: 15px;
}

.search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #888;
  font-size: 16px;
}

.search-input {
  width: 100%;
  padding: 10px 14px 10px 38px; /* Dịch text sang phải nhường chỗ cho icon */
  border: 1px solid #e2e2e2;
  border-radius: 7px;
  font-size: 14px;
  background-color: #fff;
  transition: all 0.2s;
  font-family: var(--font-family);
  color: #1a1a1a;
}

.search-input:focus {
  outline: none;
  border-color: #d70018;
  box-shadow: 0 0 0 3px rgba(59, 108, 255, 0.1);
}

/* ===== DANH SÁCH SẢN PHẨM ===== */
.list {
  max-height: 280px;
  overflow-y: auto;
  padding-right: 5px;
}

.item-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  margin-bottom: 8px;
  border-radius: 7px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s ease;
}

.item-row:hover {
  background: #f8f9fa;
  border-color: #d70018;
}

.item-row.active {
  background: #f0f4ff;
  border-color: #d70018;
}

.item-info {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* Ảnh sản phẩm */
.img-container {
  width: 44px;
  height: 44px;
  border-radius: 7px;
  overflow: hidden;
  border: 1px solid #eee;
}

.img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.price-tag {
  color: #d70018; /* Giữ màu đỏ cho giá tiền để nổi bật */
  font-weight: 600;
  font-size: 14px;
}

/* ===== CHECKBOX TIÊU CHUẨN ===== */
input[type="checkbox"] {
  accent-color: #d70018; /* Màu xanh thay vì màu đỏ/vàng cũ */
  width: 16px;
  height: 16px;
  cursor: pointer;
}

/* ===== CUSTOM SCROLLBAR ===== */
.custom-scrollbar::-webkit-scrollbar {
  width: 5px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 7px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 7px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  user-select: none;
}
.pg-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
  padding: 0 6px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  background: #fff;
  color: #333;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}
.pg-btn:hover:not(:disabled):not(.active) {
  border-color: #d70018;
  color: #d70018;
  background: #fff1f0;
}
.pg-btn.active {
  border: 1.5px solid #d70018;
  color: #d70018;
  font-weight: 600;
}
.pg-btn:disabled {
  border-color: #f0f0f0;
  background: #f5f5f5;
  color: #bfbfbf;
  cursor: not-allowed;
}
.pg-ellipsis {
  border: none;
  background: transparent;
  cursor: default;
  color: #aaa;
  min-width: 20px;
}
.pg-arrow i {
  font-size: 16px;
}
</style>
