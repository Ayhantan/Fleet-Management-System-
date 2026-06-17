import api from "./api.js";

export async function getMaintenanceDefinitions() {
  const response = await api.get("/api/maintenance-definitions");
  return response.data;
}

export async function createMaintenanceDefinition(payload) {
  const response = await api.post("/api/maintenance-definitions", payload);
  return response.data;
}

export async function updateMaintenanceDefinition(id, payload) {
  const response = await api.put(`/api/maintenance-definitions/${id}`, payload);
  return response.data;
}

export async function deleteMaintenanceDefinition(id) {
  await api.delete(`/api/maintenance-definitions/${id}`);
}

export async function getMaintenanceSchedules() {
  const response = await api.get("/api/maintenance-schedules");
  return response.data;
}

export async function getUpcomingMaintenanceSchedules() {
  const response = await api.get("/api/maintenance-schedules/upcoming");
  return response.data;
}

export async function getOverdueMaintenanceSchedules() {
  const response = await api.get("/api/maintenance-schedules/overdue");
  return response.data;
}

export async function createMaintenanceSchedule(payload) {
  const response = await api.post("/api/maintenance-schedules", payload);
  return response.data;
}

export async function updateMaintenanceSchedule(id, payload) {
  const response = await api.put(`/api/maintenance-schedules/${id}`, payload);
  return response.data;
}

export async function recalculateMaintenanceSchedule(id) {
  const response = await api.post(`/api/maintenance-schedules/${id}/recalculate`);
  return response.data;
}

export async function deleteMaintenanceSchedule(id) {
  await api.delete(`/api/maintenance-schedules/${id}`);
}

export async function getMaintenanceTasks() {
  const response = await api.get("/api/maintenance-tasks");
  return response.data;
}

export async function createMaintenanceTask(payload) {
  const response = await api.post("/api/maintenance-tasks", payload);
  return response.data;
}

export async function updateMaintenanceTask(id, payload) {
  const response = await api.put(`/api/maintenance-tasks/${id}`, payload);
  return response.data;
}

export async function completeMaintenanceTask(id, payload) {
  const response = await api.post(`/api/maintenance-tasks/${id}/complete`, payload);
  return response.data;
}

export async function deleteMaintenanceTask(id) {
  await api.delete(`/api/maintenance-tasks/${id}`);
}
