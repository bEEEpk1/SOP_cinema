# CinemaSupchik final demo

Учебный multi-service проект кинотеатра по структуре преподавательских демо:

- `cinema-contract-api` — REST/GraphQL HTTP contract.
- `cinema-events-contract` — RabbitMQ event contract.
- `cinema-grpc-contract` — gRPC proto contract.
- `cinema-core` — основной REST/GraphQL сервис бронирований.
- `cinema-pricing` — gRPC pricing server.
- `cinema-loyalty` — gRPC loyalty server.
- `cinema-analytics-server` — gRPC analytics server для async enrichment: данные фильма, длительность и рекомендации по жанру.
- `cinema-enrichment-client` — RabbitMQ consumer + gRPC client + RabbitMQ publisher.
- `audit-service` — отдельный consumer всех событий для audit history.
- `notification-service` — отдельный consumer всех событий для WebSocket + email через Mailpit.
- `cinema-web` — React/Vite frontend для пользовательского сценария кинотеатра.

## Infrastructure

```bash
docker compose -f infra/docker/docker-compose.yml up -d
```

## Backend build

```bash
./mvnw clean package -DskipTests
```

Если IntelliJ не видит generated gRPC classes:

```bash
./mvnw -U -pl cinema-grpc-contract clean compile
./mvnw -U -pl cinema-core -am clean compile
```

## Backend run order

Можно запускать отдельными Run Configuration или IntelliJ Compound:

```bash
./mvnw spring-boot:run -pl cinema-pricing -am
./mvnw spring-boot:run -pl cinema-loyalty -am
./mvnw spring-boot:run -pl cinema-analytics-server -am
./mvnw spring-boot:run -pl cinema-enrichment-client -am
./mvnw spring-boot:run -pl audit-service -am
./mvnw spring-boot:run -pl notification-service -am
./mvnw spring-boot:run -pl cinema-core -am
```

Ports:

- cinema-core: `http://localhost:8080`
- audit-service: `http://localhost:8081`
- notification-service: `http://localhost:8082`
- cinema-pricing: HTTP `8083`, gRPC `9090`
- cinema-loyalty: HTTP `8084`, gRPC `9091`
- cinema-analytics-server: HTTP `8085`, gRPC `9092`
- cinema-enrichment-client: HTTP `8086`

## Frontend

```bash
cd cinema-web
cp .env.example .env
npm install
npm run dev
```



