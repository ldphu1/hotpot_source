<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from "vue";
import { useRouter } from "vue-router";
import { getAlldatban } from "@/api/admin/datban/datBanApi";
import { getAllquanlyban } from "@/api/admin/quanlyban/quanlybanApi";
import { getAllkhuvuc } from "@/api/admin/khuvuc/khuVucApi";
import { connectWebSocket, disconnectWebSocket } from "@/utils/websocket";

const router = useRouter();

// --- TRẠNG THÁI CHUNG ---
const currentTab = ref("calendar");
const timelineMode = ref("Ngày");
const viewDate = ref(new Date());
const floors = ref([]);
const selectedFloor = ref("");
const searchGuest = ref("");

const reservations = ref([]);

const columnWidth = computed(() => {
  if (timelineMode.value === "Ngày") return 120;
  if (timelineMode.value === "Tuần") return 60;
  if (timelineMode.value === "Tháng") return 80;
});

const tables = ref([]);

const isFinishedOrCancelled = (trangThai) => {
  if (!trangThai) return false;
  const s = String(trangThai).toUpperCase().trim();
  return (
    s === "HOAN_THANH" ||
    s === "HOÀN THÀNH" ||
    s === "DA_HUY" ||
    s === "ĐÃ HỦY" ||
    s === "HET_HAN" ||
    s === "HẾT HẠN" ||
    s === "TU_CHOI" ||
    s === "TỪ CHỐI" ||
    s === "CHO_HOAN_COC" ||
    s === "CHỜ HOÀN CỌC" ||
    s === "HUY_COC" ||
    s === "HỦY CỌC"
  );
};

const convertStatus = (status) => {
  if (!status) return "green";
  const s = String(status).toUpperCase().trim();

  if (
    s === "CHO_XAC_NHAN" ||
    s === "CHỜ XÁC NHẬN" ||
    s === "CHO_COC" ||
    s === "CHỜ CỌC"
  ) {
    return "green";
  }

  if (
    s === "DA_XAC_NHAN" ||
    s === "ĐÃ XÁC NHẬN" ||
    s === "DA_XEP" ||
    s === "ĐÃ XẾP" ||
    s === "DANG_PHUC_VU" ||
    s === "ĐANG PHỤC VỤ"
  ) {
    return "blue";
  }

  if (s === "QUA_GIO" || s === "QUÁ GIỜ") {
    return "orange";
  }

  return "green";
};

const loadDatBan = async () => {
  try {
    const [resDatBan, resBan] = await Promise.all([
      getAlldatban(),
      getAllquanlyban(),
    ]);

    // ===== LOAD BÀN =====
    const dataBan = resBan?.data?.data || resBan?.data || [];
    tables.value = dataBan.map((b) => ({
      id: b.id,
      name: "Bàn " + b.tenBanAn,
      floor: b.khuVuc?.tenKhuVuc,
    }));

    // ===== LOAD TẦNG =====
    floors.value = [...new Set(dataBan.map((b) => b.khuVuc?.tenKhuVuc))].sort(
      (a, b) => {
        const numA = parseInt(String(a || "").replace(/\D/g, "") || 0);
        const numB = parseInt(String(b || "").replace(/\D/g, "") || 0);
        return numA - numB;
      },
    );

    // mặc định chọn tầng đầu
    nextTick(() => {
      if (floors.value.length > 0 && !selectedFloor.value) {
        selectedFloor.value = floors.value[0];
      }
    });

    // ===== LOAD ĐẶT BÀN =====
    const data = resDatBan?.data?.data || resDatBan?.data || [];

    const resList = [];
    data.forEach((item) => {
      // Ẩn không hiện các lịch đã hoàn thành, đã hủy, hết hạn, từ chối
      if (isFinishedOrCancelled(item.trangThai)) {
        return;
      }

      const date = new Date(item.thoiGianDat);
      const hour = date.getHours();
      const slot = String(hour).padStart(2, "0") + ":00";
      const day = formatLocalDate(date);

      const listBan =
        Array.isArray(item.listIdBan) && item.listIdBan.length > 0
          ? item.listIdBan
          : item.idBanAn
          ? [item.idBanAn]
          : [];

      if (listBan.length === 0) return;

      listBan.forEach((idBan) => {
        const tableId = Number(idBan);
        const table = dataBan.find((b) => Number(b.id) === tableId);

        resList.push({
          idDatBan: item.id,
          maDatBan: item.maDatBan,
          tableId: tableId,
          floor: table?.khuVuc?.tenKhuVuc || item.tenKhuVuc,
          slot: slot,
          day: day,
          guest: item.tenKhachHang || "Khách",
          sdt: item.soDienThoai || "",
          soNguoi: item.soNguoi || item.soNguoiToiDa || 0,
          rawStatus: item.trangThai,
          status: convertStatus(item.trangThai),
        });
      });
    });

    reservations.value = resList;
  } catch (err) {
    console.error("Load đặt bàn lỗi:", err);
  }
};

