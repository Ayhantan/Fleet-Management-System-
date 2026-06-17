import axios from "axios";
import { clearStoredAuth, getStoredAuth } from "../utils/authStorage.js";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
});

api.interceptors.request.use((config) => {
  const auth = getStoredAuth();

  if (auth?.token) {
    config.headers.Authorization = `Bearer ${auth.token}`;
  }

  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const isLoginRequest = error.config?.url?.includes("/api/auth/login");
    const backendMessage = error.response?.data?.message;
    const validationErrors = error.response?.data?.validationErrors;

    if (validationErrors && typeof validationErrors === "object") {
      error.userMessage = Object.values(validationErrors).join(" ");
    } else {
      error.userMessage = backendMessage || "An unexpected API error occurred.";
    }

    if (status === 401 && !isLoginRequest) {
      clearStoredAuth();
      if (window.location.pathname !== "/login") {
        window.location.assign("/login");
      }
    }

    return Promise.reject(error);
  },
);

export default api;
