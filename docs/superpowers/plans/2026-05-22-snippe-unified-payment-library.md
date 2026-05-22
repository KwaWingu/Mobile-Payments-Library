# Snippe-Unified Payment Library Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rewrite all mobile payment provider implementations to use the Snippe unified API, replacing the complex per-provider session/RSA auth with simple Bearer token auth, and add card payment support.

**Architecture:** Single shared `kw-mobile-payment-client` module holds `SnippeHttpClient` (Java `HttpClient` + Gson) and response records. Each provider `kw-{provider}-payment-api` module defines its own `MobilePayment` interface and typed payload builders. Each `kw-{provider}-payment-impl` wraps `SnippeHttpClient` and sets the correct Snippe `network` field.

**Tech Stack:** Java 21, Gradle multi-module, Gson 2.10.1, SLF4J 2.0.9, JUnit Jupiter 5.10.0, Snippe REST API (`https://api.snippe.sh`).

---

## File Map

### New module: `kw-mobile-payment-client`
- Create: `kw-mobile-payment-client/build.gradle`
- Create: `kw-mobile-payment-client/src/main/java/com/kwawingu/payments/client/SnippeApiKey.java`
- Create: `kw-mobile-payment-client/src/main/java/com/kwawingu/payments/client/SnippeHttpClient.java`
- Create: `kw-mobile-payment-client/src/main/java/com/kwawingu/payments/client/response/PaymentResponse.java`
- Create: `kw-mobile-payment-client/src/main/java/com/kwawingu/payments/client/response/PayoutResponse.java`

### Rewritten: `kw-mpesa-payment-api`
- Delete: all files under `kw-mpesa-payment-api/src/`
- Create: `kw-mpesa-payment-api/src/main/java/com/kwawingu/payments/mpesa/MobilePayment.java`
- Create: `kw-mpesa-payment-api/src/main/java/com/kwawingu/payments/mpesa/MpesaCollectPayload.java`
- Create: `kw-mpesa-payment-api/src/main/java/com/kwawingu/payments/mpesa/MpesaDisbursePayload.java`
- Modify: `kw-mpesa-payment-api/build.gradle`

### Rewritten: `kw-mpesa-payment-impl`
- Delete: all files under `kw-mpesa-payment-impl/src/`
- Create: `kw-mpesa-payment-impl/src/main/java/com/kwawingu/payments/mpesa/MpesaPayment.java`
- Create: `kw-mpesa-payment-impl/src/test/java/com/kwawingu/payments/mpesa/MpesaPaymentTest.java`
- Modify: `kw-mpesa-payment-impl/build.gradle`

### New source: `kw-airtel-payment-api`
- Create: `kw-airtel-payment-api/src/main/java/com/kwawingu/payments/airtel/MobilePayment.java`
- Create: `kw-airtel-payment-api/src/main/java/com/kwawingu/payments/airtel/AirtelCollectPayload.java`
- Create: `kw-airtel-payment-api/src/main/java/com/kwawingu/payments/airtel/AirtelDisbursePayload.java`
- Modify: `kw-airtel-payment-api/build.gradle`

### New source: `kw-airtel-payment-impl`
- Create: `kw-airtel-payment-impl/src/main/java/com/kwawingu/payments/airtel/AirtelPayment.java`
- Create: `kw-airtel-payment-impl/src/test/java/com/kwawingu/payments/airtel/AirtelPaymentTest.java`
- Modify: `kw-airtel-payment-impl/build.gradle`

### New source: `kw-halopesa-payment-api`
- Create: `kw-halopesa-payment-api/src/main/java/com/kwawingu/payments/halopesa/MobilePayment.java`
- Create: `kw-halopesa-payment-api/src/main/java/com/kwawingu/payments/halopesa/HalopesaCollectPayload.java`
- Create: `kw-halopesa-payment-api/src/main/java/com/kwawingu/payments/halopesa/HalopesaDisbursePayload.java`
- Modify: `kw-halopesa-payment-api/build.gradle`

### New source: `kw-halopesa-payment-impl`
- Create: `kw-halopesa-payment-impl/src/main/java/com/kwawingu/payments/halopesa/HalopesaPayment.java`
- Create: `kw-halopesa-payment-impl/src/test/java/com/kwawingu/payments/halopesa/HalopesaPaymentTest.java`
- Modify: `kw-halopesa-payment-impl/build.gradle`

### New module: `kw-mixxbyyas-payment-api`
- Create: `kw-mixxbyyas-payment-api/build.gradle`
- Create: `kw-mixxbyyas-payment-api/src/main/java/com/kwawingu/payments/mixxbyyas/MobilePayment.java`
- Create: `kw-mixxbyyas-payment-api/src/main/java/com/kwawingu/payments/mixxbyyas/MixxByYasCollectPayload.java`
- Create: `kw-mixxbyyas-payment-api/src/main/java/com/kwawingu/payments/mixxbyyas/MixxByYasDisbursePayload.java`

### New module: `kw-mixxbyyas-payment-impl`
- Create: `kw-mixxbyyas-payment-impl/build.gradle`
- Create: `kw-mixxbyyas-payment-impl/src/main/java/com/kwawingu/payments/mixxbyyas/MixxByYasPayment.java`
- Create: `kw-mixxbyyas-payment-impl/src/test/java/com/kwawingu/payments/mixxbyyas/MixxByYasPaymentTest.java`

### New module: `kw-card-payment-api`
- Create: `kw-card-payment-api/build.gradle`
- Create: `kw-card-payment-api/src/main/java/com/kwawingu/payments/card/CardPayment.java`
- Create: `kw-card-payment-api/src/main/java/com/kwawingu/payments/card/CardCollectPayload.java`

### New module: `kw-card-payment-impl`
- Create: `kw-card-payment-impl/build.gradle`
- Create: `kw-card-payment-impl/src/main/java/com/kwawingu/payments/card/CardPaymentImpl.java`
- Create: `kw-card-payment-impl/src/test/java/com/kwawingu/payments/card/CardPaymentTest.java`

### Root changes
- Modify: `settings.gradle`
- Delete: `kw-tigopesa-payment-api/` and `kw-tigopesa-payment-impl/` directories
- Modify: `.env/env.sh`

---

## Task 1: Scaffold — settings.gradle, remove Tigo-Pesa, update env

**Files:**
- Modify: `settings.gradle`
- Delete: `kw-tigopesa-payment-api/` directory
- Delete: `kw-tigopesa-payment-impl/` directory
- Modify: `.env/env.sh`

- [ ] **Step 1: Remove Tigo-Pesa directories**

```bash
rm -rf kw-tigopesa-payment-api kw-tigopesa-payment-impl
```

- [ ] **Step 2: Rewrite settings.gradle**

Replace the full content of `settings.gradle`:

```groovy
rootProject.name = 'mobile-payments-library'

include('kw-mobile-payment-logback-config')
include('kw-mobile-payment-client')
include('kw-mpesa-payment-api')
include('kw-mpesa-payment-impl')
include('kw-airtel-payment-api')
include('kw-airtel-payment-impl')
include('kw-halopesa-payment-api')
include('kw-halopesa-payment-impl')
include('kw-mixxbyyas-payment-api')
include('kw-mixxbyyas-payment-impl')
include('kw-card-payment-api')
include('kw-card-payment-impl')
include('kw-azampay-payment-api')
include('kw-azampay-payment-impl')
include('kw-selcom-payment-api')
include('kw-selcom-payment-impl')
```

- [ ] **Step 3: Update .env/env.sh**

