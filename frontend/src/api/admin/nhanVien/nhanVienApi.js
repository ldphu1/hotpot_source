import axiosClient from "@/services/axiosClient";

const API_URL = "http://localhost:8080/api/admin/nhan-vien";

export const getAllNhanVien = () => {
  return axiosClient.get(`${API_URL}/hien-thi`);
};

export const getAllGioiTinh = async () => {
  const res = await axiosClient.get(`${API_URL}/gioi-tinh`);
  return res.data;
};

export const getAllChucVu = async () => {
  const res = await axiosClient.get(`${API_URL}/chuc-vu`);
  return res.data;
};

export const addNhanVien = (formData) => {
  return axiosClient.post(`${API_URL}/save`, formData);
};

export const profile = () => {
  return axiosClient.get(`${API_URL}/me`);
};
// lay idd này
export const getNhanVienById = (id) => {
  return axiosClient.get(`${API_URL}/detail/${id}`);
};
export const doiMatKhau = (data) => {
  return axiosClient.put(`${API_URL}/doi-mat-khau`, data);
};
//sua
export const updateNhanVien = (id, formData) => {
  return axiosClient.put(`${API_URL}/update/${id}`, formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
};
//xoa mêm
export const doiTrangThaiNhanVien = (id) => {
  return axiosClient.put(`${API_URL}/doi-trang-thai/${id}`);
};

// SEARCH
export const searchNhanVien = (params) => {
  return axiosClient.get(`${API_URL}/search`, { params });
};

// import excel
export const importNhanVienExcel = (file) => {
  const form = new FormData();
  form.append("file", file);

  return axiosClient.post(`${API_URL}/import`, form);
};

//file mẫu import
export const downloadNhanVienTemplate = () =>
  axiosClient.get(`${API_URL}/import/template`, {
    responseType: "blob",
  });

// export excel
export const exportNhanVienExcel = (ids) =>
  axiosClient.post(`${API_URL}/export`, ids, {
    responseType: "blob",
  });

export const forgotPasswordNhanVien = (data) => {
  return axiosClient.post(`${API_URL}/forgot-password`, data);
};
export const verifyOtpNhanVien = (data) => {
  return axiosClient.post(`${API_URL}/verify-otp`, data);
};
export const resetPasswordNhanVien = (data) => {
  return axiosClient.post(`${API_URL}/reset-password`, data);
};
