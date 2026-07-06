# BC-04: Payment

**Owner:** Marianne | **Port:** 8084

---

## What this service does

BC-04 receives a payment trigger from BC-03 when a ride ends and records the outcome. It supports four payment methods (CARD, PAYPAL, BANK_TRANSFER, CASH) and four statuses (PENDING → PAID / FAILED / REFUNDED). Cancelling a booking refunds the payment by transitioning it to REFUNDED.

Two design decisions:
- Payments always succeed in the current implementation. A failure is only possible if BC-04 itself is unreachable (circuit breaker in BC-03 catches that). This avoids random failures interrupting the demo flow.
- Status transitions are strictly one-directional: `PENDING → PAID`, `PENDING → FAILED`, `PAID → REFUNDED`. No other transitions are allowed.

---

## How it fits in the system

```
BC-03 Booking ──(ride ends / booking cancelled)──▶ BC-04 Payment
```

BC-04 is a **leaf context** - it consumes from Booking and nothing in the platform depends on it downstream.

---

## Project structure

```
bc04-payment/
└── src/main/java/com/winx/payment/
    │
    ├── PaymentApplication.java             Entry point
    │
    ├── model/
    │   ├── Payment.java                    JPA entity - aggregate root
    │   │                                   Fields: paymentId, bookingId, userId, amount,
    │   │                                   currency, paymentMethod, status, paidAt,
    │   │                                   failureReason, createdAt
    │   │                                   Methods: markAsPaid(), markAsFailed(reason),
    │   │                                            markAsRefunded()
    │   ├── PaymentMethod.java              Enum: CARD, PAYPAL, BANK_TRANSFER, CASH
    │   └── PaymentStatus.java              Enum: PENDING, PAID, FAILED, REFUNDED
    │
    ├── repository/
    │   └── PaymentRepository.java          Spring Data JPA - findByBookingId, findByUserId
    │
    ├── service/
    │   └── PaymentService.java             createAndProcessPayment, getAllPayments,
    │                                       getPaymentById, getPaymentByBookingId,
    │                                       getPaymentsByUserId, cancelPayment
    │
    ├── dto/
    │   └── PaymentRequest.java             Input: bookingId, userId, amount, currency,
    │                                       paymentMethod
    │
    └── controller/
        ├── PaymentRestController.java      REST API - full CRUD + cancel endpoint
        └── PaymentViewController.java      Thymeleaf - payment list + create form
```

```
src/main/resources/
├── application.yml    Port 8084, H2 (jdbc:h2:mem:payment), Eureka, Config Server
└── templates/
    ├── payments.html       All payments - filterable by userId, friendly method names,
    │                       colour-coded status badges (green=PAID, orange=REFUNDED, red=FAILED)
    └── create-payment.html Manual payment form (for demo/testing purposes)
```

---

## Prerequisites

Java 21. Verify with `java -version`.

---

## Run

```bash
# Standalone - no other services needed
./mvnw -pl bc04-payment spring-boot:run
```

---

## UI pages

| URL | Description |
|-----|-------------|
| `http://localhost:8084/payments` | All payments - filter by User ID |
| `http://localhost:8084/payments/new` | Create a payment manually (for demo purposes) |

---

## REST API

Full docs: `http://localhost:8084/swagger-ui/index.html`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/payments` | Create and process a payment |
| `GET` | `/api/payments` | List all payments |
| `GET` | `/api/payments/{id}` | Get payment by ID |
| `GET` | `/api/payments/booking/{bookingId}` | Get payment for a booking |
| `GET` | `/api/payments/user/{userId}` | All payments for a user |
| `POST` | `/api/payments/booking/{bookingId}/cancel` | Cancel (refund) a payment |

**Create a payment (as triggered by BC-03):**
```bash
curl -X POST http://localhost:8084/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId": 1,
    "userId": 4,
    "amount": 6.40,
    "currency": "EUR",
    "paymentMethod": "CARD"
  }'
```

**Refund when a booking is cancelled:**
```bash
curl -X POST http://localhost:8084/api/payments/booking/1/cancel
```

**Check all payments for a user:**
```bash
curl http://localhost:8084/api/payments/user/4
```

---

## Payment methods

| Value | Display name |
|-------|-------------|
| `CARD` | Credit / Debit Card |
| `PAYPAL` | PayPal |
| `BANK_TRANSFER` | Bank Transfer |
| `CASH` | Cash |

These four values are consistent with BC-03's search form and the Booking domain.

---

## Other endpoints

| URL | What |
|-----|------|
| `http://localhost:8084/swagger-ui/index.html` | Interactive API docs |
| `http://localhost:8084/h2-console` | H2 browser - JDBC URL: `jdbc:h2:mem:payment`, user: `sa` |
| `http://localhost:8084/actuator/health` | Health check |

---

## Assignment coverage

| Assignment | What BC-04 contributes |
|---|---|
| A02 - Requirements | R07 Process Payment |
| A03 - Context Map | Leaf downstream of BC-03 (Customer/Supplier → Conformist) |
| A04 - Tactical Design | Aggregate: `Payment`; Value objects: `Money`, `PaymentMethod`, `PaymentResult`; Domain events: `PaymentInitiated`, `PaymentSucceeded`, `PaymentFailed` |
| A05 Task 1 - Standalone | Runs independently on port 8084; manual payment creation via UI or Swagger |
| A05 Task 2 - Integration | Called by BC-03 via Feign after each completed ride; cancel triggered when booking is cancelled |
| A06 - LEMMA | `paymentCore.data`, `.services`, `.mapping`, `.operation` in `lemma/bc04-payment/` |
