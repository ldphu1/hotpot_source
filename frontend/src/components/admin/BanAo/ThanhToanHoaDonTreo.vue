<script setup>
import { ref, onMounted, computed, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUtils } from "@/utils/helper";
import iconBack from "@/assets/icons/prev.png";
import { getDetailHoaDonTreo } from "@/api/admin/banao/banAoApi";
import { xacNhanThanhToanThuCong } from "@/api/admin/banao/banAoApi";
import { getAllPhuongThucThanhToan } from "@/api/admin/thanhtoan/thanhToanApi";
import { inHoaDonThanhToan } from "@/api/admin/thanhtoan/thanhToanApi";
import { getQrChuyenKhoan } from "@/api/admin/thanhtoan/thanhToanApi";
import ZaloPay from "@/assets/icons/ZaloPay.png";
import VNPAY from "@/assets/icons/VNPay.png";

const hienModalQr = ref(false);
const qrCodeUrl = ref("");
const dangTaoQr = ref(false);
const route = useRoute();
const router = useRouter();
const { notify, nprogress } = useUtils();

const idBanAo = Number(route.params.idBanAo);

const danhSachPhuongThuc = ref([]);
const dangXuLy = ref(false);
const dangTaoLinkVnpay = ref(false);
const paymentUrl = ref("");
const errorPhuongThuc = ref("");

const form = ref({
  idPhuongThucThanhToan: null,
});

const banAo = ref({
  idBanAo: idBanAo,
  maBanAo: "",
  trangThaiThanhToan: "",
  idHoaDon: null,
  maHoaDon: "",
  thoiGianTaoHoaDon: "",
  tenKhachHang: "",
  soDienThoai: "",
  tenBanGoc: "",
  tienCoc: "",
  maGiamGia: null,
  giaTriGiam: null,
  tenKhuVucGoc: "",
  tongTienMon: 0,
  soTienDuocGiam: 0,
  giamTrucTiep: 0,
  tienSauGiam: 0,
  diemTichLuySuDung: 0,
  tienDaThanhToan: 0,
  tienConLai: 0,
  tongThanhToan: 0,
  dsMon: [],
});

/* ================= LOAD DATA ================= */
async function loadChiTiet() {
  nprogress.start();
  try {
    const res = await getDetailHoaDonTreo(idBanAo);
    banAo.value = res.data;
  } catch (e) {
    console.error("Lỗi load chi tiết bill treo:", e);
    notify.error("Không tải được chi tiết bill treo");
  } finally {
    nprogress.done();
  }
}

async function loadPhuongThuc() {
  try {
    const res = await getAllPhuongThucThanhToan();
    danhSachPhuongThuc.value = Array.isArray(res.data) ? res.data : [];

    const tienMat = danhSachPhuongThuc.value.find((p) => {
      const ten = String(p.tenPhuongThuc || "")
        .trim()
        .toLowerCase();
      return ten === "tiền mặt" || ten === "tien mat";
    });
    if (tienMat) form.value.idPhuongThucThanhToan = tienMat.id;
  } catch (e) {
    console.error("Lỗi load phương thức thanh toán:", e);
  }
}

onMounted(() => {
  loadChiTiet();
  loadPhuongThuc();
});

/* ================= FORMAT ================= */
const formatTien = (value) => {
  const num = Number(value || 0);
  return new Intl.NumberFormat("vi-VN").format(num) + " đ";
};

const formatDateTime = (v) => (v ? new Date(v).toLocaleString("vi-VN") : "--");

const layAnhMon = (hinhAnh) => {
  if (!hinhAnh) return "/default-food.png";
  if (hinhAnh.startsWith("http")) return hinhAnh;
  return `http://localhost:8080/uploads/${hinhAnh.replace(/^\/+/, "")}`;
};
function onImgError(e) {
  e.target.src = "/default-food.png";
}

/* ================= COMPUTED ================= */
const dsMonGrouped = computed(() => {
  const grouped = {};
  (banAo.value.dsMon || []).forEach((mon) => {
    const dm = mon.tenDanhMuc || "Khác";
    if (!grouped[dm]) grouped[dm] = [];
    grouped[dm].push(mon);
  });
  return grouped;
});

const danhMucDangChon = ref("Tất cả");
const danhSachDanhMuc = computed(() => [
  "Tất cả",
  ...Object.keys(dsMonGrouped.value),
]);

const dsMonHienThi = computed(() => {
  if (danhMucDangChon.value === "Tất cả") return banAo.value.dsMon || [];
  return dsMonGrouped.value[danhMucDangChon.value] || [];
});

const laDaThanhToan = computed(
  () => banAo.value.trangThaiThanhToan === "DA_THANH_TOAN",
);

// Tiền khách đã thanh toán thêm ngoài tiền cọc
const tienThanhToanThem = computed(() => {
  const daThanhToan = Number(banAo.value?.tienDaThanhToan || 0);
  const tienCoc = Number(banAo.value?.tienCoc || 0);
  return Math.max(daThanhToan - tienCoc, 0);
});

// Số tiền còn phải thanh toán
const tienPhaiThanhToan = computed(() => {
  if (laDaThanhToan.value) return 0;
  if (banAo.value?.tienConLai != null) {
    return Number(banAo.value.tienConLai);
  }
  return Number(banAo.value?.tongThanhToan || 0);
});

/* ================= ACTION ================= */
async function inHoaDonTam() {
  try {
    nprogress.start();
    const res = await inHoaDonThanhToan(banAo.value.idHoaDon);
    const blob = new Blob([res.data], { type: "application/pdf" });
    const url = window.URL.createObjectURL(blob);
    window.open(url, "_blank");
  } catch (e) {
    notify.error("Không thể in hóa đơn");
  } finally {
    nprogress.done();
  }
}

async function xacNhanThanhToan() {
  errorPhuongThuc.value = "";

  if (!form.value.idPhuongThucThanhToan) {
    errorPhuongThuc.value = "Vui lòng chọn phương thức thanh toán";
    return;
  }

  const pt = danhSachPhuongThuc.value.find(
    (p) => Number(p.id) === Number(form.value.idPhuongThucThanhToan),
  );
  if (!pt) {
    notify.error("Không tìm thấy phương thức thanh toán");
    return;
  }

  const tenPt = String(pt.tenPhuongThuc || "")
    .trim()
    .toLowerCase();
  const laTienMat = tenPt.includes("tiền mặt") || tenPt.includes("tien mat");

  if (laTienMat) {
    if (!(await notify.confirm("Xác nhận đã nhận đủ tiền mặt cho bill này?")))
      return;

    dangXuLy.value = true;
    nprogress.start();
    try {
      await xacNhanThanhToanThuCong(idBanAo);
      notify.success("Thanh toán thành công");
      router.push("/admin/hoa-don-treo");
    } catch (e) {
      notify.error(e?.response?.data?.message || "Thanh toán thất bại");
    } finally {
      dangXuLy.value = false;
      nprogress.done();
    }
    return;
  }

  // Online -> gọi API tạo QR có sẵn, dùng idHoaDon gốc đứng sau bill treo
  dangTaoQr.value = true;
  nprogress.start();
  try {
    const res = await getQrChuyenKhoan(
      banAo.value.idHoaDon,
      tienPhaiThanhToan.value,
    );
    qrCodeUrl.value = res.data?.qrCodeUrl || "";
    if (qrCodeUrl.value) {
      hienModalQr.value = true;
    } else {
      notify.error("Không tạo được mã QR");
    }
  } catch (e) {
    notify.error(e?.response?.data?.message || "Tạo mã QR thất bại");
  } finally {
    dangTaoQr.value = false;
    nprogress.done();
  }
}

