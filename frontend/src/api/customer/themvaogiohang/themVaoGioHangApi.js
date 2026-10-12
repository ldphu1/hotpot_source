import axiosClient from "@/services/axiosClient";
const API_URL = "http://localhost:8080/api/customer/gio-hang";

export const themVaoGioHang = (data) => {
  return axiosClient.post(`${API_URL}/them-vao-gio`, data, {
    withCredentials: true,
  });
};

export const getGioHang = () => {
  return axiosClient.get(`${API_URL}/hien-tai`, {
    withCredentials: true,
  });
};

export const xoaMonGioHang = (idChiTiet) => {
  return axiosClient.delete(`${API_URL}/xoa-mon/${idChiTiet}`, {
    withCredentials: true,
  });
};
