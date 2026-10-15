# Inventory Management API

A REST API for managing products, categories, and suppliers, built with Java 21, Spring Boot 4.1.1, Spring Data JPA, and PostgreSQL.

## Features

- CRUD operations for products, categories, and suppliers.
- Product filtering by category, supplier, name, and price range.
- JPA relationships between products, categories, and suppliers.
- Request validation with Jakarta Bean Validation.
- Centralized exception handling using Spring `ProblemDetail`.
- Unit and integration tests.

## Technologies

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- H2 Database
- Jakarta Bean Validation
- Lombok
- Maven
- JUnit, Mockito, and MockMvc

## Prerequisites

- JDK 21
- PostgreSQL (for local and production profiles)

The project includes Maven Wrapper, so Maven does not need to be installed separately.

## Installation and Setup

Clone the repository:

```bash
git clone https://github.com/kardiguemagassa/inventory-management.git
cd inventory-management
```

### Run with H2 (default)

The `dev` profile is enabled by default and uses an in-memory H2 database.

```bash
./mvnw spring-boot:run
```

By default, the application runs at:

`http://localhost:8080`

H2 console:

`http://localhost:8080/h2-console`

H2 JDBC URL:

`jdbc:h2:mem:inventorydb`

Username: `sa`

Password: empty

### Run with PostgreSQL (local profile)

Create a PostgreSQL database and configure the environment variables:

```bash
export POSTGRES_HOST=localhost
export POSTGRES_PORT=5432
export POSTGRES_DB=inventorydb
export POSTGRES_USERNAME=postgres
export POSTGRES_PASSWORD=your_password
```

Start the application:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Replace the example credentials with your actual PostgreSQL configuration.

### Production profile

The `prod` profile requires:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

Example JDBC URL:

`jdbc:postgresql://localhost:5432/inventorydb`

The production profile uses `ddl-auto: validate`, so the database schema must already exist.

Do not commit database passwords or secrets to Git.

## Spring Profiles

| Profile | Database | Hibernate DDL |
|---|---|---|
| `dev` (default) | H2 in-memory | `update` |
| `local` | PostgreSQL | `update` |
| `prod` | PostgreSQL | `validate` |

Open Session in View is disabled using `spring.jpa.open-in-view: false`.

## API Endpoints

### Products

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/products` | List all products |
| GET | `/api/products/{id}` | Get a product by ID |
| GET | `/api/products/category/{categoryId}` | Filter by category |
| GET | `/api/products/supplier/{supplierId}` | Filter by supplier |
| GET | `/api/products/search?name=mouse` | Search by name |
| GET | `/api/products/price-range?min=10&max=100` | Filter by price |
| POST | `/api/products` | Create a product |
| PUT | `/api/products/{id}` | Update a product |
| DELETE | `/api/products/{id}` | Delete a product |

Example product request:

```json
{
  "name": "Wireless Mouse",
  "description": "Ergonomic wireless mouse",
  "price": 49.99,
  "quantity": 20,
  "categoryId": 1,
  "supplierId": 1
}
```

The referenced category and supplier must already exist.

### Categories

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/categories` | List all categories |
| GET | `/api/categories/{id}` | Get a category |
| POST | `/api/categories` | Create a category |
| PUT | `/api/categories/{id}` | Update a category |
| DELETE | `/api/categories/{id}` | Delete a category |

Example category request:

```json
{
  "name": "Electronics"
}
```

### Suppliers

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/suppliers` | List all suppliers |
| GET | `/api/suppliers/{id}` | Get a supplier |
| POST | `/api/suppliers` | Create a supplier |
| PUT | `/api/suppliers/{id}` | Update a supplier |
| DELETE | `/api/suppliers/{id}` | Delete a supplier |

Example supplier request:

```json
{
  "name": "Tech Distribution",
  "email": "contact@example.com",
  "phone": "0123456789"
}
```

## Validation and Error Handling

Requests are validated using Jakarta Bean Validation.

Examples:

- Product and category names cannot be blank.
- Product prices and quantities cannot be negative.
- Category and supplier IDs must be positive.
- Supplier email addresses must have a valid format when provided.

The API returns standardized error responses using Spring `ProblemDetail`.

| HTTP Status | Description |
|---|---|
| `400 Bad Request` | Invalid request data |
| `404 Not Found` | Resource not found |
| `409 Conflict` | Duplicate resource |
| `201 Created` | Resource created |
| `204 No Content` | Resource deleted |

## Testing

Run the complete test suite:

```bash
./mvnw clean test
```

The project includes unit and integration tests covering:

- Service business logic.
- JPA repositories.
- REST controllers using MockMvc.
- CRUD operations.
- Validation errors.
- Missing and duplicate resources.
- Product search and filtering.

Integration tests use an in-memory H2 database with the `test` profile.

**Last verified result: 101 tests passed, 0 failures, 0 errors, 0 skipped.**

## Project Structure

```text
src/
├── main/
│   ├── java/com/kmgtechnologies/inventorymanagement/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── mapper/
│   │   ├── repository/
│   │   └── service/
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-local.yml
│       └── application-prod.yml
└── test/
    └── java/com/kmgtechnologies/inventorymanagement/
```

## Database Migrations

Flyway and Liquibase are not configured. Database migrations are optional for this assignment.