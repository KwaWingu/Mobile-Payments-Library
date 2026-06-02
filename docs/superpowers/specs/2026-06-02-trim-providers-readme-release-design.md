# Trim Providers + README + Release Docs — Design

Date: 2026-06-02

## Goal

Reduce the library to four supported mobile-money providers, refresh the README to
match, and document the Maven Central release procedure so the published artifacts are
usable by third parties.

## Keep

- `kw-mobile-payment-client` (shared Snippe HTTP client)
- `kw-mobile-payment-logback-config` (runtime logging)
- M-Pesa, Airtel Money, HaloPesa, Mixx by Yas — each `-api` + `-impl`

Published module set: 10 modules, group `com.kwawingu`, version `0.0.1`.

## Remove

Six module directories (`git rm -r`), plus their `settings.gradle` includes:

- `kw-card-payment-api`, `kw-card-payment-impl`
- `kw-azampay-payment-api`, `kw-azampay-payment-impl`
- `kw-selcom-payment-api`, `kw-selcom-payment-impl`

No kept module imports these (verified by grep). Card was already commented out in
`settings.gradle`; remove those comment lines too.

## Changes

### settings.gradle
Delete the card comment block and the azampay + selcom `include(...)` lines. Leaves
client, logback-config, and the four provider api/impl pairs.

### README.md
- Intro: drop "and card checkout"; describe as a mobile-money SDK for M-Pesa, Airtel
  Money, HaloPesa, Mixx by Yas.
- Table of Contents: remove the Card Checkout entry.
- Supported Providers table: keep the four mobile rows; delete Card, AzamPay, Selcom.
- Installation: replace local `project(':...')` deps with published Maven coordinates,
  e.g. `implementation 'com.kwawingu:kw-mpesa-payment-impl:0.0.1'`, listing the four
  providers. Add the Maven `<dependency>` form too.
- Remove the entire Card Checkout usage section.

### RELEASING.md (new, committed)
Step-by-step Maven Central release runbook:
1. One-time: generate a GPG key, export the private key, upload the public key to
   `keys.openpgp.org`.
2. One-time: create a Sonatype Central Portal token (username + password pair).
3. One-time: add four GitHub Actions secrets — `GPG_PRIVATE_KEY`, `GPG_PASSPHRASE`,
   `CENTRAL_TOKEN_USERNAME`, `CENTRAL_TOKEN_PASSWORD`.
4. Release: bump `version` in `gradle.properties` if needed, commit, then
   `git tag vX.Y.Z && git push origin vX.Y.Z`.
5. The `kw-mobile-library-release` workflow runs CI then
   `./gradlew publishAggregationToCentralPortal` (AUTOMATIC publishing).
6. Verify the artifacts appear under `com.kwawingu` on central.sonatype.com / Maven
   Central, and consumption works from a sample project.

## Verification

- `./gradlew projects` shows only the 10 kept modules.
- `./gradlew clean spotlessCheck -Denable.spotless=true` passes.
- `./gradlew clean testClasses -Dbuild.errorprone=true` and `-Dbuild.checker=true` pass.
- Full `./gradlew clean check` green (collect pass, disburse skipped on PAY_004).

## Out of scope

- `docs/superpowers/` history (older specs/plans mention the removed providers) — kept
  as historical record.
- The `2026-06-02-disable-card-fix-deploy-design.md` spec — kept; superseded but valid
  history.
