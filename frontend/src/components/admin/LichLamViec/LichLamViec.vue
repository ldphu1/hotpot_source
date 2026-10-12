<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from "vue";
import { caLamApi } from "@/api/admin/lichlamviec/caLamApi";
import { lichLamViecApi } from "@/api/admin/lichlamviec/lichLamViecApi";

import iconDanhSach from "@/assets/icons/danh-sach.png";
import iconExcel from "@/assets/icons/excel.png";
import { useUtils } from "@/utils/helper";
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";

const { notify } = useUtils();

const isQuanLy = computed(() => localStorage.getItem("role") === "ADMIN");

// Data
const searchQuery = ref("");
const employees = ref([]);
const weekDays = ref([]);
const monthWeeks = ref([]);
const selectedWeekIndex = ref(0);

const updateWeekDays = () => {
  if (
    monthWeeks.value.length > 0 &&
    monthWeeks.value[selectedWeekIndex.value]
  ) {
    weekDays.value = monthWeeks.value[selectedWeekIndex.value].days;
  }
};
const timeSlots = ref([]);
const schedules = ref([]);

const showScheduleDetailsModal = ref(false);
const selectedScheduleDetails = ref(null);

const viewScheduleDetails = (sched) => {
  selectedScheduleDetails.value = sched;
  showScheduleDetailsModal.value = true;
};

const getTrangThaiEnumText = (status) => {
  if (status === 0) return "Đã hủy";
  if (status === 1) return "Chưa tới";
  if (status === 2) return "Sắp tới";
  if (status === 3) return "Trong ca";
  if (status === 4) return "Đã xong";
  return "Chưa xác định";
};

const getViTriText = (viTri) => {
  if (viTri === 0 || viTri === "0") return "Nhân viên";
  if (viTri === 1 || viTri === "1") return "Quản lý";
  if (viTri === 2 || viTri === "2") return "Thu ngân";
  return viTri || "Chưa phân công";
};

const getTrangThaiColorClass = (status) => {
  if (status === 0) return "text-secondary";
  if (status === 1) return "text-info";
  if (status === 2) return "text-warning";
  if (status === 3) return "text-success";
  if (status === 4) return "text-dark";
  return "text-muted";
};

const fetchSchedules = async () => {
  try {
    let tuNgay = "";
    let denNgay = "";
    const year = new Date().getFullYear();

    if (
      viewMode.value === "week" &&
      weekDays.value &&
      weekDays.value.length === 7
    ) {
      const startParts = weekDays.value[0].date.split("/"); // dd/mm
      const endParts = weekDays.value[6].date.split("/");
      tuNgay = `${year}-${startParts[1]}-${startParts[0]}`;
      denNgay = `${year}-${endParts[1]}-${endParts[0]}`;
    } else if (viewMode.value === "month" && selectedMonth.value) {
      tuNgay = `${year}-${selectedMonth.value}-01`;
      const lastDay = new Date(
        year,
        parseInt(selectedMonth.value),
        0,
      ).getDate();
      denNgay = `${year}-${selectedMonth.value}-${
        lastDay < 10 ? "0" + lastDay : lastDay
      }`;
    }

    const params = {};
    if (tuNgay && denNgay) {
      params.tuNgay = tuNgay;
      params.denNgay = denNgay;
    }
    const lichRes = await lichLamViecApi.layDanhSach(params);
    if (lichRes && lichRes.data) {
      schedules.value = lichRes.data.map((lich) => {
        let dateFormatted = "";
        if (lich.ngay) {
          if (Array.isArray(lich.ngay)) {
            const d = lich.ngay[2];
            const m = lich.ngay[1];
            dateFormatted = `${d < 10 ? "0" + d : d}/${m < 10 ? "0" + m : m}`;
          } else {
            const parts = lich.ngay.split("-");
            if (parts.length >= 3) {
              dateFormatted = `${parts[2]}/${parts[1]}`;
            }
          }
        }
        return {
          id: lich.id,
          slotId: lich.idCaLam,
          date: dateFormatted,
          empId: lich.idNhanVien,
          empName: lich.tenNhanVien || "Không tên",
          trangThai: lich.trangThai,
          viTri: getViTriText(lich.viTri),
          gioBatDau: lich.gioBatDau,
          gioKetThuc: lich.gioKetThuc,
          ghiChu: lich.ghiChu || "Không có",
          tenCaLam: lich.tenCaLam,
        };
      });
    }
  } catch (error) {
    console.error("Lỗi khi tải lịch làm việc:", error);
  }
};

const fetchEmployees = async () => {
  try {
    const params = searchQuery.value ? { search: searchQuery.value } : {};
    const empRes = await lichLamViecApi.layDanhSachNhanVien(params);
    if (empRes && empRes.data) {
      employees.value = empRes.data
        .filter((emp) => emp.chucVu !== 1)
        .map((emp) => ({
          id: emp.id,
          name: emp.tenNhanVien || emp.hoTen || "Không tên",
          role:
            emp.chucVu === 0
              ? "Nhân viên"
              : emp.chucVu === 1
              ? "Quản lý"
              : emp.chucVu === 2
              ? "Thu ngân"
              : "Nhân viên",
        }));
    }
  } catch (error) {
    console.error("Lỗi khi tải danh sách nhân viên:", error);
  }
};

watch(searchQuery, () => {
  fetchEmployees();
});

const fetchData = async () => {
  try {
    const [caRes] = await Promise.all([
      caLamApi.layDanhSach(),
      fetchEmployees(),
    ]);

    await fetchSchedules();

    if (caRes && caRes.data) {
      timeSlots.value = caRes.data
        .filter((ca) => ca.trangThai === 1)
        .map((ca) => {
          let tStart = ca.thoiGianBatDau
            ? ca.thoiGianBatDau.substring(0, 5)
            : "";
          let tEnd = ca.thoiGianKetThuc
            ? ca.thoiGianKetThuc.substring(0, 5)
            : "";
          return {
            id: ca.id,
            name: ca.tenCaLam,
            time: `${tStart} - ${tEnd}`,
            type: ca.tenCaLam?.toLowerCase().includes("sáng")
              ? "Sáng"
              : ca.tenCaLam?.toLowerCase().includes("chiều")
              ? "Chiều"
              : "Tối",
            _rawStart: ca.thoiGianBatDau || "23:59:59",
            _rawEnd: ca.thoiGianKetThuc || "23:59:59",
            ngayTao: ca.ngayTao,
          };
        })
        .sort((a, b) => a._rawStart.localeCompare(b._rawStart));
    }
  } catch (error) {
    console.error("Lỗi khi tải dữ liệu lịch làm việc:", error);
  }
};

const isSlotValidForDate = (slot, dateStr) => {
  if (!slot.ngayTao) return true;
  if (!dateStr) return true;
  const [day, month] = dateStr.split("/");
  const year = new Date().getFullYear();
  const dateObj = new Date(year, parseInt(month) - 1, parseInt(day), 0, 0, 0);
  const ngayTaoObj = new Date(slot.ngayTao);
  ngayTaoObj.setHours(0, 0, 0, 0);
  return dateObj >= ngayTaoObj;
};

const isToday = (dateStr) => {
  if (!dateStr) return false;
  const today = new Date();
  const day = String(today.getDate()).padStart(2, "0");
  const month = String(today.getMonth() + 1).padStart(2, "0");
  return dateStr === `${day}/${month}`;
};

let refreshInterval;

onMounted(() => {
  fetchData();
  // Auto-refresh schedule data every 3 seconds (as it was)
  refreshInterval = setInterval(() => {
    fetchSchedules();
  }, 3000);
});

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval);
  }
});

const getEmployeesInSlot = (slotId, date) => {
  return schedules.value.filter((s) => s.slotId === slotId && s.date === date);
};

const isPastShift = (slotId, dateStr) => {
  const slotInfo = timeSlots.value.find((t) => t.id === slotId);
  if (!slotInfo) return false;

  const [day, month] = dateStr.split("/");
  const year = new Date().getFullYear();
  const shiftDate = new Date(`${year}-${month}-${day}T${slotInfo._rawStart}`);
  return new Date() >= shiftDate;
};

const isFinishedShift = (slotId, dateStr) => {
  const slotInfo = timeSlots.value.find((t) => t.id === slotId);
  if (!slotInfo) return false;

  const [day, month] = dateStr.split("/");
  const year = new Date().getFullYear();
  const shiftDate = new Date(`${year}-${month}-${day}T${slotInfo._rawEnd}`);
  return new Date() >= shiftDate;
};

// Drag and drop logic
const onDragStart = (event, emp) => {
  if (!isQuanLy.value) {
    event.preventDefault();
    return;
  }
  event.dataTransfer.setData("empId", emp.id);
  event.dataTransfer.effectAllowed = "copy";
};

