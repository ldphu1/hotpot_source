import axiosClient from "@/services/axiosClient";

const API_URL = "http://localhost:8080/api/chat";

export const chatBotAI = (message, sessionId) => {
  return axiosClient.post(`${API_URL}/gui`, {
    noiDung: message,
    loaiTinNhan: "TEXT",
    sessionId: sessionId,
  });
};

export const getTinNhanKhach = (idCuocTroChuyen) => {
  return axiosClient.get(`${API_URL}/tin-nhan/${idCuocTroChuyen}`);
};
export const getCuocTroChuyenHienTai = () => {
  return axiosClient.get(`${API_URL}/hien-tai`);
};