Replace `.env/env.sh` content:

```bash
export SNIPPE_API_KEY="<your-snippe-sandbox-api-key>"
```

Get your sandbox API key from the Snippe Dashboard at `https://dashboard.snippe.sh`. The sandbox key routes to test mode; no live funds move.

- [ ] **Step 4: Commit scaffold**

```bash
git add settings.gradle .env/env.sh
git commit -m "feat: scaffold Snippe-unified module structure"
```

---

## Task 2: `kw-mobile-payment-client` — shared HTTP client + auth + response types

**Files:**
- Create: `kw-mobile-payment-client/build.gradle`
- Create: `kw-mobile-payment-client/src/main/java/com/kwawingu/payments/client/SnippeApiKey.java`
- Create: `kw-mobile-payment-client/src/main/java/com/kwawingu/payments/client/response/PaymentResponse.java`
- Create: `kw-mobile-payment-client/src/main/java/com/kwawingu/payments/client/response/PayoutResponse.java`
- Create: `kw-mobile-payment-client/src/main/java/com/kwawingu/payments/client/SnippeHttpClient.java`

- [ ] **Step 1: Create module directory structure**

```bash
mkdir -p kw-mobile-payment-client/src/main/java/com/kwawingu/payments/client/response
```

- [ ] **Step 2: Create `kw-mobile-payment-client/build.gradle`**

```groovy
apply from: rootProject.file('gradle/plugin-java.gradle')

dependencies {
    implementation libs.slfj
    implementation libs.gson
}
```

- [ ] **Step 3: Create `SnippeApiKey.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.client;

import java.util.Map;
import java.util.Objects;

public final class SnippeApiKey {
  private final String value;

  private SnippeApiKey(String value) {
    this.value = value;
  }

  public static SnippeApiKey fromEnvironment() {
    String key = System.getenv("SNIPPE_API_KEY");
    Objects.requireNonNull(key, "SNIPPE_API_KEY environment variable is not set");
    return new SnippeApiKey(key);
  }

  public void insertAuthorizationHeader(Map<String, String> headers) {
    headers.put("Authorization", "Bearer " + value);
  }
}
```

- [ ] **Step 4: Create `PaymentResponse.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.client.response;

public record PaymentResponse(
    String reference,
    String status,
    long amount,
    String currency,
    String createdAt,
    String completedAt) {}
```

- [ ] **Step 5: Create `PayoutResponse.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.client.response;

public record PayoutResponse(
    String reference,
    String status,
    long amount,
    String currency) {}
```

- [ ] **Step 6: Create `SnippeHttpClient.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnippeHttpClient {
  private static final Logger LOG = LoggerFactory.getLogger(SnippeHttpClient.class);
  private static final String BASE_URL = "https://api.snippe.sh";

  private final HttpClient httpClient;
  private final Gson gson;

  public SnippeHttpClient() {
    this.httpClient = HttpClient.newHttpClient();
    this.gson = new Gson();
  }

  public PaymentResponse postPayment(
      SnippeApiKey apiKey, String idempotencyKey, Map<String, Object> body)
      throws IOException, InterruptedException {
    String responseBody = post("/v1/payments", apiKey, idempotencyKey, body);
    return parsePaymentResponse(responseBody);
  }

  public PayoutResponse postPayout(
      SnippeApiKey apiKey, String idempotencyKey, Map<String, Object> body)
      throws IOException, InterruptedException {
    String responseBody = post("/v1/payouts/send", apiKey, idempotencyKey, body);
    return parsePayoutResponse(responseBody);
  }

  public String postCardCheckout(
      SnippeApiKey apiKey, String idempotencyKey, Map<String, Object> body)
      throws IOException, InterruptedException {
    String responseBody = post("/v1/payments", apiKey, idempotencyKey, body);
    return parseCheckoutUrl(responseBody);
  }

  private String post(String path, SnippeApiKey apiKey, String idempotencyKey, Map<String, Object> body)
      throws IOException, InterruptedException {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");
    apiKey.insertAuthorizationHeader(headers);
    if (idempotencyKey != null && !idempotencyKey.isBlank()) {
      headers.put("Idempotency-Key", idempotencyKey);
    }

    HttpRequest.Builder requestBuilder =
        HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + path))
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)));
    headers.forEach(requestBuilder::header);

    HttpResponse<String> response =
        httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

    LOG.debug("Snippe {} {} → {}", "POST", path, response.statusCode());
    return response.body();
  }

  private PaymentResponse parsePaymentResponse(String body) throws IOException {
    ApiResponse r = gson.fromJson(body, ApiResponse.class);
    if (!"success".equals(r.status)) {
      throw new IOException("Snippe error [" + r.error_code + "]: " + r.message);
    }
    JsonObject data = r.data;
    JsonObject amount = data.getAsJsonObject("amount");
    String completedAt =
        data.has("completed_at") && !data.get("completed_at").isJsonNull()
            ? data.get("completed_at").getAsString()
            : null;
    return new PaymentResponse(
        data.get("reference").getAsString(),
        data.get("status").getAsString(),
        amount.get("value").getAsLong(),
        amount.get("currency").getAsString(),
        data.get("created_at").getAsString(),
        completedAt);
  }

  private PayoutResponse parsePayoutResponse(String body) throws IOException {
    ApiResponse r = gson.fromJson(body, ApiResponse.class);
    if (!"success".equals(r.status)) {
      throw new IOException("Snippe error [" + r.error_code + "]: " + r.message);
    }
    JsonObject data = r.data;
    JsonObject amount = data.getAsJsonObject("amount");
    return new PayoutResponse(
        data.get("reference").getAsString(),
        data.get("status").getAsString(),
        amount.get("value").getAsLong(),
        amount.get("currency").getAsString());
  }

  private String parseCheckoutUrl(String body) throws IOException {
    ApiResponse r = gson.fromJson(body, ApiResponse.class);
    if (!"success".equals(r.status)) {
      throw new IOException("Snippe error [" + r.error_code + "]: " + r.message);
    }
    JsonObject data = r.data;
    // Field name verified against Snippe sandbox — update if actual name differs
    if (data.has("checkout_url")) {
      return data.get("checkout_url").getAsString();
    }
    if (data.has("redirect_url")) {
      return data.get("redirect_url").getAsString();
    }
    throw new IOException("Snippe error: no checkout URL in response. Full data: " + data);
  }

  private static class ApiResponse {
    String status;
    int code;
    String error_code;
    String message;
    JsonObject data;
  }
}
```

- [ ] **Step 7: Verify module compiles**

```bash
./gradlew :kw-mobile-payment-client:compileJava
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 8: Commit**

```bash
git add kw-mobile-payment-client/
git commit -m "feat: add kw-mobile-payment-client with SnippeHttpClient and response types"
```

---

## Task 3: `kw-mpesa-payment-api` — delete old source, new interface + payloads

**Files:**
- Delete: `kw-mpesa-payment-api/src/`
- Modify: `kw-mpesa-payment-api/build.gradle`
- Create: `kw-mpesa-payment-api/src/main/java/com/kwawingu/payments/mpesa/MobilePayment.java`
- Create: `kw-mpesa-payment-api/src/main/java/com/kwawingu/payments/mpesa/MpesaCollectPayload.java`
- Create: `kw-mpesa-payment-api/src/main/java/com/kwawingu/payments/mpesa/MpesaDisbursePayload.java`

- [ ] **Step 1: Delete old M-Pesa API source**

```bash
rm -rf kw-mpesa-payment-api/src
mkdir -p kw-mpesa-payment-api/src/main/java/com/kwawingu/payments/mpesa
```

- [ ] **Step 2: Update `kw-mpesa-payment-api/build.gradle`**

```groovy
apply from: rootProject.file('gradle/plugin-java.gradle')

