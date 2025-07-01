# Texnologia Logismikou

This repository contains the backend implementation for a music festival management system. It is built using Java, Spring Boot, and Maven.

## Building and Running

```bash
mvn spring-boot:run
```

The application exposes REST endpoints under the `/api` base path:

- `POST /api/festivals` – create a new festival
- `GET /api/festivals` – list all festivals
- `GET /api/festivals?name=&description=&startDate=&endDate=&location=` – search festivals with optional filters
- `GET /api/festivals/{id}` – view festival details
- `PUT /api/festivals/{id}` – update a festival
- `DELETE /api/festivals/{id}` – delete a festival
- `PUT /api/festivals/{id}/status` – advance festival status
- `POST /api/festivals/{festivalId}/performances` – create a performance for a festival
- `GET /api/festivals/{festivalId}/performances` – list performances of a festival
- `GET /api/performances/{id}` – view a performance
- `PUT /api/performances/{id}` – update a performance
- `POST /api/performances/{id}/submit` – submit a performance for review

## Testing

```bash
mvn test
```
