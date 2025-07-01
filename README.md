# Texnologia Logismikou

This repository contains the backend implementation for a music festival management system. It is built using Java, Spring Boot, and Maven.

## Building and Running

```bash
mvn spring-boot:run
```

The application exposes basic REST endpoints:

- `POST /festivals` – create a new festival
- `GET /festivals` – list all festivals
- `GET /festivals?name=&description=&startDate=&endDate=&location=` – search festivals with optional filters
- `GET /festivals/{id}` – view festival details
- `PUT /festivals/{id}` – update a festival
- `DELETE /festivals/{id}` – delete a festival
- `PUT /festivals/{id}/status` – update festival status
- `POST /festivals/{festivalId}/performances` – create a performance for a festival
- `GET /festivals/{festivalId}/performances` – list performances of a festival
- `GET /festivals/{festivalId}/performances/{id}` – view a performance

## Testing

```bash
mvn test
```
