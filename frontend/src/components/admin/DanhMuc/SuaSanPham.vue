<script setup>
import { useRouter, useRoute, onBeforeRouteLeave } from "vue-router";
import { reactive, ref, onMounted, computed, watch, onUnmounted } from "vue";
import iconSave from "@/assets/icons/add.png";
import iconBack from "@/assets/icons/prev.png";
import { getAllDanhMuc } from "@/api/admin/danhmuc/danhMucApi";
import { useUtils } from "@/utils/helper";
import { updateSanPham } from "@/api/admin/sanpham/sanPhamApi";
import { getAllDinhLuong } from "@/api/admin/thuoctinh/dinhLuongApi";
import { getDinhLuongByDanhMuc } from "@/api/admin/thuoctinh/dinhLuongApi";
import { getAllDonVi } from "@/api/admin/thuoctinh/donViApi";
import { deTailSanPham } from "@/api/admin/sanpham/sanPhamApi";
import { connectWebSocket, disconnectWebSocket } from "@/utils/websocket";
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";
const { notify, nprogress, formatCurrency, parseCurrency } = useUtils();
const router = useRouter();
const isSubmittingOrCanceling = ref(false);
const initialDataSnapshot = ref("");
const danhMucList = ref([]);
const danhSachDinhLuong = ref([]);
const danhSachDonVi = ref([]);
const isLoadingDetail = ref(true);
const route = useRoute();
const id = route.params.id;

const activeDanhMucList = computed(() => {
  return danhMucList.value.filter(
    (dm) => !dm.trangThaiXoa || dm.id === formUpdateSanPham.idDanhMuc,
  );
});

const coDanhMucNgungHoatDong = computed(() => {
  if (!formUpdateSanPham.idDanhMuc) return false;
  const dm = danhMucList.value.find((d) => d.id === formUpdateSanPham.idDanhMuc);
  return dm ? Boolean(dm.trangThaiXoa) : false;
});

const errors = reactive({
  hinhAnh: "",
  tenSanPham: "",
  idDanhMuc: "",
  chiTiet: [],
});

const formUpdateSanPham = reactive({
  tenSanPham: "",
  idDanhMuc: "",
  moTa: "",
  hinhAnh: null,
  chiTietSPS: [
    {
      gia: 0,
      idDinhLuong: null,
    },
  ],
});

watch(
  () => formUpdateSanPham.chiTietSPS,
  (val) => {
    console.log(JSON.parse(JSON.stringify(val)));
  },
  { deep: true },
);
watch(
  () => formUpdateSanPham.idDanhMuc,
  async (newValue) => {
    if (!newValue || isLoadingDetail.value) return;

    const res = await getDinhLuongByDanhMuc(newValue);
    danhSachDinhLuong.value = res.data;

    formUpdateSanPham.chiTietSPS.forEach((i) => {
      i.idDinhLuong = null;
    });
  },
);
const handleBeforeUnload = (e) => {
  if (isSubmittingOrCanceling.value || !hasUnsavedChanges.value) return;
  e.preventDefault();
  e.returnValue = "";
};

const hasProductChanged = ref(false);

const loadDanhMucList = async () => {
  try {
    const dmRes = await getAllDanhMuc();
    danhMucList.value = dmRes.data || [];
  } catch (err) {
    console.error("Lỗi load danh mục:", err);
  }
};

const handleWsMessage = async (msg) => {
  if (msg?.type === "DANH_MUC_UPDATE") {
    await loadDanhMucList();
    if (formUpdateSanPham.idDanhMuc) {
      const res = await getDinhLuongByDanhMuc(formUpdateSanPham.idDanhMuc);
      danhSachDinhLuong.value = res.data || [];
    }
    hasProductChanged.value = true;
    notify.confirm(
      "Danh mục món ăn có sự thay đổi, vui lòng kiểm tra lại thông tin trước khi xác nhận!",
    );
  } else if (msg?.type === "DON_VI_UPDATE") {
    if (formUpdateSanPham.idDanhMuc) {
      const res = await getDinhLuongByDanhMuc(formUpdateSanPham.idDanhMuc);
      danhSachDinhLuong.value = res.data || [];
    }
    hasProductChanged.value = true;
    notify.confirm(
      "Đơn vị định lượng có sự thay đổi, vui lòng kiểm tra lại thông tin trước khi xác nhận!",
    );
  }
};

