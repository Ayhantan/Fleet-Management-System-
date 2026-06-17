import api from "./api.js";

export async function getWorkOrders() {
  const response = await api.get("/api/work-orders");
  return response.data;
}

export async function getWorkOrderById(id) {
  const response = await api.get(`/api/work-orders/${id}`);
  return response.data;
}

export async function createWorkOrder(payload) {
  const response = await api.post("/api/work-orders", payload);
  return response.data;
}

export async function startWorkOrder(id) {
  const response = await api.post(`/api/work-orders/${id}/start`);
  return response.data;
}

export async function completeWorkOrder(id, payload) {
  const response = await api.post(`/api/work-orders/${id}/complete`, payload);
  return response.data;
}

export async function cancelWorkOrder(id) {
  await api.delete(`/api/work-orders/${id}`);
}

export async function getWorkOrderParts(id) {
  const response = await api.get(`/api/work-orders/${id}/parts`);
  return response.data;
}

export async function addWorkOrderPart(id, payload) {
  const response = await api.post(`/api/work-orders/${id}/parts`, payload);
  return response.data;
}

export async function getWorkOrderExpenses(id) {
  const response = await api.get(`/api/work-orders/${id}/expenses`);
  return response.data;
}

export async function addWorkOrderExpense(id, payload) {
  const response = await api.post(`/api/work-orders/${id}/expenses`, payload);
  return response.data;
}

export async function getWorkOrderCostSummary(id) {
  const response = await api.get(`/api/work-orders/${id}/cost-summary`);
  return response.data;
}
