import axiosClient from "@/services/axiosClient";
const API_URL = "http://localhost:8080/api/admin/set-lau";

export const getAllSetLau = () => {
  return axiosClient.get(`${API_URL}/hien-thi`);
};

export const addSetLau = (formData) => {
  const fullUrl = `${API_URL}/save`;
  return axiosClient.post(fullUrl, formData);
};

export const detailSetLau = (idSetLau) => {
  return axiosClient.get(`${API_URL}/detail/${idSetLau}`);
};

export const updateSetLau = (idSetLau, formData) => {
  return axiosClient.put(`${API_URL}/update/${idSetLau}`, formData);
};

export const searchSetLau = (params) => {
  return axiosClient.get(`${API_URL}/search`, {
    params: params,
  });
};

export const chuyenTrangThai = (idSetLau) => {
  return axiosClient.put(`${API_URL}/chuyen-trang-thai/${idSetLau}`);
};

export const exportSetLau = (params) => {
  return axiosClient.get(`${API_URL}/export-set-lau`, {
    params: params,
    responseType: "blob",
  });
};
