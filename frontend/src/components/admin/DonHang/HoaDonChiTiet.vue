<script setup>
import { ref, onMounted, computed, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";

/* ICONS (Giữ nguyên bộ icon bạn đang dùng) */
import iconBack from "@/assets/icons/prev.png"; // Giả định bạn có icon back
import {
  detailHoaDon,
  previewTienCoc,
  xacNhanHoanCoc,
} from "@/api/admin/hoadon/hoaDonApi";
import { hoanThanhHoaDon } from "@/api/admin/hoadon/hoaDonApi";
import { useUtils } from "@/utils/helper";
import { huyHoaDon } from "@/api/admin/hoadon/hoaDonApi";
import { exportHoaDonPdf } from "@/api/admin/hoadon/hoaDonApi";
import { subscribeToHoaDon } from "@/utils/sse";
import { danhDauNoXauBanAo } from "@/api/admin/banao/banAoApi";
const route = useRoute();
const router = useRouter();
const idHoaDon = route.params.id;
const isLoading = ref(false);
const hoaDon = ref(null);
const items = ref([]);
const lichSu = ref([]);
const eventSource = ref(null);
const emit = defineEmits(["success", "close"]);
const { notify, nprogress } = useUtils();
const tongTienHang = computed(() => hoaDon.value?.tongTienMon || 0);
const statusBadgeClass = {
  0: "status-pending",
  1: "status-done",
  2: "status-cancelled",
};
const statusBadgeText = {
  0: "Chưa lên món",
  1: "Đã lên món",
  2: "Hủy món",
};
const tienDaThanhToan = computed(() => {
  return (
    hoaDon.value?.tongTienDaThanhToan ?? hoaDon.value?.tienDaThanhToan ?? 0
  );
});

// Tiền khách đã thanh toán thêm ngoài tiền cọc
const tienThanhToanThem = computed(() => {
  return Math.max(tienDaThanhToan.value - (hoaDon.value?.tienCoc || 0), 0);
});

// Số tiền còn phải thanh toán
const tinhThanhTien = computed(() => {
  if ([8, 9].includes(hoaDon.value?.trangThai)) return 0;
  if (hoaDon.value?.tienConLai != null) {
    return hoaDon.value.tienConLai;
  }
  return Math.max(
    (hoaDon.value?.tienSauGiam || 0) -
      (hoaDon.value?.diemTichLuySuDung || 0) -
      tienDaThanhToan.value,
    0,
  );
});

//////////////// NỢ XẤU /////////////
const modalNoXau = ref(false);
const ghiChuNoXau = ref("");

const openModalNoXau = () => {
  modalNoXau.value = true;
};

const handleXacNhanNoXau = async () => {
  if (!(await notify.confirm("Xác nhận đánh dấu khách này là nợ xấu?"))) return;
  isLoading.value = true;
  nprogress.start();
  try {
    await danhDauNoXauBanAo(hoaDon.value.idBanAo, ghiChuNoXau.value);
    notify.success("Đã đánh dấu nợ xấu");
    modalNoXau.value = false;
    ghiChuNoXau.value = "";
    await loadDetail();
    emit("success");
  } catch (e) {
    notify.error(
      e.response?.data?.message || e.response?.data || "Có lỗi xảy ra",
    );
    console.log(e);
  } finally {
    isLoading.value = false;
    nprogress.done();
  }
};

/////////////////////// HOÀN THÀNH HÓA ĐƠN ////////////////////
const handleHoanThanh = async () => {
  try {
    if (!(await notify.confirm("Xác nhận hoàn thành hóa đơn này?"))) return;
    nprogress.start();
    await hoanThanhHoaDon(idHoaDon);
    notify.success("Thành công");
    await loadDetail();
    emit("success");
  } catch (e) {
    notify.error(e.response?.data?.message || e.response?.data);
    console.log(e);
  }
};
//////////////// HUY HOA DON /////////////
const modalHuyHoaDon = ref(false);
const lyDoHuy = ref("");
const preview = ref(null);

const openModalHuy = async () => {
  modalHuyHoaDon.value = true;

  try {
    const res = await previewTienCoc(idHoaDon);
    preview.value = res.data;
  } catch (e) {
    notify.error("Không lấy được thông tin hoàn cọc");
  }
};

const handleHuyHoaDon = async () => {
  if (!lyDoHuy.value.trim()) {
    notify.error("Vui lòng nhập lý do hủy");
    return;
  }

  if (!(await notify.confirm("Bạn có muốn hủy hóa đơn này"))) return;
  isLoading.value = true;
  nprogress.start();
  const startTime = Date.now();
  try {
    await huyHoaDon(idHoaDon, {
      ghiChu: lyDoHuy.value,
    });
    notify.success("Hủy hóa đơn thành công");
    modalHuyHoaDon.value = false;
    lyDoHuy.value = "";
    await loadDetail();
    emit("success");
  } catch (e) {
    notify.error(
      e.response?.data?.message || e.response?.data || "Có lỗi xảy ra",
    );
  } finally {
    const elapsed = Date.now() - startTime;
    const minimumLoading = 3000;
    if (elapsed < minimumLoading) {
      await new Promise((resolve) =>
        setTimeout(resolve, minimumLoading - elapsed),
      );
    }
    isLoading.value = false;
    nprogress.done();
  }
};

const handleXacNhanHoanCoc = async () => {
  if (!(await notify.confirm("Xác nhận đã hoàn tiền cọc cho khách?"))) return;
  nprogress.start();
  try {
    await xacNhanHoanCoc(idHoaDon);
    notify.success("Xác nhận hoàn cọc thành công");
    await loadDetail();
    emit("success");
  } catch (e) {
    notify.error(e.response?.data?.message || "Có lỗi xảy ra");
  } finally {
    nprogress.done();
  }
};

//////////////////////////// IN HÓA Đơn ////////////////
const handleExportPdf = async (id) => {
  if (!id) {
    alert("Không tìm thấy ID hóa đơn");
    return;
  }
  if (!(await notify.confirm("Xuất hóa đơn này ?"))) return;

  try {
    const res = await exportHoaDonPdf(id);

    const blob = new Blob([res.data], { type: "application/pdf" });
    const url = window.URL.createObjectURL(blob);

    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", `hoa-don-${id}.pdf`);

    document.body.appendChild(link);
    link.click();
    link.remove();

    window.URL.revokeObjectURL(url);
  } catch (e) {
    console.error(e);
    alert("Lỗi xuất PDF");
  }
};

/////////////// LỊCH SU HOA DON //////////////////
const convertAction = (action) => {
  switch (action) {
    case "TAO_HOA_DON":
      return "Tạo hóa đơn";
    case "NHAN_TIEN_COC":
      return "Nhận tiền cọc";
    case "XAC_NHAN_DAT_BAN":
      return "Xác nhận đặt bàn";
    case "DAT_BAN":
      return "Đặt bàn";
    case "XEP_BAN":
      return "Xếp bàn";
    case "GUI_MAIL":
      return "Gửi email";
    case "HUY_HOA_DON":
      return "Hủy đơn";
    case "AUTO_HUY":
      return "Tự động hủy";
    case "THANH_TOAN_COC":
      return "Thanh toán cọc";
    case "HOAN_COC":
      return "Hoàn tiền cọc";
    case "HOAN_COC_THAT_BAI":
      return "Hoàn cọc thất bại";
    case "CHECK_IN":
      return "Check-in";
    case "GOI_MON":
      return "Gọi món";
    case "LEN_TAT_CA":
      return "Lên hết món";
    case "HOAN_THANH":
      return "Hoàn thành";
    case "THANH_TOAN":
      return "Thanh toán";
    case "HOAN_TAT_THANH_TOAN":
      return "Thanh toán thành công";
    case "LEN_MON":
      return "Lên món";
    case "AP_DUNG_MA_GIAM_GIA":
      return "Mã giảm giá";
    case "XAC_NHAN":
      return "Xác nhận";
    case "CHO_HOAN_COC":
      return "Chờ hoàn cọc";
    case "TREO_BILL":
      return "Ghi nợ";
    case "GIAM_GIA_TRUC_TIEP":
      return "Giảm trực tiếp";
    case "GHEP_CHO_NGOI":
      return "Ghép bàn";
    case "CAP_NHAT_PHIEU":
      return "Cập nhật";
    case "NO_XAU":
      return "Nợ xấu";
    case "THANH_TOAN_COC_SAU_HUY":
      return "Thanh toán cọc sau hủy";
    default:
      return action;
  }
};

const convertHinhThucCuc = (action) => {
  switch (action) {
    case "TIEN_MAT":
      return "Tiền mặt";
    case "CHUYEN_KHOAN":
      return "Chuyển khoản";
    default:
      return "Không cần cọc";
  }
};

const formatTime = (time) => {
  if (!time) return "";
  return new Date(time).toLocaleString("vi-VN");
};

const getTimelineClass = (action) => {
  switch (action) {
    case "TAO_HOA_DON":
      return "success";
    case "NHAN_TIEN_COC":
      return "primary";
    case "XAC_NHAN_DAT_BAN":
      return "primary";
    case "GUI_MAIL":
      return "success";
    case "XEP_BAN":
      return "success";
    case "HUY_HOA_DON":
      return "success";
    case "AUTO_HUY":
      return "success";
    case "HOAN_COC":
      return "success";
    case "HOAN_COC_THAT_BAI":
      return "success";
    case "THANH_TOAN_COC":
      return "success";
    case "DAT_BAN":
      return "success";
    case "CHECK_IN":
      return "success";
    case "GOI_MON":
      return "success";
    case "LEN_TAT_CA":
      return "success";
    case "HOAN_THANH":
      return "success";
    case "THANH_TOAN":
      return "success";
    case "HOAN_TAT_THANH_TOAN":
      return "success";
    case "LEN_MON":
      return "success";
    case "AP_DUNG_MA_GIAM_GIA":
      return "success";
    case "XAC_NHAN":
      return "success";
    case "CHO_HOAN_COC":
      return "success";
    case "TREO_BILL":
      return "success";
    case "GHEP_CHO_NGOI":
      return "success";
    case "GIAM_GIA_TRUC_TIEP":
      return "success";
    case "CAP_NHAT_PHIEU":
      return "success";
    case "NO_XAU":
      return "success";
    case "THANH_TOAN_COC_SAU_HUY":
      return "success";
    default:
      return "";
  }
};

const formatMoney = (money) => {
  if (!money) return "0 đ";
  return money.toLocaleString("vi-VN") + " đ";
};

const loadDetail = async () => {
  try {
    const res = await detailHoaDon(idHoaDon);
    hoaDon.value = { ...res.data.hoaDon, idBanAo: res.data.idBanAo };
    items.value = res.data.items;
    lichSu.value = res.data.lichSu;
  } catch (error) {
    console.error("Lỗi load detail:", error);
  }
};

const convertTrangThai = (stt) => {
  switch (stt) {
    case 0:
      return "Vừa tạo";
    case 1:
      return "Chờ thanh toán cọc";
    case 2:
      return "Đã cọc";
    case 3:
      return "Đã xác nhận";
    case 4:
      return "Chờ xác nhận";
    case 5:
      return "Chờ hoàn cọc";
    case 6:
      return "Đang phục vụ";
    case 7:
      return "Chờ thanh toán";
    case 8:
      return "Đã thanh toán";
    case 9:
      return "Hoàn thành";
    case 10:
      return "Treo nợ";
    case 11:
      return "Nợ xấu";
    default:
      return "Đã hủy";
  }
};

const mapStatusToStep = (status) => {
  switch (status) {
    case 0:
      return 0;
    case 1:
    case 2:
      return 1;
    case 3:
    case 4:
      return 2;
    case 5:
    case 6:
      return 3;
    case 7:
      return 4;
    case 8:
      return 5;
    case 9:
      return 6;
    case 10:
      return -1;
    case -1:
      return -1;
    default:
      return -1;
  }
};

// const stepTimeMap = computed(() => {
//   const map = {};
//   lichSu.value.forEach((log) => {
//     switch (log.hanhDong) {
//       case "TAO_HOA_DON":
//         map[0] = log.thoiGian;
//         break;
//       case "XAC_NHAN_DAT_BAN":
//       case "NHAN_TIEN_COC":
//       case "XEP_BAN":
//         map[1] = log.thoiGian;
//         break;
//       case "XEP_BAN":
//       case "DAT_BAN":
//       case "XAC_NHAN":
//         map[2] = log.thoiGian;
//         break;
//       case "CHECK_IN":
//         map[3] = log.thoiGian;
//         break;
//       case "THANH_TOAN":
//         map[4] = log.thoiGian;
//         break;
//       case "HOAN_TAT_THANH_TOAN":
//         map[5] = log.thoiGian;
//         break;
//       case "HOAN_THANH":
//         map[6] = log.thoiGian;
//         break;
//     }
//   });
//   return map;
// });

const currentStep = computed(() => {
  return mapStatusToStep(hoaDon.value?.trangThai);
});

const steps = [
  { key: 0, label: "VỪA TẠO" },
  { key: 1, label: "CHỜ XÁC NHẬN" },
  { key: 2, label: "XÁC NHẬN" },
  { key: 3, label: "PHỤC VỤ" },
  { key: 4, label: "CHỜ THANH TOÁN" },
  { key: 5, label: "ĐÃ THANH TOÁN" },
  { key: 6, label: "HOÀN THÀNH" },
];

const visibleSteps = computed(() => {
  const step = currentStep.value;
  if (step < 0) return [];

  return steps.slice(0, step + 1);
});

onMounted(() => {
  loadDetail();
  eventSource.value = subscribeToHoaDon(idHoaDon, (data) => {
    loadDetail();
  });
});

onUnmounted(() => {
  if (eventSource.value) {
    eventSource.value.close();
  }
});

const getImageUrl = (img) => {
  if (!img) return "https://via.placeholder.com/50";

  return `http://localhost:8080/uploads/${img}`;
};

const goBack = () => {
  router.back();
};

////////////// PHÂN TRANG MÓN ///////////
const currentPage = ref(1);
const pageSize = 5; // số dòng mỗi trang

const pagedItems = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return items.value.slice(start, start + pageSize);
});
const totalPages = computed(() => Math.ceil(items.value.length / pageSize));

