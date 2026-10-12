<script setup>
import { ref, onMounted, computed, watch, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUtils } from "@/utils/helper";
import { connectWebSocket, disconnectWebSocket } from "@/utils/websocket";
import { chuyenSangBanAo } from "@/api/admin/banao/banAoApi";
import iconBack from "@/assets/icons/prev.png";
import { getThanhToanByHoaDon } from "@/api/admin/thanhtoan/thanhToanApi";
import { getAllPhuongThucThanhToan } from "@/api/admin/thanhtoan/thanhToanApi";
import { getChiTietHoaDonThanhToan } from "@/api/admin/thanhtoan/thanhToanApi";
import { thanhToanTienMat } from "@/api/admin/thanhtoan/thanhToanApi";
import { thanhToanChuyenKhoan } from "@/api/admin/thanhtoan/thanhToanApi";
import { taoLinkThanhToanOnline } from "@/api/admin/thanhtoan/thanhToanApi";
import { xacNhanThanhToanOnline } from "@/api/admin/thanhtoan/thanhToanApi";
import { inHoaDonThanhToan } from "@/api/admin/thanhtoan/thanhToanApi";
import { getQrChuyenKhoan } from "@/api/admin/thanhtoan/thanhToanApi";
import QRCode from "qrcode";
import ZaloPay from "@/assets/icons/ZaloPay.png";
import VNPAY from "@/assets/icons/VNPay.png";
import logo from "@/assets/icons/logodatn.png";
import { addDanhGia } from "@/api/admin/danhgia/danhGiaApi";
import { getAllThamSoDong } from "@/api/admin/thamsodong/thamSoDongApi";
import { getThamSoInt } from "@/api/admin/thamsodong/thamSoDongApi";
import { getMonDaChonTheoHoaDon } from "@/api/admin/checkin/checkinapi";
import { getDanhSachPhieuGiamGiaTheoKhachHang } from "@/api/admin/checkin/checkinapi";
import { getDanhSachPhieuGiamGiaHopLe } from "@/api/admin/checkin/checkinapi";
import { getDanhSachPhieuGiamGiaGoiY } from "@/api/admin/checkin/checkinapi";
import { getPhieuGiamGiaTotNhat } from "@/api/admin/checkin/checkinapi";
import { getAllKhachHang } from "@/api/admin/khachhang/khachhangApi";
import { timKiemKhachHang } from "@/api/admin/khachhang/khachhangApi";
import { getDetailPhieuGiamGia } from "@/api/admin/phieugiamgia/phieuGiamGiaApi";
const route = useRoute();
const router = useRouter();
const { notify, nprogress } = useUtils();

const idHoaDon = Number(route.params.idHoaDon);
const idBanAn = Number(route.query.idBanAn || 0);

const danhSachPhuongThuc = ref([]);
const danhSachThanhToan = ref([]);
const giamTrucTiepTam = ref(Number(route.query.giamTrucTiep || 0));
const lyDoGiamTrucTiepTam = ref(route.query.lyDoGiamTrucTiep || "");
const maGiamGiaTam = ref(route.query.maGiamGia || null);
const soTienDuocGiamTam = ref(Number(route.query.soTienDuocGiam || 0));
const paymentUrl = ref("");
const qrCodeUrl = ref("");
const dangTaoLinkThanhToan = ref(false);
const daInHoaDon = ref(false);
const hienModalQR = ref(false);
const payloadChuyenKhoan = ref(null);
const tienThuaChuyenKhoan = ref(0);
const laChuyenKhoanThuCong = ref(true);

const errorPhuongThuc = ref("");
const errorSoTien = ref("");

/* ================= VOUCHER / GIẢM GIÁ ================= */
const danhSachVoucherKhach = ref([]);
const danhSachVoucherHienTai = ref([]);
const danhSachVoucherGoiY = ref([]);
const showPopupVoucher = ref(false);
const tuKhoaVoucher = ref("");
const phieuTotNhatHienTai = ref(null);
const loadingVoucher = ref(false);
const voucherDangChon = ref(null);
const voucherDaApDungChiTiet = ref(null);
const voucherStatusMsg = ref("");
const voucherStatusType = ref("");
const loadingApDung = ref(false);
const phanTramGioiHan = ref(100);

const showPopupGiamGiaTrucTiep = ref(false);
const errorGiamGiaTrucTiep = ref("");
const kieuGiamGiaTrucTiep = ref("VNĐ");
const phanTramGiamGiaInput = ref(0);
const soTienGiamTrucTiepInput = ref(0);
const lyDoGiamTrucTiep = ref("");

const giamTrucTiepLocal = ref(0);
const lyDoGiamTrucTiepLocal = ref("");

const duDieuKienApDung = (voucher) => {
  if (!voucher) return false;
  return tongTienMon.value >= Number(voucher.dieuKienSuDung || 0);
};

const tinhTienGiamTheoVoucher = (voucher) => {
  if (!voucher || !duDieuKienApDung(voucher)) return 0;
  let soTien = 0;
  if (Number(voucher.phuongThucGiamGia) === 1) {
    soTien = tongTienMon.value * (Number(voucher.giaTriGiam || 0) / 100);
    if (voucher.giamToiDa != null && soTien > Number(voucher.giamToiDa)) {
      soTien = Number(voucher.giamToiDa);
    }
  } else {
    soTien = Math.min(Number(voucher.giaTriGiam || 0), tongTienMon.value);
  }
  return Math.round(soTien);
};

const gioiHanGiamGiaTrucTiep = computed(() => {
  return Math.floor(tongTienMon.value * (phanTramGioiHan.value / 100));
});

const soTienGiamTuVoucher = computed(() => {
  const voucher = voucherDaApDungChiTiet.value;
  if (!voucher?.maGiamGia) return 0;
  let soTien = 0;
  if (Number(voucher.phuongThucGiamGia) === 1) {
    soTien = tongTienMon.value * (Number(voucher.giaTriGiam || 0) / 100);
    if (voucher.giamToiDa != null && soTien > Number(voucher.giamToiDa)) {
      soTien = Number(voucher.giamToiDa);
    }
  } else {
    soTien = Math.min(Number(voucher.giaTriGiam || 0), tongTienMon.value);
  }
  return soTien;
});

const soTienGiamTamTinh = computed(() =>
  tinhTienGiamTheoVoucher(phieuTotNhatHienTai.value),
);

const voucherDangChonMa = computed(
  () => voucherDangChon.value?.maGiamGia || "",
);

const form = ref({
  idHoaDon: idHoaDon,
  idPhuongThucThanhToan: null,
  soTien: 0,
  ghiChu: "",
});

const hoaDon = ref({
  idHoaDon: idHoaDon,
  trangThaiHoaDon: 0,
  idBanAn: idBanAn,

  maHoaDon: "",
  tenBanAn: "",
  tenKhuVuc: "",
  idKhachHang: null,
  maKhachHang: "",
  tenKhachHang: "",
  soDienThoai: "",
  thoiGianTao: "",

  tienCoc: 0,
  tongTien: 0,
  tongTienMon: 0,
  soTienDuocGiam: 0,
  giamTrucTiep: 0,
  diemTichLuySuDung: 0,
  phiThuKhac: 0,
  maGiamGia: null,
  phieuGiamGia: null,

  dsMon: [],
});

//////////////////////////// CHỌN KHÁCH QUEN ////////////////////////
const khachHangDaChon = ref(null); // object khách hàng chọn thay thế vãng lai

const hienModalChonKhachHang = ref(false);
const dangTaiKhachHang = ref(false);
const dsKhachHangGoc = ref([]);
const dsKhachHangHienThi = ref([]);
const tuKhoaTimKhachHang = ref("");
const trangKhachHang = ref(1);
const pageSizeKhachHang = ref(5);

const tongSoTrangKhachHang = computed(
  () =>
    Math.ceil(dsKhachHangHienThi.value.length / pageSizeKhachHang.value) || 1,
);

const pageListKhachHang = computed(() => {
  const total = tongSoTrangKhachHang.value;
  if (total <= 5) return Array.from({ length: total }, (_, i) => i + 1);
  return [1, 2, 3, "…", total];
});

const dsKhachHangPhanTrang = computed(() => {
  const start = (trangKhachHang.value - 1) * pageSizeKhachHang.value;
  return dsKhachHangHienThi.value.slice(start, start + pageSizeKhachHang.value);
});

const taiDanhSachKhachHang = async () => {
  dangTaiKhachHang.value = true;
  try {
    const res = await getAllKhachHang();
    dsKhachHangGoc.value = Array.isArray(res.data) ? res.data : [];
    dsKhachHangHienThi.value = dsKhachHangGoc.value;
  } catch (e) {
    notify.error("Không tải được danh sách khách hàng");
  } finally {
    dangTaiKhachHang.value = false;
  }
};

let timeoutTimKiemKh = null;
watch(tuKhoaTimKhachHang, (val) => {
  clearTimeout(timeoutTimKiemKh);
  timeoutTimKiemKh = setTimeout(async () => {
    trangKhachHang.value = 1;
    const kw = val.trim();
    if (!kw) {
      dsKhachHangHienThi.value = dsKhachHangGoc.value;
      return;
    }
    dangTaiKhachHang.value = true;
    try {
      const res = await timKiemKhachHang(kw);
      console.log("KQ tìm kiếm khách hàng:", res.data); // xem tên field thực tế

      dsKhachHangHienThi.value = Array.isArray(res.data) ? res.data : [];
    } catch (e) {
      notify.error("Tìm kiếm khách hàng thất bại");
    } finally {
      dangTaiKhachHang.value = false;
    }
  }, 350);
});

const moModalChonKhachHang = async () => {
  hienModalChonKhachHang.value = true;
  tuKhoaTimKhachHang.value = "";
  trangKhachHang.value = 1;
  await taiDanhSachKhachHang();
};

const dongModalChonKhachHang = () => {
  hienModalChonKhachHang.value = false;
};

const chonKhachHangNay = (kh) => {
  khachHangDaChon.value = kh;
  hienModalChonKhachHang.value = false;
};

const boChonKhachHang = () => {
  khachHangDaChon.value = null;
};

//////////////////////////// GHI NỢ - VÃNG LAI ////////////////////////
const khachMoiForm = ref({
  hoTen: "",
  soDienThoai: "",
  email: "",
  ngaySinh: "",
  gioiTinh: "Nam",
  anhFile: null,
  anhPreview: "",
});

const laKhachVangLai = computed(() => {
  if (khachHangDaChon.value) return false;

  if (
    hoaDon.value.tenKhachHang === "Khách vãng lai" &&
    !hoaDon.value.soDienThoai
  ) {
    return true;
  }
  if (hoaDon.value.idKhachHang && hoaDon.value.soDienThoai) return false;
  return true;
});

const chonAnhKhachHang = (e) => {
  const file = e.target.files?.[0];
  if (!file) return;
  khachMoiForm.value.anhFile = file;
  khachMoiForm.value.anhPreview = URL.createObjectURL(file);
};

const xoaAnhKhachHang = () => {
  khachMoiForm.value.anhFile = null;
  khachMoiForm.value.anhPreview = "";
};

/* ================= GHI NỢ ================= */
const hienModalGhiNo = ref(false);
const dangXuLyGhiNo = ref(false);
const ghiNoForm = ref({ lyDo: "" });
const ghiNoErrors = ref({
  lyDo: "",
  hoTen: "",
  soDienThoai: "",
  email: "",
  ngaySinh: "",
  anhFile: "",
});

const moModalGhiNo = () => {
  ghiNoForm.value.lyDo = "";
  khachHangDaChon.value = null;
  khachMoiForm.value = {
    hoTen: "",
    soDienThoai: "",
    ngaySinh: "",
    email: "",
    gioiTinh: "Nam",
    anhFile: null,
    anhPreview: "",
  };
  ghiNoErrors.value = {
    lyDo: "",
    hoTen: "",
    soDienThoai: "",
    email: "",
    ngaySinh: "",
    anhFile: "",
  };
  hienModalGhiNo.value = true;
};

const dongModalGhiNo = () => {
  hienModalGhiNo.value = false;
};

const xacNhanGhiNo = async () => {
  let hasError = false;
  ghiNoErrors.value = {
    lyDo: "",
    hoTen: "",
    soDienThoai: "",
    email: "",
    ngaySinh: "",
    anhFile: "",
  };

  if (!ghiNoForm.value.lyDo?.trim()) {
    ghiNoErrors.value.lyDo = "Vui lòng nhập lý do ghi nợ";
    hasError = true;
  } else if (ghiNoForm.value.lyDo.trim().length > 255) {
    ghiNoErrors.value.lyDo = "Ghi chú không được vượt quá 255 ký tự";
    hasError = true;
  }

  if (laKhachVangLai.value) {
    if (!khachMoiForm.value.hoTen?.trim()) {
      ghiNoErrors.value.hoTen = "Tên khách hàng không được để trống";
      hasError = true;
    } else if (
      khachMoiForm.value.hoTen.trim().length < 2 ||
      khachMoiForm.value.hoTen.trim().length > 100
    ) {
      ghiNoErrors.value.hoTen = "Tên khách hàng phải từ 2 đến 100 ký tự";
      hasError = true;
    } else if (!/^[\p{L} ]+$/u.test(khachMoiForm.value.hoTen.trim())) {
      ghiNoErrors.value.hoTen =
        "Tên khách hàng chỉ được chứa chữ cái và khoảng trắng";
      hasError = true;
    }

    if (!khachMoiForm.value.soDienThoai?.trim()) {
      ghiNoErrors.value.soDienThoai = "Số điện thoại không được để trống";
      hasError = true;
    } else if (
      !/^(0[35789])[0-9]{8}$/.test(khachMoiForm.value.soDienThoai.trim())
    ) {
      ghiNoErrors.value.soDienThoai = "Số điện thoại không hợp lệ";
      hasError = true;
    }

    if (!khachMoiForm.value.email?.trim()) {
      ghiNoErrors.value.email = "Email không được để trống";
      hasError = true;
    } else if (khachMoiForm.value.email.trim().length > 100) {
      ghiNoErrors.value.email = "Email không được vượt quá 100 ký tự";
      hasError = true;
    } else if (
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(khachMoiForm.value.email.trim())
    ) {
      ghiNoErrors.value.email = "Email không hợp lệ";
      hasError = true;
    }

    if (!khachMoiForm.value.ngaySinh) {
      ghiNoErrors.value.ngaySinh = "Ngày sinh không được để trống";
      hasError = true;
    } else {
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const birthDate = new Date(khachMoiForm.value.ngaySinh);
      if (birthDate >= today) {
        ghiNoErrors.value.ngaySinh = "Ngày sinh phải là ngày trong quá khứ";
        hasError = true;
      } else {
        let age = today.getFullYear() - birthDate.getFullYear();
        const m = today.getMonth() - birthDate.getMonth();
        if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
          age--;
        }
        if (age < 13 || age > 85) {
          ghiNoErrors.value.ngaySinh = "Tuổi khách hàng phải từ 13 đến 85";
          hasError = true;
        }
      }
    }

    if (!khachMoiForm.value.anhFile) {
      ghiNoErrors.value.anhFile = "Vui lòng chọn ảnh khách hàng";
      hasError = true;
    }
  }

  if (hasError) return;

  if (
    !(await notify.confirm(
      "Xác nhận ghi nợ hóa đơn này? Bàn sẽ được giải phóng.",
    ))
  )
    return;

  dangXuLyGhiNo.value = true;
  nprogress.start();
  try {
    const payload = {
      idHoaDon,
      ghiChu: ghiNoForm.value.lyDo.trim(),
      diemTichLuySuDung: diemTichLuySuDung.value,
      giamTrucTiep: giamTrucTiepLocal.value || 0,
      lyDoGiamTrucTiep: lyDoGiamTrucTiepLocal.value || "",
      maGiamGia: maGiamGiaDangApDung.value || null,
      idPhieuGiamGia: idPhieuGiamGiaDangApDung.value || null,
    };

    if (khachHangDaChon.value) {
      payload.idKhachHangChon = khachHangDaChon.value.id;
    } else if (laKhachVangLai.value) {
      payload.tenKhachHang = khachMoiForm.value.hoTen.trim();
      payload.soDienThoai = khachMoiForm.value.soDienThoai.trim();
      payload.email = khachMoiForm.value.email?.trim() || null;
      payload.ngaySinh = khachMoiForm.value.ngaySinh || null;
      payload.gioiTinh = khachMoiForm.value.gioiTinh === "Nam";
    }

    // ===== DEBUG LOG =====
    console.log("[GHI_NO] tongTienMon:", tongTienMon.value);
    console.log("[GHI_NO] soTienGiam (voucher):", soTienGiam.value);
    console.log("[GHI_NO] giamTrucTiep:", giamTrucTiep.value);
    console.log("[GHI_NO] tienSauGiam:", tienSauGiam.value);
    console.log("[GHI_NO] diemTichLuySuDung:", diemTichLuySuDung.value);
    console.log("[GHI_NO] tienConLai (UI hiển thị):", tienConLai.value);
    console.log(
      "[GHI_NO] payload gửi lên BE:",
      JSON.parse(JSON.stringify(payload)),
    );
    // ======================

    await chuyenSangBanAo(
      payload,
      laKhachVangLai.value ? khachMoiForm.value.anhFile : null,
    );

    notify.success("Đã ghi nợ hóa đơn thành công");
    hienModalGhiNo.value = false;
    dongTrang();
  } catch (e) {
    notify.error(e?.response?.data?.message || "Ghi nợ thất bại");
  } finally {
    dangXuLyGhiNo.value = false;
    nprogress.done();
  }
};
////////////////////////////////////////////////////////////
const hienModalDanhGia = ref(false);
const danhGiaForm = ref({
  soSao: 5,
  tichDiem: true,
  noiDung: "",
});