dependencies {
    api project(':kw-mobile-payment-client')
}
```

- [ ] **Step 3: Create `MobilePayment.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.mpesa;

import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;

public interface MobilePayment {
  PaymentResponse collect(MpesaCollectPayload payload) throws IOException, InterruptedException;

  PayoutResponse disburse(MpesaDisbursePayload payload) throws IOException, InterruptedException;
}
```

- [ ] **Step 4: Create `MpesaCollectPayload.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.mpesa;

import java.util.Objects;

public final class MpesaCollectPayload {
  private final long amount;
  private final String phone;
  private final String reference;
  private final String description;

  private MpesaCollectPayload(Builder builder) {
    this.amount = builder.amount;
    this.phone = builder.phone;
    this.reference = builder.reference;
    this.description = builder.description;
  }

  public long amount() {
    return amount;
  }

  public String phone() {
    return phone;
  }

  public String reference() {
    return reference;
  }

  public String description() {
    return description;
  }

  public static class Builder {
    private long amount;
    private String phone;
    private String reference;
    private String description;

    public Builder setAmount(long amount) {
      this.amount = amount;
      return this;
    }

    public Builder setPhone(String phone) {
      this.phone = phone;
      return this;
    }

    public Builder setReference(String reference) {
      this.reference = reference;
      return this;
    }

    public Builder setDescription(String description) {
      this.description = description;
      return this;
    }

    public MpesaCollectPayload build() {
      Objects.requireNonNull(phone, "phone cannot be null");
      Objects.requireNonNull(reference, "reference cannot be null");
      Objects.requireNonNull(description, "description cannot be null");
      if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
      if (reference.length() > 30)
        throw new IllegalArgumentException("reference must be ≤30 chars");
      return new MpesaCollectPayload(this);
    }
  }
}
```

- [ ] **Step 5: Create `MpesaDisbursePayload.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.mpesa;

import java.util.Objects;

public final class MpesaDisbursePayload {
  private final long amount;
  private final String phone;
  private final String reference;
  private final String description;

  private MpesaDisbursePayload(Builder builder) {
    this.amount = builder.amount;
    this.phone = builder.phone;
    this.reference = builder.reference;
    this.description = builder.description;
  }

  public long amount() {
    return amount;
  }

  public String phone() {
    return phone;
  }

  public String reference() {
    return reference;
  }

  public String description() {
    return description;
  }

  public static class Builder {
    private long amount;
    private String phone;
    private String reference;
    private String description;

    public Builder setAmount(long amount) {
      this.amount = amount;
      return this;
    }

    public Builder setPhone(String phone) {
      this.phone = phone;
      return this;
    }

    public Builder setReference(String reference) {
      this.reference = reference;
      return this;
    }

    public Builder setDescription(String description) {
      this.description = description;
      return this;
    }

    public MpesaDisbursePayload build() {
      Objects.requireNonNull(phone, "phone cannot be null");
      Objects.requireNonNull(reference, "reference cannot be null");
      Objects.requireNonNull(description, "description cannot be null");
      if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
      if (reference.length() > 30)
        throw new IllegalArgumentException("reference must be ≤30 chars");
      return new MpesaDisbursePayload(this);
    }
  }
}
```

- [ ] **Step 6: Verify module compiles**

```bash
./gradlew :kw-mpesa-payment-api:compileJava
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Commit**

```bash
git add kw-mpesa-payment-api/
git commit -m "feat(mpesa): rewrite api module with Snippe-backed interface and payloads"
```

---

## Task 4: `kw-mpesa-payment-impl` — delete old source, MpesaPayment + test

**Files:**
- Delete: `kw-mpesa-payment-impl/src/`
- Modify: `kw-mpesa-payment-impl/build.gradle`
- Create: `kw-mpesa-payment-impl/src/main/java/com/kwawingu/payments/mpesa/MpesaPayment.java`
- Create: `kw-mpesa-payment-impl/src/test/java/com/kwawingu/payments/mpesa/MpesaPaymentTest.java`

- [ ] **Step 1: Delete old M-Pesa impl source**

```bash
rm -rf kw-mpesa-payment-impl/src
mkdir -p kw-mpesa-payment-impl/src/main/java/com/kwawingu/payments/mpesa
mkdir -p kw-mpesa-payment-impl/src/test/java/com/kwawingu/payments/mpesa
```

- [ ] **Step 2: Update `kw-mpesa-payment-impl/build.gradle`**

```groovy
apply from: rootProject.file('gradle/plugin-java.gradle')

dependencies {
    implementation project(':kw-mpesa-payment-api')
    implementation project(':kw-mobile-payment-client')
}
```

- [ ] **Step 3: Create `MpesaPayment.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.mpesa;

import com.kwawingu.payments.client.SnippeApiKey;
import com.kwawingu.payments.client.SnippeHttpClient;
import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class MpesaPayment implements MobilePayment {
  private final SnippeHttpClient client;
  private final SnippeApiKey apiKey;

  private MpesaPayment(SnippeHttpClient client, SnippeApiKey apiKey) {
    this.client = client;
    this.apiKey = apiKey;
  }

  @Override
  public PaymentResponse collect(MpesaCollectPayload payload)
      throws IOException, InterruptedException {
    Map<String, Object> body =
        Map.of(
            "payment_type", "mobile-money",
            "network", "mpesa",
            "amount", Map.of("currency", "TZS", "value", payload.amount()),
            "customer", Map.of("phone", payload.phone()),
            "external_reference", payload.reference(),
            "description", payload.description());
    return client.postPayment(apiKey, payload.reference(), body);
  }

  @Override
  public PayoutResponse disburse(MpesaDisbursePayload payload)
      throws IOException, InterruptedException {
    Map<String, Object> body =
        Map.of(
            "network", "mpesa",
            "amount", Map.of("currency", "TZS", "value", payload.amount()),
            "recipient", Map.of("phone", payload.phone()),
            "external_reference", payload.reference(),
            "description", payload.description());
    return client.postPayout(apiKey, payload.reference(), body);
  }

  public static class Builder {
    private SnippeApiKey apiKey;

    public Builder setApiKey(SnippeApiKey apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    public MpesaPayment build() {
      Objects.requireNonNull(apiKey, "apiKey cannot be null");
      return new MpesaPayment(new SnippeHttpClient(), apiKey);
    }
  }
}
```

