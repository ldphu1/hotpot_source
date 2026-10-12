import axiosClient from "@/services/axiosClient";
const API_URL = "http://localhost:8080/api/admin/danh-muc";

export const getAllDanhMuc = () => {
  return axiosClient.get(`${API_URL}/hien-thi`);
};

export const getAllDanhMucMonLe = () => {
  return axiosClient.get(`${API_URL}/mon-le`);
};

export const addDanhMuc = (data) => {
  return axiosClient.post(`${API_URL}/add`, data);
};

export const getDeTailDanhMuc = (id) => {
  return axiosClient.get(`${API_URL}/detail/${id}`);
};

export const updateDanhMuc = (data) => {
  return axiosClient.put(`${API_URL}/update`, data);
};

export const deleteTrangThai = (id) => {
  return axiosClient.put(`${API_URL}/delete/${id}`);
};

export const restoreDanhMuc = (id) => {
  return axiosClient.put(`${API_URL}/restore/${id}`);
};

export const filterDanhMuc = (params) => {
  return axiosClient.get(`${API_URL}/search`, { params });
};
