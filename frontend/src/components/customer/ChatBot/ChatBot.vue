<script setup>
import { ref, nextTick } from "vue";
import { chatBotAI } from "@/api/customer/chatbot/chatBotApi";
import { onMounted, onUnmounted } from "vue";
import { connectWebSocket, disconnectWebSocket } from "@/utils/websocket";
import { getTinNhanKhach } from "@/api/customer/chatbot/chatBotApi";
import { themVaoGioHang } from "@/api/customer/themvaogiohang/themVaoGioHangApi";
import { getCuocTroChuyenHienTai } from "@/api/customer/chatbot/chatBotApi";
import { useUtils } from "@/utils/helper";

const { notify } = useUtils();

///////////////  STICKER ////////////////
const showStickerPicker = ref(false);
const stickerResults = ref([]);
const stickerKeyword = ref("");
const GIPHY_KEY = "iXg2zYW0nhmTksiIV1FNNh0BqSsgO6Jv";

const searchStickers = async () => {
  if (!stickerKeyword.value.trim()) {
    // Load trending stickers mặc định
    const res = await fetch(
      `https://api.giphy.com/v1/stickers/trending?api_key=${GIPHY_KEY}&limit=20`,
    );
    const data = await res.json();
    stickerResults.value = data.data;
    return;
  }
  const res = await fetch(
    `https://api.giphy.com/v1/stickers/search?api_key=${GIPHY_KEY}&q=${stickerKeyword.value}&limit=20`,
  );
  const data = await res.json();
  stickerResults.value = data.data;
};

const guiSticker = (sticker) => {
  const url = sticker.images.fixed_height_small.url;
  messages.value.push({
    role: "user",
    content: url,
    isSticker: true,
    products: [],
    time: getCurrentTime(),
  });
  showStickerPicker.value = false;
  scrollToBottom();
};

const toggleStickerPicker = async () => {
  showStickerPicker.value = !showStickerPicker.value;
  if (showStickerPicker.value && stickerResults.value.length === 0) {
    await searchStickers();
  }
};
const addToCart = async (sp) => {
  const token = localStorage.getItem("token");
  const danhMuc = sp.tenDanhMuc?.trim().toLowerCase();

  if (!token) {
    let cart = JSON.parse(localStorage.getItem("cart") || "[]");

    const getTongTheoDanhMuc = (items, listDanhMuc) => {
      return items
        .filter((i) =>
          listDanhMuc.includes((i.tenDanhMuc || "").trim().toLowerCase()),
        )
        .reduce((sum, i) => sum + i.soLuong, 0);
    };

    const getThamSo = (code) => {
      const list = JSON.parse(localStorage.getItem("tham_so_he_thong") || "[]");
      const found = list.find((item) => item.maThamSo === code);
      if (found) return parseInt(found.giaTri);
      if (code === "MAX_LAU") return 3;
      if (code === "MAX_DO_NHUNG_LAU") return 10;
      if (code === "MAX_DO_UONG") return 20;
      if (code === "MAX_DO_AN_KEM") return 10;
      if (code === "MAX_DO_TRANG_MIENG") return 20;
      return 99;
    };

    const tongLau = getTongTheoDanhMuc(cart, ["lẩu"]);
    const tongDoNhungLau = getTongTheoDanhMuc(cart, ["đồ nhúng lẩu"]);
    const tongDoUong = getTongTheoDanhMuc(cart, ["đồ uống"]);
    const tongDoTrangMieng = getTongTheoDanhMuc(cart, ["đồ tráng miệng"]);
    const toDongAnKem = getTongTheoDanhMuc(cart, ["đồ ăn kèm"]);

    const MAX_LAU = getThamSo("MAX_LAU");
    const MAX_DO_NHUNG_LAU = getThamSo("MAX_DO_NHUNG_LAU");
    const MAX_DO_UONG = getThamSo("MAX_DO_UONG");
    const MAX_DO_AN_KEM = getThamSo("MAX_DO_AN_KEM");
    const MAX_DO_TRANG_MIENG = getThamSo("MAX_DO_TRANG_MIENG");

    if (danhMuc === "lẩu" && tongLau + 1 > MAX_LAU) {
      notify.error("Chỉ được tối đa 3 lẩu");
      return;
    }
    if (
      ["đồ nhúng lẩu"].includes(danhMuc) &&
      tongDoNhungLau + 1 > MAX_DO_NHUNG_LAU
    ) {
      notify.error("Chỉ được tối đa 10 đồ nhúng lẩu");
      return;
    }
    if (["đồ uống"].includes(danhMuc) && tongDoUong + 1 > MAX_DO_UONG) {
      notify.error("Chỉ được tối đa 20 đồ uống");
      return;
    }
    if (["đồ ăn kèm"].includes(danhMuc) && toDongAnKem + 1 > MAX_DO_AN_KEM) {
      notify.error("Chỉ được tối đa 10 đồ ăn kèm");
      return;
    }
    if (
      ["đồ tráng miệng"].includes(danhMuc) &&
      tongDoTrangMieng + 1 > MAX_DO_TRANG_MIENG
    ) {
      notify.error("Chỉ được tối đa 20 đồ tráng miệng");
      return;
    }

    const index = cart.findIndex((i) => i.id === sp.id);
    if (index !== -1) {
      cart[index].soLuong += 1;
    } else {
      cart.push({ ...sp, soLuong: 1 });
    }
    localStorage.setItem("cart", JSON.stringify(cart));
    window.dispatchEvent(new Event("cart-updated")); // thông báo cho trang thực đơn
  } else {
    try {
      await themVaoGioHang({ idSanPham: sp.id, soLuong: 1 });
      window.dispatchEvent(new Event("cart-updated"));
    } catch (err) {
      console.error("Lỗi thêm vào giỏ:", err);
      const message = err?.response?.data?.message || "Không thể thêm vào giỏ hàng";
      notify.error(message);
    }
  }
};

