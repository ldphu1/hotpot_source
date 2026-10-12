<script setup>
import { getCuocTroChuyen } from "@/api/admin/cuoctrochuyen/cuocTroChuyenApi";
import { getTinNhan } from "@/api/admin/cuoctrochuyen/cuocTroChuyenApi";
import { replyTinNhan } from "@/api/admin/cuoctrochuyen/cuocTroChuyenApi";
import { ref } from "vue";
import { onMounted } from "vue";
import { onUnmounted } from "vue";
import { connectWebSocket } from "@/utils/websocket";
import { disconnectWebSocket } from "@/utils/websocket";
import { nextTick } from "vue";
import { nhanXuLy } from "@/api/admin/cuoctrochuyen/cuocTroChuyenApi";
import { ketThucXuLy } from "@/api/admin/cuoctrochuyen/cuocTroChuyenApi";
import { computed } from "vue";
const chatContainerRef = ref(null);
const inputNhanVien = ref("");

const showStickerPicker = ref(false);
const stickerResults = ref([]);
const stickerKeyword = ref("");
const GIPHY_KEY = "iXg2zYW0nhmTksiIV1FNNh0BqSsgO6Jv";

const searchStickers = async () => {
  const url = stickerKeyword.value.trim()
    ? `https://api.giphy.com/v1/stickers/search?api_key=${GIPHY_KEY}&q=${stickerKeyword.value}&limit=20`
    : `https://api.giphy.com/v1/stickers/trending?api_key=${GIPHY_KEY}&limit=20`;
  const res = await fetch(url);
  const data = await res.json();
  stickerResults.value = data.data;
};

const toggleStickerPicker = async () => {
  showStickerPicker.value = !showStickerPicker.value;
  if (showStickerPicker.value && stickerResults.value.length === 0) {
    await searchStickers();
  }
};

const guiSticker = async (sticker) => {
  const url = sticker.images.fixed_height_small.url;
  await replyTinNhan(cuocTroChuyenDangChon.value.id, url, "STICKER");
  showStickerPicker.value = false;
};

///// TỔNG CHƯA ĐỌC
const tongChuaDoc = computed(
  () =>
    danhSachCuocTroChuyen.value.filter((item) => item.soTinChuaDoc > 0).length,
);

///// DANH SÁCH CUỘC TRÒ CHUYỆN
const danhSachCuocTroChuyen = ref([]);
const cursor = ref(null);
const hasNext = ref(false);
const tenKhachHang = ref("");

///// CHỌN CUỘC TRÒ CHUYỆN
const cuocTroChuyenDangChon = ref(null);
const danhSachTinNhan = ref([]);
const cursorTinNhan = ref(null);
const hasNextTinNhan = ref(false);

//////////// TIN NHAN //////////

const nhanXuLyCuocTroChuyen = async () => {
  await nhanXuLy(cuocTroChuyenDangChon.value.id);
  cuocTroChuyenDangChon.value.loai = "NHAN_VIEN";
  await loadCuocTroChuyen();
};

const ketThucXuLyCuocTroChuyen = async () => {
  await ketThucXuLy(cuocTroChuyenDangChon.value.id);
  cuocTroChuyenDangChon.value.loai = "AI";
  await loadCuocTroChuyen();
};

const replyTinNhanNhanVien = async () => {
  const text = inputNhanVien.value.trim();
  if (!text || !cuocTroChuyenDangChon.value) return;

  await replyTinNhan(cuocTroChuyenDangChon.value.id, text);

  inputNhanVien.value = "";
};

///////////// TỰ SCROLL
const scrollToBottom = async () => {
  await nextTick();

  if (chatContainerRef.value) {
    chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight;
  }
};

const chonCuocTroChuyen = async (item) => {
  cuocTroChuyenDangChon.value = item;
  cursorTinNhan.value = null;
  danhSachTinNhan.value = [];
  item.soTinChuaDoc = 0;

  const storedReads = JSON.parse(localStorage.getItem("readChats") || "{}");
  storedReads[item.id] = item.thoiGianTinNhanCuoi || new Date().toISOString();
  localStorage.setItem("readChats", JSON.stringify(storedReads));

  await loadTinNhan();
};

