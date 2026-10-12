import axiosClient from "@/services/axiosClient";
const API_URL = "http://localhost:8080/api/admin/don-vi";

export const getAllDonVi = () => {
  return axiosClient.get(`${API_URL}/hien-thi`);
};

export const addDonViTongHop = (formData) => {
  return axiosClient.post(`${API_URL}/add`, formData);
};

export const detaiDonVi = (idDonVi) => {
  return axiosClient.get(`${API_URL}/detail/${idDonVi}`);
};

export const updateDonVi = (idDonVi, formData) => {
  return axiosClient.put(`${API_URL}/update/${idDonVi}`, formData);
};

export const searchDonVi = (params) => {
  return axiosClient.get(`${API_URL}/search`, {
    params: params,
  });
};

export const chuyenTrangThai = (idDonVi) => {
  return axiosClient.put(`${API_URL}/chuyen-trang-thai/${idDonVi}`);
};
