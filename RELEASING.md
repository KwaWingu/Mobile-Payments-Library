# Releasing to Maven Central

This library publishes to [Maven Central](https://central.sonatype.com) via the Sonatype
Central Portal under the `com.kwawingu` group. Releases are triggered by pushing a
`v*` git tag, which runs the `kw-mobile-library-release` GitHub Actions workflow.

The workflow runs the full CI pipeline, then:

```bash
./gradlew publishAggregationToCentralPortal
```

This bundles every published module into a single Central Portal deployment. The workflow sets
`CENTRAL_PUBLISHING_TYPE=AUTOMATIC` (read in `settings.gradle`), so a tag releases with no manual
step: it fails first if the tag does not match `version` in `gradle.properties` or is a SNAPSHOT,
runs format + ErrorProne + Checker Framework, publishes, waits until repo1.maven.org serves the
POM, and creates the GitHub Release with the jars attached. Local runs default to `USER_MANAGED`
(click "Publish" in the Central Portal).

Published modules (group `com.kwawingu`):

- `kw-mobile-payment-client`
- `kw-mobile-payment-logback-config`
- `kw-mpesa-payment-api`, `kw-mpesa-payment-impl`
- `kw-airtel-payment-api`, `kw-airtel-payment-impl`
- `kw-halopesa-payment-api`, `kw-halopesa-payment-impl`
- `kw-mixxbyyas-payment-api`, `kw-mixxbyyas-payment-impl`

---

## One-time setup

### 1. GPG signing key

Central requires all artifacts to be PGP-signed.

```bash
# Generate a key (RSA 4096, no expiry or a long expiry). Use a real name + email.
gpg --full-generate-key

# Find the key id (the long hex string after sec rsa4096/)
gpg --list-secret-keys --keyid-format LONG

# Publish the PUBLIC key so Central can verify signatures
gpg --keyserver keys.openpgp.org --send-keys <KEY_ID>

# Export the PRIVATE key in ASCII-armored form (used by the CI signing plugin)
gpg --armor --export-secret-keys <KEY_ID> > private-key.asc
```

Keep `private-key.asc` and the passphrase secret. Do **not** commit them.

### 2. Sonatype Central Portal token

1. Sign in at https://central.sonatype.com.
2. Make sure the `com.kwawingu` namespace is registered and verified to your account.
3. Go to **Account → Generate User Token**.
4. Copy the generated **username** and **password** (token pair).

### 3. GitHub Actions secrets

In the GitHub repo: **Settings → Secrets and variables → Actions → New repository
secret**. Add all four:

| Secret | Value |
|---|---|
| `GPG_PRIVATE_KEY` | full contents of `private-key.asc` (including the BEGIN/END lines) |
| `GPG_PASSPHRASE` | the passphrase for that GPG key |
| `CENTRAL_TOKEN_USERNAME` | Central Portal token username |
| `CENTRAL_TOKEN_PASSWORD` | Central Portal token password |

The release workflow maps these to `ORG_GRADLE_PROJECT_signingKey`,
`ORG_GRADLE_PROJECT_signingPassword`, and the `nmcpSettings` Central credentials.

---

## Cutting a release

1. Set the version in `gradle.properties` (drop any `-SNAPSHOT`, pick the release
   number):

   ```properties
   version=0.0.1
   ```

2. Commit on `main` and push:

   ```bash
   git add gradle.properties
   git commit -m "release: 0.0.1"
   git push origin main
   ```

3. Tag and push the tag — this triggers the release workflow:

   ```bash
   git tag v0.0.1
   git push origin v0.0.1
   ```

4. Watch **Actions → kw-mobile-library-release**. It verifies the tag, runs the checks,
   publishes automatically, waits for Maven Central and creates the GitHub Release.
   A Maven Central release is permanent: double-check the version before tagging.

5. Verify: the artifacts appear at
   `https://central.sonatype.com/artifact/com.kwawingu/kw-mpesa-payment-impl` and on
   Maven Central (`https://repo1.maven.org/maven2/com/kwawingu/`) within ~10–30 min of
   sync.

6. Bump to the next development version in `gradle.properties` (e.g. `0.0.2-SNAPSHOT`)
   and commit.

---

## Local dry run (optional)

To validate signing + POM generation without uploading:

```bash
export ORG_GRADLE_PROJECT_signingKey="$(cat private-key.asc)"
export ORG_GRADLE_PROJECT_signingPassword="<passphrase>"
./gradlew clean publishAggregationToCentralPortal --dry-run
```

Or build the signed artifacts into the local Maven repo:

```bash
./gradlew clean publishToMavenLocal \
    -PsigningKey="$(cat private-key.asc)" -PsigningPassword="<passphrase>"
```