- [ ] **Step 4: Create `MpesaPaymentTest.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.mpesa;

import static org.junit.jupiter.api.Assertions.*;

import com.kwawingu.payments.client.SnippeApiKey;
import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MpesaPaymentTest {
  private static final Logger LOG = LoggerFactory.getLogger(MpesaPaymentTest.class);
  private static final Set<String> VALID_STATUSES =
      Set.of("pending", "completed", "failed", "voided", "expired");

  private MpesaPayment payment;

  @BeforeEach
  public void setUp() {
    payment = new MpesaPayment.Builder().setApiKey(SnippeApiKey.fromEnvironment()).build();
  }

  @Test
  public void testCollect() throws IOException, InterruptedException {
    MpesaCollectPayload payload =
        new MpesaCollectPayload.Builder()
            .setAmount(1000)
            .setPhone("+255741000000") // replace with a valid Snippe sandbox test number
            .setReference("TEST-MPESA-C2B-001")
            .setDescription("Test M-Pesa collection")
            .build();

    PaymentResponse response = payment.collect(payload);

    assertNotNull(response.reference(), "reference must not be null");
    assertFalse(response.reference().isBlank(), "reference must not be blank");
    assertTrue(
        VALID_STATUSES.contains(response.status()),
        "status must be one of " + VALID_STATUSES + ", got: " + response.status());
    assertEquals(1000L, response.amount());
    assertEquals("TZS", response.currency());
    LOG.info("collect response: ref={} status={}", response.reference(), response.status());
  }

  @Test
  public void testDisburse() throws IOException, InterruptedException {
    MpesaDisbursePayload payload =
        new MpesaDisbursePayload.Builder()
            .setAmount(1000)
            .setPhone("+255741000000") // replace with a valid Snippe sandbox test number
            .setReference("TEST-MPESA-B2B-001")
            .setDescription("Test M-Pesa disbursement")
            .build();

    PayoutResponse response = payment.disburse(payload);

    assertNotNull(response.reference(), "reference must not be null");
    assertFalse(response.reference().isBlank(), "reference must not be blank");
    Set<String> validPayoutStatuses = Set.of("pending", "completed", "failed", "reversed");
    assertTrue(
        validPayoutStatuses.contains(response.status()),
        "status must be one of " + validPayoutStatuses + ", got: " + response.status());
    assertEquals(1000L, response.amount());
    assertEquals("TZS", response.currency());
    LOG.info("disburse response: ref={} status={}", response.reference(), response.status());
  }
}
```

- [ ] **Step 5: Verify compilation**

```bash
./gradlew :kw-mpesa-payment-impl:compileJava :kw-mpesa-payment-impl:compileTestJava
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Run tests against sandbox**

Source the env file first, then run:

```bash
source .env/env.sh && ./gradlew :kw-mpesa-payment-impl:test
```

Expected: Both tests pass or fail with a known Snippe sandbox error (not a compilation/null error). Check logs for `ref=` and `status=` lines.

- [ ] **Step 7: Commit**

```bash
git add kw-mpesa-payment-impl/
git commit -m "feat(mpesa): implement MpesaPayment via Snippe API with sandbox tests"
```

---

## Task 5: `kw-airtel-payment-api/impl` — interface, payloads, implementation, test

**Files:**
- Modify: `kw-airtel-payment-api/build.gradle`
- Create: `kw-airtel-payment-api/src/main/java/com/kwawingu/payments/airtel/MobilePayment.java`
- Create: `kw-airtel-payment-api/src/main/java/com/kwawingu/payments/airtel/AirtelCollectPayload.java`
- Create: `kw-airtel-payment-api/src/main/java/com/kwawingu/payments/airtel/AirtelDisbursePayload.java`
- Modify: `kw-airtel-payment-impl/build.gradle`
- Create: `kw-airtel-payment-impl/src/main/java/com/kwawingu/payments/airtel/AirtelPayment.java`
- Create: `kw-airtel-payment-impl/src/test/java/com/kwawingu/payments/airtel/AirtelPaymentTest.java`

- [ ] **Step 1: Create source directories**

```bash
mkdir -p kw-airtel-payment-api/src/main/java/com/kwawingu/payments/airtel
mkdir -p kw-airtel-payment-impl/src/main/java/com/kwawingu/payments/airtel
mkdir -p kw-airtel-payment-impl/src/test/java/com/kwawingu/payments/airtel
```

- [ ] **Step 2: Update `kw-airtel-payment-api/build.gradle`**

```groovy
apply from: rootProject.file('gradle/plugin-java.gradle')

dependencies {
    api project(':kw-mobile-payment-client')
}
```

- [ ] **Step 3: Create `kw-airtel-payment-api/src/main/java/com/kwawingu/payments/airtel/MobilePayment.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.airtel;

import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;

public interface MobilePayment {
  PaymentResponse collect(AirtelCollectPayload payload) throws IOException, InterruptedException;

  PayoutResponse disburse(AirtelDisbursePayload payload) throws IOException, InterruptedException;
}
```

- [ ] **Step 4: Create `AirtelCollectPayload.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.airtel;

import java.util.Objects;

public final class AirtelCollectPayload {
  private final long amount;
  private final String phone;
  private final String reference;
  private final String description;

  private AirtelCollectPayload(Builder builder) {
    this.amount = builder.amount;
    this.phone = builder.phone;
    this.reference = builder.reference;
    this.description = builder.description;
  }

  public long amount() { return amount; }
  public String phone() { return phone; }
  public String reference() { return reference; }
  public String description() { return description; }

  public static class Builder {
    private long amount;
    private String phone;
    private String reference;
    private String description;

    public Builder setAmount(long amount) { this.amount = amount; return this; }
    public Builder setPhone(String phone) { this.phone = phone; return this; }
    public Builder setReference(String reference) { this.reference = reference; return this; }
    public Builder setDescription(String description) { this.description = description; return this; }

    public AirtelCollectPayload build() {
      Objects.requireNonNull(phone, "phone cannot be null");
      Objects.requireNonNull(reference, "reference cannot be null");
      Objects.requireNonNull(description, "description cannot be null");
      if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
      if (reference.length() > 30) throw new IllegalArgumentException("reference must be ≤30 chars");
      return new AirtelCollectPayload(this);
    }
  }
}
```

- [ ] **Step 5: Create `AirtelDisbursePayload.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.airtel;

import java.util.Objects;

public final class AirtelDisbursePayload {
  private final long amount;
  private final String phone;
  private final String reference;
  private final String description;

  private AirtelDisbursePayload(Builder builder) {
    this.amount = builder.amount;
    this.phone = builder.phone;
    this.reference = builder.reference;
    this.description = builder.description;
  }

  public long amount() { return amount; }
  public String phone() { return phone; }
  public String reference() { return reference; }
  public String description() { return description; }

  public static class Builder {
    private long amount;
    private String phone;
    private String reference;
    private String description;

    public Builder setAmount(long amount) { this.amount = amount; return this; }
    public Builder setPhone(String phone) { this.phone = phone; return this; }
    public Builder setReference(String reference) { this.reference = reference; return this; }
    public Builder setDescription(String description) { this.description = description; return this; }

    public AirtelDisbursePayload build() {
      Objects.requireNonNull(phone, "phone cannot be null");
      Objects.requireNonNull(reference, "reference cannot be null");
      Objects.requireNonNull(description, "description cannot be null");
      if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
      if (reference.length() > 30) throw new IllegalArgumentException("reference must be ≤30 chars");
      return new AirtelDisbursePayload(this);
    }
  }
}
```

- [ ] **Step 6: Update `kw-airtel-payment-impl/build.gradle`**

```groovy
apply from: rootProject.file('gradle/plugin-java.gradle')

dependencies {
    implementation project(':kw-airtel-payment-api')
    implementation project(':kw-mobile-payment-client')
}
```

- [ ] **Step 7: Create `AirtelPayment.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.airtel;

