# Auth Service

This is a learning project for building backend and cloud fundamentals through a standalone authentication service.

The goal is not to ship a finished auth platform quickly. The goal is to build each piece deliberately: understand the classes, write the code, break things, fix errors, and learn the backend patterns that make you internship-ready.

## Current State

This repository is intentionally minimal.

It contains:

- A base Spring Boot application.
- A Maven build file.
- Empty package directories for the major backend layers.
- A README to track the learning path.

It does not yet contain:

- User entities.
- Repositories.
- DTOs.
- Controllers.
- Services.
- Spring Security configuration.
- JWT logic.
- OAuth2 login.
- PostgreSQL setup.
- Redis refresh tokens.
- Docker or cloud deployment.

Those pieces will be added one at a time.

## Project Structure

```text
src/main/java/com/example/authservice
├── config
├── controller
├── dto
├── entity
├── exception
├── repository
├── security
├── service
└── AuthServiceApplication.java

src/main/resources
├── application.yml
└── db/migration
```

## Package Purpose

`entity`: Database-backed domain objects such as `User`, `Role`, and `Permission`.

`repository`: Spring Data interfaces for reading and writing entities.

`dto`: Request and response objects that define API input/output.

`controller`: REST endpoints such as registration, login, refresh, and account lookup.

`service`: Business logic that coordinates repositories, validation, and token behavior.

`security`: Spring Security, JWT filters, password handling, and OAuth2 integration.

`config`: Application configuration classes.

`exception`: Custom exceptions and API error handling.

`db/migration`: Future Flyway SQL migrations for PostgreSQL schema changes.

## Learning Roadmap

1. Run the base app.
2. Create a simple health or hello endpoint.
3. Add the first DTO and controller.
4. Add a `User` entity.
5. Add a `UserRepository`.
6. Connect PostgreSQL locally.
7. Build registration.
8. Build login with password hashing.
9. Add Spring Security basics.
10. Add JWT access tokens.
11. Add refresh tokens.
12. Add roles and permissions.
13. Add OAuth2 social login.
14. Add Docker.
15. Deploy to a cloud environment.

## Running Locally

From the project root:

```bash
mvn spring-boot:run
```

Run tests:

```bash
mvn test
```

## Working Agreement

This project is for learning. Codex should help with syntax, structure, explanations, debugging, and repetitive tasks when asked. The developer should make the core implementation decisions and write the important code with guidance.
