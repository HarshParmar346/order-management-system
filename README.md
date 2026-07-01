
# EvenFlow — Event-Driven Order Management System

A microservices-based order management system built with **Spring Boot**, **Apache Kafka**,
and the **transactional outbox pattern**. An order moves through inventory reservation and
payment processing via asynchronous, choreographed events instead of synchronous service calls.

## Architecture

```
                POST /orders
                     │
                     ▼
             ┌───────────────┐        outbox table          ┌──────────────┐
             │ order-service │ ───────(polled every 5s)───▶ │    Kafka     │
    ┌──────▶│  (Oracle DB)  │                               │ order-created│
    |        └───────────────┘                               └──────┬───────┘
    |                ▲                                               │
    |                │                                               ▼
    |                │                                       ┌─────────────────┐
    |                │                                       │ inventory-service│
    |                │                                       │ (Oracle + Redis  │
    |                │                                       │  distributed     │
    |                │                                       │  lock per SKU)   │
    |                │                                       └────────┬─────────┘
    |                │                                                │
    |                │                              inventory-reserved / inventory-failed
    |                │                                                │
    |                │        inventory-failed ──▶ order CANCELLED    │
    |                └────────────────────────────────────────────────┤
  payment-success                                                     ▼
    |        ┌─────────────────┐    inventory-reserved        ┌─────────────────┐
    |        │ payment-service │ ◀───────────────────────────  │      Kafka      │
    |        │  (Oracle DB)    │                               └────────┬────────┘
    |        └────────┬────────┘                                        │
    |                 │                                                  │
    |   payment-success / payment-failed                  payment-success / payment-failed
    |                 │                                                  │
    |                 ▼                                                  ▼
    |        ┌────────────────┐                                ┌────────────────────┐
    └────────│      Kafka     │ ────────────────────────────▶ | notification-service│
             └────────────────┘                                │    (Oracle DB)      │
                                                               └────────────────────┘
```

**`common-events`** is a shared library module (event DTOs + Kafka topic names) that every
service depends on, so producers and consumers stay on the same event contract.

### Event flow (happy path)

1. Client calls `POST /orders` on **order-service**. The order is saved as `CREATED` and an
   `OrderCreatedEvent` is written to an **outbox** table in the *same DB transaction* as the
   order — this guarantees the event is never lost even if Kafka is briefly unavailable.
2. A `@Scheduled` `OutboxScheduler` polls the outbox every 5s and publishes any unpublished
   events to the `order-created` Kafka topic.
3. **inventory-service** consumes `order-created`, takes a short-lived Redis lock per product
   (`SETNX` with a 5s TTL) to avoid racing concurrent reservations for the same SKU, then either:
   - reserves stock and publishes `inventory-reserved`, or
   - publishes `inventory-failed` if stock is insufficient.
4. **order-service** also consumes `inventory-failed` directly and marks the order `CANCELLED`
   — this short-circuits the saga immediately on a stock failure, without waiting on payment.
5. **payment-service** consumes `inventory-reserved`, simulates a payment gateway call
   (~90% success rate), and publishes `payment-success` or `payment-failed`. It is idempotent —
   if a payment already exists for the order it short-circuits.
6. **order-service** also consumes `payment-success` / `payment-failed` and updates the order's
   final status (`COMPLETED` / `FAILED`).
7. **notification-service** independently consumes `payment-success` / `payment-failed` and
   persists a notification record.

Each service owns its own database tables (`orders` + `outbox`, `inventory`, `payments`,
`notifications`) — there's no shared schema, only shared events.

### Services

| Service              | Port | Responsibility                                              | Datastores    |
|----------------------|------|---------------------------------------------------------------|---------------|
| order-service        | 8080 | REST API, order lifecycle, transactional outbox               | Oracle        |
| inventory-service     | 8081 | Stock reservation with a Redis-backed per-SKU lock             | Oracle, Redis |
| payment-service       | 8082 | Simulated payment processing, idempotent by `orderId`         | Oracle        |
| notification-service  | 8083 | Notification persistence on payment outcome                    | Oracle        |
| common-events         | —    | Shared event DTOs + Kafka topic constants (library, no server) | —             |

## Running locally

```bash
docker compose up --build
```

This brings up Zookeeper, Kafka, Redis, an Oracle Free container, and all four services.
Oracle takes 1–2 minutes to become healthy on first boot (it's building the database); the
Spring Boot services will wait for it via `depends_on: condition: service_healthy`.

Once everything is up:

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId": 1, "productId": 100, "quantity": 2}'
```

You should see the order created, then — within a few seconds, once the outbox scheduler
fires — inventory reserved, a payment attempt, and a notification recorded, all driven purely
by Kafka events.

> **Note:** in production this project is designed to pull DB / Kafka / Redis configuration
> from an external Spring Cloud Config server (see `spring.cloud.config.name` in each
> `application.yaml`). That config server isn't part of this repo, so `docker-compose.yaml`
> disables the config-server lookup (`SPRING_CLOUD_CONFIG_ENABLED=false`) and injects the
> equivalent settings as environment variables instead, purely for local/dev use.

### Running tests

```bash
mvn test
```

Unit tests cover the service layer of every module (order creation + outbox persistence,
outbox publishing including a corrupted-payload case, inventory reservation including the
insufficient-stock and Redis-lock-contention paths, payment success/failure/idempotency, and
notification persistence) using JUnit 5 + Mockito. They run against mocked repositories/Kafka/
Redis, so no Docker or DB is required.