////////////////// LOAD THAM SỐ ĐỘNG /////////////
const danhSachThamSo = ref([]);

const loadThamSo = async () => {
  try {
    const res = await getAllThamSoDong();
    danhSachThamSo.value = res.data;
  } catch (error) {
    console.log("LỖI THAM SỐ", error);
  }
};

const getThamSo = (key) => {
  return danhSachThamSo.value.find((x) => x.maThamSo === key)?.giaTri;
};

const tagGoiY = ref([
  "Món ăn rất ngon",
  "Lên món nhanh",
  "Phục vụ tận tâm",
  "Không gian sạch đẹp",
  "Nước lẩu đậm đà",
  "Giá cả hợp lý",
]);
const tagsDaChon = ref([]);

function toggleTag(tag) {
  const index = tagsDaChon.value.indexOf(tag);
  if (index > -1) {
    tagsDaChon.value.splice(index, 1);
  } else {
    tagsDaChon.value.push(tag);
  }
  danhGiaForm.value.noiDung = tagsDaChon.value.join(", ");
}

const nhanSao = computed(() => {
  const sao = Number(danhGiaForm.value.soSao);
  switch (sao) {
    case 1:
      return { text: "Tệ quá", icon: "bi bi-emoji-frown-fill" };
    case 2:
      return { text: "Chưa hài lòng", icon: "bi bi-emoji-frown" };
    case 3:
      return { text: "Bình thường", icon: "bi bi-emoji-neutral-fill" };
    case 4:
      return { text: "Rất tốt", icon: "bi bi-emoji-smile-fill" };
    case 5:
      return { text: "Tuyệt vời", icon: "bi bi-emoji-laughing-fill" };
    default:
      return { text: "", icon: "" };
  }
});

async function hoanTatDanhGia() {
  if (!(await notify.confirm("Xác nhận đánh giá?"))) return;
  try {
    await addDanhGia({
      idHoaDon: idHoaDon,
      soSao: danhGiaForm.value.soSao,
      noiDung: danhGiaForm.value.noiDung || " ",
      tichDiem: danhGiaForm.value.tichDiem,
    });
    notify.success("Cảm ơn bạn đã đánh giá!");
  } catch (e) {
    console.error("Lỗi gửi đánh giá:", e);
    notify.error("Gửi đánh giá thất bại");
  } finally {
    router.push("/admin/check-in");
  }
}

function boQuaDanhGia() {
  router.push("/admin/check-in");
}

/* ================= LOAD DATA ================= */

async function loadHoaDonChiTiet() {
  try {
    const [res, resMon] = await Promise.all([
      getChiTietHoaDonThanhToan(idHoaDon),
      getMonDaChonTheoHoaDon(idHoaDon).catch(() => ({ data: [] })),
    ]);
    const data = res?.data || {};
    const dsMenu = Array.isArray(resMon?.data) ? resMon.data : [];
    const dsMonRaw = Array.isArray(data.dsMon) ? data.dsMon : [];

    const oldPhieu = hoaDon.value.phieuGiamGia;
    const newPhieu = data.phieuGiamGia;

    let isVoucherExpired = false;
    let tenPhieu = "";

    if (
      data.warningMessage &&
      data.warningMessage.includes("không còn hợp lệ")
    ) {
      isVoucherExpired = true;
      const match = data.warningMessage.match(/Phiếu giảm giá \[(.*?)\]/);
      tenPhieu = match ? match[1] : "";
    } else if (
      !isFirstLoad &&
      oldPhieu &&
      (!newPhieu || Number(oldPhieu.id) !== Number(newPhieu.id))
    ) {
      isVoucherExpired = true;
      tenPhieu = oldPhieu.tenGiamGia || oldPhieu.maGiamGia || "này";
    }

    if (isFirstLoad) {
      if (isVoucherExpired) {
        await notify.warning(
          `Phiếu ${tenPhieu} đã hết hạn, vui lòng kiểm tra lại tổng tiền.`,
        );
        voucherStatusMsg.value = `Phiếu giảm giá [${tenPhieu}] đã hết hạn hoặc không còn hợp lệ!`;
        voucherStatusType.value = "error";
      } else {
        const applied = voucherDaApDungChiTiet.value || data.phieuGiamGia;
        if (applied?.maGiamGia) {
          voucherStatusMsg.value = `Phiếu giảm giá [${applied.maGiamGia}] đang được áp dụng hợp lệ.`;
          voucherStatusType.value = "success";
        } else {
          voucherStatusMsg.value = "";
          voucherStatusType.value = "";
        }
      }
    } else {
      if (isVoucherExpired) {
        await notify.warning(
          `Phiếu ${tenPhieu} đã hết hạn, vui lòng kiểm tra lại tổng tiền.`,
        );
        voucherStatusMsg.value = `Phiếu giảm giá [${tenPhieu}] đã hết hạn hoặc không còn hợp lệ!`;
        voucherStatusType.value = "error";
      }
    }
    if (isFirstLoad) {
      diemTichLuySuDungChon.value = Number(
        data.diemTichLuySuDung || route.query.diemTichLuySuDung || 0,
      );
    }
    const dsMon = dsMonRaw.map((m) => {
      const found = dsMenu.find((x) => {
        if (m.id && Number(x.id) === Number(m.id)) return true;
        if (
          m.idHoaDonChiTiet &&
          Number(x.idHoaDonChiTiet) === Number(m.idHoaDonChiTiet)
        )
          return true;
        if (m.maSanPham && x.maSanPham === m.maSanPham) return true;
        if (m.ma && x.maSanPham === m.ma) return true;
        if (m.tenSanPham && x.tenSanPham === m.tenSanPham) return true;
        if (m.tenMon && (x.tenMon === m.tenMon || x.tenSanPham === m.tenMon))
          return true;
        return false;
      });

      return {
        ...m,
        hinhAnh:
          m.hinhAnh ||
          m.urlAnh ||
          m.anh ||
          found?.hinhAnh ||
          found?.urlAnh ||
          found?.anh,
        tenDanhMuc: found?.tenDanhMuc || found?.loai || m.tenDanhMuc || "Khác",
      };
    });

    hoaDon.value = {
      ...hoaDon.value,
      idHoaDon: Number(data.idHoaDon || idHoaDon),
      idBanAn: Number(idBanAn || 0),

      maHoaDon: data.maHoaDon || "",
      tenBanAn: data.tenBanAn || "",
      tenKhuVuc: data.tenKhuVuc || "",
      idKhachHang: data.idKhachHang || null,
      tenKhachHang: data.tenKhachHang || "Khách vãng lai",
      soDienThoai: data.soDienThoai || "",
      thoiGianTao: data.thoiGianTao || "",
      tienCoc: Number(data.tienCoc || 0),
      diemTichLuy: Number(data.diemTichLuy || 0),
      tongTien: Number(data.tongTien || 0),
      tongTienMon: Number(data.tongTienMon || 0),
      soTienDuocGiam:
        Number(data.soTienDuocGiam || 0) ||
        (isFirstLoad ? soTienDuocGiamTam.value : 0),
      giamTrucTiep:
        Number(data.giamTrucTiep || 0) ||
        (isFirstLoad ? giamTrucTiepTam.value : 0),
      lyDoGiamTrucTiep:
        data.lyDoGiamTrucTiep || (isFirstLoad ? lyDoGiamTrucTiepTam.value : ""),
      phiThuKhac: Number(data.phiThuKhac || 0),
      trangThaiHoaDon: Number(
        data.trangThaiHoaDon ??
          data.trangThai ??
          hoaDon.value.trangThaiHoaDon ??
          0,
      ),

      maGiamGia: data.maGiamGia || (isFirstLoad ? maGiamGiaTam.value : null),
      phieuGiamGia: data.phieuGiamGia || null,

      dsMon,
    };

    if (data.warningMessage) {
      await notify.warning(data.warningMessage);
    }
  } catch (e) {
    console.error("Lỗi load hóa đơn:", e);
    notify.error("Không tải được chi tiết hóa đơn");
  }
}

const normalizeVoucher = (raw) => {
  if (!raw) return null;

  const voucher = raw.data ? raw.data : raw;

  return {
    id:
      voucher.id ??
      voucher.idPhieuGiamGia ??
      voucher.maGiamGia ??
      Math.random().toString(36).slice(2),
    maGiamGia: voucher.maGiamGia || "",
    tenGiamGia: voucher.tenGiamGia || voucher.tenPhieuGiamGia || "",
    dieuKienSuDung: Number(
      voucher.dieuKienSuDung ??
        voucher.donToiThieu ??
        voucher.donHangToiThieu ??
        0,
    ),
    giaTriGiam: Number(voucher.giaTriGiam || 0),
    phuongThucGiamGia: Number(voucher.phuongThucGiamGia || 0),
    giamToiDa: voucher.giamToiDa != null ? Number(voucher.giamToiDa) : null,
    ngayBatDau: voucher.ngayBatDau || voucher.thoiGianBatDau || null,
    ngayKetThuc: voucher.ngayKetThuc || voucher.thoiGianKetThuc || null,
    ...voucher,
  };
};

const uniqueVoucherList = (list = []) => {
  const map = new Map();

  list.forEach((item) => {
    const v = normalizeVoucher(item);
    if (!v?.maGiamGia) return;
    if (!map.has(v.maGiamGia)) map.set(v.maGiamGia, v);
  });

  return Array.from(map.values());
};

const hienThiGiaTriGiam = (voucher) => {
  if (!voucher) return "--";
  if (Number(voucher.phuongThucGiamGia) === 1) {
    return `${voucher.giaTriGiam || 0}%`;
  }
  return formatTien(voucher.giaTriGiam || 0);
};

const isVoucherDangChon = (voucher) => {
  return voucherDangChonMa.value === voucher.maGiamGia;
};

const isVoucherDangApDung = (voucher) => {
  return maGiamGiaDangApDung.value === voucher.maGiamGia;
};
const chonVoucher = (voucher) => {
  if (!duDieuKienApDung(voucher)) {
    notify.error(
      `Đơn hàng chưa đủ điều kiện tối thiểu ${formatTien(
        voucher.dieuKienSuDung,
      )} để dùng mã này`,
    );
    return;
  }
  voucherDangChon.value = normalizeVoucher(voucher);
};

const xuLyApDungUuDai = async () => {
  if (!voucherDangChon.value?.maGiamGia) {
    notify.error("Vui lòng chọn phiếu giảm giá");
    return;
  }
  if (!duDieuKienApDung(voucherDangChon.value)) {
    notify.error("Đơn hàng chưa đủ điều kiện tối thiểu để dùng mã này");
    return;
  }
  voucherDaApDungChiTiet.value = { ...voucherDangChon.value };
  voucherStatusMsg.value = `Phiếu giảm giá [${voucherDangChon.value.maGiamGia}] đang được áp dụng hợp lệ.`;
  voucherStatusType.value = "success";
  notify.success("Đã chọn phiếu giảm giá");
  showPopupVoucher.value = false;
};

const loadPhieuTotNhatTruoc = async () => {
  if (!idHoaDon) {
    phieuTotNhatHienTai.value = null;
    return;
  }

  try {
    const res = await getPhieuGiamGiaTotNhat(idHoaDon);
    const voucher = normalizeVoucher(res?.data?.data || res?.data);
    phieuTotNhatHienTai.value = voucher?.maGiamGia ? voucher : null;
  } catch (error) {
    phieuTotNhatHienTai.value = null;
  }
};

const loadDanhSachVoucher = async () => {
  console.log("idHoaDon:", idHoaDon);
  try {
    if (!idHoaDon) return;
    loadingVoucher.value = true;

    const khachRes = await getDanhSachPhieuGiamGiaTheoKhachHang(idHoaDon);
    const hienTaiRes = await getDanhSachPhieuGiamGiaHopLe(idHoaDon);
    const goiYRes = await getDanhSachPhieuGiamGiaGoiY(idHoaDon);

    const duLieuKhach =
      khachRes?.data?.data || khachRes?.data?.content || khachRes?.data || [];
    const duLieuHienTai =
      hienTaiRes?.data?.data ||
      hienTaiRes?.data?.content ||
      hienTaiRes?.data ||
      [];
    const duLieuGoiY =
      goiYRes?.data?.data || goiYRes?.data?.content || goiYRes?.data || [];

    danhSachVoucherKhach.value = uniqueVoucherList(
      Array.isArray(duLieuKhach) ? duLieuKhach : [],
    );
    danhSachVoucherHienTai.value = uniqueVoucherList(
      Array.isArray(duLieuHienTai) ? duLieuHienTai : [],
    );
    danhSachVoucherGoiY.value = uniqueVoucherList(
      Array.isArray(duLieuGoiY) ? duLieuGoiY : [],
    );
  } catch (error) {
    console.error("Lỗi load phiếu giảm giá:", error);
    notify.error(
      error?.response?.data?.message ||
        "Không tải được danh sách phiếu giảm giá",
    );
  } finally {
    loadingVoucher.value = false;
  }
};

const moPopupVoucher = async () => {
  await loadDanhSachVoucher();
  showPopupVoucher.value = true;
};

const dongPopupVoucher = () => {
  showPopupVoucher.value = false;
};

const moPopupGiamGiaTrucTiep = () => {
  soTienGiamTrucTiepInput.value = giamTrucTiepLocal.value || 0;
  errorGiamGiaTrucTiep.value = "";
  lyDoGiamTrucTiep.value = lyDoGiamTrucTiepLocal.value || "";
  showPopupGiamGiaTrucTiep.value = true;
};

const xacNhanGiamGiaTrucTiep = async () => {
  errorGiamGiaTrucTiep.value = "";
  let soTienSubmit = 0;

  if (kieuGiamGiaTrucTiep.value === "VNĐ") {
    soTienSubmit = soTienGiamTrucTiepInput.value;
    if (soTienSubmit <= 0) {
      errorGiamGiaTrucTiep.value = "Số tiền giảm phải lớn hơn 0";
      return;
    }
  } else {
    if (phanTramGiamGiaInput.value <= 0) {
      errorGiamGiaTrucTiep.value = "Phần trăm giảm phải lớn hơn 0";
      return;
    }
    if (phanTramGiamGiaInput.value > phanTramGioiHan.value) {
      errorGiamGiaTrucTiep.value =
        "Phần trăm giảm không được vượt quá " + phanTramGioiHan.value + "%";
      return;
    }
    soTienSubmit = Math.round(
      tongTienMon.value * (phanTramGiamGiaInput.value / 100),
    );
  }

  if (soTienSubmit > gioiHanGiamGiaTrucTiep.value) {
    errorGiamGiaTrucTiep.value =
      "Số tiền giảm không được vượt quá " +
      phanTramGioiHan.value +
      "% tổng tiền món (" +
      formatTien(gioiHanGiamGiaTrucTiep.value) +
      ")";
    return;
  }

  if (!lyDoGiamTrucTiep.value.trim()) {
    errorGiamGiaTrucTiep.value = "Vui lòng nhập lý do giảm giá trực tiếp";
    return;
  }

  giamTrucTiepLocal.value = soTienSubmit;
  lyDoGiamTrucTiepLocal.value = lyDoGiamTrucTiep.value.trim();

  notify.success("Đã ghi nhận giảm giá trực tiếp");
  showPopupGiamGiaTrucTiep.value = false;
};

const huyGiamGiaTrucTiep = () => {
  giamTrucTiepLocal.value = 0;
  lyDoGiamTrucTiepLocal.value = "";
  lyDoGiamTrucTiep.value = "";
  soTienGiamTrucTiepInput.value = 0;
  phanTramGiamGiaInput.value = 0;
  notify.success("Đã hủy giảm giá trực tiếp");
  showPopupGiamGiaTrucTiep.value = false;
};

const danhSachVoucherDangHienThi = computed(() => {
  let ds = uniqueVoucherList([
    ...danhSachVoucherKhach.value,
    ...danhSachVoucherHienTai.value,
    ...danhSachVoucherGoiY.value,
  ]);
  ds = ds.filter(
    (v) =>
      v.soLuong == null ||
      v.soLuong > 0 ||
      (v.maGiamGia && v.maGiamGia === maGiamGiaDangApDung.value),
  );
  const keyword = tuKhoaVoucher.value?.trim().toLowerCase();
  if (!keyword) return ds;
  return ds.filter((voucher) => {
    const ma = String(voucher.maGiamGia || "").toLowerCase();
    const ten = String(voucher.tenGiamGia || "").toLowerCase();
    return ma.includes(keyword) || ten.includes(keyword);
  });
});

const danhSachVoucherGoiYHienThi = computed(() => {
  const ds = [...danhSachVoucherGoiY.value].sort((a, b) => {
    const diffA = (a.dieuKienSuDung || 0) - tongTienMon.value;
    const diffB = (b.dieuKienSuDung || 0) - tongTienMon.value;
    return diffA - diffB;
  });
  return ds.slice(0, 2);
});