const onDrop = async (event, slotId, date) => {
  if (!isQuanLy.value) {
    notify.error(
      "Chỉ nhân viên có chức vụ Quản lý mới được xếp lịch làm việc",
    );
    return;
  }
  const empId = parseInt(event.dataTransfer.getData("empId"));
  if (!empId) return;

  const emp = employees.value.find((e) => e.id === empId);
  if (!emp) return;

  const existingIndex = schedules.value.findIndex(
    (s) => s.slotId === slotId && s.date === date && s.empId === emp.id,
  );
  if (existingIndex > -1) {
    return; // Already in this shift
  }

  const slotInfo = timeSlots.value.find((t) => t.id === slotId);
  if (slotInfo) {
    const shiftDate = new Date(
      `${new Date().getFullYear()}-${date.split("/")[1]}-${
        date.split("/")[0]
      }T${slotInfo._rawStart}`,
    );
    if (new Date() >= shiftDate) {
      notify.error(
        "Không thể xếp ca làm khi giờ bắt đầu đã qua hoặc đang diễn ra",
      );
      return;
    }
  }

  // Call API them
  try {
    const reqData = {
      idNhanVien: emp.id,
      ngay: `${new Date().getFullYear()}-${date.split("/")[1]}-${
        date.split("/")[0]
      }`,
      idCaLam: slotId,
      trangThai: 1,
    };
    const res = await lichLamViecApi.them(reqData);
    if (res.data) {
      schedules.value.push({
        id: res.data.id,
        slotId: res.data.idCaLam,
        date: date,
        empId: res.data.idNhanVien,
        empName: res.data.tenNhanVien || emp.name,
      });
      notify.success("Xếp ca thành công");
    }
  } catch (error) {
    console.error("Lỗi khi thêm ca làm:", error);
    notify.error(
      error.response?.data?.message || error.response?.data || "Lỗi khi xếp ca",
    );
  }
};

const removeSchedule = async (schedId) => {
  if (!isQuanLy.value) {
    notify.error(
      "Chỉ nhân viên có chức vụ Quản lý mới được xếp lịch làm việc",
    );
    return;
  }
  try {
    await lichLamViecApi.xoa(schedId);
    schedules.value = schedules.value.filter((s) => s.id !== schedId);
    notify.success("Xóa ca làm thành công");
  } catch (error) {
    console.error("Lỗi khi xóa ca làm:", error);
    notify.error(
      error.response?.data?.message || error.response?.data || "Lỗi khi xóa ca",
    );
  }
};

const viewMode = ref("week");
const setViewMode = (mode) => {
  viewMode.value = mode;
};

const selectedMonth = ref(
  (new Date().getMonth() + 1).toString().padStart(2, "0"),
);
const monthDays = ref([]);
const generateMonthDays = () => {
  monthDays.value = [];
  const year = new Date().getFullYear();
  const monthIdx = parseInt(selectedMonth.value) - 1;

  const firstDay = new Date(year, monthIdx, 1);
  let startingDayOfWeek = firstDay.getDay();
  let emptyCellsCount = startingDayOfWeek === 0 ? 6 : startingDayOfWeek - 1;

  for (let i = 0; i < emptyCellsCount; i++) {
    monthDays.value.push({ empty: true, date: `empty-${i}` });
  }

  const maxDays = new Date(year, monthIdx + 1, 0).getDate();
  for (let i = 1; i <= maxDays; i++) {
    const dateStr = `${i < 10 ? "0" + i : i}/${selectedMonth.value}`;
    monthDays.value.push({ day: i, date: dateStr, empty: false });
  }

  let currentMonday = new Date(firstDay);
  currentMonday.setDate(firstDay.getDate() - emptyCellsCount);
  monthWeeks.value = [];

  for (let w = 0; w < 6; w++) {
    let weekDates = [];
    for (let d = 0; d < 7; d++) {
      let dayDate = new Date(currentMonday);
      dayDate.setDate(currentMonday.getDate() + w * 7 + d);

      let dayNum = dayDate.getDate();
      let mNum = dayDate.getMonth() + 1;
      const dateStr = `${dayNum < 10 ? "0" + dayNum : dayNum}/${
        mNum < 10 ? "0" + mNum : mNum
      }`;
      const dayNames = [
        "Thứ 2",
        "Thứ 3",
        "Thứ 4",
        "Thứ 5",
        "Thứ 6",
        "Thứ 7",
        "CN",
      ];

      weekDates.push({ name: dayNames[d], date: dateStr, month: mNum - 1 });
    }

    if (weekDates.some((day) => day.month === monthIdx)) {
      monthWeeks.value.push({
        label: `Tuần ${monthWeeks.value.length + 1} (${weekDates[0].date} - ${
          weekDates[6].date
        })`,
        days: weekDates,
      });
    } else {
      break;
    }
  }

  const now = new Date();
  if (year === now.getFullYear() && monthIdx === now.getMonth()) {
    const todayNum = now.getDate();
    let foundIdx = 0;
    monthWeeks.value.forEach((week, idx) => {
      if (
        week.days.some(
          (d) =>
            d.month === monthIdx && parseInt(d.date.split("/")[0]) === todayNum,
        )
      ) {
        foundIdx = idx;
      }
    });
    selectedWeekIndex.value = foundIdx;
  } else {
    selectedWeekIndex.value = 0;
  }
  updateWeekDays();
};
generateMonthDays();

watch(selectedMonth, () => {
  generateMonthDays();
});

watch(selectedWeekIndex, () => {
  updateWeekDays();
});

watch(
  [viewMode, weekDays],
  () => {
    fetchSchedules();
  },
  { deep: true },
);

// Day Modal
const showDayModal = ref(false);
const selectedDayDate = ref(null);
const showQuickAddEmployeeModal = ref(false);

const openDayModal = (dateStr) => {
  selectedDayDate.value = dateStr;
  showDayModal.value = true;
};
const closeDayModal = () => {
  showDayModal.value = false;
  selectedDayDate.value = null;
  activeAddSlotId.value = null;
};

// Add Employee Panel inside Modal
const activeAddSlotId = ref(null);
const selectedEmployeeIds = ref([]);
const employeePickerQuery = ref("");
const openAddEmployeePanel = (slotId) => {
  if (!isQuanLy.value) {
    notify.error(
      "Chỉ nhân viên có chức vụ Quản lý mới được xếp lịch làm việc",
    );
    return;
  }
  activeAddSlotId.value = slotId;
  selectedEmployeeIds.value = [];
  employeePickerQuery.value = "";
};
const openAddEmployeePanelFromWeek = (slotId, dateStr) => {
  if (!isQuanLy.value) {
    notify.error(
      "Chỉ nhân viên có chức vụ Quản lý mới được xếp lịch làm việc",
    );
    return;
  }
  selectedDayDate.value = dateStr;
  activeAddSlotId.value = slotId;
  selectedEmployeeIds.value = [];
  employeePickerQuery.value = "";
  showQuickAddEmployeeModal.value = true;
};
const closeAddEmployeePanel = () => {
  activeAddSlotId.value = null;
  selectedEmployeeIds.value = [];
  employeePickerQuery.value = "";
};
const closeQuickAddEmployeeModal = () => {
  showQuickAddEmployeeModal.value = false;
  selectedDayDate.value = null;
  closeAddEmployeePanel();
};

const dayModalSlots = computed(() => {
  return timeSlots.value;
});

const selectedAddSlot = computed(() =>
  timeSlots.value.find((slot) => slot.id === activeAddSlotId.value),
);

const selectedDayLabel = computed(() => {
  const foundDay = weekDays.value.find(
    (day) => day.date === selectedDayDate.value,
  );
  return foundDay?.name || selectedDayDate.value || "";
});

const availableEmployeesForAdd = computed(() => {
  if (!activeAddSlotId.value || !selectedDayDate.value) {
    return [];
  }

  const assignedIds = getEmployeesInSlot(
    activeAddSlotId.value,
    selectedDayDate.value,
  ).map((sched) => sched.empId);
  const keyword = employeePickerQuery.value.trim().toLowerCase();

  return employees.value.filter((emp) => {
    const matchesKeyword =
      !keyword ||
      emp.name.toLowerCase().includes(keyword) ||
      emp.role.toLowerCase().includes(keyword);

    return !assignedIds.includes(emp.id) && matchesKeyword;
  });
});

