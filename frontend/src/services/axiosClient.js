import axios from "axios";
import { useUtils } from "@/utils/helper";
const { notify, nprogress } = useUtils();

const axiosClient = axios.create({
  baseURL: "http://localhost:8080",
});

axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  console.log("===== REQUEST =====");
  console.log("URL:", config.url);
  console.log("METHOD:", config.method);
  console.log("TOKEN:", token);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let isRefreshing = false;
let refreshQueue = [];

function logoutAndRedirect() {
  localStorage.removeItem("token");
  localStorage.removeItem("refreshToken");
  localStorage.removeItem("loginTime");
  notify.error("Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại");
  setTimeout(() => {
    window.location.href = "/admin/login";
  }, 1500);
}

axiosClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (
      error.response &&
      error.response.status === 401 &&
      !originalRequest._retry
    ) {
      if (originalRequest.url.includes("/login")) {
        return Promise.reject(error);
      }

      const refreshToken = localStorage.getItem("refreshToken");

      if (!refreshToken) {
        logoutAndRedirect();
        return Promise.reject(error);
      }

      if (isRefreshing) {
        // Đợi lần refresh đang chạy xong rồi mới retry
        return new Promise((resolve, reject) => {
          refreshQueue.push({ resolve, reject, config: originalRequest });
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const role = localStorage.getItem("role");
        const refreshUrl =
          role === "CUSTOMER"
            ? "/api/customer/auth/refresh"
            : "/api/auth/refresh";

        const res = await axios.post(
          `${axiosClient.defaults.baseURL}${refreshUrl}`,
          {
            refreshToken,
          },
        );

        const newToken = res.data.token;
        const newRefreshToken = res.data.refreshToken;

        localStorage.setItem("token", newToken);
        localStorage.setItem("refreshToken", newRefreshToken);

        refreshQueue.forEach(({ resolve, config }) => {
          config.headers.Authorization = `Bearer ${newToken}`;
          resolve(axiosClient(config));
        });
        refreshQueue = [];

        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return axiosClient(originalRequest);
      } catch (refreshError) {
        refreshQueue.forEach(({ reject }) => reject(refreshError));
        refreshQueue = [];
        logoutAndRedirect();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    if (error.response && error.response.status === 403) {
      window.location.href = "/403";
    }

    if (error.response && error.response.status === 404) {
      window.location.href = "/404";
    }

    return Promise.reject(error);
  },
);

export default axiosClient;
