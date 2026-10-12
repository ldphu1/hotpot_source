<script setup>
import { ref, computed, onMounted, h, watch } from "vue";
import {
  exportThongKeExcel,
  getBookingChannel,
  getHoaDonTheoNgay,
  getThongKeDoanhThu,
  getThongKeTheoNgay,
} from "@/api/admin/thongke/thongKeApi";
import { getDoanhThuLoc } from "@/api/admin/thongke/thongKeApi";
import { getDoanhThuThang } from "@/api/admin/thongke/thongKeApi";
import { getTopSetBanChay } from "@/api/admin/thongke/thongKeApi";
import { getTrangThaiHoaDon } from "@/api/admin/thongke/thongKeApi";
import { Filler } from "chart.js";
import { Line, Doughnut } from "vue-chartjs";
import { useUtils } from "@/utils/helper";
const { notify, nprogress } = useUtils();
import iconEye from "@/assets/icons/eye.png";
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  LineElement,
  ArcElement,
  PieController,
  DoughnutController,
  CategoryScale,
  LinearScale,
  PointElement,
} from "chart.js";

ChartJS.register(
  Title,
  Tooltip,
  Legend,
  LineElement,
  ArcElement,
  PieController,
  DoughnutController,
  CategoryScale,
  LinearScale,
  PointElement,
  Filler,
);

const isOrderStatusEmpty = computed(() =>
  orderStatus.value.datasets[0].data.every((v) => !v),
);

const isBookingChannelEmpty = computed(
  () => !rawBookingData.value.online && !rawBookingData.value.taiQuay,
);

/* ======================= BỘ LỌC (NGUỒN CHUNG CHO 4 PHẦN PHỤ THUỘC) ======================= */

const filters = ["Hôm nay", "Tuần này", "Tháng này", "Năm nay", "Tùy chỉnh"];

const mapFilterToType = {
  "Hôm nay": "HOM_NAY",
  "Tuần này": "TUAN",
  "Tháng này": "THANG",
  "Năm nay": "NAM",
  "Tùy chỉnh": "TUY_CHINH",
};

const type = ref("HOM_NAY");
const selectedFilter = ref("Hôm nay");

// Dùng cho filter "Tùy chỉnh"
const customFrom = ref(""); // yyyy-MM-dd (input type=date)
const customTo = ref("");

const isCustom = computed(() => selectedFilter.value === "Tùy chỉnh");

// Trả về { from, to } gửi lên BE, hoặc null nếu chưa chọn đủ ngày
const getCustomRange = () => {
  if (!customFrom.value || !customTo.value) return null;
  return {
    from: `${customFrom.value}T00:00:00`,
    to: `${customTo.value}T23:59:59`,
  };
};

////////////// LOAD TRẠNG THÁI HÓA ĐƠN ////////////////

////////////// LOAD TRẠNG THÁI HÓA ĐƠN ////////////////

const orderStatus = ref({
  labels: [
    "Vừa tạo",
    "Chờ xác nhận",
    "Xác nhận",
    "Phục vụ",
    "Chờ thanh toán",
    "Đã thanh toán",
    "Hoàn thành",
    "Hủy",
    "Treo nợ",
    "Nợ xấu",
  ],
  datasets: [
    {
      data: [],
      backgroundColor: [
        "#94a3b8",
        "#f59e0b",
        "#3b82f6",
        "#f97316",
        "#a855f7",
        "#22c55e",
        "#10b981",
        "#ef4444",
        "#facc15",
        "#b91c1c",
      ],
      borderWidth: 2,
      borderColor: "#ffffff",
      borderRadius: 6,
      spacing: 2,
    },
  ],
});

const applyOrderStatus = (data) => {
  orderStatus.value = {
    ...orderStatus.value,
    datasets: [
      {
        ...orderStatus.value.datasets[0],
        data: [
          data.vuaTao,
          data.choXacNhan,
          data.xacNhan,
          data.phucVu,
          data.choThanhToan,
          data.daThanhToan,
          data.hoanThanh,
          data.huy,
          data.treoNo,
          data.noXau,
        ],
      },
    ],
  };
};

const loadOrderStatus = async () => {
  if (isCustom.value) {
    const range = getCustomRange();
    if (!range) return;
    try {
      const res = await getTrangThaiHoaDon(type.value, range.from, range.to);
      applyOrderStatus(res.data);
    } catch (err) {
      console.log(err);
    }
    return;
  }

  try {
    const res = await getTrangThaiHoaDon(type.value);
    applyOrderStatus(res.data);
  } catch (err) {
    console.log(err);
  }
};

const pieOptions = {
  responsive: true,
  maintainAspectRatio: false,
  cutout: "70%",
  plugins: {
    legend: {
      position: "bottom",
      labels: {
        boxWidth: 8,
        boxHeight: 8,
        usePointStyle: true,
        padding: 16,
        font: {
          size: 11,
          weight: "500",
        },

        generateLabels: function (chart) {
          const data = chart.data;
          if (data.labels.length && data.datasets.length) {
            return data.labels.map((label, i) => ({
              text: label,
              fillStyle: data.datasets[0].backgroundColor[i],
              hidden: false,
              index: i,
            }));
          }
          return [];
        },
      },
    },
    tooltip: {
      backgroundColor: "rgba(15, 23, 42, 0.9)",
      titleFont: { size: 13, weight: "600" },
      bodyFont: { size: 12 },
      padding: 10,
      cornerRadius: 8,
      displayColors: true,
    },
  },
};

/* FORMAT MONEY */
const formatMoney = (v) => (v ?? 0).toLocaleString("vi-VN") + " đ";

// Doanh thu theo trục (đổi tên biến nhưng giữ logic load tương tự bản cũ)
const doanhThuThang = ref([]);
const loadDoanhThuThang = async () => {
  if (isCustom.value) {
    const range = getCustomRange();
    if (!range) return;
    try {
      const res = await getDoanhThuThang(type.value, range.from, range.to);
      doanhThuThang.value = res.data;
    } catch (err) {
      console.error("Lỗi load doanh thu theo trục", err);
    }
    return;
  }

  try {
    const res = await getDoanhThuThang(type.value);
    doanhThuThang.value = res.data;
  } catch (err) {
    console.error("Lỗi load doanh thu theo trục", err);
  }
};

