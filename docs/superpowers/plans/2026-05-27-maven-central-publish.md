# Maven Central Publishing + Provider Completion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the missing Azampay and Selcom payment providers, then configure all 14 modules for signed publication to Maven Central (central.sonatype.com) under `com.kwawingu`.

**Architecture:** Two sequential tracks: (1) implement Azampay + Selcom as identical copies of the existing mobile provider pattern (MobilePayment interface + payload builders + Snippe HTTP calls), then (2) add `gradle.properties` for group/version, a shared `plugin-publish.gradle` for POM metadata + signing, apply it to all publishable modules, and update the release workflow to publish to Central Portal.

**Tech Stack:** Java 21, Gradle multi-module (Groovy DSL), JUnit 5, `maven-publish` + `signing` Gradle plugins, Sonatype Central Publishing Plugin `com.sonatype.central.publish:0.6.0`, Snippe REST API (sandbox tests).

---

## File Map

**Create (Azampay):**
- `kw-azampay-payment-api/src/main/java/com/kwawingu/payments/azampay/MobilePayment.java`
- `kw-azampay-payment-api/src/main/java/com/kwawingu/payments/azampay/AzampayCollectPayload.java`
- `kw-azampay-payment-api/src/main/java/com/kwawingu/payments/azampay/AzampayDisbursePayload.java`
- `kw-azampay-payment-impl/src/main/java/com/kwawingu/payments/azampay/AzampayPayment.java`
- `kw-azampay-payment-impl/src/test/java/com/kwawingu/payments/azampay/AzampayPaymentTest.java`

**Create (Selcom):**
- `kw-selcom-payment-api/src/main/java/com/kwawingu/payments/selcom/MobilePayment.java`
- `kw-selcom-payment-api/src/main/java/com/kwawingu/payments/selcom/SelcomCollectPayload.java`
- `kw-selcom-payment-api/src/main/java/com/kwawingu/payments/selcom/SelcomDisbursePayload.java`
- `kw-selcom-payment-impl/src/main/java/com/kwawingu/payments/selcom/SelcomPayment.java`
- `kw-selcom-payment-impl/src/test/java/com/kwawingu/payments/selcom/SelcomPaymentTest.java`

**Create (Publishing):**
- `gradle.properties`
- `gradle/plugin-publish.gradle`

**Modify (Azampay build files):**
- `kw-azampay-payment-api/build.gradle` — add `kw-mobile-payment-client` dep
- `kw-azampay-payment-impl/build.gradle` — add `kw-azampay-payment-api` + `kw-mobile-payment-client` deps

**Modify (Selcom build files):**
- `kw-selcom-payment-api/build.gradle` — add `kw-mobile-payment-client` dep
- `kw-selcom-payment-impl/build.gradle` — replace broken content with correct deps

**Modify (Publishing — add plugin-publish.gradle to 14 modules):**
- `kw-mobile-payment-client/build.gradle`
- `kw-mpesa-payment-api/build.gradle`, `kw-mpesa-payment-impl/build.gradle`
- `kw-airtel-payment-api/build.gradle`, `kw-airtel-payment-impl/build.gradle`
- `kw-halopesa-payment-api/build.gradle`, `kw-halopesa-payment-impl/build.gradle`
- `kw-mixxbyyas-payment-api/build.gradle`, `kw-mixxbyyas-payment-impl/build.gradle`
- `kw-azampay-payment-api/build.gradle`, `kw-azampay-payment-impl/build.gradle`
- `kw-selcom-payment-api/build.gradle`, `kw-selcom-payment-impl/build.gradle`
- `kw-card-payment-api/build.gradle`, `kw-card-payment-impl/build.gradle`

**Modify (Root):**
- `gradle/libs.versions.toml` — add `sonatype-central-publish` plugin entry
- `build.gradle` — apply `sonatype-central-publish` plugin
- `.github/workflows/kw-mobile-library-release-production.yml` — replace GitHub Packages publish step

> `kw-mobile-payment-logback-config` is NOT published — do not add plugin-publish.gradle there.

---

## Task 1: Azampay API module — build.gradle + interface

**Files:**
- Modify: `kw-azampay-payment-api/build.gradle`
- Create: `kw-azampay-payment-api/src/main/java/com/kwawingu/payments/azampay/MobilePayment.java`

- [ ] **Step 1: Fix azampay API build.gradle**

Replace the entire content of `kw-azampay-payment-api/build.gradle` with:

