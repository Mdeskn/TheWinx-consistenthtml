# BC-02: Fleet Management

**Owner:** Priyanka | **Port:** 8082

---

## What this service does

BC-02 is the single source of truth for vehicles. Providers register their fleet here, set pricing models (per hour, per kilometre, or per day), and define usage restrictions per vehicle. BC-03 calls BC-02 when a user searches for vehicles and sends status-change commands (`BOOKED` / `AVAILABLE`) when rides start and end.

Two business rules enforced:
- `pricePerUnit` must be greater than 0.
- A vehicle cannot be deleted while its status is `BOOKED`.

---

## How it fits in the system

```
BC-02 Fleet Management
    │
    ├── consumed by BC-01 (vehicle list on the dashboard)
    ├── consumed by BC-03 (vehicle search, status updates)
    └── consumed by BC-05 (vehicle details enrichment on rating pages)
```

BC-02 is a shared upstream - downstream contexts use an Anti-Corruption Layer (ACL) to translate its data.

---

## Project structure

```
bc02-fleet-management/
└── src/main/java/com/winx/fleet/
    │
    ├── FleetManagementApplication.java     Entry point
    │
    ├── model/
    │   ├── Vehicle.java                    JPA entity - aggregate root
    │   │                                   Fields: id, providerId, name, type, description,
    │   │                                   status, latitude, longitude, pricePerUnit,
    │   │                                   billingModel, maxDurationMinutes, maxKilometers,
    │   │                                   minAge, maxPersons
    │   ├── VehicleType.java                Enum: E_SCOOTER, BICYCLE, E_BIKE, E_CAR
    │   ├── VehicleStatus.java              Enum: AVAILABLE, BOOKED, MAINTENANCE, UNAVAILABLE
    │   └── BillingModel.java               Enum: PER_HOUR, PER_KILOMETER, PER_DAY
    │
    ├── repository/
    │   └── VehicleRepository.java          Spring Data JPA - findByProviderId, custom geo query
    │
    ├── service/
    │   ├── VehicleRegistrationService.java Create and delete vehicles (validates pricePerUnit > 0)
    │   ├── VehicleAvailabilityService.java Get all vehicles, get by ID, search near a location
    │   └── FleetStatusService.java         Update GPS location or status of a vehicle
    │
    ├── dto/
    │   ├── CreateVehicleRequest.java       Input: providerId, name, type, description, pricing, restrictions
    │   ├── UpdateVehicleRequest.java       Input: name, description, pricing, restrictions
    │   ├── UpdateLocationRequest.java      Input: latitude, longitude
    │   ├── UpdateStatusRequest.java        Input: status
    │   ├── StatusUpdate.java               Enum-only update (used by BC-03 for BOOKED/AVAILABLE)
    │   └── VehicleResponse.java            Output: all vehicle fields
    │
    ├── controller/
    │   ├── VehicleController.java          REST CRUD + search endpoint
    │   ├── FleetController.java            Search endpoint for BC-03 (GET /api/v1/vehicles/search)
    │   └── HelloController.java            Health probe - GET /hello returns "Fleet is UP"
    │
    ├── exception/
    │   ├── VehicleNotFoundException.java   Thrown when vehicleId does not exist
    │   ├── InvalidVehicleException.java    Thrown for constraint violations (price <= 0, etc.)
    │   └── GlobalExceptionHandler.java     Maps exceptions to HTTP 400/404
    │
    └── config/
        └── CorsConfig.java                 Allows cross-origin requests from any localhost port
```

```
src/main/resources/
├── application.yml    Port 8082, H2 (jdbc:h2:mem:fleet), Eureka, Config Server
├── data.sql           Seeds 8 vehicles (2 per provider) on startup
└── static/
    ├── list.html      Vehicle list + detail panel (provider-facing)
    ├── add.html       Add / edit vehicle form
    ├── search.html    Location-based vehicle search
    └── fleet-style.css  BC-02 specific styles (supplemented by shared styles.css via BC-01)
```

