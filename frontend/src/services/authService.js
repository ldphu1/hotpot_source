import axiosClient from "@/services/axiosClient";

export const loginAdmin = (data) => {
  return axiosClient.post("/api/auth/login", data);
};
export const loginCustomer = (data) => {
  return axiosClient.post("/api/customer/auth/login", data);
};
export const forgotPassword = (data) => {
  return axiosClient.post("/api/customer/auth/forgot-password", data);
};
export const verifyOtp = (data) => {
  return axiosClient.post("/api/customer/auth/verify-otp", data);
};
export const resetPassword = (data) => {
  return axiosClient.post("/api/customer/auth/reset-password", data);
};

// ===== ADMIN refresh / logout =====
export const refreshAdminToken = (refreshToken) => {
  return axiosClient.post("/api/auth/refresh", { refreshToken });
};
export const logoutAdmin = (refreshToken) => {
  return axiosClient.post("/api/auth/logout", { refreshToken });
};

// ===== CUSTOMER refresh / logout =====
export const refreshCustomerToken = (refreshToken) => {
  return axiosClient.post("/api/customer/auth/refresh", { refreshToken });
};
export const logoutCustomer = (refreshToken) => {
  return axiosClient.post("/api/customer/auth/logout", { refreshToken });
};
