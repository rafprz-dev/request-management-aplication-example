# Request Management Application Example
Backend service that manages the lifecycle of requests. Each request moves through a defined set of states, and every transition must be governed by the accompanying state diagram.

# Tech stack
- Java 25
- PostgreSQL
- Spring Boot 4.1.1
- Spring Boot Starter Web
- Spring Boot Starter Validation
- Spring Boot Liquibase for database migrations
- Swagger annotations
- Jackson Databind addons
- Mapstruct for mapping between DTOs and entities
- Spring Boot Starter Test for unit, e2e, integration tests
- Spring Boot Starter WebMVC Testing for controller tests
- Spring Boot Starter Data JPA Testing for repository tests
- H2 Database In memory for testing
- OpenApi Generator

Application behavior is defined by a state diagram that specifies the valid states and transitions for requests. The application enforces these rules to ensure that requests can only move through the defined states in a valid manner.

- Two tables are used to store the requests and their transitions.
- The request table contains the current state of the request, while the transition table contains the history of all transitions that have occurred for each request.
- There is one controller that exposes the endpoints for creating, updating, and retrieving requests. The controller uses a service layer to handle the business logic of managing requests and their transitions. The service layer uses a repository layer to interact with the database.