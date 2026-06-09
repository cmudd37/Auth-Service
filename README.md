# Auth Service

Standalone authentication and authorization microservice for registration, login, JWT access tokens, refresh token rotation, role-based access control, and OAuth2 social login with Google and GitHub.

## Initiative

This project declares a production-oriented Spring Boot auth service:

- Spring Security protects API routes with stateless JWT access tokens.
- OAuth2 authorization code flow is wired through Spring Security OAuth2 Client for Google and GitHub.
- Refresh tokens are opaque, rotated on every use, revocable, hashed at rest, and stored in Redis.
- Roles and permissions live in PostgreSQL and are enforced as Spring Security authorities.
- Docker Compose runs the service with PostgreSQL and Redis.

## Stack

- Java 21
- Spring Boot 3
- Spring Security
- Spring OAuth2 Client
- Spring Data JPA
- PostgreSQL
- Redis
- Flyway
- Maven
- Docker

## Design Decisions

JWTs are short-lived access tokens signed by the auth service. They are intentionally stateless so downstream services can validate identity and authorities without a database lookup on every request.

Refresh tokens are opaque rather than JWTs. The service stores only a SHA-256 hash of the refresh secret in Redis, rotates the token during refresh, and marks the old token revoked to limit replay risk.

PostgreSQL owns users, roles, permissions, and their relationships. Flyway seeds a default `USER` role, an `ADMIN` role, and baseline permissions so authorization data is versioned with the application.

OAuth2 login uses Spring Security's authorization code flow endpoints. Successful Google or GitHub login upserts a local user, applies the default `USER` role, issues service-native tokens, and redirects to the configured frontend callback.

## API

### Register

```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "dev@example.com",
  "password": "change-me-123",
  "displayName": "Dev User"
}
```

### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "dev@example.com",
  "password": "change-me-123"
}
```

### Refresh

```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "<refresh-token>"
}
```

### Logout

```http
POST /api/auth/logout
Authorization: Bearer <access-token>
Content-Type: application/json

{
  "refreshToken": "<refresh-token>"
}
```

### Current User

```http
GET /api/me
Authorization: Bearer <access-token>
```

### OAuth2 Login

- Google: `GET /oauth2/authorization/google`
- GitHub: `GET /oauth2/authorization/github`

## Running Locally

Copy the environment template and fill in provider credentials if you want social login:

```bash
cp .env.example .env
```

Start dependencies and the service:

```bash
docker compose up --build
```

Or run databases only, then start from IntelliJ or Maven:

```bash
docker compose up -d postgres redis
mvn spring-boot:run
```

Health check:

```bash
curl http://localhost:8080/actuator/health
```

## Configuration

| Variable | Purpose | Default |
| --- | --- | --- |
| `DATABASE_URL` | JDBC URL for PostgreSQL | `jdbc:postgresql://localhost:5432/auth_service` |
| `POSTGRES_USER` | Database username | `auth_user` |
| `POSTGRES_PASSWORD` | Database password | `auth_password` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `JWT_SECRET` | HMAC signing secret | dev-only value |
| `ACCESS_TOKEN_TTL` | JWT lifetime | `PT15M` |
| `REFRESH_TOKEN_TTL` | Refresh token lifetime | `P30D` |
| `GOOGLE_CLIENT_ID` | Google OAuth2 client id | blank |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret | blank |
| `GITHUB_CLIENT_ID` | GitHub OAuth2 client id | blank |
| `GITHUB_CLIENT_SECRET` | GitHub OAuth2 client secret | blank |
| `OAUTH2_SUCCESS_REDIRECT_URI` | Frontend callback after social login | `http://localhost:3000/auth/callback` |

## IntelliJ

Open this folder in IntelliJ IDEA and import it as a Maven project. The `.run/Auth Service.run.xml` run configuration starts `com.example.authservice.AuthServiceApplication`; use `docker compose up -d postgres redis` first.

## GitHub Repository

Create the remote repository and push:

```bash
git remote add origin git@github.com:<your-org-or-user>/auth-service.git
git branch -M main
git push -u origin main
```
