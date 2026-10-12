<script setup>
import { reactive, ref, computed, onMounted, onUnmounted } from "vue";
import { useRouter, useRoute, onBeforeRouteLeave } from "vue-router";
import iconSave from "@/assets/icons/add.png";
import iconBack from "@/assets/icons/prev.png";
import { useUtils } from "@/utils/helper";
import { updateNuocLau, detailNuocLau } from "@/api/admin/nuoclau/nuocLauApi";

const router = useRouter();
const route = useRoute();
const { notify, nprogress } = useUtils();
const isSubmittingOrCanceling = ref(false);
const initialDataSnapshot = ref("");

const id = route.params.id;

/* ===== FORM DATA ===== */
const formAddNuocLau = reactive({
  tenNuocLau: "",
  giaPhuThu: 0,
  moTa: "",
  hinhAnh: null,
  oldImage: null,
});

const errors = reactive({
  tenNuocLau: "",
  giaPhuThu: "",
  moTa: "",
  hinhAnh: "",
});

/* ===== IMAGE HANDLE ===== */
const imagePreview = computed(() => {
  if (formAddNuocLau.hinhAnh instanceof File) {
    return URL.createObjectURL(formAddNuocLau.hinhAnh);
  }
  if (formAddNuocLau.oldImage) {
    return `http://localhost:8080/uploads/${formAddNuocLau.oldImage}`;
  }
  return null;
});

const giaPhuThuDisplay = ref("");

const formatCurrency = (value) => {
  const number = value.replace(/\D/g, "");
  return number.replace(/\B(?=(\d{3})+(?!\d))/g, ".");
};

const onInputGia = (event) => {
  const rawValue = event.target.value.replace(/\D/g, "");

  giaPhuThuDisplay.value = formatCurrency(rawValue);
  formAddNuocLau.giaPhuThu = rawValue ? Number(rawValue) : 0;
  errors.giaPhuThu = "";
};

const onSelectImage = (e) => {
  const file = e.target.files[0];
  if (file) {
    formAddNuocLau.hinhAnh = file;
    errors.hinhAnh = "";
  }
};

/* ===== LOAD DETAIL ===== */
const loadDetail = async () => {
  nprogress.start();
  try {
    const res = await detailNuocLau(id);
    const data = res.data;

    formAddNuocLau.tenNuocLau = data.tenNuocLau;
    formAddNuocLau.giaPhuThu = data.giaPhuThu;
    giaPhuThuDisplay.value = formatCurrency(String(data.giaPhuThu || 0));
    formAddNuocLau.moTa = data.moTa;
    formAddNuocLau.oldImage = data.hinhAnh;

    initialDataSnapshot.value = JSON.stringify({
      tenNuocLau: formAddNuocLau.tenNuocLau || "",
      giaPhuThu: formAddNuocLau.giaPhuThu || 0,
      moTa: formAddNuocLau.moTa || "",
      hinhAnhName: typeof formAddNuocLau.hinhAnh === "string" ? formAddNuocLau.hinhAnh : "",
      oldImage: formAddNuocLau.oldImage || "",
    });
  } catch (e) {
    console.error(e);
    notify.error("Không tải được dữ liệu");
  } finally {
    nprogress.done();
  }
};

