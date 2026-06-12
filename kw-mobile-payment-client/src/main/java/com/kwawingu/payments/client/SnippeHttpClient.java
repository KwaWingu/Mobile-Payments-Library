/*
 * Copyright 2021-2024 KwaWingu.
 */
package com.kwawingu.payments.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kwawingu.payments.client.response.PaymentResponse;
import com.kwawingu.payments.client.response.PaymentStatus;
import com.kwawingu.payments.client.response.PayoutResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SnippeHttpClient {
  private static final Logger LOG = LoggerFactory.getLogger(SnippeHttpClient.class);
  private static final String DEFAULT_BASE_URL = "https://api.snippe.sh";
  private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

  // HttpClient is thread-safe and pools connections; share a single instance across all callers
  // instead of allocating a new client (and connection pool/executor) per provider object.
  private static final HttpClient SHARED_HTTP_CLIENT =
      HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();

  private final HttpClient httpClient;
  private final String baseUrl;
  private final Gson gson;

  public SnippeHttpClient() {
    this(SHARED_HTTP_CLIENT, DEFAULT_BASE_URL);
  }

  /** Test/advanced seam: inject a client (e.g. with custom timeouts) and/or a sandbox base URL. */
  public SnippeHttpClient(HttpClient httpClient, String baseUrl) {
    this.httpClient = httpClient;
    this.baseUrl = baseUrl;
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

  private String post(
      String path, SnippeApiKey apiKey, String idempotencyKey, Map<String, Object> body)
      throws IOException, InterruptedException {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");
    apiKey.insertAuthorizationHeader(headers);
    if (idempotencyKey != null && !idempotencyKey.isBlank()) {
      if (idempotencyKey.length() > PayloadValidation.MAX_REFERENCE_LENGTH) {
        throw new IllegalArgumentException(
            "idempotency key must be ≤"
                + PayloadValidation.MAX_REFERENCE_LENGTH
                + " chars, got: "
                + idempotencyKey.length());
      }
      headers.put("Idempotency-Key", idempotencyKey);
    }

    HttpRequest.Builder requestBuilder =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + path))
            .timeout(REQUEST_TIMEOUT)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)));
    headers.forEach(requestBuilder::header);

    HttpResponse<String> response =
        httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      // Do not surface the raw provider body to callers/logs at INFO: it can carry customer PII.
      LOG.warn("Snippe POST {} failed: HTTP {}", path, response.statusCode());
      LOG.debug("Snippe error body for POST {}: {}", path, response.body());
      // Snippe reports business errors (e.g. PAY_004) with a non-2xx status AND a structured
      // error_code in the body; parse it so callers can branch on the code, not the message.
      String errorCode = null;
      String apiMessage = null;
      try {
        ApiResponse err = gson.fromJson(response.body(), ApiResponse.class);
        if (err != null) {
          errorCode = err.error_code;
          apiMessage = err.message;
        }
      } catch (RuntimeException ignored) {
        // Non-JSON error body; fall back to a generic message below.
      }
      String message =
          errorCode != null
              ? "Snippe error ["
                  + errorCode
                  + "] (HTTP "
                  + response.statusCode()
                  + "): "
                  + apiMessage
              : "Snippe HTTP error " + response.statusCode() + " for POST " + path;
      throw new SnippeApiException(message, errorCode, response.statusCode());
    }

    LOG.debug("Snippe POST {} → {}", path, response.statusCode());
    return response.body();
  }

  private PaymentResponse parsePaymentResponse(String body) throws IOException {
    JsonObject data = successData(body);
    JsonObject amount = requireObject(data, "amount");
    String createdAt = optString(data, "created_at");
    if (createdAt == null) {
      createdAt = optString(data, "expires_at");
    }
    return new PaymentResponse(
        requireString(data, "reference"),
        PaymentStatus.from(requireString(data, "status")),
        requireLong(amount, "value"),
        requireString(amount, "currency"),
        createdAt,
        optString(data, "completed_at"));
  }

  private PayoutResponse parsePayoutResponse(String body) throws IOException {
    JsonObject data = successData(body);
    JsonObject amount = requireObject(data, "amount");
    return new PayoutResponse(
        requireString(data, "reference"),
        PaymentStatus.from(requireString(data, "status")),
        requireLong(amount, "value"),
        requireString(amount, "currency"));
  }

  private JsonObject successData(String body) throws IOException {
    ApiResponse r = gson.fromJson(body, ApiResponse.class);
    if (r == null) {
      throw new IOException("Snippe error: empty or malformed response");
    }
    if (!"success".equals(r.status)) {
      throw new SnippeApiException(
          "Snippe error [" + r.error_code + "]: " + r.message, r.error_code, 0);
    }
    if (r.data == null) {
      throw new IOException("Snippe error: response missing 'data' field");
    }
    return r.data;
  }

  private static String requireString(JsonObject obj, String key) throws IOException {
    JsonElement el = obj.get(key);
    if (el == null || el.isJsonNull()) {
      throw new IOException("Snippe response missing required field: " + key);
    }
    return el.getAsString();
  }

  private static long requireLong(JsonObject obj, String key) throws IOException {
    JsonElement el = obj.get(key);
    if (el == null || el.isJsonNull()) {
      throw new IOException("Snippe response missing required field: " + key);
    }
    return el.getAsLong();
  }

  private static JsonObject requireObject(JsonObject obj, String key) throws IOException {
    JsonElement el = obj.get(key);
    if (el == null || !el.isJsonObject()) {
      throw new IOException("Snippe response missing required object field: " + key);
    }
    return el.getAsJsonObject();
  }

  private static @Nullable String optString(JsonObject obj, String key) {
    return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
  }

  private static class ApiResponse {
    @Nullable String status = null;
    @Nullable String error_code = null;
    @Nullable String message = null;
    @Nullable JsonObject data = null;
  }
}
