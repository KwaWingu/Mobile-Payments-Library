<div align="center">

<h1><a href="https://github.com/TheCollinsByte/Mobile-Payments-Library">KwaWingu Mobile Payments Library</a></h1>

<a href="https://github.com/TheCollinsByte/Mobile-Payments-Library/blob/main/LICENSE">
<img alt="License" src="https://img.shields.io/github/license/TheCollinsByte/Mobile-Payments-Library?style=flat&color=eee&label="> </a>

<a href="https://github.com/TheCollinsByte/Mobile-Payments-Library/graphs/contributors">
<img alt="People" src="https://img.shields.io/github/contributors/TheCollinsByte/Mobile-Payments-Library?style=flat&color=ffaaf2&label=People"> </a>

<a href="https://github.com/TheCollinsByte/Mobile-Payments-Library/stargazers">
<img alt="Stars" src="https://img.shields.io/github/stars/TheCollinsByte/Mobile-Payments-Library?style=flat&color=98c379&label=Stars"> </a>

<a href="https://github.com/TheCollinsByte/Mobile-Payments-Library/network/members">
<img alt="Forks" src="https://img.shields.io/github/forks/TheCollinsByte/Mobile-Payments-Library?style=flat&color=66a8e0&label=Forks"> </a>

<a href="https://github.com/TheCollinsByte/Mobile-Payments-Library/pulse">
<img alt="Last Updated" src="https://img.shields.io/github/last-commit/TheCollinsByte/Mobile-Payments-Library?style=flat&color=e06c75&label="> </a>

</div>

---