function dongModalQr() {
  hienModalQr.value = false;
}

async function xacNhanTuModalQr() {
  if (!(await notify.confirm("Xác nhận khách đã chuyển khoản thành công?")))
    return;

  dangXuLy.value = true;
  nprogress.start();
  try {
    await xacNhanThanhToanThuCong(idBanAo);
    notify.success("Đã xác nhận thanh toán");
    hienModalQr.value = false;
    router.push("/admin/hoa-don-treo");
  } catch (e) {
    notify.error(e?.response?.data?.message || "Xác nhận thất bại");
  } finally {
    dangXuLy.value = false;
    nprogress.done();
  }
}

async function xacNhanDaThanhToanThuCong() {
  if (!(await notify.confirm("Xác nhận khách đã chuyển khoản thành công?")))
    return;

  dangXuLy.value = true;
  nprogress.start();
  try {
    await xacNhanThanhToanThuCong(idBanAo);
    notify.success("Đã xác nhận thanh toán");
    router.push("/admin/hoa-don-treo");
  } catch (e) {
    notify.error(e?.response?.data?.message || "Xác nhận thất bại");
  } finally {
    dangXuLy.value = false;
    nprogress.done();
  }
}

function dongTrang() {
  router.push("/admin/hoa-don-treo");
}

/* ===== PAGINATION ===== */
const currentPage = ref(1);
const pageSize = ref(5);

const totalElements = computed(() => dsMonHienThi.value.length);
const totalPages = computed(
  () => Math.ceil(totalElements.value / pageSize.value) || 1,
);

const danhSachHienThiPhanTrang = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return dsMonHienThi.value.slice(start, end);
});

const pageList = computed(() => {
  const current = currentPage.value;
  const total = totalPages.value;
  if (total <= 5) {
    return Array.from({ length: total }, (_, i) => i + 1);
  }
  if (current <= 3) {
    return [1, 2, 3, 4, "…", total];
  }
  if (current >= total - 2) {
    return [1, "…", total - 3, total - 2, total - 1, total];
  }
  return [1, "…", current - 1, current, current + 1, "…", total];
});

watch([danhMucDangChon, pageSize], () => {
  currentPage.value = 1;
});
</script>

