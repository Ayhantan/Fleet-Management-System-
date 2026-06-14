# Distributed Maintenance & Fleet Management System

Spring Boot tabanli, portfolio-grade bir backend proje calismasi. Proje su an Sprint 1 kapsaminda ilerliyor ve ilk odak alani Vehicle module.

## Tech Stack

- Java 21
- Spring Boot
- Maven
- PostgreSQL
- Spring Data JPA
- Spring Security
- Jakarta Validation
- Lombok

## Current Status

Su ana kadar tamamlanan temel parcalar:

- Spring Boot application ayakta
- PostgreSQL baglantisi aktif
- Docker uzerinde veritabani kullaniliyor
- `/health` endpoint'i calisiyor
- Gecici `SecurityConfig` ile tum requestler su an `permitAll`
- Sprint 1 icin Vehicle CRUD module olusturuldu
- Vehicle domain modeli bakim stratejilerini destekleyecek sekilde guclendirildi

## Project Structure

Kod katmanli mimari ile organize edildi:

- `controller`
- `service`
- `repository`
- `dto`
- `entity`
- `exception`

Base package:

```text
com.ayhan.fleet_management
```

## Vehicle Module

Vehicle modeli artik sadece temel arac bilgilerini degil, ayni zamanda bakim davranisini da tanimliyor.

Amac:

- arac/ekipman kimligini tutmak
- guncel kullanim olcumlerini saklamak
- son bakim referanslarini saklamak
- bakimin zaman bazli, saat bazli veya mesafe bazli tetiklenmesini desteklemek

### Vehicle Domain Fields

- `id`
- `name`
- `type`
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

### MaintenanceTriggerType

- `TIME`
- `HOURS`
- `DISTANCE`
- `TIME_AND_HOURS`
- `TIME_AND_DISTANCE`

### TimeIntervalUnit

- `DAY`
- `WEEK`
- `MONTH`
- `YEAR`

### DistanceUnit

- `KILOMETER`
- `MILE`

## Validation Rules

Vehicle request validation iki seviyede yapiliyor:

1. DTO seviyesi
- zorunlu alanlar
- negatif deger engeli

2. Service seviyesi
- `HOURS` iceren trigger tiplerinde `currentHourMeter` ve `hourIntervalValue` zorunlu
- `DISTANCE` iceren trigger tiplerinde `currentDistanceReading`, `distanceIntervalValue` ve `distanceUnit` zorunlu
- `TIME` iceren trigger tiplerinde `timeIntervalValue` ve `timeIntervalUnit` zorunlu
- `lastMaintenanceHourMeter <= currentHourMeter`
- `lastMaintenanceDistanceReading <= currentDistanceReading`

## API Endpoints

### Health

- `GET /health`

### Vehicle

- `POST /api/vehicles`
- `GET /api/vehicles`
- `GET /api/vehicles/{id}`
- `PUT /api/vehicles/{id}`
- `DELETE /api/vehicles/{id}`

## Example Request Bodies

### 1. Heavy equipment with hour-based maintenance

```json
{
  "name": "CAT D6 Dozer",
  "type": "Dozer",
  "category": "Heavy Equipment",
  "currentHourMeter": 1820,
  "currentDistanceReading": null,
  "lastMaintenanceDate": "2026-05-20",
  "lastMaintenanceHourMeter": 1600,
  "lastMaintenanceDistanceReading": null,
  "maintenanceTriggerType": "HOURS",
  "hourIntervalValue": 250,
  "distanceIntervalValue": null,
  "timeIntervalValue": null,
  "timeIntervalUnit": null,
  "distanceUnit": null
}
```

### 2. Car with time and distance-based maintenance

```json
{
  "name": "Toyota Corolla Fleet 12",
  "type": "Car",
  "category": "Passenger Vehicle",
  "currentHourMeter": null,
  "currentDistanceReading": 86500,
  "lastMaintenanceDate": "2026-01-10",
  "lastMaintenanceHourMeter": null,
  "lastMaintenanceDistanceReading": 76500,
  "maintenanceTriggerType": "TIME_AND_DISTANCE",
  "hourIntervalValue": null,
  "distanceIntervalValue": 10000,
  "timeIntervalValue": 1,
  "timeIntervalUnit": "YEAR",
  "distanceUnit": "KILOMETER"
}
```

### 3. Equipment with only time-based maintenance

```json
{
  "name": "Air Compressor Unit A",
  "type": "Compressor",
  "category": "Workshop Equipment",
  "currentHourMeter": null,
  "currentDistanceReading": null,
  "lastMaintenanceDate": "2026-03-01",
  "lastMaintenanceHourMeter": null,
  "lastMaintenanceDistanceReading": null,
  "maintenanceTriggerType": "TIME",
  "hourIntervalValue": null,
  "distanceIntervalValue": null,
  "timeIntervalValue": 6,
  "timeIntervalUnit": "MONTH",
  "distanceUnit": null
}
```

## Database Configuration

Current local development database:

- Host: `localhost`
- Port: `5432`
- Database: `fleetdb`
- Username: `fleetuser`

JPA ayari:

- `ddl-auto: update`

## Running the Project

1. PostgreSQL container'in ayakta oldugundan emin ol
2. `fleetdb` veritabaninin hazir oldugunu kontrol et
3. Spring Boot application'i calistir
4. `http://localhost:8080/health` endpoint'ini test et

## Notes

- Authentication ve JWT henuz eklenmedi
- `SecurityConfig` su an gecici olarak acik yapida
- MaintenanceRecord ve MaintenancePolicy entity'leri bilerek Sprint 1 disinda tutuldu
- Domain modeli gereksiz soyutlama olmadan pragmatik tutuldu

## Next Possible Steps

- Maintenance record module
- Due maintenance calculation logic
- Dashboard / reporting endpoints
- JWT-based authentication and authorization
- Test coverage for service and controller layers