const addEmployeesToSlot = async (
  empIds,
  targetSlotId = activeAddSlotId.value,
) => {
  if (!isQuanLy.value) {
    notify.error(
      "Chỉ nhân viên có chức vụ Quản lý mới được xếp lịch làm việc",
    );
    return false;
  }
  if (!targetSlotId || !selectedDayDate.value || !empIds || empIds.length === 0)
    return false;

  const slotInfo = timeSlots.value.find((t) => t.id === targetSlotId);
  if (slotInfo) {
    const shiftDate = new Date(
      `${new Date().getFullYear()}-${selectedDayDate.value.split("/")[1]}-${
        selectedDayDate.value.split("/")[0]
      }T${slotInfo._rawStart}`,
    );
    if (new Date() >= shiftDate) {
      alert("Không thể xếp ca làm khi giờ bắt đầu đã qua hoặc đang diễn ra");
      return false;
    }
  }

  try {
    const reqData = {
      idNhanViens: empIds,
      ngay: `${new Date().getFullYear()}-${
        selectedDayDate.value.split("/")[1]
      }-${selectedDayDate.value.split("/")[0]}`,
      idCaLam: targetSlotId,
      trangThai: 1,
    };
    const res = await lichLamViecApi.them(reqData);
    if (res.data && Array.isArray(res.data)) {
      res.data.forEach((lich) => {
        const emp = employees.value.find((e) => e.id === lich.idNhanVien);
        schedules.value.push({
          id: lich.id,
          slotId: lich.idCaLam,
          date: selectedDayDate.value,
          empId: lich.idNhanVien,
          empName: lich.tenNhanVien || (emp ? emp.name : "Không tên"),
        });
      });
      notify.success(`Thêm ${res.data.length} nhân viên vào ca thành công`);
      selectedEmployeeIds.value = [];
      employeePickerQuery.value = "";
      return true;
    }
  } catch (error) {
    console.error("Lỗi khi thêm nhân viên:", error);
    notify.error(
      error.response?.data?.message ||
        error.response?.data ||
        "Lỗi khi thêm nhân viên vào ca",
    );
  }
  return false;
};

const handleAddEmployeeFromForm = async () => {
  if (!selectedEmployeeIds.value || selectedEmployeeIds.value.length === 0) {
    notify.error("Vui lòng chọn ít nhất một nhân viên");
    return;
  }

  await addEmployeesToSlot(selectedEmployeeIds.value);
};

const handleQuickAddEmployee = async () => {
  if (!selectedEmployeeIds.value || selectedEmployeeIds.value.length === 0) {
    notify.error("Vui lòng chọn ít nhất một nhân viên");
    return;
  }

  const added = await addEmployeesToSlot(selectedEmployeeIds.value);
  if (added) {
    closeQuickAddEmployeeModal();
  }
};

const isPastDay = (dateStr) => {
  if (!dateStr || dateStr.includes("empty")) return false;
  const [day, month] = dateStr.split("/");
  const year = new Date().getFullYear();

  // Tạo mốc thời gian 00:00:00 của ngày hôm nay để so sánh ngày rạch ròi
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const compareDate = new Date(year, parseInt(month) - 1, parseInt(day));
  return compareDate < today;
};

const onDropToShiftInModal = (event, slotId) => {
  const empId = parseInt(event.dataTransfer.getData("empId"));
  if (!empId) return;
  addEmployeesToSlot([empId], slotId);
};

const toggleEmployeeSelection = (empId) => {
  const index = selectedEmployeeIds.value.indexOf(empId);
  if (index > -1) {
    selectedEmployeeIds.value.splice(index, 1);
  } else {
    selectedEmployeeIds.value.push(empId);
  }
};

const onDropToMonth = (event, date) => {
  const empId = parseInt(event.dataTransfer.getData("empId"));
  if (!empId) return;
  openDayModal(date);
};

// Import Excel logic
const fileInput = ref(null);
const handleFileChange = async (event) => {
  if (!isQuanLy.value) {
    notify.error(
      "Chỉ nhân viên có chức vụ Quản lý mới được xếp lịch làm việc",
    );
    return;
  }
  const file = event.target.files[0];
  if (!file) return;
  try {
    const res = await lichLamViecApi.importExcel(file);
    notify.success(res.data?.message || "Nhập lịch từ Excel thành công");
    fetchSchedules();
  } catch (error) {
    console.error("Lỗi import excel:", error);
    notify.error(
      error.response?.data?.error ||
        error.response?.data?.message ||
        "Lỗi khi nhập lịch từ Excel",
    );
  } finally {
    event.target.value = null; // Reset input
  }
};
const triggerFileInput = () => {
  fileInput.value.click();
};

const handleExportExcel = async () => {
  try {
    let tuNgay = "";
    let denNgay = "";
    const year = new Date().getFullYear();

    if (
      viewMode.value === "week" &&
      weekDays.value &&
      weekDays.value.length === 7
    ) {
      const startParts = weekDays.value[0].date.split("/"); // dd/mm
      const endParts = weekDays.value[6].date.split("/");
      tuNgay = `${year}-${startParts[1]}-${startParts[0]}`;
      denNgay = `${year}-${endParts[1]}-${endParts[0]}`;
    } else if (viewMode.value === "month" && selectedMonth.value) {
      tuNgay = `${year}-${selectedMonth.value}-01`;
      const lastDay = new Date(
        year,
        parseInt(selectedMonth.value),
        0,
      ).getDate();
      denNgay = `${year}-${selectedMonth.value}-${
        lastDay < 10 ? "0" + lastDay : lastDay
      }`;
    }

    const params = {};
    if (tuNgay && denNgay) {
      params.tuNgay = tuNgay;
      params.denNgay = denNgay;
    }

    const response = await lichLamViecApi.exportExcel(params);
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute(
      "download",
      `lich_lam_viec_${new Date().toISOString().split("T")[0]}.xlsx`,
    );
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    notify.success("Xuất file Excel thành công");
  } catch (error) {
    console.error("Lỗi xuất excel:", error);
    notify.error("Lỗi khi xuất file Excel");
  }
};

const handleDownloadTemplate = async () => {
  try {
    const response = await lichLamViecApi.downloadTemplate();
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", `mau_them_lich_lam_viec.xlsx`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    notify.success("Tải mẫu Excel thành công");
  } catch (error) {
    console.error("Lỗi tải mẫu excel:", error);
    notify.error("Lỗi khi tải mẫu Excel");
  }
};

// Add Ca Lam logic
</script>

