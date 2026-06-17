import api from "./api.js";

export async function getVehicles() {
  const response = await api.get("/api/vehicles");
  return response.data;
}

export async function createVehicle(payload) {
  const response = await api.post("/api/vehicles", payload);
  return response.data;
}

export async function updateVehicle(id, payload) {
  const response = await api.put(`/api/vehicles/${id}`, payload);
  return response.data;
}

export async function updateVehicleUsage(id, payload) {
  const response = await api.patch(`/api/vehicles/${id}/usage`, payload);
  return response.data;
}

export async function assignVehicleGroup(vehicleId, groupId) {
  const response = await api.put(`/api/vehicles/${vehicleId}/group/${groupId}`);
  return response.data;
}

export async function removeVehicleGroup(vehicleId) {
  const response = await api.delete(`/api/vehicles/${vehicleId}/group`);
  return response.data;
}

export async function deleteVehicle(id) {
  await api.delete(`/api/vehicles/${id}`);
}

export async function getVehicleGroups() {
  const response = await api.get("/api/vehicle-groups");
  return response.data;
}
