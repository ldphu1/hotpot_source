import axiosClient from "@/services/axiosClient";
const API_URL = "http://localhost:8080/api/admin/dinh-luong";

export const getAllDinhLuong = () => {
  return axiosClient.get(`${API_URL}/hien-thi`);
};

export const getDinhLuongByDanhMuc = (idDanhMuc) => {
  return axiosClient.get(`${API_URL}/by-danh-muc/${idDanhMuc}`);
};