const xacNhanDatBan = async (msg) => {
  msg.canConfirm = false;
  await sendMessageWithText("Xác nhận");
};

const huyDatBan = async (msg) => {
  msg.canConfirm = false;
  await sendMessageWithText("Huỷ");
};
const sendMessageWithText = async (text) => {
  inputMessage.value = text;
  await sendMessage();
};

const isOpen = ref(false);
const chatBody = ref(null);
const showQuickQuestions = ref(true);
const idCuocTroChuyenHienTai = ref(null);

const getSessionId = () => {
  let sessionId = localStorage.getItem("chatSessionId");
  if (!sessionId) {
    sessionId = crypto.randomUUID();
    localStorage.setItem("chatSessionId", sessionId);
  }
  return sessionId;
};

const closeQuickQuestions = () => {
  showQuickQuestions.value = false;
};

const messages = ref([
  {
    role: "bot",
    content:
      "Xin chào! Tôi là trợ lý của FourSpiceHotpot. Tôi có thể giúp gì cho bạn không?",
    products: [],
    time: new Date().toLocaleTimeString("vi-VN", {
      hour: "2-digit",
      minute: "2-digit",
    }),
  },
]);

const getCurrentTime = () => {
  return new Date().toLocaleTimeString("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
  });
};

const inputMessage = ref("");
const isTyping = ref(false);

const scrollToBottom = async () => {
  await nextTick();
  if (chatBody.value) {
    chatBody.value.scrollTo({
      top: chatBody.value.scrollHeight,
      behavior: "smooth",
    });
  }
};
const toggleChat = async () => {
  isOpen.value = !isOpen.value;
  if (isOpen.value) {
    showQuickQuestions.value = true;
    await nextTick();
    await scrollToBottom();
  }
};

const sendMessage = async () => {
  const text = inputMessage.value.trim();
  if (!text) return;

  messages.value.push({
    role: "user",
    content: text,
    products: [],
    time: getCurrentTime(),
  });
  inputMessage.value = "";

  // Set isTyping TRƯỚC khi scroll để Vue render 3 dot ngay lập tức
  isTyping.value = true;
  await scrollToBottom();

  try {
    const res = await chatBotAI(text, getSessionId());

    if (res.data.data.idCuocTroChuyen) {
      idCuocTroChuyenHienTai.value = res.data.data.idCuocTroChuyen;
      localStorage.setItem(
        "idCuocTroChuyenHienTai",
        res.data.data.idCuocTroChuyen,
      );
    }

    // Chỉ hiện bot reply khi loaiNguoiGui là AI
    if (res.data.data.loaiNguoiGui === "AI") {
      messages.value.push({
        role: "bot",
        content: res.data.data.noiDung,
        products: res.data.data.products || [],
        canConfirm: res.data.data.canConfirm || false,
        time: getCurrentTime(),
      });
      await scrollToBottom();
    }
  } catch (e) {
    messages.value.push({
      role: "bot",
      content: "Xin lỗi, có lỗi xảy ra. Vui lòng thử lại!",
      products: [],
      time: getCurrentTime(),
    });
    await scrollToBottom();
  } finally {
    isTyping.value = false;
    await scrollToBottom();
  }
};
const sendQuickQuestion = (text) => {
  inputMessage.value = text;
  sendMessage();
};