const loadBan = async () => {
  try {
    const res = await getAllquanlyban();
    const dataBan = res?.data?.data || res?.data || [];

    tables.value = dataBan.map((b) => ({
      id: b.id,
      name: "Bàn " + b.tenBanAn,
      floor: b.khuVuc?.tenKhuVuc,
    }));

    floors.value = [...new Set(dataBan.map((b) => b.khuVuc?.tenKhuVuc))];

    if (floors.value.length > 0 && !selectedFloor.value) {
      selectedFloor.value = floors.value[0];
    }
  } catch (error) {
    console.error("Load bàn lỗi:", error);
  }
};

const filteredTables = computed(() => {
  return tables.value.filter((t) => {
    return t.floor === selectedFloor.value;
  });
});

// --- 2. XỬ LÝ CỘT TIMELINE THEO CHẾ ĐỘ ---
const timelineColumns = computed(() => {
  if (timelineMode.value === "Ngày") {
    return [
      "08:00",
      "09:00",
      "10:00",
      "11:00",
      "12:00",
      "13:00",
      "14:00",
      "15:00",
      "16:00",
      "17:00",
      "18:00",
      "19:00",
      "20:00",
      "21:00",
      "22:00",
      "23:00",
    ];
  }

  if (timelineMode.value === "Tháng") {
    const daysInMonth = new Date(
      viewDate.value.getFullYear(),
      viewDate.value.getMonth() + 1,
      0,
    ).getDate();

    return Array.from({ length: daysInMonth }, (_, i) => i + 1);
  }

  if (timelineMode.value === "Tuần") {
    const start = new Date(viewDate.value);
    const day = start.getDay() || 7;
    start.setDate(start.getDate() - day + 1); // về thứ 2

    return Array.from({ length: 7 }, (_, i) => {
      const d = new Date(start);
      d.setDate(start.getDate() + i);

      return {
        label: d.toLocaleDateString("vi-VN", { weekday: "short" }),
        value: formatLocalDate(d),
      };
    });
  }
});

// Hàm lấy data đặt bàn để hiển thị lên ô table
const getBooking = (tableId, slot) => {
  const date = viewDate.value;

  if (timelineMode.value === "Ngày") {
    const selectedDay = formatLocalDate(date);

    return filteredReservations.value.find((r) => {
      return (
        Number(r.tableId) === Number(tableId) &&
        r.day === selectedDay &&
        r.slot === slot
      );
    });
  }
  if (timelineMode.value === "Tháng") {
    return filteredReservations.value.find((r) => {
      const d = new Date(r.day);

      return (
        Number(r.tableId) === Number(tableId) &&
        d.getDate() === slot &&
        d.getMonth() === date.getMonth() &&
        d.getFullYear() === date.getFullYear()
      );
    });
  }

  if (timelineMode.value === "Tuần") {
    return filteredReservations.value.find((r) => {
      return (
        Number(r.tableId) === Number(tableId) && r.day === slot // slot = YYYY-MM-DD
      );
    });
  }
};

// --- 3. ĐIỀU HƯỚNG LỊCH MINI ---
const currentMonthLabel = computed(() => {
  const months = [
    "Tháng 1",
    "Tháng 2",
    "Tháng 3",
    "Tháng 4",
    "Tháng 5",
    "Tháng 6",
    "Tháng 7",
    "Tháng 8",
    "Tháng 9",
    "Tháng 10",
    "Tháng 11",
    "Tháng 12",
  ];
  return `${
    months[viewDate.value.getMonth()]
  }, ${viewDate.value.getFullYear()}`;
});

const changeMonth = (offset) => {
  const d = new Date(viewDate.value);
  d.setMonth(d.getMonth() + offset);
  viewDate.value = d;
};

const selectDate = (day) => {
  if (!day) return;

  const d = new Date(viewDate.value);

  if (day.current) {
    d.setDate(day.d);
  } else {
    // nếu click ngày tháng trước / sau
    if (day.d > 20) {
      d.setMonth(d.getMonth() - 1);
    } else {
      d.setMonth(d.getMonth() + 1);
    }
    d.setDate(day.d);
  }

  viewDate.value = new Date(d);
  timelineMode.value = "Ngày";

  console.log("Ngày đã chọn:", viewDate.value);
};