```groovy
apply from: rootProject.file('gradle/plugin-java.gradle')

dependencies {
    api project(':kw-mobile-payment-client')
}
```

- [ ] **Step 2: Create the MobilePayment interface**

Create `kw-azampay-payment-api/src/main/java/com/kwawingu/payments/azampay/MobilePayment.java`:

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.azampay;

import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;

public interface MobilePayment {
  PaymentResponse collect(AzampayCollectPayload payload) throws IOException, InterruptedException;

  PayoutResponse disburse(AzampayDisbursePayload payload) throws IOException, InterruptedException;
}
```

---

## Task 2: Azampay API module — payload classes

**Files:**
- Create: `kw-azampay-payment-api/src/main/java/com/kwawingu/payments/azampay/AzampayCollectPayload.java`
- Create: `kw-azampay-payment-api/src/main/java/com/kwawingu/payments/azampay/AzampayDisbursePayload.java`

- [ ] **Step 1: Create AzampayCollectPayload**

Create `kw-azampay-payment-api/src/main/java/com/kwawingu/payments/azampay/AzampayCollectPayload.java`:

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.azampay;

import org.checkerframework.checker.nullness.qual.Nullable;

public final class AzampayCollectPayload {
  private final long amount;
  private final String phone;
  private final String reference;
  private final String description;

  @SuppressWarnings(
      "nullness") // builder.build() validates non-null before calling this constructor
  private AzampayCollectPayload(Builder builder) {
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
    private @Nullable String phone = null;
    private @Nullable String reference = null;
    private @Nullable String description = null;

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

    public AzampayCollectPayload build() {
      if (phone == null) throw new NullPointerException("phone cannot be null");
      if (reference == null) throw new NullPointerException("reference cannot be null");
      if (description == null) throw new NullPointerException("description cannot be null");
      if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
      if (reference.length() > 30)
        throw new IllegalArgumentException("reference must be ≤30 chars");
      return new AzampayCollectPayload(this);
    }
  }
}
```

- [ ] **Step 2: Create AzampayDisbursePayload**

Create `kw-azampay-payment-api/src/main/java/com/kwawingu/payments/azampay/AzampayDisbursePayload.java`:

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.azampay;

import org.checkerframework.checker.nullness.qual.Nullable;

public final class AzampayDisbursePayload {
  private final long amount;
  private final String phone;
  private final String reference;
  private final String description;

  @SuppressWarnings(
      "nullness") // builder.build() validates non-null before calling this constructor
  private AzampayDisbursePayload(Builder builder) {
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
    private @Nullable String phone = null;
    private @Nullable String reference = null;
    private @Nullable String description = null;

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

    public AzampayDisbursePayload build() {
      if (phone == null) throw new NullPointerException("phone cannot be null");
      if (reference == null) throw new NullPointerException("reference cannot be null");
      if (description == null) throw new NullPointerException("description cannot be null");
      if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
      if (reference.length() > 30)
        throw new IllegalArgumentException("reference must be ≤30 chars");
      return new AzampayDisbursePayload(this);
    }
  }
}
```

---

## Task 3: Azampay Impl module — implementation + build.gradle

**Files:**
- Modify: `kw-azampay-payment-impl/build.gradle`
- Create: `kw-azampay-payment-impl/src/main/java/com/kwawingu/payments/azampay/AzampayPayment.java`

- [ ] **Step 1: Fix azampay impl build.gradle**

Replace the entire content of `kw-azampay-payment-impl/build.gradle` with:

```groovy
apply from: rootProject.file('gradle/plugin-java.gradle')