<template>
  <div
    class="lich-lam-viec schedule-container d-flex flex-column h-100 fade-in"
  >
    <!-- TOP HEADER (REMOVED: Moved to QuanLyLichLamViec) -->

    <div class="filter-box">
      <div class="filter-item">
        <label>Tìm kiếm</label>
        <input
          class="custom-input"
          placeholder="Tìm nhân viên..."
          v-model="searchQuery"
        />
      </div>

      <div class="filter-item">
        <label>Tháng</label>
        <v-select
          class="modern-select"
          v-model="selectedMonth"
          :options="[
            { label: 'Tháng 6/2026', value: '06' },
            { label: 'Tháng 7/2026', value: '07' },
          ]"
          :reduce="(opt) => opt.value"
          :clearable="false"
        ></v-select>
      </div>

      <div class="filter-item">
        <label>Tuần</label>
        <v-select
          class="modern-select"
          v-model="selectedWeekIndex"
          :options="
            monthWeeks.map((wk, idx) => ({ label: wk.label, value: idx }))
          "
          :reduce="(opt) => opt.value"
          :clearable="false"
        ></v-select>
      </div>

      <div class="filter-action ms-auto d-flex gap-2">
        <input
          type="file"
          ref="fileInput"
          @change="handleFileChange"
          accept=".xlsx,.xls"
          style="display: none"
        />

        <button
          class="btn-primary px-4 fw-semibold"
          style="height: 42px"
          @click="$router.push('/admin/chi-tiet-lich-lam-viec')"
        >
          <i class="ri-list-settings-line"></i> Chi tiết lịch làm việc
        </button>
      </div>
    </div>

    <div class="box flex-grow-1 d-flex flex-column overflow-hidden mb-0">
      <div class="box-header box-header-flex">
        <h3 class="table-title">
          <span
            class="icon-wrapper-small d-flex align-items-center justify-content-center"
          >
            <img :src="iconDanhSach" class="icon-title-small" />
          </span>
          Lịch Làm Việc
        </h3>

        <div class="header-actions d-flex align-items-center">
          <div v-if="isQuanLy" class="d-flex gap-2 me-3">
            <button class="excel-btn" @click="handleDownloadTemplate">
              <img :src="iconExcel" alt="" />
              <span>Tải mẫu Excel</span>
            </button>
            <button class="import-btn" @click="triggerFileInput">
              <img src="@/assets/icons/nhapexx.png" alt="" />
              <span>Nhập từ Excel</span>
            </button>
            <button class="excel-btn" @click="handleExportExcel">
              <img :src="iconExcel" alt="" />
              <span>Xuất Excel</span>
            </button>
          </div>

          <div
            class="view-toggle d-flex align-items-center bg-light p-1 border"
          >
            <button
              class="btn btn-sm px-3 fw-semibold border-0"
              :class="
                viewMode === 'week'
                  ? 'btn-danger shadow-sm text-white'
                  : 'text-muted'
              "
              @click="setViewMode('week')"
            >
              Tuần
            </button>
            <button
              class="btn btn-sm px-3 fw-semibold border-0"
              :class="
                viewMode === 'month'
                  ? 'btn-danger shadow-sm text-white'
                  : 'text-muted'
              "
              @click="setViewMode('month')"
            >
              Tháng
            </button>
          </div>
        </div>
      </div>

      <!-- MAIN BODY -->
      <div
        class="schedule-body d-flex gap-3 flex-grow-1 overflow-hidden px-3 pb-3"
      >
        <!-- LEFT: Employee Sidebar -->
        <div
          v-if="isQuanLy"
          class="employee-sidebar bg-white rounded-3 shadow-sm border d-flex flex-column"
        >
          <div
            class="p-3 border-bottom bg-light rounded-top d-flex justify-content-between align-items-center"
          >
            <h6
              class="m-0 fw-bold text-secondary text-uppercase"
              style="letter-spacing: 0.5px"
            >
              Nhân Viên
              <span class="badge bg-danger ms-1">{{ employees.length }}</span>
            </h6>
            <i class="ri-team-fill text-muted"></i>
          </div>
          <div class="employee-list overflow-auto p-2 custom-scrollbar">
            <div
              v-for="emp in employees"
              :key="emp.id"
              class="employee-item d-flex align-items-center gap-3 p-2 rounded mb-2"
              draggable="true"
              @dragstart="onDragStart($event, emp)"
            >
              <div
                class="avatar bg-danger text-white fw-bold rounded-circle d-flex align-items-center justify-content-center shadow-sm"
              >
                {{ emp.name.charAt(0) }}
              </div>
              <div class="overflow-hidden">
                <div class="fw-bold text-dark text-truncate">
                  {{ emp.name }}
                </div>
                <div class="small text-muted text-truncate">{{ emp.role }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- RIGHT: Calendar Grid -->
        <div
          class="calendar-wrapper bg-white rounded-3 shadow-sm border flex-grow-1 d-flex flex-column overflow-hidden"
        >
          <!-- WEEK VIEW -->
          <div
            v-if="viewMode === 'week'"
            class="d-flex flex-column h-100 flex-grow-1 overflow-hidden"
          >
            <!-- Calendar Header (Days) -->
            <div class="calendar-header d-flex border-bottom bg-light">
              <div
                class="time-col border-end p-3 text-center fw-bold text-secondary d-flex align-items-center justify-content-center"
              >
                Giờ làm
              </div>
              <div
                v-for="day in weekDays"
                :key="day.date"
                class="day-col flex-grow-1 border-end p-3 text-center"
                :class="isToday(day.date) ? 'bg-danger-subtle' : ''"
              >
                <div
                  class="fw-bold fs-6"
                  :class="isToday(day.date) ? 'text-danger' : 'text-dark'"
                >
                  {{ day.name }}
                </div>
                <div
                  class="small"
                  :class="
                    isToday(day.date) ? 'text-danger fw-bold' : 'text-muted'
                  "
                >
                  {{ day.date }}
                </div>
              </div>
            </div>

            <!-- Calendar Body (Grid) -->
            <div
              class="calendar-body flex-grow-1 overflow-auto custom-scrollbar"
            >
              <div
                v-for="(slot, index) in timeSlots"
                :key="'slot-' + slot.id"
                class="calendar-row d-flex border-bottom align-items-stretch"
              >
                <!-- Time Slot Col -->
                <div
                  class="time-col border-end p-3 d-flex flex-column align-items-center justify-content-center fw-semibold text-dark bg-light text-center"
                >
                  <div>{{ slot.name }}</div>
                  <div class="small text-muted fw-normal mt-1">
                    <i class="ri-time-line"></i> {{ slot.time }}
                  </div>
                </div>

                <!-- Shift Cells -->
                <div
                  v-for="day in weekDays"
                  :key="day.date"
                  class="day-col flex-grow-1 border-end p-2 position-relative day-cell d-flex flex-column align-items-center justify-content-start"
                  :class="{ 'cell-disabled': isPastShift(slot.id, day.date) }"
                  @dragover.prevent
                  @drop="onDrop($event, slot.id, day.date)"
                >
                  <!-- Render all scheduled employees -->
                  <div
                    v-for="sched in getEmployeesInSlot(slot.id, day.date)"
                    :key="sched.id"
                    class="shift-card p-2 rounded mb-2 shadow-sm border-start border-4 w-100 d-flex flex-column justify-content-center bg-white cursor-pointer hover-shadow"
                    :class="[
                      isFinishedShift(slot.id, day.date)
                        ? 'past-shift opacity-75'
                        : slot.type === 'Sáng'
                        ? 'border-primary'
                        : slot.type === 'Chiều'
                        ? 'border-warning'
                        : 'border-danger',
                    ]"
                    @click="viewScheduleDetails(sched)"
                  >
                    <div
                      class="d-flex justify-content-between align-items-center"
                    >
                      <span
                        class="fw-bold small text-truncate"
                        :class="
                          isFinishedShift(slot.id, day.date)
                            ? 'text-secondary'
                            : 'text-dark'
                        "
                        :title="sched.empName"
                      >
                        <i
                          class="ri-user-fill"
                          :class="
                            isFinishedShift(slot.id, day.date)
                              ? 'text-secondary'
                              : slot.type === 'Sáng'
                              ? 'text-primary'
                              : slot.type === 'Chiều'
                              ? 'text-warning'
                              : 'text-danger'
                          "
                        ></i>
                        {{ sched.empName }}
                      </span>
                      <i
                        v-if="
                          isQuanLy &&
                          ![2, 3, 4].includes(sched.trangThai) &&
                          !isPastShift(sched.slotId, sched.date)
                        "
                        class="bi bi-x text-muted cursor-pointer hover-danger"
                        title="Xóa"
                        @click.stop="removeSchedule(sched.id)"
                      ></i>
                    </div>
                    <!-- Thêm trạng thái enum text vào đây -->
                    <div
                      style="font-size: 11px; margin-top: 2px"
                      class="fw-bold"
                      :class="getTrangThaiColorClass(sched.trangThai)"
                    >
                      {{ getTrangThaiEnumText(sched.trangThai) }}
                    </div>
                  </div>

                  <!-- Hover add button (Always exists for the cell, behind cards) -->
                  <div
                    v-if="isQuanLy && !isPastShift(slot.id, day.date)"
                    class="add-shift-overlay position-absolute top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center cursor-pointer rounded"
                    @click.stop="
                      openAddEmployeePanelFromWeek(slot.id, day.date)
                    "
                  >
                    <button
                      class="btn btn-sm btn-outline-danger shadow-sm rounded-circle d-flex align-items-center justify-content-center p-2 cell-add-btn"
                      @click.stop="
                        openAddEmployeePanelFromWeek(slot.id, day.date)
                      "
                      title="Thêm nhân viên vào ca"
                    >
                      <i class="ri-add-line fw-bold"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- MONTH VIEW -->
          <div
            v-if="viewMode === 'month'"
            class="month-view flex-grow-1 bg-light p-3 overflow-auto custom-scrollbar"
          >
            <div class="month-header-grid mb-3">
              <!-- Headers -->
              <div
                class="month-header-cell text-center fw-bold text-secondary p-2 bg-white border rounded shadow-sm"
                v-for="dayName in [
                  'Thứ 2',
                  'Thứ 3',
                  'Thứ 4',
                  'Thứ 5',
                  'Thứ 6',
                  'Thứ 7',
                  'CN',
                ]"
                :key="dayName"
              >
                {{ dayName }}
              </div>
            </div>
            <div class="month-grid">
              <div
                class="month-cell d-flex flex-column"
                v-for="mDay in monthDays"
                :key="mDay.date"
                :class="[
                  mDay.empty
                    ? 'bg-light border-0 opacity-50'
                    : 'bg-white shadow-sm',
                  isToday(mDay.date)
                    ? 'border border-2 border-danger rounded'
                    : 'border rounded',
                  {
                    'month-cell-disabled': !mDay.empty && isPastDay(mDay.date),
                  },
                ]"
                @click="!mDay.empty && openDayModal(mDay.date)"
                @dragover.prevent
                @drop="!mDay.empty && onDropToMonth($event, mDay.date)"
              >
                <template v-if="!mDay.empty">
                  <div
                    class="month-cell-header d-flex justify-content-between p-1 px-2 border-bottom"
                    :class="
                      isToday(mDay.date) ? 'bg-danger text-white' : 'bg-light'
                    "
                  >
                    <span class="fw-bold small">{{ mDay.date }}</span>
                  </div>
                  <div
                    class="month-cell-body flex-grow-1 p-1 d-flex flex-column gap-1 justify-content-center"
                  >
                    <!-- Badges for each shift -->
                    <div
                      class="shift-badge small px-2 py-1 rounded d-flex justify-content-between align-items-center shadow-sm mx-1"
                      :class="
                        isFinishedShift(1, mDay.date)
                          ? 'past-shift-badge opacity-75'
                          : 'bg-primary text-white'
                      "
                      v-if="getEmployeesInSlot(1, mDay.date).length > 0"
                    >
                      <span class="fw-semibold"
                        ><i class="ri-sun-fill"></i> Sáng</span
                      >
                      <span
                        class="badge bg-white"
                        :class="
                          isFinishedShift(1, mDay.date)
                            ? 'text-secondary'
                            : 'text-primary'
                        "
                        >{{ getEmployeesInSlot(1, mDay.date).length }}</span
                      >
                    </div>
                    <div
                      class="shift-badge small px-2 py-1 rounded d-flex justify-content-between align-items-center shadow-sm mx-1"
                      :class="
                        isFinishedShift(2, mDay.date)
                          ? 'past-shift-badge opacity-75'
                          : 'bg-warning text-dark'
                      "
                      v-if="getEmployeesInSlot(2, mDay.date).length > 0"
                    >
                      <span class="fw-semibold"
                        ><i class="ri-sun-cloudy-fill"></i> Chiều</span
                      >
                      <span
                        class="badge bg-white"
                        :class="
                          isFinishedShift(2, mDay.date)
                            ? 'text-secondary'
                            : 'text-warning'
                        "
                        >{{ getEmployeesInSlot(2, mDay.date).length }}</span
                      >
                    </div>
                    <div
                      class="shift-badge small px-2 py-1 rounded d-flex justify-content-between align-items-center shadow-sm mx-1"
                      :class="
                        isFinishedShift(3, mDay.date)
                          ? 'past-shift-badge opacity-75'
                          : 'bg-danger text-white'
                      "
                      v-if="getEmployeesInSlot(3, mDay.date).length > 0"
                    >
                      <span class="fw-semibold"
                        ><i class="ri-moon-fill"></i> Tối</span
                      >
                      <span
                        class="badge bg-white"
                        :class="
                          isFinishedShift(3, mDay.date)
                            ? 'text-secondary'
                            : 'text-danger'
                        "
                        >{{ getEmployeesInSlot(3, mDay.date).length }}</span
                      >
                    </div>
                  </div>
                </template>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- QUICK ADD EMPLOYEE MODAL -->
  <Transition name="fade">
    <Teleport to="body">
      <div
        v-if="showQuickAddEmployeeModal"
        class="modal-overlay"
        @click.self="closeQuickAddEmployeeModal"
      >
        <form
          class="quick-schedule-modal"
          @submit.prevent="handleQuickAddEmployee"
        >
          <div class="quick-schedule-header">
            <h4>Xếp lịch: {{ selectedDayLabel }}</h4>
            <button
              type="button"
              class="quick-schedule-close"
              @click="closeQuickAddEmployeeModal"
            >
              <i class="ri-close-line"></i>
            </button>
          </div>

          <div class="quick-shift-card">
            <div>
              <div class="quick-shift-label">Ca làm việc</div>
              <div class="quick-shift-name">
                {{ selectedAddSlot?.name || "Chưa chọn ca" }}
              </div>
            </div>
            <div class="quick-shift-time">
              <span>Thời gian</span>
              <strong>{{ selectedAddSlot?.time || "--:-- - --:--" }}</strong>
            </div>
          </div>

          <div class="quick-schedule-field">
            <label>Chọn nhân viên trực ca:</label>
            <v-select
              v-model="selectedEmployeeIds"
              class="quick-employee-select"
              multiple
              :options="
                availableEmployeesForAdd.map((emp) => ({
                  label: emp.name,
                  value: emp.id,
                }))
              "
              :reduce="(opt) => opt.value"
              :clearable="true"
              placeholder="Chọn một hoặc nhiều nhân viên..."
            ></v-select>
          </div>

          <button
            type="submit"
            class="quick-save-btn"
            :disabled="!selectedEmployeeIds.length"
          >
            <i class="ri-save-3-line"></i>
            Xác nhận
          </button>
        </form>
      </div>
    </Teleport>
  </Transition>

  <!-- DAY DETAILS MODAL -->
  <Transition name="fade">
    <Teleport to="body">
      <div
        v-if="showDayModal"
        class="modal-overlay"
        @click.self="closeDayModal"
      >
        <div
          class="modal-box premium-modal-content d-flex flex-row"
          :style="{
            width: activeAddSlotId ? '950px' : '600px',
            transition: 'width 0.3s ease',
            maxHeight: '85vh',
          }"
        >
          <!-- Left Column: Existing Day Details -->
          <div
            class="flex-grow-1 d-flex flex-column h-100 overflow-hidden"
            style="width: 600px"
          >
            <div class="modal-header premium-modal-header border-bottom pb-3">
              <h5 class="m-0 fw-bold d-flex align-items-center gap-2">
                <i class="ri-calendar-event-line text-danger"></i> Chi Tiết Ngày
                {{ selectedDayDate }}
              </h5>
              <button class="btn-close" @click="closeDayModal">✕</button>
            </div>
            <div
              class="modal-body premium-modal-body bg-light p-3 overflow-auto custom-scrollbar"
            >
              <div
                v-for="slot in dayModalSlots"
                :key="slot.id"
                v-show="isSlotValidForDate(slot, selectedDayDate)"
                class="mb-3 bg-white p-3 rounded-3 border shadow-sm"
                :class="{ 'border-danger shadow': activeAddSlotId === slot.id }"
                @dragover.prevent
                @drop="onDropToShiftInModal($event, slot.id)"
              >
                <h6
                  class="fw-bold d-flex justify-content-between border-bottom pb-2 mb-2 align-items-center"
                  :class="
                    slot.type === 'Sáng'
                      ? 'text-primary'
                      : slot.type === 'Chiều'
                      ? 'text-warning'
                      : 'text-danger'
                  "
                >
                  <span class="d-flex align-items-center gap-2">
                    <i
                      :class="
                        slot.type === 'Sáng'
                          ? 'bi bi-brightness-alt-high'
                          : slot.type === 'Chiều'
                          ? 'bi bi-sun'
                          : 'bi bi-moon-stars'
                      "
                    ></i>
                    {{ slot.name }} ({{ slot.time }})
                  </span>
                  <div class="d-flex align-items-center gap-2">
                    <span
                      class="badge"
                      :class="
                        slot.type === 'Sáng'
                          ? 'bg-primary'
                          : slot.type === 'Chiều'
                          ? 'bg-warning text-dark'
                          : 'bg-danger'
                      "
                      >{{
                        getEmployeesInSlot(slot.id, selectedDayDate).length
                      }}
                      nhân viên</span
                    >
                    <button
                      v-if="isQuanLy && !isPastShift(slot.id, selectedDayDate)"
                      class="btn btn-sm btn-outline-danger rounded-circle p-0 d-flex align-items-center justify-content-center"
                      style="width: 24px; height: 24px"
                      @click="openAddEmployeePanel(slot.id)"
                      title="Thêm nhân viên vào ca"
                    >
                      <i class="ri-add-line"></i>
                    </button>
                  </div>
                </h6>
                <div
                  v-if="
                    getEmployeesInSlot(slot.id, selectedDayDate).length === 0
                  "
                  class="text-muted small fst-italic py-2 text-center bg-light rounded border border-dashed"
                >
                  Chưa có nhân viên nào trong ca này.
                </div>
                <div class="d-flex flex-column gap-2" v-else>
                  <div
                    v-for="sched in getEmployeesInSlot(
                      slot.id,
                      selectedDayDate,
                    )"
                    :key="sched.id"
                    class="d-flex justify-content-between align-items-center bg-light p-2 px-3 rounded border cursor-pointer hover-shadow"
                    @click="viewScheduleDetails(sched)"
                  >
                    <div class="d-flex align-items-center gap-2">
                      <div
                        class="avatar bg-secondary text-white rounded-circle d-flex align-items-center justify-content-center fw-bold shadow-sm"
                        style="width: 32px; height: 32px; font-size: 13px"
                      >
                        {{ sched.empName.charAt(0) }}
                      </div>
                      <span class="fw-semibold text-dark">{{
                        sched.empName
                      }}</span>
                    </div>
                    <div class="d-flex align-items-center gap-3">
                      <span
                        class="small text-muted fw-medium bg-white px-2 py-1 rounded border"
                        ><i class="ri-time-line"></i>
                        {{
                          slot.type === "Sáng"
                            ? "4h"
                            : slot.type === "Chiều"
                            ? "6h"
                            : "5h"
                        }}</span
                      >
                      <button
                        v-if="
                          ![2, 3, 4].includes(sched.trangThai) &&
                          !isPastShift(sched.slotId, sched.date)
                        "
                        class="btn btn-sm btn-outline-danger py-1 px-2 rounded d-flex align-items-center gap-1 shadow-sm"
                        @click.stop="removeSchedule(sched.id)"
                      >
                        <i class="ri-delete-bin-line"></i> Xóa
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="modal-footer premium-modal-footer border-top">
              <button class="btn-cancel" @click="closeDayModal">Đóng</button>
            </div>
          </div>

          <div
            v-if="activeAddSlotId"
            class="border-start bg-white d-flex flex-column overflow-hidden add-employee-form"
            style="width: 380px"
          >
            <div
              class="modal-header premium-modal-header border-bottom d-flex justify-content-between align-items-start gap-3 flex-shrink-0"
            >
              <div>
                <h5
                  class="m-0 fw-bold text-dark d-flex align-items-center gap-2"
                >
                  <i class="ri-user-add-fill text-danger"></i> Thêm nhân viên
                </h5>
                <div class="small text-muted mt-1">
                  {{ selectedAddSlot?.name }} - {{ selectedDayDate }}
                </div>
              </div>
              <button class="btn-close" @click="closeAddEmployeePanel">
                ✕
              </button>
            </div>

            <form
              class="d-flex flex-column flex-grow-1 overflow-hidden"
              style="min-height: 0"
              @submit.prevent="handleAddEmployeeFromForm"
            >
              <div class="p-3 border-bottom bg-light flex-shrink-0">
                <label class="form-label fw-bold small text-secondary">
                  Tìm nhân viên
                </label>
                <div class="position-relative">
                  <i class="ri-search-line add-employee-search-icon"></i>
                  <input
                    v-model="employeePickerQuery"
                    type="text"
                    class="form-control add-employee-search"
                    placeholder="Nhập tên hoặc chức vụ..."
                  />
                </div>
              </div>

              <div
                class="p-3 overflow-auto custom-scrollbar flex-grow-1"
                style="min-height: 0"
              >
                <label class="form-label fw-bold small text-secondary mb-2">
                  Chọn nhân viên
                </label>
                <div class="d-flex flex-column gap-2">
                  <button
                    v-for="emp in availableEmployeesForAdd"
                    :key="emp.id"
                    type="button"
                    class="employee-choice-btn"
                    :class="{ active: selectedEmployeeIds.includes(emp.id) }"
                    @click="toggleEmployeeSelection(emp.id)"
                  >
                    <span
                      class="avatar bg-danger text-white rounded-circle d-flex align-items-center justify-content-center fw-bold"
                      style="width: 36px; height: 36px; font-size: 14px"
                    >
                      {{ emp.name.charAt(0) }}
                    </span>
                    <span class="employee-choice-info">
                      <span class="employee-choice-name">{{ emp.name }}</span>
                      <span class="employee-choice-role">{{ emp.role }}</span>
                    </span>
                    <i
                      class="ri-check-line employee-choice-check"
                      v-if="selectedEmployeeIds.includes(emp.id)"
                    ></i>
                  </button>
                </div>

                <div
                  v-if="availableEmployeesForAdd.length === 0"
                  class="text-center text-muted fst-italic mt-3 p-3 bg-light rounded border border-dashed"
                >
                  Không còn nhân viên phù hợp để thêm vào ca này.
                </div>
              </div>

              <div
                class="modal-footer premium-modal-footer border-top flex-shrink-0"
              >
                <button
                  type="button"
                  class="btn-cancel"
                  @click="closeAddEmployeePanel"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  class="btn-primary"
                  :disabled="!selectedEmployeeIds.length"
                >
                  <div class="btn-light-sweep"></div>
                  <i class="ri-user-add-line"></i>
                  Xác nhận
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </Teleport>
  </Transition>

  <!-- ADD CA LAM MODAL -->
  <Transition name="fade">
    <Teleport to="body">
      <div
        v-if="showAddCaLamModal"
        class="modal-overlay"
        @click.self="showAddCaLamModal = false"
      >
        <div class="modal-box premium-modal-content" style="width: 450px">
          <div class="modal-header premium-modal-header border-bottom pb-3">
            <h5 class="m-0 fw-bold d-flex align-items-center gap-2">
              <i class="ri-history-line text-danger"></i> Thêm Ca Làm
            </h5>
            <button class="btn-close" @click="showAddCaLamModal = false">
              ✕
            </button>
          </div>
          <div class="modal-body premium-modal-body bg-light p-4">
            <div class="mb-3">
              <label class="form-label fw-bold"
                >Tên ca làm <span class="text-danger">*</span></label
              >
              <input
                type="text"
                class="form-control"
                v-model="newCaLam.tenCaLam"
                placeholder="VD: Ca Sáng, Ca Chiều..."
              />
            </div>
            <div class="row">
              <div class="col-6 mb-3">
                <label class="form-label fw-bold"
                  >Giờ bắt đầu <span class="text-danger">*</span></label
                >
                <input
                  type="time"
                  class="form-control"
                  v-model="newCaLam.thoiGianBatDau"
                />
              </div>
              <div class="col-6 mb-3">
                <label class="form-label fw-bold"
                  >Giờ kết thúc <span class="text-danger">*</span></label
                >
                <input
                  type="time"
                  class="form-control"
                  v-model="newCaLam.thoiGianKetThuc"
                />
              </div>
            </div>
          </div>
          <div class="modal-footer premium-modal-footer border-top">
            <button
              type="button"
              class="btn-cancel"
              @click="showAddCaLamModal = false"
            >
              Hủy
            </button>
            <button class="btn-primary" @click="handleAddCaLam">
              <div class="btn-light-sweep"></div>
              Xác nhận
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </Transition>
  <!-- SCHEDULE DETAILS MODAL -->
  <Transition name="fade">
    <Teleport to="body">
      <div
        v-if="showScheduleDetailsModal && selectedScheduleDetails"
        class="modal-overlay"
        @click.self="showScheduleDetailsModal = false"
      >
        <div class="modal-box premium-modal-content" style="width: 400px">
          <div class="modal-header premium-modal-header border-bottom pb-3">
            <h5 class="m-0 fw-bold d-flex align-items-center gap-2">
              Chi Tiết Ca Làm
            </h5>
            <button class="btn-close" @click="showScheduleDetailsModal = false">
              ✕
            </button>
          </div>
          <div class="modal-body premium-modal-body bg-light p-4">
            <div class="d-flex flex-column gap-3">
              <div class="d-flex align-items-center gap-3">
                <div
                  class="avatar bg-danger text-white rounded-circle d-flex align-items-center justify-content-center fw-bold shadow-sm"
                  style="width: 48px; height: 48px; font-size: 20px"
                >
                  {{ selectedScheduleDetails.empName.charAt(0) }}
                </div>
                <div>
                  <div class="fw-bold text-dark fs-5">
                    {{ selectedScheduleDetails.empName }}
                  </div>
                  <div class="text-muted small">
                    Ngày: {{ selectedScheduleDetails.date }}
                  </div>
                </div>
              </div>

              <div class="card border-0 shadow-sm rounded-3 p-3">
                <div class="row mb-2">
                  <div class="col-5 text-muted small fw-semibold">Ca làm</div>
                  <div class="col-7 fw-bold text-dark">
                    {{ selectedScheduleDetails.tenCaLam || "Không rõ" }}
                  </div>
                </div>
                <div class="row mb-2">
                  <div class="col-5 text-muted small fw-semibold">Giờ làm</div>
                  <div class="col-7 text-dark">
                    {{
                      selectedScheduleDetails.gioBatDau
                        ? selectedScheduleDetails.gioBatDau.substring(0, 5)
                        : "--:--"
                    }}
                    -
                    {{
                      selectedScheduleDetails.gioKetThuc
                        ? selectedScheduleDetails.gioKetThuc.substring(0, 5)
                        : "--:--"
                    }}
                  </div>
                </div>
                <div class="row mb-2">
                  <div class="col-5 text-muted small fw-semibold">Vị trí</div>
                  <div class="col-7 text-dark">
                    {{ selectedScheduleDetails.viTri }}
                  </div>
                </div>
                <div class="row mb-2">
                  <div class="col-5 text-muted small fw-semibold">
                    Trạng thái
                  </div>
                  <div class="col-7">
                    <span
                      class="status-text"
                      :class="{
                        'status-cancelled':
                          selectedScheduleDetails.trangThai === 0,
                        'status-created':
                          selectedScheduleDetails.trangThai === 1,
                        'status-waiting':
                          selectedScheduleDetails.trangThai === 2,
                        'status-serving':
                          selectedScheduleDetails.trangThai === 3,
                        'status-completed':
                          selectedScheduleDetails.trangThai === 4,
                      }"
                    >
                      {{
                        getTrangThaiEnumText(selectedScheduleDetails.trangThai)
                      }}
                    </span>
                  </div>
                </div>
                <div class="row">
                  <div class="col-5 text-muted small fw-semibold">Ghi chú</div>
                  <div class="col-7 text-dark fst-italic">
                    {{ selectedScheduleDetails.ghiChu }}
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer premium-modal-footer border-top">
            <button
              type="button"
              class="btn-cancel"
              @click="showScheduleDetailsModal = false"
            >
              Đóng
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </Transition>
</template>

