# BC-05: Rating

**Owner:** Mae | **Port:** 8085

---

## What this service does

BC-05 lets users rate a completed ride. Each rating scores the vehicle and the provider independently on a 1-5 scale, with an optional comment. Clicking a vehicle ID anywhere in the rating UI opens a full vehicle detail page pulled live from BC-02, so reviewers can see exactly what they are rating.

Three business rules enforced:
- A booking can only be rated once. A duplicate attempt returns 409 Conflict.
- Only bookings in `COMPLETED` status are eligible. BC-03 is called to verify this. If BC-03 is unreachable, the circuit breaker fallback returns COMPLETED so users are never blocked by a downstream outage.
- Vehicle and provider scores must each be between 1 and 5. Out-of-range values return 400 Bad Request.

---

## How it fits in the system

```
BC-03 Booking  -(verify COMPLETED)->  BC-05 Rating  <-(vehicle detail)-  BC-02 Fleet
```

BC-05 is a leaf context - it consumes from others but nothing in the platform depends on it downstream.

---

## Project structure

```
bc05-rating/
└── src/main/java/com/winx/rating/
    │
    ├── RatingApplication.java              Entry point (@EnableFeignClients)
    │
    ├── domain/
    │   ├── Score.java                      Value object - integer 1-5, rejects out-of-range at construction
    │   ├── Review.java                     Value object - vehicleScore + providerScore + comment (optional)
    │   ├── RatingTarget.java               Value object - vehicleId, providerId, bookingId (unique per booking)
    │   └── Rating.java                     Aggregate root + JPA entity (immutable once persisted)
    │
    ├── infrastructure/
    │   ├── RatingRepository.java           Spring Data JPA
    │   │                                   findByTarget_VehicleId, findByTarget_ProviderId,
    │   │                                   findByTarget_BookingId, existsByBookingId
    │   └── client/
    │       ├── BookingFeignClient.java      Feign -> BC-03 GET /api/v1/bookings/{id}
    │       ├── BookingFeignFallback.java    Returns status=COMPLETED when BC-03 is down
    │       ├── FleetFeignClient.java        Feign -> BC-02 GET /api/v1/vehicles/{id}
    │       ├── FleetFeignFallback.java      Returns placeholder vehicle when BC-02 is down
    │       └── dto/
    │           ├── BookingStatusResponse.java   bookingId + status string
    │           └── VehicleResponse.java         All 10 vehicle fields from BC-02
    │
    ├── application/
    │   ├── RatingSubmissionService.java    Validates duplicate + booking status, then persists
    │   └── RatingQueryService.java         Reads ratings + computes getAverageVehicleScore,
    │                                       getAverageProviderScore
    │
    ├── api/
    │   ├── RatingController.java           REST API (8 endpoints, see table below)
    │   ├── GlobalExceptionHandler.java     Maps domain exceptions to HTTP 400/404/409
    │   ├── dto/
    │   │   ├── SubmitRatingRequest.java     Input: bookingId, userId, vehicleId, providerId,
    │   │   │                               vehicleScore, providerScore, comment
    │   │   └── RatingResponse.java          Output: all rating fields + createdAt
    │   └── ui/
    │       ├── RatingForm.java              Mutable form bean for Thymeleaf POST binding
    │       └── RatingUiController.java      Serves all HTML pages; injects FleetFeignClient
    │                                        to enrich vehicle detail page
    │
    └── config/
        └── OpenApiConfig.java              Swagger UI title + description
```

```
src/main/resources/
├── application.yml        Port 8085, H2 (jdbc:h2:mem:rating), Eureka, Config Server,
│                          Feign timeouts, Resilience4j circuit breakers
├── data.sql               8 seeded ratings referencing real BC-02 vehicle IDs (1-8),
│                          real BC-01 provider IDs (1-3), booking IDs 9001-9008
│                          (high range to avoid conflicts with live demo bookings)
└── templates/ratings/
    ├── list.html           All ratings table - colour-coded scores, vehicle ID links
    ├── submit.html         Form to submit a new rating (pre-fills vehicleId/providerId
    │                       from query params if coming from a booking)
    ├── detail.html         Single rating detail view
    └── vehicle.html        Vehicle detail page: fetches live data from BC-02,
                            shows vehicle specs + average score + all ratings for that vehicle
```

---

## Seeded ratings (data.sql)

| Booking ID | Vehicle ID | Provider | Vehicle score | Provider score | Vehicle |
|------------|------------|----------|---------------|----------------|---------|
| 9001 | 1 | 1 | 5 | 4 | Tier e-scooter |
| 9002 | 2 | 1 | 4 | 5 | Tier bicycle |
| 9003 | 4 | 2 | 5 | 5 | ShareNow e-car |
| 9004 | 5 | 2 | 3 | 4 | Voi e-scooter |
| 9005 | 7 | 3 | 5 | 5 | Bolt e-bike |
| 9006 | 8 | 3 | 4 | 3 | Sixt e-car |
| 9007 | 3 | 1 | 2 | 4 | Lime e-bike |
| 9008 | 6 | 2 | 5 | 5 | Swapfiets bicycle |

