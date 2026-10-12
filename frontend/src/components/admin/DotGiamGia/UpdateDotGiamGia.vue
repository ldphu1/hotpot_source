<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRoute, useRouter, onBeforeRouteLeave } from "vue-router";
import { useUtils } from "@/utils/helper";
import iconBack from "@/assets/icons/prev.png";

import {
  getDetailDotGiamGia,
  updateDotGiamGia,
} from "@/api/admin/dotgiamgia/dotGiamGiaApi";

import DotGiamGiaForm from "./DotGiamGiaForm.vue";
import SanPhamApDung from "./SanPhamApDung.vue";
import SanPhamDaChon from "./SanPhamDaChon.vue";

/* ROUTER */
const route = useRoute();
const router = useRouter();
const { notify } = useUtils();
const formRef = ref(null);
const isSubmittingOrCanceling = ref(false);
const initialDataSnapshot = ref("");

/* FORM */
const form = ref({
  tenDot: "",
  giaTriGiam: 0,
  ngayBatDau: "",
  ngayKetThuc: "",
  moTa: "",
});

/* PRODUCTS */
const selectedProducts = ref([]);

/* LOADING */
const loading = ref(false);

const isFormValid = ref(false);

/* LOAD DATA */
const loadData = async () => {
  const currentId = route.params.id;

  if (!currentId) {
    notify.error("Không tìm thấy ID");
    return;
  }

  try {
    loading.value = true;

    const res = await getDetailDotGiamGia(currentId);
    const data = res.data;

    form.value = {
      tenDot: data.tenDot || "",
      giaTriGiam: data.giaTriGiam || 0,
      ngayBatDau: data.ngayBatDau?.slice(0, 16) || "",
      ngayKetThuc: data.ngayKetThuc?.slice(0, 16) || "",
      moTa: data.moTa || "",
    };

    selectedProducts.value = Array.isArray(data.sanPhams) ? data.sanPhams : [];

    initialDataSnapshot.value = JSON.stringify({
      tenDot: form.value.tenDot || "",
      giaTriGiam: form.value.giaTriGiam || 0,
      ngayBatDau: form.value.ngayBatDau || "",
      ngayKetThuc: form.value.ngayKetThuc || "",
      moTa: form.value.moTa || "",
      selectedProducts: [...selectedProducts.value].map(p => p.id).sort((a, b) => a - b),
    });
  } catch (err) {
    console.error(err);
    notify.error("Không tải được dữ liệu");
  } finally {
    loading.value = false;
  }
};

const handleBeforeUnload = (e) => {
  if (isSubmittingOrCanceling.value || !hasUnsavedChanges.value) return;
  e.preventDefault();
  e.returnValue = "";
};

onMounted(async () => {
  await loadData();
  window.addEventListener("beforeunload", handleBeforeUnload);
});

onUnmounted(() => {
  window.removeEventListener("beforeunload", handleBeforeUnload);
});

const hasUnsavedChanges = computed(() => {
  if (!initialDataSnapshot.value) return false;

  const currentSnapshot = JSON.stringify({
    tenDot: form.value.tenDot || "",
    giaTriGiam: form.value.giaTriGiam || 0,
    ngayBatDau: form.value.ngayBatDau || "",
    ngayKetThuc: form.value.ngayKetThuc || "",
    moTa: form.value.moTa || "",
    selectedProducts: [...selectedProducts.value].map(p => p.id).sort((a, b) => a - b),
  });

  return currentSnapshot !== initialDataSnapshot.value;
});

onBeforeRouteLeave(async (to, from) => {
  if (isSubmittingOrCanceling.value) {
    return true;
  }

  if (!hasUnsavedChanges.value) {
    return true;
  }

  const answer = await notify.confirm(
    "Bạn có thay đổi chưa lưu. Bạn có chắc chắn muốn rời đi và hủy bỏ các thay đổi này?"
  );

  return answer;
});

/* SELECT */
const handleSelect = (item) => {
  if (!item || !item.id) return;

  const index = selectedProducts.value.findIndex((p) => p.id === item.id);

  if (index !== -1) {
    selectedProducts.value.splice(index, 1);
  } else {
    selectedProducts.value.push({ ...item });
  }
};

const removeItem = (id) => {
  selectedProducts.value = selectedProducts.value.filter((p) => p.id !== id);
};

/* VALID */
const isValid = computed(() => {
  return (
    selectedProducts.value.length > 0 &&
    form.value.giaTriGiam > 0 &&
    form.value.ngayBatDau &&
    form.value.ngayKetThuc
  );
});

/* ✅ FIX DATE (GIỐNG FILE CREATE) */
const buildDateTime = (dateStr) => {
  if (!dateStr) return null;
  let formatted = dateStr.replace(" ", "T");
  if (formatted.length === 16) {
    return formatted + ":00";
  }
  return formatted;
};

