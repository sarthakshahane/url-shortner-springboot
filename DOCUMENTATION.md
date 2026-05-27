# URL Shortener — Spring Boot

> A production-grade URL shortener rebuilt from Go/Gin to Spring Boot + PostgreSQL + JPA.

---

## Project Structure

```
url-shortner/
├── src/main/java/com/urlshortner/
│   ├── UrlShortnerApplication.java     ← Entry point (@SpringBootApplication)
│   ├── config/
│   │   └── OpenApiConfig.java          ← Swagger/OpenAPI setup
│   ├── controller/
│   │   ├── UrlController.java          ← REST API (/api/urls)
│   │   └── RedirectController.java     ← Redirect handler (/{shortCode})
│   ├── dto/
│   │   ├── UrlRequest.java             ← What client sends (input)
│   │   ├── UrlResponse.java            ← What we return (output)
│   │   └── ErrorResponse.java          ← Standardized error format
│   ├── entity/
│   │   └── Url.java                    ← DB table mapping (JPA Entity)
│   ├── exception/
│   │   ├── UrlNotFoundException.java
│   │   ├── ShortCodeAlreadyExistsException.java
│   │   ├── UrlExpiredException.java
│   │   └── GlobalExceptionHandler.java ← Catches all exceptions, returns JSON
│   ├── repository/
│   │   └── UrlRepository.java          ← DB operations (Spring Data JPA)
│   └── service/
│       └── UrlService.java             ← Business logic
├── src/main/resources/
│   └── application.properties          ← DB config, app settings
└── pom.xml                             ← Maven dependencies
```

---

## Go vs Spring Boot — Direct Comparison

| Concept | Go (your project) | Spring Boot (this project) |
|---|---|---|
| Entry Point | `main()` in `main.go` | `UrlShortnerApplication.java` with `@SpringBootApplication` |
| Router | `gin.Default()` | `@RestController` + `@RequestMapping` |
| Route Registration | `routes.RegisterRoutes(r, handler)` | Auto-discovered via annotations |
| Handler | `handlers.NewURLHandler(service)` | `UrlController` injected by Spring |
| Service | `services.NewURLService(repo)` | `UrlService` with `@Service` |
| Repository | `repository.NewURLRepository(db)` | `UrlRepository extends JpaRepository` |
| DB Connection | `config.ConnectDB()` | Auto-configured via `application.properties` |
| Models | `models/url.go` struct | `entity/Url.java` with `@Entity` |
| SQL Queries | Manual `db.Query(...)` | Generated from method names / `@Query` |
| Error Handling | Return error values | Throw exceptions → `@RestControllerAdvice` |
| JSON | `gin.JSON(...)` | Automatic via `@RestController` |
| Dependency Injection | Manual constructor passing | Spring IoC container |

---

## API Endpoints

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| `POST` | `/api/urls` | Create a short URL | `201 Created` |
| `GET` | `/api/urls` | List all short URLs | `200 OK` |
| `GET` | `/api/urls/{code}/stats` | Get stats for a URL | `200 OK` |
| `DELETE` | `/api/urls/{code}` | Delete a short URL | `204 No Content` |
| `GET` | `/{code}` | Redirect to original URL | `302 Found` |

### Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## Request & Response Examples

### POST /api/urls

**Request body:**
```json
{
  "originalUrl": "https://www.google.com/search?q=spring+boot+jpa",
  "customCode": "google"
}
```

**Response (201):**
```json
{
  "id": 1,
  "shortUrl": "http://localhost:8080/google",
  "originalUrl": "https://www.google.com/search?q=spring+boot+jpa",
  "shortCode": "google",
  "clickCount": 0,
  "createdAt": "2025-05-27T10:30:00",
  "expiresAt": null
}
```