const pageList = computed(() => {
  const total = totalPages.value;
  if (total <= 5) return Array.from({ length: total }, (_, i) => i + 1);
  const pages = [1, 2, 3];
  if (total > 4) pages.push("…");
  pages.push(total);
  return pages;
});
</script>

<template>
  <div class="quan-ly-hoa-don-chi-tiet">
    <div class="box">
      <div class="box-header box-header-flex">
        <div class="header-left">
          <div class="icon-wrapper">
            <img src="@/assets/icons/hdct.png" alt="icon" />
          </div>

          <div class="title-group">
            <h2 class="title">Quản lý hóa đơn chi tiết</h2>
            <p class="subtitle-text">
              Theo dõi trạng thái và thông tin chi tiết hóa đơn
            </p>
          </div>
        </div>

        <button class="icon-btn" @click="goBack" data-tooltip="Quay lại">
          <img :src="iconBack" />
        </button>
      </div>
    </div>
    <div class="box info-main-box mb-4">
      <div class="status-header mb-4">
        <span class="label-status text-bold">
          <i></i> Trạng thái hóa đơn:
          <span class="ma-hd">{{ hoaDon?.maHoaDon }}</span>
        </span>
      </div>

      <div v-if="hoaDon?.trangThai === -1" class="cancel-wrapper">
        <div class="cancel-icon">
          <i class="bi bi-x-circle-fill"></i>
        </div>
        <div class="cancel-content">
          <h3>Hóa đơn đã bị hủy</h3>
          <p>
            Đơn hàng này không còn hiệu lực và không thể tiếp tục xử lý.
            <span v-if="hoaDon.tienCocHoan > 0">
              - Đã hoàn
              <strong
                >{{
                  Number(hoaDon.tienCocHoan).toLocaleString("vi-VN")
                }}đ</strong
              >
              tiền cọc cho khách.
            </span>
            <span v-else-if="hoaDon.tienCocBiMat > 0">
              - Khách bị mất
              <strong
                >{{
                  Number(hoaDon.tienCocBiMat).toLocaleString("vi-VN")
                }}đ</strong
              >
              tiền cọc.
            </span>
          </p>
        </div>
      </div>
      <div v-if="hoaDon?.trangThai === 10" class="debt-wrapper">
        <div class="debt-icon">
          <i class="bi bi-journal-minus"></i>
        </div>
        <div class="debt-content">
          <h3>Hóa đơn đang treo nợ</h3>
          <p>
            Khách chưa thanh toán, bàn đã được giải phóng.
            <span v-if="hoaDon.lyDoGhiNo"> Lý do: {{ hoaDon.lyDoGhiNo }}</span>
          </p>
        </div>
      </div>
      <div v-if="hoaDon?.trangThai === 11" class="bad-debt-wrapper">
        <div class="bad-debt-icon">
          <i class="bi bi-person-x-fill"></i>
        </div>
        <div class="bad-debt-content">
          <h3>Hóa đơn nợ xấu</h3>
          <p>
            Hóa đơn đang ở trạng thái nợ xấu không thể thao tác. Vui lòng cẩn
            thận hơn với khách hàng này.
          </p>
        </div>
      </div>
      <div
        v-if="
          hoaDon?.trangThai !== -1 &&
          hoaDon?.trangThai !== 10 &&
          hoaDon?.trangThai !== 11
        "
        class="stepper-wrapper mb-5"
      >
        <template v-for="(step, index) in visibleSteps" :key="step.key">
          <div
            class="step"
            :class="{
              completed: currentStep > index,
              active: currentStep === index,
            }"
          >
            <div class="circle">
              <i class="bi bi-check-lg"></i>
            </div>
            <div class="label">{{ step.label }}</div>

            <!-- THỜI GIAN hiển thị dưới label
            <div class="step-time" v-if="stepTimeMap[step.key]">
              {{ formatTime(stepTimeMap[step.key]) }}
            </div> -->
          </div>

          <div
            v-if="index !== visibleSteps.length - 1"
            class="line"
            :class="{ active: currentStep > index }"
          ></div>
        </template>
      </div>
      <div class="info-detail-grid">
        <div class="info-item">
          <label>Trạng thái hóa đơn</label>
          <p class="value bold">
            {{ convertTrangThai(hoaDon?.trangThai) }}
          </p>
        </div>
        <div class="info-item">
          <label>Khách hàng</label>
          <p class="value">{{ hoaDon?.tenKhachHang }}</p>
        </div>
        <div class="info-item">
          <label>Số điện thoại</label>
          <p class="value">{{ hoaDon?.soDienThoai || "Không xác định" }}</p>
        </div>
        <div class="info-item">
          <label>Bàn</label>
          <p class="value">
            {{ hoaDon?.listTenBan?.join(", ") || "Chưa chọn bàn" }} -
            {{ hoaDon?.tenKhuVuc }}
          </p>
        </div>
        <div class="info-item">
          <label>Tiền cọc</label>
          <p class="value">{{ formatMoney(hoaDon?.tienCoc) }}</p>
        </div>
        <div class="info-item">
          <label>Hình thức cọc</label>
          <p class="value">{{ convertHinhThucCuc(hoaDon?.hinhThucCoc) }}</p>
        </div>
        <div class="info-item no-border">
          <label>Thời gian tạo</label>
          <p class="value">
            {{ new Date(hoaDon?.thoiGianTao).toLocaleString("vi-VN") }}
          </p>
        </div>
      </div>
    </div>

    <div class="box table-box mb-4">
      <div class="box-title-tt mb-3">
        <h3><i class="ri-store-2-line"></i> Thông tin món đã đặt</h3>
      </div>

      <table class="table-main">
        <thead>
          <tr>
            <th style="width: 60px; text-align: center">STT</th>
            <th style="text-align: left">Tên món ăn</th>
            <th style="width: 100px; text-align: center">Số lượng</th>
            <th style="text-align: right">Đơn giá</th>
            <th style="text-align: right">Thành tiền</th>
            <th style="text-align: center">Trạng thái</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in pagedItems" :key="item.id">
            <td style="text-align: center">
              {{ (currentPage - 1) * pageSize + index + 1 }}
            </td>
            <td style="text-align: left">
              <div class="food-item">
                <img :src="getImageUrl(item.hinhAnh)" class="food-img" />

                <div class="food-info">
                  <span class="food-name">{{ item.tenSanPham }}</span>
                  <span class="danh-muc">{{ item.tenDanhMuc }}</span>
                </div>
              </div>
            </td>
            <td style="text-align: center">
              <span class="badge-qty">{{ item.soLuong }}</span>
            </td>
            <td style="text-align: right">{{ formatMoney(item.donGia) }}</td>
            <td style="text-align: right" class="text-danger text-bold">
              {{ formatMoney(item.thanhTien) }}
            </td>
            <td style="text-align: center">
              <span :class="['status-badge', statusBadgeClass[item.trangThai]]">
                {{ statusBadgeText[item.trangThai] }}
              </span>
            </td>
          </tr>

          <tr v-if="items.length === 0">
            <td colspan="6" class="empty-row py-5">
              <div class="empty-content">
                <i class="bi bi-egg-fried"></i>
                <p>Chưa có món ăn nào được gọi</p>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="totalPages > 1" class="pagination pagination-sm mt-2">
        <button
          :disabled="currentPage === 1"
          @click="currentPage--"
          class="pg-btn pg-arrow"
        >
          <i class="ri-arrow-left-s-line"></i>
        </button>
        <template v-for="p in pageList" :key="p">
          <button v-if="p === '…'" class="pg-btn pg-ellipsis" disabled>
            ···
          </button>
          <button
            v-else
            class="pg-btn"
            :class="{ active: p === currentPage }"
            @click="currentPage = p"
          >
            {{ p }}
          </button>
        </template>
        <button
          :disabled="currentPage === totalPages"
          @click="currentPage++"
          class="pg-btn pg-arrow"
        >
          <i class="ri-arrow-right-s-line"></i>
        </button>
      </div>
    </div>
    <!-- ← đóng table-box -->

    <div class="row mt-4">
      <div class="col-md-6">
        <div class="box history-box">
          <div class="box-title mb-4">
            <h3>
              <i
                class="fa-solid fa-layer-group me-2"
                style="font-size: 25px; color: #000"
                ;
              ></i>
              Lịch sử hóa đơn
            </h3>
          </div>
          <div class="fsh-timeline-v3">
            <div class="timeline-scroll">
              <div
                v-for="(log, i) in lichSu"
                :key="i"
                class="timeline-step-v3"
                :class="getTimelineClass(log.hanhDong)"
              >
                <div class="step-left">
                  <div class="dot-outer">
                    <div class="dot-inner"></div>
                  </div>

                  <div v-if="i !== lichSu.length - 1" class="step-line"></div>
                </div>

                <div class="step-right">
                  <span class="step-time-v3">
                    {{ formatTime(log.thoiGian) }} - {{ log.tenNhanVien }}
                  </span>

                  <p class="step-msg-v3">
                    <span class="action-text">
                      {{ convertAction(log.hanhDong) }}:
                    </span>
                    {{ log.ghiChu }}
                  </p>
                </div>
              </div>

              <div v-if="lichSu.length === 0" class="text-center text-muted">
                Chưa có lịch sử
              </div>
            </div>
            <button
              v-if="hoaDon?.trangThai === 5"
              class="btn-hoan-coc-outline mt-4"
              @click="handleXacNhanHoanCoc"
            >
              <i class="ri-money-dollar-circle-line me-2"></i>
              Xác nhận đã hoàn cọc
            </button>

            <button
              v-else-if="[0, 1, 2, 3, 4].includes(hoaDon?.trangThai)"
              class="btn-cancel-outline mt-4"
              @click="openModalHuy"
            >
              <i class="fa-solid fa-rectangle-xmark me-2"></i>
              Hủy hóa đơn
            </button>
          </div>
        </div>
      </div>
      <div class="col-md-6">
        <div class="box summary-box">
          <div class="summary-header">
            <div class="summary-header-icon">
              <i class="bi bi-receipt"></i>
            </div>
            <span>Tổng kết đơn hàng</span>
          </div>

          <div class="summary-body">
            <div class="summary-row">
              <span class="summary-row-label">Tổng tiền hàng</span>
              <span class="summary-row-value">
                {{ formatMoney(tongTienHang) }}
              </span>
            </div>

            <!-- Chỉ hiện khi có tiền cọc -->
            <div class="summary-row" v-if="hoaDon?.tienCoc > 0">
              <span class="summary-row-label">Tiền cọc</span>
              <span class="summary-row-value">
                - {{ formatMoney(hoaDon.tienCoc) }}
              </span>
            </div>

            <div class="summary-row" v-if="hoaDon?.soTienDuocGiam > 0">
              <div class="summary-row-label">
                Giảm giá
                <span v-if="hoaDon?.maGiamGia" class="discount-badge">
                  {{ hoaDon.maGiamGia }} · {{ hoaDon.giaTriGiam }}%
                </span>
              </div>
              <span class="summary-row-value">
                - {{ formatMoney(hoaDon.soTienDuocGiam) }}
              </span>
            </div>

            <div class="summary-row" v-if="hoaDon?.giamTrucTiep > 0">
              <span class="summary-row-label">Giảm trực tiếp</span>
              <span class="summary-row-value">
                - {{ formatMoney(hoaDon.giamTrucTiep) }}
              </span>
            </div>

            <div class="summary-row" v-if="hoaDon?.diemTichLuySuDung > 0">
              <span class="summary-row-label">Điểm tích lũy sử dụng</span>
              <span class="summary-row-value">
                - {{ formatMoney(hoaDon.diemTichLuySuDung) }}
              </span>
            </div>

            <div
              class="summary-divider"
              v-if="
                hoaDon?.tienCoc > 0 ||
                hoaDon?.soTienDuocGiam > 0 ||
                hoaDon?.giamTrucTiep > 0 ||
                hoaDon?.diemTichLuySuDung > 0
              "
            ></div>

            <div class="summary-row" v-if="tienThanhToanThem > 0">
              <span class="summary-row-label">
                {{
                  hoaDon?.tienCoc > 0 ? "Đã thanh toán thêm" : "Đã thanh toán"
                }}
              </span>
              <span class="summary-row-value" style="color: #16a34a">
                - {{ formatMoney(tienThanhToanThem) }}
              </span>
            </div>

            <div
              class="summary-divider"
              v-if="
                hoaDon?.tienCoc > 0 ||
                hoaDon?.soTienDuocGiam > 0 ||
                hoaDon?.giamTrucTiep > 0 ||
                hoaDon?.diemTichLuySuDung > 0 ||
                tienThanhToanThem > 0
              "
            ></div>

            <div class="summary-total-row">
              <span class="summary-total-label">Còn phải thanh toán</span>
              <span class="summary-total-value">
                {{ formatMoney(tinhThanhTien) }}
              </span>
            </div>
          </div>
          <div class="action-footer mt-4">
            <!-- HỦY / CHỜ HOÀN CỌC -->
            <div
              v-if="hoaDon?.trangThai === -1 || hoaDon?.trangThai === 5"
              class="cancelled-notice"
            >
              <i class="ri-information-line"></i>
              <div>
                <p class="cancelled-notice-title">
                  {{
                    hoaDon?.trangThai === 5
                      ? "Đơn hàng đang chờ hoàn cọc"
                      : "Đơn hàng đã bị hủy"
                  }}
                </p>
                <p class="cancelled-notice-sub">
                  {{
                    hoaDon?.trangThai === 5
                      ? "Vui lòng xác nhận hoàn tiền cọc cho khách trước khi đóng đơn."
                      : "Mọi thao tác trên đơn hàng này đều không còn hiệu lực."
                  }}
                </p>
              </div>
            </div>
            <div
              v-else-if="hoaDon?.trangThai === 10"
              class="cancelled-notice debt-notice"
            >
              <div class="debt-notice-content">
                <i class="ri-information-line"></i>
                <div>
                  <p class="cancelled-notice-title">Hóa đơn đang treo nợ</p>
                  <p class="cancelled-notice-sub">
                    Cần thu tiền và xác nhận thanh toán để đóng hóa đơn này.
                  </p>
                </div>
              </div>
              <button class="btn-no-xau-small" @click="openModalNoXau">
                <i class="ri-user-forbid-line"></i>
                <span>Đánh dấu nợ xấu</span>
              </button>
            </div>
            <div
              v-else-if="hoaDon?.trangThai === 11"
              class="cancelled-notice bad-debt-notice"
            >
              <i class="ri-error-warning-line"></i>
              <div>
                <p class="cancelled-notice-title">Hóa đơn nợ xấu</p>
                <p class="cancelled-notice-sub">
                  Hóa đơn đang ở trạng thái nợ xấu không thể thao tác. Vui lòng
                  cẩn thận hơn với khách.
                </p>
              </div>
            </div>

            <!-- VỪA TẠO / CHỜ CỌC / ĐÃ CỌC / CHỜ XÁC NHẬN -->
            <div
              v-else-if="[0, 1, 2, 4].includes(hoaDon?.trangThai)"
              class="confirmed-badge"
            >
              <i class="bi bi-clock-fill"></i>
              Đang chờ xác nhận đặt bàn
            </div>

            <!-- ĐÃ XÁC NHẬN -->
            <div v-else-if="hoaDon?.trangThai === 3" class="confirmed-badge">
              <i class="bi bi-check-circle-fill"></i>
              Đã xác nhận & Gửi thông báo cho khách
            </div>

            <!-- ĐANG PHỤC VỤ -->
            <div
              v-else-if="hoaDon?.trangThai === 6"
              class="confirmed-badge serving-badge"
            >
              <i class="ri-restaurant-line"></i>
              Đang phục vụ khách tại bàn
            </div>

            <!-- CHỜ THANH TOÁN -->
            <div
              v-else-if="hoaDon?.trangThai === 7"
              class="confirmed-badge waiting-payment-badge"
            >
              <i class="ri-bank-card-line"></i>
              Chờ khách thanh toán
            </div>

            <!-- ĐÃ THANH TOÁN → HOÀN THÀNH -->
            <div
              v-else-if="hoaDon?.trangThai === 8"
              class="btn-hoan-thanh"
              @click="!isLoading && handleHoanThanh()"
              :class="{ 'is-loading': isLoading }"
            >
              <i class="bi bi-check2-all"></i>
              Hoàn thành hóa đơn
            </div>

            <!-- XUẤT HÓA ĐƠN -->
            <button
              v-if="hoaDon?.trangThai === 9"
              class="btn-export-pdf"
              @click="handleExportPdf(idHoaDon)"
            >
              <i class="fa-solid fa-receipt"></i>
              Xuất hóa đơn
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
  <div v-if="isLoading" class="admin-luxury-overlay">
    <div class="luxury-loader-card">
      <div class="inner-border"></div>

      <div class="loader-content">
        <div class="premium-ring-wrapper">
          <div class="ring-child"></div>
          <div class="ring-child"></div>
          <div class="logo-placeholder">FSH</div>
        </div>

        <h3 class="luxury-status">Đang xử lý hệ thống</h3>
        <div class="status-line"></div>
        <p class="luxury-note">Vui lòng không đóng trình duyệt lúc này.</p>
      </div>
    </div>
  </div>
  <transition name="fade-scale">
    <div
      v-if="modalHuyHoaDon"
      class="cancel-overlay"
      @click.self="modalHuyHoaDon = false"
    >
      <div class="cancel-modal">
        <div class="cancel-modal-header">
          <div class="cancel-modal-icon">
            <i class="ri-spam-2-fill"></i>
          </div>
          <div>
            <h3>Hủy hóa đơn</h3>
            <p>Thao tác này không thể hoàn tác sau khi xác nhận</p>
          </div>
          <button class="cancel-modal-close" @click="modalHuyHoaDon = false">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>

        <div class="cancel-modal-body">
          <div v-if="preview" class="refund-info">
            <div class="refund-info-row">
              <span
                ><i class="bi bi-arrow-return-left me-2"></i>Hoàn tiền cọc</span
              >
              <span class="refund-positive"
                >+ {{ formatMoney(preview.tienCocHoan) }}</span
              >
            </div>
            <div class="refund-info-row">
              <span
                ><i class="bi bi-dash-circle me-2"></i>Phí hủy (Mất cọc)</span
              >
              <span class="refund-negative"
                >- {{ formatMoney(preview.tienCocBiMat) }}</span
              >
            </div>
          </div>

          <div class="cancel-reason">
            <label>Lý do hủy <span class="required">*</span></label>
            <textarea
              v-model="lyDoHuy"
              placeholder="Nhập lý do hủy hóa đơn..."
              rows="4"
            ></textarea>
          </div>
        </div>

        <div class="cancel-modal-footer">
          <button class="cancel-btn-secondary" @click="modalHuyHoaDon = false">
            Đóng
          </button>
          <button class="cancel-btn-confirm" @click="handleHuyHoaDon">
            <i class="bi bi-check2-all"></i>
            <div class="btn-light-sweep"></div>
            Xác nhận
          </button>
        </div>
      </div>
    </div>
  </transition>
  <transition name="fade-scale">
    <div
      v-if="modalNoXau"
      class="cancel-overlay"
      @click.self="modalNoXau = false"
    >
      <div class="cancel-modal">
        <div class="cancel-modal-header">
          <div class="cancel-modal-icon">
            <i class="ri-user-forbid-line"></i>
          </div>
          <div>
            <h3>Đánh dấu khách nợ xấu</h3>
            <p>Thao tác này không thể hoàn tác sau khi xác nhận</p>
          </div>
          <button class="cancel-modal-close" @click="modalNoXau = false">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>

        <div class="cancel-modal-body">
          <div class="refund-info">
            <div class="refund-info-row">
              <span><i class="bi bi-person-fill me-2"></i>Họ tên</span>
              <span class="value">{{
                hoaDon?.tenKhachHang || "Không xác định"
              }}</span>
            </div>
            <div class="refund-info-row">
              <span
                ><i class="bi bi-telephone-fill me-2"></i>Số điện thoại</span
              >
              <span class="value">{{
                hoaDon?.soDienThoai || "Không xác định"
              }}</span>
            </div>
            <div class="refund-info-row">
              <span><i class="bi bi-envelope-fill me-2"></i>Email</span>
              <span class="value">{{ hoaDon?.email || "Không xác định" }}</span>
            </div>
            <div class="refund-info-row">
              <span><i class="bi bi-cake2-fill me-2"></i>Ngày sinh</span>
              <span class="value">
                {{
                  hoaDon?.ngaySinh
                    ? new Date(hoaDon.ngaySinh).toLocaleDateString("vi-VN")
                    : "Không xác định"
                }}
              </span>
            </div>
            <div class="refund-info-row">
              <span><i class="bi bi-cash-coin me-2"></i>Số tiền nợ</span>
              <span class="refund-negative">{{
                formatMoney(hoaDon?.tienConLai)
              }}</span>
            </div>
          </div>

          <div class="cancel-reason">
            <label
              >Ghi chú <span class="text-muted">(không bắt buộc)</span></label
            >
            <textarea
              v-model="ghiChuNoXau"
              placeholder="Nhập lý do / ghi chú về khách nợ xấu..."
              rows="4"
            ></textarea>
          </div>
        </div>

        <div class="cancel-modal-footer">
          <button class="cancel-btn-secondary" @click="modalNoXau = false">
            Hủy bỏ
          </button>
          <button class="cancel-btn-confirm" @click="handleXacNhanNoXau">
            <i class="bi bi-check2-all"></i>
            <div class="btn-light-sweep"></div>
            Xác nhận
          </button>
        </div>
      </div>
    </div>
  </transition>