A Java SDK for mobile money payments in Tanzania, built on the [Snippe](https://snippe.sh) unified payments API. Supports M-Pesa, Airtel Money, HaloPesa, and Mixx by Yas — all through a single Bearer token and consistent builder API.

---

## Table of Contents

- [Supported Providers](#supported-providers)
- [Requirements](#requirements)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
  - [M-Pesa](#m-pesa)
  - [Airtel Money](#airtel-money)
  - [HaloPesa](#halopesa)
  - [Mixx by Yas](#mixx-by-yas)
- [Running Tests](#running-tests)
- [Contributing](#contributing)
- [License](#license)

---

## Supported Providers

| Provider | Network | Collection (C2B) | Disbursement (B2B) |
|---|---|:---:|:---:|
| Vodacom M-Pesa | `mpesa` | ✓ | ✓ |
| Airtel Money | `airtel_money` | ✓ | ✓ |
| Halotel HaloPesa | `halotel` | ✓ | ✓ |
| Mixx by Yas | `mixx_by_yas` | ✓ | ✓ |

All amounts are in **TZS, integer smallest unit** (no decimals).

---

## Requirements

- Java 21+
- Gradle 8+
- A [Snippe](https://snippe.sh) account with an API key

---

## Installation

The library is published to Maven Central under the `com.kwawingu` group. Add the
provider module(s) you need.

**Gradle** (`build.gradle`):

```groovy
dependencies {
    // Pick the providers you need
    implementation 'com.kwawingu:kw-mpesa-payment-impl:0.0.1'
    implementation 'com.kwawingu:kw-airtel-payment-impl:0.0.1'
    implementation 'com.kwawingu:kw-halopesa-payment-impl:0.0.1'
    implementation 'com.kwawingu:kw-mixxbyyas-payment-impl:0.0.1'
}
```

**Maven** (`pom.xml`):

```xml
<dependency>
    <groupId>com.kwawingu</groupId>
    <artifactId>kw-mpesa-payment-impl</artifactId>
    <version>0.0.1</version>
</dependency>
```

---

## Configuration

Set a single environment variable before running your application:

```bash
export SNIPPE_API_KEY="your-snippe-api-key"
```

The library reads this at runtime via `SnippeApiKey.fromEnvironment()`. No other credentials are required.

---

## Usage

### M-Pesa

```java
import com.kwawingu.payments.mpesa.MobilePayment;
import com.kwawingu.payments.mpesa.MpesaCollectPayload;
import com.kwawingu.payments.mpesa.MpesaDisbursePayload;
import com.kwawingu.payments.mpesa.MpesaPayment;
import com.kwawingu.payments.client.SnippeApiKey;
import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PayoutResponse;

MobilePayment mpesa = new MpesaPayment.Builder()
    .setApiKey(SnippeApiKey.fromEnvironment())
    .build();

// Collect (C2B) — triggers USSD push to customer
MpesaCollectPayload collectPayload = new MpesaCollectPayload.Builder()
    .setAmount(5000)
    .setPhone("255741000000")
    .setReference("INV-2026-001")       // max 30 chars, used as idempotency key
    .setDescription("Invoice payment")
    .build();

PaymentResponse payment = mpesa.collect(collectPayload);
System.out.println(payment.reference()); // Snippe reference
System.out.println(payment.status());    // pending | processing | completed | failed

// Disburse (B2B) — push funds to a mobile wallet
MpesaDisbursePayload disbursePayload = new MpesaDisbursePayload.Builder()
    .setAmount(5000)
    .setPhone("255741000000")
    .setReference("PAY-2026-001")
    .setDescription("Salary disbursement")
    .build();

PayoutResponse payout = mpesa.disburse(disbursePayload);
System.out.println(payout.status()); // pending | processing | completed | failed | reversed
```

### Airtel Money

```java
import com.kwawingu.payments.airtel.AirtelPayment;
import com.kwawingu.payments.airtel.AirtelCollectPayload;
import com.kwawingu.payments.airtel.MobilePayment;

MobilePayment airtel = new AirtelPayment.Builder()
    .setApiKey(SnippeApiKey.fromEnvironment())
    .build();

PaymentResponse payment = airtel.collect(
    new AirtelCollectPayload.Builder()
        .setAmount(5000)
        .setPhone("255780000000")
        .setReference("INV-2026-002")
        .setDescription("Invoice payment")
        .build());
```

### HaloPesa

```java
import com.kwawingu.payments.halopesa.HalopesaPayment;
import com.kwawingu.payments.halopesa.HalopesaCollectPayload;
import com.kwawingu.payments.halopesa.MobilePayment;

MobilePayment halopesa = new HalopesaPayment.Builder()
    .setApiKey(SnippeApiKey.fromEnvironment())
    .build();

PaymentResponse payment = halopesa.collect(
    new HalopesaCollectPayload.Builder()
        .setAmount(5000)
        .setPhone("255762000000")
        .setReference("INV-2026-003")
        .setDescription("Invoice payment")
        .build());
```

### Mixx by Yas

```java
import com.kwawingu.payments.mixxbyyas.MixxByYasPayment;
import com.kwawingu.payments.mixxbyyas.MixxByYasCollectPayload;
import com.kwawingu.payments.mixxbyyas.MobilePayment;

MobilePayment mixxbyyas = new MixxByYasPayment.Builder()
    .setApiKey(SnippeApiKey.fromEnvironment())
    .build();

PaymentResponse payment = mixxbyyas.collect(
    new MixxByYasCollectPayload.Builder()
        .setAmount(5000)
        .setPhone("255676000000")
        .setReference("INV-2026-004")
        .setDescription("Invoice payment")
        .build());
```

---

## Running Tests

Tests are integration tests that hit the Snippe sandbox. Set your API key before running:

```bash
export SNIPPE_API_KEY="your-snippe-sandbox-api-key"

# Run all tests
./gradlew check

# Run a single provider
./gradlew :kw-mpesa-payment-impl:test

# Full CI pipeline (format + static analysis + tests)
./scripts/ci/kw-mobile-lib-format-static-analysis-unit-test.sh
```

There are no unit tests with mocks — the project intentionally tests against the real sandbox to catch provider-specific behaviour.

---

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

When adding a new provider, follow the existing pattern:
1. Create `kw-{provider}-payment-api` with a `MobilePayment` interface and typed payload classes.
2. Create `kw-{provider}-payment-impl` with a thin Snippe adapter (set the correct `network` field).
3. Register both modules in `settings.gradle`.
4. Add sandbox integration tests.

---

## License

Licensed under the Apache License 2.0 — see [LICENSE](LICENSE) for details.

<br/>

<div align="center">

<strong>⭐ hit the star button if you found this useful ⭐</strong><br>

<a href="https://github.com/TheCollinsByte/Mobile-Payments-Library">Source</a>
| <a href="https://x.com/TheCollinsByte" target="_blank">Twitter</a>
| <a href="http://www.linkedin.com/in/collins-boniface" target="_blank">LinkedIn</a>
| <a href="mailto:collo@fastmail.com">Email</a>

</div>
