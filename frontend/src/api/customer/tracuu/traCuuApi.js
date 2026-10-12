import axiosClient from "@/services/axiosClient";
const API_URL = "http://localhost:8080/api/customer/public";

export const traCuuDatBan = (maDatBan) => {
  return axiosClient.get(`${API_URL}/tra-cuu/${maDatBan}`);
};
