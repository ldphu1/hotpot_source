import axiosClient from "@/services/axiosClient";

const API_URL = "http://localhost:8080/api/admin/thanh-toan";

// lấy toàn bộ phương thức thanh toán
export const getAllPhuongThucThanhToan = () => {
  return axiosClient.get(`${API_URL}/phuong-thuc`);
};

// thanh toán tiền mặt
export const thanhToanTienMat = (formData) => {
  return axiosClient.post(`${API_URL}/tien-mat`, formData);
};

// thanh toán chuyển khoản ngân hàng
export const thanhToanChuyenKhoan = (formData) => {
  return axiosClient.post(`${API_URL}/chuyen-khoan`, formData);
};

export const getQrChuyenKhoan = (idHoaDon, amount) => {
  return axiosClient.get(`${API_URL}/qr-chuyen-khoan`, {
    params: { idHoaDon, amount },
  });
};

// BƯỚC 1: tạo link/QR thanh toán online (VNPay/ZaloPay) - chỉ hiển thị, chưa trừ voucher/điểm
export const taoLinkThanhToanOnline = (formData) => {
  return axiosClient.post(`${API_URL}/tao-link-online`, formData);
};

// BƯỚC 2: xác nhận đã thanh toán online (VNPay/ZaloPay) - lúc này mới trừ voucher/điểm thật
export const xacNhanThanhToanOnline = (formData) => {
  return axiosClient.post(`${API_URL}/xac-nhan-online`, formData);
};

// lấy danh sách thanh toán theo hóa đơn
export const getThanhToanByHoaDon = (idHoaDon) => {
  return axiosClient.get(`${API_URL}/hoa-don/${idHoaDon}`);
};

// lấy chi tiết hóa đơn
export const getChiTietHoaDonThanhToan = (idHoaDon) => {
  return axiosClient.get(`${API_URL}/chi-tiet-hoa-don/${idHoaDon}`);
};

export const xacNhanHoanTatHoaDon = (idHoaDon) => {
  return axiosClient.post(`${API_URL}/xac-nhan-hoan-tat/${idHoaDon}`);
};

// in hóa đơn
export const inHoaDonThanhToan = (idHoaDon) => {
  return axiosClient.get(`${API_URL}/in-hoa-don/${idHoaDon}`, {
    responseType: "blob",
  });
};
