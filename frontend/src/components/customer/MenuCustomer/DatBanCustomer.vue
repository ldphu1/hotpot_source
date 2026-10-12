<script setup>
import { onUnmounted, ref } from "vue";
import { computed } from "vue";
import { onMounted } from "vue";
import { watch } from "vue";
import { onBeforeUnmount, nextTick } from "vue";
import { useRouter } from "vue-router";
import flatpickr from "flatpickr";
import "flatpickr/dist/flatpickr.min.css";
import { Vietnamese } from "flatpickr/dist/l10n/vn.js";
import { connectWebSocket, disconnectWebSocket } from "@/utils/websocket";
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";
import { useUtils } from "@/utils/helper";
import {
  getAllLoaiMonAn,
  getAllSanPhamDotGiamGia,
} from "@/api/customer/thucdon/thucDonCustomerApi";
import { datBanOnline } from "@/api/customer/datbanonline/datBanOnlineApi";
import { getAllBan } from "@/api/customer/datbanonline/datBanOnlineApi";
import { getAllThamSoCustomer } from "@/api/customer/datbanonline/datBanOnlineApi";
import { getAllLoaiLau } from "@/api/customer/thucdon/thucDonCustomerApi";
/////////////////////////////////////////
const isReady = ref(false);
const router = useRouter();
const { notify, nprogress } = useUtils();
const hasProductChanged = ref(false);
const hasDiscountChanged = ref(false);
////////////// ngày giờ //////////////

const datetimeRef = ref(null);
let dateTimePickerInstance = null;
const showMenuModal = ref(false);
const isChecking = ref(false);

const thongTinDatBan = ref(null);
const loadThongTinKhachHang = () => {
  const data = localStorage.getItem("thong-tin-khach-hang");
  if (data) {
    try {
      const parsed = JSON.parse(data);
      thongTinDatBan.value = parsed;
      return {
        tenKhachHang: parsed.tenKhachHang || "",
        soDienThoai: parsed.soDienThoai || "",
        email: parsed.email || "",
        thoiGianDat: parsed.thoiGianDat ? new Date(parsed.thoiGianDat) : "",
        soNguoiToiDa: parsed.soNguoiToiDa || "",
        ghiChu: parsed.ghiChu || "",
        monDatTruoc: parsed.monDatTruoc || [],
      };
    } catch (e) {
      console.error("Lỗi parse thong-tin-khach-hang", e);
    }
  }
  return {
    tenKhachHang: "",
    soDienThoai: "",
    email: "",
    thoiGianDat: "",
    soNguoiToiDa: "",
    ghiChu: "",
    monDatTruoc: [],
  };
};

const formDatBan = ref(loadThongTinKhachHang());

///////////////////// NÚT THANH TOANS //////////////////
const showPaymentMethods = ref(false);
const paymentType = ref(null);
// const handleClick = () => {
//   if (!validateForm()) return;
//   if (!canDeposit.value) {
//     datBan();
//     return;
//   }
//   if (!showPaymentMethods.value) {
//     showPaymentMethods.value = true;
//     return;
//   }
//   if (!paymentType.value) {
//     notify.error("Vui lòng chọn phương thức thanh toán");
//     return;
//   }

//   datBan();
// };
const handleClick = () => {
  if (!validateForm()) return;
  datBan();
};
////////////////////// THỰC ĐƠN CHỌN TRƯỚC //////////////////////
const tabHienTai = ref("chinh");
const danhSachNuocLau = ref([]);
const nuocLauDuocChon = ref(null);

const loadNuocLau = async () => {
  try {
    const res = await getAllLoaiLau();
    danhSachNuocLau.value = res.data;
  } catch (error) {
    console.log(error);
  }
};

const danhSachMonDaLoc = computed(() => {
  let list = [];
  if (tabHienTai.value === "chinh") {
    list = [...danhSachSetLau.value];
  } else {
    list = [...danhSachSanPham.value];
  }

  // 1. Lọc theo từ khóa tìm kiếm
  if (tuKhoaTimKiem.value && tuKhoaTimKiem.value.trim() !== "") {
    const kw = tuKhoaTimKiem.value.trim().toLowerCase();
    list = list.filter((item) =>
      (item.tenSanPham || "").toLowerCase().includes(kw)
    );
  }

  // 2. Lọc theo danh mục (chỉ áp dụng khi tab là món phụ)
  if (tabHienTai.value === "phu" && danhMucDuocChon.value !== null) {
    list = list.filter((item) => item.idDanhMuc === danhMucDuocChon.value);
  }

  // 3. Lọc theo loại lẩu (chỉ áp dụng khi tab là món chính)
  if (tabHienTai.value === "chinh" && nuocLauDuocChon.value !== null) {
    list = list.filter((item) => {
      const itemNuocLauId = item.idNuocLau ?? item.nuocLau?.id ?? item.nuocLauId;
      return itemNuocLauId === nuocLauDuocChon.value;
    });
  }

  // 4. Sắp xếp theo giá
  if (sapXepGia.value === "asc") {
    list.sort((a, b) => {
      const priceA = a.giaSauGiam && a.giaSauGiam < a.gia ? a.giaSauGiam : a.gia;
      const priceB = b.giaSauGiam && b.giaSauGiam < b.gia ? b.giaSauGiam : b.gia;
      return priceA - priceB;
    });
  } else if (sapXepGia.value === "desc") {
    list.sort((a, b) => {
      const priceA = a.giaSauGiam && a.giaSauGiam < a.gia ? a.giaSauGiam : a.gia;
      const priceB = b.giaSauGiam && b.giaSauGiam < b.gia ? b.giaSauGiam : b.gia;
      return priceB - priceA;
    });
  }

  return list;
});

const changeTab = async (tab) => {
  tabHienTai.value = tab;

  tuKhoaTimKiem.value = "";
  danhMucDuocChon.value = null;
  sapXepGia.value = "default";

  await searchMon();
};

// Tìm kiếm
const searchMon = async () => {
  try {
    if (tabHienTai.value === "chinh") {
      const res = await getAllSanPhamDotGiamGia(true);
      danhSachSetLau.value = res.data;
    } else {
      const res = await getAllSanPhamDotGiamGia(false);
      danhSachSanPham.value = res.data;
    }
  } catch (error) {
    console.error("Lỗi search:", error);
  }
};

/////////////////// THAM SỐ ĐỘNG ///////////////

const danhSachThamSo = ref([]);
const loadThamSo = async () => {
  try {
    const res = await getAllThamSoCustomer();
    danhSachThamSo.value = res.data;
  } catch (error) {
    console.log("Lỗi load tham số", error);
  }
};

const getThamSo = (ma) => {
  const item = danhSachThamSo.value.find((t) => t.maThamSo === ma);
  return item ? parseFloat(item.giaTri) : 0;
};
/////////////////////////////// CÁC BIẾN //////////////////

const tuKhoaTimKiem = ref("");
const sapXepGia = ref("default");
const ngayDat = ref("");
const gioDat = ref("");
const cart = ref([]);
const danhSachSanPham = ref([]);
const danhSachDanhMuc = ref([]);
const danhSachSetLau = ref([]);
const danhMucDuocChon = ref(null);
const hienThiThucDon = ref(false);

const isInit = ref(true);

watch([tuKhoaTimKiem, danhMucDuocChon, sapXepGia], () => {
  if (isInit.value) return;
  searchMon();
});

const loadDanhMuc = async () => {
  const res = await getAllLoaiMonAn();
  danhSachDanhMuc.value = res.data;
};
const refreshDanhMucList = async () => {
  try {
    const res = await getAllLoaiMonAn();
    const newList = res.data;
    danhSachDanhMuc.value = newList;

    const stillExists = newList.find((dm) => dm.id === danhMucDuocChon.value);
    if (!stillExists) {
      danhMucDuocChon.value = null;
    }
  } catch (err) {
    console.error("Lỗi refresh danh mục", err);
  }
};