### Error Response (409 Conflict):
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Short code 'google' is already taken. Try a different one.",
  "timestamp": "2025-05-27T10:31:00"
}
```

---

## Key Spring Annotations — Quick Reference

### Class-level
| Annotation | Meaning |
|---|---|
| `@SpringBootApplication` | Combines @Configuration + @EnableAutoConfiguration + @ComponentScan |
| `@RestController` | Handles HTTP requests; all methods return JSON automatically |
| `@Service` | Marks as business logic bean; Spring manages it |
| `@Repository` | Marks as data access bean; Spring adds DB exception translation |
| `@Configuration` | Contains bean definitions |
| `@RestControllerAdvice` | Global exception handler for all controllers |

### Method-level
| Annotation | Meaning |
|---|---|
| `@GetMapping("/path")` | HTTP GET handler |
| `@PostMapping("/path")` | HTTP POST handler |
| `@DeleteMapping("/path")` | HTTP DELETE handler |
| `@ExceptionHandler(X.class)` | Handles exception of type X |
| `@Transactional` | Wraps method in a DB transaction |
| `@Bean` | Return value is registered as a Spring bean |
| `@PrePersist` | Runs before entity is first saved to DB |

### Parameter-level
| Annotation | Meaning |
|---|---|
| `@PathVariable` | Extracts `{variable}` from URL path |
| `@RequestBody` | Deserializes JSON body into Java object |
| `@Valid` | Triggers validation on the annotated parameter |
| `@Value("${key}")` | Injects value from application.properties |

### JPA / Entity
| Annotation | Meaning |
|---|---|
| `@Entity` | Class maps to a DB table |
| `@Table(name="x")` | Specifies table name |
| `@Id` | Primary key field |
| `@GeneratedValue(strategy=IDENTITY)` | Auto-increment PK |
| `@Column(nullable=false, unique=true)` | Column constraints |
| `@Query("JPQL here")` | Custom query |
| `@Modifying` | Marks an UPDATE/DELETE query |

---

## Architecture Flow

```
HTTP Request
    │
    ▼
Controller (UrlController / RedirectController)
    │  Receives request, validates @RequestBody with @Valid
    │  Extracts @PathVariable
    ▼
Service (UrlService)
    │  Business logic: generate short code, check expiry, etc.
    │  Throws custom exceptions if something goes wrong
    ▼
Repository (UrlRepository)
    │  Spring Data JPA: executes SQL on PostgreSQL
    ▼
Database (PostgreSQL - urls table)
    │
    ▼
Service (maps Url entity → UrlResponse DTO)
    │
    ▼
Controller (returns ResponseEntity with status code)
    │
    ▼
HTTP Response (JSON)
```

**If an exception is thrown anywhere:**
```
Service throws UrlNotFoundException
    │
    ▼
GlobalExceptionHandler catches it (@ExceptionHandler)
    │
    ▼
Returns JSON ErrorResponse with 404 status
```

---

## Database Table

Hibernate auto-creates this table from the `@Entity` class:

```sql
CREATE TABLE urls (
    id           BIGSERIAL PRIMARY KEY,
    original_url VARCHAR(2048) NOT NULL,
    short_code   VARCHAR(10)   NOT NULL UNIQUE,
    click_count  BIGINT        DEFAULT 0,
    created_at   TIMESTAMP     NOT NULL,
    expires_at   TIMESTAMP
);
```

---

## Running the Project

### 1. Create PostgreSQL database
```sql
CREATE DATABASE urlshortner;
```

### 2. Update application.properties
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/urlshortner
spring.datasource.username=postgres
spring.datasource.password=yourpassword
```

### 3. Run
```bash
./mvnw spring-boot:run
```

### 4. Test
```bash
# Create a short URL
curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://github.com"}'

# Use the short URL (follow redirect)
curl -L http://localhost:8080/{shortCode}

# Check stats
curl http://localhost:8080/api/urls/{shortCode}/stats
```

---

## Interview Talking Points

**"Why Spring Data JPA over raw JDBC/Hibernate?"**
> Spring Data JPA auto-generates 90% of SQL from method names. We only write `@Query` for complex operations like the atomic `incrementClickCount`. This reduces boilerplate massively and is less error-prone.

**"Why DTOs instead of returning the entity directly?"**
> The Entity is your DB schema — it may have internal fields you never want to expose. DTOs are your API contract, stable and independent of DB changes. If you add a column to the DB later, your API response doesn't change.

**"How does @Transactional work?"**
> It wraps the method in a DB transaction. If an exception is thrown, Hibernate rolls back all changes. `readOnly = true` tells Hibernate no writes happen, allowing it to skip dirty-checking optimizations.

**"Why constructor injection over @Autowired on fields?"**
> Constructor injection makes dependencies explicit and testable. You can create the class in a unit test by simply passing a mock to the constructor — no Spring context needed.

**"How is short code generation collision-safe?"**
> We use a `do-while` loop that keeps regenerating until `existsByShortCode()` returns false. With 62^6 = 56 billion combinations for 6-character codes, collisions are extremely rare in practice.
