# Spring Security Learning Project 🛡️

A Spring Boot project built to understand and implement Spring Security with JWT authentication and Role-Based Authorization.

## Tech Stack

- Java 21
- Spring Boot 3.5.10
- Spring Security 6.x
- JSON Web Tokens (JJWT 0.11.5)
- Spring Data JPA
- MySQL
- Lombok
- Springdoc OpenAPI (Swagger UI)

## Project Structure
```
src/main/java/com/example/springsec/
├── AuthController.java          # Register & Login endpoints
├── UserController.java          # Protected endpoints
├── SecurityConfig.java          # Security filter chain configuration
├── JwtAuthFilter.java           # JWT validation on every request
├── JwtUtils.java                # Generate & validate JWT tokens
├── UserDetailsImpl.java         # Spring Security user wrapper
├── UserDetailsServiceImpl.java  # Loads user from database
├── SwaggerConfig.java           # Swagger / OpenAPI configuration
├── User.java                    # User entity
├── Role.java                    # Role enum (ROLE_USER, ROLE_ADMIN)
├── UserRepository.java          # JPA repository
├── LoginRequest.java            # Login DTO
└── RegisterRequest.java         # Register DTO
```

## How It Works

### Authentication Flow
```
POST /api/auth/login  →  AuthenticationManager
                               ↓
                       UserDetailsService (fetch from DB)
                               ↓
                       BCrypt (compare passwords)
                               ↓
                       JwtUtils.generateToken()
                               ↓
                       JWT returned to client
```

### Request Flow
```
Request + JWT  →  JwtAuthFilter
                       ↓
                 validateToken()
                       ↓
                 getUsernameFromToken()
                       ↓
                 SecurityContextHolder
                       ↓
                 Controller ✅ or 403 ❌
```

## API Endpoints

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/auth/register` | Public | Register a new user |
| POST | `/api/auth/login` | Public | Login and receive JWT |
| GET | `/api/user/profile` | Authenticated | Get current user profile |
| GET | `/api/admin/dashboard` | ADMIN only | Admin dashboard |

## Getting Started

### Prerequisites
- Java 21
- MySQL 8.x
- Maven

### Setup

1. Clone the repository
```bash
git clone https://github.com/yourusername/springsec.git
cd springsec
```

2. Create the database
```sql
CREATE DATABASE springsec_db;
```

3. Update `application.properties`
```properties
spring.datasource.username=your_username
spring.datasource.password=your_password
jwt.secret=your_secret_key_minimum_32_characters
```

4. Run the project
```bash
mvn spring-boot:run
```

5. Open Swagger UI
```
http://localhost:8080/swagger-ui.html
```

## Testing with Swagger

1. Register a user via `POST /api/auth/register`
2. Login via `POST /api/auth/login` — copy the JWT from response
3. Click **Authorize** button in Swagger UI
4. Enter `Bearer <your_token>`
5. Test protected endpoints

## What I Learned

- How the Spring Security Filter Chain works
- Difference between Authentication and Authorization
- How JWT tokens are generated, signed and validated
- How `UserDetails` and `UserDetailsService` connect your User entity to Spring Security
- How `SecurityContextHolder` holds the authenticated user per request
- Role-based access control with `hasRole()` and `@PreAuthorize`