<template>
  <div class="page-wrap" @click.self="dongTrang">
    <div class="payment-modal">
      <div class="box">
        <div class="box-header-flex">
          <div class="box-header">
            <h2 class="main-title">
              <span class="icon-wrapper">
                <i
                  class="bi bi-journal-minus"
                  style="color: rgb(225, 29, 72); font-size: 24px"
                ></i>
              </span>
              Thanh Toán Hóa Đơn Treo
            </h2>
            <p>
              Hóa đơn treo - <strong>{{ banAo.maBanAo }}</strong> / Hóa đơn -
              <strong>{{ banAo.maHoaDon }}</strong>
            </p>
          </div>
          <button class="icon-btn-back" @click="dongTrang">
            <img :src="iconBack" />
          </button>
        </div>
      </div>

      <div class="payment-body-split">
        <div class="invoice-panel">
          <div class="info-3-boxes">
            <div class="info-box">
              <div class="info-box-label">
                <i class="bi bi-person-circle" style="color: #6366f1"></i>
                Khách hàng
              </div>
              <div class="info-row">
                <span class="info-field-label"
                  ><i class="bi bi-person"></i> Họ tên:</span
                >
                <span class="info-field-value">{{
                  banAo.tenKhachHang || "Khách lẻ"
                }}</span>
              </div>
              <div class="info-row" v-if="banAo.soDienThoai">
                <span class="info-field-label"
                  ><i class="bi bi-telephone"></i> SĐT:</span
                >
                <span class="info-field-value">{{ banAo.soDienThoai }}</span>
              </div>
            </div>

            <div class="info-box">
              <div class="info-box-label">
                <i class="bi bi-geo-alt-fill" style="color: #10b981"></i>
                Bàn gốc (đã giải phóng)
              </div>
              <div class="info-row">
                <span class="info-field-label"
                  ><i class="bi bi-grid"></i> Bàn ăn:</span
                >
                <span class="info-field-value">{{
                  banAo.tenBanGoc || "--"
                }}</span>
              </div>
              <div class="info-row">
                <span class="info-field-label"
                  ><i class="bi bi-map"></i> Khu vực:</span
                >
                <span class="info-field-value">{{
                  banAo.tenKhuVucGoc || "--"
                }}</span>
              </div>
            </div>

            <div class="info-box">
              <div class="info-box-label">
                <i class="bi bi-receipt" style="color: #f59e0b"></i>
                Hóa đơn
              </div>
              <div class="info-row">
                <span class="info-field-label"
                  ><i class="bi bi-hash"></i> Mã HĐ:</span
                >
                <span class="info-field-value">{{ banAo.maHoaDon }}</span>
              </div>
              <div class="info-row">
                <span class="info-field-label"
                  ><i class="bi bi-clock"></i> Thời gian:</span
                >
                <span class="info-field-value">{{
                  formatDateTime(banAo.thoiGianTaoHoaDon)
                }}</span>
              </div>
            </div>
          </div>

          <div class="category-tabs" v-if="banAo.dsMon?.length > 0">
            <button
              v-for="dm in danhSachDanhMuc"
              :key="dm"
              class="tab-btn"
              :class="{ active: danhMucDangChon === dm }"
              @click="danhMucDangChon = dm"
            >
              {{ dm }} ({{
                dm === "Tất cả" ? banAo.dsMon.length : dsMonGrouped[dm].length
              }})
            </button>
          </div>

          <div class="table-head table-head-grid">
            <div class="head-stt">STT</div>
            <div class="head-ten">Danh sách món đã phục vụ</div>
            <div class="head-sl">Số lượng</div>
            <div class="head-gia">Đơn giá</div>
            <div class="head-tt">Thành tiền</div>
          </div>

          <div class="mon-list">
            <div v-if="!dsMonHienThi.length" class="empty-mon">
              Chưa có món nào
            </div>

            <div
              v-for="(item, index) in danhSachHienThiPhanTrang"
              :key="item.id"
              class="mon-row"
            >
              <div class="col-stt">
                {{ (currentPage - 1) * pageSize + index + 1 }}
              </div>
              <div class="col-ten">
                <div class="mon-thumb-wrap-inline">
                  <img
                    :src="layAnhMon(item.hinhAnh)"
                    class="mon-thumb-inline"
                    @error="onImgError"
                  />
                </div>
                <div class="mon-info-inline">
                  <div class="ma-sp">{{ item.maSanPham || "--" }}</div>
                  <div>{{ item.tenSanPham || "--" }}</div>
                </div>
              </div>
              <div class="col-sl">{{ item.soLuong }}</div>
              <div class="col-gia">{{ formatTien(item.donGia) }}</div>
              <div class="col-tt">{{ formatTien(item.thanhTien) }}</div>
            </div>
          </div>
          <div
            style="
              display: flex;
              justify-content: space-between;
              align-items: center;
              margin-top: 16px;
            "
          >
            <div style="display: flex; align-items: center; gap: 8px">
              <span
                style="
                  font-size: 14px;
                  font-weight: 600;
                  color: #64748b;
                  white-space: nowrap;
                "
                >Hiển thị</span
              >
              <select
                class="form-select"
                v-model="pageSize"
                style="
                  height: 36px;
                  border-radius: 7px;
                  border: 1px solid #e2e8f0;
                  padding: 0 10px;
                  font-size: 14px;
                  outline: none;
                  background: #fff;
                "
              >
                <option :value="5">5 món</option>
                <option :value="10">10 món</option>
                <option :value="20">20 món</option>
              </select>
            </div>

            <div class="pagination">
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

            <div style="font-size: 14px; color: #64748b; font-weight: 500">
              Hiển thị {{ danhSachHienThiPhanTrang.length }} /
              {{ totalElements }} món
            </div>
          </div>
        </div>

        <div class="payment-panel">
          <div class="payment-top-row">
            <div class="payment-summary-col">
              <div class="summary-card top-summary-card">
                <div class="summary-title">
                  <span class="summary-icon"
                    ><i class="bi bi-cash-coin"></i
                  ></span>
                  <span>Thông tin thanh toán</span>
                </div>

                <div class="summary-line">
                  <span>Tổng tiền món</span>
                  <strong>{{ formatTien(banAo.tongTienMon) }}</strong>
                </div>
                <div class="summary-line" v-if="banAo.tienCoc > 0">
                  <span>Tiền cọc</span>
                  <strong>- {{ formatTien(banAo.tienCoc) }}</strong>
                </div>

                <div class="summary-line" v-if="banAo.soTienDuocGiam > 0">
                  <div style="display: flex; align-items: center">
                    <span>Giảm giá</span>
                    <span v-if="banAo.maGiamGia" class="discount-badge ms-2">
                      {{ banAo.maGiamGia }}
                      <template v-if="banAo.giaTriGiam">
                        · {{ banAo.giaTriGiam }}%
                      </template>
                    </span>
                  </div>
                  <strong style="color: #ef4444"
                    >- {{ formatTien(banAo.soTienDuocGiam) }}</strong
                  >
                </div>
                <div class="summary-line" v-if="banAo.giamTrucTiep > 0">
                  <span>Giảm trực tiếp</span>
                  <strong style="color: #ef4444"
                    >- {{ formatTien(banAo.giamTrucTiep) }}</strong
                  >
                </div>

                <div class="summary-line" v-if="banAo.diemTichLuySuDung > 0">
                  <span>Điểm tích lũy đã dùng</span>
                  <strong>- {{ formatTien(banAo.diemTichLuySuDung) }}</strong>
                </div>
                <div class="summary-line" v-if="tienThanhToanThem > 0">
                  <span>{{ Number(banAo.tienCoc) > 0 ? "Đã thanh toán thêm" : "Tiền đã thanh toán" }}</span>
                  <strong style="color: #16a34a">- {{ formatTien(tienThanhToanThem) }}</strong>
                </div>

                <div class="summary-total">
                  <span>Tiền phải thanh toán</span>
                  <strong>{{ formatTien(tienPhaiThanhToan) }}</strong>
                </div>
              </div>
            </div>
          </div>

          <div v-if="laDaThanhToan" class="alert-da-thanh-toan">
            <i class="bi bi-check-circle-fill"></i>
            Bill này đã được thanh toán
          </div>

          <template v-else>
            <div class="payment-methods-row">
              <div
                class="pay-method-group"
                style="border-right: none; width: 100%"
              >
                <div class="method-group-title">
                  Chọn phương thức thanh toán
                </div>
                <div class="pay-methods pay-methods-horizontal">
                  <label
                    v-for="pt in danhSachPhuongThuc"
                    :key="pt.id"
                    class="radio-card radio-card-online"
                    :class="{
                      active:
                        Number(form.idPhuongThucThanhToan) === Number(pt.id),
                    }"
                  >
                    <input
                      type="radio"
                      :value="pt.id"
                      v-model="form.idPhuongThucThanhToan"
                      @change="
                        errorPhuongThuc = '';
                        paymentUrl = '';
                      "
                      style="display: none"
                    />
                    <img
                      v-if="
                        String(pt.tenPhuongThuc)
                          .toLowerCase()
                          .includes('zalopay')
                      "
                      :src="ZaloPay"
                      class="icon-pt"
                    />
                    <img
                      v-else-if="
                        String(pt.tenPhuongThuc).toLowerCase().includes('vnpay')
                      "
                      :src="VNPAY"
                      class="icon-pt"
                    />
                    <i
                      v-else-if="
                        String(pt.tenPhuongThuc)
                          .toLowerCase()
                          .includes('tiền mặt') ||
                        String(pt.tenPhuongThuc)
                          .toLowerCase()
                          .includes('tien mat')
                      "
                      class="bi bi-cash-stack"
                      style="font-size: 18px; color: #10b981"
                    ></i>
                    <i
                      v-else
                      class="bi bi-bank"
                      style="font-size: 18px; color: #3b82f6"
                    ></i>
                    <span>{{ pt.tenPhuongThuc }}</span>
                  </label>
                </div>
                <p v-if="errorPhuongThuc" class="error-hint mt-2 mb-0">
                  <i class="bi bi-exclamation-circle"></i> {{ errorPhuongThuc }}
                </p>
              </div>
            </div>

            <div v-if="paymentUrl" class="thong-bao-link-vnpay">
              <i class="bi bi-info-circle"></i>
              Đã mở tab thanh toán mới. Sau khi khách xác nhận chuyển khoản
              xong, bấm nút bên dưới.
              <button
                class="btn-cancel mt-2"
                @click="window.open(paymentUrl, '_blank')"
              >
                Mở lại trang thanh toán
              </button>
            </div>
          </template>

          <div class="footer-actions">
            <button class="btn-cancel" type="button" @click="inHoaDonTam">
              <i class="bi bi-printer"></i> In hóa đơn
            </button>

            <button
              v-if="!laDaThanhToan && !paymentUrl"
              class="btn-primary"
              type="button"
              :disabled="dangXuLy || dangTaoLinkVnpay"
              @click="xacNhanThanhToan"
            >
              <div class="btn-light-sweep"></div>
              {{
                dangTaoLinkVnpay
                  ? "Đang tạo link..."
                  : dangXuLy
                  ? "Đang xử lý..."
                  : "Xác nhận"
              }}
            </button>

            <button
              v-if="!laDaThanhToan && paymentUrl"
              class="btn-primary"
              type="button"
              :disabled="dangXuLy"
              @click="xacNhanDaThanhToanThuCong"
            >
              <div class="btn-light-sweep"></div>
              {{ dangXuLy ? "Đang xử lý..." : "Xác nhận đã thanh toán" }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
  <!-- Modal thanh toán VNPay -->
  <!-- Modal QR thanh toán -->
  <div v-if="hienModalQr" class="modal-qr-overlay" @click.self="dongModalQr">
    <div class="modal-qr-container">
      <div class="modal-qr-header">
        <span>Quét mã để thanh toán</span>
        <button class="btn-close-vnpay" @click="dongModalQr">×</button>
      </div>

      <div class="modal-qr-body">
        <div class="qr-info-row">
          <span>Mã hóa đơn</span>
          <strong>{{ banAo.maHoaDon }}</strong>
        </div>
        <div class="qr-info-row">
          <span>Khách hàng</span>
          <strong>{{ banAo.tenKhachHang }}</strong>
        </div>
        <div class="qr-info-row qr-total">
          <span>Số tiền</span>
          <strong>{{ formatTien(tienPhaiThanhToan) }}</strong>
        </div>

        <div class="qr-image-wrapper">
          <img :src="qrCodeUrl" class="qr-image-large" alt="QR thanh toán" />
        </div>

        <p class="qr-note">
          Đưa điện thoại cho khách quét mã bằng app ngân hàng/ví điện tử. Sau
          khi khách chuyển khoản xong, bấm xác nhận bên dưới.
        </p>
      </div>

      <div class="modal-qr-footer">
        <button class="btn-cancel" @click="dongModalQr">Đóng</button>
        <button
          class="btn-primary"
          :disabled="dangXuLy"
          @click="xacNhanTuModalQr"
        >
          <div class="btn-light-sweep"></div>
          {{ dangXuLy ? "Đang xử lý..." : "Xác nhận đã thanh toán" }}
        </button>
      </div>
    </div>
  </div>
</template>
<style scoped>
.error-text {
  color: #dc2626;
  font-size: 13px;
  margin-top: 6px;
  display: block;
  font-weight: 500;
}
.error-phuong-thuc {
  margin-bottom: 12px;
}

/* === 3 INFO BOXES === */
.info-3-boxes {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.info-box {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px 20px;
}
.info-box-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: #64748b;
  margin-bottom: 8px;
}
.info-box-label i {
  font-size: 18px;
  flex-shrink: 0;
}
.info-box-value {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
}
.info-box-sub {
  font-size: 13px;
  color: #64748b;
  margin-top: 4px;
}
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 5px 0;
  border-bottom: 1px dashed #f1f5f9;
  gap: 8px;
}
.info-row:last-child {
  border-bottom: none;
}
.info-field-label {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: #94a3b8;
  font-weight: 500;
  font-size: 13px;
  white-space: nowrap;
  flex-shrink: 0;
}
.info-field-label i {
  font-size: 13px;
}
.info-field-value {
  font-size: 13.5px;
  font-weight: 600;
  color: #1e293b;
  text-align: right;
}

