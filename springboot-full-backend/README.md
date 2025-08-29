# Spring Boot Full Backend (Webhook flow) - Ready-to-run

This Spring Boot application implements the assignment flow:

- On startup it POSTs to the `generateWebhook` endpoint to get a `webhook` URL and `accessToken`.
- It determines which question is assigned from the last two digits of `app.regNo` (odd/even).
- It sends your final SQL query as `{ "finalQuery": "..." }` to the returned `webhook` using the `Authorization` header set to the `accessToken`.

## Setup

1. Edit `src/main/resources/application.properties`:
   - `app.name`
   - `app.regNo`
   - `app.email`
   - Optionally set `app.finalQuery` OR create/edit `final-query.sql` at project root.

2. Build and run:
```bash
mvn clean package -DskipTests
java -jar target/springboot-full-backend-0.0.1-SNAPSHOT.jar
```
or during development:
```bash
mvn spring-boot:run
```

## Notes

- Uses `WebClient` (Spring WebFlux) to call external endpoints.
- Flow runs automatically on startup; there are no controllers/endpoints to trigger it.
- The second POST includes the `Authorization` header with the token returned by the first call.