dependencies {
    implementation project(':kw-azampay-payment-api')
    implementation project(':kw-mobile-payment-client')
    testImplementation libs.slfj
}
```

- [ ] **Step 2: Create AzampayPayment**

Create directory first: `kw-azampay-payment-impl/src/main/java/com/kwawingu/payments/azampay/`

Create `kw-azampay-payment-impl/src/main/java/com/kwawingu/payments/azampay/AzampayPayment.java`:

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.azampay;

import com.kwawingu.payments.client.SnippeApiKey;
import com.kwawingu.payments.client.SnippeHttpClient;
import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AzampayPayment implements MobilePayment {
  private final SnippeHttpClient client;
  private final SnippeApiKey apiKey;

  private AzampayPayment(SnippeHttpClient client, SnippeApiKey apiKey) {
    this.client = client;
    this.apiKey = apiKey;
  }

  @Override
  public PaymentResponse collect(AzampayCollectPayload payload)
      throws IOException, InterruptedException {
    Map<String, Object> body =
        Map.of(
            "payment_type", "mobile",
            "details", Map.of("amount", payload.amount(), "currency", "TZS"),
            "phone_number", payload.phone(),
            "customer", Map.of("firstname", "", "lastname", "", "email", ""),
            "metadata", Map.of("order_id", payload.reference()));
    return client.postPayment(apiKey, payload.reference(), body);
  }

  @Override
  public PayoutResponse disburse(AzampayDisbursePayload payload)
      throws IOException, InterruptedException {
    Map<String, Object> body =
        Map.of(
            "payment_type", "mobile",
            "details", Map.of("amount", payload.amount(), "currency", "TZS"),
            "phone_number", payload.phone(),
            "description", payload.description(),
            "metadata", Map.of("order_id", payload.reference()));
    return client.postPayout(apiKey, payload.reference(), body);
  }

  public static class Builder {
    private @Nullable SnippeApiKey apiKey = null;

    public Builder setApiKey(SnippeApiKey apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    @SuppressWarnings("nullness") // null-checked above before passing to constructor
    public AzampayPayment build() {
      if (apiKey == null) throw new NullPointerException("apiKey cannot be null");
      return new AzampayPayment(new SnippeHttpClient(), apiKey);
    }
  }
}
```

---

## Task 4: Azampay Impl module — test

**Files:**
- Create: `kw-azampay-payment-impl/src/test/java/com/kwawingu/payments/azampay/AzampayPaymentTest.java`

- [ ] **Step 1: Create AzampayPaymentTest**

Create `kw-azampay-payment-impl/src/test/java/com/kwawingu/payments/azampay/AzampayPaymentTest.java`:

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.azampay;

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

public class AzampayPaymentTest {
  private static final Logger LOG = LoggerFactory.getLogger(AzampayPaymentTest.class);
  private static final Set<String> VALID_COLLECT_STATUSES =
      Set.of("pending", "processing", "completed", "failed");
  private static final Set<String> VALID_PAYOUT_STATUSES =
      Set.of("pending", "processing", "completed", "failed", "reversed");

  private AzampayPayment payment;

  @BeforeEach
  public void setUp() {
    payment = new AzampayPayment.Builder().setApiKey(SnippeApiKey.fromEnvironment()).build();
  }

  @Test
  public void testCollect() throws IOException, InterruptedException {
    AzampayCollectPayload payload =
        new AzampayCollectPayload.Builder()
            .setAmount(1000L)
            .setPhone("255741000000")
            .setReference("azampay-collect-test")
            .setDescription("Azampay collect test")
            .build();

    PaymentResponse response = payment.collect(payload);

    assertNotNull(response.reference(), "reference must not be null");
    assertFalse(response.reference().isBlank(), "reference must not be blank");
    assertTrue(
        VALID_COLLECT_STATUSES.contains(response.status()),
        "status must be one of " + VALID_COLLECT_STATUSES + ", got: " + response.status());
    assertEquals(1000L, response.amount());
    assertEquals("TZS", response.currency());
    LOG.info("collect response: ref={} status={}", response.reference(), response.status());
  }

  @Test
  public void testDisburse() throws IOException, InterruptedException {
    AzampayDisbursePayload payload =
        new AzampayDisbursePayload.Builder()
            .setAmount(1000L)
            .setPhone("255741000000")
            .setReference("azampay-disburse-test")
            .setDescription("Azampay disburse test")
            .build();

    PayoutResponse response = payment.disburse(payload);

    assertNotNull(response.reference(), "reference must not be null");
    assertFalse(response.reference().isBlank(), "reference must not be blank");
    assertTrue(
        VALID_PAYOUT_STATUSES.contains(response.status()),
        "status must be one of " + VALID_PAYOUT_STATUSES + ", got: " + response.status());
    assertEquals(1000L, response.amount());
    assertEquals("TZS", response.currency());
    LOG.info("disburse response: ref={} status={}", response.reference(), response.status());
  }
}
```

- [ ] **Step 2: Verify Azampay modules compile**

```bash
./gradlew :kw-azampay-payment-api:compileJava :kw-azampay-payment-impl:compileJava :kw-azampay-payment-impl:compileTestJava
```

Expected: `BUILD SUCCESSFUL`

---

## Task 5: Selcom API module — build.gradle + interface + payloads

**Files:**
- Modify: `kw-selcom-payment-api/build.gradle`
- Create: `kw-selcom-payment-api/src/main/java/com/kwawingu/payments/selcom/MobilePayment.java`
- Create: `kw-selcom-payment-api/src/main/java/com/kwawingu/payments/selcom/SelcomCollectPayload.java`
- Create: `kw-selcom-payment-api/src/main/java/com/kwawingu/payments/selcom/SelcomDisbursePayload.java`

- [ ] **Step 1: Fix selcom API build.gradle**

Replace entire content of `kw-selcom-payment-api/build.gradle`:

```groovy
apply from: rootProject.file('gradle/plugin-java.gradle')

