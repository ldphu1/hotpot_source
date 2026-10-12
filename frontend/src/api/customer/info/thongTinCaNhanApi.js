import axiosClient from "@/services/axiosClient";

const API_URL = "http://localhost:8080/api/customer/private";

export const getThongTin = () => {
  return axiosClient.get(`${API_URL}/my-info`);
};

export const updateThongTin = (data) => {
  return axiosClient.put(`${API_URL}/update-info`, data);
};

export const lichSuDonHang = () => {
  return axiosClient.get(`${API_URL}/lich-su-hoa-don`);
};

export const timKiemDonHang = (params) => {
  return axiosClient.get(`${API_URL}/search`, {
    params: params,
  });
};

export const lichSuDetail = (idHoaDon) => {
  return axiosClient.get(`${API_URL}/lich-su/detail/${idHoaDon}`);
};

export const previewHuyDatBan = (idDatBan) => {
  return axiosClient.get(`${API_URL}/dat-ban/preview-huy/${idDatBan}`);
};

export const huyDatBanCustomer = (idDatBan, data) => {
  return axiosClient.put(`${API_URL}/dat-ban/huy/${idDatBan}`, data);
};

export const doiMatKhau = (data) => {
  return axiosClient.post(`${API_URL}/doi-mat-khau`, data);
};

export const layLinkVnpayCoc = (idHoaDon) => {
  return axiosClient.get(`${API_URL}/hoa-don/vnpay-coc/${idHoaDon}`);
};
