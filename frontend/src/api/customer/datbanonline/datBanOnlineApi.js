import axiosClient from "@/services/axiosClient";
const API_URL = "http://localhost:8080/api/customer/public";

export const datBanOnline = (formData) => {
  return axiosClient.post(`${API_URL}/dat-ban-online`, formData);
};

export const getAllBan = () => {
  return axiosClient.get(`${API_URL}/ban/hien-thi`);
};

export const getAllThamSoCustomer = () => {
  return axiosClient.get(`${API_URL}/tham-so/hien-thi`);
};

export const getAllDatBanCustomer = () => {
  return axiosClient.get(`${API_URL}/dat-ban/hien-thi`);
};

export const kiemTraBanTrong = (params) => {
  return axiosClient.get(`${API_URL}/kiem-tra-ban-trong`, { params });
};
