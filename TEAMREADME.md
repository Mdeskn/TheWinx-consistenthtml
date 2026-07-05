# Team Setup & Installation Guide

how to build and run the Instant Mobility microservices for
Lab Assignment 05. (Project overview is in `README.md`.)

This repository implements **Lab Assignment 05**. Task 1 = each bounded context as a standalone
Spring Boot + Spring MVC + REST microservice with a rudimentary CRUD UI. Task 2 = integration via
Eureka (discovery), Spring Cloud Config (central config) and Resilience4j (circuit breakers).

---

## 1. Prerequisites / Toolchain

- **Java 21** (compile target: Java 17). Point `JAVA_HOME` at a JDK 21.
  - macOS / Homebrew:
    ```bash
    brew install openjdk@21
    export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
    ```
  - Linux (SDKMAN): `sdk install java 21.0.9-tem && sdk use java 21.0.9-tem`
  - Windows: install a JDK 21 (e.g. Temurin) and set `JAVA_HOME` to its folder.
  - Verify: `java -version` → should print `21.x`.
- **Maven** — **not required globally.** Use the committed wrapper `./mvnw` (Windows: `mvnw.cmd`).
  The first run downloads Maven 3.9.9 automatically.

> Why JDK 21? Spring Boot 3.2.5 supports Java 17–21 only. Building on JDK 22+ (e.g. 25) will fail.

## 2. Get the code

```bash
git clone https://github.com/samaalzoubi/TheWinx-SS2026.git
cd TheWinx-SS2026
```

## 3. Build everything

```bash
./mvnw -DskipTests clean install
```

All 8 modules should report `BUILD SUCCESS`.

## 4. Module map (who owns what)

| Module | Port | Owner | Role | Feign + Circuit Breaker |
|---|---|---|---|---|
| `infra-eureka-server` | 8761 | shared | Service registry | — |
| `infra-config-server` | 8888 | shared | Central config (serves `config/`) | — |
| `infra-api-gateway` (optional) | 8080 | shared | Single entry point | — |
| `bc01-identity-access` | 8081 | Member A | Users, providers, auth | no |
| `bc02-fleet-management` | 8082 | Member B | Vehicles, search, status | no |
| `bc03-booking` | 8083 | Member C | Ride lifecycle | yes (identity, fleet, payment) |
| `bc04-payment` | 8084 | Member D | Payments | yes (external gateway stub) |
| `bc05-rating` | 8085 | Member E | Ratings | yes (booking, fleet) |

Base package per service is `com.winx.<bc>` — `identity`, `fleet`, `booking`, `payment`, `rating`.

## 5. Run a single service (Task 1, standalone)

Each microservice boots on its own — Eureka and Config Server are optional
(`spring.config.import=optional:configserver:` plus Eureka client retry make this safe):

```bash
./mvnw -pl bc02-fleet-management spring-boot:run
```

Per service, once running:
- Swagger UI:  `http://localhost:<port>/swagger-ui.html`
- H2 console:  `http://localhost:<port>/h2-console`  (JDBC URL = the one in that service's `application.yml`)
- Actuator:    `http://localhost:<port>/actuator/health`

## 6. Run the full system (Task 2)

Start infrastructure first, then the services in any order (each in its own terminal):

```bash
./mvnw -pl infra-eureka-server spring-boot:run    # 8761 — dashboard at http://localhost:8761
./mvnw -pl infra-config-server spring-boot:run    # 8888 — serves the repo-root config/ folder
./mvnw -pl bc01-identity-access spring-boot:run
./mvnw -pl bc02-fleet-management spring-boot:run
./mvnw -pl bc03-booking spring-boot:run
./mvnw -pl bc04-payment spring-boot:run
./mvnw -pl bc05-rating spring-boot:run
./mvnw -pl infra-api-gateway spring-boot:run      # 8080 — optional
```

## 7. Git workflow & branching

`main` holds the **shared skeleton baseline** (parent POM, Maven wrapper, infra modules, and all
five bootable `bcXX-*` skeletons). Everyone branches off this — do **not** re-create the skeleton on
your branch; it is already there so all 8 modules build from a consistent starting point.

**Branch model:** one feature branch per bounded context, named `feature/bcXX-name`
(e.g. `feature/bc01-identity-access`). Each member owns exactly one.

### Create your branch

```bash
git switch main
git pull                                       # get the latest baseline
git switch -c feature/bc03-booking             # your bounded context
git push -u origin feature/bc03-booking        # publish it
```

### Work on it

- Edit **only files inside your own `bcXX-*/` module** (and your own `config/bcXX-*.yml`).
- Commit in small steps; push regularly so others can see progress:
  ```bash
  git add bc03-booking
  git commit -m "bc03: add Booking aggregate and repository"
  git push
  ```
- Keep your branch fresh to avoid drift:
  ```bash
  git switch main && git pull
  git switch feature/bc03-booking && git merge main
  ```

### Merge back

Open a Pull Request `feature/bcXX-name → main` when your service passes
`./mvnw -pl bcXX-name -DskipTests package` and boots. Have one teammate review, then merge.

### Avoiding conflicts — shared files

| File | Rule |
|---|---|
| root `pom.xml` `<modules>` | Already complete — **don't edit it.** |
| `config/bcXX-*.yml` | Edit only your own. |
| `TEAMREADME.md`, `docs/` | Coordinate; prefer changing these on `main` only. |

Because each context lives in its own module folder and package (`com.winx.<bc>`), branches that
each stay inside their own module touch disjoint files and merge cleanly.

## 8. How to fill in your bounded context

Each `bcXX-*` module currently contains only the bootable skeleton (Spring Boot application class +
`application.yml` + dependencies). Implement your context under `com.winx.<bc>` using this layout:

```
com.winx.<bc>
├── api/            REST controllers, DTOs (Java records), GlobalExceptionHandler
│   └── ui/         @Controller for Thymeleaf pages
├── domain/         @Entity aggregates/entities, @Embeddable value objects, events
├── application/    @Service domain/application services
├── infrastructure/ JpaRepository interfaces, @FeignClient gateways
└── config/         Feign / OpenAPI / Resilience config
```

The authoritative domain specification (aggregates, value objects, invariants, services,
repositories) is `Labs/DMSA_Lab_Assignment_04.pdf`. The per-service endpoint sketch, Feign client
contracts and circuit-breaker setup are in `DMSA_Lab_05_Implementation_Plan.md` (laboratory root).

**Cardinal rule:** a service never reads another service's database. All cross-context data flows
through REST APIs (Feign + circuit breaker), per the Assignment 03 context map.

## 9. Troubleshooting

- **`./mvnw` permission denied** → `chmod +x mvnw`.
- **Wrong Java version errors** → confirm `echo $JAVA_HOME` points to a JDK 21; re-open the terminal.
- **Port already in use** → another service/instance is running on that port; stop it or change the
  `server.port` in that module's `application.yml`.
- **Config Server can't find files** → start it from the repo root or via `./mvnw -pl infra-config-server`;
  it searches both `file:./config` and `file:../config`.