onMounted(async () => {
  window.addEventListener("beforeunload", handleBeforeUnload);
  try {
    isLoadingDetail.value = true;

    nprogress.start();

    const [dmRes, dvRes] = await Promise.all([getAllDanhMuc(), getAllDonVi()]);

    danhMucList.value = dmRes.data || [];
    danhSachDonVi.value = dvRes.data || [];

    const detailRes = await deTailSanPham(id);
    const data = detailRes.data;

    formUpdateSanPham.tenSanPham = data.tenSanPham;
    formUpdateSanPham.idDanhMuc = Number(data.idDanhMuc);
    formUpdateSanPham.moTa = data.moTa;
    formUpdateSanPham.hinhAnh = data.hinhAnh;
    const resDL = await getDinhLuongByDanhMuc(data.idDanhMuc);
    danhSachDinhLuong.value = resDL.data || [];
    formUpdateSanPham.chiTietSPS = [
      {
        gia: data.gia,
        idDinhLuong: Number(data.idDinhLuong),
      },
    ];

    initialDataSnapshot.value = JSON.stringify({
      tenSanPham: formUpdateSanPham.tenSanPham,
      idDanhMuc: formUpdateSanPham.idDanhMuc,
      moTa: formUpdateSanPham.moTa,
      hinhAnh: formUpdateSanPham.hinhAnh,
      chiTietSPS: formUpdateSanPham.chiTietSPS,
    });
  } catch (err) {
    console.error("Lỗi load chi tiết:", err);
  } finally {
    isLoadingDetail.value = false;
    nprogress.done();
  }

  connectWebSocket(null, handleWsMessage);
});

onUnmounted(() => {
  window.removeEventListener("beforeunload", handleBeforeUnload);
  disconnectWebSocket(handleWsMessage);
});

