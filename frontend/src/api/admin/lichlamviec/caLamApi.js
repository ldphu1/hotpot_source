import axiosClient from "@/services/axiosClient";

const API_URL = "http://localhost:8080/api/admin/ca-lam";

export const caLamApi = {
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
    }
};