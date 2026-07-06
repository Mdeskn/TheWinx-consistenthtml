# BC-01: Identity & Access

**Owner:** Sama | **Port:** 8081

---

## What this service does

BC-01 is the global upstream for the entire platform. It manages user accounts, handles authentication, and exposes contract endpoints that other bounded contexts call to verify user eligibility without ever touching the identity database directly.

When a user logs in, BC-01 sets a `winx-username` cookie on `localhost`. Because all services run on the same host, this cookie is automatically present in every browser request to any port - BC-03 reads it to identify the booking user without asking them to type their name again.

Three business rules enforced here:
- Usernames and emails must be unique across all accounts.
- Passwords are stored hashed (BCrypt). Plain text is never persisted.
- An account moves through states: `PENDING → ACTIVE → DEACTIVATED`. Only `ACTIVE` accounts can log in.

---

## How it fits in the system

```
BC-01 Identity & Access
    │
    ├──▶ BC-02 Fleet  (proxies vehicle list to the dashboard)
    ├──▶ BC-03 Booking (proxies booking list; also called back by BC-03 to verify username)
    └──▶ BC-05 Rating  (proxies rating list to the dashboard)
```

BC-01 is the global upstream - every other context has an Anti-Corruption Layer (ACL) to translate its data.

---

## Project structure

```
bc01-identity-access/
└── src/main/java/com/thewinx/identityaccess/
    │
    ├── IdentityAccessServiceApplication.java   Entry point (@EnableFeignClients)
    │
    ├── domain/
    │   ├── UserAccount.java                    Aggregate root + JPA entity
    │   │                                       Fields: id, username, email, passwordHash,
    │   │                                       firstName, lastName, phoneNumber, dateOfBirth,
    │   │                                       status (PENDING/ACTIVE/DEACTIVATED), roles
    │   ├── AccountStatus.java                  Enum: PENDING, ACTIVE, DEACTIVATED
    │   └── Role.java                           JPA entity for role assignments
    │
    ├── application/
    │   ├── IdentityAccessService.java          Register, login, update, deactivate, role management
    │   ├── FleetService.java                   Calls BC-02 and BC-03 via Feign for the dashboard
    │   ├── DataSeeder.java                     Seeds 3 demo users on startup (user/admin/provider)
    │   ├── AuthenticationResult.java           Value object returned after login
    │   ├── DuplicateResourceException.java     Thrown when username or email already exists
    │   ├── NotFoundException.java              Thrown for unknown userId
    │   └── UnauthorizedException.java          Thrown for wrong password
    │
    ├── api/
    │   ├── IdentityAccessRestController.java   REST: /api/v1/identity/users + /auth/login
    │   ├── AuthValidationController.java       REST: GET /api/auth/validate?username= (called by BC-03)
    │   ├── BookingProxyController.java         REST: /api/v1/identity/fleet/bookings (proxied to BC-03)
    │   ├── VehicleDetailController.java        REST: /api/v1/identity/fleet/vehicles (proxied to BC-02)
    │   ├── GlobalExceptionHandler.java         Maps domain exceptions to HTTP 400/404/409
    │   ├── ApiErrorResponse.java               Standard error response body
    │   └── dto/
    │       ├── RegisterUserRequest.java        Input: username, email, password, firstName, lastName,
    │       │                                   phoneNumber, dateOfBirth
    │       ├── UpdateUserRequest.java          Input: username, email
    │       ├── AuthRequest.java                Input: username, password
    │       ├── AuthResponse.java               Output: userId, username, token
    │       ├── UserResponse.java               Output: full user details + roles
    │       ├── RoleAssignmentRequest.java      Input: roleName
    │       ├── PermissionCheckResponse.java    Output: userId, permission, hasPermission
    │       └── VehicleBookingRequest.java      Input: vehicleId, username, dates
    │
    ├── contracts/
    │   ├── IdentityContractController.java     REST: /api/contracts/v1 - eligibility checks
    │   ├── AdjacentContextMockService.java     Returns mock eligibility results for BC-02/03/04
    │   ├── BookingIdentityCheckResponse.java   Output DTO for BC-03 identity check
    │   ├── PaymentEligibilityResponse.java     Output DTO for BC-04 eligibility check
    │   └── ProviderAccessResponse.java         Output DTO for BC-02 provider access check
    │
    ├── infrastructure/
    │   ├── UserAccountRepository.java          Spring Data JPA - findByUsername, findByEmail
    │   ├── RoleRepository.java                 Spring Data JPA - findByName
    │   ├── fleet/
    │   │   ├── FleetClient.java                Feign client → BC-02 /api/v1/vehicles
    │   │   ├── FleetClientFallback.java        Returns empty list when BC-02 is unreachable
    │   │   └── dto/VehicleDto.java             Translates BC-02 response (ACL)
    │   ├── booking/
    │   │   ├── BookingClient.java              Feign client → BC-03 /api/v1/bookings
    │   │   └── dto/BookingDto.java             Translates BC-03 response (ACL)
    │   └── rating/
    │       ├── RatingClient.java               Feign client → BC-05 /api/ratings
    │       └── dto/RatingDto.java              Translates BC-05 response (ACL)
    │
    └── web/
        ├── IdentityUiController.java           Thymeleaf: login, register, logout (sets/clears cookie)
        └── FleetApiController.java             Thymeleaf: dashboard page with proxied fleet + bookings
```