import com.kwawingu.payments.client.SnippeApiKey;
import com.kwawingu.payments.client.SnippeHttpClient;
import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class AirtelPayment implements MobilePayment {
  private final SnippeHttpClient client;
  private final SnippeApiKey apiKey;

  private AirtelPayment(SnippeHttpClient client, SnippeApiKey apiKey) {
    this.client = client;
    this.apiKey = apiKey;
  }

  @Override
  public PaymentResponse collect(AirtelCollectPayload payload)
      throws IOException, InterruptedException {
    Map<String, Object> body =
        Map.of(
            "payment_type", "mobile-money",
            "network", "airtel_money",
            "amount", Map.of("currency", "TZS", "value", payload.amount()),
            "customer", Map.of("phone", payload.phone()),
            "external_reference", payload.reference(),
            "description", payload.description());
    return client.postPayment(apiKey, payload.reference(), body);
  }

  @Override
  public PayoutResponse disburse(AirtelDisbursePayload payload)
      throws IOException, InterruptedException {
    Map<String, Object> body =
        Map.of(
            "network", "airtel_money",
            "amount", Map.of("currency", "TZS", "value", payload.amount()),
            "recipient", Map.of("phone", payload.phone()),
            "external_reference", payload.reference(),
            "description", payload.description());
    return client.postPayout(apiKey, payload.reference(), body);
  }

  public static class Builder {
    private SnippeApiKey apiKey;

    public Builder setApiKey(SnippeApiKey apiKey) { this.apiKey = apiKey; return this; }

    public AirtelPayment build() {
      Objects.requireNonNull(apiKey, "apiKey cannot be null");
      return new AirtelPayment(new SnippeHttpClient(), apiKey);
    }
  }
}
```

- [ ] **Step 8: Create `AirtelPaymentTest.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.airtel;

import static org.junit.jupiter.api.Assertions.*;

import com.kwawingu.payments.client.SnippeApiKey;
import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AirtelPaymentTest {
  private static final Logger LOG = LoggerFactory.getLogger(AirtelPaymentTest.class);
  private static final Set<String> VALID_STATUSES =
      Set.of("pending", "completed", "failed", "voided", "expired");

  private AirtelPayment payment;

  @BeforeEach
  public void setUp() {
    payment = new AirtelPayment.Builder().setApiKey(SnippeApiKey.fromEnvironment()).build();
  }

  @Test
  public void testCollect() throws IOException, InterruptedException {
    AirtelCollectPayload payload =
        new AirtelCollectPayload.Builder()
            .setAmount(1000)
            .setPhone("+255780000000") // replace with Snippe sandbox Airtel test number
            .setReference("TEST-AIRTEL-C2B-001")
            .setDescription("Test Airtel collection")
            .build();

    PaymentResponse response = payment.collect(payload);

    assertNotNull(response.reference());
    assertFalse(response.reference().isBlank());
    assertTrue(VALID_STATUSES.contains(response.status()),
        "status must be one of " + VALID_STATUSES + ", got: " + response.status());
    assertEquals(1000L, response.amount());
    assertEquals("TZS", response.currency());
    LOG.info("collect response: ref={} status={}", response.reference(), response.status());
  }

  @Test
  public void testDisburse() throws IOException, InterruptedException {
    AirtelDisbursePayload payload =
        new AirtelDisbursePayload.Builder()
            .setAmount(1000)
            .setPhone("+255780000000") // replace with Snippe sandbox Airtel test number
            .setReference("TEST-AIRTEL-B2B-001")
            .setDescription("Test Airtel disbursement")
            .build();

    PayoutResponse response = payment.disburse(payload);

    assertNotNull(response.reference());
    assertFalse(response.reference().isBlank());
    Set<String> validPayoutStatuses = Set.of("pending", "completed", "failed", "reversed");
    assertTrue(validPayoutStatuses.contains(response.status()),
        "status must be one of " + validPayoutStatuses + ", got: " + response.status());
    assertEquals(1000L, response.amount());
    assertEquals("TZS", response.currency());
    LOG.info("disburse response: ref={} status={}", response.reference(), response.status());
  }
}
```

- [ ] **Step 9: Compile and test**

```bash
./gradlew :kw-airtel-payment-impl:compileJava :kw-airtel-payment-impl:compileTestJava
source .env/env.sh && ./gradlew :kw-airtel-payment-impl:test
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 10: Commit**

```bash
git add kw-airtel-payment-api/ kw-airtel-payment-impl/
git commit -m "feat(airtel): implement AirtelPayment via Snippe API with sandbox tests"
```

---

## Task 6: `kw-halopesa-payment-api/impl` — interface, payloads, implementation, test

**Files:**
- Modify: `kw-halopesa-payment-api/build.gradle`
- Create: `kw-halopesa-payment-api/src/main/java/com/kwawingu/payments/halopesa/MobilePayment.java`
- Create: `kw-halopesa-payment-api/src/main/java/com/kwawingu/payments/halopesa/HalopesaCollectPayload.java`
- Create: `kw-halopesa-payment-api/src/main/java/com/kwawingu/payments/halopesa/HalopesaDisbursePayload.java`
- Modify: `kw-halopesa-payment-impl/build.gradle`
- Create: `kw-halopesa-payment-impl/src/main/java/com/kwawingu/payments/halopesa/HalopesaPayment.java`
- Create: `kw-halopesa-payment-impl/src/test/java/com/kwawingu/payments/halopesa/HalopesaPaymentTest.java`

- [ ] **Step 1: Create source directories**

```bash
mkdir -p kw-halopesa-payment-api/src/main/java/com/kwawingu/payments/halopesa
mkdir -p kw-halopesa-payment-impl/src/main/java/com/kwawingu/payments/halopesa
mkdir -p kw-halopesa-payment-impl/src/test/java/com/kwawingu/payments/halopesa
```

- [ ] **Step 2: Update `kw-halopesa-payment-api/build.gradle`**

```groovy
apply from: rootProject.file('gradle/plugin-java.gradle')

dependencies {
    api project(':kw-mobile-payment-client')
}
```

- [ ] **Step 3: Create `MobilePayment.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.halopesa;

import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;

public interface MobilePayment {
  PaymentResponse collect(HalopesaCollectPayload payload) throws IOException, InterruptedException;

  PayoutResponse disburse(HalopesaDisbursePayload payload) throws IOException, InterruptedException;
}
```

- [ ] **Step 4: Create `HalopesaCollectPayload.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.halopesa;

import java.util.Objects;

public final class HalopesaCollectPayload {
  private final long amount;
  private final String phone;
  private final String reference;
  private final String description;

  private HalopesaCollectPayload(Builder builder) {
    this.amount = builder.amount;
    this.phone = builder.phone;
    this.reference = builder.reference;
    this.description = builder.description;
  }

  public long amount() { return amount; }
  public String phone() { return phone; }
  public String reference() { return reference; }
  public String description() { return description; }

  public static class Builder {
    private long amount;
    private String phone;
    private String reference;
    private String description;

    public Builder setAmount(long amount) { this.amount = amount; return this; }
    public Builder setPhone(String phone) { this.phone = phone; return this; }
    public Builder setReference(String reference) { this.reference = reference; return this; }
    public Builder setDescription(String description) { this.description = description; return this; }

    public HalopesaCollectPayload build() {
      Objects.requireNonNull(phone, "phone cannot be null");
      Objects.requireNonNull(reference, "reference cannot be null");
      Objects.requireNonNull(description, "description cannot be null");
      if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
      if (reference.length() > 30) throw new IllegalArgumentException("reference must be ≤30 chars");
      return new HalopesaCollectPayload(this);
    }
  }
}
```

