import axiosClient from "@/services/axiosClient";
const API_URL = "http://localhost:8080/api/admin/v1/ban-ao";

export const chuyenSangBanAo = (payload, anhFile) => {
  const formData = new FormData();
  formData.append(
    "data",
    new Blob([JSON.stringify(payload)], { type: "application/json" }),
  );
  if (anhFile) {
    formData.append("anh", anhFile);
  }
  return axiosClient.post(`${API_URL}/chuyen-sang-ban-ao`, formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

export const getAllBanAo = () => {
  return axiosClient.get(`${API_URL}/hien-thi`);
};

export const taoThanhToanVnpayBanAo = (idBanAo) => {
  return axiosClient.post(`${API_URL}/thanh-toan-vnpay/${idBanAo}`);
};

export const xacNhanThanhToanThuCong = (idBanAo) => {
  return axiosClient.post(`${API_URL}/xac-nhan-thanh-toan/${idBanAo}`);
};

export const getDetailHoaDonTreo = (idBanAo) => {
  return axiosClient.get(`${API_URL}/chi-tiet/${idBanAo}`);
};

export const danhDauNoXauBanAo = (idBanAo, ghiChu) => {
  return axiosClient.post(`${API_URL}/no-xau/${idBanAo}`, null, {
    params: { ghiChu },
  });
};
