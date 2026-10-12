import axiosClient from "@/services/axiosClient";

const API_URL = "http://localhost:8080/api/admin/nuoc-lau";

export const getAllNuocLau = () => {
  return axiosClient.get(`${API_URL}/hien-thi`);
};

export const addNuocLau = (formData) => {
  return axiosClient.post(`${API_URL}/add`, formData);
};

export const detailNuocLau = (idNuocLau) => {
  return axiosClient.get(`${API_URL}/detail/${idNuocLau}`);
};

export const updateNuocLau = (id, formData) => {
  return axiosClient.put(`${API_URL}/update/${id}`, formData);
};

export const searchNuocLau = (params) => {
  return axiosClient.get(`${API_URL}/search`, {
    params: params,
  });
};

export const chuyenTrangThai = (idNuocLau) => {
  return axiosClient.put(`${API_URL}/chuyen-trang-thai/${idNuocLau}`);
};