const getImageUrl = (img) => {
  if (!img) return "https://via.placeholder.com/50";
  return `http://localhost:8080/uploads/${img}`;
};

const loadTinNhanKhach = async () => {
  const token = localStorage.getItem("token");
  let convId = null;

  try {
    if (token) {
      // Đã login → hỏi BE cuộc trò chuyện hiện tại của khách hàng này
      const res = await getCuocTroChuyenHienTai();
      convId = res.data.data?.id;
    } else {
      // Chưa login → dùng id lưu trên trình duyệt (khách vãng lai)
      convId = localStorage.getItem("idCuocTroChuyenHienTai");
    }

    if (!convId) return;

    idCuocTroChuyenHienTai.value = Number(convId);
    localStorage.setItem("idCuocTroChuyenHienTai", convId);

    const res = await getTinNhanKhach(convId);
    const tinNhans = res.data.data.data;

    messages.value = tinNhans.map((tin) => ({
      role: tin.loaiNguoiGui === "KHACH_HANG" ? "user" : "bot",
      content: tin.noiDung,
      products: tin.products || [],
      time: new Date(tin.thoiGian).toLocaleTimeString("vi-VN", {
        hour: "2-digit",
        minute: "2-digit",
      }),
    }));
    await scrollToBottom();
  } catch (e) {
    console.error("Không load được tin nhắn:", e);
  }
};

// Lưu lại reference để có thể unregister đúng khi unmount
const chatHandler = (chat) => {
  // Chỉ nhận tin nhắn nhân viên, bỏ qua AI và khách
  if (chat.loaiNguoiGui !== "NHAN_VIEN") return;

  // Chỉ hiọn nếu đúng cuộc trò chuyện
  if (idCuocTroChuyenHienTai.value !== chat.idCuocTroChuyen) return;

  messages.value.push({
    role: "bot",
    content: chat.noiDung,
    products: [],
    time: getCurrentTime(),
  });
  scrollToBottom();
};

const resetChatUI = () => {
  idCuocTroChuyenHienTai.value = null;
  messages.value = [
    {
      role: "bot",
      content:
        "Xin chào! Tôi là trợ lý của FourSpiceHotpot. Tôi có thể giúp gì cho bạn không?",
      products: [],
      time: getCurrentTime(),
    },
  ];
};
const handleAuthChanged = async () => {
  resetChatUI();
  await loadTinNhanKhach();
};

onMounted(async () => {
  await loadTinNhanKhach();
  connectWebSocket(null, null, chatHandler);

  window.addEventListener("open-chatbot", () => {
    if (!isOpen.value) {
      toggleChat();
    }
  });
  window.addEventListener("auth-changed", handleAuthChanged);
});