</template>

<style scoped>
.cancelled-notice.debt-notice {
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
}

.debt-notice-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex: 1;
  min-width: 200px;
}

.btn-no-xau-small {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: 1px solid #ef4444;
  border-radius: 6px;
  background: transparent;
  color: #ef4444;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn-no-xau-small i {
  color: #ef4444 !important;
  font-size: 14px !important;
}

.btn-no-xau-small:hover {
  background: #fef2f2;
  color: #dc2626;
  border-color: #dc2626;
}

.btn-no-xau-small:hover i {
  color: #dc2626 !important;
}

.refund-info-row .value {
  font-weight: 600;
  color: #111827;
}
/* ===== CSS BẢNG MAIN PREMIUM ĐÃ ĐỒNG BỘ ===== */
.table-main {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  font-weight: 500;
}
.table-main th,
.table-main td {
  vertical-align: middle;
  padding: 12px 10px;
}
.table-main th {
  background: #e9f2fb;
  color: #333;
  font-weight: 700;
  font-size: 14px;
  border-bottom: none;
}

.table-main td {
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
  height: auto;
  padding: 10px;
}
.text-bold {
  font-weight: 600;
}
.text-danger {
  color: #d73737;
}

/* BADGE SỐ LƯỢNG */
.badge-qty {
  background: #f1f5f9;
  padding: 4px 14px;
  border-radius: 6px;
  font-weight: 700;
  color: #334155;
  font-size: 14px;
}