// Bộ lọc doanh thu
const data = ref({});
const loadBoLoc = async () => {
  if (isCustom.value) {
    const range = getCustomRange();
    if (!range) return;
    try {
      const res = await getDoanhThuLoc(type.value, range.from, range.to);
      data.value = res.data;
    } catch (err) {
      console.error(err);
    }
    return;
  }

  try {
    const res = await getDoanhThuLoc(type.value);
    data.value = res.data;
  } catch (err) {
    console.error(err);
  }
};

/* GỌI LẠI TẤT CẢ PHẦN PHỤ THUỘC FILTER */
const loadAllByFilter = async () => {
  selectedNgay.value = null;
  chiTietNgay.value = [];
  tablePage.value = 1;
  await Promise.all([
    loadBoLoc(),
    loadDoanhThuThang(),
    loadOrderStatus(),
    loadBookingChannel(),
    loadTopSet(),
    loadDoanhThuNgay(),
  ]);
};

const handleFilter = async (f) => {
  selectedFilter.value = f;

  const apiType = mapFilterToType[f];
  if (!apiType) return;

  type.value = apiType;

  // Tùy chỉnh: chờ người dùng chọn đủ from-to rồi mới gọi (nút "Áp dụng" bên dưới)
  if (apiType === "TUY_CHINH") return;

  await loadAllByFilter();
};

const handleApplyCustomRange = async () => {
  if (!customFrom.value || !customTo.value) {
    notify.error("Vui lòng chọn đủ khoảng ngày");
    return;
  }
  if (customFrom.value > customTo.value) {
    notify.error("Ngày bắt đầu phải trước ngày kết thúc");
    return;
  }
  await loadAllByFilter();
};

// Thống kê hôm nay - tuần - tháng - năm (KHÔNG đổi theo filter)

const thongKe = ref({
  today: 0,
  yesterday: 0,

  week: 0,
  lastWeek: 0,

  month: 0,
  lastMonth: 0,

  year: 0,
  lastYear: 0,

  duKienToday: 0,
  duKienWeek: 0,
  duKienMonth: 0,
  duKienYear: 0,
});

const loadThongKe = async () => {
  try {
    const res = await getThongKeDoanhThu();
    const data = res.data;

    thongKe.value = {
      today: data.doanhThuHomNay,
      yesterday: data.doanhThuHomQua,

      week: data.doanhThuTuanNay,
      lastWeek: data.doanhThuTuanTruoc,

      month: data.doanhThuThangNay,
      lastMonth: data.doanhThuThangTruoc,

      year: data.doanhThuNamNay,
      lastYear: data.doanhThuNamTruoc,

      duKienToday: data.doanhThuDuKienHomNay,
      duKienWeek: data.doanhThuDuKienTuanNay,
      duKienMonth: data.doanhThuDuKienThangNay,
      duKienYear: data.doanhThuDuKienNamNay,
    };
  } catch (e) {
    console.log("Lỗi load thống kê", e);
  }
};

const calcPercent = (cur, prev) => {
  cur = Number(cur) || 0;
  prev = Number(prev) || 0;
  if (prev === 0) return cur === 0 ? 0 : 100;

  return Number((((cur - prev) / prev) * 100).toFixed(1));
};
const animatedThongKe = ref({
  today: 0,
  yesterday: 0,
  week: 0,
  lastWeek: 0,
  month: 0,
  lastMonth: 0,
  year: 0,
  lastYear: 0,
  duKienToday: 0,
  duKienWeek: 0,
  duKienMonth: 0,
  duKienYear: 0,
});

const animatedData = ref({
  tongHoaDon: 0,
  trungBinhDon: 0,
  tienCoc: 0,
  giamGia: 0,
  huyDon: 0,
  thucNhan: 0,
  giamTrucTiep: 0,
  diemTichLuySuDung: 0,
  soDonTreoNo: 0,
  tongTienConNo: 0,
});

watch(
  () => thongKe.value,
  (newThongKe) => {
    if (!newThongKe) return;
    Object.keys(animatedThongKe.value).forEach((key) => {
      const start = animatedThongKe.value[key] || 0;
      const end = newThongKe[key] || 0;
      if (start === end) return;
      const duration = 1200;
      let startTimestamp = null;
      const step = (timestamp) => {
        if (!startTimestamp) startTimestamp = timestamp;
        const progress = Math.min((timestamp - startTimestamp) / duration, 1);
        const easeProgress = 1 - Math.pow(1 - progress, 4);
        animatedThongKe.value[key] = Math.round(
          start + easeProgress * (end - start),
        );
        if (progress < 1) {
          window.requestAnimationFrame(step);
        } else {
          animatedThongKe.value[key] = end;
        }
      };
      window.requestAnimationFrame(step);
    });
  },
  { deep: true },
);

watch(
  () => data.value,
  (newData) => {
    if (!newData) return;
    Object.keys(animatedData.value).forEach((key) => {
      const start = animatedData.value[key] || 0;
      const end = newData[key] || 0;
      if (start === end) return;
      const duration = 1000;
      let startTimestamp = null;
      const step = (timestamp) => {
        if (!startTimestamp) startTimestamp = timestamp;
        const progress = Math.min((timestamp - startTimestamp) / duration, 1);
        const easeProgress = 1 - Math.pow(1 - progress, 4);
        animatedData.value[key] = Math.round(
          start + easeProgress * (end - start),
        );
        if (progress < 1) {
          window.requestAnimationFrame(step);
        } else {
          animatedData.value[key] = end;
        }
      };
      window.requestAnimationFrame(step);
    });
  },
  { deep: true },
);

onMounted(() => {
  loadThongKe();
  loadAllByFilter();
});

/* LINE CHART */

