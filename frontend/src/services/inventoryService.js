import api from "./api.js";

export async function getParts() {
  const response = await api.get("/api/parts");
  return response.data;
}

export async function createPart(payload) {
  const response = await api.post("/api/parts", payload);
  return response.data;
}

export async function updatePart(id, payload) {
  const response = await api.put(`/api/parts/${id}`, payload);
  return response.data;
}

export async function deletePart(id) {
  await api.delete(`/api/parts/${id}`);
}

export async function getInventoryItems() {
  const response = await api.get("/api/inventory-items");
  return response.data;
}

export async function getLowStockInventoryItems() {
  const response = await api.get("/api/inventory-items/low-stock");
  return response.data;
}

export async function createInventoryItem(payload) {
  const response = await api.post("/api/inventory-items", payload);
  return response.data;
}

export async function updateInventoryItem(id, payload) {
  const response = await api.put(`/api/inventory-items/${id}`, payload);
  return response.data;
}

export async function deleteInventoryItem(id) {
  await api.delete(`/api/inventory-items/${id}`);
}

export async function stockInPart(payload) {
  const response = await api.post("/api/stock-movements/in", payload);
  return response.data;
}

export async function stockOutPart(payload) {
  const response = await api.post("/api/stock-movements/out", payload);
  return response.data;
}

export async function getStockMovementsByPart(partId) {
  const response = await api.get(`/api/stock-movements/part/${partId}`);
  return response.data;
}
