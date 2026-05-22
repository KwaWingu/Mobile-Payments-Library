# Snippe-Backed Mobile Payments Library — Design Spec

**Date:** 2026-05-22  
**Status:** Approved  
**Scope:** Rewrite all provider implementations to use the Snippe unified payments API. Add card payment support. Remove Tigo-Pesa (renamed to Mixx by Yas).

---

## 1. Problem

Current library has one fully implemented provider (M-Pesa) with a complex session management flow: RSA encryption of API key, session key retrieval, RSA encryption of session key, then usage as Bearer. Every other provider module is an empty stub. Adding a new provider requires reimplementing this full auth + HTTP machinery.

Snippe provides a unified API that abstracts all provider-specific auth and session management behind simple Bearer token authentication. This makes implementations dramatically simpler and consistent.

---

## 2. Goals

- Replace M-Pesa direct implementation with Snippe-backed implementation.
- Implement Airtel Money, HaloPesa, Mixx by Yas (formerly Tigo-Pesa), and Card via Snippe.
- Both collection (C2B) and disbursements (B2B/payouts) for all mobile money providers.
- Card payment returns a hosted checkout URL (Snippe redirect flow).
- All implementations fully functional against Snippe sandbox.
- Single shared HTTP client module — no duplication across provider impls.

---

## 3. Non-Goals

- AzamPay and Selcom remain empty stubs (Snippe does not support them).
- No webhook handling in this library (consumer responsibility).
- No retry logic (consumer responsibility; Snippe idempotency keys cover retries).
- No async/reactive API — synchronous only.

---

## 4. Module Structure

### New module

| Module | Purpose |
|---|---|
| `kw-mobile-payment-client` | Shared Snippe HTTP client, auth, response models |

### Rewritten modules

| Module | Snippe network value |
|---|---|
| `kw-mpesa-payment-api/impl` | `mpesa` |
| `kw-airtel-payment-api/impl` | `airtel_money` |
| `kw-halopesa-payment-api/impl` | `halotel` |
| `kw-mixxbyyas-payment-api/impl` | `mixx_by_yas` |
| `kw-card-payment-api/impl` | `payment_type = "card"` |

### Removed modules

- `kw-tigopesa-payment-api` — company renamed; replaced by `kw-mixxbyyas-payment-api`
- `kw-tigopesa-payment-impl` — same

### Unchanged stubs

- `kw-azampay-payment-api/impl`
- `kw-selcom-payment-api/impl`
- `kw-mobile-payment-logback-config`

### Dependency graph

```
kw-mobile-payment-client
    └── depended on by all *-payment-impl modules

kw-{provider}-payment-impl
    ├── kw-{provider}-payment-api
    └── kw-mobile-payment-client
    └── kw-mobile-payment-logback-config (runtime)
```

---

## 5. Snippe API Reference

**Base URL:** `https://api.snippe.sh`  
**Auth:** `Authorization: Bearer <SNIPPE_API_KEY>`  
**Currency:** TZS only, integer value (smallest unit)  
**Idempotency:** `Idempotency-Key` header required on POST requests (use payment reference, ≤30 chars)

| Operation | Method | Endpoint |
|---|---|---|
| Collect (mobile money / card) | POST | `/v1/payments` |
| Disburse | POST | `/v1/payouts/send` |
| Check payout status | GET | `/v1/payouts/{reference}` |

**Collect request body:**

```json
{
  "payment_type": "mobile-money",
  "network": "mpesa",
  "amount": { "currency": "TZS", "value": 5000 },
  "customer": { "phone": "+255741000000" },
  "external_reference": "ORD-123",
  "description": "Order payment"
}
```

**Card collect request body:**

```json
{
  "payment_type": "card",
  "amount": { "currency": "TZS", "value": 5000 },
  "customer": { "email": "customer@example.com" },
  "external_reference": "ORD-123",
  "description": "Order payment"
}
```

