<script setup>
import { useRouter, onBeforeRouteLeave } from "vue-router";
import { reactive, ref, onMounted, onUnmounted, computed, watch } from "vue";
import iconSave from "@/assets/icons/add.png";
import iconBack from "@/assets/icons/prev.png";
import { useUtils } from "@/utils/helper";
import { getAllNuocLau } from "@/api/admin/nuoclau/nuocLauApi";
import { addSanPham, getAllMonLe } from "@/api/admin/sanpham/sanPhamApi";
import { addSetLau } from "@/api/admin/lau/setLauApi";
import { getAllDanhMucMonLe } from "@/api/admin/danhmuc/danhMucApi";
import { getAllDonVi } from "@/api/admin/thuoctinh/donViApi";
import { getDinhLuongByDanhMuc } from "@/api/admin/thuoctinh/dinhLuongApi";
import { getAllThamSoCustomer } from "@/api/customer/datbanonline/datBanOnlineApi";
const { notify, nprogress, formatCurrency, parseCurrency } = useUtils();
const router = useRouter();
const isSubmittingOrCanceling = ref(false);

const danhSachThamSo = ref([]);

const loadThamSo = async () => {
  try {
    const res = await getAllThamSoCustomer();
    danhSachThamSo.value = res.data;
  } catch (error) {
    console.log("LỖI THAM SỐ", error);
  }
};

const getThamSo = (key) => {
  return danhSachThamSo.value.find((x) => x.maThamSo === key)?.giaTri;
};

// Computed để handle format tiền tệ real-time
const formattedGia = computed({
  get() {
    // Khi hiển thị: Chuyển số 1000 -> "1,000"
    return formSetLau.gia ? formSetLau.gia.toLocaleString("vi-VN") : "";
  },
  set(newValue) {
    // Khi nhập: Loại bỏ tất cả ký tự không phải số rồi lưu vào state gốc
    const numberValue = Number(newValue.replace(/\D/g, ""));
    formSetLau.gia = numberValue;
  },
});

// State quản lý
const nuocLauList = ref([]);
const danhSachNguyenLieu = ref([]);
const danhMucMonLe = ref([]);
const showModalMonLe = ref(false);
const searchText = ref("");
const filterDanhMuc = ref("");
const filterKhoangGia = ref("");

const hasProductChanged = ref(false);
const coMonNgungKinhDoanh = computed(() => {
  return formSetLau.thanhPhans.some((item) => item.trangThaiXoa);
});
const coNuocLauNgungKinhDoanh = computed(() => {
  if (!formSetLau.idNuocLau) return false;
  const nl = nuocLauList.value.find((item) => item.id === formSetLau.idNuocLau);
  return nl ? Boolean(nl.trangThaiXoa) : false;
});

// Hàm các để add nhanh món ăn
const showModalAddNhanh = ref(false);
const danhSachDinhLuongNhanh = ref([]);

const formAddNhanh = reactive({
  tenSanPham: "",
  idDanhMuc: "",
  moTa: "",
  hinhAnh: null,
  chiTietSPS: [{ gia: 0, idDinhLuong: null }],
});

const errorsNhanh = reactive({
  tenSanPham: "",
  idDanhMuc: "",
  hinhAnh: "",
  chiTiet: [],
});

const handleDanhMucChangeNhanh = async (idDM) => {
  if (!idDM) {
    danhSachDinhLuongNhanh.value = [];
    return;
  }
  const res = await getDinhLuongByDanhMuc(idDM);
  danhSachDinhLuongNhanh.value = res.data;
};

const themDongNhanh = () =>
  formAddNhanh.chiTietSPS.push({ gia: 0, idDinhLuong: null });
const xoaDongNhanh = (index) => {
  if (formAddNhanh.chiTietSPS.length > 1)
    formAddNhanh.chiTietSPS.splice(index, 1);
};

const saveNhanhSanPham = async () => {
  let hasError = false;
  errorsNhanh.tenSanPham = "";
  errorsNhanh.idDanhMuc = "";
  errorsNhanh.hinhAnh = "";
  errorsNhanh.chiTiet = formAddNhanh.chiTietSPS.map(() => ({
    gia: "",
    idDinhLuong: "",
  }));

  if (!formAddNhanh.hinhAnh) {
    errorsNhanh.hinhAnh = "Vui lòng chọn ảnh minh họa";
    hasError = true;
  }
  if (!formAddNhanh.tenSanPham || formAddNhanh.tenSanPham.trim() === "") {
    errorsNhanh.tenSanPham = "Vui lòng nhập tên món ăn";
    hasError = true;
  }
  if (!formAddNhanh.idDanhMuc) {
    errorsNhanh.idDanhMuc = "Vui lòng chọn danh mục";
    hasError = true;
  }
  formAddNhanh.chiTietSPS.forEach((item, index) => {
    if (item.gia === "" || item.gia === null) {
      errorsNhanh.chiTiet[index].gia = "Vui lòng nhập giá";
      hasError = true;
    } else if (item.gia < 1000) {
      errorsNhanh.chiTiet[index].gia = "Giá phải lớn hơn hoặc bằng 1,000";
      hasError = true;
    } else if (item.gia > 100000000) {
      errorsNhanh.chiTiet[index].gia = "Giá phải nhỏ hơn 100,000,000";
      hasError = true;
    }
    if (!item.idDinhLuong) {
      errorsNhanh.chiTiet[index].idDinhLuong = "Vui lòng chọn định lượng";
      hasError = true;
    }
  });

  if (hasError) return;

  if (!(await notify.confirm("Xác nhận thêm món ăn này?"))) return;
  nprogress.start();
  try {
    const formData = new FormData();
    // Đóng gói dữ liệu giống file gốc
    formAddNhanh.chiTietSPS.forEach((item, index) => {
      formData.append(`requests[${index}].tenSanPham`, formAddNhanh.tenSanPham);
      formData.append(`requests[${index}].idDanhMuc`, formAddNhanh.idDanhMuc);
      formData.append(`requests[${index}].moTa`, formAddNhanh.moTa);
      formData.append(`requests[${index}].gia`, item.gia);
      formData.append(`requests[${index}].idDinhLuong`, item.idDinhLuong);
    });
    if (formAddNhanh.hinhAnh) formData.append("file", formAddNhanh.hinhAnh);

    const res = await addSanPham(formData);
    notify.success("Đã thêm món mới vào hệ thống!");

    // Sau khi thêm xong, load lại danh sách món lẻ ở modal ngoài
    const spRes = await getAllMonLe();
    danhSachNguyenLieu.value = spRes.data;

    // Đóng modal add nhanh
    showModalAddNhanh.value = false;
    // Reset form
    Object.assign(formAddNhanh, {
      tenSanPham: "",
      idDanhMuc: "",
      moTa: "",
      hinhAnh: null,
      chiTietSPS: [{ gia: 0, idDinhLuong: null }],
    });
  } catch (e) {
    notify.error("Lỗi khi thêm nhanh");
  } finally {
    nprogress.done();
  }
};

const formSetLau = reactive({
  tenSanPham: "",
  idNuocLau: "",
  gia: 0,
  moTa: "",
  hinhAnh: null,
  thanhPhans: [],
});

import { connectWebSocket, disconnectWebSocket } from "@/utils/websocket";

let wsTimeout = null;

const capNhatTrangThaiThanhPhan = async () => {
  const selectedItems = formSetLau.thanhPhans;
  if (!selectedItems.length) return;

  try {
    const [spRes, nlRes] = await Promise.all([getAllMonLe(), getAllNuocLau()]);
    const activeMons = spRes.data || [];
    nuocLauList.value = nlRes.data || [];
    const mapMonLe = new Map(activeMons.map((p) => [String(p.id), p]));

    selectedItems.forEach((item) => {
      const mon = mapMonLe.get(String(item.idSanPham));
      if (mon) {
        item.trangThaiXoa = false;
        if (mon.gia !== undefined && mon.gia !== null) {
          item.donGia = mon.gia;
        }
      } else {
        item.trangThaiXoa = true;
      }
    });
  } catch (err) {
    console.error("Lỗi đồng bộ trạng thái thành phần set lẩu:", err);
  }
};

