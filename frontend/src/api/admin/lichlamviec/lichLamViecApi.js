import axiosClient from "@/services/axiosClient";

const API_URL = "http://localhost:8080/api/admin/lich-lam-viec";

export const lichLamViecApi = {
    layDanhSach: (params) => {
        return axiosClient.get(API_URL, { params });
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

    layDanhSachNhanVien: (params) => {
        return axiosClient.get(`${API_URL}/nhan-vien`, { params });
    },

    importExcel: (file) => {
        const formData = new FormData();
        formData.append("file", file);
        return axiosClient.post(`${API_URL}/import`, formData, {
            headers: {
                "Content-Type": "multipart/form-data"
            }
        });
    },

    exportExcel: (params) => {
        return axiosClient.get(`${API_URL}/export`, {
            params,
            responseType: 'blob'
        });
    },

    downloadTemplate: () => {
        return axiosClient.get(`${API_URL}/template`, {
            responseType: 'blob'
        });
    }
};