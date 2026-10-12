import axiosClient from "@/services/axiosClient";

const API_URL = "http://localhost:8080/api/admin/dat-ban";

export const getAlldatban = () => {
  return axiosClient.get(`${API_URL}/hien-thi`);
};

export const getAllBanAn = () => {
  return axiosClient.get(`${API_URL}/chon-ban/hien-thi`);
};

export const getAllBanChoDat = (thoiGianDat) => {
  const d = new Date(thoiGianDat);

  const formatted =
    d.getFullYear() +
    "-" +
    String(d.getMonth() + 1).padStart(2, "0") +
    "-" +
    String(d.getDate()).padStart(2, "0") +
    "T" +
    String(d.getHours()).padStart(2, "0") +
    ":" +
    String(d.getMinutes()).padStart(2, "0") +
    ":" +
    String(d.getSeconds()).padStart(2, "0");

  return axiosClient.get(`${API_URL}/ban-an/cho-dat`, {
    params: {
      thoiGianDat: formatted,
    },
  });
};

export const xepBanChoKhach = (formData) => {
  return axiosClient.post(`${API_URL}/xep-ban`, formData);
};

export const xepLaiBan = (formData) => {
  return axiosClient.post(`${API_URL}/xep-lai-ban`, formData);
};

export const datBanDetail = (idDatBan) => {
  return axiosClient.get(`${API_URL}/detail/${idDatBan}`);
};

export const xacNhanDatBan = (idDatBan) => {
  return axiosClient.put(`${API_URL}/xac-nhan/${idDatBan}`);
};

export const xacNhanDatCocVaXacNhan = (idDatBan, data) => {
  return axiosClient.put(`${API_URL}/xac-nhan-coc/${idDatBan}`, data);
};

export const searchDatBan = (params) => {
  return axiosClient.get(`${API_URL}/search`, {
    params: params,
  });
};

export const getAlldatbanByNgay1 = (params) => {
  return axiosClient.get(`${API_URL}/hien-thi-theo-ngay-1`, {
    params,
  });
};

export const huyDatBan = (idDatBan, data) => {
  return axiosClient.put(`${API_URL}/huy-dat-ban/${idDatBan}`, data);
};
export const tuChoiXepBan = (idDatBan, lyDo) => {
  return axiosClient.put(`${API_URL}/tu-choi-xep-ban/${idDatBan}`, null, {
    params: { lyDo },
  });
};

export const previewTienCoc = (idDatBan) => {
  return axiosClient.get(`${API_URL}/preview-huy-dat-ban/${idDatBan}`);
};

export const xacNhanHoanCoc = (idDatBan) => {
  return axiosClient.patch(`${API_URL}/xac-nhan-hoan-coc/${idDatBan}`);
};

export const exportDatBan = (params) => {
  return axiosClient.get(`${API_URL}/export`, {
    params: params,
    responseType: "blob",
  });
};

export const thongTinDatBan = (data) => {
  return axiosClient.post(`${API_URL}/thong-tin-dat-ban`, data);
};

export const xepBanDatTaiQuay = (idDatBan, data) => {
  return axiosClient.put(`${API_URL}/xep-ban-tai-quay/${idDatBan}`, data);
};

export const xacNhanTaiQuay = (idDatBan, data) => {
  return axiosClient.put(`${API_URL}/xac-nhan-tai-quay/${idDatBan}`, data);
};

export const huyDatBanDangTao = (idDatBan) => {
  return axiosClient.delete(`${API_URL}/huy-dat-ban-dang-tao/${idDatBan}`);
};

export const boBan = (idDatBan, idBan) => {
  return axiosClient.put(`${API_URL}/bo-ban/${idDatBan}/${idBan}`);
};

export const updatePhieuDatBan = (idDatBan, data) => {
  return axiosClient.put(`${API_URL}/cap-nhat/${idDatBan}`, data);
};
