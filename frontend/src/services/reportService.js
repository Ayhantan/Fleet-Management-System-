import api from "./api.js";

export async function getDashboardSummary() {
  const response = await api.get("/api/reports/dashboard-summary");
  return response.data;
}

export async function getLowStockSummary() {
  const response = await api.get("/api/reports/inventory/low-stock-summary");
  return response.data;
}

export async function getWorkOrderStatusSummary() {
  const response = await api.get("/api/reports/work-orders/status-summary");
  return response.data;
}

export async function getRecentCompletedWorkOrders() {
  const response = await api.get("/api/reports/work-orders/recent-completed");
  return response.data;
}

export async function getMaintenanceStatusSummary() {
  const response = await api.get("/api/reports/maintenance/status-summary");
  return response.data;
}

export async function getVehicleMaintenanceCosts() {
  const response = await api.get("/api/reports/vehicles/maintenance-costs");
  return response.data;
}

export async function getPartConsumptionReport() {
  const response = await api.get("/api/reports/parts/consumption");
  return response.data;
}