<style scoped>
.btn {
  border-radius: 7px !important;
}
@import url("https://cdn.jsdelivr.net/npm/remixicon@4.2.0/fonts/remixicon.css");

.lich-lam-viec {
  /* Removed padding and background since parent QuanLyLichLamViec handles it */
  height: 100%;
}

.month-header-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 12px;
}
.month-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 12px;
  grid-auto-rows: minmax(110px, auto);
}
.month-cell {
  min-height: 110px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
}
.month-cell:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.08) !important;
  border-color: #d73737 !important;
}
.month-cell-header {
  border-top-left-radius: 6px;
  border-top-right-radius: 6px;
}
.shift-badge {
  font-size: 11.5px;
}

.past-shift {
  border-color: #94a3b8 !important;
  background-color: #e2e8f0 !important;
}
.past-shift-badge {
  background-color: #64748b !important;
  color: #fff !important;
}

/* ================== LAYOUT & COMPONENTS CHUNG ================== */
.box {
  background: #fff;
  border-radius: 7px;
  padding: 16px;
  margin-bottom: 20px;
  border: 1px solid #eee;
}
.box-header {
  margin-bottom: 16px;
}
.box-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.icon-wrapper img {
  width: 32px;
  height: 32px;
  object-fit: contain;
}

.icon-wrapper {
  width: 56px;
  height: 56px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 7px;
  background: linear-gradient(
    135deg,
    rgba(225, 29, 72, 0.08),
    rgba(225, 29, 72, 0.15)
  );
  border: 1px solid rgba(225, 29, 72, 0.1);
}

