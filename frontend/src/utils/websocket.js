import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

let stompClient = null;
let isConnecting = false;

const handlers = {
  onMessage: new Set(),
  onChatMessage: new Set(),
  onForceLogout: new Set(),
};
const subscribedDatBan = new Set();
const subscribedMaDatBan = new Set();
const subscribedIdHoaDon = new Set();
const subscribedForceLogout = new Set();

const requestedDatBan = new Set();
const requestedMaDatBan = new Set();
const requestedIdHoaDon = new Set();
const requestedForceLogout = new Set();

const subscribeDatBanInternal = (idDatBan) => {
  if (!idDatBan || subscribedDatBan.has(idDatBan)) return;
  subscribedDatBan.add(idDatBan);
  stompClient.subscribe(`/topic/datban/${idDatBan}`, (message) => {
    const data = JSON.parse(message.body);
    handlers.onMessage.forEach((fn) => fn(data));
  });
};

///////////////////// LỊCH SỬ DETAIL
const subscribeIdHoaDonInternal = (idHoaDon) => {
  if (!idHoaDon || subscribedIdHoaDon.has(idHoaDon)) return;
  subscribedIdHoaDon.add(idHoaDon);
  stompClient.subscribe(`/topic/hoa-don-detail/${idHoaDon}`, (message) => {
    const data = JSON.parse(message.body);
    handlers.onMessage.forEach((fn) => fn({ type: "HOA_DON_DETAIL", ...data }));
  });
};

export const subscribeIdHoaDon = (idHoaDon) => {
  ensureConnected(null, null, idHoaDon);
};

export const connectWebSocketHoaDonDetail = (idHoaDon, onMessage) => {
  if (onMessage) handlers.onMessage.add(onMessage);
  ensureConnected(null, null, idHoaDon);
};
////////////////////////////////////////////////
///////////////////// FORCE LOGOUT NHÂN VIÊN
const subscribeForceLogoutInternal = (idNhanVien) => {
  if (!idNhanVien) return;
  const strId = String(idNhanVien);
  if (subscribedForceLogout.has(strId)) return;
  subscribedForceLogout.add(strId);
  console.log(`[WS] Subscribing to /topic/force-logout/${strId}`);
  stompClient.subscribe(`/topic/force-logout/${strId}`, () => {
    console.warn(`[WS] Nhận tín hiệu FORCE_LOGOUT cho idNhanVien: ${strId}`);
    if (handlers.onForceLogout.size > 0) {
      handlers.onForceLogout.forEach((fn) => fn());
    } else {
      localStorage.removeItem("token");
      localStorage.removeItem("refreshToken");
      localStorage.removeItem("idNhanVien");
      localStorage.removeItem("role");
      localStorage.removeItem("name");
      localStorage.removeItem("avatar");
      localStorage.removeItem("permissions");
      window.location.href = "/admin/login";
    }
  });
};

export const connectWebSocketForceLogout = (idNhanVien, onForceLogout) => {
  if (onForceLogout) handlers.onForceLogout.add(onForceLogout);
  if (idNhanVien) {
    requestedForceLogout.add(String(idNhanVien));
  }
  ensureConnected(null, null, null, idNhanVien);
};

/////////////////////////// TRA CỨU /////////////////////////

const subscribeMaDatBanInternal = (maDatBan) => {
  if (!maDatBan || subscribedMaDatBan.has(maDatBan)) {
    return;
  }
  subscribedMaDatBan.add(maDatBan);
  stompClient.subscribe(`/topic/dat-ban/ma/${maDatBan}`, (message) => {
    const data = JSON.parse(message.body);
    handlers.onMessage.forEach((fn) =>
      fn({ type: "DAT_BAN_TRA_CUU", ...data }),
    );
  });
};

