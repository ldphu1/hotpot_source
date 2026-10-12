<script setup>
import { ref, computed, watch } from "vue";
const props = defineProps({
  items: {
    type: Array,
    default: () => [],
  },
  giaTriGiam: {
    type: Number,
    default: 0,
  },
});

const emit = defineEmits(["remove"]);

/* TÍNH GIÁ SAU GIẢM */
const tinhGia = (gia) => {
  const base = Number(gia) || 0;
  const percent = Number(props.giaTriGiam) || 0;

  const giam = Math.round((base * percent) / 100);

  return Math.max(base - giam, 0);
};

/* FORMAT */
const formatPrice = (price) => {
  return new Intl.NumberFormat("vi-VN").format(price || 0) + " đ";
};

/* LẤY ẢNH */
const getImage = (img) => {
  if (!img) return "/no-image.png";
  if (img.startsWith("http")) return img;
  return `http://localhost:8080/uploads/${img}`;
};

// Phân trang sản phẩm đã chọn
const page = ref(1);
const pageSize = 5;

const pagedItems = computed(() => {
  const start = (page.value - 1) * pageSize;
  return props.items.slice(start, start + pageSize);
});

const totalPages = computed(() => Math.ceil(props.items.length / pageSize));

const pageList = computed(() => {
  const total = totalPages.value;
  if (total <= 5) return Array.from({ length: total }, (_, i) => i + 1);
  const pages = [1, 2, 3];
  if (total > 4) pages.push("…");
  pages.push(total);
  return pages;
});

// Reset về trang 1 nếu trang hiện tại vượt quá tổng số trang (vd: sau khi xóa item)
watch(
  () => props.items.length,
  () => {
    if (page.value > totalPages.value) {
      page.value = Math.max(totalPages.value, 1);
    }
  },
);
</script>

<template>
  <div class="box">
    <h3 class="box-title">
      <i class="ri-box-3-line" style="margin-right: 8px"></i>
      Sản phẩm áp dụng đợt giảm giá
    </h3>

    <div class="table-wrapper custom-scrollbar">
      <table class="table-sanpham">
        <thead>
          <tr>
            <th width="60">STT</th>
            <th width="110">Hình ảnh</th>
            <th class="left">Tên sản phẩm</th>
            <th>Giá gốc</th>
            <th>Giá ưu đãi</th>
            <th>Mức giảm</th>
            <th width="100">Thao tác</th>
          </tr>
        </thead>

        <tbody>
          <tr
            v-for="(item, index) in pagedItems"
            :key="item.id"
            class="product-row"
          >
            <td class="index-col">{{ index + 1 }}</td>

            <td>
              <div class="img-wrapper">
                <img
                  :src="getImage(item.hinhAnh)"
                  @error="(e) => (e.target.src = '/no-image.png')"
                  class="img-product"
                />
              </div>
            </td>

            <td class="left name-col">
              {{ item.tenSanPham }}
            </td>

            <td class="old-price-cell">
              {{ formatPrice(item.gia) }}
            </td>

            <td class="new-price-cell">
              {{ formatPrice(tinhGia(item.gia)) }}
            </td>

            <td>
              <span class="discount-badge">
                -{{ props.giaTriGiam || 0 }}%
              </span>
            </td>

            <td>
              <button
                class="btn-delete"
                @click="emit('remove', item.id)"
                title="Gỡ bỏ"
              >
                <i class="ri-delete-bin-6-line"></i>
              </button>
            </td>
          </tr>

          <tr v-if="props.items.length === 0">
            <td colspan="7" class="empty-state">
              <div class="empty-content">
                <i class="bi bi-egg-fried empty-icon"></i>
                <p>Chưa có sản phẩm nào được chọn trong danh sách</p>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="totalPages > 1" class="pagination mt-2">
        <button :disabled="page === 1" @click="page--" class="pg-btn pg-arrow">
          <i class="ri-arrow-left-s-line"></i>
        </button>
        <template v-for="p in pageList" :key="p">
          <button v-if="p === '…'" class="pg-btn pg-ellipsis" disabled>
            ···
          </button>
          <button
            v-else
            class="pg-btn"
            :class="{ active: p === page }"
            @click="page = p"
          >
            {{ p }}
          </button>
        </template>
        <button
          :disabled="page === totalPages"
          @click="page++"
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
  border: 1px solid #eee;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
  font-family: var(--font-family);
}

.box-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin-top: 0;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

/* ===== TABLE STYLING ===== */
.table-wrapper {
  max-height: 400px;
  overflow-y: auto;
  border-radius: 7px;
  border: 1px solid #eee; /* Bo viền ngoài của bảng */
}

.table-sanpham {
  width: 100%;
  border-collapse: collapse;
}

.table-sanpham th {
  background: #e9f2fb; /* Khớp với màu table bên DotGiamGia.vue */
  padding: 14px 12px;
  text-align: center;
  font-weight: 600;
  color: #333;
  font-size: 14px;
  position: sticky;
  top: 0;
  z-index: 1;
  white-space: nowrap;
  line-height: 1.4;
}

.table-sanpham th.left {
  text-align: left;
}

.table-sanpham td {
  padding: 8px;
  border-bottom: 1px solid #eee;
  text-align: center;
  vertical-align: middle;
}

.table-sanpham td.left {
  text-align: left;
}

.product-row:hover {
  background-color: #f8f9fa;
}

/* ===== COLUMN STYLES ===== */
.index-col {
  color: #888;
  font-weight: 500;
  font-size: 14px;
}

.name-col {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

/* Ảnh sản phẩm */
.img-wrapper {
  width: 44px;
  height: 44px;
  border-radius: 7px;
  overflow: hidden;
  border: 1px solid #eee;
  margin: 0 auto;
}

.img-product {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-row:hover .img-product {
  transform: scale(1.1);
}

/* ===== PRICE STYLES ===== */
.old-price-cell {
  text-decoration: line-through;
  color: #999;
  font-size: 13px;
}

.new-price-cell {
  color: #d70018; /* Màu đỏ cho giá để làm nổi bật */
  font-weight: 600;
  font-size: 14px;
}

/* ===== BADGE GIẢM GIÁ ===== */
.discount-badge {
  background: #fee2e2;
  color: #dc2626;
  padding: 4px 10px;
  border-radius: 7px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block;
}

/* ===== NÚT XÓA ===== */
.btn-delete {
  background: #fff0f0; /* Nền đỏ nhạt */
  color: #dc2626; /* Chữ đỏ */
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 7px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  font-size: 16px;
}

.btn-delete:hover {
  background: #dc2626;
  color: white;
}

/* ===== EMPTY STATE ===== */
.empty-state {
  padding: 60px 0;
  text-align: center;
  color: #888;
}

.empty-icon {
  font-size: 36px;
  margin-bottom: 8px;
  display: block;
}

.empty-content p {
  margin: 0;
  font-size: 14px;
}

/* ===== CUSTOM SCROLLBAR ===== */
.custom-scrollbar::-webkit-scrollbar {
  width: 5px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: #f1f1f1;
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
  margin-top: 12px;
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
