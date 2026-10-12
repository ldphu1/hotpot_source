import axiosClient from "@/services/axiosClient";

const API_URL = "http://localhost:8080/api/admin/v1/khach-cho";

export const getAllKhachChoBan = () => {
  return axiosClient.get(`${API_URL}/hien-thi`);
};
export const dangKyChoBan = (data) => {
  return axiosClient.post(`${API_URL}/dang-ky`, data);
};

export const huyKhachChoBan = (id) => {
  return axiosClient.put(`${API_URL}/${id}/huy`);
};

export const xepBanKhachCho = (id) => {
  return axiosClient.put(`${API_URL}/${id}/xep-ban`);
};