const handleWsMessage = (msg) => {
  console.log(">>> [ThemSetLau WS] Received message:", msg);
  if (msg?.type === "SAN_PHAM_UPDATE") {
    if (wsTimeout) clearTimeout(wsTimeout);
    wsTimeout = setTimeout(async () => {
      const spRes = await getAllMonLe();
      danhSachNguyenLieu.value = spRes.data;

      await capNhatTrangThaiThanhPhan();
      console.log(">>> [ThemSetLau WS] Synced.");

      hasProductChanged.value = true;
      notify.confirm("Món ăn có sự thay đổi vui lòng kiểm tra thông tin trước khi xác nhận");
    }, 150);
  }
};

const handleBeforeUnload = (e) => {
  if (isSubmittingOrCanceling.value || !hasUnsavedChanges.value) return;
  e.preventDefault();
  e.returnValue = "";
};

onMounted(async () => {
  window.addEventListener("beforeunload", handleBeforeUnload);
  try {
    const [nlRes, spRes, dmRes] = await Promise.all([
      getAllNuocLau(),
      getAllMonLe(),
      getAllDanhMucMonLe(),
    ]);
    nuocLauList.value = nlRes.data;
    danhSachNguyenLieu.value = spRes.data;
    danhMucMonLe.value = dmRes.data;

    loadThamSo();
    connectWebSocket(null, handleWsMessage);
  } catch (err) {
    console.error("API ERROR:", err.response?.data || err);
    console.error("Lỗi load dữ liệu:", err);
  }
});

onUnmounted(() => {
  if (wsTimeout) clearTimeout(wsTimeout);
  disconnectWebSocket(handleWsMessage);
  window.removeEventListener("beforeunload", handleBeforeUnload);
});

