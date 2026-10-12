import axiosClient from "@/services/axiosClient";

const API_URL = "http://localhost:8080/api/customer/public";

export const getAllThucDon = () => {
  return axiosClient.get(`${API_URL}/xem-thuc-don`);
};

export const getAllLoaiLau = () => {
  return axiosClient.get(`${API_URL}/loai-lau`);
};

export const getAllSetLau = () => {
  return axiosClient.get(`${API_URL}/set-lau`);
};

export const searchMonAn = (params) => {
  return axiosClient.get(`${API_URL}/mon-an/search`, {
    params: params,
  });
};

export const filterSetLau = (idNuocLau) => {
  return axiosClient.get(`${API_URL}/set-lau-filter`, {
    params: {
      idNuocLau: idNuocLau,
    },
  });
};

export const getAllSanPhamDotGiamGia = (laSet) => {
  return axiosClient.get(`${API_URL}/san-pham`, {
    params: { laSet },
  });
};

export const getAllSetLauDotGiamGia = () => {
  return axiosClient.get(`${API_URL}/set-lau-dgg`);
};

export const getAllLoaiMonAn = () => {
  return axiosClient.get(`${API_URL}/loai-mon-an`);
};

export const getAllMonTheoDanhMucDotGiamGia = (idDanhMuc) => {
  return axiosClient.get(`${API_URL}/thuc-don/client`, {
    params: { idDanhMuc },
  });
};