.main-title {
  display: flex;
  align-items: center;
  font-size: 30px;
  font-weight: 600;
  gap: 12px;
  letter-spacing: 1px;
  color: #1a1a1a;
  margin: 0;
}
.box-header p {
  margin: 10px 0 0;
  color: #666;
}

.table-title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}
.icon-wrapper-small {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background-color: #f0eaea;
  border-radius: 7px;
  margin-right: 10px;
}
.icon-title-small {
  width: 18px;
  height: 18px;
  display: block;
}

/* ===== FILTER BOX CHUẨN ===== */
.filter-box {
  background: white;
  padding: 16px 20px;
  border-radius: 7px;
  margin-bottom: 24px;
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 12px;
  border: 1px solid #eee;
}
.filter-item {
  flex: 1;
  min-width: 150px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-width: 300px;
}
.filter-item label {
  font-size: 14px;
  font-weight: 600;
  color: #666;
}
.custom-input {
  height: 42px;
  border: 1px solid #e5e7eb;
  border-radius: 7px;
  padding: 0 12px;
  font-size: 14px;
  outline: none;
  transition: all 0.2s;
  width: 100%;
  font-family: inherit;
}
.custom-input:focus {
  border-color: #00d1f3;
}

/* ===== HEADER ACTIONS ĐỒNG BỘ ===== */
.header-actions {
  display: flex;
  gap: 10px;
}
.add-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 14px;
  border: none;
  border-radius: 7px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #eef2ff;
  color: #d73737;
}
.add-btn img {
  width: 16px;
  height: 16px;
  object-fit: contain;
}
.add-btn:hover {
  background: #dfe7ff;
  transform: translateY(-1px);
}
.fade-in {
  animation: fadeIn 0.4s ease-in-out;
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
.cursor-pointer {
  cursor: pointer;
}
.hover-dark:hover {
  color: #212529 !important;
}

/* TOP HEADER */
.search-box .form-control {
  width: 250px;
  border: 1px solid #dee2e6;
  transition: all 0.2s;
}
.search-box .form-control:focus {
  border-color: #dc3545;
  box-shadow: 0 0 0 0.2rem rgba(220, 53, 69, 0.25);
  width: 280px;
}
.btn-add-shift {
  transition: transform 0.2s, box-shadow 0.2s;
}
.btn-add-shift:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(220, 53, 69, 0.3) !important;
}