---

## Seeded vehicles (data.sql)

8 vehicles across 3 providers - IDs 1–8. Types: BICYCLE (×2), E_SCOOTER (×2), E_BIKE (×2), E_CAR (×2). All start with status `AVAILABLE` near Dortmund coordinates.

---

## Prerequisites

Java 21. Verify with `java -version`.

---

## Run

```bash
# Standalone - no other services needed
./mvnw -pl bc02-fleet-management spring-boot:run
```

---

## UI pages

| URL | Description |
|-----|-------------|
| `http://localhost:8082/list.html` | All vehicles - click a row to see details in a side panel |
| `http://localhost:8082/add.html` | Add a new vehicle |
| `http://localhost:8082/search.html` | Search vehicles near a GPS coordinate |

---

## REST API

Full docs: `http://localhost:8082/swagger-ui/index.html`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/vehicles` | Register a new vehicle |
| `GET` | `/api/v1/vehicles` | List all vehicles |
| `GET` | `/api/v1/vehicles/{id}` | Get vehicle by ID |
| `PUT` | `/api/v1/vehicles/{id}` | Update vehicle details |
| `DELETE` | `/api/v1/vehicles/{id}/delete` | Delete a vehicle |
| `PATCH` | `/api/v1/vehicles/{id}/location` | Update GPS location |
| `PATCH` | `/api/v1/vehicles/{id}/status` | Update status |
| `GET` | `/api/v1/vehicles/search` | Search available vehicles near a point |

**Register a vehicle:**
```bash
curl -X POST http://localhost:8082/api/v1/vehicles \
  -H "Content-Type: application/json" \
  -d '{
    "providerId": 1,
    "name": "Lime Scooter #12",
    "type": "E_SCOOTER",
    "description": "Electric scooter, 25 km/h top speed",
    "pricePerUnit": 0.20,
    "billingModel": "PER_KILOMETER",
    "maxDurationMinutes": 120,
    "maxKilometers": 30,
    "minAge": 18,
    "maxPersons": 1
  }'
```

**Search nearby vehicles:**
```bash
# Dortmund city centre, 5 km radius
curl "http://localhost:8082/api/v1/vehicles/search?lat=51.5178&lon=7.4590&radiusKm=5"
```

**Update vehicle status (called by BC-03 when booking starts):**
```bash
curl -X PATCH http://localhost:8082/api/v1/vehicles/1/status \
  -H "Content-Type: application/json" \
  -d '{"status":"BOOKED"}'
```

---

## Other endpoints

| URL | What |
|-----|------|
| `http://localhost:8082/swagger-ui/index.html` | Interactive API docs |
| `http://localhost:8082/h2-console` | H2 browser - JDBC URL: `jdbc:h2:mem:fleet`, user: `sa` |
| `http://localhost:8082/actuator/health` | Health check |
| `http://localhost:8082/hello` | Quick liveness probe |

---

## Assignment coverage

| Assignment | What BC-02 contributes |
|---|---|
| A02 - Requirements | R12 Manage Vehicles (CRUD), R13 Define Pricing Model, R14 Define Vehicle Restrictions, R15 View Fleet Status |
| A03 - Context Map | Shared upstream; OHS/PL → ACL consumed by BC-03 and BC-05 |
| A04 - Tactical Design | Aggregate: `Vehicle`; Value objects: `VehicleLocation`, `PricingPolicy`, `UsageRestrictions`; Domain events: `VehicleCreated`, `VehicleStatusUpdated` |
| A05 Task 1 - Standalone | Runs independently on port 8082 with 8 seeded vehicles and full Swagger docs |
| A05 Task 2 - Integration | Vehicle status updates from BC-03 via REST PATCH; vehicle data consumed by BC-01 and BC-05 via Feign |
| A06 - LEMMA | `fleetManagement.data`, `.services`, `.mapping`, `.operation` in `lemma/bc02-fleet-management/` |