const isToday = (day) => {
  if (!day || !day.current) return false;

  const today = new Date();

  // nếu đã chọn ngày khác thì không highlight today
  if (viewDate.value.getDate() !== today.getDate()) return false;

  return (
    day.d === today.getDate() &&
    viewDate.value.getMonth() === today.getMonth() &&
    viewDate.value.getFullYear() === today.getFullYear()
  );
};

// --- 4. LOGIC TÍNH TOÁN LỊCH MINI (GRID 7x6) ---
const calendarDays = computed(() => {
  const y = viewDate.value.getFullYear();
  const m = viewDate.value.getMonth();
  const firstDay = new Date(y, m, 1).getDay();
  const startOffset = firstDay === 0 ? 6 : firstDay - 1; // Quy về Thứ 2 là đầu tuần
  const prevMonthLastDate = new Date(y, m, 0).getDate();
  const currentMonthLastDate = new Date(y, m + 1, 0).getDate();

  const days = [];
  // Ngày tháng trước
  for (let i = startOffset; i > 0; i--) {
    days.push({ d: prevMonthLastDate - i + 1, current: false });
  }
  // Ngày tháng hiện tại
  for (let i = 1; i <= currentMonthLastDate; i++) {
    days.push({ d: i, current: true });
  }
  // Ngày tháng sau cho đủ 42 ô
  const remaining = 42 - days.length;
  for (let i = 1; i <= remaining; i++) {
    days.push({ d: i, current: false });
  }
  return days;
});

const isSelected = (day) => {
  if (!day || !day.current) return false;

  return day.d === viewDate.value.getDate();
};

const formatLocalDate = (date) => {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
};

const filteredReservations = computed(() => {
  return reservations.value.filter((r) => {
    const matchGuest =
      !searchGuest.value ||
      r.guest.toLowerCase().includes(searchGuest.value.toLowerCase());

    const matchFloor = !selectedFloor.value || r.floor === selectedFloor.value;

    return matchGuest && matchFloor;
  });
});

const goDetail = (booking) => {
  if (booking && booking.idDatBan) {
    router.push(`/admin/dat-ban/detail/${booking.idDatBan}`);
  }
};

const handleWsMessage = () => {
  loadDatBan();
};

onMounted(() => {
  viewDate.value = new Date(); // luôn focus hôm nay
  loadDatBan();
  connectWebSocket(null, handleWsMessage);
});

onBeforeUnmount(() => {
  disconnectWebSocket(handleWsMessage);
});
</script>

