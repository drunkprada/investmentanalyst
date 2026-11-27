# Investment Analyst

Investment Analyst is a simulated stock trading and portfolio risk analysis application built using Java and Spring Boot.

The application allows users to create portfolios with virtual USD balances, execute simulated buy/sell transactions, track holdings and calculate historical financial risk metrics.


## Technology Stack

- Java 21
- Spring Boot 4.1.2
- PostgreSQL and Spring Data JPA
- Maven
- JUnit 5 and Mockito
- Spring Boot Test and MockMvc
- JaCoCo
- PIT Mutation Testing
- GitHub Actions
- HTML, CSS and JavaScript frontend

## Application Features

**Portfolio management:** Create portfolios, track available cash and retrieve holdings.

**Simulated trading:** Execute virtual buy/sell transactions using validated demonstration prices and maintain transaction history.

**Risk analysis:** Calculate daily returns, annualised volatility, historical 95% Value at Risk and portfolio concentration.

**Market data:** Retrieve historical stock prices using the Twelve Data API.

**Frontend:** A basic interface for portfolio management, simulated trading and viewing risk metrics.

The application is intended for demonstration and testing, not real-money trading.

## Running the Application

### Requirements

- Java 21
- PostgreSQL
- Valid PostgreSQL credentials
- A Twelve Data API key for live market-data requests

The Maven project is located inside:

`investanalyst/investanalyst/`

### Environment Variables

Configure the following variables:

```bash
export DB_USERNAME="your_username"
export DB_PASSWORD="your_password"
export MARKET_DATA_API_KEY="your_api_key"
```

Create the PostgreSQL development database named `investanalyst` before starting the application.

### Start the Application

On macOS or Linux, from the Maven project directory:

```bash
./mvnw spring-boot:run
```

Open:

http://localhost:8080

## Testing

The project includes 51 automated tests covering portfolio validation, simulated trading operations and financial risk calculations.

### Run the Test Suite

Tests require a separate PostgreSQL database named `investanalyst_test`.

```bash
./mvnw clean verify
```

The test profile uses `create-drop`, so never run it against a database containing important data.

### JaCoCo Coverage

After successful verification:

```bash
open target/site/jacoco/index.html
```

### PIT Mutation Testing

```bash
./mvnw test-compile org.pitest:pitest-maven:mutationCoverage
```

PIT is configured to test `RiskAnalysisService` and its corresponding unit tests.

### Continuous Integration

GitHub Actions automatically runs the Maven test suite with a PostgreSQL 16 service on configured pushes and pull requests.

[View GitHub Actions](https://github.com/drunkprada/investmentanalyst/actions)

## Software Testing Evidence

The supporting evidence for the Software Testing portfolio is organised under `evidence/`.

| Resource | Description |
|---|---|
| [Test Plan](evidence/test-plan.md) |

| [JaCoCo Screenshot](evidence/jacoco_coverage_report.png) | Instruction and branch coverage |
| [PIT Screenshot](evidence/pit_mutation_report.png) | Focused mutation-testing results |
| [GitHub Actions](https://github.com/drunkprada/investmentanalyst/actions) | Automated test execution |
| [Review Commit](https://github.com/drunkprada/investmentanalyst/commit/eb57ca1676199b79ee1bb3d0cdb8e32eb68de614) | Removal of ineffective unit test |