const hasUnsavedChanges = computed(() => {
  if (!initialDataSnapshot.value) return false;

  const currentSnapshot = JSON.stringify({
    tenSanPham: formUpdateSanPham.tenSanPham || "",
    idDanhMuc: formUpdateSanPham.idDanhMuc || "",
    moTa: formUpdateSanPham.moTa || "",
    hinhAnhName: formUpdateSanPham.hinhAnh instanceof File ? formUpdateSanPham.hinhAnh.name : (typeof formUpdateSanPham.hinhAnh === "string" ? formUpdateSanPham.hinhAnh : ""),
    chiTietSPS: [...formUpdateSanPham.chiTietSPS].map(i => ({ gia: i.gia, idDinhLuong: i.idDinhLuong })),
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

const themDong = () => {
  formUpdateSanPham.chiTietSPS.push({
    gia: 0,
    idDinhLuong: null,
  });
};

const xoaDong = (index) => {
  if (formUpdateSanPham.chiTietSPS.length > 1)
    formUpdateSanPham.chiTietSPS.splice(index, 1);
};

const chonHinhAnh = (e) => {
  const file = e.target.files[0];
  if (file) {
    formUpdateSanPham.hinhAnh = file;
    errors.hinhAnh = "";
  }
};
const updateSanPhamSubmit = async () => {
  let hasError = false;
  errors.hinhAnh = "";
  errors.tenSanPham = "";
  errors.idDanhMuc = "";
  errors.chiTiet = formUpdateSanPham.chiTietSPS.map(() => ({
    gia: "",
    idDinhLuong: "",
  }));

  if (!formUpdateSanPham.hinhAnh) {
    errors.hinhAnh = "Vui lòng chọn ảnh minh họa";
    hasError = true;
  }
  if (!formUpdateSanPham.idDanhMuc) {
    errors.idDanhMuc = "Vui lòng chọn danh mục";
    hasError = true;
  }
  if (
    !formUpdateSanPham.tenSanPham ||
    formUpdateSanPham.tenSanPham.trim() === ""
  ) {
    errors.tenSanPham = "Vui lòng nhập tên món ăn";
    hasError = true;
  }
  formUpdateSanPham.chiTietSPS.forEach((item, index) => {
    if (item.gia === "" || item.gia === null) {
      errors.chiTiet[index].gia = "Vui lòng nhập giá";
      hasError = true;
    } else if (item.gia < 1000) {
      errors.chiTiet[index].gia = "Giá phải lớn hơn hoặc bằng 1,000";
      hasError = true;
    } else if (item.gia > 100000000) {
      errors.chiTiet[index].gia = "Giá không được vượt quá 100,000,000";
      hasError = true;
    }
    if (!item.idDinhLuong) {
      errors.chiTiet[index].idDinhLuong = "Vui lòng chọn định lượng";
      hasError = true;
    }
  });

  if (coDanhMucNgungHoatDong.value) {
    notify.error("Danh mục đã ngừng hoạt động, vui lòng chọn danh mục khác!");
    return;
  }

  if (hasError) return;

  if (!(await notify.confirm("Bạn có muốn sửa sản phẩm này?"))) return;

  nprogress.start();

  try {
    const formData = new FormData();
    const detail = formUpdateSanPham.chiTietSPS[0]; // chỉ lấy 1 dòng

    formData.append("tenSanPham", formUpdateSanPham.tenSanPham);
    formData.append("idDanhMuc", formUpdateSanPham.idDanhMuc);
    formData.append("moTa", formUpdateSanPham.moTa);

    formData.append("gia", detail.gia);
    formData.append("idDinhLuong", detail.idDinhLuong);

    if (formUpdateSanPham.hinhAnh instanceof File) {
      formData.append("file", formUpdateSanPham.hinhAnh);
    }

    await updateSanPham(id, formData);

    notify.success("Sửa thành công!");
    isSubmittingOrCanceling.value = true;
    router.push("/admin/san-pham/hien-thi-all");
  } catch (e) {
    console.error(e);
    const errMsg =
      e.response?.data?.message ||
      (typeof e.response?.data === "string" ? e.response.data : "") ||
      e.message ||
      "Có lỗi xảy ra khi lưu";
    notify.error(errMsg);
  } finally {
    nprogress.done();
  }
};
const imagePreview = computed(() => {
  if (formUpdateSanPham.hinhAnh instanceof File) {
    return URL.createObjectURL(formUpdateSanPham.hinhAnh);
  }

  // nếu là ảnh string từ backend
  if (typeof formUpdateSanPham.hinhAnh === "string") {
    return `http://localhost:8080/uploads/${formUpdateSanPham.hinhAnh}`;
  }

  return null;
});

const huyBo = () => router.back();
</script>

<template>
  <div class="modern-container">
    <div class="inner-content">
      <header class="glass-header">
        <div class="box-header-card shadow-sm">
          <div class="box-header-flex">
            <div class="title-group">
              <h2 class="title">
                <div class="icon-box-wrapper">
                  <img src="@/assets/icons/categories.png" alt="icon" />
                </div>
                Cập Nhật Món Ăn
              </h2>
              <p class="subtitle-text">
                Thiết lập thông tin và quy cách hiển thị
              </p>
            </div>
            <button class="icon-btn-back" @click="huyBo">
              <img :src="iconBack" />
            </button>
          </div>
        </div>
      </header>

      <main class="main-content">
        <aside class="side-panel">
          <div class="card">
            <h3 class="card-title">
              Ảnh minh họa <span class="text-danger">*</span>
            </h3>
            <div class="image-upload-zone">
              <div v-if="imagePreview" class="preview-wrapper">
                <img :src="imagePreview" />
                <button
                  class="remove-badge"
                  @click="formUpdateSanPham.hinhAnh = null"
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
                @change="chonHinhAnh"
                accept="image/*"
              />
              <label for="main-upload" class="mini-upload-btn">
                <i class="bi bi-camera"></i> Chọn ảnh sản phẩm
              </label>
              <transition name="shake">
                <p v-if="errors.hinhAnh" class="error-hint mt-1">
                  <i class="bi bi-exclamation-circle"></i>
                  {{ errors.hinhAnh }}
                </p>
              </transition>
            </div>
          </div>

          <div class="card mt-4">
            <h3 class="card-title">Phân loại</h3>
            <div class="form-group">
              <label class="bold-label"
                >Danh mục món ăn <span class="text-danger">*</span></label
              >
              <v-select
                class="modern-select"
                :class="{ 'input-invalid-vs': errors.idDanhMuc || coDanhMucNgungHoatDong }"
                v-model="formUpdateSanPham.idDanhMuc"
                @update:modelValue="errors.idDanhMuc = ''"
                :options="activeDanhMucList"
                label="tenDanhMuc"
                :reduce="(dm) => dm.id"
                placeholder="Chọn danh mục..."
                :clearable="false"
              >
                <template #option="dm">
                  {{ dm.tenDanhMuc }}{{ dm.trangThaiXoa ? " (Ngừng HĐ)" : "" }}
                </template>
                <template #selected-option="dm">
                  {{ dm.tenDanhMuc }}{{ dm.trangThaiXoa ? " (Ngừng HĐ)" : "" }}
                </template>
                <template #no-options="{ search, searching }">
                  <template v-if="searching">
                    Không tìm thấy "<b>{{ search }}</b
                    >"
                  </template>
                  <em v-else style="opacity: 0.5">Không có dữ liệu</em>
                </template>
              </v-select>
              <transition name="shake">
                <p v-if="errors.idDanhMuc || coDanhMucNgungHoatDong" class="error-hint mt-1">
                  <i class="bi bi-exclamation-circle"></i>
                  {{ errors.idDanhMuc || "Danh mục đã ngừng hoạt động" }}
                </p>
              </transition>
            </div>
          </div>
        </aside>

        <section class="main-panel">
          <div class="card h-full">
            <div class="card-header-flex">
              <div>
                <h3 class="card-title">Thông tin chi tiết</h3>
                <p class="subtitle">
                  Thiết lập tên hiển thị, giá và kích thước
                </p>
              </div>
              <button class="btn-add-outline" @click="themDong">
                <i class="bi bi-plus-circle"></i> Thêm quy cách
              </button>
            </div>

            <div class="base-info-row">
              <div class="form-group">
                <label class="bold-label"
                  >Tên đồ uống <span class="text-danger">*</span></label
                >
                <input
                  v-model="formUpdateSanPham.tenSanPham"
                  class="modern-input"
                  :class="{ 'input-invalid': errors.tenSanPham }"
                  @input="errors.tenSanPham = ''"
                  placeholder="Nhập tên (VD: Trà đào cam sả)"
                />
                <transition name="shake">
                  <p v-if="errors.tenSanPham" class="error-hint mt-1">
                    <i class="bi bi-exclamation-circle"></i>
                    {{ errors.tenSanPham }}
                  </p>
                </transition>
              </div>
            </div>

            <div class="details-list">
              <div
                v-for="(item, index) in formUpdateSanPham.chiTietSPS"
                :key="index"
                class="detail-item-card"
              >
                <div class="detail-grid">
                  <div class="form-group">
                    <label class="bold-label"
                      >Giá bán (VNĐ) <span class="text-danger">*</span></label
                    >
                    <input
                      type="text"
                      :value="formatCurrency(item.gia)"
                      @input="
                        item.gia = parseCurrency($event.target.value);
                        errors.chiTiet[index] &&
                          (errors.chiTiet[index].gia = '');
                      "
                      class="modern-input highlight-price"
                      :class="{ 'input-invalid': errors.chiTiet?.[index]?.gia }"
                    />
                    <transition name="shake">
                      <p
                        v-if="errors.chiTiet?.[index]?.gia"
                        class="error-hint mt-1"
                      >
                        <i class="bi bi-exclamation-circle"></i>
                        {{ errors.chiTiet[index].gia }}
                      </p>
                    </transition>
                  </div>

                  <div class="form-group">
                    <label class="bold-label"
                      >Định lượng <span class="text-danger">*</span></label
                    >
                    <div class="input-with-action">
                      <v-select
                        class="modern-select flex-grow-1"
                        :class="{
                          'input-invalid-vs':
                            errors.chiTiet?.[index]?.idDinhLuong,
                        }"
                        v-model="item.idDinhLuong"
                        @update:modelValue="
                          errors.chiTiet[index] &&
                            (errors.chiTiet[index].idDinhLuong = '')
                        "
                        :options="danhSachDinhLuong"
                        :reduce="(dl) => dl.id"
                        label="giaTri"
                        placeholder="Chọn định lượng"
                        :clearable="false"
                      >
                        <template #option="{ giaTri, tenDonVi }">
                          {{ giaTri }} {{ tenDonVi }}
                        </template>

                        <template #selected-option="{ giaTri, tenDonVi }">
                          {{ giaTri }} {{ tenDonVi }}
                        </template>

                        <template #no-options="{ search, searching }">
                          <template v-if="searching">
                            Không tìm thấy "<b>{{ search }}</b
                            >"
                          </template>
                          <em v-else style="opacity: 0.5">Không có dữ liệu</em>
                        </template>
                      </v-select>
                      <button
                        v-if="formUpdateSanPham.chiTietSPS.length > 1"
                        class="btn-delete-circle"
                        @click="xoaDong(index)"
                      >
                        <i class="bi bi-trash3"></i>
                      </button>
                    </div>
                    <transition name="shake">
                      <p
                        v-if="errors.chiTiet?.[index]?.idDinhLuong"
                        class="error-hint mt-1"
                      >
                        <i class="bi bi-exclamation-circle"></i>
                        {{ errors.chiTiet[index].idDinhLuong }}
                      </p>
                    </transition>
                  </div>
                </div>
              </div>
            </div>

            <div class="form-group mt-4">
              <label class="bold-label">Mô tả sản phẩm</label>
              <textarea
                v-model="formUpdateSanPham.moTa"
                rows="4"
                class="modern-input"
                placeholder="Ghi chú thêm về đồ uống..."
              ></textarea>
            </div>
          </div>
        </section>
      </main>

      <footer class="action-footer">
        <div class="footer-content">
          <div class="footer-left-content">
            <p class="footer-note">
              * Kiểm tra kỹ thông tin trước khi lưu vào hệ thống
            </p>
            <div
              v-if="hasProductChanged"
              class="alert-product-changed fw-bold"
            >
              <i class="ri-error-warning-fill"></i>
              <span>Danh mục món ăn có sự thay đổi vui lòng kiểm tra thông tin trước khi xác nhận</span>
            </div>
          </div>
          <div class="footer-btns">
            <button class="btn-cancel" @click="huyBo">Hủy bỏ</button>
            <button
              class="btn-submit"
              @click="updateSanPhamSubmit()"
              :disabled="coDanhMucNgungHoatDong"
              :class="{ 'btn-disabled': coDanhMucNgungHoatDong }"
            >
              <i class="bi bi-check2-all"></i>
              <div class="btn-light-sweep"></div>
              Xác nhận
            </button>
          </div>
        </div>
      </footer>
    </div>
  </div>
</template>

<style scoped>
/* --- LAYOUT CHÍNH --- */
.modern-container {
  background-color: #f8fafc;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  font-family: "Inter", sans-serif;
}

.inner-content {
  width: 100%;
  max-width: 100%; /* Đổi thành 100% để tràn ra */
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* --- HEADER --- */
.box-header-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px 24px;
  border: 1px solid #e2e8f0;
}
.box-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.title {
  display: flex;
  align-items: center;
  gap: 15px;
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: #000;
}
.icon-box-wrapper {
  background: #fff1f1;
  width: 45px;
  height: 45px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}
.icon-box-wrapper img {
  width: 24px;
  height: 24px;
}
.subtitle-text {
  color: #64748b;
  font-size: 13px;
  margin: 5px 0 0 60px;
}
.icon-btn-back {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  border: none;
  background: #f1f5f9;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.icon-btn-back img {
  width: 16px;
  opacity: 0.6;
}

/* --- MAIN CONTENT GRID --- */
.main-content {
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 24px;
  align-items: start;
}

/* --- CARDS & FORMS --- */
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
}
.highlight-price {
  font-weight: 700;
  color: #d32f2f;
}

/* --- FOOTER (ĐÃ SỬA KHÔNG CÒN LÒI) --- */
.action-footer {
  background: #fff;
  padding: 20px 24px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  margin-top: 10px;
}
.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.footer-note {
  font-size: 13px;
  color: #94a3b8;
  font-style: italic;
  margin: 0;
}
.footer-btns {
  display: flex;
  gap: 12px;
}

/* --- BUTTONS --- */
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
.btn-submit:hover .btn-light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
}
.btn-submit:hover .btn-light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
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
  box-shadow: 0 4px 12px rgba(211, 47, 47, 0.2);
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
.btn-add-outline {
  background: #fff;
  border: 1px solid #d32f2f;
  color: #d32f2f;
  padding: 8px 16px;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
}

/* --- OTHER UI ELEMENTS --- */
.image-upload-zone {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}
.preview-wrapper {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  border-radius: 16px;
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
  top: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  cursor: pointer;
}
.upload-placeholder {
  width: 100%;
  aspect-ratio: 1;
  background: #f8fafc;
  border: 2px dashed #e2e8f0;
  border-radius: 16px;
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
.detail-item-card {
  background: #f8fafc;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 15px;
  border: 1px solid #eff6ff;
}
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1.2fr; /* 3 cột chuẩn */
  gap: 15px;
  align-items: flex-end; /* Căn lề dưới cho thẳng hàng */
  background: #ffffff;
  padding: 10px;
  border-radius: 8px;
}
.input-with-action {
  display: flex;
  gap: 8px;
  align-items: center;
}
.btn-delete-circle {
  min-width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: #fee2e2;
  color: #dc2626;
  cursor: pointer;
}
.text-danger {
  color: #d32f2f;
}
.mt-4 {
  margin-top: 20px;
}
.mt-3 {
  margin-top: 15px;
}
.shadow-sm {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
.card-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}
.base-info-row {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f1f5f9;
}
.subtitle {
  font-size: 13px;
  color: #64748b;
  margin-top: -10px;
  margin-bottom: 15px;
}

/* SEARCH 2 */
/* ===== SELECT 2 && TIM KIEM ===== */
/* Ghi đè triệt để các viền mặc định của thư viện */
:deep(.v-select) {
  background: #fff;
  width: 100%;
}

/* Khung chính: Xóa border mặc định, dùng border của mình */
:deep(.vs__dropdown-toggle) {
  border: 1px solid #e5e7eb !important; /* Màu viền giống ô Trạng thái */
  border-radius: 10px !important;
  padding: 0 8px 0 0 !important;
  min-height: 42px;
  height: 42px; /* Chiều cao khớp hoàn toàn với input/select thường */
  display: flex;
  align-items: center;
  transition: all 0.2s ease;
  box-shadow: none !important;
}

:deep(.vs__selected-options) {
  display: flex !important;
  align-items: center !important;
  flex-wrap: nowrap !important;
  overflow: hidden !important;
  padding: 0 0 0 12px !important;
  width: calc(100% - 35px) !important;
  max-width: calc(100% - 35px) !important;
}

/* Xóa viền đỏ/xanh khi focus mà thư viện tự thêm vào search input */
:deep(.vs__search),
:deep(.vs__search:focus) {
  border: none !important;
  margin: 0 !important;
  padding: 0 !important;
  background: transparent !important;
  box-shadow: none !important;
}

/* Xử lý text đã chọn */
:deep(.vs__selected) {
  position: static !important;
  margin: 0 !important;
  padding: 0 !important;
  color: #374151;
  border: none !important;
  white-space: nowrap !important;
  overflow: hidden !important;
  text-overflow: ellipsis !important;
  max-width: 100% !important;
  display: block !important;
}

/* Placeholder */
:deep(.vs__search::placeholder) {
  color: #9ca3af !important;
}

/* Menu đổ xuống */
:deep(.vs__dropdown-menu) {
  border: 1px solid #e5e7eb !important;
  border-radius: 10px !important;
  margin-top: 8px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1) !important;
  padding: 6px !important;
  z-index: 1000;
}