let wsTimeout = null;
let pendingSanPhamUpdate = false;
let pendingDotGiamGiaUpdate = false;

const triggerDebouncedWsUpdate = () => {
  if (wsTimeout) clearTimeout(wsTimeout);
  wsTimeout = setTimeout(() => {
    processWsUpdates();
  }, 150);
};

const processWsUpdates = () => {
  searchMon();
  capNhatTrangThaiGioHang();
  
  if (cart.value && cart.value.length > 0) {
    if (pendingDotGiamGiaUpdate) {
      hasDiscountChanged.value = true;
      hasProductChanged.value = false;
      notify.confirm("Sản phẩm đã có sự thay đổi do đợt giảm giá thay đổi vui lòng xem lại thông tin sản phẩm trước khi đặt bàn!");
    } else if (pendingSanPhamUpdate) {
      hasProductChanged.value = true;
      hasDiscountChanged.value = false;
      notify.confirm("Sản phẩm đã có thay đổi, vui lòng xem lại thông tin trước khi đặt bàn!");
    }
  }
  
  pendingSanPhamUpdate = false;
  pendingDotGiamGiaUpdate = false;
  wsTimeout = null;
};

const handleWsMessage = (data) => {
  if (data?.type === "SAN_PHAM_UPDATE") {
    pendingSanPhamUpdate = true;
    triggerDebouncedWsUpdate();
  } else if (data?.type === "DOT_GIAM_GIA_UPDATE") {
    pendingDotGiamGiaUpdate = true;
    triggerDebouncedWsUpdate();
  } else if (data && data.tenDanhMuc !== undefined) {
    refreshDanhMucList();
  }
};

const loadSanPhamDiscount = async () => {
  const res = await getAllSanPhamDotGiamGia(false); // món lẻ
  danhSachSanPham.value = res.data;
};
const loadSetLauDiscount = async () => {
  const res = await getAllSanPhamDotGiamGia(true); // set lẩu
  danhSachSetLau.value = res.data;
};
const loadMenuData = async () => {
  try {
    await Promise.all([
      loadDanhMuc(),
      loadSanPhamDiscount(),
      loadSetLauDiscount(),
    ]);
  } catch (err) {
    console.error(err);
  }
};
const setupRevealAnimation = () => {
  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add("active");
        }
      });
    },
    { threshold: 0.1 },
  );

  document.querySelectorAll(".reveal").forEach((el) => {
    observer.observe(el);
  });
};
// Đã gộp logic loadMenuData và setupRevealAnimation xuống onMounted duy nhất ở cuối script block

const capNhatTrangThaiGioHang = async () => {
  if (!cart.value.length) return;
  try {
    const [resSet, resMon] = await Promise.all([
      getAllSanPhamDotGiamGia(true),
      getAllSanPhamDotGiamGia(false),
    ]);
    const allActiveProducts = new Map(
      [...(resSet.data || []), ...(resMon.data || [])].map((p) => [p.id, p]),
    );

    cart.value.forEach((item) => {
      const product = allActiveProducts.get(item.id);
      item.trangThaiXoa = !product;
      if (product) {
        item.price =
          product.giaSauGiam && product.giaSauGiam < product.gia
            ? product.giaSauGiam
            : product.gia;
      }
    });
  } catch (err) {
    console.error("Lỗi đồng bộ trạng thái giỏ hàng", err);
  }
};

const coMonNgungKinhDoanh = computed(() => {
  return cart.value?.some((i) => i.trangThaiXoa) || false;
});

// xử lý chọn món
const addtocart = (item) => {
  const danhMuc = item.tenDanhMuc?.trim().toLowerCase();

  const finalPrice =
    item.giaSauGiam && item.giaSauGiam < item.gia ? item.giaSauGiam : item.gia;

  const getTong = (listDanhMuc) => {
    return cart.value
      .filter((i) =>
        listDanhMuc.includes((i.tenDanhMuc || "").trim().toLowerCase()),
      )
      .reduce((sum, i) => sum + i.quantity, 0);
  };

  const tongLau = getTong(["lẩu"]);
  const tongMonPhu = getTong(["đồ nhúng lẩu", "đồ ăn kèm"]);
  const tongDoUong = getTong(["đồ uống", "đồ tráng miệng"]);

  const MAX_LAU = 3;
  const MAX_MON_PHU = 10;
  const MAX_DO_UONG = 20;

  if (danhMuc === "lẩu" && tongLau + 1 > MAX_LAU) {
    notify.error("Chỉ được tối đa 3 lẩu");
    return;
  }

  if (
    ["đồ nhúng lẩu", "đồ ăn kèm"].includes(danhMuc) &&
    tongMonPhu + 1 > MAX_MON_PHU
  ) {
    notify.error("Chỉ được tối đa 10 món phụ");
    return;
  }

  if (
    ["đồ uống", "đồ tráng miệng"].includes(danhMuc) &&
    tongDoUong + 1 > MAX_DO_UONG
  ) {
    notify.error("Chỉ được tối đa 20 đồ uống");
    return;
  }

  const existing = cart.value.find((i) => i.id === item.id);

  if (existing) {
    existing.quantity++;
  } else {
    cart.value.push({
      id: item.id,
      name: item.tenSanPham,
      price: finalPrice,
      img: item.hinhAnh,
      quantity: 1,
      tenDanhMuc: item.tenDanhMuc,
      trangThaiXoa: item.trangThaiXoa || false,
    });
  }
  hasProductChanged.value = false;
  hasDiscountChanged.value = false;
};

// Update số lượng
const updatequantity = (id, delta) => {
  const item = cart.value.find((i) => i.id === id);
  if (!item) return;

  const danhMuc = item.tenDanhMuc?.trim().toLowerCase();

  const getTong = (listDanhMuc) => {
    return cart.value
      .filter((i) =>
        listDanhMuc.includes((i.tenDanhMuc || "").trim().toLowerCase()),
      )
      .reduce((sum, i) => sum + i.quantity, 0);
  };

  const tongLau = getTong(["lẩu"]);
  const tongMonPhu = getTong(["đồ nhúng lẩu", "đồ ăn kèm"]);
  const tongDoUong = getTong(["đồ uống", "đồ tráng miệng"]);

  const MAX_LAU = 3;
  const MAX_MON_PHU = 10;
  const MAX_DO_UONG = 20;

  if (delta > 0) {
    if (danhMuc === "lẩu" && tongLau + 1 > MAX_LAU) {
      notify.error("Chỉ được tối đa 3 lẩu");
      return;
    }

    if (
      ["đồ nhúng lẩu", "đồ ăn kèm"].includes(danhMuc) &&
      tongMonPhu + 1 > MAX_MON_PHU
    ) {
      notify.error("Chỉ được tối đa 10 món phụ");
      return;
    }

    if (
      ["đồ uống", "đồ tráng miệng"].includes(danhMuc) &&
      tongDoUong + 1 > MAX_DO_UONG
    ) {
      notify.error("Chỉ được tối đa 20 đồ uống");
      return;
    }
  }

  item.quantity += delta;

  if (item.quantity <= 0) {
    cart.value = cart.value.filter((i) => i.id !== id);
  }
  hasProductChanged.value = false;
  hasDiscountChanged.value = false;
};

// Tổng tiền
const totalprice = computed(() => {
  return (cart.value ?? []).reduce(
    (sum, item) => sum + item.price * item.quantity,
    0,
  );
});
// formDatBan đã được khai báo và khởi tạo đồng bộ ngay tại đầu script setup

//////////////////// VALIDATE SỐ GHẾ TRỐNG
const tongGheTrong = ref(0);
const isOverCapacity = computed(() => soChoQuaTai.value);

const loadBan = async () => {
  const res = await getAllBan();
  tongGheTrong.value = res.data
    .filter((b) => b.trangThai === 1)
    .reduce((sum, b) => sum + (b.sucChuaToiDa || 0), 0);
  console.log(res.data);
};