/* === PAYMENT TOP ROW (2 columns) === */
.payment-top-row {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 16px;
}
.payment-summary-col {
  display: flex;
  flex-direction: column;
}
.payment-summary-col .summary-card {
  flex: 1;
  height: 100%;
  box-sizing: border-box;
}
.payment-input-col {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* === PAYMENT METHODS ROW (horizontal) === */
.payment-methods-row {
  display: flex;
  gap: 0;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 16px;
}
.pay-method-group {
  display: flex;
  flex-direction: column;
  padding: 14px 16px;
  min-width: 0;
}
.pay-method-direct {
  flex: 0 0 140px;
  border-right: 1px solid #e2e8f0;
}
.pay-method-online {
  flex: 1;
  border-right: 1px solid #e2e8f0;
}
.pay-method-group + .pay-method-group {
  border-left: none;
  padding-left: 16px;
}
.method-group-title {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: #94a3b8;
  margin-bottom: 10px;
}
.pay-methods-vertical {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.pay-methods-horizontal {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 6px;
  width: 100%;
}
.radio-card-online {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  padding: 8px 4px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  background: #fff;
  transition: all 0.2s;
  gap: 4px;
}
.radio-card-online:hover {
  border-color: #cbd5e1;
  background: #f8fafc;
}
.radio-card-online.active {
  border-color: var(--color-primary, #e65a5a);
  background: #fff1f2;
}
.radio-card-online input[type="radio"] {
  display: none;
}
.radio-card-online .icon-pt {
  width: 20px;
  height: 20px;
  object-fit: contain;
  display: block;
}
.radio-card-online span {
  font-size: 11px;
  font-weight: 600;
  line-height: 1.2;
  white-space: nowrap;
}
.qr-method-col {
  display: flex;
  flex-direction: column;
  flex: 0 0 180px;
  padding: 14px 16px;
}

.thanh-toan-container,
.page-wrap,
.payment-modal,
.invoice-panel,
.payment-panel,
.mon-row,
.summary-row,
button,
input,
label,
span,
div {
  font-family: var(--font-family, "Inter", sans-serif);
  text-transform: none;
  box-sizing: border-box;
}

.page-wrap {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.payment-modal {
  width: 100%;
  height: 100%;
  max-width: none;
  max-height: none;
  margin: 0;
  background: transparent;
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-shadow: none;
  border: none;
}

.payment-body-split {
  display: flex;
  flex-direction: row;
  gap: 20px;
  min-height: 0;
  flex: 1;
}

/* ================= LEFT ================= */
.invoice-panel {
  flex: 7;
  padding: 20px;
  border-radius: 10px;
  border: 1px solid #eee;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03);
  display: flex;
  flex-direction: column;
  min-height: 0;
  background: #fff;
}

.invoice-title {
  font-size: 20px;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.4;
  letter-spacing: -0.02em;
}

.ban-info {
  color: var(--color-primary);
  font-weight: 700;
  font-size: 15px;
  margin-left: 4px;
}

.btn-close {
  width: 42px;
  height: 42px;
  border-radius: 7px;
  background: #f3f4f6;
  border: none;
  font-size: 24px;
  color: #94a3b8;
  cursor: pointer;
  font-weight: 700;
  line-height: 1;
  transition: var(--transition);
}

.btn-close:hover {
  background: rgba(230, 90, 90, 0.12);
  color: var(--color-primary);
  transform: rotate(90deg);
}

.customer-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  color: #475569;
  font-size: 14px;
  font-weight: 500;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 7px;
  background: #f8fafc;
  border: 1px solid #eef2f7;
}

.customer-name {
  font-weight: 700;
  color: #334155;
}

.invoice-time {
  color: #64748b;
  font-size: 13px;
  white-space: nowrap;
}

.tag-row {
  margin-bottom: 14px;
}

.partial-tag {
  display: inline-flex;
  align-items: center;
  padding: 7px 14px;
  border-radius: 999px;
  background: #effcf2;
  color: #22a447;
  border: 1px solid #86efac;
  font-size: 13px;
  font-weight: 700;
}

.table-head {
  background: #f3f4f6;
  padding: 12px 14px;
  border-radius: 7px;
  font-size: 14px;
  font-weight: 800;
  color: #475569;
  margin-bottom: 8px;
}

.table-head-grid {
  display: grid;
  grid-template-columns: 42px 1.8fr 70px 120px 130px;
  gap: 10px;
  align-items: center;
}

.head-stt {
  text-align: center;
}
.head-ten {
  text-align: left;
}
.head-sl {
  text-align: center;
}
.head-gia,
.head-tt {
  text-align: right;
}

.mon-list {
  flex: 1;
  overflow-y: auto;
  padding-right: 4px;
}

.mon-list::-webkit-scrollbar {
  width: 6px;
}
.mon-list::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 999px;
}
.mon-list::-webkit-scrollbar-track {
  background: transparent;
}

.category-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  overflow-x: auto;
  padding-bottom: 4px;
}
.category-tabs::-webkit-scrollbar {
  height: 4px;
}
.category-tabs::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 999px;
}
.tab-btn {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  color: #64748b;
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s ease;
}
.tab-btn:hover {
  background: #f1f5f9;
  color: #334155;
}
.tab-btn.active {
  background: #fee2e2;
  color: var(--color-primary);
  border-color: #fca5a5;
}
:deep(body.dark-mode) .tab-btn {
  background: #1f1f1f;
  border-color: #2a2a2a;
  color: #a3a3a3;
}
:deep(body.dark-mode) .tab-btn:hover {
  background: #2a2a2a;
  color: #e5e5e5;
}
:deep(body.dark-mode) .tab-btn.active {
  background: rgba(230, 90, 90, 0.15);
  color: #ffb0b0;
  border-color: rgba(230, 90, 90, 0.3);
}

