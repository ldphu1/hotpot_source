import axiosClient from "@/services/axiosClient";

const API_URL = "http://localhost:8080/api/admin/giao-ca";

export const giaoCaApi = {
    layDanhSach: () => {
        return axiosClient.get(API_URL);
    },
    
    layTheoId: (id) => {
        return axiosClient.get(`${API_URL}/${id}`);
    },
    
    them: (data) => {
        return axiosClient.post(API_URL, data);
    },
    
    sua: (id, data) => {
        return axiosClient.put(`${API_URL}/${id}`, data);
    },
    
    xoa: (id) => {
        return axiosClient.delete(`${API_URL}/${id}`);
    },

    kiemTraVaoCa: () => {
        return axiosClient.get(`${API_URL}/kiem-tra-vao-ca`);
    },

    thongKeHienTai: () => {
        return axiosClient.get(`${API_URL}/thong-ke-hien-tai`);
    },

    xacNhanVaoCa: (tongTienMat) => {
        return axiosClient.post(`${API_URL}/xac-nhan-vao-ca`, { tongTienMat });
    },

    ketThucCa: (tienBanGiao, ghiChu, nhanVienNhanCaTiepTheoId) => {
        return axiosClient.post(`${API_URL}/ket-thuc-ca`, { tienBanGiao, ghiChu, nhanVienNhanCaTiepTheoId });
    },

    danhSachNhanCaTiepTheo: () => {
        return axiosClient.get(`${API_URL}/danh-sach-nhan-ca-tiep-theo`);
    },

    timKiemLichSuHoatDong: (params) => {
        return axiosClient.get(`${API_URL}/tim-kiem`, { params });
    }
};