**Disburse request body:**

```json
{
  "network": "mpesa",
  "amount": { "currency": "TZS", "value": 5000 },
  "recipient": { "phone": "+255741000000" },
  "external_reference": "REFUND-123",
  "description": "Refund"
}
```

**Success response:**

```json
{
  "status": "success",
  "code": 200,
  "data": {
    "reference": "SNP-...",
    "status": "pending",
    "amount": { "currency": "TZS", "value": 5000 },
    "created_at": "2026-05-22T10:00:00Z"
  }
}
```

**Error response:**

```json
{
  "status": "error",
  "code": 400,
  "error_code": "validation_error",
  "message": "..."
}
```

---

## 6. `kw-mobile-payment-client` — Shared Client Module

### 6.1 `SnippeApiKey`

Reads `SNIPPE_API_KEY` env var. Exposes `insertAuthorizationHeader(Map<String, String>)`. Sandbox vs production differentiated by which Snippe API key is configured — no `Environment` enum needed.

```java
public final class SnippeApiKey {
  private final String value;

  private SnippeApiKey(String value) { this.value = value; }

  public static SnippeApiKey fromEnvironment() {
    String key = System.getenv("SNIPPE_API_KEY");
    Objects.requireNonNull(key, "SNIPPE_API_KEY env var not set");
    return new SnippeApiKey(key);
  }

  public void insertAuthorizationHeader(Map<String, String> headers) {
    headers.put("Authorization", "Bearer " + value);
  }
}
```

### 6.2 Response Records

```java
public record PaymentResponse(
    String reference,
    String status,       // pending | completed | failed | voided | expired
    long amount,
    String currency,
    String createdAt,
    String completedAt   // nullable
) {}

public record PayoutResponse(
    String reference,
    String status,       // pending | completed | failed | reversed
    long amount,
    String currency
) {}
```

### 6.3 `SnippeHttpClient`

Single class. Uses Java `HttpClient` + Gson. Three public methods:

```java
public class SnippeHttpClient {
  private static final String BASE_URL = "https://api.snippe.sh";

  public PaymentResponse postPayment(
      SnippeApiKey apiKey,
      String idempotencyKey,
      Map<String, Object> body) throws IOException, InterruptedException;

  public PayoutResponse postPayout(
      SnippeApiKey apiKey,
      String idempotencyKey,
      Map<String, Object> body) throws IOException, InterruptedException;

  // Returns the checkout URL from response data
  public String postCardCheckout(
      SnippeApiKey apiKey,
      String idempotencyKey,
      Map<String, Object> body) throws IOException, InterruptedException;
}
```

Non-2xx responses throw `IOException` with message: `"Snippe error [<error_code>]: <message>"`.

---

## 7. Provider API Modules

Each provider api module contains:
1. `MobilePayment` interface (or `CardPayment` for card)
2. `CollectPayload` builder class
3. `DisbursePayload` builder class (mobile money only)

### 7.1 `MobilePayment` Interface

```java
public interface MobilePayment {
  PaymentResponse collect(CollectPayload payload) throws IOException, InterruptedException;
  PayoutResponse disburse(DisbursePayload payload) throws IOException, InterruptedException;
}
```

`CollectPayload` and `DisbursePayload` fields (identical across all mobile money providers):

| Field | Type | Required | Notes |
|---|---|---|---|
| `amount` | `long` | Yes | TZS integer |
| `phone` | `String` | Yes | E.164 format e.g. `+255741000000` |
| `reference` | `String` | Yes | ≤30 chars, used as idempotency key |
| `description` | `String` | Yes | Free text |

### 7.2 `CardPayment` Interface

```java
public interface CardPayment {
  String checkoutUrl(CardCollectPayload payload) throws IOException, InterruptedException;
}
```

`CardCollectPayload` fields:

| Field | Type | Required | Notes |
|---|---|---|---|
| `amount` | `long` | Yes | TZS integer |
| `email` | `String` | Yes | Customer email |
| `reference` | `String` | Yes | ≤30 chars |
| `description` | `String` | Yes | Free text |

---

## 8. Provider Implementation Modules

Each impl is a single class implementing `MobilePayment` (or `CardPayment`), with a `Builder` that accepts a `SnippeApiKey`.

### Example: `MpesaPayment`

```java
public class MpesaPayment implements MobilePayment {
  private final SnippeHttpClient client;
  private final SnippeApiKey apiKey;

  public PaymentResponse collect(MpesaCollectPayload payload)
      throws IOException, InterruptedException {
    Map<String, Object> body = Map.of(
        "payment_type",     "mobile-money",
        "network",          "mpesa",
        "amount",           Map.of("currency", "TZS", "value", payload.amount()),
        "customer",         Map.of("phone", payload.phone()),
        "external_reference", payload.reference(),
        "description",      payload.description()
    );
    return client.postPayment(apiKey, payload.reference(), body);
  }

  public PayoutResponse disburse(MpesaDisbursePayload payload)
      throws IOException, InterruptedException {
    Map<String, Object> body = Map.of(
        "network",          "mpesa",
        "amount",           Map.of("currency", "TZS", "value", payload.amount()),
        "recipient",        Map.of("phone", payload.phone()),
        "external_reference", payload.reference(),
        "description",      payload.description()
    );
    return client.postPayout(apiKey, payload.reference(), body);
  }

  public static class Builder {
    private SnippeApiKey apiKey;
    public Builder setApiKey(SnippeApiKey apiKey) { this.apiKey = apiKey; return this; }
    public MpesaPayment build() {
      Objects.requireNonNull(apiKey, "apiKey cannot be null");
      return new MpesaPayment(new SnippeHttpClient(), apiKey);
    }
  }
}
```

`AirtelPayment`, `HalopesaPayment`, `MixxByYasPayment` are identical, substituting `network` value.

### `CardPayment` impl

Returns checkout URL extracted from Snippe response `data.checkout_url`.

---

## 9. Error Handling

- Non-2xx HTTP → `IOException("Snippe error [<error_code>]: <message>")`
- JSON parse failure → `IOException("Failed to parse Snippe response")`
- Missing env var → `NullPointerException` (fast fail at startup)
- No custom exception hierarchy — consistent with existing library style

---

## 10. Testing

Tests are integration tests against Snippe sandbox. No mocks. Requires `SNIPPE_API_KEY` env var (sandbox key from Snippe dashboard).

One test class per provider impl:

| Test Class | Module |
|---|---|
| `MpesaPaymentTest` | `kw-mpesa-payment-impl` |
| `AirtelPaymentTest` | `kw-airtel-payment-impl` |
| `HalopesaPaymentTest` | `kw-halopesa-payment-impl` |
| `MixxByYasPaymentTest` | `kw-mixxbyyas-payment-impl` |
| `CardPaymentTest` | `kw-card-payment-impl` |

Each test:
1. Calls `collect()` with sandbox phone number / test card email
2. Asserts response `status` is `"pending"` or `"completed"`
3. Calls `disburse()` (mobile money only), asserts response `status` is `"pending"` or `"completed"`

---

## 11. Migration Notes

- `kw-tigopesa-payment-api/impl` directories deleted; removed from `settings.gradle`
- `kw-mixxbyyas-payment-api/impl` added to `settings.gradle`
- `kw-mobile-payment-client` added to `settings.gradle`
- Existing `kw-mpesa-payment-api` `MobilePayment` interface replaced (breaking change — old RSA/session API removed)
- `MPESA_API_KEY` and `MPESA_PUBLIC_KEY` env vars no longer needed; replaced by `SNIPPE_API_KEY`
- `.env/env.sh` should be updated to set `SNIPPE_API_KEY`