const loadData = async () => {
  nprogress.start();

  try {
    const [ptRes, ttRes] = await Promise.all([
      getAllPhuongThucThanhToan(),
      getThanhToanByHoaDon(idHoaDon),
    ]);

    danhSachPhuongThuc.value = Array.isArray(ptRes.data) ? ptRes.data : [];
    danhSachThanhToan.value = Array.isArray(ttRes.data) ? ttRes.data : [];

    await loadHoaDonChiTiet();
    try {
      await Promise.all([loadDanhSachVoucher(), loadPhieuTotNhatTruoc()]);
    } catch (err) {
      console.error("Lỗi tải voucher/phiếu gợi ý:", err);
    }

    const tienMat = danhSachPhuongThuc.value.find((p) => {
      const ten = String(p.tenPhuongThuc || "")
        .trim()
        .toLowerCase();

      return ten === "tiền mặt" || ten === "tien mat";
    });

    if (tienMat && !form.value.idPhuongThucThanhToan) {
      form.value.idPhuongThucThanhToan = tienMat.id;
    }

    if (!form.value.soTien || Number(form.value.soTien) <= 0) {
      form.value.soTien = tienConLai.value > 0 ? toMoney(tienConLai.value) : 0;
    }
  } catch (e) {
    console.error("Lỗi load data:", e);
    notify.error("Không tải được dữ liệu thanh toán");
  } finally {
    nprogress.done();
    isFirstLoad = false;
  }
};

let isFirstLoad = true;

const handleWsMessage = async (msg) => {
  if (!msg) return;

  if (msg.type === "TOAST_HOA_DON") {
    if (msg.idHoaDon && Number(msg.idHoaDon) !== idHoaDon) return;
    if (msg.toastType === "success") {
      notify.success(msg.message);
    } else if (msg.toastType === "warning") {
      notify.warning(msg.message);
    }
    return;
  }

  if (msg.type === "CHI_TIET_HOA_DON") {
    if (msg.id && Number(msg.id) !== idHoaDon) return;
    if (msg.idHoaDon && Number(msg.idHoaDon) !== idHoaDon) return;

    if (isFirstLoad) return;

    if (hoaDon.value.tongTien !== undefined && msg.tongTien !== undefined) {
      if (
        Number(hoaDon.value.tongTien) === Number(msg.tongTien) &&
        Number(hoaDon.value.soTienDuocGiam) === Number(msg.soTienDuocGiam) &&
        Number(hoaDon.value.trangThaiHoaDon) === Number(msg.trangThai)
      ) {
        return;
      }
    }

    loadData();
  } else if (msg.type === "MON_DA_CHON_UPDATE") {
    loadData();
  } else if (msg.type === "PHIEU_GIAM_GIA_UPDATE") {
    console.log(">>> [WS CheckIn] Nhận sự kiện PHIEU_GIAM_GIA_UPDATE:", msg);
    console.log(">>> [WS CheckIn] Trạng thái isFirstLoad:", isFirstLoad);
    if (isFirstLoad) {
      console.log(">>> [WS CheckIn] Bỏ qua vì isFirstLoad = true");
      return;
    }

    console.log(
      ">>> [WS CheckIn] Đang gọi loadDanhSachVoucher và loadPhieuTotNhatTruoc...",
    );
    await Promise.all([loadDanhSachVoucher(), loadPhieuTotNhatTruoc()]);

    const idLienQuan = Number(msg.id);
    const daBiVoHieu =
      msg.action === "DELETED" ||
      (msg.trangThai !== undefined &&
        msg.trangThai !== 1 &&
        msg.trangThai !== "DANG_DIEN_RA");

    if (msg.action === "CREATED") {
      notify.success(`Vừa có phiếu giảm giá mới:  ${msg.tenGiamGia || ""}`);
      voucherStatusMsg.value = `Vừa có phiếu giảm giá mới: [${msg.maGiamGia}] ${
        msg.tenGiamGia || ""
      } và hợp lệ.`;
      voucherStatusType.value = "success";
    }

    const dangApDung =
      voucherDaApDungChiTiet.value || hoaDon.value?.phieuGiamGia;

    // 1. Khi phiếu bị TẮT / XÓA / HẾT HẠN
    if (daBiVoHieu || msg.action === "DELETED") {
      if (dangApDung && Number(dangApDung.id) === idLienQuan) {
        voucherDaApDungChiTiet.value = null;
        if (hoaDon.value) {
          hoaDon.value.phieuGiamGia = null;
          hoaDon.value.maGiamGia = null;
        }
        voucherStatusMsg.value = `Phiếu giảm giá [${dangApDung.maGiamGia}] đã bị tắt hoặc hết hạn!`;
        voucherStatusType.value = "error";
        loadData();
        notify.confirm(
          `Phiếu giảm giá [${dangApDung.maGiamGia}] đã bị tắt hoặc hết hạn.`,
        );
      } else {
        voucherStatusMsg.value = `Phiếu giảm giá [${msg.maGiamGia}] đã bị tắt hoặc hết hạn!`;
        voucherStatusType.value = "error";
        notify.confirm(
          `Phiếu giảm giá [${msg.maGiamGia}] đã bị tắt hoặc hết hạn.`,
        );
      }
    }

    // 2. Khi phiếu được BẬT lại (RESTORED hoặc UPDATE từ tắt sang bật)
    else if (
      msg.action === "RESTORED" ||
      (msg.action === "UPDATED" &&
        !daBiVoHieu &&
        msg.trangThai === "DANG_DIEN_RA")
    ) {
      voucherStatusMsg.value = `Phiếu giảm giá [${msg.maGiamGia}] đã được bật lại và hợp lệ.`;
      voucherStatusType.value = "success";
      notify.confirm(`Phiếu giảm giá [${msg.maGiamGia}] đã được bật lại.`);
      loadData();
    }

    // 3. Khi phiếu đang áp dụng được cập nhật (UPDATED và vẫn hoạt động)
    else if (msg.action === "UPDATED" && !daBiVoHieu) {
      if (dangApDung && Number(dangApDung.id) === idLienQuan) {
        try {
          const res = await getDetailPhieuGiamGia(idLienQuan);
          const normalized = normalizeVoucher(res.data);
          voucherDaApDungChiTiet.value = normalized;
          voucherStatusMsg.value = `Phiếu [${dangApDung.maGiamGia}] vừa được cập nhật giá trị giảm và hợp lệ.`;
          voucherStatusType.value = "success";
          loadData();
          notify.confirm(
            `Phiếu [${dangApDung.maGiamGia}] vừa được cập nhật giá trị giảm, số tiền đã được tính lại.`,
          );
        } catch (e) {
          voucherDaApDungChiTiet.value = null;
          if (hoaDon.value) {
            hoaDon.value.phieuGiamGia = null;
            hoaDon.value.maGiamGia = null;
          }
          voucherStatusMsg.value = `Phiếu giảm giá [${dangApDung.maGiamGia}] vừa được cập nhật nhưng xảy ra lỗi!`;
          voucherStatusType.value = "error";
          loadData();
        }
      } else {
        voucherStatusMsg.value = `Phiếu giảm giá [${msg.maGiamGia}] vừa được cập nhật và hợp lệ.`;
        voucherStatusType.value = "success";
        notify.confirm(`Phiếu giảm giá [${msg.maGiamGia}] vừa được cập nhật.`);
        loadData();
      }
    }
    if (
      !dangApDung &&
      phieuTotNhatHienTai.value?.id &&
      Number(phieuTotNhatHienTai.value.id) === idLienQuan &&
      msg.action === "UPDATED"
    ) {
      notify.warning(
        `Phiếu [${phieuTotNhatHienTai.value.maGiamGia}] đang được đề xuất vừa cập nhật giá trị giảm`,
      );
      voucherStatusMsg.value = `Phiếu giảm giá đề xuất [${phieuTotNhatHienTai.value.maGiamGia}] vừa cập nhật giá trị giảm và hợp lệ.`;
      voucherStatusType.value = "success";
    }
    const dangChon = voucherDangChon.value;
    if (
      dangChon &&
      Number(dangChon.id) === idLienQuan &&
      msg.action === "DELETED"
    ) {
      voucherDangChon.value = null;
    }
  }
};

onMounted(() => {
  loadThamSo();
  loadData();
  connectWebSocket(null, handleWsMessage);
});

onUnmounted(() => {
  disconnectWebSocket(handleWsMessage);
});

const toMoney = (value) => {
  const num = Number(value || 0);
  return Number(num.toFixed(2));
};

const formatTien = (value) => {
  const num = Number(value || 0);
  return (
    new Intl.NumberFormat("vi-VN", {
      minimumFractionDigits: num % 1 !== 0 ? 2 : 0,
      maximumFractionDigits: 2,
    }).format(num) + "đ"
  );
};

const diDenChonMon = () => {
  const banId = idBanAn || hoaDon.value?.idBanAn || hoaDon.value?.idBan;
  if (!banId) {
    notify.error("Không tìm thấy thông tin bàn để thêm món");
    return;
  }
  router.push({
    path: "/admin/check-in",
    query: {
      openBanAn: banId,
      openXemDonHang: "1",
      openChonMon: "1",
    },
  });
};

const diemTichLuySuDungChon = ref(0);
const diemTichLuyToiDa = computed(() => Number(hoaDon.value?.diemTichLuy || 0));

const diemHienCo = computed(() =>
  Math.max(diemTichLuyToiDa.value - diemTichLuySuDungChon.value, 0),
);

const diemTichLuySuDungHienThi = computed({
  get() {
    if (!diemTichLuySuDungChon.value) return "";
    return diemTichLuySuDungChon.value.toLocaleString("vi-VN");
  },
  set(val) {
    if (!val) {
      diemTichLuySuDungChon.value = 0;
      return;
    }
    const cleanNum = Number(String(val).replace(/\D/g, ""));
    diemTichLuySuDungChon.value = Math.min(cleanNum, diemTichLuyToiDa.value);
  },
});

const xacNhanDiemTichLuy = () => {
  const diem = Number(diemTichLuySuDungChon.value || 0);
  if (diem < 0) {
    notify.error("Điểm tích lũy không hợp lệ");
    return;
  }
  if (diem > diemTichLuyToiDa.value) {
    notify.error("Điểm sử dụng không được vượt quá số điểm hiện có");
    return;
  }
  const tienToiDaCoTheGiam = Math.max(
    tienSauGiam.value +
      toMoney(hoaDon.value.phiThuKhac) -
      toMoney(hoaDon.value.tienCoc),
    0,
  );
  if (diem > tienToiDaCoTheGiam) {
    notify.error(
      `Điểm tích lũy nhập vào không được vượt quá số tiền thanh toán (${formatTien(
        tienToiDaCoTheGiam,
      )})`,
    );
    return;
  }
  notify.success("Đã ghi nhận điểm tích lũy");
};
/* ================= COMPUTED ================= */

const soTienDisplay = computed({
  get() {
    if (form.value.soTien === "" || form.value.soTien == null) return "";
    return new Intl.NumberFormat("vi-VN").format(form.value.soTien);
  },
  set(val) {
    const numericStr = String(val).replace(/\D/g, "");
    form.value.soTien = numericStr ? Number(numericStr) : "";
  },
});

const daHoanThanh = computed(() => {
  const tt = Number(hoaDon.value.trangThaiHoaDon);
  return tt === 8;
});

const phuongThucDangChon = computed(() => {
  return danhSachPhuongThuc.value.find(
    (p) => Number(p.id) === Number(form.value.idPhuongThucThanhToan),
  );
});

const dsMonDaLen = computed(() => {
  return (hoaDon.value.dsMon || []).filter(
    (m) => Number(m.trangThai ?? 0) === 1,
  );
});

const dsMonDaLenGrouped = computed(() => {
  const grouped = {};
  dsMonDaLen.value.forEach((mon, index) => {
    const dm = mon.tenDanhMuc || "Khác";
    if (!grouped[dm]) grouped[dm] = [];
    grouped[dm].push({ ...mon, globalIndex: index });
  });
  return grouped;
});

const danhMucDangChon = ref("Tất cả");

const danhSachDanhMuc = computed(() => {
  return ["Tất cả", ...Object.keys(dsMonDaLenGrouped.value)];
});

const dsMonHienThi = computed(() => {
  if (danhMucDangChon.value === "Tất cả") {
    return Object.values(dsMonDaLenGrouped.value)
      .flat()
      .sort((a, b) => a.globalIndex - b.globalIndex);
  }
  return dsMonDaLenGrouped.value[danhMucDangChon.value] || [];
});

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

const tongTienMon = computed(() => {
  const tong = dsMonDaLen.value.reduce((tong, mon) => {
    if (mon.thanhTien != null) return tong + Number(mon.thanhTien);

    return (
      tong +
      Number(mon.soLuong || mon.soLuongChon || 0) *
        Number(mon.donGia || mon.gia || 0)
    );
  }, 0);

  return toMoney(tong);
});

const voucherThucTeApDung = computed(() => {
  if (voucherDaApDungChiTiet.value?.maGiamGia) {
    return voucherDaApDungChiTiet.value;
  }
  if (
    phieuTotNhatHienTai.value?.maGiamGia &&
    duDieuKienApDung(phieuTotNhatHienTai.value)
  ) {
    return phieuTotNhatHienTai.value;
  }
  return null;
});

const maGiamGiaDangApDung = computed(() => {
  return (
    voucherThucTeApDung.value?.maGiamGia ||
    hoaDon.value?.maGiamGia ||
    hoaDon.value?.phieuGiamGia?.maGiamGia ||
    null
  );
});

const idPhieuGiamGiaDangApDung = computed(() => {
  return (
    voucherThucTeApDung.value?.id || hoaDon.value?.phieuGiamGia?.id || null
  );
});

const soTienGiam = computed(() => {
  if (voucherDaApDungChiTiet.value?.maGiamGia) {
    return toMoney(soTienGiamTuVoucher.value);
  }
  if (
    phieuTotNhatHienTai.value?.maGiamGia &&
    duDieuKienApDung(phieuTotNhatHienTai.value)
  ) {
    return toMoney(soTienGiamTamTinh.value);
  }
  return toMoney(hoaDon.value.soTienDuocGiam || 0);
});

const giamTrucTiep = computed(() => {
  return toMoney(giamTrucTiepLocal.value || hoaDon.value.giamTrucTiep || 0);
});
const tienSauGiam = computed(() => {
  return toMoney(
    Math.max(tongTienMon.value - soTienGiam.value - giamTrucTiep.value, 0),
  );
});

const diemTichLuySuDung = computed(() =>
  Number(diemTichLuySuDungChon.value || 0),
);
const tongThanhToan = computed(() => {
  return toMoney(
    Math.max(
      tienSauGiam.value +
        toMoney(hoaDon.value.phiThuKhac) -
        toMoney(hoaDon.value.tienCoc) -
        diemTichLuySuDung.value,
      0,
    ),
  );
});

const tongDaThanhToan = computed(() => {
  return toMoney(
    (danhSachThanhToan.value || [])
      .filter((item) => Number(item.trangThai ?? 0) === 1)
      .reduce((sum, item) => {
        return sum + Number(item.soTien || item.tongTien || 0);
      }, 0),
  );
});

const tienConLai = computed(() => {
  if (daHoanThanh.value) return 0;
  return toMoney(Math.max(tongThanhToan.value - tongDaThanhToan.value, 0));
});

watch(
  () => form.value.idPhuongThucThanhToan,
  async () => {
    paymentUrl.value = "";
    qrCodeUrl.value = "";
    form.value.ghiChu = "";
    form.value.soTien = tienConLai.value > 0 ? toMoney(tienConLai.value) : 0;

    const pt = danhSachPhuongThuc.value.find(
      (p) => Number(p.id) === Number(form.value.idPhuongThucThanhToan),
    );
    if (!pt) return;

    const ten = String(pt.tenPhuongThuc || "")
      .trim()
      .toLowerCase();
    if (ten === "chuyển khoản" || ten === "chuyen khoan") {
      try {
        const amount =
          Number(form.value.soTien) || Number(tienConLai.value) || 0;
        const res = await getQrChuyenKhoan(idHoaDon, amount);
        qrCodeUrl.value = res.data?.qrCodeUrl || "";
      } catch (e) {
        console.error("Lỗi tạo mã QR:", e);
      }
    }
  },
);

watch(
  () => tienConLai.value,
  async (newVal) => {
    form.value.soTien = newVal > 0 ? toMoney(newVal) : 0;

    const pt = danhSachPhuongThuc.value.find(
      (p) => Number(p.id) === Number(form.value.idPhuongThucThanhToan),
    );
    if (!pt) return;

    const ten = String(pt.tenPhuongThuc || "")
      .trim()
      .toLowerCase();
    if (ten === "chuyển khoản" || ten === "chuyen khoan") {
      try {
        const amount = Number(form.value.soTien) || Number(newVal) || 0;
        const res = await getQrChuyenKhoan(idHoaDon, amount);
        qrCodeUrl.value = res.data?.qrCodeUrl || "";
      } catch (e) {
        console.error("Lỗi cập nhật mã QR khi đổi tiền:", e);
      }
    }
  },
);

/* ================= ACTION ================= */