const ensureConnected = (idDatBan, maDatBan, idHoaDon, idNhanVien) => {
  if (idDatBan) requestedDatBan.add(idDatBan);
  if (maDatBan) requestedMaDatBan.add(maDatBan);
  if (idHoaDon) requestedIdHoaDon.add(idHoaDon);
  if (idNhanVien) requestedForceLogout.add(String(idNhanVien));

  const currentStaffId = localStorage.getItem("idNhanVien");
  if (currentStaffId) {
    requestedForceLogout.add(String(currentStaffId));
  }

  if (stompClient && stompClient.connected) {
    if (idDatBan) subscribeDatBanInternal(idDatBan);
    if (maDatBan) subscribeMaDatBanInternal(maDatBan);
    if (idHoaDon) subscribeIdHoaDonInternal(idHoaDon);
    if (idNhanVien) subscribeForceLogoutInternal(idNhanVien);
    if (currentStaffId) subscribeForceLogoutInternal(currentStaffId);
    return;
  }

  if (isConnecting) return;
  isConnecting = true;

  const socket = new SockJS("http://localhost:8080/ws");

  stompClient = new Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,
  });

  stompClient.onConnect = () => {
    isConnecting = false;

    subscribedDatBan.clear();
    subscribedMaDatBan.clear();
    subscribedIdHoaDon.clear();
    subscribedForceLogout.clear();

    stompClient.subscribe("/topic/datban", (message) => {
      const data = JSON.parse(message.body);
      handlers.onMessage.forEach((fn) => fn(data));
    });

    stompClient.subscribe("/topic/hoadon", (message) => {
      const data = JSON.parse(message.body);
      handlers.onMessage.forEach((fn) => fn(data));
    });

    stompClient.subscribe("/topic/banan", (message) => {
      const data = JSON.parse(message.body);
      handlers.onMessage.forEach((fn) => fn(data));
    });

    stompClient.subscribe("/topic/chat", (message) => {
      const data = JSON.parse(message.body);
      handlers.onChatMessage.forEach((fn) => fn(data));
    });

    stompClient.subscribe("/topic/danh-muc", (message) => {
      const data = JSON.parse(message.body);
      handlers.onMessage.forEach((fn) =>
        fn({ type: "DANH_MUC_UPDATE", ...data }),
      );
    });
    stompClient.subscribe("/topic/phieu-giam-gia", (message) => {
      const data = JSON.parse(message.body);
      handlers.onMessage.forEach((fn) =>
        fn({ type: "PHIEU_GIAM_GIA_UPDATE", ...data }),
      );
    });

    stompClient.subscribe("/topic/chi-tiet-hoa-don", (message) => {
      const data = JSON.parse(message.body);
      handlers.onMessage.forEach((fn) =>
        fn({ type: "CHI_TIET_HOA_DON", ...data }),
      );
    });

    stompClient.subscribe("/topic/san-pham", (message) => {
      handlers.onMessage.forEach((fn) => fn({ type: "SAN_PHAM_UPDATE" }));
    });

    stompClient.subscribe("/topic/don-vi", (message) => {
      handlers.onMessage.forEach((fn) => fn({ type: "DON_VI_UPDATE" }));
    });

    stompClient.subscribe("/topic/dot-giam-gia", (message) => {
      handlers.onMessage.forEach((fn) => fn({ type: "DOT_GIAM_GIA_UPDATE" }));
    });

    stompClient.subscribe("/topic/toast-hoa-don", (message) => {
      const data = JSON.parse(message.body);
      handlers.onMessage.forEach((fn) =>
        fn({ ...data, type: "TOAST_HOA_DON", toastType: data.type }),
      );
    });

    stompClient.subscribe("/topic/mon-da-chon", () => {
      handlers.onMessage.forEach((fn) => fn({ type: "MON_DA_CHON_UPDATE" }));
    });

    stompClient.subscribe("/topic/ban", () => {
      handlers.onMessage.forEach((fn) => fn({ type: "BAN_UPDATE" }));
    });

    stompClient.subscribe("/topic/in-pdf-goi-mon", (message) => {
      const data = JSON.parse(message.body);
      handlers.onMessage.forEach((fn) => fn({ type: "IN_PDF", ...data }));
    });
    stompClient.subscribe("/topic/giu-ban-tam", (message) => {
      const data = JSON.parse(message.body);
      handlers.onMessage.forEach((fn) => fn({ type: "GIU_BAN_TAM", ...data }));
    });

    stompClient.subscribe("/topic/thong-bao-dat-ban", (message) => {
      const data = JSON.parse(message.body);
      handlers.onMessage.forEach((fn) =>
        fn({
          type: "NHAC_XAC_NHAN",
          idDatBan: data.idDatBan,
          noiDung: data.noiDung,
          phutConLai: data.phutConLai,
        }),
      );
    });

    requestedDatBan.forEach((id) => subscribeDatBanInternal(id));
    requestedMaDatBan.forEach((ma) => subscribeMaDatBanInternal(ma));
    requestedIdHoaDon.forEach((id) => subscribeIdHoaDonInternal(id));
    requestedForceLogout.forEach((id) => subscribeForceLogoutInternal(id));

    const currentStaffId = localStorage.getItem("idNhanVien");
    if (currentStaffId) {
      subscribeForceLogoutInternal(currentStaffId);
    }
  };

  stompClient.onDisconnect = () => {
    isConnecting = false;
  };

  stompClient.activate();
};

export const connectWebSocket = (idDatBan, onMessage, onChatMessage = null) => {
  if (onMessage) handlers.onMessage.add(onMessage);
  if (onChatMessage) handlers.onChatMessage.add(onChatMessage);
  ensureConnected(idDatBan);
};

export const connectWebSocketTraCuu = (maDatBan, onMessage) => {
  if (onMessage) handlers.onMessage.add(onMessage);
  ensureConnected(null, maDatBan);
};

export const subscribeMaDatBan = (maDatBan) => {
  ensureConnected(null, maDatBan);
};

export const disconnectWebSocket = (
  onMessage = null,
  onChatMessage = null,
  onForceLogout = null,
) => {
  if (onMessage) handlers.onMessage.delete(onMessage);
  if (onChatMessage) handlers.onChatMessage.delete(onChatMessage);
  if (onForceLogout) handlers.onForceLogout.delete(onForceLogout);

  // If a staff is currently logged in, keep the socket alive for force-logout
  const currentStaffId = localStorage.getItem("idNhanVien");
  if (currentStaffId) {
    return;
  }

  if (
    handlers.onMessage.size === 0 &&
    handlers.onChatMessage.size === 0 &&
    handlers.onForceLogout.size === 0
  ) {
    if (stompClient) {
      stompClient.deactivate();
      stompClient = null;
    }
    subscribedDatBan.clear();
    subscribedMaDatBan.clear();
    subscribedIdHoaDon.clear();
    subscribedForceLogout.clear();

    requestedDatBan.clear();
    requestedMaDatBan.clear();
    requestedIdHoaDon.clear();
    requestedForceLogout.clear();
  }
};
