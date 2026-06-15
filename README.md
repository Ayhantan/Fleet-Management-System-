# Distributed Maintenance & Fleet Management System

A portfolio-grade backend project built with Spring Boot for managing fleet assets, maintenance planning, authentication, and operational tracking.

The project currently includes:
- JWT-based authentication
- vehicle and vehicle group management
- maintenance definition, schedule, task, work order, and inventory modules
- usage-based and time-based maintenance calculation support

## Tech Stack

- Java 21
- Spring Boot
- Maven
- PostgreSQL
- Spring Data JPA
- Spring Security
- Jakarta Validation
- Lombok

## Current Scope

The system is being developed incrementally in sprints.

Implemented so far:
- application bootstrap and database integration
- health check endpoint
- JWT authentication and authorization
- vehicle CRUD
- vehicle group CRUD
- vehicle-to-group assignment
- maintenance definitions as database-driven master data
- maintenance schedules linked to vehicles and maintenance definitions
- maintenance tasks for planned and completed maintenance work
- work orders linked to vehicles, maintenance tasks, and assigned users
- spare parts, inventory, stock movement, and work-order part usage tracking
- vehicle usage update endpoint with schedule recalculation
- maintenance completion flow with backward compatibility for existing vehicle maintenance fields

## Architecture

The project uses a layered monolith structure:

- `controller`
- `service`
- `repository`
- `dto`
- `entity`
- `exception`
- `security`
- `config`

Base package:

```text
com.ayhan.fleet_management
```

## Authentication Module

JWT-based authentication is implemented with stateless session management.

