import axiosClient from "@/services/axiosClient";
const API_URL = "http://localhost:8080/api/admin/khach-hang";

export const getAllKhachHang = () => {
  return axiosClient.get(`${API_URL}/hien-thi`);
};

export const addKhachHang = (formData) => {
  return axiosClient.post(`${API_URL}/save`, formData);
};
export const doiTrangThaiKhachHang = (id) => {
  return axiosClient.put(`${API_URL}/${id}/doi-trang-thai`);
};
export const getKhachHangDetail = (id) => {
  return axiosClient.get(`${API_URL}/${id}`);
};
export const updateKhachHang = (id, formData) => {
  return axiosClient.post(`${API_URL}/update/${id}`, formData);
};

export const timKiemKhachHang = (keyword) => {
  return axiosClient.get(`${API_URL}/tim-kiem`, {
    params: { keyword },
  });
};

/* export theo ids */
export const exportSelectedKhachHang = (ids) =>
  axiosClient.post(`${API_URL}/export`, ids, {
    responseType: "blob",
    headers: {
      "Content-Type": "application/json",
    },
  });
/* tải file template */
export const downloadTemplateKhachHang = () =>
  axiosClient.get(`${API_URL}/template`, {
    responseType: "blob",
  });
