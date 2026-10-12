import axiosClient from "@/services/axiosClient";
const API_URL = "http://localhost:8080/api/dot-giam-gia";

/* ================== GET ALL ================== */
export const getAllDotGiamGia = () => {
  return axiosClient.get(`${API_URL}/hien-thi`);
};

/* ================== FILTER ================== */
export const filterDotGiamGia = (params) => {
  return axiosClient.get(`${API_URL}/filter`, { params });
};

/* ================== DETAIL ================== */
export const getDetailDotGiamGia = (id) => {
  return axiosClient.get(`${API_URL}/detail/${id}`);
};

/* ================== CREATE ================== */
export const createDotGiamGia = (data) => {
  return axiosClient.post(`${API_URL}/add`, data);
};

/* ================== UPDATE ================== */
export const updateDotGiamGia = (id, data) => {
  return axiosClient.put(`${API_URL}/update/${id}`, data);
};

/* ================== DELETE ================== */
export const deleteDotGiamGia = (id) => {
  return axiosClient.delete(`${API_URL}/delete/${id}`);
};

/* ================== TOGGLE TRẠNG THÁI ================== */
export const toggleTrangThaiDotGiamGia = (id) => {
  return axiosClient.put(`${API_URL}/doi-trang-thai/${id}`);
};

/* ================== GET ACTIVE BY SẢN PHẨM ================== */
export const getDotGiamGiaBySanPham = (sanPhamId) => {
  return axiosClient.get(`${API_URL}/san-pham/${sanPhamId}`);
};
