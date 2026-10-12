import axiosClient from "@/services/axiosClient";

const API_URL = "http://localhost:8080/api/admin/tham-so-he-thong";

/* ================= GET ================= */

export const getAllThamSoDong = () => {
  return axiosClient.get(`${API_URL}/hien-thi`);
};

export const getDetailThamSo = (id) => {
  return axiosClient.get(`${API_URL}/detail/${id}`);
};

export const getThamSo = (code) => {
  return axiosClient.get(`${API_URL}/get/${code}`);
};

export const getThamSoInt = (code) => {
  return axiosClient.get(`${API_URL}/get-int/${code}`);
};

export const getThamSoDouble = (code) => {
  return axiosClient.get(`${API_URL}/get-double/${code}`);
};

/* ================= CREATE ================= */

export const createThamSo = (data) => {
  return axiosClient.post(`${API_URL}/add`, data);
};

/* ================= UPDATE ================= */

export const updateThamSo = (id, data) => {
  return axiosClient.put(`${API_URL}/update/${id}`, data);
};

/* ================= DELETE ================= */

export const deleteThamSo = (id) => {
  return axiosClient.delete(`${API_URL}/delete/${id}`);
};