.category-header {
  font-size: 14px;
  font-weight: 800;
  color: #1e293b;
  padding: 6px 10px;
  background: #f1f5f9;
  border-left: 3px solid var(--color-primary);
  border-radius: 4px;
  margin: 12px 0 4px;
}

.empty-mon {
  padding: 24px 10px;
  color: #94a3b8;
  font-size: 14px;
  text-align: center;
  border: 1px dashed #dbe3ec;
  border-radius: 7px;
  background: #fafcff;
}

.mon-row {
  display: grid;
  grid-template-columns: 42px 1.8fr 70px 120px 130px;
  gap: 10px;
  align-items: center;
  padding: 14px 10px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 15px;
  color: #334155;
  transition: var(--transition);
}

.mon-row:hover {
  background: #fafbfc;
  border-radius: 7px;
}

.col-stt {
  text-align: center;
  font-weight: 700;
  color: #64748b;
}

.col-ten {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.45;
}

.mon-info-inline {
  display: flex;
  flex-direction: column;
}

.mon-thumb-wrap-inline {
  width: 48px;
  height: 48px;
  border-radius: 7px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f3f4f6;
  border: 1px solid #eef2f7;
}

.mon-thumb-inline {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.mon-thumb-inline.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 11px;
  font-weight: 500;
}

.ma-sp {
  font-size: 12px;
  font-weight: 800;
  color: var(--color-primary);
  margin-bottom: 2px;
}

.col-sl {
  text-align: center;
  font-weight: 600;
  color: #334155;
}

.col-gia,
.col-tt {
  text-align: right;
  font-weight: 700;
  color: #334155;
}

/* ================= RIGHT ================= */
.payment-panel {
  flex: 3;
  padding: 20px;
  border-radius: 10px;
  border: 1px solid #eee;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03);
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow-y: auto;
  background: #fff;
  gap: 12px;
}

.payment-panel::-webkit-scrollbar {
  width: 6px;
}
.payment-panel::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 999px;
}
.payment-panel::-webkit-scrollbar-track {
  background: transparent;
}

.summary-card {
  margin-top: 2px;
  background: linear-gradient(
    135deg,
    rgba(230, 90, 90, 0.08),
    rgba(230, 90, 90, 0.03)
  );
  border: 1px solid rgba(230, 90, 90, 0.18);
  border-radius: 7px;
  padding: 16px;
}

.summary-title {
  font-weight: 800;
  color: var(--color-primary);
  margin-bottom: 14px;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.summary-icon {
  font-size: 15px;
}

.summary-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  color: #334155;
  font-size: 14px;
  font-weight: 500;
  gap: 12px;
}

.summary-line span:first-child {
  color: #64748b;
  font-weight: 600;
}

.summary-line strong {
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
  text-align: right;
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
  vertical-align: middle;
}
.summary-total {
  margin-top: 6px;
  padding-top: 16px;
  border-top: 1px solid rgba(230, 90, 90, 0.18);
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 20px;
  font-weight: 800;
  color: #0f172a;
  gap: 12px;
}

.summary-total strong {
  color: #d12839;
  font-size: 20px;
  font-weight: 800;
  text-align: right;
}

.pay-input-box {
  background: #fff;
}

.pay-input-box label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 700;
  color: #334155;
}

.pay-input-box input {
  width: 100%;
  height: 48px;
  border: 1px solid #d1d5db;
  border-radius: 7px;
  font-size: 16px;
  font-weight: 700;
  outline: none;
  color: #0f172a;
  background: #fff;
  padding: 0 14px;
  transition: var(--transition);
}

.pay-input-box input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.1);
}

.pay-input-box input::placeholder {
  color: #94a3b8;
  font-weight: 500;
}

.pay-methods {
  display: flex;
  gap: 12px 16px;
  flex-wrap: wrap;
  margin-top: 2px;
  margin-bottom: 4px;
}

.radio-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #475569;
  cursor: pointer;
  font-weight: 600;
  line-height: 1.2;
  padding: 8px 10px;
  border-radius: 7px;
  transition: var(--transition);
}

.radio-item:hover {
  background: #f8fafc;
}

.radio-item input[type="radio"] {
  accent-color: var(--color-primary);
  cursor: pointer;
  margin: 0;
}

.icon-pt {
  width: 18px;
  height: 18px;
  object-fit: contain;
  margin-right: 2px;
  vertical-align: middle;
}

.qr-wrapper {
  min-height: 130px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: flex-start;
  gap: 10px;
  padding: 10px 0 4px;
}

.payment-icon {
  margin-bottom: 0;
}

.payment-icon img {
  width: 74px;
  height: auto;
  object-fit: contain;
  display: block;
}

.qr-area {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
}

.qr-image {
  width: 128px;
  height: 128px;
  border: 1px solid #d1d5db;
  padding: 6px;
  background: #fff;
  border-radius: 7px;
  box-shadow: 0 4px 10px rgba(15, 23, 42, 0.06);
}

.btn-show-qr {
  background: #f3f4f6;
  border: 1px solid #d1d5db;
  border-radius: 7px;
  padding: 8px 12px;
  color: #475569;
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
  text-decoration: none;
  transition: var(--transition);
}

.btn-show-qr:hover {
  background: #e5e7eb;
  color: #0f172a;
}

.qr-placeholder {
  color: #64748b;
  font-size: 14px;
  line-height: 1.5;
  padding-top: 2px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  font-size: 15px;
  border-bottom: 1px solid #f1f5f9;
  color: #475569;
  font-weight: 500;
  gap: 12px;
}

.summary-row strong {
  font-size: 15px;
  color: #0f172a;
  font-weight: 700;
  text-align: right;
  white-space: nowrap;
}

.tien-thua {
  margin-top: 2px;
  padding-top: 12px;
  border-bottom: none;
  font-size: 15px;
  font-weight: 700;
}

.tien-thua strong {
  font-size: 18px;
  color: #0f172a;
}

.footer-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
  background: #fff;
}
.footer-actions .btn-cancel {
  width: 100%;
}
.footer-actions .btn-primary {
  width: 100%;
}

.btn-primary,
.btn-cancel {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 12px 24px;
  border-radius: 7px;
  font-weight: 600;
  font-size: 14px;
  transition: 0.2s;
  text-transform: none;
  cursor: pointer;
  border: none;
}

.btn-primary {
  position: relative;
  overflow: hidden;
  background: #d73737;
  color: #fff;
}