const inHoaDonTam = async () => {
  try {
    nprogress.start();
    daInHoaDon.value = true;

    const res = await inHoaDonThanhToan(idHoaDon);
    const blob = new Blob([res.data || res], {
      type: "application/pdf",
    });
    const url = window.URL.createObjectURL(blob);

    window.open(url, "_blank");
  } catch (error) {
    console.error("Lỗi in hóa đơn:", error);
    notify.error("Không thể in hóa đơn");
  } finally {
    nprogress.done();
  }
};

const xacNhanThanhToan = async () => {
  errorPhuongThuc.value = "";
  errorSoTien.value = "";

  if (!form.value.idPhuongThucThanhToan) {
    errorPhuongThuc.value = "Vui lòng chọn phương thức thanh toán";
    return;
  }

  const soTienNhap = toMoney(form.value.soTien);
  const soTienConLai = toMoney(tienConLai.value);

  if (Number.isNaN(soTienNhap) || soTienNhap <= 0) {
    errorSoTien.value = "Vui lòng nhập số tiền hợp lệ";
    return;
  }

  if (soTienConLai <= 0) {
    errorSoTien.value = "Hóa đơn này đã đủ tiền, hãy hoàn tất hóa đơn";
    return;
  }

  const pt = danhSachPhuongThuc.value.find(
    (p) => Number(p.id) === Number(form.value.idPhuongThucThanhToan),
  );

  if (!pt) {
    notify.error("Không tìm thấy phương thức thanh toán");
    return;
  }

  const tenPhuongThuc = String(pt.tenPhuongThuc || "")
    .trim()
    .toLowerCase();

  const laTienMat =
    tenPhuongThuc.includes("tiền mặt") || tenPhuongThuc.includes("tien mat");

  if (!laTienMat && soTienNhap > soTienConLai) {
    notify.error(
      `Số tiền không được vượt quá số còn lại: ${formatTien(soTienConLai)}`,
    );
    return;
  }

  if (!(await notify.confirm("Xác nhận thanh toán hóa đơn này?"))) return;

  const soTienThucTeThanhToan = laTienMat
    ? soTienNhap
    : Math.min(soTienNhap, soTienConLai);

  const payload = {
    idHoaDon: Number(form.value.idHoaDon),
    idPhuongThucThanhToan: Number(form.value.idPhuongThucThanhToan),
    soTien: toMoney(soTienThucTeThanhToan),
    ghiChu: form.value.ghiChu || "",
    diemTichLuySuDung: diemTichLuySuDung.value,
    giamTrucTiep: giamTrucTiepLocal.value || 0,
    lyDoGiamTrucTiep: lyDoGiamTrucTiepLocal.value || "",
    maGiamGia: maGiamGiaDangApDung.value || null,
    idPhieuGiamGia: idPhieuGiamGiaDangApDung.value || null,
  };
  dangTaoLinkThanhToan.value = true;
  nprogress.start();

  try {
    paymentUrl.value = "";
    qrCodeUrl.value = "";

    if (laTienMat) {
      await thanhToanTienMat(payload);
      notify.success("Thanh toán tiền mặt thành công");
    } else {
      if (
        tenPhuongThuc.includes("chuyển khoản") ||
        tenPhuongThuc.includes("chuyen khoan")
      ) {
        // Chỉ lấy QR hiển thị lên modal, chưa tạo giao dịch
        const res = await getQrChuyenKhoan(payload.idHoaDon, payload.soTien);
        qrCodeUrl.value = res.data?.qrCodeUrl || "";
        if (qrCodeUrl.value) {
          payloadChuyenKhoan.value = payload;
          tienThuaChuyenKhoan.value = 0; // Chuyển khoản thường không có tiền thừa
          laChuyenKhoanThuCong.value = true;
          hienModalQR.value = true;
          return; // Dừng lại ở đây, chờ người dùng ấn "Đã thanh toán"
        } else {
          notify.error("Không thể tạo mã QR, vui lòng thử lại");
          return;
        }
      } else {
        // ⭐ VNPay/ZaloPay: chỉ lấy link/QR để hiển thị, CHƯA trừ voucher/điểm/tạo ThanhToan
        const res = await taoLinkThanhToanOnline(payload);
        const data = res?.data || {};

        paymentUrl.value = data.paymentUrl || data.order_url || "";
        qrCodeUrl.value = data.qrCodeUrl || data.qr_code || "";

        // Nếu cổng thanh toán trả về link nhưng không có QR sẵn (như VNPay), tự tạo QR từ link
        if (paymentUrl.value && !qrCodeUrl.value) {
          try {
            qrCodeUrl.value = await QRCode.toDataURL(paymentUrl.value, {
              width: 300,
              margin: 1,
            });
          } catch (err) {
            console.error("Lỗi tạo QR từ link thanh toán", err);
          }
        }

        if (qrCodeUrl.value) {
          payloadChuyenKhoan.value = payload;
          tienThuaChuyenKhoan.value = 0;
          laChuyenKhoanThuCong.value = false;
          hienModalQR.value = true;
          return; // Hiện modal thay vì chuyển hướng
        } else if (paymentUrl.value) {
          notify.success("Đang chuyển sang cổng thanh toán");
          window.location.href = paymentUrl.value;
          return;
        } else {
          notify.success(data.message || "Tạo thanh toán thành công");
        }
      }
    }

    const tienThua = laTienMat
      ? Math.max(toMoney(soTienNhap - soTienConLai), 0)
      : 0;

    await xuLySauThanhToan(tienThua);
  } catch (e) {
    console.error("Lỗi thanh toán:", e);
    const errorMsg =
      e?.response?.data?.message || e?.response?.data || "Thanh toán thất bại";
    notify.error(errorMsg);

    if (
      typeof errorMsg === "string" &&
      errorMsg.toLowerCase().includes("kiểm tra lại tổng tiền")
    ) {
      await loadData();
    }
  } finally {
    dangTaoLinkThanhToan.value = false;
    nprogress.done();
  }
};

async function xuLySauThanhToan(tienThua = 0) {
  form.value.ghiChu = "";
  await loadData();

  const tienConLaiSauLoad = toMoney(tienConLai.value);

  if (tienConLaiSauLoad <= 0) {
    form.value.soTien = 0;
    form.value.ghiChu = "";

    if (tienThua > 0) {
      notify.success(
        `Thanh toán thành công. Tiền thừa trả khách: ${formatTien(tienThua)}`,
      );
    } else {
      notify.success("Thanh toán thành công");
    }

    hienModalDanhGia.value = true;
    return;
  }

  form.value.soTien = tienConLai.value > 0 ? toMoney(tienConLai.value) : 0;

  if (tienThua > 0) {
    notify.success(
      `Đã ghi nhận thanh toán. Tiền thừa trả khách: ${formatTien(tienThua)}`,
    );
  } else {
    notify.success("Đã ghi nhận thanh toán");
  }
}

async function xacNhanDaThanhToanQR() {
  hienModalQR.value = false;
  dangTaoLinkThanhToan.value = true;
  nprogress.start();
  try {
    if (laChuyenKhoanThuCong.value) {
      const res = await thanhToanChuyenKhoan(payloadChuyenKhoan.value);
      const data = res?.data || {};
      notify.success(data.message || "Thanh toán chuyển khoản thành công");
    } else {
      const res = await xacNhanThanhToanOnline(payloadChuyenKhoan.value);
      const data = res?.data || {};
      notify.success(data.message || "Thanh toán thành công");
    }

    await xuLySauThanhToan(tienThuaChuyenKhoan.value);
  } catch (e) {
    console.error("Lỗi thanh toán QR:", e);
    const errorMsg =
      e?.response?.data?.message || e?.response?.data || "Thanh toán thất bại";
    notify.error(errorMsg);

    if (
      typeof errorMsg === "string" &&
      errorMsg.toLowerCase().includes("kiểm tra lại tổng tiền")
    ) {
      await loadData();
    }
  } finally {
    dangTaoLinkThanhToan.value = false;
    nprogress.done();
  }
}

function dongModalQR() {
  hienModalQR.value = false;
  payloadChuyenKhoan.value = null;
}

function dongTrang() {
  router.push({
    path: "/admin/check-in",
  });
}

/* ================= FORMAT ================= */

const formatDateTime = (v) => (v ? new Date(v).toLocaleString("vi-VN") : "--");

const layAnhMon = (hinhAnh) => {
  if (!hinhAnh) return "/default-food.png";

  if (typeof hinhAnh === "string") {
    if (hinhAnh.startsWith("http")) return hinhAnh;
    const clean = hinhAnh.replace(/^\/+/, "");
    return `http://localhost:8080/uploads/${clean}`;
  }
  return "/default-food.png";
};