const loadTinNhan = async (loadMore = false) => {
  const res = await getTinNhan(
    cuocTroChuyenDangChon.value.id,
    loadMore ? cursorTinNhan.value : null,
  );

  const { data, nextCursor, hasNext: more } = res.data.data;

  if (loadMore) {
    const mapped = data.map((tin) => ({
      ...tin,
      products: tin.products || [],
      loaiTinNhan: tin.loaiTinNhan || "TEXT",
    }));
    danhSachTinNhan.value = [...mapped, ...danhSachTinNhan.value];
  } else {
    danhSachTinNhan.value = data.map((tin) => ({
      ...tin,
      products: tin.products || [],
      loaiTinNhan: tin.loaiTinNhan || "TEXT",
    }));
    scrollToBottom();
  }

  cursorTinNhan.value = nextCursor;
  hasNextTinNhan.value = more;
};

const onScrollTinNhan = (e) => {
  const el = e.target;
  const isTop = el.scrollTop <= 10;
  if (isTop && hasNextTinNhan.value) {
    loadTinNhan(true);
  }
};

const loadCuocTroChuyen = async (loadMore = false) => {
  const res = await getCuocTroChuyen({
    tenKhachHang: tenKhachHang.value || null,
    cursor: loadMore ? cursor.value : null,
  });

  let { data, nextCursor, hasNext: more } = res.data.data;

  const storedReads = JSON.parse(localStorage.getItem("readChats") || "{}");
  data = data.map((item) => {
    if (
      storedReads[item.id] &&
      new Date(storedReads[item.id]) >= new Date(item.thoiGianTinNhanCuoi)
    ) {
      item.soTinChuaDoc = 0;
    }
    return item;
  });

  if (loadMore) {
    danhSachCuocTroChuyen.value.push(...data);
  } else {
    danhSachCuocTroChuyen.value = data;
  }

  cursor.value = nextCursor;
  hasNext.value = more;
};

const onSearch = () => {
  cursor.value = null;
  loadCuocTroChuyen();
};
const onScroll = (e) => {
  const el = e.target;
  const isBottom = el.scrollHeight - el.scrollTop <= el.clientHeight + 10;
  if (isBottom && hasNext.value) {
    loadCuocTroChuyen(true);
  }
};
const formatThoiGian = (thoiGian) => {
  if (!thoiGian) return "";

  const now = new Date();
  const date = new Date(thoiGian);

  const startOfToday = new Date(
    now.getFullYear(),
    now.getMonth(),
    now.getDate(),
  );
  const startOfWeek = new Date(startOfToday);
  startOfWeek.setDate(startOfToday.getDate() - now.getDay());

  if (date >= startOfToday) {
    // Hôm nay → hiện giờ
    return date.toLocaleTimeString("vi-VN", {
      hour: "2-digit",
      minute: "2-digit",
    });
  } else if (date >= startOfWeek) {
    // Trong tuần → hiện thứ
    return date.toLocaleDateString("vi-VN", { weekday: "long" });
  } else {
    // Khác tuần → hiện ngày tháng
    return date.toLocaleDateString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
    });
  }
};

const handleChatMessage = async (chat) => {
  if (
    cuocTroChuyenDangChon.value &&
    cuocTroChuyenDangChon.value.id === chat.idCuocTroChuyen
  ) {
    const storedReads = JSON.parse(localStorage.getItem("readChats") || "{}");
    storedReads[chat.idCuocTroChuyen] =
      chat.thoiGian || new Date().toISOString();
    localStorage.setItem("readChats", JSON.stringify(storedReads));

    danhSachTinNhan.value.push({
      idTinNhan: chat.idTinNhan,
      noiDung: chat.noiDung,
      thoiGian: chat.thoiGian,
      loaiNguoiGui: chat.loaiNguoiGui,
      loaiTinNhan: chat.loaiTinNhan || "TEXT",
      products: chat.products || [],
    });

    scrollToBottom();
  }

  await loadCuocTroChuyen();
};

onMounted(() => {
  loadCuocTroChuyen();

  connectWebSocket(null, null, handleChatMessage);
});

