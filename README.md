# The Winx -- Instant Mobility Platform

**Team:** The Winx | **Course:** DMSA SoSe 2026 | **Scenario 4**

This platform lets users find, book, and ride shared vehicles (e-scooters, bicycles, e-bikes, e-cars) from multiple providers in one place. Providers register their fleet, set pricing models, and monitor availability. We designed and built the system as five independent microservices following Domain-Driven Design principles, connected through an event-driven context map.

---

## Team

| Member | Bounded Context | Port |
|---|---|---|
| Sama | [BC-01: Identity & Access](bc01-identity-access/) | 8081 |
| Priyanka | [BC-02: Fleet Management](bc02-fleet-management/) | 8082 |
| Rowena | [BC-03: Booking](bc03-booking/) | 8083 |
| Marianne | [BC-04: Payment](bc04-payment/) | 8084 |
| Mae | [BC-05: Rating](bc05-rating/) | 8085 |

Infrastructure services (Eureka, Config Server, API Gateway) are shared across the team.

---

## System Overview

```
                   [ Eureka :8761 ]   [ Config Server :8888 ]
                               |              |
                        [ API Gateway :8080 ]
                               |
     +-----------+----------------------------+-----------+
     |           |            |               |           |
BC-01:8081  BC-02:8082   BC-03:8083   BC-04:8084  BC-05:8085
Identity &  Fleet Mgmt    Booking      Payment      Rating
  Access
```

