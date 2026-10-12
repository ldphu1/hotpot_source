import axiosClient from "@/services/axiosClient";

export const registerCustomer = (data) => {
  return axiosClient.post("/api/customer/auth/register", data, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
};