onUnmounted(() => {
  disconnectWebSocket(null, handleChatMessage);
});
</script>
<template>
  <div class="quan-ly-nhan-tin">
    <div class="box" style="margin-bottom: 20px">
      <div class="box-header">
        <h2 class="main-title">
          <span class="icon-wrapper">
            <i
              class="bi bi-chat-dots-fill"
              style="font-size: 24px; color: #e11d48"
            ></i>
          </span>
          Tin Nhắn Của Khách Hàng
        </h2>
        <p>Quản lý và phản hồi tin nhắn từ khách hàng</p>
      </div>
    </div>

    <div class="admin-chat-layout">
      <div class="chat-sidebar">
        <div class="sidebar-header">
          <h2>Khách hàng nhắn tin</h2>
          <span class="unread-count">{{ tongChuaDoc }} chưa đọc</span>
        </div>

        <div class="search-wrap">
          <i class="bi bi-search search-icon"></i>
          <input
            type="text"
            placeholder="Tìm tên khách hàng..."
            v-model="tenKhachHang"
            @input="onSearch"
          />
        </div>
        <div class="conversation-list" @scroll="onScroll">
          <div
            v-for="item in danhSachCuocTroChuyen"
            :key="item.id"
            class="conv-item"
            :class="{
              unread: item.soTinChuaDoc > 0,
              active: cuocTroChuyenDangChon?.id === item.id,
            }"
            @click="chonCuocTroChuyen(item)"
          >
            <div class="avatar-box">
              <img
                v-if="item.anhDaiDien"
                :src="`http://localhost:8080/uploads/${item.anhDaiDien}`"
                class="avatar-img"
              />
              <svg
                v-else
                class="avatar-svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
              <span class="status-dot online"></span>
            </div>
            <div class="conv-info">
              <div class="info-top">
                <span class="client-name">{{
                  item.tenKhachHang || item.tenKhach
                }}</span>
                <div class="time-badge-wrap">
                  <span class="meta-time">{{
                    formatThoiGian(item.thoiGianTinNhanCuoi)
                  }}</span>
                  <span
                    v-if="item.soTinChuaDoc > 0"
                    class="red-badge-dot"
                  ></span>
                </div>
              </div>
              <p
                class="preview-text"
                :class="{ 'text-bold': item.soTinChuaDoc > 0 }"
              >
                {{ item.tinNhanCuoi }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <div class="chat-main-area">
        <!-- Nếu chưa chọn cuộc trò chuyện -->
        <div v-if="!cuocTroChuyenDangChon" class="no-chat-selected">
          <div class="empty-chat-box">
            <i class="fas fa-comments empty-chat-icon"></i>

            <p>Chọn một cuộc trò chuyện để bắt đầu nhắn tin</p>
          </div>
        </div>

        <template v-else>
          <div class="chat-topbar">
            <div class="active-user-profile">
              <div class="avatar-box-small">
                <img
                  v-if="cuocTroChuyenDangChon.anhDaiDien"
                  :src="`http://localhost:8080/uploads/${cuocTroChuyenDangChon.anhDaiDien}`"
                  class="avatar-img-small"
                />
                <svg
                  v-else
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                  <circle cx="12" cy="7" r="4" />
                </svg>
              </div>
              <div>
                <h3>
                  {{
                    cuocTroChuyenDangChon.tenKhachHang ||
                    cuocTroChuyenDangChon.tenKhach
                  }}
                </h3>
                <p class="user-status-text">Đang trực tuyến</p>
              </div>
            </div>

            <!-- Nút nhận xử lý -->
            <button
              v-if="cuocTroChuyenDangChon.loai === 'AI'"
              class="btn-nhan-xu-ly"
              @click="nhanXuLyCuocTroChuyen"
            >
              Nhận xử lý
            </button>

            <button
              v-else
              class="btn-ket-thuc"
              @click="ketThucXuLyCuocTroChuyen"
            >
              Kết thúc
            </button>
          </div>

          <div
            class="chat-messages-container"
            @scroll="onScrollTinNhan"
            ref="chatContainerRef"
          >
            <div
              v-for="tin in danhSachTinNhan"
              :key="tin.idTinNhan"
              class="msg-block"
              :class="[
                tin.loaiNguoiGui === 'KHACH_HANG' ? 'incoming' : 'outgoing',
                { 'has-products': tin.products && tin.products.length > 0 },
              ]"
            >
              <div class="msg-bubble">
                <img
                  v-if="tin.loaiTinNhan === 'STICKER'"
                  :src="tin.noiDung"
                  class="msg-sticker"
                />
                <span v-else>{{ tin.noiDung }}</span>
              </div>

              <div
                v-if="tin.products && tin.products.length > 0"
                class="product-cards"
              >
                <div
                  v-for="sp in tin.products"
                  :key="sp.id"
                  class="product-card"
                >
                  <div class="product-img-wrapper">
                    <img
                      :src="`http://localhost:8080/uploads/${sp.hinhAnh}`"
                      class="product-img"
                    />
                  </div>
                  <div class="product-info">
                    <div class="product-name">{{ sp.tenSanPham }}</div>
                    <div class="product-price">
                      <span class="price-new"
                        >{{ sp.gia?.toLocaleString("vi-VN") }}đ</span
                      >
                    </div>
                  </div>
                </div>
              </div>

              <span
                class="msg-time"
                :class="{ 'text-right': tin.loaiNguoiGui !== 'KHACH_HANG' }"
              >
                {{ formatThoiGian(tin.thoiGian) }}
              </span>
            </div>
          </div>
          <div class="chat-footer-input-bar">
            <!-- Sticker Picker -->
            <div v-if="showStickerPicker" class="sticker-picker">
              <input
                type="text"
                v-model="stickerKeyword"
                placeholder="Tìm sticker..."
                class="sticker-search"
                @input="searchStickers"
              />
              <div class="sticker-grid">
                <img
                  v-for="s in stickerResults"
                  :key="s.id"
                  :src="s.images.fixed_height_small.url"
                  class="sticker-item"
                  @click="guiSticker(s)"
                />
              </div>
            </div>

            <div class="footer-input-container">
              <button
                class="sticker-btn"
                @click="toggleStickerPicker"
                title="Sticker"
              >
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  width="20"
                  height="20"
                >
                  <circle cx="12" cy="12" r="10" />
                  <path d="M8 13s1.5 2 4 2 4-2 4-2" />
                  <line
                    x1="9"
                    y1="9"
                    x2="9.01"
                    y2="9"
                    stroke-linecap="round"
                    stroke-width="3"
                  />
                  <line
                    x1="15"
                    y1="9"
                    x2="15.01"
                    y2="9"
                    stroke-linecap="round"
                    stroke-width="3"
                  />
                </svg>
              </button>
              <input
                type="text"
                placeholder="Nhập tin nhắn phản hồi cho khách hàng..."
                class="main-input"
                v-model="inputNhanVien"
                @keyup.enter="replyTinNhanNhanVien"
              />
              <div class="action-buttons">
                <button
                  class="send-btn"
                  @click="replyTinNhanNhanVien"
                  title="Gửi tin nhắn"
                >
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    class="send-icon"
                  >
                    <line x1="22" y1="2" x2="11" y2="13"></line>
                    <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-main-area {
  flex: 1;
  height: 100%;
  background: #f8fafc;
}