onUnmounted(() => {
  disconnectWebSocket(null, chatHandler);
  window.removeEventListener("open-chatbot", () => {});
  window.removeEventListener("auth-changed", handleAuthChanged);
});
</script>
<template>
  <div class="chat-wrapper">
    <div v-if="isOpen" class="chat-box">
      <div class="chat-header">
        <div class="header-profile">
          <div class="avatar-container">
            <svg
              class="avatar-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
            <span class="online-status"></span>
          </div>
          <div class="header-info">
            <div class="bot-name">Hỗ trợ FourSpiceHotpot</div>
            <div class="bot-status">Phản hồi ngay lập tức</div>
          </div>
        </div>
        <button class="close-btn" @click="toggleChat">×</button>
      </div>

      <div class="chat-body" ref="chatBody">
        <div class="time-divider">
          {{
            new Date().toLocaleTimeString("vi-VN", {
              hour: "2-digit",
              minute: "2-digit",
            })
          }}
        </div>

        <template v-for="(msg, index) in messages" :key="index">
          <!-- Bot -->
          <div v-if="msg.role === 'bot'" class="msg-row bot-row">
            <div class="msg-avatar">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path
                  d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"
                />
              </svg>
            </div>
            <div>
              <div class="msg-bubble bot-bubble">{{ msg.content }}</div>

              <!-- Product cards -->
              <div
                v-if="msg.products && msg.products.length > 0"
                class="product-cards"
              >
                <div
                  v-for="sp in msg.products"
                  :key="sp.id"
                  class="product-card"
                >
                  <div class="product-img-wrapper">
                    <img :src="getImageUrl(sp.hinhAnh)" class="product-img" />
                  </div>
                  <div class="product-info">
                    <div class="product-name">{{ sp.tenSanPham }}</div>
                    <div class="product-price">
                      <span class="price-old"
                        >{{ sp.gia?.toLocaleString("vi-VN") }}đ</span
                      >
                      <span class="price-new"
                        >{{ sp.giaSauGiam?.toLocaleString("vi-VN") }}đ</span
                      >
                    </div>
                  </div>
                  <button class="add-to-cart-btn" @click="addToCart(sp)">
                    <i class="bi bi-cart-plus"></i>
                  </button>
                </div>
              </div>
              <div v-if="msg.canConfirm" class="confirm-buttons">
                <button class="btn-xac-nhan" @click="xacNhanDatBan(msg)">
                  Xác nhận đặt bàn
                </button>
                <button class="btn-huy" @click="huyDatBan(msg)">Huỷ</button>
              </div>

              <div class="msg-time">{{ msg.time }}</div>
            </div>
          </div>

          <!-- User -->
          <div v-else class="msg-row user-row">
            <div>
              <img
                v-if="msg.isSticker"
                :src="msg.content"
                class="msg-sticker"
              />
              <div
                v-else
                class="msg-bubble user-bubble"
                style="white-space: pre-line"
              >
                {{ msg.content }}
              </div>
              <div class="msg-time user-time">{{ msg.time }}</div>
            </div>
          </div>
        </template>

        <!-- Typing indicator -->
        <div v-if="isTyping" class="msg-row bot-row">
          <div class="msg-avatar">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <path
                d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"
              />
            </svg>
          </div>
          <div class="msg-bubble bot-bubble typing-bubble">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </div>
        </div>
      </div>

      <div v-if="showQuickQuestions" class="quick-questions">
        <div class="qq-title">
          <span>CÂU HỎI THƯỜNG GẶP</span>
          <button class="qq-close" @click="closeQuickQuestions">×</button>
        </div>
        <div class="qq-tags">
          <button class="tag-btn" @click="sendQuickQuestion('Gặp nhân viên')">
            Gặp nhân viên
          </button>
          <button class="tag-btn" @click="sendQuickQuestion('Đặt bàn')">
            Đặt bàn
          </button>
          <button class="tag-btn" @click="sendQuickQuestion('Ưu đãi')">
            Ưu đãi
          </button>
          <button class="tag-btn" @click="sendQuickQuestion('Set lẩu hot')">
            Set lẩu hot
          </button>
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
            placeholder="Nhập câu hỏi của bạn..."
            class="main-input"
            v-model="inputMessage"
            @keyup.enter="sendMessage"
          />
          <div class="action-buttons">
            <button class="send-btn" @click="sendMessage" title="Gửi">
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
    </div>

    <button
      v-if="!isOpen"
      class="chat-trigger-btn gamer-red"
      @click="toggleChat"
    >
      <div class="btn-glow"></div>
      <svg
        class="trigger-icon"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2.5"
      >
        <path
          d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"
        />
        <circle cx="8" cy="10" r="1.5" fill="currentColor" stroke="none" />
        <circle cx="12" cy="10" r="1.5" fill="currentColor" stroke="none" />
        <circle cx="16" cy="10" r="1.5" fill="currentColor" stroke="none" />
      </svg>
      <span class="btn-text">Hỗ trợ</span>
    </button>
  </div>
</template>

