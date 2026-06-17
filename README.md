# Fleet Management System

A full-stack fleet maintenance and operations project built with Spring Boot and React. The system focuses on vehicle tracking, maintenance planning, work order execution, inventory control, and operational reporting.

This repository started as a backend-first project and now includes a responsive frontend for desktop, tablet, and mobile use.

## Overview

The project covers the core workflows of a maintenance-oriented fleet operation:

- authentication with JWT
- vehicle and vehicle group management
- maintenance definitions, schedules, and tasks
- work order lifecycle management
- spare parts, inventory, and stock movement tracking
- maintenance cost tracking
- dashboard and reporting endpoints

The goal is to keep the architecture simple enough to understand quickly, while still modeling realistic business rules.

## Tech Stack

### Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Maven
- Jakarta Validation
- Lombok

### Frontend

- React
- Vite
- React Router
- Axios
- Tailwind CSS

## Architecture

The backend follows a layered monolith structure:

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

The frontend lives under:

```text
frontend/
```

and is organized around:

- `pages`
- `components`
- `services`
- `hooks`
- `utils`

## Current Features

### Authentication

- register and login with JWT
- stateless API authentication
- bearer token handling in the frontend
- automatic redirect to login on `401`

### Vehicles

- vehicle listing
- vehicle creation
- vehicle detail modal
- vehicle editing
- vehicle usage update
- vehicle group assignment and removal
- vehicle deletion

### Maintenance

- maintenance definition management
- maintenance schedule management
- schedule recalculation
- maintenance task management
- maintenance task completion flow

### Work Orders

- work order listing and filtering
- work order creation
- work order detail view
- start, complete, and cancel actions
- part usage registration
- expense registration
- cost summary display

### Inventory

- part management
- inventory item management
- low stock visibility
- stock in / stock out operations
- stock movement history by part

### Reporting

- dashboard summary
- low stock summary
- work order status summary
- maintenance status summary
- recent completed work orders
- vehicle maintenance cost report
- part consumption report

## Frontend Status

The frontend is connected directly to the Spring Boot API and covers the main operational flows.

Available routes:

- `/login`
- `/register`
- `/dashboard`
- `/vehicles`
- `/work-orders`
- `/maintenance`
- `/inventory`
- `/reports`

## API Summary

Main endpoint groups:

- `/api/auth`
- `/api/vehicles`
- `/api/vehicle-groups`
- `/api/maintenance-definitions`
- `/api/maintenance-schedules`
- `/api/maintenance-tasks`
- `/api/work-orders`
- `/api/work-order-expenses`
- `/api/parts`
- `/api/inventory-items`
- `/api/stock-movements`
- `/api/reports`

Detailed API documentation is available in [API_README.md](/d:/Project/fleet-management/API_README.md).

## Business Rules

Some important rules currently enforced by the backend:

- plate numbers must be unique
- vehicle group names must be unique
- maintenance trigger configuration must match the selected trigger type
- a maintenance task can have at most one active work order
- assigning a user to an open work order moves it to `ASSIGNED`
- `COMPLETED` and `CANCELLED` work orders are terminal
- stock cannot go below available quantity
- inventory quantity values cannot be negative
- expenses must be greater than zero
- cost summaries are calculated dynamically from usage and expense records

## Local Development

### Prerequisites

- Java 21
- Node.js 22+ recommended
- Docker
- PostgreSQL client optional

### Database

The project includes a PostgreSQL container setup:

```powershell
docker compose up -d
```

Default local database settings:

- host: `localhost`
- port: `5432`
- database: `fleetdb`
- username: `fleetuser`
- password: `fleetpass`

### Run Backend

From the project root:

```powershell
.\mvnw.cmd spring-boot:run
```

Backend default URL:

```text
http://localhost:8080
```

Health check:

```text
http://localhost:8080/health
```

### Run Frontend

From the `frontend` directory:

```powershell
npm.cmd install
npm.cmd run dev
```

Frontend default URL:

```text
http://localhost:5173
```

## Authentication Flow

Public endpoints:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /health`

Protected endpoints require:

```http
Authorization: Bearer <token>
```

Login and register responses return:

```json
{
  "token": "jwt-token",
  "username": "demo",
  "email": "demo@example.com",
  "role": "USER"
}
```

## Testing

Run backend tests with:

```powershell
mvn test
```

Current automated coverage is focused mainly on service-layer business rules around:

- inventory constraints
- work order state transitions
- expense rules
- cost summary calculations
- reporting aggregation

## Known Limitations

- no pagination on list endpoints yet
- no frontend onboarding beyond register/login
- no user listing endpoint for richer work order assignment UX
- role information is returned in JWT responses, but role-based authorization is still limited
- some reporting queries are implemented in service-level aggregation and may need optimization later

## Project Notes

- `Vehicle` is still the main maintainable asset entity
- maintenance definitions are database records, not enums
- deleting a work order is implemented as a domain cancel flow
- reporting is read-only
- the current design intentionally stays straightforward and interview-friendly

## Roadmap Ideas

- role-based access control
- pagination and filtering improvements
- richer dashboard drill-down flows
- controller and integration test coverage
- file attachments and media support
- more advanced assignment and operator management

## License

This project is licensed under the terms of the [LICENSE](LICENSE) file.
