import axiosClient from "@/services/axiosClient";

const API_URL = "http://localhost:8080/api/admin/hoa-don";

export const getAllhoadon = () => {
  return axiosClient.get(`${API_URL}/hien-thi`);
};

export const detailHoaDon = (idHoaDon) => {
  return axiosClient.get(`${API_URL}/detail/${idHoaDon}`);
};

export const xacNhanHoaDon = (idHoaDon) => {
  return axiosClient.put(`${API_URL}/xac-nhan/${idHoaDon}`);
};

export const huyHoaDon = (idHoaDon, data) => {
  return axiosClient.put(`${API_URL}/huy/${idHoaDon}`, data);
};

export const previewTienCoc = (idHoaDon) => {
  return axiosClient.get(`${API_URL}/preview-huy/${idHoaDon}`);
};

export const xacNhanHoanCoc = (idHoaDon) => {
  return axiosClient.patch(`${API_URL}/xac-nhan-hoan-coc/${idHoaDon}`);
};

export const hoanThanhHoaDon = (idHoaDon) => {
  return axiosClient.put(`${API_URL}/hoan-thanh/${idHoaDon}`);
};

export const search = (params) => {
  return axiosClient.get(`${API_URL}/search`, {
    params: params,
  });
};

export const exportHoaDonPdf = (id) => {
  return axiosClient.get(`${API_URL}/export-pdf/${id}`, {
    responseType: "blob",
  });
};
export const exportHoaDon = (params) => {
  return axiosClient.get(`${API_URL}/export`, {
    params: params,
    responseType: "blob",
  });
};

export const scanQrHoaDon = (qrData) => {
  return axiosClient.get(`${API_URL}/qr-scan`, {
    params: { data: qrData },
  });
};