/* ===== UPDATE ===== */
const submitNuocLau = async () => {
  let hasError = false;

  errors.tenNuocLau = "";
  errors.giaPhuThu = "";
  errors.hinhAnh = "";
  errors.moTa = "";

  if (!formAddNuocLau.hinhAnh && !formAddNuocLau.oldImage) {
    errors.hinhAnh = "Vui lòng chọn hình ảnh";
    hasError = true;
  }

  const tenNuocLauTrimmed = formAddNuocLau.tenNuocLau
    ? formAddNuocLau.tenNuocLau.trim()
    : "";

  if (!tenNuocLauTrimmed) {
    errors.tenNuocLau = "Vui lòng nhập tên nước lẩu";
    hasError = true;
  } else if (tenNuocLauTrimmed.length < 3 || tenNuocLauTrimmed.length > 255) {
    errors.tenNuocLau = "Tên nước lẩu phải từ 3 đến 255 ký tự";
    hasError = true;
  }

  const moTaTrimmed = formAddNuocLau.moTa ? formAddNuocLau.moTa.trim() : "";
  if (moTaTrimmed.length > 255) {
    errors.moTa = "Mô tả không được vượt quá 255 ký tự";
    hasError = true;
  }

  const gia = formAddNuocLau.giaPhuThu;
  if (gia === null || gia === undefined || String(gia).trim() === "") {
    errors.giaPhuThu = "Vui lòng nhập giá phụ thu";
    hasError = true;
  } else if (isNaN(gia)) {
    errors.giaPhuThu = "Giá phụ thu phải là số hợp lệ";
    hasError = true;
  } else if (gia < 1000) {
    errors.giaPhuThu = "Giá phụ thu phải từ 1.000đ trở lên";
    hasError = true;
  } else if (gia > 100000000) {
    errors.giaPhuThu = "Giá phụ thu không được vượt quá 100.000.000đ";
    hasError = true;
  }

  if (hasError) return;

  if (!(await notify.confirm("Bạn có muốn cập nhật nước lẩu ?"))) return;

  nprogress.start();

  try {
    const formData = new FormData();

    formData.append("tenNuocLau", tenNuocLauTrimmed);
    formData.append("giaPhuThu", formAddNuocLau.giaPhuThu);
    formData.append("moTa", moTaTrimmed);

    if (formAddNuocLau.hinhAnh) {
      formData.append("file", formAddNuocLau.hinhAnh);
    }

    const res = await updateNuocLau(id, formData);

    notify.success(res.data.message || "Cập nhật nước lẩu thành công!");
    isSubmittingOrCanceling.value = true;
    router.push("/admin/thuc-don/nuoc-lau");
  } catch (e) {
    notify.error(
      e.response?.data?.message || e.response?.data || "Có lỗi xảy ra",
    );
  } finally {
    nprogress.done();
  }
};
const huyBo = () => router.back();

const handleBeforeUnload = (e) => {
  if (isSubmittingOrCanceling.value || !hasUnsavedChanges.value) return;
  e.preventDefault();
  e.returnValue = "";
};

onMounted(() => {
  loadDetail();
  window.addEventListener("beforeunload", handleBeforeUnload);
});

onUnmounted(() => {
  window.removeEventListener("beforeunload", handleBeforeUnload);
});

