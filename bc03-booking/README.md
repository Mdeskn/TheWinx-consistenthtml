# BC-03: Booking

**Owner:** Rowena | **Port:** 8083

---

## What this service does

BC-03 is the operational hub of the platform. It coordinates the full ride lifecycle: vehicle search → booking creation → ride completion → payment trigger. It calls BC-01 to verify the user, BC-02 to check vehicle availability and update status, and BC-04 to process payment after the ride ends.

Three key business rules:
- Only one active booking per user at a time. A second `createBooking` while one is `ACTIVE` returns 409.
- The vehicle price is captured in a `VehicleSnapshot` at booking time. Provider price changes during an active ride have no effect on the final cost.
- The user must be authenticated (via the `winx-username` cookie set by BC-01). Unauthenticated attempts are rejected before any downstream call is made.

---

## How it fits in the system

```
BC-01 Identity ──(validate username)──▶
BC-02 Fleet ────(search / set BOOKED / set AVAILABLE)──▶  BC-03 Booking ──▶ BC-04 Payment
                                                                    │
                                                           consumed by BC-05 (verify COMPLETED)
```

BC-03 is the central operational context - it is both a downstream consumer (of BC-01 and BC-02) and an upstream for BC-04 and BC-05.

---

## Project structure

```
bc03-booking/
└── src/main/java/com/winx/booking/
    │
    ├── BookingApplication.java                Entry point (@EnableFeignClients)
    │
    ├── domain/
    │   ├── Booking.java                       JPA entity - aggregate root
    │   │                                      Fields: bookingId, userId, vehicleSnapshot,
    │   │                                      interval, startLocation, endLocation, summary,
    │   │                                      paymentMethod, status
    │   ├── BookingStatus.java                 Enum: ACTIVE, COMPLETED, CANCELLED
    │   ├── event/
    │   │   └── BookingCompleted.java          Domain event fired when a ride ends
    │   └── vo/
    │       ├── VehicleSnapshot.java           Value object - vehicleId, providerId, type,
    │       │                                  pricePerUnit, billingModel (frozen at booking time)
    │       ├── TimeInterval.java              Value object - startedAt, endedAt (ISO-8601)
    │       ├── RideLocation.java              Value object - latitude, longitude
    │       └── RideSummary.java               Value object - distanceKm, totalCost (set on end)
    │
    ├── application/
    │   ├── BookingService.java                Core orchestrator: createBooking, cancelBooking,
    │   │                                      endBooking, findById, findByUser, searchVehicles
    │   ├── RestrictionValidator.java          Checks minAge / maxPersons before confirming booking
    │   ├── PaymentEventListener.java          Listens for BookingCompleted → triggers BC-04
    │   └── pricing/
    │       ├── CostCalculationService.java    Selects strategy by BillingModel
    │       ├── PricingStrategy.java           Interface: calculateCost(price, duration, distance)
    │       ├── HourlyPricing.java             price × hours (for PER_HOUR vehicles)
    │       └── PerKilometerPricing.java       price × km (for PER_KILOMETER vehicles)
    │
    ├── api/
    │   ├── BookingController.java             REST API - booking lifecycle + query endpoints
    │   ├── GlobalExceptionHandler.java        Maps domain exceptions to HTTP 404/409/503
    │   ├── ErrorResponse.java                 Standard error body
    │   └── dto/
    │       ├── BookingCreateRequest.java      Input: vehicleId, startLatitude, startLongitude, paymentMethod
    │       ├── EndRideRequest.java            Input: endLatitude, endLongitude, distanceKm
    │       ├── BookingDto.java                Output: full booking details including cost
    │       ├── BookingStatusDto.java          Lightweight status response for BC-05 to read
    │       ├── VehicleDto.java                Translated vehicle from BC-02 (ACL)
    │       ├── PrincipalDto.java              Translated user identity from BC-01 (ACL)
    │       ├── AuthorizeRequest.java          Username wrapper for identity calls
    │       └── StatusUpdate.java             Status enum wrapper sent to BC-02
    │
    ├── api/ui/
    │   ├── BookingWebController.java          Thymeleaf controller (search, create, end, cancel)
    │   │                                      Reads winx-username cookie via @CookieValue
    │   └── DemoLocations.java                 5 preset Dortmund coordinates for the location dropdown
    │
    ├── infrastructure/
    │   ├── persistence/BookingRepository.java Spring Data JPA
    │   └── client/
    │       ├── IdentityClient.java            Feign → BC-01 /api/auth/validate
    │       ├── IdentityGateway.java           Port interface (dependency inversion)
    │       ├── RealIdentityGateway.java       Calls Feign client; fallback if BC-01 is down
    │       ├── FleetClient.java               Feign → BC-02 /api/v1/vehicles/search + PATCH status
    │       ├── FleetGateway.java              Port interface
    │       ├── RealFleetGateway.java          Calls Feign client; fallback if BC-02 is down
    │       ├── PaymentClient.java             Feign → BC-04 /api/payments
    │       ├── PaymentGateway.java            Port interface
    │       ├── RealPaymentGateway.java        Calls Feign client; fallback if BC-04 is down
    │       └── mock/
    │           ├── MockIdentityGateway.java   Used in standalone mode (returns "testuser")
    │           ├── MockFleetGateway.java      Returns 3 dummy vehicles in standalone mode
    │           └── MockPaymentGateway.java    Accepts payment without calling BC-04
    │
    ├── exception/
    │   ├── DomainException.java               Base for all domain exceptions
    │   ├── BookingNotFoundException.java
    │   ├── VehicleNotAvailableException.java
    │   ├── ActiveBookingExistsException.java
    │   ├── InvalidBookingStateException.java
    │   ├── RestrictionViolationException.java
    │   └── DependencyUnavailableException.java
    │
    └── config/
        └── OpenApiConfig.java                 Swagger UI setup
```

