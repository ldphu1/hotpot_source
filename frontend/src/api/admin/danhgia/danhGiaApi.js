import axiosClient from "@/services/axiosClient";
const API_URL = "http://localhost:8080/api/admin/v1/danh-gia";

export const addDanhGia = (data) => {
  return axiosClient.post(API_URL, data);
};

export const getAllDanhGia = () => {
  return axiosClient.get(`${API_URL}/hien-thi`);
};

export const getDetailDanhGia = (idDanhGia) => {
  return axiosClient.get(`${API_URL}/${idDanhGia}`);
};