const soChoQuaTai = computed(() => {
  return (
    formDatBan.value.soNguoiToiDa > 0 &&
    formDatBan.value.soNguoiToiDa > tongGheTrong.value
  );
});

const formatDateTime = (date) => {
  if (!date) return null;

  const d = new Date(date);

  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  const hours = String(d.getHours()).padStart(2, "0");
  const minutes = String(d.getMinutes()).padStart(2, "0");

  return `${year}-${month}-${day}T${hours}:${minutes}:00`;
};

const datBan = async () => {
  if (!(await notify.confirm("Bạn có muốn đặt bàn không?"))) return;
  nprogress.start();
  try {
    const formData = {
      ...formDatBan.value,
      thoiGianDat: formatDateTime(formDatBan.value.thoiGianDat),
      monDatTruoc: cart.value.map((item) => ({
        idSanPham: item.id,
        soLuong: item.quantity,
      })),
    };
    const res = await datBanOnline(formData);

    const paymentUrl = res.data;
    if (paymentUrl) {
      window.location.href = paymentUrl;
      return;
    }

    cart.value = [];
    formDatBan.value = {
      tenKhachHang: "",
      soDienThoai: "",
      email: "",
      thoiGianDat: "",
      soNguoiToiDa: "",
      ghiChu: "",
      monDatTruoc: [],
    };
    dateTimePickerInstance?.clear();

    notify.success("Đặt bàn thành công");
  } catch (error) {
    let msg = "Có lỗi xảy ra, vui lòng thử lại!";

    if (error.response?.data) {
      const data = error.response.data;
      if (typeof data === "string") {
        msg = data;
      } else if (data.message) {
        msg = data.message;
      } else {
        msg = JSON.stringify(data); // fallback để vẫn thấy nội dung lỗi
      }
    }

    notify.error(msg);
    console.error(error.response?.data || error);
  } finally {
    nprogress.done();
  }
};

//////////////////// VALDATE ////////////////
const errors = ref({
  tenKhachHang: "",
  soDienThoai: "",
  email: "",
  soNguoiToiDa: "",
  thoiGianDat: "",
});
const validateForm = () => {
  const f = formDatBan.value;
  Object.keys(errors.value).forEach((k) => (errors.value[k] = ""));

  let isValid = true;

  if (!f.tenKhachHang?.trim()) {
    errors.value.tenKhachHang = "Vui lòng nhập họ tên";
    isValid = false;
  }

  const phoneRegex = /^(0|\+84)[0-9]{9}$/;
  if (!f.soDienThoai?.trim()) {
    errors.value.soDienThoai = "Vui lòng nhập số điện thoại";
    isValid = false;
  } else if (!phoneRegex.test(f.soDienThoai)) {
    errors.value.soDienThoai = "Số điện thoại không hợp lệ";
    isValid = false;
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!f.email?.trim()) {
    errors.value.email = "Vui lòng nhập email";
    isValid = false;
  } else if (!emailRegex.test(f.email)) {
    errors.value.email = "Email không hợp lệ";
    isValid = false;
  }

  if (!f.soNguoiToiDa || f.soNguoiToiDa <= 0) {
    errors.value.soNguoiToiDa = "Chưa nhập số lượng khách";
    isValid = false;
  } else if (soChoQuaTai.value) {
    errors.value.soNguoiToiDa = `Vượt quá ${tongGheTrong.value} chỗ`;
    isValid = false;
  }

  if (!f.thoiGianDat) {
    errors.value.thoiGianDat = "Vui lòng chọn thời gian";
    isValid = false;
  } else {
    const now = new Date();
    const timeDat = new Date(f.thoiGianDat);

    const minMinutes = getThamSo("DAT_TRUOC_TOI_THIEU");
    const maxDays = getThamSo("DAT_TRUOC_TOI_DA_NGAY");

    const minTime = new Date(now.getTime() + minMinutes * 60000);
    if (timeDat < minTime) {
      errors.value.thoiGianDat = `Phải đặt trước ít nhất ${minMinutes} phút`;
      isValid = false;
    }

    const maxTime = new Date(now.getTime() + maxDays * 24 * 60 * 60000);
    if (timeDat > maxTime) {
      errors.value.thoiGianDat = `Chỉ được đặt trước tối đa ${maxDays} ngày`;
      isValid = false;
    }
  }

  return isValid;
};

///////////////////// TIỀN CỌC //////////////////
const isDoanLon = computed(() => {
  const soKhachBatBuoc = getThamSo("SO_KHACH_BAT_BUOC_COC_DOAN_LON");
  return (
    formDatBan.value.soNguoiToiDa > 0 &&
    formDatBan.value.soNguoiToiDa > soKhachBatBuoc
  );
});

const tienCocDoanLon = computed(() => {
  if (!isDoanLon.value) return 0;
  const tienMoiKhach = getThamSo("TIEN_COC_MOI_KHACH_DOAN_LON");
  return Math.floor(formDatBan.value.soNguoiToiDa * tienMoiKhach);
});

const tienCocMon = computed(() => {
  if (cart.value.length === 0) return 0;
  const phanTram = getThamSo("PHAN_TRAM_COC");
  return Math.floor(totalprice.value * (phanTram / 100));
});

const depositAmount = computed(() => {
  return tienCocMon.value + tienCocDoanLon.value;
});

const depositLabel = computed(() => {
  const phanTram = getThamSo("PHAN_TRAM_COC");
  const parts = [];

  if (tienCocMon.value > 0) {
    parts.push(`${phanTram}% tiền món`);
  }
  if (tienCocDoanLon.value > 0) {
    parts.push(
      `cọc đoàn > ${getThamSo("SO_KHACH_BAT_BUOC_COC_DOAN_LON")} khách`,
    );
  }

  return parts.length ? `Đặt cọc (${parts.join(" + ")})` : "";
});

const canDeposit = computed(() => {
  return depositAmount.value > 0;
});
onMounted(async () => {
  dateTimePickerInstance = flatpickr(datetimeRef.value, {
    enableTime: true,
    dateFormat: "d/m/Y H:i",
    time_24hr: true,
    minuteIncrement: 5,
    locale: Vietnamese,
    minDate: "today",
    onChange: (selectedDates) => {
      if (selectedDates.length > 0) {
        const d = selectedDates[0];

        const yyyy = d.getFullYear();
        const mm = String(d.getMonth() + 1).padStart(2, "0");
        const dd = String(d.getDate()).padStart(2, "0");
        const hh = String(d.getHours()).padStart(2, "0");
        const mi = String(d.getMinutes()).padStart(2, "0");

        formDatBan.value.thoiGianDat = `${yyyy}-${mm}-${dd}T${hh}:${mi}:00`;
      }
    },
  });

  // Thiết lập ngày mặc định nếu có sẵn
  if (formDatBan.value.thoiGianDat) {
    dateTimePickerInstance.setDate(formDatBan.value.thoiGianDat, true);
    isReady.value = true;
  }

  loadNuocLau();
  connectWebSocket(null, handleWsMessage);

  const savedCart = localStorage.getItem("cart_dat_ban");
  if (savedCart) {
    try {
      const parsed = JSON.parse(savedCart);
      cart.value = (parsed.items || []).map((item) => ({
        id: item.id,
        name: item.tenSanPham,
        price:
          item.giaSauGiam && item.giaSauGiam < item.gia
            ? item.giaSauGiam
            : item.gia,
        img: item.hinhAnh,
        quantity: item.soLuong || 1,
        tenDanhMuc: item.tenDanhMuc,
        trangThaiXoa: item.trangThaiXoa || false,
      }));
    } catch (e) {
      console.error("Lỗi parse cart_dat_ban", e);
    }
  }
  localStorage.removeItem("cart_dat_ban");
  Promise.all([loadThamSo(), loadBan(), loadMenuData()])
    .then(() => {
      isInit.value = false;
      capNhatTrangThaiGioHang();
      nextTick(() => {
        setupRevealAnimation();
      });
    })
    .catch((err) => {
      console.error("Lỗi tải các tham số API khởi tạo:", err);
      isInit.value = false;
      nextTick(() => {
        setupRevealAnimation();
      });
    });
});
onBeforeUnmount(() => {
  if (wsTimeout) clearTimeout(wsTimeout);
  disconnectWebSocket(handleWsMessage, null);
});