- [ ] **Step 5: Create `HalopesaDisbursePayload.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.halopesa;

import java.util.Objects;

public final class HalopesaDisbursePayload {
  private final long amount;
  private final String phone;
  private final String reference;
  private final String description;

  private HalopesaDisbursePayload(Builder builder) {
    this.amount = builder.amount;
    this.phone = builder.phone;
    this.reference = builder.reference;
    this.description = builder.description;
  }

  public long amount() { return amount; }
  public String phone() { return phone; }
  public String reference() { return reference; }
  public String description() { return description; }

  public static class Builder {
    private long amount;
    private String phone;
    private String reference;
    private String description;

    public Builder setAmount(long amount) { this.amount = amount; return this; }
    public Builder setPhone(String phone) { this.phone = phone; return this; }
    public Builder setReference(String reference) { this.reference = reference; return this; }
    public Builder setDescription(String description) { this.description = description; return this; }

    public HalopesaDisbursePayload build() {
      Objects.requireNonNull(phone, "phone cannot be null");
      Objects.requireNonNull(reference, "reference cannot be null");
      Objects.requireNonNull(description, "description cannot be null");
      if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
      if (reference.length() > 30) throw new IllegalArgumentException("reference must be ≤30 chars");
      return new HalopesaDisbursePayload(this);
    }
  }
}
```

- [ ] **Step 6: Update `kw-halopesa-payment-impl/build.gradle`**

```groovy
apply from: rootProject.file('gradle/plugin-java.gradle')

dependencies {
    implementation project(':kw-halopesa-payment-api')
    implementation project(':kw-mobile-payment-client')
}
```

- [ ] **Step 7: Create `HalopesaPayment.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.halopesa;

import com.kwawingu.payments.client.SnippeApiKey;
import com.kwawingu.payments.client.SnippeHttpClient;
import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class HalopesaPayment implements MobilePayment {
  private final SnippeHttpClient client;
  private final SnippeApiKey apiKey;

  private HalopesaPayment(SnippeHttpClient client, SnippeApiKey apiKey) {
    this.client = client;
    this.apiKey = apiKey;
  }

  @Override
  public PaymentResponse collect(HalopesaCollectPayload payload)
      throws IOException, InterruptedException {
    Map<String, Object> body =
        Map.of(
            "payment_type", "mobile-money",
            "network", "halotel",
            "amount", Map.of("currency", "TZS", "value", payload.amount()),
            "customer", Map.of("phone", payload.phone()),
            "external_reference", payload.reference(),
            "description", payload.description());
    return client.postPayment(apiKey, payload.reference(), body);
  }

  @Override
  public PayoutResponse disburse(HalopesaDisbursePayload payload)
      throws IOException, InterruptedException {
    Map<String, Object> body =
        Map.of(
            "network", "halotel",
            "amount", Map.of("currency", "TZS", "value", payload.amount()),
            "recipient", Map.of("phone", payload.phone()),
            "external_reference", payload.reference(),
            "description", payload.description());
    return client.postPayout(apiKey, payload.reference(), body);
  }

  public static class Builder {
    private SnippeApiKey apiKey;

    public Builder setApiKey(SnippeApiKey apiKey) { this.apiKey = apiKey; return this; }

    public HalopesaPayment build() {
      Objects.requireNonNull(apiKey, "apiKey cannot be null");
      return new HalopesaPayment(new SnippeHttpClient(), apiKey);
    }
  }
}
```

- [ ] **Step 8: Create `HalopesaPaymentTest.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.halopesa;

import static org.junit.jupiter.api.Assertions.*;

import com.kwawingu.payments.client.SnippeApiKey;
import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HalopesaPaymentTest {
  private static final Logger LOG = LoggerFactory.getLogger(HalopesaPaymentTest.class);
  private static final Set<String> VALID_STATUSES =
      Set.of("pending", "completed", "failed", "voided", "expired");

  private HalopesaPayment payment;

  @BeforeEach
  public void setUp() {
    payment = new HalopesaPayment.Builder().setApiKey(SnippeApiKey.fromEnvironment()).build();
  }

  @Test
  public void testCollect() throws IOException, InterruptedException {
    HalopesaCollectPayload payload =
        new HalopesaCollectPayload.Builder()
            .setAmount(1000)
            .setPhone("+255620000000") // replace with Snippe sandbox HaloPesa test number
            .setReference("TEST-HALO-C2B-001")
            .setDescription("Test HaloPesa collection")
            .build();

    PaymentResponse response = payment.collect(payload);

    assertNotNull(response.reference());
    assertFalse(response.reference().isBlank());
    assertTrue(VALID_STATUSES.contains(response.status()),
        "status must be one of " + VALID_STATUSES + ", got: " + response.status());
    assertEquals(1000L, response.amount());
    assertEquals("TZS", response.currency());
    LOG.info("collect response: ref={} status={}", response.reference(), response.status());
  }

  @Test
  public void testDisburse() throws IOException, InterruptedException {
    HalopesaDisbursePayload payload =
        new HalopesaDisbursePayload.Builder()
            .setAmount(1000)
            .setPhone("+255620000000") // replace with Snippe sandbox HaloPesa test number
            .setReference("TEST-HALO-B2B-001")
            .setDescription("Test HaloPesa disbursement")
            .build();

    PayoutResponse response = payment.disburse(payload);

    assertNotNull(response.reference());
    assertFalse(response.reference().isBlank());
    Set<String> validPayoutStatuses = Set.of("pending", "completed", "failed", "reversed");
    assertTrue(validPayoutStatuses.contains(response.status()),
        "status must be one of " + validPayoutStatuses + ", got: " + response.status());
    assertEquals(1000L, response.amount());
    assertEquals("TZS", response.currency());
    LOG.info("disburse response: ref={} status={}", response.reference(), response.status());
  }
}
```

- [ ] **Step 9: Compile and test**

```bash
./gradlew :kw-halopesa-payment-impl:compileJava :kw-halopesa-payment-impl:compileTestJava
source .env/env.sh && ./gradlew :kw-halopesa-payment-impl:test
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 10: Commit**

```bash
git add kw-halopesa-payment-api/ kw-halopesa-payment-impl/
git commit -m "feat(halopesa): implement HalopesaPayment via Snippe API with sandbox tests"
```

---

## Task 7: `kw-mixxbyyas-payment-api/impl` — new module, interface, payloads, implementation, test

**Files:**
- Create: `kw-mixxbyyas-payment-api/build.gradle`
- Create: `kw-mixxbyyas-payment-api/src/main/java/com/kwawingu/payments/mixxbyyas/MobilePayment.java`
- Create: `kw-mixxbyyas-payment-api/src/main/java/com/kwawingu/payments/mixxbyyas/MixxByYasCollectPayload.java`
- Create: `kw-mixxbyyas-payment-api/src/main/java/com/kwawingu/payments/mixxbyyas/MixxByYasDisbursePayload.java`
- Create: `kw-mixxbyyas-payment-impl/build.gradle`
- Create: `kw-mixxbyyas-payment-impl/src/main/java/com/kwawingu/payments/mixxbyyas/MixxByYasPayment.java`
- Create: `kw-mixxbyyas-payment-impl/src/test/java/com/kwawingu/payments/mixxbyyas/MixxByYasPaymentTest.java`

- [ ] **Step 1: Create module directories**

```bash
mkdir -p kw-mixxbyyas-payment-api/src/main/java/com/kwawingu/payments/mixxbyyas
mkdir -p kw-mixxbyyas-payment-impl/src/main/java/com/kwawingu/payments/mixxbyyas
mkdir -p kw-mixxbyyas-payment-impl/src/test/java/com/kwawingu/payments/mixxbyyas
```

- [ ] **Step 2: Create `kw-mixxbyyas-payment-api/build.gradle`**

```groovy
apply from: rootProject.file('gradle/plugin-java.gradle')