Public endpoints:
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /health`

Protected endpoints:
- `/api/vehicles/**`
- `/api/vehicle-groups/**`
- `/api/maintenance-definitions/**`
- `/api/maintenance-schedules/**`
- `/api/maintenance-tasks/**`
- `/api/work-orders/**`
- `/api/parts/**`
- `/api/inventory-items/**`
- `/api/stock-movements/**`

### Auth Flow

- users register with username, email, and password
- passwords are encoded with `BCryptPasswordEncoder`
- login returns a JWT token
- protected endpoints require `Authorization: Bearer <token>`

## Vehicle Module

`Vehicle` is currently the maintainable asset root. The name stays as `Vehicle` for now to avoid a large refactor, but the maintenance model is being designed to support a broader asset domain.

### Vehicle Operational Fields

- `id`
- `name`
- `plateNumber`
- `brand`
- `model`
- `modelYear`
- `type`
- `status`
- `imageUrl`
- `vehicleGroup`
- `createdAt`
- `updatedAt`

### Vehicle Maintenance Fields

The existing maintenance fields are still preserved for backward compatibility:

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

### Vehicle Status

- `ACTIVE`
- `IN_MAINTENANCE`
- `OUT_OF_SERVICE`

### Maintenance Trigger Types

- `TIME`
- `HOURS`
- `DISTANCE`
- `TIME_AND_HOURS`
- `TIME_AND_DISTANCE`

### Interval Units

Time:
- `DAY`
- `WEEK`
- `MONTH`
- `YEAR`

Distance:
- `KILOMETER`
- `MILE`

## Vehicle Group Module

Vehicles can be grouped for operational organization.

### VehicleGroup Fields

- `id`
- `name`
- `description`
- `createdAt`
- `updatedAt`

Rules:
- group name must be unique
- a vehicle may belong to zero or one group

## Maintenance Module

The maintenance module is designed around four main concepts:

1. `MaintenanceDefinition`
- reusable, database-driven maintenance master data
- examples: oil change, brake inspection, track inspection, nozzle replacement, landing gear inspection, custom maintenance item

2. `MaintenanceSchedule`
- links a `Vehicle` and a `MaintenanceDefinition`
- stores due rule and calculated next due values

3. `MaintenanceTask`
- represents an actual planned or completed maintenance job

4. `WorkOrder`
- represents operational execution and assignment for repair or maintenance work
- always linked to a `Vehicle`
- may optionally link to a `MaintenanceTask`
- may optionally be assigned to a `User`

### MaintenanceDefinition Fields

- `id`
- `name`
- `description`
- `category`
- `applicableAssetType`
- `active`
- `createdAt`
- `updatedAt`

### MaintenanceSchedule Fields

- `id`
- `vehicle`
- `maintenanceDefinition`
- `triggerType`
- `intervalHour`
- `intervalDistance`
- `intervalTimeValue`
- `intervalTimeUnit`
- `nextDueDate`
- `nextDueHourMeter`
- `nextDueDistanceReading`
- `active`
- `createdAt`
- `updatedAt`

### MaintenanceTask Fields

- `id`
- `vehicle`
- `maintenanceSchedule`
- `maintenanceDefinition`
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
- `createdAt`
- `updatedAt`

### Maintenance Task Status

- `PLANNED`
- `IN_PROGRESS`
- `COMPLETED`
- `CANCELLED`

### WorkOrder Fields

- `id`
- `vehicle`
- `maintenanceTask`
- `assignedUser`
- `title`
- `description`
- `status`
- `completionNotes`
- `actualCost`
- `laborHours`
- `completedAt`
- `createdAt`
- `updatedAt`

### Work Order Status

- `OPEN`
- `ASSIGNED`
- `IN_PROGRESS`
- `COMPLETED`
- `CANCELLED`

### Maintenance Priority

- `LOW`
- `MEDIUM`
- `HIGH`
- `CRITICAL`

### Calculated Maintenance Status

Response-only maintenance state:

- `ON_TRACK`
- `UPCOMING`
- `OVERDUE`

## Inventory Module

Sprint 6 adds spare parts and stock tracking so work orders can consume inventory with history preserved.

### Part Fields

- `id`
- `partNumber`
- `name`
- `description`
- `unit`
- `createdAt`
- `updatedAt`

### InventoryItem Fields

- `id`
- `part`
- `currentQuantity`
- `minimumStockLevel`
- `location`
- `createdAt`
- `updatedAt`

### StockMovement Fields

- `id`
- `part`
- `inventoryItem`
- `workOrder`
- `type`
- `quantity`
- `notes`
- `createdAt`

### WorkOrderPartUsage Fields

- `id`
- `workOrder`
- `part`
- `inventoryItem`
- `stockMovement`
- `quantityUsed`
- `notes`
- `createdAt`

### Stock Movement Types

- `IN`
- `OUT`

## Validation Rules

Validation is handled at both DTO and service levels.

Examples:
- `plateNumber` must be unique
- vehicle group name must be unique
- `HOURS` trigger requires hour-based inputs
- `DISTANCE` trigger requires distance-based inputs
- `TIME` trigger requires time interval fields
- maintenance schedules validate trigger-specific interval fields
- usage update requires at least one value
- a maintenance task may have at most one active work order
- active work orders are those not in `COMPLETED` or `CANCELLED`
- assigning a user to an `OPEN` work order automatically changes status to `ASSIGNED`
- `COMPLETED` and `CANCELLED` work orders are terminal states
- work order completion requires `completionNotes`
- part number must be unique
- exactly one inventory item is allowed per part
- inventory quantity fields cannot be negative
- stock movement and work order part usage quantities must be greater than 0
- stock cannot be consumed below available quantity
- parts can only be consumed for work orders in `OPEN`, `ASSIGNED`, or `IN_PROGRESS`
- referenced parts and inventory records cannot be deleted if history would break

## API Overview

### Health

- `GET /health`

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`

### Vehicles

- `POST /api/vehicles`
- `GET /api/vehicles`
- `GET /api/vehicles/{id}`
- `PUT /api/vehicles/{id}`
- `DELETE /api/vehicles/{id}`
- `PUT /api/vehicles/{vehicleId}/group/{groupId}`
- `DELETE /api/vehicles/{vehicleId}/group`
- `PATCH /api/vehicles/{vehicleId}/usage`

### Vehicle Groups

- `POST /api/vehicle-groups`
- `GET /api/vehicle-groups`
- `GET /api/vehicle-groups/{id}`
- `PUT /api/vehicle-groups/{id}`
- `DELETE /api/vehicle-groups/{id}`

### Maintenance Definitions

- `POST /api/maintenance-definitions`
- `GET /api/maintenance-definitions`
- `GET /api/maintenance-definitions/{id}`
- `PUT /api/maintenance-definitions/{id}`
- `DELETE /api/maintenance-definitions/{id}`

### Maintenance Schedules

- `POST /api/maintenance-schedules`
- `GET /api/maintenance-schedules`
- `GET /api/maintenance-schedules/{id}`
- `PUT /api/maintenance-schedules/{id}`
- `DELETE /api/maintenance-schedules/{id}`
- `POST /api/maintenance-schedules/{id}/recalculate`
- `GET /api/maintenance-schedules/upcoming`
- `GET /api/maintenance-schedules/overdue`

### Maintenance Tasks

- `POST /api/maintenance-tasks`
- `GET /api/maintenance-tasks`
- `GET /api/maintenance-tasks/{id}`
- `PUT /api/maintenance-tasks/{id}`
- `DELETE /api/maintenance-tasks/{id}`
- `POST /api/maintenance-tasks/{id}/complete`
- `GET /api/maintenance-tasks/history/vehicle/{vehicleId}`

### Work Orders

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

### Parts

- `POST /api/parts`
- `GET /api/parts`
- `GET /api/parts/{id}`
- `PUT /api/parts/{id}`
- `DELETE /api/parts/{id}`

### Inventory Items

- `POST /api/inventory-items`
- `GET /api/inventory-items`
- `GET /api/inventory-items/{id}`
- `PUT /api/inventory-items/{id}`
- `DELETE /api/inventory-items/{id}`
- `GET /api/inventory-items/low-stock`

### Stock Movements

- `POST /api/stock-movements/in`
- `POST /api/stock-movements/out`
- `GET /api/stock-movements/part/{partId}`

## Local Development Configuration

Current local database setup:

- Host: `localhost`
- Port: `5432`
- Database: `fleetdb`
- Username: `fleetuser`

JPA setting:

- `spring.jpa.hibernate.ddl-auto=update`

## Running the Project

1. Make sure the PostgreSQL container is running.
2. Verify that the `fleetdb` database is available.
3. Start the Spring Boot application.
4. Test `http://localhost:8080/health`.
5. Register a user via `/api/auth/register`.
6. Log in via `/api/auth/login`.
7. Use the returned JWT token for protected endpoints.

## Testing

Current automated coverage focuses on service-layer business rules for Sprint 6:

- duplicate inventory item prevention per part
- part delete rejection when references exist
- stock-out rejection when quantity exceeds available stock
- work-order part consumption rejection for terminal work orders
- successful work-order part consumption with linked stock movement creation

Run tests with:

```bash
mvn test
```

## Notes

- JWT authentication is enabled and sessions are stateless.
- `Vehicle` remains the current maintainable asset root for compatibility.
- maintenance definitions are database records, not enums
- work order delete is a domain cancel, not a physical delete
- inventory history is preserved through stock movements and work order part usage records
- no file upload, invoice, technician, workshop, cron jobs, or event sourcing yet
- the design stays intentionally simple and interview-ready while remaining extensible

## Next Likely Steps

- maintenance overview and dashboard responses
- urgency scoring and richer due calculations
- role-based access restrictions
- controller-layer and integration test coverage
- work order filtering, search, and reporting
- supplier and purchase order flows
- future asset abstraction beyond `Vehicle`