const getImgUrl = (path) => {
  return path ? `http://localhost:8080/uploads/${path}` : "";
};
</script>

<template>
  <main class="booking-page py-5">
    <div class="container">
      <header class="text-center mb-5 reveal active">
        <span class="sub-title">exclusive dining</span>
        <h2 class="section-title">Đặt Bàn Thượng Hạng</h2>
        <div class="luxury-line"></div>
      </header>

      <div class="row g-5">
        <div class="col-lg-8 reveal active reveal-left">
          <div class="luxury-card p-4 p-md-5">
            <h5 class="form-section-label">thông tin đặt bàn</h5>
            <div class="booking-grid mb-5">
              <div class="form-group full-width">
                <label class="label-black">
                  Họ tên <span class="required">*</span>
                </label>
                <input
                  v-model="formDatBan.tenKhachHang"
                  type="text"
                  placeholder="Nhập họ tên..."
                  class="luxury-input"
                  @input="errors.tenKhachHang = ''"
                />
                <transition name="shake">
                  <p v-if="errors.tenKhachHang" class="error-hint">
                    <i class="fas fa-exclamation-circle"></i>
                    {{ errors.tenKhachHang }}
                  </p>
                </transition>
              </div>
              <div class="form-group">
                <label class="label-black">
                  Số điện thoại <span class="required">*</span>
                </label>
                <input
                  v-model="formDatBan.soDienThoai"
                  type="tel"
                  placeholder="090 • • • • • • •"
                  class="luxury-input"
                  @input="errors.soDienThoai = ''"
                />
                <transition name="shake">
                  <p v-if="errors.soDienThoai" class="error-hint">
                    <i class="fas fa-exclamation-circle"></i>
                    {{ errors.soDienThoai }}
                  </p>
                </transition>
              </div>
              <div class="form-group">
                <label class="label-black">
                  Địa chỉ email <span class="required">*</span>
                </label>
                <input
                  v-model="formDatBan.email"
                  type="email"
                  placeholder="khachhang@luxury.com"
                  class="luxury-input"
                  @input="errors.email = ''"
                />
                <transition name="shake">
                  <p v-if="errors.email" class="error-hint">
                    <i class="fas fa-exclamation-circle"></i>
                    {{ errors.email }}
                  </p>
                </transition>
              </div>
            </div>

            <h5 class="form-section-label">Chi tiết buổi tiệc</h5>
            <div class="booking-grid mb-5">
              <div class="form-group">
                <label class="label-black">
                  Số lượng khách<span class="required">*</span>
                </label>
                <input
                  v-model.number="formDatBan.soNguoiToiDa"
                  type="number"
                  value="1"
                  min="1"
                  class="luxury-input"
                  @input="errors.soNguoiToiDa = ''"
                />
                <p v-if="soChoQuaTai" class="error-hint">
                  Số khách vượt quá sức chứa hiện tại ({{ tongGheTrong }}
                  chỗ trống)
                </p>
                <transition name="shake">
                  <p v-if="errors.soNguoiToiDa" class="error-hint">
                    <i class="fas fa-exclamation-circle"></i>
                    {{ errors.soNguoiToiDa }}
                  </p>
                </transition>
              </div>

              <div class="form-group">
                <label class="label-black">
                  Thời gian phục vụ <span class="required">*</span>
                </label>
                <input
                  ref="datetimeRef"
                  type="text"
                  placeholder="Chọn ngày & giờ"
                  class="luxury-input"
                  @input="errors.thoiGianDat = ''"
                />
                <transition name="shake">
                  <p v-if="errors.thoiGianDat" class="error-hint">
                    <i class="fas fa-exclamation-circle"></i>
                    {{ errors.thoiGianDat }}
                  </p>
                </transition>
              </div>
              <div class="form-group full-width">
                <label>Ghi chú đặc biệt</label>
                <textarea
                  v-model="formDatBan.ghiChu"
                  rows="2"
                  placeholder="Ví dụ: bàn gần cửa sổ, dị ứng thực phẩm..."
                  class="luxury-input"
                ></textarea>
              </div>
            </div>
          </div>
        </div>

        <div class="col-lg-4 reveal active reveal-right">
          <div class="receipt-card sticky-top">
            <div class="receipt-inner p-4">
              <div class="receipt-head text-center mb-4">
                <h3 class="fw-bold m-0 text-danger uppercase tracking-widest">
                  Tổng hợp yêu cầu
                </h3>
                <small class="text-gold">reservation details</small>
              </div>

              <div class="receipt-content">
                <div class="selected-items-list mb-4">
                  <div
                    v-for="item in cart"
                    :key="item.id"
                    class="receipt-row"
                    :class="{ 'item-disabled': item.trangThaiXoa }"
                  >
                    <div class="receipt-food-img">
                      <img :src="getImgUrl(item.img)" />
                    </div>

                    <div class="item-meta">
                      <span class="item-title">{{ item.name }}</span>
                      <span
                        v-if="item.trangThaiXoa"
                        class="text-danger fw-bold small"
                      >
                        Món ăn ngừng kinh doanh
                      </span>
                      <span v-else class="item-subtotal">
                        {{ (item.price * item.quantity).toLocaleString() }}đ
                      </span>
                    </div>

                    <div class="item-controls">
                      <button
                        @click="updatequantity(item.id, -1)"
                        class="btn-qty-luxury btn-minus"
                      ></button>

                      <span class="qty-num">{{ item.quantity }}</span>

                      <button
                        @click="updatequantity(item.id, 1)"
                        class="btn-qty-luxury btn-plus"
                        :disabled="item.trangThaiXoa"
                      ></button>
                    </div>
                  </div>
                </div>

                <div class="receipt-total pt-4 border-top">
                  <p v-if="coMonNgungKinhDoanh" class="error-hint text-center">
                    Vui lòng xóa các món đã ngừng kinh doanh trước khi tiếp tục
                  </p>
                  <div
                    class="d-flex justify-content-between align-items-center mb-2"
                  >
                    <span class="fw-bold uppercase small">Tạm tính</span>
                    <span class="total-amount">
                      {{ totalprice.toLocaleString() }}đ
                    </span>
                  </div>

                  <div
                    v-if="canDeposit"
                    class="d-flex justify-content-between align-items-center mb-3 deposit-row"
                  >
                    <span class="small">{{ depositLabel }}</span>
                    <span class="deposit-amount">
                      {{ depositAmount.toLocaleString() }}đ
                    </span>
                  </div>
                  <div v-if="canDeposit" class="deposit-warning">
                    Tiền cọc dự kiến:
                    <b>{{ depositAmount.toLocaleString() }}đ</b>
                    ({{ depositLabel }})
                    <br />
                    Đơn của bạn sẽ được ghi nhận và chờ nhân viên xác nhận bàn.
                    Sau khi xác nhận, nhà hàng sẽ gửi email yêu cầu thanh toán
                    tiền cọc kèm hướng dẫn thanh toán qua VNPay. Vui lòng chú ý
                    email để hoàn tất đặt bàn.
                  </div>
                </div>
              </div>

              <div
                v-if="cart.length === 0"
                class="empty-receipt text-center py-5"
              >
                <i
                  class="bi bi-journal-text opacity-25"
                  style="font-size: 3rem"
                ></i>
                <p class="mt-3 smaller text-muted italic">
                  Quý khách có thể chọn trước món
                </p>
              </div>
              <div class="text-center mb-4">
                <button class="btn-open-menu" @click="showMenuModal = true">
                  {{
                    cart.length > 0 ? "Chỉnh sửa món đã chọn" : "Chọn món trước"
                  }}
                </button>
              </div>
              <div class="d-flex flex-column gap-2">
                <div v-if="hasProductChanged && cart.length > 0 && !showMenuModal" class="alert-product-changed fw-bold">
                  <i class="ri-error-warning-fill"></i>
                  <span>Sản phẩm đã có thay đổi, vui lòng xem lại thông tin trước khi đặt bàn</span>
                </div>
                <div v-if="hasDiscountChanged && cart.length > 0 && !showMenuModal" class="alert-product-changed fw-bold">
                  <i class="ri-error-warning-fill"></i>
                  <span>Sản phẩm đã có sự thay đổi do đợt giảm giá thay đổi vui lòng xem lại thông tin sản phẩm trước khi đặt bàn</span>
                </div>
                <button
                  class="btn-confirm-luxury w-100"
                  @click="handleClick"
                  :class="{ disabled: isOverCapacity || coMonNgungKinhDoanh }"
                  :disabled="isOverCapacity || coMonNgungKinhDoanh"
                >
                  Xác nhận
                  <div class="light-sweep"></div>
                </button>
              </div>
              <!-- <div v-if="showPaymentMethods" class="payment-expand">
                  <p class="payment-warning">
                    Vui lòng chọn phương thức thanh toán
                  </p>

                  <div class="payment-list">
                    <div
                      class="payment-item"
                      :class="{ active: paymentType === 'VNPAY' }"
                      @click="paymentType = 'VNPAY'"
                    >
                      <img
                        src="https://sandbox.vnpayment.vn/paymentv2/images/brands/logo.svg"
                      />
                      <span>VNPay</span>
                    </div>

                    <div
                      class="payment-item momo"
                      :class="{ active: paymentType === 'MOMO' }"
                      @click="paymentType = 'MOMO'"
                    >
                      <img src="@/assets/icons/momo.png" />
                      <span>MoMo</span>
                    </div>
                  </div>
                </div> -->
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
  <div v-if="showMenuModal" class="menu-luxury-overlay">
    <div class="luxury-backdrop" @click="showMenuModal = false; hasProductChanged = false; hasDiscountChanged = false"></div>

    <div class="menu-luxury-modal">
      <div class="m-corner tl"></div>
      <div class="m-corner tr"></div>
      <div class="m-corner bl"></div>
      <div class="m-corner br"></div>

      <div class="luxury-modal-header">
        <div class="header-decor-line"></div>
        <h2 class="oriental-title">THỰC ĐƠN NHÀ HÀNG</h2>
        <div class="header-decor-line"></div>
        <span class="close-btn-luxury" @click="showMenuModal = false; hasProductChanged = false; hasDiscountChanged = false">
          <span class="close-icon">✕</span>
          <div class="close-halo"></div>
        </span>
      </div>
      <div v-if="hasProductChanged && cart.length > 0" class="alert-product-changed fw-bold mt-2 mb-3">
        <i class="ri-error-warning-fill"></i>
        <span>Sản phẩm đã có thay đổi, vui lòng xem lại thông tin trước khi đặt bàn</span>
      </div>
      <div v-if="hasDiscountChanged && cart.length > 0" class="alert-product-changed fw-bold mt-2 mb-3">
        <i class="ri-error-warning-fill"></i>
        <span>Sản phẩm đã có sự thay đổi do đợt giảm giá thay đổi vui lòng xem lại thông tin sản phẩm trước khi đặt bàn</span>
      </div>
      <div class="luxury-tabs-container">
        <div
          class="luxury-tab-item"
          :class="{ active: tabHienTai === 'chinh' }"
          @click="changeTab('chinh')"
        >
          <span class="tab-text">MÓN CHÍNH</span>
          <div class="tab-indicator"></div>
        </div>

        <div class="tab-separator">
          <div class="separator-dot"></div>
        </div>

        <div
          class="luxury-tab-item"
          :class="{ active: tabHienTai === 'phu' }"
          @click="changeTab('phu')"
        >
          <span class="tab-text">MÓN PHỤ</span>
          <div class="tab-indicator"></div>
        </div>
      </div>

      <div class="luxury-filter-bar">
        <div class="search-box-luxury">
          <label>Tìm kiếm</label>
          <i class="bi bi-search"></i>
          <input
            v-model="tuKhoaTimKiem"
            type="text"
            placeholder="Tìm kiếm món ăn..."
          />
        </div>

        <div class="select-group-luxury">
          <div class="select-item" v-if="tabHienTai === 'phu'">
            <label>Danh mục</label>
            <v-select
              v-model="danhMucDuocChon"
              :options="danhSachDanhMuc"
              label="tenDanhMuc"
              :reduce="(item) => item.id"
              placeholder="Chọn danh mục..."
              @update:modelValue="searchMon"
              class="modern-select"
            >
              <template #no-options="{ search, searching }">
                <template v-if="searching">
                  Không tìm thấy "<b>{{ search }}</b
                  >"
                </template>
                <em v-else style="opacity: 0.5">Nhập tên danh mục để tìm...</em>
              </template>
            </v-select>
          </div>

          <div class="select-item" v-if="tabHienTai === 'chinh'">
            <label>Loại lẩu</label>
            <v-select
              v-model="nuocLauDuocChon"
              :options="danhSachNuocLau"
              label="tenNuocLau"
              :reduce="(item) => item.id"
              placeholder="Chọn loại lẩu..."
              @update:modelValue="searchMon"
              class="modern-select"
            >
              <template #no-options="{ search, searching }">
                <template v-if="searching">
                  Không tìm thấy "<b>{{ search }}</b
                  >"
                </template>
                <em v-else style="opacity: 0.5">Nhập tên loại lẩu để tìm...</em>
              </template>
            </v-select>
          </div>

          <div class="select-item">
            <label>Sắp xếp</label>
            <v-select
              v-model="sapXepGia"
              :options="[
                { label: 'Mặc định', value: 'default' },
                { label: 'Giá tăng dần', value: 'asc' },
                { label: 'Giá giảm dần', value: 'desc' },
              ]"
              label="label"
              :reduce="(item) => item.value"
              placeholder="Sắp xếp"
              class="oriental-select"
            />
          </div>
        </div>
      </div>

      <div class="luxury-food-scroll">
        <div class="food-grid-luxury">
          <div
            v-for="item in danhSachMonDaLoc"
            :key="item.id"
            class="food-card-luxury"
          >
            <div class="food-thumb">
              <img :src="getImgUrl(item.hinhAnh)" />
              <div class="thumb-overlay">
                <button class="btn-quick-add" @click="addtocart(item)">
                  <i class="bi bi-plus-lg"></i> CHỌN MÓN
                </button>
              </div>
            </div>

            <div class="food-info-luxury">
              <!-- bọc phần này để xử lý flexbox căn lề chuẩn -->
              <div class="food-header-wrapper">
                <h4 class="food-name-luxury">{{ item.tenSanPham }}</h4>

                <div class="food-price-luxury">
                  <template
                    v-if="item.giaSauGiam && item.giaSauGiam < item.gia"
                  >
                    <span class="old-price">
                      {{ item.gia.toLocaleString() }}đ
                    </span>
                    <span class="new-price">
                      {{ item.giaSauGiam.toLocaleString() }}đ
                    </span>
                  </template>

                  <template v-else>
                    <span class="new-price">
                      {{ (item.giaSauGiam || item.gia).toLocaleString() }}đ
                    </span>
                  </template>
                </div>
              </div>

              <!-- giữ nguyên gạch chân màu đỏ của bạn -->
              <div class="luxury-dot-divider"></div>

              <p class="food-desc-luxury">{{ item.moTa }}</p>
            </div>

            <div class="btn-light-sweep"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ////////////// MODAL CHỌN MÓN //////////// */
