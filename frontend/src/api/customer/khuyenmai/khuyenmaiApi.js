import axiosClient from "@/services/axiosClient";
const API_URL = "http://localhost:8080/api/admin/v1/public/khuyen-mai";

export const getAllKhuyenMaiPublic = () => {
  return axiosClient.get(`${API_URL}/hien-thi`);
};