.no-chat-selected {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.empty-chat-box {
  text-align: center;
  background: #ffffff;
  padding: 40px 50px;
  border-radius: 7px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.08);
  max-width: 420px;
  width: 100%;
  transition: all 0.3s ease;
}

.empty-chat-box:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.12);
}

.empty-chat-icon {
  font-size: 64px;
  color: #f14848;
  margin-bottom: 20px;
}

.empty-chat-box h3 {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 10px;
}

.empty-chat-box p {
  font-size: 15px;
  color: #64748b;
  line-height: 1.6;
}
.admin-chat-layout {
  display: flex;
  width: 100%;
  height: calc(100vh - 200px);
  background-color: #ffffff;
  border-radius: 8px;
  border: 1px solid #eee;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica,
    Arial, sans-serif;
}

/* ===== HEADER ===== */
.box {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #eee;
}
.box-header {
  margin-bottom: 10px;
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
.icon-wrapper {
  width: 56px;
  height: 56px;
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
.box-header p {
  margin: 10px 0 0;
  color: #666;
  font-size: 14px;
}

/* ==================== CỘT TRÁI: SIDEBAR DANH SÁCH KHÁCH HÀNG ==================== */
.chat-sidebar {
  width: 350px;
  background-color: #ffffff;
  border-right: 1px solid #f1f2f6;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px 16px 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h2 {
  font-size: 20px;
  color: #2c3e50;
  margin: 0;
  font-weight: 700;
}

.unread-count {
  background-color: #fce4e4; /* Màu đỏ nhạt pastel */
  color: #e74c3c;
  padding: 4px 10px;
  border-radius: 7px;
  font-size: 12px;
  font-weight: 600;
}

.search-wrap {
  padding: 0 16px 12px 16px;
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 28px;
  color: #a4b0be;
  font-size: 14px;
}

.search-wrap input {
  width: 100%;
  background-color: #f8f9fa;
  border: 1px solid #edf2f7;
  padding: 10px 12px 10px 36px;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: all 0.2s;
}

.search-wrap input:focus {
  border-color: #e11d48;
  box-shadow: 0 0 0 4px rgba(225, 29, 72, 0.1);
}

/* Danh sách các item khách hàng */
.conversation-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 0 8px;
}

.conv-item {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  border: 1px solid #eee;
  border-radius: 7px;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
  position: relative;
}

.conv-item:hover {
  border-color: #ddd;
  background: #fafafa;
}

/* Khi đang chọn cuộc trò chuyện này - Đồng bộ viền đỏ */
.conv-item.active {
  border-color: #e11d48;
  background: #fff0f2;
}

.avatar-box {
  position: relative;
  width: 44px;
  height: 44px;
  background-color: #f1f2f6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #747d8c;
  flex-shrink: 0;
}

.avatar-svg {
  width: 22px;
  height: 22px;
}

.status-dot {
  position: absolute;
  bottom: 1px;
  right: 1px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 2px solid #ffffff;
}

.status-dot.online {
  background-color: #2ecc71; /* Xanh lá online */
}

.conv-info {
  flex: 1;
  min-width: 0;
}

.info-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.client-name {
  font-weight: 600;
  color: #2f3542;
  font-size: 14.5px;
}

.meta-time {
  font-size: 11px;
  color: #a4b0be;
}

.preview-text {
  margin: 0;
  font-size: 13px;
  color: #747d8c;
  white-space: nowrap;
  overflow: hidden;
  text-transform: none;
  text-overflow: ellipsis;
}

.text-bold {
  font-weight: 600;
  color: #2f3542;
}

.time-badge-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.red-badge-dot {
  width: 8px;
  height: 8px;
  background-color: #e74c3c;
  border-radius: 50%;
  flex-shrink: 0;
  display: inline-block;
  box-shadow: 0 0 0 0 rgba(231, 76, 60, 0.4);
  animation: pulse-dot 1.5s infinite;
}

@keyframes pulse-dot {
  0% {
    box-shadow: 0 0 0 0 rgba(231, 76, 60, 0.7);
  }
  70% {
    box-shadow: 0 0 0 6px rgba(231, 76, 60, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(231, 76, 60, 0);
  }
}

.chat-main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #fffafa;
  overflow-x: hidden;
}

.chat-topbar {
  padding: 14px 24px;
  background-color: #ffffff;
  border-bottom: 1px solid #f1f2f6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.active-user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-box-small {
  width: 36px;
  height: 36px;
  background-color: #f1f2f6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #747d8c;
}

.active-user-profile h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #2f3542;
}