const lineData = computed(() => ({
  labels: doanhThuThang.value.map((i) => i.label ?? "T" + i.thang),
  datasets: [
    {
      label: "Doanh thu",
      data: doanhThuThang.value.map((i) => i.doanhThu),
      borderColor: "#e11d48",
      borderWidth: 3,
      backgroundColor: "rgba(225, 29, 72, 0.05)",
      fill: true,
      tension: 0.4,
      pointBackgroundColor: "#ffffff",
      pointBorderColor: "#e11d48",
      pointBorderWidth: 2,
      pointRadius: 4,
      pointHoverRadius: 6,
      pointHoverBackgroundColor: "#e11d48",
      pointHoverBorderColor: "#ffffff",
      pointHoverBorderWidth: 2,
    },
  ],
}));

const lineOptions = {
  responsive: true,
  maintainAspectRatio: false,
  resizeDelay: 200,
  plugins: {
    legend: {
      display: false,
    },
    tooltip: {
      backgroundColor: "rgba(15, 23, 42, 0.9)",
      titleFont: { size: 13, weight: "600" },
      bodyFont: { size: 12 },
      padding: 12,
      cornerRadius: 8,
      displayColors: false,
      callbacks: {
        label: function (context) {
          let label = context.dataset.label || "";
          if (label) {
            label += ": ";
          }
          if (context.parsed.y !== null) {
            label += new Intl.NumberFormat("vi-VN", {
              style: "currency",
              currency: "VND",
            }).format(context.parsed.y);
          }
          return label;
        },
      },
    },
  },
  scales: {
    x: {
      grid: {
        display: false,
      },
      ticks: {
        color: "#64748b",
        font: {
          size: 11,
          weight: "500",
        },
      },
    },
    y: {
      grid: {
        color: "#f1f5f9",
        drawBorder: false,
      },
      ticks: {
        color: "#64748b",
        font: {
          size: 11,
          weight: "500",
        },
        callback: function (value) {
          if (value >= 1e6) {
            return (value / 1e6).toFixed(0) + " Tr đ";
          }
          return value.toLocaleString("vi-VN") + " đ";
        },
      },
    },
  },
};

/* BOOKING CHANNEL */
const rawBookingData = ref({ online: 0, taiQuay: 0 });

const bookingChannelChartData = computed(() => ({
  labels: ["Online", "Tại quầy"],
  datasets: [
    {
      data: [rawBookingData.value.online, rawBookingData.value.taiQuay],
      backgroundColor: ["#e11d48", "#f97316"],
      borderWidth: 2,
      borderColor: "#ffffff",
      borderRadius: 6,
      spacing: 2,
    },
  ],
}));
const bookingChannelOptions = {
  responsive: true,
  maintainAspectRatio: false,
  cutout: "70%",
  plugins: {
    legend: {
      position: "bottom",
      align: "center",
      labels: {
        boxWidth: 8,
        boxHeight: 8,
        usePointStyle: true,
        padding: 16,
        font: {
          size: 12,
          weight: "500",
        },
      },
    },
    tooltip: {
      backgroundColor: "rgba(15, 23, 42, 0.9)",
      titleFont: { size: 13, weight: "600" },
      bodyFont: { size: 12 },
      padding: 10,
      cornerRadius: 8,
      displayColors: true,
    },
  },
};

const loadBookingChannel = async () => {
  if (isCustom.value) {
    const range = getCustomRange();
    if (!range) return;
    try {
      const res = await getBookingChannel(type.value, range.from, range.to);
      rawBookingData.value = res.data;
    } catch (err) {
      console.error("Lỗi booking channel", err);
    }
    return;
  }

  try {
    const res = await getBookingChannel(type.value);
    rawBookingData.value = res.data;
  } catch (err) {
    console.error("Lỗi booking channel", err);
  }
};

/* TOP SET */

const topSet = ref([]);

const loadTopSet = async () => {
  if (isCustom.value) {
    const range = getCustomRange();
    if (!range) return;
    try {
      const res = await getTopSetBanChay(type.value, range.from, range.to);
      topSet.value = res.data;
    } catch (err) {
      console.error("Lỗi load top set", err);
    }
    return;
  }

  try {
    const res = await getTopSetBanChay(type.value);
    topSet.value = res.data;
  } catch (err) {
    console.error("Lỗi load top set", err);
  }
};

const getImageUrl = (img) => {
  if (!img) return "https://via.placeholder.com/50";

  return `http://localhost:8080/uploads/${img}`;
};