dependencies {
    api project(':kw-mobile-payment-client')
}
```

- [ ] **Step 2: Create MobilePayment interface**

Create `kw-selcom-payment-api/src/main/java/com/kwawingu/payments/selcom/MobilePayment.java`:

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.selcom;

import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;

public interface MobilePayment {
  PaymentResponse collect(SelcomCollectPayload payload) throws IOException, InterruptedException;

  PayoutResponse disburse(SelcomDisbursePayload payload) throws IOException, InterruptedException;
}
```

- [ ] **Step 3: Create SelcomCollectPayload**

Create `kw-selcom-payment-api/src/main/java/com/kwawingu/payments/selcom/SelcomCollectPayload.java`:

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.selcom;

import org.checkerframework.checker.nullness.qual.Nullable;

public final class SelcomCollectPayload {
  private final long amount;
  private final String phone;
  private final String reference;
  private final String description;

  @SuppressWarnings(
      "nullness") // builder.build() validates non-null before calling this constructor
  private SelcomCollectPayload(Builder builder) {
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
    private @Nullable String phone = null;
    private @Nullable String reference = null;
    private @Nullable String description = null;

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

    public SelcomCollectPayload build() {
      if (phone == null) throw new NullPointerException("phone cannot be null");
      if (reference == null) throw new NullPointerException("reference cannot be null");
      if (description == null) throw new NullPointerException("description cannot be null");
      if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
      if (reference.length() > 30)
        throw new IllegalArgumentException("reference must be ≤30 chars");
      return new SelcomCollectPayload(this);
    }
  }
}
```

- [ ] **Step 4: Create SelcomDisbursePayload**

Create `kw-selcom-payment-api/src/main/java/com/kwawingu/payments/selcom/SelcomDisbursePayload.java`:

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.selcom;

import org.checkerframework.checker.nullness.qual.Nullable;

public final class SelcomDisbursePayload {
  private final long amount;
  private final String phone;
  private final String reference;
  private final String description;

  @SuppressWarnings(
      "nullness") // builder.build() validates non-null before calling this constructor
  private SelcomDisbursePayload(Builder builder) {
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
    private @Nullable String phone = null;
    private @Nullable String reference = null;
    private @Nullable String description = null;

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

    public SelcomDisbursePayload build() {
      if (phone == null) throw new NullPointerException("phone cannot be null");
      if (reference == null) throw new NullPointerException("reference cannot be null");
      if (description == null) throw new NullPointerException("description cannot be null");
      if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
      if (reference.length() > 30)
        throw new IllegalArgumentException("reference must be ≤30 chars");
      return new SelcomDisbursePayload(this);
    }
  }
}
```

---

## Task 6: Selcom Impl module — build.gradle + implementation

**Files:**
- Modify: `kw-selcom-payment-impl/build.gradle`
- Create: `kw-selcom-payment-impl/src/main/java/com/kwawingu/payments/selcom/SelcomPayment.java`

- [ ] **Step 1: Fix selcom impl build.gradle**

The current file has a broken `plugins { id 'java' }` block and wrong dependency scopes. Replace the entire content:

```groovy
apply from: rootProject.file('gradle/plugin-java.gradle')

dependencies {
    implementation project(':kw-selcom-payment-api')
    implementation project(':kw-mobile-payment-client')
    testImplementation libs.slfj
}
```

- [ ] **Step 2: Create SelcomPayment**

Create directory: `kw-selcom-payment-impl/src/main/java/com/kwawingu/payments/selcom/`

Create `kw-selcom-payment-impl/src/main/java/com/kwawingu/payments/selcom/SelcomPayment.java`:

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.selcom;

