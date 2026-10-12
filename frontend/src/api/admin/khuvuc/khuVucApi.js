import axiosClient from "@/services/axiosClient";

const API_URL = "http://localhost:8080/api/admin/khu-vuc";

export const getAllkhuvuc = () => {
  return axiosClient.get(`${API_URL}/hien-thi`);
};

export const themKhuVuc = (tang) => {
  return axiosClient.post(`${API_URL}/themKhuVuc`, tang);
};

export const xoaKhuVuc = (id) => {
  return axiosClient.get(`${API_URL}/xoa/${id}`);
};

export const getKhuVucChiTiet = (id) => {
  return axiosClient.get(`${API_URL}/chi-tiet/${id}`);
};