@import url("https://fonts.googleapis.com/css2?family=Playfair+Display:wght@700;900&display=swap");

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
.food-header-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 8px;
}

.food-name-luxury {
  flex: 1;
  margin: 0;
  line-height: 1.4;

  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.food-price-luxury {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  text-align: right;
  min-width: fit-content;
}

.luxury-dot-divider {
  width: 35px;
  height: 2px;
  background-color: #c0392b;
  margin-bottom: 15px;
  border: none;
}

.new-price {
  color: #c0392b;
  font-weight: bold;
  font-size: 1.1rem;
}

.old-price {
  font-size: 0.85rem;
  color: #999;
  text-decoration: line-through;
}
.menu-luxury-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2000;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.luxury-backdrop {
  position: absolute;
  width: 100%;
  height: 100%;
  background: rgba(123, 121, 121, 0.85);
  backdrop-filter: blur(8px);
}

.menu-luxury-modal {
  position: relative;
  width: 100%;
  max-width: 1100px;
  max-height: 90vh;
  background: #fff;
  border: 4px double #d4af37;
  padding: 40px;
  display: flex;
  flex-direction: column;
  animation: modalRise 0.5s ease-out;
}

.m-corner {
  position: absolute;
  width: 25px;
  height: 25px;
  border: 4px solid #e83b40;
  z-index: 2;
}
.m-corner.tl {
  top: 15px;
  left: 15px;
  border-right: none;
  border-bottom: none;
}
.m-corner.tr {
  top: 15px;
  right: 15px;
  border-left: none;
  border-bottom: none;
}
.m-corner.bl {
  bottom: 15px;
  left: 15px;
  border-right: none;
  border-top: none;
}
.m-corner.br {
  bottom: 15px;
  right: 15px;
  border-left: none;
  border-top: none;
}

/* Header */
.luxury-modal-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-bottom: 30px;
  position: relative;
}