const handleExportThongKe = async () => {
  try {
    nprogress.start();
    const res = await exportThongKeExcel();
    const blob = new Blob([res.data], {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute(
      "download",
      `thong_ke_${new Date().toISOString().slice(0, 10)}.xlsx`,
    );
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
    notify.success("Xuất excel thành công");
  } catch (e) {
    console.error("Lỗi export thống kê:", e);
    notify.error("Xuất Excel thất bại");
  } finally {
    nprogress.done();
  }
};
const activeChartTab = ref("bieu-do");

/////////////////// TABLE /////////////////
const TRANG_THAI_MAP = {
  0: "Vừa tạo",
  1: "Chờ xác nhận",
  2: "Chờ xác nhận",
  3: "Đã xác nhận",
  4: "Đã xác nhận",
  5: "Chờ hoàn cọc",
  6: "Đang phục vụ",
  7: "Chờ thanh toán",
  8: "Đã thanh toán",
  9: "Hoàn thành",
  10: "Treo nợ",
  11: "Nợ xấu",
  [-1]: "Đã hủy",
};

const doanhThuNgay = ref([]);
const chiTietNgay = ref([]);

const loadDoanhThuNgay = async () => {
  if (isCustom.value) {
    const range = getCustomRange();
    if (!range) return;
    try {
      const res = await getThongKeTheoNgay(type.value, range.from, range.to);
      doanhThuNgay.value = res.data;
    } catch (err) {
      console.error("Lỗi load doanh thu theo ngày", err);
    }
    return;
  }

  try {
    const res = await getThongKeTheoNgay(type.value);
    doanhThuNgay.value = res.data;
  } catch (err) {
    console.error("Lỗi load doanh thu theo ngày", err);
  }
};

const viewChiTietNgay = async (row) => {
  try {
    const res = await getHoaDonTheoNgay(row.ngay);
    chiTietNgay.value = res.data.map((item) => ({
      ...item,
      trangThaiText: TRANG_THAI_MAP[item.trangThai] ?? "Đã hủy",
    }));
    selectedNgay.value = row;
    tablePage.value = 1;
  } catch (err) {
    console.error("Lỗi load chi tiết ngày", err);
  }
};

const backToDoanhThuNgay = () => {
  selectedNgay.value = null;
  chiTietNgay.value = [];
  tablePage.value = 1;
};

const formatDate = (d) => {
  if (!d) return "";
  const [y, m, day] = d.split("-");
  return `${day}/${m}/${y}`;
};

const selectedNgay = ref(null);

const getStatusClass = (status) => {
  if (!status) return "status-cancelled";
  const s = status.toString().trim();
  switch (s) {
    case "Vừa tạo":
      return "status-created";
    case "Chờ khách cọc":
      return "status-wait-deposit";
    case "Chờ thanh toán cọc":
      return "status-wait-deposit";
    case "Đã cọc":
      return "status-deposited";
    case "Đã xác nhận":
      return "status-confirmed";
    case "Chờ xác nhận":
      return "status-waiting";
    case "Chờ hoàn cọc":
      return "status-arrived";
    case "Đang phục vụ":
      return "status-serving";
    case "Chờ thanh toán":
      return "status-wait-payment";
    case "Đã thanh toán":
      return "status-paid";
    case "Hoàn thành":
      return "status-completed";
    case "Đã hủy":
      return "status-cancelled";
    case "Treo nợ":
      return "status-debt";
    case "Nợ xấu":
      return "status-bad-debt";
    default:
      return "status-cancelled";
  }
};

//////////////////////////// PHÂN TRANG  //////////////////////////////
const tablePage = ref(1);
const tablePageSize = 5;

const pagedDoanhThuNgay = computed(() => {
  const start = (tablePage.value - 1) * tablePageSize;
  return doanhThuNgay.value.slice(start, start + tablePageSize);
});

const doanhThuNgayTotalPages = computed(() =>
  Math.ceil(doanhThuNgay.value.length / tablePageSize),
);

const pagedChiTietNgay = computed(() => {
  const start = (tablePage.value - 1) * tablePageSize;
  return chiTietNgay.value.slice(start, start + tablePageSize);
});

const chiTietNgayTotalPages = computed(() =>
  Math.ceil(chiTietNgay.value.length / tablePageSize),
);

// Tổng trang tùy theo đang xem bảng nào
const currentTableTotalPages = computed(() =>
  selectedNgay.value
    ? chiTietNgayTotalPages.value
    : doanhThuNgayTotalPages.value,
);

const tablePageList = computed(() => {
  const total = currentTableTotalPages.value;
  if (total <= 5) return Array.from({ length: total }, (_, i) => i + 1);
  const pages = [1, 2, 3];
  if (total > 4) pages.push("…");
  pages.push(total);
  return pages;
});
</script>

<template>
  <div class="lau-page animate-fade-slide-in">
    <div class="box">
      <div class="box-header">
        <h2 class="main-title">
          <span class="icon-wrapper">
            <img src="@/assets/icons/baocao.png" alt="Báo cáo" />
          </span>
          Thống Kê Doanh Thu
        </h2>
        <p>Phân tích doanh thu & hoạt động đặt lịch hệ thống FourSpiceHotpot</p>
      </div>
    </div>

    <!-- CARD -->

    <div class="card-grid">
      <div class="stat-card today">
        <div class="card-top">
          <div>
            <span class="card-label">Doanh thu hôm nay</span>
            <h3>{{ formatMoney(animatedThongKe.today) }}</h3>
          </div>
          <div class="card-icon">D</div>
        </div>

        <div class="card-bottom">
          <span
            class="trend"
            :class="
              calcPercent(thongKe.today, thongKe.yesterday) >= 0 ? 'up' : 'down'
            "
          >
            {{ calcPercent(thongKe.today, thongKe.yesterday) }}% - So với hôm
            qua
          </span>

          <h1 class="du-kien">
            Dự kiến: {{ formatMoney(animatedThongKe.duKienToday) }}
          </h1>
        </div>
      </div>

      <div class="stat-card week">
        <div class="card-top">
          <div>
            <span class="card-label">Doanh thu tuần này</span>
            <h3>{{ formatMoney(animatedThongKe.week) }}</h3>
          </div>
          <div class="card-icon">7N</div>
        </div>

        <div class="card-bottom">
          <span
            class="trend"
            :class="
              calcPercent(thongKe.week, thongKe.lastWeek) >= 0 ? 'up' : 'down'
            "
          >
            {{ calcPercent(thongKe.week, thongKe.lastWeek) }}% - 7 ngày gần nhất
          </span>

          <p class="du-kien">
            Dự kiến: {{ formatMoney(animatedThongKe.duKienWeek) }}
          </p>
        </div>
      </div>

      <div class="stat-card month">
        <div class="card-top">
          <div>
            <span class="card-label">Doanh thu tháng này</span>
            <h3>{{ formatMoney(animatedThongKe.month) }}</h3>
          </div>
          <div class="card-icon">30</div>
        </div>

        <div class="card-bottom">
          <span
            class="trend"
            :class="
              calcPercent(thongKe.month, thongKe.lastMonth) >= 0 ? 'up' : 'down'
            "
          >
            {{ calcPercent(thongKe.month, thongKe.lastMonth) }}% - So với tháng
            trước
          </span>

          <p class="du-kien">
            Dự kiến: {{ formatMoney(animatedThongKe.duKienMonth) }}
          </p>
        </div>
      </div>

      <div class="stat-card year">
        <div class="card-top">
          <div>
            <span class="card-label">Doanh thu năm nay</span>
            <h3>{{ formatMoney(animatedThongKe.year) }}</h3>
          </div>
          <div class="card-icon">Y</div>
        </div>

        <div class="card-bottom">
          <span
            class="trend"
            :class="
              calcPercent(thongKe.year, thongKe.lastYear) >= 0 ? 'up' : 'down'
            "
          >
            {{ calcPercent(thongKe.year, thongKe.lastYear) }}% - Năm
            {{ new Date().getFullYear() }}
          </span>

          <p class="du-kien">
            Dự kiến: {{ formatMoney(animatedThongKe.duKienYear) }}
          </p>
        </div>
      </div>
    </div>

    <div class="box filter-container">
      <!-- TITLE -->
      <div class="filter-title">
        <h4>
          <span class="section-icon-wrapper section-icon--red">
            <i class="bi bi-funnel-fill"></i>
          </span>
          Bộ lọc doanh thu
        </h4>
        <p>
          Chọn khoảng thời gian để xem báo cáo (áp dụng cho biểu đồ, top món,
          trạng thái &amp; kênh bên dưới)
        </p>
      </div>

      <!-- BUTTON -->
      <div class="filter-actions">
        <button
          v-for="f in filters"
          :key="f"
          :class="{ active: selectedFilter === f }"
          @click="handleFilter(f)"
        >
          {{ f }}
        </button>
        <button class="excel" @click="handleExportThongKe()">Xuất excel</button>
      </div>

      <!-- TÙY CHỈNH: chọn ngày -->
      <div v-if="isCustom" class="custom-range">
        <label>
          Từ ngày
          <input type="date" v-model="customFrom" />
        </label>
        <label>
          Đến ngày
          <input type="date" v-model="customTo" />
        </label>
        <button class="apply" @click="handleApplyCustomRange">Áp dụng</button>
      </div>
      <!-- STATS -->
      <div class="stats-row-bottom">
        <div class="mini-stat">
          <span>Tổng hóa đơn</span>
          <b>{{ animatedData.tongHoaDon || 0 }}</b>
        </div>

        <div class="mini-stat">
          <span>Trung bình/đơn</span>
          <b>{{ formatMoney(animatedData.trungBinhDon || 0) }}</b>
        </div>

        <div class="mini-stat blue">
          <span>Tiền cọc</span>
          <b>{{ formatMoney(animatedData.tienCoc || 0) }}</b>
        </div>

        <div class="mini-stat orange">
          <span>Giảm giá</span>
          <b>{{ formatMoney(animatedData.giamGia || 0) }}</b>
        </div>

        <div class="mini-stat">
          <span>Hủy đơn</span>
          <b>{{ animatedData.huyDon || 0 }}</b>
        </div>

        <div class="mini-stat red">
          <span>Thực nhận</span>
          <b>{{ formatMoney(animatedData.thucNhan || 0) }}</b>
        </div>

        <div class="mini-stat orange">
          <span>Giảm trực tiếp</span>
          <b>{{ formatMoney(animatedData.giamTrucTiep || 0) }}</b>
        </div>
        <div class="mini-stat blue">
          <span>Điểm tích lũy dùng</span>
          <b>{{ formatMoney(animatedData.diemTichLuySuDung || 0) }}</b>
        </div>

        <div class="mini-stat">
          <span>Đơn treo nợ</span>
          <b>{{ animatedData.soDonTreoNo || 0 }}</b>
        </div>

        <div class="mini-stat red">
          <span>Tiền còn nợ</span>
          <b>{{ formatMoney(animatedData.tongTienConNo || 0) }}</b>
        </div>
      </div>
    </div>
    <!-- CHART -->

    <div class="chart-grid">
      <div class="box chart-box chart-box--wide">
        <!-- TABS -->
        <div class="chart-tabs">
          <button
            :class="{ 'tab-active': activeChartTab === 'bieu-do' }"
            @click="activeChartTab = 'bieu-do'"
          >
            <i class="bi bi-bar-chart-line"></i> Biểu đồ doanh thu
          </button>
          <button
            :class="{ 'tab-active': activeChartTab === 'bang' }"
            @click="activeChartTab = 'bang'"
          >
            <i class="bi bi-table"></i> Doanh thu theo thời gian
          </button>
        </div>

        <!-- TAB: Biểu đồ -->
        <div v-if="activeChartTab === 'bieu-do'">
          <div class="box-header" style="margin-bottom: 12px">
            <h4>Biểu đồ doanh thu ({{ selectedFilter }})</h4>
          </div>
          <div class="chart-wrap">
            <Line :data="lineData" :options="lineOptions" />
          </div>
        </div>

        <!-- TAB: Bảng doanh thu theo thời gian -->
        <div v-else>
          <div
            class="box-header"
            style="margin-bottom: 12px; position: relative"
          >
            <h4>Doanh thu theo thời gian ({{ selectedFilter }})</h4>
            <button
              v-if="selectedNgay"
              @click="backToDoanhThuNgay"
              style="
                position: absolute;
                right: 0;
                top: 50%;
                transform: translateY(-50%);
                padding: 4px 8px;
                border-radius: 4px;
                border: 1px solid #ddd;
                background: #f8f9fa;
                cursor: pointer;
                color: #333;
              "
              title="Quay lại"
            >
              <i class="bi bi-arrow-left" style="font-size: 1.2rem"></i>
            </button>
          </div>
          <div class="revenue-table-wrap">
            <!-- Bảng Doanh Thu Ngày -->
            <table class="lau-table revenue-table" v-if="!selectedNgay">
              <thead>
                <tr>
                  <th>Ngày</th>
                  <th>Tổng Đơn Hàng</th>
                  <th>Tổng Tiền Cọc</th>
                  <th>Tổng Tiền</th>
                  <th>Tổng Đơn Hủy</th>
                  <th style="text-align: center">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="doanhThuNgay.length === 0">
                  <td colspan="6" style="text-align: center; color: #aaa">
                    Không có dữ liệu
                  </td>
                </tr>
                <tr v-for="(row, i) in pagedDoanhThuNgay" :key="i">
                  <td>{{ formatDate(row.ngay) }}</td>
                  <td>{{ row.tongDonHang }}</td>
                  <td>{{ formatMoney(row.tongTienCoc) }}</td>
                  <td class="text-red">{{ formatMoney(row.tongTien) }}</td>
                  <td>{{ row.tongDonHuy }}</td>
                  <td style="text-align: center">
                    <button
                      @click="viewChiTietNgay(row)"
                      class="btn-action-icon"
                      title="Xem chi tiết"
                      v-tooltip="'Chỉnh sửa'"
                    >
                      <img :src="iconEye" class="img-icon-action" />
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>

            <!-- Bảng Chi Tiết Ngày -->
            <table class="lau-table revenue-table" v-else>
              <thead>
                <tr>
                  <th>STT</th>
                  <th>Mã Hóa Đơn</th>
                  <th>Khách Hàng</th>
                  <th>Tiền cọc</th>
                  <th>Tổng tiền</th>
                  <th style="text-align: center">Trạng thái</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="chiTietNgay.length === 0">
                  <td colspan="6" style="text-align: center; color: #aaa">
                    Không có dữ liệu
                  </td>
                </tr>
                <tr v-for="(row, i) in pagedChiTietNgay" :key="i">
                  <td>{{ (tablePage - 1) * tablePageSize + i + 1 }}</td>
                  <td>{{ row.maHoaDon }}</td>
                  <td>{{ row.tenKhachHang }}</td>
                  <td>{{ formatMoney(row.tienCoc) }}</td>
                  <td class="text-red">{{ formatMoney(row.tongTien) }}</td>
                  <td style="text-align: center">
                    <span
                      class="status-text"
                      :class="getStatusClass(row.trangThaiText)"
                    >
                      {{ row.trangThaiText }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-if="currentTableTotalPages > 1" class="pagination mt-2">
            <button
              :disabled="tablePage === 1"
              @click="tablePage--"
              class="pg-btn pg-arrow"
            >
              <i class="bi bi-chevron-left"></i>
            </button>
            <template v-for="p in tablePageList" :key="p">
              <button v-if="p === '…'" class="pg-btn pg-ellipsis" disabled>
                ···
              </button>
              <button
                v-else
                class="pg-btn"
                :class="{ active: p === tablePage }"
                @click="tablePage = p"
              >
                {{ p }}
              </button>
            </template>
            <button
              :disabled="tablePage === currentTableTotalPages"
              @click="tablePage++"
              class="pg-btn pg-arrow"
            >
              <i class="bi bi-chevron-right"></i>
            </button>
          </div>
        </div>
      </div>

      <div class="box chart-box">
        <div class="box-header">
          <h4>
            <span class="section-icon-wrapper section-icon--blue">
              <i class="bi bi-clipboard2-data-fill"></i>
            </span>
            Trạng thái đơn hàng
          </h4>
        </div>

        <div class="chart-wrap pie-wrap">
          <Doughnut
            v-if="!isOrderStatusEmpty"
            :data="orderStatus"
            :options="pieOptions"
          />
          <div v-else class="empty-donut">
            <div class="empty-donut-ring"></div>
            <span class="empty-donut-text">Không có dữ liệu</span>
          </div>
        </div>
      </div>

      <div class="box chart-box">
        <div class="box-header">
          <h4>
            <span class="section-icon-wrapper section-icon--purple">
              <i class="bi bi-pie-chart-fill"></i>
            </span>
            Phân phối kênh đặt lịch
          </h4>
        </div>
        <div class="chart-wrap pie-wrap">
          <Doughnut
            v-if="!isBookingChannelEmpty"
            :data="bookingChannelChartData"
            :options="bookingChannelOptions"
          />
          <div v-else class="empty-donut">
            <div class="empty-donut-ring"></div>
            <span class="empty-donut-text">Không có dữ liệu</span>
          </div>
        </div>
      </div>
    </div>

    <!-- TOP SET - full width -->
    <div class="box">
      <div class="box-header">
        <h4>
          <span class="section-icon-wrapper section-icon--gold">
            <i class="bi bi-trophy-fill"></i>
          </span>
          Top 5 Set Lẩu Bán Chạy
        </h4>
      </div>

      <table class="lau-table">
        <thead>
          <tr>
            <th>#</th>
            <th>Hình ảnh</th>
            <th>Tên Set</th>
            <th>Giá</th>
            <th>Bán</th>
          </tr>
        </thead>

        <tbody>
          <tr v-if="topSet.length === 0">
            <td colspan="5" style="text-align: center">Không có dữ liệu</td>
          </tr>

          <tr v-for="(item, index) in topSet" :key="index">
            <td>{{ index + 1 }}</td>
            <td>
              <div class="food-item">
                <img :src="getImageUrl(item.hinhAnh)" class="food-img" />
              </div>
            </td>
            <td>{{ item.ten }}</td>
            <td>{{ formatMoney(item.gia || 0) }}</td>
            <td>{{ item.ban || 0 }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.du-kien {
  font-size: 20px;
  opacity: 0.85;
  margin-top: 6px;
}
.lau-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* Keyframes for sliding content from left to right on page load */
.animate-fade-slide-in {
  animation: slideInFromLeft 0.5s cubic-bezier(0.25, 1, 0.5, 1) forwards;
}

@keyframes slideInFromLeft {
  from {
    opacity: 0;
    transform: translateX(-40px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* BOX */

.box {
  background: #fff;
  border: 1px solid #ececec;
  border-radius: 7px;
  padding: 22px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.05);
}

.box-header {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.box-header h4 {
  font-size: 20px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
}

/* Section icon wrapper - border style giống header */
.section-icon-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  flex-shrink: 0;
  font-size: 16px;
}

.section-icon--blue {
  background: linear-gradient(
    135deg,
    rgba(59, 130, 246, 0.1),
    rgba(59, 130, 246, 0.2)
  );
  border: 1px solid rgba(59, 130, 246, 0.25);
  color: #3b82f6;
}

.section-icon--purple {
  background: linear-gradient(
    135deg,
    rgba(168, 85, 247, 0.1),
    rgba(168, 85, 247, 0.2)
  );
  border: 1px solid rgba(168, 85, 247, 0.25);
  color: #a855f7;
}

.section-icon--gold {
  background: linear-gradient(
    135deg,
    rgba(234, 179, 8, 0.1),
    rgba(234, 179, 8, 0.2)
  );
  border: 1px solid rgba(234, 179, 8, 0.3);
  color: #ca8a04;
}

.section-icon--red {
  background: linear-gradient(
    135deg,
    rgba(211, 47, 47, 0.08),
    rgba(211, 47, 47, 0.18)
  );
  border: 1px solid rgba(211, 47, 47, 0.25);
  color: #d32f2f;
}

.header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-flex.no-border {
  border: none;
}
.header-section {
  width: 100%;
}
.title-group {
  display: flex;
  align-items: center;
  gap: 16px;
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
.icon-wrapper img {
  width: 27px;
  height: 27px;
  object-fit: contain;
}
.icon-title {
  width: 28px;
  height: 28px;
  object-fit: contain;
}

/* Font chữ to 32px, đậm 500 chuẩn form */
.main-title {
  display: flex;
  align-items: center;
  font-size: 35px;
  font-weight: 600;
  gap: 12px;
  letter-spacing: 1px;
  color: #1a1a1a;
}
.sub-title {
  margin-top: 6px;
  font-size: 14px;
  line-height: 1.6;
  color: #6b7280;
}

/* CARD */

/* CARD REDESIGN - MINIMAL LUXURY SLATE */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
}

.stat-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 150px;
  position: relative;
  transition: all 0.2s ease-in-out;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.05);
}

/* Colored top border accents */
.stat-card.today {
  border-top: 3px solid #ef4444;
}
.stat-card.week {
  border-top: 3px solid #f59e0b;
}
.stat-card.month {
  border-top: 3px solid #3b82f6;
}
.stat-card.year {
  border-top: 3px solid #10b981;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.card-label {
  font-size: 12px;
  font-weight: 600;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  display: block;
  margin-bottom: 4px;
}

.stat-card h3 {
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
}

.card-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 700;
}

/* Minimalist indicator tags instead of full background */
.stat-card.today .card-icon {
  background: #fff1f2;
  color: #f43f5e;
}
.stat-card.week .card-icon {
  background: #fffbeb;
  color: #d97706;
}
.stat-card.month .card-icon {
  background: #eff6ff;
  color: #2563eb;
}
.stat-card.year .card-icon {
  background: #f0fdf4;
  color: #16a34a;
}

.card-bottom {
  margin-top: 14px;
  border-top: 1px solid #f1f5f9;
  padding-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.trend {
  font-size: 11px;
  font-weight: 600;
}

.trend.up {
  color: #16a34a;
}

.trend.down {
  color: #f43f5e;
}

.stat-card .du-kien {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  margin: 0;
}

/* FILTER */
/* Container chính xếp dọc */

/* CONTAINER */
.filter-container {
  background: #fff;
  border: 1px solid #ececec;
  border-radius: 8px;
  padding: 22px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.05);

  display: flex;
  flex-direction: column;
  gap: 20px;
}
/* TITLE */
.filter-title {
  text-align: left;
}

.filter-title h4 {
  font-size: 23px;
  font-weight: 600;
}

/* BUTTON - DÀN ĐỀU FULL */
.filter-actions {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 10px;
  width: 100%;
}

.filter-actions button {
  width: 100%;
  padding: 8px 14px;
  border-radius: 20px;
  background: #f3f4f6;
  border: 1px solid transparent;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  text-align: center;
  transition: all 0.2s ease;
}

.filter-actions button:hover {
  background: #e9eaec;
  border-color: #d1d5db;
}

/* ACTIVE */
.filter-actions button.active {
  background: #fff5f5;
  border-color: #ef4444;
  color: #ef4444;
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(239, 68, 68, 0.12);
}

/* EXCEL */
.filter-actions .excel {
  border: 1px solid #e5e7eb;
  background: white;
  border-radius: 20px;
  font-weight: 500;
  color: #374151;
}

/* CUSTOM RANGE */
.custom-range {
  display: flex;
  align-items: end;
  gap: 16px;
  flex-wrap: wrap;
  padding-top: 4px;
}

.custom-range label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
}

.custom-range input[type="date"] {
  padding: 7px 10px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  font-size: 13px;
}

.custom-range .apply {
  padding: 8px 18px;
  border-radius: 20px;
  border: none;
  background: #ef4444;
  color: white;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
}

.custom-range .apply:hover {
  background: #dc2626;
}

/* STATS - CHIA ĐỀU */
.stats-row-bottom {
  display: grid;
  grid-template-columns: repeat(10, 1fr);
  border-top: 1px solid #f1f5f9;
  padding-top: 20px;
}

/* ITEM */
.mini-stat {
  display: flex;
  flex-direction: column;
  align-items: center; /* 🔥 căn giữa */
  justify-content: center;
  text-align: center;
  position: relative;
}

/* LINE NGĂN */
.mini-stat:not(:last-child)::after {
  content: "";
  position: absolute;
  right: 0;
  top: 15%;
  height: 70%;
  width: 1px;
  background: #eee;
}

/* TEXT */
.mini-stat span {
  font-size: 11px;
  color: #94a3b8;
  font-weight: 700;
}

.mini-stat b {
  font-size: 18px;
  color: #1e293b;
}

/* MÀU */
.blue b {
  color: #2563eb;
}
.orange b {
  color: #f97316;
}
.red b {
  color: #dc2626;
}

/* Mobile: Nếu màn hình quá nhỏ thì chia làm 2 hoặc 3 cột */

/* responsive khi màn hình nhỏ (ipad/mobile) */

@media (max-width: 600px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.filter-box {
  width: 100%;
}

.filter-header {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 40px;
  align-items: center;
}

.filter-title p {
  margin: 0;
  font-size: 13px;
  color: #6b7280;
}

/* CHART */

.chart-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

@media (max-width: 1000px) {
  .chart-grid {
    grid-template-columns: 1fr;
  }
}

.chart-wrap {
  position: relative;
  height: 300px;
  overflow: hidden;
}
.pie-wrap {
  height: 380px;
}
.pie-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.chart-wrap canvas {
  max-width: 100% !important;
}

.chart-box .box-header {
  margin-bottom: 10px;
}

/* quan trọng nhất */
.chart-wrap {
  overflow: visible;
}

canvas {
  max-width: 100%;
  max-height: 100%;
}

/* ===== CHART TABS ===== */
.chart-box--wide {
  grid-column: 1 / -1;
}

.chart-tabs {
  display: flex;
  gap: 24px;
  margin-bottom: 18px;
  border-bottom: 2px solid #f1f5f9;
  padding-bottom: 0;
}

.chart-tabs button {
  padding: 9px 0;
  border: none;
  background: transparent;
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  border-radius: 0;
  transition: color 0.15s, border-color 0.15s;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 6px;
}

.chart-tabs button:hover {
  color: #e11d48;
}

.chart-tabs button.tab-active {
  color: #e11d48;
  border-bottom-color: #e11d48;
  font-weight: 600;
}

/* ===== REVENUE TABLE ===== */
.revenue-table-wrap {
  max-height: 340px;
  overflow-y: auto;
}

.revenue-table th {
  position: sticky;
  top: 0;
  background: #f9fafb;
  z-index: 1;
}

.text-red {
  color: #e11d48;
  font-weight: 600;
}

/* ===== STATUS BADGE ===== */
.status-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}
.badge-done {
  background: #dcfce7;
  color: #16a34a;
}
.badge-cancel {
  background: #fee2e2;
  color: #dc2626;
}
.badge-pending {
  background: #fef9c3;
  color: #ca8a04;
}
.badge-default {
  background: #f3f4f6;
  color: #6b7280;
}

/* TABLE */

.lau-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 12px;
}

.lau-table th {
  text-align: left;
  background: #f9fafb;
  padding: 14px;
}

.lau-table td {
  padding: 13px;
  border-bottom: 1px solid #f1f5f9;
}
.lau-table tbody tr {
  transition: background 0.15s ease;
}

.lau-table tbody tr:hover {
  background: #fef2f2;
}

@media (max-width: 768px) {
  .title {
    font-size: 22px;
  }

  .stat-card h3 {
    font-size: 22px;
  }
}

.food-item .food-img {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 6px;
  margin-right: 10px;
}

/* CUSTOM ADDITIONAL RESPONSIVE & ZOOM OVERRIDES */
@media (max-width: 1024px) {
  /* Remove stats dividers when stats wrap to avoid visual breaks */
  .mini-stat::after {
    display: none !important;
  }
}

@media (max-width: 991px) {
  .filter-actions {
    grid-template-columns: repeat(3, 1fr) !important;
  }
}

@media (max-width: 768px) {
  .main-title {
    font-size: 24px !important;
  }
  .icon-wrapper {
    width: 45px !important;
    height: 45px !important;
  }
  .icon-wrapper img {
    width: 24px !important;
    height: 24px !important;
  }

  .lau-table {
    display: block !important;
    width: 100% !important;
    overflow-x: auto !important;
    white-space: nowrap !important;
  }
}

@media (max-width: 576px) {
  .filter-actions {
    grid-template-columns: repeat(2, 1fr) !important;
  }
  .chart-wrap {
    height: 250px !important;
  }
  .pie-wrap {
    height: 300px !important;
  }
}

/* ===== STATUS BADGES FROM DONHANG ===== */
.status-text {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 135px;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}
.status-created {
  background: rgba(107, 114, 128, 0.12);
  color: #6b7280;
}
.status-wait-deposit {
  background: rgba(245, 158, 11, 0.12);
  color: #f59e0b;
}
.status-deposited {
  background: rgba(34, 197, 94, 0.12);
  color: #22c55e;
}
.status-confirmed {
  background: rgba(59, 130, 246, 0.12);
  color: #3b82f6;
}
.status-waiting {
  background: rgba(251, 146, 60, 0.12);
  color: #fb923c;
}
.status-arrived {
  background: rgba(14, 165, 233, 0.12);
  color: #0ea5e9;
}
.status-serving {
  background: rgba(139, 92, 246, 0.12);
  color: #8b5cf6;
}
.status-wait-payment {
  background: rgba(234, 88, 12, 0.12);
  color: #ea580c;
}
.status-paid {
  background: rgba(16, 185, 129, 0.12);
  color: #10b981;
}
.status-completed {
  background: rgba(5, 150, 105, 0.12);
  color: #059669;
}
.status-cancelled {
  background: rgba(239, 68, 68, 0.12);
  color: #ef4444;
}

/* ACTION ICONS */
.btn-action-icon {
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 50px;
  background: #f2f5f7;
  cursor: pointer;
  transition: all 0.2s ease;
}
.btn-action-icon:hover {
  background: #eef2ff;
  transform: translateY(-1px);
}
.img-icon-action {
  width: 14px !important;
  height: 14px !important;
  display: block;
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
  border-color: #e11d48;
  color: #e11d48;
  background: #fff1f0;
}
.pg-btn.active {
  border: 1.5px solid #e11d48;
  color: #e11d48;
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

@media (max-width: 900px) {
  .stats-row-bottom {
    grid-template-columns: repeat(3, 1fr);
    row-gap: 16px;
  }
}
@media (max-width: 480px) {
  .stats-row-bottom {
    grid-template-columns: repeat(2, 1fr);
  }
}

.empty-donut {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 10px;
}
.empty-donut-ring {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  border: 24px solid #f1f5f9;
  opacity: 0.7;
}
.empty-donut-text {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 600;
}
</style>