**Context Map (Vernon's DDD notation):**

We applied Open Host Service / Published Language (OHS/PL) where an upstream provides a stable shared API, and Anti-Corruption Layer (ACL) where the downstream must translate to preserve its own model. Between Booking and its two consumers (Payment and Rating) we used Customer/Supplier with Conformist (C/S -> CF), since both downstream contexts had design input and accepted the Booking interface without needing a translation layer.

| Upstream | Downstream | Pattern |
|---|---|---|
| Identity & Access | Fleet Mgmt, Booking, Rating | OHS/PL -> ACL |
| Fleet Mgmt | Booking, Rating | OHS/PL -> ACL |
| Booking | Payment | C/S -> CF |
| Booking | Rating | C/S -> CF |

At runtime: BC-01 proxies vehicle and booking requests to BC-02 and BC-03 for authenticated users. BC-03 is the central operational hub -- it calls BC-01 (identity check), BC-02 (vehicle status), and BC-04 (payment trigger) as part of the ride lifecycle. BC-04 and BC-05 are leaf contexts; nothing reads from them downstream.

---

## Prerequisites

**Java 17** is required. Check with `java -version`.

**Windows:**
Download and install from [https://adoptium.net](https://adoptium.net) (Eclipse Temurin 17 LTS). The installer sets `JAVA_HOME` and updates `PATH` automatically.

**macOS:**
```bash
brew install openjdk@17
export JAVA_HOME=$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
```
Add those two lines to `~/.zshrc` to persist across terminals.

**Linux (Debian/Ubuntu):**
```bash
sudo apt update && sudo apt install openjdk-17-jdk
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
```

Maven is not needed separately. We include the `mvnw` / `mvnw.cmd` wrapper.

---

## Get the Code

```bash
git clone https://github.com/samaalzoubi/TheWinx-SS2026.git
cd TheWinx-SS2026
```

---

## Running the Full System

### Option 1 -- Startup script (recommended)

**macOS / Linux:**
```bash
chmod +x start.sh
./start.sh
```

**Windows:**
```bat
start.bat
```

The script builds all modules, waits for Eureka and the Config Server to be healthy, then launches all five business services and the API Gateway in the background. Logs go to a `logs/` folder in the project root.

Once everything is running, open `http://localhost:8761` in a browser. All five services should appear as UP in the Eureka dashboard.

### Option 2 -- Manual, one terminal per service

Start in this order:

```bash
# 1. Service registry
./mvnw -pl infra-eureka-server spring-boot:run       # wait for port 8761

# 2. Config server
./mvnw -pl infra-config-server spring-boot:run       # wait for port 8888

# 3. Business services (any order)
./mvnw -pl bc01-identity-access  spring-boot:run
./mvnw -pl bc02-fleet-management spring-boot:run
./mvnw -pl bc03-booking          spring-boot:run
./mvnw -pl bc04-payment          spring-boot:run
./mvnw -pl bc05-rating           spring-boot:run

# 4. Optional gateway
./mvnw -pl infra-api-gateway spring-boot:run         # port 8080
```

On Windows replace `./mvnw` with `mvnw.cmd`.

---

## Running a Single Service (Standalone)

Every service boots independently without Eureka or the Config Server. Feign circuit breaker fallbacks return safe defaults when dependencies are not reachable.

```bash
# macOS / Linux
./mvnw -pl bc05-rating spring-boot:run

# Windows
mvnw.cmd -pl bc05-rating spring-boot:run
```

Each service exposes:

| URL | What |
|---|---|
| `http://localhost:<port>/swagger-ui/index.html` | Interactive API docs |
| `http://localhost:<port>/h2-console` | In-memory database browser (user: `sa`, no password) |
| `http://localhost:<port>/actuator/health` | Health check |

---

## Testing

The startup script prints all URLs to the terminal once every service is up. Here is the full reference:

| Service | Page | URL |
|---|---|---|
| BC-01 Identity & Access | Home | `http://localhost:8081/` |
| | Login | `http://localhost:8081/ui/login` |
| | Register | `http://localhost:8081/ui/register` |
| | Dashboard | `http://localhost:8081/ui/dashboard` |
| | Swagger | `http://localhost:8081/swagger-ui/index.html` |
| BC-02 Fleet Management | Vehicle list | `http://localhost:8082/list.html` |
| | Add vehicle | `http://localhost:8082/add.html` |
| | Search nearby | `http://localhost:8082/search.html` |
| | Swagger | `http://localhost:8082/swagger-ui/index.html` |
| BC-03 Booking | Vehicle search | `http://localhost:8083/ui/search` |
| | Booking history | `http://localhost:8083/ui/bookings` |
| | Swagger | `http://localhost:8083/swagger-ui/index.html` |
| BC-04 Payment | Payment list | `http://localhost:8084/payments` |
| | Create payment | `http://localhost:8084/payments/new` |
| | Swagger | `http://localhost:8084/swagger-ui/index.html` |
| BC-05 Rating | Rating list | `http://localhost:8085/ratings` |
| | Submit rating | `http://localhost:8085/ratings/submit` |
| | Swagger | `http://localhost:8085/swagger-ui/index.html` |

Every service also has an H2 database browser at `/h2-console` (user: `sa`, no password). JDBC URLs: `jdbc:h2:mem:identity`, `jdbc:h2:mem:fleet`, `jdbc:h2:mem:booking`, `jdbc:h2:mem:payment`, `jdbc:h2:mem:rating`.

**End-to-end ride flow:**
1. Register a user at `http://localhost:8081/ui/register`
2. Add a vehicle at `http://localhost:8082/add.html` or via BC-02 Swagger (`POST /vehicles`)
3. Search for vehicles at `http://localhost:8083/ui/search`
4. Create a booking from the search results
5. End the ride from the booking detail page -- triggers cost calculation and payment automatically
6. Check the payment at `http://localhost:8084/payments`
7. Submit a rating at `http://localhost:8085/ratings/submit`

---

## Bounded Contexts

### BC-01: Identity & Access

[bc01-identity-access/](bc01-identity-access/) | Port 8081 | Owner: Sama

The global upstream for the whole platform. Every other context authenticates against it. We manage user and provider accounts, session tokens, and roles here, and expose explicit contract endpoints so downstream services can verify user eligibility without touching the identity database directly.

**DDD building blocks:**

- **Aggregates:** `UserAccount`, `ProviderAccount`
- **Value objects:** `Email` (validated on construction), `Password` (hashed, immutable), `PhoneNumber`, `PersonalInfo`, `CompanyInfo`, `AuthToken`
- **Domain services:** `AuthenticationService` (login, token), `RegistrationService` (user and provider signup)
- **Domain events:** `UserRegistered`, `ProviderRegistered`, `UserAuthenticated`

**API surface:**

- `/api/v1/identity` -- user registration, login, role management, account lifecycle (PENDING -> ACTIVE -> DEACTIVATED)
- `/api/v1/identity/fleet` -- authenticated proxy to BC-02 (vehicle list) and BC-03 (bookings)
- `/api/contracts/v1` -- internal contracts consumed by BC-03 (`/booking/identity-check`), BC-04 (`/payment/eligibility`), BC-02 (`/provider/access`)

Outgoing calls to BC-02 and BC-03 are protected by Resilience4j circuit breakers (sliding window 5, 50% threshold, 10s wait).

---

### BC-02: Fleet Management

[bc02-fleet-management/](bc02-fleet-management/) | Port 8082 | Owner: Priyanka

Single source of truth for vehicles. Providers manage their fleet here and configure pricing and usage restrictions per vehicle. BC-03 reads vehicle data when users search and sends status-change commands when rides start and end.

**DDD building blocks:**

- **Aggregate:** `Vehicle` (status: AVAILABLE, BOOKED, MAINTENANCE)
- **Value objects:** `VehicleLocation` (GPS coordinates, replaced on each update), `PricingPolicy` (pricePerUnit + billing model, immutable), `UsageRestrictions` (maxDurationMins, maxKm, minAge, maxPersons -- null means no restriction)
- **Enums:** `VehicleType` (E_SCOOTER, BICYCLE, E_BIKE, E_CAR), `BillingModel` (PER_HOUR, PER_KILOMETER)
- **Domain services:** `VehicleRegistrationService`, `FleetStatusService`, `VehicleAvailabilityService`
- **Domain events:** `VehicleCreated`, `VehicleStatusUpdated`, `VehicleLocationUpdated`, `VehicleDeleted`

**Key invariant:** `pricePerUnit` must be greater than 0. A vehicle cannot be deleted while its status is BOOKED.

---

### BC-03: Booking

[bc03-booking/](bc03-booking/) | Port 8083 | Owner: Rowena

The operational hub. It coordinates the full ride lifecycle -- from vehicle search through booking creation, cost computation, payment trigger, and ride completion -- calling BC-01, BC-02, and BC-04 along the way.

**DDD building blocks:**

- **Aggregate:** `Booking` (status: ACTIVE -> COMPLETED or CANCELLED)
- **Value objects:** `VehicleSnapshot` (captures vehicle price at booking time, decoupled from future provider changes), `RideLocation` (start and end GPS coordinates), `TimeInterval` (startTime, endTime), `RideSummary` (distanceKm, totalCost -- populated on completion)
- **Domain services:** `BookingService`, `CostCalculationService` (strategy pattern: `HourlyPricing` and `PerKilometerPricing`), `RestrictionValidator` (validates user eligibility against vehicle restrictions before confirming a booking)
- **Domain events:** `BookingCreated`, `BookingCompleted`, `BookingCancelled`, `PaymentTriggered`

**Key invariant:** only one active booking per user at a time. `VehicleSnapshot` locks in the price at booking time so mid-ride provider changes have no effect on the final cost.

---

### BC-04: Payment

[bc04-payment/](bc04-payment/) | Port 8084 | Owner: Marianne

Receives a payment trigger from BC-03 when a ride ends and records the outcome. Payment is a leaf context -- it consumes from Booking and nothing depends on it downstream.

**DDD building blocks:**

- **Aggregate:** `Payment` (status: PENDING -> PAID or FAILED, strictly one-directional)
- **Value objects:** `Money` (amount + currency, ISO 4217), `PaymentMethod` (type + masked reference), `PaymentResult` (status + timestamp + optional failure reason)
- **Payment methods:** `CREDIT_CARD`, `DEBIT_CARD`, `PAYPAL`
- **Domain services:** `PaymentProcessingService`, `PaymentGatewayAdapter`
- **Domain events:** `PaymentInitiated`, `PaymentSucceeded`, `PaymentFailed`

---

### BC-05: Rating

[bc05-rating/](bc05-rating/) | Port 8085 | Owner: Mae

Lets users rate a completed ride. Rating is the other leaf context -- it reads from Booking and Fleet Management but nothing downstream depends on it.

**DDD building blocks:**

- **Aggregate:** `Rating` (immutable once submitted)
- **Value objects:** `Score` (integer 1-5, validated at construction), `Review` (vehicleScore + providerScore + optional comment), `RatingTarget` (vehicleId, providerId, bookingId)
- **Domain services:** `RatingSubmissionService` (validates that the booking is COMPLETED and has not been rated before), `RatingQueryService` (filters and computes average scores)
- **Domain event:** `RatingSubmitted`

Two business rules enforced before persistence: a booking can only be rated once, and only COMPLETED bookings are eligible. BC-03 is called to verify status; if BC-03 is unreachable the circuit breaker fallback returns COMPLETED so users are never blocked by a downstream outage.

---

## Infrastructure

| Service | Port | Role |
|---|---|---|
| Eureka Server | 8761 | Service registry. All services register here; Feign clients resolve names through it |
| Config Server | 8888 | Serves the `config/` directory to all services. `optional:` prefix means services boot without it |
| API Gateway | 8080 | Single entry point, routes by path prefix with Eureka-based load balancing |

The `config/` folder at the repository root contains one YAML file per service plus a shared `application.yml`. The Config Server serves them all at startup so settings can be changed without rebuilding any service.

---

## LEMMA Models

The `lemma/` folder contains formal LEMMA models for three bounded contexts (BC-01, BC-04, BC-05) and the Eureka infrastructure node. LEMMA is a modeling language for microservice architectures validated in Eclipse.

```
lemma/
├── technology/             Shared technology descriptors (Docker, Spring, Kubernetes, Eureka, MAP)
├── bc01-identity-access/   .data  .services  .mapping  .operation
├── bc04-payment/           .data  .services  .mapping  .operation
├── bc05-rating/            .data  .services  .mapping  .operation
└── eureka-server/          .operation
```

**File types:**

- `.data` -- domain types in strict DDD notation. Value object fields are `immutable`. Aggregates contain at least one `<part>`. Repositories and application services have operations only.
- `.services` -- typed service interfaces with named parameters and return types.
- `.mapping` -- technology mapping. Binds operations and types to Spring annotations (`@PostMapping`, `@RequestBody`, `@PathVariable`, `@Table`, etc.) and MAP decorator aspects.
- `.operation` -- deployment specification: container definition, Dockerfile content, exposed port, and basic endpoints per protocol (REST, SOAP, AMQP).

**Opening in Eclipse:**

1. File -> New -> Project -> General -> Project
2. Uncheck "Use default location" and browse to `TheWinx-SS2026/lemma/`
3. Finish. Eclipse validates all files automatically.

Two warnings will always appear and cannot be fixed from the model files: "Node is not used" (the Eureka node is referenced cross-file, which LEMMA's validator does not track) and "Project has no explicit encoding set" (fix via Project -> Properties -> Resource -> UTF-8).

---

## Assignment Coverage

### Assignment 02 -- Requirements

We identified three actors and fifteen functional requirements:

**Actors:** User (Customer), Provider, System.

| ID | Requirement | Implemented in |
|---|---|---|
| R01 | User Registration | BC-01 |
| R02 | User Login | BC-01 |
| R03 | Search Vehicles by Location | BC-03 |
| R04 | Filter Vehicles (type, price, restrictions) | BC-03 |
| R05 | Book Vehicle | BC-03 |
| R06 | End Booking and compute cost | BC-03 |
| R07 | Process Payment | BC-04 |
| R08 | Rate Vehicle and Provider | BC-05 |
| R09 | View Booking History | BC-03 |
| R10 | Provider Registration | BC-01 |
| R11 | Provider Login | BC-01 |
| R12 | Manage Vehicles (CRUD) | BC-02 |
| R13 | Define Pricing Model (PER_HOUR / PER_KILOMETER) | BC-02 + BC-03 |
| R14 | Define Vehicle Restrictions (duration, km, age, persons) | BC-02 + BC-03 |
| R15 | View Fleet Status (GPS + availability per vehicle) | BC-02 |

### Assignment 03 -- Bounded Contexts and Context Map

We defined five bounded contexts, each with its own ubiquitous language and clear responsibility boundary. The context map was drawn using Vernon's notation. Identity & Access acts as the global upstream; Fleet Management sits upstream of Booking and Rating; Booking is the operational hub supplying both Payment and Rating downstream.

OHS/PL -> ACL is used wherever the upstream is a stable platform service and the downstream needs to protect its own model from upstream changes. C/S -> CF is used between Booking and its two consumers because both had design input and the interface was clean enough to accept without translation.

### Assignment 04 -- Tactical Design and Event Storming

**Tactical design:** each team member produced a UML class diagram for their bounded context, identifying aggregates, value objects, domain services, repositories, and domain events. The design decisions are reflected in the implementation as described per context above.

**Event Storming (5 phases):**

1. **Registration** -- UserRegistered, ProviderRegistered, UserAuthenticated
2. **Vehicle Management** -- VehicleCreated, VehicleUpdated, VehicleDeleted
3. **Search and Booking** -- VehiclesFound (read model), BookingCreated, VehicleStatusUpdated (-> BOOKED)
4. **Ride and Completion** -- BookingCompleted, CostComputed, PaymentSucceeded, VehicleStatusUpdated (-> AVAILABLE)
5. **Rating** -- policy fires only after BookingCompleted and PaymentSucceeded -> RatingSubmitted

### Assignment 05 -- Implementation

**Task 1 (standalone microservices):** each bounded context is a fully runnable Spring Boot 3.5 application with a REST API, an H2 in-memory database seeded with sample data, a Thymeleaf UI, Swagger/OpenAPI documentation, and Resilience4j circuit breakers. Cross-service dependencies are mocked when the target is not running.

**Task 2 (integration):** we wired all services together using three patterns:

1. **Eureka (service discovery)** -- services register by name; Feign clients resolve names at runtime without hardcoded URLs.
2. **Spring Cloud Config (centralized configuration)** -- all environment-specific settings are served from `config/` at startup. The `optional:` import ensures standalone mode still works.
3. **Resilience4j (circuit breakers)** -- every cross-service Feign call is wrapped with a named circuit breaker that has a fallback returning a safe default, keeping the calling service functional when a dependency is down.

We additionally implemented a Spring Cloud Gateway as a single unified entry point with path-based routing.

### Assignment 06 -- LEMMA Modeling

We completed all three modeling tasks:

**Task 1 -- Installation:** LEMMA was installed in Eclipse and the example models were imported and validated without errors.

**Task 2a -- Domain data modeling:** domain types from Assignment 04 were transcribed into `.data` files following LEMMA's DDD rules. JPA annotations are expressed as technology aspects in the `.mapping` files.

**Task 2b -- Microservice modeling:** service interfaces are defined in `.services` files. Technology mapping in `.mapping` files assigns Spring Web annotations to each operation and MAP decorator aspects to DTOs.

**Task 3 -- Deployment modeling:** `.operation` files specify the container technology, Dockerfile content, exposed port, and protocol endpoints for each modeled service. BC-01 and BC-05 use Docker; BC-04 uses Kubernetes with docker-compose configuration. The Eureka server has its own `.operation` file as an infrastructure node.

All models are committed to the `lemma/` folder at the repository root and validate in Eclipse with no errors.