.timeline-scroll {
  max-height: 420px; /* chỉnh tùy UI */
  overflow-y: auto;
  padding-right: 8px;
}

/* optional: scrollbar đẹp hơn */
.timeline-scroll::-webkit-scrollbar {
  width: 6px;
}

.timeline-scroll::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 10px;
}

.timeline-scroll::-webkit-scrollbar-thumb:hover {
  background: #bbb;
}
.full-width-btn {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.footer-group-btns {
  display: flex;
  gap: 10px;
}

.completion-action-box {
  cursor: pointer;
  border-radius: 8px;
  transition: 0.2s;
}

.completion-action-box:hover {
  opacity: 0.9;
}
.ma-hd {
  font-weight: 500;
}
/* HINH ANH MON AN */
.food-item {
  display: flex;
  align-items: center;
  gap: 14px;
}

.food-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  flex-shrink: 0;
}

.food-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.food-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.4;
}

.danh-muc {
  display: inline-block;
  width: fit-content;
  padding: 4px 10px;
  border-radius: 7px;
  background: #eff4fc;
  color: #806b6b;
  font-size: 12px;
  font-weight: 500;
}

/* --- LỊCH SỬ HÓA ĐƠN (TIMELINE) --- */
.fsh-timeline-v3 {
  padding: 10px 5px;
}

