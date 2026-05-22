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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SnippeHttpClient {
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

  private String post(
      String path, SnippeApiKey apiKey, String idempotencyKey, Map<String, Object> body)
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

    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      throw new IOException(
          "Snippe HTTP error "
              + response.statusCode()
              + " for POST "
              + path
              + ": "
              + response.body());
    }

    LOG.debug("Snippe POST {} → {}", path, response.statusCode());
    return response.body();
  }

  private PaymentResponse parsePaymentResponse(String body) throws IOException {
    ApiResponse r = gson.fromJson(body, ApiResponse.class);
    if (!"success".equals(r.status)) {
      throw new IOException("Snippe error [" + r.error_code + "]: " + r.message);
    }
    if (r.data == null) {
      throw new IOException("Snippe error: response missing 'data' field. body=" + body);
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
    if (r.data == null) {
      throw new IOException("Snippe error: response missing 'data' field. body=" + body);
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
    if (r.data == null) {
      throw new IOException("Snippe error: response missing 'data' field. body=" + body);
    }
    JsonObject data = r.data;
    // Field name needs sandbox verification — try checkout_url, then redirect_url
    if (data.has("checkout_url")) {
      return data.get("checkout_url").getAsString();
    }
    if (data.has("redirect_url")) {
      return data.get("redirect_url").getAsString();
    }
    throw new IOException("Snippe error: no checkout URL in response. Full data: " + data);
  }

  private static class ApiResponse {
    @Nullable String status = null;
    int code = 0;
    @Nullable String error_code = null;
    @Nullable String message = null;
    @Nullable JsonObject data = null;
  }
}