dependencies {
    api project(':kw-mobile-payment-client')
}
```

- [ ] **Step 3: Create `MobilePayment.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.mixxbyyas;

import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;

public interface MobilePayment {
  PaymentResponse collect(MixxByYasCollectPayload payload) throws IOException, InterruptedException;

  PayoutResponse disburse(MixxByYasDisbursePayload payload) throws IOException, InterruptedException;
}
```

- [ ] **Step 4: Create `MixxByYasCollectPayload.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.mixxbyyas;

import java.util.Objects;

public final class MixxByYasCollectPayload {
  private final long amount;
  private final String phone;
  private final String reference;
  private final String description;

  private MixxByYasCollectPayload(Builder builder) {
    this.amount = builder.amount;
    this.phone = builder.phone;
    this.reference = builder.reference;
    this.description = builder.description;
  }

  public long amount() { return amount; }
  public String phone() { return phone; }
  public String reference() { return reference; }
  public String description() { return description; }

  public static class Builder {
    private long amount;
    private String phone;
    private String reference;
    private String description;

    public Builder setAmount(long amount) { this.amount = amount; return this; }
    public Builder setPhone(String phone) { this.phone = phone; return this; }
    public Builder setReference(String reference) { this.reference = reference; return this; }
    public Builder setDescription(String description) { this.description = description; return this; }

    public MixxByYasCollectPayload build() {
      Objects.requireNonNull(phone, "phone cannot be null");
      Objects.requireNonNull(reference, "reference cannot be null");
      Objects.requireNonNull(description, "description cannot be null");
      if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
      if (reference.length() > 30) throw new IllegalArgumentException("reference must be ≤30 chars");
      return new MixxByYasCollectPayload(this);
    }
  }
}
```

- [ ] **Step 5: Create `MixxByYasDisbursePayload.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.mixxbyyas;

import java.util.Objects;

public final class MixxByYasDisbursePayload {
  private final long amount;
  private final String phone;
  private final String reference;
  private final String description;

  private MixxByYasDisbursePayload(Builder builder) {
    this.amount = builder.amount;
    this.phone = builder.phone;
    this.reference = builder.reference;
    this.description = builder.description;
  }

  public long amount() { return amount; }
  public String phone() { return phone; }
  public String reference() { return reference; }
  public String description() { return description; }

  public static class Builder {
    private long amount;
    private String phone;
    private String reference;
    private String description;

    public Builder setAmount(long amount) { this.amount = amount; return this; }
    public Builder setPhone(String phone) { this.phone = phone; return this; }
    public Builder setReference(String reference) { this.reference = reference; return this; }
    public Builder setDescription(String description) { this.description = description; return this; }

    public MixxByYasDisbursePayload build() {
      Objects.requireNonNull(phone, "phone cannot be null");
      Objects.requireNonNull(reference, "reference cannot be null");
      Objects.requireNonNull(description, "description cannot be null");
      if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
      if (reference.length() > 30) throw new IllegalArgumentException("reference must be ≤30 chars");
      return new MixxByYasDisbursePayload(this);
    }
  }
}
```

- [ ] **Step 6: Create `kw-mixxbyyas-payment-impl/build.gradle`**

```groovy
apply from: rootProject.file('gradle/plugin-java.gradle')

dependencies {
    implementation project(':kw-mixxbyyas-payment-api')
    implementation project(':kw-mobile-payment-client')
}
```

- [ ] **Step 7: Create `MixxByYasPayment.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.mixxbyyas;

import com.kwawingu.payments.client.SnippeApiKey;
import com.kwawingu.payments.client.SnippeHttpClient;
import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class MixxByYasPayment implements MobilePayment {
  private final SnippeHttpClient client;
  private final SnippeApiKey apiKey;

  private MixxByYasPayment(SnippeHttpClient client, SnippeApiKey apiKey) {
    this.client = client;
    this.apiKey = apiKey;
  }

  @Override
  public PaymentResponse collect(MixxByYasCollectPayload payload)
      throws IOException, InterruptedException {
    Map<String, Object> body =
        Map.of(
            "payment_type", "mobile-money",
            "network", "mixx_by_yas",
            "amount", Map.of("currency", "TZS", "value", payload.amount()),
            "customer", Map.of("phone", payload.phone()),
            "external_reference", payload.reference(),
            "description", payload.description());
    return client.postPayment(apiKey, payload.reference(), body);
  }

  @Override
  public PayoutResponse disburse(MixxByYasDisbursePayload payload)
      throws IOException, InterruptedException {
    Map<String, Object> body =
        Map.of(
            "network", "mixx_by_yas",
            "amount", Map.of("currency", "TZS", "value", payload.amount()),
            "recipient", Map.of("phone", payload.phone()),
            "external_reference", payload.reference(),
            "description", payload.description());
    return client.postPayout(apiKey, payload.reference(), body);
  }

  public static class Builder {
    private SnippeApiKey apiKey;

    public Builder setApiKey(SnippeApiKey apiKey) { this.apiKey = apiKey; return this; }

    public MixxByYasPayment build() {
      Objects.requireNonNull(apiKey, "apiKey cannot be null");
      return new MixxByYasPayment(new SnippeHttpClient(), apiKey);
    }
  }
}
```

- [ ] **Step 8: Create `MixxByYasPaymentTest.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.mixxbyyas;

import static org.junit.jupiter.api.Assertions.*;

import com.kwawingu.payments.client.SnippeApiKey;
import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MixxByYasPaymentTest {
  private static final Logger LOG = LoggerFactory.getLogger(MixxByYasPaymentTest.class);
  private static final Set<String> VALID_STATUSES =
      Set.of("pending", "completed", "failed", "voided", "expired");

  private MixxByYasPayment payment;

  @BeforeEach
  public void setUp() {
    payment = new MixxByYasPayment.Builder().setApiKey(SnippeApiKey.fromEnvironment()).build();
  }

  @Test
  public void testCollect() throws IOException, InterruptedException {
    MixxByYasCollectPayload payload =
        new MixxByYasCollectPayload.Builder()
            .setAmount(1000)
            .setPhone("+255710000000") // replace with Snippe sandbox Mixx by Yas test number
            .setReference("TEST-MBY-C2B-001")
            .setDescription("Test Mixx by Yas collection")
            .build();

    PaymentResponse response = payment.collect(payload);

    assertNotNull(response.reference());
    assertFalse(response.reference().isBlank());
    assertTrue(VALID_STATUSES.contains(response.status()),
        "status must be one of " + VALID_STATUSES + ", got: " + response.status());
    assertEquals(1000L, response.amount());
    assertEquals("TZS", response.currency());
    LOG.info("collect response: ref={} status={}", response.reference(), response.status());
  }

  @Test
  public void testDisburse() throws IOException, InterruptedException {
    MixxByYasDisbursePayload payload =
        new MixxByYasDisbursePayload.Builder()
            .setAmount(1000)
            .setPhone("+255710000000") // replace with Snippe sandbox Mixx by Yas test number
            .setReference("TEST-MBY-B2B-001")
            .setDescription("Test Mixx by Yas disbursement")
            .build();

    PayoutResponse response = payment.disburse(payload);

    assertNotNull(response.reference());
    assertFalse(response.reference().isBlank());
    Set<String> validPayoutStatuses = Set.of("pending", "completed", "failed", "reversed");
    assertTrue(validPayoutStatuses.contains(response.status()),
        "status must be one of " + validPayoutStatuses + ", got: " + response.status());
    assertEquals(1000L, response.amount());
    assertEquals("TZS", response.currency());
    LOG.info("disburse response: ref={} status={}", response.reference(), response.status());
  }
}
```

- [ ] **Step 9: Compile and test**

```bash
./gradlew :kw-mixxbyyas-payment-impl:compileJava :kw-mixxbyyas-payment-impl:compileTestJava
source .env/env.sh && ./gradlew :kw-mixxbyyas-payment-impl:test
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 10: Commit**

