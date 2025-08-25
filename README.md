# Brokerage Firm Challenge — Spring Boot Backend

A production-style Spring Boot 3 application implementing a tiny brokerage backend where **employees (admins)** can create/list/cancel orders for customers, and (bonus) **customers** can login and access only their own data. Orders are **against TRY** and affect **usable** balances when created, and **total** balances when matched/canceled.

## Features
- Create order (BUY/SELL) — created as **PENDING**, reserves usable balances accordingly
- List orders by customer + date range
- Cancel order — only if **PENDING**; releases reserves
- List assets per customer
- **Admin auth** with HTTP Basic (username/password from `application.yml`)
- **H2** in-memory DB
- **Bonus 1**: Customer table, `/api/auth/login` returns JWT; `/api/me/**` endpoints secured with Bearer token
- **Bonus 2**: Admin `/api/orders/{id}/match` endpoint to settle an order (no order matching against another order)

## Build & Run
Requires Java 21 and Maven 3.9+

```bash
mvn clean package
java -jar target/brokerage-0.0.1-SNAPSHOT.jar
```

Server runs at `http://localhost:8080` and H2 console at `/h2-console`.

### Admin credentials (HTTP Basic)
Configured in `src/main/resources/application.yml`

- **username:** `admin`
- **password:** `admin123`

> NOTE: The password is stored as BCrypt. If you change it, update the BCrypt hash.

### Demo customer
On first run, a demo customer is seeded:
- username: `alice`, password: `alice123`, customerId: `CUST-ALICE`
- Assets: `TRY = 100000.00`, `AKBNK = 50`

## API
### Admin (Basic auth)
- **Create order**
  ```http
  POST /api/orders
  Authorization: Basic base64(admin:admin123)
  Content-Type: application/json

  {
    "customerId": "CUST-ALICE",
    "assetName": "AKBNK",
    "side": "BUY",
    "size": 10,
    "price": 10.50
  }
  ```
- **List orders**
  ```http
  GET /api/orders?customerId=CUST-ALICE&from=2025-01-01T00:00:00Z&to=2030-01-01T00:00:00Z
  Authorization: Basic ...
  ```
- **Cancel order**
  ```http
  DELETE /api/orders/{orderId}
  Authorization: Basic ...
  ```
- **Match order** (Bonus 2)
  ```http
  POST /api/orders/{orderId}/match
  Authorization: Basic ...
  ```
- **List assets**
  ```http
  GET /api/assets?customerId=CUST-ALICE
  Authorization: Basic ...
  ```

### Customer (JWT Bearer) — Bonus 1
- **Login**
  ```http
  POST /api/auth/login
  Content-Type: application/json

  { "username": "alice", "password": "alice123" }
  ```
  Response:
  ```json
  { "token": "eyJhbGciOi...", "customerId": "CUST-ALICE" }
  ```
- **List my assets**
  ```http
  GET /api/me/assets
  Authorization: Bearer <token>
  ```
- **List my orders**
  ```http
  GET /api/me/orders?from=2025-01-01T00:00:00Z&to=2030-01-01T00:00:00Z
  Authorization: Bearer <token>
  ```
- **Place my BUY/SELL orders**
  ```http
  POST /api/me/orders/buy?asset=AKBNK&size=2&price=10.00
  Authorization: Bearer <token>
  ```
  ```http
  POST /api/me/orders/sell?asset=AKBNK&size=1&price=12.00
  Authorization: Bearer <token>
  ```

## Data & Business Rules
- **TRY is an asset** in `assets` table. No extra table.
- On **BUY create**: check `TRY.usableSize >= price * size`; subtract from `TRY.usableSize` (reserve).
- On **SELL create**: check `ASSET.usableSize >= size`; subtract from `ASSET.usableSize` (reserve).
- On **CANCEL**: release the reservation back to the relevant `usableSize`.
- On **MATCH**:
  - BUY: `TRY.size -= price*size` (usable already reduced on create), `ASSET.size += size`, `ASSET.usableSize += size`.
  - SELL: `ASSET.size -= size` (usable already reduced on create), `TRY.size += price*size`, `TRY.usableSize += price*size`.

## Tests
Run:
```bash
mvn test
```
Includes service-level tests covering create/match/cancel flows and balance updates.

## Notes
- Concurrency is handled with optimistic locking on `Asset.version` and row-level version bumps via `findWithLock...`.
- Monetary/quantity fields use `BigDecimal(22,4)` to support TRY decimals and integer share sizes.
- You can create more customers via `/api/auth/signup-demo` (dev only).
