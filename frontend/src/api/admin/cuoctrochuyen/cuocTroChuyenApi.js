import axiosClient from "@/services/axiosClient";

const API_URL = "http://localhost:8080/api/admin/v1/cuoc-tro-chuyen";

export const getCuocTroChuyen = (params) => {
  return axiosClient.get(API_URL, { params });
};

export const getTinNhan = (idCuocTroChuyen, cursor = null) => {
  return axiosClient.get(`${API_URL}/detail/${idCuocTroChuyen}`, {
    params: { cursor },
  });
};

export const replyTinNhan = (
  idCuocTroChuyen,
  noiDung,
  loaiTinNhan = "TEXT",
) => {
  return axiosClient.post(`${API_URL}/reply`, {
    idCuocTroChuyen,
    noiDung,
    loaiTinNhan,
  });
};

export const nhanXuLy = (idCuocTroChuyen) => {
  return axiosClient.put(`${API_URL}/${idCuocTroChuyen}/nhan-xu-ly`);
};

export const ketThucXuLy = (idCuocTroChuyen) => {
  return axiosClient.put(`${API_URL}/${idCuocTroChuyen}/ket-thuc-xu-ly`);
};
