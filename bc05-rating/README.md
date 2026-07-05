# BC-05 Rating

**Owner:** Mae | **Port:** 8085

---

## What this service does

BC-05 Rating is the bounded context responsible for collecting user feedback after a completed ride. Once a booking reaches `COMPLETED` status, the user can submit a rating that evaluates both the vehicle and the provider independently, each scored from 1 to 5, with an optional comment.

The service enforces two core rules:
- A booking can only be rated **once**. Attempting to submit a second rating for the same booking returns a 409 error.
- A rating can only be submitted for a **completed** booking. The service calls BC-03 Booking via a Feign client to verify this. If BC-03 is unreachable, a Resilience4j circuit breaker opens and the fallback allows the rating through so users are never blocked by a downstream outage.

There is also a Feign client for BC-02 Fleet Management, used to enrich vehicle display information.

---

## How it fits in the system

```
BC-03 Booking  ──(verify COMPLETED)──▶  BC-05 Rating  ◀──(vehicle info)──  BC-02 Fleet
```

BC-05 is a **leaf context** — it consumes from others but no other service depends on it.

---

## Project structure

```
bc05-rating/
├── src/main/java/com/winx/rating/
│   ├── RatingApplication.java              Entry point (@EnableFeignClients)
│   │
│   ├── domain/                             Core business logic
│   │   ├── Score.java                      Value object — integer 1–5, rejects invalid values
│   │   ├── Review.java                     Value object — holds vehicleScore + providerScore + comment
│   │   ├── RatingTarget.java               Value object — links a rating to vehicleId, providerId, bookingId
│   │   └── Rating.java                     Aggregate root and JPA entity
│   │
│   ├── infrastructure/
│   │   ├── RatingRepository.java           Spring Data JPA — queries by vehicle, provider, booking
│   │   └── client/                         Inter-service communication
│   │       ├── BookingFeignClient.java      Calls GET /bookings/{id} on BC-03
│   │       ├── BookingFeignFallback.java    Returns COMPLETED when BC-03 is unreachable
│   │       ├── FleetFeignClient.java        Calls GET /vehicles/{id} on BC-02
│   │       ├── FleetFeignFallback.java      Returns placeholder when BC-02 is unreachable
│   │       └── dto/
│   │           ├── BookingStatusResponse.java
│   │           └── VehicleResponse.java
│   │
│   ├── application/
│   │   ├── RatingSubmissionService.java     Handles submit logic — checks duplicate + COMPLETED status
│   │   └── RatingQueryService.java          Reads ratings, computes average scores
│   │
│   ├── api/
│   │   ├── RatingController.java           REST API (7 endpoints)
│   │   ├── GlobalExceptionHandler.java     Maps exceptions to HTTP status codes
│   │   ├── dto/
│   │   │   ├── SubmitRatingRequest.java     Input record for POST /api/ratings
│   │   │   └── RatingResponse.java          Output record for all responses
│   │   └── ui/
│   │       ├── RatingForm.java              Mutable form bean for Thymeleaf binding
│   │       └── RatingUiController.java      Serves HTML pages
│   │
│   └── config/
│       └── OpenApiConfig.java              Swagger UI setup
│
└── src/main/resources/
    ├── application.yml                      Port, H2, Eureka, Config Server, Feign, Resilience4j
    ├── data.sql                             5 sample ratings seeded on startup
    ├── static/styles.css                   Shared design system (consistent with BC-01)
    └── templates/ratings/
        ├── list.html                        All ratings, filterable by vehicle or provider
        ├── submit.html                      Form to submit a new rating
        └── detail.html                      Single rating detail view
```

---

## Prerequisites

Requires **Java 21**. If `java -version` doesn't show 21, run:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
```

To make it permanent, add those two lines to your `~/.zshrc`.

---

## Build

From the repo root (`TheWinx-SS2026/`):

```bash
./mvnw -pl bc05-rating -DskipTests clean package
```

Expected output: `BUILD SUCCESS`

---

## Run

### Standalone (no other services needed)

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
./mvnw -pl bc05-rating spring-boot:run
```

Warnings about Eureka and Config Server in the log are normal when running standalone — the service works fine without them. The Feign fallbacks will activate automatically since BC-03 and BC-02 are not running.

### Full system (Task 2)

Start services in this order, each in its own terminal:

```bash
./mvnw -pl infra-eureka-server   spring-boot:run
./mvnw -pl infra-config-server   spring-boot:run
./mvnw -pl bc01-identity-access  spring-boot:run
./mvnw -pl bc02-fleet-management spring-boot:run
./mvnw -pl bc03-booking          spring-boot:run
./mvnw -pl bc04-payment          spring-boot:run
./mvnw -pl bc05-rating           spring-boot:run
```

---

## UI Pages

| URL | Description |
|-----|-------------|
| `http://localhost:8085/ratings` | All ratings — table view with color-coded scores |
| `http://localhost:8085/ratings/submit` | Form to submit a new rating |
| `http://localhost:8085/ratings/{id}` | Detail view of a single rating |
| `http://localhost:8085/ratings/vehicle/{vehicleId}` | All ratings for a vehicle + average score |
| `http://localhost:8085/ratings/provider/{providerId}` | All ratings for a provider |

The UI uses the same design system as BC-01 (Outfit/Space Grotesk fonts, shared color palette, panel layout).

---

## REST API

Full interactive docs: `http://localhost:8085/swagger-ui.html`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/ratings` | Submit a rating |
| `GET` | `/api/ratings` | List all ratings |
| `GET` | `/api/ratings/{id}` | Get one rating by ID |
| `GET` | `/api/ratings/booking/{bookingId}` | Get rating for a specific booking |
| `GET` | `/api/ratings/vehicle/{vehicleId}` | All ratings for a vehicle |
| `GET` | `/api/ratings/vehicle/{vehicleId}/average` | Average vehicle score |
| `GET` | `/api/ratings/provider/{providerId}` | All ratings for a provider |

**Submit a rating:**
```bash
curl -X POST http://localhost:8085/api/ratings \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId": 2001,
    "userId": 5,
    "vehicleId": 20,
    "providerId": 200,
    "vehicleScore": 4,
    "providerScore": 5,
    "comment": "Smooth ride, very clean vehicle!"
  }'
```

**Business rule checks:**
```bash
# Duplicate booking → 409 Conflict
curl -X POST http://localhost:8085/api/ratings \
  -H "Content-Type: application/json" \
  -d '{"bookingId":1001,"userId":1,"vehicleId":10,"providerId":100,"vehicleScore":4,"providerScore":4}'

# Score out of range → 400 Bad Request
curl -X POST http://localhost:8085/api/ratings \
  -H "Content-Type: application/json" \
  -d '{"bookingId":9999,"userId":1,"vehicleId":10,"providerId":100,"vehicleScore":9,"providerScore":3}'
```

---

## Other endpoints

**Health check:**
```
GET http://localhost:8085/actuator/health
```

**H2 database console:** `http://localhost:8085/h2-console`
- JDBC URL: `jdbc:h2:mem:rating`
- Username: `sa` | Password: *(leave empty)*
- Run `SELECT * FROM RATINGS;` to inspect stored data

**Circuit breaker status:**
```
GET http://localhost:8085/actuator/circuitbreakers
```

**Eureka dashboard** (full system only): `http://localhost:8761`
BC-05 should appear as `BC05-RATING` once registered.