import com.kwawingu.payments.client.SnippeApiKey;
import com.kwawingu.payments.client.SnippeHttpClient;
import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SelcomPayment implements MobilePayment {
  private final SnippeHttpClient client;
  private final SnippeApiKey apiKey;

  private SelcomPayment(SnippeHttpClient client, SnippeApiKey apiKey) {
    this.client = client;
    this.apiKey = apiKey;
  }

  @Override
  public PaymentResponse collect(SelcomCollectPayload payload)
      throws IOException, InterruptedException {
    Map<String, Object> body =
        Map.of(
            "payment_type", "mobile",
            "details", Map.of("amount", payload.amount(), "currency", "TZS"),
            "phone_number", payload.phone(),
            "customer", Map.of("firstname", "", "lastname", "", "email", ""),
            "metadata", Map.of("order_id", payload.reference()));
    return client.postPayment(apiKey, payload.reference(), body);
  }

  @Override
  public PayoutResponse disburse(SelcomDisbursePayload payload)
      throws IOException, InterruptedException {
    Map<String, Object> body =
        Map.of(
            "payment_type", "mobile",
            "details", Map.of("amount", payload.amount(), "currency", "TZS"),
            "phone_number", payload.phone(),
            "description", payload.description(),
            "metadata", Map.of("order_id", payload.reference()));
    return client.postPayout(apiKey, payload.reference(), body);
  }

  public static class Builder {
    private @Nullable SnippeApiKey apiKey = null;

    public Builder setApiKey(SnippeApiKey apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    @SuppressWarnings("nullness") // null-checked above before passing to constructor
    public SelcomPayment build() {
      if (apiKey == null) throw new NullPointerException("apiKey cannot be null");
      return new SelcomPayment(new SnippeHttpClient(), apiKey);
    }
  }
}
```

---

## Task 7: Selcom test + provider verification

**Files:**
- Create: `kw-selcom-payment-impl/src/test/java/com/kwawingu/payments/selcom/SelcomPaymentTest.java`

- [ ] **Step 1: Create SelcomPaymentTest**

Create `kw-selcom-payment-impl/src/test/java/com/kwawingu/payments/selcom/SelcomPaymentTest.java`:

```java
/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.selcom;

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

public class SelcomPaymentTest {
  private static final Logger LOG = LoggerFactory.getLogger(SelcomPaymentTest.class);
  private static final Set<String> VALID_COLLECT_STATUSES =
      Set.of("pending", "processing", "completed", "failed");
  private static final Set<String> VALID_PAYOUT_STATUSES =
      Set.of("pending", "processing", "completed", "failed", "reversed");

  private SelcomPayment payment;

  @BeforeEach
  public void setUp() {
    payment = new SelcomPayment.Builder().setApiKey(SnippeApiKey.fromEnvironment()).build();
  }

  @Test
  public void testCollect() throws IOException, InterruptedException {
    SelcomCollectPayload payload =
        new SelcomCollectPayload.Builder()
            .setAmount(1000L)
            .setPhone("255741000000")
            .setReference("selcom-collect-test")
            .setDescription("Selcom collect test")
            .build();

    PaymentResponse response = payment.collect(payload);

    assertNotNull(response.reference(), "reference must not be null");
    assertFalse(response.reference().isBlank(), "reference must not be blank");
    assertTrue(
        VALID_COLLECT_STATUSES.contains(response.status()),
        "status must be one of " + VALID_COLLECT_STATUSES + ", got: " + response.status());
    assertEquals(1000L, response.amount());
    assertEquals("TZS", response.currency());
    LOG.info("collect response: ref={} status={}", response.reference(), response.status());
  }

  @Test
  public void testDisburse() throws IOException, InterruptedException {
    SelcomDisbursePayload payload =
        new SelcomDisbursePayload.Builder()
            .setAmount(1000L)
            .setPhone("255741000000")
            .setReference("selcom-disburse-test")
            .setDescription("Selcom disburse test")
            .build();

    PayoutResponse response = payment.disburse(payload);

    assertNotNull(response.reference(), "reference must not be null");
    assertFalse(response.reference().isBlank(), "reference must not be blank");
    assertTrue(
        VALID_PAYOUT_STATUSES.contains(response.status()),
        "status must be one of " + VALID_PAYOUT_STATUSES + ", got: " + response.status());
    assertEquals(1000L, response.amount());
    assertEquals("TZS", response.currency());
    LOG.info("disburse response: ref={} status={}", response.reference(), response.status());
  }
}
```

- [ ] **Step 2: Verify Selcom modules compile**

```bash
./gradlew :kw-selcom-payment-api:compileJava :kw-selcom-payment-impl:compileJava :kw-selcom-payment-impl:compileTestJava
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Full project compile check**

```bash
./gradlew compileJava compileTestJava
```

Expected: `BUILD SUCCESSFUL` — all 16 modules compile.

- [ ] **Step 4: Apply formatting**

```bash
./gradlew spotlessApply -Denable.spotless=true
```

Expected: `BUILD SUCCESSFUL` (reformats any style issues).

- [ ] **Step 5: Commit providers**

```bash
git add \
  kw-azampay-payment-api/ \
  kw-azampay-payment-impl/ \
  kw-selcom-payment-api/ \
  kw-selcom-payment-impl/
git commit -m "feat: implement Azampay and Selcom payment providers via Snippe"
```

---

## Task 8: Create gradle.properties + update version catalog

**Files:**
- Create: `gradle.properties`
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: Create gradle.properties**

Create `gradle.properties` at the repo root:

```properties
group=com.kwawingu
version=0.0.1
```

- [ ] **Step 2: Add Sonatype Central plugin to version catalog**

In `gradle/libs.versions.toml`, add to `[versions]`:

```toml
sonatype-central-publish = "0.6.0"
```

And add to `[plugins]`:

```toml
sonatype-central-publish = { id = "com.sonatype.central.publish", version.ref = "sonatype-central-publish" }
```

The full `[plugins]` section should look like:

```toml
[plugins]
spotless = { id = "com.diffplug.spotless", version.ref = "spotless-plugin" }
checkerframework = { id = "org.checkerframework", version.ref = "checkerframework-plugin" }
errorprone = { id = "net.ltgt.errorprone", version.ref = "errorprone-plugin" }
owasp-dependencycheck = { id = "org.owasp.dependencycheck", version.ref = "owasp-dependencycheck-plugin" }
sonatype-central-publish = { id = "com.sonatype.central.publish", version.ref = "sonatype-central-publish" }
```

- [ ] **Step 3: Verify build still resolves**

```bash
./gradlew dependencies --configuration compileClasspath -q 2>&1 | head -20
```

Expected: resolves without error (the new plugin version won't be fetched yet, just declared).

---

## Task 9: Create gradle/plugin-publish.gradle

**Files:**
- Create: `gradle/plugin-publish.gradle`

- [ ] **Step 1: Create the shared publish plugin file**

Create `gradle/plugin-publish.gradle`:

```groovy
apply plugin: 'maven-publish'
apply plugin: 'signing'

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        mavenJava(MavenPublication) {
            from components.java
            pom {
                name = project.name
                description = "KwaWingu Mobile Payments Library - ${project.name}"
                url = 'https://github.com/kwawingu/mobile-payments-library'
                licenses {
                    license {
                        name = 'The Apache License, Version 2.0'
                        url = 'https://www.apache.org/licenses/LICENSE-2.0'
                    }
                }
                developers {
                    developer {
                        id = 'kwawingu'
                        name = 'KwaWingu'
                        email = 'dev@kwawingu.com'
                    }
                }
                scm {
                    connection = 'scm:git:git@github.com:kwawingu/mobile-payments-library.git'
                    developerConnection = 'scm:git:git@github.com:kwawingu/mobile-payments-library.git'
                    url = 'https://github.com/kwawingu/mobile-payments-library'
                }
            }
        }
    }
}

def signingKey = findProperty("signingKey") as String
def signingPassword = findProperty("signingPassword") as String
if (signingKey) {
    signing {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign publishing.publications.mavenJava
    }
}
```

- [ ] **Step 2: Apply plugin-publish.gradle to kw-mobile-payment-client**

In `kw-mobile-payment-client/build.gradle`, append this line at the end:

```groovy
apply from: rootProject.file('gradle/plugin-publish.gradle')
```

Full file content becomes:

```groovy
apply from: rootProject.file('gradle/plugin-java.gradle')

dependencies {
    api libs.gson
}

apply from: rootProject.file('gradle/plugin-publish.gradle')
```

(Read the current file first to see if there are other deps; append `apply from:` as the last line regardless.)

- [ ] **Step 3: Verify kw-mobile-payment-client publications resolve**

```bash
./gradlew :kw-mobile-payment-client:generatePomFileForMavenJavaPublication
```

Expected: `BUILD SUCCESSFUL` — generates `build/publications/mavenJava/pom-default.xml`.

---

## Task 10: Apply plugin-publish.gradle to all remaining modules

**Files:** 13 module build.gradle files (all except `kw-mobile-payment-logback-config`).

For each file below, **append** `apply from: rootProject.file('gradle/plugin-publish.gradle')` as the last line. Do NOT touch `kw-mobile-payment-logback-config/build.gradle`.

- [ ] **Step 1: Apply to API modules**

Append to `kw-mpesa-payment-api/build.gradle`:
```groovy
apply from: rootProject.file('gradle/plugin-publish.gradle')
```

Append to `kw-airtel-payment-api/build.gradle`:
```groovy
apply from: rootProject.file('gradle/plugin-publish.gradle')
```

Append to `kw-halopesa-payment-api/build.gradle`:
```groovy
apply from: rootProject.file('gradle/plugin-publish.gradle')
```

Append to `kw-mixxbyyas-payment-api/build.gradle`:
```groovy
apply from: rootProject.file('gradle/plugin-publish.gradle')
```

Append to `kw-azampay-payment-api/build.gradle`:
```groovy
apply from: rootProject.file('gradle/plugin-publish.gradle')
```

Append to `kw-selcom-payment-api/build.gradle`:
```groovy
apply from: rootProject.file('gradle/plugin-publish.gradle')
```

Append to `kw-card-payment-api/build.gradle`:
```groovy
apply from: rootProject.file('gradle/plugin-publish.gradle')
```

- [ ] **Step 2: Apply to impl modules**

Append to `kw-mpesa-payment-impl/build.gradle`:
```groovy
apply from: rootProject.file('gradle/plugin-publish.gradle')
```

Append to `kw-airtel-payment-impl/build.gradle`:
```groovy
apply from: rootProject.file('gradle/plugin-publish.gradle')
```

Append to `kw-halopesa-payment-impl/build.gradle`:
```groovy
apply from: rootProject.file('gradle/plugin-publish.gradle')
```

Append to `kw-mixxbyyas-payment-impl/build.gradle`:
```groovy
apply from: rootProject.file('gradle/plugin-publish.gradle')
```

Append to `kw-azampay-payment-impl/build.gradle`:
```groovy
apply from: rootProject.file('gradle/plugin-publish.gradle')
```

Append to `kw-selcom-payment-impl/build.gradle`:
```groovy
apply from: rootProject.file('gradle/plugin-publish.gradle')
```

Append to `kw-card-payment-impl/build.gradle`:
```groovy
apply from: rootProject.file('gradle/plugin-publish.gradle')
```

- [ ] **Step 3: Verify all POM files generate**

```bash
./gradlew generatePomFileForMavenJavaPublication
```

Expected: `BUILD SUCCESSFUL` — 14 `pom-default.xml` files generated (one per publishable module). If any module fails, check that `plugin-java.gradle` is applied BEFORE `plugin-publish.gradle` in that module's build.gradle.

---

## Task 11: Update root build.gradle for Central Portal plugin

**Files:**
- Modify: `build.gradle`

- [ ] **Step 1: Add Sonatype Central plugin to root build.gradle**

Current `build.gradle`:
```groovy
plugins {
    alias libs.plugins.spotless apply(false)
    alias libs.plugins.checkerframework apply(false)
    alias libs.plugins.errorprone apply(false)
    alias libs.plugins.owasp.dependencycheck apply(false)
}
```

Replace with:
```groovy
plugins {
    alias libs.plugins.spotless apply(false)
    alias libs.plugins.checkerframework apply(false)
    alias libs.plugins.errorprone apply(false)
    alias libs.plugins.owasp.dependencycheck apply(false)
    alias libs.plugins.sonatype.central.publish
}

centralPublishing {
    publishingType = 'AUTOMATIC'
}
```

- [ ] **Step 2: Verify the plugin resolves**

```bash
./gradlew tasks --group publishing 2>&1 | grep -E "publish|central"
```

Expected: output includes `publishAllPublicationsToCentralPortal` in the task list.

If you see `Could not resolve com.sonatype.central:com.sonatype.central.publish.gradle.plugin:0.6.0`, check the version in `libs.versions.toml` and try `0.5.0` instead. The plugin is on Maven Central — verify latest at: https://central.sonatype.com/artifact/com.sonatype.central/com.sonatype.central.publish.gradle.plugin

---

## Task 12: Update release workflow

**Files:**
- Modify: `.github/workflows/kw-mobile-library-release-production.yml`

- [ ] **Step 1: Replace the release workflow**

Replace the entire file content:

```yaml
name: kw-mobile-library-release
run-name: kw-mobile-library-release
'on':
  push:
    tags:
      - 'v*'

jobs:
  release:
    runs-on: ubuntu-latest
    timeout-minutes: 30
    permissions:
      contents: write
    steps:
      - name: Checkout
        uses: actions/checkout@v4
      - name: Set up Java 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 21
      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3
      - name: Run CI pipeline
        run: ./scripts/ci/kw-mobile-lib-format-static-analysis-unit-test.sh
        env:
          SNIPPE_API_KEY: ${{ secrets.SNIPPE_API_KEY }}
      - name: Publish to Maven Central
        run: ./gradlew publishAllPublicationsToCentralPortal
        env:
          ORG_GRADLE_PROJECT_signingKey: ${{ secrets.GPG_PRIVATE_KEY }}
          ORG_GRADLE_PROJECT_signingPassword: ${{ secrets.GPG_PASSPHRASE }}
          CENTRAL_TOKEN_USERNAME: ${{ secrets.CENTRAL_TOKEN_USERNAME }}
          CENTRAL_TOKEN_PASSWORD: ${{ secrets.CENTRAL_TOKEN_PASSWORD }}
      - name: Create GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          generate_release_notes: true
```

Note: removed `packages: write` permission (no longer publishing to GitHub Packages).

- [ ] **Step 2: Final build verification**

```bash
./gradlew build -x test
```

Expected: `BUILD SUCCESSFUL` — all modules compile and produce JARs (sources + javadoc + main).

- [ ] **Step 3: Apply formatting**

```bash
./gradlew spotlessApply -Denable.spotless=true
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit publishing config**

```bash
git add \
  gradle.properties \
  gradle/libs.versions.toml \
  gradle/plugin-publish.gradle \
  build.gradle \
  kw-mobile-payment-client/build.gradle \
  kw-mpesa-payment-api/build.gradle \
  kw-mpesa-payment-impl/build.gradle \
  kw-airtel-payment-api/build.gradle \
  kw-airtel-payment-impl/build.gradle \
  kw-halopesa-payment-api/build.gradle \
  kw-halopesa-payment-impl/build.gradle \
  kw-mixxbyyas-payment-api/build.gradle \
  kw-mixxbyyas-payment-impl/build.gradle \
  kw-azampay-payment-api/build.gradle \
  kw-azampay-payment-impl/build.gradle \
  kw-selcom-payment-api/build.gradle \
  kw-selcom-payment-impl/build.gradle \
  kw-card-payment-api/build.gradle \
  kw-card-payment-impl/build.gradle \
  .github/workflows/kw-mobile-library-release-production.yml
git commit -m "feat: configure Maven Central publishing via Sonatype Central Portal"
```

---

## GPG Key Setup (one-time manual step — do before triggering a release)

Run these commands **locally** (not in CI) before pushing a `v*` tag:

```bash
# 1. Generate RSA 4096 key — choose a passphrase you'll store as GPG_PASSPHRASE secret
gpg --full-gen-key

# 2. Find your key ID (the long hex after "sec rsa4096/")
gpg --list-secret-keys --keyid-format=long

# 3. Upload public key so Maven Central can verify signatures
gpg --keyserver keys.openpgp.org --send-keys <KEY_ID>

# 4. Export ASCII-armored private key — paste this ENTIRE output as GPG_PRIVATE_KEY secret
gpg --armor --export-secret-keys <KEY_ID>
```

**Add these 4 GitHub repository secrets** (Settings → Secrets → Actions):

| Secret name | Value |
|-------------|-------|
| `GPG_PRIVATE_KEY` | Full ASCII-armored key block from step 4 |
| `GPG_PASSPHRASE` | Passphrase from step 1 |
| `CENTRAL_TOKEN_USERNAME` | From central.sonatype.com → Account → Generate User Token |
| `CENTRAL_TOKEN_PASSWORD` | From same token generation |

**To release:**
```bash
git tag v0.0.1
git push origin v0.0.1
```

This triggers the release workflow: runs tests → signs artifacts → uploads bundle to Maven Central → creates GitHub Release.

---

## Self-Review Notes

- Spec §2 (Azampay/Selcom): Covered in Tasks 1–7.
- Spec §3.1 (gradle.properties): Covered in Task 8 Step 1.
- Spec §3.2 (plugin-publish.gradle): Covered in Tasks 9–10.
- Spec §3.3 (root build.gradle): Covered in Task 11.
- Spec §3.4 (apply to each module): Covered in Tasks 9–10.
- Spec §4 (GPG setup): Covered in the manual GPG setup section above.
- Spec §5 (release workflow): Covered in Task 12.
- Spec §7 (logback-config not published): `kw-mobile-payment-logback-config` explicitly excluded from Task 10.