const hasUnsavedChanges = computed(() => {
  return (
    formSetLau.tenSanPham ||
    formSetLau.idNuocLau ||
    formSetLau.gia > 0 ||
    formSetLau.moTa ||
    formSetLau.hinhAnh !== null ||
    formSetLau.thanhPhans.length > 0
  );
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

const filteredNguyenLieu = computed(() => {
  const kw = searchText.value.trim().replace(/\s+/g, " ").toLowerCase();

  return danhSachNguyenLieu.value.filter((sp) => {
    if (sp.laSet) return false;

    const tenSanPham = (sp.tenSanPham || "")
      .trim()
      .replace(/\s+/g, " ")
      .toLowerCase();
    const matchKeyword = !kw || tenSanPham.includes(kw);

    const matchDanhMuc =
      !filterDanhMuc.value || sp.idDanhMuc === filterDanhMuc.value;

    let matchGia = true;
    if (filterKhoangGia.value === "low") {
      matchGia = Number(sp.gia) < 100000;
    } else if (filterKhoangGia.value === "high") {
      matchGia = Number(sp.gia) > 100000;
    }

    return matchKeyword && matchDanhMuc && matchGia;
  });
});

const activeNuocLauList = computed(() => {
  return nuocLauList.value.filter((nl) => !nl.trangThaiXoa);
});

const phuThuNuocLau = computed(() => {
  const nl = nuocLauList.value.find((item) => item.id === formSetLau.idNuocLau);
  return nl ? Number(nl.giaPhuThu) : 0;
});

const giaGoiY = computed(() => {
  const tongNguyenLieu = formSetLau.thanhPhans
    .filter((item) => !item.trangThaiXoa)
    .reduce((total, item) => total + item.soLuong * item.donGia, 0);

  const tyLe = Number(getThamSo("TY_LE_GIA_BAN_LAU")) || 1;

  return (tongNguyenLieu + phuThuNuocLau.value) * tyLe;
});

const errors = reactive({
  tenSanPham: "",
  gia: "",
  thanhPhans: "",
  idNuocLau: "",
  moTa: "",
  hinhAnh: "",
});

const loiGiaBan = ref("");

watch(
  () => [formSetLau.gia, giaGoiY.value],
  ([gia, goiY]) => {
    if (gia > 0 && goiY > 0 && gia < goiY) {
      loiGiaBan.value = `Giá bán phải lớn hơn hoặc bằng giá gợi ý`;
    } else {
      loiGiaBan.value = "";
    }
  },
  { immediate: true },
);

const selectNguyenLieu = (sp) => {
  const exitsted = formSetLau.thanhPhans.find(
    (item) => item.idSanPham === sp.id,
  );
  const dinhLuongHienThi =
    sp.giaTriDinhLuong || (sp.dinhLuong ? sp.dinhLuong.giaTri : "N/A");

  if (exitsted) {
    exitsted.soLuong++;
  } else {
    formSetLau.thanhPhans.push({
      idSanPham: sp.id,
      tenSanPham: sp.tenSanPham,
      giaTriDinhLuong: dinhLuongHienThi,
      hinhAnh: sp.hinhAnh,
      soLuong: 1,
      donGia: sp.gia,
    });
  }
  notify.success(`Đã thêm ${sp.tenSanPham}`);
};

const xoaThanhPhan = (index) => formSetLau.thanhPhans.splice(index, 1);

const apDungGiaGoiY = () => (formSetLau.gia = giaGoiY.value);

const chonHinhAnh = (e) => {
  const file = e.target.files[0];
  if (file) {
    formSetLau.hinhAnh = file;
    errors.hinhAnh = "";
  }
};
const chonAnhNhanh = (e) => {
  const file = e.target.files[0];
  if (!file) return;
  formAddNhanh.hinhAnh = file;
};

const saveSetLau = async () => {
  if (coMonNgungKinhDoanh.value) {
    notify.error(
      "Vui lòng loại bỏ các món ăn ngừng kinh doanh ra khỏi Set lẩu trước khi lưu!",
    );
    return;
  }

  if (coNuocLauNgungKinhDoanh.value) {
    notify.error(
      "Loại nước lẩu đã chọn đã ngừng kinh doanh. Vui lòng chọn loại nước lẩu khác!",
    );
    return;
  }

  errors.tenSanPham = "";
  errors.gia = "";
  errors.thanhPhans = "";
  errors.idNuocLau = "";
  errors.moTa = "";
  errors.hinhAnh = "";

  let hasError = false;

  if (!formSetLau.hinhAnh) {
    errors.hinhAnh = "Vui lòng chọn hình ảnh cho Set lẩu";
    hasError = true;
  }

  if (!formSetLau.tenSanPham) {
    errors.tenSanPham = "Vui lòng nhập tên Set lẩu";
    hasError = true;
  }

  if (formSetLau.gia <= 0) {
    errors.gia = "Vui lòng nhập giá bán hợp lệ";
    hasError = true;
  }

  if (formSetLau.gia > 0 && formSetLau.gia < giaGoiY.value) {
    loiGiaBan.value = `Giá bán không được nhỏ hơn giá gợi ý`;
    hasError = true;
  }

  if (formSetLau.thanhPhans.length === 0) {
    errors.thanhPhans = "Set lẩu phải có ít nhất 1 thành phần";
    hasError = true;
  }

  if (!formSetLau.idNuocLau) {
    errors.idNuocLau = "Vui lòng chọn loại nước lẩu";
    hasError = true;
  }

  if (formSetLau.moTa && formSetLau.moTa.length > 255) {
    errors.moTa = "Mô tả không được vượt quá 255 ký tự";
    hasError = true;
  }

  if (hasError) return;

  if (!(await notify.confirm("Xác nhận lưu Set lẩu này?"))) return;
  nprogress.start();
  try {
    const formData = new FormData();

    formData.append("tenSanPham", formSetLau.tenSanPham);
    formData.append("gia", formSetLau.gia);
    formData.append("idNuocLau", formSetLau.idNuocLau);
    formData.append("moTa", formSetLau.moTa);
    formSetLau.thanhPhans.forEach((tp, index) => {
      formData.append(`thanhPhans[${index}].idSanPham`, tp.idSanPham);
      formData.append(`thanhPhans[${index}].soLuong`, tp.soLuong);
    });
    if (formSetLau.hinhAnh) formData.append("file", formSetLau.hinhAnh);

    const response = await addSetLau(formData);

    console.log("SUCCESS:", response.data);

    notify.success("Thêm Set Lẩu thành công!");

    isSubmittingOrCanceling.value = true;
    router.push("/admin/set-lau/hien-thi");
  } catch (e) {
    notify.error(
      e.response?.data?.message || e.response?.data || "Lỗi khi lưu dữ liệu",
    );
    console.log("ERROR STATUS:", e.response?.status);
    console.log("ERROR DATA:", e.response?.data);
    console.log("ERROR FULL:", e);
  } finally {
    nprogress.done();
  }
};

const imagePreview = computed(() =>
  formSetLau.hinhAnh instanceof File
    ? URL.createObjectURL(formSetLau.hinhAnh)
    : null,
);

const previewNhanh = computed(() => {
  if (!formAddNhanh.hinhAnh) return null;
  return URL.createObjectURL(formAddNhanh.hinhAnh);
});

///////////// PHÂN TRANG
// Phân trang modal chọn món
const modalPage = ref(1);
const modalPageSize = 5;

const pagedNguyenLieu = computed(() => {
  const start = (modalPage.value - 1) * modalPageSize;
  return filteredNguyenLieu.value.slice(start, start + modalPageSize);
});

const modalTotalPages = computed(() =>
  Math.ceil(filteredNguyenLieu.value.length / modalPageSize),
);

const modalPageList = computed(() => {
  const total = modalTotalPages.value;
  if (total <= 5) return Array.from({ length: total }, (_, i) => i + 1);
  const pages = [1, 2, 3];
  if (total > 4) pages.push("…");
  pages.push(total);
  return pages;
});

// Reset page khi search thay đổi
watch([searchText, filterDanhMuc, filterKhoangGia], () => {
  modalPage.value = 1;
});

// Phân trang thành phần đã chọn
const thanhPhanPage = ref(1);
const thanhPhanPageSize = 5;

const pagedThanhPhans = computed(() => {
  const start = (thanhPhanPage.value - 1) * thanhPhanPageSize;
  return formSetLau.thanhPhans.slice(start, start + thanhPhanPageSize);
});

const thanhPhanTotalPages = computed(() =>
  Math.ceil(formSetLau.thanhPhans.length / thanhPhanPageSize),
);

const thanhPhanPageList = computed(() => {
  const total = thanhPhanTotalPages.value;
  if (total <= 5) return Array.from({ length: total }, (_, i) => i + 1);
  const pages = [1, 2, 3];
  if (total > 4) pages.push("…");
  pages.push(total);
  return pages;
});
const getImgUrl = (path) =>
  path ? `http://localhost:8080/uploads/${path}` : "";
</script>

<template>
  <div class="modern-container">
    <div class="inner-content">
      <header class="box-header-card">
        <div class="box-header-flex">
          <div class="box-header">
            <h2 class="main-title">
              <span class="icon-wrapper">
                <i
                  class="bi bi-layers-fill"
                  style="color: rgb(225, 29, 72)"
                ></i>
              </span>
              Thiết lập set lẩu
            </h2>
            <p>Tạo combo lẩu kèm danh sách nguyên liệu và giá ưu đãi</p>
          </div>
          <button class="icon-btn-back" @click="router.back()">
            <img :src="iconBack" />
          </button>
        </div>
      </header>

      <main class="main-content">
        <aside class="side-panel">
          <div class="card shadow-sm">
            <h3 class="card-title">
              Ảnh đại diện Set <span style="color: red">*</span>
            </h3>
            <div class="image-upload-zone">
              <div v-if="imagePreview" class="preview-wrapper">
                <img :src="imagePreview" />
                <button class="remove-badge" @click="formSetLau.hinhAnh = null">
                  ×
                </button>
              </div>
              <div v-else class="upload-placeholder">
                <i
                  class="bi bi-image"
                  style="font-size: 2.5rem; color: #cbd5e1"
                ></i>
                <p>Ảnh Set Lẩu</p>
              </div>
              <input
                type="file"
                id="main-upload"
                hidden
                @change="chonHinhAnh"
                accept="image/*"
              />
              <label for="main-upload" class="mini-upload-btn"
                ><i class="bi bi-camera"></i> Chọn ảnh</label
              >
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

          <div class="card shadow-sm mt-4">
            <h3 class="card-title">
              Phân loại lẩu<span style="color: red"> *</span>
            </h3>
            <div class="form-group mb-3">
              <select
                v-model="formSetLau.idNuocLau"
                class="modern-input"
                :class="{ 'input-invalid': errors.idNuocLau || coNuocLauNgungKinhDoanh }"
                @change="errors.idNuocLau = ''"
              >
                <option value="">Chọn loại lẩu</option>
                <option v-for="nl in activeNuocLauList" :key="nl.id" :value="nl.id">
                  {{ nl.tenNuocLau }}
                </option>
              </select>
              <transition name="shake">
                <p
                  v-if="errors.idNuocLau || coNuocLauNgungKinhDoanh"
                  class="error-hint"
                  style="width: max-content; margin-top: 5px"
                >
                  <i class="bi bi-exclamation-circle"></i>
                  {{ errors.idNuocLau || "Nước lẩu đã ngừng kinh doanh" }}
                </p>
              </transition>
            </div>
          </div>
        </aside>

        <section class="main-panel">
          <div class="card shadow-sm">
            <div class="form-group mb-4">
              <label class="bold-label"
                >Tên Set Lẩu <span class="text-danger">*</span></label
              >
              <input
                v-model="formSetLau.tenSanPham"
                class="modern-input"
                :class="{ 'input-invalid': errors.tenSanPham }"
                placeholder="VD: Set Lẩu Thái 4 Người Đặc Biệt"
                @input="errors.tenSanPham = ''"
              />
              <transition name="shake">
                <p
                  v-if="errors.tenSanPham"
                  class="error-hint"
                  style="width: max-content; margin-top: 5px"
                >
                  <i class="bi bi-exclamation-circle"></i>
                  {{ errors.tenSanPham }}
                </p>
              </transition>
            </div>

            <div class="ingredients-section">
              <div
                class="d-flex justify-content-between align-items-center mb-3"
              >
                <label class="bold-label m-0"
                  >Thành phần ({{ formSetLau.thanhPhans.length }})</label
                >
                <button class="btn-ghost-add" @click="showModalMonLe = true">
                  <i class="bi bi-plus"></i>
                  Chọn món
                </button>
              </div>
              <transition name="shake">
                <p
                  v-if="errors.thanhPhans"
                  class="error-hint mb-3"
                  style="width: max-content"
                >
                  <i class="bi bi-exclamation-circle"></i>
                  {{ errors.thanhPhans }}
                </p>
              </transition>

              <div class="selected-items-wrapper">
                <div
                  v-if="formSetLau.thanhPhans.length === 0"
                  class="empty-list"
                >
                  <i class="bi bi-basket2"></i> Chưa có món nào được thêm
                </div>
                 <div
                  v-for="(tp, index) in pagedThanhPhans"
                  :key="index"
                  class="item-row-card"
                  :class="{ 'ngung-kinh-doanh': tp.trangThaiXoa }"
                >
                  <div class="item-main-info">
                    <img :src="getImgUrl(tp.hinhAnh)" class="item-thumb" />
                    <div class="item-text">
                      <p class="item-name">{{ tp.tenSanPham }}</p>
                      <span class="badge-dl-mini">{{
                        tp.giaTriDinhLuong
                      }}</span>
                      <div v-if="tp.trangThaiXoa" class="status-ngung-kinh-doanh-text text-danger fw-bold" style="font-size: 11px; margin-top: 4px;">
                        <i class="ri-error-warning-fill"></i> Món ăn ngừng kinh doanh
                      </div>
                    </div>
                  </div>
                  <div class="item-qty-box">
                    <span class="mini-label">Số lượng</span>
                    <input
                      type="number"
                      v-model.number="tp.soLuong"
                      class="qty-input-v2"
                      min="1"
                      :disabled="tp.trangThaiXoa"
                    />
                  </div>
                  <div class="item-price-box">
                    <span class="mini-label">Đơn giá lẻ</span>
                    <p class="price-val">{{ tp.donGia.toLocaleString() }}đ</p>
                  </div>
                  <button
                    class="btn-remove-item"
                    @click="
                      xoaThanhPhan(
                        (thanhPhanPage - 1) * thanhPhanPageSize + index,
                      )
                    "
                  >
                    <i class="bi bi-trash3"></i>
                  </button>
                </div>
              </div>
              <div v-if="thanhPhanTotalPages > 1" class="pagination mt-2">
                <button
                  :disabled="thanhPhanPage === 1"
                  @click="thanhPhanPage--"
                  class="pg-btn pg-arrow"
                >
                  <i class="ri-arrow-left-s-line"></i>
                </button>
                <template v-for="p in thanhPhanPageList" :key="p">
                  <button v-if="p === '…'" class="pg-btn pg-ellipsis" disabled>
                    ···
                  </button>
                  <button
                    v-else
                    class="pg-btn"
                    :class="{ active: p === thanhPhanPage }"
                    @click="thanhPhanPage = p"
                  >
                    {{ p }}
                  </button>
                </template>
                <button
                  :disabled="thanhPhanPage === thanhPhanTotalPages"
                  @click="thanhPhanPage++"
                  class="pg-btn pg-arrow"
                >
                  <i class="ri-arrow-right-s-line"></i>
                </button>
              </div>
            </div>

            <div class="price-container-compact mt-4">
              <div class="price-summary-box">
                <div class="summary-row">
                  <span class="summary-label">Phụ thu nước lẩu:</span>
                  <span class="value-suggested">
                    {{ phuThuNuocLau.toLocaleString() }} đ
                  </span>
                </div>
                <div class="summary-row">
                  <span class="summary-label">Giá gợi ý</span>
                  <div class="summary-value-group">
                    <span class="value-suggested"
                      >{{ giaGoiY.toLocaleString() }} đ</span
                    >

                    <button class="btn-apply-compact" @click="apDungGiaGoiY()">
                      <i class="bi bi-check"></i>
                      Áp dụng
                    </button>
                  </div>
                </div>

                <div class="summary-row highlight-row" style="flex-wrap: wrap">
                  <span class="summary-label bold-label"
                    >Giá bán thực tế <span class="text-danger">*</span>:</span
                  >
                  <div class="price-input-col">
                    <div class="input-price-wrapper">
                      <input
                        type="text"
                        v-model="formattedGia"
                        class="compact-price-input"
                        :class="{ 'input-invalid': loiGiaBan }"
                        placeholder="0"
                      />
                      <span class="currency-unit">đ</span>
                    </div>
                  </div>
                  <div
                    style="
                      width: 100%;
                      display: flex;
                      justify-content: flex-end;
                      margin-top: 5px;
                    "
                    v-if="loiGiaBan || errors.gia"
                  >
                    <transition name="shake">
                      <p class="error-hint mb-0" style="width: max-content">
                        <i class="bi bi-exclamation-circle"></i>
                        {{ loiGiaBan || errors.gia }}
                      </p>
                    </transition>
                  </div>
                </div>
              </div>
            </div>

            <div class="form-group mt-4">
              <label class="bold-label">Mô tả chi tiết</label>
              <textarea
                v-model="formSetLau.moTa"
                rows="3"
                class="modern-input"
                :class="{ 'input-invalid': errors.moTa }"
                @input="errors.moTa = ''"
                placeholder="Ghi chú thành phần hoặc ưu đãi..."
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

      <footer class="footer-sticky">
        <div class="footer-left-content">
          <p class="footer-hint">
            * Đảm bảo đã chọn đủ nguyên liệu và định lượng cho Set.
          </p>
          <div v-if="hasProductChanged" class="alert-product-changed fw-bold">
            <i class="ri-error-warning-fill"></i>
            <span>Món ăn có sự thay đổi vui lòng kiểm tra thông tin trước khi xác nhận</span>
          </div>
        </div>

        <div class="footer-btns">
          <button class="btn-cancel" @click="router.back()">Hủy bỏ</button>
          <button class="btn-submit" @click="saveSetLau" :disabled="coMonNgungKinhDoanh || coNuocLauNgungKinhDoanh" :class="{ 'btn-disabled': coMonNgungKinhDoanh || coNuocLauNgungKinhDoanh }">
            <i class="bi bi-check2-all"></i>
            Xác nhận
            <div class="btn-light-sweep"></div>
          </button>
        </div>
      </footer>
    </div>

    <!-- MODAL CHỌN MÓN LẺ -->
    <Teleport to="body">
      <div
        v-if="showModalMonLe"
        class="modal-overlay"
        @click.self="showModalMonLe = false"
      >
        <div class="modal-card animate-pop modal-xl">
          <div class="modal-header">
            <div>
              <h3 class="modal-title">Thêm món vào Set lẩu</h3>
              <p class="modal-sub">
                Chọn từ danh sách sản phẩm lẻ hoặc thêm món mới
              </p>
            </div>
            <button class="close-modal" @click="showModalMonLe = false">
              ×
            </button>
          </div>

          <div class="modal-body">
            <div class="filter-grid-system mb-4">
              <div class="filter-col">
                <label class="bold-label-v2">Tìm tên món</label>
                <div class="search-bar-v2">
                  <i class="bi bi-search"></i>
                  <input
                    v-model="searchText"
                    class="input-light"
                    placeholder="Nhập tên món..."
                  />
                </div>
              </div>

              <div class="filter-col">
                <label class="bold-label-v2">Danh mục</label>
                <select class="input-light" v-model="filterDanhMuc">
                  <option value="">Tất cả danh mục</option>
                  <option
                    v-for="dm in danhMucMonLe"
                    :key="dm.id"
                    :value="dm.id"
                  >
                    {{ dm.tenDanhMuc }}
                  </option>
                </select>
              </div>
              <div class="filter-col">
                <label class="bold-label-v2">Khoảng giá</label>
                <select class="input-light" v-model="filterKhoangGia">
                  <option value="">Mọi mức giá</option>
                  <option value="low">Dưới 100.000đ</option>
                  <option value="high">Trên 100.000đ</option>
                </select>
              </div>

              <div class="filter-col create-col">
                <div class="create-new-item" @click="showModalAddNhanh = true">
                  <span class="icon">+</span>
                  Thêm món mới
                </div>
              </div>
            </div>
            <div class="selection-list-scroll">
              <!-- CÓ DỮ LIỆU -->
              <template v-if="pagedNguyenLieu.length > 0">
                <div
                  v-for="sp in pagedNguyenLieu"
                  :key="sp.id"
                  class="selection-card-v2"
                >
                  <div class="item-left-group">
                    <img
                      :src="getImgUrl(sp.hinhAnh)"
                      class="selection-img-v2"
                    />

                    <div class="selection-info">
                      <p class="sel-name-v2">
                        {{ sp.tenSanPham }}
                      </p>

                      <span class="sel-sub-text">
                        Danh mục: {{ sp.tenDanhMuc }}
                      </span>
                    </div>
                  </div>

                  <div class="item-mid-group">
                    <div class="meta-box">
                      <span class="mini-label-v2">Định lượng</span>

                      <p class="meta-val-v2">
                        {{ sp.giaTri }} {{ sp.tenDonVi }}
                      </p>
                    </div>
                  </div>

                  <div class="item-right-group">
                    <div class="price-box-v2">
                      <span class="mini-label-v2 text-end"> Đơn giá </span>

                      <p class="sel-price-v2">
                        {{ Number(sp.gia).toLocaleString() }}đ
                      </p>
                    </div>

                    <button
                      class="btn-plus-circle"
                      @click="selectNguyenLieu(sp)"
                    >
                      <i class="ri-add-circle-fill"></i>
                    </button>
                  </div>
                </div>
              </template>

              <!-- KHÔNG CÓ DỮ LIỆU -->
              <div v-else class="summary-box-v2 mt-4 empty-summary">
                <div class="empty-content">
                  <i class="bi bi-egg-fried"></i>
                  <p>Không tìm thấy món ăn nào</p>
                </div>
              </div>
              <div v-if="modalTotalPages > 1" class="pagination mt-3">
                <button
                  :disabled="modalPage === 1"
                  @click="modalPage--"
                  class="pg-btn pg-arrow"
                >
                  <i class="ri-arrow-left-s-line"></i>
                </button>
                <template v-for="p in modalPageList" :key="p">
                  <button v-if="p === '…'" class="pg-btn pg-ellipsis" disabled>
                    ···
                  </button>
                  <button
                    v-else
                    class="pg-btn"
                    :class="{ active: p === modalPage }"
                    @click="modalPage = p"
                  >
                    {{ p }}
                  </button>
                </template>
                <button
                  :disabled="modalPage === modalTotalPages"
                  @click="modalPage++"
                  class="pg-btn pg-arrow"
                >
                  <i class="ri-arrow-right-s-line"></i>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- MODAL ADD NHANH MÓN ĂN -->
    <Teleport to="body">
      <div
        v-if="showModalAddNhanh"
        class="modal-overlay"
        style="z-index: 10000"
      >
        <div
          class="modal-card animate-pop modal-xl"
          style="max-width: 1100px; border-radius: 7px"
        >
          <div
            class="modal-header"
            style="padding: 20px 30px; border-bottom: 1px solid #f1f5f9"
          >
            <div>
              <h3 class="modal-title" style="font-weight: 700; font-size: 30px">
                Thêm mới món ăn nhanh
              </h3>
              <p class="modal-sub" style="color: #64748b; font-size: 14px">
                Khởi tạo sản phẩm nhanh để thêm vào set lẩu
              </p>
            </div>
            <button class="close-modal" @click="showModalAddNhanh = false">
              ×
            </button>
          </div>

          <div class="modal-body" style="padding: 30px">
            <div class="row g-4">
              <div class="col-md-4 border-end">
                <div class="form-group">
                  <label class="bold-label">Hình ảnh</label>
                  <div class="image-upload-zone">
                    <div
                      v-if="formAddNhanh.hinhAnh"
                      class="preview-wrapper"
                      style="border-radius: 15px"
                    >
                      <img
                        v-if="previewNhanh"
                        :src="previewNhanh"
                        style="width: 100%; height: 100%; object-fit: cover"
                      />
                      <button
                        class="remove-badge"
                        @click="formAddNhanh.hinhAnh = null"
                      >
                        ×
                      </button>
                    </div>
                    <div v-else>
                      <input
                        type="file"
                        @change="chonAnhNhanh"
                        hidden
                        id="add-nhanh-img"
                      />
                      <label
                        for="add-nhanh-img"
                        class="upload-placeholder"
                        style="
                          aspect-ratio: 1;
                          border-radius: 15px;
                          border: 2px dashed #e2e8f0;
                        "
                      >
                        <i
                          class="bi bi-camera"
                          style="font-size: 1.5rem; color: #cbd5e1"
                        ></i>
                        <span
                          class="mini-upload-btn"
                          style="
                            background: transparent;
                            border: none;
                            color: #94a3b8;
                          "
                          >Chọn ảnh</span
                        >
                      </label>
                    </div>
                  </div>
                  <transition name="shake">
                    <p
                      v-if="errorsNhanh.hinhAnh"
                      class="error-hint mt-2"
                      style="width: max-content"
                    >
                      <i class="bi bi-exclamation-circle"></i>
                      {{ errorsNhanh.hinhAnh }}
                    </p>
                  </transition>
                </div>

                <div class="form-group mt-3">
                  <label class="bold-label"
                    >Danh mục <span class="text-danger">*</span></label
                  >
                  <select
                    v-model="formAddNhanh.idDanhMuc"
                    @change="
                      handleDanhMucChangeNhanh(formAddNhanh.idDanhMuc);
                      errorsNhanh.idDanhMuc = '';
                    "
                    class="modern-input"
                    :class="{ 'input-invalid': errorsNhanh.idDanhMuc }"
                  >
                    <option value="">Chọn danh mục</option>
                    <option
                      v-for="dm in danhMucMonLe"
                      :key="dm.id"
                      :value="dm.id"
                    >
                      {{ dm.tenDanhMuc }}
                    </option>
                  </select>
                  <transition name="shake">
                    <p
                      v-if="errorsNhanh.idDanhMuc"
                      class="error-hint mt-1"
                      style="width: max-content"
                    >
                      <i class="bi bi-exclamation-circle"></i>
                      {{ errorsNhanh.idDanhMuc }}
                    </p>
                  </transition>
                </div>
              </div>

              <div class="col-md-8">
                <div class="form-group">
                  <label class="bold-label"
                    >Tên sản phẩm <span class="text-danger">*</span></label
                  >
                  <input
                    v-model="formAddNhanh.tenSanPham"
                    @input="errorsNhanh.tenSanPham = ''"
                    class="modern-input"
                    :class="{ 'input-invalid': errorsNhanh.tenSanPham }"
                    placeholder="Nhập tên món lẻ..."
                  />
                  <transition name="shake">
                    <p
                      v-if="errorsNhanh.tenSanPham"
                      class="error-hint mt-1"
                      style="width: max-content"
                    >
                      <i class="bi bi-exclamation-circle"></i>
                      {{ errorsNhanh.tenSanPham }}
                    </p>
                  </transition>
                </div>

                <div class="mt-4">
                  <div
                    class="d-flex justify-content-between align-items-center mb-3"
                  >
                    <label class="bold-label m-0">Quy cách & Giá bán</label>

                    <div class="create-new-item" @click="themDongNhanh">
                      <span class="icon">+</span>
                      Thêm quy cách
                    </div>
                  </div>

                  <div
                    class="scroll-area"
                    style="max-height: 350px; overflow-y: auto"
                  >
                    <div
                      v-for="(item, index) in formAddNhanh.chiTietSPS"
                      :key="index"
                      class="item-row-card"
                      style="
                        display: grid;
                        grid-template-columns: 1fr 1fr 40px;
                        gap: 15px;
                        background: #fff;
                        border: 1px solid #f1f5f9;
                        padding: 15px;
                        border-radius: 12px;
                        margin-bottom: 10px;
                      "
                    >
                      <div>
                        <span class="mini-label"
                          >Giá bán <span class="text-danger">*</span></span
                        >
                        <input
                          type="text"
                          :value="formatCurrency(item.gia)"
                          @input="
                            item.gia = parseCurrency($event.target.value);
                            errorsNhanh.chiTiet?.[index] &&
                              (errorsNhanh.chiTiet[index].gia = '');
                          "
                          class="modern-input"
                          :class="{
                            'input-invalid': errorsNhanh.chiTiet?.[index]?.gia,
                          }"
                          placeholder="0"
                        />
                        <transition name="shake">
                          <p
                            v-if="errorsNhanh.chiTiet?.[index]?.gia"
                            class="error-hint mt-1"
                          >
                            <i class="bi bi-exclamation-circle"></i>
                            {{ errorsNhanh.chiTiet[index].gia }}
                          </p>
                        </transition>
                      </div>

                      <div>
                        <span class="mini-label"
                          >Định lượng <span class="text-danger">*</span></span
                        >
                        <select
                          v-model="item.idDinhLuong"
                          @change="
                            errorsNhanh.chiTiet?.[index] &&
                              (errorsNhanh.chiTiet[index].idDinhLuong = '')
                          "
                          class="modern-input"
                          :class="{
                            'input-invalid':
                              errorsNhanh.chiTiet?.[index]?.idDinhLuong,
                          }"
                        >
                          <option :value="null">Chọn định lượng</option>
                          <option
                            v-for="dl in danhSachDinhLuongNhanh"
                            :key="dl.id"
                            :value="dl.id"
                          >
                            {{ dl.giaTri }} {{ dl.tenDonVi }}
                          </option>
                        </select>
                        <transition name="shake">
                          <p
                            v-if="errorsNhanh.chiTiet?.[index]?.idDinhLuong"
                            class="error-hint mt-1"
                          >
                            <i class="bi bi-exclamation-circle"></i>
                            {{ errorsNhanh.chiTiet[index].idDinhLuong }}
                          </p>
                        </transition>
                      </div>

                      <div class="d-flex align-items-end pb-1">
                        <button
                          @click="xoaDongNhanh(index)"
                          style="
                            border: none;
                            background: transparent;
                            color: #ef4444;
                            cursor: pointer;
                          "
                        >
                          <i class="bi bi-trash3" style="font-size: 1.2rem"></i>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="form-group mt-3">
                  <label class="bold-label">Mô tả</label>
                  <textarea
                    v-model="formAddNhanh.moTa"
                    class="modern-input"
                    rows="3"
                    placeholder="Ghi chú món ăn..."
                  ></textarea>
                </div>
              </div>
            </div>
          </div>

          <div class="modal-footer-v2">
            <button
              class="btn-cancel modal-btn-cancel"
              @click="showModalAddNhanh = false"
            >
              <i class="bi bi-x-circle"></i>
              Hủy
            </button>

            <button
              class="btn-submit modal-btn-submit"
              @click="saveNhanhSanPham"
            >
              <i class="bi bi-check2-all"></i>
              Xác nhận
              <div class="btn-light-sweep"></div>
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
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
/* Mở rộng Modal */
.create-col {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}
.create-new-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #8b1d1d;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  margin-top: 22px;
}