.timeline-step-v3 {
  display: flex;
  gap: 15px;
}

.step-left {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.dot-outer {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border: 1px solid #e2e8f0;
  z-index: 2;
}

.dot-inner {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  border-color: #ce3d3d;
}

.success .dot-outer {
  border-color: #ce3d3d;
  background: #ecfdf5;
}
.success .dot-inner {
  background: #d24242;
}

.primary .dot-outer {
  border-color: #ce3d3d;
}
.primary .dot-inner {
  background: #b12929;
}

.step-line {
  width: 1px;
  flex-grow: 1;
  background: rgb(131, 167, 204);
  margin: 4px 0;
}

.step-right {
  padding-bottom: 25px;
}

.step-time-v3 {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #000;
  margin-bottom: 4px;
}
.step-msg-v3 b {
  font-size: 13px;
  color: #73859f;
  margin: 0;
  line-height: 1.5;
}

.step-msg-v3 {
  font-size: 13px;
  color: #73859f;
  margin: 0;
  line-height: 1.5;
}

.step-msg-v3 b {
  font-weight: 800;
  color: #1e293b;
}
.history-timeline {
  position: relative;
  padding-left: 20px;
}

.history-timeline::before {
  content: "";
  position: absolute;
  left: 5px;
  top: 5px;
  bottom: 5px;
  width: 2px;
  background: #f0f0f0; /* Đường nối dọc mờ */
}

.timeline-item {
  position: relative;
  padding-bottom: 25px;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

/* --- TỔNG KẾT ĐƠN HÀNG --- */
.summary-details {
  border-bottom: 1px solid #f1f1f1;
  padding-bottom: 20px;
}

.summary-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-line .label {
  font-size: 15px;
  color: #333;
}

.summary-line .value {
  font-weight: 600;
  font-size: 15px;
}

.sub-label {
  font-size: 12px;
  color: #999;
  font-style: italic;
  margin: 0;
  text-align: right;
}

.summary-total {
  text-align: right;
}

.total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-label {
  font-size: 18px;
  font-weight: 700;
  color: #111;
}

.total-value {
  font-size: 26px;
  font-weight: 560;
  color: #a52a2a;
}

.total-sub {
  font-size: 12px;
  color: #666;
  margin-top: -5px;
}

/* --- BUTTONS --- */

.footer-group-btns {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}

.btn-primary-red {
  background: #e93434;
  color: white;
  border: none;
  padding: 12px;
  border-radius: 8px;
  font-weight: 700;
  transition: all 0.3s;
}

.btn-primary-red:hover:not(:disabled) {
  background: #e43131;
  transform: translateY(-1px);
}

.btn-outline {
  background: white;
  border: 1px solid #ddd;
  color: #333;
  padding: 12px;
  border-radius: 8px;
  font-weight: 700;
}

.btn-cancel-outline {
  background: transparent;
  border: 1px solid #ff4d4f;
  color: #a8383a;
  width: 100%;
  padding: 10px;
  border-radius: 8px;
  font-weight: 600;
  transition: 0.3s;
}

.btn-cancel-outline:hover {
  background: #fff1f0;
}
/* Nền mờ phía sau, rất nhẹ và thoáng */
.admin-luxury-overlay {
  position: fixed;
  inset: 0;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px); /* Hiệu ứng kính mờ sang trọng */
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99999;
}

.luxury-loader-card {
  position: relative;
  background: #ffffff;
  padding: 50px 80px;
  border-radius: 2px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.05); /* Đổ bóng nhẹ nhàng */
  border: 1px solid #f0f0f0;
  text-align: center;
}

