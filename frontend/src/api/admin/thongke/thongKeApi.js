import axiosClient from "@/services/axiosClient";
const API_URL = "http://localhost:8080/api/admin/thong-ke";

export const getThongKeDoanhThu = () => {
  return axiosClient.get(`${API_URL}/doanh-thu`);
};

export const getDoanhThuLoc = (type, from, to) => {
  return axiosClient.get(`${API_URL}/tong-quan`, {
    params: { type, from, to },
  });
};

export const getDoanhThuThang = (type, from, to) => {
  return axiosClient.get(`${API_URL}/doanh-thu-thang`, {
    params: { type, from, to },
  });
};

export const getTopSetBanChay = (type, from, to) => {
  return axiosClient.get(`${API_URL}/top-set`, {
    params: { type, from, to },
  });
};

export const getBookingChannel = (type, from, to) => {
  return axiosClient.get(`${API_URL}/booking-channel`, {
    params: { type, from, to },
  });
};

export const getTrangThaiHoaDon = (type, from, to) => {
  return axiosClient.get(`${API_URL}/trang-thai-hoa-don`, {
    params: { type, from, to },
  });
};

export const exportThongKeExcel = () => {
  return axiosClient.get(`${API_URL}/export-excel`, {
    responseType: "blob",
  });
};

export const getThongKeTheoNgay = (type, from, to) => {
  return axiosClient.get(`${API_URL}/theo-ngay`, {
    params: { type, from, to },
  });
};

export const getHoaDonTheoNgay = (ngay) => {
  return axiosClient.get(`${API_URL}/theo-ngay/chi-tiet`, {
    params: { ngay },
  });
};