.user-status-text {
  margin: 2px 0 0 0;
  font-size: 12px;
  color: #2ecc71;
}

/* Vùng chứa các tin nhắn */
.chat-messages-container {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.date-line {
  text-align: center;
  font-size: 11px;
  color: #a4b0be;
  margin-bottom: 8px;
}

/* Cấu trúc bong bóng tin nhắn */
.msg-block {
  display: flex;
  flex-direction: column;
  max-width: 65%;
  min-width: 0;
  transition: max-width 0.2s ease;
}

.msg-block.has-products {
  max-width: 85%;
  width: 100%;
}

.msg-bubble {
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.5;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
}

.incoming {
  align-self: flex-start;
}

.incoming .msg-bubble {
  background-color: #ffffff;
  color: #2f3542;
  border-radius: 4px 16px 16px 16px;
  border: 1px solid #edf2f7;
}

.incoming .product-cards {
  margin-right: auto;
}

.outgoing {
  align-self: flex-end;
}

.outgoing .msg-bubble {
  background-color: #f14949;
  color: #ffffff;
  border-radius: 16px 16px 4px 16px;
}

.outgoing .product-cards {
  margin-left: auto;
}

.msg-time {
  font-size: 11px;
  color: #a4b0be;
  margin-top: 4px;
  padding: 0 4px;
}

.text-right {
  text-align: right;
}

/* ==================== THANH PHẢN HỒI DƯỚI ĐÁY ==================== */
.chat-footer-input-bar {
  padding: 16px 24px;
  background-color: #ffffff;
  border-top: 1px solid #f1f2f6;
}

/* Hộp bọc Input viền màu đỏ nhạt pastel giống khung chat client */
.footer-input-container {
  display: flex;
  align-items: center;
  border: 1.5px solid #e74c3c;
  border-radius: 24px;
  padding: 4px 6px 4px 16px;
  background-color: #ffffff;
}

.main-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  color: #2f3542;
  padding: 8px 0;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}