```
src/main/resources/
├── application.yml     Port 8083, H2 (jdbc:h2:mem:booking), Eureka, Config Server, Feign
└── templates/
    ├── search.html       Vehicle search form + results table
    │                     Shows login status (reads loggedInUser from cookie)
    │                     Book button disabled if not logged in or vehicle not AVAILABLE
    ├── bookings.html     Booking history table, filterable by userId
    └── booking-detail.html  Single booking: status, cost, End Ride / Cancel buttons
```

---

## Demo locations (location dropdown)

| Name | Latitude | Longitude |
|------|----------|-----------|
| Dortmund City Centre | 51.5178 | 7.4590 |
| Dortmund Hauptbahnhof | 51.5167 | 7.4591 |
| Bochum City Centre | 51.4818 | 7.2162 |
| Essen City Centre | 51.4556 | 7.0116 |
| Münster City Centre | 51.9607 | 7.6261 |

---

## Prerequisites

Java 21. Verify with `java -version`.

The service works standalone. Mocks activate for BC-01/BC-02/BC-04 when they are unreachable.

---

## End-to-end flow to test

1. Log in at BC-01 (`http://localhost:8081/ui/login`) - sets the `winx-username` cookie
2. Open `http://localhost:8083/ui/search` - the green banner shows your username
3. Pick a location, select a payment method, click Search
4. Click **Book** on an AVAILABLE vehicle
5. Open the booking detail and click **End Ride**
6. Check BC-04 (`http://localhost:8084/payments`) - payment appears with status PAID
7. Submit a rating at BC-05 (`http://localhost:8085/ratings/submit`)

---

## REST API

Full docs: `http://localhost:8083/swagger-ui/index.html`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/bookings` | Create a booking (`?token=username`) |
| `GET` | `/api/v1/bookings/{id}` | Get booking by ID |
| `GET` | `/api/v1/bookings/user/{userId}` | All bookings for a user |
| `POST` | `/api/v1/bookings/{id}/cancel` | Cancel an active booking |
| `POST` | `/api/v1/bookings/{id}/end` | End a ride (triggers payment) |
| `GET` | `/api/v1/vehicles/search` | Search available vehicles (proxied from BC-02) |

**Create a booking:**
```bash
curl -X POST "http://localhost:8083/api/v1/bookings?token=user" \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleId": 1,
    "startLatitude": 51.5178,
    "startLongitude": 7.4590,
    "paymentMethod": "CARD"
  }'
```

**End a ride:**
```bash
curl -X POST http://localhost:8083/api/v1/bookings/1/end \
  -H "Content-Type: application/json" \
  -d '{"endLatitude":51.525,"endLongitude":7.463,"distanceKm":3.2}'
```

---

## Other endpoints

| URL | What |
|-----|------|
| `http://localhost:8083/swagger-ui/index.html` | Interactive API docs |
| `http://localhost:8083/h2-console` | H2 browser - JDBC URL: `jdbc:h2:mem:booking`, user: `sa` |
| `http://localhost:8083/actuator/health` | Health check |

---

## Payment methods supported

`CARD` · `PAYPAL` · `BANK_TRANSFER` · `CASH` - consistent with BC-04.

---

## Assignment coverage

| Assignment | What BC-03 contributes |
|---|---|
| A02 - Requirements | R03 Search Vehicles, R04 Filter Vehicles, R05 Book Vehicle, R06 End Booking + compute cost, R09 View Booking History |
| A03 - Context Map | Downstream (ACL) from BC-01 and BC-02; upstream (C/S → CF) for BC-04 and BC-05 |
| A04 - Tactical Design | Aggregate: `Booking`; Value objects: `VehicleSnapshot`, `TimeInterval`, `RideLocation`, `RideSummary`; Strategy pattern: `CostCalculationService`; Domain event: `BookingCompleted` |
| A05 Task 1 - Standalone | Runs independently with mock gateways; all ride lifecycle endpoints work without other services |
| A05 Task 2 - Integration | Feign clients to BC-01/BC-02/BC-04 with circuit breakers; cookie-based auth from BC-01; status updates to BC-02 |
| A06 - LEMMA | `booking.data`, `.services`, `.mapping`, `.operation` in `lemma/bc03-booking/` |
