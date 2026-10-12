import axiosClient from "@/services/axiosClient";

const API_URL = "http://localhost:8080/api/admin/khuyen-mai";

export const getAllKhuyenMai = (params) => {
  return axiosClient.get(`${API_URL}/search`, { params });
};

export const addPhieuGiamGia = (formData) => {
  return axiosClient.post(`${API_URL}/save`, formData);
};
export const updatePhieuGiamGia = (id, data) => {
  return axiosClient.put(`${API_URL}/update/${id}`, data);
};

export const deleteTrangThai = (id) => {
  return axiosClient.put(`${API_URL}/delete/${id}`);
};

export const restorePhieuGiamGia = (id) => {
  return axiosClient.put(`${API_URL}/restore/${id}`);
};

export const filterPhieuGiamGia = (params) => {
  return axiosClient.get(`${API_URL}/search`, { params });
};
export const getDetailPhieuGiamGia = (id) => {
  return axiosClient.get(`${API_URL}/detail/${id}`);
};

export const exportPhieuGiamGia = (params) => {
  return axiosClient.get(`${API_URL}/export`, {
    params: params,
    responseType: "blob",
  });
};
