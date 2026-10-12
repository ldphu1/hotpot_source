import axiosClient from "@/services/axiosClient";

const API_URL = "http://localhost:8080/ap/customer/public";

export const getAllThamSoCustomer = () => {
  return axiosClient.get(`${API_URL}/hien-thi`);
};