.oriental-title {
  font-family: "Playfair Display", serif;
  font-weight: 900;
  color: #e73e44;
  letter-spacing: 5px;
  margin: 0;
  text-align: center;
}

.header-decor-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, transparent, #d4af37, transparent);
}

.close-btn-luxury {
  position: absolute;
  top: 15px;
  right: 15px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  transition: all 0.4s ease;
}

.close-icon {
  font-size: 1.5rem;
  color: rgba(255, 10, 10, 0.6);
  z-index: 2;
  transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.close-halo {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 1px solid rgba(212, 175, 55, 0);
  border-radius: 50%;
  transition: all 0.5s ease;
  z-index: 1;
}

.close-btn-luxury:hover .close-icon {
  color: #f02020;
  transform: rotate(180deg) scale(1); /* Xoay nửa vòng và phóng to */
  text-shadow: 0 0 15px rgba(212, 175, 55, 0.8);
}

.close-btn-luxury:active {
  transform: scale(0.9);
}

/* Filter Bar */
.luxury-filter-bar {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 30px;
  background: #f9f9f9;
  padding: 15px;
  border: 1px solid #eee;
}

.search-box-luxury {
  flex: 1;
  position: relative;
}

.search-box-luxury i {
  position: absolute;
  left: 15px;
  top: 69%;
  transform: translateY(-50%);
  color: #d4af37;
}
.search-box-luxury input {
  width: 100%;
  padding: 10px 10px 10px 45px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 4px;
}

.select-group-luxury {
  display: flex;
  gap: 10px;
  width: 40%;
}

.luxury-filter-bar {
  display: flex;
  gap: 20px;
  align-items: flex-end;
  width: 100%;
  padding: 15px;
}

.search-box-luxury {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.select-group-luxury {
  display: flex;
  gap: 15px;
}

.select-item {
  display: flex;
  flex-direction: column;
  gap: 5px;

  min-width: 200px;
}

.select-item:last-child {
  min-width: 150px;
}

.modern-select,
.oriental-select {
  width: 100% !important;
}

.vs__dropdown-toggle {
  height: 40px;
  border: none !important;
  border-bottom: 2px solid #d4af37 !important;
  background: transparent !important;
  border-radius: 0 !important;
}

/* Food Grid */
.luxury-food-scroll {
  overflow-y: auto;
  padding-right: 10px;
}

.food-grid-luxury {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 30px;
}

.food-card-luxury {
  border-bottom: 1px dashed #d4af37;
  padding-bottom: 20px;
  transition: 0.3s;
}

.food-thumb {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  border: 1px solid #eee;
}

.food-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: 0.5s;
}

.thumb-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(163, 29, 33, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: 0.3s;
}

.food-card-luxury:hover .thumb-overlay {
  opacity: 1;
}
.food-card-luxury:hover img {
  transform: scale(1.1);
}

.btn-quick-add {
  background: #f43a3a;
  color: #fff;
  border: none;
  padding: 10px 20px;
  font-weight: 600;
  letter-spacing: 1px;
}
.food-card-luxury {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.food-thumb {
  width: 100%;
  height: 200px; /* cố định */
  overflow: hidden;
  flex-shrink: 0;
}

.food-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.food-info-luxury {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.food-desc-luxury {
  font-size: 0.85rem;
  color: #777;

  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;

  min-height: 40px;
}
.food-info-luxury > div:first-child {
  min-height: 50px; /* giữ header cố định */
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
.food-card-luxury {
  position: relative;
  overflow: hidden;
}

.food-card-luxury:hover .btn-light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
}
/* Text Info */
.food-name-luxury {
  font-weight: 700;
  font-size: 1.15rem;
  color: #4f3a3a;
  margin-top: 15px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;

  min-height: 30px;
}

.food-price-luxury {
  color: #ee3f45;
  font-weight: 700;
  font-size: 1.1rem;
  margin-top: 15px;
}
.old-price {
  text-decoration: line-through;
  color: #999;
  font-size: 0.9rem;
  margin-right: 6px;
}

.luxury-dot-divider {
  width: 30px;
  height: 2px;
  background: #e74343;
  margin: 8px 0;
}

.food-desc-luxury {
  font-size: 14px;
  color: #777;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Scrollbar */
.luxury-food-scroll::-webkit-scrollbar {
  width: 4px;
}
.luxury-food-scroll::-webkit-scrollbar-thumb {
  background: #d4af37;
}

@keyframes modalRise {
  from {
    opacity: 0;
    transform: translateY(40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.luxury-tabs-container {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-bottom: 25px;
}

.luxury-tab-item {
  cursor: pointer;
  position: relative;

  transition: all 0.4s ease;
}

.tab-text {
  font-family: "Playfair Display", serif;
  font-weight: 800;
  font-size: 1.1rem;
  letter-spacing: 2px;
  color: #635b5b;
  transition: 0.3s;
}

/* Hiệu ứng gạch chân kiểu Gold Line */
.tab-indicator {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 2px;
  background: #d4af37;
  transition: 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

/* Khi Tab Active */
.luxury-tab-item.active .tab-text {
  color: #a31d21; /* Đỏ đô đặc trưng */
  transform: scale(1.1);
}

.luxury-tab-item.active .tab-indicator {
  width: 80%;
}

/* Dấu chấm ngăn cách ở giữa */
.tab-separator {
  display: flex;
  align-items: center;
}

.separator-dot {
  width: 8px;
  height: 8px;
  background: #d4af37;
  transform: rotate(45deg);
  box-shadow: 0 0 10px rgba(212, 175, 55, 0.5);
}

/* Thêm hiệu ứng hover nhẹ */
.luxury-tab-item:hover .tab-text {
  color: #d4af37;
}

/* Animation cho danh sách khi chuyển tab */
.food-grid-luxury {
  /* ... giữ nguyên grid cũ ... */
  animation: fadeIn 0.5s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
/* 
/////////////// HẾT ///////////////// */

.payment-expand {
  margin-top: 12px;
  padding: 12px;
  border: 1px solid #eee;
  border-radius: 12px;
  background: #fafafa;
  animation: slideDown 0.25s ease;
}

.payment-warning {
  font-size: 13px;
  color: #ec6969;
  margin-bottom: 10px;
  font-weight: 600;
}

.payment-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.payment-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border: 1.5px solid #ddd;
  border-radius: 10px;
  cursor: pointer;
  transition: 0.25s;
  background: white;
}

.payment-item img {
  width: 36px;
  height: 36px;
}

.payment-item.active {
  border-color: #ef5757;
  background: #fadbdb;
}

/* momo riêng */
.payment-item.momo.active {
  border-color: #d82d8b;
  background: #ffe6f2;
}

/* animation */
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
.disabled {
  opacity: 0.4;
  pointer-events: none;
  filter: grayscale(40%);
}
.receipt-inner {
  display: flex;
  flex-direction: column;
}

.empty-receipt {
  flex: 1;
}
.deposit-row {
  font-size: 14px;
  color: #c0392b;
  font-weight: 600;
}

.deposit-amount {
  color: #e74c3c;
  font-weight: 800;
}
.receipt-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.receipt-food-img {
  width: 45px;
  height: 45px;
  overflow: hidden;
  border-radius: 6px;
}

.receipt-food-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.deposit-warning {
  font-size: 13px;
  color: #c0392b;
  margin-bottom: 12px;
}
.menu-filter-box {
  position: sticky;
  top: 0;
  background: white;
  z-index: 5;
  padding-bottom: 10px;
}

.menu-food-scroll {
  max-height: 420px;
  overflow-y: auto;
  padding-right: 6px;
}

.menu-food-scroll::-webkit-scrollbar {
  width: 6px;
}

.menu-food-scroll::-webkit-scrollbar-thumb {
  background: #c5a059;
  border-radius: 10px;
}

/* scroll đẹp hơn */
.menu-selection::-webkit-scrollbar {
  width: 6px;
}
.menu-selection {
  padding: 20px;
  border: 1px solid red;
}

.menu-food-scroll {
  max-height: 420px;
  overflow-y: auto;
}

.menu-selection::-webkit-scrollbar-thumb {
  background: #c5a059;
  border-radius: 10px;
}
.luxury-input[type="date"]::-webkit-datetime-edit {
  color: transparent;
}

.luxury-input[type="date"]:focus::-webkit-datetime-edit,
.luxury-input[type="date"]:valid::-webkit-datetime-edit {
  color: #1a1a1a;
}
.btn-open-menu {
  background: transparent;
  border: 1.5px solid #ec3e3e;
  color: #af0911;
  padding: 10px 28px;
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 2px;
  transition: 0.25s;
}

.btn-open-menu:hover {
  background: #f34a4a;
  color: #ffffff;
}

.booking-page {
  background-color: #fbfbfb;
  font-family: var(--font-family);
  color: #1a1a1a;
  min-height: 100vh;
}
/* Typography & Headers */
.sub-title {
  text-transform: uppercase;
  letter-spacing: 6px;
  font-size: 0.7rem;
  color: #c5a059;
  font-weight: 700;
  display: block;
  margin-bottom: 10px;
}
.section-title {
  font-family: "Playfair Display", serif;
  font-size: 2.5rem;
  font-weight: 700;
  color: #e86565;
}
.luxury-line {
  width: 40px;
  height: 2px;
  background: #c5a059;
  margin: 20px auto;
}

/* Card Styles */
.luxury-card {
  background: #ffffff;
  border: 2px solid #f0f0f0;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.02);
}
.form-section-label {
  font-family: "Playfair Display", serif;
  font-size: 1.1rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 25px;
  color: #1a1a1a;
  border-left: 3px solid #c5a059;
  padding-left: 15px;
}

/* Grid & Inputs */
.booking-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}
.full-width {
  grid-column: span 2;
}

.form-group label {
  display: block;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.5px;
  color: #1a1a1a;
  margin-bottom: 6px;
}
.luxury-input {
  width: 100%;
  border: none;
  border-bottom: 2px solid #e0e0e0;
  padding: 10px 0;
  background: transparent;
  font-size: 0.95rem;
  transition: 0.3s;
  border-radius: 0;
}
.luxury-input:focus {
  outline: none;
  border-color: #c5a059;
}
.luxury-input::placeholder {
  color: #b8b8b8;
  font-size: 0.85rem;
}

/* Food Gallery */
/* food list horizontal */
.food-row-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.food-row-bottom {
  margin-top: auto;
  display: flex;
  justify-content: flex-end;
}
.food-gallery {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* mỗi món */

.food-row {
  display: flex;
  gap: 18px;
  padding: 16px;
  border: 1px solid #ee8080;
  transition: 0.3s;
  background: white;
  border-radius: 6px;
}

.food-row:hover {
  border-color: #c5a059;
  transform: translateY(-2px);
}

/* ảnh món */

.food-img-box {
  width: 80px;
  height: 80px;
  flex-shrink: 0;
  overflow: hidden;
}

.food-img-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* info */

.food-row-info {
  flex: 1;
}

.food-row-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.food-name {
  font-size: 18px;
  font-weight: 1000;
  text-transform: uppercase;
  margin: 0;
  color: #111;
}
.price-tag {
  font-weight: 800;
  font-size: 1rem;
  color: #c5a059;
}

.food-desc {
  font-size: 0.85rem;
  color: #666;
  font-weight: 500;
  margin: 6px 0 10px;
}
/* nút thêm */

.btn-add-dish {
  background: transparent;
  border: 1px solid #c5a059;
  color: #ef3838;
  padding: 5px 14px;
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  transition: 0.25s;
}

.btn-add-dish:hover {
  background: #f47676;
  color: #111;
}

/* Receipt Card (Right Sidebar) */
.receipt-card {
  background: #ffffff;
  border: 2px solid #f0f0f0;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  color: #1a1a1a;
  top: 30px;
}
.text-gold {
  color: #ee4141;
  font-size: 0.6rem;
  letter-spacing: 2px;
  text-transform: uppercase;
}
.receipt-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #f0f0f0;
}
.item-title {
  display: block;
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
  color: #111;
  letter-spacing: 0.5px;
}
.item-subtotal {
  font-size: 0.85rem;
  font-weight: 600;
  color: #777;
}
.item-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.btn-qty-luxury {
  position: relative;
  width: 28px;
  height: 28px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 6px;
  cursor: pointer;
  padding: 0;
  color: #de1818;
}

/* dấu - */
.btn-minus::before {
  content: "";
  position: absolute;
  width: 12px;
  height: 2px;
  background: currentColor;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

/* dấu + */
.btn-plus::before,
.btn-plus::after {
  content: "";
  position: absolute;
  background: currentColor;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.btn-plus::before {
  width: 12px;
  height: 2px;
}
.item-controls {
  display: flex;
  align-items: center;
  flex-direction: row; /* QUAN TRỌNG */
  gap: 8px;
}
.btn-plus::after {
  width: 2px;
  height: 12px;
}
.btn-qty-luxury:hover {
  border-color: #c5a059;
  color: #c5a059;
}
.qty-num {
  font-weight: 700;
  min-width: 20px;
  text-align: center;
}
.total-amount {
  font-size: 1.5rem;
  font-weight: 800;
  color: #f14949;
}
.btn-confirm-luxury {
  position: relative;
  overflow: hidden;
  background: #e84242;
  color: #ffffff;
  border: none;
  padding: 18px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 3px;
  font-size: 0.8rem;
  transition: 0.3s;
}
.btn-confirm-luxury:hover {
  background: #ea4343;
  letter-spacing: 4px;
}
.btn-confirm-luxury:hover .light-sweep {
  left: 150%;
}
.light-sweep {
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  transition: 0.5s;
}

/* Minimal controls */
.minimal-search {
  border: none;
  border-bottom: 1px solid rgb(232, 16, 16);
  font-size: 1rem;
  padding: 10px 6px;
  background: transparent;
  min-width: 180px;
}

.minimal-select {
  border: none;
  background: transparent;
  cursor: pointer;
  appearance: none;
  padding-right: 20px;
  min-width: 160px;
}

.minimal-search:focus,
.minimal-select:focus {
  outline: none;
}
/* Animations */
.reveal {
  opacity: 0;
  transform: translateY(20px);
  transition: 0.8s cubic-bezier(0.2, 1, 0.3, 1);
}
.reveal.active {
  opacity: 1;
  transform: translateY(0);
}

@media (max-width: 768px) {
  .booking-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  .section-title {
    font-size: 1.8rem;
  }
}

.v-select {
  position: relative;
  z-index: 1000;
}

.vs__dropdown-menu {
  z-index: 2000;
}

/* ===== SELECT 2 && TIM KIEM ===== */
/* Ghi đè triệt để các viền mặc định của thư viện */
:deep(.v-select) {
  background: #fff;
}

/* Khung chính: Xóa border mặc định, dùng border của mình */
:deep(.vs__dropdown-toggle) {
  border: none !important;
  border-bottom: 1px solid #e74c3c !important;
  border-radius: 0 !important;
  height: 42px;
  padding: 0;
  display: flex;
  align-items: center;
  box-shadow: none !important;
}
/* Xóa viền đỏ/xanh khi focus mà thư viện tự thêm vào search input */
:deep(.vs__search),
:deep(.vs__selected) {
  padding-left: 6px !important;
  font-size: 16px;
}
:deep(.vs__search),
:deep(.vs__search:focus) {
  border: none !important;
  margin: 0 !important;
  padding: 0 6px !important;
  height: 40px;
}

/* Xử lý text đã chọn */
:deep(.vs__selected) {
  margin: 0 !important;
  padding-left: 12px !important;
  color: #374151;
  border: none !important;
}

/* Placeholder */
:deep(.vs__search::placeholder) {
  color: #9ca3af !important;
}

/* Hiệu ứng khi mở menu (Focus) */
:deep(.vs--open .vs__dropdown-toggle) {
  border-color: #c5a059 !important;
  box-shadow: none !important;
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

:deep(.vs__dropdown-option--highlight) {
  background: #f3f4f6 !important;
  color: #3b6cff !important;
}

:deep(.vs__dropdown-option--selected) {
  background: #e8efff !important;
  color: #3b6cff !important;
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
  display: none !important;
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

.menu-filter-row input {
  width: 220px;
}

.menu-filter-row .v-select {
  width: 200px;
}
.menu-filter-row {
  display: flex;
  align-items: center;
  gap: 20px;
}

.menu-filter-row .minimal-search {
  width: 220px;
}

.menu-filter-row .v-select {
  width: 200px;
}
/* ===== CHỈ FIX MOBILE ĐẲNG CẤP TẠI ĐÂY ===== */
@media (max-width: 768px) {
  /* tiêu đề co giãn mượt hơn */
  .section-title {
    font-size: 1.8rem !important;
    letter-spacing: -0.5px;
  }

  /* layout 1 cột sạch sẽ */
  .booking-grid {
    grid-template-columns: 1fr !important;
    gap: 15px !important;
  }

  .full-width {
    grid-column: span 1 !important;
  }

  /* card thông tin bo góc lớn */
  .luxury-card {
    padding: 20px !important;
    border-radius: 20px !important;
    border: none !important;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05) !important;
  }

  /* bộ lọc dồn hàng dọc chuyên nghiệp */
  .menu-filter-row {
    flex-direction: column !important;
    gap: 12px !important;
  }

  .menu-filter-row > *,
  .menu-filter-row .minimal-search,
  .menu-filter-row .v-select {
    width: 100% !important;
  }

  /* món ăn dạng card nổi */
  .food-row {
    flex-direction: row !important; /* giữ ảnh bên trái cho sang */
    align-items: flex-start !important;
    padding: 12px !important;
    border: 1px solid #f0f0f0 !important;
    border-radius: 15px !important;
  }

  .food-img-box {
    width: 70px !important;
    height: 70px !important;
    border-radius: 10px !important;
  }

  .food-name {
    font-size: 15px !important;
  }

  /* BIẾN GIỎ HÀNG THÀNH BOTTOM BAR ĐẲNG CẤP */
  .col-lg-4.reveal-right {
    position: fixed !important;
    bottom: 0 !important;
    left: 0 !important;
    right: 0 !important;
    width: 100% !important;
    z-index: 1000 !important;
    padding: 0 !important;
    margin: 0 !important;
  }

  .receipt-card {
    border-radius: 25px 25px 0 0 !important;
    box-shadow: 0 -10px 40px rgba(0, 0, 0, 0.15) !important;
    border: none !important;
    background: #fff !important;
    max-height: 80vh !important;
    overflow-y: auto !important;
  }

  .receipt-inner {
    padding: 15px 20px !important;
  }

  .selected-items-list {
    display: none !important;
  }

  .empty-receipt {
    display: none !important;
  }

  .receipt-head {
    display: none !important;
  }

  .total-amount {
    font-size: 1.5rem !important;
  }

  .btn-confirm-luxury {
    width: 100% !important;
    border-radius: 12px !important;
    padding: 14px !important;
    font-size: 0.9rem !important;
    margin-top: 5px !important;
    background: #1a1a1a !important;
    color: #c5a059 !important;
  }

  .booking-page {
    padding-bottom: 120px !important;
  }

  /* Tối ưu hóa Modal chọn món trước trên Mobile */
  .menu-luxury-modal {
    padding: 20px 15px !important;
    max-height: 95vh !important;
    margin: 10px !important;
    border-radius: 12px !important;
  }
  .oriental-title {
    font-size: 1.3rem !important;
    letter-spacing: 2px !important;
  }
  .luxury-modal-header {
    margin-bottom: 15px !important;
  }
  .luxury-tabs-container {
    margin-bottom: 15px !important;
    gap: 10px !important;
  }
  .luxury-tab-item {
    padding: 8px 12px !important;
  }
  .tab-text {
    font-size: 0.85rem !important;
  }
  .luxury-filter-bar {
    flex-direction: column !important;
    gap: 12px !important;
    align-items: stretch !important;
    padding: 10px !important;
    margin-bottom: 15px !important;
  }
  .select-group-luxury {
    width: 100% !important;
    flex-direction: column !important;
    gap: 10px !important;
  }
  .select-item {
    min-width: unset !important;
    width: 100% !important;
  }
  .food-grid-luxury {
    grid-template-columns: 1fr !important;
    gap: 15px !important;
  }
  .food-card-luxury {
    padding-bottom: 15px !important;
  }
  .food-thumb {
    height: 150px !important;
  }
  .food-name-luxury {
    font-size: 0.95rem !important;
  }
  .new-price {
    font-size: 1rem !important;
  }
  .old-price {
    font-size: 0.8rem !important;
  }
}

/* BAT DAU LAM */
.summary-actions .error-hint {
  font-size: 12px;
  color: #dc2626;
  text-align: center;
}
.error-hint {
  color: #dc2626;
  font-size: 13px;
  margin-top: 8px;
}

.required {
  color: #d32828;
  font-weight: 900;
}

.alert-product-changed {
  background: rgba(220, 38, 38, 0.05);
  border: 1px solid rgba(220, 38, 38, 0.15);
  border-radius: 8px;
  padding: 12px 14px;
  color: #b91c1c;
  font-size: 13px;
  line-height: 1.45;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.04);
  animation: slideDown 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  text-align: left;
}
.alert-product-changed i {
  font-size: 18px;
  color: #dc2626;
  flex-shrink: 0;
}
</style>
