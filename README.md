# Vehicle Registration & Spare Parts Management System

A full-stack web application built for academic demonstration of a layered Spring Boot architecture with Hibernate JPA persistence and a dependency-free HTML5/CSS3/Vanilla JavaScript frontend.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.3.4 |
| ORM / Persistence | Spring Data JPA · Hibernate ORM 6.5 |
| Database | SQLite 3 (via `sqlite-jdbc 3.46.1.0` + `hibernate-community-dialects`) |
| Frontend | HTML5 · CSS3 · Vanilla JavaScript (fetch API) |
| Build Tool | Apache Maven 3.9.9 (bundled at `.tools/`) |
| Testing | JUnit 5 · Mockito (via `spring-boot-starter-test`) |

---

## Layered Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Browser (Client)                   │
│         index.html · style.css · script.js          │
│        (fetch API → relative /api/* endpoints)      │
└───────────────────────┬─────────────────────────────┘
                        │ HTTP / JSON  (port 8081)
┌───────────────────────▼─────────────────────────────┐
│              REST Controller Layer                   │
│   CustomerController   /api/customers               │
│   VehicleController    /api/vehicles                │
│   SparePartOrderController /api/orders              │
│   GlobalExceptionHandler  (@RestControllerAdvice)   │
└───────────────────────┬─────────────────────────────┘
                        │ method calls
┌───────────────────────▼─────────────────────────────┐
│                 Service Layer                        │
│  CustomerService · VehicleService                   │
│  SparePartOrderService                              │
│  (business logic, validation, @Transactional)       │
└───────────────────────┬─────────────────────────────┘
                        │ Spring Data JPA
┌───────────────────────▼─────────────────────────────┐
│              Repository Layer                        │
│  CustomerRepository · VehicleRepository             │
│  SparePartOrderRepository                           │
│  (extends JpaRepository<Entity, Long>)              │
└───────────────────────┬─────────────────────────────┘
                        │ Hibernate / JDBC
┌───────────────────────▼─────────────────────────────┐
│              SQLite Database                         │
│  vehicle_registration.db  (auto-created on startup) │
│  Tables: customers · vehicles · spare_part_orders   │
└─────────────────────────────────────────────────────┘
```

---

## Entity-Relationship Summary

### Relationships
- **Customer** `1 ──< N` **Vehicle** (`customer_id` FK, nullable=false)
- **Vehicle** `1 ──< N` **SparePartOrder** (`vehicle_id` FK, nullable=false, cascade delete)

### Key Fields & Constraints

**Customer**
| Field | Type | Constraints |
|-------|------|------------|
| `id` | Long (PK) | Auto-generated |
| `name` | String | NOT NULL |
| `contactNumber` | String | NOT NULL |
| `email` | String | Optional |
| `address` | String | Optional |

**Vehicle**
| Field | Type | Constraints |
|-------|------|------------|
| `id` | Long (PK) | Auto-generated |
| `registrationNumber` | String | NOT NULL, **UNIQUE** |
| `chassisNumber` | String | NOT NULL, **UNIQUE** |
| `make` | String | NOT NULL |
| `model` | String | NOT NULL |
| `manufactureYear` | Integer | Optional |
| `vehicleType` | String | Optional |
| `color` | String | Optional |
| `engineNumber` | String | Optional |
| `registrationDate` | LocalDate | Auto-set on persist (`@PrePersist`) |
| `customer_id` | Long (FK) | NOT NULL → `customers.id` |

**SparePartOrder**
| Field | Type | Constraints |
|-------|------|------------|
| `id` | Long (PK) | Auto-generated |
| `partName` | String | NOT NULL |
| `quantity` | Integer | NOT NULL, min=1 |
| `unitPrice` | Double | Optional |
| `status` | String | Default `ORDERED` on persist |
| `orderDate` | LocalDate | Auto-set on persist |
| `vehicle_id` | Long (FK) | NOT NULL → `vehicles.id` |

---

## REST API Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/api/customers` | List all customers |
| `POST` | `/api/customers` | Create a customer |
| `GET` | `/api/customers/{id}` | Get customer by ID |
| `DELETE` | `/api/customers/{id}` | Delete a customer |
| `GET` | `/api/vehicles` | List all vehicles (ordered by ID desc) |
| `POST` | `/api/vehicles` | Register a new vehicle (+ optional new customer) |
| `GET` | `/api/vehicles/{id}` | Get vehicle by ID |
| `GET` | `/api/vehicles/search?keyword=` | Search by make / model / registration number |
| `DELETE` | `/api/vehicles/{id}` | Delete a vehicle (cascades orders) |
| `GET` | `/api/orders` | List all spare-part orders (newest first) |
| `POST` | `/api/orders` | Place a new spare-part order |
| `GET` | `/api/orders/vehicle/{vehicleId}` | List orders for a specific vehicle |
| `PATCH` | `/api/orders/{id}/status` | Update order status (`ORDERED` / `SHIPPED` / `DELIVERED`) |
| `DELETE` | `/api/orders/{id}` | Delete an order |

> **Validation:** `registrationNumber` and `chassisNumber` are checked for duplicates (case-insensitive) in `VehicleService` before saving. `GlobalExceptionHandler` catches `DataIntegrityViolationException` as a fallback.

---

## Running the Application

> **Prerequisites:** JDK 17+ installed (JDK 22 confirmed working). No global Maven required — a portable Maven 3.9.9 is bundled under `.tools/`.

```powershell
# From D:\Summer_project_5thsem

# Start the application (Ctrl+C to stop)
.\mvn.cmd spring-boot:run

# Run unit tests
.\mvn.cmd clean test

# Build a deployable fat JAR
.\mvn.cmd clean package
# Output: target\vehicle-registration-system-1.0.0.jar
# Run the JAR directly:
java -jar target\vehicle-registration-system-1.0.0.jar
```

Once running, open your browser at:
**http://localhost:8081** (or port configured in `application.properties`)

The SQLite database (`vehicle_registration.db`) is created automatically in the project root on first startup.

---

## Unit Tests

Located at `src/test/java/com/vrs/service/VehicleServiceTest.java`. Tests use **Mockito** to mock `VehicleRepository` and `CustomerRepository` — no database or Spring context required.

| Test | Covers |
|------|--------|
| `registerVehicle_rejectsDuplicateRegistrationNumber` | Duplicate reg-number throws `IllegalArgumentException` |
| `registerVehicle_rejectsDuplicateChassisNumber` | Duplicate chassis-number throws `IllegalArgumentException` |
| `registerVehicle_successWithNewCustomer` | Happy path: new customer + vehicle saved |

```
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## Project Structure

```
Summer_project_5thsem/
├── pom.xml
├── mvn.cmd                          ← run Maven (Windows)
├── .gitignore
└── src/
    ├── main/
    │   ├── java/com/vrs/
    │   │   ├── VrsApplication.java          (entry point)
    │   │   ├── controller/                  (REST layer)
    │   │   ├── service/                     (business logic)
    │   │   ├── repository/                  (data access)
    │   │   ├── entity/                      (JPA entities)
    │   │   └── dto/                         (request DTOs)
    │   └── resources/
    │       ├── application.properties
    │       └── static/                      (frontend)
    │           ├── index.html
    │           ├── style.css
    │           └── script.js
    └── test/
        └── java/com/vrs/service/
            └── VehicleServiceTest.java
```