```
src/main/resources/
├── application.yml        Port 8081, H2 (jdbc:h2:mem:identity), Eureka, Config Server, Resilience4j
└── templates/
    ├── index.html          Home / landing page with navigation
    ├── login.html          Two-column login form (sets winx-username cookie on success)
    ├── register.html       Two-column registration form (6 fields + password confirmation)
    ├── user-dashboard.html Logged-in view: vehicle list + booking history via BC-02 / BC-03
    └── edit.html           Edit account details form
```

---

## Seeded demo users

| Username | Password | Role |
|----------|----------|------|
| `user` | `password` | USER |
| `admin` | `password` | ADMIN |
| `provider` | `password` | PROVIDER |

---

## Prerequisites

Java 21. Verify with `java -version`. If needed:

```bash
# macOS
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
```

---

## Run

```bash
# Standalone - no other services needed
./mvnw -pl bc01-identity-access spring-boot:run
```

---

## UI pages

| URL | Description |
|-----|-------------|
| `http://localhost:8081/` | Home / navigation hub |
| `http://localhost:8081/ui/login` | Login - sets `winx-username` cookie |
| `http://localhost:8081/ui/register` | Register a new account |
| `http://localhost:8081/ui/dashboard` | User dashboard - proxied vehicles + bookings |
| `http://localhost:8081/ui/logout` | Clears the cookie and redirects to login |

---

## REST API

Full docs: `http://localhost:8081/swagger-ui/index.html`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/identity/users` | Register a new user |
| `GET` | `/api/v1/identity/users` | List all users |
| `GET` | `/api/v1/identity/users/{id}` | Get user by ID |
| `PUT` | `/api/v1/identity/users/{id}` | Update username / email |
| `DELETE` | `/api/v1/identity/users/{id}` | Deactivate account |
| `POST` | `/api/v1/identity/auth/login` | Authenticate (returns token) |
| `GET` | `/api/auth/validate?username=` | Validate username - called by BC-03 |
| `GET` | `/api/contracts/v1/booking/identity-check` | Contract for BC-03 |
| `GET` | `/api/contracts/v1/payment/eligibility` | Contract for BC-04 |
| `GET` | `/api/contracts/v1/provider/access` | Contract for BC-02 |

**Register a user:**
```bash
curl -X POST http://localhost:8081/api/v1/identity/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "email": "alice@example.com",
    "password": "secret123",
    "firstName": "Alice",
    "lastName": "Smith",
    "phoneNumber": "01711234567",
    "dateOfBirth": "1995-03-12"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8081/api/v1/identity/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"secret123"}'
```

**Validate username (called internally by BC-03):**
```bash
curl "http://localhost:8081/api/auth/validate?username=alice"
```

---

## Other endpoints

| URL | What |
|-----|------|
| `http://localhost:8081/swagger-ui/index.html` | Interactive API docs |
| `http://localhost:8081/h2-console` | H2 browser - JDBC URL: `jdbc:h2:mem:identity`, user: `sa` |
| `http://localhost:8081/actuator/health` | Health check |

---

## Assignment coverage

| Assignment | What BC-01 contributes |
|---|---|
| A02 - Requirements | R01 User Registration, R02 User Login, R10 Provider Registration, R11 Provider Login |
| A03 - Context Map | Global upstream; OHS/PL → ACL contracts consumed by BC-02, BC-03, BC-05 |
| A04 - Tactical Design | Aggregate: `UserAccount`; Value objects: `Email`, `Password`, `PersonalInfo`; Domain events: `UserRegistered`, `UserAuthenticated` |
| A05 Task 1 - Standalone | Runs independently on port 8081 with seeded users and full Swagger docs |
| A05 Task 2 - Integration | Feign clients to BC-02/BC-03/BC-05 with Resilience4j circuit breakers; cookie-based session shared with BC-03 |
| A06 - LEMMA | `IdentityAccess.data`, `.services`, `.mapping`, `.operation` in `lemma/bc01-identity-access/` |
