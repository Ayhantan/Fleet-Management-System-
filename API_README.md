# Fleet Management API

This document describes the current HTTP API for the fleet management backend.

## Base URL

Local default:

```text
http://localhost:8080
```

## Authentication

The API uses JWT bearer authentication.

Public endpoints:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /health`

Protected endpoints:

- all other `/api/**` endpoints

Send the token in the `Authorization` header:

```http
Authorization: Bearer <token>
```

### Auth Responses

Successful register and login return:

```json
{
  "token": "jwt-token",
  "username": "demo",
  "email": "demo@example.com",
  "role": "USER"
}
```

### Security Error Responses

Unauthenticated request:

```json
{
  "status": 401,
  "message": "Unauthorized"
}
```

Invalid or expired token:

```json
{
  "status": 401,
  "message": "Invalid or expired token"
}
```

Forbidden request:

```json
{
  "status": 403,
  "message": "Forbidden"
}
```

## CORS

Current CORS configuration allows:

- origin: `http://localhost:5173`
- methods: `GET`, `POST`, `PUT`, `DELETE`, `PATCH`, `OPTIONS`
- headers: `Authorization`, `Content-Type`
- credentials: `true`

Applied path:

- `/api/**`

## Common Error Format

Application-level validation and domain errors are returned as JSON from the global exception handler.

Example:

```json
{
  "timestamp": "2026-06-17T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "validationErrors": {
    "fieldName": "must not be blank"
  }
}
```

Common fields:

- `timestamp`
- `status`
- `error`
- `message`
- `validationErrors` for bean validation failures

## Health

- `GET /health`

Response:

```text
OK
```

## Auth

### Register

- `POST /api/auth/register`

Example request:

```json
{
  "username": "demo",
  "email": "demo@example.com",
  "password": "secret123"
}
```

### Login

- `POST /api/auth/login`

Example request:

```json
{
  "username": "demo",
  "password": "secret123"
}
```

## Vehicles

- `POST /api/vehicles`
- `GET /api/vehicles`
- `GET /api/vehicles/{id}`
- `PUT /api/vehicles/{id}`
- `DELETE /api/vehicles/{id}`
- `PUT /api/vehicles/{vehicleId}/group/{groupId}`
- `DELETE /api/vehicles/{vehicleId}/group`
- `PATCH /api/vehicles/{vehicleId}/usage`

Key response fields:

- `id`
- `name`
- `plateNumber`
- `brand`
- `model`
- `modelYear`
- `type`
- `status`
- `vehicleGroupId`
- `vehicleGroupName`
- `category`
- `currentHourMeter`
- `currentDistanceReading`
- `lastMaintenanceDate`
- `lastMaintenanceHourMeter`
- `lastMaintenanceDistanceReading`
- `maintenanceTriggerType`
- `hourIntervalValue`
- `distanceIntervalValue`
- `timeIntervalValue`
- `timeIntervalUnit`
- `distanceUnit`

## Vehicle Groups

- `POST /api/vehicle-groups`
- `GET /api/vehicle-groups`
- `GET /api/vehicle-groups/{id}`
- `PUT /api/vehicle-groups/{id}`
- `DELETE /api/vehicle-groups/{id}`

## Maintenance Definitions

- `POST /api/maintenance-definitions`
- `GET /api/maintenance-definitions`
- `GET /api/maintenance-definitions/{id}`
- `PUT /api/maintenance-definitions/{id}`
- `DELETE /api/maintenance-definitions/{id}`

## Maintenance Schedules

- `POST /api/maintenance-schedules`
- `GET /api/maintenance-schedules`
- `GET /api/maintenance-schedules/{id}`
- `PUT /api/maintenance-schedules/{id}`
- `DELETE /api/maintenance-schedules/{id}`
- `POST /api/maintenance-schedules/{id}/recalculate`
- `GET /api/maintenance-schedules/upcoming`
- `GET /api/maintenance-schedules/overdue`

Key response fields:

- `id`
- `vehicleId`
- `vehicleName`
- `maintenanceDefinitionId`
- `maintenanceDefinitionName`
- `maintenanceDefinitionCategory`
- `triggerType`
- `intervalHour`
- `intervalDistance`
- `intervalTimeValue`
- `intervalTimeUnit`
- `nextDueDate`
- `nextDueHourMeter`
- `nextDueDistanceReading`
- `active`
- `calculatedStatus`

## Maintenance Tasks

- `POST /api/maintenance-tasks`
- `GET /api/maintenance-tasks`
- `GET /api/maintenance-tasks/{id}`
- `PUT /api/maintenance-tasks/{id}`
- `DELETE /api/maintenance-tasks/{id}`
- `POST /api/maintenance-tasks/{id}/complete`
- `GET /api/maintenance-tasks/history/vehicle/{vehicleId}`

Key response fields:

- `id`
- `vehicleId`
- `vehicleName`
- `maintenanceScheduleId`
- `maintenanceDefinitionId`
- `maintenanceDefinitionName`
- `title`
- `description`
- `status`
- `priority`
- `plannedDate`
- `dueDate`
- `dueHourMeter`
- `dueDistanceReading`
- `completedDate`
- `completedHourMeter`
- `completedDistanceReading`
- `notes`

## Work Orders

- `POST /api/work-orders`
- `GET /api/work-orders`
- `GET /api/work-orders/{id}`
- `PUT /api/work-orders/{id}`
- `DELETE /api/work-orders/{id}`
- `POST /api/work-orders/{id}/assign`
- `POST /api/work-orders/{id}/start`
- `POST /api/work-orders/{id}/complete`
- `GET /api/work-orders/vehicle/{vehicleId}`
- `POST /api/work-orders/from-maintenance-task`
- `POST /api/work-orders/{workOrderId}/parts`
- `GET /api/work-orders/{workOrderId}/parts`
- `POST /api/work-orders/{workOrderId}/expenses`
- `GET /api/work-orders/{workOrderId}/expenses`
- `GET /api/work-orders/{workOrderId}/cost-summary`

Key response fields:

- `id`
- `vehicleId`
- `vehicleName`
- `maintenanceTaskId`
- `maintenanceTaskTitle`
- `assignedUserId`
- `assignedUsername`
- `status`
- `title`
- `description`
- `completionNotes`
- `actualCost`
- `laborHours`
- `completedAt`

### Work Order Status Values

- `OPEN`
- `ASSIGNED`
- `IN_PROGRESS`
- `COMPLETED`
- `CANCELLED`

## Work Order Expenses

- `PUT /api/work-order-expenses/{expenseId}`
- `DELETE /api/work-order-expenses/{expenseId}`

Expense response fields:

- `id`
- `workOrderId`
- `costType`
- `description`
- `amount`
- `createdAt`
- `updatedAt`

### Cost Types

- `PART`
- `LABOR`
- `EXTERNAL_SERVICE`
- `MISC`

## Parts

- `POST /api/parts`
- `GET /api/parts`
- `GET /api/parts/{id}`
- `PUT /api/parts/{id}`
- `DELETE /api/parts/{id}`

## Inventory Items

- `POST /api/inventory-items`
- `GET /api/inventory-items`
- `GET /api/inventory-items/{id}`
- `PUT /api/inventory-items/{id}`
- `DELETE /api/inventory-items/{id}`
- `GET /api/inventory-items/low-stock`

Inventory response fields:

- `id`
- `partId`
- `partNumber`
- `partName`
- `currentQuantity`
- `minimumStockLevel`
- `location`
- `createdAt`
- `updatedAt`

## Stock Movements

- `POST /api/stock-movements/in`
- `POST /api/stock-movements/out`
- `GET /api/stock-movements/part/{partId}`

## Reports

All report endpoints are read-only and live under `/api/reports`.

- `GET /api/reports/dashboard-summary`
- `GET /api/reports/work-orders/status-summary`
- `GET /api/reports/work-orders/recent-completed`
- `GET /api/reports/maintenance/status-summary`
- `GET /api/reports/vehicles/maintenance-costs`
- `GET /api/reports/parts/consumption`
- `GET /api/reports/inventory/low-stock-summary`

### Dashboard Summary

Response fields:

- `totalVehicles`
- `totalWorkOrders`
- `openWorkOrders`
- `assignedWorkOrders`
- `inProgressWorkOrders`
- `completedWorkOrders`
- `cancelledWorkOrders`
- `upcomingMaintenanceCount`
- `overdueMaintenanceCount`
- `lowStockItemCount`
- `partCostTotal`
- `laborCostTotal`
- `externalServiceCostTotal`
- `miscCostTotal`
- `grandTotalMaintenanceCost`

### Work Order Status Summary

Response shape:

```json
{
  "totalWorkOrders": 12,
  "items": [
    {
      "status": "OPEN",
      "count": 2
    }
  ]
}
```

### Recent Completed Work Orders

Returns the most recent completed work orders ordered by `completedAt` descending.

Response fields:

- `workOrderId`
- `vehicleId`
- `vehicleDisplayName`
- `maintenanceTaskId`
- `maintenanceTaskTitle`
- `title`
- `completionNotes`
- `completedAt`

### Maintenance Status Summary

Response shape:

```json
{
  "totalActiveSchedules": 8,
  "items": [
    {
      "status": "OVERDUE",
      "count": 3
    }
  ]
}
```

Status values:

- `ON_TRACK`
- `UPCOMING`
- `OVERDUE`

### Vehicle Maintenance Costs

Response fields:

- `vehicleId`
- `vehicleDisplayName`
- `partCostTotal`
- `laborCostTotal`
- `externalServiceCostTotal`
- `miscCostTotal`
- `grandTotal`

### Part Consumption

Response fields:

- `partId`
- `partNumber`
- `partName`
- `totalQuantityUsed`
- `totalCost`

### Low Stock Summary

Response shape:

```json
{
  "lowStockItemCount": 2,
  "items": [
    {
      "inventoryItemId": 1,
      "partId": 3,
      "partNumber": "P-100",
      "partName": "Oil Filter",
      "currentQuantity": 2,
      "minimumStockLevel": 5,
      "location": "Main Shelf"
    }
  ]
}
```

## Notes for Frontend Integration

- JWT is required for all protected endpoints.
- For local frontend development, use `http://localhost:5173`.
- Validation failures include `validationErrors` keyed by field name.
- Domain conflicts often return HTTP `409`.
- Business rule violations often return HTTP `400`.
- Reports are computed dynamically and are read-only.
- Current list endpoints are not paginated.
- Current list endpoints have limited server-side filtering and sorting.
