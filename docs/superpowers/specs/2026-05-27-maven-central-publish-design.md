# Design: Fully Functional Library + Maven Central Publishing

**Date:** 2026-05-27  
**Group:** `com.kwawingu`  
**Initial version:** `0.0.1`  
**Target registry:** https://central.sonatype.com/

---

## 1. Scope

Two parallel tracks:

1. **Implement missing providers** — Azampay and Selcom modules have no Java source. Implement them via Snippe API (identical pattern to existing 4 mobile providers).
2. **Maven Central publishing** — Configure Gradle to produce signed, metadata-complete artifacts and publish via Sonatype Central Publishing Plugin.

---

## 2. Azampay & Selcom Implementation

### Pattern (identical to M-Pesa / Airtel / Halopesa / MixxByYas)

**API module** (`kw-{provider}-payment-api`):
- `MobilePayment` interface — `collect(Payload)` and `disburse(Payload)` methods
- `{Provider}CollectPayload` — Builder with fields: `amount` (long), `phone` (String), `reference` (String)
- `{Provider}DisbursePayload` — Builder with fields: `amount` (long), `phone` (String), `reference` (String), `description` (String)

**Impl module** (`kw-{provider}-payment-impl`):
- `{Provider}Payment` — implements `MobilePayment`
- Collect request body (Snippe format):
  ```json
  {
    "payment_type": "mobile",
    "details": { "amount": <long>, "currency": "TZS" },
    "phone_number": "<phone>",
    "customer": { "firstname": "", "lastname": "", "email": "" },
    "metadata": { "order_id": "<reference>" }
  }
  ```
- Disburse request body (Snippe format):
  ```json
  {
    "payment_type": "mobile",
    "details": { "amount": <long>, "currency": "TZS" },
    "phone_number": "<phone>",
    "description": "<description>",
    "metadata": { "order_id": "<reference>" }
  }
  ```
- `Builder` inner class with `setApiKey(SnippeApiKey)` + `build()`
- `@Nullable` on builder fields, `@SuppressWarnings("nullness")` on constructor

**Test** (`{Provider}PaymentTest`):
- `testCollect()` — assert status in `{pending, success, failed}`, reference not blank
- `testDisburse()` — assert status in `{pending, success, failed, reversed}`, reference not blank
- Unique idempotency keys per test (`"azampay-collect-test"`, `"azampay-disburse-test"`)

---

## 3. Maven Publishing Configuration

### 3.1 `gradle.properties` (root)

```properties
group=com.kwawingu
version=0.0.1
```

### 3.2 `gradle/plugin-publish.gradle` (new shared plugin)

Applied to every module via each module's `build.gradle`. Configures:

- `maven-publish` plugin
- `java { withSourcesJar(); withJavadocJar() }` — required by Maven Central
- `mavenJava` publication with POM metadata:
  - `name` = artifact ID
  - `description` = "KwaWingu Mobile Payments SDK — {provider} module"
  - `url` = `https://github.com/kwawingu/mobile-payments-library`
  - License: Apache 2.0 (`https://www.apache.org/licenses/LICENSE-2.0`)
  - Developer: `id=kwawingu, name=KwaWingu, email=dev@kwawingu.com`
  - SCM: `connection=scm:git:git@github.com:kwawingu/mobile-payments-library.git`, `url=https://github.com/kwawingu/mobile-payments-library`
- `signing` plugin: signs all publications
  - In-memory signing via `useInMemoryPgpKeys(findProperty("signingKey"), findProperty("signingPassword"))`
  - Gradle reads key material via `ORG_GRADLE_PROJECT_signingKey` and `ORG_GRADLE_PROJECT_signingPassword` env vars (no keyring import needed)

### 3.3 Root `build.gradle` additions

```groovy
plugins {
    ...
    id 'com.sonatype.central.publish' version '0.6.0' apply false
}
```

Applied at root level:
```groovy
apply plugin: 'com.sonatype.central.publish'
centralPublishing {
    publishingType = 'AUTOMATIC'  // auto-releases after validation
}
```