.create-new-item .icon {
  width: 18px;
  height: 18px;
  background: #e02d2d;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

/* Modal rộng */
.modal-xl {
  max-width: 1000px !important;
  width: 95%;
}

/* Bộ lọc 4 cột */
.filter-grid-system {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
}

.bold-label-v2 {
  display: block;
  font-weight: 600;
  font-size: 15px;
  color: #334155;
  margin-bottom: 6px;
}

.input-light {
  width: 100%;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  font-size: 14px;
  color: #1e293b;
  outline: none;
}

.search-bar-v2 {
  position: relative;
}
.search-bar-v2 i {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
}

/* Nút thêm món mới - Black Bold nhưng không Caps */
.btn-create-manual {
  width: 100%;
  height: 42px;
  background: #e65a5a;
  color: #fff;
  border: none;
  border-radius: 10px;
  font-weight: 700;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

/* Card theo phong cách ảnh bạn gửi: Viền đỏ mảnh */
.selection-card-v2 {
  display: grid;
  grid-template-columns: 1.2fr 1fr 1fr;
  align-items: center;
  padding: 10px 20px;
  background: white;
  border: 1px solid #d32f2f; /* Viền đỏ mảnh */
  border-radius: 10px;
  margin-bottom: 12px;
  transition: 0.2s;
}

.selection-card-v2:hover {
  background: #fffcfc;
  box-shadow: 0 4px 12px rgba(211, 47, 47, 0.08);
}

.item-left-group {
  display: flex;
  align-items: center;
  gap: 15px;
}
.selection-img-v2 {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  object-fit: cover;
}

.sel-name-v2 {
  font-weight: 700;
  font-size: 17px;
  margin: 0;
  color: #0f172a;
}
.sel-sub-text {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

.item-mid-group {
  display: flex;
  gap: 30px;
}
.mini-label-v2 {
  font-size: 11px;
  font-weight: 700;
  color: #94a3b8;
  text-transform: none;
  margin-bottom: 2px;
  display: block;
}
.meta-val-v2 {
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  font-size: 15px;
}

.item-right-group {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 20px;
}
.sel-price-v2 {
  font-weight: 900;
  color: #d32f2f;
  font-size: 18px;
  margin: 0;
}

/* --- PHẦN STYLE ĐÃ ĐƯỢC CĂN CHỈNH GIÃN CÁCH --- */

.modern-container {
  background: #f8fafc;
  min-height: 100vh;

  font-family: "Inter", sans-serif;
}
.inner-content {
  width: 100%;
  max-width: 100%;
  display: flex;
  flex-direction: column;
  gap: 30px; /* Giãn cách các khối lớn */
}
.main-content {
  display: grid;
  grid-template-columns: 320px 1fr; /* Tăng Sidebar */
  gap: 32px; /* Giãn cách Sidebar và Main Panel */
  align-items: start;
}

/* CARD & TITLES */
.card {
  background: white;
  border-radius: 7px;
  padding: 30px; /* Tăng padding trong card */
  border: 1px solid #e2e8f0;
}
.card-title {
  font-weight: 700;
  font-size: 15px;
}
.box-header-card {
  background: white;
  border-radius: 7px;
  padding: 24px 32px;
  border: 1px solid #e2e8f0;
}
.title {
  font-weight: 900;
  font-size: 26px;
  display: flex;
  align-items: center;
  gap: 15px;
  margin: 0;
}
.subtitle-text {
  color: #64748b;
  margin: 6px 0 0 57px;
  font-size: 15px;
}
.icon-box-wrapper {
  width: 42px;
  height: 42px;
  background: #fff1f1;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

/* INPUTS */
.form-group {
  margin-bottom: 25px; /* Giãn cách giữa các input */
}
.bold-label {
  font-weight: 700;
  font-size: 14px;
  color: #1e293b;
  margin-bottom: 12px;
  display: block;
}
.modern-input {
  width: 100%;
  padding: 14px 18px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: #fcfcfc;
  transition: 0.2s;
}
.modern-input:focus {
  border-color: #d32f2f;
  outline: none;
  box-shadow: 0 0 0 4px rgba(211, 47, 47, 0.1);
}

/* SELECTED ITEMS LIST */
.item-row-card {
  display: grid;
  grid-template-columns: 2fr 100px 120px 40px;
  gap: 16px;
  align-items: center;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 12px;
  margin-bottom: 10px;
  border: 1px solid #d32f2f;
}
.item-main-info {
  display: flex;
  align-items: center;
  gap: 18px;
}
.item-thumb {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  object-fit: cover;
}
.item-name {
  margin: 0;
  font-size: 16px;
  color: #1e293b;
}
.badge-dl-mini {
  font-size: 11px;
  background: #e2e8f0;
  color: #475569;
  padding: 4px 10px;
  border-radius: 6px;
  font-weight: 600;
}
.qty-input-v2 {
  width: 100%;
  padding: 10px;
  border-radius: 8px;
  border: 1px solid #cbd5e1;
  text-align: center;
  font-weight: 700;
}
.mini-label {
  font-size: 11px;
  color: #94a3b8;
  display: block;
  margin-bottom: 6px;
  font-weight: 700;
  text-transform: uppercase;
}
.price-val {
  font-weight: 800;
  color: #d32f2f;
  margin: 0;
  font-size: 15px;
}
.btn-remove-item {
  border: none;
  background: #fee2e2;
  color: #ef4444;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  transition: 0.2s;
}
.btn-remove-item:hover {
  background: #ef4444;
  color: white;
}

/* PRICE BOX */
/* --- STYLE BOX GIÁ THU GỌN --- */
.price-container-compact {
  display: flex;
  justify-content: flex-end; /* Đẩy box sang phải */
}

.price-summary-box {
  background: #fffafa;
  border: 1px solid #fee2e2;
  border-radius: 16px;
  padding: 20px;
  width: 380px; /* Khống chế độ rộng */
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-label {
  font-size: 13px;
  color: #64748b;
}

.value-suggested {
  font-weight: 700;
  color: #1e293b;
  font-size: 15px;
}
.btn-apply-compact {
  display: inline-flex;
  align-items: center;
  gap: 6px;

  padding: 3px 12px;

  border: 1px solid #d32f2f;
  border-radius: 5px;

  background: white;
  color: #d32f2f;

  font-size: 13px;
  font-weight: 600;

  cursor: pointer;
  transition: all 0.2s ease;
}
.btn-apply-compact {
  margin-left: 14px;
}

.btn-apply-compact i {
  font-size: 14px;
}

.btn-apply-compact:hover {
  background: #e84141;
  color: white;
}

.highlight-row {
  padding-top: 10px;
  border-top: 1px dashed #fee2e2;
}

.input-price-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

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

.compact-price-input.input-invalid {
  border-color: #e63946;
  box-shadow: 0 0 0 4px rgba(230, 57, 70, 0.1);
}

.compact-price-input {
  width: 160px;
  padding: 8px 40px 8px 12px;
  border-radius: 10px;
  border: 1px solid #d32f2f;
  font-size: 18px;
  font-weight: 900;
  color: #d32f2f;
  text-align: right;
}
.compact-price-input:hover {
  border-color: #b71c1c;
}
.compact-price-input:focus {
  outline: none;
  border-color: #d32f2f;
  box-shadow: 0 0 0 4px rgba(211, 47, 47, 0.1);
}

.currency-unit {
  position: absolute;
  right: 10px;
  font-size: 14px;
  font-weight: 800;
  color: #d32f2f;
}

.price-input-col {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.price-input-col .error-hint {
  justify-content: flex-end;
  text-align: right;
}
/* FOOTER */
.footer-sticky {
  background: white;
  padding: 25px 40px;
  border-radius: 7px;
  border: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}
.footer-hint {
  color: #64748b;
  font-size: 14px;
  margin: 0;
}
.footer-btns {
  display: flex;
  gap: 20px;
}
.btn-submit {
  position: relative;
  overflow: hidden;
  background: #d32f2f;
  color: white;
  border: none;
  height: 54px;
  padding: 0 35px;
  border-radius: 7px;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}
.btn-cancel {
  background: #f1f5f9;
  color: #64748b;
  border: none;
  height: 54px;
  padding: 0 35px;
  border-radius: 7px;
  font-weight: 700;
  cursor: pointer;
}
.btn-icon {
  width: 20px;
  height: 20px;
  filter: brightness(0) invert(1);
}

/* MODAL STYLE */
/* --- MODAL STYLE HIỆN ĐẠI (FIX SCROLL) --- */

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(8px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  padding: 20px;
}

.modal-card {
  background: white;
  width: 100%;
  max-width: 600px;
  max-height: 85vh;
  border-radius: 8px;
  display: flex;
  flex-direction: column; /* Để Header cố định, Body cuộn */
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

/* Header cố định */
.modal-header {
  padding: 25px 35px;
  background: white;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.modal-title {
  font-size: 25px;
  font-weight: 650;
  color: #0f172a;
  margin: 0;
}

.modal-sub {
  font-size: 14px;
  color: #64748b;
  margin: 4px 0 0 0;
}

.close-modal {
  background: #f1f5f9;
  border: none;
  width: 36px;
  height: 36px;
  border-radius: 7px;
  font-size: 22px;
  color: #64748b;
  cursor: pointer;
  transition: 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-modal:hover {
  background: #fee2e2;
  color: #ef4444;
  transform: rotate(90deg);
}

.modal-body {
  padding: 25px 35px;
  overflow-y: auto; /* Cho phép cuộn dọc */
  flex: 1; /* Chiếm toàn bộ phần còn lại */
}

/* Thanh tìm kiếm hiện đại */
.search-bar {
  position: sticky; /* Giữ ô tìm kiếm ở trên khi cuộn */
  top: 0;
  z-index: 10;
  background: white;
  padding-bottom: 15px;
}

.search-input {
  width: 100%;
  padding: 14px 14px 14px 48px;
  border-radius: 16px;
  border: 2px solid #f1f5f9;
  background: #f8fafc;
  font-size: 14px;
  transition: 0.3s;
}

.search-input:focus {
  border-color: #d32f2f;
  background: white;
  box-shadow: 0 0 0 4px rgba(211, 47, 47, 0.08);
  outline: none;
}

.search-bar i {
  position: absolute;
  left: 18px;
  top: 18px; /* Căn chỉnh lại icon search */
  color: #94a3b8;
  font-size: 18px;
}

/* Danh sách món lẻ */
.selection-list-scroll {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.selection-card {
  display: flex;
  align-items: center;
  padding: 15px;
  border-radius: 18px;
  background: white;
  border: 1px solid #f1f5f9;
  transition: 0.25s all ease;
  cursor: default;
}

.selection-card:hover {
  border-color: #d32f2f;
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05);
}

.selection-img {
  width: 65px;
  height: 65px;
  border-radius: 14px;
  object-fit: cover;
  background: #f1f5f9;
}

.selection-info {
  flex: 1;
  padding-left: 15px;
}

.sel-name {
  font-weight: 800;
  color: #1e293b;
  font-size: 15.5px;
  margin: 0 0 6px 0;
}

.sel-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.sel-dl {
  font-size: 12px;
  background: #f1f5f9;
  color: #64748b;
  padding: 3px 10px;
  border-radius: 8px;
  font-weight: 700;
}

.sel-price {
  font-weight: 850;
  color: #d32f2f;
  font-size: 15px;
}

/* Custom scrollbar mượt */
.modal-body::-webkit-scrollbar {
  width: 6px;
}
.modal-body::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 10px;
}

/* BUTTONS & OTHER */
.btn-ghost-add {
  display: inline-flex;
  align-items: center;
  gap: 8px;

  padding: 8px 14px;

  background: transparent;
  border: none;
  color: #d32f2f;

  font-weight: 600;
  font-size: 15px;

  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.btn-ghost-add:hover {
  background: rgba(211, 47, 47, 0.08);
}

/* UPLOAD AREA */
.image-upload-zone {
  position: relative;
  width: 100%;
}
.preview-wrapper {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 7px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}
.upload-placeholder {
  width: 100%;
  aspect-ratio: 1;
  border: 2px dashed #e2e8f0;
  border-radius: 7px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
}
.mini-upload-btn {
  display: block;
  width: 100%;
  text-align: center;
  padding: 12px;
  background: #f1f5f9;
  border-radius: 10px;
  margin-top: 15px;
  font-weight: 700;
  cursor: pointer;
}

/* --- HẾT PHẦN STYLE --- */

/* Các animation và reset cũ của mày giữ nguyên bên dưới */

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(15, 23, 42, 0.7);
  backdrop-filter: blur(5px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}
.search-bar {
  position: relative;
  margin-bottom: 20px;
}
.search-bar i {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
}
.search-input {
  width: 100%;
  padding: 12px 12px 12px 45px;
  border-radius: 15px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
}
.selection-list-scroll {
  flex: 1;
  overflow-y: auto;
  padding-right: 10px;
}
.selection-list-scroll::-webkit-scrollbar {
  width: 6px;
}
.selection-list-scroll::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}
.selection-card {
  display: flex;
  align-items: center;
  border-radius: 18px;
  border: 1px solid #f1f5f9;
  margin-bottom: 12px;
  transition: 0.2s;
}
.selection-card:hover {
  border-color: #d32f2f;
  background: #fff1f1;
  transform: translateY(-2px);
}
.selection-img {
  width: 55px;
  height: 55px;
  border-radius: 12px;
  object-fit: cover;
}
.sel-name {
  font-weight: 800;
  margin: 0;
  font-size: 15px;
}
.sel-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 4px;
}
.sel-dl {
  font-size: 12px;
  background: #e2e8f0;
  padding: 2px 8px;
  border-radius: 6px;
  font-weight: 600;
  color: #475569;
}
.sel-price {
  font-weight: 800;
  color: #d32f2f;
  font-size: 14px;
}

.animate-pop {
  animation: modalPop 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
@keyframes modalPop {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
.remove-badge {
  position: absolute;
  top: -10px;
  right: -10px;
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 50%;
  width: 25px;
  height: 25px;
  cursor: pointer;
}
.close-modal {
  border: none;
  background: #f1f5f9;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  font-size: 20px;
  cursor: pointer;
}

.btn-plus-circle {
  width: 38px;
  height: 38px;

  border: none;
  border-radius: 7px;

  background: rgb(219, 54, 54);

  display: flex;
  align-items: center;
  justify-content: center;

  cursor: pointer;

  box-shadow: 0 6px 16px rgba(220, 38, 38, 0.22),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);

  transition: all 0.22s ease;
}

/* icon */
.btn-plus-circle i {
  font-size: 20px;
  color: white;
}

/* hover */
.btn-plus-circle:hover {
  transform: translateY(-2px) scale(1.05);

  box-shadow: 0 10px 22px rgba(220, 38, 38, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.18);
}

/* click */
.btn-plus-circle:active {
  transform: scale(0.94);
}

/* giữ modal luôn cao */
.modal-card.modal-xl {
  height: 85vh;
}

/* body flex để list chiếm full */
.modal-body {
  display: flex;
  flex-direction: column;
}

/* list luôn giữ chiều cao */
.selection-list-scroll {
  flex: 1;
  min-height: 450px;

  overflow-y: auto;

  padding-right: 6px;
}

/* empty state */
.empty-food-state {
  height: 100%;

  min-height: 420px;

  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  text-align: center;
}

.empty-food-state i {
  font-size: 70px;

  color: #d1d5db;

  margin-bottom: 16px;
}

.empty-food-state h4 {
  margin: 0;

  font-size: 20px;
  font-weight: 800;

  color: #0f172a;
}

.empty-food-state p {
  margin-top: 8px;

  font-size: 14px;

  color: #64748b;
}

.empty-summary {
  text-align: center;
  padding: 20px 0;
  color: #999;
}

.empty-summary i {
  font-size: 26px;
  display: block;
  margin-bottom: 8px;
}

.summary-box-v2 {
  background: #f8f9fa;
  padding: 25px;
  border-radius: 8px;

  text-align: center;
}
.empty-content i {
  font-size: 40px;
  display: block;
  margin-bottom: 10px;
}
.modal-footer-v2 {
  padding: 20px 30px;

  border-top: 1px solid #f1f5f9;

  display: flex;
  justify-content: flex-end;
  align-items: center;

  gap: 14px;

  background: #fff;
}

/* nút chung */
.modal-footer-v2 button {
  height: 46px;

  padding: 0 26px;

  border-radius: 12px;

  font-size: 14px;
  font-weight: 700;

  display: flex;
  align-items: center;
  justify-content: center;

  gap: 8px;

  transition: all 0.22s ease;
}

/* cancel */
.modal-btn-cancel {
  background: #f8fafc;

  border: 1px solid #e2e8f0;

  color: #475569;
}

.modal-btn-cancel:hover {
  background: #eef2f7;

  color: #0f172a;
}

/* submit */
.modal-btn-submit {
  background: linear-gradient(135deg, #dc2626, #991b1b);

  border: none;

  color: white;

  box-shadow: 0 8px 18px rgba(220, 38, 38, 0.18);
}

.modal-btn-submit i {
  font-size: 16px;
}

.modal-btn-submit:hover {
  transform: translateY(-1px);

  box-shadow: 0 12px 24px rgba(220, 38, 38, 0.26);
}

.modal-btn-submit:active {
  transform: scale(0.97);
}
.modal-footer-v2 {
  padding: 20px 30px;

  border-top: 1px solid #f1f5f9;

  display: flex;
  justify-content: flex-end;
  align-items: center;

  gap: 14px;

  background: #fff;
}

/* nút chung */
.modal-footer-v2 button {
  height: 46px;

  padding: 0 26px;

  border-radius: 7px;

  font-size: 14px;
  font-weight: 700;

  display: flex;
  align-items: center;
  justify-content: center;

  gap: 8px;

  transition: all 0.22s ease;
}

/* cancel */
.modal-btn-cancel {
  background: #f8fafc;

  border: 1px solid #e2e8f0;

  color: #475569;
}

.modal-btn-cancel:hover {
  background: #eef2f7;

  color: #0f172a;
}

.modal-btn-submit {
  background: rgb(212, 54, 54);

  border: none;

  color: white;

  box-shadow: 0 8px 18px rgba(220, 38, 38, 0.18);
}

.modal-btn-submit i {
  font-size: 16px;
}

.modal-btn-submit:hover {
  transform: translateY(-1px);

  box-shadow: 0 12px 24px rgba(220, 38, 38, 0.26);
}

.modal-btn-submit:active {
  transform: scale(0.97);
}

/* ///////////////////// HEADER ADD ////////// */
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

/* /////////////////// PHÂN TRANG ///////// */

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 24px;
  user-select: none;
}

.pg-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  height: 36px;
  padding: 0 6px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  background-color: #ffffff;
  color: #333333;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.pg-btn:hover:not(:disabled):not(.active) {
  border-color: #ff4d4f;
  color: #ff4d4f;
  background-color: #fff1f0;
}

.pg-btn.active {
  border: 1.5px solid #ff4d4f;
  color: #ff0000;
  font-weight: 600;
}

.pg-arrow i {
  font-size: 18px;
}

.pg-btn:disabled {
  border-color: #f0f0f0;
  background-color: #f5f5f5;
  color: #bfbfbf;
  cursor: not-allowed;
}

.pg-ellipsis {
  border: none;
  background: transparent;
  cursor: default;
  color: #aaa;
  min-width: 24px;
}

.selected-sidebar {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden; /* quan trọng */
}

.selected-list {
  flex: 1;
  overflow-y: auto; /* chỉ list scroll */
  padding: 16px;
  min-height: 0;
}

.pagination-sm {
  flex-shrink: 0; /* không bị ép nhỏ */
  padding: 8px 0;
  border-top: 1px solid #eee;
}

.pagination-sm .pg-btn {
  min-width: 28px;
  height: 28px;
  font-size: 12px;
  padding: 0 6px;
}

.step-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  white-space: nowrap;
  text-align: center;
}

.step.active .step-time,
.step.completed .step-time {
  color: #a52a2a;
  font-weight: 500;
}

.item-row-card.ngung-kinh-doanh {
  opacity: 0.5;
  filter: grayscale(1);
}

.qty-input-v2:disabled {
  background: #f3f4f6 !important;
  color: #9ca3af !important;
  cursor: not-allowed;
  pointer-events: none;
}

.btn-submit:disabled, .btn-submit.btn-disabled {
  background: #f3f4f6 !important;
  color: #9ca3af !important;
  cursor: not-allowed !important;
  box-shadow: none !important;
  transform: none !important;
  border: 1px solid #e5e7eb !important;
}

.alert-product-changed {
  background: rgba(220, 38, 38, 0.05);
  border: 1px solid rgba(220, 38, 38, 0.15);
  border-radius: 8px;
  padding: 12px 14px;
  color: #b91c1c;
  font-size: 13px;
  line-height: 1.45;
  margin-top: 12px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.04);
  text-align: left;
}
.alert-product-changed i {
  font-size: 18px;
  color: #dc2626;
  flex-shrink: 0;
}
</style>