/* EMPLOYEE SIDEBAR */
.employee-sidebar {
  width: 260px;
  min-width: 260px;
}
.avatar {
  width: 40px;
  height: 40px;
  font-size: 1.1rem;
}
.employee-item {
  transition: background-color 0.2s;
}
.employee-item:hover {
  background-color: #f8f9fa;
}

/* CALENDAR GRID */
.time-col {
  width: 140px;
  min-width: 140px;
  flex-shrink: 0;
}
.day-col {
  flex: 1 1 0%;
  min-width: 120px;
  width: 0;
}
.calendar-row {
  min-height: 100px;
  transition: background-color 0.2s;
}
.calendar-row:hover {
  background-color: #fcfcfc;
}

/* SHIFT CARD */
.shift-card {
  transition: transform 0.2s;
}
.shift-card:hover {
  transform: scale(1.02);
}

/* ADD SHIFT OVERLAY (Shows on hover empty space) */
.add-shift-overlay {
  background-color: rgba(220, 53, 69, 0.05);
  opacity: 0;
  transition: opacity 0.2s;
  z-index: 10;
}
.day-cell:hover .add-shift-overlay {
  opacity: 1;
}
.shift-card {
  position: relative;
  z-index: 20; /* Keep shift cards above the overlay */
}
.cell-add-btn {
  width: 32px;
  height: 32px;
  background-color: white;
}
.cell-add-btn:hover {
  background-color: #dc3545;
  color: white;
}

/* CUSTOM SCROLLBAR */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 7px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 7px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* ===== MODAL & FORM ===== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999999;
}
.modal-box {
  background: #fff;
  border-radius: 7px;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.modal-header {
  padding: 18px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.modal-body {
  padding: 24px;
  max-height: 70vh;
  overflow-y: auto;
}
.modal-footer {
  padding: 16px 24px;
}

.btn-close {
  background: #f3f4f6;
  font-size: 18px;
  color: #475569;
  width: 40px;
  height: 40px;
  border-radius: 7px;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.06);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.25s ease;
  padding: 0;
}
.btn-close:hover {
  background: rgba(230, 90, 90, 0.12);
  color: #d73737;
  box-shadow: 0 8px 18px rgba(230, 90, 90, 0.15);
  transform: translateY(-2px);
}

.premium-modal-content {
  background: #ffffff;
  border-radius: 16px;
  border: none;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  animation: modalFadeIn 0.3s ease-out;
}

@keyframes modalFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.premium-modal-header {
  background: #f8fafc;
  padding: 16px 24px;
  border-bottom: 1px solid #e2e8f0 !important;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.premium-modal-header h5 {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.premium-modal-body {
  padding: 24px;
}

.premium-modal-footer {
  padding: 16px 24px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0 !important;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.quick-schedule-modal {
  width: 430px;
  max-width: calc(100vw - 32px);
  background: #fff;
  border-radius: 12px;
  padding: 22px;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.24);
}
.quick-schedule-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}
.quick-schedule-header h4 {
  margin: 0;
  color: #1f2937;
  font-size: 19px;
  font-weight: 700;
}
.quick-schedule-close {
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 6px;
  background: #f1f5f9;
  color: #64748b;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
  transition: all 0.2s ease;
}
.quick-schedule-close:hover {
  background: #fee2e2;
  color: #d73737;
}
.quick-shift-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 16px;
  margin-bottom: 20px;
  border: 1px solid #fecaca;
  border-radius: 10px;
  background: #fff1f1;
}
.quick-shift-label {
  color: #d73737;
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
}
.quick-shift-name {
  color: #1f2937;
  font-size: 18px;
  font-weight: 800;
  margin-top: 4px;
}
.quick-shift-time {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
}
.quick-shift-time strong {
  padding: 6px 10px;
  background: #fee2e2;
  color: #d73737;
  border-radius: 4px;
  font-family: monospace;
  font-size: 13px;
  letter-spacing: 0;
}
.quick-schedule-field {
  margin-bottom: 24px;
}
.quick-schedule-field label {
  display: block;
  margin-bottom: 8px;
  color: #334155;
  font-size: 14px;
  font-weight: 700;
}
.quick-employee-select :deep(.vs__dropdown-toggle) {
  min-height: 40px;
  height: auto !important;
  border-radius: 6px !important;
}
.quick-employee-select :deep(.vs__selected) {
  position: static;
  padding-left: 8px !important;
}
.quick-save-btn {
  width: 100%;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: none;
  border-radius: 6px;
  background: #d73737;
  color: #fff;
  font-weight: 800;
  cursor: pointer;
  transition: all 0.2s ease;
}
.quick-save-btn:hover {
  background: #b92b2b;
}
.quick-save-btn:disabled {
  background: #f3a3a3;
  cursor: not-allowed;
}

