import axiosClient from "@/services/axiosClient";

const API_URL = "http://localhost:8080/api/admin/quan-ly-ban";

export const getAllquanlyban = (ngay) => {
  return axiosClient.get(`${API_URL}/hien-thi`, {
    params: { ngay },
  });
};

export const getBanPhanTrang = (keyword, trangThai, tang, page, size) => {
  return axiosClient.get(`${API_URL}/phan-trang`, {
    params: { keyword, trangThai, tang, page, size },
  });
};

export const themBanAn = (ban) => {
  return axiosClient.post(`${API_URL}/themBanAn`, ban);
};

export const importExcelBanAn = (formData) => {
  return axiosClient.post(`${API_URL}/import-excel`, formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
};

export const downloadTemplateExcel = () => {
  return axiosClient.get(`${API_URL}/import/template`, {
    responseType: "blob",
  });
};

export const exportBanAn = (keyword, trangThai, tang) => {
  return axiosClient.get(`${API_URL}/export`, {
    params: { keyword, trangThai, tang },
    responseType: "blob",
  });
};

export const getBanChiTiet = (id) => {
  return axiosClient.get(`${API_URL}/chi-tiet/${id}`);
};

export const xoaBan = (id) => {
  return axiosClient.get(`${API_URL}/xoa/${id}`);
};

export const filterBanAn = (params, thoiGianDat) => {
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
  return axiosClient.get(`${API_URL}/filter`, {
    params: {
      ...params,
      thoiGianDat: formatted,
    },
  });
};

export const doiTrangThaiBan = (id, trangThai) => {
  return axiosClient.put(`${API_URL}/doi-trang-thai/${id}`, {
    trangThai,
  });
};

export const capNhatViTriBan = (payload) => {
  return axiosClient.put(`${API_URL}/cap-nhat-vi-tri`, payload);
};

export const timKiem = (keyword) => {
  return axiosClient.get(`${API_URL}/tim-kiem`, {
    params: {
      keyword,
    },
  });
};
