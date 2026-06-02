# Disable Card Module + Fix Maven Central Deploy — Design

Date: 2026-06-02

## Goal

Ship a fully compatible, fully functional `mobile-payments-library` to Maven Central:
green CI on `main`, working release-on-tag publish. Card payment is not in scope and
its Snippe sandbox checkout flow is unreliable, so it is disabled (reversibly), not
deleted.

## Scope

In scope:
1. Disable `kw-card-payment-api` + `kw-card-payment-impl` from the build.
2. Fix the release workflow to use the correct Central Portal aggregation task.
3. Verify all remaining modules compile, pass static analysis, and are publishable.

Out of scope:
- Fixing card checkout against the real Snippe card API.
- One-time secret setup (GPG key, Central Portal token) — separate manual step.

## Changes

### 1. Disable card in `settings.gradle`

Comment out both includes; leave code on disk for later re-enable.

```gradle
// Card disabled — Snippe card checkout not in scope
// include('kw-card-payment-api')
// include('kw-card-payment-impl')
```

Nothing in the build depends on card (`grep kw-card` → only `settings.gradle` and
`kw-card-payment-impl/build.gradle` self-dep). Removing the includes drops the card
test from `check` and drops card artifacts from the publish aggregation. No other
module breaks.

### 2. Fix release deploy task

`/.github/workflows/kw-mobile-library-release-production.yml`, Publish step:

```yaml
- name: Publish to Maven Central
  run: ./gradlew publishAggregationToCentralPortal
```

Was `publishAllPublicationsToCentralPortal`. That task only exists per-subproject;
running it at the root fans out into N independent Central Portal deployments and
bypasses the `nmcpSettings { centralPortal { publishingType = 'AUTOMATIC' } }`
aggregation config in `settings.gradle`. `publishAggregationToCentralPortal` (provided
by the `com.gradleup.nmcp.settings` plugin) bundles every enabled module into ONE
deployment using the configured credentials and AUTOMATIC publishing.

## Verification

Local (no `SNIPPE_API_KEY` available → live tests run in CI only):
- `./gradlew clean spotlessCheck -Denable.spotless=true`
- `./gradlew clean testClasses -Dbuild.errorprone=true`
- `./gradlew clean testClasses -Dbuild.checker=true`
- Confirm `kw-card-*` no longer appears in `./gradlew projects`.
- Confirm `publishAggregationToCentralPortal` resolves at root.

CI (after push):
- `kw-mobile-library-checks` green on `main` (no card test; mobile + client modules pass
  live Snippe sandbox).
- Tag `vX.Y.Z` → `kw-mobile-library-release` runs pipeline then aggregated publish.

## Re-enabling card later

Uncomment the two `settings.gradle` includes once the card flow is fixed and tested.
```
