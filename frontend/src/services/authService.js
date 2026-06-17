import api from "./api.js";

export async function loginRequest(credentials) {
  const response = await api.post("/api/auth/login", credentials);
  return response.data;
}

export async function registerRequest(payload) {
  const response = await api.post("/api/auth/register", payload);
  return response.data;
}
