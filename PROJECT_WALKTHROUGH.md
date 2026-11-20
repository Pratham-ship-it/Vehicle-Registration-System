# Comprehensive Project Walkthrough & Viva Guide
## Vehicle Registration & Spare Parts Management System

---

## Table of Contents
1. [What This Project Is (Plain-English Summary)](#1-what-this-project-is-plain-english-summary)
2. [How Everything Connects (End-to-End Request Flow)](#2-how-everything-connects-end-to-end-request-flow)
   - [Flow 1: Registering a Vehicle](#flow-1-registering-a-new-vehicle)
   - [Flow 2: Placing a Spare Part Order](#flow-2-placing-a-spare-part-order)
3. [File-by-File Breakdown (Every File Explained Simply)](#3-file-by-file-breakdown)
   - [Entry Point & Configuration](#entry-point--configuration)
   - [Entity Layer (Database Models)](#entity-layer-database-models)
   - [DTO Layer (Data Transfer Objects)](#dto-layer-data-transfer-objects)
   - [Repository Layer (Data Access)](#repository-layer-data-access)
   - [Service Layer (Business Logic)](#service-layer-business-logic)
   - [Controller Layer (REST Endpoints)](#controller-layer-rest-endpoints)
   - [Exception Handling](#exception-handling)
   - [Frontend Files](#frontend-files)
   - [Unit Test](#unit-test)
4. [Top 15 Viva Voce Questions & Model Answers](#4-top-15-viva-voce-questions--model-answers)

---

## 1. What This Project Is (Plain-English Summary)

### The Real-World Problem
In traditional automotive repair centers, regional transport offices (RTOs), or dealerships, vehicle records, customer contact details, and inventory spare-part orders are frequently managed on manual paper registers or fragmented spreadsheets. This leads to:
- Duplicate vehicle registrations (same license plate or chassis assigned twice).
- Lost customer history (difficult to trace which vehicle belongs to whom).
- Disconnected spare-part orders (orders placed with no clear link to a verified registered vehicle).

### The Solution: Vehicle Registration System (VRS)
This project is a centralized, lightweight web application built using **Java 17**, **Spring Boot 3.3.4**, **Hibernate ORM / Spring Data JPA**, and an embedded **SQLite3** database, paired with a clean **HTML5/CSS3/JavaScript** dashboard.

It delivers three core features:
1. **Customer & Vehicle Registration:** Enables registering a new vehicle along with its owner in a single atomic transaction, or associating a new vehicle with an existing customer (demonstrating a `1-to-Many` relationship).
2. **Vehicle Directory & Live Search:** A search table where administrators can view all registered vehicles and search in real-time by make, model, or registration number, as well as delete records.
3. **Spare Parts Order Management:** Allows workshops to log spare-part purchase requests against registered vehicles with automatic date tracking, default status (`ORDERED`), and calculated totals.

---

## 2. How Everything Connects (End-to-End Request Flow)

### Flow 1: Registering a New Vehicle
What happens behind the scenes when a user fills out the form and clicks **"REGISTER VEHICLE"**?

```
[Browser: index.html]
         │ (User clicks "REGISTER VEHICLE")
         ▼
[script.js]
  1. Captures form inputs (registrationNumber, chassisNumber, make, customer details).
  2. Constructs JSON payload.
  3. Sends HTTP POST request to `/api/vehicles`.
         │ HTTP POST JSON
         ▼
[VehicleController.java]
  4. Receives request in `@PostMapping public ResponseEntity<?> registerVehicle(...)`.
  5. Maps JSON into `VehicleRegistrationRequest` DTO.
  6. Checks if customer is new or existing, and delegates to `VehicleService`.
         │ registerVehicle(vehicle, customerId, newCustomer)
         ▼
[VehicleService.java]
  7. Validates non-null / non-empty mandatory fields.
  8. Normalizes text (trims whitespace, converts registration & chassis to UPPERCASE).
  9. Calls `vehicleRepository.existsByRegistrationNumberIgnoreCase(regNo)` and
     `vehicleRepository.existsByChassisNumberIgnoreCase(chassisNo)`.
     -> If a duplicate is found, throws `IllegalArgumentException`.
 10. Resolves the `Customer`: fetches existing customer from `CustomerRepository`
     OR saves the new customer via `CustomerRepository.save(newCustomer)`.
 11. Links customer to vehicle: `vehicle.setCustomer(owner)`.
 12. Persists the vehicle: `vehicleRepository.save(vehicle)`.
         │ SQL INSERT statements
         ▼
[vehicle_registration.db (SQLite)]
 13. Executes INSERT into `customers` (if new) and INSERT into `vehicles`.
 14. Generates auto-incremented primary keys (`id`).
         │
         ▼
[Response Pipeline]
 15. Service returns saved `Vehicle` object -> Controller wraps it in `ResponseEntity.status(HttpStatus.CREATED).body(saved)`.
 16. `script.js` receives `201 Created`, displays green success banner, clears the form, and calls `refreshAll()`.
 17. Dashboard counters and tables immediately update to reflect the newly registered vehicle!
```

---

### Flow 2: Placing a Spare Part Order
What happens when a technician orders a part for a car?

```
[Browser: index.html]
         │ (User selects vehicle from dropdown, enters part, qty, price, clicks "PLACE ORDER")
         ▼
[script.js]
  1. Reads `orderVehicleId`, `partName`, `quantity`, `unitPrice`.
  2. Sends HTTP POST with JSON body to `/api/orders`.
         │ HTTP POST JSON
         ▼
[SparePartOrderController.java]
  3. Deserializes JSON into `SparePartOrderRequest` DTO.
  4. Calls `orderService.placeOrder(vehicleId, partName, quantity, unitPrice)`.
         │
         ▼
[SparePartOrderService.java]
  5. Validates: `vehicleId != null`, `partName` not blank, `quantity >= 1`.
  6. Fetches vehicle from `VehicleRepository.findById(vehicleId)`.
     -> If not found, throws `IllegalArgumentException`.
  7. Creates new `SparePartOrder`:
     - Sets vehicle reference: `order.setVehicle(vehicle)`.
     - Sets quantity and unit price.
     - `@PrePersist` automatically sets `orderDate = LocalDate.now()` and `status = "ORDERED"`.
  8. Saves through `orderRepository.save(order)`.
         │ SQL INSERT into spare_part_orders
         ▼
[vehicle_registration.db (SQLite)]
  9. Order record is inserted with foreign key `vehicle_id`.
         │
         ▼
[Response Pipeline]
 10. Returns `201 Created` with saved order JSON.
 11. `script.js` shows success message and refreshes the "SPARE PARTS ORDERS" table and counter.
```

---

## 3. File-by-File Breakdown

### Entry Point & Configuration

- **`src/main/java/com/vrs/VrsApplication.java`**
  - **What it is:** The main entry point of the Spring Boot application containing `public static void main(String[] args)`.
  - **Why it exists:** Annotated with `@SpringBootApplication`, it triggers component scanning, autoconfiguration, and launches the embedded Tomcat web server. It dynamically reads the configured port and prints the local startup URL in the terminal.

- **`pom.xml`**
  - **What it is:** Maven Project Object Model configuration file.
  - **Why it exists:** Declares project metadata, Java version (17), and all external dependencies: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `sqlite-jdbc`, and `hibernate-community-dialects`.

- **`src/main/resources/application.properties`**
  - **What it is:** Spring Boot configuration file.
  - **Why it exists:** Configures server port (`server.port=8081`), database connection (`jdbc:sqlite:vehicle_registration.db`), SQLite JDBC driver, and Hibernate DDL generation (`ddl-auto=update` and `SQLiteDialect`).

---

### Entity Layer (Database Models)
*Located in `src/main/java/com/vrs/entity/`*

- **`Customer.java`**
  - **What it is:** JPA entity representing the `customers` database table.
  - **Fields:** `id` (Primary Key), `name`, `contactNumber`, `email`, `address`, and `List<Vehicle> vehicles`.
  - **Relationship:** `@OneToMany(mappedBy = "customer")` — one customer can own multiple registered vehicles. Annotated with `@JsonIgnore` on the vehicle list to prevent circular serialization recursion.

- **`Vehicle.java`**
  - **What it is:** JPA entity representing the `vehicles` database table.
  - **Fields:** `id` (PK), `registrationNumber` (Unique), `chassisNumber` (Unique), `make`, `model`, `manufactureYear`, `vehicleType`, `color`, `engineNumber`, `registrationDate`, and `customer`.
  - **Relationship:** `@ManyToOne` with `Customer` (mandatory foreign key `customer_id`), and `@OneToMany` with `SparePartOrder` with `cascade = CascadeType.ALL, orphanRemoval = true` (deleting a vehicle cascades and removes its spare part orders).

- **`SparePartOrder.java`**
  - **What it is:** JPA entity representing the `spare_part_orders` table.
  - **Fields:** `id` (PK), `vehicle` (FK `vehicle_id`), `partName`, `quantity`, `unitPrice`, `status`, and `orderDate`.
  - **Relationship:** `@ManyToOne` with `Vehicle`. Contains `@PrePersist` hook that defaults `orderDate` to today's date and `status` to `"ORDERED"`.

---

### DTO Layer (Data Transfer Objects)
*Located in `src/main/java/com/vrs/dto/`*

- **`VehicleRegistrationRequest.java`**
  - **What it is:** A Plain Old Java Object (POJO) used as the incoming request payload for vehicle registration.
  - **Why it exists:** Separates API contracts from internal database schema. In a single request, the client can either pass an existing `customerId` OR raw customer details (`customerName`, `customerContact`, `customerEmail`, `customerAddress`) alongside vehicle specifications.

- **`SparePartOrderRequest.java`**
  - **What it is:** Incoming payload object for placing an order (`vehicleId`, `partName`, `quantity`, `unitPrice`).
  - **Why it exists:** Keeps client requests lightweight without needing the frontend to package a full nested `Vehicle` entity graph.

---

### Repository Layer (Data Access)
*Located in `src/main/java/com/vrs/repository/`*

- **`CustomerRepository.java`**
  - **What it is:** Spring Data JPA repository interface extending `JpaRepository<Customer, Long>`.
  - **Key Method:** `findByNameIgnoreCaseAndContactNumber(String name, String contactNumber)` to look up matching customers.

- **`VehicleRepository.java`**
  - **What it is:** Spring Data JPA repository interface extending `JpaRepository<Vehicle, Long>`.
  - **Key Methods:**
    - `existsByRegistrationNumberIgnoreCase(String regNo)`: Fast boolean check for duplicate license plates.
    - `existsByChassisNumberIgnoreCase(String chassisNo)`: Fast boolean check for duplicate chassis/VIN numbers.
    - `findByMakeContainingIgnoreCaseOrModelContainingIgnoreCaseOrRegistrationNumberContainingIgnoreCase(...)`: Implements fuzzy search across make, model, or plate.
    - `findAllByOrderByIdDesc()`: Lists all vehicles with newest registrations first.

- **`SparePartOrderRepository.java`**
  - **What it is:** Spring Data JPA repository interface extending `JpaRepository<SparePartOrder, Long>`.
  - **Key Methods:** `findByVehicleId(Long vehicleId)` and `findAllByOrderByIdDesc()`.

---

### Service Layer (Business Logic)
*Located in `src/main/java/com/vrs/service/`*

- **`CustomerService.java`**
  - **What it is:** Service handling customer retrieval, creation, and deletion.
  - **Why it exists:** Validates that customer names and contact numbers are provided before persisting records.

- **`VehicleService.java`**
  - **What it is:** Core business service handling vehicle registration, search, and deletion.
  - **Why it exists:** Enforces business constraints:
    1. Trims and capitalizes registration and chassis numbers.
    2. Enforces uniqueness by throwing `IllegalArgumentException` before saving to prevent DB-level crashes.
    3. Manages customer linking: reuses existing customer or creates a new one inside a single `@Transactional` method.

- **`SparePartOrderService.java`**
  - **What it is:** Service handling parts order placement and status updates (`ORDERED` -> `SHIPPED` -> `DELIVERED`).
  - **Why it exists:** Validates that the referenced vehicle exists in the database and that quantity is at least 1 before issuing an order.

---

### Controller Layer (REST Endpoints)
*Located in `src/main/java/com/vrs/controller/`*

- **`CustomerController.java`**
  - **What it is:** Spring `@RestController` exposing `/api/customers`.
  - **Endpoints:** `GET /api/customers`, `GET /api/customers/{id}`, `POST /api/customers`, `DELETE /api/customers/{id}`.

- **`VehicleController.java`**
  - **What it is:** Spring `@RestController` exposing `/api/vehicles`.
  - **Endpoints:**
    - `POST /api/vehicles`: Registers a vehicle.
    - `GET /api/vehicles`: Returns all vehicles.
    - `GET /api/vehicles/search?keyword=...`: Returns filtered vehicles.
    - `GET /api/vehicles/{id}`: Returns a single vehicle.
    - `DELETE /api/vehicles/{id}`: Deletes a vehicle record.

- **`SparePartOrderController.java`**
  - **What it is:** Spring `@RestController` exposing `/api/orders`.
  - **Endpoints:**
    - `GET /api/orders`: Returns all orders.
    - `GET /api/orders/vehicle/{vehicleId}`: Returns orders for a given vehicle.
    - `POST /api/orders`: Places an order.
    - `PATCH /api/orders/{id}/status`: Updates status.
    - `DELETE /api/orders/{id}`: Deletes an order.

---

### Exception Handling

- **`src/main/java/com/vrs/controller/GlobalExceptionHandler.java`**
  - **What it is:** Centralized exception interceptor annotated with `@RestControllerAdvice`.
  - **Why it exists:** Catches unhandled exceptions across all controllers. Specifically handles `DataIntegrityViolationException` (DB constraint failures) and returns a clean HTTP 400 Bad Request JSON response (`{"error": "..."}`) rather than a messy 500 stack trace.

---

### Frontend Files
*Located in `src/main/resources/static/`*

- **`index.html`**
  - **What it is:** Single-page application (SPA) user interface.
  - **Why it exists:** Contains three organized tabs: "REGISTER VEHICLE", "VEHICLE DIRECTORY", and "SPARE PARTS ORDERS", as well as dashboard stat counters and dynamic forms.

- **`style.css`**
  - **What it is:** Pure CSS styling sheet.
  - **Why it exists:** Provides modern styling with an industrial dark theme, responsive grid layouts, status badges, tabular styling, and interactive button transitions.

- **`script.js`**
  - **What it is:** Vanilla JavaScript application logic.
  - **Why it exists:** Uses the standard `fetch()` API to communicate asynchronously with `/api/*` endpoints. Handles tab switching, dynamic form reset, live search input, customer dropdown populating, and live counter updates without full page reloads.

---

### Unit Test

- **`src/test/java/com/vrs/service/VehicleServiceTest.java`**
  - **What it is:** JUnit 5 and Mockito test suite.
  - **Why it exists:** Unit tests `VehicleService` in complete isolation using `@Mock` on repositories. Verifies that duplicate registration numbers and chassis numbers are properly intercepted and rejected with informative error messages.

---

## 4. Top 15 Viva Voce Questions & Model Answers

### Q1: What architecture does this project follow?
**Answer:** It follows a classic **Layered (N-Tier) Architecture**:
1. **Presentation Layer:** HTML5/CSS3/Vanilla JS (client-side).
2. **Controller Layer:** `@RestController` handling HTTP requests and returning JSON responses.
3. **Service Layer:** `@Service` classes containing business logic and `@Transactional` boundaries.
4. **Data Access (Repository) Layer:** Spring Data JPA interfaces.
5. **Database Layer:** Embedded SQLite database.

---

### Q2: What is the difference between `@RestController` and `@Controller`?
**Answer:**
- `@Controller` is traditionally used in Spring MVC to return HTML views (like JSP or Thymeleaf).
- `@RestController` is a convenience annotation that combines `@Controller` and `@ResponseBody`. It automatically serializes return values (such as Java objects and lists) directly into JSON or XML in the HTTP response body.

---

### Q3: What is Spring Data JPA, and how is it different from Hibernate?
**Answer:**
- **Hibernate** is an Object-Relational Mapping (ORM) framework that implements the JPA (Jakarta Persistence API) specification. It maps Java classes to database tables and translates HQL/JPQL into native SQL.
- **Spring Data JPA** is an abstraction layer built on top of JPA/Hibernate. By extending interfaces like `JpaRepository<T, ID>`, Spring automatically provides complete CRUD methods (`save`, `findById`, `delete`, `findAll`) at runtime without writing any boilerplate SQL or DAO implementation classes.

---

### Q4: Why did you use DTOs (`VehicleRegistrationRequest`) instead of using `@RequestBody Vehicle` directly?
**Answer:**
1. **Security & Decoupling:** Exposing internal database entities directly in controllers allows "over-posting" vulnerabilities where clients could alter unintended fields (like `id` or internal flags).
2. **Flexible API Contracts:** In this project, registering a vehicle can either link an existing `customerId` OR take raw fields to create a new customer. The single incoming DTO cleanly captures both possibilities without cluttering the `Vehicle` entity.

---

### Q5: What database relationships exist in your system?
**Answer:**
There are two `1-to-Many` relationships:
1. **`Customer` (1) ── (N) `Vehicle`**: One customer can own multiple vehicles. In `Customer`, this is configured via `@OneToMany(mappedBy = "customer")`, and in `Vehicle`, by `@ManyToOne @JoinColumn(name = "customer_id")`.
2. **`Vehicle` (1) ── (N) `SparePartOrder`**: One vehicle can have multiple spare part orders. In `Vehicle`, configured with `cascade = CascadeType.ALL, orphanRemoval = true`, meaning deleting a vehicle automatically deletes its related orders.

---

### Q6: Why do you have `@JsonIgnore` and `@JsonIgnoreProperties` on your entity relationships?
**Answer:**
To prevent **infinite circular recursion** during JSON serialization:
- `Customer` references a `List<Vehicle>`, and each `Vehicle` references back to its `Customer`. Without `@JsonIgnore` on `Customer.vehicles`, Jackson serializer would loop back and forth endlessly, causing a `StackOverflowError`.
- `@JsonIgnoreProperties` also suppresses Hibernate internal proxy fields like `hibernateLazyInitializer` and `handler`.

---

### Q7: How does Dependency Injection (DI) and Inversion of Control (IoC) work in this project?
**Answer:**
Spring's IoC container manages the lifecycle of all beans (`@Service`, `@Repository`, `@RestController`). Instead of instantiating dependencies manually using `new VehicleService()`, we use **Constructor Injection**:
```java
public VehicleController(VehicleService vehicleService) {
    this.vehicleService = vehicleService;
}
```
Spring automatically detects the constructor and injects the required bean at runtime. This promotes loose coupling and makes unit testing simple.

---

### Q8: How did you implement duplicate registration number prevention?
**Answer:**
Through a **two-layer defense**:
1. **Application/Service Layer (Primary):** Before saving, `VehicleService` executes `vehicleRepository.existsByRegistrationNumberIgnoreCase(regNo)`. If true, it immediately throws an `IllegalArgumentException` with a user-friendly message, returning HTTP 400 Bad Request.
2. **Database Layer (Fallback):** The `vehicles` table defines a unique constraint (`@Column(unique = true)` and `@Table(uniqueConstraints = ...)`). Any race condition is caught by the database, triggering a `DataIntegrityViolationException`.

---

### Q9: What is the purpose of `GlobalExceptionHandler`?
**Answer:**
Annotated with `@RestControllerAdvice`, it acts as a global interceptor for exceptions thrown across any controller:
- It intercepts uncaught exceptions (e.g., database constraint violations).
- Instead of letting the server return an ugly HTML error page with a 500 status code and stack trace, it formats a clean JSON response (e.g., `{"error": "Database constraint violation"}`) with an appropriate HTTP status (400 or 500).

---

### Q10: Why did you choose SQLite for this project instead of MySQL or PostgreSQL?
**Answer:**
1. **Zero Configuration:** SQLite is a serverless, self-contained, file-based database stored right in the project root (`vehicle_registration.db`).
2. **Portability:** Anyone downloading or evaluating this project can run `mvn spring-boot:run` without needing to install, configure, or start a separate MySQL server or manage credentials.
3. **Hibernate Dialect Compatibility:** Using `hibernate-community-dialects`, Hibernate translates JPA queries into SQLite syntax seamlessly.

---

### Q11: What does `@Transactional` do in `VehicleService`?
**Answer:**
It ensures **ACID** properties (specifically Atomicity):
When registering a vehicle with a new customer:
1. The new `Customer` is saved.
2. The `Vehicle` is linked and saved.

If the vehicle save fails (e.g., duplicate chassis number), `@Transactional` rolls back the entire operation so that an orphan customer record is not left lingering in the database.

---

### Q12: What lifecycle callback annotations did you use in entities?
**Answer:**
We used `@PrePersist` in `Vehicle` and `SparePartOrder`:
- In `Vehicle`: Automatically sets `registrationDate = LocalDate.now()` right before the entity is first inserted into the database.
- In `SparePartOrder`: Automatically initializes `orderDate = LocalDate.now()` and sets default `status = "ORDERED"` if not provided.

---

### Q13: How does the frontend communicate with the backend?
**Answer:**
The frontend uses standard **Asynchronous JavaScript (AJAX) via the native `fetch()` API**:
- When actions occur (like submitting forms or searching), JS makes asynchronous HTTP requests (`GET`, `POST`, `DELETE`) with JSON payloads to relative endpoints (`/api/vehicles`, `/api/customers`, `/api/orders`).
- It parses the JSON response and dynamically updates the DOM elements without reloading the web page.

---

### Q14: How are unit tests written for this project?
**Answer:**
In `VehicleServiceTest.java`, unit tests are written using **JUnit 5** and **Mockito** (`@ExtendWith(MockitoExtension.class)`):
- The repositories are mocked using `@Mock`.
- We use `when(vehicleRepository.existsByRegistrationNumberIgnoreCase(...)).thenReturn(true)` to simulate database state.
- We test business rules in memory without spinning up the entire Spring context or touching the real SQLite database file, making tests execute in under 2 seconds.

---

### Q15: If this project were scaled to production, what improvements would you make?
**Answer:**
1. **Authentication & Authorization:** Add **Spring Security** with JWT (JSON Web Tokens) to secure APIs with Role-Based Access Control (Admin, Technician, Customer).
2. **Database Migration:** Switch from embedded SQLite to an enterprise relational database like **PostgreSQL** or **MySQL**, utilizing **Flyway** or **Liquibase** for schema versioning.
3. **Pagination & Sorting:** Update repository methods with `Pageable` (`Page<Vehicle>`) so that tables with thousands of records load quickly with pagination.
4. **API Documentation:** Integrate **Springdoc-OpenAPI (Swagger UI)** for interactive API documentation at `/swagger-ui.html`.
