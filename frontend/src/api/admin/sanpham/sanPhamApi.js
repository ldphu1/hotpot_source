import axiosClient from "@/services/axiosClient";
const API_URL = "http://localhost:8080/api/admin/san-pham";

export const getSanPhamByDanhMuc = (idDanhMuc) => {
  return axiosClient.get(`${API_URL}/by-danh-muc/${idDanhMuc}`);
};

export const getAllSanPham = () => {
  return axiosClient.get(`${API_URL}/hien-thi-all`);
};

export const getAllMonLe = () => {
  return axiosClient.get(`${API_URL}/mon-le`);
};

export const addSanPham = (formData) => {
  return axiosClient.post(`${API_URL}/add-san-pham`, formData);
};

export const deTailSanPham = (idSanPham) => {
  return axiosClient.get(`${API_URL}/detail/${idSanPham}`);
};

export const updateSanPham = (idSanPham, formData) => {
  return axiosClient.put(`${API_URL}/update/${idSanPham}`, formData);
};

export const searchSanPham = (params) => {
  return axiosClient.get(`${API_URL}/search`, {
    params: params,
  });
};

export const chuyenTrangThai = (idSanPham) => {
  return axiosClient.put(`${API_URL}/chuyen-trang-thai/${idSanPham}`);
};

export const getAllSetLau = () => {
  return axiosClient.get(`${API_URL}/set-lau`);
};

export const getAllSanPhamDgg = () => {
  return axiosClient.get(`${API_URL}/hien-thi-sp-dgg`);
};

export const getAllSanPhamDotGiamGia = (laSet) => {
  return axiosClient.get(`${API_URL}/san-pham-dot-giam-gia`, {
    params: { laSet },
  });
};
export const exportSanPham = (params) => {
  return axiosClient.get(`${API_URL}/export`, {
    params: params,
    responseType: "blob",
  });
};