.inner-border {
  position: absolute;
  inset: 10px;
  border: 1px solid #e5e5e5;
  pointer-events: none;
}

.premium-ring-wrapper {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto 25px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ring-child {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 1px solid transparent;
  border-top-color: #f03232; /* Vàng Champagne nhạt */
  border-radius: 50%;
  animation: luxury-spin 1.5s cubic-bezier(0.5, 0, 0.5, 1) infinite;
}

.ring-child:nth-child(2) {
  width: 80%;
  height: 80%;
  animation-delay: 0.2s;
  border-top-color: #e0e0e0;
}

.logo-placeholder {
  font-family: "Serif";
  font-size: 20px;
  color: #ef4545;
  font-weight: 300;
}

.luxury-status {
  font-family: "Inter", sans-serif;
  text-transform: uppercase;
  letter-spacing: 3px;
  font-size: 13px;
  color: #222;
  font-weight: 500;
  margin: 0;
}

.status-line {
  width: 40px;
  height: 1px;
  background: #c5a059;
  margin: 15px auto;
}

.luxury-note {
  font-size: 13px;
  color: #999;
  letter-spacing: 1px;
}

@keyframes luxury-spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
/* Tiêu đề  */

/* HEADER LEFT */
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* ICON BO TRÒN */
.icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: #f3e8e8; /* nền hồng nhạt giống ảnh */
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-wrapper img {
  width: 28px;
  height: 28px;
}

/* GROUP TITLE */
.title-group {
  display: flex;
  flex-direction: column;
}

/* TITLE */
.title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #111;
}

/* SUBTITLE */
.subtitle-text {
  margin: 4px 0 0;
  font-size: 13px;
  color: #6b7280;
}
.title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #111; /* chữ đen như file Set Lẩu */
  font-weight: 600;
}

.title img {
  width: 24px;
  height: 24px;
}

.box-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* nút icon giống bên Set Lẩu */
.icon-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  border: none;
  background: #f4f6f8;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  position: relative;
  transition: all 0.25s ease;
}