Credentials from env vars:
- `CENTRAL_TOKEN_USERNAME` → Sonatype user token username
- `CENTRAL_TOKEN_PASSWORD` → Sonatype user token password

### 3.4 Each module's `build.gradle`

Add one line:
```groovy
apply from: rootProject.file('gradle/plugin-publish.gradle')
```

---

## 4. GPG Key Setup (one-time, done locally)

Steps to complete before running the release workflow:

```bash
# 1. Generate key (RSA 4096, no expiry)
gpg --full-gen-key

# 2. Get key ID
gpg --list-secret-keys --keyid-format=long

# 3. Upload public key to keyserver (Maven Central checks here to verify signatures)
gpg --keyserver keys.openpgp.org --send-keys <KEY_ID>

# 4. Export ASCII-armored private key (paste this whole block as GPG_PRIVATE_KEY secret)
gpg --armor --export-secret-keys <KEY_ID>
```

**GitHub Secrets to add:**

| Secret | Value |
|--------|-------|
| `GPG_PRIVATE_KEY` | ASCII-armored private key (from step 4, NOT base64 — paste raw armor block) |
| `GPG_PASSPHRASE` | passphrase chosen during key generation |
| `CENTRAL_TOKEN_USERNAME` | From central.sonatype.com → Account → Generate Token |
| `CENTRAL_TOKEN_PASSWORD` | From same token generation |

---

## 5. Release Workflow Update

Update `.github/workflows/kw-mobile-library-release-production.yml`:

```yaml
- name: Publish to Maven Central
  run: ./gradlew publishAllPublicationsToCentralPortal
  env:
    SNIPPE_API_KEY: ${{ secrets.SNIPPE_API_KEY }}
    ORG_GRADLE_PROJECT_signingKey: ${{ secrets.GPG_PRIVATE_KEY }}
    ORG_GRADLE_PROJECT_signingPassword: ${{ secrets.GPG_PASSPHRASE }}
    CENTRAL_TOKEN_USERNAME: ${{ secrets.CENTRAL_TOKEN_USERNAME }}
    CENTRAL_TOKEN_PASSWORD: ${{ secrets.CENTRAL_TOKEN_PASSWORD }}
```

Remove the old `./gradlew publish` step (GitHub Packages). Keep the GitHub Release step.

---

## 6. Artifact Map

After publishing, consumers can depend on any of these artifacts:

```xml
<!-- pom.xml example -->
<dependency>
  <groupId>com.kwawingu</groupId>
  <artifactId>kw-mpesa-payment-impl</artifactId>
  <version>0.0.1</version>
</dependency>
```

| Artifact ID | Contains |
|-------------|----------|
| `kw-mobile-payment-client` | `SnippeHttpClient`, `SnippeApiKey`, response types |
| `kw-mpesa-payment-api` | M-Pesa interfaces + payloads |
| `kw-mpesa-payment-impl` | `MpesaPayment` implementation |
| `kw-airtel-payment-api` | Airtel interfaces + payloads |
| `kw-airtel-payment-impl` | `AirtelPayment` implementation |
| `kw-halopesa-payment-api` | Halopesa interfaces + payloads |
| `kw-halopesa-payment-impl` | `HalopesaPayment` implementation |
| `kw-mixxbyyas-payment-api` | MixxByYas interfaces + payloads |
| `kw-mixxbyyas-payment-impl` | `MixxByYasPayment` implementation |
| `kw-azampay-payment-api` | Azampay interfaces + payloads |
| `kw-azampay-payment-impl` | `AzampayPayment` implementation |
| `kw-selcom-payment-api` | Selcom interfaces + payloads |
| `kw-selcom-payment-impl` | `SelcomPayment` implementation |
| `kw-card-payment-api` | Card interfaces + payloads |
| `kw-card-payment-impl` | `CardPaymentImpl` implementation |

Consumers who want a single provider typically depend only on the `-impl` artifact (which transitively pulls the `-api`).

---

## 7. Out of Scope

- `kw-mobile-payment-logback-config` is NOT published (internal runtime config)
- No BOM (Bill of Materials) module — added if users request it later
- No version catalog publishing
