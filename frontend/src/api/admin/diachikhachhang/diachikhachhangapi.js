import axiosClient from "@/services/axiosClient";
const API_URL = "http://localhost:8080/api/admin/dia-chi-khach-hang";

// lấy danh sách địa chỉ theo khách hàng
export const getDiaChiByKhachHang = (khId) => {
  return axiosClient.get(`${API_URL}/${khId}`);
};

// thêm địa chỉ cho khách hàng
export const addDiaChi = (khId, data) => {
  return axiosClient.post(`${API_URL}/${khId}`, data);
};

// đặt địa chỉ mặc định
export const setMacDinh = (diaChiId) => {
  return axiosClient.put(`${API_URL}/mac-dinh/${diaChiId}`);
};
// cập nhật địa chỉ
export const updateDiaChi = (diaChiId, data) => {
  return axiosClient.put(`${API_URL}/${diaChiId}`, data);
};