.icon-btn img {
  width: 18px;
  height: 18px;
}

.icon-btn:hover {
  background: #e9eef3;
  transform: translateY(-1px);
}

/* tooltip */
.icon-btn::after {
  content: attr(data-tooltip);
  position: absolute;
  bottom: -35px;
  left: 50%;
  transform: translateX(-50%);
  white-space: nowrap;
  background: #333;
  color: #fff;
  padding: 5px 10px;
  border-radius: 6px;
  font-size: 12px;
  opacity: 0;
  transition: 0.3s;
  pointer-events: none;
}

.icon-btn:hover::after {
  opacity: 1;
}

button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.cancel-wrapper {
  display: flex;
  align-items: center;
  gap: 15px;
  background: linear-gradient(135deg, #ffeaea, #ffd6d6);
  border: 1px solid #ff4d4f;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 20px;
}

.cancel-icon {
  font-size: 28px;
  color: #ff4d4f;
}

.cancel-content h3 {
  margin: 0;
  color: #b71c1c;
  font-size: 18px;
  font-weight: 700;
}

.cancel-content p {
  margin: 4px 0 0;
  color: #666;
  font-size: 13px;
}

.quan-ly-hoa-don-chi-tiet {
  background-color: #f4f6f9;
  min-height: 100vh;
}

.box {
  background: #fff;
  border-radius: 5px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
}
.box-title-tt {
  font-size: 18px;
  color: rgb(44, 21, 21);
  font-weight: 500;
}

.box-title h3 {
  font-weight: 700;
  font-size: 20px;
}
/* HEADER */
.title-page {
  font-size: 20px;
  font-weight: 700;
  color: #a52a2a;
  margin: 0;
}
.btn-back {
  background: none;
  border: none;
  cursor: pointer;
}
.btn-back img {
  width: 24px;
}

/* STEPPER */
.stepper-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 50px;
}
.step {
  text-align: center;
  position: relative;
  z-index: 2;
}
.step .circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #fff;
  border: 2px solid #ddd;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 8px;
  color: #ddd;
}
.step.completed .circle,
.step.active .circle {
  border-color: #db3636;
  color: #de3333;
}
.step.active .circle {
  background: #e13434;
  color: #fff;
}
.step .label {
  font-size: 11px;
  font-weight: 700;
  color: #888;
}
.step.active .label {
  color: #a52a2a;
}
.line {
  flex-grow: 1;
  height: 2px;
  background: #ddd;
  margin-bottom: 25px;
}
.line.active {
  background: #a52a2a;
}

/* INFO GRID */
.info-detail-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  border-top: 1px solid #eee;
  padding-top: 20px;
}
.info-item {
  border-right: 1px solid #eee;
  padding: 0 15px;
}
.info-item label {
  display: block;
  font-size: 13px;
  color: #999;
  margin-bottom: 5px;
}
.info-item .value {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}
.info-item .value.bold {
  font-weight: 700;
}
.no-border {
  border: none !important;
}

/* TABLE */
.cozy-table thead th {
  background-color: #eaf1ff !important;
  color: #000000 !important;
  font-size: 15px;
  padding: 12px;
}
.cozy-table td {
  font-weight: 600;
}
.empty-row {
  text-align: center;
  color: #bbb;
}
.empty-content i {
  font-size: 40px;
  display: block;
  margin-bottom: 10px;
}

/* HISTORY */
.action-text {
  font-weight: 700;
  color: rgb(77, 50, 50);
}
.history-item {
  display: flex;
  gap: 15px;
  padding-bottom: 15px;
  border-left: 2px solid #eee;
  margin-left: 10px;
  padding-left: 20px;
  position: relative;
}
.history-dot {
  position: absolute;
  left: -7px;
  top: 0;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #ffc107;
}
.action-name {
  font-weight: 700;
  color: #333;
}
.action-time {
  font-size: 12px;
  color: #999;
}
.action-note {
  font-style: italic;
  color: #666;
  margin: 0;
  font-size: 13px;
}

/* SUMMARY */
.summary-row {
  display: flex;
  justify-content: space-between;
  font-size: 15px;
}
.total-row {
  border-top: 2px solid #eee;
}

/* BUTTONS ACTION */
.btn-cancel-hd {
  background: #fff;
  border: 1px solid #ddd;
  padding: 8px 20px;
  border-radius: 6px;
  color: #a52a2a;
  font-weight: 600;
}
.btn-action {
  padding: 10px 20px;
  border-radius: 6px;
  border: none;
  font-weight: 700;
  text-transform: uppercase;
  font-size: 13px;
}
.confirm-mail {
  background: #a52a2a;
  color: #fff;
}
.back {
  background: #fff;
  border: 1px solid #ddd;
}
.print {
  background: #a52a2a;
  color: #fff;
}