.btn-primary:hover {
  background: #b92b2b;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(230, 90, 90, 0.3);
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.btn-cancel {
  background: #f1f5f9;
  color: #334155;
  border: 1px solid #e2e8f0;
}

.btn-cancel:hover {
  background: #e2e8f0;
  transform: translateY(-2px);
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

.btn-primary:hover .btn-light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
}

/* ================= DARK MODE ================= */
:deep(body.dark-mode) .page-wrap {
  background: rgba(0, 0, 0, 0.72);
}

:deep(body.dark-mode) .payment-modal {
  background: transparent;
  border: none;
  box-shadow: none;
}

:deep(body.dark-mode) .modal-header,
:deep(body.dark-mode) .invoice-panel,
:deep(body.dark-mode) .payment-panel {
  background: #181818;
  border: 1px solid #2a2a2a;
  color: #e5e5e5;
  box-shadow: none;
}

:deep(body.dark-mode) .invoice-title,
:deep(body.dark-mode) .col-ten,
:deep(body.dark-mode) .summary-total,
:deep(body.dark-mode) .summary-line strong,
:deep(body.dark-mode) .summary-row strong,
:deep(body.dark-mode) .pay-input-box label {
  color: #fff;
}

:deep(body.dark-mode) .customer-row,
:deep(body.dark-mode) .table-head,
:deep(body.dark-mode) .summary-card,
:deep(body.dark-mode) .radio-item:hover,
:deep(body.dark-mode) .category-header,
:deep(body.dark-mode) .empty-mon {
  background: #1f1f1f;
  border-color: #2a2a2a;
}

:deep(body.dark-mode) .customer-name,
:deep(body.dark-mode) .col-sl,
:deep(body.dark-mode) .col-gia,
:deep(body.dark-mode) .category-header,
:deep(body.dark-mode) .col-tt {
  color: #e5e5e5;
}

:deep(body.dark-mode) .invoice-time,
:deep(body.dark-mode) .table-head,
:deep(body.dark-mode) .summary-line span:first-child,
:deep(body.dark-mode) .summary-row,
:deep(body.dark-mode) .qr-placeholder {
  color: #bdbdbd;
}

:deep(body.dark-mode) .mon-thumb-wrap-inline {
  border-color: #2a2a2a;
  background: #1f1f1f;
}

:deep(body.dark-mode) .mon-thumb-inline.placeholder {
  color: #64748b;
}

:deep(body.dark-mode) .mon-row {
  border-bottom-color: #2a2a2a;
}

:deep(body.dark-mode) .mon-row:hover {
  background: #202020;
}

:deep(body.dark-mode) .pay-input-box input {
  background: #1f1f1f;
  border-color: #333;
  color: #fff;
}

:deep(body.dark-mode) .pay-input-box input::placeholder {
  color: #8a8a8a;
}

:deep(body.dark-mode) .btn-close {
  background: #222;
  color: #bdbdbd;
}

:deep(body.dark-mode) .btn-close:hover {
  background: rgba(230, 90, 90, 0.2);
  color: #ffb0b0;
}

/* ===== PHÂN TRANG (PAGINATION PREMIUM) ===== */
.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 12px;
}
.pagination-premium {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  padding: 6px;
  border-radius: 10px;
  border: 1px solid rgba(241, 245, 249, 0.8);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02),
    0 10px 15px -3px rgba(15, 23, 42, 0.05);
  list-style: none;
  margin: 0;
}
.nav-action-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid transparent;
  border-radius: 12px;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  outline: none;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}
.nav-action-btn:hover:not(:disabled) {
  background: #f8fafc;
  color: #0f172a;
  border-color: #e2e8f0;
}
.nav-action-btn:active:not(:disabled) {
  transform: scale(0.96);
  background: #f1f5f9;
}
.nav-action-btn:disabled {
  opacity: 0.25;
  cursor: not-allowed;
  color: #94a3b8;
}
.page-indicator-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  height: 36px;
  padding: 0 16px;
  background: #f3efef;
  border-radius: 7px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  user-select: none;
}
.current-number {
  font-weight: 600;
  font-size: 0.875rem;
  color: #d73737;
}
.divider {
  font-size: 0.75rem;
  color: #d73737;
  margin: 0 2px;
}
.total-number {
  font-weight: 500;
  font-size: 0.875rem;
  color: #94a3b8;
}

:deep(body.dark-mode) .pagination-premium {
  background: rgba(30, 30, 30, 0.75);
  border-color: rgba(50, 50, 50, 0.8);
}
:deep(body.dark-mode) .nav-action-btn {
  color: #a3a3a3;
}
:deep(body.dark-mode) .nav-action-btn:hover:not(:disabled) {
  background: #2a2a2a;
  color: #e5e5e5;
  border-color: #444;
}
:deep(body.dark-mode) .page-indicator-badge {
  background: #2a2a2a;
}
:deep(body.dark-mode) .total-number {
  color: #8a8a8a;
}

:deep(body.dark-mode) .btn-show-qr {
  background: #222;
  border-color: #333;
  color: #e5e5e5;
}

:deep(body.dark-mode) .btn-show-qr:hover {
  background: #2a2a2a;
}

:deep(body.dark-mode) .qr-image {
  background: #fff;
}

.pay-method-group {
  margin-top: 14px;
}

.method-group-title {
  font-size: 13px;
  font-weight: 800;
  color: #64748b;
  margin-bottom: 10px;
}