/* ===== BUTTON MODERN (Cancel + Primary) ===== */
.btn-cancel {
  padding: 10px 20px;
  background: transparent;
  color: #64748b;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}
.btn-cancel:hover {
  background: #f8fafc;
  color: #334155;
  border-color: #94a3b8;
}

.btn-primary {
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 24px;
  background: #d73737;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(215, 55, 55, 0.2);
}
.btn-primary:hover {
  background: #b92b2b;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(215, 55, 55, 0.3);
}
.btn-primary:disabled {
  background: #cbd5e1;
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

.add-employee-form {
  min-width: 380px;
}
.add-employee-search {
  height: 42px;
  padding-left: 38px;
  border-radius: 8px;
  border-color: #e5e7eb;
}
.add-employee-search:focus {
  border-color: #dc3545;
  box-shadow: 0 0 0 4px rgba(220, 53, 69, 0.12);
}
.add-employee-search-icon {
  position: absolute;
  top: 50%;
  left: 12px;
  transform: translateY(-50%);
  color: #94a3b8;
  z-index: 2;
}
.employee-choice-btn {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  text-align: left;
  transition: all 0.2s ease;
}
.employee-choice-btn:hover {
  border-color: #f3a3a3;
  background: #fff7f7;
}
.employee-choice-btn.active {
  border-color: #d73737;
  background: #fff1f1;
  box-shadow: 0 0 0 3px rgba(215, 55, 55, 0.12);
}
.employee-choice-info {
  min-width: 0;
  display: flex;
  flex: 1;
  flex-direction: column;
}
.employee-choice-name {
  color: #111827;
  font-weight: 700;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.employee-choice-role {
  color: #64748b;
  font-size: 12px;
}
.employee-choice-check {
  color: #d73737;
  font-size: 20px;
  flex-shrink: 0;
}

.btn-light-sweep {
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(
    to right,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.3) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  transform: skewX(-25deg);
  transition: all 0.5s ease;
}
.btn-primary:hover .btn-light-sweep {
  left: 150%;
}
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ===== CSS cho ô đã qua (không thể xếp thêm ca) ===== */
.cell-disabled {
  background-color: #f1f5f9 !important; /* Nền xám nhạt cho cả ô */
}

/* Tô xám luôn các thẻ nhân viên nằm trong ô đã qua (như trong ảnh) */
.cell-disabled .shift-card {
  background-color: #e2e8f0 !important;
  border-color: #cbd5e1 !important;
  border-left-width: 1px !important; /* Thu nhỏ viền trái lại cho giống ảnh */
  box-shadow: none !important;
}

.cell-disabled .shift-card span,
.cell-disabled .shift-card i {
  color: #64748b !important; /* Chữ và icon màu xám */
}

/* Đảm bảo ô trống hiển thị nút + sẽ có nền trắng */
.day-cell:not(.cell-disabled) {
  background-color: #ffffff;
}

/* TRẠNG THÁI STATUS (GIỐNG DONHANG.VUE) */
.status-text {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 100px;
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
.status-waiting {
  background: rgba(251, 146, 60, 0.12);
  color: #fb923c;
}
.status-serving {
  background: rgba(139, 92, 246, 0.12);
  color: #8b5cf6;
}
.status-completed {
  background: rgba(5, 150, 105, 0.12);
  color: #059669;
}
.status-cancelled {
  background: rgba(239, 68, 68, 0.12);
  color: #ef4444;
}

/* ===== CSS tô xám ô ngày đã qua ở chế độ Tháng ===== */
.month-cell-disabled {
  background-color: #f1f5f9 !important; /* Nền xám nhạt cho toàn ô */
  border-color: #e2e8f0 !important;
}

/* Biến đổi tiêu đề ngày (màu nền text ngày bên trên) thành màu xám nhẹ */
.month-cell-disabled .month-cell-header {
  background-color: #e2e8f0 !important;
  color: #64748b !important;
}

/* Tô xám toàn bộ các badge ca làm (Sáng, Chiều, Tối) nằm trong ô đã qua */
.month-cell-disabled .shift-badge {
  background-color: #e2e8f0 !important;
  color: #64748b !important;
  box-shadow: none !important;
}

/* Đổi màu cả số lượng nhân viên thu nhỏ bên trong badge */
.month-cell-disabled .shift-badge .badge {
  background-color: #cbd5e1 !important;
  color: #475569 !important;
}

/* Ẩn các hiệu ứng chuyển động hover nổi khối khi ô đã qua ngày */
.month-cell-disabled:hover {
  transform: none !important;
  box-shadow: none !important;
  border-color: #e2e8f0 !important;
}

/* ===== SELECT 2 ĐỒNG BỘ ===== */
:deep(.v-select) {
  background: #fff;
  width: 100%;
  --vs-dropdown-option--active-bg: #f3f4f6;
  --vs-dropdown-option--active-color: #eb6464;
  --vs-border-color: #e5e7eb;
  --vs-border-radius: 10px;
  --vs-dropdown-box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1);
}
:deep(.vs__dropdown-toggle) {
  border: 1px solid #e5e7eb !important;
  border-radius: 10px !important;
  padding: 0 !important;
  min-height: 42px;
  height: auto;
  display: flex;
  align-items: center;
  transition: all 0.2s ease;
  box-shadow: none !important;
}
:deep(.vs__search),
:deep(.vs__search:focus) {
  border: none !important;
  margin: 0 !important;
  padding: 0 12px !important;
  background: transparent !important;
  box-shadow: none !important;
}
:deep(.vs__selected) {
  position: absolute;
  margin: 0 !important;
  padding-left: 12px !important;
  color: #374151;
  border: none !important;
  background: transparent !important;
}

/* Chỉ áp dụng CSS dạng thẻ (chip) cho ô chọn nhân viên (quick-employee-select) */
.quick-employee-select :deep(.vs__selected-options) {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  padding: 6px 8px 0 8px !important;
}
.quick-employee-select :deep(.vs__search),
.quick-employee-select :deep(.vs__search:focus) {
  margin: 0 0 6px 4px !important;
  padding: 0 !important;
}
.quick-employee-select :deep(.vs__selected) {
  position: static !important;
  margin: 0 6px 6px 0 !important;
  display: inline-flex !important;
  align-items: center;
  border-radius: 6px !important;
  padding: 4px 8px !important;
  font-weight: 500;
  background-color: #f1f5f9 !important;
  color: #334155 !important;
  border: 1px solid #e2e8f0 !important;
}
.quick-employee-select :deep(.vs__deselect) {
  margin-left: 6px !important;
  fill: #64748b !important;
}
.quick-employee-select :deep(.vs__deselect:hover) {
  fill: #ef4444 !important;
}
:deep(.vs__search::placeholder) {
  color: #9ca3af !important;
}
:deep(.vs__dropdown-toggle:hover) {
  border-color: #c8d0dc !important;
}
:deep(.vs--open .vs__dropdown-toggle) {
  border-color: var(--color-primary) !important;
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.12) !important;
}
:deep(.vs__dropdown-menu) {
  border: 1px solid #e5e7eb !important;
  border-radius: 10px !important;
  margin-top: 8px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1) !important;
  padding: 6px !important;
  z-index: 1000;
}
:deep(.vs__dropdown-option) {
  border-radius: 8px !important;
  padding: 8px 12px !important;
  margin-bottom: 2px !important;
  color: #4b5563;
}
:deep(.vs__dropdown-option--highlight) {
  background: #f3f4f6 !important;
  color: #eb6464 !important;
}
:deep(.vs__dropdown-option--selected) {
  background: #f3f5f9 !important;
  color: #fc0101 !important;
}
:deep(.vs__actions) {
  padding-right: 10px !important;
}
:deep(.vs__open-indicator) {
  fill: #9ca3af !important;
}

.custom-input {
  height: 42px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 0 12px;
  font-size: 14px;
  outline: none;
  transition: all 0.2s ease;
  width: 100%;
  font-family: inherit;
  background-color: #fff;
  box-shadow: none;
}
.custom-input:hover {
  border-color: #c8d0dc;
}
.custom-input:focus {
  border-color: var(--color-primary, #dc3545);
  box-shadow: 0 0 0 4px rgba(220, 53, 69, 0.12);
}

/* Multiselect fix for employee selection tags */
:deep(.multiselect__tags) {
  padding: 6px 8px 0 8px !important;
  border-radius: 8px;
  min-height: 42px;
}
:deep(.multiselect__tag) {
  display: inline-flex !important;
  align-items: center;
  margin: 0 6px 6px 0 !important;
  border-radius: 6px !important;
  padding: 6px 26px 6px 10px !important;
  font-weight: 500;
  background-color: #f1f5f9 !important;
  color: #334155 !important;
  border: 1px solid #e2e8f0;
}
:deep(.multiselect__tag-icon) {
  line-height: 28px !important;
  width: 26px !important;
  border-radius: 6px !important;
}
:deep(.multiselect__tag-icon:after) {
  color: #64748b !important;
}
:deep(.multiselect__tag-icon:hover) {
  background: rgba(239, 68, 68, 0.1) !important;
}
:deep(.multiselect__tag-icon:hover:after) {
  color: #ef4444 !important;
}
:deep(.multiselect__input) {
  margin: 0 !important;
  padding: 4px 0 4px 8px !important;
}

.excel-btn,
.import-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border: none;
  border-radius: 7px;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.2s;
}

.excel-btn img,
.import-btn img {
  width: 18px !important;
  height: 18px !important;
  object-fit: contain;
  transition: all 0.3s ease;
}

.excel-btn {
  background: #d4f0dd;
  color: #d73737;
}

.excel-btn:hover {
  background: #dcfce7;
  transform: translateY(-2px);
}

.import-btn {
  background: #e0f2fe;
  color: #d73737;
}

.import-btn:hover:not(:disabled) {
  background: #bae6fd;
  transform: translateY(-2px);
}

.import-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
}
</style>