<template>
  <div class="page-layout">
    <div class="page-content">
      <div v-if="currentTab === 'calendar'" class="timeline-container">
        <aside class="left-panel">
          <div class="mini-cal card">
            <div class="cal-nav">
              <strong>{{ currentMonthLabel }}</strong>
              <div class="arrows">
                <span @click="changeMonth(-1)">‹</span>
                <span @click="changeMonth(1)">›</span>
              </div>
            </div>
            <table class="cal-table">
              <thead>
                <tr>
                  <th>T2</th>
                  <th>T3</th>
                  <th>T4</th>
                  <th>T5</th>
                  <th>T6</th>
                  <th>T7</th>
                  <th>CN</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="r in 6" :key="r">
                  <td
                    v-for="c in 7"
                    :key="c"
                    :class="{
                      off: !calendarDays[(r - 1) * 7 + (c - 1)]?.current,
                      today:
                        isToday(calendarDays[(r - 1) * 7 + (c - 1)]) &&
                        !isSelected(calendarDays[(r - 1) * 7 + (c - 1)]),
                      selected: isSelected(calendarDays[(r - 1) * 7 + (c - 1)]),
                    }"
                    @click="selectDate(calendarDays[(r - 1) * 7 + (c - 1)])"
                  >
                    <span class="day-num">
                      {{ calendarDays[(r - 1) * 7 + (c - 1)]?.d }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="filter-side card p-3">
            <div class="row g-2">
              <div class="col-12">
                <label class="form-label fw-semibold">Phòng / Bàn</label>
                <select class="form-select" v-model="selectedFloor">
                  <option v-for="f in floors" :key="f" :value="f">
                    {{ f }}
                  </option>
                </select>
              </div>

              <div class="col-12">
                <label class="form-label fw-semibold">Tìm khách đặt</label>
                <input
                  v-model="searchGuest"
                  type="text"
                  class="form-control"
                  placeholder="Tên / SĐT khách"
                />
              </div>
            </div>
          </div>
        </aside>

        <section class="right-grid card">
          <div class="grid-header">
            <div class="sub-tabs">
              <span
                :class="{ active: timelineMode === 'Ngày' }"
                @click="timelineMode = 'Ngày'"
                >Ngày</span
              >
              <span
                :class="{ active: timelineMode === 'Tuần' }"
                @click="timelineMode = 'Tuần'"
              >
                Tuần
              </span>
              <span
                :class="{ active: timelineMode === 'Tháng' }"
                @click="timelineMode = 'Tháng'"
                >Tháng</span
              >
            </div>
            <div class="legend">
              <label><i class="dot blue"></i> Đã xếp</label>
              <label><i class="dot green"></i> Đã đặt</label>
              <label><i class="dot orange"></i> Quá giờ</label>
            </div>
          </div>
          <div class="scroll-area">
            <table class="timeline-table">
              <thead>
                <tr>
                  <th class="sticky-x corner">Phòng / bàn</th>
                  <th
                    v-for="col in timelineColumns"
                    :style="{ width: columnWidth + 'px' }"
                  >
                    {{ col.label || col }}
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="table in filteredTables" :key="table.id">
                  <td class="sticky-x">
                    {{ table.name || "Bàn " + table.id }}
                  </td>
                  <td
                    v-for="col in timelineColumns"
                    :key="col.value || col"
                    class="cell"
                  >
                    <template v-if="getBooking(table.id, col.value || col)">
                      <div
                        class="booking-item"
                        :class="getBooking(table.id, col.value || col).status"
                      >
                        <span class="booking-text">
                          {{ getBooking(table.id, col.value || col).guest }}
                        </span>
                      </div>
                    </template>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </div>

      <div v-else class="list-container">
        <div class="card" style="padding: 100px; text-align: center">
          Nội dung Danh sách ở đây
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-layout {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #f8fafc;
  overflow: hidden;
  font-family: var(--font-family, 'Inter', sans-serif);
}

.page-content {
  flex: 1;
  padding: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.timeline-container {
  display: flex;
  gap: 24px;
  height: 100%;
  box-sizing: border-box;
}

/* LEFT PANEL */
.left-panel {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card {
  background: #ffffff;
  border: 1px solid rgba(226, 232, 240, 0.8);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
  transition: box-shadow 0.3s ease;
}

.card:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05), 0 4px 6px -2px rgba(0, 0, 0, 0.02);
}

.mini-cal {
  padding: 20px;
}

.cal-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.cal-nav strong {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
}

.arrows {
  display: flex;
  gap: 4px;
}

.arrows span {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #64748b;
  background: #f1f5f9;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.arrows span:hover {
  background: #fee2e2;
  color: var(--color-primary, #e65a5a);
}

.cal-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  text-align: center;
}

.cal-table th {
  padding-bottom: 12px;
  font-size: 11px;
  font-weight: 600;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.cal-table td {
  padding: 4px 0;
  cursor: pointer;
}

.day-num {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  border-radius: 50%;
  font-weight: 500;
  color: #334155;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.cal-table td:hover:not(.off) .day-num {
  background: #fee2e2;
  color: var(--color-primary, #e65a5a);
}

.cal-table .off .day-num {
  color: #cbd5e1;
}

.cal-table .today .day-num {
  border: 1.5px solid var(--color-primary, #e65a5a);
  color: var(--color-primary, #e65a5a);
  background: #fff5f5;
  font-weight: 600;
}

.cal-table .selected .day-num {
  background: linear-gradient(135deg, var(--color-primary, #e65a5a), #f07575) !important;
  color: #ffffff !important;
  font-weight: 600;
  box-shadow: 0 4px 10px rgba(230, 90, 90, 0.3);
}

/* FILTER SIDE */
.filter-side {
  padding: 20px;
}

.form-label {
  font-size: 11px;
  font-weight: 600;
  color: #475569;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
  display: block;
}

.form-select,
.form-control {
  width: 100%;
  height: 40px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 8px 14px;
  font-size: 14px;
  color: #1e293b;
  background-color: #ffffff;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  box-sizing: border-box;
}

.form-select:focus,
.form-control:focus {
  border-color: var(--color-primary, #e65a5a);
  box-shadow: 0 0 0 4px rgba(230, 90, 90, 0.15);
  outline: none;
}

.filter-side .col-12:not(:last-child) {
  margin-bottom: 16px;
}

/* RIGHT GRID */
.right-grid {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.grid-header {
  height: 60px;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  flex-shrink: 0;
  background: #ffffff;
}

/* Segmented Control Style Tabs */
.sub-tabs {
  display: flex;
  gap: 4px;
  background: #f1f5f9;
  padding: 4px;
  border-radius: 8px;
}

.sub-tabs span {
  padding: 6px 16px;
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.sub-tabs span.active {
  background: #ffffff;
  color: var(--color-primary, #e65a5a);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
}

.legend {
  font-size: 13px;
  font-weight: 500;
  color: #475569;
  display: flex;
  gap: 16px;
}

.legend label {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: default;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}

.dot.blue {
  background: #4f46e5;
}

.dot.green {
  background: #10b981;
}

.dot.orange {
  background: #f59e0b;
}

/* TABLE & SCROLL AREA */
.scroll-area {
  flex: 1;
  overflow: auto;
}

.timeline-table {
  width: 100%;
  border-collapse: collapse;
}

.timeline-table th,
.timeline-table td {
  border: 1px solid #f1f5f9;
  height: 64px;
  text-align: center;
  position: relative;
  box-sizing: border-box;
}

.timeline-table th {
  background: #f8fafc;
  position: sticky;
  top: 0;
  z-index: 10;
  font-size: 11px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 2px solid #e2e8f0;
}

.sticky-x {
  position: sticky;
  left: 0;
  background: #ffffff !important;
  z-index: 11;
  width: 120px;
  font-weight: 600;
  font-size: 13px;
  color: #1e293b;
  border-right: 1px solid #e2e8f0;
  box-shadow: 4px 0 8px -4px rgba(0, 0, 0, 0.05);
}

.corner {
  z-index: 12 !important;
  background: #f8fafc !important;
  border-bottom: 2px solid #e2e8f0 !important;
}

.cell {
  background: #ffffff;
  padding: 6px;
  transition: background-color 0.2s ease;
}

.cell:hover {
  background-color: #f8fafc;
}

.booking-item {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  box-sizing: border-box;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}

.booking-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
}

.booking-text {
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

/* EVENT COLOR CLASSES */
.booking-item.blue {
  background: linear-gradient(135deg, #e0e7ff, #c7d2fe);
  border-left: 4px solid #4f46e5;
  color: #3730a3;
}

.booking-item.green {
  background: linear-gradient(135deg, #d1fae5, #a7f3d0);
  border-left: 4px solid #059669;
  color: #065f46;
}

.booking-item.orange {
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  border-left: 4px solid #d97706;
  color: #92400e;
}

/* CUSTOM SCROLLBAR */
.scroll-area::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.scroll-area::-webkit-scrollbar-track {
  background: #f1f5f9;
}

.scroll-area::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.scroll-area::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* ===== RESPONSIVE ===== */
@media (max-width: 1024px) {
  .timeline-container {
    flex-direction: column !important;
    overflow: auto !important;
    height: auto !important;
  }

  .left-panel {
    width: 100% !important;
    flex-shrink: unset !important;
    display: flex !important;
    flex-direction: row !important;
    gap: 16px !important;
  }

  .mini-cal {
    flex: 1 1 250px !important;
    margin-bottom: 0 !important;
  }

  .filter-side {
    flex: 1 1 200px !important;
  }

  .right-grid {
    width: 100% !important;
    height: 500px;
  }
}

@media (max-width: 768px) {
  .page-layout {
    height: auto !important;
    overflow: auto !important;
  }

  .page-content {
    padding: 12px !important;
    overflow: auto !important;
  }

  .timeline-container {
    flex-direction: column !important;
    gap: 12px !important;
    height: auto !important;
  }

  .left-panel {
    width: 100% !important;
    flex-direction: column !important;
  }

  .grid-header {
    flex-direction: column !important;
    align-items: flex-start !important;
    height: auto !important;
    padding: 12px !important;
    gap: 12px !important;
  }

  .scroll-area {
    overflow-x: auto !important;
  }

  .legend {
    flex-wrap: wrap !important;
    gap: 8px !important;
  }
}

@media (max-width: 480px) {
  .sub-tabs span {
    font-size: 12px !important;
    padding: 6px 12px !important;
  }

  .timeline-table th,
  .timeline-table td {
    height: 54px !important;
    font-size: 11px !important;
  }

  .sticky-x {
    font-size: 11px !important;
    width: 90px !important;
  }
}
</style>