```bash
git add kw-mixxbyyas-payment-api/ kw-mixxbyyas-payment-impl/
git commit -m "feat(mixxbyyas): implement MixxByYasPayment via Snippe API with sandbox tests"
```

---

## Task 8: `kw-card-payment-api/impl` — interface, payload, implementation, test

**Files:**
- Create: `kw-card-payment-api/build.gradle`
- Create: `kw-card-payment-api/src/main/java/com/kwawingu/payments/card/CardPayment.java`
- Create: `kw-card-payment-api/src/main/java/com/kwawingu/payments/card/CardCollectPayload.java`
- Create: `kw-card-payment-impl/build.gradle`
- Create: `kw-card-payment-impl/src/main/java/com/kwawingu/payments/card/CardPaymentImpl.java`
- Create: `kw-card-payment-impl/src/test/java/com/kwawingu/payments/card/CardPaymentTest.java`

- [ ] **Step 1: Create module directories**

```bash
mkdir -p kw-card-payment-api/src/main/java/com/kwawingu/payments/card
mkdir -p kw-card-payment-impl/src/main/java/com/kwawingu/payments/card
mkdir -p kw-card-payment-impl/src/test/java/com/kwawingu/payments/card
```

- [ ] **Step 2: Create `kw-card-payment-api/build.gradle`**

```groovy
apply from: rootProject.file('gradle/plugin-java.gradle')

dependencies {
    api project(':kw-mobile-payment-client')
}
```

- [ ] **Step 3: Create `CardPayment.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.card;

import java.io.IOException;

public interface CardPayment {
  String checkoutUrl(CardCollectPayload payload) throws IOException, InterruptedException;
}
```

- [ ] **Step 4: Create `CardCollectPayload.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.card;

import java.util.Objects;

public final class CardCollectPayload {
  private final long amount;
  private final String email;
  private final String reference;
  private final String description;

  private CardCollectPayload(Builder builder) {
    this.amount = builder.amount;
    this.email = builder.email;
    this.reference = builder.reference;
    this.description = builder.description;
  }

  public long amount() { return amount; }
  public String email() { return email; }
  public String reference() { return reference; }
  public String description() { return description; }

  public static class Builder {
    private long amount;
    private String email;
    private String reference;
    private String description;

    public Builder setAmount(long amount) { this.amount = amount; return this; }
    public Builder setEmail(String email) { this.email = email; return this; }
    public Builder setReference(String reference) { this.reference = reference; return this; }
    public Builder setDescription(String description) { this.description = description; return this; }

    public CardCollectPayload build() {
      Objects.requireNonNull(email, "email cannot be null");
      Objects.requireNonNull(reference, "reference cannot be null");
      Objects.requireNonNull(description, "description cannot be null");
      if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
      if (reference.length() > 30) throw new IllegalArgumentException("reference must be ≤30 chars");
      return new CardCollectPayload(this);
    }
  }
}
```

- [ ] **Step 5: Create `kw-card-payment-impl/build.gradle`**

```groovy
apply from: rootProject.file('gradle/plugin-java.gradle')

dependencies {
    implementation project(':kw-card-payment-api')
    implementation project(':kw-mobile-payment-client')
}
```

- [ ] **Step 6: Create `CardPaymentImpl.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.card;

import com.kwawingu.payments.client.SnippeApiKey;
import com.kwawingu.payments.client.SnippeHttpClient;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class CardPaymentImpl implements CardPayment {
  private final SnippeHttpClient client;
  private final SnippeApiKey apiKey;

  private CardPaymentImpl(SnippeHttpClient client, SnippeApiKey apiKey) {
    this.client = client;
    this.apiKey = apiKey;
  }

  @Override
  public String checkoutUrl(CardCollectPayload payload) throws IOException, InterruptedException {
    Map<String, Object> body =
        Map.of(
            "payment_type", "card",
            "amount", Map.of("currency", "TZS", "value", payload.amount()),
            "customer", Map.of("email", payload.email()),
            "external_reference", payload.reference(),
            "description", payload.description());
    return client.postCardCheckout(apiKey, payload.reference(), body);
  }

  public static class Builder {
    private SnippeApiKey apiKey;

    public Builder setApiKey(SnippeApiKey apiKey) { this.apiKey = apiKey; return this; }

    public CardPaymentImpl build() {
      Objects.requireNonNull(apiKey, "apiKey cannot be null");
      return new CardPaymentImpl(new SnippeHttpClient(), apiKey);
    }
  }
}
```

- [ ] **Step 7: Create `CardPaymentTest.java`**

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.card;

import static org.junit.jupiter.api.Assertions.*;

import com.kwawingu.payments.client.SnippeApiKey;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CardPaymentTest {
  private static final Logger LOG = LoggerFactory.getLogger(CardPaymentTest.class);

  private CardPaymentImpl payment;

  @BeforeEach
  public void setUp() {
    payment = new CardPaymentImpl.Builder().setApiKey(SnippeApiKey.fromEnvironment()).build();
  }

  @Test
  public void testCheckoutUrl() throws IOException, InterruptedException {
    CardCollectPayload payload =
        new CardCollectPayload.Builder()
            .setAmount(5000)
            .setEmail("test@example.com")
            .setReference("TEST-CARD-001")
            .setDescription("Test card checkout")
            .build();

    String url = payment.checkoutUrl(payload);

    assertNotNull(url, "checkout URL must not be null");
    assertFalse(url.isBlank(), "checkout URL must not be blank");
    assertTrue(url.startsWith("https://"),
        "checkout URL must be https, got: " + url);
    LOG.info("card checkout URL: {}", url);
  }
}
```

- [ ] **Step 8: Compile and test**

```bash
./gradlew :kw-card-payment-impl:compileJava :kw-card-payment-impl:compileTestJava
source .env/env.sh && ./gradlew :kw-card-payment-impl:test
```

Expected: `BUILD SUCCESSFUL`. If the test fails because the URL field name in the Snippe response is not `checkout_url` or `redirect_url`, check the raw response by temporarily adding `LOG.info("raw response: {}", body)` in `SnippeHttpClient.parseCheckoutUrl()`, then update the field name.

- [ ] **Step 9: Commit**

```bash
git add kw-card-payment-api/ kw-card-payment-impl/
git commit -m "feat(card): implement CardPaymentImpl via Snippe API with sandbox test"
```

---

## Task 9: Full build verification

- [ ] **Step 1: Run full build**

```bash
./gradlew build
```

Expected: `BUILD SUCCESSFUL`. All modules compile, all tests pass.

- [ ] **Step 2: Run spotless check**

```bash
./gradlew spotlessCheck -Denable.spotless=true
```

If formatting violations exist, fix them:

```bash
./gradlew spotlessApply -Denable.spotless=true
```

- [ ] **Step 3: Run full CI script**

```bash
./scripts/ci/kw-mobile-lib-format-static-analysis-unit-test.sh
```

Expected: All stages pass.

- [ ] **Step 4: Final commit if spotless made changes**

```bash
git add -A
git commit -m "style: apply spotless formatting across all new modules"
```