<style scoped>
.product-img-wrapper {
  position: relative;
  width: 100%;
  height: 90px;
  overflow: hidden;
  border-radius: 8px;
  display: block;
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.add-to-cart-btn {
  position: absolute;
  right: 8px;
  bottom: 8px;
  z-index: 10;

  width: 28px;
  height: 28px;
  border-radius: 50%;

  background: #e74c3c;
  color: #fff;
  border: none;

  display: flex;
  align-items: center;
  justify-content: center;

  padding: 0;
}

.add-to-cart-btn:hover {
  background-color: #c0392b;
}
.product-cards {
  display: flex;
  flex-direction: row;
  gap: 8px;
  margin-top: 8px;
  max-width: 280px;
  overflow-x: auto;
  padding-bottom: 4px;
  scrollbar-width: none; /* ẩn scrollbar Firefox */
}

.product-cards::-webkit-scrollbar {
  display: none; /* ẩn scrollbar Chrome */
}

.product-card {
  position: relative;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 12px;
  padding: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #f1f2f6;
  min-width: 130px;
  max-width: 130px;
  flex-shrink: 0;
}

.product-img {
  width: 100%;
  height: 90px;
  border-radius: 8px;
  object-fit: cover;
}

.product-info {
  margin-top: 6px;
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

.product-price {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.price-old {
  font-size: 11px;
  color: #95a5a6;
  text-decoration: line-through;
}

.price-new {
  font-size: 13px;
  font-weight: 700;
  color: #e74c3c;
}

.price-discount {
  font-size: 10px;
  background: #fce4e4;
  color: #c0392b;
  padding: 2px 6px;
  border-radius: 8px;
  font-weight: 600;
}
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
.msg-time {
  font-size: 10px;
  color: #95a5a6;
  margin-top: 4px;
  padding-left: 8px;
}

.user-time {
  text-align: right;
  padding-right: 8px;
}

.chat-wrapper {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica,
    Arial, sans-serif;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

/* ////////////// HÌNH CHAT  */
.chat-trigger-btn.gamer-red {
  position: fixed;
  bottom: 50px;
  right: 15px;
  z-index: 9999;
  position: relative;
  height: 55px;
  padding: 0 26px;
  border-radius: 30px;
  background: linear-gradient(135deg, #7a0000, #e60000, #ff4d4d);
  background-size: 200% 200%;
  color: #ffffff;
  border: 2px solid #ff3333;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-family: "Arial", sans-serif;
  font-weight: 900;
  font-size: 16px;
  text-transform: uppercase;
  letter-spacing: 1.5px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.6);
  box-shadow: 0 0 20px rgba(230, 0, 0, 0.6),
    inset 0 0 10px rgba(255, 255, 255, 0.2);
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  animation: red-gradient 4s ease infinite, pulse-glow 2s infinite alternate;
}

.btn-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: inherit;
  background-size: 200% 200%;
  filter: blur(12px);
  opacity: 0.6;
  z-index: -1;
  animation: red-gradient 4s ease infinite;
}

.chat-trigger-btn.gamer-red::before {
  content: "";
  position: absolute;
  top: -50%;
  left: -60%;
  width: 35%;
  height: 200%;
  background: linear-gradient(
    to right,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.75) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  transform: rotate(25deg);
  animation: light-sweep 2.2s infinite linear;
}

.trigger-icon {
  width: 24px;
  height: 24px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.4));
  animation: icon-bounce 2s infinite ease-in-out;
}

.btn-text {
  position: relative;
  z-index: 1;
  font-family: "Be Vietnam Pro", sans-serif;
  font-weight: 700;
}
.chat-trigger-btn.gamer-red:hover {
  transform: scale(1.08) translateY(-3px);
  border-color: #ffffff;
  box-shadow: 0 0 35px rgba(255, 51, 51, 1),
    inset 0 0 15px rgba(255, 255, 255, 0.4);
}

@keyframes red-gradient {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

@keyframes pulse-glow {
  0% {
    transform: scale(1);
    box-shadow: 0 0 15px rgba(230, 0, 0, 0.5);
  }
  100% {
    transform: scale(1.03);
    box-shadow: 0 0 25px rgba(255, 77, 77, 0.8);
  }
}

@keyframes light-sweep {
  0% {
    left: -70%;
  }
  100% {
    left: 150%;
  }
}

@keyframes icon-bounce {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-4px);
  }
}
/* ///////////////////////////// */
.chat-box {
  width: 360px;
  height: 580px;
  background-color: #ffffff;
  border-radius: 20px;
  box-shadow: 0 12px 36px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

/* Thanh Header màu xanh lá */
.chat-header {
  background-color: #eb4747;
  color: #ffffff;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-profile {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-container {
  position: relative;
  width: 42px;
  height: 42px;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-icon {
  width: 22px;
  height: 22px;
  color: #ffffff;
}

.online-status {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 8px;
  height: 8px;
  background-color: #ffffff;
  border-radius: 50%;
}

.header-info {
  display: flex;
  flex-direction: column;
}

.bot-name {
  font-weight: 600;
  font-size: 15px;
}

.bot-status {
  font-size: 11px;
  opacity: 0.9;
  margin-top: 2px;
}

.close-btn {
  background: rgba(255, 255, 255, 0.15);
  border: none;
  color: #ffffff;
  font-size: 18px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-body {
  flex: 1;
  padding: 16px;
  background-color: #f5f6fa;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.time-divider {
  text-align: center;
  font-size: 11px;
  color: #a4b0be;
}

.msg-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  max-width: 85%;
}

.bot-row {
  align-self: flex-start;
}

.user-row {
  align-self: flex-end;
}

.msg-avatar {
  width: 28px;
  height: 28px;
  background-color: #ef5454;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px;
  flex-shrink: 0;
}

.msg-bubble {
  padding: 12px 16px;
  font-size: 13.5px;
  line-height: 1.5;
}

.bot-bubble {
  background-color: #ffffff;
  color: #2d3436;
  border-radius: 16px 16px 16px 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
}

.user-bubble {
  background-color: #ee4c4c;
  color: #ffffff;
  border-radius: 16px 16px 4px 16px;
}

/* Ba chấm chạy */
.typing-bubble {
  display: flex;
  gap: 4px;
  padding: 12px 18px;
}

.typing-bubble .dot {
  width: 6px;
  height: 6px;
  background-color: #b2bec3;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.typing-bubble .dot:nth-child(1) {
  animation-delay: -0.32s;
}
.typing-bubble .dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes bounce {
  0%,
  80%,
  100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

/* Khối câu hỏi thường gặp */
.quick-questions {
  background-color: #ffffff;
  border-top: 1px solid #f1f2f6;
  padding: 12px 16px;
}

.qq-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 11px;
  font-weight: 700;
  color: #c0392b;
  margin-bottom: 8px;
}

.qq-close {
  background: none;
  border: none;
  color: #95a5a6;
  font-size: 20px;
  cursor: pointer;
}

.qq-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-btn {
  background-color: #fce4e4;
  color: #c0392b;
  border: none;
  padding: 8px 14px;
  border-radius: 16px;
  font-size: 10px;
  cursor: pointer;
  font-weight: 500;
  white-space: nowrap;
  transition: all 0.2s ease;
}

.tag-btn:hover {
  background-color: #ee4c4c;
  color: white;
}

/* Khung input nhập liệu đáy */
.chat-footer-area {
  padding: 12px 16px;
  background-color: #ffffff;
  border-top: 1px solid #f1f2f6;
}

.input-container {
  display: flex;
  align-items: center;
  border: 1.5px solid #e74c3c;
  border-radius: 10px;
  overflow: hidden;
  background-color: #ffffff;
}

.chat-input {
  flex: 1;
  padding: 12px 14px;
  border: none;
  font-size: 13.5px;
  outline: none;
  background: transparent;
}

.send-btn {
  background-color: #e74c3c;
  color: #ffffff;
  border: none;
  width: 46px;
  height: 44px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
  padding: 0;
}

.send-btn:hover {
  background-color: #c0392b;
}

.send-icon {
  width: 18px;
  height: 18px;
  transform: translate(-1px, 1px);
}

.powered-by {
  text-align: center;
  font-size: 10px;
  color: #b2bec3;
  margin-top: 8px;
}

.powered-by span {
  font-weight: 700;
  color: #e74c3c;
}

.confirm-buttons {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}
.btn-xac-nhan {
  background-color: #e74c3c;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}
.btn-huy {
  background-color: #ffffff;
  color: #e74c3c;
  border: 1.5px solid #e74c3c;
  padding: 8px 16px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
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
  font-size: 18px;
  cursor: pointer;
  padding: 0 8px;
  flex-shrink: 0;
}

.msg-sticker {
  width: 120px;
  border-radius: 8px;
}
</style>
