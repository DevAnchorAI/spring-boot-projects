URL Shortener — Instructions

Overview

This document describes how to build, run, test and use the small URL-shortener microservice implemented in the `url-shortener/` module.

Checklist

- [ ] Prerequisites installed (Java 17, Maven)
- [ ] Build the `url-shortener` module
- [ ] Run unit & integration tests
- [ ] Start the application
- [ ] Create a short URL via API
- [ ] Follow a short URL (redirect)
- [ ] Inspect metadata (info endpoint)

Prerequisites

- Java 17
- Maven 3.6+
- (Optional) curl or HTTP client

Build

Run this from the repository root (where `pom.xml` for the new module lives in `url-shortener/pom.xml`):

```bash
mvn -pl url-shortener -am clean package
```

This compiles the `url-shortener` module and packages it. The `-am` flag builds required modules; adjust if running in a multi-module repository.

Run tests

To execute the unit and integration tests for this module only:

```bash
mvn -pl url-shortener test
```

Run the application

Start the service locally (module will respect `src/main/resources/application.yml` which uses H2 and port `8081` by default):

```bash
mvn -pl url-shortener spring-boot:run
```

Or run the shaded jar if packaged:

```bash
java -jar url-shortener/target/url-shortener-0.0.1-SNAPSHOT.jar
```

API

1) Shorten URL

- Endpoint: POST /api/shorten
- Request JSON: { "url": "https://example.com/long/path", "expiresAt": "2026-06-01T00:00:00Z" } (expiresAt optional)
- Response (201): { "code": "aZ0", "shortUrl": "http://localhost:8081/aZ0", "originalUrl": "https://..." }

Example (curl):

```bash
curl -X POST -H "Content-Type: application/json" \
  -d '{"url":"https://example.com/long/path"}' \
  http://localhost:8081/api/shorten
```

2) Follow short URL (redirect)

- Endpoint: GET /{code}
- Behavior: returns 302 Found with Location header set to the original URL.

Example:

```bash
curl -v http://localhost:8081/{code}
```

3) Get info about a code

- Endpoint: GET /api/info/{code}
- Response: JSON representation of the mapping (originalUrl, createdAt, expiresAt, clickCount, etc.)

Example:

```bash
curl http://localhost:8081/api/info/{code}
```

Notes & design decisions

- Short-code generation: Base62 encoding of the database-generated numeric id (deterministic, compact). If the same original URL is shortened again the implementation returns the existing code.
- Persistence: H2 in-memory for development; change `spring.datasource` and other `application.yml` properties to use a production DB (Postgres/MySQL) when required.
- Expiry: mappings can optionally include an `expiresAt` timestamp; expired codes return 404.
- Click tracking: `clickCount` increments on redirect.
- Short codes are case-sensitive and use a 62-character alphabet (0-9A-Za-z).

Troubleshooting

- If the server fails to start because port `8081` is in use, either stop the conflicting service or override the port:

```bash
mvn -pl url-shortener spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"
```

- To run tests with more verbose output, add `-DskipTests=false -X` to Maven commands.

Next steps / Enhancements

- Add rate-limiting and API keys to prevent abuse.
- Persist analytics in a separate store and provide aggregated metrics.
- Add a UI for shorter creation and management.
- Add Dockerfile and Kubernetes manifests for production deployment.

Contact

If you want changes to the API or packaging, tell me which behavior you'd like and I will update the module and tests accordingly.