.pay-methods {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.radio-card {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 42px;
  padding: 10px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 7px;
  background: #fff;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  transition: all 0.2s ease;
}

.radio-card:hover {
  border-color: #f0b4b4;
  background: #fffafa;
}

.radio-card.active {
  border-color: var(--color-primary);
  background: rgba(230, 90, 90, 0.08);
  color: var(--color-primary);
  box-shadow: 0 6px 14px rgba(230, 90, 90, 0.12);
}

.radio-card input[type="radio"] {
  accent-color: var(--color-primary);
  margin: 0;
}
.hint-money {
  margin-top: 8px;
  font-size: 13px;
  font-weight: 600;
  padding-left: 4px;
  animation: fadeIn 0.2s ease-in-out;
}

.hint-money strong {
  font-size: 15px;
  font-weight: 800;
}

.gia-goc-inline {
  text-decoration: line-through;
  color: #9ca3af;
  font-size: 12px;
  line-height: 1;
  margin-bottom: 2px;
}

.gia-giam-inline {
  color: var(--color-primary);
  font-weight: 800;
  line-height: 1;
}

/* Fix CSS cho Dark mode (nếu dự án có xài dark-mode) */
:deep(body.dark-mode) .gia-goc-inline {
  color: #6b7280;
}
:deep(body.dark-mode) .gia-giam-inline {
  color: #ffb0b0;
}

/* Các màu sắc cho từng trạng thái */
.hint-thieu {
  color: #dc2626; /* Màu đỏ */
}
.hint-thua {
  color: #059669; /* Màu xanh lá */
}
.hint-du {
  color: #64748b; /* Màu xám */
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Hỗ trợ giao diện tối (Dark Mode) */
:deep(body.dark-mode) .hint-thieu {
  color: #fca5a5;
}
:deep(body.dark-mode) .hint-thua {
  color: #34d399;
}
:deep(body.dark-mode) .hint-du {
  color: #94a3b8;
}

/* ================= RESPONSIVE ================= */
@media (max-width: 1100px) {
  .page-wrap {
    padding: 14px;
    align-items: flex-start;
  }

  .payment-modal {
    width: min(100%, 98vw);
    height: auto;
    min-height: calc(100vh - 28px);
    grid-template-columns: 1fr;
  }

  .invoice-panel {
    border-right: none;
    border-bottom: 1px solid #ececec;
  }
}

@media (max-width: 768px) {
  .page-wrap {
    padding: 8px;
  }

  .payment-modal {
    width: 100%;
    min-height: calc(100vh - 16px);
    border-radius: 7px;
  }

  .invoice-title {
    font-size: 16px;
  }

  .ban-info {
    font-size: 13px;
  }

  .customer-row {
    font-size: 12px;
    flex-direction: column;
    align-items: flex-start;
  }

  .table-head-grid,
  .mon-row {
    grid-template-columns: 28px 1.6fr 44px 80px 90px;
    font-size: 12px;
    gap: 6px;
  }

  .mon-row {
    padding: 10px 4px;
  }

  .mon-thumb-wrap-inline {
    width: 36px;
    height: 36px;
  }

  .summary-total strong {
    font-size: 20px;
  }

  .footer-actions {
    flex-direction: column;
  }

  .btn-temp,
  .btn-thanh-toan {
    width: 100%;
  }
}

/* ================= RATING MODAL ================= */
.rating-wrap {
  position: fixed;
  inset: 0;
  z-index: 10000;
  background: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.rating-modal {
  width: min(880px, 94vw);
  background: #ffffff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(15, 23, 42, 0.25);
  display: flex;
  flex-direction: column;
  animation: ratingPop 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  border: 1px solid rgba(226, 232, 240, 0.8);
}

@keyframes ratingPop {
  0% {
    transform: scale(0.9) translateY(15px);
    opacity: 0;
  }
  100% {
    transform: scale(1) translateY(0);
    opacity: 1;
  }
}

.rating-header {
  background: rgb(237, 61, 61);
  color: white;
  padding: 28px 24px;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.header-sparkles {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.sparkle-one,
.sparkle-two {
  position: absolute;
  font-size: 20px;
  opacity: 0.8;
  animation: floatSparkle 3s infinite ease-in-out;
}

.sparkle-one {
  top: 15px;
  left: 20%;
  animation-delay: 0s;
}

.sparkle-two {
  bottom: 15px;
  right: 20%;
  animation-delay: 1.5s;
}

@keyframes floatSparkle {
  0%,
  100% {
    transform: translateY(0) scale(1);
    opacity: 0.3;
  }
  50% {
    transform: translateY(-8px) scale(1.2);
    opacity: 0.9;
  }
}

.success-icon-badge {
  width: 56px;
  height: 56px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  animation: bounceBadge 1s infinite alternate ease-in-out;
}

.check-svg {
  width: 30px;
  height: 30px;
  color: #fff;
}

@keyframes bounceBadge {
  0% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(-4px);
  }
}

.rating-title {
  font-size: 24px;
  font-weight: 800;
  margin: 0 0 6px 0;
  letter-spacing: -0.5px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

.rating-subtitle {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
  font-weight: 400;
}

.rating-body {
  display: grid;
  grid-template-columns: 1fr 1.25fr;
  gap: 24px;
  padding: 24px;
  background: #f8fafc;
}

.rating-left,
.rating-right {
  display: flex;
  flex-direction: column;
}

/* ================= LEFT COLUMN (RECEIPT) ================= */
.rating-receipt-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-bottom: none;
  border-radius: 16px 16px 0 0;
  padding: 24px 20px 20px;
  position: relative;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02);
  flex: 1;
  display: flex;
  flex-direction: column;
}

.rating-receipt-card::after {
  content: "";
  position: absolute;
  bottom: -6px;
  left: 0;
  right: 0;
  height: 6px;
  background: linear-gradient(-45deg, #ffffff 4px, transparent 0),
    linear-gradient(45deg, #ffffff 4px, transparent 0);
  background-size: 8px 8px;
  background-repeat: repeat-x;
  z-index: 2;
}

.receipt-paid-stamp {
  position: absolute;
  top: 18px;
  right: -24px;
  transform: rotate(25deg);
  background: rgba(34, 197, 94, 0.08);
  color: #22c55e;
  border: 2px solid #22c55e;
  padding: 4px 24px;
  font-size: 11px;
  font-weight: 900;
  border-radius: 4px;
  letter-spacing: 1.5px;
  text-transform: uppercase;
  pointer-events: none;
  opacity: 0.85;
}

.receipt-header-brand {
  text-align: center;
  margin-bottom: 16px;
}
.brand-logo {
  width: 70px;
  height: 70px;
  object-fit: contain;
  display: inline-block;
  margin-bottom: 4px;
}

.receipt-header-brand h4 {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
}

.receipt-header-brand p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

.receipt-divider {
  border-bottom: 1px dashed #e2e8f0;
  margin: 14px 0;
}

.receipt-divider.dotted {
  border-bottom: 2px dotted #cbd5e1;
}

.receipt-info-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
}

.receipt-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13.5px;
}

.receipt-info-row .label {
  color: #64748b;
  font-weight: 500;
}

.receipt-info-row .value {
  color: #1e293b;
  font-weight: 600;
}

.receipt-info-row .value.font-highlight {
  color: var(--color-primary, #e65a5a);
  font-weight: 700;
}

.receipt-total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.total-label {
  font-size: 13px;
  font-weight: 800;
  color: #475569;
  letter-spacing: 0.5px;
}

.total-value {
  font-size: 20px;
  font-weight: 850;
  color: #ee4d2d;
}

/* ================= RIGHT COLUMN (FORM) ================= */
.rating-form-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02);
  flex: 1;
}

.form-section-title {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
  margin: 0;
}

.field-title {
  font-size: 13px;
  font-weight: 700;
  color: #475569;
  margin: 0 0 8px 0;
}

/* Shopee Star Component */
.stars-container-shopee {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  background: #fffaf7;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid #ffebe0;
}

.stars-row-shopee {
  display: flex;
  gap: 8px;
}

.star-btn-shopee {
  background: none;
  border: none;
  padding: 2px;
  cursor: pointer;
  color: #e2e8f0;
  transition: all 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.star-svg {
  width: 34px;
  height: 34px;
  transition: fill 0.2s, transform 0.2s;
  fill: currentColor;
}

.star-btn-shopee.active {
  color: #ffaa00;
  filter: drop-shadow(0 2px 4px rgba(255, 170, 0, 0.15));
}

.star-btn-shopee:hover {
  transform: scale(1.22) rotate(6deg);
}

.star-feedback-text {
  font-size: 13.5px;
  font-weight: 700;
  color: #ee4d2d;
  min-height: 20px;
  text-align: center;
}

/* Quick review tags */
.quick-tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-tag-badge {
  border: 1px solid #cbd5e1;
  background: #ffffff;
  color: #475569;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.quick-tag-badge:hover {
  border-color: #ffb8a8;
  color: #ee4d2d;
  background: #fff9f8;
}

.quick-tag-badge.active {
  border-color: #ee4d2d;
  background: #fff3f0;
  color: #ee4d2d;
  font-weight: 700;
  box-shadow: 0 2px 6px rgba(238, 77, 45, 0.08);
}

/* Review Textarea */
.textarea-relative {
  position: relative;
  width: 100%;
}

.review-textarea-wrapper textarea {
  width: 100%;
  border: 1.5px solid #cbd5e1;
  border-radius: 12px;
  padding: 10px 12px 28px;
  font-family: inherit;
  font-size: 13px;
  resize: none;
  background: #f8fafc;
  transition: all 0.2s ease;
  min-height: 72px;
  box-sizing: border-box;
}

.review-textarea-wrapper textarea:focus {
  outline: none;
  border-color: #ee4d2d;
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(238, 77, 45, 0.1);
}

.review-textarea-wrapper textarea::placeholder {
  color: #94a3b8;
}

.char-count {
  position: absolute;
  bottom: 8px;
  right: 12px;
  font-size: 11px;
  color: #94a3b8;
  font-weight: 500;
}

/* points toggle */
.points-toggle-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 10px 14px;
}

.points-toggle-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.points-icon {
  font-size: 18px;
}

.points-text-group {
  display: flex;
  flex-direction: column;
}

.points-title {
  font-size: 13px;
  font-weight: 700;
  color: #1e293b;
}

.points-sub {
  font-size: 11px;
  color: #64748b;
  margin-top: 1px;
}

/* Shopee Custom Switch */
.shopee-switch {
  position: relative;
  display: inline-block;
  width: 42px;
  height: 22px;
  flex-shrink: 0;
}

.shopee-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.shopee-slider {
  position: absolute;
  cursor: pointer;
  inset: 0;
  background-color: #cbd5e1;
  transition: 0.3s;
  border-radius: 22px;
}

.shopee-slider:before {
  position: absolute;
  content: "";
  height: 16px;
  width: 16px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.3s;
  border-radius: 50%;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
}

.shopee-switch input:checked + .shopee-slider {
  background-color: #22c55e;
}

.shopee-switch input:checked + .shopee-slider:before {
  transform: translateX(20px);
}

/* ================= FOOTER ================= */
.rating-footer-new {
  padding: 16px 24px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* ================= RESPONSIVE ================= */
@media (max-width: 768px) {
  .rating-body {
    grid-template-columns: 1fr;
    padding: 16px;
    gap: 16px;
  }
  .payment-body-split {
    flex-direction: column;
  }
  .invoice-panel,
  .payment-panel {
    flex: none;
    width: 100%;
  }
}

/* ================= DARK MODE SUPPORT ================= */
:deep(body.dark-mode) .rating-modal {
  background: #1e1e24;
  border-color: #2e2e38;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
}

:deep(body.dark-mode) .rating-body {
  background: #121216;
}

:deep(body.dark-mode) .rating-receipt-card {
  background: #1e1e24;
  border-color: #2e2e38;
}

:deep(body.dark-mode) .rating-receipt-card::after {
  background: linear-gradient(-45deg, #1e1e24 4px, transparent 0),
    linear-gradient(45deg, #1e1e24 4px, transparent 0);
  background-size: 8px 8px;
}

:deep(body.dark-mode) .receipt-header-brand h4 {
  color: #f1f5f9;
}

:deep(body.dark-mode) .receipt-header-brand p {
  color: #94a3b8;
}

:deep(body.dark-mode) .receipt-divider {
  border-bottom-color: #2e2e38;
}

:deep(body.dark-mode) .receipt-info-row .label {
  color: #94a3b8;
}

:deep(body.dark-mode) .receipt-info-row .value {
  color: #f1f5f9;
}

:deep(body.dark-mode) .rating-form-card {
  background: #1e1e24;
  border-color: #2e2e38;
}

:deep(body.dark-mode) .form-section-title {
  color: #f1f5f9;
}

:deep(body.dark-mode) .field-title {
  color: #cbd5e1;
}

:deep(body.dark-mode) .stars-container-shopee {
  background: #251b18;
  border-color: #442a22;
}

:deep(body.dark-mode) .star-btn-shopee {
  color: #334155;
}

:deep(body.dark-mode) .star-btn-shopee.active {
  color: #ffb700;
}

:deep(body.dark-mode) .quick-tag-badge {
  background: #121216;
  border-color: #334155;
  color: #cbd5e1;
}

:deep(body.dark-mode) .quick-tag-badge:hover {
  background: #2a1b18;
  border-color: #ff7337;
  color: #ff7337;
}

:deep(body.dark-mode) .quick-tag-badge.active {
  background: #2d1814;
  border-color: #ee4d2d;
  color: #ff7337;
}

:deep(body.dark-mode) .review-textarea-wrapper textarea {
  background: #121216;
  border-color: #2e2e38;
  color: #f1f5f9;
}

:deep(body.dark-mode) .review-textarea-wrapper textarea:focus {
  border-color: #ee4d2d;
  background: #1e1e24;
}

:deep(body.dark-mode) .points-toggle-wrapper {
  background: #121216;
  border-color: #2e2e38;
}

:deep(body.dark-mode) .points-title {
  color: #f1f5f9;
}

:deep(body.dark-mode) .points-sub {
  color: #94a3b8;
}

:deep(body.dark-mode) .shopee-slider {
  background-color: #334155;
}

:deep(body.dark-mode) .rating-footer-new {
  background: #18181c;
  border-top-color: #2e2e38;
}

:deep(body.dark-mode) .btn-bo-qua-new {
  background: #1e1e24;
  border-color: #334155;
  color: #cbd5e1;
}

:deep(body.dark-mode) .btn-bo-qua-new:hover {
  background: #2a2a35;
  color: #ffffff;
  border-color: #475569;
}
/* ERROR CSS */
.error-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #e63946;
  font-size: 13.5px;
  margin-top: 6px;
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
  border-color: #dc3545 !important;
  box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.1) !important;
}
.shake-enter-active {
  animation: shake 0.5s;
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

:deep(body.dark-mode) .pg-btn {
  background-color: #2d3748;
  border-color: #4a5568;
  color: #e2e8f0;
}

:deep(body.dark-mode) .pg-btn:hover:not(:disabled):not(.active) {
  background-color: #4a5568;
  border-color: #ff4d4f;
  color: #ff4d4f;
}

:deep(body.dark-mode) .pg-btn:disabled {
  background-color: #1a202c;
  border-color: #2d3748;
  color: #718096;
}

:deep(body.dark-mode) .pg-btn.active {
  background-color: #ff4d4f;
  color: #ffffff;
  border-color: #ff4d4f;
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

.box {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
  border: 1px solid #eee;
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

.alert-da-thanh-toan {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  background: #f0fdf4;
  border: 1px solid #86efac;
  border-radius: 10px;
  color: #15803d;
  font-weight: 600;
  font-size: 14px;
}

.thong-bao-link-vnpay {
  padding: 14px 16px;
  background: #eff6ff;
  border: 1px solid #93c5fd;
  border-radius: 10px;
  color: #1d4ed8;
  font-size: 13px;
  line-height: 1.6;
}

/* ////////////////////// VNPAY //////////////////// */
.modal-qr-overlay {
  position: fixed;
  inset: 0;
  z-index: 10000;
  background: rgba(15, 23, 42, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}
.modal-qr-container {
  background: #fff;
  width: 400px;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}
.modal-qr-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  font-weight: 700;
  border-bottom: 1px solid #f1f5f9;
}
.modal-qr-body {
  padding: 20px;
}
.qr-info-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 14px;
}
.qr-total strong {
  color: #d73737;
  font-size: 18px;
}
.qr-image-wrapper {
  display: flex;
  justify-content: center;
  margin: 16px 0;
}
.qr-image-large {
  width: 220px;
  height: 220px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 10px;
}
.qr-note {
  font-size: 12.5px;
  color: #94a3b8;
  text-align: center;
  line-height: 1.5;
}
.modal-qr-footer {
  display: flex;
  gap: 10px;
  padding: 16px 20px;
  background: #f8fafc;
  border-top: 1px solid #f1f5f9;
}
.modal-qr-footer .btn-cancel {
  flex: 0 0 auto;
  padding: 10px 20px;
}
.modal-qr-footer .btn-primary {
  flex: 1;
  padding: 10px 20px;
}
</style>