const hasUnsavedChanges = computed(() => {
  if (!initialDataSnapshot.value) return false;

  const currentSnapshot = JSON.stringify({
    tenNuocLau: formAddNuocLau.tenNuocLau || "",
    giaPhuThu: formAddNuocLau.giaPhuThu || 0,
    moTa: formAddNuocLau.moTa || "",
    hinhAnhName: formAddNuocLau.hinhAnh instanceof File ? formAddNuocLau.hinhAnh.name : (typeof formAddNuocLau.hinhAnh === "string" ? formAddNuocLau.hinhAnh : ""),
    oldImage: formAddNuocLau.oldImage || "",
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
</script>

<template>
  <div class="modern-container">
    <div class="inner-content">
      <header class="box-header-card">
        <div class="box-header-flex">
          <div class="box-header">
            <h2 class="main-title">
              <span class="icon-wrapper">
                <img
                  src="@/assets/icons/nuoclau.png"
                  alt="Nước lẩu"
                  style="width: 28px; height: 28px; object-fit: contain"
                />
              </span>
              Sửa Nước Lẩu
            </h2>
            <p>Cập nhật thông tin và hương vị nước lẩu</p>
          </div>
          <button class="icon-btn-back" @click="huyBo">
            <img :src="iconBack" />
          </button>
        </div>
      </header>

      <main class="main-content">
        <!-- SIDEBAR -->
        <aside class="side-panel">
          <div class="card">
            <h3 class="card-title">
              Ảnh đại diện <span class="text-danger">*</span>
            </h3>

            <div class="image-upload-zone">
              <div v-if="imagePreview" class="preview-wrapper">
                <img :src="imagePreview" />
                <button
                  class="remove-badge"
                  @click="
                    formAddNuocLau.hinhAnh = null;
                    formAddNuocLau.oldImage = null;
                  "
                >
                  ×
                </button>
              </div>

              <div v-else class="upload-placeholder">
                <i class="bi bi-cloud-arrow-up"></i>
                <p>Chưa có hình ảnh</p>
              </div>

              <input
                type="file"
                id="main-upload"
                hidden
                @change="onSelectImage"
                accept="image/*"
              />

              <label for="main-upload" class="mini-upload-btn">
                <i class="bi bi-camera"></i> Chọn ảnh nước lẩu
              </label>
            </div>
            <transition name="shake">
              <p
                v-if="errors.hinhAnh"
                class="error-hint mt-2"
                style="width: max-content"
              >
                <i class="bi bi-exclamation-circle"></i>
                {{ errors.hinhAnh }}
              </p>
            </transition>
          </div>

          <div class="card mt-4">
            <h3 class="card-title">Chi phí</h3>

            <div class="form-group">
              <label class="bold-label">Giá phụ thu (VNĐ)</label>
              <input
                :value="giaPhuThuDisplay"
                @input="onInputGia"
                class="modern-input highlight-price"
                :class="{ 'input-invalid': errors.giaPhuThu }"
                placeholder="0"
              />
              <transition name="shake">
                <p
                  v-if="errors.giaPhuThu"
                  class="error-hint"
                  style="width: max-content; margin-top: 5px"
                >
                  <i class="bi bi-exclamation-circle"></i>
                  {{ errors.giaPhuThu }}
                </p>
              </transition>
            </div>
          </div>
        </aside>

        <!-- MAIN -->
        <section class="main-panel">
          <div class="card h-full">
            <div class="card-header-flex">
              <div>
                <h3 class="card-title">Thông tin cơ bản</h3>
                <p class="subtitle">Cập nhật tên và mô tả nước lẩu</p>
              </div>
            </div>

            <div class="form-group">
              <label class="bold-label">
                Tên nước lẩu <span class="text-danger">*</span>
              </label>

              <input
                v-model="formAddNuocLau.tenNuocLau"
                class="modern-input"
                :class="{ 'input-invalid': errors.tenNuocLau }"
                @input="errors.tenNuocLau = ''"
              />
              <transition name="shake">
                <p
                  v-if="errors.tenNuocLau"
                  class="error-hint"
                  style="width: max-content; margin-top: 5px"
                >
                  <i class="bi bi-exclamation-circle"></i>
                  {{ errors.tenNuocLau }}
                </p>
              </transition>
            </div>

            <div class="form-group mt-4">
              <label class="bold-label">Mô tả hương vị</label>

              <textarea
                v-model="formAddNuocLau.moTa"
                rows="8"
                class="modern-input"
                :class="{ 'input-invalid': errors.moTa }"
                @input="errors.moTa = ''"
              ></textarea>
              <transition name="shake">
                <p
                  v-if="errors.moTa"
                  class="error-hint"
                  style="width: max-content; margin-top: 5px"
                >
                  <i class="bi bi-exclamation-circle"></i>
                  {{ errors.moTa }}
                </p>
              </transition>
            </div>
          </div>
        </section>
      </main>

      <footer class="action-footer">
        <div class="footer-content">
          <p class="footer-note">* Kiểm tra lại thông tin trước khi lưu</p>

          <div class="footer-btns">
            <button class="btn-cancel" @click="huyBo">Hủy bỏ</button>

            <button class="btn-submit" @click="submitNuocLau">
              <i class="bi bi-check2-all"></i>
              Xác nhận
              <div class="btn-light-sweep"></div>
            </button>
          </div>
        </div>
      </footer>
    </div>
  </div>
</template>

<style scoped>
/* --- LAYOUT CHÍNH (Đồng bộ file trước) --- */
.modern-container {
  background-color: #f8fafc;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  font-family: "Inter", sans-serif;
}

.inner-content {
  width: 100%;
  max-width: 100%; /* Đổi từ 1200px thành 100% để tràn ra */
  display: flex;
  flex-direction: column;
  gap: 20px;
}
/* --- HEADER --- */
.box-header-card {
  background: white;
  border-radius: 7px;
  padding: 24px 32px;
  border: 1px solid #e2e8f0;
}
.box-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
.box-header {
  margin-bottom: 0;
}
.box-header p {
  color: #64748b;
  margin: 0;
  font-size: 14px;
}

/* --- GRID LAYOUT --- */
.main-content {
  display: grid;
  grid-template-columns: 320px 1fr; /* Thu nhỏ sidebar lại một chút (từ 350px xuống 320px) */
  gap: 24px;
}

/* --- CARD & INPUT --- */
.card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #e2e8f0;
}
.card-title {
  font-size: 16px;
  font-weight: 800;
  color: #000;
  margin-bottom: 15px;
}
.bold-label {
  font-weight: 800;
  color: #000;
  font-size: 13px;
  margin-bottom: 8px;
  display: block;
}
.modern-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 14px;
  background: #fcfcfc;
  outline: none;
}
.highlight-price {
  font-weight: 700;
  color: #d32f2f;
}

/* --- IMAGE UPLOAD --- */
.image-upload-zone {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}
.preview-wrapper {
  position: relative;
  width: 100%;
  aspect-ratio: 16/10;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #f1f5f9;
}
.preview-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.remove-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  cursor: pointer;
}
.upload-placeholder {
  width: 100%;
  aspect-ratio: 16/10;
  background: #f8fafc;
  border: 2px dashed #e2e8f0;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
}
.mini-upload-btn {
  background: #f1f5f9;
  padding: 8px 20px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  border: 1px solid #e2e8f0;
}

/* --- FOOTER --- */
.action-footer {
  background: #fff;
  padding: 20px 24px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
}
.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.footer-btns {
  display: flex;
  gap: 12px;
}
.btn-submit {
  position: relative;
  overflow: hidden;
  background: #d32f2f;
  color: white;
  border: none;
  padding: 12px 28px;
  border-radius: 7px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
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
.modal-btn-submit:hover .btn-light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
}
.btn-submit:hover .btn-light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
}
.btn-cancel {
  background: #f1f5f9;
  border: none;
  padding: 12px 28px;
  border-radius: 7px;
  cursor: pointer;
  font-weight: 600;
  color: #64748b;
}
.btn-icon {
  width: 18px;
  filter: brightness(0) invert(1);
}

.text-danger {
  color: #d32f2f;
}
.mt-4 {
  margin-top: 20px;
}
.subtitle {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 15px;
}

/* --- ERROR VALIDATION --- */
.error-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #e63946;
  font-size: 0.85rem;
  margin-top: 4px;
  padding: 6px 12px;
  background-color: #fff1f2;
  border-radius: 6px;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.input-invalid {
  border-color: #e63946 !important;
  box-shadow: 0 0 0 4px rgba(230, 57, 70, 0.1) !important;
}

.shake-enter-active {
  animation: shake 0.4s cubic-bezier(0.36, 0.07, 0.19, 0.97) both;
}

@keyframes shake {
  10%,
  90% {
    transform: translate3d(-1px, 0, 0);
  }
  20%,
  80% {
    transform: translate3d(2px, 0, 0);
  }
  30%,
  50%,
  70% {
    transform: translate3d(-4px, 0, 0);
  }
  40%,
  60% {
    transform: translate3d(4px, 0, 0);
  }
}
</style>