Vehicle IDs 1-8 match BC-02's seeded vehicles exactly.

---

## Prerequisites

Java 21. Verify with `java -version`. If needed:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
```

---

## Run

```bash
# Standalone - no other services needed
./mvnw -pl bc05-rating spring-boot:run
```

Eureka and Config Server warnings in the log are expected in standalone mode. Feign fallbacks activate automatically for BC-02 and BC-03.

```bash
# Full system - start in order
./mvnw -pl infra-eureka-server   spring-boot:run
./mvnw -pl infra-config-server   spring-boot:run
./mvnw -pl bc02-fleet-management spring-boot:run
./mvnw -pl bc03-booking          spring-boot:run
./mvnw -pl bc05-rating           spring-boot:run
```

---

## UI pages

| URL | Description |
|-----|-------------|
| `http://localhost:8085/ratings` | All ratings - table with colour-coded scores, click vehicle ID to see details |
| `http://localhost:8085/ratings/submit` | Submit a new rating |
| `http://localhost:8085/ratings/{id}` | Single rating detail |
| `http://localhost:8085/ratings/vehicle/{vehicleId}` | Vehicle detail page: specs from BC-02 + average score + all ratings |
| `http://localhost:8085/ratings/provider/{providerId}` | All ratings for a provider + average provider score |

---

## REST API

Full docs: `http://localhost:8085/swagger-ui/index.html`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/ratings` | Submit a rating |
| `GET` | `/api/ratings` | List all ratings |
| `GET` | `/api/ratings/{id}` | Get one rating by ID |
| `GET` | `/api/ratings/booking/{bookingId}` | Get rating for a specific booking |
| `GET` | `/api/ratings/vehicle/{vehicleId}` | All ratings for a vehicle |
| `GET` | `/api/ratings/vehicle/{vehicleId}/average` | Average vehicle score |
| `GET` | `/api/ratings/provider/{providerId}` | All ratings for a provider |
| `GET` | `/api/ratings/provider/{providerId}/average` | Average provider score |

**Submit a rating:**
```bash
curl -X POST http://localhost:8085/api/ratings \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId": 42,
    "userId": 4,
    "vehicleId": 1,
    "providerId": 1,
    "vehicleScore": 5,
    "providerScore": 4,
    "comment": "Smooth ride, battery lasted the whole trip!"
  }'
```

**Average provider score:**
```bash
curl http://localhost:8085/api/ratings/provider/1/average
# {"averageProviderScore": 4.33}
```

**Business rule checks:**
```bash
# Duplicate booking - 409 Conflict
curl -X POST http://localhost:8085/api/ratings \
  -H "Content-Type: application/json" \
  -d '{"bookingId":9001,"userId":4,"vehicleId":1,"providerId":1,"vehicleScore":3,"providerScore":3}'

# Score out of range - 400 Bad Request
curl -X POST http://localhost:8085/api/ratings \
  -H "Content-Type: application/json" \
  -d '{"bookingId":9999,"userId":4,"vehicleId":1,"providerId":1,"vehicleScore":9,"providerScore":3}'
```

---

## Other endpoints

| URL | What |
|-----|------|
| `http://localhost:8085/swagger-ui/index.html` | Interactive API docs |
| `http://localhost:8085/h2-console` | H2 browser - JDBC URL: `jdbc:h2:mem:rating`, user: `sa` |
| `http://localhost:8085/actuator/health` | Health check |
| `http://localhost:8085/actuator/circuitbreakers` | Circuit breaker state (requires full system) |

---

## Assignment coverage

| Assignment | What BC-05 contributes |
|---|---|
| A02 - Requirements | R08 Rate Vehicle and Provider |
| A03 - Context Map | Leaf downstream of BC-03 (Customer/Supplier -> Conformist) and BC-02 (OHS/PL -> ACL) |
| A04 - Tactical Design | Aggregate: `Rating` (immutable); Value objects: `Score`, `Review`, `RatingTarget`; Domain event: `RatingSubmitted`; Business rules: no duplicate ratings, only COMPLETED bookings |
| A05 Task 1 - Standalone | Runs independently with 8 seeded ratings; full Swagger docs; fallbacks for BC-02/BC-03 |
| A05 Task 2 - Integration | Feign to BC-03 (verify COMPLETED) + BC-02 (vehicle detail page); Resilience4j circuit breakers |
| A06 - LEMMA | `ratingCore.data`, `.services`, `.mapping`, `.operation` in `lemma/bc05-rating/` |
