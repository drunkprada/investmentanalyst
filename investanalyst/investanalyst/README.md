# Investment Analyst Java API — development baseline

This is a **simulated trading backend**, not a brokerage or a verified market-data execution service. The current BUY/SELL endpoints accept a caller-provided **demo price**. They must not be described as executing at real market quotes. No Python analytics integration is implemented here.

## HTTP API

- `POST /portfolio`: `{"name":"Demo","initialCash":1000.00}` — returns 201 and portfolio data.
- `GET /portfolio`, `GET /portfolio/{id}`: list/get portfolios.
- `GET /portfolio/{id}/holdings`: list holdings.
- `POST /portfolio/{id}/buy`: `{"ticker":"AAPL","quantity":2,"price":25.00}` — returns 204.
- `POST /portfolio/{id}/sell`: same body, returns 204.
- `GET /portfolio/{id}/trades`: historical trades.

Request bodies use validated DTOs, not JPA entities. Trade totals must be exactly representable in USD cents. Invalid input returns 400, absent portfolios or holdings return 404, insufficient cash/shares return 409. Portfolio rows are locked during trade transactions to serialize changes to the same portfolio. Transactions record trades alongside cash and holdings changes.

## Local verification required

Run from `investanalyst/investanalyst`:

```sh
./mvnw test
DB_USERNAME=your_user DB_PASSWORD=your_password ./mvnw spring-boot:run
```

A PostgreSQL database named `investanalyst` is expected at localhost:5432. Use environment variables for credentials; never commit them. Exercise the endpoints against a disposable database and verify database state and rollback behavior.

**Verification status (2026-11-23):** changes were committed to GitHub and read back. Maven tests, Spring context startup, PostgreSQL integration and HTTP tests have **not** been executed in the assistant environment. The current tests are mostly unit tests with mocked repositories, not proof of persistence or transaction behavior.

## Known limitations and next decisions

- Demo prices are caller supplied; a trusted quote adapter with freshness/availability checks is required for realistic trading.
- Idempotency keys and duplicate execution protection are not implemented.
- Portfolio valuation, realized/unrealized P&L, authentication, Python analytics and integration tests remain outside this Java baseline.
- For assessed Software Testing work, select a small set of requirements and gather independently reproducible LO1–LO5 evidence; do not present these unexecuted tests as passing.
