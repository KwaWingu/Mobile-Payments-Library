# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build all modules
./gradlew build

# Run all tests
./gradlew check

# Run tests with verbose output
./gradlew check -Dtest.verbose=true

# Run a single test class
./gradlew :kw-mpesa-payment-impl:test --tests "com.kwawingu.payments.c2b.CustomerToBusinessTransactionTest"

# Check formatting
./gradlew spotlessCheck -Denable.spotless=true

# Apply formatting
./gradlew spotlessApply -Denable.spotless=true

# Run ErrorProne static analysis
./gradlew testClasses -Dbuild.errorprone=true

# Run Checker Framework (nullness, optional, signedness)
./gradlew testClasses -Dbuild.checker=true

# Full CI pipeline (format → errorprone → checker → tests)
./scripts/ci/kw-mobile-lib-format-static-analysis-unit-test.sh
```

## Architecture

Multi-module Gradle project (Java 21). Each payment provider splits into two modules:
- `kw-{provider}-payment-api` — public interfaces, enums (`Environment`, `Market`, `Service`), and the `MobilePayment` interface
- `kw-{provider}-payment-impl` — concrete implementation; depends on its `-api` sibling plus SLF4J and Gson

Shared: `kw-mobile-payment-logback-config` is a runtime-only logging config pulled into every module.

### M-Pesa session flow (representative of all providers)

1. **Credential sourcing** — `MpesaKeyProvider` interface; `MpesaKeyProviderFromEnvironment` reads `MPESA_API_KEY` and `MPESA_PUBLIC_KEY` from env vars.
2. **API key encryption** — `MpesaApiKey.encrypt(MpesaPublicKey)` → RSA/ECB/PKCS1Padding → `MpesaEncryptedApiKey`.
3. **Session key retrieval** — `SessionKeyGenerator` calls `GET /getSession` with the encrypted API key as `Bearer` token → receives `MpesaSessionKey`.
4. **Session key encryption** — session key is RSA-encrypted again → `MpesaEncryptedSessionKey`, used as `Bearer` for all subsequent requests.
5. **Transaction** — `CustomerToBusinessTransaction` / `BusinessToBusinessTransaction` accept the encrypted session key + a typed payload (builder pattern) and POST to the appropriate endpoint.

`ApiEndpoint.getUrl(Service)` composes the full URL from `Environment` (SANDBOX/OPENAPI) × `Market` (VODACOM_TANZANIA, etc.) × `Service` path segment.

### Adding a new provider

Follow the M-Pesa pattern: create `kw-{provider}-payment-api` with the public contract, then `kw-{provider}-payment-impl` with session management, HTTP client, payload classes, and transaction classes. Register both in `settings.gradle`.

## Static analysis flags

Spotless enforces `googleJavaFormat` + license header from `spotless/HEADER.java`. All three static analysis tools are opt-in via system properties so normal `./gradlew build` is fast; CI enables them explicitly.

## Tests

Tests are integration tests that hit the M-Pesa sandbox. They require `MPESA_API_KEY` and `MPESA_PUBLIC_KEY` environment variables. There are no unit tests with mocks — the project intentionally avoids mocking the HTTP layer.