/* BUTTON HOAN THANH HOA DON */
.completion-action-box {
  background-color: #fafafa;
  border: 1px solid #22c55e;
  color: #15803d;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}
.action-footer {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.completion-action-box:hover {
  background-color: #f3f3f3;
  transform: translateY(-2px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.icon-circle-check {
  color: red;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;

  font-size: 18px;
}

.is-loading {
  opacity: 0.6;
  pointer-events: none;
}

/* ///////////// DA LEN MON & CHUA LEN MON */
.status-done,
.status-pending {
  padding: 4px 12px;
  border-radius: 99px;
  font-size: 13px;
  font-weight: 700;

  letter-spacing: 0.5px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.status-done {
  background: rgba(235, 87, 87, 0.12);
  color: #b72d2d;
  border: 1px solid rgba(16, 185, 129, 0.2);
}

.status-pending {
  background: rgba(202, 55, 55, 0.12);
  color: #ff0000;
  border: 1px solid rgba(245, 158, 11, 0.2);
}

/* ////////////// HUY HOA DON ///////////////// */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-box {
  background: #ffffff;
  width: 100%;
  max-width: 450px;
  border-radius: 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1),
    0 10px 10px -5px rgba(0, 0, 0, 0.04);
  overflow: hidden;
  animation: slideUp 0.3s ease-out;
}

.modal-header {
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f3f4f6;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #111827;
}

.modal-body {
  padding: 24px;
}

/* Card tóm tắt tiền cọc */
.refund-summary-card {
  background: #f9fafb;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
  border: 1px solid #e5e7eb;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.summary-item:last-child {
  margin-bottom: 0;
}

.summary-item .label {
  color: #6b7280;
  font-size: 0.95rem;
}

.summary-item .value {
  font-weight: 700;
  font-size: 1rem;
}

/* Textarea & Label */
.input-group label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
  margin-bottom: 8px;
}

textarea {
  width: 100%;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  font-family: inherit;
  resize: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

textarea:focus {
  outline: none;
  border-color: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}

.modal-footer {
  padding: 16px 24px;
  background: #f9fafb;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

/* Buttons */
.btn-secondary {
  padding: 10px 18px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  background: white;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-secondary:hover {
  background: #f3f4f6;
}

.btn-danger-confirm {
  padding: 10px 20px;
  border-radius: 8px;
  border: none;
  background: #ef4444;
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}

.btn-danger-confirm:hover:not(:disabled) {
  background: #dc2626;
}

.btn-danger-confirm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Hiệu ứng Scale */
@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
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

/* /////////////TỔNG KẾT ĐƠN HÀNG ////////// */
.summary-box {
  display: flex;
  flex-direction: column;
  border-radius: 5px;
  overflow: hidden;
}

.summary-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  font-weight: 700;
  color: #1a1a1a;
  padding-bottom: 16px;
  margin-bottom: 4px;
  border-bottom: 1px solid #f0f0f0;
}

.summary-header-icon {
  font-size: 20px;
  color: #d73737;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.summary-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px 0 4px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
}

.summary-row-label {
  font-size: 14px;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 8px;
}

.summary-row-value {
  font-size: 14px;
  font-weight: 600;
  color: #111;
}

.text-green {
  color: #16a34a;
}

.discount-badge {
  display: inline-block;
  background: #fef3f2;
  color: #d73737;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 99px;
  border: 1px solid #fecaca;
}

.summary-divider {
  height: 1px;
  background: #f0f0f0;
  margin: 2px 0;
}

.summary-total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fdf2f2;
  border-radius: 10px;
  padding: 14px 16px;
  margin-top: 4px;
}

.summary-total-label {
  font-size: 14px;
  font-weight: 600;
  color: #6b7280;
}

.summary-total-value {
  font-size: 20px;
  font-weight: 700;
  color: #d73737;
}

.summary-total-note {
  text-align: right;
  font-size: 12px;
  color: #9ca3af;
  margin: 4px 0 0;
}

/* ACTIONS */
.btn-hoan-thanh {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px;
  border-radius: 10px;
  background: #f0fdf4;
  border: 1px solid #86efac;
  color: #15803d;
  font-weight: 700;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-hoan-thanh:hover {
  background: #dcfce7;
  transform: translateY(-1px);
}

.confirmed-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f0fdf4;
  border: 1px solid #86efac;
  border-radius: 10px;
  color: #15803d;
  font-size: 14px;
  font-weight: 600;
}

.btn-export-pdf {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 13px;
  border: none;
  border-radius: 10px;
  background: #d73737;
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-export-pdf:hover {
  background: #b91c1c;
  transform: translateY(-1px);
}

/* //////////////// HỦY HÓA ĐƠN /////////////// */
.cancel-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.cancel-modal {
  background: #fff;
  width: 100%;
  max-width: 460px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
  animation: slideUp 0.3s ease-out;
}

.cancel-modal-header {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 24px;
  border-bottom: 1px solid #f3f4f6;
}

.cancel-modal-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: #fef2f2;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 20px;
  color: #ef4444;
}

.cancel-modal-header h3 {
  margin: 0 0 4px;
  font-size: 17px;
  font-weight: 700;
  color: #111;
}

.cancel-modal-header p {
  margin: 0;
  font-size: 13px;
  color: #9ca3af;
}

.cancel-modal-close {
  margin-left: auto;
  background: #f3f4f6;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #6b7280;
  transition: all 0.2s;
  flex-shrink: 0;
}

.cancel-modal-close:hover {
  background: #e5e7eb;
  color: #111;
}

.cancel-modal-body {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.refund-info {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
}

.refund-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  font-size: 14px;
  color: #374151;
}

.refund-info-row:not(:last-child) {
  border-bottom: 1px solid #e5e7eb;
}

.refund-positive {
  font-weight: 700;
  color: #16a34a;
}

.refund-negative {
  font-weight: 700;
  color: #ef4444;
}

.cancel-reason label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.cancel-reason .required {
  color: #ef4444;
}

.cancel-reason textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  font-size: 14px;
  resize: none;
  font-family: inherit;
  transition: border-color 0.2s, box-shadow 0.2s;
  box-sizing: border-box;
}

.cancel-reason textarea:focus {
  outline: none;
  border-color: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}

.cancel-modal-footer {
  display: flex;
  gap: 10px;
  padding: 16px 24px;
  background: #f9fafb;
  border-top: 1px solid #f3f4f6;
}

.cancel-btn-secondary {
  flex: 1;
  padding: 11px;
  border: 1px solid #e5e7eb;
  border-radius: 7px;
  background: #fff;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-btn-secondary:hover {
  background: #f3f4f6;
}

.cancel-btn-confirm {
  position: relative;
  overflow: hidden;
  flex: 2;
  padding: 11px;
  border: none;
  border-radius: 7px;
  background: #f12b2b;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-btn-confirm:hover {
  background: #dc2626;
  transform: translateY(-1px);
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
.cancel-btn-confirm:hover .btn-light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
}
.cancel-btn-confirm:hover .btn-light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
}

.fade-scale-enter-active,
.fade-scale-leave-active {
  transition: all 0.3s ease;
}

.fade-scale-enter-from,
.fade-scale-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

/* ////////////// NÚT XÁC NHẬN ĐÃ HOÀN CỌC /////////// */
.btn-hoan-coc-outline {
  background: transparent;
  border: 1px solid #16a34a;
  color: #16a34a;
  width: 100%;
  padding: 10px;
  border-radius: 8px;
  font-weight: 600;
  transition: 0.3s;
}

.btn-hoan-coc-outline:hover {
  background: #f0fdf4;
}

.cancelled-notice {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  background: #fef9f0;
  border: 1px solid #fde68a;
  border-radius: 10px;
  color: #92400e;
}

.cancelled-notice i {
  font-size: 20px;
  color: #f59e0b;
  flex-shrink: 0;
  margin-top: 2px;
}

.cancelled-notice-title {
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 700;
}

.cancelled-notice-sub {
  margin: 0;
  font-size: 13px;
  color: #b45309;
}

.serving-badge {
  background: #eff6ff;
  border-color: #93c5fd;
  color: #1d4ed8;
}

.waiting-payment-badge {
  background: #fff7ed;
  border-color: #fdba74;
  color: #c2410c;
}

.row.mt-4 {
  align-items: stretch;
}

.history-box,
.summary-box {
  height: 100%;
}

.fsh-timeline-v3 {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 10px 5px;
}

.timeline-scroll {
  max-height: 300px;
  overflow-y: auto;
  padding-right: 8px;
}

.history-box {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.debt-wrapper {
  display: flex;
  align-items: center;
  gap: 15px;
  background: linear-gradient(135deg, #fffbeb, #fef3c7);
  border: 1px solid #f59e0b;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 20px;
}
.debt-icon {
  font-size: 28px;
  color: #b45309;
}
.debt-content h3 {
  margin: 0;
  color: #92400e;
  font-size: 18px;
  font-weight: 700;
}
.debt-content p {
  margin: 4px 0 0;
  color: #78350f;
  font-size: 13px;
}
.bad-debt-wrapper {
  display: flex;
  align-items: center;
  gap: 15px;
  background: linear-gradient(135deg, #fef2f2, #fee2e2);
  border: 1px solid #fca5a5;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 20px;
}
.bad-debt-icon {
  font-size: 28px;
  color: #ef4444;
}
.bad-debt-content h3 {
  margin: 0;
  color: #991b1b;
  font-size: 18px;
  font-weight: 700;
}
.bad-debt-content p {
  margin: 4px 0 0;
  color: #7f1d1d;
  font-size: 13px;
}
.cancelled-notice.bad-debt-notice {
  background: #fef2f2;
  border-color: #fca5a5;
  color: #991b1b;
}
.cancelled-notice.bad-debt-notice i {
  color: #ef4444;
}
.cancelled-notice.bad-debt-notice .cancelled-notice-sub {
  color: #b91c1c;
}
.status-cancelled {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 99px;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.5px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  background: rgba(107, 114, 128, 0.12);
  color: #4b5563;
  border: 1px solid rgba(107, 114, 128, 0.2);
}
</style>