/* SUBMIT */
const submit = async () => {
  const currentId = route.params.id;

  if (!currentId) {
    notify.error("Không tìm thấy ID");
    return;
  }

  if (!isValid.value) {
    if (formRef.value) formRef.value.showErrors();
    if (selectedProducts.value.length === 0) {
      notify.error("Vui lòng chọn ít nhất 1 sản phẩm áp dụng khuyến mãi");
    }
    return;
  }

  const ok = await notify.confirm("Xác nhận cập nhật khuyến mãi?");
  if (!ok) return;

  const payload = {
    ...form.value,

    ngayBatDau: buildDateTime(form.value.ngayBatDau),
    ngayKetThuc: buildDateTime(form.value.ngayKetThuc),

    sanPhamIds: selectedProducts.value.map((p) => p.id),
  };

  try {
    loading.value = true;

    console.log("PAYLOAD:", payload);

    await updateDotGiamGia(currentId, payload);

    notify.success("Cập nhật thành công!");
    isSubmittingOrCanceling.value = true;
    router.push("/admin/dot-giam-gia");
  } catch (err) {
    console.error(err);
    notify.error(err?.response?.data?.message || "Có lỗi xảy ra khi cập nhật");
  } finally {
    loading.value = false;
  }
};

/* BACK */
const goBack = () => router.back();
</script>

<template>
  <div class="page">
    <div class="box">
      <div class="box-header-flex">
        <div class="box-header">
          <h2 class="main-title">
            <span class="icon-wrapper">
              <i
                class="bi bi-tags"
                style="color: rgb(225, 29, 72); font-size: 24px"
              ></i>
            </span>
            Cập nhật khuyến mãi
          </h2>
          <p class="sub-title">Cập nhật thông tin đợt khuyến mãi hiện tại</p>
        </div>

        <button class="icon-btn-back" @click="goBack">
          <img :src="iconBack" />
        </button>
      </div>
    </div>

    <!-- Form thông tin đợt giảm giá - nằm ngang trên cùng -->
    <div class="form-top-section">
      <DotGiamGiaForm
        ref="formRef"
        v-model="form"
        @valid="isFormValid = $event"
      />
    </div>

    <!-- Danh sách sản phẩm áp dụng - full width -->
    <div class="mt-3">
      <SanPhamApDung
        :selected="selectedProducts"
        :isFormValid="isFormValid"
        :ngayBatDau="form.ngayBatDau"
        :ngayKetThuc="form.ngayKetThuc"
        :excludeId="Number(route.params.id)"
        @select="handleSelect"
      />
    </div>

    <!-- Danh sách đã chọn - full width bên dưới -->
    <div class="mt-3">
      <SanPhamDaChon
        :items="selectedProducts"
        :giaTriGiam="form.giaTriGiam"
        :isFormValid="isFormValid"
        @remove="removeItem"
      />
    </div>

    <div class="footer-actions box mt-3">
      <button class="btn-save" @click="submit">
        <i class="bi bi-check2-all"></i>
        <div class="btn-light-sweep"></div>
        Xác nhận
      </button>
    </div>
  </div>
</template>

<style scoped>
/* ===== ĐỒNG BỘ CHUẨN FORM VỚI ADDDOTGIAMGIA ===== */
.page {
  font-family: var(--font-family);
}

.box {
  background: #fff;
  border-radius: 10px;
  padding: 20px 24px;
  margin-bottom: 20px;
  border: 1px solid #eee;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}

.main-title {
  display: flex;
  align-items: center;
  font-size: 28px;
  font-weight: 600;
  gap: 12px;
  letter-spacing: 1px;
  margin-bottom: 5px;
  color: #1a1a1a;
}

/* Khung Icon */
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
  font-family: var(--font-family);
  font-size: var(--fs-text);
  font-weight: 400;
  color: var(--color-text-muted);
  margin: 0;
}

.count-display {
  margin-top: 12px;
  font-size: 15px;
  color: #555;
}

.highlight {
  color: #d70018; /* Xanh tiêu chuẩn */
  font-weight: 700;
}

.divider {
  margin: 0 10px;
  color: #ddd;
}

/* Form thông tin nằm trên full width */
.form-top-section {
  width: 100%;
}

/* Danh sách sản phẩm nằm ngang 2 cột bên dưới */
.products-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  align-items: start;
}

@media (max-width: 900px) {
  .products-row {
    grid-template-columns: 1fr;
  }
}

.products-col {
  min-width: 0;
}

.footer-actions {
  text-align: right;
  padding: 20px 24px;
}

/* Nút Lưu chuẩn */
.btn-save {
  position: relative;
  overflow: hidden;
  background: #d70018;
  color: white;
  border: none;
  padding: 12px 28px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 15px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s ease;
}

.btn-save:hover:not(:disabled) {
  background: #d70018;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(215, 0, 24, 0.2);
}

.btn-save:disabled {
  background: #dcdcdc;
  color: #888;
  cursor: not-allowed;
  box-shadow: none;
}

.btn-save:hover {
  background: #b92b2b;
}
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
.btn-save:hover .btn-light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
}
.btn-save:hover .btn-light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
}

.selected-products-area {
  margin-top: 20px;
}

.mt-3 {
  margin-top: 1rem;
}

.box-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center; /* căn giữa dọc */
}

.box-header {
  margin-bottom: 0;
}

.icon-btn-back {
  margin-left: auto;
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background-color: #f4f6f8;
  border: 1px solid #e9e9e9;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.icon-btn-back:hover {
  background-color: #e2e6ea;
  transform: translateX(-2px);
}

.icon-btn-back img {
  width: 20px;
  height: 20px;
  object-fit: contain;
}
</style>