function onImgError(event) {
  event.target.src = "/default-food.png";
}
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
                  class="bi bi-credit-card"
                  style="color: rgb(225, 29, 72); font-size: 24px"
                ></i>
              </span>
              Phiếu thanh toán
            </h2>
            <p>
              Thanh toán cho hóa đơn -
              <strong>{{ hoaDon.maHoaDon || "HD-" + idHoaDon }}</strong>
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
                  hoaDon.tenKhachHang || "Khách lẻ"
                }}</span>
              </div>
              <div class="info-row">
                <span class="info-field-label">
                  <i class="bi bi-telephone"></i> Điện thoại:
                </span>
                <span class="info-field-value">
                  {{ hoaDon.soDienThoai || "Khách vãng lai" }}
                </span>
              </div>
            </div>
            <div class="info-box">
              <div class="info-box-label">
                <i class="bi bi-geo-alt-fill" style="color: #10b981"></i>
                Bàn &amp; Vị trí
              </div>
              <div class="info-row">
                <span class="info-field-label"
                  ><i class="bi bi-grid"></i> Bàn ăn:</span
                >
                <span class="info-field-value">{{
                  hoaDon.tenBanAn || "--"
                }}</span>
              </div>
              <div class="info-row">
                <span class="info-field-label"
                  ><i class="bi bi-map"></i> Khu vực:</span
                >
                <span class="info-field-value">{{
                  hoaDon.tenKhuVuc || "--"
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
                <span class="info-field-value">{{
                  hoaDon.maHoaDon || "HD-" + idHoaDon
                }}</span>
              </div>
              <div class="info-row">
                <span class="info-field-label"
                  ><i class="bi bi-clock"></i> Thời gian:</span
                >
                <span class="info-field-value">{{
                  formatDateTime(hoaDon.thoiGianTao)
                }}</span>
              </div>
            </div>
          </div>

          <div class="category-tabs" v-if="dsMonDaLen.length > 0">
            <button
              v-for="dm in danhSachDanhMuc"
              :key="dm"
              class="tab-btn"
              :class="{ active: danhMucDangChon === dm }"
              @click="danhMucDangChon = dm"
            >
              {{ dm }} ({{
                dm === "Tất cả"
                  ? dsMonDaLen.length
                  : dsMonDaLenGrouped[dm].length
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
            <div v-if="!dsMonDaLen.length" class="empty-mon">
              Chưa có món nào đã phục vụ
            </div>

            <template v-else>
              <div
                v-for="(item, index) in danhSachHienThiPhanTrang"
                :key="item.id || item.globalIndex"
                class="mon-row"
              >
                <div class="col-stt">
                  {{ (currentPage - 1) * pageSize + index + 1 }}
                </div>
                <div class="col-ten">
                  <div class="mon-thumb-wrap-inline">
                    <img
                      v-if="layAnhMon(item.urlAnh || item.hinhAnh || item.anh)"
                      :src="layAnhMon(item.urlAnh || item.hinhAnh || item.anh)"
                      alt="Ảnh món"
                      class="mon-thumb-inline"
                      @error="onImgError"
                    />
                    <div v-else class="mon-thumb-inline placeholder">IMG</div>
                  </div>
                  <div class="mon-info-inline">
                    <div class="ma-sp">
                      {{ item.maSanPham || item.ma || "--" }}
                    </div>
                    <div>{{ item.tenSanPham || item.tenMon || "--" }}</div>
                  </div>
                </div>
                <div class="col-sl">
                  {{ item.soLuong || item.soLuongChon || 0 }}
                </div>
                <div class="col-gia">
                  <!-- NẾU CÓ GIẢM GIÁ THÌ HIỆN GẠCH NGANG -->
                  <template
                    v-if="
                      Number(item.giaSauGiam || 0) > 0 &&
                      Number(item.giaSauGiam || 0) < Number(item.giaGoc || 0)
                    "
                  >
                    <div class="gia-goc-inline">
                      {{ formatTien(item.giaGoc) }}
                    </div>
                    <div class="gia-giam-inline">
                      {{ formatTien(item.giaSauGiam) }}
                    </div>
                  </template>

                  <!-- NẾU KHÔNG THÌ HIỆN BÌNH THƯỜNG -->
                  <template v-else>
                    {{ formatTien(item.donGia || item.gia || 0) }}
                  </template>
                </div>
                <div class="col-tt">
                  {{
                    formatTien(
                      item.thanhTien ??
                        Number(item.soLuong || item.soLuongChon || 0) *
                          Number(item.donGia || item.gia || 0),
                    )
                  }}
                </div>
              </div>
            </template>
          </div>

          <div
            style="
              display: flex;
              justify-content: center;
              align-items: center;
              margin-top: 16px;
            "
          >
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
          </div>

          <!-- PHÂN TÁCH BỐ CỤC -->
          <div class="section-divider">
            <span class="divider-line"></span>
            <span class="divider-text">Chương trình Ưu đãi & Giảm giá</span>
            <span class="divider-line"></span>
          </div>

          <!-- ƯU ĐÃI / GIẢM GIÁ -->
          <div class="uu-dai-box">
            <div class="uu-dai-header">
              <div class="uu-dai-title">
                <i class="ri-coupon-2-line"></i>
                Ưu đãi / Giảm giá
              </div>
              <div style="display: flex; gap: 8px">
                <button
                  class="btn btn-sm"
                  style="
                    border-radius: 6px;
                    border: 1px solid var(--color-border);
                    background: var(--color-white);
                    color: var(--color-text);
                    font-weight: 600;
                    padding: 4px 10px;
                  "
                  @click="moPopupGiamGiaTrucTiep"
                >
                  <i class="ri-money-dollar-box-line"></i> Giảm trực tiếp
                </button>
                <div class="current-voucher-clickable" @click="moPopupVoucher">
                  <span v-if="maGiamGiaDangApDung" class="badge-applied">
                    {{ maGiamGiaDangApDung }}
                    <i class="ri-arrow-right-s-line"></i>
                  </span>
                  <span v-else class="badge-not-applied">
                    Chọn mã <i class="ri-arrow-right-s-line"></i>
                  </span>
                </div>
              </div>
            </div>

            <div style="min-height: 60px; transition: all 0.3s ease">
              <div
                v-if="maGiamGiaDangApDung && voucherDaApDungChiTiet"
                style="
                  border: 1px solid #f97316;
                  background-color: #fffaf0;
                  padding: 12px;
                  border-radius: 7px;
                  position: relative;
                  margin-top: 10px;
                "
              >
                <div
                  style="
                    position: absolute;
                    top: 0;
                    right: 0;
                    background-color: #f97316;
                    color: white;
                    padding: 2px 8px;
                    font-size: 11px;
                    font-weight: bold;
                    border-bottom-left-radius: 6px;
                    border-top-right-radius: 7px;
                  "
                >
                  ĐANG ÁP DỤNG
                </div>
                <div style="display: flex; gap: 10px">
                  <div
                    style="color: #ea580c; font-size: 24px; padding-top: 4px"
                  >
                    <i class="ri-coupon-3-fill"></i>
                  </div>
                  <div style="flex: 1">
                    <div
                      style="
                        color: #b45309;
                        font-weight: bold;
                        margin-bottom: 6px;
                        font-size: 14px;
                      "
                    >
                      [{{ voucherDaApDungChiTiet.maGiamGia }}]
                      {{ voucherDaApDungChiTiet.tenGiamGia }}
                    </div>
                    <div
                      style="
                        display: flex;
                        font-size: 12px;
                        color: #78716c;
                        margin-bottom: 4px;
                      "
                    >
                      <div style="flex: 1">
                        Loại:
                        {{
                          voucherDaApDungChiTiet.phuongThucGiamGia === 1
                            ? "Phần trăm"
                            : "Tiền mặt"
                        }}
                      </div>
                      <div style="flex: 1">
                        Giảm:
                        <strong style="color: #ea580c">{{
                          hienThiGiaTriGiam(voucherDaApDungChiTiet)
                        }}</strong>
                      </div>
                    </div>
                    <div style="font-size: 12px; color: #a8a29e">
                      HSD:
                      {{ formatDateTime(voucherDaApDungChiTiet.ngayBatDau) }}
                      -
                      {{ formatDateTime(voucherDaApDungChiTiet.ngayKetThuc) }}
                    </div>
                  </div>
                </div>
              </div>

              <div
                v-else-if="
                  phieuTotNhatHienTai && duDieuKienApDung(phieuTotNhatHienTai)
                "
                style="
                  border: 1px solid #f97316;
                  background-color: #fffaf0;
                  padding: 12px;
                  border-radius: 7px;
                  position: relative;
                  margin-top: 10px;
                "
              >
                <div
                  style="
                    position: absolute;
                    top: 0;
                    right: 0;
                    background-color: #f97316;
                    color: white;
                    padding: 2px 8px;
                    font-size: 11px;
                    font-weight: bold;
                    border-bottom-left-radius: 6px;
                    border-top-right-radius: 7px;
                  "
                >
                  TỐT NHẤT
                </div>
                <div style="display: flex; gap: 10px">
                  <div
                    style="color: #ea580c; font-size: 24px; padding-top: 4px"
                  >
                    <i class="ri-coupon-3-fill"></i>
                  </div>
                  <div style="flex: 1">
                    <div
                      style="
                        color: #b45309;
                        font-weight: bold;
                        margin-bottom: 6px;
                        font-size: 14px;
                      "
                    >
                      [{{ phieuTotNhatHienTai.maGiamGia }}]
                      {{ phieuTotNhatHienTai.tenGiamGia }}
                    </div>
                    <div
                      style="
                        display: flex;
                        font-size: 12px;
                        color: #78716c;
                        margin-bottom: 4px;
                      "
                    >
                      <div style="flex: 1">
                        Loại:
                        {{
                          phieuTotNhatHienTai.phuongThucGiamGia === 1
                            ? "Phần trăm"
                            : "Tiền mặt"
                        }}
                      </div>
                      <div style="flex: 1">
                        Giảm:
                        <strong style="color: #ea580c">{{
                          hienThiGiaTriGiam(phieuTotNhatHienTai)
                        }}</strong>
                      </div>
                    </div>
                    <div style="font-size: 12px; color: #a8a29e">
                      HSD:
                      {{ formatDateTime(phieuTotNhatHienTai.ngayBatDau) }} -
                      {{ formatDateTime(phieuTotNhatHienTai.ngayKetThuc) }}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div
              class="voucher-card short-card applied mt-3"
              style="
                cursor: default;
                padding: 12px;
                margin-top: 10px;
                border-color: var(--color-warning);
                background: #fffcf0;
              "
              v-if="giamTrucTiepLocal > 0"
            >
              <div class="voucher-row-1">
                <strong>Giảm giá trực tiếp</strong>
                <span class="voucher-giam" style="color: var(--color-warning)">
                  -{{ formatTien(giamTrucTiepLocal) }}
                </span>
              </div>
              <div
                class="voucher-row-3 voucher-applied-label"
                style="margin-top: 6px; color: var(--color-warning)"
              >
                <i class="ri-checkbox-circle-fill"></i> Đang được áp dụng
              </div>
            </div>

            <div
              v-if="danhSachVoucherGoiYHienThi.length > 0"
              style="
                border: 1px solid #fcd34d;
                background-color: #fffbeb;
                padding: 12px;
                border-radius: 7px;
                margin-top: 15px;
              "
            >
              <div
                style="
                  color: #d97706;
                  font-weight: bold;
                  margin-bottom: 12px;
                  display: flex;
                  align-items: center;
                  gap: 6px;
                "
              >
                <i class="ri-gift-line" style="font-size: 16px"></i> Gợi ý ưu
                đãi
              </div>
              <div
                v-for="voucher in danhSachVoucherGoiYHienThi"
                :key="'goiy-' + (voucher.id || voucher.maGiamGia)"
                style="
                  background-color: white;
                  padding: 12px;
                  border-radius: 7px;
                  margin-bottom: 10px;
                  border: 1px solid #fef08a;
                "
              >
                <div
                  style="
                    color: #b45309;
                    font-weight: bold;
                    margin-bottom: 6px;
                    font-size: 14px;
                  "
                >
                  [{{ voucher.maGiamGia }}] {{ voucher.tenGiamGia }}
                </div>
                <div
                  style="
                    display: flex;
                    font-size: 12px;
                    color: #78716c;
                    margin-bottom: 4px;
                  "
                >
                  <div style="flex: 1">
                    Loại:
                    {{
                      voucher.phuongThucGiamGia === 1 ? "Phần trăm" : "Tiền mặt"
                    }}
                  </div>
                  <div style="flex: 1">
                    Giảm:
                    <strong style="color: #ea580c">
                      {{ hienThiGiaTriGiam(voucher) }}
                      <span
                        v-if="voucher.phuongThucGiamGia === 1 && voucher.giamToiDa"
                        style="font-size: 11px; font-weight: normal; color: #78716c"
                      >
                        (tối đa {{ formatTien(voucher.giamToiDa) }})
                      </span>
                    </strong>
                  </div>
                </div>
                <div
                  style="
                    font-size: 12px;
                    color: #a8a29e;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-top: 6px;
                  "
                >
                  <span
                    >Tối thiểu:
                    {{ formatTien(voucher.dieuKienSuDung || 0) }}</span
                  >
                  <button
                    type="button"
                    class="btn-an-them"
                    @click="diDenChonMon"
                  >
                    <i class="ri-add-circle-line" style="font-size: 13px"></i>
                    <span
                      >Ăn thêm:
                      {{
                        formatTien(
                          Math.max(
                            0,
                            Number(voucher.dieuKienSuDung || 0) -
                              Number(tongTienMon || 0),
                          ),
                        )
                      }}</span
                    >
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="payment-panel">
          <div class="payment-top-row">
            <!-- LEFT: Summary -->
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
                  <strong>{{ formatTien(tongTienMon) }}</strong>
                </div>
                <div class="summary-line" v-if="soTienGiam > 0">
                  <div style="display: flex; align-items: center">
                    <span>Giảm giá</span>
                    <span
                      v-if="maGiamGiaDangApDung"
                      class="discount-badge ms-2"
                    >
                      {{ maGiamGiaDangApDung }}
                    </span>
                  </div>
                  <strong style="color: #ef4444"
                    >- {{ formatTien(soTienGiam) }}</strong
                  >
                </div>

                <div class="summary-line" v-if="giamTrucTiep > 0">
                  <span>Giảm trực tiếp</span>
                  <strong style="color: #ef4444"
                    >- {{ formatTien(giamTrucTiep) }}</strong
                  >
                </div>

                <div
                  class="summary-line"
                  v-if="tienSauGiam > 0 && tienSauGiam < tongTienMon"
                >
                  <span>Tiền sau giảm</span>
                  <strong>{{ formatTien(tienSauGiam) }}</strong>
                </div>

                <div class="summary-line" v-if="hoaDon.tienCoc > 0">
                  <span>Tiền cọc</span>
                  <strong>{{ formatTien(hoaDon.tienCoc) }}</strong>
                </div>

                <div class="summary-line">
                  <span>Điểm tích lũy</span>
                  <div
                    style="
                      display: flex;
                      flex-direction: column;
                      align-items: flex-end;
                      gap: 4px;
                    "
                  >
                    <div class="diem-tich-luy-card">
                      <input
                        type="text"
                        class="diem-tich-luy-input-v2"
                        v-model="diemTichLuySuDungHienThi"
                        placeholder="Nhập số điểm ..."
                        :disabled="diemHienCo <= 0"
                      />
                      <button
                        class="diem-tich-luy-btn-v2"
                        @click="xacNhanDiemTichLuy"
                        :disabled="diemHienCo <= 0"
                      >
                        <i class="ri-check-line"></i>
                      </button>
                    </div>
                    <span style="font-size: 12px; color: red"
                      >Hiện có: {{ formatTien(diemHienCo) }}</span
                    >
                  </div>
                </div>
                <div class="summary-line" v-if="tongDaThanhToan > 0">
                  <span>Đã thanh toán</span>
                  <strong style="color: #059669"
                    >- {{ formatTien(tongDaThanhToan) }}</strong
                  >
                </div>

                <div class="summary-total">
                  <span>Tiền phải thanh toán</span>
                  <strong>{{ formatTien(tienConLai) }}</strong>
                </div>
              </div>
            </div>

            <!-- RIGHT: Inputs -->
            <div class="payment-input-col">
              <div class="pay-input-box">
                <label>Khách hàng trả</label>
                <input
                  v-model="soTienDisplay"
                  type="text"
                  inputmode="numeric"
                  :class="{ 'input-invalid': errorSoTien }"
                  placeholder="Nhập số tiền..."
                  @input="errorSoTien = ''"
                />
                <transition name="shake">
                  <p v-if="errorSoTien" class="error-hint mt-2 mb-0">
                    <i class="bi bi-exclamation-circle"></i> {{ errorSoTien }}
                  </p>
                </transition>

                <!-- Vùng hiển thị động -->
                <transition name="shake">
                  <div v-if="form.soTien !== '' && form.soTien !== null">
                    <div
                      v-if="Number(form.soTien) < tienConLai"
                      class="error-hint"
                    >
                      <i class="bi bi-exclamation-circle"></i> Khách còn thiếu:
                      <strong>{{
                        formatTien(tienConLai - Number(form.soTien))
                      }}</strong>
                    </div>
                    <div
                      v-else-if="Number(form.soTien) > tienConLai"
                      class="error-hint"
                      style="color: #059669; background-color: #d1fae5"
                    >
                      <i class="bi bi-check-circle"></i> Tiền thừa trả khách:
                      <strong>{{
                        formatTien(Number(form.soTien) - tienConLai)
                      }}</strong>
                    </div>
                  </div>
                </transition>
              </div>

              <div class="pay-input-box">
                <label>Ghi chú</label>
                <input
                  v-model="form.ghiChu"
                  type="text"
                  placeholder="Nhập ghi chú nếu có"
                />
              </div>
            </div>
          </div>

          <!-- BOTTOM: Payment Methods -->
          <div class="payment-methods-row">
            <div
              class="pay-method-group pay-method-online"
              style="border-right: none; width: 100%"
            >
              <div class="method-group-title">Chọn phương thức thanh toán</div>
              <div class="pay-methods pay-methods-horizontal">
                <label
                  v-for="pt in danhSachPhuongThuc.filter(
                    (p) =>
                      !String(p.tenPhuongThuc || '')
                        .toLowerCase()
                        .includes('momo'),
                  )"
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
                    @change="errorPhuongThuc = ''"
                    style="display: none"
                  />

                  <img
                    v-if="
                      String(pt.tenPhuongThuc || '')
                        .trim()
                        .toLowerCase()
                        .includes('zalopay')
                    "
                    :src="ZaloPay"
                    class="icon-pt"
                    alt="ZaloPay"
                  />
                  <img
                    v-else-if="
                      String(pt.tenPhuongThuc || '')
                        .trim()
                        .toLowerCase()
                        .includes('vnpay')
                    "
                    :src="VNPAY"
                    class="icon-pt"
                    alt="VNPay"
                  />
                  <i
                    v-else-if="
                      String(pt.tenPhuongThuc || '')
                        .trim()
                        .toLowerCase()
                        .includes('tiền mặt') ||
                      String(pt.tenPhuongThuc || '')
                        .trim()
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

                  <span>{{
                    pt.tenPhuongThuc.replace(/ngân hàng/i, "").trim()
                  }}</span>
                </label>
              </div>
              <transition name="shake">
                <p
                  v-if="errorPhuongThuc"
                  class="error-hint error-phuong-thuc mt-2 mb-0"
                >
                  <i class="bi bi-exclamation-circle"></i> {{ errorPhuongThuc }}
                </p>
              </transition>
            </div>
          </div>

          <!-- Dòng thông tin/cảnh báo voucher -->
          <transition name="shake">
            <div
              v-if="voucherStatusMsg"
              class="error-hint error-phuong-thuc mt-2 mb-3 d-flex align-items-center gap-2"
              :style="{
                background:
                  voucherStatusType === 'success' ? '#f0fdf4' : '#fff5f5',
                border:
                  '1px solid ' +
                  (voucherStatusType === 'success' ? '#bbf7d0' : '#feb2b2'),
                padding: '10px 15px',
                borderRadius: '8px',
                color: voucherStatusType === 'success' ? '#15803d' : '#c53030',
                fontSize: '13.5px',
                fontWeight: '500',
              }"
            >
              <i
                :class="
                  voucherStatusType === 'success'
                    ? 'bi bi-check-circle-fill'
                    : 'bi bi-exclamation-triangle-fill'
                "
                style="font-size: 16px"
              ></i>
              <span>{{ voucherStatusMsg }}</span>
            </div>
          </transition>

          <div class="footer-actions">
            <button class="btn-cancel" type="button" @click="inHoaDonTam">
              <i class="bi bi-printer"></i> In hóa đơn
            </button>
            <button
              class="btn-cancel"
              type="button"
              @click="moModalGhiNo"
              style="background: #fff7ed; color: #ea580c; border-color: #fdba74"
            >
              <i class="bi bi-journal-minus"></i> Ghi nợ
            </button>
            <button
              class="btn-primary"
              type="button"
              :disabled="dangTaoLinkThanhToan"
              @click="xacNhanThanhToan"
              style="
                display: inline-flex;
                align-items: center;
                justify-content: center;
                gap: 8px;
              "
            >
              <div class="btn-light-sweep"></div>
              <i class="bi bi-check2-all" style="font-size: 18px"></i>
              <span>{{
                dangTaoLinkThanhToan ? "Đang xử lý..." : "Xác nhận"
              }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
  <!-- Popup Nhập Giảm Giá Trực Tiếp -->
  <Teleport to="body">
    <div
      v-if="showPopupGiamGiaTrucTiep"
      class="voucher-popup-overlay"
      @click.self="showPopupGiamGiaTrucTiep = false"
    >
      <div
        class="voucher-popup giam-truc-tiep-popup"
        style="height: auto; max-height: 90vh"
      >
        <div class="voucher-popup-header">
          <h3>Nhập số tiền giảm giá trực tiếp</h3>
          <button
            class="btn btn-icon btn-close-popup"
            @click="showPopupGiamGiaTrucTiep = false"
          >
            ✕
          </button>
        </div>

        <div style="padding: 16px">
          <div class="discount-type-tabs">
            <label
              class="discount-tab"
              :class="{ active: kieuGiamGiaTrucTiep === 'VNĐ' }"
            >
              <input
                type="radio"
                v-model="kieuGiamGiaTrucTiep"
                value="VNĐ"
                style="display: none"
              />
              Theo số tiền (VNĐ)
            </label>
            <label
              class="discount-tab"
              :class="{ active: kieuGiamGiaTrucTiep === '%' }"
            >
              <input
                type="radio"
                v-model="kieuGiamGiaTrucTiep"
                value="%"
                style="display: none"
              />
              Theo phần trăm (%)
            </label>
          </div>

          <div
            class="form-group"
            style="margin-top: 16px"
            v-if="kieuGiamGiaTrucTiep === 'VNĐ'"
          >
            <label style="font-weight: 600; margin-bottom: 8px; display: block"
              >Số tiền muốn giảm (VNĐ)</label
            >
            <input
              type="text"
              class="form-control"
              :class="{ 'input-invalid': errorGiamGiaTrucTiep }"
              v-model="soTienGiamTrucTiepInput"
              placeholder="Nhập số tiền..."
              style="font-size: 16px; padding: 12px; font-weight: bold"
              @input="errorGiamGiaTrucTiep = ''"
            />
            <transition name="shake">
              <p v-if="errorGiamGiaTrucTiep" class="error-hint mt-2 mb-0">
                <i class="bi bi-exclamation-circle"></i>
                {{ errorGiamGiaTrucTiep }}
              </p>
            </transition>
            <div class="quick-btn-list mt-2">
              <button
                class="quick-btn"
                v-if="10000 <= gioiHanGiamGiaTrucTiep"
                @click="soTienGiamTrucTiepInput = 10000"
              >
                10.000đ
              </button>
              <button
                class="quick-btn"
                v-if="20000 <= gioiHanGiamGiaTrucTiep"
                @click="soTienGiamTrucTiepInput = 20000"
              >
                20.000đ
              </button>
              <button
                class="quick-btn"
                v-if="50000 <= gioiHanGiamGiaTrucTiep"
                @click="soTienGiamTrucTiepInput = 50000"
              >
                50.000đ
              </button>
              <button
                class="quick-btn"
                v-if="100000 <= gioiHanGiamGiaTrucTiep"
                @click="soTienGiamTrucTiepInput = 100000"
              >
                100.000đ
              </button>
              <button
                class="quick-btn"
                v-if="200000 <= gioiHanGiamGiaTrucTiep"
                @click="soTienGiamTrucTiepInput = 200000"
              >
                200.000đ
              </button>
              <button
                class="quick-btn btn-max"
                @click="soTienGiamTrucTiepInput = gioiHanGiamGiaTrucTiep"
              >
                Tối đa ({{ formatTien(gioiHanGiamGiaTrucTiep) }})
              </button>
            </div>
          </div>

          <div class="form-group" style="margin-top: 16px" v-else>
            <label style="font-weight: 600; margin-bottom: 8px; display: block"
              >Số phần trăm muốn giảm (%)</label
            >
            <input
              type="number"
              class="form-control"
              :class="{ 'input-invalid': errorGiamGiaTrucTiep }"
              v-model.number="phanTramGiamGiaInput"
              placeholder="Nhập %..."
              min="0"
              :max="phanTramGioiHan"
              style="font-size: 16px; padding: 12px; font-weight: bold"
              @input="errorGiamGiaTrucTiep = ''"
            />
            <transition name="shake">
              <p v-if="errorGiamGiaTrucTiep" class="error-hint mt-2 mb-0">
                <i class="bi bi-exclamation-circle"></i>
                {{ errorGiamGiaTrucTiep }}
              </p>
            </transition>
            <div class="quick-btn-list mt-2">
              <button
                class="quick-btn"
                v-if="5 <= phanTramGioiHan"
                @click="phanTramGiamGiaInput = 5"
              >
                5%
              </button>
              <button
                class="quick-btn"
                v-if="10 <= phanTramGioiHan"
                @click="phanTramGiamGiaInput = 10"
              >
                10%
              </button>
              <button
                class="quick-btn"
                v-if="15 <= phanTramGioiHan"
                @click="phanTramGiamGiaInput = 15"
              >
                15%
              </button>
              <button
                class="quick-btn btn-max"
                @click="phanTramGiamGiaInput = phanTramGioiHan"
              >
                Tối đa ({{ phanTramGioiHan }}%)
              </button>
            </div>
            <div
              style="
                margin-top: 8px;
                color: var(--color-primary);
                font-weight: 500;
              "
              v-if="phanTramGiamGiaInput > 0"
            >
              ≈ Giảm
              {{
                formatTien(
                  Math.floor(tongTienMon * (phanTramGiamGiaInput / 100)),
                )
              }}
            </div>
          </div>

          <div
            style="
              margin-top: 20px;
              background: #fff7ed;
              border: 1px solid #fed7aa;
              border-radius: 10px;
              padding: 12px 16px;
              display: flex;
              gap: 10px;
              align-items: flex-start;
            "
          >
            <i
              class="bi bi-exclamation-triangle-fill"
              style="
                color: #ea580c;
                font-size: 15px;
                flex-shrink: 0;
                margin-top: 2px;
              "
            ></i>
            <span
              style="
                font-size: 12.5px;
                color: #9a3412;
                font-weight: 500;
                line-height: 1.5;
              "
            >
              <strong>Lưu ý:</strong> Nhân viên tự ý áp dụng giảm giá trực tiếp
              khi chưa có sự đồng ý của Quản lý sẽ phải chịu hoàn toàn trách
              nhiệm về khoản chênh lệch phát sinh.
            </span>
          </div>

          <div class="form-group" style="margin-top: 14px">
            <label
              style="
                font-weight: 600;
                margin-bottom: 8px;
                display: block;
                font-size: 13.5px;
              "
            >
              Lý do giảm giá <span style="color: #dc2626">*</span>
            </label>
            <textarea
              v-model="lyDoGiamTrucTiep"
              rows="3"
              class="form-control"
              placeholder="Nhập lý do áp dụng giảm giá trực tiếp (bắt buộc)..."
              style="
                font-size: 14px;
                padding: 12px;
                resize: vertical;
                min-height: 80px;
              "
            ></textarea>
          </div>

          <div style="margin-top: 24px; text-align: right">
            <button
              v-if="giamTrucTiepLocal > 0"
              class="btn btn-outline-danger"
              style="
                margin-right: 8px;
                display: inline-flex;
                align-items: center;
                justify-content: center;
                height: 42px;
                padding: 0 20px;
                border-radius: 7px;
                font-weight: 600;
                font-size: 14px;
                cursor: pointer;
                transition: all 0.2s;
                border: 1.5px solid #d73737;
                background: transparent;
                color: #d73737;
              "
              :disabled="loadingApDung"
              @click="huyGiamGiaTrucTiep"
            >
              Hủy giảm giá
            </button>
            <button
              class="btn btn-primary"
              style="
                display: inline-flex;
                align-items: center;
                justify-content: center;
                gap: 8px;
                height: 42px;
                padding: 0 24px;
                border-radius: 7px;
                font-weight: 600;
                font-size: 14px;
                cursor: pointer;
                transition: all 0.2s;
                border: none;
                background: #d73737;
                color: #fff;
              "
              :disabled="
                loadingApDung ||
                !lyDoGiamTrucTiep.trim() ||
                (kieuGiamGiaTrucTiep === 'VNĐ'
                  ? !soTienGiamTrucTiepInput || soTienGiamTrucTiepInput <= 0
                  : !phanTramGiamGiaInput || phanTramGiamGiaInput <= 0)
              "
              @click="xacNhanGiamGiaTrucTiep"
            >
              <div class="btn-light-sweep"></div>
              <i class="bi bi-check2-all" style="font-size: 16px"></i>
              <span>Áp dụng giảm giá</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>

  <Teleport to="body">
    <div
      v-if="showPopupVoucher"
      class="voucher-popup-overlay"
      @click.self="dongPopupVoucher"
    >
      <div class="voucher-popup voucher-list-popup">
        <div class="voucher-popup-header">
          <h3>Danh sách phiếu giảm giá</h3>
          <button
            class="btn btn-icon btn-close-popup"
            @click="dongPopupVoucher"
          >
            ✕
          </button>
        </div>

        <div class="voucher-search-box">
          <div class="search-input-wrapper">
            <i class="ri-search-line"></i>
            <input
              v-model="tuKhoaVoucher"
              type="text"
              class="voucher-search-input has-icon"
              placeholder="Tìm mã, tên phiếu..."
              :disabled="loadingVoucher"
            />
          </div>
        </div>

        <div v-if="loadingVoucher" class="empty-mon">
          Đang tải danh sách phiếu giảm giá...
        </div>
        <div v-else-if="!danhSachVoucherDangHienThi.length" class="empty-mon">
          Không có phiếu giảm giá nào
        </div>

        <div v-else class="voucher-list short-list">
          <div
            v-for="voucher in danhSachVoucherDangHienThi"
            :key="voucher.id || voucher.maGiamGia"
            class="voucher-card-new"
            :class="{
              active: isVoucherDangChon(voucher),
              ineligible: !duDieuKienApDung(voucher),
            }"
            @click="chonVoucher(voucher)"
          >
            <div
              class="voucher-badge-applied"
              v-if="isVoucherDangApDung(voucher)"
            >
              ĐANG ÁP DỤNG
            </div>
            <div
              class="voucher-badge-ineligible"
              v-else-if="!duDieuKienApDung(voucher)"
            >
              CHƯA ĐỦ ĐIỀU KIỆN
            </div>
            <div
              class="voucher-selected-tick"
              v-if="isVoucherDangChon(voucher) && !isVoucherDangApDung(voucher)"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="3"
                stroke-linecap="round"
                stroke-linejoin="round"
                class="tick-svg"
              >
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
            </div>
            <div class="voucher-icon"><i class="ri-coupon-3-fill"></i></div>
            <div class="voucher-content">
              <div class="voucher-title">
                <span class="voucher-code">[{{ voucher.maGiamGia }}]</span>
                {{ voucher.tenGiamGia }}
              </div>
              <div class="voucher-info-row">
                <span
                  >Loại:
                  {{
                    voucher.phuongThucGiamGia === 1 ? "Phần trăm" : "Tiền mặt"
                  }}</span
                >
                <span
                  >Giảm:
                  <strong class="voucher-amount">{{
                    hienThiGiaTriGiam(voucher)
                  }}</strong></span
                >
              </div>
              <div
                style="
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  margin-top: 4px;
                  gap: 8px;
                  flex-wrap: wrap;
                "
              >
                <div
                  class="voucher-info-row text-muted"
                  style="margin-bottom: 0"
                >
                  Tối thiểu: {{ formatTien(voucher.dieuKienSuDung || 0) }}
                </div>
                <button
                  v-if="!duDieuKienApDung(voucher)"
                  type="button"
                  class="btn-an-them"
                  style="padding: 3px 10px; font-size: 11px"
                  @click.stop="diDenChonMon"
                >
                  <i class="ri-add-circle-line" style="font-size: 12px"></i>
                  <span
                    >Ăn thêm:
                    {{
                      formatTien(
                        Math.max(
                          0,
                          Number(voucher.dieuKienSuDung || 0) -
                            Number(tongTienMon || 0),
                        ),
                      )
                    }}</span
                  >
                </button>
              </div>
              <div class="voucher-info-row text-muted" v-if="voucher.giamToiDa">
                Giảm tối đa: {{ formatTien(voucher.giamToiDa) }}
              </div>
              <div class="voucher-info-row text-date">
                HSD: {{ formatDateTime(voucher.ngayBatDau) }} -
                {{ formatDateTime(voucher.ngayKetThuc) }}
              </div>
            </div>
          </div>
        </div>

        <div class="voucher-popup-footer">
          <button
            class="btn btn-outline-danger"
            style="
              display: inline-flex;
              align-items: center;
              justify-content: center;
              height: 42px;
              padding: 0 20px;
              border-radius: 7px;
              font-weight: 600;
              font-size: 14px;
              cursor: pointer;
              transition: all 0.2s;
              border: 1.5px solid #d73737;
              background: transparent;
              color: #d73737;
            "
            @click="dongPopupVoucher"
          >
            Đóng
          </button>
          <button
            class="btn btn-primary"
            style="
              display: inline-flex;
              align-items: center;
              justify-content: center;
              gap: 8px;
              height: 42px;
              padding: 0 24px;
              border-radius: 7px;
              font-weight: 600;
              font-size: 14px;
              cursor: pointer;
              transition: all 0.2s;
              border: none;
              background: #d73737;
              color: #fff;
            "
            :disabled="!voucherDangChonMa"
            @click="xuLyApDungUuDai"
          >
            <div class="btn-light-sweep"></div>
            <i class="bi bi-check2-all" style="font-size: 16px"></i>
            <span>Xác nhận</span>
          </button>
        </div>
      </div>
    </div>
  </Teleport>
  <!-- Modal Đánh Giá -->
  <div v-if="hienModalDanhGia" class="page-wrap rating-wrap">
    <div class="rating-modal">
      <div class="rating-header">
        <div class="header-sparkles"></div>
        <div class="success-icon-badge">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="currentColor"
            class="check-svg"
          >
            <path
              fill-rule="evenodd"
              d="M2.25 12c0-5.385 4.365-9.75 9.75-9.75s9.75 4.365 9.75 9.75-4.365 9.75-9.75 9.75S2.25 17.385 2.25 12zm13.36-1.814a.75.75 0 10-1.22-.872l-3.236 4.53L9.53 12.22a.75.75 0 00-1.06 1.06l2.25 2.25a.75.75 0 001.14-.094l3.748-5.25z"
              clip-rule="evenodd"
            />
          </svg>
        </div>
        <h2 class="rating-title">Cảm ơn bạn đã sử dụng dịch vụ!</h2>
        <p class="rating-subtitle">
          Trải nghiệm của bạn tại FourSpice Hotpot thế nào?
        </p>
      </div>

      <div class="rating-body">
        <!-- Cột trái: Hóa đơn dạng Receipt ziczac -->
        <div class="rating-left">
          <div class="rating-receipt-card">
            <div class="receipt-paid-stamp">ĐÃ THANH TOÁN</div>

            <div class="receipt-header-brand">
              <img class="brand-logo" :src="logo" alt="logo" />
              <h4>FourSpice Hotpot</h4>
              <p>Mã hóa đơn: {{ hoaDon.maHoaDon || "HD-" + idHoaDon }}</p>
            </div>

            <div class="receipt-divider"></div>

            <div class="receipt-info-grid">
              <div class="receipt-info-row">
                <span class="label"><i class="ri-table-line"></i> Bàn ăn</span>
                <span class="value font-highlight">{{
                  hoaDon.tenBanAn || "--"
                }}</span>
              </div>
              <div class="receipt-info-row">
                <span class="label"
                  ><i class="ri-map-pin-line"></i> Khu vực</span
                >
                <span class="value">{{ hoaDon.tenKhuVuc || "--" }}</span>
              </div>
              <div class="receipt-info-row">
                <span class="label"
                  ><i class="ri-user-3-line"></i> Khách hàng</span
                >
                <span class="value">{{
                  hoaDon.tenKhachHang || "Khách lẻ"
                }}</span>
              </div>
              <div class="receipt-info-row">
                <span class="label"
                  ><i class="ri-phone-line"></i> Số điện thoại</span
                >
                <span class="value">{{ hoaDon.soDienThoai || "--" }}</span>
              </div>
              <div class="receipt-info-row">
                <span class="label"
                  ><i class="ri-time-line"></i> Thời gian</span
                >
                <span class="value">{{
                  formatDateTime(hoaDon.thoiGianTao)
                }}</span>
              </div>
            </div>

            <div class="receipt-divider dotted" v-if="diemTichLuySuDung"></div>
            <div
              class="receipt-info-row"
              v-if="diemTichLuySuDung"
              style="
                margin-top: 12px;
                display: flex;
                justify-content: space-between;
                font-size: 14px;
              "
            >
              <span class="label" style="color: #64748b; font-weight: 500"
                ><i class="ri-vip-diamond-line"></i> Điểm sử dụng</span
              >
              <span class="value" style="color: #10b981; font-weight: 600"
                >- {{ formatTien(diemTichLuySuDung) }}</span
              >
            </div>

            <div class="receipt-divider dotted"></div>

            <div class="receipt-total-row">
              <span class="total-label">TIỀN SAU GIẢM</span>
              <span class="total-value">{{ formatTien(tongThanhToan) }}</span>
            </div>

            <!-- Hiệu ứng chân ziczac -->
            <div class="receipt-ziczac-edge"></div>
          </div>
        </div>

        <!-- Cột phải: Form đánh giá Shopee -->
        <div class="rating-right">
          <div class="rating-form-card">
            <h3 class="form-section-title">Đánh giá trải nghiệm</h3>

            <!-- Stars Selection -->
            <div class="stars-container-shopee">
              <div class="stars-row-shopee">
                <button
                  v-for="i in 5"
                  :key="i"
                  type="button"
                  class="star-btn-shopee"
                  :class="{ active: danhGiaForm.soSao >= i }"
                  @click="danhGiaForm.soSao = i"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 24 24"
                    fill="currentColor"
                    class="star-svg"
                  >
                    <path
                      fill-rule="evenodd"
                      d="M10.788 3.21c.448-1.077 1.976-1.077 2.424 0l2.082 5.007 5.404.433c1.164.093 1.636 1.545.749 2.305l-4.117 3.527 1.257 5.273c.271 1.136-.964 2.033-1.96 1.425L12 18.354 7.373 21.18c-.996.608-2.231-.29-1.96-1.425l1.257-5.273-4.117-3.527c-.887-.76-.415-2.212.749-2.305l5.404-.433 2.082-5.006z"
                      clip-rule="evenodd"
                    />
                  </svg>
                </button>
              </div>
              <div class="star-feedback-text" v-if="nhanSao.text">
                {{ nhanSao.text }}
                <i :class="nhanSao.icon"></i>
              </div>
            </div>

            <!-- Quick Review Tags -->
            <div class="quick-tags-section">
              <h4 class="field-title">Nhận xét nhanh</h4>
              <div class="quick-tags-container">
                <button
                  v-for="tag in tagGoiY"
                  :key="tag"
                  type="button"
                  class="quick-tag-badge"
                  :class="{ active: tagsDaChon.includes(tag) }"
                  @click="toggleTag(tag)"
                >
                  {{ tag }}
                </button>
              </div>
            </div>

            <!-- Review Input -->
            <div class="review-textarea-wrapper">
              <h4 class="field-title">Nội dung chi tiết</h4>
              <div class="textarea-relative">
                <textarea
                  v-model="danhGiaForm.noiDung"
                  placeholder="Chia sẻ cảm nhận của bạn về món ăn, dịch vụ..."
                  rows="3"
                  maxlength="200"
                ></textarea>
                <span class="char-count"
                  >{{ danhGiaForm.noiDung.length }}/200</span
                >
              </div>
            </div>

            <!-- Tích điểm thành viên Toggle Switch -->
            <div class="points-toggle-wrapper">
              <div class="points-toggle-info">
                <span class="points-icon">
                  <i class="bi bi-gift-fill"></i>
                </span>
                <div class="points-text-group">
                  <span class="points-title">Tích điểm thành viên</span>
                  <span class="points-sub">
                    Tích lũy
                    {{
                      getThamSo("TI_LE_TICH_DIEM")
                        ? getThamSo("TI_LE_TICH_DIEM") + "%"
                        : "1%"
                    }}
                    tổng giá trị hóa đơn
                  </span>
                </div>
              </div>
              <label class="shopee-switch">
                <input type="checkbox" v-model="danhGiaForm.tichDiem" />
                <span class="shopee-slider"></span>
              </label>
            </div>
          </div>
        </div>
      </div>

      <div class="rating-footer-new">
        <button class="btn-cancel" @click="boQuaDanhGia">Bỏ qua</button>
        <button class="btn-primary" @click="hoanTatDanhGia">
          <i class="bi bi-check2-all"></i>
          <div class="btn-light-sweep"></div>
          Xác nhận
        </button>
      </div>
    </div>
  </div>

  <!-- Modal Ghi nợ -->
  <div
    v-if="hienModalGhiNo"
    style="
      position: fixed;
      inset: 0;
      z-index: 10000;
      background: rgba(15, 23, 42, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
    "
    @click.self="dongModalGhiNo"
  >
    <div
      style="
        background: #fff;
        width: 750px;
        border-radius: 14px;
        padding: 24px;
        box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
      "
    >
      <div
        style="
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 20px;
        "
      >
        <div
          style="
            width: 48px;
            height: 48px;
            background: #fff7ed;
            color: #ea580c;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
          "
        >
          <i class="bi bi-journal-minus"></i>
        </div>
        <div>
          <h3
            style="margin: 0; font-size: 20px; font-weight: 700; color: #1e293b"
          >
            Ghi nợ hóa đơn
          </h3>
          <span style="font-size: 13px; color: #64748b"
            >Bàn sẽ được giải phóng sau khi ghi nợ thành công</span
          >
        </div>
        <button
          @click="dongModalGhiNo"
          style="
            margin-left: auto;
            background: #f1f5f9;
            border: none;
            width: 32px;
            height: 32px;
            border-radius: 8px;
            cursor: pointer;
            font-size: 18px;
            color: #64748b;
            display: flex;
            align-items: center;
            justify-content: center;
          "
        >
          ×
        </button>
      </div>
      <div
        style="
          background: #f8fafc;
          border-radius: 10px;
          padding: 16px;
          font-size: 14px;
          margin-bottom: 20px;
          display: flex;
          flex-direction: column;
          gap: 10px;
          border: 1px solid #e2e8f0;
        "
      >
        <div style="display: flex; justify-content: space-between">
          <span style="color: #64748b">
            <i
              class="bi bi-receipt"
              style="color: #ef4444; margin-right: 6px"
            ></i>
            Mã hóa đơn
          </span>
          <strong style="color: #334155">{{
            hoaDon.maHoaDon || "HD-" + idHoaDon
          }}</strong>
        </div>
        <div style="display: flex; justify-content: space-between">
          <span style="color: #64748b">
            <i
              class="bi bi-person"
              style="color: #ef4444; margin-right: 6px"
            ></i>
            Khách hàng
          </span>
          <strong style="color: #334155">
            {{
              khachHangDaChon
                ? khachHangDaChon.tenKhachHang || khachHangDaChon.hoTen
                : hoaDon.tenKhachHang || "Khách lẻ"
            }}
            <span
              v-if="
                khachHangDaChon
                  ? khachHangDaChon.soDienThoai
                  : hoaDon.soDienThoai
              "
              style="color: #64748b; font-weight: 500"
              >-
              {{
                khachHangDaChon
                  ? khachHangDaChon.soDienThoai
                  : hoaDon.soDienThoai
              }}</span
            >
          </strong>
        </div>
        <div style="display: flex; justify-content: space-between">
          <span style="color: #64748b">
            <i
              class="bi bi-geo-alt"
              style="color: #ef4444; margin-right: 6px"
            ></i>
            Vị trí bàn
          </span>
          <strong style="color: #334155">
            Bàn {{ hoaDon.tenBanAn || "--" }}
            <span
              v-if="hoaDon.tenKhuVuc"
              style="color: #64748b; font-weight: 500"
              >- {{ hoaDon.tenKhuVuc }}</span
            >
          </strong>
        </div>
        <div style="height: 1px; background: #e2e8f0; margin: 4px 0"></div>
        <div
          style="
            display: flex;
            justify-content: space-between;
            align-items: center;
          "
        >
          <span style="color: #64748b; font-weight: 600">
            <i
              class="bi bi-cash-stack"
              style="color: #ef4444; margin-right: 6px"
            ></i>
            Số tiền nợ
          </span>
          <strong style="color: #dc2626; font-size: 18px">{{
            formatTien(tienConLai)
          }}</strong>
        </div>
      </div>
      <!-- ===== CHỌN KHÁCH HÀNG ===== -->

      <!-- ===== THÔNG TIN KHÁCH HÀNG (gộp header + chọn khách) ===== -->
      <div style="margin-top: 20px">
        <div
          style="
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 12px;
            gap: 12px;
          "
        >
          <div style="display: flex; align-items: center; gap: 8px">
            <i
              class="bi bi-person-plus-fill"
              style="color: #ea580c; font-size: 16px"
            ></i>
            <label
              style="
                font-weight: 700;
                font-size: 14px;
                color: #334155;
                margin: 0;
              "
            >
              Thông tin khách hàng
            </label>
          </div>

          <button
            v-if="!khachHangDaChon"
            type="button"
            @click="moModalChonKhachHang"
            style="
              display: inline-flex;
              align-items: center;
              gap: 6px;
              padding: 7px 14px;
              border-radius: 8px;
              background: #eff6ff;
              border: 1px solid #bfdbfe;
              color: #2563eb;
              font-size: 12.5px;
              font-weight: 600;
              cursor: pointer;
              white-space: nowrap;
            "
          >
            <i class="bi bi-person-lines-fill"></i> Chọn khách hàng
          </button>
          <div
            v-else
            style="display: inline-flex; gap: 8px; align-items: center"
          >
            <button
              type="button"
              @click="boChonKhachHang"
              style="
                display: inline-flex;
                align-items: center;
                gap: 6px;
                padding: 6px 12px;
                border-radius: 8px;
                background: #f1f5f9;
                border: 1px solid #cbd5e1;
                color: #475569;
                font-size: 12px;
                font-weight: 600;
                cursor: pointer;
                white-space: nowrap;
              "
            >
              <i class="bi bi-x-circle-fill" style="color: #dc2626"></i> Bỏ chọn
            </button>
            <button
              type="button"
              @click="moModalChonKhachHang"
              style="
                display: inline-flex;
                align-items: center;
                gap: 6px;
                padding: 6px 12px;
                border-radius: 8px;
                background: #eff6ff;
                border: 1px solid #bfdbfe;
                color: #2563eb;
                font-size: 12px;
                font-weight: 600;
                cursor: pointer;
                white-space: nowrap;
              "
            >
              <i class="bi bi-arrow-left-right"></i> Đổi khách khác
            </button>
          </div>
        </div>

        <!-- Badge khách đã chọn -->
        <div
          v-if="khachHangDaChon"
          style="
            background: #f0fdf4;
            border-radius: 10px;
            padding: 12px 16px;
            margin-bottom: 12px;
            border: 1px solid #bbf7d0;
          "
        >
          <div style="display: grid; grid-template-columns: 1fr; gap: 8px">
            <div
              style="
                display: inline-flex;
                align-items: center;
                gap: 6px;
                padding: 6px 12px;
                width: fit-content;
                max-width: 100%;
              "
            >
              <i
                class="bi bi-person-check-fill"
                style="color: #16a34a; font-size: 13px"
              ></i>
              <span style="font-weight: 600; color: #166534; font-size: 12.5px"
                >Họ tên:</span
              >
              <span
                style="
                  font-weight: 700;
                  color: #166534;
                  font-size: 12.5px;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                "
              >
                {{
                  khachHangDaChon.tenKhachHang || khachHangDaChon.hoTen || "--"
                }}
              </span>
            </div>

            <div
              style="
                display: inline-flex;
                align-items: center;
                gap: 6px;
                padding: 6px 12px;
                width: fit-content;
                max-width: 100%;
              "
            >
              <i
                class="bi bi-telephone-fill"
                style="color: #16a34a; font-size: 13px"
              ></i>
              <span style="font-weight: 600; color: #166534; font-size: 12.5px"
                >Số điện thoại:</span
              >
              <span
                style="
                  font-weight: 700;
                  color: #166534;
                  font-size: 12.5px;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                "
              >
                {{ khachHangDaChon.soDienThoai || "--" }}
              </span>
            </div>

            <div
              style="
                display: inline-flex;
                align-items: center;
                gap: 6px;
                padding: 6px 12px;
                width: fit-content;
                max-width: 100%;
              "
            >
              <i
                class="bi bi-cake2-fill"
                style="color: #16a34a; font-size: 13px"
              ></i>
              <span style="font-weight: 600; color: #166534; font-size: 12.5px"
                >Ngày sinh:</span
              >
              <span
                style="
                  font-weight: 700;
                  color: #166534;
                  font-size: 12.5px;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                "
              >
                {{
                  khachHangDaChon.ngaySinh
                    ? new Date(khachHangDaChon.ngaySinh).toLocaleDateString(
                        "vi-VN",
                      )
                    : "--"
                }}
              </span>
            </div>

            <div
              style="
                display: inline-flex;
                align-items: center;
                gap: 6px;
                padding: 6px 12px;
                width: fit-content;
                max-width: 100%;
              "
            >
              <i
                class="bi bi-envelope-fill"
                style="color: #16a34a; font-size: 13px"
              ></i>
              <span style="font-weight: 600; color: #166534; font-size: 12.5px"
                >Email:</span
              >
              <span
                style="
                  font-weight: 700;
                  color: #166534;
                  font-size: 12.5px;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                "
              >
                {{ khachHangDaChon.email || "--" }}
              </span>
            </div>
          </div>
        </div>

        <!-- Form nhập vãng lai: chỉ hiện khi chưa chọn khách quen -->
        <template v-if="laKhachVangLai">
          <div
            style="
              display: grid;
              grid-template-columns: 1fr 1fr;
              gap: 12px;
              margin-bottom: 12px;
            "
          >
            <div>
              <label
                style="
                  display: block;
                  font-size: 13px;
                  font-weight: 600;
                  color: #475569;
                  margin-bottom: 6px;
                "
              >
                Họ tên <span style="color: #dc2626">*</span>
              </label>
              <input
                v-model="khachMoiForm.hoTen"
                @input="ghiNoErrors.hoTen = ''"
                type="text"
                placeholder="Nhập họ tên khách hàng"
                class="custom-input"
                :class="{ 'input-invalid': ghiNoErrors.hoTen }"
              />
              <transition name="shake">
                <p v-if="ghiNoErrors.hoTen" class="error-hint">
                  <i class="bi bi-exclamation-circle"></i>
                  {{ ghiNoErrors.hoTen }}
                </p>
              </transition>
            </div>
            <div>
              <label
                style="
                  display: block;
                  font-size: 13px;
                  font-weight: 600;
                  color: #475569;
                  margin-bottom: 6px;
                "
              >
                Số điện thoại <span style="color: #dc2626">*</span>
              </label>
              <input
                v-model="khachMoiForm.soDienThoai"
                @input="ghiNoErrors.soDienThoai = ''"
                type="text"
                placeholder="Nhập số điện thoại"
                class="custom-input"
                :class="{ 'input-invalid': ghiNoErrors.soDienThoai }"
              />
              <transition name="shake">
                <p v-if="ghiNoErrors.soDienThoai" class="error-hint">
                  <i class="bi bi-exclamation-circle"></i>
                  {{ ghiNoErrors.soDienThoai }}
                </p>
              </transition>
            </div>
          </div>
          <div
            style="
              display: grid;
              grid-template-columns: 1fr 1fr;
              gap: 12px;
              margin-bottom: 12px;
            "
          >
            <div>
              <label
                style="
                  display: block;
                  font-size: 13px;
                  font-weight: 600;
                  color: #475569;
                  margin-bottom: 6px;
                "
              >
                Email <span style="color: #dc2626">*</span>
              </label>
              <input
                v-model="khachMoiForm.email"
                @input="ghiNoErrors.email = ''"
                type="email"
                placeholder="Nhập email"
                class="custom-input"
                :class="{ 'input-invalid': ghiNoErrors.email }"
              />
              <transition name="shake">
                <p v-if="ghiNoErrors.email" class="error-hint">
                  <i class="bi bi-exclamation-circle"></i>
                  {{ ghiNoErrors.email }}
                </p>
              </transition>
            </div>
            <div>
              <label
                style="
                  display: block;
                  font-size: 13px;
                  font-weight: 600;
                  color: #475569;
                  margin-bottom: 6px;
                "
              >
                Ngày sinh <span style="color: #dc2626">*</span>
              </label>
              <input
                v-model="khachMoiForm.ngaySinh"
                @input="ghiNoErrors.ngaySinh = ''"
                type="date"
                class="custom-input"
                :class="{ 'input-invalid': ghiNoErrors.ngaySinh }"
              />
              <transition name="shake">
                <p v-if="ghiNoErrors.ngaySinh" class="error-hint">
                  <i class="bi bi-exclamation-circle"></i>
                  {{ ghiNoErrors.ngaySinh }}
                </p>
              </transition>
            </div>
          </div>

          <div
            style="
              display: grid;
              grid-template-columns: 1fr 1fr;
              gap: 12px;
              margin-bottom: 12px;
              align-items: start;
            "
          >
            <div>
              <label
                style="
                  display: block;
                  font-size: 13px;
                  font-weight: 600;
                  color: #475569;
                  margin-bottom: 6px;
                "
              >
                Ảnh khách hàng <span style="color: #dc2626">*</span>
              </label>
              <div style="display: flex; align-items: center; gap: 12px">
                <div
                  style="
                    width: 72px;
                    height: 72px;
                    border-radius: 10px;
                    border: 1px dashed #cbd5e1;
                    background: #f8fafc;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    overflow: hidden;
                    flex-shrink: 0;
                  "
                >
                  <img
                    v-if="khachMoiForm.anhPreview"
                    :src="khachMoiForm.anhPreview"
                    style="width: 100%; height: 100%; object-fit: cover"
                  />
                  <i
                    v-else
                    class="bi bi-image"
                    style="font-size: 24px; color: #94a3b8"
                  ></i>
                </div>

                <div style="display: flex; flex-direction: column; gap: 6px">
                  <label
                    style="
                      display: inline-flex;
                      align-items: center;
                      gap: 6px;
                      padding: 8px 14px;
                      border-radius: 8px;
                      background: #f1f5f9;
                      border: 1px solid #e2e8f0;
                      color: #334155;
                      font-size: 13px;
                      font-weight: 600;
                      cursor: pointer;
                      width: fit-content;
                    "
                  >
                    <i class="bi bi-upload"></i> Chọn ảnh
                    <input
                      type="file"
                      accept="image/*"
                      @change="chonAnhKhachHang"
                      style="display: none"
                    />
                  </label>
                  <button
                    v-if="khachMoiForm.anhPreview"
                    type="button"
                    @click="xoaAnhKhachHang"
                    style="
                      border: none;
                      background: none;
                      color: #dc2626;
                      font-size: 12px;
                      cursor: pointer;
                      text-align: left;
                      padding: 0;
                    "
                  >
                    Xóa ảnh
                  </button>
                </div>
              </div>
              <transition name="shake">
                <p v-if="ghiNoErrors.anhFile" class="error-hint mt-1">
                  <i class="bi bi-exclamation-circle"></i>
                  {{ ghiNoErrors.anhFile }}
                </p>
              </transition>
            </div>

            <div>
              <label
                style="
                  display: block;
                  font-size: 13px;
                  font-weight: 600;
                  color: #475569;
                  margin-bottom: 6px;
                "
              >
                Giới tính <span style="color: #dc2626">*</span>
              </label>
              <div
                style="
                  display: flex;
                  align-items: center;
                  gap: 16px;
                  height: 40px;
                "
              >
                <label
                  style="
                    display: flex;
                    align-items: center;
                    gap: 6px;
                    cursor: pointer;
                    font-size: 14px;
                    color: #334155;
                  "
                >
                  <input
                    type="radio"
                    value="Nam"
                    v-model="khachMoiForm.gioiTinh"
                  />
                  Nam
                </label>
                <label
                  style="
                    display: flex;
                    align-items: center;
                    gap: 6px;
                    cursor: pointer;
                    font-size: 14px;
                    color: #334155;
                  "
                >
                  <input
                    type="radio"
                    value="Nữ"
                    v-model="khachMoiForm.gioiTinh"
                  />
                  Nữ
                </label>
              </div>
            </div>
          </div>
        </template>
      </div>
      <label
        style="
          display: block;
          font-weight: 600;
          font-size: 14px;
          margin-bottom: 8px;
          color: #334155;
        "
        >Lý do ghi nợ <span style="color: #dc2626">*</span></label
      >

      <textarea
        v-model="ghiNoForm.lyDo"
        @input="ghiNoErrors.lyDo = ''"
        placeholder="VD: Khách hàng thân thiết hẹn ngày mai quay lại thanh toán..."
        class="custom-input textarea"
        :class="{ 'input-invalid': ghiNoErrors.lyDo }"
      ></textarea>
      <transition name="shake">
        <p v-if="ghiNoErrors.lyDo" class="error-hint">
          <i class="bi bi-exclamation-circle"></i>
          {{ ghiNoErrors.lyDo }}
        </p>
      </transition>

      <div
        style="
          display: flex;
          justify-content: flex-end;
          gap: 12px;
          margin-top: 24px;
        "
      >
        <button
          class="btn-cancel"
          @click="dongModalGhiNo"
          style="padding: 10px 24px; min-width: 100px"
        >
          Hủy
        </button>
        <button
          class="btn-primary"
          @click="xacNhanGhiNo"
          :disabled="dangXuLyGhiNo"
          style="
            padding: 10px 24px;
            min-width: 140px;
            border-color: #ea580c;
            box-shadow: 0 4px 12px rgba(234, 88, 12, 0.2);
          "
        >
          <i class="bi bi-journal-minus" style="margin-right: 6px"></i>
          <div class="btn-light-sweep"></div>
          {{ dangXuLyGhiNo ? "Đang xử lý..." : "Xác nhận" }}
        </button>
      </div>
    </div>
  </div>

  <!-- Modal QR Thanh Toán -->
  <div
    v-if="hienModalQR"
    style="
      position: fixed;
      inset: 0;
      z-index: 10000;
      background: rgba(15, 23, 42, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
    "
    @click.self="dongModalQR"
  >
    <div
      style="
        background: #fff;
        width: 400px;
        border-radius: 14px;
        padding: 24px;
        box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
        display: flex;
        flex-direction: column;
        align-items: center;
        text-align: center;
      "
    >
      <h3
        style="
          margin: 0 0 8px 0;
          font-size: 20px;
          font-weight: 700;
          color: #1e293b;
        "
      >
        Mã QR Thanh Toán
      </h3>
      <p style="margin: 0 0 16px 0; font-size: 14px; color: #64748b">
        Vui lòng quét mã QR bên dưới để thanh toán
      </p>

      <div
        style="
          background: #f8fafc;
          border-radius: 10px;
          padding: 16px;
          font-size: 14px;
          margin-bottom: 20px;
          display: flex;
          flex-direction: column;
          gap: 10px;
          border: 1px solid #e2e8f0;
          text-align: left;
          width: 100%;
        "
      >
        <div style="display: flex; justify-content: space-between">
          <span style="color: #64748b">Mã hóa đơn</span>
          <strong style="color: #334155">{{
            hoaDon.maHoaDon || "HD-" + idHoaDon
          }}</strong>
        </div>
        <div style="display: flex; justify-content: space-between">
          <span style="color: #64748b">Khách hàng</span>
          <strong style="color: #334155">
            {{ hoaDon.tenKhachHang || "Khách lẻ" }}
            <span
              v-if="hoaDon.soDienThoai"
              style="color: #64748b; font-weight: 500"
              >- {{ hoaDon.soDienThoai }}</span
            >
          </strong>
        </div>
        <div style="display: flex; justify-content: space-between">
          <span style="color: #64748b">Bàn</span>
          <strong style="color: #334155">
            Bàn {{ hoaDon.tenBanAn || "--" }}
            <span
              v-if="hoaDon.tenKhuVuc"
              style="color: #64748b; font-weight: 500"
              >- {{ hoaDon.tenKhuVuc }}</span
            >
          </strong>
        </div>
        <div style="height: 1px; background: #e2e8f0; margin: 4px 0"></div>
        <div
          style="
            display: flex;
            justify-content: space-between;
            align-items: center;
          "
        >
          <span style="color: #64748b; font-weight: 600"
            >Số tiền cần chuyển</span
          >
          <strong style="color: #dc2626; font-size: 18px">{{
            formatTien(payloadChuyenKhoan?.soTien || 0)
          }}</strong>
        </div>
      </div>

      <div
        style="
          background: #fff;
          border: 1px solid #e2e8f0;
          border-radius: 12px;
          padding: 16px;
          margin-bottom: 24px;
          display: flex;
          justify-content: center;
          width: 100%;
        "
      >
        <img
          v-if="qrCodeUrl"
          :src="qrCodeUrl"
          alt="Mã QR"
          style="
            width: 250px;
            height: 250px;
            object-fit: contain;
            border-radius: 8px;
          "
        />
        <div
          v-else
          style="
            width: 250px;
            height: 250px;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #94a3b8;
          "
        >
          Đang tải QR...
        </div>
      </div>

      <div style="display: flex; gap: 12px; width: 100%">
        <button
          class="btn-cancel"
          @click="dongModalQR"
          style="flex: 1; padding: 12px 0"
        >
          Đóng
        </button>
        <button
          class="btn-primary"
          @click="xacNhanDaThanhToanQR"
          style="flex: 1; padding: 12px 0"
        >
          <i class="bi bi-check-circle me-1"></i>
          Xác nhận
        </button>
      </div>
    </div>
  </div>
  <!-- Modal Chọn Khách Hàng -->
  <div
    v-if="hienModalChonKhachHang"
    style="
      position: fixed;
      inset: 0;
      z-index: 10001;
      background: rgba(15, 23, 42, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
    "
    @click.self="dongModalChonKhachHang"
  >
    <div
      style="
        background: #fff;
        width: 1000px;
        max-height: 100vh;
        border-radius: 14px;
        padding: 24px;
        box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
        display: flex;
        flex-direction: column;
      "
    >
      <div style="display: flex; align-items: center; margin-bottom: 16px">
        <h3
          style="margin: 0; font-size: 18px; font-weight: 700; color: #1e293b"
        >
          <i
            class="bi bi-person-lines-fill"
            style="color: #f87171; margin-right: 8px"
          ></i>
          Chọn khách hàng
        </h3>
        <button
          @click="dongModalChonKhachHang"
          style="
            margin-left: auto;
            background: #f1f5f9;
            border: none;
            width: 32px;
            height: 32px;
            border-radius: 8px;
            cursor: pointer;
            font-size: 18px;
            color: #64748b;
          "
        >
          ×
        </button>
      </div>

      <input
        v-model="tuKhoaTimKhachHang"
        type="text"
        placeholder="Tìm theo tên, số điện thoại..."
        class="custom-input"
        style="margin-bottom: 14px"
      />

      <div style="overflow-y: auto; flex: 1; min-height: 200px">
        <table
          style="width: 100%; border-collapse: collapse; font-size: 13.5px"
        >
          <thead>
            <tr style="background: #f8fafc; text-align: left">
              <th style="padding: 10px 8px; width: 44px; text-align: center">
                STT
              </th>
              <th style="padding: 10px 8px">Mã</th>
              <th style="padding: 10px 8px">Tên</th>
              <th style="padding: 10px 8px">SĐT</th>
              <th style="padding: 10px 8px">Email</th>
              <th style="padding: 10px 8px">Ngày sinh</th>
              <th style="padding: 10px 12px; width: 90px; text-align: center">
                Thao tác
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="dangTaiKhachHang">
              <td
                colspan="7"
                style="text-align: center; padding: 24px; color: #94a3b8"
              >
                Đang tải...
              </td>
            </tr>
            <tr v-else-if="!dsKhachHangPhanTrang.length">
              <td
                colspan="7"
                style="text-align: center; padding: 24px; color: #94a3b8"
              >
                Không tìm thấy khách hàng
              </td>
            </tr>
            <tr
              v-for="(kh, index) in dsKhachHangPhanTrang"
              :key="kh.id"
              style="border-bottom: 1px solid #f1f5f9"
            >
              <td style="padding: 10px 8px; text-align: center; color: #64748b">
                {{ (trangKhachHang - 1) * pageSizeKhachHang + index + 1 }}
              </td>
              <td style="padding: 10px 8px; font-weight: 600; color: #1e293b">
                {{ kh.maKhachHang || kh.ma || "--" }}
              </td>
              <td style="padding: 10px 8px; font-weight: 600; color: #1e293b">
                {{ kh.tenKhachHang || kh.hoTen }}
              </td>
              <td style="padding: 10px 8px">{{ kh.soDienThoai || "--" }}</td>
              <td style="padding: 10px 8px">{{ kh.email || "--" }}</td>
              <td style="padding: 10px 8px">
                {{
                  kh.ngaySinh
                    ? new Date(kh.ngaySinh).toLocaleDateString("vi-VN")
                    : "--"
                }}
              </td>
              <td style="padding: 10px 8px; text-align: center">
                <button
                  type="button"
                  @click="chonKhachHangNay(kh)"
                  style="
                    width: 30px;
                    height: 30px;
                    border-radius: 7px;
                    border: 1px solid #bbf7d0;
                    background: #f0fdf4;
                    color: #16a34a;
                    cursor: pointer;
                    font-size: 15px;
                  "
                  title="Chọn khách hàng này"
                >
                  <i class="bi bi-plus-lg"></i>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div
        class="pagination"
        style="margin-top: 14px"
        v-if="tongSoTrangKhachHang > 1"
      >
        <button
          :disabled="trangKhachHang === 1"
          @click="trangKhachHang--"
          class="pg-btn pg-arrow"
        >
          <i class="ri-arrow-left-s-line"></i>
        </button>
        <template v-for="p in pageListKhachHang" :key="p">
          <button v-if="p === '…'" class="pg-btn pg-ellipsis" disabled>
            ···
          </button>
          <button
            v-else
            class="pg-btn"
            :class="{ active: p === trangKhachHang }"
            @click="trangKhachHang = p"
          >
            {{ p }}
          </button>
        </template>
        <button
          :disabled="trangKhachHang === tongSoTrangKhachHang"
          @click="trangKhachHang++"
          class="pg-btn pg-arrow"
        >
          <i class="ri-arrow-right-s-line"></i>
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
  margin-top: auto;
  position: sticky;
  bottom: -20px;
  margin-left: -20px;
  margin-right: -20px;
  padding: 16px 20px 20px;
  border-top: 1px solid #e2e8f0;
  background: #fff;
  z-index: 10;
}
.footer-actions .btn-cancel {
  width: 100%;
}
.footer-actions .btn-primary {
  grid-column: 1 / -1;
  width: 100%;
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
.btn-primary:hover .btn-light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
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

:deep(body.dark-mode) .footer-actions {
  background: #181818 !important;
  border-top-color: #2a2a2a !important;
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
    grid-template-columns: 1fr;
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

/* INPUT STYLES FOR CHECKIN MODALS */
.custom-input {
  height: 42px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 14px;
  outline: none;
  transition: all 0.2s;
  width: 100%;
  font-family: inherit;
  color: #334155;
  background-color: #fff;
}
.custom-input:hover {
  border-color: #94a3b8;
}
.custom-input:focus {
  border-color: #ea580c;
  box-shadow: 0 0 0 4px rgba(234, 88, 12, 0.1);
}
.custom-input.textarea {
  height: auto;
  padding: 12px;
  min-height: 100px;
  resize: vertical;
}
.input-invalid {
  border-color: #dc2626 !important;
  box-shadow: 0 0 0 4px rgba(220, 38, 38, 0.1) !important;
}
.error-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #dc2626;
  font-size: 12.5px;
  margin-top: 6px;
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

.section-divider {
  display: flex;
  align-items: center;
  margin: 40px 0 28px;
  gap: 16px;
}

.section-divider .divider-line {
  flex: 1;
  height: 1px;
  background: #e2e8f0;
}

.section-divider .divider-text {
  font-size: 13px;
  font-weight: 700;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  white-space: nowrap;
}

:deep(body.dark-mode) .section-divider .divider-line {
  background: #2a2a2a;
}

:deep(body.dark-mode) .section-divider .divider-text {
  color: #a3a3a3;
}

.uu-dai-box {
  background: rgba(255, 255, 255, 0.96);
  border-radius: 10px;
  padding: 24px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03);
  margin-bottom: 24px;
}
.uu-dai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}
.uu-dai-title {
  font-weight: 800;
  color: var(--color-text);
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.current-voucher-clickable {
  cursor: pointer;
  transition: var(--transition);
}
.current-voucher-clickable:hover {
  transform: translateY(-1px);
}
.badge-applied {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  color: #16a34a;
  padding: 6px 12px;
  border-radius: 999px;
  font-weight: 800;
  font-size: 14px;
}
.badge-not-applied {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  color: #64748b;
  padding: 6px 12px;
  border-radius: 999px;
  font-weight: 700;
  font-size: 14px;
}
.voucher-popup-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(5px);
  z-index: 4000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.voucher-popup {
  width: 100%;
  max-width: 760px;
  max-height: 85vh;
  overflow-y: auto;
  background: #fff;
  border-radius: 7px;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.18);
  padding: 18px;
  border: 1px solid rgba(0, 0, 0, 0.06);
}
.voucher-popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.voucher-popup-header h3 {
  margin: 0;
  font-size: 20px;
  color: var(--color-text);
  font-weight: 800;
}
.voucher-search-box {
  margin-bottom: 10px;
}
.search-input-wrapper {
  position: relative;
}
.search-input-wrapper i {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
}
.voucher-search-input {
  width: 100%;
  padding: 11px 12px;
  border: 1px solid #d1d5db;
  border-radius: 7px;
  font-size: 14px;
  color: #1e293b;
  font-weight: 500;
}
.voucher-search-input.has-icon {
  padding-left: 38px;
}
.voucher-search-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12);
}
.voucher-popup-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
  padding-bottom: 18px;
}
.short-list {
  max-height: 360px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.voucher-card-new {
  position: relative;
  display: flex;
  flex-shrink: 0;
  align-items: flex-start;
  gap: 12px;
  background-color: #fffcf8;
  border: 1px solid #fed7aa;
  border-bottom: 2px solid #f97316;
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  overflow: hidden;
}
.voucher-card-new:hover {
  background-color: #fff7ed;
  border-color: #fdba74;
}
.voucher-card-new.active {
  border-color: #f97316;
  background-color: #fff7ed;
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.15);
}
.voucher-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #f97316;
  padding-top: 2px;
}
.voucher-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.voucher-title {
  font-size: 14.5px;
  font-weight: 600;
  color: #c2410c;
  margin-bottom: 2px;
}
.voucher-code {
  font-weight: 700;
}
.voucher-info-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #6b7280;
}
.voucher-amount {
  color: #ea580c;
  font-weight: 700;
  font-size: 14px;
}
.text-muted {
  color: #6b7280;
}
.text-date {
  color: #9ca3af;
  font-size: 12px;
  margin-top: 2px;
}
.voucher-badge-applied {
  position: absolute;
  top: 0;
  right: 0;
  background-color: #f97316;
  color: white;
  font-size: 11px;
  font-weight: bold;
  padding: 3px 8px;
  border-bottom-left-radius: 6px;
  z-index: 1;
  transition: right 0.2s ease;
}
.voucher-selected-tick {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 20px;
  height: 20px;
  background-color: #ef4444;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 5px rgba(239, 68, 68, 0.4);
  z-index: 10;
  animation: scaleUp 0.15s ease-out;
}
.tick-svg {
  width: 11px;
  height: 11px;
}
@keyframes scaleUp {
  from {
    transform: scale(0.7);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}
.discount-type-tabs {
  display: flex;
  background: #f1f5f9;
  border-radius: 8px;
  padding: 4px;
  margin-top: 16px;
  gap: 4px;
}
.discount-tab {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  color: #64748b;
  transition: all 0.2s ease;
  user-select: none;
}
.discount-tab:hover {
  color: var(--color-primary);
}
.discount-tab.active {
  background: #fff;
  color: var(--color-primary);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}
.quick-btn-list {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.quick-btn {
  border: 1px solid var(--color-border);
  background: var(--color-white);
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text);
  transition: all 0.2s ease;
  cursor: pointer;
}
.quick-btn:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}
.quick-btn.btn-max {
  border: 1px solid var(--color-primary);
  background: var(--color-primary-light, #eff6ff);
  color: var(--color-primary);
}
.quick-btn.btn-max:hover {
  background: #dbeafe;
}
.diem-tich-luy-card {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 200px;
}
.diem-tich-luy-input-v2 {
  flex: 1;
  min-width: 0;
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  padding: 9px 12px;
  font-size: 13.5px;
  font-weight: 700;
  text-align: right;
  color: #0f172a;
  background: #f8fafc;
  transition: all 0.2s;
}
.diem-tich-luy-input-v2:focus {
  outline: none;
  border-color: #d73737;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(215, 55, 55, 0.1);
}
.diem-tich-luy-input-v2:disabled {
  color: #94a3b8;
  cursor: not-allowed;
}
.diem-tich-luy-btn-v2 {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: none;
  background: linear-gradient(135deg, #d73737, #b92b2b);
  color: #fff;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 3px 8px rgba(215, 55, 55, 0.25);
}
.diem-tich-luy-btn-v2:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 5px 12px rgba(215, 55, 55, 0.35);
}
.diem-tich-luy-btn-v2:disabled {
  background: #d1d5db;
  box-shadow: none;
  cursor: not-allowed;
}

.giam-truc-tiep-popup {
  max-width: 880px !important;
  width: 90%;
  padding: 28px !important;
}

:deep(body.dark-mode) .giam-truc-tiep-popup {
  background: #181818;
  border-color: #2a2a2a;
  color: #e5e5e5;
}

.btn-an-them {
  background: #fff5f5;
  color: #d73737;
  border: 1px solid #fecaca;
  padding: 5px 12px;
  border-radius: 6px;
  font-weight: 700;
  font-size: 12px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  transition: all 0.2s ease;
  line-height: 1;
}

.btn-an-them:hover {
  background: #fee2e2;
  border-color: #fca5a5;
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(215, 55, 55, 0.1);
}

:deep(body.dark-mode) .btn-an-them {
  background: rgba(215, 55, 55, 0.1);
  color: #ff8a8a;
  border-color: rgba(215, 55, 55, 0.25);
}

:deep(body.dark-mode) .btn-an-them:hover {
  background: rgba(215, 55, 55, 0.2);
  border-color: rgba(215, 55, 55, 0.4);
}

.voucher-list-popup {
  max-width: 900px !important;
  width: 95%;
  max-height: 90vh !important;
  display: flex;
  flex-direction: column;
  padding: 24px !important;
}

.voucher-list-popup .voucher-list {
  max-height: 55vh !important;
  overflow-y: auto;
}

.voucher-card-new.ineligible {
  background-color: #f8fafc;
  border-color: #cbd5e1;
  border-bottom: 2px solid #94a3b8;
  cursor: default;
  opacity: 0.85;
}

.voucher-card-new.ineligible:hover {
  background-color: #f1f5f9;
}

.voucher-card-new.ineligible .voucher-icon {
  color: #94a3b8;
}

.voucher-card-new.ineligible .voucher-title {
  color: #475569;
}

.voucher-badge-ineligible {
  position: absolute;
  top: 0;
  right: 0;
  background-color: #94a3b8;
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  padding: 3px 8px;
  border-bottom-left-radius: 6px;
  text-transform: uppercase;
}

:deep(body.dark-mode) .voucher-card-new.ineligible {
  background-color: #1e1e1e;
  border-color: #333;
  border-bottom: 2px solid #555;
}

:deep(body.dark-mode) .voucher-card-new.ineligible .voucher-title {
  color: #a3a3a3;
}
</style>