.inner-tool-btn {
  background: none;
  border: none;
  color: #747d8c;
  cursor: pointer;
  padding: 6px;
  display: flex;
  align-items: center;
  transition: color 0.2s;
}

.inner-tool-btn:hover {
  color: #2f3542;
}

.tool-icon {
  width: 20px;
  height: 20px;
}

/* Nút bấm gửi tin hình tròn màu đỏ nhạt */
.send-btn {
  background-color: #e74c3c;
  color: #ffffff;
  border: none;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}

.send-btn:hover {
  background-color: #c0392b;
}

.send-icon {
  width: 16px;
  height: 16px;
  transform: translate(-1px, 1px);
}

.product-cards {
  display: flex;
  flex-direction: row !important;
  flex-wrap: nowrap !important;
  gap: 12px;
  margin-top: 8px;
  width: 100%;
  max-width: 100%;
  overflow-x: auto;
  padding-bottom: 8px;
}

/* Hiển thị thanh cuộn ngang mỏng, đẹp để khách biết có thể cuộn */
.product-cards::-webkit-scrollbar {
  height: 6px;
}
.product-cards::-webkit-scrollbar-track {
  background: transparent;
}
.product-cards::-webkit-scrollbar-thumb {
  background-color: #d1d5db;
  border-radius: 10px;
}
.product-cards::-webkit-scrollbar-thumb:hover {
  background-color: #9ca3af;
}
.product-card {
  position: relative;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 12px;
  padding: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #edf2f7;
  transition: all 0.2s ease;

  min-width: 130px !important;
  max-width: 130px !important;
  flex-shrink: 0 !important;
}
.product-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}
.product-img-wrapper {
  width: 100%;
  height: 90px;
  overflow: hidden;
  border-radius: 8px;
}
.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.product-name {
  font-size: 11px;
  font-weight: 600;
  color: #2d3436;
  line-height: 1.3;
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 30px; /* Cố định chiều cao để không bị lệch giá */
}
.price-old {
  font-size: 11px;
  color: #95a5a6;
  text-decoration: line-through;
  display: block;
}
.price-new {
  font-size: 13px;
  font-weight: 700;
  color: #e74c3c;
}

/* ////////////// NhẬN XỬ LÝ /////// */
.btn-nhan-xu-ly {
  background-color: #e74c3c;
  color: white;
  border: none;
  padding: 8px 18px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-nhan-xu-ly:hover {
  background-color: #c0392b;
}

.badge-nhan-vien {
  background-color: #fce4e4;
  color: #e74c3c;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

/* ////////////// BTN KẾT THÚC  */
.btn-ket-thuc {
  background-color: #ffffff;
  color: #e74c3c;
  border: 1.5px solid #e74c3c;
  padding: 8px 18px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-ket-thuc:hover {
  background-color: #fce4e4;
}

.sticker-picker {
  background: #fff;
  border: 1px solid #f1f2f6;
  border-radius: 12px;
  padding: 8px;
  margin-bottom: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.sticker-search {
  width: 100%;
  border: 1px solid #eee;
  border-radius: 20px;
  padding: 6px 12px;
  font-size: 13px;
  outline: none;
  margin-bottom: 8px;
}

.sticker-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 6px;
  max-height: 180px;
  overflow-y: auto;
}

.sticker-item {
  width: 100%;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;
}

.sticker-item:hover {
  transform: scale(1.1);
}

.sticker-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0 8px;
  flex-shrink: 0;
  color: #747d8c;
  display: flex;
  align-items: center;
}

.sticker-btn:hover {
  color: #e74c3c;
}

.msg-sticker {
  width: 120px;
  border-radius: 8px;
}

.msg-sticker {
  width: 120px;
  border-radius: 8px;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.avatar-img-small {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}
</style>