/* Các Option bên trong */
:deep(.vs__dropdown-option) {
  border-radius: 8px !important;
  padding: 8px 12px !important;
  margin-bottom: 2px !important;
  color: #4b5563;
}

/* Icon mũi tên và nút xóa */
:deep(.vs__actions) {
  padding-right: 10px !important;
}

:deep(.vs__open-indicator) {
  fill: #9ca3af !important;
  transform: scale(0.7);
}

:deep(.vs__clear) {
  fill: #9ca3af !important;
}
:deep(.modern-select .vs__dropdown-toggle) {
  height: 42px;
}
.disabled {
  opacity: 0.4;
  pointer-events: none;
}
.empty-table {
  text-align: center;
  color: #6b7280;
  padding: 20px;
  font-style: italic;
}
/* SỬA */
:deep(.vs__dropdown-toggle:hover) {
  border-color: #c8d0dc !important;
}
:deep(.vs--open .vs__dropdown-toggle) {
  border-color: var(--color-primary) !important;
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12) !important;
}
:deep(.vs__dropdown-option--highlight) {
  background: #f3f4f6 !important;
  color: #d73737 !important;
}
:deep(.vs__dropdown-option--selected) {
  background: #f3f5f9 !important;
  color: #d73737 !important;
}
.modern-input:hover {
  border-color: #c8d0dc;
}
.modern-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12);
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

.input-invalid-vs :deep(.vs__dropdown-toggle) {
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

.alert-product-changed {
  background: rgba(220, 38, 38, 0.05);
  border: 1px solid rgba(220, 38, 38, 0.15);
  border-radius: 8px;
  padding: 8px 14px;
  color: #b91c1c;
  font-size: 13px;
  line-height: 1.45;
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.04);
  text-align: left;
}
.alert-product-changed i {
  font-size: 18px;
  color: #dc2626;
  flex-shrink: 0;
}
</style>
